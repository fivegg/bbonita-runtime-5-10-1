package org.ow2.bonita.example;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.ow2.bonita.definition.InstanceInitiator;
import org.ow2.bonita.example.aw.ApprovalWorkflow;
import org.ow2.bonita.example.aw.hook.Accept;
import org.ow2.bonita.example.aw.hook.Reject;
import org.ow2.bonita.example.aw.mapper.ApprovalMapper;
import org.ow2.bonita.facade.def.element.HookDefinition.Event;
import org.ow2.bonita.facade.def.majorElement.ActivityDefinition.JoinType;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition.ProcessState;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.util.BonitaException;
import org.ow2.bonita.util.ProcessBuilder;

public class AWProcessTest extends AWTest {

  protected void execute() throws BonitaException, IOException, ClassNotFoundException {
    ProcessDefinition process = getAWProcess();
    ProcessInstanceUUID instanceUUID = ApprovalWorkflow.execute(process, true);
    ApprovalWorkflow.cleanProcess(instanceUUID);
    assertEquals(0, getQueryDefinitionAPI().getProcesses(ApprovalWorkflow.PROCESS_ID, ProcessState.ENABLED).size());  
  }
  
  protected ProcessDefinition getAWProcess() {
    Set<String> applications = new HashSet<String>();
    applications.add("Word");
    applications.add("Excel");
    applications.add("MailReader");
    applications.add("WebBrowser");

    ProcessDefinition process =
      ProcessBuilder.createProcess("ApprovalWorkflow", null)

      .addEnumData("Applications", applications, "Word")

      .addGroup("User")
      .addGroupResolver(InstanceInitiator.class.getName())
      .addGroup("Administrator")
      .addGroupResolver(ApprovalMapper.class.getName())
      
      .addSystemTask("BonitaStart")
      .addSystemTask("BonitaEnd")
        .addJoinType(JoinType.XOR)
      .addHumanTask("Request", "User")
      .addSystemTask("Reject")
        .addConnector(Event.automaticOnEnter, Reject.class.getName(), false)
      .addSystemTask("Accept")
        .addConnector(Event.automaticOnEnter, Accept.class.getName(), false)
      .addHumanTask("Approval", "Administrator")
        .addBooleanData("isGranted", false)
      
      .addTransition("Request_Approval", "Request", "Approval")
      .addTransition("Reject_End", "Reject", "BonitaEnd")
      .addTransition("Accept_End", "Accept", "BonitaEnd")
      .addTransition("Approval_Reject", "Approval", "Reject")
        .addCondition("isGranted.compareTo(Boolean.valueOf(\"false\")) == 0")
      .addTransition("Approval_Accept", "Approval", "Accept")
        .addCondition("isGranted.compareTo(Boolean.valueOf(\"true\")) == 0")
      .addTransition("Start_Request", "BonitaStart", "Request")
      .done();
    return process;
  }
}
