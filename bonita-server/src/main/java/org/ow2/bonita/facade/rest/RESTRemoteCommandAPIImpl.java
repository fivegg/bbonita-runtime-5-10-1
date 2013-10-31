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
import org.ow2.bonita.facade.impl.RemoteCommandAPIImpl;
import org.ow2.bonita.facade.impl.StandardAPIAccessorImpl;

/**
 * 
 * @author Elias Ricken de Medeiros
 *
 */
public class RESTRemoteCommandAPIImpl extends RemoteCommandAPIImpl {
	protected void putAPI(String queryList) {		
		CommandAPI commandAPI = new StandardAPIAccessorImpl()
				.getCommandAPI(queryList);
		
		// Add REST interceptor
		final RESTServerAPIInterceptor restInterceptor = new RESTServerAPIInterceptor(
				commandAPI);
		Class<CommandAPI> clazz = CommandAPI.class;
		commandAPI = clazz.cast(Proxy.newProxyInstance(APIAccessor.class
				.getClassLoader(), new Class[] { clazz }, restInterceptor));

		apis.put(queryList, commandAPI);
	}
}
