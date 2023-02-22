package toycpp.dfa

import toycpp.control_structure.generateAndUseWhileNotNull
import toycpp.encoding.escapeAsciiStringForHumans
import toycpp.iterators.CurrentIterator
import toycpp.iterators.readWhileAndUseWhile
import toycpp.iterators.withCurrent

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

data class DfaTraversalResult<Output, Input>(
    val acceptValue: Output,
    val inputConsumed: List<Input>,
    val remainingInput: Sequence<Input>
)

/**
 * Starting at the beginning of the DFA, reads input and advances through the DFA until the furthest accepting node is reached.
 * This could be the start node.
 *
 * If successful, returns a result containing the accept value, the input consumed in this process, which could be empty, and the remaining input.
 * Returns null and backtracks the input if no accepting node was reached.
 */
fun<Output, Input> traverseDfaToFurthestAcceptingNode(input: Sequence<Input>, dfa: Dfa<Output>, proj: (Input) -> Char, onAcceptingNode: (Output) -> Unit): DfaTraversalResult<Output, Input>? {
    var currentNode = dfa.start
    val iter = input.iterator().withCurrent()

    val inputConsumed = buildList {
        generateAndUseWhileNotNull({ readToNextAcceptingNode(iter, currentNode, proj) }) { (nextGoodNode, read) ->
            addAll(read)
            currentNode = nextGoodNode
            onAcceptingNode(nextGoodNode.acceptValue!!)
        }
    }

    val kind = currentNode.acceptValue
    return if (kind != null) DfaTraversalResult(kind, inputConsumed, iter.toSequence()) else null
}

/**
 * Starting at the given DFA node, reads input and advances through the DFA until an accepting node is reached.
 *
 * Returns both the final node reached and the input consumed in this process.
 * Returns null and backtracks the input if no accepting node was reached.
 */
private fun<Output, Input> readToNextAcceptingNode(iter: CurrentIterator<Input>, startNode: DfaNode<Output>, proj: (Input) -> Char): Pair<DfaNode<Output>, List<Input>>? {
    var currentNode = startNode
    val inputConsumed = buildList {
        iter.readWhileAndUseWhile(shouldRead = { (isEmpty() || currentNode.acceptValue == null) }, shouldUseValue = { currentNode[proj(it)] != null }) {
            add(it)
            currentNode = currentNode[proj(it)]!!
        }
    }

    return if (currentNode.acceptValue != null && inputConsumed.isNotEmpty()) {
        Pair(currentNode, inputConsumed)
    } else {
        if (inputConsumed.isNotEmpty()) { // This check is just an optimization
            iter.prepend(inputConsumed)
        }
        null
    }
}