import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import toycpp.diagnostics.*
import toycpp.diagnostics.DiagnosticSeverity.Error
import toycpp.diagnostics.DiagnosticSeverity.Warning
import toycpp.location.SourceLocation

internal data class DiagnosticWithLocation<D>(
    val diag: D,
    val loc: SourceLocation?
)

/**
 * Retains all diagnostics issued and allows for checking them against known diagnostics information.
 */
internal class DiagnosticCollector : DiagnosticSink {
    val diags = mutableListOf<DiagnosticWithLocation<Diagnostic>>()

    override fun handle(diag: Diagnostic, location: SourceLocation?) {
        diags += DiagnosticWithLocation(diag, location)
    }

    val issuedWarnings get() = diags.filter { it.diag.severity == Warning }
    val issuedErrors get() = diags.filter { it.diag.severity == Error }

    val issuedCount get() = diags.size
    val issuedWarningCount get() = issuedWarnings.count()
    val issuedErrorCount get() = issuedErrors.count()

    fun hasNoDiagnostics() = diags.isEmpty()
    inline fun<reified D> hasDiagnostic() = diags.any { it.diag is D }
    inline fun<reified D> firstDiagnostic() =
        diags.firstOrNull { it.diag is D }?.let { DiagnosticWithLocation(it.diag as D, it.loc) }
}

/**
 * Creates a new diagnostic collector and sets it as the diagnostic sink.
 *
 * It's important that the sink doesn't change in the actual compiler, but it's useful to change it between each test run.
 */
internal fun setNewCollectorAsSink() = DiagnosticCollector().also { DiagnosticSystem.setDiagnosticSink(it) }

class DiagnosticCollectorTest {
    val testWarning = CustomDiagnostic(Warning, "test warning")
    val testError = CustomDiagnostic(Error, "test error")

    @Test
    fun `Issuing no diagnostics produces counts of 0`() {
        val diags = setNewCollectorAsSink()

        assertEquals(0, diags.issuedCount)
        assertEquals(0, diags.issuedErrorCount)
        assertEquals(0, diags.issuedWarningCount)

        assertFalse(diags.hasDiagnostic<CustomDiagnostic>())
    }

    @Test
    fun `Issuing a warning retains that warning`() {
        val diags = setNewCollectorAsSink()
        val diagLoc = SourceLocation("test-generated-filename.cpp", 5, 6)

        diag(testWarning, diagLoc)

        assertEquals(1, diags.issuedCount)
        assertEquals(1, diags.issuedWarningCount)
        assertEquals(0, diags.issuedErrorCount)

        assertIterableEquals(listOf(DiagnosticWithLocation(testWarning, diagLoc)), diags.issuedWarnings)
        assertIterableEquals(emptyList<Diagnostic>(), diags.issuedErrors)
        assertTrue(diags.hasDiagnostic<CustomDiagnostic>())
    }

    @Test
    fun `Issuing an error retains that error`() {
        val diags = setNewCollectorAsSink()
        val diagLoc = SourceLocation("test-generated-filename.cpp", 5, 6)

        diag(testError, diagLoc)

        assertEquals(1, diags.issuedCount)
        assertEquals(0, diags.issuedWarningCount)
        assertEquals(1, diags.issuedErrorCount)

        assertIterableEquals(emptyList<Diagnostic>(), diags.issuedWarnings)
        assertIterableEquals(listOf(DiagnosticWithLocation(testError, diagLoc)), diags.issuedErrors)
        assertTrue(diags.hasDiagnostic<CustomDiagnostic>())
    }

    @Test
    fun `Issuing a warning and an error retains both in order`() {
        val diags = setNewCollectorAsSink()
        val warningLoc = SourceLocation("test-generated-warning-filename.cpp", 5, 6)
        val errorLoc = SourceLocation("test-generated-error-filename.cpp", 7, 8)

        diag(testWarning, warningLoc)
        diag(testError, errorLoc)

        assertEquals(2, diags.issuedCount)
        assertEquals(1, diags.issuedWarningCount)
        assertEquals(1, diags.issuedErrorCount)

        assertIterableEquals(listOf(DiagnosticWithLocation(testWarning, warningLoc)), diags.issuedWarnings)
        assertIterableEquals(listOf(DiagnosticWithLocation(testError, errorLoc)), diags.issuedErrors)
        assertTrue(diags.hasDiagnostic<CustomDiagnostic>())
    }
}