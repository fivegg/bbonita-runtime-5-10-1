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
package org.ow2.bonita.definition.activity;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.hibernate.AssertionFailure;
import org.hibernate.StaleStateException;
import org.hibernate.exception.LockAcquisitionException;
import org.ow2.bonita.definition.MultiInstantiator;
import org.ow2.bonita.definition.MultiInstantiatorDescriptor;
import org.ow2.bonita.env.Authentication;
import org.ow2.bonita.facade.def.InternalActivityDefinition;
import org.ow2.bonita.facade.def.InternalConnectorDefinition;
import org.ow2.bonita.facade.def.element.BoundaryEvent;
import org.ow2.bonita.facade.def.element.DeadlineDefinition;
import org.ow2.bonita.facade.def.element.HookDefinition;
import org.ow2.bonita.facade.def.element.HookDefinition.Event;
import org.ow2.bonita.facade.def.element.MultiInstantiationDefinition;
import org.ow2.bonita.facade.def.element.impl.IterationDescriptor;
import org.ow2.bonita.facade.def.element.impl.MessageBoundaryEventImpl;
import org.ow2.bonita.facade.def.element.impl.SignalBoundaryEventImpl;
import org.ow2.bonita.facade.def.element.impl.TimerBoundaryEventImpl;
import org.ow2.bonita.facade.def.majorElement.ActivityDefinition;
import org.ow2.bonita.facade.def.majorElement.ActivityDefinition.JoinType;
import org.ow2.bonita.facade.def.majorElement.ActivityDefinition.SplitType;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.def.majorElement.TransitionDefinition;
import org.ow2.bonita.facade.exception.BonitaWrapperException;
import org.ow2.bonita.facade.exception.MultiInstantiatorInvocationException;
import org.ow2.bonita.facade.exception.UnRollbackableException;
import org.ow2.bonita.facade.runtime.ActivityInstance;
import org.ow2.bonita.facade.runtime.ActivityState;
import org.ow2.bonita.facade.runtime.InstanceState;
import org.ow2.bonita.facade.runtime.impl.InternalActivityInstance;
import org.ow2.bonita.facade.runtime.impl.InternalProcessInstance;
import org.ow2.bonita.facade.runtime.impl.InternalProcessInstance.TransitionState;
import org.ow2.bonita.facade.uuid.ActivityInstanceUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.light.LightProcessDefinition.ProcessType;
import org.ow2.bonita.runtime.ExtensionPointsPolicy;
import org.ow2.bonita.runtime.IterationDetectionPolicy;
import org.ow2.bonita.runtime.event.EventConstants;
import org.ow2.bonita.runtime.event.IncomingEventInstance;
import org.ow2.bonita.runtime.event.Job;
import org.ow2.bonita.runtime.event.JobBuilder;
import org.ow2.bonita.runtime.model.Execution;
import org.ow2.bonita.services.EventService;
import org.ow2.bonita.services.Querier;
import org.ow2.bonita.services.Recorder;
import org.ow2.bonita.type.Variable;
import org.ow2.bonita.util.BonitaConstants;
import org.ow2.bonita.util.BonitaRuntimeException;
import org.ow2.bonita.util.EnvTool;
import org.ow2.bonita.util.ExceptionManager;
import org.ow2.bonita.util.GroovyException;
import org.ow2.bonita.util.GroovyExpression;
import org.ow2.bonita.util.GroovyUtil;
import org.ow2.bonita.util.Misc;
import org.ow2.bonita.util.ProcessUtil;
import org.ow2.bonita.util.TransientData;
import org.ow2.bonita.util.VariableUtil;

/**
 * @author Marc Blachon, Guillaume Porcher, Charles Souillard, Miguel Valdes, Pierre Vigneras, Pascal Verdage
 */
/**
 * Activity life cycle: 1- when an instance is created, activity state is
 * {@link ActivityState#INITIAL} 2- when the execution arrives on the node, the
 * activity becomes {@link ActivityState#READY} the activity state is recorded
 * (before start) 3- if the activity is Manual, a task is created. - when the
 * start task is started, the activity state is recorded (after start). 4- the
 * activity becomes {@link ActivityState#EXECUTING}. The business logic is
 * executed. - the activity state is recorded (before stopped) 5- if the
 * activity is Manual, wait for the task to finish. (TODO: change the activity
 * state ?) - when the task is finished, the activity state is recorded (after
 * stopped). 6- the activity becomes {@link ActivityState#FINISHED}.
 */
public abstract class AbstractActivity implements ExternalActivity {

  private static final long serialVersionUID = -2731157748250833266L;

  /** LOG */
  static final Logger LOG = Logger.getLogger(AbstractActivity.class.getName());
  protected long dbid;

  protected String activityName;

  public static final String BODY_FINISHED = "bodyFinished";
  public static final String BODY_SKIPPED = "bodySkipped";
  public static final String ACT_INSTANCE_FINISHED = "instFinished";

  protected AbstractActivity() {
  }

  public AbstractActivity(final String activityName) {
    this.activityName = activityName;
  }

  /**
   * Return true if the execution can continue
   */
  protected abstract boolean executeBusinessLogic(Execution execution);

  protected abstract boolean bodyStartAutomatically();

