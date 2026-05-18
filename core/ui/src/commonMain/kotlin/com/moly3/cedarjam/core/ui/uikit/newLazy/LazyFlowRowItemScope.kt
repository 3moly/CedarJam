package com.moly3.cedarjam.core.ui.uikit.newLazy

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp

// ---------------------------------------------------------------------------
// Public API
// ---------------------------------------------------------------------------

/**
 * Receiver scope passed to [LazyFlowRowV2]'s itemContent lambda.
 *
 * Call [Modifier.animateItem] inside your item composable to opt into
 * enter / exit / placement animations — exactly like LazyColumn's animateItem().
 */
interface LazyFlowRowItemScope {

    /**
     * Animates this item when it:
     *  - **appears**   → fades in  (fadeInSpec)
     *  - **disappears** → use [AnimatedLazyFlowRowItem] for exit animations
     *  - **moves** to a different row → slides vertically (placementSpec)
     *
     * Mirrors the signature of `LazyColumn`'s `Modifier.animateItem(...)`.
     *
     * @param fadeInSpec     Alpha animation for newly added items. null = no fade.
     * @param fadeOutSpec    Reserved for future use / [AnimatedLazyFlowRowItem].
     * @param placementSpec  IntOffset spring used for cross-row movement.
     */
    fun Modifier.animateItem(
        fadeInSpec: FiniteAnimationSpec<Float>? =
            spring(stiffness = Spring.StiffnessMediumLow),
        fadeOutSpec: FiniteAnimationSpec<Float>? =
            spring(stiffness = Spring.StiffnessMediumLow),
        placementSpec: FiniteAnimationSpec<IntOffset>? = spring(
            stiffness = Spring.StiffnessMediumLow,
            visibilityThreshold = IntOffset.VisibilityThreshold,
        ),
    ): Modifier
}

// ---------------------------------------------------------------------------
// LazyFlowRow
// ---------------------------------------------------------------------------

/**
 * A lazily-composed flow-row layout for Compose Multiplatform.
 *
 * Items are arranged in rows (like `FlowRow`), but rows are composed lazily
 * via `LazyColumn` — only visible rows are in the composition tree.
 *
 * Individual items opt into animations by calling
 * `Modifier.animateItem()` inside the [itemContent] lambda.
 *
 * ### Quick start
 * ```kotlin
 * LazyFlowRow(
 *     items = chips,
 *     key   = { it.id },
 * ) { chip ->
 *     AssistChip(
 *         label    = { Text(chip.label) },
 *         modifier = Modifier.animateItem(),   // ← opt-in
 *     )
 * }
 * ```
 *
 * @param items         The list of items to display.
 * @param modifier      Modifier applied to the outer LazyColumn.
 * @param state         LazyListState for scroll control / observation.
 * @param horizontalGap Horizontal spacing between items in the same row.
 * @param verticalGap   Vertical spacing between rows.
 * @param key           Stable key extractor. **Required for animations to work correctly.**
 * @param itemContent   Composable lambda. Receiver is [LazyFlowRowItemScope].
 */
