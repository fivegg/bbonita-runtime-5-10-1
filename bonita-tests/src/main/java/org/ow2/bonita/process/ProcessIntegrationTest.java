package org.ow2.bonita.process;

import org.bonitasoft.connectors.bonita.resolvers.ProcessInitiatorRoleResolver;
import org.ow2.bonita.APITestCase;
import org.ow2.bonita.facade.def.majorElement.ActivityDefinition.JoinType;
import org.ow2.bonita.facade.def.majorElement.ActivityDefinition.SplitType;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.light.LightProcessDefinition;
import org.ow2.bonita.util.ProcessBuilder;

public class ProcessIntegrationTest extends APITestCase {

  public void testParallelTasks() throws Exception {
    ProcessDefinition def =
      ProcessBuilder.createProcess("parallel", null)
        .addGroup("initiator")
          .addGroupResolver(ProcessInitiatorRoleResolver.class.getName())
        .addHumanTask("step1", "initiator")
        .addHumanTask("step2", "initiator")
        .addDecisionNode("gate")
          .addJoinType(JoinType.AND)
        .addSystemTask("start")
          .addSplitType(SplitType.XOR)
        .addSystemTask("end")
        .addHumanTask("step3", "initiator")
        .addTransition("start", "step1")
        .addTransition("start", "step2")
        .addTransition("step1", "gate")
        .addTransition("step2", "gate")
        .addTransition("gate", "step3")
        .addTransition("step3", "end")
      .done();
    
    def = getManagementAPI().deploy(getBusinessArchive(def, null, ProcessInitiatorRoleResolver.class));
    ProcessDefinitionUUID defUUID = def.getUUID();
    getRuntimeAPI().instantiateProcess(defUUID);
    getRuntimeAPI().instantiateProcess(defUUID);
    
    getRuntimeAPI().deleteAllProcessInstances(defUUID);
    getManagementAPI().deleteProcess(defUUID);
  }

  public void testInstantiate20TimesAProcess() throws Exception {
    ProcessDefinition process =
      ProcessBuilder.createProcess("approval", "2.0")
      .addHuman(getLogin())
      .addHumanTask("first", getLogin())
    .done();
    
    process = getManagementAPI().deploy(getBusinessArchive(process));
    
    for (int i = 0; i < 20; i++) {
      getRuntimeAPI().instantiateProcess(process.getUUID());
    }
    
    final String metaName = "*****" + process.getUUID() + "*****instance-nb*****";
    System.out.println(getManagementAPI().getMetaData(metaName));
    
    getRuntimeAPI().deleteAllProcessInstances(process.getUUID());
    getManagementAPI().deleteProcess(process.getUUID());
  }

  
  public void testProcessDescriptionMustBeAText() throws Exception {
    StringBuilder description = new StringBuilder();
    for (int i = 0; i < 255; i++) {
      description.append(i);
    }

    ProcessDefinition process = ProcessBuilder.createProcess("proc_desc", "1.5")
      .addDescription(description.toString())
    .done();

    getManagementAPI().deploy(getBusinessArchive(process));

    LightProcessDefinition processDef = getQueryDefinitionAPI().getLightProcess(process.getUUID());
    assertEquals(description.toString(), processDef.getDescription());

    getManagementAPI().deleteProcess(process.getUUID());
  }

  public void testDeployProcessWithAVersionGreaterThanTen() throws Exception {
    ProcessDefinition process = ProcessBuilder.createProcess("process", "9.4").done();
    ProcessDefinition newProcess = ProcessBuilder.createProcess("process", "10.0").done();
    ProcessDefinition definition = getManagementAPI().deploy(getBusinessArchive(process));
    ProcessDefinition newDefinition = getManagementAPI().deploy(getBusinessArchive(newProcess));

    getManagementAPI().deleteProcess(definition.getUUID());
    getManagementAPI().deleteProcess(newDefinition.getUUID());
  }

}
