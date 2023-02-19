package parser

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import toycpp.parser.AdhocParser
import toycpp.parser.ParseResult.Success

class AdhocParserTest {
    @Test
    fun `An adhoc parser uses the given parsing function`() {
        var called = false
        val parser = AdhocParser<Int, Char>("") { input ->
            called = true
            Success(0, input)
        }

        assertFalse(called)
        parser.parse("abc".asSequence())
        assertTrue(called)
    }
}