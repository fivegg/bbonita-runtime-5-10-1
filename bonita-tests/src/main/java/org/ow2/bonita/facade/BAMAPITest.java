package org.ow2.bonita.facade;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import junit.framework.Assert;

import org.ow2.bonita.APITestCase;
import org.ow2.bonita.command.BTTFActivityInstance;
import org.ow2.bonita.command.BTTFProcessInstance;
import org.ow2.bonita.facade.def.majorElement.ActivityDefinition.JoinType;
import org.ow2.bonita.facade.def.majorElement.ActivityDefinition.Type;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.exception.MonitoringException;
import org.ow2.bonita.facade.runtime.ActivityState;
import org.ow2.bonita.facade.runtime.InstanceState;
import org.ow2.bonita.facade.runtime.TaskInstance;
import org.ow2.bonita.facade.uuid.ActivityDefinitionUUID;
import org.ow2.bonita.facade.uuid.ActivityInstanceUUID;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.light.LightActivityInstance;
import org.ow2.bonita.light.LightProcessInstance;
import org.ow2.bonita.util.BonitaConstants;
import org.ow2.bonita.util.BusinessArchiveFactory;
import org.ow2.bonita.util.DateUtil;
import org.ow2.bonita.util.Misc;
import org.ow2.bonita.util.ProcessBuilder;

public class BAMAPITest extends APITestCase {

  private static final Logger LOG = Logger.getLogger(BAMAPITest.class.getName());    
    
  public void testGetNumberOfOpenStepsPerDay() throws Exception {
    ProcessDefinition definition = 
      ProcessBuilder.createProcess("bam", "1.0")
      .addHuman("john")
      .addHumanTask("a", "john")
      .done();

    definition = getManagementAPI().deploy(getBusinessArchive(definition));
    ProcessDefinitionUUID definitionUUID = definition.getUUID();

    Date today = new Date();
    List<Integer> openSteps = getBAMAPI().getNumberOfOpenStepsPerDay(today);
    assertEquals(1, openSteps.size());
    assertEquals(Integer.valueOf(0), openSteps.get(0));

    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(definitionUUID);
    getManagementAPI().deployJar("getExecJar.jar", Misc.generateJar(BTTFActivityInstance.class));
    ProcessInstanceUUID processUUID1 = getRuntimeAPI().instantiateProcess(definitionUUID);
    ProcessInstanceUUID processUUID2 = getRuntimeAPI().instantiateProcess(definitionUUID);
    openSteps = getBAMAPI().getNumberOfOpenStepsPerDay(today);
    assertEquals(1, openSteps.size());
    assertEquals(Integer.valueOf(3), openSteps.get(0));

    TaskInstance task = getQueryRuntimeAPI().getTasks(processUUID1).iterator().next();
    BTTFActivityInstance dayBeforeYesterday = new BTTFActivityInstance(task.getUUID(), DateUtil.backTo(today, 2), null);
    getCommandAPI().execute(dayBeforeYesterday);
    task = getQueryRuntimeAPI().getTasks(processUUID2).iterator().next();
    BTTFActivityInstance yesterday = new BTTFActivityInstance(task.getUUID(), DateUtil.backTo(today, 1), null);
    getCommandAPI().execute(yesterday);

    openSteps = getBAMAPI().getNumberOfOpenStepsPerDay(DateUtil.backTo(today, 2));
    assertEquals(3, openSteps.size());
    assertEquals(Integer.valueOf(1), openSteps.get(0));
    assertEquals(Integer.valueOf(2), openSteps.get(1));
    assertEquals(Integer.valueOf(3), openSteps.get(2));

    task = getQueryRuntimeAPI().getTasks(processUUID1).iterator().next();
    dayBeforeYesterday = new BTTFActivityInstance(task.getUUID(), DateUtil.backTo(today, 2), DateUtil.backTo(today, 2));
    getCommandAPI().execute(dayBeforeYesterday);
    task = getQueryRuntimeAPI().getTasks(processUUID2).iterator().next();
    yesterday = new BTTFActivityInstance(task.getUUID(), DateUtil.backTo(today, 1), DateUtil.backTo(today, 1));
    getCommandAPI().execute(yesterday);

    openSteps = getBAMAPI().getNumberOfOpenStepsPerDay(DateUtil.backTo(today, 2));
    assertEquals(3, openSteps.size());
    assertEquals(Integer.valueOf(0), openSteps.get(0));
    assertEquals(Integer.valueOf(0), openSteps.get(1));
    assertEquals(Integer.valueOf(1), openSteps.get(2));

    getRuntimeAPI().cancelProcessInstance(instanceUUID);
    openSteps = getBAMAPI().getNumberOfOpenStepsPerDay(DateUtil.backTo(today, 2));
    assertEquals(3, openSteps.size());
    assertEquals(Integer.valueOf(0), openSteps.get(0));
    assertEquals(Integer.valueOf(0), openSteps.get(1));
    assertEquals(Integer.valueOf(0), openSteps.get(2));

    getManagementAPI().removeJar("getExecJar.jar");
    getRuntimeAPI().deleteAllProcessInstances(definitionUUID);
    getManagementAPI().deleteProcess(definitionUUID);
  }

  public void testGetNumberOfExecutingCasesPerDay() throws Exception {
    final String apiType = System.getProperty(BonitaConstants.API_TYPE_PROPERTY, "undefined");
    final String jeeServer = System.getProperty(BonitaConstants.JEE_SERVER_PROPERTY, "undefined");
    //this test can not be executed if we are on jboss4/EJB3 as tehre is a conflict with javassist.jar library
    boolean executeTest = ! (jeeServer.equals("jboss4") && apiType.equals("EJB3"));

    if (executeTest) {
      ProcessDefinition definition = 
        ProcessBuilder.createProcess("bam", "1.0")
        .addHuman("john")
        .addHumanTask("a", "john")
        .done();

      definition = getManagementAPI().deploy(getBusinessArchive(definition));
      ProcessDefinitionUUID definitionUUID = definition.getUUID();
      getRuntimeAPI().instantiateProcess(definitionUUID);

      Date today = new Date();
      List<Integer> executingCases = getBAMAPI().getNumberOfExecutingCasesPerDay(today);
      assertEquals(1, executingCases.size());
      assertEquals(Integer.valueOf(1), executingCases.get(0));

      getRuntimeAPI().instantiateProcess(definitionUUID);
      getManagementAPI().deployJar("getExecJar.jar", Misc.generateJar(BTTFProcessInstance.class));
      BTTFProcessInstance dayBeforeYesterday = new BTTFProcessInstance(
          getRuntimeAPI().instantiateProcess(definitionUUID), DateUtil.backTo(today, 2), null);
      getCommandAPI().execute(dayBeforeYesterday);
      BTTFProcessInstance yesterday = new BTTFProcessInstance(
          getRuntimeAPI().instantiateProcess(definitionUUID), DateUtil.backTo(today, 1), null);
      getCommandAPI().execute(yesterday);

      executingCases = getBAMAPI().getNumberOfExecutingCasesPerDay(DateUtil.backTo(today, 2));
      assertEquals(3, executingCases.size());
      assertEquals(Integer.valueOf(1), executingCases.get(0));
      assertEquals(Integer.valueOf(2), executingCases.get(1));
      assertEquals(Integer.valueOf(4), executingCases.get(2));

      getManagementAPI().removeJar("getExecJar.jar");
      getRuntimeAPI().deleteAllProcessInstances(definitionUUID);
      getManagementAPI().deleteProcess(definitionUUID);
    }
  }

  public void testGetNumberOfFinishedCasesPerDay() throws Exception {
    ProcessDefinition definition = 
      ProcessBuilder.createProcess("bam", "1.0")
      .addSystemTask("a")
      .done();

    definition = getManagementAPI().deploy(getBusinessArchive(definition));
    ProcessDefinitionUUID definitionUUID = definition.getUUID();
    getRuntimeAPI().instantiateProcess(definitionUUID);

    Date today = new Date();
    List<Integer> finishedCases = getBAMAPI().getNumberOfFinishedCasesPerDay(today);
    assertEquals(1, finishedCases.size());
    assertEquals(Integer.valueOf(1), finishedCases.get(0));

    getRuntimeAPI().instantiateProcess(definitionUUID);
    getManagementAPI().deployJar("getExecJar.jar", Misc.generateJar(BTTFProcessInstance.class));

    getRuntimeAPI().instantiateProcess(definitionUUID);
    getRuntimeAPI().instantiateProcess(definitionUUID);

    finishedCases = getBAMAPI().getNumberOfFinishedCasesPerDay(today);
    assertEquals(1, finishedCases.size());
    assertEquals(Integer.valueOf(4), finishedCases.get(0));

    BTTFProcessInstance dayBeforeYesterday = new BTTFProcessInstance(
        getRuntimeAPI().instantiateProcess(definitionUUID), DateUtil.backTo(today, 3), DateUtil.backTo(today, 2));
    getCommandAPI().execute(dayBeforeYesterday);
    BTTFProcessInstance yesterday = new BTTFProcessInstance(
        getRuntimeAPI().instantiateProcess(definitionUUID), DateUtil.backTo(today, 2), DateUtil.backTo(today, 1));
    getCommandAPI().execute(yesterday);

    finishedCases = getBAMAPI().getNumberOfFinishedCasesPerDay(DateUtil.backTo(today, 2));
    assertEquals(3, finishedCases.size());
    assertEquals(Integer.valueOf(1), finishedCases.get(0));
    assertEquals(Integer.valueOf(1), finishedCases.get(1));
    assertEquals(Integer.valueOf(4), finishedCases.get(2));

    getManagementAPI().removeJar("getExecJar.jar");
    getRuntimeAPI().deleteAllProcessInstances(definitionUUID);
    getManagementAPI().deleteProcess(definitionUUID);
  }

  public void testGetNumberOfUserOpenSteps() throws Exception {
    ProcessDefinition definition = 
      ProcessBuilder.createProcess("bam", "1.0")
      .addHuman("john")
      .addHumanTask("a", "john")
      .done();

    ProcessDefinition systask = 
      ProcessBuilder.createProcess("bam1", "1.0")
      .addHuman("john")
      .addSystemTask("task")
      .addHumanTask("a", "john")
      .addTransition("task", "a")
      .done();
    definition = getManagementAPI().deploy(getBusinessArchive(definition));
    systask = getManagementAPI().deploy(getBusinessArchive(systask));
    ProcessDefinitionUUID definitionUUID = definition.getUUID();
    ProcessDefinitionUUID systaskUUID = systask.getUUID();
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(definitionUUID);

    int openSteps = getBAMAPI().getNumberOfUserOpenSteps();
    assertEquals(0, openSteps);

    loginAs("john", "bpm");
    openSteps = getBAMAPI().getNumberOfUserOpenSteps();
    assertEquals(1, openSteps);

    ProcessInstanceUUID instanceSysTaskUUID = getRuntimeAPI().instantiateProcess(systaskUUID);
    openSteps = getBAMAPI().getNumberOfUserOpenSteps();
    assertEquals(2, openSteps);

    Collection<TaskInstance> tasks = getQueryRuntimeAPI().getTaskList(ActivityState.READY);
    TaskInstance task = tasks.iterator().next();

    getRuntimeAPI().suspendTask(task.getUUID(), true);
    openSteps = getBAMAPI().getNumberOfUserOpenSteps();
    assertEquals(2, openSteps);

    getRuntimeAPI().resumeTask(task.getUUID(), true);
    openSteps = getBAMAPI().getNumberOfUserOpenSteps();
    assertEquals(2, openSteps);

    getRuntimeAPI().cancelProcessInstance(instanceSysTaskUUID);
    openSteps = getBAMAPI().getNumberOfUserOpenSteps();
    assertEquals(1, openSteps);

    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getRuntimeAPI().deleteProcessInstance(instanceSysTaskUUID);
    getManagementAPI().deleteProcess(definitionUUID);
    getManagementAPI().deleteProcess(systaskUUID);
  }

  public void testGetNumberOfOpenSteps() throws Exception {
    ProcessDefinition johnDef = 
      ProcessBuilder.createProcess("john", "1.0")
      .addHuman("john")
      .addHumanTask("a", "john")
      .done();

    ProcessDefinition adminDef = 
      ProcessBuilder.createProcess("admin", "1.0")
      .addHuman("admin")
      .addSystemTask("task")
      .addHumanTask("a", "admin")
      .addTransition("task", "a")
      .done();

    ProcessDefinition jamesDef = 
      ProcessBuilder.createProcess("james", "1.0")
      .addHuman("james")
      .addHumanTask("a", "james")
      .done();
    // deployment
    johnDef = getManagementAPI().deploy(getBusinessArchive(johnDef));
    adminDef = getManagementAPI().deploy(getBusinessArchive(adminDef));
    jamesDef = getManagementAPI().deploy(getBusinessArchive(jamesDef));

    ProcessDefinitionUUID johnDefUUID = johnDef.getUUID();
    ProcessDefinitionUUID adminDefUUID = adminDef.getUUID();
    ProcessDefinitionUUID jamesDefUUID = jamesDef.getUUID();

    int openSteps = getBAMAPI().getNumberOfOpenSteps();
    assertEquals(0, openSteps);

    getRuntimeAPI().instantiateProcess(johnDefUUID);
    getRuntimeAPI().instantiateProcess(jamesDefUUID);
    openSteps = getBAMAPI().getNumberOfOpenSteps();
    assertEquals(2, openSteps);

    ProcessInstanceUUID adminInstUUID = getRuntimeAPI().instantiateProcess(adminDefUUID);
    openSteps = getBAMAPI().getNumberOfOpenSteps();
    assertEquals(3, openSteps);

    // admin finishes his task
    executeTask(adminInstUUID, "a");

    openSteps = getBAMAPI().getNumberOfOpenSteps();
    assertEquals(2, openSteps);

    getRuntimeAPI().deleteAllProcessInstances(johnDefUUID);
    getRuntimeAPI().deleteAllProcessInstances(adminDefUUID);
    getRuntimeAPI().deleteAllProcessInstances(jamesDefUUID);
    getManagementAPI().deleteProcess(johnDefUUID);
    getManagementAPI().deleteProcess(adminDefUUID);
    getManagementAPI().deleteProcess(jamesDefUUID);
  }

  public void testGetNumberOfUserOverdueSteps() throws Exception {
    ProcessDefinition johnDef1 = 
      ProcessBuilder.createProcess("bam", "1.0")
      .addHuman("john")
      .addHumanTask("a", "john")
      .addActivityExecutingTime(1000)
      .done();

    ProcessDefinition johnDef2 = 
      ProcessBuilder.createProcess("bam1", "1.0")
      .addHuman("john")
      .addSystemTask("task")
      .addHumanTask("a", "john")
      .addTransition("task", "a")
      .done();

    johnDef1 = getManagementAPI().deploy(getBusinessArchive(johnDef1));
    johnDef2 = getManagementAPI().deploy(getBusinessArchive(johnDef2));
    ProcessDefinitionUUID johnDef1UUID = johnDef1.getUUID();
    ProcessDefinitionUUID johnDef2UUID = johnDef2.getUUID();

    loginAs("john", "bpm");
    int openSteps = getBAMAPI().getNumberOfUserOverdueSteps();
    assertEquals(0, openSteps);

    ProcessInstanceUUID johnInst1UUID = getRuntimeAPI().instantiateProcess(johnDef1UUID);
    Thread.sleep(2000);

    openSteps = getBAMAPI().getNumberOfUserOverdueSteps();
    assertEquals(1, openSteps);

    ProcessInstanceUUID johnInst2UUID = getRuntimeAPI().instantiateProcess(johnDef2UUID);
    openSteps = getBAMAPI().getNumberOfUserOverdueSteps();
    assertEquals(1, openSteps);

    getRuntimeAPI().deleteProcessInstance(johnInst1UUID);
    getRuntimeAPI().deleteProcessInstance(johnInst2UUID);
    getManagementAPI().deleteProcess(johnDef1UUID);
    getManagementAPI().deleteProcess(johnDef2UUID);
  }

  public void testGetNumberOfOverdueSteps() throws Exception {
    ProcessDefinition johnDef = 
      ProcessBuilder.createProcess("bam", "1.0")
      .addHuman("john")
      .addHumanTask("a", "john")
      .addActivityExecutingTime(1000)
      .done();

    ProcessDefinition adminDef = 
      ProcessBuilder.createProcess("bam1", "1.0")
      .addHuman("admin")
      .addHumanTask("a", "admin")
      .addActivityExecutingTime(1000)
      .done();

    johnDef = getManagementAPI().deploy(getBusinessArchive(johnDef));
    adminDef = getManagementAPI().deploy(getBusinessArchive(adminDef));
    ProcessDefinitionUUID johnDefUUID = johnDef.getUUID();
    ProcessDefinitionUUID adminDefUUID = adminDef.getUUID();

    loginAs("john", "bpm");
    int openSteps = getBAMAPI().getNumberOfOverdueSteps();
    assertEquals(0, openSteps);

    ProcessInstanceUUID johnInstUUID = getRuntimeAPI().instantiateProcess(johnDefUUID);
    Thread.sleep(2000);
    openSteps = getBAMAPI().getNumberOfOverdueSteps();
    assertEquals(1, openSteps);

    ProcessInstanceUUID adminInstUUID = getRuntimeAPI().instantiateProcess(adminDefUUID);
    Thread.sleep(2000);
    openSteps = getBAMAPI().getNumberOfOverdueSteps();
    assertEquals(2, openSteps);

    getRuntimeAPI().deleteProcessInstance(johnInstUUID);
    getRuntimeAPI().deleteProcessInstance(adminInstUUID);
    getManagementAPI().deleteProcess(johnDefUUID);
    getManagementAPI().deleteProcess(adminDefUUID);
  }

  public void testGetNumberOfStepsAtRisk() throws Exception {
    Date dayBeforeYesterday = DateUtil.backTo(new Date(), -2);
    Date yesterday = DateUtil.backTo(new Date(), -1);

    ProcessDefinition johnDef = 
      ProcessBuilder.createProcess("bam", "1.0")
      .addHuman("john")
      .addHumanTask("a", "john")
      .addActivityExecutingTime(1000)
      .done();

    ProcessDefinition adminDef = 
      ProcessBuilder.createProcess("bam1", "1.0")
      .addHuman("admin")
      .addHumanTask("a", "admin")
      .addActivityExecutingTime(1000)
      .done();

    johnDef = getManagementAPI().deploy(getBusinessArchive(johnDef));
    adminDef = getManagementAPI().deploy(getBusinessArchive(adminDef));
    ProcessDefinitionUUID johnDefUUID = johnDef.getUUID();
    ProcessDefinitionUUID adminDefUUID = adminDef.getUUID();

    loginAs("john", "bpm");
    int openSteps = getBAMAPI().getNumberOfStepsAtRisk(1);
    assertEquals(0, openSteps);

    ProcessInstanceUUID johnInstUUID = getRuntimeAPI().instantiateProcess(johnDefUUID);
    getManagementAPI().deployJar("getExecJar.jar", Misc.generateJar(BTTFActivityInstance.class));

    openSteps = getBAMAPI().getNumberOfStepsAtRisk(1);
    assertEquals(1, openSteps);

    Set<TaskInstance> tasks = getQueryRuntimeAPI().getTasks(johnInstUUID);
    ActivityInstanceUUID johnTaskUUID = tasks.iterator().next().getUUID();

    BTTFActivityInstance john = new BTTFActivityInstance(johnTaskUUID, dayBeforeYesterday);
    getCommandAPI().execute(john);
    openSteps = getBAMAPI().getNumberOfStepsAtRisk(1);
    assertEquals(0, openSteps);

    john = new BTTFActivityInstance(johnTaskUUID, yesterday);
    getCommandAPI().execute(john);
    openSteps = getBAMAPI().getNumberOfStepsAtRisk(1);
    assertEquals(1, openSteps);

    ProcessInstanceUUID adminInstUUID = getRuntimeAPI().instantiateProcess(adminDefUUID);
    Thread.sleep(2000);
    openSteps = getBAMAPI().getNumberOfStepsAtRisk(1);
    assertEquals(1, openSteps);

    getManagementAPI().removeJar("getExecJar.jar");
    getRuntimeAPI().deleteProcessInstance(johnInstUUID);
    getRuntimeAPI().deleteProcessInstance(adminInstUUID);
    getManagementAPI().deleteProcess(johnDefUUID);
    getManagementAPI().deleteProcess(adminDefUUID);
  }

  public void testGetNumberOfUserStepsAtRisk() throws Exception {
    Date dayBeforeYesterday = DateUtil.backTo(new Date(), -2);
    Date yesterday = DateUtil.backTo(new Date(), -1);

    ProcessDefinition johnDef1 = 
      ProcessBuilder.createProcess("bam", "1.0")
      .addHuman("john")
      .addHumanTask("a", "john")
      .addActivityExecutingTime(1000)
      .done();

    ProcessDefinition johnDef2 = 
      ProcessBuilder.createProcess("bam1", "1.0")
      .addHuman("john")
      .addHumanTask("a", "john")
      .addActivityExecutingTime(1000)
      .done();

    ProcessDefinition adminDef = 
      ProcessBuilder.createProcess("admin", "1.0")
      .addHuman("admin")
      .addHumanTask("a", "admin")
      .addActivityExecutingTime(1000)
      .done();

    johnDef1 = getManagementAPI().deploy(getBusinessArchive(johnDef1));
    johnDef2 = getManagementAPI().deploy(getBusinessArchive(johnDef2));
    adminDef = getManagementAPI().deploy(getBusinessArchive(adminDef));
    ProcessDefinitionUUID johnDefUUID1 = johnDef1.getUUID();
    ProcessDefinitionUUID johnDefUUID2 = johnDef2.getUUID();
    ProcessDefinitionUUID adminDefUUID = adminDef.getUUID();

    loginAs("john", "bpm");
    int openSteps = getBAMAPI().getNumberOfUserStepsAtRisk(1);
    assertEquals(0, openSteps);

    ProcessInstanceUUID johnInstUUID1 = getRuntimeAPI().instantiateProcess(johnDefUUID1);
    getManagementAPI().deployJar("getExecJar.jar", Misc.generateJar(BTTFActivityInstance.class));

    openSteps = getBAMAPI().getNumberOfUserStepsAtRisk(1);
    assertEquals(1, openSteps);

    Set<TaskInstance> tasks = getQueryRuntimeAPI().getTasks(johnInstUUID1);
    ActivityInstanceUUID johnTaskUUID = tasks.iterator().next().getUUID();

    BTTFActivityInstance john = new BTTFActivityInstance(johnTaskUUID, dayBeforeYesterday);
    getCommandAPI().execute(john);
    openSteps = getBAMAPI().getNumberOfUserStepsAtRisk(1);
    assertEquals(0, openSteps);

    john = new BTTFActivityInstance(johnTaskUUID, yesterday);
    getCommandAPI().execute(john);
    openSteps = getBAMAPI().getNumberOfUserStepsAtRisk(1);
    assertEquals(1, openSteps);

    ProcessInstanceUUID johnInstUUID2 = getRuntimeAPI().instantiateProcess(johnDefUUID2);
    ProcessInstanceUUID adminInstUUID = getRuntimeAPI().instantiateProcess(adminDefUUID);
    Thread.sleep(2000);
    openSteps = getBAMAPI().getNumberOfUserStepsAtRisk(1);
    assertEquals(1, openSteps);

    getManagementAPI().removeJar("getExecJar.jar");
    getRuntimeAPI().deleteProcessInstance(johnInstUUID1);
    getRuntimeAPI().deleteProcessInstance(johnInstUUID2);
    getRuntimeAPI().deleteProcessInstance(adminInstUUID);
    getManagementAPI().deleteProcess(johnDefUUID1);
    getManagementAPI().deleteProcess(johnDefUUID2);
    getManagementAPI().deleteProcess(adminDefUUID);
  }

  public void testGetNumberOfPriorityUserOpenSteps() throws Exception {
    ProcessDefinition johnDef1 = 
      ProcessBuilder.createProcess("bam", "1.0")
      .addHuman("john")
      .addHumanTask("a", "john")
      .addActivityExecutingTime(1000)
      .addActivityPriority(1)
      .done();

    johnDef1 = getManagementAPI().deploy(getBusinessArchive(johnDef1));
    ProcessDefinitionUUID johnDefUUID1 = johnDef1.getUUID();

    loginAs("john", "bpm");
    int openSteps = getBAMAPI().getNumberOfUserOpenSteps(1);
    assertEquals(0, openSteps);

    ProcessInstanceUUID johnInstUUID1 = getRuntimeAPI().instantiateProcess(johnDefUUID1);
    openSteps = getBAMAPI().getNumberOfUserStepsAtRisk(1);
    assertEquals(1, openSteps);

    getRuntimeAPI().deleteProcessInstance(johnInstUUID1);
    getManagementAPI().deleteProcess(johnDefUUID1);
  }

