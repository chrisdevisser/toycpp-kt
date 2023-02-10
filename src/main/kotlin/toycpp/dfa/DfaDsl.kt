package toycpp.dfa

import toycpp.graph.depthCover

/**
 * Represents a unique ID given to nodes.
 */
private typealias Id = DfaNode.Id

/**
 * See [dfa]
 */
class DfaDsl<Output> {


    internal val dfa = InProgressDfa<Output>()

}