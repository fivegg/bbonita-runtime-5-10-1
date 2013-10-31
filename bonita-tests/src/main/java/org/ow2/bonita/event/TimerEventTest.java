package org.ow2.bonita.event;

import java.util.Collection;
import java.util.Date;
import java.util.Set;

import org.bonitasoft.connectors.bonita.SetVarConnector;
import org.bonitasoft.connectors.scripting.GroovyConnector;
import org.ow2.bonita.APITestCase;
import org.ow2.bonita.facade.def.element.BusinessArchive;
import org.ow2.bonita.facade.def.element.HookDefinition.Event;
import org.ow2.bonita.facade.def.majorElement.ActivityDefinition.JoinType;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.runtime.ActivityState;
import org.ow2.bonita.facade.runtime.InstanceState;
import org.ow2.bonita.facade.runtime.ProcessInstance;
import org.ow2.bonita.facade.runtime.TaskInstance;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.light.LightActivityInstance;
import org.ow2.bonita.light.LightProcessInstance;
import org.ow2.bonita.light.LightTaskInstance;
import org.ow2.bonita.util.BonitaConstants;
import org.ow2.bonita.util.ProcessBuilder;

public class TimerEventTest extends APITestCase {

  public void testCancelTimer() throws Exception {
    ProcessDefinition process = ProcessBuilder.createProcess("p", "d")
    .addHuman(getLogin())
    .addHumanTask("human", getLogin())
    .addTimerTask("timer", "${new Date(System.currentTimeMillis() + 1000)}")
    .addSystemTask("start")
    .addDecisionNode("join")
      .addJoinType(JoinType.XOR)
    .addTransition("human", "join")
    .addTransition("timer", "join")
    .addTransition("start", "human")
    .addTransition("start", "timer")
    .done();

    process = getManagementAPI().deploy(getBusinessArchive(process));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);
    getRuntimeAPI().cancelProcessInstance(instanceUUID);
    assertEquals(InstanceState.CANCELLED, getQueryRuntimeAPI().getProcessInstance(instanceUUID).getInstanceState());
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testTimerDeletion() throws Exception {
    ProcessDefinition process = ProcessBuilder.createProcess("p", "d")
    .addSystemTask("start")
    .addSystemTask("system")
    .addTimerTask("timer", "${new Date(System.currentTimeMillis() + 100000)}")
    .addDecisionNode("join")
      .addJoinType(JoinType.XOR)
    .addTransition("start", "system")
    .addTransition("start", "timer")
    .addTransition("system", "join")
    .addTransition("timer", "join")
    .done();

    process = getManagementAPI().deploy(getBusinessArchive(process, null, GetIncomingEventInstancesCommand.class, GetOutgoingEventInstancesCommand.class));
    final ProcessDefinitionUUID processUUID = process.getUUID();
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);
    this.waitForInstanceEnd(3000, 10, instanceUUID);

    assertEquals(InstanceState.FINISHED, getQueryRuntimeAPI().getProcessInstance(instanceUUID).getInstanceState());
    assertEquals(0, (int) getCommandAPI().execute(new GetIncomingEventInstancesCommand(), processUUID).size());
    assertEquals(0, (int) getCommandAPI().execute(new GetOutgoingEventInstancesCommand(), processUUID).size());

