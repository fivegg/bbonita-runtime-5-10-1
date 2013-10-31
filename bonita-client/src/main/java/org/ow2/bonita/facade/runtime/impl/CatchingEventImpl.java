/**
 * Copyright (C) 2010  BonitaSoft S.A.
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
package org.ow2.bonita.facade.runtime.impl;

import java.util.Date;

import org.ow2.bonita.facade.runtime.CatchingEvent;
import org.ow2.bonita.facade.uuid.ActivityDefinitionUUID;
import org.ow2.bonita.facade.uuid.ActivityInstanceUUID;
import org.ow2.bonita.facade.uuid.CatchingEventUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.util.xml.XStreamUtil;

import com.thoughtworks.xstream.XStream;

/**
 * 
 * @author Matthieu Chaffotte
 *
 */
public class CatchingEventImpl implements CatchingEvent {

  private static final long serialVersionUID = 1409851250676470415L;
  private CatchingEventUUID uuid;
  private Position position;
  private Type type;

  private long execution;
  private ActivityDefinitionUUID activityDefinitionUUID;
  private ActivityInstanceUUID activityInstanceUUID;
  private ProcessInstanceUUID instanceUUID;
  private String activityName;
  private String processName;

  public CatchingEventImpl(CatchingEventUUID uuid, Position position, Type type, long execution,
      ActivityDefinitionUUID activityDefinitionUUID, ActivityInstanceUUID activityInstanceUUID,
      ProcessInstanceUUID instanceUUID, String activityName, String processName) {
    super();
    this.uuid = uuid;
    this.position = position;
    this.type = type;
    this.execution = execution;
    this.activityDefinitionUUID = activityDefinitionUUID;
    this.activityInstanceUUID = activityInstanceUUID;
    this.instanceUUID = instanceUUID;
    this.activityName = activityName;
    this.processName = processName;
  }

  public CatchingEventUUID getUUID() {
    return uuid;
  }

  public Position getPosition() {
    return position;
  }

  public Date getExecutionDate() {
    return new Date(execution);
  }

  public Type getType() {
    return type;
  }

  public ActivityDefinitionUUID getActivityDefinitionUUID() {
    return activityDefinitionUUID;
  }

  public ProcessInstanceUUID getProcessInstanceUUID() {
    return instanceUUID;
  }

  public ActivityInstanceUUID getActivityInstanceUUID() {
    return activityInstanceUUID;
  }

  public String getProcessName() {
    return processName;
  }

  public String getActivityName() {
    return activityName;
  }

  //Used by REST API
  @Override
  public String toString() {
    XStream xstream = XStreamUtil.getDefaultXstream();
    return xstream.toXML(this);
  }

}
