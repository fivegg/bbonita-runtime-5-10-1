/**
 * Copyright (C) 2010  BonitaSoft S.A.
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
package org.ow2.bonita.identity.auth;

import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginException;

import org.ow2.bonita.util.ExceptionManager;
import org.ow2.bonita.util.Misc;

/**
 * 
 * @author Elias Ricken de Medeiros
 *
 */
public class BonitaRESTLoginModule extends BonitaRemoteLoginModule {

  public static final String REST_USER_OPTION_NAME = "restUser";
  public static final String REST_PSWD_OPTION_NAME = "restPassword";
  
  private String restId;
  private String restPswd;
  
  public void initialize(final Subject subject, final CallbackHandler callbackHandler,
      final Map<String, ?> sharedState, final Map<String, ?> options) {
    super.initialize(subject, callbackHandler, sharedState, options);
    this.restId = (String) options.get(REST_USER_OPTION_NAME);
    this.restPswd = (String) options.get(REST_PSWD_OPTION_NAME);
    
  }
  
  public boolean login() throws LoginException {
    if (this.restId == null) {
      String message = ExceptionManager.getInstance().getFullMessage(
          "bi_MOE_1", REST_USER_OPTION_NAME);
      throw new MissingOptionException("bi_MOE_1", message);
    }
    
    if (this.restPswd == null) {
      String message = ExceptionManager.getInstance().getFullMessage(
          "bi_MOE_1", REST_PSWD_OPTION_NAME);
      throw new MissingOptionException("bi_MOE_1", message);
    }
    
    return super.login();
  }
  
  /**
   * Method to commit the authentication process (phase 2). This method is
   * called if the LoginContext's overall authentication succeeded (the relevant
   * REQUIRED, REQUISITE, SUFFICIENT and OPTIONAL LoginModules succeeded). If
   * this LoginModule's own authentication attempt succeeded (checked by
   * retrieving the private state saved by the login method), then this method
   * associates relevant Principals and Credentials with the Subject located in
   * the LoginModule. If this LoginModule's own authentication attempted failed,
   * then this method removes/destroys any state that was originally saved.
   * 
   * @return true if this method succeeded, or false if this LoginModule should
   *         be ignored.
   * @throws LoginException
   *           if the commit fails
   */
  public boolean commit() throws LoginException {
    super.commit();
    RESTUserOwner.setUser(this.restId);
    PasswordOwner.setPassword(Misc.hash(this.restPswd));
    return true;
  }

  /**
   * Method to abort the authentication process (phase 2). This method is called
   * if the LoginContext's overall authentication failed. (the relevant
   * REQUIRED, REQUISITE, SUFFICIENT and OPTIONAL LoginModules did not succeed).
   * If this LoginModule's own authentication attempt succeeded (checked by
   * retrieving the private state saved by the login method), then this method
   * cleans up any state that was originally saved.
   * 
   * @return true if this method succeeded, or false if this LoginModule should
   *         be ignored.
   * @throws LoginException
   *           if the abort fails
   */
  public boolean abort() throws LoginException {
    super.abort();
    PasswordOwner.setPassword(null);
    RESTUserOwner.setUser(null);
    return true;
  }

  /**
   * Method which logs out a Subject. An implementation of this method might
   * remove/destroy a Subject's Principals and Credentials.
   * 
   * @return true if this method succeeded, or false if this LoginModule should
   *         be ignored.
   * @throws LoginException
   *           if the logout fails
   */
  public boolean logout() throws LoginException {
    super.logout();
    PasswordOwner.setPassword(null);
    RESTUserOwner.setUser(null);
    return true;
  }


}
