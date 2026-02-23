package com.moly3.cedarjam.pages.page_workspace

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.arkivanov.decompose.Child
import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.value.Value
import com.moly3.cedarjam.navigation.Route
import com.moly3.cedarjam.pages.page_tabs.TabsComponent
import com.moly3.cedarjam.core.domain.dialog.DialogSelectTagService
import com.moly3.cedarjam.core.domain.dialog.DialogTagToTagService
import com.moly3.cedarjam.core.domain.repository.IFilesRepository
import com.moly3.cedarjam.core.domain.service.WorkspaceSession
import com.moly3.cedarjam.features.feature_settings.IDialogSettingsComponent
import com.moly3.cedarjam.navigation.NavigationComponent
import com.moly3.cedarjam.navigation.NavigationParent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

@Immutable
interface WorkspaceComponent : NavigationParent {
    val settingsDialogSlot: Value<ChildSlot<*, IDialogSettingsComponent>>
    val filesRepository: IFilesRepository
    val dialogSelectTagService: DialogSelectTagService
    val dialogTagToTagService: DialogTagToTagService
    override val children: Value<Children<*, TabsComponent>>
    val workspaceSession: WorkspaceSession
    val state: StateFlow<State>
    val labels: Flow<Label>
    fun onNavigate(route: Route)
    fun onIntent(intent: Intent)
    fun setActiveTabs(component: Any)
    fun getActiveTabsIndex(item: Any): Int

    @Stable
    class Children<out C : Any, out T : Any>(
        val items: List<Child.Created<C, T>>,
    )
}