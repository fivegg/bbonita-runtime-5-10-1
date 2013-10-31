package org.ow2.bonita.activity.subflow;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Vector;

import org.bonitasoft.connectors.bonita.SetVarConnector;
import org.bonitasoft.connectors.bonita.resolvers.ProcessInitiatorRoleResolver;
import org.bonitasoft.connectors.bonita.resolvers.UserListRoleResolver;
import org.ow2.bonita.APITestCase;
import org.ow2.bonita.definition.Hook;
import org.ow2.bonita.definition.MultiInstantiator;
import org.ow2.bonita.definition.MultiInstantiatorDescriptor;
import org.ow2.bonita.definition.TxHook;
import org.ow2.bonita.facade.APIAccessor;
import org.ow2.bonita.facade.QueryAPIAccessor;
import org.ow2.bonita.facade.QueryRuntimeAPI;
import org.ow2.bonita.facade.RuntimeAPI;
import org.ow2.bonita.facade.def.element.HookDefinition.Event;
import org.ow2.bonita.facade.def.majorElement.ActivityDefinition.JoinType;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.runtime.ActivityInstance;
import org.ow2.bonita.facade.runtime.ActivityState;
import org.ow2.bonita.facade.runtime.InstanceState;
import org.ow2.bonita.facade.runtime.ProcessInstance;
import org.ow2.bonita.facade.runtime.TaskInstance;
import org.ow2.bonita.facade.runtime.command.GetProcessInstancesActivitiesCommand;
import org.ow2.bonita.facade.uuid.ActivityInstanceUUID;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.light.LightActivityInstance;
import org.ow2.bonita.light.LightProcessInstance;
import org.ow2.bonita.util.AccessorUtil;
import org.ow2.bonita.util.BonitaException;
import org.ow2.bonita.util.ProcessBuilder;

public class SubflowTest extends APITestCase {

  public void testSubProcessNotArchivedBeforeparentInstance() throws Exception {
    ProcessDefinition parent = ProcessBuilder.createProcess("parent", "1.0")
    .addHuman(getLogin())
    .addSubProcess("sub", "sub")
    .addHumanTask("human", getLogin())
    .addTransition("sub", "human")
    .done();

    ProcessDefinition sub = ProcessBuilder.createProcess("sub", "1.0")
    .addSystemTask("auto")
    .done();

    sub = getManagementAPI().deploy(getBusinessArchive(sub));
    parent = getManagementAPI().deploy(getBusinessArchive(parent));

    final ProcessDefinitionUUID parentProcessUUID = parent.getUUID();
    final ProcessDefinitionUUID subProcessUUID = sub.getUUID();

    final ProcessInstanceUUID parentInstanceUUID = getRuntimeAPI().instantiateProcess(parentProcessUUID);

    final Collection<TaskInstance> tasks = getQueryRuntimeAPI().getTaskList(parentInstanceUUID, ActivityState.READY);

    assertEquals(1, tasks.size());

    //checks the sub process was not archived
    assertEquals(0, AccessorUtil.getQueryRuntimeAPI(AccessorUtil.QUERYLIST_HISTORY_KEY).getLightProcessInstances(subProcessUUID).size());

    getRuntimeAPI().executeTask(tasks.iterator().next().getUUID(), true);

    //check the process was archived
    assertEquals(1, AccessorUtil.getQueryRuntimeAPI(AccessorUtil.QUERYLIST_HISTORY_KEY).getLightProcessInstances(subProcessUUID).size());

    getManagementAPI().deleteProcess(parentProcessUUID);
    getManagementAPI().deleteProcess(subProcessUUID);
  }

  public void testDisableCrossDependency() throws Exception {
    ProcessDefinition parent = ProcessBuilder.createProcess("parent", "1.0")
    .addSubProcess("sub", "sub")
    .done();

    ProcessDefinition sub = ProcessBuilder.createProcess("sub", "1.0")
    .addSubProcess("parent", "parent")
    .done();

    sub = getManagementAPI().deploy(getBusinessArchive(sub));
    parent = getManagementAPI().deploy(getBusinessArchive(parent));

    final ProcessDefinitionUUID parentProcessUUID = parent.getUUID();
    final ProcessDefinitionUUID subProcessUUID = sub.getUUID();

    getManagementAPI().disable(parentProcessUUID);
    getManagementAPI().disable(subProcessUUID);

    getManagementAPI().deleteProcess(parentProcessUUID);
    getManagementAPI().deleteProcess(subProcessUUID);
  }

  public void testDeleteCrossDependency() throws Exception {
    ProcessDefinition parent = ProcessBuilder.createProcess("parent", "1.0")
    .addSubProcess("sub", "sub")
    .done();

    ProcessDefinition sub = ProcessBuilder.createProcess("sub", "1.0")
    .addSubProcess("parent", "parent")
    .done();

    sub = getManagementAPI().deploy(getBusinessArchive(sub));
    parent = getManagementAPI().deploy(getBusinessArchive(parent));

    final ProcessDefinitionUUID parentProcessUUID = parent.getUUID();
    final ProcessDefinitionUUID subProcessUUID = sub.getUUID();

    getManagementAPI().deleteProcess(parentProcessUUID);
    getManagementAPI().deleteProcess(subProcessUUID);
  }

  public void testDisableCrossDependencyExecution() throws Exception {
    ProcessDefinition parent = ProcessBuilder.createProcess("parent", "1.0")
    .addBooleanData("exit", false)
    .addHuman(getLogin())
    .addSystemTask("start")
    .addSubProcess("sub", "sub")
    .addSubProcessOutParameter("exit", "exit")
    .addHumanTask("wait", getLogin())
    .addSystemTask("end")
    .addTransition("start", "sub")
    .addCondition("exit == true")
    .addTransition("sub", "wait")
    .addTransition("wait", "end")
    .addTransition("start", "end")
    .addCondition("exit == false")
    .done();

    ProcessDefinition sub = ProcessBuilder.createProcess("sub", "1.0")
    .addBooleanData("exit", true)
    .addSystemTask("start")
    .addSubProcess("parent", "parent")
    .addSubProcessInParameter("exit", "exit")
    .addSystemTask("end")
    .addTransition("start", "parent")
    .addTransition("parent", "end")
    .done();

    sub = getManagementAPI().deploy(getBusinessArchive(sub));
    parent = getManagementAPI().deploy(getBusinessArchive(parent));

    final ProcessDefinitionUUID parentProcessUUID = parent.getUUID();
    final ProcessDefinitionUUID subProcessUUID = sub.getUUID();

    getRuntimeAPI().instantiateProcess(parentProcessUUID);

    getManagementAPI().disable(subProcessUUID);
    getManagementAPI().disable(parentProcessUUID);

    getManagementAPI().deleteProcess(subProcessUUID);
    getManagementAPI().deleteProcess(parentProcessUUID);
  }

