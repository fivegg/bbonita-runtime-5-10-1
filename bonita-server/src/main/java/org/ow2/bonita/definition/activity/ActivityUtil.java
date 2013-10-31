/**
 * Copyright (C) 2010-2012 BonitaSoft S.A.
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
package org.ow2.bonita.definition.activity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import org.ow2.bonita.facade.def.InternalProcessDefinition;
import org.ow2.bonita.facade.def.element.BoundaryEvent;
import org.ow2.bonita.facade.def.element.EventDefinition;
import org.ow2.bonita.facade.def.element.impl.ErrorBoundaryEventImpl;
import org.ow2.bonita.facade.def.element.impl.IterationDescriptor;
import org.ow2.bonita.facade.def.element.impl.MessageBoundaryEventImpl;
import org.ow2.bonita.facade.def.majorElement.ActivityDefinition;
import org.ow2.bonita.facade.def.majorElement.ActivityDefinition.JoinType;
import org.ow2.bonita.facade.def.majorElement.EventProcessDefinition;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.def.majorElement.TransitionDefinition;
import org.ow2.bonita.facade.runtime.ProcessInstance;
import org.ow2.bonita.facade.runtime.impl.InternalProcessInstance;
import org.ow2.bonita.facade.runtime.impl.InternalProcessInstance.TransitionState;
import org.ow2.bonita.facade.uuid.ActivityInstanceUUID;
import org.ow2.bonita.light.LightProcessDefinition.ProcessType;
import org.ow2.bonita.runtime.event.EventConstants;
import org.ow2.bonita.runtime.event.EventInstance;
import org.ow2.bonita.runtime.event.IncomingEventInstance;
import org.ow2.bonita.runtime.event.Job;
import org.ow2.bonita.runtime.event.JobBuilder;
import org.ow2.bonita.runtime.event.OutgoingEventInstance;
import org.ow2.bonita.runtime.model.Execution;
import org.ow2.bonita.services.EventService;
import org.ow2.bonita.util.BonitaRuntimeException;
import org.ow2.bonita.util.EnvTool;
import org.ow2.bonita.util.ExceptionManager;
import org.ow2.bonita.util.GroovyException;
import org.ow2.bonita.util.GroovyUtil;
import org.ow2.bonita.util.Misc;

/**
 * 
 * @author Charles Souillard, Matthieu Chaffotte
 * 
 */
public class ActivityUtil {

  static void deleteEvents(final String eventName, final String processName, final String activityName,
      final ActivityInstanceUUID activityUUID) {
    final EventService eventService = EnvTool.getEventService();
    for (final IncomingEventInstance incoming : eventService.getIncomingEvents(eventName, processName, activityName,
        activityUUID)) {
      eventService.removeEvent(incoming);
    }
    for (final OutgoingEventInstance outgoing : eventService.getOutgoingEvents(eventName, processName, activityName,
        activityUUID)) {
      eventService.removeEvent(outgoing);
    }
  }

  static void deleteBoundaryEvents(final ActivityInstanceUUID activityUUID) {
    final EventService eventService = EnvTool.getEventService();
    for (final IncomingEventInstance incoming : eventService.getBoundaryIncomingEvents(activityUUID)) {
      eventService.removeEvent(incoming);
    }
    for (final OutgoingEventInstance outgoing : eventService.getBoundaryOutgoingEvents(activityUUID)) {
      eventService.removeEvent(outgoing);
    }
  }

  static boolean evaluateTransition(final TransitionDefinition t, final Execution internalExecution) {
    // testing the guard condition
    final String condition = t.getCondition();
    boolean conditionOK = true;
    if (condition != null) {
      conditionOK = ConditionEvaluator.evaluate(condition, internalExecution);
      if (!conditionOK) {
        if (AbstractActivity.LOG.isLoggable(Level.FINE)) {
          AbstractActivity.LOG.fine("Unable to take transition: " + t.getName());
        }
        // TODO : cancel nodes on this branch : may be investigate for cleaning
        // of executions ?
      }
    }
    return conditionOK;
  }

  static boolean evaluateLoopCondition(final ActivityDefinition activity, final Execution internalExecution) {
    boolean execute = true;
    if (activity.isInALoop()) {
      final String condition = activity.getLoopCondition();
      execute = ConditionEvaluator.evaluate(condition, internalExecution);
    }
    return execute;
  }

