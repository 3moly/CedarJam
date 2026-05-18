import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.*
import com.moly3.cedarjam.core.domain.func.nowInMs
import com.moly3.cedarjam.core.ui.uikit.CJButton

// ─── Document Model ───────────────────────────────────────────────────────────

data class EditorLine(val id: Long, val text: String)
data class EditorCursor(val lineIndex: Int, val offset: Int)
data class EditorSelection(val start: EditorCursor, val end: EditorCursor) {
    val isCollapsed get() = start == end
    fun normalized() =
        if (toAbs(start) <= toAbs(end)) this else EditorSelection(end, start)
    private fun toAbs(c: EditorCursor) = c.lineIndex * 1_000_000 + c.offset
}

// ─── State ────────────────────────────────────────────────────────────────────

class LazyEditorState(initialLines: List<String> = listOf("")) {

    var lines by mutableStateOf(
        initialLines.mapIndexed { i, t -> EditorLine(i.toLong(), t) }
    )
        private set

    var cursor by mutableStateOf(EditorCursor(0, 0))
        private set

    var selection by mutableStateOf<EditorSelection?>(null)
        private set

    // ── Layout tracking ──────────────────────────────────────────────────────

    private val lineYPositions = mutableMapOf<Int, Float>()
    private val lineHeights    = mutableMapOf<Int, Float>()

    // Per-line intrinsic pixel widths (measured by each row after layout)
    private val lineWidthsPx = mutableStateMapOf<Int, Float>()

    // The max width across all measured lines — every row is forced to this width
    val maxLineWidthPx: Float by derivedStateOf {
        lineWidthsPx.values.maxOrNull() ?: 0f
    }

    var approxCharWidth: Float = 9f

    fun registerLineLayout(index: Int, yInWindow: Float, height: Float) {
        lineYPositions[index] = yInWindow
        lineHeights[index]    = height
    }

    /** Called from each row after its inner content is measured */
    fun registerLineWidth(index: Int, widthPx: Float) {
        lineWidthsPx[index] = widthPx
    }

    /** Remove stale width entry when a line is removed / recycled */
    fun unregisterLineWidth(index: Int) {
        lineWidthsPx.remove(index)
    }

    fun hitTestLine(windowY: Float): Int {
        var best = 0; var bestDist = Float.MAX_VALUE
        lineYPositions.forEach { (idx, y) ->
            val h    = lineHeights[idx] ?: 0f
            val dist = when {
                windowY < y     -> y - windowY
                windowY > y + h -> windowY - (y + h)
                else            -> 0f
            }
            if (dist < bestDist) { bestDist = dist; best = idx }
        }
        return best
    }

    fun hitTest(windowX: Float, windowY: Float, contentStartX: Float): EditorCursor {
        val lineIdx  = hitTestLine(windowY)
        val charOff  = ((windowX - contentStartX) / approxCharWidth)
            .toInt().coerceIn(0, lines[lineIdx].text.length)
        return EditorCursor(lineIdx, charOff)
    }

    // ── Cursor / selection mutations ─────────────────────────────────────────

    fun moveCursorTo(lineIndex: Int, offset: Int, extendSelection: Boolean = false) {
        val si        = lineIndex.coerceIn(0, lines.lastIndex)
        val so        = offset.coerceIn(0, lines[si].text.length)
        val newCursor = EditorCursor(si, so)
        if (extendSelection) {
            val anchor = selection?.start ?: cursor
            selection  = EditorSelection(anchor, newCursor)
        } else {
            selection = null
        }
        cursor = newCursor
    }

    fun startDragSelection(anchor: EditorCursor) {
        cursor    = anchor
        selection = null
    }

    fun updateDragSelection(end: EditorCursor) {
        val anchor = selection?.start ?: cursor
        selection  = EditorSelection(anchor, end)
        cursor     = end
    }

