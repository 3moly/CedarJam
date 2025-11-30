package com.moly3.cedarjam.pages.page_graph.store

import androidx.compose.ui.geometry.Offset
import com.arkivanov.mvikotlin.core.store.Store
import com.moly3.cedarjam.core.domain.model.ObsidianGraphNode
import com.moly3.cedarjam.pages.page_graph.Intent
import com.moly3.cedarjam.pages.page_graph.State
import com.moly3.cedarjam.core.domain.model.node.GraphSettingsConfig
import com.moly3.dataviz.core.graph.model.GraphViewSettings
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap

internal interface GraphStore : Store<Intent, State, Unit> {

    sealed interface Msg {
        data class SetVelocities(val value: ImmutableMap<String, Offset>) : Msg
        data class SetCoordinates(val value: ImmutableMap<String, Offset>) : Msg
        data class SetGraphViewSettings(val value: GraphViewSettings) : Msg
        data class SetGraphUserPosition(val value: Offset) : Msg
        data class SetIsShowSettings(val value: Boolean) : Msg
        data class SetZoom(val value: Float) : Msg
        data class SetConfig(val value: GraphSettingsConfig) : Msg
        data class SetNodes(val value: List<ObsidianGraphNode>) : Msg
        data class SetConnections(val value: ImmutableMap<String, ImmutableList<String>>) : Msg
    }
}