  @Override
  public void execute(final Execution execution, final boolean checkJoinType) {
    final ActivityDefinition activity = execution.getNode();
    if (activity.isAsynchronous()) {
      Authentication.setUserId(BonitaConstants.SYSTEM_USER);
    }
    // If instance is ended, don't execute next node.
    if (execution.getInstance().getInstanceState().equals(InstanceState.FINISHED)) {
      if (AbstractActivity.LOG.isLoggable(Level.FINE)) {
        AbstractActivity.LOG.fine("Instance ended : " + execution.getInstance());
      }
      execution.end();
      final Execution parent = execution.getParent();
      if (parent != null) {
        parent.removeExecution(execution);
      }
      return;
    }
    // Execute node.
    boolean joinOK = true;
    joinOK = ActivityUtil.isJoinOk(execution.getInstance(), execution.getNode());
    if (joinOK || !checkJoinType) {
      if (AbstractActivity.LOG.isLoggable(Level.FINE)) {
        AbstractActivity.LOG.fine("Join for activity " + this + " is OK.");
      }
      if (activity.getJoinType().equals(JoinType.XOR)) {
        cancelJoinXORIncomingTransitions(execution);
      }

      if (AbstractActivity.LOG.isLoggable(Level.FINE)) {
        AbstractActivity.LOG.fine("Creating a new iteration on activity : " + this);
      }
      ActivityUtil.createNewIteration(execution, activity);

      if (AbstractActivity.LOG.isLoggable(Level.FINE)) {
        final String nodeName = activity.getName();
        AbstractActivity.LOG.fine("Executing node: " + nodeName + ", class = " + this.getClass().getSimpleName());
      }

      final MultiInstantiationDefinition multiInstantiator = activity.getMultiInstantiationDefinition();
      final MultiInstantiationDefinition instantiator = activity.getMultipleInstancesInstantiator();
      if (multiInstantiator != null || instantiator != null) {
        instantiateMultiInstanceActivity(execution);
      } else {

        if (activity.isInALoop() && activity.evaluateLoopConditionBeforeExecution()) {
          if (ActivityUtil.evaluateLoopCondition(activity, execution)) {
            final Execution newExecution = execution.createChildExecution(execution.getNode().getName());
            initializeActivityInstance(newExecution, null);
            startActivityInstance(newExecution);
          } else {
            terminateInstanceIfNoOutgoingTransitions(execution);
            executeSplit(execution, false);
          }

        } else {
          final Execution newExecution = execution.createChildExecution(execution.getNode().getName());
          initializeActivityInstance(newExecution, null);
          startActivityInstance(newExecution);
        }
      }
    } else {
      execution.end();
      final Execution parent = execution.getParent();
      if (parent != null) {
        parent.removeExecution(execution);
      }
    }
  }

  private void instantiateMultiInstanceActivity(final Execution execution) {
    final ActivityDefinition activity = execution.getNode();
    final MultiInstantiationDefinition instantiator = activity.getMultipleInstancesInstantiator();
    final Recorder recorder = EnvTool.getRecorder();

    RuntimeException caughtException = null;
    RuntimeException firstException = null; // the first exception will be
                                            // thrown
    final List<Execution> activitiesToStart = new ArrayList<Execution>();
    if (instantiator != null) {
      if (AbstractActivity.LOG.isLoggable(Level.FINE)) {
        AbstractActivity.LOG.fine("MultipleActivitiesInstantiation not null on activity " + this);
      }
      final List<Map<String, Object>> contexts = getContextsFromMultiInstantiator(execution, activity, instantiator);
      execution.setWaitingForActivityInstanceNb(contexts.size());
      int childId = 0;

      for (final Map<String, Object> context : contexts) {
        if (execution.getWaitingForActivityInstanceNb() <= 0) {
          // maybe this execution is ended
          break;
        }
        final Execution childExec = createChildExecution(execution, childId);

        final Set<Variable> variables = new HashSet<Variable>();
        try {
          if (context != null) {
            for (final Entry<String, Object> variable : context.entrySet()) {
              final Variable multiInstVar = VariableUtil.createVariable(activity.getProcessDefinitionUUID(),
                  variable.getKey(), variable.getValue());
              variables.add(multiInstVar);
            }
          }
        } catch (final RuntimeException e) {
          if (LOG.isLoggable(Level.SEVERE)) {
            LOG.severe("Error while creating multiInstantiator variables" + e);
          }
          caughtException = e;
        }
        try {
          initializeActivityInstance(childExec, variables);
          activitiesToStart.add(childExec);
        } catch (final StaleStateException sse) {
          throw sse;
        } catch (final AssertionFailure af) {
          throw af;
        } catch (final LockAcquisitionException lae) {
          throw lae;
        } catch (final RuntimeException e) {
          if (LOG.isLoggable(Level.SEVERE)) {
            LOG.severe("Error while initializing multiple instances" + e);
          }
          caughtException = e;
        }
        if (caughtException != null) {
          if (firstException == null) {
            firstException = caughtException;
          }
          putActivityInFailedStateIfNecessary(recorder, caughtException, childExec);
          caughtException = null;
        }
        childId++;
      }
    } else {
      if (AbstractActivity.LOG.isLoggable(Level.FINE)) {
        AbstractActivity.LOG.fine("MultiInstantiation not null on activity " + this);
      }
      MultiInstantiatorDescriptor actInstDescr = null;
      final MultiInstantiationDefinition multiDef = activity.getMultiInstantiationDefinition();
      final MultiInstantiator actInstantiator = EnvTool.getClassDataLoader().getInstance(MultiInstantiator.class,
          execution.getInstance().getProcessDefinitionUUID(), multiDef);
      try {
        actInstDescr = ConnectorExecutor.executeMultiInstantiator(execution, activity.getName(), actInstantiator,
            multiDef.getParameters());
        if (actInstDescr == null) {
          final String message = ExceptionManager.getInstance().getFullMessage("be_AA_3", activity.getName());
          throw new BonitaRuntimeException(message);
        }
      } catch (final Exception e) {
        throw new BonitaWrapperException(new MultiInstantiatorInvocationException("be_AA_4", activity
            .getMultiInstantiationDefinition().getClassName(), e));
      }

      execution.setWaitingForActivityInstanceNb(actInstDescr.getJoinNumber());
      int childId = 0;
      for (final Object value : actInstDescr.getVariableValues()) {
        if (execution.getWaitingForActivityInstanceNb() <= 0) {
          // maybe this execution is ended
          break;
        }
        final Execution childExec = createChildExecution(execution, childId);
        Variable multiInstVar = null;
        try {
          multiInstVar = VariableUtil.createVariable(activity.getProcessDefinitionUUID(), activity
              .getMultiInstantiationDefinition().getVariableName(), value);
        } catch (final StaleStateException sse) {
          throw sse;
        } catch (final AssertionFailure af) {
          throw af;
        } catch (final LockAcquisitionException lae) {
          throw lae;
        } catch (final RuntimeException e) {
          if (LOG.isLoggable(Level.SEVERE)) {
            LOG.severe("Error while creating multiInstantiator variable" + e);
          }
          caughtException = e;
        }
        final Set<Variable> variables = new HashSet<Variable>();
        if (multiInstVar != null) {
          variables.add(multiInstVar);
        }
        try {
          initializeActivityInstance(childExec, variables);
          activitiesToStart.add(childExec);
        } catch (final RuntimeException e) {
          if (LOG.isLoggable(Level.SEVERE)) {
            LOG.severe("Error while initializing multiple instances" + e);
          }
          caughtException = e;
        }

        if (caughtException != null) {
          if (firstException == null) {
            firstException = caughtException;
          }

          putActivityInFailedStateIfNecessary(recorder, caughtException, childExec);
          caughtException = null;
        }
        childId++;
      }
    }
    final List<UnRollbackableException> unrollBackableExceptions = startActivityInstances(activitiesToStart);
    if (!unrollBackableExceptions.isEmpty()) {
      throw unrollBackableExceptions.get(0);// throw the first exception
    }
    if (firstException != null
        && ExtensionPointsPolicy.THROW_EXCPTION_ON_FAIL.equals(EnvTool.getExtensionPointsPolicy())) {
      if (firstException instanceof UnRollbackableException) {
        throw firstException;
      }
      throw new UnRollbackableException("Error while initializing activity", firstException);
    }
  }

