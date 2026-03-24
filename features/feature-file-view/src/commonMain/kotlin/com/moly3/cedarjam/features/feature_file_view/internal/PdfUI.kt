package com.moly3.cedarjam.features.feature_file_view.internal

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.moly3.cedarjam.core.domain.func.getPlatform
import com.moly3.cedarjam.features.feature_file_view.getObsPdfDocument
import com.moly3.cedarjam.core.domain.io
import com.moly3.cedarjam.core.domain.model.AnnotationDTO
import com.moly3.cedarjam.core.domain.model.FileType
import com.moly3.cedarjam.core.domain.model.Platform
import com.moly3.cedarjam.core.domain.model.request.CreateAnnotationRequest
import com.moly3.cedarjam.core.ui.service.MacTrackpadGestureService
import com.moly3.cedarjam.core.ui.compositions.LocalAppTheme
import com.moly3.cedarjam.core.ui.compositions.LocalTextStyle
import com.moly3.cedarjam.core.ui.compositions.LocalUIConfig
import com.moly3.cedarjam.core.ui.func.blendMode
import com.moly3.cedarjam.core.ui.func.navigationBarsPaddingCJ
import com.moly3.cedarjam.core.ui.func.pageControlsPadding
import com.moly3.cedarjam.core.ui.onPointerEvent
import com.moly3.cedarjam.core.ui.uikit.CJCircularProgressIndicator
import com.moly3.cedarjam.core.ui.uikit.CJText
import com.moly3.cedarjam.core.ui.uikit.CJTextField
import com.moly3.cedarjam.core.ui.uikit.CJZoomableViewLayout
import com.moly3.cedarjam.core.ui.uikit.CJIcon
import com.moly3.cedarjam.core.ui.uikit.NeumorphicShape
import vector.ArrowLeft
import vector.ArrowRight
import com.moly3.cedarjam.core.ui.volumedBorderStroke
import com.moly3.cedarjam.features.feature_file_view.func.drawAnnotationsBehind
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.launch
import vector.collection.Note


fun AnnotationDTO.toPx(
    canvasSize: IntSize
): Rect {
    val drawnW = canvasSize.width.toFloat()
    val drawnH = canvasSize.height.toFloat()

    val left = drawnW * x
    val top = drawnH * y

    return Rect(
        left,
        top,
        left + drawnW * width,
        top + drawnH * height
    )
}

