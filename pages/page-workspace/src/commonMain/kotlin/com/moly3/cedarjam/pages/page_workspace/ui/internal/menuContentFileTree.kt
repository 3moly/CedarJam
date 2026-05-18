package com.moly3.cedarjam.pages.page_workspace.ui.internal

import androidx.compose.foundation.lazy.LazyListScope
import com.mohamedrejeb.compose.dnd.DragAndDropState
import com.moly3.cedarjam.core.ui.model.FileTreeItemPresentation
import com.moly3.cedarjam.pages.page_workspace.Intent
import com.moly3.cedarjam.pages.page_workspace.State
import com.moly3.cedarjam.pages.page_workspace.ui.component.fileNodeTree
import com.moly3.lazyflow.LazyFlowScope
import kotlinx.collections.immutable.ImmutableList

internal fun LazyFlowScope.menuContentFileTree(
    isCloseMenu: Boolean,
    selectedKey: String?,
    state: State,
    items: ImmutableList<FileTreeItemPresentation>,
    dragAndDropState: DragAndDropState<FileTreeItemPresentation>,
    onIntent: (Intent) -> Unit
) {
    for (item in items) {
        fileNodeTree(
            selectedKey = selectedKey,
            item = item,
            spacingLeft = 0,
            openedDirectories = state.openedDirectories,
            renameFileNodeData = state.renameFileNodeData,
            dragAndDropState = dragAndDropState,
            contextMenuTargetKey = state.contextMenuData?.targetKey,

            onItemClick = {
                onIntent(Intent.OnFileTreeClick(it, isCloseMenu = isCloseMenu))
            },
            onMove = { target, item ->
                onIntent(
                    Intent.MoveFile(
                        directory = target,
                        file = item
                    )
                )
            },
            onSecondaryClick = { presentation, offset ->
                onIntent(
                    Intent.OpenContextMenu(
                        cursorPosition = offset,
                        target = presentation
                    )
                )
            },
            onDirectoryCreateClick = {
                onIntent(Intent.CreateDirectory(it))
            },
            onFileCreateClick = {
                onIntent(Intent.CreateFile(it))
            },
            onRename = { file, newName ->
                onIntent(Intent.Rename(file, newName))
            }
        )
    }
}