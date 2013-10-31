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
 * Modified by Charles Souillard, Matthieu Chaffotte, Elias Ricken de Medeiros - BonitaSoft S.A.
 **/
package org.ow2.bonita.facade;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ow2.bonita.facade.exception.ActivityNotFoundException;
import org.ow2.bonita.facade.exception.BonitaInternalException;
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
import org.ow2.bonita.facade.runtime.InstanceState;
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
 * 
 * @author Pierre Vigneras, Charles Souillard
 */
public interface QueryRuntimeAPI {

  /**
   * Returns records for all iterations and multi-instantiations that should append for the given process instance UUID
   * and activity Id.<br>
   * An empty set is returned if no instance is found.
   * 
   * @param instanceUUID
   *          the instance UUID.
   * @param activityName
   *          the activity name.
   * @return the set containing light activity records.
   * @throws InstanceNotFoundException
   *           if no instance has been found with the given instance UUID.
   * @throws BonitaInternalException
   *           if an exception occurs.
   */
  Set<LightActivityInstance> getLightActivityInstances(ProcessInstanceUUID instanceUUID, String activityName)
      throws InstanceNotFoundException, ActivityNotFoundException;

  /**
   * Returns records for all multi-instantiations that should append for the given process instance UUID, iteration ID
   * and activity name.<br>
   * An empty set is returned if no instance is found.
   * 
   * @param instanceUUID
   *          the instance UUID.
   * @param activityName
   *          the activity name.
   * @param iterationId
   *          the iteration ID.
   * @return
   */
  Set<LightActivityInstance> getLightActivityInstances(ProcessInstanceUUID instanceUUID, String activityName,
      String iterationId);

  /**
   * Gets all process instances. An empty set is returned if no instance is found.
   * 
   * @return a set containing all process instances.
   * @throws BonitaInternalException
   *           if an exception occurs.
   */
  Set<ProcessInstance> getProcessInstances();

  /**
   * Gets all light process instances. An empty set is returned if no instance is found.
   * 
   * @return a set containing all light process instances.
   * @throws BonitaInternalException
   *           if an exception occurs.
   */
  Set<LightProcessInstance> getLightProcessInstances();

  /**
   * Gets a set of light process instances form their UUIDs.
   * 
   * @param instanceUUIDs
   *          the collection of instance UUIDs
   * @return a set of light process instances form their UUIDs.
   */
  Set<LightProcessInstance> getLightProcessInstances(Collection<ProcessInstanceUUID> instanceUUIDs);

  /**
   * Gets a list of light process instances from an index to the page size. This list is a sub-set of
   * getLightProcessInstances.
   * 
   * @param fromIndex
   *          the index
   * @param pageSize
   *          the page size
   * @return a list of light process instances
   */
  List<LightProcessInstance> getLightProcessInstances(int fromIndex, int pageSize);

  /**
   * Gets a set of light process instances from an index to the page size ordered by the paging criterion. This list is
   * a sub-set of getLightProcessInstances.
   * 
   * @param fromIndex
   *          the index
   * @param pageSize
   *          the page size
   * @param pagingCriterion
   *          the attribute used to oder the list
   * @return a list of light process instances ordered by pagingCriterion
   */
  List<LightProcessInstance> getLightProcessInstances(int fromIndex, int pageSize,
      ProcessInstanceCriterion pagingCriterion);

  /**
   * Gets a list of light process instances from an index to the page size according to the collection of process
   * instance UUIDs. This list is a sub-set of getLightProcessInstances.
   * 
   * @param instanceUUIDs
   *          the instances UUIDs
   * @param fromIndex
   *          the index
   * @param pageSize
   *          the page size
   * @return a list of light process instances
   */
  List<LightProcessInstance> getLightProcessInstances(Collection<ProcessInstanceUUID> instanceUUIDs, int fromIndex,
      int pageSize);

  /**
   * Gets a list of light process instances from an index to the page size according to the set of process instance
   * UUIDs. This list is a sub-set of getLightProcessInstances.
   * 
   * @param instanceUUIDs
   *          the instances UUIDs
   * @param fromIndex
   *          the index
   * @param pageSize
   *          the page size
   * @param pagingCriterion
   *          the criterion used do sort the returned list
   * @return a list of light process instances sorted by pagingCriterion
   */
  List<LightProcessInstance> getLightProcessInstances(Set<ProcessInstanceUUID> instanceUUIDs, int fromIndex,
      int pageSize, ProcessInstanceCriterion pagingCriterion);

  /**
   * Gets a set of light process instances from an index to the page size. This set is a sub-set of
   * getLightProcessInstances and takes only process instances which contain sub-process(es).
   * 
   * @param fromIndex
   *          the index
   * @param pageSize
   *          the page size
   * @return a set of light process instances
   */
  List<LightProcessInstance> getLightParentProcessInstances(int fromIndex, int pageSize);

  /**
   * Gets a list of light process instances from an index to the page size. This list is a sub-set of
   * getLightProcessInstances and takes only process instances which contain sub-process(es).
   * 
   * @param fromIndex
   *          the index
   * @param pageSize
   *          the page size
   * @param pagingCriterion
   *          the attribute to be used to sort the result
   * @return a list of light process instances sorted by pagingCriterion
   */
  List<LightProcessInstance> getLightParentProcessInstances(int fromIndex, int pageSize,
      ProcessInstanceCriterion pagingCriterion);

  /**
   * Gets the LightProcessInstances that are not sub-process instances and having the ProcessDefinitionUUID in the given
   * ProcessDefinitionUUIDs sub-set.
   * 
   * @param fromIndex
   * @param pageSize
   * @param processUUIDs
   *          ProcessDefinitionUUIDs to be taken into consideration
   * @param pagingCriterion
   *          the criterion used to sort the result list
   * @return the LightProcessInstances that are not sub-process instances and having the ProcessDefinitionUUID in the
   *         given ProcessDefinitionUUIDs sub-set.
   */
  List<LightProcessInstance> getLightParentProcessInstances(Set<ProcessDefinitionUUID> processUUIDs, int fromIndex,
      int pageSize, ProcessInstanceCriterion pagingCriterion);

  /**
   * Gets the LightProcessInstances that are not sub-process instances and not having the ProcessDefinitionUUID in the
   * given ProcessDefinitionUUIDs sub-set.
   * 
   * @param fromIndex
   * @param pageSize
   * @param exceptions
   *          ProcessDefinitionUUIDs to be ignored
   * @param pagingCriterion
   *          the criterion used to sort the result list
   * @return the LightProcessInstances that are not sub-process instances and not having the ProcessDefinitionUUID in
   *         the given ProcessDefinitionUUIDs sub-set.
   */
  List<LightProcessInstance> getLightParentProcessInstancesExcept(Set<ProcessDefinitionUUID> exceptions, int fromIndex,
      int pageSize, ProcessInstanceCriterion pagingCriterion);

  /**
   * Gets a list of light task instances which belong to a process instance according to its UUID.
   * 
   * @param rootInstanceUUID
   *          the process instance UUID
   * @return a list of light task instances.
   */
  List<LightTaskInstance> getLightTaskInstancesFromRoot(ProcessInstanceUUID rootInstanceUUID);

  /**
   * Gets for each process instance UUID the list of light task instances which belong to a process instance according
   * to its UUID.
   * 
   * @param rootInstanceUUIDs
   *          the process instance UUIDs
   * @return a list of light task instances.
   */
  Map<ProcessInstanceUUID, List<LightTaskInstance>> getLightTaskInstancesFromRoot(
      Set<ProcessInstanceUUID> rootInstanceUUIDs);

  /**
   * Gets a list of light activity instances which belong to a process instance according to its UUID.
   * 
   * @param rootInstanceUUID
   *          the process instance UUID
   * @return a list of light activity instances.
   */
  List<LightActivityInstance> getLightActivityInstancesFromRoot(ProcessInstanceUUID rootInstanceUUID);

  /**
   * Gets for each process instance UUID, the list of light activity instances which belong to a process instance
   * according to its UUID.
   * 
   * @param rootInstanceUUIDs
   *          the process instance UUIDs
   * @return a list of light activity instances.
   */
  Map<ProcessInstanceUUID, List<LightActivityInstance>> getLightActivityInstancesFromRoot(
      Set<ProcessInstanceUUID> rootInstanceUUIDs);

