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
package org.ow2.bonita.util;

import java.util.List;

import org.ow2.bonita.env.Environment;
import org.ow2.bonita.facade.def.InternalActivityDefinition;
import org.ow2.bonita.facade.runtime.impl.InternalProcessInstance;
import org.ow2.bonita.runtime.event.Job;
import org.ow2.bonita.services.EventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Elias Ricken de Medeiros
 *
 */
public class UpdateJobsCommand implements Command<Void> {

  private static final long serialVersionUID = -1279648441971721363L;
  private static final int MAX_RESULTS = 1000;
  private static final Logger LOG = LoggerFactory.getLogger(UpdateJobsCommand.class);
  
  @Override
  public Void execute(final Environment environment) throws Exception {
    updateStartEventJobs();

    return null;
  }

  private void updateStartEventJobs() {
    LOG.info("updating jobs without process UUID ...");
    final EventService eventService = EnvTool.getEventService();
    final int fromIndex = 0;
    List<Job> jobs = null;
    do {      
      //the fromIndex always will be zero because the results retrieved in each iteration are updated, 
      //so will not be retrieved by the next iteration as the request is done in the same transaction  
      jobs = eventService.getJobsWithoutProcessUUID(fromIndex, MAX_RESULTS);
      updateJobs(jobs);
    } while (jobs != null && !jobs.isEmpty());
  }

  private void updateJobs(final List<Job> jobs) {
    for (final Job job : jobs) {
      if (job.getInstanceUUID() != null) {
        final InternalProcessInstance instance = EnvTool.getJournal().getProcessInstance(job.getInstanceUUID());
        job.setProcessUUID(instance.getRootInstanceUUID().getValue());
      } else if (job.getActivityDefinitionUUID() != null) {
        final InternalActivityDefinition activity = EnvTool.getJournal().getActivity(job.getActivityDefinitionUUID());
        job.setProcessUUID(activity.getProcessDefinitionUUID().getValue());
      } else {
        throw new BonitaRuntimeException("Unable to set the process UUID for the job: " + job);
      }
    }
  }

}
