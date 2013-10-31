package org.ow2.bonita.event;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ow2.bonita.APITestCase;
import org.ow2.bonita.facade.def.majorElement.ActivityDefinition.JoinType;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.runtime.ActivityState;
import org.ow2.bonita.facade.runtime.InstanceState;
import org.ow2.bonita.facade.runtime.ProcessInstance;
import org.ow2.bonita.facade.runtime.TaskInstance;
import org.ow2.bonita.facade.uuid.ActivityDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.light.LightActivityInstance;
import org.ow2.bonita.light.LightProcessInstance;
import org.ow2.bonita.light.LightTaskInstance;
import org.ow2.bonita.util.ProcessBuilder;

public class CorrelationMessageEventTest extends APITestCase {

  private void waitForCreation(final int expected, final ProcessDefinitionUUID createdUUID) throws Exception {
    final long before = System.currentTimeMillis();
    final long maxWait = 10000;
    boolean wait = true;
    do {
      Thread.sleep(500);
      final int instanceNb = getQueryRuntimeAPI().getLightProcessInstances(createdUUID).size();
      wait = instanceNb != 1 && System.currentTimeMillis() - before < maxWait;
    } while (wait);

    // wait to ensure that there is no more created instances than needed
    Thread.sleep(4000);

    final int size = getQueryRuntimeAPI().getLightProcessInstances(createdUUID).size();
    assertEquals("Process was not launched " + expected + " time(s). It was launched: " + size + " times", expected,
        size);
  }

  public void testStartEventOnlyStartReceiveEventInitialActivity() throws Exception {
    ProcessDefinition creator = ProcessBuilder.createProcess("creator", "1.0").addHuman(getLogin())
        .addSendEventTask("send").addOutgoingEvent("createProcess", null, null, null)
        .addMessageCorrelationKey("id", "${1}").done();

    ProcessDefinition created = ProcessBuilder.createProcess("created", "1.0").addHuman(getLogin())
        .addReceiveEventTask("startEvent", "createProcess").addMessageCorrelationKey("id", "${1}")
        .addSystemTask("startAuto").addHumanTask("human1", getLogin()).addHumanTask("human2", getLogin())
        .addTransition("startAuto", "human1").addTransition("startEvent", "human2").done();

    creator = getManagementAPI().deploy(getBusinessArchive(creator));
    created = getManagementAPI().deploy(getBusinessArchive(created));

    final ProcessDefinitionUUID creatorUUID = creator.getUUID();
    final ProcessDefinitionUUID createdUUID = created.getUUID();
    final ProcessInstanceUUID creatorInstanceUUID = getRuntimeAPI().instantiateProcess(creatorUUID);

    assertEquals("Creator process instance is started", InstanceState.FINISHED, getQueryRuntimeAPI()
        .getProcessInstance(creatorInstanceUUID).getInstanceState());

    waitForCreation(1, createdUUID);

    final Set<LightProcessInstance> createdInstances = getQueryRuntimeAPI().getLightProcessInstances(createdUUID);
    assertEquals(1, createdInstances.size());
    final LightProcessInstance createdInstance = createdInstances.iterator().next();
    final ProcessInstanceUUID createdInstanceUUID = createdInstance.getUUID();
    assertEquals(InstanceState.STARTED, createdInstance.getInstanceState());

    final Set<LightActivityInstance> activities = getQueryRuntimeAPI().getLightActivityInstances(createdInstanceUUID);
    assertEquals(2, activities.size());
    final Set<ActivityDefinitionUUID> activityDefinitionUUIDs = new HashSet<ActivityDefinitionUUID>();

    for (final LightActivityInstance activity : activities) {
      activityDefinitionUUIDs.add(activity.getActivityDefinitionUUID());
    }

    final Set<ActivityDefinitionUUID> expectedActivityUUIDs = new HashSet<ActivityDefinitionUUID>();
    expectedActivityUUIDs.add(created.getActivity("startEvent").getUUID());
    expectedActivityUUIDs.add(created.getActivity("human2").getUUID());

    assertEquals(activityDefinitionUUIDs, expectedActivityUUIDs);

    getManagementAPI().deleteProcess(createdUUID);
    getManagementAPI().deleteProcess(creatorUUID);
  }

