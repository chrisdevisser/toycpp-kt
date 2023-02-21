package toycpp.preprocessor

import toycpp.extensions.split1
import toycpp.lex.PpToken
import toycpp.lex.Pptok
import toycpp.lex.ppinterface.PpContextHolder
import toycpp.location.LocationTracker
import toycpp.preprocessor.directives.HeaderLoader
import toycpp.preprocessor.directives.processImportDirective

fun Sequence<Sequence<PpToken>>.processDirectives(locationTracker: LocationTracker, macroTable: MacroTable, ppContext: PpContextHolder, headerLoader: HeaderLoader): Sequence<PpToken> = sequence {
    for (line in this@processDirectives) {
        val splitLine = line.split1() ?: continue

        // [cpp.pre]/1: Directives are:
        //     - #
        //     - import followed by: header name, <, identifier, string-literal, or :
        val (first, rest) = splitLine
        if (first.kind == Pptok.Pound) {
            processPoundDirectiveLineWithoutPound(rest, locationTracker, macroTable)
        } else if (isImportLine(first, macroTable)) {
            // [cpp.import]/1: import (and maybe export) aren't allowed to be object-like macros if we're here
            if (macroTable["import"]?.isObjectLike() == true) {
                // TODO: diag
            }

            if (first.lexeme == "export" && macroTable["export"]?.isObjectLike() == true) {
                // TODO diag
            }

            processImportDirective(sequenceOf(first) + rest, ppContext, headerLoader)
        } else {
            yield(first)
            yieldAll(rest)
        }
    }
}

fun isImportLine(firstToken: PpToken, macroTable: MacroTable) =
    firstToken.kind == Pptok.Identifier &&
    firstToken.lexeme in listOf("export", "import")

fun processPoundDirectiveLineWithoutPound(line: Sequence<PpToken>, locationTracker: LocationTracker, macroTable: MacroTable) {

}
