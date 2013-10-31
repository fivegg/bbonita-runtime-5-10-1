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
import java.util.UUID;
import java.util.logging.Logger;

import org.ow2.bonita.env.Authentication;
import org.ow2.bonita.facade.def.element.HookDefinition.Event;
import org.ow2.bonita.facade.def.element.IncomingEventDefinition;
import org.ow2.bonita.facade.def.majorElement.ActivityDefinition;
import org.ow2.bonita.facade.uuid.ActivityInstanceUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.runtime.event.EventConstants;
import org.ow2.bonita.runtime.event.IncomingEventInstance;
import org.ow2.bonita.runtime.model.Execution;
import org.ow2.bonita.services.EventService;
import org.ow2.bonita.util.BonitaConstants;
import org.ow2.bonita.util.EnvTool;

/**
 * @author Marc Blachon, Guillaume Porcher, Charles Souillard, Miguel Valdes,
 *         Pierre Vigneras
 */
public class ReceiveEvent extends AbstractActivity {

  private static final long serialVersionUID = 477565487347215726L;

  protected static final Logger LOG = Logger.getLogger(ReceiveEvent.class.getName());

  protected ReceiveEvent() {
    super();
  }

  public ReceiveEvent(final String activityName) {
    super(activityName);
  }

  @Override
  protected boolean bodyStartAutomatically() {
    return true;
  }

  @Override
  protected boolean executeBusinessLogic(final Execution execution) {
    final ActivityDefinition activity = execution.getNode();
    final ActivityInstanceUUID activityUUID = execution.getActivityInstanceUUID();
    final EventService eventService = EnvTool.getEventService();
    final IncomingEventDefinition incomingEvent = activity.getIncomingEvent();
    String eventUUID = execution.getEventUUID();
    final ProcessInstanceUUID instanceUUID = execution.getInstance().getUUID();
    if (eventUUID == null) {
      eventUUID = UUID.randomUUID().toString();
    }
    
    if (execution.getNode().hasIncomingTransitions()) {
    	final IncomingEventInstance eventInstance = new IncomingEventInstance(incomingEvent.getName(),
        incomingEvent.getExpression(), instanceUUID, activity.getUUID(), activityUUID, execution.getProcessDefinition()
            .getName(), activityName, eventUUID, EventConstants.INTERMEDIATE, System.currentTimeMillis(), true);
    	ActivityUtil.addCorrelationKeys(incomingEvent, eventInstance, activityUUID);
    	eventService.subscribe(eventInstance);
    } else {
    	eventUUID = instanceUUID + "-----START-----";
    }
    execution.setEventUUID(eventUUID);
    execution.lock("Incoming event " + eventUUID);
    
    return false;
  }

  @Override
  public void signal(final Execution execution, final String signal, final Map<String, Object> signalParameters) {
    if (EventConstants.MESSAGE.equals(signal)) {
      Authentication.setUserId(BonitaConstants.SYSTEM_USER);
      ConnectorExecutor.executeConnectors(execution.getNode(), execution, Event.onEvent, signalParameters);
      super.signal(execution, BODY_FINISHED, null);
    } else {
      super.signal(execution, signal, signalParameters);
    }
  }

}
