package org.ow2.bonita.facade;

import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.security.auth.login.LoginException;

import org.ow2.bonita.APITestCase;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.exception.TaskNotFoundException;
import org.ow2.bonita.facade.runtime.ActivityInstance;
import org.ow2.bonita.facade.runtime.ActivityState;
import org.ow2.bonita.facade.runtime.TaskInstance;
import org.ow2.bonita.facade.uuid.ActivityInstanceUUID;
import org.ow2.bonita.facade.uuid.IdFactory;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.util.BonitaConstants;
import org.ow2.bonita.util.BonitaException;
import org.ow2.bonita.util.BusinessArchiveFactory;
import org.ow2.bonita.util.ProcessBuilder;


public class AssignTest extends APITestCase {
  /*
   *   test the 3 assign methods:
   *      - re-executing roleMapper & performerAssign
   *      - or set candidates list
   *      - or set assigned user
   *
   *    Check recorded informations on change state and assign
   *    using methods of the interface  (TaskInstance)
   *       - Check changing state update when changing states
   *       - Check assign update when changing assigning
   *
   *    XPDL design versus assignment is the following:
   *
   *    activity    :    act1              act2            act3          act4     act5        act6
   *    ------------------------------------------------------------------------------------------
   *    part. type  :    role              role            role          human    human       human
   *    Role Mapper :    RM                /               /             /        RM          /
   *    Perf Assign :    PA                /               /             /        /           PA
   *
   *    Note:  case act3, act4, act5, act6   not yet added/tested !
   */
  public void testAssignTask() throws BonitaException, LoginException {
    ProcessInstanceUUID instanceUUID = null;
    final String loginJohn = "john";
    final String passwordJohn = "bpm";
    URL xpdlUrl = this.getClass().getResource("testAssign.xpdl");
    ProcessDefinition process = getManagementAPI().deploy(getBusinessArchiveFromXpdl(xpdlUrl, AssignRoleMapper.class, AdminPerformerAssign.class));

    ProcessDefinitionUUID processUUID = process.getUUID();
    try {
      instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);
    } catch (BonitaException e) {
      fail("Ouch! How can it be?! Should never reach this statement!");
    }
    checkStopped(instanceUUID, new String[]{});
    Collection<TaskInstance> taskActivitiesToDo = getQueryRuntimeAPI().getTaskList(instanceUUID, ActivityState.READY);
    assertEquals(1, taskActivitiesToDo.size());
    TaskInstance taskInstance = taskActivitiesToDo.iterator().next();

    //Check assignment with initial userId/candidates
    Set<String> candidates = taskInstance.getTaskCandidates();
    assertEquals(0, candidates.size());
    assertTrue(taskInstance.isTaskAssigned());
    assertEquals("admin", taskInstance.getTaskUser());

    assertNotNull(taskInstance.getStateUpdates());
    assertEquals(1, taskInstance.getStateUpdates().size());
    assertEquals(BonitaConstants.SYSTEM_USER, taskInstance.getStateUpdates().get(0).getUpdatedBy());
    assertEquals(ActivityState.READY, taskInstance.getStateUpdates().get(0).getActivityState());
    assertNotNull(taskInstance.getStateUpdates().get(0).getUpdatedDate());

    //****************** assign task (re-executing roleMapper+performerAssign) *********************
    getRuntimeAPI().assignTask(taskInstance.getUUID());

    taskActivitiesToDo = getQueryRuntimeAPI().getTaskList(instanceUUID, ActivityState.READY);
    assertEquals(1, taskActivitiesToDo.size());
    taskInstance = taskActivitiesToDo.iterator().next();
    ActivityInstanceUUID taskUUID = taskInstance.getUUID();


    //Check execution of the role mapper
    candidates = taskInstance.getTaskCandidates();
    assertEquals(2, taskInstance.getAssignUpdates().size());
    //  assertEquals(2, taskInstance.getAssignUpdates().get(1).getCandidates().size());
    //  assertEquals(2, taskInstance.getTaskCandidates().size());

