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
package org.ow2.bonita.facade;

import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import org.ow2.bonita.facade.internal.RemoteBAMAPI;
import org.ow2.bonita.facade.internal.RemoteCommandAPI;
import org.ow2.bonita.facade.internal.RemoteIdentityAPI;
import org.ow2.bonita.facade.internal.RemoteManagementAPI;
import org.ow2.bonita.facade.internal.RemoteQueryDefinitionAPI;
import org.ow2.bonita.facade.internal.RemoteQueryRuntimeAPI;
import org.ow2.bonita.facade.internal.RemoteRepairAPI;
import org.ow2.bonita.facade.internal.RemoteRuntimeAPI;
import org.ow2.bonita.facade.internal.RemoteWebAPI;
import org.ow2.bonita.util.BonitaRuntimeException;

/**
 * @author Guillaume Porcher
 *
 */
public class RemoteAPITest extends TestCase {

	protected void checkCompatible(final Class< ? > api, final Class< ? > remoteApi) throws NoSuchMethodException {

		//check all methods in client API (*API) has a corresponding method in remote API (Remote*API)
		for (final Method apiMethod : api.getMethods()) {
			Method remoteApiMethod = null;
			Class< ? >[] remoteApiMethodParams;
			final Class< ? >[] clientApiMethodParams = apiMethod.getParameterTypes();
			if (clientApiMethodParams != null) {
				remoteApiMethodParams = new Class< ? >[clientApiMethodParams.length + 1];
				for (int i = 0; i < clientApiMethodParams.length; i++) {
					remoteApiMethodParams[i] = clientApiMethodParams[i];
				}
				remoteApiMethodParams[clientApiMethodParams.length] = Map.class;
			} else {
				remoteApiMethodParams = new Class< ? >[] {Map.class};
			}
			try {
				remoteApiMethod = remoteApi.getMethod(apiMethod.getName(), remoteApiMethodParams);
			} catch (final NoSuchMethodException e) {
				fail(getFailStack(apiMethod).toString());
			}
			assertNotNull(remoteApiMethod);
			final Set<Class< ? >> remoteApiMethodExceptions = new HashSet<Class< ? >>(Arrays.asList(remoteApiMethod.getExceptionTypes()));
			final Set<Class< ? >> clientApiMethodExceptions = new HashSet<Class< ? >>(Arrays.asList(apiMethod.getExceptionTypes()));
			clientApiMethodExceptions.add(RemoteException.class);
			assertEquals("Invalid exception in method: " + apiMethod, remoteApiMethodExceptions, clientApiMethodExceptions);
		}

		//check all methods in remote API (Remote*API) has a corresponding method in API (*API)
		for (final Method remoteApiMethod : remoteApi.getMethods()) {
			Method apiMethod = null;
			Class< ? >[] apiMethodParams;
			final Class< ? >[] p = remoteApiMethod.getParameterTypes();
			if (p != null) {
				apiMethodParams = new Class< ? >[p.length - 1];
				for (int i = 0; i < p.length - 1; i++) {
					apiMethodParams[i] = p[i];
				}
			} else {
				throw new BonitaRuntimeException("invalid method " + remoteApiMethod);
			}

			try {
				apiMethod = api.getMethod(remoteApiMethod.getName(), apiMethodParams);
			} catch (final NoSuchMethodException e) {
				fail("No client method matches with internal method: " + remoteApiMethod);
			}
			assertNotNull(apiMethod);
			final Set<Class< ? >> clientApiMethodExceptions = new HashSet<Class< ? >>(Arrays.asList(apiMethod.getExceptionTypes()));
			final Set<Class< ? >> internalApiMethodExceptions = new HashSet<Class< ? >>(Arrays.asList(remoteApiMethod.getExceptionTypes()));
			clientApiMethodExceptions.add(RemoteException.class);
			assertEquals("Invalid exception in method: " + apiMethod, clientApiMethodExceptions, internalApiMethodExceptions);
		}
	}

