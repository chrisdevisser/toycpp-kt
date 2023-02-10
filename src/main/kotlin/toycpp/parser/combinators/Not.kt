package toycpp.parser.combinators

import toycpp.parser.ParseResult.Failure
import toycpp.parser.ParseResult.Success
import toycpp.parser.Parser
import toycpp.parser.mapResult

@Deprecated("Probably not needed")
fun<T, In> oneIfNot(parser: Parser<T, In>): Parser<In, In> =
    parser.mapResult {
        it.mapEither({ Failure(remainingInput, inputConsumed) },
            failure = {
                val first = inputConsumed.firstOrNull()
                val rest = inputConsumed.drop(1)

                if (first != null) {
                    Success(first, rest.asSequence() + remainingInput, listOf(first))
                } else {
                    Failure(emptySequence(), emptyList())
                }
            })
    }