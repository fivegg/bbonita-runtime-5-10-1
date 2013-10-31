package org.ow2.bonita.runtime;

import org.ow2.bonita.facade.runtime.command.ReplayCommand;
import org.ow2.bonita.facade.runtime.command.SkipExecutingActivityCommand;
import org.ow2.bonita.facade.uuid.ActivityInstanceUUID;
import org.ow2.bonita.util.Command;

public class ServerCommandProvider implements CommandProvider {

	@Override
	public Command<Boolean> getReplayCommand(final ActivityInstanceUUID activityInstanceUUID) {
		return new ReplayCommand(activityInstanceUUID);
	}
	
	@Override
	public Command<Boolean> getSkipExecutingActivityCommand(final ActivityInstanceUUID activityInstanceUUID) {
		return new SkipExecutingActivityCommand(activityInstanceUUID);
	}

}
