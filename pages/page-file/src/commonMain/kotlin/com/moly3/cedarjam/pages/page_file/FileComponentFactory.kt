package com.moly3.cedarjam.pages.page_file

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.moly3.cedarjam.core.domain.model.navigation.input.FilePageInput
import com.moly3.cedarjam.core.domain.service.WorkspaceSession
import dev.zacsweers.metro.AssistedFactory

@AssistedFactory
interface FileComponentFactory {
    fun invoke(
        componentContext: ComponentContext,
        storeFactory: StoreFactory,
        openMenu: (Boolean) -> Unit,
        data: FilePageInput,
        workspaceSession: WorkspaceSession,
    ): FileComponentImpl
}
