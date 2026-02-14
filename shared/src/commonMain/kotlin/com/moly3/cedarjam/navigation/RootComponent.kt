package com.moly3.cedarjam.navigation

import co.touchlab.kermit.Logger
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.childStackWebNavigation
import com.arkivanov.decompose.router.stack.pushToFront
import com.arkivanov.decompose.router.stack.replaceAll
import com.arkivanov.decompose.router.webhistory.WebNavigation
import com.arkivanov.decompose.value.Value
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.moly3.cedarjam.navigation.Root.Child.SelectWorkspace
import com.moly3.cedarjam.navigation.Root.Child.Workspace
import com.moly3.cedarjam.pages.page_select_workspace.SelectWorkspaceComponent
import com.moly3.cedarjam.pages.page_workspace.WorkspaceComponentImpl
import com.moly3.cedarjam.core.domain.dialog.DialogAddCollectionRowService
import com.moly3.cedarjam.core.domain.dialog.DialogColorPickerService
import com.moly3.cedarjam.core.domain.dialog.DialogCreateWorkspaceService
import com.moly3.cedarjam.core.domain.dialog.DialogDeleteService
import com.moly3.cedarjam.core.domain.dialog.DialogSelectWorkspaceService
import com.moly3.cedarjam.core.domain.model.settings.AppSettings
import com.moly3.cedarjam.core.domain.model.WorkspaceInput
import com.moly3.cedarjam.core.domain.repository.IAppEnvironment
import com.moly3.cedarjam.core.domain.service.AlertService
import com.moly3.cedarjam.core.ui.service.MacTrackpadGestureService
import com.moly3.cedarjam.core.domain.service.IMessageService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope

@OptIn(ExperimentalDecomposeApi::class)
class RootComponent(
    private val parentComponentContext: ComponentContext,
    private val onDestroy: () -> Unit = {}
) : Root,
    ComponentContext by parentComponentContext,
    IDecomposeScopeComponent,
    KoinComponent {

    override val scope by componentScope()

    private val storeFactory: StoreFactory = DefaultStoreFactory()
    private val navigation = StackNavigation<Config>()
    private val navigator: NavigatorDispatcher by inject()
    private val coroutineScope: CoroutineScope by inject()
    private val macTrackpadGestureService: MacTrackpadGestureService by inject()
    override val dialogAddCollectionRowService: DialogAddCollectionRowService by inject()
    override val dialogColorPickerService: DialogColorPickerService by inject()
    override val dialogCreateWorkspaceService: DialogCreateWorkspaceService by inject()
    override val dialogDeleteService: DialogDeleteService by inject()
    override val dialogSelectWorkspaceService: DialogSelectWorkspaceService by inject()
    override val messageService: IMessageService by inject()
    override val appEnvironment: IAppEnvironment by inject()
    override val appSettingsFlow: StateFlow<AppSettings> = appEnvironment.getAppSettingsFlow()
    override val alertService: AlertService by inject()

    private val _stack = childStack(
        source = navigation,
        serializer = Config.serializer(),
        initialStack = { listOf(Config.Empty) },
        childFactory = ::child
    )


    override val webNavigation: WebNavigation<*> =
        childStackWebNavigation(
            navigator = navigation,
            stack = _stack,
            serializer = Config.serializer(),
            pathMapper = { null },
            childSelector = {
                null
            },
        )

    private fun child(config: Config, componentContext: ComponentContext): Root.Child =
        when (config) {
            Config.Empty -> SelectWorkspace(
                SelectWorkspaceComponent(
                    componentContext = componentContext,
                    storeFactory = storeFactory,
                    onSelectWorkspace = {
                        Logger.d("onSelectWorkspace: ${it.name}")
                        try {
                            navigation.replaceAll(Config.Workspace(it))
//                            navigation.pushToFront(Config.Workspace(it))
                        } catch (exc: Exception) {
                            Logger.d("onSelectWorkspace: ${exc.message}")
                        }
                    }
                )
            )

            is Config.Workspace -> Workspace(
                WorkspaceComponentImpl(
                    workspaceInput = config.workspace,
                    context = componentContext,
                    storeFactory = storeFactory
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

    override val childStack: Value<ChildStack<*, Root.Child>> = _stack

    override fun shareMagnifyValue(value: Double) {
        coroutineScope.launch {
            macTrackpadGestureService.shareValue(value)
        }
    }

    override fun onScopeClose(scope: Scope) {
        super.onScopeClose(scope)
        onDestroy()
    }

    @Serializable
    sealed interface Config {
        @Serializable
        data object Empty : Config

        @Serializable
        data class Workspace(val workspace: WorkspaceInput) : Config
    }
}