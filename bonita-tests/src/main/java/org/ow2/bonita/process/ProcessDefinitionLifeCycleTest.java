package org.ow2.bonita.process;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import org.junit.Assert;
import org.ow2.bonita.APITestCase;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition.ProcessState;
import org.ow2.bonita.facade.exception.DeploymentException;
import org.ow2.bonita.facade.runtime.ActivityState;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.light.LightActivityInstance;
import org.ow2.bonita.util.BonitaException;
import org.ow2.bonita.util.BonitaRuntimeException;
import org.ow2.bonita.util.ProcessBuilder;

public class ProcessDefinitionLifeCycleTest extends APITestCase {

  public void testProcessEnableToArchive() throws BonitaException {
    ProcessDefinition process = getManagementAPI().deploy(getBusinessArchive(getProcess()));
    ProcessDefinitionUUID processUUID = process.getUUID();
    try {
      getManagementAPI().archive(processUUID);
      fail("Impossible to archive an enable process");
    } catch (DeploymentException e) {
      // OK
    } 
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testCannotEnableAProcessWhichIsArchived() throws BonitaException {
    ProcessDefinition process = getManagementAPI().deploy(getBusinessArchive(getProcess()));
    ProcessDefinitionUUID processUUID = process.getUUID();
    Set<ProcessDefinition> processes = getQueryDefinitionAPI().getProcesses(ProcessState.ENABLED);
    Assert.assertEquals(1, processes.size());
    // Disable state
    getManagementAPI().disable(processUUID);
    getManagementAPI().archive(processUUID);
    try {
      getManagementAPI().enable(processUUID);
      fail("Impossible to enable a process which is archived");
    } catch (DeploymentException e) {
      // OK
    } 
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testCannotDisableAProcessWhichIsArchived() throws BonitaException {
    ProcessDefinition process = getManagementAPI().deploy(getBusinessArchive(getProcess()));
    ProcessDefinitionUUID processUUID = process.getUUID();
    // Disable state
    getManagementAPI().disable(processUUID);
    getManagementAPI().archive(processUUID);
    try {
      getManagementAPI().disable(processUUID);
      fail("Impossible to disable a process which is archived");
    } catch (DeploymentException e) {
      // OK
    } 
    getManagementAPI().deleteProcess(processUUID);
  }


  // Process Cycle ENABLE <-> DISABLE -> ARCHIVED
  private ProcessDefinition getProcess() {
    return ProcessBuilder.createProcess("processCycle", "1.0").addHuman("john").addHumanTask("yksi", "john").addSystemTask("kaksi").addTransition("yks_kaks", "yksi", "kaksi").done();
  }

  private ProcessDefinition getAutomaticProcess() {
    return ProcessBuilder.createProcess("AaaA", "1.0").addSystemTask("theA").done();
  }

  private ProcessDefinition getHumanProcess() {
    return ProcessBuilder.createProcess("AaBaA", "1.0").addHuman("john").addHumanTask("Bb", "john").done();
  }

  private ProcessDefinition getHumanAndAutomaticProcess() {
    return ProcessBuilder.createProcess("four", "1.0").addHuman("john").addHumanTask("Bb", "john").addSystemTask("theA").addTransition("Bb", "theA").done();
  }

  private Collection<ProcessDefinition> getProcesses() {
    Collection<ProcessDefinition> definitions = new ArrayList<ProcessDefinition>();
    definitions.add(getProcess());
    definitions.add(getAutomaticProcess());
    definitions.add(getHumanProcess());
    definitions.add(getHumanAndAutomaticProcess());
    return definitions;
  }

  public void testDeployAndDeleteProcesses() throws Exception {
    Collection<ProcessDefinition> definitions = getProcesses();
    Collection<ProcessDefinitionUUID> definitionUUIDs = new ArrayList<ProcessDefinitionUUID>();
    for (ProcessDefinition processDefinition : definitions) {
      getManagementAPI().deploy(getBusinessArchive(processDefinition));
    }
    Set<ProcessDefinition> processes = getQueryDefinitionAPI().getProcesses(ProcessState.ENABLED);
    Assert.assertEquals(4, processes.size());
    for (ProcessDefinition processDefinition : processes) {
      definitionUUIDs.add(processDefinition.getUUID());
    }
    getManagementAPI().delete(definitionUUIDs);
    processes = getQueryDefinitionAPI().getProcesses(ProcessState.ENABLED);
    Assert.assertEquals(0, processes.size());
    processes = getQueryDefinitionAPI().getProcesses(ProcessState.DISABLED);
    Assert.assertEquals(0, processes.size());
    processes = getQueryDefinitionAPI().getProcesses(ProcessState.ARCHIVED);
    Assert.assertEquals(0, processes.size());
  }

  public void testDeployDisableArchiveAndDeleteProcesses() throws Exception {
    Collection<ProcessDefinition> definitions = getProcesses();
    Collection<ProcessDefinitionUUID> definitionUUIDs = new ArrayList<ProcessDefinitionUUID>();
    for (ProcessDefinition processDefinition : definitions) {
      getManagementAPI().deploy(getBusinessArchive(processDefinition));
    }
    Set<ProcessDefinition> processes = getQueryDefinitionAPI().getProcesses(ProcessState.ENABLED);
    Assert.assertEquals(4, processes.size());
    for (ProcessDefinition processDefinition : processes) {
      definitionUUIDs.add(processDefinition.getUUID());
    }
    getManagementAPI().disable(definitionUUIDs);

    processes = null;
    definitionUUIDs = new ArrayList<ProcessDefinitionUUID>();
    processes = getQueryDefinitionAPI().getProcesses(ProcessState.DISABLED);
    Assert.assertEquals(4, processes.size());
    for (ProcessDefinition processDefinition : processes) {
      definitionUUIDs.add(processDefinition.getUUID());
    }
    getManagementAPI().archive(definitionUUIDs);

    processes = null;
    definitionUUIDs = new ArrayList<ProcessDefinitionUUID>();
    processes = getQueryDefinitionAPI().getProcesses(ProcessState.ARCHIVED);
    Assert.assertEquals(4, processes.size());
    for (ProcessDefinition processDefinition : processes) {
      definitionUUIDs.add(processDefinition.getUUID());
    }
    getManagementAPI().delete(definitionUUIDs);
  }

  public void testDeployDisableEnableDisableArchiveAndDeleteProcesses() throws Exception {
    Collection<ProcessDefinition> definitions = getProcesses();
    Collection<ProcessDefinitionUUID> definitionUUIDs = new ArrayList<ProcessDefinitionUUID>();
    for (ProcessDefinition processDefinition : definitions) {
      getManagementAPI().deploy(getBusinessArchive(processDefinition));
    }
    Set<ProcessDefinition> processes = getQueryDefinitionAPI().getProcesses(ProcessState.ENABLED);
    Assert.assertEquals(4, processes.size());
    for (ProcessDefinition processDefinition : processes) {
      definitionUUIDs.add(processDefinition.getUUID());
    }
    getManagementAPI().disable(definitionUUIDs);

    processes = null;
    definitionUUIDs = new ArrayList<ProcessDefinitionUUID>();
    processes = getQueryDefinitionAPI().getProcesses(ProcessState.DISABLED);
    Assert.assertEquals(4, processes.size());
    for (ProcessDefinition processDefinition : processes) {
      definitionUUIDs.add(processDefinition.getUUID());
    }
    getManagementAPI().enable(definitionUUIDs);

    processes = null;
    definitionUUIDs = new ArrayList<ProcessDefinitionUUID>();
    processes = getQueryDefinitionAPI().getProcesses(ProcessState.ENABLED);
    Assert.assertEquals(4, processes.size());
    for (ProcessDefinition processDefinition : processes) {
      definitionUUIDs.add(processDefinition.getUUID());
    }
    getManagementAPI().disable(definitionUUIDs);

    processes = null;
    definitionUUIDs = new ArrayList<ProcessDefinitionUUID>();
    processes = getQueryDefinitionAPI().getProcesses(ProcessState.DISABLED);
    Assert.assertEquals(4, processes.size());
    for (ProcessDefinition processDefinition : processes) {
      definitionUUIDs.add(processDefinition.getUUID());
    }
    getManagementAPI().archive(definitionUUIDs);

    processes = null;
    definitionUUIDs = new ArrayList<ProcessDefinitionUUID>();
    processes = getQueryDefinitionAPI().getProcesses(ProcessState.ARCHIVED);
    Assert.assertEquals(4, processes.size());
    for (ProcessDefinition processDefinition : processes) {
      definitionUUIDs.add(processDefinition.getUUID());
    }
    getManagementAPI().delete(definitionUUIDs);
  }

  public void testCanDisableProcessWithExecutingInstances() throws Exception {
    Collection<ProcessDefinition> definitions = new ArrayList<ProcessDefinition>();
    definitions.add(getProcess());
    definitions.add(getAutomaticProcess());
    definitions.add(getHumanAndAutomaticProcess());
    ProcessDefinition process = getHumanProcess();

    Collection<ProcessDefinitionUUID> definitionUUIDs = new ArrayList<ProcessDefinitionUUID>();
    for (ProcessDefinition processDefinition : definitions) {
      getManagementAPI().deploy(getBusinessArchive(processDefinition));
    }
    process = getManagementAPI().deploy(getBusinessArchive(process));
    Set<ProcessDefinition> processes = getQueryDefinitionAPI().getProcesses(ProcessState.ENABLED);
    Assert.assertEquals(4, processes.size());
    for (ProcessDefinition processDefinition : processes) {
      definitionUUIDs.add(processDefinition.getUUID());
    }
    ProcessDefinitionUUID uuid = process.getUUID();
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(uuid);

    getManagementAPI().disable(definitionUUIDs);

    processes = null;
    processes = getQueryDefinitionAPI().getProcesses(ProcessState.DISABLED);
    Assert.assertEquals(4, processes.size());

    // modify instance of a disabled process
    Set<LightActivityInstance> activities = getQueryRuntimeAPI().getLightActivityInstances(instanceUUID);
    assertEquals(1, activities.size());
    LightActivityInstance activity = activities.iterator().next();
    assertEquals(ActivityState.READY, activity.getState());
    getRuntimeAPI().executeTask(activity.getUUID(), true);

    activities = getQueryRuntimeAPI().getLightActivityInstances(instanceUUID);
    assertEquals(1, activities.size());
    activity = activities.iterator().next();
    assertEquals(ActivityState.FINISHED, activity.getState());

    getRuntimeAPI().deleteProcessInstance(instanceUUID);

    definitionUUIDs = new ArrayList<ProcessDefinitionUUID>();
    processes = getQueryDefinitionAPI().getProcesses(ProcessState.DISABLED);
    for (ProcessDefinition processDefinition : processes) {
      definitionUUIDs.add(processDefinition.getUUID());
    }
    getManagementAPI().delete(definitionUUIDs);
  }

  public void testCannotArchiveAProcessWithRunningInstances() throws Exception {
    ProcessDefinition process = getManagementAPI().deploy(getBusinessArchive(getProcess()));
    ProcessDefinitionUUID processUUID = process.getUUID();
    getRuntimeAPI().instantiateProcess(processUUID);
    getRuntimeAPI().instantiateProcess(processUUID);
    getRuntimeAPI().instantiateProcess(processUUID);
    // Disable state
    getManagementAPI().disable(processUUID);

    try {
      getManagementAPI().archive(processUUID);
      fail("Impossible to archive a process which contains running instances");
    } catch (DeploymentException e) {
      // OK
    } 

    getManagementAPI().deleteProcess(processUUID);
  }

  public void testCannotCreateInstancesOfDisabledProcess() throws BonitaException {
    Collection<ProcessDefinition> definitions = new ArrayList<ProcessDefinition>();
    definitions.add(getProcess());
    definitions.add(getAutomaticProcess());
    definitions.add(getHumanAndAutomaticProcess());
    ProcessDefinition process = getHumanProcess();

    Collection<ProcessDefinitionUUID> definitionUUIDs = new ArrayList<ProcessDefinitionUUID>();
    for (ProcessDefinition processDefinition : definitions) {
      getManagementAPI().deploy(getBusinessArchive(processDefinition));
    }
    process = getManagementAPI().deploy(getBusinessArchive(process));
    Set<ProcessDefinition> processes = getQueryDefinitionAPI().getProcesses(ProcessState.ENABLED);
    Assert.assertEquals(4, processes.size());
    for (ProcessDefinition processDefinition : processes) {
      definitionUUIDs.add(processDefinition.getUUID());
    }

    getManagementAPI().disable(definitionUUIDs);

    try {
      ProcessDefinitionUUID uuid = process.getUUID();
      getRuntimeAPI().instantiateProcess(uuid);
      fail("Impossible to instanciate an disabled process");
    } catch (BonitaRuntimeException e) {
      // OK
    } 

    processes = null;
    definitionUUIDs = new ArrayList<ProcessDefinitionUUID>();
    processes = getQueryDefinitionAPI().getProcesses(ProcessState.DISABLED);
    Assert.assertEquals(4, processes.size());
    for (ProcessDefinition processDefinition : processes) {
      definitionUUIDs.add(processDefinition.getUUID());
    }

    getManagementAPI().delete(definitionUUIDs);
  }

  public void testASingleProcessCycleWithoutInstances() throws BonitaException {
    ProcessDefinition process = getManagementAPI().deploy(getBusinessArchive(getProcess()));
    ProcessDefinitionUUID processUUID = process.getUUID();

    Set<ProcessDefinition> processes = getQueryDefinitionAPI().getProcesses(ProcessState.ENABLED);
    Assert.assertEquals(1, processes.size());
    // Disable state
    getManagementAPI().disable(processUUID);
    processes = getQueryDefinitionAPI().getProcesses(ProcessState.ENABLED);
    Assert.assertEquals(0, processes.size());
    processes = getQueryDefinitionAPI().getProcesses(ProcessState.DISABLED);
    Assert.assertEquals(1, processes.size());
    // Archive state
    getManagementAPI().archive(processUUID);
    processes = getQueryDefinitionAPI().getProcesses(ProcessState.ENABLED);
    Assert.assertEquals(0, processes.size());
    processes = getQueryDefinitionAPI().getProcesses(ProcessState.DISABLED);
    Assert.assertEquals(0, processes.size());
    processes = getQueryDefinitionAPI().getProcesses(ProcessState.ARCHIVED);
    Assert.assertEquals(1, processes.size());
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testAProcessCycleWithTwoCyclesWithoutInstances() throws BonitaException {
    ProcessDefinition process = getManagementAPI().deploy(getBusinessArchive(getProcess()));
    ProcessDefinitionUUID processUUID = process.getUUID();

    Set<ProcessDefinition> processes = getQueryDefinitionAPI().getProcesses(ProcessState.ENABLED);
    Assert.assertEquals(1, processes.size());
    // Disable state
    getManagementAPI().disable(processUUID);
    processes = getQueryDefinitionAPI().getProcesses(ProcessState.ENABLED);
    Assert.assertEquals(0, processes.size());
    processes = getQueryDefinitionAPI().getProcesses(ProcessState.DISABLED);
    Assert.assertEquals(1, processes.size());

    // Enable state
    getManagementAPI().enable(processUUID);
    processes = getQueryDefinitionAPI().getProcesses(ProcessState.ENABLED);
    Assert.assertEquals(1, processes.size());
    processes = getQueryDefinitionAPI().getProcesses(ProcessState.DISABLED);
    Assert.assertEquals(0, processes.size());

    // Disable state
    getManagementAPI().disable(processUUID);
    processes = getQueryDefinitionAPI().getProcesses(ProcessState.ENABLED);
    Assert.assertEquals(0, processes.size());
    processes = getQueryDefinitionAPI().getProcesses(ProcessState.DISABLED);
    Assert.assertEquals(1, processes.size());

    // Archive state
    getManagementAPI().archive(processUUID);
    processes = getQueryDefinitionAPI().getProcesses(ProcessState.ENABLED);
    Assert.assertEquals(0, processes.size());
    processes = getQueryDefinitionAPI().getProcesses(ProcessState.DISABLED);
    Assert.assertEquals(0, processes.size());
    processes = getQueryDefinitionAPI().getProcesses(ProcessState.ARCHIVED);
    Assert.assertEquals(1, processes.size());
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testASingleProcessCycleWithInstances() throws BonitaException {
    ProcessDefinition process = getManagementAPI().deploy(getBusinessArchive(getProcess()));
    ProcessDefinitionUUID processUUID = process.getUUID();
    Set<ProcessDefinition> processes = getQueryDefinitionAPI().getProcesses(ProcessState.ENABLED);
    Assert.assertEquals(1, processes.size());
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    getManagementAPI().disable(processUUID);
    processes = getQueryDefinitionAPI().getProcesses(ProcessState.ENABLED);
    Assert.assertEquals(0, processes.size());
    processes = getQueryDefinitionAPI().getProcesses(ProcessState.DISABLED);
    Assert.assertEquals(1, processes.size());

    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

}
