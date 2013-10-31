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

import java.util.HashSet;
import java.util.Set;

import org.ow2.bonita.env.Environment;
import org.ow2.bonita.facade.APIAccessor;
import org.ow2.bonita.facade.WebAPI;
import org.ow2.bonita.facade.impl.StandardAPIAccessorImpl;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.light.LightProcessInstance;
import org.ow2.bonita.util.Command;

public class WebDeleteProcessCommand implements Command<Void> {

  private static final long serialVersionUID = -4049500711900576134L;
  private ProcessDefinitionUUID processUUID;

  public WebDeleteProcessCommand(ProcessDefinitionUUID processUUID) {
    super();
    this.processUUID = processUUID;
  }


  public Void execute(Environment environment) throws Exception {
    final APIAccessor accessor = new StandardAPIAccessorImpl();
    // get all parent instances that are of process with given UUID
    Set<LightProcessInstance> instances = accessor.getQueryRuntimeAPI().getLightProcessInstances(processUUID);
    // delete all cases of these instances (by group of 100 for perfs)
    final WebAPI webAPI = accessor.getWebAPI();

    Set<ProcessInstanceUUID> uuids = new HashSet<ProcessInstanceUUID>();

    for (LightProcessInstance instance : instances) {
      uuids.add(instance.getUUID());
      if (uuids.size() == 100) {
        webAPI.removeAllCasesFromLabels(uuids);
        uuids.clear();
      }
    }
    webAPI.removeAllCasesFromLabels(uuids);
    accessor.getManagementAPI().deleteProcess(processUUID);
    return null;
  }


}
