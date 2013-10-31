package org.ow2.bonita.event;

import java.util.Set;

import org.ow2.bonita.APITestCase;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.runtime.ActivityState;
import org.ow2.bonita.facade.runtime.InstanceState;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.light.LightActivityInstance;
import org.ow2.bonita.light.LightProcessInstance;
import org.ow2.bonita.util.ProcessBuilder;

public class SignalEventTest extends APITestCase {

  public void testStartSignalEventWithoutWaitingASignal() throws Exception {
    ProcessDefinition start = ProcessBuilder.createProcess("startSignal", "0.9")
    .addHuman(getLogin())
    .addSignalEventTask("start", "engineStart", true)
    .addHumanTask("manual", getLogin())
    .addTransition("start", "manual")
    .done();

    ProcessDefinition end = ProcessBuilder.createProcess("endSignal", "1.0")
    .addHuman(getLogin())
    .addSignalEventTask("end", "engineStart")
    .addHumanTask("manual", getLogin())
    .addTransition("manual", "end")
    .done();

    start = getManagementAPI().deploy(getBusinessArchive(start));
    end = getManagementAPI().deploy(getBusinessArchive(end));

    getRuntimeAPI().instantiateProcess(start.getUUID());
    Set<LightProcessInstance> instances = getQueryRuntimeAPI().getLightProcessInstances();
    assertEquals(1, instances.size());
    LightProcessInstance instance = getLightProcessInstance(instances, "startSignal", "0.9");
    assertEquals(InstanceState.STARTED, instance.getInstanceState());
    Set<LightActivityInstance> activities = getQueryRuntimeAPI().getLightActivityInstances(instance.getUUID());
    assertEquals(2, activities.size());
    LightActivityInstance activity = getLightActivityInstance(activities, "start");
    assertEquals(ActivityState.FINISHED, activity.getState());
    activity = getLightActivityInstance(activities, "manual");
    assertEquals(ActivityState.READY, activity.getState());

    getManagementAPI().deleteProcess(start.getUUID());
    getManagementAPI().deleteProcess(end.getUUID());
  }

  public void testAStartAndAnEndSignalEvents() throws Exception {
    ProcessDefinition start = ProcessBuilder.createProcess("startSignal", "1.0")
    .addHuman(getLogin())
    .addSignalEventTask("start", "engineStart", true)
    .addHumanTask("manual", getLogin())
    .addTransition("start", "manual")
    .done();

    ProcessDefinition end = ProcessBuilder.createProcess("endSignal", "1.0")
    .addHuman(getLogin())
    .addSignalEventTask("end", "engineStart")
    .addHumanTask("manual", getLogin())
    .addTransition("manual", "end")
    .done();

    start = getManagementAPI().deploy(getBusinessArchive(start));
    end = getManagementAPI().deploy(getBusinessArchive(end));

    ProcessInstanceUUID endUUID = getRuntimeAPI().instantiateProcess(end.getUUID());
    executeTask(endUUID, "manual");
    Thread.sleep(5000);
    Set<LightProcessInstance> instances = getQueryRuntimeAPI().getLightProcessInstances();
    assertEquals(2, instances.size());
    LightProcessInstance instance = getLightProcessInstance(instances, "startSignal", "1.0");
    assertEquals(InstanceState.STARTED, instance.getInstanceState());
    Set<LightActivityInstance> activities = getQueryRuntimeAPI().getLightActivityInstances(instance.getUUID());
    assertEquals(2, activities.size());
    LightActivityInstance activity = getLightActivityInstance(activities, "start");
    assertEquals(ActivityState.FINISHED, activity.getState());
    activity = getLightActivityInstance(activities, "manual");
    assertEquals(ActivityState.READY, activity.getState());

    instance = getLightProcessInstance(instances, "endSignal", "1.0");
    assertEquals(InstanceState.FINISHED, instance.getInstanceState());
    activities = getQueryRuntimeAPI().getLightActivityInstances(instance.getUUID());
    assertEquals(2, activities.size());
    activity = getLightActivityInstance(activities, "end");
    assertEquals(ActivityState.FINISHED, activity.getState());
    activity = getLightActivityInstance(activities, "manual");
    assertEquals(ActivityState.FINISHED, activity.getState());

    getManagementAPI().deleteProcess(start.getUUID());
    getManagementAPI().deleteProcess(end.getUUID());
  }

