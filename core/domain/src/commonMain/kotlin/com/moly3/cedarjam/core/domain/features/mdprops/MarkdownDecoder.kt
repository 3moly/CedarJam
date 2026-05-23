package com.moly3.cedarjam.core.domain.features.mdprops

/**
 * Bridges the two halves of the Markdown stack:
 *
 *  - [FrontmatterParser] / [PropertyValue] — a typed YAML model.
 *  - [MarkdownDocument] / [MarkdownRow] / [DocumentProperty] — the editor model.
 *
 * [MarkdownDecoder] reads raw Markdown text into an editable [MarkdownDocument];
 * [MarkdownEncoder] writes a [MarkdownDocument] back out to Markdown text.
 *
 * The two are designed to round-trip: `decode(encode(doc))` reproduces `doc`
 * (modulo row ids, which are regenerated, and cosmetic whitespace).
 *
 * ```
 * val doc  = MarkdownDecoder.decode(fileText)   // text -> editor model
 * val text = MarkdownEncoder.encode(doc)        // editor model -> text
 * ```
 */

/* ====================================================================================
 *  DECODER  —  Markdown text  ->  MarkdownDocument
 * ================================================================================== */

object MarkdownDecoder {

    /**
     * Parses [source] into an editable [MarkdownDocument].
     *
     * Frontmatter (if present) becomes [MarkdownDocument.properties]; the body is
     * split into block [MarkdownRow]s. A `# Heading` on the first body line is
     * lifted into [MarkdownDocument.title] when [liftTitle] is true.
     *
     * @throws FrontmatterParser.ParseException if frontmatter is opened but never closed.
     */
    fun decode(source: String, liftTitle: Boolean = true): MarkdownDocument {
        val parsed = FrontmatterParser.parse(source)
        val properties = parsed.properties.map { (name, value) -> toDocumentProperty(name, value) }

        var rows = decodeBody(parsed.body)

        // Optionally promote a leading H1 into the document title.
        var title = ""
        if (liftTitle && rows.isNotEmpty()) {
            val first = rows.first()
            if (first.type == RowType.Heading1 && first.text.isNotBlank()) {
                title = first.text
                rows = rows.drop(1).dropWhile { it.type == RowType.Paragraph && it.text.isEmpty() }
            }
        }

        // The editor model requires at least one row.
        if (rows.isEmpty()) rows = listOf(MarkdownRow())

        return MarkdownDocument(title = title, properties = properties, rows = rows)
    }

    /* ---- frontmatter: PropertyValue -> DocumentProperty -------------------------- */

    /**
     * Converts one raw frontmatter entry into a typed [DocumentProperty].
     *
     * Type mapping:
     *  - [PropertyValue.Text]   -> [PropertyType.Text] (or Date/DateTime if it looks like one)
     *  - [PropertyValue.Number] -> [PropertyType.Number]
     *  - [PropertyValue.Bool]   -> [PropertyType.Checkbox]
     *  - [PropertyValue.ListValue] -> [PropertyType.List]
     *  - [PropertyValue.Null]   -> [PropertyType.Text] with a single empty value
     *  - [PropertyValue.MapValue] -> [PropertyType.Text] holding a flow-map rendering
     */
    private fun toDocumentProperty(name: String, value: PropertyValue): DocumentProperty = when (value) {
        is PropertyValue.Text -> DocumentProperty(
            name = name,
            type = inferTextType(value.value),
            values = listOf(value.value),
        )

        is PropertyValue.Number -> DocumentProperty(
            name = name,
            type = PropertyType.Number,
            values = listOf(value.intValue?.toString() ?: value.value.toString()),
        )

        is PropertyValue.Bool -> DocumentProperty(
            name = name,
            type = PropertyType.Checkbox,
            values = listOf(value.value.toString()),
        )

        PropertyValue.Null -> DocumentProperty(
            name = name,
            type = PropertyType.Text,
            values = listOf(""),
        )

        is PropertyValue.ListValue -> DocumentProperty(
            name = name,
            type = PropertyType.List,
            values = value.items.map { it.asStringOrNull() ?: "" },
        )

        is PropertyValue.MapValue -> DocumentProperty(
            name = name,
            type = PropertyType.Text,
            values = listOf(value.asStringOrNull() ?: ""),
        )
    }

    /** ISO-8601-ish date / date-time detection so the editor shows the right control. */
    private val dateRegex = Regex("""\d{4}-\d{2}-\d{2}""")
    private val dateTimeRegex = Regex("""\d{4}-\d{2}-\d{2}[ T]\d{2}:\d{2}(:\d{2})?.*""")

