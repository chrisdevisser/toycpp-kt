package toycpp.parser.combinators

import toycpp.parser.DiscardingParser
import toycpp.parser.Parser
import toycpp.parser.bindValue
import toycpp.parser.mapValue

infix fun<L, R, In> Parser<L, In>.then(next: Parser<R, In>): Parser<Pair<L, R>, In> =
    bindValue { firstValue -> next mapValue { nextValue -> Pair(firstValue, nextValue) } }

infix fun<L, In> Parser<L, In>.then(next: DiscardingParser<In>): Parser<L, In> =
    bindValue { firstValue -> next.mapValue { firstValue } }

infix fun<R, In> DiscardingParser<In>.then(next: Parser<R, In>): Parser<R, In> =
    bindValue { next }

infix fun<In> DiscardingParser<In>.then(next: DiscardingParser<In>): DiscardingParser<In> =
    -bindValue { next }

infix fun<T, In> Parser<List<T>, In>.thenL(next: Parser<List<T>, In>): Parser<List<T>, In> =
    bindValue { values -> next mapValue { nextValues -> values + nextValues } }