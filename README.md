# Network Flow Parallelization
This is a research project for COP_4520: Concepts of Parallel and Distributed Processing.

We are exploring how to parallelize common network flow algorithms and empirically test the benefits of making it parallel.

The following are the algorithms and the group members working on making it parallel:
### Ford-Fulkerson Algorithm
#### John Pham and Neo Camanga

### Edmonds-Karp Algorithm
#### Glenn Hartwell

### Dinics Algorithm
#### Brian Moon and Federico Baron

## Running the Program
The testing datasets 50v.in through 1000v.in come from <https://github.com/SumitPadhiyar/parallel_ford_fulkerson_gpu/tree/master/dataset>. An important thing to note about the datasets we are using is that the source node is 0 and the sink node is n-1. (n is number of nodes in the set)

To run a program, follow the following paradigm:
`$ java FordFulkerson.java`

It will ask for a path:
`$ ../dataset/50v.in`

### Expected Maxflow Output for following datasets:
- mytest1.in | 19
- mytest2.in | 7
- test-graph-1 | 2000
- test-graph-2 | 25
- test-graph-3 | 42
- 50v.in | 828
- 100v.in | 1251
- 500v.in | 7143
- 750v.in | 10930
- 1000v.in | 13476
- 2000v.in | 25027
- 3000v.in | 39170
- 4000v.in | 58517
- 5000v.in | 69349
- 10000v.in | 142610

# Ford-Fulkerson Results
| Nodes | Max Flow | Sequential Time | Parallel Time |
|-------|----------|-----------------|---------------|
| 50    | 828      | 5ms             |               |
| 100   | 1251     | 10ms            |               |
| 500   | 7143     | 182ms           |               |
| 750   | 10930    | 396ms           |               |
| 1000  | 13476    | 866ms           |               |
| 2000  | 25027    | 6532ms          |               |
| 3000  | 39170    | 18448ms         |               |
| 4000  | 58517    | 53893ms         |               |
| 5000  | 69349    | 117045ms        |               |
| 10000 | 142610   | 784219ms        |               |

# Edmonds-Karp Results

# Dinics Results

## READMEs
Please look into each README.md within each directory.
The READMEs for `Ford_Fulkerson`, `Edmonds_Karp`, and `Dinics` describe the respective
algorithms.
The README in `dataset` directory describes the experiments and results for each algorithm.
