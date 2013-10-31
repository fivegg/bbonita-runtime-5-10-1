/**
 * Copyright (C) 2009-2010  BonitaSoft S.A.
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

import org.ow2.bonita.facade.def.majorElement.ActivityDefinition.Type;
import org.ow2.bonita.facade.exception.MonitoringException;
import org.ow2.bonita.facade.uuid.ActivityDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;

/**
 * This class provides a brief overview of Bonita Business Activity Monitoring.<br>
 * Note: Using this API can affect engine performances.
 * @author Matthieu Chaffotte, Elias Ricken de Medeiros
 *
 */
@Path("/API/BAMAPI/")
@Produces({"text/*","application/xml" })
@Consumes({"application/x-www-form-urlencoded","text/*", "application/xml" })
public interface RemoteBAMAPI extends Remote {

	/**
	 * Gets the total number of overdue steps (i.e. tasks) of every users.
   * An overdue task is a task which its ended date is exceeded.
	 * @param options the options map (domain, queryList, user)
	 * @return he number of overdue steps.
	 * @throws RemoteException
	 */
	@POST	@Path("getNumberOfOverdueSteps")
  int getNumberOfOverdueSteps(
      @FormParam("options") final Map<String, String> options)
	throws RemoteException;
  
	/**
	 * Gets the total number of steps at risk (i.e. tasks) of every users. A step at risk is
   * a step which its ended date is close to exceed according to the remaining days.
   * @param remainingDays the remaining days before the ended step date is exceeded.
	 * @param options the options map (domain, queryList, user)
	 * @return the number of steps at risk of every users.
	 * @throws RemoteException
	 */
	@POST @Path("getNumberOfStepsAtRisk")
  int getNumberOfStepsAtRisk(
      @QueryParam("remainingDays") int remainingDays,
      @FormParam("options") final Map<String, String> options)
	throws RemoteException;
  
	/**
	 * Gets the number of open steps of the logged user.
	 * @param options the options map (domain, queryList, user)
	 * @return the number of open steps of the logged user
	 * @throws RemoteException
	 */
	@POST @Path("getNumberOfOpenSteps")
  int getNumberOfOpenSteps(
      @FormParam("options") final Map<String, String> options)
	throws RemoteException;

	/**
	 * Gets the number of overdue steps (i.e. tasks) of the logged user.
   * An overdue task is a task which its ended date is exceeded.
	 * @param options the options map (domain, queryList, user)
	 * @return the number of overdue steps of the logged user.
	 * @throws RemoteException
	 */
	@POST @Path("getNumberOfUserOverdueSteps")
  int getNumberOfUserOverdueSteps(
      @FormParam("options") final Map<String, String> options)
	throws RemoteException;
  
	/**
	 * Gets the number of steps at risk (i.e. tasks) of the logged user.
   * A step at risk is a step which its ended date is close to exceed according to the remaining days.
   * @param remainingDays the remaining days before the ended step date is exceeded.
	 * @param options the options map (domain, queryList, user)
	 * @return
	 * @throws RemoteException
	 */
	@POST @Path("getNumberOfUserStepsAtRisk")
  int getNumberOfUserStepsAtRisk(
      @QueryParam("remainingDays") int remainingDays,
      @FormParam("options") final Map<String, String> options)
	throws RemoteException;
  
	/**
	 * Gets the number of open steps of the logged user.
	 * @param options the options map (domain, queryList, user)
	 * @return
	 * @throws RemoteException
	 */
	@POST @Path("getNumberOfUserOpenSteps")
  int getNumberOfUserOpenSteps(
      @FormParam("options") final Map<String, String> options)
	throws RemoteException;

	/**
	 * Gets the list of the number of the finished cases (process instances) per day of every users.
   * @param since the starting date
	 * @param options the options map (domain, queryList, user)
	 * @return
	 * @throws RemoteException
	 */
	@POST	@Path("getNumberOfFinishedCasesPerDay")
  List<Integer> getNumberOfFinishedCasesPerDay(
      @FormParam("since") Date since,
      @FormParam("options") final Map<String, String> options)
  throws RemoteException;

