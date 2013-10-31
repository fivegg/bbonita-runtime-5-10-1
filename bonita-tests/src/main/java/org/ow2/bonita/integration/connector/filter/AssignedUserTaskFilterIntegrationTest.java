package org.ow2.bonita.integration.connector.filter;

import java.util.Collection;

import junit.framework.Assert;

import org.bonitasoft.connectors.bonita.filters.AssignedUserTaskFilter;
import org.bonitasoft.connectors.bonita.resolvers.UserListRoleResolver;
import org.ow2.bonita.APITestCase;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.runtime.ActivityState;
import org.ow2.bonita.facade.runtime.TaskInstance;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.util.ProcessBuilder;

public class AssignedUserTaskFilterIntegrationTest extends APITestCase {

	public void testActivityNotFound() throws Exception {
		ProcessDefinition definition = ProcessBuilder.createProcess("lastTask", "1.0")
    .addGroup("multi")
      .addGroupResolver(UserListRoleResolver.class.getName())
        .addInputParameter("setUsers", "john, james, jack")
    .addHumanTask("last", "multi")
    .addHumanTask("check", "multi")
      .addFilter(AssignedUserTaskFilter.class.getName())
        .addInputParameter("activityName", "notfound")
    .addTransition("last", "check")
    .done();

    ProcessDefinition process = getManagementAPI().deploy(getBusinessArchive(definition, null, UserListRoleResolver.class, AssignedUserTaskFilter.class));
    ProcessDefinitionUUID processUUID = process.getUUID();    
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    loginAs("john", "bpm");
    Collection<TaskInstance> tasks = 
      getQueryRuntimeAPI().getTaskList(instanceUUID, ActivityState.READY);
    Assert.assertEquals(1, tasks.size());
    TaskInstance task = tasks.iterator().next();
    getRuntimeAPI().executeTask(task.getUUID(), true);

    loginAs("jack", "bpm");
    tasks = getQueryRuntimeAPI().getTaskList(instanceUUID, ActivityState.READY);
    Assert.assertEquals(1, tasks.size());

    loginAs("james", "bpm");
    tasks = getQueryRuntimeAPI().getTaskList(instanceUUID, ActivityState.READY);
    Assert.assertEquals(1, tasks.size());

    loginAs("john", "bpm");
    tasks = getQueryRuntimeAPI().getTaskList(instanceUUID, ActivityState.READY);
    Assert.assertEquals(1, tasks.size());

    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
	}

	public void testGetUserFromLastTask() throws Exception {
		ProcessDefinition definition = ProcessBuilder.createProcess("lastTask", "1.0")
    .addGroup("multi")
      .addGroupResolver(UserListRoleResolver.class.getName())
        .addInputParameter("setUsers", "john, james, jack")
    .addHumanTask("last", "multi")
    .addHumanTask("check", "multi")
      .addFilter(AssignedUserTaskFilter.class.getName())
        .addInputParameter("activityName", "last")
    .addTransition("last", "check")
    .done();

    ProcessDefinition process = getManagementAPI().deploy(getBusinessArchive(definition, null, UserListRoleResolver.class, AssignedUserTaskFilter.class));
    ProcessDefinitionUUID processUUID = process.getUUID();    
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    loginAs("john", "bpm");
    Collection<TaskInstance> tasks = 
      getQueryRuntimeAPI().getTaskList(instanceUUID, ActivityState.READY);
    Assert.assertEquals(1, tasks.size());
    TaskInstance task = tasks.iterator().next();

    getRuntimeAPI().executeTask(task.getUUID(), true);

    loginAs("jack", "bpm");
    tasks = getQueryRuntimeAPI().getTaskList(instanceUUID, ActivityState.READY);
    Assert.assertEquals(0, tasks.size());

    loginAs("james", "bpm");
    tasks = getQueryRuntimeAPI().getTaskList(instanceUUID, ActivityState.READY);
    Assert.assertEquals(0, tasks.size());

    loginAs("john", "bpm");
    tasks = getQueryRuntimeAPI().getTaskList(instanceUUID, ActivityState.READY);
    Assert.assertEquals(1, tasks.size());

    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
	}

