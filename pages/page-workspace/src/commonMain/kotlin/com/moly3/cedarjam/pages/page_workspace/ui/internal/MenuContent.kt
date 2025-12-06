package com.moly3.cedarjam.pages.page_workspace.ui.internal

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.compose.dnd.DragAndDropState
import com.moly3.cedarjam.pages.page_workspace.Intent
import com.moly3.cedarjam.pages.page_workspace.State
import com.moly3.cedarjam.pages.page_workspace.ui.component.fileNodeTree
import com.moly3.cedarjam.core.domain.model.PageNameData
import com.moly3.cedarjam.core.ui.compositions.LocalAppTheme
import com.moly3.cedarjam.core.ui.compositions.LocalHazeState
import com.moly3.cedarjam.core.ui.compositions.LocalHazeStyle
import com.moly3.cedarjam.core.ui.model.FileTreeItemPresentation
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
    onIntent: (Intent) -> Unit,
    onSetIsFullMenu: (Boolean) -> Unit
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

                WorkspaceSelect(
                    activeWorkspace = state.activeWorkspace,
                    onChangeWorkspace = {
                        onIntent(Intent.SelectWorkspace)
                    },
                    onChangeSettings = {
                        onIntent(Intent.ChangeAppSettings)
                    },
                    onChangeColors = {
                        onIntent(Intent.ChangeAppColors)
                    })
            }
        }
    }
}