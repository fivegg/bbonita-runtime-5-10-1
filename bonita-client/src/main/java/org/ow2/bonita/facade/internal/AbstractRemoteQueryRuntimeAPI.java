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

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Date;
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

import org.ow2.bonita.facade.exception.ActivityNotFoundException;
import org.ow2.bonita.facade.exception.DocumentNotFoundException;
import org.ow2.bonita.facade.exception.EventNotFoundException;
import org.ow2.bonita.facade.exception.InstanceNotFoundException;
import org.ow2.bonita.facade.exception.ProcessNotFoundException;
import org.ow2.bonita.facade.exception.TaskNotFoundException;
import org.ow2.bonita.facade.exception.VariableNotFoundException;
import org.ow2.bonita.facade.paging.ActivityInstanceCriterion;
import org.ow2.bonita.facade.paging.ProcessInstanceCriterion;
import org.ow2.bonita.facade.runtime.ActivityInstance;
import org.ow2.bonita.facade.runtime.ActivityState;
import org.ow2.bonita.facade.runtime.AttachmentInstance;
import org.ow2.bonita.facade.runtime.CatchingEvent;
import org.ow2.bonita.facade.runtime.Comment;
import org.ow2.bonita.facade.runtime.Document;
import org.ow2.bonita.facade.runtime.ProcessInstance;
import org.ow2.bonita.facade.runtime.TaskInstance;
import org.ow2.bonita.facade.uuid.ActivityInstanceUUID;
import org.ow2.bonita.facade.uuid.CatchingEventUUID;
import org.ow2.bonita.facade.uuid.DocumentUUID;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.light.LightActivityInstance;
import org.ow2.bonita.light.LightProcessInstance;
import org.ow2.bonita.light.LightTaskInstance;
import org.ow2.bonita.search.DocumentResult;
import org.ow2.bonita.search.DocumentSearchBuilder;
import org.ow2.bonita.search.SearchQueryBuilder;
import org.ow2.bonita.util.BonitaConstants;
import org.w3c.dom.Node;

/**
 * Getters on workflow recorded data.<br>
 * Operations in this API applies to main entities managed by Bonita such as:<br>
 * processes, process instances, activities, tasks.<br>
 * Returned records are issued from recorded runtime informations (both runtime and archived workflow instances).
 * @author Elias Ricken de Medeiros, Matthieu Chaffotte
 *
 */
@Path("/API/queryRuntimeAPI/")
@Produces({"text/*","application/xml"})
@Consumes({"application/x-www-form-urlencoded","text/*", "application/xml" })
public interface AbstractRemoteQueryRuntimeAPI extends Remote {

	/**
	 * Returns the record of the instance with the given UUID.
   * @param instanceUUID the instance UUID.
	 * @param options the options map (domain, queryList, user)
	 * @return the record of the task with the given activity instance UUID.
	 * @throws InstanceNotFoundException
	 * @throws RemoteException
	 */
	@POST	@Path("getProcessInstance/{instanceUUID}")	
  ProcessInstance getProcessInstance(
      @PathParam("instanceUUID") ProcessInstanceUUID instanceUUID,
      @FormParam("options") final Map<String, String> options) 
	throws InstanceNotFoundException, RemoteException;
  
	/**
	 * Returns the light process instance with the given process instance UUID.
   * @param instanceUUID the instance UUID.
	 * @param options the options map (domain, queryList, user)
	 * @return the record of the instance with the given UUID.
	 * @throws InstanceNotFoundException
	 * @throws RemoteException
	 */
	@POST	@Path("getLightProcessInstance/{instanceUUID}")
	LightProcessInstance getLightProcessInstance(
	    @PathParam("instanceUUID") ProcessInstanceUUID instanceUUID, 
			@FormParam("options") final Map<String, String> options)
	throws InstanceNotFoundException, RemoteException;

	/**
	 * Gets all process instances. An empty set is returned if no instance is found. 
	 * @param options the options map (domain, queryList, user)
	 * @return a set containing all process instances.
	 * @throws RemoteException
	 */
	@POST @Path("getProcessInstances")
	Set<ProcessInstance> getProcessInstances(
	    @FormParam("options") final Map<String, String> options)
	throws RemoteException;

	/**
	 * Gets all light process instances. An empty set is returned if no instance is found. 
	 * @param options the options map (domain, queryList, user)
	 * @return a set containing all light process instances.
	 * @throws RemoteException
	 */
	@POST @Path("getLightProcessInstances")
	Set<LightProcessInstance> getLightProcessInstances(
	    @FormParam("options") final Map<String, String> options)
	throws RemoteException;

	/**
	 * Gets a set of light process instances from an index to the page size. This set is a sub-set
   * of getLightProcessInstances.
   * @param fromIndex the index
   * @param pageSize the page size
	 * @param options the options map (domain, queryList, user)
	 * @return
	 * @throws RemoteException
	 */
	@POST @Path("getLightProcessInstancesByIndexAndPageSize")
	List<LightProcessInstance> getLightProcessInstances(
	    @QueryParam("fromIndex") int fromIndex,
	    @QueryParam("pageSize") int pageSize, 
			@FormParam("options") final Map<String, String> options)
	throws RemoteException;
	
	
	/**
   * Gets a set of light process instances from an index to the page size ordered by the paging criterion. 
   * This list is a sub-set of getLightProcessInstances.
   * @param fromIndex the index
   * @param pageSize the page size
   * @param pagingCriterion the attribute used to oder the list
   * @return a list of light process instances ordered by pagingCriterion
   */
	@POST @Path("getLightProcessInstancesByIndexPageSizeAndPagingCriterion")
  List<LightProcessInstance> getLightProcessInstances(
  		@QueryParam("fromIndex") int fromIndex, 
  		@QueryParam("pageSize") int pageSize, 
  		@QueryParam("pagingCriterion") ProcessInstanceCriterion pagingCriterion, 
			@FormParam("options") final Map<String, String> options)
  throws RemoteException;
  
	/**
	 * Returns all records of instance for the given process processDefinitionUUID.<br>
   * An empty set is returned if no instance is found.
   * @param processUUID the process definition UUID.
	 * @param options the options map (domain, queryList, user)
	 * @return a set containing all instance records.
	 * @throws RemoteException
	 */
	@POST	@Path("getLightProcessInstances/{processUUID}")
	Set<LightProcessInstance> getLightProcessInstances(
	    @PathParam("processUUID") ProcessDefinitionUUID processUUID, 
			@FormParam("options") final Map<String, String> options)
	throws RemoteException;

	/**
   * Gets a list of light process instances from an index to the page size according to the set
   * of process instance UUIDs. This list is a sub-set of getLightProcessInstances.
   * @param instanceUUIDs the instances UUIDs
   * @param fromIndex the index
   * @param pageSize the page size
   * @param pagingCriterion the criterion used do sort the returned list
   * @param options the options map (domain, queryList, user)
   * @return a list of light process instances sorted by pagingCriterion
   */
  @POST @Path("getLightProcessInstancesByPageSizeAndPagingCriterion")
  List<LightProcessInstance> getLightProcessInstances(
  		@FormParam("instanceUUIDs") Set<ProcessInstanceUUID> instanceUUIDs, 
  		@QueryParam("fromIndex")int fromIndex, 
  		@QueryParam("pageSize")int pageSize, 
  		@QueryParam("pagingCriterion")ProcessInstanceCriterion pagingCriterion,
	    @FormParam("options") final Map<String, String> options) throws RemoteException;
	
	/**
	 * Returns all records of instance for the given process processDefinitionUUIDs.<br>
   * An empty set is returned if no instance is found.
   * @param processUUIDs the process definition UUIDs.
	 * @param options the options map (domain, queryList, user)
	 * @return a set containing all instance records.
	 * @throws RemoteException
	 */
	@POST @Path("getLightWeightProcessInstances")
	Set<LightProcessInstance> getLightWeightProcessInstances(
	    @FormParam("processUUIDs") Set<ProcessDefinitionUUID> processUUIDs, 
			@FormParam("options") final Map<String, String> options)
	throws RemoteException;

	/**
	 * Gets a set of light process instances from an index to the page size. This set is a sub-set
   * of getLightProcessInstances and takes only process instances which contain sub-process(es).
   * @param fromIndex the index
   * @param pageSize the page size
	 * @param options the options map (domain, queryList, user)
	 * @return
	 * @throws RemoteException
	 */
	@POST	@Path("getLightParentProcessInstances")
	List<LightProcessInstance> getLightParentProcessInstances(
	    @QueryParam("fromIndex") int fromIndex,
	    @QueryParam("pageSize") int pageSize, 
			@FormParam("options") final Map<String, String> options)
	throws RemoteException;
	
  /**
   * Gets a list of light process instances from an index to the page size. This list is a sub-set
   * of getLightProcessInstances and takes only process instances which contain sub-process(es).
   * @param fromIndex the index
   * @param pageSize the page size
   * @param pagingCriterion the attribute to be used to sort the result
   * @param options the options map (domain, queryList, user)
   * @return a list of light process instances sorted by pagingCriterion
   */
	@POST	@Path("getLightParentProcessInstancesByPagingCriterion")
  List<LightProcessInstance> getLightParentProcessInstances(
  		@QueryParam("fromIndex") int fromIndex, 
  		@QueryParam("pageSize") int pageSize, 
  		@QueryParam("pagingCriterion") ProcessInstanceCriterion pagingCriterion, 
			@FormParam("options") final Map<String, String> options)
	throws RemoteException;
	
	/**
   * Gets the LightProcessInstances that are not sub-process instances and having the ProcessDefinitionUUID in the given ProcessDefinitionUUIDs sub-set.
   * @param fromIndex
   * @param pageSize
   * @param processUUIDs ProcessDefinitionUUIDs to be taken into consideration
   * @param pagingCriterion the criterion used to sort the result list
   * @param options the options map (domain, queryList, user)
   * @return the LightProcessInstances that are not sub-process instances and having the ProcessDefinitionUUID in the given ProcessDefinitionUUIDs sub-set.
   */  
	@POST  @Path("getLightParentProcessInstancesFromProcessUUIDs")
  List<LightProcessInstance> getLightParentProcessInstances(
      @FormParam("processUUIDs") Set<ProcessDefinitionUUID> processUUIDs, 
      @QueryParam("fromIndex") int fromIndex, 
      @QueryParam("pageSize") int pageSize, 
      @QueryParam("pagingCriterion") ProcessInstanceCriterion pagingCriterion, 
      @FormParam("options") final Map<String, String> options)
  throws RemoteException;
  
  /**
   * Gets the LightProcessInstances that are not sub-process instances and not having the ProcessDefinitionUUID in the given ProcessDefinitionUUIDs sub-set.
   * @param fromIndex
   * @param pageSize
   * @param exceptions ProcessDefinitionUUIDs to be ignored
   * @param pagingCriterion the criterion used to sort the result list
   * @param options the options map (domain, queryList, user)
   * @return the LightProcessInstances that are not sub-process instances and not having the ProcessDefinitionUUID in the given ProcessDefinitionUUIDs sub-set.
   */  
	@POST  @Path("getLightParentProcessInstancesExcept")
  List<LightProcessInstance> getLightParentProcessInstancesExcept(
      @FormParam("exceptions") Set<ProcessDefinitionUUID> exceptions, 
      @QueryParam("fromIndex") int fromIndex, 
      @QueryParam("pageSize") int pageSize, 
      @QueryParam("pagingCriterion") ProcessInstanceCriterion pagingCriterion, 
      @FormParam("options") final Map<String, String> options)
  throws RemoteException;


	/**
	 * * Gets LightProcessInstances that are not sub-process instances and having the given userId member of the active user.
	 * @param userId
	 * @param fromIndex
	 * @param pageSize
	 * @param options the options map (domain, queryList, user)
	 * @return
	 * @throws RemoteException
	 */
	@POST @Path("getLightParentProcessInstancesWithActiveUser/{userId}")
	List<LightProcessInstance> getLightParentProcessInstancesWithActiveUser(
	    @PathParam("userId") String userId,
	    @QueryParam("fromIndex") int fromIndex, 
			@QueryParam("pageSize") int pageSize,
			@FormParam("options") final Map<String, String> options)
	throws RemoteException;
	
