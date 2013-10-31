package org.ow2.bonita.activity.instantiation;

import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import junit.framework.Assert;

import org.ow2.bonita.APITestCase;
import org.ow2.bonita.activity.instantiation.instantiator.BadJoinNActivityInstantiator;
import org.ow2.bonita.activity.instantiation.instantiator.BadVarActivityInstantiator;
import org.ow2.bonita.activity.instantiation.instantiator.HookActivityInstantiator;
import org.ow2.bonita.activity.instantiation.instantiator.JoinNumberConditionFail;
import org.ow2.bonita.activity.instantiation.instantiator.JoinNumberConditionPass;
import org.ow2.bonita.activity.instantiation.instantiator.NullActivityInstantiator;
import org.ow2.bonita.activity.instantiation.instantiator.TestActivityInstantiator;
import org.ow2.bonita.activity.instantiation.instantiator.TestOneJoinActivityInstantiator;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.exception.ActivityNotFoundException;
import org.ow2.bonita.facade.exception.MultiInstantiatorInvocationException;
import org.ow2.bonita.facade.runtime.ActivityInstance;
import org.ow2.bonita.facade.runtime.ActivityState;
import org.ow2.bonita.facade.runtime.InstanceState;
import org.ow2.bonita.facade.runtime.ProcessInstance;
import org.ow2.bonita.facade.runtime.TaskInstance;
import org.ow2.bonita.facade.uuid.ActivityInstanceUUID;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.light.LightActivityInstance;
import org.ow2.bonita.util.BonitaException;
import org.ow2.bonita.util.BonitaRuntimeException;

public class MultiInstantiationTest extends APITestCase {

  public void testMultiInstantiation() throws BonitaException {
    final URL xpdlUrl = this.getClass().getResource("multiInstantiation.xpdl");

    final ProcessDefinition process = this.getManagementAPI().deploy(this.getBusinessArchiveFromXpdl(xpdlUrl,
      TestActivityInstantiator.class));
    final ProcessDefinitionUUID processUUID = process.getUUID();
    final ProcessInstanceUUID instanceUUID = this.getRuntimeAPI().instantiateProcess(processUUID);

    this.checkExecutedManyTimes(instanceUUID, new String[]{"r1"}, 2);

    final Set<ActivityInstance> actIntances = this.getQueryRuntimeAPI().getActivityInstances(instanceUUID, "r1");

    final Set<Object> varValues = new HashSet<Object>();

    for (final ActivityInstance activityInstance : actIntances) {
      final Map<String, Object> variables = activityInstance.getLastKnownVariableValues();
      Assert.assertTrue(variables.containsKey("testVar"));
      varValues.add(variables.get("testVar"));
    }

    final Set<Object> expectedValues = new HashSet<Object>();
    expectedValues.add("val1");
    expectedValues.add("val2");

    Assert.assertEquals(expectedValues, varValues);

    this.getRuntimeAPI().deleteProcessInstance(instanceUUID);
    this.getManagementAPI().disable(processUUID);
    this.getManagementAPI().deleteProcess(processUUID);
  }


  public void testDeployWrongInstantiator() {
    //Test deployment with multiInstantiatorClass class not found
    final URL xpdlUrl = this.getClass().getResource("incorrectMultiInstantiation.xpdl");
    try {
      getBusinessArchiveFromXpdl(xpdlUrl);
      Assert.fail("Deployment shouldn't passed");
    } catch (final BonitaRuntimeException e) {
      Assert.assertTrue(e.getMessage(), e.getMessage().contains("MultiInstantiation needs to specify a MultiInstantiator class name"));
      Assert.assertTrue(e.getMessage(), e.getMessage().contains("MultiInstantiation needs to specify a variable id"));
    }
  }

  public void testTransitionsConditions() throws BonitaException {
    final URL xpdlUrl = this.getClass().getResource("transitionConditionMultiInstantiation.xpdl");
    //Test join number with transition condition on activity variable
    // this is not allowed
    final ProcessDefinitionUUID processUUID = this.getManagementAPI().deploy(this.getBusinessArchiveFromXpdl(
    	      xpdlUrl, JoinNumberConditionPass.class, JoinNumberConditionFail.class)).getUUID();
    
    final ProcessInstanceUUID instanceUUID = this.getRuntimeAPI().instantiateProcess(processUUID);
    
    final Set<LightActivityInstance> instances = getQueryRuntimeAPI().getLightActivityInstances(instanceUUID, "act1_joinNumberConditionPass");
    assertEquals(4, instances.size());
    
    int finishedSteps = 0;
    int failedSteps = 0;
    for (final LightActivityInstance activityInstance : instances) {
      if (activityInstance.getState().equals(ActivityState.FINISHED)) {
        finishedSteps++;
      } else if (activityInstance.getState().equals(ActivityState.FAILED)) {
        failedSteps++;
      }
    }
    
    assertEquals(1, failedSteps);
    assertEquals(3, finishedSteps);
    checkState(instanceUUID, InstanceState.STARTED);

    this.getManagementAPI().deleteProcess(processUUID);
  }