  private void putActivityInFailedStateIfNecessary(final Recorder recorder, final RuntimeException caughtException,
      final Execution childExec) {
    final InternalActivityInstance activityInstance = childExec.getActivityInstance();
    if (activityInstance != null) {
      if (!(caughtException instanceof UnRollbackableException)) {
        recorder.recordActivityFailed(activityInstance);
      }
    } else {
      throw caughtException;
    }
  }

  private List<Map<String, Object>> getContextsFromMultiInstantiator(final Execution execution,
      final ActivityDefinition activity, final MultiInstantiationDefinition instantiator) {
    List<Map<String, Object>> contexts;
    try {
      contexts = ConnectorExecutor.executeMultipleInstancesInstantiatior(instantiator, execution.getInstance()
          .getUUID(), activityName, execution.getIterationId());
      if (contexts == null) {
        final String message = ExceptionManager.getInstance().getFullMessage("be_AA_8", activity.getName());
        throw new BonitaRuntimeException(message);
      } else if (contexts.isEmpty()) {
        final String message = ExceptionManager.getInstance().getFullMessage("be_AA_9", activity.getName());
        throw new BonitaRuntimeException(message);
      }
    } catch (final Exception e) {
      throw new BonitaRuntimeException(e.getMessage(), e);
    }
    return contexts;
  }

  private Execution createChildExecution(final Execution execution, final int childId) {
    final Execution childExec = execution.createChildExecution(execution.getName() + "/" + childId);
    childExec.setActivityInstanceId(Integer.toString(childId));
    return childExec;
  }

  private List<UnRollbackableException> startActivityInstances(final List<Execution> activitiesToStart) {
    final List<UnRollbackableException> unRollbackableExceptions = new ArrayList<UnRollbackableException>();
    for (final Execution childExec : activitiesToStart) {
      if (childExec.isActive()) {
        // multi is not yet ended
        try {
          startActivityInstance(childExec);
        } catch (final StaleStateException sse) {
          throw sse;
        } catch (final AssertionFailure af) {
          throw af;
        } catch (final LockAcquisitionException lae) {
          throw lae;
        } catch (final RuntimeException e) {
          final InternalActivityInstance activityInstance = childExec.getActivityInstance();
          if (activityInstance != null) {
            EnvTool.getRecorder().recordActivityFailed(activityInstance);
            if (ExtensionPointsPolicy.THROW_EXCPTION_ON_FAIL.equals(EnvTool.getExtensionPointsPolicy())) {
              unRollbackableExceptions.add(new UnRollbackableException("Error while executing starting", e));
            }
          }
          // throw e;
        }
      }
    }
    return unRollbackableExceptions;
  }

  private void cancelJoinXORIncomingTransitions(final Execution execution) {
    final InternalActivityDefinition currentNode = execution.getNode();
    final InternalProcessInstance instance = execution.getInstance();
    cancelJoinXORIncomingTransitions(instance, currentNode, new HashSet<String>());
    for (final TransitionDefinition t : currentNode.getIncomingTransitions()) {
      instance.setTransitionState(t.getName(), TransitionState.ABORTED);
    }
  }

  private void cancelJoinXORIncomingTransitions(final InternalProcessInstance instance,
      final InternalActivityDefinition currentNode, final Set<String> checkedNodes) {
    final Set<TransitionDefinition> incomingTransitions = currentNode.getIncomingTransitions();
    final String currentNodeName = currentNode.getName();
    if (AbstractActivity.LOG.isLoggable(Level.FINE)) {
      AbstractActivity.LOG.fine("Canceling other branches of the join XOR : " + currentNodeName);
    }
    checkedNodes.add(currentNodeName);
    for (final TransitionDefinition incomingTransition : incomingTransitions) {
      final String sourceNodeName = incomingTransition.getFrom();
      final TransitionState transitionState = instance.getTransitionState(incomingTransition.getName());
      if (!checkedNodes.contains(sourceNodeName)
          && (transitionState == null || transitionState.equals(TransitionState.READY))) {
        boolean enable = false;
        final InternalActivityDefinition sourceNode = instance.getRootExecution().getProcessDefinition()
            .getActivity(sourceNodeName);
        if (transitionState != null) {
          // disable transition
          instance.setTransitionState(incomingTransition.getName(), TransitionState.ABORTED);

          // check if source node is still enabled
          // it is still enabled if it has at least one READY outgoing
          // transition (in the
          // same cycle if in a cycle)
          top: for (final TransitionDefinition tr : sourceNode.getOutgoingTransitions()) {
            final TransitionState ts = instance.getTransitionState(tr.getName());
            if (ts == null || ts.equals(TransitionState.READY)) {
              if (currentNode.isInCycle()) {
                final ProcessDefinition process = EnvTool.getJournalQueriers().getProcess(
                    instance.getProcessDefinitionUUID());
                for (final IterationDescriptor itDesc : process.getIterationDescriptors()) {
                  if (itDesc.containsNode(tr.getTo())) {
                    // stay in same cycle => do not disable node
                    enable = true;
                    break top;
                  }
                }
              }
            }
          }
        }
        if (!enable) {
          // abort sourceNode recursively: sourceNode is not enabled, maybe it
          // doesn't have any
          // activityInstance. Checks if there is one before
          if (AbstractActivity.LOG.isLoggable(Level.FINE)) {
            AbstractActivity.LOG.fine(sourceNodeName + " has no more outgoing transitions enabled.");
          }
          final List<Execution> execToAbortList = instance.getExecOnNode(sourceNodeName);
          for (final Execution execToAbort : execToAbortList) {
            destroyEvents(execToAbort);
            if (!execToAbort.isActive()) {
              execToAbort.unlock();
            }
            execToAbort.abort();
          }
          cancelJoinXORIncomingTransitions(instance, sourceNode, checkedNodes);
        }
      }
    }
  }

