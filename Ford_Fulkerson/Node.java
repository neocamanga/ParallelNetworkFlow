// THIS IS THE LABEL OF A NODE
public class Node {
	int prevNode;
	int thisNode;
	boolean add; // true = will +; false = will -
	int potentialFlow;

	// Constructor for source node
	public Node() {
		prevNode = -1; // source has no incoming flow
		thisNode = 0;
		add = true;
		potentialFlow = (int) (1E9); // "Infinite" flow.
	}

	// Overloaded constructor
	public Node(int i, boolean a, int j, int pFlow) {
		prevNode = i;
		thisNode = j;
		add = a;
		potentialFlow = pFlow;
	}
}
