package com.moly3.cedarjam.pages.page_graph

import androidx.compose.ui.geometry.Offset
import com.moly3.cedarjam.core.domain.model.config.GroupLogic
import com.moly3.cedarjam.core.domain.model.node.GraphFilter
import com.moly3.cedarjam.core.domain.model.node.ObsidianGraphData
import com.moly3.dataviz.core.graph.model.GraphSettings

sealed interface Intent {
    data class SetIsMouseCaptured(val value: Boolean) : Intent
    data class SetCoordinates(val value: Map<String, Offset>) : Intent
    data class OpenNodeData(val value: ObsidianGraphData) : Intent
    data class SetZoom(val isGesture: Boolean, val value: Float) : Intent
    data class SetGraphUserPosition(val value: Offset) : Intent
    data class SetIsShowSettings(val value: Boolean) : Intent
    data object OpenWorkspaceSettings : Intent
    data class SetGraphSettings(val config: GraphSettings) : Intent
    data class SetGroups(val value: List<GroupLogic>) : Intent
    data class SetFilter(val value: GraphFilter) : Intent
}