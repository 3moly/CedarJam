package com.moly3.cedarjam.core.domain.features.mdprops

/**
 * Parses Obsidian-style Markdown "properties" — YAML frontmatter delimited by
 * `---` lines at the very start of a document.
 *
 * This is a focused subset of YAML, not a full implementation. It supports:
 *  - scalar properties with type inference (text / number / boolean / null)
 *  - inline flow lists `[a, b, c]` and flow maps `{k: v}`
 *  - block lists (`- item` lines) including lists of maps
 *  - nested block maps via indentation
 *  - quoted strings (single and double), `#` comments, blank lines
 *
 * It does NOT support anchors, aliases, multi-document streams, or the
 * `|` / `>` block scalar indicators (these are treated leniently).
 *
 * Usage:
 * ```
 * val doc = FrontmatterParser.parse(markdownText)
 * val title = doc.text("title")
 * val tags = doc.stringList("tags")
 * ```
 */
object FrontmatterParser {

    private const val DELIMITER = "---"

    /** Thrown when frontmatter is present but structurally invalid. */
    class ParseException(message: String) : Exception(message)

    /**
     * Parses [source]. If no frontmatter block is found the whole input is
     * returned as [ParsedDocument.body] with empty [ParsedDocument.properties].
     *
     * @throws ParseException if a frontmatter block is opened but never closed.
     */
    fun parse(source: String): ParsedDocument {
        val normalized = source.replace("\r\n", "\n").replace("\r", "\n")
        val lines = normalized.split("\n")

        // Frontmatter must start on the very first line.
        if (lines.isEmpty() || lines[0].trim() != DELIMITER) {
            return ParsedDocument(emptyMap(), source)
        }

        // Find the closing delimiter.
        var closeIndex = -1
        for (i in 1 until lines.size) {
            if (lines[i].trim() == DELIMITER) {
                closeIndex = i
                break
            }
        }
        if (closeIndex == -1) {
            throw ParseException("Frontmatter opened with '---' but never closed.")
        }

        val yamlLines = lines.subList(1, closeIndex)
        // Body is everything after the closing delimiter, joined back.
        val body = if (closeIndex + 1 <= lines.lastIndex) {
            lines.subList(closeIndex + 1, lines.size).joinToString("\n")
        } else {
            ""
        }

        val properties = parseBlock(stripComments(yamlLines), baseIndent = 0, ctx = Cursor(0))
        return ParsedDocument(properties, body).also { it.frontmatterPresent = true }
    }

    /** Returns only the properties map; convenience for callers that ignore the body. */
    fun parseProperties(source: String): Map<String, PropertyValue> = parse(source).properties

    // --- internal parsing machinery -------------------------------------

    /** Mutable line index shared while recursing through indented blocks. */
    private class Cursor(var index: Int)

    /** Drops full-line and trailing comments and blank-only lines that carry no data. */
    private fun stripComments(lines: List<String>): List<String> = lines.map { stripTrailingComment(it) }

    /**
     * Removes a trailing ` # comment` from a line, while respecting quotes so
     * that `url: "http://x#y"` is not truncated.
     */
    private fun stripTrailingComment(line: String): String {
        var inSingle = false
        var inDouble = false
        var i = 0
        while (i < line.length) {
            when (val c = line[i]) {
                '\'' -> if (!inDouble) inSingle = !inSingle
                '"' -> if (!inSingle) inDouble = !inDouble
                '#' -> if (!inSingle && !inDouble) {
                    // A comment must be at line start or preceded by whitespace.
                    if (i == 0 || line[i - 1].isWhitespace()) {
                        return line.substring(0, i).trimEnd()
                    }
                }
            }
            i++
        }
        return line
    }

    private fun indentOf(line: String): Int = line.indexOfFirst { !it.isWhitespace() }.let { if (it < 0) Int.MAX_VALUE else it }

    private fun isBlank(line: String): Boolean = line.isBlank()

