package lex

import capture
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource
import setNewCollectorAsSink
import toycpp.encoding.formFeed
import toycpp.encoding.verticalTab
import toycpp.lex.*
import toycpp.lex.Pptok.*
import toycpp.location.SourceLocation
import java.util.stream.Stream

class LexerTest {
    @ParameterizedTest
    @ValueSource(strings = [
        "abc def", "abc\ndef", "abc\tdef", "abc\u000bdef", "abc\u000cdef", // vertical tab, form feed
        "abc  def", "abc\t\n \u000b def"
    ])
    fun `Tokens can be separated by whitespace`(input: String) {
        val tokens = lex(input).filterNot { it.kind == Newline }

        assertEquals(2, tokens.size)
        assertAllTokenKindsAre(Identifier, tokens)
    }

    @Test
    fun `Newlines are preserved through lexing`() {
        val tokens = lex("\nabc\n\n123\n\n")

        assertEquals(7, tokens.size)
        assertTokenKindsMatch(tokens, Newline, Identifier, Newline, Newline, Ppnum, Newline, Newline)
    }

    @Test
    fun `Comments between two tokens do not cause them to be merged into one`() {
        val tokens = lex("abc/*comment*/def")

        assertEquals(3, tokens.size)
        assertTokenKindsMatch(tokens, Identifier, Comment, Identifier)
    }

    @Test
    fun `The lexer backtracks if it fails to lex a token after having a chance to lex a shorter token`() {
        val tokens = lex("%:%=") // %:% looks like it's going for %:%: but is already past %:

        assertEquals(2, tokens.size)
        assertTokenKindsMatch(tokens, Pound, ModEquals)
    }

    @ParameterizedTest
    @ValueSource(strings = ["abc", "123", ".", "and", "&&", "ab\\u1234c"])
    fun `Lexed tokens' lexemes match the input used to produce them`(input: String) {
        val tokens = lex(input)

        assertEquals(1, tokens.size)
        assertEquals(input, tokens.first().lexeme)
    }

    @Test
    fun `Tokens with no whitespace in front advertise that fact`() {
        val tokens = lex("abc^123:")

        assertEquals(4, tokens.size)
        assertTrue(tokens.none { it.hasLeadingWhitespace })
    }

    @Test
    fun `Tokens with whitespace in front advertise that fact`() {
        val tokens = lex(" abc\n\n123\t .$verticalTab[$formFeed]/**/! ").filterNot { it.kind in listOf(Newline, Comment) }

        assertEquals(6, tokens.size)
        assertTrue(tokens.all { it.hasLeadingWhitespace })
    }

    @Test
    fun `Token locations are tracked`() {
        val input = """
            |abc 123
            |"test"\
            |456 .
        """.trimMargin()

        val filename = "test-filename"
        val tokens = lex(input, filename).filterNot { it.kind == Newline }

        val expectedTokenPositions = listOf(
            Pair(Pair(1,1), Pair(1,4)), Pair(Pair(1,5), Pair(1,8)),
            Pair(Pair(2,1), Pair(2,7)),
            Pair(Pair(3,1), Pair(3,4)), Pair(Pair(3,5), Pair(3,6))
        )

        val toLocation = { (line, col): Pair<Int, Int> -> SourceLocation(filename, line, col) }
        val expectedStarts = expectedTokenPositions.map { toLocation(it.first) }
        val expectedEnds = expectedTokenPositions.map { toLocation(it.second) }

        assertEquals(5, tokens.size)
        assertIterableEquals(expectedStarts, tokens.map { it.startLocation })
        assertIterableEquals(expectedEnds, tokens.map { it.endLocation })
    }

    @Test
    fun `Backtracking doesn't re-iterate the input`() {
        val input = "%:%"
        val (inputSeq, captured) = input.asSequence().capture()

        lex(inputSeq.joinToString(""))

        assertEquals(input.length, captured.size)
    }

    @Test
    fun `Raw strings don't re-iterate the input`() {
        val input = "R\"()\"foo 123"
        val (inputSeq, captured) = input.asSequence().capture()

        lex(inputSeq.joinToString(""))

        assertEquals(input.length, captured.size)
    }

    @Test
    fun `Lexing an empty input is a no-op`() {
        val diags = setNewCollectorAsSink()
        val tokens = lex("")

        assertEquals(0, tokens.size)
        assertEquals(0, diags.issuedCount)
    }

    @Test
    fun `Line splices can occur mid-token`() {
        val tokens = lex("abc\\\ndef")

        assertEquals(1, tokens.size)
        assertEquals(Identifier, tokens.first().kind)
    }

    // TODO: unterminated comment, literal

    @ParameterizedTest
    @MethodSource("generateSpecialCaseLexInputsAndTokenKindPairs")
    fun `A less than followed by two colons can be lexed differently depending on the next character`(param: Pair<String, List<Pptok>>) {
        val (input, kinds) = param
        val tokens = lex(input)

        assertEquals(kinds.size, tokens.size)
        assertTokenKindsMatch(tokens, *kinds.toTypedArray())
    }

    companion object {
        @JvmStatic
        fun generateSpecialCaseLexInputsAndTokenKindPairs(): Stream<Pair<String, List<Pptok>>> =
            Stream.of(
                "<::abc>" to listOf(LessThan, ColonColon, Identifier, GreaterThan),
                "<:: >" to listOf(LessThan, ColonColon, GreaterThan),
                "<::<:>" to listOf(LessThan, ColonColon, LSquareBracket, GreaterThan),
                "<:: :>" to listOf(LessThan, ColonColon, RSquareBracket),
                "<::>" to listOf(LSquareBracket, RSquareBracket),
                "<::>=" to listOf(LSquareBracket, RSquareBracket, Assign),
                "<:::" to listOf(LSquareBracket, ColonColon),
                "<:::>" to listOf(LSquareBracket, ColonColon, GreaterThan),
            )
    }
}