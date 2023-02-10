package toycpp.dfa.matchers

import toycpp.dfa.DfaDsl
import toycpp.dfa.InProgressNode
import toycpp.dfa.DfaNode.Id
import toycpp.dfa.DfaNodeDsl
import toycpp.dfa.InProgressDfa
import toycpp.encoding.escapeAsciiStringForHumans

/**
 * See [DfaNodeDsl.seq]
 */
class Seq<Output>(private val seq: String, private val dfa: InProgressDfa<Output>, private val node: InProgressNode<Output>) {
    /**
     * See [DfaNodeDsl.connects]
     */
    internal fun connect(id: Id) {
        require(seq.isNotEmpty()) { "The sequence must not be empty." }

        val nextToLastNode = dfa.getOrCreateNodeSequence(node, seq.dropLast(1))

        val lastChar = seq.last()
        require(lastChar !in nextToLastNode.edges) { "The next to last node (ID='${nextToLastNode.id}') in the sequence ${escapeAsciiStringForHumans(seq)} already has an edge corresponding to the final character in the sequence. [ID='${node.id}']" }

        nextToLastNode.edges += lastChar to id
    }

    /**
     * See [DfaNodeDsl.accepts]
     */
    internal fun accept(value: Output) {
        val node = dfa.getOrCreateNodeSequence(node, seq)
        require(node.acceptValue == null) { "The node (ID='${node.id}') corresponding to the sequence '${escapeAsciiStringForHumans(seq)}' is already accepting. [ID='${node.id}']" }
        node.accept(value)
    }
}