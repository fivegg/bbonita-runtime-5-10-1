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
package org.ow2.bonita.facade.impl;

import java.util.HashMap;
import java.util.Map;

import org.ow2.bonita.facade.APIAccessor;
import org.ow2.bonita.facade.CommandAPI;
import org.ow2.bonita.facade.internal.RemoteCommandAPI;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.identity.auth.DomainOwner;
import org.ow2.bonita.identity.auth.PasswordOwner;
import org.ow2.bonita.identity.auth.RESTUserOwner;
import org.ow2.bonita.identity.auth.UserOwner;
import org.ow2.bonita.util.Command;

/**
 * 
 * @author Elias Ricken de Medeiros, Matthieu Chaffotte
 * 
 */
public class RemoteCommandAPIImpl implements RemoteCommandAPI {

  protected Map<String, CommandAPI> apis = new HashMap<String, CommandAPI>();

  protected CommandAPI getAPI(final Map<String, String> options) {
    if (options == null) {
      throw new IllegalArgumentException("The options are null or not well set.");
    }
    final String queryList = options.get(APIAccessor.QUERYLIST_OPTION);
    final String user = options.get(APIAccessor.USER_OPTION);
    final String domain = options.get(APIAccessor.DOMAIN_OPTION);
    UserOwner.setUser(user);
    DomainOwner.setDomain(domain);

    final String restUser = options.get(APIAccessor.REST_USER_OPTION);
    if (restUser != null) {
      RESTUserOwner.setUser(restUser);
      final String restPswd = options.get(APIAccessor.PASSWORD_HASH_OPTION);
      PasswordOwner.setPassword(restPswd);
    }

    if (!apis.containsKey(queryList)) {
      putAPI(queryList);
    }
    return apis.get(queryList);
  }

  protected void putAPI(final String queryList) {
    apis.put(queryList, new StandardAPIAccessorImpl().getCommandAPI(queryList));
  }

  @Override
  public <T> T execute(final Command<T> command, final Map<String, String> options) throws Exception {
    return getAPI(options).execute(command);
  }

  @Override
  public <T> T execute(final Command<T> command, final ProcessDefinitionUUID processUUID,
      final Map<String, String> options) throws Exception {
    return getAPI(options).execute(command, processUUID);
  }

}
