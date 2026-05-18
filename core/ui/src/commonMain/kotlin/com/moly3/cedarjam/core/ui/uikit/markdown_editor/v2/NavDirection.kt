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
    fun onUndo()
    fun onRedo()

    /**
     * The properties (frontmatter) list changed.
     *
     * @param coalesce when true the change is merged into the previous typing
     *   step in [com.moly3.cedarjam.core.domain.features.mdprops.DocumentHistory]
     *   (use for per-keystroke value edits); when false it is a discrete step
     *   (add / remove / type change). Routing every property edit through here
     *   keeps undo/redo consistent — direct document mutation loses history.
     */
    fun onPropertiesChange(
        properties: List<com.moly3.cedarjam.core.domain.features.mdprops.DocumentProperty>,
        coalesce: Boolean,
    )

    /**
     * Shift+Arrow at a row edge: extend the block selection to the adjacent row
     * (starting one anchored at [rowId] if none exists yet).
     */
    fun onExtendSelection(rowId: String, direction: NavDirection)

    /**
     * Ctrl/Cmd+C: copy the current block selection (or, if none, the single
     * row [rowId]) to the clipboard as raw Markdown text.
     */
    fun onCopySelection(rowId: String)

    /** A plain (non-shift) interaction happened in a row — drop any block selection. */
    fun onClearSelection()

    /**
     * A [com.moly3.cedarjam.core.domain.features.mdprops.RowType.Divider] row lost
     * focus. The editor inspects its current [text]: if it is still valid divider
     * syntax (`---`, `***`, `___`) the row stays a divider and renders as a line
     * again; otherwise it is demoted to a plain paragraph carrying that text.
     */
    fun onDividerBlur(rowId: String)
}