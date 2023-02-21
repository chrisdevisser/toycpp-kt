package toycpp.parser.combinators

import toycpp.parser.ParseResult
import toycpp.parser.ParseResult.Failure
import toycpp.parser.Parser
import toycpp.parser.mapSuccess
import toycpp.parser.mapValue

operator fun<T, In> Parser<T, In>.get(condition: (T) -> Boolean, conditionStr: String = "?"): Parser<T, In> =
    mapSuccess { result ->
        if (condition(result.value)) {
            result
        } else {
            Failure(result.remainingInput)
        }
    } named("$name[if $conditionStr]")