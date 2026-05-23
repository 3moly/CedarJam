package com.moly3.cedarjam.core.ui.uikit.markdown_editor.v2

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.moly3.cedarjam.core.domain.features.mdprops.DividerSyntax
import com.moly3.cedarjam.core.domain.features.mdprops.DocumentHistory
import com.moly3.cedarjam.core.domain.features.mdprops.DocumentProperty
import com.moly3.cedarjam.core.domain.features.mdprops.FocusSnapshot
import com.moly3.cedarjam.core.domain.features.mdprops.MarkdownDocument
import com.moly3.cedarjam.core.domain.features.mdprops.MarkdownEncoder
import com.moly3.cedarjam.core.domain.features.mdprops.MarkdownRow
import com.moly3.cedarjam.core.domain.features.mdprops.PropertyType
import com.moly3.cedarjam.core.domain.features.mdprops.RowFocusManager
import com.moly3.cedarjam.core.domain.features.mdprops.RowType
import com.moly3.cedarjam.core.ui.compositions.LocalAppTheme
import com.moly3.cedarjam.core.ui.func.imePaddingCJ
import com.moly3.cedarjam.core.ui.func.navigationBarsPaddingCJ
import com.moly3.cedarjam.core.ui.uikit.CJText
import kotlinx.coroutines.launch

/**
 * Number of non-row lazy items rendered before the body rows (the title editor
 * and the properties section). A model row at index N is the lazy item at
 * index N + [HEADER_ITEMS]. Keep this in sync with the [LazyColumn] below.
 */
private const val HEADER_ITEMS = 2

/**
 * A Notion-/Obsidian-style Markdown editor for Compose Multiplatform.
 *
 * ## Structure (all inside one [LazyColumn] for performance)
 *  1. [TitleEditor]        — the note title.
 *  2. [PropertiesSection]  — Obsidian-style typed frontmatter properties.
 *  3. N × [MarkdownRowItem] — the editable body rows.
 *
 * ## Editing model
 *  - State is **fully hoisted**: the caller owns [document] and receives every change
 *    through [onDocumentChange]. This component never persists anything itself.
 *  - Typing `/` at the start of a row opens a menu to pick the row type
 *    (H1/H2/H3, image, code, quote, lists, divider …).
 *  - Plain **Enter** creates and focuses the next row. **Shift+Enter** inserts a
 *    newline within the current row without creating a new one — essential for the
 *    multiline [com.moly3.cedarjam.core.domain.features.mdprops.RowType.Code] block.
 *  - **Arrow Up/Down** at a row edge moves focus between rows.
 *  - **Backspace** at offset 0 merges the row into the previous one.
 *
 * @param document            the current document state (hoisted).
 * @param onDocumentChange    invoked with a new [com.moly3.cedarjam.core.domain.features.mdprops.MarkdownDocument] on every edit.
 * @param modifier            layout modifier for the editor surface.
 * @param contentPadding      padding applied inside the scrolling area.
 * @param readOnly            when true the document renders but cannot be edited.
 */
