package toycpp.parser.combinators

import toycpp.parser.Parser
import toycpp.parser.orElseSuccess
import toycpp.parser.mapValue

fun<T, In> optional(parser: Parser<T, In>): Parser<T?, In> =
    parser.orElseSuccess { null }

fun<T, In> optionalList(parser: Parser<List<T>, In>): Parser<List<T>, In> =
    optional(parser) mapValue { it ?: emptyList() }