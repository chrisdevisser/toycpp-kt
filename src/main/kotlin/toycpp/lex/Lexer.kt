package toycpp.lex

import toycpp.lex.ppinterface.PpContextHolder
import toycpp.location.LocationTracker
import toycpp.location.SourceChar

class Lexer(
    private val sourceText: Sequence<SourceChar>,
    private val locationTracker: LocationTracker,
    private val context: PpContextHolder
) {
    fun lazyLexPptokens(): Sequence<PpToken> = sequence {

    }
}