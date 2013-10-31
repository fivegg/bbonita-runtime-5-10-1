package org.ow2.bonita.integration.connector;

import java.util.Date;
import java.util.Set;

import org.bonitasoft.connectors.scripting.GroovyConnector;
import org.ow2.bonita.APITestCase;
import org.ow2.bonita.facade.def.element.HookDefinition.Event;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.runtime.ActivityState;
import org.ow2.bonita.facade.runtime.TaskInstance;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.util.ProcessBuilder;

public class GroovyConnectorIntegrationTest extends APITestCase {

  public void testGroovyBooleanScriptInAnActivity() throws Exception {
    ProcessDefinition definition = 
      ProcessBuilder.createProcess("groovy", "1.1")
      .addBooleanData("running", false)
      .addHuman("john")
      .addHumanTask("groove", "john")
      .addConnector(Event.taskOnStart, GroovyConnector.class.getName(), true)
      .addInputParameter("script", "running = true")
      .done();

    ProcessDefinition process = getManagementAPI().deploy(getBusinessArchive(
        definition, getResourcesFromConnector(GroovyConnector.class), GroovyConnector.class));
    ProcessDefinitionUUID processUUID = process.getUUID();
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);
    loginAs("john", "bpm");
    Boolean actual = (Boolean) getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "running");
    assertFalse(actual);
    Set<TaskInstance> tasks = getQueryRuntimeAPI().getTasks(instanceUUID);
    TaskInstance task = tasks.iterator().next();
    getRuntimeAPI().startTask(task.getUUID(), true);
    actual = (Boolean) getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "running");
    assertTrue(actual);
    getRuntimeAPI().finishTask(task.getUUID(), true);

    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testGroovyScriptIntegerInAnActivity() throws Exception {
    ProcessDefinition definition = 
      ProcessBuilder.createProcess("groovy", "1.1")
      .addIntegerData("count", 0)
      .addHuman("john")
      .addHumanTask("groove", "john")
      .addConnector(Event.taskOnStart, GroovyConnector.class.getName(), true)
      .addInputParameter("script", "count++")
      .done();

    ProcessDefinition process = getManagementAPI().deploy(getBusinessArchive(
        definition, getResourcesFromConnector(GroovyConnector.class), GroovyConnector.class));
    ProcessDefinitionUUID processUUID = process.getUUID();
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);
    loginAs("john", "bpm");
    Integer actual = (Integer) getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "count");
    assertEquals(Integer.valueOf(0), actual);
    Set<TaskInstance> tasks = getQueryRuntimeAPI().getTasks(instanceUUID);
    TaskInstance task = tasks.iterator().next();
    getRuntimeAPI().startTask(task.getUUID(), true);
    actual = (Integer) getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "count");
    assertEquals(Integer.valueOf(1), actual);
    getRuntimeAPI().finishTask(task.getUUID(), true);

    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID); 
  }

  public void testGroovyScriptDateInAnActivity() throws Exception {
    Date today = new Date();
    Thread.sleep(1000);
    ProcessDefinition definition = 
      ProcessBuilder.createProcess("groovy", "1.1")
      .addDateData("today", today)
      .addHuman("john")
      .addHumanTask("groove", "john")
      .addConnector(Event.taskOnStart, GroovyConnector.class.getName(), true)
      .addInputParameter("script", "today = new Date()")
      .done();

    ProcessDefinition process = getManagementAPI().deploy(getBusinessArchive(
        definition, getResourcesFromConnector(GroovyConnector.class), GroovyConnector.class));
    ProcessDefinitionUUID processUUID = process.getUUID();
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);
    loginAs("john", "bpm");
    Date actual = (Date) getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "today");
    assertEquals(today, actual);
    Set<TaskInstance> tasks = getQueryRuntimeAPI().getTasks(instanceUUID);
    TaskInstance task = tasks.iterator().next();
    getRuntimeAPI().startTask(task.getUUID(), true);
    actual = (Date) getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "today");
    assertTrue(today.before(actual));
    getRuntimeAPI().finishTask(task.getUUID(), true);

    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID); 
  }

  public void testGroovyBooleanScriptInAnActivityNewVariable() throws Exception {
    ProcessDefinition definition = 
      ProcessBuilder.createProcess("groovy", "1.1")
      .addBooleanData("running", false)
      .addHuman("john")
      .addHumanTask("groove", "john")
      .addConnector(Event.taskOnStart, GroovyConnector.class.getName(), true)
      .addInputParameter("script", "running = true; return 0")
      .addOutputParameter("${result}", "count")
      .done();

    ProcessDefinition process = getManagementAPI().deploy(getBusinessArchive(
        definition, getResourcesFromConnector(GroovyConnector.class), GroovyConnector.class));
    ProcessDefinitionUUID processUUID = process.getUUID();
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);
    loginAs("john", "bpm");
    Boolean actual = (Boolean) getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "running");
    assertFalse(actual);
    Set<TaskInstance> tasks = getQueryRuntimeAPI().getTasks(instanceUUID);
    TaskInstance task = tasks.iterator().next();
    getRuntimeAPI().startTask(task.getUUID(), true);
    Thread.sleep(500);
    TaskInstance failedTask = getQueryRuntimeAPI().getTask(task.getUUID());
    assertEquals(ActivityState.FAILED, failedTask.getState());

    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testGroovyBooleanScript() throws Exception {
    ProcessDefinition definition = 
      ProcessBuilder.createProcess("groovy", "1.1")
      .addBooleanData("running", false)
      .addHuman("john")
      .addHumanTask("groove", "john")
      .addConnector(Event.taskOnStart, GroovyConnector.class.getName(), true)
      .addInputParameter("script", "true")
      .addOutputParameter("${result}", "running")
      .done();

    ProcessDefinition process = getManagementAPI().deploy(getBusinessArchive(
        definition, getResourcesFromConnector(GroovyConnector.class), GroovyConnector.class));
    ProcessDefinitionUUID processUUID = process.getUUID();
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);
    loginAs("john", "bpm");
    Boolean actual = (Boolean) getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "running");
    assertFalse(actual);
    Set<TaskInstance> tasks = getQueryRuntimeAPI().getTasks(instanceUUID);
    TaskInstance task = tasks.iterator().next();
    getRuntimeAPI().startTask(task.getUUID(), true);
    actual = (Boolean) getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "running");
    assertTrue(actual);
    getRuntimeAPI().finishTask(task.getUUID(), true);

    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID); 
  }

  public void testGroovyBooleanScript2() throws Exception {
    ProcessDefinition definition = 
      ProcessBuilder.createProcess("groovy", "1.1")
      .addBooleanData("running", false)
      .addHuman("john")
      .addHumanTask("groove", "john")
      .addConnector(Event.taskOnStart, GroovyConnector.class.getName(), true)
      .addInputParameter("script", "running = true")
      .addOutputParameter("${result}", "running")
      .done();

    ProcessDefinition process = getManagementAPI().deploy(getBusinessArchive(
        definition, getResourcesFromConnector(GroovyConnector.class), GroovyConnector.class));
    ProcessDefinitionUUID processUUID = process.getUUID();
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);
    loginAs("john", "bpm");
    Boolean actual = (Boolean) getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "running");
    assertFalse(actual);
    Set<TaskInstance> tasks = getQueryRuntimeAPI().getTasks(instanceUUID);
    TaskInstance task = tasks.iterator().next();
    getRuntimeAPI().startTask(task.getUUID(), true);
    actual = (Boolean) getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "running");
    assertTrue(actual);
    getRuntimeAPI().finishTask(task.getUUID(), true);

    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID); 
  }

  public void testGroovyScriptIntegerInAnActivity2() throws Exception {
    ProcessDefinition definition = 
      ProcessBuilder.createProcess("groovy", "1.1")
      .addIntegerData("count", 0)
      .addHuman("john")
      .addHumanTask("groove", "john")
      .addConnector(Event.taskOnStart, GroovyConnector.class.getName(), true)
      .addInputParameter("script", "++count")
      .addOutputParameter("${result}", "count")
      .done();

    ProcessDefinition process = getManagementAPI().deploy(getBusinessArchive(
        definition, getResourcesFromConnector(GroovyConnector.class), GroovyConnector.class));
    ProcessDefinitionUUID processUUID = process.getUUID();
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);
    loginAs("john", "bpm");
    Integer actual = (Integer) getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "count");
    assertEquals(Integer.valueOf(0), actual);
    Set<TaskInstance> tasks = getQueryRuntimeAPI().getTasks(instanceUUID);
    TaskInstance task = tasks.iterator().next();
    getRuntimeAPI().startTask(task.getUUID(), true);
    actual = (Integer) getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "count");
    assertEquals(Integer.valueOf(1), actual);
    getRuntimeAPI().finishTask(task.getUUID(), true);

    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID); 
  }

  public void testTwoOutputsInAGrrovyExpression() throws Exception {
    ProcessDefinition definition = 
      ProcessBuilder.createProcess("groovy", "1.1")
      .addIntegerData("count", 0)
      .addHuman("john")
      .addHumanTask("groove", "john")
      .addConnector(Event.taskOnStart, GroovyConnector.class.getName(), true)
      .addInputParameter("script", "++count")
      .addOutputParameter("${(result + result) * 4}", "count")
      .done();

    ProcessDefinition process = getManagementAPI().deploy(getBusinessArchive(
        definition, getResourcesFromConnector(GroovyConnector.class), GroovyConnector.class));
    ProcessDefinitionUUID processUUID = process.getUUID();
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);
    loginAs("john", "bpm");
    Integer actual = (Integer) getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "count");
    assertEquals(Integer.valueOf(0), actual);
    Set<TaskInstance> tasks = getQueryRuntimeAPI().getTasks(instanceUUID);
    TaskInstance task = tasks.iterator().next();
    getRuntimeAPI().startTask(task.getUUID(), true);
    actual = (Integer) getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "count");
    assertEquals(Integer.valueOf(8), actual);
    getRuntimeAPI().finishTask(task.getUUID(), true);

    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID); 
  }

  public void testGroovyConnectorInFinishOfActivityWithPropagate() throws Exception {
      ProcessDefinition definition = 
        ProcessBuilder.createProcess("groovy", "1.2")
        .addBooleanData("running", false)
        .addHuman("john")
        .addHumanTask("groove", "john")
        .addConnector(Event.taskOnFinish, GroovyConnector.class.getName(), true)
        .addInputParameter("script", "running = true")
        .done();

      ProcessDefinition process = getManagementAPI().deploy(getBusinessArchive(
          definition, getResourcesFromConnector(GroovyConnector.class), GroovyConnector.class));
      ProcessDefinitionUUID processUUID = process.getUUID();
      ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);
      loginAs("john", "bpm");
      Boolean actual = (Boolean) getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "running");
      assertFalse(actual);
      Set<TaskInstance> tasks = getQueryRuntimeAPI().getTasks(instanceUUID);
      TaskInstance task = tasks.iterator().next();
      getRuntimeAPI().executeTask(task.getUUID(), true);
      actual = (Boolean) getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "running");
      assertTrue(actual);

      getManagementAPI().disable(processUUID);
      getManagementAPI().deleteProcess(processUUID);
    }
}
