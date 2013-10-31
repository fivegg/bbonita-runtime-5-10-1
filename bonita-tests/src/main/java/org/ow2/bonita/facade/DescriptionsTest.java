package org.ow2.bonita.facade;

import java.util.Set;

import junit.framework.Assert;

import org.ow2.bonita.APITestCase;
import org.ow2.bonita.activity.instantiation.instantiator.TestActivityInstantiator;
import org.ow2.bonita.definition.InstanceInitiator;
import org.ow2.bonita.facade.def.element.FilterDefinition;
import org.ow2.bonita.facade.def.element.HookDefinition;
import org.ow2.bonita.facade.def.element.HookDefinition.Event;
import org.ow2.bonita.facade.def.element.MultiInstantiationDefinition;
import org.ow2.bonita.facade.def.majorElement.ActivityDefinition;
import org.ow2.bonita.facade.def.majorElement.DataFieldDefinition;
import org.ow2.bonita.facade.def.majorElement.ParticipantDefinition;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.def.majorElement.TransitionDefinition;
import org.ow2.bonita.facade.runtime.ProcessInstance;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.util.BonitaException;
import org.ow2.bonita.util.ProcessBuilder;

public class DescriptionsTest extends APITestCase {

  public void testProcessDescription() throws BonitaException {
    ProcessDefinition process =
      ProcessBuilder.createProcess("connector", "1.0")
      .addDescription("Process Description")
    .done();

    
    final ProcessDefinition definition = getManagementAPI().deploy(getBusinessArchive(process));
    final ProcessDefinitionUUID processUUID = definition.getUUID();
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);
    final ProcessInstance instance = getQueryRuntimeAPI().getProcessInstance(instanceUUID);

    ProcessDefinition processDef = getQueryDefinitionAPI().getProcess(instance.getProcessDefinitionUUID());
    Assert.assertEquals("Process Description", processDef.getDescription());
    
    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testProcessDataFieldDescription() throws BonitaException {
    ProcessDefinition process =
      ProcessBuilder.createProcess("connector", "1.0")
      .addDescription("Process Description")
      .addIntegerData("hello", 2)
        .addDescription("Integer hello")
    .done();

    final ProcessDefinition definition = getManagementAPI().deploy(getBusinessArchive(process));
    final ProcessDefinitionUUID processUUID = definition.getUUID();
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);
    final ProcessInstance instance = getQueryRuntimeAPI().getProcessInstance(instanceUUID);

    ProcessDefinition processDef = getQueryDefinitionAPI().getProcess(instance.getProcessDefinitionUUID());
    Set<DataFieldDefinition> datafields = processDef.getDataFields();
    Assert.assertEquals(1, datafields.size());
    DataFieldDefinition dataField = datafields.iterator().next();
    Assert.assertEquals("Integer hello", dataField.getDescription());
    
    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testActivityDescription() throws BonitaException {
    ProcessDefinition process =
      ProcessBuilder.createProcess("connector", "1.0")
      .addDescription("Process Description")
      .addSystemTask("go")
        .addIntegerData("hello", 2)
          .addDescription("Integer hello")
    .done();

    final ProcessDefinition definition = getManagementAPI().deploy(getBusinessArchive(process));
    final ProcessDefinitionUUID processUUID = definition.getUUID();
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);
    final ProcessInstance instance = getQueryRuntimeAPI().getProcessInstance(instanceUUID);

    ProcessDefinition processDef = getQueryDefinitionAPI().getProcess(instance.getProcessDefinitionUUID());
    ActivityDefinition activity = processDef.getActivities().iterator().next();
    DataFieldDefinition dataField = activity.getDataFields().iterator().next();
    Assert.assertEquals("Integer hello", dataField.getDescription());
    
    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testGroupDescription() throws BonitaException {
    ProcessDefinition process =
      ProcessBuilder.createProcess("connector", "1.0")
      .addGroup("Customer")
        .addDescription("Group description")
        .addGroupResolver(InstanceInitiator.class.getName())
      .addHumanTask("go", "Customer")  
    .done();

    final ProcessDefinition definition = getManagementAPI().deploy(getBusinessArchive(process));
    final ProcessDefinitionUUID processUUID = definition.getUUID();
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);
    final ProcessInstance instance = getQueryRuntimeAPI().getProcessInstance(instanceUUID);