  public void testTwoStartAndAnEndSignalEvents() throws Exception {
    ProcessDefinition startOne = ProcessBuilder.createProcess("startOneSignal", "1.1")
    .addHuman(getLogin())
    .addSignalEventTask("start", "engineStart", true)
    .addHumanTask("manual", getLogin())
    .addTransition("start", "manual")
    .done();

    ProcessDefinition startTwo = ProcessBuilder.createProcess("startTwoSignal", "1.1")
    .addHuman(getLogin())
    .addSignalEventTask("start", "engineStart", true)
    .addHumanTask("manual", getLogin())
    .addTransition("start", "manual")
    .done();

    ProcessDefinition end = ProcessBuilder.createProcess("endSignal", "1.1")
    .addHuman(getLogin())
    .addSignalEventTask("end", "engineStart")
    .addHumanTask("manual", getLogin())
    .addTransition("manual", "end")
    .done();

    startOne = getManagementAPI().deploy(getBusinessArchive(startOne));
    startTwo = getManagementAPI().deploy(getBusinessArchive(startTwo));
    end = getManagementAPI().deploy(getBusinessArchive(end));

    ProcessInstanceUUID endUUID = getRuntimeAPI().instantiateProcess(end.getUUID());
    executeTask(endUUID, "manual");

    Thread.sleep(3000);
    Set<LightProcessInstance> instances = getQueryRuntimeAPI().getLightProcessInstances();
    assertEquals(3, instances.size());

    LightProcessInstance instance = getLightProcessInstance(instances, "startOneSignal", "1.1");
    assertEquals(InstanceState.STARTED, instance.getInstanceState());
    Set<LightActivityInstance> activities = getQueryRuntimeAPI().getLightActivityInstances(instance.getUUID());
    assertEquals(2, activities.size());
    LightActivityInstance activity = getLightActivityInstance(activities, "start");
    assertEquals(ActivityState.FINISHED, activity.getState());
    activity = getLightActivityInstance(activities, "manual");
    assertEquals(ActivityState.READY, activity.getState());

    instance = getLightProcessInstance(instances, "startTwoSignal", "1.1");
    assertEquals(InstanceState.STARTED, instance.getInstanceState());
    activities = getQueryRuntimeAPI().getLightActivityInstances(instance.getUUID());
    assertEquals(2, activities.size());
    activity = getLightActivityInstance(activities, "start");
    assertEquals(ActivityState.FINISHED, activity.getState());
    activity = getLightActivityInstance(activities, "manual");
    assertEquals(ActivityState.READY, activity.getState());

    instance = getLightProcessInstance(instances, "endSignal", "1.1");
    assertEquals(InstanceState.FINISHED, instance.getInstanceState());
    activities = getQueryRuntimeAPI().getLightActivityInstances(instance.getUUID());
    assertEquals(2, activities.size());
    activity = getLightActivityInstance(activities, "end");
    assertEquals(ActivityState.FINISHED, activity.getState());
    activity = getLightActivityInstance(activities, "manual");
    assertEquals(ActivityState.FINISHED, activity.getState());

    getManagementAPI().deleteProcess(startOne.getUUID());
    getManagementAPI().deleteProcess(startTwo.getUUID());
    getManagementAPI().deleteProcess(end.getUUID());
  }

