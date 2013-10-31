package org.ow2.bonita.integration.connector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.Assert;

import org.bonitasoft.connectors.bonita.SetVarConnector;
import org.bonitasoft.connectors.email.EmailConnector;
import org.bonitasoft.connectors.scripting.GroovyConnector;
import org.ow2.bonita.APITestCase;
import org.ow2.bonita.facade.def.element.HookDefinition.Event;
import org.ow2.bonita.facade.def.majorElement.ActivityDefinition.JoinType;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.runtime.ActivityState;
import org.ow2.bonita.facade.runtime.InstanceState;
import org.ow2.bonita.facade.runtime.ProcessInstance;
import org.ow2.bonita.facade.runtime.TaskInstance;
import org.ow2.bonita.facade.uuid.ActivityInstanceUUID;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.util.BonitaRuntimeException;
import org.ow2.bonita.util.ClassDataTool;
import org.ow2.bonita.util.ProcessBuilder;

public class ConnectorsAtProcessLevelTest extends APITestCase {

  public void testAConnectorWhenAnInstanceIsStarted() throws Exception {
    final ProcessDefinition definition = ProcessBuilder.createProcess("Connect", null).addBooleanData("started", false)
        .addHuman("john").addConnector(Event.instanceOnStart, SetVarConnector.class.getName(), true)
        .addInputParameter("variableName", "started").addInputParameter("value", true).addHumanTask("task", "john")
        .done();

    final ProcessDefinition process = getManagementAPI().deploy(
        getBusinessArchive(definition, getResourcesFromConnector(SetVarConnector.class), SetVarConnector.class));
    final ProcessDefinitionUUID processUUID = process.getUUID();
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    final Boolean actual = (Boolean) getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "started");
    Assert.assertTrue(actual);

    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testAConnectorWhenAnInstanceIsFinished() throws Exception {
    final ProcessDefinition definition = ProcessBuilder.createProcess("Connect", null).addBooleanData("started", false)
        .addHuman("Matt").addConnector(Event.instanceOnFinish, SetVarConnector.class.getName(), true)
        .addInputParameter("variableName", "started").addInputParameter("value", true).addHumanTask("task", "Matt")
        .done();

    final ProcessDefinition process = getManagementAPI().deploy(
        getBusinessArchive(definition, getResourcesFromConnector(SetVarConnector.class), SetVarConnector.class));
    final ProcessDefinitionUUID processUUID = process.getUUID();
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    Boolean actual = (Boolean) getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "started");
    Assert.assertFalse(actual);

    final Set<TaskInstance> tasks = getQueryRuntimeAPI().getTasks(instanceUUID);
    final TaskInstance task = tasks.iterator().next();
    getRuntimeAPI().startTask(task.getUUID(), true);
    getRuntimeAPI().finishTask(task.getUUID(), true);