    /**
     * Parses a block of `key: value` mappings at [baseIndent], advancing [ctx]
     * past every line consumed.
     */
    private fun parseBlock(lines: List<String>, baseIndent: Int, ctx: Cursor): Map<String, PropertyValue> {
        val result = LinkedHashMap<String, PropertyValue>()

        while (ctx.index < lines.size) {
            val line = lines[ctx.index]
            if (isBlank(line)) { ctx.index++; continue }

            val indent = indentOf(line)
            if (indent < baseIndent) break          // dedent: this block is done
            if (indent > baseIndent) {
                throw ParseException("Unexpected indentation at line: '${line.trim()}'")
            }

            val content = line.trim()
            if (content.startsWith("- ") || content == "-") {
                throw ParseException("Found a list item where a mapping was expected: '$content'")
            }

            val colon = findKeyColon(content)
                ?: throw ParseException("Expected 'key: value' but found: '$content'")

            val key = unquoteKey(content.substring(0, colon).trim())
            val rest = content.substring(colon + 1).trim()
            ctx.index++

            if (rest.isNotEmpty()) {
                // Inline value on the same line.
                result[key] = parseInlineValue(rest)
            } else {
                // Value is on following indented lines (block list, block map) or null.
                result[key] = parseNestedValue(lines, baseIndent, ctx)
            }
        }
        return result
    }

    /**
     * Determines the value attached to a key whose line ended after the colon:
     * looks ahead at indentation to decide between a block list, a nested map,
     * or an explicit null.
     */
    private fun parseNestedValue(lines: List<String>, parentIndent: Int, ctx: Cursor): PropertyValue {
        // Skip blank lines to peek at the next meaningful line.
        var peek = ctx.index
        while (peek < lines.size && isBlank(lines[peek])) peek++
        if (peek >= lines.size) return PropertyValue.Null

        val childIndent = indentOf(lines[peek])
        if (childIndent <= parentIndent) {
            // Nothing nested -> explicit null.
            return PropertyValue.Null
        }

        ctx.index = peek
        val childContent = lines[peek].trim()
        return if (childContent.startsWith("- ") || childContent == "-") {
            parseBlockList(lines, childIndent, ctx)
        } else {
            PropertyValue.MapValue(parseBlock(lines, childIndent, ctx))
        }
    }

    /** Parses a sequence of `- item` lines at [listIndent]. */
    private fun parseBlockList(lines: List<String>, listIndent: Int, ctx: Cursor): PropertyValue {
        val items = ArrayList<PropertyValue>()

        while (ctx.index < lines.size) {
            val line = lines[ctx.index]
            if (isBlank(line)) { ctx.index++; continue }

            val indent = indentOf(line)
            if (indent < listIndent) break
            if (indent > listIndent) {
                throw ParseException("Unexpected indentation in list at: '${line.trim()}'")
            }

            val content = line.trim()
            if (content != "-" && !content.startsWith("- ")) break

            val afterDash = if (content == "-") "" else content.substring(2).trim()
            ctx.index++

            if (afterDash.isEmpty()) {
                // Nested structure under this dash.
                items.add(parseNestedValue(lines, listIndent, ctx))
            } else if (findKeyColon(afterDash) != null && !looksLikeFlow(afterDash)) {
                // A list item that is itself a mapping: `- key: value`.
                // The dash adds a virtual indent equal to listIndent + 2.
                items.add(parseInlineMapItem(afterDash, lines, listIndent, ctx))
            } else {
                items.add(parseInlineValue(afterDash))
            }
        }
        return PropertyValue.ListValue(items)
    }

    /**
     * Handles `- key: value` plus any further keys belonging to the same map
     * item, which appear indented to align past the dash.
     */
    private fun parseInlineMapItem(
        firstPair: String,
        lines: List<String>,
        listIndent: Int,
        ctx: Cursor,
    ): PropertyValue {
        val map = LinkedHashMap<String, PropertyValue>()
        val colon = findKeyColon(firstPair)!!
        val key = unquoteKey(firstPair.substring(0, colon).trim())
        val rest = firstPair.substring(colon + 1).trim()

        if (rest.isNotEmpty()) {
            map[key] = parseInlineValue(rest)
        } else {
            map[key] = parseNestedValue(lines, listIndent + 1, ctx)
        }

        // Continuation keys: indented strictly more than the dash position.
        while (ctx.index < lines.size) {
            val line = lines[ctx.index]
            if (isBlank(line)) { ctx.index++; continue }
            val indent = indentOf(line)
            if (indent <= listIndent) break
            val content = line.trim()
            if (content.startsWith("- ")) break
            val c = findKeyColon(content) ?: break
            val k = unquoteKey(content.substring(0, c).trim())
            val r = content.substring(c + 1).trim()
            ctx.index++
            map[k] = if (r.isNotEmpty()) parseInlineValue(r) else parseNestedValue(lines, indent, ctx)
        }
        return PropertyValue.MapValue(map)
    }

