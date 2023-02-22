package toycpp.parser.combinators

import toycpp.parser.Parser
import toycpp.parser.mapValue

operator fun<T, In> Parser<T, In>.unaryMinus() =
    mapValue { Unit }