    getManagementAPI().deleteProcess(processUUID);
  }

  public void testSimpleTimerSystemTask() throws Exception {
  	ProcessDefinition process = ProcessBuilder.createProcess("p", "d")
  	.addSystemTask("start")
  	.addSystemTask("system")
  	.addTimerTask("timer", "${new Date(System.currentTimeMillis() + 100)}")
  	.addDecisionNode("join")
  	  .addJoinType(JoinType.AND)
  	.addTransition("start", "system")
    .addTransition("start", "timer")
  	.addTransition("system", "join")
  	.addTransition("timer", "join")
  	.done();

  	process = getManagementAPI().deploy(getBusinessArchive(process));
  	final ProcessDefinitionUUID processUUID = process.getUUID();
  	final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);
  	this.waitForInstanceEnd(3000, 10, instanceUUID);

  	assertEquals(InstanceState.FINISHED, getQueryRuntimeAPI().getProcessInstance(instanceUUID).getInstanceState());

  	getManagementAPI().deleteProcess(processUUID);
  }
  
  public void testSimpleTimerHumanTask() throws Exception {
  	ProcessDefinition process = ProcessBuilder.createProcess("p", "d")
  	.addHuman(getLogin())
  	.addSystemTask("start")
  	.addHumanTask("human", getLogin())
  	.addTimerTask("timer", "${new Date(System.currentTimeMillis() + 1000)}")
  	.addDecisionNode("join")
  	  .addJoinType(JoinType.AND)
  	.addTransition("start", "human")
    .addTransition("start", "timer")
  	.addTransition("human", "join")
  	.addTransition("timer", "join")
  	.done();

  	process = getManagementAPI().deploy(getBusinessArchive(process));
  	final ProcessDefinitionUUID processUUID = process.getUUID();
  	final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);
  	assertEquals(InstanceState.STARTED, getQueryRuntimeAPI().getProcessInstance(instanceUUID).getInstanceState());

  	Collection<TaskInstance> tasks = getQueryRuntimeAPI().getTaskList(ActivityState.READY);
  	assertEquals(1, tasks.size());
  	final TaskInstance human = tasks.iterator().next();
  	assertEquals("human", human.getActivityName());

  	getRuntimeAPI().executeTask(human.getUUID(), true);

  	Thread.sleep(300);
  	this.waitForInstanceEnd(5000, 10, instanceUUID);
  	assertEquals(InstanceState.FINISHED, getQueryRuntimeAPI().getProcessInstance(instanceUUID).getInstanceState());
  	checkExecutedOnce(instanceUUID, "human", "timer");
  	assertEquals(InstanceState.FINISHED, getQueryRuntimeAPI().getProcessInstance(instanceUUID).getInstanceState());

  	getManagementAPI().deleteProcess(processUUID);
  }
  
  public void testSimpleTimerXOR() throws Exception {
  	final long aDay = 1 * 1000 * 60 * 60 * 24;
  	ProcessDefinition process = ProcessBuilder.createProcess("p", "d")
  	.addHuman(getLogin())
  	.addSystemTask("start")
  	.addHumanTask("human", getLogin())
  	.addTimerTask("timer", Long.toString(aDay))
  	.addDecisionNode("join")
  	  .addJoinType(JoinType.XOR)
  	.addTransition("start", "human")
    .addTransition("start", "timer")
  	.addTransition("human", "join")
  	.addTransition("timer", "join")
  	.done();

  	process = getManagementAPI().deploy(getBusinessArchive(process, null));
  	final ProcessDefinitionUUID processUUID = process.getUUID();
  	final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

  	assertEquals(InstanceState.STARTED, getQueryRuntimeAPI().getProcessInstance(instanceUUID).getInstanceState());

  	Collection<TaskInstance> tasks = getQueryRuntimeAPI().getTaskList(ActivityState.READY);
  	assertEquals(1, tasks.size());
  	final TaskInstance human = tasks.iterator().next();
  	assertEquals("human", human.getActivityName());

  	executeTask(instanceUUID, "human");
  	ProcessInstance instance = getQueryRuntimeAPI().getProcessInstance(instanceUUID);
  	assertEquals(InstanceState.FINISHED, instance.getInstanceState());
  	checkExecutedOnce(instanceUUID, "human");
  	
  	getManagementAPI().deleteProcess(processUUID);
  }
  
  public void testSimpleTimerHumanXOR() throws Exception {
  	ProcessDefinition process = ProcessBuilder.createProcess("p", "d")
  	.addHuman(getLogin())
  	.addSystemTask("start")
  	.addHumanTask("human", getLogin())
  	.addTimerTask("timer", "${new Date(System.currentTimeMillis() + 1000)}")
  	.addDecisionNode("join")
  	  .addJoinType(JoinType.XOR)
  	.addTransition("start", "human")
    .addTransition("start", "timer")
  	.addTransition("human", "join")
  	.addTransition("timer", "join")
  	.done();

  	process = getManagementAPI().deploy(getBusinessArchive(process));
  	final ProcessDefinitionUUID processUUID = process.getUUID();
  	final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

  	assertEquals(InstanceState.STARTED, getQueryRuntimeAPI().getProcessInstance(instanceUUID).getInstanceState());

  	Collection<TaskInstance> tasks = getQueryRuntimeAPI().getTaskList(ActivityState.READY);
  	assertEquals(1, tasks.size());
  	final TaskInstance human = tasks.iterator().next();
  	assertEquals("human", human.getActivityName());

  	this.waitForInstanceEnd(4000, 500, instanceUUID);

  	assertEquals(InstanceState.FINISHED, getQueryRuntimeAPI().getProcessInstance(instanceUUID).getInstanceState());
  	checkExecutedOnce(instanceUUID, "timer", "join");
  	getManagementAPI().deleteProcess(processUUID);
  }

  public void testSimpleTimerHumanWithConnector() throws Exception {
  	ProcessDefinition process = ProcessBuilder.createProcess("p", "d")
  	.addStringData("var")
  	.addHuman(getLogin())
  	.addSystemTask("start")
  	.addHumanTask("human", getLogin())
  	  .addConnector(Event.taskOnReady, SetVarConnector.class.getName(), true)
  	    .addInputParameter("variableName", "var")
  	    .addInputParameter("value", "human")
  	.addTimerTask("timer", "${new Date(System.currentTimeMillis() + 1000)}")
  	  .addConnector(Event.onTimer, SetVarConnector.class.getName(), true)
  	    .addInputParameter("variableName", "var")
  	    .addInputParameter("value", "timer")
  	.addDecisionNode("join")
  	.addJoinType(JoinType.AND)
  	.addTransition("start", "human")
    .addTransition("start", "timer")
  	.addTransition("human", "join")
  	.addTransition("timer", "join")
  	.done();

  	process = getManagementAPI().deploy(getBusinessArchive(process, null, SetVarConnector.class));
  	final ProcessDefinitionUUID processUUID = process.getUUID();
  	final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);
  	assertEquals(InstanceState.STARTED, getQueryRuntimeAPI().getProcessInstance(instanceUUID).getInstanceState());

  	Collection<TaskInstance> tasks = getQueryRuntimeAPI().getTaskList(ActivityState.READY);
  	assertEquals(1, tasks.size());
  	final TaskInstance human = tasks.iterator().next();
  	assertEquals("human", human.getActivityName());
  	getRuntimeAPI().executeTask(human.getUUID(), true);

  	this.waitForInstanceEnd(5000, 200, instanceUUID);
  	assertEquals(InstanceState.FINISHED, getQueryRuntimeAPI().getProcessInstance(instanceUUID).getInstanceState());

  	checkExecutedOnce(instanceUUID, "timer");
  	checkExecutedOnce(instanceUUID, "human");
  	assertEquals("timer", getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "var"));

  	getManagementAPI().deleteProcess(processUUID);
  }
  
  public void testSimpleTimerHumanWithConnectorOnFinish() throws Exception {
  	ProcessDefinition process = ProcessBuilder.createProcess("p", "d")
  	.addStringData("var")
  	.addHuman(getLogin())
  	.addSystemTask("start")
  	.addHumanTask("human", getLogin())
  	  .addConnector(Event.taskOnFinish, SetVarConnector.class.getName(), true)
  	    .addInputParameter("variableName", "var")
  	    .addInputParameter("value", "human")
  	.addTimerTask("timer", "${new Date(System.currentTimeMillis() + 10)}")
  	  .addConnector(Event.onTimer, SetVarConnector.class.getName(), true)
  	    .addInputParameter("variableName", "var")
  	    .addInputParameter("value", "timer")
  	.addDecisionNode("join")
  	.addJoinType(JoinType.AND)
  	.addTransition("start", "human")
    .addTransition("start", "timer")
  	.addTransition("human", "join")
  	.addTransition("timer", "join")
  	.done();

  	process = getManagementAPI().deploy(getBusinessArchive(process, null, SetVarConnector.class));
  	final ProcessDefinitionUUID processUUID = process.getUUID();
  	
  	final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);
  	
  	assertEquals(InstanceState.STARTED, getQueryRuntimeAPI().getProcessInstance(instanceUUID).getInstanceState());
  	
  	//wait the timer is executed so the connector is executed to
  	Thread.sleep(2000);
  	
  	Collection<TaskInstance> tasks = getQueryRuntimeAPI().getTaskList(ActivityState.READY);
  	assertEquals(1, tasks.size());
  	final TaskInstance human = tasks.iterator().next();
  	assertEquals("human", human.getActivityName());
  	getRuntimeAPI().executeTask(human.getUUID(), true);
  	
  	this.waitForInstanceEnd(5000, 10, instanceUUID);
  	
  	assertEquals(InstanceState.FINISHED, getQueryRuntimeAPI().getProcessInstance(instanceUUID).getInstanceState());
  	
  	checkExecutedOnce(instanceUUID, "timer");
  	checkExecutedOnce(instanceUUID, "human");
  	
  	assertEquals("human", getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "var"));
  			
  	getManagementAPI().deleteProcess(processUUID);
  }

  public void testCancelTimerWithAHugeCondition() throws Exception {
    String timerCondition = 
      "${new Date(System.currentTimeMillis() + 10 + 10 + 10 + 10 + 10 + 10 + 10 + 10+ 10 + 10 + 10 + 10 + 10 +"
     + " 10 + 10 + 10 + 10 + 10 + 10 + 10 + 10 + 10 + 10 + 10 + 10 + 10 + 10 + 10 + 10 + 10 + 10 + 10 + 10 + 10 + 10 + 10 + 10 + 10 +"
     + " 10 + 10 + 10 + 10 + 10 + 10 + 10 + 10 + 10 + 10 + 10 + 10 + 10 + 10 + 10 + 10 + 10 + 10 + 10 + 10 + 10 + 10 + 10 + 10)}"; 

    ProcessDefinition process = ProcessBuilder.createProcess("p", "d")
    .addHuman(getLogin())
    .addSystemTask("start")
    .addHumanTask("human", getLogin())
    .addTimerTask("timer", timerCondition)
    .addDecisionNode("join")
    .addJoinType(JoinType.XOR)
    .addTransition("start", "human")
    .addTransition("start", "timer")
    .addTransition("human", "join")
    .addTransition("timer", "join")
    .done();

    process = getManagementAPI().deploy(getBusinessArchive(process));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);
    getRuntimeAPI().cancelProcessInstance(instanceUUID);
    assertEquals(InstanceState.CANCELLED, getQueryRuntimeAPI().getProcessInstance(instanceUUID).getInstanceState());
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testStartAProcessOnce() throws Exception {
    long now = System.currentTimeMillis();
    ProcessDefinition process =
      ProcessBuilder.createProcess("itSTime", "1.0")
      .addHuman(getLogin())
      .addTimerTask("InASecond", "${new Date(" + now + "+ 1000)}")
      .addHumanTask("step1", getLogin())
      .addTransition("InASecond", "step1")
      .done();
    process = getManagementAPI().deploy(getBusinessArchive(process));

    Thread.sleep(1500);
    Set<LightProcessInstance> instances = getQueryRuntimeAPI().getLightProcessInstances();
    assertEquals(1, instances.size());
    LightProcessInstance instance = instances.iterator().next();
    assertEquals("SYSTEM", instance.getStartedBy());
    assertTrue(new Date(now + 100).getTime() <= instance.getStartedDate().getTime());

    Thread.sleep(1500);
    instances = getQueryRuntimeAPI().getLightProcessInstances();
    assertEquals(1, instances.size());
    instance = instances.iterator().next();
    assertEquals("SYSTEM", instance.getStartedBy());
    assertTrue(new Date(now + 100).getTime() <= instance.getStartedDate().getTime());

    getManagementAPI().deleteProcess(process.getUUID());
  }

  public void testStartProcess() throws Exception {
    ProcessDefinition process =
      ProcessBuilder.createProcess("itSTime", "1.1")
      .addHuman(getLogin())
      .addTimerTask("every_5_seconds", "${new Date(" + BonitaConstants.TIMER_LAST_EXECUTION + " + 5000)}")
      .addHumanTask("step1", getLogin())
      .addTransition("every_5_seconds", "step1")
      .done();

    process = getManagementAPI().deploy(getBusinessArchive(process));

    Thread.sleep(12000);
    Set<LightProcessInstance> instances = getQueryRuntimeAPI().getLightProcessInstances();
    assertEquals(2, instances.size());
    getManagementAPI().deleteProcess(process.getUUID());
  }

  public void testDeleteAProcessUsingATimer() throws Exception {
    ProcessDefinition process =
      ProcessBuilder.createProcess("itSTime", "1.2")
      .addHuman(getLogin())
      .addTimerTask("every_10_seconds", "${new Date(timerLastExecution + 10000)}")
      .addHumanTask("step1", getLogin())
      .addTransition("every_10_seconds", "step1")
      .done();

    process = getManagementAPI().deploy(getBusinessArchive(process));
    Set<LightProcessInstance> instances = getQueryRuntimeAPI().getLightProcessInstances();
    assertEquals(0, instances.size());
    getManagementAPI().deleteProcess(process.getUUID());
  }

  public void testStartAProcessUsingTwoStartEvents() throws Exception {
    ProcessDefinition process =
      ProcessBuilder.createProcess("itSTime", "1.3")
      .addHuman(getLogin())
      .addTimerTask("every_second", "${new Date(timerLastExecution + 2000)}")
      .addHumanTask("step1", getLogin())
      .addSystemTask("start")
      .addDecisionNode("join")
        .addJoinType(JoinType.XOR)
      .addTransition("every_second", "join")
      .addTransition("start", "join")
      .addTransition("join", "step1")
      .done();

    process = getManagementAPI().deploy(getBusinessArchive(process));
    Set<LightProcessInstance> instances = getQueryRuntimeAPI().getLightProcessInstances();
    assertEquals(0, instances.size());

    waitForStartingInstance(3000, 50, process.getUUID());

    getRuntimeAPI().instantiateProcess(process.getUUID());
    instances = getQueryRuntimeAPI().getLightProcessInstances();
    assertEquals(2, instances.size());

    getManagementAPI().deleteProcess(process.getUUID());
  }

  public void testCancelAProcessUsingAStartTimerEvent() throws Exception {
    ProcessDefinition process =
      ProcessBuilder.createProcess("CancelitSTime", "1.0")
      .addHuman(getLogin())
      .addTimerTask("every_second", "${new Date(timerLastExecution + 100000)}")
      .addHumanTask("step1", getLogin())
      .addTransition("every_second", "step1")
      .done();
   
    process = getManagementAPI().deploy(getBusinessArchive(process));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);
    Set<LightActivityInstance> activities = getQueryRuntimeAPI().getLightActivityInstances(instanceUUID);
    assertEquals(2, activities.size());

    Set<LightTaskInstance> tasks = getQueryRuntimeAPI().getLightTasks(instanceUUID);
    assertEquals(1, tasks.size());
    LightTaskInstance task = tasks.iterator().next();
    assertEquals("step1", task.getActivityName());

    getRuntimeAPI().cancelProcessInstance(instanceUUID);
    assertEquals(InstanceState.CANCELLED, getQueryRuntimeAPI().getProcessInstance(instanceUUID).getInstanceState());
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testBoundaryEventTest() throws Exception {
    ProcessDefinition process = ProcessBuilder.createProcess("boundaryTimer", "1.0")
    .addHuman(getLogin())
    .addHumanTask("step1", getLogin())
      .addTimerBoundaryEvent("In_5_seconds", "5000")
    .addHumanTask("normal", getLogin())
    .addHumanTask("exception", getLogin())
    .addTransition("step1", "normal")
    .addExceptionTransition("step1", "In_5_seconds", "exception")
    .done();

    BusinessArchive archive = getBusinessArchive(process);
    process = getManagementAPI().deploy(archive);
    final ProcessDefinitionUUID processUUID = process.getUUID();

    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);
    Set<LightTaskInstance> tasks = getQueryRuntimeAPI().getLightTasks(instanceUUID);
    assertEquals(1, tasks.size());
    LightTaskInstance task = tasks.iterator().next();
    assertEquals("step1", task.getActivityName());

    Thread.sleep(8000);
    tasks = getQueryRuntimeAPI().getLightTasks(instanceUUID);
    for (LightTaskInstance lightTaskInstance : tasks) {
      System.out.println(lightTaskInstance);
    }

    assertEquals(2, tasks.size());