  public void testStartEventOnlyStartReceiveEventInitialActivityUsingProcessVariable() throws Exception {
    ProcessDefinition creator = ProcessBuilder.createProcess("creator", "1.0").addHuman(getLogin())
        .addStringData("val", "1").addSendEventTask("send").addOutgoingEvent("createProcess", null, null, null)
        .addMessageCorrelationKey("id", "${val}").done();

    ProcessDefinition created = ProcessBuilder.createProcess("created", "1.0").addHuman(getLogin())
        .addStringData("val", "1").addReceiveEventTask("startEvent", "createProcess")
        .addMessageCorrelationKey("id", "${val}").addSystemTask("startAuto").addHumanTask("human1", getLogin())
        .addHumanTask("human2", getLogin()).addTransition("startAuto", "human1").addTransition("startEvent", "human2")
        .done();

    creator = getManagementAPI().deploy(getBusinessArchive(creator));
    created = getManagementAPI().deploy(getBusinessArchive(created));

    final ProcessDefinitionUUID creatorUUID = creator.getUUID();
    final ProcessDefinitionUUID createdUUID = created.getUUID();
    final ProcessInstanceUUID creatorInstanceUUID = getRuntimeAPI().instantiateProcess(creatorUUID);

    assertEquals("Creator process instance is started", InstanceState.FINISHED, getQueryRuntimeAPI()
        .getProcessInstance(creatorInstanceUUID).getInstanceState());

    waitForCreation(1, createdUUID);

    final Set<LightProcessInstance> createdInstances = getQueryRuntimeAPI().getLightProcessInstances(createdUUID);
    assertEquals(1, createdInstances.size());
    final LightProcessInstance createdInstance = createdInstances.iterator().next();
    final ProcessInstanceUUID createdInstanceUUID = createdInstance.getUUID();
    assertEquals(InstanceState.STARTED, createdInstance.getInstanceState());

    final Set<LightActivityInstance> activities = getQueryRuntimeAPI().getLightActivityInstances(createdInstanceUUID);
    assertEquals(2, activities.size());
    final Set<ActivityDefinitionUUID> activityDefinitionUUIDs = new HashSet<ActivityDefinitionUUID>();

    for (final LightActivityInstance activity : activities) {
      activityDefinitionUUIDs.add(activity.getActivityDefinitionUUID());
    }

    final Set<ActivityDefinitionUUID> expectedActivityUUIDs = new HashSet<ActivityDefinitionUUID>();
    expectedActivityUUIDs.add(created.getActivity("startEvent").getUUID());
    expectedActivityUUIDs.add(created.getActivity("human2").getUUID());

    assertEquals(activityDefinitionUUIDs, expectedActivityUUIDs);

    getManagementAPI().deleteProcess(createdUUID);
    getManagementAPI().deleteProcess(creatorUUID);
  }

  public void testStartEventOnlyStartReceiveEventInitialActivityWith2CorrelationKeys() throws Exception {
    ProcessDefinition creator = ProcessBuilder.createProcess("creator", "1.0").addHuman(getLogin())
        .addSendEventTask("send").addOutgoingEvent("createProcess", null, null, null)
        .addMessageCorrelationKey("id", "${1}").addMessageCorrelationKey("nb", "${2}").done();

    ProcessDefinition created = ProcessBuilder.createProcess("created", "1.0").addHuman(getLogin())
        .addReceiveEventTask("startEvent", "createProcess").addMessageCorrelationKey("id", "${1}")
        .addMessageCorrelationKey("nb", "${2}").addSystemTask("startAuto").addHumanTask("human1", getLogin())
        .addHumanTask("human2", getLogin()).addTransition("startAuto", "human1").addTransition("startEvent", "human2")
        .done();

    creator = getManagementAPI().deploy(getBusinessArchive(creator));
    created = getManagementAPI().deploy(getBusinessArchive(created));

    final ProcessDefinitionUUID creatorUUID = creator.getUUID();
    final ProcessDefinitionUUID createdUUID = created.getUUID();
    final ProcessInstanceUUID creatorInstanceUUID = getRuntimeAPI().instantiateProcess(creatorUUID);

    assertEquals("Creator process instance is started", InstanceState.FINISHED, getQueryRuntimeAPI()
        .getProcessInstance(creatorInstanceUUID).getInstanceState());

    waitForCreation(1, createdUUID);

    final Set<LightProcessInstance> createdInstances = getQueryRuntimeAPI().getLightProcessInstances(createdUUID);
    assertEquals(1, createdInstances.size());
    final LightProcessInstance createdInstance = createdInstances.iterator().next();
    final ProcessInstanceUUID createdInstanceUUID = createdInstance.getUUID();
    assertEquals(InstanceState.STARTED, createdInstance.getInstanceState());

    final Set<LightActivityInstance> activities = getQueryRuntimeAPI().getLightActivityInstances(createdInstanceUUID);
    assertEquals(2, activities.size());
    final Set<ActivityDefinitionUUID> activityDefinitionUUIDs = new HashSet<ActivityDefinitionUUID>();

    for (final LightActivityInstance activity : activities) {
      activityDefinitionUUIDs.add(activity.getActivityDefinitionUUID());
    }

    final Set<ActivityDefinitionUUID> expectedActivityUUIDs = new HashSet<ActivityDefinitionUUID>();
    expectedActivityUUIDs.add(created.getActivity("startEvent").getUUID());
    expectedActivityUUIDs.add(created.getActivity("human2").getUUID());

    assertEquals(activityDefinitionUUIDs, expectedActivityUUIDs);

    getManagementAPI().deleteProcess(createdUUID);
    getManagementAPI().deleteProcess(creatorUUID);
  }

