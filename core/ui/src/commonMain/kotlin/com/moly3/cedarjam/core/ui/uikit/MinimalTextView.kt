package com.moly3.cedarjam.core.ui.uikit

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*

// ── Public API ─────────────────────────────────────────────────────────────────

/**
 * Content of a placeholder — either a raw DrawScope lambda or a Composable.
 */
sealed interface PlaceholderContent {
    /** Drawn directly on the Canvas DrawScope. */
    data class Draw(val block: DrawScope.() -> Unit) : PlaceholderContent

    /** A real @Composable placed at the placeholder rect. */
    data class Compose(val content: @Composable () -> Unit) : PlaceholderContent
}

data class InlinePlaceholder(
    val id: String,
    val width: TextUnit,
    val height: TextUnit,
    val content: PlaceholderContent,
)

@Stable
class MinimalTextViewState {
    var value by mutableStateOf(TextFieldValue(""))
        internal set

    val placeholders = mutableStateMapOf<Char, InlinePlaceholder>()

    private var nextCodePoint = 0xE000

    fun insertPlaceholder(placeholder: InlinePlaceholder): Char {
        val ch = nextCodePoint.toChar()
        nextCodePoint++
        placeholders[ch] = placeholder

        val text = value.text
        val cursor = value.selection.end
        val newText = text.substring(0, cursor) + ch + text.substring(cursor)
        value = TextFieldValue(text = newText, selection = TextRange(cursor + 1))
        return ch
    }

    internal fun updateValue(new: TextFieldValue) {
        val charsInText = new.text.toSet()
        placeholders.keys.toList().forEach { if (it !in charsInText) placeholders.remove(it) }
        value = new
    }
}

@Composable
fun rememberMinimalTextViewState() = remember { MinimalTextViewState() }

// ── Composable ─────────────────────────────────────────────────────────────────

