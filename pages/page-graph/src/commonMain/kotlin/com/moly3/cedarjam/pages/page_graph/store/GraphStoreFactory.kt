package com.moly3.cedarjam.pages.page_graph.store

import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.statekeeper.StateKeeper
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.moly3.cedarjam.core.domain.model.node.ObsidianGraphData
import com.moly3.cedarjam.core.domain.service.WorkspaceSession
import com.moly3.dataviz.core.graph.engine.IGraphEngine
import dev.zacsweers.metro.AssistedFactory

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