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

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

import org.ow2.bonita.facade.APIAccessor;
import org.ow2.bonita.facade.RepairAPI;
import org.ow2.bonita.facade.exception.ActivityNotFoundException;
import org.ow2.bonita.facade.exception.InstanceNotFoundException;
import org.ow2.bonita.facade.exception.VariableNotFoundException;
import org.ow2.bonita.facade.internal.AbstractRemoteRepairAPI;
import org.ow2.bonita.facade.uuid.ActivityInstanceUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.identity.auth.DomainOwner;
import org.ow2.bonita.identity.auth.PasswordOwner;
import org.ow2.bonita.identity.auth.RESTUserOwner;
import org.ow2.bonita.identity.auth.UserOwner;

/**
 * 
 * @author Elias Ricken de Medeiros, Matthieu Chaffotte
 * 
 */
public class AbstractRemoteRepairAPIImpl implements AbstractRemoteRepairAPI {

  protected Map<String, RepairAPI> apis = new HashMap<String, RepairAPI>();

  protected RepairAPI getAPI(final Map<String, String> options) {
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
      apis.put(queryList, new StandardAPIAccessorImpl().getRepairAPI(queryList));
    }
    return apis.get(queryList);
  }

  @Override
  public ActivityInstanceUUID startExecution(final ProcessInstanceUUID instanceUUID, final String activityName,
      final Map<String, String> options) throws RemoteException, InstanceNotFoundException, ActivityNotFoundException,
      VariableNotFoundException {
    return getAPI(options).startExecution(instanceUUID, activityName);
  }

  @Override
  public void stopExecution(final ProcessInstanceUUID instanceUUID, final String activityName,
      final Map<String, String> options) throws RemoteException, InstanceNotFoundException, ActivityNotFoundException {
    getAPI(options).stopExecution(instanceUUID, activityName);
  }

}