  public void testDeleteCrossDependencyExecution() throws Exception {
    ProcessDefinition parent = ProcessBuilder.createProcess("parent", "1.0")
    .addBooleanData("exit", false)
    .addHuman(getLogin())
    .addSystemTask("start")
    .addSubProcess("sub", "sub")
    .addSubProcessOutParameter("exit", "exit")
    .addHumanTask("wait", getLogin())
    .addSystemTask("end")
    .addTransition("start", "sub")
    .addCondition("exit == true")
    .addTransition("sub", "wait")
    .addTransition("wait", "end")
    .addTransition("start", "end")
    .addCondition("exit == false")
    .done();

    ProcessDefinition sub = ProcessBuilder.createProcess("sub", "1.0")
    .addBooleanData("exit", true)
    .addSystemTask("start")
    .addSubProcess("parent", "parent")
    .addSubProcessInParameter("exit", "exit")
    .addSystemTask("end")
    .addTransition("start", "parent")
    .addTransition("parent", "end")
    .done();

    sub = getManagementAPI().deploy(getBusinessArchive(sub));
    parent = getManagementAPI().deploy(getBusinessArchive(parent));

    final ProcessDefinitionUUID parentProcessUUID = parent.getUUID();
    final ProcessDefinitionUUID subProcessUUID = sub.getUUID();

    getRuntimeAPI().instantiateProcess(parentProcessUUID);

    getManagementAPI().deleteProcess(subProcessUUID);
    getManagementAPI().deleteProcess(parentProcessUUID);
  }

  public void testDynamicSubflowVersion() throws Exception {
    ProcessDefinition parent = ProcessBuilder.createProcess("parent", "1.0")
    .addStringData("subVersion", "1.0")
    .addSubProcess("aSubProcess", "sub", "${subVersion}")
    .done();

    ProcessDefinition sub1 = ProcessBuilder.createProcess("sub", "1.0")
    .addSystemTask("s")
    .done();

    sub1 = getManagementAPI().deploy(getBusinessArchive(sub1));
    parent = getManagementAPI().deploy(getBusinessArchive(parent));

    final ProcessDefinitionUUID parentProcessUUID = parent.getUUID();
    final ProcessDefinitionUUID sub1ProcessUUID = sub1.getUUID();

    checkSubProcess(parentProcessUUID, sub1ProcessUUID);

    getManagementAPI().deleteProcess(parentProcessUUID);
    getManagementAPI().deleteProcess(sub1ProcessUUID);
  }

  public void testLastestSubflowVersion() throws Exception {
    ProcessDefinition parent = ProcessBuilder.createProcess("parent", "1.0")
    .addSubProcess("aSubProcess", "sub")
    .done();

    ProcessDefinition sub1 = ProcessBuilder.createProcess("sub", "1.0")
    .addSystemTask("s")
    .done();

    ProcessDefinition sub2 = ProcessBuilder.createProcess("sub", "2.0")
    .addSystemTask("s")
    .done();

    sub1 = getManagementAPI().deploy(getBusinessArchive(sub1));
    parent = getManagementAPI().deploy(getBusinessArchive(parent));

    final ProcessDefinitionUUID parentProcessUUID = parent.getUUID();
    final ProcessDefinitionUUID sub1ProcessUUID = sub1.getUUID();

    checkSubProcess(parentProcessUUID, sub1ProcessUUID);

    sub2 = getManagementAPI().deploy(getBusinessArchive(sub2));
    final ProcessDefinitionUUID sub2ProcessUUID = sub2.getUUID();

    checkSubProcess(parentProcessUUID, sub2ProcessUUID);

    getManagementAPI().deleteProcess(parentProcessUUID);
    getManagementAPI().deleteProcess(sub1ProcessUUID);
    getManagementAPI().deleteProcess(sub2ProcessUUID);
  }

  public void testChooseSubflowVersion() throws Exception {
    ProcessDefinition parent = ProcessBuilder.createProcess("parent", "1.0")
    .addSubProcess("aSubProcess", "sub", "1.0")
    .done();

    ProcessDefinition sub1 = ProcessBuilder.createProcess("sub", "1.0")
    .addSystemTask("s")
    .done();

    ProcessDefinition sub2 = ProcessBuilder.createProcess("sub", "2.0")
    .addSystemTask("s")
    .done();

    sub1 = getManagementAPI().deploy(getBusinessArchive(sub1));
    parent = getManagementAPI().deploy(getBusinessArchive(parent));

    final ProcessDefinitionUUID parentProcessUUID = parent.getUUID();
    final ProcessDefinitionUUID sub1ProcessUUID = sub1.getUUID();

    checkSubProcess(parentProcessUUID, sub1ProcessUUID);

    sub2 = getManagementAPI().deploy(getBusinessArchive(sub2));
    final ProcessDefinitionUUID sub2ProcessUUID = sub2.getUUID();

    checkSubProcess(parentProcessUUID, sub1ProcessUUID);

    getManagementAPI().deleteProcess(parentProcessUUID);
    getManagementAPI().deleteProcess(sub1ProcessUUID);
    getManagementAPI().deleteProcess(sub2ProcessUUID);
  }

  private void checkSubProcess(final ProcessDefinitionUUID parentProcessUUID, final ProcessDefinitionUUID expectedSubProcessDefinitionUUID) throws Exception {
    final ProcessInstanceUUID parentInstanceUUID = getRuntimeAPI().instantiateProcess(parentProcessUUID);
    final ProcessInstance parentInstance1 = getQueryRuntimeAPI().getProcessInstance(parentInstanceUUID);
    assertNotNull(parentInstance1.getChildrenInstanceUUID());
    assertEquals(1, parentInstance1.getChildrenInstanceUUID().size());
    final ProcessInstance childInstance = getQueryRuntimeAPI().getProcessInstance( parentInstance1.getChildrenInstanceUUID().iterator().next());
    assertEquals(expectedSubProcessDefinitionUUID, childInstance.getProcessDefinitionUUID());
  }

  public void testAutoFinishSubflow() throws BonitaException {
    ProcessDefinition process = ProcessBuilder.createProcess("parentProcess", "1.0")
    .addHuman(getLogin())
    .addHumanTask("t1", getLogin())
    .addHumanTask("t2", getLogin())
    .addSystemTask("s")
    .addSubProcess("aSubProcess", "aSubProcess")
    .done();

    ProcessDefinition subProcess = ProcessBuilder.createProcess("aSubProcess", "1.0")
    .addHuman(getLogin())
    .addHumanTask("t1", getLogin())
    .addSystemTask("s")
    .done();

    subProcess = getManagementAPI().deploy(getBusinessArchive(subProcess));
    process = getManagementAPI().deploy(getBusinessArchive(process));

    final ProcessDefinitionUUID processUUID = process.getUUID();

    getRuntimeAPI().instantiateProcess(processUUID);

    getManagementAPI().deleteProcess(processUUID);
    getManagementAPI().deleteProcess(subProcess.getUUID());
  }

  //Automatic process that creates a subflow with only Automatic activities.
  public void testAutoSubFlow() throws BonitaException {
    ProcessDefinition subProcess = getManagementAPI().deploy(getBusinessArchiveFromXpdl(this.getClass().getResource("subflowAuto-sub.xpdl")));
    ProcessDefinitionUUID subProcessUUID = subProcess.getUUID();

    ProcessDefinition process = getManagementAPI().deploy(getBusinessArchiveFromXpdl(this.getClass().getResource("subflowAuto.xpdl")));
    ProcessDefinitionUUID processUUID = process.getUUID();

    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    checkExecutedOnce(instanceUUID, new String[]{"a1", "a2", "a3"});

    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
    getManagementAPI().disable(subProcessUUID);
    getManagementAPI().deleteProcess(subProcessUUID);
  }

