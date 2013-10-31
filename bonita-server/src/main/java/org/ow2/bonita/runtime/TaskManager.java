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
 * Modified by Matthieu Chaffotte, Elias Ricken de Medeiros - BonitaSoft S.A.
 **/
package org.ow2.bonita.runtime;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.hibernate.AssertionFailure;
import org.hibernate.StaleStateException;
import org.hibernate.exception.LockAcquisitionException;
import org.ow2.bonita.connector.core.Filter;
import org.ow2.bonita.connector.core.PerformerAssignFilter;
import org.ow2.bonita.definition.PerformerAssign;
import org.ow2.bonita.definition.RoleMapper;
import org.ow2.bonita.definition.activity.AbstractActivity;
import org.ow2.bonita.definition.activity.ConnectorExecutor;
import org.ow2.bonita.facade.def.InternalActivityDefinition;
import org.ow2.bonita.facade.def.element.FilterDefinition;
import org.ow2.bonita.facade.def.element.HookDefinition;
import org.ow2.bonita.facade.def.element.RoleMapperDefinition;
import org.ow2.bonita.facade.def.majorElement.ActivityDefinition;
import org.ow2.bonita.facade.def.majorElement.ParticipantDefinition;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.exception.BonitaWrapperException;
import org.ow2.bonita.facade.exception.IllegalTaskStateException;
import org.ow2.bonita.facade.exception.RoleMapperInvocationException;
import org.ow2.bonita.facade.exception.TaskNotFoundException;
import org.ow2.bonita.facade.exception.UnRollbackableException;
import org.ow2.bonita.facade.runtime.ActivityInstance;
import org.ow2.bonita.facade.runtime.ActivityState;
import org.ow2.bonita.facade.runtime.TaskInstance;
import org.ow2.bonita.facade.uuid.ActivityDefinitionUUID;
import org.ow2.bonita.facade.uuid.ActivityInstanceUUID;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.runtime.model.Execution;
import org.ow2.bonita.services.Recorder;
import org.ow2.bonita.util.BonitaRuntimeException;
import org.ow2.bonita.util.EnvTool;
import org.ow2.bonita.util.ExceptionManager;
import org.ow2.bonita.util.TransientData;

/**
 * is one task instance that can be assigned to an user
 * 
 * @author Marc Blachon, Guillaume Porcher, Charles Souillard, Miguel Valdes,
 *         Pierre Vigneras
 */
public class TaskManager {

  private static final Logger LOG = Logger.getLogger(TaskManager.class.getName());

  private static TaskInstance getTask(final ActivityInstanceUUID taskUUID) throws TaskNotFoundException {
    final TaskInstance task = EnvTool.getJournalQueriers().getTaskInstance(taskUUID);
    if (task == null) {
      throw new TaskNotFoundException("bai_RAPII_19", taskUUID);
    }
    return task;
  }

  private static ActivityDefinition getActivityDefinition(final ProcessDefinitionUUID processUUID,
      final String activityName) {
    final ActivityDefinition activity = EnvTool.getJournalQueriers().getActivity(
        new ActivityDefinitionUUID(processUUID, activityName));
    return activity;
  }

  protected static Execution getExecution(final ActivityInstanceUUID taskUUID) throws TaskNotFoundException {
    final TaskInstance task = getTask(taskUUID);
    return EnvTool.getJournalQueriers().getExecutionOnActivity(task.getProcessInstanceUUID(), taskUUID);
  }

  private static void assign(final ActivityInstance task, final java.util.Set<java.lang.String> candidates,
      final String userId) {
    final ActivityInstanceUUID taskUUID = task.getUUID();
    if (LOG.isLoggable(Level.FINE)) {
      LOG.fine("assigning task : " + taskUUID + " on activity " + task.getActivityName());
    }
    final Recorder recorder = EnvTool.getRecorder();
    recorder.recordTaskAssigned(taskUUID, task.getState(), EnvTool.getUserId(), candidates, userId);
  }

  public static void unAssign(final ActivityInstanceUUID taskUUID) throws TaskNotFoundException {
    final TaskInstance task = getTask(taskUUID);
    if (LOG.isLoggable(Level.FINE)) {
      LOG.fine("Unassigning: " + task);
    }
    // record the assign
    assign(task, task.getTaskCandidates(), null);

    if (LOG.isLoggable(Level.FINE)) {
      LOG.fine("Unassigned: " + task);
    }
  }

