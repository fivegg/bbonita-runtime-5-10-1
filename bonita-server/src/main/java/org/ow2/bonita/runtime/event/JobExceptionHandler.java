/**
 * Copyright (C) 2010-2013 BonitaSoft S.A.
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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.transaction.Synchronization;

import org.ow2.bonita.env.Environment;
import org.ow2.bonita.runtime.model.Execution;
import org.ow2.bonita.services.CommandService;
import org.ow2.bonita.util.Command;
import org.ow2.bonita.util.EnvTool;

/**
 * 
 * @author Charles Souillard, Matthieu Chaffotte
 * 
 */
public class JobExceptionHandler implements Synchronization, Command<String> {

  private static final long serialVersionUID = 219531695187147766L;

  private static final Logger LOG = Logger.getLogger(JobExceptionHandler.class.getName());

  protected transient CommandService commandService;

  protected long jobId;

  protected Throwable exception;

  public JobExceptionHandler(final long jobId, final Throwable exception, final CommandService commandService) {
    this.commandService = commandService;
    this.jobId = jobId;
    this.exception = exception;
  }

  @Override
  public void beforeCompletion() {
  }

  @Override
  public void afterCompletion(final int status) {
    // after the transaction rolled back,
    // execute this job exception handler object as a command with
    // the command service so that this gets done in a separate transaction
    LOG.severe("starting new transaction for handling job exception");
    final String processUUID = commandService.execute(this);
    LOG.severe("completed transaction for handling job exception");
    commandService.execute(new Command<Void>() {
    	@Override
    	public Void execute(Environment environment) throws Exception {
    		EnvTool.getEventExecutor().getJobExecutor().notifyThreadFinished(processUUID);
    		return null;
    	}
	});
  }

  @Override
  public String execute(final Environment environment) throws Exception {
    // serialize the stack trace
    final StringWriter sw = new StringWriter();
    exception.printStackTrace(new PrintWriter(sw));
    final Job job = EnvTool.getEventService().getJob(jobId);
    if (job.getExecutionUUID() != null) {
      final Execution execution = EnvTool.getJournal().getExecutionWithEventUUID(job.getExecutionUUID());
      if (execution != null) {
        if (!Execution.STATE_ACTIVE.equals(execution.getState())) {
          execution.unlock();
        }
        execution.lock(execution.getState());
      }
    }

    final int decrementedRetries = job.getRetries() - 1;
    if (LOG.isLoggable(Level.INFO)) {
      LOG.info("Decrementing retries on job: " + job + ": " + decrementedRetries);
    }
    job.setRetries(decrementedRetries);
    job.setException(sw.toString());
    return job.getProcessUUID();
  }

}
