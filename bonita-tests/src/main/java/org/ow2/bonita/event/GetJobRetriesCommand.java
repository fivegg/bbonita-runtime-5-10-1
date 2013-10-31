package org.ow2.bonita.event;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ow2.bonita.env.Environment;
import org.ow2.bonita.runtime.event.Job;
import org.ow2.bonita.services.EventService;
import org.ow2.bonita.util.Command;
import org.ow2.bonita.util.EnvTool;

public class GetJobRetriesCommand implements Command<Set<JobRetry>> {

  private static final long serialVersionUID = 1L;

  @Override
  public Set<JobRetry> execute(final Environment environment) throws Exception {
    final EventService eventService = EnvTool.getEventService();
    final List<Job> jobs = eventService.getJobs();
    final Set<JobRetry> result = new HashSet<JobRetry>();
    for (final Job job : jobs) {
      result.add(new JobRetry(job.getInstanceUUID(), job.getRetries()));
    }
    return result;
  }

}