  public static void assign(final ActivityInstanceUUID taskUUID, final Set<String> candidates)
      throws TaskNotFoundException {
    final TaskInstance task = getTask(taskUUID);
    if (LOG.isLoggable(Level.FINE)) {
      LOG.fine("Assigning: " + task);
    }
    // record the assign
    assign(task, candidates, null);
    if (LOG.isLoggable(Level.FINE)) {
      LOG.fine("Assigned: " + task);
    }
  }

  public static void assign(final ActivityInstanceUUID taskUUID, final String userId) throws TaskNotFoundException {
    final TaskInstance task = getTask(taskUUID);
    if (LOG.isLoggable(Level.FINE)) {
      LOG.fine("Assigning: " + task + " to " + userId);
    }
    // record the assign
    assign(task, null, userId);
    if (LOG.isLoggable(Level.FINE)) {
      LOG.fine("Assigned: " + task + " to " + userId);
    }
  }

  private static Set<String> getCandidates(final Set<Performer> performers, final TaskInstance task) {
    Set<String> candidates = null;
    if (performers != null) {
      candidates = new HashSet<String>();
      for (final Performer performer : performers) {
        if (performer.isHuman()) {
          candidates.add(performer.getName());
        } else if (performer.getRoleMapperDefinition() != null) {
          final Set<String> tmp = executeRoleMapper(task, performer);
          if (tmp != null) {
            candidates.addAll(tmp);
          }
        }
      }
    }
    return candidates;
  }

  public static void assign(final ActivityInstanceUUID taskUUID) throws TaskNotFoundException {
    final TaskInstance task = getTask(taskUUID);
    final ProcessDefinitionUUID processUUID = task.getProcessDefinitionUUID();
    final ActivityDefinition activityDefinition = getActivityDefinition(processUUID, task.getActivityName());

    // execute role resolvers and filters
    final Set<Performer> performers = getPerformers(activityDefinition);

    Set<String> candidates = getCandidates(performers, task);
    String userId = null;

    final Performer performer = performers.iterator().next();
    if (performer.getFilterDefinition() != null) {
      candidates = executeFilter(task, candidates, performer);
      if (candidates.size() == 1) {
        userId = candidates.iterator().next();
      }
    }

    if (LOG.isLoggable(Level.FINE)) {
      LOG.fine("Assigning: " + task);
    }
    // record the assign
    if (userId != null) {
      assign(task, null, userId);
    } else {
      assign(task, candidates, null);
    }
    if (LOG.isLoggable(Level.FINE)) {
      LOG.fine("Assigned: " + task);
    }
  }

  public static void finish(final ActivityInstanceUUID taskUUID, final boolean assignTask)
      throws TaskNotFoundException, IllegalTaskStateException {
    final TaskInstance task = getTask(taskUUID);
    if (!ActivityState.ABORTED.equals(task.getState()) && !ActivityState.FAILED.equals(task.getState())) {
      if (LOG.isLoggable(Level.FINE)) {
        LOG.fine("Checking compatible state of " + task);
      }
      if (!task.getState().equals(ActivityState.EXECUTING)) {
        final Set<ActivityState> expectedStates = new HashSet<ActivityState>();
        expectedStates.add(ActivityState.EXECUTING);
        final String message = ExceptionManager.getInstance().getFullMessage("bai_RAPII_11");
        throw new IllegalTaskStateException("bai_RAPII_11", message, taskUUID, expectedStates, task.getState());
      }
      final String activityName = task.getActivityName();
      if (LOG.isLoggable(Level.FINE)) {
        LOG.fine("Finishing task : " + taskUUID + " on activity " + activityName);
      }
      final Recorder recorder = EnvTool.getRecorder();
      if (assignTask) {
        assign(task, null, task.getTaskUser());
      }
      recorder.recordTaskFinished(taskUUID, EnvTool.getUserId());

      final Execution internalExecution = getExecution(taskUUID);
      final InternalActivityDefinition activityDef = internalExecution.getNode();

      final AbstractActivity abstractActivity = (AbstractActivity) activityDef.getBehaviour();

      try {
        ConnectorExecutor.executeConnectors(activityDef, internalExecution, HookDefinition.Event.taskOnFinish);
      } catch (final StaleStateException sse) {
        throw sse;
      } catch (final AssertionFailure af) {
        throw af;
      } catch (final LockAcquisitionException lae) {
        throw lae;
      } catch (final RuntimeException e) {
        // put activity in the state FAILED
        recorder.recordActivityFailed(task);
        if (ExtensionPointsPolicy.THROW_EXCPTION_ON_FAIL.equals(EnvTool.getExtensionPointsPolicy())) {
          TransientData.removeTransientData(taskUUID);
          throw new UnRollbackableException("Error while executing connector taskOnFinish", e);
        }
      }
      final ActivityState activityState = internalExecution.getActivityInstance().getState();
      if (!ActivityState.ABORTED.equals(activityState) && !ActivityState.FAILED.equals(activityState)) {
        abstractActivity.signal(internalExecution, AbstractActivity.BODY_FINISHED, null);
        if (LOG.isLoggable(Level.FINE)) {
          LOG.fine("Terminated: " + task);
        }
      } else {
        // if the the state is not aborted the data is already removed in
        // AbstractActivity.end()
        TransientData.removeTransientData(taskUUID);
      }
    }
  }

