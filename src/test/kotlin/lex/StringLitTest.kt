package lex

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import toycpp.lex.Pptok.StringLit

class StringLitTest {
    @ParameterizedTest
    @ValueSource(strings = ["\"a\"", "\"123\"", "\"@\"", "\"'\"", "\"\\u1234\"", "\"\\Udeadbeef\""])
    fun `A string literal is a series of characters or UCNs enclosed in double quotes`(input: String) {
        val tokens = lex(input)

        assertEquals(1, tokens.size)
        assertEquals(StringLit, tokens.first().kind)
    }

    @ParameterizedTest
    @ValueSource(strings = ["u8\"a\"", "u\"a\"", "U\"a\"", "L\"a\""])
    fun `A string literal can have an encoding prefix`(input: String) {
        val tokens = lex(input)

        assertEquals(1, tokens.size)
        assertEquals(StringLit, tokens.first().kind)
    }

    @ParameterizedTest
    @ValueSource(strings = [
        "\"\\\"\"", "\"\\\"\"", "\"\\?\"", "\"\\\\\"", "\"\\a\"", "\"\\b\"", "\"\\f\"", "\"\\n\"", "\"\\r\"", "\"\\t\"", "\"\\v\"",
        "\"\\1\"", "\"\\12\"", "\"\\123\"", "\"\\456\"", "\"\\567\"", "\"\\1\\12\\123a\"", "\"\\128\"",
        "\"\\x0\"", "\"\\x1234567890abcdefABCDEF\""
    ])
    fun `A string literal can have escape sequences`(input: String) {
        val tokens = lex(input)

        assertEquals(1, tokens.size)
        assertEquals(StringLit, tokens.first().kind)
    }

    @ParameterizedTest
    @ValueSource(strings = ["\"a", "\"123", "\"\\"])
    fun `A string literal needs a terminating double quote`(input: String) {
        val tokens = lex(input)
        assertTrue(tokens.size != 1 || tokens.first().kind != StringLit)
    }

    @ParameterizedTest
    @ValueSource(strings = ["\"\\e\"", "\"\\8\"", "\"\\9\"", "\"\\xg\"", "\"\\xG\"", "\"\\A\"", "\"\\N\"", "\"\\#\""])
    fun `Only specific escape sequences are allowed in a string literal`(input: String) {
        val tokens = lex(input)
        assertTrue(tokens.size != 1 || tokens.first().kind != StringLit)
    }

    @Test
    fun `A string literal can't contain newlines`() {
        val tokens = lex("\"\n\"")
        assertTrue(tokens.size != 1 || tokens.first().kind != StringLit)
    }

    @Test
    fun `A string literal can be empty`() {
        val tokens = lex("\"\"")

        assertEquals(1, tokens.size)
        assertEquals(StringLit, tokens.first().kind)
    }

    @ParameterizedTest
    @ValueSource(strings = ["R\"(abc)\"", "uR\"(abc)\""])
    fun `A string literal can be a raw string`(input: String) {
        val tokens = lex(input)

        assertEquals(1, tokens.size)
        assertEquals(StringLit, tokens.first().kind)
    }

    @Test
    fun `A raw string literal can have parentheses inside the content`() {
        val tokens = lex("R\"(())())\"")

        assertEquals(1, tokens.size)
        assertEquals(StringLit, tokens.first().kind)
    }

    @ParameterizedTest
    @ValueSource(strings = ["R\"(\n)\"", "R\"(\"abc\")\"", "R\"(\\)\""])
    fun `A raw string literal can contain characters not allowed in a string literal`(input: String) {
        val tokens = lex(input)

        assertEquals(1, tokens.size)
        assertEquals(StringLit, tokens.first().kind)
    }

    @ParameterizedTest
    @ValueSource(strings = ["Ru\"(abc)\"", "RL\"(abc)\"", "Ru8\"(abc)\""])
    fun `A raw string literal needs the R to come after the encoding prefix`(input: String) {
        val tokens = lex(input)
        assertTrue(tokens.size != 1 || tokens.first().kind != StringLit)
    }

    @ParameterizedTest
    @ValueSource(strings = ["R\"\"", "R\"abc\"", "R\"(abc\"", "R\"abc)\""])
    fun `A raw string literal needs delimiting parentheses`(input: String) {
        val tokens = lex(input)
        assertTrue(tokens.size != 1 || tokens.first().kind != StringLit)
    }

    @ParameterizedTest
    @ValueSource(strings = [
        "R\"abc()abc\"", "R\"abc(abc)abc\"", "R\"*(abc)*\"", "R\"_.\r(abc)_.\r\"",
        "R\"1234567890123456(abc)1234567890123456\""
    ])
    fun `A raw string literal can have a custom delimiter around the parentheses`(input: String) {
        val tokens = lex(input)

        assertEquals(1, tokens.size)
        assertEquals(StringLit, tokens.first().kind)
    }

    @ParameterizedTest
    @ValueSource(strings = ["R\"abc(foo)def\"", "R\"abc(foo)\"", "R\"(foo)abc\""])
    fun `A raw string literal's custom delimiter has to match`(input: String) {
        val tokens = lex(input)
        assertTrue(tokens.size != 1 || tokens.first().kind != StringLit)
    }

    @Test
    fun `A raw string literal can have a closing parenthesis followed by a double quote when there is a delimiter`() {
        val tokens = lex("R\"abc()\"foo)\")abc\"")

        assertEquals(1, tokens.size)
        assertEquals(StringLit, tokens.first().kind)
    }

    @Test
    fun `A raw string literal can have a closing parenthesis followed by its delimiter if there isn't an ending quote after it`() {
        val tokens = lex("R\"abc(foo)abc gotcha)abc\"")

        assertEquals(1, tokens.size)
        assertEquals(StringLit, tokens.first().kind)
    }

    @Test
    fun `A raw string literal delimiter can be at most 16 characters`() {
        val tokens = lex("R\"12345678901234567(abc)12345678901234567")
        assertTrue(tokens.size != 1 || tokens.first().kind != StringLit)
    }
}