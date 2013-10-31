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
package org.ow2.bonita.facade;

import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.ow2.bonita.APITestCase;
import org.ow2.bonita.facade.def.majorElement.ActivityDefinition;
import org.ow2.bonita.facade.def.majorElement.DataFieldDefinition;
import org.ow2.bonita.facade.def.majorElement.ParticipantDefinition;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition.ProcessState;
import org.ow2.bonita.facade.exception.ActivityDefNotFoundException;
import org.ow2.bonita.facade.exception.ActivityNotFoundException;
import org.ow2.bonita.facade.exception.DataFieldNotFoundException;
import org.ow2.bonita.facade.exception.ParticipantNotFoundException;
import org.ow2.bonita.facade.exception.ProcessNotFoundException;
import org.ow2.bonita.facade.uuid.ActivityDefinitionUUID;
import org.ow2.bonita.facade.uuid.IdFactory;
import org.ow2.bonita.facade.uuid.ParticipantDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.light.LightProcessDefinition;
import org.ow2.bonita.services.record.DefHook1;
import org.ow2.bonita.services.record.DefHook2;
import org.ow2.bonita.util.BonitaException;

/**
 * @author Pierre Vigneras
 */
public class DefinitionAPITest extends APITestCase {

	private static final String  PROCESS1_ID = "definitionTest";
	private static final String  PROCESS1_NAME = "definitionTest";
	private static final String  PROCESS2_ID = "definitionTest2";


	public DefinitionAPITest() {
		super();
	}

	public void testDefinitionAPI() throws BonitaException {
		ProcessDefinition p1 = getManagementAPI().deploy(getBusinessArchiveFromXpdl(
				this.getClass().getResource("definitionTest-1.xpdl"), DefHook1.class, DefHook2.class, AdminsRoleMapper.class));

		ProcessDefinition p2 = getManagementAPI().deploy(getBusinessArchiveFromXpdl(
				this.getClass().getResource("definitionTest-2.xpdl"), DefHook1.class, DefHook2.class, AdminsRoleMapper.class));

		final QueryDefinitionAPI api = getQueryDefinitionAPI();

		Set<ProcessDefinition> deployedProcesses = api.getProcesses(ProcessState.ENABLED);
		assertNotNull(deployedProcesses);
		assertEquals(2, deployedProcesses.size());

		ProcessDefinition process1 = api.getProcesses(PROCESS1_ID, ProcessState.ENABLED).iterator().next();
		assertNotNull(process1);
		assertNotNull(process1.getUUID());

		ProcessDefinition process2 = api.getProcesses(PROCESS2_ID, ProcessState.ENABLED).iterator().next();
		assertNotNull(process2);
		assertNotNull(process2.getUUID());

		//assert process when deployed
		assertProcesses(api, false);

		//assert participants
		assertParticipants(api, p1.getUUID());

		//Undeploy
		getManagementAPI().disable(p1.getUUID());
		getManagementAPI().disable(p2.getUUID());
		
		getManagementAPI().archive(p1.getUUID());
		getManagementAPI().archive(p2.getUUID());

		deployedProcesses = api.getProcesses(ProcessState.ENABLED);
		assertNotNull(deployedProcesses);
		assertEquals(0, deployedProcesses.size());
		assertEquals(0, api.getProcesses(PROCESS1_ID, ProcessState.ENABLED).size());

		deployedProcesses = api.getProcesses(ProcessState.ENABLED);
		assertNotNull(deployedProcesses);
		assertEquals(0, deployedProcesses.size());
		assertEquals(0, api.getProcesses(PROCESS2_ID, ProcessState.ENABLED).size());

		//assert package when deployed
		assertProcesses(api, true);

		getManagementAPI().deleteProcess(p1.getUUID());
		getManagementAPI().deleteProcess(p2.getUUID());
	}
	
