package toycpp.dfa

import toycpp.dfa.DfaNode.Id
import toycpp.dfa.matchers.*
import toycpp.dfa.matchers.connectChar
import toycpp.encoding.MaxAsciiChar
import toycpp.encoding.MinAsciiChar
import toycpp.encoding.escapeAsciiStringForHumans

/**
 * See [DfaDsl.node]
 */
class DfaNodeDsl<Output> internal constructor(
    private val dfa: InProgressDfa<Output>,
    id: Id,
    acceptValue: Output? = null
) {
    // The DFA DSL works in 3 steps:
    // 1. The outer block is designated as the start node.
    // 2. Within a node DSL, transitions are described, leading to accepting nodes that emit a value.
    // 3. Once everything has been described, the entire DSL is built into a real DFA.
    //
    // One supported feature is using IDs in place of actual nodes. The corresponding nodes must then be created at some point in the future.
    // This hides the internal data apart from the provided API and allows for breaking node definitions out into later code instead of nesting them.
    // For the implementation, in-progress nodes are stored using NodeData, which has mutable members. These are mapped by ID in nodeDataById.
    // Because IDs can be used before their corresponding node is created, this storage cannot be assumed to have a node just because there's an ID for it.
    //
    // Each node can change after initially being built. This allows for things like `seq("abc") accepts Value.Foo` when the a, b, and c nodes already exist.
    // This makes the DSL flatter and allows organizing things more easily.

    /**
     * Refers to the node currently being built.
     */
    val selfId: Id = id

    /**
     * The actual node being built.
     */
    private val self: InProgressNode<Output> = InProgressNode(selfId, acceptValue)

    /**
     * A shortcut for the edges of the node being built.
     */
    private val edges = self.edges

    /**
     * Creates a new node with the given ID using a DSL.
     *
     * Matchers in the DSL are conceptually tried in lexical order.
     *
     * Example:
     *
     * ```
     * node {
     *     seq("Al") connects node("Starts with Al") {
     *         seq("ex") connects acceptingNode("Person", Person)
     *         anythingSatisfying(isVowel) connects acceptingNode("Al + vowel", AlVowels) // Ala, Ali, Alo, Alu
     *         anythingElse() connects acceptingNode("Al word", AlWord) // Al_ not covered above
     *     }
     * }
     * ```
     */
    fun node(id: String, block: DfaNodeDsl<Output>.() -> Unit): Id =
        createNode(Id(id), acceptValue = null, block).id

    /**
     * Creates a new accepting node with the given ID.
     *
     * @see [node]
     */
    fun acceptingNode(id: String, value: Output, block: DfaNodeDsl<Output>.() -> Unit = {}): Id =
        createNode(Id(id), value, block).id

    /**
     * Accepts any input that fulfills the given predicate. This predicate must be pure. It will be called immediately, not later.
     */
    fun anySatisfying(pred: (Char) -> Boolean) = AnySatisfyingPredicate(pred, dfa, self)

    /**
     * Accepts input that matches any of the given values.
     */
    fun any(options: String) = AnyMatchingCharacter(options, dfa, self)

    /**
     * Accepts a sequence of input, each matching the next value in the given values.
     *
     * The given sequence must not be empty.
     */
    fun seq(sequence: String) = Seq(sequence, dfa, self)

    /**
     * Nests the given node block the given number of times.
     */
    fun nest(times: Int, block: DfaNodeDsl<Output>.() -> Unit) {
        val sampleNode = DfaNodeDsl(dfa, Id("sample"))
        sampleNode.block()
        dfa.createNestedCopies(times, self, sampleNode.toNodeData())
    }

    /**
     * Creates an edge that transitions to the node with the given ID for any input.
     *
     * There must exist at least one character that does not already have an associated edge.
     */
    fun anythingElse(): AnySatisfyingPredicate<Output> {
        val eligibleChars = (Char(0)..Char(127))
            .filter { it !in edges }

        require(eligibleChars.isNotEmpty()) { "There must exist at least one character that does not already have an associated edge. This is dead code. [ID='$selfId']" }

        return anySatisfying { true }
    }

    /**
     * Creates an edge that transitions to the node with the given ID if the input does not match any of the given values.
     *
     * There must exist at least one non-excluded character that does not already have an associated edge.
     */
    fun anyExcept(exclusions: String): AnySatisfyingPredicate<Output> {
        val eligibleChars = (MinAsciiChar..MaxAsciiChar)
            .filter {it !in exclusions }
            .filter { it !in edges }

        require(eligibleChars.isNotEmpty()) { "There must exist at least one non-excluded character that does not already have an associated edge. This is dead code. [ID='$selfId', exclusions='${escapeAsciiStringForHumans(exclusions)}]" }

        return anySatisfying { it !in exclusions }
    }

    /**
     * Creates an edge that transitions to the node with the given ID if the input matches the given value.
     *
     * The character must not already have an associated edge.
     */
    infix fun Char.connects(id: Id) = connectChar(this, id, self)

    /**
     * Marks the node reached via the given character as accepting with the given value.
     *
     * This node must not already be an accepting node.
     * If the node already has an ID, it must already be created.
     * Otherwise, if the node does not yet exist, it will be created.
     */
    infix fun Char.accepts(value: Output) = acceptChar(this, value, dfa, self)

    /**
     * Creates an edge that transitions to the node with the given ID if the input fulfills the given predicate.
     *
     * Only characters that do not already have an associated edge are eligible.
     * There must exist at least one eligible character.
     */
    infix fun AnySatisfyingPredicate<Output>.connects(id: Id) = connect(id)

    /**
     * Marks all nodes reached by characters that fulfill the given predicate as accepting with the given value.
     *
     * Only nodes that are not already accepting will change their accepting node state.
     * There must exist at least one non-accepting node.
     * If any of these nodes already has an ID, it must already be created.
     * Otherwise, if any node does not yet exist, it will be created.
     */
    infix fun AnySatisfyingPredicate<Output>.accepts(value: Output) = accept(value)

    /**
     * Creates an edge that transitions to the node with the given ID if the input matches any of the given values.
     *
     * All given characters must not have an existing edge.
     */
    infix fun AnyMatchingCharacter<Output>.connects(id: Id) = connect(id)

    /**
     * Marks all nodes reached by the given characters as accepting with the given value.
     *
     * Each node must not already be accepting.
     * If any of these nodes already has an ID, it must already be created.
     * Otherwise, if any node does not yet exist, it will be created.
     */
    infix fun AnyMatchingCharacter<Output>.accepts(value: Output) = accept(value)

    /**
     * Creates a series of nodes, the last of which transitions to the node with the given ID if each input matches the next value in the given sequence.
     *
     * Any nodes along the way that already exist are reused. These nodes must not be IDs that have not yet been created.
     * The final node must not already have an edge corresponding to the final character in the sequence.
     * The sequence must not be empty.
     */
    infix fun Seq<Output>.connects(id: Id) = connect(id)

    /**
     * Marks the node reached by the given sequence of characters as accepting with the given value.
     *
     * If this node or any node along the way already has an ID, it must already be created.
     * Otherwise, if any node does not yet exist, it will be created.
     */
    infix fun Seq<Output>.accepts(value: Output) = accept(value)

    /**
     * Converts the DSL into a proper interim node.
     */
    internal fun toNodeData(): InProgressNode<Output> = self

    /**
     * Common implementation for creating new nodes.
     */
    private fun createNode(id: Id, acceptValue: Output?, block: DfaNodeDsl<Output>.() -> Unit): InProgressNode<Output> {
        val nodeBuilder = DfaNodeDsl(dfa, id, acceptValue)
        nodeBuilder.block()

        val node = nodeBuilder.toNodeData()
        return dfa.manageNode(node)
    }
}

/**
 * Creates a DFA using a DSL.
 *
 * Example:
 * ```
 * val myDfa = dfa {
 *     start {
 *         // Nodes go here
 *     }
 * }
 * ```
 */
fun<Output> dfa(block: DfaNodeDsl<Output>.() -> Unit): Dfa<Output> {
    val dfa = InProgressDfa<Output>()

    val nodeBuilder = DfaNodeDsl(dfa, Id("start"), null)
    dfa.manageNode(nodeBuilder.toNodeData())
    nodeBuilder.block()

    return dfa.build()
}