  public static void suspend(final ActivityInstanceUUID taskUUID, final boolean assignTask)
      throws TaskNotFoundException, IllegalTaskStateException {
    final TaskInstance task = getTask(taskUUID);

    if (LOG.isLoggable(Level.FINE)) {
      LOG.fine("Checking compatible state of " + task);
    }
    if (!(task.getState().equals(ActivityState.READY) || task.getState().equals(ActivityState.EXECUTING))) {
      final Set<ActivityState> expectedStates = new HashSet<ActivityState>();
      expectedStates.add(ActivityState.READY);
      expectedStates.add(ActivityState.EXECUTING);

      final String message = ExceptionManager.getInstance().getFullMessage("bai_RAPII_13");
      throw new IllegalTaskStateException("bai_RAPII_13", message, taskUUID, expectedStates, task.getState());
    }

    final String currentUserId = EnvTool.getUserId();

    final ProcessDefinitionUUID processUUID = task.getProcessDefinitionUUID();
    final String activityName = task.getActivityName();

    if (LOG.isLoggable(Level.FINE)) {
      LOG.fine("Suspending task : " + taskUUID + " on activity " + activityName);
    }
    final Recorder recorder = EnvTool.getRecorder();
    if (assignTask) {
      assign(task, null, currentUserId);
    }
    recorder.recordTaskSuspended(taskUUID, EnvTool.getUserId());

    final Execution internalExecution = getExecution(taskUUID);

    final ActivityDefinition activityDef = getActivityDefinition(processUUID, activityName);

    ConnectorExecutor.executeConnectors(activityDef, internalExecution, HookDefinition.Event.taskOnSuspend);

    if (LOG.isLoggable(Level.FINE)) {
      LOG.fine("Suspended: " + task);
    }
  }

  public static void resume(final ActivityInstanceUUID taskUUID, final boolean taskAssign)
      throws IllegalTaskStateException, TaskNotFoundException {
    final TaskInstance task = getTask(taskUUID);

    if (LOG.isLoggable(Level.FINE)) {
      LOG.fine("Checking compatible state of " + task);
    }
    if (!task.getState().equals(ActivityState.SUSPENDED)) {
      final Set<ActivityState> expectedStates = new HashSet<ActivityState>();
      expectedStates.add(ActivityState.SUSPENDED);
      final String message = ExceptionManager.getInstance().getFullMessage("bai_RAPII_15");
      throw new IllegalTaskStateException("bai_RAPII_15", message, taskUUID, expectedStates, task.getState());
    }
    final String currentUserId = EnvTool.getUserId();

    if (LOG.isLoggable(Level.FINE)) {
      LOG.fine("Resuming: " + task);
    }

    if (LOG.isLoggable(Level.FINE)) {
      LOG.fine("Resuming task : " + taskUUID + " on activity " + task.getActivityName());
    }
    final Recorder recorder = EnvTool.getRecorder();
    if (taskAssign) {
      assign(task, null, currentUserId);
    }
    recorder.recordTaskResumed(taskUUID, EnvTool.getUserId());

    final Execution internalExecution = getExecution(taskUUID);

    final InternalActivityDefinition activityDef = internalExecution.getNode();

    ConnectorExecutor.executeConnectors(activityDef, internalExecution, HookDefinition.Event.taskOnResume);

    if (LOG.isLoggable(Level.FINE)) {
      LOG.fine("Resumed: " + task);
    }
  }

