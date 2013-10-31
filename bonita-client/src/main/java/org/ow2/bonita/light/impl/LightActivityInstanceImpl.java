/**
 * Copyright (C) 2009-2013 BonitaSoft S.A.
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
package org.ow2.bonita.light.impl;

import java.util.Date;

import org.ow2.bonita.facade.def.majorElement.ActivityDefinition;
import org.ow2.bonita.facade.def.majorElement.ActivityDefinition.Type;
import org.ow2.bonita.facade.runtime.ActivityInstance;
import org.ow2.bonita.facade.runtime.ActivityState;
import org.ow2.bonita.facade.runtime.TaskInstance;
import org.ow2.bonita.facade.runtime.impl.RuntimeRecordImpl;
import org.ow2.bonita.facade.uuid.ActivityDefinitionUUID;
import org.ow2.bonita.facade.uuid.ActivityInstanceUUID;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.light.LightTaskInstance;
import org.ow2.bonita.util.Misc;

/**
 * @author Pierre Vigneras, Matthieu Chaffotte
 */
public class LightActivityInstanceImpl extends RuntimeRecordImpl implements LightTaskInstance {
  private static final long serialVersionUID = -8515098234372896097L;

  protected ActivityInstanceUUID uuid;
  protected String iterationId;
  protected String activityInstanceId;
  protected String loopId;

  protected ActivityState state;
  protected String userId;
  protected long lastUpdate;

  protected String label;
  protected String description;
  protected String dynamicLabel;
  protected String dynamicDescription;
  protected String name;
  protected long startedDate;
  protected long endedDate;
  protected long readyDate;
  protected String endedBy;
  protected String startedBy;
  protected String executionSummary;

  protected ActivityDefinitionUUID activityDefinitionUUID;
  protected long expectedEndDate;
  protected int priority;
  protected ActivityDefinition.Type type;
  protected ProcessInstanceUUID subflowProcessInstanceUUID;
  protected boolean human = false;

  protected LightActivityInstanceImpl() {
    super();
  }

  public LightActivityInstanceImpl(final ActivityInstanceUUID uuid, final ActivityDefinition activityDefinition,
      final ProcessDefinitionUUID processUUID, final ProcessInstanceUUID instanceUUID,
      final ProcessInstanceUUID rootInstanceUUID, final String iterationId, final String activityInstanceId,
      final String loopId) {
    super(processUUID, instanceUUID, rootInstanceUUID);
    Misc.checkArgsNotNull(uuid, activityDefinition);
    this.uuid = uuid;
    this.iterationId = iterationId;
    this.activityInstanceId = activityInstanceId;
    this.loopId = loopId;
    state = ActivityState.READY;
    lastUpdate = System.currentTimeMillis();
    activityDefinitionUUID = activityDefinition.getUUID();
    priority = activityDefinition.getPriority();
    type = activityDefinition.getType();
    name = activityDefinition.getName();
    description = activityDefinition.getDescription();
    label = activityDefinition.getLabel();
    human = isTask();
    final long executingTime = activityDefinition.getExecutingTime();
    if (executingTime > 0) {
      expectedEndDate = System.currentTimeMillis() + executingTime;
    }
  }

  public LightActivityInstanceImpl(final ActivityInstance src) {
    super(src);
    uuid = new ActivityInstanceUUID(src.getUUID());
    iterationId = src.getIterationId();
    activityInstanceId = src.getActivityInstanceId();
    loopId = src.getLoopId();
    lastUpdate = Misc.getTime(src.getLastUpdateDate());
    startedDate = Misc.getTime(src.getStartedDate());
    endedDate = Misc.getTime(src.getEndedDate());
    expectedEndDate = Misc.getTime(src.getExpectedEndDate());
    readyDate = Misc.getTime(src.getReadyDate());
    activityDefinitionUUID = new ActivityDefinitionUUID(src.getActivityDefinitionUUID());
    if (src.getSubflowProcessInstanceUUID() != null) {
      subflowProcessInstanceUUID = new ProcessInstanceUUID(src.getSubflowProcessInstanceUUID());
    }

    priority = src.getPriority();
    type = src.getType();
    name = src.getActivityName();
    description = src.getActivityDescription();
    label = src.getActivityLabel();
    dynamicDescription = src.getDynamicDescription();
    dynamicLabel = src.getDynamicLabel();
    executionSummary = src.getDynamicExecutionSummary();

    if (src.isTask()) {
      final TaskInstance task = src.getTask();
      human = true;
      startedBy = task.getStartedBy();
      endedBy = task.getEndedBy();
      userId = task.getTaskUser();
    }
    state = src.getState();
  }

