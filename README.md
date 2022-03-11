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

It will ask for number of nodes:
`$ 50`

It will ask for thread limit:
`$ 8`

### Expected Maxflow Output for following datasets:
- mytest1.in | 19
- mytest2.in | 7
- 50v.in | 828
- 100v.in | 1251
- 500v.in | 7143
- 750v.in | 10930
- 1000v.in | 13476