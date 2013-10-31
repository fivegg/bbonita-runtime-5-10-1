package org.ow2.bonita.integration.connector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import junit.framework.Assert;
import junit.framework.ComparisonFailure;

import org.bonitasoft.connectors.bonita.resolvers.ProcessInitiatorRoleResolver;
import org.ow2.bonita.APITestCase;
import org.ow2.bonita.facade.def.element.HookDefinition.Event;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.runtime.ActivityState;
import org.ow2.bonita.facade.runtime.TaskInstance;
import org.ow2.bonita.facade.uuid.ActivityInstanceUUID;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.integration.connector.test.ManyArraysConnector;
import org.ow2.bonita.util.ProcessBuilder;

public class TooManyArraysConnectorIntegrationTest extends APITestCase {

	public void testMap() throws Exception {
		List<String> row1 = new ArrayList<String>();
		row1.add("key1");
		row1.add("value1");
		List<String> row2 = new ArrayList<String>();
		row2.add("key2");
		row2.add("${\"value2\"}");
		List<List<String>> values = new ArrayList<List<String>>();
		values.add(row1);
		values.add(row2);

		ProcessDefinition definition =
      ProcessBuilder.createProcess("Arrays", "1.0")
        .addStringData("get", "null")
        .addGroup("Custom")
          .addGroupResolver(ProcessInitiatorRoleResolver.class.getName())
        .addHumanTask("Request", "Custom")
          .addConnector(Event.taskOnFinish, ManyArraysConnector.class.getName(), true)
            .addInputParameter("map", values)
            .addOutputParameter("${output}", "get")
      .done();

    ProcessDefinition process = getManagementAPI().deploy(getBusinessArchive(
    		definition, getResourcesFromConnector(ManyArraysConnector.class), ManyArraysConnector.class, ProcessInitiatorRoleResolver.class));
    ProcessDefinitionUUID processUUID = process.getUUID();
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    String actual = (String) getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "get");
    Assert.assertEquals("null", actual);

    final Collection<TaskInstance> tasks = getQueryRuntimeAPI().getTaskList(instanceUUID, ActivityState.READY);
    Assert.assertEquals(1, tasks.size());
    final TaskInstance task = tasks.iterator().next();
    final ActivityInstanceUUID taskUUID = task.getUUID();
    getRuntimeAPI().startTask(taskUUID, false);
    getRuntimeAPI().finishTask(taskUUID, false);

    actual = (String) getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "get");
    try {
      assertEquals("{key1=value1, key2=value2}", actual);
    } catch (ComparisonFailure e) {
      assertEquals("{key2=value2, key1=value1}", actual);
    }

    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

	public void testIntegerArray() throws Exception {
		List<String> row1 = new ArrayList<String>();
		row1.add("${1}");
		row1.add("${2}");
		List<String> row2 = new ArrayList<String>();
		row2.add("${2}");
		row2.add("${3}");
		List<List<String>> values = new ArrayList<List<String>>();
		values.add(row1);
		values.add(row2);

		ProcessDefinition definition =
      ProcessBuilder.createProcess("Arrays", "1.0")
        .addStringData("get", "null")
        .addGroup("Custom")
          .addGroupResolver(ProcessInitiatorRoleResolver.class.getName())
        .addHumanTask("Request", "Custom")
          .addConnector(Event.taskOnFinish, ManyArraysConnector.class.getName(), true)
            .addInputParameter("integers", values)
            .addOutputParameter("${intout}", "get")
      .done();

    ProcessDefinition process = getManagementAPI().deploy(getBusinessArchive(
    		definition, getResourcesFromConnector(ManyArraysConnector.class), ManyArraysConnector.class, ProcessInitiatorRoleResolver.class));
    ProcessDefinitionUUID processUUID = process.getUUID();
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    String actual = (String) getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "get");
    Assert.assertEquals("null", actual);

    final Collection<TaskInstance> tasks = getQueryRuntimeAPI().getTaskList(instanceUUID, ActivityState.READY);
    Assert.assertEquals(1, tasks.size());
    final TaskInstance task = tasks.iterator().next();
    final ActivityInstanceUUID taskUUID = task.getUUID();
    getRuntimeAPI().startTask(taskUUID, false);
    getRuntimeAPI().finishTask(taskUUID, false);

    actual = (String) getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "get");
    Assert.assertEquals("1|2|2|3|", actual);

    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }
}
