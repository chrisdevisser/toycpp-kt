import toycpp.diagnostics.InvalidPath
import toycpp.diagnostics.diag
import toycpp.filesystem.Filesystem
import java.nio.file.Path
import java.nio.file.Paths

/**
 * A filesystem that can be used to test desired outcomes deterministically.
 *
 * @constructor
 * @param invalidPathCharacters The set of invalid characters in a path. By default, all characters are valid.
 *
 * In order to use Path as a vocabulary type, the system's filesystem will include additional invalid characters,
 * so tests should use universal characters in the paths. The tradeoff to encapsulate that completely isn't worth it.
 *
 * @param files The set of files present. By default, there are no files.
 *
 * @param allFileContents The contents of every file present.
 */
internal class TestFilesystem(
    val invalidPathCharacters: Set<Char> = emptySet(),
    val files: Set<Path> = emptySet(),
    val allFileContents: String = ""
) : Filesystem {
    /**
     * Converts a string to a path. Uses [invalidPathCharacters] as the set of invalid characters. If the system doesn't
     * like the path, the thrown exception will be passed through, so tests should avoid non-universal characters in valid paths.
     */
    override fun getPath(s: String): Path? {
        val firstInvalidChar = s.find {it in invalidPathCharacters}
        return if (firstInvalidChar == null) {
            Paths.get(s)
        } else {
            diag(InvalidPath(s, firstInvalidChar), location = null)
            null
        }
    }

    /**
     * Checks whether the given path is in [files].
     */
    override fun fileExists(path: Path) = path in files

    /**
     * Returns the contents specified in [allFileContents].
     */
    override fun readFileBytes(path: Path) = allFileContents.toByteArray()
}