package parser

import capture
import org.junit.jupiter.api.Assertions.assertIterableEquals
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import setNewCollectorAsSink
import toycpp.parser.combinators.oneIfNot
import toycpp.parser.combinators.then

class NotTest {
    init {
        setNewCollectorAsSink()
    }

    @ParameterizedTest
    @ValueSource(chars = ['a', 'b', '1', '.', '@', '\n'])
    fun `Parsing not a character passes for other characters`() {
        val parser = oneIfNot(+'x')
        val result = parser("abc".asSequence())

        assertSuccessParse(result, 'a', "bc", "a")
    }

    @Test
    fun `Parsing not a character fails for that character`() {
        val parser = oneIfNot(+'x')
        val result = parser("xbc".asSequence())

        assertFailureParse(result, "bc", "x")
    }

    @Test
    fun `Parsing not a character on empty input fails`() {
        val parser = oneIfNot(+'x')
        val result = parser("".asSequence())

        assertFailureParse(result, "", "")
    }

    @Test
    fun `Parsing not of a multicharacter parser succeeds for a partial match`() {
        val parser = oneIfNot(+'x' then +'y')
        val result = parser("xzabc".asSequence())

        assertSuccessParse(result, 'x', "zabc", "x")
    }

    @Test
    fun `Parsing not of a multicharacter parser succeeds for no match`() {
        val parser = oneIfNot(+'x' then +'y')
        val result = parser("yzabc".asSequence())

        assertSuccessParse(result, 'y', "zabc", "y")
    }

    @Test
    @Disabled
    fun `Parsing not of a multicharacter parser fails for a full match`() {
        val parser = oneIfNot(+'x' then +'y')
        val result = parser("xyabc".asSequence())

        assertFailureParse(result, "abc", "xy")
    }

    @Test
    fun `Parsing not a parser iterates the sequence only once for a partial match`() {
        val parser = oneIfNot(+'x' then +'y')
        val input = "xzabc"
        val (seq, iterated) = input.capture()

        val result = parser(seq)
        result.remainingInput.forEach {}

        assertIterableEquals(input.asIterable(), iterated)
    }

    @Test
    fun `Parsing not a parser iterates the sequence only once for no match`() {
        val parser = oneIfNot(+'x' then +'y')
        val input = "yzabc"
        val (seq, iterated) = input.capture()

        val result = parser(seq)
        result.remainingInput.forEach {}

        assertIterableEquals(input.asIterable(), iterated)
    }

    @Test
    fun `Parsing not a parser iterates the sequence only once for a full match`() {
        val parser = oneIfNot(+'x' then +'y')
        val input = "xyabc"
        val (seq, iterated) = input.capture()

        val result = parser(seq)
        result.remainingInput.forEach {}

        assertIterableEquals(input.asIterable(), iterated)
    }
}