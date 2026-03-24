package com.moly3.cedarjam.di

import co.touchlab.kermit.CommonWriter
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import com.arkivanov.essenty.statekeeper.StateKeeper
import com.moly3.cedarjam.core.coordinator.di.coordinator
import com.moly3.cedarjam.core.data.AnkiEnvironment
import com.moly3.cedarjam.core.data.AppEnvironment
import com.moly3.cedarjam.core.data.FilesRepository
import com.moly3.cedarjam.core.data.WorkspaceEnvironment
import com.moly3.cedarjam.core.domain.model.AndroidApplicationContext
import com.moly3.cedarjam.core.domain.model.WorkspaceInput
import com.moly3.cedarjam.core.domain.repository.IAnkiEnvironment
import com.moly3.cedarjam.core.domain.repository.IAppEnvironment
import com.moly3.cedarjam.core.domain.repository.IFilesRepository
import com.moly3.cedarjam.core.domain.repository.IWorkspaceEnvironment
import com.moly3.cedarjam.core.domain.service.AlertService
import com.moly3.cedarjam.core.domain.service.AppContextProvider
import com.moly3.cedarjam.core.domain.service.FileManagerService
import com.moly3.cedarjam.core.domain.service.IImageTransform
import com.moly3.cedarjam.core.domain.service.IMessageService
import com.moly3.cedarjam.core.domain.service.IUtilsService
import com.moly3.cedarjam.core.domain.service.WorkspaceSession
import com.moly3.cedarjam.core.domain.usecase.INavigateToFileUseCase
import com.moly3.cedarjam.core.domain.usecase.IOpenNodeDataUseCase
import com.moly3.cedarjam.core.domain.usecase.ISyncUseCase
import com.moly3.cedarjam.core.domain.usecase.NavigateToFileUseCase
import com.moly3.cedarjam.core.domain.usecase.OpenNodeDataUseCase
import com.moly3.cedarjam.core.domain.usecase.SyncUseCase
import com.moly3.cedarjam.core.net.di.net
import com.moly3.cedarjam.core.storage.ISqlStorage
import com.moly3.cedarjam.core.storage.di.db
import com.moly3.cedarjam.core.storage.func.init
import com.moly3.cedarjam.core.ui.service.IJvmBrowserService
import com.moly3.cedarjam.core.ui.service.MacTrackpadGestureService
import com.moly3.cedarjam.navigation.Navigator
import com.moly3.cedarjam.navigation.NavigatorDispatcher
import com.moly3.cedarjam.navigation.NavigatorImpl
import com.moly3.cedarjam.pages.page_workspace.WorkspaceComponentImpl
import com.moly3.cedarjam.repository.getJvmBrowserService
import com.moly3.cedarjam.repository.getUtilsService
import com.moly3.cedarjam.service.ImageTransform
import com.moly3.cedarjam.service.MessageServiceImpl
import com.moly3.core_domain.BuildConfig
import io.github.vinceglb.filekit.FileKit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.serialization.json.Json
import org.koin.core.context.startKoin
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module

