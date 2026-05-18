package com.moly3.cedarjam.pages.page_tab

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.moly3.cedarjam.core.domain.service.WorkspaceSession
import dev.zacsweers.metro.AssistedFactory

@AssistedFactory
interface TabComponentFactory {
    fun invoke(
        workspaceSession: WorkspaceSession,
        context: ComponentContext,
        storeFactory: StoreFactory,
        openMenu: (Boolean) -> Unit,
        tabIndex: Int,
    ): TabComponentImpl
}
