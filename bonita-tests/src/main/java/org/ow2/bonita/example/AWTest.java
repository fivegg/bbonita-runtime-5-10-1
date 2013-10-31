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
package org.ow2.bonita.example;

import java.io.IOException;
import java.net.URL;

import org.ow2.bonita.APITestCase;
import org.ow2.bonita.example.aw.ApprovalWorkflow;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition.ProcessState;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.util.BonitaException;
import org.ow2.bonita.util.ProcessBuilder;

/**
 * Testing ApprovalWorkflow example
 */
public class AWTest extends APITestCase {

  public void testAW1() throws BonitaException, IOException, ClassNotFoundException {
    execute();
  }

  public void testAW2() throws BonitaException, IOException, ClassNotFoundException {
    execute();
  }

  public void testAWTwice() throws BonitaException, IOException, ClassNotFoundException {
    execute();
    execute();
  }

  protected void execute() throws BonitaException, IOException, ClassNotFoundException {
    URL xpdlUrl = ApprovalWorkflow.class.getResource("ApprovalWorkflow.xpdl");
    ProcessDefinition clientProcess = ProcessBuilder.createProcessFromXpdlFile(xpdlUrl);
    ProcessInstanceUUID instanceUUID = ApprovalWorkflow.execute(clientProcess, true);
    ApprovalWorkflow.cleanProcess(instanceUUID);
    
    assertEquals(0, getQueryDefinitionAPI().getProcesses(ApprovalWorkflow.PROCESS_ID, ProcessState.ENABLED).size());  
  }
  protected String getLogin() {
    return "john";
  }
  protected String getPassword() {
    return "bpm";
  }
}
