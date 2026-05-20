package com.moly3.cedarjam.pages.page_workspace.ui.component

import FileButton
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.compose.dnd.DragAndDropState
import com.mohamedrejeb.compose.dnd.drag.DraggableItem
import com.mohamedrejeb.compose.dnd.drop.dropTarget
import com.moly3.cedarjam.core.domain.func.getPlatform
import com.moly3.cedarjam.core.domain.model.Platform
import com.moly3.cedarjam.core.ui.compositions.LocalAppTheme
import com.moly3.cedarjam.pages.page_workspace.model.RenameFileNodeData
import com.moly3.cedarjam.core.ui.func.onSecondaryClickWithPosition
import com.moly3.cedarjam.core.ui.func.rememberWindowSize
import com.moly3.cedarjam.core.ui.model.CJText
import com.moly3.cedarjam.core.ui.model.FileTreeItemPresentation
import com.moly3.cedarjam.core.ui.model.WindowSize
import com.moly3.cedarjam.core.ui.uikit.NeumorphicShape
import com.moly3.cedarjam.pages.page_workspace.Intent
import com.moly3.lazyflow.FlowItemSize
import com.moly3.lazyflow.LazyFlowScope
import kotlinx.collections.immutable.ImmutableSet
import org.jetbrains.compose.resources.stringResource

