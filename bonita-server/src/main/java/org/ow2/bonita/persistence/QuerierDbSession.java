/**
 * Copyright (C) 2006  Bull S. A. S.
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
 **/
package org.ow2.bonita.persistence;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ow2.bonita.facade.def.InternalActivityDefinition;
import org.ow2.bonita.facade.def.InternalProcessDefinition;
import org.ow2.bonita.facade.def.element.MetaData;
import org.ow2.bonita.facade.def.majorElement.ActivityDefinition.Type;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition.ProcessState;
import org.ow2.bonita.facade.paging.ActivityInstanceCriterion;
import org.ow2.bonita.facade.paging.ProcessDefinitionCriterion;
import org.ow2.bonita.facade.paging.ProcessInstanceCriterion;
import org.ow2.bonita.facade.runtime.ActivityState;
import org.ow2.bonita.facade.runtime.Category;
import org.ow2.bonita.facade.runtime.Comment;
import org.ow2.bonita.facade.runtime.InstanceState;
import org.ow2.bonita.facade.runtime.TaskInstance;
import org.ow2.bonita.facade.runtime.impl.CategoryImpl;
import org.ow2.bonita.facade.runtime.impl.InternalActivityInstance;
import org.ow2.bonita.facade.runtime.impl.InternalProcessInstance;
import org.ow2.bonita.facade.uuid.ActivityDefinitionUUID;
import org.ow2.bonita.facade.uuid.ActivityInstanceUUID;
import org.ow2.bonita.facade.uuid.CategoryUUID;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.runtime.model.Execution;
import org.ow2.bonita.search.SearchQueryBuilder;

public interface QuerierDbSession extends DbSession {

  /**
   * TASK INSTANCE
   */
  Set<TaskInstance> getUserTasks(String userId, Collection<ActivityState> taskStates);

  Set<TaskInstance> getUserInstanceTasks(String userId, ProcessInstanceUUID instanceUUID, ActivityState taskState);

  TaskInstance getTaskInstance(ActivityInstanceUUID taskUUID);

  Set<TaskInstance> getTaskInstances(ProcessInstanceUUID instanceUUID);

  Set<TaskInstance> getTaskInstances(ProcessInstanceUUID instanceUUID, Set<String> taskNames);

  TaskInstance getOneTask(String userId, ActivityState taskState);

  TaskInstance getOneTask(String userId, ProcessDefinitionUUID processUUID, ActivityState taskState);

  TaskInstance getOneTask(String userId, ActivityState taskState, Set<ProcessDefinitionUUID> definitionUUIDs);

  TaskInstance getOneTask(String userId, ProcessInstanceUUID instanceUUID, ActivityState taskState);

  /**
   * PROCESS INSTANCE
   */
  InternalProcessInstance getProcessInstance(ProcessInstanceUUID instanceUUID);

  Set<InternalProcessInstance> getProcessInstances();

  Set<InternalProcessInstance> getProcessInstances(ProcessDefinitionUUID processId);

  Set<InternalProcessInstance> getProcessInstances(Collection<ProcessInstanceUUID> instanceUUIDs);

  Set<InternalProcessInstance> getUserInstances(String userId);

  Set<InternalProcessInstance> getUserInstances(String userId, Set<ProcessDefinitionUUID> definitionUUIDs);

  Set<InternalProcessInstance> getUserInstances(String userId, Date minStartDate);

  Set<InternalProcessInstance> getUserParentInstances(String userId, Date minStartDate);

  Set<InternalProcessInstance> getUserInstancesExcept(String userId, Set<ProcessInstanceUUID> myCases);

  Set<InternalProcessInstance> getProcessInstances(ProcessDefinitionUUID processUUID, InstanceState instanceState);

  Set<InternalProcessInstance> getProcessInstancesWithTaskState(Collection<ActivityState> activityStates);

  Set<InternalProcessInstance> getProcessInstancesWithTaskState(Collection<ActivityState> activityStates,
      Set<ProcessDefinitionUUID> definitionUUIDs);

  Set<InternalProcessInstance> getProcessInstancesWithInstanceStates(Collection<InstanceState> instanceStates);

