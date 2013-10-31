package org.ow2.bonita.activity.route;

import java.net.URL;
import java.util.Set;

import org.ow2.bonita.APITestCase;
import org.ow2.bonita.facade.def.majorElement.ActivityDefinition.JoinType;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.runtime.ActivityState;
import org.ow2.bonita.facade.runtime.InstanceState;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.light.LightActivityInstance;
import org.ow2.bonita.light.LightProcessInstance;
import org.ow2.bonita.util.BonitaException;
import org.ow2.bonita.util.ProcessBuilder;

public class SplitAndTest extends APITestCase {

  public void testSplitAnd1Tr() throws BonitaException {
    URL xpdlUrl = this.getClass().getResource("splitAnd1Tr.xpdl");
    ProcessDefinition process = getManagementAPI().deploy(getBusinessArchiveFromXpdl(xpdlUrl));
    
    ProcessDefinitionUUID processUUID = process.getUUID();
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);
    checkExecutedOnce(instanceUUID, new String[]{"r1"});
    getManagementAPI().disable(processUUID);
    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testSplitAnd2Tr() throws BonitaException {
    URL xpdlUrl = this.getClass().getResource("splitAnd2Tr.xpdl");
    ProcessDefinition process = getManagementAPI().deploy(getBusinessArchiveFromXpdl(xpdlUrl));
    
    ProcessDefinitionUUID processUUID = process.getUUID();
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);
    checkExecutedOnce(instanceUUID, new String[]{"r1", "r2","r3"});
    getManagementAPI().disable(processUUID);
    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testSplitAndProed2Tr() throws BonitaException {
    URL xpdlUrl = this.getClass().getResource("splitAndProed2Tr.xpdl");
    ProcessDefinition process = getManagementAPI().deploy(getBusinessArchiveFromXpdl(xpdlUrl));
    
    ProcessDefinitionUUID processUUID = process.getUUID();
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);
    checkExecutedOnce(instanceUUID, new String[]{"r1", "r2","r3"});
    getManagementAPI().disable(processUUID);
    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().deleteProcess(processUUID);
  }
  
  public void testSplitAndTwoBranches() throws Exception {
    ProcessDefinition process = ProcessBuilder.createProcess("branches", "1.0")
    .addHuman(getLogin())
    .addSystemTask("start")
    .addSystemTask("branch1")
    .addSystemTask("branch2")
    .addTransition("start", "branch1")
    .addTransition("start", "branch2")
    .done();

    getManagementAPI().deploy(getBusinessArchive(process));
    
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(process.getUUID());
    waitForInstanceEnd(4000, 100, instanceUUID);
    Set<LightActivityInstance> activities = getQueryRuntimeAPI().getLightActivityInstances(instanceUUID, "branch1");
    assertEquals(1, activities.size());
    assertEquals(ActivityState.FINISHED, activities.iterator().next().getState());
    activities = getQueryRuntimeAPI().getLightActivityInstances(instanceUUID, "branch2");
    assertEquals(1, activities.size());
    assertEquals(ActivityState.FINISHED, activities.iterator().next().getState());
    
    getManagementAPI().deleteProcess(process.getUUID());
  }
  
  public void testJoinAndWithAutomaticTasks() throws Exception {
    ProcessDefinition process = ProcessBuilder.createProcess("ProcessANDAndAutomatique", "1.0")
      .addHuman(getLogin())
      .addHumanTask("step1", getLogin())
      .addDecisionNode("gateway")
      .addJoinType(JoinType.AND)
      .addSystemTask("step2")
      .addSystemTask("step3")
      .addTransition("step1", "gateway")
      .addTransition("gateway", "step2")
      .addTransition("gateway", "step3")
      .done();
    
    process = getManagementAPI().deploy(getBusinessArchive(process));
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(process.getUUID());
    
    executeTask(instanceUUID, "step1");    
    Thread.sleep(2000);
    
    final LightProcessInstance processInstance = getQueryRuntimeAPI().getLightProcessInstance(instanceUUID);
    assertEquals(InstanceState.FINISHED, processInstance.getInstanceState());
    
    Set<LightActivityInstance> activities = getQueryRuntimeAPI().getLightActivityInstances(instanceUUID, "step2");
    assertEquals(1, activities.size());
    assertEquals(ActivityState.FINISHED, activities.iterator().next().getState());
    activities = getQueryRuntimeAPI().getLightActivityInstances(instanceUUID, "step3");
    assertEquals(1, activities.size());
    assertEquals(ActivityState.FINISHED, activities.iterator().next().getState());
    
    getManagementAPI().deleteProcess(process.getUUID());
    
  }
  
  public void testComplexJoinAndWithAutomaticTasks() throws Exception {
    ProcessDefinition process = ProcessBuilder.createProcess("ProcessANDAndAutomatique", "1.1")
      .addSystemTask("step1")
      .addSystemTask("step2")
      .addSystemTask("step3")
      .addSystemTask("step4")
      .addSystemTask("step5")
      .addSystemTask("step6")
      .addSystemTask("step7")
      .addSystemTask("step8")
      .addDecisionNode("gateway1")
      .addJoinType(JoinType.AND)
      .addDecisionNode("gateway2")
      .addJoinType(JoinType.AND)
      .addDecisionNode("gateway3")
      .addJoinType(JoinType.AND)
      .addDecisionNode("gateway4")
      .addJoinType(JoinType.AND)
      .addTransition("step1", "gateway1")
      .addTransition("gateway1", "step2")
      .addTransition("gateway1", "gateway2")
      .addTransition("gateway1", "gateway3")
      .addTransition("step2", "gateway2")
      .addTransition("gateway2", "step3")
      .addTransition("gateway2", "step4")
      .addTransition("gateway3", "step7")
      .addTransition("gateway3", "step8")
      .addTransition("gateway3", "gateway4")
      .addTransition("gateway4", "step5")
      .addTransition("gateway4", "step6")
      .done();
    
    process = getManagementAPI().deploy(getBusinessArchive(process));
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(process.getUUID());
    
    waitForInstanceEnd(6000, 100, instanceUUID);
    
    final LightProcessInstance processInstance = getQueryRuntimeAPI().getLightProcessInstance(instanceUUID);
    assertEquals(InstanceState.FINISHED, processInstance.getInstanceState());
    
    final int numberOfSteps = 8;
    for (int i = 0; i < numberOfSteps; i++) {
      final Set<LightActivityInstance> activities = getQueryRuntimeAPI().getLightActivityInstances(instanceUUID, "step" + (i+1));
      assertEquals(1, activities.size());
      assertEquals(ActivityState.FINISHED, activities.iterator().next().getState());
    }
    
    getManagementAPI().deleteProcess(process.getUUID());
    
  }

}
