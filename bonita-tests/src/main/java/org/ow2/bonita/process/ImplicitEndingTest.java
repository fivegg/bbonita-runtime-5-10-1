package org.ow2.bonita.process;

import java.util.Collection;
import java.util.Set;

import org.ow2.bonita.APITestCase;
import org.ow2.bonita.facade.def.majorElement.ActivityDefinition.JoinType;
import org.ow2.bonita.facade.def.majorElement.ActivityDefinition.SplitType;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.runtime.ActivityInstance;
import org.ow2.bonita.facade.runtime.ActivityState;
import org.ow2.bonita.facade.runtime.InstanceState;
import org.ow2.bonita.facade.runtime.ProcessInstance;
import org.ow2.bonita.facade.runtime.TaskInstance;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.light.LightProcessInstance;
import org.ow2.bonita.util.ProcessBuilder;

public class ImplicitEndingTest extends APITestCase {

  public void testTerminateProcessOnlyWhenTheTwoBranchesAreDone() throws Exception {
    ProcessDefinition process = ProcessBuilder.createProcess("branches", "1.0")
    .addHuman(getLogin())
    .addSystemTask("start")
    .addHumanTask("branch1", getLogin())
    .addHumanTask("branch2", getLogin())
    .addTransition("start", "branch1")
    .addTransition("start", "branch2")
    .done();

    getManagementAPI().deploy(getBusinessArchive(process));
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(process.getUUID());
    Collection<TaskInstance> tasks = getQueryRuntimeAPI().getTaskList(instanceUUID, ActivityState.READY);
    assertEquals(2, tasks.size());
    executeTask(instanceUUID, "branch2");
    LightProcessInstance instance = getQueryRuntimeAPI().getLightProcessInstance(instanceUUID);
    assertEquals(InstanceState.STARTED, instance.getInstanceState());

    executeTask(instanceUUID, "branch1");
    instance = getQueryRuntimeAPI().getLightProcessInstance(instanceUUID);
    assertEquals(InstanceState.FINISHED, instance.getInstanceState());

    getManagementAPI().deleteProcess(process.getUUID());
  }

  public void testTerminateProcessWhenOneBranchIsDoneDueToTheTerminateEndEvent() throws Exception {
    ProcessDefinition process = ProcessBuilder.createProcess("branches", "1.0")
    .addHuman(getLogin())
    .addSystemTask("start")
    .addHumanTask("branch1", getLogin())
    .addHumanTask("branch2", getLogin())
    .addTerminateEndEvent("stop")
    .addTransition("start", "branch1")
    .addTransition("start", "branch2")
    .addTransition("branch2", "stop")
    .done();

    getManagementAPI().deploy(getBusinessArchive(process));
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(process.getUUID());
    Collection<TaskInstance> tasks = getQueryRuntimeAPI().getTaskList(instanceUUID, ActivityState.READY);
    assertEquals(2, tasks.size());
    executeTask(instanceUUID, "branch2");
    LightProcessInstance instance = getQueryRuntimeAPI().getLightProcessInstance(instanceUUID);
    assertEquals(InstanceState.FINISHED, instance.getInstanceState());

    getManagementAPI().deleteProcess(process.getUUID());
  }

  public void testImplicitEndingOnSeveralBranches() throws Exception {
    ProcessDefinition process = ProcessBuilder.createProcess("brnchs", "1.2")
    .addHuman(getLogin())
    .addSystemTask("start")
    .addHumanTask("branch1", getLogin())
    .addSystemTask("branch2")
    .addHumanTask("branch21", getLogin())
    .addHumanTask("branch22", getLogin())
    .addHumanTask("branch23", getLogin())
    .addDecisionNode("Xor")
      .addJoinType(JoinType.XOR)
    .addTransition("start", "branch1")
    .addTransition("start", "branch2")
    .addTransition("branch2", "branch21")
    .addTransition("branch2", "branch22")
    .addTransition("branch2", "branch23")
    .addTransition("branch21", "Xor")
    .addTransition("branch22", "Xor")
    .done();

    getManagementAPI().deploy(getBusinessArchive(process));
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(process.getUUID());
    executeTask(instanceUUID, "branch22");
    Collection<TaskInstance> tasks = getQueryRuntimeAPI().getTaskList(instanceUUID, ActivityState.READY);
    assertEquals(2, tasks.size());
 
    executeTask(instanceUUID, "branch1");
    tasks = getQueryRuntimeAPI().getTaskList(instanceUUID, ActivityState.READY);
    assertEquals(1, tasks.size());
 
    executeTask(instanceUUID, "branch23");
    ProcessInstance instance = getQueryRuntimeAPI().getProcessInstance(instanceUUID);
    assertEquals(InstanceState.FINISHED, instance.getInstanceState());

    getManagementAPI().deleteProcess(process.getUUID());
  }

