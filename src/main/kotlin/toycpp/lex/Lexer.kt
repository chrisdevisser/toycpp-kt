package toycpp.lex

import toycpp.control_structure.doWhile
import toycpp.control_structure.repeatWhile
import toycpp.control_structure.repeatWhileNotNull
import toycpp.dfa.Dfa
import toycpp.dfa.DfaNode
import toycpp.iterators.withCurrent
import toycpp.location.SourceChar
import toycpp.location.SourceLocation

typealias CppDfa = Dfa<Pptok>
typealias CppDfaNode = DfaNode<Pptok>

fun lazyLexPpTokens(fileName: String, sourceText: Sequence<SourceChar>, dfa: CppDfa, lexContext: LexContextHolder) =
    Lexer(sourceText, dfa, lexContext).lex(fileName)

private class Lexer(
    sourceText: Sequence<SourceChar>,
    val dfa: CppDfa,
    val lexContext: LexContextHolder
) {
    var remainingInputIter = sourceText.iterator().withCurrent()

    fun lex(fileName: String): Sequence<PpToken> = sequence {
        if (eos()) return@sequence

        val firstLineLoc = SourceLocation(fileName, 1, 0)
        yield(PpToken(Pptok.StartOfLine, "", firstLineLoc, firstLineLoc, false))

        repeatWhileNotNull({ lexOneToken() }) { (rawToken, sourceRead) ->
            when (rawToken.kind) {
//                Pptok.SpecialCaseDigraphLex -> {
//                    // Due to the maximal munch, %:% would be an error because %:%: is possible.
//                    // Instead, the DFA gives back a pseudotoken that we turn into two real tokens.
//                    check(sourceRead.size == 3) { "Expected %:% pseudotoken to have 3 characters, but it has ${sourceRead.size}." }
//                    yieldAll(splitTokenIntoTwo(firstTokenLength = 2, sourceRead, rawToken))
//                }

                Pptok.SpecialCaseTemplateLex -> {
                    // [lex.pptoken]/3.2: <:: is < ::, not <: :.
                    // The DFA gives back a pseudotoken and we fix it here.
                    check(sourceRead.size == 3) { "Expected <:: pseudotoken to have 3 characters, but it has ${sourceRead.size}." }
                    yieldAll(splitTokenIntoTwo(firstTokenLength = 1, sourceRead, rawToken))
                }

                Pptok.RawStringStart -> {
                    // The DFA determined a raw string literal has started.
                    // We have to lex it by hand because it requires a matching delimiter.
                    lexContext.state = LexContext.InRawStringLiteral
                }

                else -> {
                    yield(rawToken)
                }
            }
        }
    }

    fun lexOneToken(): Pair<PpToken, List<SourceChar>>? {
        var currentNode = dfa.start

        val hasLeadingWhitespace = skipWhitespace()
        if (eos()) return null

        val sourceRead = buildList {
            repeatWhile({ readToNextAcceptingNode(currentNode) }, { (nextGoodNode, read) -> nextGoodNode != null && read.any() }) { (nextGoodNode, read) ->
                addAll(read)
                currentNode = nextGoodNode!!
            }
        }

        val lexeme = sourceRead.map { it.c }.joinToString("")
        val startLoc = sourceRead.first().loc
        val endLoc = sourceRead.last().loc.run { copy(col = col + 1) }

        val token = PpToken(currentNode.acceptValue ?: Pptok.InvalidToken, lexeme, startLoc, endLoc, hasLeadingWhitespace )
        return Pair(token, sourceRead)
    }

    fun readToNextAcceptingNode(startNode: CppDfaNode): Pair<CppDfaNode?, List<SourceChar>> {
        var currentNode = startNode
        val sourceRead = buildList {
            readWhile({ (this@buildList.isEmpty() || currentNode.acceptValue == null) && currentNode[it] != null }) {
                add(it)
                currentNode = currentNode[it.c]!!
            }
        }

        return if (currentNode.acceptValue == null) {
            remainingInputIter = (sourceRead + remainingInputIter.asSequence()).iterator().withCurrent()
            Pair(null, sourceRead)
        } else {
            Pair(currentNode, sourceRead)
        }
    }

    fun readWhile(cond: (Char) -> Boolean, block: (SourceChar) -> Unit) {
        repeatWhile( { remainingInputIter.current() ?: return }, { remainingInputIter.hasNext() && cond(it.c) }) {
            block(it)
            remainingInputIter.next()
        }

//        while (true) {
//            if (!remainingInputIter.hasNext()) return
//
//            val next = remainingInputIter.current()!!
//            block(next)
//            remainingInputIter.next()
//
//            if (!cond(next.c)) break
//        }
    }

    fun doReadWhile(cond: (Char) -> Boolean, block: (SourceChar) -> Unit) {
        if (!remainingInputIter.hasNext()) return
        doWhile({ remainingInputIter.current()!! }, { remainingInputIter.hasNext() && cond(it.c) }) {
            block(it)
            remainingInputIter.next()
        }
    }

    fun isSkippableWhitespace(c: Char): Boolean {
        val verticalTab = Char(11)
        val formFeed = Char(12)
        return c in " \t\n${verticalTab}${formFeed}"
    }

    fun skipWhitespace(): Boolean {
        var readAny = false
        readWhile(::isSkippableWhitespace) {
            readAny = true
        }

        return readAny
    }

    fun eos() = !remainingInputIter.hasNext()
}

private fun splitTokenIntoTwo(firstTokenLength: Int, lexemeChars: List<SourceChar>, token: PpToken): List<PpToken> {
    with (token) {
        val endLocOfFirst = lexemeChars[firstTokenLength - 1].loc.run { copy(col = col + 1) }

        val first = PpToken(Pptok.Pound, lexeme.substring(0, firstTokenLength), startLocation, endLocOfFirst, hasLeadingWhitespace)
        val second = PpToken(Pptok.Mod, lexeme.substring(firstTokenLength), lexemeChars[firstTokenLength].loc, endLocation, false)
        return listOf(first, second)
    }
}
