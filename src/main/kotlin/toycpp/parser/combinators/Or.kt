package toycpp.parser.combinators

import toycpp.parser.FallbackParser
import toycpp.parser.Parser

infix fun<T, In> Parser<T, In>.or(alt: Parser<T, In>) = FallbackParser(this, alt)