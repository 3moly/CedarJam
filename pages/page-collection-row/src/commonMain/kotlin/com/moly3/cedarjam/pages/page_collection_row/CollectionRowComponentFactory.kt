package com.moly3.cedarjam.pages.page_collection_row

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.moly3.cedarjam.core.domain.model.navigation.input.CollectionRowPageInput
import com.moly3.cedarjam.core.domain.service.WorkspaceSession
import dev.zacsweers.metro.AssistedFactory

@AssistedFactory
interface CollectionRowComponentFactory {
    fun invoke(
        workspaceSession: WorkspaceSession,
        componentContext: ComponentContext,
        storeFactory: StoreFactory,
        data: CollectionRowPageInput,
        openWorkspaceSettings: (Boolean) -> Unit,
    ): CollectionRowComponentImpl
}
