package org.ow2.bonita.util;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import junit.framework.Assert;

import org.bonitasoft.connectors.bonita.SetVarConnector;
import org.bonitasoft.connectors.bonita.resolvers.ProcessInitiatorRoleResolver;
import org.bonitasoft.connectors.bonita.resolvers.UserListRoleResolver;
import org.ow2.bonita.APITestCase;
import org.ow2.bonita.building.XmlDefExporter;
import org.ow2.bonita.definition.InstanceInitiator;
import org.ow2.bonita.facade.def.element.BoundaryEvent;
import org.ow2.bonita.facade.def.element.BusinessArchive;
import org.ow2.bonita.facade.def.element.HookDefinition.Event;
import org.ow2.bonita.facade.def.element.MultiInstantiationDefinition;
import org.ow2.bonita.facade.def.majorElement.ActivityDefinition;
import org.ow2.bonita.facade.def.majorElement.ActivityDefinition.JoinType;
import org.ow2.bonita.facade.def.majorElement.DataFieldDefinition;
import org.ow2.bonita.facade.def.majorElement.ParticipantDefinition;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.def.majorElement.TransitionDefinition;
import org.ow2.bonita.facade.exception.DeploymentException;
import org.ow2.bonita.facade.exception.ProcessNotFoundException;
import org.ow2.bonita.facade.exception.UndeletableInstanceException;
import org.ow2.bonita.facade.exception.UndeletableProcessException;
import org.ow2.bonita.facade.runtime.ActivityState;
import org.ow2.bonita.facade.runtime.InstanceState;
import org.ow2.bonita.facade.runtime.ProcessInstance;
import org.ow2.bonita.facade.runtime.TaskInstance;
import org.ow2.bonita.facade.uuid.ActivityInstanceUUID;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.hook.misc.IncrementHook;

public class ProcessBuilderTest extends APITestCase {

  private final String processName = "WebSale";
  private final String processDescription = "The websale example";

  public void testCreateSimpleProcess() {
    final ProcessBuilder builder = ProcessBuilder.createProcess(processName, null);
    final ProcessDefinition process = builder.done();
    Assert.assertEquals(processName, process.getName());
    Assert.assertNull(process.getDescription());
  }

  public void testProcessWithOneActivity() {
    final ProcessBuilder builder = ProcessBuilder.createProcess(processName, null).addSystemTask("t");
    final ProcessDefinition process = builder.done();
    Assert.assertEquals(processName, process.getName());
    Assert.assertNull(process.getDescription());
  }

  public void testCreateProcessWithProcessAttributes() {
    final ProcessDefinition process = ProcessBuilder.createProcess(processName, "2.0")
        .addDescription(processDescription).addStringData("siteName").done();

    Assert.assertEquals(processName, process.getName());
    Assert.assertEquals("2.0", process.getVersion());
    Assert.assertEquals(processDescription, process.getDescription());
  }