	/**
	 * Gets the list of the number of executing cases (process instances) per day of every users.
   * @param since the starting date
	 * @param options the options map (domain, queryList, user)
	 * @return
	 * @throws RemoteException
	 */
	@POST	@Path("getNumberOfExecutingCasesPerDay")
  List<Integer> getNumberOfExecutingCasesPerDay(
      @FormParam("since") Date since,
      @FormParam("options") final Map<String, String> options)
  throws RemoteException;

	/**
	 * Gets the number of open steps of every users for every days since the given day.
   * @param since the day 
	 * @param options the options map (domain, queryList, user)
	 * @return
	 * @throws RemoteException
	 */
	@POST @Path("getNumberOfOpenStepsPerDay")
  List<Integer> getNumberOfOpenStepsPerDay(
      @FormParam("since") Date since,
      @FormParam("options") final Map<String, String> options)
  throws RemoteException;

	/**
	 * Gets the total number of open steps (i.e. tasks) of every users according to the step priority.
   * @param priority the step priority
	 * @param options the options map (domain, queryList, user)
	 * @return
	 * @throws RemoteException
	 */
	@POST @Path("getNumberOfUserOpenStepsByPriority")
  int getNumberOfUserOpenSteps(
      @QueryParam("priority") int priority,
      @FormParam("options") final Map<String, String> options)
	throws RemoteException;

	/**
	 * Gets the total number of finished steps of the logged user since the given date according
   * to the step priority.
   * @param priority the step priority
	 * @param since
	 * @param options the options map (domain, queryList, user)
	 * @return
	 * @throws RemoteException
	 */
	@POST @Path("getNumberOfUserFinishedSteps")
  int getNumberOfUserFinishedSteps(
      @QueryParam("priority") int priority,
      @FormParam("since") Date since, 
  		@FormParam("options") final Map<String, String> options)
	throws RemoteException;

	/**
	 * Gets the total number of finished steps of every users since the given date
   * according to the step priority.
   * @param priority the step priority
	 * @param since
	 * @param options the options map (domain, queryList, user)
	 * @return
	 * @throws RemoteException
	 */
	@POST @Path("getNumberOfFinishedSteps")
  int getNumberOfFinishedSteps(
      @QueryParam("priority") int priority,
      @FormParam("since") Date since, 
  		@FormParam("options") final Map<String, String> options)
	throws RemoteException;

	/**
	 * Gets the total number of open steps (i.e. tasks) of every users according to the step priority.
   * @param priority the step priority
	 * @param options the options map (domain, queryList, user)
	 * @return
	 * @throws RemoteException
	 */
	@POST	@Path("getNumberOfOpenStepsByPriority")
  int getNumberOfOpenSteps(
      @QueryParam("priority") int priority,
      @FormParam("options") final Map<String, String> options)
	throws RemoteException;
  
  /**
   * Gets the list of the duration of all finished processes instances in the given interval.
   * @param since
   * @param until
   * @param options the options map (domain, queryList, user)
   * @return the list of the duration of all finished processes instances in the given interval
   */
	@POST  @Path("getProcessInstancesDuration")
  List<Long> getProcessInstancesDuration(
      @QueryParam("since") Date since, 
      @QueryParam("until") Date until,
      @FormParam("options") final Map<String, String> options)
  throws RemoteException;
  
  /**
   * Gets the list of the duration of all finished processes instances for the given processUUID in the given interval.
   * @param processUUID
   * @param since
   * @param until
   * @param options the options map (domain, queryList, user)
   * @return the list of the duration of all finished processes instances for the given processUUID in the given interval.
   */
	@POST  @Path("getProcessInstancesDuration/{processUUID}")
  List<Long> getProcessInstancesDuration(
      @PathParam("processUUID") ProcessDefinitionUUID processUUID, 
      @QueryParam("since") Date since, 
      @QueryParam("until") Date until,
      @FormParam("options") final Map<String, String> options)
  throws RemoteException;
  
  /**
   * Gets the list of the duration of all finished processes instances for the given processUUIDs in the given interval.
   * @param processUUIDs
   * @param since
   * @param until
   * @param options the options map (domain, queryList, user)
   * @return the list of the duration of all finished processes instances for the given processUUIDs in the given interval.
   */
	@POST  @Path("getProcessInstancesDurationFromProcessUUID")
  List<Long> getProcessInstancesDuration(
      @FormParam("processUUIDs") Set<ProcessDefinitionUUID> processUUIDs, 
      @QueryParam("since") Date since, 
      @QueryParam("until") Date until,
      @FormParam("options") final Map<String, String> options)
  throws RemoteException;
  
