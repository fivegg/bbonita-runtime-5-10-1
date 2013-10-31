/**
 * Copyright (C) 2010-2012 BonitaSoft S.A.
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
import java.util.StringTokenizer;

import javax.security.auth.Destroyable;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import org.ow2.bonita.util.ExceptionManager;
import org.ow2.bonita.util.Misc;

/**
 * @author Elias Ricken de Medeiros
 * 
 */
public class BonitaRESTServerLoginModule implements LoginModule {

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
  private static final String LOGINS_OPTION_NAME = "logins";
  private static final String PASSWORDS_OPTION_NAME = "passwords";
  private static final String ROLES_OPTION_NAME = "roles";

  private Subject subject = null;
  private CallbackHandler callbackHandler = null;
  protected Map<String, Object> sharedState;
  private Map<String, Object> options = null;
  private boolean debug = false;

  private String id;
  private String role;
  private List<String> logins;
  private List<String> passwords;
  private List<String> roles;

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

    this.options = (Map<String, Object>) options;
  }

  @Override
  public boolean login() throws LoginException {
    if (this.debug) {
      System.err.println("[" + BonitaRESTServerLoginModule.class.getName() + "] login() - preparing - step 1");
    }

    processUsersPasswordsAndRoles();

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
          System.err.println("[" + BonitaRESTServerLoginModule.class.getName() + "] login() - callback - step 2");
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
        System.err.println("[" + BonitaRESTServerLoginModule.class.getName() + "] login() - authenticating - step 3");
      }

      final int i = logins.indexOf(name);

      if (i != -1 && (this.passwords.get(i).equals(password) || this.passwords.get(i).equals(Misc.hash(password)))) {
        this.id = name;
        this.role = this.roles.get(i);
      }
      if (this.debug) {
        System.err.println("[" + BonitaRESTServerLoginModule.class.getName() + "] login() - storing data - step 4");
      }
      this.sharedState.put(JAVAX_SECURITY_AUTH_LOGIN_NAME, name);
      this.sharedState.put(JAVAX_SECURITY_AUTH_LOGIN_PASSWORD, password);
      if (this.debug) {
        System.err.println("[" + BonitaRESTServerLoginModule.class.getName() + "] login() - returning - step 5");
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

  private void processUsersPasswordsAndRoles() throws BadLoginOptionException, MissingOptionException {
    if (this.logins == null) {
      final String loginList = (String) this.options.get(BonitaRESTServerLoginModule.LOGINS_OPTION_NAME);
      final String passwordList = (String) this.options.get(BonitaRESTServerLoginModule.PASSWORDS_OPTION_NAME);
      final String rolesList = (String) this.options.get(BonitaRESTServerLoginModule.ROLES_OPTION_NAME);
      if (loginList == null) {
        final String message = ExceptionManager.getInstance().getFullMessage("bi_PLM_1",
            BonitaRESTServerLoginModule.LOGINS_OPTION_NAME);
        throw new MissingOptionException("bi_PLM_1", message);
      }
      if (passwordList == null) {
        final String message = ExceptionManager.getInstance().getFullMessage("bi_PLM_2",
            BonitaRESTServerLoginModule.PASSWORDS_OPTION_NAME);
        throw new MissingOptionException("bi_PLM_2", message);
      }

      if (rolesList == null) {
        final String message = ExceptionManager.getInstance().getFullMessage("bi_PLM_7",
            BonitaRESTServerLoginModule.ROLES_OPTION_NAME);
        throw new MissingOptionException("bi_PLM_7", message);
      }
      final StringTokenizer loginTokenizer = new StringTokenizer(loginList, ",");
      final StringTokenizer passwordTokenizer = new StringTokenizer(passwordList, ",");
      final StringTokenizer roleTokenizer = new StringTokenizer(rolesList, ",");
      if (loginTokenizer.countTokens() != passwordTokenizer.countTokens()) {
        final String message = ExceptionManager.getInstance().getFullMessage("bi_PLM_8");
        throw new BadLoginOptionException(message);
      }
      if (loginTokenizer.countTokens() != roleTokenizer.countTokens()) {
        final String message = ExceptionManager.getInstance().getFullMessage("bi_PLM_8");
        throw new BadLoginOptionException(message);
      }

      this.logins = new ArrayList<String>();
      this.passwords = new ArrayList<String>();
      this.roles = new ArrayList<String>();

      while (loginTokenizer.hasMoreTokens()) {
        final String name = loginTokenizer.nextToken().trim();
        if (this.logins.contains(name)) {
          final String message = ExceptionManager.getInstance().getFullMessage("bi_PLM_4", name);
          throw new BadLoginOptionException(message);
        }

        final String password = passwordTokenizer.nextToken().trim();
        final String role = roleTokenizer.nextToken().trim();

        this.logins.add(name);
        this.passwords.add(Misc.hash(password));

        this.roles.add(role);
      }
    }
  }

  @Override
  public boolean commit() throws LoginException {
    if (this.id == null) {
      final String message = ExceptionManager.getInstance().getFullMessage("bi_PLM_6");
      throw new FailedLoginException(message);
    }

    final Set<Principal> principals = this.subject.getPrincipals();
    principals.add(new BonitaPrincipal(this.id));
    principals.add(new BonitaPrincipalRole(role));

    return true;
  }

  @Override
  public boolean abort() throws LoginException {
    if (this.debug) {
      System.err.println("[" + BonitaRESTServerLoginModule.class.getName() + "] abort()");
    }
    if (this.id == null) {
      return false;
    }
    this.subject = null;
    this.id = null;
    this.role = null;
    return true;
  }

  @Override
  public boolean logout() throws LoginException {
    if (this.id != null) {
      if (this.debug) {
        System.err.println("[" + BonitaRESTServerLoginModule.class.getName() + "] logout() - removing principals");
      }
      // Remove only principals added by our commit method
      final Set<Principal> principals = new HashSet<Principal>(this.subject.getPrincipals());
      for (final Principal p : principals) {
        if (p instanceof BonitaPrincipal) {
          if (this.debug) {
            System.err.println("[" + BonitaRESTServerLoginModule.class.getName() + "] logout() - removing principal: "
                + p);
          }
          this.subject.getPrincipals().remove(p);
        }

        if (p instanceof BonitaPrincipalRole) {
          if (this.debug) {
            System.err.println("[" + BonitaRESTServerLoginModule.class.getName() + "] logout() - removing role: " + p);
          }
          this.subject.getPrincipals().remove(p);
        }
      }

      if (this.debug) {
        System.err.println("[" + BonitaRESTServerLoginModule.class.getName()
            + "] logout() - destroying/removing credentials");
      }
      // Remove/destroy only credentials added by our commit method
      final Set<Object> credentials = new HashSet<Object>(this.subject.getPublicCredentials());
      for (final Object o : credentials) {
        if (o instanceof Destroyable && this.debug) {
          System.err.println("[" + BonitaRESTServerLoginModule.class.getName() + "] logout() - destroying credential: "
              + o);
        }
        if (!this.subject.isReadOnly()) {
          if (this.debug) {
            System.err.println("[" + BonitaRESTServerLoginModule.class.getName() + "] logout() - removing credential: "
                + o);
          }
          this.subject.getPublicCredentials().remove(o);
        }
      }
    }
    return true;
  }

}
