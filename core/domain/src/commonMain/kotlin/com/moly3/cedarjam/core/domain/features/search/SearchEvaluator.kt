package com.moly3.cedarjam.core.domain.features.search

/**
 * Evaluates a parsed [QueryNode] tree against [Searchable] items.
 *
 * The evaluator carries a small [Scope] describing *where* in the item
 * the current sub-query should look (whole content, a single line, a
 * block, a property value, …) and whether matching is case sensitive.
 * Scoped operators like `line:` simply change the scope before recursing.
 */
class SearchEvaluator(private val defaultCaseSensitive: Boolean = false) {

    /** Filter a collection down to the items matching [query]. */
    fun search(query: QueryNode, items: Iterable<Searchable>): List<Searchable> =
        items.filter { matches(query, it) }

    /** Does a single [item] satisfy [query]? */
    fun matches(query: QueryNode, item: Searchable): Boolean =
        eval(query, item, Scope.whole(defaultCaseSensitive))

    // ---- scope -------------------------------------------------------------

    /**
     * Describes the text region(s) the current evaluation searches.
     * [texts] is the candidate region(s); a content term matches if it is
     * found in *any* of them.
     */
    private data class Scope(
        val texts: List<String>,
        val caseSensitive: Boolean,
    ) {
        companion object {
            fun whole(cs: Boolean) = Scope(emptyList(), cs) // empty = use item.content
        }
    }

    // ---- core recursion ----------------------------------------------------

    private fun eval(node: QueryNode, item: Searchable, scope: Scope): Boolean = when (node) {
        is And -> node.children.all { eval(it, item, scope) }
        is Or -> node.children.any { eval(it, item, scope) }
        is Not -> !eval(node.child, item, scope)
        is Group -> eval(node.child, item, scope)
        is Term -> evalTerm(node, item, scope)
        is Regex -> evalRegex(node, item, scope)
        is Operator -> evalOperator(node, item, scope)
        is Property -> evalProperty(node, item)
        is Comparison -> evalComparison(node, item)
        is TypeFilter -> item.type in node.accepted
    }

    // ---- terms & regex -----------------------------------------------------

    private fun candidateTexts(item: Searchable, scope: Scope): List<String> =
        if (scope.texts.isEmpty()) listOf(item.content) else scope.texts

    private fun evalTerm(term: Term, item: Searchable, scope: Scope): Boolean {
        val needle = term.text
        return candidateTexts(item, scope).any { haystack ->
            contains(haystack, needle, scope.caseSensitive)
        }
    }

    private fun contains(haystack: String, needle: String, caseSensitive: Boolean): Boolean =
        if (caseSensitive) haystack.contains(needle)
        else haystack.contains(needle, ignoreCase = true)

    private fun evalRegex(regex: Regex, item: Searchable, scope: Scope): Boolean {
        val options = if (scope.caseSensitive) emptySet() else setOf(RegexOption.IGNORE_CASE)
        val compiled = Regex(regex.pattern, options)
        return candidateTexts(item, scope).any { compiled.containsMatchIn(it) }
    }

    // ---- operators ---------------------------------------------------------

    private fun evalOperator(op: Operator, item: Searchable, scope: Scope): Boolean = when (op.name) {
        OperatorName.FILE -> eval(op.arg, item, scope.copy(texts = listOf(item.fileName)))
        OperatorName.PATH -> eval(op.arg, item, scope.copy(texts = listOf(item.path)))
        OperatorName.CONTENT -> eval(op.arg, item, scope.copy(texts = listOf(item.content)))

        OperatorName.TAG -> evalTag(op.arg, item)

        // Region operators: the sub-query must match within ONE region.
        OperatorName.LINE -> item.lines.any { ln ->
            eval(op.arg, item, scope.copy(texts = listOf(ln)))
        }
        OperatorName.BLOCK -> item.blocks.any { blk ->
            eval(op.arg, item, scope.copy(texts = listOf(blk)))
        }
        OperatorName.SECTION -> item.sections.any { sec ->
            eval(op.arg, item, scope.copy(texts = listOf(sec)))
        }

        OperatorName.TASK -> item.tasks.any { t ->
            eval(op.arg, item, scope.copy(texts = listOf(t.text)))
        }
        OperatorName.TASK_TODO -> item.tasks.any { t ->
            !t.done && eval(op.arg, item, scope.copy(texts = listOf(t.text)))
        }
        OperatorName.TASK_DONE -> item.tasks.any { t ->
            t.done && eval(op.arg, item, scope.copy(texts = listOf(t.text)))
        }

        OperatorName.MATCH_CASE -> eval(op.arg, item, scope.copy(caseSensitive = true))
        OperatorName.IGNORE_CASE -> eval(op.arg, item, scope.copy(caseSensitive = false))
    }

    /**
     * `tag:` matches against the item's parsed tag set rather than text.
     * Obsidian treats `tag:#work` as not matching `#myjob/work`, but a
     * parent tag DOES match its nested children (`#work` matches
     * `#work/urgent`). The leading `#` is optional in the query.
     */
    private fun evalTag(arg: QueryNode, item: Searchable): Boolean = when (arg) {
        is And -> arg.children.all { evalTag(it, item) }
        is Or -> arg.children.any { evalTag(it, item) }
        is Not -> !evalTag(arg.child, item)
        is Group -> evalTag(arg.child, item)
        is Term -> {
            val wanted = arg.text.removePrefix("#")
            item.tags.any { tag ->
                tag.equals(wanted, ignoreCase = true) ||
                        tag.startsWith("$wanted/", ignoreCase = true)
            }
        }
        else -> false
    }

    // ---- properties --------------------------------------------------------

    private fun evalProperty(prop: Property, item: Searchable): Boolean {
        val exists = item.properties.containsKey(prop.name)
        if (!exists) return false
        val raw = item.properties[prop.name]

        // [name:null] — exists but empty
        if (prop.checkNull) {
            return raw == null || (raw is List<*> && raw.isEmpty())
        }
        // [name] — existence only
        if (prop.value == null) return true

        // [name:value] — match the sub-query against the property value(s)
        val valueTexts = propertyValueTexts(raw)
        if (valueTexts.isEmpty()) return false
        return eval(prop.value, item, Scope(valueTexts, defaultCaseSensitive))
    }

    private fun propertyValueTexts(raw: Any?): List<String> = when (raw) {
        null -> emptyList()
        is List<*> -> raw.mapNotNull { it?.toString() }
        else -> listOf(raw.toString())
    }

    private fun evalComparison(cmp: Comparison, item: Searchable): Boolean {
        val raw = item.properties[cmp.key] ?: return false
        val value = when (raw) {
            is Number -> raw.toDouble()
            is String -> raw.toDoubleOrNull() ?: return false
            else -> return false
        }
        return when (cmp.op) {
            CompareOp.LT -> value < cmp.number
            CompareOp.GT -> value > cmp.number
            CompareOp.LE -> value <= cmp.number
            CompareOp.GE -> value >= cmp.number
            CompareOp.EQ -> value == cmp.number
        }
    }
}