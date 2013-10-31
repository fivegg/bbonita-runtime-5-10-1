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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.ow2.bonita.facade.rest.apachehttpclient.RESTApacheHttpClientConnectionAuthenticationTest;
import org.ow2.bonita.facade.rest.apachehttpclient.RESTHTTPClientManagementAPITest;
import org.ow2.bonita.facade.rest.apachehttpclient.RESTVariableTest;
import org.ow2.bonita.facade.rest.httpurlconnection.RESTHttpURLConnectionAuthenticationTest;
import org.ow2.bonita.facade.rest.httpurlconnection.RESTHttpURLConnectionManagementAPITest;
import org.ow2.bonita.facade.rest.httpurlconnection.RESTHttpURLConnectionVariablesTest;
import org.ow2.bonita.facade.rest.javaclient.DeployBigArchiveTest;
import org.ow2.bonita.facade.rest.javaclient.MultiThreadCallsTest;

/**
 * 
 * @author Elias Ricken de Medeiros
 *
 */
public class RESTTests extends TestCase {
	private RESTTests() {}
	
	public static Test suite() throws Exception {
    final TestSuite suite = new TestSuite(RESTTests.class.getName());
    
    suite.addTestSuite(RESTAuthenticationTest.class);
    suite.addTestSuite(DeployBigArchiveTest.class);
    suite.addTestSuite(MultiThreadCallsTest.class);
    
    //apache HttpClient
    suite.addTestSuite(RESTHTTPClientManagementAPITest.class); 
    suite.addTestSuite(RESTApacheHttpClientConnectionAuthenticationTest.class);
    suite.addTestSuite(RESTVariableTest.class);
    
    //HttpURLConnection
    suite.addTestSuite(RESTHttpURLConnectionManagementAPITest.class);
    suite.addTestSuite(RESTHttpURLConnectionAuthenticationTest.class);
    suite.addTestSuite(RESTHttpURLConnectionVariablesTest.class);
    
    return suite;
	}
}
