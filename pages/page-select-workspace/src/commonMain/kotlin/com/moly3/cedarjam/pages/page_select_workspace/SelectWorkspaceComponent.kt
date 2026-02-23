package com.moly3.cedarjam.pages.page_select_workspace

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.moly3.cedarjam.core.domain.model.WorkspaceInput
import com.moly3.cedarjam.pages.page_select_workspace.store.SelectWorkspaceStoreFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow
import org.koin.core.component.KoinComponent

class SelectWorkspaceComponent(
    componentContext: ComponentContext,
    storeFactory: StoreFactory,
    private val onSelectWorkspace: (WorkspaceInput) -> Unit
) : ISelectWorkspaceComponent,
    ComponentContext by componentContext,
    KoinComponent {

    private val store by lazy {
        SelectWorkspaceStoreFactory(
            storeFactory = storeFactory,
            lifecycle = lifecycle,
            onSelectWorkspace = onSelectWorkspace
        ).create()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override val state: StateFlow<State> = store.stateFlow

    override fun onIntent(intent: Intent) {
        store.accept(intent)
    }
}
