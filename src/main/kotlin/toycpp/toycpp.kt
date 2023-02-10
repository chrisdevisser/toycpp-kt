package toycpp

import toycpp.diagnostics.DiagnosticSystem
import toycpp.encoding.ensureBscsAscii
import toycpp.filesystem.Filesystem
import toycpp.lex.LexContext
import toycpp.lex.LexContextHolder
import toycpp.lex.createCppDfa
import toycpp.lex.ppinterface.PpContext
import toycpp.lex.ppinterface.PpContextHolder
import toycpp.lex.withLinesSpliced
import toycpp.location.withLocations
import java.nio.file.Path
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    try {
        DiagnosticSystem.setDiagnosticSink(DiagnosticReporter(trace = true))

        val filesystem = SystemFilesystem()
        val programOptions = parseProgramOptions(args, filesystem)
        programOptions ?: exitProcess(1)

        val sourcePaths = programOptions.sourcePaths
        val binaries = sourcePaths.map { compileToBinary(it, filesystem) }
        val executableFilename = linkToExecutable(binaries)

        if (DiagnosticSystem.hasErrorBeenIssued) {
            exitProcess(1)
        }
    } catch (e: Exception) {
        System.err.println("Compiler bug - unhandled exception: $e")
        exitProcess(1)
    }
}

/**
 * Executes the phases of translation on a source file to produce a binary per `lex.phases`.
 */
fun compileToBinary(sourcePath: Path, filesystem: Filesystem) {
    val sourceFilename = sourcePath.fileName.toString()

    // /1: Physical characters are mapped to the BSCS.
    //
    // For now, the source file must be in ASCII and use \n for newlines. This means implicit UCNs aren't a thing.
    val sourceBytes = filesystem.readFileBytes(sourcePath) ?: return
    val sourceStr = ensureBscsAscii(sourceFilename, sourceBytes.asUByteArray()) ?: return

    // From now on, the source has location tracking attached to it as long as the same location tracker instance is used.
    val (sourceWithLocation, locationTracker) = sourceStr.asSequence().withLocations(sourceFilename)

    // /2: Line splices are removed internally in the lexer. The location still reflects both lines.
    // It is not necessary to ensure the file ends in a blank line. The lexer will treat both the same.
    val lexContext = LexContextHolder(LexContext.NothingSpecial)
    val sourceWithoutLineSplices = sourceWithLocation.withLinesSpliced(lexContext)

    // /3: Lexing is done to create pptokens.
    //
    // Whitespace is kept only as a token flag indicating leading whitespace.
    val ppContext = PpContextHolder(PpContext.NothingSpecial)
    val dfa = createCppDfa()
//    val lexer = Lexer(sourceWithoutLineSplices, locationTracker)
//    val pptokens = lexer.lazyLexPptokens().removeComments()

}

fun linkToExecutable(binaries: List<Unit>) {
    // TODO
}