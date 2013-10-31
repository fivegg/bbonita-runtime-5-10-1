/**
 * Copyright (C) 2006  Bull S. A. S.
 * Bull, Rue Jean Jaures, B.P.68, 78340, Les Clayes-sous-Bois
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA  02110-1301, USA.
 * 
 * Modified by Matthieu Chaffotte, Elias Ricken de Medeiros - BonitaSoft S.A.
 **/
package org.ow2.bonita.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.NamingException;

import org.ow2.bonita.facade.APIAccessor;
import org.ow2.bonita.facade.BAMAPI;
import org.ow2.bonita.facade.CommandAPI;
import org.ow2.bonita.facade.Context;
import org.ow2.bonita.facade.IdentityAPI;
import org.ow2.bonita.facade.ManagementAPI;
import org.ow2.bonita.facade.QueryAPIAccessor;
import org.ow2.bonita.facade.QueryDefinitionAPI;
import org.ow2.bonita.facade.QueryRuntimeAPI;
import org.ow2.bonita.facade.RepairAPI;
import org.ow2.bonita.facade.RuntimeAPI;
import org.ow2.bonita.facade.WebAPI;
import org.ow2.bonita.facade.ejb.ejb2.EJB2APIAccessorImpl;
import org.ow2.bonita.facade.ejb.ejb2.EJB2QueryAPIAccessorImpl;
import org.ow2.bonita.facade.ejb.ejb3.EJB3APIAccessorImpl;
import org.ow2.bonita.facade.ejb.ejb3.EJB3QueryAPIAccessorImpl;
import org.ow2.bonita.facade.impl.LocalAPIAccessorFactory;
import org.ow2.bonita.facade.impl.StandardAPIAccessorImpl;
import org.ow2.bonita.facade.impl.StandardQueryAPIAccessorImpl;
import org.ow2.bonita.facade.internal.RemoteRuntimeAPI;
import org.ow2.bonita.facade.rest.RESTAPIAccessorImpl;
import org.ow2.bonita.facade.rest.RESTQueryAPIAccessorImpl;

/**
 * This utility class has been provided to unify access to Bonita APIs and to
 * avoid the use of lookups in JEE deployments:
 * org.ow2.bonita.util.AccessorUtil.<br>
 * Through this class, Nova Bonita APIs can be reached in a unified way in both
 * local and remote applications.<br>
 * For that to be done, the system property called "org.ow2.bonita.api-type"
 * must be defined at client side to specify whether the APIs will be reached
 * locally or remotely<br>
 * (possible values are "Standard", "AutoDetect", "EJB2" and "EJB3", "REST").<br>
 */
public final class AccessorUtil {

  public static final String API_TYPE_PROPERTY = BonitaConstants.API_TYPE_PROPERTY;

  public static final String RUNTIMEAPI_JNDINAME = "runtimeAPI";
  public static final String MANAGEMENT_JNDINAME = "managementAPI";
  public static final String DEFINITIONAPI_JNDINAME = "definitionAPI";
  public static final String QUERYRUNTIME_JNDINAME = "queryRuntimeAPI";
  public static final String QUERYDEFINITION_JNDINAME = "queryDefinitionAPI";
  public static final String COMMANDAPI_JNDINAME = "commandAPI";
  public static final String WEBAPI_JNDINAME = "webAPI";
  public static final String IDENTITYAPI_JNDINAME = "identityAPI";
  public static final String BAMAPI_JNDINAME = "bamAPI";
  public static final String REPAIRAPI_JNDINAME = "repairAPI";

  public static final String QUERYLIST_DEFAULT_KEY = "queryList";
  public static final String QUERYLIST_JOURNAL_KEY = "journalQueryList";
  public static final String QUERYLIST_HISTORY_KEY = "historyQueryList";

  private static final Logger LOG = Logger.getLogger(AccessorUtil.class.getName());

  protected static final ThreadLocal<Context> CONTEXT = new ThreadLocal<Context>() {
    // Static initialization
    @Override
    protected Context initialValue() {
      return resetContext();
    }

  };

  public static Context resetContext() {
    Context context = null;
    try {
      context = getContextFromClasspath();
    } catch (final IOException ioe) {
      throw new BonitaRuntimeException(ioe);
    }
    if (context == null) {
      context = getContextFromSystem();
      if (context == null) {
        context = discoverContext();
      }
    }
    CONTEXT.set(context);
    return context;
  }

  private static Context getContextFromClasspath() throws IOException {
    final InputStream clientIS = ClassLoader.getSystemResourceAsStream("bonita-client.properties");
    if (clientIS != null) {
      final Properties properties = new Properties();
      try {
        properties.load(clientIS);
        final String apiType = properties.getProperty(API_TYPE_PROPERTY);
        if (apiType != null) {
          final Context context = Misc.stringToEnum(Context.class, apiType);
          if (LOG.isLoggable(Level.INFO)) {
            LOG.info("API-Type: " + context + " has been specified through classpath: " + API_TYPE_PROPERTY);
          } else {
            LOG.warning("The property " + API_TYPE_PROPERTY
                + "is not specified in the bonita-client.properties. Skip the properties");
          }
          return context;
        }
      } finally {
        clientIS.close();
      }
    }
    return null;
  }

  private static Context getContextFromSystem() {
    final String apiType = System.getProperty(API_TYPE_PROPERTY);
    if (apiType != null) {
      final Context context = Misc.stringToEnum(Context.class, apiType);
      if (LOG.isLoggable(Level.INFO)) {
        LOG.info("API-Type: " + context + " has been specified through property: " + API_TYPE_PROPERTY);
      }
      return context;
    }
    return null;
  }

