package org.ow2.bonita.activity.route;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ow2.bonita.APITestCase;
import org.ow2.bonita.facade.def.majorElement.ActivityDefinition.JoinType;
import org.ow2.bonita.facade.def.majorElement.ActivityDefinition.SplitType;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.exception.InstanceNotFoundException;
import org.ow2.bonita.facade.runtime.ActivityInstance;
import org.ow2.bonita.facade.runtime.ActivityState;
import org.ow2.bonita.facade.runtime.InstanceState;
import org.ow2.bonita.facade.runtime.ProcessInstance;
import org.ow2.bonita.facade.runtime.TaskInstance;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.light.LightActivityInstance;
import org.ow2.bonita.light.LightProcessInstance;
import org.ow2.bonita.light.LightTaskInstance;
import org.ow2.bonita.util.BonitaException;
import org.ow2.bonita.util.ProcessBuilder;

public class JoinXorTest extends APITestCase {
  
  public void testJoinXorWithCancelPropagation() throws Exception {
    ProcessDefinition definition = ProcessBuilder.createProcess("manyCycle", "3.2")
      .addHuman(getLogin())
      .addSystemTask("start")
      .addHumanTask("a1", getLogin())
      .addHumanTask("a2", getLogin())
      .addHumanTask("b", getLogin())
      .addHumanTask("join", getLogin())
      .addHumanTask("end", getLogin())
      
      .addTransition("start", "a1")
      .addTransition("start", "b")
      .addTransition("a1", "a2")
      .addTransition("a2", "join")
      .addTransition("b", "join")
      .addTransition("join", "end")
      .done();

    final ProcessDefinition process = getManagementAPI().deploy(getBusinessArchive(definition));
    final ProcessDefinitionUUID processUUID = process.getUUID();
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    //checks a1 and b are in the todo
    checkTodo("a1", "b");
    executeTask(instanceUUID, "b");

    //checks join is in the todo
    checkTodo("join");
    executeTask(instanceUUID, "join");
    
    //checks end is in the todo
    checkTodo("end");
    executeTask(instanceUUID, "end");
    
    assertEquals(InstanceState.FINISHED, getQueryRuntimeAPI().getLightProcessInstance(instanceUUID).getInstanceState());
    
    this.getManagementAPI().deleteProcess(processUUID);
  }
  
  private void checkTodo(final String... activityNames) {
    final Collection<LightTaskInstance> tasks = getQueryRuntimeAPI().getLightTaskList(ActivityState.READY);
    final Set<String> activitySet = new HashSet<String>();
    for (String s : activityNames) {
      activitySet.add(s);
    }
    assertEquals(tasks.toString(), activitySet.size(), tasks.size());
    final Iterator<LightTaskInstance> it = tasks.iterator();
    while (it.hasNext()) {
      final String taskName = it.next().getActivityName();
      assertTrue(taskName + " found but not expected", activitySet.contains(taskName));
    }
  }
  
  private void checkReadyActivities(ProcessInstanceUUID instanceUUID, String... activityNames)
  throws InstanceNotFoundException {
    Collection<LightTaskInstance> tasks = getQueryRuntimeAPI().getLightTaskList(instanceUUID, ActivityState.READY);
    List<String> activities = new ArrayList<String>();
    for (int i = 0; i < activityNames.length; i++) {
      activities.add(activityNames[i]);
    }
    assertEquals(activityNames.length, tasks.size());
    for (LightTaskInstance task : tasks) {
      String taskName = task.getActivityName();
      assertTrue(taskName + " is not available in the user task list", activities.contains(taskName));
    }
  }
  
  private void checkActivityStates(ProcessInstanceUUID instanceUUID, Map<String, ActivityState> activities)
  throws InstanceNotFoundException {
    Set<LightTaskInstance> tasks = getQueryRuntimeAPI().getLightTasks(instanceUUID);
    assertEquals(activities.size(), tasks.size());
    for (LightTaskInstance task : tasks) {
      String taskName = task.getActivityName();
      ActivityState expectedTaskState = activities.get(taskName);
      assertEquals(expectedTaskState, task.getState());
    }
  }

  private String getTaskIteration(ProcessInstanceUUID instanceUUID, String taskName) {
    Collection<LightTaskInstance> tasks = getQueryRuntimeAPI().getLightTaskList(ActivityState.READY);
    for (LightTaskInstance taskInstance : tasks) {
      if (taskName.equals(taskInstance.getActivityName())) {
        return taskInstance.getIterationId();
      }
    }
    return "it1";
  }

  private String getTaskLoopId(ProcessInstanceUUID instanceUUID, String taskName) {
    Collection<LightTaskInstance> tasks = getQueryRuntimeAPI().getLightTaskList(ActivityState.READY);
    for (LightTaskInstance taskInstance : tasks) {
      if (taskName.equals(taskInstance.getActivityName())) {
        return taskInstance.getLoopId();
      }
    }
    return "noLoop";
  }

