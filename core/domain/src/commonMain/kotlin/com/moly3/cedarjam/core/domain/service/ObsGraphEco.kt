package com.moly3.cedarjam.core.domain.service

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import com.moly3.cedarjam.core.domain.func.extractLinks
import com.moly3.cedarjam.core.domain.func.pathWrapper
import com.moly3.cedarjam.core.domain.io
import com.moly3.cedarjam.core.domain.model.CollectionDTO
import com.moly3.cedarjam.core.domain.model.CollectionRowDTO
import com.moly3.cedarjam.core.domain.model.FileTreeNode
import com.moly3.cedarjam.core.domain.model.FileTreeNode.Companion.getAll
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
import com.moly3.cedarjam.core.domain.repository.IAppEnvironment
import com.moly3.dataviz.core.graph.model.GraphNode
import kotlinx.coroutines.CoroutineScope
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
import kotlin.collections.iterator
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class ObsGraphEco(
    private val scope: CoroutineScope,
    appEnvironment: IAppEnvironment,
    workspaceSession: WorkspaceSession,
    startTargetId: String? = null,
    config: GraphSettingsConfig = GraphSettingsConfig.Companion.Default
) {

    private val _settingsStateFlow = MutableStateFlow(config)
    private val _targetId = MutableStateFlow(startTargetId)
    val graphState: StateFlow<GraphSettingsConfig> = _settingsStateFlow

    fun setGraphConfig(config: GraphSettingsConfig) {
        scope.launch(io) {
            _settingsStateFlow.emit(config)
        }
    }

    private fun makeBidirectional(connections: Map<String, List<String>>): Map<String, List<String>> {
        val mutableGraph = connections.mapValues { it.value.toMutableList() }.toMutableMap()

        for ((node, connections) in connections) {
            for (target in connections) {
                mutableGraph.getOrPut(target) { mutableListOf() }.add(node)
            }
        }

        return mutableGraph
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

    private fun <T> Flow<T>.scoping(): Flow<T> {
        return this
            .flowOn(io)
            .shareIn(
                scope = scope,
                started = SharingStarted.Lazily,
                replay = 1
            )
    }

    private val tagLinksFlow: Flow<List<TagLinkDTO>> =
        combine(
            isShowTagsFlow,
            workspaceSession.tagLinksFlow
        ) { tagsConfig, tagLinks ->
            val all = mutableListOf<TagLinkDTO>()
            if (tagsConfig) {
                for (tag in tagLinks) {
                    all.add(tag)
                }
            }
            all
        }.scoping()

    private val tagToTagsFlow: Flow<List<TagToTagDTO>> =
        combine(
            isShowTagsFlow,
            workspaceSession.tagToTagsFlow
        ) { tagsConfig, tagToTags ->
            val all = mutableListOf<TagToTagDTO>()
            if (tagsConfig) {
                for (tag in tagToTags) {
                    all.add(tag)
                }
            }
            all
        }.scoping()

    private val tagRowsFlow: Flow<List<TagCollectionRowDTO>> =
        combine(
            isShowTagsFlow,
            workspaceSession.tagCollectionRowsFlow
        ) { tagsConfig, tagToTags ->
            val all = mutableListOf<TagCollectionRowDTO>()
            if (tagsConfig) {
                for (tag in tagToTags) {
                    all.add(tag)
                }
            }
            all
        }.scoping()

    private val tagsFlow: Flow<List<TagDTO>> =
        combine(
            isShowTagsFlow,
            workspaceSession.tagsFlow
        ) { tagsConfig, tags ->

            val all = mutableListOf<TagDTO>()
            if (tagsConfig) {
                for (tag in tags) {
                    all.add(tag)
                }
            }
            all
        }.scoping()

    private val collectionsFlow: Flow<List<CollectionDTO>> =
        combine(
            isShowCollectionsFlow,
            workspaceSession.collectionsFlow
        ) { collectionsConfig, items ->

            val all = mutableListOf<CollectionDTO>()
            if (collectionsConfig) {
                for (item in items) {
                    all.add(item)
                }
            }
            all
        }.scoping()


    private val collectionRowsFlow: Flow<List<CollectionRowDTO>> =
        combine(
            isShowRowsFlow,
            workspaceSession.collectionRowsFlow
        ) { rowsConfig, items ->

            val all = mutableListOf<CollectionRowDTO>()
            if (rowsConfig) {
                for (item in items) {
                    all.add(item)
                }
            }
            all
        }.scoping()

    private val filesFlow: Flow<List<FileTreeNode>> =
        combine(
            _settingsStateFlow
                .map {
                    Pair(it.isShowDirectories, it.isRealFiles)
                }
                .distinctUntilChanged(),
            workspaceSession.filesFlow.map {
                when (it) {
                    is UIState.Error -> listOf()
                    is UIState.Loading -> listOf()
                    is UIState.Success -> it.data.getAll()
                }
            }
        ) { filesConfig, files ->
            val allFiles = mutableListOf<FileTreeNode>()

            for (item in files) {
                when (item) {
                    is FileTreeNode.Directory -> {
                        if (filesConfig.first) {
//                            if (filesConfig.second && item.isVirtual) {
//                                continue
//                            }
                            allFiles.add(item)
                        }
                    }

                    is FileTreeNode.File -> {
                        allFiles.add(item)
                    }
                }
            }

            allFiles
        }.scoping()

    private val graphNodesFlow = combine(
        collectionsFlow,
        collectionRowsFlow,
        tagsFlow,
        filesFlow
    ) { collections, rows, tags, files ->
        val graphNodes = mutableListOf<ObsidianGraphNode>()
        val times = mutableMapOf<Any, Long>()

        for (item in collections) {
            val graphId = item.getGraphId()
            graphNodes.add(
                GraphNode(
                    id = graphId,
                    name = item.name,
                    colorValue = Color.Companion.Green.value,
                    data = ObsidianGraphData.Collection(
                        id = item.id
                    )
                )
            )
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
            times[graphId] = item.modifiedTime
        }
        for (item in files) {
            val graphId = item.getGraphId()
            graphNodes.add(
                GraphNode(
                    id = graphId,
                    name = item.getShortName(),
                    colorValue = (if (item is FileTreeNode.File)
                        Color.Companion.Blue
                    else
                        Color.Companion.Cyan)?.value,
                    data = ObsidianGraphData.File(item.getFullPath())
                )
            )
            times[graphId] = item.modifiedTime
        }

        Triple(graphNodes.map { b -> b.id }, graphNodes, times)
    }.scoping()


    @OptIn(ExperimentalTime::class)
    private fun makeGradation(
        modifiedTimes: Map<Any, Long>,
        filtered: List<ObsidianGraphNode>,
        primaryColor: Color,
        defaultColor: Color
    ): List<ObsidianGraphNode> {
        fun getModifiedTime(id: Any): Long {
            return modifiedTimes[id] ?: 0L
        }

        val maxLastTime = Clock.System.now().toEpochMilliseconds()
        val minLastTime = filtered
            .filter { d -> getModifiedTime(d.id) > 0 }
            .minOfOrNull { d -> getModifiedTime(d.id) } ?: 0L

        val range = (maxLastTime - minLastTime).takeIf { it > 0 } ?: 1L

        val defaultColor = defaultColor

        return filtered.map { item ->
            val fraction = ((getModifiedTime(item.id) - minLastTime).toFloat() / range)
                .coerceIn(0f, 1f)

            val blendedColor = lerp(defaultColor, primaryColor, fraction)

            val colorWithAlpha = blendedColor
            item.copy(colorValue = colorWithAlpha.value)
        }
    }


    private val oldConnectionsFlow = com.moly3.cedarjam.core.domain.func.combine(
        collectionRowsFlow,
        filesFlow,
        tagToTagsFlow,
        tagLinksFlow,
        tagRowsFlow,
        workspaceSession.workspaceFlow
    ) { rows, files, tagToTags, tagLinks, tagRows, workspace ->
        val workspaceSession = workspaceSession.workspaceEnvStateFlow.value
        val connections = mutableMapOf<String, List<String>>()
        fun setConnects(id: String, new: List<String>) {
            val values = connections[id]?.toMutableList()
            if (values != null) {
                values.addAll(new)
                connections[id] = values
            } else {
                connections[id] = new
            }
        }

        for (directory in files.filter { d -> d.isDirectory() }) {
            for (file in directory.getChildrenOrNull() ?: listOf()) {
                if (file is FileTreeNode.File) {
                    val fileNodeId = file.getGraphId()
                    resultBlock {
                        val textResult = workspaceSession.getNodeText(file)
                        val text = bind(textResult)
                        val links = text.extractLinks()
                        val ssLinks = mutableListOf<String>()

                        if (links.isNotEmpty()) {
                            for (link in links) {
                                val fileNodeLink = "filenode: ${workspace.fullpath}/${link}"
                                ssLinks.add(fileNodeLink)
                            }
                            setConnects(
                                fileNodeId,
                                ssLinks
                            )
                        }
                    }
                }
                setConnects(
                    directory.getGraphId(),
                    listOf(file.getGraphId())
                )
            }
        }
        for (item in rows) {
            setConnects(
                item.getGraphId(),
                listOf(item.collectionId.getCollectionGraphId())
            )
            if (!item.fileRelativePath.isNullOrEmpty()) {
                val fullPath = pathWrapper(
                    workspace.fullpath,
                    item.fileRelativePath

                ).toString()
                setConnects(
                    item.getGraphId(),
                    listOf(fullPath.getFileTreeNodeGraphId())
                )
            }
        }
        for (item in tagRows) {
            setConnects(
                item.tagId.getTagGraphId(),
                listOf(item.rowId.getCollectionRowGraphId())
            )
        }
        for (tagLink in tagLinks) {
            val tagGraphId = tagLink.tagId.getTagGraphId()

            when (val data = tagLink.data) {
                is TagLinkDtoData.FileNode -> {
                    val fullPath = pathWrapper(
                        workspace.fullpath,
                        data.relativePath
                    ).toString()

                    setConnects(
                        tagGraphId,
                        listOf(fullPath.getFileTreeNodeGraphId())
                    )
                }
            }
        }
        for (item in tagToTags) {
            setConnects(
                item.secondTagId.getTagGraphId(),
                listOf(item.firstTagId.getTagGraphId())
            )
        }

        makeBidirectional(connections)
    }.scoping()

    fun filterConnections(
        connections: Map<String, List<String>>,
        targetId: String
    ): Map<String, List<String>> {
        // Step 1: Build reverse graph
        val reverse = mutableMapOf<String, MutableList<String>>()
        for ((from, tos) in connections) {
            for (to in tos) {
                reverse.getOrPut(to) { mutableListOf() }.add(from)
            }
        }

        // Step 2: Find all nodes that can reach targetId
        val sources = mutableSetOf<String>()
        val stack = ArrayDeque<String>()
        stack.add(targetId)
        while (stack.isNotEmpty()) {
            val node = stack.removeFirst()
            for (prev in reverse[node].orEmpty()) {
                if (sources.add(prev)) {
                    stack.add(prev)
                }
            }
        }

        // Step 3: From those sources (and targetId), find all connected forward
        val visibleNodes = mutableSetOf<String>()
        val forwardStack = ArrayDeque<String>()
        forwardStack.addAll(sources + targetId)
        while (forwardStack.isNotEmpty()) {
            val node = forwardStack.removeFirst()
            if (visibleNodes.add(node)) {
                forwardStack.addAll(connections[node].orEmpty())
            }
        }

        // Step 4: Build filtered map
        return connections
            .filterKeys { it in visibleNodes }
            .mapValues { (_, list) -> list.filter { it in visibleNodes } }
    }

    val connectionsFlow = combine(
        oldConnectionsFlow,
        _targetId
    ) { connections, targetId ->
        if (targetId != null)
            filterConnections(connections, targetId)
        else {
            connections
        }
    }.scoping()


    private val graphNodes2Flow: Flow<Pair<List<Any>, List<ObsidianGraphNode>>> = combine(
        _settingsStateFlow
            .map {
                it.isGradations
            }
            .distinctUntilChanged(),
        graphNodesFlow,
        appEnvironment.getAppSettingsFlow()
            .map {
                it.theme.primaryColor
            }.distinctUntilChanged(),
        appEnvironment.getAppSettingsFlow()
            .map {
                it.theme.colors.primaryFont
            }.distinctUntilChanged()
    ) { gradations, triple, primaryColor, defaultColor ->

        val nodes = if (gradations) {
            makeGradation(
                triple.third,
                triple.second,
                primaryColor = primaryColor,
                defaultColor = defaultColor
            )
        } else
            triple.second

        Pair(triple.first, nodes)
    }.scoping()

    val nodes = combine(
        graphNodes2Flow,
        connectionsFlow,
        _settingsStateFlow
            .map {
                Pair(it.isOrphans, it.maxNodes)
            }
    ) { graphNodes, connections, isOrphans ->
        val nodes = mutableListOf<ObsidianGraphNode>()
        for (node in graphNodes.second) {
            val connections = (connections[node.id] ?: listOf())
                .filter { d -> graphNodes.first.contains(d) }
            if (!isOrphans.first && connections.isEmpty()) {
                continue
            }
            nodes.add(node)
        }
        nodes.take(isOrphans.second)
    }.scoping()
}