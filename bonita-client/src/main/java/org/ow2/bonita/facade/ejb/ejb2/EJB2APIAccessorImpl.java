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

import org.ow2.bonita.facade.APIAccessor;
import org.ow2.bonita.facade.CommandAPI;
import org.ow2.bonita.facade.IdentityAPI;
import org.ow2.bonita.facade.ManagementAPI;
import org.ow2.bonita.facade.RepairAPI;
import org.ow2.bonita.facade.RuntimeAPI;
import org.ow2.bonita.facade.WebAPI;
import org.ow2.bonita.facade.ejb.ejb2.home.EJB2CommandAPIHome;
import org.ow2.bonita.facade.ejb.ejb2.home.EJB2IdentityAPIHome;
import org.ow2.bonita.facade.ejb.ejb2.home.EJB2ManagementAPIHome;
import org.ow2.bonita.facade.ejb.ejb2.home.EJB2RepairAPIHome;
import org.ow2.bonita.facade.ejb.ejb2.home.EJB2RuntimeAPIHome;
import org.ow2.bonita.facade.ejb.ejb2.home.EJB2WebAPIHome;
import org.ow2.bonita.facade.exception.BonitaInternalException;
import org.ow2.bonita.facade.interceptor.EJBCommandAPIInterceptor;
import org.ow2.bonita.facade.internal.RemoteCommandAPI;
import org.ow2.bonita.facade.internal.RemoteIdentityAPI;
import org.ow2.bonita.facade.internal.RemoteManagementAPI;
import org.ow2.bonita.facade.internal.RemoteRepairAPI;
import org.ow2.bonita.facade.internal.RemoteRuntimeAPI;
import org.ow2.bonita.facade.internal.RemoteWebAPI;
import org.ow2.bonita.util.AccessorProxyUtil;
import org.ow2.bonita.util.AccessorUtil;
import org.ow2.bonita.util.Misc;

public class EJB2APIAccessorImpl extends EJB2QueryAPIAccessorImpl implements APIAccessor {

  private RemoteCommandAPI commandAPI;
  private RemoteManagementAPI managementAPI;
  private RemoteRuntimeAPI runtimeAPI;
  private RemoteWebAPI webAPI;
  private RemoteIdentityAPI identityAPI;
  private RemoteRepairAPI repairAPI;
  
  public EJB2APIAccessorImpl(Hashtable<String, String> jndiEnvironment) {
    super(jndiEnvironment);
  }
  
  public CommandAPI getCommandAPI() {
  	return getCommandAPI(AccessorUtil.QUERYLIST_DEFAULT_KEY);
  }
  
  public CommandAPI getCommandAPI(final String queryList) {
    if (commandAPI == null) {
      try {
        EJB2CommandAPIHome commandAPIHome = (EJB2CommandAPIHome) PortableRemoteObject.narrow(
            Misc.<Object>lookup(AccessorUtil.COMMANDAPI_JNDINAME, jndiEnvironment), EJB2CommandAPIHome.class);
        commandAPI = commandAPIHome.create();
      } catch (RemoteException e) {
        throw new BonitaInternalException(e.getMessage(), e);
      } catch (CreateException e) {
        throw new BonitaInternalException(e.getMessage(), e);
      } catch (NamingException e) {
        throw new BonitaInternalException(e.getMessage(), e);
      }
    }
    CommandAPI ejbCommandAPI = AccessorProxyUtil.getRemoteClientAPI(CommandAPI.class, commandAPI, queryList);
    
    return new EJBCommandAPIInterceptor(ejbCommandAPI);
  }
  
  public WebAPI getWebAPI() {
  	return getWebAPI(AccessorUtil.QUERYLIST_DEFAULT_KEY);
  }
  
  public WebAPI getWebAPI(final String queryList) {
    if (webAPI == null) {
      try {
        EJB2WebAPIHome webAPIHome = (EJB2WebAPIHome) PortableRemoteObject.narrow(
            Misc.<Object>lookup(AccessorUtil.WEBAPI_JNDINAME, jndiEnvironment), EJB2WebAPIHome.class);
        webAPI = webAPIHome.create();
      } catch (RemoteException e) {
        throw new BonitaInternalException(e.getMessage(), e);
      } catch (CreateException e) {
        throw new BonitaInternalException(e.getMessage(), e);
      } catch (NamingException e) {
        throw new BonitaInternalException(e.getMessage(), e);
      }
    }
    return AccessorProxyUtil.getRemoteClientAPI(WebAPI.class, webAPI, queryList);
  }

