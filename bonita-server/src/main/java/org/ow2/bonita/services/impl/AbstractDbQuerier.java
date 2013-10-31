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
 * Modified by Charles Souillard, Matthieu Chaffotte, Elias Ricken de Medeiros - BonitaSoft S.A.
 **/
package org.ow2.bonita.services.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ow2.bonita.facade.def.InternalActivityDefinition;
import org.ow2.bonita.facade.def.InternalProcessDefinition;
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
import org.ow2.bonita.persistence.QuerierDbSession;
import org.ow2.bonita.runtime.model.Execution;
import org.ow2.bonita.search.SearchQueryBuilder;
import org.ow2.bonita.services.Querier;
import org.ow2.bonita.util.EnvTool;
import org.ow2.bonita.util.Misc;

public class AbstractDbQuerier implements Querier {

  private final String persistenceServiceName;

  public AbstractDbQuerier(final String persistenceServiceName) {
    super();
    this.persistenceServiceName = persistenceServiceName;
  }

  protected String getPersistenceServiceName() {
    return persistenceServiceName;
  }

  protected QuerierDbSession getDbSession() {
    return EnvTool.getQuerierDbSession(persistenceServiceName);
  }

  @Override
  public int getNumberOfProcesses() {
    return getDbSession().getNumberOfProcesses();
  }

  @Override
  public int getNumberOfParentProcessInstances() {
    return getDbSession().getNumberOfParentProcessInstances();
  }

  @Override
  public int getNumberOfProcessInstances() {
    return getDbSession().getNumberOfProcessInstances();
  }

  @Override
  public InternalActivityDefinition getActivity(final ActivityDefinitionUUID activityDefinitionUUID) {
    Misc.checkArgsNotNull(activityDefinitionUUID);
    return getDbSession().getActivityDefinition(activityDefinitionUUID);
  }

  @Override
  public Set<InternalActivityInstance> getActivityInstances(final ProcessInstanceUUID instanceUUID,
      final String activityName) {
    Misc.checkArgsNotNull(instanceUUID, activityName);
    final Set<InternalActivityInstance> activityInstances = getDbSession().getActivityInstances(instanceUUID,
        activityName);
    if (activityInstances != null) {
      return activityInstances;
    }
    return Collections.emptySet();
  }

  @Override
  public Set<InternalActivityInstance> getActivityInstances(final ProcessInstanceUUID instanceUUID,
      final String activityName, final String iterationId) {
    Misc.checkArgsNotNull(instanceUUID, activityName, iterationId);
    final Set<InternalActivityInstance> activityInstances = getDbSession().getActivityInstances(instanceUUID,
        activityName, iterationId);
    if (activityInstances != null) {
      return activityInstances;
    }
    return Collections.emptySet();
  }

  @Override
  public InternalActivityInstance getActivityInstance(final ProcessInstanceUUID instanceUUID,
      final String activityName, final String iterationId, final String activityInstanceId, final String loopId) {
    return getDbSession().getActivityInstance(instanceUUID, activityName, iterationId, activityInstanceId, loopId);
  }

  @Override
  public ActivityState getActivityInstanceState(final ActivityInstanceUUID activityInstanceUUID) {
    Misc.checkArgsNotNull(activityInstanceUUID);
    return getDbSession().getActivityInstanceState(activityInstanceUUID);
  }

  @Override
  public InternalActivityInstance getActivityInstance(final ActivityInstanceUUID activityInstanceUUID) {
    Misc.checkArgsNotNull(activityInstanceUUID);
    return getDbSession().getActivityInstance(activityInstanceUUID);
  }

  @Override
  public Set<InternalActivityInstance> getActivityInstances(final ProcessInstanceUUID instanceUUID) {
    Misc.checkArgsNotNull(instanceUUID);
    final Set<InternalActivityInstance> activityInstances = getDbSession().getActivityInstances(instanceUUID);
    if (activityInstances != null) {
      return activityInstances;
    }
    return Collections.emptySet();
  }

  @Override
  public List<InternalActivityInstance> getActivityInstances(final ProcessInstanceUUID instanceUUID,
      final int fromIndex, final int pageSize, final ActivityInstanceCriterion pagingCriterion) {
    Misc.checkArgsNotNull(instanceUUID);
    final List<InternalActivityInstance> activityInstances = getDbSession().getActivityInstances(instanceUUID,
        fromIndex, pageSize, pagingCriterion);
    if (activityInstances != null) {
      return activityInstances;
    }
    return Collections.emptyList();
  }

  @Override
  public List<InternalActivityInstance> getActivityInstancesFromRoot(final ProcessInstanceUUID rootInstanceUUID) {
    Misc.checkArgsNotNull(rootInstanceUUID);
    return getDbSession().getActivityInstancesFromRoot(rootInstanceUUID);
  }

  @Override
  public List<InternalActivityInstance> getActivityInstancesFromRoot(final Set<ProcessInstanceUUID> rootInstanceUUIDs) {
    Misc.checkArgsNotNull(rootInstanceUUIDs);
    return getDbSession().getActivityInstancesFromRoot(rootInstanceUUIDs);
  }

  @Override
  public List<InternalActivityInstance> getActivityInstancesFromRoot(final Set<ProcessInstanceUUID> rootInstanceUUIDs,
      final ActivityState state) {
    Misc.checkArgsNotNull(rootInstanceUUIDs, state);
    return getDbSession().getActivityInstancesFromRoot(rootInstanceUUIDs, state);
  }

  @Override
  public Map<ProcessInstanceUUID, InternalActivityInstance> getLastUpdatedActivityInstanceFromRoot(
      final Set<ProcessInstanceUUID> rootInstanceUUIDs, final boolean considerSystemTaks) {
    Misc.checkArgsNotNull(rootInstanceUUIDs);
    return getDbSession().getLastUpdatedActivityInstanceFromRoot(rootInstanceUUIDs, considerSystemTaks);
  }

  @Override
  public InternalProcessInstance getProcessInstance(final ProcessInstanceUUID instanceUUID) {
    Misc.checkArgsNotNull(instanceUUID);
    return getDbSession().getProcessInstance(instanceUUID);
  }

  @Override
  public Set<ProcessInstanceUUID> getParentInstancesUUIDs() {
    final Set<InternalProcessInstance> parentInstances = getParentInstances();
    final Set<ProcessInstanceUUID> result = new HashSet<ProcessInstanceUUID>();
    for (final InternalProcessInstance instance : parentInstances) {
      result.add(instance.getUUID());
    }
    return result;
  }

  @Override
  public Set<InternalProcessInstance> getProcessInstances() {
    final Set<InternalProcessInstance> dbInstances = getDbSession().getProcessInstances();
    if (dbInstances == null) {
      return Collections.emptySet();
    }
    return dbInstances;
  }

  @Override
  public List<InternalProcessInstance> getProcessInstances(final Collection<ProcessInstanceUUID> instanceUUIDs,
      final int fromIndex, final int pageSize) {
    final List<InternalProcessInstance> dbInstances = getDbSession().getProcessInstances(instanceUUIDs, fromIndex,
        pageSize);
    if (dbInstances == null) {
      return Collections.emptyList();
    }
    return dbInstances;
  }

