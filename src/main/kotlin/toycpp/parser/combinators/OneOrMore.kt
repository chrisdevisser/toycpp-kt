package toycpp.parser.combinators

import toycpp.parser.Parser
import toycpp.parser.bindValue
import toycpp.parser.mapValue

fun<T, In> oneOrMore(parser: Parser<T, In>): Parser<List<T>, In> =
    parser.bindValue { first ->
        zeroOrMore(parser) mapValue { rest -> listOf(first) + rest }
    }

fun<T, In> oneOrMoreL(parser: Parser<List<T>, In>): Parser<List<T>, In> =
    oneOrMore(parser) mapValue { it.flatten() }