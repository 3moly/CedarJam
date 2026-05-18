package com.moly3.cedarjam.core.ui.uikit.markdown_editor.v2

import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

/**
 * Coordinates focus across the lazy list of rows.
 *
 * The challenge with a [androidx.compose.foundation.lazy.LazyColumn]: rows scroll out of
 * composition, so we can't hold a permanent [FocusRequester] per row. Instead each row
 * *registers* its requester while it is composed and *unregisters* on dispose. When we
 * want to move focus to a row that isn't currently composed, we record a "pending focus"
 * request; the row claims it as soon as it appears (see [MarkdownRowItem]).
 */
@Stable
class RowFocusManager {

    /** Live requesters for currently-composed rows. */
    private val requesters = mutableMapOf<String, FocusRequester>()

    /** A row id whose field should grab focus as soon as it is (re)composed. */
    private val pendingFocus = mutableStateOf<PendingFocus?>(null)

    data class PendingFocus(
        val rowId: String,
        /** Where to place the caret once focused. */
        val caret: CaretTarget = CaretTarget.End,
    )

    enum class CaretTarget { Start, End }

    fun register(rowId: String, requester: FocusRequester) {
        requesters[rowId] = requester
    }

    fun unregister(rowId: String) {
        requesters.remove(rowId)
    }

    /** True if [rowId] is the row waiting to be focused. */
    fun isPending(rowId: String): Boolean = pendingFocus.value?.rowId == rowId

    fun pendingCaret(rowId: String): CaretTarget? =
        pendingFocus.value?.takeIf { it.rowId == rowId }?.caret

    fun consumePending(rowId: String) {
        if (pendingFocus.value?.rowId == rowId) pendingFocus.value = null
    }

    /**
     * Request focus on [rowId]. If the row is composed, focus immediately;
     * otherwise mark it pending so it grabs focus when it scrolls into view.
     */
    fun focus(rowId: String, caret: CaretTarget = CaretTarget.End) {
        val live = requesters[rowId]
        if (live != null) {
            pendingFocus.value = PendingFocus(rowId, caret)
            runCatching { live.requestFocus() }
        } else {
            pendingFocus.value = PendingFocus(rowId, caret)
        }
    }
}

@Composable
fun rememberRowFocusManager(): RowFocusManager = remember { RowFocusManager() }

/* ----------------------------------------------------------------------------------
 * Caret helpers
 * -------------------------------------------------------------------------------- */

/** Move the caret to the very start of a value. */
internal fun TextFieldValue.caretAtStart(): TextFieldValue =
    copy(selection = TextRange.Zero)

/** Move the caret to the very end of a value. */
internal fun TextFieldValue.caretAtEnd(): TextFieldValue =
    copy(selection = TextRange(text.length))

/** True when the caret sits on the first visual line of the text. */
internal fun TextFieldValue.caretOnFirstLine(): Boolean {
    val before = text.take(selection.start)
    return !before.contains('\n')
}

/** True when the caret sits on the last visual line of the text. */
internal fun TextFieldValue.caretOnLastLine(): Boolean {
    val after = text.substring(selection.start.coerceIn(0, text.length))
    return !after.contains('\n')
}

/** True when the caret is at character offset 0. */
internal fun TextFieldValue.caretAtDocumentStart(): Boolean = selection.start == 0

/** True when the caret is at the end of the whole text. */
internal fun TextFieldValue.caretAtDocumentEnd(): Boolean = selection.start == text.length