  static boolean isJoinOk(final InternalProcessInstance instance, final ActivityDefinition node) {
    if (!node.hasIncomingTransitions())
      return true;
    if (node.getJoinType().equals(JoinType.XOR)) {
      // join XOR
      for (final TransitionDefinition transition : node.getIncomingTransitions()) {
        final TransitionState ts = instance.getTransitionState(transition.getName());
        if (ts != null && ts.equals(TransitionState.TAKEN))
          return true;
      }
      return false;
    } else {
      // join AND
      for (final TransitionDefinition transition : node.getIncomingTransitions()) {
        final TransitionState ts = instance.getTransitionState(transition.getName());
        if (ts == null || !ts.equals(TransitionState.TAKEN))
          return false;
      }
      return true;
    }
  }

  static void createNewIteration(final Execution execution, final ActivityDefinition activity) {
    if (activity.isInCycle()) {
      final InternalProcessInstance instance = execution.getInstance();
      final ProcessDefinition process = EnvTool.getJournalQueriers().getProcess(instance.getProcessDefinitionUUID());
      final Set<IterationDescriptor> iterationDescriptors = process.getIterationDescriptors();
      final boolean newCycle = isAtTheBeginningOfACycle(execution, activity, iterationDescriptors);
      if (newCycle) {
        final String iterationUUID = Misc.getUniqueId("it");
        execution.setIterationId(iterationUUID);
        for (final IterationDescriptor it : iterationDescriptors) {
          if (it.getEntryNodes().contains(execution.getNode().getName())) {
            for (final String joinNodeDescr : it.getCycleNodes()) {
              final ActivityDefinition joinNode = process.getActivity(joinNodeDescr);
              for (final TransitionDefinition tr : joinNode.getIncomingTransitions()) {
                if (it.containsNode(tr.getFrom())) {
                  instance.removeTransitionState(tr.getName());
                }
              }
              if (ActivityUtil.isJoinOk(instance, joinNode)) {
                final String message = ExceptionManager.getInstance().getFullMessage("be_AA_7", joinNode.getName());
                throw new BonitaRuntimeException(message);
              }
            }
          }
        }
      }
    }
  }

  static boolean isAtTheBeginningOfACycle(final Execution execution, final ActivityDefinition activity,
      final Set<IterationDescriptor> iterationDescriptors) {
    boolean newCycleIteration = false;
    if (activity.isInCycle()) {
      final Iterator<IterationDescriptor> iterator = iterationDescriptors.iterator();
      while (!newCycleIteration && iterator.hasNext()) {
        final IterationDescriptor iteration = iterator.next();
        if (iteration.getEntryNodes().contains(execution.getNode().getName())) {
          newCycleIteration = true;
        }
      }
    }
    return newCycleIteration;
  }

  static String getErrorEventName(final ActivityDefinition activity, final String errorCode) {
    final List<ErrorBoundaryEventImpl> events = getErrorEvents(activity.getBoundaryEvents());
    int i = 0;
    String errorName = null;
    String errorAllName = null;
    while (errorName == null && i < events.size()) {
      final ErrorBoundaryEventImpl event = events.get(i);
      final String eventErrorCode = event.getErrorCode();
      if (eventErrorCode == null) {
        errorAllName = event.getName();
      } else if (errorCode.equals(eventErrorCode)) {
        errorName = event.getName();
      }
      i++;
    }
    String errorEventName = null;
    if (errorName != null) {
      errorEventName = errorName + EventConstants.SEPARATOR + errorCode;
    } else if (errorAllName != null) {
      errorEventName = errorAllName + EventConstants.SEPARATOR + "all";
    } else if (errorCode.equals(activity.getTimerCondition())) {
      errorEventName = activity.getName() + EventConstants.SEPARATOR + errorCode;
    }
    return errorEventName;
  }

