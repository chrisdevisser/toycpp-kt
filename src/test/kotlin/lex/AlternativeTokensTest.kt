package lex

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource
import toycpp.lex.Pptok
import toycpp.lex.Pptok.*
import java.util.stream.Stream

class AlternativeTokensTest {
    @ParameterizedTest
    @MethodSource("generateLexemeToTokenKindPairs")
    fun `Digraphs and other alternative tokens are lexed to their according equivalent tokens`(param: Pair<String, Pptok>) {
        val (input, kind) = param
        val tokens = lex(input)

        assertEquals(1, tokens.size)
        assertEquals(kind, tokens.first().kind)
    }

    @ParameterizedTest
    @ValueSource(strings = ["AND", "And", "aNd", "anD"])
    fun `Alternative tokens are case-sensitive`(input: String) {
        val tokens = lex(input)
        assertTrue(tokens.size != 1 || tokens.first().kind != And)
    }

    companion object {
        @JvmStatic
        fun generateLexemeToTokenKindPairs(): Stream<Pair<String, Pptok>> =
            Stream.of(
                "<%" to LBrace,
                "%>" to RBrace,
                "<:" to LSquareBracket,
                ":>" to RSquareBracket,
                "%:" to Pound,
                "%:%:" to Concat,
                "and" to And,
                "bitor" to BitOr,
                "or" to Or,
                "xor" to Xor,
                "compl" to Compl,
                "bitand" to BitAnd,
                "and_eq" to AndEquals,
                "or_eq" to OrEquals,
                "xor_eq" to XorEquals,
                "not" to Not,
                "not_eq" to NotEqualTo,
            )
    }
}