package org.ow2.bonita.event;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.bonitasoft.connectors.bonita.SetVarConnector;
import org.ow2.bonita.APITestCase;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.exception.EventNotFoundException;
import org.ow2.bonita.facade.runtime.ActivityInstance;
import org.ow2.bonita.facade.runtime.ActivityState;
import org.ow2.bonita.facade.runtime.CatchingEvent;
import org.ow2.bonita.facade.runtime.CatchingEvent.Position;
import org.ow2.bonita.facade.runtime.CatchingEvent.Type;
import org.ow2.bonita.facade.runtime.InstanceState;
import org.ow2.bonita.facade.uuid.CatchingEventUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.light.LightProcessInstance;
import org.ow2.bonita.light.LightTaskInstance;
import org.ow2.bonita.util.ProcessBuilder;

public class EventManagementTest extends APITestCase {

  public void testGetEvents() throws Exception {
    ProcessDefinition definition1 = ProcessBuilder.createProcess("startTimer", "1.30")
    .addTimerTask("start", "${new Date(System.currentTimeMillis() + 1520000)}")
    .addSystemTask("end")
    .addTransition("start", "end")
    .done();

    getManagementAPI().deploy(getBusinessArchive(definition1));

    ProcessDefinition definition2 = ProcessBuilder.createProcess("intermediateTimer", "1.2")
    .addSystemTask("start")
    .addTimerTask("intermediate", "15000000")
    .addSystemTask("end")
    .addTransition("start", "intermediate")
    .addTransition("intermediate", "end")
    .done();
    getManagementAPI().deploy(getBusinessArchive(definition2));

    ProcessDefinition definition3 = ProcessBuilder.createProcess("deadline", "3.5")
    .addHuman(getLogin())
    .addHumanTask("step", getLogin())
      .addDeadline("1500000", SetVarConnector.class.getName())
    .addSystemTask("start")
    .addSystemTask("end")
    .addTransition("start", "step")
    .addTransition("step", "end")
    .done();
    getManagementAPI().deploy(getBusinessArchive(definition3, null, SetVarConnector.class));

    ProcessDefinition definition4 = ProcessBuilder.createProcess("boundary", "1.0")
    .addHuman(getLogin())
    .addHumanTask("start", getLogin())
    .addTimerBoundaryEvent("boundary", "150000")
    .addSystemTask("error")
    .addExceptionTransition("start", "boundary", "error")
    .done();
    getManagementAPI().deploy(getBusinessArchive(definition4));

    ProcessInstanceUUID instanceUUID2 = getRuntimeAPI().instantiateProcess(definition2.getUUID());
    getRuntimeAPI().instantiateProcess(definition3.getUUID());
    getRuntimeAPI().instantiateProcess(definition4.getUUID());

    Date now = new Date();
    Set<CatchingEvent> events = getQueryRuntimeAPI().getEvents();
    assertEquals(4, events.size());
    for (CatchingEvent catchingEvent : events) {
      assertTrue(now.before(catchingEvent.getExecutionDate()));
    }

    events = getQueryRuntimeAPI().getEvents(instanceUUID2);
    assertEquals(1, events.size());

    getManagementAPI().deleteProcess(definition1.getUUID());
    getManagementAPI().deleteProcess(definition2.getUUID());
    getManagementAPI().deleteProcess(definition3.getUUID());
    getManagementAPI().deleteProcess(definition4.getUUID());
  }

