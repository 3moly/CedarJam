package com.moly3.cedarjam.features.feature_settings

import androidx.compose.runtime.Stable
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.backhandler.BackHandlerOwner
import com.moly3.cedarjam.features.feature_settings.child.general.ISettingsGeneralComponent
import com.moly3.cedarjam.features.feature_settings.child.main.ISettingsMainComponent
import com.moly3.cedarjam.features.feature_settings.child.transparent.ISettingsTransparentComponent

@Stable
interface IDialogSettingsComponent : BackHandlerOwner {
    val childStack: Value<ChildStack<*, Child>>
    fun onIntent(intent: Intent)
    fun onBackClicked()

    sealed class Child {
        class Transparent(val component: ISettingsTransparentComponent) : Child()
        class Main(val component: ISettingsMainComponent) : Child()
        class General(val component: ISettingsGeneralComponent) : Child()
    }
}