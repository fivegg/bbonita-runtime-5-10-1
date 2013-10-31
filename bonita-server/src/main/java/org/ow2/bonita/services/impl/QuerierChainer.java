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
 * Modified by Matthieu Chaffotte, Nicolas Chabanoles, Elias Ricken de Medeiros - BonitaSoft S.A.
 **/
package org.ow2.bonita.services.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
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
import org.ow2.bonita.runtime.model.Execution;
import org.ow2.bonita.search.SearchQueryBuilder;
import org.ow2.bonita.services.Querier;
import org.ow2.bonita.util.ActivityInstanceLastUpdateComparator;
import org.ow2.bonita.util.InternalProcessDefinitionComparator;
import org.ow2.bonita.util.InternalProcessInstanceComparator;
import org.ow2.bonita.util.Misc;
import org.ow2.bonita.util.ProcessInstanceEndedDateComparatorAsc;
import org.ow2.bonita.util.ProcessInstanceEndedDateComparatorDesc;
import org.ow2.bonita.util.ProcessInstanceLastUpdateComparator;
import org.ow2.bonita.util.ProcessInstanceLastUpdateComparatorAsc;
import org.ow2.bonita.util.ProcessInstanceNbComparatorAsc;
import org.ow2.bonita.util.ProcessInstanceNbComparatorDesc;
import org.ow2.bonita.util.ProcessInstanceStartedDateComparatorAsc;
import org.ow2.bonita.util.ProcessInstanceStartedDateComparatorDesc;
import org.ow2.bonita.util.ProcessInstanceUUIDComparatorAsc;
import org.ow2.bonita.util.ProcessInstanceUUIDComparatorDesc;

/**
 * @author Guillaume Porcher
 * 
 *         Chainer for Queriers : - for methods that returns only one object, search for the first matching object - for
 *         methods that returns a collection: search for all objects in all return queriers.
 */
public class QuerierChainer implements Querier {

  private final List<Querier> queriers;

  public QuerierChainer(final List<Querier> queriers) {
    this.queriers = queriers;
  }

  @Override
  public Set<InternalActivityInstance> getActivityInstances(final ProcessInstanceUUID instanceUUID,
      final String activityName) {
    final Set<InternalActivityInstance> set = new HashSet<InternalActivityInstance>();
    for (final Querier querier : queriers) {
      final Set<InternalActivityInstance> tmp = querier.getActivityInstances(instanceUUID, activityName);
      if (tmp != null && !tmp.isEmpty()) {
        set.addAll(tmp);
      }
    }
    return set;
  }

  @Override
  public Set<InternalActivityInstance> getActivityInstances(final ProcessInstanceUUID instanceUUID,
      final String activityName, final String iterationId) {
    final Set<InternalActivityInstance> set = new HashSet<InternalActivityInstance>();
    for (final Querier querier : queriers) {
      final Set<InternalActivityInstance> tmp = querier.getActivityInstances(instanceUUID, activityName, iterationId);
      if (tmp != null && !tmp.isEmpty()) {
        set.addAll(tmp);
      }
    }
    return set;
  }

  @Override
  public int getNumberOfProcesses() {
    int nb = 0;
    for (final Querier querier : queriers) {
      nb += querier.getNumberOfProcesses();
    }
    return nb;
  }

  @Override
  public int getNumberOfParentProcessInstances() {
    int nb = 0;
    for (final Querier querier : queriers) {
      nb += querier.getNumberOfParentProcessInstances();
    }
    return nb;
  }

  @Override
  public int getNumberOfProcessInstances() {
    int nb = 0;
    for (final Querier querier : queriers) {
      nb += querier.getNumberOfProcessInstances();
    }
    return nb;
  }

  @Override
  public InternalProcessInstance getProcessInstance(final ProcessInstanceUUID instanceUUID) {
    for (final Querier querier : queriers) {
      final InternalProcessInstance processInst = querier.getProcessInstance(instanceUUID);
      if (processInst != null) {
        return processInst;
      }
    }
    return null;
  }

  @Override
  public Set<ProcessInstanceUUID> getParentInstancesUUIDs() {
    final Set<ProcessInstanceUUID> processInsts = new HashSet<ProcessInstanceUUID>();
    for (final Querier querier : queriers) {
      final Set<ProcessInstanceUUID> tmp = querier.getParentInstancesUUIDs();
      if (tmp != null && !tmp.isEmpty()) {
        processInsts.addAll(tmp);
      }
    }
    return processInsts;
  }

  @Override
  public Set<InternalProcessInstance> getUserInstances(final String userId, final Date minStartDate) {
    final Set<InternalProcessInstance> processInsts = new HashSet<InternalProcessInstance>();
    for (final Querier querier : queriers) {
      final Set<InternalProcessInstance> tmp = querier.getUserInstances(userId, minStartDate);
      if (tmp != null && !tmp.isEmpty()) {
        processInsts.addAll(tmp);
      }
    }
    return processInsts;
  }

  @Override
  public Set<InternalProcessInstance> getUserParentInstances(final String userId, final Date minStartDate) {
    final Set<InternalProcessInstance> processInsts = new HashSet<InternalProcessInstance>();
    for (final Querier querier : queriers) {
      final Set<InternalProcessInstance> tmp = querier.getUserParentInstances(userId, minStartDate);
      if (tmp != null && !tmp.isEmpty()) {
        processInsts.addAll(tmp);
      }
    }
    return processInsts;
  }

  @Override
  public TaskInstance getOneTask(final String userId, final ProcessDefinitionUUID processUUID,
      final ActivityState taskState) {
    for (final Querier querier : queriers) {
      final TaskInstance task = querier.getOneTask(userId, processUUID, taskState);
      if (task != null) {
        return task;
      }
    }
    return null;
  }

  @Override
  public TaskInstance getOneTask(final String userId, final ProcessInstanceUUID instanceUUID,
      final ActivityState taskState) {
    for (final Querier querier : queriers) {
      final TaskInstance task = querier.getOneTask(userId, instanceUUID, taskState);
      if (task != null) {
        return task;
      }
    }
    return null;
  }

  @Override
  public TaskInstance getOneTask(final String userId, final ActivityState taskState) {
    for (final Querier querier : queriers) {
      final TaskInstance task = querier.getOneTask(userId, taskState);
      if (task != null) {
        return task;
      }
    }
    return null;
  }

  @Override
  public Set<InternalProcessInstance> getProcessInstances() {
    final Set<InternalProcessInstance> processInsts = new HashSet<InternalProcessInstance>();
    for (final Querier querier : queriers) {
      final Set<InternalProcessInstance> tmp = querier.getProcessInstances();
      if (tmp != null && !tmp.isEmpty()) {
        processInsts.addAll(tmp);
      }
    }
    return processInsts;
  }

  @Override
  public Set<InternalProcessInstance> getUserInstancesExcept(final String userId, final Set<ProcessInstanceUUID> myCases) {
    final Set<InternalProcessInstance> processInsts = new HashSet<InternalProcessInstance>();
    for (final Querier querier : queriers) {
      final Set<InternalProcessInstance> tmp = querier.getUserInstancesExcept(userId, myCases);
      if (tmp != null && !tmp.isEmpty()) {
        processInsts.addAll(tmp);
      }
    }
    return processInsts;
  }

  @Override
  public Set<InternalProcessInstance> getProcessInstances(final Collection<ProcessInstanceUUID> instanceUUIDs) {
    final Set<InternalProcessInstance> processInsts = new HashSet<InternalProcessInstance>();
    for (final Querier querier : queriers) {
      final Set<InternalProcessInstance> tmp = querier.getProcessInstances(instanceUUIDs);
      if (tmp != null && !tmp.isEmpty()) {
        processInsts.addAll(tmp);
      }
    }
    return processInsts;
  }

  @Override
  public Set<InternalProcessInstance> getParentInstances() {
    final Set<InternalProcessInstance> processInsts = new HashSet<InternalProcessInstance>();
    for (final Querier querier : queriers) {
      final Set<InternalProcessInstance> tmp = querier.getParentInstances();
      if (tmp != null && !tmp.isEmpty()) {
        processInsts.addAll(tmp);
      }
    }
    return processInsts;
  }

  @Override
  public Set<InternalProcessInstance> getProcessInstancesWithTaskState(final Collection<ActivityState> activityStates) {
    final Set<InternalProcessInstance> processInsts = new HashSet<InternalProcessInstance>();
    for (final Querier querier : queriers) {
      final Set<InternalProcessInstance> tmp = querier.getProcessInstancesWithTaskState(activityStates);
      if (tmp != null && !tmp.isEmpty()) {
        processInsts.addAll(tmp);
      }
    }
    return processInsts;
  }

  @Override
  public Set<InternalProcessInstance> getProcessInstancesWithInstanceStates(
      final Collection<InstanceState> instanceStates) {
    final Set<InternalProcessInstance> processInsts = new HashSet<InternalProcessInstance>();
    for (final Querier querier : queriers) {
      final Set<InternalProcessInstance> tmp = querier.getProcessInstancesWithInstanceStates(instanceStates);
      if (tmp != null && !tmp.isEmpty()) {
        processInsts.addAll(tmp);
      }
    }
    return processInsts;
  }

  @Override
  public Set<InternalProcessInstance> getProcessInstances(final ProcessDefinitionUUID processUUID) {
    final Set<InternalProcessInstance> processInsts = new HashSet<InternalProcessInstance>();
    for (final Querier querier : queriers) {
      processInsts.addAll(querier.getProcessInstances(processUUID));
    }
    return processInsts;
  }

  @Override
  public Set<InternalProcessInstance> getProcessInstances(final ProcessDefinitionUUID processUUID,
      final InstanceState instanceState) {
    final Set<InternalProcessInstance> processInsts = new HashSet<InternalProcessInstance>();
    for (final Querier querier : queriers) {
      processInsts.addAll(querier.getProcessInstances(processUUID, instanceState));
    }
    return processInsts;
  }

  @Override
  public InternalActivityInstance getActivityInstance(final ProcessInstanceUUID instanceUUID, final String activityId,
      final String iterationId, final String activityInstanceId, final String loopId) {
    for (final Querier querier : queriers) {
      final InternalActivityInstance activityInst = querier.getActivityInstance(instanceUUID, activityId, iterationId,
          activityInstanceId, loopId);
      if (activityInst != null) {
        return activityInst;
      }
    }
    return null;
  }

  @Override
  public List<InternalActivityInstance> getActivityInstancesFromRoot(final ProcessInstanceUUID rootInstanceUUID) {
    final List<InternalActivityInstance> activities = new ArrayList<InternalActivityInstance>();
    for (final Querier querier : queriers) {
      final List<InternalActivityInstance> tmp = querier.getActivityInstancesFromRoot(rootInstanceUUID);
      if (tmp != null && !tmp.isEmpty()) {
        activities.addAll(tmp);
      }
    }
    if (!activities.isEmpty()) {
      Collections.sort(activities, new ActivityInstanceLastUpdateComparator());
    }
    return activities;
  }

  @Override
  public List<InternalActivityInstance> getActivityInstancesFromRoot(final Set<ProcessInstanceUUID> rootInstanceUUIDs) {
    final List<InternalActivityInstance> activities = new ArrayList<InternalActivityInstance>();
    for (final Querier querier : queriers) {
      final List<InternalActivityInstance> tmp = querier.getActivityInstancesFromRoot(rootInstanceUUIDs);
      if (tmp != null && !tmp.isEmpty()) {
        activities.addAll(tmp);
      }
    }
    if (!activities.isEmpty()) {
      Collections.sort(activities, new ActivityInstanceLastUpdateComparator());
    }
    return activities;
  }

