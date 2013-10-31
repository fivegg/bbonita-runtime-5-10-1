/**
 * Copyright (C) 2012-2013 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ow2.bonita.env.Environment;
import org.ow2.bonita.services.EventService;
import org.ow2.bonita.util.Command;
import org.ow2.bonita.util.EnvTool;

/**
 * 
 * @author Matthieu Chaffotte
 * 
 */
public class GetEventCouples implements Command<Set<EventCoupleId>> {

  private static final long serialVersionUID = -3820361512043859834L;

  static final Logger LOG = Logger.getLogger(GetEventCouples.class.getName());

  private final int maxCouples;

  public GetEventCouples(final int maxCouples) {
    this.maxCouples = maxCouples;
  }

  @Override
  public Set<EventCoupleId> execute(final Environment environment) throws Exception {
    final Set<Long> usedIncomings = new HashSet<Long>();
    final Set<Long> usedOutgoings = new HashSet<Long>();
    final EventService eventService = EnvTool.getEventService();
    final List<EventCouple> couples = eventService.getCorrelationKeyMessageEventCouples(maxCouples);
    if (couples.isEmpty()) {
      return Collections.emptySet();
    }
    final Set<EventCoupleId> result = new HashSet<EventCoupleId>();
    for (final EventCouple couple : couples) {
      final IncomingEventInstance incoming = couple.getIncoming();
      final OutgoingEventInstance outgoing = couple.getOutgoing();
      final long incomingId = incoming.getId();
      final long outgoingId = outgoing.getId();
      if (!usedIncomings.contains(incomingId) && !usedOutgoings.contains(outgoingId)) {
        usedIncomings.add(incomingId);
        usedOutgoings.add(outgoingId);
        final EventCoupleId eventCoupleId = EventCoupleHandling.createEventCouple(incoming, outgoing);
        result.add(eventCoupleId);
      }
    }
    if (LOG.isLoggable(Level.INFO)) {
      LOG.info("Correlation event matcher: New event couples to execute: " + result.toString());
    }
    return result;
  }

}
