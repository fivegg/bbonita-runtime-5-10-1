/**
 * Copyright (C) 2010  BonitaSoft S.A.
 * BonitaSoft, 31 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA  02110-1301, USA.
 **/
package org.ow2.bonita.facade.def.element.impl;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.ow2.bonita.util.CopyTool;

/**
 * Describes a cycle.
 *
 * @author Guillaume Porcher
 *
 */
public class IterationDescriptor implements Serializable, Comparable<IterationDescriptor> {
  
  private static final long serialVersionUID = -3306138069531809523L;
	/**
   * List of cycle nodes (descriptors)
   */
  protected Set<String> otherNodes;
  protected Set<String> entryNodes;
  protected Set<String> exitNodes;

  protected IterationDescriptor() { }
  
  public IterationDescriptor(final Set<String> otherNodes, final Set<String> entryNodes, final Set<String> exitNodes) {
  	this.otherNodes = otherNodes;
    this.entryNodes = entryNodes;
    this.exitNodes = exitNodes;
  }

  public IterationDescriptor(IterationDescriptor id) {
  	this.otherNodes = CopyTool.copy(id.getOtherNodes());
  	this.entryNodes = CopyTool.copy(id.getEntryNodes());
  	this.exitNodes = CopyTool.copy(id.getExitNodes());
  }

	private Set<String> getOtherNodes() {
		if (this.otherNodes == null) {
  		return Collections.emptySet();
  	}
  	return this.otherNodes;
  }

	public Set<String> getEntryNodes() {
  	if (this.entryNodes == null) {
  		return Collections.emptySet();
  	}
  	return this.entryNodes;
  }
  
  public Set<String> getExitNodes() {
  	if (this.exitNodes == null) {
  		return Collections.emptySet();
  	}
  	return this.exitNodes;
  }
  
  public boolean containsNode(final String node) {
    return getCycleNodes().contains(node);
  }
  
  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
    	return false;
    }
    if (!(obj instanceof IterationDescriptor)) {
    	return false;
    }
    IterationDescriptor other = (IterationDescriptor) obj;
    return other.getEntryNodes().equals(getEntryNodes()) && other.getExitNodes().equals(getExitNodes()) && other.getOtherNodes().equals(getOtherNodes());
  }
  
  @Override
  public int hashCode() {
  	final int prime = 31;
  	int result = 1;
  	result = prime * result
  			+ ((entryNodes == null) ? 0 : entryNodes.hashCode());
  	result = prime * result + ((exitNodes == null) ? 0 : exitNodes.hashCode());
  	result = prime * result
  			+ ((otherNodes == null) ? 0 : otherNodes.hashCode());
  	return result;
  }
  
  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder(this.getClass().getSimpleName());
    builder.append(": ").append("\nEntry nodes : ");
  	for (String st : getEntryNodes()) {
  	  builder.append(st).append(", ");
  	}
  	builder.append("\nExit nodes : ");
  	for (String st : getExitNodes()) {
  	  builder.append(st).append(", ");
  	}
  	builder.append("\nOther nodes : ");
  	for (String st : getOtherNodes()) {
  	  builder.append(st).append(", ");
  	}
    return builder.toString();
  }

	public Set<String> getCycleNodes() {
    Set<String> cycleNodes = new HashSet<String>();
    cycleNodes.addAll(getEntryNodes());
    cycleNodes.addAll(getExitNodes());
    cycleNodes.addAll(getOtherNodes());
	  return cycleNodes;
  }

	@Override
	public int compareTo(IterationDescriptor anotherDescriptor) {
		return this.toString().compareTo(anotherDescriptor.toString());
	}

}