  protected void initializeActivityInstance(final Execution internalExecution,
      final Set<Variable> multiInstanceVariables) {
    final InternalActivityDefinition activity = internalExecution.getNode();
    final ProcessInstanceUUID instanceUUID = internalExecution.getInstance().getUUID();

    final Recorder recorder = EnvTool.getRecorder();

    Map<String, Variable> initialVariables = null;
    RuntimeException exception = null;
    try {
      initialVariables = VariableUtil.createVariables(activity.getDataFields(), instanceUUID, null);
    } catch (final StaleStateException sse) {
      throw sse;
    } catch (final AssertionFailure af) {
      throw af;
    } catch (final LockAcquisitionException lae) {
      throw lae;
    } catch (final RuntimeException t) {
      if (LOG.isLoggable(Level.SEVERE)) {
        LOG.severe("Error while initializingVariables " + t);
      }
      exception = t;
    }
    if (multiInstanceVariables != null) {
      if (initialVariables == null) {
        initialVariables = new HashMap<String, Variable>();
      }
      for (final Variable variable : multiInstanceVariables) {
        initialVariables.put(variable.getKey(), variable);
      }
    }
    String loopId = "noLoop";
    if (activity.isInALoop()) {
      loopId = Misc.getUniqueId("lp");
    }

    final String iterationId = internalExecution.getIterationId();
    final String activityInstanceId = internalExecution.getActivityInstanceId();
    final ActivityInstanceUUID activityUUID = new ActivityInstanceUUID(instanceUUID, activity.getName(), iterationId,
        activityInstanceId, loopId);

    final InternalActivityInstance activityInstance = new InternalActivityInstance(activityUUID, activity,
        instanceUUID, internalExecution.getInstance().getRootInstanceUUID(), iterationId, activityInstanceId, loopId);

    if (exception == null) {
      activityInstance.setActivityState(ActivityState.READY, BonitaConstants.SYSTEM_USER);
      activityInstance.setVariables(initialVariables);
      // add transient data
      TransientData.addTransientVariables(activityUUID,
          VariableUtil.createTransientVariables(activity.getDataFields(), instanceUUID));

      recorder.recordEnterActivity(activityInstance);

      if (activity.getDynamicDescription() != null) {
        try {
          if (GroovyExpression.isGroovyExpression(activity.getDynamicDescription())) {
            final Object dynamicDescription = GroovyUtil.evaluate(activity.getDynamicDescription(), null, activityUUID,
                false, false);
            if (dynamicDescription != null) {
              activityInstance.setDynamicDescription(dynamicDescription.toString());
            }
          } else {
            activityInstance.setDynamicDescription(activity.getDynamicDescription());
          }
        } catch (final Exception e) {
          internalExecution.setActivityInstance(activityInstance);
          throw new BonitaWrapperException(new BonitaRuntimeException("Error while evaluating dynamic description: "
              + activity.getDynamicDescription(), e));
        }
      }
      if (activity.getDynamicLabel() != null) {
        try {
          if (GroovyExpression.isGroovyExpression(activity.getDynamicLabel())) {
            final Object dynamicLabel = GroovyUtil.evaluate(activity.getDynamicLabel(), null, activityUUID, false,
                false);
            if (dynamicLabel != null) {
              activityInstance.setDynamicLabel(dynamicLabel.toString());
            }
          } else {
            activityInstance.setDynamicLabel(activity.getDynamicLabel());
          }
        } catch (final Exception e) {
          internalExecution.setActivityInstance(activityInstance);
          throw new BonitaWrapperException(new BonitaRuntimeException("Error while evaluating dynamic label: "
              + activity.getDynamicLabel(), e));
        }
      }

      internalExecution.setActivityInstance(activityInstance);
    } else {
      // recordFailed
      if (LOG.isLoggable(Level.SEVERE)) {
        LOG.log(Level.SEVERE, exception.getMessage(), exception);
      }
      recorder.recordEnterActivity(activityInstance);
      recorder.recordActivityFailed(activityInstance);
      internalExecution.setActivityInstance(activityInstance);
      throw exception;
    }
  }

  private void startActivityInstance(final Execution internalExecution) {
    final InternalActivityDefinition activity = internalExecution.getNode();
    final ActivityInstanceUUID activityUUID = internalExecution.getActivityInstanceUUID();
    try {
      initializeEvents(internalExecution);
    } catch (final GroovyException e) {
      final String message = "Error while initializing events: ";
      throw new BonitaWrapperException(new BonitaRuntimeException(message, e));
    }
    if (activity.isAsynchronous()) {
      final EventService eventService = EnvTool.getEventService();
      String uuid = internalExecution.getEventUUID();
      if (uuid == null) {
        uuid = UUID.randomUUID().toString();
      }
      final String eventName = BonitaConstants.ASYNC_EVENT_PREFIX + activityUUID;
      internalExecution.setEventUUID(uuid);
      internalExecution.lock("async continuation " + eventName);
      final Job async = JobBuilder.asyncJob(eventName, internalExecution.getInstance().getRootInstanceUUID(), uuid,
          internalExecution.getInstance().getProcessInstanceUUID());
      eventService.storeJob(async);
    } else {
      executeActivityInstance(internalExecution);
    }
  }

  protected void executeActivityInstance(final Execution internalExecution) {
    final boolean canContinue = executeBody(internalExecution);
    if (canContinue) {
      end(internalExecution);
    } else {
      internalExecution.waitForSignal();
    }
  }

