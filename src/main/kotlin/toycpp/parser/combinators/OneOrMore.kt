package toycpp.parser.combinators

import toycpp.parser.Parser
import toycpp.parser.bindSuccess
import toycpp.parser.withValue

fun<T, In> oneOrMore(parser: Parser<T, In>): Parser<List<T>, In> =
    parser.bindSuccess { first ->
        zeroOrMore(parser) withValue { rest -> listOf(first) + rest }
    }

fun<T, In> oneOrMoreL(parser: Parser<List<T>, In>): Parser<List<T>, In> =
    oneOrMore(parser) withValue { it.flatten() }