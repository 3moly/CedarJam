package com.moly3.cedarjam.core.domain.features.search

/** A lexical token produced by the [Lexer]. */
sealed interface Token {
    object LParen : Token
    object RParen : Token
    object LBracket : Token
    object RBracket : Token
    object Or : Token
    object Minus : Token
    object Colon : Token
    /** A bare identifier / word, e.g. `meeting`, `file`, `type`. */
    data class Word(val text: String) : Token
    /** A double-quoted phrase with quotes already stripped. */
    data class Phrase(val text: String) : Token
    /** A `/.../` regular expression with slashes stripped. */
    data class RegexLit(val pattern: String) : Token
    /** A comparison symbol: `<`, `>`, `<=`, `>=`, `=`. */
    data class Compare(val op: CompareOp) : Token
}

class SearchSyntaxException(message: String, val position: Int) :
    Exception("$message (at position $position)")

/**
 * Converts a raw query string into a list of [Token]s.
 *
 * The lexer is intentionally permissive: it does not know about grammar,
 * only about character shapes. `OR` is recognised here (uppercase only,
 * matching Obsidian) so the parser can treat it structurally.
 */
class Lexer(private val input: String) {

    private var pos = 0
    private val tokens = mutableListOf<Token>()

    fun tokenize(): List<Token> {
        while (pos < input.length) {
            val c = input[pos]
            when {
                c.isWhitespace() -> pos++
                c == '(' -> { tokens += Token.LParen; pos++ }
                c == ')' -> { tokens += Token.RParen; pos++ }
                c == '[' -> { tokens += Token.LBracket; pos++ }
                c == ']' -> { tokens += Token.RBracket; pos++ }
                c == ':' -> { tokens += Token.Colon; pos++ }
                c == '-' && isNegation() -> { tokens += Token.Minus; pos++ }
                c == '"' -> readPhrase()
                c == '/' -> readRegex()
                c == '<' || c == '>' || c == '=' -> readCompare()
                else -> readWord()
            }
        }
        return tokens
    }

    /**
     * `-` is the negation operator only when it *begins a new term*:
     * preceded by start-of-input, whitespace, or a structural token, and
     * followed by a non-whitespace character. Otherwise it is a literal
     * hyphen, e.g. inside a Zettelkasten id like `2022-07`.
     */
    private fun isNegation(): Boolean {
        val atStart = pos == 0 || input[pos - 1].isWhitespace()
        val prev = tokens.lastOrNull()
        val afterStruct = prev is Token.LParen || prev is Token.Or ||
                prev is Token.Minus || prev is Token.Colon || prev is Token.LBracket
        val followed = pos + 1 < input.length && !input[pos + 1].isWhitespace()
        return (atStart || afterStruct) && followed
    }

    private fun readPhrase() {
        val start = pos
        pos++ // opening quote
        val sb = StringBuilder()
        while (pos < input.length && input[pos] != '"') {
            if (input[pos] == '\\' && pos + 1 < input.length) {
                pos++
                sb.append(input[pos])
            } else {
                sb.append(input[pos])
            }
            pos++
        }
        if (pos >= input.length) throw SearchSyntaxException("Unterminated quoted phrase", start)
        pos++ // closing quote
        tokens += Token.Phrase(sb.toString())
    }

    private fun readRegex() {
        val start = pos
        pos++ // opening slash
        val sb = StringBuilder()
        while (pos < input.length && input[pos] != '/') {
            if (input[pos] == '\\' && pos + 1 < input.length) {
                sb.append(input[pos]); pos++
                sb.append(input[pos])
            } else {
                sb.append(input[pos])
            }
            pos++
        }
        if (pos >= input.length) throw SearchSyntaxException("Unterminated regular expression", start)
        pos++ // closing slash
        tokens += Token.RegexLit(sb.toString())
    }

    private fun readCompare() {
        val c = input[pos]
        if (pos + 1 < input.length && input[pos + 1] == '=' && (c == '<' || c == '>')) {
            tokens += Token.Compare(if (c == '<') CompareOp.LE else CompareOp.GE)
            pos += 2
        } else {
            tokens += Token.Compare(
                when (c) {
                    '<' -> CompareOp.LT
                    '>' -> CompareOp.GT
                    else -> CompareOp.EQ
                }
            )
            pos++
        }
    }

    /** A word runs until whitespace or a structural character.
     *  `:` ends a word so operator keywords split cleanly. */
    private fun readWord() {
        val sb = StringBuilder()
        while (pos < input.length) {
            val c = input[pos]
            if (c.isWhitespace() || c in "()[]:\"/" ) break
            if (c == '<' || c == '>' || c == '=') break
            sb.append(c)
            pos++
        }
        val text = sb.toString()
        if (text == "OR") tokens += Token.Or else tokens += Token.Word(text)
    }
}