/**
 * Copyright (C) 2007 Bull S. A. S. Bull, Rue Jean Jaures, B.P.68, 78340, Les
 * Clayes-sous-Bois This library is free software; you can redistribute it
 * and/or modify it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation version 2.1 of the License. This
 * library is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details. You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301,
 * USA.
 */
package org.ow2.bonita.variable;

import java.net.URL;
import java.util.Collection;
import java.util.Formatter;
import java.util.Set;

import org.bonitasoft.connectors.bonita.SetVarConnector;
import org.bonitasoft.connectors.bonita.resolvers.ProcessInitiatorRoleResolver;
import org.ow2.bonita.facade.def.element.HookDefinition.Event;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.runtime.ActivityInstance;
import org.ow2.bonita.facade.runtime.ActivityState;
import org.ow2.bonita.facade.runtime.TaskInstance;
import org.ow2.bonita.facade.uuid.ActivityInstanceUUID;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.util.BonitaException;
import org.ow2.bonita.util.BonitaRuntimeException;
import org.ow2.bonita.util.ProcessBuilder;

/**
 * Testing activity variables (string and enumeration types) by using StartMode=Manual (task behavior) taking in account
 * : propagation parameter, split, join for execution Variables are defined either into WFProcess or Package
 */
public class ActivityVariableTest extends VariableTestCase {

  public void testUnpersistableVariable() throws Exception {
    ProcessDefinition process = ProcessBuilder.createProcess("Unpersistable", "1.0")
        .addObjectData("var1", Formatter.class.getName(), "${new Formatter()}").addHuman(getLogin())
        .addHumanTask("step", getLogin()).done();

    process = getManagementAPI().deploy(getBusinessArchive(process));
    final ProcessDefinitionUUID processUUID = process.getUUID();
    try {
      getRuntimeAPI().instantiateProcess(processUUID);
      fail("The variable value is not Serializable");
    } catch (final BonitaRuntimeException e) {
      getManagementAPI().deleteProcess(processUUID);
    }
  }

  // check that activity variables (string and enumeration) defined
  // into WFProcess are :
  // - available within the execution pointing to node with the given activity Id
  // according the extended attribute
  // - propagated according the extended attribute