  public void testProcessDescription() throws IOException, ClassNotFoundException, BonitaException {
    final ProcessDefinition processClient = ProcessBuilder.createProcess("myProcess", "1.0")
        .addDescription("myDescription").addSystemTask("r1").done();

    assertEquals("Description is not set", "myDescription", processClient.getDescription());

    final ProcessDefinition process = getManagementAPI().deploy(getBusinessArchive(processClient));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    final ProcessDefinition processDef = getQueryDefinitionAPI().getProcess(processUUID);
    assertNotNull(processDef);

    assertEquals("Description is not set", "myDescription", processDef.getDescription());

    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testDynamicDescription() throws Exception {
    final String dynamic = "${true}";
    ProcessDefinition process = ProcessBuilder.createProcess("myProcess", "1.0").addSystemTask("a")
        .addDynamicDescription(dynamic).done();

    assertEquals(dynamic, process.getActivity("a").getDynamicDescription());

    process = getManagementAPI().deploy(getBusinessArchive(process));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    assertEquals(dynamic, getQueryDefinitionAPI().getProcess(processUUID).getActivity("a").getDynamicDescription());
    assertEquals(dynamic, process.getActivity("a").getDynamicDescription());

    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testDynamicLabel() throws Exception {
    final String dynamic = "${true}";
    ProcessDefinition process = ProcessBuilder.createProcess("myProcess", "1.0").addSystemTask("a")
        .addDynamicLabel(dynamic).done();

    assertEquals(dynamic, process.getActivity("a").getDynamicLabel());

    process = getManagementAPI().deploy(getBusinessArchive(process));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    assertEquals(dynamic, process.getActivity("a").getDynamicLabel());

    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testXpdl() throws ClassNotFoundException, IOException, DeploymentException, ProcessNotFoundException,
      UndeletableProcessException, UndeletableInstanceException {
    final URL xpdlUrl = this.getClass().getResource("manyHooks.xpdl");
    final ProcessDefinition process = getManagementAPI().deploy(
        getBusinessArchiveFromXpdl(xpdlUrl, IncrementHook.class));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    getManagementAPI().deleteProcess(processUUID);
  }

  public void testAutoSubProcess() throws IOException, ClassNotFoundException, BonitaException {
    final ProcessDefinition subProcessClient = ProcessBuilder.createProcess("subflow", "1.0").addSystemTask("subTask")
        .done();

    final ProcessDefinition processClient = ProcessBuilder.createProcess("main", "1.0").addSubProcess("sub", "subflow")
        .done();

    final ProcessDefinition subProcess = getManagementAPI().deploy(getBusinessArchive(subProcessClient));
    final ProcessDefinitionUUID subProcessUUID = subProcess.getUUID();

    final ProcessDefinition process = getManagementAPI().deploy(getBusinessArchive(processClient));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    final ProcessInstance parentInstance = getQueryRuntimeAPI().getProcessInstance(instanceUUID);
    assertEquals(InstanceState.FINISHED, parentInstance.getInstanceState());
    final ProcessInstanceUUID childUUID = parentInstance.getChildrenInstanceUUID().iterator().next();
    final ProcessInstance childInstance = getQueryRuntimeAPI().getProcessInstance(childUUID);
    assertEquals(InstanceState.FINISHED, childInstance.getInstanceState());

    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
    getManagementAPI().disable(subProcessUUID);
    getManagementAPI().deleteProcess(subProcessUUID);
  }

  public void testSubProcess() throws IOException, ClassNotFoundException, BonitaException {
    final ProcessDefinition subProcessClient = ProcessBuilder.createProcess("subflow", "1.0").addHuman("admin")
        .addSystemTask("r1").addSystemTask("r2").addHumanTask("r3", "admin").addTransition("r1_r2", "r1", "r2")
        .addTransition("r2", "r3").done();

    final ProcessDefinition processClient = ProcessBuilder.createProcess("main", "1.0").addSystemTask("a1")
        .addSubProcess("a2", "subflow").addSystemTask("a3").addTransition("a1_a2", "a1", "a2")
        .addTransition("a2", "a3").done();

    final ProcessDefinition subProcess = getManagementAPI().deploy(getBusinessArchive(subProcessClient));
    final ProcessDefinitionUUID subProcessUUID = subProcess.getUUID();

    final ProcessDefinition process = getManagementAPI().deploy(getBusinessArchive(processClient));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);
    checkStopped(instanceUUID, new String[] { "a1" });

    final Collection<TaskInstance> taskActivities = getQueryRuntimeAPI().getTaskList(ActivityState.READY);
    assertEquals(1, taskActivities.size());
    final TaskInstance taskActivity = taskActivities.iterator().next();
    final ProcessInstance processInstance = getQueryRuntimeAPI().getProcessInstance(
        taskActivity.getProcessInstanceUUID());
    assertEquals(instanceUUID, processInstance.getParentInstanceUUID());
    final ActivityInstanceUUID taskUUID = taskActivity.getUUID();
    getRuntimeAPI().startTask(taskUUID, true);
    getRuntimeAPI().finishTask(taskUUID, true);

    checkExecutedOnce(instanceUUID, new String[] { "a1", "a2", "a3" });

    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
    getManagementAPI().disable(subProcessUUID);
    getManagementAPI().deleteProcess(subProcessUUID);
  }

  public void testCreateProcessWithactivityAndDataSettingAtTheEnd() {
    final ProcessDefinition def = ProcessBuilder.createProcess("activities", "10.1").addHumanTask("first", "john")
        .addStringData("data1").addHuman("john").addBooleanData("data2").done();

    final Set<ActivityDefinition> activities = def.getActivities();
    Assert.assertEquals(1, activities.size());
    final ActivityDefinition activity = activities.iterator().next();
    final Set<DataFieldDefinition> dataFields = activity.getDataFields();
    Assert.assertEquals(1, dataFields.size());
    Assert.assertEquals(1, def.getDataFields().size());
  }

  public void testDescription() {
    final ProcessDefinition def = ProcessBuilder.createProcess("p", "1").addDescription("pDescription")
        .addStringData("d").addDescription("dDescription").addHuman("h").addDescription("hDescription")
        .addHumanTask("t1", "null").addDescription("hDescription").addSystemTask("t2").addDescription("sysDescription")
        .addSubProcess("t3", "null").addDescription("subDescription").addDecisionNode("t4")
        .addDescription("decisionDescription").addTransition("tr", "t1", "t2").addDescription("trDescription").done();

    assertEquals("pDescription", def.getDescription());
    assertEquals("hDescription", def.getParticipants().iterator().next().getDescription());
    assertEquals("trDescription", def.getActivity("t1").getOutgoingTransition("tr").getDescription());
    assertEquals("dDescription", def.getDataFields().iterator().next().getDescription());

    assertEquals("hDescription", def.getActivity("t1").getDescription());
    assertEquals("sysDescription", def.getActivity("t2").getDescription());
    assertEquals("subDescription", def.getActivity("t3").getDescription());
    assertEquals("decisionDescription", def.getActivity("t4").getDescription());

  }

  public void testLabel() {
    final ProcessDefinition def = ProcessBuilder.createProcess("p", "1").addLabel("pLabel").addStringData("d")
        .addLabel("dLabel").addHuman("h").addLabel("hLabel").addHumanTask("t1", "null").addLabel("hLabel")
        .addSystemTask("t2").addLabel("sysLabel").addSubProcess("t3", "null").addLabel("subLabel")
        .addDecisionNode("t4").addLabel("decisionLabel").addTransition("tr", "t1", "t2").addLabel("trLabel").done();

    assertEquals("pLabel", def.getLabel());
    assertEquals("hLabel", def.getParticipants().iterator().next().getLabel());
    assertEquals("trLabel", def.getActivity("t1").getOutgoingTransition("tr").getLabel());
    assertEquals("dLabel", def.getDataFields().iterator().next().getLabel());

    assertEquals("hLabel", def.getActivity("t1").getLabel());
    assertEquals("sysLabel", def.getActivity("t2").getLabel());
    assertEquals("subLabel", def.getActivity("t3").getLabel());
    assertEquals("decisionLabel", def.getActivity("t4").getLabel());

  }

  public void testProcessNullName() {
    try {
      ProcessBuilder.createProcess(null, "1.0").done();
      fail("The processName is null");
    } catch (final Exception e) {
    }
  }

  public void testProcessEmptyName() {
    try {
      ProcessBuilder.createProcess("", "1.0").done();
      fail("The processName is empty");
    } catch (final Exception e) {
    }
  }

  public void testProcessWhiteSpacesName() {
    try {
      ProcessBuilder.createProcess("    ", "1.0").done();
      fail("The processName is empty");
    } catch (final Exception e) {
    }
  }

  public void testProcessSpaceName() {
    try {
      ProcessBuilder.createProcess("space name", "1.0").done();
      fail("The processName contains a space");
    } catch (final Exception e) {
    }
  }

  public void testManyInstancesConnector() throws BonitaException, IOException {
    ProcessDefinition process = ProcessBuilder.createProcess("p", "1").addIntegerData("data").addSystemTask("t")
        .addConnector(Event.automaticOnEnter, NumberConnector.class.getName(), true)
        .addInputParameter("setNumber", "${data}").done();

    process = getManagementAPI().deploy(
        getBusinessArchive(process, getResourcesFromConnector(NumberConnector.class), NumberConnector.class));

    final ProcessDefinitionUUID processUUID = process.getUUID();
    getRuntimeAPI().addProcessMetaData(processUUID, "total", "0");
    assertEquals("0", getQueryDefinitionAPI().getProcessMetaData(processUUID, "total"));

    final Map<String, Object[]> parameters = getQueryDefinitionAPI().getProcess(processUUID).getActivity("t")
        .getConnectors().iterator().next().getParameters();
    assertEquals("${data}", parameters.get("setNumber")[0]);

    final Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("data", 4);
    getRuntimeAPI().instantiateProcess(processUUID, variables, null);

    assertEquals("${data}", getQueryDefinitionAPI().getProcess(processUUID).getActivity("t").getConnectors().iterator()
        .next().getParameters().get("setNumber")[0]);
    assertEquals("4", getQueryDefinitionAPI().getProcessMetaData(processUUID, "total"));

    variables.put("data", 20);
    getRuntimeAPI().instantiateProcess(processUUID, variables, null);

    assertEquals("${data}", getQueryDefinitionAPI().getProcess(processUUID).getActivity("t").getConnectors().iterator()
        .next().getParameters().get("setNumber")[0]);

    assertEquals("24", getQueryDefinitionAPI().getProcessMetaData(processUUID, "total"));

    getManagementAPI().deleteProcess(processUUID);
  }

  public void testProcessNotAJavaNameName() {
    try {
      ProcessBuilder.createProcess("0Car", "1.0").done();
      fail("The processName is not a java name");
    } catch (final Exception e) {
    }
  }

  public void testProcessNullVersion() {
    final ProcessDefinition definition = ProcessBuilder.createProcess("nullVersion", null).done();
    Assert.assertEquals("1.0", definition.getVersion());
  }

  public void testProcessEmptyVersion() {
    final ProcessDefinition definition = ProcessBuilder.createProcess("emptyVersion", "").done();
    Assert.assertEquals("1.0", definition.getVersion());
  }

  public void testProcessWhiteSpaceVersion() {
    final ProcessDefinition definition = ProcessBuilder.createProcess("whiteSpacesVersion", "    ").done();
    Assert.assertEquals("1.0", definition.getVersion());
  }

  public void testProcessNullDataName() {
    try {
      ProcessBuilder.createProcess("nullDataName", "").addStringData(null).done();
      fail("The name of the data is null");
    } catch (final Exception e) {
    }
  }

  public void testProcessEmptyDataName() {
    try {
      ProcessBuilder.createProcess("emptyDataName", "").addStringData("").done();
      fail("The name of the data is empty");
    } catch (final Exception e) {
    }
  }

  public void testProcessWhiteSpaceDataName() {
    try {
      ProcessBuilder.createProcess("whiteSpacesDataName", "").addStringData("   ").done();
      fail("The name of the data is empty");
    } catch (final Exception e) {
    }
  }

  public void testProcessNullDataType() {
    ProcessBuilder.createProcess("nullDataType", "").addStringData("car", null).done();
  }

  public void testProcessSystemTaskWithATaskEventConenctor() {
    try {
      ProcessBuilder.createProcess("nullEnumDataName", "").addSystemTask("start")
          .addConnector(Event.taskOnStart, SetVarConnector.class.getName(), true).done();
      fail("A system task needs an automatic event");
    } catch (final BonitaRuntimeException e) {
    }
  }

  public void testProcessHumanTaskWithAnAutomaticEventConnector() {
    try {
      ProcessBuilder.createProcess("nullEnumDataName", "").addHuman("john").addHumanTask("start", "john")
          .addConnector(Event.automaticOnEnter, SetVarConnector.class.getName(), true).done();
      fail("A human task needs a task event");
    } catch (final BonitaRuntimeException e) {
    }
  }

  public void testProcessTwoTasksSameName() {
    try {
      ProcessBuilder.createProcess("sameName", "1.0").addSystemTask("start").addSystemTask("start").done();
      fail("Two activities cannot have the same name");
    } catch (final BonitaRuntimeException e) {
    }
  }

  public void testProcessTwoGroupsSameName() {
    try {
      ProcessBuilder.createProcess("sameName", "1.0").addGroup("Customer").addGroup("Customer").done();
      fail("Two groups cannot have the same name");
    } catch (final BonitaRuntimeException e) {
    }
  }

  public void testProcessAGroupAHUmanSameName() {
    try {
      ProcessBuilder.createProcess("sameName", "1.0").addGroup("Customer").addHuman("Customer").done();
      fail("A group and a human cannot have the same name");
    } catch (final BonitaRuntimeException e) {
    }
  }

  public void testProcessTwoHumansSameName() {
    try {
      ProcessBuilder.createProcess("sameName", "1.0").addHuman("Customer").addHuman("Customer").done();
      fail("Two humans cannot have the same name");
    } catch (final BonitaRuntimeException e) {
    }
  }

  public void testProcessTwoDataSameName() {
    try {
      ProcessBuilder.createProcess("sameName", "1.0").addStringData("string").addCharData("string").done();
      fail("Two processes data cannot have the same name");
    } catch (final BonitaRuntimeException e) {
    }
  }

  public void testProcessATaskTwoDataSameName() {
    try {
      ProcessBuilder.createProcess("sameName", "1.0").addSystemTask("system").addStringData("string")
          .addCharData("string").done();
      fail("Two activity data cannot have the same name");
    } catch (final BonitaRuntimeException e) {
    }
  }

  public void testProcessATaskTwoData() {
    try {
      Assert.assertNotNull(ProcessBuilder.createProcess("sameName", "1.0").addCharData("string")
          .addSystemTask("system").addStringData("string").done());
    } catch (final Exception e) {
      fail("A process data and an activiy data can have the same name");
    }
  }

  public void testProcessTwoTransitionsSameName() {
    try {
      ProcessBuilder.createProcess("sameName", "1.0").addSystemTask("system").addSystemTask("start")
          .addSystemTask("end").addTransition("go", "start", "system").addTransition("go", "system", "end").done();
      fail("Two tranisitions cannot have the same name");
    } catch (final BonitaRuntimeException e) {
    }
  }

  public void testActivityInstanceLabelAndDescription() throws BonitaException {
    ProcessDefinition process = ProcessBuilder.createProcess("p", "1.0").addGroup("group")
        .addGroupResolver(ProcessInitiatorRoleResolver.class.getName()).addHumanTask("taskName", "group")
        .addLabel("taskLabel").addDescription("taskDescription").done();
    process = getManagementAPI().deploy(getBusinessArchive(process, null, ProcessInitiatorRoleResolver.class));
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(process.getUUID());
    final Collection<TaskInstance> tasks = getQueryRuntimeAPI().getTasks(instanceUUID);

    assertNotNull(tasks);
    assertFalse(tasks.isEmpty());
    assertEquals(1, tasks.size());

    final TaskInstance task = tasks.iterator().next();
    assertEquals("taskName", task.getActivityName());
    assertEquals("taskLabel", task.getActivityLabel());
    assertEquals("taskDescription", task.getActivityDescription());

    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().deleteProcess(process.getUUID());
  }

  public void testLabelOnGroup() throws BonitaException {
    ProcessDefinition p = null;

    p = ProcessBuilder.createProcess("p", "1").addSystemTask("a").addGroup("group").addLabel("myLabel")
        .addGroupResolver(UserListRoleResolver.class.getName()).addInputParameter("setUsers", "john, jack").done();

    ParticipantDefinition group = null;
    for (final ParticipantDefinition participant : p.getParticipants()) {
      if (participant.getName().equals("group")) {
        group = participant;
        break;
      }
    }
    assertNotNull(group);
    assertEquals("myLabel", group.getLabel());
  }

  public void testProcessVersion() throws BonitaException {
    final ProcessDefinition p = ProcessBuilder.createProcess("p", "2.0").addSystemTask("t").done();
    assertEquals("2.0", p.getVersion());
    final ProcessDefinition process = getManagementAPI().deploy(getBusinessArchive(p));
    assertEquals("2.0", process.getVersion());
    getManagementAPI().deleteProcess(process.getUUID());
  }

  public void testFirstActivities() throws BonitaException {
    ProcessDefinition p = null;

    p = ProcessBuilder.createProcess("p", "1").addSystemTask("a").done();
    checkInitialActivity(p, "a");

    p = ProcessBuilder.createProcess("p", "1").addSystemTask("a").addSystemTask("b").done();
    checkInitialActivity(p, "a", "b");

    p = ProcessBuilder.createProcess("p", "1").addSystemTask("a").addSystemTask("b").addTransition("a", "b").done();
    checkInitialActivity(p, "a");

    p = ProcessBuilder.createProcess("p", "1").addSystemTask("a").addSystemTask("b").addTransition("a_b", "a", "b")
        .done();
    checkInitialActivity(p, "a");

    p = ProcessBuilder.createProcess("p", "1").addSystemTask("a").addSystemTask("b").addSystemTask("c")
        .addTransition("a_b", "a", "b").done();
    checkInitialActivity(p, "a", "c");

    p = ProcessBuilder.createProcess("p", "1").addSystemTask("a").addSystemTask("b").addSystemTask("c")
        .addTransition("a_b", "a", "b").addTransition("b_c", "b", "c").done();
    checkInitialActivity(p, "a");

    p = ProcessBuilder.createProcess("p", "1").addSystemTask("a").addSystemTask("b").addSystemTask("c")
        .addTransition("a_b", "a", "b").addTransition("a_c", "a", "c").done();
    checkInitialActivity(p, "a");
  }

  private void checkInitialActivity(final ProcessDefinition process, final String... expectedActivities)
      throws BonitaException {
    assertNotNull(process);
    assertEquals(process.getInitialActivities().size(), expectedActivities.length);
    for (final String expectedActivity : expectedActivities) {
      assertTrue(process.getInitialActivities().containsKey(expectedActivity));
    }
    final ProcessDefinition deployedProcess = getManagementAPI().deploy(getBusinessArchive(process));
    assertEquals(deployedProcess.getInitialActivities().size(), expectedActivities.length);
    for (final String expectedActivity : expectedActivities) {
      assertTrue(deployedProcess.getInitialActivities().containsKey(expectedActivity));
    }
    getManagementAPI().deleteProcess(deployedProcess.getUUID());
  }

  public void testProcessConnectorsWithWrongEvent() {
    try {
      ProcessBuilder.createProcess("A", null)
          .addConnector(Event.automaticOnEnter, SetVarConnector.class.getName(), true).done();
      fail("At process level, a connector cannot have the automaticOnEnter event");
    } catch (final BonitaRuntimeException e) {
      // OK
    }
  }

  public void testProcessConnectorsWithWrongEvent2() {
    try {
      ProcessBuilder.createProcess("A", null).addConnector(Event.taskOnReady, SetVarConnector.class.getName(), true)
          .done();
      fail("At process level, a connector cannot have the taskOnReady event");
    } catch (final BonitaRuntimeException e) {
      // OK
    }
  }

  public void testProcessConnectorsWithWrongEvent3() {
    try {
      ProcessBuilder.createProcess("A", null).addSystemTask("task")
          .addConnector(Event.instanceOnFinish, SetVarConnector.class.getName(), true).done();
      fail("At activity level, a connector cannot have the instanceOnFinish event");
    } catch (final BonitaRuntimeException e) {
      // OK
    }
  }

  public void testProcessConnectorsWithWrongEvent4() {
    try {
      ProcessBuilder.createProcess("A", null).addSystemTask("task")
          .addConnector(Event.instanceOnCancel, SetVarConnector.class.getName(), true).done();
      fail("At activity level, a connector cannot have the instanceOnCancel event");
    } catch (final BonitaRuntimeException e) {
      // OK
    }
  }

  public void testBAMProcess() throws Exception {
    ProcessDefinition process = ProcessBuilder.createProcess("bam", "1.0").addHuman("john")
        .addHumanTask("bamTask", "john").addActivityExecutingTime(3600).addActivityPriority(1)
        .addSystemTask("bamSystem").addActivityExecutingTime(1000).addActivityPriority(2)
        .addTransition("bamTask", "bamSystem").done();

    process = getManagementAPI().deploy(getBusinessArchive(process, null, ProcessInitiatorRoleResolver.class));
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(process.getUUID());

    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().disable(process.getUUID());
    getManagementAPI().deleteProcess(process.getUUID());
  }

  public void testProcessTransitions() throws Exception {
    ProcessDefinition process = ProcessBuilder.createProcess("bam", "1.0").addHuman("john")
        .addHumanTask("bamTask", "john").addSystemTask("bamSystem").addTransition("bamTask", "bamSystem").done();

    process = getManagementAPI().deploy(getBusinessArchive(process, null, ProcessInitiatorRoleResolver.class));
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(process.getUUID());

    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().disable(process.getUUID());
    getManagementAPI().deleteProcess(process.getUUID());
  }

  public void testSimpleDatafieldInitialValue() throws Exception {
    ProcessDefinition process = ProcessBuilder.createProcess("p", "1.0").addSystemTask("t").addBooleanData("b", false)
        .done();
    final BusinessArchive businessArchive = BusinessArchiveFactory.getBusinessArchive(process);
    process = getManagementAPI().deploy(businessArchive);

    getRuntimeAPI().instantiateProcess(process.getUUID());
    getManagementAPI().deleteProcess(process.getUUID());
  }

  public void testManyStartingNodes() throws Exception {
    ProcessDefinition process = ProcessBuilder.createProcess("p", "1.0").addHuman(getLogin()).addSystemTask("t1")
        .addSystemTask("t2").addSystemTask("end").addJoinType(JoinType.AND).addTransition("t1", "end")
        .addTransition("t2", "end").done();
    final BusinessArchive businessArchive = BusinessArchiveFactory.getBusinessArchive(process);
    process = getManagementAPI().deploy(businessArchive);

    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(process.getUUID());

    checkExecutedOnce(instanceUUID, "t1", "t2", "end");
    getManagementAPI().deleteProcess(process.getUUID());
  }

  public void testDefaultTransitions() {
    final ProcessDefinition process = ProcessBuilder.createProcess("p", "1.0").addSystemTask("t1").addSystemTask("t2")
        .addTransition("t1", "t2").setDefault().done();
    final Set<TransitionDefinition> transitions = process.getActivity("t1").getOutgoingTransitions();
    final TransitionDefinition transition = transitions.iterator().next();
    assertTrue(transition.isDefault());
  }

  public void testTwoDefaultTransitionFromTheSameActivity() {
    final ProcessBuilder process = ProcessBuilder.createProcess("p", "1.0").addSystemTask("t1").addSystemTask("t2")
        .addSystemTask("t3").addTransition("t1", "t2").setDefault().addTransition("t1", "t3").setDefault();
    try {
      process.done();
      fail("It must be impossible to define more than one default transition.");
    } catch (final BonitaRuntimeException e) {
    }
  }

  public void testDefaultAndConditionOnATransition() {
    final ProcessBuilder process = ProcessBuilder.createProcess("p", "1.0").addSystemTask("t1").addSystemTask("t2")
        .addTransition("t1", "t2").setDefault().addCondition("forbidden !");
    try {
      process.done();
      fail("It must be impossible to define a condition on a default transition.");
    } catch (final BonitaRuntimeException e) {
      // OK
    }
  }

  public void testAddAnInstantitorWithoutAJoinChecker() {
    try {
      ProcessBuilder.createProcess("crash", "1.0").addSystemTask("ouch")
          .addMultipleActivitiesInstantiator("org.bonitasoft.connectors.SimpleInitiator").done();
      fail("The joinChecker is missing!");
    } catch (final BonitaRuntimeException e) {
      assertTrue(e.getMessage().contains("JoinChecker is undefined"));
    }
  }

  public void testAddAJoinCheckerWithoutAnInstantitor() {
    try {
      ProcessBuilder.createProcess("crash", "1.0").addSystemTask("ouch")
          .addMultipleActivitiesJoinChecker("org.bonitasoft.connectors.SimpleJoinChecker").done();
      fail("The joinChecker is missing!");
    } catch (final BonitaRuntimeException e) {
      assertTrue(e.getMessage().contains("Instantiator is undefined"));
    }
  }

  public void testAddMultipleActivities() {
    final String joinCheckerClassName = "org.bonitasoft.connectors.SimpleJoinChecker";
    final String initiatorClassName = "org.bonitasoft.connectors.SimpleInitiator";

    final ProcessDefinition definition = ProcessBuilder.createProcess("mulitpleActivities", "1.0")
        .addSystemTask("cool").addMultipleActivitiesInstantiator(initiatorClassName).addInputParameter("field1", 1)
        .addMultipleActivitiesJoinChecker(joinCheckerClassName).done();

    final ActivityDefinition activity = definition.getActivity("cool");
    final MultiInstantiationDefinition initiator = activity.getMultipleInstancesInstantiator();
    final MultiInstantiationDefinition joinChecker = activity.getMultipleInstancesJoinChecker();

    assertEquals(initiatorClassName, initiator.getClassName());
    final Map<String, Object[]> parameters = initiator.getParameters();
    assertEquals(1, parameters.size());
    assertEquals(1, parameters.get("setField1")[0]);

    assertEquals(joinCheckerClassName, joinChecker.getClassName());
  }

  public void testReplaceContext() {
    final Properties props = new Properties();
    props.put("key1", "value1");
    props.put("key2", "value2");

    final String before = "key1=" + BonitaConstants.CONTEXT_PREFIX + "key1" + BonitaConstants.CONTEXT_SUFFIX
        + ", key2=" + BonitaConstants.CONTEXT_PREFIX + "key2" + BonitaConstants.CONTEXT_SUFFIX;
    final String after = ProcessBuilder.resolveWithContext(before, props);

    assertEquals("key1=value1, key2=value2", after);
  }

  public void testContextOnInitialValue() throws Exception {
    final ProcessDefinition processWorkingCopy = ProcessBuilder
        .createProcess("testContext", "0.1")
        .addGroup("initiator")
        .addGroupResolver(InstanceInitiator.class.getName())
        .addStringData("processString",
            BonitaConstants.CONTEXT_PREFIX + "processStringKey" + BonitaConstants.CONTEXT_SUFFIX)
        .addHumanTask("activity", "initiator")
        .addStringData("activityString",
            BonitaConstants.CONTEXT_PREFIX + "activityStringKey" + BonitaConstants.CONTEXT_SUFFIX).done();

    final Properties context = new Properties();
    context.put("processStringKey", "processStringValue");
    context.put("activityStringKey", "activityStringValue");

    final File xmlDefFile = File.createTempFile("def", ".xml");
    xmlDefFile.deleteOnExit();
    final byte[] xmlDefContent = XmlDefExporter.getInstance().createProcessDefinition(processWorkingCopy);
    Misc.getFile(xmlDefFile, xmlDefContent);
    final ProcessDefinition definition = ProcessBuilder.createProcessFromXmlDefFile(xmlDefFile.toURL(), context);

    final ProcessDefinition process = getManagementAPI().deploy(getBusinessArchive(definition));
    final ProcessInstanceUUID processInstanceUUID = getRuntimeAPI().instantiateProcess(process.getUUID());
    final ActivityInstanceUUID activity = getQueryRuntimeAPI().getOneTask(processInstanceUUID, ActivityState.READY);

    assertEquals("processStringValue", getQueryRuntimeAPI().getVariable(activity, "processString"));
    assertEquals("activityStringValue", getQueryRuntimeAPI().getVariable(activity, "activityString"));

    getRuntimeAPI().deleteProcessInstance(processInstanceUUID);
    getManagementAPI().deleteProcess(process.getUUID());
  }

  public void testContextOnConnectorInput() throws Exception {
    final ProcessDefinition processWorkingCopy = ProcessBuilder
        .createProcess("testContext", "0.1")
        .addGroup("initiator")
        .addGroupResolver(InstanceInitiator.class.getName())
        .addStringData("processString", "")
        .addHumanTask("activity", "initiator")
        .addConnector(Event.taskOnReady, SetVarConnector.class.getName(), true)
        .addInputParameter("value",
            BonitaConstants.CONTEXT_PREFIX + "processStringKey" + BonitaConstants.CONTEXT_SUFFIX)
        .addInputParameter("variableName", "processString").done();

    final Properties context = new Properties();
    context.put("processStringKey", "processStringValue");

    final File xmlDefFile = File.createTempFile("def", ".xml");
    xmlDefFile.deleteOnExit();
    final byte[] xmlDefContent = XmlDefExporter.getInstance().createProcessDefinition(processWorkingCopy);
    Misc.getFile(xmlDefFile, xmlDefContent);
    final ProcessDefinition definition = ProcessBuilder.createProcessFromXmlDefFile(xmlDefFile.toURL(), context);

    final ProcessDefinition process = getManagementAPI().deploy(
        getBusinessArchive(definition, null, SetVarConnector.class));
    final ProcessInstanceUUID processInstanceUUID = getRuntimeAPI().instantiateProcess(process.getUUID());
    final ActivityInstanceUUID activity = getQueryRuntimeAPI().getOneTask(processInstanceUUID, ActivityState.READY);

    assertEquals("processStringValue", getQueryRuntimeAPI().getVariable(activity, "processString"));

    getRuntimeAPI().deleteProcessInstance(processInstanceUUID);
    getManagementAPI().deleteProcess(process.getUUID());
  }

  public void testContextOnConnectorInputWithMissingProperty() throws Exception {
    final ProcessDefinition processWorkingCopy = ProcessBuilder
        .createProcess("testContext", "0.1")
        .addGroup("initiator")
        .addGroupResolver(InstanceInitiator.class.getName())
        .addStringData("processString", "")
        .addHumanTask("activity", "initiator")
        .addConnector(Event.taskOnReady, SetVarConnector.class.getName(), true)
        .addInputParameter("value",
            BonitaConstants.CONTEXT_PREFIX + "processStringKey" + BonitaConstants.CONTEXT_SUFFIX)
        .addInputParameter("variableName", "processString").done();

    final Properties context = new Properties();

    final File xmlDefFile = File.createTempFile("def", ".xml");
    xmlDefFile.deleteOnExit();
    final byte[] xmlDefContent = XmlDefExporter.getInstance().createProcessDefinition(processWorkingCopy);
    Misc.getFile(xmlDefFile, xmlDefContent);
    final ProcessDefinition definition = ProcessBuilder.createProcessFromXmlDefFile(xmlDefFile.toURL(), context);

    final ProcessDefinition process = getManagementAPI().deploy(
        getBusinessArchive(definition, null, SetVarConnector.class));
    final ProcessInstanceUUID processInstanceUUID = getRuntimeAPI().instantiateProcess(process.getUUID());
    final ActivityInstanceUUID activity = getQueryRuntimeAPI().getOneTask(processInstanceUUID, ActivityState.READY);

    assertEquals(BonitaConstants.CONTEXT_PREFIX + "processStringKey" + BonitaConstants.CONTEXT_SUFFIX,
        getQueryRuntimeAPI().getVariable(activity, "processString"));

    getRuntimeAPI().deleteProcessInstance(processInstanceUUID);
    getManagementAPI().deleteProcess(process.getUUID());
  }

  public void testAddBoundaryEvents() throws Exception {
    final ProcessDefinition definition = ProcessBuilder.createProcess("events", "0.1").addHuman(getLogin())
        .addHumanTask("step1", getLogin()).addLabel("The step label").addDescription("description:activity")
        .addTimerBoundaryEvent("In_5_seconds", "5000").addDescription("description:seconds").addLabel("In 5 seconds")
        .addTimerBoundaryEvent("In_a_minute", "60000").addDescription("description:minute").addLabel("In a minute")
        .addHumanTask("normalStep", getLogin()).addHumanTask("exceptionStep", getLogin())
        .addTransition("step1", "normalStep").addExceptionTransition("step1", "In_5_seconds", "exceptionStep")
        .addExceptionTransition("step1", "In_a_minute", "exceptionStep").done();

    final Set<ActivityDefinition> activities = definition.getActivities();
    ActivityDefinition activity = getActivityDefinition(activities, "step1");
    assertEquals("The step label", activity.getLabel());
    assertEquals("description:activity", activity.getDescription());
    final List<BoundaryEvent> boundaryEvents = activity.getBoundaryEvents();
    assertEquals(2, boundaryEvents.size());
    BoundaryEvent boundaryEvent = getBoundaryEvent(boundaryEvents, "In_5_seconds");
    assertEquals("In 5 seconds", boundaryEvent.getLabel());
    assertEquals("description:seconds", boundaryEvent.getDescription());
    boundaryEvent = getBoundaryEvent(boundaryEvents, "In_a_minute");
    assertEquals("In a minute", boundaryEvent.getLabel());
    assertEquals("description:minute", boundaryEvent.getDescription());
    activity = getActivityDefinition(activities, "exceptionStep");
    final Set<TransitionDefinition> incomingTranistions = activity.getIncomingTransitions();
    assertEquals(2, incomingTranistions.size());
    TransitionDefinition transition = getTransition(incomingTranistions, "step1__In_5_seconds__exceptionStep");
    assertEquals("step1", transition.getFrom());
    assertEquals("In_5_seconds", transition.getFromBoundaryEvent());
    assertEquals("exceptionStep", transition.getTo());
    transition = getTransition(incomingTranistions, "step1__In_a_minute__exceptionStep");
    assertEquals("step1", transition.getFrom());
    assertEquals("In_a_minute", transition.getFromBoundaryEvent());
    assertEquals("exceptionStep", transition.getTo());
  }

  public void testAddErrorBoundaryEvents() throws Exception {
    final ProcessDefinition definition = ProcessBuilder.createProcess("events", "0.1").addHuman(getLogin())
        .addHumanTask("step1", getLogin()).addLabel("The step label").addDescription("description:activity")
        .addErrorBoundaryEvent("Cancel_rent", "cancelRent").addDescription("description:The rent was cancelled.")
        .addLabel("Cancel_rent").addErrorBoundaryEvent("Crash_system").addDescription("description:all other errors")
        .addLabel("Crash").addHumanTask("normalStep", getLogin()).addHumanTask("cancelStep", getLogin())
        .addHumanTask("crashStep", getLogin()).addTransition("step1", "normalStep")
        .addExceptionTransition("step1", "Cancel_rent", "cancelStep")
        .addExceptionTransition("step1", "Crash_system", "crashStep").done();

    final Set<ActivityDefinition> activities = definition.getActivities();
    ActivityDefinition activity = getActivityDefinition(activities, "step1");
    assertEquals("The step label", activity.getLabel());
    assertEquals("description:activity", activity.getDescription());
    final List<BoundaryEvent> boundaryEvents = activity.getBoundaryEvents();
    assertEquals(2, boundaryEvents.size());
    BoundaryEvent boundaryEvent = getBoundaryEvent(boundaryEvents, "Crash_system");
    assertEquals("Crash", boundaryEvent.getLabel());
    assertEquals("description:all other errors", boundaryEvent.getDescription());
    boundaryEvent = getBoundaryEvent(boundaryEvents, "Cancel_rent");
    assertEquals("Cancel_rent", boundaryEvent.getLabel());
    assertEquals("description:The rent was cancelled.", boundaryEvent.getDescription());
    activity = getActivityDefinition(activities, "cancelStep");
    Set<TransitionDefinition> incomingTranistions = activity.getIncomingTransitions();
    assertEquals(1, incomingTranistions.size());
    TransitionDefinition transition = getTransition(incomingTranistions, "step1__Cancel_rent__cancelStep");
    assertEquals("step1", transition.getFrom());
    assertEquals("Cancel_rent", transition.getFromBoundaryEvent());
    assertEquals("cancelStep", transition.getTo());
    activity = getActivityDefinition(activities, "crashStep");
    incomingTranistions = activity.getIncomingTransitions();
    transition = getTransition(incomingTranistions, "step1__Crash_system__crashStep");
    assertEquals("step1", transition.getFrom());
    assertEquals("Crash_system", transition.getFromBoundaryEvent());
    assertEquals("crashStep", transition.getTo());
  }

  public void testAddBoundaryEventsWithAnUnknwonTransition() throws Exception {
    final ProcessBuilder builder = ProcessBuilder.createProcess("events", "0.1").addHuman(getLogin())
        .addHumanTask("step1", getLogin()).addLabel("The step label").addDescription("description:activity")
        .addTimerBoundaryEvent("In_5_seconds", "5000").addDescription("description:seconds").addLabel("In 5 seconds")
        .addTimerBoundaryEvent("In_4_seconds", "4000").addDescription("description:minute").addLabel("In a minute")
        .addHumanTask("normalStep", getLogin()).addHumanTask("exceptionStep", getLogin())
        .addTransition("step1", "normalStep").addExceptionTransition("step1", "In_5_seconds", "exceptionStep")
        .addExceptionTransition("step1", "In_a_minute", "exceptionStep");

    try {
      builder.done();
      fail("In_a_minute does not exist");
    } catch (final BonitaRuntimeException e) {
    }
  }

  public void testAddBoundaryEventsWithTheSameName() throws Exception {
    final ProcessDefinition definition = ProcessBuilder.createProcess("events", "0.1").addHuman(getLogin())
        .addHumanTask("step1", getLogin()).addLabel("The step label").addDescription("description:activity")
        .addTimerBoundaryEvent("In_5_seconds", "5000").addDescription("description:seconds").addLabel("In 5 seconds")
        .addTimerBoundaryEvent("In_5_seconds", "60000").addDescription("description:minute").addLabel("In a minute")
        .addHumanTask("normalStep", getLogin()).addHumanTask("exceptionStep", getLogin())
        .addTransition("step1", "normalStep").addExceptionTransition("step1", "In_5_seconds", "exceptionStep").done();

    final Set<ActivityDefinition> activities = definition.getActivities();
    ActivityDefinition activity = getActivityDefinition(activities, "step1");
    assertEquals("The step label", activity.getLabel());
    assertEquals("description:activity", activity.getDescription());
    final List<BoundaryEvent> boundaryEvents = activity.getBoundaryEvents();
    assertEquals(1, boundaryEvents.size());
    final BoundaryEvent boundaryEvent = getBoundaryEvent(boundaryEvents, "In_5_seconds");
    assertEquals("In a minute", boundaryEvent.getLabel());
    assertEquals("description:minute", boundaryEvent.getDescription());
    activity = getActivityDefinition(activities, "exceptionStep");
    final Set<TransitionDefinition> incomingTranistions = activity.getIncomingTransitions();
    assertEquals(1, incomingTranistions.size());
    final TransitionDefinition transition = getTransition(incomingTranistions, "step1__In_5_seconds__exceptionStep");
    assertEquals("step1", transition.getFrom());
    assertEquals("In_5_seconds", transition.getFromBoundaryEvent());
    assertEquals("exceptionStep", transition.getTo());
  }

  public void testDefaultExceptionTransition() {
    final ProcessBuilder builder = ProcessBuilder.createProcess("events", "0.1").addHuman(getLogin())
        .addHumanTask("step1", getLogin()).addTimerBoundaryEvent("In_5_seconds", "5000")
        .addHumanTask("normalStep", getLogin()).addHumanTask("exceptionStep", getLogin())
        .addExceptionTransition("step1", "In_5_seconds", "exceptionStep").setDefault();

    try {
      builder.done();
      fail("Unable to set default transition on a ProcessDefinition");
    } catch (final BonitaRuntimeException e) {
    }
  }

  public void testConditionExceptionTransition() {
    final ProcessBuilder builder = ProcessBuilder.createProcess("events", "0.1").addHuman(getLogin())
        .addHumanTask("step1", getLogin()).addTimerBoundaryEvent("In_5_seconds", "5000")
        .addHumanTask("normalStep", getLogin()).addHumanTask("exceptionStep", getLogin())
        .addExceptionTransition("step1", "In_5_seconds", "exceptionStep").addCondition("true");

    try {
      builder.done();
      fail("Unable to set condition transition on a ProcessDefinition");
    } catch (final BonitaRuntimeException e) {
    }
  }

  public void testConditionTransitionExceptionTransitionWithAnotherTransition() {
    final ProcessBuilder builder = ProcessBuilder.createProcess("events", "0.1").addHuman(getLogin())
        .addHumanTask("step1", getLogin()).addTimerBoundaryEvent("In_5_seconds", "5000")
        .addHumanTask("normalStep", getLogin()).addHumanTask("exceptionStep", getLogin())
        .addTransition("step1", "normalStep").addExceptionTransition("step1", "In_5_seconds", "exceptionStep")
        .addCondition("true");
    try {
      builder.done();
      fail("Unable to set condition transition on a ProcessDefinition");
    } catch (final BonitaRuntimeException e) {
    }
  }

  public void testTranstitionBetweenTwoBoundaryEvents() throws Exception {
    final ProcessBuilder builder = ProcessBuilder.createProcess("events", "0.1").addHuman(getLogin())
        .addHumanTask("step1", getLogin()).addErrorBoundaryEvent("Cancel_rent", "cancelRent")
        .addErrorBoundaryEvent("Crash_system").addHumanTask("normalStep", getLogin())
        .addHumanTask("cancelStep", getLogin()).addHumanTask("crashStep", getLogin())
        .addTransition("step1", "normalStep").addExceptionTransition("step1", "Cancel_rent", "cancelStep")
        .addExceptionTransition("step1", "Crash_system", "Cancel_rent");
    try {
      builder.done();
      fail("Unable to add transition from step1 to Cancel_rent. To activity does not exists");
    } catch (final BonitaRuntimeException e) {
    }
  }

  public void testErrorBoundaryEventOnATask() {
    try {
      ProcessBuilder.createProcess("bndrvnts", "1.4").addSystemTask("task").addErrorBoundaryEvent("error")
          .addSystemTask("error").addExceptionTransition("task", "error", "error").done();
    } catch (final BonitaRuntimeException e) {
      fail("An error boundary event can be added on a task");
    }
  }

  public void testTimerBoundaryEventOnATask() {
    try {
      ProcessBuilder.createProcess("bndrvnts", "1.4").addSystemTask("task").addTimerBoundaryEvent("over", "5000")
          .addSystemTask("overdue").addExceptionTransition("task", "over", "overdue").done();
      fail("A timer boundary event cannot be added on a task");
    } catch (final BonitaRuntimeException e) {
    }
  }

  public void testSignalBoundaryEventOnATask() {
    try {
      ProcessBuilder.createProcess("bndrvnts", "1.4").addSystemTask("task")
          .addSignalBoundaryEvent("signal", "broadcast").addSystemTask("overdue")
          .addExceptionTransition("task", "signal", "overdue").done();
      fail("A signal boundary event cannot be added on a task");
    } catch (final BonitaRuntimeException e) {
    }
  }

  public void testMessageBoundaryEventOnATask() {
    try {
      ProcessBuilder.createProcess("bndrvnts", "1.4").addSystemTask("task")
          .addMessageBoundaryEvent("message", "message").addSystemTask("overdue")
          .addExceptionTransition("task", "message", "overdue").done();
      fail("A message boundary event cannot be added on a task");
    } catch (final BonitaRuntimeException e) {
    }
  }

  public void testErrorBoundaryEventOnAUserTask() {
    try {
      ProcessBuilder.createProcess("bndrvnts", "1.4").addHuman(getLogin()).addHumanTask("task", getLogin())
          .addErrorBoundaryEvent("error").addSystemTask("error").addExceptionTransition("task", "error", "error")
          .done();
    } catch (final BonitaRuntimeException e) {
      fail("An error boundary event can be added on a user task");
    }
  }

  public void testTimerBoundaryEventOnAUserTask() {
    try {
      ProcessBuilder.createProcess("bndrvnts", "1.4").addHuman(getLogin()).addHumanTask("task", getLogin())
          .addTimerBoundaryEvent("over", "5000").addSystemTask("overdue")
          .addExceptionTransition("task", "over", "overdue").done();
    } catch (final BonitaRuntimeException e) {
      fail("A timer boundary event can be added on a user task");
    }
  }

  public void testSignalBoundaryEventOnAUserTask() {
    try {
      ProcessBuilder.createProcess("bndrvnts", "1.4").addHuman(getLogin()).addHumanTask("task", getLogin())
          .addSignalBoundaryEvent("signal", "broadcast").addSystemTask("overdue")
          .addExceptionTransition("task", "signal", "overdue").done();
    } catch (final BonitaRuntimeException e) {
      fail("A signal boundary event can be added on a user task");
    }
  }

  public void testMessageBoundaryEventOnAUserTask() {
    try {
      ProcessBuilder.createProcess("bndrvnts", "1.4").addHuman(getLogin()).addHumanTask("task", getLogin())
          .addMessageBoundaryEvent("message", "message").addSystemTask("overdue")
          .addExceptionTransition("task", "message", "overdue").done();
    } catch (final BonitaRuntimeException e) {
      fail("A message boundary event can be added on a user task");
    }
  }

  public void testErrorBoundaryEventOnASubProcess() {
    try {
      ProcessBuilder.createProcess("bndrvnts", "1.4").addSubProcess("subProcess", "rent")
          .addErrorBoundaryEvent("error").addSystemTask("error").addExceptionTransition("subProcess", "error", "error")
          .done();
    } catch (final BonitaRuntimeException e) {
      fail("An error boundary event can be added on a sub-process");
    }
  }

  public void testTimerBoundaryEventOnASubProcess() {
    try {
      ProcessBuilder.createProcess("bndrvnts", "1.4").addSubProcess("subProcess", "rent")
          .addTimerBoundaryEvent("over", "5000").addSystemTask("overdue")
          .addExceptionTransition("subProcess", "over", "overdue").done();
    } catch (final BonitaRuntimeException e) {
      fail("A timer boundary event can be added on a sub-process");
    }
  }

  public void testSignalBoundaryEventOnASubProcess() {
    try {
      ProcessBuilder.createProcess("bndrvnts", "1.4").addSubProcess("subProcess", "rent")
          .addSignalBoundaryEvent("signal", "broadcast").addSystemTask("overdue")
          .addExceptionTransition("subProcess", "signal", "overdue").done();
    } catch (final BonitaRuntimeException e) {
      fail("A signal boundary event can be added on a sub-process");
    }
  }

  public void testMessageBoundaryEventOnASubProcess() {
    try {
      ProcessBuilder.createProcess("bndrvnts", "1.4").addSubProcess("subPorcess", "rent")
          .addMessageBoundaryEvent("message", "message").addSystemTask("overdue")
          .addExceptionTransition("subPorcess", "message", "overdue").done();
    } catch (final BonitaRuntimeException e) {
      fail("A message boundary event can be added on a sub-process");
    }
  }

  public void testErrorBoundaryEventOnASendTask() {
    try {
      ProcessBuilder.createProcess("bndrvnts", "1.4").addSendEventTask("send").addErrorBoundaryEvent("error")
          .addSystemTask("error").addExceptionTransition("send", "error", "error").done();
      fail("An error boundary event cannot be added on a send task");
    } catch (final BonitaRuntimeException e) {
    }
  }

  public void testTimerBoundaryEventOnASendTask() {
    try {
      ProcessBuilder.createProcess("bndrvnts", "1.4").addSendEventTask("send").addTimerBoundaryEvent("over", "5000")
          .addSystemTask("overdue").addExceptionTransition("send", "over", "overdue").done();
      fail("A timer boundary event cannot be added on a send task");
    } catch (final BonitaRuntimeException e) {
    }
  }

  public void testSignalBoundaryEventOnASendTask() {
    try {
      ProcessBuilder.createProcess("bndrvnts", "1.4").addSendEventTask("send")
          .addSignalBoundaryEvent("signal", "broadcast").addSystemTask("overdue")
          .addExceptionTransition("send", "signal", "overdue").done();
      fail("A signal boundary event cannot be added on a send task");
    } catch (final BonitaRuntimeException e) {
    }
  }

  public void testMessageBoundaryEventOnASendTask() {
    try {
      ProcessBuilder.createProcess("bndrvnts", "1.4").addSendEventTask("send")
          .addMessageBoundaryEvent("message", "message").addSystemTask("overdue")
          .addExceptionTransition("send", "message", "overdue").done();
      fail("A message boundary event cannot be added on a send task");
    } catch (final BonitaRuntimeException e) {
    }
  }

  public void testErrorBoundaryEventOnATimer() {
    try {
      ProcessBuilder.createProcess("bndrvnts", "1.4").addTimerTask("timer", "5000").addErrorBoundaryEvent("error")
          .addSystemTask("error").addExceptionTransition("timer", "error", "error").done();
      fail("An error boundary event cannot be added on a send task");
    } catch (final BonitaRuntimeException e) {
    }
  }

  public void testTimerBoundaryEventOnATimer() {
    try {
      ProcessBuilder.createProcess("bndrvnts", "1.4").addTimerTask("timer", "5000")
          .addTimerBoundaryEvent("over", "5000").addSystemTask("overdue")
          .addExceptionTransition("timer", "over", "overdue").done();
      fail("A timer boundary event cannot be added on a send task");
    } catch (final BonitaRuntimeException e) {
    }
  }

  public void testSignalBoundaryEventOnATimer() {
    try {
      ProcessBuilder.createProcess("bndrvnts", "1.4").addTimerTask("timer", "5000")
          .addSignalBoundaryEvent("signal", "broadcast").addSystemTask("overdue")
          .addExceptionTransition("timer", "signal", "overdue").done();
      fail("A signal boundary event cannot be added on a send task");
    } catch (final BonitaRuntimeException e) {
    }
  }

  public void testMessageBoundaryEventOnATimer() {
    try {
      ProcessBuilder.createProcess("bndrvnts", "1.4").addTimerTask("timer", "5000")
          .addMessageBoundaryEvent("message", "message").addSystemTask("overdue")
          .addExceptionTransition("timer", "message", "overdue").done();
      fail("A message boundary event cannot be added on a timer");
    } catch (final BonitaRuntimeException e) {
    }
  }

  public void testErrorBoundaryEventOnAGate() {
    try {
      ProcessBuilder.createProcess("bndrvnts", "1.4").addDecisionNode("gate").addErrorBoundaryEvent("error")
          .addSystemTask("error").addExceptionTransition("gate", "error", "error").done();
    } catch (final BonitaRuntimeException e) {
      fail("An error boundary event can be added on a gate");
    }
  }

  public void testTimerBoundaryEventOnAGate() {
    try {
      ProcessBuilder.createProcess("bndrvnts", "1.4").addDecisionNode("gate").addTimerBoundaryEvent("over", "5000")
          .addSystemTask("overdue").addExceptionTransition("gate", "over", "overdue").done();
      fail("A timer boundary event cannot be added on a gate");
    } catch (final BonitaRuntimeException e) {
    }
  }

  public void testSignalBoundaryEventOnAGate() {
    try {
      ProcessBuilder.createProcess("bndrvnts", "1.4").addDecisionNode("gate")
          .addSignalBoundaryEvent("signal", "broadcast").addSystemTask("overdue")
          .addExceptionTransition("gate", "signal", "overdue").done();
      fail("A signal boundary event cannot be added on a gate");
    } catch (final BonitaRuntimeException e) {
    }
  }

  public void testMessageBoundaryEventOnAGate() {
    try {
      ProcessBuilder.createProcess("bndrvnts", "1.4").addDecisionNode("gate")
          .addMessageBoundaryEvent("message", "message").addSystemTask("overdue")
          .addExceptionTransition("gate", "message", "overdue").done();
      fail("A message boundary event cannot be added on a gate");
    } catch (final BonitaRuntimeException e) {
    }
  }

  public void testEventSubProcessWithAHumanTask() {
    try {
      ProcessBuilder.createProcess("vntSbPrcss", "1.0").setEventSubProcess().addHuman(getLogin())
          .addHumanTask("step", getLogin()).done();
      fail("An event sub-process must start with a start event");
    } catch (final BonitaRuntimeException e) {
    }
  }

  public void testEventSubProcessWithASystemTask() {
    try {
      ProcessBuilder.createProcess("vntSbPrcss", "1.0").setEventSubProcess().addSystemTask("step").done();
      fail("An event sub-process must start with a start event");
    } catch (final BonitaRuntimeException e) {
    }
  }

  public void testEventSubProcessWithStartTasks() {
    try {
      ProcessBuilder.createProcess("vntSbPrcss", "1.0").setEventSubProcess().addHuman(getLogin())
          .addHumanTask("start1", getLogin()).addHumanTask("start2", getLogin()).addHumanTask("next", getLogin())
          .addTransition("start1", "next").addTransition("start2", "next").done();
      fail("An event sub-process must start with a start event");
    } catch (final BonitaRuntimeException e) {
    }
  }

  public void testEventSubProcessWithAStartTaskAndAStartEvent() {
    try {
      ProcessBuilder.createProcess("vntSbPrcss", "1.0").setEventSubProcess().addHuman(getLogin())
          .addHumanTask("start1", getLogin()).addTimerTask("start2", "60000").addHumanTask("next", getLogin())
          .addTransition("start1", "next").addTransition("start2", "next").done();
      fail("An event sub-process must start with a start event");
    } catch (final BonitaRuntimeException e) {
    }
  }

  public void testEventSubProcessWithAStartEvent() {
    ProcessBuilder.createProcess("vntSbPrcss", "1.0").setEventSubProcess().addTimerTask("In_a_minute", "60000").done();
  }

  public void testSetTransient() {
    final ProcessDefinition process = ProcessBuilder.createProcess("prcssWithTransientData", "1.0")
        .addSystemTask("activityWithTransientData").addStringData("transient").setTransient().done();

    final Set<DataFieldDefinition> dataFields = process.getActivity("activityWithTransientData").getDataFields();
    assertEquals(1, dataFields.size());
    assertTrue(dataFields.iterator().next().isTransient());
  }

  public void testSetProcessVariableTransient() {
    try {
      ProcessBuilder.createProcess("prcssWithTransientData", "1.0").addStringData("transient").setTransient().done();

      fail("Only activity variables can be transient");
    } catch (final BonitaRuntimeException e) {
    }
  }

  public void testAddAttachmentAndVariableWithIdenticalName() {
    try {
      ProcessBuilder.createProcess("pav", "2.0").addAttachment("a").addStringData("a").addHuman(getLogin())
          .addHumanTask("t", getLogin()).done();

      fail("An attachment and a variable cannot have the same name");
    } catch (final BonitaRuntimeException e) {
    }
  }

  public void testCannotDefineAnExpressionAndCorrelationKeysOnTheSameMessageEvent() {
    try {
      ProcessBuilder.createProcess("created", "1.0").addHuman(getLogin())
          .addReceiveEventTask("startEvent", "createProcess", "${true}").addMessageCorrelationKey("id", "${1}")
          .addSystemTask("startAuto").addHumanTask("human1", getLogin()).addHumanTask("human2", getLogin())
          .addTransition("startAuto", "human1").addTransition("startEvent", "human2").done();
      fail("An Receive task cannot have an expression and correlation keys");
    } catch (final BonitaRuntimeException e) {
    }
  }

  public void testTimerKillsDescription() throws Exception {
    final String processDescription = "This description does not appear with a timer in place (pool).";
    final ProcessDefinition processDefinition = ProcessBuilder.createProcess("Timer_Kills_Description_Test", "1.0")
    .addLabel("Timer Kills Description Test")
    .addDescription(processDescription)
    .addHuman(getLogin())
      .addLabel("Initiator")
      .addDescription("Person who takes the first action to start the process")
    .addSystemTask("Start1")
    .addHumanTask("ClickMe", getLogin())
      .addTimerBoundaryEvent("Timer1", "60000")
    .addSystemTask("NotFastEnough")
    .addSystemTask("TheEnd")
    .addTransition("NotFastEnough", "TheEnd")
    .addExceptionTransition("ClickMe", "Timer1", "NotFastEnough")
    .addTransition("Start1", "ClickMe")
    .addTransition("ClickMe", "TheEnd")
    .done();
    
    assertEquals(processDescription, processDefinition.getDescription());
    
    final BusinessArchive businessArchive = getBusinessArchive(processDefinition);
    assertEquals(processDescription, businessArchive.getProcessDefinition().getDescription());
    final ProcessDefinition definition = getManagementAPI().deploy(businessArchive);
    assertEquals(processDescription, definition.getDescription());
    
    getManagementAPI().deleteProcess(definition.getUUID());
  }

  public void testTimerKillsLabel() throws Exception {
    final String processDescription = "This description does not appear with a timer in place (pool).";
    final String processLabel = "Timer Kills Description Test";
    final ProcessDefinition processDefinition = ProcessBuilder.createProcess("Timer_Kills_Description_Test", "1.0")
    .addLabel(processLabel)
    .addDescription(processDescription)
    .addHuman(getLogin())
      .addLabel("Initiator")
      .addDescription("Person who takes the first action to start the process")
    .addSystemTask("Start1")
    .addHumanTask("ClickMe", getLogin())
      .addTimerBoundaryEvent("Timer1", "60000")
    .addSystemTask("NotFastEnough")
    .addSystemTask("TheEnd")
    .addTransition("NotFastEnough", "TheEnd")
    .addExceptionTransition("ClickMe", "Timer1", "NotFastEnough")
    .addTransition("Start1", "ClickMe")
    .addTransition("ClickMe", "TheEnd")
    .done();

    assertEquals(processLabel, processDefinition.getLabel());

    final BusinessArchive businessArchive = getBusinessArchive(processDefinition);
    assertEquals(processLabel, businessArchive.getProcessDefinition().getLabel());
    final ProcessDefinition definition = getManagementAPI().deploy(businessArchive);
    assertEquals(processLabel, definition.getLabel());

    getManagementAPI().deleteProcess(definition.getUUID());
  }

}
