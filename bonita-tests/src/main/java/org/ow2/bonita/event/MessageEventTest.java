/**
 * Copyright (C) 2009  BonitaSoft S.A.
 * BonitaSoft, 31 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA  02110-1301, USA.
 **/
package org.ow2.bonita.event;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bonitasoft.connectors.bonita.SetVarConnector;
import org.ow2.bonita.APITestCase;
import org.ow2.bonita.facade.def.element.HookDefinition.Event;
import org.ow2.bonita.facade.def.majorElement.ActivityDefinition.JoinType;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.exception.DocumentationCreationException;
import org.ow2.bonita.facade.runtime.ActivityInstance;
import org.ow2.bonita.facade.runtime.ActivityState;
import org.ow2.bonita.facade.runtime.Document;
import org.ow2.bonita.facade.runtime.InstanceState;
import org.ow2.bonita.facade.runtime.TaskInstance;
import org.ow2.bonita.facade.uuid.ActivityDefinitionUUID;
import org.ow2.bonita.facade.uuid.ActivityInstanceUUID;
import org.ow2.bonita.facade.uuid.DocumentUUID;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.light.LightActivityInstance;
import org.ow2.bonita.light.LightProcessInstance;
import org.ow2.bonita.light.LightTaskInstance;
import org.ow2.bonita.search.DocumentResult;
import org.ow2.bonita.search.DocumentSearchBuilder;
import org.ow2.bonita.search.index.DocumentIndex;
import org.ow2.bonita.util.BonitaRuntimeException;
import org.ow2.bonita.util.Misc;
import org.ow2.bonita.util.ProcessBuilder;

public class MessageEventTest extends APITestCase {

  public void testInstantiateProcessWithOnlyAStartMessageEvent() throws Exception {
    ProcessDefinition created = ProcessBuilder.createProcess("created", "1.0").addHuman(getLogin())
        .addReceiveEventTask("startEvent", "createProcess").addHumanTask("human2", getLogin())
        .addTransition("startEvent", "human2").done();

    created = getManagementAPI().deploy(getBusinessArchive(created));
    final ProcessDefinitionUUID createdUUID = created.getUUID();
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(createdUUID);
    final Set<LightActivityInstance> activities = getQueryRuntimeAPI().getLightActivityInstances(instanceUUID);
    assertEquals(1, activities.size());

    getManagementAPI().deleteProcess(createdUUID);
  }