    assertEquals("admin", taskInstance.getAssignUpdates().get(1).getUpdatedBy());
    assertEquals(ActivityState.READY, taskInstance.getAssignUpdates().get(1).getActivityState());
    assertNotNull(taskInstance.getAssignUpdates().get(1).getUpdatedDate());
    //  assertEquals(2, taskInstance.getAssignUpdates().get(1).getCandidates().size());

    //Check execution of the performer assign
    assertEquals("admin", taskInstance.getAssignUpdates().get(1).getAssignedUserId());
    assertEquals("admin", taskInstance.getTaskUser());

    //TaskNotFoundException
    ActivityInstanceUUID badTaskUUID = IdFactory.getNewTaskUUID();
    try {
      getRuntimeAPI().assignTask(badTaskUUID);
      fail("check TaskNotFoundException raised if not existing taskUUID");
    } catch (TaskNotFoundException e) {
      //OK
    } 

    //****************** assign task with userId **************************************************
    getRuntimeAPI().assignTask(taskInstance.getUUID(), "toto");

    taskActivitiesToDo = getQueryRuntimeAPI().getTaskList(instanceUUID,"toto", ActivityState.READY);
    assertEquals(1, taskActivitiesToDo.size());
    taskInstance = taskActivitiesToDo.iterator().next();
    taskUUID = taskInstance.getUUID();

    //assertEquals("toto", taskInstance.getUserId());
    //2 assigns has been done => 2 records in assignUpdates
    assertEquals("toto", taskInstance.getTaskUser());
    assertEquals(3, taskInstance.getAssignUpdates().size());
    assertEquals("toto", taskInstance.getAssignUpdates().get(2).getAssignedUserId());
    assertEquals("toto", taskInstance.getTaskUser());

    //TaskNotFoundException
    try {
      getRuntimeAPI().assignTask(badTaskUUID, "toto");
      fail("check TaskNotFoundException raised if not existing taskUUID");
    } catch (TaskNotFoundException e) {
      //OK
    } 

    //test null arguments: TaskUUID and userId for assign(taskUUID, userId)
    String nullUserId = null;
    try {
      getRuntimeAPI().assignTask(badTaskUUID, nullUserId);
      fail("check IllegalArgumentException raised if userId == null");
    } catch (IllegalArgumentException e) {
      //OK
    }  catch (NullPointerException e){
    	//Ok REST
    	StackTraceElement firstElement = e.getStackTrace()[0];
    	assertTrue(firstElement.getClassName().equals("org.jboss.resteasy.client.ClientRequest"));
    	assertTrue(firstElement.getMethodName().equals("toString"));
    }

    //****************** assign task with candidates *******************************************
    HashSet<String> newCandidates = new HashSet<String>();
    newCandidates.add("admin");
    newCandidates.add("titi");
    getRuntimeAPI().assignTask(taskUUID, newCandidates);
    //Test the new candidates have been set (into 4th update)
    taskActivitiesToDo = getQueryRuntimeAPI().getTaskList(instanceUUID,"toto", ActivityState.READY);
    assertEquals(0, taskActivitiesToDo.size());
    taskActivitiesToDo = getQueryRuntimeAPI().getTaskList(instanceUUID,"titi", ActivityState.READY);
    taskInstance = taskActivitiesToDo.iterator().next();
    
    //check candidates update with getUpdates()
    Set<String> updatedCandidates = taskInstance.getAssignUpdates().get(3).getCandidates();
    assertTrue(updatedCandidates.size() == 2);
    assertTrue(updatedCandidates.contains("admin"));
    assertTrue(updatedCandidates.contains("titi"));
    //check candidates update with getTaskCandidates()
    updatedCandidates = taskInstance.getTaskCandidates();
    assertTrue(updatedCandidates.size() == 2);
    assertTrue(updatedCandidates.contains("admin"));
    assertTrue(updatedCandidates.contains("titi"));