  /**
   * Gets for each process instance UUID, the list of light activity instances which belong to a process instance
   * according to its UUID having the given state.
   * 
   * @param rootInstanceUUIDs
   *          the process instance UUIDs
   * @return a list of light activity instances.
   */
  Map<ProcessInstanceUUID, List<LightActivityInstance>> getLightActivityInstancesFromRoot(
      Set<ProcessInstanceUUID> rootInstanceUUIDs, ActivityState state);

  /**
   * Gets for each process instance UUID, the light activity instance that have been updated after all others
   * 
   * @param rootInstanceUUIDs
   *          the process instance UUIDs
   * @return a light activity instance.
   */
  Map<ProcessInstanceUUID, LightActivityInstance> getLightLastUpdatedActivityInstanceFromRoot(
      Set<ProcessInstanceUUID> rootInstanceUUIDs, boolean considerSystemTaks);

  /**
   * Counts the number of process instances.
   * 
   * @return the number of process instances.
   */
  int getNumberOfProcessInstances();

  /**
   * Counts the number of process instances which are not a sub instance.
   * 
   * @return the number of parent process instances
   */
  int getNumberOfParentProcessInstances();

  /**
   * Counts the number of process instances which are not a sub instance and are instances of the given process
   * definitions.
   * 
   * @param processDefinitionUUIDs
   *          ProcessDefinitionUUIDs
   * @return the number of parent process instances
   */
  int getNumberOfParentProcessInstances(Set<ProcessDefinitionUUID> processDefinitionUUIDs);

  /**
   * Counts the number of process instances which are not a sub instance and are not an instance of the given process
   * definitions.
   * 
   * @param exceptions
   *          ProcessDefinitionUUIDs to be ignored
   * @return the number of parent process instances
   */
  int getNumberOfParentProcessInstancesExcept(Set<ProcessDefinitionUUID> exceptions);

  /**
   * Returns all records of instances matching with the given ProcessInstanceUUID.<br>
   * If one of the ProcessInstanceUUID is not found, nothing is added to the result. <br>
   * An empty set is returned if no instance is found.
   * 
   * @param instanceUUIDs
   *          the instance UUIDs.
   * @return all records of instances matching with the given ProcessInstanceUUID.
   * @throws BonitaInternalException
   *           if an exception occurs.
   */
  Set<ProcessInstance> getProcessInstances(Collection<ProcessInstanceUUID> instanceUUIDs);

  /**
   * Returns all records of instances in one of the given states.
   * 
   * @param instanceStates
   *          a Collection of the required instance states
   * @return all records of instances in one task in one of the given states.
   */
  Set<ProcessInstance> getProcessInstancesWithInstanceStates(Collection<InstanceState> instanceStates);

  /**
   * Returns all records of instances having one task in one of the given states.<br>
   * 
   * @param activityStates
   *          states
   * @return all records of instances having one task in one of the given states.
   * @throws BonitaInternalException
   *           if an exception occurs.
   */
  Set<ProcessInstance> getProcessInstancesWithTaskState(Collection<ActivityState> activityStates);

  /**
   * Returns the record of the instance with the given UUID.
   * 
   * @param instanceUUID
   *          the instance UUID.
   * @return the record of the instance with the given UUID.
   * @throws InstanceNotFoundException
   *           if no instance has been found with the given instance UUID.
   * @throws BonitaInternalException
   *           if an exception occurs.
   */
  ProcessInstance getProcessInstance(ProcessInstanceUUID instanceUUID) throws InstanceNotFoundException;

  /**
   * Returns the light process instance with the given process instance UUID.
   * 
   * @param instanceUUID
   *          the instance UUID.
   * @return the record of the instance with the given UUID.
   * @throws InstanceNotFoundException
   *           if no instance has been found with the given instance UUID.
   */
  LightProcessInstance getLightProcessInstance(ProcessInstanceUUID instanceUUID) throws InstanceNotFoundException;

  /**
   * Returns all records of instance for the given process processDefinitionUUID.<br>
   * An empty set is returned if no instance is found.
   * 
   * @param processUUID
   *          the process definition UUID.
   * @return a set containing all instance records.
   * @throws ProcessNotFoundException
   *           if no process has been found with the given process UUID.
   * @throws BonitaInternalException
   *           if an exception occurs.
   */
  Set<ProcessInstance> getProcessInstances(ProcessDefinitionUUID processUUID) throws ProcessNotFoundException;

  /**
   * Returns the record of the activity with the given activity UUID.
   * 
   * @param activityUUID
   *          the activity UUID.
   * @return the activity record with the given instance UUID and activity id.
   * @throws InstanceNotFoundException
   *           if no instance has been found with the given instance UUID.
   * @throws ActivityNotFoundException
   *           if no activity has been found with the given activity id.
   * @throws BonitaInternalException
   *           if an exception occurs.
   */
  ActivityInstance getActivityInstance(ActivityInstanceUUID activityUUID) throws ActivityNotFoundException;

  /**
   * Returns all records of activity for the given process instance UUID.
   * 
   * @param instanceUUID
   *          the instance UUID.
   * @return the unordered set containing activity records.
   * @throws InstanceNotFoundException
   *           if no instance has been found with the given instance UUID.
   * @throws BonitaInternalException
   *           if an exception occurs.
   */
  Set<ActivityInstance> getActivityInstances(ProcessInstanceUUID instanceUUID) throws InstanceNotFoundException;

  /**
   * Returns the record of the activity with the given activity instance UUID.
   * 
   * @param activityInstanceUUID
   *          the activity instance UUID.
   * @return the record of the task with the given activity instance UUID.
   * @throws ActivityNotFoundException
   *           if no task has been found with the given activity instance UUID.
   */
  LightActivityInstance getLightActivityInstance(ActivityInstanceUUID activityInstanceUUID)
      throws ActivityNotFoundException;

  /**
   * Returns all records of activity for the given process instance UUID.
   * 
   * @param instanceUUID
   *          the instance UUID.
   * @return the unordered set containing activity records.
   * @throws InstanceNotFoundException
   *           if no instance has been found with the given instance UUID.
   */
  Set<LightActivityInstance> getLightActivityInstances(ProcessInstanceUUID instanceUUID)
      throws InstanceNotFoundException;

  /**
   * Returns all records of activity for the given process instance UUID.
   * 
   * @param instanceUUID
   *          the instance UUID.
   * @param fromIdex
   *          the start index.
   * @param pageSize
   *          the max number of instances.
   * @param pagingCriterion
   *          the attribute used to order the list.
   * @return A list containing activity records order pagingCriterionterion.
   * @throws InstanceNotFoundException
   *           if no instance has been found with the given instance UUID.
   */
  List<LightActivityInstance> getLightActivityInstances(ProcessInstanceUUID instanceUUID, int fromIdex, int pageSize,
      ActivityInstanceCriterion pagingCriterion) throws InstanceNotFoundException;

  // all iterations
  /**
   * Returns records for all iterations and multi-instantiations that should append for the given process instance UUID
   * and activity Id.<br>
   * An empty set is returned if no instance is found.
   * 
   * @param instanceUUID
   *          the instance UUID.
   * @param activityName
   *          the activity name.
   * @return the unordered set containing activity records.
   * @throws InstanceNotFoundException
   *           if no instance has been found with the given instance UUID.
   * @throws BonitaInternalException
   *           if an exception occurs.
   */
  Set<ActivityInstance> getActivityInstances(ProcessInstanceUUID instanceUUID, String activityName)
      throws InstanceNotFoundException, ActivityNotFoundException;

  /**
   * Returns the record of the task with the given task UUID.
   * 
   * @param taskUUID
   *          the task UUID.
   * @return the record of the task with the given task UUID.
   * @throws TaskNotFoundException
   *           if no task has been found with the given task UUID.
   * @throws BonitaInternalException
   *           if an exception occurs.
   */
  TaskInstance getTask(ActivityInstanceUUID taskUUID) throws TaskNotFoundException;

  /**
   * Returns the candidates of the task with the given task UUID.
   * 
   * @param taskUUID
   *          the task UUID.
   * @return the candidates of the task with the given task UUID.
   * @throws TaskNotFoundException
   *           if no task has been found with the given task UUID.
   * @throws BonitaInternalException
   *           if an exception occurs.
   */
  Set<String> getTaskCandidates(final ActivityInstanceUUID taskUUID) throws TaskNotFoundException;

