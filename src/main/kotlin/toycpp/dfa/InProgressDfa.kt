package toycpp.dfa

import toycpp.dfa.DfaNode.Id
import toycpp.graph.breadthCover

class InProgressDfa<Output> {
    /**
     * The start node of the DFA
     */
    private val start = Id("start")

    /**
     * Keeps track of all nodes created so far, accessed by their ID.
     */
    private val nodeById = mutableMapOf<Id, InProgressNode<Output>>()

    /**
     * Keeps track of a node by its ID.
     *
     * A new node's ID must not match any existing IDs.
     */
    internal fun manageNode(node: InProgressNode<Output>): InProgressNode<Output> {
        val existingNode = nodeById[node.id]
        check(existingNode == null || existingNode == node) { "Duplicate ID '${node.id}' added in DFA DSL." }

        nodeById += node.id to node
        return node
    }

    /**
     * Creates a new node with the given ID and accept value and manages it.
     */
    private fun createNode(id: Id, acceptValue: Output? = null): InProgressNode<Output> {
        val node = InProgressNode(id, acceptValue)
        return manageNode(node)
    }

    /**
     * Traverses the interim DFA according to the given sequence of characters, creating nodes as needed.
     *
     * If any nodes along the way already has an ID, it must already be created.
     */
    internal fun getOrCreateNodeSequence(startNode: InProgressNode<Output>, seq: String): InProgressNode<Output> {
        val startId = startNode.id
        var currentNode = startNode
        var charsSoFar = ""

        fun makeId(): String {
            val seqPiece = "'$charsSoFar'"
            return if (startId == start) seqPiece else "${startId.value} + $seqPiece"
        }

        for (c in seq) {
            charsSoFar += c
            val nextId = currentNode.edges.getOrPut(c) { createNode(Id(makeId())).id }
            val nextNode = nodeById[nextId]
            checkNotNull(nextNode) { "The node with ID '$nextId' was referenced, but has not yet been created. [ID='$startId']" }
            currentNode = nextNode
        }

        return currentNode
    }

    internal fun createNestedCopies(times: Int, startNode: InProgressNode<Output>, sampleNode: InProgressNode<Output>) {
        check(times > 0) { "Tried to nest a DFA node $times (< 1) times. [ID='${startNode.id}']" }

        var currentNode = startNode
        repeat(times) { nestingLevel ->
            val nextNode = createNode(Id("${startNode.id.value} + ${"_".repeat(nestingLevel + 1)}"))
            for ((c, _) in sampleNode.edges) {
                currentNode.edges += c to nextNode.id
            }

            currentNode = nextNode
        }

        for ((c, id) in sampleNode.edges) {
            currentNode.edges += c to id
        }
    }

    /**
     * Builds a proper DFA from the DSL.
     */
    internal fun build(): Dfa<Output> {
        check(nodeById.values.any { it.acceptValue != null }) { "The DFA DSL must have at least one accepting node." }

        val finalNodes = mutableMapOf<Id, DfaNode<Output>>()

        fun getOrCreateNode(id: Id): DfaNode<Output> {
            check(id in nodeById) { "Tried to get or create DFA node '$id' but it wasn't found" }
            val node = nodeById[id]!!

            finalNodes.putIfAbsent(id, DfaNode(id, node.acceptValue))
            return finalNodes[id]!!
        }

        // First get a hold of all the nodes reachable from start
        val finalStart = getOrCreateNode(start)
        val nodes = breadthCover(nodeById[start]!!,
            getNeighbors = {node ->
                node.edges.values.map { id ->
                    check(id in nodeById) { "DFA node '$id' was referenced from another node but never created" }
                    nodeById[id]!!
                }
            }
        )

        // Create each node in the final DFA and add each transition
        for (node in nodes) {
            val finalNode = getOrCreateNode(node.id)
            for ((transition, nextId) in node.edges) {
                finalNode[transition] = getOrCreateNode(nextId)
            }
        }

        return Dfa(finalStart)
    }
}