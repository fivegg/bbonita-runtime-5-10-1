package org.ow2.bonita.integration.connector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.bonitasoft.connectors.bonita.SetVarConnector;
import org.bonitasoft.connectors.bonita.SetVarsConnector;
import org.bonitasoft.connectors.bonita.resolvers.ProcessInitiatorRoleResolver;
import org.ow2.bonita.APITestCase;
import org.ow2.bonita.facade.def.element.HookDefinition.Event;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.runtime.ActivityState;
import org.ow2.bonita.facade.runtime.TaskInstance;
import org.ow2.bonita.facade.uuid.ActivityInstanceUUID;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.util.ProcessBuilder;

public class SetVarConnectorIntegrationTest extends APITestCase {

  public void testSetAStringVariable() throws Exception {
    ProcessDefinition definition =
      ProcessBuilder.createProcess("setVar", "1.0")
        .addStringData("start")
        .addGroup("Custom")
          .addGroupResolver(ProcessInitiatorRoleResolver.class.getName())
        .addHumanTask("Request", "Custom")
          .addConnector(Event.taskOnFinish, SetVarConnector.class.getName(), true)
            .addInputParameter("variableName", "start")
            .addInputParameter("value", "hello")
      .done();

    Map<String, byte[]> resources = getResourcesFromConnector(SetVarsConnector.class, ProcessInitiatorRoleResolver.class);
    ProcessDefinition process = getManagementAPI().deploy(getBusinessArchive(
        definition, resources, ProcessInitiatorRoleResolver.class, SetVarConnector.class));
    ProcessDefinitionUUID processUUID = process.getUUID();
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    assertNull(getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "start"));
    executeTask(instanceUUID, "Request");
    assertEquals("hello", getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "start"));

    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testSetAnIntegerVariableToStringVariable() throws Exception {
    ProcessDefinition definition =
      ProcessBuilder.createProcess("setVar", "1.0")
        .addIntegerData("start")
        .addGroup("Custom")
          .addGroupResolver(ProcessInitiatorRoleResolver.class.getName())
        .addHumanTask("Request", "Custom")
          .addConnector(Event.taskOnFinish, SetVarConnector.class.getName(), true)
            .addInputParameter("variableName", "start")
            .addInputParameter("value", "10")
      .done();

    Map<String, byte[]> resources = getResourcesFromConnector(SetVarsConnector.class, ProcessInitiatorRoleResolver.class);
    ProcessDefinition process = getManagementAPI().deploy(getBusinessArchive(definition, resources, ProcessInitiatorRoleResolver.class, SetVarConnector.class));
    ProcessDefinitionUUID processUUID = process.getUUID();
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    assertNull(getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "start"));
    executeTask(instanceUUID, "Request");
    assertEquals("10", getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "start"));

    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testSetIntegerVariable() throws Exception {
    ProcessDefinition definition =
      ProcessBuilder.createProcess("setVar", "1.0")
        .addIntegerData("start")
        .addGroup("Custom")
          .addGroupResolver(ProcessInitiatorRoleResolver.class.getName())
        .addHumanTask("Request", "Custom")
          .addConnector(Event.taskOnFinish, SetVarConnector.class.getName(), true)
            .addInputParameter("variableName", "start")
            .addInputParameter("value", "${10}")
      .done();

    Map<String, byte[]> resources = getResourcesFromConnector(SetVarsConnector.class, ProcessInitiatorRoleResolver.class);
    ProcessDefinition process = getManagementAPI().deploy(getBusinessArchive(definition, resources, ProcessInitiatorRoleResolver.class, SetVarConnector.class));
    ProcessDefinitionUUID processUUID = process.getUUID();
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    assertNull(getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "start"));
    executeTask(instanceUUID, "Request");
    assertEquals(10, getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "start"));

    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testSetOrderedConnectors() throws Exception {
    ProcessDefinition definition =
      ProcessBuilder.createProcess("setVar", "1.0")
        .addIntegerData("start")
        .addGroup("Custom")
          .addGroupResolver(ProcessInitiatorRoleResolver.class.getName())
        .addHumanTask("Request", "Custom")
          .addConnector(Event.taskOnFinish, SetVarConnector.class.getName(), true)
            .addInputParameter("variableName", "start")
            .addInputParameter("value", "${10}")
          .addConnector(Event.taskOnFinish, SetVarConnector.class.getName(), true)
            .addInputParameter("variableName", "start")
            .addInputParameter("value", "${20}")
      .done();

    Map<String, byte[]> resources = getResourcesFromConnector(SetVarsConnector.class, ProcessInitiatorRoleResolver.class);
    ProcessDefinition process = getManagementAPI().deploy(getBusinessArchive(definition, resources, ProcessInitiatorRoleResolver.class, SetVarConnector.class));
    ProcessDefinitionUUID processUUID = process.getUUID();
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    assertNull(getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "start"));
    executeTask(instanceUUID, "Request");
    assertEquals(20, getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "start"));

    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testOrderedConnectors() throws Exception {
    List<List<Object>> variables = new ArrayList<List<Object>>();
    List<Object> row = new ArrayList<Object>();
    row.add("start");
    row.add("${10}");
    variables.add(row);
    row = new ArrayList<Object>();
    row.add("finish");
    row.add("${true}");
    variables.add(row);

    ProcessDefinition definition =
      ProcessBuilder.createProcess("setVar", "1.0")
        .addIntegerData("start")
        .addBooleanData("finish")
        .addGroup(getLogin())
        .addHumanTask("Request", getLogin())
          .addConnector(Event.taskOnFinish, SetVarsConnector.class.getName(), true)
            .addInputParameter("variables", variables)
      .done();

    Map<String, byte[]> resources = getResourcesFromConnector(SetVarsConnector.class);
    ProcessDefinition process = getManagementAPI().deploy(getBusinessArchive(
        definition, resources, SetVarsConnector.class));

    ProcessDefinitionUUID processUUID = process.getUUID();
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);
    assertNull(getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "start"));
    assertNull(getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "finish"));

    final Collection<TaskInstance> tasks = getQueryRuntimeAPI().getTaskList(instanceUUID, ActivityState.READY);
    assertEquals(1, tasks.size());
    final TaskInstance task = tasks.iterator().next();
    final ActivityInstanceUUID taskUUID = task.getUUID();
    getRuntimeAPI().startTask(taskUUID, false);
    getRuntimeAPI().finishTask(taskUUID, false);
    assertEquals(10, getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "start"));
    assertTrue((Boolean) getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "finish"));

    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testDefaultGroovyMethods() throws Exception {
    List<List<Object>> variables = new ArrayList<List<Object>>();
    List<Object> row = new ArrayList<Object>();
    row.add("myDouble");
    row.add("${toDouble(\"52.21\")}");
    variables.add(row);
    row = new ArrayList<Object>();
    row.add("myBoolean");
    row.add("${asBoolean(\"true \")}");
    variables.add(row);
    
    ProcessDefinition definition = ProcessBuilder.createProcess("defaultGroovyMethods", "1.0")
    .addHuman(getLogin())
    .addBooleanData("myBoolean", false)
    .addDoubleData("myDouble", 0.0)
    .addSystemTask("init")
      .addConnector(Event.automaticOnEnter, SetVarsConnector.class.getName(), true)
        .addInputParameter("variables", variables)
    .addHumanTask("check", getLogin())
    .addTransition("init", "check")
    .done();
    
    Map<String, byte[]> resources = getResourcesFromConnector(SetVarsConnector.class);
    ProcessDefinition process = getManagementAPI().deploy(getBusinessArchive(
        definition, resources, SetVarsConnector.class));

    ProcessDefinitionUUID processUUID = process.getUUID();
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    assertEquals(new Double(52.21), getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "myDouble"));
    assertTrue((Boolean) getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "myBoolean"));

    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().deleteProcess(processUUID);
  }
}
