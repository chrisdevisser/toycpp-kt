package parser

import assertSequenceEquals
import org.junit.jupiter.api.Assertions.*
import toycpp.parser.ParseResult
import toycpp.parser.ParseResult.Failure
import toycpp.parser.ParseResult.Success
import toycpp.parser.buildAny
import toycpp.parser.buildAnyExcept
import toycpp.parser.buildSeq

private val anyImpl = buildAny<Char, Char> { it }
private val anyExceptImpl = buildAnyExcept<Char, Char> { it  }
private val seqImpl = buildSeq<Char, Char> { it }

operator fun Char.unaryPlus() = anyImpl(listOf(this))
operator fun String.unaryPlus() = seqImpl(toList())

fun any(options: String) = anyImpl(options.asIterable())
private fun any(options: Iterable<Char>) = any(options.joinToString(""))

private fun anyExcept(exclusion: Char) = anyExceptImpl(listOf(exclusion))
fun anyExcept(vararg exclusions: Char) = anyExceptImpl(exclusions.asIterable())

fun<T> assertSuccessParse(result: ParseResult<T, Char>, value: T, remainingInput: String, inputConsumed: String) {
    assertTrue(result is Success)
    result as Success

    assertEquals(value, result.value)
    assertSequenceEquals(remainingInput.asSequence(), result.remainingInput)
    assertIterableEquals(inputConsumed.asIterable(), result.inputConsumed)
}

fun assertFailureParse(result: ParseResult<*, Char>, remainingInput: String, inputConsumed: String) {
    assertTrue(result is Failure)

    assertSequenceEquals(remainingInput.asSequence(), result.remainingInput)
    assertIterableEquals(inputConsumed.asIterable(), result.inputConsumed)
}