package toycpp.iterators

class CurrentIterator<out T>(private val iter: Iterator<T>) : Iterator<T> {
    private var current: T? = null

    fun current(): T? {
        // Start on first element.
        if (current == null && hasNext()) {
            next()
        }

        return current
    }

    override fun next() =
        if (iter.hasNext()) {
            current = iter.next()
            current!!
        } else {
            // This iterator supports one use of next when there are no more elements.
            // This supports current being null when the end is reached.
            val ret = current!!
            current = null
            ret
        }

    override fun hasNext() =
        current != null || iter.hasNext()
}

fun<T> Iterator<T>.withCurrent() = CurrentIterator(this)