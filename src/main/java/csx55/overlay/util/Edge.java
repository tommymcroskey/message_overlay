package csx55.overlay.util;

public class Edge {

  private String node1;
  private String node2;
  private int weight;

  public Edge(String node1, String node2, int weight) {
    this.node1 = node1;
    this.node2 = node2;
    this.weight = weight;
  }

  public String toString() {
    return node1 + " " + node2 + " " + weight;
  }

  public boolean containsNode(String node) {
    return node.equals(this.node1) || node.equals(this.node2);
  }
}
