package com.moly3.cedarjam.pages.page_home

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.moly3.cedarjam.core.domain.service.WorkspaceSession
import dev.zacsweers.metro.AssistedFactory

@AssistedFactory
interface HomeComponentFactory {
    fun invoke(
        componentContext: ComponentContext,
        storeFactory: StoreFactory,
        workspaceSession: WorkspaceSession,
        openWorkspaceSettings: (Boolean) -> Unit,
    ): HomeComponentImpl
}