  public void testStartEventOnlyStartReceiveEventInitialActivityWith3CorrelationKeys() throws Exception {
    ProcessDefinition creator = ProcessBuilder.createProcess("creator", "1.0").addHuman(getLogin())
        .addSendEventTask("send").addOutgoingEvent("createProcess", null, null, null)
        .addMessageCorrelationKey("id", "${1}").addMessageCorrelationKey("nb", "${2}")
        .addMessageCorrelationKey("present", "${\"yes\"}").done();

    ProcessDefinition created = ProcessBuilder.createProcess("created", "1.0").addHuman(getLogin())
        .addReceiveEventTask("startEvent", "createProcess").addMessageCorrelationKey("id", "${1}")
        .addMessageCorrelationKey("nb", "${2}").addMessageCorrelationKey("present", "${\"yes\"}")
        .addSystemTask("startAuto").addHumanTask("human1", getLogin()).addHumanTask("human2", getLogin())
        .addTransition("startAuto", "human1").addTransition("startEvent", "human2").done();

    creator = getManagementAPI().deploy(getBusinessArchive(creator));
    created = getManagementAPI().deploy(getBusinessArchive(created));

    final ProcessDefinitionUUID creatorUUID = creator.getUUID();
    final ProcessDefinitionUUID createdUUID = created.getUUID();
    final ProcessInstanceUUID creatorInstanceUUID = getRuntimeAPI().instantiateProcess(creatorUUID);

    assertEquals("Creator process instance is started", InstanceState.FINISHED, getQueryRuntimeAPI()
        .getProcessInstance(creatorInstanceUUID).getInstanceState());

    waitForCreation(1, createdUUID);

    final Set<LightProcessInstance> createdInstances = getQueryRuntimeAPI().getLightProcessInstances(createdUUID);
    assertEquals(1, createdInstances.size());
    final LightProcessInstance createdInstance = createdInstances.iterator().next();
    final ProcessInstanceUUID createdInstanceUUID = createdInstance.getUUID();
    assertEquals(InstanceState.STARTED, createdInstance.getInstanceState());

    final Set<LightActivityInstance> activities = getQueryRuntimeAPI().getLightActivityInstances(createdInstanceUUID);
    assertEquals(2, activities.size());
    final Set<ActivityDefinitionUUID> activityDefinitionUUIDs = new HashSet<ActivityDefinitionUUID>();

    for (final LightActivityInstance activity : activities) {
      activityDefinitionUUIDs.add(activity.getActivityDefinitionUUID());
    }

    final Set<ActivityDefinitionUUID> expectedActivityUUIDs = new HashSet<ActivityDefinitionUUID>();
    expectedActivityUUIDs.add(created.getActivity("startEvent").getUUID());
    expectedActivityUUIDs.add(created.getActivity("human2").getUUID());

    assertEquals(activityDefinitionUUIDs, expectedActivityUUIDs);

    getManagementAPI().deleteProcess(createdUUID);
    getManagementAPI().deleteProcess(creatorUUID);
  }

