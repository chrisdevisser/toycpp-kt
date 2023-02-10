package toycpp.parser

import toycpp.debug.prettyFormat
import toycpp.diagnostics.trace

abstract class Parser<out T, In>(
    val name: String,
) {
    operator fun invoke(input: Sequence<In>): ParseResult<T, In> {
        val result = parse(input)

        val traceMessage = "Parse of $name: " +
                result.mapEither(success = {"Success! (${prettyFormat(result)})"}, failure = {"Failure!"})
        trace(traceMessage)

        return result
    }

    abstract fun parse(input: Sequence<In>): ParseResult<T, In>
    abstract infix fun named(newName: String): Parser<T, In>
}

fun<T, R, In> Parser<T, In>.mapResult(transformResult: (ParseResult<T, In>) -> ParseResult<R, In>): Parser<R, In> =
    AdhocParser(name) { input ->
        transformResult(this(input))
    }

fun<T, R, In> Parser<T, In>.bindSuccess(transformValue: (T) -> Parser<R, In>): Parser<R, In> =
    mapResult { it.bindSuccess(transformValue) }

fun<Alt, T : Alt, In> Parser<T, In>.orElseSuccess(produceValue: () -> Alt): Parser<Alt, In> =
    mapResult { it.orElseSuccess(produceValue) }

fun<Alt, T : Alt, In> Parser<T, In>.mapFailure(produceResult: () -> ParseResult<Alt, In>): Parser<Alt, In> =
    mapResult { it.mapFailure(produceResult) }

fun<Alt, T : Alt, In> Parser<T, In>.bindFailure(produceParser: () -> Parser<Alt, In>): Parser<Alt, In> =
    mapResult { it.bindFailure(produceParser) }

infix fun<T, R, In> Parser<T, In>.withValue(transformValue: (T) -> R): Parser<R, In> =
    mapResult { it.mapSuccess(transformValue) }