  Set<InternalProcessInstance> getProcessInstancesWithInstanceStates(Collection<InstanceState> instanceStates,
      Set<ProcessDefinitionUUID> visibleProcessUUIDs);

  long getLastProcessInstanceNb(ProcessDefinitionUUID processUUID);

  Execution getExecutionPointingOnNode(ActivityInstanceUUID activityUUID);

  Execution getExecutionWithEventUUID(String eventUUID);

  Set<Execution> getExecutions(ProcessInstanceUUID instanceUUID);

  Set<ProcessInstanceUUID> getParentInstancesUUIDs();

  Set<InternalProcessInstance> getProcessInstances(Set<ProcessDefinitionUUID> definitionUUIDs);

  List<InternalProcessInstance> getProcessInstances(Collection<ProcessInstanceUUID> instanceUUIDs, int fromIndex,
      int pageSize);

  List<InternalProcessInstance> getProcessInstancesWithInstanceUUIDs(Set<ProcessInstanceUUID> instanceUUIDs,
      int fromIndex, int pageSize, ProcessInstanceCriterion pagingCriterion);

  List<InternalProcessInstance> getProcessInstances(int fromIndex, int pageSize);

  List<InternalProcessInstance> getProcessInstances(int fromIndex, int pageSize,
      ProcessInstanceCriterion pagingCriterion);

  List<InternalProcessInstance> getProcessInstances(Set<ProcessDefinitionUUID> definitionUUIDs, int fromIndex,
      int pageSize);

  List<InternalProcessInstance> getProcessInstances(Set<ProcessDefinitionUUID> definitionUUIDs, int fromIndex,
      int pageSize, ProcessInstanceCriterion pagingCriterion);

  List<InternalProcessInstance> getParentProcessInstances(int fromIndex, int pageSize);

  List<InternalProcessInstance> getParentProcessInstances(int fromIndex, int pageSize,
      ProcessInstanceCriterion pagingCriterion);

  List<InternalProcessInstance> getParentProcessInstances(Set<ProcessDefinitionUUID> processUUIDs, int fromIndex,
      int pageSize, ProcessInstanceCriterion pagingCriterion);

  List<InternalProcessInstance> getParentProcessInstancesExcept(Set<ProcessDefinitionUUID> exceptions, int fromIndex,
      int pageSize, ProcessInstanceCriterion pagingCriterion);

  List<InternalProcessInstance> getMostRecentProcessInstances(int maxResults, long time);

  List<InternalProcessInstance> getMostRecentProcessInstances(int maxResults, long time,
      ProcessInstanceCriterion pagingCriterion);

  List<InternalProcessInstance> getMostRecentParentProcessInstances(int maxResults, long time);

  List<InternalProcessInstance> getMostRecentParentProcessInstances(int maxResults, long time,
      ProcessInstanceCriterion pagingCriterion);

  List<InternalProcessInstance> getMostRecentMatchingProcessInstances(Collection<ProcessInstanceUUID> instanceUUIDs,
      int maxResults, long time);

  List<InternalProcessInstance> getMostRecentMatchingProcessInstances(Set<ProcessInstanceUUID> instanceUUIDs,
      int maxResults, long time, ProcessInstanceCriterion pagingCriterion);

  List<InternalProcessInstance> getMostRecentProcessesProcessInstances(
      Collection<ProcessDefinitionUUID> definitionUUIDs, int maxResults, long time);

  List<InternalProcessInstance> getMostRecentProcessesProcessInstances(
      Collection<ProcessDefinitionUUID> definitionUUIDs, int maxResults, long time,
      ProcessInstanceCriterion pagingCriterion);

  List<InternalProcessInstance> getParentProcessInstances(ProcessDefinitionUUID definitionUUID, int fromIndex,
      int pageSize);

  List<InternalProcessInstance> getParentUserInstances(String userId, int startingIndex, int pageSize);

  List<InternalProcessInstance> getParentUserInstances(String userId, int startingIndex, int pageSize,
      ProcessInstanceCriterion pagingCriterion);

  List<InternalProcessInstance> getParentUserInstances(String userId, int startingIndex, int pageSize,
      Set<ProcessDefinitionUUID> definitionUUIDs);