  /**
   * Returns the candidates of the tasks with the given task UUID.
   * 
   * @param taskUUID
   *          the task UUID.
   * @return the candidates of the tasks with the given task UUID.
   * @throws TaskNotFoundException
   *           if no task has been found with the given task UUID.
   * @throws BonitaInternalException
   *           if an exception occurs.
   */
  Map<ActivityInstanceUUID, Set<String>> getTaskCandidates(final Set<ActivityInstanceUUID> taskUUIDs)
      throws TaskNotFoundException;

  /**
   * Returns the record of the task with the given task UUID.
   * 
   * @param taskUUID
   *          the task UUID.
   * @return the record of the task with the given task UUID.
   * @throws TaskNotFoundException
   *           if no task has been found with the given task UUID.
   */
  LightTaskInstance getLightTaskInstance(ActivityInstanceUUID taskUUID) throws TaskNotFoundException;

  /**
   * Returns all records of task for the given process instance UUID.
   * 
   * @param instanceUUID
   *          the instance UUID.
   * @return a set containing all task records for the instance.
   * @throws InstanceNotFoundException
   *           if no instance has been found with the given instance UUID.
   * @throws BonitaInternalException
   *           if an exception occurs.
   */
  Set<TaskInstance> getTasks(ProcessInstanceUUID instanceUUID) throws InstanceNotFoundException;

  /**
   * See getTasks(ProcessInstanceUUID instanceUUID)
   */
  Set<LightTaskInstance> getLightTasks(ProcessInstanceUUID instanceUUID) throws InstanceNotFoundException;

  /**
   * Returns a set of tasks which are a task name from the set of task and the given process instance UUID and the task
   * names
   * 
   * @param instanceUUID
   *          the instance UUID.
   * @param taskNames
   *          the set of task name
   * @return a set of tasks.
   */
  Set<LightTaskInstance> getLightTasks(ProcessInstanceUUID instanceUUID, Set<String> taskNames);

  /**
   * Obtains the user tasks with state either READY or EXECUTING or SUSPENDED or FINISHED for the given instance and the
   * authenticated user.<br>
   * 
   * @param instanceUUID
   *          the instance UUID.
   * @param taskState
   *          the {@link org.ow2.bonita.facade.runtime.ActivityState state} of the task.
   * @return a collection of task records. If no tasks are found, an empty collection is returned.
   * @throws InstanceNotFoundException
   *           if no instance record is found with the given instance UUID.
   * @throws BonitaInternalException
   *           if an other exception occurs.
   */
  Collection<TaskInstance> getTaskList(ProcessInstanceUUID instanceUUID, ActivityState taskState)
      throws InstanceNotFoundException;

  /**
   * See getTaskList(ProcessInstanceUUID instanceUUID, ActivityState taskState)
   */
  Collection<LightTaskInstance> getLightTaskList(ProcessInstanceUUID instanceUUID, ActivityState taskState)
      throws InstanceNotFoundException;

  /**
   * Obtains the user tasks depending on the given activity states for the given instance and the authenticated user.<br>
   * 
   * @param instanceUUID
   *          the instance UUID.
   * @param taskStates
   *          the {@link org.ow2.bonita.facade.runtime.ActivityState states} of the task.
   * @return a collection of task records. If no tasks are found, an empty collection is returned.
   * @throws InstanceNotFoundException
   *           if no instance record is found with the given instance UUID.
   */
  Collection<TaskInstance> getTaskList(ProcessInstanceUUID instanceUUID, Collection<ActivityState> taskStates)
      throws InstanceNotFoundException;

  /**
   * See getTaskList(ProcessInstanceUUID instanceUUID, Collection taskStates)
   */
  Collection<LightTaskInstance> getLightTaskList(ProcessInstanceUUID instanceUUID, Collection<ActivityState> taskStates)
      throws InstanceNotFoundException;

  /**
   * Obtains the tasks with state either READY or EXECUTING or SUSPENDED or FINISHED for the given instance and for the
   * given user.<br>
   * If the task has been assigned to a user, only this user can get the task into the returned list. Otherwise all the
   * users that belong to the candidate list can get the task.
   * 
   * @param instanceUUID
   *          the instance UUID.
   * @param userId
   *          the userId for which the tasks are searched.
   * @param taskState
   *          the {@link org.ow2.bonita.facade.runtime.ActivityState state} of the task.
   * @return a collection of task records. If no tasks are found, an empty collection is returned.
   * @throws InstanceNotFoundException
   *           if no instance record is found with the given instance UUID.
   * @throws BonitaInternalException
   *           if an other exception occurs.
   */
  Collection<TaskInstance> getTaskList(ProcessInstanceUUID instanceUUID, String userId, ActivityState taskState)
      throws InstanceNotFoundException;

  /**
   * See getTaskList(ProcessInstanceUUID instanceUUID, String userId, ActivityState taskState)
   */
  Collection<LightTaskInstance> getLightTaskList(ProcessInstanceUUID instanceUUID, String userId,
      ActivityState taskState) throws InstanceNotFoundException;

  /**
   * Obtains the user tasks with state either READY or EXECUTING or SUSPENDED or FINISHED for the authenticated user.<br>
   * If the task has been assigned to a user, only this user can get the task into the returned list.<br>
   * Otherwise all the users that belong to the candidate list can get the task.
   * 
   * @param taskState
   *          the {@link org.ow2.bonita.facade.runtime.ActivityState state} of the task.
   * @return a collection of task records. If no tasks are found, an empty collection is returned.
   * @throws BonitaInternalException
   *           if an other exception occurs.
   */
  Collection<TaskInstance> getTaskList(ActivityState taskState);

  /**
   * See getTaskList(ActivityState taskState).
   */
  Collection<LightTaskInstance> getLightTaskList(ActivityState taskState);

  /**
   * Gets an activity UUID among all available tasks of the logged user according to the given activity state.
   * 
   * @param taskState
   *          the activity state
   * @return an activity UUID
   */
  ActivityInstanceUUID getOneTask(ActivityState taskState);

  /**
   * Gets an activity UUID among all available tasks of the logged user according to the given activity state and the
   * given process instance UUID.
   * 
   * @param instanceUUID
   *          the process instance UUID
   * @param taskState
   *          the activity state
   * @return an activity UUID
   */
  ActivityInstanceUUID getOneTask(ProcessInstanceUUID instanceUUID, ActivityState taskState);

  /**
   * Gets an activity UUID among all available tasks of the logged user according to the given activity state and the
   * given process UUID.
   * 
   * @param processUUID
   *          the process UUID
   * @param taskState
   *          the activity state
   * @return an activity UUID
   */
  ActivityInstanceUUID getOneTask(ProcessDefinitionUUID processUUID, ActivityState taskState);

  /**
   * Obtains the user tasks with state either READY or EXECUTING or SUSPENDED or FINISHED for the given user.<br>
   * If the task has been assigned to a user, only this user can get the task into the returned list.<br>
   * Otherwise all the users that belong to the candidate list can get the task.
   * 
   * @param userId
   *          the userId for which the tasks are searched.
   * @param taskState
   *          the {@link org.ow2.bonita.facade.runtime.ActivityState state} of the task.
   * @return a collection of task records. If no tasks are found, an empty collection is returned.
   * @throws BonitaInternalException
   *           if an other exception occurs.
   */
  Collection<TaskInstance> getTaskList(String userId, ActivityState taskState);

  /**
   * See getTaskList(String userId, ActivityState taskState)
   */
  Collection<LightTaskInstance> getLightTaskList(String userId, ActivityState taskState);

  /**
   * Gets the activity state of the activity according to its UUID
   * 
   * @param activityUUID
   *          the activity UUID
   * @return the activity state of the activity according to its UUID
   * @throws ActivityNotFoundException
   *           if this activity is not pointed by a process execution and the execution informations for this activity
   *           has not been recorded.
   */
  ActivityState getActivityInstanceState(ActivityInstanceUUID activityUUID) throws ActivityNotFoundException;

  /**
   * Obtains a variable defined as local to the activity for the given activity UUID and variable name. The activity
   * should either be executed or currently pointed by the a process execution.<br>
   * 
   * @param activityUUID
   *          the activity UUID.
   * @param variableName
   *          the variable name.
   * @return the variable object (can be: a plain {@link String}, a {@link Boolean}, a {@link Date}, a {@link Long} or a
   *         {@link Double} ).
   * @throws ActivityNotFoundException
   *           if this activity is not pointed by a process execution and the execution informations for this activity
   *           has not been recorded.
   * @throws VariableNotFoundException
   *           if no variable is found with the given name.
   * @throws BonitaInternalException
   *           if an other exception occurs.
   */
  Object getActivityInstanceVariable(ActivityInstanceUUID activityUUID, String variableName)
      throws ActivityNotFoundException, VariableNotFoundException;

