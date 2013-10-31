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

import org.ow2.bonita.facade.uuid.ParticipantDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.util.BonitaException;
import org.ow2.bonita.util.ExceptionManager;

/**
 * Thrown by methods of the QueryDefinitionAPI if the definition of the participant
 * has not been found.
 * @author Marc Blachon, Guillaume Porcher, Charles Souillard, Miguel Valdes, Pierre Vigneras
 *
 */
public class ParticipantNotFoundException extends BonitaException {

  /**
   * 
   */
  private static final long serialVersionUID = -3384942707469224537L;
  private final String participantId;
  private final ProcessDefinitionUUID processUUID;
  private final ParticipantDefinitionUUID participantUUID;

  public ParticipantNotFoundException(final String id, final String participantId, final ProcessDefinitionUUID processDefinitionUUID) {
    super(ExceptionManager.getInstance().getIdMessage(id)
    		+ ExceptionManager.getInstance().getMessage("PartNFE1", participantId, processDefinitionUUID));
    this.participantId = participantId;
    this.processUUID = processDefinitionUUID;
    this.participantUUID = null;
  }

  public ParticipantNotFoundException(final String id, final ParticipantDefinitionUUID participantDefinitionUUID) {
  	super(ExceptionManager.getInstance().getIdMessage(id)
    		+ ExceptionManager.getInstance().getMessage("PartNFE2", participantDefinitionUUID));
    this.participantUUID = participantDefinitionUUID;
    this.participantId = null;
    this.processUUID = null;
  }

  public ParticipantNotFoundException(ParticipantNotFoundException e) {
    super(e.getMessage());
    this.participantId = e.getParticipantId();
    this.processUUID = e.getProcessUUID();
    this.participantUUID = e.getParticipantUUID();

  }

  public static ParticipantNotFoundException build(final String id, Throwable e) {
    if (!(e instanceof ParticipantNotFoundException)) {
    	ExceptionManager manager = ExceptionManager.getInstance();
    	String message = manager.getIdMessage(id) + manager.getMessage("PartNFE3");
      throw new BonitaInternalException(message, e);
    }
    return new ParticipantNotFoundException((ParticipantNotFoundException)e);
  }
  public String getParticipantId() {
    return this.participantId;
  }
  public ProcessDefinitionUUID getProcessUUID() {
    return processUUID;
  }
  public ParticipantDefinitionUUID getParticipantUUID() {
    return participantUUID;
  }
}