  @Override
  public List<InternalProcessInstance> getProcessInstancesWithInstanceUUIDs(
      final Set<ProcessInstanceUUID> instanceUUIDs, final int fromIndex, final int pageSize,
      final ProcessInstanceCriterion pagingCriterion) {
    final List<InternalProcessInstance> dbInstances = getDbSession().getProcessInstancesWithInstanceUUIDs(
        instanceUUIDs, fromIndex, pageSize, pagingCriterion);
    if (dbInstances == null) {
      return Collections.emptyList();
    }
    return dbInstances;
  }

  @Override
  public List<InternalProcessInstance> getMostRecentProcessInstances(final int maxResults, final long time) {
    return getDbSession().getMostRecentProcessInstances(maxResults, time);
  }

  @Override
  public List<InternalProcessInstance> getMostRecentProcessInstances(final int maxResults, final long time,
      final ProcessInstanceCriterion pagingCriterion) {
    return getDbSession().getMostRecentProcessInstances(maxResults, time, pagingCriterion);
  }

  @Override
  public List<InternalProcessInstance> getMostRecentParentProcessInstances(final int maxResults, final long time) {
    return getDbSession().getMostRecentParentProcessInstances(maxResults, time);
  }

  @Override
  public List<InternalProcessInstance> getMostRecentParentProcessInstances(final int maxResults, final long time,
      final ProcessInstanceCriterion pagingCriterion) {
    return getDbSession().getMostRecentParentProcessInstances(maxResults, time, pagingCriterion);
  }

  @Override
  public List<InternalProcessInstance> getMostRecentMatchingProcessInstances(
      final Collection<ProcessInstanceUUID> instanceUUIDs, final int maxResults, final long time) {
    return getDbSession().getMostRecentMatchingProcessInstances(instanceUUIDs, maxResults, time);
  }

  @Override
  public List<InternalProcessInstance> getMostRecentMatchingProcessInstances(
      final Set<ProcessInstanceUUID> instanceUUIDs, final int maxResults, final long time,
      final ProcessInstanceCriterion pagingCriterion) {
    return getDbSession().getMostRecentMatchingProcessInstances(instanceUUIDs, maxResults, time, pagingCriterion);
  }

  @Override
  public List<InternalProcessInstance> getMostRecentProcessesProcessInstances(
      final Collection<ProcessDefinitionUUID> definitionUUIDs, final int maxResults, final long time) {
    return getDbSession().getMostRecentProcessesProcessInstances(definitionUUIDs, maxResults, time);
  }

  @Override
  public List<InternalProcessInstance> getMostRecentProcessesProcessInstances(
      final Collection<ProcessDefinitionUUID> definitionUUIDs, final int maxResults, final long time,
      final ProcessInstanceCriterion pagingCriterion) {
    return getDbSession().getMostRecentProcessesProcessInstances(definitionUUIDs, maxResults, time, pagingCriterion);
  }

  @Override
  public List<InternalProcessDefinition> getProcesses(final int fromIndex, final int pageSize) {
    return getDbSession().getProcesses(fromIndex, pageSize);
  }

  @Override
  public List<InternalProcessDefinition> getProcesses(final int fromIndex, final int pageSize,
      final ProcessDefinitionCriterion pagingCriterion) {
    return getDbSession().getProcesses(fromIndex, pageSize, pagingCriterion);
  }

  @Override
  public List<InternalProcessInstance> getProcessInstances(final int fromIndex, final int pageSize) {
    return getDbSession().getProcessInstances(fromIndex, pageSize);
  }

  @Override
  public List<InternalProcessInstance> getProcessInstances(final int fromIndex, final int pageSize,
      final ProcessInstanceCriterion pagingCriterion) {
    return getDbSession().getProcessInstances(fromIndex, pageSize, pagingCriterion);
  }

  @Override
  public List<InternalProcessInstance> getParentProcessInstances(final ProcessDefinitionUUID definitionUUID,
      final int fromIndex, final int pageSize) {
    return getDbSession().getParentProcessInstances(definitionUUID, fromIndex, pageSize);
  }

  @Override
  public List<InternalProcessInstance> getParentProcessInstances(final int fromIndex, final int pageSize) {
    return getDbSession().getParentProcessInstances(fromIndex, pageSize);
  }

  @Override
  public List<InternalProcessInstance> getParentProcessInstances(final int fromIndex, final int pageSize,
      final ProcessInstanceCriterion paginCriterion) {
    return getDbSession().getParentProcessInstances(fromIndex, pageSize, paginCriterion);
  }

  @Override
  public List<InternalProcessInstance> getParentProcessInstances(final Set<ProcessDefinitionUUID> processUUIDs,
      final int fromIndex, final int pageSize, final ProcessInstanceCriterion pagingCriterion) {
    return getDbSession().getParentProcessInstances(processUUIDs, fromIndex, pageSize, pagingCriterion);
  }

  @Override
  public List<InternalProcessInstance> getParentProcessInstancesExcept(final Set<ProcessDefinitionUUID> exceptions,
      final int fromIndex, final int pageSize, final ProcessInstanceCriterion pagingCriterion) {
    return getDbSession().getParentProcessInstancesExcept(exceptions, fromIndex, pageSize, pagingCriterion);
  }

  @Override
  public Set<InternalProcessInstance> getParentInstances() {
    final Set<InternalProcessInstance> dbInstances = getDbSession().getParentInstances();
    if (dbInstances == null) {
      return Collections.emptySet();
    }
    return dbInstances;
  }

  @Override
  public Set<InternalProcessInstance> getProcessInstances(final Collection<ProcessInstanceUUID> instanceUUIDs) {
    if (instanceUUIDs == null || instanceUUIDs.isEmpty()) {
      return Collections.emptySet();
    }
    final Set<InternalProcessInstance> dbInstances = getDbSession().getProcessInstances(instanceUUIDs);
    if (dbInstances == null) {
      return Collections.emptySet();
    }
    return dbInstances;
  }

  @Override
  public Set<InternalProcessInstance> getProcessInstances(final ProcessDefinitionUUID processUUID,
      final InstanceState instanceState) {
    Misc.checkArgsNotNull(processUUID);
    final Set<InternalProcessInstance> dbInstances = getDbSession().getProcessInstances(processUUID, instanceState);
    if (dbInstances == null) {
      return Collections.emptySet();
    }
    return dbInstances;
  }

  private Execution getExecOnNode(final Execution exec, final ActivityInstanceUUID activityInstanceUUID) {
    Misc.checkArgsNotNull(exec, activityInstanceUUID);
    if (exec.getExecutions() == null || exec.getExecutions().isEmpty()) {
      if (exec.getNode() != null && exec.getActivityInstanceUUID() != null
          && exec.getActivityInstanceUUID().equals(activityInstanceUUID)) {
        return exec;
      }
    } else {
      for (final Execution child : exec.getExecutions()) {
        final Execution found = getExecOnNode(child, activityInstanceUUID);
        if (found != null) {
          return found;
        }
      }
    }
    return null;
  }

