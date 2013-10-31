/**
 * Copyright (C) 2006  Bull S. A. S.
 * Bull, Rue Jean Jaures, B.P.68, 78340, Les Clayes-sous-Bois
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA  02110-1301, USA.
 * 
 * Modified by Matthieu Chaffotte - BonitaSoft S.A.
 **/
package org.ow2.bonita.services.impl;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.ow2.bonita.APITestCase;
import org.ow2.bonita.env.generator.DbHistoryEnvGenerator;
import org.ow2.bonita.env.generator.EnvGenerator;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.exception.InstanceNotFoundException;
import org.ow2.bonita.facade.runtime.ActivityState;
import org.ow2.bonita.facade.runtime.TaskInstance;
import org.ow2.bonita.facade.runtime.impl.InternalProcessInstance;
import org.ow2.bonita.facade.uuid.ActivityInstanceUUID;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.services.handlers.FinishedInstanceHandler;
import org.ow2.bonita.services.handlers.impl.DeleteFinishedInstanceHandler;
import org.ow2.bonita.util.BonitaException;

public class ChainFinishedInstanceHandlerTest extends APITestCase {

  public static class TestFinishedInstanceHandler implements FinishedInstanceHandler {
    private static List<ProcessInstanceUUID> finished = new ArrayList<ProcessInstanceUUID>();

    @Override
    public void handleFinishedInstance(final InternalProcessInstance instance) {
      finished.add(instance.getUUID());
    }
  }

  public static EnvGenerator getEnvGenerator() {
    final EnvGenerator envGenerator = new DbHistoryEnvGenerator();
    envGenerator.addApplicationEntry(EnvGenerator.getChainerEnvEntry(FinishedInstanceHandler.DEFAULT_KEY, "", true,
        DeleteFinishedInstanceHandler.class, TestFinishedInstanceHandler.class));
    return envGenerator;
  }

  public void testChainFIH() throws BonitaException {
    final URL xpdlUrl = this.getClass().getResource("simple_process.xpdl");
    TestFinishedInstanceHandler.finished.clear();

    final ProcessDefinition process = getManagementAPI().deploy(getBusinessArchiveFromXpdl(xpdlUrl));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    // Check there is an instance record in the journal
    assertNotNull(getQueryRuntimeAPI().getProcessInstance(instanceUUID));
    assertTrue(TestFinishedInstanceHandler.finished.isEmpty());

    // start & terminate "act1" task
    final Collection<TaskInstance> taskActivities = getQueryRuntimeAPI().getTaskList(ActivityState.READY);
    assertEquals(1, taskActivities.size());
    final ActivityInstanceUUID taskUUID = taskActivities.iterator().next().getUUID();
    getRuntimeAPI().startTask(taskUUID, true);
    getRuntimeAPI().finishTask(taskUUID, true);

    // check there is no record for this instance in the journal or in the archive.
    try {
      getQueryRuntimeAPI().getProcessInstance(instanceUUID);
      fail("This instance does not exist");
    } catch (final InstanceNotFoundException e) {
      assertEquals(instanceUUID, e.getInstanceUUID());
    }
    assertTrue(getQueryRuntimeAPI().getActivityInstances(instanceUUID).isEmpty());
    assertTrue(getQueryRuntimeAPI().getTasks(instanceUUID).isEmpty());

    assertFalse(TestFinishedInstanceHandler.finished.isEmpty());
    assertTrue(TestFinishedInstanceHandler.finished.contains(instanceUUID));

    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

}
