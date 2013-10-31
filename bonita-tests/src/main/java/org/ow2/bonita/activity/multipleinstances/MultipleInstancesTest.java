package org.ow2.bonita.activity.multipleinstances;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bonitasoft.connectors.bonita.instantiators.VariableInstantiator;
import org.bonitasoft.connectors.bonita.joincheckers.FixedNumberJoinChecker;
import org.bonitasoft.connectors.bonita.joincheckers.PercentageJoinChecker;
import org.bonitasoft.connectors.bonita.resolvers.UserListRoleResolver;
import org.ow2.bonita.APITestCase;
import org.ow2.bonita.activity.instantiation.NullContextInitiator;
import org.ow2.bonita.activity.multipleinstances.instantiator.EmptyContextInitiator;
import org.ow2.bonita.activity.multipleinstances.instantiator.NoContextMulitpleActivitiesInstantiator;
import org.ow2.bonita.activity.multipleinstances.instantiator.NullMulitpleActivitiesInstantiator;
import org.ow2.bonita.activity.multipleinstances.instantiator.SimpleContextInstantiator;
import org.ow2.bonita.activity.multipleinstances.instantiator.TwoDifferentContextsInitiator;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.runtime.ActivityState;
import org.ow2.bonita.facade.runtime.InstanceState;
import org.ow2.bonita.facade.runtime.ProcessInstance;
import org.ow2.bonita.facade.runtime.TaskInstance;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.light.LightActivityInstance;
import org.ow2.bonita.light.LightTaskInstance;
import org.ow2.bonita.util.BonitaRuntimeException;
import org.ow2.bonita.util.ProcessBuilder;

public class MultipleInstancesTest extends APITestCase {

  public void testQuickSubProcess() throws Exception {
    ProcessDefinition sub = ProcessBuilder.createProcess("sub", "1.0")
    .addSystemTask("subStep")
    .done();

    ProcessDefinition main = ProcessBuilder.createProcess("main", "1.0")
    .addHuman(getLogin())
    .addStringData("voter")
    .addSubProcess("firstStep", "sub", "1.0")
    .addMultipleActivitiesInstantiator(NoContextMulitpleActivitiesInstantiator.class.getName())
    .addInputParameter("number", 8)
    .addMultipleActivitiesJoinChecker(PercentageJoinChecker.class.getName())
    .addInputParameter("percentage", 1.0)
    .addHumanTask("secondStep", getLogin())
    .addTransition("firstStep", "secondStep")
    .done();

    sub = getManagementAPI().deploy(getBusinessArchive(sub));
    main = getManagementAPI().deploy(getBusinessArchive(main, null, NoContextMulitpleActivitiesInstantiator.class, PercentageJoinChecker.class));

    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(main.getUUID());

    assertEquals(9, getQueryRuntimeAPI().getLightActivityInstances(instanceUUID).size());
    assertEquals(8, getQueryRuntimeAPI().getLightActivityInstances(instanceUUID, "firstStep").size());
    assertEquals(1, getQueryRuntimeAPI().getLightActivityInstances(instanceUUID, "secondStep").size());

    final Set<LightActivityInstance> activities = getQueryRuntimeAPI().getLightActivityInstances(instanceUUID, "firstStep");
    for (LightActivityInstance firstStep : activities) {
      assertEquals(ActivityState.FINISHED, firstStep.getState());
    }
    getManagementAPI().deleteProcess(main.getUUID());
    getRuntimeAPI().deleteAllProcessInstances(sub.getUUID());
    getManagementAPI().deleteProcess(sub.getUUID());
  }

