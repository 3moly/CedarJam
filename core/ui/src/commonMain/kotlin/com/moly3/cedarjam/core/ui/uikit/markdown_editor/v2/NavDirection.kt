package com.moly3.cedarjam.core.ui.uikit.markdown_editor.v2

import androidx.compose.runtime.Stable
import com.moly3.cedarjam.core.domain.features.mdprops.RowType

/** Direction for arrow-key navigation between rows. */
enum class NavDirection { Up, Down }

/**
 * The set of callbacks a [MarkdownRowItem] uses to report intent back to the
 * parent [MarkdownEditor], which owns the document and applies the changes.
 *
 * Keeping these as an interface (rather than a pile of lambda params) keeps the
 * row signature small and makes the row easy to test in isolation.
 */
@Stable
interface RowCallbacks {

    /** The row's text content changed. */
    fun onTextChange(rowId: String, text: String)

    /** The row's [com.moly3.cedarjam.core.domain.features.mdprops.RowType] changed (via the slash menu). */
    fun onTypeChange(rowId: String, type: RowType)

    /**
     * Plain Enter pressed: keep [before] in the current row, create a new row
     * holding [after] right below, and move focus into it.
     */
    fun onSplitRow(rowId: String, before: String, after: String)

    /** Backspace at offset 0: merge this row's text into the previous row. */
    fun onMergeWithPrevious(rowId: String)

    /** Arrow key at a row edge: move focus to the adjacent row. */
    fun onNavigate(rowId: String, direction: NavDirection)
}