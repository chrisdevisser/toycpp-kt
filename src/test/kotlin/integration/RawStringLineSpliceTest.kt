package integration

import lex.lex
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import toycpp.lex.Pptok

class RawStringLineSpliceTest {
     @Test
    fun `Line splices are reverted inside a raw string literal`() {
        val input = """
            |R"(ab\
            |c)"
        """.trimMargin()

        val tokens = lex(input)

        assertEquals(1, tokens.size)
        assertEquals(input, tokens.first().lexeme)
    }

    @Test
    fun `Line splices are not reverted before the opening quote of a raw string literal`() {
        val input = """
            |R\
            |"(abc)"
        """.trimMargin()

        val tokens = lex(input)

        assertEquals(1, tokens.size)
        assertEquals("R\"(abc)\"", tokens.first().lexeme)
    }

    @Test
    fun `Line splices are reverted after the opening quote of a raw string literal`() {
        val input = """
            |R"\
            |(abc)"
        """.trimMargin()

        val tokens = lex(input)

        assertEquals(1, tokens.size)
        assertEquals(input, tokens.first().lexeme)
    }

    @Test
    fun `Line splices are reverted before the closing quote of a raw string literal`() {
        val input = """
            |R"(abc)\
            |"
        """.trimMargin()

        val tokens = lex(input)

        assertEquals(1, tokens.size)
        assertEquals(input, tokens.first().lexeme)
    }

    @Test
    fun `Line splices are not reverted after the closing quote of a raw string literal`() {
        val input = """
            |R"(abc)"\
            |foo
        """.trimMargin()

        val tokens = lex(input)

        assertEquals(1, tokens.size)
        assertEquals("R\"(abc)\"foo", tokens.first().lexeme)
    }

    // [lex.string]/4 example
    @Test
    fun `A fake line splice in a raw string cannot prematurely end the literal`() {
        val input = """
            |R"a(
            |)\
            |a"
            |)a"
        """.trimMargin()

        val tokens = lex(input)

        assertEquals(1, tokens.size)
        assertEquals(input, tokens.first().lexeme)
    }
}