  public void testHookMultiInstantiation() throws BonitaException {
    //Test hook execution with multi instantiation
    final URL xpdlUrl = this.getClass().getResource("hookMultiInstantiation.xpdl");
    final ProcessDefinition process = this.getManagementAPI().deploy(this.getBusinessArchiveFromXpdl(xpdlUrl,
      HookActivityInstantiator.class, IncrInstanceVariableHook.class));
    ProcessDefinitionUUID processUUID = process.getUUID();

    final ProcessInstanceUUID instanceUUID = this.getRuntimeAPI().instantiateProcess(process.getUUID());
    this.checkExecutedManyTimes(instanceUUID, new String[]{"r1"}, 3);

    final Set<ActivityInstance> actIntances = this.getQueryRuntimeAPI().getActivityInstances(instanceUUID, "r1");

    final Set<Object> varValues = new HashSet<Object>();
    final Set<Object> expectedValues = new HashSet<Object>();

    for (final ActivityInstance activity : actIntances) {
      final Map<String, Object> variables = activity.getLastKnownVariableValues();
      Assert.assertTrue(variables.containsKey("counter"));
      varValues.add(variables.get("counter"));
    }

    expectedValues.add(4L);
    expectedValues.add(5L);
    expectedValues.add(6L);

    Assert.assertEquals(expectedValues, varValues);

    this.getRuntimeAPI().deleteProcessInstance(instanceUUID);
    this.getManagementAPI().disable(processUUID);
    this.getManagementAPI().deleteProcess(processUUID);
  }

  /*
   * Multiinstantiation element with a variable not declared in the XPDL
   */
  public void testInvalidVarName() throws BonitaException {
    //Test multiInstantiatorClass which throws an exception
    final URL xpdlUrl = this.getClass().getResource("invalidVarNameMultiInstantiation.xpdl");
    try {
      this.getManagementAPI().deploy(this.getBusinessArchiveFromXpdl(xpdlUrl, BadVarActivityInstantiator.class));
      Assert.fail("exception expected");
    } catch (final BonitaRuntimeException e) {
      Assert.assertTrue(e.getMessage().contains("MultiInstantiation variable testVar must be a local variable of activity r1"));
    }
  }

  /*
   * Multiinstantiation element without variable
   */
  public void testNoVariable() throws BonitaException {
    //Test multiInstantiatorClass which throws an exception
    final URL xpdlUrl = this.getClass().getResource("noVarMultiInstantiation.xpdl");
    try {
      this.getBusinessArchiveFromXpdl(xpdlUrl, BadVarActivityInstantiator.class);
      Assert.fail("exception expected");
    } catch (final BonitaRuntimeException e) {
      Assert.assertTrue(e.getMessage(), e.getMessage().contains("MultiInstantiation needs to specify a variable id (in a nested Variable element)"));
    }
  }

  public void testFailInstantiator() throws BonitaException {
    //Test multiInstantiatorClass which throws an exception
    final URL xpdlUrl = this.getClass().getResource("failMultiInstantiation-1.xpdl");
    ProcessDefinition process = this.getManagementAPI().deploy(this.getBusinessArchiveFromXpdl(xpdlUrl,
      NullActivityInstantiator.class));
    ProcessDefinitionUUID processUUID = process.getUUID();
    try {
      this.getRuntimeAPI().instantiateProcess(process.getUUID());
      Assert.fail("Failing multiInstantiatorClass should throw an exception");
    } catch (final MultiInstantiatorInvocationException e) {
      Assert.assertTrue(this.causeContains("MultiInstantiator execution returned null in activity r1", e));
    } 
    this.getRuntimeAPI().deleteAllProcessInstances(process.getUUID());
    
    this.getManagementAPI().disable(processUUID);
    this.getManagementAPI().deleteProcess(processUUID);
  }

