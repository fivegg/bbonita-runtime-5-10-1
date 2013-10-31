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
package org.ow2.bonita.integration.connector;

import java.util.Set;
import java.util.logging.Level;

import org.bonitasoft.connectors.scripting.GroovyConnector;
import org.ow2.bonita.APITestCase;
import org.ow2.bonita.event.GetJobsCommand;
import org.ow2.bonita.facade.ErrorConnector;
import org.ow2.bonita.facade.def.element.HookDefinition.Event;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.runtime.ActivityState;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.light.LightActivityInstance;
import org.ow2.bonita.util.Misc;
import org.ow2.bonita.util.ProcessBuilder;

/**
 * @author Elias Ricken de Medeiros
 * 
 */
public class SlowConnectorsTest extends APITestCase {

  public void testCanExecuteParallelTaskWhenExecutingConnector() throws Exception {

    final StringBuilder stb = new StringBuilder();
    stb.append("System.out.println(\"\\n\\n******** going to sleep\"); ");
    stb.append("import org.ow2.bonita.facade.RuntimeAPI; ");
    stb.append("RuntimeAPI runtimeAPI = apiAccessor.getRuntimeAPI(); ");
    stb.append("runtimeAPI.setProcessInstanceVariable(processInstance.getUUID(), \"strVar\", \"update\"); ");
    stb.append("Thread.sleep(5000); ");
    stb.append("runtimeAPI.setProcessInstanceVariable(processInstance.getUUID(), \"strVar\", \"update2\"); ");
    stb.append("System.out.println(\"\\n\\n******** wake up\"); ");
    final String script = stb.toString();

    ProcessDefinition processDefinition = ProcessBuilder.createProcess("p", "1.0").addHuman(getLogin())
        .addStringData("strVar", "initial").addSystemTask("start").addSystemTask("executeConnector").asynchronous()
        .addConnector(Event.automaticOnEnter, GroovyConnector.class.getName(), true)
        .addInputParameter("script", script).addHumanTask("step1", getLogin())
        .addTransition("start", "executeConnector").addTransition("start", "step1").done();

    processDefinition = getManagementAPI().deploy(getBusinessArchive(processDefinition, null, GroovyConnector.class));

    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processDefinition.getUUID());
    waitForActivity(5000, 10, instanceUUID, "executeConnector", ActivityState.EXECUTING);
    LightActivityInstance activityInstance = getActivityInstance(instanceUUID, "step1");
    Thread.sleep(2000); // wait connector starts execution
    getRuntimeAPI().executeTask(activityInstance.getUUID(), true);

    activityInstance = getActivityInstance(instanceUUID, "step1");
    assertEquals(ActivityState.FINISHED, activityInstance.getState());

    activityInstance = getActivityInstance(instanceUUID, "executeConnector");
    assertEquals(ActivityState.EXECUTING, activityInstance.getState());

    if (LOG.isLoggable(Level.INFO)) {
      LOG.info("human task executed waiting for connector finish execution");
    }

