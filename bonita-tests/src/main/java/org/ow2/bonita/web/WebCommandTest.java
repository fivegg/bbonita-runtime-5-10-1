/**
 * Copyright (C) 2010 BonitaSoft S.A.
 * BonitaSoft, 31 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.ow2.bonita.web;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ow2.bonita.APITestCase;
import org.ow2.bonita.facade.CommandAPI;
import org.ow2.bonita.facade.ManagementAPI;
import org.ow2.bonita.facade.QueryDefinitionAPI;
import org.ow2.bonita.facade.QueryRuntimeAPI;
import org.ow2.bonita.facade.RuntimeAPI;
import org.ow2.bonita.facade.WebAPI;
import org.ow2.bonita.facade.def.element.BusinessArchive;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.exception.ProcessNotFoundException;
import org.ow2.bonita.facade.runtime.Label;
import org.ow2.bonita.facade.runtime.command.WebDeleteAllCustomLabelsExceptCommand;
import org.ow2.bonita.facade.runtime.command.WebDeleteAllLabelsByNameCommand;
import org.ow2.bonita.facade.runtime.command.WebDeleteAllProcessInstancesCommand;
import org.ow2.bonita.facade.runtime.command.WebDeleteDocumentsOfProcessCommand;
import org.ow2.bonita.facade.runtime.command.WebDeleteProcessCommand;
import org.ow2.bonita.facade.runtime.command.WebGetLightProcessInstancesWithOverdueTasksCommand;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.light.LightProcessDefinition;
import org.ow2.bonita.light.LightProcessInstance;
import org.ow2.bonita.search.DocumentResult;
import org.ow2.bonita.search.DocumentSearchBuilder;
import org.ow2.bonita.search.index.DocumentIndex;
import org.ow2.bonita.util.BusinessArchiveFactory;
import org.ow2.bonita.util.ProcessBuilder;

/**
 * @author Nicolas Chabanoles
 *
 */
public class WebCommandTest extends APITestCase {

	public void testWebDeleteAllProcessInstancesCommand() throws Exception{
		final ManagementAPI managementAPI = getManagementAPI();
		final RuntimeAPI runtimeAPI = getRuntimeAPI();
		final QueryRuntimeAPI queryRuntimeAPI = getQueryRuntimeAPI();
		final QueryDefinitionAPI queryDefinitionAPI = getQueryDefinitionAPI();
		final WebAPI webAPI = getWebAPI();
		final CommandAPI commandAPI = getCommandAPI();
				
		Set<LightProcessDefinition> processes = queryDefinitionAPI.getLightProcesses();
		assertNotNull(processes);
		assertEquals(0, processes.size());
		
		Set<LightProcessInstance> processInstances = queryRuntimeAPI.getLightProcessInstances();
		assertNotNull(processInstances);
		assertEquals(0, processInstances.size());

		// Process definitions deployment and instantiation.
		ProcessDefinition theProcess;
		int theNbOfInstances = 8;
		Set<ProcessDefinitionUUID> processDefinitionUUIDs = new HashSet<ProcessDefinitionUUID>();
		for (int i = 0; i < theNbOfInstances; i++) {
			theProcess = ProcessBuilder.createProcess("twoHumanTasksInParallelProcess" + i, "1.0").addHuman(getLogin()).addSystemTask("start").addHumanTask("aHumanTask",
					getLogin()).addHumanTask("t", getLogin()).addTransition("start", "aHumanTask").addTransition("start", "t").done();
			managementAPI.deploy(BusinessArchiveFactory.getBusinessArchive(theProcess));
			runtimeAPI.instantiateProcess(theProcess.getUUID());
			processDefinitionUUIDs.add(theProcess.getUUID());
			
		}

		processInstances = queryRuntimeAPI.getLightProcessInstances();
		assertNotNull(processInstances);
		assertEquals(theNbOfInstances, processInstances.size());
		
		Set<ProcessInstanceUUID> instanceUUIDs = new HashSet<ProcessInstanceUUID>();
		for (LightProcessInstance lightProcessInstance : processInstances) {
			instanceUUIDs.add(lightProcessInstance.getUUID());
		}
		// List the Inbox,i.e. add labels (Inbox and Unread) to instances.
		webAPI.addLabel("Inbox", getLogin(), null, null, null, true, true, null, null, 0, true);
		webAPI.addCasesToLabel(getLogin(), "Inbox", instanceUUIDs);
		
		// Check labels data
		HashSet<String> labels = new HashSet<String>();
		labels.add("Inbox");
		Set<ProcessInstanceUUID> cases = webAPI.getCases(getLogin(), labels);
		assertEquals(theNbOfInstances, cases.size());
		
		commandAPI.execute(new WebDeleteAllProcessInstancesCommand(processDefinitionUUIDs));
		
		// Check that there is no more instances.
		processInstances = queryRuntimeAPI.getLightProcessInstances();
		assertNotNull(processInstances);
		assertEquals(0, processInstances.size());

		
		// Check labels data again (this time there should be 0 instances associated to labels).
		cases = webAPI.getCases(getLogin(), labels);
		assertEquals(0, cases.size());
		
		getManagementAPI().deleteAllProcesses();
		
		webAPI.removeLabel(getLogin(), "Inbox");
	}
	
