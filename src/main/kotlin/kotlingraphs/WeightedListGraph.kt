package kotlingraphs

class WeightedListGraph<N>(val isDirected: Boolean = false) : WeightedGraph<N>() {
    private val adMap = HashMap<N, HashMap<N, Double>>()

    override val nodes: Set<N>
        get() = adMap.keys

    fun addNodes(node: N, vararg moreNodes: N) {
        fun add(n: N) {
            if (!containsNode(n))
                adMap[n] = HashMap()
        }
        add(node)
        moreNodes.forEach { add(it) }
    }

    fun addEdge(start: N, destination: N, weight: Double) {
        addNodes(start, destination)
        adMap[start]!![destination] = weight
        if (!isDirected) adMap[destination]!![start] = weight
    }

    override fun containsNode(node: N) = adMap.containsKey(node)

    override fun containsEdge(start: N, destination: N) = adMap[start]?.containsKey(destination) ?: false

    fun removeNode(node: N) {
        adMap.remove(node)
        adMap.values.forEach { it.remove(node) }
    }

    fun removeEdge(start: N, destination: N) {
        adMap[start]?.remove(destination)
        if (!isDirected) adMap[destination]?.remove(start)
    }

    override fun getEdgeWeight(start: N, destination: N) = adMap[start]?.get(destination)
        ?: throw IllegalArgumentException("The provided edge does not exist: $start -> $destination")

    override fun getAdjacentNodes(node: N): Iterable<N> =
        adMap[node]?.keys ?: throw IllegalArgumentException("The provided node is not part of the graph: $node")

    fun clear() {
        adMap.clear()
    }

    override fun getDotString(): String = getDotString(isDirected)
    fun getDotString(stylePrefix: String = "", groupExtractor: (N) -> Int = { 0 }): String =
        getDotString(isDirected, stylePrefix, groupExtractor)
}