  public void testStartEventOnlyStartReceiveEventInitialActivityWith4CorrelationKeys() throws Exception {
    ProcessDefinition creator = ProcessBuilder.createProcess("creator", "1.0").addHuman(getLogin())
        .addSendEventTask("send").addOutgoingEvent("createProcess", null, null, null)
        .addMessageCorrelationKey("id", "${1}").addMessageCorrelationKey("nb", "${2}")
        .addMessageCorrelationKey("present", "${\"yes\"}").addMessageCorrelationKey("bool", "${true}").done();

    ProcessDefinition created = ProcessBuilder.createProcess("created", "1.0").addHuman(getLogin())
        .addReceiveEventTask("startEvent", "createProcess").addMessageCorrelationKey("id", "${1}")
        .addMessageCorrelationKey("nb", "${2}").addMessageCorrelationKey("present", "${\"yes\"}")
        .addMessageCorrelationKey("bool", "${true}").addSystemTask("startAuto").addHumanTask("human1", getLogin())
        .addHumanTask("human2", getLogin()).addTransition("startAuto", "human1").addTransition("startEvent", "human2")
        .done();

    creator = getManagementAPI().deploy(getBusinessArchive(creator));
    created = getManagementAPI().deploy(getBusinessArchive(created));

    final ProcessDefinitionUUID creatorUUID = creator.getUUID();
    final ProcessDefinitionUUID createdUUID = created.getUUID();
    final ProcessInstanceUUID creatorInstanceUUID = getRuntimeAPI().instantiateProcess(creatorUUID);

    assertEquals("Creator process instance is started", InstanceState.FINISHED, getQueryRuntimeAPI()
        .getProcessInstance(creatorInstanceUUID).getInstanceState());

    waitForCreation(1, createdUUID);

    final Set<LightProcessInstance> createdInstances = getQueryRuntimeAPI().getLightProcessInstances(createdUUID);
    assertEquals(1, createdInstances.size());
    final LightProcessInstance createdInstance = createdInstances.iterator().next();
    final ProcessInstanceUUID createdInstanceUUID = createdInstance.getUUID();
    assertEquals(InstanceState.STARTED, createdInstance.getInstanceState());

    final Set<LightActivityInstance> activities = getQueryRuntimeAPI().getLightActivityInstances(createdInstanceUUID);
    assertEquals(2, activities.size());
    final Set<ActivityDefinitionUUID> activityDefinitionUUIDs = new HashSet<ActivityDefinitionUUID>();

    for (final LightActivityInstance activity : activities) {
      activityDefinitionUUIDs.add(activity.getActivityDefinitionUUID());
    }

    final Set<ActivityDefinitionUUID> expectedActivityUUIDs = new HashSet<ActivityDefinitionUUID>();
    expectedActivityUUIDs.add(created.getActivity("startEvent").getUUID());
    expectedActivityUUIDs.add(created.getActivity("human2").getUUID());

    assertEquals(activityDefinitionUUIDs, expectedActivityUUIDs);

    getManagementAPI().deleteProcess(createdUUID);
    getManagementAPI().deleteProcess(creatorUUID);
  }

  public void testStartEventOnlyStartReceiveEventInitialActivityWith5CorrelationKeys() throws Exception {
    ProcessDefinition creator = ProcessBuilder.createProcess("creator", "1.0")
        .addHuman(getLogin())
        .addSendEventTask("send")
          .addOutgoingEvent("createProcess", null, null, null)
            .addMessageCorrelationKey("id", "${1}")
            .addMessageCorrelationKey("nb", "${2}")
            .addMessageCorrelationKey("present", "${\"yes\"}")
            .addMessageCorrelationKey("bool", "${true}")
            .addMessageCorrelationKey("idbis", "${45}")
    .done();

    ProcessDefinition created = ProcessBuilder.createProcess("created", "1.0")
        .addHuman(getLogin())
        .addReceiveEventTask("startEvent", "createProcess")
          .addMessageCorrelationKey("id", "${1}")
          .addMessageCorrelationKey("nb", "${2}")
          .addMessageCorrelationKey("present", "${\"yes\"}")
          .addMessageCorrelationKey("bool", "${true}")
          .addMessageCorrelationKey("idbis", "${45}")
        .addSystemTask("startAuto")
        .addHumanTask("human1", getLogin())
        .addHumanTask("human2", getLogin())
        .addTransition("startAuto", "human1")
        .addTransition("startEvent", "human2")
    .done();

    creator = getManagementAPI().deploy(getBusinessArchive(creator));
    created = getManagementAPI().deploy(getBusinessArchive(created));

    final ProcessDefinitionUUID creatorUUID = creator.getUUID();
    final ProcessDefinitionUUID createdUUID = created.getUUID();
    final ProcessInstanceUUID creatorInstanceUUID = getRuntimeAPI().instantiateProcess(creatorUUID);

    assertEquals("Creator process instance is started", InstanceState.FINISHED, getQueryRuntimeAPI()
        .getProcessInstance(creatorInstanceUUID).getInstanceState());

    waitForCreation(1, createdUUID);

    final Set<LightProcessInstance> createdInstances = getQueryRuntimeAPI().getLightProcessInstances(createdUUID);
    assertEquals(1, createdInstances.size());
    final LightProcessInstance createdInstance = createdInstances.iterator().next();
    final ProcessInstanceUUID createdInstanceUUID = createdInstance.getUUID();
    assertEquals(InstanceState.STARTED, createdInstance.getInstanceState());

    final Set<LightActivityInstance> activities = getQueryRuntimeAPI().getLightActivityInstances(createdInstanceUUID);
    assertEquals(2, activities.size());
    final Set<ActivityDefinitionUUID> activityDefinitionUUIDs = new HashSet<ActivityDefinitionUUID>();

    for (final LightActivityInstance activity : activities) {
      activityDefinitionUUIDs.add(activity.getActivityDefinitionUUID());
    }

    final Set<ActivityDefinitionUUID> expectedActivityUUIDs = new HashSet<ActivityDefinitionUUID>();
    expectedActivityUUIDs.add(created.getActivity("startEvent").getUUID());
    expectedActivityUUIDs.add(created.getActivity("human2").getUUID());

    assertEquals(activityDefinitionUUIDs, expectedActivityUUIDs);

    getManagementAPI().deleteProcess(createdUUID);
    getManagementAPI().deleteProcess(creatorUUID);
  }

