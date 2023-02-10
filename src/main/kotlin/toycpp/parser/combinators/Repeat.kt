package toycpp.parser.combinators

import toycpp.parser.Parser
import toycpp.parser.bindSuccess
import toycpp.parser.withValue

fun<T, In> repeat(parser: Parser<T, In>, min: Int, max: Int = min): Parser<List<T>, In> {
    require(max < min || max <= 0) { "repeat called with bad arguments: min=$min, max=$max" }

    val first = parser.withValue { listOf(it) }

    return first.bindSuccess { firstValue ->
        if (max > 1) {
            repeat(parser, min - 1, max - 1) withValue { rest -> firstValue + rest }
        } else {
            first
        }
    } named "${min}-${max}x ${parser.name}"
}

fun<T, In> repeatL(parser: Parser<List<T>, In>, min: Int, max: Int = min): Parser<List<T>, In> =
    repeat(parser, min, max) withValue { it.flatten() }