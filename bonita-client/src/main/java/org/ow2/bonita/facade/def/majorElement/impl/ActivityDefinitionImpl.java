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
 * Modified by Matthieu Chaffotte - BonitaSoft S.A.
 **/
package org.ow2.bonita.facade.def.majorElement.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ow2.bonita.facade.def.element.BoundaryEvent;
import org.ow2.bonita.facade.def.element.ConnectorDefinition;
import org.ow2.bonita.facade.def.element.DeadlineDefinition;
import org.ow2.bonita.facade.def.element.FilterDefinition;
import org.ow2.bonita.facade.def.element.HookDefinition;
import org.ow2.bonita.facade.def.element.IncomingEventDefinition;
import org.ow2.bonita.facade.def.element.MultiInstantiationDefinition;
import org.ow2.bonita.facade.def.element.OutgoingEventDefinition;
import org.ow2.bonita.facade.def.element.SubflowParameterDefinition;
import org.ow2.bonita.facade.def.element.impl.BoundaryEventImpl;
import org.ow2.bonita.facade.def.element.impl.ConnectorDefinitionImpl;
import org.ow2.bonita.facade.def.element.impl.ErrorBoundaryEventImpl;
import org.ow2.bonita.facade.def.element.impl.IncomingEventDefinitionImpl;
import org.ow2.bonita.facade.def.element.impl.MessageBoundaryEventImpl;
import org.ow2.bonita.facade.def.element.impl.OutgoingEventDefinitionImpl;
import org.ow2.bonita.facade.def.element.impl.SignalBoundaryEventImpl;
import org.ow2.bonita.facade.def.element.impl.SubflowParameterDefinitionImpl;
import org.ow2.bonita.facade.def.element.impl.TimerBoundaryEventImpl;
import org.ow2.bonita.facade.def.majorElement.ActivityDefinition;
import org.ow2.bonita.facade.def.majorElement.DataFieldDefinition;
import org.ow2.bonita.facade.def.majorElement.TransitionDefinition;
import org.ow2.bonita.facade.uuid.ActivityDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;

public class ActivityDefinitionImpl extends ProcessElementImpl implements ActivityDefinition {

  private static final long serialVersionUID = 1545928041850807545L;

  protected ActivityDefinitionUUID uuid;
  protected Set<DeadlineDefinition> deadlines;
  protected Set<String> performers;
  protected JoinType joinType;
  protected SplitType splitType;
  protected List<HookDefinition> connectors;
  protected FilterDefinition filter;
  protected Set<DataFieldDefinition> dataFields;
  protected Set<TransitionDefinition> outgoingTransitions;
  protected Set<TransitionDefinition> incomingTransitions;
  protected Map<String, BoundaryEvent> boundaryEvents;

  protected Set<SubflowParameterDefinition> subflowInParameters;
  protected Set<SubflowParameterDefinition> subflowOutParameters;
  protected String subflowProcessName;
  protected String subflowProcessVersion;

  @Deprecated
  protected MultiInstantiationDefinition activityInstantiator;
  protected MultiInstantiationDefinition instantiator;
  protected MultiInstantiationDefinition joinChecker;

  protected boolean asynchronous;
  protected long executingTime;
  protected int priority;
  protected boolean inCycle;
  protected String timerCondition;
  protected IncomingEventDefinition incomingEvent;
  protected Set<OutgoingEventDefinition> outgoingEvents;
  protected Type type;

  protected boolean loop;
  protected String loopCondition;
  protected String loopMaximum;
  protected boolean beforeExecution;
  protected String dynamicLabel;
  protected String dynamicDescription;
  protected String executionSummary;

  protected boolean catchEvent;
  protected boolean terminateProcess;

  protected ActivityDefinitionImpl() {
    super();
  }

  public static ActivityDefinitionImpl createAutomaticActivity(final ProcessDefinitionUUID processUUID,
      final String name) {
    return new ActivityDefinitionImpl(processUUID, name, Type.Automatic, null, null, null, null, null, null);
  }