    fun insertNewline() {
        deleteSelection()
        val line   = lines[cursor.lineIndex]
        val before = line.text.substring(0, cursor.offset)
        val after  = line.text.substring(cursor.offset)
        val mut    = lines.toMutableList()
        mut[cursor.lineIndex] = line.copy(text = before)
        mut.add(cursor.lineIndex + 1, EditorLine(nowInMs(), after))
        lines     = mut
        cursor    = EditorCursor(cursor.lineIndex + 1, 0)
        selection = null
        // Re-index widths for lines that shifted down
        reindexWidthsAfterInsert(cursor.lineIndex)
    }

    fun backspace() {
        if (selection != null && !selection!!.isCollapsed) { deleteSelection(); return }
        if (cursor.offset > 0) {
            updateLine(cursor.lineIndex, lines[cursor.lineIndex].text
                .removeRange(cursor.offset - 1, cursor.offset))
            cursor = cursor.copy(offset = cursor.offset - 1)
        } else if (cursor.lineIndex > 0) {
            val prev = lines[cursor.lineIndex - 1]
            val curr = lines[cursor.lineIndex]
            val mut  = lines.toMutableList()
            mut[cursor.lineIndex - 1] = prev.copy(text = prev.text + curr.text)
            mut.removeAt(cursor.lineIndex)
            lines  = mut
            cursor = EditorCursor(cursor.lineIndex - 1, prev.text.length)
            reindexWidthsAfterRemove(cursor.lineIndex + 1)
        }
        selection = null
    }

    fun insertText(text: String) {
        deleteSelection()
        val line    = lines[cursor.lineIndex]
        val newText = line.text.substring(0, cursor.offset) + text + line.text.substring(cursor.offset)
        updateLine(cursor.lineIndex, newText)
        cursor = cursor.copy(offset = cursor.offset + text.length)
    }

    fun selectAll() {
        val last  = lines.lastIndex
        selection = EditorSelection(EditorCursor(0, 0), EditorCursor(last, lines[last].text.length))
        cursor    = EditorCursor(last, lines[last].text.length)
    }

    fun selectedRangeInLine(lineIndex: Int): TextRange? {
        val sel = selection?.normalized() ?: return null
        if (lineIndex !in sel.start.lineIndex..sel.end.lineIndex) return null
        val start = if (lineIndex == sel.start.lineIndex) sel.start.offset else 0
        val end   = if (lineIndex == sel.end.lineIndex)   sel.end.offset   else lines[lineIndex].text.length
        return TextRange(start, end)
    }

    private fun deleteSelection() {
        val sel  = selection?.normalized() ?: return
        val mut  = lines.toMutableList()
        val merged = mut[sel.start.lineIndex].text.substring(0, sel.start.offset) +
                mut[sel.end.lineIndex].text.substring(sel.end.offset)
        mut[sel.start.lineIndex] = mut[sel.start.lineIndex].copy(text = merged)
        for (i in sel.end.lineIndex downTo sel.start.lineIndex + 1) mut.removeAt(i)
        lines     = mut
        cursor    = sel.start
        selection = null
        reindexWidthsAfterRemove(sel.start.lineIndex + 1)
    }

    private fun updateLine(index: Int, text: String) {
        val mut = lines.toMutableList()
        mut[index] = mut[index].copy(text = text)
        lines = mut
        // Width for this line will be re-measured on next recompose
    }

    // When a new line is inserted at `afterIndex`, all entries ≥ afterIndex+1 shift +1
    private fun reindexWidthsAfterInsert(afterIndex: Int) {
        val shifted = lineWidthsPx.entries
            .filter { it.key > afterIndex }
            .sortedByDescending { it.key }
        shifted.forEach { (k, v) ->
            lineWidthsPx.remove(k)
            lineWidthsPx[k + 1] = v
        }
    }

    // When a line at `removedIndex` is deleted, all entries > removedIndex shift -1
    private fun reindexWidthsAfterRemove(removedIndex: Int) {
        lineWidthsPx.remove(removedIndex)
        val shifted = lineWidthsPx.entries
            .filter { it.key > removedIndex }
            .sortedBy { it.key }
        shifted.forEach { (k, v) ->
            lineWidthsPx.remove(k)
            lineWidthsPx[k - 1] = v
        }
    }

