import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class FordFulkersonParallel {
    // labels of nodes
    public static NodeFF[] nodeLabels;

    // Stores graph.
    public static int NUM_THREAD = 8;
    public static int[][] cap;
    public static int[][] flow;
    public static int numNodes;
    public static int source = 0;
    public static int sink;
    public static ReentrantLock lock = new ReentrantLock();
    public static AtomicInteger column = new AtomicInteger(0);
    public static AtomicBoolean isAugmented = new AtomicBoolean(false);
    public static AtomicInteger waitForThreads = new AtomicInteger(0);
    public static CyclicBarrier barrier;
    public static boolean isReady;

    public static void main(String[] args) {
        initialize();
        // printGraph();
        final long startTime = System.currentTimeMillis();
        multiThreadFF();
        final long endTime = System.currentTimeMillis();
        // totalTime += endTime - startTime;
        // printGraph(flow);
        long executionTime = endTime - startTime;
        System.out.println(executionTime + " milliseconds to finish Ford-Fulkerson.");
    }

    public static void barrierWait() {
        try {
            barrier.await();
        } catch (InterruptedException | BrokenBarrierException e) {
            // e.printStackTrace();
            System.err.println("A sensor was too slow at an iteration");
        }
    }

    public static void initialize() {
        String filename = getInput();

        if (filename.length() > 0)
            createGraph(filename);
        else
            System.exit(1);
    }

    public static String getInput() {
        System.out.println("Enter dataset path:");
        Scanner scan = new Scanner(System.in); // Create a Scanner object
        String filename = scan.nextLine();

        System.out.println(filename + " " + numNodes + ":" + sink);
        scan.close();

        return filename;
    }

    public static void createGraph(String filename) {
        try {
            File myObj = new File(filename);
            Scanner myReader = new Scanner(myObj);
            numNodes = myReader.nextInt();
            // System.out.println(numNodes);
            sink = numNodes - 1;

            cap = new int[numNodes][numNodes];
            flow = new int[numNodes][numNodes];

            while (myReader.hasNext()) {
                int fromNode = myReader.nextInt();
                int toNode = myReader.nextInt();
                int capacity = myReader.nextInt();

                // System.out.println(fromNode + " --" + capacity + "-> " + toNode);
                // Thread.sleep(1000);
                add(fromNode, toNode, capacity);
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    public static void printGraph(int[][] graph) {
        for (int i = 0; i < graph.length; i++) {
            for (int j = 0; j < graph[i].length; j++) {
                System.out.print(graph[i][j] + " ");
            }
            System.out.println();
        }
    }

    // Adds an edge from v1 -> v2 with capacity c.
    public static void add(int v1, int v2, int c) {
        cap[v1][v2] = c;
    }

    public static int getMaxFlow() {
        int maxFlow = 0;

        for (int i = 0; i <= sink; i++)
            maxFlow += flow[i][sink];

        return maxFlow;
    }

    public static void multiThreadFF() {
        Thread[] threads;

        // Limiting the amount of threads depending
        // on however many total nodes that we have
        if (NUM_THREAD > numNodes)
            NUM_THREAD = numNodes;

        barrier = new CyclicBarrier(NUM_THREAD);
        threads = new Thread[NUM_THREAD];

        for (int i = 0; i < NUM_THREAD; i++) {
            threads[i] = new Thread(new Traverse(i));
            threads[i].start();
        }

        isReady = true;

        try {
            // Wait for Threads to finish
            for (int i = 0; i < NUM_THREAD; i++)
                threads[i].join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println(getMaxFlow());
    }
}

class Traverse extends FordFulkersonParallel implements Runnable {
    // Identifying Thread Number
    private int tNum;

    // Ensure that it loops through the nodes twice
    private int colLimit = numNodes * 2;

    private int deviation = numNodes / NUM_THREAD;
    public Traverse(int tNum) {
        this.tNum = tNum;
    }

    public boolean iterateMultiFF() {

        // Outer for loop cycles through columns (To Node)
        for (int col = tNum * deviation, c = 0; c < colLimit; col = (col + 1) % numNodes, c++) {
            // Inner for loop cycles through row. (From Node)
            for (int row = 0; row < numNodes - 1; row++) {

                // ensure prev node has a label
                if (nodeLabels[row] != null) {

                    // (forward) if this node is not labeled (null) and flow is < cap
                    if ((nodeLabels[col] == null) && flow[row][col] < cap[row][col])
                        label(row, true, col);

                    // (backward) if this node not labeled and flow is > 0
                    if ((nodeLabels[col] == null) && flow[col][row] > 0)
                        label(row, false, col);

                }
            }

            // if true, make all threads wait until augmenting is finished
            if (nodeLabels[sink] != null) {
                barrierWait();

                if (isAugmented.compareAndSet(false, true)) {
                    augment();

                    resetLabels();
                    isAugmented.set(false);
                }

                barrierWait();

                return true;
            }
        }

        return false; // Did not augment
    }

    public static void resetLabels() {
        nodeLabels = new NodeFF[numNodes];
        nodeLabels[0] = new NodeFF();
    }

    public static int min(int a, int b) {
        return (a < b) ? a : b;
    }

    public static void label(int prev, boolean add, int curr) {
        int pFlow;

        if (add)
            pFlow = min(nodeLabels[prev].potentialFlow, cap[prev][curr] - flow[prev][curr]);
        else
            pFlow = min(nodeLabels[prev].potentialFlow, flow[curr][prev]);

        nodeLabels[curr] = new NodeFF(prev, add, curr, pFlow);
    }

    public static void augment() {
        // tN - traverseNode
        NodeFF tN = nodeLabels[sink]; // Start at sink
        if (tN == null)
            return;
        int augNum = tN.potentialFlow;

        // while haven't augmented source node yet
        while (tN.thisNode != 0) {
            if (tN.add)
                flow[tN.prevNode][tN.thisNode] += augNum;
            else
                flow[tN.thisNode][tN.prevNode] -= augNum;

            tN = nodeLabels[tN.prevNode];
        }
    }

    public void run() {
        resetLabels();
        barrierWait();

        // if one thread returns, then all need to as well
        while (iterateMultiFF())
            ;
    }
}