  public static ActivityDefinitionImpl createSubflowActivity(final ProcessDefinitionUUID processUUID,
      final String name, final String subflowProcessName, final String subflowProcessVersion) {
    return new ActivityDefinitionImpl(processUUID, name, Type.Subflow, null, subflowProcessName, subflowProcessVersion,
        null, null, null);
  }

  public static ActivityDefinitionImpl createTimerActivity(final ProcessDefinitionUUID processUUID, final String name,
      final String timerCondition) {
    return new ActivityDefinitionImpl(processUUID, name, Type.Timer, null, null, null, timerCondition, null, null);
  }

  public static ActivityDefinitionImpl createHumanActivity(final ProcessDefinitionUUID processUUID, final String name,
      final Set<String> performers) {
    return new ActivityDefinitionImpl(processUUID, name, Type.Human, performers, null, null, null, null, null);
  }

  public static ActivityDefinitionImpl createSendEventActivity(final ProcessDefinitionUUID processUUID,
      final String name) {
    return new ActivityDefinitionImpl(processUUID, name, Type.SendEvents, null, null, null, null, null, null);
  }

  public static ActivityDefinitionImpl createReceiveEventActivity(final ProcessDefinitionUUID processUUID,
      final String name, final String eventName, final String expression) {
    return new ActivityDefinitionImpl(processUUID, name, Type.ReceiveEvent, null, null, null, null, eventName,
        expression);
  }

  public static ActivityDefinitionImpl createErrorEventActivity(final ProcessDefinitionUUID processUUID,
      final String eventName, final String errorCode) {
    return new ActivityDefinitionImpl(processUUID, eventName, Type.ErrorEvent, null, null, null, errorCode, null, null);
  }

  public static ActivityDefinitionImpl createSignalEventActivity(final ProcessDefinitionUUID processUUID,
      final String eventName, final String signalCode) {
    return new ActivityDefinitionImpl(processUUID, eventName, Type.SignalEvent, null, null, null, signalCode, null,
        null);
  }

  private ActivityDefinitionImpl(final ProcessDefinitionUUID processUUID, final String name, final Type type,
      final Set<String> performers, final String subflowProcessName, final String subFlowProcessVersion,
      final String timerCondition, final String eventName, final String receiveEventExpression) {
    super(name, processUUID);
    uuid = new ActivityDefinitionUUID(processUUID, name);
    this.performers = performers;
    this.subflowProcessName = subflowProcessName;
    subflowProcessVersion = subFlowProcessVersion;
    this.timerCondition = timerCondition;

    joinType = JoinType.XOR;
    splitType = SplitType.AND;
    dataFields = new HashSet<DataFieldDefinition>();
    asynchronous = false;
    loop = false;

    this.type = type;
    if (eventName != null) {
      incomingEvent = new IncomingEventDefinitionImpl(eventName, receiveEventExpression);
    }
  }

