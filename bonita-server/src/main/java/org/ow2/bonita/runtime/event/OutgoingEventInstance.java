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
package org.ow2.bonita.runtime.event;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.ow2.bonita.facade.runtime.ProcessInstance;
import org.ow2.bonita.facade.uuid.ActivityInstanceUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.type.Variable;
import org.ow2.bonita.util.EnvTool;
import org.ow2.bonita.util.VariableUtil;

/**
 * 
 * @author Matthieu Chaffotte
 * 
 */
public class OutgoingEventInstance extends EventInstance {

  private static final long serialVersionUID = -4173833339581321213L;

  private Map<String, Variable> parameters;

  private long overdue;

  private Long incomingId;

  public OutgoingEventInstance() {
    super();
  }

  public OutgoingEventInstance(final String name, final String processName, final String activityName,
      final Map<String, Object> parameters, final ProcessInstanceUUID instanceUUID,
      final ActivityInstanceUUID activityUUID, final long overdue) {
    super(name, processName, activityName, instanceUUID, activityUUID);
    if (parameters != null) {
      this.parameters = new HashMap<String, Variable>();
      final ProcessInstance instance = EnvTool.getJournalQueriers().getProcessInstance(instanceUUID);
      for (final Entry<String, Object> parameter : parameters.entrySet()) {
        final String key = parameter.getKey();
        final Object value = parameter.getValue();
        this.parameters.put(key, VariableUtil.createVariable(instance.getProcessDefinitionUUID(), key, value));
      }
    }
    this.overdue = overdue;
  }

  public Map<String, Object> getParameters() {
    if (parameters == null) {
      return Collections.emptyMap();
    }
    final Map<String, Object> result = new HashMap<String, Object>();
    for (final Map.Entry<String, Variable> entry : parameters.entrySet()) {
      result.put(entry.getKey(), entry.getValue().getValue());
    }
    return result;
  }

  public long getOverdue() {
    return overdue;
  }

  public Long getIncomingId() {
    return incomingId;
  }

  public void setIncomingId(final Long incomingId) {
    this.incomingId = incomingId;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + (incomingId == null ? 0 : incomingId.hashCode());
    result = prime * result + (int) (overdue ^ overdue >>> 32);
    result = prime * result + (parameters == null ? 0 : parameters.hashCode());
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final OutgoingEventInstance other = (OutgoingEventInstance) obj;
    if (incomingId == null) {
      if (other.incomingId != null) {
        return false;
      }
    } else if (!incomingId.equals(other.incomingId)) {
      return false;
    }
    if (overdue != other.overdue) {
      return false;
    }
    if (parameters == null) {
      if (other.parameters != null) {
        return false;
      }
    } else if (!parameters.equals(other.parameters)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    final StringBuilder builder = new StringBuilder();
    builder.append("OutgoingEventInstance [id=");
    builder.append(getId());
    builder.append(", name=");
    builder.append(getName());
    builder.append(", process name=");
    builder.append(getProcessName());
    builder.append(", process instance UUID=");
    builder.append(getInstanceUUID());
    builder.append(", activity name=");
    builder.append(getActivityName());
    builder.append(", activity instance UUID=");
    builder.append(getActivityUUID());
    builder.append(", correlation key 1=");
    builder.append(getCorrelationKey1());
    builder.append(", correlation key 2=");
    builder.append(getCorrelationKey2());
    builder.append(", correlation key 3=");
    builder.append(getCorrelationKey3());
    builder.append(", correlation key 4=");
    builder.append(getCorrelationKey4());
    builder.append(", correlation key 5=");
    builder.append(getCorrelationKey5());
    builder.append(", parameters=");
    builder.append(parameters);
    builder.append(", overdue=");
    builder.append(overdue);
    builder.append("]");
    return builder.toString();
  }

}