	/**
   * Gets LightProcessInstances that are not sub-process instances and having the given userId member of the active user.
   * @param userId
   * @param fromIndex
   * @param pageSize
   * @param pagingCriterion the criterion used to sort the result list
   * @param options the options map (domain, queryList, user)
   * @return
   */
	@POST @Path("getLightParentProcessInstancesWithActiveUserWithPagingCriterion/{userId}")
  List<LightProcessInstance> getLightParentProcessInstancesWithActiveUser(
  		@PathParam("userId") String userId, 
  		@QueryParam("fromIndex") int fromIndex, 
  		@QueryParam("pageSize") int pageSize, 
  		@QueryParam("pagingCriterion") ProcessInstanceCriterion pagingCriterion,
  		@FormParam("options") final Map<String, String> options)
  throws RemoteException;
	
	/**
   * * Gets LightProcessInstances that are not sub-process instances and having the given userId member of the active user.
   * @param userId
   * @param fromIndex
   * @param pageSize
   * @param options the options map (domain, queryList, user)
   * @return
   * @throws RemoteException
   */
  @POST @Path("getLightParentProcessInstancesWithActiveUserExcept/{userId}")
  List<LightProcessInstance> getLightParentProcessInstancesWithActiveUserExcept(
      @PathParam("userId") String userId,
      @QueryParam("fromIndex") int fromIndex, 
      @QueryParam("pageSize") int pageSize,
      @FormParam("processes") Set<ProcessDefinitionUUID> processes,
      @FormParam("options") final Map<String, String> options)
  throws RemoteException;
  
  /**
   * Gets LightProcessInstances that are not sub-process instances and having the given userId member of the active user
   * order by the pagingCriterion.<br>
   * Instances of processes given in parameter are ignored.
   * @param userId
   * @param fromIndex
   * @param pageSize
   * @param processUUIDs
   * @param pagingCriterion the criterion used to sort the result list
   * @return
   */
  @POST @Path("getLightParentProcessInstancesWithActiveUserExceptWithPaginCriterion/{userId}")
  List<LightProcessInstance> getLightParentProcessInstancesWithActiveUserExcept(
  		@PathParam("userId") String userId, 
  		@QueryParam("fromIndex") int fromIndex, 
  		@QueryParam("pageSize") int pageSize, 
  		@FormParam("processUUIDs") Set<ProcessDefinitionUUID> processUUIDs, 
  		@QueryParam("pagingCriterion") ProcessInstanceCriterion pagingCriterion,
  		@FormParam("options") final Map<String, String> options)
  	throws RemoteException;
  
  /**
   * * Gets LightProcessInstances that are not sub-process instances and having the given userId member of the active user.
   * @param userId
   * @param fromIndex
   * @param pageSize
   * @param options the options map (domain, queryList, user)
   * @return
   * @throws RemoteException
   */
  @POST @Path("getLightParentProcessInstancesWithActiveUserAndProcessUUIDs/{userId}")
  List<LightProcessInstance> getLightParentProcessInstancesWithActiveUser(
      @PathParam("userId") String userId,
      @QueryParam("fromIndex") int fromIndex, 
      @QueryParam("pageSize") int pageSize,
      @FormParam("processes") Set<ProcessDefinitionUUID> processes,
      @FormParam("options") final Map<String, String> options)
  throws RemoteException;
  
  /**
   * Gets LightProcessInstances that are not sub-process instances and having the given userId member of the active user
   * order by pagingCriterion.<br>
   * Only instances of processes given in parameter are considered.
   * @param username
   * @param fromIndex
   * @param pageSize
   * @param processUUIDs
   * @param pagingCriterion the criterion used to sort the result list
   * @return
   */
  @POST @Path("getLightParentProcessInstancesWithActiveUserAndPagingCriterionFromProcessUUIDs/{username}")
  List<LightProcessInstance> getLightParentProcessInstancesWithActiveUser(
  		@PathParam("username")String username, 
  		@QueryParam("fromIndex") int fromIndex, 
  		@QueryParam("pageSize") int pageSize, 
  		@FormParam("processUUIDs") Set<ProcessDefinitionUUID> processUUIDs, 
  		@QueryParam("pagingCriterion") ProcessInstanceCriterion pagingCriterion,
      @FormParam("options") final Map<String, String> options)
  throws RemoteException;

	/**
	 * * Gets the ProcessInstances that are not sub-process instances and having the given userId member of the active users and having at least one task with the expected end date in the next {@code remainingDays}.
	 * @param userId
	 * @param remainingDays
	 * @param fromIndex
	 * @param pageSize
	 * @param options the options map (domain, queryList, user)
	 * @return
	 * @throws RemoteException
	 */
	@POST	@Path("getLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate/{userId}")
	List<LightProcessInstance> getLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate(
	    @PathParam("userId") String userId, 
			@QueryParam("remainingDays") int remainingDays,
			@QueryParam("fromIndex") int fromIndex,
			@QueryParam("pageSize") int pageSize, 
			@FormParam("options") final Map<String, String> options)
	throws RemoteException;
	
	/**
   * Gets the ProcessInstances that are not sub-process instances and having the given userId member of the 
   * active users and having at least one task with the expected end date in the next {@code remainingDays} order by the
   * given pagingCriterion.
   * @param userId
   * @param remainingDays
   * @param fromIndex
   * @param pageSize
   * @param pagingCriterion the criterion used to sort the resulting list
   * @param options the options map (domain, queryList, user)
   * @return
   */
	@POST	@Path("getLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDateWithPagingCriterion/{userId}")
  List<LightProcessInstance> getLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate(
  		@PathParam("userId") String userId, 
  		@QueryParam("remainingDays") int remainingDays, 
  		@QueryParam("fromIndex") int fromIndex, 
  		@QueryParam("pageSize") int pageSize, 
  		@QueryParam("pagingCriterion") ProcessInstanceCriterion pagingCriterion, 
			@FormParam("options") final Map<String, String> options)
	throws RemoteException;
	
	/**
   * * Gets the ProcessInstances that are not sub-process instances and having the given userId member of the active users and having at least one task with the expected end date in the next {@code remainingDays}.
   * @param userId
   * @param remainingDays
   * @param fromIndex
   * @param pageSize
   * @param options the options map (domain, queryList, user)
   * @return
   * @throws RemoteException
   */
  @POST @Path("getLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDateExcept/{userId}")
  List<LightProcessInstance> getLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDateExcept(
      @PathParam("userId") String userId, 
      @QueryParam("remainingDays") int remainingDays,
      @QueryParam("fromIndex") int fromIndex,
      @QueryParam("pageSize") int pageSize, 
      @FormParam("processes") Set<ProcessDefinitionUUID> processes,
      @FormParam("options") final Map<String, String> options)
  throws RemoteException;
  
  /**
   * Gets the ProcessInstances that are not sub-process instances and having the given userId member of the active users and having at least 
   * one task with the expected end date in the next {@code remainingDays} order by the given pagingCriterion.<br/>
   * Instance of given processes are ignored.
   * @param username
   * @param fromIndex
   * @param pageSize
   * @param remainingDays 
   * @param processUUIDs
   * @param pagingCriterion the criterion used to sort the process instances
   * @return
   */
  @POST @Path("getLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDateExceptWithPagingCriterion/{username}")
  List<LightProcessInstance> getLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDateExcept(
  		@PathParam("username") String username, 
  		@QueryParam("remainingDays") int remainingDays, 
  		@QueryParam("fromIndex") int fromIndex, 
  		@QueryParam("pageSize") int pageSize, 
  		@FormParam("processUUIDs") Set<ProcessDefinitionUUID> processUUIDs, 
  		@QueryParam("pagingCriterion") ProcessInstanceCriterion pagingCriterion,
      @FormParam("options") final Map<String, String> options)
  throws RemoteException;
  
  /**
   * * Gets the ProcessInstances that are not sub-process instances and having the given userId member of the active users and having at least one task with the expected end date in the next {@code remainingDays}.
   * @param userId
   * @param remainingDays
   * @param fromIndex
   * @param pageSize
   * @param options the options map (domain, queryList, user)
   * @return
   * @throws RemoteException
   */
  @POST @Path("getLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDateAndProcessUUIDs/{userId}")
  List<LightProcessInstance> getLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate(
      @PathParam("userId") String userId, 
      @QueryParam("remainingDays") int remainingDays,
      @QueryParam("fromIndex") int fromIndex,
      @QueryParam("pageSize") int pageSize, 
      @FormParam("processes") Set<ProcessDefinitionUUID> processes,
      @FormParam("options") final Map<String, String> options)
  throws RemoteException;
  
  /**
   * Gets the ProcessInstances that are not sub-process instances and having the given userId member of 
   * the active users and having at least one task with the expected end date in the next {@code remainingDays}
   * order by the giver pagingCriterion.<br/>
   * Only instances of given processes are considered.
   * @param username
   * @param fromIndex
   * @param pageSize
   * @param processUUIDs
   * @return
   */
  @POST @Path("getLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDateAndProcessUUIDsWithPagingCriterion/{username}")
  List<LightProcessInstance> getLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate(
  		@PathParam("username") String username, 
  		@QueryParam("remainingDays") int remainingDays, 
  		@QueryParam("fromIndex") int fromIndex, 
  		@QueryParam("pageSize") int pageSize, 
  		@FormParam("processUUIDs") Set<ProcessDefinitionUUID> processUUIDs, 
  		@QueryParam("pagingCriterion") ProcessInstanceCriterion pagingCriterion,
      @FormParam("options") final Map<String, String> options)
  throws RemoteException;
  
	/**
	 * Gets the ProcessInstances that are not sub-process instances and having the given userId member of the active users and having at least one task with the expected end date in the past.
	 * @param userId
	 * @param fromIndex
	 * @param pageSize
	 * @param options the options map (domain, queryList, user)
	 * @return
	 * @throws RemoteException
	 */
	@POST @Path("getLightParentProcessInstancesWithOverdueTasks/{userId}")
	List<LightProcessInstance> getLightParentProcessInstancesWithOverdueTasks(
	    @PathParam("userId") String userId,
	    @QueryParam("fromIndex") int fromIndex, 
			@QueryParam("pageSize") int pageSize,
			@FormParam("options") final Map<String, String> options)
	throws RemoteException;
	
	/**
   * Gets the ProcessInstances that are not sub-process instances and having the given userId member of the active users and 
   * having at least one task with the expected end date in the past order by the given pagingCriterion.
   * @param userId
   * @param fromIndex
   * @param pageSize
   * @param pagingCriterion the criterion used to sort the ProcessInstances
   * @param options the options map (domain, queryList, user)
   * @return
   */
	@POST @Path("getLightParentProcessInstancesWithOverdueTasksWithPagingCriterion/{userId}")
  List<LightProcessInstance> getLightParentProcessInstancesWithOverdueTasks(
  		@PathParam("userId") String userId, 
  		@QueryParam("fromIndex") int fromIndex, 
  		@QueryParam("pageSize") int pageSize, 
  		@QueryParam("pagingCriterion") ProcessInstanceCriterion pagingCriterion,
			@FormParam("options") final Map<String, String> options)
  throws RemoteException;
	
	/**
   * Gets the ProcessInstances that are not sub-process instances and having the given userId member of the active users and having at least one task with the expected end date in the past.
   * @param userId
   * @param fromIndex
   * @param pageSize
   * @param options the options map (domain, queryList, user)
   * @return
   * @throws RemoteException
   */
  @POST @Path("getLightParentProcessInstancesWithOverdueTasksExcept/{userId}")
  List<LightProcessInstance> getLightParentProcessInstancesWithOverdueTasksExcept(
      @PathParam("userId") String userId,
      @QueryParam("fromIndex") int fromIndex, 
      @QueryParam("pageSize") int pageSize,
      @FormParam("processes") Set<ProcessDefinitionUUID> processes,
      @FormParam("options") final Map<String, String> options)
  throws RemoteException;
  