  /**
   * Gets the list of the execution time of all finished activity instances in the given interval.
   * @param since
   * @param until
   * @param options the options map (domain, queryList, user)
   * @return the list of the execution time of all finished activity instances in the given interval.
   */
	@POST  @Path("getActivityInstancesExecutionTime")
  List<Long> getActivityInstancesExecutionTime(
      @QueryParam("since") Date since, 
      @QueryParam("until") Date until,
      @FormParam("options") final Map<String, String> options)
  throws RemoteException;
  
  /**
   * Gets the list of the execution time of all finished activity instances for the given processUUID in the given interval.
   * @param processUUID
   * @param since
   * @param until
   * @param options the options map (domain, queryList, user)
   * @return the list of the execution time of all finished activity instances for the given processUUID in the given interval.
   */
	@POST  @Path("getActivityInstancesExecutionTimeFromProcessUUID/{processUUID}")
  List<Long> getActivityInstancesExecutionTime (
      @PathParam("processUUID") ProcessDefinitionUUID processUUID, 
      @QueryParam("since") Date since, 
      @QueryParam("until") Date until,
      @FormParam("options") final Map<String, String> options)
  throws RemoteException;
  
  /**
   * Gets the list of the execution time of all finished activity instances for the given processUUIDs in the given interval.
   * @param processUUIDs
   * @param since
   * @param until
   * @param options the options map (domain, queryList, user)
   * @return the list of the execution time of all finished activity instances for the given processUUIDs in the given interval.
   */
	@POST  @Path("getActivityInstancesExecutionTimeFromProcessUUIDs")
  List<Long> getActivityInstancesExecutionTimeFromProcessUUIDs(
      @FormParam("processUUIDs") Set<ProcessDefinitionUUID> processUUIDs, 
      @QueryParam("since") Date since, 
      @QueryParam("until") Date until,
      @FormParam("options") final Map<String, String> options)
  throws RemoteException;
  
  /**
   * Gets the list of the execution time of all finished activity instances for the given ActivityDefinitionUUID in the given interval.
   * @param activityUUID
   * @param since
   * @param until
   * @param options the options map (domain, queryList, user)
   * @return the list of the execution time of all finished activity instances for the given ActivityDefinitionUUID in the given interval.
   */
	@POST  @Path("getActivityInstancesExecutionTimeFromActivityUUID/{activityUUID}")
  List<Long> getActivityInstancesExecutionTime(
      @PathParam("activityUUID") ActivityDefinitionUUID activityUUID,  
      @QueryParam("since") Date since, 
      @QueryParam("until") Date until,
      @FormParam("options") final Map<String, String> options)
  throws RemoteException;
  
  /**
   * Gets the list of the execution time of all finished activity instances for the given ActivityDefinitionUUIDs in the given interval.
   * @param activityUUIDs
   * @param since
   * @param until
   * @param options the options map (domain, queryList, user)
   * @return the list of the execution time of all finished activity instances for the given ActivityDefinitionUUIDs in the given interval.
   */
	@POST  @Path("getActivityInstancesExecutionTimeFromActivityUUIDs")
  List<Long> getActivityInstancesExecutionTimeFromActivityUUIDs(
      @FormParam("activityUUIDs") Set<ActivityDefinitionUUID> activityUUIDs, 
      @QueryParam("since") Date since, 
      @QueryParam("until") Date until,
      @FormParam("options") final Map<String, String> options)
  throws RemoteException;
  
  /**
   * Gets the list of the waiting time of all finished tasks instances in the given interval.
   * @param since
   * @param until
   * @param options the options map (domain, queryList, user)
   * @return the list of the waiting time of all finished tasks instances in the given interval.
   */
	@POST  @Path("getTaskInstancesWaitingTime")
  List<Long> getTaskInstancesWaitingTime(
      @QueryParam("since") Date since, 
      @QueryParam("until") Date until,
      @FormParam("options") final Map<String, String> options)
  throws RemoteException;
  
