package toycpp.parser.combinators

import toycpp.parser.*
import toycpp.parser.ParseResult.Failure
import toycpp.parser.ParseResult.Success

infix fun<T, U, In> Parser<T, In>.followedBy(next: Parser<U, In>): Parser<T, In> =
    bindSuccess { firstResult ->
        next.mapSuccess { firstResult }
    } named("$name followed by ${next.name}")

infix fun<T, U, In> Parser<T, In>.notFollowedBy(next: Parser<U, In>): Parser<T, In> =
    bindSuccess { firstResult ->
        next.mapEither({Failure(remainingInput)}, {firstResult})
    }