  public ActivityDefinitionImpl(final ActivityDefinition src) {
    super(src);
    uuid = new ActivityDefinitionUUID(src.getUUID());
    executingTime = src.getExecutingTime();
    priority = src.getPriority();
    timerCondition = src.getTimerCondition();

    final Set<DeadlineDefinition> deadlines = src.getDeadlines();
    if (deadlines != null) {
      this.deadlines = new HashSet<DeadlineDefinition>();
      for (final DeadlineDefinition d : deadlines) {
        this.deadlines.add(new ConnectorDefinitionImpl(d));
      }
    }
    final Set<String> performers = src.getPerformers();
    this.performers = new HashSet<String>();
    for (final String performer : performers) {
      this.performers.add(performer);
    }
    joinType = src.getJoinType();
    splitType = src.getSplitType();
    final List<HookDefinition> hooks = src.getConnectors();
    if (hooks != null) {
      connectors = new ArrayList<HookDefinition>();
      for (final HookDefinition d : hooks) {
        connectors.add(new ConnectorDefinitionImpl(d));
      }
    }
    if (src.getFilter() != null) {
      filter = new ConnectorDefinitionImpl(src.getFilter());
    }
    final Set<DataFieldDefinition> dataFields = src.getDataFields();
    if (dataFields != null) {
      this.dataFields = new HashSet<DataFieldDefinition>();
      for (final DataFieldDefinition d : dataFields) {
        this.dataFields.add(new DataFieldDefinitionImpl(d));
      }
    }
    final Set<TransitionDefinition> ogtransitions = src.getOutgoingTransitions();
    if (ogtransitions != null) {
      outgoingTransitions = new HashSet<TransitionDefinition>();
      for (final TransitionDefinition d : ogtransitions) {
        outgoingTransitions.add(new TransitionDefinitionImpl(d));
      }
    }
    final Set<TransitionDefinition> ictransitions = src.getIncomingTransitions();
    if (ictransitions != null) {
      incomingTransitions = new HashSet<TransitionDefinition>();
      for (final TransitionDefinition d : ictransitions) {
        incomingTransitions.add(new TransitionDefinitionImpl(d));
      }
    }
    final List<BoundaryEvent> boundaryevents = src.getBoundaryEvents();
    if (boundaryevents != null) {
      boundaryEvents = new HashMap<String, BoundaryEvent>();
      for (final BoundaryEvent boundaryEvent : boundaryevents) {
        BoundaryEvent event = null;
        if (boundaryEvent instanceof TimerBoundaryEventImpl) {
          final TimerBoundaryEventImpl timer = (TimerBoundaryEventImpl) boundaryEvent;
          event = new TimerBoundaryEventImpl(timer);
        } else if (boundaryEvent instanceof MessageBoundaryEventImpl) {
          final MessageBoundaryEventImpl message = (MessageBoundaryEventImpl) boundaryEvent;
          event = new MessageBoundaryEventImpl(message);
        } else if (boundaryEvent instanceof ErrorBoundaryEventImpl) {
          final ErrorBoundaryEventImpl error = (ErrorBoundaryEventImpl) boundaryEvent;
          event = new ErrorBoundaryEventImpl(error);
        } else if (boundaryEvent instanceof SignalBoundaryEventImpl) {
          final SignalBoundaryEventImpl signal = (SignalBoundaryEventImpl) boundaryEvent;
          event = new SignalBoundaryEventImpl(signal);
        }
        boundaryEvents.put(boundaryEvent.getName(), event);
      }
    }

    if (src.getIncomingEvent() != null) {
      incomingEvent = new IncomingEventDefinitionImpl(src.getIncomingEvent());
    }
    final Set<OutgoingEventDefinition> outgoingEvents = src.getOutgoingEvents();
    if (outgoingEvents != null) {
      this.outgoingEvents = new HashSet<OutgoingEventDefinition>();
      for (final OutgoingEventDefinition eventDefinition : outgoingEvents) {
        this.outgoingEvents.add(new OutgoingEventDefinitionImpl(eventDefinition));
      }
    }

    if (src.getMultiInstantiationDefinition() != null) {
      activityInstantiator = new ConnectorDefinitionImpl(src.getMultiInstantiationDefinition());
    }

    if (src.getMultipleInstancesInstantiator() != null) {
      instantiator = new ConnectorDefinitionImpl(src.getMultipleInstancesInstantiator());
    }
    if (src.getMultipleInstancesJoinChecker() != null) {
      joinChecker = new ConnectorDefinitionImpl(src.getMultipleInstancesJoinChecker());
    }

    asynchronous = src.isAsynchronous();
    final Set<SubflowParameterDefinition> subFlowInParams = src.getSubflowInParameters();
    if (subFlowInParams != null) {
      subflowInParameters = new HashSet<SubflowParameterDefinition>();
      for (final SubflowParameterDefinition p : subFlowInParams) {
        subflowInParameters.add(new SubflowParameterDefinitionImpl(p));
      }
    }

    final Set<SubflowParameterDefinition> subFlowOutParams = src.getSubflowOutParameters();
    if (subFlowOutParams != null) {
      subflowOutParameters = new HashSet<SubflowParameterDefinition>();
      for (final SubflowParameterDefinition p : subFlowOutParams) {
        subflowOutParameters.add(new SubflowParameterDefinitionImpl(p));
      }
    }
    subflowProcessName = src.getSubflowProcessName();
    subflowProcessVersion = src.getSubflowProcessVersion();
    inCycle = src.isInCycle();
    type = src.getType();

    loop = src.isInALoop();
    loopCondition = src.getLoopCondition();
    beforeExecution = src.evaluateLoopConditionBeforeExecution();
    loopMaximum = src.getLoopMaximum();

    dynamicDescription = src.getDynamicDescription();
    dynamicLabel = src.getDynamicLabel();
    executionSummary = src.getDynamicExecutionSummary();
    catchEvent = src.catchEvent();
    terminateProcess = src.isTerminateProcess();
  }