  private void checkActivityStates(ProcessInstanceUUID instanceUUID, List<Activity> activities)
  throws InstanceNotFoundException {
    Set<LightActivityInstance> lightActivities = getQueryRuntimeAPI().getLightActivityInstances(instanceUUID);
    assertEquals(activities.size(), lightActivities.size());
    for (LightActivityInstance lightActivity : lightActivities) {
      String activityName = lightActivity.getActivityName();
      Activity activity = searchActivity(activities, activityName, lightActivity.getIterationId(), lightActivity.getLoopId());
      assertNotNull(activityName + ": not found", activity);
      ActivityState expectedTaskState = activity.getState();
      assertEquals(activityName, expectedTaskState, lightActivity.getState());
    }
  }

  private Activity searchActivity(List<Activity> activities, String name, String iteration, String loop) {
    for (Activity activity : activities) {
      if (activity.getName().equals(name)
          && activity.getIteration().equals(iteration)
          && activity.getLoop().equals(loop)) {
        return activity;
      }
    }
    return null;
  }

  private class Activity {

    private String name;
    private String iteration;
    private  String loop;
    private ActivityState state;

    public Activity(String name, String iteration, ActivityState state) {
      this.name = name;
      this.iteration = iteration;
      this.state = state;
      this.loop = "noLoop";
    }

    public Activity(String name, String iteration, String loop, ActivityState state) {
      this.name = name;
      this.iteration = iteration;
      this.state = state;
      this.loop = loop;
    }

    public String getName() {
      return name;
    }

    public String getIteration() {
      return iteration;
    }

    public String getLoop() {
      return loop;
    }
    
    public ActivityState getState() {
      return state;
    }

  }
  
  public void testJoinXor1Tr() throws BonitaException {
    URL xpdlUrl = this.getClass().getResource("joinXor1Tr.xpdl");
    ProcessDefinition process = getManagementAPI().deploy(getBusinessArchiveFromXpdl(xpdlUrl));
    ProcessDefinitionUUID processUUID = process.getUUID();
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);
    checkExecutedOnce(instanceUUID, new String[]{"r1"});
    getManagementAPI().disable(processUUID);
    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testJoinXor2Tr() throws BonitaException {
    URL xpdlUrl = this.getClass().getResource("joinXor2Tr.xpdl");
    ProcessDefinition process = getManagementAPI().deploy(getBusinessArchiveFromXpdl(xpdlUrl));
    ProcessDefinitionUUID processUUID = process.getUUID();
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);
    checkExecutedOnce(instanceUUID, new String[]{"r3"});
    checkOnlyOneExecuted(instanceUUID, new String[]{"r1","r2"});
    getManagementAPI().disable(processUUID);
    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testJoinXorAtTheEndOfTwoBranches() throws Exception {
    ProcessDefinition definition =
      ProcessBuilder.createProcess("branch", "1.0")
      .addHuman(getLogin())
      .addHumanTask("firstStep", getLogin())
      .addHumanTask("firstBranchStep", getLogin())
      .addHumanTask("secondBranchStep1", getLogin())
      .addHumanTask("secondBranchStep2", getLogin())
      .addHumanTask("lastStep", getLogin())
      .addDecisionNode("firstXor")
        .addJoinType(JoinType.XOR)
        .addSplitType(SplitType.XOR)
      .addTransition("firstStep", "firstBranchStep")
      .addTransition("firstBranchStep", "firstXor")
      .addTransition("firstXor", "lastStep")
      .addTransition("firstStep", "secondBranchStep1")
      .addTransition("secondBranchStep1", "secondBranchStep2")
      .addTransition("secondBranchStep2", "firstXor")
    .done();

    definition = getManagementAPI().deploy(getBusinessArchive(definition));
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(definition.getUUID());

    checkReadyActivities(instanceUUID, "firstStep");
    executeTask(instanceUUID, "firstStep");

    checkReadyActivities(instanceUUID, "firstBranchStep", "secondBranchStep1");
    executeTask(instanceUUID, "secondBranchStep1");
    executeTask(instanceUUID, "firstBranchStep");

    Map<String, ActivityState> activities = new HashMap<String, ActivityState>();
    activities.put("firstStep", ActivityState.FINISHED);
    activities.put("firstBranchStep", ActivityState.FINISHED);
    activities.put("secondBranchStep1", ActivityState.FINISHED);
    activities.put("secondBranchStep2", ActivityState.ABORTED);
    activities.put("lastStep", ActivityState.READY);
    checkActivityStates(instanceUUID,  activities);

    checkReadyActivities(instanceUUID, "lastStep");
    executeTask(instanceUUID, "lastStep");

    getManagementAPI().deleteProcess(definition.getUUID());
  }

