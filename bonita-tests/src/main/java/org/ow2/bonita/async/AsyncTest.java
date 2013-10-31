package org.ow2.bonita.async;

import java.io.IOException;
import java.net.URL;
import java.util.Set;

import junit.framework.Assert;

import org.bonitasoft.connectors.bonita.SetVarConnector;
import org.ow2.bonita.APITestCase;
import org.ow2.bonita.definition.TxHook;
import org.ow2.bonita.facade.APIAccessor;
import org.ow2.bonita.facade.QueryRuntimeAPI;
import org.ow2.bonita.facade.def.element.HookDefinition.Event;
import org.ow2.bonita.facade.def.majorElement.ActivityDefinition.JoinType;
import org.ow2.bonita.facade.def.majorElement.ActivityDefinition.SplitType;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.runtime.ActivityInstance;
import org.ow2.bonita.facade.runtime.ActivityState;
import org.ow2.bonita.facade.runtime.InstanceState;
import org.ow2.bonita.facade.runtime.ProcessInstance;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.light.LightActivityInstance;
import org.ow2.bonita.util.BonitaException;
import org.ow2.bonita.util.ProcessBuilder;

public class AsyncTest extends APITestCase {

	private final long basicMaxWaitTime = 120 * 1000;
	private final long basicSleepTime = 100;
	
	public void testRestartAsyncActivity() throws Exception {
    //this process must fail the first time
    ProcessDefinition process = ProcessBuilder.createProcess("p", "1.0")
    .addStringData("var2")
    .addStringData("varNumber", "1") 
    .addSystemTask("t")
    .asynchronous()
    //first setVar fail on the first exec
    .addConnector(Event.automaticOnEnter, SetVarConnector.class.getName(), true)
    .addInputParameter("variableName", "${\"var\" + varNumber}")
    .addInputParameter("value", "test")
    .done();

    process = getManagementAPI().deploy(getBusinessArchive(process, null, SetVarConnector.class));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    //check instance is not finishedT
    assertEquals(InstanceState.STARTED, getQueryRuntimeAPI().getProcessInstance(instanceUUID).getInstanceState());
    
    Set<ActivityInstance> activities = getQueryRuntimeAPI().getActivityInstances(instanceUUID, "t");
    assertEquals(1, activities.size());
    
    //set varNumber to 2 to make the second execution successfull
    getRuntimeAPI().setProcessInstanceVariable(instanceUUID, "varNumber", "2");

    //execute t2 a second time 
    getRuntimeAPI().enableEventsInFailure(instanceUUID, "t");

    this.waitForInstanceEnd(10000, 100, instanceUUID);
    
    //check instance is finished
    assertEquals(InstanceState.FINISHED, getQueryRuntimeAPI().getProcessInstance(instanceUUID).getInstanceState());

    getManagementAPI().deleteProcess(processUUID);
  }

	public void testAsyncInstance() throws BonitaException {
		//A (auto/sync) -> B(auto/async) -> C(auto/async)
		//check the instance is finished. Check the var was set (hook was executed)
		//this test check that a process having asynchronous activities works as if it hasn't.
		final URL xpdlUrl = this.getClass().getResource("asyncInstance.xpdl");
		final ProcessDefinition process = this.getManagementAPI().deploy(this.getBusinessArchiveFromXpdl(xpdlUrl, NoOpHook.class, SetVarHook.class));
		final ProcessDefinitionUUID processUUID = process.getUUID();

		final ProcessInstanceUUID instanceUUID = this.getRuntimeAPI().instantiateProcess(processUUID);

		//instantiate call is not blocking. Wait for instanceEnd
		this.waitForInstanceEnd(this.basicMaxWaitTime, this.basicSleepTime, instanceUUID);

		final ProcessInstance instance = this.getQueryRuntimeAPI().getProcessInstance(instanceUUID);
		
		Assert.assertEquals("instance is not finished", InstanceState.FINISHED, instance.getInstanceState());

		this.checkExecutedOnce(instance, new String[]{"A", "B", "C"});

		final String var = (String) this.getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "var");
		Assert.assertEquals("var was not set from hook", "setFromHook", var);

