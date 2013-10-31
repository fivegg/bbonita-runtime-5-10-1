package org.ow2.bonita.facade;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

import javax.security.auth.login.LoginException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.bonitasoft.connectors.bonita.SetVarConnector;
import org.bonitasoft.connectors.bonita.joincheckers.FixedNumberJoinChecker;
import org.custommonkey.xmlunit.XMLUnit;
import org.ow2.bonita.APITestCase;
import org.ow2.bonita.activity.instantiation.instantiator.TestOneJoinActivityInstantiator;
import org.ow2.bonita.activity.multipleinstances.instantiator.NoContextMulitpleActivitiesInstantiator;
import org.ow2.bonita.cmd.RemoveProcessClassloaderCommand;
import org.ow2.bonita.definition.InstanceInitiator;
import org.ow2.bonita.facade.def.element.BusinessArchive;
import org.ow2.bonita.facade.def.element.HookDefinition.Event;
import org.ow2.bonita.facade.def.majorElement.ActivityDefinition;
import org.ow2.bonita.facade.def.majorElement.ActivityDefinition.JoinType;
import org.ow2.bonita.facade.def.majorElement.ActivityDefinition.SplitType;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.exception.ActivityNotFoundException;
import org.ow2.bonita.facade.exception.IllegalTaskStateException;
import org.ow2.bonita.facade.exception.InstanceNotFoundException;
import org.ow2.bonita.facade.exception.ProcessNotFoundException;
import org.ow2.bonita.facade.exception.TaskNotFoundException;
import org.ow2.bonita.facade.exception.UncancellableInstanceException;
import org.ow2.bonita.facade.exception.UndeletableInstanceException;
import org.ow2.bonita.facade.exception.VariableNotFoundException;
import org.ow2.bonita.facade.identity.User;
import org.ow2.bonita.facade.runtime.ActivityInstance;
import org.ow2.bonita.facade.runtime.ActivityState;
import org.ow2.bonita.facade.runtime.InstanceState;
import org.ow2.bonita.facade.runtime.ProcessInstance;
import org.ow2.bonita.facade.runtime.StateUpdate;
import org.ow2.bonita.facade.runtime.TaskInstance;
import org.ow2.bonita.facade.uuid.ActivityInstanceUUID;
import org.ow2.bonita.facade.uuid.IdFactory;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.light.LightActivityInstance;
import org.ow2.bonita.light.LightProcessInstance;
import org.ow2.bonita.light.LightTaskInstance;
import org.ow2.bonita.util.BonitaConstants;
import org.ow2.bonita.util.BonitaException;
import org.ow2.bonita.util.BusinessArchiveFactory;
import org.ow2.bonita.util.DateUtil;
import org.ow2.bonita.util.GroovyException;
import org.ow2.bonita.util.Misc;
import org.ow2.bonita.util.ProcessBuilder;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class RuntimeAPITest extends APITestCase {

  public void testDefaultTransition1() throws Exception {
    ProcessDefinition process = ProcessBuilder.createProcess("p", "1.0")
    .addSystemTask("t1")
    .addSystemTask("t2")
    .addTransition("t1", "t2")
    .setDefault()
    .done();

    process = getManagementAPI().deploy(getBusinessArchive(process));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    assertEquals(InstanceState.FINISHED, getQueryRuntimeAPI().getLightProcessInstance(instanceUUID).getInstanceState());
    checkExecutedOnce(instanceUUID, "t1", "t2");

    getManagementAPI().deleteProcess(processUUID);
  }

  public void testDefaultTransition2() throws Exception {
    ProcessDefinition process = ProcessBuilder.createProcess("p", "1.0")
    .addSystemTask("initial")
    .addSystemTask("a1")
    .addSystemTask("a2")
    .addSystemTask("end")
    .addJoinType(JoinType.XOR)
    .addTransition("initial", "a1")
    .addCondition("true")
    .addTransition("initial", "a2")
    .setDefault()
    .addTransition("a1", "end")
    .addTransition("a2", "end")
    .done();

    process = getManagementAPI().deploy(getBusinessArchive(process));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    assertEquals(InstanceState.FINISHED, getQueryRuntimeAPI().getLightProcessInstance(instanceUUID).getInstanceState());
    checkExecutedOnce(instanceUUID, "initial", "a1", "end");
    checkNotExecuted(instanceUUID, "a2");

    getManagementAPI().deleteProcess(processUUID);
  }

  public void testDefaultTransition3() throws Exception {
    ProcessDefinition process = ProcessBuilder.createProcess("p", "1.0")
    .addSystemTask("initial")
    .addSystemTask("a1")
    .addSystemTask("a2")
    .addSystemTask("end")
    .addJoinType(JoinType.XOR)
    .addTransition("initial", "a1")
    .addCondition("false")
    .addTransition("initial", "a2")
    .setDefault()
    .addTransition("a1", "end")
    .addTransition("a2", "end")
    .done();

    process = getManagementAPI().deploy(getBusinessArchive(process));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    assertEquals(InstanceState.FINISHED, getQueryRuntimeAPI().getLightProcessInstance(instanceUUID).getInstanceState());
    checkExecutedOnce(instanceUUID, "initial", "a2", "end");
    checkNotExecuted(instanceUUID, "a1");

    getManagementAPI().deleteProcess(processUUID);
  }

  public void testANDSplit() throws Exception {
    ProcessDefinition process = ProcessBuilder.createProcess("xorSplit", "1.0")
    .addSystemTask("initial")
    .addDecisionNode("xor")
    .addSplitType(SplitType.AND)
    .addSystemTask("a1")
    .addSystemTask("a2")
    .addDecisionNode("join")
    .addJoinType(JoinType.AND)
    .addSystemTask("end")
    .addJoinType(JoinType.AND)
    .addTransition("initial", "xor")
    .addTransition("xor", "a1")
    .addTransition("xor", "a2")
    .addTransition("a1", "join")
    .addTransition("a2", "join")
    .addTransition("join", "end")
    .done();

    process = getManagementAPI().deploy(getBusinessArchive(process));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    assertEquals(InstanceState.FINISHED, getQueryRuntimeAPI().getLightProcessInstance(instanceUUID).getInstanceState());
    checkExecutedOnce(instanceUUID, "initial", "xor", "a1", "a2", "end");

    getManagementAPI().deleteProcess(processUUID);
  }


  public void testArchiveFinishedInstanceWithReadyTasks() throws Exception {
    ProcessDefinition process = ProcessBuilder.createProcess("p", "1.0")
    .addHuman(getLogin())
    .addHumanTask("t1", getLogin())
    .addHumanTask("t2", getLogin())
    .addTerminateEndEvent("end")
    .addTransition("t1", "end")
    .addTransition("t2", "end")
    .done();

    process = getManagementAPI().deploy(getBusinessArchive(process));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    Collection<TaskInstance> tasks = getQueryRuntimeAPI().getTaskList(instanceUUID, ActivityState.READY);
    assertEquals(2, tasks.size());

    getRuntimeAPI().executeTask(tasks.iterator().next().getUUID(), true);

    //instance is finished
    tasks = getQueryRuntimeAPI().getTaskList(instanceUUID, ActivityState.READY);
    assertEquals(0, tasks.size());

    getManagementAPI().deleteProcess(processUUID);
  }

  private void sleep() throws InterruptedException {
    if (!Misc.isOnWindows()) {
      Thread.sleep(10);
    } else {
      Thread.sleep(200);
    }
  }

  public void testActivityInstanceLastUpdate() throws Exception {
    //getCommandAPI().execute(new MyTestCommand());

    ProcessDefinition process = ProcessBuilder.createProcess("update", "1.0")
    .addHuman(getLogin())
    .addSystemTask("start")
    .addHumanTask("human", getLogin())
    .addStringData("data", "initial")
    .addSystemTask("end")
    .addTransition("start", "human")
    .addTransition("human", "end")
    .done();

    process = getManagementAPI().deploy(getBusinessArchive(process));

    final ProcessDefinitionUUID processUUID = process.getUUID();

    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);
    LightTaskInstance task;

    sleep();
    task = getQueryRuntimeAPI().getLightTaskList(instanceUUID, ActivityState.READY).iterator().next();
    long time = System.currentTimeMillis();
    assertTrue("Expected " + time + " > " + task.getLastUpdateDate().getTime(), time > task.getLastUpdateDate().getTime());

    sleep();
    getRuntimeAPI().setActivityInstanceVariable(task.getUUID(), "data", "new");
    task = getQueryRuntimeAPI().getLightTaskList(ActivityState.READY).iterator().next();
    assertTrue("Expected " + time + " < " + task.getLastUpdateDate().getTime(), time < task.getLastUpdateDate().getTime());

    sleep();
    time = System.currentTimeMillis();
    assertTrue("Expected " + time + " > " + task.getLastUpdateDate().getTime(), time > task.getLastUpdateDate().getTime());
    sleep();
    getRuntimeAPI().suspendTask(task.getUUID(), true);
    task = getQueryRuntimeAPI().getLightTaskList(ActivityState.SUSPENDED).iterator().next();
    assertTrue("Expected " + time + " < " + task.getLastUpdateDate().getTime(), time < task.getLastUpdateDate().getTime());

    sleep();
    time = System.currentTimeMillis();
    assertTrue("Expected " + time + " > " + task.getLastUpdateDate().getTime(), time > task.getLastUpdateDate().getTime());
    sleep();
    getRuntimeAPI().resumeTask(task.getUUID(), true);
    task = getQueryRuntimeAPI().getLightTaskList(ActivityState.READY).iterator().next();
    assertTrue("Expected " + time + " < " + task.getLastUpdateDate().getTime(), time < task.getLastUpdateDate().getTime());

    getManagementAPI().deleteAllProcesses();
  }

  public void testGetTaskListWithRoleMapper() throws BonitaException {
    ProcessDefinition p = ProcessBuilder.createProcess("myProcess", "1.0")
    .addGroup("manager")
    .addGroupResolver(AdminsRoleMapper.class.getName())
    .addHumanTask("a", "manager")
    .done();

    ProcessDefinition processDefinition = getManagementAPI().deploy(getBusinessArchive(p, null, AdminsRoleMapper.class));
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processDefinition.getUUID());

    Set<TaskInstance> tasks = getQueryRuntimeAPI().getTasks(instanceUUID);

    assertNotNull(tasks);
    assertEquals(1, tasks.size());
    TaskInstance taskA = tasks.iterator().next();
    assertEquals("a", taskA.getActivityName());
    Set<String> candidates = taskA.getTaskCandidates();

    assertNotNull(candidates);
    assertEquals(2, candidates.size());

    assertTrue(candidates.contains("john"));
    assertTrue(candidates.contains("admin"));

    getManagementAPI().deleteProcess(processDefinition.getUUID());
  }

  public void testExecuteTask() throws BonitaException {
    ProcessDefinition p = ProcessBuilder.createProcess("myProcess", "1.0")
    .addHuman(getLogin())
    .addHumanTask("a", getLogin())
    .addHumanTask("b", getLogin())
    .addTransition("a_b", "a", "b")
    .done();

    ProcessDefinition processDefinition = getManagementAPI().deploy(getBusinessArchive(p, null, AdminsRoleMapper.class));
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processDefinition.getUUID());

    Collection<TaskInstance> tasks = getQueryRuntimeAPI().getTasks(instanceUUID);

    assertNotNull(tasks);
    assertEquals(1, tasks.size());
    TaskInstance taskA = tasks.iterator().next();
    assertEquals("a", taskA.getActivityName());

    assertEquals(ActivityState.READY, taskA.getState());

    getRuntimeAPI().executeTask(taskA.getUUID(), true);

    tasks = getQueryRuntimeAPI().getTaskList(instanceUUID, ActivityState.READY);

    assertNotNull(tasks);
    assertEquals(1, tasks.size());
    TaskInstance taskB = tasks.iterator().next();
    assertEquals("b", taskB.getActivityName());
    assertEquals(ActivityState.READY, taskB.getState());

    ActivityInstance a = getQueryRuntimeAPI().getActivityInstance(taskA.getUUID());
    List<StateUpdate> stateUpdates = a.getStateUpdates();
    //READY, EXECUTING, FINISHED
    assertEquals(3, stateUpdates.size());

    getManagementAPI().deleteProcess(processDefinition.getUUID());
  }

  //  
  //     test instantiateProcess(String processDefinitionUUID) from RuntimeAPI
  //  
  //    Test:
  //     -instantiation + start execution
  //     -returned instance has STARTED state
  //    test :
  //       throws ProcessNotFoundException if the process has not been found
  //       (ex. : ""  random processDefinitionUUID) ,
  //       throws BonitaInternalException if an exception occurs (ex. if processDefinitionUUID == null)
  //  
  public void testInstantiateProcess() throws BonitaException {
    final URL xpdlUrl = getClass().getResource("usertest1_1.0.xpdl");
    final ProcessDefinition process = getManagementAPI().deploy(getBusinessArchiveFromXpdl(xpdlUrl));
    final ProcessDefinitionUUID processUUID = process.getUUID();
    try {
      getRuntimeAPI().instantiateProcess(null);
      fail("Check null parameter");
    } catch (final IllegalArgumentException be) {
      // OK
    } catch (NullPointerException e){
      //Ok REST
      StackTraceElement firstElement = e.getStackTrace()[0];
      assertTrue(firstElement.getClassName().equals("org.jboss.resteasy.client.ClientRequest"));
      assertTrue(firstElement.getMethodName().equals("toString"));
    }
    try {
      getRuntimeAPI().instantiateProcess(IdFactory.getNewProcessUUID());
      fail("Check non existent process");
    } catch (final ProcessNotFoundException be) {
      //OK
    } 
    try {
      final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);
      // instantiateProcess calls start so the instance is started.
      assertEquals(InstanceState.STARTED, getQueryRuntimeAPI().getProcessInstance(instanceUUID).getInstanceState());

      getRuntimeAPI().deleteProcessInstance(instanceUUID);
      getManagementAPI().disable(processUUID);
      getManagementAPI().deleteProcess(processUUID);
    } catch (final BonitaException be) {
      if (be.getCause() instanceof ProcessNotFoundException) {
        throw new RuntimeException("Missing resource!", be);
      }
      fail("Exception thrown: " + Misc.getStackTraceFrom(be));
    }
  }

  //Test instantiateProcess(String processDefinitionUUID, Map<String Object>variables) from RuntimeAPI
  //    check the map of variables has been added to the created instance.
  public void testInstantiateProcessWithMapVariables() throws BonitaException {
    final URL xpdlUrl = getClass().getResource("usertest1_1.0.xpdl");
    final ProcessDefinition process = getManagementAPI().deploy(getBusinessArchiveFromXpdl(xpdlUrl));
    final ProcessDefinitionUUID processUUID = process.getUUID();
    try {
      final Map<String, Object> expectedVarMap = new HashMap<String, Object>();
      expectedVarMap.put("str1", "toto");
      final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID, expectedVarMap, null);
      // instantiateProcess calls start so the instance is started.
      assertEquals(InstanceState.STARTED, getQueryRuntimeAPI().getProcessInstance(instanceUUID).getInstanceState());

      final Map<String, Object> currentVarMap = getQueryRuntimeAPI().getProcessInstanceVariables(instanceUUID);
      //check the instance get the expected variable map.
      assertEquals(expectedVarMap, currentVarMap);

      getRuntimeAPI().deleteProcessInstance(instanceUUID);
      getManagementAPI().disable(processUUID);
      getManagementAPI().deleteProcess(processUUID);
    } catch (final BonitaException be) {
      if (be.getCause() instanceof ProcessNotFoundException) {
        throw new RuntimeException("Missing resource!", be);
      }
      fail("Exception thrown: " + Misc.getStackTraceFrom(be));
    }
  }

  //test deleteInstance & deleteInstances
  public void testDeleteInstance() throws BonitaException {
    final URL xpdlUrl = getClass().getResource("usertest1_1.0.xpdl");
    final ProcessDefinition process = getManagementAPI().deploy(getBusinessArchiveFromXpdl(xpdlUrl));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    try {
      //test deleteInstance()
      final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);
      assertEquals(InstanceState.STARTED, getQueryRuntimeAPI().getProcessInstance(instanceUUID).getInstanceState());
      Collection<TaskInstance> tasks = getQueryRuntimeAPI().getTaskList(instanceUUID, ActivityState.READY);
      assertTrue("Check task is not empty", tasks.size() == 1);
      getRuntimeAPI().deleteProcessInstance(instanceUUID);
      try {
        //getInstance call the journal !!
        getQueryRuntimeAPI().getProcessInstance(instanceUUID);
        fail("Check fail getting instance into the journal with getInstance()");
      } catch (final InstanceNotFoundException ie) {
        assertTrue("Check exception has well been raised !", ie.getInstanceUUID().equals(instanceUUID));
      } 

      //test deleteInstances()
      final ProcessInstanceUUID instanceUUID1 = getRuntimeAPI().instantiateProcess(processUUID);
      final ProcessInstanceUUID instanceUUID2 = getRuntimeAPI().instantiateProcess(processUUID);
      assertEquals(InstanceState.STARTED, getQueryRuntimeAPI().getProcessInstance(instanceUUID1).getInstanceState());
      assertEquals(InstanceState.STARTED, getQueryRuntimeAPI().getProcessInstance(instanceUUID2).getInstanceState());

      tasks = getQueryRuntimeAPI().getTaskList(ActivityState.READY);
      assertEquals("Check task is not empty", 2, tasks.size());

      getRuntimeAPI().deleteAllProcessInstances(processUUID);


      final ProcessInstanceUUID instanceUUID3 = getRuntimeAPI().instantiateProcess(processUUID);
      final ProcessInstanceUUID instanceUUID4 = getRuntimeAPI().instantiateProcess(processUUID);

      Collection<ProcessInstanceUUID> instanceUUIDs = new HashSet<ProcessInstanceUUID>();
      instanceUUIDs.add(instanceUUID3);
      instanceUUIDs.add(instanceUUID4);
      getRuntimeAPI().deleteProcessInstances(instanceUUIDs);


      final Set<ProcessInstance> processInstances = getQueryRuntimeAPI().getProcessInstances(processUUID);
      assertTrue("No more process instance into the journal", processInstances.size() == 0);
      getManagementAPI().disable(processUUID);
      getManagementAPI().deleteProcess(processUUID);
    } catch (final BonitaException be) {
      if (be.getCause() instanceof ProcessNotFoundException) {
        throw new RuntimeException("Missing resource!", be);
      }
      fail("Exception thrown:" + Misc.getStackTraceFrom(be));
    }
  }

  /*
   *  Test startTask(String taskUUID) from the runtimeAPI.
   *
   * test initial state need to be READY.
   * change state to EXECUTING.
   * tests following exceptions are raised:
   *  @throws TaskNotFoundException if the task has not been found.
   *  @throws IllegalTaskStateException if the state of the task has not READY state.
   *  @throws BonitaInternalException if an exception occurs (ex. : if taskUUID == null)
   */
  public void testStartTask() throws BonitaException {
    ProcessInstanceUUID instanceUUID = null;
    final URL xpdlUrl = getClass().getResource("usertest1_1.0.xpdl");
    final ProcessDefinition process = getManagementAPI().deploy(getBusinessArchiveFromXpdl(xpdlUrl));
    final ProcessDefinitionUUID processUUID = process.getUUID();
    try {
      instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);
    } catch (final BonitaException e) {
      fail("Ouch! How can it be?! Should never reach this statement!, exception: " + Misc.getStackTraceFrom(e));
    }
    try {
      getRuntimeAPI().startTask(null, true);
      fail("Check null argument");
    } catch (final IllegalArgumentException be) {
      // ok
    }catch (NullPointerException e){
      //Ok REST
      StackTraceElement firstElement = e.getStackTrace()[0];
      assertTrue(firstElement.getClassName().equals("org.jboss.resteasy.client.ClientRequest"));
      assertTrue(firstElement.getMethodName().equals("toString"));
    }  
    try {
      getRuntimeAPI().startTask(IdFactory.getNewTaskUUID(), true);
      fail("Check non existent task");
    } catch (final TaskNotFoundException be) {
      //OK
    } 
    checkStopped(instanceUUID, new String[]{});
    final Collection<TaskInstance> activitiesToDo =
      getQueryRuntimeAPI().getTaskList(instanceUUID, ActivityState.READY);
    assertEquals(1, activitiesToDo.size());
    final TaskInstance taskActivity = activitiesToDo.iterator().next();
    assertEquals(ActivityState.READY, getQueryRuntimeAPI().getTask(taskActivity.getUUID()).getState());

    try {
      getRuntimeAPI().startTask(taskActivity.getUUID(), true);
    } catch (final BonitaException e) {
      fail("Ouch! How can it be?! Should never reach this statement!, exception: " + Misc.getStackTraceFrom(e));
    }
    assertEquals(ActivityState.EXECUTING, getQueryRuntimeAPI().getTask(taskActivity.getUUID()).getState());

    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  /*
   * Test terminateTask(String taskUUID) from the runtimeAPI.
   *
   * Test initial state required is : EXECUTING.
   * Test if finish is successful state become FINISHED.
   * Test
   *    - @throws TaskNotFoundException if the task has not been found.
   *    - @throws IllegalTaskStateException if the state of the task has not EXECUTING state.
   *    - @throws BonitaInternalException if an exception occurs (for ex. with illegal taskUUID parameter).
   *
   */
  public void testFinishTask() throws BonitaException {
    ProcessInstanceUUID instanceUUID = null;
    final URL xpdlUrl = getClass().getResource("usertest1_1.0.xpdl");
    final ProcessDefinition process = getManagementAPI().deploy(getBusinessArchiveFromXpdl(xpdlUrl));
    final ProcessDefinitionUUID processUUID = process.getUUID();
    try {
      instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);
    } catch (final BonitaException e) {
      fail("Ouch! How can it be?! Should never reach this statement!");
    }
    try {
      getRuntimeAPI().finishTask(null, true);
      fail("Check null argument");
    } catch (final IllegalArgumentException be) {
      // ok
    } catch (NullPointerException e){
      //Ok REST
      StackTraceElement firstElement = e.getStackTrace()[0];
      assertTrue(firstElement.getClassName().equals("org.jboss.resteasy.client.ClientRequest"));
      assertTrue(firstElement.getMethodName().equals("toString"));
    }    
    try {
      getRuntimeAPI().finishTask(IdFactory.getNewTaskUUID(), true);
      fail("Check non existent instance");
    } catch (final TaskNotFoundException be) {
      //OK
    } 
    checkStopped(instanceUUID, new String[]{});
    Collection<TaskInstance> activitiesToDo =
      getQueryRuntimeAPI().getTaskList(instanceUUID, ActivityState.READY);
    assertEquals(1, activitiesToDo.size());
    TaskInstance taskActivity = activitiesToDo.iterator().next();
    try {
      getRuntimeAPI().startTask(taskActivity.getUUID(), true);
    } catch (final BonitaException e) {
      fail("Ouch! How can it be?! Should never reach this statement!, exception: " + Misc.getStackTraceFrom(e));
    }
    activitiesToDo = getQueryRuntimeAPI().getTaskList(instanceUUID, ActivityState.EXECUTING);
    assertEquals(1, activitiesToDo.size());
    taskActivity = activitiesToDo.iterator().next();
    assertEquals(ActivityState.EXECUTING, getQueryRuntimeAPI().getTask(taskActivity.getUUID()).getState());

    //Check IllegalTaskStateException (suspend the task and try to finish)
    try {
      getRuntimeAPI().suspendTask(taskActivity.getUUID(), true);
      getRuntimeAPI().finishTask(taskActivity.getUUID(), true);
    } catch (final IllegalTaskStateException be) {
      //OK
    } 

    try {
      getRuntimeAPI().resumeTask(taskActivity.getUUID(), false);
      final Collection<TaskInstance> resumedTasks =
        getQueryRuntimeAPI().getTaskList("admin", ActivityState.EXECUTING);
      assertNotNull(resumedTasks);
      assertEquals(1, resumedTasks.size());
      getRuntimeAPI().finishTask(taskActivity.getUUID(), true);
    } catch (final BonitaException e) {
      fail("Ouch! How can it be?! Should never reach this statement!, exception: " + Misc.getStackTraceFrom(e));
    }

    assertEquals(ActivityState.FINISHED, getQueryRuntimeAPI().getTask(taskActivity.getUUID()).getState());
    final Collection<TaskInstance> finishedTasks = getQueryRuntimeAPI().getTaskList("admin", ActivityState.FINISHED);
    assertNotNull(finishedTasks);
    assertEquals(1, finishedTasks.size());
    final TaskInstance finishedTask = finishedTasks.iterator().next();
    assertEquals(ActivityState.FINISHED, finishedTask.getState());
    assertEquals(taskActivity.getActivityName(), finishedTask.getActivityName());

    // Try to finish a finished task
    try {
      getRuntimeAPI().finishTask(finishedTask.getUUID(), true);
      fail("expecting exception");
    } catch (final IllegalTaskStateException e) {
      // ok
    } 
    // Try to start a finished task
    try {
      getRuntimeAPI().startTask(finishedTask.getUUID(), true);
      fail("expecting exception");
    } catch (final IllegalTaskStateException e) {
      // ok
    } 

    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  /*
   *  test exceptions and the documented case from javadoc
   *  (if the instance or activity var does not exist it is created)
   *  for methods (of RuntimeAPI) :
   *     - setInstanceVariable
   *     - setActivityVariable
   *
   *     TODO (not tested):
   *       - test the AmbiguousActivityException raised if multiple iterations exist !!!!!
   *       - test ActivityNotFoundException if setting activity variables when activty
   *          already finished
   *       - test InstanceNotFoundException if setting instance variables when instance
   *          already finished
   *      Note:   need probably to introduce a new exception for that !!!!!
   */
  public void testSetVariables() throws Exception {
    final URL xpdlUrl = getClass().getResource("variables.xpdl");
    final ProcessDefinition process = getManagementAPI().deploy(getBusinessArchiveFromXpdl(xpdlUrl));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    //generating bad UUID to test exceptions
    final ProcessInstanceUUID badInstanceUUID = IdFactory.getNewInstanceUUID();
    final ActivityInstanceUUID badActivityUUID = IdFactory.getNewActivityUUID();

    // Check variables within execution pointing to act1 node
    final String activity1 = "act1";
    Collection<ActivityInstance> acts = getQueryRuntimeAPI().getActivityInstances(instanceUUID, activity1);
    assertEquals(1, acts.size());
    ActivityInstance activityInst = acts.iterator().next();
    assertNotNull(activityInst);
    assertEquals("act1", activityInst.getActivityName());

    //test InstanceNotFoundException
    try {
      getRuntimeAPI().setProcessInstanceVariable(badInstanceUUID, "string_process", "titi");
      fail("Check non existent Instance");
    } catch (final InstanceNotFoundException be) {
      //OK
      assertEquals(badInstanceUUID, be.getInstanceUUID());
    } 

    //test setActivityVariable with activityUUID
    try {
      getRuntimeAPI().setActivityInstanceVariable(badActivityUUID, "string_activity", "tutu");
      fail("Check non existent Activity Instance");
    } catch (final ActivityNotFoundException be) {
      //OK
      assertEquals(badActivityUUID, be.getActivityUUID());
    } 

    // start & terminate "act1" task
    executeTask(instanceUUID, activity1);
    checkStopped(instanceUUID, new String[]{activity1});

    //get info with "act2" activity
    final String activity2 = "act2";
    acts = getQueryRuntimeAPI().getActivityInstances(instanceUUID, activity2);
    assertEquals(1, acts.size());
    activityInst = acts.iterator().next();
    assertNotNull(activityInst);
    final ActivityInstanceUUID activityUUID = activityInst.getUUID();
    assertEquals(activity2, activityInst.getActivityName());


    //**** testing setVariable() ****
    //with valid parameter to set process instance variable
    getRuntimeAPI().setVariable(activityUUID, "string_process", "new_titi");
    assertEquals("new_titi", getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "string_process"));

    //with valid parameter to set activity instance variable
    getRuntimeAPI().setVariable(activityUUID, "string_activity", "new_titi");
    assertEquals("new_titi",getQueryRuntimeAPI().getActivityInstanceVariable(activityUUID, "string_activity"));


    //with invalid parameters to test exceptions:
    //InstanceNotFoundException
    //TODO
    try {
      getRuntimeAPI().setVariable(badActivityUUID, "string_process", "titi");
      fail("Check non existent Instance");
    } catch (final ActivityNotFoundException be) {
      //OK
      assertEquals(badActivityUUID, be.getActivityUUID());
    } 

    //VariableNotFoundException
    try {
      getRuntimeAPI().setVariable(activityUUID, "string_process_bidon", "titi");
      fail("Check non existent Instance");
    } catch (final VariableNotFoundException be) {
      //OK
      assertEquals("string_process_bidon", be.getVariableId());
    } 

    // start & terminate activity2 task
    executeTask(instanceUUID, activity2);

    // start & terminate "act3" task
    executeTask(instanceUUID, "act3");
    checkExecutedOnce(instanceUUID, new String[]{"act1", "act2", "act3"});

    getManagementAPI().deployJar("cmd.jar", Misc.generateJar(RemoveProcessClassloaderCommand.class));
    getCommandAPI().execute(new RemoveProcessClassloaderCommand(new ProcessDefinitionUUID("p", "1.0")));
    getManagementAPI().removeJar("cmd.jar");
    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testSetXMLVariablesXPath() throws BonitaException, IOException, ClassNotFoundException, SAXException, ParserConfigurationException, XPathExpressionException {
    final String sep = BonitaConstants.XPATH_VAR_SEPARATOR;
    ProcessBuilder builder = ProcessBuilder.createProcess("TestXMLVariablesXPath", "0.1");
    builder
    .addXMLData("rootAttribute", "<root test=\"before\"/>")
    .addXMLData("subNode", "<node><subnode>before</subnode></node>")
    .addXMLData("complexProcessNode", "<node><before/></node>")
    .addHuman(getLogin())
    .addHumanTask("task", getLogin())
    .addXMLData("rootNode", "<root>before</root>")
    .addXMLData("emptyRootNode", "<root/>")
    .addXMLData("complexTaskNode", "<node><before/></node>");
    ProcessDefinition processDef = builder.done();

    getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(processDef));
    getRuntimeAPI().instantiateProcess(processDef.getUUID());
    ActivityInstanceUUID activityUUID = getQueryRuntimeAPI().getOneTask(processDef.getUUID(), ActivityState.READY);

    XMLUnit.setIgnoreWhitespace(true);

    getRuntimeAPI().setVariable(activityUUID, "rootNode" + sep + "/root/text()", "after");
    Document expected = Misc.generateDocument("<root>after</root>");
    Document actual = (Document) getQueryRuntimeAPI().getVariable(activityUUID, "rootNode");
    assertTrue(XMLUnit.compareXML(expected, actual).identical());

    getRuntimeAPI().setVariable(activityUUID, "emptyRootNode" + sep + "/root/text()", "after");
    expected = Misc.generateDocument("<root>after</root>");
    actual = (Document) getQueryRuntimeAPI().getVariable(activityUUID, "emptyRootNode");
    assertTrue("Bug 2644", XMLUnit.compareXML(expected, actual).identical());

    getRuntimeAPI().setVariable(activityUUID, "rootAttribute" + sep + "/root/@test", "after");
    expected = Misc.generateDocument("<root test=\"after\"/>");
    actual = (Document) getQueryRuntimeAPI().getVariable(activityUUID, "rootAttribute");
    assertTrue(XMLUnit.compareXML(expected, actual).identical());

    getRuntimeAPI().setVariable(activityUUID, "subNode" + sep + "/node/subnode[1]/text()", "after");
    expected = Misc.generateDocument("<node><subnode>after</subnode></node>");
    actual = (Document) getQueryRuntimeAPI().getVariable(activityUUID, "subNode");
    assertTrue(XMLUnit.compareXML(expected, actual).identical());

    getRuntimeAPI().setVariable(activityUUID, "complexProcessNode" + sep + "/node/before", createAfterNode());
    expected = Misc.generateDocument("<node><after/></node>");
    actual = (Document) getQueryRuntimeAPI().getVariable(activityUUID, "complexProcessNode");
    assertTrue(XMLUnit.compareXML(expected, actual).identical());

    getRuntimeAPI().setVariable(activityUUID, "complexTaskNode" + sep + "/node/before", createAfterNode());
    expected = Misc.generateDocument("<node><after/></node>");
    actual = (Document) getQueryRuntimeAPI().getVariable(activityUUID, "complexTaskNode");
    assertTrue(XMLUnit.compareXML(expected, actual).identical());

    getRuntimeAPI().setVariable(activityUUID, "rootNode", "<toto>titi</toto>");
    expected = Misc.generateDocument("<toto>titi</toto>");
    actual = (Document) getQueryRuntimeAPI().getVariable(activityUUID, "rootNode");
    assertTrue(XMLUnit.compareXML(expected, actual).identical());

    getRuntimeAPI().setVariable(activityUUID, "rootAttribute", "<toto>titi</toto>");
    expected = Misc.generateDocument("<toto>titi</toto>");
    actual = (Document) getQueryRuntimeAPI().getVariable(activityUUID, "rootAttribute");
    assertTrue(XMLUnit.compareXML(expected, actual).identical());

    expected = Misc.generateDocument("<toto>titi</toto>");
    getRuntimeAPI().setVariable(activityUUID, "rootNode", expected);
    actual = (Document) getQueryRuntimeAPI().getVariable(activityUUID, "rootNode");
    assertTrue(XMLUnit.compareXML(expected, actual).identical());

    expected = Misc.generateDocument("<toto>titi</toto>");
    getRuntimeAPI().setVariable(activityUUID, "rootAttribute", expected);
    actual = (Document) getQueryRuntimeAPI().getVariable(activityUUID, "rootAttribute");
    assertTrue(XMLUnit.compareXML(expected, actual).identical());

    getManagementAPI().deleteProcess(processDef.getUUID());
  }

  public void testSetComplexXMLVariablesXPath() throws BonitaException, IOException, ClassNotFoundException, SAXException, ParserConfigurationException, XPathExpressionException {
    final String sep = BonitaConstants.XPATH_VAR_SEPARATOR;
    ProcessBuilder builder = ProcessBuilder.createProcess("TestComplexXMLVariablesXPath", "0.1");
    String data = createXmlFromResource("initialValue.xml");
    builder
    .addHuman(getLogin())
    .addXMLData("xmlData", data)
    .addHumanTask("task", getLogin());
    ProcessDefinition processDef = builder.done();

    getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(processDef));
    getRuntimeAPI().instantiateProcess(processDef.getUUID());
    ActivityInstanceUUID activityUUID = getQueryRuntimeAPI().getOneTask(processDef.getUUID(), ActivityState.READY);

    XMLUnit.setIgnoreWhitespace(true);

    Document dummyDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();

    // Update Attribute with Text
    getRuntimeAPI().setVariable(activityUUID, "xmlData" + sep + "/purchaseOrder/@orderDate", "attributeAsText");
    String actual = (String) getQueryRuntimeAPI().getVariable(activityUUID, "xmlData"+sep+"/purchaseOrder/@orderDate");
    assertEquals("attributeAsText", actual);

    // Create Attribute with Text
    getRuntimeAPI().setVariable(activityUUID, "xmlData" + sep + "/purchaseOrder/@newAttr1", "attributeAsText");
    actual = (String) getQueryRuntimeAPI().getVariable(activityUUID, "xmlData"+sep+"/purchaseOrder/@newAttr1");
    assertEquals("attributeAsText", actual);

    // Set Same Attribute with Dom Attribute
    Attr attr = dummyDoc.createAttribute("orderDate");
    attr.setValue("testSetAttributeWithDom");
    getRuntimeAPI().setVariable(activityUUID, "xmlData" + sep + "/purchaseOrder/@orderDate", attr);
    actual = (String) getQueryRuntimeAPI().getVariable(activityUUID, "xmlData"+sep+"/purchaseOrder/@orderDate");
    assertEquals("testSetAttributeWithDom", actual);

    // Set existing Attribute with unexisting Dom Attribute
    attr = dummyDoc.createAttribute("dummyAttribute");
    attr.setValue("Set existing Attribute with unexisting Dom Attribute");
    getRuntimeAPI().setVariable(activityUUID, "xmlData" + sep + "/purchaseOrder/@orderDate", attr);
    actual = (String) getQueryRuntimeAPI().getVariable(activityUUID, "xmlData"+sep+"/purchaseOrder/@orderDate");
    assertEquals("Set existing Attribute with unexisting Dom Attribute", actual);

    // Create new Attribute on Node with Dom Attribue
    attr = dummyDoc.createAttribute("newAttr2");
    attr.setValue("Create new Attribute on Node with Dom Attribue");
    getRuntimeAPI().setVariable(activityUUID, "xmlData" + sep + "/purchaseOrder", attr);
    actual = (String) getQueryRuntimeAPI().getVariable(activityUUID, "xmlData"+sep+"/purchaseOrder/@newAttr2");
    assertEquals("Create new Attribute on Node with Dom Attribue", actual);

    // Replace Node
    Element element = dummyDoc.createElement("shipTo");
    element.setAttribute("kikoo", "lol");
    getRuntimeAPI().setVariable(activityUUID, "xmlData" + sep + "/purchaseOrder/shipTo", element);
    Document actualDoc = (Document) getQueryRuntimeAPI().getVariable(activityUUID, "xmlData");
    int nbShipTo = 0;
    NodeList childNodes = actualDoc.getDocumentElement().getChildNodes();
    for (int i = 0; i < childNodes.getLength(); i++) {
      Node node = childNodes.item(i);
      if (node instanceof Element && ((Element)node).getTagName().equals("shipTo")) {
        nbShipTo++;
      }
    }
    assertEquals(1, nbShipTo);
    String kikooValue = (String) getQueryRuntimeAPI().getVariable(activityUUID, "xmlData" + sep + "/purchaseOrder/shipTo/@kikoo");
    assertEquals("lol", kikooValue);

    // Add Node
    element = dummyDoc.createElement("shipTo");
    element.setAttribute("kikoo", "lol2");
    getRuntimeAPI().setVariable(activityUUID, "xmlData" + sep + "/purchaseOrder" + sep + BonitaConstants.XPATH_APPEND_FLAG, element);
    actualDoc = (Document) getQueryRuntimeAPI().getVariable(activityUUID, "xmlData");
    nbShipTo = 0;
    childNodes = actualDoc.getDocumentElement().getChildNodes();
    for (int i = 0; i < childNodes.getLength(); i++) {
      Node node = childNodes.item(i);
      if (node instanceof Element && ((Element)node).getTagName().equals("shipTo")) {
        nbShipTo++;
      }
    }
    assertEquals(2, nbShipTo);
    kikooValue = (String) getQueryRuntimeAPI().getVariable(activityUUID, "xmlData" + sep + "/purchaseOrder/shipTo[2]/@kikoo");
    assertEquals("lol2", kikooValue);

    getManagementAPI().deleteProcess(processDef.getUUID());
  }


  public void testXMLDataWithDocumentInitialValue() throws BonitaException, IOException, ClassNotFoundException, SAXException, ParserConfigurationException, XPathExpressionException {
    final String init = "<toto>titi</toto>";
    final Document doc = Misc.generateDocument(init);

    ProcessDefinition processDef = ProcessBuilder.createProcess("my", "0.1")
    .addXMLData("doc", doc)
    .addHuman(getLogin())
    .addHumanTask("task", getLogin())
    .done();

    getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(processDef));
    getRuntimeAPI().instantiateProcess(processDef.getUUID());
    ActivityInstanceUUID activityUUID = getQueryRuntimeAPI().getOneTask(processDef.getUUID(), ActivityState.READY);

    XMLUnit.setIgnoreWhitespace(true);
    Document actual = (Document) getQueryRuntimeAPI().getVariable(activityUUID, "doc");
    assertTrue(XMLUnit.compareXML(doc, actual).identical());

    getManagementAPI().deleteProcess(processDef.getUUID());
  }

  public void testXmlWithNameSpace() throws Exception {
    String data = createXmlFromResource("initialXmlWithNamespace.xml");

    ProcessDefinition processDef = ProcessBuilder.createProcess("my", "0.1").addXMLData("invoice", data).addHuman(getLogin()).addHumanTask("task", getLogin()).done();

    getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(processDef));
    getRuntimeAPI().instantiateProcess(processDef.getUUID());
    ActivityInstanceUUID activityUUID = getQueryRuntimeAPI().getOneTask(processDef.getUUID(), ActivityState.READY);

    getRuntimeAPI().setVariable(activityUUID, "invoice$/Invoice/InvoiceNumber/text()", "51");

    XMLUnit.setIgnoreWhitespace(true);
    Document actual = (Document) getQueryRuntimeAPI().getVariable(activityUUID, "invoice");

    String expected = createXmlFromResource("initialXmlWithNamespace_expect.xml");
    assertTrue(XMLUnit.compareXML(Misc.generateDocument(expected), actual).identical());

    getManagementAPI().deleteProcess(processDef.getUUID());
  }

  /**
   * @param string
   * @return
   * @throws IOException
   */
  protected String createXmlFromResource(String string) throws IOException {
    InputStream is = getClass().getResource(string).openStream();
    byte[] buffer = new byte[1024];
    int nbBytes;
    List<Byte> bytes = new ArrayList<Byte>();
    while ((nbBytes = is.read(buffer, 0, 1024)) != -1) {
      for (int i = 0; i < nbBytes; i++) {
        bytes.add(buffer[i]);
      }
    }
    is.close();
    buffer = new byte[bytes.size()];
    for (int i = 0; i < bytes.size(); i++) {
      buffer[i] = bytes.get(i);
    }
    String data = new String(buffer);
    return data;
  }

  private Node createAfterNode() throws ParserConfigurationException {
    DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
    return builder.newDocument().createElement("after");
  }


  public void testSetVariableWithJavaSetter() throws Exception {
    ProcessDefinition processDef = ProcessBuilder.createProcess("my", "0.1")
    .addObjectData("globalEmployee", Employee.class.getName(), "${new org.ow2.bonita.facade.Employee()}")
    .addHuman(getLogin())
    .addHumanTask("task", getLogin())
    .addObjectData("localEmployee", Employee.class.getName(), "${new org.ow2.bonita.facade.Employee()}")
    .done();
    getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(processDef, Employee.class, Address.class));
    getRuntimeAPI().instantiateProcess(processDef.getUUID());
    ActivityInstanceUUID activityUUID = getQueryRuntimeAPI().getOneTask(processDef.getUUID(), ActivityState.READY);

    getRuntimeAPI().startTask(activityUUID, true);
    String sep = BonitaConstants.JAVA_VAR_SEPARATOR;
    getRuntimeAPI().setVariable(activityUUID, "globalEmployee" + sep + "globalEmployee.getAddress()" + sep + "setStreet", "Place Victor Hugo");
    getRuntimeAPI().setVariable(activityUUID, "localEmployee" + sep + "localEmployee.getAddress()" + sep + "setStreet", "Place Grenette");
    getRuntimeAPI().finishTask(activityUUID, true);

    Object globalEmployee = getQueryRuntimeAPI().getVariable(activityUUID, "globalEmployee");
    Object globalAddress = globalEmployee.getClass().getMethod("getAddress").invoke(globalEmployee);
    Object localEmployee = getQueryRuntimeAPI().getVariable(activityUUID, "localEmployee");
    Object localAddress = localEmployee.getClass().getMethod("getAddress").invoke(localEmployee);

    assertEquals("Place Victor Hugo", globalAddress.getClass().getMethod("getStreet").invoke(globalAddress));
    assertEquals("Place Grenette", localAddress.getClass().getMethod("getStreet").invoke(localAddress));

    getManagementAPI().deleteProcess(processDef.getUUID());
  }

  public void testSetVariableWithJavaSetterWithBlankGetterPath() throws Exception {
    ProcessDefinition processDef = ProcessBuilder.createProcess("my", "0.1")
    .addObjectData("list", ArrayList.class.getName(), "${new java.util.ArrayList()}")
    .addHuman(getLogin())
    .addHumanTask("task", getLogin())
    .done();
    getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(processDef));
    getRuntimeAPI().instantiateProcess(processDef.getUUID());
    ActivityInstanceUUID activityUUID = getQueryRuntimeAPI().getOneTask(processDef.getUUID(), ActivityState.READY);

    getRuntimeAPI().startTask(activityUUID, true);
    String sep = BonitaConstants.JAVA_VAR_SEPARATOR;
    getRuntimeAPI().setVariable(activityUUID, "list" + sep + "" + sep + "add", "test");
    getRuntimeAPI().finishTask(activityUUID, true);

    List< ? > list = (List< ? >) getQueryRuntimeAPI().getVariable(activityUUID, "list");

    assertEquals("test", list.get(0));

    getManagementAPI().deleteProcess(processDef.getUUID());
  }

  public void testSetVariableWithJavaSetterInError() throws Exception {
    ProcessDefinition processDef = ProcessBuilder.createProcess("my", "0.1")
    .addObjectData("globalEmployee", Employee.class.getName(), "${new org.ow2.bonita.facade.Employee()}")
    .addHuman(getLogin())
    .addHumanTask("task", getLogin())
    .addObjectData("localEmployee", Employee.class.getName(), "${new org.ow2.bonita.facade.Employee()}")
    .done();
    getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(processDef, Employee.class, Address.class));
    getRuntimeAPI().instantiateProcess(processDef.getUUID());
    ActivityInstanceUUID activityUUID = getQueryRuntimeAPI().getOneTask(processDef.getUUID(), ActivityState.READY);

    getRuntimeAPI().startTask(activityUUID, true);
    String sep = BonitaConstants.JAVA_VAR_SEPARATOR;

    // Global
    try {
      // Bad variable name
      getRuntimeAPI().setVariable(activityUUID, "error" + sep + "globalEmployee.getAddress()" + sep + "setStreet", "Place Victor Hugo");
      fail("No Exception was raised");
    } catch (VariableNotFoundException ex) {
      // OK
    } 
    try {
      // Bad getter name
      getRuntimeAPI().setVariable(activityUUID, "globalEmployee" + sep + "error.getAddress()" + sep + "setStreet", "Place Victor Hugo");
      fail("No Exception was raised");
    } catch (VariableNotFoundException ex) {
      // OK
    } 
    try {
      // Bad setter name
      getRuntimeAPI().setVariable(activityUUID, "globalEmployee" + sep + "error.getAddress()" + sep + "error", "Place Victor Hugo");
      fail("No Exception was raised");
    } catch (VariableNotFoundException ex) {
      // OK
    } 
    try {
      // missing a part
      getRuntimeAPI().setVariable(activityUUID, "globalEmployee" + sep + "globalEmployee.getAddress()", "Place Victor Hugo");
      fail("No Exception was raised");
    } catch (VariableNotFoundException ex) {
      // OK
    } 

    // Local
    try {
      // Bad variable name
      getRuntimeAPI().setVariable(activityUUID, "error" + sep + "localEmployee.getAddress()" + sep + "setStreet", "Place Victor Hugo");
      fail("No Exception was raised");
    } catch (VariableNotFoundException ex) {
      // OK
    } 
    try {
      // Bad getter name
      getRuntimeAPI().setVariable(activityUUID, "localEmployee" + sep + "error.getAddress()" + sep + "setStreet", "Place Victor Hugo");
      fail("No Exception was raised");
    } catch (VariableNotFoundException ex) {
      // OK
    }
    try {
      // Bad setter name
      getRuntimeAPI().setVariable(activityUUID, "localEmployee" + sep + "localEmployee.getAddress()" + sep + "error", "Place Victor Hugo");
      fail("No Exception was raised");
    } catch (VariableNotFoundException ex) {
      // OK
    }
    try {
      // missing a part
      getRuntimeAPI().setVariable(activityUUID, "localEmployee" + sep + "localEmployee.getAddress()", "Place Victor Hugo");
      fail("No Exception was raised");
    } catch (VariableNotFoundException ex) {
      // OK
    } 

    getRuntimeAPI().setVariable(activityUUID, "globalEmployee" + sep + "globalEmployee.getAddress()" + sep + "setStreet", "Place Victor Hugo");
    getRuntimeAPI().setVariable(activityUUID, "localEmployee" + sep + "localEmployee.getAddress()" + sep + "setStreet", "Place Grenette");

    getRuntimeAPI().finishTask(activityUUID, true);

    Object globalEmployee = getQueryRuntimeAPI().getVariable(activityUUID, "globalEmployee");
    Object globalAddress = globalEmployee.getClass().getMethod("getAddress").invoke(globalEmployee);
    Object localEmployee = getQueryRuntimeAPI().getVariable(activityUUID, "localEmployee");
    Object localAddress = localEmployee.getClass().getMethod("getAddress").invoke(localEmployee);

    assertEquals("Place Victor Hugo", globalAddress.getClass().getMethod("getStreet").invoke(globalAddress));
    assertEquals("Place Grenette", localAddress.getClass().getMethod("getStreet").invoke(localAddress));

    getManagementAPI().deleteProcess(processDef.getUUID());
  }

  public void testInitialJavaValue() throws Exception {
    ProcessDefinition processDef = ProcessBuilder.createProcess("my", "0.1")
    .addObjectData("globalEmployee", Employee.class.getName(), new Employee())
    .addHuman(getLogin())
    .addHumanTask("task", getLogin())
    .addObjectData("localEmployee", Employee.class.getName(), new Employee())
    .done();
    final ProcessDefinition process = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(processDef, Employee.class, Address.class));
    getManagementAPI().deleteProcess(process.getUUID());
  }

  /*
   * Testing getActivityvariables() without cycle/iteration
   * There's 2 methods for getActivityVariables()
   * 1) Map<String, Object> getActivityVariables(ProcessInstanceUUID instanceUUID,String activityId)
   *  throws: ActivityNotFoundException,
   *           AmbiguousActivityException,
   *           InstanceNotFoundException;
   *  2) Map<String, Object> getActivityVariables(ActivityInstanceUUID activityUUID)
   *   throws ActivityNotFoundException;
   *
   *  Check also if trying with wrong iterationId an ActivityNotFoundException is raised
   *  (specific exception with the iteration id as parameter).
   */
  public void testGetActivityVariables() throws BonitaException {
    final URL xpdlUrl = getClass().getResource("variables.xpdl");
    final String activity1 = "act1";
    final ProcessDefinition process = getManagementAPI().deploy(getBusinessArchiveFromXpdl(xpdlUrl));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    //generating bad UUID to test exceptions
    //ProcessInstanceUUID badInstanceUUID = IdFactory.getNewInstanceUUID();
    final ActivityInstanceUUID badActivityUUID = IdFactory.getNewActivityUUID();

    // Check variables within execution pointing to act1 node
    Collection<ActivityInstance> acts = getQueryRuntimeAPI().getActivityInstances(instanceUUID, activity1);
    assertEquals(1, acts.size());
    ActivityInstance activityInst = acts.iterator().next();
    assertNotNull(activityInst);
    ActivityInstanceUUID activityUUID = activityInst.getUUID();

    //in act1 there's only process variables so the method returns an empty list
    Map<String, Object> variables = getQueryRuntimeAPI().getActivityInstanceVariables(activityUUID);
    assertTrue(variables.isEmpty());

    //Check raised exceptions with getActivityVariables(ActivityInstanceUUID activityUUID)
    // 1- ActivityNotFoundException
    try {

      getQueryRuntimeAPI().getActivityInstanceVariables(badActivityUUID);
      fail("Check non existent activity");
    } catch (final ActivityNotFoundException be) {
      //OK
    } 

    // start & terminate "act1" task
    executeTask(instanceUUID, activity1);
    checkStopped(instanceUUID, new String[]{activity1});

    //execution is poniting to act2 which contains process and activity variables
    acts = getQueryRuntimeAPI().getActivityInstances(instanceUUID, "act2");
    assertEquals(1, acts.size());
    activityInst = acts.iterator().next();
    assertNotNull(activityInst);
    activityUUID = activityInst.getUUID();

    //when using parameters: activityUUID
    variables = getQueryRuntimeAPI().getActivityInstanceVariables(activityUUID);
    assertFalse(variables.isEmpty());
    assertEquals(2, variables.size());
    assertTrue(variables.containsKey("string_activity"));
    assertTrue(variables.containsKey("enum_activity"));

    // start & terminate "act2" task
    executeTask(instanceUUID, "act2");
    // check variables within execution pointing to act3 node


    acts = getQueryRuntimeAPI().getActivityInstances(instanceUUID, "act3");
    assertEquals(1, acts.size());
    activityInst = acts.iterator().next();
    assertNotNull(activityInst);
    activityUUID = activityInst.getUUID();
    variables = getQueryRuntimeAPI().getActivityInstanceVariables(activityUUID);
    assertTrue(variables.isEmpty());

    checkStopped(instanceUUID, new String[]{"act1", "act2"});

    // start & terminate "act3" task
    executeTask(instanceUUID, "act3");
    checkExecutedOnce(instanceUUID, new String[]{"act1", "act2", "act3"});

    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testGetProcessVariables() throws BonitaException {
    final URL xpdlUrl = getClass().getResource("variables.xpdl");
    final String activity1 = "act1";
    final ProcessDefinition process = getManagementAPI().deploy(getBusinessArchiveFromXpdl(xpdlUrl));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    //generating bad UUID to test exceptions
    final ProcessInstanceUUID badInstanceUUID = IdFactory.getNewInstanceUUID();

    //in act1 there's only process variables
    Map<String, Object> variables = getQueryRuntimeAPI().getProcessInstanceVariables(instanceUUID);
    assertFalse(variables.isEmpty());
    assertEquals(2, variables.size());
    assertTrue(variables.containsKey("string_process"));
    assertTrue(variables.containsKey("enum_process"));

    //Check RAISED EXCEPTIONS
    // InstanceNotFoundException
    try {
      getQueryRuntimeAPI().getProcessInstanceVariables(badInstanceUUID);
      fail("Check non existent instance");
    } catch (final InstanceNotFoundException be) {
      //OK
    } 

    // start & terminate "act1" task
    executeTask(instanceUUID, activity1);
    checkStopped(instanceUUID, new String[]{activity1});

    // Check variables within execution pointing to act2 node
    variables = getQueryRuntimeAPI().getProcessInstanceVariables(instanceUUID);
    assertFalse(variables.isEmpty());
    assertEquals(2, variables.size());
    assertTrue(variables.containsKey("string_process"));
    assertTrue(variables.containsKey("enum_process"));
    assertFalse(variables.containsKey("string_activity"));
    assertFalse(variables.containsKey("enum_activity"));

    // start & terminate "act2" task
    executeTask(instanceUUID, "act2");

    // check variables within execution pointing to act3 node
    variables = getQueryRuntimeAPI().getProcessInstanceVariables(instanceUUID);
    assertFalse(variables.isEmpty());
    assertEquals(2, variables.size());
    assertTrue(variables.containsKey("string_process"));
    assertTrue(variables.containsKey("enum_process"));
    assertFalse(variables.containsKey("string_activity"));
    assertFalse(variables.containsKey("enum_activity"));

    checkStopped(instanceUUID, new String[]{"act1", "act2"});

    // start & terminate "act3" task
    executeTask(instanceUUID, "act3");
    checkExecutedOnce(instanceUUID, new String[]{"act1", "act2", "act3"});

    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testGetVariables() throws BonitaException {
    final URL xpdlUrl = getClass().getResource("variables.xpdl");
    final String activity1 = "act1";
    final ProcessDefinition process = getManagementAPI().deploy(getBusinessArchiveFromXpdl(xpdlUrl));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    //generating bad UUID to test exceptions
    final ActivityInstanceUUID badActivityUUID = IdFactory.getNewActivityUUID();

    Collection<ActivityInstance> acts = getQueryRuntimeAPI().getActivityInstances(instanceUUID, activity1);
    assertEquals(1, acts.size());
    ActivityInstance activityInst = acts.iterator().next();
    assertNotNull(activityInst);
    ActivityInstanceUUID activityUUID = activityInst.getUUID();

    //in act1 there's only process variables
    Map<String, Object> variables = getQueryRuntimeAPI().getVariables(activityUUID);
    assertFalse(variables.isEmpty());
    assertEquals(2, variables.size());
    assertTrue(variables.containsKey("string_process"));
    assertTrue(variables.containsKey("enum_process"));

    //Check RAISED EXCEPTIONS
    // ActivityNotFoundException
    try {
      getQueryRuntimeAPI().getVariables(badActivityUUID);
      fail("Check non existent activity");
    } catch (final ActivityNotFoundException be) {
      //OK
    } 

    // start & terminate "act1" task
    executeTask(instanceUUID, activity1);
    checkStopped(instanceUUID, new String[]{activity1});

    acts = getQueryRuntimeAPI().getActivityInstances(instanceUUID, "act2");
    assertEquals(1, acts.size());
    activityInst = acts.iterator().next();
    assertNotNull(activityInst);
    activityUUID = activityInst.getUUID();

    variables = getQueryRuntimeAPI().getVariables(activityUUID);
    assertFalse(variables.isEmpty());
    assertEquals(4, variables.size());
    assertTrue(variables.containsKey("string_process"));
    assertTrue(variables.containsKey("enum_process"));
    assertTrue(variables.containsKey("string_activity"));
    assertTrue(variables.containsKey("enum_activity"));

    // start & terminate "act2" task
    executeTask(instanceUUID, "act2");
    // check variables within execution pointing to act3 node
    acts = getQueryRuntimeAPI().getActivityInstances(instanceUUID, "act3");
    assertEquals(1, acts.size());
    activityInst = acts.iterator().next();
    assertNotNull(activityInst);
    activityUUID = activityInst.getUUID();

    variables = getQueryRuntimeAPI().getVariables(activityUUID);
    assertFalse(variables.isEmpty());
    assertEquals(2, variables.size());
    assertTrue(variables.containsKey("string_process"));
    assertTrue(variables.containsKey("enum_process"));
    assertFalse(variables.containsKey("string_activity"));
    assertFalse(variables.containsKey("enum_activity"));

    checkStopped(instanceUUID, new String[]{"act1", "act2"});

    // start & terminate "act3" task
    executeTask(instanceUUID, "act3");
    checkExecutedOnce(instanceUUID, new String[]{"act1", "act2", "act3"});

    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testGetActivityInstance() throws BonitaException {
    final URL xpdlUrl = getClass().getResource("usertest2_1.0.xpdl");
    final ProcessDefinition process = getManagementAPI().deploy(getBusinessArchiveFromXpdl(xpdlUrl));
    final ProcessDefinitionUUID processUUID = process.getUUID();
    try {
      final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);
      assertEquals(InstanceState.STARTED, getQueryRuntimeAPI().getProcessInstance(instanceUUID).getInstanceState());
      //generating bad UUID to test exceptions
      final ActivityInstanceUUID badActivityUUID = IdFactory.getNewActivityUUID();

      final Collection<ActivityInstance> acts = getQueryRuntimeAPI().getActivityInstances(instanceUUID, "a");
      assertEquals(1, acts.size());
      final ActivityInstanceUUID activityUUID = acts.iterator().next().getUUID();

      ActivityInstance activityInst = getQueryRuntimeAPI().getActivityInstance(activityUUID);
      assertNotNull(activityInst);
      assertEquals("a", activityInst.getActivityName());
      //check ActivityNotFoundException
      try {
        activityInst = getQueryRuntimeAPI().getActivityInstance(badActivityUUID);
      } catch (final ActivityNotFoundException ae) {
        assertEquals(badActivityUUID, ae.getActivityUUID());
      } 

    } catch (final ProcessNotFoundException be) {
      throw new RuntimeException("Missing resource!", be);
    }
    getRuntimeAPI().deleteAllProcessInstances(processUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  /*
   * Testing getActivityVariable() without cycle/iteration
   *
   *  getActivityVariable(ActivityInstanceUUID activityUUID,String variableId)
   *     throws : ActivityNotFoundException,
   *              VariableNotFoundException;
   */
  public void testGetActivityVariable() throws BonitaException {
    final URL xpdlUrl = getClass().getResource("variables.xpdl");
    final String activity1 = "act1";
    final ProcessDefinition process = getManagementAPI().deploy(getBusinessArchiveFromXpdl(xpdlUrl));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    //generating bad UUID to test exceptions
    //ProcessInstanceUUID badInstanceUUID = IdFactory.getNewInstanceUUID();
    final ActivityInstanceUUID badActivityUUID = IdFactory.getNewActivityUUID();

    // Check variables within execution pointing to act1 node
    final Collection<ActivityInstance> acts = getQueryRuntimeAPI().getActivityInstances(instanceUUID, activity1);
    assertEquals(1, acts.size());
    final ActivityInstance activityInst = acts.iterator().next();
    assertNotNull(activityInst);
    final ActivityInstanceUUID activityUUID = activityInst.getUUID();

    //Check getActivityVariable(ActivityInstanceUUID activityUUID,String variableId) method
    //process var
    try {
      getQueryRuntimeAPI().getActivityInstanceVariable(activityUUID, "string_process");
      fail("string_process is not local to act1 !");
    } catch (final VariableNotFoundException ve) {
      //OK
      assertEquals("string_process", ve.getVariableId());
    }  

    //activity var does not exist
    //VariableNotFoundException
    try {
      getQueryRuntimeAPI().getActivityInstanceVariable(activityUUID, "string_activity");
      fail("Check the activity var does not exist in act1");
    } catch (final VariableNotFoundException ve) {
      //OK
      assertEquals("string_activity", ve.getVariableId());
    } 
    //ActivityNotFoundException
    try {
      getQueryRuntimeAPI().getActivityInstanceVariable(badActivityUUID, "string_process");
      fail("Check non existent badActivityUUID");
    } catch (final ActivityNotFoundException ae) {
      //OK
      assertEquals(badActivityUUID, ae.getActivityUUID());
    } 

    // start & terminate "act1" task
    executeTask(instanceUUID, activity1);
    checkStopped(instanceUUID, new String[]{activity1});
    // activityInst is unused
    // activityInst = getQueryRuntimeAPI().getActivityInstance(instanceUUID, "act2");

    // start & terminate "act2" task
    executeTask(instanceUUID, "act2");
    // check variables within execution pointing to act3 node

    //    activityInst = getQueryRuntimeAPI().getActivityInstance(instanceUUID, "act3");
    // activityInst is unused
    checkStopped(instanceUUID, new String[]{"act1", "act2"});

    // start & terminate "act3" task
    executeTask(instanceUUID, "act3");
    checkExecutedOnce(instanceUUID, new String[]{"act1", "act2", "act3"});

    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testGetProcessVariable() throws BonitaException {
    final URL xpdlUrl = getClass().getResource("variables.xpdl");
    final String activity1 = "act1";
    final ProcessDefinition process = getManagementAPI().deploy(getBusinessArchiveFromXpdl(xpdlUrl));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    //generating bad UUID to test exceptions
    final ProcessInstanceUUID badInstanceUUID = IdFactory.getNewInstanceUUID();

    //Check getActivityVariable(ActivityInstanceUUID activityUUID,String variableId) method
    //process var
    String varStringProcess = (String)getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "string_process");
    assertTrue(varStringProcess.equals("initial string value"));

    //No activity var exist
    //VariableNotFoundException
    try {
      getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "string_activity");
      fail("Check the activity var does not exist in act1");
    } catch (final VariableNotFoundException ve) {
      //OK
      assertEquals("string_activity", ve.getVariableId());
    } 
    //InstanceNotFoundException
    try {
      getQueryRuntimeAPI().getProcessInstanceVariable(badInstanceUUID, "string_process");
      fail("Check non existent badActivityUUID");
    } catch (final InstanceNotFoundException ae) {
      //OK
      assertEquals(badInstanceUUID, ae.getInstanceUUID());
    } 

    // start & terminate "act1" task
    executeTask(instanceUUID, activity1);
    checkStopped(instanceUUID, new String[]{activity1});
    // activityInst is unused
    // activityInst = getQueryRuntimeAPI().getActivityInstance(instanceUUID, "act2");

    // start & terminate "act2" task
    executeTask(instanceUUID, "act2");
    // check variables within execution pointing to act3 node

    // activityInst = getQueryRuntimeAPI().getActivityInstance(instanceUUID, "act3");
    // activityInst is unused
    checkStopped(instanceUUID, new String[]{"act1", "act2"});

    // start & terminate "act3" task
    executeTask(instanceUUID, "act3");
    checkExecutedOnce(instanceUUID, new String[]{"act1", "act2", "act3"});

    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  /*
   * Test Collection<TaskInstance> getTaskList(TaskState taskState);
   * from RuntimeReadOnlyAPI
   */

  //Takes into account
  //    . the states (4 states available)
  //    . the 3 types of methods depending on the parameters:
  //                . TaskState taskState,
  //                . ProcessInstanceUUID instanceUUID,
  //                . java.lang.String userId
  // Note for the state:
  // if state == null  return IllegalArgumentException.
  // case 1: state = null and state = READY
  // - if task assigned to a user check this (authenticated) user can get the task.
  // - check that an other users of the candidates list can't get the task.
  // - if the task is not assigned (role mapper without performer assign)
  // - check that an other user in the candidate list can get the task.
  // - if 2 instances are created check that 2 tasks are available.
  // (check the tasks has READY state)
  // case 2: state = SUSPENDED
  // check task has been assigned to the user performing suspendTask
  // case 3: state = EXECUTING
  // check task has been assigned to the user performing startTask
  // case 4: state = FINISHED
  public void testGetTaskList() throws BonitaException, LoginException {

    final String loginAdmin = "admin";
    final String passwordAdmin = "bpm";
    final String loginJohn = "john";
    final String passwordJohn = "bpm";
    final URL xpdlUrl = getClass().getResource("testGetTask.xpdl");
    final ProcessDefinition process = getManagementAPI().deploy(getBusinessArchiveFromXpdl(xpdlUrl, AdminsRoleMapper.class));
    final ProcessDefinitionUUID processUUID = process.getUUID();
    ActivityInstanceUUID taskUUID = null;
    ProcessInstanceUUID instanceUUID = null;
    final ProcessInstanceUUID badInstanceUUID = IdFactory.getNewInstanceUUID();

    //**** case 1 test ***
    try {
      instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);
      assertEquals(InstanceState.STARTED, getQueryRuntimeAPI().getProcessInstance(instanceUUID).getInstanceState());
      //getToDoList with taskState = null
      try {
        getQueryRuntimeAPI().getTaskList(null);
        fail("IllegalArgumentException has been raised by the QueryRuntimeAPIImpl !");
      } catch (final IllegalArgumentException e) {
        //OK
      } catch (NullPointerException e){
        //Ok REST
        StackTraceElement firstElement = e.getStackTrace()[0];
        assertTrue(firstElement.getClassName().equals("org.jboss.resteasy.client.ClientRequest"));
        assertTrue(firstElement.getMethodName().equals("toString"));
      }

      try {
        getQueryRuntimeAPI().getTaskList(instanceUUID, (ActivityState)null);
        fail("IllegalArgumentException has been raised by the QueryRuntimeAPIImpl !");
      } catch (final IllegalArgumentException e) {
        //OK
      } catch (NullPointerException e){
        //Ok REST
        StackTraceElement firstElement = e.getStackTrace()[0];
        assertTrue(firstElement.getClassName().equals("org.jboss.resteasy.client.ClientRequest"));
        assertTrue(firstElement.getMethodName().equals("toString"));
      }

      try {
        getQueryRuntimeAPI().getTaskList(instanceUUID, (Collection<ActivityState>)null);
        fail("IllegalArgumentException has been raised by the QueryRuntimeAPIImpl !");
      } catch (final IllegalArgumentException e) {
        //OK
      } 

      try {
        getQueryRuntimeAPI().getTaskList(instanceUUID, loginAdmin, null);
        fail("IllegalArgumentException has been raised by the QueryRuntimeAPIImpl !");
      } catch (final IllegalArgumentException e) {
        //OK
      } catch (NullPointerException e){
        //Ok REST
        StackTraceElement firstElement = e.getStackTrace()[0];
        assertTrue(firstElement.getClassName().equals("org.jboss.resteasy.client.ClientRequest"));
        assertTrue(firstElement.getMethodName().equals("toString"));
      }

      //getToDoList to get task for activity = "a" with taskState = READY
      final Collection<TaskInstance> taskActivities = getQueryRuntimeAPI().getTaskList(ActivityState.READY);
      assertEquals(1, taskActivities.size());

      final Collection<TaskInstance> taskActivitiesWithInst =
        getQueryRuntimeAPI().getTaskList(instanceUUID, ActivityState.READY);
      assertEquals(1, taskActivitiesWithInst.size());

      //InstanceNotFoundException for getTaskList with argt: instanceUUID, taskState
      try {
        getQueryRuntimeAPI().getTaskList(badInstanceUUID, ActivityState.READY);
        fail("check InstanceNotFoundException has been raised");
      } catch (final InstanceNotFoundException ie) {
        assertEquals(ie.getInstanceUUID(), badInstanceUUID);
      } 

      //InstanceNotFoundException for getTaskList with argt: instanceUUID, userId, taskState
      try {
        getQueryRuntimeAPI().getTaskList(badInstanceUUID, loginAdmin, ActivityState.READY);
        fail("check InstanceNotFoundException has been raised");
      } catch (final InstanceNotFoundException ie) {
        assertEquals(ie.getInstanceUUID(), badInstanceUUID);
      } 

      final TaskInstance taskActivity = taskActivities.iterator().next();
      assertNotNull(taskActivity.getUUID());
      taskUUID = taskActivity.getUUID();
      assertEquals("a", taskActivity.getActivityName());
      assertNotNull(taskActivity.getCreatedDate());
    } catch (final ProcessNotFoundException be) {
      throw new RuntimeException("Missing resource!", be);
    }

    //check the user john can't get the task
    loginAs(loginJohn, passwordJohn);
    Collection<TaskInstance> taskActivities = getQueryRuntimeAPI().getTaskList(ActivityState.READY);
    assertEquals(0, taskActivities.size());

    // go on finish task a (create task b)
    // loginAdmin is the user assigned to the task : a
    loginAs(loginAdmin, passwordAdmin);

    //case 4: test for state = FINISHED
    taskActivities = getQueryRuntimeAPI().getTaskList(ActivityState.FINISHED);
    assertEquals(0, taskActivities.size());

    //start&finish task : a
    getRuntimeAPI().startTask(taskUUID, true);
    taskActivities = getQueryRuntimeAPI().getTaskList(ActivityState.EXECUTING);
    assertEquals(1, taskActivities.size());
    getRuntimeAPI().finishTask(taskUUID, true);

    taskActivities = getQueryRuntimeAPI().getTaskList(ActivityState.FINISHED);
    assertEquals(1, taskActivities.size());
    assertEquals("a", taskActivities.iterator().next().getActivityName());

    //check admin user can get the task b
    loginAs(loginAdmin, passwordAdmin);
    //Tasks for authenticated user
    taskActivities = getQueryRuntimeAPI().getTaskList(ActivityState.READY);
    assertEquals(1, taskActivities.size());
    //Tasks for a choosen user (admin)
    final Collection<TaskInstance> taskActivitiesAdminUser =
      getQueryRuntimeAPI().getTaskList(loginAdmin,ActivityState.READY);
    final Collection<TaskInstance> taskActivitiesAdminUserWithInstUUID =
      getQueryRuntimeAPI().getTaskList(instanceUUID, loginAdmin,ActivityState.READY);

    assertEquals("Check loginAdmin can get its tasks",1, taskActivities.size());
    assertEquals("Check tasks can be got for admin user",1, taskActivitiesAdminUserWithInstUUID.size());

    final TaskInstance taskActivityAdminUser = taskActivitiesAdminUser.iterator().next();
    final TaskInstance taskActivity = taskActivities.iterator().next();

    assertEquals(taskActivityAdminUser.getActivityName() , taskActivity.getActivityName());

    //other checks on got taskInstance
    assertNotNull(taskActivity.getActivityName());
    taskUUID = taskActivity.getUUID();

    //check task b is not assigned and candidates list contains : admin, john
    assertEquals(BonitaConstants.SYSTEM_USER, taskActivity.getUpdatedBy());
    assertFalse(taskActivity.isTaskAssigned());
    assertNull(taskActivity.getTaskUser());
    /*
    try {
      fail(taskActivity.getTaskUser());
    } catch (final IllegalStateException ie) {
      //OK
    }
     */


    assertNotNull(taskActivity.getTaskCandidates());
    assertTrue(taskActivity.getTaskCandidates().containsAll(Arrays.asList(loginAdmin, loginJohn)));
    assertEquals("b", taskActivity.getActivityName());

    //check john user can get also the task b
    loginAs(loginJohn, passwordJohn);
    taskActivities = getQueryRuntimeAPI().getTaskList(ActivityState.READY);
    assertEquals(1, taskActivities.size());

    //Check that if an other instance of the same process is created 2 tasks are available for admin user.
    loginAs(loginAdmin, passwordAdmin);
    instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);
    assertEquals(InstanceState.STARTED, getQueryRuntimeAPI().getProcessInstance(instanceUUID).getInstanceState());
    //getToDoList to get task for activity = "a"
    taskActivities = getQueryRuntimeAPI().getTaskList(ActivityState.READY);
    assertEquals(2, taskActivities.size());

    //delete the 2 nd created instance
    getRuntimeAPI().deleteProcessInstance(instanceUUID);

    //*** case 2 test ****
    //**suspend the task b withou assignment !  (only candidates are set)
    loginAs(loginAdmin, passwordAdmin);
    getRuntimeAPI().suspendTask(taskUUID, false);
    taskActivities = getQueryRuntimeAPI().getTaskList(ActivityState.SUSPENDED);
    assertEquals(1, taskActivities.size());

    //check john user can't get the suspended task (by admin user)
    loginAs(loginJohn, passwordJohn);
    taskActivities = getQueryRuntimeAPI().getTaskList(ActivityState.SUSPENDED);
    assertEquals(1, taskActivities.size());

    //check admin user can get the suspended task (by admin user)
    loginAs(loginAdmin, passwordAdmin);
    taskActivities = getQueryRuntimeAPI().getTaskList(ActivityState.SUSPENDED);
    assertEquals(1, taskActivities.size());

    //**resumeTask and check that both admin and john can get the task b
    //meaning that the assigned user has been set as it was before the suspend (ie null)
    loginAs(loginJohn, passwordJohn);
    getRuntimeAPI().resumeTask(taskUUID, false);

    taskActivities = getQueryRuntimeAPI().getTaskList(ActivityState.READY);
    assertEquals(1, taskActivities.size());

    loginAs(loginAdmin, passwordAdmin);
    taskActivities = getQueryRuntimeAPI().getTaskList(ActivityState.READY);
    assertEquals(1, taskActivities.size());


    //*** case 3 test ***
    //check that only the user having done the startTask can get the task.
    loginAs(loginAdmin, passwordAdmin);
    getRuntimeAPI().startTask(taskUUID, true);
    loginAs(loginAdmin, passwordAdmin);
    taskActivities = getQueryRuntimeAPI().getTaskList(ActivityState.EXECUTING);
    assertEquals(1, taskActivities.size());
    loginAs(loginJohn, passwordJohn);
    taskActivities = getQueryRuntimeAPI().getTaskList(ActivityState.EXECUTING);
    assertEquals(0, taskActivities.size());

    //go on until undeploying
    getRuntimeAPI().deleteAllProcessInstances(processUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }


  public void testDoneList() throws BonitaException {
    final URL xpdlUrl = getClass().getResource("doneList.xpdl");
    final ProcessDefinition process = getManagementAPI().deploy(getBusinessArchiveFromXpdl(xpdlUrl,
        AdminsRoleMapper.class));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    Collection<TaskInstance> todolist = null;
    Collection<TaskInstance> donelist = null;

    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    final String otherUser = "anotherUser";

    //all tasks are assign to instanceInitiator by default
    donelist = getQueryRuntimeAPI().getTaskList(ActivityState.FINISHED);
    assertNotNull(donelist);
    assertEquals(0, donelist.size());

    todolist = getQueryRuntimeAPI().getTaskList(ActivityState.READY);
    assertNotNull(todolist);
    assertEquals(1, todolist.size());

    //execute the first task and check it appears in the done list
    final ActivityInstanceUUID task1UUID = todolist.iterator().next().getUUID();
    getRuntimeAPI().startTask(task1UUID, true);
    getRuntimeAPI().finishTask(task1UUID, true);

    donelist = getQueryRuntimeAPI().getTaskList(ActivityState.FINISHED);
    assertNotNull(donelist);
    assertEquals(1, donelist.size());
    assertEquals(task1UUID, donelist.iterator().next().getUUID());

    //assign the second task to another user
    todolist = getQueryRuntimeAPI().getTaskList(ActivityState.READY);
    assertNotNull(todolist);
    assertEquals(1, todolist.size());
    final ActivityInstanceUUID task2UUID = todolist.iterator().next().getUUID();
    getRuntimeAPI().assignTask(task2UUID, otherUser);

    //check the other user has task2 in its todolist
    todolist = getQueryRuntimeAPI().getTaskList(otherUser, ActivityState.READY);
    assertNotNull(todolist);
    assertEquals(1, todolist.size());
    assertEquals(task2UUID, todolist.iterator().next().getUUID());

    //check the task is not anymore in todoList of the logged user
    todolist = getQueryRuntimeAPI().getTaskList(ActivityState.READY);
    assertNotNull(todolist);
    assertEquals(0, todolist.size());

    //execute the second task as 'another user"
    getRuntimeAPI().startTask(task2UUID, false);
    getRuntimeAPI().finishTask(task2UUID, false);

    //check the second task is  in the donelist of the other user
    donelist = getQueryRuntimeAPI().getTaskList(otherUser, ActivityState.FINISHED);
    assertNotNull(donelist);
    assertEquals(1, donelist.size());
    assertEquals(task2UUID, donelist.iterator().next().getUUID());

    //check the second task is not in the donelist of the current logged user. Only t1 is in donelist
    donelist = getQueryRuntimeAPI().getTaskList(ActivityState.FINISHED);
    assertNotNull(donelist);
    assertEquals(1, donelist.size());
    assertEquals(task1UUID, donelist.iterator().next().getUUID());

    //execute the third task and check it appears in the done list of the logged user
    todolist = getQueryRuntimeAPI().getTaskList(ActivityState.READY);
    assertNotNull(todolist);
    assertEquals(1, todolist.size());

    final ActivityInstanceUUID task3UUID = todolist.iterator().next().getUUID();
    getRuntimeAPI().startTask(task3UUID, true);
    getRuntimeAPI().finishTask(task3UUID, true);

    donelist = getQueryRuntimeAPI().getTaskList(ActivityState.FINISHED);
    assertNotNull(donelist);
    assertEquals(2, donelist.size());

    //check doneList of the logged user contains task1 and task3
    final Set<ActivityInstanceUUID> expected = buildSet(task1UUID, task3UUID);
    final Set<ActivityInstanceUUID> donelistUUIDs = new HashSet<ActivityInstanceUUID>();
    final Iterator<TaskInstance> it = donelist.iterator();
    while (it.hasNext()) {
      donelistUUIDs.add(it.next().getUUID());
    }
    assertEquals(expected, donelistUUIDs);

    //check doneList of the other user contains task2
    donelist = getQueryRuntimeAPI().getTaskList(otherUser, ActivityState.FINISHED);
    assertNotNull(donelist);
    assertEquals(1, donelist.size());
    assertEquals(task2UUID, donelist.iterator().next().getUUID());

    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  private Set<ActivityInstanceUUID> buildSet(final ActivityInstanceUUID... taskUUIDs) {
    final Set<ActivityInstanceUUID> s = new HashSet<ActivityInstanceUUID>();
    for (final ActivityInstanceUUID taskUUID : taskUUIDs) {
      s.add(taskUUID);
    }
    return s;
  }
  public void testGetTask() throws BonitaException {
    final URL xpdlUrl = getClass().getResource("usertest2_1.0.xpdl");
    final ProcessDefinition process = getManagementAPI().deploy(getBusinessArchiveFromXpdl(xpdlUrl));
    final ProcessDefinitionUUID processUUID = process.getUUID();
    final ActivityInstanceUUID newTaskUUID = IdFactory.getNewTaskUUID();
    ProcessInstanceUUID instanceUUID = null;
    try {
      instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);
      assertEquals(InstanceState.STARTED, getQueryRuntimeAPI().getProcessInstance(instanceUUID).getInstanceState());
      final Collection<TaskInstance> taskActivities = getQueryRuntimeAPI().getTaskList(ActivityState.READY);
      assertEquals(1, taskActivities.size());
      final ActivityInstanceUUID taskUUID = taskActivities.iterator().next().getUUID();

      //getTask for task within activity = a
      final TaskInstance taskActivity = getQueryRuntimeAPI().getTask(taskUUID);
      assertNotNull(taskActivity.getActivityName());
      assertEquals("a", taskActivity.getActivityName());
      assertNotNull(taskActivity.getCreatedDate());
    } catch (final ProcessNotFoundException be) {
      throw new RuntimeException("Missing resource!", be);
    }
    try {
      getQueryRuntimeAPI().getTask(newTaskUUID);
      fail("This task does not exist");
    } catch (final TaskNotFoundException e) {
      assertEquals(newTaskUUID, e.getActivityInstanceUUID());
    } 

    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testDeleteAllProcessesInstances() throws BonitaException {
    Collection<ProcessDefinitionUUID> nullDef = null;
    try {
      getRuntimeAPI().deleteAllProcessInstances(nullDef);
      fail("No Instance to delete");
    } catch (final IllegalArgumentException e) {
      // Test pass
    } 

    ProcessDefinition definition1 = ProcessBuilder.createProcess("hello", null)
    .addSystemTask("a")
    .addHumanTask("t", "john")
    .addTransition("a", "t")
    .addHuman("john")
    .done();

    ProcessDefinition definition2 = ProcessBuilder.createProcess("salut", null)
    .addSystemTask("a")
    .addSystemTask("b")
    .addHumanTask("t", "john")
    .addTransition("a", "t")
    .addTransition("t", "b")
    .addHuman("john")
    .done();

    ProcessDefinition definition3 = ProcessBuilder.createProcess("hallo", null)
    .addSystemTask("a")
    .addSystemTask("m")
    .addHumanTask("t", "john")
    .addTransition("a", "t")
    .addTransition("t", "m")
    .addHuman("john")
    .done();

    ProcessDefinition process1 = getManagementAPI().deploy(getBusinessArchive(definition1));
    ProcessDefinition process2 = getManagementAPI().deploy(getBusinessArchive(definition2));

    ProcessDefinitionUUID processUUID1 = process1.getUUID();
    ProcessDefinitionUUID processUUID2 = process2.getUUID();

    getRuntimeAPI().instantiateProcess(processUUID1);
    getRuntimeAPI().instantiateProcess(processUUID2);
    getRuntimeAPI().instantiateProcess(processUUID1);
    Set<ProcessInstance> processInstances = getQueryRuntimeAPI().getProcessInstances();
    assertEquals(3, processInstances.size());

    ProcessDefinition process3 = getManagementAPI().deploy(getBusinessArchive(definition3));
    ProcessDefinitionUUID processUUID3 = process3.getUUID();

    getRuntimeAPI().instantiateProcess(processUUID2);
    getRuntimeAPI().instantiateProcess(processUUID3);
    processInstances = getQueryRuntimeAPI().getProcessInstances();
    assertEquals(5, processInstances.size());

    Collection<ProcessDefinitionUUID> collection = new ArrayList<ProcessDefinitionUUID>();
    collection.add(processUUID1);
    collection.add(processUUID3);
    getRuntimeAPI().deleteAllProcessInstances(collection);

    processInstances = getQueryRuntimeAPI().getProcessInstances();
    assertEquals(2, processInstances.size());
    Iterator<ProcessInstance> iter = processInstances.iterator();
    assertEquals(processUUID2, iter.next().getProcessDefinitionUUID());
    assertEquals(processUUID2, iter.next().getProcessDefinitionUUID());

    getRuntimeAPI().instantiateProcess(processUUID1);
    processInstances = getQueryRuntimeAPI().getProcessInstances();
    assertEquals(3, processInstances.size());
    getRuntimeAPI().deleteAllProcessInstances(collection);

    processInstances = getQueryRuntimeAPI().getProcessInstances();
    assertEquals(2, processInstances.size());

    getManagementAPI().deleteAllProcesses();
  }

  public void testDeleteAllProcessInstances() throws BonitaException {
    ProcessDefinitionUUID nullDef = null;
    try {
      getRuntimeAPI().deleteAllProcessInstances(nullDef);
    } catch (final IllegalArgumentException e) {
      // Test pass
    } catch (NullPointerException e){
      //Ok REST
      StackTraceElement firstElement = e.getStackTrace()[0];
      assertTrue(firstElement.getClassName().equals("org.jboss.resteasy.client.ClientRequest"));
      assertTrue(firstElement.getMethodName().equals("toString"));
    }

    ProcessDefinition subProcess = getManagementAPI().deploy(getBusinessArchiveFromXpdl(getClass().getResource("deleteInstance-sub.xpdl")));
    ProcessDefinitionUUID subProcessUUID = subProcess.getUUID();

    final URL xpdlUrl = getClass().getResource("deleteInstance.xpdl");
    ProcessDefinition process = getManagementAPI().deploy(getBusinessArchiveFromXpdl(xpdlUrl));

    ProcessDefinitionUUID processUUID = process.getUUID();

    Collection<TaskInstance> todolist = null;
    Set<ProcessInstance> instances = null;
    ActivityInstanceUUID taskUUID = null;

    //check that both running and finished instances are deleted
    ProcessInstanceUUID instance1 = getRuntimeAPI().instantiateProcess(subProcessUUID);
    assertEquals(InstanceState.STARTED, getQueryRuntimeAPI().getProcessInstance(instance1).getInstanceState());
    todolist = getQueryRuntimeAPI().getTaskList(instance1, ActivityState.READY);
    assertNotNull(todolist);
    assertFalse(todolist.isEmpty());
    assertTrue(todolist.size() == 1);

    ProcessInstanceUUID instance2 = getRuntimeAPI().instantiateProcess(subProcessUUID);
    assertEquals(InstanceState.STARTED, getQueryRuntimeAPI().getProcessInstance(instance2).getInstanceState());
    todolist = getQueryRuntimeAPI().getTaskList(instance2, ActivityState.READY);
    assertNotNull(todolist);
    assertFalse(todolist.isEmpty());
    assertTrue(todolist.size() == 1);
    taskUUID = todolist.iterator().next().getUUID();
    getRuntimeAPI().startTask(taskUUID, true);
    getRuntimeAPI().finishTask(taskUUID, true);
    assertEquals(InstanceState.FINISHED, getQueryRuntimeAPI().getProcessInstance(instance2).getInstanceState());

    instances = getQueryRuntimeAPI().getProcessInstances(subProcessUUID);
    assertNotNull(instances);
    assertTrue(instances.size() == 2);

    getRuntimeAPI().deleteAllProcessInstances(subProcessUUID);

    instances = getQueryRuntimeAPI().getProcessInstances(subProcessUUID);
    assertNotNull(instances);
    assertTrue(instances.isEmpty());

    //check that if we try to delete instances of an unknown process, processNotFoundException is thrown
    final ProcessDefinitionUUID wrongProcessUUID = IdFactory.getNewProcessUUID();
    try {
      getRuntimeAPI().deleteAllProcessInstances(wrongProcessUUID);
      fail("An exception must be thrown when trying to delete instances of an unexisting process");
    } catch (final ProcessNotFoundException pnfe) {
      // ok
    }

    // Check UndeletableInstanceException is thrown when trying to delete process which have parentInstance

    instance1 = getRuntimeAPI().instantiateProcess(processUUID);
    final ProcessInstance processInstance = getQueryRuntimeAPI().getProcessInstance(instance1);
    final Set<ProcessInstanceUUID> instancesUUID = processInstance.getChildrenInstanceUUID();
    assertNotNull(instancesUUID);
    assertEquals(1, instancesUUID.size());
    final ProcessInstanceUUID childInstanceUUID = instancesUUID.iterator().next();
    getRuntimeAPI().instantiateProcess(processUUID);

    try {
      getRuntimeAPI().deleteAllProcessInstances(
          getQueryRuntimeAPI().getProcessInstance(childInstanceUUID).getProcessDefinitionUUID());
      fail("Cannot delete a process instance having a parent instance");
    } catch (final UndeletableInstanceException e) {
      // Ok
      instances = getQueryRuntimeAPI().getProcessInstances(processUUID);
      assertNotNull(instances);
      assertTrue(instances.size() == 2);
    } 

    getRuntimeAPI().deleteAllProcessInstances(processUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
    getManagementAPI().disable(subProcessUUID);
    getManagementAPI().deleteProcess(subProcessUUID);
  }

  public void testDeleteProcessInstance() throws BonitaException {
    // many cases to test :
    // 0) check IllegalArgumentException with null parameter
    // 1) delete a running instance : check instance not available in querier
    // 2) delete an archived instance : check available in querier, delete, check not available
    // 3) check instanceNotFound thrown if instance can't be found in repository neither in journal, neither in history
    // 4) check UndeletableInstanceException

    // case 0
    try {
      getRuntimeAPI().deleteProcessInstance(null);
      fail("IllegalArgumentException must be thrown");
    } catch (final IllegalArgumentException e) {
      // ok
    } catch (NullPointerException e){
      //Ok REST
      StackTraceElement firstElement = e.getStackTrace()[0];
      assertTrue(firstElement.getClassName().equals("org.jboss.resteasy.client.ClientRequest"));
      assertTrue(firstElement.getMethodName().equals("toString"));
    }

    ProcessDefinition subProcess = getManagementAPI().deploy(getBusinessArchiveFromXpdl(getClass().getResource("deleteInstance-sub.xpdl")));
    ProcessDefinitionUUID subProcessUUID = subProcess.getUUID();

    ProcessDefinition process = getManagementAPI().deploy(getBusinessArchiveFromXpdl(getClass().getResource("deleteInstance.xpdl")));
    ProcessDefinitionUUID processUUID = process.getUUID();

    ProcessInstanceUUID instance1 = null;
    Collection<TaskInstance> todolist = null;
    Set<ProcessInstance> instances = null;
    ActivityInstanceUUID taskUUID = null;

    // case 1
    instance1 = getRuntimeAPI().instantiateProcess(subProcessUUID);
    assertEquals(InstanceState.STARTED, getQueryRuntimeAPI().getProcessInstance(instance1).getInstanceState());
    todolist = getQueryRuntimeAPI().getTaskList(instance1, ActivityState.READY);
    assertNotNull(todolist);
    assertFalse(todolist.isEmpty());
    assertTrue(todolist.size() == 1);
    getRuntimeAPI().deleteProcessInstance(instance1);
    try {
      getQueryRuntimeAPI().getProcessInstance(instance1);
      fail("Instance is just deleted! We must a an exception here!");
    } catch (final InstanceNotFoundException infe) {
      // ok
    } 
    instances = getQueryRuntimeAPI().getProcessInstances(subProcessUUID);
    assertNotNull(instances);
    assertTrue(instances.isEmpty());

    // case 2
    instance1 = getRuntimeAPI().instantiateProcess(subProcessUUID);
    assertEquals(InstanceState.STARTED, getQueryRuntimeAPI().getProcessInstance(instance1).getInstanceState());
    todolist = getQueryRuntimeAPI().getTaskList(instance1, ActivityState.READY);
    assertNotNull(todolist);
    assertFalse(todolist.isEmpty());
    assertTrue(todolist.size() == 1);
    taskUUID = todolist.iterator().next().getUUID();
    getRuntimeAPI().startTask(taskUUID, true);
    getRuntimeAPI().finishTask(taskUUID, true);
    assertEquals(InstanceState.FINISHED, getQueryRuntimeAPI().getProcessInstance(instance1).getInstanceState());

    getRuntimeAPI().deleteProcessInstance(instance1);
    try {
      getQueryRuntimeAPI().getProcessInstance(instance1);
      fail("Instance is just deleted! We must a an exception here!");
    } catch (final InstanceNotFoundException infe) {
      // ok
    } 
    instances = getQueryRuntimeAPI().getProcessInstances(subProcessUUID);
    assertNotNull(instances);
    assertTrue(instances.isEmpty());

    // case 3
    try {
      getRuntimeAPI().deleteProcessInstance(instance1);
      fail("Unable to delete an instance which can't be found in journal neither in repository");
    } catch (final InstanceNotFoundException infe) {
      // ok
    } 

    getRuntimeAPI().deleteAllProcessInstances(subProcessUUID);

    // case 4 : Check UndeletableInstanceException is thrown when trying to delete process which have parentInstance

    instance1 = getRuntimeAPI().instantiateProcess(processUUID);
    final ProcessInstance processInstance = getQueryRuntimeAPI().getProcessInstance(instance1);
    final Set<ProcessInstanceUUID> instancesUUID = processInstance.getChildrenInstanceUUID();
    assertNotNull(instancesUUID);
    assertEquals(1, instancesUUID.size());
    final ProcessInstanceUUID childInstanceUUID = instancesUUID.iterator().next();
    try {
      getRuntimeAPI().deleteProcessInstance(childInstanceUUID);
      fail("Cannot delete a process instance having a parent instance");
    } catch (final UndeletableInstanceException e) {
      // Ok
    } 
    instances = getQueryRuntimeAPI().getProcessInstances(processUUID);
    assertNotNull(instances);
    assertTrue(instances.size() == 1);

    todolist = getQueryRuntimeAPI().getTaskList(ActivityState.READY);
    assertNotNull(todolist);
    assertTrue(todolist.size() == 1);
    taskUUID = todolist.iterator().next().getUUID();

    getRuntimeAPI().startTask(taskUUID, true);
    getRuntimeAPI().finishTask(taskUUID, true);

    getRuntimeAPI().deleteAllProcessInstances(processUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
    getManagementAPI().disable(subProcessUUID);
    getManagementAPI().deleteProcess(subProcessUUID);
  }

  /*
   *  Test cancelProcessInstance(ProcessInstanceUUID uuid) from the runtimeAPI.
   * Cancels a process instance which has a ready task. Check the ask is canceled.
   */
  public void testCancelProcessInstance() throws BonitaException {
    ProcessInstanceUUID instanceUUID = null;
    final URL xpdlUrl = getClass().getResource("usertest1_1.0.xpdl");
    final ProcessDefinition process = getManagementAPI().deploy(getBusinessArchiveFromXpdl(xpdlUrl));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    Collection<TaskInstance> activitiesToDo =
      getQueryRuntimeAPI().getTaskList(instanceUUID, ActivityState.READY);
    assertEquals(1, activitiesToDo.size());
    final TaskInstance taskActivity = activitiesToDo.iterator().next();
    assertEquals(ActivityState.READY, getQueryRuntimeAPI().getTask(taskActivity.getUUID()).getState());

    getRuntimeAPI().cancelProcessInstance(instanceUUID);

    activitiesToDo = getQueryRuntimeAPI().getTaskList(instanceUUID, ActivityState.READY);
    assertEquals(0, activitiesToDo.size());

    assertEquals(ActivityState.CANCELLED, getQueryRuntimeAPI().getTask(taskActivity.getUUID()).getState());

    final ProcessInstance processInstance = getQueryRuntimeAPI().getProcessInstance(instanceUUID);
    assertEquals(InstanceState.CANCELLED, processInstance.getInstanceState());

    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testCancelProcessInstances() throws BonitaException {
    String user = getLogin();
    ProcessDefinition process = ProcessBuilder.createProcess("p", "1.0")
    .addHuman(user)
    .addHumanTask("a", user)
    /*
    .addHumanTask("d", user)
    .addHumanTask("a", user)
    .addHumanTask("c", user)
    .addHumanTask("join", user)
    .addTransition("c_join", "c", "join")
    .addTransition("split_d", "split", "d")
    .addTransition("d_join", "d", "join")
    .addTransition("a_split", "a", "split")
    .addTransition("split_c", "split", "c")
     */
    .done();

    process = getManagementAPI().deploy(getBusinessArchive(process));
    ProcessDefinitionUUID processUUID = process.getUUID();

    ProcessInstanceUUID instanceUUID1 = getRuntimeAPI().instantiateProcess(processUUID);
    ProcessInstanceUUID instanceUUID2 = getRuntimeAPI().instantiateProcess(processUUID);
    ProcessInstanceUUID instanceUUID3 = getRuntimeAPI().instantiateProcess(processUUID);

    assertEquals(1, getQueryRuntimeAPI().getTaskList(instanceUUID1, ActivityState.READY).size());
    assertEquals(1, getQueryRuntimeAPI().getTaskList(instanceUUID2, ActivityState.READY).size());
    assertEquals(1, getQueryRuntimeAPI().getTaskList(instanceUUID3, ActivityState.READY).size());

    assertEquals(3, getQueryRuntimeAPI().getTaskList(ActivityState.READY).size());

    Collection<ProcessInstanceUUID> instanceUUIDs = new HashSet<ProcessInstanceUUID>();
    instanceUUIDs.add(instanceUUID1);
    instanceUUIDs.add(instanceUUID2);
    instanceUUIDs.add(instanceUUID3);

    getRuntimeAPI().cancelProcessInstances(instanceUUIDs);

    assertEquals(1, getQueryRuntimeAPI().getTaskList(instanceUUID1, ActivityState.CANCELLED).size());
    assertEquals(1, getQueryRuntimeAPI().getTaskList(instanceUUID2, ActivityState.CANCELLED).size());
    assertEquals(1, getQueryRuntimeAPI().getTaskList(instanceUUID3, ActivityState.CANCELLED).size());

    assertEquals(3, getQueryRuntimeAPI().getTaskList(ActivityState.CANCELLED).size());

    getManagementAPI().deleteAllProcesses();
  }

  public void testCancelProcessInstanceBadArgs() throws BonitaException {
    try {
      getRuntimeAPI().cancelProcessInstance(new ProcessInstanceUUID(UUID.randomUUID().toString()));
      fail(InstanceNotFoundException.class.getSimpleName() + " expected");
    } catch (final InstanceNotFoundException e) {
      // ok
    } 

    try {
      getRuntimeAPI().cancelProcessInstance(null);
      fail(IllegalArgumentException.class.getSimpleName() + " expected");
    } catch (final IllegalArgumentException e) {
      // ok
    } catch (NullPointerException e){
      //Ok REST
      StackTraceElement firstElement = e.getStackTrace()[0];
      assertTrue(firstElement.getClassName().equals("org.jboss.resteasy.client.ClientRequest"));
      assertTrue(firstElement.getMethodName().equals("toString"));
    }
  }

  public void testCancelWithSubflow() throws BonitaException {
    // case to test :
    // cancel a process with running subflow activities

    final URL xpdlUrl = getClass().getResource("deleteInstance.xpdl");

    ProcessDefinition subProcess = getManagementAPI().deploy(getBusinessArchiveFromXpdl(getClass().getResource("deleteInstance-sub.xpdl")));
    ProcessDefinitionUUID subProcessUUID = subProcess.getUUID();

    final ProcessDefinition process = getManagementAPI().deploy(getBusinessArchiveFromXpdl(xpdlUrl));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    final ProcessInstanceUUID instance1 = getRuntimeAPI().instantiateProcess(processUUID);
    final ProcessInstance processInstance = getQueryRuntimeAPI().getProcessInstance(instance1);
    final Set<ProcessInstanceUUID> instancesUUID = processInstance.getChildrenInstanceUUID();
    assertNotNull(instancesUUID);
    assertEquals(1, instancesUUID.size());
    final ProcessInstanceUUID childInstanceUUID = instancesUUID.iterator().next();

    Collection<TaskInstance> todolist = getQueryRuntimeAPI().getTaskList(childInstanceUUID, ActivityState.READY);
    assertNotNull(todolist);
    assertTrue(todolist.size() == 1);

    getRuntimeAPI().cancelProcessInstance(instance1);

    final Set<ProcessInstance> instances = getQueryRuntimeAPI().getProcessInstances(processUUID);
    assertNotNull(instances);
    assertEquals(1, instances.size());

    assertEquals(InstanceState.CANCELLED, instances.iterator().next().getInstanceState());

    todolist = getQueryRuntimeAPI().getTaskList(childInstanceUUID, ActivityState.READY);
    assertNotNull(todolist);
    assertEquals(0, todolist.size());

    todolist = getQueryRuntimeAPI().getTaskList(childInstanceUUID, ActivityState.CANCELLED);
    assertNotNull(todolist);
    assertEquals(1, todolist.size());

    getRuntimeAPI().deleteAllProcessInstances(processUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
    getManagementAPI().disable(subProcessUUID);
    getManagementAPI().deleteProcess(subProcessUUID);
  }

  // Test cancel with multiInstantiation.
  public void testCancelMultiInstantiation() throws BonitaException {
    final URL xpdlUrl = getClass().getResource("multiInstantiation.xpdl");
    final ProcessDefinition process = getManagementAPI().deploy(getBusinessArchiveFromXpdl(xpdlUrl,
        TestOneJoinActivityInstantiator.class));
    final ProcessDefinitionUUID processUUID = process.getUUID();
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    final Set<ActivityInstance> actIntances = getQueryRuntimeAPI().getActivityInstances(instanceUUID, "r1");
    assertEquals(2, actIntances.size());

    final Set<Object> varValues = new HashSet<Object>();

    // check two instance of activity r1 have been created
    for (final ActivityInstance activityInstance : actIntances) {
      final Map<String, Object> variables = activityInstance.getLastKnownVariableValues();
      assertTrue(variables.containsKey("testVar"));
      varValues.add(variables.get("testVar"));
    }

    final Set<Object> expectedValues = new HashSet<Object>();
    expectedValues.add("val1");
    expectedValues.add("val2");

    assertEquals(expectedValues, varValues);

    // cancel instance
    getRuntimeAPI().cancelProcessInstance(instanceUUID);

    // check the two tasks have been cancelled.
    for (final ActivityInstance activityInstance : actIntances) {
      assertEquals(ActivityState.CANCELLED,
          getQueryRuntimeAPI()
          .getActivityInstance(activityInstance.getUUID())
          .getState());
    }

    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testCancelSubflowProcessInstance() throws BonitaException {
    // case to test :
    // cancel a child process

    final URL xpdlUrl = getClass().getResource("deleteInstance.xpdl");

    ProcessDefinition subProcess = getManagementAPI().deploy(getBusinessArchiveFromXpdl(getClass().getResource("deleteInstance-sub.xpdl")));
    ProcessDefinitionUUID subProcessUUID = subProcess.getUUID();

    // Check UncancellableInstanceException is thrown when trying to cancel process which have parentInstance
    final ProcessDefinition process = getManagementAPI().deploy(getBusinessArchiveFromXpdl(xpdlUrl));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    final ProcessInstanceUUID instance1 = getRuntimeAPI().instantiateProcess(processUUID);
    final ProcessInstance processInstance = getQueryRuntimeAPI().getProcessInstance(instance1);
    final Set<ProcessInstanceUUID> instancesUUID = processInstance.getChildrenInstanceUUID();
    assertNotNull(instancesUUID);
    assertEquals(1, instancesUUID.size());
    final ProcessInstanceUUID childInstanceUUID = instancesUUID.iterator().next();
    try {
      getRuntimeAPI().cancelProcessInstance(childInstanceUUID);
      fail("Cannot cancel a process instance having a parent instance");
    } catch (final UncancellableInstanceException e) {
      // Ok
    } 
    final Set<ProcessInstance> instances = getQueryRuntimeAPI().getProcessInstances(processUUID);
    assertNotNull(instances);
    assertTrue(instances.size() == 1);

    final Collection<TaskInstance> todolist = getQueryRuntimeAPI().getTaskList(ActivityState.READY);
    assertNotNull(todolist);
    assertTrue(todolist.size() == 1);
    final ActivityInstanceUUID taskUUID = todolist.iterator().next().getUUID();

    getRuntimeAPI().startTask(taskUUID, true);
    getRuntimeAPI().finishTask(taskUUID, true);

    getRuntimeAPI().deleteAllProcessInstances(processUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
    getManagementAPI().disable(subProcessUUID);
    getManagementAPI().deleteProcess(subProcessUUID);
  }

  public void testCancelFinishedProcessInstance() throws BonitaException {
    ProcessDefinition subProcess = getManagementAPI().deploy(getBusinessArchiveFromXpdl(getClass().getResource("deleteInstance-sub.xpdl")));
    ProcessDefinitionUUID subProcessUUID = subProcess.getUUID();


    final ProcessInstanceUUID instance1 = getRuntimeAPI().instantiateProcess(subProcessUUID);
    assertEquals(InstanceState.STARTED, getQueryRuntimeAPI().getProcessInstance(instance1).getInstanceState());
    final Collection<TaskInstance>  todolist = getQueryRuntimeAPI().getTaskList(instance1, ActivityState.READY);
    assertNotNull(todolist);
    assertFalse(todolist.isEmpty());
    assertTrue(todolist.size() == 1);
    final ActivityInstanceUUID taskUUID = todolist.iterator().next().getUUID();
    getRuntimeAPI().startTask(taskUUID, true);
    getRuntimeAPI().finishTask(taskUUID, true);
    assertEquals(InstanceState.FINISHED, getQueryRuntimeAPI().getProcessInstance(instance1).getInstanceState());

    try {
      getRuntimeAPI().cancelProcessInstance(instance1);
      fail("Instance is finished! We must a an exception here!");
    } catch (final InstanceNotFoundException e) {
      // ok
    } 

    getRuntimeAPI().deleteAllProcessInstances(subProcessUUID);
    getManagementAPI().disable(subProcessUUID);
    getManagementAPI().deleteProcess(subProcessUUID);
  }  



  public void testAddProcessMetaData() throws BonitaException {
    final URL xpdlUrl = getClass().getResource("variables.xpdl");
    final ProcessDefinition process = getManagementAPI().deploy(getBusinessArchiveFromXpdl(xpdlUrl));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    getRuntimeAPI().addProcessMetaData(processUUID, "one", "A");
    String value = getQueryDefinitionAPI().getProcessMetaData(processUUID, "one");
    assertEquals("A", value);

    getRuntimeAPI().addProcessMetaData(processUUID, "one", "B");
    value = getQueryDefinitionAPI().getProcessMetaData(processUUID, "one");
    assertEquals("B", value);

    getManagementAPI().deleteProcess(processUUID);
  }

  public void testAddProcessMetaDataWithSpecialCaracters() throws BonitaException {
    final URL xpdlUrl = getClass().getResource("variables.xpdl");
    final ProcessDefinition process = getManagementAPI().deploy(getBusinessArchiveFromXpdl(xpdlUrl));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    getRuntimeAPI().addProcessMetaData(processUUID, "one", "http://localhost:8080/bonita");
    String value = getQueryDefinitionAPI().getProcessMetaData(processUUID, "one");
    assertEquals("http://localhost:8080/bonita", value);

    getRuntimeAPI().addProcessMetaData(processUUID, "one", "{k}[]$");
    value = getQueryDefinitionAPI().getProcessMetaData(processUUID, "one");
    assertEquals("{k}[]$", value);

    getRuntimeAPI().deleteProcessMetaData(processUUID, "one");
    value = getQueryDefinitionAPI().getProcessMetaData(processUUID, "one");
    assertNull(value);

    getManagementAPI().deleteProcess(processUUID);
  }

  public void testEvaluateSimpleGroovyExpression() throws BonitaException {
    final ProcessDefinition process =
      getManagementAPI().deploy(getBusinessArchive(getProcess()));
    final ProcessDefinitionUUID processUUID = process.getUUID();
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);
    String result = (String) getRuntimeAPI().evaluateGroovyExpression("one + three equals ${yksi + kolme}", instanceUUID, true);
    assertEquals("one + three equals 4", result);
    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testEvaluateGroovyExpressionInProcessAndActivity() throws BonitaException {
    final ProcessDefinition process =
      getManagementAPI().deploy(getBusinessArchive(getProcess()));
    final ProcessDefinitionUUID processUUID = process.getUUID();
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);
    String result = (String) getRuntimeAPI().evaluateGroovyExpression("one + three equals ${yksi + kolme}", instanceUUID, true);
    assertEquals("one + three equals 4", result);

    Set<ActivityInstance> actIntances =
      getQueryRuntimeAPI().getActivityInstances(instanceUUID, "go");
    ActivityInstance ai = actIntances.iterator().next();

    Integer res = (Integer) getRuntimeAPI().evaluateGroovyExpression("${viisi + kolme}", ai.getUUID(), true, true);
    assertEquals(Integer.valueOf(8), res);

    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testEvaluateWrongGroovyExpression() throws BonitaException {
    final ProcessDefinition process =
      getManagementAPI().deploy(getBusinessArchive(getProcess()));
    final ProcessDefinitionUUID processUUID = process.getUUID();
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    Integer res = (Integer) getRuntimeAPI().evaluateGroovyExpression("${yksi + kolme}", instanceUUID, true);
    assertEquals(Integer.valueOf(4), res);

    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testEvaluateGroovyExpressions() throws BonitaException {
    final ProcessDefinition process =
      getManagementAPI().deploy(getBusinessArchive(getProcess()));
    final ProcessDefinitionUUID processUUID = process.getUUID();
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    String result = (String) getRuntimeAPI().evaluateGroovyExpression("Hello, ${yksi + kolme} = ${kolme + yksi}.", instanceUUID, true);
    assertEquals("Hello, 4 = 4.", result);

    Map<String, Object> context = new HashMap<String, Object>();
    context.put("myBool", true);
    assertTrue((Boolean)getRuntimeAPI().evaluateGroovyExpression("${myBool}", instanceUUID, context, false));

    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testEvaluateProcessGroovyExpressions() throws BonitaException {
    ProcessDefinition process = ProcessBuilder.createProcess("p", "1.0")
    .addStringData("str", "initial")
    .done();
    process = getManagementAPI().deploy(getBusinessArchive(process));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    String result = (String) getRuntimeAPI().evaluateGroovyExpression("${str}", processUUID);
    assertEquals("initial", result);

    Map<String, Object> context = new HashMap<String, Object>();
    context.put("newVar", "diff");
    assertFalse((Boolean)getRuntimeAPI().evaluateGroovyExpression("${newVar == str}", processUUID, context));
    context.put("newVar", "initial");
    assertTrue((Boolean)getRuntimeAPI().evaluateGroovyExpression("${newVar == str}", processUUID, context));

    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testEvaluateGroovyExpressionWithBrackets() throws BonitaException {
    final ProcessDefinition process =
      getManagementAPI().deploy(getBusinessArchive(getProcess()));
    final ProcessDefinitionUUID processUUID = process.getUUID();
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    String result = (String) getRuntimeAPI().evaluateGroovyExpression("Where is ${men = ['James', 'John', 'Brian']; men[1]}?", instanceUUID, true);
    assertEquals("Where is John?", result);

    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testEvaluateTwoGroovyExpressions() throws BonitaException {
    final ProcessDefinition process =
      getManagementAPI().deploy(getBusinessArchive(getProcess()));
    final ProcessDefinitionUUID processUUID = process.getUUID();
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    String expression = "${men = ['James', 'John', 'Brian']; men[1]}? He is in the ${places=['kitchen', 'bathroom', 'garden']; places[1]}.";
    String result = (String) getRuntimeAPI().evaluateGroovyExpression(expression, instanceUUID, true);
    assertEquals("John? He is in the bathroom.", result);

    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testEvaluateGroovyExpressionWithBonitaVariables() throws BonitaException {
    final ProcessDefinition process =
      getManagementAPI().deploy(getBusinessArchive(getProcess()));
    final ProcessDefinitionUUID processUUID = process.getUUID();
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    ProcessDefinition processDef = (ProcessDefinition) getRuntimeAPI().evaluateGroovyExpression("${processDefinition}", instanceUUID, true);
    assertNotNull(processDef);
    ProcessInstance processInstance = (ProcessInstance) getRuntimeAPI().evaluateGroovyExpression("${processInstance}", instanceUUID, true);
    assertNotNull(processInstance);
    ActivityInstance actInstance = (ActivityInstance) getRuntimeAPI().evaluateGroovyExpression("${activityInstance}", instanceUUID, true);
    assertNull(actInstance);

    processDef = (ProcessDefinition) getRuntimeAPI().evaluateGroovyExpression("${processDefinition}", processUUID);
    assertNotNull(processDef);
    processInstance = (ProcessInstance) getRuntimeAPI().evaluateGroovyExpression("${processInstance}", processUUID);
    assertNull(processInstance);
    actInstance = (ActivityInstance) getRuntimeAPI().evaluateGroovyExpression("${activityInstance}", processUUID);
    assertNull(actInstance);

    Set<TaskInstance> tasks = getQueryRuntimeAPI().getTasks(instanceUUID);
    TaskInstance task = tasks.iterator().next();
    ActivityInstanceUUID activityUUID = task.getUUID();
    processDef = (ProcessDefinition) getRuntimeAPI().evaluateGroovyExpression("${processDefinition}", activityUUID, true, true);
    assertNotNull(processDef);
    processInstance = (ProcessInstance) getRuntimeAPI().evaluateGroovyExpression("${processInstance}", activityUUID, true, true);
    assertNotNull(processInstance);
    actInstance = (ActivityInstance) getRuntimeAPI().evaluateGroovyExpression("${activityInstance}", activityUUID, true, true);
    assertNotNull(actInstance);
    String user = (String) getRuntimeAPI().evaluateGroovyExpression("${loggedUser}", activityUUID, true, true);
    assertEquals("admin", user);

    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  @SuppressWarnings("unchecked")
  public void testEvaluateGroovyExpressionWithAPIAcessor() throws Exception {
    final ProcessDefinition process = getManagementAPI().deploy(getBusinessArchive(getProcess()));
    final String script = "${" + BonitaConstants.API_ACCESSOR + ".getIdentityAPI().getAllUsers();}";
    List<User> users = (List<User>) getRuntimeAPI().evaluateGroovyExpression(script, process.getUUID());
    assertEquals(4, users.size());
    getManagementAPI().deleteProcess(process.getUUID());
  }

  @SuppressWarnings("unchecked")
  public void testEvaluateGroovyExpressionWithAPIAcessorOnAnActivity() throws Exception {
    final String script = "${" + BonitaConstants.API_ACCESSOR + ".getIdentityAPI().getAllUsers();}";
    ProcessDefinition definition = ProcessBuilder.createProcess("api", "2.1")
    .addHuman(getLogin())
    .addHumanTask("step1", getLogin())
    .addStringDataFromScript("yksi", script)
    .done();

    final ProcessDefinition process = getManagementAPI().deploy(getBusinessArchive(definition));
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(process.getUUID());
    Set<ActivityInstance> activities = getQueryRuntimeAPI().getActivityInstances(instanceUUID);
    assertEquals(1, activities.size());
    ActivityInstanceUUID activityUUID = activities.iterator().next().getUUID();
    List<User> users = (List<User>) getQueryRuntimeAPI().getVariable(activityUUID, "yksi");
    assertEquals(4, users.size());

    users = (List<User>) getRuntimeAPI().evaluateGroovyExpression(script, activityUUID, false, true);
    assertEquals(4, users.size());

    getManagementAPI().deleteProcess(process.getUUID());
  }

  public void testEvaluateGroovyExpressionWithInstanceInitiatorOnAnActivity() throws Exception {
    final String script = "${def a = " + BonitaConstants.PROCESS_INSTANCE_INITIATOR + ";return a;}";
    ProcessDefinition definition = ProcessBuilder.createProcess("user", "2.1")
    .addHuman(getLogin())
    .addHumanTask("step1", getLogin())
    .done();

    final ProcessDefinition process = getManagementAPI().deploy(getBusinessArchive(definition));
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(process.getUUID());
    Set<ActivityInstance> activities = getQueryRuntimeAPI().getActivityInstances(instanceUUID);
    assertEquals(1, activities.size());
    ActivityInstanceUUID activityUUID = activities.iterator().next().getUUID();

    String user = (String) getRuntimeAPI().evaluateGroovyExpression(script, activityUUID, false, true);
    assertEquals(getLogin(), user);

    getManagementAPI().deleteProcess(process.getUUID());
  }

  private ProcessDefinition getAWPocess() throws Exception {
    Set<String> enumerationValues = new HashSet<String>();
    enumerationValues.add("Word");
    enumerationValues.add("Excel");
    enumerationValues.add("MailReader");
    enumerationValues.add("WebBrowser");

    ProcessDefinition process = ProcessBuilder.createProcess("approval", "1.0")
    .addEnumData("Applications", enumerationValues, "Word")
    .addHuman(getLogin())
    .addSystemTask("BonitaStart")
    .addSystemTask("BonitaEnd")
    .addHumanTask("Request", getLogin())
    .addHumanTask("Reject", getLogin())//"org.ow2.bonita.example.aw.hook.Reject"
    .addHumanTask("Accept", getLogin())//"org.ow2.bonita.example.aw.hook.Accept"
    .addHumanTask("Approval", getLogin())
    .addBooleanData("isGranted", false)
    .addTransition("Request", "Approval")
    .addTransition("Reject", "BonitaEnd")
    .addTransition("Accept", "BonitaEnd")
    .addTransition("Approval", "Reject")
    .addCondition("isGranted.compareTo(Boolean.valueOf('false')) == 0")
    .addTransition("Approval", "Accept")
    .addCondition("isGranted.compareTo(Boolean.valueOf('true')) == 0")
    .addTransition("BonitaStart", "Request")
    .done();
    return process;
  }

  public void testEvaluateInitialExpressionOnTerminatedTask() throws Exception {
    ProcessDefinition process = getAWPocess();
    BusinessArchive businessArchive = getBusinessArchive(process);
    process = getManagementAPI().deploy(businessArchive);
    ProcessInstanceUUID processInstanceUUID = getRuntimeAPI().instantiateProcess(process.getUUID());

    Collection<TaskInstance> tasks = getQueryRuntimeAPI().getTaskList(processInstanceUUID, ActivityState.READY);
    assertEquals(1, tasks.size());
    ActivityInstanceUUID uuid = tasks.iterator().next().getUUID();

    getRuntimeAPI().setVariable(uuid, "Applications", "Excel");
    getRuntimeAPI().executeTask(uuid, true);

    tasks = getQueryRuntimeAPI().getTaskList(processInstanceUUID, ActivityState.READY);
    ActivityInstanceUUID newActivityUUID = tasks.iterator().next().getUUID();
    getRuntimeAPI().setVariable(newActivityUUID, "Applications", "MailReader");
    Object result = getRuntimeAPI().evaluateGroovyExpression("${Applications}", newActivityUUID, true, false);
    assertEquals("MailReader", result.toString());
    getRuntimeAPI().executeTask(newActivityUUID, true);

    result = getRuntimeAPI().evaluateGroovyExpression("${Applications}", uuid, true, false);
    assertEquals("Excel", result.toString());

    getRuntimeAPI().deleteAllProcessInstances(process.getUUID());
    getManagementAPI().disable(process.getUUID());
    getManagementAPI().deleteProcess(process.getUUID());
  }

  public void testEvaluateInitialValueExpressionWithInstance() throws Exception {
    ProcessDefinition process = getAWPocess();
    BusinessArchive businessArchive = getBusinessArchive(process);
    process = getManagementAPI().deploy(businessArchive);
    ProcessInstanceUUID processInstanceUUID = getRuntimeAPI().instantiateProcess(process.getUUID());

    Collection<TaskInstance> tasks = getQueryRuntimeAPI().getTaskList(processInstanceUUID, ActivityState.READY);
    ActivityInstanceUUID uuid = null;
    for (TaskInstance activityInstance : tasks) {
      uuid = activityInstance.getUUID();
    }
    assertNotNull(uuid);
    getRuntimeAPI().setVariable(uuid, "Applications", "Excel");
    getRuntimeAPI().executeTask(uuid, true);

    Object result = getRuntimeAPI().evaluateGroovyExpression("${Applications}", processInstanceUUID, new HashMap<String, Object>(), true, false);
    assertEquals("Word", result.toString());

    getRuntimeAPI().deleteAllProcessInstances(process.getUUID());
    getManagementAPI().disable(process.getUUID());
    getManagementAPI().deleteProcess(process.getUUID());
  }

  public void testEvaluateCurrentValueExpressionWithTask() throws Exception {

    ProcessDefinition process = getAWPocess();
    BusinessArchive businessArchive = getBusinessArchive(process);
    process = getManagementAPI().deploy(businessArchive);
    ProcessInstanceUUID processInstanceUUID = getRuntimeAPI().instantiateProcess(process.getUUID());

    Collection<TaskInstance> tasks = getQueryRuntimeAPI().getTaskList(processInstanceUUID, ActivityState.READY);
    ActivityInstanceUUID uuid = null;
    for (TaskInstance activityInstance : tasks) {
      uuid = activityInstance.getUUID();
    }
    assertNotNull(uuid);
    getRuntimeAPI().executeTask(uuid, true);
    getRuntimeAPI().setVariable(uuid, "Applications", "Excel");
    Object result = getRuntimeAPI().evaluateGroovyExpression("${Applications}", uuid, new HashMap<String, Object>(), false, false);
    assertEquals("Excel", result.toString());

    getRuntimeAPI().deleteAllProcessInstances(process.getUUID());
    getManagementAPI().disable(process.getUUID());
    getManagementAPI().deleteProcess(process.getUUID());
  }

  public void testEvaluateInitialExpressionOnTerminatedInstance() throws Exception {

    ProcessDefinition simpleProcess = ProcessBuilder.createProcess("simple_process", "1.0")
    .addHuman(getLogin())
    .addIntegerData("stringData1")
    .addHumanTask("task", getLogin())
    .addStringData("stringData2")
    .done();

    BusinessArchive businessArchive = BusinessArchiveFactory.getBusinessArchive(simpleProcess);
    simpleProcess = getManagementAPI().deploy(businessArchive);

    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(simpleProcess.getUUID());
    try {
      Collection<TaskInstance> tasks = getQueryRuntimeAPI().getTaskList(instanceUUID, ActivityState.READY);
      ActivityInstanceUUID uuid = null;
      for (TaskInstance activityInstance : tasks) {
        uuid = activityInstance.getUUID();
      }

      assertNotNull(uuid);
      getRuntimeAPI().setVariable(uuid, "stringData1", "test1");
      getRuntimeAPI().setVariable(uuid, "stringData2", "test2");
      getRuntimeAPI().executeTask(uuid, true);

      Object result = getRuntimeAPI().evaluateGroovyExpression("${stringData1}", instanceUUID, new HashMap<String, Object>(), false, false);
      assertEquals("test1", result.toString());

      result = getRuntimeAPI().evaluateGroovyExpression("${stringData2}", uuid, new HashMap<String, Object>(), false, false);
      assertEquals("test2", result.toString());
    } finally {
      getRuntimeAPI().deleteProcessInstance(instanceUUID);
      getManagementAPI().deleteProcess(simpleProcess.getUUID());
    }
  }

  public void testSetActivityInstancePriority() throws Exception {

    ProcessDefinition process = ProcessBuilder.createProcess("priority_process", "1.0")
    .addHuman(getLogin())
    .addHumanTask("task1", getLogin())
    .addActivityPriority(2)
    .addHumanTask("task2", getLogin())
    .addTransition("task1", "task2")
    .done();

    BusinessArchive businessArchive = getBusinessArchive(process);
    process = getManagementAPI().deploy(businessArchive);
    ProcessInstanceUUID processInstanceUUID = getRuntimeAPI().instantiateProcess(process.getUUID());

    ActivityInstanceUUID activityInstanceUUID = getQueryRuntimeAPI().getOneTask(processInstanceUUID, ActivityState.READY);
    ActivityInstance activityInstance = getQueryRuntimeAPI().getActivityInstance(activityInstanceUUID);
    assertEquals("activity priority inherited from definition", 2, activityInstance.getPriority());

    getRuntimeAPI().setActivityInstancePriority(activityInstanceUUID, 1);
    activityInstance = getQueryRuntimeAPI().getActivityInstance(activityInstanceUUID);
    assertEquals("activity priority after setActivityInstancePriority", 1, activityInstance.getPriority());

    getRuntimeAPI().executeTask(activityInstanceUUID, true);
    try {
      getRuntimeAPI().setActivityInstancePriority(activityInstanceUUID, 0);
      fail("Priority shouldn't be editable on terminated activities");
    } catch (ActivityNotFoundException e) {
      //this exception is expected
    }  
    activityInstance = getQueryRuntimeAPI().getActivityInstance(activityInstanceUUID);
    assertEquals("activity priority after task execution", 1, activityInstance.getPriority());

    ActivityInstanceUUID activityInstanceUUID2 = getQueryRuntimeAPI().getOneTask(processInstanceUUID, ActivityState.READY);
    getRuntimeAPI().executeTask(activityInstanceUUID2, true);
    activityInstance = getQueryRuntimeAPI().getActivityInstance(activityInstanceUUID);
    assertEquals("activity priority after process instance end", 1, activityInstance.getPriority());

    getRuntimeAPI().deleteProcessInstance(processInstanceUUID);
    getManagementAPI().deleteProcess(process.getUUID());
  }

  private ProcessDefinition getProcess() {
    return ProcessBuilder.createProcess("Groovy", "1.0")
    .addIntegerData("yksi", 1)
    .addIntegerData("kolme", 3)
    .addIntegerData("r", 0)
    .addGroup("Customer")
    .addGroupResolver(InstanceInitiator.class.getName())
    .addSystemTask("start")
    .addHumanTask("go", "Customer")
    .addIntegerData("viisi", 5)
    .addSystemTask("end")
    .addJoinType(JoinType.XOR)
    .addTransition("start_go", "start", "go")
    .addTransition("go_end", "go", "end")
    .done();
  }

  public void testSkipTheLastTaskOfTheProcess() throws Exception {
    ProcessDefinition process = ProcessBuilder.createProcess("lasttask", "1.0")
    .addHuman(getLogin())
    .addHumanTask("step1", getLogin())
    .done();

    process = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process));
    ProcessDefinitionUUID processUUID = process.getUUID();
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    Collection<LightTaskInstance> tasks = getQueryRuntimeAPI().getLightTaskList(instanceUUID, ActivityState.READY);
    assertEquals(1, tasks.size());    
    LightTaskInstance task = tasks.iterator().next();
    assertEquals("step1", task.getActivityName());    

    tasks = getQueryRuntimeAPI().getLightTaskList(instanceUUID, ActivityState.SKIPPED);
    assertEquals(0, tasks.size());

    getRuntimeAPI().skipTask(task.getUUID(), null);

    tasks = getQueryRuntimeAPI().getLightTaskList(instanceUUID, ActivityState.SKIPPED);
    assertEquals(1, tasks.size());    
    task = tasks.iterator().next();
    assertEquals("step1", task.getActivityName());

    tasks = getQueryRuntimeAPI().getLightTaskList(instanceUUID, ActivityState.READY);
    assertEquals(0, tasks.size());

    ProcessInstance instance = getQueryRuntimeAPI().getProcessInstance(instanceUUID);
    assertEquals(InstanceState.FINISHED, instance.getInstanceState());

    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testSkipTask() throws Exception{
    ProcessDefinition process = ProcessBuilder.createProcess("process", "1.0")
    .addHuman(getLogin())
    .addHumanTask("step1", getLogin())
    .addHumanTask("step2", getLogin())
    .addTransition("step1", "step2")
    .done();

    process = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process));
    ProcessDefinitionUUID processUUID = process.getUUID();

    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    Collection<LightTaskInstance> tasks = getQueryRuntimeAPI().getLightTaskList(instanceUUID, ActivityState.READY);
    assertEquals(1, tasks.size());  	
    LightTaskInstance task = tasks.iterator().next();
    assertEquals("step1", task.getActivityName());  	

    tasks = getQueryRuntimeAPI().getLightTaskList(instanceUUID, ActivityState.SKIPPED);
    assertEquals(0, tasks.size());

    getRuntimeAPI().skipTask(task.getUUID(), null);

    tasks = getQueryRuntimeAPI().getLightTaskList(instanceUUID, ActivityState.SKIPPED);
    assertEquals(1, tasks.size());  	
    task = tasks.iterator().next();
    assertEquals("step1", task.getActivityName());

    tasks = getQueryRuntimeAPI().getLightTaskList(instanceUUID, ActivityState.READY);
    assertEquals(1, tasks.size());  	
    task = tasks.iterator().next();
    assertEquals("step2", task.getActivityName());   	

    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testSkipMultipleInstanceTask() throws Exception {
    ProcessDefinition definition =
      ProcessBuilder.createProcess("multiple", "1.0")
      .addHuman(getLogin())
      .addHumanTask("multi", getLogin())
      .addMultipleActivitiesInstantiator(NoContextMulitpleActivitiesInstantiator.class.getName())
      .addInputParameter("number", 10)
      .addMultipleActivitiesJoinChecker(FixedNumberJoinChecker.class.getName())
      .addInputParameter("activityNumber", 3)
      .addHumanTask("step2", getLogin())
      .addTransition("multi", "step2")
      .done();

    definition = getManagementAPI().deploy(getBusinessArchive(definition, null,
        NoContextMulitpleActivitiesInstantiator.class, FixedNumberJoinChecker.class));
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(definition.getUUID());
    Collection<LightTaskInstance> tasks = getQueryRuntimeAPI().getLightTaskList(getLogin(), ActivityState.SKIPPED);
    assertEquals(0, tasks.size());

    tasks = getQueryRuntimeAPI().getLightTaskList(getLogin(), ActivityState.READY);
    assertEquals(10, tasks.size());
    Iterator<LightTaskInstance> iterator = tasks.iterator();

    LightTaskInstance task = iterator.next();
    getRuntimeAPI().skipTask(task.getUUID(), null);

    tasks = getQueryRuntimeAPI().getLightTaskList(getLogin(), ActivityState.SKIPPED);
    assertEquals(10, tasks.size());

    tasks = getQueryRuntimeAPI().getLightTaskList(getLogin(), ActivityState.READY);
    assertEquals(1, tasks.size());
    assertEquals("step2", tasks.iterator().next().getActivityName());

    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().deleteProcess(definition.getUUID());
  }

  public void testSkipSecondInstanceOfMultipleInstanceTask() throws Exception {
    ProcessDefinition definition =
      ProcessBuilder.createProcess("multiple", "1.0")
      .addHuman(getLogin())
      .addHumanTask("multi", getLogin())
      .addMultipleActivitiesInstantiator(NoContextMulitpleActivitiesInstantiator.class.getName())
      .addInputParameter("number", 10)
      .addMultipleActivitiesJoinChecker(FixedNumberJoinChecker.class.getName())
      .addInputParameter("activityNumber", 3)
      .addHumanTask("task2", getLogin())
      .addTransition("multi", "task2")
      .done();

    definition = getManagementAPI().deploy(getBusinessArchive(definition, null,
        NoContextMulitpleActivitiesInstantiator.class, FixedNumberJoinChecker.class));
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(definition.getUUID());

    Collection<LightTaskInstance> tasks = getQueryRuntimeAPI().getLightTaskList(getLogin(), ActivityState.SKIPPED);
    assertEquals(0, tasks.size());    
    tasks = getQueryRuntimeAPI().getLightTaskList(getLogin(), ActivityState.READY);
    assertEquals(10, tasks.size());

    Iterator<LightTaskInstance> iterator = tasks.iterator();

    LightTaskInstance task = iterator.next();
    getRuntimeAPI().executeTask(task.getUUID(), false);
    task = iterator.next();    
    getRuntimeAPI().skipTask(task.getUUID(), null);


    tasks = getQueryRuntimeAPI().getLightTaskList(getLogin(), ActivityState.SKIPPED);
    assertEquals(9, tasks.size());    

    tasks = getQueryRuntimeAPI().getLightTaskList(getLogin(), ActivityState.READY);
    assertEquals(1, tasks.size());
    assertEquals("task2", tasks.iterator().next().getActivityName());

    tasks = getQueryRuntimeAPI().getLightTaskList(getLogin(), ActivityState.FINISHED);
    assertEquals(1, tasks.size());
    assertEquals("multi", task.getActivityName());

    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().deleteProcess(definition.getUUID());
  }

  public void testSkipInexistentTask() throws Exception{

    try {
      ActivityInstanceUUID taskUUID = new ActivityInstanceUUID("thisTaskDoesNotExist");  	
      getRuntimeAPI().skipTask(taskUUID, null);
      fail("skip an inexistent task");
    } catch (TaskNotFoundException e){
      //OK
    } 	
  }

  public void testSkipExecutingTask() throws Exception {
    ProcessDefinition process = ProcessBuilder.createProcess("process", "1.0")
    .addHuman(getLogin())
    .addHumanTask("step1", getLogin())
    .addHumanTask("step2", getLogin())
    .addTransition("step1", "step2")
    .done();

    process = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process));
    ProcessDefinitionUUID processUUID = process.getUUID();

    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    Collection<LightTaskInstance> tasks = getQueryRuntimeAPI().getLightTaskList(instanceUUID, ActivityState.READY);  	
    LightTaskInstance task = tasks.iterator().next();  	

    getRuntimeAPI().startTask(task.getUUID(), true);
    try {
      getRuntimeAPI().skipTask(task.getUUID(), null);
      fail("skip executing task");
    } catch (IllegalTaskStateException e){
      //OK
    } 

    tasks = getQueryRuntimeAPI().getLightTaskList(instanceUUID, ActivityState.EXECUTING);
    assertEquals(1, tasks.size());  	
    task = tasks.iterator().next();
    assertEquals("step1", task.getActivityName()); 

    tasks = getQueryRuntimeAPI().getLightTaskList(instanceUUID, ActivityState.SKIPPED);
    assertEquals(0, tasks.size());

    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testSkipSuspendedTask() throws Exception {
    ProcessDefinition process = ProcessBuilder.createProcess("process", "1.0")
    .addHuman(getLogin())
    .addHumanTask("step1", getLogin())
    .addHumanTask("step2", getLogin())
    .addTransition("step1", "step2")
    .done();

    process = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process));
    ProcessDefinitionUUID processUUID = process.getUUID();

    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    Collection<LightTaskInstance> tasks = getQueryRuntimeAPI().getLightTaskList(instanceUUID, ActivityState.READY);  	
    LightTaskInstance task = tasks.iterator().next();  	

    getRuntimeAPI().suspendTask(task.getUUID(), true);
    try {
      getRuntimeAPI().skipTask(task.getUUID(), null);
      fail("skip suspended task");
    } catch (IllegalTaskStateException e){
      //OK
    } 

    tasks = getQueryRuntimeAPI().getLightTaskList(instanceUUID, ActivityState.SUSPENDED);
    assertEquals(1, tasks.size());  	
    task = tasks.iterator().next();
    assertEquals("step1", task.getActivityName()); 

    tasks = getQueryRuntimeAPI().getLightTaskList(instanceUUID, ActivityState.SKIPPED);
    assertEquals(0, tasks.size());

    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testSkipCancelledTask() throws Exception {
    ProcessDefinition process = ProcessBuilder.createProcess("process", "1.0")
    .addHuman(getLogin())
    .addHumanTask("step1", getLogin())
    .addHumanTask("step2", getLogin())
    .addTransition("step1", "step2")
    .done();

    process = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process));
    ProcessDefinitionUUID processUUID = process.getUUID();

    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    Collection<LightTaskInstance> tasks = getQueryRuntimeAPI().getLightTaskList(instanceUUID, ActivityState.READY);  	
    LightTaskInstance task = tasks.iterator().next();  	

    getRuntimeAPI().cancelProcessInstance(instanceUUID);
    try {
      getRuntimeAPI().skipTask(task.getUUID(), null);
      fail("skip cancelled task");
    } catch (TaskNotFoundException e){
      //OK
    } 

    tasks = getQueryRuntimeAPI().getLightTaskList(instanceUUID, ActivityState.CANCELLED);
    assertEquals(1, tasks.size());  	
    task = tasks.iterator().next();
    assertEquals("step1", task.getActivityName()); 

    tasks = getQueryRuntimeAPI().getLightTaskList(instanceUUID, ActivityState.SKIPPED);
    assertEquals(0, tasks.size());

    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testSkipFinishedTask() throws Exception {
    ProcessDefinition process = ProcessBuilder.createProcess("process", "1.0")
    .addHuman(getLogin())
    .addHumanTask("step1", getLogin())
    .addHumanTask("step2", getLogin())
    .addTransition("step1", "step2")
    .done();

    process = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process));
    ProcessDefinitionUUID processUUID = process.getUUID();

    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    Collection<LightTaskInstance> tasks = getQueryRuntimeAPI().getLightTaskList(instanceUUID, ActivityState.READY);  	
    LightTaskInstance task = tasks.iterator().next();  	

    getRuntimeAPI().executeTask(task.getUUID(), true);
    try {
      getRuntimeAPI().skipTask(task.getUUID(), null);
      fail("skip finished task");
    } catch (IllegalTaskStateException e){
      //OK
    } 

    tasks = getQueryRuntimeAPI().getLightTaskList(instanceUUID, ActivityState.FINISHED);
    assertEquals(1, tasks.size());  	
    task = tasks.iterator().next();
    assertEquals("step1", task.getActivityName()); 

    tasks = getQueryRuntimeAPI().getLightTaskList(instanceUUID, ActivityState.SKIPPED);
    assertEquals(0, tasks.size());

    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testSkipSkippedTask() throws Exception {
    ProcessDefinition process = ProcessBuilder.createProcess("process", "1.0")
    .addHuman(getLogin())
    .addHumanTask("step1", getLogin())
    .addHumanTask("step2", getLogin())
    .addTransition("step1", "step2")
    .done();

    process = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process));
    ProcessDefinitionUUID processUUID = process.getUUID();

    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    Collection<LightTaskInstance> tasks = getQueryRuntimeAPI().getLightTaskList(instanceUUID, ActivityState.READY);  	
    LightTaskInstance task = tasks.iterator().next();  	

    getRuntimeAPI().skipTask(task.getUUID(), null);

    try {
      getRuntimeAPI().skipTask(task.getUUID(), null);
      fail("skip skipped task");
    } catch (IllegalTaskStateException e){
      //OK
    } 

    tasks = getQueryRuntimeAPI().getLightTaskList(instanceUUID, ActivityState.SKIPPED);
    assertEquals(1, tasks.size());  	
    task = tasks.iterator().next();
    assertEquals("step1", task.getActivityName()); 

    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testSkipLoopTask() throws Exception {
    ProcessDefinition definition = 
      ProcessBuilder.createProcess("loopState", "1.0")
      .addIntegerData("counter", 0)
      .addHuman(getLogin())
      .addHumanTask("loop", getLogin())
      .addLoop("counter != 1", true)
      .addSystemTask("end")
      .addTransition("loop", "end")
      .done();

    definition = getManagementAPI().deploy(getBusinessArchive(definition));

    Collection<LightTaskInstance> tasks = null;
    LightTaskInstance looptask = null;

    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(definition.getUUID());

    tasks = getQueryRuntimeAPI().getLightTaskList(instanceUUID, ActivityState.SKIPPED);
    assertEquals(0, tasks.size());

    tasks = getQueryRuntimeAPI().getLightTaskList(instanceUUID, ActivityState.READY);
    assertEquals(1, tasks.size());

    looptask = tasks.iterator().next();    
    assertEquals("loop", looptask.getActivityName());
    getRuntimeAPI().skipTask(looptask.getUUID(), null);    

    tasks = getQueryRuntimeAPI().getLightTaskList(instanceUUID, ActivityState.SKIPPED);
    assertEquals(1, tasks.size());
    looptask = tasks.iterator().next();    
    assertEquals("loop", looptask.getActivityName());

    tasks = getQueryRuntimeAPI().getLightTaskList(instanceUUID, ActivityState.READY);
    assertEquals(0, tasks.size());

    LightProcessInstance instance = getQueryRuntimeAPI().getLightProcessInstance(instanceUUID);
    assertEquals(InstanceState.FINISHED, instance.getInstanceState());

    getRuntimeAPI().deleteProcessInstance(instanceUUID);    
    getManagementAPI().deleteProcess(definition.getUUID());
  }

  public void testSkipTaskWithLocalVariablesToUpdate () throws Exception{
    ProcessDefinition process = ProcessBuilder.createProcess("process", "1.0")
    .addHuman(getLogin())
    .addHumanTask("step1", getLogin())
    .addIntegerData("count", 0)
    .addHumanTask("step2", getLogin())
    .addTransition("step1", "step2")
    .addCondition("count > 5")
    .done();

    process = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process));
    ProcessDefinitionUUID processUUID = process.getUUID();

    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    Collection<LightTaskInstance> tasks = getQueryRuntimeAPI().getLightTaskList(instanceUUID, ActivityState.READY);
    assertEquals(1, tasks.size());  	
    LightTaskInstance task = tasks.iterator().next();
    assertEquals("step1", task.getActivityName());  	

    tasks = getQueryRuntimeAPI().getLightTaskList(instanceUUID, ActivityState.SKIPPED);
    assertEquals(0, tasks.size());

    final Map<String, Object> variablesToUpdate = new HashMap<String, Object>();
    variablesToUpdate.put("count", 6);  	
    getRuntimeAPI().skipTask(task.getUUID(), variablesToUpdate);

    tasks = getQueryRuntimeAPI().getLightTaskList(instanceUUID, ActivityState.SKIPPED);
    assertEquals(1, tasks.size());  	
    task = tasks.iterator().next();
    assertEquals("step1", task.getActivityName());

    tasks = getQueryRuntimeAPI().getLightTaskList(instanceUUID, ActivityState.READY);
    assertEquals(1, tasks.size());  	
    task = tasks.iterator().next();
    assertEquals("step2", task.getActivityName());   	

    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testSkipTaskWithForgotLocalVariablesToUpdate () throws Exception {
    ProcessDefinition process = ProcessBuilder.createProcess("process", "1.0")
    .addHuman(getLogin())
    .addHumanTask("step1", getLogin())
    .addIntegerData("count", 0)
    .addHumanTask("step2", getLogin())
    .addTransition("step1", "step2")
    .addCondition("count > 5")
    .done();

    process = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process));
    ProcessDefinitionUUID processUUID = process.getUUID();

    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    Collection<LightTaskInstance> tasks = getQueryRuntimeAPI().getLightTaskList(instanceUUID, ActivityState.READY);
    assertEquals(1, tasks.size());  	
    LightTaskInstance task = tasks.iterator().next();
    assertEquals("step1", task.getActivityName());  	

    tasks = getQueryRuntimeAPI().getLightTaskList(instanceUUID, ActivityState.SKIPPED);
    assertEquals(0, tasks.size());

    getRuntimeAPI().skipTask(task.getUUID(), null);

    tasks = getQueryRuntimeAPI().getLightTaskList(instanceUUID, ActivityState.SKIPPED);
    assertEquals(1, tasks.size());  	
    task = tasks.iterator().next();
    assertEquals("step1", task.getActivityName());

    tasks = getQueryRuntimeAPI().getLightTaskList(instanceUUID, ActivityState.READY);
    assertEquals(0, tasks.size());

    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testSkipTaskWithGlobalVariablesToUpdate () throws Exception{
    ProcessDefinition process = ProcessBuilder.createProcess("process", "1.0")
    .addIntegerData("count", 0)
    .addHuman(getLogin())
    .addHumanTask("step1", getLogin())  		
    .addHumanTask("step2", getLogin())
    .addTransition("step1", "step2")
    .addCondition("count > 5")
    .done();

    process = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process));
    ProcessDefinitionUUID processUUID = process.getUUID();

    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    Collection<LightTaskInstance> tasks = getQueryRuntimeAPI().getLightTaskList(instanceUUID, ActivityState.READY);
    assertEquals(1, tasks.size());  	
    LightTaskInstance task = tasks.iterator().next();
    assertEquals("step1", task.getActivityName());  	

    tasks = getQueryRuntimeAPI().getLightTaskList(instanceUUID, ActivityState.SKIPPED);
    assertEquals(0, tasks.size());

    final Map<String, Object> variablesToUpdate = new HashMap<String, Object>();
    variablesToUpdate.put("count", 6);  	
    getRuntimeAPI().skipTask(task.getUUID(), variablesToUpdate);

    tasks = getQueryRuntimeAPI().getLightTaskList(instanceUUID, ActivityState.SKIPPED);
    assertEquals(1, tasks.size());  	
    task = tasks.iterator().next();
    assertEquals("step1", task.getActivityName());

    tasks = getQueryRuntimeAPI().getLightTaskList(instanceUUID, ActivityState.READY);
    assertEquals(1, tasks.size());  	
    task = tasks.iterator().next();
    assertEquals("step2", task.getActivityName());   	

    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testSkipTaskWithForgotGlobalVariablesToUpdate () throws Exception {
    ProcessDefinition process = ProcessBuilder.createProcess("process", "1.0")
    .addHuman(getLogin())
    .addIntegerData("count", 0)
    .addHumanTask("step1", getLogin())  	
    .addHumanTask("step2", getLogin())
    .addTransition("step1", "step2")
    .addCondition("count > 5")
    .done();

    process = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process));
    ProcessDefinitionUUID processUUID = process.getUUID();

    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    Collection<LightTaskInstance> tasks = getQueryRuntimeAPI().getLightTaskList(instanceUUID, ActivityState.READY);
    assertEquals(1, tasks.size());  	
    LightTaskInstance task = tasks.iterator().next();
    assertEquals("step1", task.getActivityName());  	

    tasks = getQueryRuntimeAPI().getLightTaskList(instanceUUID, ActivityState.SKIPPED);
    assertEquals(0, tasks.size());

    getRuntimeAPI().skipTask(task.getUUID(), null);

    tasks = getQueryRuntimeAPI().getLightTaskList(instanceUUID, ActivityState.SKIPPED);
    assertEquals(1, tasks.size());  	
    task = tasks.iterator().next();
    assertEquals("step1", task.getActivityName());

    tasks = getQueryRuntimeAPI().getLightTaskList(instanceUUID, ActivityState.READY);
    assertEquals(0, tasks.size());

    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  @SuppressWarnings("unchecked")
  public void testGetModifiedJavaObject() throws Exception{
    ProcessDefinition process = ProcessBuilder.createProcess("processGMJO", "1.0")
    .addHuman(getLogin())
    .addHumanTask("step1", getLogin())    
    .done();

    process = getManagementAPI().deploy(getBusinessArchive(process));
    String variableExpression = "myList#myList#addAll";

    List<Integer> variableValue = new ArrayList<Integer>();
    variableValue.add(3);

    List<Integer> attributeValue = new ArrayList<Integer>();
    attributeValue.add(10);
    attributeValue.add(20);

    List<Integer> list = (List<Integer>) getRuntimeAPI().getModifiedJavaObject(process.getUUID(), variableExpression, variableValue, attributeValue);
    assertEquals(3, list.size());
    assertEquals(new Integer(3), list.get(0));
    assertEquals(new Integer(10), list.get(1));
    assertEquals(new Integer(20), list.get(2));

    getManagementAPI().deleteProcess(process.getUUID());    
  }

  /* TODO Test to uncomment when activityInstance UUID will be set before activity variable
  public void testEvaluateGroovyExpresionnUsingAnActivityVariable() throws Exception {
    ProcessDefinition definition =
      ProcessBuilder.createProcess("ouch", null)
      .addHuman(getLogin())
      .addStringDataFromScript("processUUID", "${processInstance.getUUID().getValue()}")
      .addHumanTask("test", getLogin())
        .addStringDataFromScript("UUID", "${activityInstance.getUUID().getValue()}")
      .done();

    definition = getManagementAPI().deploy(getBusinessArchive(definition));

    final ProcessDefinitionUUID processUUID = definition.getUUID();
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    String actual = (String) getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "processUUID");
    assertTrue(actual.length() > 0);
    actual = (String) getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "processUUID");
    assertTrue(actual.length() > 0);
    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }*/

  public void testUpdateExpectedEndDate() throws Exception {
    Date now = new Date();
    ProcessDefinition definition = ProcessBuilder.createProcess("update", "2.1")
    .addHuman(getLogin())
    .addHumanTask("step1", getLogin())
    .addActivityExecutingTime(1000)
    .done();
    getManagementAPI().deploy(getBusinessArchive(definition));
    getRuntimeAPI().instantiateProcess(definition.getUUID());

    Collection<LightTaskInstance> tasks = getQueryRuntimeAPI().getLightTaskList(ActivityState.READY);
    assertEquals(1, tasks.size());
    LightTaskInstance task = tasks.iterator().next();
    assertTrue(now.before(task.getExpectedEndDate()));
    Date date = DateUtil.getNextDay(now);
    getRuntimeAPI().updateActivityExpectedEndDate(task.getUUID(), date);

    TaskInstance updatedTask = getQueryRuntimeAPI().getTask(task.getUUID());
    assertEquals(date, updatedTask.getExpectedEndDate());

    getManagementAPI().deleteProcess(definition.getUUID());
  }

  public void testUpdateExpectedEndDateWithAnOldDate() throws Exception {
    Date now = new Date();
    ProcessDefinition definition = ProcessBuilder.createProcess("update", "2.1")
    .addHuman(getLogin())
    .addHumanTask("step1", getLogin())
    .addActivityExecutingTime(1000)
    .done();
    getManagementAPI().deploy(getBusinessArchive(definition));
    getRuntimeAPI().instantiateProcess(definition.getUUID());

    Collection<LightTaskInstance> tasks = getQueryRuntimeAPI().getLightTaskList(ActivityState.READY);
    assertEquals(1, tasks.size());
    LightTaskInstance task = tasks.iterator().next();
    assertTrue(now.before(task.getExpectedEndDate()));

    getRuntimeAPI().updateActivityExpectedEndDate(task.getUUID(), now);

    TaskInstance updatedTask = getQueryRuntimeAPI().getTask(task.getUUID());
    assertEquals(now, updatedTask.getExpectedEndDate());

    getManagementAPI().deleteProcess(definition.getUUID());
  }

  public void testEvaluateGroovyExpressionWithInstanceInitatorUsingProcessInstance() throws Exception {
    ProcessDefinition process = 
      ProcessBuilder.createProcess("pii", "1.0")
      .addHuman(getLogin())
      .addHumanTask("step1", getLogin())
      .done();

    process = getManagementAPI().deploy(getBusinessArchive(process));

    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(process.getUUID());

    final String script = "${" + BonitaConstants.PROCESS_INSTANCE_INITIATOR + "}";
    final String instanceInitiator = (String) getRuntimeAPI().evaluateGroovyExpression(script, instanceUUID, false);
    assertEquals(getLogin(),instanceInitiator);
    getManagementAPI().deleteProcess(process.getUUID());
  }

  public void testEvaluateGroovyExpressionWithInstanceInitatorUsingProcessDefinition() throws Exception {
    ProcessDefinition process = 
      ProcessBuilder.createProcess("pii", "1.0")
      .addHuman(getLogin())
      .addHumanTask("step1", getLogin())
      .done();

    process = getManagementAPI().deploy(getBusinessArchive(process));

    final String script = "${" + BonitaConstants.PROCESS_INSTANCE_INITIATOR + "}";
    String instanceInitiator = (String) getRuntimeAPI().evaluateGroovyExpression(script, process.getUUID());
    assertNull(instanceInitiator);
    getManagementAPI().deleteProcess(process.getUUID());
  }

  public void testEvaluateGroovyExpressionWithInstanceInitatorUsingAtivityInstance() throws Exception {
    ProcessDefinition process = 
      ProcessBuilder.createProcess("pii", "1.0")
      .addHuman(getLogin())
      .addHumanTask("step1", getLogin())
      .done();

    process = getManagementAPI().deploy(getBusinessArchive(process));

    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(process.getUUID());
    final Set<LightActivityInstance> activities = getQueryRuntimeAPI().getLightActivityInstances(instanceUUID, "step1");
    assertEquals(1, activities.size());

    final ActivityInstanceUUID activityUUID = activities.iterator().next().getUUID();
    final String script = "${" + BonitaConstants.PROCESS_INSTANCE_INITIATOR + "}";
    String instanceInitiator = (String) getRuntimeAPI().evaluateGroovyExpression(script, activityUUID, false, false);
    assertEquals(getLogin(),instanceInitiator);
    getManagementAPI().deleteProcess(process.getUUID());
  }

  public void testEvaluateGroovyExpressionsInProcessDefinition() throws BonitaException {
    final ProcessDefinition process =
      getManagementAPI().deploy(getBusinessArchive(getProcess()));
    final ProcessDefinitionUUID processUUID = process.getUUID();
    getRuntimeAPI().instantiateProcess(processUUID);

    final Map<String, String> expressions = new HashMap<String, String>();
    expressions.put("first", "${yksi + kolme}");
    expressions.put("second", "one + three equals ${yksi + kolme}");
    expressions.put("third", "Where is ${men = ['James', 'John', 'Brian']; men[1]}?");
    expressions.put("fourth", "${processDefinition}");

    final Map<String, Object> results = getRuntimeAPI().evaluateGroovyExpressions(expressions, processUUID, null);

    assertEquals(Integer.valueOf(4), (Integer) results.get("first"));
    assertEquals("one + three equals 4", results.get("second"));
    assertEquals("Where is John?", results.get("third"));
    assertNotNull(results.get("fourth"));

    getManagementAPI().deleteProcess(processUUID);
  }

  public void testEvaluateGroovyExpressionsInProcessDefinitionWithContext() throws BonitaException {
    final ProcessDefinition process =
      getManagementAPI().deploy(getBusinessArchive(getProcess()));
    final ProcessDefinitionUUID processUUID = process.getUUID();
    getRuntimeAPI().instantiateProcess(processUUID);

    final Map<String, Object> context = new HashMap<String, Object>();
    context.put("quatro", 4);
    context.put("suffix", "anything");

    final Map<String, String> expressions = new HashMap<String, String>();
    expressions.put("first", "${yksi + kolme + quatro}");
    expressions.put("second", "one + three equals ${yksi + kolme} ${suffix}");
    expressions.put("third", "Where is ${men = ['James', 'John', 'Brian']; men[1]}? ${suffix}");
    expressions.put("fourth", "${processDefinition}");

    final Map<String, Object> results = getRuntimeAPI().evaluateGroovyExpressions(expressions, processUUID, context);

    assertEquals(Integer.valueOf(8), (Integer) results.get("first"));
    assertEquals("one + three equals 4 anything", results.get("second"));
    assertEquals("Where is John? anything", results.get("third"));
    assertNotNull(results.get("fourth"));

    getManagementAPI().deleteProcess(processUUID);
  }

  public void testEvaluateGroovyExpressionsInProcessDefinitionExceptions() throws BonitaException {
    final ProcessDefinition process =
      getManagementAPI().deploy(getBusinessArchive(getProcess()));
    final ProcessDefinitionUUID processUUID = process.getUUID();
    getRuntimeAPI().instantiateProcess(processUUID);

    final Map<String, String> expressions = new HashMap<String, String>();
    expressions.put("first", "${processInstance}");
    expressions.put("second", "Where is ${men = ['James', 'John', 'Brian']; men[1]}?");
    expressions.put("third", "${activityInstance}");

    final Map<String, Object> results = getRuntimeAPI().evaluateGroovyExpressions(expressions, processUUID, null);
    assertNull(results.get("first"));
    assertEquals("Where is John?", results.get("second"));
    assertNull(results.get("third"));

    getManagementAPI().deleteProcess(processUUID);
  }

  public void testEvaluateGroovyExpressionsInActivityInstance() throws BonitaException {
    final ProcessDefinition process =
      getManagementAPI().deploy(getBusinessArchive(getProcess()));
    final ProcessDefinitionUUID processUUID = process.getUUID();
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    final Set<TaskInstance> tasks = getQueryRuntimeAPI().getTasks(instanceUUID);
    final TaskInstance task = tasks.iterator().next();
    final ActivityInstanceUUID activityUUID = task.getUUID();
    final Map<String, String> expressions = new HashMap<String, String>();
    expressions.put("1", "one + three equals ${yksi + kolme}");
    expressions.put("2", "${yksi + kolme}");
    expressions.put("3", "Where is ${men = ['James', 'John', 'Brian']; men[1]}?");
    expressions.put("4", "${processDefinition}");
    expressions.put("5", "${processInstance}");
    expressions.put("6", "${activityInstance}");
    expressions.put("7", "${loggedUser}");

    final Map<String, Object> results = getRuntimeAPI().evaluateGroovyExpressions(expressions, activityUUID, null, true, true);

    assertEquals("one + three equals 4", results.get("1"));
    assertEquals(Integer.valueOf(4), (Integer) results.get("2"));
    assertEquals("Where is John?", results.get("3"));
    assertNotNull(results.get("4"));
    assertNotNull(results.get("5"));
    assertNotNull(results.get("6"));
    assertEquals("admin", results.get("7"));

    getManagementAPI().deleteProcess(processUUID);
  }

  public void testEvaluateGroovyExpressionsInActivityInstanceWithContext() throws BonitaException {
    final ProcessDefinition process =
      getManagementAPI().deploy(getBusinessArchive(getProcess()));
    final ProcessDefinitionUUID processUUID = process.getUUID();
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    final Map<String, Object> context = new HashMap<String, Object>();
    context.put("quatro", 4);
    context.put("suffix", "anything");

    final Set<TaskInstance> tasks = getQueryRuntimeAPI().getTasks(instanceUUID);
    final TaskInstance task = tasks.iterator().next();
    final ActivityInstanceUUID activityUUID = task.getUUID();
    final Map<String, String> expressions = new HashMap<String, String>();
    expressions.put("1", "one + three equals ${yksi + kolme} ${suffix}");
    expressions.put("2", "${yksi + kolme + quatro}");

    final Map<String, Object> results = getRuntimeAPI().evaluateGroovyExpressions(expressions, activityUUID, context, true, true);

    assertEquals("one + three equals 4 anything", results.get("1"));
    assertEquals(Integer.valueOf(8), (Integer) results.get("2"));

    getManagementAPI().deleteProcess(processUUID);
  }

  public void testEvaluateGroovyExpressionsInActivityInstancePropagate() throws BonitaException {
    final ProcessDefinition process =
      getManagementAPI().deploy(getBusinessArchive(getProcess()));
    final ProcessDefinitionUUID processUUID = process.getUUID();
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    final Set<TaskInstance> tasks = getQueryRuntimeAPI().getTasks(instanceUUID);
    final TaskInstance task = tasks.iterator().next();
    final ActivityInstanceUUID activityUUID = task.getUUID();
    final Map<String, String> expressions = new HashMap<String, String>();
    expressions.put("1", "one + three equals ${r = yksi + kolme}");
    expressions.put("2", "${yksi + kolme}");

    Map<String, Object> results = getRuntimeAPI().evaluateGroovyExpressions(expressions, activityUUID, null, false, false);
    int result = (Integer) getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "r");
    assertEquals(0, result);

    assertEquals("one + three equals 4", results.get("1"));
    assertEquals(Integer.valueOf(4), (Integer) results.get("2"));

    results = getRuntimeAPI().evaluateGroovyExpressions(expressions, activityUUID, null, false, true);
    result = (Integer) getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "r");
    assertEquals(4, result);

    getManagementAPI().deleteProcess(processUUID);
  }

  public void testEvaluateGroovyExpressionsInActivityInstanceUseActiveScope() throws Exception {
    ProcessDefinition process = getAWPocess();
    BusinessArchive businessArchive = getBusinessArchive(process);
    process = getManagementAPI().deploy(businessArchive);
    ProcessInstanceUUID processInstanceUUID = getRuntimeAPI().instantiateProcess(process.getUUID());

    Collection<TaskInstance> tasks = getQueryRuntimeAPI().getTaskList(processInstanceUUID, ActivityState.READY);
    ActivityInstanceUUID uuid = null;
    for (TaskInstance activityInstance : tasks) {
      uuid = activityInstance.getUUID();
    }
    assertNotNull(uuid);
    getRuntimeAPI().setProcessInstanceVariable(processInstanceUUID, "Applications", "Excel");
    getRuntimeAPI().executeTask(uuid, true);

    getRuntimeAPI().setProcessInstanceVariable(processInstanceUUID, "Applications", "MailReader");

    final Map<String, String> expressions = new HashMap<String, String>();
    expressions.put("1", "${Applications}");
    expressions.put("2", "${2+3}");

    Map<String, Object> result = getRuntimeAPI().evaluateGroovyExpressions(expressions, uuid, new HashMap<String, Object>(), true, false);
    assertEquals("Excel", result.get("1"));
    assertEquals(5, result.get("2"));

    result = getRuntimeAPI().evaluateGroovyExpressions(expressions, uuid, new HashMap<String, Object>(), false, false);
    assertEquals("MailReader", result.get("1"));
    assertEquals(5, result.get("2"));

    getRuntimeAPI().deleteAllProcessInstances(process.getUUID());
    getManagementAPI().disable(process.getUUID());
    getManagementAPI().deleteProcess(process.getUUID());
  }

  public void testEvaluateGroovyExpressionsInActivityInstanceExceptions() throws BonitaException {
    final ProcessDefinition process =
      getManagementAPI().deploy(getBusinessArchive(getProcess()));
    final ProcessDefinitionUUID processUUID = process.getUUID();
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    final Set<TaskInstance> tasks = getQueryRuntimeAPI().getTasks(instanceUUID);
    final TaskInstance task = tasks.iterator().next();
    final ActivityInstanceUUID activityUUID = task.getUUID();
    final Map<String, String> expressions = new HashMap<String, String>();
    expressions.put("1", "${yksi / 0}");
    expressions.put("2", null);

    try {
      getRuntimeAPI().evaluateGroovyExpressions(expressions, activityUUID, null, true, true);
      fail("excepiton excpeted");
    } catch (GroovyException e) {

    }

    getManagementAPI().deleteProcess(processUUID);
  }

  public void testEvaluateGroovyExpressionsInProcessInstance() throws BonitaException {
    final ProcessDefinition process =
      getManagementAPI().deploy(getBusinessArchive(getProcess()));
    final ProcessDefinitionUUID processUUID = process.getUUID();
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    final Map<String, String> expressions = new HashMap<String, String>();
    expressions.put("1", "one + three equals ${yksi + kolme}");
    expressions.put("2", "${yksi + kolme}");
    expressions.put("3", "Where is ${men = ['James', 'John', 'Brian']; men[1]}?");
    expressions.put("4", "${processDefinition}");
    expressions.put("5", "${processInstance}");
    expressions.put("6", "${activityInstance}");
    expressions.put("7", "${loggedUser}");

    final Map<String, Object> results = getRuntimeAPI().evaluateGroovyExpressions(expressions, instanceUUID, null, true, true);

    assertEquals("one + three equals 4", results.get("1"));
    assertEquals(Integer.valueOf(4), (Integer) results.get("2"));
    assertEquals("Where is John?", results.get("3"));
    assertNotNull(results.get("4"));
    assertNotNull(results.get("5"));
    assertNull(results.get("6"));
    assertEquals("admin", results.get("7"));

    getManagementAPI().deleteProcess(processUUID);
  }

  public void testEvaluateGroovyExpressionsInProcessInstanceWithContext() throws BonitaException {
    final ProcessDefinition process =
      getManagementAPI().deploy(getBusinessArchive(getProcess()));
    final ProcessDefinitionUUID processUUID = process.getUUID();
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    final Map<String, Object> context = new HashMap<String, Object>();
    context.put("quatro", 4);
    context.put("suffix", "anything");

    final Map<String, String> expressions = new HashMap<String, String>();
    expressions.put("1", "one + three equals ${yksi + kolme} ${suffix}");
    expressions.put("2", "${yksi + kolme + quatro}");
    expressions.put("3", "Where is ${men = ['James', 'John', 'Brian']; men[1]}?");
    expressions.put("4", "${processDefinition}");
    expressions.put("5", "${processInstance}");
    expressions.put("6", "${activityInstance}");
    expressions.put("7", "${loggedUser}");

    final Map<String, Object> results = getRuntimeAPI().evaluateGroovyExpressions(expressions, instanceUUID, context, true, true);

    assertEquals("one + three equals 4 anything", results.get("1"));
    assertEquals(Integer.valueOf(8), (Integer) results.get("2"));
    assertEquals("Where is John?", results.get("3"));
    assertNotNull(results.get("4"));
    assertNotNull(results.get("5"));
    assertNull(results.get("6"));
    assertEquals("admin", results.get("7"));

    getManagementAPI().deleteProcess(processUUID);
  }

  public void testEvaluateGroovyExpressionsInProcessInstancePropagate() throws BonitaException {
    final ProcessDefinition process =
      getManagementAPI().deploy(getBusinessArchive(getProcess()));
    final ProcessDefinitionUUID processUUID = process.getUUID();
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    final Map<String, String> expressions = new HashMap<String, String>();
    expressions.put("1", "one + three equals ${r = yksi + kolme}");
    expressions.put("2", "${yksi + kolme}");
    expressions.put("3", "Where is ${men = ['James', 'John', 'Brian']; men[1]}?");
    expressions.put("4", "${processDefinition}");
    expressions.put("5", "${processInstance}");
    expressions.put("6", "${activityInstance}");
    expressions.put("7", "${loggedUser}");

    getRuntimeAPI().evaluateGroovyExpressions(expressions, instanceUUID, null, false, false);

    int result = (Integer) getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "r");
    assertEquals(0, result);

    getRuntimeAPI().evaluateGroovyExpressions(expressions, instanceUUID, null, false, true);

    result = (Integer) getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "r");
    assertEquals(4, result);

    getManagementAPI().deleteProcess(processUUID);
  }

  public void testEvaluateInitialValueExpressionsWithInstance() throws Exception {

    ProcessDefinition process = getAWPocess();
    BusinessArchive businessArchive = getBusinessArchive(process);
    process = getManagementAPI().deploy(businessArchive);
    ProcessInstanceUUID processInstanceUUID = getRuntimeAPI().instantiateProcess(process.getUUID());

    Collection<TaskInstance> tasks = getQueryRuntimeAPI().getTaskList(processInstanceUUID, ActivityState.READY);
    ActivityInstanceUUID uuid = null;
    for (TaskInstance activityInstance : tasks) {
      uuid = activityInstance.getUUID();
    }
    assertNotNull(uuid);
    getRuntimeAPI().setVariable(uuid, "Applications", "Excel");
    getRuntimeAPI().executeTask(uuid, true);

    final Map<String, String> expressions = new HashMap<String, String>();
    expressions.put("1", "${Applications}");
    expressions.put("2", "${2+3}");

    Map<String, Object> result = getRuntimeAPI().evaluateGroovyExpressions(expressions, processInstanceUUID, new HashMap<String, Object>(), true, false);
    assertEquals("Word", result.get("1"));
    assertEquals(5, result.get("2"));

    result = getRuntimeAPI().evaluateGroovyExpressions(expressions, processInstanceUUID, new HashMap<String, Object>(), false, false);
    assertEquals("Excel", result.get("1"));
    assertEquals(5, result.get("2"));

    getManagementAPI().deleteProcess(process.getUUID());
  }

  public void testPerfAboutEvaluateGroovyExpressionInProcessDefiniton() throws Exception {
    final ProcessDefinition process =
      getManagementAPI().deploy(getBusinessArchive(getProcess()));
    final ProcessDefinitionUUID processUUID = process.getUUID();
    getRuntimeAPI().instantiateProcess(processUUID);

    final int nbOfExpressions = 100;
    long start = System.currentTimeMillis();
    for (int i = 0; i < nbOfExpressions; i++) {
      getRuntimeAPI().evaluateGroovyExpression("${kolme}", processUUID, null);
    }
    long end = System.currentTimeMillis();
    final long evaluateSingleExprDuration = end - start;

    final Map<String, String> expressions = getExpressions(nbOfExpressions);

    start = System.currentTimeMillis();
    getRuntimeAPI().evaluateGroovyExpressions(expressions, processUUID, null);
    end = System.currentTimeMillis();
    final long evaluateSeveralExprDuration = end - start;

    logOperationsExecutionTime(nbOfExpressions,
        evaluateSingleExprDuration, evaluateSeveralExprDuration, "evaluateGroovyExpression", "evaluateGroovyExpressions", "expressions");

    assertTrue(evaluateSeveralExprDuration < evaluateSingleExprDuration);

    getManagementAPI().deleteProcess(processUUID);
  }

  public void testPerfAboutEvaluateGroovyExpressionUsingProcessInstance() throws Exception {
    final ProcessDefinition process =
      getManagementAPI().deploy(getBusinessArchive(getProcess()));
    final ProcessDefinitionUUID processUUID = process.getUUID();
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    final int nbOfExpressions = 100;
    long start = System.currentTimeMillis();
    for (int i = 0; i < nbOfExpressions; i++) {
      getRuntimeAPI().evaluateGroovyExpression("${kolme}", instanceUUID, null, false, false);
    }
    long end = System.currentTimeMillis();
    final long evaluateSingleExprDuration = end - start;

    final Map<String, String> expressions = getExpressions(nbOfExpressions);
    start = System.currentTimeMillis();
    getRuntimeAPI().evaluateGroovyExpressions(expressions, instanceUUID, null, false, false);
    end = System.currentTimeMillis();
    final long evaluateSeveralExprDuration = end - start;

    logOperationsExecutionTime(nbOfExpressions,
        evaluateSingleExprDuration, evaluateSeveralExprDuration, "evaluateGroovyExpression", "evaluateGroovyExpressions", "expressions");

    assertTrue(evaluateSeveralExprDuration < evaluateSingleExprDuration);

    getManagementAPI().deleteProcess(processUUID);
  }

  public void testPerfAboutEvaluateGroovyExpressionUsingActivityInstance() throws Exception {
    final ProcessDefinition process =
      getManagementAPI().deploy(getBusinessArchive(getProcess()));
    final ProcessDefinitionUUID processUUID = process.getUUID();
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    final Set<LightTaskInstance> tasks = getQueryRuntimeAPI().getLightTasks(instanceUUID);
    final LightTaskInstance task = tasks.iterator().next();
    final ActivityInstanceUUID activityUUID = task.getUUID();

    final int nbOfExpressions = 100;
    long start = System.currentTimeMillis();
    for (int i = 0; i < nbOfExpressions; i++) {
      getRuntimeAPI().evaluateGroovyExpression("${kolme}", activityUUID, null, false, false);
    }
    long end = System.currentTimeMillis();
    final long evaluateSingleExprDuration = end - start;

    final Map<String, String> expressions = getExpressions(nbOfExpressions);
    start = System.currentTimeMillis();
    getRuntimeAPI().evaluateGroovyExpressions(expressions, activityUUID, null, false, false);
    end = System.currentTimeMillis();
    final long evaluateSeveralExprDuration = end - start;

    logOperationsExecutionTime(nbOfExpressions,
        evaluateSingleExprDuration, evaluateSeveralExprDuration, "evaluateGroovyExpression", "evaluateGroovyExpressions", "expressions");

    assertTrue(evaluateSeveralExprDuration < evaluateSingleExprDuration);

    getManagementAPI().deleteProcess(processUUID);
  }

  private void logOperationsExecutionTime(final int nb,
      final long singleOperationDuration,
      final long severalOperationsDuration, final String firstCalledMethod, final String secondCalledMethod, 
      final String kindOfObject) {
    if (LOG.isLoggable(Level.INFO)) {
      LOG.info("--->" + nb + " calls to " + firstCalledMethod + " took " + singleOperationDuration + " mileseconds");
      LOG.info("---> One call to " + secondCalledMethod + " with " + nb + " " + kindOfObject + " took " + severalOperationsDuration + " mileseconds");
    }
  }

  private Map<String, String> getExpressions(final int nbOfExpressions) {
    final Map<String, String> expressions = new HashMap<String, String>();
    for (int i = 0; i < nbOfExpressions; i++) {
      expressions.put(String.valueOf(i), "${kolme}");
    }
    return expressions;
  }

  private Map<String, Object> getVariables(final String variableName, final Object value, final int nbOfVariables) {
    final Map<String, Object> expressions = new HashMap<String, Object>();
    for (int i = 0; i < nbOfVariables; i++) {
      expressions.put(variableName, value);
    }
    return expressions;
  }

  public void testSetProcessInstanceVariables() throws Exception {
    final ProcessDefinition process =
      getManagementAPI().deploy(getBusinessArchive(getProcessWithGlobalAndLocalVariables()));
    final ProcessDefinitionUUID processUUID = process.getUUID();
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);
    final Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("globalInt", 5);
    variables.put("globalStr", "globalUpdated");

    getRuntimeAPI().setProcessInstanceVariables(instanceUUID, variables);

    final int globalInt = (Integer)getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "globalInt");
    final String globalStr = (String)getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "globalStr");
    assertEquals(5, globalInt);
    assertEquals("globalUpdated", globalStr);

    getManagementAPI().deleteProcess(processUUID);
  }

  public void testSetProcessInstanceVariablesWhenFail() throws Exception {
    final ProcessDefinition process =
      getManagementAPI().deploy(getBusinessArchive(getProcessWithGlobalAndLocalVariables()));
    final ProcessDefinitionUUID processUUID = process.getUUID();
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);
    final Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("globalInt", 5);
    variables.put("globalStrstr", "globalUpdated");

    try {
      getRuntimeAPI().setProcessInstanceVariables(instanceUUID, variables);
      fail("VariableNotFound expected");
    } catch (VariableNotFoundException e) {
      //OK
    }

    final int globalInt = (Integer)getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "globalInt");
    final String globalStr = (String)getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "globalStr");
    assertEquals(0, globalInt);
    assertEquals("hello", globalStr);

    getManagementAPI().deleteProcess(processUUID);
  }

  private ProcessDefinition getProcessWithGlobalAndLocalVariables() {
    final ProcessDefinition process = ProcessBuilder.createProcess("p", "2.0")
    .addIntegerData("globalInt", 0)
    .addStringData("globalStr", "hello")
    .addHuman(getLogin())
    .addHumanTask("step1", getLogin())
    .addIntegerData("localInt", 0)
    .addStringData("localStr", "localHello")
    .done();
    return process;
  }

  public void testSetActivityInstanceVariables() throws Exception {
    final ProcessDefinition process =
      getManagementAPI().deploy(getBusinessArchive(getProcessWithGlobalAndLocalVariables()));
    final ProcessDefinitionUUID processUUID = process.getUUID();
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    final Set<LightTaskInstance> tasks = getQueryRuntimeAPI().getLightTasks(instanceUUID);
    final LightTaskInstance task = tasks.iterator().next();
    final ActivityInstanceUUID activityUUID = task.getUUID();

    final Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("localInt", 5);
    variables.put("localStr", "localUpdated");

    getRuntimeAPI().setActivityInstanceVariables(activityUUID, variables);

    final int localInt = (Integer)getQueryRuntimeAPI().getActivityInstanceVariable(activityUUID, "localInt");
    final String localStr = (String)getQueryRuntimeAPI().getActivityInstanceVariable(activityUUID, "localStr");
    assertEquals(5, localInt);
    assertEquals("localUpdated", localStr);

    getManagementAPI().deleteProcess(processUUID);
  }

  public void testSetActivityInstanceVariablesWhenFail() throws Exception {
    final ProcessDefinition process =
      getManagementAPI().deploy(getBusinessArchive(getProcessWithGlobalAndLocalVariables()));
    final ProcessDefinitionUUID processUUID = process.getUUID();
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    final Set<LightTaskInstance> tasks = getQueryRuntimeAPI().getLightTasks(instanceUUID);
    final LightTaskInstance task = tasks.iterator().next();
    final ActivityInstanceUUID activityUUID = task.getUUID();

    final Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("localInt", 5);
    variables.put("localStrstr", "localUpdated");

    try {
      getRuntimeAPI().setActivityInstanceVariables(activityUUID, variables);
      fail("VariableNotFoundException expected");
    } catch (VariableNotFoundException e) {
      //oK
    }

    final int localInt = (Integer)getQueryRuntimeAPI().getActivityInstanceVariable(activityUUID, "localInt");
    final String localStr = (String)getQueryRuntimeAPI().getActivityInstanceVariable(activityUUID, "localStr");
    assertEquals(0, localInt);
    assertEquals("localHello", localStr);

    getManagementAPI().deleteProcess(processUUID);
  }

  public void testSetProcessInstanceVariablesPerf() throws Exception {
    final ProcessDefinition process =
      getManagementAPI().deploy(getBusinessArchive(getProcessWithGlobalAndLocalVariables()));
    final ProcessDefinitionUUID processUUID = process.getUUID();
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    final int nbOfVariables = 100;
    long start = System.currentTimeMillis();
    for (int i = 0; i < nbOfVariables; i++) {
      getRuntimeAPI().setProcessInstanceVariable(instanceUUID, "globalInt", 5);
    }
    long end = System.currentTimeMillis();
    final long setSingleVarDuration = end - start;

    final Map<String, Object> variables = getVariables("globalInt", 5, nbOfVariables);
    start = System.currentTimeMillis();
    getRuntimeAPI().setProcessInstanceVariables(instanceUUID, variables);
    end = System.currentTimeMillis();
    final long setSeveralVarDuration = end - start;

    logOperationsExecutionTime(nbOfVariables,
        setSingleVarDuration, setSeveralVarDuration, "setProcessInstanceVariable", "setProcessInstanceVariables", "variables");

    assertTrue(setSeveralVarDuration < setSingleVarDuration);

    getManagementAPI().deleteProcess(processUUID);
  }

  public void testSetActivityInstanceVariablesPerf() throws Exception {
    final ProcessDefinition process =
      getManagementAPI().deploy(getBusinessArchive(getProcessWithGlobalAndLocalVariables()));
    final ProcessDefinitionUUID processUUID = process.getUUID();
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    final Set<LightTaskInstance> tasks = getQueryRuntimeAPI().getLightTasks(instanceUUID);
    final LightTaskInstance task = tasks.iterator().next();
    final ActivityInstanceUUID activityUUID = task.getUUID();

    final int nbOfVariables = 100;
    long start = System.currentTimeMillis();
    for (int i = 0; i < nbOfVariables; i++) {
      getRuntimeAPI().setActivityInstanceVariable(activityUUID, "localInt", 5);
    }
    long end = System.currentTimeMillis();
    final long evaluateSingleExprDuration = end - start;

    final Map<String, Object> variables = getVariables("localInt", 5, nbOfVariables);
    start = System.currentTimeMillis();
    getRuntimeAPI().setActivityInstanceVariables(activityUUID, variables);
    end = System.currentTimeMillis();
    final long evaluateSeveralExprDuration = end - start;

    logOperationsExecutionTime(nbOfVariables,
        evaluateSingleExprDuration, evaluateSeveralExprDuration, "setActivityInstanceVariable", 
        "setActivityInstanceVariables", "variables");

    assertTrue(evaluateSeveralExprDuration < evaluateSingleExprDuration);

    getManagementAPI().deleteProcess(processUUID);
  }

  public void testCanSkipAFailedTask() throws Exception {
    ProcessDefinition process = ProcessBuilder.createProcess("skippedFailed", "1.0")
    .addHuman(getLogin())
    .addHumanTask("step1", getLogin())
    .addConnector(Event.taskOnReady, ErrorConnector.class.getName(), true)
    .addHumanTask("step2", getLogin())
    .addTransition("step1", "step2")
    .done();

    process = getManagementAPI().deploy(getBusinessArchive(process, null, ErrorConnector.class));

    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(process.getUUID());
    checkState(instanceUUID, ActivityState.FAILED, "step1");
    checkActivityInstanceNotExist(instanceUUID, "step2");

    final Set<LightActivityInstance> activityInstances = getQueryRuntimeAPI().getLightActivityInstances(instanceUUID, "step1");
    final LightActivityInstance activityInstance = activityInstances.iterator().next();
    getRuntimeAPI().skip(activityInstance.getUUID(), null);

    checkState(instanceUUID, ActivityState.SKIPPED, "step1");
    checkState(instanceUUID, ActivityState.READY, "step2");
    checkState(instanceUUID, InstanceState.STARTED);

    getManagementAPI().deleteProcess(process.getUUID());
  }

  public void testCanSkipAFailedAutomaticActivity() throws Exception {
    ProcessDefinition process = ProcessBuilder.createProcess("skippedFailed", "1.0")
    .addSystemTask("step1")
    .addConnector(Event.automaticOnExit, ErrorConnector.class.getName(), true)
    .addSystemTask("step2")
    .addTransition("step1", "step2")
    .done();

    process = getManagementAPI().deploy(getBusinessArchive(process, null, ErrorConnector.class));

    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(process.getUUID());
    checkState(instanceUUID, ActivityState.FAILED, "step1");
    checkActivityInstanceNotExist(instanceUUID, "step2");

    final Set<LightActivityInstance> activityInstances = getQueryRuntimeAPI().getLightActivityInstances(instanceUUID, "step1");
    final LightActivityInstance activityInstance = activityInstances.iterator().next();
    getRuntimeAPI().skip(activityInstance.getUUID(), null);

    checkState(instanceUUID, ActivityState.SKIPPED, "step1");
    checkState(instanceUUID, ActivityState.FINISHED, "step2");
    checkState(instanceUUID, InstanceState.FINISHED);

    getManagementAPI().deleteProcess(process.getUUID());
  }

  public void testSkippingAFailedActivityEndsInstanceIfNoMoreActiveActivities() throws Exception  {
    ProcessDefinition process = ProcessBuilder.createProcess("skippedFailed", "1.0")
    .addSystemTask("step1")
    .addConnector(Event.automaticOnExit, ErrorConnector.class.getName(), true)
    .done();

    process = getManagementAPI().deploy(getBusinessArchive(process, null, ErrorConnector.class));

    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(process.getUUID());
    checkState(instanceUUID, ActivityState.FAILED, "step1");

    final Set<LightActivityInstance> activityInstances = getQueryRuntimeAPI().getLightActivityInstances(instanceUUID, "step1");
    final LightActivityInstance activityInstance = activityInstances.iterator().next();
    getRuntimeAPI().skip(activityInstance.getUUID(), null);

    checkState(instanceUUID, ActivityState.SKIPPED, "step1");
    checkState(instanceUUID, InstanceState.FINISHED);

    getManagementAPI().deleteProcess(process.getUUID());
  }

  public void testSkipFailedMultiInstantiateActivity() throws Exception {
    final int nbMulti = 10;
    ProcessDefinition definition =
      ProcessBuilder.createProcess("multiple", "1.0")
      .addHuman(getLogin())
      .addSystemTask("multi")
      .addConnector(Event.automaticOnEnter, ErrorConnector.class.getName(), true)
      .addMultipleActivitiesInstantiator(NoContextMulitpleActivitiesInstantiator.class.getName())
      .addInputParameter("number", nbMulti)
      .addMultipleActivitiesJoinChecker(FixedNumberJoinChecker.class.getName())
      .addInputParameter("activityNumber", 3)
      .addHumanTask("step2", getLogin())
      .addTransition("multi", "step2")
      .done();

    definition = getManagementAPI().deploy(getBusinessArchive(definition, null, ErrorConnector.class,
        NoContextMulitpleActivitiesInstantiator.class, FixedNumberJoinChecker.class));

    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(definition.getUUID());
    checkState(instanceUUID, ActivityState.FAILED, "multi", nbMulti);

    final Set<LightActivityInstance> activityInstances = getQueryRuntimeAPI().getLightActivityInstances(instanceUUID, "multi");
    final LightActivityInstance activityInstance = activityInstances.iterator().next();
    getRuntimeAPI().skip(activityInstance.getUUID(), null);

    checkState(instanceUUID, ActivityState.SKIPPED, "multi", nbMulti);
    checkState(instanceUUID, ActivityState.READY, "step2");
    checkState(instanceUUID, InstanceState.STARTED);

    getManagementAPI().deleteProcess(definition.getUUID());
  }

  public void testSkipFailedLoopActivity() throws Exception {
    ProcessDefinition definition = 
      ProcessBuilder.createProcess("loopState", "1.0")
      .addIntegerData("counter", 0)
      .addHuman(getLogin())
      .addHumanTask("loop", getLogin())
      .addConnector(Event.taskOnReady, ErrorConnector.class.getName(), true)
      .addLoop("counter != 1", true)
      .addSystemTask("end")
      .addTransition("loop", "end")
      .done();

    definition = getManagementAPI().deploy(getBusinessArchive(definition, null, ErrorConnector.class));

    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(definition.getUUID());

    checkState(instanceUUID, ActivityState.FAILED, "loop");

    final Set<LightActivityInstance> activityInstances = getQueryRuntimeAPI().getLightActivityInstances(instanceUUID, "loop");
    final LightActivityInstance activityInstance = activityInstances.iterator().next();
    getRuntimeAPI().skip(activityInstance.getUUID(), null);

    checkState(instanceUUID, ActivityState.SKIPPED, "loop");
    checkState(instanceUUID, ActivityState.FINISHED, "end");
    checkState(instanceUUID, InstanceState.FINISHED);

    getManagementAPI().deleteProcess(definition.getUUID());
  }
  
  public void testInstanciateProcessInstanceActivityUUID() throws Exception {
    final String varName = "intVar";
    ProcessDefinition definition = 
      ProcessBuilder.createProcess("process", "2.0")
      .addIntegerData(varName, 0)
      .addHuman(getLogin())
      .addHumanTask("step1", getLogin())
      .addHumanTask("step3", getLogin())
      .addHumanTask("step2", getLogin())
        .addConnector(Event.taskOnReady, SetVarConnector.class.getName(), true)
        .addInputParameter("variableName", varName)
        .addInputParameter("value", 2)
      .done();

    definition = getManagementAPI().deploy(getBusinessArchive(definition, null, SetVarConnector.class));
    
    final ActivityDefinition step2 = definition.getActivity("step2");
    
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(definition.getUUID(), step2.getUUID());

    final Collection<LightTaskInstance> taskList = getQueryRuntimeAPI().getLightTaskList(ActivityState.READY);
    assertNotNull(taskList);
    assertEquals(1, taskList.size());
    assertEquals("step2", taskList.iterator().next().getActivityName());

    final Integer intVar = (Integer) getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, varName);
    assertEquals(new Integer(2), intVar);
    
    getManagementAPI().deleteProcess(definition.getUUID());
  }

}
