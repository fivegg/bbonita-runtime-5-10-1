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
package org.ow2.bonita.facade.rest.httpurlconnection;

import java.net.HttpURLConnection;

import org.ow2.bonita.facade.rest.RESTAPITestCase;
import org.ow2.bonita.facade.rest.httpurlconnection.api.RESTHttpURLConnectionManagementAPI;

/**
 * 
 * @author Elias Ricken de Medeiros
 *
 */
public class RESTHttpURLConnectionManagementAPITest extends RESTAPITestCase {
	public void testCheckUserCredentials() throws Exception {
		//valid user/password
		HttpURLConnection response = RESTHttpURLConnectionManagementAPI.checkUserCredentials("admin", "bpm", getUserForTheOptionsMap(), REST_USER, REST_PSWD);
		assertEquals(HttpURLConnection.HTTP_OK, response.getResponseCode());
		
		String responseContent = HttpURLConnectionUtil.getResponseContent(response);
		assertEquals("true", responseContent);
    
    //invalid user/password
    response = RESTHttpURLConnectionManagementAPI.checkUserCredentials("admin", "invalidPassword", null, REST_USER, REST_PSWD);
    assertEquals(HttpURLConnection.HTTP_OK, response.getResponseCode());
    
    responseContent = HttpURLConnectionUtil.getResponseContent(response);
    assertEquals("false", responseContent);
  }
}
