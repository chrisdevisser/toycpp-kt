package toycpp

import toycpp.diagnostics.Diagnostic
import toycpp.diagnostics.DiagnosticSeverity
import toycpp.diagnostics.DiagnosticSeverity.*
import toycpp.diagnostics.DiagnosticSink
import toycpp.location.SourceLocation

class DiagnosticReporter(val trace: Boolean) : DiagnosticSink {
    override fun handle(diag: Diagnostic, location: SourceLocation?) {
        val locationPrefix =
            if (location != null) {
                "$location "
            } else {
                ""
            }

        if (diag.severity != Trace || trace) {
            System.err.println("$locationPrefix${formatSeverity(diag.severity)}: ${diag.formatMessage()}")
        }
    }
}

private fun formatSeverity(s: DiagnosticSeverity) =
    when (s) {
        Trace -> "trace"
        Warning -> "warning"
        Error -> "error"
    }