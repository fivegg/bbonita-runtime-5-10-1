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
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.util.BonitaException;
import org.ow2.bonita.util.ExceptionManager;

/**
 * Thrown by methods of QueryDefinitionAPI, QueryRuntimeAPI, RuntimeAPI if
 * the runtime information of the recorded activity is not found.
 * @author Marc Blachon, Guillaume Porcher, Charles Souillard, Miguel Valdes, Pierre Vigneras
 */
public class ActivityNotFoundException extends BonitaException {

  private static final long serialVersionUID = -7298375846514108890L;
  private ProcessInstanceUUID instanceUUID;
  private String activityId;
  private ProcessDefinitionUUID processUUID;
  private ActivityInstanceUUID activityUUID;
  private String iterationId;

  public ActivityNotFoundException(final String id, final ProcessInstanceUUID instanceUUID,
      final String activityId) {
    super(ExceptionManager.getInstance().getIdMessage(id)
    		+ ExceptionManager.getInstance().getMessage("ANFE1", activityId, instanceUUID));
    this.instanceUUID = instanceUUID;
    this.activityId = activityId;
    this.iterationId = null;
    this.processUUID = null;
  }

  public ActivityNotFoundException(final String id, final ProcessInstanceUUID instanceUUID,
      final String activityId, final String iterationId) {
  	super(ExceptionManager.getInstance().getIdMessage(id)
    		+ ExceptionManager.getInstance().getMessage("ANFE2", activityId, instanceUUID, iterationId));
    this.instanceUUID = instanceUUID;
    this.activityId = activityId;
    this.iterationId = iterationId;
    this.processUUID = null;
  }

  public ActivityNotFoundException(final String id, ProcessDefinitionUUID processId,
      String activityId) {
  	super(ExceptionManager.getInstance().getIdMessage(id)
    		+ ExceptionManager.getInstance().getMessage("ANFE3", activityId, processId));
    this.processUUID = processId;
    this.activityId = activityId;
    this.iterationId = null;
    this.instanceUUID = null;
  }

  public ActivityNotFoundException(final String id, ActivityInstanceUUID activityUUID) {
  	super(ExceptionManager.getInstance().getIdMessage(id)
    		+ ExceptionManager.getInstance().getMessage("ANFE4", activityUUID));
    this.activityUUID = activityUUID;
  }

  public String getActivityId() {
    return this.activityId;
  }

  public String getIterationId() {
    return this.iterationId;
  }

  public ProcessInstanceUUID getInstanceUUID() {
    return instanceUUID;
  }

  public ProcessDefinitionUUID getProcessUUID() {
    return this.processUUID;
  }


  public ActivityInstanceUUID getActivityUUID() {
    return activityUUID;
  }
}
