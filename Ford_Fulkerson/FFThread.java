import java.util.*;

public class FFThread extends FordFulkerson {

	private int tNum;

	public FFThread(int tNum) {
		this.tNum = tNum;
	}

	public boolean iterateMultiFF() {
		// Starts on source row. This allows us to label other nodes
		for (int i = tNum; i - tNum < numNodes; i = (i + 1) % threadLimit) {
			// Once this for-loop ends, we reach the sink node
			for (column.set(0); column.get() < numNodes; column.getAndIncrement()) {

				int j = column.get();

				// ensure prev node has a label
				if (nodeLabels[i] != null) {

					// if this node is not labeled (null) and flow is < cap
					if ((nodeLabels[j] == null) && flow[i][j] < cap[i][j])
						label(i, true, j);

					// if this node not labeled and flow is > 0
					if ((nodeLabels[j] == null) && flow[j][i] > 0)
						label(i, false, j);

				}

				
				// When thread hits the while loop, increment the
				// counter by 1 to denote num of thread getting here
				waitForThreads.incrementAndGet();
				
				// wait for all other threads. When that happens, continue for-loop
				while (waitForThreads.get() == numNodes - 1) {}
			}

			// if true, make all threads wait until augmenting is finished
			if (isAugmented.compareAndSet(false, true) && nodeLabels[sink] != null) {
				augment();

				resetLabels();
				return true;
			}
		}

		return false; // Did not augment
	}

	public static void resetLabels() {
		nodeLabels = new Node[numNodes];
		nodeLabels[0] = new Node();
	}

	public static void label(int i, boolean add, int j) {

		lock.lock();

		int pFlow;

		if (add) {
			pFlow = min(nodeLabels[i].potentialFlow, cap[i][j] - flow[i][j]);
		} else {
			pFlow = min(nodeLabels[i].potentialFlow, flow[j][i]);
		}

		nodeLabels[j] = new Node(i, add, j, pFlow);

		lock.unlock();
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

	public void run() {
		resetLabels();

		while (true) {
			if (isReady) {
				// Think this need to be in while loop
				// but if one thread returns, then all need to as well
				iterateMultiFF();
				break;
			}
		}
	}
}