	public void testWebDeleteAllProcessInstancesCommandWithNullAndEmpty() throws Exception{
		final ManagementAPI managementAPI = getManagementAPI();
		final RuntimeAPI runtimeAPI = getRuntimeAPI();
		final QueryRuntimeAPI queryRuntimeAPI = getQueryRuntimeAPI();
		final QueryDefinitionAPI queryDefinitionAPI = getQueryDefinitionAPI();
		final WebAPI webAPI = getWebAPI();
		final CommandAPI commandAPI = getCommandAPI();
				
		Set<LightProcessDefinition> processes = queryDefinitionAPI.getLightProcesses();
		assertNotNull(processes);
		assertEquals(0, processes.size());
		
		Set<LightProcessInstance> processInstances = queryRuntimeAPI.getLightProcessInstances();
		assertNotNull(processInstances);
		assertEquals(0, processInstances.size());

		// Process definitions deployment and instantiation.
		ProcessDefinition theProcess;
		int theNbOfInstances = 8;
		Set<ProcessDefinitionUUID> processDefinitionUUIDs = new HashSet<ProcessDefinitionUUID>();
		for (int i = 0; i < theNbOfInstances; i++) {
			theProcess = ProcessBuilder.createProcess("twoHumanTasksInParallelProcess" + i, "1.0").addHuman(getLogin()).addSystemTask("start").addHumanTask("aHumanTask",
					getLogin()).addHumanTask("t", getLogin()).addTransition("start", "aHumanTask").addTransition("start", "t").done();
			managementAPI.deploy(BusinessArchiveFactory.getBusinessArchive(theProcess));
			runtimeAPI.instantiateProcess(theProcess.getUUID());
			processDefinitionUUIDs.add(theProcess.getUUID());
			
		}

		processInstances = queryRuntimeAPI.getLightProcessInstances();
		assertNotNull(processInstances);
		assertEquals(theNbOfInstances, processInstances.size());
		
		Set<ProcessInstanceUUID> instanceUUIDs = new HashSet<ProcessInstanceUUID>();
		for (LightProcessInstance lightProcessInstance : processInstances) {
			instanceUUIDs.add(lightProcessInstance.getUUID());
		}
		// List the Inbox,i.e. add labels (Inbox and Unread) to instances.
		webAPI.addLabel("Inbox", getLogin(), null, null, null, true, true, null, null, 0, true);
		webAPI.addCasesToLabel(getLogin(), "Inbox", instanceUUIDs);
		
		// Check labels data
		HashSet<String> labels = new HashSet<String>();
		labels.add("Inbox");
		Set<ProcessInstanceUUID> cases = webAPI.getCases(getLogin(), labels);
		assertEquals(theNbOfInstances, cases.size());
		
		commandAPI.execute(new WebDeleteAllProcessInstancesCommand(null));
		
		// Check that there is still instances.
		processInstances = queryRuntimeAPI.getLightProcessInstances();
		assertNotNull(processInstances);
		assertEquals(theNbOfInstances, processInstances.size());
		

		// Check labels data again.
		cases = webAPI.getCases(getLogin(), labels);
		assertEquals(theNbOfInstances, cases.size());
		
		commandAPI.execute(new WebDeleteAllProcessInstancesCommand(new HashSet<ProcessDefinitionUUID>()));
		
		// Check that there is still instances.
		processInstances = queryRuntimeAPI.getLightProcessInstances();
		assertNotNull(processInstances);
		assertEquals(theNbOfInstances, processInstances.size());
		

		// Check labels data again.
		cases = webAPI.getCases(getLogin(), labels);
		assertEquals(theNbOfInstances, cases.size());
		
		// clean up.
		getManagementAPI().deleteAllProcesses();
		
		webAPI.removeLabel(getLogin(), "Inbox");
	}
	
