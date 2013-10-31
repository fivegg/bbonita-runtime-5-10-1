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

import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.util.BonitaException;
import org.ow2.bonita.util.ExceptionManager;

/**
 * Thrown by methods of RuntimeAPI or QueryDefinitionAPI if the recorded runtime information
 * of the process has not been found.
 *
 * @author Marc Blachon, Guillaume Porcher, Charles Souillard, Miguel Valdes, Pierre Vigneras
 *
 */
public class ProcessNotFoundException extends BonitaException {

  /**
   * 
   */
  private static final long serialVersionUID = -264146472484882763L;
  private final String processId;
  private final String version;
  private final ProcessDefinitionUUID processUUID;

  public ProcessNotFoundException(final String id, final String processId) {
  	super(ExceptionManager.getInstance().getIdMessage(id)
  			+ ExceptionManager.getInstance().getMessage("PNFE1", processId));
    this.processId = processId;
    this.processUUID = null;
    this.version = null;
  }

  public ProcessNotFoundException(final String id, final String processId, final String version) {
  	super(ExceptionManager.getInstance().getIdMessage(id)
  			+ ExceptionManager.getInstance().getMessage("PNFE2", processId));
    this.processId = processId;
    this.version = version;
    this.processUUID = null;
  }
  public ProcessNotFoundException(final String id, final ProcessDefinitionUUID processUUID) {
  	super(ExceptionManager.getInstance().getIdMessage(id)
  			+ ExceptionManager.getInstance().getMessage("PNFE3", processUUID));
    this.processUUID = processUUID;
    this.processId = null;
    this.version = null;
  }

  public String getProcessId() {
    return this.processId;
  }
  public ProcessDefinitionUUID getProcessUUID() {
    return processUUID;
  }
  public String getVersion() {
    return version;
  }
}
