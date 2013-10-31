package org.ow2.bonita.integration.connector.resolver;

import java.util.Collection;

import javax.security.auth.login.LoginException;

import junit.framework.Assert;

import org.bonitasoft.connectors.bonita.resolvers.ProcessInitiatorRoleResolver;
import org.ow2.bonita.APITestCase;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.runtime.ActivityState;
import org.ow2.bonita.facade.runtime.TaskInstance;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.util.BonitaException;
import org.ow2.bonita.util.ProcessBuilder;

public class ProcessInitiatiorRoleResolverIntegrationTest extends APITestCase {

  public void testSimpleInstanceInitiatiorMapper() throws BonitaException, LoginException {
    ProcessDefinition definition =
      ProcessBuilder.createProcess("InstanceInitiatior", "1.0")
      .addGroup("Users")
        .addGroupResolver(ProcessInitiatorRoleResolver.class.getName())
      .addHumanTask("welcome", "Users")
    .done();
    ProcessDefinition process = getManagementAPI().deploy(getBusinessArchive(definition, null, ProcessInitiatorRoleResolver.class));
    ProcessDefinitionUUID processUUID = process.getUUID();
    loginAs("john", "bpm");
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);
    loginAs("admin", "bpm");
    Collection<TaskInstance> taskActivities = 
      getQueryRuntimeAPI().getTaskList(instanceUUID, ActivityState.READY);
    Assert.assertEquals(0, taskActivities.size());
    loginAs("john", "bpm");
    taskActivities = 
      getQueryRuntimeAPI().getTaskList(instanceUUID, ActivityState.READY);
    Assert.assertEquals(1, taskActivities.size());
    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }
}
