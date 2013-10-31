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
package org.ow2.bonita.integration.task;

import java.net.URL;
import java.util.Collection;
import java.util.HashSet;

import org.ow2.bonita.APITestCase;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.runtime.ActivityInstance;
import org.ow2.bonita.facade.uuid.ActivityInstanceUUID;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.hook.DefaultTestHook;
import org.ow2.bonita.util.BonitaException;

/**
 * Testing task functionalities: - human participant - role participant +
 * performer assign - role participant + role mapper + performer assign -
 * variables
 *
 */
public class TaskTest extends APITestCase {

  // 1 st test with role mapper and perf. assign
  public void testTaskWithRoleMapperPerfAssign() throws BonitaException {
    URL xpdlUrl = this.getClass().getResource("testPerformerMapperRole.xpdl");
    ProcessDefinition process = getManagementAPI().deploy(getBusinessArchiveFromXpdl(xpdlUrl,
        DefaultTestHook.class, AdminPerformerAssign.class, AdminRoleMapper.class));
    
    ProcessDefinitionUUID processUUID = process.getUUID();

    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);
    checkStopped(instanceUUID, new String[]{"initial"});

    // start & terminate "myTask" task
    executeTask(instanceUUID, "myTask");
    checkExecutedOnce(instanceUUID, new String[]{"initial", "myTask", "end"});
    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  // 2 nd test : without performer assign & without role mapper
  public void testTaskWithOutPerformerRoleMapper() throws BonitaException {
    URL xpdlUrl = this.getClass().getResource("testTaskWithOutPerformerMapperRole.xpdl");
    ProcessDefinition process = getManagementAPI().deploy(getBusinessArchiveFromXpdl(xpdlUrl,
        DefaultTestHook.class));
    
    ProcessDefinitionUUID processUUID = process.getUUID();

    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    checkStopped(instanceUUID, new String[]{"initial"});

    // start & terminate "myTask" task
    executeTask(instanceUUID, "myTask");
    checkExecutedOnce(instanceUUID, new String[]{"initial", "myTask", "end"});

    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  // 3 rd test : with human and performer assignment
  public void testTaskWithHumanAndPerfAssign() throws BonitaException {
    URL xpdlUrl = this.getClass().getResource("testTaskWithHumanAndPerfAssign.xpdl");
    ProcessDefinition process = getManagementAPI().deploy(getBusinessArchiveFromXpdl(xpdlUrl,
        DefaultTestHook.class, AdminPerformerAssign.class));
    
    ProcessDefinitionUUID processUUID = process.getUUID();

    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    checkStopped(instanceUUID, new String[]{"initial"});

    // start & terminate "myTask" task
    executeTask(instanceUUID, "myTask");
    checkExecutedOnce(instanceUUID, new String[]{"initial", "myTask", "end"});

    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  // 4rd test : test variable persistence within task
  // - get variables from initial definition
  // - modify variables
  // - check modified variables

  public void testTaskVariables() throws BonitaException {
    URL xpdlUrl = this.getClass().getResource("testTaskVariables.xpdl");
    ProcessDefinition process = getManagementAPI().deploy(getBusinessArchiveFromXpdl(xpdlUrl));
    
    ProcessDefinitionUUID processUUID = process.getUUID();

    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    checkStopped(instanceUUID, new String[]{"initial"});

    Collection<ActivityInstance> acts = getQueryRuntimeAPI().getActivityInstances(instanceUUID, "myTask");
    assertEquals(1, acts.size());
    ActivityInstanceUUID actUUID = acts.iterator().next().getUUID();
    String actStr1 = (String) getQueryRuntimeAPI().getActivityInstanceVariable(actUUID, "act_str1");

    assertNotNull(actStr1);
    assertEquals("initial value", actStr1);

    String actEnumStat = (String) getQueryRuntimeAPI().getActivityInstanceVariable(actUUID, "act_enum_stat");

    assertNotNull(actEnumStat);
    assertEquals("v1", actEnumStat);

    // modify variables //////////////
    // string type
    getRuntimeAPI().setActivityInstanceVariable(actUUID, "act_str1", "modified value");


    // enumeration type
    HashSet<String> ar = new HashSet<String>();
    ar.add("v3");
    ar.add("v4");
    getRuntimeAPI().setActivityInstanceVariable(actUUID, "act_enum_stat", "v3");

    // check modification of the variables
    actStr1 = (String) getQueryRuntimeAPI().getActivityInstanceVariable(actUUID, "act_str1");
    assertNotNull(actStr1);
    assertEquals("modified value", actStr1);

    actEnumStat = (String) getQueryRuntimeAPI().getActivityInstanceVariable(actUUID, "act_enum_stat");
    assertNotNull(actEnumStat);
    assertEquals("v3", actEnumStat);

    // start & terminate "myTask" task
    executeTask(instanceUUID, "myTask");

    checkExecutedOnce(instanceUUID, new String[]{"initial", "myTask", "end"});

    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  // 5rd testing with role mapper and without performer assignment ////////////
  public void testTaskWithRoleMapper() throws BonitaException {
    URL xpdlUrl = this.getClass().getResource("testTaskRoleMapper.xpdl");
    ProcessDefinition process = getManagementAPI().deploy(getBusinessArchiveFromXpdl(xpdlUrl,
        DefaultTestHook.class, AdminRoleMapper.class));
    
    ProcessDefinitionUUID processUUID = process.getUUID();

    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    checkStopped(instanceUUID, new String[]{"initial"});

    // start & terminate "myTask" task
    executeTask(instanceUUID, "myTask");
    checkExecutedOnce(instanceUUID, new String[]{"initial", "myTask", "end"});

    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

}