  public void testJoinXorOnTwoBranches() throws Exception {
    ProcessDefinition definition =
      ProcessBuilder.createProcess("branch", "1.0")
      .addHuman(getLogin())
      .addHumanTask("firstStep", getLogin())
      .addHumanTask("firstBranchStep", getLogin())
      .addHumanTask("secondBranchStep1", getLogin())
      .addHumanTask("secondBranchStep2", getLogin())
      .addHumanTask("lastStep", getLogin())
      .addDecisionNode("firstXor")
        .addJoinType(JoinType.XOR)
        .addSplitType(SplitType.XOR)
      .addTransition("firstStep", "firstBranchStep")
      .addTransition("firstBranchStep", "firstXor")
      .addTransition("firstXor", "lastStep")
      .addTransition("firstStep", "secondBranchStep1")
      .addTransition("secondBranchStep1", "secondBranchStep2")
      .addTransition("secondBranchStep2", "firstXor")
    .done();

    definition = getManagementAPI().deploy(getBusinessArchive(definition));
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(definition.getUUID());

    checkReadyActivities(instanceUUID, "firstStep");
    executeTask(instanceUUID, "firstStep");

    checkReadyActivities(instanceUUID, "firstBranchStep", "secondBranchStep1");
    executeTask(instanceUUID, "firstBranchStep");

    Map<String, ActivityState> activities = new HashMap<String, ActivityState>();
    activities.put("firstStep", ActivityState.FINISHED);
    activities.put("firstBranchStep", ActivityState.FINISHED);
    activities.put("secondBranchStep1", ActivityState.ABORTED);
    activities.put("lastStep", ActivityState.READY);
    checkActivityStates(instanceUUID,  activities);

    checkReadyActivities(instanceUUID, "lastStep");
    executeTask(instanceUUID, "lastStep");

    getManagementAPI().deleteProcess(definition.getUUID());
  }

  public void testJoinXorOnTwoBranchesContainingACycle() throws Exception {
    ProcessDefinition definition =
      ProcessBuilder.createProcess("branch", "1.0")
      .addBooleanData("validate", false)
      .addHuman(getLogin())
      .addHumanTask("firstStep", getLogin())
      .addHumanTask("firstBranchStep", getLogin())
      .addHumanTask("secondBranchStep1", getLogin())
      .addHumanTask("secondBranchStep2", getLogin())
      .addHumanTask("lastStep", getLogin())
      .addDecisionNode("secondXor")
        .addJoinType(JoinType.XOR)
        .addSplitType(SplitType.XOR)
      .addDecisionNode("firstXor")
        .addJoinType(JoinType.XOR)
        .addSplitType(SplitType.XOR)
      .addTransition("firstStep", "firstBranchStep")
      .addTransition("firstBranchStep", "secondXor")
      .addTransition("secondXor", "lastStep")
      .addTransition("firstStep", "firstXor")
      .addTransition("firstXor", "secondBranchStep1")
      .addTransition("secondBranchStep1", "secondBranchStep2")
      .addTransition("secondBranchStep2", "firstXor")
        .addCondition("!validate")
      .addTransition("secondBranchStep2", "secondXor")
        .addCondition("validate")
    .done();

    definition = getManagementAPI().deploy(getBusinessArchive(definition));
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(definition.getUUID());

    checkReadyActivities(instanceUUID, "firstStep");
    executeTask(instanceUUID, "firstStep");

    checkReadyActivities(instanceUUID, "firstBranchStep", "secondBranchStep1");
    executeTask(instanceUUID, "firstBranchStep");

    Map<String, ActivityState> activities = new HashMap<String, ActivityState>();
    activities.put("firstStep", ActivityState.FINISHED);
    activities.put("firstBranchStep", ActivityState.FINISHED);
    activities.put("secondBranchStep1", ActivityState.ABORTED);
    activities.put("lastStep", ActivityState.READY);
    checkActivityStates(instanceUUID,  activities);

    checkReadyActivities(instanceUUID, "lastStep");
    executeTask(instanceUUID, "lastStep");

    getManagementAPI().deleteProcess(definition.getUUID());
  }

