package csx55.overlay.util;

import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;

/*
 * 
 * ALERT: THIS WHOLE FILE IS BROKEN. DO NOT USE THIS. KEEPING AS BACKUP
 */
public class AdjacencyList {

  private String root;
  private String[] mstFromRoot;
  private ArrayList<ArrayList<String> > adjacencyList;
  private Set<String> neighbors;

  public AdjacencyList(String root, String[] mstFromRoot, Set<String> neighbors) {
    this.neighbors = neighbors;
    this.mstFromRoot = mstFromRoot;
    ArrayList<String> emptyBranch = new ArrayList<>();
    this.root = root;
    this.adjacencyList = this.computeAdjacencyList(0, emptyBranch);
  }

  public ArrayList<ArrayList<String> > computeAdjacencyList(int start, ArrayList<String> branch) {
    if (!branch.isEmpty()) {
      branch.remove(branch.size() - 1);
    }
    ArrayList<ArrayList<String> > adjacencyList = new ArrayList<>();
    int len = this.mstFromRoot.length;
    String[] parts;
    String prev = null;
    for (int i = start; i < len; i++) {
      parts = this.mstFromRoot[i].trim().split(", ");
      if (prev == null) {
        prev = parts[1].equals(this.root) ? parts[0] : parts[1];
        continue;
      }
      if (prev.equals(parts[0])) { // for AB and BC, checks that B1 = B2 and if true appends C
        branch.add(parts[1]); // C
        prev = parts[1];
      } else if (prev.equals(parts[1])) {
        branch.add(parts[0]);
        prev = parts[0];
      } else {                     // for AB and CD, B != C so enact a new branch to traverse this way and skip
        adjacencyList.addAll(this.computeAdjacencyList(i, new ArrayList<>(branch))); // creates a new adjacencyList with one branch and merges the two
        break;
      }
    }
    adjacencyList.add(branch);
    return adjacencyList;
  }

  public void printAL() {
    for (ArrayList<String> alist : this.adjacencyList) {
      for (String str : alist) {
        System.out.printf(str);
      }
      System.out.println();
    }
  }

  public String next(String dest) {
    for (ArrayList<String> branch : this.adjacencyList) {
      for (String neighbor : this.neighbors) {
        if (branch.contains(dest) && branch.get(0).equals(neighbor)) {
          return neighbor;
        }
      }
    }
    this.printAL();
    System.out.println("Dest: " + dest);
    return null;
  }

  public static void main(String[] args) {
    String[] strs = {
      "A, B, 3",
      "B, C, 9",
      "C, D, 7",
      "D, G, 2",
      "D, E, 1",
      "E, F, 1",
      "E, S, 5",
      "S, U, 4",
    };
    Set<String> testSet = new HashSet<>();
    testSet.add("B");
    // testSet.add("C");
    AdjacencyList route = new AdjacencyList("A", strs, testSet);
    route.printAL();
    System.out.println(route.next("S"));
    System.out.println(route.next("G"));
    // System.out.println(route.next("A"));
  }
}