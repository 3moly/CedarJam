package com.moly3.cedarjam.features.feature_settings.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalWindowInfo
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.experimental.stack.ChildStack
import com.moly3.cedarjam.core.ui.JvmWindowScope
import com.moly3.cedarjam.features.feature_settings.IDialogSettingsComponent
import com.moly3.cedarjam.features.feature_settings.child.general.ui.SettingsGeneralUI
import com.moly3.cedarjam.features.feature_settings.child.main.ui.SettingsMainUI
import com.moly3.cedarjam.features.feature_settings.child.transparent.ui.SettingsTransparentUI
import com.moly3.cedarjam.navigation.func.backAnimation2

@OptIn(ExperimentalDecomposeApi::class)
@Composable
fun JvmWindowScope.DialogSettingsUI(
    component: IDialogSettingsComponent
) {
    val windowInfo = LocalWindowInfo.current
    ChildStack(
        stack = component.childStack,
        animation = backAnimation2(
            backHandler = component.backHandler,
            onBack = component::onBackClicked,
            screenWidth = windowInfo.containerSize.width
        )
    ) {
        when (val instance = it.instance) {
            is IDialogSettingsComponent.Child.Transparent -> SettingsTransparentUI()
            is IDialogSettingsComponent.Child.Main -> SettingsMainUI(instance.component)
            is IDialogSettingsComponent.Child.General -> SettingsGeneralUI(instance.component)
        }
    }
}