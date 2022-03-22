import java.util.concurrent.BrokenBarrierException;

public class FFThread extends FordFulkerson {

	private int tNum;

	public FFThread(int tNum) {
		this.tNum = tNum;
	}

	public boolean iterateMultiFF() {
		// System.out.println("io");
		// Starts on source row. This allows us to label other nodes
		for (int offset = 0; offset < numNodes; offset++) {
			int i = ((offset * threadLimit) + tNum) % numNodes;
			// Do This? compare later, but I think not
			// int i = (offset + tNum) % numNodes;

			column.set(0);

			// Once this for-loop ends, we reach the sink node
			for (int j = column.get(); column.get() < numNodes; j = column.get()) {

				// ensure prev node has a label
				if (nodeLabels[i] != null) {

					// if this node is not labeled (null) and flow is < cap
					if ((nodeLabels[j] == null) && flow[i][j] < cap[i][j] && FCFS.compareAndSet(false, true))
						label(i, true, j);

					// if this node not labeled and flow is > 0
					if ((nodeLabels[j] == null) && flow[j][i] > 0  && FCFS.compareAndSet(false, true))
						label(i, false, j);

				}

				// wait for all other threads. When that happens, continue for-loop
				try {
					barrier1.await();
					// System.out.println(Thread.currentThread() + " - " + j);
					j = column.getAndSet(j + 1) + 1; // Ensures all threads increment correctly
				} catch (InterruptedException | BrokenBarrierException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			// if true, make all threads wait until augmenting is finished
			if (nodeLabels[sink] != null) {
				// Only allow thread-0 to do augmenting
				if (tNum == 0) {
					// System.out.println(nodeLabels[sink]);
					// System.out.println(Thread.currentThread());
					augment();
					isAugmented.set(false);
					// System.out.println("Finish");
				}

				// wait for all other threads. When that happens, continue
				try {
					// System.out.println("Barrier1: " + barrier1.getNumberWaiting() + " - " + Thread.currentThread());
					barrier2.await();
					resetLabels();
				} catch (InterruptedException | BrokenBarrierException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				return true;
			}
		}

		return false; // Did not augment
	}

	public static void label(int i, boolean add, int j) {

		// lock.lock();

		int pFlow;

		if (add) {
			pFlow = min(nodeLabels[i].potentialFlow, cap[i][j] - flow[i][j]);
		} else {
			pFlow = min(nodeLabels[i].potentialFlow, flow[j][i]);
		}

		nodeLabels[j] = new Node(i, add, j, pFlow);

		FCFS.set(false);
		// lock.unlock();
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
				while (iterateMultiFF()) { }

				break;
			}
		}
	}
}
