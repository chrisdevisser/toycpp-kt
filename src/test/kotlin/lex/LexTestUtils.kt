package lex

import org.junit.jupiter.api.Assertions.assertIterableEquals
import toycpp.lex.*
import toycpp.location.withLocations

private val dfa = createCppDfa()

fun lex(input: String, filename: String = "test"): List<PpToken> {
    val lexContext = LexContextHolder()
    val source = input.asSequence().withLocations(filename).first.withLinesSpliced(lexContext)
    return lazyLexPpTokens(source, dfa, lexContext).toList()
}

fun assertTokenKindsMatch(tokens: List<PpToken>, vararg kinds: Pptok) {
    assertIterableEquals(kinds.asIterable(), tokens.map { it.kind })
}

fun assertAllTokenKindsAre(kind: Pptok, tokens: List<PpToken>) =
    assertTokenKindsMatch(tokens, *tokens.map { kind }.toTypedArray())