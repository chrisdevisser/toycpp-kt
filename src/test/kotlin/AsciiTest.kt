
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import toycpp.diagnostics.InvalidSourceEncoding
import toycpp.encoding.ensureBscsAscii

class AsciiTest {
    @ParameterizedTest
    @ValueSource(ints = [0x80, 0xFF, 0xAB, 0xCD])
    fun `Converting a non-ASCII byte issues an appropriate diagnostic`(badByte: Int) {
        val input = ubyteArrayOf(0x31u, 0x32u, 0x33u, 0x34u, badByte.toUByte(), 0x36u, 0x37u, 0x38u, 0x39u)
        val filename = "test-generated-filename"
        val diags = setNewCollectorAsSink()

        val ascii = ensureBscsAscii(filename, input)

        assertNull(ascii)

        assertTrue(diags.hasDiagnostic<InvalidSourceEncoding>())
        val (diag, loc) = diags.firstDiagnostic<InvalidSourceEncoding>()!!

        assertEquals(badByte.toUByte(), diag.badByteValue)
        assertEquals(filename, loc?.filename)
        assertEquals(1, loc?.line)
        assertEquals(5, loc?.col)
    }

    @ParameterizedTest
    @ValueSource(chars = ['@', '`', '$'])
    fun `Converting an ASCII byte that isn't in the BSCS issues an appropriate diagnostic`(badChar: Char) {
        val input = ubyteArrayOf(badChar.code.toUByte())
        val diags = setNewCollectorAsSink()

        val ascii = ensureBscsAscii("test-generated-filename", input)

        assertNull(ascii)

        assertTrue(diags.hasDiagnostic<InvalidSourceEncoding>())
        val (diag, _) = diags.firstDiagnostic<InvalidSourceEncoding>()!!

        assertEquals(badChar.code.toUByte(), diag.badByteValue)
    }

    @ParameterizedTest
    @ValueSource(strings = ["valid string", "", "01234567889", "abcdefghijklmnopqrstuvwxyz", "\n\t"])
    fun `Converting a valid ASCII string produces the same string`(inputStr: String) {
        val filename = "test-generated-filename"
        val diags = setNewCollectorAsSink()

        val ascii = ensureBscsAscii(filename, inputStr.toByteArray().asUByteArray())

        assertTrue(diags.hasNoDiagnostics())
        assertEquals(inputStr, ascii)
    }
}