package com.moly3.cedarjam.features.feature_settings.child.general

import androidx.compose.runtime.Stable
import com.moly3.cedarjam.core.domain.model.settings.WorkspaceSettings
import kotlinx.coroutines.flow.StateFlow

@Stable
interface ISettingsGeneralComponent {
    val settingsState: StateFlow<WorkspaceSettings>
    fun onClose()
    suspend fun onSetSettings(it: WorkspaceSettings)
}