  /**
   * Gets the list of the waiting time of all finished tasks instances for the given processUUID in the given interval.
   * @param processUUID
   * @param since
   * @param until
   * @param options the options map (domain, queryList, user)
   * @return the list of the waiting time of all finished tasks instances for the given processUUID in the given interval.
   */
	@POST  @Path("getTaskInstancesWaitingTimeFromProcessUUID/{processUUID}")
  List<Long> getTaskInstancesWaitingTime(
      @PathParam("processUUID") ProcessDefinitionUUID processUUID, 
      @QueryParam("since") Date since, 
      @QueryParam("until") Date until,
      @FormParam("options") final Map<String, String> options)
  throws RemoteException;

  /**
   * Gets the list of the waiting time of all finished tasks instances for the given processUUIDs in the given interval.
   * @param processUUIDs
   * @param since
   * @param until
   * @param options the options map (domain, queryList, user)
   * @return the list of the waiting time of all finished tasks instances for the given processUUIDs in the given interval.
   */
	@POST  @Path("getTaskInstancesWaitingTimeFromProcessUUIDs")
  List<Long> getTaskInstancesWaitingTimeFromProcessUUIDs(
      @FormParam("processUUIDs") Set<ProcessDefinitionUUID> processUUIDs, 
      @QueryParam("since") Date since, 
      @QueryParam("until") Date until,
      @FormParam("options") final Map<String, String> options)
  throws RemoteException;
  
  /**
   * Gets the list of the waiting time of all finished tasks instances for the given taskUUID in the given interval.
   * @param taskUUID
   * @param since
   * @param until
   * @param options the options map (domain, queryList, user)
   * @return the list of the waiting time of all finished tasks instances for the given taskUUID in the given interval.
   */
	@POST  @Path("getTaskInstancesWaitingTimeFromTaskUUID/{taskUUID}")
  List<Long> getTaskInstancesWaitingTime(
      @PathParam("taskUUID") ActivityDefinitionUUID taskUUID, 
      @QueryParam("since") Date since, 
      @QueryParam("until") Date until,
      @FormParam("options") final Map<String, String> options)
  throws RemoteException;
  
  /**
   * Gets the list of the waiting time of all finished tasks instances for the given taskUUIDs in the given interval.
   * @param tasksUUIDs
   * @param since
   * @param until
   * @param options the options map (domain, queryList, user)
   * @return the list of the waiting time of all finished tasks instances for the given taskUUIDs in the given interval.
   */
	@POST  @Path("getTaskInstancesWaitingTimeFromTaskUUIDs")
  List<Long> getTaskInstancesWaitingTimeFromTaskUUIDs(
      @FormParam("taskUUIDs") Set<ActivityDefinitionUUID> taskUUIDs, 
      @QueryParam("since") Date since, 
      @QueryParam("until") Date until,
      @FormParam("options") final Map<String, String> options)
  throws RemoteException;
	
	/**
   * Gets the list of the waiting time of all finished human tasks for the given user in the given interval.
   * @param username
   * @param since
   * @param until
   * @param options the options map (domain, queryList, user)
   * @return the list of the waiting time of all finished human tasks for the given user in the given interval.
   */
	@POST  @Path("getTaskInstancesWaitingTimeOfUser/{username}")
  List<Long> getTaskInstancesWaitingTimeOfUser(
      @PathParam("username") String username, 
      @QueryParam("since") Date since, 
      @QueryParam("until") Date until,
      @FormParam("options") final Map<String, String> options)
    throws RemoteException;
  
  /**
   * Gets the list of the waiting time of all finished human tasks for the given user, ProcessDefintionUUID and interval.
   * @param username
   * @param processUUID
   * @param since
   * @param until
   * @param options the options map (domain, queryList, user)
   * @return the list of the waiting time of all finished human tasks for the given user, ProcessDefintionUUID and interval.
   */
  @POST  @Path("getTaskInstancesWaitingTimeOfUserFromProcessUUID/{username}/{processUUID}")
  List<Long> getTaskInstancesWaitingTimeOfUser(
      @PathParam("username") String username, 
      @PathParam("processUUID") ProcessDefinitionUUID processUUID, 
      @QueryParam("since") Date since, 
      @QueryParam("until") Date until,
      @FormParam("options") final Map<String, String> options)
    throws RemoteException;
  