  /**
   * Obtains the variables defined as local to the activity for the given activity UUID.<br>
   * An empty map is returned if no variable is found.
   * 
   * @param activityUUID
   *          the activity UUID.
   * @return the map of activity variables where key is the variable id and value is the variable object (can be: a
   *         plain {@link String}, a {@link Boolean}, a {@link Date}, a {@link Long} or a {@link Double} )).
   * @throws ActivityNotFoundException
   *           if this activity is not pointed by a process execution and the execution informations for this activity
   *           has not been recorded.
   * @throws BonitaInternalException
   *           if an other exception occurs.
   */
  Map<String, Object> getActivityInstanceVariables(ActivityInstanceUUID activityUUID) throws ActivityNotFoundException;

  /**
   * Obtains a variable for the given activity and variable name. This variable could be local to the activity or global
   * to the process. The activity should either be executed or currently pointed by the a process execution.
   * <p>
   * <i>For XML Type:</i>
   * <ul>
   * <li>getVariable(activityUUID, "myXmlData") returns a {@link org.w3c.dom.Document}
   * <li>getVariable(activityUUID, "myXmlData" + {@link BonitaConstants#XPATH_VAR_SEPARATOR} + "/root/node") returns a
   * {@link Node}</li>
   * <li>getVariable(activityUUID, "myXmlData" + {@link BonitaConstants#XPATH_VAR_SEPARATOR} + "/root/@attribute")
   * returns a {@link String}</li>
   * <li>getVariable(activityUUID, "myXmlData" + {@link BonitaConstants#XPATH_VAR_SEPARATOR} + "/root/text()") returns a
   * {@link String}</li>
   * <li>getVariable(activityUUID, "myXmlData" + {@link BonitaConstants#XPATH_VAR_SEPARATOR} +
   * anyOtherKindOfXPathExpression) returns a {@link Node}</li>
   * </ul>
   * </p>
   * 
   * @param activityUUID
   *          the activity UUID.
   * @param variableName
   *          the variable name.
   * @return the variable object (can be: a plain {@link String}, a {@link Boolean}, a {@link Date}, a {@link Long} or a
   *         {@link Double} ).
   * @throws ActivityNotFoundException
   *           if this activity is not pointed by a process execution and the execution informations for this activity
   *           has not been recorded.
   * @throws VariableNotFoundException
   *           if no variable is found with the given name.
   * @throws BonitaInternalException
   *           if an other exception occurs.
   */
  Object getVariable(ActivityInstanceUUID activityUUID, String variableName) throws ActivityNotFoundException,
      VariableNotFoundException;

  /**
   * Obtains the activity variables (including global process and local activity variables) for the given activity UUID.<br>
   * An empty map is returned if no variable is found.
   * 
   * @param activityUUID
   *          the activity UUID.
   * @return the map of activity variables where key is the variable id and value is the variable object (can be: a
   *         plain {@link String}, a {@link Boolean}, a {@link Date}, a {@link Long} or a {@link Double} ).
   * @throws ActivityNotFoundException
   *           if this activity is not pointed by a process execution and the execution informations for this activity
   *           has not been recorded.
   * @throws InstanceNotFoundException
   *           if no recorded informations is found with the given instance UUID.
   * @throws BonitaInternalException
   *           if an other exception occurs.
   */
  Map<String, Object> getVariables(ActivityInstanceUUID activityUUID) throws InstanceNotFoundException,
      ActivityNotFoundException;

  /**
   * Obtains a process variable for the given process instance UUID and variable name.
   * 
   * @param instanceUUID
   *          the instance UUID.
   * @param variableName
   *          the variable name.
   * @return the variable object (can be: a plain {@link String}, a {@link Boolean}, a {@link Date}, a {@link Long} or a
   *         {@link Double} ).
   * @throws InstanceNotFoundException
   *           if no instance has found with the given instance UUID.
   * @throws VariableNotFoundException
   *           if no variable is found with the given name.
   * @throws BonitaInternalException
   *           if an other exception occurs.
   */
  Object getProcessInstanceVariable(ProcessInstanceUUID instanceUUID, String variableName)
      throws InstanceNotFoundException, VariableNotFoundException;

  /**
   * Obtains the process variables for the given process instance UUID. An empty map is returned if no process variable
   * is found.
   * 
   * @param instanceUUID
   *          the instance UUID.
   * @return the map of process variables where key is the variable id and value is the variable object.
   * @throws InstanceNotFoundException
   *           if no instance has been found with the given instance UUID.
   * @throws BonitaInternalException
   *           if an other exception occurs.
   */
  Map<String, Object> getProcessInstanceVariables(ProcessInstanceUUID instanceUUID) throws InstanceNotFoundException;

  /**
   * Obtains the process variables for the given process instance UUID at the given date.
   * 
   * @param instanceUUID
   *          the instance UUID
   * @param maxDate
   * @return the map of process variables where key is the variable id and value is the variable object or an empty map.
   * @throws InstanceNotFoundException
   *           if no instance has been found with the given instance UUID.
   */
  Map<String, Object> getProcessInstanceVariables(ProcessInstanceUUID instanceUUID, Date maxDate)
      throws InstanceNotFoundException;

  /**
   * Obtains all the comments (activity and process) of a ProcessInstance. An empty List is returned if the Process has
   * no feed.
   * 
   * @param instanceUUID
   *          the instance UUID
   * @return the list containing all the comments
   * @throws InstanceNotFoundException
   *           if no instance was found with the given instance UUID.
   */
  List<Comment> getCommentFeed(ProcessInstanceUUID instanceUUID) throws InstanceNotFoundException;

  /**
   * Obtains the comments of an activity.
   * 
   * @param activityUUID
   *          the activity UUID
   * @return the list containing activity comments
   * @throws InstanceNotFoundException
   *           if no instance was found with the given instance UUID.
   */
  List<Comment> getActivityInstanceCommentFeed(ActivityInstanceUUID activityUUID) throws InstanceNotFoundException;

  /**
   * Obtains the comments belonging to the process.
   * 
   * @param instanceUUID
   *          the instance UUID
   * @return the list containing the process comments
   * @throws InstanceNotFoundException
   *           if no instance was found with the given instance UUID.
   */
  List<Comment> getProcessInstanceCommentFeed(ProcessInstanceUUID instanceUUID) throws InstanceNotFoundException;

  /**
   * Counts the number of comments of an activity.
   * 
   * @param activityUUID
   *          the activity UUID
   * @return the number of comments of an activity
   * @throws InstanceNotFoundException
   *           if no instance was found with the given instance UUID.
   */
  int getNumberOfActivityInstanceComments(ActivityInstanceUUID activityUUID) throws InstanceNotFoundException;

  /**
   * Counts the number of comments of all given activities.
   * 
   * @param activityUUIDs
   * @param queryList
   * @return
   * @throws InstanceNotFoundException
   */
  Map<ActivityInstanceUUID, Integer> getNumberOfActivityInstanceComments(Set<ActivityInstanceUUID> activityUUIDs)
      throws InstanceNotFoundException;

  /**
   * Counts the number of comments of a process.
   * 
   * @param instanceUUID
   *          the instance UUID
   * @return the number of comments of a process
   * @throws InstanceNotFoundException
   *           if no instance was found with the given instance UUID.
   */
  int getNumberOfProcessInstanceComments(ProcessInstanceUUID instanceUUID) throws InstanceNotFoundException;

  /**
   * Counts the number of all comments (activity and process) of a process.
   * 
   * @param instanceUUID
   *          the instance UUID
   * @return the number of all comments (activity and process) of a process
   * @throws InstanceNotFoundException
   *           if no instance was found with the given instance UUID.
   */
  int getNumberOfComments(ProcessInstanceUUID instanceUUID) throws InstanceNotFoundException;

  /**
   * Returns true if the given task is READY and: - if the task is assigned: if the assigned user is the logged user -
   * if the task is not assigned: if the logged user is in the candidates list
   * 
   * @param taskUUID
   *          the activity instance UUID of the task
   * @return true if the task can be executed
   * @throws TaskNotFoundException
   *           if no task has been found with the given task UUID.
   */
  boolean canExecuteTask(ActivityInstanceUUID taskUUID) throws TaskNotFoundException;

