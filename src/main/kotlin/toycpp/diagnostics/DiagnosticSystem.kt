package toycpp.diagnostics

import toycpp.diagnostics.DiagnosticSeverity.Error
import toycpp.diagnostics.DiagnosticSeverity.Trace
import toycpp.diagnostics.DiagnosticSystem.Companion.setDiagnosticSink
import toycpp.location.SourceLocation

/**
 * Represents the global state of the diagnostics. A [sink][DiagnosticSink] must be set via [setDiagnosticSink] before issuing a diagnostic.
 * Once it is set, it should not change. Then [diag] may be called to issue a diagnostic.
 *
 * While this is a singleton, the negative effects are limited by offloading the behaviour to a configurable sink and by
 * removing the use of the global state outside of the beginning and end of execution. The former makes this singleton
 * perfectly testable, its purpose being for code accessibility, and the latter lowers the impact of global state by isolating it.
 */
class DiagnosticSystem {
    companion object {
        internal var sink: DiagnosticSink? = null
            private set

        /**
         * True if at least one error diagnostic has been issued over the lifetime of the program.
         * If you (future me) use this flag before the end of the program, you will have 10 years of bad luck.
         */
        var hasErrorBeenIssued: Boolean = false
            internal set

        /**
         * Sets the sink that will be used to handle all diagnostics.
         *
         * Do not set the sink again once it has been set.
         */
        fun setDiagnosticSink(s: DiagnosticSink) {
            sink = s
        }
    }
}

/**
 * Issues a [diagnostic][Diagnostic].
 *
 * Requires a global sink to be set with [DiagnosticSystem.setDiagnosticSink] beforehand.
 *
 * @param d A specific diagnostic that will be handled by the installed sink
 * @param location The best-effort location within a source file where this diagnostic occurred
 */
fun diag(d: Diagnostic, location: SourceLocation?) {
    checkNotNull(DiagnosticSystem.sink) { "The diagnostic sink was not set." }

    if (d.severity == Error) {
        DiagnosticSystem.hasErrorBeenIssued = true
    }

    DiagnosticSystem.sink?.handle(d, location)
}

fun trace(message: String, location: SourceLocation? = null) {
    diag(CustomDiagnostic(Trace, message), location)
}