    //test null arguments: TaskUUID and userId  assign(taskUUID, candidates)
    //TaskNotFoundException
    try {
      getRuntimeAPI().assignTask(badTaskUUID, newCandidates);
      fail("check TaskNotFoundException raised if not existing taskUUID");
    } catch (TaskNotFoundException e) {
      //OK
    } 

    //test null arguments: TaskUUID and userId for assign(taskUUID, userId)
    Set<String> nullCandidates = null;
    try {
      getRuntimeAPI().assignTask(badTaskUUID, nullCandidates);
      fail("check IllegalArgumentException raised if userId == null");
    } catch (IllegalArgumentException e) {
      //OK
    } 

    //******************** start act1 **********************************************************
    getRuntimeAPI().startTask(taskUUID, true);
    //check that stateUpdate + assignUpdate have been added => 2 more updates
    taskActivitiesToDo = getQueryRuntimeAPI().getTaskList(instanceUUID, ActivityState.EXECUTING);
    assertEquals(1, taskActivitiesToDo.size());
    taskInstance = taskActivitiesToDo.iterator().next();
    taskUUID = taskInstance.getUUID();
    assertEquals(2, taskInstance.getStateUpdates().size()); 

    //Check content of the 4 th update (StateUpdate)
    assertEquals(2, taskInstance.getStateUpdates().size());
    assertEquals("admin", taskInstance.getStateUpdates().get(1).getUpdatedBy());
    assertNotNull(taskInstance.getStateUpdates().get(1).getUpdatedDate());
    assertEquals(ActivityState.READY, taskInstance.getStateUpdates().get(1).getInitialState());
    assertEquals(ActivityState.EXECUTING, taskInstance.getStateUpdates().get(1).getActivityState());

    //Check content of the 5 th update (AssignUpdate)
    assertEquals(5, taskInstance.getAssignUpdates().size());
    assertEquals("admin", taskInstance.getAssignUpdates().get(4).getUpdatedBy());
    assertNotNull(taskInstance.getAssignUpdates().get(4).getUpdatedDate());
    assertEquals(ActivityState.READY, taskInstance.getAssignUpdates().get(4).getActivityState());
    assertEquals("admin", taskInstance.getAssignUpdates().get(4).getAssignedUserId());


    //********************** Finish act1 (act2 gets READY) *************************************
    getRuntimeAPI().finishTask(taskUUID, true);

    taskActivitiesToDo = getQueryRuntimeAPI().getTaskList(instanceUUID, ActivityState.FINISHED);
    assertEquals(1, taskActivitiesToDo.size());
    taskInstance = taskActivitiesToDo.iterator().next();
    assertEquals(3, taskInstance.getStateUpdates().size());
    assertEquals(6, taskInstance.getAssignUpdates().size());
    assertEquals(9, taskInstance.getStateUpdates().size() + taskInstance.getAssignUpdates().size());

    //Check content of the AssignUpdate for finish
    assertEquals("admin", taskInstance.getAssignUpdates().get(5).getUpdatedBy());
    assertNotNull(taskInstance.getAssignUpdates().get(5).getUpdatedDate());
    assertEquals(ActivityState.EXECUTING, taskInstance.getAssignUpdates().get(5).getActivityState());
    assertEquals("admin", taskInstance.getAssignUpdates().get(5).getAssignedUserId());

    //Check content of the StateUpdate for finish
    assertEquals("admin", taskInstance.getStateUpdates().get(2).getUpdatedBy());
    assertNotNull(taskInstance.getStateUpdates().get(2).getUpdatedDate());
    assertEquals(ActivityState.EXECUTING, taskInstance.getStateUpdates().get(2).getInitialState());
    assertEquals(ActivityState.FINISHED, taskInstance.getStateUpdates().get(2).getActivityState());



