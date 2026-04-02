package com.moly3.cedarjam.di.metro

import co.touchlab.kermit.Logger
import com.arkivanov.essenty.statekeeper.StateKeeper
import com.moly3.cedarjam.core.domain.model.WorkspaceInput
import com.moly3.cedarjam.core.domain.repository.IAppEnvironment
import com.moly3.cedarjam.core.domain.repository.IFilesRepository
import com.moly3.cedarjam.navigation.CreateWorkspaceSession
import com.moly3.cedarjam.core.domain.service.FileManagerService
import com.moly3.cedarjam.core.domain.service.WorkspaceSession
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

@Inject
class WorkspaceSessionFactory(
    private val appEnvironment: IAppEnvironment,
    private val filesRepository: IFilesRepository,
    private val workspaceEnvironmentFactory: WorkspaceEnvironmentFactory,
) : CreateWorkspaceSession {
    override operator fun invoke(
        workspaceInput: WorkspaceInput,
        stateKeeper: StateKeeper,
    ): WorkspaceSession = create(workspaceInput, stateKeeper)

    fun create(workspaceInput: WorkspaceInput, stateKeeper: StateKeeper): WorkspaceSession {
        if (workspaceInput.name.isEmpty() || workspaceInput.serverName.isEmpty()) {
            throw NullPointerException("WorkspaceSession workspace name or serverName is empty")
        }
        return try {
            val workspace = appEnvironment.getWorkspace(name = workspaceInput.name)
            val openedFilesKey = "FileManagerServiceState_${workspace.fullpath}"
            var openedFiles = FileManagerService.OpenedFiles()
            try {
                openedFiles = stateKeeper.consume(
                    openedFilesKey,
                    FileManagerService.OpenedFiles.serializer()
                )!!
            } catch (_: Exception) {
            }
            val filesManager = FileManagerService(openedFiles)
            val workspaceEnv =
                workspaceEnvironmentFactory(appEnvironment, workspaceInput, filesManager)
            stateKeeper.register(
                key = openedFilesKey,
                strategy = FileManagerService.OpenedFiles.serializer()
            ) {
                filesManager.fileNodeState.value
            }
            WorkspaceSession(
                filesRepository = filesRepository,
                appEnvironment = appEnvironment,
                scope = CoroutineScope(SupervisorJob() + Dispatchers.Main),
                workspace = workspaceEnv,
                fileManagerService = filesManager,
            )
        } catch (exc: Exception) {
            Logger.d(exc.message ?: "")
            TODO()
        }
    }
}
