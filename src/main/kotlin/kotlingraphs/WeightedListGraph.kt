package kotlingraphs

class WeightedListGraph<N>(override val isDirected: Boolean = false) : WeightedGraph<N>() {
    private val adMap = HashMap<N, HashMap<N, Double>>()

    override val nodes: Set<N>
        get() = adMap.keys

    fun addNodes(vararg nodes: N) {
        addNodes(nodes.toList())
    }

    fun addNodes(nodes: Iterable<N>) {
        fun add(n: N) {
            if (!containsNode(n))
                adMap[n] = HashMap()
        }
        nodes.forEach { add(it) }
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
    fun getDotString(
        stylePrefix: String = "",
        groupExtractor: (N) -> Int = { 0 },
        nodeLabelExtractor: (N) -> String = { it.toString() }
    ): String =
        getDotString(isDirected, stylePrefix, groupExtractor, nodeLabelExtractor)

    override fun <T> mapNodes(nodeConverter: (N) -> T): WeightedListGraph<T> {
        val res = WeightedListGraph<T>(isDirected)
        val nodeMap = nodes.associateWith { nodeConverter(it) }
        for (node in this.nodes) {
            res.addNodes(nodeMap[node]!!)
            for (neighbour in getAdjacentNodes(node)) {
                val weight = getEdgeWeight(node, neighbour)
                res.addEdge(nodeMap[node]!!, nodeMap[neighbour]!!, weight)
            }
        }
        return res
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as WeightedListGraph<*>

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