package com.moly3.cedarjam.features.feature_settings

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.backStack
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.value.Value
import com.moly3.cedarjam.features.feature_settings.child.main.SettingsMainComponent
import com.moly3.cedarjam.features.feature_settings.child.transparent.SettingsTransparentComponent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.serialization.Serializable
import org.koin.core.component.KoinComponent

@OptIn(ExperimentalCoroutinesApi::class)
class DialogSettingsComponentImpl(
    componentContext: ComponentContext,
    private val onClose: () -> Unit
) : IDialogSettingsComponent,
    ComponentContext by componentContext, KoinComponent {

    private fun child(
        config: Config,
        componentContext: ComponentContext
    ): IDialogSettingsComponent.Child =
        when (config) {
            Config.Transparent -> IDialogSettingsComponent.Child.Transparent(
                SettingsTransparentComponent(
                    componentContext = componentContext
                )
            )

            is Config.Main -> IDialogSettingsComponent.Child.Main(
                SettingsMainComponent(
                    componentContext = componentContext
                )
            )
        }

    private val navigation = StackNavigation<Config>()

    private val _stack = childStack(
        source = navigation,
        serializer = Config.serializer(),
        initialStack = { listOf(Config.Transparent, Config.Main) },
        childFactory = ::child
    )


    override val childStack: Value<ChildStack<*, IDialogSettingsComponent.Child>>
        get() = _stack

    override fun onBackClicked() {
        if (_stack.backStack.size == 1) {
            onClose()
        } else {
            navigation.pop()
        }
    }

    override fun onIntent(intent: Intent) {

    }

    @Serializable
    sealed interface Config {
        @Serializable
        data object Transparent : Config

        @Serializable
        data object Main : Config
    }
}