package com.moly3.cedarjam.pages.page_workspace.store

import androidx.compose.ui.geometry.Offset
import com.arkivanov.mvikotlin.core.store.Store
import com.moly3.cedarjam.core.domain.model.IndexFileDto
import com.moly3.cedarjam.core.domain.model.UIState
import com.moly3.cedarjam.core.domain.model.WorkspacePresentation
import com.moly3.cedarjam.core.domain.model.error.DatabaseError
import com.moly3.cedarjam.core.domain.model.settings.WorkspaceFont
import com.moly3.cedarjam.core.domain.model.settings.WorkspaceSettings
import com.moly3.cedarjam.core.domain.usecase.GetSyncStatus
import com.moly3.cedarjam.core.ui.model.FileTreeItemPresentation
import com.moly3.cedarjam.pages.page_workspace.Intent
import com.moly3.cedarjam.pages.page_workspace.Label
import com.moly3.cedarjam.pages.page_workspace.PageNameWorkspace
import com.moly3.cedarjam.pages.page_workspace.State
import com.moly3.cedarjam.pages.page_workspace.model.ContextMenuData
import com.moly3.cedarjam.pages.page_workspace.model.LockedMenuData
import com.moly3.cedarjam.pages.page_workspace.model.RenameFileNodeData
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.ImmutableSet

internal interface WorkspaceStore : Store<Intent, State, Label> {

    sealed interface Msg {

        data class SetTabSizes(val value: ImmutableMap<Int, Float>) : Msg
        data class SetIsFullMenu(val value: Boolean) : Msg
        data class SetRenameFileNodeData(val value: RenameFileNodeData?) : Msg

        data class SetActiveTab(val value: Int) : Msg
        data class SetPosition(val value: Offset?) : Msg
        data class SetContextMenu(val value: ContextMenuData?) : Msg
        data class SetFileNodesTree(val value: ImmutableList<FileTreeItemPresentation>) : Msg
        data class SetActiveWorkspace(val value: WorkspacePresentation) : Msg
        data class SetDatabaseStatus(val value: UIState<Unit, DatabaseError>) : Msg
        data class SetCurrentTabData(val value: PageNameWorkspace) : Msg
        data class SetOpenedDirectories(val value: ImmutableSet<String>) : Msg

        data class SetMenuCovered(val value: Int?) : Msg
        data class SetLockedMenuCovered(val value: LockedMenuData?) : Msg
        data class SetMenuWidth(val value: Float) : Msg
        data class SetWorkspaceFont(val value: WorkspaceFont?) : Msg
        data class SetWorkspaceSettings(val value: WorkspaceSettings) : Msg
        data class SetIndexFiles(val value: ImmutableList<IndexFileDto>) : Msg
        data class SetSyncStatus(val value: UIState<GetSyncStatus, String>) : Msg

    }
}
