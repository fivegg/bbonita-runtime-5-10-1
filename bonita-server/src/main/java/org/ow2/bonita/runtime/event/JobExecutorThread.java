/**
 * Copyright (C) 2012-2013 BonitaSoft S.A.
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

import java.util.logging.Logger;

import org.ow2.bonita.util.Misc;

/**
 * @author Matthieu Chaffotte
 */
public class JobExecutorThread implements Runnable {

	private static final Logger LOG = Logger.getLogger(JobExecutorThread.class.getName());

	private final EventExecutor eventExecutor;

	private final ExecuteJobsCommand cmd;

	public JobExecutorThread(final EventExecutor eventExecutor, final ExecuteJobsCommand cmd) {
		this.eventExecutor = eventExecutor;
		this.cmd = cmd;
	}

	@Override
	public void run() {
		try {
			eventExecutor.getCommandService().execute(cmd);
	      } catch (final Throwable e) {
	        LOG.severe("Exception in event block: " + Misc.getStackTraceFrom(e));
	      }
		
	}

}