  @Override
  public List<InternalActivityInstance> getActivityInstancesFromRoot(final Set<ProcessInstanceUUID> rootInstanceUUIDs,
      final ActivityState state) {
    final List<InternalActivityInstance> activities = new ArrayList<InternalActivityInstance>();
    for (final Querier querier : queriers) {
      final List<InternalActivityInstance> tmp = querier.getActivityInstancesFromRoot(rootInstanceUUIDs, state);
      if (tmp != null && !tmp.isEmpty()) {
        activities.addAll(tmp);
      }
    }
    if (!activities.isEmpty()) {
      Collections.sort(activities, new ActivityInstanceLastUpdateComparator());
    }
    return activities;
  }

  @Override
  public Map<ProcessInstanceUUID, InternalActivityInstance> getLastUpdatedActivityInstanceFromRoot(
      final Set<ProcessInstanceUUID> rootInstanceUUIDs, final boolean considerSystemTaks) {
    final Map<ProcessInstanceUUID, InternalActivityInstance> activities = new HashMap<ProcessInstanceUUID, InternalActivityInstance>();
    for (final Querier querier : queriers) {
      final Map<ProcessInstanceUUID, InternalActivityInstance> tmp = querier.getLastUpdatedActivityInstanceFromRoot(
          rootInstanceUUIDs, considerSystemTaks);
      for (final Map.Entry<ProcessInstanceUUID, InternalActivityInstance> entry : tmp.entrySet()) {
        activities.put(entry.getKey(), entry.getValue());
      }
    }
    return activities;
  }

  @Override
  public long getLastProcessInstanceNb(final ProcessDefinitionUUID processUUID) {
    long max = -1;
    for (final Querier querier : queriers) {
      final long l = querier.getLastProcessInstanceNb(processUUID);
      if (l > max) {
        max = l;
      }
    }
    return max;
  }

  @Override
  public Execution getExecutionOnActivity(final ProcessInstanceUUID instanceUUID,
      final ActivityInstanceUUID activityInstanceUUID) {
    for (final Querier querier : queriers) {
      final Execution execution = querier.getExecutionOnActivity(instanceUUID, activityInstanceUUID);
      if (execution != null) {
        return execution;
      }
    }
    return null;
  }

  @Override
  public Execution getExecutionWithEventUUID(final String eventUUID) {
    for (final Querier querier : queriers) {
      final Execution execution = querier.getExecutionWithEventUUID(eventUUID);
      if (execution != null) {
        return execution;
      }
    }
    return null;
  }

  @Override
  public Set<Execution> getExecutions(final ProcessInstanceUUID instanceUUID) {
    final Set<Execution> executions = new HashSet<Execution>();
    for (final Querier querier : queriers) {
      executions.addAll(querier.getExecutions(instanceUUID));
    }
    return executions;
  }

  @Override
  public ActivityState getActivityInstanceState(final ActivityInstanceUUID activityInstanceUUID) {
    for (final Querier querier : queriers) {
      final ActivityState activityState = querier.getActivityInstanceState(activityInstanceUUID);
      if (activityState != null) {
        return activityState;
      }
    }
    return null;
  }

  @Override
  public Set<InternalActivityInstance> getActivityInstances(final ProcessInstanceUUID instanceUUID) {
    final Set<InternalActivityInstance> activityInsts = new HashSet<InternalActivityInstance>();
    for (final Querier querier : queriers) {
      activityInsts.addAll(querier.getActivityInstances(instanceUUID));
      if (!activityInsts.isEmpty()) {
        return activityInsts;
      }
    }
    return activityInsts;
  }

  @Override
  public List<InternalActivityInstance> getActivityInstances(final ProcessInstanceUUID instanceUUID,
      final int fromIndex, final int pageSize, final ActivityInstanceCriterion pagingCriterion) {
    final List<InternalActivityInstance> activityInsts = new ArrayList<InternalActivityInstance>();
    for (final Querier querier : queriers) {
      activityInsts.addAll(querier.getActivityInstances(instanceUUID, fromIndex, pageSize, pagingCriterion));
      if (!activityInsts.isEmpty()) {
        return activityInsts;
      }
    }
    return activityInsts;
  }

  @Override
  public TaskInstance getTaskInstance(final ActivityInstanceUUID taskUUID) {
    for (final Querier querier : queriers) {
      final TaskInstance activity = querier.getTaskInstance(taskUUID);
      if (activity != null) {
        return activity;
      }
    }
    return null;
  }

  @Override
  public Set<InternalProcessDefinition> getProcesses() {
    final Set<InternalProcessDefinition> processes = new HashSet<InternalProcessDefinition>();
    for (final Querier querier : queriers) {
      final Set<InternalProcessDefinition> querierProcesses = querier.getProcesses();
      if (querierProcesses != null && !querierProcesses.isEmpty()) {
        processes.addAll(querierProcesses);
      }
    }
    return processes;
  }

  @Override
  public Set<InternalProcessDefinition> getProcesses(final String processId) {
    final Set<InternalProcessDefinition> processes = new HashSet<InternalProcessDefinition>();
    for (final Querier querier : queriers) {
      final Set<InternalProcessDefinition> querierProcesses = querier.getProcesses(processId);
      if (querierProcesses != null && !querierProcesses.isEmpty()) {
        processes.addAll(querierProcesses);
      }
    }
    return processes;
  }

  @Override
  public Set<InternalProcessDefinition> getProcesses(final ProcessState processState) {
    final Set<InternalProcessDefinition> processes = new HashSet<InternalProcessDefinition>();
    for (final Querier querier : queriers) {
      final Set<InternalProcessDefinition> querierProcesses = querier.getProcesses(processState);
      if (querierProcesses != null && !querierProcesses.isEmpty()) {
        processes.addAll(querierProcesses);
      }
    }
    return processes;
  }

  @Override
  public Set<InternalProcessDefinition> getProcesses(final String processId, final ProcessState processState) {
    final Set<InternalProcessDefinition> processes = new HashSet<InternalProcessDefinition>();
    for (final Querier querier : queriers) {
      final Set<InternalProcessDefinition> querierProcesses = querier.getProcesses(processId, processState);
      if (querierProcesses != null && !querierProcesses.isEmpty()) {
        processes.addAll(querierProcesses);
      }
    }
    return processes;
  }

  @Override
  public InternalProcessDefinition getProcess(final ProcessDefinitionUUID processUUID) {
    for (final Querier querier : queriers) {
      final InternalProcessDefinition process = querier.getProcess(processUUID);
      if (process != null) {
        return process;
      }
    }
    return null;
  }

  @Override
  public InternalProcessDefinition getProcess(final String processId, final String version) {
    for (final Querier querier : queriers) {
      final InternalProcessDefinition process = querier.getProcess(processId, version);
      if (process != null) {
        return process;
      }
    }
    return null;
  }

  @Override
  public String getLastProcessVersion(final String processName) {
    Misc.checkArgsNotNull(processName);
    String last = null;
    for (final Querier querier : queriers) {
      final String version = querier.getLastProcessVersion(processName);
      if (version != null && (last == null || version.compareTo(last) > 0)) {
        last = version;
      }
    }
    return last;
  }

  @Override
  public InternalProcessDefinition getLastDeployedProcess(final String processId, final ProcessState processState) {
    Misc.checkArgsNotNull(processId, processState);
    final Set<InternalProcessDefinition> processes = getProcesses(processId, processState);
    InternalProcessDefinition lastProcess = null;
    for (final InternalProcessDefinition process : processes) {
      if (lastProcess == null) {
        lastProcess = process;
      } else if (process.getDeployedDate().after(lastProcess.getDeployedDate())) {
        lastProcess = process;
      }
    }
    return lastProcess;
  }

  @Override
  public InternalActivityInstance getActivityInstance(final ActivityInstanceUUID activityInstanceUUID) {
    for (final Querier querier : queriers) {
      final InternalActivityInstance activity = querier.getActivityInstance(activityInstanceUUID);
      if (activity != null) {
        return activity;
      }
    }
    return null;
  }

  @Override
  public Set<TaskInstance> getTaskInstances(final ProcessInstanceUUID instanceUUID) {
    final Set<TaskInstance> activities = new HashSet<TaskInstance>();
    for (final Querier querier : queriers) {
      activities.addAll(querier.getTaskInstances(instanceUUID));
      if (!activities.isEmpty()) {
        return activities;
      }
    }
    return activities;
  }

  @Override
  public Set<TaskInstance> getTaskInstances(final ProcessInstanceUUID instanceUUID, final Set<String> taskNames) {
    final Set<TaskInstance> activities = new HashSet<TaskInstance>();
    for (final Querier querier : queriers) {
      activities.addAll(querier.getTaskInstances(instanceUUID, taskNames));
      if (!activities.isEmpty()) {
        return activities;
      }
    }
    return activities;
  }

  @Override
  public Set<TaskInstance> getUserInstanceTasks(final String userId, final ProcessInstanceUUID instanceUUID,
      final ActivityState taskState) {
    final Set<TaskInstance> activities = new HashSet<TaskInstance>();
    for (final Querier querier : queriers) {
      activities.addAll(querier.getUserInstanceTasks(userId, instanceUUID, taskState));
      if (!activities.isEmpty()) {
        return activities;
      }
    }
    return activities;
  }

  @Override
  public Set<TaskInstance> getUserTasks(final String userId, final ActivityState taskState) {
    final Set<TaskInstance> activities = new HashSet<TaskInstance>();
    for (final Querier querier : queriers) {
      activities.addAll(querier.getUserTasks(userId, taskState));
    }
    return activities;
  }

  @Override
  public Set<TaskInstance> getUserTasks(final String userId, final Collection<ActivityState> taskStates) {
    final Set<TaskInstance> activities = new HashSet<TaskInstance>();
    for (final Querier querier : queriers) {
      activities.addAll(querier.getUserTasks(userId, taskStates));
    }
    return activities;
  }

  @Override
  public InternalActivityDefinition getActivity(final ActivityDefinitionUUID activityDefinitionUUID) {
    for (final Querier querier : queriers) {
      final InternalActivityDefinition activity = querier.getActivity(activityDefinitionUUID);
      if (activity != null) {
        return activity;
      }
    }
    return null;
  }

  @Override
  public int getNumberOfUserOpenSteps(final String userId) {
    int openSteps = 0;
    for (final Querier querier : queriers) {
      openSteps += querier.getNumberOfUserOpenSteps(userId);
    }
    return openSteps;
  }

  @Override
  public List<Integer> getNumberOfFinishedCasesPerDay(final Date since, final Date to) {
    final List<Integer> finishedCases = new ArrayList<Integer>();
    for (final Querier querier : queriers) {
      final List<Integer> finishedList = querier.getNumberOfFinishedCasesPerDay(since, to);
      if (finishedCases.isEmpty()) {
        for (int i = 0; i < finishedList.size(); i++) {
          finishedCases.add(finishedList.get(i));
        }
      } else {
        for (int i = 0; i < finishedList.size(); i++) {
          finishedCases.set(i, finishedList.get(i) + finishedCases.get(i));
        }
      }
    }
    return finishedCases;
  }

  @Override
  public List<Integer> getNumberOfExecutingCasesPerDay(final Date since, final Date to) {
    final List<Integer> executingCases = new ArrayList<Integer>();
    for (final Querier querier : queriers) {
      final List<Integer> executingList = querier.getNumberOfExecutingCasesPerDay(since, to);
      if (executingCases.isEmpty()) {
        for (int i = 0; i < executingList.size(); i++) {
          executingCases.add(executingList.get(i));
        }
      } else {
        for (int i = 0; i < executingList.size(); i++) {
          executingCases.set(i, executingList.get(i) + executingCases.get(i));
        }
      }
    }
    return executingCases;
  }

  @Override
  public List<Integer> getNumberOfOpenStepsPerDay(final Date since, final Date to) {
    final List<Integer> opensteps = new ArrayList<Integer>();
    for (final Querier querier : queriers) {
      final List<Integer> openList = querier.getNumberOfOpenStepsPerDay(since, to);
      if (opensteps.isEmpty()) {
        for (int i = 0; i < openList.size(); i++) {
          opensteps.add(openList.get(i));
        }
      } else {
        for (int i = 0; i < openList.size(); i++) {
          opensteps.set(i, openList.get(i) + opensteps.get(i));
        }
      }
    }
    return opensteps;
  }

