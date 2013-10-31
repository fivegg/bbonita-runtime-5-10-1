package org.ow2.bonita.facade;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.security.auth.login.LoginException;

import org.bonitasoft.connectors.bonita.resolvers.ProcessInitiatorRoleResolver;
import org.jboss.resteasy.spi.BadRequestException;
import org.junit.Assert;
import org.ow2.bonita.APITestCase;
import org.ow2.bonita.facade.def.element.BusinessArchive;
import org.ow2.bonita.facade.def.element.HookDefinition.Event;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.exception.DeploymentException;
import org.ow2.bonita.facade.exception.ProcessNotFoundException;
import org.ow2.bonita.facade.exception.RoleNotFoundException;
import org.ow2.bonita.facade.exception.UndeletableInstanceException;
import org.ow2.bonita.facade.identity.Group;
import org.ow2.bonita.facade.identity.Membership;
import org.ow2.bonita.facade.identity.Role;
import org.ow2.bonita.facade.identity.User;
import org.ow2.bonita.facade.runtime.ActivityInstance;
import org.ow2.bonita.facade.runtime.ActivityState;
import org.ow2.bonita.facade.runtime.InstanceState;
import org.ow2.bonita.facade.runtime.ProcessInstance;
import org.ow2.bonita.facade.runtime.TaskInstance;
import org.ow2.bonita.facade.uuid.ActivityInstanceUUID;
import org.ow2.bonita.facade.uuid.IdFactory;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.light.LightTaskInstance;
import org.ow2.bonita.util.AccessorUtil;
import org.ow2.bonita.util.BonitaConstants;
import org.ow2.bonita.util.BonitaException;
import org.ow2.bonita.util.BonitaRuntimeException;
import org.ow2.bonita.util.BusinessArchiveFactory;
import org.ow2.bonita.util.Misc;
import org.ow2.bonita.util.ProcessBuilder;

public class ManagementAPITest extends APITestCase {

  private static final String PROCESS_ID = "process1";
  private static final String XPDLFILE = "testManagementAPI.xpdl";
  private static final String XPDLFILEV2 = "testManagementAPIV2.0.xpdl";
  private static final String XPDLFILEV2_2 = "testManagementAPIV2.0-2.xpdl";

  private void connectorInAJar(boolean inJar) throws BonitaException, IOException {
    ProcessDefinition process = ProcessBuilder.createProcess("myProcess", "1.0").addGroup("initiator").addGroupResolver(ProcessInitiatorRoleResolver.class.getName()).addStringData("myData",
        "initialValue").addHumanTask("myTask", "initiator").addConnector(Event.taskOnReady, MyConnector.class.getName(), true).addInputParameter("variableId", "myData").addInputParameter("newValue",
        "myNewValue").done();

    // xml file does not have the right name in the source code to ensure it is
    // not found in the bonita-tests.jar
    String base = this.getClass().getPackage().getName().replace('.', '/') + "/";

    byte[] xmlValue = Misc.getAllContentFrom(getClass().getResource("MyConnectorXml.xml"));
    String xmlKey = base + "MyConnector.xml";

    byte[] clazzValue = Misc.getAllContentFrom(getClass().getResource("MyConnector.bytecode"));
    String clazzKey = this.getClass().getPackage().getName().replace(".", "/") + "/" + "MyConnector.class";

    Map<String, byte[]> resources = new HashMap<String, byte[]>();
    if (inJar) {
      Map<String, byte[]> jarResources = new HashMap<String, byte[]>();
      jarResources.put(xmlKey, xmlValue);
      jarResources.put(clazzKey, clazzValue);
      byte[] jar = Misc.generateJar(jarResources);
      resources.put("myJar.jar", jar);
    } else {
      resources.put(xmlKey, xmlValue);
      resources.put(clazzKey, clazzValue);
    }
    process = getManagementAPI().deploy(getBusinessArchive(process, resources, ProcessInitiatorRoleResolver.class));

    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(process.getUUID());

    String myData = (String) getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "myData");
    assertNotNull(myData);
    assertEquals("myNewValue", myData);

