package org.ow2.bonita.facade;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ow2.bonita.APITestCase;
import org.ow2.bonita.env.generator.DbHistoryEnvGenerator;
import org.ow2.bonita.env.generator.EnvEntry;
import org.ow2.bonita.env.generator.EnvGenerator;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition.ProcessState;
import org.ow2.bonita.facade.exception.ProcessNotFoundException;
import org.ow2.bonita.facade.privilege.PrivilegePolicy;
import org.ow2.bonita.facade.privilege.Rule;
import org.ow2.bonita.facade.privilege.Rule.RuleType;
import org.ow2.bonita.facade.runtime.ActivityState;
import org.ow2.bonita.facade.runtime.ProcessInstance;
import org.ow2.bonita.facade.runtime.TaskInstance;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.light.LightProcessDefinition;
import org.ow2.bonita.light.LightProcessInstance;
import org.ow2.bonita.services.impl.TestApplicationAccessContext;
import org.ow2.bonita.util.BonitaException;
import org.ow2.bonita.util.ProcessBuilder;

public class ApplicationAccessTest extends APITestCase {

  static final String aTeam = "A Team";
  static final String bTeam = "B Team";
  String ruleATeamUUID;
  String ruleBTeamUUID;
  ProcessDefinition firstProcess;
  ProcessDefinition firstOneProcess;
  ProcessDefinition secondProcess;
  ProcessDefinition thirdProcess;
  ProcessDefinition fourthProcess;
  ProcessDefinition firstUserProcess;
  ProcessDefinition secondUserProcess;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    getManagementAPI().setRuleTypePolicy(RuleType.PROCESS_READ, PrivilegePolicy.DENY_BY_DEFAULT);
    Rule persistedRule = getManagementAPI().createRule(getRuleName(aTeam), null, null, RuleType.PROCESS_READ);
    ruleATeamUUID = persistedRule.getUUID();
    getManagementAPI().applyRuleToEntities(ruleATeamUUID, null, null, null, null, getEntityNameSet(aTeam));
    persistedRule = getManagementAPI().createRule(getRuleName(bTeam), null, null, RuleType.PROCESS_READ);
    ruleBTeamUUID = persistedRule.getUUID();
    getManagementAPI().applyRuleToEntities(ruleBTeamUUID, null, null, null, null, getEntityNameSet(bTeam));
  }

  protected void tearDown() throws Exception {
    setApplicationContext(null);
    try {
      getManagementAPI().deleteRuleByUUID(ruleATeamUUID);
    } catch (Exception e) {
    }

    try {
      getManagementAPI().deleteRuleByUUID(ruleBTeamUUID);
    } catch (Exception e) {
    }

    super.tearDown();

  }

  private void setApplicationContext(String name) {
    TestApplicationAccessContext.applicationName = name;
  }

  private void generateProcesses() {
    firstProcess = ProcessBuilder.createProcess("yksi", "1.0").addSystemTask("yks").done();

    firstOneProcess = ProcessBuilder.createProcess("yksi", "2.0").addSystemTask("yks").done();

    firstUserProcess = ProcessBuilder.createProcess("yksi", "3.0").addHuman(getLogin()).addHumanTask("yks", getLogin()).done();

    secondProcess = ProcessBuilder.createProcess("kaksi", "1.0").addSystemTask("kaks").done();

    secondUserProcess = ProcessBuilder.createProcess("kaksi", "2.0").addHuman(getLogin()).addHumanTask("yks", getLogin()).done();

    thirdProcess = ProcessBuilder.createProcess("kolme", "1.0").addSystemTask("kol").done();

    fourthProcess = ProcessBuilder.createProcess("neljä", "1.0").addSystemTask("nel").done();
  }

  private boolean containsProcesses(Set<ProcessDefinition> processes, ProcessDefinitionUUID... definitionUUIDs) {
    Set<ProcessDefinitionUUID> processUUIDs = new HashSet<ProcessDefinitionUUID>();
    for (ProcessDefinition process : processes) {
      processUUIDs.add(process.getUUID());
    }
    Set<ProcessDefinitionUUID> definitionsUUID = new HashSet<ProcessDefinitionUUID>();
    for (ProcessDefinitionUUID definitionUUID : definitionUUIDs) {
      definitionsUUID.add(definitionUUID);
    }
    return processUUIDs.containsAll(definitionsUUID);
  }

  private boolean containsProcesses(Set<LightProcessDefinition> processes, Set<ProcessDefinitionUUID> definitionUUIDs) {
    Set<ProcessDefinitionUUID> processUUIDs = new HashSet<ProcessDefinitionUUID>();
    for (LightProcessDefinition process : processes) {
      processUUIDs.add(process.getUUID());
    }
    return processUUIDs.containsAll(definitionUUIDs);
  }

  private boolean containsProcesses(Set<ProcessInstance> instances, ProcessInstanceUUID... isntanceUUIDs) {
    Set<ProcessInstanceUUID> instanceUUIDs = new HashSet<ProcessInstanceUUID>();
    for (ProcessInstance instance : instances) {
      instanceUUIDs.add(instance.getUUID());
    }
    Set<ProcessInstanceUUID> instancesUUID = new HashSet<ProcessInstanceUUID>();
    for (ProcessInstanceUUID instanceUUID : isntanceUUIDs) {
      instancesUUID.add(instanceUUID);
    }
    return instanceUUIDs.containsAll(instancesUUID);
  }

  private boolean containsInstances(Set<LightProcessInstance> instances, ProcessInstanceUUID... isntanceUUIDs) {
    Set<ProcessInstanceUUID> instanceUUIDs = new HashSet<ProcessInstanceUUID>();
    for (LightProcessInstance instance : instances) {
      instanceUUIDs.add(instance.getUUID());
    }
    Set<ProcessInstanceUUID> instancesUUID = new HashSet<ProcessInstanceUUID>();
    for (ProcessInstanceUUID instanceUUID : isntanceUUIDs) {
      instancesUUID.add(instanceUUID);
    }
    return instanceUUIDs.containsAll(instancesUUID);
  }

  public static EnvGenerator getEnvGenerator() {
    final EnvGenerator envGenerator = new DbHistoryEnvGenerator();
    EnvEntry entry = EnvGenerator.getEnvEntry("application-access", "Service used to control application access.", TestApplicationAccessContext.class, true);
    envGenerator.addApplicationEntry(entry);
    return envGenerator;
  }

  private void deleteAllProcesses(String... applicationNames) throws Exception {
    for (String applicationName : applicationNames) {
      setApplicationContext(applicationName);
      getManagementAPI().deleteAllProcesses();
    }
  }

  /*
   * DEFINITION
   */
  public void testGetProcesses() throws Exception {
    generateProcesses();
    firstProcess = getManagementAPI().deploy(getBusinessArchive(firstProcess));
    secondProcess = getManagementAPI().deploy(getBusinessArchive(secondProcess));
    thirdProcess = getManagementAPI().deploy(getBusinessArchive(thirdProcess));

    Set<ProcessDefinitionUUID> authorisations = getProcessUUIDSet(firstProcess, secondProcess);

    getManagementAPI().addExceptionsToRuleByUUID(ruleATeamUUID, authorisations);

    getManagementAPI().addExceptionsToRuleByUUID(ruleBTeamUUID, getProcessUUIDSet(thirdProcess));

    setApplicationContext(aTeam);
    Set<ProcessDefinition> processes = getQueryDefinitionAPI().getProcesses();
    assertEquals(2, processes.size());
    assertTrue(containsProcesses(processes, firstProcess.getUUID(), secondProcess.getUUID()));

    setApplicationContext(bTeam);
    processes = getQueryDefinitionAPI().getProcesses();
    assertEquals(1, processes.size());
    assertTrue(containsProcesses(processes, thirdProcess.getUUID()));

    deleteAllProcesses(aTeam, bTeam);
  }

  public void testRemoveAllAccessAuthorisations() throws Exception {
    generateProcesses();
    firstProcess = getManagementAPI().deploy(getBusinessArchive(firstProcess));
    secondProcess = getManagementAPI().deploy(getBusinessArchive(secondProcess));
    thirdProcess = getManagementAPI().deploy(getBusinessArchive(thirdProcess));

    Set<ProcessDefinitionUUID> authorisations = getProcessUUIDSet(firstProcess, secondProcess);

    getManagementAPI().addExceptionsToRuleByUUID(ruleATeamUUID, authorisations);

    getManagementAPI().addExceptionsToRuleByUUID(ruleBTeamUUID, getProcessUUIDSet(thirdProcess));

    setApplicationContext(aTeam);
    Set<ProcessDefinition> processes = getQueryDefinitionAPI().getProcesses();
    assertEquals(2, processes.size());
    assertTrue(containsProcesses(processes, firstProcess.getUUID(), secondProcess.getUUID()));

    getManagementAPI().deleteRuleByUUID(ruleATeamUUID);
    processes = getQueryDefinitionAPI().getProcesses();
    assertEquals(0, processes.size());
    setApplicationContext(bTeam);

    getManagementAPI().addExceptionsToRuleByUUID(ruleBTeamUUID, authorisations);
    processes = getQueryDefinitionAPI().getProcesses();
    assertEquals(3, processes.size());
    assertTrue(containsProcesses(processes, firstProcess.getUUID(), secondProcess.getUUID(), thirdProcess.getUUID()));

    deleteAllProcesses(bTeam);
  }

  public void testRemoveAccessAuthorisations() throws Exception {
    generateProcesses();
    firstProcess = getManagementAPI().deploy(getBusinessArchive(firstProcess));
    secondProcess = getManagementAPI().deploy(getBusinessArchive(secondProcess));
    thirdProcess = getManagementAPI().deploy(getBusinessArchive(thirdProcess));
    fourthProcess = getManagementAPI().deploy(getBusinessArchive(fourthProcess));

    Set<ProcessDefinitionUUID> authorisations = getProcessUUIDSet(firstProcess, secondProcess, fourthProcess);

    getManagementAPI().addExceptionsToRuleByUUID(ruleATeamUUID, authorisations);

    getManagementAPI().addExceptionsToRuleByUUID(ruleBTeamUUID, getProcessUUIDSet(thirdProcess));

    setApplicationContext(aTeam);
    Set<ProcessDefinition> processes = getQueryDefinitionAPI().getProcesses();
    assertEquals(3, processes.size());
    assertTrue(containsProcesses(processes, firstProcess.getUUID(), secondProcess.getUUID(), fourthProcess.getUUID()));

    Set<ProcessDefinitionUUID> removeAuthorisations = getProcessUUIDSet(firstProcess, fourthProcess);

    getManagementAPI().removeExceptionsFromRuleByUUID(ruleATeamUUID, removeAuthorisations);
    processes = getQueryDefinitionAPI().getProcesses();
    assertEquals(1, processes.size());
    assertTrue(containsProcesses(processes, secondProcess.getUUID()));

    setApplicationContext(bTeam);
    getManagementAPI().addExceptionsToRuleByUUID(ruleBTeamUUID, removeAuthorisations);
    processes = getQueryDefinitionAPI().getProcesses();
    assertEquals(3, processes.size());
    assertTrue(containsProcesses(processes, firstProcess.getUUID(), thirdProcess.getUUID(), fourthProcess.getUUID()));

    deleteAllProcesses(aTeam, bTeam);
  }

  public void testRemoveAccessAuthorisation() throws Exception {
    generateProcesses();
    firstProcess = getManagementAPI().deploy(getBusinessArchive(firstProcess));
    secondProcess = getManagementAPI().deploy(getBusinessArchive(secondProcess));
    thirdProcess = getManagementAPI().deploy(getBusinessArchive(thirdProcess));
    fourthProcess = getManagementAPI().deploy(getBusinessArchive(fourthProcess));

    Set<ProcessDefinitionUUID> authorisations = getProcessUUIDSet(firstProcess, secondProcess, fourthProcess);

    getManagementAPI().addExceptionsToRuleByUUID(ruleATeamUUID, authorisations);
    getManagementAPI().addExceptionsToRuleByUUID(ruleBTeamUUID, getProcessUUIDSet(thirdProcess));

    setApplicationContext(aTeam);
    Set<ProcessDefinition> processes = getQueryDefinitionAPI().getProcesses();
    assertEquals(3, processes.size());
    assertTrue(containsProcesses(processes, firstProcess.getUUID(), secondProcess.getUUID(), fourthProcess.getUUID()));

    Set<ProcessDefinitionUUID> removeAuthorisations = new HashSet<ProcessDefinitionUUID>();
    removeAuthorisations.add(secondProcess.getUUID());

    getManagementAPI().removeExceptionsFromRuleByUUID(ruleATeamUUID, removeAuthorisations);
    processes = getQueryDefinitionAPI().getProcesses();
    assertEquals(2, processes.size());
    assertTrue(containsProcesses(processes, firstProcess.getUUID(), fourthProcess.getUUID()));

    setApplicationContext(bTeam);
    getManagementAPI().addExceptionsToRuleByUUID(ruleBTeamUUID, getProcessUUIDSet(secondProcess));
    processes = getQueryDefinitionAPI().getProcesses();
    assertEquals(2, processes.size());
    assertTrue(containsProcesses(processes, secondProcess.getUUID(), thirdProcess.getUUID()));

    deleteAllProcesses(aTeam, bTeam);
  }

  public void testKnownSharedProcess() throws Exception {
    generateProcesses();
    firstProcess = getManagementAPI().deploy(getBusinessArchive(firstProcess));
    secondProcess = getManagementAPI().deploy(getBusinessArchive(secondProcess));
    thirdProcess = getManagementAPI().deploy(getBusinessArchive(thirdProcess));

    Set<ProcessDefinitionUUID> authorisations = getProcessUUIDSet(firstProcess, secondProcess);

    getManagementAPI().addExceptionsToRuleByUUID(ruleATeamUUID, authorisations);
    getManagementAPI().addExceptionsToRuleByUUID(ruleBTeamUUID, getProcessUUIDSet(secondProcess));
    getManagementAPI().addExceptionsToRuleByUUID(ruleBTeamUUID, getProcessUUIDSet(thirdProcess));

    setApplicationContext(aTeam);
    Set<ProcessDefinition> processes = getQueryDefinitionAPI().getProcesses();
    assertEquals(2, processes.size());
    assertTrue(containsProcesses(processes, firstProcess.getUUID(), secondProcess.getUUID()));
    getManagementAPI().delete(authorisations);
    processes = getQueryDefinitionAPI().getProcesses();
    assertTrue(processes.isEmpty());

    setApplicationContext(bTeam);
    processes = getQueryDefinitionAPI().getProcesses();
    assertEquals(1, processes.size());

    getManagementAPI().deleteProcess(thirdProcess.getUUID());
    processes = getQueryDefinitionAPI().getProcesses();
    assertTrue(processes.isEmpty());
  }

  public void testDeleteAllProcesses() throws Exception {
    generateProcesses();
    firstProcess = getManagementAPI().deploy(getBusinessArchive(firstProcess));
    secondProcess = getManagementAPI().deploy(getBusinessArchive(secondProcess));
    thirdProcess = getManagementAPI().deploy(getBusinessArchive(thirdProcess));
    fourthProcess = getManagementAPI().deploy(getBusinessArchive(fourthProcess));

    Set<ProcessDefinitionUUID> authorisations = getProcessUUIDSet(firstProcess, secondProcess, fourthProcess);

    getManagementAPI().addExceptionsToRuleByUUID(ruleATeamUUID, authorisations);
    getManagementAPI().addExceptionsToRuleByUUID(ruleBTeamUUID, getProcessUUIDSet(secondProcess));

    setApplicationContext(aTeam);
    Set<ProcessDefinition> processes = getQueryDefinitionAPI().getProcesses();
    assertEquals(3, processes.size());
    assertTrue(containsProcesses(processes, firstProcess.getUUID(), secondProcess.getUUID(), fourthProcess.getUUID()));
    getManagementAPI().deleteAllProcesses();
    processes = getQueryDefinitionAPI().getProcesses();
    assertTrue(processes.isEmpty());

    setApplicationContext(bTeam);
    processes = getQueryDefinitionAPI().getProcesses();
    assertEquals(0, processes.size());

    getManagementAPI().deleteAllProcesses();
    processes = getQueryDefinitionAPI().getProcesses();
    assertTrue(processes.isEmpty());

    getManagementAPI().addExceptionsToRuleByUUID(ruleBTeamUUID, getProcessUUIDSet(thirdProcess));
    getManagementAPI().deleteProcess(thirdProcess.getUUID());
  }

  public void testDeleteProcesseses() throws Exception {
    generateProcesses();
    firstProcess = getManagementAPI().deploy(getBusinessArchive(firstProcess));
    secondProcess = getManagementAPI().deploy(getBusinessArchive(secondProcess));
    thirdProcess = getManagementAPI().deploy(getBusinessArchive(thirdProcess));
    fourthProcess = getManagementAPI().deploy(getBusinessArchive(fourthProcess));

    Set<ProcessDefinitionUUID> authorisations = getProcessUUIDSet(firstProcess, secondProcess, fourthProcess);

    getManagementAPI().addExceptionsToRuleByUUID(ruleATeamUUID, authorisations);

    setApplicationContext(bTeam);
    Set<ProcessDefinition> processes = getQueryDefinitionAPI().getProcesses();
    assertTrue(processes.isEmpty());

    setApplicationContext(aTeam);
    processes = getQueryDefinitionAPI().getProcesses();
    assertEquals(3, processes.size());
    getManagementAPI().deleteAllProcesses();
    processes = getQueryDefinitionAPI().getProcesses();
    assertTrue(processes.isEmpty());

    getManagementAPI().addExceptionsToRuleByUUID(ruleATeamUUID, getProcessUUIDSet(thirdProcess));
    getManagementAPI().deleteProcess(thirdProcess.getUUID());
  }

  public void testGetNumberOfProcesses() throws Exception {
    generateProcesses();
    firstProcess = getManagementAPI().deploy(getBusinessArchive(firstProcess));
    secondProcess = getManagementAPI().deploy(getBusinessArchive(secondProcess));
    thirdProcess = getManagementAPI().deploy(getBusinessArchive(thirdProcess));

    Set<ProcessDefinitionUUID> authorisations = getProcessUUIDSet(firstProcess, secondProcess);

    getManagementAPI().addExceptionsToRuleByUUID(ruleATeamUUID, authorisations);
    getManagementAPI().addExceptionsToRuleByUUID(ruleBTeamUUID, getProcessUUIDSet(thirdProcess));

    setApplicationContext(aTeam);
    assertEquals(2, getQueryDefinitionAPI().getNumberOfProcesses());

    setApplicationContext(bTeam);
    assertEquals(1, getQueryDefinitionAPI().getNumberOfProcesses());

    deleteAllProcesses(aTeam, bTeam);
  }

  public void testNoProcess() throws Exception {
    generateProcesses();
    firstProcess = getManagementAPI().deploy(getBusinessArchive(firstProcess));
    secondProcess = getManagementAPI().deploy(getBusinessArchive(secondProcess));
    thirdProcess = getManagementAPI().deploy(getBusinessArchive(thirdProcess));

    getManagementAPI().addExceptionsToRuleByUUID(ruleBTeamUUID, getProcessUUIDSet(thirdProcess));

    setApplicationContext(aTeam);
    assertEquals(0, getQueryDefinitionAPI().getNumberOfProcesses());
    setApplicationContext(bTeam);
    assertEquals(1, getQueryDefinitionAPI().getNumberOfProcesses());

    getManagementAPI().addExceptionsToRuleByUUID(ruleBTeamUUID, getProcessUUIDSet(firstProcess));
    getManagementAPI().addExceptionsToRuleByUUID(ruleBTeamUUID, getProcessUUIDSet(secondProcess));
    assertEquals(3, getQueryDefinitionAPI().getNumberOfProcesses());

    deleteAllProcesses(bTeam);
  }

  public void testTwoApplicationsShareTheSameProcess() throws Exception {
    generateProcesses();
    firstProcess = getManagementAPI().deploy(getBusinessArchive(firstProcess));
    secondProcess = getManagementAPI().deploy(getBusinessArchive(secondProcess));
    getManagementAPI().addExceptionsToRuleByUUID(ruleATeamUUID, getProcessUUIDSet(firstProcess));
    getManagementAPI().addExceptionsToRuleByUUID(ruleATeamUUID, getProcessUUIDSet(secondProcess));
    getManagementAPI().addExceptionsToRuleByUUID(ruleBTeamUUID, getProcessUUIDSet(firstProcess));

    setApplicationContext(aTeam);
    assertEquals(2, getQueryDefinitionAPI().getNumberOfProcesses());

    setApplicationContext(bTeam);
    assertEquals(1, getQueryDefinitionAPI().getNumberOfProcesses());

    setApplicationContext(null);
    assertEquals(0, getQueryDefinitionAPI().getNumberOfProcesses());

    setApplicationContext("a");
    assertEquals(0, getQueryDefinitionAPI().getNumberOfProcesses());

    setApplicationContext(bTeam);
    getManagementAPI().deleteAllProcesses();

    setApplicationContext(aTeam);
    assertEquals(1, getQueryDefinitionAPI().getNumberOfProcesses());

    setApplicationContext(bTeam);
    assertEquals(0, getQueryDefinitionAPI().getNumberOfProcesses());

    deleteAllProcesses(aTeam);
  }

  public void testGetProcess() throws Exception {
    generateProcesses();
    firstProcess = getManagementAPI().deploy(getBusinessArchive(firstProcess));
    firstOneProcess = getManagementAPI().deploy(getBusinessArchive(firstOneProcess));
    thirdProcess = getManagementAPI().deploy(getBusinessArchive(thirdProcess));

    Set<ProcessDefinitionUUID> authorisations = getProcessUUIDSet(firstProcess, thirdProcess);

    getManagementAPI().addExceptionsToRuleByUUID(ruleATeamUUID, authorisations);
    getManagementAPI().addExceptionsToRuleByUUID(ruleBTeamUUID, getProcessUUIDSet(firstOneProcess));

    setApplicationContext(aTeam);
    ProcessDefinition process = getQueryDefinitionAPI().getProcess("yksi", "1.0");
    assertEquals(firstProcess.getUUID(), process.getUUID());

    setApplicationContext(bTeam);
    try {
      getQueryDefinitionAPI().getProcess("yksi", "1.0");
      fail("Process yksi was not granted to the B Team");
    } catch (ProcessNotFoundException e) {
    } finally {
      deleteAllProcesses(aTeam, bTeam);
    }
  }

  public void testGetProcessesId() throws Exception {
    generateProcesses();
    firstProcess = getManagementAPI().deploy(getBusinessArchive(firstProcess));
    firstOneProcess = getManagementAPI().deploy(getBusinessArchive(firstOneProcess));

    Set<ProcessDefinitionUUID> authorisations = getProcessUUIDSet(firstProcess, firstOneProcess);

    getManagementAPI().addExceptionsToRuleByUUID(ruleATeamUUID, authorisations);
    getManagementAPI().addExceptionsToRuleByUUID(ruleBTeamUUID, getProcessUUIDSet(firstOneProcess));

    setApplicationContext(aTeam);
    Set<ProcessDefinition> processes = getQueryDefinitionAPI().getProcesses("yksi");
    assertEquals(2, processes.size());
    assertTrue(containsProcesses(processes, firstProcess.getUUID(), firstOneProcess.getUUID()));

    setApplicationContext(bTeam);
    processes = getQueryDefinitionAPI().getProcesses("yksi");
    assertEquals(1, processes.size());
    assertTrue(containsProcesses(processes, firstOneProcess.getUUID()));

    deleteAllProcesses(aTeam, bTeam);
  }

  public void testCannotGetProcessesId() throws Exception {
    generateProcesses();
    firstOneProcess = getManagementAPI().deploy(getBusinessArchive(firstOneProcess));

    getManagementAPI().addExceptionsToRuleByUUID(ruleBTeamUUID, getProcessUUIDSet(firstOneProcess));

    setApplicationContext(bTeam);
    Set<ProcessDefinition> processes = getQueryDefinitionAPI().getProcesses("kakskytkaks");
    assertTrue(processes.isEmpty());

    deleteAllProcesses(bTeam);
  }

  public void testGetLastProcess() throws Exception {
    generateProcesses();
    firstProcess = getManagementAPI().deploy(getBusinessArchive(firstProcess));
    firstOneProcess = getManagementAPI().deploy(getBusinessArchive(firstOneProcess));

    Set<ProcessDefinitionUUID> authorisations = getProcessUUIDSet(firstProcess, firstOneProcess);

    getManagementAPI().addExceptionsToRuleByUUID(ruleATeamUUID, authorisations);
    getManagementAPI().addExceptionsToRuleByUUID(ruleBTeamUUID, getProcessUUIDSet(firstProcess));

    setApplicationContext(aTeam);
    ProcessDefinition process = getQueryDefinitionAPI().getLastProcess("yksi");
    assertEquals(firstOneProcess.getUUID(), process.getUUID());

    setApplicationContext(bTeam);
    process = getQueryDefinitionAPI().getLastProcess("yksi");
    assertEquals(firstProcess.getUUID(), process.getUUID());

    try {
      setApplicationContext(null);
      getQueryDefinitionAPI().getLastProcess("yksi");
    } catch (ProcessNotFoundException e) {
    } finally {
      deleteAllProcesses(aTeam, bTeam);
    }
  }

  public void testCannotGetLastProcess() throws Exception {
    generateProcesses();
    secondProcess = getManagementAPI().deploy(getBusinessArchive(secondProcess));
    firstOneProcess = getManagementAPI().deploy(getBusinessArchive(firstOneProcess));

    getManagementAPI().addExceptionsToRuleByUUID(ruleBTeamUUID, getProcessUUIDSet(firstOneProcess));

    setApplicationContext(bTeam);
    try {
      getQueryDefinitionAPI().getLastProcess("kaksi");
      fail("Process kaksi was not granted to B Team");
    } catch (ProcessNotFoundException e) {
    }

    setApplicationContext(bTeam);
    getManagementAPI().addExceptionsToRuleByUUID(ruleBTeamUUID, getProcessUUIDSet(secondProcess));
    ProcessDefinition process = getQueryDefinitionAPI().getLastProcess("kaksi");
    assertEquals(secondProcess.getUUID(), process.getUUID());

    deleteAllProcesses(bTeam);
  }

  public void testGetEnableProcessesId() throws Exception {
    generateProcesses();
    firstProcess = getManagementAPI().deploy(getBusinessArchive(firstProcess));
    firstOneProcess = getManagementAPI().deploy(getBusinessArchive(firstOneProcess));

    Set<ProcessDefinitionUUID> authorisations = getProcessUUIDSet(firstProcess, firstOneProcess);

    getManagementAPI().addExceptionsToRuleByUUID(ruleATeamUUID, authorisations);
    getManagementAPI().addExceptionsToRuleByUUID(ruleBTeamUUID, getProcessUUIDSet(firstOneProcess));

    setApplicationContext(aTeam);
    Set<ProcessDefinition> processes = getQueryDefinitionAPI().getProcesses("yksi", ProcessState.ENABLED);
    assertEquals(2, processes.size());
    assertTrue(containsProcesses(processes, firstProcess.getUUID(), firstOneProcess.getUUID()));

    setApplicationContext(bTeam);
    processes = getQueryDefinitionAPI().getProcesses("yksi", ProcessState.ENABLED);
    assertEquals(1, processes.size());
    assertTrue(containsProcesses(processes, firstOneProcess.getUUID()));

    deleteAllProcesses(aTeam, bTeam);
  }

  public void testCannotGetEnableProcessesId() throws Exception {
    generateProcesses();
    firstOneProcess = getManagementAPI().deploy(getBusinessArchive(firstOneProcess));

    getManagementAPI().addExceptionsToRuleByUUID(ruleBTeamUUID, getProcessUUIDSet(firstOneProcess));

    setApplicationContext(bTeam);
    Set<ProcessDefinition> processes = getQueryDefinitionAPI().getProcesses("kakskytkaks", ProcessState.ENABLED);
    assertTrue(processes.isEmpty());

    deleteAllProcesses(aTeam, bTeam);
  }

  public void testGetDisableProcessesId() throws Exception {
    generateProcesses();
    firstProcess = getManagementAPI().deploy(getBusinessArchive(firstProcess));
    firstOneProcess = getManagementAPI().deploy(getBusinessArchive(firstOneProcess));

    getManagementAPI().disable(firstProcess.getUUID());

    Set<ProcessDefinitionUUID> authorisations = getProcessUUIDSet(firstProcess, firstOneProcess);

    getManagementAPI().addExceptionsToRuleByUUID(ruleATeamUUID, authorisations);
    getManagementAPI().addExceptionsToRuleByUUID(ruleBTeamUUID, getProcessUUIDSet(firstOneProcess));

    setApplicationContext(aTeam);
    Set<ProcessDefinition> processes = getQueryDefinitionAPI().getProcesses("yksi", ProcessState.DISABLED);
    assertEquals(1, processes.size());
    assertTrue(containsProcesses(processes, firstProcess.getUUID()));
    processes = getQueryDefinitionAPI().getProcesses(ProcessState.DISABLED);
    assertEquals(1, processes.size());
    assertTrue(containsProcesses(processes, firstProcess.getUUID()));

    deleteAllProcesses(aTeam, bTeam);
  }

  public void testCannotGetDisableProcessesId() throws Exception {
    generateProcesses();
    firstOneProcess = getManagementAPI().deploy(getBusinessArchive(firstOneProcess));

    getManagementAPI().addExceptionsToRuleByUUID(ruleBTeamUUID, getProcessUUIDSet(firstOneProcess));

    setApplicationContext(bTeam);
    Set<ProcessDefinition> processes = getQueryDefinitionAPI().getProcesses("kakskytkaks", ProcessState.DISABLED);
    assertTrue(processes.isEmpty());

    deleteAllProcesses(bTeam);
  }

  public void testGetArchivedProcessesId() throws Exception {
    generateProcesses();
    firstProcess = getManagementAPI().deploy(getBusinessArchive(firstProcess));
    firstOneProcess = getManagementAPI().deploy(getBusinessArchive(firstOneProcess));

    getManagementAPI().disable(firstProcess.getUUID());
    getManagementAPI().archive(firstProcess.getUUID());

    Set<ProcessDefinitionUUID> authorisations = getProcessUUIDSet(firstProcess, firstOneProcess);

    getManagementAPI().addExceptionsToRuleByUUID(ruleATeamUUID, authorisations);

    setApplicationContext(aTeam);
    Set<ProcessDefinition> processes = getQueryDefinitionAPI().getProcesses("yksi", ProcessState.ARCHIVED);
    assertEquals(1, processes.size());
    assertTrue(containsProcesses(processes, firstProcess.getUUID()));

    deleteAllProcesses(aTeam);
  }

  public void testCannotGetArchivedProcessesId() throws Exception {
    generateProcesses();
    firstOneProcess = getManagementAPI().deploy(getBusinessArchive(firstOneProcess));

    getManagementAPI().addExceptionsToRuleByUUID(ruleBTeamUUID, getProcessUUIDSet(firstOneProcess));

    setApplicationContext(bTeam);
    Set<ProcessDefinition> processes = getQueryDefinitionAPI().getProcesses("kakskytkaks", ProcessState.ARCHIVED);
    assertTrue(processes.isEmpty());
    processes = getQueryDefinitionAPI().getProcesses(ProcessState.ARCHIVED);
    assertTrue(processes.isEmpty());

    deleteAllProcesses(bTeam);
  }

  public void testGetEnableLightProcesses() throws Exception {
    generateProcesses();
    firstProcess = getManagementAPI().deploy(getBusinessArchive(firstProcess));
    firstOneProcess = getManagementAPI().deploy(getBusinessArchive(firstOneProcess));

    Set<ProcessDefinitionUUID> authorisations = new HashSet<ProcessDefinitionUUID>();
    authorisations.add(firstProcess.getUUID());
    authorisations.add(firstOneProcess.getUUID());

    getManagementAPI().addExceptionsToRuleByUUID(ruleATeamUUID, authorisations);
    getManagementAPI().addExceptionsToRuleByUUID(ruleBTeamUUID, getProcessUUIDSet(firstOneProcess));

    setApplicationContext(aTeam);
    Set<LightProcessDefinition> processes = getQueryDefinitionAPI().getLightProcesses(ProcessState.ENABLED);
    assertEquals(2, processes.size());

    setApplicationContext(bTeam);
    processes = getQueryDefinitionAPI().getLightProcesses(ProcessState.ENABLED);
    assertEquals(1, processes.size());
    assertEquals(firstOneProcess.getUUID(), processes.iterator().next().getUUID());

    deleteAllProcesses(aTeam, bTeam);
  }

  public void testCannotGetEnableLightProcesses() throws Exception {
    generateProcesses();
    firstOneProcess = getManagementAPI().deploy(getBusinessArchive(firstOneProcess));
    getManagementAPI().addExceptionsToRuleByUUID(ruleBTeamUUID, getProcessUUIDSet(firstOneProcess));
    getManagementAPI().disable(firstOneProcess.getUUID());

    setApplicationContext(bTeam);
    Set<LightProcessDefinition> processes = getQueryDefinitionAPI().getLightProcesses(ProcessState.ENABLED);
    assertTrue(processes.isEmpty());

    deleteAllProcesses(bTeam);
  }

  public void testGetDisableLightProcesses() throws Exception {
    generateProcesses();
    firstProcess = getManagementAPI().deploy(getBusinessArchive(firstProcess));
    firstOneProcess = getManagementAPI().deploy(getBusinessArchive(firstOneProcess));

    getManagementAPI().disable(firstProcess.getUUID());

    Set<ProcessDefinitionUUID> authorisations = new HashSet<ProcessDefinitionUUID>();
    authorisations.add(firstProcess.getUUID());
    authorisations.add(firstOneProcess.getUUID());

    getManagementAPI().addExceptionsToRuleByUUID(ruleATeamUUID, authorisations);
    getManagementAPI().addExceptionsToRuleByUUID(ruleBTeamUUID, getProcessUUIDSet(firstOneProcess));

    setApplicationContext(aTeam);
    Set<LightProcessDefinition> processes = getQueryDefinitionAPI().getLightProcesses(ProcessState.DISABLED);
    assertEquals(1, processes.size());
    assertEquals(firstProcess.getUUID(), processes.iterator().next().getUUID());

    deleteAllProcesses(aTeam, bTeam);
  }

  public void testCannotGetDisableLightProcesses() throws Exception {
    generateProcesses();
    firstOneProcess = getManagementAPI().deploy(getBusinessArchive(firstOneProcess));

    getManagementAPI().addExceptionsToRuleByUUID(ruleBTeamUUID, getProcessUUIDSet(firstOneProcess));

    setApplicationContext(bTeam);
    Set<LightProcessDefinition> processes = getQueryDefinitionAPI().getLightProcesses(ProcessState.DISABLED);
    assertTrue(processes.isEmpty());

    deleteAllProcesses(bTeam);
  }

  public void testGetArchivedLightProcesses() throws Exception {
    generateProcesses();
    firstProcess = getManagementAPI().deploy(getBusinessArchive(firstProcess));
    firstOneProcess = getManagementAPI().deploy(getBusinessArchive(firstOneProcess));

    getManagementAPI().disable(firstProcess.getUUID());
    getManagementAPI().archive(firstProcess.getUUID());

    Set<ProcessDefinitionUUID> authorisations = new HashSet<ProcessDefinitionUUID>();
    authorisations.add(firstProcess.getUUID());
    authorisations.add(firstOneProcess.getUUID());

    getManagementAPI().addExceptionsToRuleByUUID(ruleATeamUUID, authorisations);
    getManagementAPI().addExceptionsToRuleByUUID(ruleBTeamUUID, getProcessUUIDSet(firstOneProcess));

    setApplicationContext(aTeam);
    Set<LightProcessDefinition> processes = getQueryDefinitionAPI().getLightProcesses(ProcessState.ARCHIVED);
    assertEquals(1, processes.size());
    assertEquals(firstProcess.getUUID(), processes.iterator().next().getUUID());

    deleteAllProcesses(aTeam, bTeam);
  }

  public void testCannotGetArchivedLightProcesses() throws Exception {
    generateProcesses();
    firstOneProcess = getManagementAPI().deploy(getBusinessArchive(firstOneProcess));

    getManagementAPI().addExceptionsToRuleByUUID(ruleBTeamUUID, getProcessUUIDSet(firstOneProcess));

    setApplicationContext(bTeam);
    Set<LightProcessDefinition> processes = getQueryDefinitionAPI().getLightProcesses(ProcessState.ARCHIVED);
    assertTrue(processes.isEmpty());

    deleteAllProcesses(bTeam);
  }

  public void testGetLightProcesses() throws Exception {
    generateProcesses();
    firstProcess = getManagementAPI().deploy(getBusinessArchive(firstProcess));
    secondProcess = getManagementAPI().deploy(getBusinessArchive(secondProcess));
    thirdProcess = getManagementAPI().deploy(getBusinessArchive(thirdProcess));

    Set<ProcessDefinitionUUID> authorisations = new HashSet<ProcessDefinitionUUID>();
    authorisations.add(firstProcess.getUUID());
    authorisations.add(secondProcess.getUUID());

    getManagementAPI().addExceptionsToRuleByUUID(ruleATeamUUID, authorisations);
    getManagementAPI().addExceptionsToRuleByUUID(ruleBTeamUUID, getProcessUUIDSet(thirdProcess));

    setApplicationContext(aTeam);
    Set<LightProcessDefinition> processes = getQueryDefinitionAPI().getLightProcesses();
    assertEquals(2, processes.size());
    assertTrue(containsProcesses(processes, authorisations));

    setApplicationContext(bTeam);
    processes = getQueryDefinitionAPI().getLightProcesses();
    assertEquals(1, processes.size());
    assertEquals(thirdProcess.getUUID(), processes.iterator().next().getUUID());

    getManagementAPI().addExceptionsToRuleByUUID(ruleBTeamUUID, authorisations);
    processes = getQueryDefinitionAPI().getLightProcesses();
    assertEquals(3, processes.size());
    authorisations.add(thirdProcess.getUUID());
    assertTrue(containsProcesses(processes, authorisations));
    deleteAllProcesses(aTeam, bTeam);
  }

  public void testGetLightProcessPagesNumber() throws Exception {
    final int size = 33;
    for (int i = 0; i < size; i++) {
      ProcessDefinition p = ProcessBuilder.createProcess("p" + i, "1.0").addSystemTask("s").done();
      p = getManagementAPI().deploy(getBusinessArchive(p));
      if (i % 2 == 0) {
        getManagementAPI().addExceptionsToRuleByUUID(ruleATeamUUID, getProcessUUIDSet(p));
      } else {
        getManagementAPI().addExceptionsToRuleByUUID(ruleBTeamUUID, getProcessUUIDSet(p));
      }
    }

    setApplicationContext(aTeam);
    assertEquals(8, getQueryDefinitionAPI().getLightProcesses(0, 8).size());
    assertEquals(17, getQueryDefinitionAPI().getLightProcesses(0, 20).size());

    setApplicationContext(bTeam);
    assertEquals(8, getQueryDefinitionAPI().getLightProcesses(0, 8).size());
    assertEquals(16, getQueryDefinitionAPI().getLightProcesses(0, 20).size());

    deleteAllProcesses(aTeam, bTeam);
  }

  public void testGetProcessPagesNumber() throws Exception {
    int size = 33;
    for (int i = 0; i < size; i++) {
      ProcessDefinition p = ProcessBuilder.createProcess("p" + i, "1.0").addSystemTask("s").done();
      p = getManagementAPI().deploy(getBusinessArchive(p));
      if (i % 2 == 0) {
        getManagementAPI().addExceptionsToRuleByUUID(ruleATeamUUID, getProcessUUIDSet(p));
      } else {
        getManagementAPI().addExceptionsToRuleByUUID(ruleBTeamUUID, getProcessUUIDSet(p));
      }
    }

    setApplicationContext(aTeam);
    assertEquals(8, getQueryDefinitionAPI().getProcesses(0, 8).size());
    assertEquals(17, getQueryDefinitionAPI().getProcesses(0, 20).size());

    setApplicationContext(bTeam);
    assertEquals(8, getQueryDefinitionAPI().getProcesses(0, 8).size());
    assertEquals(16, getQueryDefinitionAPI().getProcesses(0, 20).size());

    deleteAllProcesses(aTeam, bTeam);
  }

  public void testGetProcessInstancesSameUser() throws Exception {
    generateProcesses();
    firstUserProcess = getManagementAPI().deploy(getBusinessArchive(firstUserProcess));
    secondUserProcess = getManagementAPI().deploy(getBusinessArchive(secondUserProcess));

    ProcessInstanceUUID firstInstance = getRuntimeAPI().instantiateProcess(firstUserProcess.getUUID());
    ProcessInstanceUUID secondInstance = getRuntimeAPI().instantiateProcess(firstUserProcess.getUUID());
    ProcessInstanceUUID thirdInstance = getRuntimeAPI().instantiateProcess(firstUserProcess.getUUID());
    ProcessInstanceUUID fourthInstance = getRuntimeAPI().instantiateProcess(firstUserProcess.getUUID());
    ProcessInstanceUUID fifthInstance = getRuntimeAPI().instantiateProcess(secondUserProcess.getUUID());

    getManagementAPI().addExceptionsToRuleByUUID(ruleATeamUUID, getProcessUUIDSet(firstUserProcess));

    setApplicationContext(aTeam);
    Set<ProcessInstance> instances = getQueryRuntimeAPI().getProcessInstances();
    assertEquals(4, instances.size());
    assertTrue(containsProcesses(instances, firstInstance, secondInstance, thirdInstance, fourthInstance));

    getManagementAPI().addExceptionsToRuleByUUID(ruleATeamUUID, getProcessUUIDSet(secondUserProcess));
    instances = getQueryRuntimeAPI().getProcessInstances();
    assertEquals(5, instances.size());
    assertTrue(containsProcesses(instances, firstInstance, secondInstance, thirdInstance, fourthInstance, fifthInstance));
    deleteAllProcesses(aTeam);
  }

  public void testGetLightProcessInstancesSameUser() throws Exception {
    generateProcesses();
    firstUserProcess = getManagementAPI().deploy(getBusinessArchive(firstUserProcess));
    secondUserProcess = getManagementAPI().deploy(getBusinessArchive(secondUserProcess));

    ProcessInstanceUUID firstInstance = getRuntimeAPI().instantiateProcess(firstUserProcess.getUUID());
    ProcessInstanceUUID secondInstance = getRuntimeAPI().instantiateProcess(firstUserProcess.getUUID());
    ProcessInstanceUUID thirdInstance = getRuntimeAPI().instantiateProcess(firstUserProcess.getUUID());
    ProcessInstanceUUID fourthInstance = getRuntimeAPI().instantiateProcess(firstUserProcess.getUUID());
    ProcessInstanceUUID fifthInstance = getRuntimeAPI().instantiateProcess(secondUserProcess.getUUID());

    getManagementAPI().addExceptionsToRuleByUUID(ruleATeamUUID, getProcessUUIDSet(firstUserProcess));

    setApplicationContext(aTeam);
    Set<LightProcessInstance> instances = getQueryRuntimeAPI().getLightProcessInstances();
    assertEquals(4, instances.size());
    assertTrue(containsInstances(instances, firstInstance, secondInstance, thirdInstance, fourthInstance));

    getManagementAPI().addExceptionsToRuleByUUID(ruleATeamUUID, getProcessUUIDSet(secondUserProcess));
    instances = getQueryRuntimeAPI().getLightProcessInstances();
    assertEquals(5, instances.size());
    assertTrue(containsInstances(instances, firstInstance, secondInstance, thirdInstance, fourthInstance, fifthInstance));

    deleteAllProcesses(aTeam);
  }

  public void testGetUserProcessInstancesSameUser() throws Exception {
    generateProcesses();
    firstUserProcess = getManagementAPI().deploy(getBusinessArchive(firstUserProcess));
    secondUserProcess = getManagementAPI().deploy(getBusinessArchive(secondUserProcess));

    ProcessInstanceUUID firstInstance = getRuntimeAPI().instantiateProcess(firstUserProcess.getUUID());
    ProcessInstanceUUID secondInstance = getRuntimeAPI().instantiateProcess(firstUserProcess.getUUID());
    ProcessInstanceUUID thirdInstance = getRuntimeAPI().instantiateProcess(firstUserProcess.getUUID());
    ProcessInstanceUUID fourthInstance = getRuntimeAPI().instantiateProcess(firstUserProcess.getUUID());
    ProcessInstanceUUID fifthInstance = getRuntimeAPI().instantiateProcess(secondUserProcess.getUUID());

    getManagementAPI().addExceptionsToRuleByUUID(ruleATeamUUID, getProcessUUIDSet(firstUserProcess));

    setApplicationContext(aTeam);
    Set<ProcessInstance> instances = getQueryRuntimeAPI().getUserInstances();
    assertEquals(4, instances.size());
    assertTrue(containsProcesses(instances, firstInstance, secondInstance, thirdInstance, fourthInstance));

    getManagementAPI().addExceptionsToRuleByUUID(ruleATeamUUID, getProcessUUIDSet(secondUserProcess));
    instances = getQueryRuntimeAPI().getUserInstances();
    assertEquals(5, instances.size());
    assertTrue(containsProcesses(instances, firstInstance, secondInstance, thirdInstance, fourthInstance, fifthInstance));

    deleteAllProcesses(aTeam);
  }

  public void testGetLightUserProcessInstancesSameUser() throws Exception {
    generateProcesses();
    firstUserProcess = getManagementAPI().deploy(getBusinessArchive(firstUserProcess));
    secondUserProcess = getManagementAPI().deploy(getBusinessArchive(secondUserProcess));

    ProcessInstanceUUID firstInstance = getRuntimeAPI().instantiateProcess(firstUserProcess.getUUID());
    ProcessInstanceUUID secondInstance = getRuntimeAPI().instantiateProcess(firstUserProcess.getUUID());
    ProcessInstanceUUID thirdInstance = getRuntimeAPI().instantiateProcess(firstUserProcess.getUUID());
    ProcessInstanceUUID fourthInstance = getRuntimeAPI().instantiateProcess(firstUserProcess.getUUID());
    ProcessInstanceUUID fifthInstance = getRuntimeAPI().instantiateProcess(secondUserProcess.getUUID());

    getManagementAPI().addExceptionsToRuleByUUID(ruleATeamUUID, getProcessUUIDSet(firstUserProcess));

    setApplicationContext(aTeam);
    Set<LightProcessInstance> instances = getQueryRuntimeAPI().getLightUserInstances();
    assertEquals(4, instances.size());
    assertTrue(containsInstances(instances, firstInstance, secondInstance, thirdInstance, fourthInstance));

    getManagementAPI().addExceptionsToRuleByUUID(ruleATeamUUID, getProcessUUIDSet(secondUserProcess));
    instances = getQueryRuntimeAPI().getLightUserInstances();
    assertEquals(5, instances.size());
    assertTrue(containsInstances(instances, firstInstance, secondInstance, thirdInstance, fourthInstance, fifthInstance));

    deleteAllProcesses(aTeam, bTeam);
  }

  public void testGetNumberOfProcessInstancesSameUser() throws Exception {
    generateProcesses();
    firstUserProcess = getManagementAPI().deploy(getBusinessArchive(firstUserProcess));
    secondUserProcess = getManagementAPI().deploy(getBusinessArchive(secondUserProcess));

    getRuntimeAPI().instantiateProcess(firstUserProcess.getUUID());
    getRuntimeAPI().instantiateProcess(firstUserProcess.getUUID());
    getRuntimeAPI().instantiateProcess(firstUserProcess.getUUID());
    getRuntimeAPI().instantiateProcess(firstUserProcess.getUUID());
    getRuntimeAPI().instantiateProcess(secondUserProcess.getUUID());

    getManagementAPI().addExceptionsToRuleByUUID(ruleATeamUUID, getProcessUUIDSet(firstUserProcess));

    setApplicationContext(aTeam);
    assertEquals(4, getQueryRuntimeAPI().getNumberOfProcessInstances());

    getManagementAPI().addExceptionsToRuleByUUID(ruleATeamUUID, getProcessUUIDSet(secondUserProcess));
    assertEquals(5, getQueryRuntimeAPI().getNumberOfProcessInstances());

    deleteAllProcesses(aTeam, bTeam);
  }

  public void testGetNumberOf() throws Exception {
    generateProcesses();
    firstUserProcess = getManagementAPI().deploy(getBusinessArchive(firstUserProcess));

    ProcessDefinition process = ProcessBuilder.createProcess("pProcess", "1.0").addSubProcess("aSubProcess", "aSubProcess").done();

    ProcessDefinition subProcess = ProcessBuilder.createProcess("aSubProcess", "1.0").addHuman(getLogin()).addHumanTask("h", getLogin()).done();

    subProcess = getManagementAPI().deploy(getBusinessArchive(subProcess));
    process = getManagementAPI().deploy(getBusinessArchive(process));

    getManagementAPI().addExceptionsToRuleByUUID(ruleATeamUUID, getProcessUUIDSet(subProcess));
    Set<ProcessDefinitionUUID> definitionsUUIDs = new HashSet<ProcessDefinitionUUID>();
    definitionsUUIDs.add(process.getUUID());
    definitionsUUIDs.add(subProcess.getUUID());
    getManagementAPI().addExceptionsToRuleByUUID(ruleBTeamUUID, definitionsUUIDs);
    ProcessDefinitionUUID parentProcessUUID = process.getUUID();
    getRuntimeAPI().instantiateProcess(parentProcessUUID);

    setApplicationContext(aTeam);
    assertEquals(1, getQueryDefinitionAPI().getNumberOfProcesses());
    assertEquals(1, getQueryRuntimeAPI().getNumberOfProcessInstances());

    setApplicationContext(bTeam);
    assertEquals(2, getQueryDefinitionAPI().getNumberOfProcesses());
    assertEquals(2, getQueryRuntimeAPI().getNumberOfProcessInstances());

    getRuntimeAPI().instantiateProcess(parentProcessUUID);

    setApplicationContext(aTeam);
    assertEquals(1, getQueryDefinitionAPI().getNumberOfProcesses());
    assertEquals(2, getQueryRuntimeAPI().getNumberOfProcessInstances());
    assertEquals(0, getQueryRuntimeAPI().getNumberOfParentProcessInstances());
    assertEquals(0, getQueryRuntimeAPI().getNumberOfParentProcessInstancesExcept(new HashSet<ProcessDefinitionUUID>()));
    assertEquals(0, getQueryRuntimeAPI().getNumberOfParentProcessInstances(new HashSet<ProcessDefinitionUUID>()));

    setApplicationContext(bTeam);
    assertEquals(2, getQueryDefinitionAPI().getNumberOfProcesses());
    assertEquals(4, getQueryRuntimeAPI().getNumberOfProcessInstances());
    assertEquals(2, getQueryRuntimeAPI().getNumberOfParentProcessInstances());
    Set<ProcessDefinitionUUID> processUUIDs = new HashSet<ProcessDefinitionUUID>();
    assertEquals(0, getQueryRuntimeAPI().getNumberOfParentProcessInstances(processUUIDs));
    assertEquals(2, getQueryRuntimeAPI().getNumberOfParentProcessInstancesExcept(processUUIDs));
    processUUIDs.add(process.getUUID());
    assertEquals(2, getQueryRuntimeAPI().getNumberOfParentProcessInstances(processUUIDs));
    assertEquals(0, getQueryRuntimeAPI().getNumberOfParentProcessInstancesExcept(processUUIDs));

    getRuntimeAPI().deleteAllProcessInstances(process.getUUID());

    getManagementAPI().addExceptionsToRuleByUUID(ruleATeamUUID, getProcessUUIDSet(firstUserProcess));
    deleteAllProcesses(aTeam, bTeam);
  }

  public void testGetProcessInstancesWithTaskState() throws Exception {
    generateProcesses();
    List<ActivityState> activityStates = new ArrayList<ActivityState>();
    activityStates.add(ActivityState.READY);
    activityStates.add(ActivityState.EXECUTING);

    firstUserProcess = getManagementAPI().deploy(getBusinessArchive(firstUserProcess));
    secondUserProcess = getManagementAPI().deploy(getBusinessArchive(secondUserProcess));

    ProcessInstanceUUID firstInstance = getRuntimeAPI().instantiateProcess(firstUserProcess.getUUID());
    ProcessInstanceUUID secondInstance = getRuntimeAPI().instantiateProcess(firstUserProcess.getUUID());
    ProcessInstanceUUID thirdInstance = getRuntimeAPI().instantiateProcess(firstUserProcess.getUUID());
    ProcessInstanceUUID fourthInstance = getRuntimeAPI().instantiateProcess(firstUserProcess.getUUID());
    ProcessInstanceUUID fifthInstance = getRuntimeAPI().instantiateProcess(secondUserProcess.getUUID());

    getManagementAPI().addExceptionsToRuleByUUID(ruleATeamUUID, getProcessUUIDSet(firstUserProcess));

    setApplicationContext(aTeam);
    Set<ProcessInstance> instances = getQueryRuntimeAPI().getProcessInstancesWithTaskState(activityStates);
    assertEquals(4, instances.size());
    assertTrue(containsProcesses(instances, firstInstance, secondInstance, thirdInstance, fourthInstance));

    getManagementAPI().addExceptionsToRuleByUUID(ruleATeamUUID, getProcessUUIDSet(secondUserProcess));
    instances = getQueryRuntimeAPI().getProcessInstancesWithTaskState(activityStates);
    assertEquals(5, instances.size());
    assertTrue(containsProcesses(instances, firstInstance, secondInstance, thirdInstance, fourthInstance, fifthInstance));

    deleteAllProcesses(aTeam);
  }

  public void testGetOneTask() throws BonitaException {
    generateProcesses();
    firstUserProcess = getManagementAPI().deploy(getBusinessArchive(firstUserProcess));

    getManagementAPI().addExceptionsToRuleByUUID(ruleATeamUUID, getProcessUUIDSet(firstUserProcess));

    setApplicationContext(aTeam);
    assertNull(getQueryRuntimeAPI().getOneTask(ActivityState.READY));

    setApplicationContext(bTeam);
    assertNull(getQueryRuntimeAPI().getOneTask(ActivityState.READY));
    getRuntimeAPI().instantiateProcess(firstUserProcess.getUUID());

    setApplicationContext(aTeam);
    assertNotNull(getQueryRuntimeAPI().getOneTask(ActivityState.READY));

    setApplicationContext(bTeam);
    assertNull(getQueryRuntimeAPI().getOneTask(ActivityState.READY));

    setApplicationContext(aTeam);
    getManagementAPI().deleteProcess(firstUserProcess.getUUID());
  }

  public void testGetTaskList() throws Exception {
    generateProcesses();
    firstUserProcess = getManagementAPI().deploy(getBusinessArchive(firstUserProcess));
    secondUserProcess = getManagementAPI().deploy(getBusinessArchive(secondUserProcess));

    getRuntimeAPI().instantiateProcess(firstUserProcess.getUUID());
    getRuntimeAPI().instantiateProcess(firstUserProcess.getUUID());
    getRuntimeAPI().instantiateProcess(firstUserProcess.getUUID());
    getRuntimeAPI().instantiateProcess(firstUserProcess.getUUID());
    getRuntimeAPI().instantiateProcess(secondUserProcess.getUUID());

    getManagementAPI().addExceptionsToRuleByUUID(ruleATeamUUID, getProcessUUIDSet(firstUserProcess));

    setApplicationContext(aTeam);
    Collection<TaskInstance> instances = getQueryRuntimeAPI().getTaskList(ActivityState.READY);
    assertEquals(4, instances.size());

    getManagementAPI().addExceptionsToRuleByUUID(ruleATeamUUID, getProcessUUIDSet(secondUserProcess));
    instances = getQueryRuntimeAPI().getTaskList(ActivityState.READY);
    assertEquals(5, instances.size());

    getManagementAPI().deleteAllProcesses();
  }

  public void testGetTaskListId() throws Exception {
    generateProcesses();
    firstUserProcess = getManagementAPI().deploy(getBusinessArchive(firstUserProcess));
    secondUserProcess = getManagementAPI().deploy(getBusinessArchive(secondUserProcess));

    getRuntimeAPI().instantiateProcess(firstUserProcess.getUUID());
    getRuntimeAPI().instantiateProcess(firstUserProcess.getUUID());
    getRuntimeAPI().instantiateProcess(firstUserProcess.getUUID());
    getRuntimeAPI().instantiateProcess(firstUserProcess.getUUID());
    getRuntimeAPI().instantiateProcess(secondUserProcess.getUUID());

    getManagementAPI().addExceptionsToRuleByUUID(ruleATeamUUID, getProcessUUIDSet(firstUserProcess));

    setApplicationContext(aTeam);
    Collection<TaskInstance> instances = getQueryRuntimeAPI().getTaskList(getLogin(), ActivityState.READY);
    assertEquals(4, instances.size());

    getManagementAPI().addExceptionsToRuleByUUID(ruleATeamUUID, getProcessUUIDSet(secondUserProcess));
    instances = getQueryRuntimeAPI().getTaskList(getLogin(), ActivityState.READY);
    assertEquals(5, instances.size());

    getManagementAPI().deleteAllProcesses();
  }

  public void testGetLightProcessInstances() throws Exception {
    generateProcesses();
    firstUserProcess = getManagementAPI().deploy(getBusinessArchive(firstUserProcess));
    getManagementAPI().addExceptionsToRuleByUUID(ruleATeamUUID, getProcessUUIDSet(firstUserProcess));
    for (int i = 0; i < 10; i++) {
      getRuntimeAPI().instantiateProcess(firstUserProcess.getUUID());
    }

    setApplicationContext(aTeam);
    assertEquals(8, getQueryRuntimeAPI().getLightProcessInstances(0, 8).size());
    assertEquals(10, getQueryRuntimeAPI().getLightProcessInstances(0, 20).size());

    setApplicationContext(bTeam);
    assertEquals(0, getQueryRuntimeAPI().getLightProcessInstances(0, 8).size());
    assertEquals(0, getQueryRuntimeAPI().getLightProcessInstances(0, 20).size());

    setApplicationContext(aTeam);
    getManagementAPI().deleteAllProcesses();
  }

  public void testGetLightParentProcessInstances() throws Exception {
    ProcessDefinition process = ProcessBuilder.createProcess("pProcess", "1.0").addSubProcess("aSubProcess", "aSubProcess").done();

    ProcessDefinition subProcess = ProcessBuilder.createProcess("aSubProcess", "1.0").addHuman(getLogin()).addHumanTask("h", getLogin()).done();

    subProcess = getManagementAPI().deploy(getBusinessArchive(subProcess));
    process = getManagementAPI().deploy(getBusinessArchive(process));

    getManagementAPI().addExceptionsToRuleByUUID(ruleATeamUUID, getProcessUUIDSet(process));
    Set<ProcessDefinitionUUID> definitionsUUIDs = new HashSet<ProcessDefinitionUUID>();
    definitionsUUIDs.add(process.getUUID());
    definitionsUUIDs.add(subProcess.getUUID());
    getManagementAPI().addExceptionsToRuleByUUID(ruleBTeamUUID, definitionsUUIDs);
    ProcessDefinitionUUID parentProcessUUID = process.getUUID();

    for (int i = 0; i < 10; i++) {
      getRuntimeAPI().instantiateProcess(parentProcessUUID);
    }

    setApplicationContext(aTeam);
    assertEquals(8, getQueryRuntimeAPI().getLightParentProcessInstances(0, 8).size());
    assertEquals(10, getQueryRuntimeAPI().getLightParentProcessInstances(0, 20).size());

    setApplicationContext(bTeam);
    assertEquals(8, getQueryRuntimeAPI().getLightParentProcessInstances(0, 8).size());
    assertEquals(10, getQueryRuntimeAPI().getLightParentProcessInstances(0, 20).size());

    deleteAllProcesses(aTeam, bTeam);
  }

  public void testDeleteAnSharedInstance() throws Exception {
    generateProcesses();
    firstUserProcess = getManagementAPI().deploy(getBusinessArchive(firstUserProcess));
    secondUserProcess = getManagementAPI().deploy(getBusinessArchive(secondUserProcess));

    getRuntimeAPI().instantiateProcess(firstUserProcess.getUUID());
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(firstUserProcess.getUUID());
    getRuntimeAPI().instantiateProcess(secondUserProcess.getUUID());

    getManagementAPI().addExceptionsToRuleByUUID(ruleATeamUUID, getProcessUUIDSet(firstUserProcess));
    getManagementAPI().addExceptionsToRuleByUUID(ruleBTeamUUID, getProcessUUIDSet(firstUserProcess));

    setApplicationContext(aTeam);
    Collection<TaskInstance> instances = getQueryRuntimeAPI().getTaskList(getLogin(), ActivityState.READY);
    assertEquals(2, instances.size());

    setApplicationContext(bTeam);
    instances = getQueryRuntimeAPI().getTaskList(getLogin(), ActivityState.READY);
    assertEquals(2, instances.size());

    getRuntimeAPI().deleteProcessInstance(instanceUUID);

    instances = getQueryRuntimeAPI().getTaskList(getLogin(), ActivityState.READY);
    assertEquals(1, instances.size());

    setApplicationContext(aTeam);
    assertEquals(1, instances.size());

    getManagementAPI().addExceptionsToRuleByUUID(ruleATeamUUID, getProcessUUIDSet(secondUserProcess));
    deleteAllProcesses(aTeam, bTeam);
  }

  private Set<ProcessDefinitionUUID> getProcessUUIDSet(ProcessDefinition... processes) {
    HashSet<ProcessDefinitionUUID> result = new HashSet<ProcessDefinitionUUID>();
    for (ProcessDefinition definition : processes) {
      result.add(definition.getUUID());
    }
    return result;
  }

  private String getRuleName(String suffix) {
    return "rule-" + suffix;
  }

  private Set<String> getEntityNameSet(String... entityName) {
    HashSet<String> result = new HashSet<String>();
    for (String entity : entityName) {
      result.add(entity);
    }
    return result;
  }
}
