package org.ow2.bonita.integration.connector;

import org.bonitasoft.connectors.bonita.GetProcessInstanceInitiator;
import org.bonitasoft.connectors.bonita.resolvers.ProcessInitiatorRoleResolver;
import org.ow2.bonita.APITestCase;
import org.ow2.bonita.facade.def.element.HookDefinition.Event;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.runtime.ActivityState;
import org.ow2.bonita.facade.runtime.InstanceState;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.util.ProcessBuilder;

public class GetProcessInstanceInitiatorIntegrationTest extends APITestCase {

  public void testGetProcessInstanceInitiator() throws Exception {
    ProcessDefinition test = ProcessBuilder.createProcess("test", "1.0")
    .addGroup("humans")
      .addGroupResolver(ProcessInitiatorRoleResolver.class.getName())
    .addHumanTask("this", "humans")
    .done();
    
    test = getManagementAPI().deploy(getBusinessArchive(test, null, ProcessInitiatorRoleResolver.class));
    loginAs("james", "bpm");
    ProcessInstanceUUID testUUID1 = getRuntimeAPI().instantiateProcess(test.getUUID());

    ProcessDefinition definition = ProcessBuilder.createProcess("first", "1.0")
      .addHuman(getLogin())
      .addStringData("user")
      .addHumanTask("task", getLogin())
        .addConnector(Event.taskOnReady, GetProcessInstanceInitiator.class.getName(), true)
          .addInputParameter("instanceUUID", testUUID1)
          .addOutputParameter("instanceInitiator", "user")
    .done();
    
    loginAs(getLogin(), getPassword());
    definition = getManagementAPI().deploy(getBusinessArchive(definition, null, GetProcessInstanceInitiator.class));
    ProcessInstanceUUID defUUID = getRuntimeAPI().instantiateProcess(definition.getUUID());
    
    String actual = (String) getQueryRuntimeAPI().getProcessInstanceVariable(defUUID, "user");
    assertEquals("james", actual);
    
    getManagementAPI().deleteProcess(test.getUUID());
    getManagementAPI().deleteProcess(definition.getUUID());
  }

  public void testGetUnknwonInstance() throws Exception {
    ProcessDefinition definition = ProcessBuilder.createProcess("first", "1.0")
      .addHuman(getLogin())
      .addStringData("user")
      .addHumanTask("task", getLogin())
        .addConnector(Event.taskOnReady, GetProcessInstanceInitiator.class.getName(), true)
          .addInputParameter("instanceUUID", "toto")
          .addOutputParameter("instanceInitiator", "user")
    .done();
    
    loginAs(getLogin(), getPassword());
    definition = getManagementAPI().deploy(getBusinessArchive(definition, null, GetProcessInstanceInitiator.class));
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(definition.getUUID());
    
    checkState(instanceUUID, ActivityState.FAILED, "task");
    checkState(instanceUUID, InstanceState.STARTED);
    
    getManagementAPI().deleteProcess(definition.getUUID());
  }

}
