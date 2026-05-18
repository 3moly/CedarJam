package com.moly3.cedarjam.pages.page_workspace

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.moly3.cedarjam.core.domain.model.WorkspaceInput
import dev.zacsweers.metro.AssistedFactory

@AssistedFactory
interface WorkspaceComponentFactory {
    fun invoke(
        workspaceInput: WorkspaceInput,
        context: ComponentContext,
        storeFactory: StoreFactory,
    ): WorkspaceComponentImpl
}
