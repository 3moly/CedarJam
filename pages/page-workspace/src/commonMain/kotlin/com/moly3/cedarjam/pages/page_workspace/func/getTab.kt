package com.moly3.cedarjam.pages.page_workspace.func

import androidx.compose.ui.geometry.Offset
import com.moly3.cedarjam.pages.page_workspace.ui.internal.MenuCoveredId

fun getTab(
    screenWidth: Float,  // in pixels
    menuWidth: Float,    // in dp
    updatedTabSizes: Map<Int, Float>,
    cursorOffset: Offset,  // in pixels
    divideSize: Float = 5f,
    isLog: Boolean
): Int? {
    // Convert everything to dp for consistent comparison
    val mouseX = cursorOffset.x
    val screenWidthDp = screenWidth

    // Check if hovering over menu divider
    if (mouseX in (menuWidth - divideSize)..menuWidth) {
        return MenuCoveredId
    }
    // Check if hovering over tab divider
    return detectTabResizeZone(
        screenWidth = screenWidthDp,  // Now in dp
        menuWidth = menuWidth,         // Already in dp
        tabWeights = updatedTabSizes,
        mouseOffsetX = mouseX,         // Now in dp
        divideSize = divideSize,
        isLog = isLog
    )
}