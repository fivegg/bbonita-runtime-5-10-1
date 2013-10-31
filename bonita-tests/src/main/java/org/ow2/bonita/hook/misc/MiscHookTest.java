/**
 * Copyright (C) 2007  Bull S. A. S.
 * Bull, Rue Jean Jaures, B.P.68, 78340, Les Clayes-sous-Bois
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA  02110-1301, USA.
 **/
package org.ow2.bonita.hook.misc;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.Set;

import org.bonitasoft.connectors.bonita.SetVarConnector;
import org.ow2.bonita.APITestCase;
import org.ow2.bonita.facade.def.element.HookDefinition.Event;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.exception.DeploymentException;
import org.ow2.bonita.facade.runtime.ActivityState;
import org.ow2.bonita.facade.runtime.InstanceState;
import org.ow2.bonita.facade.uuid.ActivityInstanceUUID;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.light.LightTaskInstance;
import org.ow2.bonita.util.BonitaException;
import org.ow2.bonita.util.Misc;
import org.ow2.bonita.util.ProcessBuilder;

/**
 * @author "Charles Souillard"
 */
public class MiscHookTest extends APITestCase {

  public void testProcessInstanceVariableVisibility() throws BonitaException {
    //this test ensure that if you are chaining 2 connectors using a process instance variable, values are well propagated and used:
    //connector1: setProcessVariable, var1=2
    //connector2: use variable var1 (must be equals to 2)
    //this test also checks that if you get var1 and var2 value with the last state update
    
    ProcessDefinition process = ProcessBuilder.createProcess("parentProcess", "1.0")
    .addHuman(getLogin())
    .addIntegerData("var1", 0)
    .addIntegerData("var2", 0)
    .addHumanTask("t", getLogin())
      .addConnector(Event.taskOnFinish, SetVarConnector.class.getName(), true)
        .addInputParameter("variableName", "var1")
        .addInputParameter("value", "${2}")
      .addConnector(Event.taskOnFinish, SetVarConnector.class.getName(), true)
        .addInputParameter("variableName", "var2")
        .addInputParameter("value", "${var1 * 2}")
    .done();
    
    process = getManagementAPI().deploy(getBusinessArchive(process, null, SetVarConnector.class));
    
    final ProcessDefinitionUUID processUUID = process.getUUID();

    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);
    
    final Collection<LightTaskInstance> tasks = getQueryRuntimeAPI().getLightTaskList(instanceUUID, ActivityState.READY);
    assertEquals(1, tasks.size());
    final LightTaskInstance task = tasks.iterator().next();
    final ActivityInstanceUUID taskUUID = task.getUUID();
    assertEquals(0, getRuntimeAPI().evaluateGroovyExpression("${var1}", taskUUID, true, false));
    assertEquals(0, getRuntimeAPI().evaluateGroovyExpression("${var2}", taskUUID, true, false));
    
    getRuntimeAPI().executeTask(taskUUID, true);
    
    assertEquals(InstanceState.FINISHED, getQueryRuntimeAPI().getLightProcessInstance(instanceUUID).getInstanceState());
    
    assertEquals(0, getRuntimeAPI().evaluateGroovyExpression("${var1}", taskUUID, true, false));
    assertEquals(0, getRuntimeAPI().evaluateGroovyExpression("${var2}", taskUUID, true, false));
    
