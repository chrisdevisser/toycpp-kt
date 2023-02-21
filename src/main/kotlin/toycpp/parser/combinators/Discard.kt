package toycpp.parser.combinators

import toycpp.parser.DiscardingParser
import toycpp.parser.Parser
import toycpp.parser.mapValue

operator fun<T, In> Parser<T, In>.unaryMinus() =
    DiscardingParser(this)

operator fun<In> DiscardingParser<In>.unaryMinus() =
    this