
public class FordFulkersonParallel extends FordFulkerson {
    // Identifying Thread Number
    private int tNum;

	// Ensure that it loops through the nodes twice
    private int colLimit = numNodes / NUM_THREAD * 2;
    // Should do numNodes instead?

    public FordFulkersonParallel(int tNum) {
        this.tNum = tNum;
    }

    public void iterateMultiFF() {

        // Outer for loop cycles through columns (To Node)
        for (int col = tNum, c = 0; c < colLimit; col = (col + NUM_THREAD) % numNodes, c++) 
		{
            // Inner for loop cycles through row. (From Node)
            for (int row = 0; row < numNodes; row++) 
			{
				// ensure prev node has a label
				if (nodeLabels[row] != null)
				{
					// if this node is not labeled (null) and flow is < cap
					if ((nodeLabels[col] == null) && flow[row][col] < cap[row][col])
						label(row, true, col);

					// if this node not labeled and flow is > 0
					if ((nodeLabels[col] == null) && flow[col][row] > 0)
						label(row, false, col);
				}
            }

            barrierWait();
            // Once we hit the sink node, augment w/ labels
        }
    }

    public void run() {
        resetLabels();
        barrierWait();
        System.out.println(tNum);

        // if one thread returns, then all need to as well
        iterateMultiFF();

    }
}