private val gridCell = FlowItemSize.GridCell(
    1,
    maxSpan = 3,
    crossAxis = 32.dp,
    minMainAxis = 45.dp
)
private val commonCell = FlowItemSize.FillCrossAxis(24.dp)
fun LazyFlowScope.fileNodeTree(
    selectedKey: String?,
    item: FileTreeItemPresentation,
    spacingLeft: Int,

    openedDirectories: ImmutableSet<String>,

    dragAndDropState: DragAndDropState<FileTreeItemPresentation>,
    renameFileNodeData: RenameFileNodeData?,
    contextMenuTargetKey: String?,

    onSecondaryClick: (FileTreeItemPresentation, Offset) -> Unit,
    onItemClick: (FileTreeItemPresentation) -> Unit,
    onMove: ((FileTreeItemPresentation, FileTreeItemPresentation) -> Unit),
    onFileCreateClick: (FileTreeItemPresentation) -> Unit,
    onDirectoryCreateClick: (FileTreeItemPresentation) -> Unit,
    onRename: (FileTreeItemPresentation, String) -> Unit
) {
    val isGrid = when (item.data) {
        FileTreeItemPresentation.FileTreeItemPresentationData.Graph,
        FileTreeItemPresentation.FileTreeItemPresentationData.Home,
        FileTreeItemPresentation.FileTreeItemPresentationData.Annotations -> true

        else -> false
    }
    val size = when (isGrid) {
        true -> gridCell
        else -> commonCell
    }
    item(
        key = item.key,
        size = size,
        contentType = "file",
        animate = false
    ) {

        if (isGrid) {
            val vector = remember(item.key) {
                when (item.data) {
                    FileTreeItemPresentation.FileTreeItemPresentationData.Graph -> vector.NetworkNode
                    FileTreeItemPresentation.FileTreeItemPresentationData.Home -> vector.Home03
                    FileTreeItemPresentation.FileTreeItemPresentationData.Annotations -> vector.collection.Annotation

                    else -> null
                }
            }
            NeumorphicShape(
                modifier = Modifier.fillMaxSize(),
                isPressed = item.key == selectedKey,
                pressedIconColor = LocalAppTheme.current.primaryColor,
                painter = if (vector != null) rememberVectorPainter(vector) else null
            ) {
                onItemClick(item)
            }
        } else {
            var isDragTarget by remember { mutableStateOf(false) }
            val isOpened = remember(openedDirectories, item.key, item.children) {
                if (item.children != null) {
                    openedDirectories.contains(item.key)
                } else
                    null
            }
            val isDirectoryCreateEnabled = remember(item) {
                when (item.data) {
                    is FileTreeItemPresentation.FileTreeItemPresentationData.Directory -> true
                    else -> false
                }
            }
            val windowSize by rememberWindowSize()
            val isDraggableEnabled = remember(item, windowSize) {
                val isEnable = when (val data = item.data) {
                    is FileTreeItemPresentation.FileTreeItemPresentationData.Directory -> data.isDragEnabled
                    is FileTreeItemPresentation.FileTreeItemPresentationData.File -> true
                    else -> false
                }
                when (getPlatform()) {
                    Platform.Android,
                    Platform.Ios -> {
                        if (windowSize != WindowSize.Compact)
                            isEnable
                        else false
                    }

                    Platform.Jvm,
                    Platform.Wasm -> isEnable
                }
            }
            val isRename = remember(renameFileNodeData, item.key) {
                renameFileNodeData?.presentation?.key == item.key
            }
            DraggableItem(
                modifier = Modifier,//.animateItem(),
                enabled = isDraggableEnabled,
                state = dragAndDropState,
                key = item.key,
                data = item,
            ) {
                FileButton(
                    modifier = Modifier.onSecondaryClickWithPosition(
                        key = item,
                        onClick = { offset ->
                            onItemClick(item)
                        },
                        onLongPress = { offset ->
                            onSecondaryClick(item, offset)
                        }
                    ) { offset ->
                        onSecondaryClick(item, offset)
                    }
                        .padding(start = spacingLeft.dp)
                        .fillMaxWidth()
                        .let {
                            if (item.data is FileTreeItemPresentation.FileTreeItemPresentationData.Directory)
                                it.dropTarget(
                                    key = item.key,
                                    state = dragAndDropState,
                                    onDragEnter = {
                                        isDragTarget = true
                                    },
                                    onDragExit = {
                                        isDragTarget = false
                                    },
                                    onDrop = { state ->
                                        isDragTarget = false
                                        onMove.invoke(item, state.data)
                                    }
                                )
                            else
                                it
                        },
                    syncStatus = when (val data = item.data) {
                        is FileTreeItemPresentation.FileTreeItemPresentationData.Directory -> data.syncStatus
                        is FileTreeItemPresentation.FileTreeItemPresentationData.File -> data.syncStatus
                        else -> null
                    },
                    backColor = item.backColor ?: LocalAppTheme.current.colors.backgroundSecondary,
                    isOpen = isOpened,
                    isSelected = selectedKey == item.key,
                    isDragTarget = isDragTarget,
                    isRename = isRename,
                    isContextMenuTarget = contextMenuTargetKey == item.key,

                    title = when (val name = item.name) {
                        is CJText.Raw -> name.text
                        is CJText.Res -> stringResource(name.res)
                    },
                    fileExtension = item.fileExtension,
                    isDirectory = item.children != null,
                    counter = null,

                    onRename = {
                        onRename(item, it)
                    },
                    onClick = {
                        onItemClick(item)
                    },
                    onCreateDirectoryClick = if (isDirectoryCreateEnabled) {
                        { onDirectoryCreateClick(item) }
                    } else null,
                    onCreateFileClick = {
                        onFileCreateClick(item)
                    }
                )
            }
        }
    }
    if (item.children != null && openedDirectories.contains(item.key)) {
        for (subItem in item.children) {
            fileNodeTree(
                selectedKey = selectedKey,
                item = subItem,
                spacingLeft = spacingLeft + 16,
                openedDirectories = openedDirectories,
                renameFileNodeData = renameFileNodeData,
                onItemClick = onItemClick,
                onSecondaryClick = onSecondaryClick,
                dragAndDropState = dragAndDropState,
                onMove = onMove,
                onFileCreateClick = onFileCreateClick,
                onDirectoryCreateClick = onDirectoryCreateClick,
                onRename = onRename,
                contextMenuTargetKey = contextMenuTargetKey
            )
        }
    }
}