package org.ow2.bonita.event;

import java.util.Set;

import junit.framework.AssertionFailedError;

import org.bonitasoft.connectors.bonita.SetVarConnector;
import org.ow2.bonita.APITestCase;
import org.ow2.bonita.facade.def.element.HookDefinition.Event;
import org.ow2.bonita.facade.def.majorElement.ActivityDefinition.JoinType;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.runtime.ActivityState;
import org.ow2.bonita.facade.runtime.InstanceState;
import org.ow2.bonita.facade.uuid.ActivityInstanceUUID;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.light.LightActivityInstance;
import org.ow2.bonita.util.Misc;
import org.ow2.bonita.util.ProcessBuilder;

public class MultipleEventsTest extends APITestCase {

  public void testConcurrentTx() throws Exception {
    ProcessDefinition process = ProcessBuilder.createProcess("myProcess", "1.0")
    .addHuman(getLogin())
    .addSystemTask("initial")
    .addHumanTask("human", getLogin())
      .addConnector(Event.taskOnReady, SleepConnector.class.getName(), true)
        .addInputParameter("setMillis", new Long(5000))
      .addConnector(Event.taskOnReady, SetVarConnector.class.getName(), true)
        .addInputParameter("setVariableName", "wrong")
        .addInputParameter("setValue", "value")
        .throwCatchError("errorCode")
      .addTimerBoundaryEvent("wait", "1000")
      .addErrorBoundaryEvent("throwError", "errorCode")
    .addSystemTask("normal")
    .addSystemTask("error")
    .addSystemTask("overdue")
    .addSystemTask("end")
    .addTransition("initial", "human")
    .addTransition("human", "normal")
    .addTransition("error", "end")
    .addTransition("overdue", "end")
    .addExceptionTransition("human", "wait", "overdue")
    .addExceptionTransition("human", "throwError", "error")
    .done();

    process = getManagementAPI().deploy(getBusinessArchive(process, null, SetVarConnector.class, SleepConnector.class));
    final ProcessDefinitionUUID processUUID = process.getUUID();
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    Thread.sleep(8000);
    Set<LightActivityInstance> activities = null; 
    try {
      checkExecutedOnce(instanceUUID, "initial", "human", "overdue", "end");
      checkNotExecuted(instanceUUID, "normal", "error");
    } catch (Exception e) {
      checkExecutedOnce(instanceUUID, "initial", "human", "error", "end");
      checkNotExecuted(instanceUUID, "normal", "overdue");
    }

    activities = getQueryRuntimeAPI().getLightActivityInstances(instanceUUID, "human");
    assertEquals(1, activities.size());
    assertEquals(ActivityState.ABORTED, activities.iterator().next().getState());

    getManagementAPI().deleteProcess(processUUID);
  }
  
  public void testRemoveUnConsumedIncomingEventOnDelete() throws Exception {    
    ProcessDefinition process = ProcessBuilder.createProcess("bbbb", "1.0")
    .addSystemTask("initial")
      .addReceiveEventTask("receive", "createProcess")
    .addTimerTask("timer", "${new Date(System.currentTimeMillis() + 1000)}")
    .addSystemTask("join")
    .addTerminateEndEvent("end")
    .addTransition("initial", "receive")
    .addTransition("initial", "timer")
    .addTransition("receive", "join")
    .addTransition("timer", "end")
    .done();

    process = getManagementAPI().deploy(getBusinessArchive(process));
    getManagementAPI().deployJar("getExecJar.jar", Misc.generateJar(GetIncomingEventInstancesCommand.class));

    final ProcessDefinitionUUID processUUID = process.getUUID();

    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);
    waitForInstanceEnd(6000, 300, instanceUUID);
    assertEquals(InstanceState.FINISHED, getQueryRuntimeAPI().getLightProcessInstance(instanceUUID).getInstanceState());

    getManagementAPI().deleteProcess(processUUID);

    //check no event is found
    assertEquals(0, getCommandAPI().execute(new GetIncomingEventInstancesCommand(), processUUID).size());

