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
 * 
 * Modified by Matthieu Chaffotte - BonitaSoft S.A.
 **/
package org.ow2.bonita.facade.impl;

import java.lang.reflect.Proxy;

import org.ow2.bonita.facade.APIAccessor;
import org.ow2.bonita.facade.APIInterceptor;
import org.ow2.bonita.facade.BAMAPI;
import org.ow2.bonita.facade.QueryAPIAccessor;
import org.ow2.bonita.facade.QueryDefinitionAPI;
import org.ow2.bonita.facade.QueryRuntimeAPI;
import org.ow2.bonita.facade.internal.InternalQueryAPIAccessor;

public class StandardInternalQueryAPIAccessorImpl implements InternalQueryAPIAccessor {

  public StandardInternalQueryAPIAccessorImpl() {
  }

  public QueryRuntimeAPI getQueryRuntimeAPI(final String queryList) {
    final APIInterceptor aPIInterceptor = new APIInterceptor(new QueryRuntimeAPIImpl(queryList));
    return (QueryRuntimeAPI) Proxy.newProxyInstance(QueryAPIAccessor.class.getClassLoader(), new Class[] {QueryRuntimeAPI.class}, aPIInterceptor);
  }

  public QueryDefinitionAPI getQueryDefinitionAPI(final String queryList) {
    final APIInterceptor aPIInterceptor = new APIInterceptor(new QueryDefinitionAPIImpl(queryList));
    return (QueryDefinitionAPI) Proxy.newProxyInstance(QueryAPIAccessor.class.getClassLoader(), new Class[] {QueryDefinitionAPI.class}, aPIInterceptor);
  }

  public BAMAPI getBAMAPI(final String queryList) {
    final APIInterceptor apiInterceptor = new APIInterceptor(new BAMAPIImpl(queryList));
    return (BAMAPI) Proxy.newProxyInstance(APIAccessor.class.getClassLoader(), new Class[] {BAMAPI.class}, apiInterceptor);
  }
  
}