  @Override
  public Execution getExecutionOnActivity(final ProcessInstanceUUID instanceUUID,
      final ActivityInstanceUUID activityUUID) {
    Misc.checkArgsNotNull(instanceUUID, activityUUID);

    final InternalProcessInstance instance = getProcessInstance(instanceUUID);
    if (instance != null) {
      return getExecOnNode(instance.getRootExecution(), activityUUID);
    }
    return getDbSession().getExecutionPointingOnNode(activityUUID);
  }

  @Override
  public Set<Execution> getExecutions(final ProcessInstanceUUID instanceUUID) {
    Misc.checkArgsNotNull(instanceUUID);
    return getDbSession().getExecutions(instanceUUID);
  }

  @Override
  public Execution getExecutionWithEventUUID(final String eventUUID) {
    Misc.checkArgsNotNull(eventUUID);
    return getDbSession().getExecutionWithEventUUID(eventUUID);
  }

  @Override
  public Set<InternalProcessInstance> getUserInstances(final String userId) {
    final Set<InternalProcessInstance> dbInstances = getDbSession().getUserInstances(userId);
    if (dbInstances == null) {
      return Collections.emptySet();
    }
    return dbInstances;
  }

  @Override
  public Set<InternalProcessInstance> getUserInstances(final String userId, final Date minStartDate) {
    final Set<InternalProcessInstance> dbInstances = getDbSession().getUserInstances(userId, minStartDate);
    if (dbInstances == null) {
      return Collections.emptySet();
    }
    return dbInstances;
  }

  @Override
  public Set<InternalProcessInstance> getUserParentInstances(final String userId, final Date minStartDate) {
    final Set<InternalProcessInstance> dbInstances = getDbSession().getUserParentInstances(userId, minStartDate);
    if (dbInstances == null) {
      return Collections.emptySet();
    }
    return dbInstances;
  }

  @Override
  public Set<InternalProcessInstance> getUserInstancesExcept(final String userId, final Set<ProcessInstanceUUID> myCases) {
    final Set<InternalProcessInstance> dbInstances = getDbSession().getUserInstancesExcept(userId, myCases);
    if (dbInstances == null) {
      return Collections.emptySet();
    }
    return dbInstances;
  }

  @Override
  public Set<InternalProcessInstance> getProcessInstancesWithTaskState(final Collection<ActivityState> activityStates) {
    final Set<InternalProcessInstance> dbInstances = getDbSession().getProcessInstancesWithTaskState(activityStates);
    if (dbInstances == null) {
      return Collections.emptySet();
    }
    return dbInstances;
  }

  @Override
  public Set<InternalProcessInstance> getProcessInstancesWithInstanceStates(
      final Collection<InstanceState> instanceStates) {
    final Set<InternalProcessInstance> dbInstances = getDbSession().getProcessInstancesWithInstanceStates(
        instanceStates);
    if (dbInstances == null) {
      return Collections.emptySet();
    }
    return dbInstances;
  }

  @Override
  public Set<InternalProcessInstance> getProcessInstances(final ProcessDefinitionUUID processUUID) {
    Misc.checkArgsNotNull(processUUID);
    final Set<InternalProcessInstance> dbInstances = getDbSession().getProcessInstances(processUUID);
    if (dbInstances == null) {
      return Collections.emptySet();
    }
    return dbInstances;
  }

  @Override
  public InternalProcessDefinition getProcess(final String processId, final String version) {
    Misc.checkArgsNotNull(processId, version);
    return getDbSession().getProcess(processId, version);
  }

  @Override
  public InternalProcessDefinition getProcess(final ProcessDefinitionUUID processUUID) {
    Misc.checkArgsNotNull(processUUID);
    return getDbSession().getProcess(processUUID);
  }

  @Override
  public Set<InternalProcessDefinition> getProcesses() {
    final Set<InternalProcessDefinition> processes = getDbSession().getProcesses();
    if (processes == null) {
      return Collections.emptySet();
    }
    return processes;
  }

  @Override
  public Set<InternalProcessDefinition> getProcesses(final String processId) {
    Misc.checkArgsNotNull(processId);
    final Set<InternalProcessDefinition> processes = getDbSession().getProcesses(processId);
    if (processes == null) {
      return Collections.emptySet();
    }
    return processes;
  }

  @Override
  public Set<InternalProcessDefinition> getProcesses(final ProcessState processState) {
    Misc.checkArgsNotNull(processState);
    final Set<InternalProcessDefinition> processes = getDbSession().getProcesses(processState);
    if (processes == null) {
      return Collections.emptySet();
    }
    return processes;
  }

  @Override
  public Set<InternalProcessDefinition> getProcesses(final String processId, final ProcessState processState) {
    Misc.checkArgsNotNull(processId, processState);
    final Set<InternalProcessDefinition> processes = getDbSession().getProcesses(processId, processState);
    if (processes == null) {
      return Collections.emptySet();
    }
    return processes;
  }

  @Override
  public TaskInstance getTaskInstance(final ActivityInstanceUUID taskUUID) {
    Misc.checkArgsNotNull(taskUUID);
    return getDbSession().getTaskInstance(taskUUID);
  }

  @Override
  public Set<TaskInstance> getTaskInstances(final ProcessInstanceUUID instanceUUID) {
    Misc.checkArgsNotNull(instanceUUID);
    final Set<TaskInstance> tasks = getDbSession().getTaskInstances(instanceUUID);
    if (tasks == null) {
      return Collections.emptySet();
    }
    return tasks;
  }

  @Override
  public Set<TaskInstance> getTaskInstances(final ProcessInstanceUUID instanceUUID, final Set<String> taskNames) {
    Misc.checkArgsNotNull(instanceUUID);
    final Set<TaskInstance> tasks = getDbSession().getTaskInstances(instanceUUID, taskNames);
    if (tasks == null) {
      return Collections.emptySet();
    }
    return tasks;
  }

  @Override
  public Set<TaskInstance> getUserInstanceTasks(final String userId, final ProcessInstanceUUID instanceUUID,
      final ActivityState taskState) {
    Misc.checkArgsNotNull(userId, instanceUUID, taskState);
    final Set<TaskInstance> tasks = getDbSession().getUserInstanceTasks(userId, instanceUUID, taskState);
    if (tasks == null) {
      return Collections.emptySet();
    }
    return tasks;
  }

  @Override
  public TaskInstance getOneTask(final String userId, final ProcessDefinitionUUID processUUID,
      final ActivityState taskState) {
    return getDbSession().getOneTask(userId, processUUID, taskState);
  }

  @Override
  public TaskInstance getOneTask(final String userId, final ProcessInstanceUUID instanceUUID,
      final ActivityState taskState) {
    return getDbSession().getOneTask(userId, instanceUUID, taskState);
  }

