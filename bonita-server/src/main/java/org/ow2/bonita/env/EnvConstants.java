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
 **/
package org.ow2.bonita.env;

public final class EnvConstants {

  private EnvConstants() {}

  public static final String HB_CONFIG_CORE = "hibernate-configuration:core";
  public static final String HB_SESSION_FACTORY_CORE = "hibernate-session-factory:core";
  public static final String HB_SESSION_CORE = "hibernate-session:core";
  public static final String BONITA_SESSION_CORE = "bonita-session:core";

  public static final String HB_CONFIG_HISTORY = "hibernate-configuration:history";
  public static final String HB_SESSION_FACTORY_HISTORY = "hibernate-session-factory:history";
  public static final String HB_SESSION_HISTORY = "hibernate-session:history";
  public static final String BONITA_SESSION_HISTORY = "bonita-session:history";

  public static final String DB_SESSION_TAG = "db-session";
  public static final String DOMAIN_TAG = "domain";
  public static final String VARIABLES_TAG = "variables";

  //SERVICES
  public static final String HISTORY_DEFAULT_KEY = "history";
  public static final String JOURNAL_DEFAULT_KEY = "journal";
  public static final String COMMAND_SERVICE_DEFAULT_KEY = "command-service";
  public static final String WEB_SERVICE_DEFAULT_KEY = "web-service";
  public static final String EVENT_SERVICE_DEFAULT_KEY = "event-service";
  public static final String UUID_SERVICE_DEFAULT_KEY = "uuid-service";
  public static final String CLASSDATA_LOADER_SERVICE_DEFAULT_KEY = "classdata-loader";
  public static final String LDR_SERVICE_DEFAULT_KEY = "large-data-repository";
  public static final String IDENTITY_SERVICE_DEFAULT_KEY = "identity-service";
  public static final String AUTHENTICATION_SERVICE_DEFAULT_KEY = "authentication-service";
  public static final String PRIVILEGE_SERVICE_DEFAULT_KEY = "privilege-service";
  public static final String WEB_TOKEN_MANAGEMENT_SERVICE_DEFAULT_KEY = "web-token-service";
  public static final String DOCUMENTATION_MANAGER_DEFAULT_KEY = "documentation-manager";
  public static final String DOCUMENT_STORAGE_SERVICE_KEY = "document-storage-service";
  public static final String ITERATION_DETECTION_TAG = "iteration-detection-policy";
  public static final String EXTENSION_POINTS_TAG = "extension-points";
  public static final String THROW_EXCEPTION_ON_FAILURE_ATTRIBUTE = "throw-exception-on-failure";

}
