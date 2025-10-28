package csx55.overlay.spanning;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class MinimumSpanningTree {
  
  public class Neighbor {
    private final String id;
    private final int weight;

    public Neighbor(String id, int weight) {
      this.id = id;
      this.weight = weight;
    }

    public String getID() {
      return this.id;
    }

    public int getWeight() {
      return this.weight;
    }
  }

  public class Node {

    private Set<Neighbor> neighbors;
    private String node;

    public Node(String node, Set<String> edgeWeights) {
      this.node = node;
      this.neighbors = this.computeNeighbors(edgeWeights);
    }

    public String getID() {
      return this.node;
    }

    public Set<Neighbor> getNeighbors() {
      return this.neighbors;
    }

    private Set<Neighbor> computeNeighbors(Set<String> edgeWeights) {
      Set<Neighbor> nb = new HashSet<>();
      String[] parts;
      for (String line : edgeWeights) {
        parts = line.trim().split("\\s+");
        if (parts[0].equals(this.node)) {
          Neighbor neighbor = new Neighbor(parts[1], Integer.parseInt(parts[2]));
          nb.add(neighbor);
        } else if (parts[1].equals(this.node)) {
          Neighbor neighbor = new Neighbor(parts[0], Integer.parseInt(parts[2]));
          nb.add(neighbor);
        }
      }
      return nb;
    }

    @Override
    public boolean equals(Object o) {
      if (o instanceof Node) {
        Node nd = (Node) o;
        return this.getID().equals(nd.getID());
      } else {
        return false;
      }
    }

    @Override
    public int hashCode() {
      return java.util.Objects.hash(this.node);
    }
  }
  
  private Set<String> mst;
  private String[] orderedMST;
  private Set<String> edgeWeights;
  private Set<Node> inMST;
  private Set<Node> notInMST;
  private Node root;

  public MinimumSpanningTree(String rootString, Set<String> edgeWeights) throws IllegalStateException {
    if (edgeWeights == null || edgeWeights.isEmpty())
      throw new IllegalStateException("Cannot create MST with missing or empty edgeWeights");
    this.mst = new HashSet<>();
    this.edgeWeights = edgeWeights;
    this.inMST = new HashSet<>();
    this.notInMST = new HashSet<>();
    this.root = new Node(rootString, this.edgeWeights);
    this.notInMST.add(this.root);
    this.configureOperation();
    this.compute();
    this.orderedMST = this.sortMST();
  }

  public String[] getOrderedMST() {
    return this.orderedMST;
  }

  // returns a random node to start from
  private void configureOperation() throws IllegalStateException {
    String[] parts;
    for (String line : this.edgeWeights) {
      parts = line.trim().split("\\s+");
      if (this.getNodeByID(parts[0], this.notInMST) == null) {
        this.notInMST.add(new Node(parts[0], this.edgeWeights));
      }
      if (this.getNodeByID(parts[1], this.notInMST) == null) {
        this.notInMST.add(new Node(parts[1], this.edgeWeights));
      }
    }
  }

  private void compute() {
    this.inMST.add(this.root);
    this.notInMST.remove(this.root);
    Set<Neighbor> validContinuations = new HashSet<>();
    
    while (this.notInMST.size() > 0) {
      for (Node node : this.inMST) {                                         // collect all valid continuation candidates
        validContinuations.addAll(node.getNeighbors());
      }
      int min = Integer.MAX_VALUE;
      Neighbor bestNeighbor = null;
      for (Neighbor continuation : validContinuations) {
        if (this.getNodeByID(continuation.getID(), this.inMST) != null) continue;
        if (continuation.getWeight() < min) {                                 // find lowest weight
          min = continuation.getWeight();
          bestNeighbor = continuation;                                        // record neighbor with lowest weight
        }
      }
      for (Node node : new HashSet<>(this.inMST)) {                           // get the neighbor that connects to the best neighbor
        if (node.getNeighbors().contains(bestNeighbor)) {
          this.mst.add(this.nodeNeighborFormattedLink(node, bestNeighbor));
          Node newNode = this.getNodeByID(bestNeighbor.getID(), this.notInMST);
          this.inMST.add(newNode);
          this.notInMST.remove(newNode);
        }
      }
      validContinuations.clear();                                             // reset continuation candidates
    }
  }

  private String nodeNeighborFormattedLink(Node node, Neighbor neighbor) {
    return node.getID() + ", " + neighbor.getID() + ", " + neighbor.getWeight();
  }

  
  public void printMST() {
    for (String line : this.orderedMST) {
      System.out.println(line);
    }
  }
  
  private String[] sortMST() {
    String rootString = this.root.getID();
    Set<String> mstCopy = new HashSet<>(this.mst);
    Set<String> mstCopy2 = new HashSet<>(this.mst);
    ArrayList<String> noodles = new ArrayList<>(); // think of better name later, im tired - sorted nodes, but i kinda like noodles now
    String[] parts;
    noodles.add(rootString);
    while (!mstCopy.isEmpty()) { // snapshot after each full pass
      for (String line : new HashSet<>(mstCopy)) {
        parts = line.trim().split(", ");
        if (noodles.contains(parts[0])) { // add the neighbors of the sorted nodes
          noodles.add(parts[1]);
          mstCopy.remove(line);
        } else if (noodles.contains(parts[1])) {
          noodles.add(parts[0]);
          mstCopy.remove(line);
        }
      }
    }
    String[] orderedMST = new String[this.mst.size()];
    int count = 0;
    for (int i = 0; i < noodles.size(); i++) {
      for (String line : new HashSet<>(mstCopy2)) {
        if (line.contains(noodles.get(i))) {
          orderedMST[count++] = line;
          mstCopy2.remove(line);
        }
      }
    }
    return orderedMST;
  }
  
  private Node getNodeByID(String id, Set<Node> nodes) {
    for (Node node : nodes) {
      if (node.getID().equals(id)) return node;
    }
    return null;
  }

  // public void computeAL() { // compute adjacency list
  //   String rootString = this.root.getID();
  //   Set<String> mstCopy = new HashSet<>(this.mst);
  //   ArrayList<ArrayList<String> > al = new ArrayList<>();
  //   Set<String> noodles = new HashSet<>(); // set because order here doesnt matter
  //   noodles.add(rootString);
  //   String[] parts;
  //   for (String line : new HashSet<>(mstCopy)) {
  //     parts = line.split(", ");
  //     if (noodles.contains(parts[0])) {
  //       noodles.add(parts[1]); // add neighbor of a noodles node
  //       mstCopy.remove(line);
  //     } else if (noodles.contains(parts[1])) {
  //       noodles.add(parts[0]);
  //       mstCopy.remove(line);
  //     }
  //   }
  // }

  public String getPath(String from, String to) {
    Node src = this.getNodeByID(from, this.inMST);
    Node dest = this.getNodeByID(to, this.inMST);
    Set<String> visited = new HashSet<>();
    visited.add(from);

    String path = "" + getPathHelper(src, dest, visited);

    return path;
  }

  private String getPathHelper(Node src, Node dest, Set<String> visited) {
    for (Neighbor neighbor : src.getNeighbors()) {
      Node nbor = this.getNodeByID(neighbor.getID(), this.inMST);
      if (nbor.equals(dest)) return nbor.getID();
      if (visited.contains(nbor.getID())) { // if we already visited skip
        continue;
      } else {
        visited.add(nbor.getID());
        return nbor.getID() + " " + getPathHelper(nbor, dest, visited);
      }
    }
    return "";
  }

  public static void main(String[] args) {
    Set<String> edgeWeights = new HashSet<>();
    edgeWeights.add("A B 2");
    edgeWeights.add("B R 2");
    edgeWeights.add("X B 2");
    edgeWeights.add("B C 1");
    edgeWeights.add("C D 4");
    edgeWeights.add("D E 2");
    edgeWeights.add("E F 4");
    edgeWeights.add("A F 3");

    MinimumSpanningTree mst = new MinimumSpanningTree("C", edgeWeights);
    mst.printMST();
    System.out.println(mst.getPath("C", "E"));
  }
}
