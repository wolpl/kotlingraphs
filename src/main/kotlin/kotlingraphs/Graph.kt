package kotlingraphs

import java.util.*
import kotlin.math.sign

abstract class Graph<N> {
    abstract fun containsNode(node: N): Boolean
    abstract fun containsEdge(start: N, destination: N): Boolean

    abstract fun getAdjacentNodes(node: N): Iterable<N>
    abstract val nodes: Set<N>
    abstract val isDirected: Boolean

    /**
     * Returns a representation of the graph in the graphviz dot language
     */
    abstract fun getDotString(): String

    fun getDotString(
        isDirected: Boolean,
        stylePrefix: String = "",
        edgeLabelExtractor: (N, N) -> String? = { _, _ -> null },
        groupExtractor: (N) -> Int = { 0 },
        nodeLabelExtractor: (N) -> String = { it.toString() }
    ): String {
        val sb = StringBuilder()
        val connectorString = if (isDirected) "->" else "--"
        sb.appendln(if (isDirected) "strict digraph{" else "strict graph{")
        sb.appendln(stylePrefix)
        for (node in nodes) {
            for (neighbour in getAdjacentNodes(node)) {
                sb.append("\"${nodeLabelExtractor(node)}\" $connectorString \"${nodeLabelExtractor(neighbour)}\"")
                val label = edgeLabelExtractor(node, neighbour)
                if (label == null)
                    sb.appendln()
                else
                    sb.appendln(" [label=\"$label\"]")
            }
        }

        nodes.associateWith(groupExtractor).forEach { (node, group) ->
            sb.appendln("subgraph cluster_$group{\nstyle=filled;")
            sb.appendln("\"${nodeLabelExtractor(node)}\"")
            sb.appendln("}")
        }
        sb.appendln("}")
        return sb.toString()
    }

    val edges
        get() = nodes.flatMap { node -> getAdjacentNodes(node).map { neighbour -> node to neighbour } }

    fun traverseDepthFirst(start: N): List<N> {
        val visited = mutableListOf<N>()
        fun visit(node: N) {
            visited += node
            for (it in getAdjacentNodes(node).filter { it !in visited }) visit(it)
        }

        visit(start)
        return visited
    }

    /**
     * Traverses the graph in breadth-first order.
     * @param start The node at which the traverse will begin
     * @param maxDepth A maximum depth where the traversal will not visit deeper nodes
     * @param stopTraversalPredicate A predicate that determines whether the traversal should not go any deeper at a certain node
     */
    fun traverseBreadthFirst(
        start: N,
        maxDepth: Int = Int.MAX_VALUE,
        stopTraversalPredicate: (N) -> Boolean = { false }
    ): List<N> {
        val visited = mutableListOf<N>()
        val queue: Queue<N> = LinkedList<N>()
        val depthMap = hashMapOf(start to 0)
        queue.offer(start)
        while (queue.any()) {
            val current = queue.poll()
            val currentDepth = depthMap[current]!!
            visited += current
            if (currentDepth == maxDepth || stopTraversalPredicate(current)) continue
            getAdjacentNodes(current).filter { it !in queue && it !in visited }.forEach {
                queue.offer(it)
                depthMap[it] = currentDepth + 1
            }
        }
        return visited
    }

    inline fun findAStarPath(
        start: N,
        matcher: (N) -> Boolean,
        distanceFunction: (N, N) -> Double = { _, _ -> 1.0 },
        crossinline hFunction: (N) -> Double = { 0.0 }
    ): List<N>? {
        val visited = mutableSetOf<N>()
        val predecessors = mutableMapOf<N, N>()
        val distances = mutableMapOf(start to 0.0)
        val discovered = PriorityQueue(Comparator<N> { o1, o2 ->
            (distances[o1]!! + hFunction(o1) - (distances[o2]!! + hFunction(o2))).sign.toInt()
        })
        discovered += start

        while (discovered.any()) {
            val current = discovered.poll()
            if (matcher(current)) {
                val path = mutableListOf<N>()
                var hop = current
                while (hop != start) {
                    path.add(0, hop)
                    hop = predecessors[hop]!!
                }
                path.add(0, hop)
                return path
            }

            visited += current
            for (next in getAdjacentNodes(current)) {
                val currentDistance = distances[current]!!
                if (distances[next] == null || distances[next]!! > currentDistance + distanceFunction(current, next)) {
                    distances[next] = currentDistance + distanceFunction(current, next)
                    predecessors[next] = current
                    discovered.remove(next)
                    discovered.offer(next)
                }
            }
        }
        return null
    }

    inline fun findAStarPath(
        start: N,
        target: N,
        distanceFunction: (N, N) -> Double = { _, _ -> 1.0 },
        crossinline hFunction: (N) -> Double = { 0.0 }
    ) =
        findAStarPath(start, { it == target }, distanceFunction, hFunction)

    fun getIncomingNodes(node: N): Set<N> {
        val res = mutableSetOf<N>()
        for (n in nodes) {
            if (getAdjacentNodes(n).contains(node))
                res.add(n)
        }
        return res
    }

    abstract fun <T> mapNodes(nodeConverter: (N) -> T): Graph<T>

    fun cloneToListGraph(): ListGraph<N> {
        val res = ListGraph<N>(isDirected)
        res.addNodes(nodes)
        for (node in nodes) {
            for (neighbour in getAdjacentNodes(node)) {
                res.addEdge(node, neighbour)
            }
        }
        return res
    }
}