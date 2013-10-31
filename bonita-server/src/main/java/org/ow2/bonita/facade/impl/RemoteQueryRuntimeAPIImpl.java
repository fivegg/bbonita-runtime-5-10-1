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
package org.ow2.bonita.facade.impl;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ow2.bonita.facade.exception.InstanceNotFoundException;
import org.ow2.bonita.facade.internal.RemoteQueryRuntimeAPI;
import org.ow2.bonita.facade.runtime.ActivityState;
import org.ow2.bonita.facade.runtime.InstanceState;
import org.ow2.bonita.facade.runtime.ProcessInstance;
import org.ow2.bonita.facade.runtime.TaskInstance;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.light.LightProcessInstance;

/**
 * 
 * @author Elias Ricken de Medeiros, Matthieu Chaffotte
 *
 */
public class RemoteQueryRuntimeAPIImpl extends AbstractRemoteQueryRuntimeAPIImpl implements RemoteQueryRuntimeAPI {

  public Set<ProcessInstance> getProcessInstances(Collection<ProcessInstanceUUID> instanceUUIDs, Map<String, String> options)
  throws RemoteException {
    return getAPI(options).getProcessInstances(instanceUUIDs);
  }

  public Set<ProcessInstance> getProcessInstancesWithTaskState(Collection<ActivityState> activityStates, Map<String, String> options)
  throws RemoteException {
    return getAPI(options).getProcessInstancesWithTaskState(activityStates);
  }

  public Collection<TaskInstance> getTaskList(ProcessInstanceUUID instanceUUID, Collection<ActivityState> taskStates, Map<String, String> options)
  throws InstanceNotFoundException, RemoteException {
    return getAPI(options).getTaskList(instanceUUID, taskStates);
  }

  public Set<LightProcessInstance> getLightProcessInstances(Collection<ProcessInstanceUUID> instanceUUIDs, Map<String, String> options)
  throws RemoteException {
    return getAPI(options).getLightProcessInstances(instanceUUIDs);
  }

  public List<LightProcessInstance> getLightProcessInstances(Collection<ProcessInstanceUUID> instanceUUIDs, int fromIndex, int pageSize, Map<String, String> options)
  throws RemoteException {
    return getAPI(options).getLightProcessInstances(instanceUUIDs, fromIndex, pageSize);
  }

  public Set<ProcessInstance> getProcessInstancesWithInstanceStates(Collection<InstanceState> instanceStates, Map<String, String> options)
  throws RemoteException {
    return getAPI(options).getProcessInstancesWithInstanceStates(instanceStates);
  }

}