  public void testAStartAndAnEndSignalEventsWithoutMatching() throws Exception {
    ProcessDefinition start = ProcessBuilder.createProcess("startSignal", "1.2")
    .addHuman(getLogin())
    .addSignalEventTask("start", "go", true)
    .addHumanTask("manual", getLogin())
    .addTransition("start", "manual")
    .done();

    ProcessDefinition end = ProcessBuilder.createProcess("endSignal", "1.2")
    .addHuman(getLogin())
    .addSignalEventTask("end", "engineStart")
    .addHumanTask("manual", getLogin())
    .addTransition("manual", "end")
    .done();

    start = getManagementAPI().deploy(getBusinessArchive(start));
    end = getManagementAPI().deploy(getBusinessArchive(end));

    ProcessInstanceUUID endUUID = getRuntimeAPI().instantiateProcess(end.getUUID());
    executeTask(endUUID, "manual");
    Thread.sleep(5000);
    Set<LightProcessInstance> instances = getQueryRuntimeAPI().getLightProcessInstances();
    assertEquals(1, instances.size());

    LightProcessInstance instance = getLightProcessInstance(instances, "endSignal", "1.2");
    assertEquals(InstanceState.FINISHED, instance.getInstanceState());
    Set<LightActivityInstance> activities = getQueryRuntimeAPI().getLightActivityInstances(instance.getUUID());
    assertEquals(2, activities.size());
    LightActivityInstance activity = getLightActivityInstance(activities, "end");
    assertEquals(ActivityState.FINISHED, activity.getState());
    activity = getLightActivityInstance(activities, "manual");
    assertEquals(ActivityState.FINISHED, activity.getState());

    getManagementAPI().deleteProcess(start.getUUID());
    getManagementAPI().deleteProcess(end.getUUID());
  }

  public void testTwoStartAndAnEndSignalEventsOnlyOneMatch() throws Exception {
    ProcessDefinition startOne = ProcessBuilder.createProcess("startOneSignal", "1.3")
    .addHuman(getLogin())
    .addSignalEventTask("start", "engineStart", true)
    .addHumanTask("manual", getLogin())
    .addTransition("start", "manual")
    .done();

    ProcessDefinition startTwo = ProcessBuilder.createProcess("startTwoSignal", "1.3")
    .addHuman(getLogin())
    .addSignalEventTask("start", "go", true)
    .addHumanTask("manual", getLogin())
    .addTransition("start", "manual")
    .done();

    ProcessDefinition end = ProcessBuilder.createProcess("endSignal", "1.3")
    .addHuman(getLogin())
    .addSignalEventTask("end", "engineStart")
    .addHumanTask("manual", getLogin())
    .addTransition("manual", "end")
    .done();

    startOne = getManagementAPI().deploy(getBusinessArchive(startOne));
    startTwo = getManagementAPI().deploy(getBusinessArchive(startTwo));
    end = getManagementAPI().deploy(getBusinessArchive(end));

    ProcessInstanceUUID endUUID = getRuntimeAPI().instantiateProcess(end.getUUID());
    executeTask(endUUID, "manual");
    Thread.sleep(5000);
    Set<LightProcessInstance> instances = getQueryRuntimeAPI().getLightProcessInstances();
    assertEquals(2, instances.size());

    LightProcessInstance instance = getLightProcessInstance(instances, "startOneSignal", "1.3");
    assertEquals(InstanceState.STARTED, instance.getInstanceState());
    Set<LightActivityInstance> activities = getQueryRuntimeAPI().getLightActivityInstances(instance.getUUID());
    assertEquals(2, activities.size());
    LightActivityInstance activity = getLightActivityInstance(activities, "start");
    assertEquals(ActivityState.FINISHED, activity.getState());
    activity = getLightActivityInstance(activities, "manual");
    assertEquals(ActivityState.READY, activity.getState());

    instance = getLightProcessInstance(instances, "endSignal", "1.3");
    assertEquals(InstanceState.FINISHED, instance.getInstanceState());
    activities = getQueryRuntimeAPI().getLightActivityInstances(instance.getUUID());
    assertEquals(2, activities.size());
    activity = getLightActivityInstance(activities, "end");
    assertEquals(ActivityState.FINISHED, activity.getState());
    activity = getLightActivityInstance(activities, "manual");
    assertEquals(ActivityState.FINISHED, activity.getState());

    getManagementAPI().deleteProcess(startOne.getUUID());
    getManagementAPI().deleteProcess(startTwo.getUUID());
    getManagementAPI().deleteProcess(end.getUUID());
  }

