package com.moly3.cedarjam.core.ui.func

import kotlin.math.absoluteValue


fun recalculateTabWeights2(
    screenWidth: Float,
    draggedTabIndex: Int,
    dragMovementInWidth: Float,
    minWidthOfTab: Float,
    tabWeights: Map<Int, Float>,
    grabPositionRatio: Float // 0.0 = left edge, 1.0 = right edge of the tab
): Map<Int, Float> {
    if (tabWeights.isEmpty() || dragMovementInWidth == 0f) {
        return tabWeights
    }

    val newWeights = tabWeights.toMutableMap()
    val totalWeight = tabWeights.values.sum()
    val minWeight = (minWidthOfTab / screenWidth) * totalWeight

    val currentWeight = newWeights[draggedTabIndex] ?: return tabWeights
    val weightChange = (dragMovementInWidth / screenWidth) * totalWeight

    // Determine which side to resize based on grab position and drag direction
    val isGrabbingRightSide = grabPositionRatio > 0.5f
    val isDraggingRight = dragMovementInWidth > 0

    val (primarySide, secondarySide) = when {
        // Grabbed right side, dragging right -> expand right, take from right neighbors
        isGrabbingRightSide && isDraggingRight ->
            Pair(tabWeights.keys.filter { it > draggedTabIndex }.sorted(),
                tabWeights.keys.filter { it < draggedTabIndex }.sortedDescending())

        // Grabbed right side, dragging left -> shrink from right, give to right neighbors
        isGrabbingRightSide && !isDraggingRight ->
            Pair(tabWeights.keys.filter { it > draggedTabIndex }.sorted(),
                tabWeights.keys.filter { it < draggedTabIndex }.sortedDescending())

        // Grabbed left side, dragging right -> expand left, give to left neighbors
        !isGrabbingRightSide && isDraggingRight ->
            Pair(tabWeights.keys.filter { it < draggedTabIndex }.sortedDescending(),
                tabWeights.keys.filter { it > draggedTabIndex }.sorted())

        // Grabbed left side, dragging left -> shrink from left, take from left neighbors
        else ->
            Pair(tabWeights.keys.filter { it < draggedTabIndex }.sortedDescending(),
                tabWeights.keys.filter { it > draggedTabIndex }.sorted())
    }

    // Calculate new weight for dragged tab
    val newDraggedWeight = (currentWeight + weightChange).coerceIn(minWeight, totalWeight - (tabWeights.size - 1) * minWeight)
    val actualWeightChange = newDraggedWeight - currentWeight

    if (actualWeightChange.absoluteValue < 0.001f) {
        return tabWeights
    }

    // Distribute weight change among tabs on the primary side
    var remainingChange = -actualWeightChange
    val adjustments = mutableMapOf<Int, Float>()

    // First, try to adjust tabs on the primary side (the side we're resizing from)
    for (tabIndex in primarySide) {
        if (remainingChange.absoluteValue < 0.001f) break

        val tabWeight = newWeights[tabIndex] ?: continue
        val maxAdjustment = if (remainingChange > 0) {
            // Need to add weight to this tab
            Float.MAX_VALUE
        } else {
            // Need to remove weight from this tab
            tabWeight - minWeight
        }

        val adjustment = if (remainingChange > 0) {
            minOf(maxAdjustment, remainingChange)
        } else {
            maxOf(-maxAdjustment, remainingChange)
        }

        if (adjustment.absoluteValue > 0.001f) {
            adjustments[tabIndex] = adjustment
            remainingChange -= adjustment
        }
    }

    // If primary side can't absorb all the change, try secondary side
    if (remainingChange.absoluteValue > 0.001f) {
        for (tabIndex in secondarySide) {
            if (remainingChange.absoluteValue < 0.001f) break

            val tabWeight = newWeights[tabIndex] ?: continue
            val maxAdjustment = if (remainingChange > 0) {
                Float.MAX_VALUE
            } else {
                tabWeight - minWeight
            }

            val adjustment = if (remainingChange > 0) {
                minOf(maxAdjustment, remainingChange)
            } else {
                maxOf(-maxAdjustment, remainingChange)
            }

            if (adjustment.absoluteValue > 0.001f) {
                adjustments[tabIndex] = adjustment
                remainingChange -= adjustment
            }
        }
    }

    // Apply all adjustments
    newWeights[draggedTabIndex] = newDraggedWeight
    adjustments.forEach { (index, change) ->
        newWeights[index] = (newWeights[index] ?: 0f) + change
    }

    // Ensure no weight goes below minimum
    newWeights.forEach { (index, weight) ->
        if (weight < minWeight) {
            newWeights[index] = minWeight
        }
    }

    return newWeights
}


