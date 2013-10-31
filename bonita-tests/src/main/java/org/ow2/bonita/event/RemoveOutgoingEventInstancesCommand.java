package org.ow2.bonita.event;

import org.ow2.bonita.env.Environment;
import org.ow2.bonita.runtime.event.OutgoingEventInstance;
import org.ow2.bonita.services.EventService;
import org.ow2.bonita.util.Command;
import org.ow2.bonita.util.EnvTool;

public class RemoveOutgoingEventInstancesCommand implements Command<Void> {

  private static final long serialVersionUID = 1L;

	public Void execute(Environment environment) throws Exception {
	  final EventService eventService = EnvTool.getEventService();
	  for (OutgoingEventInstance outgoing : eventService.getOutgoingEvents()) {
	    eventService.removeEvent(outgoing);
	  }
	  return null;
	}
	
}