  @Override
  public int getNumberOfOverdueSteps(final Date currentDate) {
    int openSteps = 0;
    for (final Querier querier : queriers) {
      openSteps += querier.getNumberOfOverdueSteps(currentDate);
    }
    return openSteps;
  }

  @Override
  public int getNumberOfStepsAtRisk(final Date currentDate, final Date atRisk) {
    int openSteps = 0;
    for (final Querier querier : queriers) {
      openSteps += querier.getNumberOfStepsAtRisk(currentDate, atRisk);
    }
    return openSteps;
  }

  @Override
  public int getNumberOfOpenSteps() {
    int openSteps = 0;
    for (final Querier querier : queriers) {
      openSteps += querier.getNumberOfOpenSteps();
    }
    return openSteps;
  }

  @Override
  public int getNumberOfUserOverdueSteps(final String userId, final Date currentDate) {
    int openSteps = 0;
    for (final Querier querier : queriers) {
      openSteps += querier.getNumberOfUserOverdueSteps(userId, currentDate);
    }
    return openSteps;
  }

  @Override
  public int getNumberOfUserStepsAtRisk(final String userId, final Date currentDate, final Date atRisk) {
    int stepsAtRisk = 0;
    for (final Querier querier : queriers) {
      stepsAtRisk += querier.getNumberOfUserStepsAtRisk(userId, currentDate, atRisk);
    }
    return stepsAtRisk;
  }

  @Override
  public int getNumberOfFinishedSteps(final int priority, final Date since) {
    int finishedSteps = 0;
    for (final Querier querier : queriers) {
      finishedSteps += querier.getNumberOfFinishedSteps(priority, since);
    }
    return finishedSteps;
  }

  @Override
  public int getNumberOfOpenSteps(final int priority) {
    int openSteps = 0;
    for (final Querier querier : queriers) {
      openSteps += querier.getNumberOfOpenSteps(priority);
    }
    return openSteps;
  }

  @Override
  public int getNumberOfUserFinishedSteps(final String userId, final int priority, final Date since) {
    int finishedSteps = 0;
    for (final Querier querier : queriers) {
      finishedSteps += querier.getNumberOfUserFinishedSteps(userId, priority, since);
    }
    return finishedSteps;
  }

  @Override
  public int getNumberOfUserOpenSteps(final String userId, final int priority) {
    int openSteps = 0;
    for (final Querier querier : queriers) {
      openSteps += querier.getNumberOfUserOpenSteps(userId, priority);
    }
    return openSteps;
  }

  @Override
  public Set<InternalProcessDefinition> getProcesses(final Set<ProcessDefinitionUUID> definitionUUIDs) {
    final Set<InternalProcessDefinition> processes = new HashSet<InternalProcessDefinition>();
    for (final Querier querier : queriers) {
      final Set<InternalProcessDefinition> temp = querier.getProcesses(definitionUUIDs);
      if (temp != null) {
        processes.addAll(temp);
      }
    }
    return processes;
  }

  @Override
  public Set<InternalProcessDefinition> getProcesses(final Set<ProcessDefinitionUUID> definitionUUIDs,
      final ProcessState processState) {
    final Set<InternalProcessDefinition> processes = new HashSet<InternalProcessDefinition>();
    for (final Querier querier : queriers) {
      final Set<InternalProcessDefinition> temp = querier.getProcesses(definitionUUIDs, processState);
      if (temp != null) {
        processes.addAll(temp);
      }
    }
    return processes;
  }

  @Override
  public InternalProcessDefinition getLastDeployedProcess(final Set<ProcessDefinitionUUID> definitionUUIDs,
      final ProcessState processState) {
    Misc.checkArgsNotNull(processState);
    final Set<InternalProcessDefinition> processes = getProcesses(definitionUUIDs, processState);
    InternalProcessDefinition lastProcess = null;
    for (final InternalProcessDefinition process : processes) {
      if (lastProcess == null) {
        lastProcess = process;
      } else if (process.getDeployedDate().after(lastProcess.getDeployedDate())) {
        lastProcess = process;
      }
    }
    return lastProcess;
  }

  @Override
  public Set<InternalProcessInstance> getProcessInstances(final Set<ProcessDefinitionUUID> definitionUUIDs) {
    final Set<InternalProcessInstance> processInsts = new HashSet<InternalProcessInstance>();
    for (final Querier querier : queriers) {
      processInsts.addAll(querier.getProcessInstances(definitionUUIDs));
    }
    return processInsts;
  }

  @Override
  public Set<InternalProcessInstance> getUserInstances(final String userId,
      final Set<ProcessDefinitionUUID> definitionUUIDs) {
    final Set<InternalProcessInstance> processInsts = new HashSet<InternalProcessInstance>();
    for (final Querier querier : queriers) {
      final Set<InternalProcessInstance> tmp = querier.getUserInstances(userId, definitionUUIDs);
      if (tmp != null && !tmp.isEmpty()) {
        processInsts.addAll(tmp);
      }
    }
    return processInsts;
  }

  @Override
  public int getNumberOfProcessInstances(final Set<ProcessDefinitionUUID> definitionUUIDs) {
    int nb = 0;
    for (final Querier querier : queriers) {
      nb += querier.getNumberOfProcessInstances(definitionUUIDs);
    }
    return nb;
  }

  @Override
  public int getNumberOfParentProcessInstances(final Set<ProcessDefinitionUUID> definitionUUIDs) {
    int nb = 0;
    for (final Querier querier : queriers) {
      nb += querier.getNumberOfParentProcessInstances(definitionUUIDs);
    }
    return nb;
  }

  @Override
  public Set<InternalProcessInstance> getProcessInstancesWithTaskState(final Collection<ActivityState> activityStates,
      final Set<ProcessDefinitionUUID> definitionUUIDs) {
    final Set<InternalProcessInstance> processInsts = new HashSet<InternalProcessInstance>();
    for (final Querier querier : queriers) {
      final Set<InternalProcessInstance> tmp = querier
          .getProcessInstancesWithTaskState(activityStates, definitionUUIDs);
      if (tmp != null && !tmp.isEmpty()) {
        processInsts.addAll(tmp);
      }
    }
    return processInsts;
  }

  @Override
  public Set<InternalProcessInstance> getProcessInstancesWithInstanceStates(
      final Collection<InstanceState> instanceStates, final Set<ProcessDefinitionUUID> visibleProcessUUIDs) {
    final Set<InternalProcessInstance> processInsts = new HashSet<InternalProcessInstance>();
    for (final Querier querier : queriers) {
      final Set<InternalProcessInstance> tmp = querier.getProcessInstancesWithInstanceStates(instanceStates,
          visibleProcessUUIDs);
      if (tmp != null && !tmp.isEmpty()) {
        processInsts.addAll(tmp);
      }
    }
    return processInsts;
  }

  @Override
  public TaskInstance getOneTask(final String userId, final ActivityState taskState,
      final Set<ProcessDefinitionUUID> definitionUUIDs) {
    for (final Querier querier : queriers) {
      final TaskInstance task = querier.getOneTask(userId, taskState, definitionUUIDs);
      if (task != null) {
        return task;
      }
    }
    return null;
  }

  @Override
  public Set<TaskInstance> getUserTasks(final String userId, final ActivityState taskState,
      final Set<ProcessDefinitionUUID> definitionUUIDs) {
    final Set<TaskInstance> activities = new HashSet<TaskInstance>();
    for (final Querier querier : queriers) {
      activities.addAll(querier.getUserTasks(userId, taskState, definitionUUIDs));
    }
    return activities;
  }

  @Override
  public int getNumberOfActivityInstanceComments(final ActivityInstanceUUID activityUUID) {
    int comments = 0;
    for (final Querier querier : queriers) {
      comments += querier.getNumberOfActivityInstanceComments(activityUUID);
    }
    return comments;
  }

  @Override
  public Set<InternalProcessInstance> getUserInstances(final String userId) {
    final Set<InternalProcessInstance> processInsts = new HashSet<InternalProcessInstance>();
    for (final Querier querier : queriers) {
      final Set<InternalProcessInstance> tmp = querier.getUserInstances(userId);
      if (tmp != null && !tmp.isEmpty()) {
        processInsts.addAll(tmp);
      }
    }
    return processInsts;
  }

  @Override
  public List<InternalProcessInstance> getParentUserInstances(final String userId, final int fromIndex,
      final int pageSize) {
    final List<InternalProcessInstance> processInsts = new ArrayList<InternalProcessInstance>();
    for (final Querier querier : queriers) {
      final List<InternalProcessInstance> tmp = querier.getParentUserInstances(userId, fromIndex, pageSize);
      if (tmp != null && !tmp.isEmpty()) {
        processInsts.addAll(tmp);
      }
    }
    return processInsts;

  }

  @Override
  public List<InternalProcessInstance> getParentUserInstances(final String userId, final int startingIndex,
      final int pageSize, final ProcessInstanceCriterion pagingCriterion) {
    final List<InternalProcessInstance> processInsts = new ArrayList<InternalProcessInstance>();
    for (final Querier querier : queriers) {
      final List<InternalProcessInstance> querierInstances = querier.getParentUserInstances(userId, startingIndex,
          pageSize, pagingCriterion);
      if (querierInstances != null && !querierInstances.isEmpty()) {
        processInsts.addAll(querierInstances);
      }
    }
    return processInsts;
  }

  @Override
  public List<InternalProcessInstance> getParentProcessInstancesWithActiveUser(final String userId,
      final int fromIndex, final int pageSize, final Set<ProcessDefinitionUUID> visibleProcessUUIDs) {
    final List<InternalProcessInstance> processInsts = new ArrayList<InternalProcessInstance>();
    for (final Querier querier : queriers) {
      final List<InternalProcessInstance> tmp = querier.getParentProcessInstancesWithActiveUser(userId, fromIndex,
          pageSize, visibleProcessUUIDs);
      if (tmp != null && !tmp.isEmpty()) {
        processInsts.addAll(tmp);
      }
    }
    return processInsts;
  }

  @Override
  public List<InternalProcessInstance> getParentProcessInstancesWithActiveUser(final String userId,
      final int startingIndex, final int pageSize, final Set<ProcessDefinitionUUID> visibleProcessUUIDs,
      final ProcessInstanceCriterion pagingCriterion) {
    final List<InternalProcessInstance> processInsts = new ArrayList<InternalProcessInstance>();
    for (final Querier querier : queriers) {
      final List<InternalProcessInstance> querierProcessInsts = querier.getParentProcessInstancesWithActiveUser(userId,
          startingIndex, pageSize, visibleProcessUUIDs, pagingCriterion);
      if (querierProcessInsts != null && !querierProcessInsts.isEmpty()) {
        processInsts.addAll(querierProcessInsts);
      }
    }
    return processInsts;
  }

  @Override
  public List<InternalProcessInstance> getParentProcessInstancesWithActiveUser(final String userId,
      final int fromIndex, final int pageSize) {
    final List<InternalProcessInstance> processInsts = new ArrayList<InternalProcessInstance>();
    for (final Querier querier : queriers) {
      final List<InternalProcessInstance> tmp = querier.getParentProcessInstancesWithActiveUser(userId, fromIndex,
          pageSize);
      if (tmp != null && !tmp.isEmpty()) {
        processInsts.addAll(tmp);
      }
    }
    return processInsts;
  }

