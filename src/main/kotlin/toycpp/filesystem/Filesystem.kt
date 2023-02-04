package toycpp.filesystem

import java.nio.file.Path
import toycpp.diagnostics.FileReadError
import toycpp.diagnostics.InvalidPath

/**
 * Encapsulates the filesystem in a way that can be changed or tested.
 */
interface Filesystem {
    /**
     * Converts the given string to a path. Issues an [InvalidPath] diagnostic and returns null if the path is not in a valid format.
     * This can happen when there is an invalid character.
     */
    fun getPath(s: String): Path?

    /**
     * Checks whether the given file exists. Do not assume it will be there when used later.
     */
    fun fileExists(path: Path): Boolean

    /**
     * Reads the contents of a file. Issues a [FileReadError] diagnostic and returns null if the file cannot be read.
     */
    fun readFileBytes(path: Path): ByteArray?
}