  public static void start(final ActivityInstanceUUID taskUUID, final boolean assignTask)
      throws IllegalTaskStateException, TaskNotFoundException {
    final TaskInstance task = getTask(taskUUID);
    if (!ActivityState.ABORTED.equals(task.getState())) {
      if (LOG.isLoggable(Level.FINE)) {
        LOG.fine("Checking compatible state of " + task);
      }
      if (!task.getState().equals(ActivityState.READY)) {
        final Set<ActivityState> expectedStates = new HashSet<ActivityState>();
        expectedStates.add(ActivityState.READY);
        final String message = ExceptionManager.getInstance().getFullMessage("bai_RAPII_9");
        throw new IllegalTaskStateException("bai_RAPII_9", message, taskUUID, expectedStates, task.getState());
      }
      if (LOG.isLoggable(Level.FINE)) {
        LOG.fine("Starting: " + task);
      }

      final Recorder recorder = EnvTool.getRecorder();
      if (assignTask) {
        assign(task, null, EnvTool.getUserId());
      }

      final Execution internalExecution = getExecution(taskUUID);
      recorder.recordBodyStarted(task);
      recorder.recordTaskStarted(taskUUID, EnvTool.getUserId());
      final InternalActivityDefinition activityDef = internalExecution.getNode();
      try {
        ConnectorExecutor.executeConnectors(activityDef, internalExecution, HookDefinition.Event.taskOnStart);
      } catch (final StaleStateException sse) {
        throw sse;
      } catch (final AssertionFailure af) {
        throw af;
      } catch (final LockAcquisitionException lae) {
        throw lae;
      } catch (final RuntimeException e) {
        // put activity in the state FAILED
        recorder.recordActivityFailed(task);
        if (ExtensionPointsPolicy.THROW_EXCPTION_ON_FAIL.equals(EnvTool.getExtensionPointsPolicy())) {
          throw new UnRollbackableException("Error while executing connector taskOnStart", e);
        }
      }
      if (LOG.isLoggable(Level.FINE)) {
        LOG.fine("Started: " + task);
      }
    }
  }

  private static Set<String> executeFilter(final TaskInstance task, final Set<String> candidates,
      final Performer performer) {
    final ProcessDefinitionUUID processUUID = task.getProcessDefinitionUUID();
    final ClassLoader baseClassLoader = Thread.currentThread().getContextClassLoader();
    try {
      final ClassLoader processClassLoader = EnvTool.getClassDataLoader().getProcessClassLoader(processUUID);
      Thread.currentThread().setContextClassLoader(processClassLoader);

      final FilterDefinition filterDefinition = performer.getFilterDefinition();
      if (filterDefinition != null) {
        final PerformerAssign performerAssign = getPerformerAssign(processUUID, filterDefinition);
        Filter filter = null;
        if (performerAssign == null) {
          filter = EnvTool.getClassDataLoader().getInstance(Filter.class, processUUID, filterDefinition);
        } else {
          filter = new PerformerAssignFilter();
        }
        try {
          return ConnectorExecutor.executeFilter(filter, performerAssign, task, candidates,
              filterDefinition.getParameters());

        } catch (final Exception e) {
          throw new BonitaWrapperException(e);
        }
      }
      return candidates;
    } finally {
      Thread.currentThread().setContextClassLoader(baseClassLoader);
    }
  }

  private static PerformerAssign getPerformerAssign(final ProcessDefinitionUUID processUUID,
      final FilterDefinition filterDefinition) {
    try {
      return EnvTool.getClassDataLoader().getInstance(PerformerAssign.class, processUUID, filterDefinition);
    } catch (final Exception e) {
      return null;
    }
  }

