package org.ow2.bonita.example;

import java.util.Map;

import org.ow2.bonita.definition.InstanceInitiator;
import org.ow2.bonita.example.aw.MultiInstantiationApproval;
import org.ow2.bonita.example.aw.hook.Accept;
import org.ow2.bonita.example.aw.hook.CheckDecision;
import org.ow2.bonita.example.aw.hook.Reject;
import org.ow2.bonita.example.aw.instantiator.ApprovalInstantiator;
import org.ow2.bonita.example.aw.performer.UserPerformerAssign;
import org.ow2.bonita.facade.def.element.HookDefinition.Event;
import org.ow2.bonita.facade.def.majorElement.ActivityDefinition.JoinType;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition.ProcessState;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.util.ProcessBuilder;

public class AWMultiInstProcessTest extends AWMultiInstTest {

  protected void execute(final Map<String, String> decisions, final String nodeToCheck) throws Exception {
    ProcessDefinition process = getAWMultiInstProcess();
    ProcessInstanceUUID instanceUUID = MultiInstantiationApproval.execute(process, decisions);

    login();
    checkExecutedOnce(instanceUUID, new String[]{nodeToCheck});
    MultiInstantiationApproval.cleanProcess(instanceUUID);
    assertEquals(0, getQueryDefinitionAPI().getProcesses(MultiInstantiationApproval.PROCESS_ID, ProcessState.ENABLED).size());
  }

  private ProcessDefinition getAWMultiInstProcess() {
    ProcessDefinition process =
      ProcessBuilder.createProcess("multiInstantiation", "1.0")
      .addDescription("Advanced version of an Approval workflow in which it requires approval from multiple users to validate or reject a particular request")

      .addStringData("MyRequest")

      .addGroup("User")
      .addGroupResolver(InstanceInitiator.class.getName())
      .addHuman("human")

      .addSystemTask("BonitaEnd")
        .addJoinType(JoinType.XOR)
      .addSystemTask("BonitaStart")
      .addHumanTask("Approval", "human")
        .addStringData("decisionAccepted")
        .addStringData("performer", "nobody")
        .addFilter(UserPerformerAssign.class.getName())
        .addMultiInstanciation("performer", ApprovalInstantiator.class.getName())
      .addSystemTask("CheckDecision")
        .addStringData("decisionAccepted")
        .addConnector(Event.automaticOnEnter, CheckDecision.class.getName(), true)
      .addSystemTask("Accept")
        .addConnector(Event.automaticOnEnter, Accept.class.getName(), true)
      .addSystemTask("Reject")
        .addConnector(Event.automaticOnEnter, Reject.class.getName(), true)
      
      .addTransition("Start_Approval", "BonitaStart", "Approval")
      .addTransition("Approval_CheckDecision", "Approval", "CheckDecision")
      .addTransition("CheckDecision_Reject", "CheckDecision", "Reject")
        .addCondition("decisionAccepted.compareTo(\"no\") == 0")
      .addTransition("CheckDecision_Accept", "CheckDecision", "Accept")
        .addCondition("decisionAccepted.compareTo(\"yes\") == 0")
      .addTransition("Reject_End", "Reject", "BonitaEnd")
      .addTransition("Accept_End", "Accept", "BonitaEnd")
      .done();
    return process;
  }
}
