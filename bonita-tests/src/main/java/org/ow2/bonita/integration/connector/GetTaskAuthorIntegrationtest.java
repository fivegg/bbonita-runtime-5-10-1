package org.ow2.bonita.integration.connector;

import org.bonitasoft.connectors.bonita.GetTaskAuthor;
import org.ow2.bonita.APITestCase;
import org.ow2.bonita.facade.def.element.HookDefinition.Event;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.runtime.ActivityState;
import org.ow2.bonita.facade.runtime.InstanceState;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.util.ProcessBuilder;

public class GetTaskAuthorIntegrationtest extends APITestCase {

  public void testGetSystemAuthor() throws Exception {
    ProcessDefinition definition = ProcessBuilder.createProcess("author", "3.0")
      .addHuman("james")
      .addStringData("author")
      .addSystemTask("first")
      .addHumanTask("last", "james")
        .addConnector(Event.taskOnReady, GetTaskAuthor.class.getName(), true)
          .addInputParameter("taskName", "first")
          .addOutputParameter("author", "author")
      .addTransition("first", "last")
    .done();

    definition = getManagementAPI().deploy(getBusinessArchive(definition, null, GetTaskAuthor.class));
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(definition.getUUID());

    String actual = (String) getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "author");
    assertNull(actual);

    getManagementAPI().deleteProcess(definition.getUUID());
  }
  
  public void testGetAuthor() throws Exception {
    ProcessDefinition definition = ProcessBuilder.createProcess("author", "3.1")
      .addHuman("john")
      .addHuman("james")
      .addStringData("author")
      .addHumanTask("first", "john")
      .addHumanTask("last", "james")
        .addConnector(Event.taskOnReady, GetTaskAuthor.class.getName(), true)
          .addInputParameter("taskName", "first")
          .addOutputParameter("author", "author")
      .addTransition("first", "last")
    .done();

    definition = getManagementAPI().deploy(getBusinessArchive(definition, null, GetTaskAuthor.class));
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(definition.getUUID());
    loginAs("john", getPassword());
    executeTask(instanceUUID, "first");
    String actual = (String) getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "author");
    assertEquals("john", actual);

    getManagementAPI().deleteProcess(definition.getUUID());
  }

  public void testGetUnknownTask() throws Exception {
    ProcessDefinition definition = ProcessBuilder.createProcess("author", "3.1")
      .addHuman("john")
      .addHuman("james")
      .addStringData("author")
      .addHumanTask("first", "john")
      .addHumanTask("last", "james")
        .addConnector(Event.taskOnReady, GetTaskAuthor.class.getName(), true)
          .addInputParameter("taskName", "firsto")
          .addOutputParameter("author", "author")
      .addTransition("first", "last")
    .done();

    definition = getManagementAPI().deploy(getBusinessArchive(definition, null, GetTaskAuthor.class));
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(definition.getUUID());
    loginAs("john", getPassword());
    
    executeTask(instanceUUID, "first");
    
    checkState(instanceUUID, ActivityState.FINISHED, "first");
    checkState(instanceUUID, ActivityState.FAILED, "last");
    checkState(instanceUUID, InstanceState.STARTED);
    
    getManagementAPI().deleteProcess(definition.getUUID());
  }

}
