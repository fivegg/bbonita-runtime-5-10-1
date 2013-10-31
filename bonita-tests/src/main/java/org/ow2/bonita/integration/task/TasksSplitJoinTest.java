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

import org.ow2.bonita.APITestCase;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.util.BonitaException;

/**
 * Test process definition including task and execution of it
 *
 */
public class TasksSplitJoinTest extends APITestCase {

  // 1st test : with XOR Join
  // ////////////////////////////////////////////////////////////
  // Join is completed if one of the task (notif_grant or notif_reject) is
  // terminated ////////////
  public void testTasksSplitXorJoin() throws BonitaException {
    URL xpdlUrl = this.getClass().getResource("test_tasks_split_Xorjoin.xpdl");
    ProcessDefinition process = getManagementAPI().deploy(getBusinessArchiveFromXpdl(xpdlUrl));
    
    ProcessDefinitionUUID processUUID = process.getUUID();
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    checkStopped(instanceUUID, new String[]{});

    // start & terminate "request" task
    executeTask(instanceUUID, "request");

    // start & terminate "validation" task
    executeTask(instanceUUID, "validation");

    // start & terminate "notif_grant" task
    executeTask(instanceUUID, "notif_grant");

    // start and terminate "synchro" task
    executeTask(instanceUUID, "synchro");

    checkExecutedOnce(instanceUUID, new String[]{"request", "validation", "notif_grant", "synchro"});

    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  // 2 nd test : AND JOIN /////////////////////////////////////////////////
  // wait for both task completion before completing the join node ////////

  public void testTasksSplitAndJoin() throws BonitaException {
    URL xpdlUrl = this.getClass().getResource("test_tasks_split_Andjoin.xpdl");
    ProcessDefinition process = getManagementAPI().deploy(getBusinessArchiveFromXpdl(xpdlUrl));
    
    ProcessDefinitionUUID processUUID = process.getUUID();
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    checkStopped(instanceUUID, new String[]{});

    // start & terminate "request" task
    executeTask(instanceUUID, "request");

    // start & terminate "validation" task
    executeTask(instanceUUID, "validation");

    // start & terminate "notif_grant" task
    executeTask(instanceUUID, "notif_grant");

    // start & terminate "notif_grant" task
    executeTask(instanceUUID, "notif_reject");

    // start and terminate "synchro" task
    executeTask(instanceUUID, "synchro");


    checkExecutedOnce(instanceUUID, new String[]{"request", "validation", "notif_grant", "notif_reject", "synchro"});

    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }
}