  List<InternalProcessInstance> getParentUserInstances(String userId, int startingIndex, int pageSize,
      Set<ProcessDefinitionUUID> definitionUUIDs, ProcessInstanceCriterion pagingCriterion);

  List<InternalProcessInstance> getParentProcessInstancesWithActiveUser(String aUserId, int aStartingIndex,
      int aPageSize, Set<ProcessDefinitionUUID> aVisibleProcessUUIDs);

  List<InternalProcessInstance> getParentProcessInstancesWithActiveUser(String aUserId, int aStartingIndex,
      int aPageSize, Set<ProcessDefinitionUUID> aVisibleProcessUUIDs, ProcessInstanceCriterion pagingCriterion);

  List<InternalProcessInstance> getParentProcessInstancesWithActiveUser(String aUserId, int aStartingIndex,
      int aPageSize);

  List<InternalProcessInstance> getParentProcessInstancesWithActiveUser(String aUserId, int aStartingIndex,
      int aPageSize, ProcessInstanceCriterion pagingCriterion);

  List<InternalProcessInstance> getParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate(
      String userId, Date currentDate, Date atRisk, int startingIndex, int pageSize,
      Set<ProcessDefinitionUUID> visibleProcessUUIDs);

  List<InternalProcessInstance> getParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate(
      String userId, Date currentDate, Date atRisk, int startingIndex, int pageSize,
      Set<ProcessDefinitionUUID> visibleProcessUUIDs, ProcessInstanceCriterion pagingCriterion);

  List<InternalProcessInstance> getParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate(
      String userId, Date currentDate, Date atRisk, int startingIndex, int pageSize);

  List<InternalProcessInstance> getParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate(
      String userId, Date currentDate, Date atRisk, int startingIndex, int pageSize,
      ProcessInstanceCriterion pagingCriterion);

  List<InternalProcessInstance> getParentProcessInstancesWithOverdueTasks(String userId, Date currentDate,
      int startingIndex, int pageSize, Set<ProcessDefinitionUUID> visibleProcessUUIDs);

  List<InternalProcessInstance> getParentProcessInstancesWithOverdueTasks(String userId, Date currentDate,
      int startingIndex, int pageSize, Set<ProcessDefinitionUUID> visibleProcessUUIDs,
      ProcessInstanceCriterion pagingCriterion);

  List<InternalProcessInstance> getParentProcessInstancesWithOverdueTasks(String userId, Date currentDate,
      int startingIndex, int pageSize);

  List<InternalProcessInstance> getParentProcessInstancesWithOverdueTasks(String userId, Date currentDate,
      int startingIndex, int pageSize, ProcessInstanceCriterion pagingCriterion);

  List<InternalProcessInstance> getParentProcessInstancesWithInvolvedUser(String aUserId, int aStartingIndex,
      int aPageSize, Set<ProcessDefinitionUUID> aVisibleProcessUUIDs);

  List<InternalProcessInstance> getParentProcessInstancesWithInvolvedUser(String aUserId, int aStartingIndex,
      int aPageSize, Set<ProcessDefinitionUUID> aVisibleProcessUUIDs, ProcessInstanceCriterion pagingCriterion);

  List<InternalProcessInstance> getParentProcessInstancesWithInvolvedUser(String aUserId, int aStartingIndex,
      int aPageSize);

  List<InternalProcessInstance> getParentProcessInstancesWithInvolvedUser(String aUserId, int aStartingIndex,
      int aPageSize, ProcessInstanceCriterion pagingCriterion);

  Integer getNumberOfParentProcessInstancesWithActiveUser(String userId, Set<ProcessDefinitionUUID> visibleProcessUUIDs);

  Integer getNumberOfParentProcessInstancesWithActiveUser(String userId);

  Integer getNumberOfParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate(String userId,
      Date currentDate, Date atRisk, Set<ProcessDefinitionUUID> visibleProcessUUIDs);

  Integer getNumberOfParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate(String userId,
      Date currentDate, Date atRisk);

  Integer getNumberOfParentProcessInstancesWithOverdueTasks(String userId, Date currentDate,
      Set<ProcessDefinitionUUID> visibleProcessUUIDs);

