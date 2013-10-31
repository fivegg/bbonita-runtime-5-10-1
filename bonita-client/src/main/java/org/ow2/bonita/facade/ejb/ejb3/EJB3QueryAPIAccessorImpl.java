/**
 * Copyright (C) 2007  Bull S. A. S.
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
package org.ow2.bonita.facade.ejb.ejb3;

import java.util.Hashtable;

import javax.naming.NamingException;

import org.ow2.bonita.facade.BAMAPI;
import org.ow2.bonita.facade.QueryAPIAccessor;
import org.ow2.bonita.facade.QueryDefinitionAPI;
import org.ow2.bonita.facade.QueryRuntimeAPI;
import org.ow2.bonita.util.AccessorProxyUtil;
import org.ow2.bonita.util.AccessorUtil;
import org.ow2.bonita.util.BonitaRuntimeException;
import org.ow2.bonita.util.Misc;


/**
 * @author Guillaume Porcher
 *
 */
public class EJB3QueryAPIAccessorImpl implements QueryAPIAccessor {

  protected Hashtable<String, String> jndiEnvironment; 

  public EJB3QueryAPIAccessorImpl(Hashtable<String, String> jndiEnvironment) {
    this.jndiEnvironment = jndiEnvironment;
  }

  public QueryDefinitionAPI getQueryDefinitionAPI() {
    return getQueryDefinitionAPI(AccessorUtil.QUERYLIST_DEFAULT_KEY);
  }

  public QueryDefinitionAPI getQueryDefinitionAPI(final String queryList) {
    try {
      return AccessorProxyUtil.getRemoteClientAPI(QueryDefinitionAPI.class,
          Misc.lookup(AccessorUtil.QUERYDEFINITION_JNDINAME, jndiEnvironment), queryList);
    } catch (final NamingException e) {
      throw new BonitaRuntimeException(e);
    }
  }

  public QueryRuntimeAPI getQueryRuntimeAPI() {
    return getQueryRuntimeAPI(AccessorUtil.QUERYLIST_DEFAULT_KEY);
  }

  public QueryRuntimeAPI getQueryRuntimeAPI(final String queryList) {
    try {
      return AccessorProxyUtil.getRemoteClientAPI(QueryRuntimeAPI.class,
          Misc.lookup(AccessorUtil.QUERYRUNTIME_JNDINAME, jndiEnvironment), queryList);
    } catch (final NamingException e) {
      throw new BonitaRuntimeException(e);
    }
  }

  public BAMAPI getBAMAPI() {
    return getBAMAPI(AccessorUtil.QUERYLIST_DEFAULT_KEY);
  }

  public BAMAPI getBAMAPI(final String queryList) {
    try {
      return AccessorProxyUtil.getRemoteClientAPI(BAMAPI.class,
          Misc.lookup(AccessorUtil.BAMAPI_JNDINAME, jndiEnvironment), queryList);
    } catch (final NamingException e) {
      throw new BonitaRuntimeException(e);
    }
  }
}
