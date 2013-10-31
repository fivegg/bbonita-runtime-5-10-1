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

import java.util.HashSet;
import java.util.Set;

import org.ow2.bonita.facade.uuid.ActivityDefinitionUUID;
import org.ow2.bonita.facade.uuid.ActivityInstanceUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;

/**
 * 
 * @author Matthieu Chaffotte
 * 
 */
public class IncomingEventInstance extends EventInstance {

  private static final long serialVersionUID = -2391431532148921303L;

  private ActivityDefinitionUUID activityDefinitionUUID;

  private String expression;

  private String executionUUID;

  private String signal;

  private long enableTime;

  private boolean executionLocked;

  private String exception;

  private boolean permanent = false;

  private Set<Long> incompatibleEvents;

  private ProcessInstanceUUID eventSubProcessRootInstanceUUID;

  public IncomingEventInstance() {
    super();
  }

  public IncomingEventInstance(final String name, final String expression, final ProcessInstanceUUID instanceUUID,
      final ActivityDefinitionUUID activityDefinitionUUID, final ActivityInstanceUUID activityUUID,
      final String processName, final String activityName, final String executionUUID, final String signal,
      final long enableTime, final boolean executionLocked) {
    super(name, processName, activityName, instanceUUID, activityUUID);
    this.activityDefinitionUUID = activityDefinitionUUID;
    this.expression = expression;
    this.executionUUID = executionUUID;
    this.signal = signal;
    this.enableTime = enableTime;
    this.executionLocked = executionLocked;
  }

  public String getExecutionUUID() {
    return executionUUID;
  }

  public void setException(final String exception) {
    this.exception = exception;
  }

  public String getException() {
    return exception;
  }

  public ActivityDefinitionUUID getActivityDefinitionUUID() {
    return activityDefinitionUUID;
  }

  public String getExpression() {
    return expression;
  }

  public String getSignal() {
    return signal;
  }

  public long getEnableTime() {
    return enableTime;
  }

  public boolean isExecutionLocked() {
    return executionLocked;
  }

  public void setPermanent(final boolean permanent) {
    this.permanent = permanent;
  }

  public boolean isPermanent() {
    return permanent;
  }

  public void setEventSubProcessRootInstanceUUID(final ProcessInstanceUUID eventSubProcessRootInstanceUUID) {
    this.eventSubProcessRootInstanceUUID = eventSubProcessRootInstanceUUID;
  }

  public ProcessInstanceUUID getEventSubProcessRootInstanceUUID() {
    return eventSubProcessRootInstanceUUID;
  }

  public void setEnableTime(final long time) {
    enableTime = time;
  }

  public synchronized void addIncompatibleEvent(final long id) {
    if (incompatibleEvents == null) {
      incompatibleEvents = new HashSet<Long>();
    }
    incompatibleEvents.add(id);
  }

  @Override
  public String toString() {
    final StringBuilder builder = new StringBuilder();
    builder.append("IncomingEventInstance [id=");
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
    builder.append(", activityDefinitionUUID=");
    builder.append(activityDefinitionUUID);
    builder.append(", expression=");
    builder.append(expression);
    builder.append(", executionUUID=");
    builder.append(executionUUID);
    builder.append(", signal=");
    builder.append(signal);
    builder.append(", enableTime=");
    builder.append(enableTime);
    builder.append(", executionLocked=");
    builder.append(executionLocked);
    builder.append(", exception=");
    builder.append(exception);
    builder.append(", permanent=");
    builder.append(permanent);
    builder.append(", incompatibleEvents=");
    builder.append(incompatibleEvents);
    builder.append(", eventSubProcessRootInstanceUUID=");
    builder.append(eventSubProcessRootInstanceUUID);
    builder.append(", getCorrelationKey1()=");
    builder.append(getCorrelationKey1());
    builder.append(", getCorrelationKey2()=");
    builder.append(getCorrelationKey2());
    builder.append(", getCorrelationKey3()=");
    builder.append(getCorrelationKey3());
    builder.append(", getCorrelationKey4()=");
    builder.append(getCorrelationKey4());
    builder.append(", getCorrelationKey5()=");
    builder.append(getCorrelationKey5());
    builder.append("]");
    return builder.toString();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + (activityDefinitionUUID == null ? 0 : activityDefinitionUUID.hashCode());
    result = prime * result + (int) (enableTime ^ enableTime >>> 32);
    result = prime * result
        + (eventSubProcessRootInstanceUUID == null ? 0 : eventSubProcessRootInstanceUUID.hashCode());
    result = prime * result + (exception == null ? 0 : exception.hashCode());
    result = prime * result + (executionLocked ? 1231 : 1237);
    result = prime * result + (executionUUID == null ? 0 : executionUUID.hashCode());
    result = prime * result + (expression == null ? 0 : expression.hashCode());
    result = prime * result + (incompatibleEvents == null ? 0 : incompatibleEvents.hashCode());
    result = prime * result + (permanent ? 1231 : 1237);
    result = prime * result + (signal == null ? 0 : signal.hashCode());
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
    final IncomingEventInstance other = (IncomingEventInstance) obj;
    if (activityDefinitionUUID == null) {
      if (other.activityDefinitionUUID != null) {
        return false;
      }
    } else if (!activityDefinitionUUID.equals(other.activityDefinitionUUID)) {
      return false;
    }
    if (enableTime != other.enableTime) {
      return false;
    }
    if (eventSubProcessRootInstanceUUID == null) {
      if (other.eventSubProcessRootInstanceUUID != null) {
        return false;
      }
    } else if (!eventSubProcessRootInstanceUUID.equals(other.eventSubProcessRootInstanceUUID)) {
      return false;
    }
    if (exception == null) {
      if (other.exception != null) {
        return false;
      }
    } else if (!exception.equals(other.exception)) {
      return false;
    }
    if (executionLocked != other.executionLocked) {
      return false;
    }
    if (executionUUID == null) {
      if (other.executionUUID != null) {
        return false;
      }
    } else if (!executionUUID.equals(other.executionUUID)) {
      return false;
    }
    if (expression == null) {
      if (other.expression != null) {
        return false;
      }
    } else if (!expression.equals(other.expression)) {
      return false;
    }
    if (incompatibleEvents == null) {
      if (other.incompatibleEvents != null) {
        return false;
      }
    } else if (!incompatibleEvents.equals(other.incompatibleEvents)) {
      return false;
    }
    if (permanent != other.permanent) {
      return false;
    }
    if (signal == null) {
      if (other.signal != null) {
        return false;
      }
    } else if (!signal.equals(other.signal)) {
      return false;
    }
    return true;
  }

}