    private fun inferTextType(s: String): PropertyType = when {
        dateTimeRegex.matches(s) -> PropertyType.DateTime
        dateRegex.matches(s) -> PropertyType.Date
        else -> PropertyType.Text
    }

    /* ---- body: Markdown text -> List<MarkdownRow> -------------------------------- */

    private val fencedCodeStart = Regex("""^\s*(```+|~~~+)\s*([^\s`~]*)\s*$""")
    private val headingRegex = Regex("""^(#{1,6})\s+(.*)$""")
    private val bulletRegex = Regex("""^\s*[-*+]\s+(.*)$""")
    private val numberedRegex = Regex("""^\s*\d+[.)]\s+(.*)$""")
    private val quoteRegex = Regex("""^\s*>\s?(.*)$""")
    private val dividerRegex = Regex("""^\s*(-{3,}|\*{3,}|_{3,})\s*$""")
    private val imageRegex = Regex("""^\s*!\[[^\]]*]\(([^)]*)\)\s*$""")

    /**
     * Splits a Markdown body string into editor rows.
     *
     * Each non-blank line generally becomes one row; fenced code blocks are
     * collapsed into a single multiline [RowType.Code] row. Blank lines act as
     * separators and are not themselves rows.
     */
    private fun decodeBody(body: String): List<MarkdownRow> {
        if (body.isBlank()) return emptyList()

        val raw = body.replace("\r\n", "\n").replace("\r", "\n")
        val normalized = if (raw.endsWith("\n")) raw.dropLast(1) else raw
        val lines = normalized.split("\n")
        val rows = ArrayList<MarkdownRow>()

        var pendingBlankLines = 0
        var prevBlock: MarkdownRow? = null

        fun flushBlankLines(nextBlock: MarkdownRow?) {
            if (pendingBlankLines == 0) return

            val mergingLists = prevBlock != null && nextBlock != null && (
                    (prevBlock!!.type == RowType.BulletList && nextBlock.type == RowType.BulletList) ||
                            (prevBlock!!.type == RowType.NumberedList && nextBlock.type == RowType.NumberedList)
                    )
            if (mergingLists) {
                pendingBlankLines = 0
                return
            }

            // Leading blank lines (before any block) are frontmatter/title artifacts.
            if (prevBlock == null) {
                pendingBlankLines = 0
                return
            }

            repeat(pendingBlankLines) {
                rows.add(MarkdownRow(type = RowType.Paragraph, text = ""))
            }
            pendingBlankLines = 0
        }

        var i = 0
        while (i < lines.size) {
            val line = lines[i]

            // --- blank line ------------------------------------------------------
            if (line.isBlank()) {
                pendingBlankLines++
                i++
                continue
            }

            // --- fenced code block -----------------------------------------------
            val fence = fencedCodeStart.find(line)
            if (fence != null) {
                val fenceToken = fence.groupValues[1]
                val language = fence.groupValues[2]
                val codeLines = ArrayList<String>()
                i++
                while (i < lines.size && !isClosingFence(lines[i], fenceToken)) {
                    codeLines.add(lines[i])
                    i++
                }
                if (i < lines.size) i++ // consume closing fence
                val codeRow = MarkdownRow(
                    type = RowType.Code,
                    text = codeLines.joinToString("\n"),
                    codeLanguage = language,
                )
                flushBlankLines(codeRow)
                rows.add(codeRow)
                prevBlock = codeRow
                continue
            }

            // --- single-line block types -----------------------------------------
            val row = decodeLine(line)
            flushBlankLines(row)
            rows.add(row)
            prevBlock = row
            i++
        }

        // Flush any trailing blank lines at the end of the file
        flushBlankLines(null)

        return rows
    }

    /** A closing fence must use the same fence char and be at least as long. */
    private fun isClosingFence(line: String, openToken: String): Boolean {
        val trimmed = line.trim()
        val fenceChar = openToken.first()
        if (trimmed.isEmpty() || trimmed.any { it != fenceChar }) return false
        return trimmed.length >= openToken.length
    }