  public void test100PercentMultiInstantiationProcess() throws Exception {
    ProcessDefinition main = ProcessBuilder.createProcess("main", "1.0")
    .addHuman(getLogin())
    .addStringData("voter")
    .addSystemTask("firstStep")
    .addMultipleActivitiesInstantiator(NoContextMulitpleActivitiesInstantiator.class.getName())
    .addInputParameter("number", 8)
    .addMultipleActivitiesJoinChecker(PercentageJoinChecker.class.getName())
    .addInputParameter("percentage", 1.0)
    .addHumanTask("secondStep", getLogin())
    .addTransition("firstStep", "secondStep")
    .done();

    main = getManagementAPI().deploy(getBusinessArchive(main, null, NoContextMulitpleActivitiesInstantiator.class, PercentageJoinChecker.class));

    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(main.getUUID());

    assertEquals(9, getQueryRuntimeAPI().getLightActivityInstances(instanceUUID).size());
    assertEquals(8, getQueryRuntimeAPI().getLightActivityInstances(instanceUUID, "firstStep").size());
    assertEquals(1, getQueryRuntimeAPI().getLightActivityInstances(instanceUUID, "secondStep").size());

    final Set<LightActivityInstance> activities = getQueryRuntimeAPI().getLightActivityInstances(instanceUUID, "firstStep");
    for (LightActivityInstance firstStep : activities) {
      assertEquals(ActivityState.FINISHED, firstStep.getState());
    }
    getManagementAPI().deleteProcess(main.getUUID());
  }

  public void testMulitpleActivities() throws Exception {
    ProcessDefinition definition =
      ProcessBuilder.createProcess("multiple", "1.0")
      .addHuman(getLogin())
      .addHumanTask("multi", getLogin())
      .addMultipleActivitiesInstantiator(NoContextMulitpleActivitiesInstantiator.class.getName())
      .addInputParameter("number", 10)
      .addMultipleActivitiesJoinChecker(FixedNumberJoinChecker.class.getName())
      .addInputParameter("activityNumber", 3)
      .done();

    definition = getManagementAPI().deploy(getBusinessArchive(definition, null,
        NoContextMulitpleActivitiesInstantiator.class, FixedNumberJoinChecker.class));
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(definition.getUUID());
    Collection<TaskInstance> tasks = getQueryRuntimeAPI().getTaskList(getLogin(), ActivityState.READY);
    assertEquals(10, tasks.size());
    Iterator<TaskInstance> iterator = tasks.iterator();
    for (int i = 0; i < 3; i++) {
      TaskInstance task = iterator.next();
      executeTask(instanceUUID, task.getActivityName());
    }
    tasks = getQueryRuntimeAPI().getTaskList(getLogin(), ActivityState.READY);
    assertEquals(0, tasks.size());

    ProcessInstance instance = getQueryRuntimeAPI().getProcessInstance(instanceUUID);
    assertNotNull(instance.getEndedDate());
    getManagementAPI().deleteProcess(definition.getUUID());
  }

  public void testSimpleMulitpleActivities() throws Exception {
    ProcessDefinition definition =
      ProcessBuilder.createProcess("multiple", "1.0")
      .addHuman(getLogin())
      .addHumanTask("multi", getLogin())
      .addMultipleActivitiesInstantiator(NoContextMulitpleActivitiesInstantiator.class.getName())
      .addInputParameter("number", 10)
      .addMultipleActivitiesJoinChecker(FixedNumberJoinChecker.class.getName())
      .addInputParameter("activityNumber", 3)
      .addHumanTask("next", getLogin())
      .addTransition("multi", "next")
      .done();

    definition = getManagementAPI().deploy(getBusinessArchive(definition, null,
        NoContextMulitpleActivitiesInstantiator.class, FixedNumberJoinChecker.class));
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(definition.getUUID());
    Collection<TaskInstance> tasks = getQueryRuntimeAPI().getTaskList(getLogin(), ActivityState.READY);
    assertEquals(10, tasks.size());
    Iterator<TaskInstance> iterator = tasks.iterator();
    for (int i = 0; i < 3; i++) {
      TaskInstance task = iterator.next();
      executeTask(instanceUUID, task.getActivityName());
    }
    tasks = getQueryRuntimeAPI().getTaskList(getLogin(), ActivityState.READY);
    assertEquals(1, tasks.size());
    assertEquals("next", tasks.iterator().next().getActivityName());
    getManagementAPI().deleteProcess(definition.getUUID());
  }

