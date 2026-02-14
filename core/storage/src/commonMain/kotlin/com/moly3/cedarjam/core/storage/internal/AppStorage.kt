package com.moly3.cedarjam.core.storage.internal

import com.moly3.cedarjam.core.domain.DefaultJson
import com.moly3.cedarjam.core.domain.model.settings.AppSettings
import com.moly3.cedarjam.core.domain.model.Workspace
import com.moly3.cedarjam.core.storage.IAppStorage
import com.russhwolf.settings.Settings

internal class AppStorage(
    private val keyValueSettings: Settings
) : IAppStorage {

    private val appSettingsKey = "cedarjam_app_settings"
    private val appWorkspacesKey = "cedarjam_app_workspaces"

    // ---------------- APP SETTINGS ----------------

    override fun getAppSettings(): AppSettings {
        return try {
            val data = keyValueSettings.getStringOrNull(appSettingsKey)!!
            DefaultJson.decodeFromString(data)
        } catch (_: Exception) {
            AppSettings.defaultSettings
        }
    }

    override fun setAppSettings(settings: AppSettings) {
        val json = DefaultJson.encodeToString(settings)
        keyValueSettings.putString(appSettingsKey, json)
    }

    // ---------------- WORKSPACES ----------------

    private fun getWorkspacesInternal(): MutableList<Workspace> {
        val json = keyValueSettings.getStringOrNull(appWorkspacesKey)
            ?: return mutableListOf()

        return try {
            DefaultJson.decodeFromString<List<Workspace>>(json).toMutableList()
        } catch (_: Exception) {
            mutableListOf()
        }
    }

    private fun saveWorkspaces(workspaces: List<Workspace>) {
        val json = DefaultJson.encodeToString(workspaces)
        keyValueSettings.putString(appWorkspacesKey, json)
    }

    override fun getWorkspaces(): List<Workspace> {
        return getWorkspacesInternal()
    }

    override fun createWorkspace(workspace: Workspace) {
        val list = getWorkspacesInternal()

        // если нужно избегать дублей по fullpath
        if (list.none { it.platformPath == workspace.platformPath }) {
            list.add(workspace)
            saveWorkspaces(list)
        }
    }

    override fun deleteWorkspace(workspace: Workspace) {
        val list = getWorkspacesInternal()
        val updated = list.filterNot { it.platformPath == workspace.platformPath }
        saveWorkspaces(updated)
    }
}