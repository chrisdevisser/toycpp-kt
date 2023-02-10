package toycpp.control_structure

inline fun<T> repeatWhile(getNextValue: () -> T, cond: (T) -> Boolean, block: (T) -> Unit) {
    while (true) {
        val value = getNextValue()
        if (!cond(value)) return

        block(value)
    }
}

inline fun<T> doWhile(getNextValue: () -> T, cond: (T) -> Boolean, block: (T) -> Unit) {
    while (true) {
        val value = getNextValue()
        block(value)

        if (!cond(value)) return
    }
}

inline fun<T> repeatWhileNotNull(getNextValue: () -> T?, block: (T) -> Unit) {
    repeatWhile(getNextValue, { it != null }) {
        block(it!!)
    }
}