package org.ow2.bonita.facade;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.security.auth.login.LoginContext;

import org.ow2.bonita.APITestCase;
import org.ow2.bonita.definition.MultiInstantiator;
import org.ow2.bonita.definition.MultiInstantiatorDescriptor;
import org.ow2.bonita.facade.def.element.BusinessArchive;
import org.ow2.bonita.facade.def.element.HookDefinition.Event;
import org.ow2.bonita.facade.def.majorElement.ActivityDefinition.JoinType;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.runtime.ActivityInstance;
import org.ow2.bonita.facade.runtime.ActivityState;
import org.ow2.bonita.facade.runtime.AttachmentInstance;
import org.ow2.bonita.facade.runtime.InitialAttachment;
import org.ow2.bonita.facade.runtime.InstanceState;
import org.ow2.bonita.facade.runtime.ProcessInstance;
import org.ow2.bonita.facade.runtime.TaskInstance;
import org.ow2.bonita.facade.runtime.impl.InitialAttachmentImpl;
import org.ow2.bonita.facade.uuid.ActivityInstanceUUID;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.util.ProcessBuilder;
import org.ow2.bonita.util.SimpleCallbackHandler;

public class RepairAPITest extends APITestCase {

  private ProcessDefinition buildSimpleProcess() throws Exception {
    final ProcessDefinition simpleProcess = ProcessBuilder.createProcess("simple_process", "1.0")
    .addIntegerData("variable1", 0)
    .addIntegerData("variable2", 0)
    .addAttachment("attachment1")
    .addAttachment("attachment2")
    .addHuman("admin")
    .addHumanTask("task1", "admin")
    .addHumanTask("task2", "admin")
    .addTransition("transition1", "task1", "task2")
    .done();
    
    final BusinessArchive businessArchive = getBusinessArchive(simpleProcess);
    return getManagementAPI().deploy(businessArchive);
  }

  private ProcessDefinition buildAutomaticProcess() throws Exception {
    final ProcessDefinition autoProcess = ProcessBuilder.createProcess("simple_process", "1.0")
    .addSystemTask("activity1")
    .asynchronous()
    .addSystemTask("activity2")
    .asynchronous()
    .addConnector(Event.automaticOnExit, ErrorConnector.class.getName(), true)
    .addSystemTask("activity3")
    .asynchronous()
    .addTransition("transition1", "activity1", "activity2")
    .addTransition("transition2", "activity2", "activity3")
    .done();
    
    final BusinessArchive businessArchive = getBusinessArchive(autoProcess, null, ErrorConnector.class);
    return getManagementAPI().deploy(businessArchive);
  }
  
  private ProcessDefinition buildComplexProcess() throws Exception {
    final ProcessDefinition simpleProcess = ProcessBuilder.createProcess("complex_process", "1.0")
    .addIntegerData("number", 0)
    .addStringData("str", "")
    .addHuman("admin")
    .addHumanTask("task1", "admin")
    .addHumanTask("task2", "admin")
    .addSystemTask("task3")
    .addHumanTask("task4", "admin")
    .addTransition("transition1", "task1", "task2")
    .addCondition("number == 0")
    .addTransition("transition2", "task1", "task3")
    .addCondition("number != 0")
    .addTransition("transition3", "task2", "task4")
    .addTransition("transition4", "task3", "task4")
    .done();
    
    final BusinessArchive businessArchive = getBusinessArchive(simpleProcess);
    return getManagementAPI().deploy(businessArchive);
  }
  
  public void testAsyncActivityGoToFailed() throws Exception {
    final String activityName = "activity";
    
    ProcessDefinition process = ProcessBuilder.createProcess("asyncFailProcess", "1.0")
    .addSystemTask(activityName)
    .asynchronous()
    .addConnector(Event.automaticOnExit, ErrorConnector.class.getName(), true)
    .done();
    
    process =  getManagementAPI().deploy(getBusinessArchive(process, null, ErrorConnector.class));
    final ProcessDefinitionUUID processUUID = process.getUUID();
    
    final ProcessInstanceUUID processInstanceUUID = getRuntimeAPI().instantiateProcess(processUUID);
    
     Thread.sleep(10000);
     checkActivityFailed(processInstanceUUID, activityName);
    
    getRuntimeAPI().deleteProcessInstance(processInstanceUUID);
    getManagementAPI().deleteAllProcesses();
  }
  
  private void checkActivityFailed(final ProcessInstanceUUID processInstanceUUID, final String activityName) throws Exception {
    final Set<ActivityInstance> activityInstances = getQueryRuntimeAPI().getActivityInstances(processInstanceUUID, activityName);
    assertEquals(1, activityInstances.size());
    final ActivityInstance activity = activityInstances.iterator().next();
    assertEquals(ActivityState.FAILED, activity.getState());
  }
  
  private ProcessDefinition buildParallelProcess() throws Exception {
    final ProcessDefinition simpleProcess = ProcessBuilder.createProcess("complex_parallel_process", "1.0")
    .addIntegerData("number", 0)
    .addHuman("admin")
    .addHumanTask("task1", "admin")
    .addHumanTask("task2", "admin")
    .addHumanTask("task3", "admin")
    .addDecisionNode("GateWay")
    .addJoinType(JoinType.AND)
    .addHumanTask("task4", "admin")
    .addTransition("transition1", "task1", "task2")
    .addTransition("transition2", "task1", "task3")
    .addTransition("transition3", "task2", "GateWay")
    .addTransition("transition4", "task3", "GateWay")
    .addTransition("transition5", "GateWay", "task4")
    .done();
    
    final BusinessArchive businessArchive = getBusinessArchive(simpleProcess);
    return getManagementAPI().deploy(businessArchive);
  }
  
