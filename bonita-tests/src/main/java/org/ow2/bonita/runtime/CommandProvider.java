package org.ow2.bonita.runtime;

import org.ow2.bonita.facade.uuid.ActivityInstanceUUID;
import org.ow2.bonita.util.Command;

public interface CommandProvider {

	Command<Boolean> getReplayCommand(final ActivityInstanceUUID activityInstanceUUID);
	
	Command<Boolean> getSkipExecutingActivityCommand(final ActivityInstanceUUID activityInstanceUUID);
}