  @Override
  public List<InternalProcessInstance> getParentProcessInstancesWithActiveUser(final String userId,
      final int startingIndex, final int pageSize, final ProcessInstanceCriterion pagingCriterion) {
    final List<InternalProcessInstance> processInsts = new ArrayList<InternalProcessInstance>();
    for (final Querier querier : queriers) {
      final List<InternalProcessInstance> querierProcessInsts = querier.getParentProcessInstancesWithActiveUser(userId,
          startingIndex, pageSize, pagingCriterion);
      if (querierProcessInsts != null && !querierProcessInsts.isEmpty()) {
        processInsts.addAll(querierProcessInsts);
      }
    }
    return processInsts;
  }

  @Override
  public List<InternalProcessInstance> getParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate(
      final String userId, final Date currentDate, final Date atRisk, final int startingIndex, final int pageSize,
      final Set<ProcessDefinitionUUID> visibleProcessUUIDs) {
    final List<InternalProcessInstance> processInsts = new ArrayList<InternalProcessInstance>();
    for (final Querier querier : queriers) {
      final List<InternalProcessInstance> tmp = querier
          .getParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate(userId, currentDate, atRisk,
              startingIndex, pageSize, visibleProcessUUIDs);
      if (tmp != null && !tmp.isEmpty()) {
        processInsts.addAll(tmp);
      }
    }
    return processInsts;
  }

  @Override
  public List<InternalProcessInstance> getParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate(
      final String userId, final Date currentDate, final Date atRisk, final int startingIndex, final int pageSize,
      final Set<ProcessDefinitionUUID> visibleProcessUUIDs, final ProcessInstanceCriterion pagingCriterion) {
    final List<InternalProcessInstance> processInsts = new ArrayList<InternalProcessInstance>();
    for (final Querier querier : queriers) {
      final List<InternalProcessInstance> tmp = querier
          .getParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate(userId, currentDate, atRisk,
              startingIndex, pageSize, visibleProcessUUIDs, pagingCriterion);
      if (tmp != null && !tmp.isEmpty()) {
        processInsts.addAll(tmp);
      }
    }
    return processInsts;
  }

  @Override
  public List<InternalProcessInstance> getParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate(
      final String userId, final Date currentDate, final Date atRisk, final int startingIndex, final int pageSize) {
    final List<InternalProcessInstance> processInsts = new ArrayList<InternalProcessInstance>();
    for (final Querier querier : queriers) {
      final List<InternalProcessInstance> tmp = querier
          .getParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate(userId, currentDate, atRisk,
              startingIndex, pageSize);
      if (tmp != null && !tmp.isEmpty()) {
        processInsts.addAll(tmp);
      }
    }
    return processInsts;
  }

  @Override
  public List<InternalProcessInstance> getParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate(
      final String userId, final Date currentDate, final Date atRisk, final int startingIndex, final int pageSize,
      final ProcessInstanceCriterion pagingCriterion) {
    final List<InternalProcessInstance> processInsts = new ArrayList<InternalProcessInstance>();
    for (final Querier querier : queriers) {
      final List<InternalProcessInstance> tmp = querier
          .getParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate(userId, currentDate, atRisk,
              startingIndex, pageSize, pagingCriterion);
      if (tmp != null && !tmp.isEmpty()) {
        processInsts.addAll(tmp);
      }
    }
    return processInsts;
  }

  @Override
  public List<InternalProcessInstance> getParentProcessInstancesWithOverdueTasks(final String userId,
      final Date currentDate, final int startingIndex, final int pageSize,
      final Set<ProcessDefinitionUUID> visibleProcessUUIDs) {
    final List<InternalProcessInstance> processInsts = new ArrayList<InternalProcessInstance>();
    for (final Querier querier : queriers) {
      final List<InternalProcessInstance> tmp = querier.getParentProcessInstancesWithOverdueTasks(userId, currentDate,
          startingIndex, pageSize, visibleProcessUUIDs);
      if (tmp != null && !tmp.isEmpty()) {
        processInsts.addAll(tmp);
      }
    }
    return processInsts;
  }

  @Override
  public List<InternalProcessInstance> getParentProcessInstancesWithOverdueTasks(final String userId,
      final Date currentDate, final int startingIndex, final int pageSize,
      final Set<ProcessDefinitionUUID> visibleProcessUUIDs, final ProcessInstanceCriterion pagingCriterion) {
    final List<InternalProcessInstance> processInsts = new ArrayList<InternalProcessInstance>();
    for (final Querier querier : queriers) {
      final List<InternalProcessInstance> querierInstances = querier.getParentProcessInstancesWithOverdueTasks(userId,
          currentDate, startingIndex, pageSize, visibleProcessUUIDs, pagingCriterion);
      if (querierInstances != null && !querierInstances.isEmpty()) {
        processInsts.addAll(querierInstances);
      }
    }
    return processInsts;
  }

  @Override
  public List<InternalProcessInstance> getParentProcessInstancesWithOverdueTasks(final String userId,
      final Date currentDate, final int startingIndex, final int pageSize) {
    final List<InternalProcessInstance> processInsts = new ArrayList<InternalProcessInstance>();
    for (final Querier querier : queriers) {
      final List<InternalProcessInstance> tmp = querier.getParentProcessInstancesWithOverdueTasks(userId, currentDate,
          startingIndex, pageSize);
      if (tmp != null && !tmp.isEmpty()) {
        processInsts.addAll(tmp);
      }
    }
    return processInsts;
  }

  @Override
  public List<InternalProcessInstance> getParentProcessInstancesWithOverdueTasks(final String userId,
      final Date currentDate, final int startingIndex, final int pageSize,
      final ProcessInstanceCriterion pagingCriterion) {
    final List<InternalProcessInstance> processInsts = new ArrayList<InternalProcessInstance>();
    for (final Querier querier : queriers) {
      final List<InternalProcessInstance> tmp = querier.getParentProcessInstancesWithOverdueTasks(userId, currentDate,
          startingIndex, pageSize, pagingCriterion);
      if (tmp != null && !tmp.isEmpty()) {
        processInsts.addAll(tmp);
      }
    }
    return processInsts;
  }

  @Override
  public List<InternalProcessInstance> getParentProcessInstancesWithInvolvedUser(final String userId,
      final int fromIndex, final int pageSize, final Set<ProcessDefinitionUUID> visibleProcessUUIDs) {
    final List<InternalProcessInstance> processInsts = new ArrayList<InternalProcessInstance>();
    for (final Querier querier : queriers) {
      final List<InternalProcessInstance> tmp = querier.getParentProcessInstancesWithInvolvedUser(userId, fromIndex,
          pageSize, visibleProcessUUIDs);
      if (tmp != null && !tmp.isEmpty()) {
        processInsts.addAll(tmp);
      }
    }
    return processInsts;
  }

  @Override
  public List<InternalProcessInstance> getParentProcessInstancesWithInvolvedUser(final String userId,
      final int startingIndex, final int pageSize, final Set<ProcessDefinitionUUID> visibleProcessUUIDs,
      final ProcessInstanceCriterion pagingCriterion) {
    final List<InternalProcessInstance> processInsts = new ArrayList<InternalProcessInstance>();
    for (final Querier querier : queriers) {
      final List<InternalProcessInstance> tmp = querier.getParentProcessInstancesWithInvolvedUser(userId,
          startingIndex, pageSize, visibleProcessUUIDs, pagingCriterion);
      if (tmp != null && !tmp.isEmpty()) {
        processInsts.addAll(tmp);
      }
    }
    return processInsts;
  }

  @Override
  public List<InternalProcessInstance> getParentProcessInstancesWithInvolvedUser(final String userId,
      final int fromIndex, final int pageSize) {
    final List<InternalProcessInstance> processInsts = new ArrayList<InternalProcessInstance>();
    for (final Querier querier : queriers) {
      final List<InternalProcessInstance> tmp = querier.getParentProcessInstancesWithInvolvedUser(userId, fromIndex,
          pageSize);
      if (tmp != null && !tmp.isEmpty()) {
        processInsts.addAll(tmp);
      }
    }
    return processInsts;
  }

  @Override
  public List<InternalProcessInstance> getParentProcessInstancesWithInvolvedUser(final String userId,
      final int startingIndex, final int pageSize, final ProcessInstanceCriterion pagingCriterion) {
    final List<InternalProcessInstance> processInsts = new ArrayList<InternalProcessInstance>();
    for (final Querier querier : queriers) {
      final List<InternalProcessInstance> tmp = querier.getParentProcessInstancesWithInvolvedUser(userId,
          startingIndex, pageSize, pagingCriterion);
      if (tmp != null && !tmp.isEmpty()) {
        processInsts.addAll(tmp);
      }
    }
    return processInsts;
  }

  @Override
  public List<InternalProcessInstance> getParentUserInstances(final String userId, final int fromIndex,
      final int pageSize, final Set<ProcessDefinitionUUID> definitionUUIDs) {
    final List<InternalProcessInstance> processInsts = new ArrayList<InternalProcessInstance>();
    for (final Querier querier : queriers) {
      final List<InternalProcessInstance> tmp = querier.getParentUserInstances(userId, fromIndex, pageSize,
          definitionUUIDs);
      if (tmp != null && !tmp.isEmpty()) {
        processInsts.addAll(tmp);
      }
    }
    return processInsts;
  }

  @Override
  public List<InternalProcessInstance> getParentUserInstances(final String userId, final int startingIndex,
      final int pageSize, final Set<ProcessDefinitionUUID> definitionUUIDs,
      final ProcessInstanceCriterion pagingCriterion) {
    final List<InternalProcessInstance> processInsts = new ArrayList<InternalProcessInstance>();
    for (final Querier querier : queriers) {
      final List<InternalProcessInstance> tmp = querier.getParentUserInstances(userId, startingIndex, pageSize,
          definitionUUIDs, pagingCriterion);
      if (tmp != null && !tmp.isEmpty()) {
        processInsts.addAll(tmp);
      }
    }
    return processInsts;
  }

  @Override
  public Integer getNumberOfParentProcessInstancesWithActiveUser(final String userId,
      final Set<ProcessDefinitionUUID> visibleProcessUUIDs) {
    Integer result = 0;
    for (final Querier querier : queriers) {
      result += querier.getNumberOfParentProcessInstancesWithActiveUser(userId, visibleProcessUUIDs);
    }
    return result;
  }

  @Override
  public Integer getNumberOfParentProcessInstancesWithActiveUser(final String userId) {
    Integer result = 0;
    for (final Querier querier : queriers) {
      result += querier.getNumberOfParentProcessInstancesWithActiveUser(userId);
    }
    return result;
  }

  @Override
  public Integer getNumberOfParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate(final String userId,
      final Date currentDate, final Date atRisk, final Set<ProcessDefinitionUUID> visibleProcessUUIDs) {
    Integer result = 0;
    for (final Querier querier : queriers) {
      result += querier.getNumberOfParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate(userId,
          currentDate, atRisk);
    }
    return result;
  }

  @Override
  public Integer getNumberOfParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate(final String userId,
      final Date currentDate, final Date atRisk) {
    Integer result = 0;
    for (final Querier querier : queriers) {
      result += querier.getNumberOfParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate(userId,
          currentDate, atRisk);
    }
    return result;
  }

  @Override
  public Integer getNumberOfParentProcessInstancesWithOverdueTasks(final String userId, final Date currentDate,
      final Set<ProcessDefinitionUUID> visibleProcessUUIDs) {
    Integer result = 0;
    for (final Querier querier : queriers) {
      result += querier.getNumberOfParentProcessInstancesWithOverdueTasks(userId, currentDate, visibleProcessUUIDs);
    }
    return result;
  }

  @Override
  public Integer getNumberOfParentProcessInstancesWithOverdueTasks(final String userId, final Date currentDate) {
    Integer result = 0;
    for (final Querier querier : queriers) {
      result += querier.getNumberOfParentProcessInstancesWithOverdueTasks(userId, currentDate);
    }
    return result;
  }

