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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.compose.dnd.DragAndDropState
import com.moly3.cedarjam.core.domain.model.UIState
import com.moly3.cedarjam.pages.page_workspace.Intent
import com.moly3.cedarjam.pages.page_workspace.State
import com.moly3.cedarjam.core.ui.model.PageNameData
import com.moly3.cedarjam.core.ui.compositions.LocalAppTheme
import com.moly3.cedarjam.core.ui.compositions.LocalHazeState
import com.moly3.cedarjam.core.ui.compositions.LocalHazeStyle
import com.moly3.cedarjam.core.ui.compositions.LocalUIConfig
import com.moly3.cedarjam.core.ui.func.isCompactUI
import com.moly3.cedarjam.core.ui.func.navigationBarsPaddingCJ
import com.moly3.cedarjam.core.ui.func.rememberWindowSize
import com.moly3.cedarjam.core.ui.func.wstatusBarsPaddingCJ
import com.moly3.cedarjam.core.ui.model.FileTreeItemPresentation
import com.moly3.cedarjam.core.ui.uikit.NeumorphicShape
import vectors.Settings
import com.moly3.cedarjam.navigation.ui.BarLeft
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
) {
    Box(
        modifier = modifier
            .background(LocalAppTheme.current.colors.backgroundPrimary)
            .wstatusBarsPaddingCJ()
            .navigationBarsPaddingCJ()
    ) {
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
            if (isCompactUI() || !isCompactUI() && isFullMenu) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 8.dp)
                        .padding(horizontal = 16.dp)
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
                            val isCompact = isCompactUI()
                            LazyColumn(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .padding(top = 4.dp, bottom = 12.dp),
                                verticalArrangement = Arrangement.spacedBy(2.dp),
                                state = listState
                            ) {
                                menuContentFileTree(
                                    isCloseMenu = isCompact,
                                    selectedKey = selectedKey,
                                    state = state,
                                    items = state.files,
                                    dragAndDropState = dragAndDropState,
                                    onIntent = onIntent
                                )
                            }
                        }
                    }
                    val toDownload = when (val sync = state.syncStatus) {
                        is UIState.Error<*> -> 0
                        UIState.Loading -> 0
                        is UIState.Success -> sync.data.toDownload.size
                    }
                    val toUpload = when (val sync = state.syncStatus) {
                        is UIState.Error<*> -> 0
                        UIState.Loading -> 0
                        is UIState.Success -> sync.data.toUpload.size
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        if (isCompactUI()) {
                            NeumorphicShape(
                                modifier = Modifier.size(LocalUIConfig.current.fabCircleSize),
                                painter = rememberVectorPainter(BarLeft)
                            ) {
                                onIntent(Intent.SetIsFullMenu(!state.isMenuOpened))
                            }
                        }
                        WorkspaceSelect(
                            modifier = Modifier.weight(1f),
                            activeWorkspace = state.activeWorkspace,
                            onChangeWorkspace = {
                                onIntent(Intent.SelectWorkspace)
                            },
                            toDownload = toDownload,
                            toUpload = toUpload
                        )
                        NeumorphicShape(
                            modifier = Modifier.size(LocalUIConfig.current.fabCircleSize),
                            painter = rememberVectorPainter(Settings)
                        ) {
                            onIntent(Intent.OpenSettings)
                        }
                    }
                }
            }

        }
    }
}