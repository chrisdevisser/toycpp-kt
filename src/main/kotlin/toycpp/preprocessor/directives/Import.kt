package toycpp.preprocessor.directives

import toycpp.extensions.split1
import toycpp.lex.PpToken
import toycpp.lex.Pptok.*
import toycpp.lex.ppinterface.PpContext
import toycpp.lex.ppinterface.PpContextHolder
import toycpp.parser.combinators.*
import toycpp.parser.mapValue
import toycpp.preprocessor.*

interface HeaderLoader {
    fun searchAngled(path: String): Boolean
    fun searchQuoted(path: String): Boolean

    fun loadAngled(path: String): ByteArray?
    fun loadQuoted(path: String): ByteArray?
}

fun replaceMacros(tokens: List<PpToken>): List<PpToken> {return tokens}

fun processImportDirective(line: Sequence<PpToken>, ppContext: PpContextHolder, headerLoader: HeaderLoader) {
    val savedLine = enableHeaderNameAfterImport(line, ppContext).toList()

//    val parser =
//        optional(namedIdentifier("export")) then -namedIdentifier("import") then (
//            (+AngledHeaderName mapValue {headerLoader.loadAngled(it.lexeme.removeOuterCharacters())}) or
//            (+QuotedHeaderName mapValue {headerLoader.loadQuoted(it.lexeme.removeOuterCharacters())}) or
//            oneOrMore(anyToken) mapValue {}
//        ) then
}

private fun String.removeOuterCharacters(): String {
    require(length >= 2) { "Tried to remove outer characters of a string that's too short ($this)" }
    return substring(1, length - 1)
}

fun enableHeaderNameAfterImport(input: Sequence<PpToken>, ppContext: PpContextHolder): Sequence<PpToken> = sequence {
    fun readUntilImport(): Triple<Boolean, List<PpToken>, Sequence<PpToken>> {
        buildList {
            val (first, secondOn) = input.split1() ?: return Triple(false, emptyList(), emptySequence())
            add(first)

            if (first.kind == Identifier && first.lexeme == "import") return Triple(true, this, secondOn)
            if (first.kind != Identifier || first.lexeme != "export") return Triple(false, this, secondOn)

            val (second, rest) = secondOn.split1() ?: return Triple(false, this, emptySequence())
            add(second)

            return if (second.kind == Identifier && second.lexeme == "import") {
                Triple(true, this, rest)
            } else {
                Triple(false, this, rest)
            }
        }
    }

    val (foundImport, readTokens, afterImport) = readUntilImport()
    yieldAll(readTokens)

    if (foundImport) {
        ppContext.state = PpContext.HeaderNameIsValid
        val (next, rest) = afterImport.split1() ?: return@sequence
        ppContext.state = PpContext.NothingSpecial

        yield(next)
        yieldAll(rest)
    } else {
        yieldAll(afterImport)
    }
}