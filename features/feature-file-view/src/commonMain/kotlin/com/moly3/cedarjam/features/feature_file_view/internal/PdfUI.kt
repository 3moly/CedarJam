package com.moly3.cedarjam.features.feature_file_view.internal

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.moly3.cedarjam.features.feature_file_view.ObsPdfDocument
import com.moly3.cedarjam.features.feature_file_view.getObsPdfDocument
import com.moly3.cedarjam.core.domain.io
import com.moly3.cedarjam.core.domain.model.FileType
import com.moly3.cedarjam.core.ui.service.MacTrackpadGestureService
import com.moly3.cedarjam.core.ui.compositions.LocalAppTheme
import com.moly3.cedarjam.core.ui.compositions.LocalTextStyle
import com.moly3.cedarjam.core.ui.func.darker
import com.moly3.cedarjam.core.ui.onPointerEvent
import com.moly3.cedarjam.core.ui.uikit.CJCircularProgressIndicator
import com.moly3.cedarjam.core.ui.uikit.CJText
import com.moly3.cedarjam.core.ui.uikit.CJTextField
import com.moly3.cedarjam.core.ui.uikit.CJZoomableViewLayout
import com.moly3.cedarjam.core.ui.uikit.CJIcon
import com.moly3.cedarjam.core.ui.vectors.ArrowLeft
import com.moly3.cedarjam.core.ui.vectors.ArrowRight
import com.moly3.cedarjam.core.ui.volumedBorderStroke
import dev.chrisbanes.haze.HazeDefaults
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import kotlinx.coroutines.launch

@Composable
internal fun PdfUI(
    fileType: FileType.PDF,
    macTrackpadGestureService: MacTrackpadGestureService,
    back: () -> Unit,
    forward: () -> Unit,
    toPage: (Int) -> Unit
) {
    var documentState by remember { mutableStateOf<ObsPdfDocument?>(null) }
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
    Box(
        modifier = Modifier
            .fillMaxSize()
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
            var text by remember { mutableStateOf<String?>(null) }
            if (painter != null) {

                CJZoomableViewLayout(
                    modifier = Modifier.fillMaxSize().hazeSource(hazeState),
                    macTrackpadGestureService = macTrackpadGestureService
                ) {
                    Image(
                        painter = painter!!,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            } else {
                CJCircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            Row(
                modifier = Modifier
                    .padding(bottom = 32.dp)
                    .align(Alignment.BottomCenter)
                    .clip(RoundedCornerShape(16.dp))
                    //todo .background(LocalAppTheme.current.colors.backgroundPrimary)
                    .hazeEffect(state = hazeState, style = hazeStyle)
                    .border(volumedBorderStroke, shape = RoundedCornerShape(16.dp))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CJIcon(
                    modifier = Modifier,
                    painter = rememberVectorPainter(ArrowLeft),
                    isEnabled = canGoBack,
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
                        .clickable {},
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
                    }
                )
                CJText(text = "of")
                CJText(text = documentState!!.getNumberOfPages().toString())
                CJIcon(
                    modifier = Modifier,
                    painter = rememberVectorPainter(ArrowRight),
                    isEnabled = canGoForward,
                    onClick = {
                        forward()
                    })
            }
            LaunchedEffect(documentState, currentPage) {
                launch(io) {
                    if (documentState != null) {
                        try {
                            painter = documentState!!.getPagePainter(currentPage - 1)

                            text = documentState!!.getPageText(currentPage - 1)
                        } catch (exc: Exception) {
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
        LaunchedEffect(Unit) {
            launch(io) {
                documentState = getObsPdfDocument(fileType.fileNode.getFullPath())
            }
        }
    }
}