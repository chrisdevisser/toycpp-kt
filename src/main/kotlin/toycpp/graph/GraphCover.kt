package toycpp.graph

import toycpp.graph.DepthVisitState.Finished
import toycpp.graph.DepthVisitState.InProgress

fun<Node> breadthCover(start: Node, getNeighbors: (Node) -> List<Node>): List<Node> {
    val visited = mutableSetOf<Node>()
    val result = mutableListOf<Node>()

    fun process(node: Node) {
        if (node in visited) return

        result += node
        visited += node

        for (neighbor in getNeighbors(node)) {
            process(neighbor)
        }
    }

    process(start)
    return result
}

data class DepthCoverResult<Node>(
    val nodes: List<Node>,
    val oneNodePerCycle: List<Node>
)

private enum class DepthVisitState {
    InProgress,
    Finished
}

/**
 * Returns a list of nodes in the directed graph starting at [start] and a list of nodes indicating cycles.
 */
fun<Node> depthCover(start: Node, getNeighbors: (Node) -> List<Node>): DepthCoverResult<Node> {
    // Implementation: Introduction to Algorithms (CLRS), 22.11
    val visited = mutableMapOf<Node, DepthVisitState>()
    val cycleNodes = mutableListOf<Node>()
    val result = mutableListOf<Node>()

    fun process(node: Node) {
        visited[node] = InProgress
        result += node

        for (neighbor in getNeighbors(node)) {
            when (visited[neighbor]) {
                null -> process(neighbor)
                InProgress -> cycleNodes += neighbor
                Finished -> {}
            }
        }

        visited[node] = Finished
    }

    process(start)
    return DepthCoverResult(result, cycleNodes)
}