  @Override
  public TaskInstance getOneTask(final String userId, final ActivityState taskState) {
    return getDbSession().getOneTask(userId, taskState);
  }

  @Override
  public Set<TaskInstance> getUserTasks(final String userId, final ActivityState taskState) {
    final Collection<ActivityState> taskStates = new HashSet<ActivityState>();
    taskStates.add(taskState);
    return getUserTasks(userId, taskStates);
  }

  @Override
  public Set<TaskInstance> getUserTasks(final String userId, final Collection<ActivityState> taskStates) {
    Misc.checkArgsNotNull(userId, taskStates);
    final Set<TaskInstance> tasks = getDbSession().getUserTasks(userId, taskStates);
    if (tasks == null) {
      return Collections.emptySet();
    }
    return tasks;
  }

  /*
   * SPECIFIC
   */
  @Override
  public String getLastProcessVersion(final String processName) {
    Misc.checkArgsNotNull(processName);
    return getDbSession().getLastProcessVersion(processName);
  }

  @Override
  public long getLastProcessInstanceNb(final ProcessDefinitionUUID processUUID) {
    Misc.checkArgsNotNull(processUUID);
    return getDbSession().getLastProcessInstanceNb(processUUID);
  }

  @Override
  public InternalProcessDefinition getLastDeployedProcess(final String processId, final ProcessState processState) {
    Misc.checkArgsNotNull(processId, processState);
    return getDbSession().getLastProcess(processId, processState);
  }

  @Override
  public List<Integer> getNumberOfFinishedCasesPerDay(final Date since, final Date to) {
    return getDbSession().getNumberOfFinishedCasesPerDay(since, to);
  }

  @Override
  public List<Integer> getNumberOfExecutingCasesPerDay(final Date since, final Date to) {
    return getDbSession().getNumberOfExecutingCasesPerDay(since, to);
  }

  @Override
  public int getNumberOfOpenSteps() {
    return getDbSession().getNumberOfOpenSteps();
  }

  @Override
  public List<Integer> getNumberOfOpenStepsPerDay(final Date since, final Date to) {
    return getDbSession().getNumberOfOpenStepsPerDay(since, to);
  }

  @Override
  public int getNumberOfOverdueSteps(final Date currentDate) {
    return getDbSession().getNumberOfOverdueSteps(currentDate);
  }

  @Override
  public int getNumberOfStepsAtRisk(final Date beginningOfTheDay, final Date atRisk) {
    return getDbSession().getNumberOfStepsAtRisk(beginningOfTheDay, atRisk);
  }

  @Override
  public int getNumberOfUserOpenSteps(final String userId) {
    return getDbSession().getNumberOfUserOpenSteps(userId);
  }

  @Override
  public int getNumberOfUserOverdueSteps(final String userId, final Date currentDate) {
    return getDbSession().getNumberOfUserOverdueSteps(userId, currentDate);
  }

  @Override
  public int getNumberOfUserStepsAtRisk(final String userId, final Date beginningOfTheDay, final Date atRisk) {
    return getDbSession().getNumberOfUserStepsAtRisk(userId, beginningOfTheDay, atRisk);
  }

  @Override
  public int getNumberOfFinishedSteps(final int priority, final Date since) {
    return getDbSession().getNumberOfFinishedSteps(priority, since);
  }

  @Override
  public int getNumberOfOpenSteps(final int priority) {
    return getDbSession().getNumberOfOpenSteps(priority);
  }

  @Override
  public int getNumberOfUserFinishedSteps(final String userId, final int priority, final Date since) {
    return getDbSession().getNumberOfUserFinishedSteps(userId, priority, since);
  }

  @Override
  public int getNumberOfUserOpenSteps(final String userId, final int priority) {
    return getDbSession().getNumberOfUserOpenSteps(userId, priority);
  }

  @Override
  public Set<InternalProcessDefinition> getProcesses(final Set<ProcessDefinitionUUID> definitionUUIDs) {
    return getDbSession().getProcesses(definitionUUIDs);
  }

  @Override
  public Set<InternalProcessDefinition> getProcesses(final Set<ProcessDefinitionUUID> definitionUUIDs,
      final ProcessState processState) {
    return getDbSession().getProcesses(definitionUUIDs, processState);
  }

  @Override
  public List<InternalProcessDefinition> getProcesses(final Set<ProcessDefinitionUUID> definitionUUIDs,
      final int fromIndex, final int pageSize) {
    return getDbSession().getProcesses(definitionUUIDs, fromIndex, pageSize);
  }

  @Override
  public List<InternalProcessDefinition> getProcesses(final Set<ProcessDefinitionUUID> definitionUUIDs,
      final int fromIndex, final int pageSize, final ProcessDefinitionCriterion pagingCriterion) {
    return getDbSession().getProcesses(definitionUUIDs, fromIndex, pageSize, pagingCriterion);
  }

  @Override
  public InternalProcessDefinition getLastDeployedProcess(final Set<ProcessDefinitionUUID> definitionUUIDs,
      final ProcessState processState) {
    Misc.checkArgsNotNull(processState);
    return getDbSession().getLastProcess(definitionUUIDs, processState);
  }

  @Override
  public Set<InternalProcessInstance> getProcessInstances(final Set<ProcessDefinitionUUID> definitionUUIDs) {
    return getDbSession().getProcessInstances(definitionUUIDs);
  }

  @Override
  public Set<InternalProcessInstance> getUserInstances(final String userId,
      final Set<ProcessDefinitionUUID> definitionUUIDs) {
    final Set<InternalProcessInstance> dbInstances = getDbSession().getUserInstances(userId, definitionUUIDs);
    if (dbInstances == null) {
      return Collections.emptySet();
    }
    return dbInstances;
  }

  @Override
  public int getNumberOfProcessInstances(final Set<ProcessDefinitionUUID> definitionUUIDs) {
    return getDbSession().getNumberOfProcessInstances(definitionUUIDs);
  }

  @Override
  public int getNumberOfParentProcessInstances(final Set<ProcessDefinitionUUID> definitionUUIDs) {
    return getDbSession().getNumberOfParentProcessInstances(definitionUUIDs);
  }

  @Override
  public Set<InternalProcessInstance> getProcessInstancesWithTaskState(final Collection<ActivityState> activityStates,
      final Set<ProcessDefinitionUUID> definitionUUIDs) {
    final Set<InternalProcessInstance> dbInstances = getDbSession().getProcessInstancesWithTaskState(activityStates,
        definitionUUIDs);
    if (dbInstances == null) {
      return Collections.emptySet();
    }
    return dbInstances;
  }

  @Override
  public Set<InternalProcessInstance> getProcessInstancesWithInstanceStates(
      final Collection<InstanceState> instanceStates, final Set<ProcessDefinitionUUID> visibleProcessUUIDs) {
    final Set<InternalProcessInstance> dbInstances = getDbSession().getProcessInstancesWithInstanceStates(
        instanceStates, visibleProcessUUIDs);
    if (dbInstances == null) {
      return Collections.emptySet();
    }
    return dbInstances;
  }

