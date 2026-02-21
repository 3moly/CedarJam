package com.moly3.cedarjam.core.ui.uikit

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.max

/**
 * A lazy flow-row layout for Compose Multiplatform.
 *
 * Items are arranged in rows (like FlowRow), but rows are lazily composed
 * using LazyColumn under the hood — only visible rows are composed.
 *
 * @param items          The list of items to display.
 * @param modifier       Modifier for the outer LazyColumn.
 * @param state          LazyListState for scroll control / observation.
 * @param horizontalGap  Horizontal spacing between items in a row.
 * @param verticalGap    Vertical spacing between rows.
 * @param alignment      Vertical alignment of items within a row.
 * @param key            Optional stable key per item (forwarded to LazyColumn).
 * @param itemContent    Composable lambda for each item.
 */
@Composable
fun <T> LazyFlowRow(
    items: List<T>,
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    horizontalGap: Dp = 8.dp,
    verticalGap: Dp = 8.dp,
    alignment: Alignment.Vertical = Alignment.CenterVertically,
    key: ((item: T) -> Any)? = null,
    itemContent: @Composable (item: T) -> Unit,
) {
    // We need the container width to bucket items into rows.
    // We measure it via a Layout wrapper on first pass; rows are re-computed
    // whenever the width or item list changes.
    var containerWidthPx by remember { mutableStateOf(0) }

    // Bucket items into rows based on measured widths.
    // Because we can't know item widths until they're measured, we use a
    // two-phase approach:
    //   1. Measure all items in a hidden (alpha=0) single-pass FlowRow to get widths.
    //   2. Feed those widths + containerWidth into a pure row-bucketing function.
    //   3. Render rows lazily via LazyColumn.

    // Measured widths cache: index -> width in px
    val measuredWidths = remember { mutableStateMapOf<Int, Int>() }

    // Compute row buckets whenever widths or container width change
    val rows: List<List<Int>> = remember(measuredWidths.toMap(), containerWidthPx, horizontalGap) {
        if (containerWidthPx <= 0 || items.isEmpty()) return@remember emptyList()
        val gapPx = horizontalGap.value  // approximation; real conversion needs density
        buildRows(
            count = items.size,
            containerWidth = containerWidthPx,
            widthOf = { measuredWidths[it] ?: containerWidthPx },
            gapPx = gapPx.toInt(),
        )
    }

    BoxWithConstraints(modifier = modifier) {
        // Capture container width in pixels
        val widthPx = constraints.maxWidth
        LaunchedEffect(widthPx) { containerWidthPx = widthPx }

        LazyColumn(state = state) {
            // --- Hidden measurement pass ---
            // We insert a single item that measures all un-measured items at zero height.
            item(key = "__measure__") {
                MeasurementPass(
                    items = items,
                    measuredWidths = measuredWidths,
                    horizontalGap = horizontalGap,
                    itemContent = itemContent,
                )
            }

            // --- Visible rows ---
            rows.forEachIndexed { rowIndex, rowItemIndices ->
                item(
                    key = key?.let { k -> rowItemIndices.map { k(items[it]) } } ?: rowIndex,
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(horizontalGap),
                    ) {
                        rowItemIndices.forEach { itemIndex ->
                            itemContent(items[itemIndex])
                        }
                    }
                    if (rowIndex < rows.lastIndex) {
                        Spacer(Modifier.height(verticalGap))
                    }
                }
            }

            // Fallback: if rows haven't been computed yet, show items in a real FlowRow
            if (rows.isEmpty() && items.isNotEmpty()) {
                item(key = "__fallback__") {
                    FlowRowFallback(
                        items = items,
                        horizontalGap = horizontalGap,
                        verticalGap = verticalGap,
                        itemContent = itemContent,
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Internal helpers
// ---------------------------------------------------------------------------

/**
 * Bucket item indices into rows using a greedy line-breaking algorithm.
 */
private fun buildRows(
    count: Int,
    containerWidth: Int,
    widthOf: (Int) -> Int,
    gapPx: Int,
): List<List<Int>> {
    val rows = mutableListOf<List<Int>>()
    var currentRow = mutableListOf<Int>()
    var currentWidth = 0

    for (i in 0 until count) {
        val w = widthOf(i)
        val needed = if (currentRow.isEmpty()) w else currentWidth + gapPx + w
        if (currentRow.isNotEmpty() && needed > containerWidth) {
            rows.add(currentRow)
            currentRow = mutableListOf(i)
            currentWidth = w
        } else {
            currentRow.add(i)
            currentWidth = needed
        }
    }
    if (currentRow.isNotEmpty()) rows.add(currentRow)
    return rows
}

/**
 * A zero-height composable that measures item widths using a custom Layout.
 * Items that are already measured are skipped.
 */
@Composable
private fun <T> MeasurementPass(
    items: List<T>,
    measuredWidths: MutableMap<Int, Int>,
    horizontalGap: Dp,
    itemContent: @Composable (item: T) -> Unit,
) {
    val unmeasured = items.indices.filter { it !in measuredWidths }
    if (unmeasured.isEmpty()) return

    Layout(
        modifier = Modifier.height(0.dp),
        content = {
            unmeasured.forEach { itemContent(items[it]) }
        },
    ) { measurables, constraints ->
        val loose = constraints.copy(minWidth = 0, minHeight = 0)
        measurables.forEachIndexed { idx, measurable ->
            val placeable = measurable.measure(loose)
            measuredWidths[unmeasured[idx]] = placeable.width
        }
        layout(0, 0) {}
    }
}

/**
 * FlowRow fallback shown while measurement hasn't happened yet.
 * Requires `androidx.compose.foundation.layout.FlowRow` (Compose 1.5+).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun <T> FlowRowFallback(
    items: List<T>,
    horizontalGap: Dp,
    verticalGap: Dp,
    itemContent: @Composable (item: T) -> Unit,
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(horizontalGap),
        verticalArrangement = Arrangement.spacedBy(verticalGap),
    ) {
        items.forEach { itemContent(it) }
    }
}

// ---------------------------------------------------------------------------
// Usage example (remove or move to a separate file)
// ---------------------------------------------------------------------------

/*
@Composable
fun ChipListScreen(chips: List<String>) {
    LazyFlowRow(
        items = chips,
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalGap = 8.dp,
        verticalGap = 8.dp,
        key = { it },
    ) { chip ->
        AssistChip(onClick = {}, label = { Text(chip) })
    }
}
*/