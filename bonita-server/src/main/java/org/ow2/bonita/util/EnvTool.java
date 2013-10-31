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
 * Modified by Matthieu Chaffotte - BonitaSoft S.A.
 **/
package org.ow2.bonita.util;

import org.ow2.bonita.env.EnvConstants;
import org.ow2.bonita.env.Environment;
import org.ow2.bonita.env.EnvironmentFactory;
import org.ow2.bonita.facade.BonitaApplicationAccessContext;
import org.ow2.bonita.facade.BonitaSecurityContext;
import org.ow2.bonita.persistence.ApplicationAccessDbSession;
import org.ow2.bonita.persistence.DocumentDbSession;
import org.ow2.bonita.persistence.EventDbSession;
import org.ow2.bonita.persistence.IdentityDbSession;
import org.ow2.bonita.persistence.JournalDbSession;
import org.ow2.bonita.persistence.PrivilegeDbSession;
import org.ow2.bonita.persistence.QuerierDbSession;
import org.ow2.bonita.persistence.WebDbSession;
import org.ow2.bonita.persistence.WebTokenManagementDbSession;
import org.ow2.bonita.runtime.ClassDataLoader;
import org.ow2.bonita.runtime.ExtensionPointsPolicy;
import org.ow2.bonita.runtime.IterationDetectionPolicy;
import org.ow2.bonita.runtime.VariablesOptions;
import org.ow2.bonita.runtime.event.EventExecutor;
import org.ow2.bonita.runtime.tx.StandardTransaction;
import org.ow2.bonita.services.Archiver;
import org.ow2.bonita.services.AuthenticationService;
import org.ow2.bonita.services.CommandService;
import org.ow2.bonita.services.DocumentStorageService;
import org.ow2.bonita.services.DocumentationManager;
import org.ow2.bonita.services.EventService;
import org.ow2.bonita.services.IdentityService;
import org.ow2.bonita.services.Journal;
import org.ow2.bonita.services.LargeDataRepository;
import org.ow2.bonita.services.LobCreator;
import org.ow2.bonita.services.PrivilegeService;
import org.ow2.bonita.services.Querier;
import org.ow2.bonita.services.Recorder;
import org.ow2.bonita.services.UUIDService;
import org.ow2.bonita.services.WebService;
import org.ow2.bonita.services.WebTokenManagementService;
import org.ow2.bonita.services.handlers.FinishedInstanceHandler;
import org.ow2.bonita.services.impl.QuerierListAccessor;
import org.ow2.bonita.type.VariableTypeResolver;
import org.ow2.bonita.type.lob.BlobStrategy;
import org.ow2.bonita.type.lob.ClobStrategy;

/**
 * This class holds the {@link EnvironmentFactory} singleton.
 * 
 * The {@link EnvironmentFactory} returned by {@link #getEnvironmentFactory()}
 * comes from the parsing of the resource defined by the property
 * {@link #ENVIRONMENT_PROPERTY}. If this property has not been set, the
 * {@link #DEFAULT_ENVIRONMENT} resource is used and parsed.
 * 
 * @author Marc Blachon, Guillaume Porcher, Charles Souillard, Miguel Valdes,
 *         Pierre Vigneras
 */
public final class EnvTool {

  private EnvTool() {}

  public static ClassDataLoader getClassDataLoader() {
    return getEnvClass(ClassDataLoader.class);
  }

  public static String getDomain() {
    return getEnvObject(String.class, EnvConstants.DOMAIN_TAG);
  }

  public static StandardTransaction getTransaction() {
    return getEnvClass(StandardTransaction.class);
  }

  public static JournalDbSession getJournalDbSession(final String id) {
    return getEnvObject(JournalDbSession.class, id);
  }

  public static QuerierDbSession getQuerierDbSession(final String id) {
    return getEnvObject(QuerierDbSession.class, id);
  }

  public static WebDbSession getWebServiceDbSession(final String id) {
    return getEnvObject(WebDbSession.class, id);
  }

