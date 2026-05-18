package com.moly3.cedarjam.core.ui.uikit.markdown_editor.v2

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.moly3.cedarjam.core.domain.features.mdprops.DocumentHistory
import com.moly3.cedarjam.core.domain.features.mdprops.MarkdownDocument
import com.moly3.cedarjam.core.domain.features.mdprops.MarkdownRow
import com.moly3.cedarjam.core.domain.features.mdprops.RowType
import com.moly3.cedarjam.core.ui.compositions.LocalAppTheme
import com.moly3.cedarjam.core.ui.uikit.CJText

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
    showLineNumbers: Boolean = true,          // <-- new toggle
) {
    val listState = rememberLazyListState()
    val focusManager = rememberRowFocusManager()

    val callbacks = remember(document, onDocumentChange, readOnly, history) {
        documentCallbacks(
            current = { document },
            emit = onDocumentChange,
            focusManager = focusManager,
            readOnly = readOnly,
            history = history
        )
    }

    // Width of the gutter scales with the largest line number so digits don't clip.
    val gutterWidth = if (showLineNumbers) {
        val digits = document.rows.size.toString().length
        (16 + digits * 9).dp          // rough monospace sizing; tune to taste
    } else 0.dp

    LazyColumn(
        modifier = modifier.fillMaxSize()
            .background(LocalAppTheme.current.colors.backgroundSecondary),
        state = listState,
        contentPadding = contentPadding,
    ) {
        item(key = "title") {
            TitleEditor(
                title = document.title,
                onTitleChange = { if (!readOnly) onDocumentChange(document.copy(title = it)) },
                onSubmit = { document.rows.firstOrNull()?.let { focusManager.focus(it.id) } },
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            )
        }

        item(key = "properties") {
            PropertiesSection(
                properties = document.properties,
                onPropertiesChange = {
                    if (!readOnly) onDocumentChange(document.copy(properties = it))
                },
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
                        number = index + 1,
                        width = gutterWidth,
                    )
                }
                MarkdownRowItem(
                    row = row,
                    index = index,
                    isLast = index == document.rows.lastIndex,
                    focusManager = focusManager,
                    callbacks = callbacks,
                    modifier = Modifier.weight(1f),   // was fillMaxWidth()
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
): RowCallbacks = object : RowCallbacks {

    override fun onUndo() {
        if (readOnly) return
        history.undo()?.let { emit(it) }
    }

    override fun onRedo() {
        if (readOnly) return
        history.redo()?.let { emit(it) }
    }

    override fun onTextChange(rowId: String, text: String) {
        if (readOnly) return
        emit(current().replaceRow(rowId) { it.copy(text = text) })
    }

    override fun onTypeChange(rowId: String, type: RowType) {
        if (readOnly) return
        emit(current().replaceRow(rowId) { it.copy(type = type) })
        // Keep focus on the row after switching its type.
        focusManager.focus(rowId, RowFocusManager.CaretTarget.End)
    }

    override fun onSplitRow(rowId: String, before: String, after: String) {
        if (readOnly) return
        val doc = current()
        val source = doc.rowOrNull(rowId) ?: return

        // A new row inherits list/quote type so consecutive Enters keep the list
        // going; everything else falls back to a plain paragraph.
        val newType = when (source.type) {
            RowType.BulletList, RowType.NumberedList, RowType.Quote -> source.type
            else -> RowType.Paragraph
        }

        // Special case: pressing Enter on an empty list/quote row exits the list.
        if (before.isEmpty() && after.isEmpty() &&
            source.type in setOf(RowType.BulletList, RowType.NumberedList, RowType.Quote)
        ) {
            emit(doc.replaceRow(rowId) { it.copy(type = RowType.Paragraph) })
            focusManager.focus(rowId)
            return
        }

        val newRow = MarkdownRow(type = newType, text = after)
        val updated = doc
            .replaceRow(rowId) { it.copy(text = before) }
            .insertRowAfter(rowId, newRow)
        emit(updated)
        focusManager.focus(newRow.id, RowFocusManager.CaretTarget.Start)
    }

    override fun onMergeWithPrevious(rowId: String) {
        if (readOnly) return
        val doc = current()
        val index = doc.indexOfRow(rowId)
        if (index <= 0) return // first row — nothing to merge into

        val currentRow = doc.rows[index]
        val previous = doc.rows[index - 1]

        // Dividers can't hold text — deleting one just removes it.
        if (previous.type == RowType.Divider) {
            emit(doc.removeRow(previous.id))
            focusManager.focus(rowId, RowFocusManager.CaretTarget.Start)
            return
        }

        // Caret should land where the two texts join.
        val mergedText = previous.text + currentRow.text
        val updated = doc
            .replaceRow(previous.id) { it.copy(text = mergedText) }
            .removeRow(rowId)
        emit(updated)
        focusManager.focus(previous.id, RowFocusManager.CaretTarget.End)
    }

    override fun onNavigate(rowId: String, direction: NavDirection) {
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
        focusManager.focus(target.id, caret)
    }
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