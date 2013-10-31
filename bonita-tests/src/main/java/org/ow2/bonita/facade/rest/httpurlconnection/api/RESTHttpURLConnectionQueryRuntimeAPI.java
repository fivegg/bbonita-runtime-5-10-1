/**
 * Copyright (C) 2012  BonitaSoft S.A.
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
 * @author Elias Ricken de Medeiros
 *
 */
public class RESTHttpURLConnectionQueryRuntimeAPI {
  
  public static HttpURLConnection getProcessInstanceVariable(final String processInstanceUUID, final String variableName, final String options, final String restUser, final String restPswd) throws Exception{
    final String uri = "API/queryRuntimeAPI/getProcessInstanceVariable/"+processInstanceUUID;    
    final String urlParameters = "variableId=" + variableName + "&options=" + options;
    
    final HttpURLConnection connection = HttpURLConnectionUtil.getConnection(uri, urlParameters, "application/x-www-form-urlencoded", null, restUser, restPswd);
    
    return connection;
  }

}
