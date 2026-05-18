package com.moly3.cedarjam.pages.page_workspace

import androidx.compose.ui.geometry.Offset
import com.moly3.cedarjam.core.domain.model.FileTreeNode
import com.moly3.cedarjam.core.ui.model.PageNameData
import com.moly3.cedarjam.core.domain.model.settings.WorkspaceSettings
import com.moly3.cedarjam.core.ui.model.FileTreeItemPresentation
import com.moly3.cedarjam.pages.page_workspace.model.LockedMenuData
import kotlinx.collections.immutable.ImmutableSet

sealed interface Intent {
    data class SetIsFullMenu(val value: Boolean) : Intent
    data object OpenSettings : Intent
    data class SetIsDark(val isDark: Boolean) : Intent
    data class SetCursorPosition(val offset: Offset) : Intent
    data class SetLockedMenuUnder(val value: LockedMenuData?) : Intent
    data class SetMenuUnder(val tab: Int?) : Intent
    data object HideContextMenu : Intent
    data object SelectWorkspace : Intent
    data object Sync : Intent
    data object CreateWorkspace : Intent
    data class MoveFile(
        val directory: FileTreeItemPresentation,
        val file: FileTreeItemPresentation
    ) : Intent

    data class UploadResource(val directory: FileTreeNode.Directory?) : Intent

    data class OnOffsetTabChangeOffset(
        val allTabIndexes: List<Int>,
        val screenWidth: Float,
        val data: Float,
        val isEnd: Boolean,
    ) : Intent

    data class OpenContextMenu(
        val cursorPosition: Offset,
        val target: FileTreeItemPresentation
    ) : Intent

    data class SelectActiveTabs(val index: Int) : Intent
    data class SetPageName(val value: PageNameWorkspace) : Intent
    data class OnFileTreeClick(val value: FileTreeItemPresentation, val isCloseMenu: Boolean) :
        Intent

    data class SetOpenedDirectories(val value: ImmutableSet<String>) : Intent
    data class CreateFile(val directory: FileTreeItemPresentation) : Intent
    data class Rename(val directory: FileTreeItemPresentation, val newName: String) : Intent
    data class CreateDirectory(val directory: FileTreeItemPresentation) : Intent
    data class RevealFile(val data: PageNameData.PageType) : Intent
    data class ClearingTabs(val data: List<Int>) : Intent
    data class SetSettings(val data: WorkspaceSettings) : Intent


}