package com.moly3.cedarjam.pages.page_graph.store

import androidx.compose.ui.geometry.Offset
import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.statekeeper.StateKeeper
import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.moly3.cedarjam.core.domain.dialog.DialogColorPickerService
import com.moly3.cedarjam.core.domain.dialog.DialogDeleteService
import com.moly3.cedarjam.core.domain.dialog.DialogGraphConfigsService
import com.moly3.cedarjam.core.domain.func.hiddenDirectory
import com.moly3.cedarjam.core.domain.func.isGraphSettingsNudge
import com.moly3.cedarjam.core.domain.func.pathWrapper
import com.moly3.cedarjam.core.domain.io
import com.moly3.cedarjam.core.domain.model.FileName
import com.moly3.cedarjam.core.domain.model.FileTreeNode
import com.moly3.cedarjam.core.domain.model.bind
import com.moly3.cedarjam.core.domain.model.config.GraphSaveConfig
import com.moly3.cedarjam.core.domain.model.config.GroupLogic
import com.moly3.cedarjam.core.domain.model.node.ObsidianGraphData
import com.moly3.cedarjam.core.domain.model.resultBlock
import com.moly3.cedarjam.core.domain.model.toKmpImmutableMap
import com.moly3.cedarjam.core.domain.repository.IAppEnvironment
import com.moly3.cedarjam.core.domain.repository.setNodeJson
import com.moly3.cedarjam.core.domain.service.ObsGraphEco
import com.moly3.cedarjam.core.domain.service.WorkspaceSession
import com.moly3.cedarjam.core.domain.usecase.IOpenNodeDataUseCase
import com.moly3.cedarjam.core.domain.usecase.OpenNodeDataUseCaseFactory
import com.moly3.cedarjam.core.ui.service.MacTrackpadGestureService
import com.moly3.cedarjam.navigation.BaseExecutor
import com.moly3.cedarjam.navigation.Navigator
import com.moly3.cedarjam.navigation.consumeOrDefault
import com.moly3.cedarjam.navigation.mapper.toRoute
import com.moly3.cedarjam.pages.page_graph.Intent
import com.moly3.cedarjam.pages.page_graph.State
import com.moly3.cedarjam.pages.page_graph.State.Companion.fromSaveable
import com.moly3.cedarjam.pages.page_graph.State.Companion.toSaveable
import com.moly3.cedarjam.pages.page_graph.placeNodesCircular
import com.moly3.cedarjam.pages.page_graph.store.GraphStore.Msg.*
import com.moly3.cedarjam.pages.page_graph.store.GraphStore.Msg.SetConnections
import com.moly3.cedarjam.pages.page_graph.store.GraphStore.Msg.SetCoordinates
import com.moly3.cedarjam.pages.page_graph.store.GraphStore.Msg.SetGraphUserPosition
import com.moly3.cedarjam.pages.page_graph.store.GraphStore.Msg.SetIsShowSettings
import com.moly3.cedarjam.pages.page_graph.store.GraphStore.Msg.SetNodes
import com.moly3.cedarjam.pages.page_graph.store.GraphStore.Msg.SetZoom
import com.moly3.dataviz.core.graph.engine.IGraphEngine
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toPersistentMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.collections.map

