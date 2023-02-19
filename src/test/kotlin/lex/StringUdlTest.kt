package lex

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import toycpp.lex.Pptok.StringUdl

class StringUdlTest {
    @ParameterizedTest
    @ValueSource(strings = ["\"a\"foo", "\"x\"define", "\"a\"_s", "u8\"a\"s", "L\"a\"\\u1234", "R\"(a)\"foo"])
    fun `A string UDL has a character literal followed by an identifier`(input: String) {
        val tokens = lex(input)

        assertEquals(1, tokens.size)
        assertEquals(StringUdl, tokens.first().kind)
    }

    @ParameterizedTest
    @ValueSource(strings = ["\"a\"fo\\u123o", "R\"(a)\"fo\\u123o"])
    fun `A string UDL's suffix must be a valid identifier`(input: String) {
        val tokens = lex(input)
        assertTrue(tokens.size != 1 || tokens.first().kind != StringUdl)
    }

    @ParameterizedTest
    @ValueSource(strings = ["\"abc\" foo", "R\"(abc)\" foo"])
    fun `A space before a string UDL suffix is not lexically a UDL`(input: String) {
        val tokens = lex(input)
        assertTrue(tokens.size != 1 || tokens.first().kind != StringUdl)
    }

    // TODO: space between suffix
}