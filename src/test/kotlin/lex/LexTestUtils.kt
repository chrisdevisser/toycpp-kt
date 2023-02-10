package lex

import org.junit.jupiter.api.Assertions.assertIterableEquals
import toycpp.lex.*
import toycpp.location.withLocations

private val dfa = createCppDfa()

fun lex(input: String, removeMarkerTokens: Boolean = true): List<PpToken> {
    val (sourceWithLocations, _) = input.asSequence().withLocations("test")
    return lazyLexPpTokens("test", sourceWithLocations, dfa, LexContextHolder()).toList()
        .filterNot { removeMarkerTokens && it.kind == Pptok.StartOfLine }
}

fun assertTokenKindsMatch(tokens: List<PpToken>, vararg kinds: Pptok) {
    assertIterableEquals(kinds.asIterable(), tokens.map { it.kind })
}

fun assertAllTokenKindsAre(kind: Pptok, tokens: List<PpToken>) =
    assertTokenKindsMatch(tokens, *tokens.map { kind }.toTypedArray())