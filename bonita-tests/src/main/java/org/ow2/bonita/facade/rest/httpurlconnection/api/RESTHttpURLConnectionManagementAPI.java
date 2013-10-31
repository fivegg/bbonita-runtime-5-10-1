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
package org.ow2.bonita.facade.rest.httpurlconnection.api;

import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;

import org.ow2.bonita.facade.rest.httpurlconnection.HttpURLConnectionUtil;

/**
 * 
 * @author Elias Ricken de Medeiros
 *
 */
public class RESTHttpURLConnectionManagementAPI {
	
	public static HttpURLConnection checkUserCredentials (final String username, final String password, final String options , final String restUser, final String restPswd) throws Exception{
		final String uri = "API/managementAPI/checkUserCredentials/"+username;		
		final String urlParameters = "password=" + password + "&options=" + options;
		
		final HttpURLConnection connection = HttpURLConnectionUtil.getConnection(uri, urlParameters, "application/x-www-form-urlencoded", null, restUser, restPswd);
		
		return connection;
	}
	
	public static HttpURLConnection isUserAdmin(final String username, final String options , final String restUser, final String restPswd) throws Exception{
		final String uri = "API/managementAPI/isUserAdmin/" + username;		
		final String urlParameters = "options=" + options;		
    return HttpURLConnectionUtil.getConnection(uri, urlParameters, "application/x-www-form-urlencoded", null, restUser, restPswd);
	}
	
	public static HttpURLConnection removeJar(final String jarName, final String options , final String restUser, final String restPswd) throws Exception {
		final String uri = "API/managementAPI/removeJar/" + jarName;
		final String parameters = "options=" + options;
		return HttpURLConnectionUtil.getConnection(uri, parameters, "application/x-www-form-urlencoded", null, restUser, restPswd);
	}
	
	public static HttpURLConnection deploy(final byte [] barContent, final String options , final String restUser, final String restPswd) throws Exception {
	  final String uri = "API/managementAPI/deploy";
	  final Map<String, String> optionsMap = new HashMap<String, String>();
    optionsMap.put("options", options);
    return HttpURLConnectionUtil.getConnection(uri, barContent, "application/octet-stream", optionsMap, restUser, restPswd);
	}

	public static HttpURLConnection deleteProcess(final String processDefinitionUUID, final String options , final String restUser, final String restPswd) throws Exception {
	  final String uri = "API/managementAPI/deleteProcess/" + processDefinitionUUID;
	  final String parameters = "options=" + options;
	  return HttpURLConnectionUtil.getConnection(uri, parameters, "application/x-www-form-urlencoded", null, restUser, restPswd);
	}
	
}
