package org.ow2.bonita.event;

import org.ow2.bonita.env.Environment;
import org.ow2.bonita.runtime.event.IncomingEventInstance;
import org.ow2.bonita.services.EventService;
import org.ow2.bonita.util.Command;
import org.ow2.bonita.util.EnvTool;

public class RemoveIncomingEventInstancesCommand implements Command<Void> {
  private static final long serialVersionUID = 1L;

	public Void execute(Environment environment) throws Exception {
	  final EventService eventService = EnvTool.getEventService();
	  for (IncomingEventInstance incoming : eventService.getIncomingEvents()) {
	    eventService.removeEvent(incoming);
	  }
	  return null;
	}
	
}