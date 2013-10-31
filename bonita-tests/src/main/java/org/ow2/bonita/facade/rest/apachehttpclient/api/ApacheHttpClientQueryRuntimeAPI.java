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
package org.ow2.bonita.facade.rest.apachehttpclient.api;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.ow2.bonita.facade.rest.HttpRESTUtil;
import org.ow2.bonita.facade.rest.apachehttpclient.ApacheHttpClientUtil;

/**
 * 
 * @author Elias Ricken de Medeiros
 *
 */
public class ApacheHttpClientQueryRuntimeAPI {
	public static HttpResponse getProcessInstanceVariable(String instanceUUID, String variableName,
			String options, String username, String password) throws URISyntaxException,
			ClientProtocolException, IOException {
		List<NameValuePair> formparams = new ArrayList<NameValuePair>();
		formparams.add(new BasicNameValuePair("variableId", variableName));
		formparams.add(new BasicNameValuePair("options", options));
		UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");

		String serverAddress = HttpRESTUtil.getRESTServerAddress();
		HttpPost getProcessInstanceVariable = new HttpPost(serverAddress + "API/queryRuntimeAPI/getProcessInstanceVariable/" +
				instanceUUID);
		getProcessInstanceVariable.setEntity(entity);		

		HttpClient client = ApacheHttpClientUtil.getHttpClient(serverAddress, username, password);
		HttpResponse httpresponse = client.execute(getProcessInstanceVariable);
		
		return httpresponse;
	}
	
	public static HttpResponse getVariables(final String activityInstanceUUID,
      String options, String username, String password) throws URISyntaxException,
      ClientProtocolException, IOException {
    List<NameValuePair> formparams = new ArrayList<NameValuePair>();
    formparams.add(new BasicNameValuePair("options", options));
    UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");

    String serverAddress = HttpRESTUtil.getRESTServerAddress();
    HttpPost getProcessInstanceVariable = new HttpPost(serverAddress + "API/queryRuntimeAPI/getVariables/" +
        activityInstanceUUID);
    getProcessInstanceVariable.setEntity(entity);   

    HttpClient client = ApacheHttpClientUtil.getHttpClient(serverAddress, username, password);
    HttpResponse httpresponse = client.execute(getProcessInstanceVariable);
    
    return httpresponse;
  }
	
	
	public static HttpResponse getLightActivities(final String instanceUUID,
      String options, String username, String password) throws URISyntaxException,
      ClientProtocolException, IOException {
    List<NameValuePair> formparams = new ArrayList<NameValuePair>();
    formparams.add(new BasicNameValuePair("options", options));
    UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");

    String serverAddress = HttpRESTUtil.getRESTServerAddress();
    HttpPost getProcessInstanceVariable = new HttpPost(serverAddress + "API/queryRuntimeAPI/getLightActivityInstances/" +
        instanceUUID);
    getProcessInstanceVariable.setEntity(entity);   

    HttpClient client = ApacheHttpClientUtil.getHttpClient(serverAddress, username, password);
    HttpResponse httpresponse = client.execute(getProcessInstanceVariable);
    
    return httpresponse;
  }
}