  public void testMulitActivitiesInACycle() throws Exception {
    ProcessDefinition definition =
      ProcessBuilder.createProcess("multipleCycle", "1.0")
      .addHuman(getLogin())
      .addHumanTask("multi", getLogin())
      .addMultipleActivitiesInstantiator(NoContextMulitpleActivitiesInstantiator.class.getName())
      .addInputParameter("number", 5)
      .addMultipleActivitiesJoinChecker(FixedNumberJoinChecker.class.getName())
      .addInputParameter("activityNumber", 3)
      .addHumanTask("first", getLogin())
      .addSystemTask("start")
      .addTransition("first", "multi")
      .addTransition("multi", "first")
      .addTransition("start", "first")
      .done();

    definition = getManagementAPI().deploy(getBusinessArchive(definition, null,
        NoContextMulitpleActivitiesInstantiator.class, FixedNumberJoinChecker.class));
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(definition.getUUID());

    executeTask(instanceUUID, "first");
    Collection<TaskInstance> tasks = getQueryRuntimeAPI().getTaskList(getLogin(), ActivityState.READY);
    assertEquals(5, tasks.size());
    Iterator<TaskInstance> iterator = tasks.iterator();
    for (int i = 0; i < 3; i++) {
      TaskInstance task = iterator.next();
      executeTask(instanceUUID, task.getActivityName());
    }
    executeTask(instanceUUID, "first");

    tasks = getQueryRuntimeAPI().getTasks(instanceUUID);
    assertEquals(12, tasks.size());

    tasks = getQueryRuntimeAPI().getTaskList(getLogin(), ActivityState.READY);
    assertEquals(5, tasks.size());
    for (TaskInstance task : tasks) {
      assertFalse("first".equals(task.getActivityName()));
    }
    getManagementAPI().deleteProcess(definition.getUUID());
  }

  public void testMapMultipleActivityInstancesVariablesToSubflowVariables() throws Exception {
    ProcessDefinition subProcess =
      ProcessBuilder.createProcess("sub", "2.3")
      .addHuman(getLogin())
      .addIntegerData("x", 0)
      .addIntegerData("y", 0)
      .addIntegerData("z", 10)
      .addHumanTask("subTask", getLogin())
      .done();

    ProcessDefinition process =
      ProcessBuilder.createProcess("process", "1.5")
      .addStringData("text", "kaksi")
      .addIntegerData("number", 2)
      .addHuman(getLogin())
      .addSubProcess("yellow", "sub")
      .addSubProcessInParameter("a", "x")
      .addSubProcessInParameter("b", "y")
      .addMultipleActivitiesInstantiator(TwoDifferentContextsInitiator.class.getName())
      .addMultipleActivitiesJoinChecker(FixedNumberJoinChecker.class.getName())
      .addInputParameter("number", 2)
      .done();

    subProcess = getManagementAPI().deploy(getBusinessArchive(subProcess));
    process = getManagementAPI().deploy(getBusinessArchive(process, null,
        TwoDifferentContextsInitiator.class, FixedNumberJoinChecker.class));

    getRuntimeAPI().instantiateProcess(process.getUUID());
    Collection<TaskInstance> tasks = getQueryRuntimeAPI().getTaskList(ActivityState.READY);
    assertEquals(2, tasks.size());
    for (TaskInstance task : tasks) {
      Integer x = (Integer) getQueryRuntimeAPI().getProcessInstanceVariable(task.getProcessInstanceUUID(), "x");
      Integer y = (Integer) getQueryRuntimeAPI().getProcessInstanceVariable(task.getProcessInstanceUUID(), "y");
      assertTrue(x == 1 && y == 1 || x == 2 && y == 2);
    }
    getManagementAPI().deleteProcess(process.getUUID());
    getManagementAPI().deleteProcess(subProcess.getUUID());
  }