  public void testFailInstantiator2() throws BonitaException {
	    //Test multiInstantiatorClass which throws an exception
	    final URL xpdlUrl = this.getClass().getResource("failMultiInstantiation-2.xpdl");
	    ProcessDefinition process = this.getManagementAPI().deploy(this.getBusinessArchiveFromXpdl(xpdlUrl, BadJoinNActivityInstantiator.class));
	    ProcessDefinitionUUID processUUID = process.getUUID();
	    
	    try {
	      this.getRuntimeAPI().instantiateProcess(process.getUUID());
	      Assert.fail("Process instantiation shouldn't passed because of invalid join number");
	    } catch (final MultiInstantiatorInvocationException e) {
	      Assert.assertTrue(this.causeContains("The join number must be greater than 0", e));
	    } 

	    this.getManagementAPI().disable(processUUID);
	    this.getManagementAPI().deleteProcess(processUUID);
	  }
  
  // multi instantiation element referring to global variable
  public void testGlobalVariableMultiInstantiation() throws BonitaException {
    final URL xpdlUrl = this.getClass().getResource("globalVarMultiInstantiation.xpdl");

    try {
      this.getManagementAPI().deploy(this.getBusinessArchiveFromXpdl(xpdlUrl,
          TestActivityInstantiator.class));
      Assert.fail("Deployment should fail");
    } catch (final BonitaRuntimeException e) {
      Assert.assertTrue(e.getMessage().contains("MultiInstantiation variable testVar must be a local variable of activity r1"));
    }
  }

  public void testSubflowMultiInstantiation() throws BonitaException {
    //Test subflow with multi instantiation
    
	  ProcessDefinition subProcess = this.getManagementAPI().deploy(this.getBusinessArchiveFromXpdl(
	    		this.getClass().getResource("subflowMultiInstantiation-sub.xpdl")));
	    ProcessDefinitionUUID subProcessUUID = subProcess.getUUID();
	    
    ProcessDefinition mainCondPass = this.getManagementAPI().deploy(this.getBusinessArchiveFromXpdl(
    		this.getClass().getResource("subflowMultiInstantiation-mainCondPass.xpdl"), JoinNumberConditionPass.class, CheckExecutionHook.class));
    ProcessDefinitionUUID mainCondPassUUID = mainCondPass.getUUID();

    ProcessInstanceUUID instanceUUID = this.getRuntimeAPI().instantiateProcess(mainCondPassUUID);
    this.checkExecutedManyTimes(instanceUUID, new String[]{"a2"}, 4);
    this.checkExecutedOnce(instanceUUID, new String[]{"a3", "a4"});

    this.getRuntimeAPI().deleteAllProcessInstances(mainCondPassUUID);

    this.getManagementAPI().disable(mainCondPassUUID);
    this.getManagementAPI().deleteProcess(mainCondPassUUID);

    ProcessDefinition mainCondFail = this.getManagementAPI().deploy(this.getBusinessArchiveFromXpdl(
    		this.getClass().getResource("subflowMultiInstantiation-mainCondFail.xpdl"), JoinNumberConditionFail.class, CheckExecutionHook.class));
    ProcessDefinitionUUID mainCondFailUUID = mainCondFail.getUUID();
    
    instanceUUID = this.getRuntimeAPI().instantiateProcess(mainCondFailUUID);
    assertEquals(2, getQueryRuntimeAPI().getLightProcessInstances(subProcessUUID).size());
    assertEquals(3, getQueryRuntimeAPI().getLightActivityInstances(instanceUUID, "a2").size());
    //this.checkExecutedManyTimes(instanceUUID, new String[]{"a2"}, 3);
    this.checkStopped(instanceUUID, new String[]{"a3"});

    try {
      this.getQueryRuntimeAPI().getActivityInstances(instanceUUID, "a4");
      Assert.fail("Activity : a3 must not be executed in this instance!");
    } catch (final ActivityNotFoundException e) {
      //nothing, we expect an exception
    } 
    this.getRuntimeAPI().deleteAllProcessInstances(mainCondFailUUID);

    this.getManagementAPI().disable(mainCondFailUUID);
    this.getManagementAPI().deleteProcess(mainCondFailUUID);
    
    this.getManagementAPI().disable(subProcessUUID);
    this.getManagementAPI().deleteProcess(subProcessUUID);
  }


