package toycpp.lex

import toycpp.location.SourceLocation

data class Token<Kind>(
    val kind: Kind,
    val lexeme: String,
    val startLocation: SourceLocation,
    val endLocation: SourceLocation,
    val hasLeadingWhitespace: Boolean
)

typealias PpToken = Token<Pptok>
typealias CppToken = Token<Tok>