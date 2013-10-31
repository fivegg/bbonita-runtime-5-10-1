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

import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;

/**
 * @author Matthieu Chaffotte
 * 
 */
public class JobLock implements Serializable {

  private static final long serialVersionUID = 4467961389902738011L;

  private long id;

  private String processUUID;

  private String lockedBy;

  public JobLock() {
    super();
  }

  public JobLock(final ProcessDefinitionUUID definitionUUID) {
    processUUID = definitionUUID.getValue();
  }

  public JobLock(final ProcessInstanceUUID rootProcessInstanceUUID) {
    processUUID = rootProcessInstanceUUID.getValue();
  }

  public long getId() {
    return id;
  }

  public String getProcessUUID() {
    return processUUID;
  }

  public void setProcessUUID(final String processUUID) {
    this.processUUID = processUUID;
  }

  public String getLockedBy() {
    return lockedBy;
  }

  public void setLockedBy(final String lockedBy) {
    this.lockedBy = lockedBy;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (lockedBy == null ? 0 : lockedBy.hashCode());
    result = prime * result + (processUUID == null ? 0 : processUUID.hashCode());
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
    final JobLock other = (JobLock) obj;
    if (lockedBy == null) {
      if (other.lockedBy != null) {
        return false;
      }
    } else if (!lockedBy.equals(other.lockedBy)) {
      return false;
    }
    if (processUUID == null) {
      if (other.processUUID != null) {
        return false;
      }
    } else if (!processUUID.equals(other.processUUID)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "JobLock [id=" + id + ", processUUID=" + processUUID + ", lockedBy=" + lockedBy + "]";
  }

}