  @Override
  public String toString() {
    return getUUID().toString();
  }

  @Override
  public String getDynamicDescription() {
    return dynamicDescription;
  }

  @Override
  public String getDynamicLabel() {
    return dynamicLabel;
  }

  @Override
  public String getDynamicExecutionSummary() {
    return executionSummary;
  }

  @Override
  public Set<DeadlineDefinition> getDeadlines() {
    if (deadlines == null) {
      return Collections.emptySet();
    }
    return deadlines;
  }

  @Override
  public Set<TransitionDefinition> getOutgoingTransitions() {
    if (outgoingTransitions == null) {
      return Collections.emptySet();
    }
    return outgoingTransitions;
  }

  @Override
  public Set<TransitionDefinition> getIncomingTransitions() {
    if (incomingTransitions == null) {
      return Collections.emptySet();
    }
    return incomingTransitions;
  }

  @Override
  public TransitionDefinition getOutgoingTransition(final String transitionName) {
    for (final TransitionDefinition transition : getOutgoingTransitions()) {
      if (transition.getName().equals(transitionName)) {
        return transition;
      }
    }
    return null;
  }

  @Override
  public TransitionDefinition getIncomingTransitions(final String transitionName) {
    for (final TransitionDefinition transition : getIncomingTransitions()) {
      if (transition.getName().equals(transitionName)) {
        return transition;
      }
    }
    return null;
  }

  @Override
  public Set<String> getPerformers() {
    if (performers == null) {
      return Collections.emptySet();
    }
    return performers;
  }

  @Override
  public String getSubflowProcessName() {
    return subflowProcessName;
  }

  @Override
  public String getSubflowProcessVersion() {
    return subflowProcessVersion;
  }

  @Override
  public List<HookDefinition> getConnectors() {
    if (connectors == null) {
      return Collections.emptyList();
    }
    return connectors;
  }

  @Override
  public FilterDefinition getFilter() {
    return filter;
  }

  @Override
  public Set<DataFieldDefinition> getDataFields() {
    if (dataFields == null) {
      return Collections.emptySet();
    }
    return dataFields;
  }

  @Override
  public ActivityDefinitionUUID getUUID() {
    return uuid;
  }

  @Override
  @Deprecated
  public MultiInstantiationDefinition getMultiInstantiationDefinition() {
    return activityInstantiator;
  }

  @Override
  public MultiInstantiationDefinition getMultipleInstancesInstantiator() {
    return instantiator;
  }

  @Override
  public MultiInstantiationDefinition getMultipleInstancesJoinChecker() {
    return joinChecker;
  }

  @Override
  public boolean isAsynchronous() {
    return asynchronous;
  }

  @Override
  public JoinType getJoinType() {
    return joinType;
  }

  @Override
  public SplitType getSplitType() {
    return splitType;
  }

  @Override
  public boolean isAutomatic() {
    return Type.Automatic.equals(getType());
  }

  @Override
  public boolean isTask() {
    return Type.Human.equals(getType());
  }

  @Override
  public Type getType() {
    return type;
  }

  public void addOutgoingTransition(final TransitionDefinition transition) {
    if (outgoingTransitions == null) {
      outgoingTransitions = new HashSet<TransitionDefinition>();
    }
    outgoingTransitions.add(transition);
  }