  /**
   * Gets the ProcessInstances that are not sub-process instances and having the given userId member of the active users 
   * and having at least one task with the expected end date in the past.<br>
   * The ProcessInstances are order by the given pagingCriterion.<br>
   * Instances of the given processes are ignored.
   * @param username
   * @param fromIndex
   * @param pageSize
   * @param processUUIDs
   * @param pagingCriterion the criterion used to sort the ProcessInstances
   * @return
   */
  @POST @Path("getLightParentProcessInstancesWithOverdueTasksExceptWithPagingCriterion/{username}")
  List<LightProcessInstance> getLightParentProcessInstancesWithOverdueTasksExcept(
  		@PathParam("username") String username, 
  		@QueryParam("fromIndex") int fromIndex, 
  		@QueryParam("pageSize") int pageSize, 
  		@FormParam("processUUIDs") Set<ProcessDefinitionUUID> processUUIDs, 
  		@QueryParam("pagingCriterion") ProcessInstanceCriterion pagingCriterion,
      @FormParam("options") final Map<String, String> options)
  throws RemoteException;
  
  /**
   * Gets the ProcessInstances that are not sub-process instances and having the given userId member of the active users and having at least one task with the expected end date in the past.
   * @param userId
   * @param fromIndex
   * @param pageSize
   * @param options the options map (domain, queryList, user)
   * @return
   * @throws RemoteException
   */
  @POST @Path("getLightParentProcessInstancesWithOverdueTasksAndProcessUUID/{userId}")
  List<LightProcessInstance> getLightParentProcessInstancesWithOverdueTasks(
      @PathParam("userId") String userId,
      @QueryParam("fromIndex") int fromIndex, 
      @QueryParam("pageSize") int pageSize,
      @FormParam("processes") Set<ProcessDefinitionUUID> processes,
      @FormParam("options") final Map<String, String> options)
  throws RemoteException;
  
  /**
   * Gets the ProcessInstances that are not sub-process instances and having the given userId member of the active users and
   *  having at least one task with the expected end date in the past order by the given pagingCriterion.<br>
   * Only instances of the given processes are considered.
   * @param username
   * @param fromIndex
   * @param pageSize
   * @param processUUIDs
   * @param pagingCriterion the criterion used to sort the ProcessInstances
   * @return
   */
  @POST @Path("getLightParentProcessInstancesWithOverdueTasksAndProcessUUIDWithPagingCriterion/{username}")
  List<LightProcessInstance> getLightParentProcessInstancesWithOverdueTasks(
  		@PathParam("username") String username, 
  		@QueryParam("fromIndex") int fromIndex, 
  		@QueryParam("pageSize") int pageSize, 
  		@FormParam("processUUIDs") Set<ProcessDefinitionUUID> processUUIDs, 
  		@QueryParam("pagingCriterion") ProcessInstanceCriterion pagingCriterion,
  		@FormParam("options") final Map<String, String> options)
  throws RemoteException;
  
	/**
	 * Gets LightProcessInstances that are not sub-process instances and having the given userId member of the active user.
	 * @param userId
	 * @param fromIndex
	 * @param pageSize
	 * @param options the options map (domain, queryList, user)
	 * @return
	 * @throws RemoteException
	 */
	@POST @Path("getLightParentProcessInstancesWithInvolvedUser/{userId}")
	List<LightProcessInstance> getLightParentProcessInstancesWithInvolvedUser(
	    @PathParam("userId") String userId,
	    @QueryParam("fromIndex") int fromIndex, 
			@QueryParam("pageSize") int pageSize,
			@FormParam("options") final Map<String, String> options)
	throws RemoteException;
	
	/**
   * Gets LightProcessInstances that are not sub-process instances and having the given userId member of the involved user order by pagingCriterion.
   * @param userId
   * @param fromIndex
   * @param pageSize
   * @param pagingCriterion the criterion used to sort the result list
   * @param options the options map (domain, queryList, user)
   * @return
   */
	@POST @Path("getLightParentProcessInstancesWithInvolvedUserWithPagingCriterion/{userId}")
  List<LightProcessInstance> getLightParentProcessInstancesWithInvolvedUser(
  		@PathParam("userId") String userId, 
  		@QueryParam("fromIndex") int fromIndex, 
  		@QueryParam("pageSize") int pageSize, 
  		@QueryParam("pagingCriterion") ProcessInstanceCriterion pagingCriterion,
			@FormParam("options") final Map<String, String> options) 
	throws RemoteException;
	
	/**
   * Gets LightProcessInstances that are not sub-process instances and having the given userId member of the active user.
   * @param userId
   * @param fromIndex
   * @param pageSize
   * @param processes
   * @param options the options map (domain, queryList, user)
   * @return
   * @throws RemoteException
   */
  @POST @Path("getLightParentProcessInstancesWithInvolvedUserExcept/{userId}")
  List<LightProcessInstance> getLightParentProcessInstancesWithInvolvedUserExcept(
      @PathParam("userId") String userId,
      @QueryParam("fromIndex") int fromIndex, 
      @QueryParam("pageSize") int pageSize,
      @FormParam("processes") Set<ProcessDefinitionUUID> processes,
      @FormParam("options") final Map<String, String> options)
  throws RemoteException;
  
  /**
   * Gets LightProcessInstances that are not sub-process instances and having the given userId member of the involved user order
   * by the given pagingCriterion.<br>
   * Instances of the given processes are ignored.
   * @param username
   * @param fromIndex
   * @param pageSize
   * @param processUUIDs
   * @param pagingCriterion the criterion used to sort the resulting list
   * @param options the options map (domain, queryList, user)
   * @return
   */
  @POST @Path("getLightParentProcessInstancesWithInvolvedUserExceptWithPagingCriterion/{username}")
  List<LightProcessInstance> getLightParentProcessInstancesWithInvolvedUserExcept(
  		@PathParam("username") String username, 
  		@QueryParam("fromIndex") int fromIndex, 
  		@QueryParam("pageSize") int pageSize, 
  		@FormParam("processUUIDs") Set<ProcessDefinitionUUID> processUUIDs, 
  		@QueryParam("pagingCriterion") ProcessInstanceCriterion pagingCriterion,
      @FormParam("options") final Map<String, String> options)
  throws RemoteException;
  
  /**
   * Gets LightProcessInstances that are not sub-process instances and having the given userId member of the active user.
   * @param userId
   * @param fromIndex
   * @param pageSize
   * @param options the options map (domain, queryList, user)
   * @return
   * @throws RemoteException
   */
  @POST @Path("getLightParentProcessInstancesWithInvolvedUserByProcesses/{userId}")
  List<LightProcessInstance> getLightParentProcessInstancesWithInvolvedUser(
      @PathParam("userId") String userId,
      @QueryParam("fromIndex") int fromIndex, 
      @QueryParam("pageSize") int pageSize,
      @FormParam("processes") Set<ProcessDefinitionUUID> processes,
      @FormParam("options") final Map<String, String> options)
  throws RemoteException;

  /**
   * Gets LightProcessInstances that are not sub-process instances and having the given userId member of the involved user order by the given pagingCriterion.<br>
   * @param userId
   * @param fromIndex
   * @param pageSize
   * @param processUUIDs
   * @param pagingCriterion the criterion used to sort the result list
   * @return
   */
  @POST @Path("getLightParentProcessInstancesWithInvolvedUserByProcessesAnPagingCriterion/{userId}")
  List<LightProcessInstance> getLightParentProcessInstancesWithInvolvedUser(
  		@PathParam("userId") String userId, 
  		@QueryParam("fromIndex") int fromIndex, 
  		@QueryParam("pageSize") int pageSize, 
  		@FormParam("processes") Set<ProcessDefinitionUUID> processUUIDs, 
  		@QueryParam("pagingCriterion") ProcessInstanceCriterion pagingCriterion,
      @FormParam("options") final Map<String, String> options)
  throws RemoteException;
  
	/**
	 * Gets the number of ProcessInstances that are not sub-process instances and having the given userId member of the active user.
   * A user is active in a ProcessInstance when he or she has currently a step to perform.
   * @param userId
	 * @param options the options map (domain, queryList, user)
	 * @return
	 * @throws RemoteException
	 */
	@POST @Path("getNumberOfParentProcessInstancesWithActiveUser/{userId}")
	Integer getNumberOfParentProcessInstancesWithActiveUser(
	    @PathParam("userId") String userId,
	    @FormParam("options") final Map<String, String> options) 
	throws RemoteException;
	
	/**
   * Gets the number of ProcessInstances that are not sub-process instances and having the given userId member of the active user.
   * A user is active in a ProcessInstance when he or she has currently a step to perform.
   * @param userId
   * @param options the options map (domain, queryList, user)
   * @return
   * @throws RemoteException
   */
  @POST @Path("getNumberOfParentProcessInstancesWithActiveUserExcept/{userId}")
  Integer getNumberOfParentProcessInstancesWithActiveUserExcept(
      @PathParam("userId") String userId,
      @FormParam("processes") Set<ProcessDefinitionUUID> processes,
      @FormParam("options") final Map<String, String> options) 
  throws RemoteException;
  
  /**
   * Gets the number of ProcessInstances that are not sub-process instances and having the given userId member of the active user.
   * A user is active in a ProcessInstance when he or she has currently a step to perform.
   * @param userId
   * @param options the options map (domain, queryList, user)
   * @return
   * @throws RemoteException
   */
  @POST @Path("getNumberOfParentProcessInstancesWithActiveUserAndProcessUUIDs/{userId}")
  Integer getNumberOfParentProcessInstancesWithActiveUser(
      @PathParam("userId") String userId,
      @FormParam("processes") Set<ProcessDefinitionUUID> processes,
      @FormParam("options") final Map<String, String> options) 
  throws RemoteException;

	/**
	 * Gets the number of ProcessInstances that are not sub-process instances and having the given userId member of the active users and having at least one task with the expected end date in the next {@code remainingDays}.
	 * @param userId
	 * @param remainingDays
	 * @param options the options map (domain, queryList, user)
	 * @return
	 * @throws RemoteException
	 */
	@POST	@Path("getNumberOfParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate/{userId}")
	Integer getNumberOfParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate(
	    @PathParam("userId") String userId, 
			@QueryParam("remainingDays") int remainingDays,
			@FormParam("options") final Map<String, String> options)
	throws RemoteException;
	
	/**
   * Gets the number of ProcessInstances that are not sub-process instances and having the given userId member of the active users and having at least one task with the expected end date in the next {@code remainingDays}.
   * @param userId
   * @param remainingDays
   * @param options the options map (domain, queryList, user)
   * @return
   * @throws RemoteException
   */
  @POST @Path("getNumberOfParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDateExcept/{userId}")
  Integer getNumberOfParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDateExcept(
      @PathParam("userId") String userId, 
      @QueryParam("remainingDays") int remainingDays,
      @FormParam("processes") Set<ProcessDefinitionUUID> processes,
      @FormParam("options") final Map<String, String> options)
  throws RemoteException;
  
  /**
   * Gets the number of ProcessInstances that are not sub-process instances and having the given userId member of the active users and having at least one task with the expected end date in the next {@code remainingDays}.
   * @param userId
   * @param remainingDays
   * @param options the options map (domain, queryList, user)
   * @return
   * @throws RemoteException
   */
  @POST @Path("getNumberOfParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDateAndProcessUUIDs/{userId}")
  Integer getNumberOfParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate(
      @PathParam("userId") String userId, 
      @QueryParam("remainingDays") int remainingDays,
      @FormParam("processes") Set<ProcessDefinitionUUID> processes,
      @FormParam("options") final Map<String, String> options)
  throws RemoteException;
  
	/**
	 * Gets the number of ProcessInstances that are not sub-process instances and having the given userId member of the active users and having at least one task with the expected end date in the past. 
	 * @param userId
	 * @param options the options map (domain, queryList, user)
	 * @return
	 * @throws RemoteException
	 */
	@POST @Path("getNumberOfParentProcessInstancesWithOverdueTasks/{userId}")
	Integer getNumberOfParentProcessInstancesWithOverdueTasks(
	    @PathParam("userId") String userId,
	    @FormParam("options") final Map<String, String> options) 
	throws RemoteException;
	