  public void testGetEvent() throws Exception {
    ProcessDefinition definition = ProcessBuilder.createProcess("intermediateTimer", "1.2")
    .addSystemTask("start")
    .addTimerTask("intermediate", "15000000")
    .addSystemTask("end")
    .addTransition("start", "intermediate")
    .addTransition("intermediate", "end")
    .done();

    getManagementAPI().deploy(getBusinessArchive(definition));
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(definition.getUUID());

    Set<CatchingEvent> events = getQueryRuntimeAPI().getEvents(instanceUUID);
    assertEquals(1, events.size());
    CatchingEvent event = events.iterator().next();
    event = getQueryRuntimeAPI().getEvent(event.getUUID());
    assertNotNull(event);
    getRuntimeAPI().deleteEvent(event.getUUID());
    try {
      getQueryRuntimeAPI().getEvent(event.getUUID());
      fail("The event does not exist anymore.");
    } catch (EventNotFoundException e) {
      // TODO: handle exception
    } 
    getManagementAPI().deleteProcess(definition.getUUID());
  }
  
  public void testExecuteTimerNow() throws Exception {
    ProcessDefinition definition = ProcessBuilder.createProcess("intermediateTimer", "1.2")
    .addSystemTask("start")
    .addTimerTask("intermediate", "15000000")
    .addSystemTask("end")
    .addTransition("start", "intermediate")
    .addTransition("intermediate", "end")
    .done();
    getManagementAPI().deploy(getBusinessArchive(definition));
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(definition.getUUID());

    Set<ActivityInstance> activities = getQueryRuntimeAPI().getActivityInstances(instanceUUID, "intermediate");
    assertEquals(1, activities.size());
    ActivityInstance activity = activities.iterator().next();
    assertEquals(ActivityState.READY, activity.getState());
    Set<CatchingEvent> events = getQueryRuntimeAPI().getEvents(activity.getUUID());
    assertEquals(1, events.size());
    CatchingEvent event = events.iterator().next(); 
    assertEquals(Type.TIMER, event.getType());
    assertEquals(Position.INTERMEDIATE, event.getPosition());

    getRuntimeAPI().executeEvent(event.getUUID());
    waitForInstanceEnd(5000, 50, instanceUUID);

    LightProcessInstance instance = getQueryRuntimeAPI().getLightProcessInstance(instanceUUID);
    assertEquals(InstanceState.FINISHED, instance.getInstanceState());

    events = getQueryRuntimeAPI().getEvents(activity.getUUID());
    assertEquals(0, events.size());

    getManagementAPI().deleteProcess(definition.getUUID());
  }

  public void testUpdateTimerExpirationDate() throws Exception {
    ProcessDefinition definition = ProcessBuilder.createProcess("intermediateTimer", "1.2")
    .addSystemTask("start")
    .addTimerTask("intermediate", "15000000")
    .addSystemTask("end")
    .addTransition("start", "intermediate")
    .addTransition("intermediate", "end")
    .done();
    getManagementAPI().deploy(getBusinessArchive(definition));
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(definition.getUUID());

    Set<ActivityInstance> activities = getQueryRuntimeAPI().getActivityInstances(instanceUUID, "intermediate");
    assertEquals(1, activities.size());
    ActivityInstance activity = activities.iterator().next();
    assertEquals(ActivityState.READY, activity.getState());
    Set<CatchingEvent> events = getQueryRuntimeAPI().getEvents(activity.getUUID());
    assertEquals(1, events.size());
    CatchingEvent event = events.iterator().next(); 
    assertEquals(Type.TIMER, event.getType());
    assertEquals(Position.INTERMEDIATE, event.getPosition());

    getRuntimeAPI().updateExpirationDate(event.getUUID(), new Date());
    Thread.sleep(500);

    LightProcessInstance instance = getQueryRuntimeAPI().getLightProcessInstance(instanceUUID);
    assertEquals(InstanceState.FINISHED, instance.getInstanceState());

    events = getQueryRuntimeAPI().getEvents(activity.getUUID());
    assertEquals(0, events.size());

    getManagementAPI().deleteProcess(definition.getUUID());
  }

