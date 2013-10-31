package org.ow2.bonita.activity.subflow;

import java.net.URL;
import java.util.Collection;
import java.util.Iterator;

import org.ow2.bonita.APITestCase;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.runtime.ActivityState;
import org.ow2.bonita.facade.runtime.InstanceState;
import org.ow2.bonita.facade.runtime.ProcessInstance;
import org.ow2.bonita.facade.runtime.TaskInstance;
import org.ow2.bonita.facade.uuid.ActivityInstanceUUID;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.util.BonitaException;

public class SubflowJoinXorTest extends APITestCase {


  /**
  * Automatic process that creates a subflow with a Manual activity.
  */
  public void testSubFlowWithTaskCanceledByJoinXor() throws BonitaException {
    final URL xpdlUrlSubflow = this.getClass().getResource("subflowWithTask.xpdl");
    
    final ProcessDefinition subProcess = getManagementAPI().deploy(getBusinessArchiveFromXpdl(xpdlUrlSubflow));
    final ProcessDefinitionUUID subProcessUUID = subProcess.getUUID();

    final URL xpdlUrl = this.getClass().getResource("mainSubflowJoinXor.xpdl");
    final ProcessDefinition process = getManagementAPI().deploy(getBusinessArchiveFromXpdl(xpdlUrl));
    final ProcessDefinitionUUID processUUID = process.getUUID();
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);
    // check the two subflows have been instantiated

    final Collection<TaskInstance> taskActivities = getQueryRuntimeAPI().getTaskList(ActivityState.READY);
    // one task for each subflow
    assertEquals(2, taskActivities.size());
    final Iterator<TaskInstance> it = taskActivities.iterator();
    final TaskInstance taskActivity = it.next();

    // execute one task
    final ProcessInstance processInstance = getQueryRuntimeAPI().getProcessInstance(taskActivity.getProcessInstanceUUID());
    assertEquals(instanceUUID, processInstance.getParentInstanceUUID());
    final ActivityInstanceUUID taskUUID = taskActivity.getUUID();
    getRuntimeAPI().startTask(taskUUID, true);
    getRuntimeAPI().finishTask(taskUUID, true);

    // check we have reached a wait state
    final Collection<TaskInstance> newTaskActivities = getQueryRuntimeAPI().getTaskList(instanceUUID, ActivityState.READY);
    assertEquals(1, newTaskActivities.size());
    final TaskInstance waitStateActivity = newTaskActivities.iterator().next();
    assertEquals("waitState", waitStateActivity.getActivityName());

    // check other subprocess is aborted
    final TaskInstance otherTaskActivity = it.next();
    final ProcessInstance otherInstance = getQueryRuntimeAPI().getProcessInstance(otherTaskActivity.getProcessInstanceUUID());
    assertEquals(InstanceState.ABORTED, otherInstance.getInstanceState());
    // check task is aborted
    assertEquals(ActivityState.ABORTED, getQueryRuntimeAPI().getTask(otherTaskActivity.getUUID()).getState());


    // execute the wait state task
    getRuntimeAPI().startTask(waitStateActivity.getUUID(), true);
    getRuntimeAPI().finishTask(waitStateActivity.getUUID(), true);

    // check main process is finished
    checkExecutedOnce(instanceUUID, new String[]{"join", "BonitaEnd"});


    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().disable(subProcessUUID);

    getManagementAPI().deleteProcess(processUUID);
    getManagementAPI().deleteProcess(subProcessUUID);
  }

}
