package toycpp.preprocessor

import toycpp.lex.PpToken
import toycpp.lex.Pptok.*
import toycpp.parser.combinators.get
import toycpp.parser.combinators.oneOrMore
import toycpp.parser.combinators.or
import toycpp.parser.combinators.then
import toycpp.parser.mapValue

fun namedIdentifier(name: String) = one(Identifier)[{it.lexeme == name}] named "'$name'"

// [lex.header]
private val angledHeaderName: PpParser<String> =
    (-LessThan then oneOrMore(anyTokenExcept(GreaterThan)) then -GreaterThan
        mapValue ::stringifyTokens
        named "<header>")


private val quotedHeaderName: PpParser<String> =
    (one(StringLit)
        mapValue {
            // Encoding prefixes not allowed

            it.lexeme.trim('"')
        }
        named "\"header\"")

val headerName = angledHeaderName or quotedHeaderName named "header-name"

fun stringifyTokens(tokens: List<PpToken>): String =
    tokens.joinToString("") { stringifyToken(it) }

fun stringifyToken(it: PpToken) =
    (if (it.hasLeadingWhitespace) " " else "") + it.lexeme

//fun relexAngledHeaderName(tokens: List<PpToken>): Pair<PpToken?, List<PpToken>> {
//    val remainingTokens = buildList {
//        var workingBuffer = ""
//
//        for (token in tokens) {
//            workingBuffer += stringifyToken(token)
//
//
//        }
//    }
//}