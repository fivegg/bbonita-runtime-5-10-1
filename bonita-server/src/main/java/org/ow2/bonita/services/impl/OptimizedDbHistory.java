/**
 * Copyright (C) 2007  Bull S. A. S.
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
 **/
package org.ow2.bonita.services.impl;

import java.util.Collections;
import java.util.Set;

import org.ow2.bonita.facade.def.InternalProcessDefinition;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition.ProcessState;
import org.ow2.bonita.facade.runtime.ActivityState;
import org.ow2.bonita.facade.runtime.TaskInstance;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.util.Misc;

/**
 *
 * @author Thomas Gueze
 *
 */
public class OptimizedDbHistory extends DbHistory {

  public OptimizedDbHistory(final String name) {
    super(name);
  }

  @Override
  public InternalProcessDefinition getLastDeployedProcess(
      final String processId, final ProcessState processState) {
    Misc.checkArgsNotNull(processId, processState);
    // History does not return deployed packages
    if (processState.equals(ProcessState.ENABLED)) {
      return null;
    }
    return super.getLastDeployedProcess(processId, processState);
  }

  @Override
  public Set<InternalProcessDefinition> getProcesses(final ProcessState processState) {
    Misc.checkArgsNotNull(processState);
    // History does not return deployed processes
    if (processState.equals(ProcessState.ENABLED)) {
      return Collections.emptySet();
    }
    return super.getProcesses(processState);
  }

  @Override
  public Set<InternalProcessDefinition> getProcesses(final String processId, final ProcessState processState) {
    Misc.checkArgsNotNull(processState);
    // History does not return deployed processes
    if (processState.equals(ProcessState.ENABLED)) {
      return Collections.emptySet();
    }
    return super.getProcesses(processId, processState);
  }

  @Override
  public Set<TaskInstance> getUserInstanceTasks(
      final String userId, final ProcessInstanceUUID instanceUUID, final ActivityState taskState) {
    Misc.checkArgsNotNull(taskState);
    // History only contains finished tasks
    if (taskState.equals(ActivityState.READY)
        || taskState.equals(ActivityState.EXECUTING)
        || taskState.equals(ActivityState.SUSPENDED)
    ) {
      return Collections.emptySet();
    }
    return super.getUserInstanceTasks(userId, instanceUUID, taskState);
  }

  @Override
  public Set<TaskInstance> getUserTasks(final String userId, final ActivityState taskState) {
    Misc.checkArgsNotNull(taskState);
    // History only contains finished tasks
    if (taskState.equals(ActivityState.READY)
        || taskState.equals(ActivityState.EXECUTING)
        || taskState.equals(ActivityState.SUSPENDED)
    ) {
      return Collections.emptySet();
    }
    return super.getUserTasks(userId, taskState);
  }
}
