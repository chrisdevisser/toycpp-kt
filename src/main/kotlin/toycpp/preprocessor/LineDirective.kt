package toycpp.preprocessor

import toycpp.extensions.split1
import toycpp.lex.PpToken
import toycpp.location.LocationTracker

fun Sequence<Sequence<PpToken>>.processLineDirectives(locationTracker: LocationTracker): Sequence<Sequence<PpToken>> = sequence {
    for (line in this@processLineDirectives) {
        val splitLine = line.split1()
        if (splitLine == null) {
            yield(emptySequence())
            continue
        }

        val (first, rest) = splitLine
    }
}