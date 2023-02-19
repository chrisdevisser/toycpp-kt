package toycpp.location

data class SourceChar(
    val c: Char,
    val loc: SourceLocation
) {
    val endLoc = loc.movePast()
}

/**
 * Views a sequence of characters with locations as just the characters.
 */
fun Sequence<SourceChar>.chars(): Sequence<Char> =
    map { it.c }

/**
 * See [chars]
 */
fun Iterable<SourceChar>.chars(): Iterable<Char> =
    map { it.c }

/**
 * Converts a sequence of characters with locations to a string containing the characters.
 */
fun Sequence<SourceChar>.toText(): String =
    chars().joinToString("")

/**
 * See [toText]
 */
fun Iterable<SourceChar>.toText(): String =
    chars().joinToString("")

/**
 * Views a sequence of characters with locations as just the locations.
 */
fun Sequence<SourceChar>.locations(): Sequence<SourceLocation> =
    map { it.loc }

/**
 * See [locations]
 */
fun Iterable<SourceChar>.locations(): Iterable<SourceLocation> =
    map { it.loc }

/**
 * The location of the first character in the sequence.
 * The sequence must not be empty.
 */
val Sequence<SourceChar>.startLoc: SourceLocation get() {
    require(any()) { "Attempted to get the start location of an empty sequence." }
    return first().loc
}

/**
 * See [startLoc]
 */
val Iterable<SourceChar>.startLoc: SourceLocation get() {
    require(any()) { "Attempted to get the start location of an empty iterable." }
    return first().loc
}

/**
 * The location past the end of the last character in the sequence.
 * The sequence must not be empty.
 */
val Sequence<SourceChar>.endLoc: SourceLocation get() {
    require(any()) { "Attempted to get the end location of an empty sequence." }
    return last().endLoc
}

/**
 * See [endLoc]
 */
val Iterable<SourceChar>.endLoc: SourceLocation get() {
    require(any()) { "Attempted to get the end location of an empty iterable." }
    return last().endLoc
}