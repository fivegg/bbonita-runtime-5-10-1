package org.ow2.bonita.event;

import java.util.HashSet;
import java.util.Set;

import org.ow2.bonita.env.Environment;
import org.ow2.bonita.runtime.event.OutgoingEventInstance;
import org.ow2.bonita.services.EventService;
import org.ow2.bonita.util.Command;
import org.ow2.bonita.util.EnvTool;

public class GetOutgoingEventInstancesCommand implements Command<Set<String>> {
  private static final long serialVersionUID = 1L;

	public Set<String> execute(Environment environment) throws Exception {
	  final EventService eventService = EnvTool.getEventService();
	  Set<String> result = new HashSet<String>();
	  for (OutgoingEventInstance outgoing : eventService.getOutgoingEvents()) {
	    result.add(outgoing.toString());
	  }
	  return result;
	}
	
}