  @Override
  public TaskInstance getOneTask(final String userId, final ActivityState taskState,
      final Set<ProcessDefinitionUUID> definitionUUIDs) {
    return getDbSession().getOneTask(userId, taskState, definitionUUIDs);
  }

  @Override
  public Set<TaskInstance> getUserTasks(final String userId, final ActivityState taskState,
      final Set<ProcessDefinitionUUID> definitionUUIDs) {
    final Collection<ActivityState> taskStates = new HashSet<ActivityState>();
    taskStates.add(taskState);
    final Set<TaskInstance> userTasks = getUserTasks(userId, taskStates);
    final Set<TaskInstance> filteredTasks = new HashSet<TaskInstance>();
    for (final TaskInstance taskInstance : userTasks) {
      if (definitionUUIDs.contains(taskInstance.getProcessDefinitionUUID())) {
        filteredTasks.add(taskInstance);
      }
    }
    return filteredTasks;
  }

  @Override
  public List<InternalProcessInstance> getProcessInstances(final Set<ProcessDefinitionUUID> definitionUUIDs,
      final int fromIndex, final int pageSize) {
    return getDbSession().getProcessInstances(definitionUUIDs, fromIndex, pageSize);
  }

  @Override
  public List<InternalProcessInstance> getProcessInstances(final Set<ProcessDefinitionUUID> definitionUUIDs,
      final int fromIndex, final int pageSize, final ProcessInstanceCriterion pagingCriterion) {
    return getDbSession().getProcessInstances(definitionUUIDs, fromIndex, pageSize, pagingCriterion);
  }

  @Override
  public List<InternalProcessDefinition> getProcessesExcept(final Set<ProcessDefinitionUUID> processUUIDs,
      final int fromIndex, final int pageSize) {
    return getDbSession().getProcessesExcept(processUUIDs, fromIndex, pageSize);
  }

  @Override
  public List<InternalProcessDefinition> getProcessesExcept(final Set<ProcessDefinitionUUID> processUUIDs,
      final int fromIndex, final int pageSize, final ProcessDefinitionCriterion pagingCriterion) {
    return getDbSession().getProcessesExcept(processUUIDs, fromIndex, pageSize, pagingCriterion);
  }

  @Override
  public int getNumberOfActivityInstanceComments(final ActivityInstanceUUID activityUUID) {
    return getDbSession().getNumberOfActivityInstanceComments(activityUUID);
  }

  @Override
  public Map<ActivityInstanceUUID, Integer> getNumberOfActivityInstanceComments(
      final Set<ActivityInstanceUUID> activityUUIDs) {
    return getDbSession().getNumberOfActivityInstanceComments(activityUUIDs);
  }

  @Override
  public int getNumberOfComments(final ProcessInstanceUUID instanceUUID) {
    return getDbSession().getNumberOfComments(instanceUUID);
  }

  @Override
  public List<Comment> getCommentFeed(final ProcessInstanceUUID instanceUUID) {
    return getDbSession().getCommentFeed(instanceUUID);
  }

  @Override
  public List<Comment> getActivityInstanceCommentFeed(final ActivityInstanceUUID activityUUID) {
    return getDbSession().getActivityInstanceCommentFeed(activityUUID);
  }

  @Override
  public Set<ProcessDefinitionUUID> getAllProcessDefinitionUUIDsExcept(final Set<ProcessDefinitionUUID> processUUIDs) {
    return getDbSession().getAllProcessDefinitionUUIDsExcept(processUUIDs);
  }

  @Override
  public Set<ProcessDefinitionUUID> getAllProcessDefinitionUUIDs() {
    return getDbSession().getAllProcessDefinitionUUIDs();
  }

  @Override
  public int getNumberOfProcessInstanceComments(final ProcessInstanceUUID instanceUUID) {
    return getDbSession().getNumberOfProcessInstanceComments(instanceUUID);
  }

  @Override
  public List<Comment> getProcessInstanceCommentFeed(final ProcessInstanceUUID instanceUUID) {
    return getDbSession().getProcessInstanceCommentFeed(instanceUUID);
  }

  @Override
  public List<InternalProcessInstance> getParentUserInstances(final String userId, final int startingIndex,
      final int pageSize) {
    final List<InternalProcessInstance> dbInstances = getDbSession().getParentUserInstances(userId, startingIndex,
        pageSize);
    if (dbInstances == null) {
      return Collections.emptyList();
    }
    return dbInstances;
  }

  @Override
  public List<InternalProcessInstance> getParentUserInstances(final String userId, final int startingIndex,
      final int pageSize, final ProcessInstanceCriterion pagingCriterion) {
    final List<InternalProcessInstance> dbInstances = getDbSession().getParentUserInstances(userId, startingIndex,
        pageSize, pagingCriterion);
    if (dbInstances == null) {
      return Collections.emptyList();
    }
    return dbInstances;
  }

  @Override
  public List<InternalProcessInstance> getParentUserInstances(final String userId, final int startingIndex,
      final int pageSize, final Set<ProcessDefinitionUUID> definitionUUIDs) {
    final List<InternalProcessInstance> dbInstances = getDbSession().getParentUserInstances(userId, startingIndex,
        pageSize, definitionUUIDs);
    if (dbInstances == null) {
      return Collections.emptyList();
    }
    return dbInstances;
  }

  @Override
  public List<InternalProcessInstance> getParentUserInstances(final String userId, final int startingIndex,
      final int pageSize, final Set<ProcessDefinitionUUID> definitionUUIDs,
      final ProcessInstanceCriterion pagingCriterion) {

    final List<InternalProcessInstance> dbInstances = getDbSession().getParentUserInstances(userId, startingIndex,
        pageSize, definitionUUIDs, pagingCriterion);
    if (dbInstances == null) {
      return Collections.emptyList();
    }
    return dbInstances;
  }

  @Override
  public List<InternalProcessInstance> getParentProcessInstancesWithActiveUser(final String userId,
      final int startingIndex, final int pageSize, final Set<ProcessDefinitionUUID> visibleProcessUUIDs) {
    final List<InternalProcessInstance> dbInstances = getDbSession().getParentProcessInstancesWithActiveUser(userId,
        startingIndex, pageSize, visibleProcessUUIDs);
    if (dbInstances == null) {
      return Collections.emptyList();
    }
    return dbInstances;
  }

  @Override
  public List<InternalProcessInstance> getParentProcessInstancesWithActiveUser(final String userId,
      final int startingIndex, final int pageSize, final Set<ProcessDefinitionUUID> visibleProcessUUIDs,
      final ProcessInstanceCriterion pagingCriterion) {

    final List<InternalProcessInstance> dbInstances = getDbSession().getParentProcessInstancesWithActiveUser(userId,
        startingIndex, pageSize, visibleProcessUUIDs, pagingCriterion);
    if (dbInstances == null) {
      return Collections.emptyList();
    }
    return dbInstances;
  }

