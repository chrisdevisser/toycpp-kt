package toycpp.dfa

import toycpp.encoding.escapeAsciiStringForHumans

/**
 * A DFA whose input is [Char] and whose accepting value output is [Output].
 *
 * The DFA has a single start node that begins traversal.
 * The traversal happens by accessing each node like a map.
 */
data class Dfa<Output>(val start: DfaNode<Output>)

data class DfaNode<Output>(
    val id: Id,
    val acceptValue: Output?,
) {
    private val outgoingEdges: Array<DfaNode<Output>?> = Array(128) { null }

    /**
     * A node ID. This can make debugging easier, but also allows for cycles within the DFA.
     */
    @JvmInline
    value class Id(val value: String) {
        /**
         * Returns the ID as a human-readable string.
         */
        override fun toString(): String {
            return escapeAsciiStringForHumans(value)
        }
    }

    operator fun get(transition: Char): DfaNode<Output>? =
        outgoingEdges[transition.code]

    internal operator fun set(transition: Char, next: DfaNode<Output>?) {
        outgoingEdges[transition.code] = next
    }
}