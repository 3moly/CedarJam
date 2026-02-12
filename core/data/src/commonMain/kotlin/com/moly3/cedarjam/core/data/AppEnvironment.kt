package com.moly3.cedarjam.core.data

import com.moly3.cedarjam.core.domain.func.getPlatform
import com.moly3.cedarjam.core.domain.func.pathWrapper
import com.moly3.cedarjam.core.domain.io
import com.moly3.cedarjam.core.domain.model.FileTreeNode
import com.moly3.cedarjam.core.domain.model.settings.AppSettings
import com.moly3.cedarjam.core.domain.model.Platform
import com.moly3.cedarjam.core.domain.model.ResultWrapper
import com.moly3.cedarjam.core.domain.model.UIState
import com.moly3.cedarjam.core.domain.model.Workspace
import com.moly3.cedarjam.core.domain.model.WorkspaceInput
import com.moly3.cedarjam.core.domain.model.WorkspacePresentation
import com.moly3.cedarjam.core.domain.model.ensure
import com.moly3.cedarjam.core.domain.model.resultBlock
import com.moly3.cedarjam.core.domain.repository.IAppEnvironment
import com.moly3.cedarjam.core.domain.repository.IWorkspaceEnvironment
import com.moly3.cedarjam.core.domain.repository.getTags
import com.moly3.cedarjam.core.domain.usecase.ISyncUseCase
import com.moly3.cedarjam.core.storage.IAppStorage
import com.moly3.cedarjam.core.storage.ISystemFilesManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AppEnvironment(
    scope: CoroutineScope,
    private val appStorage: IAppStorage,
    private val systemFilesManager: ISystemFilesManager,
    private val syncService: ISyncUseCase,
    private val getWorkspaceEnv: (WorkspacePresentation) -> IWorkspaceEnvironment
) : IAppEnvironment {

    private val _workspacesStateFlow =
        MutableStateFlow<UIState<List<WorkspacePresentation>, Nothing>>(UIState.Loading)
    private val _appSettingsStateFlow = MutableStateFlow(AppSettings.defaultSettings)

    init {
        scope.launch(io) {
            refreshWorkspaces()
        }
    }

    fun Workspace.toPresentation(): WorkspacePresentation {
        val absolutePath =
            systemFilesManager.toAbsoluteAppPath(
                pathWrapper(
                    fullpath
                )
            ).pathString
        return WorkspacePresentation(
            name = name,
            fullpath = fullpath,
            absolutePath = absolutePath,
            serverName = serverName
        )
    }

    override fun getWorkspaces(): List<WorkspacePresentation> {
        val workspaces = appStorage.getWorkspaces()
        return workspaces.map {
            it.toPresentation()
        }
    }

    override fun getWorkspace(name: String): WorkspacePresentation {
        val workspaces = getWorkspaces()
        val workspace = workspaces.firstOrNull { d -> d.name == name }
            ?: WorkspacePresentation(name = name, "", "", "")
        return workspace
    }

    private suspend fun refreshWorkspaces() {
        val workspaces = getWorkspaces()
        _workspacesStateFlow.emit(UIState.Success(workspaces))
    }

    override fun getWorkspacesFlow(): StateFlow<UIState<List<WorkspacePresentation>, Nothing>> {
        return _workspacesStateFlow
    }

    override fun getAppSettingsFlow(): StateFlow<AppSettings> {
        return _appSettingsStateFlow
    }

    override suspend fun setAppSettings(settings: AppSettings) {
        withContext(io) {
            appStorage.setAppSettings(settings)
            _appSettingsStateFlow.emit(settings)
        }
    }

    override suspend fun getLocalWorkspaces(): ResultWrapper<List<FileTreeNode>, String> {
        return withContext(io) {
            resultBlock {
                when (getPlatform()) {
                    Platform.Android,
                    Platform.Ios -> {
                        val absolutePath =
                            systemFilesManager.toAbsoluteAppPath(pathWrapper(""))
                        val nodes =
                            systemFilesManager.getNodes(directoryAbsolutePath = absolutePath.pathString)
                        nodes
                    }

                    Platform.Jvm,
                    Platform.Wasm -> {
                        listOf()
                    }
                }
            }
        }
    }

    override suspend fun createWorkspace(workspace: Workspace): ResultWrapper<WorkspaceInput, String> {
        return withContext(io) {
            resultBlock {
                ensure(workspace.name.isNotEmpty()) { "workspace name is empty" }
                ensure(workspace.fullpath.isNotEmpty()) { "workspace fullpath is empty" }
                val absolutePath =
                    systemFilesManager.toAbsoluteAppPath(
                        pathWrapper(
                            workspace.fullpath
                        )
                    )

                appStorage.createWorkspace(workspace.copy(fullpath = absolutePath.pathString))

                val workspacePresentation = workspace.toPresentation()
                val dir = systemFilesManager.getDirectoryNodeFromFullPath(
                    workspacePath = workspacePresentation.absolutePath,
                    fullPath = workspacePresentation.absolutePath
                )
                val workspaceEnv = getWorkspaceEnv(workspacePresentation)
                syncService.syncronize(workspaceEnv)

                refreshWorkspaces()

                WorkspaceInput(
                    name = workspacePresentation.name,
                    serverName = workspacePresentation.serverName
                )
            }
        }
    }

    override suspend fun deleteWorkspace(workspace: WorkspacePresentation) {
        withContext(io) {
            appStorage.deleteWorkspace(
                Workspace(
                    name = workspace.name,
                    fullpath = workspace.fullpath,
                    serverName = workspace.serverName
                )
            )
            when (getPlatform()) {
                Platform.Android,
                Platform.Ios -> {
                    systemFilesManager.deleteNodeHeavy(workspace.absolutePath)
                }

                Platform.Jvm,
                Platform.Wasm -> {
                }
            }

            refreshWorkspaces()
        }
    }
}