  public static ActivityDefinition getMatchingErrorEvenSubProcessActivity(final Execution execution,
      final String throwingErrorCode) {
    final ProcessDefinition process = execution.getProcessDefinition();
    final List<EventProcessDefinition> eventSubProcesses = process.getEventSubProcesses();
    if (!eventSubProcesses.isEmpty()) {
      for (final EventProcessDefinition eventSubProcess : eventSubProcesses) {
        final InternalProcessDefinition eventSubProcessDefinition = EnvTool.getJournalQueriers().getProcess(
            eventSubProcess.getName(), eventSubProcess.getVersion());
        final Set<ActivityDefinition> eventSubActivities = eventSubProcessDefinition.getActivities();
        for (final ActivityDefinition activityDefinition : eventSubActivities) {
          if (activityDefinition.getIncomingTransitions().size() == 0) {
            if (activityDefinition.isCatchingErrorEvent()
                && activityDefinition.getTimerCondition().equals(throwingErrorCode))
              return activityDefinition;
          }
        }
      }
    }
    return null;
  }

  private static List<ErrorBoundaryEventImpl> getErrorEvents(final List<BoundaryEvent> events) {
    final List<ErrorBoundaryEventImpl> errorEvents = new ArrayList<ErrorBoundaryEventImpl>();
    for (final BoundaryEvent event : events) {
      if (event instanceof ErrorBoundaryEventImpl) {
        errorEvents.add((ErrorBoundaryEventImpl) event);
      }
    }
    return errorEvents;
  }

  static Job getErrorEventSubProcessJob(final Execution execution, final String errorCode) {
    final ProcessDefinition process = execution.getProcessDefinition();
    final ActivityDefinition eventSubProcessActivity = ActivityUtil.getMatchingErrorEvenSubProcessActivity(process,
        errorCode);
    if (eventSubProcessActivity != null) {
      final Job startError = JobBuilder.startErrorJob(eventSubProcessActivity.getName(),
          eventSubProcessActivity.getUUID());
      startError.setEventSubProcessRootInstanceUUID(execution.getInstance().getUUID());
      return startError;
    }
    return null;
  }

  public static ActivityDefinition getMatchingErrorEvenSubProcessActivity(final ProcessDefinition process,
      final String throwingErrorCode) {
    final List<EventProcessDefinition> eventSubProcesses = process.getEventSubProcesses();
    if (!eventSubProcesses.isEmpty()) {
      for (final EventProcessDefinition eventSubProcess : eventSubProcesses) {
        final InternalProcessDefinition eventSubProcessDefinition = EnvTool.getJournalQueriers().getProcess(
            eventSubProcess.getName(), eventSubProcess.getVersion());
        final Set<ActivityDefinition> eventSubActivities = eventSubProcessDefinition.getActivities();
        for (final ActivityDefinition activityDefinition : eventSubActivities) {
          if (activityDefinition.getIncomingTransitions().size() == 0) {
            if (activityDefinition.isCatchingErrorEvent()
                && activityDefinition.getTimerCondition().equals(throwingErrorCode))
              return activityDefinition;
          }
        }
      }
    }
    return null;
  }

  static Job getTargetErrorJob(final Execution execution) {
    final ProcessDefinition process = execution.getProcessDefinition();
    final ProcessInstance instance = execution.getInstance();
    ActivityInstanceUUID parentActivityUUID = null;
    if (ProcessType.EVENT_SUB_PROCESS.equals(process.getType())) {
      final InternalProcessInstance rootEventSubProcess = EnvTool.getJournalQueriers().getProcessInstance(
          instance.getParentInstanceUUID());
      parentActivityUUID = rootEventSubProcess.getParentActivityUUID();
    } else {
      parentActivityUUID = instance.getParentActivityUUID();
    }
    if (parentActivityUUID != null) {
      final Execution exec = EnvTool.getJournalQueriers().getExecutionOnActivity(
          parentActivityUUID.getProcessInstanceUUID(), parentActivityUUID);
      final String eventName = ActivityUtil.getErrorEventName(exec.getNode(), execution.getNode().getTimerCondition());
      if (eventName != null) {
        final int indexOf = eventName.indexOf(EventConstants.SEPARATOR);
        return JobBuilder.boundaryErrorJob(eventName.substring(0, indexOf), exec.getInstance().getRootInstanceUUID(),
            exec.getEventUUID(), exec.getInstance().getProcessInstanceUUID());
      }
    }
    return null;
  }