  public ManagementAPI getManagementAPI() {
  	return getManagementAPI(AccessorUtil.QUERYLIST_DEFAULT_KEY);
  }
  
  public ManagementAPI getManagementAPI(final String queryList) {
    if (managementAPI == null) {
      try {
        EJB2ManagementAPIHome managementAPIHome = (EJB2ManagementAPIHome) PortableRemoteObject.narrow(
            Misc.<Object>lookup(AccessorUtil.MANAGEMENT_JNDINAME, jndiEnvironment), EJB2ManagementAPIHome.class);
        managementAPI = managementAPIHome.create();
      } catch (RemoteException e) {
        throw new BonitaInternalException(e.getMessage(), e);
      } catch (CreateException e) {
        throw new BonitaInternalException(e.getMessage(), e);
      } catch (NamingException e) {
        throw new BonitaInternalException(e.getMessage(), e);
      }
    }
    return AccessorProxyUtil.getRemoteClientAPI(ManagementAPI.class, managementAPI, queryList);
  }

  public RuntimeAPI getRuntimeAPI() {
  	return getRuntimeAPI(AccessorUtil.QUERYLIST_DEFAULT_KEY);
  }
  
  public RuntimeAPI getRuntimeAPI(final String queryList) {
    if (runtimeAPI == null) {
      try {
        EJB2RuntimeAPIHome runtimeAPIHome = (EJB2RuntimeAPIHome) PortableRemoteObject.narrow(
            Misc.<Object>lookup(AccessorUtil.RUNTIMEAPI_JNDINAME, jndiEnvironment), EJB2RuntimeAPIHome.class);
        runtimeAPI = runtimeAPIHome.create();
      } catch (RemoteException e) {
        throw new BonitaInternalException(e.getMessage(), e);
      } catch (CreateException e) {
        throw new BonitaInternalException(e.getMessage(), e);
      } catch (NamingException e) {
        throw new BonitaInternalException(e.getMessage(), e);
      }
    }
    return AccessorProxyUtil.getRemoteClientAPI(RuntimeAPI.class, runtimeAPI, queryList);
  }

  public IdentityAPI getIdentityAPI() {
  	return getIdentityAPI(AccessorUtil.QUERYLIST_DEFAULT_KEY);
  }
  
  public IdentityAPI getIdentityAPI(final String queryList) {
    if (identityAPI == null) {
      try {
        EJB2IdentityAPIHome identityAPIHome = (EJB2IdentityAPIHome) PortableRemoteObject.narrow(
            Misc.<Object>lookup(AccessorUtil.IDENTITYAPI_JNDINAME, jndiEnvironment), EJB2IdentityAPIHome.class);
        identityAPI = identityAPIHome.create();
      } catch (RemoteException e) {
        throw new BonitaInternalException(e.getMessage(), e);
      } catch (CreateException e) {
        throw new BonitaInternalException(e.getMessage(), e);
      } catch (NamingException e) {
        throw new BonitaInternalException(e.getMessage(), e);
      }
    }
    return AccessorProxyUtil.getRemoteClientAPI(IdentityAPI.class, identityAPI, queryList);
  }

  public RepairAPI getRepairAPI() {
  	return getRepairAPI(AccessorUtil.QUERYLIST_DEFAULT_KEY);
  }
  
  public RepairAPI getRepairAPI(final String queryList) {
    if (repairAPI == null) {
      try {
        EJB2RepairAPIHome repairAPIHome = (EJB2RepairAPIHome) PortableRemoteObject.narrow(
            Misc.<Object>lookup(AccessorUtil.REPAIRAPI_JNDINAME, jndiEnvironment), EJB2RepairAPIHome.class);
        repairAPI = repairAPIHome.create();
      } catch (RemoteException e) {
        throw new BonitaInternalException(e.getMessage(), e);
      } catch (CreateException e) {
        throw new BonitaInternalException(e.getMessage(), e);
      } catch (NamingException e) {
        throw new BonitaInternalException(e.getMessage(), e);
      }
    }
    return AccessorProxyUtil.getRemoteClientAPI(RepairAPI.class, repairAPI, queryList);
  }

}
