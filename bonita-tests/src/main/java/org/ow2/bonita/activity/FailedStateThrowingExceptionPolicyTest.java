/**
 * Copyright (C) 2012  BonitaSoft S.A.
 * BonitaSoft, 31 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA  02110-1301, USA.
 **/
package org.ow2.bonita.activity;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.security.auth.login.LoginException;

import org.bonitasoft.connectors.bonita.joincheckers.FixedNumberJoinChecker;
import org.bonitasoft.connectors.scripting.GroovyConnector;
import org.ow2.bonita.APITestCase;
import org.ow2.bonita.activity.multipleinstances.instantiator.NoContextMulitpleActivitiesInstantiator;
import org.ow2.bonita.connector.ErrorConnectorWithStaticVar;
import org.ow2.bonita.facade.ErrorConnector;
import org.ow2.bonita.facade.def.element.HookDefinition.Event;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.exception.ExpressionEvaluationException;
import org.ow2.bonita.facade.exception.HookInvocationException;
import org.ow2.bonita.facade.runtime.ActivityState;
import org.ow2.bonita.facade.runtime.InstanceState;
import org.ow2.bonita.facade.runtime.command.WebExecuteTask;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.light.LightActivityInstance;
import org.ow2.bonita.light.LightProcessInstance;
import org.ow2.bonita.util.BonitaRuntimeException;
import org.ow2.bonita.util.BusinessArchiveFactory;
import org.ow2.bonita.util.ProcessBuilder;

/**
 * @author Elias Ricken de Medeiros
 *
 */
public class FailedStateThrowingExceptionPolicyTest extends APITestCase {

  public static final String THROW_EXCEPTION_ON_FAILURE_TENANT = "teoftenant";
  
  @Override
  protected void login() throws LoginException {
    super.login();
    login(THROW_EXCEPTION_ON_FAILURE_TENANT);
  }
  
  
  public void testTaskFailedIfExceptionOnTaskOnFinishConnector() throws Exception {
    activityGoesToFailedAndExceptionIsThrown(Event.taskOnFinish);
  }

  public void testTaskFailedIfExceptionOnTaskOnStartConnector() throws Exception {
    activityGoesToFailedAndExceptionIsThrown(Event.taskOnStart);
  }

  private void activityGoesToFailedAndExceptionIsThrown(final Event eventPosition) throws Exception {
    final StringBuilder stb = new StringBuilder();
    stb.append("import org.ow2.bonita.facade.RuntimeAPI; ");
    stb.append("RuntimeAPI runtimeAPI = apiAccessor.getRuntimeAPI(); ");
    stb.append("runtimeAPI.setProcessInstanceVariable(processInstance.getUUID(), \"strVar\", \"update\"); ");
    stb.append("runtimeAPI.setProcessInstanceVariable(processInstance.getUUID(), \"unkowVar\", \"update2\"); ");
    final String script = stb.toString();
    
    ProcessDefinition process =
      ProcessBuilder.createProcess("inERROR", "1.0")
      .addStringData("strVar", "initial")
      .addHuman(getLogin())
      .addHumanTask("step1", getLogin())
        .addConnector(eventPosition, GroovyConnector.class.getName(), true) //connector without input parameters
        .addInputParameter("script", script)
      .addSystemTask("step2")
      .addTransition("step1", "step2")
      .done();

    process = getManagementAPI().deploy(getBusinessArchive(process, null, GroovyConnector.class));
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(process.getUUID());

    final Set<LightActivityInstance> activityInstances = getQueryRuntimeAPI().getLightActivityInstances(instanceUUID, "step1");
    assertEquals(1, activityInstances.size());
    final LightActivityInstance activityInstance = activityInstances.iterator().next();
    
    try {
      getRuntimeAPI().startTask(activityInstance.getUUID(), true);
      getRuntimeAPI().finishTask(activityInstance.getUUID(), true);
      fail("exception expected");
    } catch (final HookInvocationException e) {
      //ok
    }
    
    //verify task is in error
    checkState(instanceUUID, ActivityState.FAILED, "step1");
    checkState(instanceUUID, InstanceState.STARTED);
    final String strVar = (String) getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "strVar");
    assertEquals("update", strVar);
    
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
    try {
      getRuntimeAPI().instantiateProcess(process.getUUID());
      fail("exception expected");
    } catch (final HookInvocationException e){
      //ok
    }
    
    final LightProcessInstance processInstance = getProcessInstance(process);
    checkState(processInstance.getUUID(), ActivityState.FAILED, "step1");
    checkState(processInstance.getUUID(), InstanceState.STARTED);

    getManagementAPI().deleteProcess(process.getUUID());
  }

  private LightProcessInstance getProcessInstance(final ProcessDefinition process) {
    final Set<LightProcessInstance> processInstances = getQueryRuntimeAPI().getLightProcessInstances(process.getUUID());
    assertEquals(1, processInstances.size());
    final LightProcessInstance processInstance = processInstances.iterator().next();
    return processInstance;
  }

  public void testTaskFailedIfExceptionOnTaskOnStartConnectorAtomicExecute() throws Exception {
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
    try {
      getRuntimeAPI().executeTask(activityInstance.getUUID(), false);
      fail("exception expected");
    } catch (final HookInvocationException e) {
      //ok
    }
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

    try {
      getRuntimeAPI().instantiateProcess(process.getUUID());
      fail("exception expected");
    } catch (final Exception e) {
      //ok
    }
    final LightProcessInstance processInstance = getProcessInstance(process);
    checkState(processInstance.getUUID(), ActivityState.FAILED, "step1");
    checkState(processInstance.getUUID(), InstanceState.STARTED);

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

    try {
      executeTask(instanceUUID, "step1");
      fail("exception expected");
    } catch (final HookInvocationException e) {
      //ok
    }

    checkState(instanceUUID, ActivityState.FINISHED, "step1");
    checkState(instanceUUID, ActivityState.FINISHED, "step2");
    checkState(instanceUUID, ActivityState.FINISHED, "step3");
    checkState(instanceUUID, ActivityState.FAILED, "step4");
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
    try {
      getRuntimeAPI().instantiateProcess(process.getUUID());
      fail("exception expected");
    } catch (final BonitaRuntimeException e) {
      assertTrue(e.getMessage().startsWith("Error while initializing variable: "));
    }
    
    final LightProcessInstance processInstance = getProcessInstance(process);
    //verify task is in error
    checkState(processInstance.getUUID(), ActivityState.FAILED, "step1");
    checkState(processInstance.getUUID(), InstanceState.STARTED);

    getManagementAPI().deleteProcess(process.getUUID());
  }