  public void testGetNumberOfUserFinishedSteps() throws Exception {
    ProcessDefinition johnDef1 = 
      ProcessBuilder.createProcess("bam", "1.0")
      .addHuman("john")
      .addHumanTask("a", "john")
      .addActivityExecutingTime(1000)
      .addActivityPriority(1)
      .done();

    johnDef1 = getManagementAPI().deploy(getBusinessArchive(johnDef1));
    ProcessDefinitionUUID johnDefUUID1 = johnDef1.getUUID();

    Date today = new Date();
    Date yesterday = DateUtil.backTo(today, 1);

    loginAs("john", "bpm");
    int openSteps = getBAMAPI().getNumberOfUserFinishedSteps(1, yesterday);
    assertEquals(0, openSteps);

    ProcessInstanceUUID johnInstUUID1 = getRuntimeAPI().instantiateProcess(johnDefUUID1);
    executeTask(johnInstUUID1, "a");
    openSteps = getBAMAPI().getNumberOfUserFinishedSteps(1, yesterday);
    assertEquals(1, openSteps);

    getRuntimeAPI().deleteProcessInstance(johnInstUUID1);
    getManagementAPI().deleteProcess(johnDefUUID1);
  }

  public void testGetNumberOfFinishedSteps() throws Exception {
    ProcessDefinition johnDef1 = 
      ProcessBuilder.createProcess("bam", "1.0")
      .addHuman("john")
      .addHumanTask("a", "john")
      .addActivityExecutingTime(1000)
      .addActivityPriority(1)
      .done();

    johnDef1 = getManagementAPI().deploy(getBusinessArchive(johnDef1));
    ProcessDefinitionUUID johnDefUUID1 = johnDef1.getUUID();

    Date today = new Date();
    Date yesterday = DateUtil.backTo(today, 1);

    loginAs("john", "bpm");
    int openSteps = getBAMAPI().getNumberOfFinishedSteps(1, yesterday);
    assertEquals(0, openSteps);

    ProcessInstanceUUID johnInstUUID1 = getRuntimeAPI().instantiateProcess(johnDefUUID1);
    executeTask(johnInstUUID1, "a");
    openSteps = getBAMAPI().getNumberOfFinishedSteps(1, yesterday);
    assertEquals(1, openSteps);

    getRuntimeAPI().deleteProcessInstance(johnInstUUID1);
    getManagementAPI().deleteProcess(johnDefUUID1);
  }

  public void testGetNumberOfPriorityOpenSteps() throws Exception {
    ProcessDefinition parentProcess =
      ProcessBuilder.createProcess("parentProcess", "1.0")
      .addHuman(getLogin())
      .addHumanTask("p0", getLogin())
      .addActivityPriority(0)
      .addHumanTask("p1", getLogin())
      .addActivityPriority(1)
      .addHumanTask("p2", getLogin())
      .addActivityPriority(2)
      .addHumanTask("p3", getLogin())
      .addSystemTask("p4")
      .addActivityPriority(0)
      .addDecisionNode("end")
      .addJoinType(JoinType.AND)
      .addTransition("p0", "end")
      .addTransition("p1", "end")
      .addTransition("p2", "end")
      .addTransition("p3", "end")
      .addTransition("p4", "end")
      .done();

    parentProcess = getManagementAPI().deploy(getBusinessArchive(parentProcess));
    final ProcessDefinitionUUID processUUID = parentProcess.getUUID();

    int actual = getBAMAPI().getNumberOfOpenSteps(0);
    Assert.assertEquals(0, actual);
    actual = getBAMAPI().getNumberOfOpenSteps(1);
    Assert.assertEquals(0, actual);
    actual = getBAMAPI().getNumberOfOpenSteps(2);
    Assert.assertEquals(0, actual);

    // Start a case.
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);
    
    //wait for instance
    waitForInstance(5000, 50, instanceUUID, InstanceState.STARTED);
    
    actual = getBAMAPI().getNumberOfOpenSteps(0);
    Assert.assertEquals(2, actual);
    actual = getBAMAPI().getNumberOfOpenSteps(1);
    Assert.assertEquals(1, actual);
    actual = getBAMAPI().getNumberOfOpenSteps(2);
    Assert.assertEquals(1, actual);
    actual = getBAMAPI().getNumberOfOpenSteps(3);
    Assert.assertEquals("Priority 3 does not exist", 0, actual);

