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
package org.ow2.bonita.services.impl;

import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ow2.bonita.facade.IdentityAPI;
import org.ow2.bonita.facade.exception.UserNotFoundException;
import org.ow2.bonita.facade.identity.Membership;
import org.ow2.bonita.facade.identity.impl.UserImpl;
import org.ow2.bonita.persistence.IdentityDbSession;
import org.ow2.bonita.services.AuthenticationService;
import org.ow2.bonita.util.EnvTool;
import org.ow2.bonita.util.Misc;

/**
 * @author Anthony Birembaut
 *
 */
public class DbAuthentication implements AuthenticationService {

  private static final Logger LOG = Logger.getLogger(DbAuthentication.class.getName());
  
  private final String persistenceServiceName;

  public DbAuthentication(final String persistenceServiceName) {
    super();
    this.persistenceServiceName = persistenceServiceName;
  }

  protected IdentityDbSession getDbSession() {
    return EnvTool.getIdentityDbSession(persistenceServiceName);
  }
  
  @Override
  public boolean isUserAdmin(final String username) {
    try {
      final UserImpl user = getDbSession().findUserByUsername(username);
      if (user == null) {
        throw new UserNotFoundException("bsi_DBA_1", username);
      }
      final Set<Membership> userMemberships = user.getMemberships();
      for (final Membership membership : userMemberships) {
        if (IdentityAPI.ADMIN_ROLE_NAME.equals(membership.getRole().getName()) 
            && IdentityAPI.DEFAULT_GROUP_NAME.equals(membership.getGroup().getName())) {
          return true;
        }
      }
      return false;
    } catch (final UserNotFoundException e) {
      LOG.log(Level.SEVERE, e.getMessage(), e.getCause());
      return false;
    }
  }

  @Override
  public boolean checkUserCredentials(final String username, final String password) {
    final String passwordHash = Misc.hash(password);
    final UserImpl user = getDbSession().findUserByUsername(username);
    if (user != null) {
      //BUG 11848 on SQL Server
      if (!username.equals(user.getUsername())) {
        return false;
      }
      final String dbPassword = user.getPassword();
      if (passwordHash == null && (dbPassword == null || dbPassword.equals(Misc.hash(""))) 
            || (passwordHash != null && passwordHash.equals(dbPassword))) {
        return true;
      }
    }
    return false;
  }

	@Override
  public boolean checkUserCredentialsWithPasswordHash(final String username,
			final String passwordHash) {		
    final UserImpl user = getDbSession().findUserByUsername(username);
    if (user != null) {
      final String dbPassword = user.getPassword();
      if (passwordHash == null && (dbPassword == null || dbPassword.equals(Misc.hash(""))) 
            || (passwordHash != null && passwordHash.equals(dbPassword))) {
        return true;
      }
    }    
		return false;
	}

}
