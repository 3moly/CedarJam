package com.moly3.cedarjam.pages.page_workspace.func

import kotlin.collections.iterator

fun detectTabResizeZone(
    menuWidth: Float,      // in dp
    tabWeights: Map<Int, Float>,
    mouseOffsetX: Float,   // in dp
    screenWidth: Float,    // in dp
    divideSize: Float = 5f,
    isLog: Boolean
): Int? {
    val tabsAreaWidth = screenWidth - menuWidth
    if (isLog) {
        println("screenWidth: $screenWidth menuWidth: $menuWidth tabsAreaWidth $tabsAreaWidth")
    }

    if (tabsAreaWidth <= 0f) return null

    val totalWeight = tabWeights.values.sum()
    if (totalWeight <= 0f) return null

    var currentX = menuWidth  // tabs start AFTER the menu

    for ((tabId, weight) in tabWeights) {
        val tabWidth = tabsAreaWidth * (weight / totalWeight)
        val tabRight = currentX + tabWidth

        if(isLog){
            println("tabId: $tabId tabWidth: $tabWidth tabRightX: $tabRight mouse: $mouseOffsetX")
        }

        // Check if mouse is in the resize zone (last divideSize dp of this tab)
        if (mouseOffsetX in (tabRight - divideSize)..tabRight) {
            return tabId
        }

        currentX = tabRight
    }

    return null
}