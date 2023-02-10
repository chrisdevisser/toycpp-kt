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