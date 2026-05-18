package com.moly3.cedarjam.ui.pages.tags

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.moly3.cedarjam.core.domain.dialog.DialogTagToTagService
import com.moly3.cedarjam.core.domain.service.WorkspaceSession
import com.moly3.cedarjam.ui.pages.tags.store.TagsStoreFactory
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow

@AssistedInject
class TagsComponentImpl(
    @Assisted private val workspaceSession: WorkspaceSession,
    @Assisted componentContext: ComponentContext,
    @Assisted storeFactory: StoreFactory,
    private val dialogTagToTagService: DialogTagToTagService,
) : TagsComponent,
    ComponentContext by componentContext {

    private val store by lazy {
        TagsStoreFactory(
            storeFactory = storeFactory,
            lifecycle = lifecycle,
            workspaceSession = workspaceSession,
            dialogTagToTagService = dialogTagToTagService,
        ).create(stateKeeper = stateKeeper)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override val state: StateFlow<State> = store.stateFlow
    override fun onIntent(intent: Intent) {
        store.accept(intent)
    }
}
