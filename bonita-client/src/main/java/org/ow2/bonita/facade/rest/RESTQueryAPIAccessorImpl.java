/**
 * Copyright (C) 2010 BonitaSoft S.A.
 * BonitaSoft, 31 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 **/
package org.ow2.bonita.facade.rest;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.jboss.resteasy.client.ClientExecutor;
import org.jboss.resteasy.client.ProxyFactory;
import org.jboss.resteasy.client.core.executors.ApacheHttpClient4Executor;
import org.jboss.resteasy.plugins.providers.RegisterBuiltin;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.ow2.bonita.facade.BAMAPI;
import org.ow2.bonita.facade.QueryAPIAccessor;
import org.ow2.bonita.facade.QueryDefinitionAPI;
import org.ow2.bonita.facade.QueryRuntimeAPI;
import org.ow2.bonita.facade.internal.RESTRemoteQueryRuntimeAPI;
import org.ow2.bonita.facade.internal.RemoteBAMAPI;
import org.ow2.bonita.facade.internal.RemoteQueryDefinitionAPI;
import org.ow2.bonita.facade.rest.interceptor.BonitaClientErrorInterceptor;
import org.ow2.bonita.facade.rest.interceptor.RESTClientExecutionInterceptor;
import org.ow2.bonita.facade.rest.provider.GenericObjectProvider;
import org.ow2.bonita.facade.rest.provider.OctectStreamProvider;
import org.ow2.bonita.facade.rest.stringconverter.ActivityInstanceImplStringConverter;
import org.ow2.bonita.facade.rest.stringconverter.ActivityInstanceStringConverter;
import org.ow2.bonita.facade.rest.stringconverter.AttachementInstanceStringConverter;
import org.ow2.bonita.facade.rest.stringconverter.BusinessArchiveStringConverter;
import org.ow2.bonita.facade.rest.stringconverter.ConnectorExecutionDescriptorStringConverter;
import org.ow2.bonita.facade.rest.stringconverter.DateStringConverter;
import org.ow2.bonita.facade.rest.stringconverter.GenericObjectStringConverter;
import org.ow2.bonita.facade.rest.stringconverter.HashMapStringConverter;
import org.ow2.bonita.facade.rest.stringconverter.MapStringConverter;
import org.ow2.bonita.facade.rest.stringconverter.RuleStringConverter;
import org.ow2.bonita.util.AccessorProxyUtil;
import org.ow2.bonita.util.AccessorUtil;
import org.ow2.bonita.util.BonitaConstants;

/**
 * 
 * @author Elias Ricken de Medeiros
 * 
 */
public class RESTQueryAPIAccessorImpl implements QueryAPIAccessor {

  private static final Logger LOG = Logger.getLogger(RESTQueryAPIAccessorImpl.class.getName());

  private static ResteasyProviderFactory providerFactory;

  private static final ClientExecutor clientExecutor = new ApacheHttpClient4Executor(new DefaultHttpClient(new PoolingClientConnectionManager()));

  private static Map<String, Object> apis = new HashMap<String, Object>();

  public RESTQueryAPIAccessorImpl() {
    initializeProviderFactory();
  }

  private void initializeProviderFactory() {
    if (providerFactory == null) {
      providerFactory = ResteasyProviderFactory.getInstance();
      RegisterBuiltin.register(providerFactory);

      // String converters
      providerFactory.addStringConverter(BusinessArchiveStringConverter.class);
      providerFactory.addStringConverter(HashMapStringConverter.class);
      providerFactory.addStringConverter(MapStringConverter.class);
      providerFactory.addStringConverter(AttachementInstanceStringConverter.class);
      providerFactory.addStringConverter(ActivityInstanceStringConverter.class);
      providerFactory.addStringConverter(ActivityInstanceImplStringConverter.class);
      providerFactory.addStringConverter(GenericObjectStringConverter.class);
      providerFactory.addStringConverter(RuleStringConverter.class);
      providerFactory.addStringConverter(DateStringConverter.class);
      providerFactory.addStringConverter(ConnectorExecutionDescriptorStringConverter.class);

      // providers
      providerFactory.registerProvider(GenericObjectProvider.class);
      providerFactory.registerProvider(OctectStreamProvider.class);

      // interceptors
      providerFactory.registerProvider(RESTClientExecutionInterceptor.class);

      // client errorInterceptor
      providerFactory.addClientErrorInterceptor(new BonitaClientErrorInterceptor());
    }
  }

