package lex

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertIterableEquals
import org.junit.jupiter.api.Test
import text
import toycpp.lex.LexContext
import toycpp.lex.LexContextHolder
import toycpp.lex.withLinesSpliced
import toycpp.location.SourceChar
import toycpp.location.SourceLocation
import toycpp.location.withLocations
import withDummyLocations

class LineSplicesTest {
    @Test
    fun `Text with no line splices is preserved`() {
        val input = "line 1\nline 2"

        val withoutLineSplices = input.withDummyLocations().withLinesSpliced(LexContextHolder())

        assertEquals(input, withoutLineSplices.text())
    }

    // This is based on an interpretation of lex.phases/2 that the spirit is to always have a trailing blank line.
    @Test
    fun `Text with a backslash at the end is not a line splice`() {
        val input = "line 1\\"

        val withoutLineSplices = input.withDummyLocations().withLinesSpliced(LexContextHolder())

        assertEquals(input, withoutLineSplices.text())
    }

    // "Only the last backslash on any physical source line shall be eligible for being part of such a splice."
    @Test
    fun `Line splices are not recursive`() {
        val input = """
            |line 1\\
            |
            |
        """.trimMargin()

        val withoutLineSplices = input.withDummyLocations().withLinesSpliced(LexContextHolder())

        assertEquals("line 1\\\n", withoutLineSplices.text())
    }

    @Test
    fun `Lines are spliced when splices occur`() {
        val inputOutputPairs = listOf(
            "line 1\\\nline 2" to "line 1line 2",
            "line 1\\\n\\\n" to "line 1"
        )

        for ((input, expected) in inputOutputPairs) {
            val withoutLineSplices = input.withDummyLocations().withLinesSpliced(LexContextHolder())

            assertEquals(expected, withoutLineSplices.text())
        }
    }

    @Test
    fun `Lines are not spliced inside a raw string literal`() {
        val input = """
            |R"(abc\
            |def)"
        """.trimMargin()

        val withoutLineSplices = input.withDummyLocations().withLinesSpliced(LexContextHolder(LexContext.InRawStringLiteral))

        assertEquals(input, withoutLineSplices.text())
    }

    @Test
    fun `Lines are not spliced when there are no line splices`() {
        val input = """
            |a\a\b\1\\n\0
        """.trimMargin()

        val withoutLineSplices = input.withDummyLocations().withLinesSpliced(LexContextHolder())

        assertEquals(input, withoutLineSplices.text())
    }

    @Test
    fun `Extra backslashes are preserved`() {
        val input = """
            |line\ 1
            |\\\123
        """.trimMargin()

        val withoutLineSplices = input.withDummyLocations().withLinesSpliced(LexContextHolder())

        assertEquals(input, withoutLineSplices.text())
    }

    @Test
    fun `The lex context can change during line splice removal`() {
        val input = """
            |line 1\
            |line 2 R"(123\
            |abc)" line 3\
            |line 4
        """.trimMargin()
        val context = LexContextHolder(LexContext.NothingSpecial)

        val withoutLineSplices = input.withDummyLocations().withLinesSpliced(context)
        var result = ""

        // Set the context to raw string literal at ( and back to normal at )
        for (c in withoutLineSplices.map { it.c }) {
            if (c == '(') context.state = LexContext.InRawStringLiteral
            else if (c == ')') context.state = LexContext.NothingSpecial

            result += c
        }

        assertEquals("""
            |line 1line 2 R"(123\
            |abc)" line 3line 4
        """.trimMargin(), result)
    }

    @Test
    fun `Line splices preserve the locations of the non-spliced characters`() {
        val input = """
            |123\\
            |123\
            |123
            |1
        """.trimMargin()
        val filename = "test-generated-filename.cpp"

        val withoutLineSplices = input.asSequence().withLocations(filename).first.withLinesSpliced(LexContextHolder())

        val expectedLineColChars = arrayOf(
            Triple(1, 1, '1'), Triple(1, 2, '2'), Triple(1, 3, '3'), Triple(1, 4, '\\'),
            Triple(2, 1, '1'), Triple(2, 2, '2'), Triple(2, 3, '3'),
            Triple(3, 1, '1'), Triple(3, 2, '2'), Triple(3, 3, '3'), Triple(3, 4, '\n'),
            Triple(4, 1, '1')
        )
        val expected = expectedLineColChars.map { (line, col, c) -> SourceChar(c, SourceLocation(filename, line, col)) }
        assertIterableEquals(expected, withoutLineSplices.asIterable())
    }
}