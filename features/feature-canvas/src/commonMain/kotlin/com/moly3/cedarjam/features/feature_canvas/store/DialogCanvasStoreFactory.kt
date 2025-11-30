package com.moly3.cedarjam.features.feature_canvas.store

import androidx.compose.ui.geometry.Offset
import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.statekeeper.StateKeeper
import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.moly3.cedarjam.core.domain.func.getRelativePath
import com.moly3.cedarjam.features.feature_canvas.Intent
import com.moly3.cedarjam.features.feature_canvas.State
import com.moly3.cedarjam.features.feature_canvas.State.Companion.fromSaveable
import com.moly3.cedarjam.features.feature_canvas.State.Companion.toSaveable
import com.moly3.cedarjam.features.feature_canvas.store.DialogCanvasStore.*
import com.moly3.cedarjam.features.feature_canvas.store.DialogCanvasStore.Msg.*
import com.moly3.cedarjam.navigation.BaseExecutor
import com.moly3.cedarjam.navigation.consumeOrDefault
import com.moly3.cedarjam.core.domain.model.FileTreeNode
import com.moly3.cedarjam.core.domain.model.ResultWrapper
import com.moly3.cedarjam.core.domain.model.bind
import com.moly3.cedarjam.core.domain.model.canvas.CanvasDataWithErrors
import com.moly3.cedarjam.core.domain.model.canvas.ShapeData
import com.moly3.cedarjam.core.domain.model.canvas.ShapeImpl
import com.moly3.cedarjam.core.domain.model.node.ObsidianGraphData
import com.moly3.cedarjam.core.domain.model.resultBlock
import com.moly3.cedarjam.core.domain.repository.IFilesRepository
import com.moly3.cedarjam.core.domain.service.WorkspaceSession
import com.moly3.cedarjam.core.domain.usecase.IOpenNodeDataUseCase
import com.moly3.cedarjam.core.ui.service.MacTrackpadGestureService
import com.moly3.cedarjam.navigation.Navigator
import com.moly3.cedarjam.navigation.mapper.toRoute
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf
import kotlin.getValue
import kotlin.math.max
import kotlin.math.min
import kotlin.time.Clock
import kotlin.time.ExperimentalTime


