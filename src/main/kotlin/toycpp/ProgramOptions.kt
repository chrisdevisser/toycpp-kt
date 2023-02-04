package toycpp

import toycpp.diagnostics.FileNotFound
import toycpp.diagnostics.NoSourceFiles
import toycpp.diagnostics.diag
import toycpp.extensions.liftAnyNull
import toycpp.filesystem.Filesystem
import java.nio.file.Path

data class ProgramOptions(
    val sourcePaths: List<Path>
)

fun parseProgramOptions(args: Array<String>, filesystem: Filesystem): ProgramOptions? {
    if (args.isEmpty()) {
        diag(NoSourceFiles(), location = null)
        return null
    }

    val paths = args.map {
        filesystem.getPath(it)?.let { path ->
            if (filesystem.fileExists(path)) {
                path
            } else {
                diag(FileNotFound(path), location = null)
                null
            }
        }
    }

    return paths.liftAnyNull()?.let {ProgramOptions(it)}
}