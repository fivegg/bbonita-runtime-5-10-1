/**
 * Copyright (C) 2007 Bull S. A. S. Bull, Rue Jean Jaures, B.P.68, 78340, Les
 * Clayes-sous-Bois This library is free software; you can redistribute it
 * and/or modify it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation version 2.1 of the License. This
 * library is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details. You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301,
 * USA.
 */
package org.ow2.bonita.variable;

import java.net.URL;
import java.util.Collection;

import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.runtime.ActivityInstance;
import org.ow2.bonita.facade.uuid.ActivityInstanceUUID;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.util.BonitaException;

/**
 * Testing activity variables (string and enumeration types)
 * by using StartMode=Manual (task behavior)
 * taking in account :
 *     propagation parameter,
 *     split, join for execution
 * Variables are defined either into WFProcess or Package
 */
public class ActivityVariableWithSplitJoinTest extends VariableTestCase {

  // check that activity variables (string and enumeration) defined
  // into  WFProcess are well managed (including propagation)
  //      -  when spliting the initial execution
  //      -  when joining is performed
  public void testActivityWFPVariableswithSplitJoin() throws BonitaException {
    URL xpdlUrl = ActivityVariableTest.class.getResource("varActivityIntoWFPWithSplitJoin.xpdl");

    ProcessDefinition process = getManagementAPI().deploy(getBusinessArchiveFromXpdl(xpdlUrl));
    
    ProcessDefinitionUUID processUUID = process.getUUID();

    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    // Check variables within execution pointing to act1 node
    Collection<ActivityInstance> acts = getQueryRuntimeAPI().getActivityInstances(instanceUUID, "act1");
    assertEquals(1, acts.size());
    ActivityInstance activityInst = acts.iterator().next();
    assertNotNull(activityInst);
    ActivityInstanceUUID activityUUID = activityInst.getUUID();
    
    assertTrue(getQueryRuntimeAPI().getActivityInstanceVariables(activityUUID).isEmpty());
    checkStopped(instanceUUID, new String[]{});

    // start & terminate "act1" task
    executeTask(instanceUUID, "act1");
    checkStopped(instanceUUID, new String[]{"act1"});

    //get the child execution pointing on act2 node
    acts = getQueryRuntimeAPI().getActivityInstances(instanceUUID, "act2");
    assertEquals(1, acts.size());
    activityInst = acts.iterator().next();
    assertNotNull(activityInst);
    activityUUID = activityInst.getUUID();

    // Check variables within execution pointing to act2 node
    checkVariables(getQueryRuntimeAPI(), activityUUID, 7);

    // start & terminate "act2" task
    executeTask(instanceUUID, "act2");
    checkStopped(instanceUUID, new String[]{"act1", "act2"});

    // Check variables within execution pointing to act3 node
    acts = getQueryRuntimeAPI().getActivityInstances(instanceUUID, "act3");
    assertEquals(1, acts.size());
    activityInst = acts.iterator().next();
    assertNotNull(activityInst);
    activityUUID = activityInst.getUUID();
    checkVariables(getQueryRuntimeAPI(), activityUUID, 7);

    // start & terminate "act3" task
    executeTask(instanceUUID, "act3");
    checkStopped(instanceUUID, new String[]{"act1", "act2", "act3"});

    // Check variables within execution pointing to act4 node
    acts = getQueryRuntimeAPI().getActivityInstances(instanceUUID, "act4");
    assertEquals(1, acts.size());
    activityInst = acts.iterator().next();
    assertNotNull(activityInst);
    activityUUID = activityInst.getUUID();
    checkPropagatedVariables(getQueryRuntimeAPI(), activityUUID, 0);

    // start & terminate "act3" task
    executeTask(instanceUUID, "act4");
    checkStopped(instanceUUID, new String[]{"act1", "act2", "act3", "act4"});

    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

}