  @Override
  public Integer getNumberOfParentProcessInstancesWithInvolvedUser(final String userId,
      final Set<ProcessDefinitionUUID> visibleProcessUUIDs) {
    Integer result = 0;
    for (final Querier querier : queriers) {
      result += querier.getNumberOfParentProcessInstancesWithInvolvedUser(userId, visibleProcessUUIDs);
    }
    return result;
  }

  @Override
  public Integer getNumberOfParentProcessInstancesWithInvolvedUser(final String userId) {
    Integer result = 0;
    for (final Querier querier : queriers) {
      result += querier.getNumberOfParentProcessInstancesWithInvolvedUser(userId);
    }
    return result;
  }

  @Override
  public Integer getNumberOfParentProcessInstancesWithStartedBy(final String userId,
      final Set<ProcessDefinitionUUID> visibleProcessUUIDs) {
    Integer result = 0;
    for (final Querier querier : queriers) {
      result += querier.getNumberOfParentProcessInstancesWithStartedBy(userId, visibleProcessUUIDs);
    }
    return result;
  }

  @Override
  public Integer getNumberOfParentProcessInstancesWithStartedBy(final String userId) {
    Integer result = 0;
    for (final Querier querier : queriers) {
      result += querier.getNumberOfParentProcessInstancesWithStartedBy(userId);
    }
    return result;
  }

  @Override
  public Map<ActivityInstanceUUID, Integer> getNumberOfActivityInstanceComments(
      final Set<ActivityInstanceUUID> activityUUIDs) {
    final Map<ActivityInstanceUUID, Integer> result = new HashMap<ActivityInstanceUUID, Integer>();
    for (final Querier querier : queriers) {
      result.putAll(querier.getNumberOfActivityInstanceComments(activityUUIDs));
    }
    return result;
  }

  @Override
  public int getNumberOfComments(final ProcessInstanceUUID instanceUUID) {
    int comments = 0;
    for (final Querier querier : queriers) {
      comments += querier.getNumberOfComments(instanceUUID);
    }
    return comments;
  }

  @Override
  public List<Comment> getCommentFeed(final ProcessInstanceUUID instanceUUID) {
    final List<Comment> comments = new ArrayList<Comment>();
    for (final Querier querier : queriers) {
      comments.addAll(querier.getCommentFeed(instanceUUID));
    }
    return comments;
  }

  @Override
  public int getNumberOfProcessInstanceComments(final ProcessInstanceUUID instanceUUID) {
    int comments = 0;
    for (final Querier querier : queriers) {
      comments += querier.getNumberOfProcessInstanceComments(instanceUUID);
    }
    return comments;
  }

  @Override
  public List<Comment> getProcessInstanceCommentFeed(final ProcessInstanceUUID instanceUUID) {
    final List<Comment> comments = new ArrayList<Comment>();
    for (final Querier querier : queriers) {
      comments.addAll(querier.getProcessInstanceCommentFeed(instanceUUID));
    }
    return comments;
  }

  @Override
  public List<Comment> getActivityInstanceCommentFeed(final ActivityInstanceUUID activityUUID) {
    final List<Comment> comments = new ArrayList<Comment>();
    for (final Querier querier : queriers) {
      comments.addAll(querier.getActivityInstanceCommentFeed(activityUUID));
    }
    return comments;
  }

  @Override
  public Set<ProcessDefinitionUUID> getAllProcessDefinitionUUIDs() {
    final Set<ProcessDefinitionUUID> set = new HashSet<ProcessDefinitionUUID>();
    for (final Querier querier : queriers) {
      final Set<ProcessDefinitionUUID> tmp = querier.getAllProcessDefinitionUUIDs();
      if (tmp != null && !tmp.isEmpty()) {
        set.addAll(tmp);
      }
    }

    return set;
  }

  @Override
  public Set<ProcessDefinitionUUID> getAllProcessDefinitionUUIDsExcept(final Set<ProcessDefinitionUUID> processUUIDs) {
    final Set<ProcessDefinitionUUID> set = new HashSet<ProcessDefinitionUUID>();
    for (final Querier querier : queriers) {
      final Set<ProcessDefinitionUUID> tmp = querier.getAllProcessDefinitionUUIDsExcept(processUUIDs);
      if (tmp != null && !tmp.isEmpty()) {
        set.addAll(tmp);
      }
    }

    return set;
  }

  // /////////////////////////////////////////
  // //////// PAGINATION OPERATIONS //////////
  // /////////////////////////////////////////

  @Override
  public List<InternalProcessInstance> getProcessInstances(final int fromIndex, final int pageSize) {
    final List<InternalProcessInstance> processInsts = new ArrayList<InternalProcessInstance>();
    if (queriers.size() == 1) {
      // can perform directly the query
      final Querier querier = queriers.get(0);
      processInsts.addAll(querier.getProcessInstances(fromIndex, pageSize));
      return processInsts;
    }
    for (final Querier querier : queriers) {
      if (processInsts.size() < fromIndex + pageSize) {
        // buid a set with at least all the first elements
        processInsts.addAll(querier.getProcessInstances(0, fromIndex + pageSize));
      } else {
        mergePaginatedProcessInstances(processInsts,
            querier.getMostRecentProcessInstances(fromIndex + pageSize, getOldestTime(processInsts)), querier,
            fromIndex, pageSize, ProcessInstanceCriterion.DEFAULT);
      }
    }
    return getInstancesSubset(fromIndex, pageSize, processInsts, ProcessInstanceCriterion.DEFAULT);
  }

  @Override
  public List<InternalProcessInstance> getProcessInstances(final int fromIndex, final int pageSize,
      final ProcessInstanceCriterion paginCriterion) {
    final List<InternalProcessInstance> processInsts = new ArrayList<InternalProcessInstance>();
    if (queriers.size() == 1) {
      // can perform directly the query
      final Querier querier = queriers.get(0);
      processInsts.addAll(querier.getProcessInstances(fromIndex, pageSize, paginCriterion));
      return processInsts;
    }
    for (final Querier querier : queriers) {
      if (processInsts.size() < fromIndex + pageSize) {
        // build a set with at least all the first elements
        processInsts.addAll(querier.getProcessInstances(0, fromIndex + pageSize, paginCriterion));
      } else {
        mergePaginatedProcessInstances(processInsts,
            querier.getMostRecentProcessInstances(fromIndex + pageSize, getOldestTime(processInsts), paginCriterion),
            querier, fromIndex, pageSize, paginCriterion);
      }
    }

    return getInstancesSubset(fromIndex, pageSize, processInsts, paginCriterion);
  }

  private long getOldestTime(final List<InternalProcessInstance> processInsts) {
    return processInsts.get(processInsts.size() - 1).getLastUpdate().getTime();
  }

  private void mergePaginatedProcessInstances(List<InternalProcessInstance> processInsts,
      final List<InternalProcessInstance> newProcessInstances, final Querier querier, final int fromIndex,
      final int pageSize, final ProcessInstanceCriterion pagingCriterion) {

    Comparator<InternalProcessInstance> comparator = null;
    switch (pagingCriterion) {
      case LAST_UPDATE_ASC:
        comparator = new ProcessInstanceLastUpdateComparatorAsc();
        break;
      case STARTED_DATE_ASC:
        comparator = new ProcessInstanceStartedDateComparatorAsc();
        break;
      case ENDED_DATE_ASC:
        comparator = new ProcessInstanceEndedDateComparatorAsc();
        break;
      case INSTANCE_NUMBER_ASC:
        comparator = new ProcessInstanceNbComparatorAsc();
        break;
      case INSTANCE_UUID_ASC:
        comparator = new ProcessInstanceUUIDComparatorAsc();
        break;
      case LAST_UPDATE_DESC:
        comparator = new ProcessInstanceLastUpdateComparator();
        break;
      case STARTED_DATE_DESC:
        comparator = new ProcessInstanceStartedDateComparatorDesc();
        break;
      case ENDED_DATE_DESC:
        comparator = new ProcessInstanceEndedDateComparatorDesc();
        break;
      case INSTANCE_NUMBER_DESC:
        comparator = new ProcessInstanceNbComparatorDesc();
        break;
      case INSTANCE_UUID_DESC:
        comparator = new ProcessInstanceUUIDComparatorDesc();
        break;
      case DEFAULT:
        comparator = new ProcessInstanceLastUpdateComparator();
        break;
    }
    // retrieve maximum (maybe less) fromIndex+pageSize elements.
    // All returned elements must be younger or equal to the older elements of the current list
    if (!newProcessInstances.isEmpty()) {
      processInsts.addAll(newProcessInstances);
      // sort the list and keep only fromIndex+pageSize elements
      Collections.sort(processInsts, comparator);
      processInsts = Misc.subList(InternalProcessInstance.class, processInsts, 0, fromIndex + pageSize);
    }
  }

  @Override
  public List<InternalProcessInstance> getMostRecentProcessInstances(final int maxResults, final long time) {
    final List<InternalProcessInstance> processInsts = new ArrayList<InternalProcessInstance>();
    for (final Querier querier : queriers) {
      processInsts.addAll(querier.getMostRecentProcessInstances(maxResults, time));
    }
    return processInsts;
  }

  @Override
  public List<InternalProcessInstance> getMostRecentProcessInstances(final int maxResults, final long time,
      final ProcessInstanceCriterion pagingCriterion) {
    final List<InternalProcessInstance> processInsts = new ArrayList<InternalProcessInstance>();
    for (final Querier querier : queriers) {
      processInsts.addAll(querier.getMostRecentProcessInstances(maxResults, time, pagingCriterion));
    }
    return processInsts;
  }

  @Override
  public List<InternalProcessInstance> getMostRecentParentProcessInstances(final int maxResults, final long time) {
    final List<InternalProcessInstance> processInsts = new ArrayList<InternalProcessInstance>();
    for (final Querier querier : queriers) {
      processInsts.addAll(querier.getMostRecentParentProcessInstances(maxResults, time));
    }
    return processInsts;
  }

  @Override
  public List<InternalProcessInstance> getMostRecentParentProcessInstances(final int maxResults, final long time,
      final ProcessInstanceCriterion pagingCriterion) {
    final List<InternalProcessInstance> processInsts = new ArrayList<InternalProcessInstance>();
    for (final Querier querier : queriers) {
      processInsts.addAll(querier.getMostRecentParentProcessInstances(maxResults, time, pagingCriterion));
    }
    return processInsts;
  }

  @Override
  public List<InternalProcessInstance> getParentProcessInstances(final ProcessDefinitionUUID definitionUUID,
      final int fromIndex, final int pageSize) {
    final List<InternalProcessInstance> processInsts = new ArrayList<InternalProcessInstance>();
    for (final Querier querier : queriers) {
      processInsts.addAll(querier.getParentProcessInstances(definitionUUID, fromIndex, pageSize));
    }
    return processInsts;
  }

  @Override
  public List<InternalProcessInstance> getParentProcessInstances(final int fromIndex, final int pageSize) {
    final List<InternalProcessInstance> processInsts = new ArrayList<InternalProcessInstance>();
    if (queriers.size() == 1) {
      // can perform directly the query
      final Querier querier = queriers.get(0);
      processInsts.addAll(querier.getParentProcessInstances(fromIndex, pageSize));
      return processInsts;
    }
    for (final Querier querier : queriers) {
      if (processInsts.size() < fromIndex + pageSize) {
        // buid a set with at least all the first elements
        processInsts.addAll(querier.getParentProcessInstances(0, fromIndex + pageSize));
      } else {
        mergePaginatedProcessInstances(processInsts,
            querier.getMostRecentParentProcessInstances(fromIndex + pageSize, getOldestTime(processInsts)), querier,
            fromIndex, pageSize, ProcessInstanceCriterion.DEFAULT);
      }
    }
    return getInstancesSubset(fromIndex, pageSize, processInsts, ProcessInstanceCriterion.DEFAULT);
  }

