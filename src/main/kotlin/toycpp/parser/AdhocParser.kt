package toycpp.parser

class AdhocParser<T, In>(name: String, private val parseFunc: (Sequence<In>) -> ParseResult<T, In>) : Parser<T, In>(name) {
    override fun parse(input: Sequence<In>): ParseResult<T, In> = parseFunc(input)
    override fun named(newName: String) = AdhocParser(newName, parseFunc)
}