fun recalculateTabWeights3(
    screenWidth: Float,
    draggedTabIndex: Int,
    dragMovementInWidth: Float,
    minWidthOfTab: Float,
    tabWeights: Map<Int, Float>,
    grabPositionRatio: Float // 0.0 = left edge, 1.0 = right edge of the tab
): Map<Int, Float> {
    if (tabWeights.isEmpty() || dragMovementInWidth == 0f) {
        return tabWeights
    }

    val newWeights = tabWeights.toMutableMap()
    val totalWeight = tabWeights.values.sum()
    val minWeight = (minWidthOfTab / screenWidth) * totalWeight

    val currentWeight = newWeights[draggedTabIndex] ?: return tabWeights
    val weightChange = (dragMovementInWidth / screenWidth) * totalWeight

    // Determine which side to resize based on grab position and drag direction
    val isGrabbingRightSide = grabPositionRatio > 0.5f
    val isDraggingRight = dragMovementInWidth > 0

    val (primarySide, secondarySide) = when {
        // Grabbed right side, dragging right -> expand right, take from right neighbors
        isGrabbingRightSide && isDraggingRight ->
            Pair(tabWeights.keys.filter { it > draggedTabIndex }.sorted(),
                tabWeights.keys.filter { it < draggedTabIndex }.sortedDescending())

        // Grabbed right side, dragging left -> shrink from right, give to right neighbors
        isGrabbingRightSide && !isDraggingRight ->
            Pair(tabWeights.keys.filter { it > draggedTabIndex }.sorted(),
                tabWeights.keys.filter { it < draggedTabIndex }.sortedDescending())

        // Grabbed left side, dragging right -> expand left, give to left neighbors
        !isGrabbingRightSide && isDraggingRight ->
            Pair(tabWeights.keys.filter { it < draggedTabIndex }.sortedDescending(),
                tabWeights.keys.filter { it > draggedTabIndex }.sorted())

        // Grabbed left side, dragging left -> shrink from left, take from left neighbors
        else ->
            Pair(tabWeights.keys.filter { it < draggedTabIndex }.sortedDescending(),
                tabWeights.keys.filter { it > draggedTabIndex }.sorted())
    }

    // Calculate new weight for dragged tab
    val newDraggedWeight = (currentWeight + weightChange).coerceIn(minWeight, totalWeight - (tabWeights.size - 1) * minWeight)
    val actualWeightChange = newDraggedWeight - currentWeight

    if (actualWeightChange.absoluteValue < 0.001f) {
        return tabWeights
    }

    // Distribute weight change among tabs on the primary side
    var remainingChange = -actualWeightChange
    val adjustments = mutableMapOf<Int, Float>()

    // First, try to adjust tabs on the primary side (the side we're resizing from)
    for (tabIndex in primarySide) {
        if (remainingChange.absoluteValue < 0.001f) break

        val tabWeight = newWeights[tabIndex] ?: continue
        val maxAdjustment = if (remainingChange > 0) {
            // Need to add weight to this tab
            Float.MAX_VALUE
        } else {
            // Need to remove weight from this tab
            tabWeight - minWeight
        }

        val adjustment = if (remainingChange > 0) {
            minOf(maxAdjustment, remainingChange)
        } else {
            maxOf(-maxAdjustment, remainingChange)
        }

        if (adjustment.absoluteValue > 0.001f) {
            adjustments[tabIndex] = adjustment
            remainingChange -= adjustment
        }
    }

    // If primary side can't absorb all the change, try secondary side
    if (remainingChange.absoluteValue > 0.001f) {
        for (tabIndex in secondarySide) {
            if (remainingChange.absoluteValue < 0.001f) break

            val tabWeight = newWeights[tabIndex] ?: continue
            val maxAdjustment = if (remainingChange > 0) {
                Float.MAX_VALUE
            } else {
                tabWeight - minWeight
            }

            val adjustment = if (remainingChange > 0) {
                minOf(maxAdjustment, remainingChange)
            } else {
                maxOf(-maxAdjustment, remainingChange)
            }

            if (adjustment.absoluteValue > 0.001f) {
                adjustments[tabIndex] = adjustment
                remainingChange -= adjustment
            }
        }
    }

    // Apply all adjustments
    newWeights[draggedTabIndex] = newDraggedWeight
    adjustments.forEach { (index, change) ->
        newWeights[index] = (newWeights[index] ?: 0f) + change
    }

    // Ensure no weight goes below minimum
    newWeights.forEach { (index, weight) ->
        if (weight < minWeight) {
            newWeights[index] = minWeight
        }
    }

    return newWeights
}

