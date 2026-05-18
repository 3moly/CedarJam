package com.moly3.cedarjam.pages.page_tabs

import com.arkivanov.decompose.Child
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.active
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.pushNew
import com.arkivanov.decompose.router.stack.pushToFront
import com.arkivanov.decompose.value.Value
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.moly3.cedarjam.navigation.IDecomposeScopeComponent
import com.moly3.cedarjam.navigation.Route
import com.moly3.cedarjam.navigation.stateFlow
import com.moly3.cedarjam.pages.page_tab.TabComponentFactory
import com.moly3.cedarjam.pages.page_tabs.TabsComponentImpl.Config.Tab
import com.moly3.cedarjam.core.ui.model.PageNameData
import com.moly3.cedarjam.core.domain.service.WorkspaceSession
import com.moly3.cedarjam.navigation.NavigationParent
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@OptIn(ExperimentalCoroutinesApi::class)
@AssistedInject
class TabsComponentImpl(
    @Assisted context: ComponentContext,
    @Assisted storeFactory: StoreFactory,
    @Assisted workspaceSession: WorkspaceSession,
    @Assisted private val openMenu: (Boolean) -> Unit,
    @Assisted private val onSelfDestroy: () -> Unit,
    @Assisted private val onNewTabs: () -> Unit,
    @Assisted private val onFileReveal: (PageNameData.PageType) -> Unit,
    @Assisted override val index: Int,
    private val tabComponentFactory: TabComponentFactory,
) : ComponentContext by context,
    IDecomposeScopeComponent,
    TabsComponent,
    NavigationParent {

    private val coroutineScope: CoroutineScope =
        CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val navigation = StackNavigation<Config>()

    override val children: Value<ChildStack<*, TabsComponent.Child>>
        get() = _children

    override fun getItems(): List<Child<*, *>> {
        return _children.value.items
    }

    private val _children = context.childStack(
        source = navigation,
        serializer = Config.serializer(),
        initialConfiguration = Tab(index = 0),
        childFactory = { g, childContext ->
            TabsComponent.Child.Tab(
                tabComponentFactory.invoke(
                    workspaceSession = workspaceSession,
                    context = childContext,
                    storeFactory = storeFactory,
                    openMenu = openMenu,
                    tabIndex = g.index,
                )
            )
        },
    )

    override val activeTab: Flow<TabsState> = _children.stateFlow.map {
        TabsState(
            activeTabIndex = it.active.configuration.index,
            activeFlowName = it.active.instance.component.nameFlow
        )
    }

    override fun onNavigate(route: Route) {
        coroutineScope.launch(Dispatchers.Main.immediate) {
            val activeConfig = _children.active.configuration
            bringTabAndRoute(activeConfig, route)
        }
    }

    override val state: StateFlow<State> = _children.stateFlow.map {
        val activeIndex = it.active.configuration.index
        State(
            currentTabIndex = activeIndex,
            tabs = it.items.map {
                val index = it.configuration.index
                TabData(
                    index = index,
                    nameFlow = it.instance.component.nameFlow
                )
            }.sortedBy { d -> d.index }.toPersistentList())
    }.stateIn(
        scope = coroutineScope,
        started = SharingStarted.Lazily,
        initialValue = State()
    )

    override fun onIntent(intent: Intent) {
        when (intent) {
            Intent.AddNewTabs -> {
                onNewTabs()
            }

            Intent.AddNewTab -> {
                val lastIndex = (state.value.tabs.maxOfOrNull { d -> d.index } ?: 0) + 1
                navigation.pushNew(Tab(lastIndex))
            }

            is Intent.BringToFrontTab -> {
                navigation.pushToFront(Tab(intent.index))
            }

            is Intent.CloseTab -> {
                val activeIndex = state.value.currentTabIndex
                if (_children.value.backStack.isEmpty()) {
                    onSelfDestroy()
                } else {
                    if (activeIndex != null) {
                        if (intent.indexToDelete == activeIndex) {
                            navigation.pop()
                        } else {
                            navigation.pushToFront(Tab(intent.indexToDelete)) {
                                navigation.pop {
                                    navigation.pushToFront(Tab(activeIndex))
                                }
                            }
                        }
                    }
                }
            }

            is Intent.OnFileReveal -> {
                onFileReveal(intent.pageType)
            }
        }
    }

    private fun bringTabAndRoute(config: Config, route: Route) {
        navigation.pushToFront(config, onComplete = {
            val component = _children.value.active.instance.component
            component.onNavigate(route)
        })
    }

    @Serializable
    sealed interface Config {

        val index: Int

        @Serializable
        data class Tab(override val index: Int) : Config
    }
}