  private ProcessDefinition buildIterationProcess() throws Exception {
    final ProcessDefinition simpleProcess = ProcessBuilder.createProcess("complex_iteration_process", "1.0")
    .addIntegerData("number", 0)
    .addHuman("admin")
    .addHumanTask("task1", "admin")
    .addDecisionNode("GateWay")
    .addJoinType(JoinType.XOR)
    .addHumanTask("task2", "admin")
    .addHumanTask("task3", "admin")
    .addHumanTask("task4", "admin")
    .addTransition("transition1", "task1", "GateWay")
    .addTransition("transition2", "GateWay", "task2")
    .addTransition("transition3", "task2", "task3")
    .addCondition("number == 0")
    .addTransition("transition4", "task3", "GateWay")
    .addTransition("transition5", "task2", "task4")
    .addCondition("number != 0")
    .done();
    
    final BusinessArchive businessArchive = getBusinessArchive(simpleProcess);
    return getManagementAPI().deploy(businessArchive);
  }
  
  private ProcessDefinition buildMultiInstProcess() throws Exception {
    final ProcessDefinition simpleProcess = ProcessBuilder.createProcess("complex_multi_process", "1.0")
    .addHuman("admin")
    .addHumanTask("task1", "admin")
    .addHumanTask("task2", "admin")
    .addIntegerData("number", 0)
    .addMultiInstanciation("number", SimpleMultiInstantiator.class.getName())
    .addHumanTask("task3", "admin")
    .addTransition("transition1", "task1", "task2")
    .addTransition("transition2", "task2", "task3")
    .done();
    
    final BusinessArchive businessArchive = getBusinessArchive(simpleProcess, null, SimpleMultiInstantiator.class);
    return getManagementAPI().deploy(businessArchive);
  }
  
  private ProcessDefinition buildProcessWithSubProcess() throws Exception {
    final ProcessDefinition simpleProcess = ProcessBuilder.createProcess("sub_process", "1.0")
    .addHuman("admin")
    .addHumanTask("task1", "admin")
    .addSubProcess("subprocess", "simple_process")
    .addHumanTask("task3", "admin")
    .addTransition("transition1", "task1", "subprocess")
    .addTransition("transition2", "subprocess", "task3")
    .done();
    
    final BusinessArchive businessArchive = getBusinessArchive(simpleProcess, null, SimpleMultiInstantiator.class);
    return getManagementAPI().deploy(businessArchive);
  }
  
  public void testSimpleStopStartExecution() throws Exception {
    final ProcessDefinition simpleProcess = buildSimpleProcess();
    final ProcessInstanceUUID processInstanceUUID = getRuntimeAPI().instantiateProcess(simpleProcess.getUUID());
    
    getRepairAPI().stopExecution(processInstanceUUID, "task1");
    
    assertEquals(InstanceState.STARTED, getQueryRuntimeAPI().getProcessInstance(processInstanceUUID).getInstanceState());
    
    getRepairAPI().startExecution(processInstanceUUID, "task2");
    
    assertEquals(1, getQueryRuntimeAPI().getTaskList(ActivityState.READY).size());
    
    final Set<TaskInstance> taskInstances = getQueryRuntimeAPI().getTasks(processInstanceUUID);
    assertEquals(2, taskInstances.size());
    for (final TaskInstance taskInstance : taskInstances) {
      if(taskInstance.getActivityName().equals("task1")) {
        assertEquals(ActivityState.CANCELLED, taskInstance.getState());
      } else {
        assertEquals(ActivityState.READY, taskInstance.getState());
      }
    }
    
    ActivityInstanceUUID currentActivityInstanceUUID = getQueryRuntimeAPI().getOneTask(processInstanceUUID, ActivityState.READY);
    final ActivityInstance currentActivityInstance = getQueryRuntimeAPI().getTask(currentActivityInstanceUUID);
    assertEquals("task2", currentActivityInstance.getActivityName());
    
    currentActivityInstanceUUID = getQueryRuntimeAPI().getOneTask(processInstanceUUID, ActivityState.READY);
    getRuntimeAPI().executeTask(currentActivityInstanceUUID, true);
    
    assertEquals(InstanceState.FINISHED, getQueryRuntimeAPI().getProcessInstance(processInstanceUUID).getInstanceState());
    
    getRuntimeAPI().deleteProcessInstance(processInstanceUUID);
    getManagementAPI().deleteAllProcesses();
  }
  
  public void testSimpleCopyProcessInstance() throws Exception {
    final ProcessDefinition simpleProcess = buildSimpleProcess();
    final Map<String, Object> processVariables = new HashMap<String, Object>();
    processVariables.put("variable1", 1);
    final ProcessInstanceUUID processInstanceUUID = getRuntimeAPI().instantiateProcess(simpleProcess.getUUID(), processVariables);
    final ActivityInstanceUUID currentActivityInstanceUUID = getQueryRuntimeAPI().getOneTask(processInstanceUUID, ActivityState.READY);
    getRuntimeAPI().executeTask(currentActivityInstanceUUID, true);
    
    final ProcessInstance processInstance = getQueryRuntimeAPI().getProcessInstance(processInstanceUUID);
    
    loginContext.logout();
    loginContext = new LoginContext("BonitaStore", new SimpleCallbackHandler("john", "bpm"));
    loginContext.login();
    
    final Set<InitialAttachment> initialAttachments = new HashSet<InitialAttachment>();
    
    final ProcessInstanceUUID copyProcessInstanceUUID = getRepairAPI().copyProcessInstance(processInstanceUUID, new HashMap<String, Object>(), initialAttachments);
    
    final Set<TaskInstance> taskInstances = getQueryRuntimeAPI().getTasks(copyProcessInstanceUUID);
    assertEquals(1, taskInstances.size());
    assertEquals("task2", taskInstances.iterator().next().getActivityName());
    assertEquals(ActivityState.READY, taskInstances.iterator().next().getState());
    
    final Integer variable1 = (Integer)getQueryRuntimeAPI().getProcessInstanceVariable(copyProcessInstanceUUID, "variable1");
    assertEquals(1, variable1.intValue());
    
    final ProcessInstance copyProcessInstance = getQueryRuntimeAPI().getProcessInstance(copyProcessInstanceUUID);
    assertEquals(processInstance.getStartedBy(), copyProcessInstance.getStartedBy());
    
    getRuntimeAPI().deleteProcessInstance(processInstanceUUID);
    getRuntimeAPI().deleteProcessInstance(copyProcessInstanceUUID);
    getManagementAPI().deleteAllProcesses();
  }
  
