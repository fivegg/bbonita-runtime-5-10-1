package org.ow2.bonita.integration.connector.resolver;

import java.util.Set;

import org.bonitasoft.connectors.bonita.resolvers.GroupUsersRoleResolver;
import org.ow2.bonita.APITestCase;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.identity.Group;
import org.ow2.bonita.facade.identity.Membership;
import org.ow2.bonita.facade.identity.Role;
import org.ow2.bonita.facade.identity.User;
import org.ow2.bonita.facade.runtime.TaskInstance;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.util.ProcessBuilder;

public class GroupUsersRoleResolverIntergrationTest extends APITestCase {

  Role isa = null;
  Role aiti = null;
  Role lapsi = null;
  Role kenguru = null;

  Group hahmo = null;
  Group muumi = null;
  Group ihminen = null;
  Group elain = null;

  User pikkuMyy = null;
  User nipsu = null;
  User niisku = null;
  User muumipeikko = null;
  User muumimamma = null;
  User muumipappa = null;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    isa = getIdentityAPI().addRole("isä");
    aiti = getIdentityAPI().addRole("äiti");
    lapsi = getIdentityAPI().addRole("lapsi");
    kenguru = getIdentityAPI().addRole("kenguru");

    hahmo = getIdentityAPI().addGroup("hahmo", null);
    muumi = getIdentityAPI().addGroup("Muumi", hahmo.getUUID());
    ihminen = getIdentityAPI().addGroup("ihminen", hahmo.getUUID());
    elain = getIdentityAPI().addGroup("eläin", hahmo.getUUID());

    pikkuMyy = getIdentityAPI().addUser("Pikku Myy", "azerty");
    nipsu = getIdentityAPI().addUser("Nipsu", "ytreza");
    niisku = getIdentityAPI().addUser("Niisku", "azrtey");
    muumipeikko = getIdentityAPI().addUser("Muumipeikko", "qwerty");
    muumimamma = getIdentityAPI().addUser("Muumimamma", "qsdfgh");
    muumipappa = getIdentityAPI().addUser("Muumipappa", "wxsder");

    Membership mummiIsa = getIdentityAPI().getMembershipForRoleAndGroup(isa.getUUID(), muumi.getUUID());
    Membership mummiAiti = getIdentityAPI().getMembershipForRoleAndGroup(aiti.getUUID(), muumi.getUUID());
    Membership mummiLapsi = getIdentityAPI().getMembershipForRoleAndGroup(lapsi.getUUID(), muumi.getUUID());

    Membership elainKenguru = getIdentityAPI().getMembershipForRoleAndGroup(kenguru.getUUID(), elain.getUUID());
    Membership ihminenLaspi = getIdentityAPI().getMembershipForRoleAndGroup(lapsi.getUUID(), ihminen.getUUID());

    getIdentityAPI().addMembershipToUser(pikkuMyy.getUUID(), ihminenLaspi.getUUID());
    getIdentityAPI().addMembershipToUser(nipsu.getUUID(), elainKenguru.getUUID());

