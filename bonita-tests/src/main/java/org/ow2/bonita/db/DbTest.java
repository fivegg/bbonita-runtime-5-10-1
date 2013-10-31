package org.ow2.bonita.db;

import java.net.URL;
import java.util.Collection;

import javax.security.auth.login.LoginException;

import org.ow2.bonita.APITestCase;
import org.ow2.bonita.example.aw.ApprovalWorkflow;
import org.ow2.bonita.example.aw.hook.Accept;
import org.ow2.bonita.example.aw.hook.Reject;
import org.ow2.bonita.example.aw.mapper.ApprovalMapper;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.runtime.ActivityState;
import org.ow2.bonita.facade.runtime.TaskInstance;
import org.ow2.bonita.facade.uuid.ActivityInstanceUUID;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.util.BonitaException;

public class DbTest extends APITestCase {

	//try to reproduce a problem reported by the community: http://forge.ow2.org/forum/forum.php?thread_id=6496&forum_id=154
	public void testMysql() throws Exception {
		final URL xpdlUrl = this.getClass().getResource("db-mysql-1.xpdl");
		final ProcessDefinition process = this.getManagementAPI().deploy(getBusinessArchiveFromXpdl(xpdlUrl));
		final ProcessDefinitionUUID processUUID = process.getUUID();

		//process is: init -> submit (admin) -> approve (john) -> end

		ProcessInstanceUUID instance1 = execute(processUUID);
		ProcessInstanceUUID instance2 = execute(processUUID);
		ProcessInstanceUUID instance3 = execute(processUUID);
		ProcessInstanceUUID instance4 = execute(processUUID);

		this.getRuntimeAPI().deleteProcessInstance(instance1);
		this.getRuntimeAPI().deleteProcessInstance(instance2);
		this.getRuntimeAPI().deleteProcessInstance(instance3);
		this.getRuntimeAPI().deleteProcessInstance(instance4);
		this.getManagementAPI().deleteProcess(processUUID);
	}

	private ProcessInstanceUUID execute(final ProcessDefinitionUUID processUUID) throws BonitaException, LoginException {
		loginAs("admin", "bpm");
		Collection<TaskInstance> activities = null;

		//create instance
		final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

		//get submit task
		activities = getQueryRuntimeAPI().getTaskList(instanceUUID, ActivityState.READY);
		assertNotNull("task list is null", activities);
		assertFalse("No task found? Bad User?", activities.isEmpty());
		assertEquals("Many tasks found, impossible.", 1, activities.size());
		ActivityInstanceUUID submitTaskUUID = activities.iterator().next().getUUID();

		//execute submit task
		getRuntimeAPI().startTask(submitTaskUUID, true);
		getRuntimeAPI().finishTask(submitTaskUUID, true);

		/*
		loginAs("john", "bpm");

		//get approve task
		activities = getQueryRuntimeAPI().getTaskList(instanceUUID, ActivityState.READY);
		assertNotNull("task list is null", activities);
		assertFalse("No task found? Bad User?", activities.isEmpty());
		assertEquals("Many tasks found, impossible.", 1, activities.size());
		TaskInstance approveTask = activities.iterator().next();
		ActivityInstanceUUID approveTaskUUID = approveTask.getUUID();

		//execute approve task
		getRuntimeAPI().startTask(approveTaskUUID, true);
		getRuntimeAPI().setProcessInstanceVariable(instanceUUID, "name", "testing");
		getRuntimeAPI().setProcessInstanceVariable(instanceUUID, "project", "testing");
		getRuntimeAPI().setProcessInstanceVariable(instanceUUID, "comments", "testing");
		getRuntimeAPI().finishTask(approveTaskUUID, true);
		 */
		return instanceUUID;
	}

	//try to reproduce a problem reported by the community at
	//http://forge.ow2.org/forum/forum.php?thread_id=6619&forum_id=154
	public void testConstraintViolationOracle() throws Exception {
    URL url = ApprovalWorkflow.class.getResource("ApprovalWorkflow.xpdl");
		loginAs("admin", "bpm");
		ProcessDefinition process = getManagementAPI().deploy(getBusinessArchiveFromXpdl(
		    url, Accept.class, Reject.class, ApprovalMapper.class));
		ProcessDefinitionUUID processUUID = process.getUUID();
		ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);
		getRuntimeAPI().deleteProcessInstance(instanceUUID);
		getManagementAPI().disable(processUUID);
		getManagementAPI().deleteProcess(processUUID);
	}
}