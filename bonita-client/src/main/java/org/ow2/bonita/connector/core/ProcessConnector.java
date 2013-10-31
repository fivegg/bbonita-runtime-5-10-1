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

import org.ow2.bonita.facade.APIAccessor;
import org.ow2.bonita.facade.uuid.ActivityInstanceUUID;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;

/**
 * A Process Connector is a Connector with specific fields. It uses process and activity instance UUID
 * and Process Definition UUID and an apiAccssor to deal with Bonita APIs
 * @author Matthieu Chaffotte
 *
 */
public abstract class ProcessConnector extends Connector {

  private ProcessInstanceUUID processInstanceUUID;
  private ProcessDefinitionUUID processDefinitionUUID;
  private ActivityInstanceUUID activityInstanceUUID;
  private APIAccessor apiAccessor;

  public ProcessInstanceUUID getProcessInstanceUUID() {
    return processInstanceUUID;
  }

  public void setProcessInstanceUUID(ProcessInstanceUUID processInstanceUUID) {
    this.processInstanceUUID = processInstanceUUID;
  }

  public ProcessDefinitionUUID getProcessDefinitionUUID() {
    return processDefinitionUUID;
  }

  public void setProcessDefinitionUUID(ProcessDefinitionUUID processDefinitionUUID) {
    this.processDefinitionUUID = processDefinitionUUID;
  }

  @Deprecated
  public ActivityInstanceUUID getActivitytInstanceUUID() {
    return activityInstanceUUID;
  }
  
  public ActivityInstanceUUID getActivityInstanceUUID() {
    return activityInstanceUUID;
  }

  public void setActivityInstanceUUID(ActivityInstanceUUID activityInstanceUUID) {
    this.activityInstanceUUID = activityInstanceUUID;
  }

  public APIAccessor getApiAccessor() {
    return apiAccessor;
  }

  public void setApiAccessor(APIAccessor apiAccessor) {
    this.apiAccessor = apiAccessor;
  }

}
