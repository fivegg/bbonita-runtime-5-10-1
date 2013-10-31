package org.ow2.bonita.integration.connector;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.bonitasoft.connectors.bonita.SetVarConnector;
import org.bonitasoft.connectors.bonita.resolvers.ProcessInitiatorRoleResolver;
import org.bonitasoft.connectors.scripting.GroovyConnector;
import org.custommonkey.xmlunit.XMLUnit;
import org.ow2.bonita.APITestCase;
import org.ow2.bonita.connector.example.NoDescriptorConnector;
import org.ow2.bonita.facade.def.element.BusinessArchive;
import org.ow2.bonita.facade.def.element.HookDefinition.Event;
import org.ow2.bonita.facade.def.majorElement.ActivityDefinition.JoinType;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.exception.DeploymentException;
import org.ow2.bonita.facade.runtime.ActivityInstance;
import org.ow2.bonita.facade.runtime.ActivityState;
import org.ow2.bonita.facade.runtime.InstanceState;
import org.ow2.bonita.facade.runtime.TaskInstance;
import org.ow2.bonita.facade.uuid.ActivityInstanceUUID;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.integration.connector.test.AttachmentConnector;
import org.ow2.bonita.integration.connector.test.BadDescriptorConnector;
import org.ow2.bonita.integration.connector.test.InputOutputConnector;
import org.ow2.bonita.integration.connector.test.OutputConnector;
import org.ow2.bonita.util.BonitaConstants;
import org.ow2.bonita.util.BonitaException;
import org.ow2.bonita.util.BusinessArchiveFactory;
import org.ow2.bonita.util.DateUtil;
import org.ow2.bonita.util.Misc;
import org.ow2.bonita.util.ProcessBuilder;
import org.w3c.dom.Document;

public class ActivityConnectorTest extends APITestCase {

  public void testConnectorBigParameters() throws Exception {
    StringBuilder inputScript = new StringBuilder();
    String outputScript = "${import javax.xml.transform.Transformer"
        + "\n"
        + " import javax.xml.transform.TransformerException"
        + "\n"
        + " import javax.xml.transform.TransformerFactory"
        + "\n"
        + " import javax.xml.transform.TransformerFactoryConfigurationError"
        + "\n"
        + " import javax.xml.transform.stream.StreamResult"
        + "\n"
        + "\n"
        + " Transformer transformer = TransformerFactory.newInstance().newTransformer()"
        + "\n" + " StringWriter writer = new StringWriter()" + "\n"
        + " StreamResult result = new StreamResult(writer)" + "\n"
        + " transformer.transform(response, result)" + "\n"
        + " writer.toString()}" + "\n";

    for (int i = 0; i < 1500; i++) {
      inputScript.append(i);
    }

    ProcessDefinition process = ProcessBuilder.createProcess("bigParameters", null)
        .addSystemTask("t")
        .addConnector(Event.automaticOnEnter,SetVarConnector.class.getName(), true)
        .addInputParameter("variableName", inputScript.toString())
        .addInputParameter("value", outputScript)
        .addOutputParameter(outputScript, "unexistingDestination")
        .done();

    process = getManagementAPI().deploy(
        getBusinessArchive(process, null, SetVarConnector.class));

    ProcessDefinitionUUID processUUID = process.getUUID();

    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }
  
