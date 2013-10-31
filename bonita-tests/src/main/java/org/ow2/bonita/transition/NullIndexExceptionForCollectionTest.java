package org.ow2.bonita.transition;

import java.util.Set;

import org.ow2.bonita.APITestCase;
import org.ow2.bonita.facade.QueryRuntimeAPI;
import org.ow2.bonita.facade.RuntimeAPI;
import org.ow2.bonita.facade.def.element.BusinessArchive;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.exception.ActivityNotFoundException;
import org.ow2.bonita.facade.exception.IllegalTaskStateException;
import org.ow2.bonita.facade.exception.InstanceNotFoundException;
import org.ow2.bonita.facade.exception.TaskNotFoundException;
import org.ow2.bonita.facade.runtime.ActivityInstance;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.util.BusinessArchiveFactory;

public class NullIndexExceptionForCollectionTest extends APITestCase {

	public void testNullIndexForCollection1() throws Exception {
		nullIndexForCollection(getBusinessArchive1());
	}
	
	public void testNullIndexForCollection2() throws Exception {
		nullIndexForCollection(getBusinessArchive2());
	}
	
	public void testNullIndexForCollection3() throws Exception {
		nullIndexForCollection(getBusinessArchive3());
	}
	
	
	private void nullIndexForCollection(final BusinessArchive businessArchive) throws Exception {

			getManagementAPI().deleteAllProcesses();
			
			final ProcessDefinitionUUID processUUID = getManagementAPI().deploy(businessArchive).getUUID();

			final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

			executeTask(getQueryRuntimeAPI(), getRuntimeAPI(), instanceUUID, "Step4");			

			getManagementAPI().deleteProcess(processUUID);
	}
	
	private static void executeTask(final QueryRuntimeAPI queryRuntimeAPI, final RuntimeAPI runtimeAPI, final ProcessInstanceUUID instanceUUID, final String activityName) throws InstanceNotFoundException, ActivityNotFoundException, TaskNotFoundException, IllegalTaskStateException {
		final ActivityInstance activity = getActivityInstance(queryRuntimeAPI, instanceUUID, activityName);
		runtimeAPI.executeTask(activity.getUUID(), true);
	}
	
	private static ActivityInstance getActivityInstance(final QueryRuntimeAPI queryRuntimeAPI, final ProcessInstanceUUID instanceUUID, final String activityName) throws InstanceNotFoundException, ActivityNotFoundException {
		final Set<ActivityInstance> activities = queryRuntimeAPI.getActivityInstances(instanceUUID, activityName);
		assertEquals("number of activity instances", 1, activities.size());
		
		final ActivityInstance activity = activities.iterator().next();
		return activity;
	}
	
	private static BusinessArchive getBusinessArchive1() throws Exception {
		final ProcessDefinition process = org.ow2.bonita.util.ProcessBuilder.createProcess("p", "1.0")
				.addHuman("admin")
				.addSystemTask("Step1")
				.addSystemTask("Step2")
				.addHumanTask("Step4", "admin")
				.addTransition("Step1", "Step2")
				.addTransition("Step2", "Step4")
				.addTransition("Step1", "Step4")
				.done();
		final BusinessArchive businessArchive = BusinessArchiveFactory.getBusinessArchive(process, (Class<?>) null);
		return businessArchive;
	}
	
	private static BusinessArchive getBusinessArchive2() throws Exception {
		final ProcessDefinition process = org.ow2.bonita.util.ProcessBuilder.createProcess("p", "1.0")
				.addHuman("admin")
				.addSystemTask("Step1")
				.addSystemTask("Step2")
				.addSystemTask("Step3")
				.addHumanTask("Step4", "admin")
				.addTransition("Step1", "Step2")
				.addTransition("Step1", "Step3")
				.addTransition("Step2", "Step4")
				.addTransition("Step3", "Step4")
				.done();
		final BusinessArchive businessArchive = BusinessArchiveFactory.getBusinessArchive(process, (Class<?>) null);
		return businessArchive;
	}
	
	private static BusinessArchive getBusinessArchive3() throws Exception {
		final ProcessDefinition process = org.ow2.bonita.util.ProcessBuilder.createProcess("p", "1.0")
				.addHuman("admin")
				.addSystemTask("Step1")
				.addHumanTask("Step4", "admin")
				.addTransition("Haut_1_4", "Step1", "Step4")
				.addTransition("Bas_1_4", "Step1", "Step4")
				.done();
		final BusinessArchive businessArchive = BusinessArchiveFactory.getBusinessArchive(process, (Class<?>) null);
		return businessArchive;
	}

}