  protected void end(final Execution internalExecution) {
    try {
      final ActivityDefinition activity = internalExecution.getNode();

      if (activity.getDynamicExecutionSummary() != null) {
        try {
          if (GroovyExpression.isGroovyExpression(activity.getDynamicExecutionSummary())) {
            final Object dynamicExecutionSummary = GroovyUtil.evaluate(activity.getDynamicExecutionSummary(), null,
                internalExecution.getActivityInstanceUUID(), false, false);
            if (dynamicExecutionSummary != null) {
              internalExecution.getActivityInstance().setDynamicExecutionSummary(dynamicExecutionSummary.toString());
            }
          } else {
            internalExecution.getActivityInstance().setDynamicExecutionSummary(activity.getDynamicExecutionSummary());
          }
        } catch (final Exception e) {
          throw new BonitaWrapperException(new BonitaRuntimeException(
              "Error while evaluating dynamic execution summary: " + activity.getDynamicExecutionSummary(), e));
        }
      }

      EnvTool.getRecorder().recordBodyEnded(internalExecution.getActivityInstance());

      if (activity.getMultiInstantiationDefinition() != null || activity.getMultipleInstancesInstantiator() != null) {
        endMultiInstantiation(internalExecution);
      } else if (activity.isInALoop()) {
        endLoop(internalExecution);
      } else {
        terminateInstanceIfNoOutgoingTransitions(internalExecution);
        executeSplit(internalExecution, true);
      }
    } catch (final StaleStateException sse) {
      throw sse;
    } catch (final AssertionFailure af) {
      throw af;
    } catch (final LockAcquisitionException lae) {
      throw lae;
    } catch (final UnRollbackableException e) {
      throw e;
    } catch (final RuntimeException e) {
      final InternalActivityInstance activityInstance = internalExecution.getActivityInstance();
      EnvTool.getRecorder().recordActivityFailed(activityInstance);
      if (ExtensionPointsPolicy.THROW_EXCPTION_ON_FAIL.equals(EnvTool.getExtensionPointsPolicy())) {
        throw new UnRollbackableException("Error while executing connector taskOnFinish", e);
      } else {
        if (LOG.isLoggable(Level.SEVERE)) {
          LOG.log(Level.SEVERE, e.getMessage(), e);
        }
      }
    } finally {
      TransientData.removeTransientData(internalExecution.getActivityInstance().getUUID());
    }
  }

  protected void skip(final Execution internalExecution) {
    final ActivityDefinition activity = internalExecution.getNode();

    if (activity.getDynamicExecutionSummary() != null) {
      try {
        if (GroovyExpression.isGroovyExpression(activity.getDynamicExecutionSummary())) {
          final Object dynamicExecutionSummary = GroovyUtil.evaluate(activity.getDynamicExecutionSummary(), null,
              internalExecution.getActivityInstanceUUID(), false, false);
          if (dynamicExecutionSummary != null) {
            internalExecution.getActivityInstance().setDynamicExecutionSummary(dynamicExecutionSummary.toString());
          }
        } else {
          internalExecution.getActivityInstance().setDynamicExecutionSummary(activity.getDynamicExecutionSummary());
        }
      } catch (final Exception e) {
        throw new BonitaWrapperException(new BonitaRuntimeException("Error while ending execution: "
            + activity.getDynamicExecutionSummary(), e));
      }
    }

    if (activity.getMultiInstantiationDefinition() != null || activity.getMultipleInstancesInstantiator() != null) {
      removeChildrenActivityInstances(internalExecution);
      terminateInstanceIfNoOutgoingTransitions(internalExecution);
      executeSplit(internalExecution, false);
    } else {
      terminateInstanceIfNoOutgoingTransitions(internalExecution);
      executeSplit(internalExecution, true);
    }

  }

  protected void endMultiInstantiation(final Execution internalExecution) {
    final ActivityDefinition activity = internalExecution.getNode();
    final Execution parent = internalExecution.getParent();
    if (parent.getWaitingForActivityInstanceNb() > 1) {
      final MultiInstantiationDefinition joinChecker = activity.getMultipleInstancesJoinChecker();
      if (joinChecker != null) {
        boolean join = false;
        try {
          join = ConnectorExecutor.executeMultipleInstancesJoinChecker(joinChecker, internalExecution
              .getActivityInstance().getUUID());
        } catch (final Exception e) {
          throw new BonitaRuntimeException(e.getMessage(), e);
        }
        if (join) {
          parent.setWaitingForActivityInstanceNb(1);
        }
      }
    }
    destroyEvents(internalExecution);
    internalExecution.end();
    parent.removeExecution(internalExecution);
    signal(parent, AbstractActivity.ACT_INSTANCE_FINISHED, null);
  }

  protected void endLoop(final Execution internalExecution) {
    final ActivityDefinition activity = internalExecution.getNode();
    final Execution parent = internalExecution.getParent();
    int maxIterations = 0;
    final String maxIterationsExpr = activity.getLoopMaximum();
    if (maxIterationsExpr != null) {
      try {
        if (Misc.isJustAGroovyExpression(maxIterationsExpr)) {
          final ProcessInstanceUUID instanceUUID = internalExecution.getInstance().getUUID();
          final Querier journal = EnvTool.getJournalQueriers();
          final ActivityInstance activityInstance = journal.getActivityInstance(instanceUUID,
              internalExecution.getNodeName(), internalExecution.getIterationId(),
              internalExecution.getActivityInstanceId(), internalExecution.getActivityInstance().getLoopId());
          maxIterations = (Integer) GroovyUtil.evaluate(maxIterationsExpr, null, activityInstance.getUUID(), false,
              false);
        } else {
          maxIterations = Integer.parseInt(maxIterationsExpr);
        }
      } catch (final Exception e) {
        AbstractActivity.LOG.log(Level.SEVERE, "The maximum number of loop iterations for activity " + activityName
            + " must be an integer or an expression that evaluates to an integer", e);
      }
    }
    parent.setWaitingForActivityInstanceNb(maxIterations);

    signal(parent, AbstractActivity.ACT_INSTANCE_FINISHED, null);
    if (!internalExecution.isFinished()) {
      // the number of iteration reached the maximum loop iterations authorized
      boolean loop = true;
      if (!activity.evaluateLoopConditionBeforeExecution()) {
        loop = ActivityUtil.evaluateLoopCondition(activity, internalExecution);
      }
      if (loop) {
        parent.removeExecution(internalExecution);
        execute(parent, false);
      } else {
        terminateInstanceIfNoOutgoingTransitions(internalExecution);
        executeSplit(internalExecution, true);
      }
    }
  }

