package toycpp.parser.combinators

import toycpp.parser.Parser
import toycpp.parser.bindValue
import toycpp.parser.mapValue

infix fun<L, R, In> Parser<L, In>.then(next: Parser<R, In>): Parser<Pair<L, R>, In> =
    bindValue { value -> next mapValue { nextValue -> Pair(value, nextValue) } }

infix fun<T, In> Parser<List<T>, In>.thenL(next: Parser<List<T>, In>): Parser<List<T>, In> =
    bindValue { values -> next mapValue { nextValues -> values + nextValues } }