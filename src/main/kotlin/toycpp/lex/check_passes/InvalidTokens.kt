package toycpp.lex.check_passes

import toycpp.diagnostics.InvalidToken
import toycpp.diagnostics.diag
import toycpp.lex.PpToken
import toycpp.lex.Pptok

/**
 * Issues diagnostics for invalid tokens. Returns whether any invalid tokens were found.
 */
fun diagnoseInvalidTokens(tokens: Iterable<PpToken>): Boolean =
    runCheck(tokens, { it.kind == Pptok.InvalidToken }) {
        diag(InvalidToken(it.lexeme), it.startLocation)
    }