  //Automatic process that creates a subflow with a Manual activity.
  public void testSubFlowWithTask() throws BonitaException {
    ProcessDefinition subProcess = getManagementAPI().deploy(getBusinessArchiveFromXpdl(this.getClass().getResource("subflow-sub.xpdl")));
    ProcessDefinitionUUID subProcessUUID = subProcess.getUUID();

    ProcessDefinition process = getManagementAPI().deploy(getBusinessArchiveFromXpdl(this.getClass().getResource("subflow.xpdl")));
    ProcessDefinitionUUID processUUID = process.getUUID();

    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);
    checkStopped(instanceUUID, new String[]{"a1"});

    Collection<TaskInstance> taskActivities = getQueryRuntimeAPI().getTaskList(ActivityState.READY);
    assertEquals(1, taskActivities.size());
    TaskInstance taskActivity = taskActivities.iterator().next();
    ProcessInstance processInstance = getQueryRuntimeAPI().getProcessInstance(taskActivity.getProcessInstanceUUID());
    assertEquals(instanceUUID, processInstance.getParentInstanceUUID());
    ActivityInstanceUUID taskUUID = taskActivity.getUUID();
    getRuntimeAPI().startTask(taskUUID, true);
    getRuntimeAPI().finishTask(taskUUID, true);

