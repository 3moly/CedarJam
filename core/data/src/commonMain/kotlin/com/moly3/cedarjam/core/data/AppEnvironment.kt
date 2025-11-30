package com.moly3.cedarjam.core.data

import com.moly3.cedarjam.core.domain.io
import com.moly3.cedarjam.core.domain.model.AppColorsData
import com.moly3.cedarjam.core.domain.model.AppSettings
import com.moly3.cedarjam.core.domain.model.ColorsType
import com.moly3.cedarjam.core.domain.model.ResultWrapper
import com.moly3.cedarjam.core.domain.model.UIState
import com.moly3.cedarjam.core.domain.model.Workspace
import com.moly3.cedarjam.core.domain.model.WorkspacePresentation
import com.moly3.cedarjam.core.domain.model.ensure
import com.moly3.cedarjam.core.domain.model.resultBlock
import com.moly3.cedarjam.core.domain.repository.IAppEnvironment
import com.moly3.cedarjam.core.storage.IAppStorage
import com.moly3.cedarjam.core.storage.ISystemFilesManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AppEnvironment(
    scope: CoroutineScope,
    private val appStorage: IAppStorage,
    private val systemFilesManager: ISystemFilesManager
) : IAppEnvironment {

    private val _workspacesStateFlow =
        MutableStateFlow<UIState<List<WorkspacePresentation>, Nothing>>(UIState.Loading)
    private val _appSettingsStateFlow = MutableStateFlow(AppSettings.Companion.defaultSettings)

    init {
        scope.launch(Dispatchers.Main.immediate) {
            refreshWorkspaces()
            val appSettings = appStorage.getAppSettings()
            val colorsType = if (appSettings.theme.colorsType == ColorsType.Unspecified) {
                ColorsType.Dark
            } else {
                appSettings.theme.colorsType
            }
            val colors = if (appSettings.theme.colorsType == ColorsType.Unspecified) {
                AppColorsData.Companion.Dark
            } else {
                appSettings.theme.colors
            }
            val newAppSettings = appSettings.copy(
                theme = appSettings.theme.copy(
                    colorsType = colorsType,
                    colors = colors
                )
            )
            setAppSettings(newAppSettings)
            _appSettingsStateFlow.emit(newAppSettings)
        }
    }

    override fun getWorkspaces(): List<WorkspacePresentation> {
        val workspaces = appStorage.getWorkspaces()
        return workspaces.map {
            val absolutePath =
                systemFilesManager.toAbsoluteAppPath(
                    _root_ide_package_.com.moly3.cedarjam.core.domain.func.pathWrapper(
                        it.fullpath
                    )
                ).pathString
            WorkspacePresentation(
                name = it.name,
                fullpath = it.fullpath,
                absolutePath = absolutePath
            )
        }
    }

    override fun getWorkspace(name: String): WorkspacePresentation {
        val workspaces = getWorkspaces()
        val workspace = workspaces.firstOrNull { d -> d.name == name }
            ?: WorkspacePresentation(name = name, "", "")
        return workspace
    }

    private suspend fun refreshWorkspaces() {
        _workspacesStateFlow.emit(UIState.Success(getWorkspaces()))
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

    override suspend fun createWorkspace(workspace: Workspace): ResultWrapper<Unit, String> {
        return withContext(io) {
            resultBlock {
                ensure(workspace.name.isNotEmpty()) { "workspace name is empty" }
                ensure(workspace.fullpath.isNotEmpty()) { "workspace fullpath is empty" }
                val absolutePath =
                    systemFilesManager.toRelativeAppPath(
                        _root_ide_package_.com.moly3.cedarjam.core.domain.func.pathWrapper(
                            workspace.fullpath
                        )
                    )

                appStorage.createWorkspace(workspace.copy(fullpath = absolutePath.pathString))
                refreshWorkspaces()
            }
        }
    }

    override suspend fun deleteWorkspace(workspace: WorkspacePresentation) {
        withContext(io) {

            appStorage.deleteWorkspace(
                Workspace(
                    name = workspace.name,
                    fullpath = workspace.fullpath
                )
            )
            refreshWorkspaces()
        }
    }
}