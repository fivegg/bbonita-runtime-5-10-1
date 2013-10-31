/**
 * Copyright (C) 2013  BonitaSoft S.A.
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

import java.util.logging.Level;
import java.util.logging.Logger;

import org.ow2.bonita.facade.runtime.impl.InternalProcessInstance;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.services.EventService;
import org.ow2.bonita.util.EnvTool;


public class EventCoupleHandling {
  
  private static final Logger LOG = Logger.getLogger(EventCoupleHandling.class.getName());

  public static EventCoupleId createEventCouple(final IncomingEventInstance incoming, final OutgoingEventInstance outgoing) {
    incoming.setLocked(true);
    outgoing.setLocked(true);
    final long incomingId = incoming.getId();
    outgoing.setIncomingId(incomingId);
    final EventCoupleId eventCoupleId = new EventCoupleId(incomingId, outgoing.getId());
    return eventCoupleId;
  }
  
  public static void createJob(final EventService eventService, final EventCoupleId eventCoupleId) {
    final IncomingEventInstance ie = eventService.getIncomingEvent(eventCoupleId.getIncoming());
    final OutgoingEventInstance oe = eventService.getOutgoingEvent(eventCoupleId.getOutgoing());

    String eventPosition = null;
    final String event = ie.getSignal();
    if (event.contains(EventConstants.START)) {
      eventPosition = EventConstants.START;
    } else if (event.contains(EventConstants.BOUNDARY)) {
      eventPosition = EventConstants.BOUNDARY;
    } else if (event.contains(EventConstants.INTERMEDIATE)) {
      eventPosition = EventConstants.INTERMEDIATE;
    }
    final ProcessInstanceUUID instanceUUID = ie.getInstanceUUID();
    String rootUUID;
    if (instanceUUID != null) {
      final InternalProcessInstance instance = EnvTool.getJournal().getProcessInstance(instanceUUID);
      rootUUID = instance.getRootInstanceUUID().getValue();
    } else {
      rootUUID = ie.getActivityDefinitionUUID().getProcessUUID().getValue();
    }

    final Job job = new Job(ie.getName(), eventPosition, EventConstants.MESSAGE, rootUUID, ie.getExecutionUUID(),
        ie.getActivityDefinitionUUID(), null, System.currentTimeMillis(), instanceUUID);
    job.setEventSubProcessRootInstanceUUID(ie.getEventSubProcessRootInstanceUUID());
    if (!EventConstants.START.equals(job.getEventPosition())) {
      eventService.removeEvent(ie);
    } else {
      ie.setLocked(false);
    }
    if (oe.getParameters().isEmpty()) {
      eventService.removeEvent(oe);
    } else {
      job.setOutgoingEvent(oe);
    }
    eventService.storeJob(job);
    if (LOG.isLoggable(Level.FINE)) {
      LOG.fine("Job with eventCoupleId: " + eventCoupleId + " created.");
    }
  }

}
