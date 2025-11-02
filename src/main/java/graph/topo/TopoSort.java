package graph.topo;

import graph.metrics.*;

import java.util.*;

public class TopoSort {

    public static void main(String[] args) {
        List<Set<Integer>> dag = new ArrayList<>();
        for (int i = 0; i < 6; i++) dag.add(new HashSet<>());

        dag.get(0).add(1);
        dag.get(1).add(2);
        dag.get(2).add(3);
        dag.get(3).add(4);
        dag.get(4).add(5);

        AlgorithmMetrics metrics = new AlgorithmMetrics();
        metrics.startTimer();
        List<Integer> topo = kahnTopologicalSort(dag, metrics);
        metrics.stopTimer();

        System.out.println("========= Kahn’s Topological Sort =========");
        System.out.println("Execution time: " + (metrics.getElapsedTimeNs() / 1_000_000.0) + " ms");
        System.out.println("Pushes: " + metrics.getCounter("pushes"));
        System.out.println("Pops: " + metrics.getCounter("pops"));
        System.out.println("--------------------------------------------");

        System.out.println("Topological Order:");
        System.out.println(topo);
    }

    public static List<Integer> kahnTopologicalSort(List<Set<Integer>> dag, Metrics metrics) {
        int n = dag.size();
        int[] inDegree = new int[n];

        for (int u = 0; u < n; u++)
            for (int v : dag.get(u))
                inDegree[v]++;

        Queue<Integer> q = new LinkedList<>();
        for (int i = 0; i < n; i++) {
            if (inDegree[i] == 0) {
                q.add(i);
                metrics.incrementCounter("pushes");
            }
        }

        List<Integer> topo = new ArrayList<>();

        while (!q.isEmpty()) {
            int u = q.poll();
            metrics.incrementCounter("pops");
            topo.add(u);

            for (int v : dag.get(u)) {
                inDegree[v]--;
                if (inDegree[v] == 0) {
                    q.add(v);
                    metrics.incrementCounter("pushes");
                }
            }
        }

        if (topo.size() != n) {
            System.out.println("Warning: The graph contains a cycle — topological sort incomplete.");
        }

        return topo;
    }
}
