package toycpp.lex

import toycpp.lex.LexContext.InRawStringLiteral
import toycpp.location.SourceChar

/**
 * Returns the source text without any line splices per `lex.phases/2`.
 *
 * This operation is dependent on context that is conceptually gained later in translation.
 * That context is provided as a mutable parameter. It may change from resumption to resumption.
 */
fun Sequence<SourceChar>.withLinesSpliced(context: LexContextHolder): Sequence<SourceChar> =
    sequence {
        var savedSpliceStart = null as SourceChar?
        for (currentChar in this@withLinesSpliced) {
            val maybeInSplice = savedSpliceStart != null
            val maybeStartingSplice = currentChar.c == '\\' && context.state != InRawStringLiteral
            val finishingSplice = maybeInSplice && currentChar.c == '\n'

            if (maybeInSplice && !finishingSplice) yield(savedSpliceStart!!) // False alarm, catch up
            if (!maybeStartingSplice && !finishingSplice) yield(currentChar)
            savedSpliceStart = if (maybeStartingSplice) currentChar else null
        }

        if (savedSpliceStart != null) yield (savedSpliceStart) // Account for the source ending in a possible line splice
    }