  private static Set<String> executeRoleMapper(final TaskInstance task, final Performer performer) {
    final ProcessDefinitionUUID processUUID = task.getProcessDefinitionUUID();
    final RoleMapperDefinition rolemapperDef = performer.getRoleMapperDefinition();
    final ClassLoader baseClassLoader = Thread.currentThread().getContextClassLoader();
    try {
      final ClassLoader processClassLoader = EnvTool.getClassDataLoader().getProcessClassLoader(processUUID);
      Thread.currentThread().setContextClassLoader(processClassLoader);
      if (rolemapperDef != null) {
        final RoleMapper roleMapper = EnvTool.getClassDataLoader().getInstance(RoleMapper.class, processUUID,
            rolemapperDef);
        try {
          return ConnectorExecutor.executeRoleMapper(roleMapper, task, performer.getName(),
              rolemapperDef.getParameters());
        } catch (final Exception e) {
          throw new BonitaWrapperException(new RoleMapperInvocationException("be_TRT_2", rolemapperDef.toString(), e));
        }
      }
      return null;
    } finally {
      Thread.currentThread().setContextClassLoader(baseClassLoader);
    }
  }

  public static void ready(final Execution internalExecution) throws TaskNotFoundException {
    final ActivityInstanceUUID taskUUID = new ActivityInstanceUUID(internalExecution.getActivityInstanceUUID()
        .toString());
    final InternalActivityDefinition activityDef = internalExecution.getNode();
    final TaskInstance task = getTask(taskUUID);
    final String activityName = task.getActivityName();
    if (LOG.isLoggable(Level.FINE)) {
      LOG.fine("Ready task : " + taskUUID + " on activity " + activityName);
    }

    // get performer (aka participant/swimlane)
    final Set<Performer> performers = getPerformers(activityDef);
    String userId = null;
    Set<String> candidates = null;
    if (performers != null) {
      candidates = getCandidates(performers, task);
      final Performer performer = performers.iterator().next();
      if (performer.getFilterDefinition() != null) {
        candidates = executeFilter(task, candidates, performer);
        if (candidates.size() == 1) {
          userId = candidates.iterator().next();
        }
      }
    }

    if (userId != null) {
      EnvTool.getRecorder().recordTaskReady(taskUUID, null, userId);
    } else {
      EnvTool.getRecorder().recordTaskReady(taskUUID, candidates, null);
    }
    ConnectorExecutor.executeConnectors(activityDef, internalExecution, HookDefinition.Event.taskOnReady);
  }

  protected static Performer getPerformer(final ActivityDefinition activity, final ProcessDefinition processDef,
      final String performer) {
    ParticipantDefinition participant = null;
    if (processDef.getParticipants() != null) {
      for (final ParticipantDefinition p : processDef.getParticipants()) {
        if (p.getName().equals(performer)) {
          participant = p;
          break;
        }
      }
    }
    if (participant == null) {
      throw new BonitaRuntimeException("Wrong performer: " + performer
          + ". No participant is defined within the process with processDefinitionUUID: " + performer);
    }
    return new Performer(performer, participant.getRoleMapper(), activity.getFilter());
  }

  private static Set<Performer> getPerformers(final ActivityDefinition activityDefinition) {
    final Set<String> performers = activityDefinition.getPerformers();
    Set<Performer> result = null;
    if (!performers.isEmpty()) {
      final ProcessDefinition processDef = EnvTool.getJournalQueriers().getProcess(
          activityDefinition.getProcessDefinitionUUID());
      result = new HashSet<Performer>();
      for (final String performer : performers) {
        result.add(getPerformer(activityDefinition, processDef, performer));
      }
    }
    return result;
  }

  private static class Performer {
    protected String name;
    protected RoleMapperDefinition roleMapper;
    protected FilterDefinition filter;

    public Performer(final String name, final RoleMapperDefinition roleMapper, final FilterDefinition filter) {
      super();
      this.name = name;
      this.roleMapper = roleMapper;
      this.filter = filter;
    }

    public RoleMapperDefinition getRoleMapperDefinition() {
      return roleMapper;
    }

    public FilterDefinition getFilterDefinition() {
      return filter;
    }

    public String getName() {
      return name;
    }

    public boolean isHuman() {
      return roleMapper == null && filter == null;
    }
  }

  public static void skip(final ActivityInstanceUUID taskUUID, final Map<String, Object> variablesToUpdate)
      throws TaskNotFoundException, IllegalTaskStateException {
    final TaskInstance task = getTask(taskUUID);
    final Execution internalExecution = getExecution(taskUUID);
    ActivityManager.skip(internalExecution, task, variablesToUpdate);
  }

}
