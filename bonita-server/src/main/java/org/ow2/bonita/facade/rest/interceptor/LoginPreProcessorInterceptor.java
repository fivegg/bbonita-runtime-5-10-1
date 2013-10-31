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
package org.ow2.bonita.facade.rest.interceptor;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;

import org.jboss.resteasy.annotations.interception.Precedence;
import org.jboss.resteasy.annotations.interception.ServerInterceptor;
import org.jboss.resteasy.core.ResourceMethod;
import org.jboss.resteasy.core.ServerResponse;
import org.jboss.resteasy.spi.Failure;
import org.jboss.resteasy.spi.HttpRequest;
import org.jboss.resteasy.spi.interception.PreProcessInterceptor;
import org.ow2.bonita.facade.APIAccessor;
import org.ow2.bonita.identity.auth.APIMethodsSecurity;
import org.ow2.bonita.util.AccessorUtil;
import org.ow2.bonita.util.Base64;
import org.ow2.bonita.util.BonitaConstants;
import org.ow2.bonita.util.Misc;
import org.ow2.bonita.util.xml.XStreamUtil;

import com.thoughtworks.xstream.XStream;

/**
 * This class update the options map, to homogenize calls from Java Client and HTTP Clients. All incoming HTTP requests
 * passed here.
 * 
 * @author Elias Ricken de Medeiros
 * 
 */
@Provider
@ServerInterceptor
@Precedence("SECURITY")
public class LoginPreProcessorInterceptor implements PreProcessInterceptor {

  private static final Logger LOG = Logger.getLogger(LoginPreProcessorInterceptor.class.getName());
  private static final String AUTHENTICATION_SCHEME = "Basic";
  private static final String OPTIONS = "options";
  private static final String AUTHORIZATION_PROPERTY = "Authorization";
  private boolean optionsMapCreatedOrModified = false;
  private boolean wasEncoded = false;

  @Override
  public ServerResponse preProcess(final HttpRequest request, final ResourceMethod method) throws Failure,
      WebApplicationException {
    // get the options map from form parameter
    decodeParameters(request);

    boolean optionsInHeaders = false;
    List<String> options = request.getDecodedFormParameters().get(OPTIONS);
    if (options == null || options.isEmpty()) {
      options = request.getHttpHeaders().getRequestHeader(OPTIONS);
      optionsInHeaders = true;
    }
    Map<String, String> optionsMap = null;
    if (options != null && !options.isEmpty()) {
      if (options.size() > 1 && LOG.isLoggable(Level.WARNING)) {
        LOG.warning("Attention: there are more than one parameter named \"" + OPTIONS
            + "\". Only the first one will be used.");
      }
      String strOptions = options.get(0);
      if (!strOptions.startsWith("<")) {
        wasEncoded = true;
        try {
          strOptions = URLDecoder.decode(options.get(0), "UTF-8");
        } catch (final UnsupportedEncodingException e) {
          if (LOG.isLoggable(Level.WARNING)) {
            LOG.warning("Error while decoding " + options.get(0) + " using UTF-8: " + Misc.getStackTraceFrom(e));
          }
        }
      }
      optionsMap = getOptionsMap(strOptions);
    } else {
      optionsMap = getOptionsMap(null);
    }

    if (optionsMap.get(APIAccessor.QUERYLIST_OPTION) == null) {
      optionsMapCreatedOrModified = true;
      optionsMap.put(APIAccessor.QUERYLIST_OPTION, AccessorUtil.QUERYLIST_DEFAULT_KEY);
    }
    if (optionsMap.get(APIAccessor.DOMAIN_OPTION) == null) {
      optionsMapCreatedOrModified = true;
      optionsMap.put(APIAccessor.DOMAIN_OPTION, BonitaConstants.DEFAULT_DOMAIN);
    }

    // if it's not a secure method skip authentication
    if (!APIMethodsSecurity.isSecuredMethod(method.getMethod())) {
      // update the options map in the HTTP parameters
      if (optionsMapCreatedOrModified) {
        if (optionsInHeaders) {
          updateOptionsHeaderParam(request, optionsMap);
        } else {
          updateOptionsFormParam(request, optionsMap);
        }
      }
      return null;
    }

    final String passwordHash = optionsMap.get(APIAccessor.PASSWORD_HASH_OPTION);
    // password is on the options' map
    if (passwordHash != null) {
      // skip
      return null;
    }

    // password is on the header
    final HttpHeaders headers = request.getHttpHeaders();
    final List<String> authorization = headers.getRequestHeader(AUTHORIZATION_PROPERTY);

    // get restuser and password
    final String encodedUserPassword = authorization.get(0).replaceFirst(AUTHENTICATION_SCHEME + " ", "");
    final String usernameAndPassword = new String(Base64.decode(encodedUserPassword));

    final StringTokenizer tokenizer = new StringTokenizer(usernameAndPassword, ":");
    final String restUser = tokenizer.nextToken();
    final String restPswd = tokenizer.nextToken();

    // fill out the options map
    optionsMapCreatedOrModified = true;
    optionsMap.put(APIAccessor.REST_USER_OPTION, restUser);
    optionsMap.put(APIAccessor.PASSWORD_HASH_OPTION, restPswd);

    // update the options map in the HTTP parameters
    if (optionsInHeaders) {
      updateOptionsHeaderParam(request, optionsMap);
    } else {
      updateOptionsFormParam(request, optionsMap);
    }
    // skip
    return null;
  }

