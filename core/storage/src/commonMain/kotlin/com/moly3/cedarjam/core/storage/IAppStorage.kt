package com.moly3.cedarjam.core.storage

import com.moly3.cedarjam.core.domain.model.settings.AppSettings
import com.moly3.cedarjam.core.domain.model.Workspace

interface IAppStorage {
    fun getAppSettings(): AppSettings
    fun setAppSettings(settings: AppSettings)
    fun createWorkspace(workspace: Workspace)
    fun deleteWorkspace(workspace: Workspace)
    fun getWorkspaces(): List<Workspace>
}