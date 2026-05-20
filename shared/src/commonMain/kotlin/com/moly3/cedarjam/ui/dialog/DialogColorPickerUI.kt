package com.moly3.cedarjam.ui.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.moly3.cedarjam.core.domain.dialog.DialogColorPickerService
import com.moly3.cedarjam.core.ui.compositions.LocalAppTheme
import com.moly3.cedarjam.core.ui.shader.ColorPickerShader
import com.moly3.cedarjam.core.ui.uikit.CJButton
import com.moly3.cedarjam.core.ui.uikit.CJButton2
import com.moly3.cedarjam.core.ui.uikit.CJSlider
import com.moly3.cedarjam.core.ui.uikit.CJText
import com.moly3.shaders.shaderBackground
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun DialogColorPickerUI(dialog: DialogColorPickerService, data: Color) {
    val scope = rememberCoroutineScope()

    var normPos by remember { mutableStateOf(Offset(0.5f, 0.5f)) } // hue, saturation
    var brightness by remember { mutableStateOf(1f) }
    var boxSize by remember { mutableStateOf(IntSize.Zero) }

    val shader = remember { ColorPickerShader() }
    LaunchedEffect(brightness) { shader.setValue(brightness) }

    val selectedColor = remember(normPos, brightness) {
        ColorPickerShader.colorAt(normPos.x, normPos.y, brightness)
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth().padding(16.dp)
    ) {

        CJText(
            modifier = Modifier.fillMaxWidth(),
            text = "select color",
            textAlign = TextAlign.Center
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(12.dp))
                .shaderBackground(shader)
                .onSizeChanged { boxSize = it }
                .pointerInput(boxSize) {
                    if (boxSize.width == 0 || boxSize.height == 0) return@pointerInput
                    detectTapGestures { offset ->
                        normPos = Offset(
                            (offset.x / boxSize.width).coerceIn(0f, 1f),
                            (offset.y / boxSize.height).coerceIn(0f, 1f)
                        )
                    }
                }
                .pointerInput(boxSize) {
                    if (boxSize.width == 0 || boxSize.height == 0) return@pointerInput
                    detectDragGestures { change, _ ->
                        change.consume()
                        normPos = Offset(
                            (change.position.x / boxSize.width).coerceIn(0f, 1f),
                            (change.position.y / boxSize.height).coerceIn(0f, 1f)
                        )
                    }
                }
        ) {
            val handleSize = 28.dp
            val handlePx = with(LocalDensity.current) { handleSize.toPx() }
            val ringColor = if (selectedColor.luminance() > 0.5f) Color.Black else Color.White

            Box(
                modifier = Modifier
                    .size(handleSize)
                    .graphicsLayer {
                        translationX = normPos.x * boxSize.width - handlePx / 2f
                        translationY = normPos.y * boxSize.height - handlePx / 2f
                    }
                    .clip(CircleShape)
                    .background(ringColor)
                    .padding(3.dp)
                    .clip(CircleShape)
                    .background(selectedColor)
            )
        }

        // Brightness
        CJText("brightness")
        CJSlider(
            modifier = Modifier.fillMaxWidth(),
            value = brightness,
            valueRange = 0f..1f,
            onValueChange = { brightness = it }
        )

        // Preview + confirm
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(selectedColor)
            )
            CJText("#${selectedColor.toHex()}")
            Spacer(Modifier.weight(1f))
            CJButton(
                modifier = Modifier
                    .height(32.dp),
                text = "Select",
                backColor = LocalAppTheme.current.primaryColor,
                onClick = {
                    scope.launch { dialog.setResult(selectedColor) }
                })
        }
    }
}

private fun Color.toHex(): String {
    val r = (red * 255).roundToInt().coerceIn(0, 255)
    val g = (green * 255).roundToInt().coerceIn(0, 255)
    val b = (blue * 255).roundToInt().coerceIn(0, 255)
    return r.toHex2() + g.toHex2() + b.toHex2()
}

private fun Int.toHex2(): String {
    val hex = toString(16).uppercase()
    return if (hex.length == 1) "0$hex" else hex
}