package toycpp.parser

import toycpp.debug.prettyFormat
import toycpp.diagnostics.trace

sealed class ParseResult<out T, In>(
    val remainingInput: Sequence<In>,
    val inputsConsumed: Int
) {
    class Success<out T, In>(val result: T, remainingInput: Sequence<In>, inputsConsumed: Int)
        : ParseResult<T, In>(remainingInput, inputsConsumed)

    class Failure<out T, In>(val result: T, remainingInput: Sequence<In>, inputsConsumed: Int)
        : ParseResult<T, In>(remainingInput, inputsConsumed)

    fun asBase() = this // For generic functions that require the type to match exactly

    /**
     * Picks the appropriate mapping function and returns the result of calling it with this parse result.
     */
    fun<R> map(success: (Success<T, In>) -> R, failure: (Failure<T, In>) -> R): R =
        when (this) {
            is Success -> success(this)
            is Failure -> failure(this)
        }
}

class Parser<out T, In>(
    val name: String,
    val parse: (Sequence<In>) -> ParseResult<T, In>
) {
    operator fun invoke(input: Sequence<In>): ParseResult<T, In> {
        val result = parse(input)

        val traceMessage = "Parse of $name: " +
                result.map(success = {"Success! (${prettyFormat(it.result)})"}, failure = {"Failure!"})
        trace(traceMessage)

        return result
    }
}