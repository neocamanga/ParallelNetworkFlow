/*
   * Java Implementation of Edmonds-Karp Algorithm *
   * By: Pedro Contipelli                          *
   * Edited for use by: Glenn Eric Hartwell		   *
   * 											   *
   * All inputs should be read in from some sort   *
   * text file.									   *

 Input Format:                                     (Sample Input)

 N , E         | (N total nodes , E total edges) |  4 5
 u1 , v1 , c1  |                                 |  0 1 1000
 u2 , v2 , c2  | Each line u , v , c represents  |  1 2 1
 u3 , v3 , c3  | an edge in the graph from node  |  0 2 1000
  ...          | u to node v with capacity C     |  1 3 1000
 uE , vE , cE  |                                 |  2 3 1000
 
 Nodes 0 and N-1 are assumed to be the source and sink (respectively).
*/

import java.util.*;
import java.io.*;

public class SeqEdmondsKarp
{
	public static void main(String[] args) 
	{
		Scanner fpScanner = new Scanner(System.in);
		
		System.out.print("Please enter your graph file name: ");
		String filename = fpScanner.nextLine();
		Scanner scan;
		try
		{
			scan = new Scanner(new File(filename));
			fpScanner.close();
		}
		catch (Exception e)
		{
			System.out.println("Looks like we couldn't find your file. Make sure it is in your current directory and run this program again.");
			return;
		}
		
		int nodes = scan.nextInt();
		int source = 0;
		int sink = nodes - 1;

		Node[] graph = new Node[nodes];

		// Initialize each node
		for (int i = 0; i < nodes; i++)
			graph[i] = new Node();

		// // Initialize each edge
		// for (int i = 0; i < edges; i++) {
		// 	int u = scan.nextInt();
		// 	int v = scan.nextInt();
		// 	int c = scan.nextInt();
			
		// 	// Note edge "b" is not actually in the input graph
		// 	// It is a construct that allows us to solve the problem
		// 	Edge a = new Edge(u , v , 0 , c);
		// 	Edge b = new Edge(v , u , 0 , 0);
			
		// 	// Set pointer from each edge "a" to
		// 	// its reverse edge "b" and vice versa
		// 	a.setReverse(b);
		// 	b.setReverse(a);
			
		// 	graph[u].edges.add(a);
		// 	graph[v].edges.add(b);
		// }
		
		// No need for edge count, just number of nodes
		// Initialize each edge
		while (scan.hasNext()) 
		{
			int u = scan.nextInt();
			int v = scan.nextInt();
			int c = scan.nextInt();
			
			// Note edge "b" is not actually in the input graph
			// It is a construct that allows us to solve the problem
			Edge a = new Edge(u , v , 0 , c);
			Edge b = new Edge(v , u , 0 , 0);
			
			// Set pointer from each edge "a" to
			// its reverse edge "b" and vice versa
			a.setReverse(b);
			b.setReverse(a);
			
			graph[u].edges.add(a);
			graph[v].edges.add(b);
		}

		int maxFlow = 0;

		while (true)
		{
			// Parent array used for storing path
			// (parent[i] stores edge used to get to node i)
			Edge[] parent = new Edge[nodes];
			
			Queue<Node> q = new ArrayDeque<>();
			q.add(graph[source]);
			
			// BFS finding shortest augmenting path
			while (!q.isEmpty()) {
				Node curr = q.remove(); 
				
				// Checks that edge has not yet been visited, and it doesn't
				// point to the source, and it is possible to send flow through it. 
				for (Edge e : curr.edges)
					if (parent[e.t] == null && e.t != source && e.capacity > e.flow) {
						parent[e.t] = e;
						q.add(graph[e.t]);
					}
			}
				
			// If sink was NOT reached, no augmenting path was found.
			// Algorithm terminates and prints out max flow.
			if (parent[sink] == null)
				break;
			
			// If sink WAS reached, we will push more flow through the path
			int pushFlow = Integer.MAX_VALUE;
			
			// Finds maximum flow that can be pushed through given path
			// by finding the minimum residual flow of every edge in the path
			for (Edge e = parent[sink]; e != null; e = parent[e.s])
				pushFlow = Math.min(pushFlow , e.capacity - e.flow);
			
			// Adds to flow values and subtracts from reverse flow values in path
			for (Edge e = parent[sink]; e != null; e = parent[e.s]) {
				e.flow += pushFlow;
				e.reverse.flow -= pushFlow;
			}
			
			maxFlow += pushFlow;
		}

		System.out.println("Max Flow: " + maxFlow);

		scan.close();
	}
}

// No explicit constructor is necessary :P

class Node
{

	// List of edges also includes reverse edges that
	// are not in original given graph (for push-back flow)
	ArrayList<Edge> edges = new ArrayList<>();

}

class Edge
{
	
	int s, t, flow, capacity;
	Edge reverse;

	public Edge(int s, int t, int flow, int capacity)
	{
		this.s = s;
		this.t = t;
		this.flow = flow;
		this.capacity = capacity;
	}

	public void setReverse(Edge e)
	{
		reverse = e;
	}

}