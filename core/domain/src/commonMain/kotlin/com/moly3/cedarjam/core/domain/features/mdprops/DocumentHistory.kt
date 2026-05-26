package com.moly3.cedarjam.core.domain.features.mdprops

import kotlin.time.Clock.System

/**
 * Linear undo/redo history for a [MarkdownDocument].
 *
 * The owner of the document holds one of these. Every committed edit pushes the
 * *previous* state onto the undo stack and clears the redo stack. [undo]/[redo]
 * move the cursor along that history.
 *
 * Edits are committed in two modes:
 *  - [commit]    — a discrete edit (split, merge, type change). Always a new step.
 *  - [commitCoalescing] — a typing edit. Merged into the previous step if that
 *    step was also a coalescing edit within [coalesceWindowMs], so a burst of
 *    typing collapses into a single undo step.
 */
class DocumentHistory(
    initial: MarkdownDocument,
    private val maxDepth: Int = 200,
    private val coalesceWindowMs: Long = 600L,
    private val now: () -> Long = { System.now().toEpochMilliseconds() },
) {
    private data class Entry(
        val doc: MarkdownDocument,
        val focus: FocusSnapshot,
        val coalescing: Boolean,
        val at: Long,
    )

    private val undoStack = ArrayDeque<Entry>()
    private val redoStack = ArrayDeque<Entry>()

    var current: MarkdownDocument = initial
        private set

    /** Focus snapshot for [current] — kept up to date by the editor as the caret moves. */
    var currentFocus: FocusSnapshot = FocusSnapshot(rowId = initial.rows.firstOrNull()?.id)
        private set

    val canUndo: Boolean get() = undoStack.isNotEmpty()
    val canRedo: Boolean get() = redoStack.isNotEmpty()

    /**
     * Records that the caret moved without the document changing (navigation,
     * clicking into a row). Updates [currentFocus] so the *next* commit captures
     * the right pre-edit position. Does NOT create an undo step.
     */
    fun noteFocus(focus: FocusSnapshot) {
        currentFocus = focus
    }

    /** Discrete edit. [focusAfter] is where the caret lands once the edit applies. */
    fun commit(next: MarkdownDocument, focusAfter: FocusSnapshot) {
        if (next == current) return
        push(coalescing = false)          // pushes (current, currentFocus)
        current = next
        currentFocus = focusAfter
        redoStack.clear()
    }

    fun commitCoalescing(next: MarkdownDocument, focusAfter: FocusSnapshot) {
        if (next == current) return
        val last = undoStack.lastOrNull()
        val mergeable = last != null && last.coalescing &&
                (now() - last.at) <= coalesceWindowMs
        if (!mergeable) push(coalescing = true)
        current = next
        currentFocus = focusAfter
        redoStack.clear()
    }

    /** Returns the restored document plus the focus to apply, or null. */
    fun undo(): Pair<MarkdownDocument, FocusSnapshot>? {
        val prev = undoStack.removeLastOrNull() ?: return null
        // Save current onto redo so redo can return here.
        redoStack.addLast(Entry(current, currentFocus, prev.coalescing, now()))
        current = prev.doc
        currentFocus = prev.focus
        return current to currentFocus
    }

    fun redo(): Pair<MarkdownDocument, FocusSnapshot>? {
        val next = redoStack.removeLastOrNull() ?: return null
        undoStack.addLast(Entry(current, currentFocus, next.coalescing, now()))
        current = next.doc
        currentFocus = next.focus
        return current to currentFocus
    }

    private fun push(coalescing: Boolean) {
        undoStack.addLast(Entry(current, currentFocus, coalescing, now()))
        while (undoStack.size > maxDepth) undoStack.removeFirst()
    }
}