@Composable
fun MarkdownEditor(
    document: MarkdownDocument,
    onDocumentChange: (MarkdownDocument) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
    history: DocumentHistory,
    readOnly: Boolean = false,
    showLineNumbers: Boolean = true,
    onWikiLinkClick: (String) -> Unit = {},
) {
    val listState = rememberLazyListState()
    val focusManager = rememberRowFocusManager()
    val selection = rememberRowSelection()
    val clipboard = LocalClipboardManager.current
    val scope = rememberCoroutineScope()
    val lineNumbers = remember(document) { document.rowLineNumbers() }
    // There are 2 leading lazy items (title, properties) before the rows,
    // so a row at model index N lives at lazy index N + HEADER_ITEMS.
    val rowScroller: (Int) -> Unit = { rowIndex ->
        scope.launch {
            // Only scroll if the row isn't already fully visible; otherwise
            // scrolling would yank the viewport on every arrow press.
            val lazyIndex = rowIndex + HEADER_ITEMS
            val visible = listState.layoutInfo.visibleItemsInfo
            val info = visible.firstOrNull { it.index == lazyIndex }
            val viewportEnd = listState.layoutInfo.viewportEndOffset
            val viewportStart = listState.layoutInfo.viewportStartOffset
            val needsScroll = info == null ||
                    info.offset < viewportStart ||
                    info.offset + info.size > viewportEnd
            if (needsScroll) {
                // Land the target with a little breathing room from the edge.
                listState.animateScrollToItem(lazyIndex, scrollOffset = -48)
            }
        }
    }

    val callbacks = remember(document, onDocumentChange, readOnly, history) {
        documentCallbacks(
            current = { document },
            emit = { updated ->
                val lastRow = updated.rows.lastOrNull()
                val rows = updated.rows.toMutableList()
                if (lastRow != null && !lastRow.text.isEmpty()) {
                    rows.add(MarkdownRow())
                }
                onDocumentChange(updated.copy(rows = rows))
            },
            focusManager = focusManager,
            readOnly = readOnly,
            history = history,
            scrollToRow = rowScroller,
            selection = selection,
            copyToClipboard = { clipboard.setText(AnnotatedString(it)) },
            onWikiLinkClick = onWikiLinkClick,   // <-- new
        )
    }

    // Width of the gutter scales with the largest line number so digits don't clip.
    val gutterWidth = if (showLineNumbers) {
        val digits = document.rows.size.toString().length
        (16 + digits * 9).dp          // rough monospace sizing; tune to taste
    } else 0.dp

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(LocalAppTheme.current.colors.backgroundSecondary),
        state = listState,
        contentPadding = contentPadding,
    ) {
        item(key = "properties") {
            PropertiesSection(
                properties = document.properties,
                onPropertiesChange = { updated, coalesce ->
                    if (!readOnly) callbacks.onPropertiesChange(updated, coalesce)
                },
                onUndo = callbacks::onUndo,
                onRedo = callbacks::onRedo,
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            )
        }

        itemsIndexed(
            items = document.rows,
            key = { _, row -> row.id },
        ) { index, row ->
            Row(modifier = Modifier.fillMaxWidth()) {
                if (showLineNumbers) {
                    LineNumberGutter(
                        number = lineNumbers[index],
                        width = gutterWidth,
                    )
                }
                MarkdownRowItem(
                    row = row,
                    index = index,
                    isLast = index == document.rows.lastIndex,
                    focusManager = focusManager,
                    callbacks = callbacks,
                    isSelected = selection.contains(row.id, document),
                    modifier = Modifier.weight(1f),
                )
            }
        }
        item("markdown_bottom") {
            Column {
                Box(
                    Modifier
                        .height(100.dp)
                        .imePaddingCJ()
                        .navigationBarsPaddingCJ()
                )
            }
        }
    }
}


/* ----------------------------------------------------------------------------------
 * Callback implementation — turns row intent into immutable document updates.
 * -------------------------------------------------------------------------------- */