  /**
   * Gets the list of the waiting time of all finished human tasks for the given user, ProcessDefintionUUIDs and interval.
   * @param username
   * @param processUUIDs
   * @param since
   * @param until
   * @param options the options map (domain, queryList, user)
   * @return the list of the waiting time of all finished human tasks for the given user, ProcessDefintionUUIDs and interval. 
   */
  @POST  @Path("getTaskInstancesWaitingTimeOfUserFromProcessUUIDs/{username}")
  List<Long> getTaskInstancesWaitingTimeOfUserFromProcessUUIDs(
      @PathParam("username") String username, 
      @FormParam("processUUIDs") Set<ProcessDefinitionUUID> processUUIDs, 
      @QueryParam("since") Date since, 
      @QueryParam("until") Date until,
      @FormParam("options") final Map<String, String> options)
    throws RemoteException;
  
  /**
   * Gets the list of the waiting time of all finished human tasks for the given user, taskUUID and interval.
   * @param username
   * @param taskUUID
   * @param since
   * @param until
   * @param options the options map (domain, queryList, user)
   * @return the list of the waiting time of all finished human tasks for the given user, taskUUID and interval.
   */
  @POST  @Path("getTaskInstancesWaitingTimeOfUserFromTaskUUID/{username}/{taskUUID}")
  List<Long> getTaskInstancesWaitingTimeOfUser(
      @PathParam("username") String username, 
      @PathParam("taskUUID") ActivityDefinitionUUID taskUUID, 
      @QueryParam("since") Date since, 
      @QueryParam("until") Date until,
      @FormParam("options") final Map<String, String> options)
    throws RemoteException;
  
  /**
   * Gets the list of the waiting time of all finished human tasks for the given user, taskUUIDs and interval.
   * @param username
   * @param tasksUUIDs
   * @param since
   * @param until
   * @param options the options map (domain, queryList, user)
   * @return the list of the waiting time of all finished human tasks for the given user, taskUUIDs and interval.
   */
  @POST  @Path("getTaskInstancesWaitingTimeOfUserFromTaskUUIDs/{username}")
  List<Long> getTaskInstancesWaitingTimeOfUserFromTaskUUIDs(
      @PathParam("username") String username, 
      @FormParam("taskUUIDs") Set<ActivityDefinitionUUID> taskUUIDs, 
      @QueryParam("since") Date since, 
      @QueryParam("until") Date until,
      @FormParam("options") final Map<String, String> options)
    throws RemoteException;
  
  /**
   * Gets the list of the duration of all finished activity instances for the given interval.
   * @param since
   * @param until
   * @param options the options map (domain, queryList, user)
   * @return the list of the duration of all finished activity instances for the given interval.
   */
  @POST  @Path("getActivityInstancesDuration")
  List<Long> getActivityInstancesDuration(
      @QueryParam("since") Date since, 
      @QueryParam("until") Date until,
      @FormParam("options") final Map<String, String> options)
    throws RemoteException;
  
  /**
   * Gets the list of the duration of all finished activity instances for the given ProcessDefintionUUID and interval.
   * @param processUUID
   * @param since
   * @param until
   * @param options the options map (domain, queryList, user)
   * @return
   */
  @POST  @Path("getActivityInstancesDurationFromProcessUUID/{processUUID}")
  List<Long> getActivityInstancesDuration(
      @PathParam("processUUID") ProcessDefinitionUUID processUUID, 
      @QueryParam("since") Date since, 
      @QueryParam("until") Date until,
      @FormParam("options") final Map<String, String> options)
    throws RemoteException;
  
  /**
   * Gets the list of the duration of all finished activity instances for the given ProcessDefintionUUIDs and interval.
   * @param processUUIDs
   * @param since
   * @param until
   * @param options the options map (domain, queryList, user)
   * @return the list of the duration of all finished activity instances for the given ProcessDefintionUUIDs and interval.
   */
  @POST  @Path("getActivityInstancesDurationFromProcessUUIDs")
  List<Long> getActivityInstancesDurationFromProcessUUIDs(
      @FormParam("processUUIDs") Set<ProcessDefinitionUUID> processUUIDs, 
      @QueryParam("since") Date since, 
      @QueryParam("until") Date until,
      @FormParam("options") final Map<String, String> options)
      throws RemoteException;
  
  /**
   * Gets the list of the duration of all finished activity instances for the given ActivityDefintionUUID and interval.
   * @param activityUUID
   * @param since
   * @param until
   * @param options the options map (domain, queryList, user)
   * @return the list of the duration of all finished activity instances for the given ActivityDefintionUUID and interval.
   */
  @POST  @Path("getActivityInstancesDurationFromActivityUUID/{activityUUID}")
  List<Long> getActivityInstancesDuration(
      @PathParam("activityUUID") ActivityDefinitionUUID activityUUID, 
      @QueryParam("since") Date since, 
      @QueryParam("until") Date until,
      @FormParam("options") final Map<String, String> options)
    throws RemoteException;
  
