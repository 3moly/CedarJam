package com.moly3.cedarjam.pages.page_collection

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.moly3.cedarjam.core.domain.model.navigation.input.CollectionPageInput
import com.moly3.cedarjam.core.domain.service.WorkspaceSession
import dev.zacsweers.metro.AssistedFactory

@AssistedFactory
interface CollectionComponentFactory {
    fun invoke(
        workspaceSession: WorkspaceSession,
        componentContext: ComponentContext,
        storeFactory: StoreFactory,
        data: CollectionPageInput,
        openWorkspaceSettings: (Boolean) -> Unit,
    ): CollectionComponentImpl
}
