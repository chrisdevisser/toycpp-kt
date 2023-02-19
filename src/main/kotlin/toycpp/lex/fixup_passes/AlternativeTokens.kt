package toycpp.lex.fixup_passes

import toycpp.lex.PpToken
import toycpp.lex.Pptok.*

private val replacements = mapOf(
    "and" to And,
    "and_eq" to AndEquals,
    "bitor" to BitOr,
    "or_eq" to OrEquals,
    "or" to Or,
    "xor_eq" to XorEquals,
    "xor" to Xor,
    "not" to Not,
    "compl" to Compl,
    "not_eq" to NotEqualTo,
    "bitand" to BitAnd
)

fun Sequence<PpToken>.transformAlternativeTokens(): Sequence<PpToken> =
    map { token ->
        val newKind = replacements[token.lexeme]
        if (newKind != null) token.copy(kind = newKind) else token
    }