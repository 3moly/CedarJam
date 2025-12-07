package com.moly3.cedarjam.features.feature_settings.child.general.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.moly3.cedarjam.core.ui.JvmWindowScope
import com.moly3.cedarjam.features.feature_settings.child.general.ISettingsGeneralComponent
import com.moly3.cedarjam.features.feature_settings.child.general.ui.internal.SettingsGeneralUIContent

@Composable
fun JvmWindowScope.SettingsGeneralUI(component: ISettingsGeneralComponent) {
    val settings by component.settingsState.collectAsState()
    SettingsGeneralUIContent(
        settings = settings,
        onIntent = component::onIntent
    )
}