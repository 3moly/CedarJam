package com.moly3.cedarjam.pages.page_graph.store

import androidx.compose.ui.geometry.Offset
import com.arkivanov.mvikotlin.core.store.Store
import com.moly3.cedarjam.core.domain.model.ObsidianGraphNode
import com.moly3.cedarjam.core.domain.model.config.GraphPartConfig
import com.moly3.cedarjam.pages.page_graph.Intent
import com.moly3.cedarjam.pages.page_graph.State
import com.moly3.cedarjam.core.domain.model.node.ObsidianGraphData
import com.moly3.dataviz.core.graph.engine.IGraphEngine
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap

interface GraphStore : Store<Intent, State, Unit> {

    val engine: IGraphEngine<String, ObsidianGraphData>

    sealed interface Msg {
        data class SetCoordinates(val value: ImmutableMap<String, Offset>) : Msg
        data class SetGraphUserPosition(val value: Offset) : Msg
        data class SetIsShowSettings(val value: Boolean) : Msg
        data class SetZoom(val value: Float) : Msg
        data class SetPartConfig(val value: GraphPartConfig) : Msg
        data class SetNodes(val value: List<ObsidianGraphNode>) : Msg
        data class SetConnections(val value: ImmutableMap<String, ImmutableList<String>>) : Msg
    }
}