  public static IdentityDbSession getIdentityDbSession(final String id) {
    return getEnvObject(IdentityDbSession.class, id);
  }

  public static DocumentDbSession getDocumentDbSession(final String id) {
    return getEnvObject(DocumentDbSession.class, id);
  }

  public static ApplicationAccessDbSession getApplicationAccessDbSession(final String id) {
    return getEnvObject(ApplicationAccessDbSession.class, id);
  }

  public static EventDbSession getEventServiceDbSession(final String id) {
    return getEnvObject(EventDbSession.class, id);
  }

  public static PrivilegeDbSession getPrivilegeDbSession(final String id) {
    return getEnvObject(PrivilegeDbSession.class, id);
  }

  public static WebTokenManagementDbSession getWebTokenManagementDbSession(final String id) {
    return getEnvObject(WebTokenManagementDbSession.class, id);
  }

  public static String getApplicationAccessName() {
    try {
      return getEnvClass(BonitaApplicationAccessContext.class).getApplicationName();
    } catch (final BonitaRuntimeException e) {
      return null;
    }
  }

  public static boolean isRestrictedApplicationAcces() {
    try {
      getEnvClass(BonitaApplicationAccessContext.class).getApplicationName();
      return true;
    } catch (final BonitaRuntimeException e) {
      return false;
    }
  }

  @SuppressWarnings("unchecked")
  public static <T> T get(final Class<T> class1, final String key) {
    Misc.checkArgsNotNull(class1, key);
    final Object o = getEnv().get(key);
    String message = ExceptionManager.getInstance().getFullMessage("bis_ET_1", key);
    Misc.badStateIfNull(o, message);
    message = ExceptionManager.getInstance().getFullMessage("bis_ET_2", class1, o.getClass());
    Misc.badStateIfFalse(class1.isAssignableFrom(o.getClass()), message);
    return (T) o;
  }

  protected static Environment getEnv() {
    final Environment environment = Environment.getCurrent();
    Misc.badStateIfNull(environment, "Environment is null!");
    return environment;
  }

  @SuppressWarnings("unchecked")
  protected static <T> T getEnvObject(final Class<T> clazz, final String name) {
    final Environment environment = getEnv();

    Misc.badStateIfNull(environment, "Environment is null!");
    final Object object = environment.get(name);
    if (object == null) {
      final String message = ExceptionManager.getInstance().getMessage("bs_SET_1", name);
      throw new BonitaRuntimeException(message);
    } else if (!clazz.isInstance(object)) {
      final String message = ExceptionManager.getInstance().getMessage("bs_SET_2", object, name, clazz.getName());
      throw new BonitaRuntimeException(message);
    }
    return (T) object;
  }

  protected static <T> T getEnvClass(final Class<T> clazz) {
    final Environment environment = getEnv();
    final T object = environment.get(clazz);
    if (object == null) {
      final String message = ExceptionManager.getInstance().getFullMessage("be_EET_1", clazz.getName());
      throw new BonitaRuntimeException(message);
    }
    return object;
  }

  public static BonitaSecurityContext getBonitaSecurityContext() {
    return getEnvClass(BonitaSecurityContext.class);
  }

  public static Journal getJournal() {
    return getEnvClass(Journal.class);
  }

  public static LargeDataRepository getLargeDataRepository() {
    return getEnvObject(LargeDataRepository.class, EnvConstants.LDR_SERVICE_DEFAULT_KEY);
  }

  public static IdentityService getIdentityService() {
    return getEnvObject(IdentityService.class, EnvConstants.IDENTITY_SERVICE_DEFAULT_KEY);
  }

  public static CommandService getCommandService() {
    return getEnvObject(CommandService.class, EnvConstants.COMMAND_SERVICE_DEFAULT_KEY);
  }

  public static AuthenticationService getAuthenticationService() {
    return getEnvObject(AuthenticationService.class, EnvConstants.AUTHENTICATION_SERVICE_DEFAULT_KEY);
  }

