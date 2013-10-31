package org.ow2.bonita.event;

import java.util.Collection;
import java.util.Set;

import org.bonitasoft.connectors.bonita.SetVarConnector;
import org.ow2.bonita.APITestCase;
import org.ow2.bonita.facade.QueryRuntimeAPI;
import org.ow2.bonita.facade.def.element.HookDefinition.Event;
import org.ow2.bonita.facade.def.majorElement.ActivityDefinition.JoinType;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.runtime.ActivityState;
import org.ow2.bonita.facade.runtime.InstanceState;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.light.LightActivityInstance;
import org.ow2.bonita.light.LightTaskInstance;
import org.ow2.bonita.util.AccessorUtil;
import org.ow2.bonita.util.ProcessBuilder;

public class ErrorEventTest extends APITestCase {
  
  private static long waitingTime = 2000;

  public void testErrorWhenAConnectorFailsOnASystemTaskOnEnter() throws Exception {
    errorWhenAConnectorFailsOnASystemTask(Event.automaticOnEnter);
  }

  public void testErrorWhenAConnectorFailsOnASystemTaskOnExit() throws Exception {
    errorWhenAConnectorFailsOnASystemTask(Event.automaticOnExit);
  }

  public void testErrorWhenAConnectorFailsOnAHumanTaskOnReady() throws Exception {
    errorWhenAConnectorFailsOnAHumanTask(Event.taskOnReady);
  }

  public void testErrorWhenAConnectorFailsOnAHumanTaskOnStart() throws Exception {
    errorWhenAConnectorFailsOnAHumanTask(Event.taskOnStart);
  }

  public void testErrorWhenAConnectorFailsOnAHumanTaskOnFinish() throws Exception {
    errorWhenAConnectorFailsOnAHumanTask(Event.taskOnFinish);
  }

  public void testErrorWhenAConnectorFailsOnASystemTaskOfASubProcessOnEnter() throws Exception {
    errorWhenAConnectorFailsOnASystemOfASubProcess(Event.automaticOnEnter);
  }

  public void testErrorWhenAConnectorFailsOnASystemTaskOfASubProcessOnExit() throws Exception {
    errorWhenAConnectorFailsOnASystemOfASubProcess(Event.automaticOnExit);
  }

  public void testErrorWhenAConnectorFailsOnAHumanTaskOfASubProcessOnReady() throws Exception {
    errorWhenAConnectorFailsOnAHumanOfASubProcess(Event.taskOnReady);
  }

  public void testErrorWhenAConnectorFailsOnAHumanTaskOfASubProcessOnStart() throws Exception {
    errorWhenAConnectorFailsOnAHumanOfASubProcess(Event.taskOnStart);
  }

  public void testErrorWhenAConnectorFailsOnAHumanTaskOfASubProcessOnFinish() throws Exception {
    errorWhenAConnectorFailsOnAHumanOfASubProcess(Event.taskOnFinish);
  }

  public void testErrorWhenAConnectorFailsOnASystemTaskTwoErrorsOnEnter() throws Exception {
    errorWhenAConnectorFailsOnASystemTaskTwoErrors(Event.automaticOnEnter);
  }

  public void testErrorWhenAConnectorFailsOnASystemTaskTwoErrorsOnExit() throws Exception {
    errorWhenAConnectorFailsOnASystemTaskTwoErrors(Event.automaticOnExit);
  }

  public void testErrorWhenAConnectorFailsOnAHumanTaskTwoErrorsOnReady() throws Exception {
    errorWhenAConnectorFailsOnAHumanTaskTwoErrors(Event.taskOnReady);
  }

  public void testErrorWhenAConnectorFailsOnAHumanTaskTwoErrorsOnStart() throws Exception {
    errorWhenAConnectorFailsOnAHumanTaskTwoErrors(Event.taskOnStart);
  }

  public void testErrorWhenAConnectorFailsOnAHumanTaskTwoErrorsOnFinish() throws Exception {
    errorWhenAConnectorFailsOnAHumanTaskTwoErrors(Event.taskOnFinish);
  }

  public void testErrorWhenAConnectorFailsOnASystemTaskOfASubProcessTwoErrorsOnEnter() throws Exception {
    errorWhenAConnectorFailsOnASystemOfASubProcessTwoErrors(Event.automaticOnEnter);
  }

  public void testErrorWhenAConnectorFailsOnASystemTaskOfASubProcessTwoErrorsOnExit() throws Exception {
    errorWhenAConnectorFailsOnASystemOfASubProcessTwoErrors(Event.automaticOnExit);
  }

  public void testErrorWhenAConnectorFailsOnAHumanTaskOfASubProcessTwoErrorsOnReady() throws Exception {
    errorWhenAConnectorFailsOnAHumanOfASubProcessTwoErrors(Event.taskOnReady);
  }

  public void testErrorWhenAConnectorFailsOnAHumanTaskOfASubProcessTwoErrorsOnStart() throws Exception {
    errorWhenAConnectorFailsOnAHumanOfASubProcessTwoErrors(Event.taskOnStart);
  }

  public void testErrorWhenAConnectorFailsOnAHumanTaskOfASubProcessTwoErrorsOnFinish() throws Exception {
    errorWhenAConnectorFailsOnAHumanOfASubProcessTwoErrors(Event.taskOnFinish);
  }

  public void testErrorWhenAConnectorFailsOnASystemTaskTwoPossibleErrorsOnEnter() throws Exception {
    errorWhenAConnectorFailsOnASystemTaskTwoPossibleErrors(Event.automaticOnEnter);
  }

  public void testErrorWhenAConnectorFailsOnASystemTaskTwoPossibleErrorsOnExit() throws Exception {
    errorWhenAConnectorFailsOnASystemTaskTwoPossibleErrors(Event.automaticOnExit);
  }

  public void testErrorWhenAConnectorFailsOnAHumanTaskTwoPossibleErrorsOnReady() throws Exception {
    errorWhenAConnectorFailsOnAHumanTaskTwoPossibleErrors(Event.taskOnReady);
  }

  public void testErrorWhenAConnectorFailsOnAHumanTaskTwoPossibleErrorsOnStart() throws Exception {
    errorWhenAConnectorFailsOnAHumanTaskTwoPossibleErrors(Event.taskOnStart);
  }

  public void testErrorWhenAConnectorFailsOnAHumanTaskTwoPossibleErrorsOnFinish() throws Exception {
    errorWhenAConnectorFailsOnAHumanTaskTwoPossibleErrors(Event.taskOnFinish);
  }

