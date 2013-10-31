package org.ow2.bonita.integration.connector;

import org.bonitasoft.connectors.bonita.GetUser;
import org.ow2.bonita.APITestCase;
import org.ow2.bonita.facade.def.element.HookDefinition.Event;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.runtime.ActivityState;
import org.ow2.bonita.facade.runtime.InstanceState;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.util.ProcessBuilder;

public class GetUserIntegrationTest extends APITestCase {

  public void testGetAdmin() throws Exception {
    ProcessDefinition definition = ProcessBuilder.createProcess("hello", "3.2")
    .addStringData("userName")
    .addStringData("firstName")
    .addStringData("lastName")
    .addHuman("john")
    .addHumanTask("task","john")
      .addConnector(Event.taskOnReady, GetUser.class.getName(), true)
        .addInputParameter("username", "admin")
        .addOutputParameter("user.getUsername()", "userName")
        .addOutputParameter("user.getFirstName()", "firstName")
        .addOutputParameter("user.getLastName()", "lastName")
    .done();

    definition = getManagementAPI().deploy(getBusinessArchive(definition, null, GetUser.class));
    loginAs("john", "bpm");
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(definition.getUUID());
    String userName = (String) getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "userName");
    assertEquals("admin", userName);
    String firstName = (String) getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "firstName");
    assertNull(firstName);
    String lastName = (String) getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "lastName");
    assertNull(lastName);

    getManagementAPI().deleteProcess(definition.getUUID());
  }

  public void testGetJames() throws Exception {
    ProcessDefinition definition = ProcessBuilder.createProcess("hello", "3.2")
    .addStringData("userName")
    .addStringData("firstName")
    .addStringData("lastName")
    .addHuman("john")
    .addHumanTask("task","john")
      .addConnector(Event.taskOnReady, GetUser.class.getName(), true)
        .addInputParameter("username", "james")
        .addOutputParameter("user.getUsername()", "userName")
        .addOutputParameter("user.getFirstName()", "firstName")
        .addOutputParameter("user.getLastName()", "lastName")
    .done();

    definition = getManagementAPI().deploy(getBusinessArchive(definition, null, GetUser.class));
    loginAs("john", "bpm");
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(definition.getUUID());
    String userName = (String) getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "userName");
    assertEquals("james", userName);
    String firstName = (String) getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "firstName");
    assertEquals("James", firstName);
    String lastName = (String) getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "lastName");
    assertEquals("Doe", lastName);

    getManagementAPI().deleteProcess(definition.getUUID());
  }

  public void testGetUnknownUser() throws Exception {
    ProcessDefinition definition = ProcessBuilder.createProcess("hello", "3.2")
    .addStringData("userName")
    .addStringData("firstName")
    .addStringData("lastName")
    .addHuman("john")
    .addHumanTask("task","john")
      .addConnector(Event.taskOnReady, GetUser.class.getName(), true)
        .addInputParameter("username", "matti")
        .addOutputParameter("user.getUsername()", "userName")
        .addOutputParameter("user.getFirstName()", "firstName")
        .addOutputParameter("user.getLastName()", "lastName")
    .done();

    definition = getManagementAPI().deploy(getBusinessArchive(definition, null, GetUser.class));
    loginAs("john", "bpm");
    
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(definition.getUUID());
    checkState(instanceUUID, ActivityState.FAILED, "task");
    checkState(instanceUUID, InstanceState.STARTED);
    
    
    getManagementAPI().deleteProcess(definition.getUUID());
  }

}
