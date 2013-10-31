/**
 * Copyright (C) 2009  BonitaSoft S.A.
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
package org.ow2.bonita.connector.core;

import java.util.List;

import org.ow2.bonita.definition.MultiInstantiatorDescriptor;
import org.ow2.bonita.facade.QueryAPIAccessor;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;

/**
 * 
 * @author Matthieu Chaffotte
 *
 */
public abstract class MultiInstantiator extends Connector implements org.ow2.bonita.definition.MultiInstantiator {

  private ProcessInstanceUUID processInstanceUUID;
  private String activityId;
  private String iterationId;
  private MultiInstantiatorDescriptor descriptor;

  public ProcessInstanceUUID getProcessInstanceUUID() {
    return processInstanceUUID;
  }

  public void setProcessInstanceUUID(ProcessInstanceUUID processInstanceUUID) {
    this.processInstanceUUID = processInstanceUUID;
  }

  public String getActivityId() {
    return activityId;
  }

  public void setActivityId(String activityId) {
    this.activityId = activityId;
  }

  public String getIterationId() {
    return iterationId;
  }

  public void setIterationId(String iterationId) {
    this.iterationId = iterationId;
  }

  public MultiInstantiatorDescriptor getDescriptor() {
    return descriptor;
  }

  public void setDescriptor(MultiInstantiatorDescriptor descriptor) {
    this.descriptor = descriptor;
  }

  public final MultiInstantiatorDescriptor execute(QueryAPIAccessor accessor,
      ProcessInstanceUUID instanceUUID, String activityId, String iterationId) throws Exception {
    setActivityId(activityId);
    setIterationId(iterationId);
    setProcessInstanceUUID(instanceUUID);
    executeConnector();
    return descriptor;
  }

  @Override
  protected final void executeConnector() throws Exception {
    descriptor = new MultiInstantiatorDescriptor(getJoinNumber(), getVariableValues());
  }

  public abstract List<Object> getVariableValues();
  public abstract int getJoinNumber();
}
