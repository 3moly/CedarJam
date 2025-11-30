package com.moly3.cedarjam.pages.page_graph

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow
import com.moly3.cedarjam.pages.page_graph.store.GraphStoreFactory
import com.moly3.cedarjam.core.domain.service.WorkspaceSession

class GraphComponentImpl(
    componentContext: ComponentContext,
    storeFactory: StoreFactory,
    workspaceSession: WorkspaceSession
) : GraphComponent,
    ComponentContext by componentContext {

    private val store by lazy {
        GraphStoreFactory(
            storeFactory = storeFactory,
            lifecycle = lifecycle,
            workspaceSession = workspaceSession
        ).create(componentContext.stateKeeper)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override val state: StateFlow<State> = store.stateFlow
    override fun onIntent(intent: Intent) {
        store.accept(intent)
    }
}
