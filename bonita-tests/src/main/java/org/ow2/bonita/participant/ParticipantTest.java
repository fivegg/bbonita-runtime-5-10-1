package org.ow2.bonita.participant;

import java.net.URL;
import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Level;

import org.ow2.bonita.APITestCase;
import org.ow2.bonita.definition.VariablePerformerAssign;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.runtime.ActivityState;
import org.ow2.bonita.facade.runtime.TaskInstance;
import org.ow2.bonita.facade.uuid.ActivityInstanceUUID;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.util.BonitaException;

public class ParticipantTest extends APITestCase {

  /*
   *  test role mapper with type = Custom
   */
  public void testRoleMapperCustom() throws BonitaException {
    URL xpdlUrl = this.getClass().getResource("roleMapperCustom.xpdl");
    ProcessDefinition process = getManagementAPI().deploy(getBusinessArchiveFromXpdl(xpdlUrl,
        AdminRoleMapper.class));
    
    ProcessDefinitionUUID processUUID = process.getUUID();

    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    //get task
    Collection<TaskInstance> taskActivities = 
      getQueryRuntimeAPI().getTaskList(instanceUUID, ActivityState.READY);
    assertNotNull(taskActivities);
    assertEquals(1, taskActivities.size());
    for (TaskInstance taskActivity : taskActivities) {
      TaskInstance task = taskActivity;
      getRuntimeAPI().startTask(task.getUUID(), true);
      if (LOG.isLoggable(Level.FINE)) {
        LOG.fine("Starting task: " + task);
      }
      getRuntimeAPI().finishTask(task.getUUID(), true);
      if (LOG.isLoggable(Level.FINE)) {
        LOG.fine("Task " + task + " terminated.");
      }
    }
    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  /*
   *   Test Performer Assignment with type = Callback
   */
  public void testPerformerAssignmentCallback() throws BonitaException {
    URL xpdlUrl = this.getClass().getResource("performerAssignmentCallback.xpdl");
    ProcessDefinition process = getManagementAPI().deploy(getBusinessArchiveFromXpdl(xpdlUrl,
        AdminPerformerAssign.class));
    
    ProcessDefinitionUUID processUUID = process.getUUID();

    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    //get task
    Collection<TaskInstance> taskActivities = 
      getQueryRuntimeAPI().getTaskList(instanceUUID, ActivityState.READY);
    assertNotNull(taskActivities);
    assertEquals(1, taskActivities.size());
    for (TaskInstance taskActivity : taskActivities) {
      TaskInstance task = taskActivity;
      getRuntimeAPI().startTask(task.getUUID(), true);
      if (LOG.isLoggable(Level.FINE)) {
        LOG.fine("Starting task: " + task);
      }
      getRuntimeAPI().finishTask(task.getUUID(), true);
      if (LOG.isLoggable(Level.FINE)) {
        LOG.fine("Task " + task + " terminated.");
      }
    }
    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testPerformerAssignmentGlobalVariable() throws BonitaException {
    URL xpdlUrl = this.getClass().getResource("performerAssignmentGlobalVariable.xpdl");
    ProcessDefinition process = getManagementAPI().deploy(getBusinessArchiveFromXpdl(xpdlUrl,
        VariablePerformerAssign.class));
    
    ProcessDefinitionUUID processUUID = process.getUUID();

    assertTrue(getQueryRuntimeAPI().getTaskList(getLogin(), ActivityState.READY).isEmpty());

    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    //get task
    Collection<TaskInstance> taskActivities = 
      getQueryRuntimeAPI().getTaskList(instanceUUID, ActivityState.READY);
    assertNotNull(taskActivities);
    assertEquals(1, taskActivities.size());
    for (TaskInstance taskActivity : taskActivities) {
      TaskInstance task = taskActivity;
      getRuntimeAPI().startTask(task.getUUID(), true);
      if (LOG.isLoggable(Level.FINE)) {
        LOG.fine("Starting task: " + task);
      }
      getRuntimeAPI().finishTask(task.getUUID(), true);
      if (LOG.isLoggable(Level.FINE)) {
        LOG.fine("Task " + task + " terminated.");
      }
    }
    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testGetTaskList() throws BonitaException {
    URL xpdlUrl = this.getClass().getResource("getTaskList.xpdl");
    ProcessDefinition process = getManagementAPI().deploy(getBusinessArchiveFromXpdl(xpdlUrl,
        VariablePerformerAssign.class));
    
    ProcessDefinitionUUID processUUID = process.getUUID();

    assertTrue(getQueryRuntimeAPI().getTaskList(getLogin(), ActivityState.READY).isEmpty());

    //create 3 instances
    ProcessInstanceUUID instance1UUID = getRuntimeAPI().instantiateProcess(processUUID);
    ProcessInstanceUUID instance2UUID = getRuntimeAPI().instantiateProcess(processUUID);
    ProcessInstanceUUID instance3UUID = getRuntimeAPI().instantiateProcess(processUUID);
    
    assertFalse(getQueryRuntimeAPI().getTaskList(getLogin(), ActivityState.READY).isEmpty());
    assertEquals(3, getQueryRuntimeAPI().getTaskList(getLogin(), ActivityState.READY).size());
    
    //get task uuids
    Collection<TaskInstance> instance1Tasks = 
      getQueryRuntimeAPI().getTaskList(instance1UUID, ActivityState.READY); 
    assertEquals(1, instance1Tasks.size());
    TaskInstance task1 = instance1Tasks.iterator().next();
    ActivityInstanceUUID task1UUID = task1.getUUID();
    
    Collection<TaskInstance> instance2Tasks = 
      getQueryRuntimeAPI().getTaskList(instance2UUID, ActivityState.READY); 
    assertEquals(1, instance2Tasks.size());
    TaskInstance task2 = instance2Tasks.iterator().next();
    ActivityInstanceUUID task2UUID = task2.getUUID();
    
    Collection<TaskInstance> instance3Tasks = 
      getQueryRuntimeAPI().getTaskList(instance3UUID, ActivityState.READY); 
    assertEquals(1, instance3Tasks.size());
    TaskInstance task3 = instance3Tasks.iterator().next();
    ActivityInstanceUUID task3UUID = task3.getUUID();
    
    //assign tasks
    getRuntimeAPI().assignTask(task1UUID, "m1");
    getRuntimeAPI().assignTask(task2UUID, "m1");
    getRuntimeAPI().assignTask(task3UUID, "m2");
    
    //verify tasks are well assigned
    assertEquals("m1", getQueryRuntimeAPI().getTask(task1UUID).getTaskUser());
    assertEquals("m1", getQueryRuntimeAPI().getTask(task2UUID).getTaskUser());
    assertEquals("m2", getQueryRuntimeAPI().getTask(task3UUID).getTaskUser());
    
    //check m1 user has 2 tasks
    Collection<TaskInstance> m1Tasks = getQueryRuntimeAPI().getTaskList("m1", ActivityState.READY);
    assertEquals(2, m1Tasks.size());
    Iterator<TaskInstance> it = m1Tasks.iterator();
    boolean task1OK = false;
    boolean task2OK = false;
    while (it.hasNext()) {
      ActivityInstanceUUID taskUUID = it.next().getUUID();
      if (taskUUID.equals(task1UUID)) {
        task1OK = true;
      } else if (taskUUID.equals(task2UUID)) {
        task2OK = true;
      } else {
        fail("Wrong taskUUID");
      }
    }
    assertTrue(task1OK && task2OK);
    
    //check m1 user has only 1 task for instance1
    Collection<TaskInstance> m1TasksForInstance1 = 
      getQueryRuntimeAPI().getTaskList(instance1UUID, "m1", ActivityState.READY);
    assertEquals(1, m1TasksForInstance1.size());
    assertEquals(task1UUID, m1TasksForInstance1.iterator().next().getUUID());
    
    //check m1 user has only 1 task for instance2
    Collection<TaskInstance> m1TasksForInstance2 = 
      getQueryRuntimeAPI().getTaskList(instance2UUID, "m1", ActivityState.READY);
    assertEquals(1, m1TasksForInstance2.size());
    assertEquals(task2UUID, m1TasksForInstance2.iterator().next().getUUID());
    
    //check m1 user has 2 tasks
    Collection<TaskInstance> m2Tasks = getQueryRuntimeAPI().getTaskList("m2", ActivityState.READY);
    assertEquals(1, m2Tasks.size());
    assertEquals(task3UUID, m2Tasks.iterator().next().getUUID());
    
    //check m1 user has only 1 task for instance3
    m2Tasks = getQueryRuntimeAPI().getTaskList(instance3UUID, "m2", ActivityState.READY);
    assertEquals(1, m2Tasks.size());
    assertEquals(task3UUID, m2Tasks.iterator().next().getUUID());
    
    //check
    assertTrue(getQueryRuntimeAPI().getTaskList(instance1UUID, "m2", ActivityState.READY).isEmpty());
    assertTrue(getQueryRuntimeAPI().getTaskList(instance2UUID, "m2", ActivityState.READY).isEmpty());
    assertTrue(getQueryRuntimeAPI().getTaskList(instance3UUID, "m1", ActivityState.READY).isEmpty());
    
    getRuntimeAPI().deleteProcessInstance(instance1UUID);
    getRuntimeAPI().deleteProcessInstance(instance2UUID);
    getRuntimeAPI().deleteProcessInstance(instance3UUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }
}
