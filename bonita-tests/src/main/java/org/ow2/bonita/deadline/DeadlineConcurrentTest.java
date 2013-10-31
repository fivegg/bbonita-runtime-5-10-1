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

import org.ow2.bonita.APITestCase;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.exception.DeploymentException;
import org.ow2.bonita.facade.exception.ProcessNotFoundException;
import org.ow2.bonita.facade.exception.UndeletableInstanceException;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.util.BonitaException;
import org.ow2.bonita.util.Misc;
import org.ow2.bonita.util.ProcessBuilder;

/**
 * @author Pascal Verdage
 */
public class DeadlineConcurrentTest extends APITestCase {

  private static final long TIMEOUT = 3 * 60 * 1000; // 3 minutes

  /**
   * wait for variable 'variable' of the process instance 'instanceUUID'
   * to be equals to value 'expected'.
   * @param instanceUUID: process instance UUID
   * @param variable: process variable name
   * @param expected: expected value for the variable
   * @param timeout: maximum waited time before returning
   * @return the value of the variable when it is equals to expected value
   * or its value after timeout milliseconds.
   */
  private long waitForLongVariableToBeEqual(ProcessInstanceUUID instanceUUID,
      String variable, long expected, long timeout) {
    long result = Long.MAX_VALUE;
    long rate = 500;
    long safeTime = 4;
    for (long i = 0; i < timeout / rate; i++) {
      try {
        Thread.sleep(rate);
        Object object = getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, variable);
        if (object != null && object instanceof String) {
          result = Long.parseLong((String) object);
          if (result >= expected) {
            // Wait for safeTime more iterations to be sure other events won't be executed
            Thread.sleep(safeTime * rate);
            object = getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, variable);
            result = Long.parseLong((String) object);
            return result;
          }
        }
      } catch (Exception e) {
        fail(Misc.getStackTraceFrom(e));
      }
    }
    return result;
  }

  /**
   * Instantiates a process and wait for a variable to be equals to an expected
   * value.
   * @param process: the process to deploy
   * @param variable: name of a process variable
   * @param expected: expected value of the variable
   * @return the value of variable when it is equals to expected value or
   * after a timeout occurred
   * @throws ProcessNotFoundException
   * @throws UndeletableInstanceException 
   */
  private long instantiateAndWaitForLongVariableToBeEqual(ProcessDefinition process,
      String variable, long expected) throws ProcessNotFoundException, DeploymentException, UndeletableInstanceException {
    
    ProcessDefinitionUUID processUUID = process.getUUID();
    long time;
    try {
      ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);
      time = waitForLongVariableToBeEqual(instanceUUID, variable, expected, TIMEOUT);
    } finally {
      getRuntimeAPI().deleteAllProcessInstances(processUUID);
      getManagementAPI().disable(processUUID);
    }
    return time;
  }

  /**
   * multiple deadlines set to fire on different date (using duration),
   * in the same activity
   */
  public void testParallelDeadlines() throws BonitaException {
    int nbDeadlines = Misc.random(5, 20);
    Class< ? > hook = IncrementalTxHook.class;
    String counterVariable = "counter"; // this name is used in IncrementalHook

    ProcessBuilder builder = ProcessBuilder.createProcess("main", "1.0")
    .addStringData(counterVariable, "0")
    .addHuman("admin")
    .addHumanTask("a", "admin");
    
    for (int i = 0; i < nbDeadlines; i++) {
      builder.addDeadline(Integer.toString(1000 * (i + 1)), hook.getName());
    }
    
    ProcessDefinition clientProcess = builder.done();
    
    ProcessDefinition process = getManagementAPI().deploy(getBusinessArchive(clientProcess, null,
        hook));
    long count = instantiateAndWaitForLongVariableToBeEqual(process, counterVariable, nbDeadlines);
    assertEquals(nbDeadlines, count);
    getManagementAPI().deleteProcess(process.getUUID());
  }

  /**
   * multiple deadlines set to fire on the same date (using duration)
   */
  
  public void testSimultaneousDeadlines() throws BonitaException {
    int nbDeadlines = Misc.random(5, 20);
    Class< ? > hook = IncrementalTxHook.class;
    String counterVariable = "counter"; // this name is used in IncrementalHook
    ProcessBuilder builder = ProcessBuilder.createProcess("main", "1.0")
    .addStringData(counterVariable, "0")
    .addHuman("admin")
    .addHumanTask("a", "admin");
    
    for (int i = 0; i < nbDeadlines; i++) {
      builder.addDeadline(Integer.toString(1000 * (i + 1)), hook.getName());
    }
    
    ProcessDefinition clientProcess = builder.done();
    
    ProcessDefinition process = getManagementAPI().deploy(getBusinessArchive(clientProcess, null,
        hook));
    long count = instantiateAndWaitForLongVariableToBeEqual(process, counterVariable, nbDeadlines);
    assertEquals(nbDeadlines, count);
    getManagementAPI().deleteProcess(process.getUUID());
  }
  

}