  public void testNullInitiator() throws Exception {
    ProcessDefinition definition =
      ProcessBuilder.createProcess("nullinit", "1.0")
      .addHuman(getLogin())
      .addHumanTask("multi", getLogin())
      .addMultipleActivitiesInstantiator(NullMulitpleActivitiesInstantiator.class.getName())
      .addMultipleActivitiesJoinChecker(FixedNumberJoinChecker.class.getName())
      .addInputParameter("number", 3)
      .done();

    definition = getManagementAPI().deploy(getBusinessArchive(definition, null,
        NullMulitpleActivitiesInstantiator.class, FixedNumberJoinChecker.class));
    try {
      getRuntimeAPI().instantiateProcess(definition.getUUID());
      fail("The initial context is null");
    } catch (BonitaRuntimeException e) {
    } 
    getManagementAPI().deleteProcess(definition.getUUID());
  }

  public void testEmptyInitiator() throws Exception {
    ProcessDefinition definition =
      ProcessBuilder.createProcess("nullinit", "1.0")
      .addHuman(getLogin())
      .addHumanTask("multi", getLogin())
      .addMultipleActivitiesInstantiator(EmptyContextInitiator.class.getName())
      .addMultipleActivitiesJoinChecker(FixedNumberJoinChecker.class.getName())
      .addInputParameter("number", 3)
      .done();

    definition = getManagementAPI().deploy(getBusinessArchive(definition, null,
        EmptyContextInitiator.class, FixedNumberJoinChecker.class));
    try {
      getRuntimeAPI().instantiateProcess(definition.getUUID());
      fail("The initial context is empty");
    } catch (BonitaRuntimeException e) {
    } 
    getManagementAPI().deleteProcess(definition.getUUID());
  }

  public void testJoinNeverReached() throws Exception {
    ProcessDefinition definition =
      ProcessBuilder.createProcess("mulitple", "1.0")
      .addHuman(getLogin())
      .addHumanTask("multi", getLogin())
      .addMultipleActivitiesInstantiator(NoContextMulitpleActivitiesInstantiator.class.getName())
      .addInputParameter("number", 3)
      .addMultipleActivitiesJoinChecker(FixedNumberJoinChecker.class.getName())
      .addInputParameter("activityNumber", 5)
      .done();

    definition = getManagementAPI().deploy(getBusinessArchive(definition, null,
        NoContextMulitpleActivitiesInstantiator.class, FixedNumberJoinChecker.class));
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(definition.getUUID());
    Collection<TaskInstance> tasks = getQueryRuntimeAPI().getTaskList(getLogin(), ActivityState.READY);
    assertEquals(3, tasks.size());
    final Iterator<TaskInstance> iterator = tasks.iterator();
    for (int i = 0; i < 3; i++) {
      TaskInstance task = iterator.next();
      executeTask(instanceUUID, task.getActivityName());
    }
    final ProcessInstance instance = getQueryRuntimeAPI().getProcessInstance(instanceUUID);
    assertEquals(InstanceState.FINISHED, instance.getInstanceState());
    tasks = getQueryRuntimeAPI().getTaskList(getLogin(), ActivityState.READY);
    assertEquals(0, tasks.size());
    
    getManagementAPI().deleteProcess(definition.getUUID());
  }

  public void testNullContextInitiator() throws Exception {
    ProcessDefinition definition =
      ProcessBuilder.createProcess("nullinit", "1.0")
      .addHuman(getLogin())
      .addHumanTask("multi", getLogin())
      .addMultipleActivitiesInstantiator(NullContextInitiator.class.getName())
      .addMultipleActivitiesJoinChecker(FixedNumberJoinChecker.class.getName())
      .addInputParameter("number", 1)
      .done();

    definition = getManagementAPI().deploy(getBusinessArchive(definition, null,
        NullContextInitiator.class, FixedNumberJoinChecker.class));

    getRuntimeAPI().instantiateProcess(definition.getUUID());
    Collection<TaskInstance> tasks = getQueryRuntimeAPI().getTaskList(getLogin(), ActivityState.READY);
    assertEquals(2, tasks.size());

    getManagementAPI().deleteProcess(definition.getUUID());
  }

