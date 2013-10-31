/**
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 * 
 * Modified by Matthieu Chaffotte - BonitaSoft S.A.
 */
package org.ow2.bonita.runtime.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.transaction.Status;
import javax.transaction.Synchronization;

import org.ow2.bonita.definition.activity.ConnectorExecutor;
import org.ow2.bonita.definition.activity.ExternalActivity;
import org.ow2.bonita.env.Environment;
import org.ow2.bonita.facade.def.InternalActivityDefinition;
import org.ow2.bonita.facade.def.InternalProcessDefinition;
import org.ow2.bonita.facade.def.element.HookDefinition;
import org.ow2.bonita.facade.def.majorElement.TransitionDefinition;
import org.ow2.bonita.facade.runtime.impl.InternalActivityInstance;
import org.ow2.bonita.facade.runtime.impl.InternalProcessInstance;
import org.ow2.bonita.facade.runtime.impl.InternalProcessInstance.TransitionState;
import org.ow2.bonita.facade.uuid.ActivityDefinitionUUID;
import org.ow2.bonita.facade.uuid.ActivityInstanceUUID;
import org.ow2.bonita.services.Querier;
import org.ow2.bonita.services.Recorder;
import org.ow2.bonita.util.BonitaRuntimeException;
import org.ow2.bonita.util.Command;
import org.ow2.bonita.util.EnvTool;
import org.ow2.bonita.util.EqualsUtil;
import org.ow2.bonita.util.ExceptionManager;
import org.ow2.bonita.util.ProcessUtil;
import org.ow2.bonita.util.TransientData;

/**
 * @author Tom Baeyens
 */
public class Execution implements Serializable {

  private static final long serialVersionUID = 1L;
  private static final Logger LOG = Logger.getLogger(Execution.class.getName());

  public static final String INITIAL_ITERATION_ID = "it1";

  protected long id;
  protected int dbversion;

  protected String activityInstanceId = "mainActivityInstance"; // used when this execution points a node in a multi
                                                                // instantiation
  protected String iterationId;
  protected int waitingForActivityInstanceNb;
  protected int activityInstanceNb;

  protected InternalProcessInstance instance;
  protected InternalActivityInstance activityInstance;
  protected String name;
  protected String state;
  protected String eventUUID;
  protected Propagation propagation = null;
  protected InternalProcessDefinition processDefinition;
  protected InternalActivityDefinition node;
  protected Collection<Execution> executions;
  protected Execution parent;

  /** the queue of atomic operations to be performed for this execution. */
  protected Queue<ExecuteNode> atomicOperations;

  public enum Propagation {
    UNSPECIFIED, WAIT, EXPLICIT
  }

  /**
   * between creation of a new process instance and the {@link Execution#beginWithOneStartNode() start} of that process
   * instance. The motivation of this state is that variables can be set programmatically on the process instance so
   * that they can be used during initializations of variables and timers
   */
  public static final String STATE_CREATED = "created";
  /**
   * either executing or in a wait state waiting for a signal. This is the normal state of an execution and the initial
   * state when creating a new execution. Make sure that comparisons are done with .equals and not with '==' because if
   * executions are loaded from persistent storage, a new string is created instead of the constants.
   */
  public static final String STATE_ACTIVE = "active";
  /**
   * parents with concurrent child executions are inactive. When an execution has concurrent child executions, it
   * implies that this execution can't be active. For example, at a fork, the parent execution can wait inactively in
   * the fork being till all the child executions are joined. Only leaves of the execution tree can be active. Make sure
   * that comparisons are done with .equals and not with '==' because if executions are loaded from persistent storage,
   * a new string is created instead of the constants.
   */
  public static final String STATE_INACTIVE = "inactive";
  /**
   * this execution has ended normally. Make sure that comparisons are done with .equals and not with '==' because if
   * executions are loaded from persistent storage, a new string is created instead of the constants.
   */
  public static final String STATE_ENDED = "ended";
  /**
   * this execution was cancelled with the {@link #cancel()} method before normal execution ended. Make sure that
   * comparisons are done with .equals and not with '==' because if executions are loaded from persistent storage, a new
   * string is created instead of the constants.
   */
  public static final String STATE_CANCELLED = "cancelled";
  /** indicates that this execution is doing an asynchronous continuation. */
  public static final String STATE_ASYNC = "async";

