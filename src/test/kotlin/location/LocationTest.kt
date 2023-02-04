package location

import assertSequenceEquals
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import toycpp.location.SourceChar
import toycpp.location.SourceLocation
import toycpp.location.withLocations

class LocationTest {
    @Test
    fun `An empty sequence with location tracked is an empty sequence with a 1,1 location`() {
        val input = emptySequence<Char>()
        val filename = "test-generated-filename.cpp"

        val (tracked, tracker) = input.withLocations(filename)

        assertSequenceEquals(emptySequence<SourceChar>(), tracked)
        assertEquals(SourceLocation(filename, 1, 1), tracker.currentLocation)
    }

    @Test
    fun `A sequence with location tracked gives correct locations`() {
        val input = "123\n\n4"
        val filename = "test-generated-filename.cpp"

        val (tracked, tracker) = input.asSequence().withLocations(filename)

        val expectedLineColChars = listOf(
            Triple(1, 1, '1'), Triple(1, 2, '2'), Triple(1, 3, '3'), Triple(1, 4, '\n'),
            Triple(2, 1, '\n'),
            Triple(3, 1, '4')
        )

        val expected = expectedLineColChars.map { (line, col, c) -> SourceChar(c, SourceLocation(filename, line, col)) }.toList()

        assertIterableEquals(expected, tracked.toList())
        assertEquals(SourceLocation(filename, 3, 2), tracker.currentLocation)
    }

    @Test
    fun `A lazily tracked location starts at 1,1 before characters are processed`() {
        val input = "line 1\nline 2"

        val (_, tracker) = input.asSequence().withLocations("test-generated-filename.cpp")
        val loc = tracker.currentLocation

        assertEquals(1, loc.line)
        assertEquals(1, loc.col)
    }

    @Test
    fun `A lazily tracked location has a strictly increasing location as each character is processed`() {
        val input = "line 1\n\nline 3\n"
        val filename = "test-generated-filename.cpp"

        val (tracked, tracker) = input.asSequence().withLocations(filename)

        // The first location remains (1,1) through one character.
        // This tested property doesn't care about the relation between the initial location and the first character (which should be the same).
        var prevLoc = SourceLocation(filename, 0, 0)

        for (c in tracked) {
            val loc = tracker.currentLocation
            assertTrue(SourceLocation.fileInsensitiveComparator.compare(loc, prevLoc) > 0)
            prevLoc = loc
        }
    }

    @Test
    fun `Processing one character of a tracked location increments the location by 1`() {
        val input = "line 1\nline 2"

        val (tracked, tracker) = input.asSequence().withLocations("test-generated-filename.cpp")
        val iter = tracked.iterator()

        repeat(2) {
            iter.next()
        }
        val loc = tracker.currentLocation

        assertEquals(1, loc.line)
        assertEquals(2, loc.col)
    }

    @Test
    fun `Forcing a new location continues from there upon processing the next character`() {
        val input = "line 1\nline 2"
        val filename = "test-generated-filename.cpp"
        val newFilename = "new-file.cpp"

        val (tracked, tracker) = input.asSequence().withLocations(filename)
        val iter = tracked.iterator()

        tracker.forceNewLineNumber(5)
        iter.next()
        val loc = tracker.currentLocation

        assertEquals(5, loc.line)
        assertEquals(1, loc.col)

        tracker.forceNewLineNumberAndFile(10, newFilename)
        iter.next()

        assertEquals(SourceLocation(newFilename, 10, 1), tracker.currentLocation)
    }
}