  public void testWaintingMessageToFinish() throws Exception {
    ProcessDefinition process = ProcessBuilder.createProcess("message", "1.0")
    .addHuman(getLogin())
    .addHumanTask("step", getLogin())
    .addSystemTask("start")
    .addReceiveEventTask("order", "orderNow")
    .addTransition("start", "step")
    .addTransition("start", "order")
    .done();

    getManagementAPI().deploy(getBusinessArchive(process));
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(process.getUUID());

    executeTask(instanceUUID, "step");
    ProcessInstance instance = getQueryRuntimeAPI().getProcessInstance(instanceUUID);
    assertEquals(InstanceState.STARTED, instance.getInstanceState());

    getManagementAPI().deleteProcess(process.getUUID());
  }

  public void testWaitingSignalToFinish() throws Exception {
    ProcessDefinition process = ProcessBuilder.createProcess("message", "1.0")
    .addHuman(getLogin())
    .addHumanTask("step", getLogin())
    .addSystemTask("start")
    .addSignalEventTask("order", "orderNow", true)
    .addTransition("start", "step")
    .addTransition("start", "order")
    .addHumanTask("stre", getLogin())
    .addTransition("order", "stre")
    .done();

    getManagementAPI().deploy(getBusinessArchive(process));
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(process.getUUID());

    executeTask(instanceUUID, "step");
    ProcessInstance instance = getQueryRuntimeAPI().getProcessInstance(instanceUUID);
    assertEquals(InstanceState.STARTED, instance.getInstanceState());

    getManagementAPI().deleteProcess(process.getUUID());
  }

  public void testWaitingTimerToFinish() throws Exception {
    ProcessDefinition process = ProcessBuilder.createProcess("message", "1.0")
    .addHuman(getLogin())
    .addHumanTask("step", getLogin())
    .addSystemTask("start")
    .addTimerTask("order", "360000")
    .addTransition("start", "step")
    .addTransition("start", "order")
    .done();

    getManagementAPI().deploy(getBusinessArchive(process));
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(process.getUUID());

    executeTask(instanceUUID, "step");
    ProcessInstance instance = getQueryRuntimeAPI().getProcessInstance(instanceUUID);
    assertEquals(InstanceState.STARTED, instance.getInstanceState());

    getManagementAPI().deleteProcess(process.getUUID());
  }

  public void testTerminateProcessWithASingleImplicitEnding() throws Exception {
    ProcessDefinition process = ProcessBuilder.createProcess("switcher", "2.5")
      .addBooleanData("approved", true)
      .addHuman(getLogin())
      .addSystemTask("Start")
      .addHumanTask("Technical_Review", getLogin())
      .addHumanTask("Financial_Review", getLogin())
      .addDecisionNode("Approved")
        .addJoinType(JoinType.AND)
        .addSplitType(SplitType.XOR)
      .addTerminateEndEvent("Stop_Work")
      .addSystemTask("Review_Complete")
        .addJoinType(JoinType.AND)
      .addTransition("Start", "Technical_Review")
      .addTransition("Start", "Financial_Review")
      .addTransition("Technical_Review", "Review_Complete")
      .addTransition("Financial_Review", "Approved")
      .addTransition("Approved", "Review_Complete")
        .addCondition("approved")
      .addTransition("Approved", "Stop_Work")
        .addCondition("!approved")
    .done();

    getManagementAPI().deploy(getBusinessArchive(process));
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(process.getUUID());
    ProcessInstance instance = getQueryRuntimeAPI().getProcessInstance(instanceUUID);
    assertEquals(InstanceState.STARTED, instance.getInstanceState());

    executeTask(instanceUUID, "Financial_Review");
    instance = getQueryRuntimeAPI().getProcessInstance(instanceUUID);
    assertEquals(InstanceState.STARTED, instance.getInstanceState());

    executeTask(instanceUUID, "Technical_Review");
    instance = getQueryRuntimeAPI().getProcessInstance(instanceUUID);
    assertEquals(InstanceState.FINISHED, instance.getInstanceState());
    Set<ActivityInstance> activities = instance.getActivities();
    assertEquals(5, activities.size());
    ActivityInstance activity = getActivityInstance(activities, "Technical_Review");
    assertEquals(ActivityState.FINISHED, activity.getState());
    activity = getActivityInstance(activities, "Start");
    assertEquals(ActivityState.FINISHED, activity.getState());
    activity = getActivityInstance(activities, "Financial_Review");
    assertEquals(ActivityState.FINISHED, activity.getState());
    activity = getActivityInstance(activities, "Approved");
    assertEquals(ActivityState.FINISHED, activity.getState());
    activity = getActivityInstance(activities, "Technical_Review");
    assertEquals(ActivityState.FINISHED, activity.getState());

    getManagementAPI().deleteProcess(process.getUUID());
  }

