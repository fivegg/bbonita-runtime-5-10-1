package org.ow2.bonita.activity.route;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.ow2.bonita.APITestCase;
import org.ow2.bonita.activity.instantiation.instantiator.TestOneJoinActivityInstantiator;
import org.ow2.bonita.facade.def.majorElement.ActivityDefinition.JoinType;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.runtime.ActivityInstance;
import org.ow2.bonita.facade.runtime.ActivityState;
import org.ow2.bonita.facade.runtime.InstanceState;
import org.ow2.bonita.facade.runtime.TaskInstance;
import org.ow2.bonita.facade.uuid.ActivityInstanceUUID;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.light.LightProcessInstance;
import org.ow2.bonita.light.LightTaskInstance;
import org.ow2.bonita.util.BonitaException;
import org.ow2.bonita.util.ProcessBuilder;

public class CancelJoinXorTest extends APITestCase {

  public void testPartialJoinMultiInstantiation() throws BonitaException {
    final URL xpdlUrl = this.getClass().getResource("joinXorCancel.xpdl");
    final ProcessDefinition process = getManagementAPI().deploy(getBusinessArchiveFromXpdl(xpdlUrl,
      TestOneJoinActivityInstantiator.class));
    final ProcessDefinitionUUID processUUID = process.getUUID();
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    final Set<ActivityInstance> actIntances = getQueryRuntimeAPI().getActivityInstances(instanceUUID, "r1");
    assertEquals(1, actIntances.size());


    for (final ActivityInstance activityInstance : actIntances) {
      if (activityInstance.isTask()) {
        final ActivityInstanceUUID taskUUID = activityInstance.getUUID();
        getRuntimeAPI().startTask(taskUUID, false);
        getRuntimeAPI().finishTask(taskUUID, false);
      }
    }
    Collection<TaskInstance> tasks = getQueryRuntimeAPI().getTaskList(instanceUUID, ActivityState.READY);
    assertEquals(0, tasks.size());
    tasks = getQueryRuntimeAPI().getTaskList(instanceUUID, ActivityState.ABORTED);
    assertEquals(1, tasks.size());

    checkExecutedOnce(instanceUUID, new String[]{"r1", "end"});

    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testRouteWithOnlyXORJoin() throws BonitaException {
    final URL xpdlUrl = this.getClass().getResource("joinXorCancelAutomaticActivities.xpdl");
    final ProcessDefinition process = getManagementAPI().deploy(getBusinessArchiveFromXpdl(xpdlUrl));
    final ProcessDefinitionUUID processUUID = process.getUUID();
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    final Set<ActivityInstance> actIntances = getQueryRuntimeAPI().getActivityInstances(instanceUUID);
    // shortest path to end is 6 activities
    // check shortest path is taken
    assertEquals(6, actIntances.size());

    checkOnlyOneExecuted(instanceUUID, new String[]{"a", "b"});
    checkOnlyOneExecuted(instanceUUID, new String[]{"a1", "b1", "c"});
    checkOnlyOneExecuted(instanceUUID, new String[]{"d", "e"});
    checkExecutedOnce(instanceUUID, new String[]{"f", "BonitaEnd"});

    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testRouteWithXORandANDJoin() throws Exception {
    final URL xpdlUrl = this.getClass().getResource("joinXorAndCancelAutomaticActivities.xpdl");
    final ProcessDefinition process = getManagementAPI().deploy(getBusinessArchiveFromXpdl(xpdlUrl));
    final ProcessDefinitionUUID processUUID = process.getUUID();
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    final Set<ActivityInstance> actIntances = getQueryRuntimeAPI().getActivityInstances(instanceUUID);
    Thread.sleep(3000);
    assertEquals(5, actIntances.size());
    List<String> activityNames = new ArrayList<String>();
    for (ActivityInstance activityInstance : actIntances) {
      assertEquals(ActivityState.FINISHED, activityInstance.getState());
      activityNames.add(activityInstance.getActivityName());
    }
    assertTrue(activityNames.contains("BonitaInit"));
    assertTrue(activityNames.contains("a") || activityNames.contains("b"));
    if(activityNames.contains("a")){
      assertTrue(activityNames.contains("a1"));
      assertTrue(activityNames.contains("c"));
      assertTrue(activityNames.contains("d"));
    } else {
      assertTrue(activityNames.contains("b1"));
      assertTrue(activityNames.contains("c"));
      assertTrue(activityNames.contains("e"));
    }

    final LightProcessInstance processInstance = getQueryRuntimeAPI().getLightProcessInstance(instanceUUID);
    assertTrue(InstanceState.STARTED.equals(processInstance.getInstanceState()));

    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }
  

  public void testXorGate() throws Exception {
    final String stepToExecuteName = "branch1Step1";
    final String stepToBecancelledName = "branch2Step1";
    final String stepAfterJoinName = "step2";
    ProcessDefinition process = ProcessBuilder.createProcess("xorCancellingBranchWithMultipleOutgoingTransitions", "1.0")
    .addHuman(getLogin())
    .addSystemTask("start")
    .addSystemTask("end")
    .addDecisionNode("xor")
    .addHumanTask(stepToExecuteName, getLogin())
    .addHumanTask(stepToBecancelledName, getLogin())
    .addHumanTask(stepAfterJoinName, getLogin())
    .addTransition("start", stepToExecuteName)
    .addTransition(stepToExecuteName, "xor")
    .addTransition("start", stepToBecancelledName)
    .addTransition(stepToBecancelledName, "end")
    .addTransition(stepToBecancelledName, "xor")
    .addTransition("xor", stepAfterJoinName)
    .addTransition(stepAfterJoinName, "end")
    .done();

    getManagementAPI().deploy(getBusinessArchive(process));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);
    assertEquals(1, getQueryRuntimeAPI().getProcessInstances().size());

    Collection<LightTaskInstance> tasks = getQueryRuntimeAPI().getLightTaskList(instanceUUID, ActivityState.READY);
    assertEquals(2, tasks.size());

    Iterator<LightTaskInstance> iter = tasks.iterator();
    LightTaskInstance task = iter.next();
    if (!stepToExecuteName.equals(task.getActivityName())) {
      task = iter.next();
    }
    assertEquals(stepToExecuteName, task.getActivityName());

    getRuntimeAPI().executeTask(task.getUUID(), true);
    tasks = getQueryRuntimeAPI().getLightTaskList(instanceUUID, ActivityState.READY);
    assertEquals(1, tasks.size());
    iter = tasks.iterator();
    task = iter.next();
    assertEquals(stepAfterJoinName, task.getActivityName());

    // Check the number of steps.
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testXorGateTwoTasks() throws Exception {
    final String stepToExecuteName = "branch2Step1";
    final String stepToBecancelledName = "branch1Step1";
    final String stepAfterJoinName = "step2";
    final String stepOtherName = "step3";
    ProcessDefinition process = ProcessBuilder.createProcess("xorCancellingBranchWithMultipleOutgoingTransitions", "1.0")
    .addHuman(getLogin())
    .addSystemTask("start")
    .addSystemTask("end")
    .addDecisionNode("xor")
    .addHumanTask(stepToExecuteName, getLogin())
    .addHumanTask(stepToBecancelledName, getLogin())
    .addHumanTask(stepAfterJoinName, getLogin())
    .addHumanTask(stepOtherName,getLogin())
    .addTransition("start", stepToExecuteName)
    .addTransition(stepToExecuteName, "xor")
    .addTransition("start", stepToBecancelledName)
    .addTransition(stepToExecuteName, stepOtherName)
    .addTransition(stepToBecancelledName, "xor")
    .addTransition("xor", stepAfterJoinName)
    .addTransition(stepAfterJoinName, "end")
    .addTransition(stepOtherName, "end")
    .done();

    getManagementAPI().deploy(getBusinessArchive(process));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);
    assertEquals(1, getQueryRuntimeAPI().getProcessInstances().size());

    Collection<LightTaskInstance> tasks = getQueryRuntimeAPI().getLightTaskList(instanceUUID, ActivityState.READY);
    assertEquals(2, tasks.size());

    executeTask(instanceUUID, stepToExecuteName);

    tasks = getQueryRuntimeAPI().getLightTaskList(instanceUUID, ActivityState.READY);
    assertEquals(2, tasks.size());
    Iterator<LightTaskInstance> iterator = tasks.iterator();
    LightTaskInstance taskOne = iterator.next();
    LightTaskInstance taskTwo = iterator.next();
    if (stepAfterJoinName.equals(taskOne.getActivityName())) {
      assertTrue(stepOtherName.equals(taskTwo.getActivityName()));
    } else {
      assertTrue(stepOtherName.equals(taskOne.getActivityName()));
      assertTrue(stepAfterJoinName.equals(taskTwo.getActivityName()));
    }

    // Check the number of steps.
    getManagementAPI().deleteProcess(processUUID);
  }
  
  public void testComplexJoinXorWithAutomaticTasks() throws Exception {
    ProcessDefinition process = ProcessBuilder.createProcess("complexjoinxor", "5.0")
      .addSystemTask("start")
      .addSystemTask("a")
      .addSystemTask("a1")
      .addSystemTask("b")
      .addSystemTask("b1")
      .addSystemTask("c")
      .addSystemTask("d")
      .addSystemTask("e")
      .addSystemTask("end")
      .addDecisionNode("gateway1")
        .addJoinType(JoinType.AND)
      .addDecisionNode("gateway2")
        .addJoinType(JoinType.XOR)
      .addDecisionNode("gateway3")
        .addJoinType(JoinType.AND)
      .addDecisionNode("gateway4")
        .addJoinType(JoinType.AND)
      .addDecisionNode("gateway5")
        .addJoinType(JoinType.AND)
      .addDecisionNode("gateway6")
        .addJoinType(JoinType.AND)
      .addDecisionNode("gateway7")
        .addJoinType(JoinType.XOR)
      .addTransition("start", "a")
      .addTransition("start", "b")
      .addTransition("a", "gateway1")
      .addTransition("gateway1", "a1")
      .addTransition("gateway1", "gateway2")
      .addTransition("a1", "d")
      .addTransition("d", "gateway3")
      .addTransition("gateway3", "gateway7")
      .addTransition("gateway7", "end")
      .addTransition("gateway2", "c")
      .addTransition("c", "gateway5")
      .addTransition("gateway5", "gateway3")
      .addTransition("gateway5", "gateway6")
      .addTransition("gateway6", "gateway7")
      .addTransition("b", "gateway4")
      .addTransition("gateway4", "gateway2")
      .addTransition("gateway4", "b1")
      .addTransition("b1", "e")
      .addTransition("e", "gateway6")
      
      .done();

    process = getManagementAPI().deploy(getBusinessArchive(process));
    final ProcessDefinitionUUID processUUID = process.getUUID();
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    waitForInstanceEnd(5000, 100, instanceUUID);
    final Set<ActivityInstance> actIntances = getQueryRuntimeAPI().getActivityInstances(instanceUUID);
    List<String> activityNames = new ArrayList<String>();
    for (ActivityInstance activityInstance : actIntances) {
      assertEquals("Activity: " + activityInstance.getActivityName(), ActivityState.FINISHED, activityInstance.getState());
      final String activityName = activityInstance.getActivityName();
      if (!activityName.startsWith("gateway")) {
        activityNames.add(activityName);
      }
    }
    assertEquals(activityNames.toString(), 6, activityNames.size());
    assertTrue(activityNames.contains("start"));
    assertTrue(activityNames.contains("end"));
    assertTrue(activityNames.contains("a") || activityNames.contains("b"));
    if(activityNames.contains("a")){
      assertTrue(activityNames.contains("a1"));
      assertTrue(activityNames.contains("c"));
      assertTrue(activityNames.contains("d"));
    } else {
      assertTrue(activityNames.contains("b1"));
      assertTrue(activityNames.contains("c"));
      assertTrue(activityNames.contains("e"));
    }
    final LightProcessInstance processInstance = getQueryRuntimeAPI().getLightProcessInstance(instanceUUID);
    assertTrue(InstanceState.FINISHED.equals(processInstance.getInstanceState()));

    getManagementAPI().deleteProcess(processUUID);
  }

  public void testComplexJoinXorWithHumanTasks() throws Exception {
    ProcessDefinition process = ProcessBuilder.createProcess("complexjoinxor", "5.0")
      .addSystemTask("start")
      .addHuman(getLogin())
      .addHumanTask("a", getLogin())
      .addHumanTask("a1", getLogin())
      .addHumanTask("b", getLogin())
      .addHumanTask("b1", getLogin())
      .addHumanTask("c", getLogin())
      .addHumanTask("d", getLogin())
      .addHumanTask("e", getLogin())
      .addSystemTask("end")
      .addDecisionNode("gateway1")
      .addJoinType(JoinType.AND)
      .addDecisionNode("gateway2")
      .addJoinType(JoinType.XOR)
      .addDecisionNode("gateway3")
      .addJoinType(JoinType.AND)
      .addDecisionNode("gateway4")
      .addJoinType(JoinType.AND)
      .addDecisionNode("gateway5")
      .addJoinType(JoinType.AND)
      .addDecisionNode("gateway6")
      .addJoinType(JoinType.AND)
      .addDecisionNode("gateway7")
      .addJoinType(JoinType.XOR)
      .addTransition("start", "a")
      .addTransition("start", "b")
      .addTransition("a", "gateway1")
      .addTransition("gateway1", "a1")
      .addTransition("gateway1", "gateway2")
      .addTransition("a1", "d")
      .addTransition("d", "gateway3")
      .addTransition("gateway3", "gateway7")
      .addTransition("gateway7", "end")
      .addTransition("gateway2", "c")
      .addTransition("c", "gateway5")
      .addTransition("gateway5", "gateway3")
      .addTransition("gateway5", "gateway6")
      .addTransition("gateway6", "gateway7")
      .addTransition("b", "gateway4")
      .addTransition("gateway4", "gateway2")
      .addTransition("gateway4", "b1")
      .addTransition("b1", "e")
      .addTransition("e", "gateway6")
      .done();
    
    process = getManagementAPI().deploy(getBusinessArchive(process));
    final ProcessDefinitionUUID processUUID = process.getUUID();
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);
    executeTask(instanceUUID, "a");
    executeTask(instanceUUID, "a1");
    executeTask(instanceUUID, "c");
    executeTask(instanceUUID, "d");
    
    final Set<ActivityInstance> actIntances = getQueryRuntimeAPI().getActivityInstances(instanceUUID);
    List<String> activityNames = new ArrayList<String>();
    for (ActivityInstance activityInstance : actIntances) {
      final String activityName = activityInstance.getActivityName();
      if(activityName.equals("b")) {
        assertEquals(ActivityState.ABORTED, activityInstance.getState());
      } else {
        assertEquals(ActivityState.FINISHED, activityInstance.getState());
        if (!activityName.startsWith("gateway")) {
          activityNames.add(activityName);
        }
      }
    }
    assertEquals(6, activityNames.size());
    assertTrue(activityNames.contains("start"));
    assertTrue(activityNames.contains("end"));
    assertTrue(activityNames.contains("a"));
    assertTrue(activityNames.contains("a1"));
    assertTrue(activityNames.contains("c"));
    assertTrue(activityNames.contains("d"));

    final LightProcessInstance processInstance = getQueryRuntimeAPI().getLightProcessInstance(instanceUUID);
    assertTrue(InstanceState.FINISHED.equals(processInstance.getInstanceState()));
    
    getManagementAPI().deleteProcess(processUUID);
  }
  
}