  public void testCreateContext() throws Exception {
    ProcessDefinition definition =
      ProcessBuilder.createProcess("context", "3.2")
      .addHuman(getLogin())
      .addHumanTask("multi", getLogin())
      .addMultipleActivitiesInstantiator(SimpleContextInstantiator.class.getName())
      .addMultipleActivitiesJoinChecker(FixedNumberJoinChecker.class.getName())
      .addInputParameter("activityNumber", 3)
      .addHumanTask("next", getLogin())
      .addTransition("multi", "next")
      .done();

    definition = getManagementAPI().deploy(getBusinessArchive(definition, null,
        SimpleContextInstantiator.class, FixedNumberJoinChecker.class));
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(definition.getUUID());
    Collection<TaskInstance> tasks = getQueryRuntimeAPI().getTaskList(getLogin(), ActivityState.READY);
    assertEquals(3, tasks.size());
    for (TaskInstance task : tasks) {
      Map<String, Object> variables = getQueryRuntimeAPI().getVariables(task.getUUID());
      int variableNumber = variables.size();
      switch (variableNumber) {
        case 0:
          break;
        case 1:
          assertEquals(10, variables.get("a"));
          break;
        case 2:
          assertEquals(50, variables.get("a"));
          assertEquals("text", variables.get("b"));
          break;
        default:
          fail("");
          break;
      }
      executeTask(instanceUUID, task.getActivityName());
    }
    tasks = getQueryRuntimeAPI().getTaskList(getLogin(), ActivityState.READY);
    assertEquals(1, tasks.size());
    assertEquals("next", tasks.iterator().next().getActivityName());
    getManagementAPI().deleteProcess(definition.getUUID());
  }

  public void testVoting() throws Exception {
    List<String> users = new ArrayList<String>();
    users.add("john");
    users.add("jack");
    users.add("james");

    ProcessDefinition vote = ProcessBuilder.createProcess("vote", "1.0")
    .addStringData("voter")
    .addGroup("people")
    .addGroupResolver(UserListRoleResolver.class.getName())
    .addInputParameter("users", "${voter}")
    .addHumanTask("vote", "people")
    .done();

    ProcessDefinition main = ProcessBuilder.createProcess("main", "1.0")
    .addHuman(getLogin())
    .addStringData("voter")
    .addSubProcess("firstStep", "vote", "1.0")
    .addSubProcessInParameter("voter", "voter")
    .addMultipleActivitiesInstantiator(VariableInstantiator.class.getName())
    .addInputParameter("name", "voter")
    .addInputParameter("values", users)
    .addMultipleActivitiesJoinChecker(FixedNumberJoinChecker.class.getName())
    .addInputParameter("activityNumber", 2)
    .addHumanTask("secondStep", getLogin())
    .addTransition("firstStep", "secondStep")
    .done();

    vote = getManagementAPI().deploy(getBusinessArchive(vote, null, UserListRoleResolver.class));
    main = getManagementAPI().deploy(getBusinessArchive(main, null, VariableInstantiator.class, FixedNumberJoinChecker.class));

    getRuntimeAPI().instantiateProcess(main.getUUID());
    loginAs("john", "bpm");
    Collection<TaskInstance> tasks = getQueryRuntimeAPI().getTaskList(ActivityState.READY);
    assertEquals(1, tasks.size());
    getRuntimeAPI().startTask(tasks.iterator().next().getUUID(), true);
    getRuntimeAPI().finishTask(tasks.iterator().next().getUUID(), true);

    tasks = getQueryRuntimeAPI().getTaskList(ActivityState.READY);
    assertEquals(0, tasks.size());

    loginAs(getLogin(), getPassword());
    tasks = getQueryRuntimeAPI().getTaskList(ActivityState.READY);
    assertEquals(0, tasks.size());

    loginAs("james", "bpm");
    tasks = getQueryRuntimeAPI().getTaskList(ActivityState.READY);
    assertEquals(1, tasks.size());
    getRuntimeAPI().startTask(tasks.iterator().next().getUUID(), true);
    getRuntimeAPI().finishTask(tasks.iterator().next().getUUID(), true);

    loginAs(getLogin(), getPassword());
    tasks = getQueryRuntimeAPI().getTaskList(ActivityState.READY);
    assertEquals(1, tasks.size());
    assertEquals("secondStep", tasks.iterator().next().getActivityName());

    getManagementAPI().deleteProcess(main.getUUID());
    getManagementAPI().deleteProcess(vote.getUUID());
  }

