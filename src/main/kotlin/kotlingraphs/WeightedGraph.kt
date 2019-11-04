package kotlingraphs

abstract class WeightedGraph<N> : Graph<N>() {
    abstract fun getEdgeWeight(start: N, destination: N): Double

    fun findAStarPath(start: N, matcher: (N) -> Boolean, hFunction: (N) -> Double = { 0.0 }) =
        findAStarPath(start, matcher, { n1, n2 -> getEdgeWeight(n1, n2) }, hFunction)

    fun findAStarPath(start: N, target: N, hFunction: (N) -> Double = { 0.0 }) =
        findAStarPath(start, target, { n1, n2 -> getEdgeWeight(n1, n2) }, hFunction)
}