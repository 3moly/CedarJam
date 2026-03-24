package com.moly3.cedarjam.features.feature_canvas.ui.internal

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.innerShadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.mikepenz.hypnoticcanvas.shaderBackground
import com.mohamedrejeb.compose.dnd.drop.dropTarget
import com.moly3.cedarjam.core.domain.func.pathWrapper
import com.moly3.cedarjam.core.domain.model.FileType
import com.moly3.cedarjam.core.domain.model.canvas.ShapeData
import com.moly3.cedarjam.core.domain.model.canvas.ShapeImpl
import com.moly3.cedarjam.core.domain.model.canvas.calculateBounds
import com.moly3.cedarjam.core.domain.model.toGetFileType
import com.moly3.cedarjam.core.domain.repository.IFilesRepository
import com.moly3.cedarjam.core.ui.compositions.LocalAppTheme
import com.moly3.cedarjam.core.ui.compositions.LocalDragAndDrop
import com.moly3.cedarjam.core.ui.compositions.LocalTextStyle
import com.moly3.cedarjam.core.ui.func.navigationBarsPaddingCJ
import com.moly3.cedarjam.core.ui.func.pageControlsPadding
import com.moly3.cedarjam.core.ui.model.FileTreeItemPresentation
import com.moly3.cedarjam.core.ui.uikit.CJText
import com.moly3.cedarjam.core.ui.uikit.CJTextField
import vector.Add
import vector.TrashCan
import com.moly3.cedarjam.features.feature_canvas.Intent
import com.moly3.cedarjam.features.feature_canvas.State
import com.moly3.cedarjam.features.feature_canvas.ui.shader.UmlShader
import com.moly3.dataviz.whiteboard.func.absoluteOffset
import com.moly3.dataviz.core.whiteboard.model.Action
import com.moly3.dataviz.core.whiteboard.model.ShapeConnection
import com.moly3.dataviz.core.whiteboard.model.WhiteboardSettings
import com.moly3.dataviz.whiteboard.ui.Whiteboard
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@Composable
internal fun DialogCanvasUIContent(
    modifier: Modifier,
    filesRepository: IFilesRepository,
    state: State,
    onFileTypeView: @Composable (FileType) -> Unit,
    onIntent: (Intent) -> Unit
) {
    if (state.isShowContent) {
        val isDrawingState = remember { mutableStateOf(false) }
        val actionState = remember { mutableStateOf<Action<ShapeImpl, Long>?>(value = null) }
        val backgroundSecondary = LocalAppTheme.current.colors.backgroundSecondary
        val selectedShader: UmlShader by remember { mutableStateOf(UmlShader) }
        LaunchedEffect(backgroundSecondary) {
            selectedShader.setColor(backgroundSecondary)
        }
        LaunchedEffect(state.zoom, state.userCoordinate) {
            selectedShader.userCoordinates = state.userCoordinate
            selectedShader.zoom = state.zoom
            selectedShader.dotSpacing = 50f
        }

        Box(modifier = modifier) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
//                .hazeSource(hazeState, zIndex = 0f)
                    .shaderBackground(shader = selectedShader)
            )
            val canvasSettings = remember {
                WhiteboardSettings(
                    defaultLineColor = Color.Red,
                    sideCircleColor = Color.Cyan
                )
            }
            val dragAndDropState = LocalDragAndDrop.current
            val isDraggable = remember { mutableStateOf(false) }
            Whiteboard(
                minShapeSize = 0f,
                modifier = Modifier.dropTarget(
                    key = "targetKey:",
                    state = dragAndDropState,
                    onDragEnter = {
                        isDraggable.value = true
                        //draggableItems[data.row.id] = true
                    },
                    onDragExit = {
                        isDraggable.value = false
                        //draggableItems[data.row.id] = false
                    },
                    onDrop = { state ->
                        println("state: ${state}")
                        isDraggable.value = false
                        val presData = state.data.data
                        if (presData is FileTreeItemPresentation.FileTreeItemPresentationData.File) {
                            onIntent(Intent.AddFileShape(presData.fileNode))
                        }
                    }
                ),
                backgroundModifier = Modifier,
                connectionsModifier = Modifier,
                settings = canvasSettings,
                zoom = state.zoom,
                userCoordinate = state.userCoordinate,
                isDrawing = isDrawingState.value,
                shapes = state.shapes,
                connections = state.connections,
                onAddPath = {
                    onIntent(Intent.AddDrawingPath(it))
                },
                onMoveShape = { index, pos ->
                    onIntent(Intent.MoveShape(index, pos))
                },
                onResizeShape = { index, pos, size ->
                    onIntent(Intent.ResizeShape(index, pos, size))
                },
                onAddConnection = {
                    onIntent(
                        Intent.AddConnection(
                            ShapeConnection(
                                id = Clock.System.now().toEpochMilliseconds(),
                                fromBoxId = it.fromBoxId,
                                toBoxId = it.toBoxId,
                                fromSide = it.fromSide,
                                toSide = it.toSide,
                                arcHeight = 80f,
                                color = null
                            )
                        )
                    )
                },
                settingsPanel = { offset, action, onDoneAction ->
                    var centerWidth by remember { mutableStateOf(0f) }
                    val actualDensity = LocalDensity.current
                    when (action) {
                        is Action.DoubleClicked -> {}

                        is Action.Connection,
                        is Action.ShapeAction -> {
                            Row(
                                Modifier
                                    .absoluteOffset(
                                        ((offset / LocalDensity.current.density - Offset(
                                            centerWidth / 2f,
                                            50f
                                        ) - Offset(0f, 8f)))
                                    )
                                    .onGloballyPositioned {
                                        centerWidth =
                                            it.size.width.toFloat() / actualDensity.density
                                    }
                            ) {
                                ButtonIcon(
                                    modifier = Modifier,
                                    color = LocalAppTheme.current.colors.divide,
                                    iconColor = LocalAppTheme.current.colors.backgroundPrimary,
                                    painter = rememberVectorPainter(TrashCan),
                                    onClick = {
                                        when (action) {
                                            is Action.Connection -> {
                                                //onIntent(Intent.DeleteShape(action.))
                                                //onIntent(Intent.ChangeShape)
                                                //connections.remove(action.selectedConnection.connection)
                                            }

                                            is Action.DoubleClicked -> TODO()
                                            is Action.ShapeAction -> {
                                                onIntent(Intent.DeleteShape(action.shape))
                                            }
                                        }
                                        onDoneAction()
                                    })
                            }
                        }
                    }
                },
                onDrawBlock = { shapeState ->
                    val isDrawing = shapeState.shape.data is ShapeData.Drawing
                    val borderCoef by animateFloatAsState(
                        if (shapeState.isSelected) 3f else 1f
                    )
                    val bgColor = if (shapeState.isDoubleClicked) {
                        Color.Yellow.copy(alpha = 0.2f)
                    } else {
                        Color.Black
//                        (shapeState.shape.backgroundColor
//                            ?: Color.Black).copy(alpha = 0.3f) // Dark semi-transparent
                    }

                    Box(
                        shapeState.modifier
                            .let {
                                if (shapeState.isDoubleClicked) {
                                    it.zIndex(100f)
                                } else
                                    it
                            }
                            .fillMaxSize()
                            .let {
                                if (isDrawing)
                                    it
                                else
                                    it.background(bgColor)
                                        .border((1f * state.zoom * borderCoef).dp, Color.White)
                            }
                    ) {
                        when (val data = shapeState.shape.data) {
                            is ShapeData.Drawing -> {
                                val bounds = remember(data.value) {
                                    data.value.calculateBounds()
                                }
                                val pathData = data.value
                                val bitmap = ImageBitmap(
                                    bounds.size.width.toInt().coerceAtLeast(1),
                                    bounds.size.height.toInt().coerceAtLeast(1)
                                )
                                val canvas = Canvas(bitmap)
                                val path = Path()

                                val paint = Paint().apply {
                                    color = pathData.color
                                    style = PaintingStyle.Stroke
                                    strokeCap = StrokeCap.Round
                                    strokeJoin = StrokeJoin.Round
                                    isAntiAlias = true // Essential for smooth ink
                                }

                                val points = pathData.points
                                if (points.size > 1) {
                                    path.moveTo(points[0].x, points[0].y)

                                    for (i in 1 until points.size) {
                                        val p1 = points[i - 1]
                                        val p2 = points[i]

                                        // Calculate dynamic stroke width based on pressure
                                        paint.strokeWidth = ((p1.strokeWidth * p1.pressure) + (p2.strokeWidth * p2.pressure)) / 2f

                                        // Quadratic Bezier for smoothing
                                        val midX = (p1.x + p2.x) / 2f
                                        val midY = (p1.y + p2.y) / 2f

                                        if (i == 1) {
                                            path.lineTo(midX, midY)
                                        } else {
                                            path.quadraticTo(p1.x, p1.y, midX, midY)
                                        }

                                        // Note: Canvas.drawPath uses a single strokeWidth.
                                        // For variable width within one stroke, we draw segment by segment:
                                        canvas.drawPath(path, paint)
                                        path.reset()
                                        path.moveTo(midX, midY)
                                    }
                                }
                                Image(
                                    bitmap = bitmap,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.FillBounds // This ensures it scales with the Box
                                )
                            }

                            is ShapeData.Text -> {
                                if (shapeState.isDoubleClicked) {
                                    val textState =
                                        remember {
                                            mutableStateOf(
                                                TextFieldValue(
                                                    data.text,
                                                    selection = TextRange(data.text.length)
                                                )
                                            )
                                        }
                                    val focusRequest = remember { FocusRequester() }
                                    CJTextField(
                                        modifier = Modifier
                                            .padding(4.dp)
                                            .fillMaxWidth()
                                            .focusRequester(focusRequest),
                                        value = textState.value,
//                                        singleLine = true,
                                        onValueChange = {
                                            textState.value = it
                                        },
                                        textStyle = LocalTextStyle.current.copy(
                                            fontSize = (12 * state.zoom / LocalDensity.current.density).sp
                                        ),
                                        onDone = {
                                            val newChange = shapeState.shape.copy(
                                                data = data.copy(text = textState.value.text)
                                            )
                                            onIntent(Intent.ChangeShape(newChange))
                                            actionState.value = null
                                        }
                                    )
                                    LaunchedEffect(Unit) {
                                        focusRequest.requestFocus()
                                    }
                                } else {
                                    CJText(
                                        modifier = Modifier.align(Alignment.Center),
                                        text = data.text,
                                        color = Color.White,
                                        fontSize = (12 * state.zoom / LocalDensity.current.density).sp
                                    )
                                }
                            }

                            is ShapeData.FileNode -> {

                                var fileType by remember { mutableStateOf<FileType?>(null) }
                                fileType?.let { onFileTypeView(it) }

                                LaunchedEffect(
                                    data.relativeToFilePath,
                                    state.workspaceFullpath
                                ) {
                                    val workspaceFullPath = state.workspaceFullpath
                                    val fileRelativePath = data.relativeToFilePath
                                    if (workspaceFullPath != null) {
                                        try {
                                            val fileNode =
                                                filesRepository.getFileNodeFromFullPath(
                                                    workspacePath = workspaceFullPath,
                                                    pathWrapper(
                                                        workspaceFullPath,
                                                        fileRelativePath
                                                    ).pathString,
                                                    false
                                                )
                                            fileType =
                                                fileNode.toGetFileType(filesRepository = filesRepository)
                                        } catch (exc: Exception) {
                                            fileType = null
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                onZoomChange = {
                    onIntent(Intent.SetZoom(it))
                },
                action = actionState.value,
                roundToNearest = null,
                onActionSet = {
                    actionState.value = it
                },
                onUserCoordinateChange = {
                    onIntent(Intent.SetUserCoordinate(it))
                },
                consume = true,
                circleRadius = 12f,
                connectionDragBlankId = 1L,
                onDrawConnectionCircle = { shape, modifier ->
                    Box(modifier = modifier.background(Color.White, shape).innerShadow(shape) {
                        color = Color.Black
                        radius = 4f
                    })
                }
            )
            if (isDraggable.value) {
                Box(Modifier.fillMaxSize().background(Color.Gray.copy(alpha = 0.4f)))
            }
            val paddingAdd = pageControlsPadding()
            Row(
                modifier = Modifier
                    .padding(bottom = 8.dp + paddingAdd)
                    .navigationBarsPaddingCJ()
                    .align(Alignment.BottomCenter),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ButtonIcon(
                    modifier = Modifier,
                    painter = rememberVectorPainter(Add),
                    color = LocalAppTheme.current.colors.divide,
                    iconColor = LocalAppTheme.current.colors.backgroundPrimary,
                    onClick = {
                        onIntent(Intent.AddShape)
                    })
                Box(
                    Modifier.size(30.dp)
                        .background(if (isDrawingState.value) Color.Green else Color.Gray)
                        .clickable {
                            isDrawingState.value = !isDrawingState.value
                        })
            }
        }
    }
}