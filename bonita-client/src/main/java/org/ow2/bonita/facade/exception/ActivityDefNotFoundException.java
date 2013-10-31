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

import org.ow2.bonita.facade.uuid.ActivityDefinitionUUID;
import org.ow2.bonita.util.BonitaException;
import org.ow2.bonita.util.ExceptionManager;

/**
 * Thrown by methods of the QueryDefinitionAPI when the activity definition is not found.
 * @author Marc Blachon, Guillaume Porcher, Charles Souillard, Miguel Valdes, Pierre Vigneras
 */
public class ActivityDefNotFoundException extends BonitaException {

  private static final long serialVersionUID = 1L;
  private final String processId;
  private final String activityId;
  private final ActivityDefinitionUUID activityUUID;

  public ActivityDefNotFoundException(final String id, final String processId,
      final String activityId) {
    super(ExceptionManager.getInstance().getIdMessage(id)
    		+ ExceptionManager.getInstance().getMessage("ADNFE1", activityId, processId));
    this.processId = processId;
    this.activityId = activityId;
    this.activityUUID = null;
  }

  public ActivityDefNotFoundException(final String id, final ActivityDefinitionUUID activityDefinitionUUID) {
    super(ExceptionManager.getInstance().getIdMessage(id)
    		+ ExceptionManager.getInstance().getMessage("ADNFE2", activityDefinitionUUID));
    this.activityUUID = activityDefinitionUUID;
    this.processId = null;
    this.activityId = null;
  }

  public ActivityDefNotFoundException(ActivityDefNotFoundException e) {
    super(e.getMessage());
    this.activityUUID = e.getActivityUUID();
    this.processId = e.getProcessId();
    this.activityId = e.getActivityId();
  }

  public static ActivityDefNotFoundException build(final String id, Throwable e) {
    if (!(e instanceof ActivityDefNotFoundException)) {
    	ExceptionManager manager = ExceptionManager.getInstance();
    	
    	String message = manager.getIdMessage(id) + manager.getMessage("ADNFE3");
      throw new BonitaInternalException(message, e);
    }
    return new ActivityDefNotFoundException((ActivityDefNotFoundException)e);
  }

  public String getActivityId() {
    return this.activityId;
  }

  public String getProcessId() {
    return processId;
  }

  public ActivityDefinitionUUID getActivityUUID() {
    return activityUUID;
  }

}
