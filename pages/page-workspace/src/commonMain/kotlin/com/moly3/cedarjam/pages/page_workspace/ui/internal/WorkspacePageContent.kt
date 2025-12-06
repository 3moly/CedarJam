package com.moly3.cedarjam.pages.page_workspace.ui.internal

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.mohamedrejeb.compose.dnd.DragAndDropContainer
import com.mohamedrejeb.compose.dnd.rememberDragAndDropState
import com.moly3.cedarjam.core.domain.func.getPlatform
import com.moly3.cedarjam.core.domain.model.Platform
import com.moly3.cedarjam.core.domain.model.UIState
import com.moly3.cedarjam.core.domain.model.error.DatabaseError
import com.moly3.cedarjam.core.ui.compositions.LocalAppTheme
import com.moly3.cedarjam.core.ui.compositions.LocalDragAndDrop
import com.moly3.cedarjam.core.ui.func.PointerIconType
import com.moly3.cedarjam.core.ui.func.absoluteOffset
import com.moly3.cedarjam.core.ui.func.getPointerIcon
import com.moly3.cedarjam.core.ui.func.rememberWindowSize
import com.moly3.cedarjam.core.ui.model.FileTreeItemPresentation
import com.moly3.cedarjam.core.ui.model.WindowSize
import com.moly3.cedarjam.core.ui.motions.PointerRequisite
import com.moly3.cedarjam.core.ui.motions.detectPointerTransformGestures
import com.moly3.cedarjam.core.ui.onPointerEvent
import com.moly3.cedarjam.core.ui.uikit.CJButton
import com.moly3.cedarjam.core.ui.uikit.CJContextMenu
import com.moly3.cedarjam.core.ui.uikit.CJContextMenuButton
import com.moly3.cedarjam.core.ui.uikit.CJIcon
import com.moly3.cedarjam.core.ui.uikit.CJLinearProgressIndicator
import com.moly3.cedarjam.core.ui.uikit.CJText
import com.moly3.cedarjam.core.ui.vectors.BarLeft
import com.moly3.cedarjam.pages.page_tabs.ui.TabsPage
import com.moly3.cedarjam.pages.page_tabs.ui.TabsPageContent
import com.moly3.cedarjam.pages.page_workspace.Intent
import com.moly3.cedarjam.pages.page_workspace.State
import com.moly3.cedarjam.pages.page_workspace.WorkspaceComponent
import com.moly3.cedarjam.pages.page_workspace.func.getTab
import com.moly3.cedarjam.pages.page_workspace.model.LockedMenuData
import com.moly3.cedarjam.pages.page_workspace.ui.ToolbarHeight
import com.moly3.cedarjam.pages.page_workspace.ui.ToolbarState
import com.moly3.cedarjam.pages.page_workspace.ui.dialog.DialogSelectTagUI
import com.moly3.cedarjam.pages.page_workspace.ui.dialog.DialogTagToTagUI
import com.moly3.cedarjam.pages.page_workspace.ui.dialog.DialogWorkspaceSettingsUI
import dev.chrisbanes.haze.HazeDefaults
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState

