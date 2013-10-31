package org.ow2.bonita.transition;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.ow2.bonita.APITestCase;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.runtime.ActivityInstance;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.util.BonitaException;
import org.ow2.bonita.util.ProcessBuilder;

public class TransitionWithGroovyExpressions extends APITestCase {

  public void testTransitionWithStringActivityVar() throws BonitaException {
    ProcessDefinition process = getManagementAPI().deploy(getBusinessArchive(getProcess()));
    
    ProcessDefinitionUUID processUUID = process.getUUID();

    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    // Check variables within execution pointing to act1 node
    Collection<ActivityInstance> acts = getQueryRuntimeAPI().getActivityInstances(instanceUUID, "act1");
    assertEquals(1, acts.size());
    ActivityInstance activityInst = acts.iterator().next();
    assertNotNull(activityInst);

    assertNotNull(getQueryRuntimeAPI().getActivityInstanceVariables(activityInst.getUUID()));
    assertEquals("no", getQueryRuntimeAPI().getActivityInstanceVariable(activityInst.getUUID(), "check"));
    checkStopped(instanceUUID, new String[]{});

    // start & terminate "act1" task
    executeTask(instanceUUID, "act1");
    checkStopped(instanceUUID, new String[]{"act1"});

    //get the child execution pointing on act2 node
    acts = getQueryRuntimeAPI().getActivityInstances(instanceUUID, "act2");
    assertEquals(1, acts.size());
    activityInst = acts.iterator().next();
    assertNotNull(activityInst);

    // start & terminate "act2" task
    executeTask(instanceUUID, "act2");
    checkExecutedOnce(instanceUUID, new String[]{"act1", "act2"});

    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testTransitionWithNoEnumerationActivityVar() throws BonitaException {
    ProcessDefinition process = getManagementAPI().deploy(getBusinessArchive(getNoEnumProcess()));
    
    ProcessDefinitionUUID processUUID = process.getUUID();

    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    // Check variables within execution pointing to act1 node
    Collection<ActivityInstance> acts = getQueryRuntimeAPI().getActivityInstances(instanceUUID, "act1");
    assertEquals(1, acts.size());
    ActivityInstance activityInst = acts.iterator().next();
    assertNotNull(activityInst);

    assertNotNull(getQueryRuntimeAPI().getActivityInstanceVariables(activityInst.getUUID()));
    String enumCheck = (String) getQueryRuntimeAPI().getActivityInstanceVariable(activityInst.getUUID(), "enumCheck");
    assertNotNull(enumCheck);
    assertEquals("no", enumCheck);

    checkStopped(instanceUUID, new String[]{});

    // start & terminate "act1" task
    executeTask(instanceUUID, "act1");
    checkStopped(instanceUUID, new String[]{"act1"});

    //get the child execution pointing on act2 node
    acts = getQueryRuntimeAPI().getActivityInstances(instanceUUID, "act2");
    assertEquals(1, acts.size());
    activityInst = acts.iterator().next();
    assertNotNull(activityInst);

    // start & terminate "act2" task
    executeTask(instanceUUID, "act2");
    checkExecutedOnce(instanceUUID, new String[]{"act1", "act2"});

    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }
  
  public void testTransitionWithYesEnumerationActivityVar() throws BonitaException {
    ProcessDefinition process = getManagementAPI().deploy(getBusinessArchive(getYesEnumProcess()));
    
    ProcessDefinitionUUID processUUID = process.getUUID();

    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    // Check variables within execution pointing to act1 node
    Collection<ActivityInstance> acts = getQueryRuntimeAPI().getActivityInstances(instanceUUID, "act1");
    assertEquals(1, acts.size());
    ActivityInstance activityInst = acts.iterator().next();
    assertNotNull(activityInst);

    assertNotNull(getQueryRuntimeAPI().getActivityInstanceVariables(activityInst.getUUID()));
    String enumCheck = (String) getQueryRuntimeAPI().getActivityInstanceVariable(activityInst.getUUID(), "enumCheck");
    assertNotNull(enumCheck);
    assertEquals("yes", enumCheck);

    checkStopped(instanceUUID, new String[]{});

    // start & terminate "act1" task
    executeTask(instanceUUID, "act1");
    checkStopped(instanceUUID, new String[]{"act1"});

    //get the child execution pointing on act2 node
    acts = getQueryRuntimeAPI().getActivityInstances(instanceUUID, "act3");
    assertEquals(1, acts.size());
    activityInst = acts.iterator().next();
    assertNotNull(activityInst);

    // start & terminate "act2" task
    executeTask(instanceUUID, "act3");
    checkExecutedOnce(instanceUUID, new String[]{"act1", "act3"});

    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public ProcessDefinition getProcess() {
    return ProcessBuilder.createProcess("transitionWithStringActivityVar", "1.0")
      .addHuman("admin")
      
      .addHumanTask("act1", "admin")
        .addStringData("check", "no")
      .addHumanTask("act2", "admin")
      .addHumanTask("act3", "admin")
      
      .addTransition("act1_act2", "act1", "act2")
        .addCondition("check.equals(\"no\")")
      .addTransition("act1_act3", "act1", "act3")
        .addCondition("check.equals(\"yes\")")
      .done();
  }

  private ProcessDefinition getNoEnumProcess() {
    Set<String> enums = new HashSet<String>();
    enums.add("yes");
    enums.add("no");
    
    return ProcessBuilder.createProcess("transitionWithEnumerationActivityVar", "1.0")
      .addHuman("admin")
      
      .addHumanTask("act1", "admin")
        .addEnumData("enumCheck", enums, "no")
      .addHumanTask("act2", "admin")
      .addHumanTask("act3", "admin")

      .addTransition("act1_act2", "act1", "act2")
        .addCondition("enumCheck.equals(\"no\")")
      .addTransition("act1_act3", "act1", "act3")
        .addCondition("enumCheck.equals(\"yes\")")
      .done();
  }
  
  private ProcessDefinition getYesEnumProcess() {
    Set<String> enums = new HashSet<String>();
    enums.add("yes");
    enums.add("no");
    
    return ProcessBuilder.createProcess("transitionWithEnumerationActivityVar", "1.0")
      .addHuman("admin")
      
      .addHumanTask("act1", "admin")
        .addEnumData("enumCheck", enums, "yes")
      .addHumanTask("act2", "admin")
      .addHumanTask("act3", "admin")

      .addTransition("act1_act2", "act1", "act2")
        .addCondition("enumCheck.equals(\"no\")")
      .addTransition("act1_act3", "act1", "act3")
        .addCondition("enumCheck.equals(\"yes\")")
      .done();
  }

}