	/**
   * Gets the number of ProcessInstances that are not sub-process instances and having the given userId member of the active users and having at least one task with the expected end date in the past. 
   * @param userId
   * @param options the options map (domain, queryList, user)
   * @return
   * @throws RemoteException
   */
  @POST @Path("getNumberOfParentProcessInstancesWithOverdueTasksExcept/{userId}")
  Integer getNumberOfParentProcessInstancesWithOverdueTasksExcept(
      @PathParam("userId") String userId,
      @FormParam("processUUIDs") Set<ProcessDefinitionUUID> processUUIDs,
      @FormParam("options") final Map<String, String> options) 
  throws RemoteException;
  
  /**
   * Gets the number of ProcessInstances that are not sub-process instances and having the given userId member of the active users and having at least one task with the expected end date in the past. 
   * @param userId
   * @param options the options map (domain, queryList, user)
   * @return
   * @throws RemoteException
   */
  @POST @Path("getNumberOfParentProcessInstancesWithOverdueTasksAndProcessUUIDs/{userId}")
  Integer getNumberOfParentProcessInstancesWithOverdueTasks(
      @PathParam("userId") String userId,
      @FormParam("processUUIDs") Set<ProcessDefinitionUUID> processUUIDs,
      @FormParam("options") final Map<String, String> options) 
  throws RemoteException;

	/**
	 * Gets the number of ProcessInstances that are not sub-process instances and having the given userId member of the involved user.
	 * @param userId
	 * @param options the options map (domain, queryList, user)
	 * @return
	 * @throws RemoteException
	 */
	@POST	@Path("getNumberOfParentProcessInstancesWithInvolvedUser/{userId}")
	Integer getNumberOfParentProcessInstancesWithInvolvedUser(
	    @PathParam("userId") String userId,
	    @FormParam("options") final Map<String, String> options) 
	throws RemoteException;
	
	/**
   * Gets the number of ProcessInstances that are not sub-process instances and having the given userId member of the involved user.
   * @param userId
   * @param options the options map (domain, queryList, user)
   * @return
   * @throws RemoteException
   */
  @POST @Path("getNumberOfParentProcessInstancesWithInvolvedUserExcept/{userId}")
  Integer getNumberOfParentProcessInstancesWithInvolvedUserExcept(
      @PathParam("userId") String userId,
      @FormParam("processUUIDs") Set<ProcessDefinitionUUID> processUUIDs,
      @FormParam("options") final Map<String, String> options) 
  throws RemoteException;

  /**
   * Gets the number of ProcessInstances that are not sub-process instances and having the given userId member of the involved user.
   * @param userId
   * @param options the options map (domain, queryList, user)
   * @return
   * @throws RemoteException
   */
  @POST @Path("getNumberOfParentProcessInstancesWithInvolvedUserAndProcessUUIDs/{userId}")
  Integer getNumberOfParentProcessInstancesWithInvolvedUser(
      @PathParam("userId") String userId,
      @FormParam("processUUIDs") Set<ProcessDefinitionUUID> processUUIDs,
      @FormParam("options") final Map<String, String> options) 
  throws RemoteException;
  
	/**
	 * Gets the number of ProcessInstances that are not sub-process instances and having the given userId member of the active user.
	 * @param userId
	 * @param options the options map (domain, queryList, user)
	 * @return
	 * @throws RemoteException
	 */
	@POST @Path("getNumberOfParentProcessInstancesWithStartedBy/{userId}")
	Integer getNumberOfParentProcessInstancesWithStartedBy(
	    @PathParam("userId") String userId,
	    @FormParam("options") final Map<String, String> options) 
	throws RemoteException;
	
	/**
   * Gets the number of ProcessInstances that are not sub-process instances and having the given userId member of the active user.
   * @param userId
   * @param options the options map (domain, queryList, user)
   * @return
   * @throws RemoteException
   */
  @POST @Path("getNumberOfParentProcessInstancesWithStartedByExcept/{userId}")
  Integer getNumberOfParentProcessInstancesWithStartedByExcept(
      @PathParam("userId") String userId,
      @FormParam("processUUIDs") Set<ProcessDefinitionUUID> processUUIDs,
      @FormParam("options") final Map<String, String> options) 
  throws RemoteException;
  
  /**
   * Gets the number of ProcessInstances that are not sub-process instances and having the given userId member of the active user.
   * @param userId
   * @param options the options map (domain, queryList, user)
   * @return
   * @throws RemoteException
   */
  @POST @Path("getNumberOfParentProcessInstancesWithStartedByAndProcessUUIDs/{userId}")
  Integer getNumberOfParentProcessInstancesWithStartedBy(
      @PathParam("userId") String userId,
      @FormParam("processUUIDs") Set<ProcessDefinitionUUID> processUUIDs,
      @FormParam("options") final Map<String, String> options) 
  throws RemoteException;
  
	/**
	 * Gets the number of ProcessInstances that are not sub-process instances and having the given userId member of the involved users and the given category.
	 * @param userId
	 * @param category
	 * @param options the options map (domain, queryList, user)
	 * @return
	 * @throws RemoteException
	 */
	@POST @Path("getNumberOfParentProcessInstancesWithInvolvedUserAndCategory/{userId}")
	Integer getNumberOfParentProcessInstancesWithInvolvedUserAndCategory(
	    @PathParam("userId") String userId, 
	    @FormParam("category") String category, 
			@FormParam("options") final Map<String, String> options)
	throws RemoteException;
	
	/**
   * Gets the number of ProcessInstances that are not sub-process instances and having the given userId member of the involved users and the given category.
   * @param userId
   * @param category
   * @param options the options map (domain, queryList, user)
   * @return
   * @throws RemoteException
   */
  @POST @Path("getNumberOfParentProcessInstancesWithInvolvedUserAndCategoryExcept/{userId}")
  Integer getNumberOfParentProcessInstancesWithInvolvedUserAndCategoryExcept(
      @PathParam("userId") String userId,  
      @FormParam("category") String category, 
      @FormParam("processUUIDs") Set<ProcessDefinitionUUID> processUUIDs,
      @FormParam("options") final Map<String, String> options)
  throws RemoteException;

  /**
   * Gets the number of ProcessInstances that are not sub-process instances and having the given userId member of the involved users and the given category.
   * @param userId
   * @param category
   * @param options the options map (domain, queryList, user)
   * @return
   * @throws RemoteException
   */
  @POST @Path("getNumberOfParentProcessInstancesWithInvolvedUserAndCategoryAndProcessUUIDs/{userId}")
  Integer getNumberOfParentProcessInstancesWithInvolvedUserAndCategory(
      @PathParam("userId") String userId, 
      @FormParam("category") String category,
      @FormParam("processUUIDs") Set<ProcessDefinitionUUID> processUUIDs,
      @FormParam("options") final Map<String, String> options)
  throws RemoteException;
  
	/**
	 * Returns all records of instance for the given process processDefinitionUUID.<br>
   * An empty set is returned if no instance is found.
   * @param processUUID the process definition UUID.
	 * @param options the options map (domain, queryList, user)
	 * @return
	 * @throws ProcessNotFoundException
	 * @throws RemoteException
	 */
	@POST @Path("getProcessInstances/{processUUID}")
	Set<ProcessInstance> getProcessInstances(
	    @PathParam("processUUID") ProcessDefinitionUUID processUUID, 
      @FormParam("options") final Map<String, String> options)
  throws ProcessNotFoundException, RemoteException;

	/**
	 * Counts the number of process instances. 
	 * @param options the options map (domain, queryList, user)
	 * @return
	 * @throws RemoteException
	 */
	@POST	@Path("getNumberOfProcessInstances")
	int getNumberOfProcessInstances(
	    @FormParam("options") final Map<String, String> options)
	throws RemoteException;
  
  /**
   * Counts the number of process instances which are not a sub instance and are instances of the given process definitions.
   * @param processDefinitionUUIDs ProcessDefinitionUUIDs
   * @return the number of parent process instances
   */
	@POST  @Path("getNumberOfProcessInstancesWithProcessUUIDs")
  int getNumberOfParentProcessInstances(@FormParam("processUUIDs") Set<ProcessDefinitionUUID> processDefinitionUUIDs, @FormParam("options") final Map<String, String> options) throws RemoteException;
	
  /**
   * Counts the number of process instances which are not a sub instance and are not an instance of the given process definitions.
   * @param exceptions ProcessDefinitionUUIDs to be ignored
   * @param options the options map (domain, queryList, user)
   * @return the number of parent process instances
   */
	@POST  @Path("getNumberOfProcessInstancesExcept")
  int getNumberOfParentProcessInstancesExcept(@FormParam("processUUIDs") Set<ProcessDefinitionUUID> exceptions, @FormParam("options") final Map<String, String> options) throws RemoteException;
	
	/**
	 * Counts the number of process instances which are not a sub instance. 
	 * @param options the options map (domain, queryList, user)
	 * @return
	 * @throws RemoteException
	 */
	@POST	@Path("getNumberOfParentProcessInstances")
	int getNumberOfParentProcessInstances(
	    @FormParam("options") final Map<String, String> options)
	throws RemoteException;
  
	/**
	 * Returns the record of the activity with the given activity UUID.
   * @param activityUUID the activity UUID.
	 * @param options the options map (domain, queryList, user)
	 * @return the activity record with the given instance UUID and activity id.
	 * @throws ActivityNotFoundException
	 * @throws RemoteException
	 */
	@POST @Path("getActivityInstance/{activityUUID}")
	ActivityInstance getActivityInstance(
	    @PathParam("activityUUID") ActivityInstanceUUID activityUUID,
	    @FormParam("options") final Map<String, String> options)
	throws ActivityNotFoundException, RemoteException;

	/**
	 * Returns the record of the activity with the given activity instance UUID.
   * @param activityInstanceUUID the activity instance UUID.
	 * @param options the options map (domain, queryList, user)
	 * @return the record of the task with the given activity instance UUID.
	 * @throws ActivityNotFoundException
	 * @throws RemoteException
	 */
	@POST @Path("getLightActivityInstance/{activityInstanceUUID}")
	LightActivityInstance getLightActivityInstance(
	    @PathParam("activityInstanceUUID") ActivityInstanceUUID activityInstanceUUID, 
			@FormParam("options") final Map<String, String> options)
	throws ActivityNotFoundException, RemoteException;

	/**
	 * Returns all records of activity for the given process instance UUID.
   * @param instanceUUID the instance UUID.
	 * @param options the options map (domain, queryList, user)
	 * @return the unordered set containing activity records.
	 * @throws InstanceNotFoundException
	 * @throws RemoteException
	 */
	@POST	@Path("getActivityInstances/{instanceUUID}")
	Set<ActivityInstance> getActivityInstances(
	    @PathParam("instanceUUID") ProcessInstanceUUID instanceUUID, 
			@FormParam("options") final Map<String, String> options)
	throws InstanceNotFoundException, RemoteException;

	/**
	 * Returns all records of activity for the given process instance UUID.
   * @param instanceUUID the instance UUID.
	 * @param options the options map (domain, queryList, user)
	 * @return the unordered set containing activity records.
	 * @throws InstanceNotFoundException
	 * @throws RemoteException
	 */
	@POST @Path("getLightActivityInstances/{instanceUUID}")
	Set<LightActivityInstance> getLightActivityInstances(
	    @PathParam("instanceUUID") ProcessInstanceUUID instanceUUID, 
			@FormParam("options") final Map<String, String> options)
	throws InstanceNotFoundException, RemoteException;
	
	/**
   * Returns all records of activity for the given process instance UUID.
   * @param instanceUUID the instance UUID.
   * @param fromIdex the start index.
   * @param pageSize the max number of instances.
   * @param pagingCriterion the attribute used to order the list.
   * @return A list containing activity records order pagingCriterionterion.
   * @throws InstanceNotFoundException if no instance has been found with the given instance UUID.
   */
	@POST @Path("getLightActivityInstancesPaged/{instanceUUID}")
  List<LightActivityInstance> getLightActivityInstances(
  		@PathParam("instanceUUID") ProcessInstanceUUID instanceUUID,
  		@QueryParam("fromIdex") int fromIdex, 
  		@QueryParam("pageSize") int pageSize, 
  		@QueryParam("pagingCriterion") ActivityInstanceCriterion pagingCriterion, 
			@FormParam("options") final Map<String, String> options) 
  throws InstanceNotFoundException, RemoteException;

