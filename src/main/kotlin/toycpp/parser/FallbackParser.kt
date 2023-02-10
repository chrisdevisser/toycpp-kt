package toycpp.parser

import toycpp.parser.ParseResult.Failure

class FallbackParser<out T, In>(rawOptions: List<Parser<T, In>>, name: String? = null) : Parser<T, In>(
    // Inner parsers are named appropriately. Any default-named FallbackParsers will match a flattened one's name.
    name = name ?: rawOptions.joinToString("|"),
) {
    val options: List<Parser<T, In>> = simplifyParsers(rawOptions)

    constructor(vararg options: Parser<T, In>) : this(options.toList())

    override fun parse(input: Sequence<In>): ParseResult<T, In> =
        // Start with failure, any successful parse cascades through the rest and determines the value.
        options.fold(Failure<T, In>(input, emptyList()).asBase()) { resultSoFar, nextParser ->
            resultSoFar.bindFailure {nextParser}
        }

    override fun named(newName: String): Parser<T, In> = FallbackParser(options, newName)

    /**
     * Flattens any directly nested FallbackParsers, resulting in one list.
     * This does not have to be recursive because inner FallbackParsers are guaranteed to be simplified.
     */
    @Suppress("UNCHECKED_CAST") // Not sure if it's possible to avoid the need for *
    private fun<T, In> simplifyParsers(parsers: List<Parser<T, In>>) = parsers.flatMap { parser ->
        if (parser is FallbackParser<*,*>)
            parser.options.map { it as Parser<T, In> }
        else
            listOf(parser)
    }
}