	private StringBuilder getFailStack(final Method apiMethod) {
		boolean containsCollection = false;
		for (Class<?> clazz : apiMethod.getParameterTypes()) {
			if (Collection.class.equals(clazz)) {
				containsCollection = true;
			}
		}
		
		String apiClassName = apiMethod.getDeclaringClass().getSimpleName();
		
		StringBuilder stb = new StringBuilder("No remote method matches with client method: ");
		stb.append(apiMethod);
						
		int count = 1;
		stb.append("\n " + (count++) + ") --> Add the method:\n");
		String missingRemoteMethod = getMissingRemoteMethod(apiMethod, false);
		stb.append(missingRemoteMethod);
		stb.append("\n--> in the class:\n");
		
		if (containsCollection) {
			stb.append("Remote");
			stb.append(apiClassName);
			stb.append("\n " + (count++) + ") --> Add the method:\n");
			
			String restMissingMethod = getMissingRemoteMethod(apiMethod, true);
			stb.append(restMissingMethod);
			stb.append("\n-->in the class:\n");
			stb.append("RESTRemote");
			stb.append(apiClassName);
			stb.append("\n\n" + (count++) + ")--> Are you sure you need a Collection in the method's parameters? If" +
					" it's possible to use a List, a Set or a TreeSet instead and you will need to " +
					"add only one method in the AbstractAPI");
		} else {			
			stb.append("AbstractRemote");
			stb.append(apiClassName);
		}
		
		stb.append("\n************************************");
		stb.append("\n" + (count++) + ")--> Make sure you have added the options map, the jax-rs annotations (if it's Abstract*API or REST*API)" +
		" and throws RemoteException");
		stb.append("\n************************************\n");
		return stb;
	}

	/**
	 * @param apiMethod
	 * @param replaceCollection 
	 * @return
	 */
	private String getMissingRemoteMethod(Method apiMethod, boolean replaceCollection) {
		Class<?> returnType = apiMethod.getReturnType();
		
		Class< ? >[] params;    
    
    final Class< ? >[] p = apiMethod.getParameterTypes();
		if (p != null) {
			params = new Class<?>[p.length + 1];
			for (int i = 0; i < p.length; i++) {
				if (!replaceCollection){
					params[i] = p[i];
				} else {
					if (p[i].equals(Collection.class)) {
						params[i] = List.class;
					} else {
						params[i] = p[i];
					}					
				} 
			}
			params[p.length] = Map.class;
		} else {
			params = new Class<?>[] { Map.class };
		}
    
		
		
		Class<?> [] e = apiMethod.getExceptionTypes();
		Class<?> [] exceptions;
		
		if (e != null) {
			exceptions = new Class<?>[e.length + 1];
			for (int i = 0; i < e.length; i++) {
				exceptions[i] = e[i];
			}
			exceptions[e.length] = RemoteException.class;
		} else {
			exceptions = new Class<?>[] { RemoteException.class };
		}
		
		StringBuilder stb = new StringBuilder();
		stb.append(returnType.getName());
		stb.append(" ");
		stb.append(apiMethod.getName());
		stb.append("(");
		
		for (Class<?> paramType : params) {
			stb.append(paramType.getName());
			stb.append(", ");
		}
		stb.replace(stb.length()-2, stb.length(), "");
		
		stb.append(") ");
		stb.append("throws ");
		
		for (Class<?> exceptionType : exceptions) {
			stb.append(exceptionType.getName());
			stb.append(", ");
		}
    
		stb.replace(stb.length()-2, stb.length(), "");
		
		return stb.toString();
	}

	public void testRuntimeAPI() throws Exception {
		checkCompatible(RuntimeAPI.class, RemoteRuntimeAPI.class);
	}

	public void testQueryDefintionAPI() throws Exception {
		checkCompatible(QueryDefinitionAPI.class, RemoteQueryDefinitionAPI.class);
	}

	public void testQueryRuntimeAPI() throws Exception {
		checkCompatible(QueryRuntimeAPI.class, RemoteQueryRuntimeAPI.class);
	}

	public void testCommandAPI() throws Exception {
		checkCompatible(CommandAPI.class, RemoteCommandAPI.class);
	}

	public void testBAMAPI() throws Exception {
		checkCompatible(BAMAPI.class, RemoteBAMAPI.class);
	}

	public void testIdentityAPI() throws Exception {
		checkCompatible(IdentityAPI.class, RemoteIdentityAPI.class);
	}

	public void testManagementAPI() throws Exception {
		checkCompatible(ManagementAPI.class, RemoteManagementAPI.class);
	}

	public void testWebAPI() throws Exception {
		checkCompatible(WebAPI.class, RemoteWebAPI.class);
	}

	public void testRepairAPI() throws Exception {
		checkCompatible(RepairAPI.class, RemoteRepairAPI.class);
	}

}
