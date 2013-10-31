/**
 * Copyright (C) 2010-2013 BonitaSoft S.A.
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

import java.io.Serializable;

import org.ow2.bonita.facade.uuid.ActivityInstanceUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;

/**
 * 
 * @author Charles Souillard, Matthieu Chaffotte
 * 
 */
public abstract class EventInstance implements Serializable {

  private static final long serialVersionUID = 1702669672454090624L;

  public static final String TIMER = "timer";

  protected long id;

  protected String name;

  protected String processName;

  protected String activityName;

  protected ProcessInstanceUUID instanceUUID;

  protected ActivityInstanceUUID activityUUID;

  private String correlationKey1;

  private String correlationKey2;

  private String correlationKey3;

  private String correlationKey4;

  private String correlationKey5;

  private boolean locked = false;

  protected EventInstance() {
    super();
  }

  protected EventInstance(final String name, final String processName, final String activityName,
      final ProcessInstanceUUID instanceUUID, final ActivityInstanceUUID activityUUID) {
    this.name = name;
    this.processName = processName;
    this.activityName = activityName;
    this.activityUUID = activityUUID;
    this.instanceUUID = instanceUUID;
  }

  public String getName() {
    return name;
  }

  public String getProcessName() {
    return processName;
  }

  public String getActivityName() {
    return activityName;
  }

  public long getId() {
    return id;
  }

  public ActivityInstanceUUID getActivityUUID() {
    return activityUUID;
  }

  public ProcessInstanceUUID getInstanceUUID() {
    return instanceUUID;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (activityName == null ? 0 : activityName.hashCode());
    result = prime * result + (activityUUID == null ? 0 : activityUUID.hashCode());
    result = prime * result + (correlationKey1 == null ? 0 : correlationKey1.hashCode());
    result = prime * result + (correlationKey2 == null ? 0 : correlationKey2.hashCode());
    result = prime * result + (correlationKey3 == null ? 0 : correlationKey3.hashCode());
    result = prime * result + (correlationKey4 == null ? 0 : correlationKey4.hashCode());
    result = prime * result + (correlationKey5 == null ? 0 : correlationKey5.hashCode());
    result = prime * result + (int) (id ^ id >>> 32);
    result = prime * result + (instanceUUID == null ? 0 : instanceUUID.hashCode());
    result = prime * result + (locked ? 1231 : 1237);
    result = prime * result + (name == null ? 0 : name.hashCode());
    result = prime * result + (processName == null ? 0 : processName.hashCode());
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
    final EventInstance other = (EventInstance) obj;
    if (activityName == null) {
      if (other.activityName != null) {
        return false;
      }
    } else if (!activityName.equals(other.activityName)) {
      return false;
    }
    if (activityUUID == null) {
      if (other.activityUUID != null) {
        return false;
      }
    } else if (!activityUUID.equals(other.activityUUID)) {
      return false;
    }
    if (correlationKey1 == null) {
      if (other.correlationKey1 != null) {
        return false;
      }
    } else if (!correlationKey1.equals(other.correlationKey1)) {
      return false;
    }
    if (correlationKey2 == null) {
      if (other.correlationKey2 != null) {
        return false;
      }
    } else if (!correlationKey2.equals(other.correlationKey2)) {
      return false;
    }
    if (correlationKey3 == null) {
      if (other.correlationKey3 != null) {
        return false;
      }
    } else if (!correlationKey3.equals(other.correlationKey3)) {
      return false;
    }
    if (correlationKey4 == null) {
      if (other.correlationKey4 != null) {
        return false;
      }
    } else if (!correlationKey4.equals(other.correlationKey4)) {
      return false;
    }
    if (correlationKey5 == null) {
      if (other.correlationKey5 != null) {
        return false;
      }
    } else if (!correlationKey5.equals(other.correlationKey5)) {
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
    if (locked != other.locked) {
      return false;
    }
    if (name == null) {
      if (other.name != null) {
        return false;
      }
    } else if (!name.equals(other.name)) {
      return false;
    }
    if (processName == null) {
      if (other.processName != null) {
        return false;
      }
    } else if (!processName.equals(other.processName)) {
      return false;
    }
    return true;
  }

  public String getCorrelationKey1() {
    return correlationKey1;
  }

  public void setCorrelationKey1(final String correlationKey1) {
    this.correlationKey1 = correlationKey1;
  }

  public String getCorrelationKey2() {
    return correlationKey2;
  }

  public void setCorrelationKey2(final String correlationKey2) {
    this.correlationKey2 = correlationKey2;
  }

  public String getCorrelationKey3() {
    return correlationKey3;
  }

  public void setCorrelationKey3(final String correlationKey3) {
    this.correlationKey3 = correlationKey3;
  }

  public String getCorrelationKey4() {
    return correlationKey4;
  }

  public void setCorrelationKey4(final String correlationKey4) {
    this.correlationKey4 = correlationKey4;
  }

  public String getCorrelationKey5() {
    return correlationKey5;
  }

  public void setCorrelationKey5(final String correlationKey5) {
    this.correlationKey5 = correlationKey5;
  }

  public void setLocked(final boolean locked) {
    this.locked = locked;
  }

}
