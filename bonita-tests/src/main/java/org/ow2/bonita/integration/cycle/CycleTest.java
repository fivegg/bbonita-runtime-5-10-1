/**
 * Copyright (C) 2007  Bull S. A. S.
 * Bull, Rue Jean Jaures, B.P.68, 78340, Les Clayes-sous-Bois
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA  02110-1301, USA.
 **/
package org.ow2.bonita.integration.cycle;

import java.net.URL;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import org.ow2.bonita.APITestCase;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.exception.DeploymentException;
import org.ow2.bonita.facade.runtime.ActivityState;
import org.ow2.bonita.facade.runtime.InstanceState;
import org.ow2.bonita.facade.runtime.TaskInstance;
import org.ow2.bonita.facade.uuid.ActivityInstanceUUID;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.light.LightActivityInstance;
import org.ow2.bonita.light.LightTaskInstance;
import org.ow2.bonita.util.BonitaException;
import org.ow2.bonita.util.BonitaRuntimeException;
import org.ow2.bonita.util.ProcessBuilder;

/**
 * Test. Deploys a xpdl process.
 *
 * @author Miguel Valdes
 */
public class CycleTest extends APITestCase {
  
  //http://www.bonitasoft.org/bugs/view.php?id=1903
  public void testManyCycles() throws Exception {
    ProcessDefinition definition = ProcessBuilder.createProcess("manyCycle", "3.2")
      .addHuman(getLogin())
      .addSystemTask("start")
      .addHumanTask("step1", getLogin())
        .addBooleanData("data", true)
      .addHumanTask("step2", getLogin())
        .addBooleanData("data", true)
      .addHumanTask("step3", getLogin())
      .addHumanTask("step4", getLogin())
      .addHumanTask("step5", getLogin())
        .addBooleanData("data", true)
      .addSystemTask("end")
      
      .addTransition("start", "step1")
      .addTransition("step1", "step2")
        .addCondition("!data")
      .addTransition("step1", "step4")
        .addCondition("data")
      .addTransition("step2", "step1")
        .addCondition("!data")
      .addTransition("step2", "step3")
        .addCondition("data")
      .addTransition("step3", "step4")
      .addTransition("step4", "step5")
      .addTransition("step5", "end")
        .addCondition("data")
      .addTransition("step5", "step1")
        .addCondition("!data")
      .done();

    final ProcessDefinition process = getManagementAPI().deploy(getBusinessArchive(definition));
    final ProcessDefinitionUUID processUUID = process.getUUID();
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    setVariable(instanceUUID, "step1", "data", false);
    executeTask(instanceUUID, "step1");

    setVariable(instanceUUID, "step2", "data", true);
    executeTask(instanceUUID, "step2");
    
    executeTask(instanceUUID, "step3");
    
    executeTask(instanceUUID, "step4");
    
    setVariable(instanceUUID, "step5", "data", false);
    executeTask(instanceUUID, "step5");
    
    assertEquals(InstanceState.STARTED, getQueryRuntimeAPI().getLightProcessInstance(instanceUUID).getInstanceState());

    //checks step1 is in the task list
    Collection<LightTaskInstance> tasks = getQueryRuntimeAPI().getLightTaskList(instanceUUID, ActivityState.READY);
    assertEquals(1, tasks.size());
    LightTaskInstance step1 = tasks.iterator().next();
    assertEquals("step1", step1.getActivityName());
    
    setVariable(instanceUUID, "step1", "data", false);
    executeTask(instanceUUID, "step1");

    setVariable(instanceUUID, "step2", "data", true);
    executeTask(instanceUUID, "step2");
    
    executeTask(instanceUUID, "step3");
    
    executeTask(instanceUUID, "step4");
    
    setVariable(instanceUUID, "step5", "data", false);
    executeTask(instanceUUID, "step5");
    
    assertEquals(InstanceState.STARTED, getQueryRuntimeAPI().getLightProcessInstance(instanceUUID).getInstanceState());
    //do it twice to ensure many iterations can be done
    
    //checks step1 is in the task list
    tasks = getQueryRuntimeAPI().getLightTaskList(instanceUUID, ActivityState.READY);
    assertEquals(1, tasks.size());
    step1 = tasks.iterator().next();
    assertEquals("step1", step1.getActivityName());
    
    setVariable(instanceUUID, "step1", "data", true);
    executeTask(instanceUUID, "step1");
    
    executeTask(instanceUUID, "step4");
    
    setVariable(instanceUUID, "step5", "data", true);
    executeTask(instanceUUID, "step5");
    
    assertEquals(InstanceState.FINISHED, getQueryRuntimeAPI().getLightProcessInstance(instanceUUID).getInstanceState());
    
    this.getManagementAPI().deleteProcess(processUUID);
  }
  
