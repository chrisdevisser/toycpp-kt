package toycpp.parser.combinators

import toycpp.extensions.split1
import toycpp.parser.*
import toycpp.parser.ParseResult.Failure
import toycpp.parser.ParseResult.Success

@Deprecated("Probably not needed")
fun<T, In> oneIfNot(parser: Parser<T, In>): Parser<In, In> =
    AdhocParser("not ${parser.name}") { input ->
        parser.parse(input).mapEither(
            success = { Failure(remainingInput) },
            failure = { single<In>().parse(input) }
        )
    }