  // Mandatory for hibernate
  protected Execution() {
  }

  public Execution(final String name, final InternalProcessDefinition processDefinition,
      final InternalProcessInstance processInstance, final InternalActivityDefinition activity, final String state,
      final String iterationId) {
    this.processDefinition = processDefinition;
    instance = processInstance;
    this.name = name;
    this.state = state;
    node = activity;
    this.iterationId = iterationId;
  }

  public void beginWithOneStartNode() {
    setIterationId(INITIAL_ITERATION_ID);
    if (!STATE_CREATED.equals(state)) {
      final String message = ExceptionManager.getInstance().getFullMessage("bp_EI_1 ", toString(), state);
      throw new BonitaRuntimeException(message);
    }
    state = STATE_ACTIVE;
    if (node != null) {
      performAtomicOperation(new ExecuteNode());
    }
  }

  public void beginWithManyStartNodes(final ActivityDefinitionUUID activityUUID) {
    beginWithOneStartNode();
    for (final InternalActivityDefinition activity : getProcessDefinition().getInternalInitialActivities().values()) {
      if (activityUUID == null && !activity.isReceiveEvent() || activity.getUUID().equals(activityUUID)) {
        final Execution child = createChildExecution(activity.getName());
        child.execute(activity);
      }
    }
  }

  public Execution createChildExecution(final String name) {
    // creating a child execution implies that this execution
    // is not a leave any more and therefore, it is inactivated
    if (isActive()) {
      lock(STATE_INACTIVE);
      propagation = Propagation.EXPLICIT;
    }

    // create child execution
    final Execution child = new Execution(name, getProcessDefinition(), getInstance(), getNode(), STATE_ACTIVE,
        getIterationId());
    if (LOG.isLoggable(Level.FINE)) {
      LOG.fine("creating " + child);
    }

    // copy the current state from the child execution to the parent execution
    child.setPropagation(getPropagation());

    // add it to this execution
    addExecution(child);

    return child;
  }

  public Execution backToParent() {
    if (parent == null) {
      return this;
    }
    // copy the current state from the child execution to the parent execution
    getParent().setNode(getNode());
    getParent().setPropagation(getPropagation());

    end();
    parent.removeExecution(this);

    return parent;
  }

  @Override
  public String toString() {
    final StringBuilder builder = new StringBuilder();
    builder.append("execution, name=").append(getName()).append(", parent= ").append(getParent())
        .append(", instance= ").append(getInstance()).append(", activityInstanceUUID= ")
        .append(getActivityInstanceUUID());
    return builder.toString();
  }

  public void end() {
    end(Execution.STATE_ENDED);
  }

  public void end(final String state) {
    if (state == null) {
      final String message = ExceptionManager.getInstance().getFullMessage("bp_EI_2");
      throw new BonitaRuntimeException(message);
    }
    if (state.equals(STATE_ACTIVE) || state.equals(STATE_CREATED) || state.equals(STATE_INACTIVE)
        || state.equals(STATE_ASYNC)) {
      final String message = ExceptionManager.getInstance().getFullMessage("bp_EI_3", state);
      throw new BonitaRuntimeException(message);
    }

    if (LOG.isLoggable(Level.FINE)) {
      LOG.fine(toString() + " ends with state " + state);
    }

    // end all child executions
    if (executions != null) {
      for (final Execution child : executions) {
        child.end(state);
      }
    }
    lock(state);
    propagation = Propagation.EXPLICIT;
  }

