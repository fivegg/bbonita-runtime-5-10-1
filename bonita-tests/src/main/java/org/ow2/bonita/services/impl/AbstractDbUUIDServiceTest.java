package org.ow2.bonita.services.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.ow2.bonita.APITestCase;
import org.ow2.bonita.facade.RuntimeAPI;
import org.ow2.bonita.facade.def.element.HookDefinition.Event;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.runtime.InstanceState;
import org.ow2.bonita.facade.runtime.ProcessInstance;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.light.LightProcessInstance;
import org.ow2.bonita.util.AccessorUtil;
import org.ow2.bonita.util.ProcessBuilder;
import org.ow2.bonita.util.SimpleCallbackHandler;

public abstract class AbstractDbUUIDServiceTest extends APITestCase {

  protected abstract String getProcessPrefix();
  
  private static List<Throwable> failures = new ArrayList<Throwable>();

  public static synchronized void addFailure(final Throwable t) {
    failures.add(t);
  }

  public void testConcurrentUUIDCreation() throws Exception {
    final int threadNb = 10;
    final int loopNb = 10;

    ProcessDefinition process = ProcessBuilder.createProcess(getProcessPrefix() + "uuid", "1.0")
    .addIntegerData("counter", 0)
    .addSystemTask("start")
    .addSystemTask("t1")
    .addSystemTask("t2")
    .addConnector(Event.automaticOnExit, IncrementIntegerVariableConnector.class.getName(), true)
    .addInputParameter("setVariableName", "counter")
    .addSystemTask("end")
    .addTransition("start", "t1")
    .addTransition("t1", "t2")
    .addTransition("t2", "t1")
    .addCondition("counter < " + loopNb)
    .addTransition("t2", "end")
    .addCondition("counter >= " +loopNb)
    .done();

    getManagementAPI().deploy(getBusinessArchive(process, null, IncrementIntegerVariableConnector.class));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    final Set<Long> expectedInstancesNb = new HashSet<Long>();
    final Set<UUIDThread> threads = new HashSet<UUIDThread>();
    for (int i = 0 ; i < threadNb ; i++) {
      threads.add(new UUIDThread(processUUID));
      expectedInstancesNb.add(new Long(i + 1));
    }

    for (UUIDThread uuidThread : threads) {
      uuidThread.start();
    }
    for (UUIDThread uuidThread : threads) {
      uuidThread.join();
    }

    System.err.println("Got " + failures.size() + " failures:\n*********\n");

    for (Throwable t : failures) {
      System.err.println("Failure:\n");
      t.printStackTrace();
    }
    System.err.println("\n*********\n");

    assertEquals(failures.size() + " threads have failed.", 0, failures.size());
    assertEquals(threadNb, getQueryRuntimeAPI().getNumberOfProcessInstances());

    //check all instances are finished
    final Set<Long> actualInstancesNb = new HashSet<Long>();
    final Set<LightProcessInstance> allInstances = getQueryRuntimeAPI().getLightProcessInstances();
    for (LightProcessInstance lightProcessInstance : allInstances) {
      System.err.println("Instance: " + lightProcessInstance.getUUID() + " is " + lightProcessInstance.getInstanceState());
      assertEquals(InstanceState.FINISHED, lightProcessInstance.getInstanceState());
      actualInstancesNb.add(lightProcessInstance.getNb());
    }
    assertEquals(expectedInstancesNb, actualInstancesNb);

    getManagementAPI().deleteProcess(processUUID);
  }

  private static class UUIDThread extends Thread {
    private ProcessDefinitionUUID processUUID;

    private static final String LOGIN = "admin";
    private static final String PASSWORD = "bpm";

    public UUIDThread(final ProcessDefinitionUUID processUUID) {
      this.processUUID = processUUID;
    }

    @Override
    public void run() {
      super.run();
      LoginContext loginContext = null;
      try {
        loginContext = new LoginContext("BonitaAuth", new SimpleCallbackHandler(LOGIN, PASSWORD));
        loginContext.login();
        loginContext.logout();
        loginContext = new LoginContext("BonitaStore", new SimpleCallbackHandler(LOGIN, PASSWORD));
        loginContext.login();

        final RuntimeAPI runtimeAPI = AccessorUtil.getRuntimeAPI();
        final ProcessInstanceUUID instanceUUID = runtimeAPI.instantiateProcess(processUUID);
        if (!InstanceState.FINISHED.equals(AccessorUtil.getQueryRuntimeAPI().getLightProcessInstance(instanceUUID).getInstanceState())) {
          throw new RuntimeException("Instance " + instanceUUID + " is not finished.");
        }
      } catch (Throwable t) {
        t.printStackTrace();
        AbstractDbUUIDServiceTest.addFailure(t);
      } finally {
        try {
          loginContext.logout();
        } catch (LoginException e) {
          e.printStackTrace();
        }
      }
    }
  }

