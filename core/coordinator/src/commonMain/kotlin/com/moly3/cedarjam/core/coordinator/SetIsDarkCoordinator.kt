package com.moly3.cedarjam.core.coordinator

import com.moly3.cedarjam.core.domain.io
import com.moly3.cedarjam.core.domain.model.AppColorsData
import com.moly3.cedarjam.core.domain.model.ColorsType
import com.moly3.cedarjam.core.domain.model.settings.WorkspaceSettings
import com.moly3.cedarjam.core.domain.service.WorkspaceSession
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SetIsDarkCoordinator {

    private suspend fun setSettings(
        workspaceSession: WorkspaceSession,
        newSettings: WorkspaceSettings
    ) {
        withContext(io) {
            val state = workspaceSession.getSettingsFlow().value
            if (state != newSettings) {
                workspaceSession.setSettings(newSettings)
            }
        }
    }

    suspend fun invoke(workspaceSession: WorkspaceSession, isDark: Boolean) {
        val colors = if (isDark) {
            AppColorsData.Dark
        } else {
            AppColorsData.Light
        }
        val state = workspaceSession.getSettingsFlow().value
        setSettings(
            workspaceSession = workspaceSession,
            newSettings = state.copy(
                theme = state.theme.copy(
                    colorsType = if (isDark) ColorsType.Dark else ColorsType.Light,
                    colors = colors
                )
            )
        )
    }
}