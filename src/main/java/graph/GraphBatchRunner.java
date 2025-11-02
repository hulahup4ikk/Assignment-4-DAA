package graph;

import com.google.gson.*;
import graph.metrics.*;
import graph.scc.Kosaraju;
import graph.topo.TopoSort;
import graph.dagsp.DAGShortestPath;

import java.io.*;
import java.util.*;

public class GraphBatchRunner {

    public static void main(String[] args) throws IOException {
        System.out.println("========== Assignment 4 – Batch Graph Runner ==========\n");

        File dataFolder = new File("src/main/resources/data/");
        File[] files = dataFolder.listFiles((dir, name) -> name.endsWith(".json"));

        if (files == null || files.length == 0) {
            System.out.println("No dataset files found in /data/ folder!");
            return;
        }

        System.out.printf("%-15s %-12s %-12s %-12s %-15s %-15s %-15s%n",
                "Dataset", "Nodes", "Edges", "SCCs", "SCC Time(ms)",
                "Topo Time(ms)", "SP Time(ms)");
        System.out.println("-----------------------------------------------------------------------------------------");

        for (File file : files) {
            runSingleDataset(file);
        }

        System.out.println("\nBatch test completed for all datasets.");
    }

    private static void runSingleDataset(File file) {
        try {
            Gson gson = new Gson();
            Reader reader = new FileReader(file);
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

            List<Set<Integer>> dag = Kosaraju.buildCondensationGraph(adj, sccs);

            AlgorithmMetrics topoMetrics = new AlgorithmMetrics();
            topoMetrics.startTimer();
            List<Integer> topoOrder = TopoSort.kahnTopologicalSort(dag, topoMetrics);
            topoMetrics.stopTimer();

            AlgorithmMetrics spMetrics = new AlgorithmMetrics();
            spMetrics.startTimer();
            DAGShortestPath.shortestPathDAG(weight, topoOrder, source, spMetrics);
            spMetrics.stopTimer();

            System.out.printf("%-15s %-12d %-12d %-12d %-15.3f %-15.3f %-15.3f%n",
                    file.getName(),
                    n,
                    edges.size(),
                    sccs.size(),
                    sccMetrics.getElapsedTimeNs() / 1_000_000.0,
                    topoMetrics.getElapsedTimeNs() / 1_000_000.0,
                    spMetrics.getElapsedTimeNs() / 1_000_000.0);

        } catch (Exception e) {
            System.out.println("Error processing " + file.getName() + ": " + e.getMessage());
        }
    }
}