  public void testDeleteTimerEvent() throws Exception {
    ProcessDefinition definition = ProcessBuilder.createProcess("intermediateTimer", "1.2")
    .addSystemTask("start")
    .addTimerTask("intermediate", "15000000")
    .addSystemTask("end")
    .addTransition("start", "intermediate")
    .addTransition("intermediate", "end")
    .done();
    getManagementAPI().deploy(getBusinessArchive(definition));
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(definition.getUUID());

    Set<ActivityInstance> activities = getQueryRuntimeAPI().getActivityInstances(instanceUUID, "intermediate");
    assertEquals(1, activities.size());
    ActivityInstance activity = activities.iterator().next();
    assertEquals(ActivityState.READY, activity.getState());
    Set<CatchingEvent> events = getQueryRuntimeAPI().getEvents(activity.getUUID());
    assertEquals(1, events.size());
    CatchingEvent event = events.iterator().next(); 
    assertEquals(Type.TIMER, event.getType());
    assertEquals(Position.INTERMEDIATE, event.getPosition());

    getRuntimeAPI().deleteEvent(event.getUUID());
    events = getQueryRuntimeAPI().getEvents(activity.getUUID());
    assertEquals(0, events.size());

    getManagementAPI().deleteProcess(definition.getUUID());
  }

  public void testExecuteDeadlineNow() throws Exception {
    ProcessDefinition definition = ProcessBuilder.createProcess("executeDeadlineNow", "1.6")
    .addHuman(getLogin())
    .addHumanTask("step", getLogin())
      .addBooleanData("executeDeadline", false)
      .addDeadline("15000000", SetVarConnector.class.getName())
        .addInputParameter("variableName", "executeDeadline")
        .addInputParameter("value", true)
    .addSystemTask("start")
    .addSystemTask("end")
    .addTransition("start", "step")
    .addTransition("step", "end")
    .done();

    getManagementAPI().deploy(getBusinessArchive(definition, null, SetVarConnector.class));
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(definition.getUUID());

    Collection<LightTaskInstance> tasks = getQueryRuntimeAPI().getLightTaskList(instanceUUID, ActivityState.READY);
    assertEquals(1, tasks.size());
    LightTaskInstance task = tasks.iterator().next();
    Set<CatchingEvent> events = getQueryRuntimeAPI().getEvents(task.getUUID());
    assertEquals(1, events.size());
    CatchingEvent event = events.iterator().next(); 
    assertEquals(Type.TIMER, event.getType());
    assertEquals(Position.DEADLINE, event.getPosition());

    Boolean executed = (Boolean) getQueryRuntimeAPI().getActivityInstanceVariable(task.getUUID(), "executeDeadline");
    assertFalse(executed);
    getRuntimeAPI().executeEvent(event.getUUID());
    Thread.sleep(800);

    executed = (Boolean) getQueryRuntimeAPI().getActivityInstanceVariable(task.getUUID(), "executeDeadline");
    assertTrue(executed);

    events = getQueryRuntimeAPI().getEvents(task.getUUID());
    assertEquals(0, events.size());

    getManagementAPI().deleteProcess(definition.getUUID());
  }

  public void testUpdateDeadlineExpirationDate() throws Exception {
    ProcessDefinition definition = ProcessBuilder.createProcess("updateDeadlineExpirationDate", "1.6")
    .addHuman(getLogin())
    .addHumanTask("step", getLogin())
      .addBooleanData("executeDeadline", false)
      .addDeadline("15000000", SetVarConnector.class.getName())
        .addInputParameter("variableName", "executeDeadline")
        .addInputParameter("value", true)
    .addSystemTask("start")
    .addSystemTask("end")
    .addTransition("start", "step")
    .addTransition("step", "end")
    .done();

    getManagementAPI().deploy(getBusinessArchive(definition, null, SetVarConnector.class));
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(definition.getUUID());

    Collection<LightTaskInstance> tasks = getQueryRuntimeAPI().getLightTaskList(instanceUUID, ActivityState.READY);
    assertEquals(1, tasks.size());
    LightTaskInstance task = tasks.iterator().next();
    Set<CatchingEvent> events = getQueryRuntimeAPI().getEvents(task.getUUID());
    assertEquals(1, events.size());
    CatchingEvent event = events.iterator().next(); 
    assertEquals(Type.TIMER, event.getType());
    assertEquals(Position.DEADLINE, event.getPosition());

    Boolean executed = (Boolean) getQueryRuntimeAPI().getActivityInstanceVariable(task.getUUID(), "executeDeadline");
    assertFalse(executed);
    getRuntimeAPI().updateExpirationDate(event.getUUID(), new Date());
    Thread.sleep(500);

    executed = (Boolean) getQueryRuntimeAPI().getActivityInstanceVariable(task.getUUID(), "executeDeadline");
    assertTrue(executed);

    events = getQueryRuntimeAPI().getEvents(task.getUUID());
    assertEquals(0, events.size());

    getManagementAPI().deleteProcess(definition.getUUID());
  }

