package com.moly3.cedarjam.core.domain.features.search

/**
 * Recursive-descent parser turning a [Token] stream into a [QueryNode] tree.
 *
 * Precedence, lowest to highest:
 *   OR  <  AND (juxtaposition)  <  NOT (`-`)  <  primary
 *
 * This mirrors Obsidian: `a OR b c` parses as `a OR (b AND c)`.
 */
class Parser(private val tokens: List<Token>) {

    private var pos = 0

    fun parse(): QueryNode {
        if (tokens.isEmpty()) return And(emptyList()) // empty query matches all
        val node = parseOr()
        if (!atEnd()) throw SearchSyntaxException("Unexpected trailing input", pos)
        return node
    }

    // ---- precedence levels -------------------------------------------------

    private fun parseOr(): QueryNode {
        val parts = mutableListOf(parseAnd())
        while (peek() is Token.Or) {
            advance()
            parts += parseAnd()
        }
        return if (parts.size == 1) parts[0] else Or(parts)
    }

    private fun parseAnd(): QueryNode {
        val parts = mutableListOf<QueryNode>()
        while (!atEnd() && !isAndTerminator()) {
            parts += parseUnary()
        }
        if (parts.isEmpty()) throw SearchSyntaxException("Expected a search term", pos)
        return if (parts.size == 1) parts[0] else And(parts)
    }

    private fun isAndTerminator(): Boolean = when (peek()) {
        is Token.Or, is Token.RParen, is Token.RBracket -> true
        else -> false
    }

    private fun parseUnary(): QueryNode {
        if (peek() is Token.Minus) {
            advance()
            return Not(parseUnary())
        }
        return parsePrimary()
    }

    // ---- primary expressions ----------------------------------------------

    private fun parsePrimary(): QueryNode {
        return when (val t = peek()) {
            is Token.LParen -> parseGroup()
            is Token.LBracket -> parseProperty()
            is Token.RegexLit -> { advance(); Regex(t.pattern) }
            is Token.Phrase -> { advance(); Term(t.text, phrase = true) }
            is Token.Word -> parseWordOrOperator()
            else -> throw SearchSyntaxException("Unexpected token: $t", pos)
        }
    }

    private fun parseGroup(): QueryNode {
        expect<Token.LParen>("(")
        val inner = parseOr()
        expect<Token.RParen>(")")
        return Group(inner)
    }

    /**
     * A word may be a plain term, or an operator if it is immediately
     * followed by a colon. `type:` is special-cased before the generic
     * operator path because its argument is a constrained enum.
     */
    private fun parseWordOrOperator(): QueryNode {
        val word = (peek() as Token.Word).text
        // not an operator unless a colon directly follows
        if (peekAt(1) !is Token.Colon) {
            advance()
            return Term(word)
        }

        // `type:` — the custom operator requested
        if (word.equals("type", ignoreCase = true)) {
            advance(); advance() // consume word + colon
            return parseTypeFilter()
        }

        val opName = OperatorName.fromKeyword(word)
            ?: run {
                // unknown `something:` — treat the whole thing as a literal term
                advance()
                return Term(word)
            }
        advance(); advance() // consume word + colon
        val arg = parseOperatorArg()
        return Operator(opName, arg)
    }

    /** Operator argument: either a parenthesised sub-query or a single atom. */
    private fun parseOperatorArg(): QueryNode {
        return when (peek()) {
            is Token.LParen -> parseGroup()
            is Token.Phrase -> { val p = peek() as Token.Phrase; advance(); Term(p.text, phrase = true) }
            is Token.RegexLit -> { val r = peek() as Token.RegexLit; advance(); Regex(r.pattern) }
            is Token.Word -> { val w = peek() as Token.Word; advance(); Term(w.text) }
            is Token.Minus -> parseUnary()
            else -> throw SearchSyntaxException("Operator requires an argument", pos)
        }
    }

    /**
     * Parses the argument of `type:`.
     *
     * Accepts both `type:file` and `type:(file OR directory)`.
     * Every keyword is validated against [ItemType]; an unknown value is
     * a hard parse error, listing the legal options.
     */
    private fun parseTypeFilter(): TypeFilter {
        val accepted = mutableSetOf<ItemType>()

        fun consumeOne() {
            val w = peek()
            if (w !is Token.Word) {
                throw SearchSyntaxException("type: expects one of ${ItemType.allKeywords}", pos)
            }
            val type = ItemType.fromKeyword(w.text)
                ?: throw SearchSyntaxException(
                    "Unknown type '${w.text}'. Valid types: ${ItemType.allKeywords.joinToString(", ")}",
                    pos
                )
            accepted += type
            advance()
        }

        if (peek() is Token.LParen) {
            advance()
            consumeOne()
            while (peek() is Token.Or) {
                advance()
                consumeOne()
            }
            expect<Token.RParen>(")")
        } else {
            consumeOne()
        }
        return TypeFilter(accepted)
    }

    /**
     * Parses `[name]`, `[name:value]`, `[name:null]`, and `[name:<5]`.
     */
    private fun parseProperty(): QueryNode {
        expect<Token.LBracket>("[")
        val nameTok = peek()
        if (nameTok !is Token.Word) throw SearchSyntaxException("Property name expected", pos)
        val name = nameTok.text
        advance()

        if (peek() is Token.RBracket) {
            advance()
            return Property(name, value = null) // existence check
        }
        expect<Token.Colon>(":")

        // numeric comparison: [duration:<5]
        if (peek() is Token.Compare) {
            val op = (peek() as Token.Compare).op
            advance()
            val numTok = peek()
            val raw = when (numTok) {
                is Token.Word -> numTok.text
                is Token.Phrase -> numTok.text
                else -> throw SearchSyntaxException("Comparison needs a number", pos)
            }
            val num = raw.toDoubleOrNull()
                ?: throw SearchSyntaxException("'$raw' is not a number", pos)
            advance()
            expect<Token.RBracket>("]")
            return Comparison(name, op, num)
        }

        // null check: [aliases:null]
        if (peek().let { it is Token.Word && it.text.equals("null", ignoreCase = true) }) {
            advance()
            expect<Token.RBracket>("]")
            return Property(name, value = null, checkNull = true)
        }

        // value sub-query: [status:Draft OR Published]
        val value = parseOr()
        expect<Token.RBracket>("]")
        return Property(name, value)
    }

    // ---- token helpers -----------------------------------------------------

    private fun peek(): Token? = tokens.getOrNull(pos)
    private fun peekAt(offset: Int): Token? = tokens.getOrNull(pos + offset)
    private fun advance() { pos++ }
    private fun atEnd(): Boolean = pos >= tokens.size

    private inline fun <reified T : Token> expect(label: String) {
        if (peek() !is T) throw SearchSyntaxException("Expected '$label'", pos)
        advance()
    }
}

/** Convenience entry point: string → AST. */
fun parseQuery(query: String): QueryNode =
    Parser(Lexer(query).tokenize()).parse()