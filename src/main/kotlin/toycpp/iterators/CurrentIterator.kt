package toycpp.iterators

/**
 * Acts similarly to a C++ iterator. When in range, [current] is not null and can be used repeatedly.
 * When out of range, [current] is null and no more iteration can be done.
 *
 * Logically, [current] always refers to an element until the end of the range.
 * However, in order to keep this behaviour while delaying the read of the next element, [lazyMoveNext] can be used.
 */
class CurrentIterator<T>(private var iter: Iterator<T>) {
    /**
     * Null at the end of the range or when the next advance is delayed (including at construction).
     */
    private var _currentOrDelayed: T? = null

    /**
     * Returns whether the iterator is in range.
     */
    fun hasCurrent(): Boolean =
        _currentOrDelayed != null || iter.hasNext()

    /**
     * Returns the current value. The iterator must be in range.
     */
    val current: T
        get() {
            check(hasCurrent()) { "Attempted to access a range past the end." }
            moveIfDelayed()
            return _currentOrDelayed!!
        }

    /**
     * Returns the current value if the iterator is in range, else null.
     */
    fun currentOrNull(): T? =
        if (hasCurrent()) current else null

    /**
     * Moves the iterator to the next element. If the iterator is currently on the last element, advances the iterator out of range.
     */
    fun moveNext() {
        check(hasCurrent()) { "Attempted to iterate past the end of a range." }
        moveIfDelayed()
        _currentOrDelayed = if (iter.hasNext()) { iter.next() } else null
    }

    /**
     * Lazily advances the iterator to the next element.
     * This move will take place the next time the current element is accessed or the iterator is advanced.
     */
    fun lazyMoveNext() {
        _currentOrDelayed = null
    }


    /**
     * Attempts to advance the iterator to the next element.
     * Returns the current element if the iterator is currently in range, else null.
     */
    fun tryConsume(): T? =
        if (hasCurrent()) consume() else null

    /**
     * Moves the iterator to the next element. The iterator must be in range.
     * Returns the current element.
     */
    fun consume(): T {
        check(hasCurrent()) { "Attempted to consume nothing." }
        return current.also { moveNext() }
    }

    /**
     * See [prepend]
     */
    fun prepend(elem: T) =
        prepend(sequenceOf(elem))

    /**
     * Moves the iterator to the first element in [elems].
     * Once all elements in [elems] have been iterated through, the iterator returns to where it was before this call.
     */
    fun prepend(elems: Sequence<T>) {
        iter = (elems + toSequence()).iterator()
        _currentOrDelayed = null
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
        return if (_currentOrDelayed != null) {
            sequenceOf(current) + iter.asSequence()
        } else {
            iter.asSequence() // Handles both end of range and delayed
        }
    }

    /**
     * Returns whether the next advance is delayed.
     * If delayed, any use of the current element needs to advance to that element first.
     */
    private fun isDelayed() =
        _currentOrDelayed == null && iter.hasNext()

    /**
     * If delayed, advances the iterator to the next element.
     */
    private fun moveIfDelayed() {
        if (isDelayed()) {
            _currentOrDelayed = iter.next()
        }
    }
}

/**
 * Converts an iterator from being before each element to on each element.
 *
 * @see [CurrentIterator]
 */
fun<T> Iterator<T>.withCurrent() = CurrentIterator(this)