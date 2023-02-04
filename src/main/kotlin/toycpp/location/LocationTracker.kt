package toycpp.location

/**
 * Tracks the per-character location for a given C++ file as a line and column number.
 *
 * The location is tracked by feeding in characters one at a time via [feed].
 * When the current location is desired, it can be accessed through [currentLocation].
 *
 * The location can be set directly via [forceNewLineNumber] and [forceNewLineNumberAndFile]. This is intended for #line directive support.
 * Because of this, the location cannot be assumed to be within the file, nor can it be assumed to be monotonic.
 *
 * Line splices (`lex.phases/2`) are ignored for the purpose of location tracking. That is, unless the location is
 * set directly, it will reflect the actual location of the current character in the original file.
 *
 * Only UNIX line endings (\n) are currently supported.
 *
 * @param filename The filename this tracker uses to build locations. There are no restrictions on the value.
 * @param onLocationForceChanged A function called when [forceNewLineNumber] or [forceNewLineNumberAndFile] are used to forcibly change the location
 */
class LocationTracker(private var filename: String, private val onLocationForceChanged: () -> Unit = {}) {
    private var line: Int = 1
    private var col: Int = 1

    /**
     * The current location in the given file. If [forceNewLineNumber] or [forceNewLineNumberAndFile] has been called, this will not be directly accurate.
     */
    val currentLocation get() = SourceLocation(filename, line, col)

    /**
     * Updates the location based on the given character. A newline causes the line to be incremented,
     * while other characters cause the column to be incremented.
     */
    fun feed(c: Char) {
        if (c == '\n') {
            moveToLine(line + 1)
        } else {
            ++col
        }
    }

    /**
     * Sets a new line, discarding the current one. [onLocationForceChanged] is called.
     * This function is intended to support #line directives.
     */
    fun forceNewLineNumber(newLine: Int) {
        moveToLine(newLine)
        onLocationForceChanged()
    }

    /**
     * Sets a new line and filename, discarding the current ones. [onLocationForceChanged] is called.
     * This function is intended to support #line directives.
     *
     * @param newFilename See [filename]
     */
    fun forceNewLineNumberAndFile(newLine: Int, newFilename: String) {
        filename = newFilename
        forceNewLineNumber(newLine)
    }

    /**
     * Moves to the specified line, at the beginning of the line.
     */
    private fun moveToLine(nextLine: Int) {
        line = nextLine
        col = 1
    }
}