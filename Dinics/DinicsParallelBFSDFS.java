
/**
 * Implementation of Dinic's network flow algorithm. The algorithm works by first constructing a
 * level graph using a BFS and then finding augmenting paths on the level graph using multiple DFSs.
 *
 * <p>Time Complexity: O(EV²)
 */

import static java.lang.Math.min;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;

public class DinicsParallelBFSDFS {
    public static AtomicIntegerArray lockNode;
    public static final AtomicInteger runIndex = new AtomicInteger(0);
    public static ArrayBlockingQueue<Integer> q;
    public static int[] level;
    // The adjacency list representing the flow graph.
    public static List<Edge>[] graph;

    private static class Edge {
        public int from, to;
        public Edge residual;
        public long flow;
        public final long capacity;

        public Edge(int from, int to, long capacity) {
            this.from = from;
            this.to = to;
            this.capacity = capacity;
        }

        public boolean isResidual() {
            return capacity == 0;
        }

        public long remainingCapacity() {
            return capacity - flow;
        }

        public void augment(long bottleNeck) {
            flow += bottleNeck;
            residual.flow -= bottleNeck;
        }

        public String toString(int s, int t) {
            String u = (from == s) ? "s" : ((from == t) ? "t" : String.valueOf(from));
            String v = (to == s) ? "s" : ((to == t) ? "t" : String.valueOf(to));
            return String.format(
                    "Edge %s -> %s | flow = %3d | capacity = %3d | is residual: %s",
                    u, v, flow, capacity, isResidual());
        }
    }

    private abstract static class NetworkFlowSolverBase {

        // To avoid overflow, set infinity to a value less than Long.MAX_VALUE;
        static final long INF = Long.MAX_VALUE / 2;

        // Inputs: n = number of nodes, s = source, t = sink
        final int n, s, t;

        // Indicates whether the network flow algorithm has ran. The solver only
        // needs to run once because it always yields the same result.
        protected boolean solved;

        // The maximum flow. Calculated by calling the {@link #solve} method.
        protected static long maxFlow;

        /**
         * Creates an instance of a flow network solver. Use the {@link #addEdge} method
         * to add edges to
         * the graph.
         *
         * @param n - The number of nodes in the graph including s and t.
         * @param s - The index of the source node, 0 <= s < n
         * @param t - The index of the sink node, 0 <= t < n and t != s
         */
        public NetworkFlowSolverBase(int n, int s, int t) {
            this.n = n;
            this.s = s;
            this.t = t;
            initializeEmptyFlowGraph();
        }

        // Constructs an empty graph with n nodes including s and t.
        @SuppressWarnings("unchecked")
        private void initializeEmptyFlowGraph() {
            graph = new List[n];
            for (int i = 0; i < n; i++)
                graph[i] = new ArrayList<Edge>();
        }

        /**
         * Adds a directed edge (and its residual edge) to the flow graph.
         *
         * @param from     - The index of the node the directed edge starts at.
         * @param to       - The index of the node the directed edge ends at.
         * @param capacity - The capacity of the edge
         */
        public void addEdge(int from, int to, long capacity) {
            // if (capacity <= 0)
            // throw new IllegalArgumentException("Forward edge capacity <= 0");
            Edge e1 = new Edge(from, to, capacity);
            Edge e2 = new Edge(to, from, 0);
            e1.residual = e2;
            e2.residual = e1;
            graph[from].add(e1);
            graph[to].add(e2);
        }

        /**
         * Returns the residual graph after the solver has been executed. This allows
         * you to inspect the
         * {@link Edge#flow} and {@link Edge#capacity} values of each edge. This is
         * useful if you are
         * debugging or want to figure out which edges were used during the max flow.
         */
        public List<Edge>[] getGraph() {
            execute();
            return graph;
        }

        // Returns the maximum flow from the source to the sink.
        public long getMaxFlow() {
            execute();
            return maxFlow;
        }

        // Wrapper method that ensures we only call solve() once
        private void execute() {
            if (solved)
                return;
            solved = true;
            solve();
        }

        // Method to implement which solves the network flow problem.
        public abstract void solve();
    }

    private static class DinicsSolver extends NetworkFlowSolverBase {

        /**
         * Creates an instance of a flow network solver. Use the {@link #addEdge} method
         * to add edges to
         * the graph.
         *
         * @param n - The number of nodes in the graph including source and sink nodes.
         * @param s - The index of the source node, 0 <= s < n
         * @param t - The index of the sink node, 0 <= t < n, t != s
         */
        public DinicsSolver(int n, int s, int t) {
            super(n, s, t);
            level = new int[n];
        }