  @Override
  public List<InternalProcessInstance> getParentProcessInstances(final int fromIndex, final int pageSize,
      final ProcessInstanceCriterion pagingCriterion) {
    final List<InternalProcessInstance> processInsts = new ArrayList<InternalProcessInstance>();
    if (queriers.size() == 1) {
      // can perform directly the query
      final Querier querier = queriers.get(0);
      processInsts.addAll(querier.getParentProcessInstances(fromIndex, pageSize, pagingCriterion));
      return processInsts;
    }
    for (final Querier querier : queriers) {
      if (processInsts.size() < fromIndex + pageSize) {
        // buid a set with at least all the first elements
        processInsts.addAll(querier.getParentProcessInstances(0, fromIndex + pageSize, pagingCriterion));
      } else {
        mergePaginatedProcessInstances(processInsts, querier.getMostRecentParentProcessInstances(fromIndex + pageSize,
            getOldestTime(processInsts), pagingCriterion), querier, fromIndex, pageSize, pagingCriterion);
      }
    }
    return getInstancesSubset(fromIndex, pageSize, processInsts, pagingCriterion);
  }

  @Override
  public List<InternalProcessInstance> getParentProcessInstances(final Set<ProcessDefinitionUUID> processUUIDs,
      final int fromIndex, final int pageSize, final ProcessInstanceCriterion pagingCriterion) {
    if (processUUIDs == null || processUUIDs.isEmpty()) {
      return Collections.emptyList();
    }
    final List<InternalProcessInstance> processInsts = new ArrayList<InternalProcessInstance>();
    if (queriers.size() == 1) {
      // can perform directly the query
      final Querier querier = queriers.get(0);
      processInsts.addAll(querier.getParentProcessInstances(processUUIDs, fromIndex, pageSize, pagingCriterion));
      return processInsts;
    }

    for (final Querier querier : queriers) {
      processInsts.addAll(querier.getParentProcessInstances(processUUIDs, fromIndex, pageSize, pagingCriterion));
    }
    Collections.sort(processInsts, new InternalProcessInstanceComparator(pagingCriterion));

    return Misc.subList(InternalProcessInstance.class, processInsts, 0, pageSize);
  }

  @Override
  public List<InternalProcessInstance> getParentProcessInstancesExcept(final Set<ProcessDefinitionUUID> exceptions,
      final int fromIndex, final int pageSize, final ProcessInstanceCriterion pagingCriterion) {
    if (exceptions == null || exceptions.isEmpty()) {
      return getParentProcessInstances(fromIndex, pageSize, pagingCriterion);
    }
    final List<InternalProcessInstance> processInsts = new ArrayList<InternalProcessInstance>();
    if (queriers.size() == 1) {
      // can perform directly the query
      final Querier querier = queriers.get(0);
      processInsts.addAll(querier.getParentProcessInstancesExcept(exceptions, fromIndex, pageSize, pagingCriterion));
      return processInsts;
    }

    for (final Querier querier : queriers) {
      processInsts.addAll(querier.getParentProcessInstancesExcept(exceptions, fromIndex, pageSize, pagingCriterion));
    }
    Collections.sort(processInsts, new InternalProcessInstanceComparator(pagingCriterion));

    return Misc.subList(InternalProcessInstance.class, processInsts, 0, pageSize);
  }

  @Override
  public List<InternalProcessInstance> getMostRecentMatchingProcessInstances(
      final Collection<ProcessInstanceUUID> instanceUUIDs, final int maxResults, final long time) {
    final List<InternalProcessInstance> processInsts = new ArrayList<InternalProcessInstance>();
    for (final Querier querier : queriers) {
      processInsts.addAll(querier.getMostRecentMatchingProcessInstances(instanceUUIDs, maxResults, time));
    }
    return processInsts;
  }

  @Override
  public List<InternalProcessInstance> getMostRecentMatchingProcessInstances(
      final Set<ProcessInstanceUUID> instanceUUIDs, final int maxResults, final long time,
      final ProcessInstanceCriterion pagingCriterion) {
    final List<InternalProcessInstance> processInsts = new ArrayList<InternalProcessInstance>();
    for (final Querier querier : queriers) {
      processInsts.addAll(querier.getMostRecentMatchingProcessInstances(instanceUUIDs, maxResults, time,
          pagingCriterion));
    }
    return processInsts;
  }

  @Override
  public List<InternalProcessInstance> getProcessInstances(final Collection<ProcessInstanceUUID> instanceUUIDs,
      final int fromIndex, final int pageSize) {
    final List<InternalProcessInstance> processInsts = new ArrayList<InternalProcessInstance>();
    if (queriers.size() == 1) {
      // can perform directly the query
      final Querier querier = queriers.get(0);
      processInsts.addAll(querier.getProcessInstances(instanceUUIDs, fromIndex, pageSize));
      return processInsts;
    }
    for (final Querier querier : queriers) {
      if (processInsts.size() < fromIndex + pageSize) {
        // buid a set with at least all the first elements
        processInsts.addAll(querier.getProcessInstances(instanceUUIDs, 0, fromIndex + pageSize));
      } else {
        mergePaginatedProcessInstances(processInsts, querier.getMostRecentMatchingProcessInstances(instanceUUIDs,
            fromIndex + pageSize, getOldestTime(processInsts)), querier, fromIndex, pageSize,
            ProcessInstanceCriterion.DEFAULT);
      }
    }
    return getInstancesSubset(fromIndex, pageSize, processInsts, ProcessInstanceCriterion.DEFAULT);
  }

  @Override
  public List<InternalProcessInstance> getProcessInstancesWithInstanceUUIDs(
      final Set<ProcessInstanceUUID> instanceUUIDs, final int fromIndex, final int pageSize,
      final ProcessInstanceCriterion pagingCriterion) {
    final List<InternalProcessInstance> processInsts = new ArrayList<InternalProcessInstance>();
    if (queriers.size() == 1) {
      // can perform directly the query
      final Querier querier = queriers.get(0);
      processInsts.addAll(querier.getProcessInstancesWithInstanceUUIDs(instanceUUIDs, fromIndex, pageSize,
          pagingCriterion));
      return processInsts;
    }
    for (final Querier querier : queriers) {
      if (processInsts.size() < fromIndex + pageSize) {
        // buid a set with at least all the first elements
        processInsts.addAll(querier.getProcessInstancesWithInstanceUUIDs(instanceUUIDs, 0, fromIndex + pageSize,
            pagingCriterion));
      } else {
        mergePaginatedProcessInstances(processInsts, querier.getMostRecentMatchingProcessInstances(instanceUUIDs,
            fromIndex + pageSize, getOldestTime(processInsts), pagingCriterion), querier, fromIndex, pageSize,
            pagingCriterion);
      }
    }
    return getInstancesSubset(fromIndex, pageSize, processInsts, pagingCriterion);
  }

  @Override
  public List<InternalProcessInstance> getMostRecentProcessesProcessInstances(
      final Collection<ProcessDefinitionUUID> definitionUUIDs, final int maxResults, final long time) {
    final List<InternalProcessInstance> processInsts = new ArrayList<InternalProcessInstance>();
    for (final Querier querier : queriers) {
      processInsts.addAll(querier.getMostRecentProcessesProcessInstances(definitionUUIDs, maxResults, time));
    }
    return processInsts;
  }

  @Override
  public List<InternalProcessInstance> getMostRecentProcessesProcessInstances(
      final Collection<ProcessDefinitionUUID> definitionUUIDs, final int maxResults, final long time,
      final ProcessInstanceCriterion pagingCriterion) {
    final List<InternalProcessInstance> processInsts = new ArrayList<InternalProcessInstance>();
    for (final Querier querier : queriers) {
      processInsts.addAll(querier.getMostRecentProcessesProcessInstances(definitionUUIDs, maxResults, time,
          pagingCriterion));
    }
    return processInsts;
  }

  @Override
  public List<InternalProcessInstance> getProcessInstances(final Set<ProcessDefinitionUUID> definitionUUIDs,
      final int fromIndex, final int pageSize) {
    final List<InternalProcessInstance> processInsts = new ArrayList<InternalProcessInstance>();
    if (queriers.size() == 1) {
      // can perform directly the query
      final Querier querier = queriers.get(0);
      processInsts.addAll(querier.getProcessInstances(definitionUUIDs, fromIndex, pageSize));
      return processInsts;
    }
    for (final Querier querier : queriers) {
      if (processInsts.size() < fromIndex + pageSize) {
        // buid a set with at least all the first elements
        processInsts.addAll(querier.getProcessInstances(definitionUUIDs, 0, fromIndex + pageSize));
      } else {
        mergePaginatedProcessInstances(processInsts, querier.getMostRecentProcessesProcessInstances(definitionUUIDs,
            fromIndex + pageSize, getOldestTime(processInsts)), querier, fromIndex, pageSize,
            ProcessInstanceCriterion.DEFAULT);
      }
    }
    return getInstancesSubset(fromIndex, pageSize, processInsts, ProcessInstanceCriterion.DEFAULT);
  }

  @Override
  public List<InternalProcessInstance> getProcessInstances(final Set<ProcessDefinitionUUID> definitionUUIDs,
      final int fromIndex, final int pageSize, final ProcessInstanceCriterion pagingCriterion) {
    final List<InternalProcessInstance> processInsts = new ArrayList<InternalProcessInstance>();
    if (queriers.size() == 1) {
      // can perform directly the query
      final Querier querier = queriers.get(0);
      processInsts.addAll(querier.getProcessInstances(definitionUUIDs, fromIndex, pageSize, pagingCriterion));
      return processInsts;
    }
    for (final Querier querier : queriers) {
      if (processInsts.size() < fromIndex + pageSize) {
        // buid a set with at least all the first elements
        processInsts.addAll(querier.getProcessInstances(definitionUUIDs, 0, fromIndex + pageSize, pagingCriterion));
      } else {
        mergePaginatedProcessInstances(processInsts, querier.getMostRecentProcessesProcessInstances(definitionUUIDs,
            fromIndex + pageSize, getOldestTime(processInsts), pagingCriterion), querier, fromIndex, pageSize,
            pagingCriterion);
      }
    }
    return getInstancesSubset(fromIndex, pageSize, processInsts, pagingCriterion);
  }

  private List<InternalProcessInstance> getInstancesSubset(final int fromIndex, final int pageSize,
      final List<InternalProcessInstance> processInstances, final ProcessInstanceCriterion pagingCriterion) {
    if (processInstances == null || processInstances.isEmpty() || fromIndex > processInstances.size()) {
      return Collections.emptyList();
    }
    int toIndex = fromIndex + pageSize;
    if (toIndex > processInstances.size()) {
      toIndex = processInstances.size();
    }
    Comparator<InternalProcessInstance> comparator = null;
    switch (pagingCriterion) {
      case LAST_UPDATE_ASC:
        comparator = new ProcessInstanceLastUpdateComparatorAsc();
        break;
      case STARTED_DATE_ASC:
        comparator = new ProcessInstanceStartedDateComparatorAsc();
        break;
      case ENDED_DATE_ASC:
        comparator = new ProcessInstanceEndedDateComparatorAsc();
        break;
      case INSTANCE_NUMBER_ASC:
        comparator = new ProcessInstanceNbComparatorAsc();
        break;
      case INSTANCE_UUID_ASC:
        comparator = new ProcessInstanceUUIDComparatorAsc();
        break;
      case LAST_UPDATE_DESC:
        comparator = new ProcessInstanceLastUpdateComparator();
        break;
      case STARTED_DATE_DESC:
        comparator = new ProcessInstanceStartedDateComparatorDesc();
        break;
      case ENDED_DATE_DESC:
        comparator = new ProcessInstanceEndedDateComparatorDesc();
        break;
      case INSTANCE_NUMBER_DESC:
        comparator = new ProcessInstanceNbComparatorDesc();
        break;
      case INSTANCE_UUID_DESC:
        comparator = new ProcessInstanceUUIDComparatorDesc();
        break;
      case DEFAULT:
        comparator = new ProcessInstanceLastUpdateComparator();
        break;
    }
    Collections.sort(processInstances, comparator);
    return Misc.subList(InternalProcessInstance.class, processInstances, fromIndex, toIndex);
  }

