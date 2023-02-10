package toycpp.location

/**
 * Represents a character-level location in a file. This location should not be assumed to reference a real location
 * in the given file, or even be for a real file, due to the #line directive.
 *
 * @param filename A filename string that is not guaranteed to be in any particular format
 * @param line The line number of the file, starting at 1
 * @param col The column number of the file, starting at 1
 */
data class SourceLocation(
    // The filename can't be a Path because #line directives must be able to set the filename to an arbitrary string literal.
    // In order for Path to be used, it would need a concrete type supporting this, which isn't a productive use of time.
    val filename: String,
    val line: Int,
    val col: Int
) {
    override fun toString() =
        "<$filename> $line:$col"

    companion object {
        /**
         * Compares two source locations while ignoring the filename. The line number is prioritized over the column number.
         */
        val fileInsensitiveComparator = compareBy<SourceLocation>({it.line}, {it.col})
    }
}