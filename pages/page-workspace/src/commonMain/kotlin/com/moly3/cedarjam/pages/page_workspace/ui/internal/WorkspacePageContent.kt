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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
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
import com.moly3.cedarjam.core.ui.compositions.LocalJvmToolbarState
import com.moly3.cedarjam.core.ui.func.PointerIconType
import com.moly3.cedarjam.core.ui.func.absoluteOffset
import com.moly3.cedarjam.core.ui.func.consumeWindowToolbarPaddingCJ
import com.moly3.cedarjam.core.ui.func.flatClickable
import com.moly3.cedarjam.core.ui.func.getPointerIcon
import com.moly3.cedarjam.core.ui.func.isCompactUI
import com.moly3.cedarjam.core.ui.func.rememberWindowSize
import com.moly3.cedarjam.core.ui.func.statusBarsPaddingCJ
import com.moly3.cedarjam.core.ui.func.windowToolbarPadding
import com.moly3.cedarjam.core.ui.model.CJText
import com.moly3.cedarjam.core.ui.model.FileTreeItemPresentation
import com.moly3.cedarjam.core.ui.model.WindowSize
import com.moly3.cedarjam.core.ui.motions.PointerRequisite
import com.moly3.cedarjam.core.ui.motions.detectPointerTransformGestures
import com.moly3.cedarjam.core.ui.onPointerEvent
import com.moly3.cedarjam.core.ui.uikit.CJButton
import com.moly3.cedarjam.core.ui.uikit.CJContextMenu
import com.moly3.cedarjam.core.ui.uikit.CJContextMenuButton
import com.moly3.cedarjam.core.ui.uikit.CJDraggableArea
import com.moly3.cedarjam.core.ui.uikit.CJLinearProgressIndicator
import com.moly3.cedarjam.core.ui.uikit.CJText
import com.moly3.cedarjam.pages.page_tabs.ui.TabsPageContent
import com.moly3.cedarjam.pages.page_workspace.Intent
import com.moly3.cedarjam.pages.page_workspace.State
import com.moly3.cedarjam.pages.page_workspace.WorkspaceComponent
import com.moly3.cedarjam.pages.page_workspace.func.getTab
import com.moly3.cedarjam.pages.page_workspace.model.LockedMenuData
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun WorkspacePageContent(
    component: WorkspaceComponent,
    state: State,
    onIntent: (Intent) -> Unit
) {
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
                                onIntent(Intent.CreateWorkspaceDatabaseFiles)
                            }
                        }

                        is DatabaseError.WrongFile -> {
                            CJText(text = dbError.message)
                        }

                        is DatabaseError.Error -> {
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
                    .let {
                        if (isCompactUI()) {
                            it.pointerInput(Unit) {
                                detectPointerTransformGestures(
                                    scope = scope,
                                    numberOfPointers = 0,
                                    consume = false,
                                    requisite = PointerRequisite.GreaterThan,
                                    onCursorMove = { cursorOffset ->
                                        onIntent(Intent.SetCursorPosition(cursorOffset))
                                    }
                                )
                            }
                        } else {
                            it.pointerInput(Unit) {
                                detectPointerTransformGestures(
                                    scope = scope,
                                    numberOfPointers = 0,
                                    consume = false,
                                    requisite = PointerRequisite.GreaterThan,
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
                        }
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
                        val toolbarState = LocalJvmToolbarState.current
                        Column(
                            Modifier.fillMaxSize()
                                .let {
                                    if (isCompactUI()) {
                                        it
                                    } else {
                                        it.statusBarsPaddingCJ()
                                            .consumeWindowToolbarPaddingCJ()
                                    }
                                }
                        ) {
                            if (!isCompactUI()) {
                                TabsToolbarContent(
                                    modifier = Modifier,
                                    state = state,
                                    toolbarState = toolbarState,
                                    items = items,
                                    updatedScreenWidth = updatedScreenWidth,
                                    component = component,
                                    onIntent = onIntent
                                )
                            }
                            PageContent(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                                state = state,
                                labelsFlow = component.labels,
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
                                                remember(state.tabSizes, item.instance.index) {
                                                    state.tabSizes[item.instance.index] ?: 1f
                                                }

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
                        if (isCompactUI()) {
                            CJDraggableArea(
                                Modifier.fillMaxWidth().height(windowToolbarPadding.dp)
                            ) {}
                        }

                    }
                }
                if (state.contextMenuData != null) {
                    Box(
                        Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.2f))
                            .flatClickable {
                                onIntent(Intent.HideContextMenu)
                            }) {

                    }
                    CJContextMenu(
                        modifier = Modifier
                            .absoluteOffset(state.contextMenuData.cursorPosition / density)
                            .clip(RoundedCornerShape(8.dp))
                            .border(
                                1.dp,
                                LocalAppTheme.current.primaryColor,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .background(LocalAppTheme.current.primaryColor),
                        columnModifier = Modifier
                    ) {
                        for (button in state.contextMenuData.menuButtons) {
                            val rawText = when (val name = button.title) {
                                is CJText.Raw -> name.text
                                is CJText.Res -> stringResource(name.res)
                            }
                            CJContextMenuButton(text = rawText, onClick = button.onClick)
                        }
                    }
                }
            }
        }
    }
}