package org.ow2.bonita.multitenancy;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import junit.framework.TestCase;

import org.bonitasoft.connectors.bonita.SetVarConnector;
import org.hibernate.cfg.Configuration;
import org.ow2.bonita.env.EnvConstants;
import org.ow2.bonita.env.GlobalEnvironmentFactory;
import org.ow2.bonita.facade.ManagementAPI;
import org.ow2.bonita.facade.QueryDefinitionAPI;
import org.ow2.bonita.facade.QueryRuntimeAPI;
import org.ow2.bonita.facade.RuntimeAPI;
import org.ow2.bonita.facade.def.element.BusinessArchive;
import org.ow2.bonita.facade.def.element.HookDefinition.Event;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.exception.BonitaInternalException;
import org.ow2.bonita.facade.exception.ProcessNotFoundException;
import org.ow2.bonita.facade.runtime.InstanceState;
import org.ow2.bonita.facade.runtime.ProcessInstance;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.light.LightProcessInstance;
import org.ow2.bonita.search.SearchQueryBuilder;
import org.ow2.bonita.search.index.UserIndex;
import org.ow2.bonita.util.AccessorUtil;
import org.ow2.bonita.util.BonitaConstants;
import org.ow2.bonita.util.BonitaException;
import org.ow2.bonita.util.BusinessArchiveFactory;
import org.ow2.bonita.util.DbTool;
import org.ow2.bonita.util.Misc;
import org.ow2.bonita.util.ProcessBuilder;
import org.ow2.bonita.util.SimpleCallbackHandler;

public class MultiTenancyTest extends TestCase {

  private static final String TENANT1_ID = "tenant1";
  private static final String TENANT2_ID = "tenant2";

  private static final String TENANT1_ENV_PATH = "src/main/resources/" +TENANT1_ID + "/bonita-server.xml";
  private static final String TENANT2_ENV_PATH = "src/main/resources/" + TENANT2_ID + "/bonita-server.xml";

  private static final String LOGIN = "admin";
  private static final String PASSWORD = "bpm";
  
  private static File envIndex;
  private static File jaas;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    
    jaas = File.createTempFile("jaas-multi", ".cfg");
    System.setProperty(BonitaConstants.JAAS_PROPERTY, jaas.getAbsolutePath());
    storeJaasFile(TENANT1_ID, TENANT2_ID);
    
    envIndex = File.createTempFile("tenants", ".properties");
    System.setProperty(BonitaConstants.ENVIRONMENT_PROPERTY, envIndex.getAbsolutePath());
    addTenant(TENANT1_ID, TENANT1_ENV_PATH);
    addTenant(TENANT2_ID, TENANT2_ENV_PATH);
    
    System.setProperty("ldr.tenant1", System.getProperty("java.io.tmpdir") + File.separator + TENANT1_ID);
    System.setProperty("ldr.tenant2", System.getProperty("java.io.tmpdir") + File.separator + TENANT2_ID);

