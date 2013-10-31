package org.ow2.bonita.integration.connector;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarInputStream;

import junit.framework.Assert;

import org.bonitasoft.connectors.java.JavaConnector;
import org.bonitasoft.connectors.java.MethodCall;
import org.ow2.bonita.APITestCase;
import org.ow2.bonita.definition.InstanceInitiator;
import org.ow2.bonita.facade.def.element.BusinessArchive;
import org.ow2.bonita.facade.def.element.HookDefinition.Event;
import org.ow2.bonita.facade.def.majorElement.ActivityDefinition.JoinType;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.runtime.ActivityState;
import org.ow2.bonita.facade.runtime.TaskInstance;
import org.ow2.bonita.facade.uuid.ActivityInstanceUUID;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.integration.connector.test.HelloWorld;
import org.ow2.bonita.util.BonitaException;
import org.ow2.bonita.util.Misc;
import org.ow2.bonita.util.ProcessBuilder;

public class JavaConnectorIntegrationTest extends APITestCase {

  private void execute(BusinessArchive archive) throws BonitaException {
    ProcessDefinition process = getManagementAPI().deploy(archive);
    ProcessDefinitionUUID processUUID = process.getUUID();
    getRuntimeAPI().instantiateProcess(processUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  private void executeRequestTask(BusinessArchive archive) throws BonitaException {
    ProcessDefinition process = getManagementAPI().deploy(archive);
    ProcessDefinitionUUID processUUID = process.getUUID();
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);
    // execute the task
    final Collection<TaskInstance> tasks =
      getQueryRuntimeAPI().getTaskList(instanceUUID, ActivityState.READY);
    Assert.assertEquals(1, tasks.size());
    final TaskInstance task = tasks.iterator().next();
    Assert.assertEquals("Request", task.getActivityName());
    Assert.assertEquals(ActivityState.READY, task.getState());
    final ActivityInstanceUUID taskUUID = task.getUUID();
    getRuntimeAPI().startTask(taskUUID, false);
    getRuntimeAPI().finishTask(taskUUID, false);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testSaySimpleHello() throws Exception {
    ArrayList<ArrayList<Object>> methods = new ArrayList<ArrayList<Object>>();
    ArrayList<Object> first =  new ArrayList<Object>();
    first.add("sayHello");
    methods.add(first);

    ProcessDefinition process = ProcessBuilder.createProcess("helloWorld", "1.0")
    .addSystemTask("go")
      .addConnector(Event.automaticOnEnter, JavaConnector.class.getName(), true)
        .addInputParameter("className", HelloWorld.class.getName())
        .addInputParameter("methods", methods)
    .done();
    execute(getBusinessArchive(process, null, HelloWorld.class, JavaConnector.class, MethodCall.class));
  }

  public void testSayHelloWithAParameter() throws Exception {
    ArrayList<ArrayList<Object>> methods = new ArrayList<ArrayList<Object>>();
    ArrayList<Object> first =  new ArrayList<Object>();
    first.add("saySomething");
    first.add("Hello");
    methods.add(first);

    ProcessDefinition process = ProcessBuilder.createProcess("helloWorld", "1.0")
    .addSystemTask("go")
      .addConnector(Event.automaticOnEnter, JavaConnector.class.getName(), true)
        .addInputParameter("className", HelloWorld.class.getName())
        .addInputParameter("methods", methods)
    .done();
    execute(getBusinessArchive(process, null, HelloWorld.class, JavaConnector.class, MethodCall.class));
  }

  public void testSayHelloWithTwoParameters() throws Exception {
    List<Object> parameters = new ArrayList<Object>();
    parameters.add("Hello");
    
    ArrayList<ArrayList<Object>> methods = new ArrayList<ArrayList<Object>>();
    ArrayList<Object> first =  new ArrayList<Object>();
    first.add("sayCompositeSomething");
    first.add("Hello");
    first.add(Long.valueOf(12));
    methods.add(first);

    ProcessDefinition process = ProcessBuilder.createProcess("helloWorld", "1.0")
      .addSystemTask("go")
        .addConnector(Event.automaticOnEnter, JavaConnector.class.getName(), true)
          .addInputParameter("className", HelloWorld.class.getName())
          .addInputParameter("methods", methods)
    .done();
    execute(getBusinessArchive(process, null, HelloWorld.class, JavaConnector.class, MethodCall.class));
  }

  public void testSayHelloWithTwoArrayParameters() throws Exception {
    List<Object> parameters = new ArrayList<Object>();
    parameters.add("Hello");

    ArrayList<ArrayList<Object>> methods = new ArrayList<ArrayList<Object>>();
    ArrayList<Object> first =  new ArrayList<Object>();
    first.add("sayCompositeArray");
    first.add("Hello");
    first.add(new Integer[]{12, 3});
    methods.add(first);

    ProcessDefinition process = ProcessBuilder.createProcess("helloWorld", "1.0")
      .addSystemTask("go")
        .addConnector(Event.automaticOnEnter, JavaConnector.class.getName(), true)
          .addInputParameter("className", HelloWorld.class.getName())
          .addInputParameter("methods", methods)
    .done();
    execute(getBusinessArchive(process, null, HelloWorld.class, JavaConnector.class, MethodCall.class));
  }

  public void testHelloThreeParameters() throws Exception {
    List<Object> parameters = new ArrayList<Object>();
    parameters.add("Hello");

    ArrayList<ArrayList<Object>> methods = new ArrayList<ArrayList<Object>>();
    ArrayList<Object> first =  new ArrayList<Object>();
    first.add("sayTwoThings");
    first.add("Today");
    first.add(Long.valueOf(3));
    first.add(new Date());
    methods.add(first);

    ProcessDefinition process = ProcessBuilder.createProcess("helloWorld", "1.0")
      .addSystemTask("go")
        .addConnector(Event.automaticOnEnter, JavaConnector.class.getName(), true)
          .addInputParameter("className", HelloWorld.class.getName())
          .addInputParameter("methods", methods)
    .done();
    execute(getBusinessArchive(process, null, HelloWorld.class, JavaConnector.class, MethodCall.class));
  }

  public void testSayAdditionWithMethods() throws Exception {
    List<Object> parameters = new ArrayList<Object>();
    parameters.add("Hello");

    List<List<Object>> methods = new ArrayList<List<Object>>();
    ArrayList<Object> first =  new ArrayList<Object>();
    first.add("addition");
    methods.add(first);

    ProcessDefinition process = ProcessBuilder.createProcess("helloWorld", "1.0")
      .addSystemTask("go")
        .addConnector(Event.automaticOnEnter, JavaConnector.class.getName(), true)
          .addInputParameter("className", HelloWorld.class.getName())
          .addInputParameter("methods", methods)
    .done();
    execute(getBusinessArchive(process, null, HelloWorld.class, JavaConnector.class, MethodCall.class));
  }

  public void testJavaConnectorWithPrivateConstructorAndGroovy() throws BonitaException {
    List<List<Object>> methods = new ArrayList<List<Object>>();
    ArrayList<Object> first =  new ArrayList<Object>();
    first.add("addition");
    methods.add(first);

    ArrayList<Object> params = new ArrayList<Object>();
    params.add("${val}");
    ProcessDefinition definition =
      ProcessBuilder.createProcess("Java", "1.0")
      .addGroup("Customer")
      .addGroupResolver(InstanceInitiator.class.getName())
      .addSystemTask("BonitaEnd")
        .addJoinType(JoinType.XOR)
      .addSystemTask("BonitaInit")
      .addHumanTask("Request", "Customer")
        .addStringData("val", "12")
        .addConnector(Event.taskOnFinish, JavaConnector.class.getName(), true)
          .addInputParameter("className", HelloWorld.class.getName())
          .addInputParameter("methods", methods)
          .addInputParameter("constructorParameters", params)
      .addTransition("Start_Request", "BonitaInit", "Request")
      .addTransition("Request_End", "Request", "BonitaEnd")
      .done();
    executeRequestTask(getBusinessArchive(definition, null, HelloWorld.class, JavaConnector.class, InstanceInitiator.class, MethodCall.class));
  }
  
  public void testJavaConnectorWithPrivateConstructorAnd() throws BonitaException {
    List<List<Object>> methods = new ArrayList<List<Object>>();
    ArrayList<Object> first =  new ArrayList<Object>();
    first.add("sayHello");
    methods.add(first);

    ProcessDefinition definition =
      ProcessBuilder.createProcess("Java", "1.0")
      .addGroup("Customer")
      .addGroupResolver(InstanceInitiator.class.getName())
      .addSystemTask("BonitaEnd")
        .addJoinType(JoinType.XOR)
      .addSystemTask("BonitaInit")
      .addHumanTask("Request", "Customer")
        .addConnector(Event.taskOnFinish, JavaConnector.class.getName(), true)
          .addInputParameter("className", HelloWorld.class.getName())
          .addInputParameter("methods", methods)
      .addTransition("Start_Request", "BonitaInit", "Request")
      .addTransition("Request_End", "Request", "BonitaEnd")
      .done();
    executeRequestTask(getBusinessArchive(definition, null, HelloWorld.class, InstanceInitiator.class, JavaConnector.class, MethodCall.class));
  }
  
  public void testJavaConnectorWithTwoMethods() throws BonitaException {
    List<List<Object>> methods = new ArrayList<List<Object>>();
    ArrayList<Object> first =  new ArrayList<Object>();
    first.add("add");
    first.add(Integer.valueOf(15));
    first.add(Integer.valueOf(14));
    methods.add(first);
    ArrayList<Object> second =  new ArrayList<Object>();
    second.add("sayPosition");
    methods.add(second);

    ProcessDefinition definition =
      ProcessBuilder.createProcess("Java", "1.0")
      .addGroup("Customer")
      .addGroupResolver(InstanceInitiator.class.getName())
      .addSystemTask("BonitaEnd")
        .addJoinType(JoinType.XOR)
      .addSystemTask("BonitaInit")
      .addHumanTask("Request", "Customer")
        .addConnector(Event.taskOnFinish, JavaConnector.class.getName(), true)
          .addInputParameter("className", HelloWorld.class.getName())
          .addInputParameter("methods", methods)
      .addTransition("Start_Request", "BonitaInit", "Request")
      .addTransition("Request_End", "Request", "BonitaEnd")
      .done();
    executeRequestTask(getBusinessArchive(definition, null, HelloWorld.class, InstanceInitiator.class, JavaConnector.class, MethodCall.class));
  }
  
  public void testJavaConnectorWithAStaticMethod() throws BonitaException {
    List<List<Object>> methods = new ArrayList<List<Object>>();
    ArrayList<Object> first =  new ArrayList<Object>();
    first.add("setProperty");
    first.add("a.b");
    first.add("value");
    methods.add(first);

    ProcessDefinition definition =
      ProcessBuilder.createProcess("Java", "1.0")
      .addGroup("Customer")
      .addGroupResolver(InstanceInitiator.class.getName())
      .addSystemTask("BonitaEnd")
        .addJoinType(JoinType.XOR)
      .addSystemTask("BonitaInit")
      .addHumanTask("Request", "Customer")
        .addConnector(Event.taskOnFinish, JavaConnector.class.getName(), true)
          .addInputParameter("className", System.class.getName())
          .addInputParameter("methods", methods)
      .addTransition("Start_Request", "BonitaInit", "Request")
      .addTransition("Request_End", "Request", "BonitaEnd")
      .done();
    executeRequestTask(getBusinessArchive(definition, null, HelloWorld.class, InstanceInitiator.class, JavaConnector.class, MethodCall.class));
  }
  
  public void testJavaConnectorWithStaticMethods() throws BonitaException {
    List<List<Object>> methods = new ArrayList<List<Object>>();
    ArrayList<Object> first =  new ArrayList<Object>();
    first.add("nanoTime");
    methods.add(first);

    ProcessDefinition definition =
      ProcessBuilder.createProcess("Java", "1.0")
      .addGroup("Customer")
      .addGroupResolver(InstanceInitiator.class.getName())
      .addSystemTask("BonitaEnd")
        .addJoinType(JoinType.XOR)
      .addSystemTask("BonitaInit")
      .addHumanTask("Request", "Customer")
        .addConnector(Event.taskOnFinish, JavaConnector.class.getName(), true)
          .addInputParameter("className", System.class.getName())
          .addInputParameter("methods", methods)
      .addTransition("Start_Request", "BonitaInit", "Request")
      .addTransition("Request_End", "Request", "BonitaEnd")
      .done();
    executeRequestTask(getBusinessArchive(definition, null, HelloWorld.class, InstanceInitiator.class, JavaConnector.class, MethodCall.class));
  }

  public void testJavaClassInABarInsideAJar() throws Exception {
    javaClassInABar(true);
  }

  public void testJavaClassInABar() throws Exception {
    javaClassInABar(false);
  }

  private void javaClassInABar(boolean inJar) throws Exception {
    //class file which is in the resources. MyJavaClass is a simple java class with :
    //public void execute() {LOG.severe("In " + this.getClass().getName());}
    String className = this.getClass().getPackage().getName() + ".MyJavaClass";

    List<List<Object>> methods = new ArrayList<List<Object>>();
    ArrayList<Object> first =  new ArrayList<Object>();
    first.add("execute");
    methods.add(first);

    ProcessDefinition process = ProcessBuilder.createProcess("myProcessInBar" + inJar, "1.0")
    .addSystemTask("myTask")
    .addConnector(Event.automaticOnEnter, JavaConnector.class.getName(), true)
      .addInputParameter("className", className)
      .addInputParameter("methods", methods)
    .done();

    final String jarFileName = "myJavaClass.jar";
    final byte[] jarContent =  Misc.getAllContentFrom(this.getClass().getResource(jarFileName));
    Map<String, byte[]> resources = new HashMap<String, byte[]>();
    if (inJar) {
      resources.put("myJar.jar",jarContent);
    } else {
      JarInputStream jis = new JarInputStream(new ByteArrayInputStream(jarContent));
      Map<String, byte[]> jarEntries = Misc.getJarEntries(jis, jarFileName);
      jis.close();
      String clazzKey = className.replace(".", "/") + ".class";
      byte[] clazzValue = jarEntries.get(clazzKey);
      resources.put(clazzKey, clazzValue);
    }
    process = getManagementAPI().deploy(getBusinessArchive(process, resources, JavaConnector.class, MethodCall.class));
    getRuntimeAPI().instantiateProcess(process.getUUID());
    getManagementAPI().deleteProcess(process.getUUID());
  }

}
