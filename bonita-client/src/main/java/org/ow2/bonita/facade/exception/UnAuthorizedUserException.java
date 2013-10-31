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
 * Thrown when a user attemps to execute an unauthorized action.
 * @author Marc Blachon, Guillaume Porcher, Charles Souillard, Miguel Valdes, Pierre Vigneras.
 */
public class UnAuthorizedUserException extends BonitaException {
  /**
   * 
   */
  private static final long serialVersionUID = 1043694645994801762L;
  private String instanceUUID;
  private ActivityInstanceUUID taskUUID;
  private String userId;

  /**
   * Constructs an UnAuthorizedUserException for instance.
   * ActivityInstanceUUID is set to null.
   * @param msg the detail message.
   * @param instanceUUID the instance processDefinitionUUID.
   * @param userId the user processDefinitionUUID.
   */
  public UnAuthorizedUserException(String id, String msg, String instanceUUID, String userId) {
  	super(ExceptionManager.getInstance().getIdMessage(id)
  			+ ExceptionManager.getInstance().getMessage("UAUE1", userId, msg, instanceUUID));
    this.instanceUUID = instanceUUID;
    this.userId = userId;
    this.taskUUID = null;
  }

  /**
   * Constructs an UnAuthorizedUserException for task.
   * @param msg the detail message.
   * @param instanceUUID the instance processDefinitionUUID.
   * @param taskUUID the task processDefinitionUUID.
   * @param userId the user processDefinitionUUID.
   */
  public UnAuthorizedUserException(String id, String msg, String instanceUUID, ActivityInstanceUUID taskUUID, String userId) {
  	super(ExceptionManager.getInstance().getIdMessage(id)
  			+ ExceptionManager.getInstance().getMessage("UAUE2", userId, msg, taskUUID, instanceUUID));
    this.instanceUUID = instanceUUID;
    this.taskUUID = taskUUID;
    this.userId = userId;
  }

  public UnAuthorizedUserException(UnAuthorizedUserException e) {
    super(e.getMessage());
    this.instanceUUID = e.getInstanceUUID();
    this.taskUUID = e.getActivityInstanceUUID();
    this.userId = e.getUserId();
  }

  public UnAuthorizedUserException(String id, String msg, ActivityInstanceUUID taskUUID, String userId) {
  	super(ExceptionManager.getInstance().getIdMessage(id)
  			+ ExceptionManager.getInstance().getMessage("UAUE3", userId, msg, taskUUID));
    this.taskUUID = taskUUID;
    this.userId = userId;
  }

  public static UnAuthorizedUserException build(String id, Throwable e) {
    if (!(e instanceof UnAuthorizedUserException)) {
    	ExceptionManager manager = ExceptionManager.getInstance();
    	String message = manager.getIdMessage(id) + manager.getMessage("UAUE4");
      throw new BonitaInternalException(message, e);
    }
    return new UnAuthorizedUserException((UnAuthorizedUserException)e);
  }

  public ActivityInstanceUUID getActivityInstanceUUID() {
    return this.taskUUID;
  }

  public String getInstanceUUID() {
    return this.instanceUUID;
  }

  public String getUserId() {
    return this.userId;
  }
}