  public void testDeleteDeadlineEvent() throws Exception {
    ProcessDefinition definition = ProcessBuilder.createProcess("deleteDeadline", "1.6")
    .addHuman(getLogin())
    .addHumanTask("step", getLogin())
      .addDeadline("1500000", SetVarConnector.class.getName())
        .addInputParameter("variableName", "executeDeadline")
        .addInputParameter("value", true)
    .addSystemTask("start")
    .addSystemTask("end")
    .addTransition("start", "step")
    .addTransition("step", "end")
    .done();

    getManagementAPI().deploy(getBusinessArchive(definition, null, SetVarConnector.class));
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(definition.getUUID());
    Collection<LightTaskInstance> tasks = getQueryRuntimeAPI().getLightTaskList(instanceUUID, ActivityState.READY);
    assertEquals(1, tasks.size());
    LightTaskInstance task = tasks.iterator().next();
    Set<CatchingEvent> events = getQueryRuntimeAPI().getEvents(task.getUUID());
    assertEquals(1, events.size());
    CatchingEvent event = events.iterator().next(); 
    assertEquals(Type.TIMER, event.getType());
    assertEquals(Position.DEADLINE, event.getPosition());

    getRuntimeAPI().deleteEvent(event.getUUID());
    events = getQueryRuntimeAPI().getEvents(task.getUUID());
    assertEquals(0, events.size());

    getManagementAPI().deleteProcess(definition.getUUID());
  }

  public void testDeleteDeadlineEvents() throws Exception {
    ProcessDefinition definition = ProcessBuilder.createProcess("deleteDeadline", "1.6")
    .addHuman(getLogin())
    .addHumanTask("step", getLogin())
      .addDeadline("1500000", SetVarConnector.class.getName())
        .addInputParameter("variableName", "executeDeadline")
        .addInputParameter("value", true)
      .addDeadline("150000", SetVarConnector.class.getName())
        .addInputParameter("variableName", "executeDeadline")
        .addInputParameter("value", true)
    .addSystemTask("start")
    .addSystemTask("end")
    .addTransition("start", "step")
    .addTransition("step", "end")
    .done();

    getManagementAPI().deploy(getBusinessArchive(definition, null, SetVarConnector.class));
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(definition.getUUID());
    Collection<LightTaskInstance> tasks = getQueryRuntimeAPI().getLightTaskList(instanceUUID, ActivityState.READY);
    assertEquals(1, tasks.size());
    LightTaskInstance task = tasks.iterator().next();
    Set<CatchingEvent> events = getQueryRuntimeAPI().getEvents(task.getUUID());
    assertEquals(2, events.size());
    List<CatchingEventUUID> eventUUIDs = new ArrayList<CatchingEventUUID>();
    for (CatchingEvent event : events) {
      eventUUIDs.add(event.getUUID());
    }

    getRuntimeAPI().deleteEvents(eventUUIDs);
    events = getQueryRuntimeAPI().getEvents(task.getUUID());
    assertEquals(0, events.size());

    getManagementAPI().deleteProcess(definition.getUUID());
  }

}
