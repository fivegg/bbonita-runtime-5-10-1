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

import org.ow2.bonita.facade.BAMAPI;
import org.ow2.bonita.facade.QueryAPIAccessor;
import org.ow2.bonita.facade.QueryDefinitionAPI;
import org.ow2.bonita.facade.QueryRuntimeAPI;
import org.ow2.bonita.facade.internal.InternalQueryAPIAccessor;
import org.ow2.bonita.util.AccessorUtil;

public class StandardQueryAPIAccessorImpl implements QueryAPIAccessor {

  private final InternalQueryAPIAccessor serverQueryAccessor = LocalAPIAccessorFactory.getStandardServerQueryAPIAccessor();

  public StandardQueryAPIAccessorImpl() {
  }

  public QueryRuntimeAPI getQueryRuntimeAPI() {
    return getQueryRuntimeAPI(AccessorUtil.QUERYLIST_DEFAULT_KEY);
  }

  public QueryRuntimeAPI getQueryRuntimeAPI(final String queryList) {
  	return this.serverQueryAccessor.getQueryRuntimeAPI(queryList);
  }

  public QueryDefinitionAPI getQueryDefinitionAPI() {
    return getQueryDefinitionAPI(AccessorUtil.QUERYLIST_DEFAULT_KEY);
  }
  
  public QueryDefinitionAPI getQueryDefinitionAPI(final String queryList) {
  	return this.serverQueryAccessor.getQueryDefinitionAPI(queryList);
  }

  public BAMAPI getBAMAPI() {
    return getBAMAPI(AccessorUtil.QUERYLIST_DEFAULT_KEY);
  }

  public BAMAPI getBAMAPI(final String queryList) {
  	return this.serverQueryAccessor.getBAMAPI(queryList);
  }
}
