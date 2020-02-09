package kotlingraphs

import java.util.*
import kotlin.math.sign

abstract class Graph<N> {
    abstract fun containsNode(node: N): Boolean
    abstract fun containsEdge(start: N, destination: N): Boolean

    abstract fun getAdjacentNodes(node: N): Iterable<N>
    abstract val nodes: Set<N>

    /**
     * Returns a representation of the graph in the graphviz dot language
     */
    abstract fun getDotString(): String

    protected fun getDotString(
        isDirected: Boolean,
        edgeLabelExtractor: (N, N) -> String? = { _, _ -> null }
    ): String {
        val sb = StringBuilder()
        val connectorString = if (isDirected) "->" else "--"
        sb.appendln(if (isDirected) "strict digraph{" else "strict graph{")
        for (node in nodes) {
            sb.appendln("\"$node\"")
            for (neighbour in getAdjacentNodes(node)) {
                sb.append("\"$node\" $connectorString \"$neighbour\"")
                val label = edgeLabelExtractor(node, neighbour)
                if (label == null)
                    sb.appendln()
                else
                    sb.appendln(" [label=\"$label\"]")
            }
        }
        sb.appendln("}")
        return sb.toString()
    }

    fun traverseDepthFirst(start: N): List<N> {
        val visited = mutableListOf<N>()
        fun visit(node: N) {
            visited += node
            for (it in getAdjacentNodes(node).filter { it !in visited }) visit(it)
        }

        visit(start)
        return visited
    }

    fun traverseBreadthFirst(start: N, maxDepth: Int = Int.MAX_VALUE): List<N> {
        val visited = mutableListOf<N>()
        val queue: Queue<N> = LinkedList<N>()
        val depthMap = hashMapOf(start to 0)
        queue.offer(start)
        while (queue.any()) {
            val current = queue.poll()
            val currentDepth = depthMap[current]!!
            visited += current
            if (currentDepth == maxDepth) continue
            getAdjacentNodes(current).filter { it !in queue && it !in visited }.forEach {
                queue.offer(it)
                depthMap[it] = currentDepth + 1
            }
        }
        return visited
    }

    protected inline fun findAStarPath(
        start: N,
        matcher: (N) -> Boolean,
        distanceFunction: (N, N) -> Double,
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

    protected inline fun findAStarPath(
        start: N,
        target: N,
        distanceFunction: (N, N) -> Double,
        crossinline hFunction: (N) -> Double
    ) =
        findAStarPath(start, { it == target }, distanceFunction, hFunction)


}