package com.moly3.cedarjam.pages.page_workspace

import com.arkivanov.decompose.Child
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.DelicateDecomposeApi
import com.arkivanov.decompose.router.children.ChildNavState
import com.arkivanov.decompose.router.children.NavState
import com.arkivanov.decompose.router.children.SimpleChildNavState
import com.arkivanov.decompose.router.children.SimpleNavigation
import com.arkivanov.decompose.router.children.children
import com.arkivanov.decompose.router.slot.activate
import com.arkivanov.decompose.router.slot.child
import com.arkivanov.decompose.value.Value
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.moly3.cedarjam.core.coordinator.SetIsDarkCoordinator
import com.moly3.cedarjam.core.domain.dialog.DialogColorPickerService
import com.moly3.cedarjam.core.domain.dialog.DialogDeleteService
import com.moly3.cedarjam.core.domain.model.WorkspaceInput
import com.moly3.cedarjam.core.domain.repository.IFilesRepository
import com.moly3.cedarjam.core.domain.service.AlertService
import com.moly3.cedarjam.core.domain.service.IMessageService
import com.moly3.cedarjam.core.domain.service.WorkspaceSession
import com.moly3.cedarjam.core.domain.usecase.ISyncUseCase
import com.moly3.cedarjam.core.domain.usecase.NavigateToFileUseCaseFactory
import com.moly3.cedarjam.core.ui.model.PageNameData
import com.moly3.cedarjam.features.feature_settings.func.settingsDialogScopeFactory
import com.moly3.cedarjam.features.feature_settings.model.DialogConfig
import com.moly3.cedarjam.navigation.CreateWorkspaceSession
import com.moly3.cedarjam.navigation.IDecomposeScopeComponent
import com.moly3.cedarjam.navigation.Navigator
import com.moly3.cedarjam.navigation.Route
import com.moly3.cedarjam.navigation.stateFlow
import com.moly3.cedarjam.pages.page_tabs.TabsComponent
import com.moly3.cedarjam.pages.page_tabs.TabsComponentFactory
import com.moly3.cedarjam.pages.page_workspace.store.WorkspaceStoreFactory
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.SupervisorJob
import kotlinx.serialization.Serializable

data class PageNameWorkspace(
    val pageNameData: PageNameData?
)