    getManagementAPI().deleteProcess(process.getUUID());
  }

  public void testConnectorInAJarInsideBar() throws BonitaException, IOException {
    connectorInAJar(true);
  }

  public void testConnectorInABar() throws IOException, BonitaException {
    connectorInAJar(false);
  }

  @SuppressWarnings("rawtypes")
  public void testDeployCommandInBar() throws BonitaException, IOException, ClassNotFoundException {
    final String processId = "testManagementAPICommand";
    final String xpdlFile = processId + ".xpdl";

    final Class[] classes = { GetProcessIdCommand.class };
    final URL url = this.getClass().getResource(xpdlFile);

    ProcessDefinition clientProcess = ProcessBuilder.createProcessFromXpdlFile(url);
    BusinessArchive businessArchive = BusinessArchiveFactory.getBusinessArchive(clientProcess, classes);
    final ProcessDefinition process = getManagementAPI().deploy(businessArchive);
    assertNotNull(process);

    final ProcessDefinitionUUID processUUID = process.getUUID();
    try {
      final String returnedProcessId = getCommandAPI().execute(new GetProcessIdCommand(processUUID), processUUID);
      assertEquals(processId, returnedProcessId);
    } catch (final Exception e) {
      if (!(e instanceof ProcessNotFoundException)) {
        throw new BonitaRuntimeException(e);
      }
      throw (ProcessNotFoundException) e;
    } finally {
      getManagementAPI().disable(processUUID);
      getManagementAPI().deleteProcess(processUUID);
    }
  }

  public void testLoggedUser() throws LoginException {
    assertEquals("Wrong logged user", getLogin(), getManagementAPI().getLoggedUser());

    loginAs("jack", "bpm");
    assertEquals("Wrong logged user", "jack", getManagementAPI().getLoggedUser());

    loginAs("james", "bpm");
    assertEquals("Wrong logged user", "james", getManagementAPI().getLoggedUser());

    loginAs("john", "bpm");
    assertEquals("Wrong logged user", "john", getManagementAPI().getLoggedUser());

    loginAs("admin", "bpm");
    assertEquals("Wrong logged user", "admin", getManagementAPI().getLoggedUser());
  }

  @SuppressWarnings("rawtypes")
  public void testDeployBusinessArchive() throws BonitaException, IOException, ClassNotFoundException, URISyntaxException {
    File tmpdir = new File(System.getProperty("java.io.tmpdir"));
    tmpdir.mkdirs();

    // Common utilities for the test.
    final Class[] emptyClasses = {};
    final Class[] classes = { Accept.class, AcceptGlobal.class };
    final URL url = this.getClass().getResource(XPDLFILE);

    ProcessDefinition clientProcess = ProcessBuilder.createProcessFromXpdlFile(url);
    BusinessArchive businessArchive = BusinessArchiveFactory.getBusinessArchive(clientProcess, classes);
    ProcessDefinition processDefinition = getManagementAPI().deploy(businessArchive);
    // Check deployment is OK
    checkDeployment(processDefinition);
    getManagementAPI().disable(processDefinition.getUUID());
    getManagementAPI().deleteProcess(processDefinition.getUUID());

    // **** Test deployBar(bytesBar) with valid parameter but empty classes
    // table
    businessArchive = BusinessArchiveFactory.getBusinessArchive(clientProcess, emptyClasses);
    processDefinition = getManagementAPI().deploy(businessArchive);
    // Check deployment is OK
    checkDeployment(processDefinition);
    getManagementAPI().disable(processDefinition.getUUID());

    File file = Misc.createTempFile(PROCESS_ID, ".bar", tmpdir);
    businessArchive = BusinessArchiveFactory.getBusinessArchive(clientProcess, classes);
    BusinessArchiveFactory.generateBusinessArchiveFile(file, businessArchive);
    URL urlBar = new File(file.getAbsolutePath()).toURI().toURL();

    // Do not delete process !!!

    // Test DeploymentException: raised if A process with id process1 and
    // version 1.0 has already been deployed.
    // Please change the version.
    try {
      processDefinition = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(urlBar));
      checkDeployment(processDefinition);
      fail("Check DeploymentException raised if same version of the process is deployed twice even if undeployed done.");
    } catch (final DeploymentException de) {
      // assertEquals("A process with id process1 and version 1.0 has already been deployed."
      // + "Please change the version.", de.getMessage());
    } 

    file.delete();
    getManagementAPI().deleteProcess(processDefinition.getUUID());

    // (valid parameter) Check package with new version of processes can be
    // deployed (with also new version of the package)

    file = Misc.createTempFile(PROCESS_ID, ".bar", tmpdir);

    final URL urlV2 = this.getClass().getResource(XPDLFILEV2);
    clientProcess = ProcessBuilder.createProcessFromXpdlFile(urlV2);
    businessArchive = BusinessArchiveFactory.getBusinessArchive(clientProcess, classes);
    BusinessArchiveFactory.generateBusinessArchiveFile(file, businessArchive);

    urlBar = new File(file.getAbsolutePath()).toURI().toURL();

    processDefinition = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(urlBar));
    checkDeployment(processDefinition);

    assertTrue(file.delete());
    getManagementAPI().deleteProcess(processDefinition.getUUID());

    // **** giving a null bar (bytes table)
    final BusinessArchive bar = null;
    try {
      getManagementAPI().deploy(bar);
      fail("Check null bar");
    } catch (final IllegalArgumentException e) {
      // ok
      assertTrue(e.getMessage(), e.getMessage().contains("Some parameters are null in org.ow2.bonita.facade.impl.ManagementAPIImpl.deploy()"));
    } catch (final BadRequestException e){
      //REST
    }

    // **** Check if the URL contain an empty archive

    final File urlonBarEmptyFile = Misc.createTempFile(XPDLFILEV2, ".bar", tmpdir);
    urlonBarEmptyFile.deleteOnExit();
    final URL urlonBarEmpty = urlonBarEmptyFile.toURI().toURL();
    try {
      getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(urlonBarEmpty));
      fail("Check DeploymentException raised if no xpdl into the bar file");
    } catch (final Exception e) {
      // ok
    }

    // **** test no archive at the url
    final File tempFile = Misc.createTempFile("toto", ".ext", tmpdir);
    tempFile.deleteOnExit();
    assertTrue(tempFile.delete());
    final URL urlonNoArchive = tempFile.toURI().toURL();
    try {
      BusinessArchiveFactory.getBusinessArchive(urlonNoArchive);
      fail("Check DeploymentException raised if no xpdl into the bar file");
    } catch (final FileNotFoundException e) {
      // ok
    } finally {
      Misc.deleteDir(tempFile);
    }
  }

  // Testing deploy with parameters:
  // - org.ow2.bonita.deployment.Deployment
  // - java.net.URL, java.lang.Class<?>[]

  public void testDeploy() throws BonitaException {
    final URL xpdlUrl = this.getClass().getResource(XPDLFILE);
    final ProcessDefinition processDefinition = getManagementAPI().deploy(getBusinessArchiveFromXpdl(xpdlUrl, Accept.class));
    // Check deployment is OK
    checkDeployment(processDefinition);
    getManagementAPI().disable(processDefinition.getUUID());
    getManagementAPI().deleteProcess(processDefinition.getUUID());

    // IllegalArgumentException
    try {
      getManagementAPI().deploy(null);
      fail("Check IllegalArgumentException");
    } catch (final IllegalArgumentException e) {
      // ok
    } catch (final BadRequestException e){
      //REST
    }
  }

  // Testing deployZip with parameters:
  // - java.net.URL
  // - byte[]

  public void testDeployZip() throws BonitaException {
    // TODO ??
  }

  // Testing deployClass
  // covers methods with parameters being:
  // - byte[]

  public void testDeployClass() throws BonitaException, IOException, ClassNotFoundException {
    // IllegalArgumentException
    try {
      getManagementAPI().deployJar(null, null);
      fail("Check null bar");
    } catch (final IllegalArgumentException e) {
      // ok
      assertTrue(e.getMessage().contains("Some parameters are null"));
    } catch (NullPointerException e) {
      // Ok REST
      StackTraceElement firstElement = e.getStackTrace()[0];
      assertTrue(firstElement.getClassName().equals("org.jboss.resteasy.client.ClientRequest"));
      assertTrue(firstElement.getMethodName().equals("toString"));
    }

    // valid param
    getManagementAPI().deployJar("myJar.jar", Misc.generateJar(Accept.class));

    // Invalid test case : impossible to deploy 2 times at common ClassLoader
    try {
      getManagementAPI().deployJar("myJar.jar", Misc.generateJar(Accept.class));
      fail("Check impossible to deploy 2 times same class");
    } catch (final DeploymentException e) {
      assertEquals(Misc.getStackTraceFrom(e), "A jar with name: myJar.jar already exists in repository.", e.getMessage());
    } 
    getManagementAPI().removeJar("myJar.jar");
    // Valid test case:
    // Deploy the class into the Globalclassloader
    // Try to deploy class into the packageClassloader (class into the deploy
    // bar file)
    getManagementAPI().deployJar("myJar.jar", Misc.generateJar(Accept.class));
    final java.lang.Class<?>[] classes = { Accept.class };
    URL url = this.getClass().getResource(XPDLFILE);
    ProcessDefinition clientProcess = ProcessBuilder.createProcessFromXpdlFile(url);
    BusinessArchive businessArchive = BusinessArchiveFactory.getBusinessArchive(clientProcess, classes);
    ProcessDefinition processDefinition = getManagementAPI().deploy(businessArchive);
    ProcessDefinitionUUID processUUID = processDefinition.getUUID();
    final ProcessInstanceUUID instanceUUID1 = getRuntimeAPI().instantiateProcess(processUUID);
    // Check it's possible to remove the global class (not used for the
    // instantiated process)
    getManagementAPI().removeJar("myJar.jar");
    // Check instantiate is always possible
    final ProcessInstanceUUID instanceUUID2 = getRuntimeAPI().instantiateProcess(processUUID);
    getRuntimeAPI().deleteProcessInstance(instanceUUID1);
    getRuntimeAPI().deleteProcessInstance(instanceUUID2);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);

    // Valid test case:
    // Deploy class into the packageClassloader (class into the deploy bar file)
    // Try to deploy the class into the Globalclassloader
    processDefinition = getManagementAPI().deploy(businessArchive);
    getManagementAPI().deployJar("myJar.jar", Misc.generateJar(Accept.class));

    // undeploy package and classes before next test
    getManagementAPI().removeJar("myJar.jar");
    processUUID = processDefinition.getUUID();

    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  // Testing deployClasses
  // covers methods with parameters being:
  // - java.util.Set<byte[]>,
  //
  public void testDeployClasses() throws BonitaException, IOException {
    getManagementAPI().deployJar("jar.jar", Misc.generateJar(Accept.class));
    getManagementAPI().removeJar("jar.jar");

    // TODO: test with more than one class.
  }

  public void testRemoveClasses() throws BonitaException, IOException {
    getManagementAPI().deployJar("jar1.jar", Misc.generateJar(Accept.class));

    // invalid case: not existing class
    try {
      getManagementAPI().removeJar("wrong.jar");
    } catch (final DeploymentException e) {
      assertEquals("Bonita Error: bai_MAPII_6\nThere is no class defined in global class repository " + "with name: \nwrong.jar", e.getMessage());
    } 

    // invalid case : null className
    try {
      getManagementAPI().removeJar(null);
      fail("must get an error with a null className");
    } catch (final IllegalArgumentException e) {
      // ok
    } catch (NullPointerException e) {
      // Ok REST
      StackTraceElement firstElement = e.getStackTrace()[0];
      assertTrue(firstElement.getClassName().equals("org.jboss.resteasy.client.ClientRequest"));
      assertTrue(firstElement.getMethodName().equals("toString"));
    }

    // **** Valid case
    getManagementAPI().removeJar("jar1.jar");

    // **** Invalid test case: Deploy globally class A. deploy a package using
    // class A. Try to remove class A
    // global deploy of the class : AcceptGlobal
    getManagementAPI().deployJar("global.jar", Misc.generateJar(AcceptGlobal.class));

    // Process deploy of the class : Accept
    final java.lang.Class<?>[] processClasses = { Accept.class };
    final URL xpdlUrl = this.getClass().getResource(XPDLFILEV2);
    BusinessArchive businessArchive = getBusinessArchiveFromXpdl(xpdlUrl, processClasses);
    final ProcessDefinition process1 = getManagementAPI().deploy(businessArchive);

    final URL xpdlUrl2 = this.getClass().getResource(XPDLFILEV2_2);
    BusinessArchive businessArchive2 = getBusinessArchiveFromXpdl(xpdlUrl2, processClasses);
    final ProcessDefinition process2 = getManagementAPI().deploy(businessArchive2);

    // Check deployment is OK
    checkDeployment(process1);

    // valid test case: if the package is undeployed the class can be
    // undeployed.
    getManagementAPI().disable(process1.getUUID());
    getManagementAPI().deleteProcess(process1.getUUID());
    getManagementAPI().disable(process2.getUUID());
    getManagementAPI().deleteProcess(process2.getUUID());
    getManagementAPI().removeJar("global.jar");
  }

  //
  // testing undeploy

  public void testUndeploy() throws BonitaException, IOException {
    // **** Valid parameter
    // already done into previous test.

    // **** null parameter
    try {
      ProcessDefinitionUUID def = null;
      getManagementAPI().disable(def);
      fail("Check IllegalArgumentException raised if null parameter");
    } catch (final IllegalArgumentException e) {
      // ok
      assertTrue(e.getMessage().contains("Some parameters are null in org.ow2.bonita.facade.impl.ManagementAPIImpl.disable()"));
    } catch (final NullPointerException e) {
      // Ok REST
      StackTraceElement firstElement = e.getStackTrace()[0];
      assertTrue(firstElement.getClassName().equals("org.jboss.resteasy.client.ClientRequest"));
      assertTrue(firstElement.getMethodName().equals("toString"));
    }

    // **** Not existing Package
    final ProcessDefinitionUUID badProcessDefinitionUUID = IdFactory.getNewProcessUUID();

    try {
      getManagementAPI().disable(badProcessDefinitionUUID);
      fail("Check DeploymentException raised if the package does not exists in the journal");
    } catch (final DeploymentException de) {
      final String expected = "process " + badProcessDefinitionUUID + " not found in journal!";
      assertTrue("Expected: " + expected + ", result: " + de.getMessage(), de.getMessage().contains(expected));
    } 

    // now it's possible to undeploy package with not finished instance
    final java.lang.Class<?>[] processClasses = { Accept.class, AcceptGlobal.class };
    final URL xpdlUrl = this.getClass().getResource(XPDLFILEV2);
    BusinessArchive businessArchive = getBusinessArchiveFromXpdl(xpdlUrl, processClasses);
    ProcessDefinition processDefinition = getManagementAPI().deploy(businessArchive);
    checkDeployment(processDefinition);
    final ProcessDefinitionUUID processDefinitionUUID = processDefinition.getUUID();
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processDefinitionUUID);

    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().disable(processDefinitionUUID);
    getManagementAPI().deleteProcess(processDefinitionUUID);
  }

  public void testDeleteProcess() throws BonitaException {
    // check IllegalArgumentException with null parameter
    // check undeployed packages are deleted and then we can deploy it again
    // check undeletablepackageException
    // check package, processes and instances are deleted
    // check an exception is thrown if the package doesn't exist

    try {
      getManagementAPI().deleteProcess(null);
      fail("IllegalArgumentException must be thrown with null parameter");
    } catch (final IllegalArgumentException e) {
      // Test pass
    } catch (final NullPointerException e) {
      // ok REST
      StackTraceElement firstElement = e.getStackTrace()[0];
      assertTrue(firstElement.getClassName().equals("org.jboss.resteasy.client.ClientRequest"));
      assertTrue(firstElement.getMethodName().equals("toString"));
    }

    URL xpdlUrl = this.getClass().getResource("deletePackage.xpdl");

    // check undeployed packages are deleted
    ProcessDefinition processDefinition = getManagementAPI().deploy(getBusinessArchiveFromXpdl(xpdlUrl));
    ProcessDefinitionUUID processUUID = processDefinition.getUUID();
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
    // process
    try {
      getQueryDefinitionAPI().getProcess(processUUID);
      fail("Process must not be found as it was deleted.");
    } catch (final ProcessNotFoundException e) {
      // ok
    } 
    // check a deleted process can be deployed again
    processDefinition = getManagementAPI().deploy(getBusinessArchiveFromXpdl(xpdlUrl));
    processUUID = processDefinition.getUUID();
    getManagementAPI().deleteProcess(processUUID);

    processDefinition = getManagementAPI().deploy(getBusinessArchiveFromXpdl(xpdlUrl));
    processUUID = processDefinition.getUUID();

    Collection<TaskInstance> todolist = null;
    Set<ProcessInstance> instances = null;
    ActivityInstanceUUID taskUUID = null;

    // create 2 instances. 1 will not finished (waiting on task), 2 will
    // finished
    ProcessInstanceUUID instance1 = getRuntimeAPI().instantiateProcess(processUUID);
    assertEquals(InstanceState.STARTED, getQueryRuntimeAPI().getProcessInstance(instance1).getInstanceState());
    todolist = getQueryRuntimeAPI().getTaskList(instance1, ActivityState.READY);
    assertNotNull(todolist);
    assertFalse(todolist.isEmpty());
    assertTrue(todolist.size() == 1);

    // finish instance 1
    todolist = getQueryRuntimeAPI().getTaskList(instance1, ActivityState.READY);
    taskUUID = todolist.iterator().next().getUUID();
    getRuntimeAPI().startTask(taskUUID, true);
    getRuntimeAPI().finishTask(taskUUID, true);
    assertEquals(InstanceState.FINISHED, getQueryRuntimeAPI().getProcessInstance(instance1).getInstanceState());

    instances = getQueryRuntimeAPI().getProcessInstances(processUUID);
    assertNotNull(instances);
    assertTrue(instances.size() == 1);

    // check the process can be deleted
    getManagementAPI().deleteProcess(processUUID);

    // check everything was correctly deleted

    // process
    try {
      getQueryDefinitionAPI().getProcess(processUUID);
      fail("Process must not be found as it was deleted.");
    } catch (final ProcessNotFoundException e) {
      // ok
    } 

    // instances
    instances = getQueryRuntimeAPI().getProcessInstances(processUUID);
    assertNotNull(instances);
    assertTrue(instances.isEmpty());

    // check that if we try to delete an unknown package,
    // packageNotFoundException is thrown
    try {
      getManagementAPI().deleteProcess(processUUID);
      fail("An exception must be thrown when trying to delete instances of an unexisting process");
    } catch (final ProcessNotFoundException pnfe) {
      // ok
    } 

    // Test cannot delete when instance running
    xpdlUrl = this.getClass().getResource("deleteInstance-sub.xpdl");
    final ProcessDefinition externProcess = getManagementAPI().deploy(getBusinessArchiveFromXpdl(xpdlUrl));

    xpdlUrl = this.getClass().getResource("deletePackageSubflow.xpdl");
    final ProcessDefinition parentProcess = getManagementAPI().deploy(getBusinessArchiveFromXpdl(xpdlUrl));

    instance1 = getRuntimeAPI().instantiateProcess(parentProcess.getUUID());

    instances = getQueryRuntimeAPI().getProcessInstances(parentProcess.getUUID());
    assertNotNull(instances);
    assertEquals(1, instances.size());
    assertEquals(instances.iterator().next().getInstanceState(), InstanceState.STARTED);

    final Set<ProcessInstanceUUID> childInstances = getQueryRuntimeAPI().getProcessInstance(instance1).getChildrenInstanceUUID();
    assertNotNull(childInstances);
    assertTrue(childInstances.size() == 1);
    final ProcessInstance childInstance = getQueryRuntimeAPI().getProcessInstance(childInstances.iterator().next());
    final ProcessDefinitionUUID childPackageUUID = childInstance.getProcessDefinitionUUID();

    try {
      getManagementAPI().deleteProcess(childPackageUUID);
      fail("UndeletableInstanceException must be thrown when trying to delete package with processes instances running");
    } catch (final UndeletableInstanceException e) {
      // ok
    } 
    getRuntimeAPI().deleteAllProcessInstances(parentProcess.getUUID());
    getManagementAPI().deleteProcess(parentProcess.getUUID());
    getManagementAPI().deleteProcess(externProcess.getUUID());
  }

  public void testDeployTwiceSamePackage() throws BonitaException {
    // check two packaes can't be deployed twice
    // 1) in the journal
    // 2) 1 in journal and one in history
    final URL xpdlUrl = this.getClass().getResource("deletePackage.xpdl");
    final ProcessDefinitionUUID processUUID = getManagementAPI().deploy(getBusinessArchiveFromXpdl(xpdlUrl)).getUUID();
    try {
      getManagementAPI().deploy(getBusinessArchiveFromXpdl(xpdlUrl));
      fail("The same package can't be deployed twice !");
    } catch (final DeploymentException e) {
      // ok
    } 

    getManagementAPI().disable(processUUID);

    // now, the package has moved to history, try to deploy one again in journal
    try {
      getManagementAPI().deploy(getBusinessArchiveFromXpdl(xpdlUrl));
      fail("The same package can't be deployed twice !");
    } catch (final DeploymentException e) {
      // ok
    } 

    getManagementAPI().deleteProcess(processUUID);
  }

  public void testDeployCommandAtPackageLevel() throws BonitaException {
    final URL xpdlUrl = this.getClass().getResource("deletePackage.xpdl");
    ProcessDefinition processDefinition = getManagementAPI().deploy(getBusinessArchiveFromXpdl(xpdlUrl, GetProcessIdCommand.class));
    getManagementAPI().disable(processDefinition.getUUID());
    getManagementAPI().deleteProcess(processDefinition.getUUID());
  }

  // --------------------------------------- private methods
  // ---------------------------------------------------
  private void checkDeployment(final ProcessDefinition processDef) throws BonitaException {
    assertNotNull(processDef);
    assertEquals("process1", processDef.getName());

    final ProcessDefinition process = getQueryDefinitionAPI().getLastProcess("process1");
    final String processId = process.getName();
    assertNotNull(processId);
    assertEquals("process1", processId);
  }

  public void testGetProcessMetaData() throws BonitaException {
    URL xpdlUrl = getClass().getResource(XPDLFILE);
    ProcessDefinition process = getManagementAPI().deploy(getBusinessArchiveFromXpdl(xpdlUrl));
    ProcessDefinitionUUID processUUID = process.getUUID();

    getManagementAPI().addMetaData("one", "yksi");
    String value = getManagementAPI().getMetaData("one");
    Assert.assertEquals("yksi", value);

    getManagementAPI().addMetaData("one", "kaksi");
    value = getManagementAPI().getMetaData("one");
    Assert.assertEquals("kaksi", value);

    getManagementAPI().deleteMetaData("one");
    value = getManagementAPI().getMetaData("one");
    Assert.assertNull(value);

    getManagementAPI().deleteProcess(processUUID);
  }

  public void testIsUserAdmin() throws Exception {
    User userTest = getIdentityAPI().addUser("usertest", "passwordtest");
    Role adminRole;
    try {
      adminRole = getIdentityAPI().findRoleByName("admin");
    } catch (RoleNotFoundException e) {
      adminRole = getIdentityAPI().addRole("admin");
    }
    Group platformGroup;
    platformGroup = getIdentityAPI().getGroupUsingPath(Arrays.asList("platform"));
    if (platformGroup == null) {
      platformGroup = getIdentityAPI().addGroup("platform", null);
    }
    Membership membership = getIdentityAPI().getMembershipForRoleAndGroup(adminRole.getUUID(), platformGroup.getUUID());
    getIdentityAPI().addMembershipToUser(userTest.getUUID(), membership.getUUID());
    assertTrue(getManagementAPI().isUserAdmin("usertest"));

    getIdentityAPI().removeUserByUUID(userTest.getUUID());
  }

  public void testCheckUserCredentials() throws Exception {
    User userTest1 = getIdentityAPI().addUser("usertest1", "passwordtest1");
    assertTrue(getManagementAPI().checkUserCredentials("usertest1", "passwordtest1"));
    User userTest2 = getIdentityAPI().addUser("usertest2", "passwordtest2");
    assertTrue(getManagementAPI().checkUserCredentials("usertest2", "passwordtest2"));
    assertFalse(getManagementAPI().checkUserCredentials("usertest3", "passwordtest3"));

    getIdentityAPI().removeUserByUUID(userTest1.getUUID());
    getIdentityAPI().removeUserByUUID(userTest2.getUUID());
  }

  public void testCheckUserCredentialsWithPasswordHash() throws Exception {
    User userTest3 = getIdentityAPI().addUser("usertest3", "passwordtest3");
    assertTrue(getManagementAPI().checkUserCredentialsWithPasswordHash("usertest3", Misc.hash("passwordtest3")));
    User userTest4 = getIdentityAPI().addUser("usertest4", "passwordtest4");
    assertTrue(getManagementAPI().checkUserCredentialsWithPasswordHash("usertest4", Misc.hash("passwordtest4")));
    assertFalse(getManagementAPI().checkUserCredentialsWithPasswordHash("usertest5", Misc.hash("passwordtest5")));

    getIdentityAPI().removeUserByUUID(userTest3.getUUID());
    getIdentityAPI().removeUserByUUID(userTest4.getUUID());
  }

  public void testKeepLastUpdateValuesWhenFinishingAProcess() throws Exception {
    ProcessDefinition process = ProcessBuilder.createProcess("keepValues", "1.0-alpha")
    .addHuman(getLogin())
    .addStringData("processVar", "one")
    .addSystemTask("start")
    .addHumanTask("step1", getLogin())
    .addHumanTask("step2", getLogin())
    .addHumanTask("step3", getLogin())
    .addSystemTask("end")
    .addTransition("start", "step1")
    .addTransition("step1", "step2")
    .addTransition("step2", "step3")
    .addTransition("step3", "end")
    .done();

    getManagementAPI().deploy(getBusinessArchive(process));
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(process.getUUID());
    executeTask(instanceUUID, "step1");
    Thread.sleep(1000);
    getRuntimeAPI().setProcessInstanceVariable(instanceUUID, "processVar", "two");
    executeTask(instanceUUID, "step2");
    Thread.sleep(1000);
    ProcessInstance instanceBeforeEnding = AccessorUtil.getQueryRuntimeAPI(AccessorUtil.QUERYLIST_JOURNAL_KEY).getProcessInstance(instanceUUID);
    Thread.sleep(1000);
    executeTask(instanceUUID, "step3");
    ProcessInstance instanceAfterEnding = AccessorUtil.getQueryRuntimeAPI(AccessorUtil.QUERYLIST_HISTORY_KEY).getProcessInstance(instanceUUID);

    assertEquals(
        instanceBeforeEnding.getVariableUpdates().get(0).getDate()
        , instanceAfterEnding.getVariableUpdates().get(0).getDate());

    assertEquals(
        getActivityInstance(instanceBeforeEnding.getActivities(), "step1").getLastUpdateDate(),
        getActivityInstance(instanceAfterEnding.getActivities(), "step1").getLastUpdateDate());

    assertEquals(
        getActivityInstance(instanceBeforeEnding.getActivities(), "step2").getLastUpdateDate(),
        getActivityInstance(instanceAfterEnding.getActivities(), "step2").getLastUpdateDate());

    assertEquals(
        getActivityInstance(instanceBeforeEnding.getActivities(), "start").getLastUpdateDate(),
        getActivityInstance(instanceAfterEnding.getActivities(), "start").getLastUpdateDate());

    getManagementAPI().deleteProcess(process.getUUID());
  }

  public void testKeepLastActivityUpdateValuesWhenFinishingAProcess() throws Exception {
    ProcessDefinition process = ProcessBuilder.createProcess("keepValues", "1.0-beta")
    .addHuman(getLogin())
    .addSystemTask("start")
    .addHumanTask("step1", getLogin())
      .addStringData("step1Var", "one")
    .addHumanTask("step2", getLogin())
    .addHumanTask("step3", getLogin())
    .addSystemTask("end")
    .addTransition("start", "step1")
    .addTransition("step1", "step2")
    .addTransition("step2", "step3")
    .addTransition("step3", "end")
    .done();

    getManagementAPI().deploy(getBusinessArchive(process));
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(process.getUUID());
    Collection<LightTaskInstance> tasks = getQueryRuntimeAPI().getLightTaskList(instanceUUID, ActivityState.READY);
    getRuntimeAPI().setActivityInstanceVariable(tasks.iterator().next().getUUID(), "step1Var", "two");
    executeTask(instanceUUID, "step1");
    Thread.sleep(1000);
    executeTask(instanceUUID, "step2");
    Thread.sleep(1000);

    ProcessInstance instanceBeforeEnding = AccessorUtil.getQueryRuntimeAPI(AccessorUtil.QUERYLIST_JOURNAL_KEY).getProcessInstance(instanceUUID);
    Thread.sleep(1000);

    executeTask(instanceUUID, "step3");
    ProcessInstance instanceAfterEnding = AccessorUtil.getQueryRuntimeAPI(AccessorUtil.QUERYLIST_HISTORY_KEY).getProcessInstance(instanceUUID);

    Set<ActivityInstance> beforeActivities = instanceBeforeEnding.getActivities();
    Set<ActivityInstance> afterActivities = instanceAfterEnding.getActivities();

    assertEquals(
        getActivityInstance(beforeActivities, "step1").getLastUpdateDate(),
        getActivityInstance(afterActivities, "step1").getLastUpdateDate());

    assertEquals(
        getActivityInstance(beforeActivities, "step2").getLastUpdateDate(),
        getActivityInstance(afterActivities, "step2").getLastUpdateDate());

    assertEquals(
        getActivityInstance(beforeActivities, "start").getLastUpdateDate(),
        getActivityInstance(afterActivities, "start").getLastUpdateDate());

    getManagementAPI().deleteProcess(process.getUUID());
  }

  public void testRemoveStartEventBeforeRemovingProcessDefinition() throws Exception {
    ProcessDefinition process =
      ProcessBuilder.createProcess("itSTime", "1.1")
      .addHuman(getLogin())
      .addTimerTask("every_second", "${new Date(" + BonitaConstants.TIMER_LAST_EXECUTION + " + 1000)}")
      .addHumanTask("step1", getLogin())
      .addTransition("every_second", "step1")
      .done();

    process = getManagementAPI().deploy(getBusinessArchive(process));
    Thread.sleep(8000);
    getManagementAPI().deleteProcess(process.getUUID());
  }

}