  @Override
  public boolean hasIncomingTransitions() {
    return getIncomingTransitions().size() > 0;
  }

  @Override
  public boolean hasOutgoingTransitions() {
    return getOutgoingTransitions().size() > 0;
  }

  public void addIncomingTransition(final TransitionDefinition transition) {
    if (incomingTransitions == null) {
      incomingTransitions = new HashSet<TransitionDefinition>();
    }
    incomingTransitions.add(transition);
  }

  public void addData(final DataFieldDefinition data) {
    if (dataFields == null) {
      dataFields = new HashSet<DataFieldDefinition>();
    }
    dataFields.add(data);
  }

  public void setFilter(final FilterDefinition filter) {
    this.filter = filter;
  }

  @Deprecated
  public void setMultiInstanciation(final MultiInstantiationDefinition multiInstanciation) {
    activityInstantiator = multiInstanciation;
  }

  public void setMultipleInstancesInstantiator(final MultiInstantiationDefinition instantiator) {
    this.instantiator = instantiator;
  }

  public void setMultipleInstancesJoinChecker(final MultiInstantiationDefinition joinChecker) {
    this.joinChecker = joinChecker;
  }

  public void addDeadline(final DeadlineDefinition deadline) {
    if (deadlines == null) {
      deadlines = new HashSet<DeadlineDefinition>();
    }
    deadlines.add(deadline);
  }

  public void addConnector(final HookDefinition connector) {
    if (connectors == null) {
      connectors = new ArrayList<HookDefinition>();
    }
    connectors.add(connector);
  }

  public void setJoinType(final JoinType join) {
    joinType = join;
  }

  public void setSplitType(final SplitType split) {
    splitType = split;
  }

  public void setAsynchronous(final boolean asynchronous) {
    this.asynchronous = asynchronous;
  }

  @Override
  public Set<SubflowParameterDefinition> getSubflowInParameters() {
    if (subflowInParameters == null) {
      return Collections.emptySet();
    }
    return subflowInParameters;
  }

  @Override
  public Set<SubflowParameterDefinition> getSubflowOutParameters() {
    if (subflowOutParameters == null) {
      return Collections.emptySet();
    }
    return subflowOutParameters;
  }

  @Override
  public boolean isSubflow() {
    return Type.Subflow.equals(getType());
  }

  public void addSubflowOutParameter(final SubflowParameterDefinition param) {
    if (subflowOutParameters == null) {
      subflowOutParameters = new HashSet<SubflowParameterDefinition>();
    }
    subflowOutParameters.add(param);
  }

  public void addSubflowInParameter(final SubflowParameterDefinition param) {
    if (subflowInParameters == null) {
      subflowInParameters = new HashSet<SubflowParameterDefinition>();
    }
    subflowInParameters.add(param);
  }

  public void setExecutingTime(final long executingTime) {
    this.executingTime = executingTime;
  }

  @Override
  public long getExecutingTime() {
    return executingTime;
  }

  public void setPriority(final int priority) {
    this.priority = priority;
  }

  @Override
  public int getPriority() {
    return priority;
  }

  @Override
  public Set<String> getClassDependencies() {
    final Set<String> classDependencies = new HashSet<String>();
    for (final DeadlineDefinition deadline : getDeadlines()) {
      classDependencies.add(deadline.getClassName());
    }
    for (final ConnectorDefinition connector : getConnectors()) {
      classDependencies.add(connector.getClassName());
    }
    if (getMultiInstantiationDefinition() != null) {
      classDependencies.add(getMultiInstantiationDefinition().getClassName());
    }
    if (getFilter() != null) {
      classDependencies.add(getFilter().getClassName());
    }
    return classDependencies;
  }

  public void setInCycle(final boolean inCycle) {
    this.inCycle = inCycle;
  }

