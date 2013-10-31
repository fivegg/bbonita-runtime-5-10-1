package org.ow2.bonita.event;

import java.util.HashSet;
import java.util.Set;

import org.ow2.bonita.env.Environment;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.runtime.event.OutgoingEventInstance;
import org.ow2.bonita.services.EventService;
import org.ow2.bonita.util.Command;
import org.ow2.bonita.util.EnvTool;

public class GetOutgoingEventInstancesUUIDsCommand implements Command<Set<ProcessInstanceUUID>> {
  private static final long serialVersionUID = 1L;

	public Set<ProcessInstanceUUID> execute(Environment environment) throws Exception {
	  final EventService eventService = EnvTool.getEventService();
	  Set<ProcessInstanceUUID> result = new HashSet<ProcessInstanceUUID>();
    for (OutgoingEventInstance outgoing : eventService.getOutgoingEvents()) {
      result.add(outgoing.getInstanceUUID());
    }
    return result;
	}
}