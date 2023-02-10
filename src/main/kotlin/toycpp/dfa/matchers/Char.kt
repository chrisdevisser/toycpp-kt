package toycpp.dfa.matchers

import toycpp.dfa.DfaDsl
import toycpp.dfa.InProgressNode
import toycpp.dfa.DfaNodeDsl
import toycpp.dfa.DfaNode.Id
import toycpp.dfa.InProgressDfa
import toycpp.encoding.escapeAsciiStringForHumans

/**
 * See [DfaNodeDsl.connects]
 */
internal fun<Output> connectChar(c: Char, id: Id, fromNode: InProgressNode<Output>) {
    require(c !in fromNode.edges) { "Attempted to add an edge for a character '${escapeAsciiStringForHumans(c.toString())}' that already has an edge. This would either be dead code or overwrite an edge. [ID='${fromNode.id}']" }
    fromNode.edges += c to id
}

// TODO: Consider requiring no existing node
/**
 * See [DfaNodeDsl.accepts]
 */
internal fun<Output> acceptChar(c: Char, value: Output, dfa: InProgressDfa<Output>, fromNode: InProgressNode<Output>) {
    val targetNode = dfa.getOrCreateNodeSequence(fromNode, c.toString())
    require(targetNode.acceptValue == null) { "The node (ID=${targetNode.id}) corresponding to '${escapeAsciiStringForHumans(c.toString())}' is already an accepting node. [ID='${fromNode.id}']" }
    targetNode.accept(value)
}