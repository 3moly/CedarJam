package com.moly3.cedarjam.core.ui.tests

import com.moly3.cedarjam.core.ui.func.recalculateTabWeights
import kotlin.test.Test

class RecalculateTabWeightsTest {
    @Test
    fun test() {
        println("=== Tab Weight Recalculation Demo ===\n")

        val screenWidth = 1000f
        val minWidthOfTab = 100f

        // Test 1: Drag middle tab to the right
        println("Test 1: Drag tab 1 to the right (+50px)")
        val initialWeights1 = mapOf(0 to 1f, 1 to 1f, 2 to 1f, 3 to 1f)
        println("Initial weights: $initialWeights1")
        println("Initial widths: ${calculateWidths(initialWeights1, screenWidth)}")

        val result1 = recalculateTabWeights(
            screenWidth = screenWidth,
            draggedTabIndex = 1,
            dragMovementInWidth = 50f,
            minWidthOfTab = minWidthOfTab,
            tabWeights = initialWeights1,
            grabPositionRatio = 0f
        )
        println("New weights: $result1")
        println("New widths: ${calculateWidths(result1, screenWidth)}")
        println("Total weight: ${result1.values.sum()}\n")

        // Test 2: Drag first tab to the right
        println("Test 2: Drag tab 0 to the right (+100px)")
        val initialWeights2 = mapOf(0 to 1f, 1 to 1f, 2 to 1f)
        println("Initial weights: $initialWeights2")
        println("Initial widths: ${calculateWidths(initialWeights2, screenWidth)}")

        val result2 = recalculateTabWeights(
            screenWidth = screenWidth,
            draggedTabIndex = 0,
            dragMovementInWidth = 100f,
            minWidthOfTab = minWidthOfTab,
            tabWeights = initialWeights2,
            grabPositionRatio = 0f
        )
        println("New weights: $result2")
        println("New widths: ${calculateWidths(result2, screenWidth)}")
        println("Total weight: ${result2.values.sum()}\n")

        // Test 3: Drag last tab to the left
        println("Test 3: Drag tab 3 to the left (-75px)")
        val initialWeights3 = mapOf(0 to 1f, 1 to 1f, 2 to 1f, 3 to 1f)
        println("Initial weights: $initialWeights3")
        println("Initial widths: ${calculateWidths(initialWeights3, screenWidth)}")

        val result3 = recalculateTabWeights(
            screenWidth = screenWidth,
            draggedTabIndex = 3,
            dragMovementInWidth = -75f,
            minWidthOfTab = minWidthOfTab,
            tabWeights = initialWeights3,
            grabPositionRatio = 0f
        )
        println("New weights: $result3")
        println("New widths: ${calculateWidths(result3, screenWidth)}")
        println("Total weight: ${result3.values.sum()}\n")

        // Test 4: Try to drag beyond minimum width
        println("Test 4: Try to drag tab 1 left beyond minimum (-200px)")
        val initialWeights4 = mapOf(0 to 0.5f, 1 to 1f, 2 to 1.5f)
        println("Initial weights: $initialWeights4")
        println("Initial widths: ${calculateWidths(initialWeights4, screenWidth)}")

        val result4 = recalculateTabWeights(
            screenWidth = screenWidth,
            draggedTabIndex = 1,
            dragMovementInWidth = -200f,
            minWidthOfTab = minWidthOfTab,
            tabWeights = initialWeights4,
            grabPositionRatio = 0f
        )
        println("New weights: $result4")
        println("New widths: ${calculateWidths(result4, screenWidth)}")
        println("Total weight: ${result4.values.sum()}\n")
    }

    fun calculateWidths(weights: Map<Int, Float>, screenWidth: Float): Map<Int, Float> {
        val totalWeight = weights.values.sum()
        return weights.mapValues { (screenWidth * it.value) / totalWeight }
    }
}