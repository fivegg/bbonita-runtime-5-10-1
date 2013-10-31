package org.ow2.bonita.activity;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.bonitasoft.connectors.bonita.SetVarConnector;
import org.bonitasoft.connectors.bonita.joincheckers.FixedNumberJoinChecker;
import org.ow2.bonita.APITestCase;
import org.ow2.bonita.activity.multipleinstances.instantiator.NoContextMulitpleActivitiesInstantiator;
import org.ow2.bonita.facade.RepairAPITest.SimpleMultiInstantiator;
import org.ow2.bonita.facade.def.element.BusinessArchive;
import org.ow2.bonita.facade.def.element.HookDefinition.Event;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.runtime.ActivityInstance;
import org.ow2.bonita.facade.runtime.ActivityState;
import org.ow2.bonita.facade.runtime.InstanceState;
import org.ow2.bonita.facade.runtime.ProcessInstance;
import org.ow2.bonita.facade.runtime.TaskInstance;
import org.ow2.bonita.facade.uuid.ActivityInstanceUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.util.ProcessBuilder;

public class LoopActivityTest extends APITestCase {

  //checks the correct execution is reused when going back to parent after a loop
  public void testExecutionBackToParent() throws Exception {
    ProcessDefinition process =
      ProcessBuilder.createProcess("iterLoop", "2.1")
      .addHuman(getLogin())
      .addStringData("msg", "any")
      .addSystemTask("init")
      .addHumanTask("a1", getLogin())
        .addLoop("msg != 'go'", true)
      .addHumanTask("b", getLogin())
      .addHumanTask("a2", getLogin())
      .addTerminateEndEvent("end")
      .addTransition("init", "a1")
      .addTransition("init", "b")
      .addTransition("a1", "a2")
      .addTransition("a2", "end")
      .done();

    process = getManagementAPI().deploy(getBusinessArchive(process));
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(process.getUUID());

    executeTask(instanceUUID, "a1");
    getRuntimeAPI().setProcessInstanceVariable(instanceUUID, "msg", "go");
    executeTask(instanceUUID, "a1");
    
    executeTask(instanceUUID, "a2");
    
    assertEquals(InstanceState.FINISHED, getQueryRuntimeAPI().getLightProcessInstance(instanceUUID).getInstanceState());

    getManagementAPI().deleteProcess(process.getUUID());
  }
  
  public void testActivityStateOfLoop() throws Exception {
    ProcessDefinition definition = 
      ProcessBuilder.createProcess("loopState", "1.0")
      .addIntegerData("counter", 0)
      .addHuman(getLogin())
      .addHumanTask("loop", getLogin())
        .addLoop("counter != 1", true)
      .addSystemTask("end")
      .addTransition("loop", "end")
      .done();

    definition = getManagementAPI().deploy(getBusinessArchive(definition));

    Collection<TaskInstance> tasks = null;
    TaskInstance looptask = null;
    
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(definition.getUUID());
    
    tasks = getQueryRuntimeAPI().getTaskList(instanceUUID, ActivityState.READY);
    assertEquals(1, tasks.size());
    looptask = tasks.iterator().next();
    
    assertEquals("loop", looptask.getActivityName());
    getRuntimeAPI().setProcessInstanceVariable(instanceUUID, "counter", 1);
    getRuntimeAPI().executeTask(looptask.getUUID(), true);

    tasks = getQueryRuntimeAPI().getTaskList(instanceUUID, ActivityState.READY);
    assertEquals(0, tasks.size());
    
    Set<ActivityInstance> loppActivities = getQueryRuntimeAPI().getActivityInstances(instanceUUID, "loop");
    assertEquals(1, loppActivities.size());
    final ActivityInstance loopActivity = loppActivities.iterator().next();
    assertEquals(ActivityState.FINISHED, loopActivity.getState());
    
    assertEquals(InstanceState.FINISHED, getQueryRuntimeAPI().getLightProcessInstance(instanceUUID).getInstanceState());
    
    getManagementAPI().deleteProcess(definition.getUUID());
  }
  
