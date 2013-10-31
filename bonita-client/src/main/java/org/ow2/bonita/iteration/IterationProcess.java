package org.ow2.bonita.iteration;

import java.util.SortedMap;
import java.util.TreeMap;

public class IterationProcess {

  SortedMap<String, IterationNode> nodes = new TreeMap<String, IterationNode>();
  public void addNode(IterationNode node) {
    this.nodes.put(node.getName(), node);
  }
  public IterationNode getNode(String nodeName) {
    return nodes.get(nodeName);
  }
  public boolean hasNode(String name) {
    return nodes.containsKey(name);
  }
  public SortedMap<String, IterationNode> getNodes() {
    return nodes;
  }
  
  public SortedMap<String, IterationNode> getInitialActivities() {
    final SortedMap<String, IterationNode> result = new TreeMap<String, IterationNode>();
    for (IterationNode node : getNodes().values()) {
      if (!node.hasIncomingTransitions()) {
        result.put(node.getName(), node);
      }
    }
    return result;
  }
  
}
