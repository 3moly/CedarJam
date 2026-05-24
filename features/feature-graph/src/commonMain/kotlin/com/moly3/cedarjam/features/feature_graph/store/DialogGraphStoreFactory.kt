package com.moly3.cedarjam.features.feature_graph.store

import androidx.compose.ui.geometry.Offset
import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.statekeeper.StateKeeper
import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.moly3.cedarjam.core.domain.dialog.DialogDeleteService
import com.moly3.cedarjam.core.domain.dialog.DialogSelectTagService
import com.moly3.cedarjam.core.domain.func.doNothing
import com.moly3.cedarjam.core.domain.func.nowInMs
import com.moly3.cedarjam.features.feature_graph.Intent
import com.moly3.cedarjam.features.feature_graph.State
import com.moly3.cedarjam.features.feature_graph.State.Companion.fromSaveable
import com.moly3.cedarjam.features.feature_graph.State.Companion.toSaveable
import com.moly3.cedarjam.features.feature_graph.store.DialogGraphStore.*
import com.moly3.cedarjam.features.feature_graph.store.DialogGraphStore.Msg.*
import com.moly3.cedarjam.navigation.BaseExecutor
import com.moly3.cedarjam.navigation.consumeOrDefault
import com.moly3.cedarjam.core.domain.io
import com.moly3.cedarjam.core.domain.model.UIState
import com.moly3.cedarjam.core.domain.model.bind
import com.moly3.cedarjam.core.domain.model.getCollectionRowGraphId
import com.moly3.cedarjam.core.domain.model.getFileTreeNodeGraphId
import com.moly3.cedarjam.core.domain.model.getGraphId
import com.moly3.cedarjam.core.domain.model.getTagGraphId
import com.moly3.cedarjam.core.domain.model.placeNodesCircular
import com.moly3.cedarjam.core.domain.model.request.CreateTagCollectionRowRequest
import com.moly3.cedarjam.core.domain.model.request.CreateTagLinkRequest
import com.moly3.cedarjam.core.domain.model.request.CreateTagToTagRequest
import com.moly3.cedarjam.core.domain.model.resultBlock
import com.moly3.cedarjam.core.domain.service.WorkspaceSession
import com.moly3.cedarjam.core.domain.usecase.IOpenNodeDataUseCase
import com.moly3.cedarjam.features.feature_graph.model.GraphDialogInput
import com.moly3.cedarjam.navigation.Navigator
import com.moly3.cedarjam.navigation.mapper.toRoute
import com.moly3.dataviz.core.graph.model.Connection
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.collections.immutable.toPersistentMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

