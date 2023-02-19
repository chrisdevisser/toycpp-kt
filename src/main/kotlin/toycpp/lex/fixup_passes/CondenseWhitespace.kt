package toycpp.lex.fixup_passes

import toycpp.lex.PpToken
import toycpp.lex.Pptok.*

/**
 * Replaces explicit whitespace tokens with a token attribute that tracks leading whitespace.
 * Newlines and comments are counted as whitespace for this purpose, but are retained as tokens.
 */
fun Sequence<PpToken>.condenseWhitespace(): Sequence<PpToken> = sequence {
    var justHadWhitespace = false

    for (token in this@condenseWhitespace) {
        // Discard whitespace tokens, keep the others
        if (token.kind != Whitespace) {
            yield(if (justHadWhitespace) token.copy(hasLeadingWhitespace = true) else token)
        }

        justHadWhitespace = token.kind in listOf(Whitespace, Newline, Comment)
    }
}