  public static WebService getWebService() {
    return getEnvObject(WebService.class, EnvConstants.WEB_SERVICE_DEFAULT_KEY);
  }

  public static EventService getEventService() {
    return getEnvObject(EventService.class, EnvConstants.EVENT_SERVICE_DEFAULT_KEY);
  }

  public static UUIDService getUUIDService() {
    return getEnvObject(UUIDService.class, EnvConstants.UUID_SERVICE_DEFAULT_KEY);
  }

  public static PrivilegeService getPrivilegeService() {
    return getEnvObject(PrivilegeService.class, EnvConstants.PRIVILEGE_SERVICE_DEFAULT_KEY);
  }

  public static WebTokenManagementService getWebTokenManagementService() {
    return getEnvObject(WebTokenManagementService.class, EnvConstants.WEB_TOKEN_MANAGEMENT_SERVICE_DEFAULT_KEY);
  }

  public static LobCreator getLobCreator() {
    return getEnvClass(LobCreator.class);
  }

  public static BlobStrategy getBlobStrategy() {
    return getEnvClass(BlobStrategy.class);
  }

  public static ClobStrategy getClobStrategy() {
    return getEnvClass(ClobStrategy.class);
  }

  public static EventExecutor getEventExecutor() {
    return getEnvClass(EventExecutor.class);
  }

  public static Recorder getRecorder() {
    return getEnvObject(Recorder.class, Recorder.DEFAULT_KEY);
  }

  public static Archiver getArchiver() {
    return getEnvObject(Archiver.class, Archiver.DEFAULT_KEY);
  }

  // // TODO: remove the 3 methods
  public static Querier getAllQueriers() {
    return getQuerierListAccessor(AccessorUtil.QUERYLIST_DEFAULT_KEY).getAllQueriers();
  }

  public static Querier getJournalQueriers() {
    return getQuerierListAccessor(AccessorUtil.QUERYLIST_DEFAULT_KEY).getJournals();
  }

  public static Querier getHistoryQueriers() {
    return getQuerierListAccessor(AccessorUtil.QUERYLIST_DEFAULT_KEY).getHistories();
  }

  // // End of remove

  private static QuerierListAccessor getQuerierListAccessor(final String queryList) {
    return getEnvObject(QuerierListAccessor.class, queryList);
  }

  public static Querier getAllQueriers(final String queryList) {
    return getQuerierListAccessor(queryList).getAllQueriers();
  }

  public static Querier getJournalQueriers(final String queryList) {
    return getQuerierListAccessor(queryList).getJournals();
  }

  public static Querier getHistoryQueriers(final String queryList) {
    return getQuerierListAccessor(queryList).getHistories();
  }

  public static FinishedInstanceHandler getFinishedInstanceHandler() {
    return getEnvObject(FinishedInstanceHandler.class, FinishedInstanceHandler.DEFAULT_KEY);
  }

  public static String getUserId() {
    final String userId = getEnv().getUserId();
    if (userId == null) {
      final String message = ExceptionManager.getInstance().getMessage("bs_SET_3");
      throw new BonitaRuntimeException(message);
    }
    return userId;
  }

  public static VariableTypeResolver getVariableTypeResolver() {
    return getEnvClass(VariableTypeResolver.class);
  }

  public static VariablesOptions getVariablesOptions() {
    return getEnvClass(VariablesOptions.class);
  }

  public static DocumentationManager getDocumentationManager() {
    return getEnvObject(DocumentationManager.class, EnvConstants.DOCUMENTATION_MANAGER_DEFAULT_KEY);
  }

  public static DocumentStorageService getDocumentStorageService() {
    return getEnvObject(DocumentStorageService.class, EnvConstants.DOCUMENT_STORAGE_SERVICE_KEY);
  }

  public static IterationDetectionPolicy getIterationDetectionPolicy() {
    return getEnvClass(IterationDetectionPolicy.class);
  }

  public static ExtensionPointsPolicy getExtensionPointsPolicy() {
    return getEnvClass(ExtensionPointsPolicy.class);
  }

}
