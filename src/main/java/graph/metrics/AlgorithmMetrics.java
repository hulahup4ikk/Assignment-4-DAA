package graph.metrics;

import java.util.HashMap;
import java.util.Map;

public class AlgorithmMetrics implements Metrics {
    private long startTime;
    private long elapsedTime;
    private final Map<String, Integer> counters = new HashMap<>();

    @Override
    public void startTimer() {
        startTime = System.nanoTime();
    }

    @Override
    public void stopTimer() {
        elapsedTime = System.nanoTime() - startTime;
    }

    @Override
    public long getElapsedTimeNs() {
        return elapsedTime;
    }

    @Override
    public void reset() {
        counters.clear();
        elapsedTime = 0;
    }

    @Override
    public void incrementCounter(String name) {
        counters.put(name, counters.getOrDefault(name, 0) + 1);
    }

    @Override
    public int getCounter(String name) {
        return counters.getOrDefault(name, 0);
    }
}
