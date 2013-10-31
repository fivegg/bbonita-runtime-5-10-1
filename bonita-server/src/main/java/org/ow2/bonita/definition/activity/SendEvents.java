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
 **/
package org.ow2.bonita.definition.activity;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.ow2.bonita.facade.def.element.OutgoingEventDefinition;
import org.ow2.bonita.facade.def.majorElement.ActivityDefinition;
import org.ow2.bonita.facade.uuid.ActivityInstanceUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.runtime.event.EventCoupleId;
import org.ow2.bonita.runtime.event.EventCoupleHandling;
import org.ow2.bonita.runtime.event.IncomingEventInstance;
import org.ow2.bonita.runtime.event.OutgoingEventInstance;
import org.ow2.bonita.runtime.model.Execution;
import org.ow2.bonita.services.EventService;
import org.ow2.bonita.util.BonitaRuntimeException;
import org.ow2.bonita.util.EnvTool;
import org.ow2.bonita.util.GroovyException;
import org.ow2.bonita.util.GroovyUtil;
import org.ow2.bonita.util.Misc;

/**
 * @author Marc Blachon, Guillaume Porcher, Charles Souillard, Miguel Valdes, Pierre Vigneras
 */
public class SendEvents extends AbstractActivity {

  private static final long serialVersionUID = 477565487347215726L;

  protected static final Logger LOG = Logger.getLogger(SendEvents.class.getName());

  protected SendEvents() {
    super();
  }

  public SendEvents(final String activityName) {
    super(activityName);
  }

  @Override
  protected boolean bodyStartAutomatically() {
    return true;
  }

  @Override
  protected boolean executeBusinessLogic(final Execution execution) {
    final ActivityDefinition activity = execution.getNode();
    final ProcessInstanceUUID instanceUUID = execution.getInstance().getUUID();
    final ActivityInstanceUUID activityUUID = execution.getActivityInstanceUUID();
    final EventService eventService = EnvTool.getEventService();

    final Set<String> processNames = new HashSet<String>();
    boolean canContinue = true;
    for (final OutgoingEventDefinition outgoingEvent : activity.getOutgoingEvents()) {
      final Map<String, Object> parameters = outgoingEvent.getParameters();
      processNames.add(outgoingEvent.getToProcessName());

      Map<String, Object> evaluatedParameters = null;
      if (parameters != null) {
        evaluatedParameters = new HashMap<String, Object>();
        for (final Map.Entry<String, Object> parameter : parameters.entrySet()) {
          Object newValue = parameter.getValue();
          if (newValue instanceof String && Misc.isJustAGroovyExpression((String) newValue)) {
            try {
              newValue = GroovyUtil.evaluate((String) newValue, null, activityUUID, false, false);
            } catch (final GroovyException e) {
              throw new BonitaRuntimeException(e);
            }
          }
          evaluatedParameters.put(parameter.getKey(), newValue);
        }
      }
      long overdue = -1;
      if (outgoingEvent.getTimeToLive() >= 0) {
        overdue = System.currentTimeMillis() + outgoingEvent.getTimeToLive();
      }
      final OutgoingEventInstance eventInstance = new OutgoingEventInstance(outgoingEvent.getName(),
          outgoingEvent.getToProcessName(), outgoingEvent.getToActivityName(), evaluatedParameters, instanceUUID,
          activityUUID, overdue);
      ActivityUtil.addCorrelationKeys(outgoingEvent, eventInstance, activityUUID);
      eventService.fire(eventInstance);
      final List<IncomingEventInstance> messageSubProcessesIncomingEvents = eventService.getMessageEventSubProcessIncomingEvents(instanceUUID, eventInstance.getId());
      if (!messageSubProcessesIncomingEvents.isEmpty()) {
        //as the root process instance is passed as parameter at most one incoming event will be retrieved
        final EventCoupleId eventCoupleId = EventCoupleHandling.createEventCouple(messageSubProcessesIncomingEvents.get(0), eventInstance);
        EventCoupleHandling.createJob(eventService, eventCoupleId);
        canContinue = false;
      }
      
    }
    return canContinue;
  }

}