    private fun looksLikeFlow(s: String): Boolean =
        s.startsWith("[") || s.startsWith("{") || s.startsWith("\"") || s.startsWith("'")

    /** Parses an inline value: flow list, flow map, or scalar. */
    private fun parseInlineValue(raw: String): PropertyValue {
        val token = raw.trim()
        if (token.startsWith("[")) return parseFlowList(token)
        if (token.startsWith("{")) return parseFlowMap(token)
        return ScalarParser.parse(token)
    }

    private fun parseFlowList(token: String): PropertyValue {
        require(token.startsWith("["))
        if (!token.endsWith("]")) throw ParseException("Unterminated flow list: '$token'")
        val inner = token.substring(1, token.length - 1)
        val parts = splitFlow(inner)
        return PropertyValue.ListValue(parts.map { parseInlineValue(it) })
    }

    private fun parseFlowMap(token: String): PropertyValue {
        require(token.startsWith("{"))
        if (!token.endsWith("}")) throw ParseException("Unterminated flow map: '$token'")
        val inner = token.substring(1, token.length - 1)
        val parts = splitFlow(inner)
        val map = LinkedHashMap<String, PropertyValue>()
        for (part in parts) {
            val colon = findKeyColon(part)
                ?: throw ParseException("Expected 'key: value' in flow map but found: '$part'")
            val k = unquoteKey(part.substring(0, colon).trim())
            val v = part.substring(colon + 1).trim()
            map[k] = parseInlineValue(v)
        }
        return PropertyValue.MapValue(map)
    }

    /**
     * Splits a flow-collection body on top-level commas, respecting nested
     * `[]`, `{}` and quoted strings.
     */
    private fun splitFlow(inner: String): List<String> {
        if (inner.isBlank()) return emptyList()
        val parts = ArrayList<String>()
        val sb = StringBuilder()
        var depth = 0
        var inSingle = false
        var inDouble = false
        for (c in inner) {
            when {
                c == '\'' && !inDouble -> { inSingle = !inSingle; sb.append(c) }
                c == '"' && !inSingle -> { inDouble = !inDouble; sb.append(c) }
                inSingle || inDouble -> sb.append(c)
                c == '[' || c == '{' -> { depth++; sb.append(c) }
                c == ']' || c == '}' -> { depth--; sb.append(c) }
                c == ',' && depth == 0 -> { parts.add(sb.toString()); sb.setLength(0) }
                else -> sb.append(c)
            }
        }
        parts.add(sb.toString())
        return parts.map { it.trim() }.filter { it.isNotEmpty() }
    }

    /**
     * Finds the colon separating a key from its value, ignoring colons inside
     * quotes and bracketed flow collections. Returns `null` if there is none.
     */
    private fun findKeyColon(s: String): Int? {
        var depth = 0
        var inSingle = false
        var inDouble = false
        var i = 0
        while (i < s.length) {
            val c = s[i]
            when {
                c == '\'' && !inDouble -> inSingle = !inSingle
                c == '"' && !inSingle -> inDouble = !inDouble
                inSingle || inDouble -> {}
                c == '[' || c == '{' -> depth++
                c == ']' || c == '}' -> depth--
                c == ':' && depth == 0 -> {
                    // YAML requires `: ` (colon + space) or colon at end of token.
                    if (i == s.length - 1 || s[i + 1].isWhitespace()) return i
                }
            }
            i++
        }
        return null
    }

    private fun unquoteKey(key: String): String {
        if (key.length >= 2) {
            if (key.first() == '"' && key.last() == '"') return key.substring(1, key.length - 1)
            if (key.first() == '\'' && key.last() == '\'') return key.substring(1, key.length - 1)
        }
        return key
    }
}