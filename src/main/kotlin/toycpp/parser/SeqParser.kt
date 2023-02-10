package toycpp.parser

import toycpp.parser.ParseResult.Success

class SeqParser<out T, In>(rawParsers: List<Parser<T, In>>, name: String? = null) : Parser<List<T>, In>(
    // Inner parsers are named appropriately. Any default-named SeqParsers will match a flattened one's name.
    name ?: rawParsers.joinToString(">")
) {
    val parsers: List<Parser<T, In>> = simplifyParsers(rawParsers)

    override fun parse(input: Sequence<In>): ParseResult<List<T>, In> =
        // Start with success, any failed parse cascades through the rest.
        // Each successful parse adds an element to the result list.
        parsers.fold(Success<List<T>, In>(emptyList(), input, emptyList()).asBase()) { resultSoFar, nextParser ->
            resultSoFar.bindSuccess { valueSoFar ->
                nextParser withValue { valueSoFar + it }
            }
        }

    override fun named(newName: String) = SeqParser(parsers, name)

    /**
     * Flattens any directly nested SeqParsers, resulting in one list.
     * This does not have to be recursive because inner SeqParsers are guaranteed to be simplified.
     */
    @Suppress("UNCHECKED_CAST") // Not sure if it's possible to avoid the need for *
    private fun<T, In> simplifyParsers(parsers: List<Parser<T, In>>) = parsers.flatMap { parser ->
        if (parser is SeqParser<*,*>)
            parser.parsers.map { it as Parser<T, In> }
        else
            listOf(parser)
    }
}