/**
 * Copyright (C) 2010-2012 BonitaSoft S.A.
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
package org.ow2.bonita.facade.rest.interceptor;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;

import org.jboss.resteasy.annotations.interception.ClientInterceptor;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.spi.interception.ClientExecutionContext;
import org.jboss.resteasy.spi.interception.ClientExecutionInterceptor;
import org.ow2.bonita.facade.APIAccessor;
import org.ow2.bonita.facade.rest.stringconverter.MapStringConverter;
import org.ow2.bonita.util.Base64;

/**
 * @author Elias Ricken de Medeiros, Matthieu Chaffotte
 * 
 */
@ClientInterceptor
@Provider
public class RESTClientExecutionInterceptor implements ClientExecutionInterceptor {

  private static final Logger LOG = Logger.getLogger(RESTClientExecutionInterceptor.class.getName());
  private final static String AUTHENTICATION_SCHEME = "Basic";
  private final static String OPTIONS_PROPERTY_NAME = "options";
  private final static String AUTHORIZATION_PROPERTY = "Authorization";

  @Override
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public ClientResponse execute(final ClientExecutionContext ctx) throws Exception {
    final ClientRequest request = ctx.getRequest();
    final MultivaluedMap<String, String> formParameters = request.getFormParameters();
    List<String> options = null;
    // options in the a form parameter
    if (formParameters != null) {
      options = formParameters.get(OPTIONS_PROPERTY_NAME);
    }

    // options on the header
    if (options == null || options.isEmpty()) {
      final MultivaluedMap<String, String> headers = request.getHeaders();
      options = headers.get(OPTIONS_PROPERTY_NAME);
    }

    if ((options == null || options.isEmpty()) && LOG.isLoggable(Level.SEVERE)) {
      LOG.severe("No options map was found!");
      return ctx.proceed();
    }

    if (options.size() > 1 && LOG.isLoggable(Level.SEVERE)) {
      LOG.severe("More than one parameter named options was found!");
    }

    final MapStringConverter converter = new MapStringConverter();

    final Map<String, String> optionsMap = (Map<String, String>) converter.fromString(options.get(0));
    final String userName = optionsMap.get(APIAccessor.REST_USER_OPTION);
    if (userName != null && !userName.trim().equals("")) {
      final String passWord = optionsMap.get(APIAccessor.PASSWORD_HASH_OPTION);
      final String encodedUserAndPswd = Base64.encodeBytes((userName + ":" + passWord).getBytes());
      request.getHeaders().add(AUTHORIZATION_PROPERTY, AUTHENTICATION_SCHEME + " " + encodedUserAndPswd);
    }
    encodeParameters(request);
    return ctx.proceed();
  }

  private void encodeParameters(final ClientRequest request) {
    encodeFormParameters(request);
    encodeOptionsHeaderParameter(request);
  }

  private void encodeOptionsHeaderParameter(final ClientRequest request) {
    final MultivaluedMap<String, String> headers = request.getHeaders();
    final List<String> options = headers.get(OPTIONS_PROPERTY_NAME);
    if (options != null && options.size() > 0) {
      final String strOptions = options.get(0);
      String encodedStrOptions = null;
      try {
        encodedStrOptions = URLEncoder.encode(strOptions, "UTF-8");
      } catch (final UnsupportedEncodingException e) {
        if (LOG.isLoggable(Level.WARNING)) {
          LOG.log(Level.WARNING, "Impossible to encode some parameters using UTF-8", e);
        }
      }
      headers.remove(OPTIONS_PROPERTY_NAME);
      headers.add(OPTIONS_PROPERTY_NAME, encodedStrOptions);
    }
  }

  private void encodeFormParameters(final ClientRequest request) {
    try {
      final MultivaluedMap<String, String> formParameters = request.getFormParameters();
      encodeMultivalueMap(formParameters);
    } catch (final UnsupportedEncodingException e) {
      if (LOG.isLoggable(Level.WARNING)) {
        LOG.log(Level.WARNING, "Impossible to encode some parameters using UTF-8", e);
      }
    }
  }

  private void encodeMultivalueMap(final MultivaluedMap<String, String> parameters) throws UnsupportedEncodingException {
    for (final Entry<String, List<String>> parameter : parameters.entrySet()) {
      final List<String> encodedValues = new ArrayList<String>();
      for (final String value : parameter.getValue()) {
        encodedValues.add(URLEncoder.encode(value, "UTF-8"));
      }
      parameters.put(parameter.getKey(), encodedValues);
    }
  }

}
