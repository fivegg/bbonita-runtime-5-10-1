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
 **/
package org.ow2.bonita.facade.ejb.ejb3;

import java.util.Hashtable;

import javax.naming.NamingException;

import org.ow2.bonita.facade.APIAccessor;
import org.ow2.bonita.facade.CommandAPI;
import org.ow2.bonita.facade.IdentityAPI;
import org.ow2.bonita.facade.ManagementAPI;
import org.ow2.bonita.facade.RepairAPI;
import org.ow2.bonita.facade.RuntimeAPI;
import org.ow2.bonita.facade.WebAPI;
import org.ow2.bonita.facade.interceptor.EJBCommandAPIInterceptor;
import org.ow2.bonita.util.AccessorProxyUtil;
import org.ow2.bonita.util.AccessorUtil;
import org.ow2.bonita.util.BonitaRuntimeException;
import org.ow2.bonita.util.Misc;


/**
 * @author Guillaume Porcher
 *
 */
public class EJB3APIAccessorImpl extends EJB3QueryAPIAccessorImpl implements APIAccessor {

  public EJB3APIAccessorImpl(Hashtable<String, String> jndiEnvironment) {
    super(jndiEnvironment);
  }
  
  public CommandAPI getCommandAPI() {
  	return getCommandAPI(AccessorUtil.QUERYLIST_DEFAULT_KEY);
  }
  
  public CommandAPI getCommandAPI(final String queryList) {
    CommandAPI ejbCommandAPI;
    try {
      ejbCommandAPI = AccessorProxyUtil.getRemoteClientAPI(CommandAPI.class, Misc.lookup(AccessorUtil.COMMANDAPI_JNDINAME, jndiEnvironment), queryList);
    } catch (final NamingException e) {
      throw new BonitaRuntimeException(e);
    }
    return new EJBCommandAPIInterceptor(ejbCommandAPI);

  }
  
  public WebAPI getWebAPI() {
  	return getWebAPI(AccessorUtil.QUERYLIST_DEFAULT_KEY);
  }
  
  public WebAPI getWebAPI(final String queryList) {
    try {
      return AccessorProxyUtil.getRemoteClientAPI(WebAPI.class, Misc.lookup(AccessorUtil.WEBAPI_JNDINAME, jndiEnvironment), queryList);
    } catch (final NamingException e) {
      throw new BonitaRuntimeException(e);
    }
  }
  
  public ManagementAPI getManagementAPI() {
  	return getManagementAPI(AccessorUtil.QUERYLIST_DEFAULT_KEY);
  }
  
  public ManagementAPI getManagementAPI(final String queryList) {
    try {
      return AccessorProxyUtil.getRemoteClientAPI(ManagementAPI.class, Misc.lookup(AccessorUtil.MANAGEMENT_JNDINAME, jndiEnvironment), queryList);
    } catch (final NamingException e) {
      throw new BonitaRuntimeException(e);
    }
  }

  public RuntimeAPI getRuntimeAPI() {
  	return getRuntimeAPI(AccessorUtil.QUERYLIST_DEFAULT_KEY);
  }
  
  public RuntimeAPI getRuntimeAPI(final String queryList) {
    try {
      return AccessorProxyUtil.getRemoteClientAPI(RuntimeAPI.class, Misc.lookup(AccessorUtil.RUNTIMEAPI_JNDINAME, jndiEnvironment), queryList);
    } catch (final NamingException e) {
      throw new BonitaRuntimeException(e);
    }
  }

  public IdentityAPI getIdentityAPI() {
  	return getIdentityAPI(AccessorUtil.QUERYLIST_DEFAULT_KEY);
  }
  
  public IdentityAPI getIdentityAPI(final String queryList) {
    try {
      return AccessorProxyUtil.getRemoteClientAPI(IdentityAPI.class, Misc.lookup(AccessorUtil.IDENTITYAPI_JNDINAME, jndiEnvironment), queryList);
    } catch (final NamingException e) {
      throw new BonitaRuntimeException(e);
    }
  }

  public RepairAPI getRepairAPI() {
  	return getRepairAPI(AccessorUtil.QUERYLIST_DEFAULT_KEY);
  }
  
  public RepairAPI getRepairAPI(final String queryList) {
    try {
      return AccessorProxyUtil.getRemoteClientAPI(RepairAPI.class, Misc.lookup(AccessorUtil.REPAIRAPI_JNDINAME, jndiEnvironment), queryList);
    } catch (final NamingException e) {
      throw new BonitaRuntimeException(e);
    }
  }

}
