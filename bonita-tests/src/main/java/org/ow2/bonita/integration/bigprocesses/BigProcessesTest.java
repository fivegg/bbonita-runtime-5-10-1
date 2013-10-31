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
package org.ow2.bonita.integration.bigprocesses;

import java.util.HashSet;
import java.util.Set;

import org.bonitasoft.connectors.bonita.SetVarConnector;
import org.ow2.bonita.APITestCase;
import org.ow2.bonita.facade.def.element.HookDefinition.Event;
import org.ow2.bonita.facade.def.majorElement.ActivityDefinition.JoinType;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.runtime.InstanceState;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.util.ProcessBuilder;

/**
 * Test. Deploys a xpdl process.
 *
 * @author Marc Blachon, Charles Souillard
 */
public class BigProcessesTest extends APITestCase {


  private int factoriel(int n) {
    if (n <= 1) {
      return  1;
    } else {
      return  n * factoriel(n - 1);
    }
  }

  public void testFactoriel() throws Exception {
    final int n = 3;
    int expected = factoriel(n);
    ProcessDefinition process = ProcessBuilder.createProcess("factoriel", "1.0")
    .addIntegerData("n", n)
    .addIntegerData("result", 1)
    .addSystemTask("start")
    .addSystemTask("prepareResult")
      .addConnector(Event.automaticOnEnter, SetVarConnector.class.getName(), true)
        .addInputParameter("variableName", "result")
        .addInputParameter("value", "${n - 1}")
    .addSubProcess("sub", "factoriel")
      .addSubProcessInParameter("result", "n")
      .addSubProcessOutParameter("n", "result")
    .addSystemTask("propagateResult")
      .addConnector(Event.automaticOnEnter, SetVarConnector.class.getName(), true)
        .addInputParameter("variableName", "n")
        .addInputParameter("value", "${n * result}")
    .addSystemTask("end")
    .addJoinType(JoinType.XOR)
    
    .addTransition("start", "end")
      .addCondition("n <= 1")
    .addTransition("start", "prepareResult")
      .addCondition("n > 1")
    .addTransition("prepareResult", "sub")
    .addTransition("sub", "propagateResult")
    .addTransition("propagateResult", "end")
    .done();

    //FOR N=3
    //create i1 (n=3, result=1), prepareResult => result=2, subFlow
    //create i2 (n=parent result=2, result=1), prepareResult => result=1, subflow
    //create i3 (n=parent result=1, result=1), direct go to end
    //back to i2 subflow, child n->result -> result=1, propagateResult => n = n * result = 2 * 1 = 2
    //back to i1 subflow, child n->result -> result=2, propagateResult => n = n * result = 3 * 2 = 6
    process = getManagementAPI().deploy(getBusinessArchive(process, null, SetVarConnector.class));

    final ProcessDefinitionUUID processUUID = process.getUUID();

    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    assertEquals(expected, getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "n"));
    getRuntimeAPI().deleteAllProcessInstances(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  
  public void testSupport() throws Exception {
    Set<String> actionValues = new HashSet<String>();
    actionValues.add("Close");
    actionValues.add("Update");
    actionValues.add("Escalate");

    ProcessDefinition supportLevel2 = ProcessBuilder.createProcess("SupportLevel2", "1.0")
    .addHuman(getLogin())
    .addSystemTask("Start")
    .addReceiveEventTask("ReceiveTicketFromLevel1", "ticket")
    .addHumanTask("Answer", getLogin())
    .addSendEventTask("SendResponseToLevel1")
    .addOutgoingEvent("response")
    .addSystemTask("End")
    .addTransition("Start", "ReceiveTicketFromLevel1")
    .addTransition("ReceiveTicketFromLevel1", "Answer")
    .addTransition("Answer", "SendResponseToLevel1")
    .addTransition("SendResponseToLevel1", "End")
    .done();

    ProcessDefinition supportLevel1 = ProcessBuilder.createProcess("SupportLevel1", "1.0")
    .addHuman(getLogin())
    .addIntegerData("level", 1)
    .addEnumData("action", actionValues, "Escalate")
    .addSystemTask("Start")
    //.addTimerTask("Timer", "60000")
    //.addSystemTask("AutoAcknowledgeTicket")
    .addHumanTask("CreateTicket", getLogin())
    .addHumanTask("AcknowledgeTicket", getLogin())
    .addHumanTask("ValidateTicket", getLogin())
    .addJoinType(JoinType.XOR)
    .addHumanTask("UpdateTicket", getLogin())
    .addHumanTask("CloseTicket", getLogin())
    .addSystemTask("EscalateToLevel2")
    .addConnector(Event.automaticOnEnter, StartProcessConnector.class.getName(), true)
    .addInputParameter("processUUIDToStart", supportLevel2.getUUID())
    .addSendEventTask("SendTicketToLevel2")
    .addJoinType(JoinType.XOR)
    .addOutgoingEvent("ticket")
    .addReceiveEventTask("ReceiveResponseFromLevel2", "response")
    .addReceiveEventTask("ReceiveClosingFromLevel2", "closing")
    .addSystemTask("End")
    .addTransition("Start", "CreateTicket")
    //.addTransition("CreateTicket", "Timer")
    //.addTransition("Timer", "AutoAcknowledgeTicket")
    .addTransition("CreateTicket", "AcknowledgeTicket")
    .addTransition("AcknowledgeTicket", "ValidateTicket")
    //.addTransition("AutoAcknowledgeTicket", "ValidateTicket")
    .addTransition("ValidateTicket", "UpdateTicket")
    .addCondition("action==\"Update\"")
    .addTransition("UpdateTicket", "ValidateTicket")
    .addTransition("ValidateTicket", "EscalateToLevel2")
    .addCondition("level ==1 && action==\"Escalate\"")
    .addTransition("EscalateToLevel2", "SendTicketToLevel2")
    .addTransition("SendTicketToLevel2", "ValidateTicket")
    .addCondition("level > 1 && action==\"Escalate\"")
    .addTransition("SendTicketToLevel2", "ReceiveResponseFromLevel2")
    .addTransition("ReceiveResponseFromLevel2", "ValidateTicket")
    .addTransition("ValidateTicket", "CloseTicket")
    .addCondition("action==\"Close\"")
    .addTransition("CloseTicket", "End")
    .done();

    supportLevel1 = getManagementAPI().deploy(getBusinessArchive(supportLevel1, null, StartProcessConnector.class));
    supportLevel2 = getManagementAPI().deploy(getBusinessArchive(supportLevel2));

    final ProcessDefinitionUUID supportLevel1UUID = supportLevel1.getUUID();
    final ProcessDefinitionUUID supportLevel2UUID = supportLevel2.getUUID();

    final ProcessInstanceUUID instance1 = getRuntimeAPI().instantiateProcess(supportLevel1UUID);

    assertEquals(InstanceState.STARTED, getQueryRuntimeAPI().getProcessInstance(instance1).getInstanceState());

    executeTask(instance1, "CreateTicket");
    executeTask(instance1, "AcknowledgeTicket");
    executeTask(instance1, "ValidateTicket");

    Thread.sleep(2000);
    final ProcessInstanceUUID instance2 = getQueryRuntimeAPI().getProcessInstances(supportLevel2UUID).iterator().next().getUUID();
    executeTask(instance2, "Answer");
    assertEquals(InstanceState.STARTED, getQueryRuntimeAPI().getProcessInstance(instance1).getInstanceState());
    assertEquals(InstanceState.FINISHED, getQueryRuntimeAPI().getProcessInstance(instance2).getInstanceState());

    Thread.sleep(2000);

    assertEquals(InstanceState.STARTED, getQueryRuntimeAPI().getProcessInstance(instance1).getInstanceState());

    getRuntimeAPI().setProcessInstanceVariable(instance1, "action", "Close");
    executeTask(instance1, "ValidateTicket");
    assertEquals(InstanceState.STARTED, getQueryRuntimeAPI().getProcessInstance(instance1).getInstanceState());
    executeTask(instance1, "CloseTicket");
    assertEquals(InstanceState.FINISHED, getQueryRuntimeAPI().getProcessInstance(instance1).getInstanceState());

    getManagementAPI().deleteAllProcesses();
  }

}
