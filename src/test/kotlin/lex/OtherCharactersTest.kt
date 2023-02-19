package lex

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import toycpp.lex.Pptok

class OtherCharactersTest {
    @Test
    fun `Backslashes are lexed as other characters`() {
        val tokens = lex("\\")

        assertEquals(1, tokens.size)
        assertEquals(Pptok.OtherCharacter, tokens.first().kind)
    }
}