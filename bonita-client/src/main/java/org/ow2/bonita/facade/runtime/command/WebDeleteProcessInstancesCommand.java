/**
 * Copyright (C) 2009-2012  BonitaSoft S.A.
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
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.util.Command;

public class WebDeleteProcessInstancesCommand implements Command<Void> {

  private static final long serialVersionUID = 1L;

  private final Set<ProcessInstanceUUID> instancesUUIDs;
  
  public WebDeleteProcessInstancesCommand(final Set<ProcessInstanceUUID> instancesUUIDs) {
    super();
    this.instancesUUIDs = instancesUUIDs;
  }

  @Override
  public Void execute(final Environment environment) throws Exception {
    final APIAccessor accessor = new StandardAPIAccessorImpl();
    if (instancesUUIDs != null && !instancesUUIDs.isEmpty()) {
      final WebAPI webAPI = accessor.getWebAPI();
      final Set<ProcessInstanceUUID> uuids = new HashSet<ProcessInstanceUUID>();
      final Set<String> instanceUuidsAsString = new HashSet<String>();
      for (final ProcessInstanceUUID uuid : instancesUUIDs) {
        uuids.add(uuid);
        instanceUuidsAsString.add(uuid.getValue());
        if (uuids.size() == 100) {
          webAPI.removeAllCasesFromLabels(uuids);
          uuids.clear();
          instanceUuidsAsString.clear();
        }
               
      }
      webAPI.removeAllCasesFromLabels(uuids);
      accessor.getRuntimeAPI().deleteProcessInstances(instancesUUIDs); 
    }
    return null;
  }
  
}
