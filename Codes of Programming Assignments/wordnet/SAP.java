import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.StdIn;
import edu.princeton.cs.algs4.StdOut;

import edu.princeton.cs.algs4.Stack;
import edu.princeton.cs.algs4.Queue;
import edu.princeton.cs.algs4.Digraph;

public class SAP {
    private int[] cache;              // store recently v, w, length and ancestor
    private int length;               // length of the shortest path between V and W
    private int ancestor;             // the nearest ancestor of V and W
    private Digraph copyG;            // save the copy of associated digraph
    private int[] distTo1;            // distTo1[v] = length of shortest V->v path
    private int[] distTo2;            // distTo2[v] = length of shortest W->v path
    private boolean[] marked1;        // marked1[v] = is there an V->v path?
    private boolean[] marked2;        // marked2[v] = is there an W->v path?
    private Stack<Integer> Stack1;    // store changed auxiliary array1 entries
    private Stack<Integer> Stack2;    // store changed auxiliary array1 entries

    // constructor takes a digraph (not necessarily a DAG)
    public SAP(Digraph G) {
        cache = new int[4];
        copyG = new Digraph(G);
        distTo1 = new int[G.V()];
        distTo2 = new int[G.V()];
        marked1 = new boolean[G.V()];
        marked2 = new boolean[G.V()];
        Stack1 = new Stack<Integer>();
        Stack2 = new Stack<Integer>();
    }

    // length of shortest ancestral path between v and w; -1 if no such path
    public int length(int v, int w) {
        validateVertex(v);
        validateVertex(w);
        if ((cache[0] == v && cache[1] == w)
            || (cache[1] == v && cache[0] == w)) {
                return cache[2];
        }

        length = -1;
        ancestor = -1;
        cache[2] = length;
        cache[3] = ancestor;
        Queue<Integer> q1 = new Queue<Integer>();
        Queue<Integer> q2 = new Queue<Integer>();
        marked1[v] = true;
        marked2[w] = true;
        Stack1.push(v);
        Stack2.push(w);
        distTo1[v] = 0;
        distTo2[w] = 0;
        q1.enqueue(v);
        q2.enqueue(w);
        bfs(q1, q2);
        cache[0] = v;
        cache[1] = w;
        return length;   
    }

    // a common ancestor of v and w that participates in a shortest ancestral path;
    // -1 if no such path
    public int ancestor(int v, int w) {
        validateVertex(v);
        validateVertex(w);
        if ((cache[0] == v && cache[1] == w)
            || (cache[1] == v && cache[0] == w)) {
                return cache[3];
        }

        length = -1;
        ancestor = -1;
        cache[2] = length;
        cache[3] = ancestor;
        Queue<Integer> q1 = new Queue<Integer>();
        Queue<Integer> q2 = new Queue<Integer>();
        marked1[v] = true;
        marked2[w] = true;
        Stack1.push(v);
        Stack2.push(w);
        distTo1[v] = 0;
        distTo2[w] = 0;
        q1.enqueue(v);
        q2.enqueue(w);
        bfs(q1, q2);
        cache[0] = v;
        cache[1] = w;
        return ancestor;
    }

    // length of shortest ancestral path between any vertex in v and any vertex in
    // w; -1 if no such path
    public int length(Iterable<Integer> v, Iterable<Integer> w) {
        length = -1;
        ancestor = -1;
        validateVertices(v);
        validateVertices(w);
        Queue<Integer> q1 = new Queue<Integer>();
        Queue<Integer> q2 = new Queue<Integer>();
        for (int v1 : v) {
            Stack1.push(v1);
            marked1[v1] = true;
            distTo1[v1] = 0;
            q1.enqueue(v1);
        }
        for (int w1 : w) {
            Stack2.push(w1);
            marked2[w1] = true;
            distTo2[w1] = 0;
            q2.enqueue(w1);
        }
        bfs(q1, q2);
        return length;
    }

    // a common ancestor that participates in shortest ancestral path; -1 if no such
    // path
    public int ancestor(Iterable<Integer> v, Iterable<Integer> w) {
        length = -1;
        ancestor = -1;
        validateVertices(v);
        validateVertices(w);
        Queue<Integer> q1 = new Queue<Integer>();
        Queue<Integer> q2 = new Queue<Integer>();
        for (int v1 : v) {
            Stack1.push(v1);
            marked1[v1] = true;
            distTo1[v1] = 0;
            q1.enqueue(v1);
        }
        for (int w1 : w) {
            Stack2.push(w1);
            marked2[w1] = true;
            distTo2[w1] = 0;
            q2.enqueue(w1);
        }
        bfs(q1, q2);
        return ancestor;
    }

    // run two bfs alternating back and forth bewteen q1 and q2
    private void bfs(Queue<Integer> q1, Queue<Integer> q2) {
        while (!q1.isEmpty() || !q2.isEmpty()) {
            if (!q1.isEmpty()) {
                int v = q1.dequeue();
                if (marked2[v]) {
                    if (distTo1[v] + distTo2[v] < length || length == -1) {
                        ancestor = v;
                        length = distTo1[v] + distTo2[v];
                        cache[2] = length;
                        cache[3] = ancestor;
                    }
                }
                if (distTo1[v] < length || length == -1) {
                    for (int w : copyG.adj(v)) {
                        if (!marked1[w]) {
                            distTo1[w] = distTo1[v] + 1;
                            marked1[w] = true;
                            Stack1.push(w);
                            q1.enqueue(w);
                        }
                    }
                }
            }
            if (!q2.isEmpty()) {
                int v = q2.dequeue();
                if (marked1[v]) {
                    if (distTo2[v] + distTo2[v] < length || length == -1) {
                        ancestor = v;
                        length = distTo1[v] + distTo2[v];
                        cache[2] = length;
                        cache[3] = ancestor;
                    }
                }
                if (distTo2[v] < length || length == -1) {
                    for (int w : copyG.adj(v)) {
                        if (!marked2[w]) {
                            distTo2[w] = distTo2[v] + 1;
                            marked2[w] = true;
                            Stack2.push(w);
                            q2.enqueue(w);
                        }
                    }
                }
            }
        }
        init();    // reinitialize auxiliary array for next bfs
    }

    // init auxiliary array for bfs
    private void init() {
        while (!Stack1.isEmpty()) {
            int v = Stack1.pop();
            marked1[v] = false;
        }
        while (!Stack2.isEmpty()) {
            int v = Stack2.pop();
            marked2[v] = false;
        }
    }

    // throw an IllegalArgumentException unless {@code 0 <= v < V}
    private void validateVertex(int v) {
        int V = marked1.length;
        if (v < 0 || v >= V)
            throw new IllegalArgumentException("vertex " + v + " is not between 0 and " + (V - 1));
    }

    // throw an IllegalArgumentException unless {@code 0 <= v < V}
    private void validateVertices(Iterable<Integer> vertices) {
        if (vertices == null) {
            throw new IllegalArgumentException("argument is null");
        }
        int V = marked1.length;
        for (int v : vertices) {
            if (v < 0 || v >= V) {
                throw new IllegalArgumentException("vertex " + v + " is not between 0 and " + (V - 1));
            }
        }
    }

    // do unit testing of this class
    public static void main(String[] args) {
        In in = new In(args[0]);
        Digraph G = new Digraph(in);
        SAP sap = new SAP(G);

        while (!StdIn.isEmpty()) {
            int v = StdIn.readInt();
            int w = StdIn.readInt();
            int length = sap.length(v, w);
            int ancestor = sap.ancestor(v, w);
            StdOut.printf("length = %d, ancestor = %d\n", length, ancestor);
        }
    }
}