@Composable
internal fun PdfUI(
    fileType: FileType.PDF,
    macTrackpadGestureService: MacTrackpadGestureService,
    annotations: ImmutableList<AnnotationDTO>,
    back: () -> Unit,
    forward: () -> Unit,
    toPage: (Int) -> Unit,
    onAddAnnotation: (CreateAnnotationRequest) -> Unit,
    onDeleteAnnotation: (AnnotationDTO) -> Unit
) {
    val isShowAnnotations = remember { mutableStateOf(false) }
    val pdfFullPath = remember(fileType.fileNode) {
        val abs = fileType.fileNode.getFullPath()
        abs
    }
    val documentState = getObsPdfDocument(pdfFullPath)
    val currentPage = fileType.currentPage
    val canGoBack = remember(documentState, currentPage) {
        if (documentState != null) {
            currentPage > 1
        } else
            false
    }
    val canGoForward = remember(documentState, currentPage) {
        val numberOfPages = documentState?.getNumberOfPages()
        if (numberOfPages != null) {
            currentPage < numberOfPages
        } else
            false
    }
    var isMouseCaptured by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(isMouseCaptured) {
        if (isMouseCaptured) {
            focusRequester.requestFocus()
        } else {
            focusRequester.freeFocus()
        }
    }
    Row(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .weight(1f)
                .onPointerEvent(PointerEventType.Enter) {
                    isMouseCaptured = true
                }
                .onPointerEvent(PointerEventType.Move) {
                    isMouseCaptured = true
                }
                .onPointerEvent(PointerEventType.Exit) {
                    isMouseCaptured = false
                }
                .onKeyEvent(onKeyEvent = { event ->
                    if (event.type == KeyEventType.KeyUp) {
                        if (Key.DirectionLeft == event.key && canGoBack) {
                            back()
                        }
                        if (Key.DirectionRight == event.key && canGoForward) {
                            forward()
                        }
                    }
                    true
                })
                .focusRequester(focusRequester)
        ) {
            if (documentState != null) {
                var painter by remember { mutableStateOf<Painter?>(null) }
                Row(Modifier.fillMaxSize()) {
                    when (getPlatform()) {
                        Platform.Android,
                        Platform.Jvm,
                        Platform.Ios,
                        Platform.Wasm -> {
                            val isEnableAnnotation = remember { mutableStateOf(false) }
                            val isEnableAnnotationUpdated by rememberUpdatedState(isEnableAnnotation.value)
                            Box(Modifier.weight(1f).fillMaxHeight()) {
                                if (painter != null) {
//                                    ZoomImage(
//                                        zoomState = zoomState,
//                                        painter = painter!!,
//                                        contentDescription = "view image",
//                                        modifier = Modifier
//                                            .fillMaxSize()
//                                            .liquefiable(liquidState)
//                                            .drawAnnotationsBehind(
//                                                zoomState = zoomState,
//                                                currentPage = currentPage,
//                                                annotations = annotations
//                                            )
//                                    )
//                                    CJText(
//                                        text = "container: ${zoomState.zoomable.transform}\n\nbaseTrans ${zoomState.zoomable.baseTransform}",
//                                        modifier = Modifier.padding(16.dp).background(Color.Black).align(Alignment.Center)
//                                    )
                                    CJZoomableViewLayout(
                                        modifier = Modifier.fillMaxSize(),
                                        isEnable = !isEnableAnnotation.value,
                                        macTrackpadGestureService = macTrackpadGestureService
                                    ) {
                                        painter?.let {
                                            val startedDragging =
                                                remember { mutableStateOf<Offset?>(null) }
                                            val endDragging =
                                                remember { mutableStateOf<Offset?>(null) }
                                            val pdfPageSize =
                                                remember { mutableStateOf<IntSize?>(null) }
                                            Image(
                                                painter = it,
                                                contentDescription = null,
                                                modifier = Modifier
                                                    .background(Color.White)
                                                    .drawAnnotationsBehind(
                                                        currentPage = currentPage,
                                                        annotations = annotations
                                                    )
                                                    .drawWithContent {
                                                        this.drawContent()
                                                        val start = startedDragging.value
                                                        val end = endDragging.value

                                                        if (start != null && end != null) {
                                                            val size = end - start
                                                            drawRect(
                                                                color = Color.Yellow.copy(alpha = 0.35f),
                                                                topLeft = start,
                                                                size = Size(
                                                                    size.x,
                                                                    size.y
                                                                )
                                                            )
                                                        }
                                                    }
                                                    .onGloballyPositioned {
                                                        pdfPageSize.value = it.size
                                                    }
                                                    .let {
                                                        if (isEnableAnnotationUpdated) {
                                                            it.pointerInput(Unit) {
                                                                detectDragGestures(
                                                                    onDragStart = {
                                                                        if (isEnableAnnotationUpdated) {
                                                                            startedDragging.value =
                                                                                it
                                                                        }
                                                                    },
                                                                    onDragEnd = {
                                                                        if (isEnableAnnotationUpdated) {
                                                                            val start =
                                                                                startedDragging.value
                                                                            val end =
                                                                                endDragging.value

                                                                            val pdf =
                                                                                pdfPageSize.value
                                                                            if (start != null && end != null && pdf != null) {

                                                                                val pdfWidth =
                                                                                    pdf.width.toFloat()
                                                                                val pdfHeight =
                                                                                    pdf.height.toFloat()

                                                                                val left = minOf(
                                                                                    start.x,
                                                                                    end.x
                                                                                )
                                                                                val top = minOf(
                                                                                    start.y,
                                                                                    end.y
                                                                                )
                                                                                val right = maxOf(
                                                                                    start.x,
                                                                                    end.x
                                                                                )
                                                                                val bottom = maxOf(
                                                                                    start.y,
                                                                                    end.y
                                                                                )

                                                                                val x1 =
                                                                                    (left / pdfWidth).coerceIn(
                                                                                        0f,
                                                                                        1f
                                                                                    )
                                                                                val y1 =
                                                                                    (top / pdfHeight).coerceIn(
                                                                                        0f,
                                                                                        1f
                                                                                    )
                                                                                val width =
                                                                                    ((right - left) / pdfWidth).coerceIn(
                                                                                        0f,
                                                                                        1f
                                                                                    )
                                                                                val height =
                                                                                    ((bottom - top) / pdfHeight).coerceIn(
                                                                                        0f,
                                                                                        1f
                                                                                    )

                                                                                onAddAnnotation(
                                                                                    CreateAnnotationRequest(
                                                                                        dataPath = fileType.fileNode.getFullPath(),
                                                                                        description = "",
                                                                                        dataPoint = (currentPage).toDouble(),
                                                                                        x = x1,
                                                                                        y = y1,       // PDF-space y (bottom origin)
                                                                                        width = width,
                                                                                        height = height,
                                                                                        rowId = null
                                                                                    )
                                                                                )
                                                                            }
                                                                            endDragging.value = null
                                                                            startedDragging.value =
                                                                                null
                                                                        }
                                                                    },
                                                                    onDragCancel = {
                                                                        endDragging.value = null
                                                                        startedDragging.value = null
                                                                    },
                                                                    onDrag = { _, dragAmount ->
                                                                        if (isEnableAnnotationUpdated) {
                                                                            endDragging.value =
                                                                                (endDragging.value
                                                                                    ?: startedDragging.value!!) + dragAmount  // ✅
                                                                        }
                                                                    }
                                                                )
                                                            }
                                                        } else {
                                                            it
                                                        }
                                                    }

                                            )
                                        }
                                    }
                                    NeumorphicShape(
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .padding(end = 16.dp, bottom = (LocalUIConfig.current.fabCircleSize.value+16).dp)
                                            .navigationBarsPaddingCJ()
                                            .size(LocalUIConfig.current.fabCircleSize),
                                        isPressed = isEnableAnnotation.value,
                                        accentColor = LocalAppTheme.current.primaryColor,
                                        painter = rememberVectorPainter(Note)
                                    ) {
                                        isEnableAnnotation.value = !isEnableAnnotation.value
                                    }

                                } else {
                                    CJCircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                                }
                            }
                        }

                        Platform.Ios -> {
                            CJPdf(
                                Modifier.fillMaxSize(),
                                currentPage = currentPage,
                                pdf = documentState,
                                annotations = annotations,
                                filePath = fileType.fileNode.getFullPath(),
                                onAddAnnotation = onAddAnnotation,
                                onDeleteAnnotation = onDeleteAnnotation
                            )
                        }
                    }
                }
                val paddingAdd = pageControlsPadding()
                Row(
                    modifier = Modifier
                        .padding(bottom = paddingAdd)
                        .navigationBarsPaddingCJ()
                        .align(Alignment.BottomCenter)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            LocalAppTheme.current.colors.backgroundPrimary,
                            shape = RoundedCornerShape(16.dp)
                        )
                        //.hazeEffect(state = hazeState, style = hazeStyle)
                        .border(volumedBorderStroke, shape = RoundedCornerShape(16.dp))
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CJIcon(
                        modifier = Modifier.blendMode(BlendMode.Difference),
                        painter = rememberVectorPainter(ArrowLeft),
                        isEnabled = canGoBack,
                        tintColor = Color.White,
                        onClick = {
                            back()
                        })
                    var textState by remember { mutableStateOf(TextFieldValue("")) }
                    LaunchedEffect(currentPage) {
                        if (textState.text != currentPage.toString()) {
                            textState = TextFieldValue((currentPage.toString()))
                        }
                    }
                    CJTextField(
                        modifier = Modifier
                            .widthIn(min = 30.dp)
                            .width(IntrinsicSize.Min)
                            .clickable {}
                            .blendMode(BlendMode.Difference),
                        value = textState,
                        onValueChange = {
                            textState = it
                        },
                        textStyle = LocalTextStyle.current.merge(
                            textAlign = TextAlign.End
                        ),
                        keyboardType = KeyboardType.Number,
                        onDone = {
                            val number = textState.text.trim().toIntOrNull()
                            if (number != null) {
                                toPage(number)
                            } else {
                                textState = TextFieldValue((currentPage.toString()))
                                //state.setTextAndPlaceCursorAtEnd(currentPage.toString())
                            }
                        },
                        color = Color.White
                    )
                    CJText(
                        text = "-", Modifier.blendMode(BlendMode.Difference),
                        color = Color.White
                    )
                    CJText(
                        text = documentState.getNumberOfPages().toString(),
                        Modifier.blendMode(BlendMode.Difference),
                        color = Color.White
                    )
                    CJIcon(
                        modifier = Modifier.blendMode(BlendMode.Difference),
                        painter = rememberVectorPainter(ArrowRight),
                        isEnabled = canGoForward,
                        tintColor = Color.White,
                        onClick = {
                            forward()
                        })
                }

                LaunchedEffect(documentState, currentPage) {
                    launch(io) {
                        painter = null
                        if (documentState != null) {
                            try {
                                painter = documentState?.getPagePainter(currentPage - 1)

                            } catch (exc: Exception) {
                                val msg = "" + exc.message
                            }
                        }
                    }
                }
            } else {
                CJCircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}