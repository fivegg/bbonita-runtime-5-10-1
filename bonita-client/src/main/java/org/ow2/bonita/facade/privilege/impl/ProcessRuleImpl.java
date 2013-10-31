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
package org.ow2.bonita.facade.privilege.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;

/**
 * @author Nicolas Chabanoles
 * 
 */
public class ProcessRuleImpl extends RuleImpl {

  private static final long serialVersionUID = -1096929781625084230L;

  protected ProcessRuleImpl() {
    super();
  }
  
  public ProcessRuleImpl(RuleType ruleType) {
    super(ruleType);
  }
  
  public ProcessRuleImpl(String name, String label, String description, RuleType ruleType, Set<ProcessDefinitionUUID> processes) {
    super(name, label, description, ruleType, processes);
  }

  public ProcessRuleImpl(ProcessRuleImpl src) {
    super(src);
  }
  
  public void addProcesses(Collection<ProcessDefinitionUUID> processes) {
    super.addExceptions(processes);
  }
  
  public void removeProcesses(Set<ProcessDefinitionUUID> processes) {
    super.removeExceptions(processes);
  }

  public void setProcesses(Set<ProcessDefinitionUUID> processes) {
    super.setExceptions(processes);
  }
  
  public Set<ProcessDefinitionUUID> getProcesses() {
    
    Set<ProcessDefinitionUUID> processUUIDs = new HashSet<ProcessDefinitionUUID>();
    for (String definitionUUID : getItems()) {
      processUUIDs.add(new ProcessDefinitionUUID(definitionUUID));
    }
    return processUUIDs;
  }
}