    getIdentityAPI().addMembershipToUser(niisku.getUUID(), mummiLapsi.getUUID());
    getIdentityAPI().addMembershipToUser(muumipeikko.getUUID(), mummiLapsi.getUUID());
    getIdentityAPI().addMembershipToUser(muumipappa.getUUID(), mummiIsa.getUUID());
    getIdentityAPI().addMembershipToUser(muumimamma.getUUID(), mummiAiti.getUUID());
  }

  @Override
  protected void tearDown() throws Exception {
    getIdentityAPI().removeGroupByUUID(hahmo.getUUID());
    getIdentityAPI().removeRoleByUUID(isa.getUUID());
    getIdentityAPI().removeRoleByUUID(aiti.getUUID());
    getIdentityAPI().removeRoleByUUID(lapsi.getUUID());
    getIdentityAPI().removeRoleByUUID(kenguru.getUUID());
    
    getIdentityAPI().removeUserByUUID(pikkuMyy.getUUID());
    getIdentityAPI().removeUserByUUID(nipsu.getUUID());
    getIdentityAPI().removeUserByUUID(niisku.getUUID());
    getIdentityAPI().removeUserByUUID(muumipeikko.getUUID());
    getIdentityAPI().removeUserByUUID(muumimamma.getUUID());
    getIdentityAPI().removeUserByUUID(muumipappa.getUUID());
    super.tearDown();
  }

  public void testGetUsersOfASubGroup() throws Exception {
    ProcessDefinition process = ProcessBuilder.createProcess("groups", "1.1")
    .addGroup("Users")
      .addGroupResolver(GroupUsersRoleResolver.class.getName())
        .addInputParameter("setGroupPath", "/hahmo/Muumi")
    .addHumanTask("welcome", "Users")
    .done();

    process = getManagementAPI().deploy(getBusinessArchive(process, null, GroupUsersRoleResolver.class));
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(process.getUUID());

    Set<TaskInstance> tasks = getQueryRuntimeAPI().getTasks(instanceUUID);
    assertEquals(1, tasks.size());
    TaskInstance task = tasks.iterator().next();
    Set<String> candidates = task.getTaskCandidates();
    assertEquals(4, candidates.size());
    assertTrue(candidates.contains(muumipeikko.getUsername()));
    assertTrue(candidates.contains(muumipappa.getUsername()));
    assertTrue(candidates.contains(muumimamma.getUsername()));
    assertTrue(candidates.contains(niisku.getUsername()));

    getManagementAPI().deleteProcess(process.getUUID());
  }

  public void testGetUsersOfASubGroups() throws Exception {
    ProcessDefinition process = ProcessBuilder.createProcess("groups", "1.2")
    .addGroup("Users")
      .addGroupResolver(GroupUsersRoleResolver.class.getName())
        .addInputParameter("setGroupPath", "/hahmo")
        .addInputParameter("setIncludingSubGroups", true)
    .addHumanTask("welcome", "Users")
    .done();

    process = getManagementAPI().deploy(getBusinessArchive(process, null, GroupUsersRoleResolver.class));
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(process.getUUID());

    Set<TaskInstance> tasks = getQueryRuntimeAPI().getTasks(instanceUUID);
    assertEquals(1, tasks.size());
    TaskInstance task = tasks.iterator().next();
    Set<String> candidates = task.getTaskCandidates();
    assertEquals(6, candidates.size());
    assertTrue(candidates.contains(muumipeikko.getUsername()));
    assertTrue(candidates.contains(muumipappa.getUsername()));
    assertTrue(candidates.contains(muumimamma.getUsername()));
    assertTrue(candidates.contains(niisku.getUsername()));
    assertTrue(candidates.contains(nipsu.getUsername()));
    assertTrue(candidates.contains(pikkuMyy.getUsername()));

    getManagementAPI().deleteProcess(process.getUUID());
  }

  public void testGetNoUsersOfASubGroups() throws Exception {
    ProcessDefinition process = ProcessBuilder.createProcess("groups", "1.2")
    .addGroup("Users")
      .addGroupResolver(GroupUsersRoleResolver.class.getName())
        .addInputParameter("setGroupPath", "/hahmo/kone")
        .addInputParameter("setIncludingSubGroups", true)
    .addHumanTask("welcome", "Users")
    .done();

    process = getManagementAPI().deploy(getBusinessArchive(process, null, GroupUsersRoleResolver.class));
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(process.getUUID());

    Set<TaskInstance> tasks = getQueryRuntimeAPI().getTasks(instanceUUID);
    assertEquals(1, tasks.size());
    TaskInstance task = tasks.iterator().next();
    Set<String> candidates = task.getTaskCandidates();
    assertEquals(0, candidates.size());

    getManagementAPI().deleteProcess(process.getUUID());
  }

}
