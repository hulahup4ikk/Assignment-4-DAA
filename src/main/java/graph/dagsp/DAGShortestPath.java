package graph.dagsp;

import com.google.gson.*;
import graph.metrics.*;
import java.io.*;
import java.util.*;

public class DAGShortestPath {

    public static void main(String[] args) throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Reader reader = new FileReader("src/main/resources/tasks.json");
        JsonObject data = gson.fromJson(reader, JsonObject.class);
        reader.close();

        int n = data.get("n").getAsInt();
        int source = data.get("source").getAsInt();
        JsonArray edges = data.getAsJsonArray("edges");

        double[][] weight = new double[n][n];
        for (int i = 0; i < n; i++)
            Arrays.fill(weight[i], Double.POSITIVE_INFINITY);

        for (JsonElement e : edges) {
            JsonObject obj = e.getAsJsonObject();
            int u = obj.get("u").getAsInt();
            int v = obj.get("v").getAsInt();
            double w = obj.get("w").getAsDouble();
            weight[u][v] = w;
        }

        List<Integer> topoOrder = new ArrayList<>();
        for (int i = 0; i < n; i++) topoOrder.add(i);

        AlgorithmMetrics metrics = new AlgorithmMetrics();
        metrics.startTimer();
        double[] dist = shortestPathDAG(weight, topoOrder, source, metrics);
        metrics.stopTimer();

        System.out.println("========= DAG Shortest Paths =========");
        System.out.println("Execution time: " + (metrics.getElapsedTimeNs() / 1_000_000.0) + " ms");
        System.out.println("Relaxations: " + metrics.getCounter("relaxations"));
        System.out.println("--------------------------------------");

        System.out.println("Shortest distances from source = " + source);
        for (int i = 0; i < dist.length; i++) {
            if (Double.isInfinite(dist[i])) System.out.println("Vertex " + i + " → unreachable");
            else System.out.println("Vertex " + i + " → " + dist[i]);
        }
    }

    public static double[] shortestPathDAG(double[][] weight, List<Integer> topoOrder,
                                           int source, Metrics metrics) {
        int n = weight.length;
        double[] dist = new double[n];
        Arrays.fill(dist, Double.POSITIVE_INFINITY);
        dist[source] = 0;

        for (int u : topoOrder) {
            if (dist[u] != Double.POSITIVE_INFINITY) {
                for (int v = 0; v < n; v++) {
                    if (weight[u][v] != Double.POSITIVE_INFINITY) {
                        if (dist[v] > dist[u] + weight[u][v]) {
                            dist[v] = dist[u] + weight[u][v];
                            metrics.incrementCounter("relaxations");
                        }
                    }
                }
            }
        }
        return dist;
    }
}
