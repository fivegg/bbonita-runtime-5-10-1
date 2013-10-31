package org.ow2.bonita.runtime;

import org.ow2.bonita.facade.uuid.ActivityInstanceUUID;
import org.ow2.bonita.util.ClientReplayCommand;
import org.ow2.bonita.util.ClientSkipExecutingActivityCommand;
import org.ow2.bonita.util.Command;

public class ClientCommandProvider implements CommandProvider {

	@Override
	public Command<Boolean> getReplayCommand(final ActivityInstanceUUID activityInstanceUUID) {
		return new ClientReplayCommand(activityInstanceUUID);
	}
	
	@Override
	public Command<Boolean> getSkipExecutingActivityCommand(final ActivityInstanceUUID activityInstanceUUID) {
		return new ClientSkipExecutingActivityCommand(activityInstanceUUID);
	}

}