	public void testWebDeleteAllCustomLabelsExceptCommand() throws Exception{
    final ManagementAPI managementAPI = getManagementAPI();
    final RuntimeAPI runtimeAPI = getRuntimeAPI();
    final WebAPI webAPI = getWebAPI();
    final CommandAPI commandAPI = getCommandAPI();

    
    // Process definitions deployment and instantiation.
    ProcessDefinition theProcess;
    int theNbOfInstances = 8;
    final Set<ProcessDefinitionUUID> processDefinitionUUIDs = new HashSet<ProcessDefinitionUUID>();
    final Set<ProcessInstanceUUID> processInstanceUUIDs = new HashSet<ProcessInstanceUUID>();
    for (int i = 0; i < theNbOfInstances; i++) {
      theProcess = ProcessBuilder.createProcess("twoHumanTasksInParallelProcess" + i, "1.0").addHuman(getLogin()).addSystemTask("start").addHumanTask("aHumanTask",
          getLogin()).addHumanTask("t", getLogin()).addTransition("start", "aHumanTask").addTransition("start", "t").done();
      managementAPI.deploy(BusinessArchiveFactory.getBusinessArchive(theProcess));
      processInstanceUUIDs.add(runtimeAPI.instantiateProcess(theProcess.getUUID()));
      processDefinitionUUIDs.add(theProcess.getUUID());
      
    }
    final String[] systemLabels = new String[]{"Starred","Inbox","All","AtRisk","MyCases"};
    String ownerName;
    String labelName;
    for(int u=0; u<10; u++){
      ownerName = "user"+u;
      // Create system labels.
      for (String theString : systemLabels) {
        webAPI.addLabel(theString, ownerName, null, null, null, true, ((u%2)==0), null, null, u, true);
        // bind label to all cases.
        for (ProcessInstanceUUID processInstanceUUID : processInstanceUUIDs) {
          webAPI.addCasesToLabel(ownerName, theString, new HashSet<ProcessInstanceUUID>(Arrays.asList(processInstanceUUID)));
        }
      }
      // Create user defined labels.
      for(int l=0; l<5;l++){
        labelName = "label"+l;
        webAPI.addLabel(labelName, ownerName, null, null, null, false, true, null, null, u, false);
        // bind label to all cases.
        for (ProcessInstanceUUID processInstanceUUID : processInstanceUUIDs) {
          webAPI.addCasesToLabel(ownerName, labelName, new HashSet<ProcessInstanceUUID>(Arrays.asList(processInstanceUUID)));
        }
      }
    }
    // Check labels creation & cases association.
    Set<ProcessInstanceUUID> cases;
    Set<Label> labels;
    for(int u=0; u<10; u++){
      ownerName = "user"+u;
      labels = webAPI.getLabels(ownerName);
      assertEquals(5 + systemLabels.length, labels.size());
      for (Label label : labels) {
        cases = webAPI.getCases(ownerName, new HashSet<String>(Arrays.asList(label.getName())));
        assertEquals(processInstanceUUIDs.size(), cases.size());
      }
    }
    
    commandAPI.execute(new WebDeleteAllCustomLabelsExceptCommand(systemLabels));
    for(int u=0; u<10; u++){
      ownerName = "user"+u;
      labels = webAPI.getLabels(ownerName);
      assertEquals(systemLabels.length, labels.size());
      for (Label label : labels) {
        cases = webAPI.getCases(ownerName, new HashSet<String>(Arrays.asList(label.getName())));
        assertEquals(processInstanceUUIDs.size(), cases.size());
      }
    }
    
    
    // clean up.
    getManagementAPI().deleteAllProcesses();
    webAPI.deleteAllCases();
    for(int u=0; u<10; u++){
      ownerName = "user"+u;
      // Create system labels.
      for (String theString : systemLabels) {
        webAPI.removeLabel(ownerName,theString);
      }
      // Create user defined labels.
      for(int l=0; l<5;l++){
        labelName = "label"+l;
        webAPI.removeLabel(ownerName,labelName);
      }
    }
    
  }
	
