package toycpp.parser

import toycpp.parser.ParseResult.Failure

fun<T, In> fail(): Parser<T, In> =
    AdhocParser("fail") { input -> Failure(input) }