  /**
   * Returns all instances started by the logged user
   * 
   * @return all instances started by the logged user
   */
  Set<ProcessInstance> getUserInstances();

  /**
   * Returns all instances started by the logged user
   * 
   * @return all instances started by the logged user
   */
  Set<LightProcessInstance> getLightUserInstances();

  /**
   * Returns at most pageSize instances started by the logged user
   * 
   * @return instances started by the logged user from fromIndex to pageSize order by last update
   */
  List<LightProcessInstance> getLightParentUserInstances(int fromIndex, int pageSize);

  /**
   * Returns at most pageSize instances started by the logged user
   * 
   * @param fromIndex
   * @param pageSize
   * @param pagingCriterion
   *          the criterion used to sort the returned instances
   * @return instances started by the logged user from fromIndex to pageSize order by pagingCriterion
   */
  List<LightProcessInstance> getLightParentUserInstances(int fromIndex, int pageSize,
      ProcessInstanceCriterion pagingCriterion);

  /**
   * Returns at most pageSize instances started by the current user.</br> Instances of the given processes are ignored.
   * 
   * @param fromIndex
   * @param pageSize
   * @param processUUIDs
   * @return
   */
  List<LightProcessInstance> getLightParentUserInstancesExcept(int fromIndex, int pageSize,
      Set<ProcessDefinitionUUID> processUUIDs);

  /**
   * Returns at most pageSize instances started by the current user order by pagingCriterion.</br> Instances of the
   * given processes are ignored.
   * 
   * @param fromIndex
   * @param pageSize
   * @param processUUIDs
   * @param pagingCriterion
   *          the criterion used to sort the result list
   * @return
   */
  List<LightProcessInstance> getLightParentUserInstancesExcept(int fromIndex, int pageSize,
      Set<ProcessDefinitionUUID> processUUIDs, ProcessInstanceCriterion pagingCriterion);

  /**
   * Returns at most pageSize instances started by the given user.</br> Only instances of the given processes are
   * considered.
   * 
   * @param fromIndex
   * @param pageSize
   * @param processUUIDs
   * @return
   */
  List<LightProcessInstance> getLightParentUserInstances(int fromIndex, int pageSize,
      Set<ProcessDefinitionUUID> processUUIDs);

  /**
   * Returns at most pageSize instances started by the given user.</br> Only instances of the given processes are
   * considered.
   * 
   * @param fromIndex
   * @param pageSize
   * @param processUUIDs
   * @param pagingCriterion
   *          the criterion used to sort the result list
   * @return
   */
  List<LightProcessInstance> getLightParentUserInstances(int fromIndex, int pageSize,
      Set<ProcessDefinitionUUID> processUUIDs, ProcessInstanceCriterion pagingCriterion);

  /**
   * Returns all records of instance for the given process processDefinitionUUID.<br>
   * An empty set is returned if no instance is found.
   * 
   * @param processUUID
   *          the process definition UUID.
   * @return a set containing all instance records.
   * @throws ProcessNotFoundException
   *           if no process has been found with the given process UUID.
   * @throws BonitaInternalException
   *           if an exception occurs.
   */
  Set<LightProcessInstance> getLightProcessInstances(ProcessDefinitionUUID processUUID);

  /**
   * Returns all records of instance for the given process processDefinitionUUIDs.<br>
   * An empty set is returned if no instance is found.
   * 
   * @param processUUIDs
   *          the process definition UUIDs.
   * @return a set containing all instance records.
   * @throws ProcessNotFoundException
   *           if no process has been found with a given process UUID.
   * @throws BonitaInternalException
   *           if an exception occurs.
   */
  Set<LightProcessInstance> getLightWeightProcessInstances(Set<ProcessDefinitionUUID> processUUIDs);

  /**
   * Obtains the process attachment names.
   * 
   * @deprecated replaced by {@link #searchDocuments(DocumentSearchBuilder, int, int)}
   * @param instanceUUID
   *          the instance UUID
   * @return the attachment names of a process instance
   */
  @Deprecated
  Set<String> getAttachmentNames(ProcessInstanceUUID instanceUUID);

  /**
   * Obtains for each attachment (given by its name) the its last version for a process instance
   * 
   * @deprecated replaced by {@link #searchDocuments(DocumentSearchBuilder, int, int)}
   * @param instanceUUID
   *          the process instance UUID
   * @param attachmentNames
   *          the attachment names
   * @return a collection of {@link AttachmentInstance}. The method getAttachmentValue should be used to retrieve the
   *         content of an attachment
   */
  @Deprecated
  Collection<AttachmentInstance> getLastAttachments(ProcessInstanceUUID instanceUUID, Set<String> attachmentNames);

  /**
   * Obtains the last versions of some process attachments (the attachment name should match with the regular
   * expression)
   * 
   * @deprecated replaced by {@link #searchDocuments(DocumentSearchBuilder, int, int)}
   * @param instanceUUID
   *          the instance UUID
   * @param regex
   *          the regular expression
   * @return a collection of {@link AttachmentInstance}. The method getAttachmentValue should be used to retrieve the
   *         content of an attachment
   */
  @Deprecated
  Collection<AttachmentInstance> getLastAttachments(ProcessInstanceUUID instanceUUID, String regex);

  /**
   * Obtains versions of a process attachment according to its UUID and the attachment name.
   * 
   * @deprecated replaced by {@link #searchDocuments(DocumentSearchBuilder, int, int)}
   * @param instanceUUID
   *          the process instance UUID
   * @param attachmentName
   *          the attachment name
   * @return a list of {@link AttachmentInstance}. The method getAttachmentValue should be used to retrieve the content
   *         of an attachment
   */
  @Deprecated
  List<AttachmentInstance> getAttachments(ProcessInstanceUUID instanceUUID, String attachmentName);

  /**
   * Obtains the last attachment of a process according to its UUID and the attachment name.
   * 
   * @deprecated replaced by {@link #searchDocuments(DocumentSearchBuilder, int, int)}
   * @param instanceUUID
   *          the process instance UUID
   * @param attachmentName
   *          the attachment name
   * @return an {@link AttachmentInstance} corresponding to the last version of the attachment. The method
   *         getAttachmentValue should be used to retrieve the content of an attachment
   */
  @Deprecated
  AttachmentInstance getLastAttachment(ProcessInstanceUUID instanceUUID, String attachmentName);
  
  /**
   * Obtains the last version of a process attachment created before the given date.
   * 
   * @deprecated replaced by {@link #getLastDocument(ProcessInstanceUUID, String, Date)}
   * @param instanceUUID
   *          the process instance UUID
   * @param attachmentName
   *          the attachment name
   * @param date
   *          the date
   * @return an {@link AttachmentInstance} corresponding to the last version of the attachment created before the date.
   *         The method getAttachmentValue should be used to retrieve the content of an attachment
   */
  @Deprecated
  AttachmentInstance getLastAttachment(ProcessInstanceUUID instanceUUID, String attachmentName, Date date);

  /**
   * Obtains the last version of a process document created before the given date.
   * 
   * @param instanceUUID
   *          the process instance UUID
   * @param documentName
   *          the document name
   * @param date
   *          the date
   * @return a {@link Document} corresponding to the last version of the document created before the date.
   *         The method {@link #getDocumentContent(DocumentUUID)} should be used to retrieve the content of a document
   */
  Document getLastDocument(ProcessInstanceUUID instanceUUID, String documentName, Date date);

  /**
   * Obtains the last version of a process attachment created before an activity end.
   * 
   * @deprecated replaced by {@link #getLastDocument(ProcessInstanceUUID, String, ActivityInstanceUUID)}
   * @param instanceUUID
   *          the process instance UUID
   * @param attachmentName
   *          the attachment name
   * @param activityUUID
   *          the activity instance UUID
   * @return an {@link AttachmentInstance} corresponding to the last version of the attachment created before the end of
   *         the activity. The method getAttachmentValue should be used to retrieve the content of an attachment
   * @throws ActivityNotFoundException
   *           if this activity is not pointed by a process execution and the execution informations for this activity
   *           has not been recorded.
   */
  @Deprecated
  AttachmentInstance getLastAttachment(ProcessInstanceUUID instanceUUID, String attachmentName,
      ActivityInstanceUUID activityUUID) throws ActivityNotFoundException;

