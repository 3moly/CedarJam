package com.moly3.cedarjam.core.domain.service

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import com.moly3.cedarjam.core.domain.func.extractLinks
import com.moly3.cedarjam.core.domain.func.pathWrapper
import com.moly3.cedarjam.core.domain.io
import com.moly3.cedarjam.core.domain.model.AnnotationDTO
import com.moly3.cedarjam.core.domain.model.CollectionDTO
import com.moly3.cedarjam.core.domain.model.CollectionRowDTO
import com.moly3.cedarjam.core.domain.model.FileTreeNode
import com.moly3.cedarjam.core.domain.model.FileTreeNode.Companion.getAll
import com.moly3.cedarjam.core.domain.model.FileTypeExt
import com.moly3.cedarjam.core.domain.model.ObsidianGraphNode
import com.moly3.cedarjam.core.domain.model.TagCollectionRowDTO
import com.moly3.cedarjam.core.domain.model.TagDTO
import com.moly3.cedarjam.core.domain.model.TagLinkDTO
import com.moly3.cedarjam.core.domain.model.TagLinkDtoData
import com.moly3.cedarjam.core.domain.model.TagToTagDTO
import com.moly3.cedarjam.core.domain.model.UIState
import com.moly3.cedarjam.core.domain.model.bind
import com.moly3.cedarjam.core.domain.model.getCollectionGraphId
import com.moly3.cedarjam.core.domain.model.getCollectionRowGraphId
import com.moly3.cedarjam.core.domain.model.getFileTreeNodeGraphId
import com.moly3.cedarjam.core.domain.model.getGraphId
import com.moly3.cedarjam.core.domain.model.getTagGraphId
import com.moly3.cedarjam.core.domain.model.node.GraphSettingsConfig
import com.moly3.cedarjam.core.domain.model.node.ObsidianGraphData
import com.moly3.cedarjam.core.domain.model.resultBlock
import com.moly3.cedarjam.core.domain.model.toFileType
import com.moly3.cedarjam.core.domain.repository.IAppEnvironment
import com.moly3.dataviz.core.graph.model.GraphNode
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
    config: GraphSettingsConfig = GraphSettingsConfig.Companion.Default
) {

    private val _settingsStateFlow = MutableStateFlow(config)
    private val _targetId = MutableStateFlow(startTargetId)
    val graphState: StateFlow<GraphSettingsConfig> = _settingsStateFlow

    fun setGraphConfig(config: GraphSettingsConfig) {
        scope.launch(io) {
            // Dedupe at the source: every downstream .map{}.distinctUntilChanged()
            // chain runs its map lambda on each emission, so suppressing equal
            // emissions here avoids a cascade of useless recomputation.
            if (_settingsStateFlow.value != config) {
                _settingsStateFlow.emit(config)
            }
        }
    }

    /**
     * OPTIMIZED: builds bidirectional adjacency in a single pass using HashSet
     * to dedupe, then converts to lists.
     */
    private fun makeBidirectional(connections: Map<String, List<String>>): Map<String, List<String>> {
        val approxSize = connections.size * 2
        val adjacency = HashMap<String, HashSet<String>>(approxSize)

        for ((node, neighbors) in connections) {
            val nodeSet = adjacency.getOrPut(node) { HashSet(neighbors.size + 4) }
            for (target in neighbors) {
                if (target == node) continue // skip self-loops
                nodeSet.add(target)
                adjacency.getOrPut(target) { HashSet(4) }.add(node)
            }
        }

        val result = HashMap<String, List<String>>(adjacency.size)
        for ((k, v) in adjacency) {
            result[k] = v.toList()
        }
        return result
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

    private val graphNodesFlow = combine(
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
                    data = ObsidianGraphData.Annotation(id = item.id)
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

    private inline fun MutableMap<String, MutableList<String>>.addEdge(from: String, to: String) {
        getOrPut(from) { ArrayList(4) }.add(to)
    }

    private inline fun MutableMap<String, MutableList<String>>.addEdges(
        from: String,
        to: List<String>
    ) {
        if (to.isEmpty()) return
        getOrPut(from) { ArrayList(to.size + 2) }.addAll(to)
    }

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

    /**
     * OPTIMIZED: file-link extraction is its own flow.
     *
     * Previously this lived inside oldConnectionsFlow, which combined SEVEN
     * sources — so flipping a tag toggle (tagLinksFlow etc.) re-ran every
     * disk read. Now disk I/O reacts ONLY to filesFlow + workspaceFlow.
     *
     * On top of that, each text file's links are cached by path+mtime, so an
     * unchanged file skips getNodeText entirely on subsequent emissions
     * (e.g. when only ONE file changed, the other N-1 are served from cache).
     */
    private val fileLinkEdgesFlow: Flow<Map<String, List<String>>> = combine(
        filesFlow,
        workspaceSession.workspaceFlow,
    ) { files, workspace ->
        val ws = workspaceSession.workspaceEnvStateFlow.value
        val workspacePathPrefix = "filenode: ${workspace.fullpath}/"

        val connections = HashMap<String, MutableList<String>>(files.size + 16)

        // Track keys seen this pass so we can prune the cache of deleted files.
        val liveKeys = HashSet<FileCacheKey>()

        coroutineScope {
            // Each async returns (cacheKey, fileGraphId, extracted links).
            // The cache is written ONLY in the sequential await loop below,
            // so the HashMap is never touched concurrently.
            val deferreds =
                ArrayList<Deferred<Triple<FileCacheKey, String, List<String>>?>>()

            for (directory in files) {
                if (!directory.isDirectory()) continue
                val children = directory.getChildrenOrNull() ?: continue
                val dirGraphId = directory.getGraphId()

                for (file in children) {
                    val fileGraphId = file.getGraphId()
                    // Directory -> file edge needs no I/O.
                    connections.addEdge(dirGraphId, fileGraphId)

                    if (file is FileTreeNode.File &&
                        file.name.extension.toFileType() == FileTypeExt.Text
                    ) {
                        val cacheKey = FileCacheKey(file.getFullPath(), file.modifiedTime)
                        liveKeys.add(cacheKey)

                        val cached = linkCache[cacheKey]
                        if (cached != null) {
                            // Cache hit — no disk read, no parsing.
                            if (cached.isNotEmpty()) {
                                connections.addEdges(fileGraphId, cached)
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
                                    for (link in links) {
                                        ssLinks.add(workspacePathPrefix + link)
                                    }
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
                // Single-threaded: safe to write the plain HashMap here.
                // Store even empty results to avoid re-reading link-free files.
                linkCache[triple.first] = triple.third
                if (triple.third.isNotEmpty()) {
                    connections.addEdges(triple.second, triple.third)
                }
            }
        }

        // Prune cache entries for files no longer present (or changed mtime),
        // keeping memory bounded to the current file set.
        if (linkCache.size > liveKeys.size) {
            linkCache.keys.retainAll(liveKeys)
        }

        @Suppress("UNCHECKED_CAST")
        connections as Map<String, List<String>>
    }.scoping()

    /**
     * OPTIMIZED: cheap in-memory edges (annotations, rows, tag links, tag-to-tag).
     * No disk I/O — re-runs in microseconds when a tag toggle flips, instead
     * of dragging every file read along with it.
     */
    private val metadataEdgesFlow: Flow<Map<String, List<String>>> =
        com.moly3.cedarjam.core.domain.func.combine(
            collectionRowsFlow,
            tagToTagsFlow,
            tagLinksFlow,
            tagRowsFlow,
            annotationsFlow,
        ) { rows, tagToTags, tagLinks, tagRows, annotations ->
            val connections = HashMap<String, MutableList<String>>(
                rows.size + tagToTags.size + tagLinks.size + annotations.size + 16
            )

            // 1) Annotations
            for (item in annotations) {
                val annId = item.getGraphId()
                val keys = if (item.rowId != null) ArrayList<String>(2) else ArrayList<String>(1)
                keys.add(item.dataPath.getFileTreeNodeGraphId())
                if (item.rowId != null) {
                    keys.add(item.rowId.getCollectionRowGraphId())
                }
                connections.addEdges(annId, keys)
            }

            // 2) Collection rows
            for (item in rows) {
                val rowId = item.getGraphId()
                connections.addEdge(rowId, item.collectionId.getCollectionGraphId())
                val rel = item.fileRelativePath
                if (!rel.isNullOrEmpty()) {
                    connections.addEdge(rowId, rel.getFileTreeNodeGraphId())
                }
            }

            // 3) Tag-row links
            for (item in tagRows) {
                connections.addEdge(
                    item.tagId.getTagGraphId(),
                    item.rowId.getCollectionRowGraphId()
                )
            }

            // 4) Tag links (file nodes)
            for (tagLink in tagLinks) {
                val tagGraphId = tagLink.tagId.getTagGraphId()
                when (val data = tagLink.data) {
                    is TagLinkDtoData.FileNode -> {
                        connections.addEdge(tagGraphId, data.relativePath.getFileTreeNodeGraphId())
                    }
                }
            }

            // 5) Tag-to-tag
            for (item in tagToTags) {
                connections.addEdge(
                    item.secondTagId.getTagGraphId(),
                    item.firstTagId.getTagGraphId()
                )
            }

            @Suppress("UNCHECKED_CAST")
            connections as Map<String, List<String>>
        }.scoping()

    /**
     * Merges the (expensive, I/O-bound) file edges with the (cheap) metadata
     * edges, then makes the graph bidirectional. A tag toggle only re-runs
     * this merge + makeBidirectional, reusing the shared file-edges emission.
     */
    private val oldConnectionsFlow: Flow<Map<String, List<String>>> = combine(
        fileLinkEdgesFlow,
        metadataEdgesFlow,
    ) { fileEdges, metaEdges ->
        val merged = HashMap<String, MutableList<String>>(fileEdges.size + metaEdges.size)
        for ((k, v) in fileEdges) {
            merged.getOrPut(k) { ArrayList(v.size) }.addAll(v)
        }
        for ((k, v) in metaEdges) {
            merged.getOrPut(k) { ArrayList(v.size + 2) }.addAll(v)
        }
        @Suppress("UNCHECKED_CAST")
        makeBidirectional(merged as Map<String, List<String>>)
    }.scoping()

    /**
     * OPTIMIZED filterConnections — single-pass reverse graph, ArrayDeque BFS,
     * single-pass final filter.
     */
    fun filterConnections(
        connections: Map<String, List<String>>,
        targetId: String
    ): Map<String, List<String>> {
        val reverse = HashMap<String, MutableList<String>>(connections.size)
        for ((from, tos) in connections) {
            for (to in tos) {
                reverse.getOrPut(to) { ArrayList(2) }.add(from)
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
                for (n in nexts) forwardStack.addLast(n)
            }
        }

        val result = HashMap<String, List<String>>(visibleNodes.size)
        for ((k, v) in connections) {
            if (k !in visibleNodes) continue
            val filtered = ArrayList<String>(v.size)
            for (item in v) {
                if (item in visibleNodes) filtered.add(item)
            }
            result[k] = filtered
        }
        return result
    }

    val connectionsFlow = combine(
        oldConnectionsFlow,
        _targetId
    ) { connections, targetId ->
        if (targetId != null) filterConnections(connections, targetId) else connections
    }.scoping()

    private val graphNodes2Flow: Flow<Pair<Set<Any>, List<ObsidianGraphNode>>> = combine(
        _settingsStateFlow.map { it.isGradations }.distinctUntilChanged(),
        graphNodesFlow,
        appEnvironment.getAppSettingsFlow().map { it.theme.primaryColor }.distinctUntilChanged(),
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
        } else triple.second
        Pair(triple.first, nodes)
    }.scoping()

    /**
     * OPTIMIZED final nodes flow — graphNodes.first is already a Set built in
     * graphNodesFlow, so the O(1) `contains` lookup needs no per-emission
     * List->Set conversion.
     */
    val nodes = combine(
        graphNodes2Flow,
        connectionsFlow,
        _settingsStateFlow.map { Pair(it.isOrphans, it.maxNodes) }.distinctUntilChanged()
    ) { graphNodes, connections, isOrphans ->
        val showOrphans = isOrphans.first
        val maxNodes = isOrphans.second

        val validIds: Set<Any> = graphNodes.first

        val nodes = ArrayList<ObsidianGraphNode>(graphNodes.second.size.coerceAtMost(maxNodes))
        for (node in graphNodes.second) {
            if (nodes.size >= maxNodes) break
            if (showOrphans) {
                nodes.add(node)
            } else {
                val neighbors = connections[node.id]
                if (neighbors != null) {
                    var hasValidNeighbor = false
                    for (n in neighbors) {
                        if (n in validIds) {
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

    private companion object {
        // OPTIMIZED: hoisted out of graphNodesFlow so they aren't recomputed
        // on every emission.
        private val GREEN_COLOR = Color.Green.value
        private val RED_COLOR = Color.Red.value
        private val BLUE_COLOR = Color.Blue.value
        private val CYAN_COLOR = Color.Cyan.value
    }
}