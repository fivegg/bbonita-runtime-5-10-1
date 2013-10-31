package org.ow2.bonita.runtime;

import java.util.Collection;

import org.ow2.bonita.APITestCase;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.runtime.ActivityState;
import org.ow2.bonita.facade.runtime.InstanceState;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.light.LightTaskInstance;
import org.ow2.bonita.util.BusinessArchiveFactory;
import org.ow2.bonita.util.Command;
import org.ow2.bonita.util.ProcessBuilder;

public class SkipExecutingActivityCommandTest extends APITestCase {

	private Command getCommand() {
		return null;
	}
	public void testSkipExecutingActivityServer() throws Exception {
		testSkipExecutingActivity(new ServerCommandProvider());
	}

	public void testSkipExecutingActivityClient() throws Exception {
		testSkipExecutingActivity(new ClientCommandProvider());
	}


	private void testSkipExecutingActivity(final CommandProvider commandProvider) throws Exception {
		ProcessDefinition process = ProcessBuilder.createProcess("myProcess", "1.0")
				.addStringData("globalVar", "defaultValue")
				.addHuman(getLogin())
				.addHumanTask("step1", getLogin())
				.done();

		//deploy process
		process = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process));

		LOG.info("Process deployed");

		final ProcessDefinitionUUID processUUID = process.getUUID();
		//instantiate process
		ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);
		LOG.info("New process instance Created");

		final Collection<LightTaskInstance> taskList = getQueryRuntimeAPI().getLightTaskList(instanceUUID, ActivityState.READY);
		if (taskList.size() != 1) {
			throw new Exception("Incorrect list size. Actual size: " + taskList.size());
		}

		//execute task
		final LightTaskInstance taskInstance = taskList.iterator().next();
		getRuntimeAPI().startTask(taskInstance.getUUID(), true);
		LOG.info("Task started");

		ActivityState stateTask = getQueryRuntimeAPI().getActivityInstanceState(taskInstance.getUUID());
		if(!stateTask.equals(ActivityState.EXECUTING)){
			throw new Exception("Incorrect state. Suppose to be EXECUTING but found "+stateTask.toString());
		}

		LOG.info("Skip Executing task started");
		final Command<Boolean> command = commandProvider.getSkipExecutingActivityCommand(taskInstance.getUUID());

		if(!getCommandAPI().execute(command)){
			throw new Exception("Incorrect response. Suppose to be TRUE but found FALSE");
		}

		final InstanceState state = getQueryRuntimeAPI().getProcessInstance(instanceUUID).getInstanceState();
		if(!state.equals(InstanceState.FINISHED)){
			throw new Exception("Incorrect state. Actual state: " + state);
		}

		getManagementAPI().deleteProcess(process.getUUID());
	}


}