  public void testSimpleCopyProcessInstanceAndSetVariables() throws Exception {
    final ProcessDefinition simpleProcess = buildSimpleProcess();
    final Map<String, Object> processVariables = new HashMap<String, Object>();
    processVariables.put("variable1", 1);
    final ProcessInstanceUUID processInstanceUUID = getRuntimeAPI().instantiateProcess(simpleProcess.getUUID(), processVariables);
    final ActivityInstanceUUID currentActivityInstanceUUID = getQueryRuntimeAPI().getOneTask(processInstanceUUID, ActivityState.READY);
    getRuntimeAPI().executeTask(currentActivityInstanceUUID, true);

    processVariables.put("variable1", 2);
    
    final Set<InitialAttachment> initialAttachments = new HashSet<InitialAttachment>();
    final InitialAttachmentImpl initialAttachment = new InitialAttachmentImpl("attachment1", new byte[1]);
    initialAttachment.setLabel("label");
    initialAttachment.setFileName("fileName");
    initialAttachments.add(initialAttachment);
    
    final ProcessInstanceUUID copyProcessInstanceUUID = getRepairAPI().copyProcessInstance(processInstanceUUID, processVariables, initialAttachments);
    
    final Integer variable1 = (Integer)getQueryRuntimeAPI().getProcessInstanceVariable(copyProcessInstanceUUID, "variable1");
    assertEquals(2, variable1.intValue());
    
    final AttachmentInstance attachment1 = getQueryRuntimeAPI().getLastAttachment(copyProcessInstanceUUID, "attachment1");
    assertEquals("fileName", attachment1.getFileName());
    
    getRuntimeAPI().deleteProcessInstance(processInstanceUUID);
    getRuntimeAPI().deleteProcessInstance(copyProcessInstanceUUID);
    getManagementAPI().deleteAllProcesses();
  }
  
  public void testSimpleCopyProcessInstanceAtDate() throws Exception {
    final String attachmentName = "attachment1";
    final ProcessDefinition simpleProcess = buildSimpleProcess();
    final Map<String, Object> processVariables = new HashMap<String, Object>();
    processVariables.put("variable1", 1);
    final Set<InitialAttachment> initialAttachments1 = new HashSet<InitialAttachment>();
    final InitialAttachmentImpl initialAttachment1 = new InitialAttachmentImpl(attachmentName, new byte[1]);
    initialAttachment1.setLabel("label1");
    initialAttachment1.setFileName("fileName1");
    initialAttachments1.add(initialAttachment1);
    final ProcessInstanceUUID processInstanceUUID = getRuntimeAPI().instantiateProcess(simpleProcess.getUUID(), processVariables, initialAttachments1);

    final ActivityInstanceUUID currentActivityInstanceUUID = getQueryRuntimeAPI().getOneTask(processInstanceUUID, ActivityState.READY);
    getRuntimeAPI().executeTask(currentActivityInstanceUUID, true);

    Thread.sleep(10);
    getRuntimeAPI().setProcessInstanceVariable(processInstanceUUID, "variable1", 2);
    getRuntimeAPI().addAttachment(processInstanceUUID, attachmentName, "fileName2", new byte[1]);

    final long activityEndDate = getQueryRuntimeAPI().getActivityInstance(currentActivityInstanceUUID).getEndedDate().getTime();

    final ProcessInstanceUUID copyProcessInstanceUUID = getRepairAPI().copyProcessInstance(processInstanceUUID, new HashMap<String, Object>(), new HashSet<InitialAttachment>(), new Date(activityEndDate));

    final Integer variable1 = (Integer)getQueryRuntimeAPI().getProcessInstanceVariable(copyProcessInstanceUUID, "variable1");
    assertEquals(1, variable1.intValue());

    final AttachmentInstance attachment1 = getQueryRuntimeAPI().getLastAttachment(copyProcessInstanceUUID, attachmentName);
    assertEquals("fileName1", attachment1.getFileName());

    getRuntimeAPI().deleteProcessInstance(processInstanceUUID);
    getRuntimeAPI().deleteProcessInstance(copyProcessInstanceUUID);
    getManagementAPI().deleteAllProcesses();
  }

  public void testSimpleCopyProcessInstanceAtDateAndSetVariables() throws Exception {
    final ProcessDefinition simpleProcess = buildSimpleProcess();
    final Map<String, Object> processVariables = new HashMap<String, Object>();
    processVariables.put("variable1", 1);
    final Set<InitialAttachment> initialAttachments1 = new HashSet<InitialAttachment>();
    final InitialAttachmentImpl initialAttachment1 = new InitialAttachmentImpl("attachment1", new byte[1]);
    initialAttachment1.setLabel("label1");
    initialAttachment1.setFileName("fileName1");
    initialAttachments1.add(initialAttachment1);
    final ProcessInstanceUUID processInstanceUUID = getRuntimeAPI().instantiateProcess(simpleProcess.getUUID(), processVariables, initialAttachments1);
    
    final ActivityInstanceUUID currentActivityInstanceUUID = getQueryRuntimeAPI().getOneTask(processInstanceUUID, ActivityState.READY);
    getRuntimeAPI().executeTask(currentActivityInstanceUUID, true);
    
    Thread.sleep(2);
    getRuntimeAPI().setProcessInstanceVariable(processInstanceUUID, "variable1", 2);
    getRuntimeAPI().addAttachment(processInstanceUUID, "attachment1", "fileName2", new byte[1]);
    
    final long activityEndDate = getQueryRuntimeAPI().getActivityInstance(currentActivityInstanceUUID).getEndedDate().getTime();
    
    processVariables.put("variable2", 3);
    final Set<InitialAttachment> initialAttachments3 = new HashSet<InitialAttachment>();
    final InitialAttachmentImpl initialAttachment3 = new InitialAttachmentImpl("attachment2", new byte[1]);
    initialAttachment3.setLabel("label3");
    initialAttachment3.setFileName("fileName3");
    initialAttachments3.add(initialAttachment3);
    initialAttachments3.add(initialAttachment1);
    
    final ProcessInstanceUUID copyProcessInstanceUUID = getRepairAPI().copyProcessInstance(processInstanceUUID, processVariables, initialAttachments3, new Date(activityEndDate));
    
    final Integer variable1 = (Integer)getQueryRuntimeAPI().getProcessInstanceVariable(copyProcessInstanceUUID, "variable1");
    assertEquals(1, variable1.intValue());
    
    final AttachmentInstance attachment1 = getQueryRuntimeAPI().getLastAttachment(copyProcessInstanceUUID, "attachment1");
    assertEquals("fileName1", attachment1.getFileName());
    
    final Integer variable2 = (Integer)getQueryRuntimeAPI().getProcessInstanceVariable(copyProcessInstanceUUID, "variable2");
    assertEquals(3, variable2.intValue());
    
    final AttachmentInstance attachment2 = getQueryRuntimeAPI().getLastAttachment(copyProcessInstanceUUID, "attachment2");
    assertEquals("fileName3", attachment2.getFileName());
    
    getRuntimeAPI().deleteProcessInstance(processInstanceUUID);
    getRuntimeAPI().deleteProcessInstance(copyProcessInstanceUUID);
    getManagementAPI().deleteAllProcesses();
  }
  
