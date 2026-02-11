package com.moly3.cedarjam.core.domain.repository

import com.moly3.cedarjam.core.domain.model.FileTreeNode
import com.moly3.cedarjam.core.domain.model.settings.AppSettings
import com.moly3.cedarjam.core.domain.model.ResultWrapper
import com.moly3.cedarjam.core.domain.model.UIState
import com.moly3.cedarjam.core.domain.model.Workspace
import com.moly3.cedarjam.core.domain.model.WorkspaceInput
import com.moly3.cedarjam.core.domain.model.WorkspacePresentation
import kotlinx.coroutines.flow.StateFlow

interface IAppEnvironment {
    fun getWorkspaces(): List<WorkspacePresentation>
    fun getWorkspace(name: String): WorkspacePresentation
    fun getWorkspacesFlow(): StateFlow<UIState<List<WorkspacePresentation>, Nothing>>
    fun getAppSettingsFlow(): StateFlow<AppSettings>

    suspend fun setAppSettings(settings: AppSettings)
    suspend fun getLocalWorkspaces(): ResultWrapper<List<FileTreeNode>, String>
    suspend fun createWorkspace(workspace: Workspace): ResultWrapper<WorkspaceInput, String>
    suspend fun deleteWorkspace(workspace: WorkspacePresentation)
}