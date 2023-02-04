package toycpp.sequences

class SinglePassSequence<T>(seq: Sequence<T>) : Sequence<T> {
    private var _seq: Sequence<T>? = seq

    override fun iterator(): Iterator<T> =
        _seq?.iterator() ?: throw RuntimeException("Multiple iteration over single-pass sequence")

    fun move(): SinglePassSequence<T> {
        val ret =  SinglePassSequence(_seq ?: throw RuntimeException("Moving from dead single-pass sequence"))
        _seq = null
        return ret
    }
}