  private void setVariable(final ProcessInstanceUUID instanceUUID, final String activityName, final String variableName, final Object variableValue) throws Exception {
    ActivityInstanceUUID activityUUID = null;
    final Set<LightActivityInstance> activities = getQueryRuntimeAPI().getLightActivityInstances(instanceUUID, activityName);
    for (LightActivityInstance activity : activities) {
      if (activity.getState().equals(ActivityState.READY)) {
        activityUUID = activity.getUUID();
        break;
      }
    }
    getRuntimeAPI().setActivityInstanceVariable(activityUUID, variableName, variableValue);
  }
  
  public void testSameIterationIdInACycle() throws Exception {
    ProcessDefinition definition = ProcessBuilder.createProcess("simpleCycle", "3.2")
      .addBooleanData("validate", false)
      .addHuman(getLogin())
      .addSystemTask("start")
      .addHumanTask("step1", getLogin())
      .addHumanTask("step2", getLogin())
      .addHumanTask("stop", getLogin())
      .addTransition("start", "step1")
      .addTransition("step1", "step2")
      .addTransition("step2", "step1")
        .addCondition("!validate")
      .addTransition("step2", "stop")
        .addCondition("validate")
    .done();

    final ProcessDefinition process = getManagementAPI().deploy(getBusinessArchive(definition));
    final ProcessDefinitionUUID processUUID = process.getUUID();
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    Collection<TaskInstance> tasks = getQueryRuntimeAPI().getTaskList(instanceUUID, ActivityState.READY);
    assertEquals(1, tasks.size());
    TaskInstance task = tasks.iterator().next();
    assertEquals("step1", task.getActivityName());
    String firstIterationId = task.getIterationId();
    executeTask(instanceUUID, "step1");
 
    tasks = getQueryRuntimeAPI().getTaskList(instanceUUID,ActivityState.READY);
    assertEquals(1, tasks.size());
    task = tasks.iterator().next();
    assertEquals("step2", task.getActivityName());
    assertEquals(firstIterationId, task.getIterationId());
    executeTask(instanceUUID, "step2");

    tasks = getQueryRuntimeAPI().getTaskList(instanceUUID,ActivityState.READY);
    assertEquals(1, tasks.size());
    task = tasks.iterator().next();
    assertEquals("step1", task.getActivityName());
    assertNotSame(firstIterationId, task.getIterationId());

    this.getManagementAPI().deleteProcess(processUUID);
  }