  /**
   * Gets the list of the duration of all finished activity instances for the given ActivityDefintionUUIDs and interval.
   * @param activityUUIDs
   * @param since
   * @param until
   * @param options the options map (domain, queryList, user)
   * @return the list of the duration of all finished activity instances for the given ActivityDefintionUUIDs and interval.
   */
  @POST  @Path("getActivityInstancesDurationFromActivityUUIDs")
  List<Long> getActivityInstancesDurationFromActivityUUIDs(
      @FormParam("activityUUIDs") Set<ActivityDefinitionUUID> activityUUIDs, 
      @QueryParam("since") Date since, 
      @QueryParam("until") Date until,
      @FormParam("options") final Map<String, String> options)
    throws RemoteException;
  
  /**
   * Gets the list of the duration of all finished activity instances for the given type and interval.
   * @param activityType
   * @param since
   * @param until
   * @param options the options map (domain, queryList, user)
   * @return the list of the duration of all finished activity instances for the given type and interval.
   */
  @POST  @Path("getActivityInstancesDurationByActivityType/{activityType}")
  List<Long> getActivityInstancesDurationByActivityType(
      @PathParam("activityType") Type activityType, 
      @QueryParam("since") Date since, 
      @QueryParam("until") Date until,
      @FormParam("options") final Map<String, String> options)
    throws RemoteException;
  
  /**
   * Gets the list of the duration of all finished activity instances for the given type, ProcessDefinitionUUID and interval.
   * @param activityType
   * @param processUUID
   * @param since
   * @param until
   * @param options the options map (domain, queryList, user)
   * @return the list of the duration of all finished activity instances for the given type, ProcessDefinitionUUID and interval.
   */
  @POST  @Path("getActivityInstancesDurationByActivityType/{activityType}/{processUUID}")
  List<Long> getActivityInstancesDurationByActivityType(
      @PathParam("activityType") Type activityType, 
      @PathParam("processUUID") ProcessDefinitionUUID processUUID, 
      @QueryParam("since") Date since, 
      @QueryParam("until") Date until,
      @FormParam("options") final Map<String, String> options)
    throws RemoteException;
  
  /**
   * Gets the list of the duration of all finished activity instances for the given type, ProcessDefinitionUUIDs and interval.
   * @param activityType
   * @param processUUIDs
   * @param since
   * @param until
   * @return the list of the duration of all finished activity instances for the given type, ProcessDefinitionUUIDs and interval.
   */
  @POST  @Path("getActivityInstancesDurationByActivityTypeFromProcessUUIDs/{activityType}")
  List<Long> getActivityInstancesDurationByActivityTypeFromProcessUUIDs(
      @PathParam("activityType") Type activityType, 
      @FormParam("processUUIDs") Set<ProcessDefinitionUUID> processUUIDs, 
      @QueryParam("since") Date since, 
      @QueryParam("until") Date until,
      @FormParam("options") final Map<String, String> options)
      throws RemoteException;
  
  /**
   * Gets the number of created Process Instances in the given interval
   * @param since
   * @param until
   * @param options the options map (domain, queryList, user)
   * @return the number of created Process Instances in the given interval
   */
  @POST  @Path("getNumberOfCreatedProcessInstances")
  long getNumberOfCreatedProcessInstances(
      @QueryParam("since") Date since, 
      @QueryParam("until") Date until,
      @FormParam("options") final Map<String, String> options)
  throws RemoteException;
  
  /**
   * Gets the number of created Process Instances for the the given ProcessDefinitionUUID and interval
   * @param processUUID
   * @param since
   * @param until
   * @param options the options map (domain, queryList, user)
   * @return the number of created Process Instances for the the given ProcessDefinitionUUID and interval
   */
  @POST  @Path("getNumberOfCreatedProcessInstances/{processUUID}")
  long getNumberOfCreatedProcessInstances(
      @PathParam("processUUID") ProcessDefinitionUUID processUUID, 
      @QueryParam("since") Date since, 
      @QueryParam("until") Date until,
      @FormParam("options") final Map<String, String> options)
  throws RemoteException;
  
