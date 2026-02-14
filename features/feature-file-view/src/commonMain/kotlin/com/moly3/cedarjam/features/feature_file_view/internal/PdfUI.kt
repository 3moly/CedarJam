package com.moly3.cedarjam.features.feature_file_view.internal

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PaintingStyle.Companion.Stroke
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import co.touchlab.kermit.Logger
import com.github.panpf.zoomimage.CoilZoomAsyncImage
import com.github.panpf.zoomimage.ZoomImage
import com.github.panpf.zoomimage.compose.ZoomState
import com.github.panpf.zoomimage.compose.rememberZoomState
import com.moly3.cedarjam.core.domain.func.getPlatform
import com.moly3.cedarjam.features.feature_file_view.ObsPdfDocument
import com.moly3.cedarjam.features.feature_file_view.getObsPdfDocument
import com.moly3.cedarjam.core.domain.io
import com.moly3.cedarjam.core.domain.model.AnnotationDTO
import com.moly3.cedarjam.core.domain.model.FileType
import com.moly3.cedarjam.core.domain.model.Platform
import com.moly3.cedarjam.core.domain.model.request.CreateAnnotationRequest
import com.moly3.cedarjam.core.ui.service.MacTrackpadGestureService
import com.moly3.cedarjam.core.ui.compositions.LocalAppTheme
import com.moly3.cedarjam.core.ui.compositions.LocalTextStyle
import com.moly3.cedarjam.core.ui.func.blendMode
import com.moly3.cedarjam.core.ui.func.darker
import com.moly3.cedarjam.core.ui.func.flatClickable
import com.moly3.cedarjam.core.ui.model.CJText
import com.moly3.cedarjam.core.ui.onPointerEvent
import com.moly3.cedarjam.core.ui.uikit.CJButton
import com.moly3.cedarjam.core.ui.uikit.CJCircularProgressIndicator
import com.moly3.cedarjam.core.ui.uikit.CJText
import com.moly3.cedarjam.core.ui.uikit.CJTextField
import com.moly3.cedarjam.core.ui.uikit.CJZoomableViewLayout
import com.moly3.cedarjam.core.ui.uikit.CJIcon
import com.moly3.cedarjam.core.ui.vectors.ArrowLeft
import com.moly3.cedarjam.core.ui.vectors.ArrowRight
import com.moly3.cedarjam.core.ui.volumedBorderStroke
import com.moly3.cedarjam.features.feature_file_view.func.drawAnnotationsBehind
import dev.chrisbanes.haze.HazeDefaults
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import io.github.fletchmckee.liquid.LiquidState
import io.github.fletchmckee.liquid.liquefiable
import io.github.fletchmckee.liquid.liquid
import io.github.fletchmckee.liquid.rememberLiquidState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.launch
import kotlin.math.min


