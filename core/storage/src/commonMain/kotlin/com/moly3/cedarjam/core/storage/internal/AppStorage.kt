package com.moly3.cedarjam.core.storage.internal

import com.moly3.cedarjam.core.domain.DefaultJson
import com.moly3.cedarjam.core.domain.model.AppSettings
import com.moly3.cedarjam.core.domain.model.Workspace
import com.moly3.cedarjam.core.storage.IAppStorage
import com.russhwolf.settings.Settings
import com.russhwolf.settings.contains
import kotlinx.serialization.json.Json

internal class AppStorage(
    private val keyValueSettings: Settings
) : IAppStorage {

    private val appSettingsKey = "cedarjam_app_settings"

    private fun getWorkspaces2(): Map<String, Workspace> {
        val mutMap = mutableMapOf<String, Workspace>()
        val keys = keyValueSettings.keys
            .filter { d -> d.contains("workspace") }
            .map { d -> d.split(".")[0] }
            .toSet()
        keys.map {
            try {
                val workspaceJson = keyValueSettings.getStringOrNull(it)
                val workspace = Json.Default.decodeFromString<Workspace?>(workspaceJson!!)
                if (workspace != null) {
                    mutMap.put(it, workspace)
                }
            } catch (_: Exception) {
            }
        }
        return mutMap
    }

    override fun getAppSettings(): AppSettings {
        return try {
//
//            val appSettings =
//            AppSettings(
//                theme = AppThemeData(
//                    primaryColor = appSettings.appTheme.primaryColor.toColor(),
//
//                    fontFamily = appSettings.appTheme.fontFamily,
//                    colorsType = appSettings.appTheme.colorsType,
//
//                    colors = appSettings.appTheme.colorsData.toData()
//                ),
//                currentWorkspaceFullPath = appSettings.currentWorkspaceFullPath
//            )
            val data = keyValueSettings.getStringOrNull(appSettingsKey)!!
            DefaultJson.decodeFromString<AppSettings>(data)
        } catch (exc: Exception) {
            AppSettings.Companion.defaultSettings
        }
    }

    override fun setAppSettings(settings: AppSettings) {
//        val theme = settings.theme
//        val themeJson = AppThemeJson(
//            primaryColor = theme.primaryColor.toHexString(),
//            colorsType = theme.colorsType,
//            fontFamily = theme.fontFamily,
//            colorsData = theme.colors.toJson()
//        )
//        val appSettingsJson = AppSettingsJson(
//            appTheme = themeJson,
//            currentWorkspaceFullPath = settings.currentWorkspaceFullPath
//        )
        val json = DefaultJson.encodeToString(settings)
        keyValueSettings.putString(appSettingsKey, json)
    }

    override fun createWorkspace(workspace: Workspace) {
        var index = 0
        while (true) {
            val key = "workspace_${index}"
            if (keyValueSettings.contains(key)) {
                index++
                continue
            }
            keyValueSettings.putString(key, Json.Default.encodeToString(workspace))
            break
        }
    }

    override fun deleteWorkspace(workspace: Workspace) {
        val mapWorkspaces = getWorkspaces2()
        for (map in mapWorkspaces) {
            if (map.value.fullpath == workspace.fullpath) {
                keyValueSettings.remove(map.key)
            }
        }
    }

    override fun getWorkspaces(): List<Workspace> {
        return getWorkspaces2().map { d -> d.value }
    }
}