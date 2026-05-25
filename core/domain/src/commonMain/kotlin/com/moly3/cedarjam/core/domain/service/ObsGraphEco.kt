package com.moly3.cedarjam.core.domain.service

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import com.moly3.cedarjam.core.domain.features.search.ItemType
import com.moly3.cedarjam.core.domain.features.search.SearchEngine
import com.moly3.cedarjam.core.domain.features.search.SearchSyntaxException
import com.moly3.cedarjam.core.domain.features.search.Searchable
import com.moly3.cedarjam.core.domain.func.combine
import com.moly3.cedarjam.core.domain.func.extractLinks
import com.moly3.cedarjam.core.domain.io
import com.moly3.cedarjam.core.domain.model.AnnotationDTO
import com.moly3.cedarjam.core.domain.model.CollectionDTO
import com.moly3.cedarjam.core.domain.model.CollectionRowDTO
import com.moly3.cedarjam.core.domain.model.FileTreeNode
import com.moly3.cedarjam.core.domain.model.FileTreeNode.Companion.getAll
import com.moly3.cedarjam.core.domain.model.FileTypeExt
import com.moly3.cedarjam.core.domain.model.GraphEdge
import com.moly3.cedarjam.core.domain.model.GraphEdgeType
import com.moly3.cedarjam.core.domain.model.ObsidianGraphNode
import com.moly3.cedarjam.core.domain.model.TagCollectionRowDTO
import com.moly3.cedarjam.core.domain.model.TagDTO
import com.moly3.cedarjam.core.domain.model.TagLinkDTO
import com.moly3.cedarjam.core.domain.model.TagLinkDtoData
import com.moly3.cedarjam.core.domain.model.TagToTagDTO
import com.moly3.cedarjam.core.domain.model.UIState
import com.moly3.cedarjam.core.domain.model.bind
import com.moly3.cedarjam.core.domain.model.config.GroupLogic
import com.moly3.cedarjam.core.domain.model.getCollectionGraphId
import com.moly3.cedarjam.core.domain.model.getCollectionRowGraphId
import com.moly3.cedarjam.core.domain.model.getFileTreeNodeGraphId
import com.moly3.cedarjam.core.domain.model.getGraphId
import com.moly3.cedarjam.core.domain.model.getTagGraphId
import com.moly3.cedarjam.core.domain.model.node.GraphFilter
import com.moly3.cedarjam.core.domain.model.node.ObsidianGraphData
import com.moly3.cedarjam.core.domain.model.resultBlock
import com.moly3.cedarjam.core.domain.model.toFileType
import com.moly3.cedarjam.core.domain.repository.IAppEnvironment
import com.moly3.dataviz.core.graph.model.ArrowHead
import com.moly3.dataviz.core.graph.model.ArrowStyle
import com.moly3.dataviz.core.graph.model.Connection
import com.moly3.dataviz.core.graph.model.GraphNode
import com.moly3.dataviz.core.graph.model.LineStyle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class ObsGraphEco(
    private val scope: CoroutineScope,
    appEnvironment: IAppEnvironment,
    private val workspaceSession: WorkspaceSession,
    startTargetId: String? = null,
    config: GraphFilter = GraphFilter.Companion.Default
) {
    private val searchEngine = SearchEngine()
    private val contentCache = HashMap<FileCacheKey, String>()
    private val _settingsStateFlow = MutableStateFlow(config)
    private val _targetId = MutableStateFlow(startTargetId)
    val graphState: StateFlow<GraphFilter> = _settingsStateFlow

    private val _groupsStateFlow = MutableStateFlow<List<GroupLogic>>(emptyList())

    /**
     * Cache of extracted+prefixed link lists keyed by file path + modifiedTime.
     * When a file's content changes its modifiedTime changes, so the key
     * changes and the stale entry is naturally bypassed. Bounded by pruning
     * to whatever set of files is currently present (see fileLinkEdgesFlow).
     *
     * KMP-safe: this is a plain HashMap, NOT a concurrent map. It is only ever
     * mutated from the single-threaded body of fileLinkEdgesFlow (the I/O
     * happens upstream in rawFileTextFlow now, so there is no async fan-out
     * here at all). Since fileLinkEdgesFlow is shareIn'd, only one collector
     * pass mutates it at a time, so no synchronization is required.
     */
    private val linkCache = HashMap<FileCacheKey, List<String>>()

    private data class FileCacheKey(val path: String, val modifiedTime: Long)

    fun setGraphConfig(config: GraphFilter) {
        scope.launch(io) {
            // Dedupe at the source: every downstream .map{}.distinctUntilChanged()
            // chain runs its map lambda on each emission, so suppressing equal
            // emissions here avoids a cascade of useless recomputation.
            if (_settingsStateFlow.value != config) {
                _settingsStateFlow.emit(config)
            }
        }
    }

    // ---------------------------------------------------------------------
    // Edge style policy.
    //
    // styleFor maps each GraphEdgeType to the ArrowStyle used when rendering
    // a Connection of that type. This is the single place to tune per-edge
    // visual identity — colors are left Unspecified so the theme's
    // resolvedEdgeColor wins by default; set explicit colors here only when
    // an edge type warrants its own hue regardless of theme.
    //
    // isMutual decides whether an edge contributes one Connection (A -> B)
    // or two (A -> B AND B -> A). Edges that are mutual in meaning — e.g.
    // a directory containing a file, or a free-form link between two notes —
    // are mirrored so both endpoints surface them. Directional edges
    // (annotations attached to files, tags applied to things, tag-to-tag
    // relations) stay one-way and the arrowhead conveys direction.
    // ---------------------------------------------------------------------
    private fun styleFor(type: GraphEdgeType): ArrowStyle {
        return ArrowStyle(head = ArrowHead.None, line = LineStyle.Solid, color = Color.Unspecified)
        return when (type) {
            GraphEdgeType.DirectoryContainsFile -> ArrowStyle.Default
            GraphEdgeType.RowInCollection       -> ArrowStyle.Default

            is GraphEdgeType.FileLink -> ArrowStyle(
                head = ArrowHead.Open,
                line = LineStyle.Solid,
            )

            GraphEdgeType.AnnotationOnFile -> ArrowStyle(head = ArrowHead.Open)
            GraphEdgeType.AnnotationOnRow  -> ArrowStyle(head = ArrowHead.Open)

            GraphEdgeType.TagOnFile -> ArrowStyle(
                head = ArrowHead.Open,
                line = LineStyle.Dashed,
            )
            GraphEdgeType.TagOnRow -> ArrowStyle(
                head = ArrowHead.Open,
                line = LineStyle.Dashed,
            )

            GraphEdgeType.RowOnFile -> ArrowStyle(head = ArrowHead.Open)

            GraphEdgeType.TagToTag -> ArrowStyle(head = ArrowHead.FilledTriangle)
        }
    }

    private fun isMutual(type: GraphEdgeType): Boolean = when (type) {
        GraphEdgeType.DirectoryContainsFile -> true
        is GraphEdgeType.FileLink           -> true
        else                                -> false
    }

    private val isShowTagsFlow = _settingsStateFlow
        .map { if (it.isRealFiles) false else it.isTags }
        .distinctUntilChanged()

    private val isShowCollectionsFlow = _settingsStateFlow
        .map { if (it.isRealFiles) false else it.isCollections }
        .distinctUntilChanged()

    private val isShowRowsFlow = _settingsStateFlow
        .map { if (it.isRealFiles) false else it.isRows }
        .distinctUntilChanged()

    private val isShowAnnotationsFlow = _settingsStateFlow
        .map { if (it.isRealFiles) false else it.isAnnotations }
        .distinctUntilChanged()

    private fun <T> Flow<T>.scoping(): Flow<T> {
        return this
            .flowOn(io)
            .shareIn(
                scope = scope,
                started = SharingStarted.Lazily,
                replay = 1
            )
    }

    // OPTIMIZED: when toggle is off, emit emptyList() (shared singleton, zero alloc).
    private val tagLinksFlow: Flow<List<TagLinkDTO>> =
        combine(isShowTagsFlow, workspaceSession.tagLinksFlow) { show, tagLinks ->
            if (show) tagLinks else emptyList()
        }.scoping()

    private val tagToTagsFlow: Flow<List<TagToTagDTO>> =
        combine(isShowTagsFlow, workspaceSession.tagToTagsFlow) { show, tagToTags ->
            if (show) tagToTags else emptyList()
        }.scoping()

    private val tagRowsFlow: Flow<List<TagCollectionRowDTO>> =
        combine(isShowTagsFlow, workspaceSession.tagCollectionRowsFlow) { show, items ->
            if (show) items else emptyList()
        }.scoping()

    private val tagsFlow: Flow<List<TagDTO>> =
        combine(isShowTagsFlow, workspaceSession.tagsFlow) { show, tags ->
            if (show) tags else emptyList()
        }.scoping()

    private val collectionsFlow: Flow<List<CollectionDTO>> =
        combine(isShowCollectionsFlow, workspaceSession.collectionsFlow) { show, items ->
            if (show) items else emptyList()
        }.scoping()

    private val annotationsFlow: Flow<List<AnnotationDTO>> =
        combine(isShowAnnotationsFlow, workspaceSession.annotationsFlow) { show, items ->
            if (show) items else emptyList()
        }.scoping()

    private val collectionRowsFlow: Flow<List<CollectionRowDTO>> =
        combine(isShowRowsFlow, workspaceSession.collectionRowsFlow) { show, items ->
            if (show) items else emptyList()
        }.scoping()

    private val filesFlow: Flow<List<FileTreeNode>> =
        combine(
            _settingsStateFlow
                .map { Pair(it.isShowDirectories, it.isRealFiles) }
                .distinctUntilChanged(),
            workspaceSession.filesFlow.map {
                when (it) {
                    is UIState.Error -> emptyList()
                    is UIState.Loading -> emptyList()
                    is UIState.Success -> it.data.getAll()
                }
            }
        ) { filesConfig, files ->
            val showDirectories = filesConfig.first
            val allFiles = ArrayList<FileTreeNode>(files.size)
            for (item in files) {
                when (item) {
                    is FileTreeNode.Directory -> if (showDirectories) allFiles.add(item)
                    is FileTreeNode.File -> allFiles.add(item)
                }
            }
            allFiles
        } .debounce(150L)   // collapse rapid bursts during indexing
            .scoping()

    private val graphNodesFlow = kotlinx.coroutines.flow.combine(
        collectionsFlow,
        collectionRowsFlow,
        tagsFlow,
        filesFlow,
        annotationsFlow
    ) { collections, rows, tags, files, annotations ->
        val total = collections.size + rows.size + tags.size + files.size + annotations.size
        val graphNodes = ArrayList<ObsidianGraphNode>(total)
        val times = HashMap<Any, Long>(total)
        // OPTIMIZED: build the id lookup as a HashSet directly. The downstream
        // `nodes` flow needs O(1) `contains`, so producing a Set here avoids a
        // List->Set conversion on every emission later.
        val ids = HashSet<Any>(total)

        for (item in collections) {
            val graphId = item.getGraphId()
            graphNodes.add(
                GraphNode(
                    id = graphId,
                    name = item.name,
                    colorValue = GREEN_COLOR,
                    data = ObsidianGraphData.Collection(id = item.id)
                )
            )
            ids.add(graphId)
            times[graphId] = item.modifiedTime
        }
        for (item in annotations) {
            val graphId = item.getGraphId()
            graphNodes.add(
                GraphNode(
                    id = graphId,
                    name = "annotation: page ${item.dataPoint}",
                    colorValue = RED_COLOR,
                    data = ObsidianGraphData.Annotation(
                        id = item.id,
                        dataPath = item.dataPath,
                        dataPoint = item.dataPoint
                    )
                )
            )
            ids.add(graphId)
            times[graphId] = item.modifiedTime
        }
        for (item in rows) {
            val graphId = item.getGraphId()
            graphNodes.add(
                GraphNode(
                    id = graphId,
                    name = item.name,
                    colorValue = null,
                    data = ObsidianGraphData.CollectionRow(
                        id = item.id,
                        collectionId = item.collectionId
                    )
                )
            )
            ids.add(graphId)
            times[graphId] = item.modifiedTime
        }
        for (item in tags) {
            val graphId = item.getGraphId()
            graphNodes.add(
                GraphNode(
                    id = graphId,
                    name = item.name,
                    colorValue = item.color.value,
                    data = ObsidianGraphData.Tag(item.id)
                )
            )
            ids.add(graphId)
            times[graphId] = item.modifiedTime
        }
        for (item in files) {
            val graphId = item.getGraphId()
            val color = if (item is FileTreeNode.File) BLUE_COLOR else CYAN_COLOR
            graphNodes.add(
                GraphNode(
                    id = graphId,
                    name = item.getShortName(),
                    colorValue = color,
                    data = ObsidianGraphData.File(
                        item.getRelativePath(),
                        isDirectory = item.isDirectory(),
                        extension = item.getExtension(),
                        fullPath = item.getFullPath()
                    )
                )
            )
            ids.add(graphId)
            times[graphId] = item.modifiedTime
        }

        Triple<Set<Any>, List<ObsidianGraphNode>, Map<Any, Long>>(ids, graphNodes, times)
    }.scoping()


    @OptIn(ExperimentalTime::class)
    private fun makeGradation(
        modifiedTimes: Map<Any, Long>,
        filtered: List<ObsidianGraphNode>,
        primaryColor: Color,
        defaultColor: Color
    ): List<ObsidianGraphNode> {
        val maxLastTime = Clock.System.now().toEpochMilliseconds()

        var minLastTime = Long.MAX_VALUE
        for (item in filtered) {
            val t = modifiedTimes[item.id] ?: 0L
            if (t > 0 && t < minLastTime) minLastTime = t
        }
        if (minLastTime == Long.MAX_VALUE) minLastTime = 0L

        val range = (maxLastTime - minLastTime).takeIf { it > 0 } ?: 1L
        val rangeF = range.toFloat()

        return filtered.map { item ->
            val t = modifiedTimes[item.id] ?: 0L
            val fraction = ((t - minLastTime).toFloat() / rangeF).coerceIn(0f, 1f)
            val blendedColor = lerp(defaultColor, primaryColor, fraction)
            item.copy(colorValue = blendedColor.value)
        }
    }

    private inline fun MutableList<GraphEdge>.add(
        from: String,
        to: String,
        type: GraphEdgeType,
    ) {
        if (from == to) return // skip self-loops at the source
        add(GraphEdge(from, to, type))
    }

    /**
     * Reachability filter for the directional Connection adjacency.
     *
     * Same two-phase walk as before:
     *   1. Walk backwards from targetId through reverse edges to gather every
     *      node that can reach the target ("sources").
     *   2. Walk forwards from those sources to gather every node reachable
     *      from them ("visibleNodes").
     *   3. Keep only Connections whose source AND target are visible.
     *
     * Styles ride along untouched — the walk only cares about node identity.
     */
    fun filterConnections(
        connections: Map<String, List<Connection<String>>>,
        targetId: String,
    ): Map<String, List<Connection<String>>> {

        val reverse = HashMap<String, MutableList<String>>(connections.size)
        for ((from, conns) in connections) {
            for (c in conns) {
                if (c.target == from) continue
                reverse.getOrPut(c.target) { ArrayList(2) }.add(from)
            }
        }

        val sources = HashSet<String>()
        val stack = ArrayDeque<String>()
        stack.addLast(targetId)
        sources.add(targetId)
        while (stack.isNotEmpty()) {
            val node = stack.removeLast()
            val prevs = reverse[node] ?: continue
            for (prev in prevs) {
                if (sources.add(prev)) stack.addLast(prev)
            }
        }

        val visibleNodes = HashSet<String>(sources.size * 2)
        val forwardStack = ArrayDeque<String>(sources.size)
        for (s in sources) forwardStack.addLast(s)
        while (forwardStack.isNotEmpty()) {
            val node = forwardStack.removeLast()
            if (visibleNodes.add(node)) {
                val nexts = connections[node] ?: continue
                for (c in nexts) forwardStack.addLast(c.target)
            }
        }

        val result = HashMap<String, List<Connection<String>>>(visibleNodes.size)
        for ((k, v) in connections) {
            if (k !in visibleNodes) continue
            val filtered = ArrayList<Connection<String>>(v.size)
            for (c in v) {
                if (c.target in visibleNodes) filtered.add(c)
            }
            result[k] = filtered
        }
        return result
    }

    /**
     * Single source of truth for file text. Reads every text file once,
     * cached by path+modifiedTime, and is shared by:
     *   - fileContentFlow      (gated by isIndexFileContent for search indexing)
     *   - fileLinkEdgesFlow    (always uses it, then runs extractLinks())
     *
     * Previously these two flows each opened every text file independently,
     * doubling disk I/O when content indexing was enabled.
     *
     * Key: file graphId  ->  raw text content (empty-string entries are dropped).
     */
    private val rawFileTextFlow: Flow<Map<String, String>> = filesFlow
        .map { files ->
            val ws = workspaceSession.workspaceEnvStateFlow.value
            val result = HashMap<String, String>(files.size)
            val liveKeys = HashSet<FileCacheKey>()

            coroutineScope {
                val deferreds = ArrayList<Deferred<Triple<FileCacheKey, String, String>?>>()

                for (item in files) {
                    if (item !is FileTreeNode.File) continue
                    if (item.name.extension.toFileType() != FileTypeExt.Text) continue

                    val graphId = item.getGraphId()
                    val cacheKey = FileCacheKey(item.getFullPath(), item.modifiedTime)
                    liveKeys.add(cacheKey)

                    val cached = contentCache[cacheKey]
                    if (cached != null) {
                        if (cached.isNotEmpty()) result[graphId] = cached
                        continue
                    }

                    deferreds.add(async(io) {
                        val text = resultBlock(onError = { "" }) {
                            bind(ws.getNodeText(item))
                        } as? String ?: return@async null
                        Triple(cacheKey, graphId, text)
                    })
                }

                for (d in deferreds) {
                    val triple = d.await() ?: continue
                    // Single-threaded merge step — safe to write the plain HashMap.
                    contentCache[triple.first] = triple.third
                    if (triple.third.isNotEmpty()) result[triple.second] = triple.third
                }
            }

            // Prune deleted / changed files; keep memory bounded to current file set.
            if (contentCache.size > liveKeys.size) {
                contentCache.keys.retainAll(liveKeys)
            }

            @Suppress("USELESS_CAST")
            result as Map<String, String>
        }
        .scoping()

    private val isIndexContentFlow = _settingsStateFlow
        .map { it.isIndexFileContent }
        .distinctUntilChanged()

    /**
     * Search-indexable file text. Just a cheap gate over [rawFileTextFlow]:
     * when content indexing is off, downstream search sees no file bodies,
     * but [fileLinkEdgesFlow] still gets the text it needs for link extraction.
     */
    private val fileContentFlow: Flow<Map<String, String>> =
        combine(isIndexContentFlow, rawFileTextFlow) { on, texts ->
            if (on) texts else emptyMap()
        }.scoping()

    private val searchQueryFlow = _settingsStateFlow
        .map { it.search }
        .distinctUntilChanged()

    /**
     * Casts every graph node into a [Searchable] so the search engine can index
     * the whole graph uniformly. File content is optional: only filled when
     * fileContentFlow has data (driven by isIndexFileContent), otherwise the
     * `content` field is left blank for files.
     */
    val searchablesWithIdFlow: Flow<Map<String, Searchable>> = combine(
        collectionsFlow,
        collectionRowsFlow,
        tagsFlow,
        filesFlow,
        annotationsFlow,
        fileContentFlow,
    ) { collections, rows, tags, files, annotations, fileContent ->

        val total = collections.size + rows.size + tags.size + files.size + annotations.size
        val out = HashMap<String, Searchable>(total)

        for (item in collections) {
            val graphId = item.getGraphId()
            out[graphId] = Searchable(
                type = ItemType.COLLECTION,
                fileName = item.name,
                path = graphId,
                content = "",
            )
        }

        for (item in tags) {
            val graphId = item.getGraphId()
            out[graphId] = Searchable(
                type = ItemType.TAG,
                fileName = item.name,
                path = graphId,
                content = "",
                tags = setOf(item.name),
            )
        }

        for (item in rows) {
            val graphId = item.getGraphId()
            out[graphId] = Searchable(
                type = ItemType.ROW,
                fileName = item.name,
                path = item.fileRelativePath ?: graphId,
                content = buildString {
                    item.exampleSentence?.let { appendLine(it) }
                    item.translation?.let { appendLine(it) }
                    item.pronunciation?.let { appendLine(it) }
                }.trim(),
            )
        }

        for (item in annotations) {
            val graphId = item.getGraphId()
            out[graphId] = Searchable(
                type = ItemType.ANNOTATION,
                fileName = "annotation: page ${item.dataPoint}",
                path = item.dataPath,
                content = "",
            )
        }

        for (item in files) {
            val graphId = item.getGraphId()
            out[graphId] = Searchable(
                type = ItemType.FILE,
                fileName = item.getFullName(),
                path = item.getRelativePath(),
                content = fileContent[graphId] ?: "",
            )
        }

        out
    }.scoping()


    /**
     * Graph ids of nodes that satisfy the active search query.
     *
     * - Blank query  -> null  (sentinel for "no filtering", avoids matching every node)
     * - Invalid query -> null (fail-open; the half-typed query shouldn't empty the graph)
     * - Valid query  -> Set of matching graphIds
     */
    private val searchMatchIdsFlow: Flow<Set<Any>?> = combine(
        searchQueryFlow,
        searchablesWithIdFlow,
    ) { query, searchables ->
        if (query.isBlank()) return@combine null

        val compiled = try {
            searchEngine.compile(query)
        } catch (e: SearchSyntaxException) {
            return@combine null    // fail-open
        }

        val matched = HashSet<Any>(searchables.size)
        for ((graphId, searchable) in searchables) {
            if (searchEngine.matches(compiled, searchable)) {
                matched.add(graphId)
            }
        }
        matched
    }.scoping()


    fun setGroups(groups: List<GroupLogic>) {
        val filteredGroups = groups.filter { d -> d.isVisible }
        scope.launch(io) {
            if (_groupsStateFlow.value != filteredGroups) {
                _groupsStateFlow.emit(filteredGroups)
            }
        }
    }

    val nodeLandsFlow: Flow<Map<String, List<String>>> = combine(
        _groupsStateFlow,
        searchablesWithIdFlow
    ) { groups, searchables ->
        val result = HashMap<String, MutableList<String>>()

        for (group in groups) {
            if (group.filter.isBlank()) continue

            val compiled = try {
                searchEngine.compile(group.filter)
            } catch (e: SearchSyntaxException) {
                continue
            }

            for ((graphId, searchable) in searchables) {
                if (searchEngine.matches(compiled, searchable)) {
                    result.getOrPut(graphId) { ArrayList(2) }.add(group.name)
                }
            }
        }

        @Suppress("UNCHECKED_CAST")
        result as Map<String, List<String>>
    }.scoping()

    private val nodeGroupColorsFlow: Flow<Map<String, ULong>> = combine(
        _groupsStateFlow,
        searchablesWithIdFlow
    ) { groups, searchables ->
        val result = HashMap<String, ULong>()

        for (group in groups) {
            if (group.filter.isBlank()) continue

            val compiled = try {
                searchEngine.compile(group.filter)
            } catch (e: SearchSyntaxException) {
                continue
            }

            for ((graphId, searchable) in searchables) {
                if (!result.containsKey(graphId) && searchEngine.matches(compiled, searchable)) {
                    result[graphId] = group.color.value
                }
            }
        }
        result
    }.scoping()

    private val coloredGraphNodesFlow: Flow<Triple<Set<Any>, List<ObsidianGraphNode>, Map<Any, Long>>> =
        kotlinx.coroutines.flow.combine(
            graphNodesFlow,
            nodeGroupColorsFlow
        ) { (validIds, nodes, times), colorOverrides ->
            val coloredNodes = if (colorOverrides.isEmpty()) {
                nodes
            } else {
                nodes.map { node ->
                    val key = node.id as? String ?: node.id.toString()
                    val overrideColor = colorOverrides[key]
                    if (overrideColor != null) node.copy(colorValue = overrideColor) else node
                }
            }
            Triple(validIds, coloredNodes, times)
        }.scoping()

    /**
     * Optional final pass. When gradations are ON, recolors EVERY node by
     * modifiedTime, discarding static colors AND group colors. When OFF,
     * passes the group-colored nodes through untouched.
     */
    private val gradatedGraphNodesFlow: Flow<Pair<Set<Any>, List<ObsidianGraphNode>>> =
        kotlinx.coroutines.flow.combine(
            _settingsStateFlow.map { it.isGradations }.distinctUntilChanged(),
            coloredGraphNodesFlow,
            appEnvironment.getAppSettingsFlow().map { it.theme.primaryColor }
                .distinctUntilChanged(),
            appEnvironment.getAppSettingsFlow().map { it.theme.colors.primaryFont }
                .distinctUntilChanged()
        ) { gradations, triple, primaryColor, defaultColor ->
            val nodes = if (gradations) {
                makeGradation(
                    triple.third,
                    triple.second,
                    primaryColor = primaryColor,
                    defaultColor = defaultColor
                )
            } else {
                triple.second
            }
            Pair(triple.first, nodes)
        }.scoping()

    /**
     * File-link edges. Pure CPU step: the disk I/O already happened upstream
     * in [rawFileTextFlow], so this just walks the directory tree, emits
     * DirectoryContainsFile edges, and runs extractLinks() on each file's
     * already-loaded text. linkCache memoizes the extracted+prefixed list
     * across emissions so unchanged files skip even the regex work.
     */
    private val fileLinkEdgesFlow: Flow<List<GraphEdge>> = combine(
        filesFlow,
        workspaceSession.workspaceFlow,
        rawFileTextFlow,
    ) { files, workspace, rawTexts ->
        val workspacePathPrefix = "filenode: ${workspace.fullpath}/"

        val edges = ArrayList<GraphEdge>(files.size + 16)
        val liveKeys = HashSet<FileCacheKey>()

        for (directory in files) {
            if (!directory.isDirectory()) continue
            val children = directory.getChildrenOrNull() ?: continue
            val dirGraphId = directory.getGraphId()

            for (file in children) {
                val fileGraphId = file.getGraphId()
                edges.add(dirGraphId, fileGraphId, GraphEdgeType.DirectoryContainsFile)

                if (file !is FileTreeNode.File) continue
                if (file.name.extension.toFileType() != FileTypeExt.Text) continue

                val cacheKey = FileCacheKey(file.getFullPath(), file.modifiedTime)
                liveKeys.add(cacheKey)

                // Reuse extracted-link cache: extractLinks is pure CPU but still
                // worth memoizing across emissions when the file hasn't changed.
                val cachedLinks = linkCache[cacheKey]
                if (cachedLinks != null) {
                    for (target in cachedLinks) {
                        edges.add(fileGraphId, target, GraphEdgeType.FileLink())
                    }
                    continue
                }

                val text = rawTexts[fileGraphId] ?: continue
                val rawLinks = text.extractLinks()
                val prefixed = if (rawLinks.isEmpty()) {
                    emptyList()
                } else {
                    val ssLinks = ArrayList<String>(rawLinks.size)
                    for (link in rawLinks) ssLinks.add(workspacePathPrefix + link)
                    ssLinks
                }
                linkCache[cacheKey] = prefixed
                for (target in prefixed) {
                    edges.add(fileGraphId, target, GraphEdgeType.FileLink())
                }
            }
        }

        if (linkCache.size > liveKeys.size) {
            linkCache.keys.retainAll(liveKeys)
        }

        edges
    }.scoping()


    private val metadataEdgesFlow: Flow<List<GraphEdge>> =
        com.moly3.cedarjam.core.domain.func.combine(
            collectionRowsFlow,
            tagToTagsFlow,
            tagLinksFlow,
            tagRowsFlow,
            annotationsFlow,
        ) { rows, tagToTags, tagLinks, tagRows, annotations ->
            val edges = ArrayList<GraphEdge>(
                rows.size * 2 + tagToTags.size + tagLinks.size + annotations.size * 2 + 16
            )

            // 1) Annotations
            for (item in annotations) {
                val annId = item.getGraphId()
                edges.add(annId, item.dataPath.getFileTreeNodeGraphId(), GraphEdgeType.AnnotationOnFile)
                if (item.rowId != null) {
                    edges.add(annId, item.rowId.getCollectionRowGraphId(), GraphEdgeType.AnnotationOnRow)
                }
            }

            // 2) Collection rows
            for (item in rows) {
                val rowId = item.getGraphId()
                edges.add(rowId, item.collectionId.getCollectionGraphId(), GraphEdgeType.RowInCollection)
                val rel = item.fileRelativePath
                if (!rel.isNullOrEmpty()) {
                    edges.add(rowId, rel.getFileTreeNodeGraphId(), GraphEdgeType.RowOnFile)
                }
            }

            // 3) Tag-row links
            for (item in tagRows) {
                edges.add(
                    item.tagId.getTagGraphId(),
                    item.rowId.getCollectionRowGraphId(),
                    GraphEdgeType.TagOnRow,
                )
            }

            // 4) Tag links to files
            for (tagLink in tagLinks) {
                val tagGraphId = tagLink.tagId.getTagGraphId()
                when (val data = tagLink.data) {
                    is TagLinkDtoData.FileNode -> {
                        edges.add(
                            tagGraphId,
                            data.relativePath.getFileTreeNodeGraphId(),
                            GraphEdgeType.TagOnFile,
                        )
                    }
                }
            }

            // 5) Tag-to-tag (note: original code put secondTagId as "from")
            for (item in tagToTags) {
                edges.add(
                    item.secondTagId.getTagGraphId(),
                    item.firstTagId.getTagGraphId(),
                    GraphEdgeType.TagToTag,
                )
            }

            edges
        }.scoping()


    private val oldConnectionsFlow: Flow<List<GraphEdge>> = combine(
        fileLinkEdgesFlow,
        metadataEdgesFlow,
    ) { fileEdges, metaEdges ->
        val merged = ArrayList<GraphEdge>(fileEdges.size + metaEdges.size)
        merged.addAll(fileEdges)
        merged.addAll(metaEdges)
        merged
    }.scoping()

    /**
     * Directional Connection adjacency.
     *
     * Each GraphEdge becomes at least one Connection, styled via [styleFor].
     * Edges whose type is [isMutual] also contribute the reverse Connection,
     * so both endpoints surface them in the rendered graph. Other edge types
     * stay one-way and the arrowhead conveys direction.
     *
     * Dedupe key is (target, edgeType): two edges of different types between
     * the same pair survive as two distinct Connections, which is exactly
     * the "two types of connections" case the directional Connection model
     * was designed for.
     *
     * Split from connectionsFlow so that changing _targetId only re-runs the
     * cheap reachability filter, not the full adjacency rebuild.
     */
    private val adjacencyFlow: Flow<Map<String, List<Connection<String>>>> =
        oldConnectionsFlow
            .map { edgesToConnectionAdjacency(it) }
            .scoping()

    val connectionsFlow: Flow<Map<String, List<Connection<String>>>> = combine(
        adjacencyFlow,
        _targetId,
    ) { adjacency, targetId ->
        if (targetId != null) filterConnections(adjacency, targetId) else adjacency
    }.scoping()

    val nodesFlow = kotlinx.coroutines.flow.combine(
        gradatedGraphNodesFlow,
        connectionsFlow,
        _settingsStateFlow.map { Pair(it.isOrphans, it.maxNodes) }.distinctUntilChanged(),
        searchMatchIdsFlow,
    ) { graphNodes, connections, isOrphansAndMax, searchIds ->
        val (showOrphans, maxNodes) = isOrphansAndMax
        val validIds: Set<Any> = graphNodes.first

        // Precompute the set of "connected" ids once instead of scanning each
        // node's neighbors during the filter loop. One pass over connections
        // builds a HashSet of every endpoint that participates in a surviving
        // edge; the per-node loop below is then a single O(1) lookup.
        val connectedIds: Set<String>? = if (showOrphans) null else {
            val s = HashSet<String>()
            for ((from, conns) in connections) {
                for (c in conns) {
                    val target = c.target
                    val passesSearch = searchIds == null || target in searchIds
                    if (target in validIds && passesSearch) {
                        s.add(from)
                        s.add(target)
                    }
                }
            }
            s
        }

        val nodes = ArrayList<ObsidianGraphNode>(graphNodes.second.size.coerceAtMost(maxNodes))
        for (node in graphNodes.second) {
            if (nodes.size >= maxNodes) break
            if (searchIds != null && node.id !in searchIds) continue
            if (connectedIds == null || (node.id as? String) in connectedIds) {
                nodes.add(node)
            }
        }
        nodes
    }.scoping()


    private fun edgesToConnectionAdjacency(
        edges: List<GraphEdge>,
    ): Map<String, List<Connection<String>>> {

        // (from -> ((target, edgeType) -> Connection))
        val acc = HashMap<String, LinkedHashMap<Pair<String, GraphEdgeType>, Connection<String>>>(edges.size)

        fun addOne(from: String, to: String, type: GraphEdgeType) {
            if (from == to) return
            val bucket = acc.getOrPut(from) { LinkedHashMap(4) }
            val key = to to type
            if (key !in bucket) {
                bucket[key] = Connection(target = to, style = styleFor(type))
            }
        }

        // Phase 1: build adjacency from raw edges (one-way per edge).
        for (e in edges) {
            addOne(e.target1, e.target2, e.type)
            if (isMutual(e.type)) {
                addOne(e.target2, e.target1, e.type)
            }
        }

        // Phase 2: symmetrize. For every A -> B of type T, ensure B -> A of type T
        // exists. If the reverse is already present (genuinely bidirectional edge),
        // this is a no-op thanks to the `key !in bucket` guard in addOne.
        //
        // Snapshot the current entries first so we don't iterate while mutating.
        val snapshot = ArrayList<Triple<String, String, GraphEdgeType>>()
        for ((from, bucket) in acc) {
            for ((key, _) in bucket) {
                snapshot.add(Triple(from, key.first, key.second))
            }
        }
        for ((from, to, type) in snapshot) {
            addOne(to, from, type)
        }

        val out = HashMap<String, List<Connection<String>>>(acc.size)
        for ((from, bucket) in acc) {
            out[from] = bucket.values.toList()
        }
        return out
    }


    private companion object {
        private val GREEN_COLOR = Color.Green.value
        private val RED_COLOR = Color.Red.value
        private val BLUE_COLOR = Color.Blue.value
        private val CYAN_COLOR = Color.Cyan.value
    }
}