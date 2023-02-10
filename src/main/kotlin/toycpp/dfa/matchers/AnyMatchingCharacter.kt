package toycpp.dfa.matchers

import toycpp.dfa.DfaDsl
import toycpp.dfa.InProgressNode
import toycpp.dfa.DfaNode.Id
import toycpp.dfa.DfaNodeDsl
import toycpp.dfa.InProgressDfa
import toycpp.encoding.escapeAsciiStringForHumans

/**
 * See [DfaNodeDsl.any]
 */
data class AnyMatchingCharacter<Output>(private val options: String, private val dfa: InProgressDfa<Output>, private val node: InProgressNode<Output>) {
    /**
     * See [DfaNodeDsl.connects]
     */
    internal fun connect(id: Id) {
        val charsWithEdge = options.filter { it in node.edges }
        require(charsWithEdge.isEmpty()) { "The following characters already have an existing edge: '${escapeAsciiStringForHumans(charsWithEdge)}' [ID='${node.id}']" }

        for (c in options) {
            connectChar(c, id, node)
        }
    }

    /**
     * See [DfaNodeDsl.accepts]
     */
    internal fun accept(value: Output) {
        val nodes = options.map { dfa.getOrCreateNodeSequence(node, it.toString()) }

        val acceptingNodes = nodes.filter { it.acceptValue != null }
        require(acceptingNodes.isEmpty()) { "The nodes with the following IDs are already accepting nodes: ${acceptingNodes.joinToString { "'${it.id}'" }} [ID='${node.id}']" }

        for (node in nodes) {
            node.accept(value)
        }
    }

    /**
     * Removes the given options from the list of characters.
     */
    infix fun except(exceptions: String) = copy(options = options.filterNot { it in exceptions })
}