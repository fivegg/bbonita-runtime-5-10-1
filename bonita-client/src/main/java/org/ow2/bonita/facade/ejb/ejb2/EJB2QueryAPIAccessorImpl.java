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
package org.ow2.bonita.facade.ejb.ejb2;

import java.rmi.RemoteException;
import java.util.Hashtable;

import javax.ejb.CreateException;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;

import org.ow2.bonita.facade.BAMAPI;
import org.ow2.bonita.facade.QueryAPIAccessor;
import org.ow2.bonita.facade.QueryDefinitionAPI;
import org.ow2.bonita.facade.QueryRuntimeAPI;
import org.ow2.bonita.facade.ejb.ejb2.home.EJB2BAMAPIHome;
import org.ow2.bonita.facade.ejb.ejb2.home.EJB2QueryDefinitionAPIHome;
import org.ow2.bonita.facade.ejb.ejb2.home.EJB2QueryRuntimeAPIHome;
import org.ow2.bonita.facade.exception.BonitaInternalException;
import org.ow2.bonita.facade.internal.RemoteBAMAPI;
import org.ow2.bonita.facade.internal.RemoteQueryDefinitionAPI;
import org.ow2.bonita.facade.internal.RemoteQueryRuntimeAPI;
import org.ow2.bonita.util.AccessorProxyUtil;
import org.ow2.bonita.util.AccessorUtil;
import org.ow2.bonita.util.Misc;

public class EJB2QueryAPIAccessorImpl implements QueryAPIAccessor {

  private RemoteQueryDefinitionAPI queryDefinitionAPI;
  private RemoteQueryRuntimeAPI queryRuntimeAPI;
  private RemoteBAMAPI bamAPI;
  protected Hashtable<String, String> jndiEnvironment; 
  
  public EJB2QueryAPIAccessorImpl(Hashtable<String, String> jndiEnvironment) {
    this.jndiEnvironment = jndiEnvironment;
  }
  
  public QueryDefinitionAPI getQueryDefinitionAPI() {
    return getQueryDefinitionAPI(AccessorUtil.QUERYLIST_DEFAULT_KEY);
  }
  
  public QueryDefinitionAPI getQueryDefinitionAPI(final String queryList) {
    if (this.queryDefinitionAPI == null) {
      try {
        final EJB2QueryDefinitionAPIHome queryDefinitionHome = (EJB2QueryDefinitionAPIHome) PortableRemoteObject.narrow(
            Misc.<Object>lookup(AccessorUtil.QUERYDEFINITION_JNDINAME, jndiEnvironment), EJB2QueryDefinitionAPIHome.class);
        this.queryDefinitionAPI = queryDefinitionHome.create();
      } catch (final RemoteException e) {
        throw new BonitaInternalException(e.getMessage(), e);
      } catch (final CreateException e) {
        throw new BonitaInternalException(e.getMessage(), e);
      } catch (final NamingException e) {
        throw new BonitaInternalException(e.getMessage(), e);
      }
    }
    return AccessorProxyUtil.getRemoteClientAPI(QueryDefinitionAPI.class, queryDefinitionAPI, queryList);
  }

  public QueryRuntimeAPI getQueryRuntimeAPI() {
    return getQueryRuntimeAPI(AccessorUtil.QUERYLIST_DEFAULT_KEY);
  }

  public QueryRuntimeAPI getQueryRuntimeAPI(final String queryList) {
    if (this.queryRuntimeAPI == null) {
      try {
        final EJB2QueryRuntimeAPIHome queryRuntimeAPIHome = (EJB2QueryRuntimeAPIHome) PortableRemoteObject.narrow(
            Misc.<Object>lookup(AccessorUtil.QUERYRUNTIME_JNDINAME, jndiEnvironment), EJB2QueryRuntimeAPIHome.class);
        this.queryRuntimeAPI = queryRuntimeAPIHome.create();
      } catch (final RemoteException e) {
        throw new BonitaInternalException(e.getMessage(), e);
      } catch (final CreateException e) {
        throw new BonitaInternalException(e.getMessage(), e);
      } catch (final NamingException e) {
        throw new BonitaInternalException(e.getMessage(), e);
      }
    }
    return AccessorProxyUtil.getRemoteClientAPI(QueryRuntimeAPI.class, queryRuntimeAPI, queryList);
  }

  public BAMAPI getBAMAPI() {
    return getBAMAPI(AccessorUtil.QUERYLIST_DEFAULT_KEY);
  }

  public BAMAPI getBAMAPI(final String queryList) {
    if (this.bamAPI == null) {
      try {
        final EJB2BAMAPIHome bamAPIHome = (EJB2BAMAPIHome) PortableRemoteObject.narrow(
            Misc.<Object>lookup(AccessorUtil.BAMAPI_JNDINAME, jndiEnvironment), EJB2BAMAPIHome.class);
        this.bamAPI = bamAPIHome.create();
      } catch (final RemoteException e) {
        throw new BonitaInternalException(e.getMessage(), e);
      } catch (final CreateException e) {
        throw new BonitaInternalException(e.getMessage(), e);
      } catch (final NamingException e) {
        throw new BonitaInternalException(e.getMessage(), e);
      }
    }
    return AccessorProxyUtil.getRemoteClientAPI(BAMAPI.class, bamAPI, queryList);
  } 
}
