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
package org.ow2.bonita.integration.routeSplitJoin;

import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.ow2.bonita.APITestCase;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.hook.DefaultTestHook;
import org.ow2.bonita.integration.task.AdminRoleMapper;
import org.ow2.bonita.util.BonitaException;

/**
 * Testing process starting with Split (either Automatic, task or route)
 */
public class SplitStartingNodeTest extends APITestCase {

  public void testRejectWithAuto() throws BonitaException {
    URL xpdlUrl = this.getClass().getResource("splitAutomaticStartingNode.xpdl");
    ProcessDefinition process = getManagementAPI().deploy(getBusinessArchiveFromXpdl(xpdlUrl,
        DefaultTestHook.class, MailAccept.class, MailReject.class));
    
    ProcessDefinitionUUID processUUID = process.getUUID();
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    // check mailReject hook has been executed
    String statusMail = (String) getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "StatusMail");
    assertEquals("mailReject sent", statusMail);
    checkExecutedOnce(instanceUUID, new String[]{"Approval", "Rejection", "BonitaEnd"});

    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testAcceptWithAuto() throws BonitaException {
    URL xpdlUrl = this.getClass().getResource("splitAutomaticStartingNode.xpdl");
    ProcessDefinition process = getManagementAPI().deploy(getBusinessArchiveFromXpdl(xpdlUrl,
        DefaultTestHook.class, MailAccept.class, MailReject.class));
    
    ProcessDefinitionUUID processUUID = process.getUUID();

    Map<String, Object> variables = new HashMap<String, Object>();
    Set<String> possibleValues = new HashSet<String>();
    possibleValues.add("grant");
    possibleValues.add("rejected");
    variables.put("approval_decision", "grant");

    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID, variables, null);
    checkExecutedOnce(instanceUUID, new String[]{"Approval", "Acceptance", "BonitaEnd"});

    // check mailAccept hook has been executed
    String statusMail = (String) getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "StatusMail");
    assertEquals("mailAccept sent", statusMail);

    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testRejectWithTask() throws BonitaException {
    URL xpdlUrl = this.getClass().getResource("splitTaskStartingNode.xpdl");
    ProcessDefinition process = getManagementAPI().deploy(getBusinessArchiveFromXpdl(xpdlUrl,
        DefaultTestHook.class, MailAccept.class, MailReject.class, AdminRoleMapper.class));
    
    ProcessDefinitionUUID processUUID = process.getUUID();
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    checkStopped(instanceUUID, new String[]{});
    executeTask(instanceUUID, "Approval");
    checkExecutedOnce(instanceUUID, new String[]{"Approval", "Rejection", "BonitaEnd"});

    // check mailReject hook has been executed
    String statusMail = (String) getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "StatusMail");
    assertEquals("mailReject sent", statusMail);

    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testAcceptWithTask() throws BonitaException {
    URL xpdlUrl = this.getClass().getResource("splitTaskStartingNode.xpdl");
    ProcessDefinition process = getManagementAPI().deploy(getBusinessArchiveFromXpdl(xpdlUrl,
        DefaultTestHook.class, MailAccept.class, MailReject.class, AdminRoleMapper.class));
    
    ProcessDefinitionUUID processUUID = process.getUUID();

    Map<String, Object> variables = new HashMap<String, Object>();
    Set<String> possibleValues = new HashSet<String>();
    possibleValues.add("grant");
    possibleValues.add("rejected");
    variables.put("approval_decision", "grant");

    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID, variables, null);

    checkStopped(instanceUUID, new String[]{});

    executeTask(instanceUUID, "Approval");

    checkExecutedOnce(instanceUUID, new String[]{"Approval", "Acceptance", "BonitaEnd"});

    String statusMail = (String) getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "StatusMail");
    assertEquals("mailAccept sent", statusMail);

    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testRejectWithRoute() throws BonitaException {
    URL xpdlUrl = this.getClass().getResource("splitRouteStartingNode.xpdl");
    ProcessDefinition process = getManagementAPI().deploy(getBusinessArchiveFromXpdl(xpdlUrl,
        DefaultTestHook.class, MailAccept.class, MailReject.class));
    
    ProcessDefinitionUUID processUUID = process.getUUID();
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    checkExecutedOnce(instanceUUID, new String[]{"Approval", "Rejection", "BonitaEnd"});

    // check mailReject hook has been executed
    String statusMail = (String) getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "StatusMail");

    assertEquals("mailReject sent", statusMail);

    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testAcceptWithRoute() throws BonitaException {
    URL xpdlUrl = this.getClass().getResource("splitRouteStartingNode.xpdl");
    ProcessDefinition process = getManagementAPI().deploy(getBusinessArchiveFromXpdl(xpdlUrl,
        DefaultTestHook.class, MailAccept.class, MailReject.class));
    
    ProcessDefinitionUUID processUUID = process.getUUID();

    Map<String, Object> variables = new HashMap<String, Object>();
    Set<String> possibleValues = new HashSet<String>();
    possibleValues.add("grant");
    possibleValues.add("rejected");
    variables.put("approval_decision", "grant");

    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID, variables, null);
    checkExecutedOnce(instanceUUID, new String[]{"Approval", "Acceptance", "BonitaEnd"});

    String statusMail = (String) getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "StatusMail");
    assertEquals("mailAccept sent", statusMail);

    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testProcessStartingWithRouteNoSplit() throws BonitaException {
    URL xpdlUrl = this.getClass().getResource("splitRoute1TrStartingNode.xpdl");
    ProcessDefinition process = getManagementAPI().deploy(getBusinessArchiveFromXpdl(xpdlUrl,
        DefaultTestHook.class));
    
    ProcessDefinitionUUID processUUID = process.getUUID();

    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);
    checkExecutedOnce(instanceUUID, new String[]{"r1", "a", "BonitaEnd"});
    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

}
