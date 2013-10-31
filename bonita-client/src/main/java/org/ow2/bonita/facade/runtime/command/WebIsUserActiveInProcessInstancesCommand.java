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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.ow2.bonita.env.Environment;
import org.ow2.bonita.facade.APIAccessor;
import org.ow2.bonita.facade.QueryRuntimeAPI;
import org.ow2.bonita.facade.impl.StandardAPIAccessorImpl;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.util.AccessorUtil;
import org.ow2.bonita.util.Command;

public class WebIsUserActiveInProcessInstancesCommand implements Command<Map<ProcessInstanceUUID, Boolean>> {

  private static final long serialVersionUID = -8687520379012488413L;
  private final Set<ProcessInstanceUUID> instanceUUIDs;
  private final String username;

  public WebIsUserActiveInProcessInstancesCommand(Set<ProcessInstanceUUID> instanceUUIDs, String username) {
    this.instanceUUIDs = instanceUUIDs;
    this.username = username;
  }

  public Map<ProcessInstanceUUID, Boolean> execute(Environment environment) throws Exception {
    final APIAccessor accessor = new StandardAPIAccessorImpl();
    final QueryRuntimeAPI journalQueryRuntimeAPI = accessor.getQueryRuntimeAPI(AccessorUtil.QUERYLIST_JOURNAL_KEY);

    final Map<ProcessInstanceUUID, Boolean> instances = new HashMap<ProcessInstanceUUID, Boolean>();
    Map<ProcessInstanceUUID, Set<String>> users;
    users = journalQueryRuntimeAPI.getActiveUsersOfProcessInstances(instanceUUIDs);
    for (ProcessInstanceUUID uuid : instanceUUIDs) {
      instances.put(uuid, (users != null && users.get(uuid)!=null && users.get(uuid).contains(username)));
    }
    return instances;
  }

}
