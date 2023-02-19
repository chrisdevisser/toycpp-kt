package toycpp.lex.check_passes

import toycpp.diagnostics.EmptyCharacterLiteral
import toycpp.diagnostics.diag
import toycpp.lex.PpToken
import toycpp.lex.Pptok

// TODO: This is a factor in error recovery and early termination
fun diagnoseEmptyCharacterLiterals(tokens: Iterable<PpToken>): Boolean =
    runCheck(tokens, ::isEmptyCharLit) {
        diag(EmptyCharacterLiteral(it.lexeme), it.startLocation)
    }

private fun isEmptyCharLit(token: PpToken): Boolean {
    if (token.kind !in listOf(Pptok.CharLit, Pptok.CharUdl)) return false

    val afterFirstQuote = token.lexeme.substringAfter("\'")
    assert(afterFirstQuote.isNotEmpty()) { "Encountered a token masquerading as a character literal (lexeme=${token.lexeme})" }

    return afterFirstQuote.first() == '\''
}