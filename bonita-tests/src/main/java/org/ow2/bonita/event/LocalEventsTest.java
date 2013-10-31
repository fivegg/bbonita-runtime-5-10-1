/**
 * Copyright (C) 2009  BonitaSoft S.A.
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
package org.ow2.bonita.event;

import java.net.URL;
import java.util.Set;

import org.ow2.bonita.APITestCase;
import org.ow2.bonita.env.GlobalEnvironmentFactory;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.runtime.InstanceState;
import org.ow2.bonita.facade.uuid.ActivityDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.light.LightProcessInstance;
import org.ow2.bonita.runtime.event.EventExecutor;
import org.ow2.bonita.util.BonitaConstants;
import org.ow2.bonita.util.BonitaRuntimeException;
import org.ow2.bonita.util.BusinessArchiveFactory;
import org.ow2.bonita.util.Misc;
import org.ow2.bonita.util.ProcessBuilder;

public class LocalEventsTest extends APITestCase {

  public void testTimerOnRestart() throws Exception {
    final long timeToWait = 2000;

    ProcessDefinition process = ProcessBuilder.createProcess("p", "d").addSystemTask("init")
        .addTimerTask("timer", "${new Date(System.currentTimeMillis() + " + timeToWait + ")}")
        .addTransition("init", "timer").done();

    process = getManagementAPI().deploy(getBusinessArchive(process));
    final ProcessDefinitionUUID processUUID = process.getUUID();
    final EventExecutor eventExecutor = GlobalEnvironmentFactory.getEnvironmentFactory(BonitaConstants.DEFAULT_DOMAIN)
        .get(EventExecutor.class);
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);
    eventExecutor.stop();
    assertFalse(eventExecutor.isActive());
    assertEquals(InstanceState.STARTED, getQueryRuntimeAPI().getProcessInstance(instanceUUID).getInstanceState());

    Thread.sleep(timeToWait * 2);
    assertEquals(InstanceState.STARTED, getQueryRuntimeAPI().getProcessInstance(instanceUUID).getInstanceState());

    eventExecutor.start();
    waitForInstanceEnd(3000, 100, instanceUUID);

    getManagementAPI().deleteProcess(processUUID);
  }

  public void testCreateInstanceEventWithFailingConnector() throws Exception {
    getManagementAPI().deployJar("commands.jar",
        Misc.generateJar(GetOutgoingEventInstancesUUIDsCommand.class, GetJobRetriesCommand.class));

    // creator send an event to created
    ProcessDefinition creator = ProcessBuilder.createProcess("creator", "1.0").addHuman(getLogin())
        .addSendEventTask("send").addOutgoingEvent("createProcess", null, null, null).done();

    ProcessDefinition created = ProcessBuilder.createProcess("created", "1.0")
        .addReceiveEventTask("receive", "createProcess")
        .addReceiveEventConnector(FailingConnector.class.getName(), true).done();

    creator = getManagementAPI().deploy(getBusinessArchive(creator));
    created = getManagementAPI().deploy(getBusinessArchive(created));

    final ProcessDefinitionUUID creatorUUID = creator.getUUID();
    final ProcessDefinitionUUID createdUUID = created.getUUID();

    final ProcessInstanceUUID creatorInstanceUUID = getRuntimeAPI().instantiateProcess(creatorUUID);

    final LightProcessInstance creatorInstance = getQueryRuntimeAPI().getLightProcessInstance(creatorInstanceUUID);
    assertEquals("Creator process instance is finished", InstanceState.FINISHED, creatorInstance.getInstanceState());

    Thread.sleep(5000);

    assertEquals(1, getQueryRuntimeAPI().getNumberOfProcessInstances());
    assertEquals(1, getQueryRuntimeAPI().getLightProcessInstances(creatorUUID).size());
    assertEquals(0, getQueryRuntimeAPI().getLightProcessInstances(createdUUID).size());

    // check there is one incoming and one outgoing events with retries=0

    final Set<ProcessInstanceUUID> outgoingUUIDs = getCommandAPI().execute(new GetOutgoingEventInstancesUUIDsCommand());
    assertEquals(0, outgoingUUIDs.size());

    final Set<JobRetry> retries = getCommandAPI().execute(new GetJobRetriesCommand());
    assertEquals(1, retries.size());
    final JobRetry jobRetry = retries.iterator().next();
    assertEquals(0, jobRetry.getRetry());

    assertEquals(5, FailingConnector.numberOfExecutions);

    // set fail to false and enable event in failure
    FailingConnector.fail = false;
    getRuntimeAPI().enablePermanentEventInFailure(new ActivityDefinitionUUID(createdUUID, "receive"));

    Thread.sleep(2000);
    assertEquals(2, getQueryRuntimeAPI().getNumberOfProcessInstances());
    assertEquals(1, getQueryRuntimeAPI().getLightProcessInstances(createdUUID).size());
    assertEquals(1, getQueryRuntimeAPI().getLightProcessInstances(creatorUUID).size());

    final Set<ProcessInstanceUUID> outgoingUUIDs2 = getCommandAPI()
        .execute(new GetOutgoingEventInstancesUUIDsCommand());
    assertEquals(0, outgoingUUIDs2.size());

    final Set<JobRetry> retries2 = getCommandAPI().execute(new GetJobRetriesCommand());
    assertEquals(0, retries2.size());
    assertEquals(6, FailingConnector.numberOfExecutions);

    getManagementAPI().removeJar("commands.jar");

    getManagementAPI().deleteProcess(creatorUUID);
    getManagementAPI().deleteProcess(createdUUID);
  }

  public void testUnableToDeployProcessWithWrongProductVersion() throws Exception {
    final URL processDefinitionUrl = this.getClass().getResource("process5_5_3--1.0.bar");
    try {
      BusinessArchiveFactory.getBusinessArchive(processDefinitionUrl);
      fail("A process definition defined in previous product version cannot be deployed");
    } catch (final BonitaRuntimeException e) {
      assertTrue(e.getMessage().contains(
          "The given business archive was created with a different version of BOS. Please use the current BOS version"));
    }
  }

}