  public void testErrorWhenAConnectorFailsOnASystemTaskOfASubProcessTwoPossibleErrorsOnEnter() throws Exception {
    errorWhenAConnectorFailsOnASystemOfASubProcessTwoPossibleErrors(Event.automaticOnEnter);
  }

  public void testErrorWhenAConnectorFailsOnASystemTaskOfASubProcessTwoPossibleErrorsOnExit() throws Exception {
    errorWhenAConnectorFailsOnASystemOfASubProcessTwoPossibleErrors(Event.automaticOnExit);
  }

  public void testErrorWhenAConnectorFailsOnAHumanTaskOfASubProcessTwoPossibleErrorsOnReady() throws Exception {
    errorWhenAConnectorFailsOnAHumanOfASubProcessTwoPossibleErrors(Event.taskOnReady);
  }

  public void testErrorWhenAConnectorFailsOnAHumanTaskOfASubProcessTwoPossibleErrorsOnStart() throws Exception {
    errorWhenAConnectorFailsOnAHumanOfASubProcessTwoPossibleErrors(Event.taskOnStart);
  }

  public void testErrorWhenAConnectorFailsOnAHumanTaskOfASubProcessTwoPossibleErrorsOnFinish() throws Exception {
    errorWhenAConnectorFailsOnAHumanOfASubProcessTwoPossibleErrors(Event.taskOnFinish);
  }

  private void errorWhenAConnectorFailsOnASystemTask(Event event) throws Exception {
    ProcessDefinition process = ProcessBuilder.createProcess("simpleActivity", "1.0")
    .addHuman(getLogin())
    .addSystemTask("errorStep")
      .addErrorBoundaryEvent("error", "fail")
      .addConnector(event, SetVarConnector.class.getName(), false)
        .throwCatchError("fail")
        .addInputParameter("variableName", "unknownVariable")
        .addInputParameter("value", 10)
    .addHumanTask("exceptionStep", getLogin())
    .addExceptionTransition("errorStep", "error", "exceptionStep")
    .done();

    process = getManagementAPI().deploy(getBusinessArchive(process, null, SetVarConnector.class));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);
    Thread.sleep(waitingTime);
    assertEquals(InstanceState.STARTED, getQueryRuntimeAPI().getLightProcessInstance(instanceUUID).getInstanceState());
    Set<LightActivityInstance> activities = getQueryRuntimeAPI().getLightActivityInstances(instanceUUID);
    assertEquals(2, activities.size());
    LightActivityInstance activity = getLightActivityInstance(activities, "errorStep");
    assertEquals(ActivityState.ABORTED, activity.getState());
    activity = getLightActivityInstance(activities, "exceptionStep");
    assertEquals(ActivityState.READY, activity.getState());