  public void testIntermediateCatchSignalEventTest() throws Exception {
    ProcessDefinition end = ProcessBuilder.createProcess("endSignal", "1.3")
    .addHuman(getLogin())
    .addSignalEventTask("end", "engineStart")
    .addHumanTask("manual", getLogin())
    .addTransition("manual", "end")
    .done();

    ProcessDefinition intermediate = ProcessBuilder.createProcess("intermediateSignal", "1.0")
    .addHuman(getLogin())
    .addHumanTask("manual", getLogin())
    .addSignalEventTask("signal", "engineStart", true)
    .addHumanTask("end", getLogin())
    .addTransition("manual", "signal")
    .addTransition("signal", "end")
    .done();

    intermediate = getManagementAPI().deploy(getBusinessArchive(intermediate));
    end = getManagementAPI().deploy(getBusinessArchive(end));

    ProcessInstanceUUID intermediateUUID = getRuntimeAPI().instantiateProcess(intermediate.getUUID());
    executeTask(intermediateUUID, "manual");
    Set<LightProcessInstance> instances = getQueryRuntimeAPI().getLightProcessInstances();
    assertEquals(1, instances.size());
    LightProcessInstance instance = getLightProcessInstance(instances, "intermediateSignal", "1.0");
    assertEquals(InstanceState.STARTED, instance.getInstanceState());
    Set<LightActivityInstance> activities = getQueryRuntimeAPI().getLightActivityInstances(instance.getUUID());
    assertEquals(2, activities.size());
    LightActivityInstance activity = getLightActivityInstance(activities, "signal");
    assertEquals(ActivityState.EXECUTING, activity.getState());
    activity = getLightActivityInstance(activities, "manual");
    assertEquals(ActivityState.FINISHED, activity.getState());

    ProcessInstanceUUID endUUID = getRuntimeAPI().instantiateProcess(end.getUUID());
    executeTask(endUUID, "manual");
    Thread.sleep(5000);
    instances = getQueryRuntimeAPI().getLightProcessInstances();
    assertEquals(2, instances.size());
    instance = getLightProcessInstance(instances, "intermediateSignal", "1.0");
    assertEquals(InstanceState.STARTED, instance.getInstanceState());
    activities = getQueryRuntimeAPI().getLightActivityInstances(instance.getUUID());
    assertEquals(3, activities.size());
    activity = getLightActivityInstance(activities, "signal");
    assertEquals(ActivityState.FINISHED, activity.getState());
    activity = getLightActivityInstance(activities, "manual");
    assertEquals(ActivityState.FINISHED, activity.getState());
    activity = getLightActivityInstance(activities, "end");
    assertEquals(ActivityState.READY, activity.getState());

    instance = getLightProcessInstance(instances, "endSignal", "1.3");
    assertEquals(InstanceState.FINISHED, instance.getInstanceState());
    activities = getQueryRuntimeAPI().getLightActivityInstances(instance.getUUID());
    assertEquals(2, activities.size());
    activity = getLightActivityInstance(activities, "end");
    assertEquals(ActivityState.FINISHED, activity.getState());
    activity = getLightActivityInstance(activities, "manual");
    assertEquals(ActivityState.FINISHED, activity.getState());

    getManagementAPI().deleteProcess(intermediate.getUUID());
    getManagementAPI().deleteProcess(end.getUUID());
  }

  public void testIntermediateThrowSignalEventTest() throws Exception {
    ProcessDefinition start = ProcessBuilder.createProcess("startSignal", "1.0")
    .addHuman(getLogin())
    .addSignalEventTask("start", "engineStart", true)
    .addHumanTask("manual", getLogin())
    .addTransition("start", "manual")
    .done();

    ProcessDefinition end = ProcessBuilder.createProcess("intermediateSignal", "1.0")
    .addHuman(getLogin())
    .addHumanTask("manual", getLogin())
    .addSignalEventTask("signal", "engineStart", false)
    .addHumanTask("end", getLogin())
    .addTransition("manual", "signal")
    .addTransition("signal", "end")
    .done();

    start = getManagementAPI().deploy(getBusinessArchive(start));
    end = getManagementAPI().deploy(getBusinessArchive(end));

    ProcessInstanceUUID endUUID = getRuntimeAPI().instantiateProcess(end.getUUID());
    executeTask(endUUID, "manual");
    Thread.sleep(5000);
    Set<LightProcessInstance> instances = getQueryRuntimeAPI().getLightProcessInstances();
    assertEquals(2, instances.size());
    LightProcessInstance instance = getLightProcessInstance(instances, "startSignal", "1.0");
    assertEquals(InstanceState.STARTED, instance.getInstanceState());
    Set<LightActivityInstance> activities = getQueryRuntimeAPI().getLightActivityInstances(instance.getUUID());
    assertEquals(2, activities.size());
    LightActivityInstance activity = getLightActivityInstance(activities, "start");
    assertEquals(ActivityState.FINISHED, activity.getState());
    activity = getLightActivityInstance(activities, "manual");
    assertEquals(ActivityState.READY, activity.getState());

    instance = getLightProcessInstance(instances, "intermediateSignal", "1.0");
    assertEquals(InstanceState.STARTED, instance.getInstanceState());
    activities = getQueryRuntimeAPI().getLightActivityInstances(instance.getUUID());
    assertEquals(3, activities.size());
    activity = getLightActivityInstance(activities, "end");
    assertEquals(ActivityState.READY, activity.getState());
    activity = getLightActivityInstance(activities, "manual");
    assertEquals(ActivityState.FINISHED, activity.getState());
    activity = getLightActivityInstance(activities, "signal");
    assertEquals(ActivityState.FINISHED, activity.getState());

    getManagementAPI().deleteProcess(start.getUUID());
    getManagementAPI().deleteProcess(end.getUUID());
  }