  // This test creates two instance of task r, and wait for only one to be done. The other task is aborted.
  public void testPartialJoinTaskMultiInstantiation() throws BonitaException {
    final URL xpdlUrl = this.getClass().getResource("joinMultiInstantiation.xpdl");
    final ProcessDefinition process = this.getManagementAPI().deploy(this.getBusinessArchiveFromXpdl(xpdlUrl,
      TestOneJoinActivityInstantiator.class));
    final ProcessDefinitionUUID processUUID = process.getUUID();
    final ProcessInstanceUUID instanceUUID = this.getRuntimeAPI().instantiateProcess(processUUID);

    final Set<ActivityInstance> actIntances = this.getQueryRuntimeAPI().getActivityInstances(instanceUUID, "r1");
    Assert.assertEquals(2, actIntances.size());

    final Set<Object> varValues = new HashSet<Object>();

    // check two instance of activity r1 have been created
    for (final ActivityInstance activityInstance : actIntances) {
      final Map<String, Object> variables = activityInstance.getLastKnownVariableValues();
      Assert.assertTrue(variables.containsKey("testVar"));
      varValues.add(variables.get("testVar"));
    }

    final Set<Object> expectedValues = new HashSet<Object>();
    expectedValues.add("val1");
    expectedValues.add("val2");

    Assert.assertEquals(expectedValues, varValues);

    // execute one task
    final ActivityInstance activityInstance = actIntances.iterator().next();
    Assert.assertTrue(activityInstance.isTask());
    ActivityInstanceUUID taskUUID = activityInstance.getUUID();
    this.getRuntimeAPI().startTask(taskUUID, false);
    this.getRuntimeAPI().finishTask(taskUUID, false);

    // check we are now on activity r2
    final Set<ActivityInstance> r2instances = this.getQueryRuntimeAPI().getActivityInstances(instanceUUID, "r2");
    Assert.assertEquals(1, r2instances.size());

    // check the second task r1 has been aborted
    final Collection<TaskInstance> abortedTasks =
      this.getQueryRuntimeAPI().getTaskList(instanceUUID, ActivityState.ABORTED);
    Assert.assertEquals(1, abortedTasks.size());
    Assert.assertEquals("r1", abortedTasks.iterator().next().getActivityName());

    // execute task r2 to finish the instance
    final Collection<TaskInstance> tasks = this.getQueryRuntimeAPI().getTaskList(instanceUUID, ActivityState.READY);
    Assert.assertEquals(1, tasks.size());
    final TaskInstance task = tasks.iterator().next();
    Assert.assertEquals("r2", task.getActivityName());
    Assert.assertEquals(ActivityState.READY, task.getState());
    taskUUID = task.getUUID();
    this.getRuntimeAPI().startTask(taskUUID, false);
    this.getRuntimeAPI().finishTask(taskUUID, false);

    this.checkExecutedOnce(instanceUUID, new String[]{"r2"});

    this.getManagementAPI().disable(processUUID);
    this.getManagementAPI().deleteProcess(processUUID);
  }

  /**
   * Automatic process that creates 2 subflows with a Manual activity.
   *
   */
   public void testPartialJoinSubFlowMultiInstantiation() throws BonitaException {
	   final ProcessDefinition subProcess = this.getManagementAPI().deploy(this.getBusinessArchiveFromXpdl(this.getClass().getResource("joinSubflowMultiInstantiation-sub.xpdl"),
		         TestOneJoinActivityInstantiator.class));
		     final ProcessDefinitionUUID subProcessUUID = subProcess.getUUID();
		     
     final ProcessDefinition process = this.getManagementAPI().deploy(this.getBusinessArchiveFromXpdl(this.getClass().getResource("joinSubflowMultiInstantiation.xpdl"),
         TestOneJoinActivityInstantiator.class));
     final ProcessDefinitionUUID processUUID = process.getUUID();
     
     
     final ProcessInstanceUUID instanceUUID = this.getRuntimeAPI().instantiateProcess(processUUID);
     this.checkStopped(instanceUUID, new String[]{"a1"});

     // check two tasks have been created in two subprocess
     final Collection<TaskInstance> taskActivities = this.getQueryRuntimeAPI().getTaskList(ActivityState.READY);
     Assert.assertEquals(2, taskActivities.size());
     final Iterator<TaskInstance> iterator = taskActivities.iterator();
     final TaskInstance taskActivity = iterator.next();
     final ProcessInstance processInstance = this.getQueryRuntimeAPI().getProcessInstance(taskActivity.getProcessInstanceUUID());
     Assert.assertEquals(instanceUUID, processInstance.getParentInstanceUUID());
     // check other task is not in the same sub process
     final TaskInstance otherTaskActivity = iterator.next();
     Assert.assertFalse(otherTaskActivity.getProcessInstanceUUID().equals(taskActivity.getProcessInstanceUUID()));

     // execute one task => the subprocess will be finished
     final ActivityInstanceUUID taskUUID = taskActivity.getUUID();
     this.getRuntimeAPI().startTask(taskUUID, true);
     this.getRuntimeAPI().finishTask(taskUUID, true);

     Assert.assertEquals(InstanceState.FINISHED, this.getQueryRuntimeAPI().getProcessInstance(taskActivity.getProcessInstanceUUID()).getInstanceState());

     // check other subprocess instance has been aborted
     Assert.assertEquals(InstanceState.ABORTED, this.getQueryRuntimeAPI().getProcessInstance(otherTaskActivity.getProcessInstanceUUID()).getInstanceState());
     Assert.assertEquals(ActivityState.ABORTED, this.getQueryRuntimeAPI().getActivityInstance(otherTaskActivity.getUUID()).getState());

     // check end of main execution
     this.checkExecutedOnce(instanceUUID, new String[]{"a1", "a3"});

     this.getRuntimeAPI().deleteProcessInstance(instanceUUID);
     this.getManagementAPI().disable(processUUID);
     this.getManagementAPI().deleteProcess(processUUID);
     this.getManagementAPI().disable(subProcessUUID);
     this.getManagementAPI().deleteProcess(subProcessUUID);
   }

