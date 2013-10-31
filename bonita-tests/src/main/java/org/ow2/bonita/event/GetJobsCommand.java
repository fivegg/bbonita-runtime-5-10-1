package org.ow2.bonita.event;

import java.util.HashSet;
import java.util.Set;

import org.ow2.bonita.env.Environment;
import org.ow2.bonita.runtime.event.Job;
import org.ow2.bonita.services.EventService;
import org.ow2.bonita.util.Command;
import org.ow2.bonita.util.EnvTool;

public class GetJobsCommand implements Command<Set<String>> {

  private static final long serialVersionUID = 8095303588024772988L;

  public Set<String> execute(Environment environment) throws Exception {
	  final EventService eventService = EnvTool.getEventService();
	  Set<String> result = new HashSet<String>();
	  for (Job job : eventService.getJobs()) {
	    result.add(job.toString());
	  }
	  return result;
	}

}