    //****************** get act2 *****************************************
    //get act2 that is not assigned !! (Role + No performer assignment)
    taskActivitiesToDo = getQueryRuntimeAPI().getTaskList(instanceUUID, ActivityState.READY);
    assertEquals(1, taskActivitiesToDo.size());
    taskInstance = taskActivitiesToDo.iterator().next();
    taskUUID = taskInstance.getUUID();
    assertEquals(BonitaConstants.SYSTEM_USER, taskInstance.getUpdatedBy());
    //check the task is not assigned
    assertFalse(taskInstance.isTaskAssigned());
    assertNull(taskInstance.getTaskUser());
    /*
    try {
      fail(taskInstance.getTaskUser());
    } catch (IllegalStateException ie) {
      //OK
    }
     */

    //Check only stateUpdate has been added in updates when the task changes from INITIAL to READY state.
    assertEquals(1, taskInstance.getStateUpdates().size());
    assertEquals(1, taskInstance.getAssignUpdates().size());
    assertEquals(BonitaConstants.SYSTEM_USER, taskInstance.getStateUpdates().get(0).getUpdatedBy());
    assertNotNull(taskInstance.getStateUpdates().get(0).getUpdatedDate());
    assertEquals(ActivityState.READY, taskInstance.getStateUpdates().get(0).getInitialState());
    assertEquals(ActivityState.READY, taskInstance.getStateUpdates().get(0).getActivityState());

    //************* Suspend the task choosing assignTask = false ***********************
    getRuntimeAPI().suspendTask(taskUUID, false);
    taskActivitiesToDo = getQueryRuntimeAPI().getTaskList(instanceUUID, ActivityState.SUSPENDED);
    assertEquals(1, taskActivitiesToDo.size());
    taskInstance = taskActivitiesToDo.iterator().next();
    taskUUID = taskInstance.getUUID();
    assertEquals(2, taskInstance.getStateUpdates().size());
    assertEquals(1, taskInstance.getAssignUpdates().size());

    //Check content of the update for the suspend (StateUpdate)
    assertEquals("admin", taskInstance.getStateUpdates().get(1).getUpdatedBy());
    assertNotNull(taskInstance.getStateUpdates().get(1).getUpdatedDate());
    assertEquals(ActivityState.READY, taskInstance.getStateUpdates().get(1).getInitialState());
    assertEquals(ActivityState.SUSPENDED, taskInstance.getStateUpdates().get(1).getActivityState());
    assertEquals(ActivityState.SUSPENDED, taskInstance.getState());

    //Check the task has not been assigned when suspend
    assertFalse(taskInstance.isTaskAssigned());
    assertNull(taskInstance.getTaskUser());
    /*
    try {
      fail(taskInstance.getTaskUser());
    } catch (IllegalStateException ie) {
      //OK
    }
     */
    //***************** Resume the task  choosing assignTask = false **********************
    getRuntimeAPI().resumeTask(taskUUID, false);
    taskActivitiesToDo = getQueryRuntimeAPI().getTaskList(instanceUUID, ActivityState.READY);
    assertEquals(1, taskActivitiesToDo.size());
    taskInstance = taskActivitiesToDo.iterator().next();
    assertEquals(3, taskInstance.getStateUpdates().size());
    assertEquals(1, taskInstance.getAssignUpdates().size());

    //Check content of the update for the resume (StateUpdate)
    assertEquals("admin", taskInstance.getStateUpdates().get(2).getUpdatedBy());
    assertNotNull(taskInstance.getStateUpdates().get(2).getUpdatedDate());
    assertEquals(ActivityState.SUSPENDED, taskInstance.getStateUpdates().get(2).getInitialState());
    assertEquals(ActivityState.READY, taskInstance.getStateUpdates().get(2).getActivityState());
    assertEquals(ActivityState.READY, taskInstance.getState());

    //Check the task has not been assigned when suspend
    assertFalse(taskInstance.isTaskAssigned());
    assertNull(taskInstance.getTaskUser());
    /*
    try {
      fail(taskInstance.getTaskUser());
    } catch (IllegalStateException ie) {
      //OK
    }
     */

