package org.ow2.bonita.activity;

import java.util.Set;

import org.bonitasoft.connectors.bonita.joincheckers.FixedNumberJoinChecker;
import org.bonitasoft.connectors.scripting.GroovyConnector;
import org.ow2.bonita.APITestCase;
import org.ow2.bonita.activity.multipleinstances.instantiator.NoContextMulitpleActivitiesInstantiator;
import org.ow2.bonita.connector.ErrorConnectorWithStaticVar;
import org.ow2.bonita.facade.ErrorConnector;
import org.ow2.bonita.facade.def.element.HookDefinition.Event;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.runtime.ActivityState;
import org.ow2.bonita.facade.runtime.InstanceState;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.light.LightActivityInstance;
import org.ow2.bonita.light.LightProcessInstance;
import org.ow2.bonita.util.ProcessBuilder;

public class ActivityTest extends APITestCase {

  public void testHugeDescription() throws Exception {
    final StringBuilder hugeDescription = new StringBuilder();
    for (int i = 0; i < 255; i++) {
      hugeDescription.append(i);
    }
    final ProcessDefinition definition = ProcessBuilder.createProcess("desc", "2.1")
    .addSystemTask("sys")
    .addDescription(hugeDescription.toString())
    .done();

    getManagementAPI().deploy(getBusinessArchive(definition));

    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(definition.getUUID());
    final Set<LightActivityInstance> activities = getQueryRuntimeAPI().getLightActivityInstances(instanceUUID);
    assertEquals(1, activities.size());
    final LightActivityInstance activity = activities.iterator().next();
    assertEquals(hugeDescription.toString(), activity.getActivityDescription());

    getManagementAPI().deleteProcess(definition.getUUID());
  }

  public void testTaskFailedIfExceptionOnTaskOnFinishConnector() throws Exception {
    ProcessDefinition process =
      ProcessBuilder.createProcess("inERROR", "1.0")
      .addHuman(getLogin())
      .addHumanTask("step1", getLogin())
      .addConnector(Event.taskOnFinish, ErrorConnector.class.getName(), true) //connector without input parameters
      .addSystemTask("step2")
      .addTransition("step1", "step2")
      .done();

    process = getManagementAPI().deploy(getBusinessArchive(process, null, ErrorConnector.class));
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(process.getUUID());

    executeTaskWithoutCheckingState(instanceUUID, "step1");
    
    //verify task is in error
    checkState(instanceUUID, ActivityState.FAILED, "step1");
    checkState(instanceUUID, InstanceState.STARTED);
    
    getManagementAPI().deleteProcess(process.getUUID());
  }
  
  public void testTaskFailedIfExceptionOnTaskOnFinishGroovyConnector() throws Exception {
    ProcessDefinition process =
      ProcessBuilder.createProcess("inERROR", "1.0")
      .addHuman(getLogin())
      .addHumanTask("step1", getLogin())
        .addConnector(Event.taskOnFinish, GroovyConnector.class.getName(), true) //connector without input parameters
        .addInputParameter("script", "${throw new Exception();}")
      .addSystemTask("step2")
      .addTransition("step1", "step2")
      .done();
    
    process = getManagementAPI().deploy(getBusinessArchive(process, null, GroovyConnector.class));
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(process.getUUID());
    
    //execute task
    executeTaskWithoutCheckingState(instanceUUID, "step1");

    checkState(instanceUUID, ActivityState.FAILED, "step1");
    checkState(instanceUUID, InstanceState.STARTED);

    getManagementAPI().deleteProcess(process.getUUID());
  }

  public void testTaskFailedIfExceptionOnTaskOnReadyConnector() throws Exception {
    ProcessDefinition process =
      ProcessBuilder.createProcess("inERROR", "1.0")
      .addHuman(getLogin())
      .addHumanTask("step1", getLogin())
      .addConnector(Event.taskOnReady, ErrorConnector.class.getName(), true) //connector without input parameters
      .addSystemTask("step2")
      .addTransition("step1", "step2")
      .done();

    process = getManagementAPI().deploy(getBusinessArchive(process, null, ErrorConnector.class));
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(process.getUUID());

    checkState(instanceUUID, ActivityState.FAILED, "step1");
    checkState(instanceUUID, InstanceState.STARTED);

    getManagementAPI().deleteProcess(process.getUUID());
  }

