/**
 * Copyright (C) 2009  BonitaSoft S.A.
 * BonitaSoft, 31 rue Gustave Eiffel - 38000 Grenoble
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

import org.ow2.bonita.util.BonitaException;
import org.ow2.bonita.util.ExceptionManager;

/**
 * Exception thrown when no user if found
 * @author Anthony Birembaut
 *
 */
public class UserNotFoundException extends BonitaException {
  
  /**
   * UID
   */
  private static final long serialVersionUID = 1029349235147804941L;
  private final String userId;
  
  public UserNotFoundException(final String id, final String userId) {
    super(ExceptionManager.getInstance().getIdMessage(id)
        + ExceptionManager.getInstance().getMessage("UNFE", userId));
    this.userId = userId;
  }
  
  public UserNotFoundException(UserNotFoundException e) {
    super(e.getMessage());
    this.userId = e.getUserId();
  }

  public String getUserId() {
    return this.userId;
  }
}