    //************* Suspend the task choosing assignTask = true ***********************
    getRuntimeAPI().suspendTask(taskUUID, true);
    taskActivitiesToDo = getQueryRuntimeAPI().getTaskList(instanceUUID, ActivityState.SUSPENDED);
    assertEquals(1, taskActivitiesToDo.size());
    taskInstance = taskActivitiesToDo.iterator().next();
    assertEquals(4, taskInstance.getStateUpdates().size());

    //Check assign update
    assertEquals(2, taskInstance.getAssignUpdates().size());

    //Check content of the update for the suspend (StateUpdate)
    assertEquals("admin", taskInstance.getStateUpdates().get(3).getUpdatedBy());
    assertNotNull(taskInstance.getStateUpdates().get(3).getUpdatedDate());
    assertEquals(ActivityState.READY, taskInstance.getStateUpdates().get(3).getInitialState());
    assertEquals(ActivityState.SUSPENDED, taskInstance.getStateUpdates().get(3).getActivityState());
    assertEquals(ActivityState.SUSPENDED, taskInstance.getState());

    //Check the task has been assigned when suspend
    assertTrue(taskInstance.isTaskAssigned());
    assertEquals("admin", taskInstance.getTaskUser());


    //***************** Resume the task  choosing assignTask = true **********************
    //****** Login John ******
    loginAs(loginJohn, passwordJohn);

    getRuntimeAPI().resumeTask(taskUUID, true);
    taskActivitiesToDo = getQueryRuntimeAPI().getTaskList(instanceUUID, ActivityState.READY);
    assertEquals(1, taskActivitiesToDo.size());
    taskInstance = taskActivitiesToDo.iterator().next();
    assertEquals(5, taskInstance.getStateUpdates().size());
    assertEquals(3, taskInstance.getAssignUpdates().size());


    //Check content of the update for the resume (StateUpdate)
    assertEquals(loginJohn, taskInstance.getStateUpdates().get(4).getUpdatedBy());
    assertNotNull(taskInstance.getStateUpdates().get(4).getUpdatedDate());
    assertEquals(ActivityState.SUSPENDED, taskInstance.getStateUpdates().get(4).getInitialState());
    assertEquals(ActivityState.READY, taskInstance.getStateUpdates().get(4).getActivityState());
    assertEquals(ActivityState.READY, taskInstance.getState());

    //Check assign update
    //TODO : complete .....

    //Check the task has been assigned when Resume
    assertTrue(taskInstance.isTaskAssigned());
    assertEquals(loginJohn, taskInstance.getTaskUser());

    //******************* start the task act2 without assign ***********************************
    getRuntimeAPI().startTask(taskUUID, false);

    //check the task is not assigned
    assertTrue(taskInstance.isTaskAssigned());

    //check stateUpdate
    taskActivitiesToDo = getQueryRuntimeAPI().getTaskList(instanceUUID, ActivityState.EXECUTING);
    assertEquals(1, taskActivitiesToDo.size());
    taskInstance = taskActivitiesToDo.iterator().next();
    assertNotNull(taskInstance.getCreatedDate());
    assertEquals(6, taskInstance.getStateUpdates().size());
    assertEquals(3, taskInstance.getAssignUpdates().size());

    //Check content of the 2 nd update (StateUpdate)
    assertEquals("john", taskInstance.getStateUpdates().get(5).getUpdatedBy());
    assertNotNull(taskInstance.getStateUpdates().get(5).getUpdatedDate());
    assertEquals(ActivityState.READY, taskInstance.getStateUpdates().get(5).getInitialState());
    assertEquals(ActivityState.EXECUTING, taskInstance.getStateUpdates().get(5).getActivityState());