  private void decodeParameters(final HttpRequest request) {
    decodeFormParameters(request);
  }

  private void decodeFormParameters(final HttpRequest request) {

    try {
      decodeMultiValuedMap(request.getFormParameters());
    } catch (final Throwable e) {
      if (LOG.isLoggable(Level.WARNING)) {
        LOG.warning("Impossible to decode some parameters using UTF-8. Keeping encoded values: " + e);
      }
    }
    try {
      decodeMultiValuedMap(request.getDecodedFormParameters());
    } catch (final Throwable e) {
      if (LOG.isLoggable(Level.WARNING)) {
        LOG.warning("Impossible to decode some parameters using UTF-8. Keeping encoded values: " + e);
      }
    }
  }

  private void decodeMultiValuedMap(final MultivaluedMap<String, String> parameters)
      throws UnsupportedEncodingException {
    for (final Entry<String, List<String>> parameter : parameters.entrySet()) {
      final List<String> decodedValues = new ArrayList<String>();
      for (final String value : parameter.getValue()) {
        decodedValues.add(URLDecoder.decode(value, "UTF-8"));
      }
      parameters.put(parameter.getKey(), decodedValues);
    }
  }

  private void updateOptionsFormParam(final HttpRequest request, final Map<String, String> optionsMap) {
    final String options = getNonEncodedStringRepresentation(optionsMap);
    // form parameters
    request.getFormParameters().remove(OPTIONS);
    request.getFormParameters().add(OPTIONS, encodeIfNecessary(options));

    // decoded form parameters
    request.getDecodedFormParameters().remove(OPTIONS);
    request.getDecodedFormParameters().add(OPTIONS, options);
  }

  private void updateOptionsHeaderParam(final HttpRequest request, final Map<String, String> optionsMap) {
    // header
    request.getHttpHeaders().getRequestHeaders().remove(OPTIONS);
    request.getHttpHeaders().getRequestHeaders().add(OPTIONS, getStringRepresentationEncodedIfNecessary(optionsMap));
  }

  private String getStringRepresentationEncodedIfNecessary(final Map<String, String> optionsMap) {
    String strRepresentation = getNonEncodedStringRepresentation(optionsMap);
    strRepresentation = encodeIfNecessary(strRepresentation);
    return strRepresentation;
  }

  private String getNonEncodedStringRepresentation(final Map<String, String> optionsMap) {
    final XStream xstream = XStreamUtil.getDefaultXstream();
    return xstream.toXML(optionsMap);
  }

  private String encodeIfNecessary(String strRepresentation) {
    try {
      if (wasEncoded) {
        strRepresentation = URLEncoder.encode(strRepresentation, "UTF-8");
      }
    } catch (final UnsupportedEncodingException e) {
      if (LOG.isLoggable(Level.WARNING)) {
        LOG.warning("Cannot encode " + strRepresentation + " using UTF-8");
      }
    }
    return strRepresentation;
  }

  @SuppressWarnings("unchecked")
  private Map<String, String> getOptionsMap(final String strOptions) {
    if (strOptions == null || "".equals(strOptions.trim())) {
      optionsMapCreatedOrModified = true;
      return new HashMap<String, String>();
    }

    XStream xstream = null;
    // XML representation
    if (strOptions.startsWith("<")) {
      xstream = XStreamUtil.getDefaultXstream();
      try {
        return (Map<String, String>) xstream.fromXML(strOptions);
      } catch (final Exception e) {
        if (LOG.isLoggable(Level.WARNING)) {
          LOG.warning("Error while creating the java object from " + strOptions + ": " + Misc.getStackTraceFrom(e));
        }
        optionsMapCreatedOrModified = true;
        return new HashMap<String, String>();
      }
    }

    optionsMapCreatedOrModified = true;
    final Map<String, String> map = new HashMap<String, String>();
    // parse the user entry
    final StringTokenizer optionsTokenizer = new StringTokenizer(strOptions, ",");

    while (optionsTokenizer.hasMoreTokens()) {
      final String option = optionsTokenizer.nextToken();
      final StringTokenizer elementTokenizer = new StringTokenizer(option, ": ");
      if (elementTokenizer.countTokens() == 2) {
        final String key = elementTokenizer.nextToken();
        final String value = elementTokenizer.nextToken();
        if (key.equalsIgnoreCase(APIAccessor.QUERYLIST_OPTION)) {
          map.put(APIAccessor.QUERYLIST_OPTION, value);
        } else if (key.equalsIgnoreCase(APIAccessor.DOMAIN_OPTION)) {
          map.put(APIAccessor.DOMAIN_OPTION, value);
        } else if (key.equalsIgnoreCase(APIAccessor.USER_OPTION)) {
          map.put(APIAccessor.USER_OPTION, value);
        }

        else if (key.equalsIgnoreCase(APIAccessor.REST_USER_OPTION)) {
          map.put(APIAccessor.REST_USER_OPTION, value);
        }
      }
    }
    return map;
  }

}