@Composable
fun <T> LazyFlowRowV2(
    items: List<T>,
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    horizontalGap: Dp = 8.dp,
    verticalGap: Dp = 8.dp,
    key: ((item: T) -> Any)? = null,
    itemContent: @Composable LazyFlowRowItemScope.(item: T) -> Unit,
) {
    var containerWidthPx by remember { mutableStateOf(0) }

    // Stable key helper
    val itemKey: (T) -> Any = remember(key) {
        if (key != null) key else { item -> items.indexOf(item).toString() }
    }

    // key → measured width in px
    val measuredWidths = remember { mutableStateMapOf<Any, Int>() }

    // key → last known row index (for placement animation direction)
    val rowTracker = remember { mutableStateMapOf<Any, Int>() }

    // Recompute row buckets whenever layout or measurements change
    val rows: List<List<T>> by remember(measuredWidths.size, containerWidthPx, items) {
        derivedStateOf {
            if (containerWidthPx <= 0 || items.isEmpty()) return@derivedStateOf emptyList()
            buildRows(
                items = items,
                containerWidth = containerWidthPx,
                widthOf = { item -> measuredWidths[itemKey(item)] ?: containerWidthPx },
                gapPx = horizontalGap.value.toInt(),
            )
        }
    }

    BoxWithConstraints(modifier = modifier) {
        val widthPx = constraints.maxWidth
        LaunchedEffect(widthPx) { containerWidthPx = widthPx }

        LazyColumn(state = state, modifier = Modifier.fillMaxWidth()) {

            // ── Measurement pass (zero-height, invisible) ───────────────────
            item(key = "__lazyflowrow_measure__") {
                MeasurementPass(
                    items = items,
                    measuredWidths = measuredWidths,
                    itemKey = itemKey,
                    itemContent = { NoOpScope.itemContent(it) },
                )
            }

            // ── Fallback: show a real FlowRow before measurement is done ────
            if (rows.isEmpty() && items.isNotEmpty()) {
                item(key = "__lazyflowrow_fallback__") {
                    @OptIn(ExperimentalLayoutApi::class)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(horizontalGap),
                        verticalArrangement = Arrangement.spacedBy(verticalGap),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        items.forEach { NoOpScope.itemContent(it) }
                    }
                }
            }

            // ── Lazy rows ───────────────────────────────────────────────────
            rows.forEachIndexed { rowIndex, rowItems ->
                // Use a stable composite key so LazyColumn can animate row appearance
                val rowKey = rowItems.joinToString("|") { itemKey(it).toString() }

                item(key = rowKey) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(horizontalGap),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        rowItems.forEach { item ->
                            val k = itemKey(item)
                            val prevRow = rowTracker[k]
                            val isNew = prevRow == null
                            val moved = prevRow != null && prevRow != rowIndex

                            SideEffect { rowTracker[k] = rowIndex }

                            val scope = rememberItemAnimationScope(
                                isNew = isNew,
                                moved = moved,
                                rowDelta = (prevRow ?: rowIndex) - rowIndex,
                            )
                            scope.itemContent(item)
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

// ---------------------------------------------------------------------------
// AnimatedLazyFlowRowItem — exit animations
// ---------------------------------------------------------------------------

/**
 * Wraps an item in [AnimatedVisibility] to support **exit** animations when
 * the item is removed from the visible set (but still in the list).
 *
 * Pair with `Modifier.animateItem()` for full enter + exit + placement support:
 *
 * ```kotlin
 * LazyFlowRow(items, key = { it.id }) { item ->
 *     AnimatedLazyFlowRowItem(visible = item.id in activeIds) {
 *         MyChip(
 *             item,
 *             modifier = Modifier.animateItem()
 *         )
 *     }
 * }
 * ```
 */
@Composable
fun AnimatedLazyFlowRowItem(
    visible: Boolean,
    enter: EnterTransition = fadeIn() + expandHorizontally(),
    exit: ExitTransition = fadeOut() + shrinkHorizontally(),
    content: @Composable () -> Unit,
) {
    AnimatedVisibility(visible = visible, enter = enter, exit = exit) {
        content()
    }
}

// ---------------------------------------------------------------------------
// Internal: ItemAnimationScope
// ---------------------------------------------------------------------------

@Composable
private fun rememberItemAnimationScope(
    isNew: Boolean,
    moved: Boolean,
    rowDelta: Int,
): LazyFlowRowItemScope {
    // Capture values into a stable object for the scope
    return remember(isNew, moved, rowDelta) {
        ItemAnimationScope(isNew = isNew, moved = moved, rowDelta = rowDelta)
    }
}

private class ItemAnimationScope(
    private val isNew: Boolean,
    private val moved: Boolean,
    private val rowDelta: Int,   // positive = moved up, negative = moved down
) : LazyFlowRowItemScope {

    override fun Modifier.animateItem(
        fadeInSpec: FiniteAnimationSpec<Float>?,
        fadeOutSpec: FiniteAnimationSpec<Float>?,
        placementSpec: FiniteAnimationSpec<IntOffset>?,
    ): Modifier = this.composed {

        // ── Fade-in for newly added items ────────────────────────────────
        val alphaAnim = if (isNew && fadeInSpec != null) {
            remember { Animatable(0f) }.also { anim ->
                LaunchedEffect(Unit) { anim.animateTo(1f, fadeInSpec) }
            }
        } else null

        // ── Vertical slide for items that moved rows ─────────────────────
        // We estimate the start offset from rowDelta × a row height approximation.
        // The animation settles at 0 (natural position), so layout handles the rest.
        val translationAnim = if (moved && placementSpec != null && rowDelta != 0) {
            // Approximate: 48dp per row as a starting heuristic. You can expose
            // this as a parameter if you need exact control.
            val startPx = rowDelta * 48f
            remember { Animatable(startPx) }.also { anim ->
                LaunchedEffect(rowDelta) {
                    anim.animateTo(0f, placementSpec.toFloatSpec())
                }
            }
        } else null

        graphicsLayer {
            if (alphaAnim != null) alpha = alphaAnim.value
            if (translationAnim != null) translationY = translationAnim.value
        }
    }
}

// Adapt an IntOffset spec to Float for translationY
private fun FiniteAnimationSpec<IntOffset>.toFloatSpec(): FiniteAnimationSpec<Float> =
    when (this) {
        is SpringSpec<IntOffset> -> spring(
            dampingRatio = dampingRatio,
            stiffness = stiffness,
        )
        is TweenSpec<IntOffset> -> tween(
            durationMillis = durationMillis,
            easing = easing,
        )
        is SnapSpec<IntOffset> -> snap(delayMillis = delay)
        else -> spring(stiffness = Spring.StiffnessMediumLow)
    }

// ---------------------------------------------------------------------------
// Internal: row bucketing
// ---------------------------------------------------------------------------

private fun <T> buildRows(
    items: List<T>,
    containerWidth: Int,
    widthOf: (T) -> Int,
    gapPx: Int,
): List<List<T>> {
    val rows = mutableListOf<MutableList<T>>()
    var currentRow = mutableListOf<T>()
    var currentWidth = 0

    for (item in items) {
        val w = widthOf(item)
        val needed = if (currentRow.isEmpty()) w else currentWidth + gapPx + w
        if (currentRow.isNotEmpty() && needed > containerWidth) {
            rows += currentRow
            currentRow = mutableListOf(item)
            currentWidth = w
        } else {
            currentRow += item
            currentWidth = needed
        }
    }
    if (currentRow.isNotEmpty()) rows += currentRow
    return rows
}

// ---------------------------------------------------------------------------
// Internal: measurement pass
// ---------------------------------------------------------------------------

@Composable
private fun <T> MeasurementPass(
    items: List<T>,
    measuredWidths: MutableMap<Any, Int>,
    itemKey: (T) -> Any,
    itemContent: @Composable (T) -> Unit,
) {
    val unmeasured = remember(items, measuredWidths.size) {
        items.filter { itemKey(it) !in measuredWidths }
    }
    if (unmeasured.isEmpty()) return

    Layout(
        modifier = Modifier
            .height(0.dp)
            .graphicsLayer { alpha = 0f },
        content = { unmeasured.forEach { itemContent(it) } },
    ) { measurables, constraints ->
        val loose = constraints.copy(minWidth = 0, minHeight = 0)
        measurables.forEachIndexed { idx, measurable ->
            measuredWidths[itemKey(unmeasured[idx])] = measurable.measure(loose).width
        }
        layout(0, 0) {}
    }
}

// ---------------------------------------------------------------------------
// Internal: no-op scope for measurement / fallback passes
// ---------------------------------------------------------------------------

private object NoOpScope : LazyFlowRowItemScope {
    override fun Modifier.animateItem(
        fadeInSpec: FiniteAnimationSpec<Float>?,
        fadeOutSpec: FiniteAnimationSpec<Float>?,
        placementSpec: FiniteAnimationSpec<IntOffset>?,
    ): Modifier = this
}

// ---------------------------------------------------------------------------
// Usage example
// ---------------------------------------------------------------------------

/*
data class Chip(val id: String, val label: String)

@Composable
fun ChipListScreen(viewModel: ChipViewModel) {
    val chips by viewModel.chips.collectAsState()
    val activeIds by viewModel.activeIds.collectAsState()

    LazyFlowRow(
        items = chips,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalGap = 8.dp,
        verticalGap = 8.dp,
        key = { it.id },
    ) { chip ->

        // Option A: fade-in + placement only (no exit anim)
        AssistChip(
            onClick = { viewModel.toggleChip(chip.id) },
            label = { Text(chip.label) },
            modifier = Modifier.animateItem(
                fadeInSpec = tween(durationMillis = 300),
                placementSpec = spring(stiffness = Spring.StiffnessMedium),
            ),
        )

        // Option B: full enter + exit with AnimatedLazyFlowRowItem
        AnimatedLazyFlowRowItem(
            visible = chip.id in activeIds,
            enter = fadeIn() + expandHorizontally(),
            exit  = fadeOut() + shrinkHorizontally(),
        ) {
            AssistChip(
                onClick = {},
                label = { Text(chip.label) },
                modifier = Modifier.animateItem(),
            )
        }
    }
}
*/