	public void testWebDeleteAllLabelsByNameCommand() throws Exception{
    final ManagementAPI managementAPI = getManagementAPI();
    final RuntimeAPI runtimeAPI = getRuntimeAPI();
    final WebAPI webAPI = getWebAPI();
    final CommandAPI commandAPI = getCommandAPI();

    
    // Process definitions deployment and instantiation.
    ProcessDefinition theProcess;
    int theNbOfInstances = 8;
    final Set<ProcessDefinitionUUID> processDefinitionUUIDs = new HashSet<ProcessDefinitionUUID>();
    final Set<ProcessInstanceUUID> processInstanceUUIDs = new HashSet<ProcessInstanceUUID>();
    for (int i = 0; i < theNbOfInstances; i++) {
      theProcess = ProcessBuilder.createProcess("twoHumanTasksInParallelProcess" + i, "1.0").addHuman(getLogin()).addSystemTask("start").addHumanTask("aHumanTask",
          getLogin()).addHumanTask("t", getLogin()).addTransition("start", "aHumanTask").addTransition("start", "t").done();
      managementAPI.deploy(BusinessArchiveFactory.getBusinessArchive(theProcess));
      processInstanceUUIDs.add(runtimeAPI.instantiateProcess(theProcess.getUUID()));
      processDefinitionUUIDs.add(theProcess.getUUID());
      
    }
    final String labelToRemove = "Starred";
    final String[] systemLabels = new String[]{labelToRemove,"Inbox","All","AtRisk","MyCases"};
    String ownerName;
    String labelName;
    for(int u=0; u<10; u++){
      ownerName = "user"+u;
      // Create system labels.
      for (String theString : systemLabels) {
        webAPI.addLabel(theString, ownerName, null, null, null, true, ((u%2)==0), null, null, u, true);
        // bind label to all cases.
        for (ProcessInstanceUUID processInstanceUUID : processInstanceUUIDs) {
          webAPI.addCasesToLabel(ownerName, theString, new HashSet<ProcessInstanceUUID>(Arrays.asList(processInstanceUUID)));
        }
      }
      // Create user defined labels.
      for(int l=0; l<5;l++){
        labelName = "label"+l;
        webAPI.addLabel(labelName, ownerName, null, null, null, false, true, null, null, u, false);
        // bind label to all cases.
        for (ProcessInstanceUUID processInstanceUUID : processInstanceUUIDs) {
          webAPI.addCasesToLabel(ownerName, labelName, new HashSet<ProcessInstanceUUID>(Arrays.asList(processInstanceUUID)));
        }
      }
    }
    // Check labels creation & cases association.
    Set<ProcessInstanceUUID> cases;
    Set<Label> labels;
    for(int u=0; u<10; u++){
      ownerName = "user"+u;
      labels = webAPI.getLabels(ownerName);
      assertEquals(5 + systemLabels.length, labels.size());
      for (Label label : labels) {
        cases = webAPI.getCases(ownerName, new HashSet<String>(Arrays.asList(label.getName())));
        assertEquals(processInstanceUUIDs.size(), cases.size());
      }
    }
    
    commandAPI.execute(new WebDeleteAllLabelsByNameCommand(labelToRemove));
    for(int u=0; u<10; u++){
      ownerName = "user"+u;
      labels = webAPI.getLabels(ownerName);
      assertEquals(5+systemLabels.length-1, labels.size());
      for (Label label : labels) {
        cases = webAPI.getCases(ownerName, new HashSet<String>(Arrays.asList(label.getName())));
        assertEquals(processInstanceUUIDs.size(), cases.size());
      }
    }
    
    
    // clean up.
    getManagementAPI().deleteAllProcesses();
    webAPI.deleteAllCases();
    for(int u=0; u<10; u++){
      ownerName = "user"+u;
      // Create system labels.
      for (String theString : systemLabels) {
        webAPI.removeLabel(ownerName,theString);
      }
      // Create user defined labels.
      for(int l=0; l<5;l++){
        labelName = "label"+l;
        webAPI.removeLabel(ownerName,labelName);
      }
    }
  }
  
