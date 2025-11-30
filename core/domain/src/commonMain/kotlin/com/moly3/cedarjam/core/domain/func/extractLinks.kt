package com.moly3.cedarjam.core.domain.func

fun String.extractLinks(text: String = this): List<String> {
    val linkRegex = Regex("""\[\[([^\]]+?)\]\]""")
    return linkRegex.findAll(text)
        .map { it.groupValues[1].trim() }
        .map { d -> "$d.md" }
        .toList()
}