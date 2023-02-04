package toycpp.encoding

import toycpp.diagnostics.InvalidSourceEncoding
import toycpp.diagnostics.diag
import toycpp.extensions.toChar
import toycpp.location.LocationTracker

val MinAsciiChar = Char(0)
val MaxAsciiChar = Char(127)

/**
 * Converts the given bytes of a file to an ASCII-compatible BSCS (basic source character set) string per lex.charset.
 * Issues a diagnostic if a non-BSCS byte is encountered.
 *
 * @param filename The filename of the file being converted. This filename is used only for diagnostics
 * @param bytes The bytes to convert
 *
 * @return The converted string, or `null` if [bytes] contains a non-BSCS ASCII byte.
 */
fun ensureBscsAscii(filename: String, bytes: Iterable<UByte>): String? {
    val locTracker = LocationTracker(filename)
    val bscsChars =
            " \t\n" + Char(11) + Char(12) + // Vertical tab, form feed
            ('a'..'z').joinToString("") +
            ('A'..'Z').joinToString("") +
            ('0'..'9').joinToString("") +
            "_{}[]#()<>%:;.?*+-/^&|~!=,\\\"'"

    assert(bscsChars.count() == 96) { "BUG: The BSCS is specified as 96 characters." }

    return bytes.map { b ->
        if (b.toChar() in bscsChars) {
            val c = b.toChar()
            locTracker.feed(c)
            c
        } else {
            diag(InvalidSourceEncoding("ASCII limited to the basic source character set", b), locTracker.currentLocation)
            return null
        }
    }.joinToString("")
}

/**
 * Makes replacements in a string so that ASCII characters that are difficult to read become readable.
 *
 * Regular escape characters are replaced by their escaped version (e.g., \n).
 * Other invisible characters are replaced by \#00, with 00 being substituted for the decimal value of the code.
 */
fun escapeAsciiStringForHumans(s: String): String {
    val escapeCharReplacements = mapOf(
        Char(0) to "\\0",
        Char(7) to "\\a",
        '\b' to "\\b",
        '\t' to "\\t",
        '\n' to "\\n",
        Char(11) to "\\v",
        Char(12) to "\\f",
        '\r' to "\\r",
        '\\' to "\\\\",
        Char(127) to "\\DEL"
    )

    val numericalReplacements =
        (Char(1)..Char(31))
            .filterNot { it in escapeCharReplacements }
            .associateWith { String.format("\\#%02d", it.code) }

    val replacements = escapeCharReplacements + numericalReplacements

    return s
        .map { replacements[it] ?: it.toString() }
        .joinToString("")
}