    fun getContent() = lines.joinToString("\n") { it.text }
}

// ─── Editor ───────────────────────────────────────────────────────────────────

@Composable
fun LazyEditor(
    state: LazyEditorState,
    modifier: Modifier = Modifier,
    textStyle: TextStyle  = TextStyle(fontSize = 15.sp, color = Color(0xFFE0E0E0)),
    selectionColor: Color = Color(0x884FC3F7),
    cursorColor: Color    = Color(0xFF4FC3F7),
) {
    val listState         = rememberLazyListState()
    // ── Single shared horizontal scroll for ALL rows ──────────────────────
    val horizontalScroll  = rememberScrollState()

    var containerYInWindow by remember { mutableStateOf(0f) }
    var containerXInWindow by remember { mutableStateOf(0f) }
    var containerWidthPx   by remember { mutableStateOf(0f) }

    val charWidthPx = textStyle.fontSize.value * 0.6f
    SideEffect { state.approxCharWidth = charWidthPx }

    val selectionColors = remember(selectionColor) {
        TextSelectionColors(handleColor = selectionColor, backgroundColor = selectionColor)
    }

    CompositionLocalProvider(LocalTextSelectionColors provides selectionColors) {
        Box(modifier = modifier.background(Color(0xFF1E1E1E))) {

            // ── Main scrollable area ─────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .onGloballyPositioned {
                        containerYInWindow = it.positionInWindow().y
                        containerXInWindow = it.positionInWindow().x
                        containerWidthPx   = it.size.width.toFloat()
                    }
                    // Horizontal scroll on the outer box — LazyColumn handles vertical
                    .horizontalScroll(horizontalScroll)
                    .pointerInput(Unit) {
                        awaitEachGesture {
                            val down   = awaitFirstDown(requireUnconsumed = false)
                            val downWX = containerXInWindow + down.position.x + horizontalScroll.value
                            val downWY = containerYInWindow + down.position.y
                            val anchor = state.hitTest(downWX, downWY, containerXInWindow)
                            state.startDragSelection(anchor)

                            var isDrag = false
                            do {
                                val event  = awaitPointerEvent(PointerEventPass.Main)
                                val change = event.changes.firstOrNull() ?: break
                                if (change.positionChanged()) {
                                    val wx  = containerXInWindow + change.position.x + horizontalScroll.value
                                    val wy  = containerYInWindow + change.position.y
                                    val end = state.hitTest(wx, wy, containerXInWindow)
                                    if (end != anchor || isDrag) {
                                        isDrag = true
                                        state.updateDragSelection(end)
                                        change.consume()
                                    }
                                }
                            } while (event.changes.any { it.pressed })

                            if (!isDrag) state.moveCursorTo(anchor.lineIndex, anchor.offset, false)
                        }
                    }
            ) {
                // The inner Box is sized to max(containerWidth, maxLineWidth) so the
                // horizontal scroll has something to scroll against
                val minWidthPx = state.maxLineWidthPx + 32f   // +padding
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .widthIn(min = with(LocalDensity.current) { minWidthPx.toDp() })
                ) {
                    LazyColumn(
                        state    = listState,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        itemsIndexed(state.lines, key = { _, line -> line.id }) { index, line ->
                            EditorLineRow(
                                index          = index,
                                line           = line,
                                state          = state,
                                textStyle      = textStyle,
                                cursorColor    = cursorColor,
                                // Each row must be at least as wide as the widest line
                                minWidthPx     = state.maxLineWidthPx,
                            )
                        }
                    }
                }
            }

            // ── Horizontal scrollbar (pinned to bottom) ──────────────────
//            HorizontalScrollbar(
//                modifier = Modifier
//                    .align(androidx.compose.ui.Alignment.BottomStart)
//                    .fillMaxWidth(),
//                adapter  = rememberScrollbarAdapter(horizontalScroll)
//            )
        }
    }
}