  /**
   * Obtains the last version of a process document created before an activity end.
   * 
   * @param instanceUUID
   *          the process instance UUID
   * @param documentName
   *          the document name
   * @param activityUUID
   *          the activity instance UUID
   * @return {@link Document} corresponding to the last version of the document created before the end of
   *         the activity. The method {@link #getDocumentContent(DocumentUUID)} should be used to retrieve the content of a document
   * @throws ActivityNotFoundException
   *           if this activity is not pointed by a process execution and the execution informations for this activity
   *           has not been recorded.
   */
  Document getLastDocument(ProcessInstanceUUID instanceUUID, String documentName,
      ActivityInstanceUUID activityUUID) throws ActivityNotFoundException;

  /**
   * Gets the content of an attachment instance as a byte array. This method is the only way to retrieve an attachment
   * content from an attachment instance.
   * 
   * @deprecated replaced by {@link #getDocumentContent(DocumentUUID)}
   * @param attachmentInstance
   *          the attachment instance
   * @return the content of an attachment instance
   */
  @Deprecated
  byte[] getAttachmentValue(AttachmentInstance attachmentInstance);

  /**
   * Gets LightProcessInstances that are not sub-process instances and having the given userId member of the active
   * user.
   * 
   * @param userId
   * @param fromIndex
   * @param pageSize
   * @return
   */
  List<LightProcessInstance> getLightParentProcessInstancesWithActiveUser(String userId, int fromIndex, int pageSize);

  /**
   * Gets LightProcessInstances that are not sub-process instances and having the given userId member of the active
   * user.
   * 
   * @param userId
   * @param fromIndex
   * @param pageSize
   * @param pagingCriterion
   *          the criterion used to sort the result list
   * @return
   */
  List<LightProcessInstance> getLightParentProcessInstancesWithActiveUser(String userId, int fromIndex, int pageSize,
      ProcessInstanceCriterion pagingCriterion);

  /**
   * Gets the LightProcessInstances that are not sub-process instances and having the given userId member of the active
   * user.<br>
   * Instances of processes given in parameter are ignored.
   * 
   * @param aUsername
   * @param aFromIndex
   * @param aPageSize
   * @param aProcessUUIDs
   * @return
   */
  List<LightProcessInstance> getLightParentProcessInstancesWithActiveUserExcept(String userId, int fromIndex,
      int pageSize, Set<ProcessDefinitionUUID> processUUIDs);

  /**
   * Gets LightProcessInstances that are not sub-process instances and having the given userId member of the active user
   * order by the pagingCriterion.<br>
   * Instances of processes given in parameter are ignored.
   * 
   * @param userId
   * @param fromIndex
   * @param pageSize
   * @param processUUIDs
   * @param pagingCriterion
   *          the criterion used to sort the result list
   * @return
   */
  List<LightProcessInstance> getLightParentProcessInstancesWithActiveUserExcept(String userId, int fromIndex,
      int pageSize, Set<ProcessDefinitionUUID> processUUIDs, ProcessInstanceCriterion pagingCriterion);

  /**
   * Gets LightProcessInstances that are not sub-process instances and having the given userId member of the active
   * user.<br>
   * Only instances of processes given in parameter are considered.
   * 
   * @param aUsername
   * @param aFromIndex
   * @param aPageSize
   * @param aProcessUUIDs
   * @return
   */
  List<LightProcessInstance> getLightParentProcessInstancesWithActiveUser(String username, int fromIndex, int pageSize,
      Set<ProcessDefinitionUUID> processUUIDs);

  /**
   * Gets LightProcessInstances that are not sub-process instances and having the given userId member of the active
   * user.<br>
   * Only instances of processes given in parameter are considered.
   * 
   * @param username
   * @param fromIndex
   * @param pageSize
   * @param processUUIDs
   * @param pagingCriterion
   *          the criterion used to sort the result list
   * @return
   */
  List<LightProcessInstance> getLightParentProcessInstancesWithActiveUser(String username, int fromIndex, int pageSize,
      Set<ProcessDefinitionUUID> processUUIDs, ProcessInstanceCriterion pagingCriterion);

  /**
   * Gets LightProcessInstances that are not sub-process instances and having the given userId member of the involved
   * user.
   * 
   * @param aUserId
   * @param aFromIndex
   * @param aPageSize
   * @return
   */
  List<LightProcessInstance> getLightParentProcessInstancesWithInvolvedUser(String userId, int fromIndex, int pageSize);

  /**
   * Gets LightProcessInstances that are not sub-process instances and having the given userId member of the involved
   * user order by pagingCriterion.
   * 
   * @param userId
   * @param fromIndex
   * @param pageSize
   * @param pagingCriterion
   *          the criterion used to sort the result list
   * @return
   */
  List<LightProcessInstance> getLightParentProcessInstancesWithInvolvedUser(String userId, int fromIndex, int pageSize,
      ProcessInstanceCriterion pagingCriterion);

  /**
   * Gets LightProcessInstances that are not sub-process instances and having the given userId member of the involved
   * user.<br>
   * 
   * @param userId
   * @param fromIndex
   * @param pageSize
   * @param processUUIDs
   * @return
   */
  List<LightProcessInstance> getLightParentProcessInstancesWithInvolvedUser(String userId, int fromIndex, int pageSize,
      Set<ProcessDefinitionUUID> processUUIDs);

  /**
   * Gets LightProcessInstances that are not sub-process instances and having the given userId member of the involved
   * user order by the given pagingCriterion.<br>
   * 
   * @param userId
   * @param fromIndex
   * @param pageSize
   * @param processUUIDs
   * @param pagingCriterion
   *          the criterion used to sort the result list
   * @return
   */
  List<LightProcessInstance> getLightParentProcessInstancesWithInvolvedUser(String userId, int fromIndex, int pageSize,
      Set<ProcessDefinitionUUID> processUUIDs, ProcessInstanceCriterion pagingCriterion);

  /**
   * Gets LightProcessInstances that are not sub-process instances and having the given userId member of the involved
   * user.<br>
   * Instances of the given processes are ignored.
   * 
   * @param username
   * @param fromIndex
   * @param pageSize
   * @param processUUIDs
   * @return
   */
  List<LightProcessInstance> getLightParentProcessInstancesWithInvolvedUserExcept(String username, int fromIndex,
      int pageSize, Set<ProcessDefinitionUUID> processUUIDs);

  /**
   * Gets LightProcessInstances that are not sub-process instances and having the given userId member of the involved
   * user order by the given pagingCriterion.<br>
   * Instances of the given processes are ignored.
   * 
   * @param username
   * @param fromIndex
   * @param pageSize
   * @param processUUIDs
   * @param pagingCriterion
   *          the criterion used to sort the resulting list
   * @return
   */
  List<LightProcessInstance> getLightParentProcessInstancesWithInvolvedUserExcept(String username, int fromIndex,
      int pageSize, Set<ProcessDefinitionUUID> processUUIDs, ProcessInstanceCriterion pagingCriterion);

  /**
   * Gets the number of ProcessInstances that are not sub-process instances and having the given userId member of the
   * active user. A user is active in a ProcessInstance when he or she has currently a step to perform.
   * 
   * @param userId
   * @return
   */
  Integer getNumberOfParentProcessInstancesWithActiveUser(String userId);

  /**
   * Gets the number of ProcessInstances that are not sub-process instances and having the given userId member of the
   * active user.</br> A user is active in a ProcessInstance when he or she has currently a step to perform.</br>
   * Instances of the given processes are ignored.
   * 
   * @param username
   * @param processUUIDs
   * @return
   */
  Integer getNumberOfParentProcessInstancesWithActiveUserExcept(String username, Set<ProcessDefinitionUUID> processUUIDs);

  /**
   * Gets the number of ProcessInstances that are not sub-process instances and having the given userId member of the
   * active user.</br> A user is active in a ProcessInstance when he or she has currently a step to perform.</br> Only
   * instances of the given processes are considered.
   * 
   * @param username
   * @param processUUIDs
   * @return
   */
  Integer getNumberOfParentProcessInstancesWithActiveUser(String username, Set<ProcessDefinitionUUID> processUUIDs);

  /**
   * Gets the number of ProcessInstances that are not sub-process instances and having the given userId member of the
   * involved user.
   * 
   * @param userId
   * @return
   */
  Integer getNumberOfParentProcessInstancesWithInvolvedUser(String userId);