  /**
   * Get a proxy implementing the REST API
   * 
   * @param <T>
   *          REST interface
   * @param clazz
   * @return
   */
  protected <T> T getRESTAccess(final Class<T> clazz) {
    final String restServerAddress = System.getProperty(BonitaConstants.REST_SERVER_ADDRESS_PROPERTY);
    if (restServerAddress == null && LOG.isLoggable(Level.SEVERE)) {
      LOG.severe("The property " + BonitaConstants.REST_SERVER_ADDRESS_PROPERTY + " is null!");
    } else if (restServerAddress != null && LOG.isLoggable(Level.FINE)) {
      LOG.fine(BonitaConstants.REST_SERVER_ADDRESS_PROPERTY + ": " + restServerAddress);
    }

    return getAPISingleton(clazz, restServerAddress);
  }

  private static synchronized <T> T getAPISingleton(final Class<T> clazz, final String restServerAddress) {
    final String key = clazz.getName() + restServerAddress;

    T api = clazz.cast(apis.get(key));
    if (api == null) {
      api = ProxyFactory.create(clazz, restServerAddress, clientExecutor);
      logGetAPIProxyResult(clazz, restServerAddress, key, true);
      apis.put(key, api);
    } else {
      logGetAPIProxyResult(clazz, restServerAddress, key, false);
    }
    return api;
  }

  private static <T> void logGetAPIProxyResult(final Class<T> clazz, final String restServerAddress, final String key, final boolean found) {
    if (LOG.isLoggable(Level.FINE)) {
      final StringBuilder stb = new StringBuilder();
      stb.append("Serving class (");
      stb.append(clazz.getName());
      stb.append(") for url (");
      stb.append(restServerAddress);
      stb.append(") [key=");
      stb.append(key);
      stb.append("]: ");
      if (found) {
        stb.append("NOT FOUND YET, creating a new proxy...");
      } else {
        stb.append("FOUND.");
      }
      LOG.fine(stb.toString());
    }
  }

  @Override
  public BAMAPI getBAMAPI() {
    return getBAMAPI(AccessorUtil.QUERYLIST_DEFAULT_KEY);
  }

  @Override
  public BAMAPI getBAMAPI(final String queryList) {
    final RemoteBAMAPI remoteBAMAPI = getRESTAccess(RemoteBAMAPI.class);
    return AccessorProxyUtil.getRemoteClientAPI(BAMAPI.class, remoteBAMAPI, queryList);
  }

  @Override
  public QueryDefinitionAPI getQueryDefinitionAPI() {
    return getQueryDefinitionAPI(AccessorUtil.QUERYLIST_DEFAULT_KEY);
  }

  @Override
  public QueryDefinitionAPI getQueryDefinitionAPI(final String queryList) {
    final RemoteQueryDefinitionAPI remoteQueryRuntimeAPI = getRESTAccess(RemoteQueryDefinitionAPI.class);
    return AccessorProxyUtil.getRemoteClientAPI(QueryDefinitionAPI.class, remoteQueryRuntimeAPI, queryList);
  }

  @Override
  public QueryRuntimeAPI getQueryRuntimeAPI() {
    return getQueryRuntimeAPI(AccessorUtil.QUERYLIST_DEFAULT_KEY);
  }

  @Override
  public QueryRuntimeAPI getQueryRuntimeAPI(final String queryList) {
    final RESTRemoteQueryRuntimeAPI remoteQueryRuntimeAPI = getRESTAccess(RESTRemoteQueryRuntimeAPI.class);
    return AccessorProxyUtil.getRemoteClientAPI(QueryRuntimeAPI.class, remoteQueryRuntimeAPI, queryList);
  }

}