  public void testSimpleInstantiateProcess() throws Exception {
    final ProcessDefinition simpleProcess = buildSimpleProcess();
    
    final Map<String, Object> processVariables = new HashMap<String, Object>();
    processVariables.put("variable1", 1);
    final Set<InitialAttachment> initialAttachments = new HashSet<InitialAttachment>();
    final InitialAttachmentImpl initialAttachment1 = new InitialAttachmentImpl("attachment1", new byte[1]);
    initialAttachment1.setLabel("label1");
    initialAttachment1.setFileName("fileName1");
    initialAttachments.add(initialAttachment1);
    final List<String> activityExecutionsToStart = new ArrayList<String>();
    activityExecutionsToStart.add("task2");
    final ProcessInstanceUUID processInstanceUUID = getRepairAPI().instantiateProcess(simpleProcess.getUUID(), processVariables, initialAttachments, activityExecutionsToStart);
    
    final Set<TaskInstance> taskInstances = getQueryRuntimeAPI().getTasks(processInstanceUUID);
    assertEquals(1, taskInstances.size());
    assertEquals("task2", taskInstances.iterator().next().getActivityName());
    assertEquals(ActivityState.READY, taskInstances.iterator().next().getState());
    
    final Integer variable1 = (Integer)getQueryRuntimeAPI().getProcessInstanceVariable(processInstanceUUID, "variable1");
    assertEquals(1, variable1.intValue());
    
    final AttachmentInstance attachment1 = getQueryRuntimeAPI().getLastAttachment(processInstanceUUID, "attachment1");
    assertEquals("fileName1", attachment1.getFileName());
    
    assertEquals(InstanceState.STARTED, getQueryRuntimeAPI().getProcessInstance(processInstanceUUID).getInstanceState());
    
    getRuntimeAPI().deleteProcessInstance(processInstanceUUID);
    getManagementAPI().deleteAllProcesses();
  }
  
  public void testSimpleInstantiateProcessWithUserId() throws Exception {
    final ProcessDefinition simpleProcess = buildSimpleProcess();
    
    final Map<String, Object> processVariables = new HashMap<String, Object>();
    final Set<InitialAttachment> initialAttachments = new HashSet<InitialAttachment>();
    final List<String> activityExecutionsToStart = new ArrayList<String>();
    activityExecutionsToStart.add("task2");
    final ProcessInstanceUUID processInstanceUUID = getRepairAPI().instantiateProcess(simpleProcess.getUUID(), processVariables, initialAttachments, activityExecutionsToStart, "jack");
    
    final Set<TaskInstance> taskInstances = getQueryRuntimeAPI().getTasks(processInstanceUUID);
    assertEquals(1, taskInstances.size());
    assertEquals("task2", taskInstances.iterator().next().getActivityName());
    assertEquals(ActivityState.READY, taskInstances.iterator().next().getState());
    
    assertEquals(InstanceState.STARTED, getQueryRuntimeAPI().getProcessInstance(processInstanceUUID).getInstanceState());
    
    final ProcessInstance processInstance = getQueryRuntimeAPI().getProcessInstance(processInstanceUUID);
    assertEquals("jack", processInstance.getStartedBy());
    
    getRuntimeAPI().deleteProcessInstance(processInstanceUUID);
    getManagementAPI().deleteAllProcesses();
  }
  
  public void testComplexStopStartExecution() throws Exception {
    final ProcessDefinition complexProcess = buildComplexProcess();
    final ProcessInstanceUUID processInstanceUUID = getRuntimeAPI().instantiateProcess(complexProcess.getUUID());
    
    getRepairAPI().stopExecution(processInstanceUUID, "task1");

    getRepairAPI().startExecution(processInstanceUUID, "task2");
    
    final Set<TaskInstance> taskInstances = getQueryRuntimeAPI().getTasks(processInstanceUUID);
    assertEquals(2, taskInstances.size());
    for (final TaskInstance taskInstance : taskInstances) {
      if(taskInstance.getActivityName().equals("task1")) {
        assertEquals(ActivityState.CANCELLED, taskInstance.getState());
      } else {
        assertEquals(ActivityState.READY, taskInstance.getState());
      }
    }
    
    getRuntimeAPI().deleteProcessInstance(processInstanceUUID);
    getManagementAPI().deleteAllProcesses();
  }
  
  public void testComplexInstantiateProcess() throws Exception {
    final ProcessDefinition complexProcess = buildComplexProcess();
    
    final List<String> activityExecutionsToStart = new ArrayList<String>();
    activityExecutionsToStart.add("task2");
    final ProcessInstanceUUID processInstanceUUID = getRepairAPI().instantiateProcess(complexProcess.getUUID(), new HashMap<String, Object>(), new HashSet<InitialAttachment>(), activityExecutionsToStart);
    
    final Set<TaskInstance> taskInstances = getQueryRuntimeAPI().getTasks(processInstanceUUID);
    assertEquals(1, taskInstances.size());
    assertEquals("task2", taskInstances.iterator().next().getActivityName());
    assertEquals(ActivityState.READY, taskInstances.iterator().next().getState());
    
    getRuntimeAPI().deleteProcessInstance(processInstanceUUID);
    getManagementAPI().deleteAllProcesses();
  }
  
