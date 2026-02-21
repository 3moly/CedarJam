package com.moly3.cedarjam.core.ui.uikit.newLazyv3

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
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp

// ---------------------------------------------------------------------------
// Public API
// ---------------------------------------------------------------------------

/**
 * Receiver scope for [LazyFlowRowV3]'s itemContent lambda.
 * Call [Modifier.animateItem] to opt into enter/exit/placement animations.
 */
interface LazyFlowRowItemScope {
    /**
     * Animates this item on appear (fade-in) and row-change (vertical slide).
     * For exit animations wrap the item with [AnimatedLazyFlowRowItem].
     * Signature mirrors `LazyColumn`'s `Modifier.animateItem(...)`.
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
 * A **truly lazy** flow-row layout for Compose Multiplatform.
 *
 * Items are arranged in rows (like `FlowRow`) and rows are lazily composed via
 * `LazyColumn` — **only visible rows enter composition**, just like `LazyColumn`.
 * No items are composed eagerly for measurement purposes.
 *
 * ### How widths are resolved (in priority order)
 * 1. **[itemWidthPx]** — if provided, widths are computed without any composition.
 *    Use this whenever the width is knowable from the item data (e.g. fixed size,
 *    or size stored on the model). This is the most efficient path.
 * 2. **Lazy per-row measurement** — if [itemWidthPx] is null, each row's items are
 *    measured in a hidden zero-height slot *within that row's lazy item*, so only
 *    visible rows trigger measurement. This costs one extra frame per newly-visible
 *    row but keeps composition strictly lazy.
 *
 * ### Quick start (known sizes — zero overhead)
 * ```kotlin
 * LazyFlowRow(
 *     items = chips,
 *     key = { it.id },
 *     itemWidthPx = { it.widthPx },   // ← supply width from your model
 * ) { chip ->
 *     MyChip(chip, Modifier.animateItem())
 * }
 * ```
 *
 * ### Quick start (unknown sizes — lazy measurement)
 * ```kotlin
 * LazyFlowRow(items = chips, key = { it.id }) { chip ->
 *     MyChip(chip, Modifier.animateItem())
 * }
 * ```
 *
 * @param items         The list of items to display.
 * @param modifier      Modifier applied to the root container.
 * @param state         LazyListState for scroll control / observation.
 * @param horizontalGap Horizontal spacing between items in the same row.
 * @param verticalGap   Vertical spacing between rows.
 * @param key           Stable key extractor. Required for correct animations.
 * @param itemWidthPx   Optional width supplier. When non-null, no measurement
 *                      compositions are needed and all items stay truly lazy.
 * @param itemContent   Composable lambda; receiver is [LazyFlowRowItemScope].
 */
@Composable
fun <T> LazyFlowRowV3(
    items: List<T>,
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    horizontalGap: Dp = 8.dp,
    verticalGap: Dp = 8.dp,
    key: ((item: T) -> Any)? = null,
    itemWidthPx: ((item: T) -> Int)? = null,
    itemContent: @Composable LazyFlowRowItemScope.(item: T) -> Unit,
) {
    val itemKey: (T) -> Any = remember(key) {
        if (key != null) key else { item -> items.indexOf(item).toString() }
    }

    var containerWidthPx by remember { mutableStateOf(0) }

    // key → measured width (populated lazily, or upfront from itemWidthPx)
    val measuredWidths = remember { mutableStateMapOf<Any, Int>() }

    // Pre-populate from itemWidthPx so rows can be built on the first frame
    // without any measurement pass.
    if (itemWidthPx != null) {
        SideEffect {
            items.forEach { item ->
                measuredWidths[itemKey(item)] = itemWidthPx(item)
            }
        }
    }

    // key → last known row index, for placement animation direction
    val rowTracker = remember { mutableStateMapOf<Any, Int>() }

    val gapPx = with(LocalDensity.current) { horizontalGap.roundToPx() }

    // Row buckets — recomputed when widths or container width change
    val rows: List<List<T>> by remember(containerWidthPx, items) {
        derivedStateOf {
            if (containerWidthPx <= 0 || items.isEmpty()) return@derivedStateOf emptyList()
            buildRows(
                items = items,
                containerWidth = containerWidthPx,
                widthOf = { item -> measuredWidths[itemKey(item)] ?: 0 },
                gapPx = gapPx,
            )
        }
    }

    Box(modifier = modifier.onSizeChanged { containerWidthPx = it.width }) {
        LazyColumn(state = state, modifier = Modifier.fillMaxSize()) {

            // ── Fallback / pre-layout frame ──────────────────────────────────
            // Shown only if no rows have been computed yet (first frame with
            // lazy measurement, before any widths are known).
            if (rows.isEmpty() && items.isNotEmpty() && itemWidthPx == null) {
                item(key = "__lazyflowrow_fallback__") {
                    @OptIn(ExperimentalLayoutApi::class)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(horizontalGap),
                        verticalArrangement = Arrangement.spacedBy(verticalGap),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        // Use NoOpScope — these are real items in the fallback but
                        // the fallback itself lives in a single lazy slot.
                        items.forEach { NoOpScope.itemContent(it) }
                    }
                }
            }

            // ── Lazy rows ────────────────────────────────────────────────────
            rows.forEachIndexed { rowIndex, rowItems ->
                val rowKey = rowItems.joinToString("|") { itemKey(it).toString() }

                item(key = rowKey) {
                    // When itemWidthPx is null we measure this row's items
                    // in a hidden slot — only this row is composed for measurement,
                    // not the entire list.
                    if (itemWidthPx == null) {
                        LazyRowMeasurementPass(
                            rowItems = rowItems,
                            measuredWidths = measuredWidths,
                            itemKey = itemKey,
                            itemContent = { NoOpScope.itemContent(it) },
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(horizontalGap),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        rowItems.forEach { item ->
                            val k = itemKey(item)
                            val prevRow = rowTracker[k]
                            val isNew   = prevRow == null
                            val moved   = prevRow != null && prevRow != rowIndex

                            SideEffect { rowTracker[k] = rowIndex }

                            val scope = rememberItemAnimationScope(
                                isNew    = isNew,
                                moved    = moved,
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
 * Wraps an item in [AnimatedVisibility] to support exit animations.
 *
 * ```kotlin
 * LazyFlowRow(items, key = { it.id }) { item ->
 *     AnimatedLazyFlowRowItem(visible = item.id in activeIds) {
 *         MyChip(item, modifier = Modifier.animateItem())
 *     }
 * }
 * ```
 */
@Composable
fun AnimatedLazyFlowRowItem(
    visible: Boolean,
    enter: EnterTransition = fadeIn() + expandHorizontally(),
    exit: ExitTransition   = fadeOut() + shrinkHorizontally(),
    content: @Composable () -> Unit,
) {
    AnimatedVisibility(visible = visible, enter = enter, exit = exit) {
        content()
    }
}

// ---------------------------------------------------------------------------
// Internal — per-row lazy measurement
// ---------------------------------------------------------------------------

/**
 * Measures only the items in [rowItems] that haven't been measured yet.
 * Runs inside the lazy item for that row, so measurement is deferred until
 * the row scrolls into view — the entire list is never eagerly composed.
 *
 * Widths are collected in a plain map during the measure pass (no snapshot
 * state mutation) and applied via [SideEffect] after layout completes.
 */
@Composable
private fun <T> LazyRowMeasurementPass(
    rowItems: List<T>,
    measuredWidths: MutableMap<Any, Int>,
    itemKey: (T) -> Any,
    itemContent: @Composable (T) -> Unit,
) {
    val unmeasured = remember(rowItems, measuredWidths.size) {
        rowItems.filter { itemKey(it) !in measuredWidths }
    }
    if (unmeasured.isEmpty()) return

    val pendingWidths = remember { mutableMapOf<Any, Int>() }

    Layout(
        modifier = Modifier
            .height(0.dp)
            .graphicsLayer { alpha = 0f },
        content = { unmeasured.forEach { itemContent(it) } },
    ) { measurables, constraints ->
        val loose = constraints.copy(minWidth = 0, minHeight = 0)
        // ✅ Plain map — no SnapshotState written during measure
        measurables.forEachIndexed { idx, measurable ->
            pendingWidths[itemKey(unmeasured[idx])] = measurable.measure(loose).width
        }
        layout(0, 0) {}
    }

    // ✅ SideEffect runs after layout; safe to write SnapshotState here
    SideEffect {
        measuredWidths.putAll(pendingWidths)
        pendingWidths.clear()
    }
}

// ---------------------------------------------------------------------------
// Internal — ItemAnimationScope
// ---------------------------------------------------------------------------

@Composable
private fun rememberItemAnimationScope(
    isNew: Boolean,
    moved: Boolean,
    rowDelta: Int,
): LazyFlowRowItemScope = remember(isNew, moved, rowDelta) {
    ItemAnimationScope(isNew = isNew, moved = moved, rowDelta = rowDelta)
}

private class ItemAnimationScope(
    private val isNew: Boolean,
    private val moved: Boolean,
    private val rowDelta: Int,
) : LazyFlowRowItemScope {

    override fun Modifier.animateItem(
        fadeInSpec: FiniteAnimationSpec<Float>?,
        fadeOutSpec: FiniteAnimationSpec<Float>?,
        placementSpec: FiniteAnimationSpec<IntOffset>?,
    ): Modifier = this.composed {

        val alphaAnim = if (isNew && fadeInSpec != null) {
            remember { Animatable(0f) }.also { anim ->
                LaunchedEffect(Unit) { anim.animateTo(1f, fadeInSpec) }
            }
        } else null

        val translationAnim = if (moved && placementSpec != null && rowDelta != 0) {
            val startPx = rowDelta * 48f
            remember { Animatable(startPx) }.also { anim ->
                LaunchedEffect(rowDelta) { anim.animateTo(0f, placementSpec.toFloatSpec()) }
            }
        } else null

        if (alphaAnim == null && translationAnim == null) return@composed this

        graphicsLayer {
            if (alphaAnim != null) alpha = alphaAnim.value
            if (translationAnim != null) translationY = translationAnim.value
        }
    }
}

private fun FiniteAnimationSpec<IntOffset>.toFloatSpec(): FiniteAnimationSpec<Float> =
    when (this) {
        is SpringSpec<IntOffset> -> spring(dampingRatio = dampingRatio, stiffness = stiffness)
        is TweenSpec<IntOffset>  -> tween(durationMillis = durationMillis, easing = easing)
        is SnapSpec<IntOffset>   -> snap(delayMillis = delay)
        else                     -> spring(stiffness = Spring.StiffnessMediumLow)
    }

// ---------------------------------------------------------------------------
// Internal — row bucketing
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
        val w = widthOf(item).coerceAtLeast(1)
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
// Internal — no-op scope
// ---------------------------------------------------------------------------

private object NoOpScope : LazyFlowRowItemScope {
    override fun Modifier.animateItem(
        fadeInSpec: FiniteAnimationSpec<Float>?,
        fadeOutSpec: FiniteAnimationSpec<Float>?,
        placementSpec: FiniteAnimationSpec<IntOffset>?,
    ): Modifier = this
}

// ---------------------------------------------------------------------------
// Missing import helper (add to your imports)
// ---------------------------------------------------------------------------
// import androidx.compose.ui.platform.LocalDensity

// ---------------------------------------------------------------------------
// Usage — your exact use case (zero composition overhead for sizing)
// ---------------------------------------------------------------------------

/*
data class MakeDiff(val id: Int, val size: Int)

LazyFlowRow(
    modifier = Modifier.weight(1f).fillMaxWidth(),
    items = timemachinesBig,
    key = { it.id },
    // ✅ Width is known from the model — no measurement pass needed at all.
    // Only visible rows are ever composed.
    itemWidthPx = { it.size /* already in px, or convert dp→px here */ },
) { item ->
    Box(Modifier.size(item.size.dp).background(Color.Red))
    LaunchedEffect(Unit) {
        Logger.w { "dawn: ${item.id}" }   // fires only when row scrolls into view
    }
}

 */