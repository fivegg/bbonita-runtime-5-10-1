/**
 * Copyright (C) 2012-2013 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
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

import java.util.List;
import java.util.Set;

import org.ow2.bonita.services.EventService;
import org.ow2.bonita.util.Command;
import org.ow2.bonita.util.EnvTool;

/**
 * 
 * @author Matthieu Chaffotte
 * 
 */
public class MemoryLockJobExecutor extends JobExecutor {
	
	MemoryLockJobExecutor(final EventExecutor executor, final String name, final int nbOfThreads, final int locksToQuery, final int lockIdleTime) {
		super(executor, name, nbOfThreads, locksToQuery, lockIdleTime);		
	}

	@Override
	protected Long getNextDueDate() {
		return getCommandService().execute(new GetNextJobDueDate(getProcessUUIDsToExclude()));
	}

	@Override
	protected Command<List<String>> getNonlockedProcessUUIDsCommand(final Set<String> processUUIDsToExclude) {
		return new GetNonlockedProcessUUIDs(processUUIDsToExclude, getLocksToQuery());
	}

	@Override
	protected List<Job> getLockedJobs(final String processUUID) {
		final EventService eventService = EnvTool.getEventService();
		return eventService.getExecutableJobs(processUUID);		
	}

	@Override
	protected boolean lockJob(final String processUUID) {
		//already managed by the job executor running threads
		return true;
	}

	@Override
	protected void releaseLock(final String processUUID) {
		//nothing to do, managed by the jobExecutor running threads
	}

	@Override
	protected String getJobExecutorName() {
		return this.getClass().getSimpleName();
	}

}