//    task = tasks.iterator().next();
//    assertEquals("exception", task.getActivityName());

    getManagementAPI().deleteProcess(processUUID);
  }

  public void testDeleteBoundaryEventTest() throws Exception {
    ProcessDefinition process = ProcessBuilder.createProcess("boundaryTimer", "1.1")
    .addHuman(getLogin())
    .addHumanTask("step1", getLogin())
      .addTimerBoundaryEvent("In_a_minute", "5000")
    .addHumanTask("normal", getLogin())
    .addHumanTask("exception", getLogin())
    .addTransition("step1", "normal")
    .addExceptionTransition("step1", "In_a_minute", "exception")
    .done();

    BusinessArchive archive = getBusinessArchive(process);
    process = getManagementAPI().deploy(archive);
    final ProcessDefinitionUUID processUUID = process.getUUID();

    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);
    Set<LightTaskInstance> tasks = getQueryRuntimeAPI().getLightTasks(instanceUUID);
    assertEquals(1, tasks.size());
    LightTaskInstance task = tasks.iterator().next();
    assertEquals("step1", task.getActivityName());
    executeTask(instanceUUID, "step1");

    getManagementAPI().deleteProcess(processUUID);
  }

  public void testExecuteTimerBoundaryEventOnASubFlow() throws Exception {
    ProcessDefinition subflow = ProcessBuilder.createProcess("sub", "atomic")
    .addHuman(getLogin())
    .addHumanTask("subStep", getLogin())
    .done();

    ProcessDefinition mainflow = ProcessBuilder.createProcess("main", "frame")
    .addHuman(getLogin())
    .addSubProcess("step1", "sub")
      .addTimerBoundaryEvent("In_5_seconds", "5000")
    .addHumanTask("normalStep", getLogin())
    .addHumanTask("exceptionStep", getLogin())
    .addTransition("step1", "normalStep")
    .addExceptionTransition("step1", "In_5_seconds", "exceptionStep")
    .done();

    subflow = getManagementAPI().deploy(getBusinessArchive(subflow));
    mainflow = getManagementAPI().deploy(getBusinessArchive(mainflow));

    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(mainflow.getUUID());
    final ProcessInstanceUUID subflowInstanceUUID = getQueryRuntimeAPI().getLightProcessInstances(subflow.getUUID()).iterator().next().getUUID();
   
    final Set<LightActivityInstance> parentActivities = getQueryRuntimeAPI().getLightActivityInstances(instanceUUID);
    assertEquals(1, parentActivities.size());
    LightActivityInstance activity = parentActivities.iterator().next();
    assertEquals("step1", activity.getActivityName());
    assertEquals(ActivityState.EXECUTING, activity.getState());
   
    final Set<LightActivityInstance> subflowActivities = getQueryRuntimeAPI().getLightActivityInstances(subflowInstanceUUID);
    assertEquals(1, subflowActivities.size());
    LightActivityInstance subActivity = subflowActivities.iterator().next();
    assertEquals("subStep", subActivity.getActivityName());
    assertEquals(ActivityState.READY, subActivity.getState());

    Thread.sleep(6000);
    Set<LightTaskInstance> tasks = getQueryRuntimeAPI().getLightTasks(instanceUUID);
    assertEquals(1, tasks.size());
    LightTaskInstance task = tasks.iterator().next();
    assertEquals("exceptionStep", task.getActivityName());
   
    assertEquals(0, getQueryRuntimeAPI().getLightTaskList(subflowInstanceUUID, ActivityState.READY).size());
    assertEquals(ActivityState.ABORTED, getQueryRuntimeAPI().getLightActivityInstances(instanceUUID, "step1").iterator().next().getState());
    assertEquals(InstanceState.ABORTED, getQueryRuntimeAPI().getLightProcessInstance(subflowInstanceUUID).getInstanceState());
    assertEquals(ActivityState.ABORTED, getQueryRuntimeAPI().getLightActivityInstances(subflowInstanceUUID, "subStep").iterator().next().getState());

    getManagementAPI().deleteProcess(mainflow.getUUID());
    getManagementAPI().deleteProcess(subflow.getUUID());
  }

  public void testExecuteTimerBoundaryEventOnASubFlowUsingATimer() throws Exception {
    ProcessDefinition subflow = ProcessBuilder.createProcess("sub", "atomic")
    .addHuman(getLogin())
    .addSystemTask("end")
    .addHumanTask("subStep", getLogin())
      .addTimerBoundaryEvent("In_a_minute", "60000")
    .addExceptionTransition("subStep", "In_a_minute", "end")
    .done();

    ProcessDefinition mainflow = ProcessBuilder.createProcess("main", "frame")
    .addHuman(getLogin())
    .addSubProcess("step1", "sub")
      .addTimerBoundaryEvent("In_5_seconds", "5000")
    .addHumanTask("normalStep", getLogin())
    .addHumanTask("exceptionStep", getLogin())
    .addTransition("step1", "normalStep")
    .addExceptionTransition("step1", "In_5_seconds", "exceptionStep")
    .done();

    subflow = getManagementAPI().deploy(getBusinessArchive(subflow));
    mainflow = getManagementAPI().deploy(getBusinessArchive(mainflow));

    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(mainflow.getUUID());
    final ProcessInstanceUUID subflowInstanceUUID = getQueryRuntimeAPI().getLightProcessInstances(subflow.getUUID()).iterator().next().getUUID();
   
    final Set<LightActivityInstance> parentActivities = getQueryRuntimeAPI().getLightActivityInstances(instanceUUID);
    assertEquals(1, parentActivities.size());
    LightActivityInstance activity = parentActivities.iterator().next();
    assertEquals("step1", activity.getActivityName());
    assertEquals(ActivityState.EXECUTING, activity.getState());
   
    final Set<LightActivityInstance> subflowActivities = getQueryRuntimeAPI().getLightActivityInstances(subflowInstanceUUID);
    assertEquals(1, subflowActivities.size());
    LightActivityInstance subActivity = subflowActivities.iterator().next();
    assertEquals("subStep", subActivity.getActivityName());
    assertEquals(ActivityState.READY, subActivity.getState());

    Thread.sleep(6000);
    Set<LightTaskInstance> tasks = getQueryRuntimeAPI().getLightTasks(instanceUUID);
    assertEquals(1, tasks.size());
    LightTaskInstance task = tasks.iterator().next();
    assertEquals("exceptionStep", task.getActivityName());
   
    assertEquals(0, getQueryRuntimeAPI().getLightTaskList(subflowInstanceUUID, ActivityState.READY).size());
    assertEquals(ActivityState.ABORTED, getQueryRuntimeAPI().getLightActivityInstances(instanceUUID, "step1").iterator().next().getState());
    assertEquals(InstanceState.ABORTED, getQueryRuntimeAPI().getLightProcessInstance(subflowInstanceUUID).getInstanceState());
    assertEquals(ActivityState.ABORTED, getQueryRuntimeAPI().getLightActivityInstances(subflowInstanceUUID, "subStep").iterator().next().getState());

    getManagementAPI().deleteProcess(mainflow.getUUID());
    getManagementAPI().deleteProcess(subflow.getUUID());
  }

  public void testExecuteTimerBoundaryEventOnASubFlowUsingATimer2() throws Exception {
    ProcessDefinition subflow = ProcessBuilder.createProcess("sub", "atomic")
    .addHuman(getLogin())
    .addSystemTask("end")
    .addHumanTask("subStep", getLogin())
      .addTimerBoundaryEvent("In_a_second", "1000")
    .addExceptionTransition("subStep", "In_a_second", "end")
    .done();

    ProcessDefinition mainflow = ProcessBuilder.createProcess("main", "frame")
    .addHuman(getLogin())
    .addSubProcess("step1", "sub")
      .addTimerBoundaryEvent("In_5_seconds", "5000")
    .addHumanTask("normalStep", getLogin())
    .addHumanTask("exceptionStep", getLogin())
    .addTransition("step1", "normalStep")
    .addExceptionTransition("step1", "In_5_seconds", "exceptionStep")
    .done();

    subflow = getManagementAPI().deploy(getBusinessArchive(subflow));
    mainflow = getManagementAPI().deploy(getBusinessArchive(mainflow));

    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(mainflow.getUUID());
    final ProcessInstanceUUID subflowInstanceUUID = getQueryRuntimeAPI().getLightProcessInstances(subflow.getUUID()).iterator().next().getUUID();
   
    final Set<LightActivityInstance> parentActivities = getQueryRuntimeAPI().getLightActivityInstances(instanceUUID);
    assertEquals(1, parentActivities.size());
    LightActivityInstance activity = parentActivities.iterator().next();
    assertEquals("step1", activity.getActivityName());
    assertEquals(ActivityState.EXECUTING, activity.getState());
   
    final Set<LightActivityInstance> subflowActivities = getQueryRuntimeAPI().getLightActivityInstances(subflowInstanceUUID);
    assertEquals(1, subflowActivities.size());
    LightActivityInstance subActivity = subflowActivities.iterator().next();
    assertEquals("subStep", subActivity.getActivityName());
    assertEquals(ActivityState.READY, subActivity.getState());

    Thread.sleep(6000);
    Set<LightTaskInstance> tasks = getQueryRuntimeAPI().getLightTasks(instanceUUID);
    assertEquals(1, tasks.size());
    LightTaskInstance task = tasks.iterator().next();
    assertEquals("normalStep", task.getActivityName());

    assertEquals(0, getQueryRuntimeAPI().getLightTaskList(subflowInstanceUUID, ActivityState.READY).size());
    assertEquals(ActivityState.FINISHED, getQueryRuntimeAPI().getLightActivityInstances(instanceUUID, "step1").iterator().next().getState());
    assertEquals(InstanceState.FINISHED, getQueryRuntimeAPI().getLightProcessInstance(subflowInstanceUUID).getInstanceState());
    assertEquals(ActivityState.ABORTED, getQueryRuntimeAPI().getLightActivityInstances(subflowInstanceUUID, "subStep").iterator().next().getState());

    getManagementAPI().deleteProcess(mainflow.getUUID());
    getManagementAPI().deleteProcess(subflow.getUUID());
  }

  public void testStartTimerContinuesEventIfTheInstanceFails() throws Exception {
    final String script =
      "import org.ow2.bonita.facade.ManagementAPI;" +
    	"import org.ow2.bonita.facade.impl.StandardAPIAccessorImpl;" +
    	"StandardAPIAccessorImpl accessor = new StandardAPIAccessorImpl();" +
    	"ManagementAPI managementAPI = accessor.getManagementAPI();" +
    	"def count = managementAPI.getMetaData(\"count\");" +
    	"def number = Integer.parseInt(count);" +
    	"if (number == 0) {" +
    	"  return 1 / 0;" +
    	"} else {" +
    	"  return 0;" +
    	"}";

    getManagementAPI().addMetaData("count", "0");
    ProcessDefinition process =
      ProcessBuilder.createProcess("stillContinue", "1.0")
      .addHuman(getLogin())
      .addStringData("vars", "null")
      .addTimerTask("every_second", "${random = new Random(); return new Date(" + BonitaConstants.TIMER_LAST_EXECUTION + "- random.nextInt(200-100+1)+100)}")
      .addSystemTask("system")
        .addConnector(Event.automaticOnEnter, GroovyConnector.class.getName(), true)
          .addInputParameter("script", script)
      .addHumanTask("next", getLogin())
      .addTransition("every_second", "system")
      .addTransition("system", "next")
      .done();

    process = getManagementAPI().deploy(getBusinessArchive(process, null, GroovyConnector.class));
    Set<LightProcessInstance> instances = getQueryRuntimeAPI().getLightProcessInstances();
    assertEquals(0, instances.size());

    Thread.sleep(3000);
    getQueryRuntimeAPI().getLightProcessInstances();
    assertEquals(0, instances.size());
    getManagementAPI().addMetaData("count", "1");
    Thread.sleep(3000);

    instances = getQueryRuntimeAPI().getLightProcessInstances();
    assertTrue(instances.size() > 0);

    getManagementAPI().deleteProcess(process.getUUID());
  }

  public void testStartAProcessXTimes() throws Exception {
    ProcessDefinition process =
      ProcessBuilder.createProcess("itSTime", "1.0")
      .addHuman(getLogin())
      .addTimerTask("InASecond", "${random = new Random(); return new Date(" + BonitaConstants.TIMER_LAST_EXECUTION + "- random.nextInt(200-100+1)+100)}")
      .addHumanTask("step1", getLogin())
      .addTransition("InASecond", "step1")
      .done();
    process = getManagementAPI().deploy(getBusinessArchive(process));

    Thread.sleep(1500);
    Set<LightProcessInstance> instances = getQueryRuntimeAPI().getLightProcessInstances();
    assertTrue(instances.size() > 1);

    getManagementAPI().deleteProcess(process.getUUID());
  }

}
