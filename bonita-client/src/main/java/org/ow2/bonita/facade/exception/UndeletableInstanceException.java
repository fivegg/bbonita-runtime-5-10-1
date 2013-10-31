/**
 * Copyright (C) 2006  Bull S. A. S.
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

import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.util.BonitaException;
import org.ow2.bonita.util.ExceptionManager;

/**
 * Thrown by some methods of ManagementAPI and RuntimeAPI when an instance cannot be deleted<br>
 * (for instance: case the instance has a parent instance or the instance has not been found).
 */
public class UndeletableInstanceException extends BonitaException {

  /**
   * 
   */
  private static final long serialVersionUID = -5253409335368750503L;
  private ProcessInstanceUUID processInstanceUUID;
  private ProcessInstanceUUID parentInstanceUUID;

  public UndeletableInstanceException(String id,
      ProcessInstanceUUID processInstanceUUID,
      ProcessInstanceUUID parentInstanceUUID) {
  	super(ExceptionManager.getInstance().getIdMessage(id)
  			+ ExceptionManager.getInstance().getMessage("UndelIE1", processInstanceUUID, parentInstanceUUID));
    this.processInstanceUUID = processInstanceUUID;
    this.parentInstanceUUID = parentInstanceUUID;
  }

  public ProcessInstanceUUID getProcessInstanceUUID() {
    return processInstanceUUID;
  }

  public ProcessInstanceUUID getParentInstanceUUID() {
    return parentInstanceUUID;
  }
}


