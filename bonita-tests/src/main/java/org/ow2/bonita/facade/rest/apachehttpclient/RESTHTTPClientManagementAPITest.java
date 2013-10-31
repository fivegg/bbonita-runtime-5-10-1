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

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.Response.Status;

import org.apache.http.HttpResponse;
import org.bonitasoft.connectors.bonita.resolvers.ProcessInitiatorRoleResolver;
import org.ow2.bonita.facade.Accept;
import org.ow2.bonita.facade.GetProcessIdCommand;
import org.ow2.bonita.facade.ManagementAPITest;
import org.ow2.bonita.facade.MyConnector;
import org.ow2.bonita.facade.def.element.BusinessArchive;
import org.ow2.bonita.facade.def.element.HookDefinition.Event;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.rest.RESTAPITestCase;
import org.ow2.bonita.facade.rest.apachehttpclient.api.ApacheHttpClientCommandAPI;
import org.ow2.bonita.facade.rest.apachehttpclient.api.ApacheHttpClientManagementAPI;
import org.ow2.bonita.facade.rest.apachehttpclient.api.ApacheHttpClientQueryRuntimeAPI;
import org.ow2.bonita.facade.rest.apachehttpclient.api.ApacheHttpClientRuntimeAPI;
import org.ow2.bonita.facade.rest.wrapper.RESTCommand;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.util.BonitaException;
import org.ow2.bonita.util.BusinessArchiveFactory;
import org.ow2.bonita.util.Misc;
import org.ow2.bonita.util.ProcessBuilder;
import org.ow2.bonita.util.xml.XStreamUtil;

import com.thoughtworks.xstream.XStream;

/**
 * 
 * @author Elias Ricken de Medeiros
 *
 */
public class RESTHTTPClientManagementAPITest extends RESTAPITestCase {
	
	
	private static final String XPDLFILE = "testManagementAPI.xpdl";
  private static XStream xstream = XStreamUtil.getDefaultXstream();

  private void connectorInAJar(boolean inJar) throws Exception {
    ProcessDefinition process = ProcessBuilder.createProcess("myProcess", "1.0")
    .addGroup("initiator")
      .addGroupResolver(ProcessInitiatorRoleResolver.class.getName())
    .addStringData("myData", "initialValue")
    .addHumanTask("myTask", "initiator")
    .addConnector(Event.taskOnReady, MyConnector.class.getName(), true)
      .addInputParameter("variableId", "myData")
      .addInputParameter("newValue", "myNewValue")
    .done();

    //xml file does not have the right name in the source code to ensure it is not found in the bonita-tests.jar
    String base = ManagementAPITest.class.getPackage().getName().replace('.', '/') + "/";

    byte[] xmlValue = Misc.getAllContentFrom(ManagementAPITest.class.getResource("MyConnectorXml.xml"));
    String xmlKey = base + "MyConnector.xml";

    byte[] clazzValue = Misc.getAllContentFrom(ManagementAPITest.class.getResource("MyConnector.bytecode"));
    String clazzKey = ManagementAPITest.class.getPackage().getName().replace(".", "/") + "/" + "MyConnector.class";

    Map<String, byte[]> resources = new HashMap<String, byte[]>();
    if (inJar) {
      Map<String, byte[]> jarResources = new HashMap<String, byte[]>();
      jarResources.put(xmlKey, xmlValue);
      jarResources.put(clazzKey, clazzValue);
      byte[] jar = Misc.generateJar(jarResources);
      resources.put("myJar.jar", jar);
    } else {
      resources.put(xmlKey, xmlValue);
      resources.put(clazzKey, clazzValue);
    }
    BusinessArchive businessArchive = getBusinessArchive(process, resources, ProcessInitiatorRoleResolver.class);
    
    HttpResponse deployResponse = ApacheHttpClientManagementAPI.deploy(businessArchive, getUserForTheOptionsMap(), REST_USER, REST_PSWD);
    assertEquals(Status.OK.getStatusCode(), deployResponse.getStatusLine().getStatusCode());

    String strProcessDefinition = ApacheHttpClientUtil.getResponseContent(deployResponse);
    
    XStream xstream = XStreamUtil.getDefaultXstream();
    ProcessDefinition processDefinition = (ProcessDefinition)xstream.fromXML(strProcessDefinition);
    assertEquals(processDefinition.getName(), process.getName());
    
    HttpResponse instanciateResponse = ApacheHttpClientRuntimeAPI.instantiateProcess(process.getUUID().toString(), 
        getUserForTheOptionsMap(), REST_USER, REST_PSWD);
    assertEquals(Status.OK.getStatusCode(), instanciateResponse.getStatusLine().getStatusCode());
    String xInstanceUUID = ApacheHttpClientUtil.getResponseContent(instanciateResponse);
    
    ProcessInstanceUUID instanceUUID = (ProcessInstanceUUID)xstream.fromXML(xInstanceUUID);

    HttpResponse getProcessInstanceVariableResponse = ApacheHttpClientQueryRuntimeAPI.getProcessInstanceVariable(
    		instanceUUID.toString(), "myData", getUserForTheOptionsMap(), REST_USER, REST_PSWD);
    assertEquals(Status.OK.getStatusCode(), getProcessInstanceVariableResponse.getStatusLine().getStatusCode());
    String xMyData = ApacheHttpClientUtil.getResponseContent(getProcessInstanceVariableResponse);
    
    assertNotNull(xMyData);
    assertEquals("myNewValue", xMyData);

    HttpResponse deleteProcessResponce = ApacheHttpClientManagementAPI.deleteProcess(
    		process.getUUID().toString(), getUserForTheOptionsMap(), REST_USER, REST_PSWD);
    assertEquals(Status.NO_CONTENT.getStatusCode(), deleteProcessResponce.getStatusLine().getStatusCode());    
  }

