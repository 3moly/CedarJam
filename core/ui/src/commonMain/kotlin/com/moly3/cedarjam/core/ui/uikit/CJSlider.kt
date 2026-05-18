package com.moly3.cedarjam.core.ui.uikit

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitTouchSlopOrCancellation
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.moly3.cedarjam.core.domain.func.formatCommon
import com.moly3.cedarjam.core.ui.compositions.LocalAppTheme
import com.moly3.cedarjam.core.ui.func.isCompactUI
import kotlin.math.roundToInt

@Composable
fun CJSlider(
    modifier: Modifier = Modifier,
    value: Float,
    step: Int = 0,
    isStepCosmetic: Boolean = false,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    onValueChangeFinished: (() -> Unit)? = null,
) {
    CJNeuSlider(
        modifier = modifier,
        value = value,
        onValueChange = onValueChange,
        valueRange = valueRange,
        step = step,
        isCosmetic = isStepCosmetic,
        onValueChangeFinished = onValueChangeFinished,
        showRangeLabels = true
    )
}

@Composable
fun CJNeuSlider(
    modifier: Modifier = Modifier,
    value: Float,
    step: Int = 0,
    isCosmetic: Boolean = false,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    onValueChangeFinished: (() -> Unit)? = null,
    showRangeLabels: Boolean = false,
) {
    val appTheme = LocalAppTheme.current
    val density = LocalDensity.current

    val compact = isCompactUI()

    val thumbSizeDp = 24.dp
    val trackHeightDp = 8.dp
    val tickRowHeightDp = 8.dp

    val thumbSizePx = with(density) { thumbSizeDp.toPx() }

    val coercedValue = value.coerceIn(valueRange.start, valueRange.endInclusive)
    val rangeSpan = (valueRange.endInclusive - valueRange.start).takeIf { it > 0f } ?: 1f
    val fraction = ((coercedValue - valueRange.start) / rangeSpan).coerceIn(0f, 1f)

    fun snapFraction(raw: Float): Float {
        if (step <= 0 || isCosmetic) return raw.coerceIn(0f, 1f)
        val stepCount = step + 1
        val snapped = (raw * stepCount).roundToInt().toFloat() / stepCount
        return snapped.coerceIn(0f, 1f)
    }

    fun fractionToValue(f: Float): Float =
        (valueRange.start + f * rangeSpan).coerceIn(valueRange.start, valueRange.endInclusive)

    Column(modifier = modifier) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.CenterStart,
        ) {
            val widthPx = with(density) { maxWidth.toPx() }
            val trackUsablePx = (widthPx - thumbSizePx).coerceAtLeast(1f)
            val thumbCenterPx = thumbSizePx / 2f + fraction * trackUsablePx
            val thumbOffsetXPx = thumbCenterPx - thumbSizePx / 2f

            val pointerX = remember { mutableFloatStateOf(0f) }
            var dragActive by remember { mutableStateOf(false) }

            fun applyPointerX(x: Float, finished: Boolean = false) {
                val raw = ((x - thumbSizePx / 2f) / trackUsablePx).coerceIn(0f, 1f)
                val snapped = snapFraction(raw)
                val newValue = fractionToValue(snapped)
                if (newValue != value) onValueChange(newValue)
                if (finished) onValueChangeFinished?.invoke()
            }

            // Returns true if x (in this Box's coordinate space) lands on the thumb circle.
            fun isOnThumb(x: Float): Boolean {
                val touchPadPx = with(density) { 12.dp.toPx() }
                val half = thumbSizePx / 2f + touchPadPx
                return x >= thumbCenterPx - half && x <= thumbCenterPx + half
            }

            Column(Modifier.fillMaxWidth()) {
                Box(Modifier.height(tickRowHeightDp))

                Box(
                    modifier = Modifier
                        .height(trackHeightDp)
                        .fillMaxWidth()
                ) {
                    BoxWithConstraints(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = thumbSizeDp / 2)
                    ) {
                        NeumorphicShape(
                            buttonShape = RoundedCornerShape(trackHeightDp / 2),
                            modifier = Modifier
                                .height(trackHeightDp)
                                .fillMaxWidth(),
                            isPressed = true,
                            strength = 0.01f,
                            accentColor = Color.White,
                            isShowBigGradient = false,
                            shadowConfig = NeumorphicShadowConfig(
                                pressedShadowAlpha = 4f,
                            )
                        ) {}

                        Box(
                            modifier = Modifier
                                .height(trackHeightDp)
                                .fillMaxWidth(fraction = fraction.coerceAtLeast(0.0001f))
                                .background(
                                    color = appTheme.primaryColor,
                                    shape = RoundedCornerShape(trackHeightDp / 2)
                                )
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .height(tickRowHeightDp)
                        .fillMaxWidth()
                        .padding(horizontal = thumbSizeDp / 2),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    if (step > 0) {
                        val tickCount = step + 2
                        Row(Modifier.fillMaxWidth()) {
                            for (i in 0 until tickCount) {
                                val tickFraction = i.toFloat() / (tickCount - 1).toFloat()
                                val isActive = tickFraction <= fraction
                                Box(
                                    modifier = Modifier
                                        .size(width = 2.dp, height = 8.dp)
                                        .background(
                                            color = if (isActive) appTheme.primaryColor
                                            else appTheme.colors.secondaryFont,
                                            shape = RoundedCornerShape(1.dp)
                                        )
                                )
                                if (i < tickCount - 1) {
                                    Box(Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }

            // Thumb — purely visual, no gestures on it.
            Box(
                modifier = Modifier
                    .offset { IntOffset(thumbOffsetXPx.roundToInt(), 0) }
                    .size(thumbSizeDp)
            ) {
                NeumorphicShape(
                    modifier = Modifier.size(thumbSizeDp),
                    buttonShape = CircleShape,
                    isPressed = false,
                    strength = 2f,
                    isShowBigGradient = true,
                    shadowConfig = NeumorphicShadowConfig(
                        pressedShadowAlpha = 4f,
                    )
                ) {}
            }

            Box(
                modifier = Modifier
                    .matchParentSize()
                    .pointerInput(valueRange, step, isCosmetic, trackUsablePx, thumbSizePx, compact) {
                        awaitPointerEventScope {
                            while (true) {
                                val down = awaitFirstDown(requireUnconsumed = false)

                                // 1. Mobile constraint: If in compact UI, ignore touches not starting on the thumb.
                                // Using `continue` skips tracking this finger entirely, letting parent scroll views
                                // take over the gesture, while keeping our slider ready for the next finger touch.
                                if (compact && !isOnThumb(down.position.x)) {
                                    continue
                                }

                                // 2. Claim the gesture instantly (works for both tap and drag)
                                down.consume()
                                pointerX.floatValue = down.position.x
                                applyPointerX(down.position.x)

                                var isDragging = true
                                var dragId = down.id

                                // 3. Manually track the pointer until it lifts or is cancelled
                                while (isDragging) {
                                    val event = awaitPointerEvent()
                                    val change = event.changes.firstOrNull { it.id == dragId }

                                    if (change == null || change.isConsumed) {
                                        // Pointer disappeared unexpectedly or parent forcefully intercepted
                                        isDragging = false
                                    } else if (!change.pressed) {
                                        // Finger lifted naturally (End of tap or drag)
                                        change.consume()
                                        isDragging = false
                                    } else {
                                        // Finger moved (Dragging)
                                        val posChange = change.positionChange()
                                        if (posChange != Offset.Zero) {
                                            change.consume()
                                            pointerX.floatValue += posChange.x
                                            applyPointerX(pointerX.floatValue)
                                        }
                                    }
                                }

                                // 4. Finalize the gesture once the loop exits
                                applyPointerX(pointerX.floatValue, finished = true)
                            }
                        }
                    }
//                    .pointerInput(valueRange, step, isCosmetic, trackUsablePx, thumbSizePx, compact) {
//                        detectTapGestures(
//                            onPress = { offset ->
//                                // In compact mode, only react if the press starts on the thumb.
//                                if (compact && !isOnThumb(offset.x)) {
//                                    tryAwaitRelease()
//                                    return@detectTapGestures
//                                }
//                                pointerX.floatValue = offset.x
//                                applyPointerX(offset.x)
//                                tryAwaitRelease()
//                                applyPointerX(pointerX.floatValue, finished = true)
//                            }
//                        )
//                    }
//                    .pointerInput(valueRange, step, isCosmetic, trackUsablePx, thumbSizePx, compact) {
//                        detectDragGestures(
//                            onDragStart = { offset ->
//                                // In compact mode, only start dragging if the gesture began on the thumb.
//                                if (compact && !isOnThumb(offset.x)) {
//                                    dragActive = false
//                                    return@detectDragGestures
//                                }
//                                dragActive = true
//                                pointerX.floatValue = offset.x
//                                applyPointerX(offset.x)
//                            },
//                            onDragEnd = {
//                                if (dragActive) applyPointerX(pointerX.floatValue, finished = true)
//                            },
//                            onDragCancel = {
//                                if (dragActive) applyPointerX(pointerX.floatValue, finished = true)
//                            },
//                        ) { change, dragAmount ->
//                            if (!dragActive) return@detectDragGestures
//                            change.consume()
//                            pointerX.floatValue += dragAmount.x
//                            applyPointerX(pointerX.floatValue)
//                        }
//                    }
            )
        }

        if (showRangeLabels) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, start = 4.dp, end = 4.dp)
            ) {
                CJText(
                    text = formatRangeValue(value),
                    color = appTheme.colors.primaryFont,
                )
                Box(Modifier.weight(1f))
                CJText(
                    text = formatRangeValue(valueRange.endInclusive),
                    color = appTheme.colors.secondaryFont,
                )
            }
        }
    }
}

private fun formatRangeValue(v: Float): String {
    return v.formatCommon(2)
}

@Preview
@Composable
internal fun CJSliderPreview() {
    AppThemePreview(isDark = true) {
        val valueState = remember { mutableStateOf(0f) }
        Box(Modifier.fillMaxWidth().height(200.dp).padding(24.dp)) {
            CJNeuSlider(
                modifier = Modifier.fillMaxWidth(),
                value = valueState.value,
                step = 4,
                showRangeLabels = true,
                valueRange = 0f..10f,
                onValueChange = {
                    valueState.value = it
                }
            )
        }
    }
}