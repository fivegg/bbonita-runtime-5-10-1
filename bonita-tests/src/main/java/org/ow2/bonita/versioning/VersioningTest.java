package org.ow2.bonita.versioning;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.ow2.bonita.APITestCase;
import org.ow2.bonita.facade.def.element.BusinessArchive;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition.ProcessState;
import org.ow2.bonita.facade.exception.DeploymentException;
import org.ow2.bonita.facade.exception.ProcessNotFoundException;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.hook.misc.IncrementHook;
import org.ow2.bonita.hook.misc.MiscHookTest;
import org.ow2.bonita.util.BonitaException;
import org.ow2.bonita.util.Misc;

public class VersioningTest extends APITestCase {

  private final String processId = "workflowProcess";

  /* ************************
   * TEST DEPLOYMENT FAILURE**************************
   */

  public void testTwiceSameBothVersionDeployment() throws BonitaException {
    testFirstDeployOkSecondFail("WorkflowPack1.0Proc1.0.xpdl", "WorkflowPack1.0Proc1.0.xpdl");
  }

  public void testDiffPackageVersionSameProcessVersion() throws BonitaException {
    testFirstDeployOkSecondFail("WorkflowPack1.0Proc1.0.xpdl", "WorkflowPack1.1Proc1.0.xpdl");
  }

  public void testWrongPackageVersion() throws BonitaException {
    // Deploy a package version, then try to deploy the same package with an
    // older version --> should fail
    testFirstDeployOkSecondFail("WorkflowPack1.1Proc1.0.xpdl", "WorkflowPack1.0Proc1.0.xpdl");
  }

  public void testWrongProcessVersion() throws BonitaException {
    // Deploy a process version, then try to deploy the same process with an
    // older version --> should fail
    testFirstDeployOkSecondFail("WorkflowPack1.0Proc1.1.xpdl", "WorkflowPack1.1Proc1.0.xpdl");
  }

  private void testFirstDeployOkSecondFail(final String url1, final String url2) throws BonitaException {
    final URL xpdlURL1 = VersioningTest.class.getResource(url1);
    final URL xpdlURL2 = VersioningTest.class.getResource(url2);
    ProcessDefinition processDef = null;
    try {
      processDef = getManagementAPI().deploy(getBusinessArchiveFromXpdl(xpdlURL1));
    } catch (final DeploymentException e) {
      fail("First deployment should pass");
    }
    try {
      getManagementAPI().deploy(getBusinessArchiveFromXpdl(xpdlURL2));
      fail("Second deployment should fail");
    } catch (final DeploymentException e) {
      // Test pass
      e.printStackTrace();
    }
    final Set<ProcessDefinition> processes = getQueryDefinitionAPI().getProcesses(ProcessState.ENABLED);
    assertEquals(1, processes.size());
    assertTrue(processes.contains(processDef));
    getManagementAPI().disable(processDef.getUUID());
    getManagementAPI().deleteProcess(processDef.getUUID());

  }

  // TEST DEPLOY OK
  // Test right processes belongs to right package, right instances belongs to
  // right processes
  // Test right processes undeployment

  public void testTwiceDifferentPackageProcessVersion() throws BonitaException {
    deployOkTestGoodBelongings("WorkflowPack1.0Proc1.0.xpdl", "1.0", "1.0", "WorkflowPack1.1Proc1.1.xpdl", "1.1", "1.1");
  }

  public void testSameProcessVersion() throws BonitaException {
    testFirstDeployOkSecondFail("WorkflowPack1.0Proc1.0.xpdl", "WorkflowPack2_1.0Proc1.0.xpdl");
  }