    //******************* finish the task act2 without assign ***********************************
    getRuntimeAPI().finishTask(taskUUID, false);
    taskActivitiesToDo = getQueryRuntimeAPI().getTaskList(instanceUUID, ActivityState.FINISHED);
    assertEquals(1, taskActivitiesToDo.size());
    taskInstance = taskActivitiesToDo.iterator().next();
    assertEquals(7, taskInstance.getStateUpdates().size());
    assertEquals(3, taskInstance.getAssignUpdates().size());
    assertNotNull(taskInstance.getStateUpdates().get(6).getUpdatedDate());
    assertEquals(ActivityState.EXECUTING, taskInstance.getStateUpdates().get(6).getInitialState());
    assertEquals(ActivityState.FINISHED, taskInstance.getStateUpdates().get(6).getActivityState());
    assertNotNull(taskInstance.getEndedDate());

    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testUnassign() throws BonitaException {

    URL xpdlUrl = this.getClass().getResource("unassign.xpdl");
    ProcessDefinition process = getManagementAPI()
    .deploy(getBusinessArchiveFromXpdl(xpdlUrl, AssignRoleMapper.class,AdminPerformerAssign.class));

    ProcessDefinitionUUID processUUID = process.getUUID();

    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    Collection<TaskInstance> todolist = null;

    todolist = getQueryRuntimeAPI().getTaskList(ActivityState.READY);
    assertNotNull(todolist);
    assertEquals(1, todolist.size());

    ActivityInstanceUUID taskUUID = todolist.iterator().next().getUUID();

    getRuntimeAPI().assignTask(taskUUID, "unknownactor");

    todolist = getQueryRuntimeAPI().getTaskList(ActivityState.READY);
    assertNotNull(todolist);
    assertEquals(0, todolist.size());

    getRuntimeAPI().unassignTask(taskUUID);

    todolist = getQueryRuntimeAPI().getTaskList(ActivityState.READY);
    assertNotNull(todolist);
    assertEquals(1, todolist.size());

    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }


  public void testAssignTask2() throws Exception {
    final QueryRuntimeAPI queryRuntime = getQueryRuntimeAPI();
    final RuntimeAPI runtimeAPI = getRuntimeAPI();
    final ManagementAPI managementAPI = getManagementAPI();
    final String testUserId = "Nicolas";

    ProcessDefinition Process = ProcessBuilder.createProcess("MyProcess", "1.0").addHuman(getLogin()).addHumanTask("t", getLogin()).done();
    managementAPI.deploy(BusinessArchiveFactory.getBusinessArchive(Process));
    final ProcessDefinitionUUID processUUID = Process.getUUID();
    // Start a case.
    ProcessInstanceUUID processInstanceUUID = runtimeAPI.instantiateProcess(processUUID);
    Set<ActivityInstance> activities = queryRuntime.getActivityInstances(processInstanceUUID);
    TaskInstance taskInstance = activities.iterator().next().getTask();
    assertEquals(1, taskInstance.getTaskCandidates().size());
    assertEquals(getLogin(), taskInstance.getTaskCandidates().iterator().next());

    runtimeAPI.assignTask(taskInstance.getUUID(), testUserId);

    activities = queryRuntime.getActivityInstances(processInstanceUUID);
    assertEquals(1, activities.size());
    taskInstance = activities.iterator().next().getTask();				
    assertTrue(taskInstance.isTaskAssigned());
    assertEquals(testUserId, taskInstance.getTaskUser());

    HashSet<String> Candidates = new HashSet<String>();
    Candidates.add(testUserId);
    Candidates.add(getLogin());
    runtimeAPI.assignTask(taskInstance.getUUID(), Candidates);

    activities = queryRuntime.getActivityInstances(processInstanceUUID);
    taskInstance = activities.iterator().next().getTask();
    assertFalse(taskInstance.isTaskAssigned());
    assertEquals(2, taskInstance.getTaskCandidates().size());
    assertTrue(taskInstance.getTaskCandidates().contains(getLogin()));
    assertTrue(taskInstance.getTaskCandidates().contains(testUserId));

    runtimeAPI.assignTask(taskInstance.getUUID(), getLogin());

    activities = queryRuntime.getActivityInstances(processInstanceUUID);
    assertEquals(1, activities.size());
    taskInstance = activities.iterator().next().getTask();				
    assertTrue(taskInstance.isTaskAssigned());
    assertEquals(getLogin(), taskInstance.getTaskUser());

    getManagementAPI().deleteAllProcesses();
  }

}