    getManagementAPI().deleteProcess(processUUID);
  }

  public void testUserTaskAtRisk() throws Exception {
    ProcessDefinition process =
      ProcessBuilder.createProcess("parentProcess", "1.0")
      .addHuman(getLogin())
      .addHumanTask("overdue", getLogin())
      .addActivityPriority(0)
      .addActivityExecutingTime(1000)
      .addHumanTask("onTrack", getLogin())
      .addActivityPriority(1)
      .addActivityExecutingTime(999999999)
      .addHumanTask("atRisk", getLogin())
      .addActivityPriority(2)
      .addActivityExecutingTime(10000).done();

    process = getManagementAPI().deploy(getBusinessArchive(process));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);
    Thread.sleep(1000);
    int stepsAtRisk = getBAMAPI().getNumberOfUserStepsAtRisk(0);
    assertEquals(1, stepsAtRisk);

    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testUserTaskAtRiskWithMultipleInstances() throws Exception {
    ProcessDefinition process =
      ProcessBuilder.createProcess("parentProcess", "1.0")
      .addHuman(getLogin())
      .addSystemTask("Start")
      .addHumanTask("overdue", getLogin())
      .addActivityPriority(0)
      .addActivityExecutingTime(1000)
      .addHumanTask("onTrack", getLogin())
      .addActivityPriority(1)
      .addActivityExecutingTime(259200000) // 72 * 60 * 60 * 1000
      .addHumanTask("atRisk", getLogin())
      .addActivityPriority(2)
      .addActivityExecutingTime(90 * 1000)
      .addTransition("Start", "overdue")
      .addTransition("Start", "onTrack")
      .addTransition("Start", "atRisk")
      .done();
    process = getManagementAPI().deploy(getBusinessArchive(process));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    getRuntimeAPI().instantiateProcess(processUUID);
    getRuntimeAPI().instantiateProcess(processUUID);

    Thread.sleep(1000);
    int stepsAtRisk = getBAMAPI().getNumberOfUserStepsAtRisk(0);
    assertEquals(2, stepsAtRisk);

    getRuntimeAPI().deleteAllProcessInstances(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testAllTaskAtRiskWithMultipleInstances() throws Exception {
    ProcessDefinition process =
      ProcessBuilder.createProcess("parentProcess", "1.0")
      .addHuman(getLogin())
      .addSystemTask("Start")
      .addHumanTask("overdue", getLogin())
      .addActivityPriority(0)
      .addActivityExecutingTime(1000)
      .addHumanTask("onTrack", getLogin())
      .addActivityPriority(1)
      .addActivityExecutingTime(259200000) // 72 * 60 * 60 * 1000
      .addHumanTask("atRisk", getLogin())
      .addActivityPriority(2)
      .addActivityExecutingTime(90 * 1000)
      .addTransition("Start", "overdue")
      .addTransition("Start", "onTrack")
      .addTransition("Start", "atRisk").done();

    process = getManagementAPI().deploy(getBusinessArchive(process));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    getRuntimeAPI().instantiateProcess(processUUID);
    getRuntimeAPI().instantiateProcess(processUUID);

    Thread.sleep(1000);
    int theStepsAtRisk = getBAMAPI().getNumberOfStepsAtRisk(0);
    Assert.assertEquals(2, theStepsAtRisk);

    getRuntimeAPI().deleteAllProcessInstances(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testUserTaskAtRiskWithoutPriority() throws Exception {
    ProcessDefinition process =
      ProcessBuilder.createProcess("parentProcess", "1.0")
      .addHuman(getLogin())
      .addHumanTask("overdue", getLogin())
      .addActivityExecutingTime(1000)
      .addHumanTask("onTrack", getLogin())
      .addActivityExecutingTime(999999999)
      .addHumanTask("atRisk", getLogin())
      .addActivityExecutingTime(10000)
      .done();

    process = getManagementAPI().deploy(getBusinessArchive(process));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    getRuntimeAPI().instantiateProcess(processUUID);
    Thread.sleep(1000);
    int theStepsAtRisk = getBAMAPI().getNumberOfUserStepsAtRisk(0);
    assertEquals(1, theStepsAtRisk);

    getRuntimeAPI().deleteAllProcessInstances(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testTaskAtRiskWithoutEstimatedTime() throws Exception {
    ProcessDefinition process =
      ProcessBuilder.createProcess("parentProcess", "1.0")
      .addHuman(getLogin())
      .addHumanTask("overdue", getLogin())
      .addHumanTask("onTrack", getLogin())
      .addHumanTask("atRisk", getLogin())
      .done();
    process = getManagementAPI().deploy(getBusinessArchive(process));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    getRuntimeAPI().instantiateProcess(processUUID);
    int theStepsAtRisk = getBAMAPI().getNumberOfUserStepsAtRisk(0);
    assertEquals(0, theStepsAtRisk);

    getRuntimeAPI().deleteAllProcessInstances(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testUserTaskOverdue() throws Exception {
    ProcessDefinition process = 
      ProcessBuilder.createProcess("parentProcess", "1.0")
      .addHuman(getLogin())
      .addHumanTask("overdue", getLogin())
      .addActivityPriority(0)
      .addActivityExecutingTime(1000)
      .addHumanTask("onTrack", getLogin())
      .addActivityPriority(1)
      .addActivityExecutingTime(999999999)
      .addHumanTask("atRisk", getLogin())
      .addActivityPriority(2)
      .addActivityExecutingTime(10000)
      .done();
    process = getManagementAPI().deploy(getBusinessArchive(process));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    getRuntimeAPI().instantiateProcess(processUUID);
    Thread.sleep(2000);
    int theStepsOverdue = getBAMAPI().getNumberOfUserOverdueSteps();
    assertEquals(1, theStepsOverdue);

    getRuntimeAPI().deleteAllProcessInstances(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }


  public void testAllTaskOverdue() throws Exception {
    ProcessDefinition process =
      ProcessBuilder.createProcess("parentProcess", "1.0")
      .addHuman(getLogin())
      .addHumanTask("overdue", getLogin())
      .addActivityPriority(0)
      .addActivityExecutingTime(1000)
      .addHumanTask("onTrack", getLogin())
      .addActivityPriority(1)
      .addActivityExecutingTime(999999999)
      .addHumanTask("atRisk", getLogin())
      .addActivityPriority(2)
      .addActivityExecutingTime(10000)
      .done();
    process = getManagementAPI().deploy(getBusinessArchive(process));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    getRuntimeAPI().instantiateProcess(processUUID);
    Thread.sleep(2000);
    int theStepsOverdue = getBAMAPI().getNumberOfOverdueSteps();
    assertEquals(1, theStepsOverdue);

    getRuntimeAPI().deleteAllProcessInstances(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testTaskOverdueWithoutPriority() throws Exception {
    ProcessDefinition process =
      ProcessBuilder.createProcess("parentProcess", "1.0")
      .addHuman(getLogin())
      .addHumanTask("overdue", getLogin())
      .addActivityExecutingTime(1000)
      .addHumanTask("onTrack", getLogin())
      .addActivityExecutingTime(999999999)
      .addHumanTask("atRisk", getLogin())
      .addActivityExecutingTime(10000)
      .done();
    process = getManagementAPI().deploy(getBusinessArchive(process));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    getRuntimeAPI().instantiateProcess(processUUID);
    Thread.sleep(2000);
    int theStepsOverdue = getBAMAPI().getNumberOfUserOverdueSteps();
    assertEquals(1, theStepsOverdue);

    getRuntimeAPI().deleteAllProcessInstances(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testTaskOverDueWithoutEstimatedTime() throws Exception {
    ProcessDefinition process =
      ProcessBuilder.createProcess("parentProcess", "1.0")
      .addHuman(getLogin())
      .addHumanTask("overdue", getLogin())
      .addHumanTask("onTrack", getLogin())
      .addHumanTask("atRisk", getLogin())
      .done();
    process = getManagementAPI().deploy(getBusinessArchive(process));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    getRuntimeAPI().instantiateProcess(processUUID);
    int theStepsOverDue = getBAMAPI().getNumberOfUserOverdueSteps();
    assertEquals(0, theStepsOverDue);

    getRuntimeAPI().deleteAllProcessInstances(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testAllTaskOpen() throws Exception {
    ProcessDefinition process =
      ProcessBuilder.createProcess("parentProcess", "1.0")
      .addHuman(getLogin())
      .addHumanTask("overdue", getLogin())
      .addActivityPriority(0)
      .addActivityExecutingTime(1000)
      .addHumanTask("onTrack", getLogin())
      .addActivityPriority(1)
      .addActivityExecutingTime(999999999)
      .addHumanTask("atRisk", getLogin())
      .addActivityPriority(2)
      .addActivityExecutingTime(10000)
      .done();
    process = getManagementAPI().deploy(getBusinessArchive(process));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    getRuntimeAPI().instantiateProcess(processUUID);
    Thread.sleep(1000);
    int theStepsOpen = getBAMAPI().getNumberOfOpenSteps();
    assertEquals(3, theStepsOpen);

    getRuntimeAPI().deleteAllProcessInstances(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testExpectingEndDate() throws Exception {
    ProcessDefinition process =
      ProcessBuilder.createProcess("expect", "2.1")
      .addHuman(getLogin())
      .addHumanTask("lumi", getLogin())
      .addActivityExecutingTime(10000)
      .done();

    process = getManagementAPI().deploy(getBusinessArchive(process));
    getRuntimeAPI().instantiateProcess(process.getUUID());

    Collection<TaskInstance> tasks = getQueryRuntimeAPI().getTaskList(getLogin(), ActivityState.READY);
    TaskInstance task = tasks.iterator().next();
    assertNotNull(task.getExpectedEndDate());

    getRuntimeAPI().startTask(task.getUUID(), true);
    tasks = getQueryRuntimeAPI().getTaskList(getLogin(), ActivityState.EXECUTING);
    task = tasks.iterator().next();
    assertNotNull(task.getExpectedEndDate());

    getRuntimeAPI().finishTask(task.getUUID(), true);
    getManagementAPI().deleteProcess(process.getUUID());
  }
  
  public void testGetProcessInstancesDuration() throws Exception {
    ProcessDefinition process =
      ProcessBuilder.createProcess("pidfi", "1.0")
      .addHuman(getLogin())
      .addHumanTask("theTask", getLogin())      
      .done();
    
    process = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process));
    
    Date since = new Date();
    int numberOfInstances = 2;    
    List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();
    for (int i = 0; i < numberOfInstances; i++) {
      instanceUUIDs.add(getRuntimeAPI().instantiateProcess(process.getUUID()));
    }
    for (int i = 0; i < numberOfInstances; i++) {
      executeTask(instanceUUIDs.get(i), "theTask");
    }
    Date until = new Date();
    
    List<Long> exptectedDurations = new ArrayList<Long>();
    
    for (int i = 0; i < numberOfInstances; i++) {
      LightProcessInstance processInstance = getQueryRuntimeAPI().getLightProcessInstance(instanceUUIDs.get(i));
      exptectedDurations.add(processInstance.getEndedDate().getTime() - processInstance.getStartedDate().getTime());
    }
    
    List<Long> retrivedDurations = getBAMAPI().getProcessInstancesDuration(since, until);
    assertEquals(numberOfInstances, retrivedDurations.size());
    Collections.sort(exptectedDurations);
    checkDurationList(exptectedDurations, retrivedDurations);
    
    getManagementAPI().deleteProcess(process.getUUID());
  }
  
  public void testGetProcessInstancesDurationWithNoFinishedInstances() throws Exception {
    ProcessDefinition process =
      ProcessBuilder.createProcess("pidfi", "1.1")
      .addHuman(getLogin())
      .addHumanTask("theTask", getLogin())      
      .done();
    
    process = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process));
    
    Date since = new Date();
    int numberOfInstances = 2;    
    List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();
    for (int i = 0; i < numberOfInstances; i++) {
      instanceUUIDs.add(getRuntimeAPI().instantiateProcess(process.getUUID()));
    }
    
    List<Long> retrivedDurations = getBAMAPI().getProcessInstancesDuration(since, new Date());
    assertEquals(0, retrivedDurations.size());    
    
    getManagementAPI().deleteProcess(process.getUUID());
  }
  
  public void testGetProcessInstancesDurationWithLimitedInterval() throws Exception {
    ProcessDefinition process =
      ProcessBuilder.createProcess("pidli", "1.0")
      .addHuman(getLogin())
      .addHumanTask("theTask", getLogin())      
      .done();
    
    process = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process));
    
    Date since = new Date();
    int numberOfInstances = 2;    
    List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();
    for (int i = 0; i < numberOfInstances; i++) {
      instanceUUIDs.add(getRuntimeAPI().instantiateProcess(process.getUUID()));
    }
    executeTask(instanceUUIDs.get(0), "theTask");
    Date until = new Date(); 
    
    for (int i = 1; i < numberOfInstances; i++) {
      executeTask(instanceUUIDs.get(i), "theTask");
    }
    
    
    List<Long> exptectedDurations = new ArrayList<Long>();
    LightProcessInstance processInstance = getQueryRuntimeAPI().getLightProcessInstance(instanceUUIDs.get(0));
    exptectedDurations.add(processInstance.getEndedDate().getTime() - processInstance.getStartedDate().getTime());    
    
    List<Long> retrivedDurations = getBAMAPI().getProcessInstancesDuration(since, until);
    assertEquals(1, retrivedDurations.size());    
    checkDurationList(exptectedDurations, retrivedDurations);
    
    getManagementAPI().deleteProcess(process.getUUID());
  }
  
  public void testGetProcessInstancesDurationFromProcessUUID() throws Exception {
    ProcessDefinition processA =
      ProcessBuilder.createProcess("processA", "1.0")
      .addHuman(getLogin())
      .addHumanTask("theTask", getLogin())      
      .done();
    
    ProcessDefinition processB =
      ProcessBuilder.createProcess("processB", "1.0")
      .addHuman(getLogin())
      .addHumanTask("theTask", getLogin())      
      .done();
    
    processA = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(processA));
    processB = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(processB));
    
    //beginning of interval 
    Date since = new Date();
    
    //create instances
    int numberOfInstances = 2;    
    List<ProcessInstanceUUID> instanceUUIDsA = new ArrayList<ProcessInstanceUUID>();
    List<ProcessInstanceUUID> instanceUUIDsB = new ArrayList<ProcessInstanceUUID>();
    for (int i = 0; i < numberOfInstances; i++) {
      instanceUUIDsA.add(getRuntimeAPI().instantiateProcess(processA.getUUID()));
      instanceUUIDsB.add(getRuntimeAPI().instantiateProcess(processB.getUUID()));
    }
    
    //execute tasks
    for (int i = 0; i < numberOfInstances; i++) {
      executeTask(instanceUUIDsA.get(i), "theTask");
      executeTask(instanceUUIDsB.get(i), "theTask");
    }
    
    //end of the interval
    Date until = new Date();
    
    List<Long> exptectedDurations = new ArrayList<Long>();
    
    for (int i = 0; i < numberOfInstances; i++) {
      LightProcessInstance processInstance = getQueryRuntimeAPI().getLightProcessInstance(instanceUUIDsA.get(i));
      exptectedDurations.add(processInstance.getEndedDate().getTime() - processInstance.getStartedDate().getTime());
    }
    
    List<Long> retrivedDurations = getBAMAPI().getProcessInstancesDuration(processA.getUUID(), since, until);
    assertEquals(numberOfInstances, retrivedDurations.size());
    Collections.sort(exptectedDurations);
    checkDurationList(exptectedDurations, retrivedDurations);
    
    getManagementAPI().deleteProcess(processA.getUUID());
    getManagementAPI().deleteProcess(processB.getUUID());
  }
  
  public void testGetProcessInstancesDurationFromProcessUUIDWithLimitedInterval() throws Exception {
    ProcessDefinition processA =
      ProcessBuilder.createProcess("processA", "1.0")
      .addHuman(getLogin())
      .addHumanTask("theTask", getLogin())      
      .done();
    
    ProcessDefinition processB =
      ProcessBuilder.createProcess("processB", "1.0")
      .addHuman(getLogin())
      .addHumanTask("theTask", getLogin())      
      .done();
    
    processA = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(processA));
    processB = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(processB));
    
    //beginning of interval 
    Date since = new Date();
    
    //create instances
    int numberOfInstances = 2;    
    List<ProcessInstanceUUID> instanceUUIDsA = new ArrayList<ProcessInstanceUUID>();
    List<ProcessInstanceUUID> instanceUUIDsB = new ArrayList<ProcessInstanceUUID>();
    for (int i = 0; i < numberOfInstances; i++) {
      instanceUUIDsA.add(getRuntimeAPI().instantiateProcess(processA.getUUID()));
      instanceUUIDsB.add(getRuntimeAPI().instantiateProcess(processB.getUUID()));
    }
    
    executeTask(instanceUUIDsA.get(0), "theTask");
    executeTask(instanceUUIDsB.get(0), "theTask");
    
    //end of the interval
    Date until = new Date();
    
    //execute tasks
    for (int i = 1; i < numberOfInstances; i++) {
      executeTask(instanceUUIDsA.get(i), "theTask");
      executeTask(instanceUUIDsB.get(i), "theTask");
    }
    
    List<Long> exptectedDurations = new ArrayList<Long>();
        
    LightProcessInstance processInstance = getQueryRuntimeAPI().getLightProcessInstance(instanceUUIDsA.get(0));
    exptectedDurations.add(processInstance.getEndedDate().getTime() - processInstance.getStartedDate().getTime());
    
    
    List<Long> retrivedDurations = getBAMAPI().getProcessInstancesDuration(processA.getUUID(), since, until);
    assertEquals(1, retrivedDurations.size());    
    checkDurationList(exptectedDurations, retrivedDurations);
    
    getManagementAPI().deleteProcess(processA.getUUID());
    getManagementAPI().deleteProcess(processB.getUUID());
  }
  
  private void checkDurationList(List<Long> expectedDurations, List<Long> actualDuration) {
    assertEquals(expectedDurations, actualDuration);
    for (Long duration : actualDuration) {
      assertTrue(duration >= 0);
    }
  }
  
  public void testGetProcessInstancesDurationFromProcessUUIDs() throws Exception {
    int numberOfProcess = 3;
    List<ProcessDefinition> processes = new ArrayList<ProcessDefinition>();
    Set<ProcessDefinitionUUID> processUUIDs = new HashSet<ProcessDefinitionUUID>();
    for (int i = 0; i < numberOfProcess; i++) {
      ProcessDefinition process =
        ProcessBuilder.createProcess("process" + i, "1.0")
        .addHuman(getLogin())
        .addHumanTask("theTask", getLogin())      
        .done();
      
      process = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process));      
      processes.add(process);
      processUUIDs.add(process.getUUID());
    }
    
    //beginning of interval 
    Date since = new Date();
    
    //create instances
    int numberOfInstances = 2;    
    ProcessInstanceUUID [][] processInstancesUUIDs = new ProcessInstanceUUID [numberOfProcess] [numberOfInstances];
    
    for (int i = 0; i < numberOfProcess; i++) {
      for (int j = 0; j < numberOfInstances; j++) {
        processInstancesUUIDs[i][j] = getRuntimeAPI().instantiateProcess(processes.get(i).getUUID());        
      }
    }
        
    //execute tasks
    for (int i = 0; i < numberOfProcess; i++) {
      for (int j = 0; j < numberOfInstances; j++) {
        executeTask(processInstancesUUIDs[i][j], "theTask");        
      }
    }
    
    //end of the interval
    Date until = new Date();

    
    List<Long> exptectedDurations = new ArrayList<Long>();
    for (int i = 1; i < numberOfProcess; i++) {
      for (int j = 0; j < numberOfInstances; j++) {
        LightProcessInstance processInstance = getQueryRuntimeAPI().getLightProcessInstance(processInstancesUUIDs[i][j]);
        exptectedDurations.add(processInstance.getEndedDate().getTime()- processInstance.getStartedDate().getTime());
      }
    }
    
    Set<ProcessDefinitionUUID> fromProcesses = new HashSet<ProcessDefinitionUUID>();
    fromProcesses.add(processes.get(1).getUUID());
    fromProcesses.add(processes.get(2).getUUID());
    
    List<Long> retrivedDurations = getBAMAPI().getProcessInstancesDuration(fromProcesses, since, until);
    assertEquals(numberOfInstances * 2, retrivedDurations.size()); 
    Collections.sort(exptectedDurations);
    checkDurationList(exptectedDurations, retrivedDurations);
    
    getManagementAPI().delete(processUUIDs);
  }
  
  public void testGetProcessInstancesDurationFromProcessUUIDsWithLimitedInterval() throws Exception {
    int numberOfProcess = 3;
    List<ProcessDefinition> processes = new ArrayList<ProcessDefinition>();
    Set<ProcessDefinitionUUID> processUUIDs = new HashSet<ProcessDefinitionUUID>();
    for (int i = 0; i < numberOfProcess; i++) {
      ProcessDefinition process =
        ProcessBuilder.createProcess("process" + i, "1.0")
        .addHuman(getLogin())
        .addHumanTask("theTask", getLogin())      
        .done();
      
      process = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process));      
      processes.add(process);
      processUUIDs.add(process.getUUID());
    }
    
    //beginning of interval 
    Date since = new Date();
    
    //create instances
    int numberOfInstances = 2;    
    ProcessInstanceUUID [][] processInstancesUUIDs = new ProcessInstanceUUID [numberOfProcess] [numberOfInstances];
    
    for (int i = 0; i < numberOfProcess; i++) {
      for (int j = 0; j < numberOfInstances; j++) {
        processInstancesUUIDs[i][j] = getRuntimeAPI().instantiateProcess(processes.get(i).getUUID());        
      }
    }
    
    for (int i = 0; i < numberOfProcess; i++) {
      executeTask(processInstancesUUIDs[i][0], "theTask");
    }
    
    //end of the interval
    Date until = new Date();
    
    //execute tasks
    for (int i = 0; i < numberOfProcess; i++) {
      for (int j = 1; j < numberOfInstances; j++) {
        executeTask(processInstancesUUIDs[i][j], "theTask");        
      }
    }
    
    List<Long> exptectedDurations = new ArrayList<Long>();
    for (int i = 1; i < numberOfProcess; i++) {      
        LightProcessInstance processInstance = getQueryRuntimeAPI().getLightProcessInstance(processInstancesUUIDs[i][0]);
        exptectedDurations.add(processInstance.getEndedDate().getTime()- processInstance.getStartedDate().getTime());      
    }
    
    Set<ProcessDefinitionUUID> fromProcesses = new HashSet<ProcessDefinitionUUID>();
    fromProcesses.add(processes.get(1).getUUID());
    fromProcesses.add(processes.get(2).getUUID());
    
    List<Long> retrivedDurations = getBAMAPI().getProcessInstancesDuration(fromProcesses, since, until);
    assertEquals(numberOfProcess - 1, retrivedDurations.size());   
    Collections.sort(exptectedDurations);
    checkDurationList(exptectedDurations, retrivedDurations);
    
    getManagementAPI().delete(processUUIDs);
  }
  
  public void testGetActivityInstancesExecutionTime() throws Exception {
    ProcessDefinition process =
      ProcessBuilder.createProcess("aiet", "1.0")
      .addHuman(getLogin())
      .addHumanTask("aTask", getLogin())      
      .done();
    
    process = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process));
    
    Date since = new Date();
    int numberOfInstances = 2;    
    List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();
    for (int i = 0; i < numberOfInstances; i++) {
      instanceUUIDs.add(getRuntimeAPI().instantiateProcess(process.getUUID()));
    }
        
    for (int i = 0; i < numberOfInstances; i++) {
      executeTask(instanceUUIDs.get(i), "aTask");
    }        
    Date until = new Date();
    
    List<Long> exptectedExecutionTimes = new ArrayList<Long>();
    for (int i = 0; i < numberOfInstances; i++) {
      Set<LightActivityInstance> activityInstances = getQueryRuntimeAPI().getLightActivityInstances(instanceUUIDs.get(i), "aTask");
      assertEquals(1, activityInstances.size());
      LightActivityInstance activityInstance = activityInstances.iterator().next();
      exptectedExecutionTimes.add(activityInstance.getEndedDate().getTime() - activityInstance.getStartedDate().getTime());
    }
    
    List<Long> retrivedExecutionTimess = getBAMAPI().getActivityInstancesExecutionTime(since, until);
    assertEquals(numberOfInstances, retrivedExecutionTimess.size());
    Collections.sort(exptectedExecutionTimes);
    checkDurationList(exptectedExecutionTimes, retrivedExecutionTimess);
    
    getManagementAPI().deleteProcess(process.getUUID());
  }
  
  public void testGetActivityInstancesExecutionTimeWithLimitedInterval() throws Exception {
    ProcessDefinition process =
      ProcessBuilder.createProcess("aiet", "1.0")
      .addHuman(getLogin())
      .addHumanTask("aTask", getLogin())      
      .done();
    
    process = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process));
    
    Date since = new Date();
    int numberOfInstances = 2;    
    List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();
    for (int i = 0; i < numberOfInstances; i++) {
      instanceUUIDs.add(getRuntimeAPI().instantiateProcess(process.getUUID()));
    }
    
    executeTask(instanceUUIDs.get(0), "aTask");
    
    Date until = new Date();
    
    for (int i = 1; i < numberOfInstances; i++) {
      executeTask(instanceUUIDs.get(i), "aTask");
    }
    
    List<Long> exptectedExecutionTimes = new ArrayList<Long>();
    
    Set<LightActivityInstance> activityInstances = getQueryRuntimeAPI().getLightActivityInstances(instanceUUIDs.get(0), "aTask");
    assertEquals(1, activityInstances.size());
    LightActivityInstance activityInstance = activityInstances.iterator().next();
    exptectedExecutionTimes.add(activityInstance.getEndedDate().getTime() - activityInstance.getStartedDate().getTime());
    
    
    List<Long> retrivedExecutionTimess = getBAMAPI().getActivityInstancesExecutionTime(since, until);
    assertEquals(1, retrivedExecutionTimess.size());    
    checkDurationList(exptectedExecutionTimes, retrivedExecutionTimess);
    
    getManagementAPI().deleteProcess(process.getUUID());
  }
  
  public void testGetActivityInstancesExecutionTimeFromProcessUUID () throws Exception {
    int numberOfProcess = 3;
    List<ProcessDefinition> processes = new ArrayList<ProcessDefinition>();
    Set<ProcessDefinitionUUID> processUUIDs = new HashSet<ProcessDefinitionUUID>();
    for (int i = 0; i < numberOfProcess; i++) {
      ProcessDefinition process =
        ProcessBuilder.createProcess("processAiet" + i, "2.0")
        .addHuman(getLogin())
        .addHumanTask("theTask", getLogin())      
        .done();
      
      process = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process));      
      processes.add(process);
      processUUIDs.add(process.getUUID());
    }
    
    //beginning of interval 
    Date since = new Date();
    
    //create instances
    int numberOfInstances = 2;    
    ProcessInstanceUUID [][] processInstancesUUIDs = new ProcessInstanceUUID [numberOfProcess] [numberOfInstances];
    
    for (int i = 0; i < numberOfProcess; i++) {
      for (int j = 0; j < numberOfInstances; j++) {
        processInstancesUUIDs[i][j] = getRuntimeAPI().instantiateProcess(processes.get(i).getUUID());        
      }
    }
        
    //execute tasks
    for (int i = 0; i < numberOfProcess; i++) {
      for (int j = 0; j < numberOfInstances; j++) {
        executeTask(processInstancesUUIDs[i][j], "theTask");        
      }
    }
    
    //end of the interval
    Date until = new Date();
    
    List<Long> exptectedExecutionTimes = new ArrayList<Long>();
    
    for (int j = 0; j < numberOfInstances; j++) {
      Set<LightActivityInstance> activityInstances = getQueryRuntimeAPI().getLightActivityInstances(processInstancesUUIDs[0][j], "theTask");
      assertEquals(1, activityInstances.size());
      LightActivityInstance activityInstance = activityInstances.iterator().next();
      exptectedExecutionTimes.add(activityInstance.getEndedDate().getTime() - activityInstance.getStartedDate().getTime());
    }        
    
    List<Long> retrivedExecutionTimes = getBAMAPI().getActivityInstancesExecutionTime(processes.get(0).getUUID(), since, until);
    assertEquals(numberOfInstances, retrivedExecutionTimes.size()); 
    Collections.sort(exptectedExecutionTimes);
    checkDurationList(exptectedExecutionTimes, retrivedExecutionTimes);
    
    getManagementAPI().delete(processUUIDs);
  }
  
  public void testGetActivityInstancesExecutionTimeFromProcessUUIDWithLimitedInterval () throws Exception {
    int numberOfProcess = 3;
    List<ProcessDefinition> processes = new ArrayList<ProcessDefinition>();
    Set<ProcessDefinitionUUID> processUUIDs = new HashSet<ProcessDefinitionUUID>();
    for (int i = 0; i < numberOfProcess; i++) {
      ProcessDefinition process =
        ProcessBuilder.createProcess("processAiet" + i, "2.0")
        .addHuman(getLogin())
        .addHumanTask("theTask", getLogin())      
        .done();
      
      process = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process));      
      processes.add(process);
      processUUIDs.add(process.getUUID());
    }
    
    //beginning of interval 
    Date since = new Date();
    
    //create instances
    int numberOfInstances = 2;    
    ProcessInstanceUUID [][] processInstancesUUIDs = new ProcessInstanceUUID [numberOfProcess] [numberOfInstances];
    
    for (int i = 0; i < numberOfProcess; i++) {
      for (int j = 0; j < numberOfInstances; j++) {
        processInstancesUUIDs[i][j] = getRuntimeAPI().instantiateProcess(processes.get(i).getUUID());        
      }
    }
    
    for (int i = 0; i < numberOfProcess; i++) {
      executeTask(processInstancesUUIDs[i][0], "theTask");
    }
    
    //end of the interval
    Date until = new Date();
        
    //execute tasks
    for (int i = 0; i < numberOfProcess; i++) {
      for (int j = 1; j < numberOfInstances; j++) {
        executeTask(processInstancesUUIDs[i][j], "theTask");        
      }
    }
    
    List<Long> exptectedExecutionTimes = new ArrayList<Long>();    
    
    Set<LightActivityInstance> activityInstances = getQueryRuntimeAPI().getLightActivityInstances(processInstancesUUIDs[0][0], "theTask");
    assertEquals(1, activityInstances.size());
    LightActivityInstance activityInstance = activityInstances.iterator().next();
    exptectedExecutionTimes.add(activityInstance.getEndedDate().getTime() - activityInstance.getStartedDate().getTime());
    
    List<Long> retrivedExecutionTimes = getBAMAPI().getActivityInstancesExecutionTime(processes.get(0).getUUID(), since, until);
    assertEquals(1, retrivedExecutionTimes.size());    
    checkDurationList(exptectedExecutionTimes, retrivedExecutionTimes);
    
    getManagementAPI().delete(processUUIDs);
  }  
  
  public void testGetActivityInstancesExecutionTimeFromProcessUUIDs() throws Exception {
    int numberOfProcess = 3;
    List<ProcessDefinition> processes = new ArrayList<ProcessDefinition>();
    Set<ProcessDefinitionUUID> processUUIDs = new HashSet<ProcessDefinitionUUID>();
    for (int i = 0; i < numberOfProcess; i++) {
      ProcessDefinition process =
        ProcessBuilder.createProcess("processAiet" + i, "3.0")
        .addHuman(getLogin())
        .addHumanTask("theTask", getLogin())      
        .done();
      
      process = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process));      
      processes.add(process);
      processUUIDs.add(process.getUUID());
    }
    
    //beginning of interval 
    Date since = new Date();
    
    //create instances
    int numberOfInstances = 2;    
    ProcessInstanceUUID [][] processInstancesUUIDs = new ProcessInstanceUUID [numberOfProcess] [numberOfInstances];
    
    for (int i = 0; i < numberOfProcess; i++) {
      for (int j = 0; j < numberOfInstances; j++) {
        processInstancesUUIDs[i][j] = getRuntimeAPI().instantiateProcess(processes.get(i).getUUID());        
      }
    }
        
    //execute tasks
    for (int i = 0; i < numberOfProcess; i++) {
      for (int j = 0; j < numberOfInstances; j++) {
        executeTask(processInstancesUUIDs[i][j], "theTask");        
      }
    }
    
    //end of the interval
    Date until = new Date();
    
    List<Long> exptectedExecutionTimes = new ArrayList<Long>();
    for (int i = 1; i < numberOfProcess; i++) {
      for (int j = 0; j < numberOfInstances; j++) {
        Set<LightActivityInstance> activityInstances = getQueryRuntimeAPI().getLightActivityInstances(processInstancesUUIDs[i][j], "theTask");
        assertEquals(1, activityInstances.size());
        LightActivityInstance activityInstance = activityInstances.iterator().next();
        exptectedExecutionTimes.add(activityInstance.getEndedDate().getTime() - activityInstance.getStartedDate().getTime());
      }
    }    
    
    Set<ProcessDefinitionUUID> fromProcesses = new HashSet<ProcessDefinitionUUID>();
    fromProcesses.add(processes.get(1).getUUID());
    fromProcesses.add(processes.get(2).getUUID());
    
    List<Long> retrivedExecutionTimes = getBAMAPI().getActivityInstancesExecutionTimeFromProcessUUIDs(fromProcesses, since, until);
    assertEquals(numberOfInstances * 2, retrivedExecutionTimes.size());
    Collections.sort(exptectedExecutionTimes);
    checkDurationList(exptectedExecutionTimes, retrivedExecutionTimes);
    
    getManagementAPI().delete(processUUIDs);
  }  
  
  public void testGetActivityInstancesExecutionTimeFromProcessUUIDsWithLimitedInterval() throws Exception {
    int numberOfProcess = 3;
    List<ProcessDefinition> processes = new ArrayList<ProcessDefinition>();
    Set<ProcessDefinitionUUID> processUUIDs = new HashSet<ProcessDefinitionUUID>();
    for (int i = 0; i < numberOfProcess; i++) {
      ProcessDefinition process =
        ProcessBuilder.createProcess("processAiet" + i, "3.0")
        .addHuman(getLogin())
        .addHumanTask("theTask", getLogin())      
        .done();
      
      process = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process));      
      processes.add(process);
      processUUIDs.add(process.getUUID());
    }
    
    //beginning of interval 
    Date since = new Date();
    
    //create instances
    int numberOfInstances = 2;    
    ProcessInstanceUUID [][] processInstancesUUIDs = new ProcessInstanceUUID [numberOfProcess] [numberOfInstances];
    
    for (int i = 0; i < numberOfProcess; i++) {
      for (int j = 0; j < numberOfInstances; j++) {
        processInstancesUUIDs[i][j] = getRuntimeAPI().instantiateProcess(processes.get(i).getUUID());        
      }
    }
    
    for (int i = 0; i < numberOfProcess; i++) {
      executeTask(processInstancesUUIDs[i][0], "theTask");
    }
    
    //end of the interval
    Date until = new Date();
        
    //execute tasks
    for (int i = 0; i < numberOfProcess; i++) {
      for (int j = 1; j < numberOfInstances; j++) {
        executeTask(processInstancesUUIDs[i][j], "theTask");        
      }
    }    
    
    List<Long> exptectedExecutionTimes = new ArrayList<Long>();
    for (int i = 1; i < numberOfProcess; i++) {      
      Set<LightActivityInstance> activityInstances = getQueryRuntimeAPI().getLightActivityInstances(processInstancesUUIDs[i][0], "theTask");
      assertEquals(1, activityInstances.size());
      LightActivityInstance activityInstance = activityInstances.iterator().next();
      exptectedExecutionTimes.add(activityInstance.getEndedDate().getTime() - activityInstance.getStartedDate().getTime());
    }   
    
    Set<ProcessDefinitionUUID> fromProcesses = new HashSet<ProcessDefinitionUUID>();
    fromProcesses.add(processes.get(1).getUUID());
    fromProcesses.add(processes.get(2).getUUID());
    
    List<Long> retrivedExecutionTimes = getBAMAPI().getActivityInstancesExecutionTimeFromProcessUUIDs(fromProcesses, since, until);
    assertEquals(numberOfProcess - 1, retrivedExecutionTimes.size());
    Collections.sort(exptectedExecutionTimes);
    checkDurationList(exptectedExecutionTimes, retrivedExecutionTimes);
    
    getManagementAPI().delete(processUUIDs);
  }
  
  public void testGetActivityInstancesExecutionTimeFromActivityDefUUID () throws Exception {
    ProcessDefinition process =
      ProcessBuilder.createProcess("processaiet", "4.0")      
      .addHuman(getLogin())
      .addHumanTask("aTask", getLogin())
      .addHumanTask("anotherTask", getLogin())
      .addTransition("aTask", "anotherTask")
      .done();
    
    process = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process));
    
    //start interval
    Date since = new Date();
    int numberOfInstances = 3;    
    List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();
    for (int i = 0; i < numberOfInstances; i++) {
      instanceUUIDs.add(getRuntimeAPI().instantiateProcess(process.getUUID()));
    }
    for (int i = 0; i < numberOfInstances; i++) {
      executeTask(instanceUUIDs.get(i), "aTask");
      executeTask(instanceUUIDs.get(i), "anotherTask");
    }
    //end interval
    Date until = new Date();
    
    List<Long> exptectedExecutionTimes = new ArrayList<Long>();    
    for (int i = 0; i < numberOfInstances; i++) {
      Set<LightActivityInstance> activityInstances = getQueryRuntimeAPI().getLightActivityInstances(instanceUUIDs.get(i), "anotherTask");
      assertEquals(1, activityInstances.size());
      LightActivityInstance activityInstance = activityInstances.iterator().next();
      exptectedExecutionTimes.add(activityInstance.getEndedDate().getTime() - activityInstance.getStartedDate().getTime());
    }
    
    ActivityDefinitionUUID activityUUID = getQueryDefinitionAPI().getProcessActivity(process.getUUID(), "anotherTask").getUUID();
    
    List<Long> retrivedExecutionTimes = getBAMAPI().getActivityInstancesExecutionTime(activityUUID, since, until);
    assertEquals(numberOfInstances, retrivedExecutionTimes.size());
    Collections.sort(exptectedExecutionTimes);
    checkDurationList(exptectedExecutionTimes, retrivedExecutionTimes);
    
    getManagementAPI().deleteProcess(process.getUUID());
  }
  
  public void testGetActivityInstancesExecutionTimeFromActivityDefUUIDWithLimitedInterval () throws Exception {
    ProcessDefinition process =
      ProcessBuilder.createProcess("processaiet", "4.1")
      .addHuman(getLogin())
      .addHumanTask("aTask", getLogin())
      .addHumanTask("anotherTask", getLogin())
      .addTransition("aTask", "anotherTask")
      .done();
    
    process = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process));
    
    //start interval
    Date since = new Date();
    int numberOfInstances = 3;    
    List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();
    for (int i = 0; i < numberOfInstances; i++) {
      instanceUUIDs.add(getRuntimeAPI().instantiateProcess(process.getUUID()));
    }
    
    executeTask(instanceUUIDs.get(0), "aTask");
    executeTask(instanceUUIDs.get(0), "anotherTask");
    
    //end interval
    Date until = new Date();
    
    for (int i = 1; i < numberOfInstances; i++) {
      executeTask(instanceUUIDs.get(i), "aTask");
      executeTask(instanceUUIDs.get(i), "anotherTask");
    }    
    
    List<Long> exptectedExecutionTimes = new ArrayList<Long>();    
    
    Set<LightActivityInstance> activityInstances = getQueryRuntimeAPI().getLightActivityInstances(instanceUUIDs.get(0), "anotherTask");
    assertEquals(1, activityInstances.size());
    LightActivityInstance activityInstance = activityInstances.iterator().next();
    exptectedExecutionTimes.add(activityInstance.getEndedDate().getTime() - activityInstance.getStartedDate().getTime());
    
    ActivityDefinitionUUID activityUUID = getQueryDefinitionAPI().getProcessActivity(process.getUUID(), "anotherTask").getUUID();
    
    List<Long> retrivedExecutionTimes = getBAMAPI().getActivityInstancesExecutionTime(activityUUID, since, until);
    assertEquals(1, retrivedExecutionTimes.size());    
    checkDurationList(exptectedExecutionTimes, retrivedExecutionTimes);
    
    getManagementAPI().deleteProcess(process.getUUID());
  }
  
  public void testGetActivityInstancesExecutionTimeFromActivityDefUUIDs() throws Exception {
    ProcessDefinition process =
      ProcessBuilder.createProcess("processaiet", "4.2")      
      .addHuman(getLogin())
      .addHumanTask("aTask", getLogin())      
      .addHumanTask("anotherTask", getLogin())
      .addSystemTask("automaticTask")
      .addTransition("aTask", "anotherTask")
      .addTransition("anotherTask", "automaticTask")
      .done();
    
    process = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process));
    
    //start interval
    Date since = new Date();
    int numberOfInstances = 3;    
    List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();
    for (int i = 0; i < numberOfInstances; i++) {
      instanceUUIDs.add(getRuntimeAPI().instantiateProcess(process.getUUID()));
    }
    for (int i = 0; i < numberOfInstances; i++) {
      executeTask(instanceUUIDs.get(i), "aTask");
      executeTask(instanceUUIDs.get(i), "anotherTask");
    }
    //end interval
    Date until = new Date();
    
    List<Long> exptectedExecutionTimes = new ArrayList<Long>();    
    for (int i = 0; i < numberOfInstances; i++) {
      Set<LightActivityInstance> activityInstances = getQueryRuntimeAPI().getLightActivityInstances(instanceUUIDs.get(i), "aTask");
      assertEquals(1, activityInstances.size());
      LightActivityInstance activityInstance = activityInstances.iterator().next();
      exptectedExecutionTimes.add(activityInstance.getEndedDate().getTime() - activityInstance.getStartedDate().getTime());
      
      activityInstances = getQueryRuntimeAPI().getLightActivityInstances(instanceUUIDs.get(i), "automaticTask");
      assertEquals(1, activityInstances.size());
      activityInstance = activityInstances.iterator().next();
      exptectedExecutionTimes.add(activityInstance.getEndedDate().getTime() - activityInstance.getStartedDate().getTime());
    }
    
    Set<ActivityDefinitionUUID> activityUUIDs = new HashSet<ActivityDefinitionUUID>();
    activityUUIDs.add(getQueryDefinitionAPI().getProcessActivity(process.getUUID(), "aTask").getUUID());
    activityUUIDs.add(getQueryDefinitionAPI().getProcessActivity(process.getUUID(), "automaticTask").getUUID());
    
    List<Long> retrivedExecutionTimes = getBAMAPI().getActivityInstancesExecutionTimeFromActivityUUIDs(activityUUIDs, since, until);
    assertEquals(numberOfInstances * 2, retrivedExecutionTimes.size());
    Collections.sort(exptectedExecutionTimes);
    checkDurationList(exptectedExecutionTimes, retrivedExecutionTimes);
    
    getManagementAPI().deleteProcess(process.getUUID());
  }
  
  public void testGetActivityInstancesExecutionTimeFromActivityDefUUIDsWithLimitedInterval () throws Exception {
    ProcessDefinition process =
      ProcessBuilder.createProcess("processaiet", "4.3")      
      .addHuman(getLogin())
      .addHumanTask("aTask", getLogin())      
      .addHumanTask("anotherTask", getLogin())
      .addSystemTask("automaticTask")
      .addTransition("aTask", "anotherTask")
      .addTransition("anotherTask", "automaticTask")
      .done();
    
    process = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process));
    
    //start interval
    Date since = new Date();
    int numberOfInstances = 3;    
    List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();
    for (int i = 0; i < numberOfInstances; i++) {
      instanceUUIDs.add(getRuntimeAPI().instantiateProcess(process.getUUID()));
    }
    
    executeTask(instanceUUIDs.get(0), "aTask");
    executeTask(instanceUUIDs.get(0), "anotherTask");
    
    //end interval
    Date until = new Date();
    
    for (int i = 1; i < numberOfInstances; i++) {
      executeTask(instanceUUIDs.get(i), "aTask");
      executeTask(instanceUUIDs.get(i), "anotherTask");
    }    
    
    List<Long> exptectedExecutionTimes = new ArrayList<Long>();    
    
    Set<LightActivityInstance> activityInstances = getQueryRuntimeAPI().getLightActivityInstances(instanceUUIDs.get(0), "aTask");
    assertEquals(1, activityInstances.size());
    LightActivityInstance activityInstance = activityInstances.iterator().next();
    exptectedExecutionTimes.add(activityInstance.getEndedDate().getTime() - activityInstance.getStartedDate().getTime());
    
    activityInstances = getQueryRuntimeAPI().getLightActivityInstances(instanceUUIDs.get(0), "automaticTask");
    assertEquals(1, activityInstances.size());
    activityInstance = activityInstances.iterator().next();
    exptectedExecutionTimes.add(activityInstance.getEndedDate().getTime() - activityInstance.getStartedDate().getTime());
    
    
    Set<ActivityDefinitionUUID> activityUUIDs = new HashSet<ActivityDefinitionUUID>();
    activityUUIDs.add(getQueryDefinitionAPI().getProcessActivity(process.getUUID(), "aTask").getUUID());
    activityUUIDs.add(getQueryDefinitionAPI().getProcessActivity(process.getUUID(), "automaticTask").getUUID());
    
    List<Long> retrivedExecutionTimes = getBAMAPI().getActivityInstancesExecutionTimeFromActivityUUIDs(activityUUIDs, since, until);
    assertEquals(2, retrivedExecutionTimes.size());
    Collections.sort(exptectedExecutionTimes);
    checkDurationList(exptectedExecutionTimes, retrivedExecutionTimes);
    
    getManagementAPI().deleteProcess(process.getUUID());
  }
  
  public void testGetTaskInstancesWaitingTime() throws Exception {
    ProcessDefinition process =
      ProcessBuilder.createProcess("processTiWt", "1.0")
      .addHuman(getLogin())
      .addHumanTask("task1", getLogin())      
      .done();
    
    process = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process));
    
    Date since = new Date();
    int numberOfInstances = 3;    
    List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();
    for (int i = 0; i < numberOfInstances; i++) {
      instanceUUIDs.add(getRuntimeAPI().instantiateProcess(process.getUUID()));
    }
        
    for (int i = 0; i < numberOfInstances; i++) {
      executeTask(instanceUUIDs.get(i), "task1");
    }        
    Date until = new Date();
    
    List<Long> exptectedWaitingTimes = new ArrayList<Long>();
    for (int i = 0; i < numberOfInstances; i++) {
      Set<LightActivityInstance> activityInstances = getQueryRuntimeAPI().getLightActivityInstances(instanceUUIDs.get(i), "task1");
      assertEquals(1, activityInstances.size());
      LightActivityInstance activityInstance = activityInstances.iterator().next();
      exptectedWaitingTimes.add(activityInstance.getStartedDate().getTime() - activityInstance.getReadyDate().getTime());
    }
    
    List<Long> retrivedWaitingTimes = getBAMAPI().getTaskInstancesWaitingTime(since, until);
    assertEquals(numberOfInstances, retrivedWaitingTimes.size());
    Collections.sort(exptectedWaitingTimes);
    checkDurationList(exptectedWaitingTimes, retrivedWaitingTimes);
    
    getManagementAPI().deleteProcess(process.getUUID());
  }
  
  public void testGetTaskInstancesWaitingTimeWithLimitedInterval() throws Exception {
    ProcessDefinition process =
      ProcessBuilder.createProcess("processTiWt", "1.1")
      .addHuman(getLogin())
      .addHumanTask("task1", getLogin())      
      .done();
    
    process = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process));
    
    Date since = new Date();
    int numberOfInstances = 3;    
    List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();
    for (int i = 0; i < numberOfInstances; i++) {
      instanceUUIDs.add(getRuntimeAPI().instantiateProcess(process.getUUID()));
    }
    
    executeTask(instanceUUIDs.get(0), "task1");
    Date until = new Date();
    
    for (int i = 1; i < numberOfInstances; i++) {
      executeTask(instanceUUIDs.get(i), "task1");
    }        
    
    List<Long> exptectedWaitingTimes = new ArrayList<Long>();
    
    Set<LightActivityInstance> activityInstances = getQueryRuntimeAPI().getLightActivityInstances(instanceUUIDs.get(0), "task1");
    assertEquals(1, activityInstances.size());
    LightActivityInstance activityInstance = activityInstances.iterator().next();
    exptectedWaitingTimes.add(activityInstance.getStartedDate().getTime() - activityInstance.getReadyDate().getTime());    
    
    List<Long> retrivedWaitingTimes = getBAMAPI().getTaskInstancesWaitingTime(since, until);
    assertEquals(1, retrivedWaitingTimes.size());    
    checkDurationList(exptectedWaitingTimes, retrivedWaitingTimes);
    
    getManagementAPI().deleteProcess(process.getUUID());
  }
  
  public void testGetTaskInstancesWaitingTimeWithoutHumanTask() throws Exception {
    ProcessDefinition process =
      ProcessBuilder.createProcess("processTiWt", "1.2")
      .addHuman(getLogin())
      .addSystemTask("task1")      
      .done();
    
    process = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process));
    
    Date since = new Date();
    int numberOfInstances = 3;    
    List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();
    for (int i = 0; i < numberOfInstances; i++) {
      instanceUUIDs.add(getRuntimeAPI().instantiateProcess(process.getUUID()));
    }
    
    List<Long> retrivedWaitingTimes = getBAMAPI().getTaskInstancesWaitingTime(since, new Date());
    assertEquals(0, retrivedWaitingTimes.size());
    
    getManagementAPI().deleteProcess(process.getUUID());
  }
  
  public void testGetTaskInstancesWaitingTimeFromProcessUUID() throws Exception {
    int numberOfProcess = 3;
    List<ProcessDefinition> processes = new ArrayList<ProcessDefinition>();
    Set<ProcessDefinitionUUID> processUUIDs = new HashSet<ProcessDefinitionUUID>();
    for (int i = 0; i < numberOfProcess; i++) {
      ProcessDefinition process =
        ProcessBuilder.createProcess("processTiWt" + i, "2.0")
        .addHuman(getLogin())
        .addHumanTask("task1", getLogin())      
        .done();
      
      process = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process));      
      processes.add(process);
      processUUIDs.add(process.getUUID());
    }
    
    //beginning of interval 
    Date since = new Date();
    
    //create instances
    int numberOfInstances = 2;    
    ProcessInstanceUUID [][] processInstancesUUIDs = new ProcessInstanceUUID [numberOfProcess] [numberOfInstances];
    
    for (int i = 0; i < numberOfProcess; i++) {
      for (int j = 0; j < numberOfInstances; j++) {
        processInstancesUUIDs[i][j] = getRuntimeAPI().instantiateProcess(processes.get(i).getUUID());        
      }
    }
        
    //execute tasks
    for (int i = 0; i < numberOfProcess; i++) {
      for (int j = 0; j < numberOfInstances; j++) {
        executeTask(processInstancesUUIDs[i][j], "task1");        
      }
    }
    
    //end of the interval
    Date until = new Date();
    
    List<Long> exptectedWaitingTimes = new ArrayList<Long>();
    
    for (int j = 0; j < numberOfInstances; j++) {
      Set<LightActivityInstance> activityInstances = getQueryRuntimeAPI().getLightActivityInstances(processInstancesUUIDs[0][j], "task1");
      assertEquals(1, activityInstances.size());
      LightActivityInstance activityInstance = activityInstances.iterator().next();
      exptectedWaitingTimes.add(activityInstance.getStartedDate().getTime() - activityInstance.getReadyDate().getTime());
    }        
    
    List<Long> retrivedWaitingTimes = getBAMAPI().getTaskInstancesWaitingTime(processes.get(0).getUUID(), since, until);
    assertEquals(numberOfInstances, retrivedWaitingTimes.size()); 
    Collections.sort(exptectedWaitingTimes);
    checkDurationList(exptectedWaitingTimes, retrivedWaitingTimes);
    
    getManagementAPI().delete(processUUIDs);
  }
  
  public void testGetTaskInstancesWaitingTimeFromProcessUUIDWithLimitedInterval() throws Exception {
    int numberOfProcess = 3;
    List<ProcessDefinition> processes = new ArrayList<ProcessDefinition>();
    Set<ProcessDefinitionUUID> processUUIDs = new HashSet<ProcessDefinitionUUID>();
    for (int i = 0; i < numberOfProcess; i++) {
      ProcessDefinition process =
        ProcessBuilder.createProcess("processTiWt" + i, "2.1")
        .addHuman(getLogin())
        .addHumanTask("task1", getLogin())      
        .done();
      
      process = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process));      
      processes.add(process);
      processUUIDs.add(process.getUUID());
    }
    
    //beginning of interval 
    Date since = new Date();
    
    //create instances
    int numberOfInstances = 2;    
    ProcessInstanceUUID [][] processInstancesUUIDs = new ProcessInstanceUUID [numberOfProcess] [numberOfInstances];
    
    for (int i = 0; i < numberOfProcess; i++) {
      for (int j = 0; j < numberOfInstances; j++) {
        processInstancesUUIDs[i][j] = getRuntimeAPI().instantiateProcess(processes.get(i).getUUID());        
      }
    }
    
    for (int i = 0; i < numberOfProcess; i++) {
      executeTask(processInstancesUUIDs[i][0], "task1");
    }
    //end of the interval
    Date until = new Date();    
    
    for (int i = 0; i < numberOfProcess; i++) {
      for (int j = 1; j < numberOfInstances; j++) {
        executeTask(processInstancesUUIDs[i][j], "task1");        
      }
    }    
    
    List<Long> exptectedWaitingTimes = new ArrayList<Long>();    
    
    Set<LightActivityInstance> activityInstances = getQueryRuntimeAPI().getLightActivityInstances(processInstancesUUIDs[0][0], "task1");
    assertEquals(1, activityInstances.size());
    LightActivityInstance activityInstance = activityInstances.iterator().next();
    exptectedWaitingTimes.add(activityInstance.getStartedDate().getTime() - activityInstance.getReadyDate().getTime());
            
    
    List<Long> retrivedWaitingTimes = getBAMAPI().getTaskInstancesWaitingTime(processes.get(0).getUUID(), since, until);
    assertEquals(1, retrivedWaitingTimes.size());
    checkDurationList(exptectedWaitingTimes, retrivedWaitingTimes);
    
    getManagementAPI().delete(processUUIDs);
  }
  
  public void testGetTaskInstancesWaitingTimeFromProcessUUIDs() throws Exception {
    int numberOfProcess = 3;
    List<ProcessDefinition> processes = new ArrayList<ProcessDefinition>();
    Set<ProcessDefinitionUUID> processUUIDs = new HashSet<ProcessDefinitionUUID>();
    for (int i = 0; i < numberOfProcess; i++) {
      ProcessDefinition process =
        ProcessBuilder.createProcess("processTiWt" + i, "3.0")
        .addHuman(getLogin())
        .addHumanTask("task1", getLogin())      
        .done();
      
      process = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process));      
      processes.add(process);
      processUUIDs.add(process.getUUID());
    }
    
    //beginning of interval 
    Date since = new Date();
    
    //create instances
    int numberOfInstances = 2;    
    ProcessInstanceUUID [][] processInstancesUUIDs = new ProcessInstanceUUID [numberOfProcess] [numberOfInstances];
    
    for (int i = 0; i < numberOfProcess; i++) {
      for (int j = 0; j < numberOfInstances; j++) {
        processInstancesUUIDs[i][j] = getRuntimeAPI().instantiateProcess(processes.get(i).getUUID());        
      }
    }
        
    //execute tasks
    for (int i = 0; i < numberOfProcess; i++) {
      for (int j = 0; j < numberOfInstances; j++) {
        executeTask(processInstancesUUIDs[i][j], "task1");        
      }
    }
    
    //end of the interval
    Date until = new Date();
    
    List<Long> exptectedWaitingTimes = new ArrayList<Long>();
    for (int i = 1; i < numberOfProcess; i++) {
      for (int j = 0; j < numberOfInstances; j++) {
        Set<LightActivityInstance> activityInstances = getQueryRuntimeAPI().getLightActivityInstances(processInstancesUUIDs[i][j], "task1");
        assertEquals(1, activityInstances.size());
        LightActivityInstance activityInstance = activityInstances.iterator().next();
        exptectedWaitingTimes.add(activityInstance.getStartedDate().getTime() - activityInstance.getReadyDate().getTime());
      }        
    }
    
    Set<ProcessDefinitionUUID> fromProcess = new HashSet<ProcessDefinitionUUID>();    
    fromProcess.add(processes.get(1).getUUID());
    fromProcess.add(processes.get(2).getUUID());    
    
    List<Long> retrivedWaitingTimes = getBAMAPI().getTaskInstancesWaitingTimeFromProcessUUIDs(fromProcess, since, until);
    assertEquals(numberOfInstances * 2, retrivedWaitingTimes.size()); 
    Collections.sort(exptectedWaitingTimes);
    checkDurationList(exptectedWaitingTimes, retrivedWaitingTimes);
    
    getManagementAPI().delete(processUUIDs);
  }
  
  public void testGetTaskInstancesWaitingTimeFromProcessUUIDsWithLimitedInterval() throws Exception {
    int numberOfProcess = 3;
    List<ProcessDefinition> processes = new ArrayList<ProcessDefinition>();
    Set<ProcessDefinitionUUID> processUUIDs = new HashSet<ProcessDefinitionUUID>();
    for (int i = 0; i < numberOfProcess; i++) {
      ProcessDefinition process =
        ProcessBuilder.createProcess("processTiWt" + i, "3.1")
        .addHuman(getLogin())
        .addHumanTask("task1", getLogin())      
        .done();
      
      process = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process));      
      processes.add(process);
      processUUIDs.add(process.getUUID());
    }
    
    //beginning of interval 
    Date since = new Date();
    
    //create instances
    int numberOfInstances = 2;    
    ProcessInstanceUUID [][] processInstancesUUIDs = new ProcessInstanceUUID [numberOfProcess] [numberOfInstances];
    
    for (int i = 0; i < numberOfProcess; i++) {
      for (int j = 0; j < numberOfInstances; j++) {
        processInstancesUUIDs[i][j] = getRuntimeAPI().instantiateProcess(processes.get(i).getUUID());        
      }
    }
    
    for (int i = 0; i < numberOfProcess; i++) {
      executeTask(processInstancesUUIDs[i][0], "task1");
    }
    //end of the interval
    Date until = new Date();    
    
    for (int i = 0; i < numberOfProcess; i++) {
      for (int j = 1; j < numberOfInstances; j++) {
        executeTask(processInstancesUUIDs[i][j], "task1");        
      }
    }    
    
    List<Long> exptectedWaitingTimes = new ArrayList<Long>();
    Set<LightActivityInstance> activityInstances = getQueryRuntimeAPI().getLightActivityInstances(processInstancesUUIDs[1][0], "task1");
    assertEquals(1, activityInstances.size());
    LightActivityInstance activityInstance = activityInstances.iterator().next();
    exptectedWaitingTimes.add(activityInstance.getStartedDate().getTime() - activityInstance.getReadyDate().getTime());
    
    activityInstances = getQueryRuntimeAPI().getLightActivityInstances(processInstancesUUIDs[2][0], "task1");
    assertEquals(1, activityInstances.size());
    activityInstance = activityInstances.iterator().next();
    exptectedWaitingTimes.add(activityInstance.getStartedDate().getTime() - activityInstance.getReadyDate().getTime());
    
    Set<ProcessDefinitionUUID> fromProcess = new HashSet<ProcessDefinitionUUID>();    
    fromProcess.add(processes.get(1).getUUID());
    fromProcess.add(processes.get(2).getUUID());                
    
    List<Long> retrivedWaitingTimes = getBAMAPI().getTaskInstancesWaitingTimeFromProcessUUIDs(fromProcess, since, until);
    assertEquals(2, retrivedWaitingTimes.size());
    Collections.sort(exptectedWaitingTimes);
    checkDurationList(exptectedWaitingTimes, retrivedWaitingTimes);
    
    getManagementAPI().delete(processUUIDs);
  }
  
  public void testGetTaskInstancesWaitingTimeFromActivityDefUUID() throws Exception {
    ProcessDefinition process =
      ProcessBuilder.createProcess("processaiet", "4.0")      
      .addHuman(getLogin())
      .addHumanTask("aTask", getLogin())
      .addHumanTask("anotherTask", getLogin())
      .addTransition("aTask", "anotherTask")
      .done();
    
    process = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process));
    
    //start interval
    Date since = new Date();
    int numberOfInstances = 3;    
    List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();
    for (int i = 0; i < numberOfInstances; i++) {
      instanceUUIDs.add(getRuntimeAPI().instantiateProcess(process.getUUID()));
    }
    for (int i = 0; i < numberOfInstances; i++) {
      executeTask(instanceUUIDs.get(i), "aTask");
      executeTask(instanceUUIDs.get(i), "anotherTask");
    }
    //end interval
    Date until = new Date();
    
    List<Long> exptectedWaitingTimes = new ArrayList<Long>();    
    for (int i = 0; i < numberOfInstances; i++) {
      Set<LightActivityInstance> activityInstances = getQueryRuntimeAPI().getLightActivityInstances(instanceUUIDs.get(i), "anotherTask");
      assertEquals(1, activityInstances.size());
      LightActivityInstance activityInstance = activityInstances.iterator().next();
      exptectedWaitingTimes.add(activityInstance.getStartedDate().getTime() - activityInstance.getReadyDate().getTime());
    }
    
    ActivityDefinitionUUID activityUUID = getQueryDefinitionAPI().getProcessActivity(process.getUUID(), "anotherTask").getUUID();
    
    List<Long> retrivedWaitingTimes = getBAMAPI().getTaskInstancesWaitingTime(activityUUID, since, until);
    assertEquals(numberOfInstances, retrivedWaitingTimes.size());
    Collections.sort(exptectedWaitingTimes);
    checkDurationList(exptectedWaitingTimes, retrivedWaitingTimes);
    
    getManagementAPI().deleteProcess(process.getUUID());
  }
  
  public void testGetTaskInstancesWaitingTimeFromActivityDefUUIDWithLimitedInterval() throws Exception {
    ProcessDefinition process =
      ProcessBuilder.createProcess("processaiet", "4.1")
      .addHuman(getLogin())
      .addHumanTask("aTask", getLogin())
      .addHumanTask("anotherTask", getLogin())
      .addTransition("aTask", "anotherTask")
      .done();
    
    process = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process));
    
    //start interval
    Date since = new Date();
    int numberOfInstances = 3;    
    List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();
    for (int i = 0; i < numberOfInstances; i++) {
      instanceUUIDs.add(getRuntimeAPI().instantiateProcess(process.getUUID()));
    }
    
    executeTask(instanceUUIDs.get(0), "aTask");
    executeTask(instanceUUIDs.get(0), "anotherTask");
    
    //end interval
    Date until = new Date();
    
    for (int i = 1; i < numberOfInstances; i++) {
      executeTask(instanceUUIDs.get(i), "aTask");
      executeTask(instanceUUIDs.get(i), "anotherTask");
    }    
    
    List<Long> exptectedWaitingTimes = new ArrayList<Long>();    
    
    Set<LightActivityInstance> activityInstances = getQueryRuntimeAPI().getLightActivityInstances(instanceUUIDs.get(0), "anotherTask");
    assertEquals(1, activityInstances.size());
    LightActivityInstance activityInstance = activityInstances.iterator().next();
    exptectedWaitingTimes.add(activityInstance.getStartedDate().getTime() - activityInstance.getReadyDate().getTime());
    
    ActivityDefinitionUUID activityUUID = getQueryDefinitionAPI().getProcessActivity(process.getUUID(), "anotherTask").getUUID();
    
    List<Long> retrivedWaitingTimes = getBAMAPI().getTaskInstancesWaitingTime(activityUUID, since, until);
    assertEquals(1, retrivedWaitingTimes.size());    
    checkDurationList(exptectedWaitingTimes, retrivedWaitingTimes);
    
    getManagementAPI().deleteProcess(process.getUUID());
  }
  
  public void testGetTaskInstancesWaitingTimeFromTaskUUIDs() throws Exception {
    ProcessDefinition process =
      ProcessBuilder.createProcess("processAiWt", "4.2")      
      .addHuman(getLogin())
      .addHumanTask("aTask", getLogin())      
      .addHumanTask("anotherTask", getLogin())
      .addSystemTask("automaticTask")
      .addTransition("aTask", "anotherTask")
      .addTransition("anotherTask", "automaticTask")
      .done();
    
    process = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process));
    
    //start interval
    Date since = new Date();
    int numberOfInstances = 3;    
    List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();
    for (int i = 0; i < numberOfInstances; i++) {
      instanceUUIDs.add(getRuntimeAPI().instantiateProcess(process.getUUID()));
    }
    for (int i = 0; i < numberOfInstances; i++) {
      executeTask(instanceUUIDs.get(i), "aTask");
      executeTask(instanceUUIDs.get(i), "anotherTask");
    }
    //end interval
    Date until = new Date();
    
    List<Long> exptectedWaitingTimes = new ArrayList<Long>();    
    for (int i = 0; i < numberOfInstances; i++) {
      Set<LightActivityInstance> activityInstances = getQueryRuntimeAPI().getLightActivityInstances(instanceUUIDs.get(i), "aTask");
      assertEquals(1, activityInstances.size());
      LightActivityInstance activityInstance = activityInstances.iterator().next();
      exptectedWaitingTimes.add(activityInstance.getStartedDate().getTime() - activityInstance.getReadyDate().getTime());
      
      activityInstances = getQueryRuntimeAPI().getLightActivityInstances(instanceUUIDs.get(i), "anotherTask");
      assertEquals(1, activityInstances.size());
      activityInstance = activityInstances.iterator().next();
      exptectedWaitingTimes.add(activityInstance.getStartedDate().getTime() - activityInstance.getReadyDate().getTime());
    }
    
    Set<ActivityDefinitionUUID> activityUUIDs = new HashSet<ActivityDefinitionUUID>();
    activityUUIDs.add(getQueryDefinitionAPI().getProcessActivity(process.getUUID(), "aTask").getUUID());
    activityUUIDs.add(getQueryDefinitionAPI().getProcessActivity(process.getUUID(), "anotherTask").getUUID());
    
    List<Long> retrivedWaitingTimes = getBAMAPI().getTaskInstancesWaitingTimeFromTaskUUIDs(activityUUIDs, since, until);
    assertEquals(numberOfInstances * 2, retrivedWaitingTimes.size());
    Collections.sort(exptectedWaitingTimes);
    checkDurationList(exptectedWaitingTimes, retrivedWaitingTimes);
    
    getManagementAPI().deleteProcess(process.getUUID());
  }
  
  public void testGetTaskInstancesWaitingTimeFromTaskUUIDsWithLimitedInterval () throws Exception {
    ProcessDefinition process =
      ProcessBuilder.createProcess("processAiWt", "4.3")      
      .addHuman(getLogin())
      .addHumanTask("aTask", getLogin())      
      .addHumanTask("anotherTask", getLogin())
      .addSystemTask("automaticTask")
      .addTransition("aTask", "anotherTask")
      .addTransition("anotherTask", "automaticTask")
      .done();
    
    process = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process));
    
    //start interval
    Date since = new Date();
    int numberOfInstances = 3;    
    List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();
    for (int i = 0; i < numberOfInstances; i++) {
      instanceUUIDs.add(getRuntimeAPI().instantiateProcess(process.getUUID()));
    }
    
    executeTask(instanceUUIDs.get(0), "aTask");
    executeTask(instanceUUIDs.get(0), "anotherTask");
    
    //end interval
    Date until = new Date();
    
    for (int i = 1; i < numberOfInstances; i++) {
      executeTask(instanceUUIDs.get(i), "aTask");
      executeTask(instanceUUIDs.get(i), "anotherTask");
    }    
    
    List<Long> exptectedWaitingTimes = new ArrayList<Long>();    
    
    Set<LightActivityInstance> activityInstances = getQueryRuntimeAPI().getLightActivityInstances(instanceUUIDs.get(0), "aTask");
    assertEquals(1, activityInstances.size());
    LightActivityInstance activityInstance = activityInstances.iterator().next();
    exptectedWaitingTimes.add(activityInstance.getStartedDate().getTime() - activityInstance.getReadyDate().getTime());
    
    activityInstances = getQueryRuntimeAPI().getLightActivityInstances(instanceUUIDs.get(0), "anotherTask");
    assertEquals(1, activityInstances.size());
    activityInstance = activityInstances.iterator().next();
    exptectedWaitingTimes.add(activityInstance.getStartedDate().getTime() - activityInstance.getReadyDate().getTime());
    
    
    Set<ActivityDefinitionUUID> activityUUIDs = new HashSet<ActivityDefinitionUUID>();
    activityUUIDs.add(getQueryDefinitionAPI().getProcessActivity(process.getUUID(), "aTask").getUUID());
    activityUUIDs.add(getQueryDefinitionAPI().getProcessActivity(process.getUUID(), "anotherTask").getUUID());
    
    List<Long> retrivedWaitingTimes = getBAMAPI().getTaskInstancesWaitingTimeFromTaskUUIDs(activityUUIDs, since, until);
    assertEquals(2, retrivedWaitingTimes.size());
    Collections.sort(exptectedWaitingTimes);
    checkDurationList(exptectedWaitingTimes, retrivedWaitingTimes);
    
    getManagementAPI().deleteProcess(process.getUUID());
  }
  
  public void testGetTaskInstancesWaitingTimeOfUser() throws Exception {
    ProcessDefinition process =
      ProcessBuilder.createProcess("processTiWt", "5.0")
      .addHuman("admin")
      .addHuman("john")
      .addHumanTask("adminTask1", "admin")
      .addHumanTask("adminTask2", "admin")
      .addHumanTask("johnTask1", "john")
      .addHumanTask("johnTask2", "john")
      .addHumanTask("johnTask3", "john")
      .addTransition("adminTask1", "adminTask2")
      .addTransition("adminTask2", "johnTask1")
      .addTransition("johnTask1", "johnTask2")
      .addTransition("johnTask2", "johnTask3")
      .done();
    
    process = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process));
    
    Date since = new Date();    
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(process.getUUID());      
    
    executeTask(instanceUUID, "adminTask1");
    executeTask(instanceUUID, "adminTask2");    
    loginAs("john", "bpm");
    executeTask(instanceUUID, "johnTask1");
    executeTask(instanceUUID, "johnTask2");
    executeTask(instanceUUID, "johnTask3");
            
    Date until = new Date();
    
    List<Long> exptectedWaitingTimes = new ArrayList<Long>();   
    Set<LightActivityInstance> activityInstances = getQueryRuntimeAPI().getLightActivityInstances(instanceUUID, "adminTask1");
    assertEquals(1, activityInstances.size());
    LightActivityInstance activityInstance = activityInstances.iterator().next();    
    exptectedWaitingTimes.add(activityInstance.getStartedDate().getTime() - activityInstance.getReadyDate().getTime());
    
    activityInstances = getQueryRuntimeAPI().getLightActivityInstances(instanceUUID, "adminTask2");
    assertEquals(1, activityInstances.size());
    activityInstance = activityInstances.iterator().next();    
    exptectedWaitingTimes.add(activityInstance.getStartedDate().getTime() - activityInstance.getReadyDate().getTime());
    
    
    List<Long> retrivedWaitingTimes = getBAMAPI().getTaskInstancesWaitingTimeOfUser("admin", since, until);
    assertEquals(2, retrivedWaitingTimes.size());
    Collections.sort(exptectedWaitingTimes);
    checkDurationList(exptectedWaitingTimes, retrivedWaitingTimes);
    
    getManagementAPI().deleteProcess(process.getUUID());
  }
    
  public void testGetTaskInstancesWaitingTimeOfUserWithLimitedInterval() throws Exception {
    ProcessDefinition process =
      ProcessBuilder.createProcess("processTiWt", "5.1")
      .addHuman("admin")
      .addHuman("john")
      .addHumanTask("adminTask1", "admin")
      .addHumanTask("adminTask2", "admin")
      .addHumanTask("johnTask1", "john")
      .addHumanTask("johnTask2", "john")
      .addHumanTask("johnTask3", "john")
      .addTransition("adminTask1", "adminTask2")
      .addTransition("adminTask2", "johnTask1")
      .addTransition("johnTask1", "johnTask2")
      .addTransition("johnTask2", "johnTask3")
      .done();
    
    process = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process));
    
    Date since = new Date();    
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(process.getUUID());      
    
    executeTask(instanceUUID, "adminTask1");
    Date until = new Date();
    executeTask(instanceUUID, "adminTask2");    
    loginAs("john", "bpm");
    executeTask(instanceUUID, "johnTask1");
    executeTask(instanceUUID, "johnTask2");
    executeTask(instanceUUID, "johnTask3");
    
    List<Long> exptectedWaitingTimes = new ArrayList<Long>();   
    Set<LightActivityInstance> activityInstances = getQueryRuntimeAPI().getLightActivityInstances(instanceUUID, "adminTask1");
    assertEquals(1, activityInstances.size());
    LightActivityInstance activityInstance = activityInstances.iterator().next();    
    exptectedWaitingTimes.add(activityInstance.getStartedDate().getTime() - activityInstance.getReadyDate().getTime());    
    
    List<Long> retrivedWaitingTimes = getBAMAPI().getTaskInstancesWaitingTimeOfUser("admin", since, until);
    assertEquals(1, retrivedWaitingTimes.size());
    Collections.sort(exptectedWaitingTimes);
    checkDurationList(exptectedWaitingTimes, retrivedWaitingTimes);
    
    getManagementAPI().deleteProcess(process.getUUID());
  }
  
  public void testGetTaskInstancesWaitingTimeOfUserFromProcessDefUUID() throws Exception {
    int numberOfProcess = 3;
    final List<ProcessDefinition> processes = new ArrayList<ProcessDefinition>();
    final List<ProcessDefinitionUUID> processUUIDs = new ArrayList<ProcessDefinitionUUID>();
    for (int i = 0; i < numberOfProcess; i++) {
      ProcessDefinition process =
        ProcessBuilder.createProcess("processTiWt" + i, "6.0")
        .addHuman("admin")
        .addHuman("john")
        .addHumanTask("adminTask1", "admin")
        .addHumanTask("adminTask2", "admin")
        .addHumanTask("johnTask1", "john")
        .addHumanTask("johnTask2", "john")
        .addHumanTask("johnTask3", "john")
        .addTransition("adminTask1", "adminTask2")
        .addTransition("adminTask2", "johnTask1")
        .addTransition("johnTask1", "johnTask2")
        .addTransition("johnTask2", "johnTask3")
        .done();
      
      process = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process));      
      processes.add(process);
      processUUIDs.add(process.getUUID());
    }
      
    List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();
    for (int i = 0; i < numberOfProcess; i++) {      
      instanceUUIDs.add(getRuntimeAPI().instantiateProcess(processes.get(i).getUUID()));
    }
    Date since = new Date();
    
    for (int i = 0; i < numberOfProcess; i++) {
      executeTask(instanceUUIDs.get(i), "adminTask1");
      executeTask(instanceUUIDs.get(i), "adminTask2");
    }
    
    loginAs("john", "bpm");    
    for (int i = 0; i < numberOfProcess; i++) {
      executeTask(instanceUUIDs.get(i), "johnTask1");
      executeTask(instanceUUIDs.get(i), "johnTask2");
      executeTask(instanceUUIDs.get(i), "johnTask3");
    }
            
    Date until = new Date();
    
    List<Long> exptectedWaitingTimes = new ArrayList<Long>();
    
    Set<LightActivityInstance> activityInstances = getQueryRuntimeAPI().getLightActivityInstances(instanceUUIDs.get(0), "johnTask1");
    assertEquals(1, activityInstances.size());
    LightActivityInstance activityInstance = activityInstances.iterator().next();    
    exptectedWaitingTimes.add(activityInstance.getStartedDate().getTime() - activityInstance.getReadyDate().getTime());
    
    activityInstances = getQueryRuntimeAPI().getLightActivityInstances(instanceUUIDs.get(0), "johnTask2");
    assertEquals(1, activityInstances.size());
    activityInstance = activityInstances.iterator().next();    
    exptectedWaitingTimes.add(activityInstance.getStartedDate().getTime() - activityInstance.getReadyDate().getTime());
    
    activityInstances = getQueryRuntimeAPI().getLightActivityInstances(instanceUUIDs.get(0), "johnTask3");
    assertEquals(1, activityInstances.size());
    activityInstance = activityInstances.iterator().next();    
    exptectedWaitingTimes.add(activityInstance.getStartedDate().getTime() - activityInstance.getReadyDate().getTime());
        
    
    List<Long> retrivedWaitingTimes = getBAMAPI().getTaskInstancesWaitingTimeOfUser("john", processUUIDs.get(0), since, until);
    assertEquals(3, retrivedWaitingTimes.size());
    Collections.sort(exptectedWaitingTimes);
    checkDurationList(exptectedWaitingTimes, retrivedWaitingTimes);
        
    getManagementAPI().delete(processUUIDs);
  }
  
  public void testGetTaskInstancesWaitingTimeOfUserFromProcessDefUUIDWithLimitedInterval() throws Exception {
    int numberOfProcess = 3;
    final List<ProcessDefinition> processes = new ArrayList<ProcessDefinition>();
    final List<ProcessDefinitionUUID> processUUIDs = new ArrayList<ProcessDefinitionUUID>();
    for (int i = 0; i < numberOfProcess; i++) {
      ProcessDefinition process =
        ProcessBuilder.createProcess("processTiWt" + i, "6.0")
        .addHuman("admin")
        .addHuman("john")
        .addHumanTask("adminTask1", "admin")
        .addHumanTask("adminTask2", "admin")
        .addHumanTask("johnTask1", "john")
        .addHumanTask("johnTask2", "john")
        .addHumanTask("johnTask3", "john")
        .addTransition("adminTask1", "adminTask2")
        .addTransition("adminTask2", "johnTask1")
        .addTransition("johnTask1", "johnTask2")
        .addTransition("johnTask2", "johnTask3")
        .done();
      
      process = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process));      
      processes.add(process);
      processUUIDs.add(process.getUUID());
    }
      
    List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();
    for (int i = 0; i < numberOfProcess; i++) {      
      instanceUUIDs.add(getRuntimeAPI().instantiateProcess(processes.get(i).getUUID()));
    }
    Date since = new Date();
    
    for (int i = 0; i < numberOfProcess; i++) {
      executeTask(instanceUUIDs.get(i), "adminTask1");
      executeTask(instanceUUIDs.get(i), "adminTask2");
    }
    
    loginAs("john", "bpm");    
    for (int i = 0; i < numberOfProcess; i++) {
      executeTask(instanceUUIDs.get(i), "johnTask1");
    }
    Date until = new Date();
    
    for (int i = 0; i < numberOfProcess; i++) {      
      executeTask(instanceUUIDs.get(i), "johnTask2");
      executeTask(instanceUUIDs.get(i), "johnTask3");
    }               
    
    List<Long> exptectedWaitingTimes = new ArrayList<Long>();    
    Set<LightActivityInstance> activityInstances = getQueryRuntimeAPI().getLightActivityInstances(instanceUUIDs.get(0), "johnTask1");
    assertEquals(1, activityInstances.size());
    LightActivityInstance activityInstance = activityInstances.iterator().next();    
    exptectedWaitingTimes.add(activityInstance.getStartedDate().getTime() - activityInstance.getReadyDate().getTime());
    
    List<Long> retrivedWaitingTimes = getBAMAPI().getTaskInstancesWaitingTimeOfUser("john", processUUIDs.get(0), since, until);
    assertEquals(1, retrivedWaitingTimes.size());    
    checkDurationList(exptectedWaitingTimes, retrivedWaitingTimes);
        
    getManagementAPI().delete(processUUIDs);
  }
  
  public void testGetTaskInstancesWaitingTimeOfUserFromProcessDefUUIDs() throws Exception {
    int numberOfProcess = 3;
    final List<ProcessDefinition> processes = new ArrayList<ProcessDefinition>();
    final List<ProcessDefinitionUUID> processUUIDs = new ArrayList<ProcessDefinitionUUID>();
    for (int i = 0; i < numberOfProcess; i++) {
      ProcessDefinition process =
        ProcessBuilder.createProcess("processTiWt" + i, "6.0")
        .addHuman("admin")
        .addHuman("john")
        .addHumanTask("adminTask1", "admin")
        .addHumanTask("adminTask2", "admin")
        .addHumanTask("johnTask1", "john")
        .addHumanTask("johnTask2", "john")
        .addHumanTask("johnTask3", "john")
        .addTransition("adminTask1", "adminTask2")
        .addTransition("adminTask2", "johnTask1")
        .addTransition("johnTask1", "johnTask2")
        .addTransition("johnTask2", "johnTask3")
        .done();
      
      process = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process));      
      processes.add(process);
      processUUIDs.add(process.getUUID());
    }
      
    List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();
    for (int i = 0; i < numberOfProcess; i++) {      
      instanceUUIDs.add(getRuntimeAPI().instantiateProcess(processes.get(i).getUUID()));
    }
    Date since = new Date();
    
    for (int i = 0; i < numberOfProcess; i++) {
      executeTask(instanceUUIDs.get(i), "adminTask1");
      executeTask(instanceUUIDs.get(i), "adminTask2");
    }
    
    loginAs("john", "bpm");    
    for (int i = 0; i < numberOfProcess; i++) {
      executeTask(instanceUUIDs.get(i), "johnTask1");
      executeTask(instanceUUIDs.get(i), "johnTask2");
      executeTask(instanceUUIDs.get(i), "johnTask3");
    }
            
    Date until = new Date();
    
    List<Long> exptectedWaitingTimes = new ArrayList<Long>();
    HashSet<ProcessDefinitionUUID> fromProcesses = new HashSet<ProcessDefinitionUUID>();    
    for (int i = 0; i < numberOfProcess - 1; i++) {
      fromProcesses.add(processUUIDs.get(i));
      
      Set<LightActivityInstance> activityInstances = getQueryRuntimeAPI().getLightActivityInstances(instanceUUIDs.get(i), "johnTask1");
      assertEquals(1, activityInstances.size());
      LightActivityInstance activityInstance = activityInstances.iterator().next();    
      exptectedWaitingTimes.add(activityInstance.getStartedDate().getTime() - activityInstance.getReadyDate().getTime());
      
      activityInstances = getQueryRuntimeAPI().getLightActivityInstances(instanceUUIDs.get(i), "johnTask2");
      assertEquals(1, activityInstances.size());
      activityInstance = activityInstances.iterator().next();    
      exptectedWaitingTimes.add(activityInstance.getStartedDate().getTime() - activityInstance.getReadyDate().getTime());
      
      activityInstances = getQueryRuntimeAPI().getLightActivityInstances(instanceUUIDs.get(i), "johnTask3");
      assertEquals(1, activityInstances.size());
      activityInstance = activityInstances.iterator().next();    
      exptectedWaitingTimes.add(activityInstance.getStartedDate().getTime() - activityInstance.getReadyDate().getTime());
    }        
    
    List<Long> retrivedWaitingTimes = getBAMAPI().getTaskInstancesWaitingTimeOfUserFromProcessUUIDs("john", fromProcesses, since, until);
    assertEquals((numberOfProcess - 1) * 3, retrivedWaitingTimes.size());
    Collections.sort(exptectedWaitingTimes);
    checkDurationList(exptectedWaitingTimes, retrivedWaitingTimes);
        
    getManagementAPI().delete(processUUIDs);
  }
  
  public void testGetTaskInstancesWaitingTimeOfUserFromProcessDefUUIDsWithLimitedInterval() throws Exception {
    int numberOfProcess = 3;
    final List<ProcessDefinition> processes = new ArrayList<ProcessDefinition>();
    final List<ProcessDefinitionUUID> processUUIDs = new ArrayList<ProcessDefinitionUUID>();
    for (int i = 0; i < numberOfProcess; i++) {
      ProcessDefinition process =
        ProcessBuilder.createProcess("processTiWt" + i, "6.0")
        .addHuman("admin")
        .addHuman("john")
        .addHumanTask("adminTask1", "admin")
        .addHumanTask("adminTask2", "admin")
        .addHumanTask("johnTask1", "john")
        .addHumanTask("johnTask2", "john")
        .addHumanTask("johnTask3", "john")
        .addTransition("adminTask1", "adminTask2")
        .addTransition("adminTask2", "johnTask1")
        .addTransition("johnTask1", "johnTask2")
        .addTransition("johnTask2", "johnTask3")
        .done();
      
      process = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process));      
      processes.add(process);
      processUUIDs.add(process.getUUID());
    }
      
    List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();
    for (int i = 0; i < numberOfProcess; i++) {      
      instanceUUIDs.add(getRuntimeAPI().instantiateProcess(processes.get(i).getUUID()));
    }
    Date since = new Date();
    
    for (int i = 0; i < numberOfProcess; i++) {
      executeTask(instanceUUIDs.get(i), "adminTask1");
      executeTask(instanceUUIDs.get(i), "adminTask2");
    }
    
    loginAs("john", "bpm");    
    for (int i = 0; i < numberOfProcess; i++) {
      executeTask(instanceUUIDs.get(i), "johnTask1");
    }
    Date until = new Date();
    for (int i = 1; i < numberOfProcess; i++) {      
      executeTask(instanceUUIDs.get(i), "johnTask2");
      executeTask(instanceUUIDs.get(i), "johnTask3");
    }               
    
    List<Long> exptectedWaitingTimes = new ArrayList<Long>();    
    HashSet<ProcessDefinitionUUID> fromProcesses = new HashSet<ProcessDefinitionUUID>();
    for (int i = 0; i < numberOfProcess - 1; i++) {
      fromProcesses.add(processUUIDs.get(i));
      
      Set<LightActivityInstance> activityInstances = getQueryRuntimeAPI().getLightActivityInstances(instanceUUIDs.get(i), "johnTask1");
      assertEquals(1, activityInstances.size());
      LightActivityInstance activityInstance = activityInstances.iterator().next();    
      exptectedWaitingTimes.add(activityInstance.getStartedDate().getTime() - activityInstance.getReadyDate().getTime());
    }
    
    List<Long> retrivedWaitingTimes = getBAMAPI().getTaskInstancesWaitingTimeOfUserFromProcessUUIDs("john", fromProcesses, since, until);
    assertEquals(numberOfProcess - 1, retrivedWaitingTimes.size());
    Collections.sort(exptectedWaitingTimes);
    checkDurationList(exptectedWaitingTimes, retrivedWaitingTimes);
        
    getManagementAPI().delete(processUUIDs);
  }
  
  public void testGetTaskInstancesWaitingTimeOfUserFromActivityDefUUID() throws Exception {
    ProcessDefinition process =
      ProcessBuilder.createProcess("processTiWt", "4.0")      
      .addHuman("admin")
      .addHuman("john")
      .addHumanTask("adminTask", "admin")
      .addHumanTask("firstAdminOrJohnTask", "admin", "john")
      .addHumanTask("secondAdminOrJohnTask", "admin", "john")
      .addTransition("adminTask", "firstAdminOrJohnTask")
      .addTransition("firstAdminOrJohnTask", "secondAdminOrJohnTask")
      .done();
    
    process = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process));
    
    //start interval
    Date since = new Date();
    int numberOfInstances = 3;    
    List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();
    for (int i = 0; i < numberOfInstances; i++) {
      instanceUUIDs.add(getRuntimeAPI().instantiateProcess(process.getUUID()));
    }
    //admin
    for (int i = 0; i < numberOfInstances; i++) {
      executeTask(instanceUUIDs.get(i), "adminTask");
      executeTask(instanceUUIDs.get(i), "firstAdminOrJohnTask");      
    }
    //john
    loginAs("john", "bpm");
    for (int i = 0; i < numberOfInstances; i++) {      
      executeTask(instanceUUIDs.get(i), "secondAdminOrJohnTask");
    }
    //end interval
    Date until = new Date();
    
    List<Long> exptectedWaitingTimes = new ArrayList<Long>();    
    for (int i = 0; i < numberOfInstances; i++) {
      Set<LightActivityInstance> activityInstances = getQueryRuntimeAPI().getLightActivityInstances(instanceUUIDs.get(i), "firstAdminOrJohnTask");
      assertEquals(1, activityInstances.size());
      LightActivityInstance activityInstance = activityInstances.iterator().next();
      exptectedWaitingTimes.add(activityInstance.getStartedDate().getTime() - activityInstance.getReadyDate().getTime());
    }
    
    ActivityDefinitionUUID activityUUID = getQueryDefinitionAPI().getProcessActivity(process.getUUID(), "firstAdminOrJohnTask").getUUID();
    
    List<Long> retrivedWaitingTimes = getBAMAPI().getTaskInstancesWaitingTimeOfUser("admin", activityUUID, since, until);
    assertEquals(numberOfInstances, retrivedWaitingTimes.size());
    Collections.sort(exptectedWaitingTimes);
    checkDurationList(exptectedWaitingTimes, retrivedWaitingTimes);
    
    getManagementAPI().deleteProcess(process.getUUID());
  }
  
  public void testGetTaskInstancesWaitingTimeOfUserFromActivityDefUUIDWithLimitedInterval() throws Exception {
    ProcessDefinition process =
      ProcessBuilder.createProcess("processTiWt", "4.0")      
      .addHuman("admin")
      .addHuman("john")
      .addHumanTask("adminTask", "admin")
      .addHumanTask("firstAdminOrJohnTask", "admin", "john")
      .addHumanTask("secondAdminOrJohnTask", "admin", "john")
      .addTransition("adminTask", "firstAdminOrJohnTask")
      .addTransition("firstAdminOrJohnTask", "secondAdminOrJohnTask")
      .done();
    
    process = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process));
    
    
    int numberOfInstances = 3;    
    List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();
    for (int i = 0; i < numberOfInstances; i++) {
      instanceUUIDs.add(getRuntimeAPI().instantiateProcess(process.getUUID()));
    }
    //start interval
    Date since = new Date();
    
    //admin
    executeTask(instanceUUIDs.get(0), "adminTask");        
    executeTask(instanceUUIDs.get(0), "firstAdminOrJohnTask");
    // end interval
    Date until = new Date();
    
    for (int i = 1; i < numberOfInstances; i++) {
      executeTask(instanceUUIDs.get(i), "adminTask");
      executeTask(instanceUUIDs.get(i), "firstAdminOrJohnTask");      
    }
    //john
    loginAs("john", "bpm");
    for (int i = 0; i < numberOfInstances; i++) {      
      executeTask(instanceUUIDs.get(i), "secondAdminOrJohnTask");
    }
    
    
    List<Long> exptectedWaitingTimes = new ArrayList<Long>();    
    
    Set<LightActivityInstance> activityInstances = getQueryRuntimeAPI().getLightActivityInstances(instanceUUIDs.get(0), "firstAdminOrJohnTask");
    assertEquals(1, activityInstances.size());
    LightActivityInstance activityInstance = activityInstances.iterator().next();
    exptectedWaitingTimes.add(activityInstance.getStartedDate().getTime() - activityInstance.getReadyDate().getTime());    
    
    ActivityDefinitionUUID activityUUID = getQueryDefinitionAPI().getProcessActivity(process.getUUID(), "firstAdminOrJohnTask").getUUID();
    
    List<Long> retrivedWaitingTimes = getBAMAPI().getTaskInstancesWaitingTimeOfUser("admin", activityUUID, since, until);
    assertEquals(1, retrivedWaitingTimes.size());    
    checkDurationList(exptectedWaitingTimes, retrivedWaitingTimes);
    
    getManagementAPI().deleteProcess(process.getUUID());
  }
  
  public void testGetTaskInstancesWaitingTimeOfUserFromActivityDefUUIDs() throws Exception {
    ProcessDefinition process =
      ProcessBuilder.createProcess("processTiWt", "4.0")      
      .addHuman("admin")
      .addHuman("john")
      .addHumanTask("adminTask1", "admin")
      .addHumanTask("adminTask2", "admin")
      .addHumanTask("firstAdminOrJohnTask", "admin", "john")
      .addHumanTask("secondAdminOrJohnTask", "admin", "john")
      .addTransition("adminTask1", "adminTask2")
      .addTransition("adminTask2", "firstAdminOrJohnTask")
      .addTransition("firstAdminOrJohnTask", "secondAdminOrJohnTask")
      .done();
    
    process = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process));
    
    //start interval
    Date since = new Date();
    int numberOfInstances = 3;    
    List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();
    for (int i = 0; i < numberOfInstances; i++) {
      instanceUUIDs.add(getRuntimeAPI().instantiateProcess(process.getUUID()));
    }
    //admin
    for (int i = 0; i < numberOfInstances; i++) {
      executeTask(instanceUUIDs.get(i), "adminTask1");
      executeTask(instanceUUIDs.get(i), "adminTask2");
      executeTask(instanceUUIDs.get(i), "firstAdminOrJohnTask");      
    }
    //john
    loginAs("john", "bpm");
    for (int i = 0; i < numberOfInstances; i++) {      
      executeTask(instanceUUIDs.get(i), "secondAdminOrJohnTask");
    }
    //end interval
    Date until = new Date();
    
    List<Long> exptectedWaitingTimes = new ArrayList<Long>();    
    for (int i = 0; i < numberOfInstances; i++) {
      Set<LightActivityInstance> activityInstances = getQueryRuntimeAPI().getLightActivityInstances(instanceUUIDs.get(i), "firstAdminOrJohnTask");
      assertEquals(1, activityInstances.size());
      LightActivityInstance activityInstance = activityInstances.iterator().next();
      exptectedWaitingTimes.add(activityInstance.getStartedDate().getTime() - activityInstance.getReadyDate().getTime());
      
      activityInstances = getQueryRuntimeAPI().getLightActivityInstances(instanceUUIDs.get(i), "adminTask1");
      assertEquals(1, activityInstances.size());
      activityInstance = activityInstances.iterator().next();
      exptectedWaitingTimes.add(activityInstance.getStartedDate().getTime() - activityInstance.getReadyDate().getTime());
    }
    
    HashSet<ActivityDefinitionUUID> activityUUIDs = new HashSet<ActivityDefinitionUUID>();
    activityUUIDs.add(getQueryDefinitionAPI().getProcessActivity(process.getUUID(), "firstAdminOrJohnTask").getUUID());
    activityUUIDs.add(getQueryDefinitionAPI().getProcessActivity(process.getUUID(), "adminTask1").getUUID());
    
    List<Long> retrivedWaitingTimes = getBAMAPI().getTaskInstancesWaitingTimeOfUserFromTaskUUIDs("admin", activityUUIDs, since, until);
    assertEquals(numberOfInstances * 2, retrivedWaitingTimes.size());
    Collections.sort(exptectedWaitingTimes);
    checkDurationList(exptectedWaitingTimes, retrivedWaitingTimes);
    
    getManagementAPI().deleteProcess(process.getUUID());
  }
  
  public void testGetTaskInstancesWaitingTimeOfUserFromActivityDefUUIDsWithLimitedInterval() throws Exception {
    ProcessDefinition process =
      ProcessBuilder.createProcess("processTiWt", "4.1")      
      .addHuman("admin")
      .addHuman("john")
      .addHumanTask("adminTask1", "admin")
      .addHumanTask("adminTask2", "admin")
      .addHumanTask("firstAdminOrJohnTask", "admin", "john")
      .addHumanTask("secondAdminOrJohnTask", "admin", "john")
      .addTransition("adminTask1", "adminTask2")
      .addTransition("adminTask2", "firstAdminOrJohnTask")
      .addTransition("firstAdminOrJohnTask", "secondAdminOrJohnTask")
      .done();
    
    process = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process));
    
    //start interval
    Date since = new Date();
    int numberOfInstances = 3;    
    List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();
    for (int i = 0; i < numberOfInstances; i++) {
      instanceUUIDs.add(getRuntimeAPI().instantiateProcess(process.getUUID()));
    }
    //admin
    executeTask(instanceUUIDs.get(0), "adminTask1");
    executeTask(instanceUUIDs.get(0), "adminTask2");
    executeTask(instanceUUIDs.get(0), "firstAdminOrJohnTask");
    //end interval
    Date until = new Date();
    
    for (int i = 1; i < numberOfInstances; i++) {
      executeTask(instanceUUIDs.get(i), "adminTask1");
      executeTask(instanceUUIDs.get(i), "adminTask2");
      executeTask(instanceUUIDs.get(i), "firstAdminOrJohnTask");      
    }
    //john
    loginAs("john", "bpm");
    for (int i = 0; i < numberOfInstances; i++) {      
      executeTask(instanceUUIDs.get(i), "secondAdminOrJohnTask");
    }
    
    
    List<Long> exptectedWaitingTimes = new ArrayList<Long>();
    Set<LightActivityInstance> activityInstances = getQueryRuntimeAPI().getLightActivityInstances(instanceUUIDs.get(0), "firstAdminOrJohnTask");
    assertEquals(1, activityInstances.size());
    LightActivityInstance activityInstance = activityInstances.iterator().next();
    exptectedWaitingTimes.add(activityInstance.getStartedDate().getTime() - activityInstance.getReadyDate().getTime());
    
    activityInstances = getQueryRuntimeAPI().getLightActivityInstances(instanceUUIDs.get(0), "adminTask1");
    assertEquals(1, activityInstances.size());
    activityInstance = activityInstances.iterator().next();
    exptectedWaitingTimes.add(activityInstance.getStartedDate().getTime() - activityInstance.getReadyDate().getTime());
    
    
    HashSet<ActivityDefinitionUUID> activityUUIDs = new HashSet<ActivityDefinitionUUID>();
    activityUUIDs.add(getQueryDefinitionAPI().getProcessActivity(process.getUUID(), "firstAdminOrJohnTask").getUUID());
    activityUUIDs.add(getQueryDefinitionAPI().getProcessActivity(process.getUUID(), "adminTask1").getUUID());
    
    List<Long> retrivedWaitingTimes = getBAMAPI().getTaskInstancesWaitingTimeOfUserFromTaskUUIDs("admin", activityUUIDs, since, until);
    assertEquals(2, retrivedWaitingTimes.size());
    Collections.sort(exptectedWaitingTimes);
    checkDurationList(exptectedWaitingTimes, retrivedWaitingTimes);
    
    getManagementAPI().deleteProcess(process.getUUID());
  }
  
  public void testGetActivityInstancesDuration() throws Exception {
    ProcessDefinition process =
      ProcessBuilder.createProcess("processAiD", "1.0")
      .addHuman(getLogin())
      .addHumanTask("task1", getLogin())
      .addHumanTask("task2", getLogin())
      .addHumanTask("task3", getLogin())
      .addHumanTask("task4", getLogin())
      .addTransition("task1", "task2")
      .addTransition("task2", "task3")
      .addTransition("task3", "task4")
      .done();
    
    process = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process));
    
    Date since = new Date();
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(process.getUUID());            
    
    int numberOfExecutedTasks = 3;
    for (int i = 0; i < numberOfExecutedTasks; i++) {
      executeTask(instanceUUID, "task" + (i + 1));      
    }            
    Date until = new Date();    
    List<Long> exptectedDurations = new ArrayList<Long>();
    
    for (int i = 0; i < numberOfExecutedTasks; i++) {
      Set<LightActivityInstance> activityInstances = getQueryRuntimeAPI().getLightActivityInstances(instanceUUID, "task" + (i+1));
      assertEquals(1, activityInstances.size());
      LightActivityInstance activityInstance = activityInstances.iterator().next();
      exptectedDurations.add(activityInstance.getEndedDate().getTime() - activityInstance.getReadyDate().getTime());
    }
    
    List<Long> retrivedDurations = getBAMAPI().getActivityInstancesDuration(since, until);
    assertEquals(numberOfExecutedTasks, retrivedDurations.size());
    Collections.sort(exptectedDurations);
    checkDurationList(exptectedDurations, retrivedDurations);
    
    getManagementAPI().deleteProcess(process.getUUID());
  }
  
  public void testGetActivityInstancesDurationWithLimitedInterval() throws Exception {
    ProcessDefinition process =
      ProcessBuilder.createProcess("processAiD", "1.1")
      .addHuman(getLogin())
      .addHumanTask("task1", getLogin())
      .addHumanTask("task2", getLogin())
      .addHumanTask("task3", getLogin())
      .addHumanTask("task4", getLogin())
      .addTransition("task1", "task2")
      .addTransition("task2", "task3")
      .addTransition("task3", "task4")
      .done();
    
    process = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process));
    
    Date since = new Date();
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(process.getUUID());            
    
    int numberOfExecutedTasks = 3;
    executeTask(instanceUUID, "task1");
    Date until = new Date();
    for (int i = 1; i < numberOfExecutedTasks; i++) {
      executeTask(instanceUUID, "task" + (i + 1));      
    }           
        
    List<Long> exptectedDurations = new ArrayList<Long>();
    Set<LightActivityInstance> activityInstances = getQueryRuntimeAPI().getLightActivityInstances(instanceUUID, "task1");
    assertEquals(1, activityInstances.size());
    LightActivityInstance activityInstance = activityInstances.iterator().next();
    exptectedDurations.add(activityInstance.getEndedDate().getTime() - activityInstance.getReadyDate().getTime());
    
    
    List<Long> retrivedDurations = getBAMAPI().getActivityInstancesDuration(since, until);
    assertEquals(1, retrivedDurations.size());    
    checkDurationList(exptectedDurations, retrivedDurations);
    
    getManagementAPI().deleteProcess(process.getUUID());
  }
  
  public void testGetActivityInstancesDurationFromProcessUUID() throws Exception {
    int numberOfProcess = 3;
    List<ProcessDefinition> processes = new ArrayList<ProcessDefinition>();
    List<ProcessDefinitionUUID> processUUIDs = new ArrayList<ProcessDefinitionUUID>();
    for (int i = 0; i < numberOfProcess; i++) {
      ProcessDefinition process =
        ProcessBuilder.createProcess("processAiD" + i, "2.0")
        .addHuman(getLogin())
        .addHumanTask("task1", getLogin())
        .addHumanTask("task2", getLogin())
        .addHumanTask("task3", getLogin())
        .addHumanTask("task4", getLogin())
        .addTransition("task1", "task2")
        .addTransition("task2", "task3")
        .addTransition("task3", "task4")
        .done();
      
      process = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process));
      processes.add(process);
      processUUIDs.add(process.getUUID());
    }
    
    Date since = new Date();
    List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();
    for (int i = 0; i < numberOfProcess; i++) {
      instanceUUIDs.add(getRuntimeAPI().instantiateProcess(processUUIDs.get(i)));
    }
    
    int numberOfExecutedTasks = 3;
    for (int i = 0; i < numberOfProcess; i++) {
      for (int j = 0; j < numberOfExecutedTasks; j++) {
        executeTask(instanceUUIDs.get(i), "task" + (j + 1));      
      }            
    }
    Date until = new Date();    
    List<Long> exptectedDurations = new ArrayList<Long>();
    
    for (int i = 0; i < numberOfExecutedTasks; i++) {
      Set<LightActivityInstance> activityInstances = getQueryRuntimeAPI().getLightActivityInstances(instanceUUIDs.get(0), "task" + (i+1));
      assertEquals(1, activityInstances.size());
      LightActivityInstance activityInstance = activityInstances.iterator().next();
      exptectedDurations.add(activityInstance.getEndedDate().getTime() - activityInstance.getReadyDate().getTime());
    }
    
    List<Long> retrivedDurations = getBAMAPI().getActivityInstancesDuration(processUUIDs.get(0), since, until);
    assertEquals(numberOfExecutedTasks, retrivedDurations.size());
    Collections.sort(exptectedDurations);
    checkDurationList(exptectedDurations, retrivedDurations);
    
    getManagementAPI().delete(processUUIDs);
  }
  
  public void testGetActivityInstancesDurationFromProcessUUIDWithLimitedInterval() throws Exception {
    int numberOfProcess = 3;
    List<ProcessDefinition> processes = new ArrayList<ProcessDefinition>();
    List<ProcessDefinitionUUID> processUUIDs = new ArrayList<ProcessDefinitionUUID>();
    for (int i = 0; i < numberOfProcess; i++) {
      ProcessDefinition process =
        ProcessBuilder.createProcess("processAiD" + i, "2.1")
        .addHuman(getLogin())
        .addHumanTask("task1", getLogin())
        .addHumanTask("task2", getLogin())
        .addHumanTask("task3", getLogin())
        .addHumanTask("task4", getLogin())
        .addTransition("task1", "task2")
        .addTransition("task2", "task3")
        .addTransition("task3", "task4")
        .done();
      
      process = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process));
      processes.add(process);
      processUUIDs.add(process.getUUID());
    }
    
    Date since = new Date();
    List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();
    for (int i = 0; i < numberOfProcess; i++) {
      instanceUUIDs.add(getRuntimeAPI().instantiateProcess(processUUIDs.get(i)));
    }
    
    int numberOfExecutedTasks = 3;
    
    for (int i = 0; i < numberOfProcess; i++) {
      executeTask(instanceUUIDs.get(i), "task1");
    }          
    Date until = new Date();    
    for (int i = 0; i < numberOfProcess; i++) {
      for (int j = 1; j < numberOfExecutedTasks; j++) {
        executeTask(instanceUUIDs.get(i), "task" + (j + 1));      
      }            
    }
    
    List<Long> exptectedDurations = new ArrayList<Long>();    
    
    Set<LightActivityInstance> activityInstances = getQueryRuntimeAPI().getLightActivityInstances(instanceUUIDs.get(0), "task1");
    assertEquals(1, activityInstances.size());
    LightActivityInstance activityInstance = activityInstances.iterator().next();
    exptectedDurations.add(activityInstance.getEndedDate().getTime() - activityInstance.getReadyDate().getTime());    
    
    List<Long> retrivedDurations = getBAMAPI().getActivityInstancesDuration(processUUIDs.get(0), since, until);
    assertEquals(1, retrivedDurations.size());    
    checkDurationList(exptectedDurations, retrivedDurations);
    
    getManagementAPI().delete(processUUIDs);
  }
  
  public void testGetActivityInstancesDurationFromProcessUUIDs() throws Exception {
    int numberOfProcess = 3;
    List<ProcessDefinition> processes = new ArrayList<ProcessDefinition>();
    List<ProcessDefinitionUUID> processUUIDs = new ArrayList<ProcessDefinitionUUID>();
    for (int i = 0; i < numberOfProcess; i++) {
      ProcessDefinition process =
        ProcessBuilder.createProcess("processAiD" + i, "2.0")
        .addHuman(getLogin())
        .addHumanTask("task1", getLogin())
        .addHumanTask("task2", getLogin())
        .addHumanTask("task3", getLogin())
        .addHumanTask("task4", getLogin())
        .addTransition("task1", "task2")
        .addTransition("task2", "task3")
        .addTransition("task3", "task4")
        .done();
      
      process = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process));
      processes.add(process);
      processUUIDs.add(process.getUUID());
    }
    
    Date since = new Date();
    List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();
    for (int i = 0; i < numberOfProcess; i++) {
      instanceUUIDs.add(getRuntimeAPI().instantiateProcess(processUUIDs.get(i)));
    }
    
    int numberOfExecutedTasks = 3;
    for (int i = 0; i < numberOfProcess; i++) {
      for (int j = 0; j < numberOfExecutedTasks; j++) {
        executeTask(instanceUUIDs.get(i), "task" + (j + 1));      
      }            
    }
    Date until = new Date();    
    List<Long> exptectedDurations = new ArrayList<Long>();
    
    for (int i = 0; i < numberOfProcess - 1; i++) {
      for (int j = 0; j < numberOfExecutedTasks; j++) {
        Set<LightActivityInstance> activityInstances = getQueryRuntimeAPI().getLightActivityInstances(instanceUUIDs.get(i), "task" + (j+1));
        assertEquals(1, activityInstances.size());
        LightActivityInstance activityInstance = activityInstances.iterator().next();
        exptectedDurations.add(activityInstance.getEndedDate().getTime() - activityInstance.getReadyDate().getTime());
      }
    }
    
    Set<ProcessDefinitionUUID> fromProcess = new HashSet<ProcessDefinitionUUID>(processUUIDs.subList(0, numberOfProcess-1));
    
    List<Long> retrivedDurations = getBAMAPI().getActivityInstancesDurationFromProcessUUIDs(fromProcess, since, until);
    assertEquals((numberOfProcess - 1) * numberOfExecutedTasks, retrivedDurations.size());
    Collections.sort(exptectedDurations);
    checkDurationList(exptectedDurations, retrivedDurations);
    
    getManagementAPI().delete(processUUIDs);
  }
  
  public void testGetActivityInstancesDurationFromProcessUUIDsWithLimitedInterval() throws Exception {
    int numberOfProcess = 3;
    List<ProcessDefinition> processes = new ArrayList<ProcessDefinition>();
    List<ProcessDefinitionUUID> processUUIDs = new ArrayList<ProcessDefinitionUUID>();
    for (int i = 0; i < numberOfProcess; i++) {
      ProcessDefinition process =
        ProcessBuilder.createProcess("processAiD" + i, "2.1")
        .addHuman(getLogin())
        .addHumanTask("task1", getLogin())
        .addHumanTask("task2", getLogin())
        .addHumanTask("task3", getLogin())
        .addHumanTask("task4", getLogin())
        .addTransition("task1", "task2")
        .addTransition("task2", "task3")
        .addTransition("task3", "task4")
        .done();
      
      process = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process));
      processes.add(process);
      processUUIDs.add(process.getUUID());
    }
    
    Date since = new Date();
    List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();
    for (int i = 0; i < numberOfProcess; i++) {
      instanceUUIDs.add(getRuntimeAPI().instantiateProcess(processUUIDs.get(i)));
    }
    
    int numberOfExecutedTasks = 3;
    for (int i = 0; i < numberOfProcess; i++) {      
      executeTask(instanceUUIDs.get(i), "task1");
    }
    Date until = new Date();
    for (int i = 0; i < numberOfProcess; i++) {
      for (int j = 1; j < numberOfExecutedTasks; j++) {
        executeTask(instanceUUIDs.get(i), "task" + (j + 1));      
      }            
    }
        
    List<Long> exptectedDurations = new ArrayList<Long>();    
    for (int i = 0; i < numberOfProcess - 1; i++) {      
      Set<LightActivityInstance> activityInstances = getQueryRuntimeAPI().getLightActivityInstances(instanceUUIDs.get(i), "task1");
      assertEquals(1, activityInstances.size());
      LightActivityInstance activityInstance = activityInstances.iterator().next();
      exptectedDurations.add(activityInstance.getEndedDate().getTime() - activityInstance.getReadyDate().getTime());      
    }
    
    Set<ProcessDefinitionUUID> fromProcess = new HashSet<ProcessDefinitionUUID>(processUUIDs.subList(0, numberOfProcess-1));
    
    List<Long> retrivedDurations = getBAMAPI().getActivityInstancesDurationFromProcessUUIDs(fromProcess, since, until);
    assertEquals((numberOfProcess - 1), retrivedDurations.size());
    Collections.sort(exptectedDurations);
    checkDurationList(exptectedDurations, retrivedDurations);
    
    getManagementAPI().delete(processUUIDs);
  }
  
  public void testGetActivityInstancesDurationFromActivityDefintionUUID() throws Exception {
    ProcessDefinition process =
      ProcessBuilder.createProcess("processAiD", "3.0")
      .addHuman(getLogin())
      .addHumanTask("task1", getLogin())
      .addSystemTask("task2")
      .addSystemTask("task3")
      .addHumanTask("task4", getLogin())
      .addTransition("task1", "task2")
      .addTransition("task2", "task3")
      .addTransition("task3", "task4")
      .done();
    
    process = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process));
    
    //start interval
    Date since = new Date();
    int numberOfInstances = 3;    
    List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();
    for (int i = 0; i < numberOfInstances; i++) {
      instanceUUIDs.add(getRuntimeAPI().instantiateProcess(process.getUUID()));
    }    
     
    for (int i = 0; i < numberOfInstances; i++) {
      executeTask(instanceUUIDs.get(i), "task1");      
    }
    //end interval
    Date until = new Date();
    
    List<Long> exptectedDuration = new ArrayList<Long>();    
    for (int i = 0; i < numberOfInstances; i++) {
      Set<LightActivityInstance> activityInstances = getQueryRuntimeAPI().getLightActivityInstances(instanceUUIDs.get(i), "task2");
      assertEquals(1, activityInstances.size());
      LightActivityInstance activityInstance = activityInstances.iterator().next();
      exptectedDuration.add(activityInstance.getEndedDate().getTime() - activityInstance.getReadyDate().getTime());
    }
    
    ActivityDefinitionUUID activityUUID = getQueryDefinitionAPI().getProcessActivity(process.getUUID(), "task2").getUUID();
    
    List<Long> retrivedDuration = getBAMAPI().getActivityInstancesDuration(activityUUID, since, until);
    assertEquals(numberOfInstances, retrivedDuration.size());
    Collections.sort(exptectedDuration);
    checkDurationList(exptectedDuration, retrivedDuration);
    
    getManagementAPI().deleteProcess(process.getUUID());
  }
  

  public void testGetActivityInstancesDurationFromActivityDefintionUUIDWithLimitedInterval() throws Exception {
    ProcessDefinition process =
      ProcessBuilder.createProcess("processAiD", "3.1")
      .addHuman(getLogin())
      .addHumanTask("task1", getLogin())
      .addSystemTask("task2")
      .addSystemTask("task3")
      .addHumanTask("task4", getLogin())
      .addTransition("task1", "task2")
      .addTransition("task2", "task3")
      .addTransition("task3", "task4")
      .done();
    
    process = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process));
    
    //start interval
    Date since = new Date();
    int numberOfInstances = 3;    
    List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();
    for (int i = 0; i < numberOfInstances; i++) {
      instanceUUIDs.add(getRuntimeAPI().instantiateProcess(process.getUUID()));
    }    
     
    executeTask(instanceUUIDs.get(0), "task1");
    //end interval
    Date until = new Date();
    for (int i = 1; i < numberOfInstances; i++) {
      executeTask(instanceUUIDs.get(i), "task1");      
    }    
    
    List<Long> exptectedDuration = new ArrayList<Long>();    
    
    Set<LightActivityInstance> activityInstances = getQueryRuntimeAPI().getLightActivityInstances(instanceUUIDs.get(0), "task2");
    assertEquals(1, activityInstances.size());
    LightActivityInstance activityInstance = activityInstances.iterator().next();
    exptectedDuration.add(activityInstance.getEndedDate().getTime() - activityInstance.getReadyDate().getTime());
    
    
    ActivityDefinitionUUID activityUUID = getQueryDefinitionAPI().getProcessActivity(process.getUUID(), "task2").getUUID();
    
    List<Long> retrivedDuration = getBAMAPI().getActivityInstancesDuration(activityUUID, since, until);
    assertEquals(1, retrivedDuration.size());    
    checkDurationList(exptectedDuration, retrivedDuration);
    
    getManagementAPI().deleteProcess(process.getUUID());
  }
  
  public void testGetActivityInstancesDurationFromActivityDefintionUUIDs() throws Exception {
    ProcessDefinition process =
      ProcessBuilder.createProcess("processAiD", "4.0")
      .addHuman(getLogin())
      .addHumanTask("task1", getLogin())
      .addSystemTask("task2")
      .addSystemTask("task3")
      .addHumanTask("task4", getLogin())
      .addTransition("task1", "task2")
      .addTransition("task2", "task3")
      .addTransition("task3", "task4")
      .done();
    
    process = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process));
    
    //start interval
    Date since = new Date();
    int numberOfInstances = 3;    
    List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();
    for (int i = 0; i < numberOfInstances; i++) {
      instanceUUIDs.add(getRuntimeAPI().instantiateProcess(process.getUUID()));
    }    
     
    for (int i = 0; i < numberOfInstances; i++) {
      executeTask(instanceUUIDs.get(i), "task1");      
    }
    //end interval
    Date until = new Date();
    
    List<Long> exptectedDuration = new ArrayList<Long>();    
    for (int i = 0; i < numberOfInstances; i++) {
      Set<LightActivityInstance> activityInstances = getQueryRuntimeAPI().getLightActivityInstances(instanceUUIDs.get(i), "task2");
      assertEquals(1, activityInstances.size());
      LightActivityInstance activityInstance = activityInstances.iterator().next();
      exptectedDuration.add(activityInstance.getEndedDate().getTime() - activityInstance.getReadyDate().getTime());
      
      activityInstances = getQueryRuntimeAPI().getLightActivityInstances(instanceUUIDs.get(i), "task1");
      assertEquals(1, activityInstances.size());
      activityInstance = activityInstances.iterator().next();
      exptectedDuration.add(activityInstance.getEndedDate().getTime() - activityInstance.getReadyDate().getTime());
    }
    
    Set<ActivityDefinitionUUID> activityUUIDs = new HashSet<ActivityDefinitionUUID>();
    activityUUIDs.add(getQueryDefinitionAPI().getProcessActivity(process.getUUID(), "task2").getUUID());
    activityUUIDs.add(getQueryDefinitionAPI().getProcessActivity(process.getUUID(), "task1").getUUID());
    
    List<Long> retrivedDuration = getBAMAPI().getActivityInstancesDurationFromActivityUUIDs(activityUUIDs, since, until);
    assertEquals(numberOfInstances * 2, retrivedDuration.size());
    Collections.sort(exptectedDuration);
    checkDurationList(exptectedDuration, retrivedDuration);
    
    getManagementAPI().deleteProcess(process.getUUID());
  }

  
  public void testGetActivityInstancesDurationFromActivityDefintionUUIDsWithLimitedInterval() throws Exception {
    ProcessDefinition process =
      ProcessBuilder.createProcess("processAiD", "4.1")
      .addHuman(getLogin())
      .addHumanTask("task1", getLogin())
      .addSystemTask("task2")
      .addSystemTask("task3")
      .addHumanTask("task4", getLogin())
      .addTransition("task1", "task2")
      .addTransition("task2", "task3")
      .addTransition("task3", "task4")
      .done();
    
    process = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process));
    
    //start interval
    Date since = new Date();
    int numberOfInstances = 3;    
    List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();
    for (int i = 0; i < numberOfInstances; i++) {
      instanceUUIDs.add(getRuntimeAPI().instantiateProcess(process.getUUID()));
    }      
    executeTask(instanceUUIDs.get(0), "task1");
    //end interval
    Date until = new Date();
    for (int i = 1; i < numberOfInstances; i++) {
      executeTask(instanceUUIDs.get(i), "task1");      
    }    
    
    List<Long> exptectedDuration = new ArrayList<Long>();    
    
    Set<LightActivityInstance> activityInstances = getQueryRuntimeAPI().getLightActivityInstances(instanceUUIDs.get(0), "task2");
    assertEquals(1, activityInstances.size());
    LightActivityInstance activityInstance = activityInstances.iterator().next();
    exptectedDuration.add(activityInstance.getEndedDate().getTime() - activityInstance.getReadyDate().getTime());
    
    activityInstances = getQueryRuntimeAPI().getLightActivityInstances(instanceUUIDs.get(0), "task1");
    assertEquals(1, activityInstances.size());
    activityInstance = activityInstances.iterator().next();
    exptectedDuration.add(activityInstance.getEndedDate().getTime() - activityInstance.getReadyDate().getTime());    
    
    Set<ActivityDefinitionUUID> activityUUIDs = new HashSet<ActivityDefinitionUUID>();
    activityUUIDs.add(getQueryDefinitionAPI().getProcessActivity(process.getUUID(), "task2").getUUID());
    activityUUIDs.add(getQueryDefinitionAPI().getProcessActivity(process.getUUID(), "task1").getUUID());
    
    List<Long> retrivedDuration = getBAMAPI().getActivityInstancesDurationFromActivityUUIDs(activityUUIDs, since, until);
    assertEquals(2, retrivedDuration.size());
    Collections.sort(exptectedDuration);
    checkDurationList(exptectedDuration, retrivedDuration);
    
    getManagementAPI().deleteProcess(process.getUUID());
  }
  
  public void testGetHumanActivityInstancesDuration() throws Exception {
    ProcessDefinition process =
      ProcessBuilder.createProcess("processHAiD", "1.0")
      .addHuman(getLogin())
      .addHumanTask("task1", getLogin())
      .addHumanTask("task2", getLogin())
      .addSystemTask("task3")
      .addSystemTask("task4")
      .addTransition("task1", "task2")
      .addTransition("task2", "task3")
      .addTransition("task3", "task4")
      .done();
    
    process = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process));
    
    Date since = new Date();
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(process.getUUID());            
    
    executeTask(instanceUUID, "task1");
    executeTask(instanceUUID, "task2");                
    Date until = new Date();    
    
    List<Long> exptectedDurations = new ArrayList<Long>();
    Set<LightActivityInstance> activityInstances = getQueryRuntimeAPI().getLightActivityInstances(instanceUUID, "task1");
    assertEquals(1, activityInstances.size());
    LightActivityInstance activityInstance = activityInstances.iterator().next();
    exptectedDurations.add(activityInstance.getEndedDate().getTime() - activityInstance.getReadyDate().getTime());
    
    activityInstances = getQueryRuntimeAPI().getLightActivityInstances(instanceUUID, "task2");
    assertEquals(1, activityInstances.size());
    activityInstance = activityInstances.iterator().next();
    exptectedDurations.add(activityInstance.getEndedDate().getTime() - activityInstance.getReadyDate().getTime());    
    
    List<Long> retrivedDurations = getBAMAPI().getActivityInstancesDurationByActivityType(Type.Human, since, until);
    assertEquals(2, retrivedDurations.size());
    Collections.sort(exptectedDurations);
    checkDurationList(exptectedDurations, retrivedDurations);
    
    getManagementAPI().deleteProcess(process.getUUID());
  }
  
  public void testGetHumanActivityInstancesDurationWithLimitedInterval() throws Exception {
    ProcessDefinition process =
      ProcessBuilder.createProcess("processHAiD", "1.1")
      .addHuman(getLogin())
      .addHumanTask("task1", getLogin())
      .addHumanTask("task2", getLogin())
      .addSystemTask("task3")
      .addSystemTask("task4")
      .addTransition("task1", "task2")
      .addTransition("task2", "task3")
      .addTransition("task3", "task4")
      .done();
    
    process = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process));
    
    Date since = new Date();
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(process.getUUID());            
    
    executeTask(instanceUUID, "task1");
    Date until = new Date();
    executeTask(instanceUUID, "task2");
    
    List<Long> exptectedDurations = new ArrayList<Long>();
    Set<LightActivityInstance> activityInstances = getQueryRuntimeAPI().getLightActivityInstances(instanceUUID, "task1");
    assertEquals(1, activityInstances.size());
    LightActivityInstance activityInstance = activityInstances.iterator().next();
    exptectedDurations.add(activityInstance.getEndedDate().getTime() - activityInstance.getReadyDate().getTime());        
    
    List<Long> retrivedDurations = getBAMAPI().getActivityInstancesDurationByActivityType(Type.Human, since, until);
    assertEquals(1, retrivedDurations.size());
    checkDurationList(exptectedDurations, retrivedDurations);
    
    getManagementAPI().deleteProcess(process.getUUID());
  }
  
  public void testGetAutomaticActivityInstancesDuration() throws Exception {
    ProcessDefinition process =
      ProcessBuilder.createProcess("processHAiD", "1.2")
      .addHuman(getLogin())
      .addHumanTask("task1", getLogin())
      .addHumanTask("task2", getLogin())
      .addSystemTask("task3")
      .addSystemTask("task4")
      .addTransition("task1", "task2")
      .addTransition("task2", "task3")
      .addTransition("task3", "task4")
      .done();
    
    process = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process));
    
    Date since = new Date();
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(process.getUUID());            
    
    executeTask(instanceUUID, "task1");
    executeTask(instanceUUID, "task2");                
    Date until = new Date();    
    
    List<Long> exptectedDurations = new ArrayList<Long>();
    Set<LightActivityInstance> activityInstances = getQueryRuntimeAPI().getLightActivityInstances(instanceUUID, "task3");
    assertEquals(1, activityInstances.size());
    LightActivityInstance activityInstance = activityInstances.iterator().next();
    exptectedDurations.add(activityInstance.getEndedDate().getTime() - activityInstance.getReadyDate().getTime());
    
    activityInstances = getQueryRuntimeAPI().getLightActivityInstances(instanceUUID, "task4");
    assertEquals(1, activityInstances.size());
    activityInstance = activityInstances.iterator().next();
    exptectedDurations.add(activityInstance.getEndedDate().getTime() - activityInstance.getReadyDate().getTime());    
    
    List<Long> retrivedDurations = getBAMAPI().getActivityInstancesDurationByActivityType(Type.Automatic, since, until);
    assertEquals(2, retrivedDurations.size());
    Collections.sort(exptectedDurations);
    checkDurationList(exptectedDurations, retrivedDurations);
    
    getManagementAPI().deleteProcess(process.getUUID());
  }
  
  public void testGetHumanActivityInstancesDurationFromProcessUUID() throws Exception {
    int numberOfProcess = 3;   
    List<ProcessDefinitionUUID> processUUIDs = new ArrayList<ProcessDefinitionUUID>();    
    for (int i = 0; i < numberOfProcess; i++) {
      ProcessDefinition process =
        ProcessBuilder.createProcess("processHAiD" + i, "1.0")
        .addHuman(getLogin())
        .addHumanTask("task1", getLogin())
        .addHumanTask("task2", getLogin())
        .addSystemTask("task3")
        .addSystemTask("task4")
        .addTransition("task1", "task2")
        .addTransition("task2", "task3")
        .addTransition("task3", "task4")
        .done();
      
      process = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process));      
      processUUIDs.add(process.getUUID());
    }
    
    Date since = new Date();
    List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();
    for (int i = 0; i < numberOfProcess; i++) {
      instanceUUIDs.add(getRuntimeAPI().instantiateProcess(processUUIDs.get(i)));
    }
    
    for (int i = 0; i < numberOfProcess; i++) {
      executeTask(instanceUUIDs.get(i), "task1");
      executeTask(instanceUUIDs.get(i), "task2");        
    }
    Date until = new Date();    
    
    List<Long> exptectedDurations = new ArrayList<Long>();
   
    Set<LightActivityInstance> activityInstances = getQueryRuntimeAPI().getLightActivityInstances(instanceUUIDs.get(0), "task1");
    assertEquals(1, activityInstances.size());
    LightActivityInstance activityInstance = activityInstances.iterator().next();
    exptectedDurations.add(activityInstance.getEndedDate().getTime() - activityInstance.getReadyDate().getTime());    
  
    activityInstances = getQueryRuntimeAPI().getLightActivityInstances(instanceUUIDs.get(0), "task2");
    assertEquals(1, activityInstances.size());
    activityInstance = activityInstances.iterator().next();
    exptectedDurations.add(activityInstance.getEndedDate().getTime() - activityInstance.getReadyDate().getTime());    
    
    List<Long> retrivedDurations = getBAMAPI().getActivityInstancesDurationByActivityType(Type.Human, processUUIDs.get(0), since, until);
    assertEquals(2, retrivedDurations.size());
    Collections.sort(exptectedDurations);
    checkDurationList(exptectedDurations, retrivedDurations);
    
    getManagementAPI().delete(processUUIDs);
  }
  
  public void testGetHumanActivityInstancesDurationFromProcessUUIDWithLimitedInterval() throws Exception {
    int numberOfProcess = 3;   
    List<ProcessDefinitionUUID> processUUIDs = new ArrayList<ProcessDefinitionUUID>();    
    for (int i = 0; i < numberOfProcess; i++) {
      ProcessDefinition process =
        ProcessBuilder.createProcess("processHAiD" + i, "1.1")
        .addHuman(getLogin())
        .addHumanTask("task1", getLogin())
        .addHumanTask("task2", getLogin())
        .addSystemTask("task3")
        .addSystemTask("task4")
        .addTransition("task1", "task2")
        .addTransition("task2", "task3")
        .addTransition("task3", "task4")
        .done();
      
      process = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process));      
      processUUIDs.add(process.getUUID());
    }
    
    Date since = new Date();
    List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();
    for (int i = 0; i < numberOfProcess; i++) {
      instanceUUIDs.add(getRuntimeAPI().instantiateProcess(processUUIDs.get(i)));
    }
    
    for (int i = 0; i < numberOfProcess; i++) {
      executeTask(instanceUUIDs.get(i), "task1");              
    }    
    Date until = new Date();
    
    for (int i = 0; i < numberOfProcess; i++) {     
      executeTask(instanceUUIDs.get(i), "task2");        
    }        
    
    List<Long> exptectedDurations = new ArrayList<Long>();   
    Set<LightActivityInstance> activityInstances = getQueryRuntimeAPI().getLightActivityInstances(instanceUUIDs.get(0), "task1");
    assertEquals(1, activityInstances.size());
    LightActivityInstance activityInstance = activityInstances.iterator().next();
    exptectedDurations.add(activityInstance.getEndedDate().getTime() - activityInstance.getReadyDate().getTime());
        
    List<Long> retrivedDurations = getBAMAPI().getActivityInstancesDurationByActivityType(Type.Human, processUUIDs.get(0), since, until);
    assertEquals(1, retrivedDurations.size());
    Collections.sort(exptectedDurations);
    checkDurationList(exptectedDurations, retrivedDurations);
    
    getManagementAPI().delete(processUUIDs);
  }
  
  public void testGetSytemActivityInstancesDurationFromProcessUUID() throws Exception {
    int numberOfProcess = 3;   
    List<ProcessDefinitionUUID> processUUIDs = new ArrayList<ProcessDefinitionUUID>();    
    for (int i = 0; i < numberOfProcess; i++) {
      ProcessDefinition process =
        ProcessBuilder.createProcess("processHAiD" + i, "1.2")
        .addHuman(getLogin())
        .addHumanTask("task1", getLogin())
        .addHumanTask("task2", getLogin())
        .addSystemTask("task3")
        .addSystemTask("task4")
        .addTransition("task1", "task2")
        .addTransition("task2", "task3")
        .addTransition("task3", "task4")
        .done();
      
      process = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process));      
      processUUIDs.add(process.getUUID());
    }
    
    Date since = new Date();
    List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();
    for (int i = 0; i < numberOfProcess; i++) {
      instanceUUIDs.add(getRuntimeAPI().instantiateProcess(processUUIDs.get(i)));
    }
    
    for (int i = 0; i < numberOfProcess; i++) {
      executeTask(instanceUUIDs.get(i), "task1");
      executeTask(instanceUUIDs.get(i), "task2");        
    }
    Date until = new Date();    
    
    List<Long> exptectedDurations = new ArrayList<Long>();
   
    Set<LightActivityInstance> activityInstances = getQueryRuntimeAPI().getLightActivityInstances(instanceUUIDs.get(0), "task3");
    assertEquals(1, activityInstances.size());
    LightActivityInstance activityInstance = activityInstances.iterator().next();
    exptectedDurations.add(activityInstance.getEndedDate().getTime() - activityInstance.getReadyDate().getTime());    
  
    activityInstances = getQueryRuntimeAPI().getLightActivityInstances(instanceUUIDs.get(0), "task4");
    assertEquals(1, activityInstances.size());
    activityInstance = activityInstances.iterator().next();
    exptectedDurations.add(activityInstance.getEndedDate().getTime() - activityInstance.getReadyDate().getTime());    
    
    List<Long> retrivedDurations = getBAMAPI().getActivityInstancesDurationByActivityType(Type.Automatic, processUUIDs.get(0), since, until);
    assertEquals(2, retrivedDurations.size());
    Collections.sort(exptectedDurations);
    checkDurationList(exptectedDurations, retrivedDurations);
    
    getManagementAPI().delete(processUUIDs);
  }
  
  public void testGetHumanActivityInstancesDurationFromProcessUUIDs() throws Exception {
    final int numberOfProcess = 3;   
    List<ProcessDefinitionUUID> processUUIDs = new ArrayList<ProcessDefinitionUUID>();    
    for (int i = 0; i < numberOfProcess; i++) {
      ProcessDefinition process =
        ProcessBuilder.createProcess("processHAiD" + i, "2.0")
        .addHuman(getLogin())
        .addHumanTask("task1", getLogin())
        .addHumanTask("task2", getLogin())
        .addSystemTask("task3")
        .addSystemTask("task4")
        .addTransition("task1", "task2")
        .addTransition("task2", "task3")
        .addTransition("task3", "task4")
        .done();
      
      process = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process));      
      processUUIDs.add(process.getUUID());
    }
    
    Date since = new Date();
    List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();
    for (int i = 0; i < numberOfProcess; i++) {
      instanceUUIDs.add(getRuntimeAPI().instantiateProcess(processUUIDs.get(i)));
    }
    
    for (int i = 0; i < numberOfProcess; i++) {
      executeTask(instanceUUIDs.get(i), "task1");
      executeTask(instanceUUIDs.get(i), "task2");        
    }
    Date until = new Date();    
    
    List<Long> exptectedDurations = new ArrayList<Long>();
   
    for (int i = 0; i < numberOfProcess - 1; i++) {    
      Set<LightActivityInstance> activityInstances = getQueryRuntimeAPI().getLightActivityInstances(instanceUUIDs.get(i), "task1");
      assertEquals(1, activityInstances.size());
      LightActivityInstance activityInstance = activityInstances.iterator().next();
      exptectedDurations.add(activityInstance.getEndedDate().getTime() - activityInstance.getReadyDate().getTime());    
    
      activityInstances = getQueryRuntimeAPI().getLightActivityInstances(instanceUUIDs.get(i), "task2");
      assertEquals(1, activityInstances.size());
      activityInstance = activityInstances.iterator().next();
      exptectedDurations.add(activityInstance.getEndedDate().getTime() - activityInstance.getReadyDate().getTime());
    }
    
    Set<ProcessDefinitionUUID> fromProcesses = new HashSet<ProcessDefinitionUUID>(processUUIDs.subList(0, numberOfProcess - 1));
    
    List<Long> retrivedDurations = getBAMAPI().getActivityInstancesDurationByActivityTypeFromProcessUUIDs(Type.Human, fromProcesses, since, until);
    assertEquals(2 * (numberOfProcess - 1), retrivedDurations.size());
    Collections.sort(exptectedDurations);
    checkDurationList(exptectedDurations, retrivedDurations);
    
    getManagementAPI().delete(processUUIDs);
  }
  
  public void testGetHumanActivityInstancesDurationFromProcessUUIDsWithLimitedInterval() throws Exception {
    final int numberOfProcess = 3;   
    List<ProcessDefinitionUUID> processUUIDs = new ArrayList<ProcessDefinitionUUID>();    
    for (int i = 0; i < numberOfProcess; i++) {
      ProcessDefinition process =
        ProcessBuilder.createProcess("processHAiD" + i, "2.1")
        .addHuman(getLogin())
        .addHumanTask("task1", getLogin())
        .addHumanTask("task2", getLogin())
        .addSystemTask("task3")
        .addSystemTask("task4")
        .addTransition("task1", "task2")
        .addTransition("task2", "task3")
        .addTransition("task3", "task4")
        .done();
      
      process = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process));      
      processUUIDs.add(process.getUUID());
    }
    
    Date since = new Date();
    List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();
    for (int i = 0; i < numberOfProcess; i++) {
      instanceUUIDs.add(getRuntimeAPI().instantiateProcess(processUUIDs.get(i)));
    }
    
    for (int i = 0; i < numberOfProcess; i++) {
      executeTask(instanceUUIDs.get(i), "task1");              
    }
    Date until = new Date();
    for (int i = 0; i < numberOfProcess; i++) {      
      executeTask(instanceUUIDs.get(i), "task2");        
    }
    
    List<Long> exptectedDurations = new ArrayList<Long>();
   
    for (int i = 0; i < numberOfProcess - 1; i++) {    
      Set<LightActivityInstance> activityInstances = getQueryRuntimeAPI().getLightActivityInstances(instanceUUIDs.get(i), "task1");
      assertEquals(1, activityInstances.size());
      LightActivityInstance activityInstance = activityInstances.iterator().next();
      exptectedDurations.add(activityInstance.getEndedDate().getTime() - activityInstance.getReadyDate().getTime());      
    }
    
    Set<ProcessDefinitionUUID> fromProcesses = new HashSet<ProcessDefinitionUUID>(processUUIDs.subList(0, numberOfProcess - 1));
    
    List<Long> retrivedDurations = getBAMAPI().getActivityInstancesDurationByActivityTypeFromProcessUUIDs(Type.Human, fromProcesses, since, until);
    assertEquals(numberOfProcess - 1, retrivedDurations.size());
    Collections.sort(exptectedDurations);
    checkDurationList(exptectedDurations, retrivedDurations);
    
    getManagementAPI().delete(processUUIDs);
  }
  
  public void testGetSystemActivityInstancesDurationFromProcessUUIDs() throws Exception {
    final int numberOfProcess = 3;   
    List<ProcessDefinitionUUID> processUUIDs = new ArrayList<ProcessDefinitionUUID>();    
    for (int i = 0; i < numberOfProcess; i++) {
      ProcessDefinition process =
        ProcessBuilder.createProcess("processHAiD" + i, "2.0")
        .addHuman(getLogin())
        .addHumanTask("task1", getLogin())
        .addHumanTask("task2", getLogin())
        .addSystemTask("task3")
        .addSystemTask("task4")
        .addTransition("task1", "task2")
        .addTransition("task2", "task3")
        .addTransition("task3", "task4")
        .done();
      
      process = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process));      
      processUUIDs.add(process.getUUID());
    }
    
    Date since = new Date();
    List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();
    for (int i = 0; i < numberOfProcess; i++) {
      instanceUUIDs.add(getRuntimeAPI().instantiateProcess(processUUIDs.get(i)));
    }
    
    for (int i = 0; i < numberOfProcess; i++) {
      executeTask(instanceUUIDs.get(i), "task1");
      executeTask(instanceUUIDs.get(i), "task2");        
    }
    Date until = new Date();    
    
    List<Long> exptectedDurations = new ArrayList<Long>();
   
    for (int i = 0; i < numberOfProcess - 1; i++) {    
      Set<LightActivityInstance> activityInstances = getQueryRuntimeAPI().getLightActivityInstances(instanceUUIDs.get(i), "task3");
      assertEquals(1, activityInstances.size());
      LightActivityInstance activityInstance = activityInstances.iterator().next();
      exptectedDurations.add(activityInstance.getEndedDate().getTime() - activityInstance.getReadyDate().getTime());    
    
      activityInstances = getQueryRuntimeAPI().getLightActivityInstances(instanceUUIDs.get(i), "task4");
      assertEquals(1, activityInstances.size());
      activityInstance = activityInstances.iterator().next();
      exptectedDurations.add(activityInstance.getEndedDate().getTime() - activityInstance.getReadyDate().getTime());
    }
    
    Set<ProcessDefinitionUUID> fromProcesses = new HashSet<ProcessDefinitionUUID>(processUUIDs.subList(0, numberOfProcess - 1));
    
    List<Long> retrivedDurations = getBAMAPI().getActivityInstancesDurationByActivityTypeFromProcessUUIDs(Type.Automatic, fromProcesses, since, until);
    assertEquals(2 * (numberOfProcess - 1), retrivedDurations.size());
    Collections.sort(exptectedDurations);
    checkDurationList(exptectedDurations, retrivedDurations);
    
    getManagementAPI().delete(processUUIDs);
  }
  
  public void testGetNumberOfCreatedProcessInstances() throws Exception {
    ProcessDefinition process =
      ProcessBuilder.createProcess("processNoCI", "1.0")
      .addHuman(getLogin())
      .addHumanTask("task1", getLogin())
      .done();    
    process = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process));
    
    final Date first = new Date();
    final int numberOfInstances = 5;
    getRuntimeAPI().instantiateProcess(process.getUUID());
    
    //sleep 3 milliseconds to avoid conflicting dates
    Thread.sleep(3);
    final Date second = new Date();
    //sleep 3 milliseconds to avoid conflicting dates
    Thread.sleep(3);
    for (int i = 0; i < numberOfInstances - 1 ; i++) {
      getRuntimeAPI().instantiateProcess(process.getUUID());
    }
    
    //sleep 3 milliseconds to avoid conflicting dates
    Thread.sleep(3);
    final Date third = new Date();
    //sleep 3 milliseconds to avoid conflicting dates
    Thread.sleep(3);
    
    long nbOfCreatedInstances = getBAMAPI().getNumberOfCreatedProcessInstances(first, third);
    assertEquals(numberOfInstances, nbOfCreatedInstances);
    
    nbOfCreatedInstances = getBAMAPI().getNumberOfCreatedProcessInstances(first, second);
    assertEquals(1, nbOfCreatedInstances);
    
    nbOfCreatedInstances = getBAMAPI().getNumberOfCreatedProcessInstances(second, third);
    assertEquals(numberOfInstances - 1, nbOfCreatedInstances);
    
    getManagementAPI().deleteProcess(process.getUUID());
  }
  
  public void testGetNumberOfCreatedProcessInstancesWithSomeFinishedProcess() throws Exception {
    ProcessDefinition process =
      ProcessBuilder.createProcess("processNoCI", "1.1")
      .addHuman(getLogin())
      .addHumanTask("task1", getLogin())
      .done();    
    process = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process));
    
    Date since = new Date();
    int numberOfInstances = 5;
    List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();
    for (int i = 0; i < numberOfInstances; i++) {
      instanceUUIDs.add(getRuntimeAPI().instantiateProcess(process.getUUID()));
    }
    Date until = new Date();
    
    executeTask(instanceUUIDs.get(0), "task1");
    
    long nbOfCreatedInstances = getBAMAPI().getNumberOfCreatedProcessInstances(since, until);
    assertEquals(numberOfInstances, nbOfCreatedInstances);
    
    getManagementAPI().deleteProcess(process.getUUID());
  }
  
  public void testGetNumberOfCreatedProcessInstancesWithNocreatedInstances() throws Exception {
    Date since = new Date();    
    ProcessDefinition process =
      ProcessBuilder.createProcess("processNoCI", "1.2")
      .addHuman(getLogin())
      .addHumanTask("task1", getLogin())
      .done();    
    process = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process));    
    
    Date until = new Date();
    
    long nbOfCreatedInstances = getBAMAPI().getNumberOfCreatedProcessInstances(since, until);
    assertEquals(0, nbOfCreatedInstances);
    
    getManagementAPI().deleteProcess(process.getUUID());
  }
  
  public void testGetNumberOfCreatedProcessInstancesFromProcessUUID() throws Exception {
    int numberOfProcess = 3;
    List<ProcessDefinitionUUID> processUUIDs = new ArrayList<ProcessDefinitionUUID>();
    for (int i = 0; i < numberOfProcess; i++) {
      ProcessDefinition process =
        ProcessBuilder.createProcess("processNoCI" + i, "2.0")
        .addHuman(getLogin())
        .addHumanTask("task1", getLogin())
        .done();    
      process = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process));
      
      processUUIDs.add(process.getUUID());
    }
    
    Date first = new Date();    
    for (int i = 0; i < numberOfProcess; i++) {      
        getRuntimeAPI().instantiateProcess(processUUIDs.get(i));            
    }
    Date second = new Date();    
    for (int i = 0; i < numberOfProcess; i++) {      
        getRuntimeAPI().instantiateProcess(processUUIDs.get(i));            
    }
    Date third = new Date();
    
    long nbOfCreatedInstances = getBAMAPI().getNumberOfCreatedProcessInstances(processUUIDs.get(0), first, third);
    assertEquals(2, nbOfCreatedInstances);    
    nbOfCreatedInstances = getBAMAPI().getNumberOfCreatedProcessInstances(first, third);
    assertEquals(2 * numberOfProcess, nbOfCreatedInstances);
    
    nbOfCreatedInstances = getBAMAPI().getNumberOfCreatedProcessInstances(processUUIDs.get(0), first, second);
    assertEquals(1, nbOfCreatedInstances);    
    nbOfCreatedInstances = getBAMAPI().getNumberOfCreatedProcessInstances(first, second);
    assertEquals(numberOfProcess, nbOfCreatedInstances);
    
    nbOfCreatedInstances = getBAMAPI().getNumberOfCreatedProcessInstances(processUUIDs.get(0), second, third);
    assertEquals(1, nbOfCreatedInstances);    
    nbOfCreatedInstances = getBAMAPI().getNumberOfCreatedProcessInstances(second, third);
    assertEquals(numberOfProcess, nbOfCreatedInstances);
    
    getManagementAPI().delete(processUUIDs);
  }
  
  public void testGetNumberOfCreatedProcessInstancesFromProcessUUIDWithNoInstances() throws Exception {
    int numberOfProcess = 3;
    List<ProcessDefinitionUUID> processUUIDs = new ArrayList<ProcessDefinitionUUID>();
    for (int i = 0; i < numberOfProcess; i++) {
      ProcessDefinition process =
        ProcessBuilder.createProcess("processNoCI" + i, "2.1")
        .addHuman(getLogin())
        .addHumanTask("task1", getLogin())
        .done();    
      process = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process));
      
      processUUIDs.add(process.getUUID());
    }
    
    Date since = new Date();        
    for (int i = 1; i < numberOfProcess; i++) {
      getRuntimeAPI().instantiateProcess(processUUIDs.get(i));
    }    
    for (int i = 1; i < numberOfProcess; i++) {
      getRuntimeAPI().instantiateProcess(processUUIDs.get(i));
    }
    Date until = new Date();
    
    long nbOfCreatedInstances = getBAMAPI().getNumberOfCreatedProcessInstances(processUUIDs.get(0), since, until);
    assertEquals(0, nbOfCreatedInstances);    
    
    getManagementAPI().delete(processUUIDs);
  }
  
  public void testGetNumberOfCreatedActivityInstances() throws Exception {
    ProcessDefinition process =
      ProcessBuilder.createProcess("processNoCAI", "1.0")
      .addSystemTask("automatic1")
      .addSystemTask("automatic2")
      .addHuman(getLogin())
      .addHumanTask("task1", getLogin())
      .addSystemTask("automatic3")
      .addSystemTask("automatic4")
      .addTransition("automatic1", "automatic2")
      .addTransition("automatic2", "task1")
      .addTransition("task1", "automatic3")
      .addTransition("automatic3", "automatic4")
      .done();    
    process = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process));
    
    Date first = new Date();
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(process.getUUID());
    Date second = new Date();
    executeTask(instanceUUID, "task1");
    
    long nbOfCreatedActivityInstances = getBAMAPI().getNumberOfCreatedActivityInstances(first, second); 
    assertEquals(3, nbOfCreatedActivityInstances);
    
    nbOfCreatedActivityInstances = getBAMAPI().getNumberOfCreatedActivityInstances(first, new Date()); 
    assertEquals(5, nbOfCreatedActivityInstances);
    
    nbOfCreatedActivityInstances = getBAMAPI().getNumberOfCreatedActivityInstances(second, new Date()); 
    assertEquals(2, nbOfCreatedActivityInstances);
    
    getManagementAPI().deleteProcess(process.getUUID());
  }
  
  public void testGetNumberOfCreatedActivityInstancesWithSomeActivitiesNotInstanciate() throws Exception {
    ProcessDefinition process =
      ProcessBuilder.createProcess("processNoCAI", "1.1")
      .addSystemTask("automatic1")
      .addSystemTask("automatic2")
      .addHuman(getLogin())
      .addHumanTask("task1", getLogin())
      .addSystemTask("automatic3")
      .addSystemTask("automatic4")
      .addTransition("automatic1", "automatic2")
      .addTransition("automatic2", "task1")
      .addTransition("task1", "automatic3")
      .addTransition("automatic3", "automatic4")
      .done();    
    process = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process));
    
    Date first = new Date();
    getRuntimeAPI().instantiateProcess(process.getUUID());       
    
    long nbOfCreatedActivityInstances = getBAMAPI().getNumberOfCreatedActivityInstances(first, new Date()); 
    assertEquals(3, nbOfCreatedActivityInstances);
    
    getManagementAPI().deleteProcess(process.getUUID());
  }
  
  public void testGetNumberOfCreatedActivityInstancesFromProcessUUID() throws Exception {
    int numberOfProcess = 3;
    List<ProcessDefinitionUUID> processUUIDs = new ArrayList<ProcessDefinitionUUID>();
    for (int i = 0; i < numberOfProcess; i++) {
      ProcessDefinition process =
        ProcessBuilder.createProcess("processNoCAI" + i, "2.0")
        .addSystemTask("automatic1")        
        .addHuman(getLogin())
        .addHumanTask("task1", getLogin())
        .addSystemTask("automatic3")
        .addSystemTask("automatic4")
        .addTransition("automatic1", "task1")
        .addTransition("task1", "automatic3")
        .addTransition("automatic3", "automatic4")
        .done();    
      
      process = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process));
      processUUIDs.add(process.getUUID());
    }
    
    Date first = new Date();
    List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();
    for (int i = 0; i < numberOfProcess; i++) {
      instanceUUIDs.add(getRuntimeAPI().instantiateProcess(processUUIDs.get(i)));
    }
    Date second = new Date();
    executeTask(instanceUUIDs.get(0), "task1");
    
    long nbOfCreatedActivityInstances = getBAMAPI().getNumberOfCreatedActivityInstances(processUUIDs.get(0), first, second); 
    assertEquals(2, nbOfCreatedActivityInstances);
    nbOfCreatedActivityInstances = getBAMAPI().getNumberOfCreatedActivityInstances(processUUIDs.get(1), first, second); 
    assertEquals(2, nbOfCreatedActivityInstances);
    
    nbOfCreatedActivityInstances = getBAMAPI().getNumberOfCreatedActivityInstances(processUUIDs.get(0), first, new Date()); 
    assertEquals(4, nbOfCreatedActivityInstances);
    nbOfCreatedActivityInstances = getBAMAPI().getNumberOfCreatedActivityInstances(processUUIDs.get(1), first, new Date()); 
    assertEquals(2, nbOfCreatedActivityInstances);
    
    nbOfCreatedActivityInstances = getBAMAPI().getNumberOfCreatedActivityInstances(processUUIDs.get(0), second, new Date()); 
    assertEquals(2, nbOfCreatedActivityInstances);
    nbOfCreatedActivityInstances = getBAMAPI().getNumberOfCreatedActivityInstances(processUUIDs.get(1), second, new Date()); 
    assertEquals(0, nbOfCreatedActivityInstances);
    
    getManagementAPI().delete(processUUIDs);
  }
  

  
  public void testGetNumberOfCreatedActivityInstancesFromProcessUUIDs() throws Exception {
    int numberOfProcess = 3;
    List<ProcessDefinitionUUID> processUUIDs = new ArrayList<ProcessDefinitionUUID>();
    for (int i = 0; i < numberOfProcess; i++) {
      ProcessDefinition process =
        ProcessBuilder.createProcess("processNoCAI" + i, "2.0")
        .addSystemTask("automatic1")        
        .addHuman(getLogin())
        .addHumanTask("task1", getLogin())
        .addSystemTask("automatic3")
        .addSystemTask("automatic4")
        .addTransition("automatic1", "task1")
        .addTransition("task1", "automatic3")
        .addTransition("automatic3", "automatic4")
        .done();    
      
      process = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process));
      processUUIDs.add(process.getUUID());
    }
    
    Date first = new Date();
    List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();
    for (int i = 0; i < numberOfProcess; i++) {
      instanceUUIDs.add(getRuntimeAPI().instantiateProcess(processUUIDs.get(i)));
    }
    Date second = new Date();
    executeTask(instanceUUIDs.get(0), "task1");
    
    Set<ProcessDefinitionUUID> fromProcesses = new HashSet<ProcessDefinitionUUID>(processUUIDs.subList(0, 2));
    
    long nbOfCreatedActivityInstances = getBAMAPI().getNumberOfCreatedActivityInstancesFromProcessUUIDs(fromProcesses, first, second); 
    assertEquals(4, nbOfCreatedActivityInstances);
    
    nbOfCreatedActivityInstances = getBAMAPI().getNumberOfCreatedActivityInstancesFromProcessUUIDs(fromProcesses, first, new Date()); 
    assertEquals(6, nbOfCreatedActivityInstances);
    
    nbOfCreatedActivityInstances = getBAMAPI().getNumberOfCreatedActivityInstancesFromProcessUUIDs(fromProcesses, second, new Date()); 
    assertEquals(2, nbOfCreatedActivityInstances);
    
    getManagementAPI().delete(processUUIDs);
  }
  
  public void testGetNumberOfCreatedActivityInstancesFromActivityDefinitionUUID() throws Exception {
    ProcessDefinition process =
      ProcessBuilder.createProcess("processNoCAI", "3.0")
      .addSystemTask("automatic1")
      .addSystemTask("automatic2")
      .addHuman(getLogin())
      .addHumanTask("task1", getLogin())
      .addSystemTask("automatic3")
      .addSystemTask("automatic4")
      .addTransition("automatic1", "automatic2")
      .addTransition("automatic2", "task1")
      .addTransition("task1", "automatic3")
      .addTransition("automatic3", "automatic4")
      .done();    
    process = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process));
    
    Date first = new Date();
    int numberOfInstances = 4;
    List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();
    instanceUUIDs.add(getRuntimeAPI().instantiateProcess(process.getUUID()));
    Date second = new Date();
    for (int i = 0; i < numberOfInstances - 1; i++) {
      instanceUUIDs.add(getRuntimeAPI().instantiateProcess(process.getUUID()));
    }
    executeTask(instanceUUIDs.get(0), "task1");
    
    ActivityDefinitionUUID activityUUID = getQueryDefinitionAPI().getProcessActivity(process.getUUID(), "automatic1").getUUID();
    long nbOfCreatedActivityInstances = getBAMAPI().getNumberOfCreatedActivityInstances(activityUUID, first, second); 
    assertEquals(1, nbOfCreatedActivityInstances);
    nbOfCreatedActivityInstances = getBAMAPI().getNumberOfCreatedActivityInstances(activityUUID, first, new Date()); 
    assertEquals(numberOfInstances, nbOfCreatedActivityInstances);
    
    activityUUID = getQueryDefinitionAPI().getProcessActivity(process.getUUID(), "automatic3").getUUID();
    nbOfCreatedActivityInstances = getBAMAPI().getNumberOfCreatedActivityInstances(activityUUID, first, second); 
    assertEquals(0, nbOfCreatedActivityInstances);
    nbOfCreatedActivityInstances = getBAMAPI().getNumberOfCreatedActivityInstances(activityUUID, first, new Date()); 
    assertEquals(1, nbOfCreatedActivityInstances);
     
    getManagementAPI().deleteProcess(process.getUUID());
  }
  
  public void testGetNumberOfCreatedActivityInstancesFromActivityDefinitionUUIDs() throws Exception {
    ProcessDefinition process =
      ProcessBuilder.createProcess("processNoCAI", "3.1")
      .addSystemTask("automatic1")
      .addSystemTask("automatic2")
      .addHuman(getLogin())
      .addHumanTask("task1", getLogin())
      .addSystemTask("automatic3")
      .addSystemTask("automatic4")
      .addTransition("automatic1", "automatic2")
      .addTransition("automatic2", "task1")
      .addTransition("task1", "automatic3")
      .addTransition("automatic3", "automatic4")
      .done();    
    process = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process));
    
    Date first = new Date();
    int numberOfInstances = 4;
    List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();
    instanceUUIDs.add(getRuntimeAPI().instantiateProcess(process.getUUID()));
    Date second = new Date();
    for (int i = 0; i < numberOfInstances - 1; i++) {
      instanceUUIDs.add(getRuntimeAPI().instantiateProcess(process.getUUID()));
    }
    executeTask(instanceUUIDs.get(0), "task1");
    
    Set<ActivityDefinitionUUID> fromActivities = new HashSet<ActivityDefinitionUUID>();
    fromActivities.add(getQueryDefinitionAPI().getProcessActivity(process.getUUID(), "automatic1").getUUID());
    fromActivities.add(getQueryDefinitionAPI().getProcessActivity(process.getUUID(), "automatic3").getUUID());
    fromActivities.add(getQueryDefinitionAPI().getProcessActivity(process.getUUID(), "task1").getUUID());
    long nbOfCreatedActivityInstances = getBAMAPI().getNumberOfCreatedActivityInstancesFromActivityUUIDs(fromActivities, first, second); 
    assertEquals(2, nbOfCreatedActivityInstances);
    nbOfCreatedActivityInstances = getBAMAPI().getNumberOfCreatedActivityInstancesFromActivityUUIDs(fromActivities, first, new Date()); 
    assertEquals(2 * numberOfInstances + 1, nbOfCreatedActivityInstances);    
     
    getManagementAPI().deleteProcess(process.getUUID());
  }
  
  public void testGetNumberOfCreatedActivityInstancesByActivityType() throws Exception {
    ProcessDefinition process =
      ProcessBuilder.createProcess("processNoCAI", "4.0")
      .addSystemTask("automatic1")
      .addSystemTask("automatic2")
      .addHuman(getLogin())
      .addHumanTask("task1", getLogin())      
      .addSystemTask("automatic3")
      .addHumanTask("task2", getLogin())
      .addSystemTask("automatic4")
      .addTransition("automatic1", "automatic2")
      .addTransition("automatic2", "task1")      
      .addTransition("task1", "automatic3")
      .addTransition("automatic3", "task2")
      .addTransition("task2", "automatic4")
      
      .done();    
    process = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process));
    
    Date first = new Date();
    ProcessInstanceUUID intanceUUID = getRuntimeAPI().instantiateProcess(process.getUUID());
    Date second = new Date();
    
    long nbOfCreatedActivityInstances = getBAMAPI().getNumberOfCreatedActivityInstancesByActivityType(Type.Human, first, second); 
    assertEquals(1, nbOfCreatedActivityInstances);
    nbOfCreatedActivityInstances = getBAMAPI().getNumberOfCreatedActivityInstancesByActivityType(Type.Automatic, first, second); 
    assertEquals(2, nbOfCreatedActivityInstances);
    
    executeTask(intanceUUID, "task1");
    Date third = new Date();
    
    nbOfCreatedActivityInstances = getBAMAPI().getNumberOfCreatedActivityInstancesByActivityType(Type.Human, first, third); 
    assertEquals(2, nbOfCreatedActivityInstances);
    nbOfCreatedActivityInstances = getBAMAPI().getNumberOfCreatedActivityInstancesByActivityType(Type.Automatic, first, third); 
    assertEquals(3, nbOfCreatedActivityInstances);
    
    executeTask(intanceUUID, "task2");
    Date fourth = new Date();
    nbOfCreatedActivityInstances = getBAMAPI().getNumberOfCreatedActivityInstancesByActivityType(Type.Human, first, fourth); 
    assertEquals(2, nbOfCreatedActivityInstances);
    nbOfCreatedActivityInstances = getBAMAPI().getNumberOfCreatedActivityInstancesByActivityType(Type.Automatic, first, fourth); 
    assertEquals(4, nbOfCreatedActivityInstances);
    
    getManagementAPI().deleteProcess(process.getUUID());
  } 
  
  public void testGetNumberOfCreatedActivityInstancesByActivityTypeFromProcessUUID() throws Exception {
    int numberOfProcess = 3;
    List<ProcessDefinitionUUID> processUUIDs = new ArrayList<ProcessDefinitionUUID>();
    for (int i = 0; i < numberOfProcess; i++) {
      ProcessDefinition process =
        ProcessBuilder.createProcess("processNoCAI" + i, "5.0")
        .addSystemTask("automatic1")
        .addSystemTask("automatic2")
        .addHuman(getLogin())
        .addHumanTask("task1", getLogin())      
        .addSystemTask("automatic3")
        .addHumanTask("task2", getLogin())
        .addSystemTask("automatic4")
        .addTransition("automatic1", "automatic2")
        .addTransition("automatic2", "task1")      
        .addTransition("task1", "automatic3")
        .addTransition("automatic3", "task2")
        .addTransition("task2", "automatic4")
        
        .done();    
      process = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process));
      processUUIDs.add(process.getUUID());
    }
    
    Date first = new Date();
    List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();
    for (int i = 0; i < numberOfProcess; i++) {
      instanceUUIDs.add(getRuntimeAPI().instantiateProcess(processUUIDs.get(i)));
    }
    Date second = new Date();
    
    long nbOfCreatedActivityInstances = getBAMAPI().getNumberOfCreatedActivityInstancesByActivityType(Type.Human, processUUIDs.get(0), first, second); 
    assertEquals(1, nbOfCreatedActivityInstances);
    nbOfCreatedActivityInstances = getBAMAPI().getNumberOfCreatedActivityInstancesByActivityType(Type.Automatic, processUUIDs.get(0), first, second); 
    assertEquals(2 , nbOfCreatedActivityInstances);
    
    for (int i = 0; i < numberOfProcess; i++) {
      executeTask(instanceUUIDs.get(i), "task1");
    }
    
    Date third = new Date();
    
    nbOfCreatedActivityInstances = getBAMAPI().getNumberOfCreatedActivityInstancesByActivityType(Type.Human, processUUIDs.get(0), first, third); 
    assertEquals(2, nbOfCreatedActivityInstances);
    nbOfCreatedActivityInstances = getBAMAPI().getNumberOfCreatedActivityInstancesByActivityType(Type.Automatic, processUUIDs.get(0), first, third); 
    assertEquals(3, nbOfCreatedActivityInstances);
    
    for (int i = 0; i < numberOfProcess; i++) {
      executeTask(instanceUUIDs.get(i), "task2");
    }
    
    Date fourth = new Date();
    nbOfCreatedActivityInstances = getBAMAPI().getNumberOfCreatedActivityInstancesByActivityType(Type.Human, first, fourth); 
    assertEquals(2 * numberOfProcess, nbOfCreatedActivityInstances);
    nbOfCreatedActivityInstances = getBAMAPI().getNumberOfCreatedActivityInstancesByActivityType(Type.Automatic, first, fourth); 
    assertEquals(4 * numberOfProcess, nbOfCreatedActivityInstances);
    
    nbOfCreatedActivityInstances = getBAMAPI().getNumberOfCreatedActivityInstancesByActivityType(Type.Human, processUUIDs.get(0), first, fourth); 
    assertEquals(2, nbOfCreatedActivityInstances);
    nbOfCreatedActivityInstances = getBAMAPI().getNumberOfCreatedActivityInstancesByActivityType(Type.Automatic, processUUIDs.get(0), first, fourth); 
    assertEquals(4, nbOfCreatedActivityInstances);
    
    getManagementAPI().delete(processUUIDs);
  } 
  
  public void testGetNumberOfCreatedActivityInstancesByActivityTypeFromProcessUUIDs() throws Exception {
    int numberOfProcess = 3;
    List<ProcessDefinitionUUID> processUUIDs = new ArrayList<ProcessDefinitionUUID>();
    for (int i = 0; i < numberOfProcess; i++) {
      ProcessDefinition process =
        ProcessBuilder.createProcess("processNoCAI" + i, "6.0")
        .addSystemTask("automatic1")
        .addSystemTask("automatic2")
        .addHuman(getLogin())
        .addHumanTask("task1", getLogin())      
        .addSystemTask("automatic3")
        .addHumanTask("task2", getLogin())
        .addSystemTask("automatic4")
        .addTransition("automatic1", "automatic2")
        .addTransition("automatic2", "task1")      
        .addTransition("task1", "automatic3")
        .addTransition("automatic3", "task2")
        .addTransition("task2", "automatic4")
        
        .done();    
      process = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process));
      processUUIDs.add(process.getUUID());
    }
    
    Date first = new Date();
    List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();
    for (int i = 0; i < numberOfProcess; i++) {
      instanceUUIDs.add(getRuntimeAPI().instantiateProcess(processUUIDs.get(i)));
    }
    Date second = new Date();
    
    Set<ProcessDefinitionUUID> fromProcesses = new HashSet<ProcessDefinitionUUID>(processUUIDs.subList(0, 2));
    
    long nbOfCreatedActivityInstances = getBAMAPI().getNumberOfCreatedActivityInstancesByActivityTypeFromProcessUUIDs(Type.Human, fromProcesses, first, second); 
    assertEquals(2, nbOfCreatedActivityInstances);
    nbOfCreatedActivityInstances = getBAMAPI().getNumberOfCreatedActivityInstancesByActivityTypeFromProcessUUIDs(Type.Automatic, fromProcesses, first, second); 
    assertEquals(4 , nbOfCreatedActivityInstances);
    
    for (int i = 0; i < numberOfProcess; i++) {
      executeTask(instanceUUIDs.get(i), "task1");
    }
    
    Date third = new Date();
    
    nbOfCreatedActivityInstances = getBAMAPI().getNumberOfCreatedActivityInstancesByActivityTypeFromProcessUUIDs(Type.Human, fromProcesses, first, third); 
    assertEquals(4, nbOfCreatedActivityInstances);
    nbOfCreatedActivityInstances = getBAMAPI().getNumberOfCreatedActivityInstancesByActivityTypeFromProcessUUIDs(Type.Automatic, fromProcesses, first, third); 
    assertEquals(6, nbOfCreatedActivityInstances);
    
    executeTask(instanceUUIDs.get(0), "task2");
    
    Date fourth = new Date();
    nbOfCreatedActivityInstances = getBAMAPI().getNumberOfCreatedActivityInstancesByActivityType(Type.Human, first, fourth); 
    assertEquals(6, nbOfCreatedActivityInstances);
    nbOfCreatedActivityInstances = getBAMAPI().getNumberOfCreatedActivityInstancesByActivityType(Type.Automatic, first, fourth); 
    assertEquals(10, nbOfCreatedActivityInstances);
    
    nbOfCreatedActivityInstances = getBAMAPI().getNumberOfCreatedActivityInstancesByActivityTypeFromProcessUUIDs(Type.Human, fromProcesses, first, fourth); 
    assertEquals(4, nbOfCreatedActivityInstances);
    nbOfCreatedActivityInstances = getBAMAPI().getNumberOfCreatedActivityInstancesByActivityTypeFromProcessUUIDs(Type.Automatic, fromProcesses, first, fourth); 
    assertEquals(7, nbOfCreatedActivityInstances);
    
    getManagementAPI().delete(processUUIDs);
  }

  public void testGetCurrentMemoryUsage() throws MonitoringException{
      Assert.assertTrue(getBAMAPI().getCurrentMemoryUsage()>0);
  }
  
  public void testGetMemoryUsagePercentage() throws MonitoringException{
      Assert.assertTrue(getBAMAPI().getMemoryUsagePercentage()>=0);
      Assert.assertTrue(getBAMAPI().getMemoryUsagePercentage()<=100);
  }

  public void testGetSystemLoadAverage() throws MonitoringException{
      double result = getBAMAPI().getSystemLoadAverage();
      if (result<0){
          LOG.warning("the getSystemLoadAverage method is not available");
      }
      Assert.assertTrue(getBAMAPI().getSystemLoadAverage()!=0);
  }

  public void testGetUpTime() throws MonitoringException{
      Assert.assertTrue(getBAMAPI().getUpTime()>0);
  }  

  public void testGetStartTime() throws MonitoringException{
      Assert.assertTrue(getBAMAPI().getStartTime()>0);
  }

  public void testGetTotalThreadsCpuTime() throws MonitoringException{
      Assert.assertTrue(getBAMAPI().getTotalThreadsCpuTime()>0);
      long result = getBAMAPI().getTotalThreadsCpuTime();
      if (result<0){
          LOG.warning("the getTotalThreadsCpuTime method is not available");
      }
      Assert.assertTrue(getBAMAPI().getTotalThreadsCpuTime()!=0);      
  }
  
  public void testGetThreadCount() throws MonitoringException{
      Assert.assertTrue(getBAMAPI().getThreadCount()>0);
  }  
  
  public void testGetAvailableProcessors() throws MonitoringException{
      Assert.assertTrue(getBAMAPI().getAvailableProcessors()>0);
  }
  
  public void testGetOSArch() throws MonitoringException{
      Assert.assertNotNull(getBAMAPI().getOSArch());
  }
  
  public void testGetOSName() throws MonitoringException{
      Assert.assertNotNull(getBAMAPI().getOSName());
  }
  
  public void testGetOSVersion() throws MonitoringException{
      Assert.assertNotNull(getBAMAPI().getOSVersion());
  }
  
  public void testGetJvmName() throws MonitoringException{
      Assert.assertNotNull(getBAMAPI().getJvmName());
  }

  public void testGetJvmVendor() throws MonitoringException{
      Assert.assertNotNull(getBAMAPI().getJvmVendor());
  }
  
  public void testGetJvmVersion() throws MonitoringException{
      Assert.assertNotNull(getBAMAPI().getJvmVersion());
  }
  
  public void testGetJvmSystemProperties() throws MonitoringException{
      final Map<String,String> systemProperties = getBAMAPI().getJvmSystemProperties();
      Assert.assertNotNull(systemProperties);
  }
  
}
