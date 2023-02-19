package toycpp.parser

import toycpp.debug.prettyFormat
import toycpp.diagnostics.trace
import toycpp.parser.ParseResult.Failure
import toycpp.parser.ParseResult.Success

abstract class Parser<out T, In>(
    val name: String,
) {
    fun parse(input: Sequence<In>): ParseResult<T, In> {
        val result = doParse(input)

        val traceMessage = "Parse of $name: " +
                result.mapEither(success = {"Success! (${prettyFormat(result)})"}, failure = {"Failure!"})
        trace(traceMessage)

        return result
    }

    abstract fun doParse(input: Sequence<In>): ParseResult<T, In>
    abstract infix fun named(newName: String): Parser<T, In>
}

fun<T, R, In> Parser<T, In>.mapResult(transformResult: (ParseResult<T, In>) -> ParseResult<R, In>): Parser<R, In> =
    AdhocParser(name) { input ->
        transformResult(parse(input))
    }

fun<T, R, In> Parser<T, In>.mapEither(success: Success<T, In>.() -> ParseResult<R, In>, failure: Failure<T, In>.() -> ParseResult<R, In>): Parser<R, In> =
    mapResult { it.mapEither(success, failure) }

infix fun<T, R, In> Parser<T, In>.mapValue(transformValue: (T) -> R): Parser<R, In> =
    mapResult { it.mapValue(transformValue) }

fun<T, R, In> Parser<T, In>.bindValue(transformValue: (T) -> Parser<R, In>): Parser<R, In> =
    mapResult { it.bindValue(transformValue) }

fun<T, R, In> Parser<T, In>.mapSuccess(transformResult: (Success<T, In>) -> ParseResult<R, In>): Parser<R, In> =
    mapResult { it.mapSuccess(transformResult) }

fun<T, R, In> Parser<T, In>.bindSuccess(transformResult: (Success<T, In>) -> Parser<R, In>): Parser<R, In> =
    mapResult { it.bindSuccess(transformResult) }

fun<Alt, T : Alt, In> Parser<T, In>.orElseSuccess(produceValue: () -> Alt): Parser<Alt, In> =
    mapResult { it.orElseSuccess(produceValue) }

fun<Alt, T : Alt, In> Parser<T, In>.mapFailure(produceResult: () -> ParseResult<Alt, In>): Parser<Alt, In> =
    mapResult { it.mapFailure(produceResult) }

fun<Alt, T : Alt, In> Parser<T, In>.bindFailure(produceParser: () -> Parser<Alt, In>): Parser<Alt, In> =
    AdhocParser(name) { input ->
        parse(input).mapFailure { produceParser().parse(input) }
    }