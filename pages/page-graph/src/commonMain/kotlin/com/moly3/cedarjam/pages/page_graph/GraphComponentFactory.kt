package com.moly3.cedarjam.pages.page_graph

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.moly3.cedarjam.core.domain.service.WorkspaceSession
import dev.zacsweers.metro.AssistedFactory

@AssistedFactory
interface GraphComponentFactory {
    fun invoke(
        componentContext: ComponentContext,
        storeFactory: StoreFactory,
        workspaceSession: WorkspaceSession,
        openWorkspaceSettings: (Boolean) -> Unit,
    ): GraphComponentImpl
}
