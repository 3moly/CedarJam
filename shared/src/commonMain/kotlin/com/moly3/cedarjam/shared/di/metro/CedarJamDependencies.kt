package com.moly3.cedarjam.shared.di.metro

import com.moly3.cedarjam.core.data.di.AppGraphServices
import com.moly3.cedarjam.core.data.di.WorkspaceEnvironmentFactory
import com.moly3.cedarjam.core.domain.dialog.DialogCreateWorkspaceService
import com.moly3.cedarjam.core.domain.repository.IAppEnvironment
import com.moly3.cedarjam.core.domain.service.AppContextProvider
import com.moly3.cedarjam.core.domain.service.IFileHasher
import com.moly3.cedarjam.core.domain.usecase.ISyncUseCase
import com.moly3.cedarjam.core.net.IRemoteSyncRepository
import com.moly3.cedarjam.core.storage.ISystemFilesManager
import dev.zacsweers.metro.Inject

@Inject
class CedarJamDependencies(
    override val appContextProvider: AppContextProvider,
    override val fileHasher: IFileHasher,
    override val syncUseCase: ISyncUseCase,
    override val systemFileManager: ISystemFilesManager,
    override val remoteSyncRepository: IRemoteSyncRepository,
    override val appEnvironment: IAppEnvironment,
    override val workspaceFactory: WorkspaceEnvironmentFactory
) : AppGraphServices
