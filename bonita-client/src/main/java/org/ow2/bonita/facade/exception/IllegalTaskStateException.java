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

import java.util.Set;

import org.ow2.bonita.facade.runtime.ActivityState;
import org.ow2.bonita.facade.uuid.ActivityInstanceUUID;
import org.ow2.bonita.util.BonitaException;
import org.ow2.bonita.util.ExceptionManager;

/**
 * Thrown if a bad state of the task is detected by the methods dedicated to
 * change the state of the task within the RuntimeAPI.
 * @author Marc Blachon, Guillaume Porcher, Charles Souillard, Miguel Valdes, Pierre Vigneras
 */
public class IllegalTaskStateException extends BonitaException {

  /**
   * 
   */
  private static final long serialVersionUID = 8686470271658322546L;
  private final ActivityInstanceUUID taskUUID;
  private final Set<ActivityState> expectedStates;
  private final ActivityState currentState;

  /**
   * Constructs an IllegalTaskStateTException.
   * @param msg the detail message.
   * @param taskUUID the task Id.
   * @param expectedStates the expected states.
   * @param currentState the current state.
   */
  public IllegalTaskStateException(String id, String msg, ActivityInstanceUUID taskUUID, Set<ActivityState> expectedStates, ActivityState currentState) {
    super(ExceptionManager.getInstance().getIdMessage(id)
    		+ ExceptionManager.getInstance().getMessage("ITSE1", msg, taskUUID, expectedStates, currentState));
    this.taskUUID = taskUUID;
    this.expectedStates = expectedStates;
    this.currentState = currentState;
  }

  public IllegalTaskStateException(IllegalTaskStateException e) {
    super(e.getMessage());
    this.taskUUID = e.getActivityInstanceUUID();
    this.expectedStates = e.getExpectedStates();
    this.currentState = e.getCurrentState();
  }

  public static IllegalTaskStateException build(String id, Throwable e) {
    if (!(e instanceof IllegalTaskStateException)) {
    	ExceptionManager manager = ExceptionManager.getInstance();
    	String message = manager.getIdMessage(id) + manager.getMessage("ITSE2");
      throw new BonitaInternalException(message, e);
    }
    return new IllegalTaskStateException((IllegalTaskStateException)e);
  }

  public ActivityInstanceUUID getActivityInstanceUUID() {
    return this.taskUUID;
  }

  public Set<ActivityState> getExpectedStates() {
    return this.expectedStates;
  }

  public ActivityState getCurrentState() {
    return this.currentState;
  }
}
