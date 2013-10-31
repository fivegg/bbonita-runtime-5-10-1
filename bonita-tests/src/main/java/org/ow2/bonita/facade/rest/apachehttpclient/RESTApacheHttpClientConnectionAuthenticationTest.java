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
package org.ow2.bonita.facade.rest.apachehttpclient;

import javax.ws.rs.core.Response.Status;

import org.apache.http.HttpResponse;
import org.ow2.bonita.facade.APIAccessor;
import org.ow2.bonita.facade.rest.RESTAPITestCase;
import org.ow2.bonita.facade.rest.apachehttpclient.api.ApacheHttpClientManagementAPI;

/**
 * 
 * @author Elias Ricken de Medeiros
 *
 */
public class RESTApacheHttpClientConnectionAuthenticationTest extends RESTAPITestCase {
	
	public void testActivateRESTAuthentication () throws Exception{
				
		HttpResponse isUserAdminResponse = ApacheHttpClientManagementAPI.isUserAdmin("admin", APIAccessor.USER_OPTION + ":non-existent-user", "non-existent-user", "anypassword");
		assertEquals(Status.UNAUTHORIZED.getStatusCode(), isUserAdminResponse.getStatusLine().getStatusCode());
		
		isUserAdminResponse = ApacheHttpClientManagementAPI.isUserAdmin("admin", getUserForTheOptionsMap(), REST_USER, REST_PSWD);
    assertEquals(Status.OK.getStatusCode(), isUserAdminResponse.getStatusLine().getStatusCode());

    String isAdmin = ApacheHttpClientUtil.getResponseContent(isUserAdminResponse);;
    assertEquals(isAdmin, "true");
	}
}
