package com.moly3.cedarjam.features.feature_settings

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.backStack
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.navigate
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.value.Value
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.moly3.cedarjam.core.domain.dialog.DialogColorPickerService
import com.moly3.cedarjam.core.domain.repository.IFilesRepository
import com.moly3.cedarjam.core.domain.service.WorkspaceSession
import com.moly3.cedarjam.core.domain.usecase.ISyncUseCase
import com.moly3.cedarjam.features.feature_settings.IDialogSettingsComponent.Child.*
import com.moly3.cedarjam.features.feature_settings.child.general.SettingsGeneralComponent
import com.moly3.cedarjam.features.feature_settings.child.main.SettingsMainComponent
import com.moly3.cedarjam.features.feature_settings.child.storage.SettingsStorageComponent
import com.moly3.cedarjam.features.feature_settings.child.sync.SettingsSyncComponent
import com.moly3.cedarjam.features.feature_settings.child.transparent.SettingsTransparentComponent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.serialization.Serializable

@OptIn(ExperimentalCoroutinesApi::class)
class DialogSettingsComponentImpl(
    componentContext: ComponentContext,
    private val storeFactory: StoreFactory,
    private val workspaceSession: WorkspaceSession,
    private val dialogColorPickerService: DialogColorPickerService,
    private val systemFilesManager: IFilesRepository,
    private val syncUseCase: ISyncUseCase,
    private val onClose: () -> Unit
) : IDialogSettingsComponent,
    ComponentContext by componentContext {

    private fun child(
        config: Config,
        componentContext: ComponentContext
    ): IDialogSettingsComponent.Child =
        when (config) {
            Config.Transparent -> Transparent(
                SettingsTransparentComponent(
                    componentContext = componentContext
                )
            )

            is Config.Main -> Main(
                SettingsMainComponent(
                    componentContext = componentContext,
                    close = {
                        onClose()
                    },
                    openGeneral = {
                        navigation.navigate {
                            it + Config.General
                        }
                    },
                    openSync = {
                        navigation.navigate {
                            it + Config.Sync
                        }
                    },
                    openStorage = {
                        navigation.navigate {
                            it + Config.Storage
                        }
                    },
                )
            )

            Config.General -> General(
                SettingsGeneralComponent(
                    componentContext = componentContext,
                    workspaceSession = workspaceSession,
                    back = {
                        navigation.pop()
                    },
                    close = {
                        onClose()
                    },
                    dialogColorPickerService = dialogColorPickerService,
                    systemFilesManager = systemFilesManager,
                )
            )

            Config.Storage -> Storage(
                SettingsStorageComponent(
                    componentContext = componentContext,
                    workspaceSession = workspaceSession,
                    storeFactory = storeFactory,
                    back = {
                        navigation.pop()
                    },
                    close = {
                        onClose()
                    }
                )
            )

            Config.Sync -> Sync(
                SettingsSyncComponent(
                    componentContext = componentContext,
                    workspaceSession = workspaceSession,
                    storeFactory = storeFactory,
                    back = {
                        navigation.pop()
                    },
                    close = {
                        onClose()
                    },
                    syncUseCase = syncUseCase,
                )
            )
        }

    private val navigation = StackNavigation<Config>()

    private val _stack = childStack(
        key = "settings",
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

        @Serializable
        data object General : Config

        @Serializable
        data object Storage : Config
        @Serializable
        data object Sync : Config
    }
}
