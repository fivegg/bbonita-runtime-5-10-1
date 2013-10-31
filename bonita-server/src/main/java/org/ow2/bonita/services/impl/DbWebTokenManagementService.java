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
package org.ow2.bonita.services.impl;

import java.util.Set;

import org.ow2.bonita.facade.runtime.WebTemporaryToken;
import org.ow2.bonita.facade.runtime.impl.WebTemporaryTokenImpl;
import org.ow2.bonita.persistence.WebTokenManagementDbSession;
import org.ow2.bonita.persistence.db.HibernateDbSession;
import org.ow2.bonita.services.WebTokenManagementService;
import org.ow2.bonita.util.EnvTool;

/**
 * @author Nicolas Chabanoles
 * 
 */
public class DbWebTokenManagementService extends HibernateDbSession implements
    WebTokenManagementService {

  private String persistenceServiceName;

  public DbWebTokenManagementService(String persistenceServiceName) {
    super();
    this.persistenceServiceName = persistenceServiceName;
  }

  protected WebTokenManagementDbSession getDbSession() {
    return EnvTool.getWebTokenManagementDbSession(persistenceServiceName);
  }

  public void deleteToken(WebTemporaryTokenImpl token) {
    getDbSession().delete(token);

  }

  public void addTemporaryToken(WebTemporaryTokenImpl token) {
    getDbSession().save(token);
  }

  public WebTemporaryToken getToken(String token) {
    return getDbSession().getToken(token);
  }

  public Set<WebTemporaryToken> getExpiredTokens() {
    return getDbSession().getExpiredTokens();
  }

}
