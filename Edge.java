public class Edge {
    public Node from, to;
    public int weight;
    public boolean highlighted = false;

    public Edge(Node from, Node to, int weight) {
        this.from = from;
        this.to = to;
        this.weight = weight;
    }
}
