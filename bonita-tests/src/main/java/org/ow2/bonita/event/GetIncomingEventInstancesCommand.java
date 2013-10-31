package org.ow2.bonita.event;

import java.util.HashSet;
import java.util.Set;

import org.ow2.bonita.env.Environment;
import org.ow2.bonita.runtime.event.IncomingEventInstance;
import org.ow2.bonita.services.EventService;
import org.ow2.bonita.util.Command;
import org.ow2.bonita.util.EnvTool;

public class GetIncomingEventInstancesCommand implements Command<Set<String>> {
  private static final long serialVersionUID = 1L;

	public Set<String> execute(Environment environment) throws Exception {
	  final EventService eventService = EnvTool.getEventService();
	  Set<String> result = new HashSet<String>();
    for (IncomingEventInstance incoming : eventService.getIncomingEvents()) {
      result.add(incoming.toString());
    }
    return result;
	}
}