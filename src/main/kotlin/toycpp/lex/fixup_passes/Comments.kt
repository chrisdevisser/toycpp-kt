package toycpp.lex.fixup_passes

import toycpp.lex.PpToken
import toycpp.lex.Pptok

/**
 * Removes comments from lexed tokens.
 */
fun Sequence<PpToken>.removeComments(): Sequence<PpToken> =
    // This would be the place to store comment info in a table and return it
    filterNot { it.kind == Pptok.Comment }