  @Override
  public List<InternalProcessDefinition> getProcesses(final int fromIndex, final int pageSize) {
    final List<InternalProcessDefinition> processes = new ArrayList<InternalProcessDefinition>();
    if (queriers.size() == 1) {
      // can perform directly the query
      final Querier querier = queriers.get(0);
      processes.addAll(querier.getProcesses(fromIndex, pageSize));
      return processes;
    }
    for (final Querier querier : queriers) {
      processes.addAll(querier.getProcesses(0, fromIndex + pageSize));
    }
    return getProcessesSubset(fromIndex, pageSize, processes, ProcessDefinitionCriterion.DEFAULT);
  }

  @Override
  public List<InternalProcessDefinition> getProcesses(final int fromIndex, final int pageSize,
      final ProcessDefinitionCriterion pagingCriterion) {
    final List<InternalProcessDefinition> processes = new ArrayList<InternalProcessDefinition>();
    if (queriers.size() == 1) {
      // can perform directly the query
      final Querier querier = queriers.get(0);
      processes.addAll(querier.getProcesses(fromIndex, pageSize, pagingCriterion));
      return processes;
    }
    for (final Querier querier : queriers) {
      processes.addAll(querier.getProcesses(0, fromIndex + pageSize, pagingCriterion));
    }
    return getProcessesSubset(fromIndex, pageSize, processes, pagingCriterion);
  }

  @Override
  public List<InternalProcessDefinition> getProcesses(final Set<ProcessDefinitionUUID> definitionUUIDs,
      final int fromIndex, final int pageSize) {
    final List<InternalProcessDefinition> processes = new ArrayList<InternalProcessDefinition>();
    if (queriers.size() == 1) {
      // can perform directly the query
      final Querier querier = queriers.get(0);
      processes.addAll(querier.getProcesses(definitionUUIDs, fromIndex, pageSize));
      return processes;
    }
    for (final Querier querier : queriers) {
      processes.addAll(querier.getProcesses(definitionUUIDs, 0, fromIndex + pageSize));
    }
    return getProcessesSubset(fromIndex, pageSize, processes, ProcessDefinitionCriterion.DEFAULT);
  }

  @Override
  public List<InternalProcessDefinition> getProcesses(final Set<ProcessDefinitionUUID> definitionUUIDs,
      final int fromIndex, final int pageSize, final ProcessDefinitionCriterion pagingCriterion) {
    final List<InternalProcessDefinition> processes = new ArrayList<InternalProcessDefinition>();
    if (queriers.size() == 1) {
      // can perform directly the query
      final Querier querier = queriers.get(0);
      processes.addAll(querier.getProcesses(definitionUUIDs, fromIndex, pageSize, pagingCriterion));
      return processes;
    }
    for (final Querier querier : queriers) {
      processes.addAll(querier.getProcesses(definitionUUIDs, 0, fromIndex + pageSize, pagingCriterion));
    }
    return getProcessesSubset(fromIndex, pageSize, processes, pagingCriterion);
  }

  @Override
  public List<InternalProcessDefinition> getProcessesExcept(final Set<ProcessDefinitionUUID> processUUIDs,
      final int fromIndex, final int pageSize) {
    final List<InternalProcessDefinition> processes = new ArrayList<InternalProcessDefinition>();
    if (queriers.size() == 1) {
      // can perform directly the query
      final Querier querier = queriers.get(0);
      processes.addAll(querier.getProcessesExcept(processUUIDs, fromIndex, pageSize));
      return processes;
    }
    for (final Querier querier : queriers) {
      processes.addAll(querier.getProcessesExcept(processUUIDs, 0, fromIndex + pageSize));
    }
    return getProcessesSubset(fromIndex, pageSize, processes, ProcessDefinitionCriterion.DEFAULT);
  }

  @Override
  public List<InternalProcessDefinition> getProcessesExcept(final Set<ProcessDefinitionUUID> processUUIDs,
      final int fromIndex, final int pageSize, final ProcessDefinitionCriterion pagingCriterion) {
    final List<InternalProcessDefinition> processes = new ArrayList<InternalProcessDefinition>();
    if (queriers.size() == 1) {
      // can perform directly the query
      final Querier querier = queriers.get(0);
      processes.addAll(querier.getProcessesExcept(processUUIDs, fromIndex, pageSize, pagingCriterion));
      return processes;
    }
    for (final Querier querier : queriers) {
      processes.addAll(querier.getProcessesExcept(processUUIDs, 0, fromIndex + pageSize, pagingCriterion));
    }
    return getProcessesSubset(fromIndex, pageSize, processes, pagingCriterion);
  }

  @Override
  public Set<Category> getCategories(final Collection<String> categoryNames) {
    final Set<Category> result = new HashSet<Category>();
    for (final Querier querier : queriers) {
      result.addAll(querier.getCategories(categoryNames));
    }
    return result;
  }

  @Override
  public Set<Category> getAllCategories() {
    final Set<Category> result = new HashSet<Category>();
    for (final Querier querier : queriers) {
      result.addAll(querier.getAllCategories());
    }
    return result;
  }

  @Override
  public Set<Category> getAllCategoriesExcept(final Set<String> uuids) {
    final Set<Category> result = new HashSet<Category>();
    for (final Querier querier : queriers) {
      result.addAll(querier.getAllCategoriesExcept(uuids));
    }
    return result;
  }

  @Override
  public Set<CategoryImpl> getCategoriesByUUIDs(final Set<CategoryUUID> uuids) {
    final Set<CategoryImpl> result = new HashSet<CategoryImpl>();
    for (final Querier querier : queriers) {
      result.addAll(querier.getCategoriesByUUIDs(uuids));
    }
    return result;
  }

  @Override
  public CategoryImpl getCategoryByUUID(final String uuid) {
    CategoryImpl category = null;
    int i = 0;
    Querier querier;
    while (category == null && i < queriers.size()) {
      querier = queriers.get(i);
      category = querier.getCategoryByUUID(uuid);
      i++;
    }
    return category;
  }

  @Override
  public Set<ProcessDefinitionUUID> getProcessUUIDsFromCategory(final String category) {
    final Set<ProcessDefinitionUUID> result = new HashSet<ProcessDefinitionUUID>();
    for (final Querier querier : queriers) {
      result.addAll(querier.getProcessUUIDsFromCategory(category));
    }
    return result;
  }

  private List<InternalProcessDefinition> getProcessesSubset(final int fromIndex, final int pageSize,
      final List<InternalProcessDefinition> processes, final ProcessDefinitionCriterion pagingCriterion) {
    int toIndex = fromIndex + pageSize;
    if (toIndex > processes.size()) {
      toIndex = processes.size();
    }
    Collections.sort(processes, new InternalProcessDefinitionComparator(pagingCriterion));
    return Misc.subList(InternalProcessDefinition.class, processes, fromIndex, toIndex);
  }

  @Override
  public List<Object> search(final SearchQueryBuilder query, final int firstResult, final int maxResults,
      final Class<?> indexClass) {
    List<Object> entities = new ArrayList<Object>();
    if (queriers.size() == 1) {
      final Querier querier = queriers.get(0);
      entities.addAll(querier.search(query, firstResult, maxResults, indexClass));
    } else {
      for (final Querier querier : queriers) {
        final List<Object> temp = querier.search(query, firstResult, maxResults, indexClass);
        entities.addAll(temp);
        if (entities.size() > maxResults) {
          entities = entities.subList(firstResult, maxResults);
          break;
        }
      }
    }
    return entities;
  }

  @Override
  public int search(final SearchQueryBuilder query, final Class<?> indexClass) {
    int count = 0;
    for (final Querier querier : queriers) {
      count += querier.search(query, indexClass);
    }
    return count;
  }

  @Override
  public Set<ActivityDefinitionUUID> getProcessTaskUUIDs(final ProcessDefinitionUUID definitionUUID) {
    final Set<ActivityDefinitionUUID> result = new HashSet<ActivityDefinitionUUID>();
    for (final Querier querier : queriers) {
      result.addAll(querier.getProcessTaskUUIDs(definitionUUID));
    }
    return result;
  }

  @Override
  public boolean processExists(final ProcessDefinitionUUID definitionUUID) {
    for (final Querier querier : queriers) {
      final boolean querierExists = querier.processExists(definitionUUID);
      if (querierExists) {
        return true;
      }
    }
    return false;
  }

  @Override
  public List<Long> getProcessInstancesDuration(final Date since, final Date until) {
    final List<Long> durations = new ArrayList<Long>();
    for (final Querier querier : queriers) {
      durations.addAll(querier.getProcessInstancesDuration(since, until));
    }
    Collections.sort(durations);
    return durations;
  }

  @Override
  public List<Long> getProcessInstancesDuration(final ProcessDefinitionUUID processUUID, final Date since,
      final Date until) {
    final List<Long> durations = new ArrayList<Long>();
    for (final Querier querier : queriers) {
      durations.addAll(querier.getProcessInstancesDuration(processUUID, since, until));
    }
    Collections.sort(durations);
    return durations;
  }

  @Override
  public List<Long> getProcessInstancesDurationFromProcessUUIDs(final Set<ProcessDefinitionUUID> processUUIDs,
      final Date since, final Date until) {
    final List<Long> durations = new ArrayList<Long>();
    for (final Querier querier : queriers) {
      durations.addAll(querier.getProcessInstancesDurationFromProcessUUIDs(processUUIDs, since, until));
    }
    Collections.sort(durations);
    return durations;
  }

  @Override
  public List<Long> getActivityInstancesExecutionTime(final Date since, final Date until) {
    final List<Long> executionTimes = new ArrayList<Long>();
    for (final Querier querier : queriers) {
      executionTimes.addAll(querier.getActivityInstancesExecutionTime(since, until));
    }
    Collections.sort(executionTimes);
    return executionTimes;
  }

  @Override
  public List<Long> getActivityInstancesExecutionTime(final ProcessDefinitionUUID processUUID, final Date since,
      final Date until) {
    final List<Long> executionTimes = new ArrayList<Long>();
    for (final Querier querier : queriers) {
      executionTimes.addAll(querier.getActivityInstancesExecutionTime(processUUID, since, until));
    }
    Collections.sort(executionTimes);
    return executionTimes;
  }

  @Override
  public List<Long> getActivityInstancesExecutionTimeFromProcessUUIDs(final Set<ProcessDefinitionUUID> processUUIDs,
      final Date since, final Date until) {
    final List<Long> executionTimes = new ArrayList<Long>();
    for (final Querier querier : queriers) {
      executionTimes.addAll(querier.getActivityInstancesExecutionTimeFromProcessUUIDs(processUUIDs, since, until));
    }
    Collections.sort(executionTimes);
    return executionTimes;
  }

  @Override
  public List<Long> getActivityInstancesExecutionTime(final ActivityDefinitionUUID activityUUID, final Date since,
      final Date until) {
    final List<Long> executionTimes = new ArrayList<Long>();
    for (final Querier querier : queriers) {
      executionTimes.addAll(querier.getActivityInstancesExecutionTime(activityUUID, since, until));
    }
    Collections.sort(executionTimes);
    return executionTimes;
  }

  @Override
  public List<Long> getActivityInstancesExecutionTimeFromActivityUUIDs(final Set<ActivityDefinitionUUID> activityUUIDs,
      final Date since, final Date until) {
    final List<Long> executionTimes = new ArrayList<Long>();
    for (final Querier querier : queriers) {
      executionTimes.addAll(querier.getActivityInstancesExecutionTimeFromActivityUUIDs(activityUUIDs, since, until));
    }
    Collections.sort(executionTimes);
    return executionTimes;
  }