	/**
	 * Gets a list of light task instances which belong to a process instance according to its UUID.
   * @param rootInstanceUUID the process instance UUID
	 * @param options the options map (domain, queryList, user)
	 * @return a list of light task instances.
	 * @throws RemoteException
	 */
	@POST @Path("getLightTaskInstancesFromRoot/{rootInstanceUUID}")
	List<LightTaskInstance> getLightTaskInstancesFromRoot(
	    @PathParam("rootInstanceUUID") ProcessInstanceUUID rootInstanceUUID, 
			@FormParam("options") final Map<String, String> options)
	throws RemoteException;

	/**
	 * Gets for each process instance UUID the list of light task instances which belong to a process instance according to its UUID.
   * @param rootInstanceUUIDs the process instance UUIDs
	 * @param options the options map (domain, queryList, user)
	 * @return
	 * @throws RemoteException
	 */
	@POST @Path("getLightTaskInstancesFromRoot")
	Map<ProcessInstanceUUID, List<LightTaskInstance>> getLightTaskInstancesFromRoot(
	    @FormParam("rootInstanceUUIDs") Set<ProcessInstanceUUID> rootInstanceUUIDs, 
			@FormParam("options") final Map<String, String> options)
	throws RemoteException;

	/**
	 * Gets a list of light activity instances which belong to a process instance according to its UUID.
	 * @param rootInstanceUUID the process instance UUID
	 * @param options the options map (domain, queryList, user)
	 * @return a list of light activity instances.
	 * @throws RemoteException
	 */
	@POST @Path("getLightActivityInstancesFromRoot/{rootInstanceUUID}")
	List<LightActivityInstance> getLightActivityInstancesFromRoot(
	    @PathParam("rootInstanceUUID") ProcessInstanceUUID rootInstanceUUID, 
			@FormParam("options") final Map<String, String> options)
	throws RemoteException;

	/**
	 * Gets for each process instance UUID, the list of light activity instances which belong to a process instance according to its UUID.
   * @param rootInstanceUUIDs the process instance UUIDs
	 * @param options the options map (domain, queryList, user)
	 * @return
	 * @throws RemoteException
	 */
	@POST @Path("getLightActivityInstancesFromRoot")
	Map<ProcessInstanceUUID, List<LightActivityInstance>> getLightActivityInstancesFromRoot(
			@FormParam("rootInstanceUUIDs") Set<ProcessInstanceUUID> rootInstanceUUIDs,
			@FormParam("options") final Map<String, String> options) 
	throws RemoteException;

	/**
	 Gets for each process instance UUID, the list of light activity instances which belong to a process instance according to its UUID having the given state.
   * @param rootInstanceUUIDs the process instance UUIDs
	 * @param state
	 * @param options the options map (domain, queryList, user)
	 * @return
	 * @throws RemoteException
	 */
	@POST @Path("getLightActivityInstancesFromRootByState/{state}")
	Map<ProcessInstanceUUID,List<LightActivityInstance>> getLightActivityInstancesFromRoot(
	    @FormParam("rootInstanceUUIDs") Set<ProcessInstanceUUID> rootInstanceUUIDs,
	    @PathParam("state") ActivityState state,
	    @FormParam("options") final Map<String, String> options)
	throws RemoteException;

	/**
	 * Gets for each process instance UUID, the light activity instance that have been updated after all others
   * @param rootInstanceUUIDs the process instance UUIDs
	 * @param considerSystemTaks
	 * @param options the options map (domain, queryList, user)
	 * @return
	 * @throws RemoteException
	 */
	@POST @Path("getLightLastUpdatedActivityInstanceFromRoot/{considerSystemTaks}")
	Map<ProcessInstanceUUID, LightActivityInstance> getLightLastUpdatedActivityInstanceFromRoot(
			@FormParam("rootInstanceUUIDs") Set<ProcessInstanceUUID> rootInstanceUUIDs,
			@PathParam("considerSystemTaks") boolean considerSystemTaks,
			@FormParam("options") final Map<String, String> options)
	throws RemoteException;

	/**
	 * Returns records for all iterations and multi-instantiations that should append for the given
   * process instance UUID and activity Id.<br>
   * An empty set is returned if no instance is found.
   * @param instanceUUID the instance UUID.
   * @param activityId the activity id.
	 * @param options the options map (domain, queryList, user)
	 * @return
	 * @throws InstanceNotFoundException
	 * @throws ActivityNotFoundException
	 * @throws RemoteException
	 */
	@POST @Path("getActivityInstances/{instanceUUID}/{activityId}")
	Set<ActivityInstance> getActivityInstances(
	    @PathParam("instanceUUID") ProcessInstanceUUID instanceUUID, 
			@PathParam("activityId")String activityId,
			@FormParam("options") final Map<String, String> options)
  throws InstanceNotFoundException, ActivityNotFoundException, RemoteException;

	/**
	 * Returns records for all iterations and multi-instantiations that should append for the given
   * process instance UUID and activity Id.<br>
   * An empty set is returned if no instance is found.
   * @param instanceUUID the instance UUID.
   * @param activityName the activity name.
	 * @param options the options map (domain, queryList, user)
	 * @return
	 * @throws InstanceNotFoundException
	 * @throws ActivityNotFoundException
	 * @throws RemoteException
	 */
	@POST @Path("getLightActivityInstances/{instanceUUID}/{activityName}")
	Set<LightActivityInstance> getLightActivityInstances(
	    @PathParam("instanceUUID") ProcessInstanceUUID instanceUUID, 
			@PathParam("activityName") String activityName,
			@FormParam("options") final Map<String, String> options)
	throws InstanceNotFoundException, ActivityNotFoundException, RemoteException;

	/**
	 * Returns records for all multi-instantiations that should append for the given
   * process instance UUID, iteration ID and activity name.<br>
   * An empty set is returned if no instance is found.
   * @param instanceUUID the instance UUID.
   * @param activityName the activity name.
   * @param iterationId the iteration ID.
	 * @param options the options map (domain, queryList, user)
	 * @return
	 * @throws RemoteException
	 */
	@POST @Path("getLightActivityInstances/{instanceUUID}/{activityName}/{iterationId}")
	Set<LightActivityInstance> getLightActivityInstances(
	    @PathParam("instanceUUID") ProcessInstanceUUID instanceUUID, 
			@PathParam("activityName") String activityName,
			@PathParam("iterationId")String iterationId,
			@FormParam("options") final Map<String, String> options)
  throws RemoteException;

	/**
	 * Returns the record of the task with the given task UUID.
   * @param taskUUID the task UUID.
	 * @param options the options map (domain, queryList, user)
	 * @return the record of the task with the given task UUID.
	 * @throws TaskNotFoundException
	 * @throws RemoteException
	 */
	@POST @Path("getTask/{taskUUID}")
	TaskInstance getTask(
	    @PathParam("taskUUID") ActivityInstanceUUID taskUUID,
	    @FormParam("options") final Map<String, String> options) 
	throws TaskNotFoundException, RemoteException;

	/**
	 * Returns true if the given task is READY and:
   * - if the task is assigned: if the assigned user is the logged user
   * - if the task is not assigned: if the logged user is in the candidates list 
   * @param taskUUID the activity instance UUID of the task
	 * @param taskUUID
	 * @param options the options map (domain, queryList, user)
	 * @return true if the task can be executed
	 * @throws TaskNotFoundException
	 * @throws RemoteException
	 */
	@POST @Path("canExecuteTask/{taskUUID}")
	Boolean canExecuteTask(
	    @PathParam("taskUUID") ActivityInstanceUUID taskUUID,
	    @FormParam("options") final Map<String, String> options) 
	throws TaskNotFoundException, RemoteException;

	/**
	 * Returns all records of task for the given process instance UUID.
   * @param instanceUUID the instance UUID.
	 * @param options the options map (domain, queryList, user)
	 * @return a set containing all task records for the instance.
	 * @throws InstanceNotFoundException
	 * @throws RemoteException
	 */
	@POST @Path("getTasks/{instanceUUID}")
	Set<TaskInstance> getTasks(
	    @PathParam("instanceUUID") ProcessInstanceUUID instanceUUID,
	    @FormParam("options") final Map<String, String> options) 
	throws InstanceNotFoundException, RemoteException;

	/**
	 * See getTasks(ProcessInstanceUUID instanceUUID)
	 * @param instanceUUID
	 * @param options the options map (domain, queryList, user)
	 * @return
	 * @throws InstanceNotFoundException
	 * @throws RemoteException
	 */
	@POST @Path("getLightTasks/{instanceUUID}")
	Set<LightTaskInstance> getLightTasks(
	    @PathParam("instanceUUID") ProcessInstanceUUID instanceUUID,
	    @FormParam("options") final Map<String, String> options) 
	throws InstanceNotFoundException, RemoteException;

	/**
	 * Returns a set of tasks which are a task name from the set of task and the given process instance UUID and the task names
   * @param instanceUUID the instance UUID.
   * @param taskNames the set of task name
	 * @param options the options map (domain, queryList, user)
	 * @return
	 * @throws RemoteException
	 */
	@POST @Path("getLightTasksByTaskNames/{instanceUUID}")
	Set<LightTaskInstance> getLightTasks(
	    @PathParam("instanceUUID") ProcessInstanceUUID instanceUUID,
	    @FormParam("taskNames") Set<String> taskNames, 
			@FormParam("options") final Map<String, String> options)
	throws RemoteException;

	/**
	 * Obtains the user tasks with state either READY or EXECUTING or SUSPENDED or FINISHED
   * for the given instance and the authenticated user.<br>
   * @param instanceUUID the instance UUID.
   * @param taskState the {@link org.ow2.bonita.facade.runtime.ActivityState state} of the task.
	 * @param options the options map (domain, queryList, user)
	 * @return a collection of task records. If no tasks are found, an empty collection is returned.
	 * @throws InstanceNotFoundException
	 * @throws RemoteException
	 */
	@POST @Path("getTaskList/{instanceUUID}/{taskState}")
	Collection<TaskInstance> getTaskList(
	    @PathParam("instanceUUID") ProcessInstanceUUID instanceUUID,
	    @PathParam("taskState") ActivityState taskState, 
			@FormParam("options") final Map<String, String> options)
	throws InstanceNotFoundException, RemoteException;

	/**
	 * See getTaskList(ProcessInstanceUUID instanceUUID, ActivityState taskState)
	 * @param instanceUUID
	 * @param taskState
	 * @param options the options map (domain, queryList, user)
	 * @return
	 * @throws InstanceNotFoundException
	 * @throws RemoteException
	 */
	@POST	@Path("getLightTaskList/{instanceUUID}/{taskState}")
	Collection<LightTaskInstance> getLightTaskList(
	    @PathParam("instanceUUID") ProcessInstanceUUID instanceUUID,
	    @PathParam("taskState") ActivityState taskState, 
			@FormParam("options") final Map<String, String> options)
	throws InstanceNotFoundException, RemoteException;
  
	/**
	 * Obtains the user tasks with state either READY or EXECUTING or SUSPENDED or FINISHED
   * for the authenticated user.<br>
   * If the task has been assigned to a user, only this user can get the task into the returned list.<br>
   * Otherwise all the users that belong to the candidate list can get the task.
   * @param taskState the {@link org.ow2.bonita.facade.runtime.ActivityState state} of the task.
	 * @param options the options map (domain, queryList, user)
	 * @return a collection of task records. If no tasks are found, an empty collection is returned.
	 * @throws RemoteException
	 */
	@POST @Path("getTaskListByActivityState/{taskState}")
	Collection<TaskInstance> getTaskList(
	    @PathParam("taskState") ActivityState taskState,
	    @FormParam("options") final Map<String, String> options) 
	throws RemoteException;
  
