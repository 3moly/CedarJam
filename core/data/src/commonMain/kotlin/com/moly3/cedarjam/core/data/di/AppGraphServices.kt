package com.moly3.cedarjam.core.data.di

import com.moly3.cedarjam.core.domain.dialog.DialogCreateWorkspaceService
import com.moly3.cedarjam.core.domain.repository.IAppEnvironment
import com.moly3.cedarjam.core.domain.repository.IWorkspaceEnvironment
import com.moly3.cedarjam.core.domain.service.AppContextProvider
import com.moly3.cedarjam.core.domain.service.IFileHasher
import com.moly3.cedarjam.core.domain.usecase.ISyncUseCase
import com.moly3.cedarjam.core.net.IRemoteSyncRepository
import com.moly3.cedarjam.core.storage.ISystemFilesManager
import com.moly3.cedarjam.core.ui.dialog.DialogRegistry

/**
 * Application service surface for UI/store layers that cannot depend on the `shared` Metro graph
 * (avoids Gradle cycles like shared → page-workspace → page-collection → shared).
 */
interface AppGraphServices {
    val appContextProvider: AppContextProvider
    val fileHasher: IFileHasher
    val syncUseCase: ISyncUseCase
    val systemFileManager: ISystemFilesManager
    val remoteSyncRepository: IRemoteSyncRepository
    val appEnvironment: IAppEnvironment
    val workspaceFactory: WorkspaceEnvironmentFactory
}

object AppGraphServicesLocator {
    lateinit var instance: AppGraphServices
}
