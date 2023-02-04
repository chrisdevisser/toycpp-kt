package toycpp.extensions

/**
 * Converts this byte to the character whose code point is the same value.
 */
fun UByte.toChar(): Char =
    toInt().toChar()