internal class DialogCanvasStoreFactory(
    private val workspaceSession: WorkspaceSession,
    private val storeFactory: StoreFactory,
    private val lifecycle: Lifecycle,
    private val file: FileTreeNode.File,
    private val openNode: (ObsidianGraphData) -> Unit
) : KoinComponent {

    private val openNodeDataUseCase: IOpenNodeDataUseCase by inject {
        parametersOf(workspaceSession.fileManagerService)
    }

    private val filesRepository: IFilesRepository by inject()
    private val navigator: Navigator by inject()
    private val magnifier: MacTrackpadGestureService by inject()

    fun create(stateKeeper: StateKeeper): DialogCanvasStore = object : DialogCanvasStore,
        Store<Intent, State, Unit> by storeFactory.create(
            name = DialogCanvasStore::class.simpleName,
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
                workspaceSession.workspaceEnvStateFlow.collectLatest {
                    dispatch(Msg.SetWorkspaceFullpath(it.getWorkspace().absolutePath))
                }
            }
            scopeFromStartToStop.launch {
                val minZoom = 0.5f
                val maxZoom = 5f

                magnifier.valueStateFlow.collectLatest {
                    val state = state().zoom

                    val zoom = max(minZoom, min(maxZoom, (it + state).toFloat()))
                    dispatch(Msg.SetZoom(zoom))
                }
            }
            scopeFromStartToStop.launch {
                val data = filesRepository.getNodeCanvas(file.getFullPath())
                when (data) {
                    is ResultWrapper.Error -> {}
                    is ResultWrapper.Success -> {

                        val shapes = data.value.shapes.map {
                            when (it) {
                                is ResultWrapper.Error -> {
                                    ShapeImpl(
                                        id = it.error.id.toLong(),
                                        position = Offset(
                                            it.error.position.x,
                                            it.error.position.y
                                        ),
                                        size = Offset(it.error.size.x, it.error.size.y),
                                        color = null,
                                        data = ShapeData.Text("error")
                                    )
                                }

                                is ResultWrapper.Success -> {
                                    it.value
                                }
                            }
                        }
                        val connections = data.value.connections.mapNotNull {
                            when (it) {
                                is ResultWrapper.Error -> {
                                    null
                                }

                                is ResultWrapper.Success -> {
                                    it.value
                                }
                            }
                        }
                        dispatch(Msg.SetShapes(value = shapes.toPersistentList()))
                        dispatch(Msg.SetConnections(value = connections.toPersistentList()))
                    }
                }
            }
        }

        private fun save() {
            scope.launch {
                val state = state()
                val shapes = state.shapes.map {
                    ResultWrapper.Success(it)
                }
                val connections = state.connections.map {
                    ResultWrapper.Success(it)
                }
                filesRepository.saveNodeCanvas(
                    file.getFullPath(),
                    data = CanvasDataWithErrors(shapes = shapes, connections = connections)
                )
            }
        }

        @OptIn(ExperimentalTime::class)
        override fun executeIntent(intent: Intent) {
            when (intent) {

                is Intent.SetZoom -> {
                    dispatch(SetZoom(intent.value))
                }

                Intent.Close -> dispatch(SetIsShowContent(false))
                is Intent.AddFileShape -> {
                    val stat = state().userCoordinate
                    val work = workspaceSession.workspaceEnvStateFlow.value.getWorkspace()
                    val shapes = state().shapes.toMutableList()
                    val relative = intent.file.getRelativePath(workspacePath = work.absolutePath)
                    shapes.add(
                        ShapeImpl(
                            Clock.System.now().toEpochMilliseconds(),
                            position = stat,
                            size = Offset(50f, 50f),
                            data = ShapeData.FileNode(relativeToFilePath = relative)
                        )
                    )
                    dispatch(SetShapes(value = shapes.toPersistentList()))
                }

                Intent.AddShape -> {
                    val shapes = state().shapes.toMutableList()
                    shapes.add(
                        ShapeImpl(
                            Clock.System.now().toEpochMilliseconds(),
                            position = Offset(0f, 0f),
                            size = Offset(50f, 50f),
                            data = ShapeData.Text("123")
                        )
                    )
                    dispatch(SetShapes(value = shapes.toPersistentList()))
                }

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

                is Intent.AddConnection -> {
                    val connections = state().connections.toMutableList()
                    connections.add(intent.arcConnection)
                    dispatch(SetConnections(connections.toPersistentList()))
                    save()
                }

                is Intent.SetUserCoordinate -> {
                    scope.launch(Dispatchers.Main.immediate) {
                        dispatch(SetUserCoordinate(intent.value))
                    }
                }

                is Intent.MoveShape -> {
                    val shapes = state().shapes.toMutableList()
                    shapes[intent.index] = shapes[intent.index].copy(position = intent.position)
                    dispatch(SetShapes(value = shapes.toPersistentList()))
                    save()
                }

                is Intent.ResizeShape -> {
                    val shapes = state().shapes.toMutableList()
                    shapes[intent.index] = shapes[intent.index].copy(
                        position = intent.position,
                        size = intent.size
                    )
                    dispatch(SetShapes(value = shapes.toPersistentList()))
                    save()
                }

                is Intent.ChangeShape -> {
                    val shapes = state().shapes.toMutableList()
                    val oldShape = shapes.firstOrNull { b -> b.id == intent.shape.id }
                    val index = shapes.indexOf(oldShape)
                    shapes[index] = intent.shape
                    dispatch(SetShapes(value = shapes.toPersistentList()))
                    save()
                }
            }
        }
    }

    private object ReducerImpl : Reducer<State, Msg> {
        override fun State.reduce(msg: Msg): State {
            return when (msg) {
                is SetShapes -> copy(shapes = msg.value)
                is SetZoom -> copy(zoom = msg.value)
                is SetIsShowContent -> copy(isShowContent = msg.value)
                is SetUserCoordinate -> copy(userCoordinate = msg.value)
                is SetConnections -> copy(connections = msg.value)
                is SetWorkspaceFullpath -> copy(workspaceFullpath = msg.value)
            }
        }
    }
}