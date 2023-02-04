package toycpp

import toycpp.diagnostics.FileReadError
import toycpp.diagnostics.InvalidPath
import toycpp.diagnostics.diag
import toycpp.filesystem.Filesystem
import java.io.IOException
import java.lang.Exception
import java.nio.file.Files
import java.nio.file.InvalidPathException
import java.nio.file.Path
import java.nio.file.Paths

/**
 * A filesystem based on the system's filesystem, converting exceptions to diagnostics.
 */
class SystemFilesystem : Filesystem {
    override fun getPath(s: String): Path? =
        try {
            Paths.get(s)
        } catch (e: InvalidPathException) {
            diag(InvalidPath(s, if (e.index != -1) s[e.index] else null), location = null)
            null
        }

    override fun fileExists(path: Path): Boolean {
        return Files.exists(path)
    }

    override fun readFileBytes(path: Path): ByteArray? =
        try {
            Files.readAllBytes(path)
        } catch (e: Exception) {
            when (e) {
                is IOException -> diag(FileReadError(path, e.message ?: "unknown"), location = null)
                else -> throw e
            }
            null
        }

}