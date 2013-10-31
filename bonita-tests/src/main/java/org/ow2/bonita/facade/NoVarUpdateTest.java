package org.ow2.bonita.facade;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;

import org.bonitasoft.connectors.bonita.SetVarConnector;
import org.ow2.bonita.APITestCase;
import org.ow2.bonita.env.EnvConstants;
import org.ow2.bonita.env.generator.DbHistoryEnvGenerator;
import org.ow2.bonita.env.generator.EnvEntry;
import org.ow2.bonita.env.generator.EnvGenerator;
import org.ow2.bonita.facade.def.element.HookDefinition.Event;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.runtime.ActivityInstance;
import org.ow2.bonita.facade.runtime.ActivityState;
import org.ow2.bonita.facade.runtime.ProcessInstance;
import org.ow2.bonita.facade.runtime.TaskInstance;
import org.ow2.bonita.facade.uuid.ActivityInstanceUUID;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.util.ProcessBuilder;

public class NoVarUpdateTest extends APITestCase {

  public static EnvGenerator getEnvGenerator() {
    final EnvGenerator envGenerator = new DbHistoryEnvGenerator();
    
    EnvEntry entry = EnvGenerator.getEnvEntry(EnvConstants.VARIABLES_TAG,
        "Properties of variables management.",
        "<" + EnvConstants.VARIABLES_TAG + " store-history='false'/>",
        true);
    
    envGenerator.addApplicationEntry(entry);
    return envGenerator;
  }
  
  public void testActivityVarUpdate() throws Exception {
    ProcessDefinition process = ProcessBuilder.createProcess("activityVar", "1.0")
    .addHuman(getLogin())
    .addSystemTask("init")
    .addHumanTask("human", getLogin())
      .addStringData("var", "initial")
      .addConnector(Event.taskOnFinish, SetVarConnector.class.getName(), true)
        .addInputParameter("setVariableName", "var")
        .addInputParameter("setValue", "newvalue")
    .addTransition("init", "human")
    .done();
    
    process = getManagementAPI().deploy(getBusinessArchive(process, null, SetVarConnector.class));
    ProcessDefinitionUUID processUUID = process.getUUID();
    
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);
    
    final Collection<TaskInstance> tasks = getQueryRuntimeAPI().getTaskList(instanceUUID, ActivityState.READY);
    assertEquals(1, tasks.size());
    final ActivityInstanceUUID activityUUID = tasks.iterator().next().getUUID();
    ActivityInstance activityInstance = getQueryRuntimeAPI().getActivityInstance(activityUUID);
    
    //check initial value
    assertEquals("initial", getQueryRuntimeAPI().getActivityInstanceVariable(activityUUID, "var"));
    assertEquals("initial", activityInstance.getLastKnownVariableValues().get("var"));
    assertEquals("initial", activityInstance.getVariableValueBeforeStarted("var"));
    //check no var update
    assertEquals(Collections.emptyList(), activityInstance.getVariableUpdates());
    
    final long readyTime = System.currentTimeMillis();
    
    //wait to check last update date...
    Thread.sleep(2000);
    
    //Execute task
    getRuntimeAPI().executeTask(activityUUID, true);
    activityInstance = getQueryRuntimeAPI().getActivityInstance(activityUUID);
    
    //check new value
    assertEquals("newvalue", getQueryRuntimeAPI().getActivityInstanceVariable(activityUUID, "var"));
    assertEquals("newvalue", activityInstance.getLastKnownVariableValues().get("var"));
    assertEquals("newvalue", activityInstance.getVariableValueBeforeStarted("var"));
    
    //check no var update
    assertEquals(Collections.emptyList(), activityInstance.getVariableUpdates());
    
    //check last update date
    assertTrue(activityInstance.getLastUpdateDate().after(new Date(readyTime)));
    
    getManagementAPI().deleteProcess(processUUID);
  }
  
  public void testInstanceVarUpdate() throws Exception {
    ProcessDefinition process = ProcessBuilder.createProcess("instanceVar", "1.0")
    .addHuman(getLogin())
    .addStringData("var", "initial")
    .addSystemTask("init")
    .addHumanTask("human", getLogin())
    .addTransition("init", "human")
    .done();

    process = getManagementAPI().deploy(getBusinessArchive(process, null, SetVarConnector.class));
    ProcessDefinitionUUID processUUID = process.getUUID();
    
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);
    
    ProcessInstance instance = getQueryRuntimeAPI().getProcessInstance(instanceUUID);
    
    //check initial value
    assertEquals("initial", getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "var"));
    assertEquals("initial", instance.getLastKnownVariableValues().get("var"));
    assertEquals("initial", instance.getInitialVariableValue("var"));
    //check no var update
    assertEquals(Collections.emptyList(), instance.getVariableUpdates());
    
    final long readyTime = System.currentTimeMillis();
    
    //wait to check last update date...
    Thread.sleep(2000);
    
    getRuntimeAPI().setProcessInstanceVariable(instanceUUID, "var", "newvalue");
    
    instance = getQueryRuntimeAPI().getProcessInstance(instanceUUID);
    
    //check initial value
    assertEquals("newvalue", getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "var"));
    assertEquals("newvalue", instance.getLastKnownVariableValues().get("var"));
    assertEquals("newvalue", instance.getInitialVariableValue("var"));
    //check no var update
    assertEquals(Collections.emptyList(), instance.getVariableUpdates());
    
    //check last update date
    assertTrue(instance.getLastUpdate().after(new Date(readyTime)));
    
    getManagementAPI().deleteProcess(processUUID);
  }
}
