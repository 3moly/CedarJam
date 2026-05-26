package com.moly3.cedarjam.core.data.di

import com.moly3.cedarjam.core.domain.model.WorkspaceInput
import com.moly3.cedarjam.core.domain.repository.IAppEnvironment
import com.moly3.cedarjam.core.domain.repository.IWorkspaceEnvironment
import com.moly3.cedarjam.core.domain.service.FileManagerService

fun interface WorkspaceEnvironmentFactory {
    operator fun invoke(
        appEnvironment: IAppEnvironment,
        workspaceInput: WorkspaceInput,
        fileManagerService: FileManagerService
    ): IWorkspaceEnvironment
}