   // This test creates two instance of task r1.
   // The task r3 is executed, and as r2 as a join XOR, r1 tasks are aborted.
   public void testJoinXORAbortMultiInstantiation() throws BonitaException {
     final URL xpdlUrl = this.getClass().getResource("joinXORMultiInstantiation.xpdl");
     final ProcessDefinition process = this.getManagementAPI().deploy(this.getBusinessArchiveFromXpdl(xpdlUrl,
       TestOneJoinActivityInstantiator.class));
     final ProcessDefinitionUUID processUUID = process.getUUID();
     final ProcessInstanceUUID instanceUUID = this.getRuntimeAPI().instantiateProcess(processUUID);

     final Set<ActivityInstance> actIntances = this.getQueryRuntimeAPI().getActivityInstances(instanceUUID, "r1");
     Assert.assertEquals(2, actIntances.size());

     final Set<Object> varValues = new HashSet<Object>();

     // check two instance of activity r1 have been created
     for (final ActivityInstance activityInstance : actIntances) {
       final Map<String, Object> variables = activityInstance.getLastKnownVariableValues();
       Assert.assertTrue(variables.containsKey("testVar"));
       varValues.add(variables.get("testVar"));
     }

     final Set<Object> expectedValues = new HashSet<Object>();
     expectedValues.add("val1");
     expectedValues.add("val2");

     Assert.assertEquals(expectedValues, varValues);

     // execute task r3
     final Set<ActivityInstance> r3Intances = this.getQueryRuntimeAPI().getActivityInstances(instanceUUID, "r3");
     Assert.assertEquals(1, r3Intances.size());
     final ActivityInstance activityInstance = r3Intances.iterator().next();
     Assert.assertTrue(activityInstance.isTask());
     if (activityInstance.isTask()) {
       final ActivityInstanceUUID taskUUID = activityInstance.getUUID();
       this.getRuntimeAPI().startTask(taskUUID, false);
       this.getRuntimeAPI().finishTask(taskUUID, false);
     }

     // check we are now on activity r2
     final Set<ActivityInstance> r2instances = this.getQueryRuntimeAPI().getActivityInstances(instanceUUID, "r2");
     Assert.assertEquals(1, r2instances.size());

     // check the tasks r1 has been aborted
     final Collection<TaskInstance> abortedTasks =
       this.getQueryRuntimeAPI().getTaskList(instanceUUID, ActivityState.ABORTED);
     Assert.assertEquals(2, abortedTasks.size());
     for (final TaskInstance abortedTask : abortedTasks) {
       Assert.assertEquals("r1", abortedTask.getActivityName());
       Assert.assertEquals(ActivityState.ABORTED, abortedTask.getState());
     }

     // execute task r2 to finish the instance
     final Collection<TaskInstance> tasks = this.getQueryRuntimeAPI().getTaskList(instanceUUID, ActivityState.READY);
     Assert.assertEquals(1, tasks.size());
     final TaskInstance task = tasks.iterator().next();
     Assert.assertEquals("r2", task.getActivityName());
     Assert.assertEquals(ActivityState.READY, task.getState());
     final ActivityInstanceUUID taskUUID = task.getUUID();
     this.getRuntimeAPI().startTask(taskUUID, false);
     this.getRuntimeAPI().finishTask(taskUUID, false);

     this.checkExecutedOnce(instanceUUID, new String[]{"r2"});

     this.getManagementAPI().disable(processUUID);
     this.getManagementAPI().deleteProcess(processUUID);
   }
}
