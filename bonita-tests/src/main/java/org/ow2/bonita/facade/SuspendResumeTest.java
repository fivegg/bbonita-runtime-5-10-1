package org.ow2.bonita.facade;

import java.net.URL;
import java.util.Collection;

import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.ow2.bonita.APITestCase;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.exception.IllegalTaskStateException;
import org.ow2.bonita.facade.exception.TaskNotFoundException;
import org.ow2.bonita.facade.runtime.ActivityState;
import org.ow2.bonita.facade.runtime.InstanceState;
import org.ow2.bonita.facade.runtime.TaskInstance;
import org.ow2.bonita.facade.uuid.ActivityInstanceUUID;
import org.ow2.bonita.facade.uuid.IdFactory;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.light.LightActivityInstance;
import org.ow2.bonita.light.LightProcessInstance;
import org.ow2.bonita.util.BonitaConstants;
import org.ow2.bonita.util.BonitaException;
import org.ow2.bonita.util.ProcessBuilder;
import org.ow2.bonita.util.SimpleCallbackHandler;

public class SuspendResumeTest extends APITestCase {

  /*
   * Test suspendTask() from the runtimeAPI.
   *
   * Suspend the task if the task has READY or EXECUTING state.
   * If successful, this operation changes task state from EXECUTING to SUSPENDED.
   * @throws TaskNotFoundException if the task has not been found.
   * @throws IllegalTaskStateException if the state of the task has not either READY or EXECUTING state.
   * @throws UnAuthorizedUserException if the user is not the user assigned to the task.
   * @throws UnAuthorizedUserException if task has not been assigned and the user is not in the list of candidates for the task.
   * @throws BonitaInternalException if an exception occurs (for ex. with illegal taskUUID parameter).
   *
   *
   * Test resumeTask() from the RuntimeAPI
   *
   * Resume the task if the task has SUSPENDED state.
   * If successful, this operation changes task state from SUSPENDED to EXECUTING.
   * @throws TaskNotFoundException if the task has not been found.
   * @throws IllegalTaskStateException if the state of the task has not SUSPENDED state.
   * @throws UnAuthorizedUserException if the user is not the user assigned to the task.
   * @throws UnAuthorizedUserException if task has not been assigned and the user is not in the list of candidates for the task.
   * @throws BonitaInternalException if an exception occurs (for ex. with illegal taskUUID parameter).
   */