@Composable
internal fun WorkspacePageContent(
    component: WorkspaceComponent,
    state: State,
    titleBarContent: @Composable (@Composable (ToolbarState) -> Unit) -> Unit = {},
    onIntent: (Intent) -> Unit
) {
    val hazeState = rememberHazeState(blurEnabled = false)

    val primaryColor = LocalAppTheme.current.primaryColor
    val hazeStyle = remember(primaryColor) {
        HazeStyle(
            backgroundColor = primaryColor,
            tints = listOf(HazeTint(primaryColor.copy(0.2f))),
            blurRadius = 4.dp,
            noiseFactor = HazeDefaults.noiseFactor
        )
    }
    val scope = rememberCoroutineScope()

    val windowSize by rememberWindowSize()
    val updatedWindowSize by rememberUpdatedState(windowSize)
    val updatedIsMenuOpened by rememberUpdatedState(state.isMenuOpened)
    val updatedMenuWidth by rememberUpdatedState(state.menuWidth)

    when (val status = state.databaseStatus) {
        is UIState.Error -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    when (val dbError = status.error) {
                        DatabaseError.NotExist -> {
                            CJText(text = "database is not created")
                            CJButton(text = "create database for this workspace") {
                                onIntent(Intent.CreateWorkspace)
                            }
                        }

                        is DatabaseError.WrongFile -> {
                            CJText(text = dbError.message)
                        }
                    }

                    CJButton(text = "select workspace") {
                        onIntent(Intent.SelectWorkspace)
                    }
                }
            }
        }

        is UIState.Loading -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CJLinearProgressIndicator()
            }
        }

        is UIState.Success -> {
            var screenWidth by remember { mutableStateOf<Float?>(null) }
            val updatedScreenWidth by rememberUpdatedState(screenWidth)
            val pointerIcon = remember(state.lockedMenuCovered, state.menuCovered) {
                if (state.lockedMenuCovered != null || state.menuCovered != null) {
                    PointerIconType.ResizeHorizontal
                } else {
                    PointerIconType.Default
                }
            }
            val updatedTabSizes by rememberUpdatedState(state.tabSizes)
            val items by component.children.subscribeAsState()
            val density = LocalDensity.current.density
            val updatedDensity = rememberUpdatedState(density)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .onGloballyPositioned {
                        screenWidth = it.size.width.toFloat()
                    }
                    .let {
                        if (pointerIcon == PointerIconType.Default) {
                            it
                        } else {
                            it.pointerHoverIcon(
                                getPointerIcon(pointerIcon),
                                overrideDescendants = true
                            )
                        }
                    }
                    .pointerInput(Unit) {
                        detectPointerTransformGestures(
                            scope = scope,
                            numberOfPointers = 0,
                            consume = false,
                            requisite = PointerRequisite.GreaterThan,
                            onClick = {
                                onIntent(Intent.HideContextMenu)
                            },
                            onCursorMove = { cursorOffset ->
                                onIntent(Intent.SetCursorPosition(cursorOffset))

                                val tab = getTab(
                                    screenWidth = (updatedScreenWidth
                                        ?: 0f) / updatedDensity.value,
                                    menuWidth = if (updatedIsMenuOpened) (updatedMenuWidth) else {
                                        if (updatedWindowSize == WindowSize.Compact)
                                            0f
                                        else
                                            50f
                                    },
                                    updatedTabSizes = updatedTabSizes,
                                    cursorOffset = cursorOffset / updatedDensity.value,
                                    divideSize = 15f,
                                    isLog = false
                                )
                                onIntent(Intent.SetMenuUnder(tab))
                            },
                            onGestureStart = { input ->

                                val sop = input.position / updatedDensity.value

                                val tab = getTab(
                                    screenWidth = (updatedScreenWidth
                                        ?: 0f) / updatedDensity.value,
                                    menuWidth = if (updatedIsMenuOpened) updatedMenuWidth else {
                                        if (updatedWindowSize == WindowSize.Compact)
                                            0f
                                        else
                                            50f
                                    },
                                    updatedTabSizes = updatedTabSizes,
                                    cursorOffset = sop,
                                    divideSize = 15f,
                                    isLog = false
                                )
                                if (tab == null) {
                                    onIntent(Intent.SetLockedMenuUnder(null))
                                } else {
                                    println("tab detected: ${tab}")
                                    onIntent(
                                        Intent.SetLockedMenuUnder(
                                            LockedMenuData(
                                                offsetX = sop.x,
                                                menu = tab
                                            )
                                        )
                                    )
                                }
                            },
                            onGesture = { centroid, pan, zoom, rotation, mainPointer, changes ->
                                val allTabs = items.items.map {
                                    it.instance.index
                                }
                                if (updatedScreenWidth != null) {
                                    onIntent(
                                        Intent.OnOffsetTabChangeOffset(
                                            allTabIndexes = allTabs,
                                            screenWidth = (updatedScreenWidth
                                                ?: 0f) / updatedDensity.value,
                                            data = pan.x / updatedDensity.value,
                                            isEnd = false
                                        )
                                    )
                                }
                            },
                            onGestureEnd = { input ->
                                onIntent(Intent.SetLockedMenuUnder(null))
                            },
                            onGestureCancel = {
                                onIntent(Intent.SetLockedMenuUnder(null))
                            }
                        )
                    }
            ) {
                val dragAndDropState = rememberDragAndDropState<FileTreeItemPresentation>()
                DragAndDropContainer(
                    state = dragAndDropState,
                    modifier = Modifier.fillMaxSize()
                ) {
                    CompositionLocalProvider(
                        LocalDragAndDrop provides dragAndDropState
                    ) {
                        Column {
                            titleBarContent { toolbarState ->

                                val menuWidth = remember(state.menuWidth, state.isMenuOpened) {
                                    if (state.isMenuOpened) {
                                        state.menuWidth.dp
                                    } else {
                                        45.dp
                                    }
                                }
                                val menuWidthCustomEnd = remember(
                                    menuWidth,
                                    toolbarState
                                ) {
                                    if (toolbarState.isFirstCut) {
                                        (menuWidth - toolbarState.menuButtonsWidth).coerceAtLeast(
                                            45.dp
                                        )
                                    } else {
                                        menuWidth
                                    }
                                }
                                val controlsToCut =
                                    remember(toolbarState, menuWidth, menuWidthCustomEnd) {
                                        if (toolbarState.isFirstCut) {
                                            if (menuWidthCustomEnd == 45.dp) {
                                                //toolbarState.menuButtonsWidth
                                                toolbarState.controlsWidthToCut + menuWidthCustomEnd - menuWidth
                                            } else {
                                                0.dp
                                            }
                                        } else {
                                            toolbarState.controlsWidthToCut
                                        }
                                    }
                                val lastWeightToCut = remember(
                                    updatedScreenWidth,
                                    menuWidth,
                                    state.tabSizes,
                                    controlsToCut,
                                    density
                                ) {
                                    if (updatedScreenWidth == null || state.tabSizes.isEmpty()) {
                                        0f
                                    } else {
                                        val availableWidth =
                                            updatedScreenWidth!! - menuWidth.value
                                        val totalWeight = state.tabSizes.map { b -> b.value }
                                            .sumOf { it.toDouble() }.toFloat()
                                        val widthPerWeightUnit = availableWidth / totalWeight
                                        if (widthPerWeightUnit > 0) {
                                            controlsToCut.value * density / widthPerWeightUnit
                                        } else {
                                            0f
                                        }
                                    }
                                }


                                Box(
                                    Modifier
                                        .height(ToolbarHeight.dp)
                                        .fillMaxWidth()
                                ) {
                                    Row(
                                        Modifier.fillMaxSize(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            Modifier.fillMaxHeight().width(menuWidthCustomEnd)
                                        ) {
                                            CJIcon(
                                                modifier = Modifier.padding(end = 12.dp)
                                                    .align(Alignment.CenterEnd),
                                                painter = rememberVectorPainter(BarLeft),
                                                onClick = {
                                                    onIntent(Intent.SetIsFullMenu(!state.isMenuOpened))
                                                }
                                            )
                                        }
                                        Row(modifier = Modifier.weight(1f)) {
                                            for ((index, item) in items.items.withIndex()) {
                                                val isFirstTab = index == 0
                                                val isLastTab = index == items.items.lastIndex

                                                val tabWeight = if (toolbarState.isFullscreen) {
                                                    state.tabSizes[item.instance.index] ?: 1f
                                                } else {
                                                    if (isFirstTab && toolbarState.isFirstCut) {
                                                        (state.tabSizes[item.instance.index]
                                                            ?: 1f) - lastWeightToCut
                                                    } else if (isLastTab && !toolbarState.isFirstCut) {
                                                        (state.tabSizes[item.instance.index]
                                                            ?: 1f) - lastWeightToCut
                                                    } else {
                                                        state.tabSizes[item.instance.index]
                                                            ?: 1f
                                                    }
                                                }
                                                val isActive =
                                                    remember(item, state.activeTabsIndex) {
                                                        component.getActiveTabsIndex(item.configuration) == state.activeTabsIndex
                                                    }
                                                val updatedConfiguration by rememberUpdatedState(
                                                    item.configuration
                                                )
                                                val modifier = Modifier
                                                    .weight(if (tabWeight <= 0f) 0.01f else tabWeight)
                                                    .fillMaxHeight()
                                                    .let {
                                                        when (getPlatform()) {
                                                            Platform.Ios,
                                                            Platform.Android -> it.pointerInput(
                                                                Unit
                                                            ) {
                                                                detectTapGestures {
                                                                    component.setActiveTabs(
                                                                        updatedConfiguration
                                                                    )
                                                                }
                                                            }

                                                            is Platform.Jvm,
                                                            Platform.Wasm -> it.onPointerEvent(
                                                                PointerEventType.Press
                                                            ) {
                                                                component.setActiveTabs(
                                                                    updatedConfiguration
                                                                )
                                                            }
                                                        }
                                                    }
                                                    .clip(RoundedCornerShape(0.dp))
                                                TabsPage(
                                                    modifier = modifier,
                                                    isActive = isActive,
                                                    isLastTab = isLastTab,
                                                    component = item.instance,
                                                    onSelectedTab = {
                                                        component.setActiveTabs(item.configuration)
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            PageContent(
                                modifier = Modifier
                                    .hazeSource(state = hazeState)
                                    .weight(1f)
                                    .fillMaxWidth(),
                                state = state,
                                labelsFlow = component.labels,
                                onSetIsFullMenu = {
                                    onIntent(Intent.SetIsFullMenu(it))
                                },
                                onIntent = {
                                    onIntent(it)
                                }) {
                                Column(Modifier.fillMaxSize()) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .weight(1f)
                                            .background(LocalAppTheme.current.colors.backgroundSecondary),
                                        horizontalArrangement = Arrangement.spacedBy(0.dp)
                                    ) {
                                        for ((index, item) in items.items.withIndex()) {
                                            val tabWeight =
                                                state.tabSizes[item.instance.index] ?: 1f

                                            val isLastTab = index == items.items.lastIndex
                                            val updatedConfiguration by rememberUpdatedState(
                                                item.configuration
                                            )
                                            TabsPageContent(
                                                modifier = Modifier
                                                    .weight(tabWeight)
                                                    .fillMaxHeight()
                                                    .let {
                                                        when (getPlatform()) {
                                                            Platform.Ios,
                                                            Platform.Android -> it.pointerInput(
                                                                Unit
                                                            ) {
                                                                detectTapGestures {
                                                                    component.setActiveTabs(
                                                                        updatedConfiguration
                                                                    )
                                                                }
                                                            }

                                                            is Platform.Jvm,
                                                            Platform.Wasm -> it.onPointerEvent(
                                                                PointerEventType.Press
                                                            ) {
                                                                component.setActiveTabs(
                                                                    updatedConfiguration
                                                                )
                                                            }
                                                        }
                                                    }
                                                    .clip(RoundedCornerShape(0.dp)),
                                                isMenuCovered = state.menuCovered == item.instance.index,
                                                isLastTab = isLastTab,
                                                component = item.instance
                                            )
                                        }
                                    }
                                }
                            }
                        }

                    }
                }
                if (state.contextMenuData != null) {
                    CJContextMenu(
                        modifier = Modifier
                            .absoluteOffset(state.contextMenuData!!.cursorPosition / density)
                            .clip(RoundedCornerShape(8.dp))
                            .border(
                                1.dp,
                                LocalAppTheme.current.primaryColor,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .hazeEffect(state = hazeState, style = hazeStyle),
                        columnModifier = Modifier
                    ) {
                        for (button in state.contextMenuData!!.menuButtons) {
                            CJContextMenuButton(text = button.title, onClick = button.onClick)
                        }
                    }
                }
            }
            DialogTagToTagUI(
                workspaceSession = component.workspaceSession,
                dialog = component.dialogTagToTagService
            )
            DialogSelectTagUI(
                dialog = component.dialogSelectTagService,
                workspaceSession = component.workspaceSession
            )
            DialogWorkspaceSettingsUI(
                settings = state.settings,
                dialog = component.dialogWorkspaceSettingsService,
                onSetSettings = {
                    component.onIntent(Intent.SetSettings(it))
                },
                onChangeFont = {
                    onIntent(Intent.ChangeFont)
                }
            )
        }
    }
}