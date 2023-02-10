package toycpp.parser.combinators

import toycpp.parser.Parser
import toycpp.parser.bindSuccess
import toycpp.parser.orElseSuccess
import toycpp.parser.withValue

fun<T, In> zeroOrMore(parser: Parser<T, In>): Parser<List<T>, In> {
    val first = parser.withValue { listOf(it) }
    return first.bindSuccess { firstValue -> zeroOrMore(parser) withValue { rest -> firstValue + rest } }
        .orElseSuccess { emptyList() }
        .named("0+ ${parser.name}")
}

fun<T, In> zeroOrMoreL(parser: Parser<List<T>, In>): Parser<List<T>, In> =
    zeroOrMore(parser) withValue { it.flatten() }