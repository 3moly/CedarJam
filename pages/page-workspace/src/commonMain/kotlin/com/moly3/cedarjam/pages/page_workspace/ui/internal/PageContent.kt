package com.moly3.cedarjam.pages.page_workspace.ui.internal

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moly3.cedarjam.pages.page_workspace.Intent
import com.moly3.cedarjam.pages.page_workspace.Label
import com.moly3.cedarjam.pages.page_workspace.State
import com.moly3.cedarjam.core.ui.compositions.LocalAppTheme
import com.moly3.cedarjam.core.ui.compositions.LocalDragAndDrop
import com.moly3.cedarjam.core.ui.func.rememberWindowSize
import com.moly3.cedarjam.core.ui.model.WindowSize
import com.moly3.cedarjam.core.ui.uikit.CJText
import com.moly3.cedarjam.core.ui.uikit.UIStateContentNoBox
import dev.chrisbanes.haze.HazeDefaults
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.rememberHazeState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest

const val minimumWidth = 40f
const val MenuCoveredId = -1

@Composable
fun PageContent(
    modifier: Modifier,
    state: State,
    labelsFlow: Flow<Label>,
    onIntent: (Intent) -> Unit,
    onSetIsFullMenu: (Boolean) -> Unit,
    content: @Composable () -> Unit
) {
    val dragAndDropState = LocalDragAndDrop.current
    val menuAnimWidth = if (state.isMenuOpened) state.menuWidth.dp else minimumWidth.dp
    val hazeState = rememberHazeState(blurEnabled = false)
    val hazeStyle = remember {
        HazeStyle(
            backgroundColor = Color.Black,
            tints = listOf(HazeTint(Color.Black.copy(0.1f))),
            blurRadius = 16.dp,
            noiseFactor = HazeDefaults.noiseFactor
        )
    }
    val listState = rememberLazyListState()
    LaunchedEffect(Unit) {
        labelsFlow.collectLatest {
            when (it) {
                is Label.ScrollToIndex -> {
                    listState.scrollToItem(it.index)
                }
            }
        }
    }
    val windowSize by rememberWindowSize()
    if (windowSize == WindowSize.Compact) {
        Box(Modifier.fillMaxSize()) {
            if (state.isMenuOpened) {
                MenuContent(
                    modifier = Modifier.fillMaxSize(),
                    isFullMenu = state.isMenuOpened,
                    hazeState = hazeState,
                    hazeStyle = hazeStyle,
                    state = state,
                    listState = listState,
                    dragAndDropState = dragAndDropState,
                    onIntent = onIntent
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF3F3F3F))
                ) {
                    content()
                    UIStateContentNoBox(state = state.fileVersionsState) {
                        Column(modifier=Modifier.padding(8.dp).background(Color.DarkGray),verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            CJText(text = "filesToArchive", fontSize = 16.sp)
                            for (item in it.filesToArchive) {
                                CJText(text = item.relativePath, fontSize = 12.sp)
                            }
                            CJText(text = "filesToDownload", fontSize = 16.sp)
                            for (item in it.filesToDownload) {
                                CJText(text = item, fontSize = 12.sp)
                            }
                            CJText(text = "delete local", fontSize = 16.sp)
                            for (item in it.localDeletedFilesByServer) {
                                CJText(text = item.getRelativePath(), fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    } else {
        Row(modifier = modifier.fillMaxSize()) {
            val sliderWidth by animateDpAsState(
                targetValue = if (state.menuCovered == MenuCoveredId) 5.dp else 0.dp
            )
            val sliderColor by animateColorAsState(
                targetValue = if (state.menuCovered == MenuCoveredId) LocalAppTheme.current.primaryColor else LocalAppTheme.current.colors.backgroundPrimary
            )
            val backColor = LocalAppTheme.current.colors.backgroundPrimary
            MenuContent(
                modifier = Modifier
                    .width(menuAnimWidth)
                    .drawWithContent {
                        drawContent()
                        val dividerWidth = 5.dp.toPx()
                        val sliderWidthPx = sliderWidth.toPx()
                        val slider = sliderColor
                        drawRect(
                            color = backColor,
                            topLeft = Offset(size.width - dividerWidth, 0f),
                            size = Size(dividerWidth, size.height)
                        )
                        drawRect(
                            color = slider,
                            topLeft = Offset(size.width - sliderWidthPx, 0f),
                            size = Size(sliderWidthPx, size.height)
                        )
                    },
                isFullMenu = state.isMenuOpened,
                hazeState = hazeState,
                hazeStyle = hazeStyle,
                state = state,
                listState = listState,
                dragAndDropState = dragAndDropState,
                onIntent = onIntent
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(Color(0xFF3F3F3F))
            ) {
                content()
                UIStateContentNoBox(state = state.fileVersionsState) {
                    Column(modifier=Modifier.padding(8.dp).background(Color.DarkGray),verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        CJText(text = "filesToArchive", fontSize = 16.sp)
                        for (item in it.filesToArchive) {
                            CJText(text = item.relativePath, fontSize = 12.sp)
                        }
                        CJText(text = "filesToDownload", fontSize = 16.sp)
                        for (item in it.filesToDownload) {
                            CJText(text = item, fontSize = 12.sp)
                        }
                        CJText(text = "delete local", fontSize = 16.sp)
                        for (item in it.localDeletedFilesByServer) {
                            CJText(text = item.getRelativePath(), fontSize = 12.sp)
                        }
                    }
                }

            }
        }
    }
}