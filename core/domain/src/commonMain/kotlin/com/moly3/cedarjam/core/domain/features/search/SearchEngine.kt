package com.moly3.cedarjam.core.domain.features.search

/**
 * Renders a parsed [QueryNode] tree back into a human-readable explanation,
 * mirroring Obsidian's "Explain search term" feature.
 */
object QueryExplainer {

    fun explain(node: QueryNode): String = describe(node)

    private fun describe(node: QueryNode): String = when (node) {
        is And ->
            if (node.children.isEmpty()) "match everything"
            else node.children.joinToString(" AND ") { describe(it) }
        is Or -> node.children.joinToString(" OR ") { describe(it) }
        is Not -> "NOT ${describe(node.child)}"
        is Group -> "(${describe(node.child)})"
        is Term ->
            if (node.phrase) "the phrase \"${node.text}\""
            else "the word '${node.text}'"
        is Regex -> "text matching regex /${node.pattern}/"
        is Operator -> "${node.name.keyword} contains [${describe(node.arg)}]"
        is Property ->
            when {
                node.checkNull -> "property '${node.name}' is empty"
                node.value == null -> "property '${node.name}' exists"
                else -> "property '${node.name}' is [${describe(node.value)}]"
            }
        is Comparison -> "property '${node.key}' ${node.op.symbol} ${formatNumber(node.number)}"
        is TypeFilter ->
            "item type is one of {" +
                    ItemType.entries.filter { it in node.accepted }
                        .joinToString(", ") { it.keyword } +
                    "}"
    }

    private fun formatNumber(d: Double): String =
        if (d % 1.0 == 0.0) d.toLong().toString() else d.toString()
}

/**
 * The public entry point. Parse a query once, then reuse it across many
 * searches.
 *
 * ```
 * val engine = SearchEngine()
 * val q = engine.compile("type:(file OR directory) project -archived")
 * val hits = engine.search(q, myItems)
 * println(engine.explain(q))
 * ```
 */
class SearchEngine(caseSensitive: Boolean = false) {

    private val evaluator = SearchEvaluator(caseSensitive)

    /** Parse a query string into a reusable [QueryNode] tree.
     *  Throws [SearchSyntaxException] on malformed input. */
    fun compile(query: String): QueryNode = parseQuery(query)

    /** Parse and run in one step. */
    fun search(query: String, items: Iterable<Searchable>): List<Searchable> =
        search(compile(query), items)

    /** Run a pre-compiled query. */
    fun search(query: QueryNode, items: Iterable<Searchable>): List<Searchable> =
        evaluator.search(query, items)

    /** Test one item against a pre-compiled query. */
    fun matches(query: QueryNode, item: Searchable): Boolean =
        evaluator.matches(query, item)

    /** Human-readable explanation of a compiled query. */
    fun explain(query: QueryNode): String = QueryExplainer.explain(query)

    /** Parse a query and return its explanation; useful for UI tooltips. */
    fun explain(query: String): String = QueryExplainer.explain(compile(query))
}