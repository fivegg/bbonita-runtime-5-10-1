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
package org.ow2.bonita.identity.auth;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.security.auth.Destroyable;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import org.ow2.bonita.facade.ManagementAPI;
import org.ow2.bonita.util.AccessorUtil;
import org.ow2.bonita.util.BonitaConstants;
import org.ow2.bonita.util.ExceptionManager;
import org.ow2.bonita.util.Misc;

/**
 * This {@link LoginModule} is used to verify a user identity against Bonita authentication service.
 * 
 * @author Anthony Birembaut
 */
public class BonitaIdentityLoginModule implements LoginModule {

  private static final String NAME_PROMPT = "Name: ";
  private static final String PASSWORD_PROMPT = "Password: ";

  private static final String JAVAX_SECURITY_AUTH_LOGIN_PASSWORD = "javax.security.auth.login.password";
  private static final String JAVAX_SECURITY_AUTH_LOGIN_NAME = "javax.security.auth.login.name";

  /**
   * Property key for the debug flag. Defined to be "debug".
   * 
   * Property Value. If set, should be either "true" or "false". Default is "false".
   */
  public static final String DEBUG_OPTION_NAME = "debug";
  public static final String DOMAIN_OPTION_NAME = "domain";

  private Subject subject = null;
  private CallbackHandler callbackHandler = null;
  private Map<String, Object> sharedState;
  private boolean debug = false;
  private String domain;

  private String id;

  /**
   * Initialize this LoginModule. This method is called by the LoginContext after this LoginModule has been
   * instantiated. The purpose of this method is to initialize this LoginModule with the relevant information. If this
   * LoginModule does not understand any of the data stored in sharedState or options parameters, they can be ignored.
   * 
   * @param subject the Subject to be authenticated.
   * @param callbackHandler a CallbackHandler for communicating with the end user (prompting for usernames and
   *          passwords, for example).
   * @param sharedState state shared with other configured LoginModules.
   * @param options options specified in the login Configuration for this particular LoginModule.
   */
  @Override
  @SuppressWarnings("unchecked")
  public void initialize(final Subject subject, final CallbackHandler callbackHandler,
      final Map<String, ?> sharedState, final Map<String, ?> options) {

    Misc.checkArgsNotNull(subject, callbackHandler, sharedState, options);

    this.subject = subject;
    this.callbackHandler = callbackHandler;
    this.sharedState = (Map<String, Object>) sharedState;
    final String debugFlag = (String) options.get(DEBUG_OPTION_NAME);
    if (debugFlag != null) {
      this.debug = Boolean.valueOf(debugFlag);
    }
    this.domain = (String) options.get(DOMAIN_OPTION_NAME);
    if (this.domain == null) {
      this.domain = BonitaConstants.DEFAULT_DOMAIN;
    }
  }

  /**
   * Method to authenticate a Subject (phase 1). The implementation of this method authenticates a Subject. For example,
   * it may prompt for Subject information such as a username and password and then attempt to verify the password. This
   * method saves the result of the authentication attempt as private state within the LoginModule.
   * 
   * @return true if the authentication succeeded, or false if this LoginModule should be ignored.
   * @throws LoginException if the authentication fails
   */
  @Override
  public boolean login() throws LoginException {
    if (this.debug) {
      System.err.println("[" + BonitaIdentityLoginModule.class.getName() + "] login() - preparing - step 1");
    }
    try {
      String name = (String) this.sharedState.get(JAVAX_SECURITY_AUTH_LOGIN_NAME);
      String password = (String) this.sharedState.get(JAVAX_SECURITY_AUTH_LOGIN_PASSWORD);
      final List<Callback> callbacks = new ArrayList<Callback>();
      final NameCallback nameCallback = new NameCallback(NAME_PROMPT);
      final PasswordCallback passwordCallback = new PasswordCallback(PASSWORD_PROMPT, false);
      if (name == null) {
        callbacks.add(nameCallback);
      }
      if (password == null) {
        callbacks.add(passwordCallback);
      }
      if (!callbacks.isEmpty()) {
        if (this.debug) {
          System.err.println("[" + BonitaIdentityLoginModule.class.getName() + "] login() - callback - step 2");
        }
        this.callbackHandler.handle(callbacks.toArray(new Callback[0]));
        if (name == null) {
          name = nameCallback.getName();
        }
        if (password == null) {
          password = new String(passwordCallback.getPassword());
          passwordCallback.clearPassword();
        }
      }
      if (this.debug) {
        System.err.println("[" + BonitaIdentityLoginModule.class.getName() + "] login() - authenticating - step 3");
      }
      if (name != null) {
        DomainOwner.setDomain(domain);
        final ManagementAPI managementAPI = AccessorUtil.getManagementAPI();
        if (managementAPI.checkUserCredentials(name, password)) {
          this.id = name;
        }
        DomainOwner.setDomain(null);
      }
      if (this.debug) {
        System.err.println("[" + BonitaIdentityLoginModule.class.getName() + "] login() - storing data - step 4");
      }
      this.sharedState.put(JAVAX_SECURITY_AUTH_LOGIN_NAME, name);
      this.sharedState.put(JAVAX_SECURITY_AUTH_LOGIN_PASSWORD, password);
      if (this.debug) {
        System.err.println("[" + BonitaIdentityLoginModule.class.getName() + "] login() - returning - step 5");
      }
      if (this.id == null) {
        final String message = ExceptionManager.getInstance().getFullMessage("bi_LSLM_1");
        throw new FailedLoginException(message);
      }
      return true;
    } catch (final Exception e) {
      e.printStackTrace();
      final LoginException le = new LoginException();
      le.initCause(e);
      throw le;
    }
  }

