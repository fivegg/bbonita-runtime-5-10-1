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
package org.ow2.bonita.env.descriptor;

import org.hibernate.Session;
import org.ow2.bonita.env.WireContext;
import org.ow2.bonita.env.WireDefinition;
import org.ow2.bonita.env.WireException;
import org.ow2.bonita.persistence.DbSession;

/**
 *
 * @author Charles Souillard
 */
public abstract class AbstractDbSessionDescriptor extends AbstractDescriptor {

  private static final long serialVersionUID = 1L;

  protected String sessionName = null;

  abstract Class< ? extends DbSession> getDbSessionClass();
  abstract Object getDbSessionObject(final Session session);
  
  public Object construct(WireContext wireContext) {
    Session session = null;
    if (sessionName != null) {
      session = (Session) wireContext.get(sessionName);
      if (session == null) {
        throw new WireException("Could not find a session with name: " + sessionName);
      }
    } else {
      session = wireContext.get(Session.class);
      if (session == null) {
        throw new WireException("No object ob type Session found in environment");
      }
    }
    return getDbSessionObject(session);
  }

  public Class< ? > getType(WireDefinition wireDefinition) {
    return getDbSessionClass();
  }

  public void setSessionName(String sessionName) {
    this.sessionName = sessionName;
  }

}
