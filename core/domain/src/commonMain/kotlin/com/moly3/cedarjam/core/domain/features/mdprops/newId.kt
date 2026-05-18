package com.moly3.cedarjam.core.domain.features.mdprops

import androidx.compose.runtime.Immutable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Data model for the Markdown editor.
 *
 * The whole model is immutable. The editor never mutates state in place — every change
 * produces a new [MarkdownDocument] which is handed back to the caller through the
 * `onDocumentChange` lambda. This keeps it predictable on Compose Multiplatform and
 * makes undo/redo trivial to add later if needed.
 */

@OptIn(ExperimentalUuidApi::class)
internal fun newId(): String = Uuid.random().toString()

/* ----------------------------------------------------------------------------------
 * Document
 * -------------------------------------------------------------------------------- */

@Immutable
data class MarkdownDocument(
    val title: String = "",
    val properties: List<DocumentProperty> = emptyList(),
    val rows: List<MarkdownRow> = listOf(MarkdownRow()),
) {
    /** Index of a row by id, or -1. */
    fun indexOfRow(rowId: String): Int = rows.indexOfFirst { it.id == rowId }

    fun rowOrNull(rowId: String): MarkdownRow? = rows.firstOrNull { it.id == rowId }

    fun replaceRow(rowId: String, transform: (MarkdownRow) -> MarkdownRow): MarkdownDocument {
        val idx = indexOfRow(rowId)
        if (idx < 0) return this
        return copy(rows = rows.toMutableList().also { it[idx] = transform(it[idx]) })
    }

    fun insertRowAfter(rowId: String, row: MarkdownRow): MarkdownDocument {
        val idx = indexOfRow(rowId)
        val at = if (idx < 0) rows.size else idx + 1
        return copy(rows = rows.toMutableList().also { it.add(at, row) })
    }

    fun removeRow(rowId: String): MarkdownDocument {
        if (rows.size <= 1) return this // always keep at least one row
        return copy(rows = rows.filterNot { it.id == rowId })
    }
}

/* ----------------------------------------------------------------------------------
 * Properties — Obsidian-style frontmatter
 * -------------------------------------------------------------------------------- */

/** The kinds of property values Obsidian supports. */
enum class PropertyType(val label: String) {
    Text("Text"),
    Number("Number"),
    Checkbox("Checkbox"),
    Date("Date"),
    DateTime("Date & time"),
    List("List"),          // multi-value, e.g. tags
}

@Immutable
data class DocumentProperty(
    val id: String = newId(),
    val name: String = "",
    val type: PropertyType = PropertyType.Text,
    /** Single-value types store one entry; List uses all entries. */
    val values: List<String> = listOf(""),
) {
    val singleValue: String get() = values.firstOrNull().orEmpty()

    fun withSingleValue(v: String) = copy(values = listOf(v))
}

/* ----------------------------------------------------------------------------------
 * Rows — Notion-style blocks
 * -------------------------------------------------------------------------------- */

/**
 * Every row in the document has exactly one type. The slash menu ("/") switches
 * a row from one type to another.
 */
enum class RowType(
    val menuLabel: String,
    val menuHint: String,
    /** A code block keeps Shift+Enter newlines inside one row. */
    val isMultiline: Boolean = false,
) {
    Paragraph("Text", "Plain paragraph"),
    Heading1("Heading 1", "Big section heading"),
    Heading2("Heading 2", "Medium section heading"),
    Heading3("Heading 3", "Small section heading"),
    BulletList("Bulleted list", "A simple bullet"),
    NumberedList("Numbered list", "A numbered item"),
    Quote("Quote", "Block quote"),
    Code("Code", "Multiline code block", isMultiline = true),
    Image("Image", "Embed an image by URL"),
    Divider("Divider", "Horizontal rule"),
}

@Immutable
data class MarkdownRow(
    val id: String = newId(),
    val type: RowType = RowType.Paragraph,
    /** Main text content. For [RowType.Image] this holds the image URL. */
    val text: String = "",
    /** Optional language tag for [RowType.Code]. */
    val codeLanguage: String = "",
)