	/**
	 * See getTaskList(ActivityState taskState).
	 * @param taskState
	 * @param options the options map (domain, queryList, user)
	 * @return
	 * @throws RemoteException
	 */
	@POST @Path("getLightTaskListByActivityState/{taskState}")
	Collection<LightTaskInstance> getLightTaskList(
	    @PathParam("taskState") ActivityState taskState,
	    @FormParam("options") final Map<String, String> options) 
	throws RemoteException;

	/**
	 * Gets an activity UUID among all available tasks of the logged user according to the given
   * activity state and the given process instance UUID.
   * @param instanceUUID the process instance UUID
   * @param taskState the activity state
	 * @param options the options map (domain, queryList, user)
	 * @return an activity UUID
	 * @throws RemoteException
	 */
	@POST @Path("getOneTaskByProcessInstanceUUIDAndActivityState/{instanceUUID}/{taskState}")
	ActivityInstanceUUID getOneTask(
	    @PathParam("instanceUUID") ProcessInstanceUUID instanceUUID,
	    @PathParam("taskState") ActivityState taskState, 
			@FormParam("options") final Map<String, String> options)
	throws RemoteException;

	/**
	 * Gets an activity UUID among all available tasks of the logged user according to the given
   * activity state and the given process UUID.
   * @param processUUID the process UUID
   * @param taskState the activity state
	 * @param options the options map (domain, queryList, user)
	 * @return an activity UUID
	 * @throws RemoteException
	 */
	@POST @Path("getOneTaskByProcessDefinitionUUIDAndActivityState/{processUUID}/{taskState}")
	ActivityInstanceUUID getOneTask(
	    @PathParam("processUUID") ProcessDefinitionUUID processUUID,
	    @PathParam("taskState") ActivityState taskState, 
			@FormParam("options") final Map<String, String> options)
	throws RemoteException;

	/**
	 * Gets an activity UUID among all available tasks of the logged user according to the given
   * activity state.
   * @param taskState the activity state
	 * @param options the options map (domain, queryList, user)
	 * @return an activity UUID
	 * @throws RemoteException
	 */
	@POST @Path("getOneTask/{taskState}")
	ActivityInstanceUUID getOneTask(
	    @PathParam("taskState") ActivityState taskState,
	    @FormParam("options") final Map<String, String> options) 
	throws RemoteException;

	/**
	 * Obtains a variable defined as local to the activity for the given activity UUID and variable name.
   * The activity should either be executed or currently pointed by the a process execution.<br>
   * @param activityUUID the activity UUID.
   * @param variableId the variable name.
	 * @param options the options map (domain, queryList, user)
	 * @return the variable object (can be: a plain {@link String}, a {@link Boolean}, a {@link Date}, a {@link Long} or a {@link Double}).
	 * @throws ActivityNotFoundException
	 * @throws VariableNotFoundException
	 * @throws RemoteException
	 */
	@POST @Path("getActivityInstanceVariable/{activityUUID}")
	Object getActivityInstanceVariable(
	    @PathParam("activityUUID") ActivityInstanceUUID activityUUID,
      @FormParam("variableId") String variableId,
      @FormParam("options") final Map<String, String> options)
	throws ActivityNotFoundException, VariableNotFoundException, RemoteException;

	/**
	 * Gets the activity state of the activity according to its UUID
   * @param activityUUID the activity UUID
	 * @param options the options map (domain, queryList, user)
	 * @return the activity state of the activity according to its UUID
	 * @throws ActivityNotFoundException
	 * @throws RemoteException
	 */
	@POST @Path("getActivityInstanceState/{activityUUID}")
	ActivityState getActivityInstanceState(
	    @PathParam("activityUUID") ActivityInstanceUUID activityUUID,
      @FormParam("options") final Map<String, String> options)
	throws ActivityNotFoundException, RemoteException;
  
	/**
	 * Obtains the variables defined as local to the activity for the given activity UUID.<br>
   * An empty map is returned if no variable is found.
   * @param activityUUID the activity UUID.
	 * @param options the options map (domain, queryList, user)
	 * @return the map of activity variables where key is the variable id and value is the variable object
   * (can be: a plain {@link String}, a {@link Boolean}, a {@link Date}, a {@link Long} or a {@link Double})).
	 * @throws ActivityNotFoundException
	 * @throws RemoteException
	 */
	@POST @Path("getActivityInstanceVariables/{activityUUID}")
	Map<String, Object> getActivityInstanceVariables(
	    @PathParam("activityUUID") ActivityInstanceUUID activityUUID, 
			@FormParam("options") final Map<String, String> options)
	throws ActivityNotFoundException, RemoteException;

	/**
	 * Obtains a process variable for the given process instance UUID and variable name.
   * @param instanceUUID the instance UUID.
   * @param variableId the variable name.
	 * @param options the options map (domain, queryList, user)
	 * @return the variable object (can be: a plain {@link String}, a {@link Boolean}, a {@link Date},
   * a {@link Long} or a {@link Double}).
	 * @throws InstanceNotFoundException
	 * @throws VariableNotFoundException
	 * @throws RemoteException
	 */
	@POST @Path("getProcessInstanceVariable/{instanceUUID}")	
	Object getProcessInstanceVariable(
	    @PathParam("instanceUUID") ProcessInstanceUUID instanceUUID,
      @FormParam("variableId") String variableId,
      @FormParam("options") final Map<String, String> options) 
	throws InstanceNotFoundException, VariableNotFoundException, RemoteException;

	/**
	 * Obtains the process variables for the given process instance UUID.
   * An empty map is returned if no process variable is found.
   * @param instanceUUID the instance UUID.
	 * @param options the options map (domain, queryList, user)
	 * @return the map of process variables where key is the variable id and value is the variable object.
	 * @throws InstanceNotFoundException
	 * @throws RemoteException
	 */
	@POST @Path("getProcessInstanceVariables/{instanceUUID}")
	Map<String, Object> getProcessInstanceVariables(
	    @PathParam("instanceUUID") ProcessInstanceUUID instanceUUID, 
			@FormParam("options") final Map<String, String> options)
	throws InstanceNotFoundException, RemoteException;

	/**
	 * Obtains the process variables for the given process instance UUID at the given date.
   * @param instanceUUID the instance UUID
   * @param maxDate
	 * @param options the options map (domain, queryList, user)
	 * @return the map of process variables where key is the variable id and value is the variable object or an empty map.
	 * @throws RemoteException
	 * @throws InstanceNotFoundException
	 */
	@POST @Path("getProcessInstanceVariablesWithMaxDate/{instanceUUID}")
	Map<String, Object> getProcessInstanceVariables(
	    @PathParam("instanceUUID") ProcessInstanceUUID instanceUUID,
	    @QueryParam("maxDate") Date maxDate,
	    @FormParam("options") final Map<String, String> options)
  throws RemoteException, InstanceNotFoundException;

	/**
	 * Obtains a variable for the given activity and variable name.
   * This variable could be local to the activity or global to the process.
   * The activity should either be executed or currently pointed by the a process execution.
   * <p>
   * <i>For XML Type:</i>
   * <ul>
   * <li>getVariable(activityUUID, "myXmlData") returns a {@link Document}
   * <li>getVariable(activityUUID, "myXmlData" + {@link BonitaConstants#XPATH_VAR_SEPARATOR} + "/root/node") returns a {@link Node}</li>
   * <li>getVariable(activityUUID, "myXmlData" + {@link BonitaConstants#XPATH_VAR_SEPARATOR} + "/root/@attribute") returns a {@link String}</li>
   * <li>getVariable(activityUUID, "myXmlData" + {@link BonitaConstants#XPATH_VAR_SEPARATOR} + "/root/text()") returns a {@link String}</li>
   * <li>getVariable(activityUUID, "myXmlData" + {@link BonitaConstants#XPATH_VAR_SEPARATOR} + anyOtherKindOfXPathExpression) returns a {@link Node}</li>
   * </ul>
   * </p>
   * @param activityUUID the activity UUID.
   * @param variableId the variable name.
	 * @param options the options map (domain, queryList, user)
	 * @return the variable object (can be: a plain {@link String}, a {@link Boolean}, a {@link Date},
   * a {@link Long} or a {@link Double}).
	 * @throws ActivityNotFoundException
	 * @throws VariableNotFoundException
	 * @throws RemoteException
	 */
	@POST	@Path("getVariable/{activityUUID}")
	Object getVariable(
	    @PathParam("activityUUID") ActivityInstanceUUID activityUUID,
			@FormParam("variableId") String variableId,
			@FormParam("options") final Map<String, String> options) 
	throws ActivityNotFoundException, VariableNotFoundException, RemoteException;

	/**
	 * Obtains the activity variables (including global process and local activity variables)
   * for the given activity UUID.<br>
   * An empty map is returned if no variable is found.
   * @param activityUUID the activity UUID.
	 * @param options the options map (domain, queryList, user)
	 * @return the map of activity variables where key is the variable id and value is the variable object
	 * @throws InstanceNotFoundException
	 * @throws ActivityNotFoundException
	 * @throws RemoteException
	 */
	@POST @Path("getVariables/{activityUUID}")
	Map<String, Object> getVariables(
	    @PathParam("activityUUID") ActivityInstanceUUID activityUUID,
	    @FormParam("options") final Map<String, String> options)
	throws InstanceNotFoundException, ActivityNotFoundException, RemoteException;

	/**
	  Obtains the tasks with state either READY or EXECUTING or SUSPENDED or FINISHED for the given instance
   * and for the given user.<br>
   * If the task has been assigned to a user, only this user can get the task into the returned list.
   * Otherwise all the users that belong to the candidate list can get the task.
   * @param instanceUUID the instance UUID.
   * @param userId the userId for which the tasks are searched.
   * @param taskState the {@link org.ow2.bonita.facade.runtime.ActivityState state} of the task.
	 * @param options the options map (domain, queryList, user)
	 * @return a collection of task records. If no tasks are found, an empty collection is returned.
	 * @throws InstanceNotFoundException
	 * @throws RemoteException
	 */
	@POST @Path("getTaskList/{instanceUUID}/{userId}/{taskState}")
	Collection<TaskInstance> getTaskList(
	    @PathParam("instanceUUID") ProcessInstanceUUID instanceUUID,
	    @PathParam("userId") String userId, 
			@PathParam("taskState") ActivityState taskState,
			@FormParam("options") final Map<String, String> options)
	throws InstanceNotFoundException, RemoteException;
  
	/**
	 * See getTaskList(ProcessInstanceUUID instanceUUID, String userId, ActivityState taskState)
	 * @param instanceUUID
	 * @param userId
	 * @param taskState
	 * @param options the options map (domain, queryList, user)
	 * @return
	 * @throws InstanceNotFoundException
	 * @throws RemoteException
	 */
	@POST @Path("getLightTaskList/{instanceUUID}/{userId}/{taskState}")
	Collection<LightTaskInstance> getLightTaskList(
	    @PathParam("instanceUUID") ProcessInstanceUUID instanceUUID,
	    @PathParam("userId") String userId, 
			@PathParam("taskState") ActivityState taskState,
			@FormParam("options") final Map<String, String> options) 
	throws InstanceNotFoundException, RemoteException;

	/**
	 * Obtains the user tasks with state either READY or EXECUTING or SUSPENDED or FINISHED
   * for the given user.<br>
   * If the task has been assigned to a user, only this user can get the task into the returned list.<br>
   * Otherwise all the users that belong to the candidate list can get the task.
   * @param userId the userId for which the tasks are searched.
   * @param taskState the {@link org.ow2.bonita.facade.runtime.ActivityState state} of the task.
	 * @param options the options map (domain, queryList, user)
	 * @return a collection of task records. If no tasks are found, an empty collection is returned.
	 * @throws RemoteException
	 */
	@POST @Path("getTaskListByUserIdAndActivityState/{userId}/{taskState}")
	Collection<TaskInstance> getTaskList(
	    @PathParam("userId") String userId,
	    @PathParam("taskState") ActivityState taskState, 
			@FormParam("options") final Map<String, String> options)
	throws RemoteException;

