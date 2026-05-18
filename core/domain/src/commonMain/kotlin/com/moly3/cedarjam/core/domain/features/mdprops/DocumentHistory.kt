package com.moly3.cedarjam.core.domain.features.mdprops

import androidx.compose.runtime.Stable
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
@Stable
class DocumentHistory(
    initial: MarkdownDocument,
    private val maxDepth: Int = 200,
    private val coalesceWindowMs: Long = 600L,
    private val now: () -> Long = { System.now().toEpochMilliseconds() },
) {
    private data class Entry(val doc: MarkdownDocument, val coalescing: Boolean, val at: Long)

    private val undoStack = ArrayDeque<Entry>()
    private val redoStack = ArrayDeque<Entry>()

    var current: MarkdownDocument = initial
        private set

    val canUndo: Boolean get() = undoStack.isNotEmpty()
    val canRedo: Boolean get() = redoStack.isNotEmpty()

    /** A discrete, non-mergeable edit. */
    fun commit(next: MarkdownDocument) {
        if (next == current) return
        push(coalescing = false)
        current = next
        redoStack.clear()
    }

    /** A typing edit; merges into the previous coalescing step if recent enough. */
    fun commitCoalescing(next: MarkdownDocument) {
        if (next == current) return
        val last = undoStack.lastOrNull()
        val mergeable = last != null && last.coalescing &&
                (now() - last.at) <= coalesceWindowMs
        // If not mergeable, push the current state as a fresh coalescing checkpoint.
        if (!mergeable) push(coalescing = true)
        // If mergeable, we keep the existing checkpoint and just advance current —
        // the stack already holds the pre-burst state.
        current = next
        redoStack.clear()
    }

    fun undo(): MarkdownDocument? {
        val prev = undoStack.removeLastOrNull() ?: return null
        redoStack.addLast(Entry(current, prev.coalescing, now()))
        current = prev.doc
        return current
    }

    fun redo(): MarkdownDocument? {
        val next = redoStack.removeLastOrNull() ?: return null
        undoStack.addLast(Entry(current, next.coalescing, now()))
        current = next.doc
        return current
    }

    private fun push(coalescing: Boolean) {
        undoStack.addLast(Entry(current, coalescing, now()))
        while (undoStack.size > maxDepth) undoStack.removeFirst()
    }
}