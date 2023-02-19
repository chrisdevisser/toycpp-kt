package toycpp.lex

import toycpp.control_structure.generateAndUseWhile
import toycpp.control_structure.generateWhileAndUseWhile
import toycpp.dfa.Dfa
import toycpp.dfa.DfaNode
import toycpp.dfa.dfa
import toycpp.diagnostics.RawStringDelimiterTooLong
import toycpp.diagnostics.diag
import toycpp.encoding.escapeAsciiStringForHumans
import toycpp.iterators.CurrentIterator
import toycpp.iterators.withCurrent
import toycpp.lex.check_passes.diagnoseEmptyCharacterLiterals
import toycpp.lex.fixup_passes.condenseWhitespace
import toycpp.lex.fixup_passes.transformAlternativeTokens
import toycpp.location.*
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

typealias CppDfa = Dfa<Pptok>
typealias CppDfaNode = DfaNode<Pptok>

fun lazyLexPpTokens(sourceText: Sequence<SourceChar>, dfa: CppDfa, lexContext: LexContextHolder): Sequence<PpToken> =
    lexCommon(Lexer(sourceText, dfa, lexContext))

// TODO: This has problems such as one pseudotoken being two tokens
fun lexOneAdhoc(sourceText: Sequence<SourceChar>, dfa: CppDfa, lexContext: LexContextHolder): Pair<Sequence<PpToken>, CurrentIterator<SourceChar>> {
    val lexer = Lexer(sourceText, dfa, lexContext, maximum = 1)
    return Pair(lexCommon(lexer), lexer.remainingInputIter)
}

private fun lexCommon(lexer: Lexer): Sequence<PpToken> =
    lexer.lex()
        .condenseWhitespace()
        .transformAlternativeTokens()

