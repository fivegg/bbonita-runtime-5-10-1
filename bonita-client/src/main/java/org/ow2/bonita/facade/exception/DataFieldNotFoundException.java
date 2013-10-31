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
package org.ow2.bonita.facade.exception;

import org.ow2.bonita.facade.uuid.ActivityDefinitionUUID;
import org.ow2.bonita.facade.uuid.DataFieldDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.util.BonitaException;
import org.ow2.bonita.util.ExceptionManager;

/**
 * Thrown by methods of the QueryDefinitionAPI if the dataField definition is not found.
 */
public class DataFieldNotFoundException extends BonitaException {

  private static final long serialVersionUID = -2248193638347196601L;
  private final String dataFieldId;
  private final ProcessDefinitionUUID processUUID;
  private final ActivityDefinitionUUID activityUUID;
  private final DataFieldDefinitionUUID dataFieldUUID;

  public DataFieldNotFoundException(final String id, final String dataFieldId, final ProcessDefinitionUUID processDefinitionUUID) {
    super(ExceptionManager.getInstance().getIdMessage(id)
    		+ ExceptionManager.getInstance().getMessage("DFNFE1", dataFieldId, processDefinitionUUID));
    this.dataFieldId = dataFieldId;
    this.processUUID = processDefinitionUUID;
    this.activityUUID = null;
    this.dataFieldUUID = null;
  }

  public DataFieldNotFoundException(final String id, final String dataFieldId, final ActivityDefinitionUUID activityDefinitionUUID) {
  	super(ExceptionManager.getInstance().getIdMessage(id)
    		+ ExceptionManager.getInstance().getMessage("DFNFE2", dataFieldId, activityDefinitionUUID));
    this.dataFieldId = dataFieldId;
    this.processUUID = null;
    this.activityUUID = activityDefinitionUUID;
    this.dataFieldUUID = null;
  }

  public DataFieldNotFoundException(final String id, final DataFieldDefinitionUUID dataFieldDefinitionUUID) {
  	super(ExceptionManager.getInstance().getIdMessage(id)
    		+ ExceptionManager.getInstance().getMessage("DFNFE3", dataFieldDefinitionUUID));
    this.dataFieldUUID = dataFieldDefinitionUUID;
    this.dataFieldId = null;
    this.processUUID = null;
    this.activityUUID = null;
  }

  public DataFieldNotFoundException(DataFieldNotFoundException e) {
    super(e.getMessage());
    this.dataFieldId = e.getDataFieldId();
    this.processUUID = e.getProcessUUID();
    this.activityUUID = e.getActivityUUID();
    this.dataFieldUUID = e.getDataFieldUUID();
  }

  public static DataFieldNotFoundException build(final String id, Throwable e) {
    if (!(e instanceof DataFieldNotFoundException)) {
    	ExceptionManager manager = ExceptionManager.getInstance();
    	String message = manager.getIdMessage(id) + manager.getMessage("DFNFE4");
      throw new BonitaInternalException(message, e);
    }
    return new DataFieldNotFoundException((DataFieldNotFoundException)e);
  }

  public String getDataFieldId() {
    return this.dataFieldId;
  }
  public ProcessDefinitionUUID getProcessUUID() {
    return processUUID;
  }
  public DataFieldDefinitionUUID getDataFieldUUID() {
    return dataFieldUUID;
  }

  public ActivityDefinitionUUID getActivityUUID() {
    return activityUUID;
  }
}
