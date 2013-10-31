package org.ow2.bonita.util;

import java.lang.reflect.Constructor;

import org.ow2.bonita.env.Environment;
import org.ow2.bonita.facade.uuid.ActivityInstanceUUID;

public class ClientSkipExecutingActivityCommand implements Command<Boolean> {

	private static final long serialVersionUID = 1L;
	private final ActivityInstanceUUID activityInstanceUUID;

	public ClientSkipExecutingActivityCommand(final ActivityInstanceUUID activityInstanceUUID) {
		this.activityInstanceUUID = activityInstanceUUID; 
	}

	@Override
	public Boolean execute(Environment environment) throws Exception {
		final Class<?> commandClass = Class.forName("org.ow2.bonita.facade.runtime.command.SkipExecutingActivityCommand");
		@SuppressWarnings("unchecked")
		final Constructor<Command<Boolean>> commandConstructor = (Constructor<Command<Boolean>>) commandClass.getConstructor(ActivityInstanceUUID.class);
		final Command<Boolean> command = (Command<Boolean>) commandConstructor.newInstance(activityInstanceUUID);
		
		return command.execute(environment);
	}
}