    assertEquals(2, getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "var1"));
    assertEquals(4, getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "var2"));

    getManagementAPI().deleteProcess(processUUID);
  }

	//http://www.bonitasoft.org/bugs/view.php?id=1396
	public void testOnFinishConnectorWithSubprocessMapping() throws BonitaException {
    ProcessDefinition parentProcess = ProcessBuilder.createProcess("parentProcess", "1.0")
    .addStringData("var1", "initialVar1")
    .addStringData("var2", "initialVar2")
    .addSubProcess("sub", "subProcess")
    .addSubProcessOutParameter("subVar", "var1")
    .addConnector(Event.automaticOnExit, SetVarConnector.class.getName(), true)
    .addInputParameter("variableName", "var2")
    .addInputParameter("value", "${var1}")
    .done();
    
    ProcessDefinition subProcess = ProcessBuilder.createProcess("subProcess", "1.0")
    .addStringData("subVar", "subProcessValue")
    .addSystemTask("activity")
    .done();
    
    parentProcess = getManagementAPI().deploy(getBusinessArchive(parentProcess, null, SetVarConnector.class));
    subProcess = getManagementAPI().deploy(getBusinessArchive(subProcess));
    
    final ProcessDefinitionUUID parentProcessUUID = parentProcess.getUUID();
    final ProcessDefinitionUUID subProcessUUID = subProcess.getUUID();

    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(parentProcessUUID);

    assertEquals(InstanceState.FINISHED, getQueryRuntimeAPI().getProcessInstance(instanceUUID).getInstanceState());
    
    //check variableValue
    assertEquals(getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "var1"), getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "var2"));
    assertEquals("subProcessValue", getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "var1"));
    
    getManagementAPI().deleteProcess(parentProcessUUID);
    getManagementAPI().deleteProcess(subProcessUUID);
  }
	
  public void testIncoherentHook() throws BonitaException {
    final URL xpdlUrl = this.getClass().getResource("incoherentHook.xpdl");
    final ProcessDefinition process = getManagementAPI().deploy(getBusinessArchiveFromXpdl(xpdlUrl,
        IncrementHook.class, FailingHook.class, IncoherentHook.class));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);
    checkExecutedOnce(instanceUUID, new String[]{"uniqueActivity"});
    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testFailingHook() throws BonitaException {
    final URL xpdlUrl = this.getClass().getResource("failingHook.xpdl");
    final ProcessDefinition process = getManagementAPI().deploy(getBusinessArchiveFromXpdl(xpdlUrl,
        IncrementHook.class, FailingHook.class, IncoherentHook.class));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);
    
    checkState(instanceUUID, ActivityState.FAILED, "uniqueActivity");
    checkState(instanceUUID, InstanceState.STARTED);
    
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testManyHooks() throws BonitaException {
    final URL xpdlUrl = this.getClass().getResource("manyHooks.xpdl");
    final ProcessDefinition process = getManagementAPI().deploy(getBusinessArchiveFromXpdl(xpdlUrl,
        IncrementHook.class, FailingHook.class, IncoherentHook.class));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);
    checkExecutedOnce(instanceUUID, new String[]{"uniqueActivity"});
    final String counter = (String) getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "counter");
    assertEquals("3", counter);
    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }
  
  public void testGlobalHook() throws BonitaException, IOException {
    // deploy a global class used in a process and check it is found
    final URL xpdlUrl = this.getClass().getResource("manyHooks.xpdl");
    getManagementAPI().deployJar("j1.jar", Misc.generateJar(IncrementHook.class, FailingHook.class, IncoherentHook.class));

    final ProcessDefinition process = getManagementAPI().deploy(getBusinessArchiveFromXpdl(xpdlUrl));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);
    checkExecutedOnce(instanceUUID, new String[]{"uniqueActivity"});
    final String counter = (String) getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "counter");
    assertEquals("3", counter);
    getManagementAPI().disable(processUUID);
    
    getManagementAPI().removeJar("j1.jar");
    
    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testDeployGlobalTwiceKO() throws BonitaException, IOException {
    //deploy two times the same class (className) and check that an exception is thrown
    getManagementAPI().deployJar("j2.jar", Misc.generateJar(IncrementHook.class));
    try {
      getManagementAPI().deployJar("j2.jar", Misc.generateJar(IncrementHook.class));
      fail("This test should have fail !!");
    } catch (final DeploymentException e) {
      getManagementAPI().removeJar("j2.jar");
      assertEquals("A jar with name: " + "j2.jar" + " already exists in repository.", e.getMessage());
    } 
  }

  public void testDeployGlobalTwiceOK() throws BonitaException, IOException {
    //deploy two times the same class (className) and check that an exception is thrown
    getManagementAPI().deployJar("j3.jar", Misc.generateJar(IncrementHook.class));
    getManagementAPI().removeJar("j3.jar");
    getManagementAPI().deployJar("j3.jar", Misc.generateJar(IncrementHook.class));
    getManagementAPI().removeJar("j3.jar");
  }

  public void testDeployProcessAndGlobalHook() throws BonitaException, IOException {
    //deploy the same class in process and global, check that the process one is used
    InputStream is = this.getClass().getResourceAsStream("inc.zip");
    getManagementAPI().deployJar("pagh.jar", Misc.getAllContentFrom(is));
    is.close();

    final URL xpdlUrl = this.getClass().getResource("manyHooks.xpdl");
    ProcessDefinition process = getManagementAPI().deploy(getBusinessArchiveFromXpdl(xpdlUrl,
        IncrementHook.class, FailingHook.class, IncoherentHook.class));

    ProcessDefinitionUUID processUUID = process.getUUID();

    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);
    checkExecutedOnce(instanceUUID, new String[]{"uniqueActivity"});
    String counter = (String) getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "counter");
    assertEquals("3", counter);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);

    Set<String> availableJars = getManagementAPI().getAvailableJars();
    assertEquals(availableJars.toString(), 2, getManagementAPI().getAvailableJars().size());
    assertTrue(availableJars.toString(), availableJars.contains("pagh.jar"));

    //do not deploy IncrementHook in package and check the global one is called
    process = getManagementAPI().deploy(getBusinessArchiveFromXpdl(xpdlUrl,
        FailingHook.class, IncoherentHook.class));
    processUUID = process.getUUID();
    instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);
    checkExecutedOnce(instanceUUID, new String[]{"uniqueActivity"});
    counter = (String) getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "counter");
    assertEquals("6", counter);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
    getManagementAPI().removeJar("pagh.jar");
  }
  
  public void testUndeploy() throws BonitaException, IOException {
    //check undeploy forbidden : try to undeploy a global used by a process : must fail
    getManagementAPI().deployJar("undep.jar", Misc.generateJar(IncrementHook.class, FailingHook.class));
    final URL xpdlUrl = this.getClass().getResource("manyHooks.xpdl");
    final ProcessDefinition process = getManagementAPI().deploy(getBusinessArchiveFromXpdl(xpdlUrl,
        FailingHook.class, IncoherentHook.class));
    //this undeploy must be OK as the process defines its own FailingHook
    getManagementAPI().removeJar("undep.jar");

    //here undeploy the referenced process
    getManagementAPI().disable(process.getUUID());
    getManagementAPI().deleteProcess(process.getUUID());

    //check impossible to undeploy an unexisting class...
    try {
      getManagementAPI().removeJar("undep.jar");
      fail("This test must have failed as it is impossible to remove an unexisting class...");
    } catch (final DeploymentException e) {
      //assertTrue(causeContains("There is no class with name " + IncrementHook.class.getName()
      //    + " defined in global class repository.", e));
      assertEquals("Bonita Error: bai_MAPII_6\nThere is no class defined in global class repository with name: \n" + e.getClassName(), e.getMessage());

    } 
  }

  public void testRedeploy() throws BonitaException, IOException {
    //undeploy a class from global, deploy a new one with the same name and check the new one is used

    getManagementAPI().deployJar("redep.jar", Misc.generateJar(IncrementHook.class));
    getManagementAPI().removeJar("redep.jar");
    assertEquals(1, getManagementAPI().getAvailableJars().size());
    getManagementAPI().deployJar("redep.jar", Misc.getAllContentFrom(this.getClass().getResourceAsStream("inc.zip")));

    final URL xpdlUrl = this.getClass().getResource("manyHooks.xpdl");
    final ProcessDefinition process = getManagementAPI().deploy(getBusinessArchiveFromXpdl(xpdlUrl,
        FailingHook.class, IncoherentHook.class));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);
    checkExecutedOnce(instanceUUID, new String[]{"uniqueActivity"});
    final String counter = (String) getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "counter");
    assertEquals("6", counter);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
    getManagementAPI().removeJar("redep.jar");
  }

}