  public void cancel() {
    if (getExecutions() != null) {
      for (final Execution child : new ArrayList<Execution>(getExecutions())) {
        child.cancel();
      }
    }
    if (LOG.isLoggable(Level.FINE)) {
      LOG.fine(this + " cancelled.");
    }
    if (getActivityInstanceUUID() != null) {
      final boolean isSubflow = getNode().isSubflow();
      if (isSubflow) {
        final Querier journal = EnvTool.getJournalQueriers();
        final InternalProcessInstance childInstance = journal.getProcessInstance(getActivityInstance()
            .getSubflowProcessInstanceUUID());
        childInstance.cancel();
      }
      EnvTool.getRecorder().recordBodyCancelled(getActivityInstance());
      TransientData.removeTransientData(getActivityInstanceUUID());
    }
    if (!STATE_ACTIVE.equals(state)) {
      unlock();
    }
    end(Execution.STATE_CANCELLED);
    final Execution parent = getParent();
    if (parent != null) {
      parent.removeExecution(this);
    }
  }

  public void abort() {
    if (getExecutions() != null) {
      for (final Execution child : new ArrayList<Execution>(getExecutions())) {
        child.abort();
      }
    }
    if (LOG.isLoggable(Level.FINE)) {
      LOG.fine(this + " aborted.");
    }
    ConnectorExecutor.executeConnectors(this, HookDefinition.Event.instanceOnAbort);
    if (getActivityInstanceUUID() != null) {
      final boolean isSubflow = getNode().isSubflow();
      if (isSubflow && getActivityInstance().getSubflowProcessInstanceUUID() != null) {
        // execution has been started only if getSubflowProcessInstanceUUID is not null
        final Querier journal = EnvTool.getJournalQueriers();
        final InternalProcessInstance childInstance = journal.getProcessInstance(getActivityInstance()
            .getSubflowProcessInstanceUUID());
        childInstance.getRootExecution().abort();
        final Recorder recorder = EnvTool.getRecorder();

        recorder.recordInstanceAborted(childInstance.getUUID(), EnvTool.getUserId());
        ProcessUtil.removeInternalInstanceEvents(instance.getUUID());
      }

      EnvTool.getRecorder().recordBodyAborted(getActivityInstance());
      TransientData.removeTransientData(getActivityInstanceUUID());
    }
    end(Execution.STATE_CANCELLED);
    final Execution parent = getParent();
    if (parent != null) {
      parent.removeExecution(this);
    }
  }

  public void signal(final String signal, final Map<String, Object> parameters) {
    checkLock();
    if (node != null) {
      if (LOG.isLoggable(Level.FINE)) {
        LOG.fine(toString() + " signals " + node);
      }
      final ExternalActivity externalActivity = node.getBehaviour();
      try {
        setPropagation(Propagation.UNSPECIFIED);
        externalActivity.signal(this, signal, parameters);
      } catch (final RuntimeException e) {
        throw e;
      } catch (final Exception e) {
        final String message = ExceptionManager.getInstance().getFullMessage("bp_S_1", node, e.getMessage());
        throw new BonitaRuntimeException(message, e);
      }

      if (getPropagation() == Propagation.UNSPECIFIED) {
        proceed();
      }
    } else {
      final String message = ExceptionManager.getInstance().getFullMessage("bp_EI_6");
      throw new BonitaRuntimeException(message);
    }
  }

  public void take(final TransitionDefinition transition) {
    instance.setTransitionState(transition.getName(), TransitionState.TAKEN);
    checkLock();

    setPropagation(Propagation.EXPLICIT);

    if (LOG.isLoggable(Level.FINE)) {
      LOG.fine(toString() + " takes " + transition);
    }
    setNode(getProcessDefinition().getActivity(transition.getTo()));

    performAtomicOperation(new ExecuteNode());
  }

  public void execute(final InternalActivityDefinition node) {
    if (node == null) {
      final String message = ExceptionManager.getInstance().getFullMessage("bp_EI_12");
      throw new BonitaRuntimeException(message);
    }
    checkLock();

    propagation = Propagation.EXPLICIT;
    this.node = node;
    performAtomicOperation(new ExecuteNode());
  }

