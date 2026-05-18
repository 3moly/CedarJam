package com.moly3.cedarjam.features.feature_graph

import androidx.compose.runtime.Stable
import com.moly3.cedarjam.core.domain.model.node.ObsidianGraphData
import com.moly3.dataviz.core.graph.engine.IGraphEngine
import com.moly3.dataviz.core.graph.engine.impl.ultra.UltraFastEngine
import kotlinx.coroutines.flow.StateFlow

@Stable
interface IDialogGraphComponent {
    val engine: IGraphEngine<String, ObsidianGraphData>
    val state: StateFlow<State>
    fun onIntent(intent: Intent)

}