  Integer getNumberOfParentProcessInstancesWithOverdueTasks(String userId, Date currentDate);

  Integer getNumberOfParentProcessInstancesWithInvolvedUser(String userId,
      Set<ProcessDefinitionUUID> visibleProcessUUIDs);

  Integer getNumberOfParentProcessInstancesWithInvolvedUser(String userId);

  Integer getNumberOfParentProcessInstancesWithStartedBy(String userId, Set<ProcessDefinitionUUID> visibleProcessUUIDs);

  Integer getNumberOfParentProcessInstancesWithStartedBy(String userId);

  /**
   * ACTIVITY INSTANCE
   */
  InternalActivityInstance getActivityInstance(ProcessInstanceUUID instanceUUID, String activityId, String iterationId,
      String activityInstanceId, String loopId);

  Set<InternalActivityInstance> getActivityInstances(ProcessInstanceUUID instanceUUID);

  List<InternalActivityInstance> getActivityInstances(ProcessInstanceUUID instanceUUID, int fromIndex, int pageSize,
      ActivityInstanceCriterion pagingCriterion);

  InternalActivityInstance getActivityInstance(ActivityInstanceUUID activityInstanceUUID);

  List<InternalActivityInstance> getActivityInstancesFromRoot(ProcessInstanceUUID rootInstanceUUID);

  List<InternalActivityInstance> getActivityInstancesFromRoot(Set<ProcessInstanceUUID> rootInstanceUUIDs);

  List<InternalActivityInstance> getActivityInstancesFromRoot(Set<ProcessInstanceUUID> rootInstanceUUIDs,
      ActivityState state);

  Map<ProcessInstanceUUID, InternalActivityInstance> getLastUpdatedActivityInstanceFromRoot(
      Set<ProcessInstanceUUID> rootInstanceUUIDs, boolean considerSystemTaks);

  Set<InternalActivityInstance> getActivityInstances(ProcessInstanceUUID instanceUUID, String activityName);

  Set<InternalActivityInstance> getActivityInstances(ProcessInstanceUUID instanceUUID, String activityName,
      String iterationId);

  /**
   * PROCESS DEFINITION
   */
  boolean processExists(ProcessDefinitionUUID definitionUUID);

  Set<InternalProcessDefinition> getProcesses();

  Set<InternalProcessDefinition> getProcesses(String processId);

  Set<InternalProcessDefinition> getProcesses(ProcessState processState);

  Set<InternalProcessDefinition> getProcesses(String processId, ProcessState processState);

  InternalProcessDefinition getProcess(ProcessDefinitionUUID processUUID);

  InternalProcessDefinition getProcess(String processId, String version);

  String getLastProcessVersion(String processName);

  InternalProcessDefinition getLastProcess(String processId, ProcessState processState);

  InternalProcessDefinition getLastProcess(Set<ProcessDefinitionUUID> definitionUUIDs, ProcessState processState);

  List<InternalProcessDefinition> getProcesses(int fromIndex, int pageSize);

  List<InternalProcessDefinition> getProcesses(int fromIndex, int pageSize, ProcessDefinitionCriterion pagingCriterion);

  Set<InternalProcessDefinition> getProcesses(Set<ProcessDefinitionUUID> definitionUUIDs);

  Set<InternalProcessDefinition> getProcesses(Set<ProcessDefinitionUUID> definitionUUIDs, ProcessState processState);

  List<InternalProcessDefinition> getProcesses(Set<ProcessDefinitionUUID> definitionUUIDs, int fromIndex, int pageSize);

  List<InternalProcessDefinition> getProcesses(Set<ProcessDefinitionUUID> definitionUUIDs, int fromIndex, int pageSize,
      ProcessDefinitionCriterion pagingCriterion);

  /**
   * ACTIVITY DEFINITION
   */
  InternalActivityDefinition getActivityDefinition(ActivityDefinitionUUID activityDefinitionUUID);

  ActivityState getActivityInstanceState(ActivityInstanceUUID activityInstanceUUID);

