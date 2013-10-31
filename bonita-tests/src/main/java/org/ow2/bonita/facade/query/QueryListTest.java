/**
 * Copyright (C) 2009  BonitaSoft S.A.
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
package org.ow2.bonita.facade.query;

import java.util.Set;

import org.ow2.bonita.APITestCase;
import org.ow2.bonita.facade.ManagementAPI;
import org.ow2.bonita.facade.QueryDefinitionAPI;
import org.ow2.bonita.facade.QueryRuntimeAPI;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.exception.InstanceNotFoundException;
import org.ow2.bonita.facade.exception.ProcessNotFoundException;
import org.ow2.bonita.facade.runtime.TaskInstance;
import org.ow2.bonita.facade.uuid.ActivityInstanceUUID;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.util.AccessorUtil;
import org.ow2.bonita.util.ProcessBuilder;

/**
 * @author Charles Souillard, Matthieu Chaffotte
 */
public class QueryListTest extends APITestCase {

	public void testQueryDefinitionAPI() throws Exception {
    ProcessDefinition process = ProcessBuilder.createProcess("queryDefinitionProcess", "1.0")
    .addHuman(getLogin())
    .addHumanTask("t", getLogin())
    .done();

    final ManagementAPI managementAPI = AccessorUtil.getManagementAPI();
    final QueryDefinitionAPI journalQueryDefinitionAPI = AccessorUtil.getQueryDefinitionAPI("journalQueryList");
    final QueryDefinitionAPI historyQueryDefinitionAPI = AccessorUtil.getQueryDefinitionAPI("historyQueryList");
    final QueryDefinitionAPI defaultQueryDefinitionAPI = AccessorUtil.getQueryDefinitionAPI();
    
    process = managementAPI.deploy(getBusinessArchive(process));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    journalQueryDefinitionAPI.getProcess(processUUID);
    defaultQueryDefinitionAPI.getProcess(processUUID);
    try {
      historyQueryDefinitionAPI.getProcess(processUUID);
      fail("Process must not be found in history");
    } catch (ProcessNotFoundException e) { }
    
    getManagementAPI().disable(processUUID);
    getManagementAPI().archive(processUUID);
    
    historyQueryDefinitionAPI.getProcess(processUUID);
    defaultQueryDefinitionAPI.getProcess(processUUID);
    try {
      journalQueryDefinitionAPI.getProcess(processUUID);
      fail("Process must not be found in journal");
    } catch (ProcessNotFoundException e) { }
    
    getManagementAPI().deleteProcess(processUUID);
  }
	
	public void testQueryRuntimeAPI() throws Exception {
    ProcessDefinition process = ProcessBuilder.createProcess("queryRuntimeProcess", "1.0")
    .addHuman(getLogin())
    .addHumanTask("t", getLogin())
    .done();

    final ManagementAPI managementAPI = AccessorUtil.getManagementAPI();
    final QueryRuntimeAPI journalQueryRuntimeAPI = AccessorUtil.getQueryRuntimeAPI("journalQueryList");
    final QueryRuntimeAPI historyQueryRuntimeAPI = AccessorUtil.getQueryRuntimeAPI("historyQueryList");
    final QueryRuntimeAPI defaultQueryRuntimeAPI = AccessorUtil.getQueryRuntimeAPI();
    
    process = managementAPI.deploy(getBusinessArchive(process));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);
    
    journalQueryRuntimeAPI.getProcessInstance(instanceUUID);
    defaultQueryRuntimeAPI.getProcessInstance(instanceUUID);
    try {
      historyQueryRuntimeAPI.getProcessInstance(instanceUUID);
      fail("Instance must not be found in history");
    } catch (InstanceNotFoundException e) { } 
    
    final Set<TaskInstance> tasks = getQueryRuntimeAPI().getTasks(instanceUUID);
    assertEquals(1, tasks.size());
    final TaskInstance task = tasks.iterator().next();
    final ActivityInstanceUUID taskUUID = task.getUUID();
    
    assertNotNull(defaultQueryRuntimeAPI.getTask(taskUUID));
    assertNotNull(journalQueryRuntimeAPI.getTask(taskUUID));
    //not found due to OptimizedDbHistory assertNull(historyQueryRuntimeAPI.getTask(taskUUID));
    
    getRuntimeAPI().executeTask(taskUUID, true);
    
    assertNotNull(defaultQueryRuntimeAPI.getTask(taskUUID));
    assertNotNull(historyQueryRuntimeAPI.getTask(taskUUID));
    //not found due to OptimizedDbHistory assertNull(journalQueryRuntimeAPI.getTask(taskUUID));
    
    historyQueryRuntimeAPI.getProcessInstance(instanceUUID);
    defaultQueryRuntimeAPI.getProcessInstance(instanceUUID);
    try {
      journalQueryRuntimeAPI.getProcessInstance(instanceUUID);
      fail("Instance must not be found in journal");
    } catch (InstanceNotFoundException e) { }
    
    getManagementAPI().deleteProcess(processUUID);
  }
  
}