  public void testActivityVariablesIntoWFProcess() throws BonitaException {
    final URL xpdlUrl = ActivityVariableTest.class.getResource("varActivityIntoWFProcess.xpdl");
    final ProcessDefinition process = getManagementAPI().deploy(getBusinessArchiveFromXpdl(xpdlUrl));
    final ProcessDefinitionUUID processUUID = process.getUUID();
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    // Check variables within execution pointing to act1 node
    Collection<ActivityInstance> acts = getQueryRuntimeAPI().getActivityInstances(instanceUUID, "act1");
    assertEquals(1, acts.size());
    ActivityInstance activityInst = acts.iterator().next();
    assertNotNull(activityInst);

    assertTrue(getQueryRuntimeAPI().getActivityInstanceVariables(activityInst.getUUID()).isEmpty());
    checkStopped(instanceUUID, new String[] {});

    // start & terminate "act1" task
    executeTask(instanceUUID, "act1");
    checkStopped(instanceUUID, new String[] { "act1" });

    acts = getQueryRuntimeAPI().getActivityInstances(instanceUUID, "act2");
    assertEquals(1, acts.size());
    activityInst = acts.iterator().next();
    assertNotNull(activityInst);

    // Check variables within execution pointing to act2 node
    checkVariables(getQueryRuntimeAPI(), activityInst.getUUID(), 7);

    // start & terminate "act2" task
    executeTask(instanceUUID, "act2");
    // check variables within execution pointing to act3 node
    acts = getQueryRuntimeAPI().getActivityInstances(instanceUUID, "act3");
    assertEquals(1, acts.size());
    activityInst = acts.iterator().next();
    assertNotNull(activityInst);

    checkVariables(getQueryRuntimeAPI(), activityInst.getUUID(), 0);
    checkStopped(instanceUUID, new String[] { "act1", "act2" });

    // start & terminate "act3" task
    executeTask(instanceUUID, "act3");
    checkExecutedOnce(instanceUUID, new String[] { "act1", "act2", "act3" });

    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testSetVariableInHumanTaskAfterAutomaticActivity() throws BonitaException {
    // Secondly, when an automated task follows a manual task, we encounter weird behaviour. Even if the
    // previous activity was finished, when whe are in onEnter hook, set local variable is not possible.
    // Following code throws an activityinstancenotfound even if the activity id is the one of activity passed to hook
    // ...
    // AccessorUtil.getAPIAccessor().getRuntimeAPI().setVariable(activityInstance.getUUID(), "varKnownUser", false);
    ProcessDefinition process = ProcessBuilder.createProcess("p", "1.0").addHuman(getLogin())
        .addHumanTask("t1", getLogin()).addSystemTask("t2").addStringData("var", "initial")
        .addConnector(Event.automaticOnEnter, SetVarConnector.class.getName(), true)
        .addInputParameter("setVariableName", "var").addInputParameter("setValue", "newvalue")
        .addHumanTask("t3", getLogin()).addTransition("t1_t2", "t1", "t2").addTransition("t2_t3", "t2", "t3").done();

    process = getManagementAPI().deploy(getBusinessArchive(process, null, SetVarConnector.class));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    Collection<TaskInstance> tasks = getQueryRuntimeAPI().getTaskList(instanceUUID, ActivityState.READY);
    assertEquals(1, tasks.size());
    final ActivityInstanceUUID t1UUID = tasks.iterator().next().getUUID();
    getRuntimeAPI().startTask(t1UUID, true);
    getRuntimeAPI().finishTask(t1UUID, true);

    tasks = getQueryRuntimeAPI().getTaskList(instanceUUID, ActivityState.READY);
    assertEquals(1, tasks.size());

    final Collection<ActivityInstance> activities = getQueryRuntimeAPI().getActivityInstances(instanceUUID);
    ActivityInstanceUUID t2UUID = null;
    for (final ActivityInstance activity : activities) {
      if (activity.getActivityName().equals("t2")) {
        t2UUID = activity.getUUID();
        break;
      }
    }
    assertNotNull(t2UUID);
    assertEquals("newvalue", getQueryRuntimeAPI().getActivityInstanceVariable(t2UUID, "var"));

    getManagementAPI().deleteProcess(processUUID);
  }

  private ProcessDefinition getProcessWithTwoHumanTasksAndSameVariableName() {
    return ProcessBuilder.createProcess("vars", "1.0").addStringData("var", "polo").addGroup("Initiator")
        .addGroupResolver(ProcessInitiatorRoleResolver.class.getName()).addHumanTask("first", "Initiator")
        .addStringData("var", "pool").addHumanTask("second", "Initiator").addStringData("var", "loop")
        .addTransition("fs", "first", "second").done();
  }

  public void testTwoTasksSameVariableName() throws BonitaException {
    final ProcessDefinition process = getManagementAPI().deploy(
        getBusinessArchive(getProcessWithTwoHumanTasksAndSameVariableName(), null, ProcessInitiatorRoleResolver.class));
    final ProcessDefinitionUUID definitionUUID = process.getUUID();
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(definitionUUID);
    String var = (String) getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "var");
    assertEquals("polo", var);

    Set<ActivityInstance> activities = getQueryRuntimeAPI().getActivityInstances(instanceUUID, "first");
    ActivityInstance activity = activities.iterator().next();
    ActivityInstanceUUID activityUUID = activity.getUUID();
    var = (String) getQueryRuntimeAPI().getActivityInstanceVariable(activityUUID, "var");
    assertEquals("pool", var);
    executeTask(instanceUUID, "first");

    activities = getQueryRuntimeAPI().getActivityInstances(instanceUUID, "second");
    activity = activities.iterator().next();
    activityUUID = activity.getUUID();
    var = (String) getQueryRuntimeAPI().getActivityInstanceVariable(activityUUID, "var");
    assertEquals("loop", var);

    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().disable(definitionUUID);
    getManagementAPI().deleteProcess(definitionUUID);
  }

  public void testTwoTasksSameVariableNameAndSet() throws BonitaException {
    final ProcessDefinition process = getManagementAPI().deploy(
        getBusinessArchive(getProcessWithTwoHumanTasksAndSameVariableName(), null, ProcessInitiatorRoleResolver.class));
    final ProcessDefinitionUUID definitionUUID = process.getUUID();
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(definitionUUID);
    String var = (String) getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "var");
    assertEquals("polo", var);

