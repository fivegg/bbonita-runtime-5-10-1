package org.ow2.bonita.util;

import java.lang.reflect.Constructor;

import org.ow2.bonita.env.Environment;
import org.ow2.bonita.facade.uuid.ActivityInstanceUUID;

public class ClientReplayCommand implements Command<Boolean> {

	private static final long serialVersionUID = 1L;
	private final ActivityInstanceUUID activityInstanceUUID;

	public ClientReplayCommand(final ActivityInstanceUUID activityInstanceUUID) {
		this.activityInstanceUUID = activityInstanceUUID; 
	}

	@Override
	public Boolean execute(Environment environment) throws Exception {
		final Class<?> replayCommandClass = Class.forName("org.ow2.bonita.facade.runtime.command.ReplayCommand");
		@SuppressWarnings("unchecked")
		final Constructor<Command<Boolean>> replayCommandConstructor = (Constructor<Command<Boolean>>) replayCommandClass.getConstructor(ActivityInstanceUUID.class);
		final Command<Boolean> replayCommand = (Command<Boolean>) replayCommandConstructor.newInstance(activityInstanceUUID);
		
		return replayCommand.execute(environment);
	}
}