  public void testTaskFailedIfExceptionOnTaskOnStartConnector() throws Exception {
    ProcessDefinition process =
      ProcessBuilder.createProcess("inERROR", "1.0")
      .addHuman(getLogin())
      .addHumanTask("step1", getLogin())
      .addConnector(Event.taskOnStart, ErrorConnector.class.getName(), true) //connector without input parameters
      .addSystemTask("step2")
      .addTransition("step1", "step2")
      .done();

    process = getManagementAPI().deploy(getBusinessArchive(process, null, ErrorConnector.class));
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(process.getUUID());

    startTaskWithoutVerifiyAfterState(instanceUUID, "step1");

    checkState(instanceUUID, ActivityState.FAILED, "step1");
    checkState(instanceUUID, InstanceState.STARTED);

    getManagementAPI().deleteProcess(process.getUUID());
  }
  
  public void testTaskFailedIfExceptionOnTaskOnStartConnectorAtomicExecute() throws Exception {
    ProcessDefinition process =
      ProcessBuilder.createProcess("inERROR", "1.0")
      .addHuman(getLogin())
      .addHumanTask("step1", getLogin())
      .addConnector(Event.taskOnStart, ErrorConnector.class.getName(), true) //connector without input parameters
      .addSystemTask("step2")
      .addTransition("step1", "step2")
      .done();

    process = getManagementAPI().deploy(getBusinessArchive(process, null, ErrorConnector.class));
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(process.getUUID());

    atomicExecutTask(instanceUUID, "step1");

    checkState(instanceUUID, ActivityState.FAILED, "step1");
    checkState(instanceUUID, InstanceState.STARTED);

    getManagementAPI().deleteProcess(process.getUUID());
  }
  
  private void atomicExecutTask(final ProcessInstanceUUID instanceUUID, final String taskName) throws Exception {
    final Set<LightActivityInstance> activityInstances = getQueryRuntimeAPI().getLightActivityInstances(instanceUUID, taskName);
    assertEquals("More then one activity was found", 1, activityInstances.size());
    final LightActivityInstance activityInstance = activityInstances.iterator().next();
    assertEquals(ActivityState.READY, activityInstance.getState());
    getRuntimeAPI().executeTask(activityInstance.getUUID(), false);
  }

  public void testAutomaticActivityInError() throws Exception {
    ProcessDefinition process =
      ProcessBuilder.createProcess("inERROR", "1.0")
      .addSystemTask("step1")
      .addConnector(Event.automaticOnExit, ErrorConnector.class.getName(), true) //connector without input parameters
      .addSystemTask("step2")
      .addTransition("step1", "step2")
      .done();

    process = getManagementAPI().deploy(getBusinessArchive(process, null, ErrorConnector.class));

    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(process.getUUID());
    Thread.sleep(500);

    checkState(instanceUUID, ActivityState.FAILED, "step1");
    checkState(instanceUUID, InstanceState.STARTED);

    getManagementAPI().deleteProcess(process.getUUID());
  }

  public void testSeveralAutomaticActivitiesLastFails() throws Exception {
    ProcessDefinition process =
      ProcessBuilder.createProcess("inERROR", "1.0")
      .addHuman(getLogin())
      .addHumanTask("step1", getLogin())
      .addSystemTask("step2")
      .addSystemTask("step3")
      .addSystemTask("step4")
      .addConnector(Event.automaticOnExit, ErrorConnector.class.getName(), true) //connector without input parameters
      .addTransition("step1", "step2")
      .addTransition("step2", "step3")
      .addTransition("step3", "step4")
      .done();

    process = getManagementAPI().deploy(getBusinessArchive(process, null, ErrorConnector.class));

    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(process.getUUID());

    executeTask(instanceUUID, "step1");

    checkState(instanceUUID, ActivityState.FINISHED, "step1");
    checkState(instanceUUID, ActivityState.FINISHED, "step2");
    checkState(instanceUUID, ActivityState.FINISHED, "step3");
    checkState(instanceUUID, ActivityState.FAILED, "step4");
    checkState(instanceUUID, InstanceState.STARTED);

    getManagementAPI().deleteProcess(process.getUUID());
  }

  public void testDoAllTransitionsWhenAllBranchesFails() throws Exception {
    ProcessDefinition process =
      ProcessBuilder.createProcess("inERROR", "1.0")
      .addSystemTask("step1")
      .addSystemTask("step2")
      .addConnector(Event.automaticOnExit, ErrorConnector.class.getName(), true) //connector without input parameters
      .addSystemTask("step3")
      .addConnector(Event.automaticOnExit, ErrorConnector.class.getName(), true) //connector without input parameters
      .addTransition("step1", "step2")
      .addTransition("step1", "step3")
      .done();

    process = getManagementAPI().deploy(getBusinessArchive(process, null, ErrorConnector.class));

    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(process.getUUID());

    checkState(instanceUUID, ActivityState.FINISHED, "step1");
    checkState(instanceUUID, ActivityState.FAILED, "step2");
    checkState(instanceUUID, ActivityState.FAILED, "step3");
    checkState(instanceUUID, InstanceState.STARTED);

    //verify instance is no finished
    final LightProcessInstance instance = getQueryRuntimeAPI().getLightProcessInstance(instanceUUID);
    assertEquals(InstanceState.STARTED, instance.getInstanceState());

    getManagementAPI().deleteProcess(process.getUUID());
  }

