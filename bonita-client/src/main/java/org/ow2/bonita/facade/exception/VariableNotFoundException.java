/**
 * Copyright (C) 2007  Bull S. A. S.
 * Bull, Rue Jean Jaures, B.P.68, 78340, Les Clayes-sous-Bois
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

import org.ow2.bonita.facade.uuid.ActivityInstanceUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.util.BonitaException;
import org.ow2.bonita.util.ExceptionManager;

/**
 * This exception is thrown by some methods of QueryRuntimeAPI and RuntimeAPI
 * when the variable is not found.
 * @author Marc Blachon, Guillaume Porcher, Charles Souillard, Miguel Valdes, Pierre Vigneras
 */
public class VariableNotFoundException extends BonitaException {

  /**
   * 
   */
  private static final long serialVersionUID = -5432738295806093855L;
  private ProcessInstanceUUID instanceUUID;
  private String activityId;
  private String variableId;
  private ActivityInstanceUUID activityUUID;

  public VariableNotFoundException(final String id, final ProcessInstanceUUID instanceUUID,
      final String activityId, final String variableId) {
  	super(ExceptionManager.getInstance().getIdMessage(id)
  			+ ExceptionManager.getInstance().getMessage("VNFE1", variableId, activityId, instanceUUID));
    this.instanceUUID = instanceUUID;
    this.activityId = activityId;
    this.variableId = variableId;
  }

  public VariableNotFoundException(final String id, final ProcessInstanceUUID instanceUUID, final String variableId) {
  	super(ExceptionManager.getInstance().getIdMessage(id)
  			+ ExceptionManager.getInstance().getMessage("VNFE2", variableId, instanceUUID));
    this.instanceUUID = instanceUUID;
    this.activityId = null;
    this.variableId = variableId;
  }

  public VariableNotFoundException(VariableNotFoundException e) {
    super(e.getMessage());
    this.instanceUUID = e.getInstanceUUID();
    this.activityId = e.getActivityId();
    this.variableId = e.getVariableId();
  }

  public VariableNotFoundException(String id, ActivityInstanceUUID activityUUID,
      String variableId) {
  	super(ExceptionManager.getInstance().getIdMessage(id)
  			+ ExceptionManager.getInstance().getMessage("VNFE3", variableId, activityUUID));
    this.variableId = variableId;
    this.activityUUID = activityUUID;
  }

  public static VariableNotFoundException build(String id, Throwable e) {
    if (!(e instanceof VariableNotFoundException)) {
    	ExceptionManager manager = ExceptionManager.getInstance();
    	String message = manager.getIdMessage(id) + manager.getMessage("VNFE4");
      throw new BonitaInternalException(message, e);
    }
    return new VariableNotFoundException((VariableNotFoundException)e);
  }

  public String getActivityId() {
    return this.activityId;
  }

  public ProcessInstanceUUID getInstanceUUID() {
    return instanceUUID;
  }

  public String getVariableId() {
    return variableId;
  }


  public ActivityInstanceUUID getActivityUUID() {
    return activityUUID;
  }
}
