package com.moly3.cedarjam.core.ui.uikit.markdown_editor.v2

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.moly3.cedarjam.core.domain.features.mdprops.MarkdownDocument

/**
 * Tracks a contiguous block-level selection across [MarkdownRowItem] rows.
 *
 * Compose's [androidx.compose.foundation.text.BasicTextField]s are independent
 * islands — text selection cannot cross from one field into another. To let the
 * user select *whole rows* (for "copy as raw Markdown") we keep a separate,
 * row-granular selection here, anchored by row id.
 *
 * The selection is the inclusive range of rows between [anchorId] (where the
 * selection started) and [focusId] (the row the caret/last shift action is in).
 * Either end may come first in document order, so callers resolve the range
 * against the live [MarkdownDocument] via [selectedIds].
 *
 * A `null` [anchorId] means "no block selection" — normal single-row text
 * editing is active.
 */
@Stable
class RowSelection {

    var anchorId by mutableStateOf<String?>(null)
        private set

    var focusId by mutableStateOf<String?>(null)
        private set

    /** True when a multi-row block selection is active. */
    val isActive: Boolean get() = anchorId != null

    /** Clears the block selection (back to plain single-row editing). */
    fun clear() {
        anchorId = null
        focusId = null
    }

    /**
     * Begins (or restarts) a selection anchored at [rowId]. Used when the user
     * shift-interacts starting from a row that had no selection yet — the row
     * the caret was already in becomes the anchor.
     */
    fun startAt(rowId: String) {
        anchorId = rowId
        focusId = rowId
    }

    /**
     * Extends the current selection so [rowId] becomes the moving end. If no
     * selection exists yet, [rowId] also becomes the anchor (a 1-row selection).
     */
    fun extendTo(rowId: String) {
        if (anchorId == null) anchorId = rowId
        focusId = rowId
    }

    /** True if [rowId] falls within the selected range, given the document order. */
    fun contains(rowId: String, document: MarkdownDocument): Boolean {
        val ids = selectedIds(document)
        return rowId in ids
    }

    /**
     * Resolves the selection to the ordered list of selected row ids.
     * Empty when no selection is active or the endpoints are stale.
     */
    fun selectedIds(document: MarkdownDocument): List<String> {
        val a = anchorId ?: return emptyList()
        val f = focusId ?: a
        val ai = document.indexOfRow(a)
        val fi = document.indexOfRow(f)
        if (ai < 0 || fi < 0) return emptyList()
        val lo = minOf(ai, fi)
        val hi = maxOf(ai, fi)
        return document.rows.subList(lo, hi + 1).map { it.id }
    }
}

@Composable
fun rememberRowSelection(): RowSelection = remember { RowSelection() }