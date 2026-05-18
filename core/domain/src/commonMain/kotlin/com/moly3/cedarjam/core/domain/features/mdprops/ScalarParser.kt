package com.moly3.cedarjam.core.domain.features.mdprops

/**
 * Converts a raw scalar token (the text after `key:` or a `-` list item)
 * into a typed [PropertyValue], following YAML 1.1-ish / Obsidian conventions.
 */
internal object ScalarParser {

    private val nullTokens = setOf("", "~", "null", "Null", "NULL")
    private val trueTokens = setOf("true", "True", "TRUE", "yes", "Yes", "YES")
    private val falseTokens = setOf("false", "False", "FALSE", "no", "No", "NO")

    fun parse(raw: String): PropertyValue {
        val token = raw.trim()

        // Quoted strings are always text; quotes are stripped and escapes resolved.
        if (token.length >= 2) {
            if (token.first() == '"' && token.last() == '"') {
                return PropertyValue.Text(unescapeDoubleQuoted(token.substring(1, token.length - 1)))
            }
            if (token.first() == '\'' && token.last() == '\'') {
                // YAML single-quote: '' is a literal '
                return PropertyValue.Text(token.substring(1, token.length - 1).replace("''", "'"))
            }
        }

        if (token in nullTokens) return PropertyValue.Null
        if (token in trueTokens) return PropertyValue.Bool(true)
        if (token in falseTokens) return PropertyValue.Bool(false)

        parseNumber(token)?.let { return PropertyValue.Number(it) }

        return PropertyValue.Text(token)
    }

    /**
     * Parses a number only if the *entire* token is numeric. Leading `+`,
     * underscores as digit separators, and scientific notation are accepted.
     * Bare integers with leading zeros (e.g. `007`) are treated as text to
     * avoid surprising octal/ID coercion — matching Obsidian's behavior.
     */
    private fun parseNumber(token: String): Double? {
        if (token.isEmpty()) return null

        val cleaned = token.replace("_", "")
        if (cleaned.isEmpty()) return null

        // Reject things Double would otherwise happily accept.
        if (cleaned.equals("nan", ignoreCase = true)) return null
        if (cleaned.endsWith("d", ignoreCase = true) ||
            cleaned.endsWith("f", ignoreCase = true)
        ) return null
        if (cleaned.lastOrNull()?.isDigit() == false && cleaned.last() != '.') return null

        // Preserve leading-zero strings (ZIP codes, IDs) as text.
        val digitsPart = cleaned.removePrefix("+").removePrefix("-")
        if (digitsPart.length > 1 &&
            digitsPart[0] == '0' &&
            digitsPart[1].isDigit()
        ) return null

        return cleaned.toDoubleOrNull()
    }

    private fun unescapeDoubleQuoted(s: String): String {
        if (!s.contains('\\')) return s
        val sb = StringBuilder(s.length)
        var i = 0
        while (i < s.length) {
            val c = s[i]
            if (c == '\\' && i + 1 < s.length) {
                when (val next = s[i + 1]) {
                    'n' -> sb.append('\n')
                    't' -> sb.append('\t')
                    'r' -> sb.append('\r')
                    '"' -> sb.append('"')
                    '\\' -> sb.append('\\')
                    '0' -> sb.append('\u0000')
                    else -> { sb.append(c); sb.append(next) }
                }
                i += 2
            } else {
                sb.append(c)
                i++
            }
        }
        return sb.toString()
    }
}