  public void testJoinXorAtTheEndOfTwoBranchesContainingACycle() throws Exception {
    ProcessDefinition definition =
      ProcessBuilder.createProcess("branch", "1.0")
      .addBooleanData("validate", false)
      .addHuman(getLogin())
      .addHumanTask("firstStep", getLogin())
      .addHumanTask("firstBranchStep", getLogin())
      .addHumanTask("secondBranchStep1", getLogin())
      .addHumanTask("secondBranchStep2", getLogin())
      .addHumanTask("lastStep", getLogin())
      .addDecisionNode("secondXor")
        .addJoinType(JoinType.XOR)
        .addSplitType(SplitType.XOR)
      .addDecisionNode("firstXor")
        .addJoinType(JoinType.XOR)
        .addSplitType(SplitType.XOR)
      .addTransition("firstStep", "firstBranchStep")
      .addTransition("firstBranchStep", "secondXor")
      .addTransition("secondXor", "lastStep")
      .addTransition("firstStep", "firstXor")
      .addTransition("firstXor", "secondBranchStep1")
      .addTransition("secondBranchStep1", "secondBranchStep2")
      .addTransition("secondBranchStep2", "firstXor")
        .addCondition("!validate")
      .addTransition("secondBranchStep2", "secondXor")
        .addCondition("validate")
    .done();

    definition = getManagementAPI().deploy(getBusinessArchive(definition));
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(definition.getUUID());

    checkReadyActivities(instanceUUID, "firstStep");
    executeTask(instanceUUID, "firstStep");

    checkReadyActivities(instanceUUID, "firstBranchStep", "secondBranchStep1");
    executeTask(instanceUUID, "secondBranchStep1");
    executeTask(instanceUUID, "firstBranchStep");

    Map<String, ActivityState> activities = new HashMap<String, ActivityState>();
    activities.put("firstStep", ActivityState.FINISHED);
    activities.put("firstBranchStep", ActivityState.FINISHED);
    activities.put("secondBranchStep1", ActivityState.FINISHED);
    activities.put("secondBranchStep2", ActivityState.ABORTED);
    activities.put("lastStep", ActivityState.READY);
    checkActivityStates(instanceUUID,  activities);

    checkReadyActivities(instanceUUID, "lastStep");
    executeTask(instanceUUID, "lastStep");

    getManagementAPI().deleteProcess(definition.getUUID());
  }

  public void testJoinXorAtTheEndOfTwoBranchesContainingARunningCycle() throws Exception {
    ProcessDefinition definition =
      ProcessBuilder.createProcess("branch", "1.0")
      .addBooleanData("validate", false)
      .addHuman(getLogin())
      .addHumanTask("firstStep", getLogin())
      .addHumanTask("firstBranchStep", getLogin())
      .addHumanTask("secondBranchStep1", getLogin())
      .addHumanTask("secondBranchStep2", getLogin())
        .addSplitType(SplitType.XOR)
      .addHumanTask("lastStep", getLogin())
      .addDecisionNode("secondXor")
        .addJoinType(JoinType.XOR)
        .addSplitType(SplitType.XOR)
      .addDecisionNode("firstXor")
        .addJoinType(JoinType.XOR)
        .addSplitType(SplitType.XOR)
      .addTransition("firstStep", "firstBranchStep")
      .addTransition("firstBranchStep", "secondXor")
      .addTransition("secondXor", "lastStep")
      .addTransition("firstStep", "firstXor")
      .addTransition("firstXor", "secondBranchStep1")
      .addTransition("secondBranchStep1", "secondBranchStep2")
      .addTransition("secondBranchStep2", "firstXor")
        .addCondition("!validate")
      .addTransition("secondBranchStep2", "secondXor")
        .addCondition("validate")
    .done();

    definition = getManagementAPI().deploy(getBusinessArchive(definition));
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(definition.getUUID());

    checkReadyActivities(instanceUUID, "firstStep");
    executeTask(instanceUUID, "firstStep");

    String firstIteration = getTaskIteration(instanceUUID, "secondBranchStep1");
    checkReadyActivities(instanceUUID, "firstBranchStep", "secondBranchStep1");
    executeTask(instanceUUID, "secondBranchStep1");
    executeTask(instanceUUID, "secondBranchStep2");
    String secondIteration = getTaskIteration(instanceUUID, "secondBranchStep1");
    executeTask(instanceUUID, "secondBranchStep1");
    executeTask(instanceUUID, "firstBranchStep");

    List<Activity> activities = new ArrayList<Activity>();
    activities.add(new Activity("firstStep", "it1", ActivityState.FINISHED));
    activities.add(new Activity("firstXor", firstIteration, ActivityState.FINISHED));
    activities.add(new Activity("secondBranchStep1", firstIteration, ActivityState.FINISHED));
    activities.add(new Activity("secondBranchStep2", firstIteration, ActivityState.FINISHED));
    activities.add(new Activity("firstXor", secondIteration, ActivityState.FINISHED));
    activities.add(new Activity("secondBranchStep1", secondIteration, ActivityState.FINISHED));
    activities.add(new Activity("secondBranchStep2", secondIteration, ActivityState.ABORTED));
    activities.add(new Activity("firstBranchStep", "it1", ActivityState.FINISHED));
    activities.add(new Activity("secondXor", "it1", ActivityState.FINISHED));
    activities.add(new Activity("lastStep", "it1", ActivityState.READY));
    checkActivityStates(instanceUUID,  activities);

    checkReadyActivities(instanceUUID, "lastStep");
    executeTask(instanceUUID, "lastStep");

    getManagementAPI().deleteProcess(definition.getUUID());
  }

