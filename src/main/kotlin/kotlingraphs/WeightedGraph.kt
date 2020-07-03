package kotlingraphs

abstract class WeightedGraph<N> : Graph<N>() {
    abstract fun getEdgeWeight(start: N, destination: N): Double

    val edgeWeights
        get() = edges.map { it to getEdgeWeight(it.first, it.second) }

    fun findAStarPath(start: N, matcher: (N) -> Boolean, hFunction: (N) -> Double = { 0.0 }) =
        findAStarPath(start, matcher, { n1, n2 -> getEdgeWeight(n1, n2) }, hFunction)

    fun findAStarPath(start: N, target: N, hFunction: (N) -> Double = { 0.0 }) =
        findAStarPath(start, target, { n1, n2 -> getEdgeWeight(n1, n2) }, hFunction)

    protected fun getDotString(
        isDirected: Boolean,
        stylePrefix: String = "",
        groupExtractor: (N) -> Int = { 0 },
        nodeLabelExtractor: (N) -> String = { it.toString() }
    ) =
        super.getDotString(
            isDirected,
            stylePrefix,
            { x, y -> getEdgeWeight(x, y).toString() },
            groupExtractor,
            nodeLabelExtractor
        )

    abstract override fun <T> mapNodes(nodeConverter: (N) -> T): WeightedGraph<T>

    fun cloneToWeightedListGraph(): WeightedListGraph<N> {
        val res = WeightedListGraph<N>(isDirected)
        res.addNodes(nodes)
        for (node in nodes) {
            for (neighbour in getAdjacentNodes(node)) {
                res.addEdge(node, neighbour, getEdgeWeight(node, neighbour))
            }
        }
        return res
    }

    fun mapEdgeWeights(weightConverter: (from: N, to: N, oldWeight: Double) -> Double): WeightedListGraph<N> {
        val res = WeightedListGraph<N>(isDirected)
        res.addNodes(nodes)
        for (node in nodes) {
            for (neighbour in getAdjacentNodes(node)) {
                res.addEdge(node, neighbour, weightConverter(node, neighbour, getEdgeWeight(node, neighbour)))
            }
        }
        return res
    }

    fun mapOrRemoveEdgeWeights(weightConverter: (from: N, to: N, oldWeight: Double) -> Double?): WeightedListGraph<N> {
        val res = WeightedListGraph<N>(isDirected)
        res.addNodes(nodes)
        for (node in nodes) {
            for (neighbour in getAdjacentNodes(node)) {
                weightConverter(node, neighbour, getEdgeWeight(node, neighbour))?.let {
                    res.addEdge(
                        node, neighbour,
                        it
                    )
                }
            }
        }
        return res
    }
}