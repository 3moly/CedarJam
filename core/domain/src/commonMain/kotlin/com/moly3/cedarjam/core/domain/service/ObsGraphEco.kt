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
     * Cache of extracted links keyed by file path + modifiedTime.
     * When a file's content changes its modifiedTime changes, so the key
     * changes and the stale entry is naturally bypassed. Bounded by pruning
     * to whatever set of files is currently present (see fileLinkEdgesFlow).
     *
     * KMP-safe: this is a plain HashMap, NOT a concurrent map. It is only ever
     * mutated from the single-threaded merge step of fileLinkEdgesFlow (after
     * each async result is awaited), never from inside the async blocks. Since
     * fileLinkEdgesFlow is shareIn'd, only one collector pass mutates it at a
     * time, so no synchronization is required.
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
    private fun styleFor(type: GraphEdgeType): ArrowStyle = when (type) {
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
        }.scoping()

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

    private val isIndexContentFlow = _settingsStateFlow
        .map { it.isIndexFileContent }
        .distinctUntilChanged()

    /**
     * Optional file-content extraction. When [isIndexContentFlow] is false this
     * emits an empty map (zero disk I/O). Cached by path+mtime exactly like
     * fileLinkEdgesFlow, so unchanged files skip getNodeText on later emissions.
     *
     * Key: file graphId  ->  raw text content.
     */
    private val fileContentFlow: Flow<Map<String, String>> = combine(
        isIndexContentFlow,
        filesFlow,
    ) { indexContent, files ->
        if (!indexContent) return@combine emptyMap<String, String>()

        val ws = workspaceSession.workspaceEnvStateFlow.value
        val result = HashMap<String, String>(files.size)
        val liveKeys = HashSet<FileCacheKey>()

        coroutineScope {
            val deferreds =
                ArrayList<Deferred<Triple<FileCacheKey, String, String>?>>()

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
                // single-threaded merge step — safe to write the plain HashMap
                contentCache[triple.first] = triple.third
                if (triple.third.isNotEmpty()) result[triple.second] = triple.third
            }
        }

        // prune deleted / changed files, keep memory bounded to current file set
        if (contentCache.size > liveKeys.size) {
            contentCache.keys.retainAll(liveKeys)
        }

        result as Map<String, String>
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

    private val fileLinkEdgesFlow: Flow<List<GraphEdge>> = combine(
        filesFlow,
        workspaceSession.workspaceFlow,
    ) { files, workspace ->
        val ws = workspaceSession.workspaceEnvStateFlow.value
        val workspacePathPrefix = "filenode: ${workspace.fullpath}/"

        val edges = ArrayList<GraphEdge>(files.size + 16)
        val liveKeys = HashSet<FileCacheKey>()

        coroutineScope {
            val deferreds = ArrayList<Deferred<Triple<FileCacheKey, String, List<String>>?>>()

            for (directory in files) {
                if (!directory.isDirectory()) continue
                val children = directory.getChildrenOrNull() ?: continue
                val dirGraphId = directory.getGraphId()

                for (file in children) {
                    val fileGraphId = file.getGraphId()
                    edges.add(dirGraphId, fileGraphId, GraphEdgeType.DirectoryContainsFile)

                    if (file is FileTreeNode.File &&
                        file.name.extension.toFileType() == FileTypeExt.Text
                    ) {
                        val cacheKey = FileCacheKey(file.getFullPath(), file.modifiedTime)
                        liveKeys.add(cacheKey)

                        val cached = linkCache[cacheKey]
                        if (cached != null) {
                            for (target in cached) {
                                edges.add(fileGraphId, target, GraphEdgeType.FileLink())
                            }
                            continue
                        }

                        deferreds.add(async(io) {
                            val result = resultBlock(onError = { "" }) {
                                val textResult = ws.getNodeText(file)
                                val text = bind(textResult)
                                val links = text.extractLinks()
                                if (links.isEmpty()) {
                                    emptyList<String>()
                                } else {
                                    val ssLinks = ArrayList<String>(links.size)
                                    for (link in links) ssLinks.add(workspacePathPrefix + link)
                                    ssLinks
                                }
                            }
                            @Suppress("UNCHECKED_CAST")
                            val list = result as? List<String> ?: return@async null
                            Triple(cacheKey, fileGraphId, list)
                        })
                    }
                }
            }

            for (d in deferreds) {
                val triple = d.await() ?: continue
                linkCache[triple.first] = triple.third
                for (target in triple.third) {
                    edges.add(triple.second, target, GraphEdgeType.FileLink())
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
     */
    val connectionsFlow: Flow<Map<String, List<Connection<String>>>> = combine(
        oldConnectionsFlow,
        _targetId,
    ) { edges, targetId ->
        val adjacency = edgesToConnectionAdjacency(edges)
        if (targetId != null) filterConnections(adjacency, targetId) else adjacency
    }.scoping()

    val nodesFlow = kotlinx.coroutines.flow.combine(
        gradatedGraphNodesFlow,
        connectionsFlow,
        _settingsStateFlow.map { Pair(it.isOrphans, it.maxNodes) }.distinctUntilChanged(),
        searchMatchIdsFlow,
    ) { graphNodes, connections, isOrphans, searchIds ->
        val showOrphans = isOrphans.first
        val maxNodes = isOrphans.second
        val validIds: Set<Any> = graphNodes.first

        val nodes = ArrayList<ObsidianGraphNode>(graphNodes.second.size.coerceAtMost(maxNodes))
        for (node in graphNodes.second) {
            if (nodes.size >= maxNodes) break

            // Search gate: when a query is active, drop non-matching nodes.
            if (searchIds != null && node.id !in searchIds) continue

            if (showOrphans) {
                nodes.add(node)
            } else {
                val neighbors = connections[node.id]
                if (neighbors != null) {
                    var hasValidNeighbor = false
                    for (c in neighbors) {
                        // A neighbor counts only if it exists in the graph
                        // AND it survives the active search filter.
                        val target = c.target
                        val isNeighborInGraph = target in validIds
                        val doesNeighborSurviveSearch =
                            searchIds == null || target in searchIds

                        if (isNeighborInGraph && doesNeighborSurviveSearch) {
                            hasValidNeighbor = true
                            break
                        }
                    }
                    if (hasValidNeighbor) nodes.add(node)
                }
            }
        }
        nodes
    }.scoping()


    private fun edgesToConnectionAdjacency(
        edges: List<GraphEdge>,
    ): Map<String, List<Connection<String>>> {

        // (from -> ((target, edgeType) -> Connection))
        // LinkedHashMap on the inner bucket preserves edge insertion order,
        // which keeps render order stable across emissions for a given source
        // — useful when overlapping styled lines stack predictably.
        val acc = HashMap<String, LinkedHashMap<Pair<String, GraphEdgeType>, Connection<String>>>(edges.size)

        fun addOne(from: String, to: String, type: GraphEdgeType) {
            if (from == to) return
            val bucket = acc.getOrPut(from) { LinkedHashMap(4) }
            val key = to to type
            if (key !in bucket) {
                bucket[key] = Connection(target = to, style = styleFor(type))
            }
        }

        for (e in edges) {
            addOne(e.target1, e.target2, e.type)
            if (isMutual(e.type)) {
                addOne(e.target2, e.target1, e.type)
            }
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