  public void testSameIterationIdInAHugeCycle() throws Exception {
    ProcessDefinition definition = ProcessBuilder.createProcess("simpleCycle", "3.2")
      .addBooleanData("validate", false)
      .addHuman(getLogin())
      .addSystemTask("start")
      .addHumanTask("step1", getLogin())
      .addHumanTask("step2", getLogin())
      .addHumanTask("step3", getLogin())
      .addHumanTask("stop", getLogin())
      .addTransition("start", "step1")
      .addTransition("step1", "step2")
      .addTransition("step2", "step3")
      .addTransition("step3", "step1")
        .addCondition("!validate")
      .addTransition("step3", "stop")
        .addCondition("validate")
    .done();

    final ProcessDefinition process = getManagementAPI().deploy(getBusinessArchive(definition));
    final ProcessDefinitionUUID processUUID = process.getUUID();
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    Collection<TaskInstance> tasks = getQueryRuntimeAPI().getTaskList(instanceUUID, ActivityState.READY);
    assertEquals(1, tasks.size());
    TaskInstance task = tasks.iterator().next();
    assertEquals("step1", task.getActivityName());
    String firstIterationId = task.getIterationId();
    executeTask(instanceUUID, "step1");
 
    tasks = getQueryRuntimeAPI().getTaskList(instanceUUID,ActivityState.READY);
    assertEquals(1, tasks.size());
    task = tasks.iterator().next();
    assertEquals("step2", task.getActivityName());
    assertEquals(firstIterationId, task.getIterationId());
    executeTask(instanceUUID, "step2");
    
    tasks = getQueryRuntimeAPI().getTaskList(instanceUUID,ActivityState.READY);
    assertEquals(1, tasks.size());
    task = tasks.iterator().next();
    assertEquals("step3", task.getActivityName());
    assertEquals(firstIterationId, task.getIterationId());
    executeTask(instanceUUID, "step3");

    tasks = getQueryRuntimeAPI().getTaskList(instanceUUID,ActivityState.READY);
    assertEquals(1, tasks.size());
    task = tasks.iterator().next();
    assertEquals("step1", task.getActivityName());
    String secondIteration = task.getIterationId();
    assertNotSame(firstIterationId, secondIteration);
    executeTask(instanceUUID, "step1");
    
    tasks = getQueryRuntimeAPI().getTaskList(instanceUUID,ActivityState.READY);
    assertEquals(1, tasks.size());
    task = tasks.iterator().next();
    assertEquals("step2", task.getActivityName());
    assertEquals(secondIteration, task.getIterationId());

    this.getManagementAPI().deleteProcess(processUUID);
  }

  //http://www.bonitasoft.org/bugs/view.php?id=1248
  public void testBadCancellation() throws Exception {
    ProcessDefinition process = ProcessBuilder.createProcess("p", "1.0")
    .addHuman(getLogin())
    .addSystemTask("initial")
    .addHumanTask("t1", getLogin())
    .addHumanTask("t2", getLogin())
    .addTransition("initial", "t1")
    .addTransition("initial", "t2")
    .addTransition("t1", "t1")
    .addTransition("t2", "t2")
    .done();
    
    process = getManagementAPI().deploy(getBusinessArchive(process));
    final ProcessDefinitionUUID processUUID = process.getUUID();
    
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);
    
    Collection<TaskInstance> tasks = null;
    TaskInstance t1 = null;
    TaskInstance t2 = null;
    
    //check that when a cycle on t1 ends an iteration, the other branch over t2 is not canceled
    tasks = getQueryRuntimeAPI().getTaskList(ActivityState.READY);
    assertEquals(2, tasks.size());
    assertEquals(3, getQueryRuntimeAPI().getActivityInstances(instanceUUID).size());
    
    t1 = getTask(tasks, "t1");
    t2 = getTask(tasks, "t2");
    
    assertEquals(ActivityState.READY, t1.getState());
    assertEquals(ActivityState.READY, t2.getState());
    
    getRuntimeAPI().executeTask(t1.getUUID(), true);
    
    tasks = getQueryRuntimeAPI().getTaskList(ActivityState.READY);
    assertEquals(tasks.toString(), 2, tasks.size());
    assertEquals(4, getQueryRuntimeAPI().getActivityInstances(instanceUUID).size());
    
    t1 = getTask(tasks, "t1");
    t2 = getTask(tasks, "t2");
    
    assertEquals(ActivityState.READY, t1.getState());
    assertEquals(ActivityState.READY, t2.getState());
    
    assertEquals("Instance must not be finished.", InstanceState.STARTED, getQueryRuntimeAPI().getProcessInstance(instanceUUID).getInstanceState());
    
