package org.ow2.bonita.integration.connector.filter;

import java.util.Collection;

import junit.framework.Assert;

import org.bonitasoft.connectors.bonita.filters.UniqueRandomFilter;
import org.bonitasoft.connectors.bonita.resolvers.UserListRoleResolver;
import org.ow2.bonita.APITestCase;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.runtime.ActivityState;
import org.ow2.bonita.facade.runtime.TaskInstance;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.util.ProcessBuilder;

public class UniqueRandomFilterIntegrationTest extends APITestCase {

  public void test() throws Exception {
    ProcessDefinition definition = ProcessBuilder.createProcess("unique", "1.0")
    .addGroup("multi")
      .addGroupResolver(UserListRoleResolver.class.getName())
        .addInputParameter("setUsers", "john, james, jack")
    .addHumanTask("check", "multi")
      .addFilter(UniqueRandomFilter.class.getName())
    .done();
    
    ProcessDefinition process = getManagementAPI().deploy(getBusinessArchive(definition, null, UserListRoleResolver.class, UniqueRandomFilter.class));
    ProcessDefinitionUUID processUUID = process.getUUID();    
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);
    JohnOrJackOrJames(instanceUUID);
    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID); 
  }
  
  private void JohnOrJackOrJames(ProcessInstanceUUID instanceUUID) throws Exception {
    int tasks = 0;
    loginAs("john", "bpm");
    Collection<TaskInstance> taskActivities = 
      getQueryRuntimeAPI().getTaskList(instanceUUID, ActivityState.READY);
    tasks += taskActivities.size();

    loginAs("jack", "bpm");
    taskActivities = getQueryRuntimeAPI().getTaskList(instanceUUID, ActivityState.READY);
    tasks += taskActivities.size();

    loginAs("james", "bpm");
    taskActivities = getQueryRuntimeAPI().getTaskList(instanceUUID, ActivityState.READY);
    tasks += taskActivities.size();

    Assert.assertEquals(1, tasks);
  }
}
