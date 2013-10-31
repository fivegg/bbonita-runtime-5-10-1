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
 * 
 * Modified by Matthieu Chaffotte - BonitaSoft
 **/
package org.ow2.bonita.facade.exception;

import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.util.BonitaException;
import org.ow2.bonita.util.ExceptionManager;

/**
 * Thrown by some methods of the QueryRuntimeAPI and the RuntimeAPI if recorded runtime information for the instance has
 * not been found.
 * 
 * @author Marc Blachon, Guillaume Porcher, Charles Souillard, Miguel Valdes, Pierre Vigneras
 */
public class InstanceNotFoundException extends BonitaException {

  private static final long serialVersionUID = -925582175189740596L;

  private final ProcessInstanceUUID instanceUUID;

  public InstanceNotFoundException(final String id, final ProcessInstanceUUID instanceUUID) {
    super(ExceptionManager.getInstance().getIdMessage(id)
        + ExceptionManager.getInstance().getMessage("INFE1", instanceUUID));
    this.instanceUUID = instanceUUID;
  }

  public InstanceNotFoundException(final InstanceNotFoundException e) {
    super(e.getMessage());
    this.instanceUUID = e.getInstanceUUID();
  }

  public static InstanceNotFoundException build(final String id, final Throwable e) {
    if (!(e instanceof InstanceNotFoundException)) {
      final ExceptionManager manager = ExceptionManager.getInstance();
      final String message = manager.getIdMessage(id) + manager.getMessage("INFE2");
      throw new BonitaInternalException(message, e);
    }
    return new InstanceNotFoundException((InstanceNotFoundException) e);
  }

  public ProcessInstanceUUID getInstanceUUID() {
    return this.instanceUUID;
  }

}