    ProcessDefinition processDef = getQueryDefinitionAPI().getProcess(instance.getProcessDefinitionUUID());
    
    ParticipantDefinition group = processDef.getParticipants().iterator().next();
    Assert.assertEquals("Group description", group.getDescription());

    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testTransitionDescription() throws BonitaException {
    ProcessDefinition process =
      ProcessBuilder.createProcess("connector", "1.0")
      .addSystemTask("start")
      .addSystemTask("finish")
      .addTransition("start_finish", "start", "finish")
        .addDescription("Transition description")
    .done();

    final ProcessDefinition definition = getManagementAPI().deploy(getBusinessArchive(process));
    final ProcessDefinitionUUID processUUID = definition.getUUID();
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);
    final ProcessInstance instance = getQueryRuntimeAPI().getProcessInstance(instanceUUID);

    ProcessDefinition processDef = getQueryDefinitionAPI().getProcess(instance.getProcessDefinitionUUID());
    TransitionDefinition transition = processDef.getTransitions().iterator().next();
    Assert.assertEquals("Transition description", transition.getDescription());

    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testConnectorActivityDescription() throws BonitaException {
    ProcessDefinition process =
      ProcessBuilder.createProcess("connector", "1.0")
      .addSystemTask("go")
        .addConnector(Event.automaticOnEnter, Accept.class.getName(), true)
          .addDescription("Connector description")
    .done();

    final ProcessDefinition definition = getManagementAPI().deploy(getBusinessArchive(process, null, Accept.class));
    final ProcessDefinitionUUID processUUID = definition.getUUID();
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);
    final ProcessInstance instance = getQueryRuntimeAPI().getProcessInstance(instanceUUID);

    ProcessDefinition processDef = getQueryDefinitionAPI().getProcess(instance.getProcessDefinitionUUID());
    ActivityDefinition activity = processDef.getActivities().iterator().next();
    HookDefinition connector = activity.getConnectors().iterator().next();
    Assert.assertEquals("Connector description", connector.getDescription());

    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testFilterActivityDescription() throws BonitaException {
    ProcessDefinition process =
      ProcessBuilder.createProcess("connector", "1.0")
      .addGroup("Customer")
        .addGroupResolver(InstanceInitiator.class.getName())
      .addHumanTask("request", "Customer")
        .addFilter(AdminPerformerAssign.class.getName())
          .addDescription("Filter description")
    .done();

    final ProcessDefinition definition = getManagementAPI().deploy(getBusinessArchive(process, null, AdminPerformerAssign.class));
    final ProcessDefinitionUUID processUUID = definition.getUUID();
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);
    final ProcessInstance instance = getQueryRuntimeAPI().getProcessInstance(instanceUUID);

    ProcessDefinition processDef = getQueryDefinitionAPI().getProcess(instance.getProcessDefinitionUUID());
    ActivityDefinition activity = processDef.getActivities().iterator().next();
    FilterDefinition filter = activity.getFilter();
    Assert.assertEquals("Filter description", filter.getDescription());

    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testMultiInstantiationActivityDescription() throws BonitaException {
    ProcessDefinition process =
      ProcessBuilder.createProcess("connector", "1.0")
      .addSystemTask("go")
        .addStringData("testVar", "initial value")
        .addMultiInstanciation("testVar", TestActivityInstantiator.class.getName())
          .addDescription("MultiInstanciation description")
    .done();

    final ProcessDefinition definition = getManagementAPI().deploy(getBusinessArchive(process, null, TestActivityInstantiator.class));
    final ProcessDefinitionUUID processUUID = definition.getUUID();
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);
    final ProcessInstance instance = getQueryRuntimeAPI().getProcessInstance(instanceUUID);

    ProcessDefinition processDef = getQueryDefinitionAPI().getProcess(instance.getProcessDefinitionUUID());
    ActivityDefinition activity = processDef.getActivities().iterator().next();
    MultiInstantiationDefinition multi = activity.getMultiInstantiationDefinition();
    Assert.assertEquals("MultiInstanciation description", multi.getDescription());

    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

}
