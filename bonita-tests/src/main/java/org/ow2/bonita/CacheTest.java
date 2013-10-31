package org.ow2.bonita;

import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.connectors.bonita.resolvers.ProcessInitiatorRoleResolver;
import org.ow2.bonita.definition.InstanceInitiator;
import org.ow2.bonita.example.aw.hook.Accept;
import org.ow2.bonita.example.aw.hook.CheckDecision;
import org.ow2.bonita.example.aw.hook.Reject;
import org.ow2.bonita.example.aw.instantiator.ApprovalInstantiator;
import org.ow2.bonita.example.aw.performer.UserPerformerAssign;
import org.ow2.bonita.facade.def.element.HookDefinition.Event;
import org.ow2.bonita.facade.def.majorElement.ActivityDefinition.JoinType;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.exception.DeploymentException;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.util.BonitaException;
import org.ow2.bonita.util.ProcessBuilder;

public class CacheTest extends APITestCase {

  private ProcessDefinition getAWMultiInstProcess(final int i) {
    final ProcessDefinition process = ProcessBuilder
        .createProcess("multiInstantiation_" + i, "1." + i)
        .addDescription(
            "Advanced version of an Approval workflow in which it requires approval from multiple users to validate or reject a particular request")
        .addStringData("MyRequest").addGroup("User").addGroupResolver(InstanceInitiator.class.getName())
        .addHuman("human").addSystemTask("BonitaEnd").addJoinType(JoinType.XOR).addSystemTask("BonitaStart")
        .addHumanTask("Approval", "human").addStringData("decisionAccepted").addStringData("performer", "nobody")
        .addFilter(UserPerformerAssign.class.getName())
        .addMultiInstanciation("performer", ApprovalInstantiator.class.getName()).addSystemTask("CheckDecision")
        .addStringData("decisionAccepted").addConnector(Event.automaticOnEnter, CheckDecision.class.getName(), true)
        .addSystemTask("Accept").addConnector(Event.automaticOnEnter, Accept.class.getName(), true)
        .addSystemTask("Reject").addConnector(Event.automaticOnEnter, Reject.class.getName(), true)
        .addAttachment("myDoc" + i, "/home/manu/myDocPath" + i)
        .addTransition("Start_Approval", "BonitaStart", "Approval")
        .addTransition("Approval_CheckDecision", "Approval", "CheckDecision")
        .addTransition("CheckDecision_Reject", "CheckDecision", "Reject")
        .addCondition("decisionAccepted.compareTo(\"no\") == 0")
        .addTransition("CheckDecision_Accept", "CheckDecision", "Accept")
        .addCondition("decisionAccepted.compareTo(\"yes\") == 0").addTransition("Reject_End", "Reject", "BonitaEnd")
        .addTransition("Accept_End", "Accept", "BonitaEnd").done();
    return process;
  }

  private void getProcessDefinition(final ProcessDefinitionUUID uuid, final int nTimes) throws BonitaException {
    final long firstStartTime = System.currentTimeMillis();
    getQueryDefinitionAPI().getProcess(uuid);
    final long firstCallDuration = System.currentTimeMillis() - firstStartTime;
    // System.out.println("FIRST getProcessDefinition(" + uuid + " in (ms): " +
    // firstCallDuration);

    final List<Long> execTimes = new ArrayList<Long>(nTimes);
    for (int i = 0; i < nTimes; i++) {
      final long startTime = System.currentTimeMillis();
      getQueryDefinitionAPI().getProcess(uuid);
      final long execTime = System.currentTimeMillis() - startTime;
      execTimes.add(execTime);
      // System.out.println("getProcessDefinition in: " + execTime + " ms");
    }
    System.out.println("First/Avg for uuid  " + uuid + ": " + firstCallDuration + "/" + getAvg(execTimes));
  }

  private ProcessDefinitionUUID deployAProcessDefinition(final int cpt) throws DeploymentException {
    ProcessDefinition definition = getAWMultiInstProcess(cpt);
    definition = getManagementAPI().deploy(getBusinessArchive(definition, null, ProcessInitiatorRoleResolver.class));
    return definition.getUUID();
  }

  public void testLoopDeployAndGetProcessDef() throws BonitaException {
    final int processDefNb = 50;
    final List<ProcessDefinitionUUID> uuids = new ArrayList<ProcessDefinitionUUID>(50);
    for (int i = 0; i < processDefNb; i++) {
      uuids.add(deployAProcessDefinition(i));
    }
    for (int i = 0; i < processDefNb; i++) {
      getProcessDefinition(uuids.get(i), 30);
    }
    // clean up:
    for (int i = 0; i < processDefNb; i++) {
      getManagementAPI().deleteProcess(uuids.get(i));
    }
  }

  /**
   * calculate the Average value of the list of long values passed as parameter
   * 
   * @param list
   *          the list of input values
   * @return the average value, or 0 if list is empty
   */
  private long getAvg(final List<Long> list) {
    if (list.size() == 0) {
      return 0;
    }
    long total = 0;
    for (final long value : list) {
      total += value;
    }
    return total / list.size();
  }

}
