package com.moly3.cedarjam.ui.pages.tags

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow
import com.moly3.cedarjam.ui.pages.tags.store.TagsStoreFactory
import com.moly3.cedarjam.core.domain.service.WorkspaceSession

class TagsComponentImpl(
    private val workspaceSession: WorkspaceSession,
    componentContext: ComponentContext,
    storeFactory: StoreFactory,
) : TagsComponent,
    ComponentContext by componentContext {

    private val store by lazy {
        TagsStoreFactory(
            storeFactory = storeFactory,
            lifecycle = lifecycle,
            workspaceSession = workspaceSession
        ).create(stateKeeper = stateKeeper)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override val state: StateFlow<State> = store.stateFlow
    override fun onIntent(intent: Intent) {
        store.accept(intent)
    }
}
