package toycpp.lex

import toycpp.location.SourceLocation

data class PpToken(
    val kind: Pptok,
    val lexeme: String,
    val startLocation: SourceLocation,
    val endLocation: SourceLocation,
    val hasLeadingWhitespace: Boolean = false
)