private fun documentCallbacks(
    current: () -> MarkdownDocument,
    emit: (MarkdownDocument) -> Unit,
    history: DocumentHistory,
    focusManager: RowFocusManager,
    readOnly: Boolean,
    scrollToRow: (Int) -> Unit,
    selection: RowSelection,
    copyToClipboard: (String) -> Unit,
    onWikiLinkClick: (String) -> Unit,
): RowCallbacks = object : RowCallbacks {

    private fun applyFocus(f: FocusSnapshot) {
        f.rowId?.let { focusManager.focus(it, f.caret) }
    }

    private fun commit(next: MarkdownDocument, focusAfter: FocusSnapshot) {
        history.commit(next, focusAfter)
        emit(history.current)
        applyFocus(focusAfter)
    }

    /**
     * Like [commit] but does NOT actively move focus. Used for edits triggered
     * by losing focus (e.g. a divider blur): re-focusing would yank the caret
     * back to the row the user just left. The snapshot is still recorded so
     * undo restores a sensible caret.
     */
    private fun commitNoFocus(next: MarkdownDocument, focusAfter: FocusSnapshot) {
        history.commit(next, focusAfter)
        emit(history.current)
    }

    private fun commitTyping(next: MarkdownDocument, rowId: String) {
        // Typing: caret stays in the same row. End is the good-enough target.
        val f = FocusSnapshot(rowId, RowFocusManager.CaretTarget.End)
        history.commitCoalescing(next, f)
        emit(history.current)
        // No applyFocus here — the field already has the caret; re-focusing
        // would fight the user's typing. (See note below.)
    }

    override fun onUndo() {
        if (readOnly) return
        history.undo()?.let { (doc, focus) ->
            emit(doc)
            applyFocus(focus)
        }
    }

    override fun onRedo() {
        if (readOnly) return
        history.redo()?.let { (doc, focus) ->
            emit(doc)
            applyFocus(focus)
        }
    }

    override fun onTextChange(rowId: String, text: String) {
        if (readOnly) return
        commitTyping(current().replaceRow(rowId) { it.copy(text = text) }, rowId)
    }

    override fun onPropertiesChange(properties: List<DocumentProperty>, coalesce: Boolean) {
        if (readOnly) return
        val next = current().copy(properties = properties)
        // Property edits don't move row focus — preserve whatever focus the
        // history already has so undoing back past a property edit restores
        // the caret to the row the user was last in.
        val focus = history.currentFocus
        if (coalesce) {
            // A burst of typing in a property value collapses to one undo step.
            history.commitCoalescing(next, focus)
        } else {
            // Add / remove / type-switch — a discrete, always-undoable step.
            history.commit(next, focus)
        }
        emit(history.current)
    }

    override fun onTypeChange(rowId: String, type: RowType) {
        if (readOnly) return
        commit(
            current().replaceRow(rowId) { it.copy(type = type) },
            FocusSnapshot(rowId, RowFocusManager.CaretTarget.End),
        )
    }

    override fun onSplitRow(rowId: String, before: String, after: String) {
        if (readOnly) return
        val doc = current()
        val source = doc.rowOrNull(rowId) ?: return

        val newType = when (source.type) {
            RowType.BulletList, RowType.NumberedList, RowType.Quote -> source.type
            else -> RowType.Paragraph
        }

        if (before.isEmpty() && after.isEmpty() &&
            source.type in setOf(RowType.BulletList, RowType.NumberedList, RowType.Quote)
        ) {
            commit(
                doc.replaceRow(rowId) { it.copy(type = RowType.Paragraph) },
                FocusSnapshot(rowId, RowFocusManager.CaretTarget.Start),
            )
            return
        }

        val newRow = MarkdownRow(type = newType, text = after)
        val updated = doc
            .replaceRow(rowId) { it.copy(text = before) }
            .insertRowAfter(rowId, newRow)
        commit(updated, FocusSnapshot(newRow.id, RowFocusManager.CaretTarget.Start))
    }

    override fun onMergeWithPrevious(rowId: String) {
        if (readOnly) return
        val doc = current()
        val index = doc.indexOfRow(rowId)
        if (index <= 0) return

        val currentRow = doc.rows[index]
        val previous = doc.rows[index - 1]

        if (previous.type == RowType.Divider) {
            commit(
                doc.removeRow(previous.id),
                FocusSnapshot(rowId, RowFocusManager.CaretTarget.Start),
            )
            return
        }

        val mergedText = previous.text + currentRow.text
        val updated = doc
            .replaceRow(previous.id) { it.copy(text = mergedText) }
            .removeRow(rowId)
        commit(updated, FocusSnapshot(previous.id, RowFocusManager.CaretTarget.End))
    }

    override fun onNavigate(rowId: String, direction: NavDirection) {
        // A plain arrow move ends any block selection.
        selection.clear()
        val doc = current()
        val index = doc.indexOfRow(rowId)
        if (index < 0) return
        val targetIndex = when (direction) {
            NavDirection.Up -> index - 1
            NavDirection.Down -> index + 1
        }
        val target = doc.rows.getOrNull(targetIndex) ?: return
        val caret = when (direction) {
            NavDirection.Up -> RowFocusManager.CaretTarget.End
            NavDirection.Down -> RowFocusManager.CaretTarget.Start
        }
        // Navigation is not an edit — but it MUST update currentFocus, so the
        // next commit records the right pre-edit caret.
        history.noteFocus(FocusSnapshot(target.id, caret))
        // Scroll the target into view FIRST. If it is off-screen it is not
        // composed, so it owns no FocusRequester; focus() would just park a
        // pending request that never resolves because the list never moves.
        // Scrolling forces the row to compose, and it then claims the pending
        // focus in its LaunchedEffect.
        scrollToRow(targetIndex)
        focusManager.focus(target.id, caret)
    }

    override fun onExtendSelection(rowId: String, direction: NavDirection) {
        val doc = current()
        val index = doc.indexOfRow(rowId)
        if (index < 0) return
        // First shift-arrow with no selection: anchor on the current row.
        if (!selection.isActive) selection.startAt(rowId)
        val targetIndex = when (direction) {
            NavDirection.Up -> index - 1
            NavDirection.Down -> index + 1
        }
        val target = doc.rows.getOrNull(targetIndex) ?: return
        selection.extendTo(target.id)
        // Keep the moving end visible and focused so further shift-arrows chain.
        scrollToRow(targetIndex)
        focusManager.focus(
            target.id,
            if (direction == NavDirection.Up) RowFocusManager.CaretTarget.Start
            else RowFocusManager.CaretTarget.End,
        )
    }

    override fun onCopySelection(rowId: String) {
        val doc = current()
        val ids = selection.selectedIds(doc).ifEmpty { listOf(rowId) }
        val idSet = ids.toSet()
        val rows = doc.rows.filter { it.id in idSet }
        if (rows.isEmpty()) return
        copyToClipboard(MarkdownEncoder.encodeRows(rows))
    }

    override fun onClearSelection() {
        if (selection.isActive) selection.clear()
    }

    override fun onDividerBlur(rowId: String) {
        if (readOnly) return
        val doc = current()
        val source = doc.rowOrNull(rowId) ?: return
        if (source.type != RowType.Divider) return

        if (DividerSyntax.isDivider(source.text)) {
            // Still a valid rule. Normalize the stored source (blank -> "---")
            // so the blurred row always has something to render and round-trip.
            val normalized = DividerSyntax.normalize(source.text)
            if (normalized != source.text) {
                commitNoFocus(
                    doc.replaceRow(rowId) { it.copy(text = normalized) },
                    FocusSnapshot(rowId, RowFocusManager.CaretTarget.End),
                )
            }
        } else {
            // No longer a rule — demote to a plain paragraph keeping the text.
            commitNoFocus(
                doc.replaceRow(rowId) { it.copy(type = RowType.Paragraph) },
                FocusSnapshot(rowId, RowFocusManager.CaretTarget.End),
            )
        }
    }

    override fun onWikiLinkClick(target: String) { onWikiLinkClick(target) }
}