  public void testComplexCopyProcessInstance() throws Exception {
    final ProcessDefinition complexProcess = buildComplexProcess();

    final ProcessInstanceUUID processInstanceUUID = getRuntimeAPI().instantiateProcess(complexProcess.getUUID(), new HashMap<String, Object>());
    
    final ActivityInstanceUUID currentActivityInstanceUUID = getQueryRuntimeAPI().getOneTask(processInstanceUUID, ActivityState.READY);
    getRuntimeAPI().executeTask(currentActivityInstanceUUID, true);
    
    final ProcessInstanceUUID copyProcessInstanceUUID = getRepairAPI().copyProcessInstance(processInstanceUUID, new HashMap<String, Object>(), new HashSet<InitialAttachment>());
    
    final Set<TaskInstance> taskInstances = getQueryRuntimeAPI().getTasks(copyProcessInstanceUUID);
    assertEquals(1, taskInstances.size());
    assertEquals("task2", taskInstances.iterator().next().getActivityName());
    assertEquals(ActivityState.READY, taskInstances.iterator().next().getState());
    
    getRuntimeAPI().deleteProcessInstance(processInstanceUUID);
    getRuntimeAPI().deleteProcessInstance(copyProcessInstanceUUID);
    getManagementAPI().deleteAllProcesses();
  }
  
  public void testComplexCopyProcessInstanceAfterBranch() throws Exception {
    final ProcessDefinition complexProcess = buildComplexProcess();

    final ProcessInstanceUUID processInstanceUUID = getRuntimeAPI().instantiateProcess(complexProcess.getUUID(), new HashMap<String, Object>());
    
    ActivityInstanceUUID currentActivityInstanceUUID = getQueryRuntimeAPI().getOneTask(processInstanceUUID, ActivityState.READY);
    getRuntimeAPI().executeTask(currentActivityInstanceUUID, true);
    
    currentActivityInstanceUUID = getQueryRuntimeAPI().getOneTask(processInstanceUUID, ActivityState.READY);
    getRuntimeAPI().executeTask(currentActivityInstanceUUID, true);
    
    getQueryRuntimeAPI().getOneTask(processInstanceUUID, ActivityState.READY);
    
    final ProcessInstanceUUID copyProcessInstanceUUID = getRepairAPI().copyProcessInstance(processInstanceUUID, new HashMap<String, Object>(), new HashSet<InitialAttachment>());
    
    final Set<TaskInstance> taskInstances = getQueryRuntimeAPI().getTasks(copyProcessInstanceUUID);
    assertEquals(1, taskInstances.size());
    assertEquals("task4", taskInstances.iterator().next().getActivityName());
    assertEquals(ActivityState.READY, taskInstances.iterator().next().getState());
    
    getRuntimeAPI().deleteProcessInstance(processInstanceUUID);
    getRuntimeAPI().deleteProcessInstance(copyProcessInstanceUUID);
    getManagementAPI().deleteAllProcesses();
  }
  
  public void testParallelInstantiateProcess() throws Exception {
    final ProcessDefinition parallelProcess = buildParallelProcess();
    
    final List<String> activityExecutionsToStart = new ArrayList<String>();
    activityExecutionsToStart.add("task2");
    activityExecutionsToStart.add("task3");
    final ProcessInstanceUUID processInstanceUUID = getRepairAPI().instantiateProcess(parallelProcess.getUUID(), new HashMap<String, Object>(), new HashSet<InitialAttachment>(), activityExecutionsToStart);
    
    final Set<TaskInstance> taskInstances = getQueryRuntimeAPI().getTasks(processInstanceUUID);
    assertEquals(2, taskInstances.size());
    for (final TaskInstance taskInstance : taskInstances) {
      if (taskInstance.getActivityName().equals("task2") || taskInstance.getActivityName().equals("task3")) {
        assertEquals(ActivityState.READY, taskInstance.getState());
      } else {
        fail("only task 2 and 3 should be present in the task instances list.");
      }
    }
    
    getRuntimeAPI().deleteProcessInstance(processInstanceUUID);
    getManagementAPI().deleteAllProcesses();
  }
  
  public void testParallelInstantiateProcessInABranch() throws Exception {
    final ProcessDefinition parallelProcess = buildParallelProcess();
    
    final List<String> activityExecutionsToStart = new ArrayList<String>();
    activityExecutionsToStart.add("task2");
    final ProcessInstanceUUID processInstanceUUID = getRepairAPI().instantiateProcess(parallelProcess.getUUID(), new HashMap<String, Object>(), new HashSet<InitialAttachment>(), activityExecutionsToStart);
    
    Collection<TaskInstance> taskInstances = getQueryRuntimeAPI().getTasks(processInstanceUUID);
    assertEquals(1, taskInstances.size());
    TaskInstance taskInstance = taskInstances.iterator().next();
    assertEquals(ActivityState.READY, taskInstance.getState());
    getRuntimeAPI().executeTask(taskInstance.getUUID(), true);
    
    taskInstances = getQueryRuntimeAPI().getTaskList(processInstanceUUID, ActivityState.READY);
    assertEquals(0, taskInstances.size());
    
    getRepairAPI().startExecution(processInstanceUUID, "task3");
    taskInstances = getQueryRuntimeAPI().getTaskList(processInstanceUUID, ActivityState.READY);
    taskInstance = taskInstances.iterator().next();
    getRuntimeAPI().executeTask(taskInstance.getUUID(), true);
    
    taskInstances = getQueryRuntimeAPI().getTaskList(processInstanceUUID, ActivityState.READY);
    assertEquals(1, taskInstances.size());
    taskInstance = taskInstances.iterator().next();
    assertEquals("task4", taskInstance.getActivityName());
    
    getRuntimeAPI().deleteProcessInstance(processInstanceUUID);
    getManagementAPI().deleteAllProcesses();
  }
  
