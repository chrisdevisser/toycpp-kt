package lex

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import toycpp.lex.Pptok.CharLit
import toycpp.lex.check_passes.diagnoseEmptyCharacterLiterals

class CharLitTest {
    @ParameterizedTest
    @ValueSource(strings = ["'a'", "'123'", "'@'", "'\"'", "'\\u1234'", "'\\Udeadbeef'"])
    fun `A character literal is a series of characters or UCNs enclosed in single quotes`(input: String) {
        val tokens = lex(input)

        assertEquals(1, tokens.size)
        assertEquals(CharLit, tokens.first().kind)
    }

    @ParameterizedTest
    @ValueSource(strings = ["u8'a'", "u'a'", "U'a'", "L'a'"])
    fun `A character literal can have an encoding prefix`(input: String) {
        val tokens = lex(input)

        assertEquals(1, tokens.size)
        assertEquals(CharLit, tokens.first().kind)
    }

    @ParameterizedTest
    @ValueSource(strings = [
        "'\\''", "'\\\"'", "'\\?'", "'\\\\'", "'\\a'", "'\\b'", "'\\f'", "'\\n'", "'\\r'", "'\\t'", "'\\v'",
        "'\\1'", "'\\12'", "'\\123'", "'\\456'", "'\\567'", "'\\1\\12\\123a'", "'\\128'",
        "'\\x0'", "'\\x1234567890abcdefABCDEF'"
    ])
    fun `A character literal can have escape sequences`(input: String) {
        val tokens = lex(input)

        assertEquals(1, tokens.size)
        assertEquals(CharLit, tokens.first().kind)
    }

    @ParameterizedTest
    @ValueSource(strings = ["'a", "'123", "'\\"])
    fun `A character literal needs a terminating single quote`(input: String) {
        val tokens = lex(input)
        assertTrue(tokens.size != 1 || tokens.first().kind != CharLit)
    }

    @ParameterizedTest
    @ValueSource(strings = ["'\\e'", "'\\8'", "'\\9'", "'\\xg'", "'\\xG'", "'\\A'", "'\\N'", "'\\#'"])
    fun `Only specific escape sequences are allowed in a character literal`(input: String) {
        val tokens = lex(input)
        assertTrue(tokens.size != 1 || tokens.first().kind != CharLit)
    }

    @Test
    fun `A character literal can't contain newlines`() {
        val tokens = lex("'\n'")
        assertTrue(tokens.size != 1 || tokens.first().kind != CharLit)
    }

    @Test
    fun `A character literal can't be empty`() {
        val tokens = lex("''")
        assertFalse(diagnoseEmptyCharacterLiterals(tokens))
    }
}