  public void testJoinXorOnTwoBranchesContainingARunningCycle() throws Exception {
    ProcessDefinition definition =
      ProcessBuilder.createProcess("branch", "1.0")
      .addBooleanData("validate", false)
      .addHuman(getLogin())
      .addHumanTask("firstStep", getLogin())
      .addHumanTask("firstBranchStep", getLogin())
      .addHumanTask("secondBranchStep1", getLogin())
      .addHumanTask("secondBranchStep2", getLogin())
        .addSplitType(SplitType.XOR)
      .addHumanTask("lastStep", getLogin())
      .addDecisionNode("secondXor")
        .addJoinType(JoinType.XOR)
        .addSplitType(SplitType.XOR)
      .addDecisionNode("firstXor")
        .addJoinType(JoinType.XOR)
        .addSplitType(SplitType.XOR)
      .addTransition("firstStep", "firstBranchStep")
      .addTransition("firstBranchStep", "secondXor")
      .addTransition("secondXor", "lastStep")
      .addTransition("firstStep", "firstXor")
      .addTransition("firstXor", "secondBranchStep1")
      .addTransition("secondBranchStep1", "secondBranchStep2")
      .addTransition("secondBranchStep2", "firstXor")
        .addCondition("!validate")
      .addTransition("secondBranchStep2", "secondXor")
        .addCondition("validate")
    .done();

    definition = getManagementAPI().deploy(getBusinessArchive(definition));
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(definition.getUUID());

    checkReadyActivities(instanceUUID, "firstStep");
    executeTask(instanceUUID, "firstStep");

    String firstIteration = getTaskIteration(instanceUUID, "secondBranchStep1");
    checkReadyActivities(instanceUUID, "firstBranchStep", "secondBranchStep1");
    executeTask(instanceUUID, "secondBranchStep1");
    executeTask(instanceUUID, "secondBranchStep2");
    String secondIteration = getTaskIteration(instanceUUID, "secondBranchStep1");
    executeTask(instanceUUID, "firstBranchStep");

    List<Activity> activities = new ArrayList<Activity>();
    activities.add(new Activity("firstStep", "it1", ActivityState.FINISHED));
    activities.add(new Activity("firstBranchStep", "it1", ActivityState.FINISHED));
    activities.add(new Activity("firstXor", firstIteration, ActivityState.FINISHED));
    activities.add(new Activity("secondBranchStep1", firstIteration, ActivityState.FINISHED));
    activities.add(new Activity("secondBranchStep2", firstIteration, ActivityState.FINISHED));
    activities.add(new Activity("firstXor", secondIteration, ActivityState.FINISHED));
    activities.add(new Activity("secondBranchStep1", secondIteration, ActivityState.ABORTED));
    activities.add(new Activity("secondXor", "it1", ActivityState.FINISHED));
    activities.add(new Activity("lastStep", "it1", ActivityState.READY));
    checkActivityStates(instanceUUID,  activities);

    checkReadyActivities(instanceUUID, "lastStep");
    executeTask(instanceUUID, "lastStep");

    getManagementAPI().deleteProcess(definition.getUUID());
  }

