package toycpp.iterators

/**
 * Acts similarly to a C++ iterator. When in range, [_current] is not null and can be used repeatedly.
 * When out of range, [_current] is null and no more iteration can be done.
 * The iterator moves to the first element upon construction; [_current] is never null until the end of the range is reached.
 */
class CurrentIterator<T>(private var iter: Iterator<T>) {
    private var _current: T? = null

    init {
        moveNext() // Start on the first element instead of before the first element
    }

    /**
     * Returns the current value. The iterator must be in range.
     */
    val current: T
        get() {
            check(hasCurrent()) { "Attempted to access a range past the end." }
            return _current!!
        }

    /**
     * Returns the current value if the iterator is in range, else null.
     */
    fun currentOrNull(): T? =
        _current

    /**
     * Returns whether the iterator is in range.
     */
    fun hasCurrent(): Boolean =
        _current != null

    /**
     * Moves the iterator to the next element. If the iterator is currently on the last element, moves the iterator out of range.
     */
    fun moveNext() {
        _current = if (iter.hasNext()) { iter.next() } else null
    }

    /**
     * Attempts to move the iterator to the next element.
     * Returns the current element if the iterator is currently in range, else null.
     */
    fun tryConsume(): T? =
        _current?.also { moveNext() }

    /**
     * Moves the iterator to the next element. The iterator must be in range.
     * Returns the current element.
     */
    fun consume(): T {
        check(hasCurrent()) { "Attempted to move an iterator when it was already out of range." }
        return _current!!.also { moveNext() }
    }

    /**
     * Moves the iterator to the first element in [elems].
     * Once all elements in [elems] have been iterated through, the iterator returns to where it was before this call.
     */
    fun prepend(elems: Sequence<T>) {
        iter = (elems + toSequence()).iterator()
        moveNext()
    }

    /**
     * See [prepend]
     */
    fun prepend(elems: Iterable<T>) = prepend(elems.asSequence())

    /**
     * Returns a sequence representing all elements from the current one to the end of the range.
     * The current element is included as the first element of the returned sequence.
     */
    fun toSequence(): Sequence<T> {
        val rest = iter.asSequence()
        return if (_current != null) {
            sequenceOf(_current!!) + rest
        } else {
            rest
        }
    }
}

/**
 * Converts an iterator from being before each element to on each element.
 *
 * @see [CurrentIterator]
 */
fun<T> Iterator<T>.withCurrent() = CurrentIterator(this)