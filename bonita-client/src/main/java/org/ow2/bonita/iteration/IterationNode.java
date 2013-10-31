package org.ow2.bonita.iteration;

import java.util.HashSet;
import java.util.Set;

public class IterationNode implements Comparable<IterationNode> {

  public static enum JoinType {
    AND, XOR
  }

  public static enum SplitType {
    AND, XOR
  }

  private final String name;
  private JoinType joinType;
  private SplitType splitType;

  private final Set<IterationTransition> incomingTransitions = new HashSet<IterationTransition>();
  private final Set<IterationTransition> outgoingTransitions = new HashSet<IterationTransition>();

  public IterationNode(final String name) {
    super();
    this.name = name;
  }

  public IterationNode(final String name, final JoinType joinType, final SplitType splitType) {
    this(name);
    this.joinType = joinType;
    this.splitType = splitType;
  }

  public String getName() {
    return name;
  }

  public Set<IterationTransition> getOutgoingTransitions() {
    return outgoingTransitions;
  }

  public Set<IterationTransition> getIncomingTransitions() {
    return incomingTransitions;
  }

  public void addIncomingTransition(final IterationTransition t) {
    incomingTransitions.add(t);
  }

  public void addOutgoingTransition(final IterationTransition t) {
    outgoingTransitions.add(t);
  }

  public void removeOutgoingTransition(final IterationTransition transition) {
    outgoingTransitions.remove(transition);
  }

  public void removeIncomingTransition(final IterationTransition transition) {
    incomingTransitions.remove(transition);
  }

  public boolean hasOutgoingTransitions() {
    return !outgoingTransitions.isEmpty();
  }

  public boolean hasIncomingTransitions() {
    return !incomingTransitions.isEmpty();
  }

  public JoinType getJoinType() {
    return joinType;
  }

  public SplitType getSplitType() {
    return splitType;
  }

  @Override
  public String toString() {
    return "IterationNode [name=" + name + ", joinType=" + joinType + ", splitType=" + splitType + "]";
  }

  @Override
  public int compareTo(final IterationNode anotherIterationNode) {
    return toString().compareTo(anotherIterationNode.toString());
  }

}