  public void testStartEventOnlyStartReceiveEventInitialActivity() throws Exception {
    ProcessDefinition creator = ProcessBuilder.createProcess("creator", "1.0").addHuman(getLogin())
        .addSendEventTask("send").addOutgoingEvent("createProcess", null, null, null).done();

    ProcessDefinition created = ProcessBuilder.createProcess("created", "1.0").addHuman(getLogin())
        .addReceiveEventTask("startEvent", "createProcess").addSystemTask("startAuto")
        .addHumanTask("human1", getLogin()).addHumanTask("human2", getLogin()).addTransition("startAuto", "human1")
        .addTransition("startEvent", "human2").done();

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

  public void testInstantiateProcessStartOnlyAutomaticInitialActivity() throws Exception {
    ProcessDefinition process = ProcessBuilder.createProcess("p", "1.0").addHuman(getLogin())
        .addReceiveEventTask("startEvent", "createProcess").addSystemTask("startAuto")
        .addHumanTask("human1", getLogin()).addHumanTask("human2", getLogin()).addTransition("startAuto", "human1")
        .addTransition("startEvent", "human2").done();

    process = getManagementAPI().deploy(getBusinessArchive(process));
    final ProcessDefinitionUUID processUUID = process.getUUID();
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);
    final LightProcessInstance instance = getQueryRuntimeAPI().getLightProcessInstance(instanceUUID);
    assertEquals(InstanceState.STARTED, instance.getInstanceState());
    assertEquals(2, getQueryRuntimeAPI().getLightActivityInstances(instanceUUID).size());
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testReceiveEventFromManySources() throws Exception {
    ProcessDefinition sendProcess = ProcessBuilder.createProcess("sendProcess", "d").addSendEventTask("sendEvent1")
        .addOutgoingEvent("event", "receiveProcess", "receiveEvent", null).addSendEventTask("sendEvent2")
        .addOutgoingEvent("event", "receiveProcess", "receiveEvent", null).addDecisionNode("join")
        .addJoinType(JoinType.AND).addTransition("sendEvent1", "join").addTransition("sendEvent2", "join").done();

    ProcessDefinition receiveProcess = ProcessBuilder.createProcess("receiveProcess", "d")
        .addReceiveEventTask("receiveEvent", "event").done();

    sendProcess = getManagementAPI().deploy(getBusinessArchive(sendProcess));
    receiveProcess = getManagementAPI().deploy(getBusinessArchive(receiveProcess));

    final ProcessInstanceUUID sendInstanceUUID = getRuntimeAPI().instantiateProcess(sendProcess.getUUID());
    // check instance is finished
    assertEquals(InstanceState.FINISHED, getQueryRuntimeAPI().getProcessInstance(sendInstanceUUID).getInstanceState());

    waitForCreation(2, receiveProcess.getUUID());

    getManagementAPI().deleteProcess(receiveProcess.getUUID());
    getManagementAPI().deleteProcess(sendProcess.getUUID());
  }

  public void testRemoveUnConsumedOutgoingEventOnDelete() throws Exception {
    ProcessDefinition process = ProcessBuilder.createProcess("aaaa", "1.0").addSendEventTask("send")
        .addOutgoingEvent("xxxxxxpppppp", null, null, null).done();

    process = getManagementAPI().deploy(getBusinessArchive(process));
    getManagementAPI().deployJar("getExecJar.jar", Misc.generateJar(GetOutgoingEventInstancesCommand.class));

    final ProcessDefinitionUUID processUUID = process.getUUID();
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);
    waitForInstanceEnd(6000, 300, instanceUUID);
    assertEquals(InstanceState.FINISHED, getQueryRuntimeAPI().getLightProcessInstance(instanceUUID).getInstanceState());

    getManagementAPI().deleteProcess(processUUID);

    // check no event is found
    assertEquals(0, getCommandAPI().execute(new GetOutgoingEventInstancesCommand(), processUUID).size());

    getManagementAPI().removeJar("getExecJar.jar");
    // need to be deployed and deleted again as execution command recreate
    // classloaders...
    getManagementAPI().deploy(getBusinessArchive(process));
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testDisable() throws Exception {
    // creator send an event to created
    ProcessDefinition creator = ProcessBuilder.createProcess("creator", "1.0").addHuman(getLogin())
        .addSendEventTask("send").addOutgoingEvent("createProcess", null, null, null).addHumanTask("t", getLogin())
        .addTransition("send", "t").done();

    ProcessDefinition created = ProcessBuilder.createProcess("created", "1.0")
        .addReceiveEventTask("receive", "createProcess").done();
    creator = getManagementAPI().deploy(getBusinessArchive(creator));
    created = getManagementAPI().deploy(getBusinessArchive(created));
    final ProcessDefinitionUUID creatorUUID = creator.getUUID();
    final ProcessDefinitionUUID createdUUID = created.getUUID();

    getManagementAPI().disable(createdUUID);

    final ProcessInstanceUUID creatorInstanceUUID = getRuntimeAPI().instantiateProcess(creatorUUID);
    assertEquals("Creator process instance is finished", InstanceState.STARTED, getQueryRuntimeAPI()
        .getProcessInstance(creatorInstanceUUID).getInstanceState());

    waitForCreation(0, createdUUID);

    getManagementAPI().enable(createdUUID);

    waitForCreation(1, createdUUID);

    getManagementAPI().deleteProcess(creatorUUID);
    getManagementAPI().deleteProcess(createdUUID);
  }

  public void testCreateInstanceEvent() throws Exception {
    // creator send an event to created
    ProcessDefinition creator = ProcessBuilder.createProcess("creator", "1.0").addHuman(getLogin())
        .addSendEventTask("send").addOutgoingEvent("createProcess", null, null, null).addHumanTask("t", getLogin())
        .addTransition("send", "t").done();

    ProcessDefinition created = ProcessBuilder.createProcess("created", "1.0")
        .addReceiveEventTask("receive", "createProcess").done();

    creator = getManagementAPI().deploy(getBusinessArchive(creator));
    created = getManagementAPI().deploy(getBusinessArchive(created));

    final ProcessDefinitionUUID creatorUUID = creator.getUUID();
    final ProcessDefinitionUUID createdUUID = created.getUUID();

    final ProcessInstanceUUID creatorInstanceUUID = getRuntimeAPI().instantiateProcess(creatorUUID);

    assertEquals("Creator process instance is finished", InstanceState.STARTED, getQueryRuntimeAPI()
        .getProcessInstance(creatorInstanceUUID).getInstanceState());

    waitForCreation(1, createdUUID);

    getManagementAPI().deleteProcess(creatorUUID);
    getManagementAPI().deleteProcess(createdUUID);
  }

  public void testCreateInstanceEventWithProcessAndActivityNames() throws Exception {
    // creator send an event to created
    ProcessDefinition creator = ProcessBuilder.createProcess("creator", "1.0").addHuman(getLogin())
        .addSendEventTask("send").addOutgoingEvent("createProcess", "created", "receive", null)
        .addHumanTask("t", getLogin()).addTransition("send", "t").done();

    ProcessDefinition created = ProcessBuilder.createProcess("created", "1.0")
        .addReceiveEventTask("receive", "createProcess").done();

    creator = getManagementAPI().deploy(getBusinessArchive(creator));
    created = getManagementAPI().deploy(getBusinessArchive(created));

    final ProcessDefinitionUUID creatorUUID = creator.getUUID();
    final ProcessDefinitionUUID createdUUID = created.getUUID();
    final ProcessInstanceUUID creatorInstanceUUID = getRuntimeAPI().instantiateProcess(creatorUUID);

    assertEquals("Creator process instance is finished", InstanceState.STARTED, getQueryRuntimeAPI()
        .getProcessInstance(creatorInstanceUUID).getInstanceState());

    waitForCreation(1, createdUUID);

    getManagementAPI().deleteProcess(creatorUUID);
    getManagementAPI().deleteProcess(createdUUID);
  }

  private void waitForCreation(final int expected, final ProcessDefinitionUUID createdUUID) throws Exception {
    final long before = System.currentTimeMillis();
    final long maxWait = 10000;
    boolean wait = true;
    do {
      Thread.sleep(500);
      final int instanceNb = getQueryRuntimeAPI().getLightProcessInstances(createdUUID).size();
      wait = instanceNb != expected && System.currentTimeMillis() - before < maxWait;
    } while (wait);

    // wait to ensure that there is no more created instances than needed
    Thread.sleep(4000);

    final int size = getQueryRuntimeAPI().getLightProcessInstances(createdUUID).size();
    assertEquals("Process was not launched " + expected + " time(s). It was launched: " + size + " times", expected,
        size);
  }

  public void testManyInstancesCreateInstanceEvent() throws Exception {
    // creator send an event to created

    ProcessDefinition creator = ProcessBuilder.createProcess("creator", "1.0").addHuman(getLogin())
        .addSendEventTask("send").addOutgoingEvent("createProcess", null, null, null).addHumanTask("t", getLogin())
        .addTransition("send", "t").done();

    ProcessDefinition created = ProcessBuilder.createProcess("created", "1.0")
        .addReceiveEventTask("receive", "createProcess").done();

    creator = getManagementAPI().deploy(getBusinessArchive(creator));
    created = getManagementAPI().deploy(getBusinessArchive(created));

    final ProcessDefinitionUUID creatorUUID = creator.getUUID();
    final ProcessDefinitionUUID createdUUID = created.getUUID();

    final int instancesNb = 20;
    for (int i = 0; i < instancesNb; i++) {
      final ProcessInstanceUUID creatorInstanceUUID = getRuntimeAPI().instantiateProcess(creatorUUID);
      assertEquals("Creator process instance is finished", InstanceState.STARTED, getQueryRuntimeAPI()
          .getProcessInstance(creatorInstanceUUID).getInstanceState());
    }

    waitForCreation(instancesNb, createdUUID);

    getManagementAPI().deleteProcess(creatorUUID);
    getManagementAPI().deleteProcess(createdUUID);
  }

  public void testFailingEventReception() throws Exception {
    ProcessDefinition process = ProcessBuilder.createProcess("p", "d").addStringData("varName", "unknownVariable")
        .addStringData("processVar", "initial").addSystemTask("initial").addSendEventTask("send")
        .addOutgoingEvent("event", null, null, null).addReceiveEventTask("receive", "event")
        .addReceiveEventConnector(SetVarConnector.class.getName(), true)
        .addInputParameter("variableName", "${varName}").addInputParameter("value", "changed").addSystemTask("join")
        .addJoinType(JoinType.AND).addTransition("send", "join").addTransition("initial", "receive")
        .addTransition("receive", "join").done();

    process = getManagementAPI().deploy(getBusinessArchive(process, null, SetVarConnector.class, GetJobsCommand.class));
    final ProcessDefinitionUUID processUUID = process.getUUID();
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    assertEquals(InstanceState.STARTED, getQueryRuntimeAPI().getProcessInstance(instanceUUID).getInstanceState());
    assertEquals("initial", getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "processVar"));

    int numberOJobs = 0;
    final long maxDate = System.currentTimeMillis() + 15000;
    while (numberOJobs < 1 && System.currentTimeMillis() < maxDate) {
      numberOJobs = getCommandAPI().execute(new GetJobsCommand(), processUUID).size();
    }
    assertEquals(1, numberOJobs);

    getRuntimeAPI().setProcessInstanceVariable(instanceUUID, "varName", "processVar");
    getRuntimeAPI().enableEventsInFailure(instanceUUID, "receive");

    waitForInstanceEnd(10000, 10, instanceUUID);

    assertEquals(InstanceState.FINISHED, getQueryRuntimeAPI().getProcessInstance(instanceUUID).getInstanceState());
    checkExecutedOnce(instanceUUID, "send", "receive", "join");
    assertEquals("changed", getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "processVar"));
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testEventTimeToLive() throws Exception {
    ProcessDefinition process = ProcessBuilder.createProcess("p", "d").addSendEventTask("send")
        .addOutgoingEvent("event", null, null, 10 * 1000, null).done();

    process = getManagementAPI().deploy(getBusinessArchive(process));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    getManagementAPI().deployJar("my.jar", Misc.generateJar(GetOutgoingEventInstancesCommand.class));
    getRuntimeAPI().instantiateProcess(processUUID);

    assertEquals(1, getCommandAPI().execute(new GetOutgoingEventInstancesCommand()).size());

    Thread.sleep(5 * 1000);

    assertEquals(1, getCommandAPI().execute(new GetOutgoingEventInstancesCommand()).size());

    Thread.sleep(5 * 1000);

    for (int i = 0; i < 100; i++) {
      Thread.sleep(100);
      if (getCommandAPI().execute(new GetOutgoingEventInstancesCommand()).size() == 0) {
        break;
      }
    }
    // check one incomingEvent is removed from the db
    assertEquals(0, getCommandAPI().execute(new GetOutgoingEventInstancesCommand()).size());

    getManagementAPI().removeJar("my.jar");
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testEventWithSimpleTrueExpression() throws Exception {
    ProcessDefinition process = ProcessBuilder.createProcess("p", "d").addSystemTask("initial")
        .addSendEventTask("send").addOutgoingEvent("event", null, null, null)
        .addReceiveEventTask("receive", "event", "true").addSystemTask("join").addJoinType(JoinType.AND)
        .addTransition("send", "join").addTransition("initial", "receive").addTransition("receive", "join").done();

    process = getManagementAPI().deploy(getBusinessArchive(process));
    final ProcessDefinitionUUID processUUID = process.getUUID();
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    waitForInstanceEnd(11000, 10, instanceUUID);

    assertEquals(InstanceState.FINISHED, getQueryRuntimeAPI().getProcessInstance(instanceUUID).getInstanceState());
    checkExecutedOnce(instanceUUID, "send", "receive", "join");

    getManagementAPI().deleteProcess(processUUID);
  }

  public void testEventWithSimpleFalseExpression() throws Exception {
    ProcessDefinition process = ProcessBuilder.createProcess("p", "d").addHuman(getLogin()).addSendEventTask("send")
        .addOutgoingEvent("event", null, null, null).addReceiveEventTask("receive", "event", "false")
        .addHumanTask("human", getLogin()).addTransition("send", "human").done();

    process = getManagementAPI().deploy(getBusinessArchive(process));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    Thread.sleep(5000);
    final Collection<TaskInstance> tasks = getQueryRuntimeAPI().getTaskList(ActivityState.READY);
    assertEquals(1, tasks.size());

    final TaskInstance human = tasks.iterator().next();

    getRuntimeAPI().executeTask(human.getUUID(), true);

    assertEquals(InstanceState.FINISHED, getQueryRuntimeAPI().getProcessInstance(instanceUUID).getInstanceState());
    checkExecutedOnce(instanceUUID, "send", "human");

    final ActivityInstanceUUID activityUUID = getQueryRuntimeAPI().getActivityInstances(instanceUUID, "send")
        .iterator().next().getUUID();
    getRuntimeAPI().deleteEvents("event", null, null, activityUUID);

    getManagementAPI().deleteProcess(processUUID);
  }

  public void testEventWithExpressionUsingParameters() throws Exception {
    final Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("newValue", "${x}");

    ProcessDefinition process = ProcessBuilder.createProcess("p", "d").addStringData("v", "initial")
        .addStringData("x", "xValue").addSystemTask("initial").addSendEventTask("send")
        .addOutgoingEvent("event", null, null, parameters)
        .addReceiveEventTask("receive", "event", "newValue==\"xValue\"")
        .addReceiveEventConnector(SetVarConnector.class.getName(), true).addInputParameter("variableName", "v")
        .addInputParameter("value", "${newValue}").addSystemTask("join").addJoinType(JoinType.AND)
        .addTransition("send", "join").addTransition("initial", "receive").addTransition("receive", "join").done();

    process = getManagementAPI().deploy(getBusinessArchive(process, null, SetVarConnector.class));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    waitForInstanceEnd(10000, 10, instanceUUID);

    assertEquals(InstanceState.FINISHED, getQueryRuntimeAPI().getProcessInstance(instanceUUID).getInstanceState());
    checkExecutedOnce(instanceUUID, "send", "receive", "join");

    assertEquals("xValue", getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "v"));

    getManagementAPI().deleteProcess(processUUID);
  }

