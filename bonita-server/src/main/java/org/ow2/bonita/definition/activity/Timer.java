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
package org.ow2.bonita.definition.activity;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

import org.ow2.bonita.env.Authentication;
import org.ow2.bonita.facade.def.InternalActivityDefinition;
import org.ow2.bonita.facade.def.element.HookDefinition.Event;
import org.ow2.bonita.facade.def.majorElement.TransitionDefinition;
import org.ow2.bonita.facade.runtime.ProcessInstance;
import org.ow2.bonita.facade.uuid.ActivityInstanceUUID;
import org.ow2.bonita.runtime.event.EventConstants;
import org.ow2.bonita.runtime.event.Job;
import org.ow2.bonita.runtime.event.JobBuilder;
import org.ow2.bonita.runtime.model.Execution;
import org.ow2.bonita.services.EventService;
import org.ow2.bonita.util.BonitaConstants;
import org.ow2.bonita.util.BonitaRuntimeException;
import org.ow2.bonita.util.EnvTool;
import org.ow2.bonita.util.GroovyException;
import org.ow2.bonita.util.ProcessUtil;

/**
 * @author Marc Blachon, Guillaume Porcher, Charles Souillard, Miguel Valdes, Pierre Vigneras
 */
public class Timer extends AbstractActivity {

  private static final long serialVersionUID = 3180203041449396046L;

  protected static final Logger LOG = Logger.getLogger(Timer.class.getName());

  protected Timer() {
    super();
  }

  public Timer(String activityName) {
    super(activityName);
  }

  @Override
  protected boolean bodyStartAutomatically() {
    return false;
  }

  @Override
  protected boolean executeBusinessLogic(final Execution execution) {
    final InternalActivityDefinition node = execution.getNode();
    final Set<TransitionDefinition> incomingTransitions = node.getIncomingTransitions();
    if (incomingTransitions.isEmpty()) {
      signal(execution, EventConstants.TIMER, null);
    } else {
      final String condition = execution.getNode().getTimerCondition();
      final ActivityInstanceUUID activityUUID = execution.getActivityInstanceUUID();
      long fireTime;
      try {
    	  fireTime = ProcessUtil.getTimerDate(condition, activityUUID).getTime();
      } catch (GroovyException e) {
        final StringBuilder stb = new StringBuilder("Error while evaluating timer's condition: '");
        stb.append(condition);
        stb.append("'.");
        throw new BonitaRuntimeException(stb.toString(), e);
      }

      String executionEventUUID = execution.getEventUUID();
      if (executionEventUUID == null) {
        executionEventUUID = "timer-" + UUID.randomUUID().toString();
      }
      final ProcessInstance instance = execution.getInstance();
      final EventService eventService = EnvTool.getEventService();
      execution.setEventUUID(executionEventUUID);
      execution.lock("Timer event " + executionEventUUID);

      final Job timer = JobBuilder.intermediateTimerJob(node.getName(), instance.getRootInstanceUUID(), executionEventUUID, fireTime, instance.getProcessInstanceUUID());
      eventService.storeJob(timer);
    }
    return false;
  }

  @Override
  public void signal(final Execution execution, final String signal, final Map<String, Object> signalParameters) {
    if (EventConstants.TIMER.equals(signal)) {
      Authentication.setUserId(BonitaConstants.SYSTEM_USER);
      ConnectorExecutor.executeConnectors(execution.getNode(), execution, Event.onTimer);
      super.signal(execution, BODY_FINISHED, null);
    } else {
      super.signal(execution, signal, signalParameters);
    }
  }

}
