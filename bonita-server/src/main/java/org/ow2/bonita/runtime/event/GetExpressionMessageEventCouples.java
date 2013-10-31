/**
 * Copyright (C) 2012-2013 BonitaSoft S.A.
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
package org.ow2.bonita.runtime.event;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ow2.bonita.env.Environment;
import org.ow2.bonita.facade.uuid.ActivityInstanceUUID;
import org.ow2.bonita.services.EventService;
import org.ow2.bonita.util.Command;
import org.ow2.bonita.util.EnvTool;
import org.ow2.bonita.util.GroovyExpression;
import org.ow2.bonita.util.GroovyUtil;

/**
 * 
 * @author Matthieu Chaffotte
 * 
 */
public class GetExpressionMessageEventCouples implements Command<Set<EventCoupleId>> {

  private static final long serialVersionUID = 4329888516348355224L;

  static final Logger LOG = Logger.getLogger(GetExpressionMessageEventCouples.class.getName());

  @Override
  public Set<EventCoupleId> execute(final Environment environment) throws Exception {
    final Set<Long> usedIncomings = new HashSet<Long>();
    final Set<Long> usedOutgoings = new HashSet<Long>();
    final Set<EventCouple> validCouples = new HashSet<EventCouple>();
    final Map<IncomingEventInstance, Set<Long>> incompatibleEvents = new HashMap<IncomingEventInstance, Set<Long>>();
    final EventService eventService = EnvTool.getEventService();
    final List<EventCouple> potentialCouples = eventService.getMessageEventCouples();
    for (final EventCouple eventCouple : potentialCouples) {
      if (!usedIncomings.contains(eventCouple.getIncoming().getId())
          && !usedOutgoings.contains(eventCouple.getOutgoing().getId())) {
        // matcher
        boolean match = true;
        final IncomingEventInstance incoming = eventCouple.getIncoming();
        final OutgoingEventInstance outgoing = eventCouple.getOutgoing();
        final String expression = incoming.getExpression();
        final Map<String, Object> parameters = outgoing.getParameters();
        final String signal = incoming.getSignal();
        if (expression != null && !"event.start.timer".equals(signal)) {
          final String groovyExpression = GroovyExpression.START_DELIMITER + expression
              + GroovyExpression.END_DELIMITER;
          final ActivityInstanceUUID activityUUID = incoming.getActivityUUID();
          if (activityUUID != null) {
            match = (Boolean) GroovyUtil.evaluate(groovyExpression, parameters, activityUUID, false, false);
          } else {
            match = (Boolean) GroovyUtil.evaluate(groovyExpression, parameters);
          }
        }
        if (match) {
          usedIncomings.add(incoming.getId());
          usedOutgoings.add(outgoing.getId());
          validCouples.add(eventCouple);
          if (LOG.isLoggable(Level.INFO)) {
            LOG.info("Adding eventCouple:[incoming=" + incoming.getId() + " " + incoming.getSignal() + ", outgoing="
                + outgoing.getId() + "] to the queue");
          }
        } else {
          if (!incompatibleEvents.containsKey(incoming)) {
            incompatibleEvents.put(incoming, new HashSet<Long>());
          }
          incompatibleEvents.get(incoming).add(outgoing.getId());
        }
      }
    }

    // FOR ALL INCOMINGS THAT ARE NOT GOING TO BE CONSUMED, FULLFILL the
    // INCOMPATIBLE EVENTS LIST
    for (final Map.Entry<IncomingEventInstance, Set<Long>> incomingIncompatibleEvents : incompatibleEvents.entrySet()) {
      if (!usedIncomings.contains(incomingIncompatibleEvents.getKey())) {
        for (final long incompatibleOutgoingId : incomingIncompatibleEvents.getValue()) {
          if (!usedOutgoings.contains(incompatibleOutgoingId)) {
            incomingIncompatibleEvents.getKey().addIncompatibleEvent(incompatibleOutgoingId);
          }
        }
      }
    }

    if (validCouples.isEmpty()) {
      return Collections.emptySet();
    }
    final Set<EventCoupleId> result = new HashSet<EventCoupleId>();
    for (final EventCouple validCouple : validCouples) {
      final IncomingEventInstance incoming = validCouple.getIncoming();
      final OutgoingEventInstance outgoing = validCouple.getOutgoing();
      incoming.setLocked(true);
      outgoing.setLocked(true);
      result.add(new EventCoupleId(incoming.getId(), outgoing.getId()));
    }

    if (LOG.isLoggable(Level.INFO)) {
      LOG.info("Expression message event: New event couples to execute: " + result.toString());
    }
    return result;
  }

}
