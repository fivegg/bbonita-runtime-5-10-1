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
package org.ow2.bonita.example;

import java.io.IOException;
import java.net.URL;

import org.ow2.bonita.APITestCase;
import org.ow2.bonita.example.carpool.Carpool;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.runtime.ProcessInstance;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.util.BonitaException;
import org.ow2.bonita.util.ProcessBuilder;

public class CarpoolTest extends APITestCase {

  public void testCarpoolCancelRequest() throws BonitaException, IOException, ClassNotFoundException {

    ProcessInstance processInstance = instantiate(Carpool.CANCEL_REQUEST);

    checkNotExecuted(processInstance, new String[]{"CancelPlace", "Association"});
    checkExecutedOnce(processInstance,
        new String[]{"Initial", "InitRequest", "CancelRequest", "BonitaEnd"});

    checkExecutedManyTimes(processInstance, new String[]{"WaitRequest"}, 3);
    checkExecutedManyTimes(processInstance, new String[]{"WaitRequestDL"}, 2);
    checkExecutedManyTimes(processInstance, new String[]{"WaitAnswer"}, 2);
    checkExecutedManyTimes(processInstance, new String[]{"WaitAnswerDL"}, 1);

    Carpool.cleanProcess(processInstance.getUUID());
  }

  public void testCarpoolCancelPlaceImmediately() throws BonitaException, IOException, ClassNotFoundException {
    ProcessInstance processInstance = instantiate(Carpool.CANCEL_PLACE_IMMEDIATELY);

    checkNotExecuted(processInstance,
        new String[]{"WaitRequestDL", "InitRequest", "WaitAnswer", "Association", "CancelRequest", "WaitAnswerDL"});
    checkExecutedOnce(processInstance, new String[]{"Initial", "WaitRequest", "CancelPlace", "BonitaEnd"});

    Carpool.cleanProcess(processInstance.getUUID());
  }

  public void testCarpoolCancelPlaceAfter3Wait() throws BonitaException, IOException, ClassNotFoundException {
    ProcessInstance processInstance = instantiate(Carpool.CANCEL_PLACE_AFTER_3_WAIT);

    checkNotExecuted(processInstance,
        new String[]{"InitRequest", "WaitAnswer", "Association", "CancelRequest", "WaitAnswerDL"});
    checkExecutedOnce(processInstance, new String[]{"Initial", "CancelPlace", "BonitaEnd"});
    checkExecutedManyTimes(processInstance, new String[]{"WaitRequest"}, 4);
    checkExecutedManyTimes(processInstance, new String[]{"WaitRequestDL"}, 3);
    
    Carpool.cleanProcess(processInstance.getUUID());
  }

  public void testCarpoolAssociation() throws BonitaException, IOException, ClassNotFoundException {
    ProcessInstance processInstance = instantiate(Carpool.ASSOCIATION);

    checkNotExecuted(processInstance,
        new String[]{"CancelRequest", "WaitRequestDL", "CancelPlace", "WaitAnswerDL"});
    checkExecutedOnce(processInstance,
        new String[]{"Initial", "WaitRequest", "InitRequest", "WaitAnswer", "BonitaEnd"});

    Carpool.cleanProcess(processInstance.getUUID());
  }

  protected ProcessInstance instantiate(int mode) throws BonitaException, IOException, ClassNotFoundException {
    URL xpdlUrl = Carpool.class.getResource("carpool.xpdl");
    ProcessDefinition clientProcess = ProcessBuilder.createProcessFromXpdlFile(xpdlUrl);

    ProcessInstanceUUID instanceUUID = Carpool.execute(clientProcess, mode);

    return getQueryRuntimeAPI().getProcessInstance(instanceUUID);
  }

}
