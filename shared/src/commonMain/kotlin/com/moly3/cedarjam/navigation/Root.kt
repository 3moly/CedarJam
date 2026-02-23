package com.moly3.cedarjam.navigation

import androidx.compose.runtime.Immutable
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.router.webhistory.WebNavigationOwner
import com.moly3.cedarjam.pages.page_select_workspace.ISelectWorkspaceComponent
import com.moly3.cedarjam.pages.page_workspace.WorkspaceComponent
import com.moly3.cedarjam.core.domain.dialog.DialogAddCollectionRowService
import com.moly3.cedarjam.core.domain.dialog.DialogColorPickerService
import com.moly3.cedarjam.core.domain.dialog.DialogCreateWorkspaceService
import com.moly3.cedarjam.core.domain.dialog.DialogDeleteService
import com.moly3.cedarjam.core.domain.dialog.DialogSelectWorkspaceService
import com.moly3.cedarjam.core.domain.model.UIState
import com.moly3.cedarjam.core.domain.model.settings.AppSettings
import com.moly3.cedarjam.core.domain.repository.IAppEnvironment
import com.moly3.cedarjam.core.domain.service.AlertService
import com.moly3.cedarjam.core.domain.service.IMessageService
import com.moly3.cedarjam.core.domain.usecase.SyncStatusChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

@OptIn(ExperimentalDecomposeApi::class)
@Immutable
interface Root : NavigationComponent<Root.Child>, WebNavigationOwner {

    val isRelease: Boolean
    val messageService: IMessageService
    val dialogColorPickerService: DialogColorPickerService
    val dialogSelectWorkspaceService: DialogSelectWorkspaceService
    val appEnvironment: IAppEnvironment
    val dialogCreateWorkspaceService: DialogCreateWorkspaceService
    val dialogAddCollectionRowService: DialogAddCollectionRowService
    val dialogDeleteService: DialogDeleteService
    val alertService: AlertService
    val appSettingsFlow: StateFlow<AppSettings>
    fun shareMagnifyValue(value: Double)
    val sendingBranchFlow: Flow<UIState<SyncStatusChannel, String>>

    sealed class Child : NavigationInstance {
        class SelectWorkspace(override val component: ISelectWorkspaceComponent) : Child()
        class Workspace(override val component: WorkspaceComponent) : Child()
    }
}