package integration

import lex.lex
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import toycpp.lex.Pptok

class CommentsInLiteralsTest {
    @ParameterizedTest
    @ValueSource(strings = ["'//abc'", "'/*abc*/'"])
    fun `Comments don't work in character literals`(input: String) {
        val tokens = lex(input)

        assertEquals(1, tokens.size)
        assertEquals(Pptok.CharLit, tokens.first().kind)
    }

    @ParameterizedTest
    @ValueSource(strings = ["\"//abc\"", "\"/*abc*/\""])
    fun `Comments don't work in string literals`(input: String) {
        val tokens = lex(input)

        assertEquals(1, tokens.size)
        assertEquals(Pptok.StringLit, tokens.first().kind)
    }
}