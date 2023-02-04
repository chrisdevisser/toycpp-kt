import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import toycpp.diagnostics.FileNotFound
import toycpp.diagnostics.InvalidPath
import toycpp.diagnostics.NoSourceFiles
import toycpp.parseProgramOptions
import java.nio.file.Paths

class ProgramOptionsTest {
    @Test
    fun `Supplying no source files errors with an appropriate diagnostic`() {
        val diags = setNewCollectorAsSink()
        val fs = TestFilesystem()

        val options = parseProgramOptions(arrayOf(), fs)

        assertNull(options)
        assertTrue(diags.hasDiagnostic<NoSourceFiles>())
    }

    @Test
    fun `Supplying invalid paths errors with an appropriate diagnostic`() {
        val diags = setNewCollectorAsSink()
        val fs = TestFilesystem(invalidPathCharacters = setOf('|'))
        val badPath = "test-file|abcde"

        val options = parseProgramOptions(arrayOf(badPath), fs)

        assertNull(options)

        assertTrue(diags.hasDiagnostic<InvalidPath>())
        val (diag, loc) = diags.firstDiagnostic<InvalidPath>()!!

        assertEquals(badPath, diag.attemptedPath)
        assertEquals('|', diag.firstBadChar)
        assertNull(loc)
    }

    @Test
    fun `Supplying a missing file errors with an appropriate diagnostic`() {
        val diags = setNewCollectorAsSink()
        val fs = TestFilesystem()
        val missingFile = "abc.cpp"

        val options = parseProgramOptions(arrayOf(missingFile), fs)

        assertNull(options)

        assertTrue(diags.hasDiagnostic<FileNotFound>())
        val (diag, loc) = diags.firstDiagnostic<FileNotFound>()!!

        assertEquals(Paths.get(missingFile), diag.path)
        assertNull(loc)
    }

    @Test
    fun `Supplying valid files produces no errors`() {
        val diags = setNewCollectorAsSink()
        val paths = arrayOf("abc.cpp", "def.cpp")
        val fs = TestFilesystem(files = paths.map(Paths::get).toSet())

        val options = parseProgramOptions(paths, fs)

        assertIterableEquals(paths.asIterable(), options?.sourcePaths?.map {it.toString()})
        assertTrue(diags.hasNoDiagnostics())
    }
}