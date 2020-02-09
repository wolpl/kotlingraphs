package kotlingraphs

class ListGraph<N>(val isDirected: Boolean = false) : Graph<N>() {
    private val adMap = HashMap<N, HashSet<N>>()

    override val nodes: Set<N>
        get() = adMap.keys

    override fun containsNode(node: N): Boolean = adMap.containsKey(node)

    override fun containsEdge(start: N, destination: N): Boolean = adMap[start]?.contains(destination) ?: false

    fun addNodes(node: N, vararg moreNodes: N) {
        fun add(n: N) {
            if (!containsNode(n))
                adMap[n] = HashSet()
        }
        add(node)
        moreNodes.forEach { add(it) }
    }

    fun removeNode(node: N) {
        adMap.remove(node)
        adMap.values.forEach { it.remove(node) }
    }

    fun removeEdge(start: N, destination: N) {
        adMap[start]!!.remove(destination)
        if (!isDirected) adMap[destination]!!.remove(start)
    }

    override fun getAdjacentNodes(node: N): Iterable<N> = adMap[node]!!

    fun addEdge(start: N, destination: N) {
        addNodes(start, destination)
        adMap[start]!! += destination
        if (!isDirected) adMap[destination]!! += start
    }

    fun findAStarPath(start: N, matcher: (N) -> Boolean) = findAStarPath(start, matcher, ({ _, _ -> 1.0 }))

    override fun getDotString(): String = getDotString(isDirected)
    fun getDotString(stylePrefix: String = "", groupExtractor: (N) -> Int = { 0 }): String =
        getDotString(isDirected, stylePrefix, groupExtractor = groupExtractor)
}