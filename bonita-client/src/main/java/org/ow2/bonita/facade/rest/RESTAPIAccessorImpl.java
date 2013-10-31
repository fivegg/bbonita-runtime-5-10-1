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
package org.ow2.bonita.facade.rest;

import java.lang.reflect.Proxy;

import org.ow2.bonita.facade.APIAccessor;
import org.ow2.bonita.facade.CommandAPI;
import org.ow2.bonita.facade.IdentityAPI;
import org.ow2.bonita.facade.ManagementAPI;
import org.ow2.bonita.facade.RepairAPI;
import org.ow2.bonita.facade.RuntimeAPI;
import org.ow2.bonita.facade.WebAPI;
import org.ow2.bonita.facade.internal.RESTRemoteIdentityAPI;
import org.ow2.bonita.facade.internal.RESTRemoteManagementAPI;
import org.ow2.bonita.facade.internal.RESTRemoteRepairAPI;
import org.ow2.bonita.facade.internal.RESTRemoteRuntimeAPI;
import org.ow2.bonita.facade.internal.RESTRemoteWebAPI;
import org.ow2.bonita.facade.internal.RemoteCommandAPI;
import org.ow2.bonita.util.AccessorProxyUtil;
import org.ow2.bonita.util.AccessorUtil;

/**
 * 
 * @author Elias Ricken de Medeiros
 *
 */
public class RESTAPIAccessorImpl extends RESTQueryAPIAccessorImpl implements
    APIAccessor {	
	
	public RESTAPIAccessorImpl() {
		super();
	}
	
	public CommandAPI getCommandAPI() {
		return getCommandAPI(AccessorUtil.QUERYLIST_DEFAULT_KEY);
	}
	
	public CommandAPI getCommandAPI(String queryList){		
		RemoteCommandAPI remoteCommandAPI = getRESTAccess(RemoteCommandAPI.class);
		CommandAPI commandAPI = AccessorProxyUtil.getRemoteClientAPI(CommandAPI.class,
  		  remoteCommandAPI, queryList);
		
		//add REST interceptor
		RESTClientAPIInterceptor restInterceptor = new RESTClientAPIInterceptor(commandAPI);
		Class<CommandAPI> clazz = CommandAPI.class; 		
		return clazz.cast(Proxy.newProxyInstance(APIAccessor.class.getClassLoader(), new Class[] {clazz}, restInterceptor));	
	}

	public IdentityAPI getIdentityAPI() {
		return getIdentityAPI(AccessorUtil.QUERYLIST_DEFAULT_KEY);
	}
	
	public IdentityAPI getIdentityAPI(String queryList){
		RESTRemoteIdentityAPI remoteIdentityAPI = getRESTAccess(RESTRemoteIdentityAPI.class);		
		return AccessorProxyUtil.getRemoteClientAPI(IdentityAPI.class,
  		  remoteIdentityAPI, queryList);
	}

	public ManagementAPI getManagementAPI() {
			return getManagementAPI(AccessorUtil.QUERYLIST_DEFAULT_KEY);
	}
	
	public ManagementAPI getManagementAPI(String queryList){
		RESTRemoteManagementAPI remoteManagementAPI = getRESTAccess(RESTRemoteManagementAPI.class);		
		return AccessorProxyUtil.getRemoteClientAPI(ManagementAPI.class,
  		  remoteManagementAPI, queryList);
	}

	public RepairAPI getRepairAPI() {
		return getRepairAPI(AccessorUtil.QUERYLIST_DEFAULT_KEY);		
	}
	
	public RepairAPI getRepairAPI(String queryList){
		RESTRemoteRepairAPI remoteRepairAPI = getRESTAccess(RESTRemoteRepairAPI.class);		
		return AccessorProxyUtil.getRemoteClientAPI(RepairAPI.class,
  		  remoteRepairAPI, queryList);
	}

	public RuntimeAPI getRuntimeAPI() {
		return getRuntimeAPI(AccessorUtil.QUERYLIST_DEFAULT_KEY);
	}
	
	public RuntimeAPI getRuntimeAPI(String queryList){
		RESTRemoteRuntimeAPI remoteRuntimeAPI = getRESTAccess(RESTRemoteRuntimeAPI.class);		
		RuntimeAPI runtimeAPI = AccessorProxyUtil.getRemoteClientAPI(RuntimeAPI.class,
  		  remoteRuntimeAPI, queryList);
		
	  //add REST interceptor
		RESTClientAPIInterceptor restInterceptor = new RESTClientAPIInterceptor(runtimeAPI);
		Class<RuntimeAPI> clazz = RuntimeAPI.class; 		
		return clazz.cast(Proxy.newProxyInstance(APIAccessor.class.getClassLoader(), new Class[] {clazz}, restInterceptor));
	}

	public WebAPI getWebAPI() {
		return getWebAPI(AccessorUtil.QUERYLIST_DEFAULT_KEY);
	}
	
	public WebAPI getWebAPI(String queryList){
		RESTRemoteWebAPI remoteWebAPI = getRESTAccess(RESTRemoteWebAPI.class);		
		return AccessorProxyUtil.getRemoteClientAPI(WebAPI.class,
  		  remoteWebAPI, queryList);
	}

}
