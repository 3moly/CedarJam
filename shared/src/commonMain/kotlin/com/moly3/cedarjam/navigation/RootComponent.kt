package com.moly3.cedarjam.navigation

import co.touchlab.kermit.Logger
import com.arkivanov.decompose.Child
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pushToFront
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.moly3.cedarjam.core.domain.model.UIState
import com.moly3.cedarjam.core.domain.model.WorkspaceInput
import com.moly3.cedarjam.core.domain.model.settings.AppSettings
import com.moly3.cedarjam.core.domain.repository.IAppEnvironment
import com.moly3.cedarjam.core.domain.service.AlertService
import com.moly3.cedarjam.core.domain.service.IMessageService
import com.moly3.cedarjam.core.domain.usecase.ISyncUseCase
import com.moly3.cedarjam.core.domain.usecase.SyncStatusChannel
import com.moly3.cedarjam.core.ui.dialog.DialogRegistry
import com.moly3.cedarjam.core.ui.service.MacTrackpadGestureService
import com.moly3.cedarjam.di.metro.RootCoroutineScope
import com.moly3.cedarjam.navigation.Root.Child.SelectWorkspace
import com.moly3.cedarjam.navigation.Root.Child.Workspace
import com.moly3.cedarjam.pages.page_select_workspace.SelectWorkspaceFactory
import com.moly3.cedarjam.pages.page_workspace.WorkspaceComponentFactory
import com.moly3.core_domain.BuildConfig
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@AssistedInject
class RootComponent(
    @Assisted private val componentContext: ComponentContext,
    @Assisted private val onDestroy: () -> Unit = {},
    private val selectWorkspaceFactory: SelectWorkspaceFactory,
    private val navigator: NavigatorDispatcher,
    private val rootCoroutineScope: RootCoroutineScope,
    private val macTrackpadGestureService: MacTrackpadGestureService,
    override val messageService: IMessageService,
    override val alertService: AlertService,
    override val appEnvironment: IAppEnvironment,
    private val syncUseCase: ISyncUseCase,
    private val workspaceComponentFactory: WorkspaceComponentFactory,
    override val dialogRegistry: DialogRegistry,
) : Root,
    ComponentContext by componentContext,
    IDecomposeScopeComponent,
    NavigationParent {

    private val coroutineScope: CoroutineScope = rootCoroutineScope.scope

    override val isRelease: Boolean by lazy {
        BuildConfig.IsRelease
    }
    private val storeFactory: StoreFactory = DefaultStoreFactory()
    private val navigation = StackNavigation<Config>()
    override val appSettingsFlow: StateFlow<AppSettings> = appEnvironment.getAppSettingsFlow()

    override val sendingBranchFlow: Flow<UIState<SyncStatusChannel, String>>
        get() = syncUseCase.sendingBranchFlow()
    private val _stack = childStack(
        source = navigation,
        serializer = Config.serializer(),
        initialStack = { listOf(Config.Empty) },
        childFactory = ::child
    )

    private fun child(config: Config, componentContext: ComponentContext): Root.Child =
        when (config) {
            Config.Empty -> SelectWorkspace(
                selectWorkspaceFactory.invoke(
                    componentContext = componentContext,
                    storeFactory = storeFactory,
                    onSelectWorkspace = {
                        Logger.d("onSelectWorkspace: ${it.name}")
                        try {
                            navigation.pushToFront(Config.Workspace(it))
                        } catch (exc: Exception) {
                            Logger.d("onSelectWorkspace: ${exc.message}")
                        }
                    }
                )
            )

            is Config.Workspace -> Workspace(
                workspaceComponentFactory.invoke(
                    workspaceInput = config.workspace,
                    context = componentContext,
                    storeFactory = storeFactory,
                )
            )
        }

    private fun bringTabAndRoute(config: Config, route: Route) {
        navigation.pushToFront(config, onComplete = {
            val active = _stack.value.active.instance
            if (active is Root.Child.Workspace) {
                active.component.onNavigate(route)
            }
        })
    }

    init {
        lifecycle.doOnDestroy { onDestroy() }
        coroutineScope.launch {
            navigator.events.collect {
                onNavigate(it)
            }
        }
    }

    override fun onNavigate(route: Route) {
        coroutineScope.launch(Dispatchers.Main.immediate) {
            val configuration = _stack.value.active.configuration
            when (route) {
                is Route.Workspace -> {
                    navigation.pushToFront(Config.Workspace(route.workspace))
                }

                Route.Back,
                Route.Forward -> {
                    bringTabAndRoute(configuration, route)
                }

                is Route.CollRow,
                is Route.Collection,
                is Route.File,
                Route.MainGraph,
                Route.MainHome,
                is Route.Tag,
                Route.Tags -> {
                    bringTabAndRoute(configuration, route)
                }

                Route.Empty -> navigation.pushToFront(Config.Empty)
            }
        }
    }

    override val children: Value<ChildStack<*, Root.Child>> = _stack

    @OptIn(ExperimentalDecomposeApi::class, ExperimentalCoroutinesApi::class)
    override fun getItems(): List<Child<*, *>> {
        return children.stateFlow.value.items
    }

    override fun shareMagnifyValue(value: Double) {
        coroutineScope.launch {
            macTrackpadGestureService.shareValue(value)
        }
    }

    @Serializable
    sealed interface Config {
        @Serializable
        data object Empty : Config

        @Serializable
        data class Workspace(val workspace: WorkspaceInput) : Config
    }
}
