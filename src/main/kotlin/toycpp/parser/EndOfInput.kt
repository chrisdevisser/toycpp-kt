package toycpp.parser

import toycpp.parser.ParseResult.Failure
import toycpp.parser.ParseResult.Success
import toycpp.parser.combinators.unaryMinus

fun<In> endOfInput(): Parser<Unit, In> =
    -AdhocParser<_, In>("end of input") { input ->
        if (input.any()) {
            Success(Unit, emptySequence())
        } else {
            Failure(input)
        }
    }