	/**
	 * See getTaskList(String userId, ActivityState taskState)
	 * @param userId
	 * @param taskState
	 * @param options the options map (domain, queryList, user)
	 * @return
	 * @throws RemoteException
	 */
	@POST @Path("getLightTaskListByUserId/{userId}/{taskState}")
	Collection<LightTaskInstance> getLightTaskList(
	    @PathParam("userId") String userId,
	    @PathParam("taskState") ActivityState taskState, 
			@FormParam("options") final Map<String, String> options)
	throws RemoteException;

  /* COMMENT FEED */
	/**
	 * Obtains all the comments (activity and process) of a ProcessInstance.
   * An empty List is returned if the Process has no feed.
   * @param instanceUUID the instance UUID
	 * @param options the options map (domain, queryList, user)
	 * @return the list containing all the comments
	 * @throws InstanceNotFoundException
	 * @throws RemoteException
	 */
	@POST @Path("getCommentFeed/{instanceUUID}")
	List<Comment> getCommentFeed(
	    @PathParam("instanceUUID") ProcessInstanceUUID instanceUUID,
	    @FormParam("options") final Map<String, String> options) 
	throws InstanceNotFoundException, RemoteException;

	/**
	 * Obtains the comments of an activity.
   * @param activityUUID the activity UUID
	 * @param options the options map (domain, queryList, user)
	 * @return the list containing activity comments
	 * @throws InstanceNotFoundException
	 * @throws RemoteException
	 */
	@POST @Path("getActivityInstanceCommentFeed/{activityUUID}")
	List<Comment> getActivityInstanceCommentFeed(
	    @PathParam("activityUUID") ActivityInstanceUUID activityUUID, 
			@FormParam("options") final Map<String, String> options)
	throws InstanceNotFoundException, RemoteException;
  
	/**
	 * Obtains the comments belonging to the process.
   * @param instanceUUID the instance UUID
	 * @param options the options map (domain, queryList, user)
	 * @return the list containing the process comments
	 * @throws InstanceNotFoundException
	 * @throws RemoteException
	 */
	@POST @Path("getProcessInstanceCommentFeed/{instanceUUID}")
	List<Comment> getProcessInstanceCommentFeed(
	    @PathParam("instanceUUID") ProcessInstanceUUID instanceUUID, 
			@FormParam("options") final Map<String, String> options)
	throws InstanceNotFoundException, RemoteException;

	/**
	 * Counts the number of comments of an activity.
   * @param activityUUID the activity UUID
	 * @param options the options map (domain, queryList, user)
	 * @return the number of comments of an activity
	 * @throws InstanceNotFoundException
	 * @throws RemoteException
	 */
	@POST @Path("getNumberOfActivityInstanceComments/{activityUUID}")
	int getNumberOfActivityInstanceComments(
	    @PathParam("activityUUID") ActivityInstanceUUID activityUUID, 
			@FormParam("options") final Map<String, String> options)
	throws InstanceNotFoundException, RemoteException;

	/**
	 * Counts the number of comments of all given activities.
   * @param activityUUIDs
	 * @param options the options map (domain, queryList, user)
	 * @return
	 * @throws InstanceNotFoundException
	 * @throws RemoteException
	 */
	@POST @Path("getNumberOfActivityInstanceComments")
	Map<ActivityInstanceUUID, Integer> getNumberOfActivityInstanceComments(
	    @FormParam("activityUUIDs") Set<ActivityInstanceUUID> activityUUIDs, 
			@FormParam("options") final Map<String, String> options)
	throws InstanceNotFoundException, RemoteException;

	/**
	 * Counts the number of comments of a process.
   * @param instanceUUID the instance UUID
	 * @param options the options map (domain, queryList, user)
	 * @return the number of comments of a process
	 * @throws InstanceNotFoundException
	 * @throws RemoteException
	 */
	@POST @Path("getNumberOfProcessInstanceComments/{instanceUUID}")
	int getNumberOfProcessInstanceComments(
	    @PathParam("instanceUUID") ProcessInstanceUUID instanceUUID, 
			@FormParam("options") final Map<String, String> options)
	throws InstanceNotFoundException, RemoteException;

	/**
	 * Counts the number of all comments (activity and process) of a process.
   * @param instanceUUID  the instance UUID
	 * @param options the options map (domain, queryList, user)
	 * @return the number of all comments (activity and process) of a process
	 * @throws InstanceNotFoundException
	 * @throws RemoteException
	 */
	@POST @Path("getNumberOfComments/{instanceUUID}")
	int getNumberOfComments(
	    @PathParam("instanceUUID") ProcessInstanceUUID instanceUUID,
	    @FormParam("options") final Map<String, String> options) 
	throws InstanceNotFoundException, RemoteException;

	/**	 
   * Returns all instances started by the logged user 
	 * @param options the options map (domain, queryList, user)
	 * @return all instances started by the logged user
	 * @throws RemoteException
	 */
	@POST @Path("getUserInstances")
	Set<ProcessInstance> getUserInstances(
	    @FormParam("options") final Map<String, String> options)
	throws RemoteException;
  
	/**
	 * Returns all instances started by the logged user
	 * @param options the options map (domain, queryList, user)
	 * @return all instances started by the logged user
	 * @throws RemoteException
	 */
	@POST @Path("getLightUserInstances")
	Set<LightProcessInstance> getLightUserInstances(
	    @FormParam("options") final Map<String, String> options)
	throws RemoteException;

	/**
   * Returns at most pageSize instances started by the given user.</br>
   * Only instances of the given processes are considered.
   * @param fromIndex
   * @param pageSize
   * @param processUUIDs
   * @param options the options map (domain, queryList, user)
   * @return
   */
	@POST @Path("getLightParentUsersInstances")
	 List<LightProcessInstance> getLightParentUserInstances(
	     @QueryParam("fromIndex") int fromIndex,
	     @QueryParam("pageSize") int pageSize,
	     @FormParam("processUUIDs") Set<ProcessDefinitionUUID> processUUIDs,
	     @FormParam("options") final Map<String, String> options)
	 throws RemoteException;
	
	/**
   * Returns at most pageSize instances started by the given user.</br>
   * Only instances of the given processes are considered.
   * @param fromIndex
   * @param pageSize
   * @param processUUIDs
   * @param pagingCriterion the criterion used to sort the result list
   * @return
   */
	@POST @Path("getLightParentUserInstancesFromProcessUUIDsWithPagingCriterion")
  List<LightProcessInstance> getLightParentUserInstances(
  		@QueryParam("fromIndex") int fromIndex, 
  		@QueryParam("pageSize") int pageSize, 
  		@FormParam("processUUIDs") Set<ProcessDefinitionUUID> processUUIDs, 
  		@QueryParam("pagingCriterion") ProcessInstanceCriterion pagingCriterion,
  		@FormParam("options") final Map<String, String> options)
  	throws RemoteException;

	/**
   * Returns at most pageSize instances started by the given user.</br>
   * Instances of the given processes are ignored.
   * @param username
   * @param fromIndex
   * @param pageSize
   * @param processUUIDs
   * @param options the options map (domain, queryList, user)
   * @return
   */
	@POST @Path("getLightParentUserInstancesExcept")
  List<LightProcessInstance> getLightParentUserInstancesExcept(
      @QueryParam("fromIndex") int fromIndex,
      @QueryParam("pageSize")int pageSize,
      @FormParam("processUUIDs") Set<ProcessDefinitionUUID> processUUIDs,
      @FormParam("options") final Map<String, String> options)
  throws RemoteException;

	/**
   * Returns at most pageSize instances started by the current user order by pagingCriterion.</br>
   * Instances of the given processes are ignored.
   * @param fromIndex
   * @param pageSize
   * @param processUUIDs
   * @param pagingCriterion the criterion used to sort the result list
   * @param options the options map (domain, queryList, user)
   * @return
   */
	@POST @Path("getLightParentUserInstancesExceptWithPagingCriterion")
  List<LightProcessInstance> getLightParentUserInstancesExcept(
  		@QueryParam("fromIndex") int fromIndex, 
  		@QueryParam("pageSize") int pageSize, 
  		@FormParam("processUUIDs") Set<ProcessDefinitionUUID> processUUIDs, 
  		@QueryParam("pagingCriterion") ProcessInstanceCriterion pagingCriterion,
      @FormParam("options") final Map<String, String> options)
  throws RemoteException;
	
	/**
	 * Returns at most pageSize instances started by the logged user
	 * @param startingIndex
	 * @param pageSize
	 * @param options the options map (domain, queryList, user)
	 * @return instances started by the logged user from startingIndex to pageSize order by last update
	 * @throws RemoteException
	 */
	@POST @Path("getLightParentUserInstances")
	List<LightProcessInstance> getLightParentUserInstances(
	    @QueryParam("startingIndex") int startingIndex,
	    @QueryParam("pageSize") int pageSize, 
			@FormParam("options") final Map<String, String> options)
	throws RemoteException;
	
	/**
   * Returns at most pageSize instances started by the logged user
   * @param fromIndex
   * @param pageSize
   * @param pagingCriterion the criterion used to sort the returned instances
   * @return instances started by the logged user from fromIndex to pageSize order by pagingCriterion 
   */
	@POST @Path("getLightParentUserInstancesWithPagingCriterion")
  List<LightProcessInstance> getLightParentUserInstances(
  		@QueryParam("fromIndex") int fromIndex, 
  		@QueryParam("pageSize") int pageSize, 
  		@QueryParam("pagingCriterion") ProcessInstanceCriterion pagingCriterion,
  		@FormParam("options") final Map<String, String> options) throws RemoteException;

	/**
	 * Obtains the process attachment names.
   * @param instanceUUID the instance UUID
	 * @param options the options map (domain, queryList, user)
	 * @return the attachment names of a process instance
	 * @throws RemoteException
	 */
	@POST @Path("getAttachmentNames/{instanceUUID}")
	Set<String> getAttachmentNames(
	    @PathParam("instanceUUID") ProcessInstanceUUID instanceUUID,
	    @FormParam("options") final Map<String, String> options) 
	throws RemoteException;

	/**
	 * Obtains for each attachment (given by its name) the its last version for a process instance
   * @param instanceUUID the process instance UUID
   * @param attachmentNames the attachment names
	 * @param options the options map (domain, queryList, user)
	 * @return a collection of {@link AttachmentInstance}. The method getAttachmentValue should be used to retrieve the content of an attachment
	 * @throws RemoteException
	 */
	@POST @Path("getLastAttachments/{instanceUUID}")
	Collection<AttachmentInstance> getLastAttachments(
	    @PathParam("instanceUUID") ProcessInstanceUUID instanceUUID, 
			@FormParam("attachmentNames")Set<String> attachmentNames,
			@FormParam("options") final Map<String, String> options)
	throws RemoteException;

	/**
	 * Obtains the last versions of some process attachments (the attachment name should match with the regular expression)
   * @param instanceUUID the instance UUID
   * @param regex the regular expression
	 * @param options the options map (domain, queryList, user)
	 * @return a collection of {@link AttachmentInstance}. The method getAttachmentValue should be used to retrieve the content of an attachment
	 * @throws RemoteException
	 */
	@POST @Path("getLastAttachmentsByRegex/{instanceUUID}")
	Collection<AttachmentInstance> getLastAttachments(
	    @PathParam("instanceUUID") ProcessInstanceUUID instanceUUID,
	    @FormParam("regex")String regex, 
			@FormParam("options") final Map<String, String> options)
	throws RemoteException;
  
	/**
	 * Obtains versions of a process attachment according to its UUID and the attachment name.
   * @param instanceUUID the process instance UUID
   * @param attachmentName the attachment name
	 * @param options the options map (domain, queryList, user)
	 * @return a list of {@link AttachmentInstance}. The method getAttachmentValue should be used to retrieve the content of an attachment
	 * @throws RemoteException
	 */
	@POST @Path("getAttachments/{instanceUUID}/{attachmentName}")
	List<AttachmentInstance> getAttachments(
	    @PathParam("instanceUUID") ProcessInstanceUUID instanceUUID,
	    @PathParam("attachmentName") String attachmentName, 
			@FormParam("options") final Map<String, String> options)
	throws RemoteException;