class GraphStoreImpl @AssistedInject constructor(
    @Assisted storeFactory: StoreFactory,
    @Assisted private val lifecycle: Lifecycle,
    @Assisted private val workspaceSession: WorkspaceSession,
    @Assisted private val openWorkspaceSettings: (Boolean) -> Unit,
    @Assisted stateKeeper: StateKeeper,
    @Assisted override val engine: IGraphEngine<String, ObsidianGraphData>,
    private val navigator: Navigator,
    private val appEnvironment: IAppEnvironment,
    private val macTrackpadGestureService: MacTrackpadGestureService,
    private val openNodeDataUseCaseFactory: OpenNodeDataUseCaseFactory,
    private val dialogColorPickerService: DialogColorPickerService,
    private val dialogDeleteService: DialogDeleteService,
    private val dialogGraphConfigsService: DialogGraphConfigsService
) : GraphStore,
    Store<Intent, State, Unit> by storeFactory.create(
        name = GraphStore::class.simpleName,
        initialState = stateKeeper.consumeOrDefault(
            "GraphStore",
            State.SaveableState.serializer(),
            default = State.SaveableState()
        ).fromSaveable(),
        bootstrapper = SimpleBootstrapper(Unit),
        executorFactory = {
            ExecutorImpl(
                lifecycle = lifecycle,
                workspaceSession = workspaceSession,
                appEnvironment = appEnvironment,
                engine = engine,
                macTrackpadGestureService = macTrackpadGestureService,
                navigator = navigator,
                openNodeDataUseCase = openNodeDataUseCaseFactory.invoke(fileManagerService = workspaceSession.fileManagerService),
                openWorkspaceSettings = openWorkspaceSettings,
                dialogColorPickerService = dialogColorPickerService,
                dialogDeleteService = dialogDeleteService,
                dialogGraphConfigsService = dialogGraphConfigsService
            )
        },
        reducer = ReducerImpl
    ) {

    init {
        stateKeeper.register(key = "GraphStore", strategy = State.SaveableState.serializer()) {
            this.state.toSaveable()
        }
    }

    private class ExecutorImpl(
        lifecycle: Lifecycle,
        private val workspaceSession: WorkspaceSession,
        private val appEnvironment: IAppEnvironment,
        private val navigator: Navigator,
        private val macTrackpadGestureService: MacTrackpadGestureService,
        private val openNodeDataUseCase: IOpenNodeDataUseCase,
        private val engine: IGraphEngine<String, ObsidianGraphData>,
        private val openWorkspaceSettings: (Boolean) -> Unit,
        private val dialogColorPickerService: DialogColorPickerService,
        private val dialogDeleteService: DialogDeleteService,
        private val dialogGraphConfigsService: DialogGraphConfigsService
    ) : BaseExecutor<Intent, Unit, State, GraphStore.Msg, Unit>(lifecycle) {

        private val _isMouseCapturedState = MutableStateFlow(false)
        private val _pendingSaveConfig = MutableSharedFlow<GraphSaveConfig>()


        private val graphEco: ObsGraphEco by lazy {
            ObsGraphEco(
                scope = scope,
                workspaceSession = workspaceSession,
                config = state().partConfig.filter,
                appEnvironment = appEnvironment
            )
        }

        @OptIn(FlowPreview::class)
        override fun onStart(scopeFromStartToStop: CoroutineScope) {
            super.onStart(scopeFromStartToStop)

            graphEco.setGroups(state().partConfig.groups)

            // Collect the new node lands map
            scopeFromStartToStop.launch {
                graphEco.nodeLandsFlow
                    .distinctUntilChanged()
                    .collectLatest { landsMap ->
                        dispatch(GraphStore.Msg.SetNodeLands(landsMap))
                        // dispatch(SetNodeLands(landsMap.toPersistentMap())) // Add this Msg to your GraphStore.Msg
                    }
            }
            scopeFromStartToStop.launch {
                _pendingSaveConfig
                    .debounce(300L)
                    .collectLatest {
                        val env = workspaceSession.workspaceEnvStateFlow.value
                        val fileNode = FileTreeNode.File(
                            name = FileName("graph_configs", "json"),
                            workspaceFullPath = env.getWorkspace().absolutePath,
                            parentRelativePath = pathWrapper(hiddenDirectory).pathString
                        )
                        env.setNodeJson(
                            node = fileNode,
                            data = it
                        )
                    }
            }
            scopeFromStartToStop.launch {
                macTrackpadGestureService.valueStateFlow.collectLatest {
                    if (_isMouseCapturedState.value) {
                        val newZoom = state().zoom + it.toFloat()
                        dispatch(SetZoom(newZoom))
                    }
                }
            }
            scopeFromStartToStop.launch {
                // distinctUntilChanged: ObsGraphEco may re-emit an equal config
                // (it's a StateFlow). collectLatest does NOT dedupe — without
                // this guard each emission triggers a reducer copy() + a full
                // State emission to every subscriber.
                graphEco.graphState
//                    .distinctUntilChanged()
                    .collectLatest {
                        if (it != state().partConfig.filter) {
                            //dispatch(SetConfig(it))
                        }
                    }
            }
            scopeFromStartToStop.launch {
                graphEco.connectionsFlow
//                    .map {
//                        it
//                            .map { d -> d.key to d.value.toImmutableList() }
//                            .toMap()
//                            .toPersistentMap()
//                    }
                    .map { m ->
                        m.mapValues { (_, v) -> v.toImmutableList() }
                            .toPersistentMap()
                    }
                    .flowOn(io)
                    .distinctUntilChanged()
                    .collectLatest {
                        if (it != state().connections) {
                            dispatch(SetConnections(it))
                        }
                    }
            }
            scopeFromStartToStop.launch {
                graphEco.nodesFlow
                    .map {
                        it.toImmutableList()

                    }     // copy on IO
                    .distinctUntilChanged()           // equality on IO
                    .flowOn(io)
                    .collectLatest { immutableNodes ->
                        if (state().coordinates.isEmpty()) {
                            val mutMap = mutableMapOf<String, Offset>()
                            placeNodesCircular(stateNodes = immutableNodes, changeCoords = mutMap)
                            dispatch(SetCoordinates(mutMap.toPersistentMap()))
                        }
                        if (immutableNodes != state().graphNodes) {
                            dispatch(SetNodes(immutableNodes))
                        }
                    }
            }
        }

        private fun setGroups(groups: List<GroupLogic>) {
            val partConfig = state().partConfig
            val grroups = if (partConfig.config.groupSettings.enabled) {
                groups
            } else {
                listOf()
            }

            graphEco.setGroups(grroups)

            dispatch(
                SetPartConfig(
                    partConfig.copy(groups = grroups)
                )
            )
            scope.launch {
                delay(500L)
                engine.reheat()
            }
        }

        private fun changeGroup(groupName: String, change: (GroupLogic) -> GroupLogic) {
            val groups = state().partConfig.groups.toMutableList()

            setGroups(groups.map {
                if (it.name == groupName)
                    change(it)
                else
                    it
            })
        }


        override fun executeIntent(intent: Intent) {
            when (intent) {

                is Intent.OpenNodeData -> {
                    scope.launch(io) {

                        resultBlock {
                            val result = openNodeDataUseCase.invoke(intent.value, true)
                            navigator.navigate(bind(result).toRoute())
                        }
                    }
                }

                is Intent.SetZoom -> {
                    val oldZoom = state().zoom
                    val newZoom = if (intent.isGesture) {
                        intent.value * oldZoom
                    } else {
                        intent.value * oldZoom * 0.05f + oldZoom
                    }
                    dispatch(SetZoom(newZoom))
                }

                is Intent.SetGraphUserPosition -> {
                    val op = intent.value
                    if (op != state().graphUserPosition) {
                        dispatch(SetGraphUserPosition(op))
                    }
                }

                is Intent.SetIsShowSettings -> {
                    if (intent.value != state().isShowSettings) {
                        dispatch(SetIsShowSettings(intent.value))
                    }
                }

                is Intent.OpenWorkspaceSettings -> {
                    openWorkspaceSettings(true)
                }

                is Intent.SetCoordinates -> {
                    dispatch(SetCoordinates(intent.value.toPersistentMap()))
                }

                is Intent.SetIsMouseCaptured -> {
                    scope.launch {
                        if (_isMouseCapturedState.value != intent.value) {
                            _isMouseCapturedState.emit(intent.value)
                        }
                    }
                }

                is Intent.SetGroups -> {
                    setGroups(intent.value)
                }

                is Intent.DeleteGroup -> {
                    scope.launch {
                        val groupCount =
                            state().partConfig.groups.count { d -> d.name == intent.groupName }
                        if (groupCount > 1) {
                            //todo tell user that it has more 1 of the same Group name
                            return@launch
                        }
                        val isDelete = dialogDeleteService.open(Unit)
                        if (isDelete) {
                            val partConfig = state().partConfig
                            val config =
                                partConfig.copy(groups = partConfig.groups.filter { d -> d.name != intent.groupName })
                            dispatch(SetPartConfig(config))
                        }
                    }
                }

                is Intent.SetGroupColor -> {
                    scope.launch {
                        val result = dialogColorPickerService.open(intent.color)
                        if (result != null) {
                            changeGroup(groupName = intent.groupName, change = {
                                it.copy(color = result)
                            })
                        }
                    }
                }

                is Intent.SetGraphSettings -> {
                    val config = intent.config
                    val partConfig = state().partConfig
                    val oldSettings = partConfig.config
                    if (config != oldSettings) {
                        if (config.isGraphSettingsNudge(oldSettings)) {
                            engine.nudge()
                        }
                    }

                    dispatch(
                        SetPartConfig(
                            partConfig.copy(config = config)
                        )
                    )
                }

                is Intent.SetFilter -> {
                    val filter = intent.value
                    val partConfig = state().partConfig
                    if (filter != partConfig.filter) {
                        engine.reheat()
                    }
                    graphEco.setGraphConfig(filter)

                    // Ensure groups sync if you ever update partConfig
                    graphEco.setGroups(partConfig.groups)

                    dispatch(
                        SetPartConfig(
                            partConfig.copy(filter = filter)
                        )
                    )
                }

                Intent.OpenConfigs -> scope.launch {
                    val config = state().partConfig
                    val result =
                        dialogGraphConfigsService.open(DialogGraphConfigsService.Input(config))
                    if (result != null) {
                        engine.reheat()
                        graphEco.setGraphConfig(result.part.filter)
                        graphEco.setGroups(result.part.groups)
                        dispatch(GraphStore.Msg.SetPartConfig(result.part))
                    }
                }
            }
        }
    }

    private object ReducerImpl : Reducer<State, GraphStore.Msg> {
        override fun State.reduce(msg: GraphStore.Msg): State {
            return when (msg) {
                is SetNodes -> copy(graphNodes = msg.value.toImmutableList())
                is SetConnections -> copy(connections = msg.value)
                is SetZoom -> copy(zoom = msg.value)
                is SetIsShowSettings -> copy(isShowSettings = msg.value)
                is SetGraphUserPosition -> copy(graphUserPosition = msg.value)
                is SetCoordinates -> copy(coordinates = msg.value)
                is SetPartConfig -> copy(partConfig = msg.value)
                is SetNodeLands -> {
                    copy(nodeLands = msg.value.toKmpImmutableMap())
                }
            }
        }
    }
}