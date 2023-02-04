package toycpp.extensions

import java.nio.file.Path
import java.nio.file.Paths

fun getCurrentDirectory(): Path =
    Paths.get(".").toAbsolutePath().normalize()