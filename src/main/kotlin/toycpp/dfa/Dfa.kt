package toycpp.dfa

import toycpp.encoding.escapeAsciiStringForHumans

/**
 * A DFA whose input is [Char] and whose accepting value output is [Output].
 *
 * The DFA has a single start node that begins traversal.
 * This traversal can be done by feeding characters with [feed].
 */
data class Dfa<out Output>(
    private val start: DfaNode<Output>
) {
    private var current = start

    fun reset() {
        current = start
    }
}

data class DfaNode<out Output>(
    val id: Id,
    val acceptValue: Output?,
    private val outgoingEdges: List<DfaNode<Output>?>
) {
    /**
     * A node ID. This can make debugging easier, but also allows for cycles within the DFA.
     */
    @JvmInline
    value class Id(val id: String) {
        /**
         * Returns the ID as a human-readable string.
         */
        override fun toString(): String {
            return escapeAsciiStringForHumans(id)
        }
    }

    operator fun get(transition: Char): DfaNode<Output>? =
        outgoingEdges[transition.code]
}