  public void testVotingTwoInstances() throws Exception {
    List<String> users = new ArrayList<String>();
    users.add("john");
    users.add("jack");
    users.add("james");

    ProcessDefinition vote = ProcessBuilder.createProcess("vote", "1.0")
    .addStringData("voter")
    .addGroup("people")
    .addGroupResolver(UserListRoleResolver.class.getName())
    .addInputParameter("users", "${voter}")
    .addHumanTask("vote", "people")
    .done();

    ProcessDefinition main = ProcessBuilder.createProcess("main", "1.0")
    .addHuman(getLogin())
    .addStringData("voter")
    .addSubProcess("firstStep", "vote", "1.0")
    .addSubProcessInParameter("voter", "voter")
    .addMultipleActivitiesInstantiator(VariableInstantiator.class.getName())
    .addInputParameter("name", "voter")
    .addInputParameter("values", users)
    .addMultipleActivitiesJoinChecker(FixedNumberJoinChecker.class.getName())
    .addInputParameter("activityNumber", 2)
    .addHumanTask("secondStep", getLogin())
    .addTransition("firstStep", "secondStep")
    .done();

    vote = getManagementAPI().deploy(getBusinessArchive(vote, null, UserListRoleResolver.class));
    main = getManagementAPI().deploy(getBusinessArchive(main, null, VariableInstantiator.class, FixedNumberJoinChecker.class));

    ProcessInstanceUUID instance1 = getRuntimeAPI().instantiateProcess(main.getUUID());
    ProcessInstanceUUID instance2 = getRuntimeAPI().instantiateProcess(main.getUUID());
    loginAs("john", "bpm");
    Collection<TaskInstance> tasks = getQueryRuntimeAPI().getTaskList(ActivityState.READY);
    assertEquals(2, tasks.size());
    Iterator<TaskInstance> iterator = tasks.iterator();
    TaskInstance task = iterator.next();
    if (task.getRootInstanceUUID().equals(instance2)) {
      task = iterator.next();
    }
    assertEquals(instance1, task.getRootInstanceUUID());
    getRuntimeAPI().startTask(task.getUUID(), true);
    getRuntimeAPI().finishTask(task.getUUID(), true);

    tasks = getQueryRuntimeAPI().getTaskList(ActivityState.READY);
    assertEquals(1, tasks.size());

    loginAs(getLogin(), getPassword());
    tasks = getQueryRuntimeAPI().getTaskList(ActivityState.READY);
    assertEquals(0, tasks.size());

    loginAs("james", "bpm");
    tasks = getQueryRuntimeAPI().getTaskList(ActivityState.READY);
    assertEquals(2, tasks.size());
    iterator = tasks.iterator();
    task = iterator.next();
    if (task.getRootInstanceUUID().equals(instance2)) {
      task = iterator.next();
    }
    assertEquals(instance1, task.getRootInstanceUUID());
    getRuntimeAPI().startTask(task.getUUID(), true);
    getRuntimeAPI().finishTask(task.getUUID(), true);

    loginAs(getLogin(), getPassword());
    tasks = getQueryRuntimeAPI().getTaskList(ActivityState.READY);
    assertEquals(1, tasks.size());
    task = tasks.iterator().next();
    assertEquals("secondStep", task.getActivityName());
    assertEquals(instance1, task.getProcessInstanceUUID());

    getManagementAPI().deleteProcess(main.getUUID());
    getRuntimeAPI().deleteAllProcessInstances(vote.getUUID());
    getManagementAPI().deleteProcess(vote.getUUID());
  }

