package toycpp.parser.combinators

import toycpp.parser.*
import toycpp.parser.ParseResult.Failure

@Deprecated("Probably not needed")
fun<T, In> oneIfNot(parser: Parser<T, In>): Parser<In, In> =
    AdhocParser("not ${parser.name}") { input ->
        parser.parse(input).mapEither(
            success = { Failure(remainingInput) },
            failure = { one<In>().parse(input) }
        )
    }