  @Override
  public List<InternalProcessInstance> getParentProcessInstancesWithActiveUser(final String userId,
      final int startingIndex, final int pageSize) {
    final List<InternalProcessInstance> dbInstances = getDbSession().getParentProcessInstancesWithActiveUser(userId,
        startingIndex, pageSize);
    if (dbInstances == null) {
      return Collections.emptyList();
    }
    return dbInstances;
  }

  @Override
  public List<InternalProcessInstance> getParentProcessInstancesWithActiveUser(final String userId,
      final int startingIndex, final int pageSize, final ProcessInstanceCriterion pagingCriterion) {
    final List<InternalProcessInstance> dbInstances = getDbSession().getParentProcessInstancesWithActiveUser(userId,
        startingIndex, pageSize, pagingCriterion);
    if (dbInstances == null) {
      return Collections.emptyList();
    }
    return dbInstances;
  }

  @Override
  public List<InternalProcessInstance> getParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate(
      final String userId, final Date currentDate, final Date atRisk, final int startingIndex, final int pageSize,
      final Set<ProcessDefinitionUUID> visibleProcessUUIDs) {
    final List<InternalProcessInstance> dbInstances = getDbSession()
        .getParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate(userId, currentDate, atRisk,
            startingIndex, pageSize, visibleProcessUUIDs);
    if (dbInstances == null) {
      return Collections.emptyList();
    }
    return dbInstances;
  }

  @Override
  public List<InternalProcessInstance> getParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate(
      final String userId, final Date currentDate, final Date atRisk, final int startingIndex, final int pageSize,
      final Set<ProcessDefinitionUUID> visibleProcessUUIDs, final ProcessInstanceCriterion pagingCriterion) {
    final List<InternalProcessInstance> dbInstances = getDbSession()
        .getParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate(userId, currentDate, atRisk,
            startingIndex, pageSize, visibleProcessUUIDs, pagingCriterion);
    if (dbInstances == null) {
      return Collections.emptyList();
    }
    return dbInstances;
  }

  @Override
  public List<InternalProcessInstance> getParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate(
      final String userId, final Date currentDate, final Date atRisk, final int startingIndex, final int pageSize) {
    final List<InternalProcessInstance> dbInstances = getDbSession()
        .getParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate(userId, currentDate, atRisk,
            startingIndex, pageSize);
    if (dbInstances == null) {
      return Collections.emptyList();
    }
    return dbInstances;
  }

  @Override
  public List<InternalProcessInstance> getParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate(
      final String userId, final Date currentDate, final Date atRisk, final int startingIndex, final int pageSize,
      final ProcessInstanceCriterion pagingCriterion) {
    final List<InternalProcessInstance> dbInstances = getDbSession()
        .getParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate(userId, currentDate, atRisk,
            startingIndex, pageSize, pagingCriterion);
    if (dbInstances == null) {
      return Collections.emptyList();
    }
    return dbInstances;
  }

  @Override
  public List<InternalProcessInstance> getParentProcessInstancesWithOverdueTasks(final String userId,
      final Date currentDate, final int startingIndex, final int pageSize,
      final Set<ProcessDefinitionUUID> visibleProcessUUIDs) {
    final List<InternalProcessInstance> dbInstances = getDbSession().getParentProcessInstancesWithOverdueTasks(userId,
        currentDate, startingIndex, pageSize, visibleProcessUUIDs);
    if (dbInstances == null) {
      return Collections.emptyList();
    }
    return dbInstances;
  }

  @Override
  public List<InternalProcessInstance> getParentProcessInstancesWithOverdueTasks(final String userId,
      final Date currentDate, final int startingIndex, final int pageSize,
      final Set<ProcessDefinitionUUID> visibleProcessUUIDs, final ProcessInstanceCriterion pagingCriterion) {
    final List<InternalProcessInstance> dbInstances = getDbSession().getParentProcessInstancesWithOverdueTasks(userId,
        currentDate, startingIndex, pageSize, visibleProcessUUIDs, pagingCriterion);
    if (dbInstances == null) {
      return Collections.emptyList();
    }
    return dbInstances;
  }

  @Override
  public List<InternalProcessInstance> getParentProcessInstancesWithOverdueTasks(final String userId,
      final Date currentDate, final int startingIndex, final int pageSize) {
    final List<InternalProcessInstance> dbInstances = getDbSession().getParentProcessInstancesWithOverdueTasks(userId,
        currentDate, startingIndex, pageSize);
    if (dbInstances == null) {
      return Collections.emptyList();
    }
    return dbInstances;
  }

  @Override
  public List<InternalProcessInstance> getParentProcessInstancesWithOverdueTasks(final String userId,
      final Date currentDate, final int startingIndex, final int pageSize,
      final ProcessInstanceCriterion pagingCriterion) {
    final List<InternalProcessInstance> dbInstances = getDbSession().getParentProcessInstancesWithOverdueTasks(userId,
        currentDate, startingIndex, pageSize, pagingCriterion);
    if (dbInstances == null) {
      return Collections.emptyList();
    }
    return dbInstances;
  }

  @Override
  public List<InternalProcessInstance> getParentProcessInstancesWithInvolvedUser(final String userId,
      final int startingIndex, final int pageSize, final Set<ProcessDefinitionUUID> visibleProcessUUIDs) {
    final List<InternalProcessInstance> dbInstances = getDbSession().getParentProcessInstancesWithInvolvedUser(userId,
        startingIndex, pageSize, visibleProcessUUIDs);
    if (dbInstances == null) {
      return Collections.emptyList();
    }
    return dbInstances;
  }

  @Override
  public List<InternalProcessInstance> getParentProcessInstancesWithInvolvedUser(final String userId,
      final int startingIndex, final int pageSize, final Set<ProcessDefinitionUUID> visibleProcessUUIDs,
      final ProcessInstanceCriterion pagingCriterion) {
    final List<InternalProcessInstance> dbInstances = getDbSession().getParentProcessInstancesWithInvolvedUser(userId,
        startingIndex, pageSize, visibleProcessUUIDs, pagingCriterion);
    if (dbInstances == null) {
      return Collections.emptyList();
    }
    return dbInstances;
  }

  @Override
  public List<InternalProcessInstance> getParentProcessInstancesWithInvolvedUser(final String userId,
      final int startingIndex, final int pageSize) {
    final List<InternalProcessInstance> dbInstances = getDbSession().getParentProcessInstancesWithInvolvedUser(userId,
        startingIndex, pageSize);
    if (dbInstances == null) {
      return Collections.emptyList();
    }
    return dbInstances;
  }

  @Override
  public List<InternalProcessInstance> getParentProcessInstancesWithInvolvedUser(final String userId,
      final int startingIndex, final int pageSize, final ProcessInstanceCriterion pagingCriterion) {
    final List<InternalProcessInstance> dbInstances = getDbSession().getParentProcessInstancesWithInvolvedUser(userId,
        startingIndex, pageSize, pagingCriterion);
    if (dbInstances == null) {
      return Collections.emptyList();
    }
    return dbInstances;
  }

