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

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.ow2.bonita.facade.def.InternalActivityDefinition;
import org.ow2.bonita.facade.def.majorElement.TransitionDefinition;
import org.ow2.bonita.facade.runtime.impl.InternalProcessInstance;
import org.ow2.bonita.runtime.event.EventConstants;
import org.ow2.bonita.runtime.event.IncomingEventInstance;
import org.ow2.bonita.runtime.model.Execution;
import org.ow2.bonita.services.EventService;
import org.ow2.bonita.util.EnvTool;

/**
 * @author Matthieu Chaffotte
 *
 */
public class CatchingSignalEvent extends AbstractActivity {

  private static final long serialVersionUID = 1163820971844205953L;

  protected CatchingSignalEvent() {
    super();
  }

  public CatchingSignalEvent(String eventName) {
    super(eventName);
  }

  @Override
  protected boolean bodyStartAutomatically() {
    return true;
  }

  @Override
  protected boolean executeBusinessLogic(Execution execution) {
    final InternalActivityDefinition node = execution.getNode();
    final Set<TransitionDefinition> outgoingTransitions = node.getOutgoingTransitions();
    final Set<TransitionDefinition> incomingTransitions = node.getIncomingTransitions();
    boolean finishTask = true;
    if (!outgoingTransitions.isEmpty() && !incomingTransitions.isEmpty()) {
        catchEvent(execution);
        finishTask = false;
    }
    return finishTask;
  }

  private void catchEvent(final Execution execution) {
    final InternalActivityDefinition activity = execution.getNode();
    final InternalProcessInstance instance = execution.getInstance();
    final String processName = execution.getProcessDefinition().getName();
    final String eventName = activity.getTimerCondition();
    String executionEventUUID = execution.getEventUUID();
    if (executionEventUUID == null) {
      executionEventUUID = "signal-" + UUID.randomUUID().toString();
    }
    IncomingEventInstance signalEventInstance = new IncomingEventInstance(
        eventName, null, instance.getUUID(), activity.getUUID(), null, processName,
        activity.getName(), executionEventUUID, EventConstants.SIGNAL_INTERMEDIATE_EVENT, -1, true);
    execution.setEventUUID(executionEventUUID);
    final EventService eventService = EnvTool.getEventService();
    eventService.subscribe(signalEventInstance);
    execution.lock("Intermediate signal event " + executionEventUUID);
  }

  @Override
  public void signal(final Execution execution, final String signal, final Map<String, Object> signalParameters) {
    if (EventConstants.SIGNAL.equals(signal)) {
      super.signal(execution, BODY_FINISHED, null);
    } else {
      super.signal(execution, signal, signalParameters);
    }
  }

}
