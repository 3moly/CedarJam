package com.moly3.cedarjam.core.domain.features.mdprops;

import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.focus.FocusRequester

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