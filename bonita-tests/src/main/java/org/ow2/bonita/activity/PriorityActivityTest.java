package org.ow2.bonita.activity;

import java.util.Locale;
import java.util.Set;

import junit.framework.Assert;

import org.ow2.bonita.APITestCase;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.runtime.TaskInstance;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.util.Misc;
import org.ow2.bonita.util.ProcessBuilder;

public class PriorityActivityTest extends APITestCase {

	public void testWithoutPriority() throws Exception {
		ProcessDefinition definition =
			ProcessBuilder.createProcess("without", "1.0")
			  .addHuman(getLogin())
			  .addHumanTask("first", getLogin())
			.done();

		ProcessDefinition process = getManagementAPI().deploy(getBusinessArchive(definition));
		ProcessDefinitionUUID processUUID = process.getUUID();

		ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);
		Set<TaskInstance> tasks = getQueryRuntimeAPI().getTasks(instanceUUID);
		TaskInstance task = tasks.iterator().next();
		int priority = task.getPriority();
		String actual = Misc.getActivityPriority(priority, Locale.ENGLISH);
		Assert.assertEquals("Normal", actual);

		getRuntimeAPI().deleteProcessInstance(instanceUUID);
		getManagementAPI().deleteProcess(processUUID);
	}

	public void testWithPriorityZero() throws Exception {
		ProcessDefinition definition =
			ProcessBuilder.createProcess("without", "1.0")
			  .addHuman(getLogin())
			  .addHumanTask("first", getLogin())
			    .addActivityPriority(0)
			.done();

		ProcessDefinition process = getManagementAPI().deploy(getBusinessArchive(definition));
		ProcessDefinitionUUID processUUID = process.getUUID();

		ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);
		Set<TaskInstance> tasks = getQueryRuntimeAPI().getTasks(instanceUUID);
		TaskInstance task = tasks.iterator().next();
		int priority = task.getPriority();
		String actual = Misc.getActivityPriority(priority, Locale.ENGLISH);
		Assert.assertEquals("Normal", actual);

		getRuntimeAPI().deleteProcessInstance(instanceUUID);
		getManagementAPI().deleteProcess(processUUID);
	}

	public void testWithPriorityOne() throws Exception {
		ProcessDefinition definition =
			ProcessBuilder.createProcess("without", "1.0")
			  .addHuman(getLogin())
			  .addHumanTask("first", getLogin())
			    .addActivityPriority(1)
			.done();

		ProcessDefinition process = getManagementAPI().deploy(getBusinessArchive(definition));
		ProcessDefinitionUUID processUUID = process.getUUID();

		ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);
		Set<TaskInstance> tasks = getQueryRuntimeAPI().getTasks(instanceUUID);
		TaskInstance task = tasks.iterator().next();
		int priority = task.getPriority();
		String actual = Misc.getActivityPriority(priority, Locale.ENGLISH);
		Assert.assertEquals("High", actual);

		getRuntimeAPI().deleteProcessInstance(instanceUUID);
		getManagementAPI().deleteProcess(processUUID);
	}
	
	public void testWithPriorityTwo() throws Exception {
		ProcessDefinition definition =
			ProcessBuilder.createProcess("without", "1.0")
			  .addHuman(getLogin())
			  .addHumanTask("first", getLogin())
			    .addActivityPriority(2)
			.done();

		ProcessDefinition process = getManagementAPI().deploy(getBusinessArchive(definition));
		ProcessDefinitionUUID processUUID = process.getUUID();

		ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);
		Set<TaskInstance> tasks = getQueryRuntimeAPI().getTasks(instanceUUID);
		TaskInstance task = tasks.iterator().next();
		int priority = task.getPriority();
		String actual = Misc.getActivityPriority(priority, Locale.ENGLISH);
		Assert.assertEquals("Urgent", actual);

		getRuntimeAPI().deleteProcessInstance(instanceUUID);
		getManagementAPI().deleteProcess(processUUID);
	}
	
	public void testWithPriorityThree() throws Exception {
		ProcessDefinition definition =
			ProcessBuilder.createProcess("without", "1.0")
			  .addHuman(getLogin())
			  .addHumanTask("first", getLogin())
			    .addActivityPriority(3)
			.done();

		ProcessDefinition process = getManagementAPI().deploy(getBusinessArchive(definition));
		ProcessDefinitionUUID processUUID = process.getUUID();

		ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);
		Set<TaskInstance> tasks = getQueryRuntimeAPI().getTasks(instanceUUID);
		TaskInstance task = tasks.iterator().next();
		int priority = task.getPriority();
		String actual = Misc.getActivityPriority(priority, Locale.ENGLISH);
		Assert.assertEquals("Normal", actual);

		getRuntimeAPI().deleteProcessInstance(instanceUUID);
		getManagementAPI().deleteProcess(processUUID);
	}
	
	public void testWithPriorityFour() throws Exception {
		ProcessDefinition definition =
			ProcessBuilder.createProcess("without", "1.0")
			  .addHuman(getLogin())
			  .addHumanTask("first", getLogin())
			    .addActivityPriority(4)
			.done();

		ProcessDefinition process = getManagementAPI().deploy(getBusinessArchive(definition));
		ProcessDefinitionUUID processUUID = process.getUUID();

		ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);
		Set<TaskInstance> tasks = getQueryRuntimeAPI().getTasks(instanceUUID);
		TaskInstance task = tasks.iterator().next();
		int priority = task.getPriority();
		String actual = Misc.getActivityPriority(priority, Locale.ENGLISH);
		Assert.assertEquals("Normal", actual);

		getRuntimeAPI().deleteProcessInstance(instanceUUID);
		getManagementAPI().deleteProcess(processUUID);
	}
	
	public void testWithPriorityFive() throws Exception {
		ProcessDefinition definition =
			ProcessBuilder.createProcess("without", "1.0")
			  .addHuman(getLogin())
			  .addHumanTask("first", getLogin())
			    .addActivityPriority(5)
			.done();

		ProcessDefinition process = getManagementAPI().deploy(getBusinessArchive(definition));
		ProcessDefinitionUUID processUUID = process.getUUID();

		ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);
		Set<TaskInstance> tasks = getQueryRuntimeAPI().getTasks(instanceUUID);
		TaskInstance task = tasks.iterator().next();
		int priority = task.getPriority();
		String actual = Misc.getActivityPriority(priority, Locale.ENGLISH);
		Assert.assertEquals("Normal", actual);

		getRuntimeAPI().deleteProcessInstance(instanceUUID);
		getManagementAPI().deleteProcess(processUUID);
	}
}