  /**
   * Gets the number of created Activity Instances in the given interval
   * @param since
   * @param until
   * @param options the options map (domain, queryList, user)
   * @return the number of created Activity Instances in the given interval
   */
  @POST  @Path("getNumberOfCreatedActivityInstances")
  long getNumberOfCreatedActivityInstances(
      @QueryParam("since") Date since, 
      @QueryParam("until") Date until,
      @FormParam("options") final Map<String, String> options)
  throws RemoteException;
  
  /**
   * Gets the number of created Activity Instances for the given ProcessDefinitionUUID and interval
   * @param processUUID ProcessDefinitionUUID
   * @param since
   * @param until
   * @param options the options map (domain, queryList, user)
   * @return the number of created Activity Instances for the given ProcessDefinitionUUID and interval
   */
  @POST  @Path("getNumberOfCreatedActivityInstancesFromProcessUUID/{processUUID}")
  long getNumberOfCreatedActivityInstances(
      @PathParam("processUUID") ProcessDefinitionUUID processUUID, 
      @QueryParam("since") Date since, 
      @QueryParam("until") Date until,
      @FormParam("options") final Map<String, String> options)
  throws RemoteException;
  
  /**
   * Gets the number of created Activity Instances for the given ProcessDefinitionUUIDs in the given interval
   * @param processUUIDs set of ProcessDefinitionUUID
   * @param since
   * @param until
   * @param options the options map (domain, queryList, user)
   * @return the number of created Activity Instances for the given ProcessDefinitionUUIDs  in the given interval
   */
  @POST  @Path("getNumberOfCreatedActivityInstancesFromProcessUUIDs")
  long getNumberOfCreatedActivityInstancesFromProcessUUIDs(
      @FormParam("processUUIDs") Set<ProcessDefinitionUUID> processUUIDs, 
      @QueryParam("since") Date since, 
      @QueryParam("until") Date until,
      @FormParam("options") final Map<String, String> options)
  throws RemoteException;
  
  /**
   * Gets the number of created Activity Instances for the given ActivityDefinitionUUID in the given interval
   * @param activityUUID the ActivityDefinitionUUID
   * @param since
   * @param until
   * @param options the options map (domain, queryList, user)
   * @return the number of created Activity Instances for the given ActivityDefinitionUUID in the given interval
   */
  @POST  @Path("getNumberOfCreatedActivityInstancesFromActivityUUID/{activityUUID}")
  long getNumberOfCreatedActivityInstances(
      @PathParam("activityUUID") ActivityDefinitionUUID activityUUID, 
      @QueryParam("since") Date since, 
      @QueryParam("until") Date until,
      @FormParam("options") final Map<String, String> options)
  throws RemoteException;
  
  /**
   * Gets the number of created Activity Instances for the given ActivityDefinitionUUIDs in the given interval
   * @param activityUUIDs ActivityDefinitionUUIDs
   * @param since
   * @param until
   * @param options the options map (domain, queryList, user)
   * @return the number of created Activity Instances for the given ActivityDefinitionUUIDs in the given interval
   */
  @POST  @Path("getNumberOfCreatedActivityInstancesFromActivityUUIDs")
  long getNumberOfCreatedActivityInstancesFromActivityUUIDs(
      @FormParam("activityUUIDs") Set<ActivityDefinitionUUID> activityUUIDs, 
      @QueryParam("since") Date since, 
      @QueryParam("until") Date until,
      @FormParam("options") final Map<String, String> options)
  throws RemoteException;
  
  /**
   * Gets the number of created Activity Instances for the given type in the given interval
   * @param activityType the ActivityType
   * @param since
   * @param until
   * @param options the options map (domain, queryList, user)
   * @return the number of created Activity Instances for the given type in the given interval
   */
  @POST  @Path("getNumberOfCreatedActivityInstancesByActivityType/{activityType}")
  long getNumberOfCreatedActivityInstancesByActivityType(
      @PathParam("activityType") Type activityType, 
      @QueryParam("since") Date since, 
      @QueryParam("until") Date until,
      @FormParam("options") final Map<String, String> options)
  throws RemoteException;
  