  public void testABoundaryAndAnEndSignalEvents() throws Exception {
    ProcessDefinition start = ProcessBuilder.createProcess("boundarySignal", "1.0")
    .addHuman(getLogin())
    .addHumanTask("start", getLogin())
      .addSignalBoundaryEvent("signal", "engineStart")
    .addHumanTask("manual", getLogin())
    .addHumanTask("exception", getLogin())
    .addTransition("start", "manual")
    .addExceptionTransition("start", "signal", "exception")
    .done();

    ProcessDefinition end = ProcessBuilder.createProcess("endSignal", "1.0")
    .addHuman(getLogin())
    .addHumanTask("manual", getLogin())
    .addSignalEventTask("end", "engineStart")
    .addTransition("manual", "end")
    .done();

    start = getManagementAPI().deploy(getBusinessArchive(start));
    end = getManagementAPI().deploy(getBusinessArchive(end));

    getRuntimeAPI().instantiateProcess(start.getUUID());
    ProcessInstanceUUID endUUID = getRuntimeAPI().instantiateProcess(end.getUUID());
    executeTask(endUUID, "manual");
    Thread.sleep(5000);
    Set<LightProcessInstance> instances = getQueryRuntimeAPI().getLightProcessInstances();
    assertEquals(2, instances.size());
    LightProcessInstance instance = getLightProcessInstance(instances, "boundarySignal", "1.0");
    assertEquals(InstanceState.STARTED, instance.getInstanceState());
    Set<LightActivityInstance> activities = getQueryRuntimeAPI().getLightActivityInstances(instance.getUUID());
    assertEquals(2, activities.size());
    LightActivityInstance activity = getLightActivityInstance(activities, "start");
    assertEquals(ActivityState.ABORTED, activity.getState());
    activity = getLightActivityInstance(activities, "exception");
    assertEquals(ActivityState.READY, activity.getState());

    instance = getLightProcessInstance(instances, "endSignal", "1.0");
    assertEquals(InstanceState.FINISHED, instance.getInstanceState());
    activities = getQueryRuntimeAPI().getLightActivityInstances(instance.getUUID());
    assertEquals(2, activities.size());
    activity = getLightActivityInstance(activities, "end");
    assertEquals(ActivityState.FINISHED, activity.getState());
    activity = getLightActivityInstance(activities, "manual");
    assertEquals(ActivityState.FINISHED, activity.getState());

    getManagementAPI().deleteProcess(start.getUUID());
    getManagementAPI().deleteProcess(end.getUUID());
  }

