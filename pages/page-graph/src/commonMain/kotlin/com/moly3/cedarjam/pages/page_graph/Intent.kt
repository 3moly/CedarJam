package com.moly3.cedarjam.pages.page_graph

import androidx.compose.ui.geometry.Offset
import com.moly3.cedarjam.core.domain.model.node.GraphSettingsConfig
import com.moly3.cedarjam.core.domain.model.node.ObsidianGraphData
import com.moly3.dataviz.core.graph.model.GraphViewSettings

sealed interface Intent {
    data class SetIsMouseCaptured(val value: Boolean) : Intent
    data class SetCoordinates(val value: Map<String, Offset>) : Intent
    data class SetVelocities(val value: Map<String, Offset>) : Intent
    data class OpenNodeData(val value: ObsidianGraphData) : Intent
    data class SetConfig(val value: GraphSettingsConfig) : Intent
    data class SetZoom(val value: Float) : Intent
    data class SetGraphUserPosition(val value: Offset) : Intent
    data class SetGraphViewSettings(val value: GraphViewSettings) : Intent
    data class SetIsShowSettings(val value: Boolean) : Intent
}