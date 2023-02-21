package toycpp.preprocessor

import toycpp.lex.*
import toycpp.location.SourceChar
import toycpp.location.SourceLocation
import toycpp.parser.*
import toycpp.parser.ParseResult.Failure
import toycpp.parser.ParseResult.Success
import toycpp.parser.combinators.unaryMinus

typealias PpParser<T> = Parser<T, PpToken>

private val anyImpl = buildAny<PpToken, Pptok>("") { it.kind }
private val anyExceptImpl = buildAnyExcept<PpToken, Pptok>("") { it.kind }
private val seqImpl = buildSeq<PpToken, Pptok>("") { it.kind }

val anyToken = anyImpl(emptyList())
fun anyTokenExcept(vararg exceptions: Pptok) = anyExceptImpl(exceptions.asIterable())

fun one(kind: Pptok) = anyImpl(listOf(kind)) named kind.toString()
operator fun Pptok.unaryPlus() = one(this)
operator fun Pptok.unaryMinus() = -one(this)

val endOfInput = endOfInput<PpToken>()

//fun relexOne(dfa: CppDfa): PpParser<PpToken> =
//    AdhocParser<PpToken, PpToken>("relexed token") { input ->
//        val sourceInput = input.flatMap { token -> stringifyToken(token).map { SourceChar(it, token.endLocation) } }
//        val sourceInputIter = sourceInput.iterator()
//        val (relexedTokens, remainingInputIter) = lexOneAdhoc(sourceInput, dfa, LexContextHolder())
//        if (relexedTokens.none { it.kind == Pptok.InvalidToken }) {
//            Success(relexedTokens, sourceInputIter.asSequence())
//        } else {
//            Failure()
//        }
//    }