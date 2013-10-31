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
import org.apache.http.message.BasicNameValuePair;
import org.ow2.bonita.facade.def.element.BusinessArchive;
import org.ow2.bonita.facade.rest.apachehttpclient.ApacheHttpClientUtil;
import org.ow2.bonita.util.Misc;

/**
 * 
 * @author Elias Ricken de Medeiros
 * 
 */
public class ApacheHttpClientManagementAPI {
	public static HttpResponse deploy(BusinessArchive businessArchive, String options,
			String restUser, String restPassword) throws URISyntaxException,
			ClientProtocolException, IOException, ClassNotFoundException {
		
		String uri = "API/managementAPI/deploy";
		byte[] content = Misc.serialize(businessArchive);
		
		HttpResponse httpresponse = ApacheHttpClientUtil.executeHttpConnection(uri, content, options, restUser, restPassword);
		return httpresponse;
	}
	
	public static HttpResponse deleteProcess(String processUUID, String options,
			String username, String password) throws URISyntaxException,
			ClientProtocolException, IOException {
		
		String uri = "API/managementAPI/deleteProcess/" + processUUID;
		
		List<NameValuePair> formparams = new ArrayList<NameValuePair>();		
		formparams.add(new BasicNameValuePair("options", options));
		
		HttpResponse httpresponse = ApacheHttpClientUtil.executeHttpConnection(uri, formparams, options, username, password);
		return httpresponse;
	}
	
	public static HttpResponse disable(String processUUID, String options,
			String username, String password) throws URISyntaxException,
			ClientProtocolException, IOException {
		String uri = "API/managementAPI/disable/" +	processUUID;
		
		List<NameValuePair> formparams = new ArrayList<NameValuePair>();		
		formparams.add(new BasicNameValuePair("options", options));
		
		HttpResponse httpresponse = ApacheHttpClientUtil.executeHttpConnection(uri, formparams, options, username, password);
		return httpresponse;
	}
	
	public static HttpResponse isUserAdmin (String username, String options, String restUser, String restPassword) throws URISyntaxException, IOException{
		String uri = "API/managementAPI/isUserAdmin/" + username;
		
		List<NameValuePair> formparams = new ArrayList<NameValuePair>();		
		formparams.add(new BasicNameValuePair("options", options));
		
		HttpResponse response = ApacheHttpClientUtil.executeHttpConnection(uri, formparams, options, restUser, restPassword);
		return response;
	}
	
	public static HttpResponse deployJar (String jarName, byte[] jar, String options, String restUser, String restPassword) throws URISyntaxException, IOException, ClassNotFoundException{
		String uri = "API/managementAPI/deployJar/" + jarName;
		return ApacheHttpClientUtil.executeHttpConnection(uri, jar, options, restUser, restPassword);
	}
	
	public static HttpResponse removeJar (String jarName, String options, String restUser, String restPassword) throws URISyntaxException, IOException{
		String uri = "API/managementAPI/removeJar/" + jarName;
		
		List<NameValuePair> formparams = new ArrayList<NameValuePair>();		
		formparams.add(new BasicNameValuePair("options", options));
		
		return ApacheHttpClientUtil.executeHttpConnection(uri, formparams, options, restUser, restPassword);
	}
}