@OptIn(ExperimentalCoroutinesApi::class)
@AssistedInject
class WorkspaceComponentImpl(
    @Assisted private val workspaceInput: WorkspaceInput,
    @Assisted context: ComponentContext,
    @Assisted storeFactory: StoreFactory,
    override val filesRepository: IFilesRepository,
    private val createWorkspaceSession: CreateWorkspaceSession,
    private val navigator: Navigator,
    private val setIsDarkCoordinator: SetIsDarkCoordinator,
    private val alertService: AlertService,
    private val syncUseCase: ISyncUseCase,
    private val messageService: IMessageService,
    private val dialogColorPickerService: DialogColorPickerService,
    private val dialogDeleteService: DialogDeleteService,
    private val navigateToFileUseCaseFactory: NavigateToFileUseCaseFactory,
    private val tabsComponentFactory: TabsComponentFactory,
) : ComponentContext by context,
    IDecomposeScopeComponent,
    WorkspaceComponent {

    private val coroutineScope: CoroutineScope =
        CoroutineScope(SupervisorJob() + Dispatchers.Main)
    override val workspaceSession: WorkspaceSession by lazy {
        createWorkspaceSession(workspaceInput, stateKeeper)
    }

    private val settingsDialogScope by lazy {
        settingsDialogScopeFactory(
            storeFactory = storeFactory,
            workspaceSession = workspaceSession,
            dialogColorPickerService = dialogColorPickerService,
            systemFilesManager = filesRepository,
            syncUseCase = syncUseCase,
        )
    }

    override val settingsDialogSlot = settingsDialogScope.slot

    private val store by lazy {
        WorkspaceStoreFactory(
            storeFactory = storeFactory,
            lifecycle = lifecycle,
            workspaceSession = workspaceSession,
            navigator = navigator,
            setIsDarkCoordinator = setIsDarkCoordinator,
            alertService = alertService,
            filesRepo = filesRepository,
            syncUseCase = syncUseCase,
            messagerService = messageService,
            dialogColorPickerService = dialogColorPickerService,
            dialogDeleteService = dialogDeleteService,
            fileManagerService = workspaceSession.fileManagerService,
            navigateToFileUseCase = navigateToFileUseCaseFactory(workspaceSession.fileManagerService),
            onSettingsOpen = {
                settingsDialogScope.navigation.activate(DialogConfig)
            },
        ).create(stateKeeper = stateKeeper)
    }


    override val state: StateFlow<State>
        get() = store.stateFlow

    override fun onIntent(intent: Intent) {
        store.accept(intent)
    }

    override val labels: Flow<Label> = store.labels

    private val navigation = SimpleNavigation<(NavigationState) -> NavigationState>()

    private val _children: Value<WorkspaceComponent.Children<*, TabsComponent>> =
        children(
            source = navigation,
            stateSerializer = NavigationState.serializer(),
            key = "workspace_${workspaceInput.name}",
            initialState = {
                NavigationState(
                    configurations = listOf(Config.Tabs(index = 0))
                )
            },
            navTransformer = { state, operation -> operation(state) },
            stateMapper = { state, children ->
                WorkspaceComponent.Children(
                    items = children.map { it as Child.Created }
                )
            },
            childFactory = { config, componentContext ->
                tabsComponentFactory.invoke(
                    context = componentContext,
                    storeFactory = storeFactory,
                    workspaceSession = workspaceSession,
                    index = config.index,
                    openMenu = {
                        store.accept(Intent.SetIsFullMenu(it))
                    },
                    onSelfDestroy = {
                        removeTabs(index = config.index)
                    },
                    onFileReveal = {
                        store.accept(Intent.RevealFile(it))
                    },
                    onNewTabs = {
                        navigation.navigate { state ->
                            val maxIndex = state.configurations.maxOf { d -> d.index } + 1
                            val configsMap = state.configurations.toMutableList()
                            configsMap.add(Config.Tabs(maxIndex))
                            state.copy(
                                configurations = configsMap.sortedBy { d -> d.index }
                            )
                        }
                    }
                )
            },
        )

    override val children: Value<WorkspaceComponent.Children<*, TabsComponent>>
        get() = _children

    override fun getItems(): List<Child<*, *>> {

        return _children.stateFlow.value.items
    }

    init {
        coroutineScope.launch {
            _children.stateFlow.collectLatest {
                val indexes = it.items.map { d -> d.instance.index }
                store.accept(Intent.ClearingTabs(indexes))
            }
        }
        coroutineScope.launch {
            val activeIndexFlow = state
                .map { it.activeTabsIndex }
                .distinctUntilChanged()

            combine(
                _children.stateFlow,
                activeIndexFlow
            ) { children, activeIndex ->
                val foundItem = children.items.first { it.instance.index == activeIndex }
                foundItem.instance.activeTab
                    .flatMapLatest { it.activeFlowName }
            }.flatMapLatest { activeFlow ->
                activeFlow.map { state ->
                    PageNameWorkspace(
                        pageNameData = state
                    )
                }
            }.collectLatest { page ->
                store.accept(Intent.SetPageName(page))
            }
        }
    }

    private fun removeTabs(index: Int) {
        if (children.value.items.size > 1) {
            val itWasActiveIndex = state.value.activeTabsIndex == index
            navigation.navigate { state ->
                val configsMap =
                    state.configurations.filter { d ->
                        d.index != index
                    }
                state.copy(
                    configurations = configsMap.sortedBy { d -> d.index }
                )
            }
            if (itWasActiveIndex) {
                val firstIndex = _children.value.items.firstOrNull()?.instance?.index
                onIntent(Intent.SelectActiveTabs(firstIndex ?: 0))
            }

        }
    }

    @OptIn(DelicateDecomposeApi::class)
    override fun onNavigate(route: Route) {
        val settingsSlotChild = settingsDialogScope.slot.child

        if (settingsSlotChild != null && route is Route.Back) {
            settingsSlotChild.instance.onBackClicked()
        } else {
            coroutineScope.launch(Dispatchers.Main.immediate) {
                val foundActive = _children.value.items
                    .firstOrNull { d -> d.instance.index == state.value.activeTabsIndex }
                    ?: _children.value.items.firstOrNull()
                foundActive?.instance?.onNavigate(route)
            }
        }
    }

    override fun setActiveTabs(component: Any) {
        require(component is Config) { "component is not config" }
        when (component) {
            is Config.Tabs -> {
                store.accept(Intent.SelectActiveTabs(component.index))
            }
        }
    }

    override fun getActiveTabsIndex(item: Any): Int {
        require(item is Config) { "component is not config" }
        return when (item) {
            is Config.Tabs -> item.index
        }
    }


    @Serializable
    sealed interface Config {
        val index: Int

        @Serializable
        data class Tabs(override val index: Int) : Config
    }

    @Serializable
    private data class NavigationState(
        val configurations: List<Config>
    ) : NavState<Config> {

        override val children: List<SimpleChildNavState<Config>> by lazy {
            configurations.mapIndexed { index, config ->
                SimpleChildNavState(
                    configuration = config,
                    status = ChildNavState.Status.RESUMED
                )
            }
        }
    }
}