@Composable
fun MinimalTextView(
    state: MinimalTextViewState,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 16.sp,
    color: Color = Color.Black,
    cursorColor: Color = Color.Black,
    selectionColor: Color = Color.Blue.copy(alpha = 0.3f),
    onCtrlP: (() -> InlinePlaceholder)? = null,
) {
    val textMeasurer = rememberTextMeasurer()
    val focusRequester = remember { FocusRequester() }

    var cursorVisible by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(500)
            cursorVisible = !cursorVisible
        }
    }

    val style = TextStyle(fontSize = fontSize, color = color, fontFamily = FontFamily.Default)
    var selectionAnchor by remember { mutableStateOf<Int?>(null) }

    // Snapshot so lambdas always see fresh values without restarting pointerInput
    val currentValue by rememberUpdatedState(state.value)
    val currentAnchor by rememberUpdatedState(selectionAnchor)
    val currentPlaceholders by rememberUpdatedState(state.placeholders.toMap())

    fun buildAnnotated(text: String) = AnnotatedString(text)

    fun buildPlaceholderRanges(text: String): List<AnnotatedString.Range<Placeholder>> {
        val list = mutableListOf<AnnotatedString.Range<Placeholder>>()
        text.forEachIndexed { i, ch ->
            val ph = state.placeholders[ch] ?: return@forEachIndexed
            list += AnnotatedString.Range(
                item = Placeholder(ph.width, ph.height, PlaceholderVerticalAlign.Center),
                start = i, end = i + 1, tag = ph.id
            )
        }
        return list
    }

    fun measure(text: String, maxWidth: Int) = textMeasurer.measure(
        text = buildAnnotated(text),
        style = style,
        constraints = Constraints(maxWidth = maxWidth),
        softWrap = true,
        overflow = TextOverflow.Clip,
        placeholders = buildPlaceholderRanges(text)
    )

    SubcomposeLayout(
        modifier = modifier
            .focusRequester(focusRequester)
            .focusable()
            .onKeyEvent { event ->
                if (event.type != KeyEventType.KeyDown) return@onKeyEvent false
                val v = state.value
                val text = v.text
                val sel = v.selection
                val hasSel = !sel.collapsed
                when {
                    event.key == Key.P && event.isCtrlOrMetaPressed() -> {
                        onCtrlP?.invoke()?.let { state.insertPlaceholder(it) }
                        true
                    }
                    event.key == Key.Enter -> {
                        val (t, c) = replaceSelection(text, sel, "\n")
                        state.updateValue(TextFieldValue(t, TextRange(c))); selectionAnchor = null; true
                    }
                    event.key == Key.Backspace -> {
                        if (hasSel) {
                            val (t, c) = replaceSelection(text, sel, "")
                            state.updateValue(TextFieldValue(t, TextRange(c)))
                        } else if (sel.end > 0) {
                            state.updateValue(TextFieldValue(text.removeRange(sel.end - 1, sel.end), TextRange(sel.end - 1)))
                        }
                        selectionAnchor = null; true
                    }
                    event.key == Key.Delete -> {
                        if (hasSel) {
                            val (t, c) = replaceSelection(text, sel, "")
                            state.updateValue(TextFieldValue(t, TextRange(c)))
                        } else if (sel.end < text.length) {
                            state.updateValue(v.copy(text = text.removeRange(sel.end, sel.end + 1)))
                        }
                        selectionAnchor = null; true
                    }
                    event.key == Key.DirectionLeft -> {
                        if (event.isShiftPressed) {
                            val anchor = selectionAnchor ?: sel.end.also { selectionAnchor = it }
                            state.updateValue(v.copy(selection = rangeFromAnchor(anchor, (sel.end - 1).coerceAtLeast(0))))
                        } else {
                            selectionAnchor = null
                            state.updateValue(v.copy(selection = TextRange(if (hasSel) sel.min else (sel.end - 1).coerceAtLeast(0))))
                        }
                        true
                    }
                    event.key == Key.DirectionRight -> {
                        if (event.isShiftPressed) {
                            val anchor = selectionAnchor ?: sel.start.also { selectionAnchor = it }
                            state.updateValue(v.copy(selection = rangeFromAnchor(anchor, (sel.end + 1).coerceAtMost(text.length))))
                        } else {
                            selectionAnchor = null
                            state.updateValue(v.copy(selection = TextRange(if (hasSel) sel.max else (sel.end + 1).coerceAtMost(text.length))))
                        }
                        true
                    }
                    event.key == Key.MoveHome -> {
                        if (event.isShiftPressed) {
                            val anchor = selectionAnchor ?: sel.end.also { selectionAnchor = it }
                            state.updateValue(v.copy(selection = rangeFromAnchor(anchor, 0)))
                        } else { selectionAnchor = null; state.updateValue(v.copy(selection = TextRange(0))) }
                        true
                    }
                    event.key == Key.MoveEnd -> {
                        if (event.isShiftPressed) {
                            val anchor = selectionAnchor ?: sel.start.also { selectionAnchor = it }
                            state.updateValue(v.copy(selection = rangeFromAnchor(anchor, text.length)))
                        } else { selectionAnchor = null; state.updateValue(v.copy(selection = TextRange(text.length))) }
                        true
                    }
                    event.key == Key.A && event.isCtrlOrMetaPressed() -> {
                        selectionAnchor = 0
                        state.updateValue(v.copy(selection = TextRange(0, text.length))); true
                    }
                    event.utf16CodePoint > 0 -> {
                        val ch = event.utf16CodePoint.toChar()
                        if (!ch.isISOControl()) {
                            val (t, c) = replaceSelection(text, sel, ch.toString())
                            state.updateValue(TextFieldValue(t, TextRange(c))); selectionAnchor = null
                        }
                        true
                    }
                    else -> false
                }
            }
            .pointerInput(Unit) { detectTapGestures { focusRequester.requestFocus() } }
    ) { constraints ->

        val value = state.value
        val maxWidth = constraints.maxWidth
        val textLayout = measure(value.text, maxWidth)
        val width = maxWidth
        val height = textLayout.size.height.coerceAtLeast(constraints.minHeight)

        // ── 1. Subcompose + measure the background Canvas ──────────────────
        val canvasPlaceable = subcompose("canvas") {
            Canvas(
                modifier = Modifier
                    .pointerInput(currentValue) {
                        detectTapGestures { tapOffset ->
                            focusRequester.requestFocus()
                            val layout = measure(currentValue.text, size.width.toInt())
                            val pos = layout.getOffsetForPosition(tapOffset)
                            selectionAnchor = null
                            state.updateValue(currentValue.copy(selection = TextRange(pos)))
                        }
                    }
                    .pointerInput(currentValue) {
                        var dragStart = 0
                        detectDragGestures(
                            onDragStart = { startPos ->
                                focusRequester.requestFocus()
                                val layout = measure(currentValue.text, size.width.toInt())
                                dragStart = layout.getOffsetForPosition(startPos)
                                selectionAnchor = dragStart
                                state.updateValue(currentValue.copy(selection = TextRange(dragStart)))
                            },
                            onDrag = { change, _ ->
                                val layout = measure(currentValue.text, size.width.toInt())
                                val pos = layout.getOffsetForPosition(change.position)
                                state.updateValue(currentValue.copy(selection = rangeFromAnchor(currentAnchor ?: dragStart, pos)))
                            }
                        )
                    }
            ) {
                val layout = measure(value.text, size.width.toInt())
                val sel = value.selection

                // Selection highlight
                if (!sel.collapsed) {
                    for (rect in getSelectionRects(layout, sel.min, sel.max)) {
                        drawRect(selectionColor, topLeft = rect.topLeft, size = rect.size)
                    }
                }

                // Text (placeholder slots left blank — composables placed on top)
                drawText(layout, topLeft = Offset.Zero)

                // Draw-only placeholders
                layout.placeholderRects.forEachIndexed { index, rect ->
                    if (rect == null) return@forEachIndexed
                    val charIndex = buildPlaceholderRanges(value.text).getOrNull(index)?.start ?: return@forEachIndexed
                    val ch = value.text.getOrNull(charIndex) ?: return@forEachIndexed
                    val ph = state.placeholders[ch] ?: return@forEachIndexed
                    if (ph.content is PlaceholderContent.Draw) {
                        drawContext.canvas.save()
                        drawContext.canvas.clipRect(rect)

                        translate(rect.left, rect.top) { ph.content.block(this) }
                        drawContext.canvas.restore()
                    }
                }

                // Cursor
                val sel2 = value.selection
                if (sel2.collapsed && cursorVisible) {
                    val cursorRect = layout.getCursorRect(sel2.end.coerceIn(0, value.text.length))
                    drawLine(cursorColor, cursorRect.topCenter, cursorRect.bottomCenter, strokeWidth = 2f)
                }
            }
        }.first().measure(Constraints.fixed(width, height))

        // ── 2. Subcompose + measure each Composable placeholder ────────────
        //    We key by the placeholder char so compose gets a stable slot per placeholder
        val composablePlaceables = textLayout.placeholderRects
            .mapIndexedNotNull { index, rect ->
                if (rect == null) return@mapIndexedNotNull null
                val charIndex = buildPlaceholderRanges(value.text).getOrNull(index)?.start
                    ?: return@mapIndexedNotNull null
                val ch = value.text.getOrNull(charIndex) ?: return@mapIndexedNotNull null
                val ph = state.placeholders[ch] ?: return@mapIndexedNotNull null
                if (ph.content !is PlaceholderContent.Compose) return@mapIndexedNotNull null

                val phWidth = rect.width.toInt().coerceAtLeast(1)
                val phHeight = rect.height.toInt().coerceAtLeast(1)

                val placeable = subcompose("ph_$ch") {
                    ph.content.content()
                }.first().measure(Constraints.fixed(phWidth, phHeight))

                Triple(placeable, rect.left.toInt(), rect.top.toInt())
            }

        layout(width, height) {
            // Canvas behind everything
            canvasPlaceable.place(0, 0)
            // Composable placeholders on top, positioned at their text rects
            composablePlaceables.forEach { (placeable, x, y) ->
                placeable.place(x, y)
            }
        }
    }

    LaunchedEffect(Unit) { focusRequester.requestFocus() }
}

// ── Helpers ────────────────────────────────────────────────────────────────────

private fun getSelectionRects(layout: TextLayoutResult, start: Int, end: Int): List<Rect> =
    if (start >= end) emptyList()
    else (start until end).mapNotNull { layout.getBoundingBox(it) }

private fun replaceSelection(text: String, selection: TextRange, replacement: String): Pair<String, Int> {
    val s = selection.min; val e = selection.max
    return (text.substring(0, s) + replacement + text.substring(e)) to (s + replacement.length)
}

private fun rangeFromAnchor(anchor: Int, moving: Int): TextRange =
    if (moving >= anchor) TextRange(anchor, moving) else TextRange(moving, anchor)

private fun KeyEvent.isCtrlOrMetaPressed() = isCtrlPressed || isMetaPressed