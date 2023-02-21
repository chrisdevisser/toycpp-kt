package toycpp.parser.combinators

import toycpp.parser.Parser
import toycpp.parser.bindValue
import toycpp.parser.orElseSuccess
import toycpp.parser.mapValue

fun<T, In> zeroOrMore(parser: Parser<T, In>): Parser<List<T>, In> {
    val first = parser.mapValue { listOf(it) }
    return first.bindValue { firstValue -> zeroOrMore(parser) mapValue { rest -> firstValue + rest } }
        .orElseSuccess { emptyList() }
        .named("0+ ${parser.name}")
}

fun<T, In> zeroOrMoreOpt(parser: Parser<T?, In>): Parser<List<T>, In> =
    zeroOrMore(parser) mapValue { it.filterNotNull() }

fun<T, In> zeroOrMoreL(parser: Parser<List<T>, In>): Parser<List<T>, In> =
    zeroOrMore(parser) mapValue { it.flatten() }