  public void testMessageBoundaryEventAfterMessageThrowing() throws Exception {
    ProcessDefinition process = ProcessBuilder.createProcess("messageBoundary", "1.3.4")
      .addHuman(getLogin())
      .addSendEventTask("send")
        .addOutgoingEvent("event", null, null, null)
          .addMessageCorrelationKey("id", "${1}")
          .addMessageCorrelationKey("nb", "${2}")
          .addMessageCorrelationKey("present", "${\"yes\"}")
          .addMessageCorrelationKey("bool", "${true}")
          .addMessageCorrelationKey("idbis", "${45}")
      .addHumanTask("boundary", getLogin())
        .addMessageBoundaryEvent("event", null)
          .addMessageCorrelationKey("id", "${1}")
          .addMessageCorrelationKey("nb", "${2}")
          .addMessageCorrelationKey("present", "${\"yes\"}")
          .addMessageCorrelationKey("bool", "${true}")
          .addMessageCorrelationKey("idbis", "${45}")
      .addHumanTask("normal", getLogin())
      .addHumanTask("exception", getLogin())
      .addTransition("send", "boundary")
      .addExceptionTransition("boundary", "event", "exception")
      .addTransition("boundary", "normal")
    .done();

    process = getManagementAPI().deploy(getBusinessArchive(process));
    final ProcessDefinitionUUID processUUID = process.getUUID();
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);
    Thread.sleep(5500);
    final Set<LightActivityInstance> activities = getQueryRuntimeAPI().getLightActivityInstances(instanceUUID);
    assertEquals(3, activities.size());
    for (final LightActivityInstance activity : activities) {
      if ("send".equals(activity.getActivityName())) {
        assertEquals(ActivityState.FINISHED, activity.getState());
      } else if ("boundary".equals(activity.getActivityName())) {
        assertEquals(ActivityState.ABORTED, activity.getState());
      } else {
        assertEquals("exception", activity.getActivityName());
        assertEquals(ActivityState.READY, activity.getState());
      }
    }

    final Collection<LightTaskInstance> tasks = getQueryRuntimeAPI().getLightTaskList(ActivityState.READY);
    assertEquals(1, tasks.size());
    final LightTaskInstance task = tasks.iterator().next();
    assertEquals("exception", task.getActivityName());

