package parser

import capture
import org.junit.jupiter.api.Assertions.assertIterableEquals
import org.junit.jupiter.api.Test
import setNewCollectorAsSink
import toycpp.parser.combinators.or

class FallbackParserTest {
    @Test
    fun `Parsing a or b succeeds with a`() {
        val parser = +'a' or +'b'
        val result = parser("abc".asSequence())

        assertSuccessParse(result, 'a', "bc", "a")
    }

    @Test
    fun `Parsing a or b succeeds with b`() {
        val parser = +'a' or +'b'
        val result = parser("bac".asSequence())

        assertSuccessParse(result, 'b', "ac", "b")
    }

    @Test
    fun `Parsing a or b fails with neither`() {
        val parser = +'a' or +'b'
        val result = parser("cab".asSequence())

        assertFailureParse(result, "cab", "")
    }

    @Test
    fun `Parsing a or b iterates the sequence only once for a`() {
        val parser = +"abc" or +"abd"
        val input = "abcdef"
        val (seq, captured) = input.capture()

        val result = parser(seq)
        result.remainingInput.forEach {}

        assertIterableEquals(input.asIterable(), captured)
    }

    @Test
    fun `Parsing a or b iterates the sequence only once for b`() {
        val parser = +"abc" or +"abd"
        val input = "abdcef"
        val (seq, captured) = input.capture()

        val result = parser(seq)
        result.remainingInput.forEach {}

        assertIterableEquals(input.asIterable(), captured)
    }

    @Test
    fun `Parsing a or b iterates the sequence only once for neither`() {
        val parser = +"abc" or +"abd"
        val input = "abecdf"
        val (seq, captured) = input.capture()

        val result = parser(seq)
        result.remainingInput.forEach {}

        assertIterableEquals(input.asIterable(), captured)
    }
}