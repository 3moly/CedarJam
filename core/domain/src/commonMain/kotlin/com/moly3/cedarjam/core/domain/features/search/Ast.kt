package com.moly3.cedarjam.core.domain.features.search

/**
 * The Abstract Syntax Tree for an Obsidian-style search query.
 *
 * A query parses into a tree of [QueryNode]s. Evaluation walks the tree
 * against a [Searchable] item and returns a boolean match.
 *
 * Grammar (informally):
 *
 *   query      := orExpr
 *   orExpr     := andExpr ("OR" andExpr)*
 *   andExpr    := unary (unary)*            // juxtaposition = AND
 *   unary      := "-" unary | primary
 *   primary    := group | operator | typeOp | property | regex | phrase | word
 *   group      := "(" query ")"
 *   operator   := NAME ":" (group | atom)   // file: path: tag: line: ...
 *   typeOp     := "type:" "(" typeEnum ("OR" typeEnum)* ")" | "type:" typeEnum
 *   property   := "[" NAME (":" query)? "]"
 */
sealed interface QueryNode

/** Logical AND of all children. Empty AND matches everything. */
data class And(val children: List<QueryNode>) : QueryNode

/** Logical OR of all children. Empty OR matches nothing. */
data class Or(val children: List<QueryNode>) : QueryNode

/** Logical negation. */
data class Not(val child: QueryNode) : QueryNode

/** Explicit parenthesised grouping. Kept distinct from [And] so the
 *  "explain" output can reproduce the user's parentheses faithfully. */
data class Group(val child: QueryNode) : QueryNode

/** A bare word or quoted phrase searched against content. */
data class Term(val text: String, val phrase: Boolean = false) : QueryNode

/** A `/regex/` literal. */
data class Regex(val pattern: String) : QueryNode

/**
 * A scoped operator such as `file:`, `path:`, `tag:`, `line:`, etc.
 * [name] is the operator keyword, [arg] is the nested sub-query.
 */
data class Operator(val name: OperatorName, val arg: QueryNode) : QueryNode

/**
 * A property query: `[name]`, `[name:value]`, `[name:null]`.
 * [value] is null for an existence check.
 */
data class Property(val name: String, val value: QueryNode?, val checkNull: Boolean = false) : QueryNode

/** A numeric comparison inside brackets: `[duration:<5]`. */
data class Comparison(val key: String, val op: CompareOp, val number: Double) : QueryNode

/**
 * The custom `type:` operator. Matches if the item's [Searchable.type]
 * is in [accepted]. Validated at parse time, so the enum can never hold
 * a bad value.
 */
data class TypeFilter(val accepted: Set<ItemType>) : QueryNode

/** The set of item types `type:` can filter on. */
enum class ItemType(val keyword: String) {
    TAG("tag"),
    COLLECTION("collection"),
    ROW("row"),
    FILE("file"),
    DIRECTORY("directory"),
    ANNOTATION("annotation");

    companion object {
        private val byKeyword = entries.associateBy { it.keyword }
        fun fromKeyword(kw: String): ItemType? = byKeyword[kw.lowercase()]
        val allKeywords: List<String> get() = entries.map { it.keyword }
    }
}

/** Recognised scoped operators. */
enum class OperatorName(val keyword: String) {
    FILE("file"),
    PATH("path"),
    CONTENT("content"),
    TAG("tag"),
    LINE("line"),
    BLOCK("block"),
    SECTION("section"),
    TASK("task"),
    TASK_TODO("task-todo"),
    TASK_DONE("task-done"),
    MATCH_CASE("match-case"),
    IGNORE_CASE("ignore-case");

    companion object {
        private val byKeyword = entries.associateBy { it.keyword }
        fun fromKeyword(kw: String): OperatorName? = byKeyword[kw.lowercase()]
    }
}

enum class CompareOp(val symbol: String) {
    LT("<"), GT(">"), LE("<="), GE(">="), EQ("=");
}