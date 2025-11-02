package graph;

import com.google.gson.*;
import graph.metrics.*;
import graph.scc.Kosaraju;
import graph.topo.TopoSort;
import graph.dagsp.DAGShortestPath;

import java.io.*;
import java.util.*;

public class GraphTasks {

    public static void main(String[] args) throws IOException {
        System.out.println("========== Assignment 4 – Graph Tasks ==========\n");

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Reader reader = new FileReader("src/main/resources/tasks.json");
        JsonObject data = gson.fromJson(reader, JsonObject.class);
        reader.close();

        int n = data.get("n").getAsInt();
        JsonArray edges = data.getAsJsonArray("edges");
        int source = data.get("source").getAsInt();

        List<List<Integer>> adj = new ArrayList<>();
        double[][] weight = new double[n][n];
        for (int i = 0; i < n; i++) {
            adj.add(new ArrayList<>());
            Arrays.fill(weight[i], Double.POSITIVE_INFINITY);
        }

        for (JsonElement e : edges) {
            JsonObject obj = e.getAsJsonObject();
            int u = obj.get("u").getAsInt();
            int v = obj.get("v").getAsInt();
            double w = obj.get("w").getAsDouble();
            adj.get(u).add(v);
            weight[u][v] = w;
        }

        AlgorithmMetrics sccMetrics = new AlgorithmMetrics();
        sccMetrics.startTimer();
        List<List<Integer>> sccs = Kosaraju.findSCCs(adj, n, sccMetrics);
        sccMetrics.stopTimer();

        System.out.println("----- Kosaraju’s SCC Results -----");
        System.out.printf("Execution time: %.3f ms%n",
                sccMetrics.getElapsedTimeNs() / 1_000_000.0);
        System.out.println("DFS_visits: " + sccMetrics.getCounter("DFS_visits"));
        System.out.println("DFS_edges: " + sccMetrics.getCounter("DFS_edges"));
        System.out.println("Total SCCs: " + sccs.size());
        for (int i = 0; i < sccs.size(); i++)
            System.out.println("SCC " + i + ": " + sccs.get(i));
        System.out.println();

        List<Set<Integer>> dag = Kosaraju.buildCondensationGraph(adj, sccs);
        System.out.println("Condensation Graph (DAG):");
        for (int i = 0; i < dag.size(); i++)
            System.out.println("Component " + i + " -> " + dag.get(i));
        System.out.println();

        AlgorithmMetrics topoMetrics = new AlgorithmMetrics();
        topoMetrics.startTimer();
        List<Integer> topoOrder = TopoSort.kahnTopologicalSort(dag, topoMetrics);
        topoMetrics.stopTimer();

        System.out.println("----- Kahn’s Topological Sort -----");
        System.out.printf("Execution time: %.3f ms%n",
                topoMetrics.getElapsedTimeNs() / 1_000_000.0);
        System.out.println("Pushes: " + topoMetrics.getCounter("pushes"));
        System.out.println("Pops: " + topoMetrics.getCounter("pops"));
        System.out.println("Topological order of components: " + topoOrder);
        System.out.println();

        AlgorithmMetrics spMetrics = new AlgorithmMetrics();
        spMetrics.startTimer();
        double[] dist = DAGShortestPath.shortestPathDAG(weight, topoOrder, source, spMetrics);
        spMetrics.stopTimer();

        System.out.println("----- DAG Shortest Paths -----");
        System.out.printf("Execution time: %.3f ms%n",
                spMetrics.getElapsedTimeNs() / 1_000_000.0);
        System.out.println("Relaxations: " + spMetrics.getCounter("relaxations"));
        System.out.println("Shortest distances from source = " + source);
        for (int i = 0; i < dist.length; i++) {
            if (Double.isInfinite(dist[i]))
                System.out.println("Vertex " + i + " → unreachable");
            else
                System.out.println("Vertex " + i + " → " + dist[i]);
        }

        System.out.println("\n========= Performance Summary =========");
        System.out.printf("%-20s %-15s %-15s%n", "Algorithm", "Time (ms)", "Key Operations");
        System.out.println("----------------------------------------------");
        System.out.printf("%-20s %-15.3f %-15s%n", "Kosaraju SCC",
                sccMetrics.getElapsedTimeNs() / 1_000_000.0,
                "DFS=" + (sccMetrics.getCounter("DFS_visits") + sccMetrics.getCounter("DFS_edges")));
        System.out.printf("%-20s %-15.3f %-15s%n", "Kahn TopoSort",
                topoMetrics.getElapsedTimeNs() / 1_000_000.0,
                "Pushes=" + topoMetrics.getCounter("pushes") +
                        ", Pops=" + topoMetrics.getCounter("pops"));
        System.out.printf("%-20s %-15.3f %-15s%n", "DAG Shortest Path",
                spMetrics.getElapsedTimeNs() / 1_000_000.0,
                "Relax=" + spMetrics.getCounter("relaxations"));
        System.out.println("===============================================");
    }
}
