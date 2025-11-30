package com.moly3.cedarjam.pages.page_workspace

import androidx.compose.runtime.Immutable
import com.arkivanov.decompose.Child
import com.arkivanov.decompose.value.Value
import com.moly3.cedarjam.navigation.Route
import com.moly3.cedarjam.pages.page_tabs.TabsComponent
import com.moly3.cedarjam.core.domain.dialog.DialogSelectTagService
import com.moly3.cedarjam.core.domain.dialog.DialogTagToTagService
import com.moly3.cedarjam.core.domain.service.WorkspaceSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

@Immutable
interface WorkspaceComponent {
    val dialogSelectTagService: DialogSelectTagService
    val dialogTagToTagService: DialogTagToTagService
    val children: Value<Children<*, TabsComponent>>
    val workspaceSession: WorkspaceSession
    val state: StateFlow<State>
    val labels: Flow<Label>
    fun onIntent(intent: Intent)
    fun onNavigate(route: Route)
    fun setActiveTabs(component: Any)
    fun getActiveTabsIndex(item: Any): Int

    class Children<out C : Any, out T : Any>(
        val items: List<Child.Created<C, T>>,
    )
}