  @Override
  public boolean isInCycle() {
    return inCycle;
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof ActivityDefinition)) {
      return false;
    }
    final ActivityDefinition other = (ActivityDefinition) obj;
    if (other.getUUID() == null) {
      return uuid == null;
    }
    return other.getUUID().equals(uuid);
  }

  @Override
  public IncomingEventDefinition getIncomingEvent() {
    return incomingEvent;
  }

  @Override
  public String getTimerCondition() {
    return timerCondition;
  }

  @Override
  public boolean isTimer() {
    return Type.Timer.equals(getType());
  }

  @Override
  public boolean isSendEvents() {
    return Type.SendEvents.equals(getType());
  }

  @Override
  public boolean isReceiveEvent() {
    return Type.ReceiveEvent.equals(getType());
  }

  @Override
  public boolean isThrowingErrorEvent() {
    return Type.ErrorEvent.equals(getType()) && getOutgoingTransitions().isEmpty();
  }

  @Override
  public boolean isSignalEvent() {
    return Type.SignalEvent.equals(getType());
  }

  @Override
  public boolean isThrowingSignalEvent() {
    return isSignalEvent() && !catchEvent;
  }

  @Override
  public boolean isCatchingSignalEvent() {
    return isSignalEvent() && catchEvent;
  }

  @Override
  public boolean isCatchingErrorEvent() {
    return Type.ErrorEvent.equals(getType()) && this.getIncomingTransitions().isEmpty();
  }

  @Override
  public Set<OutgoingEventDefinition> getOutgoingEvents() {
    if (outgoingEvents == null) {
      return Collections.emptySet();
    }
    return outgoingEvents;
  }

  public void addOutgoingEvent(final OutgoingEventDefinition oged) {
    if (outgoingEvents == null) {
      outgoingEvents = new HashSet<OutgoingEventDefinition>();
    }
    outgoingEvents.add(oged);
  }

  @Override
  public boolean evaluateLoopConditionBeforeExecution() {
    return beforeExecution;
  }

  @Override
  public String getLoopCondition() {
    return loopCondition;
  }

  @Override
  public String getLoopMaximum() {
    return loopMaximum;
  }

  @Override
  public boolean isInALoop() {
    return loop;
  }

  public void setLoop(final String condition, final boolean beforeExecution, final String loopMaximum) {
    loop = true;
    loopCondition = condition;
    this.beforeExecution = beforeExecution;
    this.loopMaximum = loopMaximum;
  }

  public void setDynamicDescription(final String dynamicDescription) {
    this.dynamicDescription = dynamicDescription;
  }

  public void setDynamicLabel(final String dynamicLabel) {
    this.dynamicLabel = dynamicLabel;
  }

  public void setDynamicExecutionSummary(final String expression) {
    executionSummary = expression;
  }

  public void addBoundaryEvent(final BoundaryEvent event) {
    if (boundaryEvents == null) {
      boundaryEvents = new HashMap<String, BoundaryEvent>();
    }
    boundaryEvents.put(event.getName(), event);
  }

  public void addExceptionTransition(final String eventName, final TransitionDefinition transition) {
    if (boundaryEvents != null) {
      final BoundaryEventImpl event = (BoundaryEventImpl) boundaryEvents.get(eventName);
      if (event != null) {
        event.setExceptionTransition(transition);
        boundaryEvents.put(eventName, event);
      }
    }
  }

  @Override
  public BoundaryEvent getBoundaryEvent(final String eventName) {
    if (boundaryEvents == null) {
      return null;
    }
    return boundaryEvents.get(eventName);
  }

  @Override
  public List<BoundaryEvent> getBoundaryEvents() {
    final List<BoundaryEvent> events = new ArrayList<BoundaryEvent>();
    if (boundaryEvents != null) {
      events.addAll(boundaryEvents.values());
    }
    return events;
  }

  @Override
  public boolean hasBoundaryEvents() {
    return getBoundaryEvents().size() > 0;
  }

  @Override
  public boolean catchEvent() {
    return catchEvent;
  }

  public void setCatchEvent(final boolean catchEvent) {
    this.catchEvent = catchEvent;
  }

  @Override
  public boolean isTerminateProcess() {
    return terminateProcess;
  }

  public void setTerminateProcess(final boolean terminateProcess) {
    this.terminateProcess = terminateProcess;
  }

}