  @Override
  public List<Long> getTaskInstancesWaitingTime(final Date since, final Date until) {
    final List<Long> waitingTimes = new ArrayList<Long>();
    for (final Querier querier : queriers) {
      waitingTimes.addAll(querier.getTaskInstancesWaitingTime(since, until));
    }
    Collections.sort(waitingTimes);
    return waitingTimes;
  }

  @Override
  public List<Long> getTaskInstancesWaitingTime(final ProcessDefinitionUUID processUUID, final Date since,
      final Date until) {
    final List<Long> waitingTimes = new ArrayList<Long>();
    for (final Querier querier : queriers) {
      waitingTimes.addAll(querier.getTaskInstancesWaitingTime(processUUID, since, until));
    }
    Collections.sort(waitingTimes);
    return waitingTimes;
  }

  @Override
  public List<Long> getTaskInstancesWaitingTimeFromProcessUUIDs(final Set<ProcessDefinitionUUID> processUUIDs,
      final Date since, final Date until) {
    final List<Long> waitingTimes = new ArrayList<Long>();
    for (final Querier querier : queriers) {
      waitingTimes.addAll(querier.getTaskInstancesWaitingTimeFromProcessUUIDs(processUUIDs, since, until));
    }
    Collections.sort(waitingTimes);
    return waitingTimes;
  }

  @Override
  public List<Long> getTaskInstancesWaitingTime(final ActivityDefinitionUUID taskUUID, final Date since,
      final Date until) {
    final List<Long> waitingTimes = new ArrayList<Long>();
    for (final Querier querier : queriers) {
      waitingTimes.addAll(querier.getTaskInstancesWaitingTime(taskUUID, since, until));
    }
    Collections.sort(waitingTimes);
    return waitingTimes;
  }

  @Override
  public List<Long> getTaskInstancesWaitingTimeFromTaskUUIDs(final Set<ActivityDefinitionUUID> taskUUIDs,
      final Date since, final Date until) {
    final List<Long> waitingTimes = new ArrayList<Long>();
    for (final Querier querier : queriers) {
      waitingTimes.addAll(querier.getTaskInstancesWaitingTimeFromTaskUUIDs(taskUUIDs, since, until));
    }
    Collections.sort(waitingTimes);
    return waitingTimes;
  }

  @Override
  public List<Long> getTaskInstancesWaitingTimeOfUser(final String username, final Date since, final Date until) {
    final List<Long> waitingTimes = new ArrayList<Long>();
    for (final Querier querier : queriers) {
      waitingTimes.addAll(querier.getTaskInstancesWaitingTimeOfUser(username, since, until));
    }
    Collections.sort(waitingTimes);
    return waitingTimes;
  }

  @Override
  public List<Long> getTaskInstancesWaitingTimeOfUser(final String username, final ProcessDefinitionUUID processUUID,
      final Date since, final Date until) {
    final List<Long> waitingTimes = new ArrayList<Long>();
    for (final Querier querier : queriers) {
      waitingTimes.addAll(querier.getTaskInstancesWaitingTimeOfUser(username, processUUID, since, until));
    }
    Collections.sort(waitingTimes);
    return waitingTimes;
  }

  @Override
  public List<Long> getTaskInstancesWaitingTimeOfUserFromProcessUUIDs(final String username,
      final Set<ProcessDefinitionUUID> processUUIDs, final Date since, final Date until) {
    final List<Long> waitingTimes = new ArrayList<Long>();
    for (final Querier querier : queriers) {
      waitingTimes.addAll(querier.getTaskInstancesWaitingTimeOfUserFromProcessUUIDs(username, processUUIDs, since,
          until));
    }
    Collections.sort(waitingTimes);
    return waitingTimes;
  }

  @Override
  public List<Long> getTaskInstancesWaitingTimeOfUser(final String username, final ActivityDefinitionUUID taskUUID,
      final Date since, final Date until) {
    final List<Long> waitingTimes = new ArrayList<Long>();
    for (final Querier querier : queriers) {
      waitingTimes.addAll(querier.getTaskInstancesWaitingTimeOfUser(username, taskUUID, since, until));
    }
    Collections.sort(waitingTimes);
    return waitingTimes;
  }

  @Override
  public List<Long> getTaskInstancesWaitingTimeOfUserFromTaskUUIDs(final String username,
      final Set<ActivityDefinitionUUID> taskUUIDs, final Date since, final Date until) {
    final List<Long> waitingTimes = new ArrayList<Long>();
    for (final Querier querier : queriers) {
      waitingTimes.addAll(querier.getTaskInstancesWaitingTimeOfUserFromTaskUUIDs(username, taskUUIDs, since, until));
    }
    Collections.sort(waitingTimes);
    return waitingTimes;
  }

  @Override
  public List<Long> getActivityInstancesDuration(final Date since, final Date until) {
    final List<Long> durations = new ArrayList<Long>();
    for (final Querier querier : queriers) {
      durations.addAll(querier.getActivityInstancesDuration(since, until));
    }
    Collections.sort(durations);
    return durations;
  }

  @Override
  public List<Long> getActivityInstancesDuration(final ProcessDefinitionUUID processUUID, final Date since,
      final Date until) {
    final List<Long> durations = new ArrayList<Long>();
    for (final Querier querier : queriers) {
      durations.addAll(querier.getActivityInstancesDuration(processUUID, since, until));
    }
    Collections.sort(durations);
    return durations;
  }

  @Override
  public List<Long> getActivityInstancesDurationFromProcessUUIDs(final Set<ProcessDefinitionUUID> processUUIDs,
      final Date since, final Date until) {
    final List<Long> durations = new ArrayList<Long>();
    for (final Querier querier : queriers) {
      durations.addAll(querier.getActivityInstancesDurationFromProcessUUIDs(processUUIDs, since, until));
    }
    Collections.sort(durations);
    return durations;
  }

  @Override
  public List<Long> getActivityInstancesDuration(final ActivityDefinitionUUID activityUUID, final Date since,
      final Date until) {
    final List<Long> durations = new ArrayList<Long>();
    for (final Querier querier : queriers) {
      durations.addAll(querier.getActivityInstancesDuration(activityUUID, since, until));
    }
    Collections.sort(durations);
    return durations;
  }

  @Override
  public List<Long> getActivityInstancesDurationFromActivityUUIDs(final Set<ActivityDefinitionUUID> activityUUIDs,
      final Date since, final Date until) {
    final List<Long> durations = new ArrayList<Long>();
    for (final Querier querier : queriers) {
      durations.addAll(querier.getActivityInstancesDurationFromActivityUUIDs(activityUUIDs, since, until));
    }
    Collections.sort(durations);
    return durations;
  }

  @Override
  public List<Long> getActivityInstancesDurationByActivityType(final Type activityType, final Date since,
      final Date until) {
    final List<Long> durations = new ArrayList<Long>();
    for (final Querier querier : queriers) {
      durations.addAll(querier.getActivityInstancesDurationByActivityType(activityType, since, until));
    }
    Collections.sort(durations);
    return durations;
  }

  @Override
  public List<Long> getActivityInstancesDurationByActivityType(final Type activityType,
      final ProcessDefinitionUUID processUUID, final Date since, final Date until) {
    final List<Long> durations = new ArrayList<Long>();
    for (final Querier querier : queriers) {
      durations.addAll(querier.getActivityInstancesDurationByActivityType(activityType, processUUID, since, until));
    }
    Collections.sort(durations);
    return durations;
  }

  @Override
  public List<Long> getActivityInstancesDurationByActivityTypeFromProcessUUIDs(final Type activityType,
      final Set<ProcessDefinitionUUID> processUUIDs, final Date since, final Date until) {
    final List<Long> durations = new ArrayList<Long>();
    if (queriers.size() == 1) {
      return queriers.get(0).getActivityInstancesDurationByActivityTypeFromProcessUUIDs(activityType, processUUIDs,
          since, until);
    }
    for (final Querier querier : queriers) {
      durations.addAll(querier.getActivityInstancesDurationByActivityTypeFromProcessUUIDs(activityType, processUUIDs,
          since, until));
    }
    Collections.sort(durations);
    return durations;
  }

  @Override
  public long getNumberOfCreatedProcessInstances(final Date since, final Date until) {
    long nb = 0;
    for (final Querier querier : queriers) {
      nb += querier.getNumberOfCreatedProcessInstances(since, until);
    }
    return nb;
  }

  @Override
  public long getNumberOfCreatedProcessInstances(final ProcessDefinitionUUID processUUID, final Date since,
      final Date until) {
    long nb = 0;
    for (final Querier querier : queriers) {
      nb += querier.getNumberOfCreatedProcessInstances(processUUID, since, until);
    }
    return nb;
  }

  @Override
  public long getNumberOfCreatedActivityInstances(final Date since, final Date until) {
    long nb = 0;
    for (final Querier querier : queriers) {
      nb += querier.getNumberOfCreatedActivityInstances(since, until);
    }
    return nb;
  }

  @Override
  public long getNumberOfCreatedActivityInstances(final ProcessDefinitionUUID processUUID, final Date since,
      final Date until) {
    long nb = 0;
    for (final Querier querier : queriers) {
      nb += querier.getNumberOfCreatedActivityInstances(processUUID, since, until);
    }
    return nb;
  }

  @Override
  public long getNumberOfCreatedActivityInstancesFromProcessUUIDs(final Set<ProcessDefinitionUUID> processUUIDs,
      final Date since, final Date until) {
    long nb = 0;
    for (final Querier querier : queriers) {
      nb += querier.getNumberOfCreatedActivityInstancesFromProcessUUIDs(processUUIDs, since, until);
    }
    return nb;
  }

  @Override
  public long getNumberOfCreatedActivityInstances(final ActivityDefinitionUUID activityUUID, final Date since,
      final Date until) {
    long nb = 0;
    for (final Querier querier : queriers) {
      nb += querier.getNumberOfCreatedActivityInstances(activityUUID, since, until);
    }
    return nb;
  }

  @Override
  public long getNumberOfCreatedActivityInstancesFromActivityUUIDs(final Set<ActivityDefinitionUUID> activityUUIDs,
      final Date since, final Date until) {
    long nb = 0;
    for (final Querier querier : queriers) {
      nb += querier.getNumberOfCreatedActivityInstancesFromActivityUUIDs(activityUUIDs, since, until);
    }
    return nb;
  }

  @Override
  public long getNumberOfCreatedActivityInstancesByActivityType(final Type activityType, final Date since,
      final Date until) {
    long nb = 0;
    for (final Querier querier : queriers) {
      nb += querier.getNumberOfCreatedActivityInstancesByActivityType(activityType, since, until);
    }
    return nb;
  }

  @Override
  public long getNumberOfCreatedActivityInstancesByActivityType(final Type activityType,
      final ProcessDefinitionUUID processUUID, final Date since, final Date until) {
    long nb = 0;
    for (final Querier querier : queriers) {
      nb += querier.getNumberOfCreatedActivityInstancesByActivityType(activityType, processUUID, since, until);
    }
    return nb;
  }

  @Override
  public long getNumberOfCreatedActivityInstancesByActivityTypeFromProcessUUIDs(final Type activityType,
      final Set<ProcessDefinitionUUID> processUUIDs, final Date since, final Date until) {
    long nb = 0;
    for (final Querier querier : queriers) {
      nb += querier.getNumberOfCreatedActivityInstancesByActivityTypeFromProcessUUIDs(activityType, processUUIDs,
          since, until);
    }
    return nb;
  }

  @Override
  public boolean containsOtherActiveActivities(final ProcessInstanceUUID instanceUUID,
      final ActivityInstanceUUID activityUUID) {
    for (final Querier querier : queriers) {
      if (querier.containsOtherActiveActivities(instanceUUID, activityUUID)) {
        return true;
      }
    }
    return false;
  }

}