    getManagementAPI().deleteProcess(processUUID);
  }

  private void errorWhenAConnectorFailsOnAHumanTask(Event event) throws Exception {
    ProcessDefinition process = ProcessBuilder.createProcess("simpleActivity", "1.0")
    .addHuman(getLogin())
    .addHumanTask("errorStep", getLogin())
      .addErrorBoundaryEvent("error", "fail")
      .addConnector(event, SetVarConnector.class.getName(), false)
        .throwCatchError("fail")
        .addInputParameter("variableName", "unknownVariable")
        .addInputParameter("value", 10)
    .addHumanTask("exceptionStep", getLogin())
    .addExceptionTransition("errorStep", "error", "exceptionStep")
    .done();

    process = getManagementAPI().deploy(getBusinessArchive(process, null, SetVarConnector.class));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);
    Set<LightActivityInstance> activities = getQueryRuntimeAPI().getLightActivityInstances(instanceUUID);
    if (!Event.taskOnReady.equals(event)) {
      assertEquals(1, activities.size());
      LightActivityInstance task = activities.iterator().next();
      getRuntimeAPI().startTask(task.getUUID(), false);
      getRuntimeAPI().finishTask(task.getUUID(), false);
    }
    Thread.sleep(waitingTime);
    assertEquals(InstanceState.STARTED, getQueryRuntimeAPI().getLightProcessInstance(instanceUUID).getInstanceState());
    activities = getQueryRuntimeAPI().getLightActivityInstances(instanceUUID);
    assertEquals(2, activities.size());
    LightActivityInstance activity = getLightActivityInstance(activities, "errorStep");
    assertEquals(ActivityState.ABORTED, activity.getState());
    activity = getLightActivityInstance(activities, "exceptionStep");
    assertEquals(ActivityState.READY, activity.getState());

    getManagementAPI().deleteProcess(processUUID);
  }

  private void errorWhenAConnectorFailsOnASystemOfASubProcess(Event event) throws Exception {
    ProcessDefinition subProcess = ProcessBuilder.createProcess("subError", "1.0")
    .addSystemTask("errorStep")
      .addConnector(event, SetVarConnector.class.getName(), false)
        .throwCatchError("fail")
        .addInputParameter("variableName", "unknownVariable")
        .addInputParameter("value", 10)
    .done();

    ProcessDefinition process = ProcessBuilder.createProcess("main", "1.5")
    .addHuman(getLogin())
    .addSubProcess("failingActivity", "subError")
      .addErrorBoundaryEvent("error", "fail")
    .addHumanTask("exceptionStep", getLogin())
    .addExceptionTransition("failingActivity", "error", "exceptionStep")
    .done();

    subProcess = getManagementAPI().deploy(getBusinessArchive(subProcess, null, SetVarConnector.class));
    process = getManagementAPI().deploy(getBusinessArchive(process));
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(process.getUUID());
    final ProcessInstanceUUID subflowInstanceUUID = getQueryRuntimeAPI().getLightProcessInstances(subProcess.getUUID()).iterator().next().getUUID();

    Thread.sleep(waitingTime);
    assertEquals(InstanceState.STARTED, getQueryRuntimeAPI().getLightProcessInstance(instanceUUID).getInstanceState());
    Set<LightActivityInstance> activities = getQueryRuntimeAPI().getLightActivityInstances(instanceUUID);
    assertEquals(2, activities.size());
    LightActivityInstance activity = getLightActivityInstance(activities, "failingActivity");
    assertEquals(ActivityState.ABORTED, activity.getState());
    activity = getLightActivityInstance(activities, "exceptionStep");
    assertEquals(ActivityState.READY, activity.getState());

    final Set<LightActivityInstance> subflowActivities = getQueryRuntimeAPI().getLightActivityInstances(subflowInstanceUUID);
    assertEquals(1, subflowActivities.size());
    LightActivityInstance subActivity = subflowActivities.iterator().next();
    assertEquals("errorStep", subActivity.getActivityName());
    assertEquals(ActivityState.ABORTED, subActivity.getState());
    assertEquals(InstanceState.ABORTED, getQueryRuntimeAPI().getLightProcessInstance(subflowInstanceUUID).getInstanceState());

    getManagementAPI().deleteProcess(process.getUUID());
    getManagementAPI().deleteProcess(subProcess.getUUID());
  }

  private void errorWhenAConnectorFailsOnAHumanOfASubProcess(Event event) throws Exception {
    ProcessDefinition subProcess = ProcessBuilder.createProcess("subError", "1.0")
    .addHuman(getLogin())
    .addHumanTask("errorStep", getLogin())
      .addConnector(event, SetVarConnector.class.getName(), false)
        .throwCatchError("fail")
        .addInputParameter("variableName", "unknownVariable")
        .addInputParameter("value", 10)
    .done();

    ProcessDefinition process = ProcessBuilder.createProcess("main", "1.5")
    .addHuman(getLogin())
    .addSubProcess("failingActivity", "subError")
      .addErrorBoundaryEvent("error", "fail")
    .addHumanTask("exceptionStep", getLogin())
    .addExceptionTransition("failingActivity", "error", "exceptionStep")
    .done();

    subProcess = getManagementAPI().deploy(getBusinessArchive(subProcess, null, SetVarConnector.class));
    process = getManagementAPI().deploy(getBusinessArchive(process));
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(process.getUUID());
    final ProcessInstanceUUID subflowInstanceUUID = getQueryRuntimeAPI().getLightProcessInstances(subProcess.getUUID()).iterator().next().getUUID();

    if (!Event.taskOnReady.equals(event)) {
      Set<LightTaskInstance> tasks = getQueryRuntimeAPI().getLightTasks(subflowInstanceUUID);
      LightTaskInstance task = tasks.iterator().next();
      getRuntimeAPI().startTask(task.getUUID(), false);
      getRuntimeAPI().finishTask(task.getUUID(), false);
    }

    Thread.sleep(waitingTime);
    assertEquals(InstanceState.STARTED, getQueryRuntimeAPI().getLightProcessInstance(instanceUUID).getInstanceState());
    Set<LightActivityInstance> activities = getQueryRuntimeAPI().getLightActivityInstances(instanceUUID);
    assertEquals(2, activities.size());
    LightActivityInstance activity = getLightActivityInstance(activities, "failingActivity");
    assertEquals(ActivityState.ABORTED, activity.getState());
    activity = getLightActivityInstance(activities, "exceptionStep");
    assertEquals(ActivityState.READY, activity.getState());

    final Set<LightActivityInstance> subflowActivities = getQueryRuntimeAPI().getLightActivityInstances(subflowInstanceUUID);
    assertEquals(1, subflowActivities.size());
    LightActivityInstance subActivity = subflowActivities.iterator().next();
    assertEquals("errorStep", subActivity.getActivityName());
    assertEquals(ActivityState.ABORTED, subActivity.getState());
    assertEquals(InstanceState.ABORTED, getQueryRuntimeAPI().getLightProcessInstance(subflowInstanceUUID).getInstanceState());

    getManagementAPI().deleteProcess(process.getUUID());
    getManagementAPI().deleteProcess(subProcess.getUUID());
  }

  private void errorWhenAConnectorFailsOnASystemTaskTwoPossibleErrors(Event event) throws Exception {
    ProcessDefinition process = ProcessBuilder.createProcess("simpleActivity", "1.0")
    .addHuman(getLogin())
    .addSystemTask("errorStep")
      .addErrorBoundaryEvent("otherError", "crash")
      .addErrorBoundaryEvent("error", "fail")
      .addConnector(event, SetVarConnector.class.getName(), false)
        .throwCatchError("fail")
        .addInputParameter("variableName", "unknownVariable")
        .addInputParameter("value", 10)
    .addHumanTask("exceptionStep", getLogin())
    .addHumanTask("otherExceptionStep", getLogin())
    .addExceptionTransition("errorStep", "error", "exceptionStep")
    .addExceptionTransition("errorStep", "otherError", "otherExceptionStep")
    .done();

    process = getManagementAPI().deploy(getBusinessArchive(process, null, SetVarConnector.class));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);
    Thread.sleep(waitingTime);
    assertEquals(InstanceState.STARTED, getQueryRuntimeAPI().getLightProcessInstance(instanceUUID).getInstanceState());
    Set<LightActivityInstance> activities = getQueryRuntimeAPI().getLightActivityInstances(instanceUUID);
    assertEquals(2, activities.size());
    LightActivityInstance activity = getLightActivityInstance(activities, "errorStep");
    assertEquals(ActivityState.ABORTED, activity.getState());
    activity = getLightActivityInstance(activities, "exceptionStep");
    assertEquals(ActivityState.READY, activity.getState());

    getManagementAPI().deleteProcess(processUUID);
  }

  private void errorWhenAConnectorFailsOnAHumanTaskTwoPossibleErrors(Event event) throws Exception {
    ProcessDefinition process = ProcessBuilder.createProcess("simpleActivity", "1.0")
    .addHuman(getLogin())
    .addHumanTask("errorStep", getLogin())
      .addErrorBoundaryEvent("otherError", "crash")
      .addErrorBoundaryEvent("error", "fail")
      .addConnector(event, SetVarConnector.class.getName(), false)
        .throwCatchError("fail")
        .addInputParameter("variableName", "unknownVariable")
        .addInputParameter("value", 10)
    .addHumanTask("exceptionStep", getLogin())
    .addHumanTask("otherExceptionStep", getLogin())
    .addExceptionTransition("errorStep", "error", "exceptionStep")
    .addExceptionTransition("errorStep", "otherError", "otherExceptionStep")
    .done();

    process = getManagementAPI().deploy(getBusinessArchive(process, null, SetVarConnector.class));
    final ProcessDefinitionUUID processUUID = process.getUUID();
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    if (!Event.taskOnReady.equals(event)) {
      Collection<LightTaskInstance> tasks = getQueryRuntimeAPI().getLightTaskList(ActivityState.READY);
      assertEquals(1, tasks.size());
      LightTaskInstance task = tasks.iterator().next();
      getRuntimeAPI().startTask(task.getUUID(), false);
      getRuntimeAPI().finishTask(task.getUUID(), false);
    }
    Thread.sleep(waitingTime);
    assertEquals(InstanceState.STARTED, getQueryRuntimeAPI().getLightProcessInstance(instanceUUID).getInstanceState());
    Set<LightActivityInstance> activities = getQueryRuntimeAPI().getLightActivityInstances(instanceUUID);
    assertEquals(2, activities.size());
    LightActivityInstance activity = getLightActivityInstance(activities, "errorStep");
    assertEquals(ActivityState.ABORTED, activity.getState());
    activity = getLightActivityInstance(activities, "exceptionStep");
    assertEquals(ActivityState.READY, activity.getState());

    getManagementAPI().deleteProcess(processUUID);
  }

  private void errorWhenAConnectorFailsOnASystemOfASubProcessTwoPossibleErrors(Event event) throws Exception {
    ProcessDefinition subProcess = ProcessBuilder.createProcess("subError", "1.0")
    .addSystemTask("errorStep")
      .addConnector(event, SetVarConnector.class.getName(), false)
        .throwCatchError("fail")
        .addInputParameter("variableName", "unknownVariable")
        .addInputParameter("value", 10)
    .done();

    ProcessDefinition process = ProcessBuilder.createProcess("main", "1.5")
    .addHuman(getLogin())
    .addSubProcess("failingActivity", "subError")
      .addErrorBoundaryEvent("error", "fail")
      .addErrorBoundaryEvent("otherError", "crash")
    .addHumanTask("exceptionStep", getLogin())
    .addHumanTask("otherExceptionStep", getLogin())
    .addExceptionTransition("failingActivity", "error", "exceptionStep")
    .addExceptionTransition("failingActivity", "otherError", "otherExceptionStep")
    .done();

    subProcess = getManagementAPI().deploy(getBusinessArchive(subProcess, null, SetVarConnector.class));
    process = getManagementAPI().deploy(getBusinessArchive(process));

    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(process.getUUID());
    final ProcessInstanceUUID subflowInstanceUUID = getQueryRuntimeAPI().getLightProcessInstances(subProcess.getUUID()).iterator().next().getUUID();
    Thread.sleep(waitingTime);
    assertEquals(InstanceState.STARTED, getQueryRuntimeAPI().getLightProcessInstance(instanceUUID).getInstanceState());
    Set<LightActivityInstance> activities = getQueryRuntimeAPI().getLightActivityInstances(instanceUUID);
    assertEquals(2, activities.size());
    LightActivityInstance activity = getLightActivityInstance(activities, "failingActivity");
    assertEquals(ActivityState.ABORTED, activity.getState());
    activity = getLightActivityInstance(activities, "exceptionStep");
    assertEquals(ActivityState.READY, activity.getState());

    final Set<LightActivityInstance> subflowActivities = getQueryRuntimeAPI().getLightActivityInstances(subflowInstanceUUID);
    assertEquals(1, subflowActivities.size());
    LightActivityInstance subActivity = subflowActivities.iterator().next();
    assertEquals("errorStep", subActivity.getActivityName());
    assertEquals(ActivityState.ABORTED, subActivity.getState());
    assertEquals(InstanceState.ABORTED, getQueryRuntimeAPI().getLightProcessInstance(subflowInstanceUUID).getInstanceState());

    getManagementAPI().deleteProcess(process.getUUID());
    getManagementAPI().deleteProcess(subProcess.getUUID());
  }

  private void errorWhenAConnectorFailsOnAHumanOfASubProcessTwoPossibleErrors(Event event) throws Exception {
    ProcessDefinition subProcess = ProcessBuilder.createProcess("subError", "1.0")
    .addHuman(getLogin())
    .addHumanTask("errorStep", getLogin())
      .addConnector(event, SetVarConnector.class.getName(), false)
        .throwCatchError("fail")
        .addInputParameter("variableName", "unknownVariable")
        .addInputParameter("value", 10)
    .done();

    ProcessDefinition process = ProcessBuilder.createProcess("main", "1.5")
    .addHuman(getLogin())
    .addSubProcess("failingActivity", "subError")
      .addErrorBoundaryEvent("error", "fail")
      .addErrorBoundaryEvent("otherError", "crash")
    .addHumanTask("exceptionStep", getLogin())
    .addHumanTask("otherExceptionStep", getLogin())
    .addExceptionTransition("failingActivity", "error", "exceptionStep")
    .addExceptionTransition("failingActivity", "otherError", "otherExceptionStep")
    .done();

    subProcess = getManagementAPI().deploy(getBusinessArchive(subProcess, null, SetVarConnector.class));
    process = getManagementAPI().deploy(getBusinessArchive(process));
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(process.getUUID());
    final ProcessInstanceUUID subflowInstanceUUID = getQueryRuntimeAPI().getLightProcessInstances(subProcess.getUUID()).iterator().next().getUUID();

    if (!Event.taskOnReady.equals(event)) {
      Set<LightTaskInstance> tasks = getQueryRuntimeAPI().getLightTasks(subflowInstanceUUID);
      LightTaskInstance task = tasks.iterator().next();
      getRuntimeAPI().startTask(task.getUUID(), false);
      getRuntimeAPI().finishTask(task.getUUID(), false);
    }

    Thread.sleep(waitingTime);
    assertEquals(InstanceState.STARTED, getQueryRuntimeAPI().getLightProcessInstance(instanceUUID).getInstanceState());
    Set<LightActivityInstance> activities = getQueryRuntimeAPI().getLightActivityInstances(instanceUUID);
    assertEquals(2, activities.size());
    LightActivityInstance activity = getLightActivityInstance(activities, "failingActivity");
    assertEquals(ActivityState.ABORTED, activity.getState());
    activity = getLightActivityInstance(activities, "exceptionStep");
    assertEquals(ActivityState.READY, activity.getState());

    final Set<LightActivityInstance> subflowActivities = getQueryRuntimeAPI().getLightActivityInstances(subflowInstanceUUID);
    assertEquals(1, subflowActivities.size());
    LightActivityInstance subActivity = subflowActivities.iterator().next();
    assertEquals("errorStep", subActivity.getActivityName());
    assertEquals(ActivityState.ABORTED, subActivity.getState());
    assertEquals(InstanceState.ABORTED, getQueryRuntimeAPI().getLightProcessInstance(subflowInstanceUUID).getInstanceState());

    getManagementAPI().deleteProcess(process.getUUID());
    getManagementAPI().deleteProcess(subProcess.getUUID());
  }

  private void errorWhenAConnectorFailsOnASystemTaskTwoErrors(Event event) throws Exception {
    ProcessDefinition process = ProcessBuilder.createProcess("simpleActivity", "1.0")
    .addHuman(getLogin())
    .addSystemTask("errorStep")
      .addErrorBoundaryEvent("otherError")
      .addErrorBoundaryEvent("error", "fail")
      .addConnector(event, SetVarConnector.class.getName(), false)
        .throwCatchError("fail")
        .addInputParameter("variableName", "unknownVariable")
        .addInputParameter("value", 10)
    .addHumanTask("exceptionStep", getLogin())
    .addHumanTask("otherExceptionStep", getLogin())
    .addExceptionTransition("errorStep", "error", "exceptionStep")
    .addExceptionTransition("errorStep", "otherError", "otherExceptionStep")
    .done();

    process = getManagementAPI().deploy(getBusinessArchive(process, null, SetVarConnector.class));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);
    Thread.sleep(2000);
    assertEquals(InstanceState.STARTED, getQueryRuntimeAPI().getLightProcessInstance(instanceUUID).getInstanceState());
    Set<LightActivityInstance> activities = getQueryRuntimeAPI().getLightActivityInstances(instanceUUID);
    assertEquals(2, activities.size());
    LightActivityInstance activity = getLightActivityInstance(activities, "errorStep");
    assertEquals(ActivityState.ABORTED, activity.getState());
    activity = getLightActivityInstance(activities, "exceptionStep");
    assertEquals(ActivityState.READY, activity.getState());

    getManagementAPI().deleteProcess(processUUID);
  }

  private void errorWhenAConnectorFailsOnAHumanTaskTwoErrors(Event event) throws Exception {
    ProcessDefinition process = ProcessBuilder.createProcess("simpleActivity", "1.0")
    .addHuman(getLogin())
    .addHumanTask("errorStep", getLogin())
      .addErrorBoundaryEvent("otherError")
      .addErrorBoundaryEvent("error", "fail")
      .addConnector(event, SetVarConnector.class.getName(), false)
        .throwCatchError("fail")
        .addInputParameter("variableName", "unknownVariable")
        .addInputParameter("value", 10)
    .addHumanTask("exceptionStep", getLogin())
    .addHumanTask("otherExceptionStep", getLogin())
    .addExceptionTransition("errorStep", "error", "exceptionStep")
    .addExceptionTransition("errorStep", "otherError", "otherExceptionStep")
    .done();

    process = getManagementAPI().deploy(getBusinessArchive(process, null, SetVarConnector.class));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);
    if (!Event.taskOnReady.equals(event)) {
      Collection<LightTaskInstance> tasks = getQueryRuntimeAPI().getLightTaskList(ActivityState.READY);
      assertEquals(1, tasks.size());
      LightTaskInstance task = tasks.iterator().next();
      getRuntimeAPI().startTask(task.getUUID(), false);
      getRuntimeAPI().finishTask(task.getUUID(), false);
    }
    Thread.sleep(waitingTime);
    assertEquals(InstanceState.STARTED, getQueryRuntimeAPI().getLightProcessInstance(instanceUUID).getInstanceState());
    Set<LightActivityInstance> activities = getQueryRuntimeAPI().getLightActivityInstances(instanceUUID);
    assertEquals(2, activities.size());
    LightActivityInstance activity = getLightActivityInstance(activities, "errorStep");
    assertEquals(ActivityState.ABORTED, activity.getState());
    activity = getLightActivityInstance(activities, "exceptionStep");
    assertEquals(ActivityState.READY, activity.getState());

    getManagementAPI().deleteProcess(processUUID);
  }

  private void errorWhenAConnectorFailsOnASystemOfASubProcessTwoErrors(Event event) throws Exception {
    ProcessDefinition subProcess = ProcessBuilder.createProcess("subError", "1.0")
    .addSystemTask("errorStep")
      .addConnector(event, SetVarConnector.class.getName(), false)
        .throwCatchError("fail")
        .addInputParameter("variableName", "unknownVariable")
        .addInputParameter("value", 10)
    .done();

    ProcessDefinition process = ProcessBuilder.createProcess("main", "1.5")
    .addHuman(getLogin())
    .addSubProcess("failingActivity", "subError")
      .addErrorBoundaryEvent("error", "fail")
      .addErrorBoundaryEvent("otherError")
    .addHumanTask("exceptionStep", getLogin())
    .addHumanTask("otherExceptionStep", getLogin())
    .addExceptionTransition("failingActivity", "error", "exceptionStep")
    .addExceptionTransition("failingActivity", "otherError", "otherExceptionStep")
    .done();

    subProcess = getManagementAPI().deploy(getBusinessArchive(subProcess, null, SetVarConnector.class));
    process = getManagementAPI().deploy(getBusinessArchive(process));
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(process.getUUID());
    final ProcessInstanceUUID subflowInstanceUUID = getQueryRuntimeAPI().getLightProcessInstances(subProcess.getUUID()).iterator().next().getUUID();

    Thread.sleep(waitingTime);
    assertEquals(InstanceState.STARTED, getQueryRuntimeAPI().getLightProcessInstance(instanceUUID).getInstanceState());
    Set<LightActivityInstance> activities = getQueryRuntimeAPI().getLightActivityInstances(instanceUUID);
    assertEquals(2, activities.size());
    LightActivityInstance activity = getLightActivityInstance(activities, "failingActivity");
    assertEquals(ActivityState.ABORTED, activity.getState());
    activity = getLightActivityInstance(activities, "exceptionStep");
    assertEquals(ActivityState.READY, activity.getState());

    final Set<LightActivityInstance> subflowActivities = getQueryRuntimeAPI().getLightActivityInstances(subflowInstanceUUID);
    assertEquals(1, subflowActivities.size());
    LightActivityInstance subActivity = subflowActivities.iterator().next();
    assertEquals("errorStep", subActivity.getActivityName());
    assertEquals(ActivityState.ABORTED, subActivity.getState());
    assertEquals(InstanceState.ABORTED, getQueryRuntimeAPI().getLightProcessInstance(subflowInstanceUUID).getInstanceState());

    getManagementAPI().deleteProcess(process.getUUID());
    getManagementAPI().deleteProcess(subProcess.getUUID());
  }

  private void errorWhenAConnectorFailsOnAHumanOfASubProcessTwoErrors(Event event) throws Exception {
    ProcessDefinition subProcess = ProcessBuilder.createProcess("subError", "1.0")
    .addHuman(getLogin())
    .addHumanTask("errorStep", getLogin())
      .addConnector(event, SetVarConnector.class.getName(), false)
        .throwCatchError("fail")
        .addInputParameter("variableName", "unknownVariable")
        .addInputParameter("value", 10)
    .done();

    ProcessDefinition process = ProcessBuilder.createProcess("main", "1.5")
    .addHuman(getLogin())
    .addSubProcess("failingActivity", "subError")
      .addErrorBoundaryEvent("error", "fail")
      .addErrorBoundaryEvent("otherError")
    .addHumanTask("exceptionStep", getLogin())
    .addHumanTask("otherExceptionStep", getLogin())
    .addExceptionTransition("failingActivity", "error", "exceptionStep")
    .addExceptionTransition("failingActivity", "otherError", "otherExceptionStep")
    .done();

    subProcess = getManagementAPI().deploy(getBusinessArchive(subProcess, null, SetVarConnector.class));
    process = getManagementAPI().deploy(getBusinessArchive(process));
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(process.getUUID());
    final ProcessInstanceUUID subflowInstanceUUID = getQueryRuntimeAPI().getLightProcessInstances(subProcess.getUUID()).iterator().next().getUUID();
    if (!Event.taskOnReady.equals(event)) {
      Set<LightTaskInstance> tasks = getQueryRuntimeAPI().getLightTasks(subflowInstanceUUID);
      LightTaskInstance task = tasks.iterator().next();
      getRuntimeAPI().startTask(task.getUUID(), false);
      getRuntimeAPI().finishTask(task.getUUID(), false);
    }

    Thread.sleep(waitingTime);
    assertEquals(InstanceState.STARTED, getQueryRuntimeAPI().getLightProcessInstance(instanceUUID).getInstanceState());
    Set<LightActivityInstance> activities = getQueryRuntimeAPI().getLightActivityInstances(instanceUUID);
    assertEquals(2, activities.size());
    LightActivityInstance activity = getLightActivityInstance(activities, "failingActivity");
    assertEquals(ActivityState.ABORTED, activity.getState());
    activity = getLightActivityInstance(activities, "exceptionStep");
    assertEquals(ActivityState.READY, activity.getState());

    final Set<LightActivityInstance> subflowActivities = getQueryRuntimeAPI().getLightActivityInstances(subflowInstanceUUID);
    assertEquals(1, subflowActivities.size());
    LightActivityInstance subActivity = subflowActivities.iterator().next();
    assertEquals("errorStep", subActivity.getActivityName());
    assertEquals(ActivityState.ABORTED, subActivity.getState());
    assertEquals(InstanceState.ABORTED, getQueryRuntimeAPI().getLightProcessInstance(subflowInstanceUUID).getInstanceState());

    getManagementAPI().deleteProcess(process.getUUID());
    getManagementAPI().deleteProcess(subProcess.getUUID());
  }
  
  public void testEndErrorEventTask() throws Exception {
    ProcessDefinition subProcess = ProcessBuilder.createProcess("inError", "1.4")
    .addHuman(getLogin())
    .addHumanTask("error", getLogin())
    .addErrorEventTask("Cancel_user", "Cncl")
    .addTransition("error", "Cancel_user")
    .done();

    ProcessDefinition mainProcess = ProcessBuilder.createProcess("followTheError", "1.6")
    .addHuman(getLogin())
    .addSubProcess("what_to_do", "inError")
      .addErrorBoundaryEvent("Cancel_user", "Cncl")
    .addHumanTask("normal", getLogin())
    .addHumanTask("exception", getLogin())
    .addTransition("what_to_do", "normal")
    .addExceptionTransition("what_to_do", "Cancel_user", "exception")
    .done();

    subProcess = getManagementAPI().deploy(getBusinessArchive(subProcess));
    mainProcess = getManagementAPI().deploy(getBusinessArchive(mainProcess));
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(mainProcess.getUUID());
    final ProcessInstanceUUID subflowInstanceUUID = getQueryRuntimeAPI().getLightProcessInstances(subProcess.getUUID()).iterator().next().getUUID();

    executeTask(subflowInstanceUUID, "error");
    Thread.sleep(waitingTime);

    assertEquals(InstanceState.STARTED, getQueryRuntimeAPI().getLightProcessInstance(instanceUUID).getInstanceState());
    Set<LightActivityInstance> activities = getQueryRuntimeAPI().getLightActivityInstances(instanceUUID);
    assertEquals(2, activities.size());
    LightActivityInstance activity = getLightActivityInstance(activities, "what_to_do");
    assertEquals(ActivityState.ABORTED, activity.getState());
    activity = getLightActivityInstance(activities, "exception");
    assertEquals(ActivityState.READY, activity.getState());

    assertEquals(InstanceState.ABORTED, getQueryRuntimeAPI().getLightProcessInstance(subflowInstanceUUID).getInstanceState());
    final Set<LightActivityInstance> subflowActivities = getQueryRuntimeAPI().getLightActivityInstances(subflowInstanceUUID);
    assertEquals(2, subflowActivities.size());
    LightActivityInstance subActivity = getLightActivityInstance(subflowActivities, "error");
    assertEquals(ActivityState.FINISHED, subActivity.getState());
    subActivity = getLightActivityInstance(subflowActivities, "Cancel_user");
    assertEquals(ActivityState.ABORTED, subActivity.getState());

    getManagementAPI().deleteProcess(mainProcess.getUUID());
    getManagementAPI().deleteProcess(subProcess.getUUID());
  }

  public void testEndErrorEventTaskSeveralPaths() throws Exception {
    ProcessDefinition subProcess = ProcessBuilder.createProcess("inError", "1.4")
    .addHuman(getLogin())
    .addHumanTask("error", getLogin())
    .addDecisionNode("XOR")
    .addErrorEventTask("Cancel_user", "Cncl")
    .addSystemTask("end")
    .addTransition("error", "XOR")
    .addTransition("XOR", "end")
      .addCondition("false")
    .addTransition("XOR", "Cancel_user")
      .addCondition("true")
    .done();

    ProcessDefinition mainProcess = ProcessBuilder.createProcess("followTheError", "1.6")
    .addHuman(getLogin())
    .addSubProcess("what_to_do", "inError")
      .addErrorBoundaryEvent("Cancel_user", "Cncl")
    .addHumanTask("normal", getLogin())
    .addHumanTask("exception", getLogin())
    .addTransition("what_to_do", "normal")
    .addExceptionTransition("what_to_do", "Cancel_user", "exception")
    .done();

    subProcess = getManagementAPI().deploy(getBusinessArchive(subProcess));
    mainProcess = getManagementAPI().deploy(getBusinessArchive(mainProcess));
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(mainProcess.getUUID());
    final ProcessInstanceUUID subflowInstanceUUID = getQueryRuntimeAPI().getLightProcessInstances(subProcess.getUUID()).iterator().next().getUUID();

    executeTask(subflowInstanceUUID, "error");
    Thread.sleep(waitingTime);

    assertEquals(InstanceState.STARTED, getQueryRuntimeAPI().getLightProcessInstance(instanceUUID).getInstanceState());
    Set<LightActivityInstance> activities = getQueryRuntimeAPI().getLightActivityInstances(instanceUUID);
    assertEquals(2, activities.size());
    LightActivityInstance activity = getLightActivityInstance(activities, "what_to_do");
    assertEquals(ActivityState.ABORTED, activity.getState());
    activity = getLightActivityInstance(activities, "exception");
    assertEquals(ActivityState.READY, activity.getState());

    assertEquals(InstanceState.ABORTED, getQueryRuntimeAPI().getLightProcessInstance(subflowInstanceUUID).getInstanceState());
    final Set<LightActivityInstance> subflowActivities = getQueryRuntimeAPI().getLightActivityInstances(subflowInstanceUUID);
    assertEquals(3, subflowActivities.size());
    LightActivityInstance subActivity = getLightActivityInstance(subflowActivities, "error");
    assertEquals(ActivityState.FINISHED, subActivity.getState());
    subActivity = getLightActivityInstance(subflowActivities, "Cancel_user");
    assertEquals(ActivityState.ABORTED, subActivity.getState());
    subActivity = getLightActivityInstance(subflowActivities, "XOR");
    assertEquals(ActivityState.FINISHED, subActivity.getState());

    getManagementAPI().deleteProcess(mainProcess.getUUID());
    getManagementAPI().deleteProcess(subProcess.getUUID());
  }

  public void testEndErrorEventTaskUsingTheAllExceptionPath() throws Exception {
    ProcessDefinition subProcess = ProcessBuilder.createProcess("inError", "1.4")
    .addHuman(getLogin())
    .addHumanTask("error", getLogin())
    .addDecisionNode("XOR")
    .addErrorEventTask("Cancel_user", "Cncl")
    .addSystemTask("end")
    .addTransition("error", "XOR")
    .addTransition("XOR", "end")
      .addCondition("false")
    .addTransition("XOR", "Cancel_user")
      .addCondition("true")
    .done();

    ProcessDefinition mainProcess = ProcessBuilder.createProcess("followTheError", "1.6")
    .addHuman(getLogin())
    .addSubProcess("what_to_do", "inError")
      .addErrorBoundaryEvent("Cancel_user")
    .addHumanTask("normal", getLogin())
    .addHumanTask("exception", getLogin())
    .addTransition("what_to_do", "normal")
    .addExceptionTransition("what_to_do", "Cancel_user", "exception")
    .done();

    subProcess = getManagementAPI().deploy(getBusinessArchive(subProcess));
    mainProcess = getManagementAPI().deploy(getBusinessArchive(mainProcess));
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(mainProcess.getUUID());
    final ProcessInstanceUUID subflowInstanceUUID = getQueryRuntimeAPI().getLightProcessInstances(subProcess.getUUID()).iterator().next().getUUID();

    executeTask(subflowInstanceUUID, "error");
    Thread.sleep(waitingTime);

    assertEquals(InstanceState.STARTED, getQueryRuntimeAPI().getLightProcessInstance(instanceUUID).getInstanceState());
    Set<LightActivityInstance> activities = getQueryRuntimeAPI().getLightActivityInstances(instanceUUID);
    assertEquals(2, activities.size());
    LightActivityInstance activity = getLightActivityInstance(activities, "what_to_do");
    assertEquals(ActivityState.ABORTED, activity.getState());
    activity = getLightActivityInstance(activities, "exception");
    assertEquals(ActivityState.READY, activity.getState());

    assertEquals(InstanceState.ABORTED, getQueryRuntimeAPI().getLightProcessInstance(subflowInstanceUUID).getInstanceState());
    final Set<LightActivityInstance> subflowActivities = getQueryRuntimeAPI().getLightActivityInstances(subflowInstanceUUID);
    assertEquals(3, subflowActivities.size());
    LightActivityInstance subActivity = getLightActivityInstance(subflowActivities, "error");
    assertEquals(ActivityState.FINISHED, subActivity.getState());
    subActivity = getLightActivityInstance(subflowActivities, "Cancel_user");
    assertEquals(ActivityState.ABORTED, subActivity.getState());
    subActivity = getLightActivityInstance(subflowActivities, "XOR");
    assertEquals(ActivityState.FINISHED, subActivity.getState());

    getManagementAPI().deleteProcess(mainProcess.getUUID());
    getManagementAPI().deleteProcess(subProcess.getUUID());
  }

  public void testEndErrorEventTaskUsingTheAllExceptionPathDueToSpectifcErrorCodeDoesNotMatch() throws Exception {
    ProcessDefinition subProcess = ProcessBuilder.createProcess("inError", "1.4")
    .addHuman(getLogin())
    .addHumanTask("error", getLogin())
    .addDecisionNode("XOR")
    .addErrorEventTask("Cancel_user", "Cncl")
    .addSystemTask("end")
    .addTransition("error", "XOR")
    .addTransition("XOR", "end")
      .addCondition("false")
    .addTransition("XOR", "Cancel_user")
      .addCondition("true")
    .done();

    ProcessDefinition mainProcess = ProcessBuilder.createProcess("followTheError", "1.6")
    .addHuman(getLogin())
    .addSubProcess("what_to_do", "inError")
      .addErrorBoundaryEvent("Cancel_user")
      .addErrorBoundaryEvent("other_cancel", "other")
    .addHumanTask("normal", getLogin())
    .addHumanTask("exception1", getLogin())
    .addHumanTask("exception2", getLogin())
    .addTransition("what_to_do", "normal")
    .addExceptionTransition("what_to_do", "Cancel_user", "exception1")
    .addExceptionTransition("what_to_do", "other_cancel", "exception2")
    .done();

    subProcess = getManagementAPI().deploy(getBusinessArchive(subProcess));
    mainProcess = getManagementAPI().deploy(getBusinessArchive(mainProcess));
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(mainProcess.getUUID());
    final ProcessInstanceUUID subflowInstanceUUID = getQueryRuntimeAPI().getLightProcessInstances(subProcess.getUUID()).iterator().next().getUUID();

    executeTask(subflowInstanceUUID, "error");
    Thread.sleep(waitingTime);

    assertEquals(InstanceState.STARTED, getQueryRuntimeAPI().getLightProcessInstance(instanceUUID).getInstanceState());
    Set<LightActivityInstance> activities = getQueryRuntimeAPI().getLightActivityInstances(instanceUUID);
    assertEquals(2, activities.size());
    LightActivityInstance activity = getLightActivityInstance(activities, "what_to_do");
    assertEquals(ActivityState.ABORTED, activity.getState());
    activity = getLightActivityInstance(activities, "exception1");
    assertEquals(ActivityState.READY, activity.getState());

    assertEquals(InstanceState.ABORTED, getQueryRuntimeAPI().getLightProcessInstance(subflowInstanceUUID).getInstanceState());
    final Set<LightActivityInstance> subflowActivities = getQueryRuntimeAPI().getLightActivityInstances(subflowInstanceUUID);
    assertEquals(3, subflowActivities.size());
    LightActivityInstance subActivity = getLightActivityInstance(subflowActivities, "error");
    assertEquals(ActivityState.FINISHED, subActivity.getState());
    subActivity = getLightActivityInstance(subflowActivities, "Cancel_user");
    assertEquals(ActivityState.ABORTED, subActivity.getState());
    subActivity = getLightActivityInstance(subflowActivities, "XOR");
    assertEquals(ActivityState.FINISHED, subActivity.getState());

    getManagementAPI().deleteProcess(mainProcess.getUUID());
    getManagementAPI().deleteProcess(subProcess.getUUID());
  }

  public void testRestartActivityOnBoundaryEvent() throws Exception {
    ProcessDefinition process = ProcessBuilder.createProcess("restart", "1.0")
    .addHuman(getLogin())
    .addSystemTask("start")
    .addDecisionNode("XOR")
      .addJoinType(JoinType.XOR)
    .addHumanTask("try", getLogin())
    .addHumanTask("fails", getLogin())
      .addConnector(Event.taskOnFinish, SetVarConnector.class.getName(), false)
        .throwCatchError("fail")
        .addInputParameter("variableName", "unknownVariable")
        .addInputParameter("value", 10)
      .addErrorBoundaryEvent("retry", "fail")
    .addHumanTask("serviceRetry", getLogin())
    .addSystemTask("end")
    .addTransition("start", "XOR")
    .addTransition("XOR", "try")
    .addTransition("try", "fails")
    .addTransition("fails", "end")
    .addExceptionTransition("fails", "retry", "serviceRetry")
    .addTransition("serviceRetry", "XOR")
    .done();

    process = getManagementAPI().deploy(getBusinessArchive(process, null, SetVarConnector.class));
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(process.getUUID());

    executeTask(instanceUUID, "try");
    Collection<LightTaskInstance> tasks = getQueryRuntimeAPI().getLightTaskList(ActivityState.READY);
    LightTaskInstance task = tasks.iterator().next();
    getRuntimeAPI().startTask(task.getUUID(), false);
    getRuntimeAPI().finishTask(task.getUUID(), false);
    Thread.sleep(waitingTime);
    Set<LightActivityInstance> activities = getQueryRuntimeAPI().getLightActivityInstances(instanceUUID);
    assertEquals(5, activities.size());
    LightActivityInstance activity = getLightActivityInstance(activities, "serviceRetry");
    assertEquals(ActivityState.READY, activity.getState());

    executeTask(instanceUUID, "serviceRetry");
    activities = getQueryRuntimeAPI().getLightActivityInstances(instanceUUID);
    assertEquals(7, activities.size());
    tasks = getQueryRuntimeAPI().getLightTaskList(ActivityState.READY);
    assertEquals(1, tasks.size());
    task = tasks.iterator().next();
    assertEquals("try", task.getActivityName());
    assertNotSame("it1", task.getIterationId());

    getManagementAPI().deleteProcess(process.getUUID());
  }

  public void testArchiveProcessWhenNoErrorCodeSent() throws Exception {
    ProcessDefinition process = ProcessBuilder.createProcess("inError", "1.4")
    .addSystemTask("error")
    .addErrorEventTask("Cancel_user", "Cncl")
    .addTransition("error", "Cancel_user")
    .done();

    process = getManagementAPI().deploy(getBusinessArchive(process));
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(process.getUUID());
    Thread.sleep(waitingTime);

    QueryRuntimeAPI queryRuntimAPI = AccessorUtil.getQueryRuntimeAPI(AccessorUtil.QUERYLIST_HISTORY_KEY);
    assertEquals(InstanceState.ABORTED, queryRuntimAPI.getLightProcessInstance(instanceUUID).getInstanceState());
    Set<LightActivityInstance> activities = queryRuntimAPI.getLightActivityInstances(instanceUUID);
    assertEquals(2, activities.size());
    LightActivityInstance activity = getLightActivityInstance(activities, "error");
    assertEquals(ActivityState.FINISHED, activity.getState());
    activity = getLightActivityInstance(activities, "Cancel_user");
    assertEquals(ActivityState.ABORTED, activity.getState());

    getManagementAPI().deleteProcess(process.getUUID());
  }

  public void testActivityFailsWhenAConnectorThrowsAnErrorAndImpossibleToCatchIt() throws Exception {
    ProcessDefinition process = ProcessBuilder.createProcess("simpleActivity", "1.0")
    .addHuman(getLogin())
    .addHumanTask("errorStep", getLogin())
      .addConnector(Event.taskOnFinish, SetVarConnector.class.getName(), false)
        .throwCatchError("fail")
        .addInputParameter("variableName", "unknownVariable")
        .addInputParameter("value", 10)
    .addHumanTask("nextStep", getLogin())
    .addTransition("errorStep", "nextStep")
    .done();

    process = getManagementAPI().deploy(getBusinessArchive(process, null, SetVarConnector.class));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);
    Set<LightActivityInstance> activities = getQueryRuntimeAPI().getLightActivityInstances(instanceUUID);
    assertEquals(1, activities.size());
    LightActivityInstance task = activities.iterator().next();
    getRuntimeAPI().executeTask(task.getUUID(), false);
    Thread.sleep(waitingTime);
    QueryRuntimeAPI queryRuntimAPI = AccessorUtil.getQueryRuntimeAPI(AccessorUtil.QUERYLIST_JOURNAL_KEY);
    assertEquals(InstanceState.STARTED, queryRuntimAPI.getLightProcessInstance(instanceUUID).getInstanceState());
    activities = queryRuntimAPI.getLightActivityInstances(instanceUUID);
    assertEquals(1, activities.size());
    LightActivityInstance activity = getLightActivityInstance(activities, "errorStep");
    assertEquals(ActivityState.FAILED, activity.getState());
    getManagementAPI().deleteProcess(processUUID);
  }

}
