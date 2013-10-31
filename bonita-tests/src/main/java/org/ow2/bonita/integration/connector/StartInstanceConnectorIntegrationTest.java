package org.ow2.bonita.integration.connector;

import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.connectors.bonita.StartInstanceConnector;
import org.ow2.bonita.APITestCase;
import org.ow2.bonita.facade.def.element.HookDefinition.Event;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.runtime.ProcessInstance;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.util.ProcessBuilder;

public class StartInstanceConnectorIntegrationTest extends APITestCase {

  private ProcessDefinition deployProcessToInstantiate() throws Exception {
    ProcessDefinition processToInstantiate = ProcessBuilder.createProcess("processToInstantiate", "2.0")
    .addIntegerData("data1", 0)
    .addStringData("data2")
    .addSystemTask("activity")
    .done();
    return getManagementAPI().deploy(getBusinessArchive(processToInstantiate));
  }
  
  private ProcessDefinition deployProcessToInstantiateNewVersion() throws Exception {
    ProcessDefinition processToInstantiate = ProcessBuilder.createProcess("processToInstantiate", "3.0")
    .addIntegerData("data1", 0)
    .addStringData("data2")
    .addSystemTask("activity")
    .done();
    return getManagementAPI().deploy(getBusinessArchive(processToInstantiate));
  }
  
  public void testConnectorWithVersion() throws Exception {
    
    ProcessDefinition processToInstantiate = deployProcessToInstantiate();
    
    List<List<Object>> variablesMap = new ArrayList<List<Object>>();
    List<Object> variableEntry = new ArrayList<Object>();
    variableEntry.add("data1");
    variableEntry.add(2);
    variablesMap.add(variableEntry);
    List<Object> variableEntry2 = new ArrayList<Object>();
    variableEntry2.add("data2");
    variableEntry2.add("test");
    variablesMap.add(variableEntry2);
    
    ProcessDefinition process = ProcessBuilder.createProcess("testProcess", null)
    .addStringData("processInstantiated")
    .addSystemTask("activity")
    .addConnector(Event.automaticOnEnter,StartInstanceConnector.class.getName(), true)
    .addInputParameter("processName", "processToInstantiate")
    .addInputParameter("processVersion", "2.0")
    .addInputParameter("processVariables", variablesMap)
    .addOutputParameter("createdProcessInstanceUUID", "processInstantiated")
    .done();
    
    process = getManagementAPI().deploy(getBusinessArchive(process, null, StartInstanceConnector.class));
    
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(process.getUUID());
    String instantiatedProcessUUIDStr = (String)getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "processInstantiated");
    assertNotNull(instantiatedProcessUUIDStr);
    ProcessInstanceUUID instantiatedProcessUUID = new ProcessInstanceUUID(instantiatedProcessUUIDStr);
    ProcessInstance instantiatedProcess = getQueryRuntimeAPI().getProcessInstance(instantiatedProcessUUID);
    final ProcessDefinition processDef = getQueryDefinitionAPI().getProcess(instantiatedProcess.getProcessDefinitionUUID());
    assertEquals(processToInstantiate.getVersion(), processDef.getVersion());
    Integer data1Value = (Integer)getQueryRuntimeAPI().getProcessInstanceVariable(instantiatedProcessUUID, "data1");
    assertEquals(Integer.valueOf(2), data1Value);
    String data2Value = (String)getQueryRuntimeAPI().getProcessInstanceVariable(instantiatedProcessUUID, "data2");
    assertEquals("test", data2Value);
    
    getManagementAPI().deleteAllProcesses();
  }
  
  public void testConnectorWithoutVersion() throws Exception {
    
    deployProcessToInstantiate();
    ProcessDefinition processToInstantiateNewVersion = deployProcessToInstantiateNewVersion();
    
    List<List<Object>> variablesMap = new ArrayList<List<Object>>();
    List<Object> variableEntry = new ArrayList<Object>();
    variableEntry.add("data1");
    variableEntry.add(2);
    variablesMap.add(variableEntry);
    List<Object> variableEntry2 = new ArrayList<Object>();
    variableEntry2.add("data2");
    variableEntry2.add("test");
    variablesMap.add(variableEntry2);
    
    ProcessDefinition process = ProcessBuilder.createProcess("testProcess", null)
    .addStringData("processInstantiated")
    .addSystemTask("activity")
    .addConnector(Event.automaticOnEnter,StartInstanceConnector.class.getName(), true)
    .addInputParameter("processName", "processToInstantiate")
    .addInputParameter("processVariables", variablesMap)
    .addOutputParameter("createdProcessInstanceUUID", "processInstantiated")
    .done();
    
    process = getManagementAPI().deploy(getBusinessArchive(process, null, StartInstanceConnector.class));
    
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(process.getUUID());
    String instantiatedProcessUUIDStr = (String)getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "processInstantiated");
    assertNotNull(instantiatedProcessUUIDStr);
    final ProcessInstanceUUID instantiatedProcessUUID = new ProcessInstanceUUID(instantiatedProcessUUIDStr);
    final ProcessInstance instantiatedProcess = getQueryRuntimeAPI().getProcessInstance(instantiatedProcessUUID);
    final ProcessDefinition processDef = getQueryDefinitionAPI().getProcess(instantiatedProcess.getProcessDefinitionUUID());
    assertEquals(processToInstantiateNewVersion.getVersion(), processDef.getVersion());
    Integer data1Value = (Integer)getQueryRuntimeAPI().getProcessInstanceVariable(instantiatedProcessUUID, "data1");
    assertEquals(Integer.valueOf(2), data1Value);
    String data2Value = (String)getQueryRuntimeAPI().getProcessInstanceVariable(instantiatedProcessUUID, "data2");
    assertEquals("test", data2Value);
    
    getManagementAPI().deleteAllProcesses();
  }
}
