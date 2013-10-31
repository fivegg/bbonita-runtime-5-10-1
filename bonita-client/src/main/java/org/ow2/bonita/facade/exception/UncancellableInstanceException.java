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

import org.ow2.bonita.facade.runtime.InstanceState;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.util.BonitaException;
import org.ow2.bonita.util.ExceptionManager;

/**
 * Thrown by some methods of ManagementAPI and RuntimeAPI when an instance cannot be cancelled<br>
 * (for instance: case the instance has a parent instance or the instance has not been found).
 */
public class UncancellableInstanceException extends BonitaException {

  /**
   * 
   */
  private static final long serialVersionUID = 7889974646865004308L;
  private final ProcessInstanceUUID processInstanceUUID;
  private final ProcessInstanceUUID parentInstanceUUID;
  private final InstanceState processInstanceState;

  public UncancellableInstanceException(final String id,
      final ProcessInstanceUUID processInstanceUUID,
      final ProcessInstanceUUID parentInstanceUUID,
      final InstanceState processInstanceState) {
  	super(ExceptionManager.getInstance().getIdMessage(id)
  			+ (parentInstanceUUID != null ?
  					ExceptionManager.getInstance().getMessage("UIE1",processInstanceUUID, parentInstanceUUID)
  					: ExceptionManager.getInstance().getMessage("UIE2",processInstanceUUID, processInstanceState)));
    this.processInstanceUUID = processInstanceUUID;
    this.parentInstanceUUID = parentInstanceUUID;
    this.processInstanceState = processInstanceState;
  }

  public ProcessInstanceUUID getProcessInstanceUUID() {
    return this.processInstanceUUID;
  }

  public ProcessInstanceUUID getParentInstanceUUID() {
    return this.parentInstanceUUID;
  }

  public InstanceState getProcessInstanceState() {
    return this.processInstanceState;
  }
}
