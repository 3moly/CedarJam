package com.moly3.cedarjam.core.ui.uikit

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import co.touchlab.kermit.Logger
import com.moly3.cedarjam.core.ui.service.MacTrackpadGestureService
import com.moly3.cedarjam.core.ui.motions.PointerRequisite
import com.moly3.cedarjam.core.ui.motions.detectPointerTransformGestures
import com.moly3.cedarjam.core.ui.onPointerEvent
import kotlinx.coroutines.flow.collectLatest
import kotlin.math.max
import kotlin.math.min

@Composable
fun CJZoomableViewLayout(
    modifier: Modifier,
    isEnable: Boolean,
    macTrackpadGestureService: MacTrackpadGestureService,
    content: @Composable () -> Unit
) {
    CJZoomableImageLayout(
        modifier = modifier,
        isEnable = isEnable,
        macTrackpadGestureService = macTrackpadGestureService
    ) {
        content()
    }
}

@Composable
fun CJZoomableImageLayout(
    modifier: Modifier = Modifier,
    isEnable: Boolean,
    macTrackpadGestureService: MacTrackpadGestureService? = null,
    minZoom: Float = 0.5f,
    maxZoom: Float = 5f,
    initialZoom: Float = 1f,
    content: @Composable () -> Unit = {}
) {
    var isMouseCaptured by remember { mutableStateOf(false) }
    val isMouseCapturedUpdated by rememberUpdatedState(isMouseCaptured)

    val scope = rememberCoroutineScope()
    var zoom by remember { mutableStateOf(initialZoom) }
    var translationX by remember { mutableStateOf(0f) }
    var translationY by remember { mutableStateOf(0f) }

    val latestZoom by rememberUpdatedState(zoom)
    val latestTranslationX by rememberUpdatedState(translationX)
    val latestTranslationY by rememberUpdatedState(translationY)

    // Listen to magnify service if provided
    LaunchedEffect(macTrackpadGestureService, isEnable) {
        macTrackpadGestureService?.valueStateFlow?.collectLatest { magnifyValue ->
            if (isEnable) {
                zoom = max(minZoom, min(maxZoom, (magnifyValue + zoom).toFloat()))
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(8.dp))
            .onPointerEvent(PointerEventType.Enter) {
                isMouseCaptured = true
            }
            .onPointerEvent(PointerEventType.Move) {
                isMouseCaptured = true
            }
            .onPointerEvent(PointerEventType.Exit) {
                isMouseCaptured = false
            }
            .let {
                if (isEnable) {
                    it.pointerInput(Unit) {
                        detectPointerTransformGestures(
                            scope = scope,
                            numberOfPointers = 0,
                            requisite = PointerRequisite.GreaterThan,
                            onVerticalScrollChange = { scrollDelta ->
                                val zoomFactor = (if (scrollDelta > 0) 1.01f else 0.99f)
                                val newZoom = max(minZoom, min(maxZoom, latestZoom * zoomFactor))
                                zoom = newZoom
                            },
                            onGesture = { gestureCentroid: Offset,
                                          gesturePan: Offset,
                                          gestureZoom: Float,
                                          gestureRotate: Float,
                                          mainPointerInputChange: PointerInputChange,
                                          pointerList: List<PointerInputChange> ->

                                when (pointerList.size) {
                                    1 -> {
                                        // Single finger/mouse - pan
                                        translationX = latestTranslationX + gesturePan.x
                                        translationY = latestTranslationY + gesturePan.y
                                    }

                                    2 -> {
                                        // Two fingers - zoom and pan
                                        val newZoom =
                                            max(minZoom, min(maxZoom, latestZoom * gestureZoom))
                                        zoom = newZoom

                                        // Apply pan with zoom compensation
                                        translationX =
                                            latestTranslationX + gesturePan.x / latestZoom
                                        translationY =
                                            latestTranslationY + gesturePan.y / latestZoom
                                    }
                                }
                            },
                            onGestureEnd = {
                                Logger.d("Gesture ended")
                            },
                            onGestureCancel = {
                                Logger.d("Gesture cancelled")
                            }
                        )
                    }
                } else {
                    it
                }
            }

        // Add additional pointer input for more precise drag handling
//            .pointerInput(Unit) {
//                detectDragGestures { change, dragAmount ->
//                    change.consume()
//                    translationX += dragAmount.x
//                    translationY += dragAmount.y
//                }
//            }
//            // Double tap to reset zoom and translation
//            .pointerInput(Unit) {
//                detectTapGestures(
//                    onDoubleTap = {
//                        zoom = initialZoom
//                        translationX = 0f
//                        translationY = 0f
//                    }
//                )
//            }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = zoom
                    scaleY = zoom
                    this.translationX = translationX
                    this.translationY = translationY
                },
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}