        @Override
        public void solve() {
            // next[i] indicates the next edge index to take in the adjacency list for node
            // i. This is
            // part
            // of the Shimon Even and Alon Itai optimization of pruning deads ends as part
            // of the DFS
            // phase.
            int[] next = new int[n];
            
            while (bfs()) {
                Arrays.fill(next, 0);
                // Find max flow by adding all augmenting path flows using all threads
                int numThreads = 16;
                ExecutorService service = Executors.newFixedThreadPool(numThreads);
                for (int i = 0; i < numThreads; i++) {
                    service.submit(new DFSHelper(s, next, t));
                }

                service.shutdown();

                try {
                    service.awaitTermination(Long.MAX_VALUE, TimeUnit.MINUTES);
                } catch (Throwable e) {
                    System.out.println(e);
                }                  

            }
        }

        
        static class DFSHelper extends DinicsParallelBFSDFS implements Runnable {
            private int limit = MIN_DELAY;
            private static final int MIN_DELAY = 10;
            private static final int MAX_DELAY = 1000;
            // To avoid overflow, set infinity to a value less than Long.MAX_VALUE;
            final long INF = Long.MAX_VALUE / 2;
            // Inputs: n = number of nodes, s = source, t = sink
            final int s, t;
            int[] next;
            DFSHelper(int s, int[] next, int t) {
                this.s = s;
                this.t = t;
                this.next = next;
            }
            public void run() {
                // Find max flow by adding all augmenting path flows.
                for (long f = dfs(s, next, INF); f != 0; f = dfs(s, next, INF)) {
                    maxFlow += f;
                }            
            }
            private long dfs(int at, int[] next, long flow) {
                if (at == t)
                    return flow;
                final int numEdges = graph[at].size();

                for (; next[at] < numEdges; next[at]++) {
                    Edge edge = graph[at].get(next[at]);
                    long cap = edge.remainingCapacity();
                    if (cap > 0 && level[edge.to] == level[at] + 1) {
                        this.lock(edge.to);
                        try {
                            cap = edge.remainingCapacity();
                            if (cap > 0 && level[edge.to] == level[at] + 1) {
                                long bottleNeck = dfs(edge.to, next, min(flow, cap));
                                if (bottleNeck > 0) {
                                    edge.augment(bottleNeck);
                                    return bottleNeck;
                                }
                            }
                        } catch(Exception e) {
                            e.printStackTrace();
                        } finally {
                            this.unlock(edge.to);
                        }
                    }
                }
                return 0;
            }

            public void backoff() {
                Random random = new Random();
                try {
                int delay = random.nextInt(limit); 
                limit = Math.min(MAX_DELAY, 2 * limit); 
                Thread.sleep(delay);
                } catch (Exception e) {
                System.out.println(e);
                }
            } 

            public void lock(int nodeNumber) {
                while (true) {
                    while (lockNode.get(nodeNumber) == 1) {//System.out.println(nodeNumber + " node is busy");
                    };
                    if (lockNode.compareAndSet(nodeNumber, 0, 1)) {
                        // System.out.println(nodeNumber + " is locked");
                        return; 
                    } else {
                        // System.out.println("backoff");
                        backoff();
                    }
                }
            }

            public void unlock(int nodeNumber) {
                // System.out.println(nodeNumber + " is unlocked");
                lockNode.set(nodeNumber, 0); 
            }             
        }

       

        // Do a BFS from source to sink and compute the depth/level of each node
        // which is the minimum number of edges from that node to the source.
        private boolean bfs() {
            Arrays.fill(level, -1);
            q = new ArrayBlockingQueue<>(n);
            q.offer(s);
            level[s] = 0;

            int numThreads = 16;
            ExecutorService service = Executors.newFixedThreadPool(numThreads);
            for (int i = 0; i < numThreads; i++) {
                service.submit(new BFSHelper());
            }

            service.shutdown();

            try {
                service.awaitTermination(Long.MAX_VALUE, TimeUnit.MINUTES);
            } catch (Throwable e) {
                System.out.println(e);
            }

            // Return whether we were able to reach the sink node.
            return level[t] != -1;
        }

        static class BFSHelper extends DinicsParallelBFSDFS implements Runnable {
            public void run() {
                boolean firstEntry = true;
                while (firstEntry || runIndex.equals(new AtomicInteger(0))) {

                    // System.out.println(Thread.currentThread().getId());
                    firstEntry = false;
                    try {
                        runIndex.getAndIncrement();
                        while (q.size() > 0) {
                            int node = q.poll();
                            for (Edge edge : graph[node]) {
                                long cap = edge.remainingCapacity();
                                if (cap > 0 && level[edge.to] == -1) {
                                    level[edge.to] = level[node] + 1;
                                    q.offer(edge.to);
                                }
                            }
                        }
                    } finally {
                        runIndex.getAndDecrement();
                    }

                }
            }
        }

    }

    public static void main(String[] args) {
        System.out.println("Enter dataset path:");
        Scanner scan = new Scanner(System.in); // Create a Scanner object
        String filename = scan.nextLine();

        scan.close();
        File myObj = new File(filename);
        Scanner kb;
        try {
            kb = new Scanner(myObj);
            int n = kb.nextInt();
            int s = 0;
            int t = n - 1;
            lockNode = new AtomicIntegerArray(n);

            NetworkFlowSolverBase solver;
            solver = new DinicsSolver(n, s, t);

            while (kb.hasNext()) {
                solver.addEdge(kb.nextInt(), kb.nextInt(), kb.nextInt());
            }

            // Prints: "Maximum flow: 30"

            long timeStart = System.currentTimeMillis();
            System.out.printf("Maximum flow: %d\n", solver.getMaxFlow());
            long timeEnd = System.currentTimeMillis();
            System.out.println("Execution time: " + (timeEnd - timeStart) + "ms");
            kb.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}