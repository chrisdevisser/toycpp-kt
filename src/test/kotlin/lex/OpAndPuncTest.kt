package lex

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import toycpp.lex.Pptok
import toycpp.lex.Pptok.*
import java.util.stream.Stream

class OpAndPuncTest {
    @ParameterizedTest
    @MethodSource("generateLexemeToTokenKindPairs")
    fun `Operators and punctuation are lexed as specified in the table`(param: Pair<String, Pptok>) {
        val (input, kind) = param
        val tokens = lex(input)

        assertEquals(1, tokens.size)
        assertEquals(kind, tokens.first().kind)
    }

    companion object {
        @JvmStatic
        fun generateLexemeToTokenKindPairs(): Stream<Pair<String, Pptok>> =
            Stream.of(
                "#" to Pound,
                "##" to Concat,
                "{" to LBrace,
                "}" to RBrace,
                "[" to LSquareBracket,
                "]" to RSquareBracket,
                "(" to OpenParen,
                ")" to CloseParen,
                ";" to Semicolon,
                ":" to Colon,
                "..." to Ellipsis,
                "?" to Cond,
                "::" to ColonColon,
                "." to Dot,
                ".*" to DotStar,
                "->" to Arrow,
                "->*" to ArrowStar,
                "~" to Compl,
                "!" to Not,
                "+" to Plus,
                "-" to Minus,
                "*" to Times,
                "/" to Over,
                "%" to Mod,
                "^" to Xor,
                "&" to BitAnd,
                "|" to BitOr,
                "=" to Assign,
                "+=" to PlusEquals,
                "-=" to MinusEquals,
                "*=" to TimesEquals,
                "/=" to OverEquals,
                "%=" to ModEquals,
                "^=" to XorEquals,
                "&=" to AndEquals,
                "|=" to OrEquals,
                "==" to EqualTo,
                "!=" to NotEqualTo,
                "<" to LessThan,
                ">" to GreaterThan,
                "<=" to LessThanOrEqualTo,
                ">=" to GreaterThanOrEqualTo,
                "<=>" to Spaceship,
                "&&" to And,
                "||" to Or,
                "<<" to LeftShift,
                ">>" to RightShift,
                "<<=" to LeftShiftEquals,
                ">>=" to RightShiftEquals,
                "++" to PlusPlus,
                "--" to MinusMinus,
                "," to Comma,
            )
    }
}