  public void testVotingPercentageTwoInstances() throws Exception {
    List<String> users = new ArrayList<String>();
    users.add("john");
    users.add("jack");
    users.add("james");

    ProcessDefinition vote = ProcessBuilder.createProcess("vote", "1.0")
    .addStringData("voter")
    .addGroup("people")
    .addGroupResolver(UserListRoleResolver.class.getName())
    .addInputParameter("users", "${voter}")
    .addHumanTask("vote", "people")
    .done();

    ProcessDefinition main = ProcessBuilder.createProcess("main", "1.0")
    .addHuman(getLogin())
    .addStringData("voter")
    .addSubProcess("firstStep", "vote", "1.0")
    .addSubProcessInParameter("voter", "voter")
    .addMultipleActivitiesInstantiator(VariableInstantiator.class.getName())
    .addInputParameter("name", "voter")
    .addInputParameter("values", users)
    .addMultipleActivitiesJoinChecker(PercentageJoinChecker.class.getName())
    .addInputParameter("percentage", 0.66)
    .addHumanTask("secondStep", getLogin())
    .addTransition("firstStep", "secondStep")
    .done();

    vote = getManagementAPI().deploy(getBusinessArchive(vote, null, UserListRoleResolver.class));
    main = getManagementAPI().deploy(getBusinessArchive(main, null, VariableInstantiator.class, PercentageJoinChecker.class));

    ProcessInstanceUUID instance1 = getRuntimeAPI().instantiateProcess(main.getUUID());
    ProcessInstanceUUID instance2 = getRuntimeAPI().instantiateProcess(main.getUUID());
    loginAs("john", "bpm");
    Collection<TaskInstance> tasks = getQueryRuntimeAPI().getTaskList(ActivityState.READY);
    assertEquals(2, tasks.size());
    Iterator<TaskInstance> iterator = tasks.iterator();
    TaskInstance task = iterator.next();
    if (task.getRootInstanceUUID().equals(instance2)) {
      task = iterator.next();
    }
    assertEquals(instance1, task.getRootInstanceUUID());
    getRuntimeAPI().startTask(task.getUUID(), true);
    getRuntimeAPI().finishTask(task.getUUID(), true);

    tasks = getQueryRuntimeAPI().getTaskList(ActivityState.READY);
    assertEquals(1, tasks.size());

    loginAs(getLogin(), getPassword());
    tasks = getQueryRuntimeAPI().getTaskList(ActivityState.READY);
    assertEquals(0, tasks.size());

    loginAs("james", "bpm");
    tasks = getQueryRuntimeAPI().getTaskList(ActivityState.READY);
    assertEquals(2, tasks.size());
    iterator = tasks.iterator();
    task = iterator.next();
    if (task.getRootInstanceUUID().equals(instance2)) {
      task = iterator.next();
    }
    assertEquals(instance1, task.getRootInstanceUUID());
    getRuntimeAPI().startTask(task.getUUID(), true);
    getRuntimeAPI().finishTask(task.getUUID(), true);

    loginAs(getLogin(), getPassword());
    tasks = getQueryRuntimeAPI().getTaskList(ActivityState.READY);
    assertEquals(1, tasks.size());
    task = tasks.iterator().next();
    assertEquals("secondStep", task.getActivityName());
    assertEquals(instance1, task.getProcessInstanceUUID());

    getManagementAPI().deleteProcess(main.getUUID());
    getRuntimeAPI().deleteAllProcessInstances(vote.getUUID());
    getManagementAPI().deleteProcess(vote.getUUID());
  }


