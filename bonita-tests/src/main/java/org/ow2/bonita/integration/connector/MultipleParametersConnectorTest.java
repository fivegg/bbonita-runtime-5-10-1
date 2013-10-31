package org.ow2.bonita.integration.connector;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bonitasoft.connectors.java.JavaConnector;
import org.ow2.bonita.APITestCase;
import org.ow2.bonita.facade.def.element.BusinessArchive;
import org.ow2.bonita.facade.def.element.HookDefinition.Event;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.integration.connector.test.HelloWorld;
import org.ow2.bonita.util.BonitaException;
import org.ow2.bonita.util.ProcessBuilder;

public class MultipleParametersConnectorTest extends APITestCase {

  private void execute(BusinessArchive businessArchive) throws BonitaException {
    ProcessDefinition process = getManagementAPI().deploy(businessArchive);
    ProcessDefinitionUUID processUUID = process.getUUID();
    getRuntimeAPI().instantiateProcess(processUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }
  
  public void testSaySimpleHello() throws BonitaException {    
    execute(getSimpleProcess());
  }
  
  public void testSayHelloWithAParameter() throws BonitaException {
    execute(getFirstProcess());
  }

  public void testSayHelloWithTwoParameters() throws BonitaException {
    execute(getProcess());
  }

  public void testSayHelloWithTwoArrayParameters() throws BonitaException {
    execute(getProcessArray());
  }

  public void testHelloThreeParameters() throws BonitaException {
    execute(getProcessThree());
  }

  public void testSayAdditionWithMethods() throws BonitaException {
    execute(getProcessMethods());
  }

  public BusinessArchive getSimpleProcess() {    
    ProcessDefinition process = ProcessBuilder.createProcess("helloWorld", "1.0")
    .addSystemTask("go")
      .addConnector(Event.automaticOnEnter, JavaConnector.class.getName(), true)
        .addInputParameter("className", HelloWorld.class.getName())
        .addInputParameter("mainMethodName", "sayHello")
    .done();
    return getBusinessArchive(process, null, JavaConnector.class);
  }

  public BusinessArchive getFirstProcess() {
    List<Object> parameters = new ArrayList<Object>();
    parameters.add("Hello");
    
    ProcessDefinition process = ProcessBuilder.createProcess("helloWorld", "1.0")
    .addSystemTask("go")
      .addConnector(Event.automaticOnEnter, JavaConnector.class.getName(), true)
        .addInputParameter("className", HelloWorld.class.getName())
        .addInputParameter("mainMethodName", "saySomething")
        .addInputParameter("mainMethodParameters", parameters)
    .done();
    return getBusinessArchive(process, null, JavaConnector.class, HelloWorld.class);
  }

  public BusinessArchive getProcess() {
    List<Object> parameters = new ArrayList<Object>();
    parameters.add("Hello");
    parameters.add(Long.valueOf(12));
    
    ProcessDefinition process = ProcessBuilder.createProcess("helloWorld", "1.0")
      .addSystemTask("go")
        .addConnector(Event.automaticOnEnter, JavaConnector.class.getName(), true)
          .addInputParameter("className", HelloWorld.class.getName())
          .addInputParameter("mainMethodName", "sayCompositeSomething")
          .addInputParameter("mainMethodParameters", parameters)
    .done();
    return getBusinessArchive(process, null, JavaConnector.class, HelloWorld.class);
  }

  public BusinessArchive getProcessArray() {
    List<Object> parameters = new ArrayList<Object>();
    parameters.add("Hello");
    parameters.add(new Integer[]{12, 3});
    
    ProcessDefinition process = ProcessBuilder.createProcess("helloWorld", "1.0")
      .addSystemTask("go")
        .addConnector(Event.automaticOnEnter, JavaConnector.class.getName(), true)
          .addInputParameter("className", HelloWorld.class.getName())
          .addInputParameter("mainMethodName", "sayCompositeArray")
          .addInputParameter("mainMethodParameters", parameters)
    .done();
    return getBusinessArchive(process, null, JavaConnector.class, HelloWorld.class);
  }

  public BusinessArchive getProcessThree() {
    List<Object> parameters = new ArrayList<Object>();
    parameters.add("Today");
    parameters.add(Long.valueOf(3));
    parameters.add(new Date());

    ProcessDefinition process = ProcessBuilder.createProcess("helloWorld", "1.0")
      .addSystemTask("go")
        .addConnector(Event.automaticOnEnter, JavaConnector.class.getName(), true)
          .addInputParameter("className", HelloWorld.class.getName())
          .addInputParameter("mainMethodName", "sayTwoThings")
          .addInputParameter("mainMethodParameters", parameters)
    .done();
    return getBusinessArchive(process, null, JavaConnector.class, HelloWorld.class);
  }

  public BusinessArchive getProcessMethods() {
    List<Object> parameters = new ArrayList<Object>();
    parameters.add(Integer.valueOf(4));
    parameters.add(Integer.valueOf(10));

    ProcessDefinition process = ProcessBuilder.createProcess("helloWorld", "1.0")
      .addSystemTask("go")
        .addConnector(Event.automaticOnEnter, JavaConnector.class.getName(), true)
          .addInputParameter("className", HelloWorld.class.getName())
          .addInputParameter("mainMethodName", "addition")
          .addInputParameter("methodName", "add", parameters)
    .done();
    return getBusinessArchive(process, null, JavaConnector.class, HelloWorld.class);
  }
}