  // execution method : waitForSignal /////////////////////////////////////////

  public void waitForSignal() {
    propagation = Propagation.WAIT;
  }

  // execution method : proceed ///////////////////////////////////////////////

  public void proceed() {
    checkLock();

    // in graph based processDefinition languages we assume that a
    // default transition is available
    final TransitionDefinition defaultTransition = getDefaultTransition(node);
    if (defaultTransition != null) {
      take(defaultTransition);

      // in block structured processDefinition languages we assume that
      // there is no default transition and that there is a
      // parent node of the current node
    } else {
      // When we don't know how to proceed, i don't know if it's best to throw new
      // BonitaRuntimeException("don't know how to proceed");
      // or to end the execution. Because of convenience for testing,
      // I opted to end the execution.
      end();
    }
  }

  public void move(final InternalActivityDefinition destination, final Execution execution) {
    execution.move(destination);
  }

  public void move(final InternalActivityDefinition destination) {
    checkLock();
    setNode(destination);
  }

  /** @see Execution#lock(String) */
  public void lock(final String state) {
    if (state == null) {
      final String message = ExceptionManager.getInstance().getFullMessage("bp_EI_22");
      throw new BonitaRuntimeException(message);
    }
    checkLock();
    if (LOG.isLoggable(Level.FINE)) {
      LOG.fine("locking " + this);
    }
    this.state = state;
  }

  /** @see Execution#unlock() */
  public void unlock() {
    if (STATE_ACTIVE.equals(state)) {
      final String message = ExceptionManager.getInstance().getFullMessage("bp_EI_23");
      throw new BonitaRuntimeException(message);
    }
    if (LOG.isLoggable(Level.FINE)) {
      LOG.fine("unlocking " + this);
    }
    state = STATE_ACTIVE;
  }

  // state : internal methods /////////////////////////////////////////////////

  protected void checkLock() {
    if (!STATE_ACTIVE.equals(state)) {
      final String message = ExceptionManager.getInstance().getFullMessage("bp_EI_24", toString(), state);
      throw new BonitaRuntimeException(message);
    }
  }

  public void performAtomicOperation(final ExecuteNode operation) {
    performAtomicOperation(operation, true);
  }

  public void performAtomicOperation(final ExecuteNode operation, final boolean checkJoinType) {
    if (atomicOperations == null) {

      // initialise the fifo queue of atomic operations
      atomicOperations = new LinkedList<ExecuteNode>();
      atomicOperations.offer(operation);

      try {
        while (!atomicOperations.isEmpty()) {
          final ExecuteNode atomicOperation = atomicOperations.poll();
          atomicOperation.perform(this, checkJoinType);
        }

      } catch (final RuntimeException e) {
        throw e;
      } finally {
        atomicOperations = null;
      }
    } else {
      atomicOperations.offer(operation);
    }
  }

  public boolean isActive() {
    return STATE_ACTIVE.equals(state);
  }

  public boolean isFinished() {
    return STATE_ENDED.equals(state) || STATE_CANCELLED.equals(state);
  }

  public String getState() {
    return state;
  }

  public InternalProcessInstance getInstance() {
    return instance;
  }

  public void setInstance(final InternalProcessInstance instance) {
    this.instance = instance;
  }

  public String getIterationId() {
    return iterationId;
  }

  public void setIterationId(final String iterationId) {
    this.iterationId = iterationId;
  }

  public String getActivityInstanceId() {
    return activityInstanceId;
  }

  public void setActivityInstanceId(final String activityInstanceId) {
    this.activityInstanceId = activityInstanceId;
  }

  public int getWaitingForActivityInstanceNb() {
    return waitingForActivityInstanceNb;
  }

  public void setWaitingForActivityInstanceNb(final int waitingFor) {
    waitingForActivityInstanceNb = waitingFor;
  }

  public int getActivityInstanceNb() {
    return activityInstanceNb;
  }

  public void setActivityInstanceNb(final int activityInstanceNb) {
    this.activityInstanceNb = activityInstanceNb;
  }

