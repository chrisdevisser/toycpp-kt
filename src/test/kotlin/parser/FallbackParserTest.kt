package parser

import capture
import org.junit.jupiter.api.Assertions.assertIterableEquals
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import toycpp.parser.combinators.or

class FallbackParserTest {
    @Test
    fun `Parsing a or b succeeds with a`() {
        val parser = +'a' or +'b'
        val result = parser.parse("abc".asSequence())

        assertSuccessParse(result, 'a', "bc")
    }

    @Test
    fun `Parsing a or b succeeds with b`() {
        val parser = +'a' or +'b'
        val result = parser.parse("bac".asSequence())

        assertSuccessParse(result, 'b', "ac")
    }

    @Test
    fun `Parsing a or b fails with neither`() {
        val parser = +'a' or +'b'
        val result = parser.parse("cab".asSequence())

        assertFailureParse(result, "ab")
    }

    @Test
    @Disabled
    fun `Parsing a or b iterates the sequence only once for a`() {
        val parser = +"abc" or +"abd"
        val input = "abcdef"
        val (seq, captured) = input.capture()

        val result = parser.parse(seq)
        result.remainingInput.forEach {}

        assertIterableEquals(input.asIterable(), captured)
    }

    @Test
    @Disabled
    fun `Parsing a or b iterates the sequence only once for b`() {
        val parser = +"abc" or +"abd"
        val input = "abdcef"
        val (seq, captured) = input.capture()

        val result = parser.parse(seq)
        result.remainingInput.forEach {}

        assertIterableEquals(input.asIterable(), captured)
    }

    @Test
    @Disabled
    fun `Parsing a or b iterates the sequence only once for neither`() {
        val parser = +"abc" or +"abd"
        val input = "abecdf"
        val (seq, captured) = input.capture()

        val result = parser.parse(seq)
        result.remainingInput.forEach {}

        assertIterableEquals(input.asIterable(), captured)
    }
}