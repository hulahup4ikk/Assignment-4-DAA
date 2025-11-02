# Assignment 4
## 1. Overview
- This project implements and evaluates three fundamental graph algorithms with full instrumentation for performance measurement and comparison:
  
  | Task                          | Algorithm                            | File                               |
  | ----------------------------- | ------------------------------------ | ---------------------------------- |
  | **1.1 SCC Detection**         | Kosaraju’s Algorithm                 | `graph/scc/Kosaraju.java`          |
  | **1.2 Topological Sorting**   | Kahn’s Algorithm                     | `graph/topo/TopoSort.java`         |
  | **1.3 Shortest Paths in DAG** | DAG Shortest Path (Relaxation-based) | `graph/dagsp/DAGShortestPath.java` |

### Instrumentation includes:
- Execution time via System.nanoTime()

- Operation counters (DFS visits, edges, queue pushes/pops, relaxations)

- Reproducible measurements over multiple datasets

## 2. Data Summary

- A total of 9 directed graphs (.json format) were created under src/main/resources/data/ with varying size and structure.

| Category   | File           | Nodes | Edges | Type   | Cyclic | SCC Count | Description           | Weight Model |
| ---------- | -------------- | ----- | ----- | ------ | ------ | --------- | --------------------- | ------------ |
| **Small**  | `small1.json`  | 6     | 6     | DAG    | No     | 6         | Simple chain          | edge         |
| **Small**  | `small2.json`  | 7     | 6     | Cyclic | Yes    | 2         | Single SCC (1–3)      | edge         |
| **Small**  | `small3.json`  | 9     | 10    | Mixed  | Yes    | 3         | Two cycles (0–2, 3–5) | edge         |
| **Medium** | `medium1.json` | 12    | 14    | Mixed  | Yes    | 3         | Several SCCs          | edge         |
| **Medium** | `medium2.json` | 15    | 15    | DAG    | No     | 15        | Dense DAG             | edge         |
| **Medium** | `medium3.json` | 18    | 20    | Mixed  | Yes    | 3         | 3 SCC clusters        | edge         |
| **Large**  | `large1.json`  | 20    | 18    | DAG    | No     | 20        | Long chain DAG        | edge         |
| **Large**  | `large2.json`  | 35    | 35    | Mixed  | Yes    | 4         | Dense cyclic graph    | edge         |
| **Large**  | `large3.json`  | 50    | 50    | DAG    | No     | 50        | Performance test DAG  | edge         |

### Weight model:
- All edges have positive integer weights (1–5) stored as "w" in the JSON files.
- Example: {"u": 2, "v": 3, "w": 4}

## 3. Results Summary
### Execution Metrics
- Each dataset was processed with the following pipeline: 
- Kosaraju → Condensation Graph → Kahn’s Topological Sort → DAG Shortest Path

| Dataset          | Nodes | Edges | SCCs | SCC Time (ms) | Topo Time (ms) | SP Time (ms) |
| ---------------- | ----- | ----- | ---- | ------------- | -------------- | ------------ |
| **large1.json**  | 20    | 18    | 20   | 0.846         | 0.623          | 0.558        |
| **large2.json**  | 35    | 35    | 29   | 0.163         | 0.068          | 0.028        |
| **large3.json**  | 50    | 49    | 50   | 0.118         | 0.043          | 0.021        |
| **medium1.json** | 12    | 14    | 6    | 0.034         | 0.009          | 0.003        |
| **medium2.json** | 15    | 15    | 15   | 0.023         | 0.019          | 0.007        |
| **medium3.json** | 18    | 20    | 12   | 0.023         | 0.016          | 0.005        |
| **small1.json**  | 6     | 6     | 6    | 0.011         | 0.009          | 0.003        |
| **small2.json**  | 7     | 6     | 5    | 0.015         | 0.006          | 0.002        |
| **small3.json**  | 9     | 10    | 5    | 0.014         | 0.007          | 0.003        |

### Why the First Execution Is the Longest

- During performance testing, it was observed that the first dataset run consistently took more time than the following ones.
- This behavior is typical for Java-based programs and results from system-level initialization rather than algorithmic inefficiency.

