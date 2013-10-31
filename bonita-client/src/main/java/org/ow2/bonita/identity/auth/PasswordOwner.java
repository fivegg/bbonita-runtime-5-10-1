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

import java.util.logging.Level;
import java.util.logging.Logger;

import org.ow2.bonita.util.Misc;

/**
 * 
 * @author Elias Ricken de Medeiros
 *
 */
public class PasswordOwner {
	private static final ThreadLocal<String> PASWORDS = new ThreadLocal<String>();

  private static final Logger LOG = Logger.getLogger(PasswordOwner.class.getName());

  public static final String HELP = "Password has not been set up using setPassword(String password)!"
    + "Problem may be:"
    + Misc.LINE_SEPARATOR
    + "\t - you did not inform the rest password in the jaas file (e.g. using the option: restPassword=\"restbpm\"" + ")";


  public static void setPassword(final String passwordHash) {
  	if (LOG.isLoggable(Level.FINEST)) {
      LOG.entering(PasswordOwner.class.getName(), "setPassword: " + "*****");
    }
    PASWORDS.set(passwordHash);
  }

  public static String getPassword() {
    if (LOG.isLoggable(Level.FINEST)) {
      LOG.entering(PasswordOwner.class.getName(), "getPassword");
    }
    final String s = PASWORDS.get();
    if (s == null) {
      throw new IllegalStateException(HELP);
    }

    if (LOG.isLoggable(Level.FINEST)) {
      LOG.exiting(PasswordOwner.class.getName(), "getPassword", "*****");
    }
    return s;
  }

}