  /**
   * Gets the number of created Activity Instances for the given type and ProcessDefinitionUUID in the given interval
   * @param activityType the activity type
   * @param processUUID the ProcesssDefinitionUUID
   * @param since
   * @param until
   * @param options the options map (domain, queryList, user)
   * @return the number of created Activity Instances for the given type and ProcessDefinitionUUID in the given interval
   */
  @POST  @Path("getNumberOfCreatedActivityInstancesByActivityTypeFromProcessUUID/{activityType}/{processUUID}")
  long getNumberOfCreatedActivityInstancesByActivityType(
      @PathParam("activityType") Type activityType, 
      @PathParam("processUUID") ProcessDefinitionUUID processUUID, 
      @QueryParam("since") Date since, 
      @QueryParam("until") Date until,
      @FormParam("options") final Map<String, String> options)
  throws RemoteException;
  
  /**
   * the number of created Activity Instances for the given type and ProcessDefinitionUUIDs in the given interval
   * @param activityType the activity type
   * @param processUUIDs the set of ProcesssDefinitionUUID
   * @param since
   * @param until
   * @param options the options map (domain, queryList, user)
   * @return the number of created Activity Instances for the given type and ProcessDefinitionUUIDs in the given interval
   */
  @POST  @Path("getNumberOfCreatedActivityInstancesByActivityTypeFromProcessUUID/{activityType}")
  long getNumberOfCreatedActivityInstancesByActivityTypeFromProcessUUIDs(
      @PathParam("activityType") Type activityType, 
      @FormParam("processUUIDs") Set<ProcessDefinitionUUID> processUUIDs, 
      @QueryParam("since") Date since, 
      @QueryParam("until") Date until,
      @FormParam("options") final Map<String, String> options)
  throws RemoteException;
  
  @POST @Path("getSystemLoadAverage")
  double getSystemLoadAverage(
          @FormParam("options") final Map<String, String> options) 
  throws RemoteException, MonitoringException;    
  
  @POST @Path("getCurrentMemoryUsage")
  long getCurrentMemoryUsage(
          @FormParam("options") final Map<String, String> options) 
  throws RemoteException, MonitoringException;  
  
  @POST @Path("getMemoryUsagePercentage")
  float getMemoryUsagePercentage(
          @FormParam("options") final Map<String, String> options) 
  throws RemoteException, MonitoringException;    
  
  @POST @Path("getUpTime")
  long getUpTime(
          @FormParam("options") final Map<String, String> options) 
  throws RemoteException, MonitoringException;  
  
  @POST @Path("getStartTime")
  long getStartTime(
          @FormParam("options") final Map<String, String> options) 
  throws RemoteException, MonitoringException;  
  
  @POST @Path("getTotalThreadsCpuTime")
  long getTotalThreadsCpuTime(
          @FormParam("options") final Map<String, String> options) 
  throws RemoteException, MonitoringException;  
  
  @POST @Path("getThreadCount")
  int getThreadCount(
          @FormParam("options") final Map<String, String> options) 
  throws RemoteException, MonitoringException;    
  
  @POST @Path("getAvailableProcessors")
  int getAvailableProcessors(
          @FormParam("options") final Map<String, String> options) 
  throws RemoteException, MonitoringException;
  
  @POST @Path("getOSArch")
  String getOSArch(
          @FormParam("options") final Map<String, String> options) 
  throws RemoteException, MonitoringException;    
  
  @POST @Path("getOSName")
  String getOSName(
          @FormParam("options") final Map<String, String> options) 
  throws RemoteException, MonitoringException;    
  
  @POST @Path("getOSVersion")
  String getOSVersion(
          @FormParam("options") final Map<String, String> options) 
  throws RemoteException, MonitoringException;    
  
  @POST @Path("getJvmName")
  String getJvmName(
          @FormParam("options") final Map<String, String> options) 
  throws RemoteException, MonitoringException;    
  
  @POST @Path("getJvmVendor")
  String getJvmVendor(
          @FormParam("options") final Map<String, String> options) 
  throws RemoteException, MonitoringException;    
  
  @POST @Path("getJvmVersion")
  String getJvmVersion(
          @FormParam("options") final Map<String, String> options) 
  throws RemoteException, MonitoringException;
  
  @POST @Path("getJvmSystemProperties")
  Map<String, String> getJvmSystemProperties(
          @FormParam("options") final Map<String, String> options) 
  throws RemoteException, MonitoringException;  
}