	/**
	 * Gets the content of an attachment instance as a byte array.
   * This method is the only way to retrieve an attachment content from an attachment instance.
   * @param attachmentInstance the attachment instance
	 * @param options the options map (domain, queryList, user)
	 * @return the content of an attachment instance
	 * @throws RemoteException
	 */
	@POST @Path("getAttachmentValue")
	byte[] getAttachmentValue(
	    @FormParam("attachmentInstance") AttachmentInstance attachmentInstance,
	    @FormParam("options") final Map<String, String> options) 
	throws RemoteException;

	/**
	 * Obtains the last attachment of a process according to its UUID and the attachment name.
   * @param instanceUUID the process instance UUID
   * @param attachmentName the attachment name
	 * @param options the options map (domain, queryList, user)
	 * @return an {@link AttachmentInstance} corresponding to the last version of the attachment. The method getAttachmentValue should be used to retrieve the content of an attachment
	 * @throws RemoteException
	 */
	@POST @Path("getLastAttachment/{instanceUUID}/{attachmentName}")
	AttachmentInstance getLastAttachment(
	    @PathParam("instanceUUID") ProcessInstanceUUID instanceUUID,
	    @PathParam("attachmentName") String attachmentName, 
			@FormParam("options") final Map<String, String> options)
	throws RemoteException;
  
	/**
	 * Obtains the last version of a process attachment created before the given date.
   * @param instanceUUID the process instance UUID
   * @param attachmentName the attachment name
   * @param date the date
	 * @param options the options map (domain, queryList, user)
	 * @return an {@link AttachmentInstance} corresponding to the last version of the attachment created before the date. The method getAttachmentValue should be used to retrieve the content of an attachment
	 * @throws RemoteException
	 */
	@POST @Path("getLastAttachmentWihDate/{instanceUUID}/{attachmentName}")
	AttachmentInstance getLastAttachment(
	    @PathParam("instanceUUID") ProcessInstanceUUID instanceUUID,
	    @PathParam("attachmentName") String attachmentName, 
			@FormParam("date") Date date,
			@FormParam("options") final Map<String, String> options)
	throws RemoteException;
	
	
	/**
   * Obtains the last version of a process document created before the given date.
   * 
   * @param instanceUUID
   *          the process instance UUID
   * @param documentName
   *          the document name
   * @param date
   *          the date
   * @param options 
   *          the options map (domain, queryList, user)
   * @return a {@link Document} corresponding to the last version of the document created before the date.
   *         The method {@link #getDocumentContent(DocumentUUID)} should be used to retrieve the content of a document
   */
	@POST @Path("getLastDocumenttWihDate/{instanceUUID}/{documentName}")
  Document getLastDocument(
      @PathParam("instanceUUID") ProcessInstanceUUID instanceUUID, 
      @PathParam("documentName") String documentName, 
      @FormParam("date") Date date,
      @FormParam("options") final Map<String, String> options)
  throws RemoteException;
	

	/**
	 * Obtains the last version of a process attachment created before an activity end.
   * @param instanceUUID the process instance UUID
   * @param attachmentName the attachment name
   * @param activityUUID the activity instance UUID
	 * @param options the options map (domain, queryList, user)
	 * @return an {@link AttachmentInstance} corresponding to the last version of the attachment created before the end of the activity. The method getAttachmentValue should be used to retrieve the content of an attachment
	 * @throws ActivityNotFoundException
	 * @throws RemoteException
	 */
	@POST	@Path("getLastAttachment/{instanceUUID}/{attachmentName}/{activityUUID}")
	AttachmentInstance getLastAttachment(
	    @PathParam("instanceUUID") ProcessInstanceUUID instanceUUID,
	    @PathParam("attachmentName") String attachmentName, 
			@PathParam("activityUUID") ActivityInstanceUUID activityUUID,
			@FormParam("options") final Map<String, String> options)
  throws ActivityNotFoundException, RemoteException;
	
	/**
   * Obtains the last version of a process document created before an activity end.
   * 
   * @param instanceUUID
   *          the process instance UUID
   * @param documentName
   *          the document name
   * @param activityUUID
   *          the activity instance UUID
   * @param options 
   *          the options map (domain, queryList, user)
   * @return {@link Document} corresponding to the last version of the document created before the end of
   *         the activity. The method {@link #getDocumentContent(DocumentUUID)} should be used to retrieve the content of a document
   * @throws ActivityNotFoundException
   *           if this activity is not pointed by a process execution and the execution informations for this activity
   *           has not been recorded.
   */
	@POST  @Path("getLastDocument/{instanceUUID}/{documentName}/{activityUUID}")
  Document getLastDocument(
      @PathParam("instanceUUID") ProcessInstanceUUID instanceUUID, 
      @PathParam("documentName") String documentName,
      @PathParam("activityUUID") ActivityInstanceUUID activityUUID,
      @FormParam("options") final Map<String, String> options) 
  throws ActivityNotFoundException, RemoteException;

	/**
	 * Returns the record of the task with the given task UUID.
   * @param taskUUID the task UUID.
	 * @param options the options map (domain, queryList, user)
	 * @return the record of the task with the given task UUID.
	 * @throws TaskNotFoundException
	 * @throws RemoteException
	 */
	@POST	@Path("getLightTaskInstance/{activityUUID}")
	LightTaskInstance getLightTaskInstance(
	    @PathParam("activityUUID") ActivityInstanceUUID activityUUID, 
			@FormParam("options") final Map<String, String> options)
  throws TaskNotFoundException, RemoteException;

	/**
	 * Returns the candidates of the task with the given task UUID.
   * @param taskUUID the task UUID.
	 * @param options the options map (domain, queryList, user)
	 * @return the candidates of the task with the given task UUID.
	 * @throws RemoteException
	 * @throws TaskNotFoundException
	 */
	@POST @Path("getTaskCandidates/{taskUUID}")
	Set<String> getTaskCandidates(
	    @PathParam("taskUUID") final ActivityInstanceUUID taskUUID,
	    @FormParam("options") final Map<String, String> options) 
	throws RemoteException, TaskNotFoundException;

	/**
	 * Returns the candidates of the tasks with the given task UUID.
   * @param taskUUID the task UUID.
	 * @param options the options map (domain, queryList, user)
	 * @return the candidates of the tasks with the given task UUID.
	 * @throws RemoteException
	 * @throws TaskNotFoundException
	 */
	@POST @Path("getTaskCandidates")
	Map<ActivityInstanceUUID, Set<String>> getTaskCandidates(
	    @FormParam("taskUUIDs") final Set<ActivityInstanceUUID> taskUUIDs, 
			@FormParam("options") final Map<String, String> options)
	throws RemoteException, TaskNotFoundException;

	/**
	 * @param query
	 * @param options the options map (domain, queryList, user)
	 * @return
	 * @throws RemoteException
	 */
	@POST @Path("search")
	int search(
	    @FormParam("query") SearchQueryBuilder query,
	    @FormParam("options") final Map<String, String> options)
	throws RemoteException;
	
	/**
	 * @param <T>
	 * @param query
	 * @param firstResult
	 * @param maxResults
	 * @param options the options map (domain, queryList, user)
	 * @return
	 * @throws RemoteException
	 */
	@POST @Path("searchByMaxResult")
	<T> List<T> search(
	    @FormParam("query") SearchQueryBuilder query,
	    @QueryParam("firstResult")int firstResult,
	    @QueryParam("maxResults")int maxResults, 
			@FormParam("options") final Map<String, String> options)
	throws RemoteException;

	/**
   * List name of active users.
   * @param uuid
   * @return
   * @throws InstanceNotFoundException 
   */
	@POST @Path("getActiveUsersOfProcessInstance/{instanceuuid}")
  Set<String> getActiveUsersOfProcessInstance(
      @PathParam("instanceuuid") ProcessInstanceUUID uuid, 
      @FormParam("options") final Map<String, String> options)
  throws RemoteException, InstanceNotFoundException;
	
	/**
   * List name of active users for each given process instances.
   * @param instanceUUIDs
   * @return the mapping between the process instance UUID and the set of active users in the related process instance.
   * @throws InstanceNotFoundException 
   */
  @POST @Path("getActiveUsersOfProcessInstances")
	Map<ProcessInstanceUUID, Set<String>> getActiveUsersOfProcessInstances(
	    @FormParam("instanceuuids") Set<ProcessInstanceUUID> instanceUUIDs,
	    @FormParam("options") final Map<String, String> options)
	throws RemoteException, InstanceNotFoundException;

  /**
   * Returns the event according to its UUID.
   * @param eventUUID the eventUUID
   * @return the event
   * @throws EventNotFoundException if the event is not found
   */
  @POST @Path("getEvent/{eventUUID}")
  CatchingEvent getEvent(
      @PathParam("eventUUID") final CatchingEventUUID eventUUID,
      @FormParam("options") final Map<String, String> options)
  throws RemoteException, EventNotFoundException;

  /**
   * Returns all events which are waiting to be executed.
   * @return a set of catching events or an empty set if no event exists
   */
  @POST @Path("getEvents")
  Set<CatchingEvent> getEvents(
      @FormParam("options") final Map<String, String> options)
  throws RemoteException;

  /**
   * Returns all process instance events which are waiting to be executed.
   * @param instanceUUID the process instance UUID
   * @return a set of catching events or an empty set if no event exists
   */
  @POST @Path("getProcessInstanceEvents/{instanceuuid}")
  Set<CatchingEvent> getEvents(
      @PathParam("instanceuuid") ProcessInstanceUUID instanceUUID,
      @FormParam("options") final Map<String, String> options)
  throws RemoteException;

  /**
   * Returns all activity instance events which are waiting to be executed.
   * @param activityUUID the activity instance UUID
   * @return a set of catching events or an empty set if no event exists
   */
  @POST @Path("getActivityInstanceEvents/{activityuuid}")
  Set<CatchingEvent> getEvents(
      @PathParam("activityuuid") ActivityInstanceUUID activityUUID,
      @FormParam("options") final Map<String, String> options)
  throws RemoteException;

  @POST @Path("getDocumentContent/{documentUUID}")
  byte[] getDocumentContent(
      @PathParam("documentUUID") DocumentUUID documentUUID,
      @FormParam("options") final Map<String, String> options)
  throws RemoteException, DocumentNotFoundException;

  @POST @Path("searchDocuments")
  DocumentResult searchDocuments(
      @FormParam("builder") final DocumentSearchBuilder builder,
      @FormParam("fromResult") final int fromResult,
      @FormParam("maxResults") final int maxResults,
      @FormParam("options") final Map<String, String> options)
  throws RemoteException;

  @POST @Path("getDocument/{documentUUID}")
  Document getDocument(
      @PathParam("documentUUID") final DocumentUUID documentUUID,
      @FormParam("options") final Map<String, String> options)
  throws RemoteException, DocumentNotFoundException;

  @POST @Path("getDocuments")
  List<Document> getDocuments(
      @FormParam("documentUUIDs") final List<DocumentUUID> documentUUIDs,
      @FormParam("options") final Map<String, String> options)
  throws RemoteException, DocumentNotFoundException;

  @POST @Path("getDocumentVersions/{documentUUID}")
  List<Document> getDocumentVersions(
      @PathParam("documentUUID") DocumentUUID documentUUID,
      @FormParam("options") final Map<String, String> options)
  throws RemoteException, DocumentNotFoundException;

  @POST @Path("getInvolvedUsersOfProcessInstance/{instanceUUID}")
  Set<String> getInvolvedUsersOfProcessInstance(
      @PathParam("instanceUUID") ProcessInstanceUUID instanceUUID,
      @FormParam("options") final Map<String, String> options)
  throws RemoteException, InstanceNotFoundException;

  @POST @Path("getChildrenInstanceUUIDsOfProcessInstance/{instanceUUID}")
  Set<ProcessInstanceUUID> getChildrenInstanceUUIDsOfProcessInstance(
      @PathParam("instanceUUID") ProcessInstanceUUID instanceUUID,
      @FormParam("options") final Map<String, String> options)
  throws RemoteException, InstanceNotFoundException;

}
