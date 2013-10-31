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
package org.ow2.bonita.facade.rest.httpurlconnection;

import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import org.ow2.bonita.facade.def.element.BusinessArchive;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.rest.RESTAPITestCase;
import org.ow2.bonita.facade.rest.httpurlconnection.api.RESTHttpURLConnectionManagementAPI;
import org.ow2.bonita.facade.rest.httpurlconnection.api.RESTHttpURLConnectionQueryRuntimeAPI;
import org.ow2.bonita.facade.rest.httpurlconnection.api.RESTHttpURLConnectionRuntimeAPI;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.util.BusinessArchiveFactory;
import org.ow2.bonita.util.Misc;
import org.ow2.bonita.util.ProcessBuilder;
import org.ow2.bonita.util.xml.XStreamUtil;

import com.thoughtworks.xstream.XStream;

/**
 * @author Elias Ricken de Medeiros
 *
 */
public class RESTHttpURLConnectionVariablesTest extends RESTAPITestCase {
  
  public void testSpecialCaracterOnVariables() throws Exception {
    
    final ProcessDefinition process = 
      ProcessBuilder.createProcess("Psv2", "1.0")
        .addStringData("strVariable", "default")
        .addSystemTask("activity1")
        .done();
    
    final BusinessArchive businessArchive = BusinessArchiveFactory.getBusinessArchive(process);
    
    final HttpURLConnection response = RESTHttpURLConnectionManagementAPI.deploy(Misc.serialize(businessArchive), getUserForTheOptionsMap(), REST_USER, REST_PSWD);
    assertEquals(HttpURLConnection.HTTP_OK, response.getResponseCode());
    
    final String strProcessDefinition = HttpURLConnectionUtil.getResponseContent(response);
    
    final XStream xstream = XStreamUtil.getDefaultXstream();
    final ProcessDefinition processDefinition = (ProcessDefinition)xstream.fromXML(strProcessDefinition);
    assertEquals(processDefinition.getName(), process.getName());
    
    final Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("strVariable", "%");
    final String xmlVariables = URLEncoder.encode(xstream.toXML(variables), "UTF-8");
    
    final HttpURLConnection instProcResponse = RESTHttpURLConnectionRuntimeAPI.instantiateProcessWithVariables(processDefinition.getUUID().getValue(), xmlVariables, getUserForTheOptionsMap(), REST_USER, REST_PSWD);
    assertEquals(HttpURLConnection.HTTP_OK, instProcResponse.getResponseCode());
    final String xInstanceUUID = HttpURLConnectionUtil.getResponseContent(instProcResponse);
    
    final ProcessInstanceUUID instanceUUID = (ProcessInstanceUUID)xstream.fromXML(xInstanceUUID);

    final HttpURLConnection getProcessInstanceVariableResponse = RESTHttpURLConnectionQueryRuntimeAPI.getProcessInstanceVariable(instanceUUID.getValue().toString(), "strVariable", getUserForTheOptionsMap(), REST_USER, REST_PSWD);
    assertEquals(HttpURLConnection.HTTP_OK, getProcessInstanceVariableResponse.getResponseCode());
    
    final String strVariable = HttpURLConnectionUtil.getResponseContent(getProcessInstanceVariableResponse);
    
    assertNotNull(strVariable);
    assertEquals("%", strVariable);

    final HttpURLConnection deleteProcessResponce = RESTHttpURLConnectionManagementAPI.deleteProcess(
        process.getUUID().toString(), getUserForTheOptionsMap(), REST_USER, REST_PSWD);
    assertEquals(HttpURLConnection.HTTP_NO_CONTENT, deleteProcessResponce.getResponseCode());

  }

}
