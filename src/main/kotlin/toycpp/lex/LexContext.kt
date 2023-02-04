package toycpp.lex

import toycpp.lex.LexContext.NothingSpecial

/**
 * Provides necessary lexing context that can be fed back to earlier translation steps.
 * This is necessary for line splicing, which conceptually happens before lexing, but not in a raw string literal.
 */
class LexContextHolder(var state: LexContext = NothingSpecial)

enum class LexContext {
    NothingSpecial,
    InRawStringLiteral
}