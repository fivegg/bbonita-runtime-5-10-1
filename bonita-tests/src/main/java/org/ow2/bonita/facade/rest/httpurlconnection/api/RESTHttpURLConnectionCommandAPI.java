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
public class RESTHttpURLConnectionCommandAPI {

	public static HttpURLConnection execute (final String command, final String options, final String restUser, final String restPswd) throws Exception{
		final String uri = "API/commandAPI/execute";
		final String parameters = "command=" + command + "&options=" + options;
		return HttpURLConnectionUtil.getConnection(uri, parameters, "application/x-www-form-urlencoded", null, restUser, restPswd);		
	}
}