  public void testSuspendResume() throws BonitaException, LoginException {
    final URL xpdlUrl = this.getClass().getResource("suspendResume.xpdl");
    final ProcessDefinition process =
      getManagementAPI().deploy(getBusinessArchiveFromXpdl(xpdlUrl, AdminRoleMapper.class));
    
    final ProcessDefinitionUUID processUUID = process.getUUID();
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    Collection<TaskInstance> todoList = getQueryRuntimeAPI().getTaskList(ActivityState.READY);
    assertNotNull(todoList);
    assertEquals(1, todoList.size());

    TaskInstance taskActivity = todoList.iterator().next();
    ActivityInstanceUUID taskUUID = taskActivity.getUUID();

    assertEquals(ActivityState.READY, getQueryRuntimeAPI().getTask(taskUUID).getState());

    assertEquals(0, getQueryRuntimeAPI().getTaskList(ActivityState.SUSPENDED).size());
    assertEquals(0, getQueryRuntimeAPI().getTaskList(instanceUUID, ActivityState.SUSPENDED).size());

    //check BonitaInternalException and TaskNotFounException for suspendTask()
    checkBIExceptionForSuspend(taskUUID);

    //IllegalTaskStateException for resumeTask() : try ro resume a task with READY state.
    try {
      getRuntimeAPI().resumeTask(taskUUID, false);
      fail("Check illegal state when resuming the task");
    } catch (final IllegalTaskStateException ie) {
      //OK
      assertEquals(ActivityState.READY, ie.getCurrentState());
    } 

    //suspend task
    getRuntimeAPI().suspendTask(taskUUID, true);

    //Testing IllegalTaskStateException when task is already SUSPENDED
    try {
      getRuntimeAPI().suspendTask(taskUUID, true);
      fail("Check illegal state when suspending the task");
    } catch (final IllegalTaskStateException ie) {
      //OK
      assertEquals(ActivityState.SUSPENDED, ie.getCurrentState());
    } 

    // check the task is not anymore in the todoList
    todoList = getQueryRuntimeAPI().getTaskList(ActivityState.READY);
    assertEquals(0, todoList.size());
    todoList = getQueryRuntimeAPI().getTaskList(instanceUUID, ActivityState.READY);
    assertEquals(0, todoList.size());

    //check that the task is SUSPENDED
    assertEquals(ActivityState.SUSPENDED, getQueryRuntimeAPI().getTask(taskUUID).getState());

    Collection<TaskInstance> suspendedTasks = getQueryRuntimeAPI().getTaskList(ActivityState.SUSPENDED);
    assertEquals(1, suspendedTasks.size());
    assertEquals(taskUUID, suspendedTasks.iterator().next().getUUID());

    suspendedTasks = getQueryRuntimeAPI().getTaskList(instanceUUID, ActivityState.SUSPENDED);
    assertEquals(1, suspendedTasks.size());
    assertEquals(taskUUID, suspendedTasks.iterator().next().getUUID());

    try {
      //check we can't start a SUSPENDED task
      getRuntimeAPI().startTask(taskUUID, true);
      fail("It is impossible to start a suspended task...");
    } catch (final IllegalTaskStateException e) {
      causeContains("Task " + taskUUID + " is not in the READY state!", e);
    } 

    // Log as a new user
    loginContext.logout();
    final String loginName = "john";
    final String password = "bpm";
    try {
      loginContext = new LoginContext("BonitaAuth", new SimpleCallbackHandler(loginName, password));
      loginContext.login();
      loginContext.logout();
      loginContext = new LoginContext("BonitaStore", new SimpleCallbackHandler(loginName, password));
      loginContext.login();
    } catch (final LoginException e) {
      throw new RuntimeException("Please, configure a JAAS test user with login: "
          + loginName + " and password: " + password, e);
    }
    // Check that the task taskUUID is not in our list of suspended task
    todoList = getQueryRuntimeAPI().getTaskList(ActivityState.SUSPENDED);
    assertTrue(todoList.toString(), todoList.isEmpty());

    // UnAuthorizedUserException : try to resume and suspend the task with user
    // that does not have the task into its suspended list
    // this is authorized.
    getRuntimeAPI().resumeTask(taskUUID, false);
    assertTrue(getQueryRuntimeAPI().getTask(taskUUID).getState().equals(ActivityState.READY));
    getRuntimeAPI().suspendTask(taskUUID, true);
    assertTrue(getQueryRuntimeAPI().getTask(taskUUID).getState().equals(ActivityState.SUSPENDED));


    loginContext.logout();
    login(); // Again as the "admin user"


    //check BonitaInternalException and TaskNotFounException for resumeTask()
    checkBIExceptionForResume();

    // Resume normally
    getRuntimeAPI().resumeTask(taskUUID, true);

    todoList = getQueryRuntimeAPI().getTaskList(instanceUUID, ActivityState.READY);
    assertEquals(1, todoList.size());
    assertEquals(ActivityState.READY, getQueryRuntimeAPI().getTask(taskUUID).getState());

    assertEquals(0, getQueryRuntimeAPI().getTaskList(ActivityState.SUSPENDED).size());
    assertEquals(0, getQueryRuntimeAPI().getTaskList(instanceUUID, ActivityState.SUSPENDED).size());

    //start task "a"
    getRuntimeAPI().startTask(taskUUID, true);
    assertEquals(ActivityState.EXECUTING, getQueryRuntimeAPI().getTask(taskUUID).getState());
    assertEquals("a", getQueryRuntimeAPI().getTask(taskUUID).getActivityName());

    //check that if the state before suspend was EXECUTING, then after resume it is EXECUTIONG
    getRuntimeAPI().suspendTask(taskUUID, true);
    assertEquals(ActivityState.SUSPENDED, getQueryRuntimeAPI().getTask(taskUUID).getState());
    getRuntimeAPI().resumeTask(taskUUID, false);
    assertEquals(ActivityState.EXECUTING, getQueryRuntimeAPI().getTask(taskUUID).getState());
    //finish task
    getRuntimeAPI().finishTask(taskUUID, true);

    //Testing IllegalTaskStateException when task has been already FINISHED
    try {
      getRuntimeAPI().suspendTask(taskUUID, true);
      fail("Check finished state is illegal state when suspending the task");
    } catch (final IllegalTaskStateException te) {
      //OK
      assertEquals(taskUUID, te.getActivityInstanceUUID());
    } 

    // test the admin login can suspend and resume the task since this task has not been assigned.
    // ie the performer is role type and there's no performer assignment
    // then the userId of the task is null but the list of candidates contains admin.
    todoList = getQueryRuntimeAPI().getTaskList(ActivityState.READY);
    assertNotNull(todoList);
    assertEquals(1, todoList.size());

    taskActivity = todoList.iterator().next();
    assertEquals("b", taskActivity.getActivityName());
    taskUUID = taskActivity.getUUID();
    assertTrue(getQueryRuntimeAPI().getTask(taskUUID).getTaskCandidates().contains("admin"));

    assertEquals(BonitaConstants.SYSTEM_USER, getQueryRuntimeAPI().getTask(taskUUID).getUpdatedBy());
    assertFalse(getQueryRuntimeAPI().getTask(taskUUID).isTaskAssigned());
    assertNull(getQueryRuntimeAPI().getTask(taskUUID).getTaskUser());
    /*
    try {
      fail(getQueryRuntimeAPI().getTask(taskUUID).getTaskUser());
    } catch (IllegalStateException ie) {
      //OK
    }
*/


    getRuntimeAPI().suspendTask(taskUUID, true);
    getRuntimeAPI().resumeTask(taskUUID, false);

    //go on with the remaining tasks : c
    todoList = getQueryRuntimeAPI().getTaskList(ActivityState.READY);
    while (todoList.size() != 0) {
      taskActivity = todoList.iterator().next();
      getRuntimeAPI().startTask(taskActivity.getUUID(), true);
      getRuntimeAPI().finishTask(taskActivity.getUUID(), true);
      todoList = getQueryRuntimeAPI().getTaskList(ActivityState.READY);
    }

    checkExecutedOnce(instanceUUID, new String[]{"a", "b", "c"});
    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  private void checkBIExceptionForResume() throws BonitaException {
    try {
      getRuntimeAPI().resumeTask(null, false);
      fail("Check null argument");
    } catch (final IllegalArgumentException be) {
      // ok
    } catch (final NullPointerException e){
    	//Ok REST
    	final StackTraceElement firstElement = e.getStackTrace()[0];
    	assertTrue(firstElement.getClassName().equals("org.jboss.resteasy.client.ClientRequest"));
    	assertTrue(firstElement.getMethodName().equals("toString"));
    }
    try {
      getRuntimeAPI().resumeTask(IdFactory.getNewTaskUUID(), false);
      fail("Check non existent instance");
    } catch (final TaskNotFoundException be) {
      //OK
    } 
  }

  private void checkBIExceptionForSuspend(final ActivityInstanceUUID taskUUID) throws BonitaException, LoginException {
    //BonitaInternalException
    try {
      getRuntimeAPI().suspendTask(null, true);
      fail("Check null argument");
    } catch (final IllegalArgumentException be) {
      // ok
    } catch (final NullPointerException e){
    	//Ok REST
    	final StackTraceElement firstElement = e.getStackTrace()[0];
    	assertTrue(firstElement.getClassName().equals("org.jboss.resteasy.client.ClientRequest"));
    	assertTrue(firstElement.getMethodName().equals("toString"));
    }

    //TaskNotFoundException
    try {
      getRuntimeAPI().suspendTask(IdFactory.getNewTaskUUID(), true);
      fail("Check non existent instance");
    } catch (final TaskNotFoundException be) {
      //OK
    } 

    //Login as UnAuthorizedUserException, suspend & resume the task
    loginAsUnauthorizedLoginForTest();
    getRuntimeAPI().suspendTask(taskUUID, true);
    assertTrue(getQueryRuntimeAPI().getTask(taskUUID).getState().equals(ActivityState.SUSPENDED));
    getRuntimeAPI().resumeTask(taskUUID, false);
    assertTrue(getQueryRuntimeAPI().getTask(taskUUID).getState().equals(ActivityState.READY));


    loginContext.logout();
    login(); // Again as the "normal user"
  }

  private void loginAsUnauthorizedLoginForTest() throws LoginException {
    loginContext.logout();
    final String loginName = "john";
    final String password = "bpm";
    try {
      loginContext = new LoginContext("BonitaAuth", new SimpleCallbackHandler(loginName, password));
      loginContext.login();
      loginContext.logout();
      loginContext = new LoginContext("BonitaStore", new SimpleCallbackHandler(loginName, password));
      loginContext.login();
    } catch (final LoginException e) {
      throw new RuntimeException("Please, configure a JAAS test user with login: "
          + loginName + " and password: " + password, e);
    }
  }
  
  public void testProcessInstanceCannotFinishWithSupendedStep() throws Exception {
    ProcessDefinition processDefinition = ProcessBuilder.createProcess("suspendedProcess", "1.0")
      .addHuman(getLogin())
      .addSystemTask("step1")
      .addHumanTask("step2", getLogin())
      .addHumanTask("step3", getLogin())
      .addTransition("step1", "step2")
      .addTransition("step1", "step3")
      .done();
    
    processDefinition = getManagementAPI().deploy(getBusinessArchive(processDefinition));
    
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processDefinition.getUUID());
    LightActivityInstance activityInstance = getActivityInstance(instanceUUID, "step2");
    getRuntimeAPI().suspendTask(activityInstance.getUUID(), true);
    
    executeTask(instanceUUID, "step3");
    
    activityInstance = getActivityInstance(instanceUUID, "step2");
    assertEquals(ActivityState.SUSPENDED, activityInstance.getState());
    final LightProcessInstance processInstance = getQueryRuntimeAPI().getLightProcessInstance(instanceUUID);
    assertEquals(InstanceState.STARTED, processInstance.getInstanceState());
    
    getManagementAPI().deleteProcess(processDefinition.getUUID());
    
  }

}