    actual = (Boolean) getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "started");
    Assert.assertTrue(actual);

    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testAConnectorWhenAnInstanceIsCancelled() throws Exception {
    final ProcessDefinition definition = ProcessBuilder.createProcess("Connect", null).addBooleanData("started", false)
        .addHuman("Matt").addConnector(Event.instanceOnCancel, SetVarConnector.class.getName(), true)
        .addInputParameter("variableName", "started").addInputParameter("value", true).addHumanTask("task", "Matt")
        .done();

    final ProcessDefinition process = getManagementAPI().deploy(
        getBusinessArchive(definition, getResourcesFromConnector(SetVarConnector.class), SetVarConnector.class));
    final ProcessDefinitionUUID processUUID = process.getUUID();
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    Boolean actual = (Boolean) getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "started");
    Assert.assertFalse(actual);

    final Set<TaskInstance> tasks = getQueryRuntimeAPI().getTasks(instanceUUID);
    final TaskInstance task = tasks.iterator().next();
    getRuntimeAPI().startTask(task.getUUID(), true);
    getRuntimeAPI().cancelProcessInstance(instanceUUID);

    actual = (Boolean) getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "started");
    Assert.assertTrue(actual);

    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testAConnectorWhenAnInstanceIsAborted() throws Exception {
    final ProcessDefinition subFlowDefinition = ProcessBuilder.createProcess("subflowWithTask", "1.0")
        .addHuman("admin").addSystemTask("BonitaInit").addSystemTask("BonitaEnd").addJoinType(JoinType.XOR)
        .addHumanTask("task", "admin").addTransition("Start_task", "BonitaInit", "task")
        .addTransition("task_End", "task", "BonitaEnd").done();

    final ProcessDefinition definition = ProcessBuilder.createProcess("mainSubflowJoinXor", "1.0")
        .addBooleanData("started", false).addConnector(Event.instanceOnAbort, SetVarConnector.class.getName(), true)
        .addInputParameter("variableName", "started").addInputParameter("value", true).addHuman("admin")
        .addSubProcess("sf2", "subflowWithTask").addSubProcess("sf1", "subflowWithTask").addDecisionNode("join")
        .addJoinType(JoinType.XOR).addSystemTask("BonitaInit").addSystemTask("BonitaEnd").addJoinType(JoinType.XOR)
        .addHumanTask("waitState", "admin").addTransition("sf2_join", "sf2", "join")
        .addTransition("Start_sf2", "BonitaInit", "sf2").addTransition("join_waitState", "join", "waitState")
        .addTransition("sf1_join", "sf1", "join").addTransition("Start_sf1", "BonitaInit", "sf1")
        .addTransition("waitState_End", "waitState", "BonitaEnd").done();

    final ProcessDefinition subProcess = getManagementAPI().deploy(getBusinessArchive(subFlowDefinition));
    final ProcessDefinitionUUID subProcessUUID = subProcess.getUUID();
    final ProcessDefinition process = getManagementAPI().deploy(
        getBusinessArchive(definition, getResourcesFromConnector(SetVarConnector.class), SetVarConnector.class));
    final ProcessDefinitionUUID processUUID = process.getUUID();
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    final Collection<TaskInstance> taskActivities = getQueryRuntimeAPI().getTaskList(ActivityState.READY);
    // one task for each subflow
    assertEquals(2, taskActivities.size());
    final Iterator<TaskInstance> it = taskActivities.iterator();
    final TaskInstance taskActivity = it.next();

    // execute one task
    final ProcessInstance processInstance = getQueryRuntimeAPI().getProcessInstance(
        taskActivity.getProcessInstanceUUID());
    assertEquals(instanceUUID, processInstance.getParentInstanceUUID());

    Boolean actual = (Boolean) getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "started");
    Assert.assertFalse(actual);

    final ActivityInstanceUUID taskUUID = taskActivity.getUUID();
    getRuntimeAPI().startTask(taskUUID, true);
    getRuntimeAPI().finishTask(taskUUID, true);

    actual = (Boolean) getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "started");
    Assert.assertTrue(actual);

    // check other subprocess is aborted
    final TaskInstance otherTaskActivity = it.next();
    final ProcessInstance otherInstance = getQueryRuntimeAPI().getProcessInstance(
        otherTaskActivity.getProcessInstanceUUID());
    assertEquals(InstanceState.ABORTED, otherInstance.getInstanceState());
    // check task is aborted
    assertEquals(ActivityState.ABORTED, getQueryRuntimeAPI().getTask(otherTaskActivity.getUUID()).getState());

    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().archive(processUUID);
    getManagementAPI().disable(subProcessUUID);
    getManagementAPI().archive(subProcessUUID);

    getManagementAPI().deleteProcess(processUUID);
    getManagementAPI().deleteProcess(subProcessUUID);
  }

  public void testConnectorsWhenAnInstanceIsStarted() throws Exception {
    final Date firstDate = new Date();
    Thread.sleep(2000l);
    final Date secondDate = new Date();
    Thread.sleep(2000l);
    final Date thirdDate = new Date();

    final ProcessDefinition definition = ProcessBuilder.createProcess("Connect", null).addDateData("firstDate")
        .addDateData("secondDate").addDateData("thirdDate").addHuman("john")
        .addConnector(Event.instanceOnStart, SetVarConnector.class.getName(), true)
        .addInputParameter("variableName", "firstDate").addInputParameter("value", firstDate)
        .addConnector(Event.instanceOnStart, SetVarConnector.class.getName(), true)
        .addInputParameter("variableName", "secondDate").addInputParameter("value", secondDate)
        .addConnector(Event.instanceOnStart, SetVarConnector.class.getName(), true)
        .addInputParameter("variableName", "thirdDate").addInputParameter("value", thirdDate)
        .addHumanTask("task", "john").done();

    final ProcessDefinition process = getManagementAPI().deploy(
        getBusinessArchive(definition, getResourcesFromConnector(SetVarConnector.class), SetVarConnector.class));
    final ProcessDefinitionUUID processUUID = process.getUUID();
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    final Set<TaskInstance> tasks = getQueryRuntimeAPI().getTasks(instanceUUID);
    final TaskInstance task = tasks.iterator().next();
    getRuntimeAPI().startTask(task.getUUID(), true);
    getRuntimeAPI().finishTask(task.getUUID(), true);

    final Date firstActual = (Date) getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "firstDate");
    final Date secondActual = (Date) getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "secondDate");
    final Date thirdActual = (Date) getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "thirdDate");
    Assert.assertTrue(firstActual.before(secondActual));
    Assert.assertTrue(secondActual.before(thirdActual));

    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testConnectorsWhenAnInstanceIsFinished() throws Exception {
    final Date firstDate = new Date();
    Thread.sleep(2000l);
    final Date secondDate = new Date();
    Thread.sleep(2000l);
    final Date thirdDate = new Date();

    final ProcessDefinition definition = ProcessBuilder.createProcess("Connect", null).addDateData("firstDate")
        .addDateData("secondDate").addDateData("thirdDate").addHuman("john")
        .addConnector(Event.instanceOnFinish, SetVarConnector.class.getName(), true)
        .addInputParameter("variableName", "firstDate").addInputParameter("value", firstDate)
        .addConnector(Event.instanceOnFinish, SetVarConnector.class.getName(), true)
        .addInputParameter("variableName", "secondDate").addInputParameter("value", secondDate)
        .addConnector(Event.instanceOnFinish, SetVarConnector.class.getName(), true)
        .addInputParameter("variableName", "thirdDate").addInputParameter("value", thirdDate)
        .addHumanTask("task", "john").done();

    final ProcessDefinition process = getManagementAPI().deploy(
        getBusinessArchive(definition, getResourcesFromConnector(SetVarConnector.class), SetVarConnector.class));
    final ProcessDefinitionUUID processUUID = process.getUUID();
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    Date firstActual = (Date) getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "firstDate");
    Date secondActual = (Date) getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "secondDate");
    Date thirdActual = (Date) getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "thirdDate");
    Assert.assertNull(firstActual);
    Assert.assertNull(secondActual);
    Assert.assertNull(thirdActual);

    final Set<TaskInstance> tasks = getQueryRuntimeAPI().getTasks(instanceUUID);
    final TaskInstance task = tasks.iterator().next();
    getRuntimeAPI().startTask(task.getUUID(), true);
    getRuntimeAPI().finishTask(task.getUUID(), true);

    firstActual = (Date) getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "firstDate");
    secondActual = (Date) getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "secondDate");
    thirdActual = (Date) getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "thirdDate");

    Assert.assertTrue(firstActual.before(secondActual));
    Assert.assertTrue(secondActual.before(thirdActual));

    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testACycleConnector() throws Exception {
    final ProcessDefinition definition = ProcessBuilder.createProcess("Connect", null).addIntegerData("count", 1)
        .addHuman("john").addConnector(Event.instanceOnStart, SetVarConnector.class.getName(), true)
        .addInputParameter("variableName", "count").addInputParameter("value", 2)
        .addConnector(Event.instanceOnFinish, SetVarConnector.class.getName(), true)
        .addInputParameter("variableName", "count").addInputParameter("value", 3).addHumanTask("task", "john").done();

    final ProcessDefinition process = getManagementAPI().deploy(
        getBusinessArchive(definition, getResourcesFromConnector(SetVarConnector.class), SetVarConnector.class));
    final ProcessDefinitionUUID processUUID = process.getUUID();
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    Integer actual = (Integer) getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "count");
    Assert.assertEquals(Integer.valueOf(2), actual);

    final Set<TaskInstance> tasks = getQueryRuntimeAPI().getTasks(instanceUUID);
    final TaskInstance task = tasks.iterator().next();
    getRuntimeAPI().startTask(task.getUUID(), true);
    getRuntimeAPI().finishTask(task.getUUID(), true);

    actual = (Integer) getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "count");
    Assert.assertEquals(Integer.valueOf(3), actual);

    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testOutputValue() throws Exception {
    final ProcessDefinition definition = ProcessBuilder.createProcess("output", "3.0").addBooleanData("running", false)
        .addHuman(getLogin()).addConnector(Event.instanceOnStart, GroovyConnector.class.getName(), true)
        .addInputParameter("script", "true").addOutputParameter("${result}", "running")
        .addHumanTask("start", getLogin()).done();

    final ProcessDefinition process = getManagementAPI().deploy(
        getBusinessArchive(definition, getResourcesFromConnector(GroovyConnector.class), GroovyConnector.class));
    final ProcessDefinitionUUID processUUID = process.getUUID();
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    final Boolean actual = (Boolean) getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "running");
    assertTrue(actual);

    getRuntimeAPI().deleteAllProcessInstances(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testCheckThatHeadersAreNotWellFormed() throws Exception {
    final List<List<Object>> headers = new ArrayList<List<Object>>();
    final List<Object> keyValue = new ArrayList<Object>();
    keyValue.add("X-Priority");
    headers.add(keyValue);

    getManagementAPI().deployJar("emailConnector.jar", ClassDataTool.getClassData(EmailConnector.class));
    final Map<String, Object[]> parameters = new HashMap<String, Object[]>();
    parameters.put("setHeaders", new Object[] { headers });
    try {
      getRuntimeAPI().executeConnector(EmailConnector.class.getName(), parameters);
      fail("The connector is not well configured");
    } catch (final BonitaRuntimeException e) {
      getManagementAPI().removeJar("emailConnector.jar");
    }
  }

}
