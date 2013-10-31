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

import org.ow2.bonita.facade.runtime.InstanceState;
import org.ow2.bonita.facade.runtime.ProcessInstance;
import org.ow2.bonita.facade.runtime.impl.RuntimeRecordImpl;
import org.ow2.bonita.facade.uuid.ActivityInstanceUUID;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.light.LightProcessInstance;
import org.ow2.bonita.util.Misc;

/**
 * @author Pierre Vigneras, Charles Souillard, Matthieu Chaffotte
 */
public class LightProcessInstanceImpl extends RuntimeRecordImpl implements LightProcessInstance {

  private static final long serialVersionUID = 8366284714927360659L;

  protected ProcessInstanceUUID parentInstanceUUID;

  protected ActivityInstanceUUID parentActivityUUID;

  protected long nb;

  protected long lastUpdate;

  protected InstanceState state;

  protected String endedBy;

  protected long endedDate;

  protected String startedBy;

  protected long startedDate;

  protected boolean isArchived;

  protected LightProcessInstanceImpl() {
    super();
  }

  public LightProcessInstanceImpl(final ProcessDefinitionUUID processUUID, final ProcessInstanceUUID instanceUUID,
      final ProcessInstanceUUID rootInstanceUUID, final long instanceNb) {
    super(processUUID, instanceUUID, rootInstanceUUID);
    nb = instanceNb;
    state = InstanceState.STARTED;
    lastUpdate = System.currentTimeMillis();
    isArchived = false;
  }

  public LightProcessInstanceImpl(final ProcessInstance processInstance) {
    super(processInstance);
    if (processInstance.getParentInstanceUUID() != null) {
      parentInstanceUUID = new ProcessInstanceUUID(processInstance.getParentInstanceUUID());
    }
    if (processInstance.getParentActivityUUID() != null) {
      parentActivityUUID = new ActivityInstanceUUID(processInstance.getParentActivityUUID());
    }
    nb = processInstance.getNb();
    lastUpdate = Misc.getTime(processInstance.getLastUpdate());
    state = processInstance.getInstanceState();
    endedBy = processInstance.getEndedBy();
    endedDate = Misc.getTime(processInstance.getEndedDate());
    startedBy = processInstance.getStartedBy();
    startedDate = Misc.getTime(processInstance.getStartedDate());
    isArchived = processInstance.isArchived();
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj == null) {
      return false;
    }
    if (!obj.getClass().equals(this.getClass())) {
      return false;
    }
    final LightProcessInstanceImpl other = (LightProcessInstanceImpl) obj;
    if (other.getUUID() == null) {
      return getUUID() == null;
    }
    return other.getUUID().equals(getUUID());
  }

  @Override
  public int hashCode() {
    return getUUID().hashCode();
  }

  @Override
  public String getEndedBy() {
    return endedBy;
  }

  @Override
  public Date getEndedDate() {
    return Misc.getDate(endedDate);
  }

  @Override
  public String getStartedBy() {
    return startedBy;
  }

  @Override
  public Date getStartedDate() {
    return Misc.getDate(startedDate);
  }

  @Override
  public InstanceState getInstanceState() {
    return state;
  }

  @Override
  public ProcessInstanceUUID getParentInstanceUUID() {
    return parentInstanceUUID;
  }

  @Override
  public ActivityInstanceUUID getParentActivityUUID() {
    return parentActivityUUID;
  }

  @Override
  public ProcessInstanceUUID getUUID() {
    return getProcessInstanceUUID();
  }

  @Override
  public Date getLastUpdate() {
    return Misc.getDate(lastUpdate);
  }

  @Override
  public long getNb() {
    return nb;
  }

  @Override
  public ProcessInstanceUUID getRootInstanceUUID() {
    return rootInstanceUUID;
  }

  @Override
  public boolean isArchived() {
    return isArchived;
  }

  @Override
  public String toString() {
    return this.getClass().getName() + "[uuid: " + getUUID() + ", processDefinitionUUID: " + getProcessDefinitionUUID()
        + ", processUUID: " + getProcessInstanceUUID() + ", parentInstanceUUID: " + getParentInstanceUUID()
        + ", parentActivityUUID: " + getParentActivityUUID() + ", startedBy: " + getStartedBy() + ", endedBy: "
        + getEndedBy() + ", startedDate: " + getStartedDate() + ", endedDate: " + getEndedDate()
        + ", rootInstanceUUID: " + getRootInstanceUUID() + ", archived:" + isArchived + "]";
  }

}
