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
 * 
 * Modified by Matthieu Chaffotte - BonitaSoft S.A.
 **/
package org.ow2.bonita.facade.runtime.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ow2.bonita.facade.def.majorElement.ActivityDefinition;
import org.ow2.bonita.facade.runtime.ActivityInstance;
import org.ow2.bonita.facade.runtime.AssignUpdate;
import org.ow2.bonita.facade.runtime.StateUpdate;
import org.ow2.bonita.facade.runtime.TaskInstance;
import org.ow2.bonita.facade.runtime.Update;
import org.ow2.bonita.facade.runtime.VariableUpdate;
import org.ow2.bonita.facade.uuid.ActivityInstanceUUID;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.light.impl.LightActivityInstanceImpl;
import org.ow2.bonita.util.CopyTool;
import org.ow2.bonita.util.ExceptionManager;

/**
 * @author Pierre Vigneras
 */
public class ActivityInstanceImpl extends LightActivityInstanceImpl implements TaskInstance {

  private static final long serialVersionUID = -8515098234372896097L;

  protected List<StateUpdate> stateUpdates = new ArrayList<StateUpdate>();
  protected Map<String, Object> clientVariables;
  protected List<VariableUpdate> variableUpdates = new ArrayList<VariableUpdate>();
  protected List<AssignUpdate> assignUpdates;
  protected Set<String> candidates;

  protected ActivityInstanceImpl() {
    super();
  }

  public ActivityInstanceImpl(final ActivityInstanceUUID uuid, final ActivityDefinition activityDefinition,
      final ProcessDefinitionUUID processUUID, final ProcessInstanceUUID instanceUUID,
      final ProcessInstanceUUID rootInstanceUUID, final String iterationId, final String activityInstanceId,
      final String loopId) {
    super(uuid, activityDefinition, processUUID, instanceUUID, rootInstanceUUID, iterationId, activityInstanceId,
        loopId);
  }

  public ActivityInstanceImpl(final ActivityInstance src) {
    super(src);
    clientVariables = src.getVariablesBeforeStarted();

    final List<VariableUpdate> list = src.getVariableUpdates();
    if (list != null && !list.isEmpty()) {
      variableUpdates = new ArrayList<VariableUpdate>();
      for (final VariableUpdate varUpdate : list) {
        variableUpdates.add(new VariableUpdateImpl(varUpdate));
      }
    }
    final List<StateUpdate> stateList = src.getStateUpdates();
    if (stateList != null && !stateList.isEmpty()) {
      stateUpdates = new ArrayList<StateUpdate>();
      for (final StateUpdate update : stateList) {
        stateUpdates.add(new StateUpdateImpl(update));
      }
    }

    if (src.isTask()) {
      final TaskInstance task = src.getTask();
      final List<AssignUpdate> assignList = task.getAssignUpdates();
      if (assignList != null && !assignList.isEmpty()) {
        assignUpdates = new ArrayList<AssignUpdate>();
        for (final AssignUpdate update : assignList) {
          assignUpdates.add(new AssignUpdateImpl(update));
        }
      }
      candidates = CopyTool.copy(task.getTaskCandidates());
    }
  }

  @Override
  public String toString() {
    String userId;
    try {
      userId = getTaskUser();
    } catch (final IllegalStateException e) {
      userId = null;
    }
    Set<String> candidates;
    try {
      candidates = getTaskCandidates();
    } catch (final IllegalStateException e) {
      candidates = null;
    }

    final StringBuilder builder = new StringBuilder(this.getClass().getName());
    builder.append("[uuid: ").append(getUUID()).append(", activityId: ").append(getActivityName())
        .append(", iterationId: ").append(getIterationId()).append(", loopId: ").append(getLoopId())
        .append(", processDefinitionUUID: ").append(getProcessDefinitionUUID()).append(", processUUID: ")
        .append(getProcessInstanceUUID()).append(", variablesBeforeStarted: ").append(getVariablesBeforeStarted())
        .append(", variableUpdates: ").append(getVariableUpdates()).append(", startedDate: ").append(getStartedDate())
        .append(", endedDate: ").append(getEndedDate()).append(", readyDate: ").append(getReadyDate())
        .append(", userId: ").append(userId).append(", candidates: ").append(candidates).append(", state: ")
        .append(getState()).append(", createdDate: ").append(getCreatedDate()).append(", startedBy: ")
        .append(getStartedBy()).append(", startedDate: ").append(getStartedDate()).append(", endedDate: ")
        .append(getEndedDate()).append(", endedBy: ").append(getEndedBy()).append("]");
    return builder.toString();
  }

  @Override
  public Map<String, Object> getVariablesBeforeStarted() {
    return clientVariables;
  }

  @Override
  public Object getVariableValueBeforeStarted(final String variableId) {
    return getVariablesBeforeStarted().get(variableId);
  }

  @Override
  public TaskInstance getTask() {
    if (isTask()) {
      return this;
    }
    return null;
  }

  @Override
  public List<VariableUpdate> getVariableUpdates() {
    if (variableUpdates == null) {
      return Collections.emptyList();
    }
    return variableUpdates;
  }

  @Override
  public Map<String, Object> getLastKnownVariableValues() {
    Map<String, Object> var = getVariablesBeforeStarted();
    if (var != null) {
      var = new HashMap<String, Object>(var);
    } else {
      var = new HashMap<String, Object>();
    }
    for (final VariableUpdate varUp : getVariableUpdates()) {
      var.put(varUp.getName(), varUp.getValue());
    }
    return var;
  }

  @Override
  public List<StateUpdate> getStateUpdates() {
    return stateUpdates;
  }

  @Override
  public String getUpdatedBy() {
    final Update lastUpdate = getLastUpdate();
    if (lastUpdate == null) {
      return null;
    }
    if (lastUpdate.getUpdatedBy() != null) {
      return lastUpdate.getUpdatedBy();
    }
    final String message = ExceptionManager.getInstance().getFullMessage("baoi_TII_2", getUUID());
    throw new IllegalStateException(message);
  }

  @Override
  public List<AssignUpdate> getAssignUpdates() {
    return assignUpdates;
  }

  @Override
  public StateUpdate getLastStateUpdate() {
    if (stateUpdates != null && !stateUpdates.isEmpty()) {
      return stateUpdates.get(stateUpdates.size() - 1);
    }
    return null;
  }

  @Override
  public AssignUpdate getLastAssignUpdate() {
    if (assignUpdates != null && !assignUpdates.isEmpty()) {
      return assignUpdates.get(assignUpdates.size() - 1);
    }
    return null;
  }

  protected Update getLastUpdate() {
    Update lastState = null;
    if (stateUpdates != null && !stateUpdates.isEmpty()) {
      lastState = stateUpdates.get(stateUpdates.size() - 1);
    }

    Update lastAssign = null;
    if (assignUpdates != null && !assignUpdates.isEmpty()) {
      lastAssign = assignUpdates.get(assignUpdates.size() - 1);
    }

    if (lastState == null && lastAssign == null) {
      return null;
    } else if (lastState == null && lastAssign != null) {
      return lastAssign;
    } else if (lastState != null && lastAssign == null) {
      return lastState;
    }
    if (lastAssign.getUpdatedDate().getTime() <= lastState.getUpdatedDate().getTime()) {
      return lastState;
    }
    return lastAssign;
  }

  @Override
  public Set<String> getTaskCandidates() {
    if (candidates == null) {
      return Collections.emptySet();
    }
    return candidates;
  }

}
