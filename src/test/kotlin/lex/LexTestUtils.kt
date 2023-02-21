package lex

import org.junit.jupiter.api.Assertions.assertIterableEquals
import toycpp.lex.*
import toycpp.lex.ppinterface.PpContextHolder
import toycpp.location.withLocations

fun lex(input: String, filename: String = "test", ppContext: PpContextHolder = PpContextHolder()): List<PpToken> {
    val lexContext = LexContextHolder()
    val source = input.asSequence().withLocations(filename).first.withLinesSpliced(lexContext)
    return lazyLexPpTokens(source, chooseDfasFromPpContext(ppContext), lexContext).toList()
}

fun assertTokenKindsMatch(tokens: List<PpToken>, vararg kinds: Pptok) {
    assertIterableEquals(kinds.asIterable(), tokens.map { it.kind })
}

fun assertAllTokenKindsAre(kind: Pptok, tokens: List<PpToken>) =
    assertTokenKindsMatch(tokens, *tokens.map { kind }.toTypedArray())