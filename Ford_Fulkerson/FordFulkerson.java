import java.io.File; // Import the File class
import java.io.FileNotFoundException; // Import this class to handle errors
import java.util.Scanner; // Import the Scanner class to read text files

class Node {
	int prevNode;
	boolean add; // True = will +; False = will -
	int potentialFlow;
}

public class FordFulkerson {

	// Stores graph.
	static public int[][] cap;
	static public int numNodes;
	static public int source = 0;
	static public int sink;
	static public int threadLimit = 8;

	// "Infinite" flow.
	final public static int oo = (int) (1E9);

	public static void main(String[] args) {
		initialize();
		printGraph();
	}

	public static void initialize() {
		String filename = getInput();

		if (filename.length() > 0)
			createGraph(filename);
		else
			System.exit(1);
	}

	public static String getInput() {
		System.out.println("Enter dataset filename:");
		Scanner fileInput = new Scanner(System.in); // Create a Scanner object
		String filename = fileInput.nextLine();

		System.out.println("Enter number of nodes (includes source and sink):");
		Scanner nodeInput = new Scanner(System.in); // Create a Scanner object
		numNodes = nodeInput.nextInt();
		cap = new int[numNodes][numNodes];
		sink = numNodes - 1;

		System.out.println("Enter the thread limit (defaults to 8):");
		Scanner threadInput = new Scanner(System.in); // Create a Scanner object
		threadLimit = threadInput.nextInt();

		System.out.println(filename + " " + numNodes + ":" + sink);
		fileInput.close();
		nodeInput.close();
		threadInput.close();

		return filename;
	}

	public static void createGraph(String filename) {
		try {
			File myObj = new File(filename);
			Scanner myReader = new Scanner(myObj);
			while (myReader.hasNextLine()) {
				int fromNode = myReader.nextInt();
				int toNode = myReader.nextInt();
				int capacity = myReader.nextInt();

				System.out.println(fromNode + " --" + capacity + "-> " + toNode);
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
}