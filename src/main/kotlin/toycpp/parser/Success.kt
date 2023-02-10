package toycpp.parser

import toycpp.parser.ParseResult.*

fun<T, In> success(value: T): Parser<T, In> =
    AdhocParser("success") { input -> Success(value, input, emptyList()) }