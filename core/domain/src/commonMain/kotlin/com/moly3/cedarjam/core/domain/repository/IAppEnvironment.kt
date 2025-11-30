package com.moly3.cedarjam.core.domain.repository

import com.moly3.cedarjam.core.domain.model.AppSettings
import com.moly3.cedarjam.core.domain.model.ResultWrapper
import com.moly3.cedarjam.core.domain.model.UIState
import com.moly3.cedarjam.core.domain.model.Workspace
import com.moly3.cedarjam.core.domain.model.WorkspacePresentation
import kotlinx.coroutines.flow.StateFlow

interface IAppEnvironment {
    fun getWorkspaces(): List<WorkspacePresentation>
    fun getWorkspace(name: String): WorkspacePresentation
    fun getWorkspacesFlow(): StateFlow<UIState<List<WorkspacePresentation>, Nothing>>
    fun getAppSettingsFlow(): StateFlow<AppSettings>

    suspend fun setAppSettings(settings: AppSettings)
    suspend fun createWorkspace(workspace: Workspace): ResultWrapper<Unit, String>
    suspend fun deleteWorkspace(workspace: WorkspacePresentation)
}