package toycpp.parser.combinators

import toycpp.parser.Parser
import toycpp.parser.bindSuccess
import toycpp.parser.withValue

infix fun<L, R, In> Parser<L, In>.then(next: Parser<R, In>): Parser<Pair<L, R>, In> =
    bindSuccess { value -> next withValue { nextValue -> Pair(value, nextValue) } }

infix fun<T, In> Parser<List<T>, In>.thenL(next: Parser<List<T>, In>): Parser<List<T>, In> =
    bindSuccess { values -> next withValue { nextValues -> values + nextValues } }