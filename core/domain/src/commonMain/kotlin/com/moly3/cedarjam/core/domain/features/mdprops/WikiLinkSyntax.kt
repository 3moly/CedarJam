package com.moly3.cedarjam.core.domain.features.mdprops

object WikiLinkSyntax {
    private val PATTERN = Regex("""\[\[([^\[\]\n|]+)(?:\|([^\[\]\n]+))?]]""")

    data class Match(
        val range: IntRange,   // inclusive start/end in source text
        val target: String,
        val alias: String?,
    )

    fun findAll(text: String): Sequence<Match> =
        PATTERN.findAll(text).map { m ->
            Match(
                range = m.range,
                target = m.groupValues[1].trim(),
                alias = m.groupValues.getOrNull(2)?.takeIf { it.isNotBlank() },
            )
        }

    fun hasAny(text: String): Boolean = PATTERN.containsMatchIn(text)
}