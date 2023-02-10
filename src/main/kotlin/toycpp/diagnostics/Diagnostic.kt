package toycpp.diagnostics

import toycpp.diagnostics.DiagnosticSeverity.Error
import toycpp.encoding.escapeAsciiStringForHumans
import toycpp.extensions.getCurrentDirectory
import java.nio.file.Path

sealed class Diagnostic(val severity: DiagnosticSeverity) {
    abstract fun formatMessage(): String
}

/**
 * A flexible diagnostic for when it's not worth making a new class.
 */
class CustomDiagnostic(severity: DiagnosticSeverity, val message: String) : Diagnostic(severity) {
    override fun formatMessage(): String = message
}

/**
 * Issued when no source files are given to compile.
 */
class NoSourceFiles : Diagnostic(Error) {
    override fun formatMessage(): String = "No source files were supplied."
}

/**
 * Issued when a supplied path is not a valid path, e.g., because it contains a character that cannot be in a path.
 */
class InvalidPath(val attemptedPath: String, val firstBadChar: Char?) : Diagnostic(Error) {
    override fun formatMessage(): String {
        val firstBadCharPortion = if (firstBadChar != null) "'$firstBadChar'" else "unknown"
        return "The path '$attemptedPath' is not in a valid path format. The first bad character is $firstBadCharPortion."
    }
}

/**
 * Issued when a file is not found on disk.
 *
 * This is used as a more descriptive error, not for assuming a file can be read once it's found.
 */
class FileNotFound(val path: Path) : Diagnostic(Error) {
    override fun formatMessage(): String = "The file '$path' was not found.\nCurrent directory: ${getCurrentDirectory()}"
}

/**
 * Issued when a file cannot be read.
 */
class FileReadError(val path: Path, val cause: String) : Diagnostic(Error) {
    override fun formatMessage(): String = "Couldn't read file '$path'.\nCause: $cause"
}

/**
 * Issued when a source file is not in a valid encoding.
 */
class InvalidSourceEncoding(val expectedEncoding: String, val badByteValue: UByte) : Diagnostic(Error) {
    override fun formatMessage(): String =
        "Expected source file to be $expectedEncoding, but got invalid character with bad byte value of $badByteValue (${badByteValue.toString(radix = 16)})."
}

class InvalidToken(val lexeme: String) : Diagnostic(Error) {
    override fun formatMessage(): String =
        "Lexing a token couldn't finish after matching '${escapeAsciiStringForHumans(lexeme)}'."
}