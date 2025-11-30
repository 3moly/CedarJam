package com.moly3.cedarjam.pages.page_collection

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.moly3.cedarjam.core.domain.model.navigation.input.CollectionPageInput
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow
import com.moly3.cedarjam.pages.page_collection.store.CollectionStoreFactory
import com.moly3.cedarjam.core.domain.model.PageNameData
import com.moly3.cedarjam.core.domain.service.WorkspaceSession

class CollectionComponentImpl(
    workspaceSession: WorkspaceSession,
    componentContext: ComponentContext,
    storeFactory: StoreFactory,
    private val data: CollectionPageInput
) : CollectionComponent,
    ComponentContext by componentContext {

    private val store by lazy {
        CollectionStoreFactory(
            storeFactory = storeFactory,
            lifecycle = lifecycle,
            pageData = data,
            workspaceSession = workspaceSession
        ).create()
    }

    override val nameFlow: StateFlow<PageNameData?> = store.nameStateFlow

    override val labels = store.labels

    @OptIn(ExperimentalCoroutinesApi::class)
    override val state: StateFlow<State> = store.stateFlow
    override fun onIntent(intent: Intent) {
        store.accept(intent)
    }
}
