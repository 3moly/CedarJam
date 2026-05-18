package com.moly3.cedarjam.pages.page_graph.ui.internal.settingsPanel

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import com.moly3.cedarjam.core.ui.uikit.CJSlider
import com.moly3.cedarjam.core.ui.uikit.CJText

@Composable
fun SettingsFloatSlider(
    title: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    onValueChange: (Float) -> Unit
) {
    Column {
        CJText(
            text = title,
            modifier = Modifier,
            fontSize = 10.sp
        )
        CJSlider(
            modifier = Modifier.fillMaxWidth(),
            value = value,
            valueRange = valueRange,
            isStepCosmetic = true,
            step = 5,
            onValueChange = onValueChange
        )
    }
}