  public void testABoundaryAndAnEndSignalEventsThrowingBeforeListening() throws Exception {
    ProcessDefinition start = ProcessBuilder.createProcess("boundarySignal", "1.0")
    .addHuman(getLogin())
    .addHumanTask("start", getLogin())
      .addSignalBoundaryEvent("signal", "engineStart")
    .addHumanTask("manual", getLogin())
    .addHumanTask("exception", getLogin())
    .addTransition("start", "manual")
    .addExceptionTransition("start", "signal", "exception")
    .done();

    ProcessDefinition end = ProcessBuilder.createProcess("endSignal", "1.0")
    .addHuman(getLogin())
    .addHumanTask("manual", getLogin())
    .addSignalEventTask("end", "engineStart")
    .addTransition("manual", "end")
    .done();

    start = getManagementAPI().deploy(getBusinessArchive(start));
    end = getManagementAPI().deploy(getBusinessArchive(end));

    ProcessInstanceUUID endUUID = getRuntimeAPI().instantiateProcess(end.getUUID());
    executeTask(endUUID, "manual");
    getRuntimeAPI().instantiateProcess(start.getUUID());
    Thread.sleep(5000);
    Set<LightProcessInstance> instances = getQueryRuntimeAPI().getLightProcessInstances();
    assertEquals(2, instances.size());
    LightProcessInstance instance = getLightProcessInstance(instances, "boundarySignal", "1.0");
    assertEquals(InstanceState.STARTED, instance.getInstanceState());
    Set<LightActivityInstance> activities = getQueryRuntimeAPI().getLightActivityInstances(instance.getUUID());
    assertEquals(1, activities.size());
    LightActivityInstance activity = getLightActivityInstance(activities, "start");
    assertEquals(ActivityState.READY, activity.getState());

    instance = getLightProcessInstance(instances, "endSignal", "1.0");
    assertEquals(InstanceState.FINISHED, instance.getInstanceState());
    activities = getQueryRuntimeAPI().getLightActivityInstances(instance.getUUID());
    assertEquals(2, activities.size());
    activity = getLightActivityInstance(activities, "end");
    assertEquals(ActivityState.FINISHED, activity.getState());
    activity = getLightActivityInstance(activities, "manual");
    assertEquals(ActivityState.FINISHED, activity.getState());

    getManagementAPI().deleteProcess(start.getUUID());
    getManagementAPI().deleteProcess(end.getUUID());
  }

  public void testAStartAndAnEndSignalEventsSeveralTimes() throws Exception {
    ProcessDefinition start = ProcessBuilder.createProcess("startSignal", "1.0")
    .addHuman(getLogin())
    .addSignalEventTask("start", "engineStart", true)
    .addHumanTask("manual", getLogin())
    .addTransition("start", "manual")
    .done();

    ProcessDefinition end = ProcessBuilder.createProcess("endSignal", "1.0")
    .addSystemTask("start")
    .addSignalEventTask("end", "engineStart")
    .addTransition("start", "end")
    .done();

    start = getManagementAPI().deploy(getBusinessArchive(start));
    end = getManagementAPI().deploy(getBusinessArchive(end));

    getRuntimeAPI().instantiateProcess(end.getUUID());
    getRuntimeAPI().instantiateProcess(end.getUUID());

    Thread.sleep(5000);
    Set<LightProcessInstance> instances = getQueryRuntimeAPI().getLightProcessInstances(start.getUUID());
    assertEquals(2, instances.size());

    getManagementAPI().deleteProcess(start.getUUID());
    getManagementAPI().deleteProcess(end.getUUID());
  }

  /**
   * see bug 0010844
   * It throws BonitaRuntimeException: No user found in environment
   * @throws Exception
   */
  public void testIntermediateCatchSignalEventWithEnd() throws Exception {
    ProcessDefinition catcher = ProcessBuilder.createProcess("signalcatcher", "1.4")
    .addHuman(getLogin())
    .addSystemTask("startCatcher")
    .addSignalEventTask("catcher", "bipbipbip",true)
    .addSystemTask("endCatcher")
    .addTransition("startCatcher", "catcher")
    .addTransition("catcher", "endCatcher")
    .done();

    ProcessDefinition thrower = ProcessBuilder.createProcess("signalThrower", "1.1")
    .addHuman(getLogin())
    .addSystemTask("startThrower")
    .addSignalEventTask("thrower", "bipbipbip", false)
    .addTransition("startThrower", "thrower")
    .done();

    thrower = getManagementAPI().deploy(getBusinessArchive(thrower));
    catcher = getManagementAPI().deploy(getBusinessArchive(catcher));

    ProcessInstanceUUID catcherUUID = getRuntimeAPI().instantiateProcess(catcher.getUUID());
    getRuntimeAPI().instantiateProcess(thrower.getUUID());
    
    waitForInstanceEnd(2000, 50, catcherUUID);
    getManagementAPI().deleteProcess(thrower.getUUID());
    getManagementAPI().deleteProcess(catcher.getUUID());
  }
  
}