	public void testGetAllLightProcessesExcept() throws BonitaException {
		ProcessDefinition p1 = getManagementAPI().deploy(getBusinessArchiveFromXpdl(
				this.getClass().getResource("definitionTest-1.xpdl"), DefHook1.class, DefHook2.class, AdminsRoleMapper.class));

		ProcessDefinition p2 = getManagementAPI().deploy(getBusinessArchiveFromXpdl(
				this.getClass().getResource("definitionTest-2.xpdl"), DefHook1.class, DefHook2.class, AdminsRoleMapper.class));

		final QueryDefinitionAPI api = getQueryDefinitionAPI();

		List<LightProcessDefinition> deployedProcesses = api.getAllLightProcessesExcept(new HashSet<ProcessDefinitionUUID>(), 0, api.getNumberOfProcesses());
		assertNotNull(deployedProcesses);
		assertEquals(2, deployedProcesses.size());

		deployedProcesses = api.getAllLightProcessesExcept(new HashSet<ProcessDefinitionUUID>(Arrays.asList(p2.getUUID())), 0, api.getNumberOfProcesses());
		assertNotNull(deployedProcesses);
		assertEquals(1, deployedProcesses.size());
		assertEquals(p1.getUUID(), deployedProcesses.iterator().next().getUUID());
		
		deployedProcesses = api.getAllLightProcessesExcept(new HashSet<ProcessDefinitionUUID>(Arrays.asList(p1.getUUID())), 0, api.getNumberOfProcesses());
		assertNotNull(deployedProcesses);
		assertEquals(1, deployedProcesses.size());
		assertEquals(p2.getUUID(), deployedProcesses.iterator().next().getUUID());
		
		deployedProcesses = api.getAllLightProcessesExcept(new HashSet<ProcessDefinitionUUID>(Arrays.asList(p2.getUUID(),p1.getUUID())), 0, api.getNumberOfProcesses());
		assertNotNull(deployedProcesses);
		assertEquals(0, deployedProcesses.size());
				
		getManagementAPI().deleteProcess(p1.getUUID());
		getManagementAPI().deleteProcess(p2.getUUID());
	}

	private void assertProcesses(QueryDefinitionAPI api, boolean undeployed)
	  throws ProcessNotFoundException, ActivityNotFoundException, DataFieldNotFoundException {
		try {
			api.getLastProcess("bidon");
		} catch (ProcessNotFoundException pe) {
			// OK
		} 
		Set<ProcessDefinition> processes = api.getProcesses();

		assertEquals(processes.toString(), 2, processes.size());
		Iterator<ProcessDefinition> it = processes.iterator();
		while (it.hasNext()) {
			ProcessDefinition process = it.next();
			assertTrue("Invalid process processDefinitionUUID",
					process.getName().equals(PROCESS1_ID) || process.getName().equals(PROCESS2_ID));
		}
		processes = api.getProcesses(PROCESS1_ID);
		assertNotNull(processes);
		assertEquals(1, processes.size());
		ProcessDefinition process1 = processes.iterator().next();
		assertNotNull(process1);
		assertEquals("definitionTest", process1.getName());

		// not valid parameter
		ProcessDefinitionUUID badProcessDefinitionUUID = IdFactory.getNewProcessUUID();
		try {
			process1 = api.getProcess(badProcessDefinitionUUID);
		} catch (ProcessNotFoundException pe) {
			//OK
		} 

		process1 = api.getProcess(process1.getUUID());
		assertNotNull(process1);
		assertEquals(PROCESS1_ID, process1.getName());

		assertNotNull(process1);
		assertEquals(PROCESS1_ID, process1.getName());

		processes = api.getProcesses(PROCESS2_ID);
		assertNotNull(processes);
		assertEquals(1, processes.size());
		ProcessDefinition process2 = processes.iterator().next();
		assertNotNull(process2);

		processes = api.getProcesses();
		assertNotNull(processes);
		assertEquals(processes.toString(), 2, processes.size());

		processes = api.getProcesses(PROCESS1_ID);
		assertNotNull(processes);
		assertEquals(processes.toString(), 1, processes.size());

		processes = api.getProcesses(PROCESS2_ID);
		assertNotNull(processes);
		assertEquals(processes.toString(), 1, processes.size());

		assertNotNull(process1);
		assertEquals(PROCESS1_ID, process1.getName());
		assertEquals(PROCESS1_NAME, process1.getName());

		assertNotNull(process2);
		assertEquals(PROCESS2_ID, process2.getName());
		//by default, if no name is specified, name is filled with processDefinitionUUID
		assertEquals(PROCESS2_ID, process2.getName());

		assertProcess1(api, undeployed);
	}

