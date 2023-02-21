package toycpp.parser.combinators

import toycpp.parser.*

infix fun<T, In> Parser<T, In>.or(alt: Parser<T, In>) =
    FallbackParser(this, alt)

infix fun<T, In> Parser<T, In>.or(alt: DiscardingParser<In>): Parser<T?, In> =
    this or (alt mapValue {null})

infix fun<Alt, In> DiscardingParser<In>.or(alt: Parser<Alt, In>): Parser<Alt?, In> =
    (this mapValue {null}) or alt

infix fun<In> DiscardingParser<In>.or(alt: DiscardingParser<In>): DiscardingParser<In> =
    -FallbackParser(this, alt)