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
package org.ow2.bonita.transition;

import java.net.URL;
import java.util.Collection;

import org.ow2.bonita.APITestCase;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.runtime.ActivityInstance;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.util.BonitaException;

/**
 * Testing transition expression with WFProcess variables:
 *    -  either string type
 *    -  TODO or  enumeration type
 *    -  TODO or mixed types
 */
public class TransitionWithWFProcessVarTest extends APITestCase {

  // check simple transition expression
  //     - with String process variable
  //
  public void testTransitionWithStringWFProcessVar() throws BonitaException {
    URL xpdlUrl = TransitionWithWFProcessVarTest.class.getResource("transitionWithStringWFProcessVar.xpdl");
    ProcessDefinition process = getManagementAPI().deploy(getBusinessArchiveFromXpdl(xpdlUrl));
    
    ProcessDefinitionUUID processUUID = process.getUUID();

    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    // Check variables within execution pointing to act1 node
    Collection<ActivityInstance> acts = getQueryRuntimeAPI().getActivityInstances(instanceUUID, "act1");
    assertEquals(1, acts.size());
    ActivityInstance activityInst = acts.iterator().next();
    assertNotNull(activityInst);
    
    assertNotNull(getQueryRuntimeAPI().getActivityInstanceVariables(activityInst.getUUID()));
    assertEquals("no", getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "check"));
    checkStopped(instanceUUID, new String[]{});

    // start & terminate "act1" task
    executeTask(instanceUUID, "act1");
    checkStopped(instanceUUID, new String[]{"act1"});

    //get the child execution pointing on act2 node
    acts = getQueryRuntimeAPI().getActivityInstances(instanceUUID, "act2");
    assertEquals(1, acts.size());
    activityInst = acts.iterator().next();
    assertNotNull(activityInst);

    // start & terminate "act2" task
    executeTask(instanceUUID, "act2");
    checkExecutedOnce(instanceUUID, new String[]{"act1", "act2"});
    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  // check simple transition expression
  //     - with Enumeration Process variable
  //
  public void testTransitionWithEnumerationWFProcessVar() throws BonitaException {
    URL xpdlUrl = TransitionWithWFProcessVarTest.class.getResource("transitionWithEnumerationWFProcessVar.xpdl");
    ProcessDefinition process = getManagementAPI().deploy(getBusinessArchiveFromXpdl(xpdlUrl));
    
    ProcessDefinitionUUID processUUID = process.getUUID();

    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    // Check variables within execution pointing to act1 node
    Collection<ActivityInstance> acts = getQueryRuntimeAPI().getActivityInstances(instanceUUID, "act1");
    assertEquals(1, acts.size());
    ActivityInstance activityInst = acts.iterator().next();
    assertNotNull(activityInst);


    assertNotNull(getQueryRuntimeAPI().getActivityInstanceVariables(activityInst.getUUID()));
    String enumCheck = (String) getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "enumCheck");
    assertNotNull(enumCheck);
    assertEquals("no", enumCheck);

    checkStopped(instanceUUID, new String[]{});

    // start & terminate "act1" task
    executeTask(instanceUUID, "act1");
    checkStopped(instanceUUID, new String[]{"act1"});

    //get the child execution pointing on act2 node
    acts = getQueryRuntimeAPI().getActivityInstances(instanceUUID, "act2");
    assertEquals(1, acts.size());
    activityInst = acts.iterator().next();
    assertNotNull(activityInst);

    // start & terminate "act2" task
    executeTask(instanceUUID, "act2");
    checkExecutedOnce(instanceUUID, new String[]{"act1", "act2"});

    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }
}
