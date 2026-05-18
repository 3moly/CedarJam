package com.moly3.cedarjam.pages.page_graph

import androidx.compose.runtime.Immutable
import com.moly3.cedarjam.core.domain.model.node.ObsidianGraphData
import com.moly3.dataviz.core.graph.engine.IGraphEngine
import kotlinx.coroutines.flow.StateFlow

@Immutable
interface GraphComponent {
    val engine: IGraphEngine<String, ObsidianGraphData>
    val state: StateFlow<State>
    fun onIntent(intent: Intent)
}