  /**
   * Method to commit the authentication process (phase 2). This method is called if the LoginContext's overall
   * authentication succeeded (the relevant REQUIRED, REQUISITE, SUFFICIENT and OPTIONAL LoginModules succeeded). If
   * this LoginModule's own authentication attempt succeeded (checked by retrieving the private state saved by the login
   * method), then this method associates relevant Principals and Credentials with the Subject located in the
   * LoginModule. If this LoginModule's own authentication attempted failed, then this method removes/destroys any state
   * that was originally saved.
   * 
   * @return true if this method succeeded, or false if this LoginModule should be ignored.
   * @throws LoginException if the commit fails
   */
  @Override
  public boolean commit() throws LoginException {
    if (this.id == null) {
      final String message = ExceptionManager.getInstance().getFullMessage("bi_PLM_2");
      throw new FailedLoginException(message);
    }
    final Set<Principal> principals = this.subject.getPrincipals();
    principals.add(new BonitaPrincipal(this.id));
    UserOwner.setUser(null);
    DomainOwner.setDomain(null);
    return true;
  }

  /**
   * Method to abort the authentication process (phase 2). This method is called if the LoginContext's overall
   * authentication failed. (the relevant REQUIRED, REQUISITE, SUFFICIENT and OPTIONAL LoginModules did not succeed). If
   * this LoginModule's own authentication attempt succeeded (checked by retrieving the private state saved by the login
   * method), then this method cleans up any state that was originally saved.
   * 
   * @return true if this method succeeded, or false if this LoginModule should be ignored.
   * @throws LoginException if the abort fails
   */
  @Override
  public boolean abort() throws LoginException {
    if (this.debug) {
      System.err.println("[" + BonitaIdentityLoginModule.class.getName() + "] abort()");
    }
    if (this.id == null) {
      return false;
    }
    this.subject = null;
    this.id = null;
    UserOwner.setUser(null);
    DomainOwner.setDomain(null);
    return true;
  }

  /**
   * Method which logs out a Subject. An implementation of this method might remove/destroy a Subject's Principals and
   * Credentials.
   * 
   * @return true if this method succeeded, or false if this LoginModule should be ignored.
   * @throws LoginException if the logout fails
   */
  @Override
  public boolean logout() throws LoginException {
    if (this.id != null) {
      if (this.debug) {
        System.err.println("[" + BonitaIdentityLoginModule.class.getName() + "] logout() - removing principals");
      }
      // Remove only principals added by our commit method
      final Set<Principal> principals = new HashSet<Principal>(this.subject.getPrincipals());
      for (final Principal p : principals) {
        if (p instanceof BonitaPrincipal) {
          if (this.debug) {
            System.err.println("[" + BonitaIdentityLoginModule.class.getName() + "] logout() - removing principal: "
                + p);
          }
          this.subject.getPrincipals().remove(p);
        }
      }
      UserOwner.setUser(null);
      DomainOwner.setDomain(null);
      if (this.debug) {
        System.err.println("[" + BonitaIdentityLoginModule.class.getName()
            + "] logout() - destroying/removing credentials");
      }
      // Remove/destroy only credentials added by our commit method
      final Set<Object> credentials = new HashSet<Object>(this.subject.getPublicCredentials());
      for (final Object o : credentials) {
        if (o instanceof Destroyable && this.debug) {
          System.err.println("[" + BonitaIdentityLoginModule.class.getName() + "] logout() - destroying credential: "
              + o);
          // Bug: only from this module !!
          // ((Destroyable) o).destroy();
        }
        if (!this.subject.isReadOnly()) {
          if (this.debug) {
            System.err.println("[" + BonitaIdentityLoginModule.class.getName() + "] logout() - removing credential: "
                + o);
          }
          this.subject.getPublicCredentials().remove(o);
        }
      }
    }
    return true;
  }

}
