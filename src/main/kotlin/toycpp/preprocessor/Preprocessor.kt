package toycpp.preprocessor

import toycpp.extensions.splitBy
import toycpp.lex.PpToken
import toycpp.lex.Pptok
import toycpp.location.LocationTracker

fun preprocess(tokens: Sequence<PpToken>, locationTracker: LocationTracker) {
    val lines = tokens.splitBy { it.kind == Pptok.Newline }
//        .processDirectives(locationTracker)
    val x = if (tokens.any()) Unit else 2
}