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
import org.ow2.bonita.facade.rest.apachehttpclient.ApacheHttpClientUtil;

/**
 * 
 * @author Elias Ricken de Medeiros
 *
 */
public class ApacheHttpClientCommandAPI {
	public static HttpResponse execute(String command, String processUUID, String options,
			String restUser, String restPassword) throws URISyntaxException,
			ClientProtocolException, IOException {
		
		String uri = "API/commandAPI/execute/" + processUUID;
		
		List<NameValuePair> formparams = new ArrayList<NameValuePair>();
		formparams.add(new BasicNameValuePair("command", command));
		formparams.add(new BasicNameValuePair("options", options));
		
		HttpResponse httpresponse = ApacheHttpClientUtil.executeHttpConnection(uri, formparams, options, restUser, restPassword);		
		return httpresponse;
	}
	
	public static HttpResponse execute(String command, String options,
			String restUser, String restPassword) throws URISyntaxException,
			ClientProtocolException, IOException {
		String uri = "API/commandAPI/execute";
		List<NameValuePair> formparams = new ArrayList<NameValuePair>();
		formparams.add(new BasicNameValuePair("command", command));
		formparams.add(new BasicNameValuePair("options", options));
		
		HttpResponse httpresponse = ApacheHttpClientUtil.executeHttpConnection(uri, formparams, options, restUser, restPassword);
		
		return httpresponse;
	}
}
