package lex

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import toycpp.lex.Pptok
import toycpp.lex.Pptok.Ppnum


class PpnumTest {
    @ParameterizedTest
    @ValueSource(strings = ["0", "1", "2", "3", "9"])
    fun `Single digits are ppnums`(input: String) {
        val tokens = lex(input)

        assertEquals(1, tokens.size)
        assertTrue(tokens.first().kind == Ppnum)
    }

    @ParameterizedTest
    @ValueSource(strings = [".0", ".5"])
    fun `Ppnums can have a leading dot`(input: String) {
        val tokens = lex(input)

        assertEquals(1, tokens.size)
        assertTrue(tokens.first().kind == Ppnum)
    }

    @Test
    fun `A single dot is not a ppnum`() {
        val tokens = lex(".")
        assertTrue(tokens.size != 1 || tokens.first().kind != Ppnum)
    }

    @ParameterizedTest
    @ValueSource(strings = [
        "2340", "000", "12345678901234567890", ".500",
        "1a", "0u", "5abc", ".3t",
        "1_", ".5__",
        "0\\u0000", ".5\\Udeadbeef",
        "0'1'2", ".53'0",
        "0'a'b", ".53'x",
        "0'__'_", ".53'_",
        "0e+2", ".5E-1", "4p-2", "1P+0",
        "0.", "2.5", "2.a.3", ".5...",
        "0a'2.5e+4_\\u1234p-1"
    ])
    fun `Ppnums can have following digits, letters, underscores, UCNs, single quotes separating letters, numbers or underscores, exponent indicators with a sign after, or dots`(input: String) {
        val tokens = lex(input)

        assertEquals(1, tokens.size)
        assertTrue(tokens.first().kind == Ppnum)
    }

    @Test
    fun `Ppnums can't end with a single quote`() {
        val tokens = lex("0'")
        assertTrue(tokens.size != 1 || tokens.first().kind != Ppnum)
    }

    @ParameterizedTest
    @ValueSource(strings = ["0''1", "0++1", "0+-1"])
    fun `Ppnums can't have two consecutive single quotes or signs`(input: String) {
        val tokens = lex(input)
        assertTrue(tokens.size != 1 || tokens.first().kind != Ppnum)
    }

    @Test
    fun `Ppnums can't have a UCN right after a single quote`() {
        val tokens = lex("0'\\u1234")
        assertTrue(tokens.size != 1 || tokens.first().kind != Ppnum)
    }

    @ParameterizedTest
    @ValueSource(strings = ["0+", "0+1", "-2", ".5-e", "0'+1", "0e'+1"])
    fun `Ppnums can't have a sign without a preceding exponent indicator`(input: String) {
        val tokens = lex(input)
        assertTrue(tokens.size != 1 || tokens.first().kind != Ppnum)
    }
}