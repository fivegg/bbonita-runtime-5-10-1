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

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Map;

import org.ow2.bonita.facade.rest.HttpRESTUtil;
import org.ow2.bonita.util.Base64;

/**
 * 
 * @author Elias Ricken de Medeiros
 *
 */
public class HttpURLConnectionUtil {
		
	public static HttpURLConnection getConnection (final String uri, final String parameters, 
	    final String contentType, final Map<String, String> headers, final String restUserName, final String restPswd) throws Exception{
		final HttpURLConnection connection = getConnection(uri, contentType, headers, restUserName, restPswd);
		writeBody(parameters, connection);
		return connection;
	}

	public static HttpURLConnection getConnection (final String uri, final byte[] content, 
	    final String contentType, final Map<String, String> headers, final String restUserName, final String restPsswd) throws Exception{
	  final HttpURLConnection connection = getConnection(uri, contentType, headers, restUserName, restPsswd);
	  writeBody(content, connection);
	  return connection;
	}

  private static void writeBody(final String parameters, final HttpURLConnection connection) throws IOException {
    final DataOutputStream output = new DataOutputStream(connection.getOutputStream());
		output.writeBytes(parameters);
		output.flush();
		output.close();
  }

  private static void writeBody(final byte [] content, final HttpURLConnection connection) throws IOException {
    final DataOutputStream output = new DataOutputStream(connection.getOutputStream());
    output.write(content);
    output.flush();
    output.close();
  }

  private static HttpURLConnection getConnection(final String uri, final String contentType, final Map<String, String> headers, final String restUserName, final String restPswd) throws MalformedURLException,
      IOException, ProtocolException {
    final String serverAddress = HttpRESTUtil.getRESTServerAddress();
		final URL url = new URL(serverAddress + uri);
		final HttpURLConnection connection = (HttpURLConnection)url.openConnection();
		
		connection.setUseCaches (false);
    connection.setDoInput(true);
		connection.setDoOutput(true);
		
		connection.setInstanceFollowRedirects(false);
		connection.setRequestMethod("POST");
		connection.setRequestProperty("Content-Type", contentType);
		final String usernamePasswordHeader = restUserName + ":" + restPswd;
		connection.setRequestProperty("Authorization", "Basic " + Base64.encodeBytes(usernamePasswordHeader.getBytes()));
		
		if (headers!= null) {
		  for (final String headerKey : headers.keySet()) {
        connection.setRequestProperty(headerKey, headers.get(headerKey));
      }
		}
    return connection;
  }
	
	public static String getResponseContent (final HttpURLConnection connection) throws IOException {
		final InputStream is = connection.getInputStream();
    final BufferedReader reader = new BufferedReader(new InputStreamReader(is));
    String line;
    final StringBuffer response = new StringBuffer();
    try {
      while((line = reader.readLine()) != null) {
        response.append(line);
        response.append('\n');
      }
    } finally {
      reader.close();
      is.close();
    }
    return response.toString().trim();
	}
	
}
