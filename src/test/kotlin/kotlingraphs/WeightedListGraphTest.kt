package kotlingraphs

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertIterableEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class WeightedListGraphTest {

    private val g = WeightedListGraph<Int>(true)

    @BeforeEach
    fun setUp() {
        g.clear()
    }

    @AfterEach
    fun tearDown() {
    }

    @Test
    fun `addNodes should make the graph contain the nodes`() {
        g.addNodes(1, 2)
        assert(g.containsNode(1))
        assert(g.containsNode(2))
    }

    @Test
    fun `addEdge should make graph contain the nodes earlier absent`() {
        g.addEdge(1, 2, 1.0)
        assert(g.containsNode(1))
        assert(g.containsNode(2))
    }

    @Test
    fun `removeNode should remove node and incoming edge`() {
        g.addNodes(1, 2, 3)
        g.addEdge(1, 2, 1.0)
        g.removeNode(2)
        assertFalse(g.containsNode(2))
        assertFalse(g.containsEdge(1, 2))
    }

    @Test
    fun `addNodes should not overwrite edges from already existing node`() {
        g.addEdge(1, 2, 1.0)
        g.addNodes(1)
        assert(g.containsEdge(1, 2))
    }

    @Test
    fun `BFS should find all nodes in linear graph`() {
        g.addEdge(1, 2, 1.0)
        g.addEdge(2, 3, 2.0)
        g.addEdge(3, 4, 2.0)
        val nodes = g.traverseBreadthFirst(1)
        assertIterableEquals(listOf(1, 2, 3, 4), nodes)
    }

    @Test
    fun `DFS should find all nodes in linear graph`() {
        g.addEdge(1, 2, 1.0)
        g.addEdge(2, 3, 2.0)
        g.addEdge(3, 4, 2.0)
        val nodes = g.traverseDepthFirst(1)
        assertIterableEquals(listOf(1, 2, 3, 4), nodes)
    }

    @Test
    fun `Dijkstra should find shortest path in small graph`() {
        g.addEdge(1, 2, 10.0)
        g.addEdge(1, 3, 2.0)
        g.addEdge(3, 2, 3.0)
        val path = g.findAStarPath(1, 2)
        assertIterableEquals(listOf(1, 3, 2), path)
    }

    @Test
    fun `getDotString should return a non empty string`() {
        g.addEdge(1, 2, 10.0)
        g.addEdge(1, 3, 2.0)
        g.addEdge(3, 2, 3.0)
        g.addNodes(4)
        val dotString = g.getDotString()
        println(dotString)
        assert(dotString.isNotEmpty())
    }
}