// ─── Line Row ─────────────────────────────────────────────────────────────────

@Composable
private fun EditorLineRow(
    index: Int,
    line: EditorLine,
    state: LazyEditorState,
    textStyle: TextStyle,
    cursorColor: Color,
    minWidthPx: Float,
) {
    val isCursorLine = state.cursor.lineIndex == index
    val selRange     = state.selectedRangeInLine(index)

    val tfValue = remember(line.text, isCursorLine, state.cursor.offset, selRange) {
        when {
            selRange != null -> TextFieldValue(line.text, selRange)
            isCursorLine     -> TextFieldValue(line.text, TextRange(state.cursor.offset))
            else             -> TextFieldValue(line.text)
        }
    }

    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(isCursorLine) {
        if (isCursorLine) runCatching { focusRequester.requestFocus() }
    }

    // Cleanup stale width when this composition leaves
    DisposableEffect(index) {
        onDispose { state.unregisterLineWidth(index) }
    }

    val density = LocalDensity.current

    // Row is at least minWidthPx wide so all rows share the same scrollable canvas
    val minWidthDp = with(density) { minWidthPx.toDp() }

    BasicTextField(
        value = tfValue,
        onValueChange = { new ->
            val old = line.text
            when {
                new.text == old -> {
                    val isSingleLine = state.selection == null ||
                            state.selection!!.normalized().let {
                                it.start.lineIndex == index && it.end.lineIndex == index
                            }
                    if (isSingleLine) {
                        if (new.selection.collapsed)
                            state.moveCursorTo(index, new.selection.start)
                        else {
                            state.moveCursorTo(index, new.selection.start, false)
                            state.moveCursorTo(index, new.selection.end, true)
                        }
                    }
                }
                new.text.length < old.length -> {
                    val count = old.length - new.text.length
                    state.moveCursorTo(index, new.selection.start + count)
                    repeat(count) { state.backspace() }
                }
                else -> {
                    val inserted = new.text.substring(
                        tfValue.selection.start,
                        tfValue.selection.start + (new.text.length - old.length)
                    )
                    state.moveCursorTo(index, tfValue.selection.start)
                    state.insertText(inserted)
                }
            }
        },
        modifier = Modifier
            .widthIn(min = minWidthDp)          // ← synchronized minimum width
            .padding(horizontal = 8.dp, vertical = 2.dp)
            .focusRequester(focusRequester)
            .onGloballyPositioned { coords ->
                state.registerLineLayout(
                    index,
                    coords.positionInWindow().y,
                    coords.size.height.toFloat()
                )
                // Report this line's actual rendered width back to state
                state.registerLineWidth(index, coords.size.width.toFloat())
            }
            .onKeyEvent { event ->
                if (event.type != KeyEventType.KeyDown) return@onKeyEvent false
                when (event.key) {
                    Key.Enter, Key.NumPadEnter -> { state.insertNewline(); true }
                    Key.Backspace             -> { state.backspace();      true }
                    else -> false
                }
            },
        textStyle   = textStyle,
        cursorBrush = SolidColor(cursorColor),
        singleLine  = true,
    )
}

// ─── Screen ───────────────────────────────────────────────────────────────────

@Composable
fun EditorScreen() {
    val state = remember {
        LazyEditorState(listOf(
            "fun main() {",
            "    println(\"Hello, Lazy Editor! This is a very long line to test horizontal scrolling behavior.\")",
            "}",
            "",
            "// start typing here...",
        ))
    }

    Column(Modifier.fillMaxSize().background(Color(0xFF1E1E1E))) {
        Row(
            Modifier.fillMaxWidth().background(Color(0xFF2D2D2D)).padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CJButton(text = "Select All", onClick = { state.selectAll() })
        }

        LazyEditor(
            state    = state,
            modifier = Modifier.fillMaxSize().padding(4.dp)
        )
    }
}