  public void testConnectorInAJarInsideBar() throws Exception {
    connectorInAJar(true);
  }

  public void testConnectorInABar() throws Exception {
    connectorInAJar(false);
  }

  public void testDeployCommandInBar() throws BonitaException, IOException, ClassNotFoundException, URISyntaxException {
    final String processId = "testManagementAPICommand";
    final String xpdlFile = processId + ".xpdl";

    @SuppressWarnings("rawtypes")
    final Class[] classes = {GetProcessIdCommand.class};
    final URL url = ManagementAPITest.class.getResource(xpdlFile);

    ProcessDefinition clientProcess = ProcessBuilder.createProcessFromXpdlFile(url);
    BusinessArchive businessArchive = BusinessArchiveFactory.getBusinessArchive(clientProcess, classes);
    
    HttpResponse deployResponse;
		deployResponse = ApacheHttpClientManagementAPI.deploy(businessArchive, getUserForTheOptionsMap(), REST_USER, REST_PSWD);
    assertEquals(Status.OK.getStatusCode(), deployResponse.getStatusLine().getStatusCode());
		String xmlProcess = ApacheHttpClientUtil.getResponseContent(deployResponse);		
		
		final ProcessDefinition process = (ProcessDefinition) xstream.fromXML(xmlProcess);		
    
    assertNotNull(process);

    final ProcessDefinitionUUID processUUID = process.getUUID();
    try {
    	RESTCommand<String> restCommand = new RESTCommand<String>(new GetProcessIdCommand(processUUID));
    	HttpResponse executeResponse = ApacheHttpClientCommandAPI.execute(restCommand.toString(), processUUID.toString(), getUserForTheOptionsMap(), REST_USER, REST_PSWD);
    	assertEquals(Status.OK.getStatusCode(), executeResponse.getStatusLine().getStatusCode());
    	final String returnedProcessId = ApacheHttpClientUtil.getResponseContent(executeResponse);      
      assertEquals(processId, returnedProcessId);
    } finally {
    	
    	HttpResponse disableResponse = ApacheHttpClientManagementAPI.disable(processUUID.toString(), getUserForTheOptionsMap(), REST_USER, REST_PSWD);
    	assertEquals(Status.NO_CONTENT.getStatusCode(), disableResponse.getStatusLine().getStatusCode());
      
    	HttpResponse deleteProcessResponse = ApacheHttpClientManagementAPI.deleteProcess(processUUID.toString(), getUserForTheOptionsMap(), REST_USER, REST_PSWD);
    	assertEquals(Status.NO_CONTENT.getStatusCode(), deleteProcessResponse.getStatusLine().getStatusCode());
    }
  }
  
  //  Testing deploy with parameters:
  //     - org.ow2.bonita.deployment.Deployment
  //    - java.net.URL, java.lang.Class<?>[]

  public void testDeploy() throws Exception { 
		final URL xpdlUrl = ManagementAPITest.class.getResource(XPDLFILE);
		BusinessArchive businessArchive = getBusinessArchiveFromXpdl(xpdlUrl, Accept.class);
    
		HttpResponse httpresponse = ApacheHttpClientManagementAPI.deploy(businessArchive, getUserForTheOptionsMap(), REST_USER, REST_PSWD);			
		assertEquals(Status.OK.getStatusCode(), httpresponse.getStatusLine().getStatusCode());
		
		String xmlProcessDefinition = ApacheHttpClientUtil.getResponseContent(httpresponse);
		xstream = XStreamUtil.getDefaultXstream();
		
		ProcessDefinition processDefinition = (ProcessDefinition)xstream.fromXML(xmlProcessDefinition); 
		HttpResponse disableResponse = ApacheHttpClientManagementAPI.disable(processDefinition.getUUID().toString(), getUserForTheOptionsMap(), REST_USER, REST_PSWD);
		assertEquals(Status.NO_CONTENT.getStatusCode(), disableResponse.getStatusLine().getStatusCode());
	  
		HttpResponse deleteProcessResponse = ApacheHttpClientManagementAPI.deleteProcess(
				processDefinition.getUUID().toString(), getUserForTheOptionsMap(), REST_USER, REST_PSWD);
		
		assertEquals(Status.NO_CONTENT.getStatusCode(), deleteProcessResponse.getStatusLine().getStatusCode());
	}

}
