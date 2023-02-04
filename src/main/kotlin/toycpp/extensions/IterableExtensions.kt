package toycpp.extensions

/**
 * Returns null if any element is null.
 * Otherwise, returns an identical list with non-nullable element type.
 */
fun<T> Iterable<T?>.liftAnyNull(): List<T>? =
    if (contains(null)) null else filterNotNull()