  public void testConnectorInALoopHumanTask() throws Exception {
    ProcessDefinition definition = 
      ProcessBuilder.createProcess("loopWithConnector", "1.0")
      .addIntegerData("counter", 0)
      .addIntegerData("enterCounter", 0)
      .addIntegerData("finishCounter" , 0)
      .addHuman(getLogin())
      .addHumanTask("loop", getLogin())
        .addLoop("counter != 1", true)
        .addConnector(Event.taskOnReady, SetVarConnector.class.getName(), true)
          .addInputParameter("variableName", "enterCounter")
          .addInputParameter("value", "${enterCounter + 1}")
        .addConnector(Event.taskOnFinish, SetVarConnector.class.getName(), true)
          .addInputParameter("variableName", "finishCounter")
          .addInputParameter("value", "${finishCounter + 1}")
      .addSystemTask("end")
      .addTransition("loop", "end")
      .done();

    definition = getManagementAPI().deploy(getBusinessArchive(definition, null, SetVarConnector.class));

    Collection<TaskInstance> tasks = null;
    TaskInstance looptask = null;
    
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(definition.getUUID());
    
    final int numberOfLoops = 3;
    for (int i = 0; i < numberOfLoops; i++) {
      tasks = getQueryRuntimeAPI().getTaskList(instanceUUID, ActivityState.READY);
      assertEquals(1, tasks.size());
      looptask = tasks.iterator().next();
      
      assertEquals("loop", looptask.getActivityName());
      if(i == numberOfLoops-1) {
        getRuntimeAPI().setProcessInstanceVariable(instanceUUID, "counter", 1);
      }
      
      int enterCounter = (Integer)getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "enterCounter");
      assertEquals(i + 1, enterCounter);
      
      getRuntimeAPI().executeTask(looptask.getUUID(), true);
      
      int finishCounter = (Integer)getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "finishCounter");
      assertEquals(i+1, finishCounter);
    }
    int enterCounter = (Integer)getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "enterCounter");
    assertEquals(3, enterCounter);
    int finishCounter = (Integer)getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "finishCounter");
    assertEquals(3, finishCounter);
    
    tasks = getQueryRuntimeAPI().getTaskList(instanceUUID, ActivityState.READY);
    assertEquals(0, tasks.size());
    
    Set<ActivityInstance> loppActivities = getQueryRuntimeAPI().getActivityInstances(instanceUUID, "loop");
    assertEquals(3, loppActivities.size());
    for (final ActivityInstance activityInstance : loppActivities) {
      assertEquals(ActivityState.FINISHED, activityInstance.getState());
    }
    
    assertEquals(InstanceState.FINISHED, getQueryRuntimeAPI().getLightProcessInstance(instanceUUID).getInstanceState());
    
    getManagementAPI().deleteProcess(definition.getUUID());
  }
  

  public void testConnectorInALoopSytemTask() throws Exception {
    ProcessDefinition definition = 
      ProcessBuilder.createProcess("loopWithConnector", "1.0")
      .addIntegerData("enterCounter", 0)
      .addIntegerData("finishCounter" , 0)
      .addHuman(getLogin())
      .addSystemTask("loop")
        .addLoop("finishCounter != 4", true, "${3}")
        .addConnector(Event.automaticOnEnter, SetVarConnector.class.getName(), true)
          .addInputParameter("variableName", "enterCounter")
          .addInputParameter("value", "${enterCounter + 1}")
        .addConnector(Event.automaticOnExit, SetVarConnector.class.getName(), true)
          .addInputParameter("variableName", "finishCounter")
          .addInputParameter("value", "${finishCounter + 1}")
      .addSystemTask("end")
      .addTransition("loop", "end")
      .done();

    definition = getManagementAPI().deploy(getBusinessArchive(definition, null, SetVarConnector.class));

    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(definition.getUUID());
    
    int enterCounter = (Integer)getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "enterCounter");
    assertEquals(3, enterCounter);
    int finishCounter = (Integer)getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "finishCounter");
    assertEquals(3, finishCounter);
    
    Set<ActivityInstance> loppActivities = getQueryRuntimeAPI().getActivityInstances(instanceUUID, "loop");
    assertEquals(3, loppActivities.size());
    for (final ActivityInstance activityInstance : loppActivities) {
      assertEquals(ActivityState.FINISHED, activityInstance.getState());
    }
    
    assertEquals(InstanceState.FINISHED, getQueryRuntimeAPI().getLightProcessInstance(instanceUUID).getInstanceState());
    
    getManagementAPI().deleteProcess(definition.getUUID());
  }
  
  public void testActivityStateBeforeExecutionOfLoopWithMaxLoopCondition() throws Exception {
    ProcessDefinition definition = 
      ProcessBuilder.createProcess("loopStateMax", "1.0")
      .addSystemTask("loop")
        .addLoop("true", true, "2")
      .addSystemTask("end")
      .addTransition("loop", "end")
      .done();

    definition = getManagementAPI().deploy(getBusinessArchive(definition));

    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(definition.getUUID());
    assertEquals(InstanceState.FINISHED, getQueryRuntimeAPI().getLightProcessInstance(instanceUUID).getInstanceState());
    
    final Set<ActivityInstance> loopActivities = getQueryRuntimeAPI().getActivityInstances(instanceUUID, "loop");
    assertEquals(2, loopActivities.size());
    
    for (ActivityInstance activity : loopActivities) {
      assertEquals(ActivityState.FINISHED, activity.getState());  
    }
    
    getManagementAPI().deleteProcess(definition.getUUID());
  }
  
  public void testActivityStateAfterExecutionOfLoopWithMaxLoopCondition() throws Exception {
    ProcessDefinition definition = 
      ProcessBuilder.createProcess("loopStateMax", "1.0")
      .addSystemTask("loop")
        .addLoop("true", false, "2")
      .addSystemTask("end")
      .addTransition("loop", "end")
      .done();

    definition = getManagementAPI().deploy(getBusinessArchive(definition));

    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(definition.getUUID());
    assertEquals(InstanceState.FINISHED, getQueryRuntimeAPI().getLightProcessInstance(instanceUUID).getInstanceState());
    
    final Set<ActivityInstance> loopActivities = getQueryRuntimeAPI().getActivityInstances(instanceUUID, "loop");
    assertEquals(2, loopActivities.size());
    
    for (ActivityInstance activity : loopActivities) {
      assertEquals(ActivityState.FINISHED, activity.getState());  
    }
    
    getManagementAPI().deleteProcess(definition.getUUID());
  }
  
  public void testNoTaskLoop() throws Exception {
    ProcessDefinition definition = 
      ProcessBuilder.createProcess("loop", "in")
      .addIntegerData("counter", 2)
      .addSystemTask("loop")
        .addLoop("counter < 0", true)
      .addHuman(getLogin())
      .addHumanTask("result", getLogin())
      .addTransition("loop", "result")
      .done();

    definition = getManagementAPI().deploy(getBusinessArchive(definition));

    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(definition.getUUID());
    Set<ActivityInstance> activities = getQueryRuntimeAPI().getActivityInstances(instanceUUID);
    assertEquals(1, activities.size());
    Collection<TaskInstance> tasks = getQueryRuntimeAPI().getTaskList(instanceUUID, ActivityState.READY);
    assertEquals(1, tasks.size());
    assertEquals("result", tasks.iterator().next().getActivityName());

    getManagementAPI().deleteProcess(definition.getUUID());
  }
  
  public void testSystemTaskBeforeLoop() throws Exception {
    ProcessDefinition definition = 
      ProcessBuilder.createProcess("loop", "in")
      .addIntegerData("counter", 0)
      .addSystemTask("test1")
      .addSystemTask("loop")
        .addLoop("counter < 10", true)
        .addConnector(Event.automaticOnEnter, SetVarConnector.class.getName(), true)
          .addInputParameter("variableName", "counter")
          .addInputParameter("value", "${++counter}")
       .addSystemTask("test2")
       .addTransition("test1", "loop")
       .addTransition("loop", "test2")
      .done();

    definition = getManagementAPI().deploy(getBusinessArchive(definition, null, SetVarConnector.class));

    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(definition.getUUID());
    Integer counter = (Integer) getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "counter");
    assertEquals(Integer.valueOf(10), counter);
    
    Set<ActivityInstance> activities = getQueryRuntimeAPI().getActivityInstances(instanceUUID, "loop");
    assertEquals(10, activities.size());
    
    activities = getQueryRuntimeAPI().getActivityInstances(instanceUUID);
    assertEquals(12, activities.size());

    getManagementAPI().deleteProcess(definition.getUUID());
  }

  public void testSystemTaskAfterLoop() throws Exception {
    ProcessDefinition definition = 
      ProcessBuilder.createProcess("loop", "in")
      .addIntegerData("counter", 0)
      .addSystemTask("loop")
        .addLoop("counter < 10", false)
        .addConnector(Event.automaticOnEnter, SetVarConnector.class.getName(), true)
          .addInputParameter("variableName", "counter")
          .addInputParameter("value", "${++counter}")
       .addHuman(getLogin())
       .addHumanTask("result", getLogin())
       .addTransition("loop", "result")
      .done();

    definition = getManagementAPI().deploy(getBusinessArchive(definition, null, SetVarConnector.class));

    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(definition.getUUID());
    Integer counter = (Integer) getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "counter");
    assertEquals(Integer.valueOf(10), counter);
    
    Set<ActivityInstance> activities = getQueryRuntimeAPI().getActivityInstances(instanceUUID);
    assertEquals(11, activities.size());

    getManagementAPI().deleteProcess(definition.getUUID());
  }
  
  public void testSystemTaskAfterLoopWithMax() throws Exception {
    ProcessDefinition definition = 
      ProcessBuilder.createProcess("loop", "in")
      .addIntegerData("counter", 0)
      .addSystemTask("loop")
        .addLoop("counter < 10", false, "${5}")
        .addConnector(Event.automaticOnEnter, SetVarConnector.class.getName(), true)
          .addInputParameter("variableName", "counter")
          .addInputParameter("value", "${++counter}")
       .addHuman(getLogin())
       .addHumanTask("result", getLogin())
       .addTransition("loop", "result")
      .done();

    definition = getManagementAPI().deploy(getBusinessArchive(definition, null, SetVarConnector.class));

    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(definition.getUUID());
    Integer counter = (Integer) getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "counter");
    assertEquals(Integer.valueOf(5), counter);
    
    Set<ActivityInstance> activities = getQueryRuntimeAPI().getActivityInstances(instanceUUID);
    assertEquals(6, activities.size());

    getManagementAPI().deleteProcess(definition.getUUID());
  }
  
  public void testSystemTaskAfterLoopWithMaxInt() throws Exception {
    ProcessDefinition definition = 
      ProcessBuilder.createProcess("loop", "in")
      .addIntegerData("counter", 0)
      .addSystemTask("loop")
        .addLoop("counter < 10", false, "5")
        .addConnector(Event.automaticOnEnter, SetVarConnector.class.getName(), true)
          .addInputParameter("variableName", "counter")
          .addInputParameter("value", "${++counter}")
       .addHuman(getLogin())
       .addHumanTask("result", getLogin())
       .addTransition("loop", "result")
      .done();

    definition = getManagementAPI().deploy(getBusinessArchive(definition, null, SetVarConnector.class));

    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(definition.getUUID());
    Integer counter = (Integer) getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "counter");
    assertEquals(Integer.valueOf(5), counter);
    
    Set<ActivityInstance> activities = getQueryRuntimeAPI().getActivityInstances(instanceUUID);
    assertEquals(6, activities.size());

    getManagementAPI().deleteProcess(definition.getUUID());
  }

  public void testHumanTaskMadLoop() throws Exception {
    ProcessDefinition definition = 
      ProcessBuilder.createProcess("madloop", "in")
      .addBooleanData("next", false)
      .addHumanTask("loop", getLogin())
        .addLoop("!next", false)
      .addHuman(getLogin())
      .done();

    definition = getManagementAPI().deploy(getBusinessArchive(definition));

    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(definition.getUUID());
    Boolean next = (Boolean) getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "next");
    assertFalse(next);
    Set<ActivityInstanceUUID> activiyUUIDs = new HashSet<ActivityInstanceUUID>();
    Collection<TaskInstance> tasks = getQueryRuntimeAPI().getTaskList(instanceUUID, ActivityState.READY);
    assertEquals(1, tasks.size());
    TaskInstance task = tasks.iterator().next();
    activiyUUIDs.add(task.getUUID());

    executeTask(instanceUUID, "loop");
    tasks = getQueryRuntimeAPI().getTaskList(instanceUUID, ActivityState.READY);
    assertEquals(1, tasks.size());
    task = tasks.iterator().next();
    assertFalse(activiyUUIDs.contains(task.getUUID()));

    executeTask(instanceUUID, "loop");
    tasks = getQueryRuntimeAPI().getTaskList(instanceUUID, ActivityState.READY);
    assertEquals(1, tasks.size());
    task = tasks.iterator().next();
    assertFalse(activiyUUIDs.contains(task.getUUID()));

    getRuntimeAPI().startTask(task.getUUID(), true);
    getRuntimeAPI().setProcessInstanceVariable(instanceUUID, "next", true);
    getRuntimeAPI().finishTask(task.getUUID(), true);

    tasks = getQueryRuntimeAPI().getTaskList(instanceUUID, ActivityState.READY);
    assertEquals(0, tasks.size());

    getManagementAPI().deleteProcess(definition.getUUID());
  }

  //test subprocess
  private ProcessDefinition buildSimpleProcess() throws Exception {
    ProcessDefinition simpleProcess = ProcessBuilder.createProcess("sub_process", "1.0")
    .addIntegerData("counter")
    .addHuman("admin")
    .addHumanTask("task1", "admin")
    .addHumanTask("task2", "admin")
      .addConnector(Event.taskOnFinish, SetVarConnector.class.getName(), true)
      .addInputParameter("variableName", "counter")
      .addInputParameter("value", "${++counter}")
    .addTransition("transition1", "task1", "task2")
    .done();
    
    BusinessArchive businessArchive = getBusinessArchive(simpleProcess, null, SetVarConnector.class);
    return getManagementAPI().deploy(businessArchive);
  }
  
  private ProcessDefinition buildSimpleAutomaticProcess() throws Exception {
    ProcessDefinition simpleProcess = ProcessBuilder.createProcess("sub_process", "1.0")
    .addIntegerData("counter")
    .addSystemTask("task1")
    .addSystemTask("task2")
      .addConnector(Event.automaticOnExit, SetVarConnector.class.getName(), true)
      .addInputParameter("variableName", "counter")
      .addInputParameter("value", "${++counter}")
    .addTransition("transition1", "task1", "task2")
    .done();
    
    BusinessArchive businessArchive = getBusinessArchive(simpleProcess, null, SetVarConnector.class);
    return getManagementAPI().deploy(businessArchive);
  }
  
  private ProcessDefinition buildProcessWithSubProcessBeforeLoop() throws Exception {
    ProcessDefinition simpleProcess = ProcessBuilder.createProcess("process_with_sub_process", "1.0")
    .addHuman("admin")
    .addIntegerData("counter", 0)
    .addSubProcess("subprocess", "sub_process")
      .addSubProcessInParameter("counter", "counter")
      .addSubProcessOutParameter("counter", "counter")
      .addLoop("counter < 10", true)
    .addHumanTask("task3", "admin")
    .addTransition("transition2", "subprocess", "task3")
    .done();
    
    BusinessArchive businessArchive = getBusinessArchive(simpleProcess, null, SimpleMultiInstantiator.class);
    return getManagementAPI().deploy(businessArchive);
  }
  
  private ProcessDefinition buildProcessWithSubProcessAfterLoop() throws Exception {
    ProcessDefinition simpleProcess = ProcessBuilder.createProcess("process_with_sub_process", "1.0")
    .addHuman("admin")
    .addIntegerData("counter", 0)
    .addSubProcess("subprocess", "sub_process")
      .addSubProcessInParameter("counter", "counter")
      .addSubProcessOutParameter("counter", "counter")
      .addLoop("counter < 10", false)
    .addHumanTask("task3", "admin")
    .addTransition("transition2", "subprocess", "task3")
    .done();
    
    BusinessArchive businessArchive = getBusinessArchive(simpleProcess, null, SimpleMultiInstantiator.class);
    return getManagementAPI().deploy(businessArchive);
  }
  
  private ProcessDefinition buildProcessWithSubProcessNoLoop() throws Exception {
    ProcessDefinition simpleProcess = ProcessBuilder.createProcess("process_with_sub_process", "1.0")
    .addHuman("admin")
    .addIntegerData("counter", 10)
    .addSubProcess("subprocess", "sub_process")
      .addSubProcessInParameter("counter", "counter")
      .addSubProcessOutParameter("counter", "counter")
      .addLoop("counter < 10", true)
    .addHumanTask("task3", "admin")
    .addTransition("transition2", "subprocess", "task3")
    .done();
    
    BusinessArchive businessArchive = getBusinessArchive(simpleProcess, null, SimpleMultiInstantiator.class);
    return getManagementAPI().deploy(businessArchive);
  }
  
  private void testSubprocesswithLoop(ProcessDefinition processWithSubProcess) throws Exception {
    
    ProcessInstanceUUID processInstanceUUID = getRuntimeAPI().instantiateProcess(processWithSubProcess.getUUID());
    
    for (int i = 0; i < 10; i++) {
      Set<ActivityInstance> activityInstances = getQueryRuntimeAPI().getActivityInstances(processInstanceUUID);
      assertEquals(i+1, activityInstances.size());
      for (ActivityInstance activityInstance : activityInstances) {
        assertEquals("subprocess", activityInstance.getActivityName());
      }
      
      ProcessInstance processInstance = getQueryRuntimeAPI().getProcessInstance(processInstanceUUID);
      Set<ProcessInstanceUUID> childrenInstanceUUID = processInstance.getChildrenInstanceUUID();
      for (ProcessInstanceUUID childInstanceUUID : childrenInstanceUUID) {
        ProcessInstance childInstance = getQueryRuntimeAPI().getProcessInstance(childInstanceUUID);
        if (InstanceState.STARTED.equals(childInstance.getInstanceState())) {
          Collection<TaskInstance> taskInstances = getQueryRuntimeAPI().getTaskList(childInstanceUUID, ActivityState.READY);
          assertEquals(1, taskInstances.size());
          TaskInstance childTaskInstance = taskInstances.iterator().next();
          assertEquals("task1", childTaskInstance.getActivityName());
          assertEquals(ActivityState.READY, childTaskInstance.getState());
          getRuntimeAPI().executeTask(childTaskInstance.getUUID(), true);
          
          taskInstances = getQueryRuntimeAPI().getTaskList(childInstanceUUID, ActivityState.READY);
          assertEquals(1, taskInstances.size());
          childTaskInstance = taskInstances.iterator().next();
          assertEquals("task2", childTaskInstance.getActivityName());
          assertEquals(ActivityState.READY, childTaskInstance.getState());
          getRuntimeAPI().executeTask(childTaskInstance.getUUID(), true);
      
          childInstance = getQueryRuntimeAPI().getProcessInstance(childInstanceUUID);
          assertEquals(InstanceState.FINISHED, childInstance.getInstanceState());
        }
      }
    }
    
    Collection<TaskInstance> taskInstances = getQueryRuntimeAPI().getTaskList(processInstanceUUID, ActivityState.READY);
    assertEquals(1, taskInstances.size());
    TaskInstance taskInstance = taskInstances.iterator().next();
    assertEquals("task3", taskInstance.getActivityName());
    assertEquals(ActivityState.READY, taskInstance.getState());
    getRuntimeAPI().executeTask(taskInstance.getUUID(), true);
    
    getRuntimeAPI().deleteProcessInstance(processInstanceUUID);
    getManagementAPI().deleteAllProcesses();
  }
  
  public void testSubprocessBeforeLoop() throws Exception {
    buildSimpleProcess();
    ProcessDefinition processWithSubProcess = buildProcessWithSubProcessBeforeLoop();
    testSubprocesswithLoop(processWithSubProcess);
  }
  
  public void testSubprocessAfterLoop() throws Exception {
    buildSimpleProcess();
    ProcessDefinition processWithSubProcess = buildProcessWithSubProcessAfterLoop();
    testSubprocesswithLoop(processWithSubProcess);
  }
  
  public void testAutomaticSubprocessBeforeLoop() throws Exception {
    buildSimpleAutomaticProcess();
    ProcessDefinition processWithSubProcess = buildProcessWithSubProcessBeforeLoop();

    ProcessInstanceUUID processInstanceUUID = getRuntimeAPI().instantiateProcess(processWithSubProcess.getUUID());
    
    Collection<ActivityInstance> activities = getQueryRuntimeAPI().getActivityInstances(processInstanceUUID);
    assertEquals(11, activities.size());
    Collection<TaskInstance> taskInstances = getQueryRuntimeAPI().getTaskList(processInstanceUUID, ActivityState.READY);
    assertEquals(1, taskInstances.size());
    TaskInstance taskInstance = taskInstances.iterator().next();
    assertEquals("task3", taskInstance.getActivityName());
    assertEquals(ActivityState.READY, taskInstance.getState());
    getRuntimeAPI().executeTask(taskInstance.getUUID(), true);
    
    getRuntimeAPI().deleteProcessInstance(processInstanceUUID);
    getManagementAPI().deleteAllProcesses();
  }
  
  public void testSubprocessNoLoop() throws Exception {
    buildSimpleProcess();
    ProcessDefinition processWithSubProcess = buildProcessWithSubProcessNoLoop();

    ProcessInstanceUUID processInstanceUUID = getRuntimeAPI().instantiateProcess(processWithSubProcess.getUUID());
    
    Collection<TaskInstance> tasks = getQueryRuntimeAPI().getTasks(processInstanceUUID);
    assertEquals(1, tasks.size());
    TaskInstance taskInstance = tasks.iterator().next();
    assertEquals("task3", taskInstance.getActivityName());
    assertEquals(ActivityState.READY, taskInstance.getState());
    getRuntimeAPI().executeTask(taskInstance.getUUID(), true);
    
    getRuntimeAPI().deleteProcessInstance(processInstanceUUID);
    getManagementAPI().deleteAllProcesses();
  }
  
  //test multi-instantiation
  public void testMultiInstatiationAfterLoopSystemTask() throws Exception {
    ProcessDefinition definition =
      ProcessBuilder.createProcess("multiple", "1.0")
      .addIntegerData("counter", 0)
      .addHuman(getLogin())
      .addSystemTask("multi")
        .addConnector(Event.automaticOnExit, SetVarConnector.class.getName(), true)
        .addInputParameter("variableName", "counter")
        .addInputParameter("value", "${++counter}")
        .addMultipleActivitiesInstantiator(NoContextMulitpleActivitiesInstantiator.class.getName())
          .addInputParameter("number", 10)
        .addMultipleActivitiesJoinChecker(FixedNumberJoinChecker.class.getName())
          .addInputParameter("activityNumber", 3)
        .addLoop("counter < 9", false)
      .addHumanTask("next", getLogin())
      .addTransition("multi", "next")
      .done();

    definition = getManagementAPI().deploy(getBusinessArchive(definition, null,
        SetVarConnector.class, NoContextMulitpleActivitiesInstantiator.class, FixedNumberJoinChecker.class));
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(definition.getUUID());
    Collection<ActivityInstance> activities = getQueryRuntimeAPI().getActivityInstances(instanceUUID);
    //check that the loop isn't considered id there is a multi-instantiation
    assertEquals(11, activities.size());
    Collection<TaskInstance> tasks = getQueryRuntimeAPI().getTaskList(getLogin(), ActivityState.READY);
    assertEquals(1, tasks.size());
    assertEquals("next", tasks.iterator().next().getActivityName());
    getManagementAPI().deleteProcess(definition.getUUID());
  }

  public void testLoopIdInTwoIterations() throws Exception {
    ProcessDefinition process =
      ProcessBuilder.createProcess("iterLoop", "2.1")
      .addHuman(getLogin())
      .addStringData("msg", "any")
      .addHumanTask("first", getLogin())
      .addHumanTask("firstLoop", getLogin())
        .addLoop("msg != 'go'", true)
      .addHumanTask("secondLoop", getLogin())
        .addLoop("msg != 'stop'", true)
      .addHumanTask("firstStep", getLogin())
      .addTransition("first", "firstLoop")
      .addTransition("first", "secondLoop")
      .addTransition("firstLoop", "firstStep")
      .done();

    process = getManagementAPI().deploy(getBusinessArchive(process));
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(process.getUUID());

    Set<String> loopIds = new HashSet<String>();
    Collection<TaskInstance> tasks = getQueryRuntimeAPI().getTaskList(instanceUUID, ActivityState.READY);
    assertEquals(1, tasks.size());
    TaskInstance task = tasks.iterator().next();
    assertEquals("first", task.getActivityName());
    assertEquals("noLoop", task.getLoopId());
    String iterationId = task.getIterationId();
    loopIds.add("noLoop");

    executeTask(instanceUUID, "first");

    tasks = getQueryRuntimeAPI().getTaskList(instanceUUID, ActivityState.READY);
    assertEquals(2, tasks.size());
    for (TaskInstance taskInstance : tasks) {
      String loopId = taskInstance.getLoopId();
      assertEquals(iterationId, taskInstance.getIterationId());
      assertFalse(loopIds.contains(loopId));
      loopIds.add(loopId);
      if ("firstLoop".equals(taskInstance.getActivityName())) {
        executeTask(instanceUUID, "firstLoop");
      }
    }

    tasks = getQueryRuntimeAPI().getTaskList(instanceUUID, ActivityState.READY);
    assertEquals(2, tasks.size());
    Iterator<TaskInstance> iterator = tasks.iterator();
    task = iterator.next();
    if ("secondLoop".equals(task.getActivityName())) {
      task = iterator.next();
    }
    assertEquals("firstLoop", task.getActivityName());
    String loopId = task.getLoopId();
    assertEquals(iterationId, task.getIterationId());
    assertFalse(loopIds.contains(loopId));
    loopIds.add(loopId);
    getRuntimeAPI().setProcessInstanceVariable(instanceUUID, "msg", "go");
    executeTask(instanceUUID, "firstLoop");

    tasks = getQueryRuntimeAPI().getTaskList(instanceUUID, ActivityState.READY);
    assertEquals(2, tasks.size());
    iterator = tasks.iterator();
    task = iterator.next();
    if ("secondLoop".equals(task.getActivityName())) {
      task = iterator.next();
    }
    assertEquals("firstStep", task.getActivityName());
    assertEquals(iterationId, task.getIterationId());
    assertEquals("noLoop", task.getLoopId());
    executeTask(instanceUUID, "firstStep");

    getManagementAPI().deleteProcess(process.getUUID());
  }

}
