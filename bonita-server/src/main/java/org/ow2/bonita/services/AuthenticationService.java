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
package org.ow2.bonita.services;

import org.ow2.bonita.facade.exception.UserNotFoundException;
import org.ow2.bonita.identity.auth.BonitaIdentityLoginModule;

/**
 * @author Anthony Birembaut
 *
 */
public interface AuthenticationService {

  /**
   * Check whether a user has admin privileges or not
   * @param username the user's username
   * @return true if the user has admin privileges, false otherwise
   * @throws UserNotFoundException
   */
  boolean isUserAdmin(String username) throws UserNotFoundException;
  
  /**
   * Check some user's credentials
   * This method gets called by {@link BonitaIdentityLoginModule}
   * A proper implementation  of this method is required only if {@link BonitaIdentityLoginModule} is used
   * @param username the user's username
   * @param password the user's password
   * @return true if the credentials are valid, false otherwise
   */
  boolean checkUserCredentials(String username, String password);
  
  /**
   * Check some user's credentials
   * This method gets called by {@link BonitaIdentityLoginModule}
   * A proper implementation  of this method is required only if {@link BonitaIdentityLoginModule} is used
   * @param username the user's username
   * @param passwordHash the user's password hash
   * @return true if the credentials are valid, false otherwise
   */
  boolean checkUserCredentialsWithPasswordHash(String username, String passwordHash);
}
