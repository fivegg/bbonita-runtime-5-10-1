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
import org.ow2.bonita.util.BonitaException;
import org.ow2.bonita.util.ExceptionManager;

/**
 * Thrown by methods of RuntimeAPI or QueryRuntimeAPI if the recorded runtime information
 * of the task has not been found.
 * @author Marc Blachon, Guillaume Porcher, Charles Souillard, Miguel Valdes, Pierre Vigneras
 */
public class TaskNotFoundException extends BonitaException {

  /**
   * 
   */
  private static final long serialVersionUID = -3138973316075795539L;
  private final ActivityInstanceUUID taskUUID;

  public TaskNotFoundException(final String id, ActivityInstanceUUID taskUUID) {
  	super(ExceptionManager.getInstance().getIdMessage(id)
  			+ ExceptionManager.getInstance().getMessage("TNFE1", taskUUID));
    this.taskUUID = taskUUID;
  }

  public TaskNotFoundException(TaskNotFoundException e) {
    super(e.getMessage());
    this.taskUUID = e.getActivityInstanceUUID();
  }

  public static TaskNotFoundException build(String id, Throwable e) {
    if (!(e instanceof TaskNotFoundException)) {
    	ExceptionManager manager = ExceptionManager.getInstance();
    	String message = manager.getIdMessage(id) + manager.getMessage("TNFE2");
      throw new BonitaInternalException(message, e);
    }
    return new TaskNotFoundException((TaskNotFoundException)e);
  }

  public ActivityInstanceUUID getActivityInstanceUUID() {
    return this.taskUUID;
  }
}
