package graph.metrics;

public interface Metrics {
    void startTimer();
    void stopTimer();
    long getElapsedTimeNs();
    void reset();
    void incrementCounter(String name);
    int getCounter(String name);
}
