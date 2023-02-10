package lex

import assertIterableMatches
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import toycpp.lex.Pptok.Identifier

class IdentifierTest {
    @ParameterizedTest
    @ValueSource(strings = ["a", "b", "_", "\\u0123", "\\Udeadbeef"])
    fun `Single letters, underscores, and UCNs are identifiers`(input: String) {
        val tokens = lex(input)

        assertEquals(1, tokens.size)
        assertTrue(tokens.first().kind == Identifier)
    }

    @Test
    fun `Single numbers are not identifiers`() {
        val tokens = lex("1")
        assertTrue(tokens.size != 1 || tokens.first().kind != Identifier)
    }

    @ParameterizedTest
    @ValueSource(strings = [
        "abc", "_abc", "\\u9876abc", "\\U00000000xyz",
        "a123", "_123", "\\u000000", "\\udeadbeef2",
        "a_", "__", "\\u5554__", "\\udeadbeef_",
        "a\\u1234\\Udeadbeef", "_\\Udeadbeef\\u1234", "\\u1234\\Udeadbeef", "\\Udeadbeef\\u1234",
        "a0_a\\u1234_"
    ])
    fun `Identifiers can have following letters, numbers, underscores, and UCNs`(input: String) {
        val tokens = lex(input)

        assertEquals(1, tokens.size)
        assertTrue(tokens.first().kind == Identifier)
    }

    @ParameterizedTest
    @ValueSource(strings = ["u", "u8", "U", "L", "R", "uR", "u8R", "LR"])
    fun `Encoding prefixes are identifiers`(input: String) {
        val tokens = lex(input)

        assertEquals(1, tokens.size)
        assertTrue(tokens.first().kind == Identifier)
    }

    @ParameterizedTest
    @ValueSource(strings = ["u123", "u8_abc", "U_", "L1", "R0", "uR\\u1234", "u8RR", "LR\\Udeadbeef"])
    fun `Identifiers can start with encoding prefixes`(input: String) {
        val tokens = lex(input)

        assertEquals(1, tokens.size)
        assertTrue(tokens.first().kind == Identifier)
    }
}