### Reasons:

#### JVM Warm-Up and Class Loading
- The first execution triggers the Java Virtual Machine (JVM) to start, allocate memory, and load all required classes (including libraries such as Gson).
- This one-time setup adds noticeable overhead.

#### Just-In-Time (JIT) Compilation
- Initially, Java bytecode is interpreted. The JIT compiler optimizes and compiles frequently used methods to native code during execution.
- Subsequent runs are faster because the optimized code is reused.

#### File and Disk Caching
- The first run reads input files (.json datasets) from disk. The operating system caches them in memory, so later executions read them directly from cache (RAM), reducing I/O time.

#### Library Initialization (e.g., Gson Reflection)
- JSON parsing libraries perform reflection and cache metadata about object structures during the first use, speeding up subsequent operations.

### Operation Counters

| Algorithm    | Metric                    | Meaning                                           |
| ------------ | ------------------------- | ------------------------------------------------- |
| **Kosaraju** | `DFS_visits`, `DFS_edges` | number of recursive DFS calls and traversed edges |
| **Kahn**     | `pushes`, `pops`          | queue operations during sorting                   |
| **DAG-SP**   | `relaxations`             | number of edge relaxations (`dist[v]` updates)    |

### Average metrics (aggregated from datasets):

| Algorithm | Operations (avg) | Observation                                    |
| --------- |----------------| ---------------------------------------------- |
| Kosaraju  | ~150 DFS ops   | Grows with E and SCC density                   |
| Kahn      | ~n queue ops   | Linear to vertices (O(V+E))                    |
| DAG-SP    | ~E relaxations | Linear to edge count (sparse graphs efficient) |

## 4. Analysis
### 4.1 Strongly Connected Components (Kosaraju)

- Performs two DFS passes; complexity O(V + E).
- Time scales linearly with graph size.
- More SCCs → slightly more edges in condensation DAG → small time overhead.
- Sparse DAGs process faster than dense cyclic graphs.
#### Bottleneck:
- Recursive DFS depth; high density increases edge traversals → more DFS_edges.

### 4.2 Topological Sort (Kahn’s Algorithm)

- Executes efficiently for DAGs (O(V + E)).
- Cyclic graphs trigger the built-in detection mechanism (Warning: cycle detected).
- Queue operations (pushes/pops) scale proportionally to vertex count.

#### Observation:
- Dense graphs increase queue activity but not significantly affect runtime.
- Performance remains stable across datasets up to n=50.

### 4.3 Shortest Path in DAG

- Based on topological order — no need for priority queue (unlike Dijkstra).
- Performance ~O(V + E).
- Works only for acyclic graphs (otherwise order incomplete).
- Relaxation count ≈ edge count, consistent across datasets.

#### Bottleneck:
- Dense DAGs → more edge relaxations; negligible CPU overhead.

### 4.4 Effect of Graph Structure

| Structure    | SCC Behavior          | Topo Sort            | DAG-SP           |
| ------------ | --------------------- | -------------------- | ---------------- |
| Sparse DAG   | Fast; each node = SCC | Valid                | Few relaxations  |
| Dense DAG    | Slightly slower DFS   | Valid                | More relaxations |
| Cyclic graph | Merged SCCs           | Incomplete (warning) | Undefined paths  |
| Large DAG    | Linear growth in time | Stable               | Stable           |

## 5. Conclusions

| Algorithm                   | Best suited for                            | Limitations                         | Key Observation                  |
| --------------------------- | ------------------------------------------ | ----------------------------------- | -------------------------------- |
| **Kosaraju (SCC)**          | Detecting cycles and grouping dependencies | Two DFS passes (recursive overhead) | Simple, robust, scalable         |
| **Kahn’s Topological Sort** | Scheduling tasks in DAGs                   | Fails on cyclic graphs              | Clear cycle detection, fast      |
| **DAG Shortest Path**       | Weighted acyclic dependency resolution     | Requires valid topo order           | Very efficient for sparse graphs |


### Practical Recommendations