  @Override
  public Integer getNumberOfParentProcessInstancesWithActiveUser(final String userId,
      final Set<ProcessDefinitionUUID> visibleProcessUUIDs) {
    return getDbSession().getNumberOfParentProcessInstancesWithActiveUser(userId, visibleProcessUUIDs);
  }

  @Override
  public Integer getNumberOfParentProcessInstancesWithActiveUser(final String userId) {
    return getDbSession().getNumberOfParentProcessInstancesWithActiveUser(userId);
  }

  @Override
  public Integer getNumberOfParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate(final String userId,
      final Date currentDate, final Date atRisk, final Set<ProcessDefinitionUUID> visibleProcessUUIDs) {
    return getDbSession().getNumberOfParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate(userId,
        currentDate, atRisk, visibleProcessUUIDs);
  }

  @Override
  public Integer getNumberOfParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate(final String userId,
      final Date currentDate, final Date atRisk) {
    return getDbSession().getNumberOfParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate(userId,
        currentDate, atRisk);
  }

  @Override
  public Integer getNumberOfParentProcessInstancesWithOverdueTasks(final String userId, final Date currentDate,
      final Set<ProcessDefinitionUUID> visibleProcessUUIDs) {
    return getDbSession().getNumberOfParentProcessInstancesWithOverdueTasks(userId, currentDate, visibleProcessUUIDs);
  }

  @Override
  public Integer getNumberOfParentProcessInstancesWithOverdueTasks(final String userId, final Date currentDate) {
    return getDbSession().getNumberOfParentProcessInstancesWithOverdueTasks(userId, currentDate);
  }

  @Override
  public Integer getNumberOfParentProcessInstancesWithInvolvedUser(final String userId,
      final Set<ProcessDefinitionUUID> visibleProcessUUIDs) {
    return getDbSession().getNumberOfParentProcessInstancesWithInvolvedUser(userId, visibleProcessUUIDs);
  }

  @Override
  public Integer getNumberOfParentProcessInstancesWithInvolvedUser(final String userId) {
    return getDbSession().getNumberOfParentProcessInstancesWithInvolvedUser(userId);
  }

  @Override
  public Integer getNumberOfParentProcessInstancesWithStartedBy(final String userId,
      final Set<ProcessDefinitionUUID> visibleProcessUUIDs) {
    return getDbSession().getNumberOfParentProcessInstancesWithStartedBy(userId, visibleProcessUUIDs);
  }

  @Override
  public Integer getNumberOfParentProcessInstancesWithStartedBy(final String userId) {
    return getDbSession().getNumberOfParentProcessInstancesWithStartedBy(userId);
  }

  @Override
  public Set<Category> getCategories(final Collection<String> categoryNames) {
    return getDbSession().getCategories(categoryNames);
  }

  @Override
  public Set<Category> getAllCategories() {
    return getDbSession().getAllCategories();
  }

  @Override
  public Set<Category> getAllCategoriesExcept(final Set<String> uuids) {
    return getDbSession().getAllCategoriesExcept(uuids);
  }

  @Override
  public CategoryImpl getCategoryByUUID(final String uuid) {
    return getDbSession().getCategoryByUUID(uuid);
  }

  @Override
  public Set<CategoryImpl> getCategoriesByUUIDs(final Set<CategoryUUID> uuids) {
    return getDbSession().getCategoriesByUUIDs(uuids);
  }

  @Override
  public Set<ProcessDefinitionUUID> getProcessUUIDsFromCategory(final String category) {
    return getDbSession().getProcessUUIDsFromCategory(category);
  }

  @Override
  public List<Object> search(final SearchQueryBuilder query, final int firstResult, final int maxResults,
      final Class<?> indexClass) {
    return getDbSession().search(query, firstResult, maxResults, indexClass);
  }

  @Override
  public int search(final SearchQueryBuilder query, final Class<?> indexClass) {
    return getDbSession().search(query, indexClass);
  }

  @Override
  public Set<ActivityDefinitionUUID> getProcessTaskUUIDs(final ProcessDefinitionUUID definitionUUID) {
    return getDbSession().getProcessTaskUUIDs(definitionUUID);
  }

  @Override
  public boolean processExists(final ProcessDefinitionUUID definitionUUID) {
    return getDbSession().processExists(definitionUUID);
  }

  @Override
  public List<Long> getProcessInstancesDuration(final Date since, final Date until) {
    return getDbSession().getProcessInstancesDuration(since, until);
  }

  @Override
  public List<Long> getProcessInstancesDuration(final ProcessDefinitionUUID processUUID, final Date since,
      final Date until) {
    return getDbSession().getProcessInstancesDuration(processUUID, since, until);
  }

  @Override
  public List<Long> getProcessInstancesDurationFromProcessUUIDs(final Set<ProcessDefinitionUUID> processUUIDs,
      final Date since, final Date until) {
    return getDbSession().getProcessInstancesDurationFromProcessUUIDs(processUUIDs, since, until);
  }

  @Override
  public List<Long> getActivityInstancesExecutionTime(final Date since, final Date until) {
    return getDbSession().getActivityInstancesExecutionTime(since, until);
  }

  @Override
  public List<Long> getActivityInstancesExecutionTime(final ProcessDefinitionUUID processUUID, final Date since,
      final Date until) {
    return getDbSession().getActivityInstancesExecutionTime(processUUID, since, until);
  }

  @Override
  public List<Long> getActivityInstancesExecutionTimeFromProcessUUIDs(final Set<ProcessDefinitionUUID> processUUIDs,
      final Date since, final Date until) {
    return getDbSession().getActivityInstancesExecutionTimeFromProcessUUIDs(processUUIDs, since, until);
  }

  @Override
  public List<Long> getActivityInstancesExecutionTime(final ActivityDefinitionUUID activityUUID, final Date since,
      final Date until) {
    return getDbSession().getActivityInstancesExecutionTime(activityUUID, since, until);
  }

  @Override
  public List<Long> getActivityInstancesExecutionTimeFromActivityUUIDs(final Set<ActivityDefinitionUUID> activityUUIDs,
      final Date since, final Date until) {
    return getDbSession().getActivityInstancesExecutionTimeFromActivityUUIDs(activityUUIDs, since, until);
  }

  @Override
  public List<Long> getTaskInstancesWaitingTime(final Date since, final Date until) {
    return getDbSession().getTaskInstancesWaitingTime(since, until);
  }

  @Override
  public List<Long> getTaskInstancesWaitingTime(final ProcessDefinitionUUID processUUID, final Date since,
      final Date until) {
    return getDbSession().getTaskInstancesWaitingTime(processUUID, since, until);
  }

  @Override
  public List<Long> getTaskInstancesWaitingTimeFromProcessUUIDs(final Set<ProcessDefinitionUUID> processUUIDs,
      final Date since, final Date until) {
    return getDbSession().getTaskInstancesWaitingTimeFromProcessUUIDs(processUUIDs, since, until);
  }

