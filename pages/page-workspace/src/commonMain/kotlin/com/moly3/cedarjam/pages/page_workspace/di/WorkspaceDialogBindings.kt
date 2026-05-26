package com.moly3.cedarjam.pages.page_workspace.di

import com.moly3.cedarjam.core.domain.dialog.DialogDeleteService
import com.moly3.cedarjam.core.domain.dialog.DialogGraphConfigsService
import com.moly3.cedarjam.core.domain.dialog.IDialogRegister
import com.moly3.cedarjam.core.domain.service.WorkspaceSession
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

@BindingContainer
object WorkspaceDialogBindings {
    @Provides
    @SingleIn(WorkspaceScope::class)
    fun provideDialogGraphConfigsService(
        register: IDialogRegister,
        workspaceSession: WorkspaceSession,
        dialogDeleteService: DialogDeleteService
    ): DialogGraphConfigsService =
        DialogGraphConfigsService(
            register = register,
            workspaceSession = workspaceSession,
            dialogDeleteService = dialogDeleteService
        )

}