/** Left-gutter cell showing a single line's number. */
@Composable
private fun LineNumberGutter(number: Int, width: Dp) {
    Box(
        modifier = Modifier
            .width(width)
            .padding(top = 4.dp, end = 8.dp),   // top aligns roughly with first text line
        contentAlignment = Alignment.TopEnd,
    ) {
        CJText(
            text = number.toString(),
            color = LocalAppTheme.current.colors.secondaryFont
        )
    }
}

/**
 * Returns the 1-based file line number where each body row starts, accounting
 * for frontmatter, the blank line after frontmatter, the title H1 and its
 * trailing blank, and multi-line code blocks. The result is parallel to
 * [MarkdownDocument.rows].
 */
fun MarkdownDocument.rowLineNumbers(): List<Int> {
    var line = 1

    // Frontmatter: ---, one line per property, ---, then a blank separator.
    if (properties.isNotEmpty()) {
        line += 1                       // opening ---
        line += properties.sumOf { encodedPropertyLineCount(it) }
        line += 1                       // closing ---
        line += 1                       // blank line after ---
    }

    // Title H1 and its trailing blank separator.
    if (title.isNotBlank()) {
        line += 1                       // "# title"
        line += 1                       // blank line
    }

    val starts = ArrayList<Int>(rows.size)
    for (row in rows) {
        starts.add(line)
        line += rowLineCount(row)
    }
    return starts
}

private fun encodedPropertyLineCount(p: DocumentProperty): Int = when (p.type) {
    PropertyType.List -> if (p.values.isEmpty()) 1 else 1 + p.values.size
    else -> 1
}

private fun rowLineCount(row: MarkdownRow): Int = when (row.type) {
    RowType.Code -> {
        val bodyLines = if (row.text.isEmpty()) 0 else row.text.count { it == '\n' } + 1
        2 + bodyLines
    }

    else -> 1
}