package toycpp.parser.combinators

import toycpp.parser.Parser
import toycpp.parser.bindValue
import toycpp.parser.mapValue

infix fun<L, R, In> Parser<L, In>.then(next: Parser<R, In>): Parser<Pair<L, R>, In> =
    bindValue { firstValue -> next mapValue { nextValue -> Pair(firstValue, nextValue) } }

@JvmName("thenLUnit")
infix fun<L, In> Parser<L, In>.then(next: Parser<Unit, In>): Parser<L, In> =
    bindValue { firstValue -> next mapValue { firstValue } }

@JvmName("thenUnitR")
infix fun<R, In> Parser<Unit, In>.then(next: Parser<R, In>): Parser<R, In> =
    bindValue { next }

@JvmName("thenUnitUnit")
infix fun<In> Parser<Unit, In>.then(next: Parser<Unit, In>): Parser<Unit, In> =
    -bindValue { next }

infix fun<T, In> Parser<List<T>, In>.thenL(next: Parser<List<T>, In>): Parser<List<T>, In> =
    bindValue { values -> next mapValue { nextValues -> values + nextValues } }
