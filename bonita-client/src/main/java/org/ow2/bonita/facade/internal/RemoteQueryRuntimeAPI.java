/**
 * Copyright (C) 2007  Bull S. A. S.
 * Bull, Rue Jean Jaures, B.P.68, 78340, Les Clayes-sous-Bois
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA  02110-1301, USA.
 * 
 * Modified by Charles Souillard - BonitaSoft S.A.
 * Modified by Matthieu Chaffotte - BonitaSoft S.A.
 * Modified by Nicolas Chabanoles - BonitaSoft S.A.
 * Modified by Elias Ricken de Medeiros - BonitaSoft S.A.
 **/
package org.ow2.bonita.facade.internal;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ow2.bonita.facade.exception.InstanceNotFoundException;
import org.ow2.bonita.facade.runtime.ActivityState;
import org.ow2.bonita.facade.runtime.InstanceState;
import org.ow2.bonita.facade.runtime.ProcessInstance;
import org.ow2.bonita.facade.runtime.TaskInstance;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.light.LightProcessInstance;
import org.ow2.bonita.light.LightTaskInstance;

/**
 * For internal use only.
 *
 * */
public interface RemoteQueryRuntimeAPI extends AbstractRemoteQueryRuntimeAPI {

	Set<ProcessInstance> getProcessInstances(
	    Collection<ProcessInstanceUUID> instanceUUIDs, 
			final Map<String, String> options)
	throws RemoteException;

	Set<LightProcessInstance> getLightProcessInstances(
	    Collection<ProcessInstanceUUID> instanceUUIDs, 
			final Map<String, String> options)
	throws RemoteException;

	List<LightProcessInstance> getLightProcessInstances(
	    Collection<ProcessInstanceUUID> instanceUUIDs, 
			int fromIndex,
			int pageSize,
			final Map<String, String> options)
	throws RemoteException;

	Set<ProcessInstance> getProcessInstancesWithInstanceStates(
	    Collection<InstanceState> instanceStates, 
			final Map<String, String> options)
	throws RemoteException;

	Set<ProcessInstance> getProcessInstancesWithTaskState(
	    Collection<ActivityState> activityStates, 
			final Map<String, String> options)
	throws RemoteException;

	Collection<TaskInstance> getTaskList(
	    ProcessInstanceUUID instanceUUID, 
			Collection<ActivityState> taskStates,
			final Map<String, String> options)
	throws InstanceNotFoundException, RemoteException;

	Collection<LightTaskInstance> getLightTaskList(
	    ProcessInstanceUUID instanceUUID, 
			Collection<ActivityState> taskStates,
			final Map<String, String> options)
	throws InstanceNotFoundException, RemoteException;

}
