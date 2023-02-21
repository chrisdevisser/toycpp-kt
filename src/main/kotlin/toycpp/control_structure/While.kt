package toycpp.control_structure

import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * Generates one value per iteration using [generate].
 * If [shouldUseValue] passes for the generated value, calls [block] with that value.
 *
 * Finishes iteration as soon as [shouldUseValue] returns false.
 */
inline fun<T> generateAndUseWhile(generate: () -> T, shouldUseValue: (T) -> Boolean, block: (T) -> Unit) {
    contract {
        callsInPlace(generate, InvocationKind.AT_LEAST_ONCE)
        callsInPlace(shouldUseValue) // generate could return early, so we can't guarantee a call.
        callsInPlace(block)
    }

    generateWhileAndUseWhile({ true }, generate, shouldUseValue, block)
}

/**
 * Generates one value per iteration using [generate].
 * If the generated value is not null, calls [block] with that value.
 *
 * Finishes iteration as soon as the generated value is null.
 */
inline fun<T> generateAndUseWhileNotNull(generate: () -> T?, block: (T) -> Unit) {
    contract {
        callsInPlace(generate, InvocationKind.AT_LEAST_ONCE)
        callsInPlace(block)
    }

    generateAndUseWhile(generate, { it != null }) {
        block(it!!)
    }
}

/**
 * Before each iteration, pre-emptively finishes iteration if [shouldGenerate] returns false.
 * Generates one value per iteration using [generate].
 * If [shouldUseValue] passes for the generated value, calls [block] with that value.
 *
 * Also finishes iteration as soon as [shouldUseValue] returns false.
 */
inline fun<T> generateWhileAndUseWhile(shouldGenerate: () -> Boolean, generate: () -> T, shouldUseValue: (T) -> Boolean, block: (T) -> Unit) {
    contract {
        callsInPlace(shouldGenerate, InvocationKind.AT_LEAST_ONCE)
        callsInPlace(generate)
        callsInPlace(shouldUseValue)
        callsInPlace(block)
    }

    while (shouldGenerate()) {
        val value = generate()
        if (!shouldUseValue(value)) return

        block(value)
    }
}