  public void testDoAllTransitionsWhenOnlyOneBrancheFail() throws Exception {
    ProcessDefinition process =
      ProcessBuilder.createProcess("inERROR", "1.0")
      .addHuman(getLogin())
      .addSystemTask("step1")
      .addSystemTask("step2")
      .addConnector(Event.automaticOnEnter, ErrorConnector.class.getName(), true) 
      .addSystemTask("step3")
      .addTransition("step1", "step2")
      .addTransition("step1", "step3")
      .done();

    process = getManagementAPI().deploy(getBusinessArchive(process, null, ErrorConnector.class));

    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(process.getUUID());

    checkState(instanceUUID, ActivityState.FINISHED, "step1");
    checkState(instanceUUID, ActivityState.FAILED, "step2");
    checkState(instanceUUID, ActivityState.FINISHED, "step3");
    checkState(instanceUUID, InstanceState.STARTED);

    getManagementAPI().deleteProcess(process.getUUID());
  }

  public void testActivityFailWhileExceptionOnLocalVariable() throws Exception {
    ProcessDefinition process =
      ProcessBuilder.createProcess("inERROR", "1.0")
      .addHuman(getLogin())
      .addHumanTask("step1", getLogin())
      .addIntegerData("var", "${throw new RuntimeException()}")
      .addSystemTask("step2")
      .addTransition("step1", "step2")
      .done();

    process = getManagementAPI().deploy(getBusinessArchive(process));
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(process.getUUID());

    //verify task is in error
    checkState(instanceUUID, ActivityState.FAILED, "step1");
    checkState(instanceUUID, InstanceState.STARTED);

    getManagementAPI().deleteProcess(process.getUUID());
  }

  public void testNoActivityFailsWhileExceptionOnGlobalVariable() throws Exception {
    ProcessDefinition process =
      ProcessBuilder.createProcess("inERROR", "1.0")
      .addHuman(getLogin())
      .addIntegerData("var", "${throw new RuntimeException()}")
      .addHumanTask("step1", getLogin())
      .addSystemTask("step2")
      .addTransition("step1", "step2")
      .done();

    process = getManagementAPI().deploy(getBusinessArchive(process));
    try {
      getRuntimeAPI().instantiateProcess(process.getUUID());
      fail("exception expected");
    } catch (final Exception e) {
      //ok
    }

    final Set<LightProcessInstance> instances = getQueryRuntimeAPI().getLightProcessInstances(process.getUUID());
    assertEquals(0, instances.size());

    getManagementAPI().deleteProcess(process.getUUID());
  }

  public void testCanExecuteAnotherBranch() throws Exception {
    ProcessDefinition process =
      ProcessBuilder.createProcess("inERROR", "1.0")
      .addHuman(getLogin())
      .addHumanTask("step1", getLogin())
      .addConnector(Event.taskOnFinish, ErrorConnector.class.getName(), true) //connector without input parameters
      .addHumanTask("step2", getLogin())
      .done();

    process = getManagementAPI().deploy(getBusinessArchive(process, null, ErrorConnector.class));
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(process.getUUID());

    //execute task
    executeTaskWithoutCheckingState(instanceUUID, "step1");

    executeTask(instanceUUID, "step2");

    checkState(instanceUUID, ActivityState.FAILED, "step1");
    checkState(instanceUUID, ActivityState.FINISHED, "step2");
    checkState(instanceUUID, InstanceState.STARTED);

    getManagementAPI().deleteProcess(process.getUUID());
  }

  public void testTaskFailsIfEventErrorNotCaught() throws Exception {
    ProcessDefinition process =
      ProcessBuilder.createProcess("inERROR", "1.0")
      .addHuman(getLogin())
      .addHumanTask("step1", getLogin())
      .addConnector(Event.taskOnFinish, ErrorConnector.class.getName(), false) //connector without input parameters
      .throwCatchError("fail")
      .addSystemTask("step2")
      .addTransition("step1", "step2")
      .done();

    process = getManagementAPI().deploy(getBusinessArchive(process, null, ErrorConnector.class));
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(process.getUUID());

    executeTaskWithoutCheckingState(instanceUUID, "step1");

    //verify task is in error
    checkState(instanceUUID, ActivityState.FAILED, "step1");
    checkState(instanceUUID, InstanceState.STARTED);

    getManagementAPI().deleteProcess(process.getUUID());
  }

