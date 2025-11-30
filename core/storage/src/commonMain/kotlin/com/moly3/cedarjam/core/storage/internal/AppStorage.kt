package com.moly3.cedarjam.core.storage.internal

import com.moly3.cedarjam.core.storage.IAppStorage
import com.moly3.cedarjam.core.storage.model.AppSettingsJson
import com.moly3.cedarjam.core.storage.model.AppThemeJson
import com.moly3.cedarjam.core.storage.model.toData
import com.moly3.cedarjam.core.storage.model.toJson
import com.russhwolf.settings.Settings
import com.russhwolf.settings.contains
import com.moly3.cedarjam.core.domain.DefaultJson
import com.moly3.cedarjam.core.domain.func.toColor
import com.moly3.cedarjam.core.domain.func.toHexString
import com.moly3.cedarjam.core.domain.model.AppSettings
import com.moly3.cedarjam.core.domain.model.AppThemeData
import com.moly3.cedarjam.core.domain.model.Workspace
import kotlinx.serialization.json.Json
import kotlin.collections.iterator

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
            val data = keyValueSettings.getStringOrNull(appSettingsKey)!!
            val appSettings = DefaultJson.decodeFromString<AppSettingsJson>(data)
            AppSettings(
                theme = AppThemeData(
                    primaryColor = appSettings.appTheme.primaryColor.toColor(),

                    fontFamily = appSettings.appTheme.fontFamily,
                    colorsType = appSettings.appTheme.colorsType,

                    colors = appSettings.appTheme.colorsData.toData()
                ),
                currentWorkspaceFullPath = appSettings.currentWorkspaceFullPath
            )
        } catch (exc: Exception) {
            AppSettings.Companion.defaultSettings
        }
    }

    override fun setAppSettings(settings: AppSettings) {
        val theme = settings.theme
        val themeJson = AppThemeJson(
            primaryColor = theme.primaryColor.toHexString(),
            colorsType = theme.colorsType,
            fontFamily = theme.fontFamily,
            colorsData = theme.colors.toJson()
        )
        val appSettingsJson = AppSettingsJson(
            appTheme = themeJson,
            currentWorkspaceFullPath = settings.currentWorkspaceFullPath
        )
        val json = DefaultJson.encodeToString(appSettingsJson)
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