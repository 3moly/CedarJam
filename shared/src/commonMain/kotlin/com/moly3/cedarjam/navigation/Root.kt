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
import com.moly3.cedarjam.core.domain.model.settings.AppSettings
import com.moly3.cedarjam.core.domain.repository.IAppEnvironment
import com.moly3.cedarjam.core.domain.service.IMessageService
import kotlinx.coroutines.flow.StateFlow

@OptIn(ExperimentalDecomposeApi::class)
@Immutable
interface Root : NavigationComponent<Root.Child>, WebNavigationOwner {
    val messageService: IMessageService

    val dialogColorPickerService: DialogColorPickerService
    val dialogSelectWorkspaceService: DialogSelectWorkspaceService
    val appEnvironment: IAppEnvironment
    val dialogCreateWorkspaceService: DialogCreateWorkspaceService
    val dialogAddCollectionRowService: DialogAddCollectionRowService
    val dialogDeleteService: DialogDeleteService
    val appSettingsFlow: StateFlow<AppSettings>
    fun shareMagnifyValue(value: Double)

    sealed class Child {
        class Empty(val component: ISelectWorkspaceComponent) : Child()
        class Workspace(val component: WorkspaceComponent) : Child()
    }
}