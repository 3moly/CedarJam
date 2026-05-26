package com.moly3.cedarjam.core.domain.features.mdprops

/**
 * Recognises Markdown horizontal-rule ("divider") syntax.
 *
 * A divider row keeps its raw source in [MarkdownRow.text] — e.g. `---`, `***`,
 * `___` — so the editor can reveal and edit it like Obsidian does: a focused
 * divider shows its source text; a blurred one renders as a horizontal line.
 *
 * When a [RowType.Divider] row is edited so its text no longer matches, the
 * editor demotes it to a [RowType.Paragraph]; when a paragraph's text becomes a
 * valid rule, callers may promote it. [isDivider] is the single source of truth
 * for that decision, shared with [MarkdownDecoder] so parsing and in-editor
 * behaviour never disagree.
 */
object DividerSyntax {

    /** Matches a CommonMark thematic break: 3+ of `-`, `*`, or `_`, spaces allowed. */
    private val pattern = Regex("""^\s*([-*_])(\s*\1){2,}\s*$""")

    /** The canonical divider text inserted when a divider row has no source yet. */
    const val CANONICAL = "---"

    /** True when [text] is a valid Markdown horizontal rule. */
    fun isDivider(text: String): Boolean = pattern.matches(text)

    /**
     * Normalises divider source for storage: blank input becomes [CANONICAL],
     * anything else is returned trimmed of trailing whitespace.
     */
    fun normalize(text: String): String =
        if (text.isBlank()) CANONICAL else text.trimEnd()
}