  public void testFailsOnMultiInstantianteTasks() throws Exception {
    final int nbMulti = 10;
    ProcessDefinition definition =
      ProcessBuilder.createProcess("multiple", "1.0")
      .addHuman(getLogin())
      .addHumanTask("multi", getLogin())
      .addConnector(Event.taskOnReady, ErrorConnector.class.getName(), true)
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
    checkState(instanceUUID, InstanceState.STARTED);

    getManagementAPI().deleteProcess(definition.getUUID());
  }

  public void testFailsOnMultiInstantiateAutomaticActivity() throws Exception {
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
    checkState(instanceUUID, InstanceState.STARTED);

    getManagementAPI().deleteProcess(definition.getUUID());
  }

  public void testFailsOnMultiInstantiateAutomaticActivityUsingBadVariableValue() throws Exception {
    final int nbMulti = 10;
    ProcessDefinition definition =
      ProcessBuilder.createProcess("multiple", "1.0")
      .addHuman(getLogin())
      .addSystemTask("multi")
      .addIntegerData("var", "${throw new Exception();}")
      .addMultipleActivitiesInstantiator(NoContextMulitpleActivitiesInstantiator.class.getName())
      .addInputParameter("number", nbMulti)
      .addMultipleActivitiesJoinChecker(FixedNumberJoinChecker.class.getName())
      .addInputParameter("activityNumber", 3)
      .addHumanTask("step2", getLogin())
      .addTransition("multi", "step2")
      .done();

    definition = getManagementAPI().deploy(getBusinessArchive(definition, null,
        NoContextMulitpleActivitiesInstantiator.class, FixedNumberJoinChecker.class));

    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(definition.getUUID());
    checkState(instanceUUID, ActivityState.FAILED, "multi", nbMulti);
    checkState(instanceUUID, InstanceState.STARTED);

    getManagementAPI().deleteProcess(definition.getUUID());
  }

  public void testExceptionOnTransitionHumanSteps() throws Exception {
    ProcessDefinition process =
      ProcessBuilder.createProcess("inERROR", "1.0")
      .addHuman(getLogin())
      .addHumanTask("step1", getLogin())
      .addHumanTask("step2", getLogin())
      .addTransition("step1", "step2")
      .addCondition("wrong")
      .done();

    process = getManagementAPI().deploy(getBusinessArchive(process));
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(process.getUUID());

    executeTaskWithoutCheckingState(instanceUUID, "step1");

    checkState(instanceUUID, ActivityState.FAILED, "step1");
    checkActivityInstanceNotExist(instanceUUID, "step2");
    checkState(instanceUUID, InstanceState.STARTED);

    getManagementAPI().deleteProcess(process.getUUID());
  }

  public void testExceptionOnTransitionsAutomaticSteps() throws Exception {
    ProcessDefinition process =
      ProcessBuilder.createProcess("inERROR", "1.0")
      .addSystemTask("step1")
      .addSystemTask("step2")
      .addTransition("step1", "step2")
      .addCondition("wrongVar")
      .done();

    process = getManagementAPI().deploy(getBusinessArchive(process));
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(process.getUUID());

    checkState(instanceUUID, ActivityState.FAILED, "step1");
    checkActivityInstanceNotExist(instanceUUID, "step2");
    checkState(instanceUUID, InstanceState.STARTED);

    getManagementAPI().deleteProcess(process.getUUID());
  }
  
  public void testAsyncActivityFailsIfExceptionOnTaskOnFinishConnector() throws Exception {

	    ProcessDefinition process =
	      ProcessBuilder.createProcess("inERROR", "1.0")
	      .addSystemTask("step1")
	      .asynchronous()
	      .addConnector(Event.automaticOnExit, ErrorConnectorWithStaticVar.class.getName(), true)
	      .done();

	    process = getManagementAPI().deploy(getBusinessArchive(process, null, ErrorConnectorWithStaticVar.class));

	    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(process.getUUID());

	    final long maxTime = System.currentTimeMillis() + 10000;
	    while (!ErrorConnectorWithStaticVar.isExecuted() && System.currentTimeMillis() < maxTime) {
	    Thread.sleep(300);
	    }

	    System.err.println("\n\n\n\n\n*****IsExecuted: " + ErrorConnectorWithStaticVar.isExecuted());

	    assertEquals(ActivityState.FAILED, getQueryRuntimeAPI().getActivityInstances(instanceUUID).iterator().next().getState());
	   

	    getManagementAPI().deleteProcess(process.getUUID());

	  }
  

}