    Set<ActivityInstance> activities = getQueryRuntimeAPI().getActivityInstances(instanceUUID, "first");
    ActivityInstance activity = activities.iterator().next();
    final ActivityInstanceUUID activityUUID = activity.getUUID();
    var = (String) getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "var");
    assertEquals("polo", var);
    var = (String) getQueryRuntimeAPI().getActivityInstanceVariable(activityUUID, "var");
    assertEquals("pool", var);
    getRuntimeAPI().setActivityInstanceVariable(activityUUID, "var", "coco");
    executeTask(instanceUUID, "first");

    activities = getQueryRuntimeAPI().getActivityInstances(instanceUUID, "second");
    activity = activities.iterator().next();
    final ActivityInstanceUUID activityUUID2 = activity.getUUID();
    var = (String) getQueryRuntimeAPI().getActivityInstanceVariable(activityUUID, "var");
    assertEquals("coco", var);
    var = (String) getQueryRuntimeAPI().getActivityInstanceVariable(activityUUID2, "var");
    assertEquals("loop", var);
    var = (String) getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "var");
    assertEquals("polo", var);

    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().disable(definitionUUID);
    getManagementAPI().deleteProcess(definitionUUID);
  }

  public void testTwoSystemTasksSameVariableNameAndSet() throws BonitaException {
    final ProcessDefinition definition = ProcessBuilder.createProcess("vars", "1.0").addStringData("var", "polo")
        .addSystemTask("first").addStringData("var", "pool")
        .addConnector(Event.automaticOnEnter, SetVarConnector.class.getName(), true)
        .addInputParameter("setVariableName", "var").addInputParameter("setValue", "hello").addSystemTask("second")
        .addStringData("var", "loop").addConnector(Event.automaticOnEnter, SetVarConnector.class.getName(), true)
        .addInputParameter("setVariableName", "var").addInputParameter("setValue", "ehlo")
        .addTransition("fs", "first", "second").done();

    final ProcessDefinition process = getManagementAPI().deploy(
        getBusinessArchive(definition, null, SetVarConnector.class));
    final ProcessDefinitionUUID definitionUUID = process.getUUID();
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(definitionUUID);
    String var = (String) getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "var");
    assertEquals("polo", var);

    Set<ActivityInstance> activities = getQueryRuntimeAPI().getActivityInstances(instanceUUID, "first");
    ActivityInstance activity = activities.iterator().next();
    final ActivityInstanceUUID activityUUID = activity.getUUID();
    activities = getQueryRuntimeAPI().getActivityInstances(instanceUUID, "second");
    activity = activities.iterator().next();
    final ActivityInstanceUUID activityUUID2 = activity.getUUID();

    var = (String) getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "var");
    assertEquals("polo", var);
    var = (String) getQueryRuntimeAPI().getActivityInstanceVariable(activityUUID, "var");
    assertEquals("hello", var);
    var = (String) getQueryRuntimeAPI().getActivityInstanceVariable(activityUUID2, "var");
    assertEquals("ehlo", var);

    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().disable(definitionUUID);
    getManagementAPI().deleteProcess(definitionUUID);
  }

  public void testSameSystemTaskName() throws BonitaException {
    try {
      ProcessBuilder.createProcess("vars", "1.0").addStringData("var", "polo").addSystemTask("first")
          .addSystemTask("first").done();
      fail("Two activities cannot get the same name!");
    } catch (final BonitaRuntimeException e) {
      // ok
    }
  }

  public void testSameHumanTaskName() throws BonitaException {
    try {
      ProcessBuilder.createProcess("vars", "1.0").addStringData("var", "polo").addHumanTask("first")
          .addHumanTask("first").done();
      fail("Two activities cannot get the same name!");
    } catch (final BonitaRuntimeException e) {
      // ok
    }
  }

  public void testSameTaskName() throws BonitaException {
    try {
      ProcessBuilder.createProcess("vars", "1.0").addStringData("var", "polo").addSystemTask("first")
          .addHumanTask("first").done();
      fail("Two activities cannot get the same name!");
    } catch (final BonitaRuntimeException e) {
      // ok
    }
  }

  public void testDecisionNodeName() throws BonitaException {
    try {
      ProcessBuilder.createProcess("vars", "1.0").addStringData("var", "polo").addDecisionNode("first")
          .addDecisionNode("first").done();
      fail("Two activities cannot get the same name!");
    } catch (final BonitaRuntimeException e) {
      // ok
    }
  }

  public void testDecisionNodeAndTaskName() throws BonitaException {
    try {
      ProcessBuilder.createProcess("vars", "1.0").addStringData("var", "polo").addDecisionNode("first")
          .addSystemTask("first").done();
      fail("Two activities cannot get the same name!");
    } catch (final BonitaRuntimeException e) {
      // ok
    }
  }

  public void testSubProcessName() throws BonitaException {
    try {
      ProcessBuilder.createProcess("vars", "1.0").addStringData("var", "polo").addSubProcess("first", "one")
          .addSubProcess("first", "one").done();
      fail("Two activities cannot get the same name!");
    } catch (final BonitaRuntimeException e) {
      // ok
    }
  }

  public void testSubProcessAndTaskName() throws BonitaException {
    try {
      ProcessBuilder.createProcess("vars", "1.0").addStringData("var", "polo").addSubProcess("first", "one")
          .addSystemTask("first").done();
      fail("Two activities cannot get the same name!");
    } catch (final BonitaRuntimeException e) {
      // ok
    }
  }

  public void testGroovyVariable() throws Exception {
    ProcessDefinition process = ProcessBuilder.createProcess("groovy", null).addStringData("groovy", "${'hello'}")
        .addStringDataFromScript("groove", "${'groovy'}").addHuman("john").addHumanTask("step", "john")
        .addStringData("activity", "${'hello'}").addStringDataFromScript("act", "${'groovy'}").done();

    process = getManagementAPI().deploy(getBusinessArchive(process));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);
    String actual = (String) getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "groovy");
    assertEquals("${'hello'}", actual);
    actual = (String) getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "groove");
    assertEquals("groovy", actual);
    loginAs("john", "bpm");
    final Set<TaskInstance> tasks = getQueryRuntimeAPI().getTasks(instanceUUID);
    final ActivityInstanceUUID taskUUID = tasks.iterator().next().getUUID();
    actual = (String) getQueryRuntimeAPI().getActivityInstanceVariable(taskUUID, "activity");
    assertEquals("${'hello'}", actual);
    actual = (String) getQueryRuntimeAPI().getActivityInstanceVariable(taskUUID, "act");
    assertEquals("groovy", actual);

    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

}