//  public void testTaskFailsIfEventErrorNotCaught() throws Exception {
//    ProcessDefinition process =
//      ProcessBuilder.createProcess("inERROR", "1.0")
//      .addHuman(getLogin())
//      .addHumanTask("step1", getLogin())
//      .addConnector(Event.taskOnFinish, ErrorConnector.class.getName(), false) //connector without input parameters
//      .throwCatchError("fail")
//      .addSystemTask("step2")
//      .addTransition("step1", "step2")
//      .done();
//
//    process = getManagementAPI().deploy(getBusinessArchive(process, null, ErrorConnector.class));
//    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(process.getUUID());
//
//    executeTaskWithoutCheckingState(instanceUUID, "step1");
//
//    //verify task is in error
//    checkState(instanceUUID, ActivityState.FAILED, "step1");
//    checkState(instanceUUID, InstanceState.STARTED);
//
//    getManagementAPI().deleteProcess(process.getUUID());
//  }

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

    try {
      getRuntimeAPI().instantiateProcess(definition.getUUID());
      fail("exception expected");
    } catch (final HookInvocationException e) {
      // ok
    }
    final LightProcessInstance processInstance = getProcessInstance(definition);
    
    checkState(processInstance.getUUID(), ActivityState.FAILED, "multi", nbMulti);
    checkState(processInstance.getUUID(), InstanceState.STARTED);

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

    try { 
      getRuntimeAPI().instantiateProcess(definition.getUUID());
      fail("exception expected");
    } catch (final HookInvocationException e) {
      //ok
    }
    final LightProcessInstance processInstance = getProcessInstance(definition);
    checkState(processInstance.getUUID(), ActivityState.FAILED, "multi", nbMulti);
    checkState(processInstance.getUUID(), InstanceState.STARTED);

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

    try {
      getRuntimeAPI().instantiateProcess(definition.getUUID());
      fail("exception expected");
    } catch (final Exception e) {
      //ok
    }
   
    final LightProcessInstance processInstance = getProcessInstance(definition);
    checkState(processInstance.getUUID(), ActivityState.FAILED, "multi", nbMulti);
    checkState(processInstance.getUUID(), InstanceState.STARTED);

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

    try {
      executeTaskWithoutCheckingState(instanceUUID, "step1");
      fail("exception expected");
    } catch (final ExpressionEvaluationException e) {
      //ok
    }

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
    
    try {
      getRuntimeAPI().instantiateProcess(process.getUUID());
      fail("exception expected");
    } catch (final ExpressionEvaluationException e) {
      //ok
    }
    
    final LightProcessInstance processInstance = getProcessInstance(process);

    checkState(processInstance.getUUID(), ActivityState.FAILED, "step1");
    checkActivityInstanceNotExist(processInstance.getUUID(), "step2");
    checkState(processInstance.getUUID(), InstanceState.STARTED);

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
  
  public void testActivityFailsWithWebExecuteTaskCommand() throws Exception {
    final StringBuilder stb = new StringBuilder();
    stb.append("throw new RuntimeException(\"forced exception\"); ");
    final String script = stb.toString();
    
    final String taskName = "step1";
    ProcessDefinition processDefinition = ProcessBuilder.createProcess("ExceptionOnSubmitTask", "1.0")
     .addHuman(getLogin()) 
     .addSystemTask("start")
     .addHumanTask(taskName, getLogin())
       .addConnector(Event.taskOnFinish, GroovyConnector.class.getName(), true) //connector without input parameters
       .addInputParameter("script", script)
     .addSystemTask("end")
     .addTransition("start", taskName)
     .addTransition(taskName, "end")
      .done();
    
    processDefinition = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(processDefinition, GroovyConnector.class));
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processDefinition.getUUID());
    LightActivityInstance activityInstance = getActivityInstance(instanceUUID, taskName);
    
    
    final Map<String, Object> processVariables = Collections.emptyMap();
    final List<String> scriptsToExecute = Collections.emptyList();
    try {
      final WebExecuteTask command = new WebExecuteTask(activityInstance.getUUID(), processVariables, null, null, null, scriptsToExecute, null);
      getCommandAPI().execute(command);
      fail("Exception expected");
    } catch (final HookInvocationException e) {
      //OK
    }
    
    activityInstance = getActivityInstance(instanceUUID, taskName);
    assertEquals(ActivityState.FAILED, activityInstance.getState());
    
    final LightProcessInstance processInstance = getQueryRuntimeAPI().getLightProcessInstance(instanceUUID);
    assertEquals(InstanceState.STARTED, processInstance.getInstanceState());
    
    getManagementAPI().deleteProcess(processDefinition.getUUID());
  }
  
}
