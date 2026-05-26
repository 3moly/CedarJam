package com.moly3.cedarjam.pages.page_workspace.di

import com.moly3.cedarjam.core.domain.service.WorkspaceSession
import com.moly3.cedarjam.pages.page_tabs.TabsComponentFactory
import dev.zacsweers.metro.GraphExtension
import dev.zacsweers.metro.Provides

@GraphExtension(
    scope = WorkspaceScope::class,
    bindingContainers = [WorkspaceDialogBindings::class],
)
interface WorkspaceGraph {
    val tabsComponentFactory: TabsComponentFactory

    @GraphExtension.Factory
    fun interface Factory {
        fun create(
            @Provides workspaceSession: WorkspaceSession
        ): WorkspaceGraph
    }
}

