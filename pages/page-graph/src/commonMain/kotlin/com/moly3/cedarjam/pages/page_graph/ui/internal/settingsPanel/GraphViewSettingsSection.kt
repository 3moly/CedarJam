package com.moly3.cedarjam.pages.page_graph.ui.internal.settingsPanel

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import com.moly3.cedarjam.pages.page_graph.Intent
import com.moly3.cedarjam.pages.page_graph.State
import com.moly3.cedarjam.pages.page_graph.ui.internal.SettingsSection
import com.moly3.dataviz.core.graph.model.GraphSettings

@Composable
fun GraphViewSettingsSection(
    zoom: Float,
    settings: GraphSettings,
    onIntent: (Intent) -> Unit
) {
    val updateSettings by rememberUpdatedState(settings)
    SettingsSection(
        title = "View"
    ) {
        SettingsFloatSlider(
            title = "center force",
            value = settings.view.centerForce,
            valueRange = 0.0f..1f,
            onValueChange = {
                val view = updateSettings.view.copy(centerForce = it)
                onIntent(Intent.SetGraphSettings(updateSettings.copy(view = view)))
            }
        )
        SettingsFloatSlider(
            title = "link force",
            value = updateSettings.view.linkForce,
            valueRange = 0.0f..100f,
            onValueChange = {
                val view = updateSettings.view.copy(linkForce = it)
                onIntent(Intent.SetGraphSettings(updateSettings.copy(view = view)))
            }
        )
        SettingsFloatSlider(
            title = "link distance",
            value = updateSettings.view.linkDistance,
            valueRange = 0.0f..100f,
            onValueChange = {
                val view = updateSettings.view.copy(linkDistance = it)
                onIntent(Intent.SetGraphSettings(updateSettings.copy(view = view)))
            }
        )
        SettingsFloatSlider(
            title = "repel force",
            value = updateSettings.view.repelForce,
            valueRange = 0.0f..50_000f,
            onValueChange = {
                val view = updateSettings.view.copy(repelForce = it)
                onIntent(Intent.SetGraphSettings(updateSettings.copy(view = view)))
            }
        )
        SettingsFloatSlider(
            title = "circle size",
            value = updateSettings.view.circleSize,
            valueRange = 0.1f..10f,
            onValueChange = {
                val view = updateSettings.view.copy(circleSize = it)
                onIntent(Intent.SetGraphSettings(updateSettings.copy(view = view)))
            }
        )
        SettingsFloatSlider(
            title = "circle size multiplier",
            value = updateSettings.view.circleSizeMultiplier ?: 0f,
            valueRange = 0f..2f,
            onValueChange = {
                val view =
                    updateSettings.view.copy(circleSizeMultiplier = if (it == 0f) null else it)
                onIntent(Intent.SetGraphSettings(updateSettings.copy(view = view)))
            }
        )
        SettingsFloatSlider(
            title = "circle quality",
            value = updateSettings.view.circleQuality,
            valueRange = 0.01f..1f,
            onValueChange = {
                val view =
                    updateSettings.view.copy(circleQuality = it)
                onIntent(Intent.SetGraphSettings(updateSettings.copy(view = view)))
            }
        )
        SettingsFloatSlider(
            title = "circle border width",
            value = updateSettings.view.circleBorderWidth,
            valueRange = 0.0f..1f,
            onValueChange = {
                val view =
                    updateSettings.view.copy(circleBorderWidth = it)
                onIntent(Intent.SetGraphSettings(updateSettings.copy(view = view)))
            }
        )
        SettingsFloatSlider(
            title = "damping factor",
            value = updateSettings.view.dampingFactor,
            valueRange = 0.0f..1f,
            onValueChange = {
                val view = updateSettings.view.copy(dampingFactor = it)
                onIntent(Intent.SetGraphSettings(updateSettings.copy(view = view)))
            }
        )
        SettingsFloatSlider(
            title = "minMutualConnectionsForClustering",
            value = updateSettings.view.minMutualConnectionsForClustering.toFloat(),
            valueRange = 0.0f..20f,
            onValueChange = {
                val view = updateSettings.view.copy(minMutualConnectionsForClustering = it.toInt())
                onIntent(Intent.SetGraphSettings(updateSettings.copy(view = view)))
            }
        )
        SettingsFloatSlider(
            title = "max force",
            value = updateSettings.view.maxForce,
            valueRange = 0.0f..20f,
            onValueChange = {
                val view = updateSettings.view.copy(maxForce = it)
                onIntent(Intent.SetGraphSettings(updateSettings.copy(view = view)))
            }
        )
        SettingsFloatSlider(
            title = "maxTextsAtCenterVisible",
            value = updateSettings.view.longDistanceLinkMultiplier,
            valueRange = 0.0f..4f,
            onValueChange = {
                val view = updateSettings.view.copy(longDistanceLinkMultiplier = it)
                onIntent(Intent.SetGraphSettings(updateSettings.copy(view = view)))
            }
        )
        SettingsFloatSlider(
            title = "maxTextsAtCenterVisible",
            value = updateSettings.view.maxTextsAtCenterVisible.toFloat(),
            valueRange = 0.0f..Int.MAX_VALUE.toFloat(),
            onValueChange = {
                val view = updateSettings.view.copy(maxTextsAtCenterVisible = it.toInt())
                onIntent(Intent.SetGraphSettings(updateSettings.copy(view = view)))
            }
        )
        SettingsFloatSlider(
            title = "maxConnectionsForFullProcessing",
            value = updateSettings.view.maxConnectionsForFullProcessing.toFloat(),
            valueRange = 0.0f..1_000f,
            onValueChange = {
                val view = updateSettings.view.copy(maxConnectionsForFullProcessing = it.toInt())
                onIntent(Intent.SetGraphSettings(updateSettings.copy(view = view)))
            }
        )
        SettingsFloatSlider(
            title = "spatialOptimizationThreshold",
            value = updateSettings.view.spatialOptimizationThreshold.toFloat(),
            valueRange = 0.0f..1_000f,
            onValueChange = {
                val view = updateSettings.view.copy(spatialOptimizationThreshold = it.toInt())
                onIntent(Intent.SetGraphSettings(updateSettings.copy(view = view)))
            }
        )
        SettingsFloatSlider(
            title = "hubExpansionExponent",
            value = updateSettings.view.hubExpansionExponent,
            valueRange = 0.0f..1f,
            onValueChange = {
                val view = updateSettings.view.copy(hubExpansionExponent = it)
                onIntent(Intent.SetGraphSettings(updateSettings.copy(view = view)))
            }
        )
    }
}