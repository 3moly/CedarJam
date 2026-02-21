package com.moly3.cedarjam.core.ui.uikit.v5

import androidx.compose.animation.core.*
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.MeasurePolicy
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

// ─────────────────────────────────────────────────────────────────────────────
// Public model
// ─────────────────────────────────────────────────────────────────────────────

data class FlowItemSize(val widthPx: Int, val heightPx: Int)

// ─────────────────────────────────────────────────────────────────────────────
// Public composable
// ─────────────────────────────────────────────────────────────────────────────

/**
 * A fully custom lazy FlowRow for Compose Multiplatform.
 *
 * - **Zero Row/FlowRow/LazyColumn usage** — layout is pure coordinate math.
 * - **Caller supplies width+height** for every item → no measurement pass ever.
 * - **Custom scroll** via [ScrollState] + a single [Layout] that reports the
 *   total content height so the system scrollbar works correctly.
 * - **Spring animation on resize** — every item animates its (x, y) position
 *   independently when the container width changes, including cross-row moves.
 * - **Lazy composition** — items outside the visible viewport are skipped entirely.
 *
 * @param items         Data list.
 * @param key           Stable unique key per item. Required for animations.
 * @param itemSize      Pixel width + height for each item.
 * @param modifier      Applied to the scrollable root.
 * @param scrollState   Scroll position control / observation.
 * @param horizontalGap Horizontal pixel gap between items in the same row.
 * @param verticalGap   Vertical pixel gap between rows.
 * @param enterFadeSpec Fade-in spec for newly added items. Pass null to disable.
 * @param slideSpec     Spring spec used when items slide to a new position on resize.
 * @param itemContent   Composable for each item.
 */