  @Override
  public String getActivityLabel() {
    return label;
  }

  @Override
  public String getActivityDescription() {
    return description;
  }

  @Override
  public String getDynamicDescription() {
    return dynamicDescription;
  }

  @Override
  public String getDynamicLabel() {
    return dynamicLabel;
  }

  @Override
  public String toString() {
    String userId;
    try {
      userId = getTaskUser();
    } catch (final IllegalStateException e) {
      userId = null;
    }

    final String st = this.getClass().getName() + "[uuid: " + getUUID() + ", activityId: " + getActivityName()
        + ", iterationId: " + getIterationId() + ", processDefinitionUUID: " + getProcessDefinitionUUID()
        + ", processUUID: " + getProcessInstanceUUID() + ", startedDate: " + getStartedDate() + ", endedDate: "
        + getEndedDate() + ", readyDate: " + getReadyDate() + ", userId: " + userId + ", state: " + getState()
        + ", createdDate: " + getCreatedDate() + ", startedBy: " + getStartedBy() + ", startedDate: "
        + getStartedDate() + ", endedDate: " + getEndedDate() + ", endedBy: " + getEndedBy() + "]";
    return st;
  }

  @Override
  public LightTaskInstance getTask() {
    if (isTask()) {
      return this;
    }
    return null;
  }

  @Override
  public String getIterationId() {
    return iterationId;
  }

  @Override
  public String getActivityInstanceId() {
    return activityInstanceId;
  }

  @Override
  public String getLoopId() {
    return loopId;
  }

  @Override
  public String getActivityName() {
    return name;
  }

  @Override
  public ActivityInstanceUUID getUUID() {
    return uuid;
  }

  @Override
  public Date getStartedDate() {
    return Misc.getDate(startedDate);
  }

  @Override
  public Date getEndedDate() {
    return Misc.getDate(endedDate);
  }

  @Override
  public Date getReadyDate() {
    return Misc.getDate(readyDate);
  }

  @Override
  public Date getCreatedDate() {
    return getReadyDate();
  }

  @Override
  public String getEndedBy() {
    return endedBy;
  }

  @Override
  public String getStartedBy() {
    return startedBy;
  }

  @Override
  public ActivityState getState() {
    return state;
  }

  @Override
  public String getTaskUser() {
    return userId;
  }

  @Override
  public boolean isTaskAssigned() {
    return userId != null;
  }

  @Override
  public Date getLastUpdateDate() {
    return Misc.getDate(lastUpdate);
  }

  @Override
  public ActivityDefinitionUUID getActivityDefinitionUUID() {
    return activityDefinitionUUID;
  }

  @Override
  public int getPriority() {
    return priority;
  }

  @Override
  public Date getExpectedEndDate() {
    return Misc.getDate(expectedEndDate);
  }

  @Override
  public ProcessInstanceUUID getSubflowProcessInstanceUUID() {
    return subflowProcessInstanceUUID;
  }

  @Override
  public boolean isAutomatic() {
    return Type.Automatic.equals(getType());
  }

  @Override
  public boolean isSubflow() {
    return Type.Subflow.equals(getType());
  }

  @Override
  public boolean isTimer() {
    return Type.Timer.equals(getType());
  }

  @Override
  public boolean isTask() {
    return Type.Human.equals(getType());
  }

  @Override
  public Type getType() {
    return type;
  }

  @Override
  public String getDynamicExecutionSummary() {
    return executionSummary;
  }

}
