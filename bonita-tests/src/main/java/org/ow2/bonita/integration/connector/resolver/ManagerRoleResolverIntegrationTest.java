package org.ow2.bonita.integration.connector.resolver;

import java.util.Set;

import org.bonitasoft.connectors.bonita.resolvers.ManagerRoleResolver;
import org.ow2.bonita.APITestCase;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.identity.User;
import org.ow2.bonita.facade.runtime.ActivityInstance;
import org.ow2.bonita.facade.runtime.ActivityState;
import org.ow2.bonita.facade.runtime.InstanceState;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.util.ProcessBuilder;

public class ManagerRoleResolverIntegrationTest extends APITestCase {

  public void testUserWithoutAManager() throws Exception {
    ProcessDefinition process = ProcessBuilder.createProcess("manager", "1.0")
    .addGroup("Manager")
      .addGroupResolver(ManagerRoleResolver.class.getName())
        .addInputParameter("userName", "john")
     .addHumanTask("step", "Manager")
    .done();

    getManagementAPI().deploy(getBusinessArchive(process, null, ManagerRoleResolver.class));
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(process.getUUID());

    Set<ActivityInstance> activities = getQueryRuntimeAPI().getActivityInstances(instanceUUID);
    assertEquals(1, activities.size());
    ActivityInstance activity = activities.iterator().next();
    assertEquals("step", activity.getActivityName());
    Set<String> candidates = activity.getTask().getTaskCandidates();
    assertEquals(0, candidates.size());
    getManagementAPI().deleteProcess(process.getUUID());
  }

  public void testUnknwonUser() throws Exception {
    ProcessDefinition process = ProcessBuilder.createProcess("manager", "1.1")
    .addGroup("Manager")
      .addGroupResolver(ManagerRoleResolver.class.getName())
        .addInputParameter("userName", "joe")
     .addHumanTask("step", "Manager")
    .done();

    getManagementAPI().deploy(getBusinessArchive(process, null, ManagerRoleResolver.class));
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(process.getUUID());
    
    checkState(instanceUUID, ActivityState.FAILED, "step");
    checkState(instanceUUID, InstanceState.STARTED);
      
    getManagementAPI().deleteProcess(process.getUUID());
  }

  public void testUserWithAManager() throws Exception {
    User jane = getIdentityAPI().addUser("jane", "bpm");
    User joe = getIdentityAPI().addUser("joe", "bpm", "Joe", "Doe", null, "", jane.getUUID(), null);

    ProcessDefinition process = ProcessBuilder.createProcess("manager", "1.2")
    .addGroup("Manager")
      .addGroupResolver(ManagerRoleResolver.class.getName())
        .addInputParameter("userName", "joe")
     .addHumanTask("step", "Manager")
    .done();

    getManagementAPI().deploy(getBusinessArchive(process, null, ManagerRoleResolver.class));
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(process.getUUID());

    Set<ActivityInstance> activities = getQueryRuntimeAPI().getActivityInstances(instanceUUID);
    assertEquals(1, activities.size());
    ActivityInstance activity = activities.iterator().next();
    assertEquals("step", activity.getActivityName());
    Set<String> candidates = activity.getTask().getTaskCandidates();
    assertEquals(1, candidates.size());
    assertTrue(candidates.contains(jane.getUsername()));

    getManagementAPI().deleteProcess(process.getUUID());
    getIdentityAPI().removeUserByUUID(jane.getUUID());
    getIdentityAPI().removeUserByUUID(joe.getUUID());
  }

}