  public void testJoinXorOnTwoBranchesContainingARunningCycleWithASubProcess() throws Exception {
    ProcessDefinition subProcess =
      ProcessBuilder.createProcess("sub", "0")
      .addHuman(getLogin())
      .addHumanTask("first", getLogin())
      .addHumanTask("second", getLogin())
      .addTransition("first", "second")
    .done();

    ProcessDefinition definition =
      ProcessBuilder.createProcess("branch", "1.0")
      .addBooleanData("validate", false)
      .addHuman(getLogin())
      .addHumanTask("firstStep", getLogin())
      .addHumanTask("firstBranchStep", getLogin())
      .addHumanTask("secondBranchStep1", getLogin())
      .addSubProcess("secondBranchStep2", "sub")
      .addHumanTask("lastStep", getLogin())
      .addDecisionNode("firstXor")
        .addJoinType(JoinType.XOR)
        .addSplitType(SplitType.XOR)
      .addDecisionNode("secondXor")
        .addJoinType(JoinType.XOR)
        .addSplitType(SplitType.XOR)
      .addTransition("firstStep", "firstBranchStep")
      .addTransition("firstBranchStep", "secondXor")
      .addTransition("secondXor", "lastStep")
      .addTransition("firstStep", "firstXor")
      .addTransition("firstXor", "secondBranchStep1")
      .addTransition("secondBranchStep1", "secondBranchStep2")
      .addTransition("secondBranchStep2", "firstXor")
        .addCondition("!validate")
      .addTransition("secondBranchStep2", "secondXor")
        .addCondition("validate")
    .done();

    subProcess = getManagementAPI().deploy(getBusinessArchive(subProcess));
    definition = getManagementAPI().deploy(getBusinessArchive(definition));
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(definition.getUUID());

    checkReadyActivities(instanceUUID, "firstStep");
    executeTask(instanceUUID, "firstStep");

    String firstIteration = getTaskIteration(instanceUUID, "secondBranchStep1");
    checkReadyActivities(instanceUUID, "firstBranchStep", "secondBranchStep1");
    executeTask(instanceUUID, "secondBranchStep1");

    ProcessInstanceUUID subUUID = getProcessInstance("first");
    executeTask(subUUID, "first");
    executeTask(subUUID, "second");
    String secondIteration = getTaskIteration(instanceUUID, "secondBranchStep1");

    executeTask(instanceUUID, "secondBranchStep1");
    ProcessInstanceUUID subUUID2 = getProcessInstance("first");
    executeTask(subUUID2, "first");
    executeTask(instanceUUID, "firstBranchStep");

    List<Activity> activities = new ArrayList<Activity>();
    activities.add(new Activity("firstStep", "it1", ActivityState.FINISHED));
    activities.add(new Activity("firstXor", firstIteration, ActivityState.FINISHED));
    activities.add(new Activity("secondBranchStep1", firstIteration, ActivityState.FINISHED));
    activities.add(new Activity("secondBranchStep2", firstIteration, ActivityState.FINISHED));
    activities.add(new Activity("firstXor", secondIteration, ActivityState.FINISHED));
    activities.add(new Activity("secondBranchStep1", secondIteration, ActivityState.FINISHED));
    activities.add(new Activity("secondBranchStep2", secondIteration, ActivityState.ABORTED));
    activities.add(new Activity("firstBranchStep", "it1", ActivityState.FINISHED));
    activities.add(new Activity("secondXor", "it1", ActivityState.FINISHED));
    activities.add(new Activity("lastStep", "it1", ActivityState.READY));
    checkActivityStates(instanceUUID,  activities);

    LightProcessInstance firstSubProcess = getQueryRuntimeAPI().getLightProcessInstance(subUUID);
    assertEquals(InstanceState.FINISHED, firstSubProcess.getInstanceState());
    activities = new ArrayList<Activity>();
    activities.add(new Activity("first", "it1", ActivityState.FINISHED));
    activities.add(new Activity("second", "it1", ActivityState.FINISHED));
    checkActivityStates(subUUID,  activities);

    LightProcessInstance secondSubProcess = getQueryRuntimeAPI().getLightProcessInstance(subUUID2);
    assertEquals(InstanceState.ABORTED, secondSubProcess.getInstanceState());
    activities = new ArrayList<Activity>();
    activities.add(new Activity("first", "it1", ActivityState.FINISHED));
    activities.add(new Activity("second", "it1", ActivityState.ABORTED));
    checkActivityStates(subUUID2,  activities);

    checkReadyActivities(instanceUUID, "lastStep");
    executeTask(instanceUUID, "lastStep");

    getManagementAPI().deleteProcess(definition.getUUID());
    getManagementAPI().deleteProcess(subProcess.getUUID());
  }

  private ProcessInstanceUUID getProcessInstance(String taskName) throws Exception {
    Collection<LightTaskInstance> tasks = getQueryRuntimeAPI().getLightTaskList(ActivityState.READY);
    Iterator<LightTaskInstance> iterator = tasks.iterator();
    boolean found = false;
    LightTaskInstance task = null;
    while (!found && iterator.hasNext()) {
      task = iterator.next();
      if (task.getActivityName().equals(taskName)) {
        found = true;
      }
    }
    if (task != null) {
      return task.getProcessInstanceUUID();
    } else {
      throw new IllegalArgumentException("No available task: " + taskName+ " for the logged user");
    }
  }

