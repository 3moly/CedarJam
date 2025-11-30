package com.moly3.cedarjam.pages.page_workspace

import androidx.compose.ui.geometry.Offset
import com.moly3.cedarjam.pages.page_workspace.model.ContextMenuData
import com.moly3.cedarjam.pages.page_workspace.model.RenameFileNodeData
import com.moly3.cedarjam.core.domain.model.UIState
import com.moly3.cedarjam.core.domain.model.WorkspacePresentation
import com.moly3.cedarjam.core.domain.model.error.DatabaseError
import com.moly3.cedarjam.core.ui.model.FileTreeItemPresentation
import com.moly3.cedarjam.pages.page_workspace.model.LockedMenuData
import com.moly3.cedarjam.pages.page_workspace.model.TabWeightsData
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toPersistentMap
import kotlinx.collections.immutable.toPersistentSet
import kotlinx.serialization.Serializable

data class State(
    val isMenuOpened: Boolean = false,
    val activeTabsIndex: Int = 0,
    val openedDirectories: ImmutableSet<String> = persistentSetOf(),
    val tabSizes: ImmutableMap<Int, Float> = persistentMapOf(),

    val renameFileNodeData: RenameFileNodeData? = null,
    val contextMenuData: ContextMenuData? = null,
    val files: ImmutableList<FileTreeItemPresentation> = persistentListOf(),
    val activeWorkspace: WorkspacePresentation? = null,
    val databaseStatus: UIState<Unit, DatabaseError> = UIState.Loading,
    val activeTabPageData: PageNameWorkspace? = null,

    val cursorPosition: Offset? = null,
    val menuWidth: Float = 400f,
    val menuCovered: Int? = null,
    val lockedMenuCovered: LockedMenuData? = null

) {
    @Serializable
    data class SaveableState(
        val isMenuOpened: Boolean,
        val activeTabsIndex: Int,
        val openedDirectories: Set<String>,
        val tabSizes: Map<Int, Float>
    )

    companion object {
        fun SaveableState.fromSaveable(): State {
            return State(
                isMenuOpened = isMenuOpened,
                activeTabsIndex = this.activeTabsIndex,
                openedDirectories = this.openedDirectories.toPersistentSet(),
                tabSizes = tabSizes.toPersistentMap()
            )
        }

        fun State.toSaveable(): SaveableState {
            return SaveableState(
                isMenuOpened = isMenuOpened,
                activeTabsIndex = this.activeTabsIndex,
                openedDirectories = this.openedDirectories,
                tabSizes = this.tabSizes
            )
        }
    }
}