    getManagementAPI().deleteProcess(processUUID);
  }

  public void testStartMessageEventSubProcess() throws Exception {
    final ProcessDefinition messageProcess = ProcessBuilder.createProcess("signal", "1.5")
      .addHuman(getLogin())
      .addHumanTask("start", getLogin())
      .addSendEventTask("msg")
        .addOutgoingEvent("go", "ESB", "start", null)
          .addMessageCorrelationKey("id", "${1}")
          .addMessageCorrelationKey("nb", "${2}")
          .addMessageCorrelationKey("present", "${\"yes\"}")
          .addMessageCorrelationKey("bool", "${true}")
          .addMessageCorrelationKey("idbis", "${45}")
      .addTransition("start", "msg")
    .done();

    final ProcessDefinition eventSubProcess = ProcessBuilder.createProcess("ESB", "1.0")
      .setEventSubProcess()
      .addReceiveEventTask("start", "go")
        .addMessageCorrelationKey("id", "${1}")
        .addMessageCorrelationKey("nb", "${2}")
        .addMessageCorrelationKey("present", "${\"yes\"}")
        .addMessageCorrelationKey("bool", "${true}")
        .addMessageCorrelationKey("idbis", "${45}")
      .addHuman(getLogin())
      .addHumanTask("event", getLogin())
      .addTransition("start", "event")
    .done();

    final ProcessDefinition process = ProcessBuilder.createProcess("process", "1.0")
      .addHuman(getLogin())
      .addStringData("name", "bonita")
      .addHumanTask("wait", getLogin())
      .addEventSubProcess("ESB", "1.0")
    .done();

    getManagementAPI().deploy(getBusinessArchive(eventSubProcess));
    getManagementAPI().deploy(getBusinessArchive(process));
    getManagementAPI().deploy(getBusinessArchive(messageProcess));

    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(process.getUUID());

    final ProcessInstanceUUID signalUUID = getRuntimeAPI().instantiateProcess(messageProcess.getUUID());
    executeTask(signalUUID, "start");
    waitForInstance(4000, 50, instanceUUID, InstanceState.ABORTED);

    final Set<ProcessInstance> eventSubProcesses = getQueryRuntimeAPI().getProcessInstances(eventSubProcess.getUUID());
    assertEquals(1, eventSubProcesses.size());
    final ProcessInstanceUUID eventSubProcessUUID = eventSubProcesses.iterator().next().getUUID();

    final Set<TaskInstance> tasks = getQueryRuntimeAPI().getTasks(eventSubProcessUUID);
    assertEquals(1, tasks.size());
    assertEquals("event", tasks.iterator().next().getActivityName());

    final ProcessInstance instance = getQueryRuntimeAPI().getProcessInstance(instanceUUID);
    assertEquals(InstanceState.ABORTED, instance.getInstanceState());

    getManagementAPI().deleteProcess(process.getUUID());
    getManagementAPI().deleteProcess(eventSubProcess.getUUID());
    getManagementAPI().deleteProcess(messageProcess.getUUID());
  }

  public void testProcThrowingMsgToSubProcFinishesIfCorrelationDontMatch() throws Exception {

    final ProcessDefinition eventSubProcess = ProcessBuilder.createProcess("ESB", "1.0")
        .setEventSubProcess()
        .addReceiveEventTask("start", "go")
        .addMessageCorrelationKey("id", "${1}") //correlation that doesn't match
        .addHuman(getLogin())
        .addHumanTask("event", getLogin())
        .addTransition("start", "event")
        .done();
    
    final ProcessDefinition process = ProcessBuilder.createProcess("process", "1.0")
        .addStringData("name", "bonita")
        .addSystemTask("start")
        .addSendEventTask("msg")
        .addOutgoingEvent("go", "ESB", "start", null)
        .addMessageCorrelationKey("id", "${2}") //correlation that doesn't match
        .addEventSubProcess("ESB", "1.0")
        .addTransition("start", "msg")
        .done();
    
    getManagementAPI().deploy(getBusinessArchive(eventSubProcess));
    getManagementAPI().deploy(getBusinessArchive(process));
    
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(process.getUUID());
    waitForInstance(4000, 50, instanceUUID, InstanceState.FINISHED); // as the correlation doesn't match the process must finish
    
    final Set<ProcessInstance> eventSubProcesses = getQueryRuntimeAPI().getProcessInstances(eventSubProcess.getUUID());
    assertEquals(0, eventSubProcesses.size());
    
    getManagementAPI().deleteProcess(process.getUUID());
    getManagementAPI().deleteProcess(eventSubProcess.getUUID());
  }

  public void testProcThrowingMsgToSubProc() throws Exception {
    
    final ProcessDefinition eventSubProcess = ProcessBuilder.createProcess("ESB", "1.0")
        .setEventSubProcess()
        .addReceiveEventTask("start", "go")
        .addMessageCorrelationKey("id", "${5}")
        .addHuman(getLogin())
        .addHumanTask("event", getLogin())
        .addTransition("start", "event")
        .done();
    
    final ProcessDefinition process = ProcessBuilder.createProcess("process", "1.0")
        .addStringData("name", "bonita")
        .addSystemTask("start")
        .addSendEventTask("msg")
        .addOutgoingEvent("go", "ESB", "start", null)
        .addMessageCorrelationKey("id", "${5}")
        .addEventSubProcess("ESB", "1.0")
        .addTransition("start", "msg")
        .done();
    
    getManagementAPI().deploy(getBusinessArchive(eventSubProcess));
    getManagementAPI().deploy(getBusinessArchive(process));
    
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(process.getUUID());
    waitForInstance(4000, 50, instanceUUID, InstanceState.ABORTED); // as the correlation doesn't match the process must finish
    
    final Set<ProcessInstance> eventSubProcesses = getQueryRuntimeAPI().getProcessInstances(eventSubProcess.getUUID());
    assertEquals(1, eventSubProcesses.size());
    
    final ProcessInstanceUUID eventSubProcessUUID = eventSubProcesses.iterator().next().getUUID();
    final Set<TaskInstance> tasks = getQueryRuntimeAPI().getTasks(eventSubProcessUUID);
    assertEquals(1, tasks.size());
    assertEquals("event", tasks.iterator().next().getActivityName());
    
    getManagementAPI().deleteProcess(process.getUUID());
    getManagementAPI().deleteProcess(eventSubProcess.getUUID());
  }

  public void testStartMessageEventSubProcessWithProcessVariables() throws Exception {
    final ProcessDefinition sendMessageProcess = ProcessBuilder.createProcess("sendMessageProc", "1.5")
        .addIntegerData("var", 11)
        .addHuman(getLogin())
        .addHumanTask("start", getLogin())
        .addSendEventTask("msg")
        .addOutgoingEvent("go", "ESB", "start", null)
        .addMessageCorrelationKey("id", "${var}")
        .addTransition("start", "msg")
        .done();
    
    final ProcessDefinition eventSubProcess = ProcessBuilder.createProcess("ESB", "1.0")
        .setEventSubProcess()
        .addReceiveEventTask("start", "go")
        .addMessageCorrelationKey("id", "${intVar}")
        .addHumanTask("event", getLogin())
        .addTransition("start", "event")
        .done();
    
    final ProcessDefinition process = ProcessBuilder.createProcess("process", "1.0")
        .addIntegerData("intVar", 10)
        .addHuman(getLogin())
        .addStringData("name", "bonita")
        .addHumanTask("wait", getLogin())
        .addEventSubProcess("ESB", "1.0")
        .done();
    
    getManagementAPI().deploy(getBusinessArchive(eventSubProcess));
    getManagementAPI().deploy(getBusinessArchive(process));
    getManagementAPI().deploy(getBusinessArchive(sendMessageProcess));
    
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(process.getUUID(), Collections.<String, Object> singletonMap("intVar", 11));
    
    final ProcessInstanceUUID sendMessageProcInstUUID = getRuntimeAPI().instantiateProcess(sendMessageProcess.getUUID());
    executeTask(sendMessageProcInstUUID, "start");
    waitForInstance(4000, 50, instanceUUID, InstanceState.ABORTED);
    
    final Set<ProcessInstance> eventSubProcesses = getQueryRuntimeAPI().getProcessInstances(eventSubProcess.getUUID());
    assertEquals(1, eventSubProcesses.size());
    final ProcessInstanceUUID eventSubProcessUUID = eventSubProcesses.iterator().next().getUUID();
    
    final Set<TaskInstance> tasks = getQueryRuntimeAPI().getTasks(eventSubProcessUUID);
    assertEquals(1, tasks.size());
    assertEquals("event", tasks.iterator().next().getActivityName());
    
    final LightProcessInstance instance = getQueryRuntimeAPI().getLightProcessInstance(instanceUUID);
    assertEquals(InstanceState.ABORTED, instance.getInstanceState());
    
    final LightProcessInstance sendMessageProcInst = getQueryRuntimeAPI().getLightProcessInstance(sendMessageProcInstUUID);
    assertEquals(InstanceState.FINISHED, sendMessageProcInst.getInstanceState());
    
    getManagementAPI().deleteProcess(process.getUUID());
    getManagementAPI().deleteProcess(eventSubProcess.getUUID());
    getManagementAPI().deleteProcess(sendMessageProcess.getUUID());
  }

  public void testEventBetweenProcessActivities() throws Exception {
    ProcessDefinition sendProcess = ProcessBuilder.createProcess("sendProcess", "d")
      .addSendEventTask("sendEvent")
        .addOutgoingEvent("event", "receiveProcess", "receiveEvent", null)
          .addMessageCorrelationKey("id", "${1}")
          .addMessageCorrelationKey("nb", "${2}")
          .addMessageCorrelationKey("present", "${\"yes\"}")
          .addMessageCorrelationKey("bool", "${true}")
          .addMessageCorrelationKey("idbis", "${45}")
    .done();

    ProcessDefinition receiveProcess = ProcessBuilder.createProcess("receiveProcess", "d")
      .addSystemTask("initial")
      .addReceiveEventTask("receiveEvent", "event")
        .addMessageCorrelationKey("id", "${1}")
        .addMessageCorrelationKey("nb", "${2}")
        .addMessageCorrelationKey("present", "${\"yes\"}")
        .addMessageCorrelationKey("bool", "${true}")
        .addMessageCorrelationKey("idbis", "${45}")
      .addTransition("initial", "receiveEvent")
    .done();

    sendProcess = getManagementAPI().deploy(getBusinessArchive(sendProcess));
    receiveProcess = getManagementAPI().deploy(getBusinessArchive(receiveProcess));

    final ProcessInstanceUUID receiveInstanceUUID = getRuntimeAPI().instantiateProcess(receiveProcess.getUUID());
    //check instance is waiting
    assertEquals(InstanceState.STARTED, getQueryRuntimeAPI().getProcessInstance(receiveInstanceUUID).getInstanceState());

    final ProcessInstanceUUID sendInstanceUUID = getRuntimeAPI().instantiateProcess(sendProcess.getUUID());
    //check instance is finished
    assertEquals(InstanceState.FINISHED, getQueryRuntimeAPI().getProcessInstance(sendInstanceUUID).getInstanceState());
    checkExecutedOnce(sendInstanceUUID, "sendEvent");

    this.waitForInstanceEnd(10000, 10, receiveInstanceUUID);

    assertEquals(InstanceState.FINISHED, getQueryRuntimeAPI().getProcessInstance(receiveInstanceUUID).getInstanceState());
    checkExecutedOnce(receiveInstanceUUID, "receiveEvent");

    getManagementAPI().deleteProcess(receiveProcess.getUUID());
    getManagementAPI().deleteProcess(sendProcess.getUUID());
  }
  
  public void testOneOutgoingMatchingWithTwoIncommings() throws Exception {
    ProcessDefinition sendProcess = ProcessBuilder.createProcess("sendProcess", "d")
      .addSendEventTask("sendEvent")
        .addOutgoingEvent("event", "receiveProcess")
    .done();

    ProcessDefinition receiveProcess = ProcessBuilder.createProcess("receiveProcess", "d")
      .addSystemTask("initial")
      .addReceiveEventTask("receiveEvent1", "event")
      .addReceiveEventTask("receiveEvent2", "event", "true")
      .addSystemTask("end")
        .addJoinType(JoinType.AND)
      .addTransition("initial", "receiveEvent1")
      .addTransition("initial", "receiveEvent2")
      .addTransition("receiveEvent1", "end")
      .addTransition("receiveEvent2", "end")
    .done();

    sendProcess = getManagementAPI().deploy(getBusinessArchive(sendProcess));
    receiveProcess = getManagementAPI().deploy(getBusinessArchive(receiveProcess));
    
    final int nbOfReceive = 3;


    for (int i = 0; i < 2 * nbOfReceive; i++) {
      final ProcessInstanceUUID sendInstanceUUID = getRuntimeAPI().instantiateProcess(sendProcess.getUUID());
      //check instance is finished
      assertEquals(InstanceState.FINISHED, getQueryRuntimeAPI().getProcessInstance(sendInstanceUUID).getInstanceState());
      checkExecutedOnce(sendInstanceUUID, "sendEvent");
    }

    final List<ProcessInstanceUUID> receiveUUIDs = new ArrayList<ProcessInstanceUUID>(nbOfReceive);

    for (int i = 0; i < nbOfReceive; i++) {
      final ProcessInstanceUUID receiveInstanceUUID = getRuntimeAPI().instantiateProcess(receiveProcess.getUUID());
      receiveUUIDs.add(receiveInstanceUUID);
    }
    
    for (final ProcessInstanceUUID receiveInstanceUUID : receiveUUIDs) {
      this.waitForInstanceEnd(30000, 10, receiveInstanceUUID);
      
      assertEquals(InstanceState.FINISHED, getQueryRuntimeAPI().getProcessInstance(receiveInstanceUUID).getInstanceState());
      checkExecutedOnce(receiveInstanceUUID, "receiveEvent1");
      checkExecutedOnce(receiveInstanceUUID, "receiveEvent2");
    }

    getManagementAPI().deleteProcess(receiveProcess.getUUID());
    getManagementAPI().deleteProcess(sendProcess.getUUID());
  }
  

}
