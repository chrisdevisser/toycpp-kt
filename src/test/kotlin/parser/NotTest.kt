package parser

import capture
import org.junit.jupiter.api.Assertions.assertIterableEquals
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import toycpp.parser.combinators.oneIfNot
import toycpp.parser.combinators.then

class NotTest {
    @ParameterizedTest
    @ValueSource(chars = ['a', 'b', '1', '.', '@', '\n'])
    fun `Parsing not a character passes for other characters`() {
        val parser = oneIfNot(+'x')
        val result = parser.parse("abc".asSequence())

        assertSuccessParse(result, 'a', "bc")
    }

    @Test
    fun `Parsing not a character fails for that character`() {
        val parser = oneIfNot(+'x')
        val result = parser.parse("xbc".asSequence())

        assertFailureParse(result, "bc")
    }

    @Test
    fun `Parsing not a character on empty input fails`() {
        val parser = oneIfNot(+'x')
        val result = parser.parse("".asSequence())

        assertFailureParse(result, "")
    }

    @Test
    fun `Parsing not of a multicharacter parser succeeds for a partial match`() {
        val parser = oneIfNot(+'x' then +'y')
        val result = parser.parse("xzabc".asSequence())

        assertSuccessParse(result, 'x', "zabc")
    }

    @Test
    fun `Parsing not of a multicharacter parser succeeds for no match`() {
        val parser = oneIfNot(+'x' then +'y')
        val result = parser.parse("yzabc".asSequence())

        assertSuccessParse(result, 'y', "zabc")
    }

    @Test
    @Disabled
    fun `Parsing not of a multicharacter parser fails for a full match`() {
        val parser = oneIfNot(+'x' then +'y')
        val result = parser.parse("xyabc".asSequence())

        assertFailureParse(result, "abc")
    }

    @Test
    @Disabled
    fun `Parsing not a parser iterates the sequence only once for a partial match`() {
        val parser = oneIfNot(+'x' then +'y')
        val input = "xzabc"
        val (seq, iterated) = input.capture()

        val result = parser.parse(seq)
        result.remainingInput.forEach {}

        assertIterableEquals(input.asIterable(), iterated)
    }

    @Test
    @Disabled
    fun `Parsing not a parser iterates the sequence only once for no match`() {
        val parser = oneIfNot(+'x' then +'y')
        val input = "yzabc"
        val (seq, iterated) = input.capture()

        val result = parser.parse(seq)
        result.remainingInput.forEach {}

        assertIterableEquals(input.asIterable(), iterated)
    }

    @Test
    @Disabled
    fun `Parsing not a parser iterates the sequence only once for a full match`() {
        val parser = oneIfNot(+'x' then +'y')
        val input = "xyabc"
        val (seq, iterated) = input.capture()

        val result = parser.parse(seq)
        result.remainingInput.forEach {}

        assertIterableEquals(input.asIterable(), iterated)
    }
}