  /**
   * Gets the number of ProcessInstances that are not sub-process instances and having the given userId member of the
   * involved user.</br> Instances of the given processes are ignored.
   * 
   * @param username
   * @param processUUIDs
   * @return
   */
  Integer getNumberOfParentProcessInstancesWithInvolvedUserExcept(String username,
      Set<ProcessDefinitionUUID> processUUIDs);

  /**
   * Gets the number of ProcessInstances that are not sub-process instances and having the given userId member of the
   * involved user.</br> Only instances of the given processes are considered.
   * 
   * @param username
   * @param processUUIDs
   * @return
   */
  Integer getNumberOfParentProcessInstancesWithInvolvedUser(String username, Set<ProcessDefinitionUUID> processUUIDs);

  /**
   * Gets the number of ProcessInstances that are not sub-process instances and having the given userId member of the
   * active user.
   * 
   * @param userId
   * @return
   */
  Integer getNumberOfParentProcessInstancesWithStartedBy(String userId);

  /**
   * Gets the number of ProcessInstances that are not sub-process instances and having the given userId member of the
   * active user.</br> Instances of the given processes are ignored.
   * 
   * @param username
   * @param processUUIDs
   * @return
   */
  Integer getNumberOfParentProcessInstancesWithStartedByExcept(String username, Set<ProcessDefinitionUUID> processUUIDs);

  /**
   * Gets the number of ProcessInstances that are not sub-process instances and having the given userId member of the
   * active user.</br> Only instances of the given processes are considered.
   * 
   * @param username
   * @param processUUIDs
   * @return
   */
  Integer getNumberOfParentProcessInstancesWithStartedBy(String username, Set<ProcessDefinitionUUID> processUUIDs);

  /**
   * Gets the number of ProcessInstances that are not sub-process instances and having the given userId member of the
   * involved users and the given category.
   * 
   * @param userId
   * @param category
   * @return
   */
  Integer getNumberOfParentProcessInstancesWithInvolvedUserAndCategory(String userId, String category);

  /**
   * Gets the number of ProcessInstances that are not sub-process instances and having the given userId member of the
   * involved users and the given category.</br> Instances of the given processes are ignored.
   * 
   * @param username
   * @param category
   * @param processUUIDs
   * @return
   */
  Integer getNumberOfParentProcessInstancesWithInvolvedUserAndCategoryExcept(String username, String category,
      Set<ProcessDefinitionUUID> processUUIDs);

  /**
   * Gets the number of ProcessInstances that are not sub-process instances and having the given userId member of the
   * involved users and the given category.</br> Only instances of the given processes are considered.
   * 
   * @param username
   * @param category
   * @param processUUIDs
   * @return
   */
  Integer getNumberOfParentProcessInstancesWithInvolvedUserAndCategory(String username, String category,
      Set<ProcessDefinitionUUID> processUUIDs);

  /**
   * Gets the ProcessInstances that are not sub-process instances and having the given userId member of the active users
   * and having at least one task with the expected end date in the next {@code remainingDays}.
   * 
   * @param userId
   * @param remainingDays
   * @param fromIndex
   * @param pageSize
   * @return
   */
  List<LightProcessInstance> getLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate(
      String userId, int remainingDays, int fromIndex, int pageSize);

  /**
   * Gets the ProcessInstances that are not sub-process instances and having the given userId member of the active users
   * and having at least one task with the expected end date in the next {@code remainingDays} order by the given
   * pagingCriterion.
   * 
   * @param userId
   * @param remainingDays
   * @param fromIndex
   * @param pageSize
   * @param pagingCriterion
   *          the criterion used to sort the resulting list
   * @return
   */
  List<LightProcessInstance> getLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate(
      String userId, int remainingDays, int fromIndex, int pageSize, ProcessInstanceCriterion pagingCriterion);

  /**
   * Gets the ProcessInstances that are not sub-process instances and having the given userId member of the active users
   * and having at least one task with the expected end date in the next {@code remainingDays}.<br/>
   * Instance of given processes are ignored.
   * 
   * @param username
   * @param fromIndex
   * @param pageSize
   * @param remainingDays
   * @param processUUIDs
   * @return
   */
  List<LightProcessInstance> getLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDateExcept(
      String username, int remainingDays, int fromIndex, int pageSize, Set<ProcessDefinitionUUID> processUUIDs);

  /**
   * Gets the ProcessInstances that are not sub-process instances and having the given userId member of the active users
   * and having at least one task with the expected end date in the next {@code remainingDays} order by the given
   * pagingCriterion.<br/>
   * Instance of given processes are ignored.
   * 
   * @param username
   * @param fromIndex
   * @param pageSize
   * @param remainingDays
   * @param processUUIDs
   * @param pagingCriterion
   *          the criterion used to sort the process instances
   * @return
   */
  List<LightProcessInstance> getLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDateExcept(
      String username, int remainingDays, int fromIndex, int pageSize, Set<ProcessDefinitionUUID> processUUIDs,
      ProcessInstanceCriterion pagingCriterion);

  /**
   * Gets the ProcessInstances that are not sub-process instances and having the given userId member of the active users
   * and having at least one task with the expected end date in the next {@code remainingDays}.<br/>
   * Only instances of given processes are considered.
   * 
   * @param username
   * @param fromIndex
   * @param pageSize
   * @param processUUIDs
   * @return
   */
  List<LightProcessInstance> getLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate(
      String username, int remainingDays, int fromIndex, int pageSize, Set<ProcessDefinitionUUID> processUUIDs);

  /**
   * Gets the ProcessInstances that are not sub-process instances and having the given userId member of the active users
   * and having at least one task with the expected end date in the next {@code remainingDays} order by the giver
   * pagingCriterion.<br/>
   * Only instances of given processes are considered.
   * 
   * @param username
   * @param fromIndex
   * @param pageSize
   * @param processUUIDs
   * @return
   */
  List<LightProcessInstance> getLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate(
      String username, int remainingDays, int fromIndex, int pageSize, Set<ProcessDefinitionUUID> processUUIDs,
      ProcessInstanceCriterion pagingCriterion);

  /**
   * Gets the number of ProcessInstances that are not sub-process instances and having the given userId member of the
   * active users and having at least one task with the expected end date in the next {@code remainingDays}.
   * 
   * @param userId
   * @param remainingDays
   * @return
   */
  Integer getNumberOfParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate(String userId,
      int remainingDays);

  /**
   * Gets the number of ProcessInstances that are not sub-process instances and having the given userId member of the
   * active users and having at least one task with the expected end date in the next {@code remainingDays}.</br>
   * Instances of the given processes are ignored.
   * 
   * @param username
   * @param processUUIDs
   * @return
   */
  Integer getNumberOfParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDateExcept(String username,
      int remainingDays, Set<ProcessDefinitionUUID> processUUIDs);

  /**
   * Gets the number of ProcessInstances that are not sub-process instances and having the given userId member of the
   * active users and having at least one task with the expected end date in the next {@code remainingDays}.</br> Only
   * instances of the given processes are considered.
   * 
   * @param username
   * @param processUUIDs
   * @return
   */
  Integer getNumberOfParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate(String username,
      int remainingDays, Set<ProcessDefinitionUUID> processUUIDs);

  /**
   * Gets the number of ProcessInstances that are not sub-process instances and having the given userId member of the
   * active users and having at least one task with the expected end date in the past.
   * 
   * @param userId
   * @return
   */
  Integer getNumberOfParentProcessInstancesWithOverdueTasks(String userId);

  /**
   * Gets the number of ProcessInstances that are not sub-process instances and having the given userId member of the
   * active users and having at least one task with the expected end date in the past.</br> Instances of the given
   * processes are ignored.
   * 
   * @param username
   * @param processUUIDs
   * @return
   */
  Integer getNumberOfParentProcessInstancesWithOverdueTasksExcept(String username,
      Set<ProcessDefinitionUUID> processUUIDs);

  /**
   * Gets the number of ProcessInstances that are not sub-process instances and having the given userId member of the
   * active users and having at least one task with the expected end date in the past.</br> Only instances of the given
   * processes are considered.
   * 
   * @param username
   * @param processUUIDs
   * @return
   */
  Integer getNumberOfParentProcessInstancesWithOverdueTasks(String username, Set<ProcessDefinitionUUID> processUUIDs);

  /**
   * Gets the ProcessInstances that are not sub-process instances and having the given userId member of the active users
   * and having at least one task with the expected end date in the past.
   * 
   * @param userId
   * @param fromIndex
   * @param pageSize
   * @return
   */
  List<LightProcessInstance> getLightParentProcessInstancesWithOverdueTasks(String userId, int fromIndex, int pageSize);