  public void testInputConnector() throws Exception {
    URL xpdlUrl = getClass().getResource("InputConnector.xpdl");
    ProcessDefinition process =
      getManagementAPI().deploy(getBusinessArchiveFromXpdl(
          xpdlUrl, getResourcesFromConnector(InputOutputConnector.class), InputOutputConnector.class));

    ProcessDefinitionUUID processUUID = process.getUUID();
    ProcessInstanceUUID instanceUUID =
      getRuntimeAPI().instantiateProcess(processUUID);

    Double price = (Double) getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "price");
    assertEquals(20.1, price);
    Long phoneNumber = (Long) getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "phoneNumber");
    assertEquals(Long.valueOf("0848754512"), phoneNumber);
    String where = (String) getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "Where");
    assertEquals("SunnyTown", where);
    Date myDate = (Date) getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "myDate");
    assertNotNull(myDate);
    assertEquals(DateUtil.parseDate("2009-04-14T13:12:54.000+0200"), myDate);

    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testOutputConnector() throws Exception {
    URL xpdlUrl = getClass().getResource("OutputConnector.xpdl");

    ProcessDefinition process =
      getManagementAPI().deploy(getBusinessArchiveFromXpdl(
          xpdlUrl, getResourcesFromConnector(OutputConnector.class), OutputConnector.class));

    ProcessDefinitionUUID processUUID = process.getUUID();
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    Set<ActivityInstance> actIntances =
      getQueryRuntimeAPI().getActivityInstances(instanceUUID, "Request");
    ActivityInstance ai = actIntances.iterator().next();
    String message = (String)
        getQueryRuntimeAPI().getActivityInstanceVariable(ai.getUUID(), "output");
    assertEquals("Nothing", message);

    // execute the task
    final Collection<TaskInstance> tasks = getQueryRuntimeAPI().getTaskList(instanceUUID, ActivityState.READY);
    assertEquals(1, tasks.size());
    final TaskInstance task = tasks.iterator().next();
    assertEquals("Request", task.getActivityName());
    assertEquals(ActivityState.READY, task.getState());
    final ActivityInstanceUUID taskUUID = task.getUUID();
    getRuntimeAPI().startTask(taskUUID, false);
    getRuntimeAPI().finishTask(taskUUID, false);

    // check the value after executing
    actIntances = getQueryRuntimeAPI().getActivityInstances(instanceUUID, "Request");
    ai = actIntances.iterator().next();
    message = (String) getQueryRuntimeAPI().getActivityInstanceVariable(ai.getUUID(), "output");
    assertEquals("Something", message);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }
  
  

  public void testInputOutputConnector() throws Exception {
    URL xpdlUrl = getClass().getResource("InputOutputConnector.xpdl");
    ProcessDefinition process =
      getManagementAPI().deploy(getBusinessArchiveFromXpdl(
          xpdlUrl, getResourcesFromConnector(InputOutputConnector.class), InputOutputConnector.class));

    ProcessDefinitionUUID processUUID = process.getUUID();
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    Set<ActivityInstance> actIntances =
      getQueryRuntimeAPI().getActivityInstances(instanceUUID, "Request");
    ActivityInstance ai = actIntances.iterator().next();
    String message = (String)
      getQueryRuntimeAPI().getActivityInstanceVariable(ai.getUUID(), "message");
    assertEquals("Nothing", message);
    Double price = (Double)
      getQueryRuntimeAPI().getActivityInstanceVariable(ai.getUUID(), "price");
    assertEquals(20.1, price);
    Long phoneNumber = (Long)
      getQueryRuntimeAPI().getActivityInstanceVariable(ai.getUUID(), "phoneNumber");
    assertEquals(Long.valueOf("0848754512"), phoneNumber);
    String where = (String)
      getQueryRuntimeAPI().getActivityInstanceVariable(ai.getUUID(), "Where");
    assertEquals("SunnyTown", where);
    Date myDate = (Date)
      getQueryRuntimeAPI().getActivityInstanceVariable(ai.getUUID(), "myDate");
    assertEquals(DateUtil.parseDate("2009-04-14T13:12:54.000+0200"), myDate);

    // execute the task
    final Collection<TaskInstance> tasks =
      getQueryRuntimeAPI().getTaskList(instanceUUID, ActivityState.READY);
    assertEquals(1, tasks.size());
    final TaskInstance task = tasks.iterator().next();
    assertEquals("Request", task.getActivityName());
    assertEquals(ActivityState.READY, task.getState());
    final ActivityInstanceUUID taskUUID = task.getUUID();
    getRuntimeAPI().startTask(taskUUID, false);
    getRuntimeAPI().finishTask(taskUUID, false);

    // check the value after executing
    actIntances = getQueryRuntimeAPI().getActivityInstances(instanceUUID, "Request");
    ai = actIntances.iterator().next();
    message = (String)
        getQueryRuntimeAPI().getActivityInstanceVariable(ai.getUUID(), "message");
    assertEquals("848754512 SunnyTown 20.1 " + DateUtil.parseDate("2009-04-14T13:12:54.000+0200"), message);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testInputOutputConnectorProcess() throws Exception {
    ProcessDefinition process = getManagementAPI().deploy(getBusinessArchive());
    ProcessDefinitionUUID processUUID = process.getUUID();
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    Set<ActivityInstance> actIntances =
      getQueryRuntimeAPI().getActivityInstances(instanceUUID, "Request");
    ActivityInstance ai = actIntances.iterator().next();
    String message = (String)
      getQueryRuntimeAPI().getActivityInstanceVariable(ai.getUUID(), "message");
    assertEquals("Nothing", message);
    Double price = (Double)
      getQueryRuntimeAPI().getActivityInstanceVariable(ai.getUUID(), "price");
    assertEquals(new Double(20.1), price);
    Integer phoneNumber = (Integer)
      getQueryRuntimeAPI().getActivityInstanceVariable(ai.getUUID(), "phoneNumber");
    assertEquals(new Integer(848754512), phoneNumber);
    String where = (String)
      getQueryRuntimeAPI().getActivityInstanceVariable(ai.getUUID(), "Where");
    assertEquals("SunnyTown", where);
    Date myDate = (Date)
      getQueryRuntimeAPI().getActivityInstanceVariable(ai.getUUID(), "myDate");
    assertEquals(DateUtil.parseDate("2009-04-14T13:12:54.000+0200"), myDate);

    // execute the task
    final Collection<TaskInstance> tasks =
      getQueryRuntimeAPI().getTaskList(instanceUUID, ActivityState.READY);
    assertEquals(1, tasks.size());
    final TaskInstance task = tasks.iterator().next();
    assertEquals("Request", task.getActivityName());
    assertEquals(ActivityState.READY, task.getState());
    final ActivityInstanceUUID taskUUID = task.getUUID();
    getRuntimeAPI().startTask(taskUUID, false);
    getRuntimeAPI().finishTask(taskUUID, false);

    // check the value after executing
    actIntances = getQueryRuntimeAPI().getActivityInstances(instanceUUID, "Request");
    ai = actIntances.iterator().next();
    message = (String)
        getQueryRuntimeAPI().getActivityInstanceVariable(ai.getUUID(), "message");
    assertEquals("848754512 SunnyTown 20.1 " + DateUtil.parseDate("2009-04-14T13:12:54.000+0200"), message);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testInputOutputConnectorProcessStringTo() throws Exception {
    ProcessDefinition def =
      ProcessBuilder.createProcess("InputOutput", null)
      .addGroup("Customer")
      .addGroupResolver(ProcessInitiatorRoleResolver.class.getName())
      .addSystemTask("BonitaEnd")
        .addJoinType(JoinType.XOR)
      .addSystemTask("BonitaInit")
      .addHumanTask("Request", "Customer")
        .addDateData("myDate", DateUtil.parseDate("2009-04-14T13:12:54.000+0200"))
        .addStringData("phoneNumber", "848754512")
        .addStringData("Where", "SunnyTown")
        .addStringData("message", "Nothing")
        .addIntegerData("price", 20)
        .addConnector(Event.taskOnFinish, InputOutputConnector.class.getName(), true)
          .addOutputParameter("${output}", "message")
          .addInputParameter("thirdInput", "${price + 0.1}")
          .addInputParameter("firstInput", "${phoneNumber}")
          .addInputParameter("secondInput", "${Where}")
          .addInputParameter("fourthInput", "${myDate}")
      .addTransition("Start_Request", "BonitaInit", "Request")
      .addTransition("Request_End", "Request", "BonitaEnd")
      .done();

    ProcessDefinition process = getManagementAPI().deploy(getBusinessArchive(
        def, getResourcesFromConnector(InputOutputConnector.class, ProcessInitiatorRoleResolver.class), InputOutputConnector.class, ProcessInitiatorRoleResolver.class));

    ProcessDefinitionUUID processUUID = process.getUUID();
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    Set<ActivityInstance> actIntances =
      getQueryRuntimeAPI().getActivityInstances(instanceUUID, "Request");
    ActivityInstance ai = actIntances.iterator().next();
    String message = (String)
      getQueryRuntimeAPI().getActivityInstanceVariable(ai.getUUID(), "message");
    assertEquals("Nothing", message);
    Integer price = (Integer)
      getQueryRuntimeAPI().getActivityInstanceVariable(ai.getUUID(), "price");
    assertEquals(Integer.valueOf(20), price);
    String phoneNumber = (String)
      getQueryRuntimeAPI().getActivityInstanceVariable(ai.getUUID(), "phoneNumber");
    assertEquals("848754512", phoneNumber);
    String where = (String)
      getQueryRuntimeAPI().getActivityInstanceVariable(ai.getUUID(), "Where");
    assertEquals("SunnyTown", where);
    Date myDate = (Date)
      getQueryRuntimeAPI().getActivityInstanceVariable(ai.getUUID(), "myDate");
    assertEquals(DateUtil.parseDate("2009-04-14T13:12:54.000+0200"), myDate);

    // execute the task
    final Collection<TaskInstance> tasks =
      getQueryRuntimeAPI().getTaskList(instanceUUID, ActivityState.READY);
    assertEquals(1, tasks.size());
    final TaskInstance task = tasks.iterator().next();
    assertEquals("Request", task.getActivityName());
    assertEquals(ActivityState.READY, task.getState());
    final ActivityInstanceUUID taskUUID = task.getUUID();
    getRuntimeAPI().startTask(taskUUID, false);
    getRuntimeAPI().finishTask(taskUUID, false);

    // check the value after executing
    actIntances = getQueryRuntimeAPI().getActivityInstances(instanceUUID, "Request");
    ai = actIntances.iterator().next();
    message = (String)
        getQueryRuntimeAPI().getActivityInstanceVariable(ai.getUUID(), "message");
    assertEquals("848754512 SunnyTown 20.1 " + DateUtil.parseDate("2009-04-14T13:12:54.000+0200"), message);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  private BusinessArchive getBusinessArchive() throws IOException {
    ProcessDefinition process =
      ProcessBuilder.createProcess("InputOutput", null)
      .addGroup("Customer")
      .addGroupResolver(ProcessInitiatorRoleResolver.class.getName())
      .addSystemTask("BonitaEnd")
        .addJoinType(JoinType.XOR)
      .addSystemTask("BonitaInit")
      .addHumanTask("Request", "Customer")
        .addDateData("myDate", DateUtil.parseDate("2009-04-14T13:12:54.000+0200"))
        .addIntegerData("phoneNumber", 848754512)
        .addStringData("Where", "SunnyTown")
        .addStringData("message", "Nothing")
        .addDoubleData("price", new Double(20.1))
        .addConnector(Event.taskOnFinish, InputOutputConnector.class.getName(), true)
          .addOutputParameter("${output}", "message")
          .addInputParameter("thirdInput", "${price}")
          .addInputParameter("firstInput", "${phoneNumber}")
          .addInputParameter("secondInput", "${Where}")
          .addInputParameter("fourthInput", "${myDate}")
      .addTransition("Start_Request", "BonitaInit", "Request")
      .addTransition("Request_End", "Request", "BonitaEnd")
      .done();
    return getBusinessArchive(process, getResourcesFromConnector(InputOutputConnector.class), InputOutputConnector.class, ProcessInitiatorRoleResolver.class);
  }

  public void testProcessConnector() throws Exception {
    ProcessDefinition definition = 
      ProcessBuilder.createProcess("MyProcess", null)
      .addGroup("Customer")
      .addGroupResolver(ProcessInitiatorRoleResolver.class.getName())
      .addHumanTask("start", "Customer")
        .addStringData("result")
        .addConnector(Event.taskOnFinish, MyProcessConnector.class.getName(), true)
          .addOutputParameter("${result}", "result")
      .done();

    ProcessDefinition process = getManagementAPI().deploy(getBusinessArchive(
          definition, getResourcesFromConnector(MyProcessConnector.class), MyProcessConnector.class, ProcessInitiatorRoleResolver.class));
    ProcessDefinitionUUID processUUID = process.getUUID();
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);
    Set<ActivityInstance> actIntances =
      getQueryRuntimeAPI().getActivityInstances(instanceUUID, "start");
    ActivityInstance ai = actIntances.iterator().next();

    executeTask(instanceUUID, ai.getActivityName());

    String message = (String)
        getQueryRuntimeAPI().getActivityInstanceVariable(ai.getUUID(), "result");
    assertEquals(processUUID + "|" + instanceUUID +"|" + ai.getUUID(), message);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

	public void testLocalVariableWithInitialValueInACondition() throws Exception {
		ProcessDefinition definition =
  		ProcessBuilder.createProcess("InscriptionEnfant", "1.0")
  		.addStringData("enseignantsConcernes", "john,jack")
  		.addSystemTask("start")
  		  .addBooleanData("valide", false)
  		.addSystemTask("end")
  		.addTransition("se", "start", "end")
  		  .addCondition("valide")
		.done();

		ProcessDefinition process = getManagementAPI().deploy(getBusinessArchive(definition));
    ProcessDefinitionUUID processUUID = process.getUUID();
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
	}
	
	public void testLocalVariableWithInitialValueInAConditionOnAGate() throws Exception {
		ProcessDefinition definition =
  		ProcessBuilder.createProcess("InscriptionEnfant", "1.0")
  		.addGroup("Initiator")
  		  .addGroupResolver(ProcessInitiatorRoleResolver.class.getName())
  		.addHumanTask("start", "Initiator")
  		  .addBooleanData("valide", true)
  		.addSystemTask("end")
  		.addDecisionNode("gate1")
  		.addTransition("sg", "start", "gate1")
  		.addTransition("ge", "gate1", "end")
  		  .addCondition("valide")
		.done();

		ProcessDefinition process = getManagementAPI().deploy(getBusinessArchive(definition, null, ProcessInitiatorRoleResolver.class));
    ProcessDefinitionUUID processUUID = process.getUUID();
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    Set<ActivityInstance> actIntances =
      getQueryRuntimeAPI().getActivityInstances(instanceUUID, "start");
    ActivityInstance ai = actIntances.iterator().next();
    getRuntimeAPI().startTask(ai.getUUID(), true);
    getRuntimeAPI().setActivityInstanceVariable(ai.getUUID(), "valide", true);
  	getRuntimeAPI().finishTask(ai.getUUID(), true);
  	
  	checkState(instanceUUID, ActivityState.FINISHED, "start");
  	checkState(instanceUUID, ActivityState.FAILED, "gate1");
    checkActivityInstanceNotExist(instanceUUID, "end");
    checkState(instanceUUID, InstanceState.STARTED);
  	
    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
	}
	
	public void testLocalVariableWithoutInitialValueInAConditionOnAGate() throws Exception {
		ProcessDefinition definition =
  		ProcessBuilder.createProcess("InscriptionEnfant", "1.0")
  		.addGroup("Initiator")
  		  .addGroupResolver(ProcessInitiatorRoleResolver.class.getName())
  		.addHumanTask("start", "Initiator")
  		  .addBooleanData("valide")
  		.addSystemTask("end")
  		.addDecisionNode("gate1")
  		.addTransition("sg", "start", "gate1")
  		.addTransition("ge", "gate1", "end")
  		  .addCondition("valide")
		.done();

		ProcessDefinition process = getManagementAPI().deploy(getBusinessArchive(definition, null, ProcessInitiatorRoleResolver.class));
    ProcessDefinitionUUID processUUID = process.getUUID();
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    Set<ActivityInstance> actIntances =
      getQueryRuntimeAPI().getActivityInstances(instanceUUID, "start");
    ActivityInstance ai = actIntances.iterator().next();
    getRuntimeAPI().startTask(ai.getUUID(), true);
    getRuntimeAPI().finishTask(ai.getUUID(), true);
    
    checkState(instanceUUID, ActivityState.FINISHED, "start");
    checkState(instanceUUID, ActivityState.FAILED, "gate1");
    checkActivityInstanceNotExist(instanceUUID, "end");
    checkState(instanceUUID, InstanceState.STARTED);
    
    getManagementAPI().deleteProcess(processUUID);
	}
	
	public void testExecuteAConenctorAndConvertLongToString() throws Exception {
		ProcessDefinition definition =
  		ProcessBuilder.createProcess("groovyClass", "1.0")
  		.addStringData("value")
  		.addHuman("john")
  		.addSystemTask("start")
  		  .addConnector(Event.automaticOnEnter, GroovyConnector.class.getName(), true)
  		    .addInputParameter("script", "(long)42")
          .addOutputParameter("${2 * result}", "value")
      .addHumanTask("end", "john")
      .addTransition("st", "start", "end")
		.done();

		ProcessDefinition process = getManagementAPI().deploy(getBusinessArchive(definition, null, GroovyConnector.class));
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(process.getUUID());
    assertEquals("84", getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "value"));
    getManagementAPI().deleteProcess(process.getUUID());
	}
	
	public void testExecuteAConnectorAndConvertStringToInteger() throws Exception {
		ProcessDefinition definition =
  		ProcessBuilder.createProcess("groovyClass", "1.0")
  		.addIntegerData("value")
  		.addHuman("john")
  		.addSystemTask("start")
  		  .addConnector(Event.automaticOnEnter, GroovyConnector.class.getName(), true)
  		    .addInputParameter("script", "'8'")
          .addOutputParameter("${result}", "value")
      .addHumanTask("end", "john")
      .addTransition("st", "start", "end")
		.done();

		ProcessDefinition process = getManagementAPI().deploy(getBusinessArchive(definition, null, GroovyConnector.class));
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(process.getUUID());
    assertEquals(8, getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "value"));
    getManagementAPI().deleteProcess(process.getUUID());
	}
	
	public void testExecuteAConnectorWithoutADescriptor() throws Exception {
		ProcessDefinition definition =
  		ProcessBuilder.createProcess("groovyClass", "1.0")
  		.addStringData("value", "empty")
  		.addHuman("john")
  		.addHumanTask("start", "john")
  		  .addConnector(Event.taskOnStart, NoDescriptorConnector.class.getName(), true)
  		    .addInputParameter("age", 15)
  		    .addInputParameter("name", "John")
          .addOutputParameter("result", "value")
      .addHumanTask("end", "john")
      .addTransition("start", "end")
		.done();

    ProcessDefinition process = getManagementAPI().deploy(getBusinessArchive(
    		definition, null, NoDescriptorConnector.class));

    ProcessDefinitionUUID processUUID = process.getUUID();
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    String actual = (String) getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "value");
    assertEquals("empty", actual);
    loginAs("john", "bpm");
    executeTask(instanceUUID, "start");
    actual = (String) getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "value");
    assertEquals("John: 15", actual);
    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
	}
	
	public void testExecuteAConnectorWithABadDescriptor() throws Exception {
    ProcessDefinition definition =
      ProcessBuilder.createProcess("groovyClass", "1.0")
      .addStringData("num", "15")
      .addIntegerData("value", 0)
      .addHuman("john")
      .addHumanTask("start", "john")
        .addConnector(Event.taskOnStart, BadDescriptorConnector.class.getName(), true)
          .addInputParameter("number", "${num}")
          .addOutputParameter("result", "value")
      .addHumanTask("end", "john")
      .addTransition("start", "end")
    .done();

    ProcessDefinition process = getManagementAPI().deploy(getBusinessArchive(
        definition, getResourcesFromConnector(BadDescriptorConnector.class), BadDescriptorConnector.class));

    ProcessDefinitionUUID processUUID = process.getUUID();
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    Integer actual = (Integer) getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "value");
    assertEquals(Integer.valueOf(0), actual);
    loginAs("john", "bpm");
    executeTask(instanceUUID, "start");
    actual = (Integer) getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "value");
    assertEquals(Integer.valueOf(15), actual);
    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }
	
	public void testClassNotFound() throws BonitaException, IOException {
    ProcessDefinition definition =
      ProcessBuilder.createProcess("NoClassDefFound", "1.0")
      .addSystemTask("start")
        .addConnector(Event.automaticOnEnter, "org.ow2.bonita.integration.connector.test.GConnector", true)
    .done();

    String className = "org.ow2.bonita.integration.connector.test.GConnector";
    String classFile = "GConnector.bytecode";
    byte[] clazzValue = Misc.getAllContentFrom(getClass().getResource(classFile));
    String clazzKey = className + ".class";
    Map<String, byte[]> resources = new HashMap<String, byte[]>();
    Map<String, byte[]> jarResources = new HashMap<String, byte[]>();
    jarResources.put(clazzKey, clazzValue);
    byte[] jar = Misc.generateJar(jarResources);
    resources.put("myJar.jar", jar);

    try {
      getManagementAPI().deploy(getBusinessArchive(definition, resources));
      fail("A lib is missing for GConnector class");
    } catch (DeploymentException e) {
      assertTrue(e.getMessage().contains(
          "No Class available with classname: org.ow2.bonita.integration.connector.test.GConnector"));
    } 
  }

	public void testProcessDefinitionInAConnector() throws Exception {
	  ProcessDefinition definition =
	    ProcessBuilder.createProcess("processDefinition", "1.0")
	    .addHuman(getLogin())
	    .addStringData("processDefUUID")
	    .addHumanTask("one", getLogin())
	      .addConnector(Event.taskOnFinish, SetVarConnector.class.getName(), true)
	        .addInputParameter("variableName", "processDefUUID")
	        .addInputParameter("value", "${processDefinition.getUUID().getValue()}")
	    .done();

	  ProcessDefinition process = getManagementAPI().deploy(getBusinessArchive(
        definition, null, SetVarConnector.class));

    ProcessDefinitionUUID processUUID = process.getUUID();
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    String actual = (String) getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "processDefUUID");
    assertNull(actual);
    executeTask(instanceUUID, "one");
    actual = (String) getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "processDefUUID");
    assertNotNull(actual);
    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
	}

	public void testProcessInstanceInAConnector() throws Exception {
    ProcessDefinition definition =
      ProcessBuilder.createProcess("processDefinition", "1.0")
      .addHuman(getLogin())
      .addStringData("processInstUUID")
      .addHumanTask("one", getLogin())
        .addConnector(Event.taskOnFinish, SetVarConnector.class.getName(), true)
          .addInputParameter("variableName", "processInstUUID")
          .addInputParameter("value", "${processInstance.getUUID().getValue()}")
      .done();

    ProcessDefinition process = getManagementAPI().deploy(getBusinessArchive(
        definition, null, SetVarConnector.class));

    ProcessDefinitionUUID processUUID = process.getUUID();
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    String actual = (String) getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "processInstUUID");
    assertNull(actual);
    executeTask(instanceUUID, "one");
    actual = (String) getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "processInstUUID");
    assertNotNull(actual);
    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

	public void testActivityInstanceInAConnector() throws Exception {
    ProcessDefinition definition =
      ProcessBuilder.createProcess("processDefinition", "1.0")
      .addHuman(getLogin())
      .addStringData("activityInstUUID")
      .addHumanTask("one", getLogin())
        .addConnector(Event.taskOnFinish, SetVarConnector.class.getName(), true)
          .addInputParameter("variableName", "activityInstUUID")
          .addInputParameter("value", "${activityInstance.getUUID().getValue()}")
      .done();

    ProcessDefinition process = getManagementAPI().deploy(getBusinessArchive(
        definition, null, SetVarConnector.class));

    ProcessDefinitionUUID processUUID = process.getUUID();
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    String actual = (String) getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "activityInstUUID");
    assertNull(actual);
    executeTask(instanceUUID, "one");
    actual = (String) getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "activityInstUUID");
    assertNotNull(actual);
    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

	public void testLoggedUserInAConnector() throws Exception {
    ProcessDefinition definition =
      ProcessBuilder.createProcess("processDefinition", "1.0")
      .addHuman(getLogin())
      .addStringData("user")
      .addHumanTask("one", getLogin())
        .addConnector(Event.taskOnFinish, SetVarConnector.class.getName(), true)
          .addInputParameter("variableName", "user")
          .addInputParameter("value", "${loggedUser}")
      .done();

    ProcessDefinition process = getManagementAPI().deploy(getBusinessArchive(
        definition, null, SetVarConnector.class));

    ProcessDefinitionUUID processUUID = process.getUUID();
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    String actual = (String) getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "user");
    assertNull(actual);
    executeTask(instanceUUID, "one");
    actual = (String) getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "user");
    assertEquals(getLogin(), actual);
    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

	public void testConnectorOnEnterOfAnAutomaticTask() throws Exception {
	  ProcessDefinition process =
	    ProcessBuilder.createProcess("at1", "1.0")
	    .addStringData("name", "james")
	    .addSystemTask("auto")
	      .addConnector(Event.automaticOnEnter, SetVarConnector.class.getName(), true)
	        .addInputParameter("variableName", "name")
	        .addInputParameter("value", "john")
	    .done();

	  process = getManagementAPI().deploy(getBusinessArchive(process,
	      getResourcesFromConnector(SetVarConnector.class), SetVarConnector.class));
    ProcessDefinitionUUID processUUID = process.getUUID();
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    String actual = (String) getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "name");
    assertEquals("john", actual);
    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
	}

  public void testConnectorOnExitOfAnAutomaticTask() throws Exception {
    ProcessDefinition process =
      ProcessBuilder.createProcess("at1", "1.0")
      .addStringData("name", "james")
      .addSystemTask("auto")
        .addConnector(Event.automaticOnExit, SetVarConnector.class.getName(), true)
          .addInputParameter("variableName", "name")
          .addInputParameter("value", "john")
      .done();

    process = getManagementAPI().deploy(getBusinessArchive(process,
        getResourcesFromConnector(SetVarConnector.class), SetVarConnector.class));
    ProcessDefinitionUUID processUUID = process.getUUID();
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    String actual = (String) getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "name");
    assertEquals("john", actual);
    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testConnectorOnEnterOfADecisionNode()  throws Exception {
    ProcessDefinition process =
      ProcessBuilder.createProcess("at1", "1.0")
      .addStringData("name", "james")
      .addDecisionNode("node")
        .addConnector(Event.automaticOnEnter, SetVarConnector.class.getName(), true)
          .addInputParameter("variableName", "name")
          .addInputParameter("value", "john")
      .done();

    process = getManagementAPI().deploy(getBusinessArchive(process,
        getResourcesFromConnector(SetVarConnector.class), SetVarConnector.class));
    ProcessDefinitionUUID processUUID = process.getUUID();
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    String actual = (String) getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "name");
    assertEquals("john", actual);
    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testConnectorOnExitOfADecisionNode() throws Exception {
    ProcessDefinition process =
      ProcessBuilder.createProcess("at1", "1.0")
      .addStringData("name", "james")
      .addDecisionNode("node")
        .addConnector(Event.automaticOnExit, SetVarConnector.class.getName(), true)
          .addInputParameter("variableName", "name")
          .addInputParameter("value", "john")
      .done();

    process = getManagementAPI().deploy(getBusinessArchive(process,
        getResourcesFromConnector(SetVarConnector.class), SetVarConnector.class));
    ProcessDefinitionUUID processUUID = process.getUUID();
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    String actual = (String) getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "name");
    assertEquals("john", actual);
    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }
  
  public void testATest() throws Exception {
    ProcessDefinition definition =
      ProcessBuilder.createProcess("groovyBaby", "1.0")
      .addObjectData("myList", ArrayList.class.getName())
      .addSystemTask("first")
        .addConnector(Event.automaticOnEnter, GroovyConnector.class.getName(), true)
          .addInputParameter("script", "myList")
      .done();
    
    definition = getManagementAPI().deploy(getBusinessArchive(definition,
        getResourcesFromConnector(GroovyConnector.class), GroovyConnector.class));
    ProcessDefinitionUUID processUUID = definition.getUUID();
    getRuntimeAPI().instantiateProcess(processUUID);
    
    getManagementAPI().deleteProcess(processUUID);
  }

	/*public void testClassInBarUsedInAGroovyExpression() throws BonitaException, IOException {
    ProcessDefinition definition =
      ProcessBuilder.createProcess("groovyClass", "1.0")
      .addSystemTask("start")
      .addSystemTask("end")
      .addTransition("se", "start", "end")
        .addCondition("(new org.ow2.bonita.integration.connector.MyJavaClass()).execute(); true")
    .done();

    String className = this.getClass().getPackage().getName() + ".MyJavaClass";
    String classFile = "MyJavaClass.bytecode";
    byte[] clazzValue = Misc.getAllContentFrom(getClass().getResource(classFile));
    String clazzKey = className + ".class";
    Map<String, byte[]> resources = new HashMap<String, byte[]>();
    Map<String, byte[]> jarResources = new HashMap<String, byte[]>();
    jarResources.put(clazzKey, clazzValue);
    byte[] jar = Misc.generateJar(jarResources);
    resources.put("myJar.jar", jar);

    ProcessDefinition process = getManagementAPI().deploy(getBusinessArchive(definition, resources));
    getRuntimeAPI().instantiateProcess(process.getUUID());
    getManagementAPI().deleteProcess(process.getUUID());
  }
  
  public void testClassInBarUsedInAGroovyExpressionInAConnectorParameter() throws BonitaException, IOException {
    ProcessDefinition definition =
      ProcessBuilder.createProcess("groovyClass", "1.0")
      .addStringData("str")
      .addHuman("john")
      .addSystemTask("start")
        .addConnector(Event.automaticOnEnter, SetVarConnector.class.getName(), true)
          .addInputParameter("variableName", "str")
          .addInputParameter("value", "${(new org.ow2.bonita.integration.connector.MyJavaClass()).execute(); \"hello\"}")
      .addHumanTask("end", "john")
      .addTransition("st", "start", "end")
    .done();

    String className = this.getClass().getPackage().getName() + ".MyJavaClass";
    String classFile = "MyJavaClass.bytecode";
    byte[] clazzValue = Misc.getAllContentFrom(getClass().getResource(classFile));
    String clazzKey = className + ".class";
    Map<String, byte[]> resources = new HashMap<String, byte[]>();
    Map<String, byte[]> jarResources = new HashMap<String, byte[]>();
    jarResources.put(clazzKey, clazzValue);
    byte[] jar = Misc.generateJar(jarResources);
    resources.put("myJar.jar", jar);

    ProcessDefinition process = getManagementAPI().deploy(getBusinessArchive(definition, resources, SetVarConnector.class));
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(process.getUUID());
    assertEquals("hello", getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "str"));
    getManagementAPI().deleteProcess(process.getUUID());
  }*/

  public void testOutputConnectorIntoXML() throws Exception {
    final String separator = BonitaConstants.XPATH_VAR_SEPARATOR;
    ProcessBuilder builder = ProcessBuilder.createProcess("TestMapConnectorOutputToXMLVariables", "0.1");
    builder
    .addXMLData("rootAttribute", "<root test=\"before\"/>")
    .addXMLData("subNode", "<node><subnode>before</subnode></node>")
    .addConnector(Event.instanceOnStart, OutputConnector.class.getName(), true)
      .addOutputParameter("output", "rootAttribute" + separator + "/root/@test")
    .addHuman(getLogin()).addHumanTask("task", getLogin())
    .addXMLData("rootNode", "<root>before</root>")
    .addConnector(Event.taskOnReady, OutputConnector.class.getName(), true)
      .addOutputParameter("output", "rootNode" + separator + "/root/text()")
    .addConnector(Event.taskOnReady, OutputConnector.class.getName(), true)
      .addOutputParameter("output", "subNode" + separator + "/node/subnode/text()");
    ProcessDefinition processDef = builder.done();

    processDef = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(processDef, OutputConnector.class));
    getRuntimeAPI().instantiateProcess(processDef.getUUID());
    ActivityInstanceUUID activityUUID = getQueryRuntimeAPI().getOneTask(processDef.getUUID(), ActivityState.READY);

    XMLUnit.setIgnoreWhitespace(true);
    Document doc = Misc.generateDocument("<root>Something</root>");
    Document actual = (Document) getQueryRuntimeAPI().getVariable(activityUUID, "rootNode");
    assertTrue(XMLUnit.compareXML(doc, actual).identical());
    doc = Misc.generateDocument("<root test=\"Something\"/>");
    actual = (Document) getQueryRuntimeAPI().getVariable(activityUUID, "rootAttribute");
    assertTrue(XMLUnit.compareXML(doc, actual).identical());
    doc = Misc.generateDocument("<node><subnode>Something</subnode></node>");
    actual = (Document) getQueryRuntimeAPI().getVariable(activityUUID, "subNode");
    assertTrue(XMLUnit.compareXML(doc, actual).identical());
    
    this.getManagementAPI().deleteProcess(processDef.getUUID());
  }

  public void testAttachmentConnector() throws Exception {
    final File attachmentFile = File.createTempFile("attachment-test",".txt");

    ProcessDefinition process = ProcessBuilder.createProcess("att", "1.0")
    .addStringData("var")
    .addSystemTask("start")
      .addConnector(Event.automaticOnEnter, AttachmentConnector.class.getName(), true)
        .addInputParameter("attachment", "${myAttach}")
        .addOutputParameter("attachmentName", "var")
    .addAttachment("myAttach", attachmentFile.getAbsolutePath(), attachmentFile.getName())
    .done();

    getManagementAPI().deploy(getBusinessArchive(process, null, AttachmentConnector.class));

    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(process.getUUID());
    String var = (String) getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "var");
    assertEquals("myAttach", var);

    getManagementAPI().deleteProcess(process.getUUID());
    attachmentFile.delete();
  }

}