fun initApp(
    context: AndroidApplicationContext,
    isRelease: Boolean = BuildConfig.IsRelease,
    isTest: Boolean = false
) {
    if (isRelease) {
        Logger.setLogWriters(listOf())
        Logger.setMinSeverity(Severity.Assert)
    } else {
        Logger.addLogWriter(CommonWriter())
    }
    FileKit.init(context)
    val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    val navigator = NavigatorImpl(scope)
    val koinModule = module {
        single<IAnkiEnvironment> { AnkiEnvironment() }
        single<IImageTransform> { ImageTransform() }
        single<AlertService> { AlertService() }
        factory<INavigateToFileUseCase> { params ->
            val filesManager: FileManagerService = params.get()
            NavigateToFileUseCase(
                fileManagerService = filesManager,
                filesRepository = get()
            )
        }
        single<ISyncUseCase> {
            SyncUseCase(alertService = get(), filesRepo = get())
        }
        factory<IOpenNodeDataUseCase> { params ->
            val filesManager: FileManagerService = params.get()
            val navigateToFileUseCase: INavigateToFileUseCase =
                get { parametersOf(filesManager) }
            OpenNodeDataUseCase(
                navigateToFileUseCase = navigateToFileUseCase
            )
        }
        single<IMessageService> { MessageServiceImpl() }
        single<NavigatorDispatcher> { navigator }
        single<AppContextProvider> { AppContextProvider(context) }
        single<IJvmBrowserService> { getJvmBrowserService() }
        single<MacTrackpadGestureService> { MacTrackpadGestureService() }
        single<Navigator> { navigator }
        factory<CoroutineScope> {
            CoroutineScope(SupervisorJob() + Dispatchers.Main)
        }
        single { Json { isLenient = true; ignoreUnknownKeys = true; explicitNulls = false } }

        single<IFilesRepository> {
            FilesRepository(
                filesStorage = get()
            )
        }
        single<IAppEnvironment> {
            AppEnvironment(
                scope = get(),
                syncNetRepository = get(),
                appStorage = get(),
                systemFilesManager = get(),
                syncService = get(),
                alertService = get(),
                getWorkspaceEnv = {
                    val filesManager =
                        FileManagerService(it, FileManagerService.OpenedFiles())
                    val workspaceInput: WorkspaceInput = WorkspaceInput(
                        name = it.name,
                        serverName = it.serverName
                    )
                    val workspaceEnv: IWorkspaceEnvironment =
                        get { parametersOf(workspaceInput, filesManager) }
                    workspaceEnv
                }
            )
        }
        single<IUtilsService> {
            getUtilsService()
        }
        factory<IWorkspaceEnvironment> { params ->
            val appEnvironment: IAppEnvironment = get()
            val workspaceInput: WorkspaceInput = params.get()
            val filesManager: FileManagerService = params.get()
            val workspace = appEnvironment.getWorkspace(name = workspaceInput.name)

            val filesRepository: IFilesRepository = get()

            WorkspaceEnvironment(
                workspace = workspace,
                filesRepository = filesRepository,
                fileManagerService = filesManager,
                sqlStorageFactory = {
                    get<ISqlStorage> { parametersOf(workspace.absolutePath) }
                },
                syncNetRepository = get(),
            )
        }
        scope<WorkspaceComponentImpl> {
            scoped<WorkspaceSession> { params ->
                try {
                    val appEnvironment: IAppEnvironment = get()
                    val workspaceInput: WorkspaceInput = params.get()
                    if (workspaceInput.name.isEmpty() || workspaceInput.serverName.isEmpty()) {
                        throw NullPointerException("koin WorkspaceSession name is empty")
                    }
                    val stateKeeper: StateKeeper = params.get()
                    val workspace = appEnvironment.getWorkspace(name = workspaceInput.name)

                    val openedFilesKey = "FileManagerServiceState_${workspace.fullpath}"
                    var openedFiles = FileManagerService.OpenedFiles()

                    try {
                        openedFiles = stateKeeper.consume(
                            openedFilesKey,
                            FileManagerService.OpenedFiles.serializer()
                        )!!
                    } catch (exc: Exception) {
                    }

                    val filesManager = FileManagerService(workspace, openedFiles)
                    val workspaceEnv: IWorkspaceEnvironment =
                        get { parametersOf(workspaceInput, filesManager) }
                    stateKeeper.register(
                        key = openedFilesKey,
                        strategy = FileManagerService.OpenedFiles.serializer()
                    ) {
                        filesManager.fileNodeState.value
                    }
                    WorkspaceSession(
                        appEnvironment = get(),
                        scope = get(),
                        workspace = workspaceEnv,
                        fileManagerService = filesManager,
                        filesRepository = get()
                    )
                } catch (exc: Exception) {
                    Logger.d(exc.message ?: "")
                    TODO()
                }
            }
        }
    }
    startKoin {
        modules(
            koinModule,
            net(baseUrl = BuildConfig.SyncServerUrl, token = BuildConfig.SyncServerToken),
            db(isTest = isTest),
            coordinator(),
            initDialogs()
        )
    }
}