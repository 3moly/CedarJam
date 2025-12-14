package com.moly3.cedarjam.pages.page_workspace.ui.internal

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.compose.dnd.DragAndDropState
import com.moly3.cedarjam.pages.page_workspace.Intent
import com.moly3.cedarjam.pages.page_workspace.State
import com.moly3.cedarjam.pages.page_workspace.ui.component.fileNodeTree
import com.moly3.cedarjam.core.ui.model.PageNameData
import com.moly3.cedarjam.core.ui.compositions.LocalAppTheme
import com.moly3.cedarjam.core.ui.compositions.LocalHazeState
import com.moly3.cedarjam.core.ui.compositions.LocalHazeStyle
import com.moly3.cedarjam.core.ui.model.CJText
import com.moly3.cedarjam.core.ui.model.FileTreeItemPresentation
import com.moly3.cedarjam.core.ui.uikit.CJButton
import com.moly3.cedarjam.core.ui.uikit.CJText
import com.moly3.cedarjam.core.ui.uikit.UIStateContentNoBox
import com.moly3.cedarjam.core.ui.volumedBorderStroke
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.hazeSource

@Composable
internal fun MenuContent(
    modifier: Modifier,
    hazeState: HazeState,
    hazeStyle: HazeStyle,
    isFullMenu: Boolean,
    state: State,
    listState: LazyListState,
    dragAndDropState: DragAndDropState<FileTreeItemPresentation>,
    onIntent: (Intent) -> Unit
) {
    Box(modifier = modifier.background(LocalAppTheme.current.colors.backgroundPrimary)) {
        Box(Modifier.fillMaxSize().hazeSource(hazeState)) {
            Box(
                Modifier.fillMaxSize()
                    .background(LocalAppTheme.current.colors.backgroundPrimary)
            )
        }
        CompositionLocalProvider(
            LocalHazeState provides hazeState,
            LocalHazeStyle provides hazeStyle
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
                    .padding(bottom = 8.dp)
                    .padding(horizontal = 12.dp)
            ) {
                val selectedKey = remember(state.activeTabPageData) {
                    if (state.activeTabPageData?.pageNameData != null) {
                        when (val pageType =
                            state.activeTabPageData.pageNameData.pageType) {
                            is PageNameData.PageType.Collection -> "collection: ${pageType.id}"
                            is PageNameData.PageType.CollectionRow -> null
                            is PageNameData.PageType.FileNode -> "file: ${pageType.fileTreeNode.getFullPath()}"
                            PageNameData.PageType.Graph -> "graph_tab"
                            PageNameData.PageType.Home -> "home_tab"
                            is PageNameData.PageType.Tag -> "tag: ${pageType.id}"
                            PageNameData.PageType.Tags -> "tags_tab"
                        }
                    } else {
                        null
                    }
                }
                Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    AnimatedVisibility(isFullMenu) {
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .padding(top = 4.dp, bottom = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                            state = listState
                        ) {
                            for (item in state.files) {
                                fileNodeTree(
                                    selectedKey = selectedKey,
                                    item = item,
                                    spacingLeft = 0,
                                    openedDirectories = state.openedDirectories,
                                    renameFileNodeData = state.renameFileNodeData,
                                    dragAndDropState = dragAndDropState,
                                    contextMenuTargetKey = state.contextMenuData?.targetKey,

                                    onItemClick = {
                                        onIntent(Intent.OnFileTreeClick(it))
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
                    }
                }
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    val new = remember(state.indexes) {
                        state.indexes.count { d -> d.serverSyncStatus == com.moly3.cedarjam.core.domain.model.SyncStatus.NEW }
                    }
                    val upload = remember(state.indexes) {
                        state.indexes.count { d -> d.serverSyncStatus == com.moly3.cedarjam.core.domain.model.SyncStatus.DIRTY }
                    }
                    val deleted = remember(state.indexes) {
                        state.indexes.count { d -> d.serverSyncStatus == com.moly3.cedarjam.core.domain.model.SyncStatus.DELETED }
                    }
                    Row(
                        modifier = Modifier,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            Modifier
                                .border(volumedBorderStroke, RoundedCornerShape(8.dp))
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CJText(text = "->")
                            CJText(text = (upload + new).toString())
                        }
//                       todo Row(
//                            Modifier
//                                .border(volumedBorderStroke, RoundedCornerShape(8.dp))
//                                .padding(4.dp),
//                            horizontalArrangement = Arrangement.spacedBy(4.dp),
//                            verticalAlignment = Alignment.CenterVertically
//                        ) {
//                            CJText(text = "<-")
//                            CJText(text = new.toString())
//                        }
                        Row(
                            Modifier
                                .border(volumedBorderStroke, RoundedCornerShape(8.dp))
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CJText(text = "D")
                            CJText(text = deleted.toString())
                        }
                        if (!state.syncStatus.isLoading()) {
                            CJButton(text = "sync") {
                                onIntent(Intent.Sync)
                            }
                            if (state.syncStatus.isError()) {
                                Box(Modifier.size(10.dp).background(Color.Red))
                            }
                        } else {

                        }
                    }
                }
                WorkspaceSelect(
                    activeWorkspace = state.activeWorkspace,
                    onChangeWorkspace = {
                        onIntent(Intent.SelectWorkspace)
                    },
                    onChangeSettings = {
                        onIntent(Intent.OpenSettings)
                    })
            }
        }
    }
}