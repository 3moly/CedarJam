package com.moly3.cedarjam.core.domain.features.search

/**
 * Anything the search engine can match against.
 *
 * In Obsidian this would normally be a note file, but the [type] field
 * generalises it so the same engine can index tags, collections, table
 * rows, directories and annotations alongside files.
 */
data class Searchable(
    /** What kind of thing this is — drives the `type:` operator. */
    val type: ItemType,
    /** Display name / filename (no path). */
    val fileName: String,
    /** Full path from the vault root. */
    val path: String,
    /** Raw textual content. */
    val content: String,
    /** Tags attached to the item, without the leading `#`. */
    val tags: Set<String> = emptySet(),
    /** Frontmatter / inline properties. A value may be a String, Number,
     *  Boolean, a List of those, or null for an empty property. */
    val properties: Map<String, Any?> = emptyMap(),
    /** Tasks found in the item, used by task: operators. */
    val tasks: List<Task> = emptyList(),
) {
    /** Content split into lines, for the `line:` operator. */
    val lines: List<String> by lazy { content.split('\n') }

    /** Content split into blocks (separated by blank lines), for `block:`. */
    val blocks: List<String> by lazy {
        content.split(kotlin.text.Regex("\\n\\s*\\n")).filter { it.isNotBlank() }
    }

    /** Content split into sections (text from one heading to the next). */
    val sections: List<String> by lazy { splitSections(content) }

    private fun splitSections(text: String): List<String> {
        val result = mutableListOf<String>()
        val current = StringBuilder()
        val headingRegex = kotlin.text.Regex("^#{1,6}\\s")
        for (line in text.split('\n')) {
            if (headingRegex.containsMatchIn(line) && current.isNotEmpty()) {
                result += current.toString()
                current.clear()
            }
            current.appendLine(line)
        }
        if (current.isNotEmpty()) result += current.toString()
        return result
    }
}

/** A single task line. */
data class Task(
    val text: String,
    val done: Boolean,
)