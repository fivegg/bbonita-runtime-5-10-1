package org.ow2.bonita.event;

import java.util.Set;

import org.bonitasoft.connectors.bonita.joincheckers.FixedNumberJoinChecker;
import org.ow2.bonita.APITestCase;
import org.ow2.bonita.activity.multipleinstances.instantiator.NoContextMulitpleActivitiesInstantiator;
import org.ow2.bonita.facade.QueryRuntimeAPI;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.runtime.ActivityInstance;
import org.ow2.bonita.facade.runtime.ActivityState;
import org.ow2.bonita.facade.runtime.TaskInstance;
import org.ow2.bonita.facade.uuid.ActivityInstanceUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.util.AccessorUtil;
import org.ow2.bonita.util.ProcessBuilder;

public class BoudaryEventsAndMultipleInstancesTest extends APITestCase {

  public void testTimer() throws Exception {
    ProcessDefinition definition = ProcessBuilder.createProcess("multiple", "1.0").addHuman(getLogin())
        .addHumanTask("multi", getLogin())
        .addMultipleActivitiesInstantiator(NoContextMulitpleActivitiesInstantiator.class.getName())
        .addInputParameter("number", 10).addMultipleActivitiesJoinChecker(FixedNumberJoinChecker.class.getName())
        .addInputParameter("activityNumber", 3).addTimerBoundaryEvent("time", "2000").addSystemTask("stop")
        .addExceptionTransition("multi", "time", "stop").done();

    definition = getManagementAPI().deploy(
        getBusinessArchive(definition, null, NoContextMulitpleActivitiesInstantiator.class,
            FixedNumberJoinChecker.class));
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(definition.getUUID());
    waitForInstanceEnd(10000, 100, instanceUUID);

    final QueryRuntimeAPI queryRuntimeAPI = AccessorUtil.getQueryRuntimeAPI(AccessorUtil.QUERYLIST_HISTORY_KEY);
    Set<ActivityInstance> activityInstances = queryRuntimeAPI.getActivityInstances(instanceUUID, "multi");
    assertEquals(10, activityInstances.size());
    for (final ActivityInstance activityInstance : activityInstances) {
      assertEquals(ActivityState.ABORTED, activityInstance.getState());
    }

    activityInstances = queryRuntimeAPI.getActivityInstances(instanceUUID, "stop");
    assertEquals(1, activityInstances.size());

    getManagementAPI().deleteProcess(definition.getUUID());
  }

  public void testSignal() throws Exception {
    ProcessDefinition definition = ProcessBuilder.createProcess("multiple", "1.0").addHuman(getLogin())
        .addHumanTask("multi", getLogin())
        .addMultipleActivitiesInstantiator(NoContextMulitpleActivitiesInstantiator.class.getName())
        .addInputParameter("number", 10).addMultipleActivitiesJoinChecker(FixedNumberJoinChecker.class.getName())
        .addInputParameter("activityNumber", 3).addSignalBoundaryEvent("signal", "GoGoGo").addSystemTask("stop")
        .addExceptionTransition("multi", "signal", "stop").done();

    final ProcessDefinition end = ProcessBuilder.createProcess("endSignal", "1.0").addSystemTask("start")
        .addSignalEventTask("end", "GoGoGo").addTransition("start", "end").done();

    definition = getManagementAPI().deploy(
        getBusinessArchive(definition, null, NoContextMulitpleActivitiesInstantiator.class,
            FixedNumberJoinChecker.class));
    final ProcessDefinition endDefinition = getManagementAPI().deploy(getBusinessArchive(end));

    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(definition.getUUID());

    final ActivityInstanceUUID taskUUID = getQueryRuntimeAPI().getOneTask(instanceUUID, ActivityState.READY);
    final TaskInstance task = getQueryRuntimeAPI().getTask(taskUUID);
    assertEquals("multi", task.getActivityName());
    getRuntimeAPI().executeTask(taskUUID, true);
    getRuntimeAPI().instantiateProcess(endDefinition.getUUID());
    waitForInstanceEnd(10000, 100, instanceUUID);

    final QueryRuntimeAPI queryRuntimeAPI = AccessorUtil.getQueryRuntimeAPI(AccessorUtil.QUERYLIST_HISTORY_KEY);
    Set<ActivityInstance> activityInstances = queryRuntimeAPI.getActivityInstances(instanceUUID, "multi");
    assertEquals(10, activityInstances.size());
    int abortedSteps = 0;
    int finishedSteps = 0;
    for (final ActivityInstance activityInstance : activityInstances) {
      if (activityInstance.getState() == ActivityState.ABORTED) {
        abortedSteps++;
      } else {
        finishedSteps++;
      }
    }
    assertEquals(1, finishedSteps);
    assertEquals(9, abortedSteps);

    activityInstances = queryRuntimeAPI.getActivityInstances(instanceUUID, "stop");
    assertEquals(1, activityInstances.size());

    getManagementAPI().deleteProcess(endDefinition.getUUID());
    getManagementAPI().deleteProcess(definition.getUUID());
  }

}
