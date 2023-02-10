package toycpp.parser.combinators

import toycpp.parser.AdhocParser
import toycpp.parser.ParseResult.Failure
import toycpp.parser.ParseResult.Success
import toycpp.parser.Parser

infix fun<T, U, In> Parser<T, In>.followedBy(next: Parser<U, In>): Parser<T, In> =
    AdhocParser("followed by (${next.name})") { input: Sequence<In> ->
        val firstResult = this(input)
        if (firstResult is Success) {
            val nextResult = next(firstResult.remainingInput)
            if (nextResult is Success) {
                Success(firstResult.value, nextResult.inputConsumed.asSequence() + nextResult.remainingInput, firstResult.inputConsumed)
            } else {
                Failure(nextResult.remainingInput, firstResult.inputConsumed + nextResult.inputConsumed)
            }
        } else {
            firstResult
        }
    }