  public void testJoinXorOnTwoBranchesContainingARunningCycleWithALoop() throws Exception {
    ProcessDefinition definition =
      org.ow2.bonita.util.ProcessBuilder.createProcess("branch", "1.0")
      .addBooleanData("validate", false)
      .addBooleanData("loop", true)
      .addHuman(getLogin())
      .addHumanTask("firstStep", getLogin())
      .addHumanTask("firstBranchStep", getLogin())
      .addHumanTask("secondBranchStep1", getLogin())
      .addHumanTask("secondBranchStep2", getLogin())
        .addSplitType(SplitType.XOR)
        .addLoop("loop", true)
      .addHumanTask("lastStep", getLogin())
      .addDecisionNode("secondXor")
        .addJoinType(JoinType.XOR)
        .addSplitType(SplitType.XOR)
      .addDecisionNode("firstXor")
        .addJoinType(JoinType.XOR)
        .addSplitType(SplitType.XOR)
      .addTransition("firstStep", "firstBranchStep")
      .addTransition("firstBranchStep", "secondXor")
      .addTransition("secondXor", "lastStep")
      .addTransition("firstStep", "firstXor")
      .addTransition("firstXor", "secondBranchStep1")
      .addTransition("secondBranchStep1", "secondBranchStep2")
      .addTransition("secondBranchStep2", "firstXor")
        .addCondition("!validate")
      .addTransition("secondBranchStep2", "secondXor")
        .addCondition("validate")
    .done();

    definition = getManagementAPI().deploy(getBusinessArchive(definition));
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(definition.getUUID());

    checkReadyActivities(instanceUUID, "firstStep");
    executeTask(instanceUUID, "firstStep");

    checkReadyActivities(instanceUUID, "firstBranchStep", "secondBranchStep1");
    String firstIteration = getTaskIteration(instanceUUID, "secondBranchStep1");
    executeTask(instanceUUID, "secondBranchStep1");

    checkReadyActivities(instanceUUID, "firstBranchStep", "secondBranchStep2");
    String firstLoopId = getTaskLoopId(instanceUUID, "secondBranchStep2");
    executeTask(instanceUUID, "secondBranchStep2");

    getRuntimeAPI().setProcessInstanceVariable(instanceUUID, "loop", false);
    checkReadyActivities(instanceUUID, "firstBranchStep", "secondBranchStep2");
    String secondLoopId = getTaskLoopId(instanceUUID, "secondBranchStep2");
    executeTask(instanceUUID, "secondBranchStep2");

    getRuntimeAPI().setProcessInstanceVariable(instanceUUID, "loop", true);
    checkReadyActivities(instanceUUID, "firstBranchStep", "secondBranchStep1");
    String secondIteration = getTaskIteration(instanceUUID, "secondBranchStep1");
    executeTask(instanceUUID, "secondBranchStep1");

    checkReadyActivities(instanceUUID, "firstBranchStep", "secondBranchStep2");
    String thirdLoopId = getTaskLoopId(instanceUUID, "secondBranchStep2");
    executeTask(instanceUUID, "secondBranchStep2");

    checkReadyActivities(instanceUUID, "firstBranchStep", "secondBranchStep2");
    String fourthLoopId = getTaskLoopId(instanceUUID, "secondBranchStep2");
    executeTask(instanceUUID, "firstBranchStep");

    List<Activity> activities = new ArrayList<Activity>();
    activities.add(new Activity("firstStep", "it1", ActivityState.FINISHED));
    activities.add(new Activity("firstXor", firstIteration, ActivityState.FINISHED));
    activities.add(new Activity("secondBranchStep1", firstIteration, ActivityState.FINISHED));
    activities.add(new Activity("secondBranchStep2", firstIteration, firstLoopId, ActivityState.FINISHED));
    activities.add(new Activity("secondBranchStep2", firstIteration, secondLoopId, ActivityState.FINISHED));
    activities.add(new Activity("firstXor", secondIteration, ActivityState.FINISHED));
    activities.add(new Activity("secondBranchStep1", secondIteration, ActivityState.FINISHED));
    activities.add(new Activity("secondBranchStep2", secondIteration, thirdLoopId, ActivityState.FINISHED));
    activities.add(new Activity("secondBranchStep2", secondIteration, fourthLoopId, ActivityState.ABORTED));
    activities.add(new Activity("firstBranchStep", "it1", ActivityState.FINISHED));
    activities.add(new Activity("secondXor", "it1", ActivityState.FINISHED));
    activities.add(new Activity("lastStep", "it1", ActivityState.READY));
    checkActivityStates(instanceUUID,  activities);

    checkReadyActivities(instanceUUID, "lastStep");
    executeTask(instanceUUID, "lastStep");

    getManagementAPI().deleteProcess(definition.getUUID());
  }

