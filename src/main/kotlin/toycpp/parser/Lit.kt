package toycpp.parser

import toycpp.extensions.split1
import toycpp.parser.ParseResult.Failure
import toycpp.parser.ParseResult.Success
import toycpp.parser.combinators.get

fun<In> one(): Parser<In, In> =
    AdhocParser("one") { input ->
        input.split1()?.let { (first, rest) -> Success(first, rest) }
            ?: Failure(emptySequence())
    }

fun<In> oneOf(vararg options: In): Parser<In?, In> =
    one<In>()[{it in options}] named("one of ${options.joinToString()}")

fun<In> oneExcept(vararg options: In): Parser<In?, In> =
    one<In>()[{it !in options}] named("one except ${options.joinToString()}")

private fun<In, InComp> buildAnyImpl(proj: (In) -> InComp, comp: (InComp) -> Boolean): Parser<In, In> {
    fun parse(input: Sequence<In>): ParseResult<In, In> {
        val (first, rest) = input.split1() ?: return Failure(input)
        return if (comp(proj(first))) {
            Success(first, rest)
        } else {
            Failure(rest)
        }
    }
    return AdhocParser("", ::parse)
}

/**
 * Helps to build the parser that parses a single input from a set of given options.
 * The return value is a parser generator that takes the set of literal options
 * and returns a parser that can parse any of those options into a single result.
 *
 * varargs can easily be provided on top, but can't be supported directly.
 *
 * The set of options does not have to match the type of the input. A projection function is used to convert
 * from the input type to the option type for comparisons.
 */
fun<In, InComp> buildAny(nameSeparator: String = ",", proj: (In) -> InComp): (Iterable<InComp>) -> Parser<In, In> =
    { options: Iterable<InComp> ->
        buildAnyImpl(proj) { it in options } named "any(${options.joinToString(nameSeparator)})"
    }

fun<In, InComp> buildAnyExcept(nameSeparator: String = ",", proj: (In) -> InComp): (Iterable<InComp>) -> Parser<In, In> =
    { exclusions: Iterable<InComp> ->
        buildAnyImpl(proj) { it !in exclusions } named "anyExcept(${exclusions.joinToString(nameSeparator)})"
    }

fun<In, InComp> buildSeq(nameSeparator: String = ",", proj: (In) -> InComp): (Iterable<InComp>) -> Parser<List<In>, In> =
    { seq: Iterable<InComp> ->
        fun parse(input: Sequence<In>): ParseResult<List<In>, In> {
            val value = mutableListOf<In>()
            val inputConsumed = mutableListOf<In>()
            var remainingInput = input

            for (elem in seq) {
                val (first, rest) = remainingInput.split1() ?: return Failure(input)
                inputConsumed += first
                remainingInput = rest

                if (proj(first) != elem) {
                    return Failure(rest)
                }

                value += first
            }

            return Success(value, remainingInput)
        }
        AdhocParser("seq(${seq.joinToString(nameSeparator)})", ::parse)
    }