  public static void deleteJobs(final String executionEventUUID) {
    if (executionEventUUID != null) {
      final EventService eventService = EnvTool.getEventService();
      eventService.removeJobs(executionEventUUID);
    }
  }

  public static String getEvaluatedCorrelationKey(final ActivityInstanceUUID activityUUID,
      final String correlationKeyName, final String correlationKeyExpression) {
    if (correlationKeyExpression != null) {
      try {
        final String expression = GroovyUtil.evaluate(correlationKeyExpression, null, activityUUID, false, false)
            .toString();
        return correlationKeyName.concat("||||").concat(expression);
      } catch (final GroovyException e) {
        throw new BonitaRuntimeException(e);
      }
    }
    return null;
  }

  public static void addCorrelationKeys(final EventDefinition eventDefinition, final EventInstance eventInstance,
      final ActivityInstanceUUID activityUUID) {
    String correlationKeyName = eventDefinition.getCorrelationKeyName1();
    if (correlationKeyName != null) {
      final String correlationKey = getEvaluatedCorrelationKey(activityUUID, correlationKeyName,
          eventDefinition.getCorrelationKeyExpression1());
      eventInstance.setCorrelationKey1(correlationKey);
    }
    correlationKeyName = eventDefinition.getCorrelationKeyName2();
    if (correlationKeyName != null) {
      final String correlationKey = getEvaluatedCorrelationKey(activityUUID, correlationKeyName,
          eventDefinition.getCorrelationKeyExpression2());
      eventInstance.setCorrelationKey2(correlationKey);
    }
    correlationKeyName = eventDefinition.getCorrelationKeyName3();
    if (correlationKeyName != null) {
      final String correlationKey = getEvaluatedCorrelationKey(activityUUID, correlationKeyName,
          eventDefinition.getCorrelationKeyExpression3());
      eventInstance.setCorrelationKey3(correlationKey);
    }
    correlationKeyName = eventDefinition.getCorrelationKeyName4();
    if (correlationKeyName != null) {
      final String correlationKey = getEvaluatedCorrelationKey(activityUUID, correlationKeyName,
          eventDefinition.getCorrelationKeyExpression4());
      eventInstance.setCorrelationKey4(correlationKey);
    }
    correlationKeyName = eventDefinition.getCorrelationKeyName5();
    if (correlationKeyName != null) {
      final String correlationKey = getEvaluatedCorrelationKey(activityUUID, correlationKeyName,
          eventDefinition.getCorrelationKeyExpression5());
      eventInstance.setCorrelationKey5(correlationKey);
    }
  }

  public static void addCorrelationKeys(final MessageBoundaryEventImpl message,
      final IncomingEventInstance eventInstance, final ActivityInstanceUUID activityUUID) {
    String correlationKeyName = message.getCorrelationKeyName1();
    if (correlationKeyName != null) {
      final String correlationKey = getEvaluatedCorrelationKey(activityUUID, correlationKeyName,
          message.getCorrelationKeyExpression1());
      eventInstance.setCorrelationKey1(correlationKey);
    }
    correlationKeyName = message.getCorrelationKeyName2();
    if (correlationKeyName != null) {
      final String correlationKey = getEvaluatedCorrelationKey(activityUUID, correlationKeyName,
          message.getCorrelationKeyExpression2());
      eventInstance.setCorrelationKey2(correlationKey);
    }
    correlationKeyName = message.getCorrelationKeyName3();
    if (correlationKeyName != null) {
      final String correlationKey = getEvaluatedCorrelationKey(activityUUID, correlationKeyName,
          message.getCorrelationKeyExpression3());
      eventInstance.setCorrelationKey3(correlationKey);
    }
    correlationKeyName = message.getCorrelationKeyName4();
    if (correlationKeyName != null) {
      final String correlationKey = getEvaluatedCorrelationKey(activityUUID, correlationKeyName,
          message.getCorrelationKeyExpression4());
      eventInstance.setCorrelationKey4(correlationKey);
    }
    correlationKeyName = message.getCorrelationKeyName5();
    if (correlationKeyName != null) {
      final String correlationKey = getEvaluatedCorrelationKey(activityUUID, correlationKeyName,
          message.getCorrelationKeyExpression5());
      eventInstance.setCorrelationKey5(correlationKey);
    }
  }

}
