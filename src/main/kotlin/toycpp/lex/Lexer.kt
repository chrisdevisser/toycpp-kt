package toycpp.lex

import toycpp.dfa.Dfa
import toycpp.dfa.dfa
import toycpp.dfa.traverseDfaToFurthestAcceptingNode
import toycpp.diagnostics.RawStringDelimiterTooLong
import toycpp.diagnostics.diag
import toycpp.iterators.readWhileAndUseWhile
import toycpp.iterators.withCurrent
import toycpp.lex.fixup_passes.condenseWhitespace
import toycpp.lex.fixup_passes.transformAlternativeTokens
import toycpp.location.SourceChar
import toycpp.location.endLoc
import toycpp.location.startLoc
import toycpp.location.toText

typealias CppDfa = Dfa<Pptok>

fun lazyLexPpTokens(sourceText: Sequence<SourceChar>, getDfaPriorityList: () -> List<CppDfa>, lexContext: LexContextHolder): Sequence<PpToken> =
    lexCommon(Lexer(sourceText, getDfaPriorityList, lexContext))

private fun lexCommon(lexer: Lexer): Sequence<PpToken> =
    lexer.lex()
        .condenseWhitespace()
        .transformAlternativeTokens()

private class Lexer(
    sourceText: Sequence<SourceChar>,
    val getDfaPriorityList: () -> List<CppDfa>,
    val lexContext: LexContextHolder,
) {
    var remainingInputIter = sourceText.iterator().withCurrent()

    /**
     * Converts the input to a sequence of pptokens. This transformation is specified by [dfa].
     * Beyond the DFA, there are adjustments:
     * - SpecialCaseTemplateLex (<::) pseudotokens produce < :: if the next character is not : or >, otherwise <: with the last : starting the next token.
     * - RawStringStart pseudotokens are taken and finished in the lexer to produce either a StringLit or InvalidToken token.
     * - [lexContext] is updated when entering and exiting the quoted part of a raw string literal.
     */
    fun lex(): Sequence<PpToken> = sequence {
        var tokenCount = 0

        while (hasMoreInput()) {
            val (rawToken, sourceConsumed) = lexOneToken()
            ++tokenCount

            when (rawToken.kind) {
                Pptok.SpecialCaseTemplateLex -> {
                    // [lex.pptoken]/3.2: <:: is < ::, not <: :, if the next character is not : or >.
                    // The DFA gives back a pseudotoken and we fix it here.
                    // It can't be a separate pass because location info for the middle of the token is lost after here.
                    yieldAll(replaceSpecialCaseTemplateToken(sourceConsumed, rawToken, remainingInputIter.currentOrNull()?.c))
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
        val dfas = getDfaPriorityList()
        require(dfas.isNotEmpty()) { "Lexer received no valid DFAs to lex with." }

        val tokenInfo = dfas.firstNotNullOfOrNull { tryLexOneToken(it) }
        require(tokenInfo != null) { "No given DFAs could lex a token. Remaining input:\n${remainingInputIter.toSequence().toText()}" }

        return tokenInfo
    }

    /**
     * Lexes one token using the given DFA.
     *
     * If the start of a raw string literal is encountered, updates the lex context to reflect that.
     * This is done before the character after the opening " is read.
     *
     * Returns the given token, or null if there is the DFA doesn't accept the input.
     * Returns null if no input is consumed to form the token.
     */
    fun tryLexOneToken(dfa: CppDfa): Pair<PpToken, List<SourceChar>>? {
        val result = traverseDfaToFurthestAcceptingNode(remainingInputIter.toSequence(), dfa, { it.c }) {
            // Update the lex context here so that it's set as soon as the " is read, not after reading the next character
            if (it == Pptok.RawStringStart) {
                lexContext.state = LexContext.InRawStringLiteral
            }
        }

        val (kind, sourceConsumed, remainingInput) = result ?: return null
        remainingInputIter = remainingInput.iterator().withCurrent()

        return if (sourceConsumed.isNotEmpty()) {
            Pair(PpToken(kind, sourceConsumed.toText(), sourceConsumed.startLoc, sourceConsumed.endLoc), sourceConsumed)
        } else {
            null
        }
    }

    /**
     * Reads one character per iteration, starting with the current character.
     * If [shouldUseValue] passes for the character, calls [block] with the character and consumes the character.
     *
     * Finishes iteration as soon as [shouldUseValue] returns false.
     */
    inline fun readAndUseWhile(shouldUseValue: (Char) -> Boolean, block: (SourceChar) -> Unit) {
        readWhileAndUseWhile({ true }, shouldUseValue, block)
    }

    /**
     * Before each iteration, pre-emptively finishes iteration if [shouldRead] returns false.
     * Reads one character per iteration, starting with the current character.
     * If [shouldUseValue] passes for the character, calls [block] with the character and consumes the character.
     *
     * Also finishes iteration as soon as [shouldUseValue] returns false.
     */
    inline fun readWhileAndUseWhile(shouldRead: () -> Boolean, shouldUseValue: (Char) -> Boolean, block: (SourceChar) -> Unit) {
        remainingInputIter.readWhileAndUseWhile(shouldRead, { shouldUseValue(it.c) }, block)
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

        val (possibleIdentifier, _) = tryLexOneToken(identifierDfa) ?: Pair(null as PpToken?, emptyList())

        // From here, the identifier is taken as part of the token. An error could affect this, but better to lex everything if there's an error anyway.
        val fullLexeme = rawToken.lexeme + charsIncludingEndQuote.toText() + (possibleIdentifier?.lexeme ?: "")
        val endLoc = possibleIdentifier?.endLocation ?: charsIncludingEndQuote.last().endLoc

        // [lex.string]/1: The delimiter must be at most 16 characters.
        if (delimiter.length > 16) {
            diag(RawStringDelimiterTooLong(delimiter), rawToken.startLocation)
            return PpToken(Pptok.InvalidToken, fullLexeme, rawToken.startLocation, endLoc, false)
        }

        val kind = if (possibleIdentifier != null) Pptok.StringUdl else Pptok.StringLit
        return PpToken(kind, fullLexeme, rawToken.startLocation, endLoc, false)

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
            return if (nextChar?.let { it in ":>" } != true) { // No next character is treated as not in the list
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