package csx55.overlay.util;

import csx55.overlay.util.Edge;

import java.util.HashSet;
import java.util.Set;
import java.util.Random;
import java.util.HashMap;

public class OverlayCreator {

  private final int seed = 42;

  private HashMap<String, Integer> nodeCounts;
  private HashMap<Integer, Set<String> > countNodes;
  private Set<String> nodes;
  private Set<Edge> edges;
  private Random rand;
  private int k;
  private String[] sortedNodes;

  public OverlayCreator(Set<String> nodes, int k) {
    this.nodeCounts = new HashMap<>();
    this.countNodes = new HashMap<>(); // countNodes change
    this.k = k;
    this.configureCountNodes(k); // countNodes change
    this.nodes = nodes;
    this.edges = new HashSet<>();
    this.rand = new Random(this.seed);
    this.sortedNodes = new String[nodes.size()];
  }

  public void computeOverlay() {
    this.firstPass();
    if (this.k % 2 == 1)
      this.secondPass();
    this.greedyPass();
  }

  public Set<Edge> getOverlay() {
    return this.edges;
  }

  public Set<String> getOverlaySetString() {
    Set<String> overlay = new HashSet<>();
    for (Edge edge : this.edges) {
      overlay.add(edge.toString());
    }
    return overlay;
  }

  public void firstPass() {
    String first = null, prev = null;
    int i = 0;
    for (String node : this.nodes) {
      this.sortedNodes[i++] = node;
      if (prev == null) {
        first = node;
        prev = node;
        continue;
      }
      this.addEdge(prev, node, this.genWeight());
      prev = node;
    }
    this.addEdge(first, prev, this.genWeight());
  }

  public void secondPass() {
    int len = this.sortedNodes.length;
    for (int i = 0; i < len / 2; i++) {
      this.addEdge(this.sortedNodes[i],
                   this.sortedNodes[(i + len / 2) % len],
                   this.genWeight());
    }
  }

  public void greedyPass() {
    int count;
    Set<String> bucket;
    outer0:
    for (String node : this.nodes) {
      count = this.nodeCounts.get(node);
      if (count >= k) continue;                                    // if count already at k, skip
      for (int i = 2; i < k; i++) {                                // loop through all buckets from least connections up
        bucket = this.countNodes.get(i);
        if (bucket == null || bucket.isEmpty()) continue;          // if bucket is empty or doesnt exist, go to next bucket
          outer1:
          for (String n : new HashSet<>(bucket)) {                 // pick a target node, take a snapshot after each change to solve concurrency problem
            if (this.nodeCounts.get(node) >= k) continue outer0;   // if finished finding enough connections then grab a new source
            for (Edge edge : this.edges) {                         // search through edges
              if (!edge.containsNode(n)) continue;                 // if target node is not the edge, look at next edge
              if (edge.containsNode(node)) continue outer1;        // if target node already connected to our node, try a new target
            }
            this.addEdge(node, n, this.genWeight());               // after searching through all edges, if no edge between target and source exist, add a connection
          }                                                        // then move on to find another connection for target until it reaches k connections
      }
    }
  }

  /*
   * A B C where ABC are all kc = 3 and k = 4. AB -> A and B where A and B kc = 4, C where kc = 3. is this really an algorithmic problem??? is it an invariant problem
   */

  private void addEdge(String node1, String node2, int weight) {
    Edge edge = new Edge(node1, node2, weight);
    this.edges.add(edge);
    addEdgeHelper(node1);
    addEdgeHelper(node2);
  }

  // increments the node count if it contains it, otherwise adds the node to the map
  private void addEdgeHelper(String node) {
    int temp;
    if (this.nodeCounts.containsKey(node)) {
      temp = this.nodeCounts.get(node);
      this.nodeCounts.put(node, temp + 1); // node to count increment
      this.countNodes.get(temp).remove(node); // delete node from previous count key  -- countNodes change
      this.countNodes.get(temp + 1).add(node); // add node to next count key -- countNodes change
    } else {
      this.nodeCounts.put(node, 1);
      this.countNodes.get(1).add(node);
    }
  }

  private void configureCountNodes(int k) {
    for (int i = 1; i <= k; i++) {
      this.countNodes.put(i, new HashSet<String>());
    }
  }

  private int genWeight() {
    return rand.nextInt(10) + 1;
  }

  public static void main(String[] args) {
    Set<String> s = new HashSet<>();
    s.add("A");
    s.add("B");
    s.add("C");
    s.add("D");
    s.add("E");
    s.add("F");
    s.add("G");
    s.add("H");
    OverlayCreator oc = new OverlayCreator(s, 4);
  }
}