  public void testParallelCopyProcessInstance() throws Exception {
    final ProcessDefinition parallelProcess = buildParallelProcess();
    final ProcessInstanceUUID processInstanceUUID = getRuntimeAPI().instantiateProcess(parallelProcess.getUUID());
    
    ActivityInstanceUUID currentActivityInstanceUUID = getQueryRuntimeAPI().getOneTask(processInstanceUUID, ActivityState.READY);
    getRuntimeAPI().executeTask(currentActivityInstanceUUID, true);

    final ProcessInstanceUUID copyProcessInstanceUUID = getRepairAPI().copyProcessInstance(processInstanceUUID, new HashMap<String, Object>(), new HashSet<InitialAttachment>());
    
    Collection<TaskInstance> taskInstances = getQueryRuntimeAPI().getTasks(copyProcessInstanceUUID);
    assertEquals(2, taskInstances.size());
    for (final TaskInstance taskInstance : taskInstances) {
      if (taskInstance.getActivityName().equals("task2") || taskInstance.getActivityName().equals("task3")) {
        assertEquals(ActivityState.READY, taskInstance.getState());
      } else {
        fail("only task 2 and 3 should be present in the task instances list.");
      }
    }
    
    currentActivityInstanceUUID = getQueryRuntimeAPI().getOneTask(copyProcessInstanceUUID, ActivityState.READY);
    getRuntimeAPI().executeTask(currentActivityInstanceUUID, true);
    
    currentActivityInstanceUUID = getQueryRuntimeAPI().getOneTask(copyProcessInstanceUUID, ActivityState.READY);
    getRuntimeAPI().executeTask(currentActivityInstanceUUID, true);
    
    taskInstances = getQueryRuntimeAPI().getTaskList(copyProcessInstanceUUID, ActivityState.READY);
    assertEquals(1, taskInstances.size());
    assertEquals("task4", taskInstances.iterator().next().getActivityName());
    
    currentActivityInstanceUUID = getQueryRuntimeAPI().getOneTask(copyProcessInstanceUUID, ActivityState.READY);
    getRuntimeAPI().executeTask(currentActivityInstanceUUID, true);
    
    assertEquals(InstanceState.FINISHED, getQueryRuntimeAPI().getProcessInstance(copyProcessInstanceUUID).getInstanceState());
    
    getRuntimeAPI().deleteProcessInstance(processInstanceUUID);
    getRuntimeAPI().deleteProcessInstance(copyProcessInstanceUUID);
    getManagementAPI().deleteAllProcesses();
  }
  
  public void testIterationInstantiateProcess() throws Exception {
    final ProcessDefinition iterationProcess = buildIterationProcess();

    final List<String> activityExecutionsToStart = new ArrayList<String>();
    activityExecutionsToStart.add("task2");
    final ProcessInstanceUUID processInstanceUUID = getRepairAPI().instantiateProcess(iterationProcess.getUUID(), new HashMap<String, Object>(), new HashSet<InitialAttachment>(), activityExecutionsToStart);
    
    Collection<TaskInstance> taskInstances = getQueryRuntimeAPI().getTasks(processInstanceUUID);
    assertEquals(1, taskInstances.size());
    assertEquals("task2", taskInstances.iterator().next().getActivityName());
    assertEquals(ActivityState.READY, taskInstances.iterator().next().getState());

    ActivityInstanceUUID currentActivityInstanceUUID = getQueryRuntimeAPI().getOneTask(processInstanceUUID, ActivityState.READY);
    getRuntimeAPI().executeTask(currentActivityInstanceUUID, true);
    
    taskInstances = getQueryRuntimeAPI().getTaskList(ActivityState.READY);
    assertEquals(1, taskInstances.size());
    assertEquals("task3", taskInstances.iterator().next().getActivityName());
    
    currentActivityInstanceUUID = getQueryRuntimeAPI().getOneTask(processInstanceUUID, ActivityState.READY);
    getRuntimeAPI().executeTask(currentActivityInstanceUUID, true);
    
    taskInstances = getQueryRuntimeAPI().getTaskList(ActivityState.READY);
    assertEquals(1, taskInstances.size());
    assertEquals("task2", taskInstances.iterator().next().getActivityName());
    
    currentActivityInstanceUUID = getQueryRuntimeAPI().getOneTask(processInstanceUUID, ActivityState.READY);
    getRuntimeAPI().setVariable(currentActivityInstanceUUID, "number", 1);
    getRuntimeAPI().executeTask(currentActivityInstanceUUID, true);
    
    taskInstances = getQueryRuntimeAPI().getTaskList(ActivityState.READY);
    assertEquals(1, taskInstances.size());
    assertEquals("task4", taskInstances.iterator().next().getActivityName());
    
    currentActivityInstanceUUID = getQueryRuntimeAPI().getOneTask(processInstanceUUID, ActivityState.READY);
    getRuntimeAPI().executeTask(currentActivityInstanceUUID, true);
    
    assertEquals(InstanceState.FINISHED, getQueryRuntimeAPI().getProcessInstance(processInstanceUUID).getInstanceState());
    
    getRuntimeAPI().deleteProcessInstance(processInstanceUUID);
    getManagementAPI().deleteAllProcesses();
  }
  