  public void testTerminateProcess() throws Exception {
    ProcessDefinition process = ProcessBuilder.createProcess("switcher", "2.5")
      .addBooleanData("approved", false)
      .addHuman(getLogin())
      .addSystemTask("Start")
      .addHumanTask("Technical_Review", getLogin())
      .addHumanTask("Financial_Review", getLogin())
      .addDecisionNode("Approved")
      .addTerminateEndEvent("Stop_Work")
      .addSystemTask("Review_Complete")
      .addTransition("Start", "Technical_Review")
      .addTransition("Start", "Financial_Review")
      .addTransition("Technical_Review", "Review_Complete")
      .addTransition("Financial_Review", "Approved")
      .addTransition("Approved", "Review_Complete")
        .addCondition("approved")
      .addTransition("Approved", "Stop_Work")
        .addCondition("!approved")
    .done();

    getManagementAPI().deploy(getBusinessArchive(process));
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(process.getUUID());
    ProcessInstance instance = getQueryRuntimeAPI().getProcessInstance(instanceUUID);
    assertEquals(InstanceState.STARTED, instance.getInstanceState());

    executeTask(instanceUUID, "Financial_Review");
    instance = getQueryRuntimeAPI().getProcessInstance(instanceUUID);
    assertEquals(InstanceState.FINISHED, instance.getInstanceState());
    Set<ActivityInstance> activities = instance.getActivities();
    assertEquals(5, activities.size());
    ActivityInstance activity = getActivityInstance(activities, "Technical_Review");
    assertEquals(ActivityState.ABORTED, activity.getState());
    activity = getActivityInstance(activities, "Start");
    assertEquals(ActivityState.FINISHED, activity.getState());
    activity = getActivityInstance(activities, "Financial_Review");
    assertEquals(ActivityState.FINISHED, activity.getState());
    activity = getActivityInstance(activities, "Approved");
    assertEquals(ActivityState.FINISHED, activity.getState());
    activity = getActivityInstance(activities, "Stop_Work");
    assertEquals(ActivityState.FINISHED, activity.getState());

    getManagementAPI().deleteProcess(process.getUUID());
  }

  public void testTerminateASubProcessDoesNotTerminateParentProcess() throws Exception {
    ProcessDefinition process = ProcessBuilder.createProcess("switcher", "2.5")
      .addBooleanData("approved", false)
      .addHuman(getLogin())
      .addSystemTask("Start")
      .addHumanTask("Financial_Review", getLogin())
      .addTerminateEndEvent("Stop_Work")
      .addTransition("Start", "Financial_Review")
      .addTransition("Financial_Review", "Stop_Work")
    .done();

    ProcessDefinition mainProcess = ProcessBuilder.createProcess("mainPro", "1.5")
      .addHuman(getLogin())
      .addSubProcess("sub", "switcher", "2.5")
      .addHumanTask("next", getLogin())
      .addTransition("sub", "next")
    .done();

    getManagementAPI().deploy(getBusinessArchive(process));
    getManagementAPI().deploy(getBusinessArchive(mainProcess));

    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(mainProcess.getUUID());
    ProcessInstance instance = getQueryRuntimeAPI().getProcessInstance(instanceUUID);
    assertEquals(InstanceState.STARTED, instance.getInstanceState());

    Collection<TaskInstance> tasks = getQueryRuntimeAPI().getTaskList(ActivityState.READY);
    assertEquals(1, tasks.size());
    TaskInstance task = tasks.iterator().next();
    assertEquals("Financial_Review", task.getActivityName());
    getRuntimeAPI().executeTask(task.getUUID(), true);

    instance = getQueryRuntimeAPI().getProcessInstance(instanceUUID);
    assertEquals(InstanceState.STARTED, instance.getInstanceState());

    getManagementAPI().deleteProcess(mainProcess.getUUID());
    getManagementAPI().deleteProcess(process.getUUID());
  }
}
