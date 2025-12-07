package com.moly3.cedarjam.pages.page_tabs

import androidx.compose.runtime.Immutable
import com.moly3.cedarjam.navigation.NavigationComponent
import com.moly3.cedarjam.pages.page_tab.TabComponent
import com.moly3.cedarjam.core.ui.model.PageNameData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

@Immutable
interface TabsComponent : NavigationComponent<TabsComponent.Child> {
    val index: Int
    val activeTab: Flow<TabsState>
    val state: StateFlow<State>
    fun onIntent(intent: Intent)

    @Immutable
    sealed class Child {
        abstract val component: NavigationComponent<*>

        class Tab(override val component: TabComponent) : Child()
    }
}

data class TabsState(
    val activeTabIndex: Int,
    val activeFlowName: Flow<PageNameData?>
)