  private static Context discoverContext() {
    if (LOG.isLoggable(Level.INFO)) {
      LOG.info("Property: " + API_TYPE_PROPERTY + " has not been specified for api-type. Trying to autodetect it.");
    }
    // check if the call was done from server side or client side
    try {
      LocalAPIAccessorFactory.getStandardServerAPIAccessor();
      if (LOG.isLoggable(Level.INFO)) {
        LOG.info(AccessorUtil.class.getName() + " called from server side. Using " + Context.Standard + " context.");
      }
      return Context.Standard;
    } catch (final BonitaRuntimeException e) {
      // not called from server
      if (LOG.isLoggable(Level.INFO)) {
        LOG.info(AccessorUtil.class.getName() + " called from client side. Trying to autodetect apiType.");
      }
      try {
        RemoteRuntimeAPI.class.cast(Misc.lookup(RUNTIMEAPI_JNDINAME, null));
        if (LOG.isLoggable(Level.INFO)) {
          LOG.info(Context.EJB3 + " context found: EJB3 api-type will be used.");
        }
        return Context.EJB3;
      } catch (final NamingException ne) {
        if (LOG.isLoggable(Level.INFO)) {
          LOG.info("No context found: assuming J2SE. Standard api-type will be used."
              + " Note that this may not be what you want."
              + " In this case, either specify the api-type in the environment or via the property: "
              + API_TYPE_PROPERTY);
        }
        return Context.Standard;
      } catch (final ClassCastException cceEJB3) {
        return Context.EJB2;
      }
    }
  }

  private AccessorUtil() {
  }

  public static QueryAPIAccessor getQueryAPIAccessor(final Hashtable<String, String> jndiEnvironment) {
    switch (AccessorUtil.CONTEXT.get()) {
      case EJB2:
        return new EJB2QueryAPIAccessorImpl(jndiEnvironment);
      case EJB3:
        return new EJB3QueryAPIAccessorImpl(jndiEnvironment);
      case REST:
        return new RESTQueryAPIAccessorImpl();
      default:
        return new StandardQueryAPIAccessorImpl();
    }
  }

  public static APIAccessor getAPIAccessor(final Hashtable<String, String> jndiEnvironment) {
    switch (AccessorUtil.CONTEXT.get()) {
      case EJB2:
        return new EJB2APIAccessorImpl(jndiEnvironment);
      case EJB3:
        return new EJB3APIAccessorImpl(jndiEnvironment);
      case REST:
        return new RESTAPIAccessorImpl();
      default:
        return new StandardAPIAccessorImpl();
    }
  }

  /**
   * To get the APIAccessor interface.
   * 
   * @return the interface APIAccessor.
   */
  public static APIAccessor getAPIAccessor() {
    return getAPIAccessor(null);
  }

  /**
   * To get the QueryAPIAccessor interface.
   * 
   * @return the interface QueryAPIAccessor.
   */
  public static QueryAPIAccessor getQueryAPIAccessor() {
    return getQueryAPIAccessor(null);
  }

  /**
   * To get the RuntimeAPI interface.
   * 
   * @return the interface RuntimeAPI.
   */
  public static RuntimeAPI getRuntimeAPI() {
    return getAPIAccessor().getRuntimeAPI();
  }

  /**
   * To get the ManagementAPI interface.
   * 
   * @return the interface ManagementAPI.
   */
  public static ManagementAPI getManagementAPI() {
    return getAPIAccessor().getManagementAPI();
  }

  /**
   * To get the CommandAPI interface.
   * 
   * @return the interface CommandAPI.
   */
  public static CommandAPI getCommandAPI() {
    return getAPIAccessor().getCommandAPI();
  }

  /**
   * To get the QueryRuntimeAPI interface.
   * 
   * @return the interface QueryRuntimeAPI.
   */
  public static QueryRuntimeAPI getQueryRuntimeAPI() {
    return getAPIAccessor().getQueryRuntimeAPI();
  }

  /**
   * To get the QueryRuntimeAPI interface.
   * 
   * @param the
   *          name of the list of queriers to use (this name should be defined
   *          in the environment).
   * @return the interface QueryRuntimeAPI.
   */
  public static QueryRuntimeAPI getQueryRuntimeAPI(final String queryList) {
    return getAPIAccessor().getQueryRuntimeAPI(queryList);
  }

  /**
   * To get the QueryDefinitionAPI interface.
   * 
   * @return the interface QueryDefinitionAPI.
   */
  public static QueryDefinitionAPI getQueryDefinitionAPI() {
    return getAPIAccessor().getQueryDefinitionAPI();
  }

  public static WebAPI getWebAPI() {
    return getAPIAccessor().getWebAPI();
  }

  public static BAMAPI getBAMAPI() {
    return getAPIAccessor().getBAMAPI();
  }

  public static BAMAPI getBAMAPI(final String queryList) {
    return getAPIAccessor().getBAMAPI(queryList);
  }

  /**
   * To get the QueryDefinitionAPI interface.
   * 
   * @param the
   *          name of the list of queriers to use (this name should be defined
   *          in the environment).
   * @return the interface QueryDefinitionAPI.
   */
  public static QueryDefinitionAPI getQueryDefinitionAPI(final String queryList) {
    return getAPIAccessor().getQueryDefinitionAPI(queryList);
  }

  /**
   * To get the IdentityAPI interface.
   * 
   * @return the interface IdentityAPI.
   */
  public static IdentityAPI getIdentityAPI() {
    return getAPIAccessor().getIdentityAPI();
  }

  /**
   * To get the RepairAPI interface.
   * 
   * @return the interface RepairAPI.
   */
  public static RepairAPI getRepairAPI() {
    return getAPIAccessor().getRepairAPI();
  }

}
