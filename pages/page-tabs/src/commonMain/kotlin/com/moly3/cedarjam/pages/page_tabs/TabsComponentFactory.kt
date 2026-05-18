package com.moly3.cedarjam.pages.page_tabs

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.moly3.cedarjam.core.domain.service.WorkspaceSession
import com.moly3.cedarjam.core.ui.model.PageNameData
import dev.zacsweers.metro.AssistedFactory

@AssistedFactory
interface TabsComponentFactory {
    fun invoke(
        context: ComponentContext,
        storeFactory: StoreFactory,
        workspaceSession: WorkspaceSession,
        openMenu: (Boolean) -> Unit,
        onSelfDestroy: () -> Unit,
        onNewTabs: () -> Unit,
        onFileReveal: (PageNameData.PageType) -> Unit,
        index: Int,
    ): TabsComponentImpl
}