  // Deploy two same process in different version --> must be successful
  // Test if the first process belong to the first package, process 2 to the
  // package 2
  // Same test with instances and processes
  // Test of undeployment : undeploy second package, check process 1 and package
  // 1 are still in the engine / Then do the inverse
  private void deployOkTestGoodBelongings(final String url1, final String pack1Version, final String process1Version,
      final String url2, final String pack2Version, final String process2Version) throws BonitaException {

    final URL xpdlURL1 = VersioningTest.class.getResource(url1);
    final URL xpdlURL2 = VersioningTest.class.getResource(url2);
    ProcessDefinition process1 = null, process2 = null;
    ProcessInstanceUUID instance1 = null, instance2 = null;
    try {
      process1 = getManagementAPI().deploy(getBusinessArchiveFromXpdl(xpdlURL1));
    } catch (final DeploymentException e) {
      fail("First deployment should pass");
    }
    try {
      process2 = getManagementAPI().deploy(getBusinessArchiveFromXpdl(xpdlURL2));
    } catch (final DeploymentException e) {
      fail("Second deployment should pass");
    }
    final Set<ProcessDefinition> processes = getQueryDefinitionAPI().getProcesses(processId, ProcessState.ENABLED);
    assertEquals(2, processes.size());

    try {
      instance1 = getRuntimeAPI().instantiateProcess(process1.getUUID());
      instance2 = getRuntimeAPI().instantiateProcess(process2.getUUID());

      assertEquals(process1.getUUID(), getQueryRuntimeAPI().getProcessInstance(instance1).getProcessDefinitionUUID());
      assertEquals(process2.getUUID(), getQueryRuntimeAPI().getProcessInstance(instance2).getProcessDefinitionUUID());
    } catch (final ProcessNotFoundException e) {
      fail("Instantiation should pass");
    }

    // Test undeploy package 2 then package 1
    checkUndeployment(process2, process1);

    process1 = getManagementAPI().deploy(getBusinessArchiveFromXpdl(xpdlURL1));
    process2 = getManagementAPI().deploy(getBusinessArchiveFromXpdl(xpdlURL2));

    getRuntimeAPI().instantiateProcess(process1.getUUID());
    getRuntimeAPI().instantiateProcess(process2.getUUID());

    // Test undeploy package 1 then package 2
    checkUndeployment(process1, process2);
  }

  // Check if the right process version is undeployed
  private void checkUndeployment(final ProcessDefinition process1, final ProcessDefinition process2)
      throws BonitaException {
    getManagementAPI().disable(process1.getUUID());
    getManagementAPI().archive(process1.getUUID());
    assertEquals(1, getQueryDefinitionAPI().getProcesses(processId, ProcessState.ENABLED).size());
    assertTrue(getQueryDefinitionAPI().getProcesses(processId, ProcessState.ENABLED).contains(process2));
    assertEquals(1, getQueryDefinitionAPI().getProcesses(processId, ProcessState.ARCHIVED).size());
    assertTrue(getQueryDefinitionAPI().getProcesses(processId, ProcessState.ARCHIVED).contains(process1));

    getManagementAPI().disable(process2.getUUID());
    getManagementAPI().archive(process2.getUUID());
    assertEquals(0, getQueryDefinitionAPI().getProcesses(processId, ProcessState.ENABLED).size());
    assertEquals(2, getQueryDefinitionAPI().getProcesses(processId, ProcessState.ARCHIVED).size());
    assertTrue(getQueryDefinitionAPI().getProcesses(processId, ProcessState.ARCHIVED).contains(process1));
    assertTrue(getQueryDefinitionAPI().getProcesses(processId, ProcessState.ARCHIVED).contains(process2));

    getManagementAPI().deleteProcess(process1.getUUID());
    getManagementAPI().deleteProcess(process2.getUUID());
  }

  // HOOK TEST
  public void testHookDiffPackAndProcessVersions() throws BonitaException, IOException {
    checkHooks("hook/HookPack1.0Proc1.0.xpdl", "hook/HookPack1.1Proc1.1.xpdl");
  }

