package toycpp.parser.combinators

import toycpp.parser.*

infix fun<T, In> Parser<T, In>.or(alt: Parser<T, In>) =
    FallbackParser(this, alt)

@JvmName("orTUnit")
infix fun<T, In> Parser<T, In>.or(alt: Parser<Unit, In>): Parser<T?, In> =
    this or (alt mapValue {null})

@JvmName("orUnitAlt")
infix fun<Alt, In> Parser<Unit, In>.or(alt: Parser<Alt, In>): Parser<Alt?, In> =
    (this mapValue {null}) or alt

@JvmName("orUnitUnit")
infix fun<In> Parser<Unit, In>.or(alt: Parser<Unit, In>): Parser<Unit, In> =
    -FallbackParser(this, alt)