private class Lexer(
    sourceText: Sequence<SourceChar>,
    val dfa: CppDfa,
    val lexContext: LexContextHolder,
    val maximum: Int? = null
) {
    var remainingInputIter = sourceText.iterator().withCurrent()

    /**
     * Converts the input to a sequence of pptokens. This transformation is specified by [dfa].
     * Beyond the DFA, there are adjustments:
     * - Unless the lexer is adhoc, a StartOfLine token is produced at the start of input.
     * - SpecialCaseTemplateLex (<::) pseudotokens produce < :: if the next character is not : or >, otherwise <: with the last : starting the next token.
     * - RawStringStart pseudotokens are taken and finished in the lexer to produce either a StringLit or InvalidToken token.
     */
    fun lex(): Sequence<PpToken> = sequence {
        if (!hasMoreInput()) return@sequence // Being inside the sequence builder means that input is not touched until the returned sequence is

        var tokenCount = 0

        while (hasMoreInput() && (maximum == null || tokenCount < maximum)) {
            val (rawToken, sourceRead) = lexOneToken()
            ++tokenCount

            when (rawToken.kind) {
                Pptok.SpecialCaseTemplateLex -> {
                    // [lex.pptoken]/3.2: <:: is < ::, not <: :, if the next character is not : or >.
                    // The DFA gives back a pseudotoken and we fix it here.
                    // It can't be a separate pass because location info for the middle of the token is lost after here.
                    yieldAll(replaceSpecialCaseTemplateToken(sourceRead, rawToken, remainingInputIter.current.c))
                }

                Pptok.RawStringStart -> {
                    // The DFA determined a raw string literal has started.
                    // We have to lex it by hand because it requires a matching delimiter.
                    yield(lexRawString(rawToken))
                }

                else -> {
                    yield(rawToken)
                }
            }
        }
    }

    /**
     * Reads the input until one pptoken can be created. This token uses the maximum amount of input possible.
     * Requires that there is more input to be lexed.
     *
     * At least one input is guaranteed to be consumed.
     *
     * Returns both the created pptoken, which can be an InvalidToken if the input does not form a valid token, and the input that was consumed during that process.
     * It is unspecified whether the location of a StartOfLine pseudotoken is at the end of the preceding line or at the start of the following line.
     */
    fun lexOneToken(): Pair<PpToken, List<SourceChar>> {
        check(hasMoreInput()) { "Attempted to lex a token when already at the end of input." }

        var currentNode = dfa.start

        val sourceRead = buildList {
            generateAndUseWhile(generate = { readToNextAcceptingNode(currentNode) }, shouldUseValue = { (nextGoodNode, _) -> nextGoodNode != null }) { (nextGoodNode, read) ->
                addAll(read)
                currentNode = nextGoodNode!!
            }

            // Would be a shame if the lexer were to get into an infinite loop...
            // This can happen if the DFA doesn't have a transition for the first character of a token.
            // We'll just take that character and turn it into an invalid token to be handled like the rest.
            // Note: Simply adding the character to the list could still loop infinitely if the start node is accepting.
            if (isEmpty() && hasMoreInput()) {
                currentNode = DfaNode(DfaNode.Id("no input consumed"), acceptValue = null)
                add(remainingInputIter.consume())
            }
        }

        // Leading whitespace is fixed in another pass
        val token = PpToken(currentNode.acceptValue ?: Pptok.InvalidToken, sourceRead.toText(), sourceRead.startLoc, sourceRead.endLoc, hasLeadingWhitespace = false)
        return Pair(token, sourceRead)
    }

    /**
     * Starting at the given DFA node, reads input and advances through the DFA until a node is reached that can produce a token.
     *
     * Returns both the final node reached (null if no applicable node was reached) and the input consumed in this process.
     */
    fun readToNextAcceptingNode(startNode: CppDfaNode): Pair<CppDfaNode?, List<SourceChar>> {
        var currentNode = startNode
        val sourceRead = buildList {
            readWhileAndUseWhile(shouldRead = { (this@buildList.isEmpty() || currentNode.acceptValue == null) }, shouldUseValue = { currentNode[it] != null }) {
                add(it)
                currentNode = currentNode[it.c]!!

                // Update the lex context here so that it's set as soon as the " is read, not when the next character is read
                if (currentNode.acceptValue == Pptok.RawStringStart) {
                    lexContext.state = LexContext.InRawStringLiteral
                }
            }
        }

        return if (currentNode.acceptValue != null && sourceRead.isNotEmpty()) {
            Pair(currentNode, sourceRead)
        } else {
            remainingInputIter.prepend(sourceRead)
            Pair(null, sourceRead)
        }
    }

    /**
     * Reads one character per iteration, starting with the current character.
     * If [shouldUseValue] passes for the character, calls [block] with the character and consumes the character.
     *
     * Finishes iteration as soon as [shouldUseValue] returns false.
     */
    inline fun readAndUseWhile(shouldUseValue: (Char) -> Boolean, block: (SourceChar) -> Unit) {
        contract {
            callsInPlace(shouldUseValue)
            callsInPlace(block)
        }

        readWhileAndUseWhile( { true }, shouldUseValue, block)
    }

    /**
     * Before each iteration, pre-emptively finishes iteration if [shouldRead] returns false.
     * Reads one character per iteration, starting with the current character.
     * If [shouldUseValue] passes for the character, calls [block] with the character and consumes the character.
     *
     * Also finishes iteration as soon as [shouldUseValue] returns false.
     */
    inline fun readWhileAndUseWhile(shouldRead: () -> Boolean, shouldUseValue: (Char) -> Boolean, block: (SourceChar) -> Unit) {
        contract {
            callsInPlace(shouldRead, InvocationKind.AT_LEAST_ONCE)
            callsInPlace(shouldUseValue)
            callsInPlace(block)
        }

        generateWhileAndUseWhile( { hasMoreInput() && shouldRead() }, { remainingInputIter.current }, { shouldUseValue(it.c) }) {
            block(it)
            remainingInputIter.moveNext()
        }
    }

    /**
     * Returns true if there is more input to process, including the current character.
     */
    fun hasMoreInput() = remainingInputIter.hasCurrent()

    /**
     * Lexes the remainder of a raw string literal. The custom delimiter prevents doing this in the DFA.
     * The DFA should have just produced a RawStringStart pseudotoken indicating that the starting " has been read.
     */
    fun lexRawString(rawToken: PpToken): PpToken {
        require(rawToken.kind == Pptok.RawStringStart) { "Trying to lex a raw string without being in a raw string (got a ${rawToken.kind} token)." }
        require(rawToken.lexeme.endsWith("R\"")) { "Got a RawStringStart pseudotoken that doesn't end in R\" (lexeme=${rawToken.lexeme})" }

        // The lex context is used in a pre-lexing step to determine whether to do line splicing and UCN replacement.
        // Per [lex.pptoken]/3.1, these should not be done within the quotes.
        // The same paragraph also requires the token to be a raw string literal, so we can avoid any backtracking.
        val (charsIncludingEndQuote, delimiter, endQuoteReached) = readRestOfQuotedPartOfRawString()

        if (!endQuoteReached) {
            val lexeme = rawToken.lexeme + charsIncludingEndQuote.toText()
            val endLoc = if (charsIncludingEndQuote.isNotEmpty()) charsIncludingEndQuote.endLoc else rawToken.endLocation
            return PpToken(Pptok.InvalidUnterminatedLiteral, lexeme, rawToken.startLocation, endLoc, false)
        }

        val (possibleIdentifierSeq, newRemainingInputIter) = lexOneAdhoc(remainingInputIter.toSequence(), identifierDfa, lexContext)
        remainingInputIter = newRemainingInputIter

        val possibleIdentifier = possibleIdentifierSeq.toList()
        assert(possibleIdentifier.size <= 1) { "An identifier DFA managed to parse more than one token: ${possibleIdentifier.joinToString { "'${escapeAsciiStringForHumans(it.lexeme)}'" }}" }

        // From here, the identifier is taken as part of the token. An error could affect this, but better to lex everything if there's an error anyway.
        val fullLexeme = rawToken.lexeme + charsIncludingEndQuote.toText() + possibleIdentifier.joinToString("") { it.lexeme }

        // [lex.string]/1: The delimiter must be at most 16 characters.
        if (delimiter.length > 16) {
            diag(RawStringDelimiterTooLong(delimiter), rawToken.startLocation)
            val endLoc = if (possibleIdentifier.size == 1 && possibleIdentifier.first().kind == Pptok.Identifier) possibleIdentifier.first().endLocation else charsIncludingEndQuote.last().endLoc
            return PpToken(Pptok.InvalidToken, fullLexeme, rawToken.startLocation, endLoc, false)
        }

        return if (possibleIdentifier.size == 1) {
            // We got a UDL or the identifier was invalid
            // TODO: Could theoretically check UCNs earlier and guarantee a valid result here
            val kind = if (possibleIdentifier.first().kind == Pptok.Identifier) Pptok.StringUdl else Pptok.InvalidToken
            PpToken(kind, fullLexeme, rawToken.startLocation, possibleIdentifier.first().endLocation, false)
        } else {
            // We didn't get an identifier, so this is just a regular raw string
            PpToken(Pptok.StringLit, fullLexeme, rawToken.startLocation, charsIncludingEndQuote.endLoc, false)
        }
    }

    /**
     * Reads to and excluding the end " of a raw string literal.
     * Returns the input consumed, the delimiter lexeme, and a flag indicating whether the end " was found.
     * The " is not consumed because the lex context should be updated before the next character is read.
     */
    fun readRestOfQuotedPartOfRawString(): Triple<List<SourceChar>, String, Boolean> {
        val delimiterChars = buildList {
            readAndUseWhile({ it != '(' }) { add(it) }
        }
        val delimiter = delimiterChars.toText()

        val restLitChars = buildList {
            // Keep trying to move to the next ) and then read the end of the literal.
            // The loop exits when input ends while looking for ) or when the final " is found.
            while (true) {
                readAndUseWhile({ it != ')' }) { add(it) }

                // Move past the ) to the delimiter
                val paren = remainingInputIter.tryConsume() ?: return Triple(delimiterChars + this, delimiter, false)
                add(paren)

                // Try to match the delimiter
                var matchedCount = 0
                readAndUseWhile({ matchedCount < delimiterChars.size && it == delimiterChars[matchedCount].c }) {
                    add(it)
                    ++matchedCount
                }

                // If there's a " after, add it and that's the end of the content, just an optional identifier afterward
                if (matchedCount == delimiterChars.size && remainingInputIter.currentOrNull()?.c == '"') {
                    // Update the lex context here so that it's set before reading the character after the "
                    lexContext.state = LexContext.NothingSpecial
                    add(remainingInputIter.consume())
                    break
                }
            }
        }

        return Triple(delimiterChars + restLitChars, delimiter, true)
    }

    /**
     * Given info about a <:: token, returns the two < :: tokens if the next character is not ':' or '>'.
     * Otherwise, returns the <: token and puts the : back into the input to start the next token.
     */
    private fun replaceSpecialCaseTemplateToken(lexemeChars: List<SourceChar>, token: PpToken, nextChar: Char?): List<PpToken> {
        check(lexemeChars.size == 3) { "Expected <:: pseudotoken to have 3 characters, but it has ${lexemeChars.size}." }
        with (token) {
            return if (nextChar?.let { it in ":>" } == false) {
                val first = PpToken(Pptok.LessThan, lexeme.first().toString(), startLocation, lexemeChars.first().endLoc, false)
                val second = PpToken(Pptok.ColonColon, lexeme.substring(1), lexemeChars[1].loc, endLocation, false)
                listOf(first, second)
            } else {
                val endLocOfFirst = lexemeChars[1].endLoc
                val digraph = PpToken(Pptok.LSquareBracket, lexeme.substring(0, 2), startLocation, endLocOfFirst, false)

                remainingInputIter.prepend(sequenceOf(lexemeChars.last())) // Backtrack so that the colon can be used for the next token
                listOf(digraph)
            }
        }
    }
}

/**
 * Specifies an identifier token and nothing else.
 */
private val identifierDfa = dfa {
    acceptIdentifier("identifier", Pptok.Identifier)
}