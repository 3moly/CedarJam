package com.moly3.cedarjam.pages.page_graph.store

import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.statekeeper.StateKeeper
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.moly3.cedarjam.core.domain.model.node.ObsidianGraphData
import com.moly3.cedarjam.core.domain.service.WorkspaceSession
import com.moly3.dataviz.core.graph.engine.IGraphEngine
import com.moly3.dataviz.core.graph.model.GraphSettings
import dev.zacsweers.metro.AssistedFactory
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class GraphSaveConfig(
    @SerialName("is_pinned")
    val isPinned: Boolean,
    @SerialName("name")
    val name: String,
    @SerialName("config")
    val config: GraphSettings = GraphSettings.Default
)

@Serializable
data class GraphSaveConfigs(
    @SerialName("configs")
    val configs: List<GraphSaveConfig>,
)

@AssistedFactory
interface GraphStoreFactory {
    fun create(
        storeFactory: StoreFactory,
        lifecycle: Lifecycle,
        workspaceSession: WorkspaceSession,
        openWorkspaceSettings: (Boolean) -> Unit,
        stateKeeper: StateKeeper,
        engine: IGraphEngine<String, ObsidianGraphData>
    ): GraphStoreImpl
}