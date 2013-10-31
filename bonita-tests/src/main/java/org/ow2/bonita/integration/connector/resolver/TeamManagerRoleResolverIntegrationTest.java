package org.ow2.bonita.integration.connector.resolver;

import java.util.Set;

import org.bonitasoft.connectors.bonita.resolvers.TeamMembersRoleResolver;
import org.ow2.bonita.APITestCase;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.identity.User;
import org.ow2.bonita.facade.runtime.ActivityInstance;
import org.ow2.bonita.facade.runtime.ActivityState;
import org.ow2.bonita.facade.runtime.InstanceState;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.util.ProcessBuilder;

public class TeamManagerRoleResolverIntegrationTest extends APITestCase {

  public void testUserWithoutAManager() throws Exception {
    ProcessDefinition process = ProcessBuilder.createProcess("manager", "1.0")
    .addGroup("TeamMembers")
      .addGroupResolver(TeamMembersRoleResolver.class.getName())
        .addInputParameter("managerName", "admin")
     .addHumanTask("step", "TeamMembers")
    .done();

    getManagementAPI().deploy(getBusinessArchive(process, null, TeamMembersRoleResolver.class));
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
    .addGroup("TeamMembers")
      .addGroupResolver(TeamMembersRoleResolver.class.getName())
        .addInputParameter("managerName", "joe")
     .addHumanTask("step", "TeamMembers")
    .done();

    getManagementAPI().deploy(getBusinessArchive(process, null, TeamMembersRoleResolver.class));

    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(process.getUUID());
    
    checkState(instanceUUID, ActivityState.FAILED, "step");
    checkState(instanceUUID, InstanceState.STARTED);
    
    getManagementAPI().deleteProcess(process.getUUID());
  }

  public void testUserWithAManager() throws Exception {
    User jane = getIdentityAPI().addUser("jane", "bpm");
    User joe = getIdentityAPI().addUser("joe", "bpm", "Joe", "Doe", null, "", jane.getUUID(), null);
    User jd = getIdentityAPI().addUser("jd", "bpm", "JD", "Doe", null, "", jane.getUUID(), null);

    ProcessDefinition process = ProcessBuilder.createProcess("manager", "1.2")
    .addGroup("TeamMembers")
      .addGroupResolver(TeamMembersRoleResolver.class.getName())
        .addInputParameter("managerName", "jane")
     .addHumanTask("step", "TeamMembers")
    .done();

    getManagementAPI().deploy(getBusinessArchive(process, null, TeamMembersRoleResolver.class));
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(process.getUUID());

    Set<ActivityInstance> activities = getQueryRuntimeAPI().getActivityInstances(instanceUUID);
    assertEquals(1, activities.size());
    ActivityInstance activity = activities.iterator().next();
    assertEquals("step", activity.getActivityName());
    Set<String> candidates = activity.getTask().getTaskCandidates();
    assertEquals(2, candidates.size());
    assertTrue(candidates.contains(joe.getUsername()));
    assertTrue(candidates.contains(jd.getUsername()));

    getManagementAPI().deleteProcess(process.getUUID());
    getIdentityAPI().removeUserByUUID(jane.getUUID());
    getIdentityAPI().removeUserByUUID(joe.getUUID());
    getIdentityAPI().removeUserByUUID(jd.getUUID());
  }

}