  Set<ActivityDefinitionUUID> getProcessTaskUUIDs(ProcessDefinitionUUID definitionUUID);

  /**
   * WEB
   */
  Set<Category> getCategories(Collection<String> names);

  Set<Category> getAllCategories();

  Set<Category> getAllCategoriesExcept(Set<String> uuids);

  Set<CategoryImpl> getCategoriesByUUIDs(Set<CategoryUUID> uuids);

  CategoryImpl getCategoryByUUID(String uuid);

  Set<ProcessDefinitionUUID> getProcessUUIDsFromCategory(String category);

  /**
   * METADATA
   */
  MetaData getMetaData(String key);

  Set<InternalProcessInstance> getParentInstances();

  /**
   * BAM
   */
  int getNumberOfOpenSteps();

  int getNumberOfOverdueSteps(Date currentDate);

  int getNumberOfStepsAtRisk(Date currentDate, Date atRisk);

  int getNumberOfUserOpenSteps(String userId);

  int getNumberOfUserOverdueSteps(String userId, Date currentDate);

  int getNumberOfUserStepsAtRisk(String userId, Date currentDate, Date atRisk);

  List<Integer> getNumberOfFinishedCasesPerDay(Date since, Date to);

  List<Integer> getNumberOfExecutingCasesPerDay(Date since, Date to);

  List<Integer> getNumberOfOpenStepsPerDay(Date since, Date to);

  public int getNumberOfFinishedSteps(int priority, Date since);

  public int getNumberOfOpenSteps(int priority);

  public int getNumberOfUserFinishedSteps(String userId, int priority, Date since);

  public int getNumberOfUserOpenSteps(String userId, int priority);

  int getNumberOfParentProcessInstances();

  int getNumberOfParentProcessInstances(Set<ProcessDefinitionUUID> definitionUUIDs);

  int getNumberOfProcessInstances();

  int getNumberOfProcessInstances(Set<ProcessDefinitionUUID> definitionUUIDs);

  int getNumberOfProcesses();

  List<Long> getProcessInstancesDuration(Date since, Date until);

  List<Long> getProcessInstancesDuration(ProcessDefinitionUUID processUUID, Date since, Date until);

  List<Long> getProcessInstancesDurationFromProcessUUIDs(Set<ProcessDefinitionUUID> processUUIDs, Date since, Date until);

  List<Long> getActivityInstancesExecutionTime(Date since, Date until);

  List<Long> getActivityInstancesExecutionTime(ProcessDefinitionUUID processUUID, Date since, Date until);

  List<Long> getActivityInstancesExecutionTimeFromProcessUUIDs(Set<ProcessDefinitionUUID> processUUIDs, Date since,
      Date until);

  List<Long> getActivityInstancesExecutionTime(ActivityDefinitionUUID activityUUID, Date since, Date until);

  List<Long> getActivityInstancesExecutionTimeFromActivityUUIDs(Set<ActivityDefinitionUUID> activityUUIDs, Date since,
      Date until);

  List<Long> getTaskInstancesWaitingTime(Date since, Date until);

  List<Long> getTaskInstancesWaitingTime(ProcessDefinitionUUID processUUID, Date since, Date until);

  List<Long> getTaskInstancesWaitingTimeFromProcessUUIDs(Set<ProcessDefinitionUUID> processUUIDs, Date since, Date until);

  List<Long> getTaskInstancesWaitingTime(ActivityDefinitionUUID taskUUID, Date since, Date until);

  List<Long> getTaskInstancesWaitingTimeFromTasksUUIDs(Set<ActivityDefinitionUUID> tasksUUIDs, Date since, Date until);

  List<Long> getTaskInstancesWaitingTimeOfUser(String username, Date since, Date until);

  List<Long> getTaskInstancesWaitingTimeOfUser(String username, ProcessDefinitionUUID processUUID, Date since,
      Date until);

  List<Long> getTaskInstancesWaitingTimeOfUserFromProcessUUIDs(String username,
      Set<ProcessDefinitionUUID> processUUIDs, Date since, Date until);

  List<Long> getTaskInstancesWaitingTimeOfUser(String username, ActivityDefinitionUUID taskUUID, Date since, Date until);