    checkExecutedOnce(instanceUUID, new String[]{"a1", "a2", "a3"});

    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
    getManagementAPI().disable(subProcessUUID);
    getManagementAPI().deleteProcess(subProcessUUID);
  }

  //Main process creates a subflow instance, which creates a new subflow.
  public void testNestedSubFlowWithTask() throws BonitaException {
    ProcessDefinition nestedsubProcess = getManagementAPI().deploy(getBusinessArchiveFromXpdl(this.getClass().getResource("nestedSubflow-nestedSub.xpdl")));
    ProcessDefinitionUUID nestedsubProcessUUID = nestedsubProcess.getUUID();

    ProcessDefinition subProcess = getManagementAPI().deploy(getBusinessArchiveFromXpdl(this.getClass().getResource("nestedSubflow-sub.xpdl")));
    ProcessDefinitionUUID subProcessUUID = subProcess.getUUID();

    ProcessDefinition process = getManagementAPI().deploy(getBusinessArchiveFromXpdl(this.getClass().getResource("nestedSubflow.xpdl")));
    ProcessDefinitionUUID processUUID = process.getUUID();

    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);
    checkStopped(instanceUUID, new String[]{"a1"});

    Collection<TaskInstance> taskActivities = getQueryRuntimeAPI().getTaskList(ActivityState.READY);
    assertEquals(1, taskActivities.size());
    ActivityInstanceUUID taskUUID = taskActivities.iterator().next().getUUID();
    getRuntimeAPI().startTask(taskUUID, true);
    getRuntimeAPI().finishTask(taskUUID, true);

    checkExecutedOnce(instanceUUID, new String[]{"a1", "a2", "a3"});

    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
    getManagementAPI().disable(subProcessUUID);
    getManagementAPI().deleteProcess(subProcessUUID);
    getManagementAPI().disable(nestedsubProcessUUID);
    getManagementAPI().deleteProcess(nestedsubProcessUUID);
  }

  public void testSubFlowWithManyChildInstances() throws BonitaException {
    //sub processes names are at alphabet begin + end to ensure parentprocess will not be deleted first.
    //we hope alphabetical order (or reverse) is used... :-( )

    ProcessDefinition parentProcess = ProcessBuilder.createProcess("parentProcess", "1.0")
    .addSubProcess("aSubProcess", "aSubProcess")
    .done();

    ProcessDefinition subProcess = ProcessBuilder.createProcess("aSubProcess", "1.0")
    .addHuman(getLogin())
    .addSubProcess("zSubSubProcess", "zSubSubProcess")
    .done();

    ProcessDefinition subSubProcess = ProcessBuilder.createProcess("zSubSubProcess", "1.0")
    .addHuman(getLogin())
    .addHumanTask("t", getLogin())
    .done();

    getManagementAPI().deploy(getBusinessArchive(subSubProcess));
    getManagementAPI().deploy(getBusinessArchive(subProcess));
    getManagementAPI().deploy(getBusinessArchive(parentProcess));

    final ProcessDefinitionUUID processUUID = parentProcess.getUUID();

    getRuntimeAPI().instantiateProcess(processUUID);

    assertEquals(3, getQueryRuntimeAPI().getProcessInstances().size());

    getManagementAPI().deleteAllProcesses();
  }

  public void testSubFlowWithTaskAndParams() throws BonitaException {
    ProcessDefinition process = ProcessBuilder.createProcess("parentProcess", "1.0")
    .addStringData("param1", "test")
    .addStringData("param2", "initial")
    .addStringData("param3", "initial")
    .addBooleanData("param4", false)
    .addBooleanData("param5", false)
    .addSystemTask("a1")
    .addSubProcess("subflow", "subProcess")
    .addSubProcessInParameter("param1", "inParam")
    .addSubProcessInParameter("param2", "inoutParam")
    .addSubProcessInParameter("param4", "bool")
    .addSubProcessInParameter("param4", "bool2")
    .addSubProcessOutParameter("outParam", "param3")
    .addSubProcessOutParameter("inoutParam", "param2")
    .addSubProcessOutParameter("bool", "param4")
    .addSubProcessOutParameter("bool", "param5")
    .addSystemTask("a3")
    .addTransition("a1_subflow", "a1", "subflow")
    .addTransition("subflow_a3", "subflow", "a3")
    .done();

    ProcessDefinition subProcess = ProcessBuilder.createProcess("subProcess", "1.0")
    .addStringData("inParam")
    .addStringData("inoutParam")
    .addStringData("outParam")
    .addBooleanData("bool")
    .addBooleanData("bool2")
    .addBooleanData("bool3")
    .addHuman("admin")
    .addSystemTask("r1")
    .addSystemTask("r2")
    .addHumanTask("r3", "admin")
    .addTransition("r1_r2", "r1", "r2")
    .addTransition("r2_r3", "r2", "r3")
    .done();

    getManagementAPI().deleteAllProcesses();

    subProcess = getManagementAPI().deploy(getBusinessArchive(subProcess));
    process = getManagementAPI().deploy(getBusinessArchive(process));

    ProcessDefinitionUUID subProcessUUID = subProcess.getUUID();    
    ProcessDefinitionUUID processUUID = process.getUUID();

    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);
    checkStopped(instanceUUID, new String[]{"a1"});

    assertEquals("test", getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "param1"));
    assertEquals("initial", getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "param2"));
    assertEquals("initial", getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "param3"));
    assertEquals(false, getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "param4"));
    assertEquals(false, getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "param5"));

    Collection<TaskInstance> taskActivities = getQueryRuntimeAPI().getTaskList(ActivityState.READY);
    assertEquals(1, taskActivities.size());
    TaskInstance taskActivity = taskActivities.iterator().next();

    ProcessInstanceUUID taskInstanceUUID = taskActivity.getProcessInstanceUUID();
    assertNotSame(instanceUUID, taskInstanceUUID);

    assertEquals("test", getQueryRuntimeAPI().getVariable(taskActivity.getUUID(), "inParam"));
    assertEquals("initial", getQueryRuntimeAPI().getVariable(taskActivity.getUUID(), "inoutParam"));
    assertEquals(false, getQueryRuntimeAPI().getVariable(taskActivity.getUUID(), "bool"));
    assertEquals(false, getQueryRuntimeAPI().getVariable(taskActivity.getUUID(), "bool2"));

    assertNull(getQueryRuntimeAPI().getVariable(taskActivity.getUUID(), "outParam"));
    assertNull(getQueryRuntimeAPI().getVariable(taskActivity.getUUID(), "bool3"));

    ActivityInstanceUUID taskUUID = taskActivity.getUUID();
    getRuntimeAPI().startTask(taskUUID, true);
    getRuntimeAPI().setProcessInstanceVariable(taskInstanceUUID, "outParam", "changed");
    getRuntimeAPI().setProcessInstanceVariable(taskInstanceUUID, "inoutParam", "changed");
    getRuntimeAPI().setProcessInstanceVariable(taskInstanceUUID, "inParam", "changed");
    getRuntimeAPI().setProcessInstanceVariable(taskInstanceUUID, "bool", true);
    getRuntimeAPI().finishTask(taskUUID, true);

    checkExecutedOnce(instanceUUID, new String[]{"a1", "subflow", "a3"});

    assertEquals("test", getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "param1"));
    assertEquals("changed", getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "param2"));
    assertEquals("changed", getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "param3"));
    assertEquals(true, getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "param4"));
    assertEquals(true, getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "param5"));

    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
    getManagementAPI().disable(subProcessUUID);
    getManagementAPI().deleteProcess(subProcessUUID);
  }

  //ProEd use a subflow process defined in an external file.
  //This process needs to be deployed separately
  public void testAutoSubFlowProEd() throws BonitaException {
    URL xpdlUrlSubflow = this.getClass().getResource("automaticProcess.xpdl");
    ProcessDefinitionUUID subflowProcessUUID =
      getManagementAPI().deploy(getBusinessArchiveFromXpdl(xpdlUrlSubflow)).getUUID();

    URL xpdlUrl = this.getClass().getResource("subflow_proed.xpdl");
    ProcessDefinition process = getManagementAPI().deploy(getBusinessArchiveFromXpdl(xpdlUrl));

    ProcessDefinitionUUID processUUID = process.getUUID();
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    checkExecutedOnce(instanceUUID, new String[]{"a1", "a2", "a3"});

    getRuntimeAPI().deleteProcessInstance(instanceUUID);

    getManagementAPI().disable(processUUID);
    getManagementAPI().archive(processUUID);
    getManagementAPI().disable(subflowProcessUUID);
    getManagementAPI().archive(subflowProcessUUID);

    getManagementAPI().deleteProcess(processUUID);
    getManagementAPI().deleteProcess(subflowProcessUUID);
  }

  //ProEd use a subflow process defined in an external file.
  //This process needs to be deployed separately
  public void testSubFlowProEdVariables() throws BonitaException {
    URL xpdlUrlSubflow = this.getClass().getResource("subflow_proed_variables.xpdl");

    ProcessDefinitionUUID subflowProcessUUID =
      getManagementAPI().deploy(getBusinessArchiveFromXpdl(xpdlUrlSubflow)).getUUID();
    URL xpdlUrl = this.getClass().getResource("main_variables_proed.xpdl");
    ProcessDefinition process = getManagementAPI().deploy(getBusinessArchiveFromXpdl(xpdlUrl));

    ProcessDefinitionUUID processUUID = process.getUUID();
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    checkStopped(instanceUUID, new String[]{"a1"});

    Collection<TaskInstance> tasks = getQueryRuntimeAPI().getTaskList(ActivityState.READY);
    assertEquals(1, tasks.size());
    TaskInstance taskInstance = tasks.iterator().next();

    assertFalse("sub process instanceUUID must be different from parent instanceUUID", instanceUUID.equals(taskInstance.getProcessInstanceUUID()));

    ProcessInstance processInstance = getQueryRuntimeAPI().getProcessInstance(taskInstance.getProcessInstanceUUID());
    assertEquals(instanceUUID, processInstance.getParentInstanceUUID());
    ActivityInstanceUUID taskUUID = taskInstance.getUUID();

    Map<String, Object> variables =
      getQueryRuntimeAPI().getActivityInstanceVariables(taskInstance.getUUID());
    assertNotNull(variables);
    assertTrue(variables.containsKey("activityVar1"));
    assertFalse(variables.containsKey("processVar1"));
    assertFalse(variables.containsKey("subflow_var"));
    assertFalse(variables.containsKey("parent_process_var"));

    variables = getQueryRuntimeAPI().getProcessInstanceVariables(taskInstance.getProcessInstanceUUID());
    assertFalse(variables.containsKey("activityVar1"));
    assertTrue(variables.containsKey("processVar1"));
    assertFalse(variables.containsKey("subflow_var"));
    assertFalse(variables.containsKey("parent_process_var"));

    getRuntimeAPI().startTask(taskUUID, true);
    getRuntimeAPI().finishTask(taskUUID, true);

    checkExecutedOnce(instanceUUID, new String[]{"a1", "sf"});

    Collection<ActivityInstance> acts = getQueryRuntimeAPI().getActivityInstances(instanceUUID, "sf");
    assertEquals(1, acts.size());
    ActivityInstanceUUID sfUUID = acts.iterator().next().getUUID();
    variables = getQueryRuntimeAPI().getActivityInstanceVariables(sfUUID);

    assertNotNull(variables);
    assertFalse(variables.containsKey("activityVar1"));
    assertTrue(variables.containsKey("subflow_var"));

    getManagementAPI().disable(processUUID);
    getManagementAPI().archive(processUUID);
    getManagementAPI().disable(subflowProcessUUID);
    getManagementAPI().archive(subflowProcessUUID);

    getManagementAPI().deleteProcess(processUUID);
    getManagementAPI().deleteProcess(subflowProcessUUID);
  }

  //http://www.bonitasoft.org/forum/viewtopic.php?id=29
  public void testSubProcessWith2versions() throws Exception {
    ProcessDefinition sub1 = ProcessBuilder.createProcess("sub", "1.0")
    .addHuman(getLogin())
    .addHumanTask("task")
    .done();

    ProcessDefinition parent1 = ProcessBuilder.createProcess("parent", "1.0")
    .addSubProcess("subTask", "sub")
    .done();

    ProcessDefinition sub2 = ProcessBuilder.createProcess("sub", "2.0")
    .addHuman(getLogin())
    .addHumanTask("task")
    .done();

    ProcessDefinition parent2 = ProcessBuilder.createProcess("parent", "2.0")
    .addSubProcess("subTask", "sub")
    .done();

    sub1 = getManagementAPI().deploy(getBusinessArchive(sub1));
    parent1 = getManagementAPI().deploy(getBusinessArchive(parent1));

    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(parent1.getUUID());

    sub2 = getManagementAPI().deploy(getBusinessArchive(sub2));
    parent2 = getManagementAPI().deploy(getBusinessArchive(parent2));

    getRuntimeAPI().deleteProcessInstance(instanceUUID);

    getManagementAPI().deleteProcess(parent1.getUUID());
    getManagementAPI().deleteProcess(parent2.getUUID());
    getManagementAPI().deleteProcess(sub1.getUUID());
    getManagementAPI().deleteProcess(sub2.getUUID());
  }

  public void testSubflowProcessParameters() throws BonitaException {
    ProcessDefinition parentProcess = ProcessBuilder.createProcess("parent", "1.0")
    .addStringData("s", "initial")
    .addStringData("s2", "initial")
    .addSubProcess("sub", "sub")
    .addSubProcessInParameter("s", "s")
    .addSubProcessInParameter("s2", "s2")
    .done();

    ProcessDefinition childProcess = ProcessBuilder.createProcess("sub", "1.0")
    .addStringData("s")
    .addStringData("s2")
    .addHuman(getLogin())
    .addHumanTask("t", getLogin())
    .done();            

    childProcess = getManagementAPI().deploy(getBusinessArchive(childProcess));
    parentProcess = getManagementAPI().deploy(getBusinessArchive(parentProcess));

    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("s2", "changed");

    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(parentProcess.getUUID(), variables);

    ProcessInstance subInstance = getQueryRuntimeAPI().getProcessInstances(childProcess.getUUID()).iterator().next();
    assertEquals("initial", subInstance.getLastKnownVariableValues().get("s"));
    assertEquals("changed", subInstance.getLastKnownVariableValues().get("s2"));
    getRuntimeAPI().deleteProcessInstance(instanceUUID);

    getManagementAPI().deleteProcess(parentProcess.getUUID());
    getManagementAPI().deleteProcess(childProcess.getUUID());
  }

  public void testSubflowProcessParametersAsync() throws BonitaException, InterruptedException {
    ProcessDefinition parentProcess = ProcessBuilder.createProcess("parent", "1.0")
    .addStringData("s", "initial")
    .addSubProcess("sub", "sub")
    .asynchronous()
    .addSubProcessInParameter("s", "s")
    .done();

    ProcessDefinition childProcess = ProcessBuilder.createProcess("sub", "1.0")
    .addStringData("s")
    .addHuman(getLogin())
    .addHumanTask("t", getLogin())
    .done();            

    childProcess = getManagementAPI().deploy(getBusinessArchive(childProcess));
    parentProcess = getManagementAPI().deploy(getBusinessArchive(parentProcess));

    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(parentProcess.getUUID());

    int i = 0;

    do {
      Thread.sleep(100);
      i++;
    } while (getQueryRuntimeAPI().getProcessInstances(childProcess.getUUID()).size() == 0 && i < 10);

    if (i == 10) {
      fail("Unable to find subprocess instance.");
    }
    ProcessInstance subInstance = getQueryRuntimeAPI().getProcessInstances(childProcess.getUUID()).iterator().next();
    assertEquals("initial", subInstance.getLastKnownVariableValues().get("s"));

    getRuntimeAPI().deleteProcessInstance(instanceUUID);

    getManagementAPI().deleteProcess(parentProcess.getUUID());
    getManagementAPI().deleteProcess(childProcess.getUUID());
  }

  public void testSubflowActivityParameters() throws BonitaException {
    ProcessDefinition parentProcess = ProcessBuilder.createProcess("parent", "1.0")
    .addSubProcess("sub", "sub")
    .addStringData("s", "initial")
    .addSubProcessInParameter("s", "s")
    .done();

    ProcessDefinition childProcess = ProcessBuilder.createProcess("sub", "1.0")
    .addStringData("s")
    .addHuman(getLogin())
    .addHumanTask("t", getLogin())
    .done();            

    childProcess = getManagementAPI().deploy(getBusinessArchive(childProcess));
    parentProcess = getManagementAPI().deploy(getBusinessArchive(parentProcess));

    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(parentProcess.getUUID());

    ProcessInstance subInstance = getQueryRuntimeAPI().getProcessInstances(childProcess.getUUID()).iterator().next();
    assertEquals("initial", subInstance.getLastKnownVariableValues().get("s"));
    getRuntimeAPI().deleteProcessInstance(instanceUUID);

    getManagementAPI().deleteProcess(parentProcess.getUUID());
    getManagementAPI().deleteProcess(childProcess.getUUID());
  }

  public void testSubflowActivityParametersAsync() throws BonitaException, InterruptedException {
    ProcessDefinition parentProcess = ProcessBuilder.createProcess("parent", "1.0")
    .addSubProcess("sub", "sub")
    .addStringData("s", "initial")
    .asynchronous()
    .addSubProcessInParameter("s", "s")
    .done();

    ProcessDefinition childProcess = ProcessBuilder.createProcess("sub", "1.0")
    .addStringData("s")
    .addHuman(getLogin())
    .addHumanTask("t", getLogin())
    .done();            

    childProcess = getManagementAPI().deploy(getBusinessArchive(childProcess));
    parentProcess = getManagementAPI().deploy(getBusinessArchive(parentProcess));

    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(parentProcess.getUUID());

    int i = 0;

    do {
      Thread.sleep(100);
      i++;
    } while (getQueryRuntimeAPI().getProcessInstances(childProcess.getUUID()).size() == 0 && i < 10);

    if (i == 10) {
      fail("Unable to find subprocess instance.");
    }
    ProcessInstance subInstance = getQueryRuntimeAPI().getProcessInstances(childProcess.getUUID()).iterator().next();
    assertEquals("initial", subInstance.getLastKnownVariableValues().get("s"));

    getRuntimeAPI().deleteProcessInstance(instanceUUID);

    getManagementAPI().deleteProcess(parentProcess.getUUID());
    getManagementAPI().deleteProcess(childProcess.getUUID());
  }

  public void testSubflowParametersMulti() throws BonitaException, InterruptedException {
    ProcessDefinition parentProcess = ProcessBuilder.createProcess("parent", "1.0")
    .addSubProcess("sub", "sub")
    .addStringData("s")
    .addSubProcessInParameter("s", "s")
    .addMultiInstanciation("s", SimpleMulti.class.getName())
    .done();

    ProcessDefinition childProcess = ProcessBuilder.createProcess("sub", "1.0")
    .addStringData("s")
    .addHuman(getLogin())
    .addHumanTask("t", getLogin())
    .done();            

    childProcess = getManagementAPI().deploy(getBusinessArchive(childProcess));
    parentProcess = getManagementAPI().deploy(getBusinessArchive(parentProcess, null, SimpleMulti.class));

    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(parentProcess.getUUID());

    Iterator<ProcessInstance> subInstances = getQueryRuntimeAPI().getProcessInstances(childProcess.getUUID()).iterator();
    ProcessInstance subInstance1 = subInstances.next();
    ProcessInstance subInstance2 = subInstances.next();
    Set<String> expected = new HashSet<String>();
    expected.add("a");
    expected.add("b");

    Set<String> actual = new HashSet<String>();
    actual.add((String) subInstance1.getLastKnownVariableValues().get("s"));
    actual.add((String) subInstance2.getLastKnownVariableValues().get("s"));

    assertEquals(expected, actual);

    getRuntimeAPI().deleteProcessInstance(instanceUUID);

    getManagementAPI().deleteProcess(parentProcess.getUUID());
    getManagementAPI().deleteProcess(childProcess.getUUID());
  }

  public void testSubflowParametersAsyncMulti() throws BonitaException, InterruptedException {
    ProcessDefinition parentProcess = ProcessBuilder.createProcess("parent", "1.0")
    .addSubProcess("sub", "sub")
    .addStringData("s")
    .asynchronous()
    .addSubProcessInParameter("s", "s")
    .addMultiInstanciation("s", SimpleMulti.class.getName())
    .done();

    ProcessDefinition childProcess = ProcessBuilder.createProcess("sub", "1.0")
    .addStringData("s")
    .addHuman(getLogin())
    .addHumanTask("t", getLogin())
    .done();            

    childProcess = getManagementAPI().deploy(getBusinessArchive(childProcess));
    parentProcess = getManagementAPI().deploy(getBusinessArchive(parentProcess, null, SimpleMulti.class));

    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(parentProcess.getUUID());

    int i = 0;

    do {
      Thread.sleep(100);
      i++;
    } while (getQueryRuntimeAPI().getProcessInstances(childProcess.getUUID()).size() != 2 && i < 10);

    if (i == 10) {
      fail("Unable to find subprocess instance.");
    }
    Iterator<ProcessInstance> subInstances = getQueryRuntimeAPI().getProcessInstances(childProcess.getUUID()).iterator();
    ProcessInstance subInstance1 = subInstances.next();
    ProcessInstance subInstance2 = subInstances.next();
    Set<String> expected = new HashSet<String>();
    expected.add("a");
    expected.add("b");

    Set<String> actual = new HashSet<String>();
    actual.add((String) subInstance1.getLastKnownVariableValues().get("s"));
    actual.add((String) subInstance2.getLastKnownVariableValues().get("s"));

    assertEquals(expected, actual);

    getRuntimeAPI().deleteProcessInstance(instanceUUID);

    getManagementAPI().deleteProcess(parentProcess.getUUID());
    getManagementAPI().deleteProcess(childProcess.getUUID());
  }

  public static class  SimpleMulti implements MultiInstantiator {
    public MultiInstantiatorDescriptor execute(QueryAPIAccessor arg0, ProcessInstanceUUID arg1, String arg2, String arg3) throws Exception {
      List<Object> objects = new ArrayList<Object>();
      objects.add("a");
      objects.add("b");
      return new MultiInstantiatorDescriptor(objects.size(), objects);
    }
  }

  //http://www.bonitasoft.org/forum/viewtopic.php?id=24
  public void testInnerSubflowWithAutomaticActivities() throws BonitaException {
    ProcessDefinition parentProcess = ProcessBuilder.createProcess("Prueba", "1.0")
    .addStringData("ListaNum", "0")
    .addSubProcess("MultiInst", "Prueba_sub")
    .addSubProcessInParameter("Num", "Num")
    .asynchronous()
    .addStringData("Num")
    .addMultiInstanciation("Num", Multi.class.getName())
    .addSystemTask("P2")
    .addConnector(Event.automaticOnEnter, P2Hook.class.getName(), true)
    .addSystemTask("P1")
    .addConnector(Event.automaticOnEnter, P1Hook.class.getName(), true)
    .addTransition("P1_MultiInst", "P1", "MultiInst")
    .addTransition("MultiInst_P2", "MultiInst", "P2")
    .done();

    ProcessDefinition childProcess = ProcessBuilder.createProcess("Prueba_sub", "1.0")
    .addStringData("Num")
    .addStringData("Numero", "0")
    .addBooleanData("EsPar", true)
    .addSystemTask("PS")
    .addConnector(Event.automaticOnEnter, PSHook.class.getName(), true)
    .addSystemTask("Par")
    .addConnector(Event.automaticOnEnter, ParHook.class.getName(), false)
    .addSystemTask("Impar")
    .addConnector(Event.automaticOnEnter, ImparHook.class.getName(), false)
    .addTransition("PS_Par", "PS", "Par")
    .addCondition("EsPar.compareTo(Boolean.valueOf('true')) == 0")
    .addTransition("PS_Impar", "PS", "Impar")
    .addCondition("EsPar.compareTo(Boolean.valueOf('false')) == 0")
    .done();            

    childProcess = getManagementAPI().deploy(getBusinessArchive(childProcess, null, PSHook.class, ParHook.class, ImparHook.class));
    parentProcess = getManagementAPI().deploy(getBusinessArchive(parentProcess, null, P1Hook.class, P2Hook.class, Multi.class));

    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(parentProcess.getUUID());
    this.waitForInstanceEnd(10000, 500, instanceUUID);
    assertEquals(InstanceState.FINISHED, getQueryRuntimeAPI().getProcessInstance(instanceUUID).getInstanceState());

    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().deleteProcess(parentProcess.getUUID());
    getManagementAPI().deleteProcess(childProcess.getUUID());
  }

  public static class  Multi implements MultiInstantiator {
    public MultiInstantiatorDescriptor execute(QueryAPIAccessor arg0, ProcessInstanceUUID arg1, String arg2, String arg3) throws Exception {
      String lista=(String)arg0.getQueryRuntimeAPI().getProcessInstanceVariable(arg1, "ListaNum");
      List<Object> l=new Vector<Object>();
      String[]array=lista.split(",");
      for(int i=0;i<array.length;i++){
        l.add(array[i]);
      }
      System.out.println("MultiInstancia, l= "+l);
      return new MultiInstantiatorDescriptor(l.size(), l);
    }
  }

  public static class P1Hook implements TxHook {
    public void execute(APIAccessor arg0, ActivityInstance arg1)
    throws Exception {
      System.out.println("P1Hook: ListaNum = 1,2,3,4,5,6,7,8,9,10 ");
      arg0.getRuntimeAPI().setProcessInstanceVariable(arg1.getProcessInstanceUUID(), "ListaNum", "1,2,3,4,5,6,7,8,9,10");
    }
  }

  public static class P2Hook implements TxHook {
    public void execute(APIAccessor arg0, ActivityInstance arg1) throws Exception {
      String lista=(String)arg0.getQueryRuntimeAPI().getProcessInstanceVariable(arg1.getProcessInstanceUUID(), "ListaNum");
      System.out.println("P2Hook: ListaNum = "+lista);
      System.out.println("End");
    }
  }

  public static class ImparHook implements Hook {
    public void execute(QueryAPIAccessor arg0, ActivityInstance arg1) throws Exception {
      System.out.println("Y es impar");
    }
  }

  public static class ParHook implements Hook {
    public void execute(QueryAPIAccessor arg0, ActivityInstance arg1) throws Exception {
      System.out.println("Y es par");
    }
  }

  public static class PSHook implements TxHook {
    public void execute(APIAccessor accessor, ActivityInstance activityInstance) throws Exception {
      System.out.println("PSHOOK");
      String num = (String)accessor.getQueryRuntimeAPI().getProcessInstanceVariable(activityInstance.getProcessInstanceUUID(), "Num");
      System.out.println("\n\n*****\nNum=" + num);
      Boolean par= Boolean.valueOf(Integer.parseInt(num)%2==0);
      boolean trabaja=false;
      int suma=0;
      if(trabaja){
        Random m=new Random();
        int iters=m.nextInt(1000);
        boolean uno=m.nextBoolean();
        int iters2=m.nextInt(10000);
        boolean dos=m.nextBoolean();
        int iters3=m.nextInt(10000);            
        for(int i=0;i<iters;i++){
          suma++;
          if(uno){
            for(int j=0;j<iters2;j++){
              suma++;
              if(dos){
                for(int z=0;z<iters3;z++){
                  suma++;
                }                        
              }                    
            }
          }
        }
      }
      RuntimeAPI rAPI = accessor.getRuntimeAPI();
      QueryRuntimeAPI qrAPI = accessor.getQueryRuntimeAPI();

      ProcessInstanceUUID parentUUID = qrAPI.getProcessInstance(activityInstance.getProcessInstanceUUID()).getParentInstanceUUID();
      rAPI.setProcessInstanceVariable(parentUUID, "ListaNum", "Lista modificada");

      System.out.println("Mi Num es: "+num+" Par:"+par+" Suma: "+suma  );
      accessor.getRuntimeAPI().setProcessInstanceVariable(activityInstance.getProcessInstanceUUID(), "EsPar", par);
    }
  }

  public void testDynamicSubflow() throws BonitaException {
    ProcessDefinition parentProcess =
      ProcessBuilder.createProcess("parent", "1.0")
      .addStringData("subName", "child")
      .addSubProcess("sub1", "${subName}")
      .addSubProcess("sub2", "${if (true) {return subName}}")
      .addSystemTask("end")
      .addJoinType(JoinType.AND)
      .addTransition("sub1", "end")
      .addTransition("sub2", "end")
      .done();

    ProcessDefinition childProcess =
      ProcessBuilder.createProcess("child", "1.0")
      .addHuman(getLogin())
      .addHumanTask("t", getLogin())
      .done();            

    //check we can deploy the parent before as the sub process is not resolved at deployment time
    parentProcess = getManagementAPI().deploy(getBusinessArchive(parentProcess));
    childProcess = getManagementAPI().deploy(getBusinessArchive(childProcess));

    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(parentProcess.getUUID());

    //we should have 2 tasks in todolist
    Collection<TaskInstance> tasks = getQueryRuntimeAPI().getTaskList(ActivityState.READY);
    assertEquals(2, tasks.size());
    Iterator<TaskInstance> it = tasks.iterator(); 
    TaskInstance t1 = it.next();
    TaskInstance t2 = it.next();

    assertFalse(t1.getProcessInstanceUUID().equals(t2.getProcessInstanceUUID()));

    getRuntimeAPI().executeTask(t1.getUUID(), true);
    getRuntimeAPI().executeTask(t2.getUUID(), true);

    assertEquals(InstanceState.FINISHED, getQueryRuntimeAPI().getProcessInstance(instanceUUID).getInstanceState());

    //check we can disable parent process before child process
    getManagementAPI().disable(parentProcess.getUUID());
    getManagementAPI().disable(childProcess.getUUID());

    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().deleteProcess(parentProcess.getUUID());
    getManagementAPI().deleteProcess(childProcess.getUUID());
  }

  public void testAddASubflowConnectorOnEnter() throws Exception {
    ProcessDefinition subflow =
      ProcessBuilder.createProcess("sub", "1.0")
      .addHuman("john")
      .addHumanTask("first_sub", "john")
      .done();

    ProcessDefinition definition =
      ProcessBuilder.createProcess("main", "1.0")
      .addStringData("value")
      .addHuman(getLogin())
      .addSubProcess("mySub", "sub")
      .addConnector(Event.automaticOnEnter, SetVarConnector.class.getName(), true)
      .addInputParameter("variableName", "value")
      .addInputParameter("value", "It works!")
      .addHumanTask("second", getLogin())
      .addTransition("mySub", "second")
      .done();

    subflow = getManagementAPI().deploy(getBusinessArchive(subflow));
    definition = getManagementAPI().deploy(getBusinessArchive(definition, getResourcesFromConnector(SetVarConnector.class), SetVarConnector.class));

    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(definition.getUUID());
    String actual = (String) getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "value");
    assertEquals("It works!", actual);

    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().deleteProcess(definition.getUUID());
    getManagementAPI().deleteProcess(subflow.getUUID());
  }

  public void testAddASubflowConnectorOnExit() throws Exception {
    ProcessDefinition subflow =
      ProcessBuilder.createProcess("sub", "1.0")
      .addHuman("john")
      .addHumanTask("first_sub", "john")
      .done();

    ProcessDefinition definition =
      ProcessBuilder.createProcess("main", "1.0")
      .addStringData("value")
      .addHuman(getLogin())
      .addSubProcess("mySub", "sub")
      .addConnector(Event.automaticOnExit, SetVarConnector.class.getName(), true)
      .addInputParameter("variableName", "value")
      .addInputParameter("value", "It works!")
      .addHumanTask("second", getLogin())
      .addTransition("mySub", "second")
      .done();

    subflow = getManagementAPI().deploy(getBusinessArchive(subflow));
    definition = getManagementAPI().deploy(getBusinessArchive(definition, getResourcesFromConnector(SetVarConnector.class), SetVarConnector.class));

    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(definition.getUUID());
    String actual = (String) getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "value");
    assertNull(actual);

    loginAs("john", "bpm");
    Collection<TaskInstance> tasks = getQueryRuntimeAPI().getTaskList(ActivityState.READY);
    executeTask(tasks.iterator().next().getProcessInstanceUUID(), "first_sub");

    actual = (String) getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "value");
    assertEquals("It works!", actual);

    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().deleteProcess(definition.getUUID());
    getManagementAPI().deleteProcess(subflow.getUUID());
  }


  public void testEvaluateBooleanInSubSubProcessAndUsedInMainProcess() throws Exception {
    ProcessDefinition subSubDefinition =
      ProcessBuilder.createProcess("sub_subProcess", "1.0")
      .addHuman(getLogin())
      .addBooleanData("solved", false)
      .addHumanTask("choice", getLogin())
      .done();

    ProcessDefinition subDefinition =
      ProcessBuilder.createProcess("sub_process", "1.0")
      .addBooleanData("solved", false)
      .addSubProcess("sub", "sub_subProcess")
      .addSubProcessOutParameter("solved", "solved")
      .done();

    ProcessDefinition definition =
      ProcessBuilder.createProcess("process", "1.0")
      .addHuman(getLogin())
      .addSubProcess("condition", "sub_process")
      .addBooleanData("solved")
      .addSubProcessOutParameter("solved", "solved")
      .addHumanTask("ok", getLogin())
      .addHumanTask("no", getLogin())
      .addTransition("condition", "ok")
      .addCondition("solved")
      .addTransition("condition", "no")
      .addCondition("!solved")
      .done();

    subSubDefinition = getManagementAPI().deploy(getBusinessArchive(subSubDefinition));
    subDefinition = getManagementAPI().deploy(getBusinessArchive(subDefinition));
    definition = getManagementAPI().deploy(getBusinessArchive(definition));

    getRuntimeAPI().instantiateProcess(definition.getUUID());
    Collection<TaskInstance> tasks = getQueryRuntimeAPI().getTaskList(ActivityState.READY);
    TaskInstance task = tasks.iterator().next();
    getRuntimeAPI().setProcessInstanceVariable(task.getProcessInstanceUUID(), "solved", true);
    executeTask(task.getProcessInstanceUUID(), "choice");

    tasks = getQueryRuntimeAPI().getTaskList(ActivityState.READY);
    task = tasks.iterator().next();
    assertEquals("ok", task.getActivityName());

    executeTask(task.getProcessInstanceUUID(), "ok");

    getManagementAPI().deleteProcess(definition.getUUID());
    getManagementAPI().deleteProcess(subDefinition.getUUID());
    getManagementAPI().deleteProcess(subSubDefinition.getUUID());
  }

  public void testSubProcessKnowsWhichActivityStartsIts() throws Exception {
    ProcessDefinition process = ProcessBuilder.createProcess("parentProcess", "1.0")
    .addHuman(getLogin())
    .addSubProcess("aSubProcess", "childProcess")
    .done();

    ProcessDefinition subProcess = ProcessBuilder.createProcess("childProcess", "1.0")
    .addHuman(getLogin())
    .addHumanTask("t1", getLogin())
    .done();

    subProcess = getManagementAPI().deploy(getBusinessArchive(subProcess));
    process = getManagementAPI().deploy(getBusinessArchive(process));
    final ProcessDefinitionUUID processUUID = process.getUUID();
    ProcessInstanceUUID parentInstanceUUID = getRuntimeAPI().instantiateProcess(processUUID);
    LightProcessInstance parentInstance = getQueryRuntimeAPI().getLightProcessInstance(parentInstanceUUID);
    assertNull(parentInstance.getParentActivityUUID());

    Set<LightActivityInstance> parentActivities = getQueryRuntimeAPI().getLightActivityInstances(parentInstanceUUID);
    assertEquals(1, parentActivities.size());
    LightActivityInstance activity = parentActivities.iterator().next();
    ActivityInstanceUUID parent = activity.getUUID();
    Set<LightProcessInstance> childProcesses = getQueryRuntimeAPI().getLightProcessInstances(subProcess.getUUID());
    assertEquals(1, childProcesses.size());
    LightProcessInstance childInstance = childProcesses.iterator().next();
    ActivityInstanceUUID child = childInstance.getParentActivityUUID();
    assertEquals(parent, child);

    getManagementAPI().deleteProcess(processUUID);
    getManagementAPI().deleteProcess(subProcess.getUUID());
  }

  public void testSubProcessesKnowWhichActivityStartsThem() throws Exception {
    ProcessDefinition process = ProcessBuilder.createProcess("parentProcess", "1.0")
    .addSubProcess("aSubProcess", "childProcess")
    .done();

    ProcessDefinition subProcess = ProcessBuilder.createProcess("childProcess", "1.0")
    .addSubProcess("aSubSubProcess", "subChildProcess")
    .done();

    ProcessDefinition subSubProcess = ProcessBuilder.createProcess("subChildProcess", "1.0")
    .addHuman(getLogin())
    .addHumanTask("t1", getLogin())
    .done();

    subSubProcess = getManagementAPI().deploy(getBusinessArchive(subSubProcess));
    subProcess = getManagementAPI().deploy(getBusinessArchive(subProcess));
    process = getManagementAPI().deploy(getBusinessArchive(process));
    final ProcessDefinitionUUID processUUID = process.getUUID();
    ProcessInstanceUUID parentInstanceUUID = getRuntimeAPI().instantiateProcess(processUUID);
    LightProcessInstance parentInstance = getQueryRuntimeAPI().getLightProcessInstance(parentInstanceUUID);
    assertNull(parentInstance.getParentActivityUUID());

    Set<LightActivityInstance> parentActivities = getQueryRuntimeAPI().getLightActivityInstances(parentInstanceUUID);
    assertEquals(1, parentActivities.size());
    LightActivityInstance activity = parentActivities.iterator().next();
    ActivityInstanceUUID parent = activity.getUUID();

    Set<LightProcessInstance> childProcesses = getQueryRuntimeAPI().getLightProcessInstances(subProcess.getUUID());
    assertEquals(1, childProcesses.size());
    LightProcessInstance childInstance = childProcesses.iterator().next();
    ActivityInstanceUUID child = childInstance.getParentActivityUUID();
    parentActivities = getQueryRuntimeAPI().getLightActivityInstances(childInstance.getUUID());
    assertEquals(1, parentActivities.size());
    activity = parentActivities.iterator().next();
    ActivityInstanceUUID parentChild = activity.getUUID();

    childProcesses = getQueryRuntimeAPI().getLightProcessInstances(subSubProcess.getUUID());
    assertEquals(1, childProcesses.size());
    childInstance = childProcesses.iterator().next();
    ActivityInstanceUUID childChild = childInstance.getParentActivityUUID();

    assertEquals(parent, child);
    assertEquals(parentChild, childChild);
    assertNotSame(parent, parentChild);

    getManagementAPI().deleteProcess(processUUID);
    getManagementAPI().deleteProcess(subProcess.getUUID());
    getManagementAPI().deleteProcess(subSubProcess.getUUID());
  }

  public void testTheInitiatorOfASubProcessIsTheInitiatorOfTheMainProcess() throws Exception {
    ProcessDefinition subProcess = ProcessBuilder.createProcess("subPro", "1.0")
      .addGroup("initiator")
        .addGroupResolver(ProcessInitiatorRoleResolver.class.getName())
      .addHumanTask("step", "initiator")
    .done();

    ProcessDefinition mainProcess = ProcessBuilder.createProcess("main", "1.0")
      .addGroup("users")
        .addGroupResolver(UserListRoleResolver.class.getName())
          .addInputParameter("users", "john, jack")
      .addHumanTask("before", "users")
      .addSubProcess("sub", "subPro", "1.0")
      .addTransition("before", "sub")
      .done();

    getManagementAPI().deploy(getBusinessArchive(subProcess, null, ProcessInitiatorRoleResolver.class));
    getManagementAPI().deploy(getBusinessArchive(mainProcess, null, UserListRoleResolver.class));

    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(mainProcess.getUUID());
    loginAs("john", "bpm");
    executeTask(instanceUUID, "before");

    Set<ProcessInstance> subProcessInstances = getQueryRuntimeAPI().getProcessInstances(subProcess.getUUID());
    assertEquals(1, subProcessInstances.size());
    ProcessInstance subInstance = subProcessInstances.iterator().next();
    assertEquals(getLogin(), subInstance.getStartedBy());

    getManagementAPI().deleteProcess(mainProcess.getUUID());
    getManagementAPI().deleteProcess(subProcess.getUUID());
  }

  public void testCancelMainProcessCancelAsWellSubProcessActivities() throws Exception {
    ProcessDefinition subProcess = ProcessBuilder.createProcess("subPro", "1.0")
    .addHuman(getLogin())
    .addHumanTask("step3", getLogin())
    .done();

    ProcessDefinition mainProcess = ProcessBuilder.createProcess("main", "1.0")
    .addHuman(getLogin())
    .addHumanTask("step1", getLogin())
    .addSubProcess("step2", "subPro", "1.0")
    .addHumanTask("step4", getLogin())
    .addTransition("step1", "step2")
    .addTransition("step2", "step4")
    .done();

    getManagementAPI().deploy(getBusinessArchive(subProcess));
    getManagementAPI().deploy(getBusinessArchive(mainProcess));

    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(mainProcess.getUUID());
    executeTask(instanceUUID, "step1");

    getRuntimeAPI().cancelProcessInstance(instanceUUID);

    Set<ProcessInstanceUUID> instanceUUIDs = new HashSet<ProcessInstanceUUID>();
    instanceUUIDs.add(instanceUUID);

    GetProcessInstancesActivitiesCommand command = new GetProcessInstancesActivitiesCommand(instanceUUIDs, true);
    Map<ProcessInstanceUUID, List<LightActivityInstance>> processInstancesActivities = getCommandAPI().execute(command);
    assertEquals(1, processInstancesActivities.size());

    List<LightActivityInstance> lightActivityInstances = processInstancesActivities.get(instanceUUID);
    assertEquals(1, lightActivityInstances.size());

    LightActivityInstance lightActivityInstance = lightActivityInstances.get(0);
    assertEquals("step2", lightActivityInstance.getActivityName());
    assertEquals(ActivityState.CANCELLED, lightActivityInstance.getState());

    getManagementAPI().deleteProcess(mainProcess.getUUID());
    getManagementAPI().deleteProcess(subProcess.getUUID());
  }

}
