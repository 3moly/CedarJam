package com.moly3.cedarjam.core.domain.features.mdprops

/**
 * A typed representation of a value found in Markdown frontmatter (YAML).
 *
 * Obsidian's "Properties" feature maps onto these types:
 *  - Text      -> [Text]
 *  - Number    -> [Number]
 *  - Checkbox  -> [Bool]
 *  - Date / Date & time -> [Text] (kept as a string; callers can parse further)
 *  - List      -> [ListValue]
 *  - (nested maps from raw YAML) -> [MapValue]
 *  - empty / `null` / `~` -> [Null]
 */
sealed interface PropertyValue {

    /** A scalar string value, e.g. `title: Hello`. */
    data class Text(val value: String) : PropertyValue

    /** A numeric value. Stored as [Double]; use [intValue] for whole numbers. */
    data class Number(val value: Double) : PropertyValue {
        /** Returns the value as a [Long] if it has no fractional part, else `null`. */
        val intValue: Long?
            get() = if (value % 1.0 == 0.0 && !value.isInfinite()) value.toLong() else null
    }

    /** A boolean value, e.g. `published: true`. */
    data class Bool(val value: Boolean) : PropertyValue

    /** An explicit null (`key:`, `key: null`, `key: ~`). */
    data object Null : PropertyValue

    /** A YAML sequence / Obsidian list, e.g. `tags: [a, b]` or block-style `- a`. */
    data class ListValue(val items: List<PropertyValue>) : PropertyValue

    /** A nested YAML mapping. */
    data class MapValue(val entries: Map<String, PropertyValue>) : PropertyValue
}

/**
 * The result of parsing a Markdown document: its frontmatter [properties]
 * and the document [body] with the frontmatter block removed.
 */
data class ParsedDocument(
    val properties: Map<String, PropertyValue>,
    val body: String,
) {
    val hasFrontmatter: Boolean get() = properties.isNotEmpty() || frontmatterPresent

    /** Internal flag: a frontmatter block existed even if it was empty. */
    internal var frontmatterPresent: Boolean = false

    /** Convenience: get a property as text, returning `null` if absent or not text. */
    fun text(key: String): String? = (properties[key] as? PropertyValue.Text)?.value

    /** Convenience: get a property as a number. */
    fun number(key: String): Double? = (properties[key] as? PropertyValue.Number)?.value

    /** Convenience: get a property as a boolean. */
    fun bool(key: String): Boolean? = (properties[key] as? PropertyValue.Bool)?.value

    /**
     * Convenience: get a property as a list of strings.
     * Non-text items are coerced via [PropertyValue] string form; nulls are dropped.
     */
    fun stringList(key: String): List<String>? {
        val v = properties[key] ?: return null
        return when (v) {
            is PropertyValue.ListValue -> v.items.mapNotNull { it.asStringOrNull() }
            is PropertyValue.Text -> listOf(v.value)
            else -> null
        }
    }
}

/** Best-effort string representation of a scalar value; `null` for [PropertyValue.Null]. */
fun PropertyValue.asStringOrNull(): String? = when (this) {
    is PropertyValue.Text -> value
    is PropertyValue.Number -> intValue?.toString() ?: value.toString()
    is PropertyValue.Bool -> value.toString()
    PropertyValue.Null -> null
    is PropertyValue.ListValue -> items.joinToString(", ") { it.asStringOrNull() ?: "" }
    is PropertyValue.MapValue -> entries.entries.joinToString(", ") { "${it.key}: ${it.value.asStringOrNull()}" }
}