    final String defaultLoggingFile = "src/main/resources/logging.properties";
    final String loggingFile = System.getProperty(BonitaConstants.LOGGING_PROPERTY, defaultLoggingFile);
    if (loggingFile.equals(defaultLoggingFile)) {
      System.setProperty(BonitaConstants.LOGGING_PROPERTY, defaultLoggingFile);
    }
  }

  @Override
  protected void tearDown() throws Exception {
    jaas.delete();
    envIndex.delete();
    super.tearDown();
  }
  
  private void storeJaasFile(final String... tenantIDs) throws Exception {
    String all = "";
    for (final String tenantID : tenantIDs) {
      final String auth = "BonitaAuth-" + tenantID + " {\n    org.ow2.bonita.identity.auth.BonitaIdentityLoginModule required domain=\"" + tenantID + "\";\n};";
      final String store = "BonitaStore-" + tenantID + " {\n    org.ow2.bonita.identity.auth.LocalStorageLoginModule required domain=\"" + tenantID + "\";\n};";
      all += auth + "\n\n" + store + "\n\n";
    }
    Misc.write(all, jaas);
    System.err.println("Jaas file written at: " + jaas);
  }
  
  private void storeProperties(final Properties properties) throws Exception {
    final FileOutputStream fos = new FileOutputStream(envIndex);
    properties.store(fos, null); 
    fos.close();
  }
  
  private Properties loadProperties() throws Exception {
    final FileInputStream fis = new FileInputStream(envIndex);
    final Properties properties = new Properties();
    properties.load(fis);
    fis.close();
    return properties;
  }
  
  private void addTenant(final String tenantId, final String envFilePath) throws Exception {
    final Properties properties = loadProperties();
    
    properties.put(tenantId, new File(envFilePath).getAbsolutePath());
    storeProperties(properties);
    
    System.err.println("Envindex generated at: " + envIndex);
  }
  
  public void testDynamicAddOfTenant() throws Exception {    
    MultiTenancyTest.dropDb(TENANT1_ID);
    MultiTenancyTest.dropDb(TENANT2_ID);

    final ManagementAPI managementAPI = AccessorUtil.getManagementAPI();
    final RuntimeAPI runtimeAPI = AccessorUtil.getRuntimeAPI();
    final QueryRuntimeAPI queryRuntimeAPI = AccessorUtil.getQueryRuntimeAPI();
    final QueryDefinitionAPI queryDefinitionAPI = AccessorUtil.getQueryDefinitionAPI();

    final BusinessArchive businessArchive = MultiTenancyTest.getBusinessArchive();
    final ProcessDefinitionUUID processUUID = businessArchive.getProcessUUID();

    final Map<String, Object> variables1 = new HashMap<String, Object>();
    variables1.put("newValue", TENANT1_ID);

    final Map<String, Object> variables2 = new HashMap<String, Object>();
    variables2.put("newValue", TENANT2_ID);

    LoginContext loginContext;

    
    final Properties properties = new Properties();
    storeProperties(properties);
    addTenant(TENANT1_ID, TENANT1_ENV_PATH);
    storeJaasFile(TENANT1_ID);
    
    loginContext = login(TENANT1_ID);
    managementAPI.deploy(businessArchive);
    assertNotNull(queryDefinitionAPI.getLightProcess(processUUID));
    final ProcessInstanceUUID instance1UUID = runtimeAPI.instantiateProcess(processUUID, variables1);
    final ProcessInstance instance1 = queryRuntimeAPI.getProcessInstance(instance1UUID);
    assertEquals(1, instance1.getNb());
    loginContext.logout();

    addTenant(TENANT2_ID, TENANT2_ENV_PATH);
    storeJaasFile(TENANT1_ID, TENANT2_ID);
    loginContext = login(TENANT2_ID);
    try {
      queryDefinitionAPI.getLightProcess(processUUID);
      fail("Process must not be found in tenant2");
    } catch (final ProcessNotFoundException e) {}

    managementAPI.deploy(businessArchive);
    assertNotNull(queryDefinitionAPI.getLightProcess(processUUID));
    final ProcessInstanceUUID instance2UUID = runtimeAPI.instantiateProcess(processUUID, variables2);
    final ProcessInstance instance2 = queryRuntimeAPI.getProcessInstance(instance2UUID);
    assertEquals(1, instance2.getNb());
    loginContext.logout();

    loginContext = login(TENANT1_ID);
    MultiTenancyTest.waitForInstanceEnd(5000, 200, instance1UUID);
    assertEquals(InstanceState.FINISHED, queryRuntimeAPI.getLightProcessInstance(instance1UUID).getInstanceState());
    assertEquals(TENANT1_ID, (String) queryRuntimeAPI.getProcessInstanceVariable(instance1UUID, "data"));
    loginContext.logout();

    loginContext = login(TENANT2_ID);
    MultiTenancyTest.waitForInstanceEnd(5000, 200, instance2UUID);
    assertEquals(InstanceState.FINISHED, queryRuntimeAPI.getLightProcessInstance(instance2UUID).getInstanceState());
    assertEquals(TENANT2_ID, (String) queryRuntimeAPI.getProcessInstanceVariable(instance2UUID, "data"));
    loginContext.logout();

    loginContext = login(TENANT1_ID);
    managementAPI.deleteAllProcesses();
    assertEquals(0, queryDefinitionAPI.getProcesses().size());
    loginContext.logout();

    loginContext = login(TENANT2_ID);
    assertEquals(1, queryDefinitionAPI.getLightProcesses().size());
    managementAPI.deleteAllProcesses();
    assertEquals(0, queryDefinitionAPI.getProcesses().size());
    loginContext.logout();
  }
  
  public void testMultiTenancyInManyThreads() throws Exception {
    final MultiTenantThread t1 = new MultiTenantThread(TENANT1_ID);
    final MultiTenantThread t2 = new MultiTenantThread(TENANT2_ID);

    MultiTenancyTest.dropDb(TENANT1_ID);
    MultiTenancyTest.dropDb(TENANT2_ID);

    t1.start();
    t2.start();

    t1.join(15000);
    t2.join(15000);

    assertTrue(t1.isFinished());
    assertTrue(t2.isFinished());

    assertNull(t1.getThrowable());
    assertNull(t2.getThrowable());

    assertFalse(t1.isAlive());
    assertFalse(t2.isAlive());
  }

  public void testMultiTenancyInSameThread() throws Exception {
    MultiTenancyTest.dropDb(TENANT1_ID);
    MultiTenancyTest.dropDb(TENANT2_ID);

    final ManagementAPI managementAPI = AccessorUtil.getManagementAPI();
    final RuntimeAPI runtimeAPI = AccessorUtil.getRuntimeAPI();
    final QueryRuntimeAPI queryRuntimeAPI = AccessorUtil.getQueryRuntimeAPI();
    final QueryDefinitionAPI queryDefinitionAPI = AccessorUtil.getQueryDefinitionAPI();

    final BusinessArchive businessArchive = MultiTenancyTest.getBusinessArchive();
    final ProcessDefinitionUUID processUUID = businessArchive.getProcessUUID();

    final Map<String, Object> variables1 = new HashMap<String, Object>();
    variables1.put("newValue", TENANT1_ID);

    final Map<String, Object> variables2 = new HashMap<String, Object>();
    variables2.put("newValue", TENANT2_ID);

    LoginContext loginContext;

    loginContext = login(TENANT1_ID);
    managementAPI.deploy(businessArchive);
    assertNotNull(queryDefinitionAPI.getLightProcess(processUUID));
    Set<LightProcessInstance> processInstances = queryRuntimeAPI.getLightProcessInstances(processUUID);
    assertTrue(processInstances.isEmpty());
    final ProcessInstanceUUID instance1UUID = runtimeAPI.instantiateProcess(processUUID, variables1);
    final ProcessInstance instance1 = queryRuntimeAPI.getProcessInstance(instance1UUID);
    assertEquals(1, instance1.getNb());
    loginContext.logout();

    loginContext = login(TENANT2_ID);
    try {
      queryDefinitionAPI.getLightProcess(processUUID);
      fail("Process must not be found in tenant2");
    } catch (final ProcessNotFoundException e) {}

    managementAPI.deploy(businessArchive);
    assertNotNull(queryDefinitionAPI.getLightProcess(processUUID));
    processInstances = queryRuntimeAPI.getLightProcessInstances(processUUID);
    assertTrue(processInstances.isEmpty());
    final ProcessInstanceUUID instance2UUID = runtimeAPI.instantiateProcess(processUUID, variables2);
    final ProcessInstance instance2 = queryRuntimeAPI.getProcessInstance(instance2UUID);
    assertEquals(1, instance2.getNb());
    loginContext.logout();

    loginContext = login(TENANT1_ID);
    MultiTenancyTest.waitForInstanceEnd(5000, 200, instance1UUID);
    assertEquals(InstanceState.FINISHED, queryRuntimeAPI.getLightProcessInstance(instance1UUID).getInstanceState());
    assertEquals(TENANT1_ID, (String) queryRuntimeAPI.getProcessInstanceVariable(instance1UUID, "data"));
    loginContext.logout();

    loginContext = login(TENANT2_ID);
    MultiTenancyTest.waitForInstanceEnd(5000, 200, instance2UUID);
    assertEquals(InstanceState.FINISHED, queryRuntimeAPI.getLightProcessInstance(instance2UUID).getInstanceState());
    assertEquals(TENANT2_ID, (String) queryRuntimeAPI.getProcessInstanceVariable(instance2UUID, "data"));
    loginContext.logout();

    loginContext = login(TENANT1_ID);
    managementAPI.deleteAllProcesses();
    assertEquals(0, queryDefinitionAPI.getProcesses().size());
    loginContext.logout();

    loginContext = login(TENANT2_ID);
    assertEquals(1, queryDefinitionAPI.getLightProcesses().size());
    managementAPI.deleteAllProcesses();
    assertEquals(0, queryDefinitionAPI.getProcesses().size());
    loginContext.logout();
  }
  
  public void testEventCreateProcessInstance() throws Exception {
    MultiTenancyTest.dropDb(TENANT1_ID);
    MultiTenancyTest.dropDb(TENANT2_ID);

    final ManagementAPI managementAPI = AccessorUtil.getManagementAPI();
    final RuntimeAPI runtimeAPI = AccessorUtil.getRuntimeAPI();
    final QueryRuntimeAPI queryRuntimeAPI = AccessorUtil.getQueryRuntimeAPI();

    ProcessDefinition creator = ProcessBuilder.createProcess("creatorMulti", "1.0")
    .addHuman(LOGIN)
    .addSendEventTask("send")
    .addOutgoingEvent("createProcess", "createdMulti", "receive", null)
    .addHumanTask("t", LOGIN)
    .addTransition("send", "t")
    .done();

    ProcessDefinition created = ProcessBuilder.createProcess("createdMulti", "1.0")
    .addReceiveEventTask("receive", "createProcess")
    .done();

    final ProcessDefinitionUUID creatorUUID = creator.getUUID();
    final ProcessDefinitionUUID createdUUID = created.getUUID();

    LoginContext loginContext = null;

    loginContext = login(TENANT1_ID);
    creator = managementAPI.deploy(BusinessArchiveFactory.getBusinessArchive(creator));
    created = managementAPI.deploy(BusinessArchiveFactory.getBusinessArchive(created));
    final ProcessInstanceUUID creatorInstanceUUID1 = runtimeAPI.instantiateProcess(creatorUUID);
    assertEquals("Creator process instance is finished", InstanceState.STARTED, queryRuntimeAPI.getProcessInstance(creatorInstanceUUID1).getInstanceState());
    waitForCreation(15000, 1, createdUUID);
    loginContext.logout();

    loginContext = login(TENANT2_ID);
    creator = managementAPI.deploy(BusinessArchiveFactory.getBusinessArchive(creator));
    created = managementAPI.deploy(BusinessArchiveFactory.getBusinessArchive(created));
    final ProcessInstanceUUID creatorInstanceUUID2 = runtimeAPI.instantiateProcess(creatorUUID);
    assertEquals("Creator process instance is finished", InstanceState.STARTED, queryRuntimeAPI.getProcessInstance(creatorInstanceUUID2).getInstanceState());
    waitForCreation(15000, 1, createdUUID);
    loginContext.logout();

    loginContext = login(TENANT1_ID);
    managementAPI.deleteProcess(creatorUUID);
    managementAPI.deleteProcess(createdUUID);
    loginContext.logout();

    loginContext = login(TENANT2_ID);
    managementAPI.deleteProcess(creatorUUID);
    managementAPI.deleteProcess(createdUUID);
    loginContext.logout();
  }
  
  public void testProcessClassLoader() throws Exception {
    //this test checks that if 2 processes with the same UUID are deployed in 
    //2 differents tenants, then classes are well found by both of them
    //To check that, these 2 processes must have different dependencies
    
    MultiTenancyTest.dropDb(TENANT1_ID);
    MultiTenancyTest.dropDb(TENANT2_ID);

    final ManagementAPI managementAPI = AccessorUtil.getManagementAPI();
    final RuntimeAPI runtimeAPI = AccessorUtil.getRuntimeAPI();

    final String className1 = "CustomClassTenant1";
    final Map<String, byte[]> resources1 = new HashMap<String, byte[]>();
    resources1.put("tenant1.jar", Misc.getAllContentFrom(getClass().getResource("tenant1.jar")));
    final Map<String, Object> variables1 = new HashMap<String, Object>();
    variables1.put("className", className1);
    
    final String className2 = "CustomClassTenant2";
    final Map<String, byte[]> resources2 = new HashMap<String, byte[]>();
    resources2.put("tenant2.jar", Misc.getAllContentFrom(getClass().getResource("tenant2.jar")));
    final Map<String, Object> variables2 = new HashMap<String, Object>();
    variables2.put("className", className2);
    
    final ProcessDefinition process = ProcessBuilder.createProcess("classloader", "1.0")
    .addStringData("className", "initial")
    .addSystemTask("sys")
    .addConnector(Event.automaticOnEnter, CheckClassConnector.class.getName(), true)
    .addInputParameter("className", "${className}")
    .done();
    
    final ProcessDefinitionUUID processUUID = process.getUUID();

    
    LoginContext loginContext = null;

    loginContext = login(TENANT1_ID);
    managementAPI.deploy(BusinessArchiveFactory.getBusinessArchive(process, resources1));
    runtimeAPI.instantiateProcess(processUUID, variables1);
    loginContext.logout();

    loginContext = login(TENANT2_ID);
    managementAPI.deploy(BusinessArchiveFactory.getBusinessArchive(process, resources2));
    runtimeAPI.instantiateProcess(processUUID, variables2);
    loginContext.logout();

    loginContext = login(TENANT1_ID);
    managementAPI.deleteProcess(processUUID);
    loginContext.logout();

    loginContext = login(TENANT2_ID);
    managementAPI.deleteProcess(processUUID);
    loginContext.logout();
  }
  
  public void testCommonClassLoader() throws Exception {
    MultiTenancyTest.dropDb(TENANT1_ID);
    MultiTenancyTest.dropDb(TENANT2_ID);

    final ManagementAPI managementAPI = AccessorUtil.getManagementAPI();
    final RuntimeAPI runtimeAPI = AccessorUtil.getRuntimeAPI();

    final String className1 = "CustomClassTenant1";
    final String className2 = "CustomClassTenant2";
    
    final Map<String, Object> variables1 = new HashMap<String, Object>();
    variables1.put("foundClassName", className1);
    variables1.put("notFoundClassName", className2);
    
    final Map<String, Object> variables2 = new HashMap<String, Object>();
    variables2.put("foundClassName", className2);
    variables2.put("notFoundClassName", className1);
    
    final ProcessDefinition process = ProcessBuilder.createProcess("classloader", "1.0")
    .addStringData("foundClassName", "initial")
    .addStringData("notFoundClassName", "initial")
    .addSystemTask("sys")
    .addConnector(Event.automaticOnEnter, CheckClassesConnector.class.getName(), true)
    .addInputParameter("foundClassName", "${foundClassName}")
    .addInputParameter("notFoundClassName", "${notFoundClassName}")
    .done();
    
    final ProcessDefinitionUUID processUUID = process.getUUID();

    
    LoginContext loginContext = null;

    loginContext = login(TENANT1_ID);
    try {managementAPI.removeJar("tenant.jar");} catch (final Exception e) {}
    managementAPI.deployJar("tenant.jar", Misc.getAllContentFrom(getClass().getResource("tenant1.jar")));
    loginContext.logout();

    loginContext = login(TENANT2_ID);
    try {managementAPI.removeJar("tenant.jar");} catch (final Exception e) {}
    managementAPI.deployJar("tenant.jar", Misc.getAllContentFrom(getClass().getResource("tenant2.jar")));
    loginContext.logout();
    
    loginContext = login(TENANT1_ID);
    managementAPI.deploy(BusinessArchiveFactory.getBusinessArchive(process));
    runtimeAPI.instantiateProcess(processUUID, variables1);
    loginContext.logout();

    loginContext = login(TENANT2_ID);
    managementAPI.deploy(BusinessArchiveFactory.getBusinessArchive(process));
    runtimeAPI.instantiateProcess(processUUID, variables2);
    loginContext.logout();

    loginContext = login(TENANT1_ID);
    managementAPI.removeJar("tenant.jar");
    managementAPI.deleteProcess(processUUID);
    loginContext.logout();

    loginContext = login(TENANT2_ID);
    managementAPI.removeJar("tenant.jar");
    managementAPI.deleteProcess(processUUID);
    loginContext.logout();
  }

  public static Map<String, byte[]> getJarFile(final String jarFileName, final String className, final String base) throws Exception {
    final InputStream in = MultiTenancyTest.class.getResourceAsStream(base + ".bytecode");
    final byte[] resource = Misc.getAllContentFrom(in);
    final Map<String, byte[]> resources = new HashMap<String, byte[]>();
    resources.put(className, resource);
    final byte[] jar = Misc.generateJar(resources);
    resources.clear();
    resources.put(jarFileName, jar);
    return resources;
  }
  public static LoginContext login(final String tenantID) throws LoginException {
    javax.security.auth.login.Configuration.getConfiguration().refresh();
    LoginContext loginContext = new LoginContext("BonitaAuth-" + tenantID, new SimpleCallbackHandler(LOGIN, PASSWORD));
    loginContext.login();
    loginContext.logout();
    loginContext = new LoginContext("BonitaStore-" + tenantID, new SimpleCallbackHandler(LOGIN, PASSWORD));
    loginContext.login();
    return loginContext;
  }

  public static void dropDb(final String tenantId) throws Exception {
    final Map<String, String> hibernateConfigs = new HashMap<String, String>();
    hibernateConfigs.put(EnvConstants.HB_CONFIG_CORE, EnvConstants.HB_SESSION_FACTORY_CORE);
    hibernateConfigs.put(EnvConstants.HB_CONFIG_HISTORY, EnvConstants.HB_SESSION_FACTORY_HISTORY);
    for (final Map.Entry<String, String> e : hibernateConfigs.entrySet()) {
      final Configuration config = (Configuration) GlobalEnvironmentFactory.getEnvironmentFactory(tenantId).get(e.getKey());
      if (config != null) {
        DbTool.recreateDb(tenantId, e.getKey());
        if ("true".equals(DbTool.getDbUseQueryCache(config))) {
          DbTool.cleanCache(tenantId, e.getValue());
        }
      }
    }
  }

  public static BusinessArchive getBusinessArchive() throws Exception {
    final ProcessDefinition process = ProcessBuilder.createProcess("testMultiTenancy", "0.1")
    .addStringData("data", "initial")
    .addStringData("newValue")
    .addTimerTask("timer", "${new Date(System.currentTimeMillis() + 500)}")
    .addConnector(Event.onTimer, SetVarConnector.class.getName(), true)
    .addInputParameter("variableName", "data")
    .addInputParameter("value", "${newValue}")
    .addSystemTask("end")
    .addTransition("timer", "end")
    .done();
    final BusinessArchive businessArchive = BusinessArchiveFactory.getBusinessArchive(process, SetVarConnector.class);
    return businessArchive;
  }

  public static void waitForInstanceEnd(final long maxWait, final long sleepTime, final ProcessInstanceUUID instanceUUID) throws BonitaException {
    final long maxDate = System.currentTimeMillis() + maxWait;
    LightProcessInstance processInstance = null;
    LightProcessInstance temp = null;
    while (System.currentTimeMillis() < maxDate && processInstance == null) {
      try {
        temp = AccessorUtil.getQueryRuntimeAPI().getProcessInstance(instanceUUID);
        if (InstanceState.FINISHED == temp.getInstanceState()) {
          processInstance = temp;
        }
      } catch (final Exception infe) {
        //maybe instance move from journal to history due to handler
      }
      try {
        Thread.sleep(sleepTime);
      } catch (final Exception e) {
        // still waiting
      }
    }
    assertNotNull(processInstance);
  }

  public static void waitForCreation(final long maxWait, final int expected, final ProcessDefinitionUUID createdUUID) throws Exception {
    final long before = System.currentTimeMillis();
    boolean wait = true;
    do {
      Thread.sleep(500);
      final int instanceNb = AccessorUtil.getQueryRuntimeAPI().getLightProcessInstances(createdUUID).size(); 
      wait = instanceNb != 1 && (System.currentTimeMillis() - before) < maxWait;  
    } while (wait);

    //wait to ensure that there is no more created instances than needed
    Thread.sleep(4000);

    final int size = AccessorUtil.getQueryRuntimeAPI().getLightProcessInstances(createdUUID).size();
    assertEquals("Process was not launched " + expected + " time(s). It was launched: " + size + " times", expected, size);
  }
  
  public void testSearchError() throws Exception {
    MultiTenancyTest.dropDb(TENANT1_ID);
    MultiTenancyTest.dropDb(TENANT2_ID);

    final QueryRuntimeAPI queryRuntimeAPI = AccessorUtil.getQueryRuntimeAPI();

    LoginContext loginContext;
    final Properties properties = new Properties();
    storeProperties(properties);
    addTenant(TENANT1_ID, TENANT1_ENV_PATH);
    storeJaasFile(TENANT1_ID);

    loginContext = login(TENANT1_ID);
    
    final SearchQueryBuilder query = new SearchQueryBuilder(new UserIndex());
    query.criterion(UserIndex.LAST_NAME).equalsTo("Doe")
    .or().criterion(UserIndex.NAME).equalsTo("admin");

    try {
      queryRuntimeAPI.search(query);
    } catch (final BonitaInternalException e) {
      assertNotNull(e);
      assertNotNull(e.getMessage());
      e.printStackTrace();
      assertTrue(e.getMessage().contains("None of the specified entity types or any of their subclasses are indexed."));
    }

    loginContext.logout();
  }

}