- Use Kosaraju to pre-process graphs and detect SCCs before applying DAG algorithms.
- Apply Kahn for scheduling, dependency graphs, compiler passes, etc.
- Use DAG-SP for performance-critical acyclic networks (project planning, dependency optimization).
- For dense or cyclic networks, use Tarjan or Dijkstra as alternatives.

## Example Console Output

========== Assignment 4 – Graph Tasks ==========

----- Kosaraju’s SCC Results -----
Execution time: 1.074 ms  
DFS_visits: 70  
DFS_edges: 70  
Total SCCs: 29

SCC Components:
SCC 0: [6, 8, 7]  
SCC 1: [3, 5, 4]  
SCC 2: [0, 2, 1]  
SCC 3: [9]  
SCC 4: [10]  
SCC 5: [11]  
SCC 6: [12]  
SCC 7: [13]  
SCC 8: [14]  
SCC 9: [15]  
SCC 10: [16]  
SCC 11: [17]  
SCC 12: [18]  
SCC 13: [19]  
SCC 14: [20]  
SCC 15: [21]  
SCC 16: [22]  
SCC 17: [23]  
SCC 18: [24]  
SCC 19: [25]  
SCC 20: [26]  
SCC 21: [27]  
SCC 22: [28]  
SCC 23: [29]  
SCC 24: [30]  
SCC 25: [31]  
SCC 26: [32]  
SCC 27: [33]  
SCC 28: [34]

Condensation Graph (DAG):
Component 0 → []  
Component 1 → []  
Component 2 → [3]  
Component 3 → [4]  
Component 4 → [5]  
Component 5 → [6]  
Component 6 → [7]  
Component 7 → [8]  
Component 8 → [9]  
Component 9 → [10]  
Component 10 → [11]  
Component 11 → [12]  
Component 12 → [13]  
Component 13 → [14]  
Component 14 → [15]  
Component 15 → [16]  
Component 16 → [17]  
Component 17 → [18]  
Component 18 → [19]  
Component 19 → [20]  
Component 20 → [21]  
Component 21 → [22]  
Component 22 → [23]  
Component 23 → [24]  
Component 24 → [25]  
Component 25 → [26]  
Component 26 → [27]  
Component 27 → [28]  
Component 28 → []

----- Kahn’s Topological Sort -----
Execution time: 0.965 ms  
Pushes: 29  
Pops: 29  
Topological order of components:  
[0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28]

----- DAG Shortest Paths -----
Execution time: 0.475 ms  
Relaxations: 23

Shortest distances from source = 0:
Vertex 0 → 0.0  
Vertex 1 → 2.0  
Vertex 2 → 3.0  
Vertex 3 → unreachable  
Vertex 4 → unreachable  
Vertex 5 → unreachable  
Vertex 6 → unreachable  
Vertex 7 → unreachable  
Vertex 8 → unreachable  
Vertex 9 → 8.0  
Vertex 10 → 11.0  
Vertex 11 → 12.0  
Vertex 12 → 16.0  
Vertex 13 → 18.0  
Vertex 14 → 21.0  
Vertex 15 → 23.0  
Vertex 16 → 28.0  
Vertex 17 → 32.0  
Vertex 18 → 35.0  
Vertex 19 → 37.0  
Vertex 20 → 38.0  
Vertex 21 → 40.0  
Vertex 22 → 44.0  
Vertex 23 → 47.0  
Vertex 24 → 49.0  
Vertex 25 → 50.0  
Vertex 26 → 52.0  
Vertex 27 → 55.0  
Vertex 28 → 59.0  
Vertex 29 → 64.0  
Vertex 30 → unreachable  
Vertex 31 → unreachable  
Vertex 32 → unreachable  
Vertex 33 → unreachable  
Vertex 34 → unreachable

========= Performance Summary =========
Algorithm            Time (ms)       Key Operations
----------------------------------------------  
Kosaraju SCC         1.074           DFS = 140  
Kahn TopoSort        0.965           Pushes = 29, Pops = 29  
DAG Shortest Path    0.475           Relax = 23  
===============================================