fun AnnotationDTO.toPx(
    canvasSize: IntSize
): Rect {
    val drawnW = canvasSize.width.toFloat()
    val drawnH = canvasSize.height.toFloat()

    val left = drawnW * x
    val top = drawnH * (1f - y - height)

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
//    var documentState by remember { mutableStateOf<ObsPdfDocument?>(null) }
    val pdfFullPath = remember(fileType.fileNode) {
        val abs = fileType.fileNode.getFullPath()
        abs
    }
    val documentState = getObsPdfDocument(pdfFullPath)
//    LaunchedEffect(Unit) {
//        launch(io) {
//            if (documentState == null) {
//                documentState =
//            }
//        }
//    }
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
    val hazeState = rememberHazeState(blurEnabled = false)
    val primaryColor = LocalAppTheme.current.colors.backgroundPrimary.darker()
    val hazeStyle = remember(primaryColor) {
        HazeStyle(
            backgroundColor = primaryColor,
            tints = listOf(HazeTint(primaryColor.copy(0.5f))),
            blurRadius = 4.dp,
            noiseFactor = HazeDefaults.noiseFactor
        )
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
            val zoomState: ZoomState = rememberZoomState()
            val liquidState: LiquidState = rememberLiquidState()
            if (documentState != null) {
                var painter by remember { mutableStateOf<Painter?>(null) }
                Row(Modifier.fillMaxSize()) {
                    when (getPlatform()) {
                        Platform.Android,
                        Platform.Jvm,
                        Platform.Wasm -> {
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
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .hazeSource(hazeState)
                                            .liquefiable(liquidState),
                                        macTrackpadGestureService = macTrackpadGestureService
                                    ) {
                                        painter?.let {
                                            Image(
                                                painter = it,
                                                contentDescription = null,
                                                modifier = Modifier
                                                    .drawAnnotationsBehind(
                                                        currentPage = currentPage,
                                                        annotations = annotations
                                                    )
                                            )
                                        }
                                    }

                                } else {
                                    CJCircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                                }
                            }
                        }

                        Platform.Ios -> {
                            CJPdf(
                                Modifier.fillMaxSize().liquefiable(liquidState),
                                currentPage = currentPage,
                                pdf = documentState!!,
                                annotations = annotations,
                                filePath = fileType.fileNode.getFullPath(),
                                onAddAnnotation = onAddAnnotation,
                                onDeleteAnnotation = onDeleteAnnotation
                            )
                        }
                    }
                }
                Row(
                    modifier = Modifier
                        .padding(bottom = 32.dp)
                        .align(Alignment.BottomCenter)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            LocalAppTheme.current.colors.backgroundPrimary,
                            shape = RoundedCornerShape(16.dp)
                        )
                        //.hazeEffect(state = hazeState, style = hazeStyle)
                        .border(volumedBorderStroke, shape = RoundedCornerShape(16.dp))
                        .liquid(liquidState) {
                            this.frost = 1.dp
                            refraction = 0.15f
                            edge = 0.05f
                            curve = 0.4f
                            saturation = 0.5f
                            dispersion = 1f
                        }
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
                        text = "of", Modifier.blendMode(BlendMode.Difference),
                        color = Color.White
                    )
                    CJText(
                        text = documentState?.getNumberOfPages().toString(),
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

                            //                                            imgBitmap = getPdfImage(
//                                                Path(
//                                                    fileNode.fileNode.getFullPath()
//                                                ).toString(),
//                                                page = currentPage,
//                                                dpi = 100f
//                                            )
                        }
                    }
                }
            } else {
                CJCircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
//            Row(
//                modifier = Modifier.align(Alignment.BottomEnd).background(Color.Black)
//                    .padding(8.dp),
//                horizontalArrangement = Arrangement.spacedBy(8.dp)
//            ) {
//                Box(
//                    modifier = Modifier.background(Color.Black)
//                        .flatClickable {
//                            //      currentPage = currentPage,
//                            //                                pdf = documentState!!,
//                            //                                annotations = annotations,
//                            //                                filePath = fileType.fileNode.getFullPath(),
//                            onAddAnnotation(
//                                CreateAnnotationRequest(
//                                    dataPath = fileType.fileNode.getFullPath(),
//                                    dataPoint = (currentPage - 1).toDouble(),
//                                    description = "-",
//                                    x = 0.5f,
//                                    y = 0.5f,
//                                    width = 0.1f,
//                                    height = 0.1f,
//                                )
//                            )
//                        }
//                ) {
//                    CJText(text = "annotate")
//                }
//                Box(
//                    modifier = Modifier.background(Color.Black)
//                        .flatClickable {
//                            isShowAnnotations.value = !isShowAnnotations.value
//                        }
//                ) {
//                    CJText(text = "annotations: ${annotations.size}")
//                }
//            }

        }

        Row(
            modifier = Modifier.fillMaxHeight()
                .background(LocalAppTheme.current.colors.backgroundPrimary)
        ) {
            AnimatedVisibility(isShowAnnotations.value) {
                Column(
                    modifier = Modifier.width(200.dp).fillMaxHeight().verticalScroll(
                        rememberScrollState()
                    )
                ) {
                    for (item in annotations) {
                        Box(
                            modifier = Modifier
                                .padding(4.dp)
                                .fillMaxWidth()
                                .heightIn(max = 150.dp)
                                .border(1.dp, Color.White)
                                .padding(8.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                if (!item.description.isEmpty()) {
                                    CJText("Примеч: ${item.description}", maxLines = 3)
                                }
                                Row {
                                    CJButton(text = "go to ${item.dataPoint}") {
                                        toPage(item.dataPoint.toInt() + 1)
                                    }
                                    Box(Modifier.weight(1f))
                                    CJButton(text = "remove") {
                                        onDeleteAnnotation(item)
                                    }
                                }

                            }
                        }
                    }
                }
            }
        }
    }

}