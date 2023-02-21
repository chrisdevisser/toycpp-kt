package toycpp.extensions

/**
 * Equivalent to (take(1), drop(1)), but iterates only once.
 *
 * Returns null if the sequence is empty.
 */
fun<T> Sequence<T>.split1(): Pair<T, Sequence<T>>? {
    val iter = iterator()
    val first = if (iter.hasNext()) iter.next() else return null

    return Pair(first, iter.asSequence())
}

fun<T> Sequence<T>.splitBy(isSeparator: (T) -> Boolean): Sequence<Sequence<T>> = sequence {
    val iter = iterator()

    while (iter.hasNext()) {
        yield(iter.asSequence().takeWhile { !isSeparator(it) } )
        if (iter.hasNext()) iter.next() // Skip separator
    }
}