	private void assertProcess1(QueryDefinitionAPI api, boolean undeployed)
	throws ProcessNotFoundException, ActivityNotFoundException, DataFieldNotFoundException {
		Set<ProcessDefinition> processes = api.getProcesses(PROCESS1_ID);
		assertNotNull(processes);
		assertEquals(1, processes.size());
		ProcessDefinition process1 = processes.iterator().next();
		assertEquals("processDefinitionUUID", PROCESS1_ID, process1.getName());
		assertEquals("name", PROCESS1_NAME, process1.getName());

		assertEquals("Description", "P_Description", process1.getDescription());
		assertEquals("Label", "definitionTestName", process1.getLabel());
		
		assertEquals("Version", "0.1", process1.getVersion());

		assertActivities(api, process1.getUUID());

		//hooks
		Set<String> classDeps = process1.getClassDependencies();
		assertNotNull("Class dependencies", classDeps);
		assertTrue(classDeps.toString(), process1.getClassDependencies().size() == 3);
		assertTrue("DefHook1 is in class dependencies", classDeps.contains(DefHook1.class.getName()));
		assertTrue("DefHook2 is in class dependencies", classDeps.contains(DefHook2.class.getName()));
		assertTrue("AdminsRoleMapper is in class dependencies", classDeps.contains(AdminsRoleMapper.class.getName()));
		assertEquals("Deployed by", "admin", process1.getDeployedBy());
		assertNotNull("Deployed Date", process1.getDeployedDate());

		if (!undeployed) {
			assertNull("Undeployed By", process1.getUndeployedBy());
			assertNull("Undeployed Date", process1.getUndeployedDate());
		} else {
			assertNotNull("Undeployed By", process1.getUndeployedBy());
			assertNotNull("Undeployed Date", process1.getUndeployedDate());
		}
		assertEquals("Version", "0.1", process1.getVersion());
	}

	private void assertActivities(QueryDefinitionAPI api,
			ProcessDefinitionUUID processDefinitionUUID) throws ProcessNotFoundException, ActivityNotFoundException {
		//invalid parameter
		ProcessDefinitionUUID badProcessDefinitionUUID = IdFactory.getNewProcessUUID();
		try {
			api.getProcessActivities(badProcessDefinitionUUID);
			fail("This process must not be found");
		} catch (ProcessNotFoundException pe) {
			//OK
		} 

		Set<ActivityDefinition> activities = api.getProcessActivities(processDefinitionUUID);
		assertNotNull(activities);
		assertEquals(2, activities.size());

		//invalid parameter
		try {
			api.getProcessActivity(badProcessDefinitionUUID, "Activity_A");
			fail("This process must not be found");
		} catch (ProcessNotFoundException pe) {
			//OK
		} 
		try {
			api.getProcessActivity(processDefinitionUUID, "bidon");
			fail("This process must not be found");
		} catch (ActivityNotFoundException ae) {
			//OK
		} 

		//invalid parameter
		try {
			api.getProcessActivityId(badProcessDefinitionUUID, "Activity A Name");
		} catch (ProcessNotFoundException pe) {
			//OK
		} 

		ActivityDefinition activityA = api.getProcessActivity(processDefinitionUUID, "Activity_A");
		assertNotNull(activityA);
		assertEquals("Activity_A", activityA.getName());
		assertEquals("getProcessActivityId", api.getProcessActivityId(processDefinitionUUID, "Activity_A"), activityA.getUUID());
		assertEquals("Description", "A_Description", activityA.getDescription());
		assertEquals("Name", "Activity_A", activityA.getName());
		assertEquals("Performer", "Caesar", activityA.getPerformers().iterator().next());

	}
	private void assertParticipants(QueryDefinitionAPI api,
			ProcessDefinitionUUID processDefinitionUUID) throws ProcessNotFoundException, ParticipantNotFoundException {
		String humanParticipantId = "Remus";
		String roleParticipantId = "manager";
		//constructs bad UUIIDs
		ProcessDefinitionUUID badProcessDefinitionUUID = IdFactory.getNewProcessUUID();

		//------test getProcessParticipants()----------
		Set<ParticipantDefinition> participants = api.getProcessParticipants(processDefinitionUUID);
		assertNotNull(participants);
		assertEquals(3, participants.size());

		try {
			api.getProcessParticipants(badProcessDefinitionUUID);
		} catch (ProcessNotFoundException pe) {
			//OK
		} 

		//--------- test getProcessParticipant() - Human ---------
		ParticipantDefinition participantHuman1 = api.getProcessParticipant(processDefinitionUUID, humanParticipantId);
		assertNotNull(participantHuman1);
		assertEquals("ParticipantId", humanParticipantId, participantHuman1.getName());
		// test getProcessParticipant() - Role with role mapper
		ParticipantDefinition participantRole1 = api.getProcessParticipant(processDefinitionUUID, roleParticipantId);
		assertNotNull(participantRole1);
		assertNotNull("DefinitionMapper", participantRole1.getRoleMapper());

		assertNotNull("ClassName in RoleMapperDefinition is not set", participantRole1.getRoleMapper().getClassName());
		assertTrue("className in role mapper", participantRole1.getRoleMapper().getClassName()
				.equals(AdminsRoleMapper.class.getName()));

		try {
			participantHuman1 = api.getProcessParticipant(badProcessDefinitionUUID, humanParticipantId);
		} catch (ProcessNotFoundException pe) {
			//OK
		}
		try {
			participantHuman1 = api.getProcessParticipant(processDefinitionUUID, "toto");
		} catch (ParticipantNotFoundException pe) {
			//OK
		} 

		//--------- test getProcessParticipantId() ---------
		ParticipantDefinitionUUID  participantDefinitionUUID = api.getProcessParticipantId(processDefinitionUUID, humanParticipantId);
		assertEquals("ParticipantDefinitionUUID", participantDefinitionUUID, participantHuman1.getUUID());
		try {
			participantHuman1 = api.getProcessParticipant(badProcessDefinitionUUID, humanParticipantId);
		} catch (ProcessNotFoundException pe) {
			//OK
		} 
	}

