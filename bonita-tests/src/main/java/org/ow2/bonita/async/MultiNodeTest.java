package org.ow2.bonita.async;

import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.connectors.bonita.SetVarConnector;
import org.ow2.bonita.APITestCase;
import org.ow2.bonita.env.GlobalEnvironmentFactory;
import org.ow2.bonita.facade.def.element.HookDefinition.Event;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.runtime.InstanceState;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.runtime.event.EventExecutor;
import org.ow2.bonita.services.CommandService;
import org.ow2.bonita.util.BonitaConstants;
import org.ow2.bonita.util.ProcessBuilder;

public class MultiNodeTest extends APITestCase {

	public void testSeveralEventExecutors() throws Exception {
		final long waitTime = 10000L;
		
		ProcessDefinition process = ProcessBuilder.createProcess("p", "1.0")
				.addSystemTask("t")
				.asynchronous()
				.addConnector(Event.automaticOnExit, WaitConnector.class.getName(), true)
				.addInputParameter("time", waitTime)
				.done();


		process = getManagementAPI().deploy(getBusinessArchive(process, null, SetVarConnector.class));
		final ProcessDefinitionUUID processUUID = process.getUUID();

		final CommandService commandService = GlobalEnvironmentFactory.getEnvironmentFactory(BonitaConstants.DEFAULT_DOMAIN).get(CommandService.class);

		final EventExecutor eventExecutor = new EventExecutor();
		eventExecutor.setCommandService(commandService);
		eventExecutor.start();

		final long maxTime = System.currentTimeMillis() + 30000;

		final int NUMBER_OF_INSTANCES = 10;
		final List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();
		for (int i = 0 ; i < NUMBER_OF_INSTANCES ; i++) {
			final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);
			instanceUUIDs.add(instanceUUID);
			Thread.sleep(2000L);
		}

		Thread.sleep(waitTime + 15000L);
		for (ProcessInstanceUUID instanceUUID : instanceUUIDs) {
			do {
				Thread.sleep(20);
			} while (
					(!InstanceState.FINISHED.equals(getQueryRuntimeAPI().getProcessInstance(instanceUUID).getInstanceState())) 
					&& 
					maxTime < System.currentTimeMillis()
					);
		}
		
		for (ProcessInstanceUUID instanceUUID : instanceUUIDs) {
			assertEquals(InstanceState.FINISHED, getQueryRuntimeAPI().getProcessInstance(instanceUUID).getInstanceState());
		}
		/*
    //check instance is not finished
    assertEquals(InstanceState.STARTED, getQueryRuntimeAPI().getProcessInstance(instanceUUID).getInstanceState());

    Set<ActivityInstance> activities = getQueryRuntimeAPI().getActivityInstances(instanceUUID, "t");
    assertEquals(1, activities.size());

    Thread.sleep(2000);
    //set varNumber to 2 to make the second execution successfull
    getRuntimeAPI().setProcessInstanceVariable(instanceUUID, "varNumber", "2");

    //execute t2 a second time 
    getRuntimeAPI().enableEventsInFailure(instanceUUID, "t");

    this.waitForInstanceEnd(5000, 100, instanceUUID);

    //check instance is finished
    assertEquals(InstanceState.FINISHED, getQueryRuntimeAPI().getProcessInstance(instanceUUID).getInstanceState());
		 */
		getManagementAPI().deleteProcess(processUUID);
	}

}