  private void terminateInstanceIfNoOutgoingTransitions(final Execution internalExecution) {
    final ActivityDefinition activity = internalExecution.getNode();
    final InternalProcessInstance instance = internalExecution.getInstance();
    final ProcessInstanceUUID instanceUUID = instance.getUUID();
    final ActivityInstanceUUID activityUUID = internalExecution.getActivityInstanceUUID();
    if (activity.isTerminateProcess()
        || !(activity.hasOutgoingTransitions() || hasStillReadyActivities(instanceUUID, activityUUID) || hasStillReadyTransitions(instance))) {

      final ProcessInstanceUUID parentInstanceUUID = instance.getParentInstanceUUID();
      ConnectorExecutor.executeConnectors(internalExecution, HookDefinition.Event.instanceOnFinish);
      final Recorder recorder = EnvTool.getRecorder();
      recorder.recordInstanceEnded(instance.getUUID(), EnvTool.getUserId());
      ProcessUtil.removeInternalInstanceEvents(instance.getUUID());

      if (parentInstanceUUID != null) {
        if (ProcessType.EVENT_SUB_PROCESS.equals(internalExecution.getProcessDefinition().getType())) {
          final InternalProcessInstance parentInstance = EnvTool.getJournalQueriers().getProcessInstance(
              parentInstanceUUID);
          if (parentInstance.getParentActivityUUID() != null) {
            final Execution rootExecution = EnvTool.getAllQueriers().getExecutionOnActivity(
                parentInstance.getParentInstanceUUID(), parentInstance.getParentActivityUUID());
            try {
              rootExecution.getNode().getBehaviour().signal(rootExecution, BODY_FINISHED, null);
            } catch (final Exception e) {
              throw new BonitaRuntimeException(e.getMessage(), e);
            }
          } else {
            parentInstance.finish();
          }
        } else {
          // We are in a subflow
          final Map<String, Object> signalParameters = new HashMap<String, Object>();
          signalParameters.put("childInstanceUUID", instanceUUID);

          final InternalProcessInstance parentInstance = EnvTool.getJournalQueriers().getProcessInstance(
              parentInstanceUUID);
          final Execution parentRootExecution = parentInstance.getRootExecution();
          final Execution execToSignal = getSubflowExecution(parentRootExecution, instanceUUID);

          try {
            execToSignal.getNode().getBehaviour().signal(execToSignal, SubFlow.SUBFLOW_SIGNAL, signalParameters);
          } catch (final Exception e) {
            throw new BonitaRuntimeException(e.getMessage(), e);
          }
        }
      } else {
        instance.finish();
      }
    }
  }

  private boolean hasStillReadyActivities(final ProcessInstanceUUID instanceUUID,
      final ActivityInstanceUUID activityUUID) {
    return EnvTool.getJournalQueriers().containsOtherActiveActivities(instanceUUID, activityUUID);
  }

  private boolean hasStillReadyTransitions(final InternalProcessInstance instance) {
    boolean hasStillReadyTransitions = false;
    final Iterator<String> iterator = instance.getTransitionsStates().values().iterator();
    while (!hasStillReadyTransitions && iterator.hasNext()) {
      final String state = iterator.next();
      if (TransitionState.READY.toString().equals(state)) {
        hasStillReadyTransitions = true;
      }
    }
    return hasStillReadyTransitions;
  }

  private Execution getSubflowExecution(final Execution exec, final ProcessInstanceUUID subflowInstanceUUID) {
    if (exec.getActivityInstance() != null && exec.getActivityInstance().getSubflowProcessInstanceUUID() != null
        && exec.getActivityInstance().getSubflowProcessInstanceUUID().equals(subflowInstanceUUID)) {
      return exec;
    }
    for (final Execution child : exec.getExecutions()) {
      final Execution e = getSubflowExecution(child, subflowInstanceUUID);
      if (e != null) {
        return e;
      }
    }
    return null;
  }

  @Override
  @SuppressWarnings("deprecation")
  public void signal(final Execution execution, final String signal, final Map<String, Object> parameters) {
    final Execution internalExecution = execution;
    final InternalActivityDefinition activity = execution.getNode();
    if (AbstractActivity.BODY_FINISHED.equals(signal)) {
      end(internalExecution);
    } else if (AbstractActivity.ACT_INSTANCE_FINISHED.equals(signal)) {
      if (activity.getMultiInstantiationDefinition() != null || activity.getMultipleInstancesInstantiator() != null) {
        // in case of a multi-instantiation
        internalExecution.setWaitingForActivityInstanceNb(internalExecution.getWaitingForActivityInstanceNb() - 1);
        if (internalExecution.getWaitingForActivityInstanceNb() == 0) {
          removeChildrenActivityInstances(internalExecution);
          terminateInstanceIfNoOutgoingTransitions(internalExecution);
          executeSplit(internalExecution, false);
        }
      } else {
        // in case of a loop
        internalExecution.setActivityInstanceNb(internalExecution.getActivityInstanceNb() + 1);
        final int maxIterations = internalExecution.getWaitingForActivityInstanceNb();
        if (0 < maxIterations && maxIterations <= internalExecution.getActivityInstanceNb()) {
          endChildrenActivityInstances(internalExecution);
          terminateInstanceIfNoOutgoingTransitions(internalExecution);
          executeSplit(internalExecution, false);
        }
      }
    } else if (EventConstants.ASYNC.equals(signal)) {
      Authentication.setUserId(BonitaConstants.SYSTEM_USER);
      executeActivityInstance(internalExecution);
    } else if (EventConstants.DEADLINE.equals(signal)) {
      final Long id = (Long) parameters.get("id");
      DeadlineDefinition deadline = null;
      if (id != null) {
        deadline = getMatchingDeadline(id, activity.getDeadlines());
      } else {
        final String className = (String) parameters.get("className");
        deadline = getCompatibleDeadline(className, activity.getDeadlines());
      }
      if (deadline != null) {
        Authentication.setUserId(BonitaConstants.SYSTEM_USER);
        // By default, a deadline does not propagate execution
        internalExecution.waitForSignal();
        final String activityId = internalExecution.getNode().getName();
        ConnectorExecutor.executeConnector(internalExecution, activityId, deadline);
      }
    } else if (EventConstants.BOUNDARY.equals(signal)) {
      destroyEvents(internalExecution);
      if (activity.getMultiInstantiationDefinition() != null || activity.getMultipleInstancesInstantiator() != null) {
        if (internalExecution.getParent() != null) {
          for (final Execution execToAbort : new ArrayList<Execution>(internalExecution.getParent().getExecutions())) {
            if (!execToAbort.equals(internalExecution) && execToAbort.isActive()) {
              execToAbort.abort();
            }
          }
        }
      }
      final InternalActivityInstance activityInstance = internalExecution.getActivityInstance();
      EnvTool.getRecorder().recordBodyAborted(activityInstance);
      TransientData.removeTransientData(activityInstance.getUUID());
      if (activity.isSubflow()) {
        final InternalProcessInstance subprocessInstance = EnvTool.getJournalQueriers().getProcessInstance(
            activityInstance.getSubflowProcessInstanceUUID());
        EnvTool.getRecorder().recordInstanceAborted(subprocessInstance.getUUID(), BonitaConstants.SYSTEM_USER);
      }
      internalExecution.setActivityInstance(null);
      if (EventConstants.MESSAGE_BOUNDARY_EVENT.equals(signal)) {
        ConnectorExecutor.executeConnectors(activity, execution, Event.onEvent, parameters);
      }
      final BoundaryEvent event = activity.getBoundaryEvent(parameters.get("eventName").toString());
      final TransitionDefinition exceptionTransition = event.getTransition();
      internalExecution.take(exceptionTransition);
    } else if (AbstractActivity.BODY_SKIPPED.equals(signal)) {
      skip(internalExecution);
    } else if (EventConstants.CONNECTORS_AUTOMATIC_ON_ENTER_EXECUTED.equals(signal)) {
      ConnectorExecutor.executeConnectors(activity, execution, Event.automaticOnExit);
      end(internalExecution);
    }
  }