	public void testGetVariableDefinitions() throws BonitaException {
		URL xpdlUrl = this.getClass().getResource("variables.xpdl");
		ProcessDefinition process = getManagementAPI().deploy(getBusinessArchiveFromXpdl(xpdlUrl));
		ProcessDefinitionUUID processDefinitionUUID = process.getUUID();
		ProcessDefinitionUUID badProcessDefinitionUUID = IdFactory.getNewProcessUUID();
		// Check process variable definitions
		Set<DataFieldDefinition> datafields = getQueryDefinitionAPI().getProcessDataFields(processDefinitionUUID);
		assertFalse(datafields.isEmpty());
		assertEquals(2, datafields.size());

		DataFieldDefinition stringProcessDataField = null;
		DataFieldDefinition enumProcessDataField = null;
		DataFieldDefinition stringActivityDataField = null;
		DataFieldDefinition enumActivityDataField = null;

		for (DataFieldDefinition datafield : datafields) {
			assertNotNull(datafield);
			assertNotNull(datafield.getName());
			String id = datafield.getName();

			if (id.equals("string_process")) {
				stringProcessDataField = datafield;
			} else if (id.equals("enum_process")) {
				enumProcessDataField = datafield;
			} else if (id.equals("string_activity")) {
				stringActivityDataField = datafield;
			} else if (id.equals("enum_activity")) {
				enumActivityDataField = datafield;
			}
		}

		assertNotNull(stringProcessDataField);
		assertNotNull(enumProcessDataField);
		assertNull(stringActivityDataField);
		assertNull(enumActivityDataField);

		assertEquals("string_process", stringProcessDataField.getName());
		assertEquals("initial string value", stringProcessDataField.getInitialValue());
		assertEquals("enum_process", enumProcessDataField.getName());
		assertTrue(enumProcessDataField.isEnumeration());

		//Check ProcessNotFoundException
		try {
			getQueryDefinitionAPI().getProcessDataFields(badProcessDefinitionUUID);
			fail("check ProcessNotFoundException is raised");
		} catch (ProcessNotFoundException e) {
			assertEquals(badProcessDefinitionUUID, e.getProcessUUID());
		} 

		//******* test the method getProcessDataField()
		//string type
		DataFieldDefinition stringProcessDef = getQueryDefinitionAPI().getProcessDataField(processDefinitionUUID, "string_process");
		assertNotNull(stringProcessDef.getUUID());
		assertEquals("string_process", stringProcessDef.getName());
		assertEquals("string_process", stringProcessDef.getName());
		assertEquals(String.class.getName(), stringProcessDef.getDataTypeClassName());
		assertEquals("initial string value", stringProcessDef.getInitialValue());
		assertEquals("string for process", stringProcessDef.getDescription());

		//enumeration type
		DataFieldDefinition enumerationProcessDef = getQueryDefinitionAPI()
		.getProcessDataField(processDefinitionUUID, "enum_process");
		assertTrue(enumerationProcessDef.isEnumeration());

		//Check ProcessNotFoundException & DataFieldNotFoundException
		try {
			getQueryDefinitionAPI().getProcessDataField(badProcessDefinitionUUID, "string_process");
			fail("check raisinf exception : ProcessNotFoundException");
		} catch (ProcessNotFoundException e) {
			assertEquals(badProcessDefinitionUUID, e.getProcessUUID());
		} 
		try {
			getQueryDefinitionAPI().getProcessDataField(processDefinitionUUID, "bad_string_process");
			fail("check raisinf exception : ProcessNotFoundException");
		} catch (DataFieldNotFoundException e) {
			assertEquals("bad_string_process", e.getDataFieldId());
		} 

		// activity variables contains: process variables + local variables (act1 defines no local vars)

		//***** test getActivityDataFields & getActivityDataField
		/* Activity 1 */
		ActivityDefinitionUUID activityUUID = getQueryDefinitionAPI().getProcessActivityId(processDefinitionUUID, "act1");
		datafields = getQueryDefinitionAPI().getActivityDataFields(activityUUID);
		assertTrue(datafields.isEmpty());

		/* Activity 2 */
		activityUUID = getQueryDefinitionAPI().getProcessActivityId(processDefinitionUUID, "act2");
		datafields = getQueryDefinitionAPI().getActivityDataFields(activityUUID);
		assertFalse(datafields.isEmpty());
		assertEquals(2, datafields.size());

		stringProcessDataField = null;
		enumProcessDataField = null;
		stringActivityDataField = null;
		enumActivityDataField = null;

		for (DataFieldDefinition datafield : datafields) {
			assertNotNull(datafield);
			assertNotNull(datafield.getName());
			String id = datafield.getName();
			if (id.equals("string_process")) {
				stringProcessDataField = datafield;
			} else if (id.equals("enum_process")) {
				enumProcessDataField = datafield;
			} else if (id.equals("string_activity")) {
				stringActivityDataField = datafield;
			} else if (id.equals("enum_activity")) {
				enumActivityDataField = datafield;
			}
		}
		assertNull(stringProcessDataField);
		assertNull(enumProcessDataField);
		assertNotNull(stringActivityDataField);
		assertNotNull(enumActivityDataField);

		//Testing getActivityDataField method
		DataFieldDefinition stringActivityDef = getQueryDefinitionAPI().getActivityDataField(activityUUID, "string_activity");
		assertNotNull(stringActivityDef.getUUID());
		assertEquals("string_activity", stringActivityDef.getName());
		assertEquals("string_activity", stringActivityDef.getName());
		assertEquals(String.class.getName(), stringActivityDef.getDataTypeClassName());
		assertEquals("initial value", stringActivityDef.getInitialValue());
		assertEquals("string for activity", stringActivityDef.getDescription());

		//enumeration type
		DataFieldDefinition enumerationActivityDef = getQueryDefinitionAPI()
		.getActivityDataField(activityUUID, "enum_activity");
		assertTrue(enumerationActivityDef.isEnumeration());

		//Check exceptions :ActivityDefNotFoundException, DataFieldNotFoundException
		ActivityDefinitionUUID badActivityUUID = IdFactory.getNewActivityDefinitionUUID();
		try {
			getQueryDefinitionAPI().getActivityDataField(badActivityUUID, "string_activity");
			fail("check the exception ActivityDefNotFoundException is raised");
		} catch (ActivityDefNotFoundException e) {
			assertEquals(badActivityUUID, e.getActivityUUID());
		} 

		try {
			getQueryDefinitionAPI().getActivityDataField(activityUUID, "string_process");
			fail("check the exception DataFieldNotFoundException is raised");
		} catch (DataFieldNotFoundException e) {
			assertEquals("string_process", e.getDataFieldId());
		}
		/* Activity 3 */
		activityUUID = getQueryDefinitionAPI().getProcessActivityId(processDefinitionUUID, "act3");
		datafields = getQueryDefinitionAPI().getActivityDataFields(activityUUID);
		assertTrue(datafields.isEmpty());

		//check ActivityDefNotFoundException for get getActivityDataFields
		ActivityDefinitionUUID badActivityDefinitionUUID = IdFactory.getNewActivityDefinitionUUID();
		try {
			getQueryDefinitionAPI().getActivityDataFields(badActivityDefinitionUUID);
		} catch (ActivityDefNotFoundException e) {
			assertEquals(badActivityDefinitionUUID, e.getActivityUUID());
		} 

		getManagementAPI().disable(processDefinitionUUID);
		getManagementAPI().deleteProcess(processDefinitionUUID);
	}
	
	

	/*
   still not tested :

  Set<ParticipantDefinition> getProcessParticipants(String processDefinitionUUID, String deploymentId);
  ParticipantDefinition getProcessParticipant(String processDefinitionUUID, String deploymentId, String participantUUID);
  String getProcessParticipantId(String processDefinitionUUID, String deploymentId, String participantName);

  Set<ActivityDefinition> getProcessActivities(String processDefinitionUUID, String deploymentId);
  ActivityDefinition getProcessActivity(String processDefinitionUUID, String deploymentId, String activityId);
  String getProcessActivityId(String processDefinitionUUID, String deploymentId, String activityId);

	 */
}
