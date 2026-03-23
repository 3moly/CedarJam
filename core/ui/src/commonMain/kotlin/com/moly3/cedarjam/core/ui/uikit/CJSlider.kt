package com.moly3.cedarjam.core.ui.uikit

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.moly3.cedarjam.core.ui.compositions.LocalAppTheme

@Composable
fun CJSlider(
    modifier: Modifier = Modifier,
    value: Float,
    step: Int = 0,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    onValueChangeFinished: (() -> Unit)? = null,
) {
    val appTheme = LocalAppTheme.current
    Slider(
        modifier = modifier,
        value = value,
        onValueChange = onValueChange,
        valueRange = valueRange,
        steps = step,
        onValueChangeFinished = onValueChangeFinished,
        colors = SliderDefaults.colors(
            thumbColor = appTheme.primaryColor,
            activeTrackColor = appTheme.colors.primaryFont,
            inactiveTrackColor = appTheme.colors.secondaryFont,
        )
    )
}

@Preview
@Composable
internal fun CJSliderPreview(){
    Box(Modifier.fillMaxSize().background(Color.Yellow))
}