  public void testEventWithConnectorUsingEventGroovyParameters() throws Exception {
    final Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("newValue", "${x}");

    ProcessDefinition process = ProcessBuilder.createProcess("p", "d").addStringData("v", "initial")
        .addStringData("x", "xValue").addSystemTask("initial").addSendEventTask("send")
        .addOutgoingEvent("event", null, null, parameters).addReceiveEventTask("receive", "event")
        .addReceiveEventConnector(SetVarConnector.class.getName(), true).addInputParameter("variableName", "v")
        .addInputParameter("value", "${newValue}").addSystemTask("join").addJoinType(JoinType.AND)
        .addTransition("send", "join").addTransition("initial", "receive").addTransition("receive", "join").done();

    process = getManagementAPI().deploy(getBusinessArchive(process, null, SetVarConnector.class));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    waitForInstanceEnd(10000, 10, instanceUUID);

    assertEquals(InstanceState.FINISHED, getQueryRuntimeAPI().getProcessInstance(instanceUUID).getInstanceState());
    checkExecutedOnce(instanceUUID, "send", "receive", "join");

    assertEquals("xValue", getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "v"));

    getManagementAPI().deleteProcess(processUUID);
  }

  public void testEventWithConnectorUsingEventParameters() throws Exception {
    final Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("newValue", "changed");

    ProcessDefinition process = ProcessBuilder.createProcess("p", "d").addStringData("v", "initial")
        .addSystemTask("initial").addSendEventTask("send").addOutgoingEvent("event", null, null, parameters)
        .addReceiveEventTask("receive", "event").addReceiveEventConnector(SetVarConnector.class.getName(), true)
        .addInputParameter("variableName", "v").addInputParameter("value", "${newValue}").addSystemTask("join")
        .addJoinType(JoinType.AND).addTransition("send", "join").addTransition("initial", "receive")
        .addTransition("receive", "join").done();

    process = getManagementAPI().deploy(getBusinessArchive(process, null, SetVarConnector.class));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    waitForInstanceEnd(10000, 10, instanceUUID);

    assertEquals(InstanceState.FINISHED, getQueryRuntimeAPI().getProcessInstance(instanceUUID).getInstanceState());
    checkExecutedOnce(instanceUUID, "send", "receive", "join");

    assertEquals("changed", getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "v"));

    getManagementAPI().deleteProcess(processUUID);
  }

  public void testReceiveManyEventsWithSameName() throws Exception {
    ProcessDefinition process = ProcessBuilder.createProcess("p", "d").addSystemTask("initial")
        .addSendEventTask("send").addOutgoingEvent("event", null, null, null)
        .addOutgoingEvent("event", null, null, null).addOutgoingEvent("event", null, null, null)
        .addReceiveEventTask("receive1", "event").addReceiveEventTask("receive2", "event")
        .addReceiveEventTask("receive3", "event").addSystemTask("join").addJoinType(JoinType.AND)
        .addTransition("send", "join").addTransition("initial", "receive1").addTransition("initial", "receive2")
        .addTransition("initial", "receive3").addTransition("receive1", "join").addTransition("receive2", "join")
        .addTransition("receive3", "join").done();

    process = getManagementAPI().deploy(getBusinessArchive(process));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);
    waitForInstanceEnd(18000, 10, instanceUUID);

    checkExecutedOnce(instanceUUID, "send", "receive1", "receive2", "receive3", "join");
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testEventWithConnectorUsingExpression() throws Exception {
    ProcessDefinition process = ProcessBuilder.createProcess("p", "d").addStringData("v", "initial")
        .addStringData("x", "xValue").addSystemTask("initial").addSendEventTask("send")
        .addOutgoingEvent("event", null, null, null).addReceiveEventTask("receive", "event")
        .addReceiveEventConnector(SetVarConnector.class.getName(), true).addInputParameter("variableName", "v")
        .addInputParameter("value", "${x}").addSystemTask("join").addJoinType(JoinType.AND)
        .addTransition("send", "join").addTransition("initial", "receive").addTransition("receive", "join").done();

    process = getManagementAPI().deploy(getBusinessArchive(process, null, SetVarConnector.class));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    waitForInstanceEnd(10000, 10, instanceUUID);

    assertEquals(InstanceState.FINISHED, getQueryRuntimeAPI().getProcessInstance(instanceUUID).getInstanceState());
    checkExecutedOnce(instanceUUID, "send", "receive", "join");

    assertEquals("xValue", getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "v"));

    getManagementAPI().deleteProcess(processUUID);
  }

  public void testEventWithManyConnector() throws Exception {
    ProcessDefinition process = ProcessBuilder.createProcess("p", "d").addStringData("v", "initial")
        .addStringData("v2", "initial").addSystemTask("initial").addSendEventTask("send")
        .addOutgoingEvent("event", null, null, null).addReceiveEventTask("receive", "event")
        .addReceiveEventConnector(SetVarConnector.class.getName(), true).addInputParameter("variableName", "v")
        .addInputParameter("value", "changed1").addReceiveEventConnector(SetVarConnector.class.getName(), true)
        .addInputParameter("variableName", "v2").addInputParameter("value", "changed2").addSystemTask("join")
        .addJoinType(JoinType.AND).addTransition("send", "join").addTransition("initial", "receive")
        .addTransition("receive", "join").done();

    process = getManagementAPI().deploy(getBusinessArchive(process, null, SetVarConnector.class));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    waitForInstanceEnd(10000, 10, instanceUUID);

    assertEquals(InstanceState.FINISHED, getQueryRuntimeAPI().getProcessInstance(instanceUUID).getInstanceState());
    checkExecutedOnce(instanceUUID, "send", "receive", "join");

    assertEquals("changed1", getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "v"));
    assertEquals("changed2", getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "v2"));

    getManagementAPI().deleteProcess(processUUID);
  }

  public void testEventWithOneConnector() throws Exception {
    ProcessDefinition process = ProcessBuilder.createProcess("p", "d").addStringData("v", "initial")
        .addSystemTask("initial").addSendEventTask("send").addOutgoingEvent("event", null, null, null)
        .addReceiveEventTask("receive", "event").addReceiveEventConnector(SetVarConnector.class.getName(), true)
        .addInputParameter("variableName", "v").addInputParameter("value", "changed").addSystemTask("join")
        .addJoinType(JoinType.AND).addTransition("send", "join").addTransition("initial", "receive")
        .addTransition("receive", "join").done();

    process = getManagementAPI().deploy(getBusinessArchive(process, null, SetVarConnector.class));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    waitForInstanceEnd(10000, 10, instanceUUID);

    assertEquals(InstanceState.FINISHED, getQueryRuntimeAPI().getProcessInstance(instanceUUID).getInstanceState());
    checkExecutedOnce(instanceUUID, "send", "receive", "join");

    assertEquals("changed", getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "v"));

    getManagementAPI().deleteProcess(processUUID);
  }

  public void testOutgoingEventsDeletion() throws Exception {
    ProcessDefinition process = ProcessBuilder.createProcess("p", "d").addSendEventTask("send")
        .addOutgoingEvent("event1", null, null, null).addOutgoingEvent("event2", null, null, null)
        .addOutgoingEvent("event3", null, null, null).done();

    process = getManagementAPI().deploy(getBusinessArchive(process));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    getManagementAPI().deployJar("getExecJar.jar", Misc.generateJar(GetOutgoingEventInstancesCommand.class));

    assertEquals(0, getCommandAPI().execute(new GetOutgoingEventInstancesCommand()).size());

    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    assertEquals(InstanceState.FINISHED, getQueryRuntimeAPI().getProcessInstance(instanceUUID).getInstanceState());

    // check one incomingEvent is in the db
    assertEquals(3, getCommandAPI().execute(new GetOutgoingEventInstancesCommand()).size());

    checkExecutedOnce(instanceUUID, "send");

    getManagementAPI().removeJar("getExecJar.jar");

    final ActivityInstanceUUID activityUUID = getQueryRuntimeAPI().getActivityInstances(instanceUUID, "send")
        .iterator().next().getUUID();
    getRuntimeAPI().deleteEvents("event1", null, null, activityUUID);
    getRuntimeAPI().deleteEvents("event2", null, null, activityUUID);
    getRuntimeAPI().deleteEvents("event3", null, null, activityUUID);

    getManagementAPI().deleteProcess(processUUID);
  }

  public void testIncomingEventsDeletion() throws Exception {
    ProcessDefinition process = ProcessBuilder.createProcess("p", "d").addHuman(getLogin())
        .addHumanTask("human", getLogin()).addSystemTask("initial").addReceiveEventTask("receive", "event")
        .addSystemTask("join").addJoinType(JoinType.XOR).addTransition("human", "join")
        .addTransition("initial", "receive").addTransition("receive", "join").done();

    process = getManagementAPI().deploy(getBusinessArchive(process));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    getManagementAPI().deployJar("getExecJar.jar", Misc.generateJar(GetIncomingEventInstancesCommand.class));
    assertEquals(0, getCommandAPI().execute(new GetIncomingEventInstancesCommand()).size());

    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    assertEquals(InstanceState.STARTED, getQueryRuntimeAPI().getProcessInstance(instanceUUID).getInstanceState());
    // human task must be ready
    final Collection<TaskInstance> tasks = getQueryRuntimeAPI().getTaskList(instanceUUID, ActivityState.READY);
    assertEquals(1, tasks.size());

    // sleep to be sure the event is not consumed
    Thread.sleep(5000);

    // check one incomingEvent is in the db
    assertEquals(1, getCommandAPI().execute(new GetIncomingEventInstancesCommand()).size());

    // execute human task, so the good send event will be launched
    getRuntimeAPI().executeTask(tasks.iterator().next().getUUID(), true);

    waitForInstanceEnd(10000, 10, instanceUUID);

    assertEquals(InstanceState.FINISHED, getQueryRuntimeAPI().getProcessInstance(instanceUUID).getInstanceState());
    checkExecutedOnce(instanceUUID, "human", "receive", "join");

    assertEquals(1, getQueryRuntimeAPI().getActivityInstances(instanceUUID, "receive").size());
    assertEquals(ActivityState.ABORTED, getQueryRuntimeAPI().getActivityInstances(instanceUUID, "receive").iterator()
        .next().getState());

    assertEquals(0, getCommandAPI().execute(new GetIncomingEventInstancesCommand()).size());

    getManagementAPI().removeJar("getExecJar.jar");
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testManyEvents() throws Exception {
    ProcessDefinition process = ProcessBuilder.createProcess("p", "d").addSystemTask("initial")
        .addSendEventTask("send").addOutgoingEvent("event1", null, null, null)
        .addOutgoingEvent("event2", null, null, null).addOutgoingEvent("event3", null, null, null)
        .addReceiveEventTask("receive1", "event1").addReceiveEventTask("receive2", "event2")
        .addReceiveEventTask("receive3", "event3").addSystemTask("join").addJoinType(JoinType.AND)
        .addTransition("send", "join").addTransition("initial", "receive1").addTransition("initial", "receive2")
        .addTransition("initial", "receive3").addTransition("receive1", "join").addTransition("receive2", "join")
        .addTransition("receive3", "join").done();

    process = getManagementAPI().deploy(getBusinessArchive(process));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    waitForInstanceEnd(2000, 10, instanceUUID);

    assertEquals(InstanceState.FINISHED, getQueryRuntimeAPI().getProcessInstance(instanceUUID).getInstanceState());
    checkExecutedOnce(instanceUUID, "send", "receive1", "receive2", "receive3", "join");
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testWrongEventBetweenProcessActivities() throws Exception {
    // check if an event of wrong type is sent, it is not received properly
    ProcessDefinition process = ProcessBuilder.createProcess("p", "d").addHuman(getLogin()).addSystemTask("initial")
        .addSendEventTask("wrongSend").addOutgoingEvent("wrong", null, null, null).addHumanTask("human", getLogin())
        .addSendEventTask("send").addOutgoingEvent("event", null, null, null).addReceiveEventTask("receive", "event")
        .addSystemTask("join").addJoinType(JoinType.AND).addTransition("wrongSend", "human")
        .addTransition("human", "send").addTransition("send", "join").addTransition("initial", "receive")
        .addTransition("receive", "join").done();

    process = getManagementAPI().deploy(getBusinessArchive(process));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);
    assertEquals(InstanceState.STARTED, getQueryRuntimeAPI().getProcessInstance(instanceUUID).getInstanceState());
    // human task must be ready
    final Collection<TaskInstance> tasks = getQueryRuntimeAPI().getTaskList(instanceUUID, ActivityState.READY);
    assertEquals(1, tasks.size());

    // sleep to be sure the event is not consumed
    Thread.sleep(5000);

    // execute human task, so the good send event will be launched
    getRuntimeAPI().executeTask(tasks.iterator().next().getUUID(), true);

    waitForInstanceEnd(10000, 10, instanceUUID);

    assertEquals(InstanceState.FINISHED, getQueryRuntimeAPI().getProcessInstance(instanceUUID).getInstanceState());
    checkExecutedOnce(instanceUUID, "wrongSend", "human", "send", "receive", "join");

    final ActivityInstanceUUID activityUUID = getQueryRuntimeAPI().getActivityInstances(instanceUUID, "wrongSend")
        .iterator().next().getUUID();
    getRuntimeAPI().deleteEvents("wrong", null, null, activityUUID);

    getManagementAPI().deleteProcess(processUUID);
  }

  public void testEventBetweenProcessActivities() throws Exception {
    ProcessDefinition sendProcess = ProcessBuilder.createProcess("sendProcess", "d").addSendEventTask("sendEvent")
        .addOutgoingEvent("event", "receiveProcess", "receiveEvent", null).done();

    ProcessDefinition receiveProcess = ProcessBuilder.createProcess("receiveProcess", "d").addSystemTask("initial")
        .addReceiveEventTask("receiveEvent", "event").addTransition("initial", "receiveEvent").done();

    sendProcess = getManagementAPI().deploy(getBusinessArchive(sendProcess));
    receiveProcess = getManagementAPI().deploy(getBusinessArchive(receiveProcess));

    final ProcessInstanceUUID receiveInstanceUUID = getRuntimeAPI().instantiateProcess(receiveProcess.getUUID());
    // check instance is waiting
    assertEquals(InstanceState.STARTED, getQueryRuntimeAPI().getProcessInstance(receiveInstanceUUID).getInstanceState());

    final ProcessInstanceUUID sendInstanceUUID = getRuntimeAPI().instantiateProcess(sendProcess.getUUID());
    // check instance is finished
    assertEquals(InstanceState.FINISHED, getQueryRuntimeAPI().getProcessInstance(sendInstanceUUID).getInstanceState());
    checkExecutedOnce(sendInstanceUUID, "sendEvent");

    waitForInstanceEnd(10000, 10, receiveInstanceUUID);

    assertEquals(InstanceState.FINISHED, getQueryRuntimeAPI().getProcessInstance(receiveInstanceUUID)
        .getInstanceState());
    checkExecutedOnce(receiveInstanceUUID, "receiveEvent");

    getManagementAPI().deleteProcess(receiveProcess.getUUID());
    getManagementAPI().deleteProcess(sendProcess.getUUID());
  }

  public void testEventBetweenProcesses() throws Exception {
    ProcessDefinition sendProcess = ProcessBuilder.createProcess("sendProcess", "d").addSendEventTask("sendEvent")
        .addOutgoingEvent("event", "receiveProcess", null, null).done();

    ProcessDefinition receiveProcess = ProcessBuilder.createProcess("receiveProcess", "d").addSystemTask("initial")
        .addReceiveEventTask("receiveEvent", "event").addTransition("initial", "receiveEvent").done();

    sendProcess = getManagementAPI().deploy(getBusinessArchive(sendProcess));
    receiveProcess = getManagementAPI().deploy(getBusinessArchive(receiveProcess));

    final ProcessInstanceUUID receiveInstanceUUID = getRuntimeAPI().instantiateProcess(receiveProcess.getUUID());
    // check instance is waiting
    assertEquals(InstanceState.STARTED, getQueryRuntimeAPI().getProcessInstance(receiveInstanceUUID).getInstanceState());

    final ProcessInstanceUUID sendInstanceUUID = getRuntimeAPI().instantiateProcess(sendProcess.getUUID());
    // check instance is finished
    assertEquals(InstanceState.FINISHED, getQueryRuntimeAPI().getProcessInstance(sendInstanceUUID).getInstanceState());
    checkExecutedOnce(sendInstanceUUID, "sendEvent");

    waitForInstanceEnd(10000, 10, receiveInstanceUUID);

    assertEquals(InstanceState.FINISHED, getQueryRuntimeAPI().getProcessInstance(receiveInstanceUUID)
        .getInstanceState());
    checkExecutedOnce(receiveInstanceUUID, "receiveEvent");

    getManagementAPI().deleteProcess(receiveProcess.getUUID());
    getManagementAPI().deleteProcess(sendProcess.getUUID());
  }

  public void testSimpleEventTask() throws Exception {
    ProcessDefinition process = ProcessBuilder.createProcess("p", "d").addSystemTask("initial")
        .addSendEventTask("send").addOutgoingEvent("event", null, null, null).addReceiveEventTask("receive", "event")
        .addSystemTask("join").addJoinType(JoinType.AND).addTransition("send", "join")
        .addTransition("initial", "receive").addTransition("receive", "join").done();

    process = getManagementAPI().deploy(getBusinessArchive(process));
    final ProcessDefinitionUUID processUUID = process.getUUID();
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    waitForInstanceEnd(10000, 10, instanceUUID);

    assertEquals(InstanceState.FINISHED, getQueryRuntimeAPI().getProcessInstance(instanceUUID).getInstanceState());
    checkExecutedOnce(instanceUUID, "send", "receive", "join");
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testSimpleEventTask2() throws Exception {
    ProcessDefinition process = ProcessBuilder.createProcess("p", "d").addSystemTask("initial").addHuman(getLogin())
        .addHumanTask("sleep", getLogin()).addConnector(Event.taskOnStart, SleepConnector.class.getName(), true)
        .addInputParameter("setMillis", new Long(5000)).addSendEventTask("send")
        .addOutgoingEvent("event", null, null, null).addReceiveEventTask("receive", "event").addSystemTask("join")
        .addJoinType(JoinType.AND)

        .addTransition("initial", "receive").addTransition("initial", "sleep").addTransition("sleep", "send")
        .addTransition("send", "join").addTransition("receive", "join").done();

    process = getManagementAPI().deploy(getBusinessArchive(process, null, SleepConnector.class));
    final ProcessDefinitionUUID processUUID = process.getUUID();
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    executeTask(instanceUUID, "sleep");

    waitForInstanceEnd(5000, 10, instanceUUID);

    assertEquals(InstanceState.FINISHED, getQueryRuntimeAPI().getProcessInstance(instanceUUID).getInstanceState());
    checkExecutedOnce(instanceUUID, "sleep", "send", "receive", "join");
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testReceiveSomeEventsWithSameName() throws Exception {
    ProcessDefinition process = ProcessBuilder.createProcess("p", "d").addSystemTask("initial")
        .addSendEventTask("send").addOutgoingEvent("event", "p", "receive1", null)
        .addOutgoingEvent("event", "p", "receive3", null).addReceiveEventTask("receive1", "event")
        .addReceiveEventTask("receive2", "event").addReceiveEventTask("receive3", "event").addSystemTask("join")
        .addJoinType(JoinType.AND).addTransition("send", "join").addTransition("initial", "receive1")
        .addTransition("initial", "receive2").addTransition("initial", "receive3").addTransition("receive1", "join")
        .addTransition("receive2", "join").addTransition("receive3", "join").done();

    process = getManagementAPI().deploy(getBusinessArchive(process));
    final ProcessDefinitionUUID processUUID = process.getUUID();
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    Thread.sleep(5000);
    final Set<LightActivityInstance> activities = getQueryRuntimeAPI().getLightActivityInstances(instanceUUID);
    assertEquals(5, activities.size());
    for (final LightActivityInstance activity : activities) {
      if ("initial".equals(activity.getActivityName())) {
        assertEquals(ActivityState.FINISHED, activity.getState());
      } else if ("receive1".equals(activity.getActivityName())) {
        assertEquals(ActivityState.FINISHED, activity.getState());
      } else if ("receive2".equals(activity.getActivityName())) {
        assertEquals(ActivityState.EXECUTING, activity.getState());
      } else if ("receive3".equals(activity.getActivityName())) {
        assertEquals(ActivityState.FINISHED, activity.getState());
      } else {
        assertEquals("send", activity.getActivityName());
        assertEquals(ActivityState.FINISHED, activity.getState());
      }
    }

    assertEquals(InstanceState.STARTED, getQueryRuntimeAPI().getProcessInstance(instanceUUID).getInstanceState());
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testMessageBoundaryEventAfterMessageThrowing() throws Exception {
    ProcessDefinition process = ProcessBuilder.createProcess("messageBoundary", "1.3.4").addHuman(getLogin())
        .addSendEventTask("send").addOutgoingEvent("event", null, null, null).addHumanTask("boundary", getLogin())
        .addMessageBoundaryEvent("event", null).addHumanTask("normal", getLogin())
        .addHumanTask("exception", getLogin()).addTransition("send", "boundary")
        .addExceptionTransition("boundary", "event", "exception").addTransition("boundary", "normal").done();

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

  public void testMessageBoundaryEventBeforeMessageThowing() throws Exception {
    ProcessDefinition process = ProcessBuilder.createProcess("messageBoundary", "1.3.4").addHuman(getLogin())
        .addSystemTask("start").addHumanTask("step1", getLogin()).addSendEventTask("send")
        .addOutgoingEvent("event", null, null, null).addHumanTask("boundary", getLogin())
        .addMessageBoundaryEvent("event", null).addHumanTask("normal", getLogin())
        .addHumanTask("exception", getLogin()).addHumanTask("step2", getLogin()).addTransition("start", "step1")
        .addTransition("step1", "send").addTransition("send", "step2").addTransition("start", "boundary")
        .addExceptionTransition("boundary", "event", "exception").addTransition("boundary", "normal").done();

    process = getManagementAPI().deploy(getBusinessArchive(process));
    final ProcessDefinitionUUID processUUID = process.getUUID();
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);
    Thread.sleep(2000);
    Set<LightActivityInstance> activities = getQueryRuntimeAPI().getLightActivityInstances(instanceUUID);
    assertEquals(3, activities.size());
    for (final LightActivityInstance activity : activities) {
      if ("start".equals(activity.getActivityName())) {
        assertEquals(ActivityState.FINISHED, activity.getState());
      } else if ("boundary".equals(activity.getActivityName())) {
        assertEquals(ActivityState.READY, activity.getState());
      } else {
        assertEquals("step1", activity.getActivityName());
        assertEquals(ActivityState.READY, activity.getState());
      }
    }
    executeTask(instanceUUID, "step1");
    Thread.sleep(2000);
    activities = getQueryRuntimeAPI().getLightActivityInstances(instanceUUID);
    assertEquals(6, activities.size());
    LightActivityInstance activity = getLightActivityInstance(activities, "start");
    assertEquals(ActivityState.FINISHED, activity.getState());
    activity = getLightActivityInstance(activities, "boundary");
    assertEquals(ActivityState.ABORTED, activity.getState());
    activity = getLightActivityInstance(activities, "step2");
    assertEquals(ActivityState.READY, activity.getState());
    activity = getLightActivityInstance(activities, "exception");
    assertEquals(ActivityState.READY, activity.getState());
    activity = getLightActivityInstance(activities, "send");
    assertEquals(ActivityState.FINISHED, activity.getState());
    activity = getLightActivityInstance(activities, "step1");
    assertEquals(ActivityState.FINISHED, activity.getState());

    getManagementAPI().deleteProcess(processUUID);
  }

  public void testMessageBoundaryEventAfterThrowingMessageBetweenTwoProcesses() throws Exception {
    ProcessDefinition sendProcess = ProcessBuilder.createProcess("sendProcess", "d").addHuman(getLogin())
        .addHumanTask("start", getLogin()).addSendEventTask("sendMessage")
        .addOutgoingEvent("event", "receiveProcess", null, null).addTransition("start", "sendMessage").done();

    ProcessDefinition receiveProcess = ProcessBuilder.createProcess("receiveProcess", "d").addHuman(getLogin())
        .addSystemTask("initial").addSubProcess("receiveEvent", "sendProcess").addMessageBoundaryEvent("event", null)
        .addHumanTask("exception", getLogin()).addTransition("initial", "receiveEvent")
        .addExceptionTransition("receiveEvent", "event", "exception").done();

    sendProcess = getManagementAPI().deploy(getBusinessArchive(sendProcess));
    receiveProcess = getManagementAPI().deploy(getBusinessArchive(receiveProcess));

    final ProcessInstanceUUID receiveInstanceUUID = getRuntimeAPI().instantiateProcess(receiveProcess.getUUID());
    // check instance is waiting
    assertEquals(InstanceState.STARTED, getQueryRuntimeAPI().getProcessInstance(receiveInstanceUUID).getInstanceState());

    final ProcessInstanceUUID sendInstanceUUID = getRuntimeAPI().instantiateProcess(sendProcess.getUUID());
    // check instance is finished
    executeTask(sendInstanceUUID, "start");
    assertEquals(InstanceState.FINISHED, getQueryRuntimeAPI().getProcessInstance(sendInstanceUUID).getInstanceState());
    Thread.sleep(2000);
    final Set<LightActivityInstance> activities = getQueryRuntimeAPI().getLightActivityInstances(receiveInstanceUUID);
    assertEquals(3, activities.size());
    LightActivityInstance activity = getLightActivityInstance(activities, "initial");
    assertEquals(ActivityState.FINISHED, activity.getState());
    activity = getLightActivityInstance(activities, "receiveEvent");
    assertEquals(ActivityState.ABORTED, activity.getState());
    activity = getLightActivityInstance(activities, "exception");
    assertEquals(ActivityState.READY, activity.getState());

    getManagementAPI().deleteProcess(receiveProcess.getUUID());
    getManagementAPI().deleteProcess(sendProcess.getUUID());
  }

  public void testMessageBoundaryEventsAfterThrowingMessageBetweenTwoProcesses() throws Exception {
    ProcessDefinition sendProcess = ProcessBuilder.createProcess("sendProcess", "d").addHuman(getLogin())
        .addHumanTask("start", getLogin()).addSendEventTask("sendMessage")
        .addOutgoingEvent("event", "receiveProcess", "receiveEvent2", null).addTransition("start", "sendMessage")
        .done();

    ProcessDefinition receiveProcess = ProcessBuilder.createProcess("receiveProcess", "d").addHuman(getLogin())
        .addSystemTask("initial").addHumanTask("receiveEvent1", getLogin()).addMessageBoundaryEvent("event", null)
        .addHumanTask("receiveEvent2", getLogin()).addMessageBoundaryEvent("event", null)
        .addHumanTask("exception1", getLogin()).addHumanTask("exception2", getLogin())
        .addTransition("initial", "receiveEvent1").addTransition("initial", "receiveEvent2")
        .addExceptionTransition("receiveEvent1", "event", "exception1")
        .addExceptionTransition("receiveEvent2", "event", "exception2").done();

    sendProcess = getManagementAPI().deploy(getBusinessArchive(sendProcess));
    receiveProcess = getManagementAPI().deploy(getBusinessArchive(receiveProcess));

    final ProcessInstanceUUID receiveInstanceUUID = getRuntimeAPI().instantiateProcess(receiveProcess.getUUID());
    // check instance is waiting
    assertEquals(InstanceState.STARTED, getQueryRuntimeAPI().getProcessInstance(receiveInstanceUUID).getInstanceState());

    final ProcessInstanceUUID sendInstanceUUID = getRuntimeAPI().instantiateProcess(sendProcess.getUUID());
    // check instance is finished
    executeTask(sendInstanceUUID, "start");
    assertEquals(InstanceState.FINISHED, getQueryRuntimeAPI().getProcessInstance(sendInstanceUUID).getInstanceState());
    Thread.sleep(2000);
    final Set<LightActivityInstance> activities = getQueryRuntimeAPI().getLightActivityInstances(receiveInstanceUUID);
    assertEquals(4, activities.size());
    LightActivityInstance activity = getLightActivityInstance(activities, "initial");
    assertEquals(ActivityState.FINISHED, activity.getState());
    activity = getLightActivityInstance(activities, "receiveEvent1");
    assertEquals(ActivityState.READY, activity.getState());
    activity = getLightActivityInstance(activities, "receiveEvent2");
    assertEquals(ActivityState.ABORTED, activity.getState());
    activity = getLightActivityInstance(activities, "exception2");
    assertEquals(ActivityState.READY, activity.getState());

    getManagementAPI().deleteProcess(receiveProcess.getUUID());
    getManagementAPI().deleteProcess(sendProcess.getUUID());
  }

  public void testNoMessage() throws Exception {
    try {
      ProcessBuilder.createProcess("creator", "1.0").addSendEventTask("send").done();
      fail("A sent message event must send at least a message");
    } catch (final BonitaRuntimeException e) {
    }
  }

  public void testSendAMessageToNobody() throws Exception {
    ProcessDefinition creator = ProcessBuilder.createProcess("creator", "1.0").addSendEventTask("send")
        .addOutgoingEvent("createProcess", null, null, null).done();

    creator = getManagementAPI().deploy(getBusinessArchive(creator));
    final ProcessDefinitionUUID creatorUUID = creator.getUUID();
    getRuntimeAPI().instantiateProcess(creatorUUID);

    getManagementAPI().deleteProcess(creatorUUID);
  }

  public void testSendMessageWithAttachments() throws Exception {
    final Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("a1", "${att1}");
    parameters.put("a2", "${att2}");

    ProcessDefinition procDefToSend = ProcessBuilder.createProcess("pToSend", "1.0").addAttachment("att1")
        .addAttachment("att2").addHuman(getLogin()).addHumanTask("step1", getLogin()).addSendEventTask("send")
        .addOutgoingEvent("m1", "pToReceive", "receive", parameters).addTransition("step1", "send").done();

    ProcessDefinition procDefToReceive = ProcessBuilder.createProcess("pToReceive", "d").addAttachment("doc1")
        .addAttachment("doc2").addHuman(getLogin()).addReceiveEventTask("receive", "m1")
        .addReceiveEventConnector(SetVarConnector.class.getName(), true).addInputParameter("variableName", "doc2")
        .addInputParameter("value", "${a2}").addReceiveEventConnector(SetVarConnector.class.getName(), true)
        .addInputParameter("variableName", "doc1").addInputParameter("value", "${a1}")
        .addHumanTask("step2", getLogin()).addTransition("receive", "step2").done();

    procDefToSend = getManagementAPI().deploy(getBusinessArchive(procDefToSend));
    procDefToReceive = getManagementAPI().deploy(getBusinessArchive(procDefToReceive, null, SetVarConnector.class));

    final ProcessInstanceUUID sendProcInstUUID = getRuntimeAPI().instantiateProcess(procDefToSend.getUUID());
    Set<ActivityInstance> activityInstances = getQueryRuntimeAPI().getActivityInstances(sendProcInstUUID, "step1");
    assertEquals(1, activityInstances.size());

    final String strDoc1Content = "this is the content of file1";
    updateDocument(sendProcInstUUID, "att1", strDoc1Content.getBytes());
    final String strDoc2Content = "this is the content of file2";
    updateDocument(sendProcInstUUID, "att2", strDoc2Content.getBytes());

    executeTask(sendProcInstUUID, "step1");
    waitForStartingInstance(10000, 50, procDefToReceive.getUUID());

    final Set<LightProcessInstance> lightProcessInstances = getQueryRuntimeAPI().getLightProcessInstances(
        procDefToReceive.getUUID());
    assertEquals(1, lightProcessInstances.size());
    final ProcessInstanceUUID receiveProcInstUUUID = lightProcessInstances.iterator().next().getUUID();

    activityInstances = getQueryRuntimeAPI().getActivityInstances(receiveProcInstUUUID, "receive");
    assertEquals(1, activityInstances.size());
    final ActivityInstance activityInstance = activityInstances.iterator().next();
    assertEquals(ActivityState.FINISHED, activityInstance.getState());

    activityInstances = getQueryRuntimeAPI().getActivityInstances(receiveProcInstUUUID, "step2");
    assertEquals(1, activityInstances.size());

    DocumentResult documentResult = searchDocuments(receiveProcInstUUUID, "doc1");
    assertEquals(1, documentResult.getCount());
    Document document = documentResult.getDocuments().get(0);
    final byte[] doc1Content = getQueryRuntimeAPI().getDocumentContent(document.getUUID());
    assertEquals(strDoc1Content, new String(doc1Content));
    assertEquals("a/b/c/att1.txt", document.getContentFileName());

    documentResult = searchDocuments(receiveProcInstUUUID, "doc2");
    assertEquals(1, documentResult.getCount());
    document = documentResult.getDocuments().get(0);
    final byte[] doc2Content = getQueryRuntimeAPI().getDocumentContent(document.getUUID());
    assertEquals(strDoc2Content, new String(doc2Content));
    assertEquals("a/b/c/att2.txt", document.getContentFileName());

    getManagementAPI().deleteProcess(procDefToSend.getUUID());
    getManagementAPI().deleteProcess(procDefToReceive.getUUID());
  }

  private void updateDocument(final ProcessInstanceUUID procInstUUID, final String documentName, final byte[] newContent)
      throws DocumentationCreationException {
    final DocumentResult documentsResult = searchDocuments(procInstUUID, documentName);
    assertEquals(1, documentsResult.getCount());
    final Document document = documentsResult.getDocuments().get(0);
    final DocumentUUID documentUUID = document.getUUID();

    getRuntimeAPI().addDocumentVersion(documentUUID, document.isMajorVersion(), "a/b/c/" + documentName + ".txt",
        "text/*", newContent);
  }

  private DocumentResult searchDocuments(final ProcessInstanceUUID procInstUUID, final String documentName) {
    final DocumentSearchBuilder searchBuilder = new DocumentSearchBuilder();
    searchBuilder.criterion(DocumentIndex.NAME).equalsTo(documentName).and()
        .criterion(DocumentIndex.PROCESS_INSTANCE_UUID).equalsTo(procInstUUID.getValue());
    final DocumentResult documentsResult = getQueryRuntimeAPI().searchDocuments(searchBuilder, 0, 10);
    return documentsResult;
  }

  /**
   * see bug 0010844 It throws BonitaRuntimeException: No user found in
   * environment
   * 
   * @throws Exception
   */
  public void testIntermediateCatchMessageEventWithEnd() throws Exception {
    ProcessDefinition catcher = ProcessBuilder.createProcess("signalcatcher", "1.4").addHuman(getLogin())
        .addSystemTask("startCatcher").addReceiveEventTask("catcher", "message").addSystemTask("endCatcher")
        .addTransition("startCatcher", "catcher").addTransition("catcher", "endCatcher").done();

    ProcessDefinition thrower = ProcessBuilder.createProcess("signalThrower", "1.1").addHuman(getLogin())
        .addSystemTask("startThrower").addSendEventTask("thrower")
        .addOutgoingEvent("message", "signalcatcher", "catcher").addTransition("startThrower", "thrower").done();

    thrower = getManagementAPI().deploy(getBusinessArchive(thrower));
    catcher = getManagementAPI().deploy(getBusinessArchive(catcher));

    final ProcessInstanceUUID catcherUUID = getRuntimeAPI().instantiateProcess(catcher.getUUID());
    getRuntimeAPI().instantiateProcess(thrower.getUUID());
    waitForInstanceEnd(1500, 50, catcherUUID);
    getManagementAPI().deleteProcess(thrower.getUUID());
    getManagementAPI().deleteProcess(catcher.getUUID());
  }

}
