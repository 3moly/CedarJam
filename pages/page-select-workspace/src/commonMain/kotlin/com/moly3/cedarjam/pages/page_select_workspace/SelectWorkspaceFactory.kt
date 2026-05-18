package com.moly3.cedarjam.pages.page_select_workspace

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.moly3.cedarjam.core.domain.model.WorkspaceInput
import dev.zacsweers.metro.AssistedFactory

@AssistedFactory
interface SelectWorkspaceFactory {
    fun invoke(
        componentContext: ComponentContext,
        storeFactory: StoreFactory,
        onSelectWorkspace: (WorkspaceInput) -> Unit
    ): SelectWorkspaceComponent
}