internal class DialogGraphStoreFactory(
    private val navigator: Navigator,
    private val deleteService: DialogDeleteService,
    private val selectTagService: DialogSelectTagService,
    private val openNodeDataUseCase: IOpenNodeDataUseCase,
    private val workspaceSession: WorkspaceSession,
    private val storeFactory: StoreFactory,
    private val lifecycle: Lifecycle,
    private val targetInput: GraphDialogInput,
    private val openWorkspaceSettings: (Boolean) -> Unit,
    private val openPdfPage: (Int) -> Unit
) {

//    private val openNodeDataUseCase: IOpenNodeDataUseCase get() =
//        d.openNodeDataUseCaseFactory(workspaceSession.fileManagerService)
//    private val selectTagService: DialogSelectTagService get() = d.dialogSelectTagService

    fun create(stateKeeper: StateKeeper): DialogGraphStore = object : DialogGraphStore,
        Store<Intent, State, Unit> by storeFactory.create(
            name = DialogGraphStore::class.simpleName,
            initialState = stateKeeper.consumeOrDefault(
                "DialogGraphStore",
                State.SaveableState.serializer(),
                default = State().toSaveable()
            ).fromSaveable(),
            bootstrapper = SimpleBootstrapper(Unit),
            executorFactory = ::ExecutorImpl,
            reducer = ReducerImpl
        ) {
    }.also {
        stateKeeper.register(
            key = "DialogGraphStore",
            strategy = State.SaveableState.serializer()
        ) {
            it.state.toSaveable()
        }
    }

    private inner class ExecutorImpl :
        BaseExecutor<Intent, Unit, State, Msg, Unit>(lifecycle) {

        private val _isShowNestedConnectionsStateFlow = MutableStateFlow(false)


        private val targetIdFlow = workspaceSession.fileManagerService.fileNodeState.map {
            when (targetInput) {
                is GraphDialogInput.File -> {
                    val foundFile = it.states[targetInput.timestamp]
                    if (foundFile != null) {
                        foundFile.fileNodeRelativePath.getFileTreeNodeGraphId()
                    } else {
                        null
                    }
                }

                is GraphDialogInput.Row -> targetInput.id.getCollectionRowGraphId()
                is GraphDialogInput.Tag -> targetInput.id.getTagGraphId()
            }
        }


        private val connectionsFlow = combine(
            workspaceSession.graphEco.connectionsFlow,
            targetIdFlow
        ) { connections, targetId ->
            connections[targetId]?.associateBy { it.target } ?: emptyMap()
        }

        private val connectedTags = combine(
            connectionsFlow,
            workspaceSession.tagsFlow
        ) { connections, tags ->
            val filteredTags = tags.filter {
                connections[it.getGraphId()] != null
            }
            UIState.Success(filteredTags.toPersistentList())
        }

        private val connectedFiles = combine(
            connectionsFlow,
            workspaceSession.allFiles
        ) { connections, filesState ->
            filesState.map(onError = {}) {
                it.filter {
                    val graphId = it.getGraphId()
                    connections[graphId] != null
                }.toPersistentList()
            }
        }

        private val connectedRows = combine(
            connectionsFlow,
            workspaceSession.collectionRowsFlow
        ) { connections, rows ->
            val filtered = rows.filter {
                connections[it.getGraphId()] != null
            }
            UIState.Success(filtered.toPersistentList())
        }

        private val connectedAnnotations = combine(
            connectionsFlow,
            workspaceSession.annotationsFlow
        ) { connections, annotations ->
            val filtered = annotations.filter {
                connections[it.getGraphId()] != null
            }
            UIState.Success(filtered.toPersistentList())
        }


        fun findAllConnected(
            startId: String,
            connections: Map<String, List<Connection<String>>>,
        ): Set<String> {
            val visited = mutableSetOf<String>()
            val queue = ArrayDeque<String>()

            queue.add(startId)
            visited.add(startId)

            while (queue.isNotEmpty()) {
                val current = queue.removeFirst()
                val neighbours = connections[current].orEmpty()

                for (neighbour in neighbours) {
                    if (visited.add(neighbour.target)) {
                        queue.add(neighbour.target)
                    }
                }
            }

            return visited
        }

        @OptIn(ExperimentalCoroutinesApi::class)
        override fun onStart(scopeFromStartToStop: CoroutineScope) {
            super.onStart(scopeFromStartToStop)

            _isShowNestedConnectionsStateFlow.value = state().isShowNestedConnections
            scopeFromStartToStop.launch {
                targetIdFlow.collectLatest {
                    dispatch(Msg.SetGraphTargetId(it))
                }
            }
            scopeFromStartToStop.launch {
                connectedTags
                    .flowOn(io)
                    .collectLatest {
                        dispatch(Msg.SetTagsState(it))
                    }
            }
            scopeFromStartToStop.launch {
                connectedRows
                    .flowOn(io)
                    .collectLatest {
                        dispatch(Msg.SetRowsState(it))
                    }
            }
            scopeFromStartToStop.launch {
                connectedFiles
                    .flowOn(io)
                    .collectLatest {
                        dispatch(Msg.SetFilesState(it))
                    }
            }
            scopeFromStartToStop.launch {
                connectedAnnotations
                    .flowOn(io)
                    .collectLatest {
                        dispatch(Msg.SetAnnotationsState(it))
                    }
            }

            scopeFromStartToStop.launch {
                workspaceSession.graphEco.connectionsFlow
                    .map { m ->
                        m.mapValues { (_, v) -> v.toImmutableList() }
                            .toPersistentMap()
                    }
                    .flowOn(io)
                    .collectLatest {
                        dispatch(Msg.SetConnections(it))
                    }
            }



            scopeFromStartToStop.launch {
                combine(
                    workspaceSession.graphEco.nodesFlow,
                    workspaceSession.graphEco.connectionsFlow,
                    targetIdFlow,
                    _isShowNestedConnectionsStateFlow
                ) { nodes, connections, targetId, isShowNested ->
                    val filteredNodes = if (targetId != null) {
                        if (isShowNested) {
                            val connectedIds = findAllConnected(targetId, connections)
                            nodes.filter { it.id in connectedIds }
                        } else {
                            // Project the master's outgoing Connections to a Set of
                            // target node ids; we only care about membership here.
                            val masterTargets: Set<String> =
                                connections[targetId]?.mapTo(HashSet()) { it.target } ?: emptySet()
                            nodes.filter { it.id == targetId || it.id in masterTargets }
                        }
                    } else nodes

                    var coordinates: Map<String, Offset>? = null
                    if (state().coordinates.isEmpty()) {
                        val mutMap = mutableMapOf<String, Offset>()
                        placeNodesCircular(
                            stateNodes = filteredNodes,
                            changeCoords = mutMap
                        )
                        coordinates = mutMap
                    }
                    Pair(filteredNodes, coordinates)
                }.collectLatest {
                    val coordinates = it.second
                    if (coordinates != null) {
                        dispatch(Msg.SetCoordinates(coordinates.toPersistentMap()))
                    }
                    dispatch(Msg.SetNodes(it.first.toPersistentList()))
                }
            }
        }

        override fun executeIntent(intent: Intent) {
            when (intent) {
                is Intent.OpenWorkspaceSettings -> {
                    openWorkspaceSettings(true)
                }

                is Intent.SetCurrentTabPage -> {
                    dispatch(SetCurrentPage(intent.page))
                }

                is Intent.SetCoordinates -> {
                    dispatch(SetCoordinates(intent.value.toPersistentMap()))
                }

                is Intent.RemoveAnnotation -> {
                    val workspaceEnv = workspaceSession.workspaceEnvStateFlow.value
                    scope.launch {
                        val result = deleteService.open(Unit)
                        if (result) {
                            workspaceEnv.deleteAnnotation(id = intent.id)
                        }
                    }
                }

                is Intent.OpenPdfPage -> {
                    dispatch(SetIsShowContent(false))
                    openPdfPage(intent.page)
                }

                is Intent.AnnotationsScrollState -> {
                    dispatch(Msg.SetAnnotationsScrollState(intent.value))
                }

                is Intent.AddTag -> {
                    scope.launch {
                        val tag = selectTagService.open(workspaceSession)
                        val workspaceEnv = workspaceSession.workspaceEnvStateFlow.value
                        val fileManagerService = workspaceSession.fileManagerService
                        if (tag != null) {
                            when (targetInput) {
                                is GraphDialogInput.File -> {
                                    val fileRelativePath =
                                        fileManagerService.getFileNodeByTimestamp(timestamp = targetInput.timestamp)
                                    if (fileRelativePath != null) {
                                        workspaceEnv.createTagLink(
                                            CreateTagLinkRequest(
                                                relativePath = fileRelativePath,
                                                tagId = tag.id
                                            )
                                        )
                                    } else {
                                        doNothing()
                                    }
                                }

                                is GraphDialogInput.Row -> {
                                    workspaceEnv.createTagCollectionRow(
                                        CreateTagCollectionRowRequest(
                                            tagId = tag.id,
                                            rowId = targetInput.id,
                                            createdTime = nowInMs()
                                        )
                                    )
                                }

                                is GraphDialogInput.Tag -> {
                                    if (tag.id != targetInput.id) {
                                        workspaceEnv.createTagToTag(
                                            CreateTagToTagRequest(
                                                tagId = targetInput.id,
                                                tag2Id = tag.id,
                                                createdTime = nowInMs()
                                            )
                                        )
                                    } else {
                                        doNothing()
                                    }
                                }
                            }
                        }
                    }
                }

                is Intent.SetVelocities -> {
                    dispatch(SetVelocities(intent.value.toPersistentMap()))
                }

                is Intent.SetZoom -> {
                    dispatch(SetZoom(intent.value))
                }

                Intent.Close -> dispatch(SetIsShowContent(false))
                is Intent.SetIsShowContent -> {
                    dispatch(SetIsShowContent(intent.value))
                }

                is Intent.SetIsShowNestedConnections -> {
                    scope.launch {
                        _isShowNestedConnectionsStateFlow.emit(intent.value)
                        dispatch(SetIsShowNestedConnections(intent.value))
                    }
                }

                is Intent.OpenNode -> {
                    scope.launch {
                        resultBlock {
                            val result = openNodeDataUseCase.invoke(intent.value, true)
                            navigator.navigate(bind(result).toRoute())
                        }
                    }
                }

                is Intent.OpenTimeMachine -> {

                }
            }
        }
    }

    private object ReducerImpl : Reducer<State, Msg> {
        override fun State.reduce(msg: Msg): State {
            return when (msg) {
                is SetConnections -> copy(connections = msg.value)
                is SetCoordinates -> copy(coordinates = msg.value)
                is SetNodes -> copy(graphNodes = msg.value)
                is SetVelocities -> copy(velocities = msg.value)
                is SetZoom -> copy(zoom = msg.value)
                is SetIsShowContent -> copy(isShowContent = msg.value)
                is SetCurrentPage -> copy(currentPage = msg.value)
                is SetGraphTargetId -> copy(graphTargetId = msg.value)
                is SetIsShowNestedConnections -> copy(isShowNestedConnections = msg.value)
                is SetTagsState -> copy(tagsState = msg.value)
                is SetAnnotationsState -> copy(annotationsState = msg.value)
                is SetFilesState -> copy(filesState = msg.value)
                is SetRowsState -> copy(rowsState = msg.value)
                is SetAnnotationsScrollState -> copy(annotationsScrollState = msg.value)
            }
        }
    }
}