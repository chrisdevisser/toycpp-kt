package toycpp.parser

fun<T, In> seq(vararg parsers: Parser<T, In>) = SeqParser(parsers.toList())