package toycpp.parser.combinators

import toycpp.parser.Parser
import toycpp.parser.bindValue
import toycpp.parser.mapValue

infix fun<T, Sep, In> Parser<T, In>.separatedBy(separator: Parser<Sep, In>): Parser<List<T>, In> =
    bindValue { firstValue ->
        zeroOrMore(-separator then this) mapValue { rest -> listOf(firstValue) + rest }
    } named "$name separated by ${separator.name}"