  private DeadlineDefinition getCompatibleDeadline(final String className, final Set<DeadlineDefinition> deadlines) {
    for (final DeadlineDefinition deadline : deadlines) {
      if (deadline.getClassName().equals(className)) {
        return deadline;
      }
    }
    return null;
  }

  private DeadlineDefinition getMatchingDeadline(final Long id, final Set<DeadlineDefinition> deadlines) {
    for (final DeadlineDefinition d : deadlines) {
      final InternalConnectorDefinition deadline = (InternalConnectorDefinition) d;
      if (deadline.getDbid() == id) {
        return deadline;
      }
    }
    return null;
  }

  private void removeChildrenActivityInstances(final Execution execution) {
    if (execution.getExecutions() != null) {
      for (final Execution execToAbort : new ArrayList<Execution>(execution.getExecutions())) {
        execToAbort.abort();
      }
    }
  }

  private void endChildrenActivityInstances(final Execution execution) {
    if (execution.getExecutions() != null) {
      for (final Execution execToEnd : new ArrayList<Execution>(execution.getExecutions())) {
        execToEnd.end();
        execution.removeExecution(execToEnd);
      }
    }
  }

  private void initializeEvents(final Execution execution) throws GroovyException {
    final InternalActivityDefinition activity = execution.getNode();
    final Set<DeadlineDefinition> deadlines = activity.getDeadlines();
    final List<BoundaryEvent> boundaryEvents = activity.getBoundaryEvents();
    if (!deadlines.isEmpty() || !boundaryEvents.isEmpty()) {
      final String executionEventUUID = "event-" + UUID.randomUUID().toString();
      execution.setEventUUID(executionEventUUID);
      initializeTimers(execution, executionEventUUID);
      initializeBoundaryEvents(execution, executionEventUUID);
    }
  }

  private void initializeTimers(final Execution execution, final String executionEventUUID) throws GroovyException {
    // initialize the timers
    final InternalActivityDefinition activity = execution.getNode();
    final ActivityInstanceUUID activityUUID = execution.getActivityInstanceUUID();
    final Set<DeadlineDefinition> deadlines = activity.getDeadlines();
    if (!deadlines.isEmpty()) {
      final EventService eventService = EnvTool.getEventService();
      for (final DeadlineDefinition d : deadlines) {
        final InternalConnectorDefinition deadline = (InternalConnectorDefinition) d;
        final String condition = deadline.getCondition();
        final long fireTime = ProcessUtil.getTimerDate(condition, activityUUID).getTime();
        final Job dl = JobBuilder.deadlineJob("" + deadline.getDbid(), execution.getInstance().getRootInstanceUUID(),
            executionEventUUID, fireTime, execution.getInstance().getProcessInstanceUUID());
        eventService.storeJob(dl);
      }
    }
  }

  private void initializeBoundaryEvents(final Execution execution, final String executionEventUUID)
      throws GroovyException {
    final InternalActivityDefinition activity = execution.getNode();
    final List<BoundaryEvent> boundaryEvents = activity.getBoundaryEvents();
    if (!boundaryEvents.isEmpty()) {
      final ActivityInstanceUUID activityUUID = execution.getActivityInstanceUUID();
      final ActivityInstance activityInstance = execution.getActivityInstance();
      final ProcessInstanceUUID instanceUUID = activityInstance.getProcessInstanceUUID();
      final EventService eventService = EnvTool.getEventService();
      final String processName = execution.getProcessDefinition().getName();
      for (final BoundaryEvent boundaryEvent : boundaryEvents) {
        if (boundaryEvent instanceof TimerBoundaryEventImpl) {
          final TimerBoundaryEventImpl timer = (TimerBoundaryEventImpl) boundaryEvent;
          final String eventName = timer.getName();
          final String condition = timer.getCondition();
          final Date date = ProcessUtil.getTimerDate(condition, activityUUID);
          final ProcessInstanceUUID rootInstanceUUID = execution.getInstance().getRootInstanceUUID();
          final Job boundaryTimer = JobBuilder.boundaryTimerJob(eventName, rootInstanceUUID, executionEventUUID,
              date.getTime(), instanceUUID);
          eventService.storeJob(boundaryTimer);
        } else if (boundaryEvent instanceof MessageBoundaryEventImpl) {
          final MessageBoundaryEventImpl message = (MessageBoundaryEventImpl) boundaryEvent;
          final String expression = message.getExpression();
          final IncomingEventInstance eventInstance = new IncomingEventInstance(message.getName(), expression,
              instanceUUID, activity.getUUID(), activityUUID, processName, activityName, executionEventUUID,
              EventConstants.MESSAGE_BOUNDARY_EVENT, System.currentTimeMillis(), true);
          ActivityUtil.addCorrelationKeys(message, eventInstance, activityUUID);
          eventService.subscribe(eventInstance);
        } else if (boundaryEvent instanceof SignalBoundaryEventImpl) {
          final SignalBoundaryEventImpl signal = (SignalBoundaryEventImpl) boundaryEvent;
          final String signalName = signal.getSignalCode();
          final IncomingEventInstance signalEventInstance = new IncomingEventInstance(signalName, null, instanceUUID,
              activity.getUUID(), null, processName, signal.getName(), executionEventUUID,
              EventConstants.SIGNAL_BOUNDARY_EVENT, -1, false);
          eventService.subscribe(signalEventInstance);
        }
      }
    }
  }

  private static void destroyEvents(final Execution execution) {
    if (LOG.isLoggable(Level.FINE)) {
      LOG.fine("destroying events of " + execution.toString());
    }
    final ActivityInstanceUUID activityUUID = execution.getActivityInstanceUUID();
    if (activityUUID != null) {
      if (execution.getNode() == null || execution.getNode().hasBoundaryEvents()) {
        ActivityUtil.deleteBoundaryEvents(activityUUID);
      }
      ActivityUtil.deleteJobs(execution.getEventUUID());
    }
  }

