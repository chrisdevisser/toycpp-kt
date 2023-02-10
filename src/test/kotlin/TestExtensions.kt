import org.junit.jupiter.api.Assertions.assertIterableEquals
import org.junit.jupiter.api.Assertions.assertTrue
import toycpp.location.SourceChar
import toycpp.location.SourceLocation

fun Sequence<SourceChar>.text(): String =
    joinToString("") { it.c.toString() }

fun Sequence<SourceChar>.locations(): Sequence<SourceLocation> =
    map { it.loc }

fun Sequence<Char>.withDummyLocations(): Sequence<SourceChar> =
    map { SourceChar(it, SourceLocation("test-generated-filename.cpp", 1, 1)) }

fun String.withDummyLocations(): Sequence<SourceChar> =
    asSequence().withDummyLocations()

fun assertSequenceEquals(expected: Sequence<Any>, actual: Sequence<Any>) =
    assertIterableEquals(expected.asIterable(), actual.asIterable())

fun<T> assertIterableMatches(iterable: Iterable<T>, pred: (T) -> Boolean) {
    for (x in iterable) {
        assertTrue(pred(x))
    }
}