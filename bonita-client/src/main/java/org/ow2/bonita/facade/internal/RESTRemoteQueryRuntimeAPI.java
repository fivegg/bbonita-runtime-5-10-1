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
package org.ow2.bonita.facade.internal;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.ow2.bonita.facade.exception.InstanceNotFoundException;
import org.ow2.bonita.facade.runtime.ActivityState;
import org.ow2.bonita.facade.runtime.InstanceState;
import org.ow2.bonita.facade.runtime.ProcessInstance;
import org.ow2.bonita.facade.runtime.TaskInstance;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.light.LightProcessInstance;
import org.ow2.bonita.light.LightTaskInstance;

/**
 * 
 * @author Elias Ricken de Medeiros
 *
 */
@Path("/API/queryRuntimeAPI/")
@Produces({"text/*","application/xml"})
@Consumes({"application/x-www-form-urlencoded","text/*", "application/xml" })
public interface RESTRemoteQueryRuntimeAPI extends AbstractRemoteQueryRuntimeAPI {

	/**
	 * Returns all records of instances matching with the given ProcessInstanceUUID.<br>
   * If one of the ProcessInstanceUUID is not found, nothing is added to the result. <br>
   * An empty set is returned if no instance is found.
   * @param instanceUUIDs the instance UUIDs.
	 * @param options the options map (domain, queryList, user)
	 * @return all records of instances matching with the given ProcessInstanceUUID.
	 * @throws RemoteException
	 */
	@POST @Path("getProcessInstancesByProcessInstanceUUIDs")
	Set<ProcessInstance> getProcessInstances(
	    @FormParam("instanceUUIDs") List<ProcessInstanceUUID> instanceUUIDs,
			@FormParam("options") final Map<String, String> options)
	throws RemoteException;

	/**
	 * Gets a set of light process instances form their UUIDs.
   * @param instanceUUIDs the collection of instance UUIDs
	 * @param options the options map (domain, queryList, user)
	 * @return a set of light process instances form their UUIDs.
	 * @throws RemoteException
	 */
	@POST @Path("getLightProcessInstancesByProcessInstanceUUIDs")
	Set<LightProcessInstance> getLightProcessInstances(
	    @FormParam("instanceUUIDs") List<ProcessInstanceUUID> instanceUUIDs,
			@FormParam("options") final Map<String, String> options)
	throws RemoteException;

	/**
	 * Gets a set of light process instances from an index to the page size according to the collection
   * of process instance UUIDs. This set is a sub-set of getLightProcessInstances.
   * @param instanceUUIDs the instances UUIDs
   * @param fromIndex the index
   * @param pageSize the page size
	 * @param options the options map (domain, queryList, user)
	 * @return a set of light process instances
	 * @throws RemoteException
	 */
	@POST	@Path("getLightProcessInstancesByProcessInstanceUUIDsIndexAndPageSize")
	List<LightProcessInstance> getLightProcessInstances(
	    @FormParam("instanceUUIDs") List<ProcessInstanceUUID> instanceUUIDs,
			@QueryParam("fromIndex") int fromIndex,
			@QueryParam("pageSize") int pageSize,
			@FormParam("options") final Map<String, String> options)
	throws RemoteException;

	/**
	 * Returns all records of instances in one of the given states.
   * @param instanceStates a Collection of the required instance states
	 * @param options the options map (domain, queryList, user)
	 * @return all records of instances in one task in one of the given states.
	 * @throws RemoteException
	 */
	@POST @Path("getProcessInstancesWithInstanceStates")
	Set<ProcessInstance> getProcessInstancesWithInstanceStates(
	    @FormParam("instanceStates") List<InstanceState> instanceStates, 
			@FormParam("options") final Map<String, String> options)
	throws RemoteException;

	/**
	 * Returns all records of instances having one task in one of the given states.<br>
   * @param activityStates states
	 * @param options the options map (domain, queryList, user)
	 * @return all records of instances having one task in one of the given states.
	 * @throws RemoteException
	 */
	@POST	@Path("getProcessInstancesWithTaskState")
	Set<ProcessInstance> getProcessInstancesWithTaskState(
	    @FormParam("activityStates") List<ActivityState> activityStates, 
			@FormParam("options") final Map<String, String> options)
	throws RemoteException;

	/**
	 * Obtains the user tasks depending on the given activity states for the given instance
   * and the authenticated user.<br>
   * @param instanceUUID the instance UUID.
   * @param taskStates the {@link org.ow2.bonita.facade.runtime.ActivityState states} of the task.
	 * @param options the options map (domain, queryList, user)
	 * @return a collection of task records. If no tasks are found, an empty collection is returned.
	 * @throws InstanceNotFoundException
	 * @throws RemoteException
	 */
	@POST @Path("getTaskList/{instanceUUID}")
	Collection<TaskInstance> getTaskList(
	    @PathParam("instanceUUID") ProcessInstanceUUID instanceUUID,
			@FormParam("taskStates") List<ActivityState> taskStates,
			@FormParam("options") final Map<String, String> options)
	throws InstanceNotFoundException, RemoteException;
  
	/**
	 * See getTaskList(ProcessInstanceUUID instanceUUID, Collection taskStates)
	 * @param instanceUUID
	 * @param taskStates
	 * @param options the options map (domain, queryList, user)
	 * @return
	 * @throws InstanceNotFoundException
	 * @throws RemoteException
	 */
	@POST	@Path("getLightTaskListByProcessInstanceUUID/{instanceUUID}")
	Collection<LightTaskInstance> getLightTaskList(
	    @PathParam("instanceUUID") ProcessInstanceUUID instanceUUID,
			@FormParam("taskStates") List<ActivityState> taskStates,
			@FormParam("options") final Map<String, String> options)
	throws InstanceNotFoundException, RemoteException;

}
