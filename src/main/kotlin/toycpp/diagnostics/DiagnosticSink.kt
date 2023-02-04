package toycpp.diagnostics

import toycpp.location.SourceLocation

/**
 * Acts on all issued diagnostics. [handle] will be immediately called for each diagnostic.
 */
interface DiagnosticSink {
    /**
     * Called when a diagnostic is issued so that a central piece of code can act on that diagnostic,
     * e.g., display it to the user.
     *
     * While re-entrancy will work, it is not recommended to issue a diagnostic from this callback.
     * Infinite recursion is not guarded against.
     *
     * @param diag The diagnostic being issued. Treat this as an exception base class in its design.
     * @param location If not null, the best-effort location where this diagnostic was generated.
     *                 There is no guarantee that this location exists within the file, or even that the filename is a real file.
     *                 If null, there is no associated location.
     */
    fun handle(diag: Diagnostic, location: SourceLocation?)
}