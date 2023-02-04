package toycpp.extensions

fun<T> Sequence<T>.peek1(): Peek1Sequence<T> =
    Peek1Sequence(this)

class Peek1Sequence<T>(private val seq: Sequence<T>) : Sequence<Pair<T, T?>> {
    override fun iterator(): Iterator<Pair<T, T?>> {
        return Peek1Iterator(seq.iterator())
    }
}

class Peek1Iterator<T>(private val iter: Iterator<T>) : Iterator<Pair<T, T?>> {
    private var next: T? = null

    init {
        if (iter.hasNext()) {
            next = iter.next()
        }
    }

    override fun hasNext(): Boolean =
        next != null

    override fun next(): Pair<T, T?> {
        val current = next!!
        next = if (iter.hasNext()) iter.next() else null
        return Pair(current, next)
    }
}