    waitForInstanceEnd(50000, 100, instanceUUID);
    final String strVar = (String) getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "strVar");
    assertEquals("update2", strVar);

    getManagementAPI().deleteProcess(processDefinition.getUUID());

  }

  public void testCanExecuteSeveralParallelTasks() throws Exception {

    final StringBuilder stb = new StringBuilder();
    stb.append("System.out.println(\"\\n\\n******** going to sleep\"); ");
    stb.append("import org.ow2.bonita.facade.RuntimeAPI; ");
    stb.append("RuntimeAPI runtimeAPI = apiAccessor.getRuntimeAPI(); ");
    stb.append("runtimeAPI.setProcessInstanceVariable(processInstance.getUUID(), \"strVar\", \"update\"); ");
    stb.append("Thread.sleep(300); ");
    stb.append("runtimeAPI.setProcessInstanceVariable(processInstance.getUUID(), \"strVar\", \"update2\"); ");
    stb.append("System.out.println(\"\\n\\n******** wake up\"); ");
    final String script = stb.toString();

    final int nbOfTaskWithConnector = 10;

    final ProcessBuilder processBuilder = ProcessBuilder.createProcess("p", "1.0").addHuman(getLogin())
        .addStringData("strVar", "initial").addSystemTask("start").addHumanTask("step1", getLogin())
        .addTransition("start", "step1");
    for (int i = 0; i < nbOfTaskWithConnector; i++) {
      final String taskName = "executeConnector" + i;
      processBuilder.addSystemTask(taskName).asynchronous()
          .addConnector(Event.automaticOnEnter, GroovyConnector.class.getName(), true)
          .addInputParameter("script", script).addTransition("start", taskName);
    }
    ProcessDefinition processDefinition = processBuilder.done();

    processDefinition = getManagementAPI().deploy(getBusinessArchive(processDefinition, null, GroovyConnector.class));

    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processDefinition.getUUID());
    for (int i = 0; i < nbOfTaskWithConnector; i++) {
      final String taskName = "executeConnector" + i;
      waitForActivity(4000, 10, instanceUUID, taskName, ActivityState.EXECUTING);
    }
    LightActivityInstance activityInstance = getActivityInstance(instanceUUID, "step1");
    Thread.sleep(1000); // wait connector starts execution
    getRuntimeAPI().executeTask(activityInstance.getUUID(), true);

    activityInstance = getActivityInstance(instanceUUID, "step1");
    assertEquals(ActivityState.FINISHED, activityInstance.getState());

    if (LOG.isLoggable(Level.INFO)) {
      LOG.info("human task executed waiting for connector finish execution");
    }

    waitForInstanceEnd(50000, 100, instanceUUID);
    final String strVar = (String) getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "strVar");
    assertEquals("update2", strVar);

    getManagementAPI().deleteProcess(processDefinition.getUUID());

  }

  private LightActivityInstance waitForActivity(final long maxWait, final long sleepTime,
      final ProcessInstanceUUID instanceUUID, final String activityName, final ActivityState state) throws Exception {
    Set<LightActivityInstance> activityInstances = null;
    ActivityState currentState = null;
    LightActivityInstance activityInstance = null;
    final long begin = System.currentTimeMillis();
    do {
      activityInstances = getQueryRuntimeAPI().getLightActivityInstances(instanceUUID, activityName);
      if (!activityInstances.isEmpty()) {
        activityInstance = activityInstances.iterator().next();
        currentState = activityInstance.getState();
      }
      if (activityInstances.isEmpty() || !state.equals(currentState)) {
        Thread.sleep(sleepTime);
      }
    } while ((activityInstances.isEmpty() || !state.equals(currentState))
        && begin + maxWait > System.currentTimeMillis());

    assertEquals(state, activityInstance.getState());

    return activityInstance;
  }

  public void testFailingConnectorPutTaskInFailedState() throws Exception {

    ProcessDefinition processDefinition = ProcessBuilder.createProcess("p", "1.0").addHuman(getLogin())
        .addStringData("strVar", "initial").addSystemTask("start").addSystemTask("executeConnector").asynchronous()
        .addConnector(Event.automaticOnEnter, ErrorConnector.class.getName(), true).addHumanTask("step1", getLogin())
        .addTransition("start", "executeConnector").addTransition("start", "step1").done();

    processDefinition = getManagementAPI().deploy(getBusinessArchive(processDefinition, null, ErrorConnector.class));

    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processDefinition.getUUID());
    LightActivityInstance activityInstance = getActivityInstance(instanceUUID, "step1");
    getRuntimeAPI().executeTask(activityInstance.getUUID(), true);

    activityInstance = getActivityInstance(instanceUUID, "step1");
    assertEquals(ActivityState.FINISHED, activityInstance.getState());

    waitForActivity(5000, 10, instanceUUID, "executeConnector", ActivityState.FAILED);

    getManagementAPI().deleteProcess(processDefinition.getUUID());

  }

  public void testLongConnectorAndTerminateEvent() throws Exception {

    final StringBuilder stb = new StringBuilder();
    stb.append("System.out.println(\"\\n\\n******** going to sleep\"); ");
    stb.append("Thread.sleep(5000); ");
    stb.append("System.out.println(\"\\n\\n******** wake up\"); ");
    final String script = stb.toString();

    ProcessDefinition processDefinition = ProcessBuilder.createProcess("p", "1.0").addHuman(getLogin())
        .addStringData("strVar", "initial").addSystemTask("start").addSystemTask("executeConnector").asynchronous()
        .addConnector(Event.automaticOnEnter, GroovyConnector.class.getName(), true)
        .addInputParameter("script", script).addHumanTask("step1", getLogin()).addTerminateEndEvent("terminate1")
        .addTerminateEndEvent("terminate2").addTransition("start", "executeConnector").addTransition("start", "step1")
        .addTransition("step1", "terminate1").addTransition("executeConnector", "terminate2").done();

    getManagementAPI().deployJar("getJobs.jar", Misc.generateJar(GetJobsCommand.class));

    processDefinition = getManagementAPI().deploy(getBusinessArchive(processDefinition, null, GroovyConnector.class));

    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processDefinition.getUUID());
    waitForActivity(5000, 10, instanceUUID, "executeConnector", ActivityState.EXECUTING);
    LightActivityInstance activityInstance = getActivityInstance(instanceUUID, "step1");
    Thread.sleep(1000); // wait connector starts execution
    getRuntimeAPI().executeTask(activityInstance.getUUID(), true);

    activityInstance = getActivityInstance(instanceUUID, "step1");
    assertEquals(ActivityState.FINISHED, activityInstance.getState());

    if (LOG.isLoggable(Level.INFO)) {
      LOG.info("human task executed waiting for connector finish execution");
    }

    waitForInstanceEnd(50000, 100, instanceUUID);
    final String strVar = (String) getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "strVar");
    assertEquals("initial", strVar);
    activityInstance = getActivityInstance(instanceUUID, "executeConnector");
    assertEquals(ActivityState.ABORTED, activityInstance.getState());
    final Set<String> jobs = getCommandAPI().execute(new GetJobsCommand());
    assertTrue(jobs.isEmpty());

    getManagementAPI().removeJar("getJobs.jar");
    getManagementAPI().deleteProcess(processDefinition.getUUID());

  }

}