fun recalculateTabWeights(
    screenWidth: Float,
    draggedTabIndex: Int,
    dragMovementInWidth: Float,
    minWidthOfTab: Float,
    tabWeights: Map<Int, Float>,
    grabPositionRatio: Float // 0.0 = left edge, 1.0 = right edge of the tab
): Map<Int, Float> {
    if (tabWeights.isEmpty() || dragMovementInWidth == 0f) {
        return tabWeights
    }

    val newWeights = tabWeights.toMutableMap()
    val totalWeight = tabWeights.values.sum()
    val minWeight = (minWidthOfTab / screenWidth) * totalWeight

    val currentWeight = newWeights[draggedTabIndex] ?: return tabWeights
    val requestedWeightChange = (dragMovementInWidth / screenWidth) * totalWeight

    // Determine which side to resize based on grab position
    val isGrabbingRightSide = grabPositionRatio > 0.5f
    val isDraggingRight = dragMovementInWidth > 0

    // Determine which tabs will be directly affected (the side being resized from)
    val directlyAffectedTabs = when {
        isGrabbingRightSide && isDraggingRight ->
            tabWeights.keys.filter { it > draggedTabIndex }.sorted()
        isGrabbingRightSide && !isDraggingRight ->
            tabWeights.keys.filter { it > draggedTabIndex }.sorted()
        !isGrabbingRightSide && isDraggingRight ->
            tabWeights.keys.filter { it < draggedTabIndex }.sortedDescending()
        else ->
            tabWeights.keys.filter { it < draggedTabIndex }.sortedDescending()
    }

    // Check if we're at a boundary (no tabs on the affected side)
    if (directlyAffectedTabs.isEmpty()) {
        // At boundary - can't resize in this direction
        return tabWeights
    }

    // Calculate how much weight can actually be transferred
    var availableWeight = 0f

    if (requestedWeightChange > 0) {
        // Expanding dragged tab: check how much we can take from affected tabs
        for (tabIndex in directlyAffectedTabs) {
            val tabWeight = newWeights[tabIndex] ?: continue
            availableWeight += (tabWeight - minWeight).coerceAtLeast(0f)
        }

        // If no weight available, don't expand
        if (availableWeight < 0.001f) {
            return tabWeights
        }
    } else {
        // Shrinking dragged tab: check if we can shrink more
        val minAllowedWeight = minWeight
        if (currentWeight <= minAllowedWeight) {
            // Already at minimum, can't shrink further
            return tabWeights
        }
        availableWeight = currentWeight - minAllowedWeight
    }

    // Calculate actual weight change (limited by available space)
    val actualWeightChange = if (requestedWeightChange > 0) {
        minOf(requestedWeightChange, availableWeight)
    } else {
        maxOf(requestedWeightChange, -availableWeight)
    }

    if (actualWeightChange.absoluteValue < 0.001f) {
        return tabWeights
    }

    // Apply weight change to dragged tab
    newWeights[draggedTabIndex] = currentWeight + actualWeightChange

    // Distribute the opposite weight change among affected tabs
    var remainingChange = -actualWeightChange
    val adjustments = mutableMapOf<Int, Float>()

    for (tabIndex in directlyAffectedTabs) {
        if (remainingChange.absoluteValue < 0.001f) break

        val tabWeight = newWeights[tabIndex] ?: continue

        val adjustment = if (remainingChange > 0) {
            // Need to add weight to this tab (dragged tab is shrinking)
            val sharePerTab = remainingChange / (directlyAffectedTabs.size - adjustments.size)
            sharePerTab
        } else {
            // Need to remove weight from this tab (dragged tab is expanding)
            val maxReduction = tabWeight - minWeight
            -minOf(maxReduction, remainingChange.absoluteValue)
        }

        if (adjustment.absoluteValue > 0.001f) {
            adjustments[tabIndex] = adjustment
            remainingChange -= adjustment
        }
    }

    // Apply all adjustments
    adjustments.forEach { (index, change) ->
        newWeights[index] = (newWeights[index] ?: 0f) + change
    }

    return newWeights
}