    /** Classifies a single body line into a typed [MarkdownRow]. */
    private fun decodeLine(line: String): MarkdownRow {
        dividerRegex.matchEntire(line)?.let {
            // Keep the raw rule source so the editor can reveal & edit it.
            return MarkdownRow(type = RowType.Divider, text = it.groupValues[1].trim())
        }
        imageRegex.matchEntire(line)?.let {
            return MarkdownRow(type = RowType.Image, text = it.groupValues[1].trim())
        }
        headingRegex.matchEntire(line)?.let {
            val level = it.groupValues[1].length
            val type = when (level) {
                1 -> RowType.Heading1
                2 -> RowType.Heading2
                else -> RowType.Heading3   // H3..H6 all collapse to Heading3
            }
            return MarkdownRow(type = type, text = it.groupValues[2].trim())
        }
        quoteRegex.matchEntire(line)?.let {
            return MarkdownRow(type = RowType.Quote, text = it.groupValues[1])
        }
        bulletRegex.matchEntire(line)?.let {
            return MarkdownRow(type = RowType.BulletList, text = it.groupValues[1])
        }
        numberedRegex.matchEntire(line)?.let {
            return MarkdownRow(type = RowType.NumberedList, text = it.groupValues[1])
        }
        return MarkdownRow(type = RowType.Paragraph, text = line.trimEnd())
    }
}

/* ====================================================================================
 *  ENCODER  —  MarkdownDocument  ->  Markdown text
 * ================================================================================== */

object MarkdownEncoder {

    /**
     * Serialises [doc] to a Markdown string.
     *
     * Output layout:
     * ```
     * ---
     * <frontmatter>
     * ---
     *
     * # <title>
     *
     * <body rows>
     * ```
     *
     * Frontmatter is omitted entirely when [doc] has no properties. The title,
     * if non-empty, is written as a leading H1 so [MarkdownDecoder.decode] can
     * lift it back out.
     */
    fun encode(doc: MarkdownDocument): String {
        val sb = StringBuilder()

        if (doc.properties.isNotEmpty()) {
            sb.append(encodeFrontmatter(doc.properties))
            sb.append("\n\n")
        }

        if (doc.title.isNotBlank()) {
            sb.append("# ").append(doc.title).append("\n\n")
        }

        sb.append(encodeBody(doc.rows))
        val s = sb.toString()
        return if (s.endsWith("\n")) s else "$s\n"
    }

    /* ---- frontmatter: List<DocumentProperty> -> YAML ----------------------------- */

    /** YAML frontmatter fence; matches [FrontmatterParser]'s expected delimiter. */
    private const val DELIMITER = "---"

    private fun encodeFrontmatter(properties: List<DocumentProperty>): String {
        val sb = StringBuilder()
        sb.append(DELIMITER).append("\n")
        for (prop in properties) {
            sb.append(encodeProperty(prop)).append("\n")
        }
        sb.append(DELIMITER)
        return sb.toString()
    }

    private fun encodeProperty(prop: DocumentProperty): String {
        val key = encodeKey(prop.name)
        return when (prop.type) {
            PropertyType.List -> {
                if (prop.values.isEmpty()) {
                    "$key: []"
                } else {
                    buildString {
                        append(key).append(":")
                        for (v in prop.values) {
                            append("\n  - ").append(encodeScalar(v))
                        }
                    }
                }
            }

            PropertyType.Checkbox -> {
                val b = prop.singleValue.trim().lowercase()
                val normalized = if (b == "true" || b == "yes") "true" else "false"
                "$key: $normalized"
            }

            PropertyType.Number -> {
                val raw = prop.singleValue.trim()
                // Emit a bare number if it parses, otherwise fall back to a quoted string.
                if (raw.toDoubleOrNull() != null) "$key: $raw" else "$key: ${quote(raw)}"
            }

            PropertyType.Text, PropertyType.Date, PropertyType.DateTime -> {
                val raw = prop.singleValue
                if (raw.isEmpty()) "$key:" else "$key: ${encodeScalar(raw)}"
            }
        }
    }

    /** Quotes a key only when it contains characters that would break parsing. */
    private fun encodeKey(name: String): String =
        if (name.isEmpty() || name.any { it == ':' || it == '#' || it == '"' || it == '\'' } ||
            name != name.trim()
        ) {
            quote(name)
        } else {
            name
        }

    /**
     * Renders a scalar value, quoting when bare text would be misread by the
     * parser (e.g. would be inferred as a number/bool/null, or contains YAML
     * control characters).
     */
    private fun encodeScalar(value: String): String {
        if (value.isEmpty()) return "\"\""
        return if (needsQuoting(value)) quote(value) else value
    }

    private val ambiguousScalars = setOf(
        "true", "True", "TRUE", "yes", "Yes", "YES",
        "false", "False", "FALSE", "no", "No", "NO",
        "null", "Null", "NULL", "~",
    )

