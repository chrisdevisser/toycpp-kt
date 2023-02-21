package toycpp.parser

class DiscardingParser<In>(private val parser: Parser<*, In>) : Parser<Unit, In>(parser.name) {
    override fun doParse(input: Sequence<In>): ParseResult<Unit, In> =
        parser.parse(input).mapValue { Unit }

    override fun named(newName: String): DiscardingParser<In> {
        return DiscardingParser(parser named newName)
    }

}