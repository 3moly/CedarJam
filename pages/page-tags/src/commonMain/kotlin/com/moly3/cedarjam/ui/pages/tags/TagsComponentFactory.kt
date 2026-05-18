package com.moly3.cedarjam.ui.pages.tags

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.moly3.cedarjam.core.domain.service.WorkspaceSession
import dev.zacsweers.metro.AssistedFactory

@AssistedFactory
interface TagsComponentFactory {
    fun invoke(
        workspaceSession: WorkspaceSession,
        componentContext: ComponentContext,
        storeFactory: StoreFactory,
    ): TagsComponentImpl
}
