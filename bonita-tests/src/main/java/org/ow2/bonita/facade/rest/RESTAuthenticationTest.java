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

import java.io.File;

import javax.security.auth.login.Configuration;
import javax.security.auth.login.LoginException;

import org.jboss.resteasy.client.ClientResponseFailure;
import org.ow2.bonita.APITestCase;
import org.ow2.bonita.facade.exception.UserAlreadyExistsException;
import org.ow2.bonita.facade.exception.UserNotFoundException;
import org.ow2.bonita.facade.identity.User;
import org.ow2.bonita.util.BonitaConstants;

/**
 * 
 * @author Elias Ricken de Medeiros
 *
 */
public class RESTAuthenticationTest extends APITestCase {
	
  private static String WRONG_JAAS = "jaas-with-wrong-rest-user.cfg";
  
	public void testUnauthorizedUser() throws LoginException, UserNotFoundException{
	  String actualJaas = System.getProperty(BonitaConstants.JAAS_PROPERTY);
	  changeJaasConfig(getPahtToJassWithAWrongRESTUser());
	  
	  //correct valid user and password, but jaas file containing a wrong rest user
	  loginAs("admin", "bpm");
		try {
			getIdentityAPI().getUserByUUID("unauthorizeduser");
			fail("Unauthorized user can acess the REST API");
		} catch (ClientResponseFailure e) {
			assertEquals("Error status 401 Unauthorized returned", e.getMessage());
		}
		
		changeJaasConfig(actualJaas);
		loginAs("admin", "bpm");
	}
	
	private String getPahtToJassWithAWrongRESTUser() {
	  String bonitaHome = System.getProperty(BonitaConstants.HOME);
	  return bonitaHome + File.separator + WRONG_JAAS;
	}

  private void changeJaasConfig(String jaasPath) {
    System.setProperty(BonitaConstants.JAAS_PROPERTY, jaasPath);
	  Configuration.getConfiguration().refresh();
  }
	
	public void testAuthorizedUser() throws LoginException, UserNotFoundException, UserAlreadyExistsException{
		User expectedUser = getIdentityAPI().addUser("authorizeduser", "authorized");
		loginAs("authorizeduser", "authorized");
		User user = getIdentityAPI().getUserByUUID(expectedUser.getUUID());
		assertNotNull(user);
		assertEquals(expectedUser.getUsername(), user.getUsername());
		
		loginAs("admin", "bpm");
		getIdentityAPI().removeUserByUUID(user.getUUID());
	}
	
	public void testUnauthorizedAndAuthorizedUser() throws Exception {
	  User expectedUser = getIdentityAPI().addUser("authorizeduser", "authorized");
    String actualJaas = System.getProperty(BonitaConstants.JAAS_PROPERTY);
    changeJaasConfig(getPahtToJassWithAWrongRESTUser());
    
    //correct valid user and password, but jaas file containing a wrong rest user
    loginAs("admin", "bpm");
    try {
      getIdentityAPI().getUserByUUID(expectedUser.getUUID());
      fail("Unauthorized user can acess the REST API");
    } catch (ClientResponseFailure e) {
      assertEquals("Error status 401 Unauthorized returned", e.getMessage());
    }
    
    changeJaasConfig(actualJaas);
    loginAs("admin", "bpm");
    getIdentityAPI().getUserByUUID(expectedUser.getUUID());
    
    changeJaasConfig(getPahtToJassWithAWrongRESTUser());
    
    //verify that there are no cache problems
    //correct valid user and password, but jaas file containing a wrong rest user
    loginAs("admin", "bpm");
    try {
      getIdentityAPI().getUserByUUID(expectedUser.getUUID());
      fail("Unauthorized user can acess the REST API");
    } catch (ClientResponseFailure e) {
      assertEquals("Error status 401 Unauthorized returned", e.getMessage());
    }
    
    changeJaasConfig(actualJaas);
    loginAs("admin", "bpm");
    getIdentityAPI().removeUserByUUID(expectedUser.getUUID());
  }
	
}
