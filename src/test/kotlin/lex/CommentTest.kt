package lex

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import toycpp.lex.Pptok.*

class CommentTest {
    @ParameterizedTest
    @ValueSource(strings = [
        "//abc", "// abc", "//\\u.!~~ \"",
        "/*abc*/", "/* abc */", "/* \\u24 \" */"
    ])
    fun `Comments can be used to ignore text inside`(input: String) {
        val tokens = lex(input)

        assertEquals(1, tokens.size)
        assertTrue(tokens.first().kind == Comment)
    }

    @Test
    fun `Line comments only ignore characters until the next newline`() {
        val input = """
            |test//asd123;.//\u- 1
            |.
        """.trimMargin()

        val tokens = lex(input)

        assertEquals(4, tokens.size)
        assertTokenKindsMatch(tokens, Identifier, Comment, Newline, Dot)
    }

    @Test
    fun `Block comments only ignore characters until the ending marker`() {
        val tokens = lex("test /*abc '*/ .")

        assertEquals(3, tokens.size)
        assertTokenKindsMatch(tokens, Identifier, Comment, Dot)
    }

    @Test
    fun `Block comments can contain newlines`() {
        val input = """
            |/*abc
            |def*/
        """.trimMargin()

        val tokens = lex(input)

        assertEquals(1, tokens.size)
        assertEquals(Comment, tokens.first().kind)
    }

    @Test
    fun `Block comments require an ending marker`() {
        val tokens = lex("/*abc")
        assertTrue(tokens.size != 1 || tokens.first().kind != Comment)
    }

    @Test
    fun `Block comments don't nest`() {
        val tokens = lex("/* level 1 /* level 2 */")

        assertEquals(1, tokens.size)
        assertEquals(Comment, tokens.first().kind)
    }

    @Test
    fun `Block comments are ignored in line comments`() {
        val input = """
            |//abc /*
            |*/
        """.trimMargin()

        val tokens = lex(input)

        assertEquals(4, tokens.size)
        assertTokenKindsMatch(tokens, Comment, Newline, Times, Over)
    }

    @Test
    fun `Line comments are ignored in block comments`() {
        val input = """
            |/*abc // end*/
            |*/
        """.trimMargin()

        val tokens = lex(input)

        assertEquals(4, tokens.size)
        assertTokenKindsMatch(tokens, Comment, Newline, Times, Over)
    }
}