  public void testGetActivityInstances() throws Exception {
    ProcessDefinition sub = ProcessBuilder.createProcess("sub", "0")
    .addHuman(getLogin())
    .addHumanTask("SubStep", getLogin())
    .done();

    ProcessDefinition process = ProcessBuilder.createProcess("process", "0")
    .addHuman(getLogin())
    .addSystemTask("start")
    .addSubProcess("firstStep", "sub", "0")
      .addMultipleActivitiesInstantiator(NoContextMulitpleActivitiesInstantiator.class.getName())
        .addInputParameter("number", 8)
      .addMultipleActivitiesJoinChecker(PercentageJoinChecker.class.getName())
        .addInputParameter("percentage", 1.0)
    .addHumanTask("secondStep", getLogin())
    .addTransition("start", "firstStep")
    .addTransition("firstStep", "secondStep")
    .done();

    sub = getManagementAPI().deploy(getBusinessArchive(sub));
    process = getManagementAPI().deploy(getBusinessArchive(process, null, NoContextMulitpleActivitiesInstantiator.class, PercentageJoinChecker.class));

    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(process.getUUID());

    Set<LightActivityInstance> tasks = getQueryRuntimeAPI().getLightActivityInstances(instanceUUID);
    assertEquals(9, tasks.size());
    Iterator<LightActivityInstance> iterator = tasks.iterator();
    LightActivityInstance task = iterator.next();
    if ("start".equals(task.getActivityName())) {
      task = iterator.next();
    }
    Set<LightActivityInstance> activities = getQueryRuntimeAPI().getLightActivityInstances(instanceUUID, task.getActivityName(), task.getIterationId());
    assertEquals(8, activities.size());

    getManagementAPI().deleteProcess(process.getUUID());
    getRuntimeAPI().deleteAllProcessInstances(sub.getUUID());
    getManagementAPI().deleteProcess(sub.getUUID());
  }

  public void testMICanUseUserTaskVariables() throws Exception {
    final List<String> users = new ArrayList<String>();
    users.add("jack");
    users.add("james");

    ProcessDefinition definition = ProcessBuilder.createProcess("ActorSelectorDiagram", "1.0")
        .addObjectData("listOfStepActors", List.class.getName(), "${[\"jack\", \"james\"]}")
        .addGroup("Users")
          .addGroupResolver(UserListRoleResolver.class.getName())
            .addInputParameter("users", "${actorUsername}")
        .addHumanTask("Step1", "Users")
          .addMultipleActivitiesInstantiator(VariableInstantiator.class.getName())
            .addInputParameter("name", "actorUsername")
            .addInputParameter("values", "${listOfStepActors}")
          .addMultipleActivitiesJoinChecker(PercentageJoinChecker.class.getName())
            .addInputParameter("percentage", 1.0)
        .done();

    definition = getManagementAPI().deploy(getBusinessArchive(definition, null, UserListRoleResolver.class, VariableInstantiator.class, PercentageJoinChecker.class));
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(definition.getUUID());

    final Set<LightActivityInstance> activityInstances = getQueryRuntimeAPI().getLightActivityInstances(instanceUUID);
    assertEquals(2, activityInstances.size());

    loginAs("james", "bpm");

    final Collection<LightTaskInstance> tasks = getQueryRuntimeAPI().getLightTaskList(instanceUUID, ActivityState.READY);
    assertEquals(1, tasks.size());
    final LightTaskInstance task = tasks.iterator().next();
    assertEquals("Step1", task.getActivityName());

    getManagementAPI().deleteProcess(definition.getUUID());
  }

}
