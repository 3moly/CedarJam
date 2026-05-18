package com.moly3.cedarjam.pages.page_graph.ui.internal.settingsPanel

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import com.moly3.cedarjam.pages.page_graph.Intent
import com.moly3.cedarjam.pages.page_graph.ui.internal.SettingsSection
import com.moly3.dataviz.core.graph.model.GraphSettings

@Composable
fun GraphTextSettingsSection(
    settings: GraphSettings,
    onIntent: (Intent) -> Unit
) {
    val updateSettings by rememberUpdatedState(settings)
    SettingsSection(
        title = "Text"
    ) {
        SettingsFloatSlider(
            title = "normal font size",
            value = settings.text.normalFontSizeSp,
            valueRange = 0.1f..30f,
            onValueChange = {
                val text = updateSettings.text.copy(normalFontSizeSp = it)
                onIntent(Intent.SetGraphSettings(updateSettings.copy(text = text)))
            }
        )
        SettingsFloatSlider(
            title = "maxLabelsVisible",
            value = settings.text.maxLabelsVisible.toFloat(),
            valueRange = 0.0f..Int.MAX_VALUE.toFloat(),
            onValueChange = {
                val text = updateSettings.text.copy(maxLabelsVisible = it.toInt())
                onIntent(Intent.SetGraphSettings(updateSettings.copy(text = text)))
            }
        )
    }
}