  public void testIterationCopyProcessInstance() throws Exception {
    final ProcessDefinition iterationProcess = buildIterationProcess();

    final ProcessInstanceUUID processInstanceUUID = getRuntimeAPI().instantiateProcess(iterationProcess.getUUID());

    ActivityInstanceUUID currentActivityInstanceUUID = getQueryRuntimeAPI().getOneTask(processInstanceUUID, ActivityState.READY);
    getRuntimeAPI().executeTask(currentActivityInstanceUUID, true);
    
    currentActivityInstanceUUID = getQueryRuntimeAPI().getOneTask(processInstanceUUID, ActivityState.READY);
    getRuntimeAPI().executeTask(currentActivityInstanceUUID, true);
    
    currentActivityInstanceUUID = getQueryRuntimeAPI().getOneTask(processInstanceUUID, ActivityState.READY);
    getRuntimeAPI().executeTask(currentActivityInstanceUUID, true);
    
    final Map<String, Object> processVariables = new HashMap<String, Object>();
    processVariables.put("number", 1);
    final ProcessInstanceUUID copyProcessInstanceUUID = getRepairAPI().copyProcessInstance(processInstanceUUID, processVariables, new HashSet<InitialAttachment>());
    
    Collection<TaskInstance> taskInstances = getQueryRuntimeAPI().getTasks(copyProcessInstanceUUID);
    assertEquals(1, taskInstances.size());
    assertEquals("task2", taskInstances.iterator().next().getActivityName());
    assertEquals(ActivityState.READY, taskInstances.iterator().next().getState());
    
    currentActivityInstanceUUID = getQueryRuntimeAPI().getOneTask(copyProcessInstanceUUID, ActivityState.READY);
    getRuntimeAPI().executeTask(currentActivityInstanceUUID, true);
    
    taskInstances = getQueryRuntimeAPI().getTaskList(copyProcessInstanceUUID, ActivityState.READY);
    assertEquals(1, taskInstances.size());
    assertEquals("task4", taskInstances.iterator().next().getActivityName());
    
    currentActivityInstanceUUID = getQueryRuntimeAPI().getOneTask(copyProcessInstanceUUID, ActivityState.READY);
    getRuntimeAPI().executeTask(currentActivityInstanceUUID, true);
    
    assertEquals(InstanceState.FINISHED, getQueryRuntimeAPI().getProcessInstance(copyProcessInstanceUUID).getInstanceState());
    
    getRuntimeAPI().deleteProcessInstance(processInstanceUUID);
    getRuntimeAPI().deleteProcessInstance(copyProcessInstanceUUID);
    getManagementAPI().deleteAllProcesses();
  }
  
  public void testMultiInstInstantiateProcess() throws Exception {
    final ProcessDefinition multiInstProcess = buildMultiInstProcess();
    
    final List<String> activityExecutionsToStart = new ArrayList<String>();
    activityExecutionsToStart.add("task2");
    final ProcessInstanceUUID processInstanceUUID = getRepairAPI().instantiateProcess(multiInstProcess.getUUID(), new HashMap<String, Object>(), new HashSet<InitialAttachment>(), activityExecutionsToStart);
    
    Collection<TaskInstance> taskInstances = getQueryRuntimeAPI().getTasks(processInstanceUUID);
    assertEquals(3, taskInstances.size());
    Integer number = null;
    for (final TaskInstance taskInstance : taskInstances) {
      assertEquals("task2", taskInstance.getActivityName());
      assertEquals(ActivityState.READY, taskInstance.getState());
      final Integer variable = (Integer)getQueryRuntimeAPI().getActivityInstanceVariable(taskInstance.getUUID(), "number");
      assertNotSame(number, variable);
      number = variable;
      getRuntimeAPI().executeTask(taskInstance.getUUID(), true);
    }
    
    taskInstances = getQueryRuntimeAPI().getTaskList(processInstanceUUID, ActivityState.READY);
    assertEquals(1, taskInstances.size());
    assertEquals("task3", taskInstances.iterator().next().getActivityName());
    
    final ActivityInstanceUUID currentActivityInstanceUUID = getQueryRuntimeAPI().getOneTask(processInstanceUUID, ActivityState.READY);
    getRuntimeAPI().executeTask(currentActivityInstanceUUID, true);
    
    assertEquals(InstanceState.FINISHED, getQueryRuntimeAPI().getProcessInstance(processInstanceUUID).getInstanceState());
    
    getRuntimeAPI().deleteProcessInstance(processInstanceUUID);
    getManagementAPI().deleteAllProcesses();
  }
  
  public void testMultiInstCopyProcessInstance() throws Exception {
    final ProcessDefinition multiInstProcess = buildMultiInstProcess();

    final ProcessInstanceUUID processInstanceUUID = getRuntimeAPI().instantiateProcess(multiInstProcess.getUUID());

    ActivityInstanceUUID currentActivityInstanceUUID = getQueryRuntimeAPI().getOneTask(processInstanceUUID, ActivityState.READY);
    getRuntimeAPI().executeTask(currentActivityInstanceUUID, true);
    
    final ProcessInstanceUUID copyProcessInstanceUUID = getRepairAPI().copyProcessInstance(processInstanceUUID, new HashMap<String, Object>(), new HashSet<InitialAttachment>());
    
    Collection<TaskInstance> taskInstances = getQueryRuntimeAPI().getTasks(copyProcessInstanceUUID);
    assertEquals(3, taskInstances.size());
    Integer number = null;
    for (final TaskInstance taskInstance : taskInstances) {
      assertEquals("task2", taskInstance.getActivityName());
      assertEquals(ActivityState.READY, taskInstance.getState());
      final Integer variable = (Integer)getQueryRuntimeAPI().getActivityInstanceVariable(taskInstance.getUUID(), "number");
      assertNotSame(number, variable);
      number = variable;
      getRuntimeAPI().executeTask(taskInstance.getUUID(), true);
    }
    
    taskInstances = getQueryRuntimeAPI().getTaskList(copyProcessInstanceUUID, ActivityState.READY);
    assertEquals(1, taskInstances.size());
    assertEquals("task3", taskInstances.iterator().next().getActivityName());
    
    currentActivityInstanceUUID = getQueryRuntimeAPI().getOneTask(copyProcessInstanceUUID, ActivityState.READY);
    getRuntimeAPI().executeTask(currentActivityInstanceUUID, true);
    
    assertEquals(InstanceState.FINISHED, getQueryRuntimeAPI().getProcessInstance(copyProcessInstanceUUID).getInstanceState());
    
    getRuntimeAPI().deleteProcessInstance(processInstanceUUID);
    getRuntimeAPI().deleteProcessInstance(copyProcessInstanceUUID);
    getManagementAPI().deleteAllProcesses();
  }
  
