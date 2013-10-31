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

import org.ow2.bonita.facade.rest.httpurlconnection.HttpURLConnectionUtil;

/**
 * 
 * @author Elias Ricken de Medeiros
 *
 */
public class RESTHttpURLConnectionIdentityAPI {
	public static HttpURLConnection getUserByUUID (final String userUUID, final String options, final String restUser, final String restPswd) throws Exception{
		final String uri = "API/identityAPI/getUserByUUID/" + userUUID;
		final String parameters = "options=" + options;
		return HttpURLConnectionUtil.getConnection(uri, parameters, "application/x-www-form-urlencoded", null, restUser, restPswd);		
	}
	
	public static HttpURLConnection addUser (final String username, final String password, final String options, final String restUser, final String restPswd) throws Exception{
		final String uri = "API/identityAPI/addUser";
		final String parameters = "username=" + username + "&password=" + password + "&options=" + options;
		return HttpURLConnectionUtil.getConnection(uri, parameters, "application/x-www-form-urlencoded", null, restUser, restPswd);		
	}
	
	public static HttpURLConnection removeUserByUUID (final String username, final String options, final String restUser, final String restPswd) throws Exception{
		final String uri = "API/identityAPI/removeUserByUUID/" + username;
		final String parameters = "options=" + options;
		return HttpURLConnectionUtil.getConnection(uri, parameters, "application/x-www-form-urlencoded", null, restUser, restPswd);		
	}
}
