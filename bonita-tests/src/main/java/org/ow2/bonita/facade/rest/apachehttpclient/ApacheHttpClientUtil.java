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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.ow2.bonita.facade.rest.HttpRESTUtil;

/**
 * 
 * @author Elias Ricken de Medeiros, Matthieu Chaffotte
 *
 */
public class ApacheHttpClientUtil {

	public static HttpClient getHttpClient (String serverAddress, String username, String password)
	throws URISyntaxException{
		URI serverURI = new URI(serverAddress);
		DefaultHttpClient client = new DefaultHttpClient();
		AuthScope authScope = new AuthScope(serverURI.getHost(), serverURI.getPort(), AuthScope.ANY_REALM, AuthScope.ANY_SCHEME);
		UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(username, password);
		client.getCredentialsProvider().setCredentials(authScope, credentials);
		return client;
	}

	public static HttpResponse executeHttpConnection (String uri, List<NameValuePair> formParams, String options, String username, String password)
	throws URISyntaxException, IOException, IOException{
		UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formParams, "UTF-8");
		String serverAddress = HttpRESTUtil.getRESTServerAddress();
		HttpPost post = new HttpPost(serverAddress + uri);
		post.setEntity(entity);		
		HttpClient client = ApacheHttpClientUtil.getHttpClient(serverAddress, username, password);
		HttpResponse httpresponse = client.execute(post);
		return httpresponse;
	}
	
	public static HttpResponse executeHttpConnection (String uri, byte[] content, String optionsHeader,
      String username, String password) throws URISyntaxException, IOException, IOException, ClassNotFoundException{
   String serverAddress = HttpRESTUtil.getRESTServerAddress();
   HttpPost post = new HttpPost(serverAddress + uri);
   
   ByteArrayEntity entity = new ByteArrayEntity(content);
   entity.setContentType("application/octet-stream");
   post.setHeader("options", optionsHeader);
   post.setEntity(entity);   

   HttpClient client = ApacheHttpClientUtil.getHttpClient(serverAddress, username, password);
   HttpResponse httpresponse = client.execute(post);
   return httpresponse;
 }

	public static String getResponseContent(HttpResponse httpresponse)
	throws IOException {
		BufferedReader responseReader = new BufferedReader(
		    new InputStreamReader(httpresponse.getEntity().getContent()));
		String line = responseReader.readLine();
		StringBuilder builder = new StringBuilder();
		try {
		  while (line != null) {
		    builder.append(line).append("\n");
	      line = responseReader.readLine();
	    }
		} catch (IOException e) {
		  responseReader.close();
    }
		return builder.toString().trim();
	}

}