  @Override
  public List<Long> getTaskInstancesWaitingTime(final ActivityDefinitionUUID taskUUID, final Date since,
      final Date until) {
    return getDbSession().getTaskInstancesWaitingTime(taskUUID, since, until);
  }

  @Override
  public List<Long> getTaskInstancesWaitingTimeFromTaskUUIDs(final Set<ActivityDefinitionUUID> taskUUIDs,
      final Date since, final Date until) {
    return getDbSession().getTaskInstancesWaitingTimeFromTasksUUIDs(taskUUIDs, since, until);
  }

  @Override
  public List<Long> getTaskInstancesWaitingTimeOfUser(final String username, final Date since, final Date until) {
    return getDbSession().getTaskInstancesWaitingTimeOfUser(username, since, until);
  }

  @Override
  public List<Long> getTaskInstancesWaitingTimeOfUser(final String username, final ProcessDefinitionUUID processUUID,
      final Date since, final Date until) {
    return getDbSession().getTaskInstancesWaitingTimeOfUser(username, processUUID, since, until);
  }

  @Override
  public List<Long> getTaskInstancesWaitingTimeOfUserFromProcessUUIDs(final String username,
      final Set<ProcessDefinitionUUID> processUUIDs, final Date since, final Date until) {
    return getDbSession().getTaskInstancesWaitingTimeOfUserFromProcessUUIDs(username, processUUIDs, since, until);
  }

  @Override
  public List<Long> getTaskInstancesWaitingTimeOfUser(final String username, final ActivityDefinitionUUID taskUUID,
      final Date since, final Date until) {
    return getDbSession().getTaskInstancesWaitingTimeOfUser(username, taskUUID, since, until);
  }

  @Override
  public List<Long> getTaskInstancesWaitingTimeOfUserFromTaskUUIDs(final String username,
      final Set<ActivityDefinitionUUID> taskUUIDs, final Date since, final Date until) {
    return getDbSession().getTaskInstancesWaitingTimeOfUserFromTaskUUIDs(username, taskUUIDs, since, until);
  }

  @Override
  public List<Long> getActivityInstancesDuration(final Date since, final Date until) {
    return getDbSession().getActivityInstancesDuration(since, until);
  }

  @Override
  public List<Long> getActivityInstancesDuration(final ProcessDefinitionUUID processUUID, final Date since,
      final Date until) {
    return getDbSession().getActivityInstancesDuration(processUUID, since, until);
  }

  @Override
  public List<Long> getActivityInstancesDurationFromProcessUUIDs(final Set<ProcessDefinitionUUID> processUUIDs,
      final Date since, final Date until) {
    return getDbSession().getActivityInstancesDurationFromProcessUUIDs(processUUIDs, since, until);
  }

  @Override
  public List<Long> getActivityInstancesDuration(final ActivityDefinitionUUID activityUUID, final Date since,
      final Date until) {
    return getDbSession().getActivityInstancesDuration(activityUUID, since, until);
  }

  @Override
  public List<Long> getActivityInstancesDurationFromActivityUUIDs(final Set<ActivityDefinitionUUID> activityUUIDs,
      final Date since, final Date until) {
    return getDbSession().getActivityInstancesDurationFromActivityUUIDs(activityUUIDs, since, until);
  }

  @Override
  public List<Long> getActivityInstancesDurationByActivityType(final Type activityType, final Date since,
      final Date until) {
    return getDbSession().getActivityInstancesDurationByActivityType(activityType, since, until);
  }

  @Override
  public List<Long> getActivityInstancesDurationByActivityType(final Type activityType,
      final ProcessDefinitionUUID processUUID, final Date since, final Date until) {
    return getDbSession().getActivityInstancesDurationByActivityType(activityType, processUUID, since, until);
  }

  @Override
  public List<Long> getActivityInstancesDurationByActivityTypeFromProcessUUIDs(final Type activityType,
      final Set<ProcessDefinitionUUID> processUUIDs, final Date since, final Date until) {
    return getDbSession().getActivityInstancesDurationByActivityTypeFromProcessUUIDs(activityType, processUUIDs, since,
        until);
  }

  @Override
  public long getNumberOfCreatedProcessInstances(final Date since, final Date until) {
    return getDbSession().getNumberOfCreatedProcessInstances(since, until);
  }

  @Override
  public long getNumberOfCreatedProcessInstances(final ProcessDefinitionUUID processUUID, final Date since,
      final Date until) {
    return getDbSession().getNumberOfCreatedProcessInstances(processUUID, since, until);
  }

  @Override
  public long getNumberOfCreatedActivityInstances(final Date since, final Date until) {
    return getDbSession().getNumberOfCreatedActivityInstances(since, until);
  }

  @Override
  public long getNumberOfCreatedActivityInstances(final ProcessDefinitionUUID processUUID, final Date since,
      final Date until) {
    return getDbSession().getNumberOfCreatedActivityInstances(processUUID, since, until);
  }

  @Override
  public long getNumberOfCreatedActivityInstancesFromProcessUUIDs(final Set<ProcessDefinitionUUID> processUUIDs,
      final Date since, final Date until) {
    return getDbSession().getNumberOfCreatedActivityInstancesFromProcessUUIDs(processUUIDs, since, until);
  }

  @Override
  public long getNumberOfCreatedActivityInstances(final ActivityDefinitionUUID activityUUID, final Date since,
      final Date until) {
    return getDbSession().getNumberOfCreatedActivityInstances(activityUUID, since, until);
  }

  @Override
  public long getNumberOfCreatedActivityInstancesFromActivityUUIDs(final Set<ActivityDefinitionUUID> activityUUIDs,
      final Date since, final Date until) {
    return getDbSession().getNumberOfCreatedActivityInstancesFromActivityUUIDs(activityUUIDs, since, until);
  }

  @Override
  public long getNumberOfCreatedActivityInstancesByActivityType(final Type activityType, final Date since,
      final Date until) {
    return getDbSession().getNumberOfCreatedActivityInstancesByActivityType(activityType, since, until);
  }

  @Override
  public long getNumberOfCreatedActivityInstancesByActivityType(final Type activityType,
      final ProcessDefinitionUUID processUUID, final Date since, final Date until) {
    return getDbSession().getNumberOfCreatedActivityInstancesByActivityType(activityType, processUUID, since, until);
  }

  @Override
  public long getNumberOfCreatedActivityInstancesByActivityTypeFromProcessUUIDs(final Type activityType,
      final Set<ProcessDefinitionUUID> processUUIDs, final Date since, final Date until) {
    return getDbSession().getNumberOfCreatedActivityInstancesByActivityTypeFromProcessUUIDs(activityType, processUUIDs,
        since, until);
  }

  @Override
  public boolean containsOtherActiveActivities(final ProcessInstanceUUID instanceUUID,
      final ActivityInstanceUUID activityUUID) {
    return getDbSession().containsOtherActiveActivities(instanceUUID, activityUUID);
  }

}
