/**
 * Copyright (C) 2012-2013 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
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

import java.io.Serializable;

import org.ow2.bonita.facade.uuid.ActivityDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;

/**
 * @author Matthieu Chaffotte
 * 
 */
public class Job implements Serializable {

  private static final long serialVersionUID = -5719893185504667955L;

  private long id;

  private String eventPosition;

  private String eventType;

  private String eventName;

  private String processUUID;

  private String executionEventUUID;

  private ActivityDefinitionUUID activityDefinitionUUID;

  private String timerExpression;

  private ProcessInstanceUUID eventSubProcessRootInstanceUUID;

  private OutgoingEventInstance outgoingEvent;

  private String exception;

  private int retries;

  private long fireTime;

  /**
   * Used only to clean jobs when deleting a process instance
   */
  private ProcessInstanceUUID instanceUUID;

  protected Job() {
    super();
  }

  public Job(final String eventName, final String eventPosition, final String eventType, final String processUUID,
      final String executionEventUUID, final ActivityDefinitionUUID activityDefinitionUUID,
      final String timerExpression, final long fireTime, final ProcessInstanceUUID instanceUUID) {
    this.eventName = eventName;
    this.eventPosition = eventPosition;
    this.eventType = eventType;
    this.processUUID = processUUID;
    this.executionEventUUID = executionEventUUID;
    this.activityDefinitionUUID = activityDefinitionUUID;
    this.timerExpression = timerExpression;
    this.fireTime = fireTime;
    this.instanceUUID = instanceUUID;
  }

  public String getExecutionUUID() {
    return executionEventUUID;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (activityDefinitionUUID == null ? 0 : activityDefinitionUUID.hashCode());
    result = prime * result + (eventName == null ? 0 : eventName.hashCode());
    result = prime * result + (eventPosition == null ? 0 : eventPosition.hashCode());
    result = prime * result
        + (eventSubProcessRootInstanceUUID == null ? 0 : eventSubProcessRootInstanceUUID.hashCode());
    result = prime * result + (eventType == null ? 0 : eventType.hashCode());
    result = prime * result + (exception == null ? 0 : exception.hashCode());
    result = prime * result + (executionEventUUID == null ? 0 : executionEventUUID.hashCode());
    result = prime * result + (int) (fireTime ^ fireTime >>> 32);
    result = prime * result + (int) (id ^ id >>> 32);
    result = prime * result + (instanceUUID == null ? 0 : instanceUUID.hashCode());
    result = prime * result + (outgoingEvent == null ? 0 : outgoingEvent.hashCode());
    result = prime * result + retries;
    result = prime * result + (processUUID == null ? 0 : processUUID.hashCode());
    result = prime * result + (timerExpression == null ? 0 : timerExpression.hashCode());
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final Job other = (Job) obj;
    if (activityDefinitionUUID == null) {
      if (other.activityDefinitionUUID != null) {
        return false;
      }
    } else if (!activityDefinitionUUID.equals(other.activityDefinitionUUID)) {
      return false;
    }
    if (eventName == null) {
      if (other.eventName != null) {
        return false;
      }
    } else if (!eventName.equals(other.eventName)) {
      return false;
    }
    if (eventPosition == null) {
      if (other.eventPosition != null) {
        return false;
      }
    } else if (!eventPosition.equals(other.eventPosition)) {
      return false;
    }
    if (eventSubProcessRootInstanceUUID == null) {
      if (other.eventSubProcessRootInstanceUUID != null) {
        return false;
      }
    } else if (!eventSubProcessRootInstanceUUID.equals(other.eventSubProcessRootInstanceUUID)) {
      return false;
    }
    if (eventType == null) {
      if (other.eventType != null) {
        return false;
      }
    } else if (!eventType.equals(other.eventType)) {
      return false;
    }
    if (exception == null) {
      if (other.exception != null) {
        return false;
      }
    } else if (!exception.equals(other.exception)) {
      return false;
    }
    if (executionEventUUID == null) {
      if (other.executionEventUUID != null) {
        return false;
      }
    } else if (!executionEventUUID.equals(other.executionEventUUID)) {
      return false;
    }
    if (fireTime != other.fireTime) {
      return false;
    }
    if (id != other.id) {
      return false;
    }
    if (instanceUUID == null) {
      if (other.instanceUUID != null) {
        return false;
      }
    } else if (!instanceUUID.equals(other.instanceUUID)) {
      return false;
    }
    if (outgoingEvent == null) {
      if (other.outgoingEvent != null) {
        return false;
      }
    } else if (!outgoingEvent.equals(other.outgoingEvent)) {
      return false;
    }
    if (retries != other.retries) {
      return false;
    }
    if (processUUID == null) {
      if (other.processUUID != null) {
        return false;
      }
    } else if (!processUUID.equals(other.processUUID)) {
      return false;
    }
    if (timerExpression == null) {
      if (other.timerExpression != null) {
        return false;
      }
    } else if (!timerExpression.equals(other.timerExpression)) {
      return false;
    }
    return true;
  }

  public String getEventPosition() {
    return eventPosition;
  }

  public ActivityDefinitionUUID getActivityDefinitionUUID() {
    return activityDefinitionUUID;
  }

  public ProcessInstanceUUID getEventSubProcessRootInstanceUUID() {
    return eventSubProcessRootInstanceUUID;
  }

  public String getProcessUUID() {
    return processUUID;
  }

  public void setProcessUUID(final String processUUID) {
    this.processUUID = processUUID;
  }

  public String getEventType() {
    return eventType;
  }

  public String getExpression() {
    return timerExpression;
  }

  public long getFireTime() {
    return fireTime;
  }

  /**
   * internal use only
   * 
   * @return
   */
  public int getRetries() {
    return retries;
  }

  public void renewFireTime(final long fireTime) {
    this.fireTime = fireTime;
  }

  public void setRetries(final int retries) {
    this.retries = retries;
  }

  public void setException(final String exception) {
    this.exception = exception;
  }

  public long getId() {
    return id;
  }

  public String getEventName() {
    return eventName;
  }

  public void setEventSubProcessRootInstanceUUID(final ProcessInstanceUUID eventSubProcessRootInstanceUUID) {
    this.eventSubProcessRootInstanceUUID = eventSubProcessRootInstanceUUID;
  }

  @Override
  public String toString() {
    return "Job [id=" + id + ", eventPosition=" + eventPosition + ", eventType=" + eventType + ", eventName="
        + eventName + ", processUUID=" + processUUID + ", executionEventUUID=" + executionEventUUID
        + ", activityDefinitionUUID=" + activityDefinitionUUID + ", timerExpression=" + timerExpression
        + ", eventSubProcessRootInstanceUUID=" + eventSubProcessRootInstanceUUID + ", outgoingEvent=" + outgoingEvent
        + ", exception=" + exception + ", retries=" + retries + ", fireTime=" + fireTime + ", instanceUUID="
        + instanceUUID + "]";
  }

  public OutgoingEventInstance getOutgoingEvent() {
    return outgoingEvent;
  }

  public void setOutgoingEvent(final OutgoingEventInstance outgoingEvent) {
    this.outgoingEvent = outgoingEvent;
  }

  public ProcessInstanceUUID getInstanceUUID() {
    return instanceUUID;
  }

  public void setFireTime(final long fireTime) {
    this.fireTime = fireTime;
  }

}