  private boolean executeBody(final Execution internalExecution) {
    if (bodyStartAutomatically()) {
      EnvTool.getRecorder().recordBodyStarted(internalExecution.getActivityInstance());
    }
    return executeBusinessLogic(internalExecution);
  }

  @Override
  public String toString() {
    final StringBuffer buffer = new StringBuffer();
    buffer.append(this.getClass().getName());
    buffer.append(": activtyName: " + getActivityName());
    return buffer.toString();
  }

  public String getActivityName() {
    return activityName;
  }

  public void executeSplit(final Execution execution, final boolean removeScope) {
    final ActivityDefinition activity = execution.getNode();
    Execution internalExecution = execution;
    final InternalActivityDefinition currentNode = internalExecution.getNode();
    if (AbstractActivity.LOG.isLoggable(Level.FINE)) {
      AbstractActivity.LOG.fine("node = " + currentNode.getName() + " - splitType = " + activity.getSplitType()
          + " - execution = " + execution.getName());
    }

    final Set<TransitionDefinition> transitions = currentNode.getOutgoingTransitions();

    if (transitions.isEmpty()) {
      if (AbstractActivity.LOG.isLoggable(Level.FINE)) {
        AbstractActivity.LOG.fine("node = " + currentNode.getName() + " - splitType = " + activity.getSplitType()
            + " - execution = " + execution.getName() + " no transition available. Ending execution");
      }
      internalExecution.end();
      final Execution parent = internalExecution.getParent();
      if (parent != null) {
        parent.removeExecution(internalExecution);
      }
    } else {
      TransitionDefinition defaultTransition = null;
      final List<TransitionDefinition> transitionsToTake = new ArrayList<TransitionDefinition>();
      for (final TransitionDefinition t : transitions) {
        if (t.isDefault()) {
          defaultTransition = t;
        } else if (ActivityUtil.evaluateTransition(t, internalExecution)) {
          final TransitionState transitionState = internalExecution.getInstance().getTransitionState(t.getName());
          if (transitionState == null || transitionState.equals(TransitionState.READY)) {
            internalExecution.getInstance().setTransitionState(t.getName(), TransitionState.READY);
            transitionsToTake.add(t);
          }
        }
      }
      if (defaultTransition != null && transitionsToTake.size() == 0) {
        final TransitionState transitionState = internalExecution.getInstance().getTransitionState(
            defaultTransition.getName());
        if (transitionState == null) {
          internalExecution.getInstance().setTransitionState(defaultTransition.getName(), TransitionState.READY);
          transitionsToTake.add(defaultTransition);
        }
      }
      // remove not propagated variables
      if (removeScope) {
        destroyEvents(internalExecution);
        internalExecution = internalExecution.backToParent();
      }
      internalExecution.setActivityInstance(null);
      if (transitionsToTake.size() == 0) {
        internalExecution.end();
        final Execution parent = internalExecution.getParent();
        if (parent != null) {
          parent.removeExecution(internalExecution);
        }
      } else {
        Set<IterationDescriptor> iterationDescriptors = null;
        // check we are leaving a cycle
        if (activity.isInCycle()) {
          final ProcessDefinition process = EnvTool.getJournalQueriers()
              .getProcess(activity.getProcessDefinitionUUID());
          iterationDescriptors = process.getIterationDescriptors();
          for (final IterationDescriptor itD : iterationDescriptors) {
            boolean isLeaving = false;
            for (final TransitionDefinition t : transitionsToTake) {
              if (!itD.containsNode(t.getTo()) && itD.containsNode(t.getFrom())) {
                isLeaving = true;
              }
            }
            if (isLeaving) {
              // abort execution of other nodes.
              if (AbstractActivity.LOG.isLoggable(Level.FINE)) {
                AbstractActivity.LOG.fine(activity.getName() + " is leaving a cycle, aborting other nodes in cycle.");
              }
              for (final String nodeToAbort : itD.getCycleNodes()) {
                if (!nodeToAbort.equals(currentNode.getName())) {
                  final List<Execution> execToAbortList = internalExecution.getInstance().getExecOnNode(nodeToAbort);
                  for (final Execution execToAbort : execToAbortList) {
                    if (execToAbort.isActive()) {
                      execToAbort.abort();
                    }
                  }
                }
              }
            }
          }
        }
        if (transitionsToTake.size() == 1 || SplitType.XOR.equals(activity.getSplitType())) {
          // We are in a Split/AND and only one transition is true,
          // or we are in a Split/XOR, so we take the first one that is true.
          final TransitionDefinition t = transitionsToTake.get(0);
          if (AbstractActivity.LOG.isLoggable(Level.FINE)) {
            AbstractActivity.LOG.fine("Taking transition " + t);
          }
          internalExecution.take(t);
        } else {
          // We are in a Split/AND and more than one transition is true.
          // check we are not leaving a cycle and staying in the cycle at the
          // same time
          if (EnvTool.getIterationDetectionPolicy() == IterationDetectionPolicy.ENABLE && activity.isInCycle()) {
            for (final IterationDescriptor itD : iterationDescriptors) {
              boolean isLeaving = false;
              boolean isStaying = false;
              for (final TransitionDefinition t : transitionsToTake) {
                if (!itD.containsNode(t.getTo())) {
                  isLeaving = true;
                } else {
                  isStaying = true;
                }
              }
              if (isStaying && isLeaving) {
                final String message = ExceptionManager.getInstance().getFullMessage("be_AA_5");
                throw new BonitaWrapperException(new BonitaRuntimeException(message));
              }
            }
          }
          final List<Execution> children = new ArrayList<Execution>();
          for (int i = 0; i < transitionsToTake.size(); i++) {
            final TransitionDefinition t = transitionsToTake.get(i);
            final String name = t.getFrom() + "_to_" + t.getTo();
            final Execution childExecution = internalExecution.createChildExecution(name);
            children.add(childExecution);
          }
          for (int i = 0; i < transitionsToTake.size(); i++) {
            final Execution childExecution = children.get(i);
            final TransitionDefinition t = transitionsToTake.get(i);
            if (!childExecution.isFinished()) {
              if (AbstractActivity.LOG.isLoggable(Level.FINE)) {
                AbstractActivity.LOG.fine("Execution " + childExecution.getName() + " is taking transition " + t);
              }
              childExecution.take(t);
            }
          }
        }
      }
    }
  }

}
