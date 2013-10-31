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
package org.ow2.bonita.util;

import java.io.File;

public final class BonitaConstants {

  private BonitaConstants() {
  }

  public static final String LOGGING_PROPERTY = "java.util.logging.config.file";
  public static final String JAAS_PROPERTY = "java.security.auth.login.config";
  public static final String ENVIRONMENT_PROPERTY = "org.ow2.bonita.environment";
  public static final String API_TYPE_PROPERTY = "org.ow2.bonita.api-type";
  public static final String INITIAL_CONTEXT_FACTORY_PROPERTY = "java.naming.factory.initial";
  public static final String PROVIDER_URL_PROPERTY = "java.naming.provider.url";
  public static final String SYSTEM_USER = "SYSTEM";
  public static final String JEE_SERVER_PROPERTY = "org.ow2.bonita.test.jee.server";

  public static final String DEFAULT_DOMAIN = "default";
  public static final String HOME = "BONITA_HOME";

  // used in examples/tests
  public static final String LOGIN_MODE_PROPERTY = "login.mode";
  public static final String LOGIN_MODE_TEST = "test";
  public static final String LOGIN_MODE_EXAMPLE = "example";

  public static final int MAX_QUERY_SIZE = 500;
  public static final int MAX_LIST_SIZE = 2500;
  public static final String TIMER_EVENT_PREFIX = "**bonita_timer**-";
  public static final String DEADLINE_EVENT_PREFIX = "**bonita_deadline**-";
  public static final String ASYNC_EVENT_PREFIX = "**bonita_async**-";
  public static final String CONNECTOR_AUTOMATIC_ON_ENTER_PREFIX = "**bonita_connector_automatic_on_enter**-";
  public static final String CONNECTOR_AUTOMATIC_ON_ENTER_EXECUTED_PREFIX = "**bonita_connector_automatic_on_enter_executed**-";

  // Encoding
  public static final String FILE_ENCONDING = "UTF-8";
  // XPath
  public static final String XPATH_VAR_SEPARATOR = "$";
  public static final Object XPATH_APPEND_FLAG = "APPEND";
  // Java
  public static final String JAVA_VAR_SEPARATOR = "#";

  // Context
  public static final String CONTEXT_PREFIX = "#context[";
  public static final String CONTEXT_SUFFIX = "]";
  public static final String CONTEXTS_FOLDER_IN_BAR = "contexts/";

  // Groovy
  public static final String API_ACCESSOR = "apiAccessor";
  public static final String PROCESS_DEFINITION = "processDefinition";
  public static final String LOGGED_USER = "loggedUser";
  public static final String ACTIVITY_INSTANCE = "activityInstance";
  public static final String PROCESS_INSTANCE = "processInstance";
  public static final String TIMER_LAST_EXECUTION = "timerLastExecution";
  public static final String USER_LOCALE = "webUserLocale";
  public static final String PROCESS_INSTANCE_INITIATOR = "processInstanceInitiator";

  // REST
  public static final String REST_SERVER_ADDRESS_PROPERTY = "org.ow2.bonita.rest-server-address";
  public static final String REST_SERVER_EXCEPTION = "rest.server.exception";

  public static final String getBonitaHomeFolder() {
    final String home = System.getProperty(HOME);
    if (home == null) {
      throw new BonitaRuntimeException("The system property '" + HOME + "' is not set");
    }
    return home;
  }

  private static final String getClientFolder() {
    final StringBuilder clientPath = new StringBuilder(getBonitaHomeFolder());
    clientPath.append(File.separator).append("client");
    return clientPath.toString();
  }

  public static final String getTemporaryFolder() {
    final StringBuilder tempPath = new StringBuilder(getClientFolder());
    tempPath.append(File.separator).append("tmp").append(File.separator).append("engine");
    return tempPath.toString();
  }

}
