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
 * @author Elias Ricken de Medeiros
 *
 */
public class RESTUserOwner {
  private static final ThreadLocal<String> REST_USERS = new ThreadLocal<String>();

  private static final Logger LOG = Logger.getLogger(RESTUserOwner.class.getName());

  public static final String HELP = "RESTUser has not been set up using setUser(String user)!"
    + "Problem may be:"
    + Misc.LINE_SEPARATOR
    + "\t - you did not inform the REST user in the jass file (e.g. adding de option: restUser=\"restuser\"" + ")";


  public static void setUser(final String user) {
    if (LOG.isLoggable(Level.FINEST)) {
      LOG.entering(RESTUserOwner.class.getName(), "setUser: " + user);
    }
    REST_USERS.set(user);
  }

  public static String getUser() {
    if (LOG.isLoggable(Level.FINEST)) {
      LOG.entering(RESTUserOwner.class.getName(), "getUser");
    }
    final String s = REST_USERS.get();
    if (s == null) {
      throw new IllegalStateException(HELP);
    }

    if (LOG.isLoggable(Level.FINEST)) {
      LOG.exiting(RESTUserOwner.class.getName(), "getUser", s);
    }
    return s;
  }
}
