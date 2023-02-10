package toycpp.dfa

import toycpp.dfa.DfaNode.Id

data class InProgressNode<Output>(
    internal val id: Id,
    internal var acceptValue: Output?,
    internal val edges: MutableMap<Char, Id> = mutableMapOf()
) {
    internal fun accept(value: Output) {
        acceptValue = value
    }

    /**
     * Compares two objects based only on their ID.
     */
    override fun equals(other: Any?): Boolean =
        other is InProgressNode<*> && id == other.id

    /**
     * Generates a hashcode based only on the ID.
     */
    override fun hashCode(): Int {
        return id.hashCode()
    }
}