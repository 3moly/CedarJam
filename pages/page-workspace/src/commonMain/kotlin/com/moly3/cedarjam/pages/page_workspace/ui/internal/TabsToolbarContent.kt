package com.moly3.cedarjam.pages.page_workspace.ui.internal

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import com.moly3.cedarjam.core.domain.func.getPlatform
import com.moly3.cedarjam.core.domain.model.Platform
import com.moly3.cedarjam.core.ui.ToolbarHeight
import com.moly3.cedarjam.core.ui.func.isCompactUI
import com.moly3.cedarjam.core.ui.func.rememberWindowSize
import com.moly3.cedarjam.core.ui.model.JvmToolbarState
import com.moly3.cedarjam.core.ui.model.WindowSize
import com.moly3.cedarjam.core.ui.onPointerEvent
import com.moly3.cedarjam.core.ui.uikit.CJDraggableArea
import com.moly3.cedarjam.core.ui.uikit.CJIcon
import vectors.BarLeft
import com.moly3.cedarjam.pages.page_tabs.TabsComponent
import com.moly3.cedarjam.pages.page_tabs.ui.TabsPage
import com.moly3.cedarjam.pages.page_workspace.Intent
import com.moly3.cedarjam.pages.page_workspace.State
import com.moly3.cedarjam.pages.page_workspace.WorkspaceComponent

@Composable
fun TabsToolbarContent(
    modifier: Modifier = Modifier,
    state: State,
    items: WorkspaceComponent.Children<*, TabsComponent>,
    updatedScreenWidth: Float?,
    toolbarState: JvmToolbarState,
    onIntent: (Intent) -> Unit,
    component: WorkspaceComponent
) {
    val windowSize by rememberWindowSize()
    val density = LocalDensity.current.density
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
            (menuWidth - toolbarState.controlsWidthToCut).coerceAtLeast(
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

    CJDraggableArea(modifier = modifier.then(toolbarState.modifier).height(ToolbarHeight.dp)) {
        Box(it.fillMaxWidth()) {
            Row(
                Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier.fillMaxHeight().let {
                        if (windowSize != WindowSize.Compact) {
                            it.width(menuWidthCustomEnd)
                        } else {
                            if (state.isMenuOpened) {
                                it.fillMaxWidth()
                            } else {
                                it
                            }
                        }
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(end = 12.dp)
                            .align(Alignment.CenterEnd),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (state.isMenuOpened) {
//                            CJIcon(
//                                painter = rememberVectorPainter(TrashEmpty),
//                                onClick = {
//                                    onIntent(Intent.Sync)
//                                }
//                            )
                            Box(Modifier.weight(1f))
                        }
                        CJIcon(
                            painter = rememberVectorPainter(BarLeft),
                            onClick = {
                                onIntent(Intent.SetIsFullMenu(!state.isMenuOpened))
                            }
                        )
                    }

                }

                if (!isCompactUI()) {
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
    }
}