@Composable
fun <T> LazyFlowRowV6(
    items: List<T>,
    key: (T) -> Any,
    itemSize: (T) -> FlowItemSize,
    modifier: Modifier = Modifier,
    scrollState: ScrollState = rememberScrollState(),
    horizontalGap: Dp = 8.dp,
    verticalGap: Dp = 8.dp,
    enterFadeSpec: FiniteAnimationSpec<Float>? = spring(stiffness = Spring.StiffnessMediumLow),
    slideSpec: FiniteAnimationSpec<Offset> = spring(
        stiffness = Spring.StiffnessMediumLow,
        visibilityThreshold = Offset(0.5f, 0.5f),
    ),
    itemContent: @Composable (item: T) -> Unit,
) {
    val density = LocalDensity.current
    val hGapPx = remember(horizontalGap, density) { with(density) { horizontalGap.roundToPx() } }
    val vGapPx = remember(verticalGap, density) { with(density) { verticalGap.roundToPx() } }

    // ── Container width (set by the Layout measure pass) ────────────────────
    var containerWidthPx by remember { mutableStateOf(0) }

    // ── Coordinate map: key → target (x, y) in px ───────────────────────────
    // Recomputed whenever items or container width change — pure arithmetic.
    val targetCoords: Map<Any, Offset> by remember(items, containerWidthPx) {
        derivedStateOf {
            if (containerWidthPx <= 0) emptyMap()
            else buildFlowCoords(
                items       = items,
                containerW  = containerWidthPx,
                hGap        = hGapPx,
                vGap        = vGapPx,
                widthOf     = { itemSize(it).widthPx },
                heightOf    = { itemSize(it).heightPx },
                keyOf       = key,
            )
        }
    }

    // ── Total content height (for scroll layout) ─────────────────────────────
    val contentHeightPx: Int by remember(items, containerWidthPx) {
        derivedStateOf {
            if (targetCoords.isEmpty()) 0
            else {
                var max = 0
                items.forEach { item ->
                    val k = key(item)
                    val coord = targetCoords[k] ?: return@forEach
                    val bottom = coord.y.toInt() + itemSize(item).heightPx
                    if (bottom > max) max = bottom
                }
                max
            }
        }
    }

    // ── Per-item animated position state ────────────────────────────────────
    // Keyed animatables live for the lifetime of the item.
    // We store them outside composition so they survive recomposition.
    val animatables = remember { mutableStateMapOf<Any, Animatable<Offset, AnimationVector2D>>() }
    val seenKeys    = remember { mutableStateMapOf<Any, Boolean>() }

    // Drive animations whenever target coords change
    LaunchedEffect(targetCoords) {
        targetCoords.forEach { (k, target) ->
            val isNew = k !in seenKeys
            seenKeys[k] = true

            val anim = animatables.getOrPut(k) {
                Animatable(target, Offset.VectorConverter)
            }

            if (isNew) {
                // Snap to position; fade-in handles the appear effect
                anim.snapTo(target)
            } else if (anim.value != target) {
                // Launch independent coroutine per item so they animate in parallel
                launch {
                    anim.animateTo(target, slideSpec)
                }
            }
        }

        // Clean up keys that are no longer in the list
        val currentKeys = targetCoords.keys
        animatables.keys.toList().forEach { k ->
            if (k !in currentKeys) {
                animatables.remove(k)
                seenKeys.remove(k)
            }
        }
    }

    // ── Per-item fade-in alpha state ─────────────────────────────────────────
    val alphaMap = remember { mutableStateMapOf<Any, Animatable<Float, AnimationVector1D>>() }

    // ── Viewport bounds for lazy culling ────────────────────────────────────
    val scrollY = scrollState.value

    // ── Layout ───────────────────────────────────────────────────────────────
    // A single Layout node that:
    //   1. Reports containerWidthPx to the coordinate engine on first measure.
    //   2. Sets its own size to (containerWidth × contentHeight) for scroll.
    //   3. Places each item at its animated (x, y) offset.
    // Items outside the visible viewport are skipped (lazy).

    Layout(
        modifier = modifier.verticalScroll(scrollState),
        measurePolicy = rememberFlowMeasurePolicy(
            items           = items,
            key             = key,
            itemSize        = itemSize,
            animatables     = animatables,
            alphaMap        = alphaMap,
            seenKeys        = seenKeys,
            contentHeightPx = contentHeightPx,
            scrollY         = scrollY,
            enterFadeSpec   = enterFadeSpec,
            onContainerWidth = { w -> if (w != containerWidthPx) containerWidthPx = w },
        ),
        content = {
            // Only compose items that are within (or near) the visible window
            items.forEach { item ->
                val k = key(item)
                val animPos = animatables[k]?.value ?: targetCoords[k] ?: return@forEach
                val size    = itemSize(item)
                val itemTop = animPos.y.toInt()
                val itemBot = itemTop + size.heightPx

                // Lazy culling: skip if fully outside viewport (with 1-row overdraw buffer)
                val viewportTop = scrollY - size.heightPx
                val viewportBot = scrollY + 10_000 // large enough; Layout constrains height

                if (itemBot < viewportTop || itemTop > viewportBot) return@forEach

                key(k) {
                    // Fade-in for new items
                    val alpha = if (enterFadeSpec != null) {
                        alphaMap.getOrPut(k) { Animatable(0f) }.also { anim ->
                            LaunchedEffect(k) { anim.animateTo(1f, enterFadeSpec) }
                        }.value
                    } else 1f

                    itemContent(item) // NOTE: Modifier.graphicsLayer applied via placement
                    // We pass alpha to the measure policy via alphaMap
                }
            }
        },
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// MeasurePolicy
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun <T> rememberFlowMeasurePolicy(
    items: List<T>,
    key: (T) -> Any,
    itemSize: (T) -> FlowItemSize,
    animatables: Map<Any, Animatable<Offset, AnimationVector2D>>,
    alphaMap: Map<Any, Animatable<Float, AnimationVector1D>>,
    seenKeys: Map<Any, Boolean>,
    contentHeightPx: Int,
    scrollY: Int,
    enterFadeSpec: FiniteAnimationSpec<Float>?,
    onContainerWidth: (Int) -> Unit,
): MeasurePolicy = remember(
    items, animatables, alphaMap, contentHeightPx, scrollY
) {
    MeasurePolicy { measurables, constraints ->
        val containerW = constraints.maxWidth
        onContainerWidth(containerW)

        // Build a lookup: key → measurable (only visible items were composed)
        // We use index order since we emitted them in items order (skipping culled)
        var measurableIndex = 0
        val keyToMeasurable = mutableMapOf<Any, androidx.compose.ui.layout.Measurable>()
        items.forEach { item ->
            val k = key(item)
            val animPos = animatables[k]?.value
            val itemTop = animPos?.y?.toInt() ?: Int.MAX_VALUE
            val itemBot = itemTop + itemSize(item).heightPx
            val viewportTop = scrollY - itemSize(item).heightPx

            if (itemBot >= viewportTop && measurableIndex < measurables.size) {
                keyToMeasurable[k] = measurables[measurableIndex++]
            }
        }

        // Measure each visible item to its declared size (no intrinsic measurement)
        val placeables = keyToMeasurable.mapValues { (k, measurable) ->
            val sz = items.firstOrNull { key(it) == k }?.let { itemSize(it) }
                ?: return@mapValues measurable.measure(constraints)
            measurable.measure(
                constraints.copy(
                    minWidth  = sz.widthPx,  maxWidth  = sz.widthPx,
                    minHeight = sz.heightPx, maxHeight = sz.heightPx,
                )
            )
        }

        layout(
            width  = containerW,
            height = contentHeightPx.coerceAtLeast(0),
        ) {
            placeables.forEach { (k, placeable) ->
                val animPos = animatables[k]?.value ?: return@forEach
                val alpha   = if (enterFadeSpec != null) alphaMap[k]?.value ?: 0f else 1f

                placeable.placeWithLayer(
                    x = animPos.x.toInt(),
                    y = animPos.y.toInt(),
                ) {
                    this.alpha = alpha
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Pure coordinate math — no Compose, no state
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Computes the top-left pixel coordinate for every item using greedy row packing.
 * Row height = tallest item in that row.
 * No Compose APIs used — pure arithmetic.
 */
private fun <T> buildFlowCoords(
    items: List<T>,
    containerW: Int,
    hGap: Int,
    vGap: Int,
    widthOf: (T) -> Int,
    heightOf: (T) -> Int,
    keyOf: (T) -> Any,
): Map<Any, Offset> {
    val result = LinkedHashMap<Any, Offset>(items.size)

    data class RowItem(val item: T, val x: Int)

    var currentRow   = mutableListOf<RowItem>()
    var usedWidth    = 0
    var currentY     = 0
    var currentRowH  = 0

    fun flushRow() {
        if (currentRow.isEmpty()) return
        currentRow.forEach { (item, x) ->
            result[keyOf(item)] = Offset(x.toFloat(), currentY.toFloat())
        }
        currentY += currentRowH + vGap
        currentRow = mutableListOf()
        usedWidth  = 0
        currentRowH = 0
    }

    for (item in items) {
        val w = widthOf(item).coerceAtLeast(1)
        val h = heightOf(item).coerceAtLeast(1)

        val needed = if (currentRow.isEmpty()) w else usedWidth + hGap + w

        if (currentRow.isNotEmpty() && needed > containerW) {
            flushRow()
        }

        val x = if (currentRow.isEmpty()) 0 else usedWidth + hGap
        currentRow += RowItem(item, x)
        usedWidth  = if (currentRow.size == 1) w else usedWidth + hGap + w
        if (h > currentRowH) currentRowH = h
    }

    flushRow()
    return result
}