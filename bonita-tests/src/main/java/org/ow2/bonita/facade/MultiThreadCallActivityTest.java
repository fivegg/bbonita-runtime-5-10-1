/**
 * Copyright (C) 2012  BonitaSoft S.A.
 * BonitaSoft, 31 rue Gustave Eiffel - 38000 Grenoble
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
package org.ow2.bonita.facade;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bonitasoft.connectors.bonita.joincheckers.PercentageJoinChecker;
import org.ow2.bonita.APITestCase;
import org.ow2.bonita.activity.multipleinstances.instantiator.NoContextMulitpleActivitiesInstantiator;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.runtime.ActivityState;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.light.LightTaskInstance;
import org.ow2.bonita.util.ProcessBuilder;

/**
 * @author Elias Ricken de Medeiros
 *
 */
public class MultiThreadCallActivityTest extends APITestCase {
  
  public void testMultiThread() throws Exception {
    
    final String initiatorClassName = NoContextMulitpleActivitiesInstantiator.class.getName();
    final String joinCheckerClassName = PercentageJoinChecker.class.getName();
    final int nbOfInstances = 2;
    ProcessDefinition caller = ProcessBuilder.createProcess("caller", "1.0")
      .addSubProcess("call", "subprocess", "1.0")
      .addMultipleActivitiesInstantiator(initiatorClassName)
      .addInputParameter("number", nbOfInstances)
      .addMultipleActivitiesJoinChecker(joinCheckerClassName)
      .addInputParameter("percentage", 1.0)
      .done();
    
    caller = getManagementAPI().deploy(getBusinessArchive(caller, null, NoContextMulitpleActivitiesInstantiator.class, PercentageJoinChecker.class));
    
    ProcessDefinition subProcess = ProcessBuilder.createProcess("subprocess", "1.0")
      .addHuman(getLogin())
      .addHumanTask("step1", getLogin())
      .addHumanTask("step2", getLogin())
      .addTransition("step1", "step2")
      .done();
    
    subProcess = getManagementAPI().deploy(getBusinessArchive(subProcess, null, NoContextMulitpleActivitiesInstantiator.class, PercentageJoinChecker.class));
    
    final ProcessInstanceUUID callerUUID = getRuntimeAPI().instantiateProcess(caller.getUUID());
    
    final List<ExecuteTaskThread>  myThreads = new ArrayList<ExecuteTaskThread>();
    Collection<LightTaskInstance> taskIs = getQueryRuntimeAPI().getLightTaskList(getLogin(), ActivityState.READY);
    assertEquals(nbOfInstances, taskIs.size());
    
    ExecuteTaskThread cT = null;
    for (final LightTaskInstance ti : taskIs) {
      cT = new ExecuteTaskThread(ti.getUUID());
      myThreads.add(cT);
    }
    for (final ExecuteTaskThread mT : myThreads) {
      mT.start();
    }
    for (final ExecuteTaskThread mT : myThreads) {
      mT.join();
    }

    for (final ExecuteTaskThread mT : myThreads) {
      if (mT.getException() != null) {
        throw mT.getException();
      }
    }
    
    taskIs = getQueryRuntimeAPI().getLightTaskList(getLogin(), ActivityState.READY);
    assertEquals(nbOfInstances, taskIs.size());
    for (final LightTaskInstance lightTaskInstance : taskIs) {
        assertEquals("step2", lightTaskInstance.getActivityName());
    }
    
    getManagementAPI().deleteProcess(caller.getUUID());
    getManagementAPI().deleteProcess(subProcess.getUUID());
  }

}
