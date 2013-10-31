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
package org.ow2.bonita.command;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.ow2.bonita.APITestCase;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.runtime.ActivityState;
import org.ow2.bonita.facade.runtime.InstanceState;
import org.ow2.bonita.facade.runtime.command.WebExecuteTask;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.light.LightActivityInstance;
import org.ow2.bonita.light.LightProcessInstance;
import org.ow2.bonita.util.GroovyException;
import org.ow2.bonita.util.ProcessBuilder;

/**
 * @author Elias Ricken de Medeiros
 * 
 */
public class WebExecuteTaskCommandTest extends APITestCase {

  public void testExecueTask() throws Exception {
    final String variableName = "strVar";
    ProcessDefinition processDefinition = ProcessBuilder.createProcess("webProcess", "1.0").addHuman(getLogin()).addStringData(variableName, "initial")
        .addHumanTask("step1", getLogin()).done();

    processDefinition = getManagementAPI().deploy(getBusinessArchive(processDefinition));
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processDefinition.getUUID());
    LightActivityInstance activityInstance = getActivityInstance(instanceUUID, "step1");

    final StringBuilder stb = new StringBuilder();
    stb.append("${");
    stb.append("System.out.println(\"\\n\\n******************** Groovy executed!!!! ****************************************\\n\\n\");");
    stb.append("}");

    final Map<String, Object> processVariables = new HashMap<String, Object>(1);
    processVariables.put(variableName, "updated");
      final WebExecuteTask command = new WebExecuteTask(activityInstance.getUUID(), Collections.<String, Object> singletonMap(variableName, "updated"), null, null, null,
          Collections.singletonList(stb.toString()), null);
      getCommandAPI().execute(command);

    final String strVar = (String) getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, variableName);
    assertEquals("updated", strVar);
    
    activityInstance = getActivityInstance(instanceUUID, "step1");
    assertEquals(ActivityState.FINISHED, activityInstance.getState());
    
    final LightProcessInstance processInstance = getQueryRuntimeAPI().getLightProcessInstance(instanceUUID);
    assertEquals(InstanceState.FINISHED, processInstance.getInstanceState());

    getManagementAPI().deleteProcess(processDefinition.getUUID());

  }

  public void testActivityGoesToFailedStateOnScriptException() throws Exception {
    final String variableName = "strVar";
    ProcessDefinition processDefinition = ProcessBuilder.createProcess("webProcess", "1.0").addHuman(getLogin()).addStringData(variableName, "initial")
        .addHumanTask("step1", getLogin()).done();
    
    processDefinition = getManagementAPI().deploy(getBusinessArchive(processDefinition));
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processDefinition.getUUID());
    LightActivityInstance activityInstance = getActivityInstance(instanceUUID, "step1");
    
    final StringBuilder stb = new StringBuilder();
    stb.append("${");
    stb.append("System.out.println(\"\\n\\n******************** Groovy executed!!!! ****************************************\\n\\n\");");
    stb.append("throw new RuntimeException(\"Invalid script!\");");
    stb.append("}");
    
    final Map<String, Object> processVariables = new HashMap<String, Object>(1);
    processVariables.put(variableName, "updated");
    try {
      final WebExecuteTask command = new WebExecuteTask(activityInstance.getUUID(), Collections.<String, Object> singletonMap(variableName, "updated"), null, null, null,
          Collections.singletonList(stb.toString()), null);
      getCommandAPI().execute(command);
      fail("Exception expected");
    } catch (final GroovyException e) {
      //OK
    }
    
    final String strVar = (String) getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, variableName);
    assertEquals("updated", strVar);
    
    activityInstance = getActivityInstance(instanceUUID, "step1");
    assertEquals(ActivityState.FAILED, activityInstance.getState());
    
    final LightProcessInstance processInstance = getQueryRuntimeAPI().getLightProcessInstance(instanceUUID);
    assertEquals(InstanceState.STARTED, processInstance.getInstanceState());
    
    getManagementAPI().deleteProcess(processDefinition.getUUID());
    
  }

}