  /**
   * Gets the ProcessInstances that are not sub-process instances and having the given userId member of the active users
   * and having at least one task with the expected end date in the past order by the given pagingCriterion.
   * 
   * @param userId
   * @param fromIndex
   * @param pageSize
   * @param pagingCriterion
   *          the criterion used to sort the ProcessInstances
   * @return
   */
  List<LightProcessInstance> getLightParentProcessInstancesWithOverdueTasks(String userId, int fromIndex, int pageSize,
      ProcessInstanceCriterion pagingCriterion);

  /**
   * Gets the ProcessInstances that are not sub-process instances and having the given userId member of the active users
   * and having at least one task with the expected end date in the past.<br>
   * Instances of the given processes are ignored.
   * 
   * @param username
   * @param fromIndex
   * @param pageSize
   * @param processUUIDs
   * @return
   */
  List<LightProcessInstance> getLightParentProcessInstancesWithOverdueTasksExcept(String username, int fromIndex,
      int pageSize, Set<ProcessDefinitionUUID> processUUIDs);

  /**
   * Gets the ProcessInstances that are not sub-process instances and having the given userId member of the active users
   * and having at least one task with the expected end date in the past order by the given pagingCriterion.<br>
   * Instances of the given processes are ignored.
   * 
   * @param username
   * @param fromIndex
   * @param pageSize
   * @param processUUIDs
   * @param pagingCriterion
   *          the criterion used to sort the ProcessInstances
   * @return
   */
  List<LightProcessInstance> getLightParentProcessInstancesWithOverdueTasksExcept(String username, int fromIndex,
      int pageSize, Set<ProcessDefinitionUUID> processUUIDs, ProcessInstanceCriterion pagingCriterion);

  /**
   * Gets the ProcessInstances that are not sub-process instances and having the given userId member of the active users
   * and having at least one task with the expected end date in the past.<br>
   * Only instances of the given processes are considered.
   * 
   * @param username
   * @param fromIndex
   * @param pageSize
   * @param processUUIDs
   * @return
   */
  List<LightProcessInstance> getLightParentProcessInstancesWithOverdueTasks(String username, int fromIndex,
      int pageSize, Set<ProcessDefinitionUUID> processUUIDs);

  /**
   * Gets the ProcessInstances that are not sub-process instances and having the given userId member of the active users
   * and having at least one task with the expected end date in the past order by the given pagingCriterion.<br>
   * Only instances of the given processes are considered.
   * 
   * @param username
   * @param fromIndex
   * @param pageSize
   * @param processUUIDs
   * @param pagingCriterion
   *          the criterion used to sort the ProcessInstances
   * @return
   */
  List<LightProcessInstance> getLightParentProcessInstancesWithOverdueTasks(String username, int fromIndex,
      int pageSize, Set<ProcessDefinitionUUID> processUUIDs, ProcessInstanceCriterion pagingCriterion);

  /**
   * List name of active users.
   * 
   * @param uuid
   * @return
   * @throws InstanceNotFoundException
   */
  Set<String> getActiveUsersOfProcessInstance(ProcessInstanceUUID uuid) throws InstanceNotFoundException;

  /**
   * List name of active users of each given process instance.
   * 
   * @param instanceUUIDs
   * @return
   */
  Map<ProcessInstanceUUID, Set<String>> getActiveUsersOfProcessInstances(Set<ProcessInstanceUUID> instanceUUIDs)
      throws InstanceNotFoundException;

  /**
   * Searches Processes, Activities, ... according to the query in a paginated way. Note: for more details about what to
   * retrieve check the available indexes in package org.ow2.bonita.search.index. <br />
   * Limitation: when searching entities which contain variables, it is not possible to find a named variable with a
   * specific value. The method returns entities with a variable with the given name or with the given value. For
   * example: In a process, if there are two variables A = 1 and B = 2, the query searches a variable with name A and
   * value 3, the process will be in the result list.
   * 
   * @param query
   *          the query
   * @param fromResult
   *          the first Bonita Object
   * @param maxResults
   *          the max number of Bonita objects to retrieve
   * @return the list of Bonita Objects
   */
  <T> List<T> search(SearchQueryBuilder query, int fromResult, int maxResults);

  /**
   * Searches Processes, Activities, ... according to the query. Note: for more details about what to retrieve check the
   * available indexes in package org.ow2.bonita.search.index. <br />
   * Limitation: when searching entities which contain variables, it is not possible to find a named variable with a
   * specific value. The method returns entities with a variable with the given name or with the given value. For
   * example: In a process, if there are two variables A = 1 and B = 2, the query searches a variable with name A and
   * value 3, the process will be counted.
   * 
   * @param query
   *          the query
   * @return the list of Bonita Objects
   */
  int search(SearchQueryBuilder query);

  /**
   * Returns the event according to its UUID.
   * 
   * @param eventUUID
   *          the eventUUID
   * @return the event
   * @throws EventNotFoundException
   *           if the event is not found
   */
  CatchingEvent getEvent(CatchingEventUUID eventUUID) throws EventNotFoundException;

  /**
   * Returns all events which are waiting to be executed.
   * 
   * @return a set of catching events or an empty set if no event exists
   */
  Set<CatchingEvent> getEvents();

  /**
   * Returns all process instance events which are waiting to be executed.
   * 
   * @param instanceUUID
   *          the process instance UUID
   * @return a set of catching events or an empty set if no event exists
   */
  Set<CatchingEvent> getEvents(ProcessInstanceUUID instanceUUID);

  /**
   * Returns all activity instance events which are waiting to be executed.
   * 
   * @param activityUUID
   *          the activity instance UUID
   * @return a set of catching events or an empty set if no event exists
   */
  Set<CatchingEvent> getEvents(ActivityInstanceUUID activityUUID);

  /**
   * Get the content of a given document identified by its DocumentUUID.
   * 
   * @param doc
   *          the identifier of the document to read content of.
   * @return the content of the document.
   * @throws DocumentNotFoundException
   */
  byte[] getDocumentContent(DocumentUUID documentUUID) throws DocumentNotFoundException;

  /**
   * Search documents according to the query from the DocumentSearchBuilder.
   * 
   * @param builder
   *          the DocumentSearchBuilder which contains the query
   * @param fromResult
   *          the first Document to retrieve
   * @param maxResults
   *          the maximum number of documents to retrieve
   * @return the paginated documents which match with the query
   */
  DocumentResult searchDocuments(final DocumentSearchBuilder builder, final int fromResult, final int maxResults);

  /**
   * Gets the document according to its UUID.
   * 
   * @param documentUUID
   *          the document UUID
   * @return the document
   * @throws DocumentNotFoundException
   *           if the UUID does not refer to a real document
   */
  Document getDocument(final DocumentUUID documentUUID) throws DocumentNotFoundException;

  /**
   * Returns all documents according to its UUIDs.
   * 
   * @param documentUUIDs
   *          the list of document UUIDs
   * @return the list of documents
   * @throws DocumentNotFoundException
   *           if an UUID does not refer to a real document
   */
  List<Document> getDocuments(final List<DocumentUUID> documentUUIDs) throws DocumentNotFoundException;

  /**
   * Gets all versions of the document.
   * 
   * @param documentUUID
   *          the document UUID
   * @return all versions of the document
   * @throws DocumentNotFoundException
   *           if the UUID does not refer to a real document
   */
  List<Document> getDocumentVersions(final DocumentUUID documentUUID) throws DocumentNotFoundException;

  /**
   * Returns the involved users of the given process instance.
   * 
   * @param instanceUUID
   *          the process instance UUID
   * @return the set of involved users or an empty set
   * @throws InstanceNotFoundException
   *           occurs when the process instance UUID does not refer to a process instance
   */
  Set<String> getInvolvedUsersOfProcessInstance(ProcessInstanceUUID instanceUUID) throws InstanceNotFoundException;

  /**
   * Returns the subprocess UUIDS of the given process instance.
   * 
   * @param instanceUUID
   *          the process instance UUID
   * @return the set of subprocess UUIDS or an empty set
   * @throws InstanceNotFoundException
   *           occurs when the process instance UUID does not refer to a process instance
   */
  Set<ProcessInstanceUUID> getChildrenInstanceUUIDsOfProcessInstance(ProcessInstanceUUID instanceUUID)
      throws InstanceNotFoundException;

}