    private fun needsQuoting(value: String): Boolean {
        if (value != value.trim()) return true
        if (value in ambiguousScalars) return true
        if (value.toDoubleOrNull() != null) return true
        val first = value.first()
        if (first == '[' || first == '{' || first == '"' || first == '\'' ||
            first == '-' || first == '#' || first == '>' || first == '|' ||
            first == '*' || first == '&' || first == '!' || first == '%' || first == '@'
        ) return true
        if (value.contains(": ") || value.endsWith(":")) return true
        if (value.contains(" #")) return true
        if (value.contains('\n')) return true
        return false
    }

    /** Double-quotes a string, escaping the characters [ScalarParser] understands. */
    private fun quote(s: String): String {
        val sb = StringBuilder(s.length + 2)
        sb.append('"')
        for (c in s) {
            when (c) {
                '\\' -> sb.append("\\\\")
                '"' -> sb.append("\\\"")
                '\n' -> sb.append("\\n")
                '\t' -> sb.append("\\t")
                '\r' -> sb.append("\\r")
                '\u0000' -> sb.append("\\0")
                else -> sb.append(c)
            }
        }
        sb.append('"')
        return sb.toString()
    }

    /* ---- body: List<MarkdownRow> -> Markdown text -------------------------------- */

    /**
     * Renders an arbitrary list of [rows] to Markdown text, with no frontmatter
     * or title. Used to copy a multi-row block selection to the clipboard as
     * raw Markdown. The output is the same body text [encode] would produce for
     * those rows, trimmed of trailing whitespace.
     */
    fun encodeRows(rows: List<MarkdownRow>): String =
        encodeBody(rows).trimEnd()

    /**
     * Renders rows to Markdown. Adjacent list items of the same kind stay on
     * consecutive lines; every other block is separated by a blank line, which
     * mirrors how [MarkdownDecoder] re-reads the text.
     */
    private fun encodeBody(rows: List<MarkdownRow>): String {
        val sb = StringBuilder()
        var numberedCounter = 0
        var lastEmittedRow: MarkdownRow? = null

        rows.forEach { row ->
            // DO NOT skip empty paragraphs anymore! We want them saved.

            numberedCounter = if (row.type == RowType.NumberedList) {
                if (lastEmittedRow?.type == RowType.NumberedList) numberedCounter + 1 else 1
            } else {
                0
            }

            if (lastEmittedRow != null && needsBlankLineBetween(lastEmittedRow, row)) {
                sb.append("\n")
            }

            sb.append(encodeRow(row, numberedCounter))
            sb.append("\n")

            lastEmittedRow = row
        }
        return sb.toString()
    }

    private fun needsBlankLineBetween(prev: MarkdownRow?, current: MarkdownRow): Boolean {
        if (prev == null) return false

        val isPrevEmpty = prev.type == RowType.Paragraph && prev.text.isBlank()
        val isCurrentEmpty = current.type == RowType.Paragraph && current.text.isBlank()

        // Empty blocks carry their own newline; never double-space around them.
        if (isPrevEmpty || isCurrentEmpty) return false

        // Otherwise, adjacent non-empty blocks butt up against each other.
        // The user's explicit empty rows are the only source of vertical spacing.
        return false
    }

    private fun encodeRow(row: MarkdownRow, numberedIndex: Int): String = when (row.type) {
        RowType.Paragraph -> row.text
        RowType.Heading1 -> "# ${row.text}"
        RowType.Heading2 -> "## ${row.text}"
        RowType.Heading3 -> "### ${row.text}"
        RowType.BulletList -> "- ${row.text}"
        RowType.NumberedList -> "${numberedIndex.coerceAtLeast(1)}. ${row.text}"
        RowType.Quote -> if (row.text.isEmpty()) ">" else "> ${row.text}"
        RowType.Code -> encodeCodeRow(row)
        RowType.Image -> "![](${row.text})"
        RowType.Divider -> if (row.text.isBlank()) DividerSyntax.CANONICAL else row.text.trim()
    }

    /** Fences a code row, widening the fence if the body itself contains backticks. */
    private fun encodeCodeRow(row: MarkdownRow): String {
        var fence = "```"
        while (row.text.contains(fence)) fence += "`"
        return buildString {
            append(fence)
            if (row.codeLanguage.isNotBlank()) append(row.codeLanguage)
            append("\n")
            append(row.text)
            if (row.text.isNotEmpty()) append("\n")
            append(fence)
        }
    }
}