  public ActivityInstanceUUID getActivityInstanceUUID() {
    if (activityInstance == null) {
      return null;
    }
    return activityInstance.getUUID();
  }

  public InternalActivityInstance getActivityInstance() {
    return activityInstance;
  }

  public void setActivityInstance(final InternalActivityInstance activityInstance) {
    this.activityInstance = activityInstance;
  }

  public void addExecution(final Execution execution) {
    execution.parent = this;
    if (executions == null) {
      executions = new ArrayList<Execution>();
    }
    executions.add(execution);
  }

  public Execution getExecution(final String name) {
    for (final Execution exec : getExecutions()) {
      if (exec.getName().equals(name)) {
        return exec;
      }
    }
    return null;
  }

  public void removeExecution(final Execution child) {
    if (executions != null) {
      if (executions.remove(child)) {
        if (state.equals(STATE_INACTIVE) && executions.isEmpty()) {
          if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("last child execution was removed; unlocking");
          }
          state = STATE_ACTIVE;
        }
        if (LOG.isLoggable(Level.FINE)) {
          LOG.fine("removed " + child + " from " + this);
        }
      } else {
        final String message = ExceptionManager.getInstance().getFullMessage("bp_EI_29", child, this);
        throw new BonitaRuntimeException(message);
      }
    }
    EnvTool.getTransaction().registerSynchronization(new Synchronization() {
      @Override
      public void beforeCompletion() {
      }

      @Override
      public void afterCompletion(final int status) {
        if (status == Status.STATUS_COMMITTED) {
          EnvTool.getCommandService().execute(new DeleteExecutionCommand(child.getId()));
        }
      }
    });
  }

  private static class DeleteExecutionCommand implements Command<Void> {
    private static final long serialVersionUID = 1L;
    private final long executionId;

    public DeleteExecutionCommand(final long executionId) {
      this.executionId = executionId;
    }

    @Override
    public Void execute(final Environment environment) throws Exception {
      EnvTool.getJournal().removeExecution(executionId);
      return null;
    }
  }

  public String getNodeName() {
    if (node == null) {
      return null;
    }
    return node.getName();
  }

  /**
   * by default this will use activity.getOutgoingTransitions to search for the outgoing transition, which includes a
   * search over the parent chain of the current node. This method allows process languages to overwrite this default
   * implementation of the transition lookup by transitionName.
   */
  protected TransitionDefinition findTransition(final String transitionName) {
    return node.getOutgoingTransition(transitionName);
  }

  // equals ///////////////////////////////////////////////////////////////////
  // hack to support comparing hibernate proxies against the real objects
  // since this always falls back to ==, we don't need to overwrite the hashcode
  @Override
  public boolean equals(final Object o) {
    return EqualsUtil.equals(this, o);
  }

  // getters and setters
  // /////////////////////////////////////////////////////////

  public Collection<Execution> getExecutions() {
    if (executions == null) {
      return Collections.emptySet();
    }
    return executions;
  }

  public String getName() {
    return name;
  }

  public Execution getParent() {
    return parent;
  }

  public Propagation getPropagation() {
    return propagation;
  }

  public void setPropagation(final Propagation propagation) {
    this.propagation = propagation;
  }

  public void setName(final String name) {
    this.name = name;
  }

  public void setState(final String state) {
    this.state = state;
  }

  public InternalActivityDefinition getNode() {
    return node;
  }

  private void setNode(final InternalActivityDefinition node) {
    this.node = node;
  }

  public long getId() {
    return id;
  }

  public InternalProcessDefinition getProcessDefinition() {
    return processDefinition;
  }

  private static TransitionDefinition getDefaultTransition(final InternalActivityDefinition activity) {
    if (activity.hasOutgoingTransitions()) {
      return activity.getOutgoingTransitions().iterator().next();
    }
    return null;
  }

  public String getEventUUID() {
    return eventUUID;
  }

  public void setEventUUID(final String eventUUID) {
    this.eventUUID = eventUUID;
  }

}
