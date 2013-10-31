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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ow2.bonita.env.Environment;
import org.ow2.bonita.facade.APIAccessor;
import org.ow2.bonita.facade.QueryRuntimeAPI;
import org.ow2.bonita.facade.impl.StandardAPIAccessorImpl;
import org.ow2.bonita.facade.runtime.ActivityState;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.light.LightActivityInstance;
import org.ow2.bonita.util.AccessorUtil;
import org.ow2.bonita.util.Command;

public class GetProcessInstancesActivitiesCommand implements Command<Map<ProcessInstanceUUID, List<LightActivityInstance>>> {

  private static final long serialVersionUID = 1L;
  private final Set<ProcessInstanceUUID> instanceUUIDs;
  private final boolean considerSystemTaks;
    
  public GetProcessInstancesActivitiesCommand(Set<ProcessInstanceUUID> instanceUUIDs, boolean considerSystemTaks) {
    this.instanceUUIDs = instanceUUIDs;
    this.considerSystemTaks = considerSystemTaks;
  }
  
  public Map<ProcessInstanceUUID, List<LightActivityInstance>> execute(Environment environment) throws Exception {
    final APIAccessor accessor = new StandardAPIAccessorImpl();
    final QueryRuntimeAPI journalQueryRuntimeAPI = accessor.getQueryRuntimeAPI(AccessorUtil.QUERYLIST_JOURNAL_KEY);
    final QueryRuntimeAPI allQueryRuntimeAPI = accessor.getQueryRuntimeAPI();
    
    Map<ProcessInstanceUUID, List<LightActivityInstance>> activities;
    
    if(considerSystemTaks){
        final Map<ProcessInstanceUUID, LightActivityInstance> lastUpdatedActivities = allQueryRuntimeAPI.getLightLastUpdatedActivityInstanceFromRoot(instanceUUIDs, considerSystemTaks);
        activities = new HashMap<ProcessInstanceUUID, List<LightActivityInstance>>();
        for (Map.Entry<ProcessInstanceUUID, LightActivityInstance> entry : lastUpdatedActivities.entrySet()) {
            activities.put(entry.getKey(), Arrays.asList(entry.getValue()));
          }
    } else {
        activities = journalQueryRuntimeAPI.getLightActivityInstancesFromRoot(instanceUUIDs, ActivityState.READY);
        final Set<ProcessInstanceUUID> instancesWithoutReadyTask = new HashSet<ProcessInstanceUUID>();
        
        for (ProcessInstanceUUID instanceUUID : instanceUUIDs) {
            if (activities.get(instanceUUID) == null || activities.get(instanceUUID).isEmpty()) {
              instancesWithoutReadyTask.add(instanceUUID);
            }
          }
          
          if (!instancesWithoutReadyTask.isEmpty()) {
            
            final Map<ProcessInstanceUUID, LightActivityInstance> lastUpdatedActivities = allQueryRuntimeAPI.getLightLastUpdatedActivityInstanceFromRoot(instancesWithoutReadyTask, considerSystemTaks);
            
            for (Map.Entry<ProcessInstanceUUID, LightActivityInstance> entry : lastUpdatedActivities.entrySet()) {
              final List<LightActivityInstance> list = new ArrayList<LightActivityInstance>();
              list.add(entry.getValue());
              activities.put(entry.getKey(), list);
            }
          }
    }
    return activities;
  }
  
}