  public void testUUIDContinuousSequence() throws Exception {
    ProcessDefinition process = ProcessBuilder.createProcess(getProcessPrefix() + "sequence", "1.0")
    .addHuman(getLogin())
    .addHumanTask("step1", getLogin())
    .done();

    getManagementAPI().deploy(getBusinessArchive(process));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    ProcessInstanceUUID instanceUUID1 = getRuntimeAPI().instantiateProcess(processUUID);
    ProcessInstanceUUID instanceUUID2 = getRuntimeAPI().instantiateProcess(processUUID);
    ProcessInstanceUUID instanceUUID3 = getRuntimeAPI().instantiateProcess(processUUID);
    ProcessInstance instance1 = getQueryRuntimeAPI().getProcessInstance(instanceUUID1);
    ProcessInstance instance2 = getQueryRuntimeAPI().getProcessInstance(instanceUUID2);
    ProcessInstance instance3 = getQueryRuntimeAPI().getProcessInstance(instanceUUID3);

    assertEquals(1, instance1.getNb());
    assertEquals(2, instance2.getNb());
    assertEquals(3, instance3.getNb());

    getRuntimeAPI().deleteProcessInstance(instanceUUID2);
    ProcessInstanceUUID instanceUUID4 = getRuntimeAPI().instantiateProcess(processUUID);
    ProcessInstance instance4 = getQueryRuntimeAPI().getProcessInstance(instanceUUID4);
    assertEquals(4, instance4.getNb());

    getManagementAPI().deleteProcess(processUUID);
  }

  public void testUUIDSequence() throws Exception {
    ProcessDefinition process = ProcessBuilder.createProcess(getProcessPrefix() + "sequence2", "1.0")
    .addHuman(getLogin())
    .addHumanTask("step1", getLogin())
    .done();

    getManagementAPI().deploy(getBusinessArchive(process));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    ProcessInstanceUUID instanceUUID1 = getRuntimeAPI().instantiateProcess(processUUID);
    ProcessInstanceUUID instanceUUID2 = getRuntimeAPI().instantiateProcess(processUUID);
    ProcessInstanceUUID instanceUUID3 = getRuntimeAPI().instantiateProcess(processUUID);
    ProcessInstance instance1 = getQueryRuntimeAPI().getProcessInstance(instanceUUID1);
    ProcessInstance instance2 = getQueryRuntimeAPI().getProcessInstance(instanceUUID2);
    ProcessInstance instance3 = getQueryRuntimeAPI().getProcessInstance(instanceUUID3);

    assertEquals(1, instance1.getNb());
    assertEquals(2, instance2.getNb());
    assertEquals(3, instance3.getNb());
    getRuntimeAPI().deleteAllProcessInstances(processUUID);

    ProcessInstanceUUID instanceUUID4 = getRuntimeAPI().instantiateProcess(processUUID);
    ProcessInstance instance4 = getQueryRuntimeAPI().getProcessInstance(instanceUUID4);
    assertEquals(4, instance4.getNb());

    getManagementAPI().deleteProcess(processUUID);
  }

  public void testUUIDSequences() throws Exception {
    ProcessDefinition process = ProcessBuilder.createProcess(getProcessPrefix() + "sequence3", "1.0")
    .addHuman(getLogin())
    .addHumanTask("step1", getLogin())
    .done();

    getManagementAPI().deploy(getBusinessArchive(process));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    ProcessInstanceUUID instanceUUID1 = getRuntimeAPI().instantiateProcess(processUUID);
    ProcessInstanceUUID instanceUUID2 = getRuntimeAPI().instantiateProcess(processUUID);
    ProcessInstanceUUID instanceUUID3 = getRuntimeAPI().instantiateProcess(processUUID);
    ProcessInstance instance1 = getQueryRuntimeAPI().getProcessInstance(instanceUUID1);
    ProcessInstance instance2 = getQueryRuntimeAPI().getProcessInstance(instanceUUID2);
    ProcessInstance instance3 = getQueryRuntimeAPI().getProcessInstance(instanceUUID3);
    assertEquals(1, instance1.getNb());
    assertEquals(2, instance2.getNb());
    assertEquals(3, instance3.getNb());
    getRuntimeAPI().deleteProcessInstance(instanceUUID2);
    getRuntimeAPI().deleteProcessInstance(instanceUUID1);
    getRuntimeAPI().deleteProcessInstance(instanceUUID3);

    ProcessInstanceUUID instanceUUID4 = getRuntimeAPI().instantiateProcess(processUUID);
    ProcessInstance instance4 = getQueryRuntimeAPI().getProcessInstance(instanceUUID4);
    assertEquals(4, instance4.getNb());

    getManagementAPI().deleteProcess(processUUID);
  }

}