    getManagementAPI().deleteProcess(processUUID);
  }
  
  private TaskInstance getTask(final Collection<TaskInstance> tasks, final String taskName) {
   final Iterator<TaskInstance> it = tasks.iterator();
   TaskInstance task = null;
   while (task == null && it.hasNext()) {
     final TaskInstance temp = it.next();
     if (temp.getActivityName().equals(taskName)) {
       task = temp;
     }
   }
   assertNotNull(task);
   return task;
  }
  
  public void testBadIteration() {
    final URL xpdlUrl = this.getClass().getResource("badIteration.xpdl");
    try {
      this.getManagementAPI().deploy(getBusinessArchiveFromXpdl(xpdlUrl));
      fail("Expected DeploymentException");
    } catch (final Exception e) {
      // ok
    }

  }
  
  /*
   * Test a simple cycle.
   * Correct execution is: init a1 a2 a4 a1 a2 end
   */
  public void testSimpleCycle() throws BonitaException {
    final URL xpdlUrl = this.getClass().getResource("simpleCycle.xpdl");
    final ProcessDefinition process = this.getManagementAPI().deploy(getBusinessArchiveFromXpdl(xpdlUrl,
        HookBeforeTerminateUpdateVariable.class));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    final ProcessInstanceUUID instanceUUID = this.getRuntimeAPI().instantiateProcess(processUUID);
    this.checkExecutedOnce(instanceUUID, new String[]{"init", "a4"});
    assertEquals(2, this.getQueryRuntimeAPI().getActivityInstances(instanceUUID, "a1").size());
    assertEquals(2, this.getQueryRuntimeAPI().getActivityInstances(instanceUUID, "a2").size());

    this.getRuntimeAPI().deleteProcessInstance(instanceUUID);
    this.getManagementAPI().disable(processUUID);
    this.getManagementAPI().deleteProcess(processUUID);
  }


  /*
   * Test a simple proEd cycle.
   * Correct execution is: init a1 a2 a4 a1 a2 end
   */
  public void testSimpleCycleProEd() throws BonitaException {
    final URL xpdlUrl = this.getClass().getResource("simpleCycleProEd.xpdl");
    final ProcessDefinition process = this.getManagementAPI().deploy(getBusinessArchiveFromXpdl(xpdlUrl,
        HookBeforeTerminateUpdateVariable.class));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    final ProcessInstanceUUID instanceUUID = this.getRuntimeAPI().instantiateProcess(processUUID);
    this.checkStopped(instanceUUID, new String[]{"init", "a1"});

    Collection<TaskInstance> tasks = this.getQueryRuntimeAPI().getTaskList(instanceUUID, ActivityState.READY);
    assertEquals(1, tasks.size());

    TaskInstance task = tasks.iterator().next();
    this.getRuntimeAPI().startTask(task.getUUID(), true);
    this.getRuntimeAPI().finishTask(task.getUUID(), true);

    this.checkStopped(instanceUUID, new String[]{"init", "a2"});
    assertEquals(2, this.getQueryRuntimeAPI().getActivityInstances(instanceUUID, "a1").size());
    assertEquals(2, this.getQueryRuntimeAPI().getActivityInstances(instanceUUID, "a4").size());

    tasks = this.getQueryRuntimeAPI().getTaskList(instanceUUID, ActivityState.READY);
    assertEquals(1, tasks.size());

    task = tasks.iterator().next();
    this.getRuntimeAPI().startTask(task.getUUID(), true);
    this.getRuntimeAPI().setProcessInstanceVariable(instanceUUID, "cont", "0");
    this.getRuntimeAPI().finishTask(task.getUUID(), true);

    this.checkExecutedOnce(instanceUUID, new String[]{"init", "BonitaEnd"});
    assertEquals(2, this.getQueryRuntimeAPI().getActivityInstances(instanceUUID, "a1").size());
    assertEquals(2, this.getQueryRuntimeAPI().getActivityInstances(instanceUUID, "a4").size());
    assertEquals(2, this.getQueryRuntimeAPI().getActivityInstances(instanceUUID, "a2").size());

    this.getRuntimeAPI().deleteProcessInstance(instanceUUID);
    this.getManagementAPI().disable(processUUID);
    this.getManagementAPI().deleteProcess(processUUID);
  }

  /*
   * Test a nested cycle.
   * Correct execution is: init a1 a3 a2 a4 a3 a2 a6 a5 a1 a3 a2 a6 end
   */
  public void testNestedCycle() throws BonitaException {
    final URL xpdlUrl = this.getClass().getResource("nestedCycle.xpdl");
    final ProcessDefinition process = this.getManagementAPI().deploy(getBusinessArchiveFromXpdl(xpdlUrl,
        HookBeforeTerminateUpdateVariable.class, HookBeforeTerminateUpdateVariable2.class));
    final ProcessDefinitionUUID processUUID = process.getUUID();


    final ProcessInstanceUUID instanceUUID = this.getRuntimeAPI().instantiateProcess(processUUID);
    this.checkExecutedOnce(instanceUUID, new String[]{"init", "a4", "a5", "BonitaEnd"});
    assertEquals(2, this.getQueryRuntimeAPI().getActivityInstances(instanceUUID, "a1").size());
    assertEquals(3, this.getQueryRuntimeAPI().getActivityInstances(instanceUUID, "a2").size());
    assertEquals(3, this.getQueryRuntimeAPI().getActivityInstances(instanceUUID, "a3").size());
    assertEquals(2, this.getQueryRuntimeAPI().getActivityInstances(instanceUUID, "a6").size());

    this.getRuntimeAPI().deleteProcessInstance(instanceUUID);
    this.getManagementAPI().disable(processUUID);
    this.getManagementAPI().deleteProcess(processUUID);
  }


  /*
   * Test an arbitrary cycle.
   * Correct execution is: init a1 a3 a2 a4 a1 a3 a2 a6 a5 a3 a2 a6 end
   */
  public void testArbitraryCycle() throws BonitaException {
    final URL xpdlUrl = this.getClass().getResource("arbitraryCycle.xpdl");
    final ProcessDefinition process = this.getManagementAPI().deploy(getBusinessArchiveFromXpdl(xpdlUrl,
        HookBeforeTerminateUpdateVariable.class, HookBeforeTerminateUpdateVariable2.class));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    final ProcessInstanceUUID instanceUUID = this.getRuntimeAPI().instantiateProcess(processUUID);
    this.checkExecutedOnce(instanceUUID, new String[]{"init", "a4", "a5", "BonitaEnd"});
    assertEquals(2, this.getQueryRuntimeAPI().getActivityInstances(instanceUUID, "a1").size());
    assertEquals(3, this.getQueryRuntimeAPI().getActivityInstances(instanceUUID, "a2").size());
    assertEquals(3, this.getQueryRuntimeAPI().getActivityInstances(instanceUUID, "a3").size());
    assertEquals(2, this.getQueryRuntimeAPI().getActivityInstances(instanceUUID, "a6").size());

    this.getRuntimeAPI().deleteProcessInstance(instanceUUID);
    this.getManagementAPI().disable(processUUID);
    this.getManagementAPI().deleteProcess(processUUID);
  }

  /*
   * Test a simple proEd cycle with an extra entry point.
   * Entry point with AND join is not allowed
   * Correct execution is: init a3  a1 a2 a4 a1 a2 end
   */
  public void testEntryAndJoinProEd() throws BonitaException {
    final URL xpdlUrl = this.getClass().getResource("entryAndCycle.xpdl");
    try {
      this.getManagementAPI().deploy(getBusinessArchiveFromXpdl(xpdlUrl,
        HookBeforeTerminateUpdateVariable.class));
      fail("iteration with AND join is not allowed");
    } catch (final DeploymentException e) {
      assertTrue(e.getMessage().contains("AND join"));
    }
  }

  /*
   * Test a simple proEd cycle with an extra entry point.
   * Correct execution is: init a0/a3  a2 a1 a4 a2 a1 a4 a2 end
   */
  public void testEntryXorJoinProEd() throws BonitaException {
    final URL xpdlUrl = this.getClass().getResource("entryXorCycle.xpdl");
    final ProcessDefinition process = this.getManagementAPI().deploy(getBusinessArchiveFromXpdl(xpdlUrl,
        HookBeforeTerminateUpdateVariable.class));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    ProcessInstanceUUID instanceUUID = this.getRuntimeAPI().instantiateProcess(processUUID);
    this.checkStopped(instanceUUID, new String[]{"init"});

    // Check there are two tasks. Execute task from a3
    Collection<TaskInstance> tasks = this.getQueryRuntimeAPI().getTaskList(instanceUUID, ActivityState.READY);
    assertEquals(2, tasks.size());
    Iterator<TaskInstance> it = tasks.iterator();
    TaskInstance act = it.next();
    if ("a0".equals(act.getActivityName())) {
      act = it.next();
    }
    assertEquals("a3",act.getActivityName());
    TaskInstance task = act;
    this.getRuntimeAPI().startTask(task.getUUID(), true);
    this.getRuntimeAPI().finishTask(task.getUUID(), true);

    this.checkStopped(instanceUUID, new String[]{"init", "a3", "a2", "a1"});

    // Tasks from a4 and from a0 has been created, task from a0 has been removed
    tasks = this.getQueryRuntimeAPI().getTaskList(instanceUUID, ActivityState.READY);
    assertEquals(1, tasks.size());
    act = tasks.iterator().next();
    assertEquals("a4",act.getActivityName());
    task = act;
    this.getRuntimeAPI().startTask(task.getUUID(), true);
    this.getRuntimeAPI().finishTask(task.getUUID(), true);

    assertEquals(2, this.getQueryRuntimeAPI().getActivityInstances(instanceUUID, "a2").size());
    assertEquals(2, this.getQueryRuntimeAPI().getActivityInstances(instanceUUID, "a1").size());
    assertEquals(2, this.getQueryRuntimeAPI().getActivityInstances(instanceUUID, "a4").size());

    tasks = this.getQueryRuntimeAPI().getTaskList(instanceUUID, ActivityState.READY);
    assertEquals(1, tasks.size());

    act = tasks.iterator().next();
    assertEquals("a4",act.getActivityName());
    task = act;
    this.getRuntimeAPI().startTask(task.getUUID(), true);
    this.getRuntimeAPI().setProcessInstanceVariable(instanceUUID, "cont", "0");
    this.getRuntimeAPI().finishTask(task.getUUID(), true);

    this.checkExecutedOnce(instanceUUID, new String[]{"init", "BonitaEnd"});
    assertEquals(2, this.getQueryRuntimeAPI().getActivityInstances(instanceUUID, "a1").size());
    assertEquals(2, this.getQueryRuntimeAPI().getActivityInstances(instanceUUID, "a4").size());
    assertEquals(3, this.getQueryRuntimeAPI().getActivityInstances(instanceUUID, "a2").size());

    this.getRuntimeAPI().deleteProcessInstance(instanceUUID);

    ///////////////////////////////////////////
    // Check same test when entering from a0 //
    ///////////////////////////////////////////
    instanceUUID = this.getRuntimeAPI().instantiateProcess(processUUID);
    this.checkStopped(instanceUUID, new String[]{"init"});

    // Check there are two tasks. Execute task from a3
    tasks = this.getQueryRuntimeAPI().getTaskList(instanceUUID, ActivityState.READY);
    assertEquals(2, tasks.size());
    it = tasks.iterator();
    act = it.next();
    if ("a3".equals(act.getActivityName())) {
      act = it.next();
    }
    assertEquals("a0",act.getActivityName());
    task = act;
    this.getRuntimeAPI().startTask(task.getUUID(), true);
    this.getRuntimeAPI().finishTask(task.getUUID(), true);

    this.checkStopped(instanceUUID, new String[]{"init", "a0", "a1"});

    // Tasks from a4 has been created
    tasks = this.getQueryRuntimeAPI().getTaskList(instanceUUID, ActivityState.READY);
    assertEquals(1, tasks.size());
    act = tasks.iterator().next();
    assertEquals("a4",act.getActivityName());
    task = act;
    this.getRuntimeAPI().startTask(task.getUUID(), true);
    this.getRuntimeAPI().finishTask(task.getUUID(), true);

    assertEquals(1, this.getQueryRuntimeAPI().getActivityInstances(instanceUUID, "a2").size());
    assertEquals(2, this.getQueryRuntimeAPI().getActivityInstances(instanceUUID, "a1").size());
    assertEquals(2, this.getQueryRuntimeAPI().getActivityInstances(instanceUUID, "a4").size());

    tasks = this.getQueryRuntimeAPI().getTaskList(instanceUUID, ActivityState.READY);
    assertEquals(1, tasks.size());

    act = tasks.iterator().next();
    assertEquals("a4",act.getActivityName());
    task = act;
    this.getRuntimeAPI().startTask(task.getUUID(), true);
    this.getRuntimeAPI().setProcessInstanceVariable(instanceUUID, "cont", "0");
    this.getRuntimeAPI().finishTask(task.getUUID(), true);

    this.checkExecutedOnce(instanceUUID, new String[]{"init", "BonitaEnd"});
    assertEquals(2, this.getQueryRuntimeAPI().getActivityInstances(instanceUUID, "a1").size());
    assertEquals(2, this.getQueryRuntimeAPI().getActivityInstances(instanceUUID, "a4").size());
    assertEquals(2, this.getQueryRuntimeAPI().getActivityInstances(instanceUUID, "a2").size());

    this.getRuntimeAPI().deleteProcessInstance(instanceUUID);


    this.getManagementAPI().disable(processUUID);
    this.getManagementAPI().deleteProcess(processUUID);
  }

  /*
   * Test a nested cycle with all nodes in the super cycle.
   * Correct execution is: init step0 step1 step2 step1 step2 step3 step0 step1 step2 step3 end
   */
  public void testNestedCycleNoExtraNodes() throws BonitaException {
    final URL xpdlUrl = this.getClass().getResource("testNestedCycleNoExtraNodes.xpdl");
    final ProcessDefinition process = this.getManagementAPI().deploy(getBusinessArchiveFromXpdl(xpdlUrl));
    final ProcessDefinitionUUID processUUID = process.getUUID();


    final ProcessInstanceUUID instanceUUID = this.getRuntimeAPI().instantiateProcess(processUUID);

    Collection<TaskInstance> tasks = this.getQueryRuntimeAPI().getTaskList(instanceUUID, ActivityState.READY);
    assertEquals(1, tasks.size());
    TaskInstance task = tasks.iterator().next();
    assertEquals("init", task.getActivityName());
    ActivityInstanceUUID taskUUID = task.getUUID();
    this.getRuntimeAPI().startTask(taskUUID, true);
    this.getRuntimeAPI().finishTask(taskUUID, true);


    tasks = this.getQueryRuntimeAPI().getTaskList(instanceUUID, ActivityState.READY);
    assertEquals(1, tasks.size());
    task = tasks.iterator().next();
    assertEquals("step0", task.getActivityName());
    taskUUID = task.getUUID();
    this.getRuntimeAPI().startTask(taskUUID, true);
    this.getRuntimeAPI().finishTask(taskUUID, true);

    tasks = this.getQueryRuntimeAPI().getTaskList(instanceUUID, ActivityState.READY);
    assertEquals(1, tasks.size());
    task = tasks.iterator().next();
    assertEquals("step1", task.getActivityName());
    taskUUID = task.getUUID();
    this.getRuntimeAPI().startTask(taskUUID, true);
    this.getRuntimeAPI().finishTask(taskUUID, true);

    tasks = this.getQueryRuntimeAPI().getTaskList(instanceUUID, ActivityState.READY);
    assertEquals(1, tasks.size());
    task = tasks.iterator().next();
    assertEquals("step2", task.getActivityName());
    taskUUID = task.getUUID();
    this.getRuntimeAPI().startTask(taskUUID, true);
    this.getRuntimeAPI().finishTask(taskUUID, true);

    tasks = this.getQueryRuntimeAPI().getTaskList(instanceUUID, ActivityState.READY);
    assertEquals(1, tasks.size());
    task = tasks.iterator().next();
    assertEquals("step1", task.getActivityName());
    taskUUID = task.getUUID();
    this.getRuntimeAPI().setProcessInstanceVariable(instanceUUID, "loop1", "yes");
    this.getRuntimeAPI().startTask(taskUUID, true);
    this.getRuntimeAPI().finishTask(taskUUID, true);

    tasks = this.getQueryRuntimeAPI().getTaskList(instanceUUID, ActivityState.READY);
    assertEquals(1, tasks.size());
    task = tasks.iterator().next();
    assertEquals("step2", task.getActivityName());
    taskUUID = task.getUUID();
    this.getRuntimeAPI().startTask(taskUUID, true);
    this.getRuntimeAPI().finishTask(taskUUID, true);

    tasks = this.getQueryRuntimeAPI().getTaskList(instanceUUID, ActivityState.READY);
    assertEquals(1, tasks.size());
    task = tasks.iterator().next();
    assertEquals("step3", task.getActivityName());
    taskUUID = task.getUUID();
    this.getRuntimeAPI().startTask(taskUUID, true);
    this.getRuntimeAPI().finishTask(taskUUID, true);

    tasks = this.getQueryRuntimeAPI().getTaskList(instanceUUID, ActivityState.READY);
    assertEquals(1, tasks.size());
    task = tasks.iterator().next();
    assertEquals("step0", task.getActivityName());
    this.getRuntimeAPI().setProcessInstanceVariable(instanceUUID, "loop2", "yes");
    taskUUID = task.getUUID();
    this.getRuntimeAPI().startTask(taskUUID, true);
    this.getRuntimeAPI().finishTask(taskUUID, true);

    tasks = this.getQueryRuntimeAPI().getTaskList(instanceUUID, ActivityState.READY);
    assertEquals(1, tasks.size());
    task = tasks.iterator().next();
    assertEquals("step1", task.getActivityName());
    taskUUID = task.getUUID();
    this.getRuntimeAPI().startTask(taskUUID, true);
    this.getRuntimeAPI().finishTask(taskUUID, true);

    tasks = this.getQueryRuntimeAPI().getTaskList(instanceUUID, ActivityState.READY);
    assertEquals(1, tasks.size());
    task = tasks.iterator().next();
    assertEquals("step2", task.getActivityName());
    taskUUID = task.getUUID();
    this.getRuntimeAPI().startTask(taskUUID, true);
    this.getRuntimeAPI().finishTask(taskUUID, true);

    tasks = this.getQueryRuntimeAPI().getTaskList(instanceUUID, ActivityState.READY);
    assertEquals(1, tasks.size());
    task = tasks.iterator().next();
    assertEquals("step3", task.getActivityName());
    taskUUID = task.getUUID();
    this.getRuntimeAPI().startTask(taskUUID, true);
    this.getRuntimeAPI().finishTask(taskUUID, true);

    assertNotNull(this.getQueryRuntimeAPI().getProcessInstance(instanceUUID).getEndedDate());
    this.getRuntimeAPI().deleteProcessInstance(instanceUUID);
    this.getManagementAPI().disable(processUUID);
    this.getManagementAPI().deleteProcess(processUUID);
  }

}
