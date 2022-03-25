import java.io.File; // Import the File class
import java.io.FileNotFoundException; // Import this class to handle errors
import java.util.Scanner; // Import the Scanner class to read text files
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.*;

public class FordFulkerson implements Runnable {

	// labels of nodes
	static public Node[] nodeLabels;

	// Stores graph.
	static public int[][] cap;
	static public int[][] flow;
	static public int numNodes;
	static public int source = 0;
	static public int sink;
	static public int threadLimit = 8;
	static public ReentrantLock lock = new ReentrantLock();
	static public AtomicInteger column = new AtomicInteger(0);
	static public AtomicBoolean isAugmented = new AtomicBoolean(false);
	static public AtomicInteger waitForThreads = new AtomicInteger(0);
	static public boolean isReady;

	public static void main(String[] args) {
		initialize();
		// printGraph();
		singleThreadFF();
		// multiThreadFF();
	}

	public void run()
	{
		;
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

		// System.out.println("Enter number of nodes (includes source and sink):");
		// Scanner nodeInput = new Scanner(System.in); // Create a Scanner object
		// numNodes = nodeInput.nextInt();
		// numNodes = scan.nextInt();

		// System.out.println("Enter the thread limit (defaults to 8):");
		// Scanner threadInput = new Scanner(System.in); // Create a Scanner object
		// threadLimit = threadInput.nextInt();

		// threadLimit = scan.nextInt();

		System.out.println(filename + " " + numNodes + ":" + sink);
		scan.close();
		// fileInput.close();
		// nodeInput.close();
		// threadInput.close();

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

	public static void printGraph() {

		for (int i = 0; i < cap.length; i++) {
			for (int j = 0; j < cap[i].length; j++) {
				System.out.print(cap[i][j] + " ");
			}
			System.out.println();
		}
	}

	// Adds an edge from v1 -> v2 with capacity c.
	public static void add(int v1, int v2, int c) {
		cap[v1][v2] = c;
	}

	public static void singleThreadFF() {
		while (iterateFF()) {
		}


		System.out.println(getMaxFlow());
	}

	public static int getMaxFlow() {
		int maxFlow = 0;

		for (int i = 0; i <= sink; i++)
			maxFlow += flow[i][sink];
		
		return maxFlow;
	}

	public static void multiThreadFF() {
		Thread[] threads;

		// Limiting the amount of threads depending on however many total nodes that we have
		if (threadLimit > numNodes)
			threadLimit = numNodes;

		threads = new Thread[threadLimit];

		for (int i = 0; i < numNodes; i++) {
			threads[i] = new Thread(new FFThread(i));
			threads[i].start();
		}

		isReady = true;

		while (threads[0].isAlive()) { }

		System.out.println(getMaxFlow());
	}

	public static boolean iterateFF() {

		resetLabels();

		// Loop through twice to ensure if augmented
		for (int l = 0; l < 2; l++) {
			// Starts on source row. This allows us to label other nodes
			for (int i = 0; i < numNodes; i++) {

				// Once this for-loop ends, we reach the sink node
				for (int j = 0; j < numNodes; j++) {
					// ensure prev node has a label
					if (nodeLabels[i] != null) {
						// if this node is not labeled (null) and flow is < cap
						if ((nodeLabels[j] == null) && flow[i][j] < cap[i][j])
							label(i, true, j);

						// if this node not labeled and flow is > 0
						if ((nodeLabels[j] == null) && flow[j][i] > 0)
							label(i, false, j);
					}
				}

				// if true, make all threads wait until augmenting is finished
				if (nodeLabels[sink] != null) {
					augment();
					return true;
				}
			}
		}

		return false; // Did not augment
	}

	public static void resetLabels() {
		nodeLabels = new Node[numNodes];
		nodeLabels[0] = new Node();
	}

	public static void label(int i, boolean add, int j) {
		int pFlow;

		if (add) {
			pFlow = min(nodeLabels[i].potentialFlow, cap[i][j] - flow[i][j]);
		} else {
			pFlow = min(nodeLabels[i].potentialFlow, flow[j][i]);
		}

		nodeLabels[j] = new Node(i, add, j, pFlow);
	}

	public static int min(int a, int b) {
		return (a < b) ? a : b;
	}

	public static void augment() {
		// tN - traverseNode
		Node tN = nodeLabels[sink]; // Start at sink
		int augNum = tN.potentialFlow;

		while (tN.thisNode != 0) {
			if (tN.add) {
				flow[tN.prevNode][tN.thisNode] += augNum;
			} else {
				flow[tN.thisNode][tN.prevNode] -= augNum;
			}

			tN = nodeLabels[tN.prevNode];
		}
	}
}