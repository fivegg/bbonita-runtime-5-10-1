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
package org.ow2.bonita.deadline;

import org.bonitasoft.connectors.bonita.SetVarConnector;
import org.ow2.bonita.APITestCase;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.exception.ActivityNotFoundException;
import org.ow2.bonita.facade.exception.DeploymentException;
import org.ow2.bonita.facade.exception.InstanceNotFoundException;
import org.ow2.bonita.facade.exception.ProcessNotFoundException;
import org.ow2.bonita.facade.exception.UndeletableInstanceException;
import org.ow2.bonita.facade.exception.UndeletableProcessException;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.util.BonitaException;
import org.ow2.bonita.util.Misc;
import org.ow2.bonita.util.ProcessBuilder;

/**
 * @author Pascal Verdage
 */
public class DeadlineFunctionalTest extends APITestCase {


  private static final long TIMEOUT = 3 * 60 * 1000; // 3 minutes

  protected long waitForLongVariableToBeSet(final ProcessInstanceUUID instanceUUID,
      final String variable, final long timeout) {
    long result = Long.MAX_VALUE;
    final long rate = 500;
    for (long i = 0; i < timeout / rate; i++) {
      try {
        Thread.sleep(rate);
        final Object object = getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, variable);
        if (object != null && object instanceof String) {
          result = Long.parseLong((String) object);
          if (result != 0) {
            break;
          }
        }
      } catch (final Exception e) {
        fail(Misc.getStackTraceFrom(e));
      }
    }
    return result;
  }

  protected long instantiateAndWaitForLongVariableToBeSet(final ProcessDefinition process,
      final String variable) throws ProcessNotFoundException,
      DeploymentException, UndeletableProcessException, UndeletableInstanceException, InstanceNotFoundException, ActivityNotFoundException {
    return instantiateAndWaitForLongVariableToBeSet(process, variable, TIMEOUT);
  }

  protected long instantiateAndWaitForLongVariableToBeSet(final ProcessDefinition process,
      final String variable, final long timeout) throws ProcessNotFoundException,
      DeploymentException, UndeletableProcessException, UndeletableInstanceException, InstanceNotFoundException, ActivityNotFoundException {
    final ProcessDefinitionUUID processUUID = process.getUUID();
    long time;
    ProcessInstanceUUID instanceUUID = null; 
    try {
      instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);
      time = waitForLongVariableToBeSet(instanceUUID, variable, timeout);
    } finally {
      getRuntimeAPI().deleteAllProcessInstances(processUUID);
      getManagementAPI().disable(processUUID);
      getManagementAPI().deleteProcess(processUUID);
    }
    return time;
  }

  //test the execution of a transactional hook after a fixed duration
  public void testTxHookAfterDuration() throws BonitaException {
    final Class< ? > hook = SetTimeTxHook.class;

    ProcessDefinition clientProcess = ProcessBuilder.createProcess("main", "1.0")
    .addStringData("time", "0")
    .addHuman("admin")
    .addHumanTask("a", "admin")
    .addDeadline("1000", hook.getName())
    .done();

    final ProcessDefinition process = 
      getManagementAPI().deploy(getBusinessArchive(clientProcess, null, hook));
    final long startTime = (System.currentTimeMillis() / 1000) * 1000;
    final long time = instantiateAndWaitForLongVariableToBeSet(process, "time");

    assertTrue("execution duration: " + (time - startTime), time - startTime >= 1000);
  }

  //test the execution of a non-transactional hook after a fixed duration
  public void testHookAfterDuration() throws BonitaException {
    final Class< ? > hook = SetTimeHook.class;

    ProcessDefinition clientProcess = ProcessBuilder.createProcess("main", "1.0")
    .addStringData("time", "0")
    .addHuman("admin")
    .addHumanTask("a", "admin")
    .addDeadline("1000", hook.getName())
    .done();

    final ProcessDefinition process = getManagementAPI().deploy(getBusinessArchive(clientProcess, null, hook));
    final long startTime = (System.currentTimeMillis() / 1000) * 1000;
    final long time = instantiateAndWaitForLongVariableToBeSet(process, "time");

    assertTrue("execution duration: " + (time - startTime), time - startTime >= 1000);
  }

  //test the execution of a transactional hook at a fixed date
  public void testTxHookAtDate() throws BonitaException {
    final long startTime = (System.currentTimeMillis() / 1000) * 1000;
    final Class< ? > hook = SetTimeTxHook.class;

    ProcessDefinition clientProcess = ProcessBuilder.createProcess("main", "1.0")
    .addStringData("time", "0")
    .addHuman("admin")
    .addHumanTask("a", "admin")
    .addDeadline(DeadlineDocument.createDeadlineDateFromDelay(1000), hook.getName())
    .done();


    final ProcessDefinition process = 
      getManagementAPI().deploy(getBusinessArchive(clientProcess, null, hook));
    final long time = instantiateAndWaitForLongVariableToBeSet(process, "time");

    assertTrue("execution duration: " + (time - startTime), time - startTime >= 1000);
  }

  public void testGroovyLongDeadline() throws Exception {
    final long time = 1000;
    
    ProcessDefinition process = ProcessBuilder.createProcess("main", "1.0")
    .addLongData("time", time)
    .addStringData("result", "initial")
    .addHuman("admin")
    .addHumanTask("a", "admin")
    .addDeadline("${new Long(time)}", SetVarConnector.class.getName())
      .addInputParameter("variableName", "result")
      .addInputParameter("value", "changed")
    .done();

    process = getManagementAPI().deploy(getBusinessArchive(process, null, SetVarConnector.class));
    final ProcessDefinitionUUID processUUID = process.getUUID();
    
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);
    assertEquals("initial", getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "result"));
    Thread.sleep(2000);
    assertEquals("changed", getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "result"));
    
    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().deleteProcess(processUUID);
  }
  
  public void testDynamicDeadline() throws BonitaException {
    final long startTime = (System.currentTimeMillis() / 1000) * 1000;
    final Class< ? > hook = SetTimeHook.class;

    ProcessDefinition clientProcess = ProcessBuilder.createProcess("main", "1.0")
    .addStringData("time")
    .addHuman("admin")
    .addHumanTask("a", "admin")
    .addDeadline("${return new java.text.SimpleDateFormat(\"yyyy/MM/dd/HH/mm/ss/SSS\").format(new java.util.Date(System.currentTimeMillis() + 1000))}", hook.getName())
    .done();

    final ProcessDefinition process = getManagementAPI().deploy(getBusinessArchive(clientProcess, null, hook));
    final long time = instantiateAndWaitForLongVariableToBeSet(process, "time");

    assertTrue("execution duration: " + (time - startTime), time - startTime >= 1000);
  }

  //test the execution of a non-transactional hook at a fixed date
  public void testHookAtDate() throws BonitaException {
    final long startTime = (System.currentTimeMillis() / 1000) * 1000;
    final Class< ? > hook = SetTimeHook.class;

    ProcessDefinition clientProcess = ProcessBuilder.createProcess("main", "1.0")
    .addStringData("time")
    .addHuman("admin")
    .addHumanTask("a", "admin")
    .addDeadline(DeadlineDocument.createDeadlineDateFromDelay(1000), hook.getName())
    .done();

    final ProcessDefinition process = getManagementAPI().deploy(getBusinessArchive(clientProcess, null, hook));
    final long time = instantiateAndWaitForLongVariableToBeSet(process, "time");

    assertTrue("execution duration: " + (time - startTime), time - startTime >= 1000);
  }

  //test the cancellation of a deadline when the instance is deleted
  public void testDeadlinesCanceledByDeleteInstance() throws BonitaException {
    ProcessDefinition clientProcess = ProcessBuilder.createProcess("main", "1.0")
    .addStringData("time", "0")
    .addHuman("admin")
    .addHumanTask("a", "admin")
    .addDeadline("1000", NullHook.class.getName())
    .addDeadline("10000", SetTimeTxHook.class.getName())
    .addDeadline("10000", SetTimeHook.class.getName())
    .done();

    final ProcessDefinition process = getManagementAPI().deploy(
        getBusinessArchive(clientProcess, null, NullHook.class, SetTimeHook.class, SetTimeTxHook.class));
    final long time = instantiateAndWaitForLongVariableToBeSet(process, "time", 2000);
    assertEquals(0, time);
  }

  //test the cancellation of a deadline when the activity is executed
  public void testDeadlinesCanceledByActivity() throws BonitaException {
    final Class< ? > hook = SetTimeHook.class;

    ProcessDefinition clientProcess = ProcessBuilder.createProcess("main", "1.0")
    .addStringData("time", "0")
    .addHuman("admin")
    .addHumanTask("a", "admin")
    .addDeadline("10000", hook.getName())
    .addDeadline("10000", hook.getName())
    .addHumanTask("b", "admin")
    .addTransition("a_b", "a", "b")
    .done();

    final ProcessDefinition process = getManagementAPI().deploy(
        getBusinessArchive(clientProcess, null, hook));
    final ProcessDefinitionUUID processUUID = process.getUUID();
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    long time = Long.MAX_VALUE;
    try {
      checkStopped(instanceUUID, new String[]{});
      executeTask(instanceUUID, "a");
      time = waitForLongVariableToBeSet(instanceUUID, "time", 12000);
    } finally {
      getRuntimeAPI().deleteAllProcessInstances(processUUID);
      getManagementAPI().disable(processUUID);
      getManagementAPI().deleteProcess(processUUID);
    }
    assertEquals(0, time);
  }

}