	public void testChooseUserFromAnotherGroup() throws Exception {
		ProcessDefinition definition = ProcessBuilder.createProcess("lastTask", "1.0")
    .addHuman("admin")
		.addGroup("multi")
      .addGroupResolver(UserListRoleResolver.class.getName())
        .addInputParameter("setUsers", "john, james, jack")
    .addHumanTask("last", getLogin())
    .addHumanTask("check", "multi")
      .addFilter(AssignedUserTaskFilter.class.getName())
        .addInputParameter("activityName", "last")
    .addTransition("last", "check")
    .done();

    ProcessDefinition process = getManagementAPI().deploy(getBusinessArchive(definition, null, UserListRoleResolver.class, AssignedUserTaskFilter.class));
    ProcessDefinitionUUID processUUID = process.getUUID();    
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    Collection<TaskInstance> tasks = 
      getQueryRuntimeAPI().getTaskList(instanceUUID, ActivityState.READY);
    Assert.assertEquals(1, tasks.size());
    TaskInstance task = tasks.iterator().next();

    getRuntimeAPI().executeTask(task.getUUID(), true);

    loginAs("jack", "bpm");
    tasks = getQueryRuntimeAPI().getTaskList(instanceUUID, ActivityState.READY);
    Assert.assertEquals(0, tasks.size());

    loginAs("james", "bpm");
    tasks = getQueryRuntimeAPI().getTaskList(instanceUUID, ActivityState.READY);
    Assert.assertEquals(0, tasks.size());

    loginAs("john", "bpm");
    tasks = getQueryRuntimeAPI().getTaskList(instanceUUID, ActivityState.READY);
    Assert.assertEquals(0, tasks.size());

    loginAs(getLogin(), getPassword());
    tasks = getQueryRuntimeAPI().getTaskList(instanceUUID, ActivityState.READY);
    Assert.assertEquals(1, tasks.size());

    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
	}
	
	public void testGetUserFromNextTask() throws Exception {
		ProcessDefinition definition = ProcessBuilder.createProcess("lastTask", "1.0")
    .addGroup("multi")
      .addGroupResolver(UserListRoleResolver.class.getName())
        .addInputParameter("setUsers", "john, james, jack")
    .addHumanTask("last", "multi")
    .addHumanTask("check", "multi")
      .addFilter(AssignedUserTaskFilter.class.getName())
        .addInputParameter("activityName", "next")
    .addHumanTask("next", "admin")
    .addTransition("last", "check")
    .addTransition("check", "next")
    .done();

    ProcessDefinition process = getManagementAPI().deploy(getBusinessArchive(definition, null, UserListRoleResolver.class, AssignedUserTaskFilter.class));
    ProcessDefinitionUUID processUUID = process.getUUID();    
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    loginAs("john", "bpm");
    Collection<TaskInstance> tasks = 
      getQueryRuntimeAPI().getTaskList(instanceUUID, ActivityState.READY);
    Assert.assertEquals(1, tasks.size());
    TaskInstance task = tasks.iterator().next();

    getRuntimeAPI().executeTask(task.getUUID(), true);

    loginAs("jack", "bpm");
    tasks = getQueryRuntimeAPI().getTaskList(instanceUUID, ActivityState.READY);
    Assert.assertEquals(1, tasks.size());

    loginAs("james", "bpm");
    tasks = getQueryRuntimeAPI().getTaskList(instanceUUID, ActivityState.READY);
    Assert.assertEquals(1, tasks.size());

    loginAs("john", "bpm");
    tasks = getQueryRuntimeAPI().getTaskList(instanceUUID, ActivityState.READY);
    Assert.assertEquals(1, tasks.size());

    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
	}
}
