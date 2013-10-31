package org.ow2.bonita.iteration;

import java.util.HashSet;
import java.util.Set;

public class IterationObject {

//List of entry nodes
  protected Set<String> entryNodes = new HashSet<String>();
  // List of exit nodes
  protected Set<String> exitNodes = new HashSet<String>();
  // List of nodes in the cycle
  protected Set<String> nodesInPath = new HashSet<String>();
  // List of transitions that are in the cycle
  protected Set<IterationTransition> transitions = new HashSet<IterationTransition>();

  @Override
  public String toString() {
    return "entryNodes: " + this.entryNodes + ", exitNodes: " + this.exitNodes + " path: " + this.nodesInPath + ", transitions: " + this.transitions;
  }
  
}