		this.getRuntimeAPI().deleteProcessInstance(instanceUUID);
		this.getManagementAPI().disable(processUUID);
		this.getManagementAPI().deleteProcess(processUUID);
	}

	public void testAsyncBfail() throws Exception {
		//A (auto/sync) -> B(auto/async) -> C(auto/sync)
		//check if B has a hook which throws an exception, A is executed, B and C not.
		//This test checks transaction has been committed before executing B
	  ProcessDefinition process = ProcessBuilder.createProcess("asyncBfail", "1.0")
    .addStringData("var", "novalue")
    
    .addSystemTask("A")
      .addConnector(Event.automaticOnEnter, NoOpHook.class.getName(), true)
    .addSystemTask("B")
      .asynchronous()
      .addConnector(Event.automaticOnEnter, FailingHook.class.getName(), true)
    .addSystemTask("C")
      .asynchronous()
      .addConnector(Event.automaticOnEnter, SetVarHook.class.getName(), true)
        .addInputParameter("variableName", "var")
        .addInputParameter("value", "newValue")
    .addTimerTask("timer", "10000")
    .addTerminateEndEvent("end")
    .addTransition("A", "timer")
    .addTransition("A", "B")
    .addTransition("B", "C")
    .addTransition("timer", "end")
    .done();

	  process = getManagementAPI().deploy(getBusinessArchive(process, null, SetVarHook.class, NoOpHook.class, FailingHook.class));
	  final ProcessDefinitionUUID processUUID = process.getUUID();
		final ProcessInstanceUUID instanceUUID = this.getRuntimeAPI().instantiateProcess(processUUID);
		
		this.waitForInstanceEnd(this.basicMaxWaitTime, this.basicSleepTime, instanceUUID);
		
		final ProcessInstance instance = this.getQueryRuntimeAPI().getProcessInstance(instanceUUID);
		  
		final Set<ActivityInstance> activityInsts = this.getQueryRuntimeAPI().getActivityInstances(instanceUUID, "A");
		Assert.assertEquals("Activity A executed more than once.", 1, activityInsts.size());
		final ActivityInstance activityInst = activityInsts.iterator().next();
		Assert.assertNotNull("Activity A not executed", activityInst);
		Assert.assertNotNull("Bad state for activity A", activityInst.getEndedDate());

		Assert.assertEquals("instance is finished", InstanceState.FINISHED, instance.getInstanceState());
		final String var = (String) this.getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "var");
		Assert.assertEquals("var was set from hook", "novalue", var);

		this.getRuntimeAPI().deleteProcessInstance(instanceUUID);
		this.getManagementAPI().disable(processUUID);
		this.getManagementAPI().deleteProcess(processUUID);
	}

	public void testAsyncTasks() throws BonitaException, IOException {
		// A (manu/sync) -> B (manu/async) -> C (manu/async) -> D (manu/sync)
		// this test check that a process having asynchronous activities works as if it hasn't.
		final URL xpdlUrl = this.getClass().getResource("asyncTasks.xpdl");
		final ProcessDefinition process = this.getManagementAPI().deploy(this.getBusinessArchiveFromXpdl(xpdlUrl, SetVarHook.class));
		final ProcessDefinitionUUID processUUID = process.getUUID();

		final ProcessInstanceUUID instanceUUID = this.getRuntimeAPI().instantiateProcess(processUUID);

		this.waitForTask(this.basicMaxWaitTime, this.basicSleepTime, instanceUUID, "A");
		this.executeTask(instanceUUID, "A");

		this.waitForTask(this.basicMaxWaitTime, this.basicSleepTime, instanceUUID, "B");
		this.executeTask(instanceUUID, "B");

		String var = (String) this.getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "var");
		Assert.assertEquals("var was not set from hook", "novalue", var);

		this.waitForTask(this.basicMaxWaitTime, this.basicSleepTime, instanceUUID, "C");
		this.executeTask(instanceUUID, "C");

		var = (String) this.getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "var");
		Assert.assertEquals("var was not set from hook", "setFromHook", var);

		this.waitForTask(this.basicMaxWaitTime, this.basicSleepTime, instanceUUID, "D");
		this.executeTask(instanceUUID, "D");

		final ProcessInstance instance = this.getQueryRuntimeAPI().getProcessInstance(instanceUUID);
		Assert.assertEquals("instance is not finished", InstanceState.FINISHED, instance.getInstanceState());

		this.getRuntimeAPI().deleteProcessInstance(instanceUUID);
		this.getManagementAPI().disable(processUUID);
		this.getManagementAPI().deleteProcess(processUUID);
	}

	public void testAsyncCycle() throws BonitaException, IOException {
		// A (manu/sync) -> B (manu/async) -> C (manu/async) -> D (manu/sync)
		// this test check that a process having asynchronous activities works as if it hasn't.
		final URL xpdlUrl = this.getClass().getResource("asyncCycle.xpdl");
		final ProcessDefinition process = this.getManagementAPI().deploy(this.getBusinessArchiveFromXpdl(xpdlUrl, NoOpHook.class, IncrementHook.class));
		final ProcessDefinitionUUID processUUID = process.getUUID();

		final ProcessInstanceUUID instanceUUID = this.getRuntimeAPI().instantiateProcess(processUUID);

		//instantiate call is not blocking. Wait for instanceEnd
		this.waitForInstanceEnd(this.basicMaxWaitTime, this.basicSleepTime, instanceUUID);

		final Long counter = (Long) this.getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "counter");
		Assert.assertEquals("counter is not equal to 5", 5, counter.intValue());

		this.checkExecutedManyTimes(instanceUUID, new String[]{"A", "B", "C"}, 5);

		final ProcessInstance instance = this.getQueryRuntimeAPI().getProcessInstance(instanceUUID);
		Assert.assertEquals("instance is not finished", InstanceState.FINISHED, instance.getInstanceState());

		this.getRuntimeAPI().deleteProcessInstance(instanceUUID);
		this.getManagementAPI().disable(processUUID);
		this.getManagementAPI().deleteProcess(processUUID);
	}

	public void testAsyncMultiInstantiator() throws BonitaException, IOException {
		// this tests checks that a multi instantiated activity async works
		final URL xpdlUrl = this.getClass().getResource("asyncMulti.xpdl");
		final ProcessDefinition process = this.getManagementAPI().deploy(this.getBusinessArchiveFromXpdl(xpdlUrl,
				NoOpHook.class, MultiIncrementHook.class, MultiInstantiator.class));
		final ProcessDefinitionUUID processUUID = process.getUUID();

		final ProcessInstanceUUID instanceUUID = this.getRuntimeAPI().instantiateProcess(processUUID);

		//instantiate call is not blocking. Wait for instanceEnd
		this.waitForInstanceEnd(this.basicMaxWaitTime, this.basicSleepTime, instanceUUID);

		final Long counter = (Long) this.getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "counter");
		final int expected = 11 + 17 + 29;
		Assert.assertEquals("counter is not equal to " + expected, expected, counter.intValue());

		this.checkExecutedOnce(instanceUUID, new String[]{"A", "C"});
		this.checkExecutedManyTimes(instanceUUID, new String[]{"B"}, 3);

		final ProcessInstance instance = this.getQueryRuntimeAPI().getProcessInstance(instanceUUID);
		Assert.assertEquals("instance is not finished", InstanceState.FINISHED, instance.getInstanceState());

		this.getRuntimeAPI().deleteProcessInstance(instanceUUID);
		this.getManagementAPI().disable(processUUID);
		this.getManagementAPI().deleteProcess(processUUID);
	}

	public void testAsyncSubflow() throws BonitaException, IOException {
		// this tests checks that a subprocess activity async works
		// and that an async activity in a subprocess works
		final URL parentXpdlUrl = this.getClass().getResource("asyncSubParent.xpdl");
		final URL childXpdlUrl = this.getClass().getResource("asyncSubChild.xpdl");
		// deploy child
		final ProcessDefinition childProcess = this.getManagementAPI().deploy(this.getBusinessArchiveFromXpdl(childXpdlUrl,
				NoOpHook.class));
		final ProcessDefinitionUUID childProcessUUID = childProcess.getUUID();

		// deploy parent
		final ProcessDefinition parentProcess = this.getManagementAPI().deploy(this.getBusinessArchiveFromXpdl(parentXpdlUrl,
				NoOpHook.class));
		final ProcessDefinitionUUID parentProcessUUID = parentProcess.getUUID();

		final ProcessInstanceUUID parentInstanceUUID = this.getRuntimeAPI().instantiateProcess(parentProcessUUID);

		this.waitForInstanceEnd(this.basicMaxWaitTime, this.basicSleepTime, parentInstanceUUID);
		
		final ProcessInstance parentInstance = this.getQueryRuntimeAPI().getProcessInstance(parentInstanceUUID);
		Assert.assertEquals("instance is not finished", InstanceState.FINISHED, parentInstance.getInstanceState());
		
		Set<ProcessInstanceUUID> childrenUUIDs = parentInstance.getChildrenInstanceUUID();
		assertNotNull(childrenUUIDs);
		assertEquals(1, childrenUUIDs.size());
		final ProcessInstanceUUID childInstanceUUID = childrenUUIDs.iterator().next();

		final ProcessInstance childInstance = this.getQueryRuntimeAPI().getProcessInstance(childInstanceUUID);
		Assert.assertEquals("instance is not finished", InstanceState.FINISHED, childInstance.getInstanceState());

		this.checkExecutedOnce(childInstanceUUID, new String[]{"A"});
		this.checkExecutedOnce(parentInstanceUUID, new String[]{"A", "B"});

		this.getRuntimeAPI().deleteProcessInstance(parentInstanceUUID);

		this.getManagementAPI().disable(parentProcessUUID);
		this.getManagementAPI().deleteProcess(parentProcessUUID);

		this.getManagementAPI().disable(childProcessUUID);
		this.getManagementAPI().deleteProcess(childProcessUUID);
	}
	
	public void testAsyncManySplitsCycleJoinXorSimpleProcess() throws BonitaException, IOException {
    ProcessDefinition process = ProcessBuilder.createProcess("p", "1.0")
    .addLongData("counter", 0L)
    .addSystemTask("Start")
    .addSystemTask("A")
      .addJoinType(JoinType.XOR)
      .addSplitType(SplitType.XOR)
    .addSystemTask("B1")
      .asynchronous()
    .addSystemTask("B2")
      .asynchronous()
    .addSystemTask("C")
      .addJoinType(JoinType.XOR)
      .addConnector(Event.automaticOnEnter, IncrementHook.class.getName(), true)
    .addSystemTask("D")

    .addTransition("Start", "A")
    .addTransition("A", "B1")
      .addCondition("counter!=1")
    .addTransition("A", "B2")
      .addCondition("counter==1")
    .addTransition("B1", "C")
    .addTransition("B2", "C")
    .addTransition("C", "D")
      .addCondition("counter==3")
    .addTransition("C", "A")
      .addCondition("counter!=3")  
    
    .done();
    
    process = this.getManagementAPI().deploy(this.getBusinessArchive(process, null, NoOpHook.class, IncrementHook.class));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    final ProcessInstanceUUID instanceUUID = this.getRuntimeAPI().instantiateProcess(processUUID);
    final int loopNb = 3;

    //instantiate call is not blocking. Wait for instanceEnd
    this.waitForInstanceEnd(1000* 5000, this.basicSleepTime, instanceUUID);

    this.checkExecutedOnce(instanceUUID, new String[]{"D", "B2"});
    this.checkExecutedManyTimes(instanceUUID, new String[] {"A", "C"}, loopNb);
    this.checkExecutedManyTimes(instanceUUID, new String[] {"B1"}, 2);

    final ProcessInstance instance = this.getQueryRuntimeAPI().getProcessInstance(instanceUUID);
    Assert.assertEquals("instance is not finished", InstanceState.FINISHED, instance.getInstanceState());

    this.getManagementAPI().deleteProcess(processUUID);
  }

	/*
	public void testAsyncManySplitsCycleJoinXor() throws BonitaException, IOException {
		final URL xpdlUrl = this.getClass().getResource("asyncSplitCycleJoinXor.xpdl");
		//A, C and D are sync, others are async
		final ProcessDefinition process = this.getManagementAPI().deploy(this.getBusinessArchiveFromXpdl(xpdlUrl,
				NoOpHook.class, IncrementHook.class));
		final ProcessDefinitionUUID processUUID = process.getUUID();

		final ProcessInstanceUUID instanceUUID = this.getRuntimeAPI().instantiateProcess(processUUID);
		final int loopNb = 20;

		//instantiate call is not blocking. Wait for instanceEnd
		this.waitForInstanceEnd(this.basicMaxWaitTime * (1 + loopNb / 2), this.basicSleepTime, instanceUUID);

		this.checkExecutedOnce(instanceUUID, new String[]{"D"});

		this.checkExecutedManyTimes(instanceUUID, new String[]{"A", "C"}, loopNb);

		final ProcessInstance instance = this.getQueryRuntimeAPI().getProcessInstance(instanceUUID);
		Assert.assertEquals("instance is not finished", InstanceState.FINISHED, instance.getInstanceState());

		this.getRuntimeAPI().deleteProcessInstance(instanceUUID);
		this.getManagementAPI().disable(processUUID);
		this.getManagementAPI().deleteProcess(processUUID);
	}

	public void testAsyncManySplitsCycleJoinXorBuilder() throws BonitaException, IOException {
		ProcessDefinition process = ProcessBuilder.createProcess("asyncSplitCycle", "1.0")
		.addLongData("counter", 0L)
		.addSystemTask("Start")
		.addSystemTask("A")
		.addJoinType(JoinType.XOR)
		.addConnector(Event.automaticOnEnter, NoOpHook.class.getName(), true)
		.addSystemTask("D")
		.addConnector(Event.automaticOnEnter, NoOpHook.class.getName(), true)
		.addSystemTask("C")
		.addJoinType(JoinType.XOR)
		.addConnector(Event.automaticOnEnter, IncrementHook.class.getName(), true)
		.addSystemTask("B1")
		.asynchronous()
		.addConnector(Event.automaticOnEnter, NoOpHook.class.getName(), true)
		.addSystemTask("B2")
		.asynchronous()
		.addConnector(Event.automaticOnEnter, NoOpHook.class.getName(), true)
		.addSystemTask("B3")
		.asynchronous()
		.addConnector(Event.automaticOnEnter, NoOpHook.class.getName(), true)
		.addSystemTask("B4")
		.asynchronous()
		.addConnector(Event.automaticOnEnter, NoOpHook.class.getName(), true)
		.addSystemTask("B5")
		.asynchronous()
		.addConnector(Event.automaticOnEnter, NoOpHook.class.getName(), true)
		.addSystemTask("B6")
		.asynchronous()
		.addConnector(Event.automaticOnEnter, NoOpHook.class.getName(), true)
		.addSystemTask("B7")
		.asynchronous()
		.addConnector(Event.automaticOnEnter, NoOpHook.class.getName(), true)
		.addSystemTask("B8")
		.asynchronous()
		.addConnector(Event.automaticOnEnter, NoOpHook.class.getName(), true)
		.addSystemTask("B9")
		.asynchronous()
		.addConnector(Event.automaticOnEnter, NoOpHook.class.getName(), true)
		.addSystemTask("B10")
		.asynchronous()
		.addConnector(Event.automaticOnEnter, NoOpHook.class.getName(), true)
		.addTransition("Start", "A")
		.addTransition("B1", "C").addTransition("B2", "C").addTransition("B3", "C").addTransition("B4", "C")
		.addTransition("B5", "C").addTransition("B6", "C").addTransition("B7", "C").addTransition("B8", "C")
		.addTransition("B9", "C").addTransition("B10", "C")
		.addTransition("C", "D")
		.addCondition("counter == 20")
		.addTransition("C", "A")
		.addCondition("counter != 20")
		.addTransition("A", "B1").addTransition("A", "B2").addTransition("A", "B3").addTransition("A", "B4")
		.addTransition("A", "B5").addTransition("A", "B6").addTransition("A", "B7").addTransition("A", "B8")
		.addTransition("A", "B9").addTransition("A", "B10")
		.done();
		//A, C and D are sync, others are async
		process = this.getManagementAPI().deploy(this.getBusinessArchive(process, null, NoOpHook.class, IncrementHook.class));
		final ProcessDefinitionUUID processUUID = process.getUUID();

		final ProcessInstanceUUID instanceUUID = this.getRuntimeAPI().instantiateProcess(processUUID);
		final int loopNb = 20;

		//instantiate call is not blocking. Wait for instanceEnd
		this.waitForInstanceEnd(this.basicMaxWaitTime * (1 + loopNb / 2), this.basicSleepTime, instanceUUID);

		this.checkExecutedOnce(instanceUUID, new String[]{"D"});

		this.checkExecutedManyTimes(instanceUUID, new String[]{"A", "C"}, loopNb);

		final ProcessInstance instance = this.getQueryRuntimeAPI().getProcessInstance(instanceUUID);
		Assert.assertEquals("instance is not finished", InstanceState.FINISHED, instance.getInstanceState());

		this.getRuntimeAPI().deleteProcessInstance(instanceUUID);
		this.getManagementAPI().disable(processUUID);
		this.getManagementAPI().deleteProcess(processUUID);
	}
*/
	public void testAsyncJoinXor() throws BonitaException, IOException {
		final URL xpdlUrl = this.getClass().getResource("asyncJoinXor.xpdl");
		final ProcessDefinition process = this.getManagementAPI().deploy(this.getBusinessArchiveFromXpdl(xpdlUrl,
				NoOpHook.class));
		final ProcessDefinitionUUID processUUID = process.getUUID();

		final ProcessInstanceUUID instanceUUID = this.getRuntimeAPI().instantiateProcess(processUUID);

		this.waitForTask(this.basicMaxWaitTime, this.basicSleepTime, instanceUUID, "D");

		try {
			//important so that second event on C executes
			Thread.sleep(2000);
		} catch (final InterruptedException e) {

		}

		this.executeTask(instanceUUID, "D");

		//instantiate call is not blocking. Wait for instanceEnd
		this.waitForInstanceEnd(this.basicMaxWaitTime, this.basicSleepTime, instanceUUID);

		this.checkExecutedOnce(instanceUUID, new String[]{"A", "C", "D"});

		final ProcessInstance instance = this.getQueryRuntimeAPI().getProcessInstance(instanceUUID);
		Assert.assertEquals("instance is not finished", InstanceState.FINISHED, instance.getInstanceState());

		this.getRuntimeAPI().deleteProcessInstance(instanceUUID);
		this.getManagementAPI().disable(processUUID);
		this.getManagementAPI().deleteProcess(processUUID);
	}

	public void testAsyncSubProcess() throws BonitaException {
		ProcessDefinition parentProcess = ProcessBuilder.createProcess("parentProcess", "1.0")
		.addSubProcess("subProcess", "subProcess")
		.asynchronous()
		.addSystemTask("end")
		.addTransition("subProcess_end", "subProcess", "end")
		.done();

		ProcessDefinition childProcess = ProcessBuilder.createProcess("subProcess", "1.0")
		.addSystemTask("t")
		.addConnector(Event.automaticOnEnter, GetPIHook.class.getName(), true)
		.done();            

		childProcess = getManagementAPI().deploy(getBusinessArchive(childProcess, null, GetPIHook.class));
		parentProcess = getManagementAPI().deploy(getBusinessArchive(parentProcess));

		ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(parentProcess.getUUID());

		this.waitForInstanceEnd(5000, 50, instanceUUID);

		assertEquals(InstanceState.FINISHED, getQueryRuntimeAPI().getProcessInstance(instanceUUID).getInstanceState());

		getRuntimeAPI().deleteProcessInstance(instanceUUID);

		getManagementAPI().deleteProcess(parentProcess.getUUID());
		getManagementAPI().deleteProcess(childProcess.getUUID());
	}


	public static class GetPIHook implements TxHook {
		public void execute(APIAccessor arg0, ActivityInstance arg1) throws Exception {
			System.out.println("GetPIHook");
			QueryRuntimeAPI qrAPI = arg0.getQueryRuntimeAPI();
			ProcessInstanceUUID parentUUID = qrAPI.getProcessInstance(arg1.getProcessInstanceUUID()).getParentInstanceUUID();
			qrAPI.getProcessInstance(parentUUID);
		}
	}
	
	public void testCannotStayAndLeaveAcycleAtSameTime() throws BonitaException, IOException {
    ProcessDefinition process = ProcessBuilder.createProcess("p", "1.0")
    .addLongData("counter", 0L)
    .addSystemTask("Start")
    .addSystemTask("A")
      .addJoinType(JoinType.XOR)
    .addSystemTask("B1")
      .asynchronous()
    .addSystemTask("B2")
      .asynchronous()
    .addSystemTask("C")
      .addJoinType(JoinType.XOR)
      .addConnector(Event.automaticOnEnter, IncrementHook.class.getName(), true)
    .addSystemTask("D")

    .addTransition("Start", "A")
    .addTransition("A", "B1")
    .addTransition("A", "B2")
    .addTransition("B1", "C")
    .addTransition("B2", "C")
    .addTransition("C", "D")
      .addCondition("counter==3")
    .addTransition("C", "A")
      .addCondition("counter!=3")  
    .done();
    
    process = this.getManagementAPI().deploy(this.getBusinessArchive(process, null, NoOpHook.class, IncrementHook.class));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    final ProcessInstanceUUID instanceUUID = this.getRuntimeAPI().instantiateProcess(processUUID);
    final Set<LightActivityInstance> activityInstances = getQueryRuntimeAPI().getLightActivityInstances(instanceUUID, "A");
    assertEquals(1, activityInstances.size());
    final LightActivityInstance activityInstance = activityInstances.iterator().next();
    assertEquals(ActivityState.FAILED, activityInstance.getState());
    
    //instantiate call is not blocking. Wait for instanceEnd
     this.getManagementAPI().deleteProcess(processUUID);
  }

}