    getManagementAPI().removeJar("getExecJar.jar");
    //need to be deployed and deleted again as execution command recreate classloaders...
    getManagementAPI().deploy(getBusinessArchive(process));
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testPartialIncomingEventsDeletion() throws Exception {
    ProcessDefinition process = ProcessBuilder.createProcess("p", "d")
    .addSystemTask("initial")
    .addSendEventTask("send")
      .addOutgoingEvent("event", null, null, null)
        .addReceiveEventTask("receive1", "event")
        .addReceiveEventTask("receive2", "event")
    .addTimerTask("timer", "10000")
    .addTerminateEndEvent("end")
    .addSystemTask("join")
      .addJoinType(JoinType.AND)
    .addTransition("send", "timer")
    .addTransition("initial", "receive1")
    .addTransition("initial", "receive2")
    .addTransition("receive1", "join")
    .addTransition("receive2", "join")
    .addTransition("timer", "end")
    .done();

    process = getManagementAPI().deploy(getBusinessArchive(process));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    getManagementAPI().deployJar("iec.jar", Misc.generateJar(GetIncomingEventInstancesCommand.class));
    assertEquals(0, (int) getCommandAPI().execute(new GetIncomingEventInstancesCommand()).size());

    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);
    //it depends on threads... May be 1 or 2 or 3 -> 1 for timer task, 1 for receive1, one for receive2
    try {
      assertEquals(3, (int) getCommandAPI().execute(new GetIncomingEventInstancesCommand()).size());
    } catch (AssertionFailedError e) {
      try {
        assertEquals(2, (int) getCommandAPI().execute(new GetIncomingEventInstancesCommand()).size());
      } catch (AssertionFailedError e2) {
        assertEquals(1, (int) getCommandAPI().execute(new GetIncomingEventInstancesCommand()).size());
      }
    }

    this.waitForInstanceEnd(15000, 10, instanceUUID);

    assertEquals(0, (int) getCommandAPI().execute(new GetIncomingEventInstancesCommand()).size());

    getManagementAPI().removeJar("iec.jar");

    assertEquals(InstanceState.FINISHED, getQueryRuntimeAPI().getProcessInstance(instanceUUID).getInstanceState());
    checkExecutedOnce(instanceUUID, "send", "timer", "receive1");
    checkNotExecuted(instanceUUID, "join");
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testEventWithSimpleFalseExpressionUsingTimer() throws Exception {
    ProcessDefinition process = ProcessBuilder.createProcess("p", "d")
    .addSendEventTask("send")
      .addOutgoingEvent("event", null, null, null)
    .addSystemTask("initial")
    .addReceiveEventTask("receive", "event", "false")
    .addTimerTask("timer", Long.toString(5 * 1000))
    .addTerminateEndEvent("end")
    .addTransition("send", "timer")
    .addTransition("initial", "receive")
    .addTransition("timer", "end")
    .done();

    process = getManagementAPI().deploy(getBusinessArchive(process));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    this.waitForInstanceEnd(11000, 10, instanceUUID);

    assertEquals(InstanceState.FINISHED, getQueryRuntimeAPI().getProcessInstance(instanceUUID).getInstanceState());
    checkExecutedOnce(instanceUUID, "send", "timer");

    final ActivityInstanceUUID activityUUID = getQueryRuntimeAPI().getActivityInstances(instanceUUID, "send").iterator().next().getUUID();
    getRuntimeAPI().deleteEvents("event", null, null, activityUUID);

    getManagementAPI().deleteProcess(processUUID);
  }

  public void testReceiveTaskWithATimer() throws Exception {
    ProcessDefinition process = ProcessBuilder.createProcess("events", "1.2.5")
    .addHuman(getLogin())
    .addSystemTask("init")
    .addReceiveEventTask("event", "signal")
      .addTimerBoundaryEvent("over", "5000")
    .addHumanTask("next", getLogin())
    .addHumanTask("overdue", getLogin())
    .addTransition("init", "event")
    .addTransition("event", "next")
    .addExceptionTransition("event", "over", "overdue")
    .done();

    getManagementAPI().deploy(getBusinessArchive(process));
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(process.getUUID());
    Thread.sleep(6000);

    Set<LightActivityInstance> activities = getQueryRuntimeAPI().getLightActivityInstances(instanceUUID);
    assertEquals(3, activities.size());
    LightActivityInstance activity = getLightActivityInstance(activities, "init");
    assertEquals(ActivityState.FINISHED, activity.getState());
    activity = getLightActivityInstance(activities, "event");
    assertEquals(ActivityState.ABORTED, activity.getState());
    activity = getLightActivityInstance(activities, "overdue");
    assertEquals(ActivityState.READY, activity.getState());
    getManagementAPI().deleteProcess(process.getUUID());
  }

}
