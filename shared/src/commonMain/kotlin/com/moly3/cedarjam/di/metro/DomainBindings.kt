package com.moly3.cedarjam.di.metro

import com.moly3.core_domain.BuildConfig
import com.moly3.cedarjam.core.data.AnkiEnvironment
import com.moly3.cedarjam.core.data.AppEnvironment
import com.moly3.cedarjam.core.data.FilesRepository
import com.moly3.cedarjam.core.data.WorkspaceEnvironment
import com.moly3.cedarjam.core.domain.model.WorkspaceInput
import com.moly3.cedarjam.core.domain.SyncServerConfig
import com.moly3.cedarjam.core.domain.repository.IAnkiEnvironment
import com.moly3.cedarjam.core.domain.repository.IAppEnvironment
import com.moly3.cedarjam.core.domain.repository.IFilesRepository
import com.moly3.cedarjam.core.domain.service.AlertService
import com.moly3.cedarjam.core.domain.service.AppContextProvider
import com.moly3.cedarjam.core.domain.service.FileManagerService
import com.moly3.cedarjam.core.domain.service.IImageTransform
import com.moly3.cedarjam.core.domain.service.IUtilsService
import com.moly3.cedarjam.core.domain.usecase.ISyncUseCase
import com.moly3.cedarjam.core.domain.usecase.NavigateToFileUseCase
import com.moly3.cedarjam.core.domain.usecase.NavigateToFileUseCaseFactory
import com.moly3.cedarjam.core.domain.usecase.OpenNodeDataUseCase
import com.moly3.cedarjam.core.domain.usecase.OpenNodeDataUseCaseFactory
import com.moly3.cedarjam.core.domain.usecase.SyncUseCase
import com.moly3.cedarjam.core.net.IRemoteSyncRepository
import com.moly3.cedarjam.navigation.CreateWorkspaceSession
import com.moly3.cedarjam.core.storage.IAppStorage
import com.moly3.cedarjam.core.storage.ISystemFilesManager
import com.moly3.cedarjam.core.storage.func.createSqlStorage
import com.moly3.cedarjam.core.ui.service.IJvmBrowserService
import com.moly3.cedarjam.di.PlatformAndroidContext
import com.moly3.cedarjam.repository.getJvmBrowserService
import com.moly3.cedarjam.repository.getUtilsService
import com.moly3.cedarjam.service.ImageTransform
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

@ContributesTo(AppScope::class)
@BindingContainer
object DomainBindings {

    @SingleIn(AppScope::class)
    @Provides
    fun provideCreateWorkspaceSession(
        factory: WorkspaceSessionFactory,
    ): CreateWorkspaceSession = factory

    @SingleIn(AppScope::class)
    @Provides
    fun provideSyncServerConfig(): SyncServerConfig =
        SyncServerConfig(baseUrl = BuildConfig.SyncServerUrl, token = BuildConfig.SyncServerToken)

    @SingleIn(AppScope::class)
    @Provides
    fun provideAppEnvironmentCoroutineScope(): AppEnvironmentCoroutineScope =
        AppEnvironmentCoroutineScope(CoroutineScope(SupervisorJob() + Dispatchers.Main))

    @SingleIn(AppScope::class)
    @Provides
    fun provideRootCoroutineScope(): RootCoroutineScope =
        RootCoroutineScope(CoroutineScope(SupervisorJob() + Dispatchers.Main))

    @SingleIn(AppScope::class)
    @Provides
    fun provideAppContextProvider(): AppContextProvider =
        AppContextProvider(PlatformAndroidContext.applicationContext)

    @SingleIn(AppScope::class)
    @Provides
    fun provideSqlStorageFactory(
        systemFilesManager: ISystemFilesManager,
        appContextProvider: AppContextProvider,
    ): SqlStorageFactory = SqlStorageFactory { workspacePath ->
        createSqlStorage(
            systemFilesManager = systemFilesManager,
            applicationProvider = appContextProvider,
            workspaceDirectoryPath = workspacePath
        )
    }

    @Provides
    fun provideWorkspaceEnvironmentFactory(
        filesRepository: IFilesRepository,
        syncNetRepository: IRemoteSyncRepository,
        sqlStorageFactory: SqlStorageFactory,
    ): WorkspaceEnvironmentFactory = WorkspaceEnvironmentFactory { appEnvironment, workspaceInput, fileManagerService ->
        val workspace = appEnvironment.getWorkspace(name = workspaceInput.name)
        WorkspaceEnvironment(
            sqlStorageFactory = { sqlStorageFactory(workspace.absolutePath) },
            workspace = workspace,
            filesRepository = filesRepository,
            fileManagerService = fileManagerService,
            syncNetRepository = syncNetRepository,
        )
    }

    @Provides
    fun provideNavigateToFileUseCaseFactory(
        filesRepository: IFilesRepository,
    ): NavigateToFileUseCaseFactory = NavigateToFileUseCaseFactory { fileManagerService ->
        NavigateToFileUseCase(
            fileManagerService = fileManagerService,
            filesRepository = filesRepository,
        )
    }

    @Provides
    fun provideOpenNodeDataUseCaseFactory(
        navigateToFileUseCaseFactory: NavigateToFileUseCaseFactory,
    ): OpenNodeDataUseCaseFactory = OpenNodeDataUseCaseFactory { fileManagerService ->
        OpenNodeDataUseCase(
            navigateToFileUseCase = navigateToFileUseCaseFactory(fileManagerService),
        )
    }

    @SingleIn(AppScope::class)
    @Provides
    fun provideIFilesRepository(
        systemFilesManager: ISystemFilesManager,
    ): IFilesRepository = FilesRepository(filesStorage = systemFilesManager)

    @SingleIn(AppScope::class)
    @Provides
    fun provideISyncUseCase(
        alertService: AlertService,
        filesRepository: IFilesRepository,
    ): ISyncUseCase = SyncUseCase(alertService = alertService, filesRepo = filesRepository)

    @SingleIn(AppScope::class)
    @Provides
    fun provideIAppEnvironment(
        appEnvironmentCoroutineScope: AppEnvironmentCoroutineScope,
        syncNetRepository: IRemoteSyncRepository,
        appStorage: IAppStorage,
        alertService: AlertService,
        systemFilesManager: ISystemFilesManager,
        syncService: ISyncUseCase,
        workspaceEnvironmentFactory: WorkspaceEnvironmentFactory,
    ): IAppEnvironment {
        lateinit var env: IAppEnvironment
        env = AppEnvironment(
            scope = appEnvironmentCoroutineScope.scope,
            syncNetRepository = syncNetRepository,
            appStorage = appStorage,
            alertService = alertService,
            systemFilesManager = systemFilesManager,
            syncService = syncService,
            getWorkspaceEnv = { presentation ->
                val filesManager = FileManagerService(FileManagerService.OpenedFiles())
                val workspaceInput = WorkspaceInput(
                    name = presentation.name,
                    serverName = presentation.serverName
                )
                workspaceEnvironmentFactory(env, workspaceInput, filesManager)
            }
        )
        return env
    }

    @SingleIn(AppScope::class)
    @Provides
    fun provideIAnkiEnvironment(): IAnkiEnvironment = AnkiEnvironment()

    @SingleIn(AppScope::class)
    @Provides
    fun provideIImageTransform(): IImageTransform = ImageTransform()

    @SingleIn(AppScope::class)
    @Provides
    fun provideIJvmBrowserService(): IJvmBrowserService = getJvmBrowserService()

    @SingleIn(AppScope::class)
    @Provides
    fun provideIUtilsService(): IUtilsService = getUtilsService()
}
