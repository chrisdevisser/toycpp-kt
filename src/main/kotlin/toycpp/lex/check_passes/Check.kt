package toycpp.lex.check_passes

import toycpp.lex.PpToken
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

fun runCheck(tokens: Iterable<PpToken>, predicate: (PpToken) -> Boolean, action: (PpToken) -> Unit): Boolean {
    contract {
        callsInPlace(predicate, InvocationKind.AT_LEAST_ONCE)
        callsInPlace(action)
    }

    return tokens.fold(true) { valid, token ->
        if (predicate(token)) {
            action(token)
            false
        } else {
            valid
        }
    }
}

fun runAllChecks(tokens: Iterable<PpToken>, vararg checks: (Iterable<PpToken>) -> Boolean): Boolean =
    checks.all { it(tokens) }