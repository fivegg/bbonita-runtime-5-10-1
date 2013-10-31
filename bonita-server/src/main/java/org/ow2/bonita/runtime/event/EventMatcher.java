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

import java.util.Set;
import java.util.logging.Level;

/**
 * 
 * @author Matthieu Chaffotte
 * 
 */
public class EventMatcher extends SlicedEventExecutorThread {

	private boolean matchingConditionEnable;

	private int maxCouples;

	EventMatcher(final EventExecutor executor, final String name) {
		super(executor, name);
	}

	@Override
	protected void activate() {
		// nothing to do
	}

	@Override
	protected void execute() {
		getCommandService().execute(new RemoveOverdueEvents());
		Set<EventCoupleId> coupleIds;
		do {
			if (LOG.isLoggable(Level.FINE)) {
				LOG.fine("Getting couples...");
			}
			coupleIds = getCommandService().execute(new GetEventCouples(maxCouples));
			if (LOG.isLoggable(Level.FINE)) {
				LOG.fine(this.getClass().getSimpleName() + " on node " + System.getProperty("bonita.node.name") + " has got " + coupleIds.size() + " couples");
			}
			if (LOG.isLoggable(Level.FINE)) {
				LOG.fine("Creating jobs...");
			}
			storeJobs(coupleIds);
			if (LOG.isLoggable(Level.FINE)) {
				LOG.fine("Jobs created.");
			}
			if (LOG.isLoggable(Level.FINE)) {
				LOG.fine("All couples handled, looping...");
			}
		} while (coupleIds.size() == maxCouples);


		if (matchingConditionEnable) {
			final Set<EventCoupleId> coupleIds2 = getCommandService().execute(new GetExpressionMessageEventCouples());
			storeJobs(coupleIds2);
		}
	}

	@Override
	protected String getJobExecutorName() {
		return "Correlation key message matcher";
	}

	@Override
	protected Long getNextDueDate() {
		//DB correlation next due date
		final Long dueDate1 = getCommandService().execute(new GetNextEventDueDate());
		if (matchingConditionEnable) {
			//matching condition next due date
			final Long dueDate2 = getCommandService().execute(new GetNextExpressionEventDueDate());
			if (dueDate1 == null && dueDate2 == null) {
				return null;
			} else if (dueDate1 != null && dueDate2 == null) {
				return dueDate1;
			} else if (dueDate2 != null && dueDate1 == null) {
				return dueDate2;
			} else {
				return Math.min(dueDate1, dueDate2);
			}
		} else {
			return dueDate1;
		}
	}

	public void setMatchingConditionMatcher(final boolean matchingConditionEnable) {
		this.matchingConditionEnable = matchingConditionEnable;
	}

	public void setMaxCouples(final int maxCouples) {
		this.maxCouples = maxCouples;
	}

}
