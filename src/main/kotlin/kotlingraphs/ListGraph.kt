package kotlingraphs

class ListGraph<N>(override val isDirected: Boolean = false) : Graph<N>() {
    private val adMap = HashMap<N, HashSet<N>>()

    override val nodes: Set<N>
        get() = adMap.keys

    override fun containsNode(node: N): Boolean = adMap.containsKey(node)

    override fun containsEdge(start: N, destination: N): Boolean = adMap[start]?.contains(destination) ?: false

    fun addNodes(vararg nodes: N) {
        addNodes(nodes.toList())
    }

    fun addNodes(nodes: Iterable<N>) {
        fun add(n: N) {
            if (!containsNode(n))
                adMap[n] = HashSet()
        }
        nodes.forEach { add(it) }
    }

    fun removeNode(node: N) {
        adMap.remove(node)
        adMap.values.forEach { it.remove(node) }
    }

    fun removeEdge(start: N, destination: N) {
        adMap[start]?.remove(destination)
        if (!isDirected) adMap[destination]?.remove(start)
    }

    override fun getAdjacentNodes(node: N): Iterable<N> =
        adMap[node] ?: error("The provided node is not part of the graph: $node")

    fun addEdge(start: N, destination: N) {
        addNodes(start, destination)
        adMap[start]!! += destination
        if (!isDirected) adMap[destination]!! += start
    }

    fun findAStarPath(start: N, matcher: (N) -> Boolean) = findAStarPath(start, matcher, ({ _, _ -> 1.0 }))

    override fun getDotString(): String = getDotString(isDirected)
    fun getDotString(
        stylePrefix: String = "",
        groupExtractor: (N) -> Int = { 0 },
        nodeLabelExtractor: (N) -> String = { it.toString() }
    ): String =
        getDotString(isDirected, stylePrefix, groupExtractor = groupExtractor, nodeLabelExtractor = nodeLabelExtractor)

    override fun <T> mapNodes(nodeConverter: (N) -> T): ListGraph<T> {
        val res = ListGraph<T>(isDirected)
        val nodeMap = nodes.associateWith { nodeConverter(it) }
        for (node in this.nodes) {
            res.addNodes(nodeMap[node]!!)
            for (neighbour in getAdjacentNodes(node)) {
                res.addEdge(nodeMap[node]!!, nodeMap[neighbour]!!)
            }
        }
        return res
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ListGraph<*>

        if (isDirected != other.isDirected) return false
        if (adMap != other.adMap) return false

        return true
    }

    override fun hashCode(): Int {
        var result = isDirected.hashCode()
        result = 31 * result + adMap.hashCode()
        return result
    }
}