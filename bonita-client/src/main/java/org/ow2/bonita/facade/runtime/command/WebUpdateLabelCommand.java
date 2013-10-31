/**
 * Copyright (C) 2009  BonitaSoft S.A.
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
package org.ow2.bonita.facade.runtime.command;

import java.util.Set;

import org.ow2.bonita.env.Environment;
import org.ow2.bonita.facade.APIAccessor;
import org.ow2.bonita.facade.impl.StandardAPIAccessorImpl;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.util.Command;

public class WebUpdateLabelCommand implements Command<Void> {

  private static final long serialVersionUID = 1L;

  private String ownerName;
  private Set<String> labelsToAdd;
  private Set<String> labelsToRemove;
  private Set<ProcessInstanceUUID> instanceUUIDs;
  
  public WebUpdateLabelCommand(String ownerName, Set<String> labelsToAdd,
      Set<String> labelsToRemove, Set<ProcessInstanceUUID> instanceUUIDs) {
    super();
    this.ownerName = ownerName;
    this.labelsToAdd = labelsToAdd;
    this.labelsToRemove = labelsToRemove;
    this.instanceUUIDs = instanceUUIDs;
  }


  public Void execute(Environment environment) throws Exception {
    for (String labelName : labelsToAdd) {
      addCasesToLabel(ownerName, labelName, instanceUUIDs);
    }
    for (String labelName : labelsToRemove) {
      removeCasesFromLabel(ownerName, labelName, instanceUUIDs);
    }
    return null;
  }
  

  private void addCasesToLabel(String ownerName, String labelName, Set<ProcessInstanceUUID> instanceUUIDs) {
    if (instanceUUIDs != null && !instanceUUIDs.isEmpty()) {
      final APIAccessor accessor = new StandardAPIAccessorImpl();
      accessor.getWebAPI().addCasesToLabel(ownerName, labelName, instanceUUIDs);
    }
  }

  public void removeCasesFromLabel(String ownerName, String labelName, Set<ProcessInstanceUUID> instanceUUIDs) {
    if (instanceUUIDs != null && !instanceUUIDs.isEmpty()) {
      final APIAccessor accessor = new StandardAPIAccessorImpl();
      accessor.getWebAPI().removeCasesFromLabel(ownerName, labelName, instanceUUIDs);
    }
  }
}
