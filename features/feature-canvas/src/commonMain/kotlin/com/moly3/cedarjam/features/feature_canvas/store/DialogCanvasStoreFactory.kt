package com.moly3.cedarjam.features.feature_canvas.store

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.statekeeper.StateKeeper
import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
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
import com.moly3.cedarjam.core.domain.model.canvas.MyStylusPath
import com.moly3.cedarjam.core.domain.model.canvas.MyStylusPoint
import com.moly3.cedarjam.core.domain.model.canvas.ShapeData
import com.moly3.cedarjam.core.domain.model.canvas.ShapeImpl
import com.moly3.cedarjam.core.domain.model.canvas.toImpl
import com.moly3.cedarjam.core.domain.model.canvas.toSimple
import com.moly3.cedarjam.core.domain.model.node.ObsidianGraphData
import com.moly3.cedarjam.core.domain.model.resultBlock
import com.moly3.cedarjam.core.domain.repository.IFilesRepository
import com.moly3.cedarjam.core.domain.service.WorkspaceSession
import com.moly3.cedarjam.core.domain.usecase.IOpenNodeDataUseCase
import com.moly3.cedarjam.core.ui.service.MacTrackpadGestureService
import com.moly3.cedarjam.navigation.Navigator
import com.moly3.cedarjam.navigation.mapper.toRoute
import com.moly3.dataviz.core.whiteboard.func.calculateBounds
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import com.moly3.cedarjam.navigation.AppGraphServicesLocator
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
) {

    private val d get() = AppGraphServicesLocator.instance
    private val openNodeDataUseCase: IOpenNodeDataUseCase get() =
        d.openNodeDataUseCaseFactory(workspaceSession.fileManagerService)

    private val filesRepository: IFilesRepository get() = d.filesRepository
    private val navigator: Navigator get() = d.navigator
    private val magnifier: MacTrackpadGestureService get() = d.macTrackpadGestureService

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
                    dispatch(SetWorkspaceFullpath(it.getWorkspace().absolutePath))
                }
            }
            scopeFromStartToStop.launch {
                val minZoom = 0.5f
                val maxZoom = 5f

                magnifier.valueStateFlow.collectLatest {
                    val state = state().zoom

                    val zoom = max(minZoom, min(maxZoom, (it + state).toFloat()))
                    dispatch(SetZoom(zoom))
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
                        val drawing = data.value.drawing.mapNotNull {
                            when (it) {
                                is ResultWrapper.Error -> {
                                    null
                                }

                                is ResultWrapper.Success -> {
                                    it.value.toSimple()
                                }
                            }
                        }
                        dispatch(SetShapes(value = shapes.toPersistentList()))
                        dispatch(SetConnections(value = connections.toPersistentList()))
                        dispatch(SetDrawing(value = drawing.toPersistentList()))
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
                val drawing = state.drawing.map {
                    ResultWrapper.Success(it.toImpl())
                }
                filesRepository.saveNodeCanvas(
                    file.getFullPath(),
                    data = CanvasDataWithErrors(
                        shapes = shapes,
                        connections = connections,
                        drawing = drawing
                    )
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
                    val relative = intent.file.getRelativePath()
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

                Intent.AddShape -> {
                    val shapes = state().shapes.toMutableList()
                    val position = state().userCoordinate
                    val shapeSize = Offset(150f, 75f)
                    shapes.add(
                        ShapeImpl(
                            Clock.System.now().toEpochMilliseconds(),
                            position = position - shapeSize / 2f,
                            size = shapeSize,
                            data = ShapeData.Text("")
                        )
                    )
                    dispatch(SetShapes(value = shapes.toPersistentList()))
                }

                is Intent.AddDrawingPath -> {
                    val path = intent.drawingPath
                    val pathBounds = path.calculateBounds()

                    // 1. Shift every point's coordinates to be relative to the path's bounding box
                    val localizedPoints = path.points.map { point ->
                        point.copy(
                            x = point.x - pathBounds.globalPosition.x,
                            y = point.y - pathBounds.globalPosition.y
                        )
                    }

                    // 2. Create a new path with the localized points and the updated color
                    val localizedPath = path.copy(
                        points = localizedPoints,
                        color = Color.Cyan
                    )
                    val offsetSize = Offset(pathBounds.size.width, pathBounds.size.height)
                    val shapes = state().shapes.toMutableList()
                    shapes.add(
                        ShapeImpl(
                            Clock.System.now().toEpochMilliseconds(),
                            position = pathBounds.globalPosition,
                            size = offsetSize,
                            data = ShapeData.Drawing(
                                MyStylusPath(
                                    color = localizedPath.color,
                                    points = localizedPath.points.map { d ->
                                        MyStylusPoint(
                                            x = d.x,
                                            y = d.y,
                                            pressure = d.pressure,
                                            tiltX = d.tiltX,
                                            tiltY = d.tiltY,
                                            strokeWidth = d.strokeWidth,
                                            timestamp = d.timestamp,
                                        )
                                    }
                                ))
                        )
                    )
                    dispatch(SetShapes(value = shapes.toPersistentList()))
                    save()
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

                is Intent.DeleteShape -> {
                    val shapes = state().shapes.toMutableList()
                    val oldShape = shapes.firstOrNull { b -> b.id == intent.shape.id }
                    val index = shapes.indexOf(oldShape)
                    shapes.removeAt(index)
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
                is SetDrawing -> copy(drawing = msg.value)
            }
        }
    }
}