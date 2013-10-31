package org.ow2.bonita.runtime.event;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public abstract class SlicedEventExecutorThread extends EventExecutorThread {

	
	protected SlicedEventExecutorThread(EventExecutor executor, String name) {
		super(executor, name);
	}

	protected void storeJobs(final Set<EventCoupleId> eventCoupleIds) {
		final int batchSize = 50;

		if (eventCoupleIds.size() <= batchSize) {
			getCommandService().execute(new CreateJobs(eventCoupleIds));
		} else {
			//build slices
			final Set<EventCoupleId> eventCoupleIdsToProcessInThisLoop = new HashSet<EventCoupleId>();
			final Iterator<EventCoupleId> it = eventCoupleIds.iterator();
			while (it.hasNext()) {
				eventCoupleIdsToProcessInThisLoop.clear();
				while (it.hasNext() && eventCoupleIdsToProcessInThisLoop.size() < batchSize) {
					eventCoupleIdsToProcessInThisLoop.add(it.next());
				}
				getCommandService().execute(new CreateJobs(eventCoupleIdsToProcessInThisLoop));
			}
		}
	}
	
}