  private void checkHooks(final String hookFile1, final String hookFile2) throws BonitaException, IOException {
    // check rights hooks are used with different process versions
    final InputStream in = MiscHookTest.class.getResourceAsStream("IncrementHook2.bytecode");
    if (in == null) {
      throw new IllegalArgumentException("Can't find ressource: IncrementHook2.bytecode");
    }
    final byte[] incrementHook2 = Misc.getAllContentFrom(in);
    in.close();

    final URL xpdlUrl1 = this.getClass().getResource(hookFile1);
    final ProcessDefinition process1 = getManagementAPI().deploy(
        getBusinessArchiveFromXpdl(xpdlUrl1, IncrementHook.class));

    final URL xpdlUrl2 = this.getClass().getResource(hookFile2);
    final Set<byte[]> hookBytes = new HashSet<byte[]>();
    hookBytes.add(incrementHook2);

    final Map<String, byte[]> resources = new HashMap<String, byte[]>();
    resources.put(IncrementHook.class.getName().replace(".", "/") + ".class", incrementHook2);
    final ProcessDefinition process2 = getManagementAPI().deploy(getBusinessArchiveFromXpdl(xpdlUrl2, resources));

    final ProcessDefinitionUUID processUUID1 = process1.getUUID();
    final ProcessDefinitionUUID processUUID2 = process2.getUUID();

    final ProcessInstanceUUID instanceUUID1 = getRuntimeAPI().instantiateProcess(processUUID1);
    final ProcessInstanceUUID instanceUUID2 = getRuntimeAPI().instantiateProcess(processUUID2);

    checkExecutedOnce(instanceUUID1, new String[] { "uniqueActivity" });
    String counter = (String) getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID1, "counter");
    assertEquals("1", counter);

    checkExecutedOnce(instanceUUID2, new String[] { "uniqueActivity" });
    counter = (String) getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID2, "counter");
    assertEquals("2", counter);

    getManagementAPI().deleteProcess(process1.getUUID());
    getManagementAPI().deleteProcess(process2.getUUID());
  }

  // TEST UNDEPLOY WITH RUNNING INSTANCES

  public void testRunInstDiffPackAndProcVersion() throws Exception {
    checkUndeployRunningInstances("WorkflowRuntimePack1.0Proc1.0.xpdl", "WorkflowRuntimePack1.1Proc1.1.xpdl");
  }

  private void checkUndeployRunningInstances(final String url1, final String url2) throws BonitaException {
    // Check instances belong to the right process definition
    final URL xpdlURL1 = VersioningTest.class.getResource(url1);
    final URL xpdlURL2 = VersioningTest.class.getResource(url2);
    ProcessDefinition process1, process2;
    ProcessInstanceUUID instanceUUID1, instanceUUID2;
    final BusinessArchive ba1 = getBusinessArchiveFromXpdl(xpdlURL1, WorkflowException.class);
    final BusinessArchive ba2 = getBusinessArchiveFromXpdl(xpdlURL2, WorkflowException.class);
    process1 = getManagementAPI().deploy(ba1);
    process2 = getManagementAPI().deploy(ba2);

    // now it's possible to disable a process with running instances
    instanceUUID1 = getRuntimeAPI().instantiateProcess(process1.getUUID());
    getManagementAPI().disable(process1.getUUID());

    try {
      getManagementAPI().disable(process2.getUUID());
      getManagementAPI().deleteProcess(process2.getUUID());
    } catch (final DeploymentException e) {
      fail("Undeployment should pass");
    }

    getRuntimeAPI().deleteProcessInstance(instanceUUID1);
    getManagementAPI().deleteProcess(process1.getUUID());

    process1 = getManagementAPI().deploy(ba1);
    process2 = getManagementAPI().deploy(ba2);
    instanceUUID2 = getRuntimeAPI().instantiateProcess(process2.getUUID());

    try {
      getManagementAPI().disable(process1.getUUID());
    } catch (final DeploymentException e) {
      fail("Undeployment should pass");
    }
    // now it's possible to disable a process with running instances
    try {
      getManagementAPI().disable(process2.getUUID());
    } catch (final DeploymentException e) {
      fail("Undeployment should pass");
    }
    getRuntimeAPI().deleteProcessInstance(instanceUUID2);
    getManagementAPI().deleteProcess(process1.getUUID());
    getManagementAPI().deleteProcess(process2.getUUID());
  }

}
