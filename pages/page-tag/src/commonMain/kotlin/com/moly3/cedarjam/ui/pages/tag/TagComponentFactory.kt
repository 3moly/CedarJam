package com.moly3.cedarjam.ui.pages.tag

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.moly3.cedarjam.core.domain.model.navigation.input.TagPageInput
import com.moly3.cedarjam.core.domain.service.WorkspaceSession
import dev.zacsweers.metro.AssistedFactory

@AssistedFactory
interface TagComponentFactory {
    fun invoke(
        workspaceSession: WorkspaceSession,
        componentContext: ComponentContext,
        storeFactory: StoreFactory,
        data: TagPageInput,
        openWorkspaceSettings: (Boolean) -> Unit,
    ): TagComponentImpl
}