	public void testWebGetLightProcessInstancesWithOverdueTasksCommand() throws Exception{
    final ManagementAPI managementAPI = getManagementAPI();
    final RuntimeAPI runtimeAPI = getRuntimeAPI();
    final CommandAPI commandAPI = getCommandAPI();
    
    List<LightProcessInstance> processInstances = commandAPI.execute(new WebGetLightProcessInstancesWithOverdueTasksCommand(null,null,null,null,getLogin(), 0, 20, false));
    assertNotNull(processInstances);
    assertEquals(0, processInstances.size());

    ProcessDefinition withoutTimeEstimationProcess = ProcessBuilder.createProcess("withoutTimeEstimation", "1.0").addHuman(getLogin()).addHumanTask("overdue", getLogin()).addHumanTask(
        "onTrack", getLogin()).addHumanTask("atRisk", getLogin()).done();
    managementAPI.deploy(BusinessArchiveFactory.getBusinessArchive(withoutTimeEstimationProcess));
    
    final ProcessDefinitionUUID withoutTimeEstimationProcessUUID = withoutTimeEstimationProcess.getUUID();

    // Start a case.
    runtimeAPI.instantiateProcess(withoutTimeEstimationProcessUUID);
    
    processInstances = commandAPI.execute(new WebGetLightProcessInstancesWithOverdueTasksCommand(null,null,null,null,getLogin(), 0, 20, false));
    assertNotNull(processInstances);
    assertEquals(0, processInstances.size());
    
    ProcessDefinition withTimeEstimationProcess = ProcessBuilder.createProcess("withTimeEstimation", "1.0").addHuman(getLogin()).addHumanTask("overdue", getLogin())
    .addActivityExecutingTime(1000).addHumanTask("onTrack", getLogin()).addActivityExecutingTime(999999999).addHumanTask("atRisk", getLogin())
    .addActivityExecutingTime(10000).done();
    managementAPI.deploy(BusinessArchiveFactory.getBusinessArchive(withTimeEstimationProcess));
    final ProcessDefinitionUUID withTimeEstimationProcessUUID = withTimeEstimationProcess.getUUID();
    
 // Start a case.
    runtimeAPI.instantiateProcess(withTimeEstimationProcessUUID);
    
    // Be sure the step is overdue.
    Thread.sleep(1010);
    
    processInstances = commandAPI.execute(new WebGetLightProcessInstancesWithOverdueTasksCommand(null,null,null,null,getLogin(), 0, 20, false));
    assertNotNull(processInstances);
    assertEquals(1, processInstances.size());
        
    getManagementAPI().deleteProcess(withTimeEstimationProcessUUID);
    getManagementAPI().deleteProcess(withoutTimeEstimationProcessUUID);
    
  }
	
	public void testWebDeleteProcessAndAttachmentCommand() throws Exception{
    final ManagementAPI managementAPI = getManagementAPI();
    final RuntimeAPI runtimeAPI = getRuntimeAPI();
    final QueryRuntimeAPI queryRuntimeAPI = getQueryRuntimeAPI();
    final QueryDefinitionAPI queryDefinitionAPI = getQueryDefinitionAPI();
    final CommandAPI commandAPI = getCommandAPI();
    
    File attachmentFile = File.createTempFile("attachment-test",".txt");
    
    ProcessDefinition attachmentProcess = ProcessBuilder.createProcess("attachment_process", "1.0")
    .addAttachment("attachment", attachmentFile.getPath(), attachmentFile.getName())
    .addHuman("john")
    .addHumanTask("task1", "john")
    .addHumanTask("task2", "john")
    .addTransition("transition", "task1", "task2")
    .done();
    
    BusinessArchive businessArchive = BusinessArchiveFactory.getBusinessArchive(attachmentProcess);
    attachmentProcess = managementAPI.deploy(businessArchive);

    ProcessInstanceUUID instanceUUID = runtimeAPI.instantiateProcess(attachmentProcess.getUUID());
    final DocumentSearchBuilder documentSearchBuilder = new DocumentSearchBuilder();
    documentSearchBuilder.leftParenthesis();
    documentSearchBuilder.criterion(DocumentIndex.PROCESS_INSTANCE_UUID).equalsTo(instanceUUID.getValue());
    documentSearchBuilder.rightParenthesis();
    DocumentResult foundDocuments;
    foundDocuments = queryRuntimeAPI.searchDocuments(documentSearchBuilder, 0, 100);
    assertEquals(1,foundDocuments.getCount());

    commandAPI.execute(new WebDeleteDocumentsOfProcessCommand(attachmentProcess.getUUID(),true));
    commandAPI.execute(new WebDeleteProcessCommand(attachmentProcess.getUUID()));
    
    foundDocuments = queryRuntimeAPI.searchDocuments(documentSearchBuilder, 0, 100);
    assertEquals(0,foundDocuments.getCount());
    
    try {
      queryDefinitionAPI.getProcess(attachmentProcess.getUUID());
      fail();
    } catch (ProcessNotFoundException e) {
      // Expected behaviour
    }
  }
	
}
