package com.moly3.cedarjam.pages.page_graph.ui.internal.settingsPanel

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import com.moly3.cedarjam.pages.page_graph.Intent
import com.moly3.cedarjam.pages.page_graph.ui.internal.SettingsSection
import com.moly3.dataviz.core.graph.model.GraphSettings

@Composable
fun GraphGroupSettingsSection(
    settings: GraphSettings,
    onIntent: (Intent) -> Unit
) {
    val updateSettings by rememberUpdatedState(settings)
    SettingsSection(
        title = "Group"
    ) {
        SettingsFloatSlider(
            title = "cohesion force",
            value = settings.groupSettings.cohesionForce,
            valueRange = 0.1f..30f,
            onValueChange = {
                val group = updateSettings.groupSettings.copy(cohesionForce = it)
                onIntent(Intent.SetGraphSettings(updateSettings.copy(groupSettings = group)))
            }
        )
        SettingsFloatSlider(
            title = "group separation",
            value = settings.groupSettings.groupSeparation,
            valueRange = 0f..50000f,
            onValueChange = {
                val group = updateSettings.groupSettings.copy(groupSeparation = it)
                onIntent(Intent.SetGraphSettings(updateSettings.copy(groupSettings = group)))
            }
        )
        SettingsFloatSlider(
            title = "group separation",
            value = settings.groupSettings.groupSeparationSoftening,
            valueRange = 0f..50000f,
            onValueChange = {
                val group = updateSettings.groupSettings.copy(groupSeparationSoftening = it)
                onIntent(Intent.SetGraphSettings(updateSettings.copy(groupSettings = group)))
            }
        )
        SettingsFloatSlider(
            title = "hull recompute interval ms",
            value = settings.groupSettings.hullRecomputeIntervalMs.toFloat(),
            valueRange = 0.1f..1_000f,
            onValueChange = {
                val group = updateSettings.groupSettings.copy(hullRecomputeIntervalMs = it.toLong())
                onIntent(Intent.SetGraphSettings(updateSettings.copy(groupSettings = group)))
            }
        )
        SettingsFloatSlider(
            title = "hull recompute interval ms",
            value = settings.groupSettings.hullSettledIntervalMs.toFloat(),
            valueRange = 0.1f..1_000f,
            onValueChange = {
                val group = updateSettings.groupSettings.copy(hullSettledIntervalMs = it.toLong())
                onIntent(Intent.SetGraphSettings(updateSettings.copy(groupSettings = group)))
            }
        )
        SettingsFloatSlider(
            title = "hull recompute interval ms",
            value = settings.groupSettings.hullK.toFloat(),
            valueRange = 0.1f..10f,
            onValueChange = {
                val group = updateSettings.groupSettings.copy(hullK = it.toInt())
                onIntent(Intent.SetGraphSettings(updateSettings.copy(groupSettings = group)))
            }
        )
        SettingsFloatSlider(
            title = "hull recompute interval ms",
            value = settings.groupSettings.hullPadding,
            valueRange = 0.1f..200f,
            onValueChange = {
                val group = updateSettings.groupSettings.copy(hullPadding = it)
                onIntent(Intent.SetGraphSettings(updateSettings.copy(groupSettings = group)))
            }
        )
    }
}