  List<Long> getTaskInstancesWaitingTimeOfUserFromTaskUUIDs(String username, Set<ActivityDefinitionUUID> taskUUIDs,
      Date since, Date until);

  List<Long> getActivityInstancesDuration(Date since, Date until);

  List<Long> getActivityInstancesDuration(ProcessDefinitionUUID processUUID, Date since, Date until);

  List<Long> getActivityInstancesDurationFromProcessUUIDs(Set<ProcessDefinitionUUID> processUUIDs, Date since,
      Date until);

  List<Long> getActivityInstancesDuration(ActivityDefinitionUUID activityUUID, Date since, Date until);

  List<Long> getActivityInstancesDurationFromActivityUUIDs(Set<ActivityDefinitionUUID> activityUUIDs, Date since,
      Date until);

  List<Long> getActivityInstancesDurationByActivityType(Type activityType, Date since, Date until);

  List<Long> getActivityInstancesDurationByActivityType(Type activityType, ProcessDefinitionUUID processUUID,
      Date since, Date until);

  List<Long> getActivityInstancesDurationByActivityTypeFromProcessUUIDs(Type activityType,
      Set<ProcessDefinitionUUID> processUUIDs, Date since, Date until);

  long getNumberOfCreatedProcessInstances(Date since, Date until);

  long getNumberOfCreatedProcessInstances(ProcessDefinitionUUID processUUID, Date since, Date until);

  long getNumberOfCreatedActivityInstances(Date since, Date until);

  long getNumberOfCreatedActivityInstances(ProcessDefinitionUUID processUUID, Date since, Date until);

  long getNumberOfCreatedActivityInstancesFromProcessUUIDs(Set<ProcessDefinitionUUID> processUUIDs, Date since,
      Date until);

  long getNumberOfCreatedActivityInstances(ActivityDefinitionUUID activityUUID, Date since, Date until);

  long getNumberOfCreatedActivityInstancesFromActivityUUIDs(Set<ActivityDefinitionUUID> activityUUIDs, Date since,
      Date until);

  long getNumberOfCreatedActivityInstancesByActivityType(Type activityType, Date since, Date until);

  long getNumberOfCreatedActivityInstancesByActivityType(Type activityType, ProcessDefinitionUUID processUUID,
      Date since, Date until);

  long getNumberOfCreatedActivityInstancesByActivityTypeFromProcessUUIDs(Type activityType,
      Set<ProcessDefinitionUUID> processUUIDs, Date since, Date until);

  List<InternalProcessDefinition> getProcessesExcept(Set<ProcessDefinitionUUID> processUUIDs, int fromIndex,
      int pageSize);

  List<InternalProcessDefinition> getProcessesExcept(Set<ProcessDefinitionUUID> processUUIDs, int fromIndex,
      int pageSize, ProcessDefinitionCriterion pagingCriterion);

  Set<ProcessDefinitionUUID> getAllProcessDefinitionUUIDsExcept(Set<ProcessDefinitionUUID> processUUIDs);

  Set<ProcessDefinitionUUID> getAllProcessDefinitionUUIDs();

  /*
   * Comments
   */
  int getNumberOfActivityInstanceComments(ActivityInstanceUUID activityUUID);

  Map<ActivityInstanceUUID, Integer> getNumberOfActivityInstanceComments(Set<ActivityInstanceUUID> activityUUIDs);

  int getNumberOfComments(ProcessInstanceUUID instanceUUID);

  List<Comment> getCommentFeed(ProcessInstanceUUID instanceUUID);

  List<Comment> getActivityInstanceCommentFeed(ActivityInstanceUUID activityUUID);

  int getNumberOfProcessInstanceComments(ProcessInstanceUUID instanceUUID);

  List<Comment> getProcessInstanceCommentFeed(ProcessInstanceUUID instanceUUID);

  /*
   * Search
   */
  int search(SearchQueryBuilder query, Class<?> indexClass);

  List<Object> search(SearchQueryBuilder query, int firstResult, int maxResults, Class<?> indexClass);

  boolean containsOtherActiveActivities(ProcessInstanceUUID instanceUUID, ActivityInstanceUUID activityUUID);
}
