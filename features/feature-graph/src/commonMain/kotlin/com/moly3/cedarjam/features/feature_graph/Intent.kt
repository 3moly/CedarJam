package com.moly3.cedarjam.features.feature_graph

import androidx.compose.ui.geometry.Offset
import com.moly3.cedarjam.core.domain.model.TimeMachine
import com.moly3.cedarjam.core.domain.model.node.ObsidianGraphData
import com.moly3.cedarjam.features.feature_graph.model.GraphPage

sealed interface Intent {
    data class SetCurrentTabPage(val page: GraphPage) : Intent
    data class SetCoordinates(val value: Map<String, Offset>) : Intent
    data object AddTag : Intent
    data class OpenPdfPage(val page: Int) : Intent
    data class RemoveAnnotation(val id: Long) : Intent
    data class SetVelocities(val value: Map<String, Offset>) : Intent
    data class SetZoom(val value: Float) : Intent
    data object Close : Intent
    data object OpenWorkspaceSettings : Intent
    data class SetIsShowContent(val value: Boolean) : Intent
    data class SetIsShowNestedConnections(val value: Boolean) : Intent
    data class OpenNode(val value: ObsidianGraphData) : Intent
    data class OpenTimeMachine(val value: TimeMachine) : Intent
    data class AnnotationsScrollState(val value: Int) : Intent
}