  public void testSubprocessInstantiateProcess() throws Exception {
    buildSimpleProcess();
    final ProcessDefinition processWithSubProcess = buildProcessWithSubProcess();
    
    final List<String> activityExecutionsToStart = new ArrayList<String>();
    activityExecutionsToStart.add("subprocess");
    final ProcessInstanceUUID processInstanceUUID = getRepairAPI().instantiateProcess(processWithSubProcess.getUUID(), new HashMap<String, Object>(), new HashSet<InitialAttachment>(), activityExecutionsToStart);
    
    final Set<ActivityInstance> activityInstances = getQueryRuntimeAPI().getActivityInstances(processInstanceUUID);
    assertEquals(1, activityInstances.size());
    assertEquals("subprocess", activityInstances.iterator().next().getActivityName());
    assertEquals(ActivityState.EXECUTING, activityInstances.iterator().next().getState());
    
    final ProcessInstance processInstance = getQueryRuntimeAPI().getProcessInstance(processInstanceUUID);
    final ProcessInstanceUUID childInstanceUUID = processInstance.getChildrenInstanceUUID().iterator().next();
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

    assertEquals(InstanceState.FINISHED, getQueryRuntimeAPI().getProcessInstance(childInstanceUUID).getInstanceState());
    
    taskInstances = getQueryRuntimeAPI().getTaskList(processInstanceUUID, ActivityState.READY);
    assertEquals(1, taskInstances.size());
    final TaskInstance taskInstance = taskInstances.iterator().next();
    assertEquals("task3", taskInstance.getActivityName());
    assertEquals(ActivityState.READY, taskInstance.getState());
    getRuntimeAPI().executeTask(taskInstance.getUUID(), true);
  
    assertEquals(InstanceState.FINISHED, getQueryRuntimeAPI().getProcessInstance(processInstanceUUID).getInstanceState());
    
    getRuntimeAPI().deleteProcessInstance(processInstanceUUID);
    getManagementAPI().deleteAllProcesses();
  }
  
  public void testSubprocessCopyProcessInstance() throws Exception {
    buildSimpleProcess();
    final ProcessDefinition processWithSubProcess = buildProcessWithSubProcess();

    final ProcessInstanceUUID processInstanceUUID = getRuntimeAPI().instantiateProcess(processWithSubProcess.getUUID());

    final ActivityInstanceUUID currentActivityInstanceUUID = getQueryRuntimeAPI().getOneTask(processInstanceUUID, ActivityState.READY);
    getRuntimeAPI().executeTask(currentActivityInstanceUUID, true);
    
    final ProcessInstanceUUID copyProcessInstanceUUID = getRepairAPI().copyProcessInstance(processInstanceUUID, new HashMap<String, Object>(), new HashSet<InitialAttachment>());

    final Set<ActivityInstance> activityInstances = getQueryRuntimeAPI().getActivityInstances(copyProcessInstanceUUID);
    assertEquals(1, activityInstances.size());
    assertEquals("subprocess", activityInstances.iterator().next().getActivityName());
    assertEquals(ActivityState.EXECUTING, activityInstances.iterator().next().getState());
    
    final ProcessInstance processInstance = getQueryRuntimeAPI().getProcessInstance(copyProcessInstanceUUID);
    final ProcessInstanceUUID childInstanceUUID = processInstance.getChildrenInstanceUUID().iterator().next();
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

    assertEquals(InstanceState.FINISHED, getQueryRuntimeAPI().getProcessInstance(childInstanceUUID).getInstanceState());
    
    taskInstances = getQueryRuntimeAPI().getTaskList(copyProcessInstanceUUID, ActivityState.READY);
    assertEquals(1, taskInstances.size());
    final TaskInstance taskInstance = taskInstances.iterator().next();
    assertEquals("task3", taskInstance.getActivityName());
    assertEquals(ActivityState.READY, taskInstance.getState());
    getRuntimeAPI().executeTask(taskInstance.getUUID(), true);
  
    assertEquals(InstanceState.FINISHED, getQueryRuntimeAPI().getProcessInstance(copyProcessInstanceUUID).getInstanceState());
    
    getRuntimeAPI().deleteProcessInstance(copyProcessInstanceUUID);
    getRuntimeAPI().deleteProcessInstance(processInstanceUUID);
    getManagementAPI().deleteAllProcesses();
  }

  public void testStopExecutionAfterError() throws Exception {
    final ProcessDefinition autoProcess = buildAutomaticProcess();
    final ProcessInstanceUUID processInstanceUUID = getRuntimeAPI().instantiateProcess(autoProcess.getUUID());
    
    Set<ActivityInstance> activityInstances = null;
    
//    Thread.sleep(100);
//    
//    activityInstances = getQueryRuntimeAPI().getActivityInstances(processInstanceUUID);
//    assertEquals(2, activityInstances.size());
//    for (ActivityInstance activityInstance : activityInstances) {
//      System.out.println(activityInstance.getActivityName() + " - " + activityInstance.getState());
//    }
//    
//    Thread.sleep(900);
//
//    activityInstances = getQueryRuntimeAPI().getActivityInstances(processInstanceUUID);
//    assertEquals(2, activityInstances.size());
//    for (ActivityInstance activityInstance : activityInstances) {
//      System.out.println(activityInstance.getActivityName() + " - " + activityInstance.getState());
//    }
    
    Thread.sleep(1000);
    
    getRepairAPI().stopExecution(processInstanceUUID, "activity2");
    
    activityInstances = getQueryRuntimeAPI().getActivityInstances(processInstanceUUID);
    assertEquals(2, activityInstances.size());
    for (final ActivityInstance activityInstance : activityInstances) {
      if(activityInstance.getActivityName().equals("activity2")) {
        assertEquals(ActivityState.CANCELLED, activityInstance.getState());
      }
    }
    
    getRuntimeAPI().deleteProcessInstance(processInstanceUUID);
    getManagementAPI().deleteAllProcesses();
  }
  
  public static class  SimpleMultiInstantiator implements MultiInstantiator {
    @Override
    public MultiInstantiatorDescriptor execute(final QueryAPIAccessor arg0, final ProcessInstanceUUID arg1, final String arg2, final String arg3) throws Exception {
      final List<Object> objects = new ArrayList<Object>();
      objects.add(1);
      objects.add(2);
      objects.add(3);
      return new MultiInstantiatorDescriptor(objects.size(), objects);
    }
  }
 
}
