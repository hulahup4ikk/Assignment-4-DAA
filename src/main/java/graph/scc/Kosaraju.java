package graph.scc;

import com.google.gson.*;
import graph.metrics.*;

import java.io.*;
import java.util.*;

public class Kosaraju {

    public static void main(String[] args) throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Reader reader = new FileReader("src/main/resources/tasks.json");
        JsonObject data = gson.fromJson(reader, JsonObject.class);

        int n = data.get("n").getAsInt();
        JsonArray edges = data.getAsJsonArray("edges");

        List<List<Integer>> adj = new ArrayList<>();
        for (int i = 0; i < n; i++) adj.add(new ArrayList<>());

        for (JsonElement e : edges) {
            JsonObject obj = e.getAsJsonObject();
            int u = obj.get("u").getAsInt();
            int v = obj.get("v").getAsInt();
            adj.get(u).add(v);
        }

        AlgorithmMetrics metrics = new AlgorithmMetrics();
        metrics.startTimer();
        List<List<Integer>> sccs = findSCCs(adj, n, metrics);
        metrics.stopTimer();

        System.out.println("========= Kosaraju’s SCC Algorithm =========");
        System.out.println("Execution time: " + (metrics.getElapsedTimeNs() / 1_000_000.0) + " ms");
        System.out.println("DFS_visits: " + metrics.getCounter("DFS_visits"));
        System.out.println("DFS_edges: " + metrics.getCounter("DFS_edges"));
        System.out.println("--------------------------------------------");

        System.out.println("Strongly Connected Components (SCCs):");
        int id = 0;
        for (List<Integer> comp : sccs) {
            System.out.println("SCC " + (++id) + ": " + comp + " (size = " + comp.size() + ")");
        }

        List<Set<Integer>> dag = buildCondensationGraph(adj, sccs);

        System.out.println("\nCondensation Graph (DAG):");
        for (int i = 0; i < dag.size(); i++) {
            System.out.println("Component " + i + " -> " + dag.get(i));
        }
    }

    public static List<List<Integer>> findSCCs(List<List<Integer>> adj, int n, Metrics metrics) {
        boolean[] visited = new boolean[n];
        Stack<Integer> stack = new Stack<>();

        for (int i = 0; i < n; i++) {
            if (!visited[i]) {
                dfsFillOrder(i, visited, stack, adj, metrics);
            }
        }

        List<List<Integer>> rev = new ArrayList<>();
        for (int i = 0; i < n; i++) rev.add(new ArrayList<>());
        for (int u = 0; u < n; u++) {
            for (int v : adj.get(u)) {
                rev.get(v).add(u);
            }
        }

        Arrays.fill(visited, false);
        List<List<Integer>> sccs = new ArrayList<>();

        while (!stack.isEmpty()) {
            int v = stack.pop();
            if (!visited[v]) {
                List<Integer> comp = new ArrayList<>();
                dfsCollect(v, visited, rev, comp, metrics);
                sccs.add(comp);
            }
        }
        return sccs;
    }

    private static void dfsFillOrder(int v, boolean[] visited, Stack<Integer> stack,
                                     List<List<Integer>> adj, Metrics metrics) {
        visited[v] = true;
        metrics.incrementCounter("DFS_visits");
        for (int u : adj.get(v)) {
            metrics.incrementCounter("DFS_edges");
            if (!visited[u]) {
                dfsFillOrder(u, visited, stack, adj, metrics);
            }
        }
        stack.push(v);
    }

    private static void dfsCollect(int v, boolean[] visited, List<List<Integer>> rev,
                                   List<Integer> comp, Metrics metrics) {
        visited[v] = true;
        comp.add(v);
        metrics.incrementCounter("DFS_visits");
        for (int u : rev.get(v)) {
            metrics.incrementCounter("DFS_edges");
            if (!visited[u]) {
                dfsCollect(u, visited, rev, comp, metrics);
            }
        }
    }

    public static List<Set<Integer>> buildCondensationGraph(List<List<Integer>> adj, List<List<Integer>> sccs) {
        int m = sccs.size();
        List<Set<Integer>> dag = new ArrayList<>();
        for (int i = 0; i < m; i++) dag.add(new HashSet<>());

        Map<Integer, Integer> compIndex = new HashMap<>();
        for (int i = 0; i < sccs.size(); i++) {
            for (int v : sccs.get(i)) {
                compIndex.put(v, i);
            }
        }


        for (int u = 0; u < adj.size(); u++) {
            for (int v : adj.get(u)) {
                int cu = compIndex.get(u);
                int cv = compIndex.get(v);
                if (cu != cv) {
                    dag.get(cu).add(cv);
                }
            }
        }

        return dag;
    }
}
