package lex

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import toycpp.lex.Pptok.CharUdl

class CharUdlTest {
    @ParameterizedTest
    @ValueSource(strings = ["'a'foo", "'x'define", "'a'_s", "u8'a's", "L'a'\\u1234"])
    fun `A character UDL has a character literal followed by an identifier`(input: String) {
        val tokens = lex(input)

        assertEquals(1, tokens.size)
        assertEquals(CharUdl, tokens.first().kind)
    }

    @Test
    fun `A character UDL's suffix must be a valid identifier`() {
        val tokens = lex("'a'fo\\u123o")
        assertTrue(tokens.size != 1 || tokens.first().kind != CharUdl)
    }

    @Test
    fun `A space before a character UDL suffix is not lexically a UDL`() {
        val tokens = lex("'a' foo")
        assertTrue(tokens.size != 1 || tokens.first().kind != CharUdl)
    }
}