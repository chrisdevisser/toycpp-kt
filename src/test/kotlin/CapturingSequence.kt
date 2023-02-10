fun<T> Sequence<T>.capture(): Pair<Sequence<T>, List<T>> {
    val captured = mutableListOf<T>()
    val seqRet = this.onEach { captured.add(it) }
    return Pair(seqRet, captured)
}

fun String.capture() = asSequence().capture()