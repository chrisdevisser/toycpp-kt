package toycpp.dfa.matchers

import toycpp.dfa.DfaDsl
import toycpp.dfa.InProgressNode
import toycpp.dfa.DfaNode.Id
import toycpp.dfa.DfaNodeDsl
import toycpp.dfa.InProgressDfa
import toycpp.encoding.MaxAsciiChar
import toycpp.encoding.MinAsciiChar

/**
 * See [DfaNodeDsl.anySatisfying]
 */
class AnySatisfyingPredicate<Output>(private val pred: (Char) -> Boolean, private val dfa: InProgressDfa<Output>, private val node: InProgressNode<Output>) {
    /**
     * See [DfaNodeDsl.connects]
     */
    fun connect(id: Id) {
        val eligibleChars = (MinAsciiChar..MaxAsciiChar)
            .filter(pred)
            .filter { it !in node.edges }

        require(eligibleChars.isNotEmpty()) { "There must exist at least one character that satisfies the predicate and does not already have an associated edge. This is dead code. [ID='${node.id}']" }

        for (c in eligibleChars) {
            node.edges += c to id
        }
    }

    /**
     * See [DfaNodeDsl.accepts]
     */
    infix fun accept(value: Output) {
        val nodes = (MinAsciiChar..MaxAsciiChar).map { dfa.getOrCreateNodeSequence(node, it.toString()) }

        val nonAcceptingNodes = nodes.filter { it.acceptValue == null }
        require(nonAcceptingNodes.isNotEmpty()) { "There must exist at least one non-accepting node that can be reached via the given predicate. This is dead code. [ID='${node.id}']" }

        for (node in nodes) {
            node.accept(value)
        }
    }
}