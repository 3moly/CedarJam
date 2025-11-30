package com.moly3.cedarjam.features.feature_graph.store

import androidx.compose.ui.geometry.Offset
import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.statekeeper.StateKeeper
import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.moly3.cedarjam.features.feature_graph.Intent
import com.moly3.cedarjam.features.feature_graph.State
import com.moly3.cedarjam.features.feature_graph.State.Companion.fromSaveable
import com.moly3.cedarjam.features.feature_graph.State.Companion.toSaveable
import com.moly3.cedarjam.features.feature_graph.store.DialogGraphStore.*
import com.moly3.cedarjam.features.feature_graph.store.DialogGraphStore.Msg.*
import com.moly3.cedarjam.navigation.BaseExecutor
import com.moly3.cedarjam.navigation.consumeOrDefault
import com.moly3.cedarjam.core.domain.io
import com.moly3.cedarjam.core.domain.model.bind
import com.moly3.cedarjam.core.domain.model.node.ObsidianGraphData
import com.moly3.cedarjam.core.domain.model.placeNodesCircular
import com.moly3.cedarjam.core.domain.model.resultBlock
import com.moly3.cedarjam.core.domain.service.WorkspaceSession
import com.moly3.cedarjam.core.domain.usecase.IOpenNodeDataUseCase
import com.moly3.cedarjam.navigation.Navigator
import com.moly3.cedarjam.navigation.mapper.toRoute
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.collections.immutable.toPersistentMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf
import kotlin.getValue

internal class DialogGraphStoreFactory(
    private val workspaceSession: WorkspaceSession,
    private val storeFactory: StoreFactory,
    private val lifecycle: Lifecycle,
    private val startTargetId: String?,
    private val openNode: (ObsidianGraphData) -> Unit
) : KoinComponent {

    private val navigator: Navigator by inject()
    private val openNodeDataUseCase: IOpenNodeDataUseCase by inject {
        parametersOf(workspaceSession.fileManagerService)
    }

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

        @OptIn(ExperimentalCoroutinesApi::class)
        override fun onStart(scopeFromStartToStop: CoroutineScope) {
            super.onStart(scopeFromStartToStop)

            scopeFromStartToStop.launch {
                workspaceSession.graphEco.connectionsFlow
                    .map {
                        it
                            .map { d -> d.key to d.value.toImmutableList() }
                            .toMap()
                            .toPersistentMap()
                    }
                    .flowOn(io)
                    .collectLatest {
                        dispatch(Msg.SetConnections(it))
                    }
            }
            scopeFromStartToStop.launch {
                combine(
                    workspaceSession.graphEco.nodes,
                    workspaceSession.graphEco.connectionsFlow
                ) { nodes, connections ->
                    val filteredNodes = if (startTargetId != null) {
                        val masterConnections = connections[startTargetId] ?: listOf()
                        nodes.filter { it.id == startTargetId || masterConnections.contains(it.id) }
                    } else
                        nodes
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
                is Intent.SetCoordinates -> {
                    dispatch(SetCoordinates(intent.value.toPersistentMap()))
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

                is Intent.OpenNode -> {
                    scope.launch {
                        resultBlock {
                            val result = openNodeDataUseCase.invoke(intent.value, true)
                            navigator.navigate(bind(result).toRoute())
                        }
                    }
                }
            }
        }
    }

    private object ReducerImpl : Reducer<State, Msg> {
        override fun State.reduce(msg: Msg): State {
            return when (msg) {
                is Msg.SetConnections -> copy(connections = msg.value)
                is Msg.SetCoordinates -> copy(coordinates = msg.value)
                is Msg.SetNodes -> copy(graphNodes = msg.value)
                is Msg.SetVelocities -> copy(velocities = msg.value)
                is Msg.SetZoom -> copy(zoom = msg.value)
                is Msg.SetIsShowContent -> copy(isShowContent = msg.value)
            }
        }
    }
}