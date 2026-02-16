package com.moly3.cedarjam.pages.page_home

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.moly3.cedarjam.pages.page_home.store.HomeStoreFactory
import com.moly3.cedarjam.core.domain.service.WorkspaceSession
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow

class HomeComponentImpl(
    componentContext: ComponentContext,
    storeFactory: StoreFactory,
    private val workspaceSession: WorkspaceSession,
    private val openWorkspaceSettings: (Boolean) -> Unit
) : HomeComponent,
    ComponentContext by componentContext {

    private val store by lazy {
        HomeStoreFactory(
            workspaceSession = workspaceSession,
            storeFactory = storeFactory,
            openWorkspaceSettings = openWorkspaceSettings
        ).create(stateKeeper = stateKeeper, lifecycle = lifecycle)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override val state: StateFlow<State> = store.stateFlow
    override fun onIntent(intent: Intent) {
        store.accept(intent)
    }
}
