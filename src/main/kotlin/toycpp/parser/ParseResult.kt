package toycpp.parser

import toycpp.parser.ParseResult.Success

sealed class ParseResult<out T, In>(
    val remainingInput: Sequence<In>,
    val inputConsumed: List<In>,
) {
    class Success<out T, In>(val value: T, remainingInput: Sequence<In>, inputConsumed: List<In>)
        : ParseResult<T, In>(remainingInput, inputConsumed)

    class Failure<out T, In>(remainingInput: Sequence<In>, inputConsumed: List<In>)
        : ParseResult<T, In>(remainingInput, inputConsumed) {
            fun<R> cast() = Failure<R, In>(remainingInput, inputConsumed)
        }

    fun asBase() = this // For generic functions that require the type to match exactly
    fun withConsumed(consumed: (ParseResult<T, In>) -> List<In>) =
        mapEither({Success(value, remainingInput, consumed(this))}, {Failure(remainingInput, consumed(this))})

    /**
     * Picks the appropriate mapping function and returns the result of calling it with this parse result.
     */
    fun<R> mapEither(success: Success<T, In>.() -> R, failure: Failure<T, In>.() -> R): R =
        when (this) {
            is Success -> success(this)
            is Failure -> failure(this)
        }

    /**
     * Transforms a successful result while leaving a failure alone. The transformation produces the direct result.
     */
    fun<R> mapSuccess(transformValue: (T) -> R): ParseResult<R, In> =
        mapEither({Success(transformValue(value), remainingInput, inputConsumed)}, {cast()})

    /**
     * Transforms a successful result while leaving a failure alone. The transformation produces a new parser,
     * which is then called with the remaining input.
     */
    fun<R> bindSuccess(transformValue: (T) -> Parser<R, In>): ParseResult<R, In> =
        mapEither({
            transformValue(value)(remainingInput)
                .withConsumed { inputConsumed + it.inputConsumed }
        }, {cast()})
}

/**
 * Coalesces a result into a success, using the provided value if called on a failure.
 */
fun<Alt, T : Alt, In> ParseResult<T, In>.orElseSuccess(produceValue: () -> Alt): Success<Alt, In> =
    mapEither({this}, {Success(produceValue(), remainingInput, inputConsumed)})

/**
 * Transforms a failure while leaving a success alone. The transformation produces the direct result.
 */
fun<Alt, T : Alt, In> ParseResult<T, In>.mapFailure(produceResult: () -> ParseResult<Alt, In>): ParseResult<Alt, In> =
    mapEither({this}, {produceResult()})

fun<Alt, T : Alt, In> ParseResult<T, In>.bindFailure(produceParser: () -> Parser<Alt, In>): ParseResult<Alt, In> =
    mapEither({this}, { produceParser()(inputConsumed.asSequence() + remainingInput) })

