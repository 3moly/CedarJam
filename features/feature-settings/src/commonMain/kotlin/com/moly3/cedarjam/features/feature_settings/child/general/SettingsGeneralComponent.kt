package com.moly3.cedarjam.features.feature_settings.child.general

import com.arkivanov.decompose.ComponentContext
import com.moly3.cedarjam.core.domain.model.settings.WorkspaceSettings
import com.moly3.cedarjam.core.domain.service.WorkspaceSession
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.koin.core.component.KoinComponent

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsGeneralComponent(
    componentContext: ComponentContext,
    private val workspaceSession: WorkspaceSession,
    private val close: () -> Unit
) : ISettingsGeneralComponent,
    ComponentContext by componentContext, KoinComponent {

   override val settingsState = workspaceSession.getSettingsFlow()

    override fun onClose() {
        close()
    }

    override suspend fun onSetSettings(it: WorkspaceSettings) {
        workspaceSession.setSettings(it)
    }
}