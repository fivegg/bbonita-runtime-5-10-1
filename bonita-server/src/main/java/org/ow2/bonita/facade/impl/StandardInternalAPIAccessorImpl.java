/**
 * Copyright (C) 2006  Bull S. A. S.
 * Bull, Rue Jean Jaures, B.P.68, 78340, Les Clayes-sous-Bois
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

import java.lang.reflect.Proxy;

import org.ow2.bonita.facade.APIAccessor;
import org.ow2.bonita.facade.APIInterceptor;
import org.ow2.bonita.facade.CommandAPI;
import org.ow2.bonita.facade.IdentityAPI;
import org.ow2.bonita.facade.ManagementAPI;
import org.ow2.bonita.facade.RepairAPI;
import org.ow2.bonita.facade.RuntimeAPI;
import org.ow2.bonita.facade.WebAPI;
import org.ow2.bonita.facade.internal.InternalAPIAccessor;

public class StandardInternalAPIAccessorImpl extends StandardInternalQueryAPIAccessorImpl implements InternalAPIAccessor {

  public RuntimeAPI getRuntimeAPI(final String queryList) {
    final APIInterceptor apiInterceptor = new APIInterceptor(new RuntimeAPIImpl(queryList));
    return (RuntimeAPI) Proxy.newProxyInstance(APIAccessor.class.getClassLoader(), new Class[] {RuntimeAPI.class}, apiInterceptor);
  }

  public WebAPI getWebAPI(final String queryList) {
    final APIInterceptor apiInterceptor = new APIInterceptor(new WebAPIImpl(queryList));
    return (WebAPI) Proxy.newProxyInstance(APIAccessor.class.getClassLoader(), new Class[] {WebAPI.class}, apiInterceptor);
  }
  
  public ManagementAPI getManagementAPI(final String queryList) {
    final APIInterceptor apiInterceptor = new APIInterceptor(new ManagementAPIImpl(queryList));
    return (ManagementAPI) Proxy.newProxyInstance(APIAccessor.class.getClassLoader(), new Class[] {ManagementAPI.class}, apiInterceptor);
  }

  public CommandAPI getCommandAPI(final String queryList) {
    final APIInterceptor apiInterceptor = new APIInterceptor(new CommandAPIImpl(queryList));
    return (CommandAPI) Proxy.newProxyInstance(APIAccessor.class.getClassLoader(), new Class[] {CommandAPI.class}, apiInterceptor);
  }

  public IdentityAPI getIdentityAPI(final String queryList) {
    final APIInterceptor apiInterceptor = new APIInterceptor(new IdentityAPIImpl(queryList));
    return (IdentityAPI) Proxy.newProxyInstance(APIAccessor.class.getClassLoader(), new Class[] {IdentityAPI.class}, apiInterceptor);
  }

  public RepairAPI getRepairAPI(final String queryList) {
    final APIInterceptor apiInterceptor = new APIInterceptor(new RepairAPIImpl(queryList));
    return (RepairAPI) Proxy.newProxyInstance(APIAccessor.class.getClassLoader(), new Class[] {RepairAPI.class}, apiInterceptor);
  }

}
