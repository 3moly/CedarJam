package com.moly3.cedarjam.core.ui.uikit.v4

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// ─────────────────────────────────────────────────────────────────────────────
// Model
// ─────────────────────────────────────────────────────────────────────────────

/** Every item must expose its pixel size so no measurement pass is ever needed. */
data class FlowItemSize(val widthPx: Int, val heightPx: Int)

// ─────────────────────────────────────────────────────────────────────────────
// Public API
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Truly lazy FlowRow for Compose Multiplatform.
 *
 * - Caller supplies **width + height** for each item → zero measurement overhead.
 * - Only rows that scroll into view are composed.
 * - Items animate when they change rows (container resize → re-flow).
 *
 * @param items          Data list.
 * @param key            Stable key per item (required for correct animation).
 * @param itemSize       Returns pixel width + height for a given item.
 * @param modifier       Applied to the root Box.
 * @param state          Scroll state.
 * @param horizontalGap  Horizontal spacing between items in a row.
 * @param verticalGap    Vertical spacing between rows.
 * @param itemContent    Composable for each item; [Modifier.animateFlowItem] is available.
 */
@Composable
fun <T> LazyFlowRowV4(
    items: List<T>,
    key: (T) -> Any,
    itemSize: (T) -> FlowItemSize,
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    horizontalGap: Dp = 8.dp,
    verticalGap: Dp = 8.dp,
    itemContent: @Composable FlowItemScope.(item: T) -> Unit,
) {
    val density = LocalDensity.current
    val gapPx = remember(horizontalGap, density) {
        with(density) { horizontalGap.roundToPx() }
    }

    var containerWidthPx by remember { mutableStateOf(0) }

    // key → row index tracked across recompositions for slide animation
    val rowTracker = remember { mutableStateMapOf<Any, Int>() }

    // Re-bucket whenever items list or container width changes
    val rows: List<List<T>> by remember(items, containerWidthPx) {
        derivedStateOf {
            if (containerWidthPx <= 0) emptyList()
            else buildFlowRows(items, containerWidthPx, gapPx) { itemSize(it).widthPx }
        }
    }

    Box(modifier = modifier.onSizeChanged { containerWidthPx = it.width }) {
        LazyColumn(state = state, modifier = Modifier.fillMaxSize()) {
            rows.forEachIndexed { rowIndex, rowItems ->
                // Stable row key: joined item keys
                val rowKey = rowItems.joinToString(separator = "|") { key(it).toString() }

                item(key = rowKey) {
                    // Row height = tallest item in this row
                    val rowHeightPx = rowItems.maxOf { itemSize(it).heightPx }
                    val rowHeightDp = with(density) { rowHeightPx.toDp() }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(rowHeightDp),
                        horizontalArrangement = Arrangement.spacedBy(horizontalGap),
                    ) {
                        rowItems.forEach { item ->
                            val k = key(item)
                            val prevRow = rowTracker[k]
                            val rowDelta = (prevRow ?: rowIndex) - rowIndex

                            SideEffect { rowTracker[k] = rowIndex }

                            val size = itemSize(item)
                            val scope = rememberFlowItemScope(
                                isNew    = prevRow == null,
                                rowDelta = rowDelta,
                            )

                            Box(
                                modifier = Modifier
                                    .width(with(density) { size.widthPx.toDp() })
                                    .fillMaxHeight(),
                            ) {
                                scope.itemContent(item)
                            }
                        }
                    }

                    if (rowIndex < rows.lastIndex) {
                        Spacer(Modifier.height(verticalGap))
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Scope
// ─────────────────────────────────────────────────────────────────────────────

/** Receiver for [LazyFlowRow]'s itemContent lambda. */
interface FlowItemScope {
    /**
     * Fade-in for new items; vertical slide when an item moves to a different row
     * (e.g. container resize → re-flow).
     */
    fun Modifier.animateFlowItem(
        fadeSpec: FiniteAnimationSpec<Float>  = spring(stiffness = Spring.StiffnessMediumLow),
        slideSpec: FiniteAnimationSpec<Float> = spring(stiffness = Spring.StiffnessMediumLow),
    ): Modifier
}

// ─────────────────────────────────────────────────────────────────────────────
// Internal scope impl
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun rememberFlowItemScope(
    isNew: Boolean,
    rowDelta: Int,
): FlowItemScope = remember(isNew, rowDelta) {
    FlowItemScopeImpl(isNew = isNew, rowDelta = rowDelta)
}

private class FlowItemScopeImpl(
    private val isNew: Boolean,
    private val rowDelta: Int,
) : FlowItemScope {

    override fun Modifier.animateFlowItem(
        fadeSpec: FiniteAnimationSpec<Float>,
        slideSpec: FiniteAnimationSpec<Float>,
    ): Modifier = this.then(
        Modifier.composed {
            // Fade-in for brand-new items
            val alpha = if (isNew) {
                remember { Animatable(0f) }.also { a ->
                    LaunchedEffect(Unit) { a.animateTo(1f, fadeSpec) }
                }
            } else null

            // Vertical slide when the item moved rows (re-flow on resize)
            val translateY = if (rowDelta != 0) {
                // 1 row ≈ 48 px — good enough for a spring settle
                remember(rowDelta) { Animatable(rowDelta * 48f) }.also { a ->
                    LaunchedEffect(rowDelta) { a.animateTo(0f, slideSpec) }
                }
            } else null

            if (alpha == null && translateY == null) return@composed this

            graphicsLayer {
                if (alpha != null)      this.alpha      = alpha.value
                if (translateY != null) this.translationY = translateY.value
            }
        }
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Internal — greedy row bucketing
// ─────────────────────────────────────────────────────────────────────────────

private fun <T> buildFlowRows(
    items: List<T>,
    containerWidth: Int,
    gapPx: Int,
    widthOf: (T) -> Int,
): List<List<T>> {
    val rows = mutableListOf<MutableList<T>>()
    var currentRow = mutableListOf<T>()
    var usedWidth = 0

    for (item in items) {
        val w = widthOf(item).coerceAtLeast(1)
        val needed = if (currentRow.isEmpty()) w else usedWidth + gapPx + w

        if (currentRow.isNotEmpty() && needed > containerWidth) {
            rows += currentRow
            currentRow = mutableListOf(item)
            usedWidth = w
        } else {
            currentRow += item
            usedWidth = needed
        }
    }
    if (currentRow.isNotEmpty()) rows += currentRow
    return rows
}