package com.moly3.cedarjam.pages.page_graph.store

import androidx.compose.ui.geometry.Offset
import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.statekeeper.StateKeeper
import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.moly3.cedarjam.navigation.BaseExecutor
import com.moly3.cedarjam.navigation.consumeOrDefault
import com.moly3.cedarjam.pages.page_graph.Intent
import com.moly3.cedarjam.pages.page_graph.State
import com.moly3.cedarjam.pages.page_graph.State.Companion.fromSaveable
import com.moly3.cedarjam.pages.page_graph.State.Companion.toSaveable
import com.moly3.cedarjam.pages.page_graph.store.GraphStore.Msg.SetConfig
import com.moly3.cedarjam.pages.page_graph.store.GraphStore.Msg.SetConnections
import com.moly3.cedarjam.pages.page_graph.store.GraphStore.Msg.SetCoordinates
import com.moly3.cedarjam.pages.page_graph.store.GraphStore.Msg.SetGraphUserPosition
import com.moly3.cedarjam.pages.page_graph.store.GraphStore.Msg.SetGraphViewSettings
import com.moly3.cedarjam.pages.page_graph.store.GraphStore.Msg.SetIsShowSettings
import com.moly3.cedarjam.pages.page_graph.store.GraphStore.Msg.SetNodes
import com.moly3.cedarjam.pages.page_graph.store.GraphStore.Msg.SetZoom
import com.moly3.cedarjam.core.domain.io
import com.moly3.cedarjam.core.domain.model.bind
import com.moly3.cedarjam.core.domain.model.resultBlock
import com.moly3.cedarjam.core.domain.service.ObsGraphEco
import com.moly3.cedarjam.core.domain.repository.IAppEnvironment
import com.moly3.cedarjam.core.ui.service.MacTrackpadGestureService
import com.moly3.cedarjam.core.domain.service.WorkspaceSession
import com.moly3.cedarjam.core.domain.usecase.IOpenNodeDataUseCase
import com.moly3.cedarjam.navigation.Navigator
import com.moly3.cedarjam.navigation.mapper.toRoute
import com.moly3.cedarjam.pages.page_graph.placeNodesCircular
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toPersistentMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import com.moly3.cedarjam.navigation.AppGraphServicesLocator

internal class GraphStoreFactory(
    private val storeFactory: StoreFactory,
    private val lifecycle: Lifecycle,
    private val workspaceSession: WorkspaceSession,
    private val openWorkspaceSettings: (Boolean) -> Unit
) {

    private val d get() = AppGraphServicesLocator.instance
    private val navigator: Navigator get() = d.navigator
    private val _isMouseCapturedState = MutableStateFlow(false)
    private val appEnvironment: IAppEnvironment get() = d.appEnvironment
    private val macTrackpadGestureService: MacTrackpadGestureService get() = d.macTrackpadGestureService
    private val openNodeDataUseCase: IOpenNodeDataUseCase get() =
        d.openNodeDataUseCaseFactory(workspaceSession.fileManagerService)

    fun create(stateKeeper: StateKeeper): GraphStore = object : GraphStore,
        Store<Intent, State, Unit> by storeFactory.create(
            name = GraphStore::class.simpleName,
            initialState = stateKeeper.consumeOrDefault(
                "GraphStore",
                State.SaveableState.serializer(),
                default = State.SaveableState()
            ).fromSaveable(),
            bootstrapper = SimpleBootstrapper(Unit),
            executorFactory = ::ExecutorImpl,
            reducer = ReducerImpl
        ) {}.also {
        stateKeeper.register(key = "GraphStore", strategy = State.SaveableState.serializer()) {
            it.state.toSaveable()
        }
    }

    private inner class ExecutorImpl :
        BaseExecutor<Intent, Unit, State, GraphStore.Msg, Unit>(lifecycle) {

        private val graphEco: ObsGraphEco by lazy {
            ObsGraphEco(
                scope = scope,
                workspaceSession = workspaceSession,
                config = state().config,
                appEnvironment = appEnvironment
            )
        }

        override fun onStart(scopeFromStartToStop: CoroutineScope) {
            super.onStart(scopeFromStartToStop)

            scopeFromStartToStop.launch {
                macTrackpadGestureService.valueStateFlow.collectLatest {
                    if (_isMouseCapturedState.value) {
                        val newZoom = state().zoom + it.toFloat()
                        dispatch(SetZoom(newZoom))
                    }
                }
            }
            scopeFromStartToStop.launch {
                graphEco.graphState.collectLatest {
                    dispatch(SetConfig(it))
                }
            }
            scopeFromStartToStop.launch {
                graphEco.connectionsFlow
                    .map {
                        it
                            .map { d -> d.key to d.value.toImmutableList() }
                            .toMap()
                            .toPersistentMap()
                    }
                    .flowOn(io)
                    .collectLatest {
                        dispatch(
                            SetConnections(it)
                        )
                    }
            }
            scopeFromStartToStop.launch {
                println("scopeFromStartToStop graphEco.nodes")
                graphEco.nodes.collectLatest {
                    if (state().coordinates.isEmpty()) {
                        val mutMap = mutableMapOf<String, Offset>()
                        placeNodesCircular(
                            stateNodes = it,
                            changeCoords = mutMap
                        )
                        dispatch(SetCoordinates(mutMap.toPersistentMap()))
                    }
                    println("scopeFromStartToStop collectLatest graphEco.nodes ${it.size}")
                    dispatch(SetNodes(it))
                }
            }
        }

        override fun executeIntent(intent: Intent) {
            when (intent) {

                is Intent.SetConfig -> {
                    graphEco.setGraphConfig(intent.value)
                }

                is Intent.OpenNodeData -> {
                    scope.launch(io) {

                        resultBlock {
                            val result = openNodeDataUseCase.invoke(intent.value, true)
                            navigator.navigate(bind(result).toRoute())
                        }
                    }
                }

                is Intent.SetZoom -> {
                    dispatch(SetZoom(intent.value))
                }

                is Intent.SetIsShowSettings -> {
                    dispatch(SetIsShowSettings(intent.value))
                }

                is Intent.OpenWorkspaceSettings -> {
                    openWorkspaceSettings(true)
                }

                is Intent.SetGraphUserPosition -> {
                    val op = state().graphUserPosition + intent.value
                    dispatch(SetGraphUserPosition(op))
                }

                is Intent.SetGraphViewSettings -> {
                    dispatch(SetGraphViewSettings(intent.value))
                }

                is Intent.SetCoordinates -> {
                    scope.launch(Dispatchers.Main) {
                        dispatch(SetCoordinates(intent.value.toPersistentMap()))
                    }
                }

                is Intent.SetIsMouseCaptured -> {
                    scope.launch {
                        _isMouseCapturedState.emit(intent.value)
                    }
                }

                is Intent.SetVelocities -> {
                    scope.launch(Dispatchers.Main) {
                        dispatch(GraphStore.Msg.SetVelocities(intent.value.toPersistentMap()))
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
                is SetConfig -> copy(config = msg.value)
                is SetZoom -> copy(zoom = msg.value)
                is SetIsShowSettings -> copy(isShowSettings = msg.value)
                is SetGraphUserPosition -> copy(graphUserPosition = msg.value)
                is SetGraphViewSettings -> copy(graphViewSettings = msg.value)
                is SetCoordinates -> copy(coordinates = msg.value)
                is GraphStore.Msg.SetVelocities -> copy(velocities = msg.value)
            }
        }
    }
}