  public void testTwoSimpleBranchesJoinXorGate() throws Exception {
    ProcessDefinition process = ProcessBuilder.createProcess("Xor", "1.0")
    .addHuman(getLogin())
    .addSystemTask("start")
    .addHumanTask("branch1", getLogin())
    .addHumanTask("branch2", getLogin())
    .addDecisionNode("join")
      .addJoinType(JoinType.XOR)
    .addTransition("start", "branch1")
    .addTransition("start", "branch2")
    .addTransition("branch1", "join")
    .addTransition("branch2", "join")
    .addHumanTask("end", getLogin())
    .addTransition("join", "end")
    .done();

    process = getManagementAPI().deploy(getBusinessArchive(process, null));
    final ProcessDefinitionUUID processUUID = process.getUUID();
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    Collection<TaskInstance> tasks = getQueryRuntimeAPI().getTaskList(ActivityState.READY);
    assertEquals(2, tasks.size());
    executeTask(instanceUUID, "branch2");

    ProcessInstance instance = getQueryRuntimeAPI().getProcessInstance(instanceUUID);
    assertEquals(InstanceState.STARTED, instance.getInstanceState());
    Set<ActivityInstance> activities = instance.getActivities();
    ActivityInstance activity = getActivityInstance(activities, "branch2");
    assertEquals(ActivityState.FINISHED, activity.getState());
    activity = getActivityInstance(activities, "branch1");
    assertEquals(ActivityState.ABORTED, activity.getState());

    tasks = getQueryRuntimeAPI().getTaskList(instanceUUID, ActivityState.READY);
    assertEquals(1, tasks.size());
    assertEquals("end", tasks.iterator().next().getActivityName());
    
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testTwoBranchesTimerJoinXorGate() throws Exception {
    ProcessDefinition process = ProcessBuilder.createProcess("Xor", "1.0")
    .addHuman(getLogin())
    .addSystemTask("start")
    .addHumanTask("branch1", getLogin())
    .addTimerTask("timer", "100000000000000")
    .addDecisionNode("join")
      .addJoinType(JoinType.XOR)
    .addTransition("start", "branch1")
    .addTransition("start", "timer")
    .addTransition("branch1", "join")
    .addTransition("timer", "join")
    .addHumanTask("end", getLogin())
    .addTransition("join", "end")
    .done();

    process = getManagementAPI().deploy(getBusinessArchive(process, null));
    final ProcessDefinitionUUID processUUID = process.getUUID();
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    Collection<TaskInstance> tasks = getQueryRuntimeAPI().getTaskList(ActivityState.READY);
    assertEquals(1, tasks.size());
    executeTask(instanceUUID, "branch1");

    ProcessInstance instance = getQueryRuntimeAPI().getProcessInstance(instanceUUID);
    assertEquals(InstanceState.STARTED, instance.getInstanceState());
    Set<ActivityInstance> activities = instance.getActivities();
    ActivityInstance activity = getActivityInstance(activities, "branch1");
    assertEquals(ActivityState.FINISHED, activity.getState());
    activity = getActivityInstance(activities, "timer");
    assertEquals(ActivityState.ABORTED, activity.getState());

    tasks = getQueryRuntimeAPI().getTaskList(instanceUUID, ActivityState.READY);
    assertEquals(1, tasks.size());
    assertEquals("end", tasks.iterator().next().getActivityName());
    
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testTwoBranchesSignalJoinXorGate() throws Exception {
    ProcessDefinition process = ProcessBuilder.createProcess("Xor", "1.0")
    .addHuman(getLogin())
    .addSystemTask("start")
    .addHumanTask("branch1", getLogin())
    .addSignalEventTask("signal", "go", true)
    .addDecisionNode("join")
      .addJoinType(JoinType.XOR)
    .addTransition("start", "branch1")
    .addTransition("start", "signal")
    .addTransition("branch1", "join")
    .addTransition("signal", "join")
    .addHumanTask("end", getLogin())
    .addTransition("join", "end")
    .done();

    process = getManagementAPI().deploy(getBusinessArchive(process, null));
    final ProcessDefinitionUUID processUUID = process.getUUID();
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    Collection<TaskInstance> tasks = getQueryRuntimeAPI().getTaskList(ActivityState.READY);
    assertEquals(1, tasks.size());
    executeTask(instanceUUID, "branch1");

    ProcessInstance instance = getQueryRuntimeAPI().getProcessInstance(instanceUUID);
    assertEquals(InstanceState.STARTED, instance.getInstanceState());
    Set<ActivityInstance> activities = instance.getActivities();
    ActivityInstance activity = getActivityInstance(activities, "branch1");
    assertEquals(ActivityState.FINISHED, activity.getState());
    activity = getActivityInstance(activities, "signal");
    assertEquals(ActivityState.ABORTED, activity.getState());

    tasks = getQueryRuntimeAPI().getTaskList(instanceUUID, ActivityState.READY);
    assertEquals(1, tasks.size());
    assertEquals("end", tasks.iterator().next().getActivityName());
    
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testTwoBranchesMessageJoinXorGate() throws Exception {
    ProcessDefinition process = ProcessBuilder.createProcess("Xor", "1.0")
    .addHuman(getLogin())
    .addSystemTask("start")
    .addHumanTask("branch1", getLogin())
    .addReceiveEventTask("message", "msg")
    .addDecisionNode("join")
      .addJoinType(JoinType.XOR)
    .addTransition("start", "branch1")
    .addTransition("start", "message")
    .addTransition("branch1", "join")
    .addTransition("message", "join")
    .addHumanTask("end", getLogin())
    .addTransition("join", "end")
    .done();

    process = getManagementAPI().deploy(getBusinessArchive(process, null));
    final ProcessDefinitionUUID processUUID = process.getUUID();
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    Collection<TaskInstance> tasks = getQueryRuntimeAPI().getTaskList(ActivityState.READY);
    assertEquals(1, tasks.size());
    executeTask(instanceUUID, "branch1");

    ProcessInstance instance = getQueryRuntimeAPI().getProcessInstance(instanceUUID);
    assertEquals(InstanceState.STARTED, instance.getInstanceState());
    Set<ActivityInstance> activities = instance.getActivities();
    ActivityInstance activity = getActivityInstance(activities, "branch1");
    assertEquals(ActivityState.FINISHED, activity.getState());
    activity = getActivityInstance(activities, "message");
    assertEquals(ActivityState.ABORTED, activity.getState());

    tasks = getQueryRuntimeAPI().getTaskList(instanceUUID, ActivityState.READY);
    assertEquals(1, tasks.size());
    assertEquals("end", tasks.iterator().next().getActivityName());

    getManagementAPI().deleteProcess(processUUID);
  }

}
