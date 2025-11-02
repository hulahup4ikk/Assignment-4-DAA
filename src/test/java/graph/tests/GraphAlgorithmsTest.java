package graph.tests;

import graph.metrics.*;
import graph.scc.Kosaraju;
import graph.topo.TopoSort;
import graph.dagsp.DAGShortestPath;

import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class GraphAlgorithmsTest {


    private List<List<Integer>> simpleDAG() {
        List<List<Integer>> adj = new ArrayList<>();
        for (int i = 0; i < 4; i++) adj.add(new ArrayList<>());
        adj.get(0).add(1);
        adj.get(1).add(2);
        adj.get(2).add(3);
        return adj;
    }

    private double[][] simpleWeights() {
        double INF = Double.POSITIVE_INFINITY;
        double[][] w = {
                {INF, 2, INF, INF},
                {INF, INF, 3, INF},
                {INF, INF, INF, 1},
                {INF, INF, INF, INF}
        };
        return w;
    }

    private List<List<Integer>> cyclicGraph() {
        List<List<Integer>> adj = new ArrayList<>();
        for (int i = 0; i < 3; i++) adj.add(new ArrayList<>());
        adj.get(0).add(1);
        adj.get(1).add(2);
        adj.get(2).add(0);
        return adj;
    }


    @Test
    void testSCCSingleComponent() {
        List<List<Integer>> adj = cyclicGraph();
        AlgorithmMetrics metrics = new AlgorithmMetrics();
        List<List<Integer>> sccs = Kosaraju.findSCCs(adj, adj.size(), metrics);
        assertEquals(1, sccs.size(), "Cyclic graph should form one SCC");
        assertEquals(3, sccs.get(0).size(), "SCC should include all 3 vertices");
    }

    @Test
    void testSCCMultipleComponents() {
        List<List<Integer>> adj = simpleDAG();
        AlgorithmMetrics metrics = new AlgorithmMetrics();
        List<List<Integer>> sccs = Kosaraju.findSCCs(adj, adj.size(), metrics);
        assertEquals(4, sccs.size(), "DAG should have one SCC per vertex");
    }

    @Test
    void testSCCPerformanceMetricsNonNegative() {
        List<List<Integer>> adj = simpleDAG();
        AlgorithmMetrics metrics = new AlgorithmMetrics();
        Kosaraju.findSCCs(adj, adj.size(), metrics);
        assertTrue(metrics.getCounter("DFS_visits") >= 0);
        assertTrue(metrics.getCounter("DFS_edges") >= 0);
    }


    @Test
    void testTopoSortOrderForDAG() {
        List<Set<Integer>> dag = new ArrayList<>();
        for (int i = 0; i < 4; i++) dag.add(new HashSet<>());
        dag.get(0).add(1);
        dag.get(1).add(2);
        dag.get(2).add(3);

        AlgorithmMetrics metrics = new AlgorithmMetrics();
        List<Integer> order = TopoSort.kahnTopologicalSort(dag, metrics);

        assertEquals(List.of(0, 1, 2, 3), order, "Topological order must match expected sequence");
        assertEquals(4, order.size());
        assertTrue(metrics.getCounter("pushes") >= 0);
        assertTrue(metrics.getCounter("pops") >= 0);
    }

    @Test
    void testTopoSortCycleDetection() {
        List<Set<Integer>> dag = new ArrayList<>();
        for (int i = 0; i < 3; i++) dag.add(new HashSet<>());
        dag.get(0).add(1);
        dag.get(1).add(2);
        dag.get(2).add(0);

        AlgorithmMetrics metrics = new AlgorithmMetrics();
        List<Integer> order = TopoSort.kahnTopologicalSort(dag, metrics);
        assertTrue(order.size() < 3, "Topological order should be incomplete for cyclic graph");
    }


    @Test
    void testShortestPathsSimpleDAG() {
        double[][] w = simpleWeights();
        List<Integer> topoOrder = List.of(0, 1, 2, 3);
        AlgorithmMetrics metrics = new AlgorithmMetrics();

        double[] dist = DAGShortestPath.shortestPathDAG(w, topoOrder, 0, metrics);

        assertEquals(0.0, dist[0]);
        assertEquals(2.0, dist[1]);
        assertEquals(5.0, dist[2]);
        assertEquals(6.0, dist[3]);
        assertTrue(metrics.getCounter("relaxations") > 0);
    }

    @Test
    void testShortestPathsDisconnected() {
        double INF = Double.POSITIVE_INFINITY;
        double[][] w = {
                {INF, 1, INF},
                {INF, INF, INF},
                {INF, INF, INF}
        };
        List<Integer> topoOrder = List.of(0, 1, 2);
        AlgorithmMetrics metrics = new AlgorithmMetrics();

        double[] dist = DAGShortestPath.shortestPathDAG(w, topoOrder, 0, metrics);
        assertTrue(Double.isInfinite(dist[2]), "Unreachable vertices should remain infinite");
    }

    @Test
    void testShortestPathTimingNonNegative() {
        double[][] w = simpleWeights();
        List<Integer> topoOrder = List.of(0, 1, 2, 3);
        AlgorithmMetrics metrics = new AlgorithmMetrics();

        metrics.startTimer();
        DAGShortestPath.shortestPathDAG(w, topoOrder, 0, metrics);
        metrics.stopTimer();

        assertTrue(metrics.getElapsedTimeNs() >= 0, "Execution time must be non-negative");
    }
}