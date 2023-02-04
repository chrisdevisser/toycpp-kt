package toycpp.location

/**
 * Returns the same sequence of characters with their locations.
 *
 * The location returned by the tracker represents the beginning of the accompanied character.
 *
 * The returned sequence should be iterated through only once. Reiteration will cause future locations to be incorrect.
 *
 * @param filename The name of the file the returned location tracker should be attached to
 * @return A pair containing a sequence of characters with location attached and the location tracker used for generating these locations
 */
fun Sequence<Char>.withLocations(filename: String): Pair<Sequence<SourceChar>, LocationTracker> {
    var locationForceChanged = false
    val locationTracker = LocationTracker(filename, onLocationForceChanged = { locationForceChanged = true })
    val seq = sequence {
        for (c in this@withLocations) {
            yield(SourceChar(c, locationTracker.currentLocation))

            // If the location is force-changed, the character that triggered this change should not factor into the location of the next character.
            // Rather, the location of the next character is given directly by the new location provided.
            if (!locationForceChanged) locationTracker.feed(c)
            locationForceChanged = false
        }
    }

    return Pair(seq, locationTracker)
}