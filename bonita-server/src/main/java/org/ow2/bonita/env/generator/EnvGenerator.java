/**
 * Copyright (C) 2007  Bull S. A. S.
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
package org.ow2.bonita.env.generator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ow2.bonita.env.BonitaEnvironmentParser;
import org.ow2.bonita.env.EnvConstants;
import org.ow2.bonita.env.EnvironmentFactory;
import org.ow2.bonita.runtime.ClassDataLoader;
import org.ow2.bonita.runtime.event.EventMatcher;
import org.ow2.bonita.runtime.event.MasterChecker;
import org.ow2.bonita.runtime.event.MemoryLockJobExecutor;
import org.ow2.bonita.runtime.tx.StandardTransaction;
import org.ow2.bonita.services.Archiver;
import org.ow2.bonita.services.Recorder;
import org.ow2.bonita.services.handlers.FinishedInstanceHandler;
import org.ow2.bonita.services.handlers.impl.ArchiveFinishedInstanceHandler;
import org.ow2.bonita.services.impl.DbAuthentication;
import org.ow2.bonita.services.impl.DbHistory;
import org.ow2.bonita.services.impl.DbIdentity;
import org.ow2.bonita.services.impl.DbJournal;
import org.ow2.bonita.services.impl.DbPrivilegeService;
import org.ow2.bonita.services.impl.DbThreadEventService;
import org.ow2.bonita.services.impl.DbUUIDService;
import org.ow2.bonita.services.impl.DbWebService;
import org.ow2.bonita.services.impl.DbWebTokenManagementService;
import org.ow2.bonita.services.impl.DocumentManagerImpl;
import org.ow2.bonita.services.impl.DocumentStorageServiceImpl;
import org.ow2.bonita.services.impl.FileLargeDataRepository;
import org.ow2.bonita.services.impl.HibernateLobCreator;
import org.ow2.bonita.services.impl.LoggerArchiver;
import org.ow2.bonita.services.impl.LoggerRecorder;
import org.ow2.bonita.services.impl.OptimizedDbHistory;
import org.ow2.bonita.type.lob.BlobStrategyBlob;
import org.ow2.bonita.type.lob.ClobStrategyChopped;
import org.ow2.bonita.util.AccessorUtil;
import org.ow2.bonita.util.BonitaConstants;
import org.ow2.bonita.util.Misc;

/**
 * @author Marc Blachon, Guillaume Porcher, Charles Souillard, Miguel Valdes,
 *         Pierre Vigneras
 */
public class EnvGenerator {

  private static final Logger LOG = Logger.getLogger(EnvGenerator.class.getName());
  public static final String INDENT = "  ";

  private final Map<String, EnvEntry> envFactoryEntries = new TreeMap<String, EnvEntry>();
  private final Map<String, EnvEntry> envEntries = new TreeMap<String, EnvEntry>();

  public EnvGenerator(final boolean defaultConfiguration) {
    // APPLICATION ENTRIES
    addApplicationEntry(getDomain());

    addApplicationEntry(getCoreHibernateConfiguration());
    addApplicationEntry(getCoreHibernateSessionFactory());

    addApplicationEntry(getHistoryHibernateConfiguration());
    addApplicationEntry(getHistoryHibernateSessionFactory());

    addApplicationEntry(getCommandService());
    addApplicationEntry(getVariables());
    addApplicationEntry(getEventExecutor());
    addApplicationEntry(getTypeResolver());
    addApplicationEntry(getLobCreator());
    addApplicationEntry(getBlobStrategy());
    addApplicationEntry(getClobStrategy());
    addApplicationEntry(getLargeDataRepository());
    addApplicationEntry(getDocumentationManager());
    addApplicationEntry(getDocumentStorageServiceManager());
    addApplicationEntry(getFinishedInstanceHandler());
    addApplicationEntry(getDbUUIDService());
    addApplicationEntry(getClassdataLoader());
    addApplicationEntry(getIterationDetectionPolicy());
    addApplicationEntry(getExtenstionPointsPolicy());

    // BLOCK ENTRIES
    addBlockEntry(getCoreHibernateSession());
    addBlockEntry(getHistoryDbSession());
    addBlockEntry(getHistoryHibernateSession());
    addBlockEntry(getCoreDbSession());
    addBlockEntry(getEventService());
    addBlockEntry(getDefaultQueryList());
    addBlockEntry(getJournalQueryList());
    addBlockEntry(getHistoryQueryList());
    addBlockEntry(getDbHistory());
    addBlockEntry(getOptimizeDbHistory());
    addBlockEntry(getRecorder());
    addBlockEntry(getArchiver());
    addBlockEntry(getTransaction());
    addBlockEntry(getJournal());
    addBlockEntry(getWebService());

    addBlockEntry(getIdentityService());
    addBlockEntry(getAuthenticationService());
    addBlockEntry(getPrivilegeService());
    addBlockEntry(getWebTokenManagementService());
  }

  private EnvEntry getOptimizeDbHistory() {
    final String key = EnvConstants.HISTORY_DEFAULT_KEY;
    final EnvEntry optimizeDbHistoryEntry = new EnvEntry("Optimized DB history",
        "Optimized DB Implementation of the history. " + "This implementation contains optimized methods. "
            + "For example, a call to getUserTasks(final String userId, final ActivityState taskState) "
            + " looks in DB only if the state is an expected one in History (FINISHED is one, READY is not...)", "<"
            + key + " name='" + key + "' class='" + OptimizedDbHistory.class.getName() + "'>" + Misc.LINE_SEPARATOR
            + EnvGenerator.INDENT + "<arg><string value='" + EnvConstants.BONITA_SESSION_HISTORY + "' /></arg>"
            + Misc.LINE_SEPARATOR + "</" + key + ">", true);
    return optimizeDbHistoryEntry;
  }

  private EnvEntry getHistoryDbSession() {
    return getDbSession(EnvConstants.BONITA_SESSION_CORE, EnvConstants.HB_SESSION_CORE,
        "hibernate session name for Bonita core.");
  }

  private EnvEntry getCoreDbSession() {
    return getDbSession(EnvConstants.BONITA_SESSION_HISTORY, EnvConstants.HB_SESSION_HISTORY,
        "hibernate session name for Bonita history.");
  }

  private EnvEntry getHistoryHibernateSession() {
    return getHibernateSession(EnvConstants.HB_SESSION_HISTORY, EnvConstants.HB_SESSION_FACTORY_HISTORY,
        "hibernate session for Bonita history.");
  }

  private EnvEntry getCoreHibernateSession() {
    return getHibernateSession(EnvConstants.HB_SESSION_CORE, EnvConstants.HB_SESSION_FACTORY_CORE,
        "hibernate session for Bonita core.");
  }

  private EnvEntry getHistoryHibernateSessionFactory() {
    return getHibernateSessionFactory(EnvConstants.HB_SESSION_FACTORY_HISTORY, EnvConstants.HB_CONFIG_HISTORY,
        "hibernate session factory for Bonita history.");
  }

  private EnvEntry getCoreHibernateSessionFactory() {
    return getHibernateSessionFactory(EnvConstants.HB_SESSION_FACTORY_CORE, EnvConstants.HB_CONFIG_CORE,
        "hibernate session factory for Bonita core.");
  }

  private EnvEntry getHistoryHibernateConfiguration() {
    // Hibernate configuration for history
    final StringBuilder historyPathBuilder = new StringBuilder(getDefaultServerFolder());
    historyPathBuilder.append(File.separator).append("conf").append(File.separator).append("bonita-history.properties");
    return getHibernateConfig(historyPathBuilder.toString(), EnvConstants.HB_CONFIG_HISTORY,
        "bonita.history.cache.xml", "nonstrict-read-write",
        "Configuration of the Hibernate session factory used by Bonita history.");
  }

  private EnvEntry getCoreHibernateConfiguration() {
    final StringBuilder corePathBuilder = new StringBuilder(getDefaultServerFolder());
    corePathBuilder.append(File.separator).append("conf").append(File.separator).append("bonita-journal.properties");
    return getHibernateConfig(corePathBuilder.toString(), EnvConstants.HB_CONFIG_CORE, "bonita.core.cache.xml",
        "read-write", "Configuration of the Hibernate session factory used by Bonita core.");
  }

  private EnvEntry getDbHistory() {
    return new EnvEntry("Db history",
        "DB Implementation of the history. This implementation is full conformant with the spec (but not optimized).",
        "<history name='" + EnvConstants.HISTORY_DEFAULT_KEY + "' class='" + DbHistory.class.getName() + "'/>", false);
  }

  private EnvEntry getDefaultQueryList() {
    final List<EnvEntry> queryApiEntries = new ArrayList<EnvEntry>();
    queryApiEntries.add(new EnvEntry(EnvConstants.JOURNAL_DEFAULT_KEY, "Reference to the journal", "<ref object='"
        + EnvConstants.JOURNAL_DEFAULT_KEY + "' />", true));
    queryApiEntries.add(new EnvEntry(EnvConstants.HISTORY_DEFAULT_KEY, "Reference to the history", "<ref object='"
        + EnvConstants.HISTORY_DEFAULT_KEY + "' />", true));
    return new QueryApiEnvEntry(AccessorUtil.QUERYLIST_DEFAULT_KEY, "List of Queriers to use.", queryApiEntries, true);
  }

  private EnvEntry getJournalQueryList() {
    final List<EnvEntry> queryApiEntries = new ArrayList<EnvEntry>();
    queryApiEntries.add(new EnvEntry(EnvConstants.JOURNAL_DEFAULT_KEY, "Reference to the journal", "<ref object='"
        + EnvConstants.JOURNAL_DEFAULT_KEY + "' />", true));
    return new QueryApiEnvEntry(AccessorUtil.QUERYLIST_JOURNAL_KEY, "List of Queriers to use.", queryApiEntries, true);
  }

  private EnvEntry getHistoryQueryList() {
    final List<EnvEntry> queryApiEntries = new ArrayList<EnvEntry>();
    queryApiEntries.add(new EnvEntry(EnvConstants.HISTORY_DEFAULT_KEY, "Reference to the history", "<ref object='"
        + EnvConstants.HISTORY_DEFAULT_KEY + "' />", true));
    return new QueryApiEnvEntry(AccessorUtil.QUERYLIST_HISTORY_KEY, "List of Queriers to use.", queryApiEntries, true);
  }

  private EnvEntry getRecorder() {
    final List<EnvEntry> recorders = new ArrayList<EnvEntry>();
    recorders.add(new EnvEntry("Log recorder", "Recorder which writes recorded data to logs.", "<recorder class='"
        + LoggerRecorder.class.getName() + "' />", false));
    recorders.add(new EnvEntry(EnvConstants.JOURNAL_DEFAULT_KEY, "Reference to the journal", "<ref object='"
        + EnvConstants.JOURNAL_DEFAULT_KEY + "' />", true));
    return new ChainerEnvEntry(Recorder.DEFAULT_KEY, "List of recorders.", recorders, true);
  }

  private EnvEntry getArchiver() {
    final List<EnvEntry> archivers = new ArrayList<EnvEntry>();
    archivers.add(new EnvEntry("Log archiver", "Archiver which writes archived data to logs.", "<archiver class='"
        + LoggerArchiver.class.getName() + "' />", false));
    archivers.add(new EnvEntry(EnvConstants.HISTORY_DEFAULT_KEY, "Reference to the history", "<ref object='"
        + EnvConstants.HISTORY_DEFAULT_KEY + "' />", true));
    return new ChainerEnvEntry(Archiver.DEFAULT_KEY, "List of archivers.", archivers, true);
  }

  private EnvEntry getDomain() {
    return getEnvEntry(EnvConstants.DOMAIN_TAG, "Specify the domain of this environment", "<" + EnvConstants.DOMAIN_TAG
        + " id='" + BonitaConstants.DEFAULT_DOMAIN + "' />", true);
  }

  private EnvEntry getTransaction() {
    return getEnvEntry("transaction", "Db Transaction.", StandardTransaction.class, true);
  }

  private EnvEntry getTypeResolver() {
    return getEnvEntry("type-resolver", "Mapping from java variable to database type",
        "<variable-types resource='bonita.type.resolver.xml' />", true);
  }

  private EnvEntry getLobCreator() {
    return getEnvEntry("lob-creator",
        "Implementation of the LobCreator interface which will be used to create SQL Blobs, Clobs.",
        HibernateLobCreator.class, true);
  }

  private EnvEntry getBlobStrategy() {
    return getEnvEntry("blob-strategy",
        "Implementation of the BlobStrategy interface which will be used to store Blobs.", BlobStrategyBlob.class, true);
  }

  private EnvEntry getClobStrategy() {
    return getEnvEntry("clob-strategy",
        "Implementation of the LobCreator interface which will be used to store Clobs.", ClobStrategyChopped.class,
        true);
  }

  private EnvEntry getEventExecutor() {
    String eventExecutorXml = "<event-executor idle-min='50' idle='5000' threads='3' lock='120000' retries='5' auto-start='true' command-service='"
        + EnvConstants.COMMAND_SERVICE_DEFAULT_KEY + "'>" + Misc.LINE_SEPARATOR;
    eventExecutorXml += EnvGenerator.INDENT + "<job-executor class='" + MemoryLockJobExecutor.class.getName() + "' locks-to-query='200' lock-idle-time='5000' />" + Misc.LINE_SEPARATOR;
    eventExecutorXml += EnvGenerator.INDENT + "<matcher max-couples='500' class='" + EventMatcher.class.getName() + "'>" + Misc.LINE_SEPARATOR;
    eventExecutorXml += EnvGenerator.INDENT + EnvGenerator.INDENT + "<condition-matching enable='true' />"
        + Misc.LINE_SEPARATOR;
    eventExecutorXml += EnvGenerator.INDENT + "</matcher>" + Misc.LINE_SEPARATOR;
    
    eventExecutorXml += EnvGenerator.INDENT + "<master-checker class='" + MasterChecker.class.getName() + "' enable='true' />" + Misc.LINE_SEPARATOR;
    
    eventExecutorXml += "</event-executor>" + Misc.LINE_SEPARATOR;
    return getEnvEntry("event-executor", "Service which executes events", eventExecutorXml, true);
  }

  private EnvEntry getIterationDetectionPolicy() {
    return getEnvEntry(EnvConstants.ITERATION_DETECTION_TAG, "Enable/disable iteration detection", "<"
        + EnvConstants.ITERATION_DETECTION_TAG + " disable='false' />", true);
  }

  private EnvEntry getExtenstionPointsPolicy() {
    return getEnvEntry(EnvConstants.EXTENSION_POINTS_TAG,
        "Identify if extension points (connectors and Groovy scritps) throw the exception on failure", "<"
            + EnvConstants.EXTENSION_POINTS_TAG + " " + EnvConstants.THROW_EXCEPTION_ON_FAILURE_ATTRIBUTE
            + "='false' />", true);
  }

  private EnvEntry getVariables() {
    return getEnvEntry(EnvConstants.VARIABLES_TAG, "Properties of variables management.", "<"
        + EnvConstants.VARIABLES_TAG + " store-history='true'/>", true);
  }

  private EnvEntry getCommandService() {
    String commandServiceConfigXml = "<command-service name='" + EnvConstants.COMMAND_SERVICE_DEFAULT_KEY + "'>\n";
    commandServiceConfigXml += EnvGenerator.INDENT + "<retry-interceptor retries='5' delay='50' delay-factor='10'/>\n";
    commandServiceConfigXml += EnvGenerator.INDENT + "<environment-interceptor />\n";
    commandServiceConfigXml += EnvGenerator.INDENT + "<standard-transaction-interceptor />\n";
    commandServiceConfigXml += "</command-service>\n";
    return getEnvEntry("command-service", "Service which executes commands", commandServiceConfigXml, true);
  }

  private EnvEntry getHibernateConfig(final String propertiesName, final String hibernateConfigurationName,
      final String cacheFile, final String cacheUsage, final String usage) {
    final StringBuffer hibernateConfigXml = new StringBuffer();
    hibernateConfigXml.append("<hibernate-configuration name='" + hibernateConfigurationName + "' >")
        .append(Misc.LINE_SEPARATOR).append(EnvGenerator.INDENT).append("<properties file='" + propertiesName + "' />")
        .append(Misc.LINE_SEPARATOR).append(EnvGenerator.INDENT)
        .append("<mappings resource='bonita.mappings.hbm.xml' />").append(Misc.LINE_SEPARATOR)
        .append(EnvGenerator.INDENT)
        .append("<cache-configuration resource='" + cacheFile + "' usage='" + cacheUsage + "' />")
        .append(Misc.LINE_SEPARATOR).append("</hibernate-configuration>").append(Misc.LINE_SEPARATOR);
    return getEnvEntry(hibernateConfigurationName, usage, hibernateConfigXml.toString(), true);
  }

  private EnvEntry getHibernateSessionFactory(final String hibernateSessionFactoryName,
      final String hibernateConfigurationName, final String usage) {
    return getEnvEntry(hibernateSessionFactoryName, "Hibernate session factory used by " + usage + ".",
        "<hibernate-session-factory name='" + hibernateSessionFactoryName + "' configuration='"
            + hibernateConfigurationName + "' init='eager'/>", true);
  }

  private EnvEntry getHibernateSession(final String hibernateSessionName, final String hibernateSessionFactoryName,
      final String usage) {
    return getEnvEntry(hibernateSessionName, "Hibernate session used by " + usage + ".", "<hibernate-session name='"
        + hibernateSessionName + "' factory='" + hibernateSessionFactoryName + "' />", true);
  }

  private EnvEntry getDbSession(final String dbSessionName, final String hibernateSessionName, final String usage) {
    return getEnvEntry(dbSessionName, "Querier used in " + usage + ".", "<" + EnvConstants.DB_SESSION_TAG + " name='"
        + dbSessionName + "' session='" + hibernateSessionName + "'/>", true);
  }

  private EnvEntry getFinishedInstanceHandler() {
    return getChainerEnvEntry(FinishedInstanceHandler.DEFAULT_KEY,
        "List of services called when an instance is finished.", true, ArchiveFinishedInstanceHandler.class);
  }

  private EnvEntry getJournal() {
    final String key = EnvConstants.JOURNAL_DEFAULT_KEY;
    String xml = "<" + key + " name='" + key + "' class='" + DbJournal.class.getName() + "'> " + Misc.LINE_SEPARATOR;
    xml += EnvGenerator.INDENT + "<arg><string value='" + EnvConstants.BONITA_SESSION_CORE + "' /></arg>"
        + Misc.LINE_SEPARATOR;
    xml += "</" + key + ">";
    return new EnvEntry(key, "Implementation of the journal.", xml, true);
  }

  private EnvEntry getWebService() {
    final String key = EnvConstants.WEB_SERVICE_DEFAULT_KEY;
    String xml = "<" + key + " name='" + key + "' class='" + DbWebService.class.getName() + "'> " + Misc.LINE_SEPARATOR;
    xml += EnvGenerator.INDENT + "<arg><string value='" + EnvConstants.BONITA_SESSION_CORE + "' /></arg>"
        + Misc.LINE_SEPARATOR;
    xml += "</" + key + ">";
    return new EnvEntry(key, "Implementation of the Web Service.", xml, true);
  }

  private EnvEntry getEventService() {
    final String key = EnvConstants.EVENT_SERVICE_DEFAULT_KEY;
    String xml = "<" + key + " name='" + key + "' class='" + DbThreadEventService.class.getName() + "'> "
        + Misc.LINE_SEPARATOR;
    xml += EnvGenerator.INDENT + "<arg><string value='" + EnvConstants.BONITA_SESSION_CORE + "' /></arg>"
        + Misc.LINE_SEPARATOR;
    xml += "</" + key + ">";
    return new EnvEntry(key, "Implementation of the Event Service.", xml, true);
  }

  private EnvEntry getDbUUIDService() {
    final String key = EnvConstants.UUID_SERVICE_DEFAULT_KEY;
    final String xml = "<" + key + " name='" + key + "' class='" + DbUUIDService.class.getName() + "'/>";
    return new EnvEntry(key, "Implementation of the UUID Service.", xml, true);
  }

  private EnvEntry getClassdataLoader() {
    final String key = EnvConstants.CLASSDATA_LOADER_SERVICE_DEFAULT_KEY;
    final String xml = "<" + key + " name='" + key + "' class='" + ClassDataLoader.class.getName() + "'/>";
    return new EnvEntry(key, "Implementation of the Classdata loader Service.", xml, true);
  }

  private EnvEntry getLargeDataRepository() {
    final StringBuilder fileRepositoryPath = new StringBuilder(getDefaultServerFolder());
    fileRepositoryPath.append(File.separator).append("work");
    final String key = EnvConstants.LDR_SERVICE_DEFAULT_KEY;
    String xml = "<" + key + " name='" + key + "' class='" + FileLargeDataRepository.class.getName() + "'>"
        + Misc.LINE_SEPARATOR;
    xml += EnvGenerator.INDENT + "<arg><string value='" + fileRepositoryPath.toString() + "' /></arg>"
        + Misc.LINE_SEPARATOR;
    xml += "</" + key + ">";
    return new EnvEntry(key, "Implementation of the large data repository.", xml, true);
  }

  private EnvEntry getDocumentationManager() {
    final String key = EnvConstants.DOCUMENTATION_MANAGER_DEFAULT_KEY;
    final String i = EnvGenerator.INDENT;

    final StringBuilder stb = new StringBuilder();
    stb.append("<");
    stb.append(key);
    stb.append(" name='");
    stb.append(key);
    stb.append("' class='");
    stb.append(DocumentManagerImpl.class.getName());
    stb.append("'>");
    stb.append(Misc.LINE_SEPARATOR);
    stb.append(i);
    stb.append("<arg><string value='");
    stb.append(EnvConstants.BONITA_SESSION_CORE);
    stb.append("' /></arg>");
    stb.append(Misc.LINE_SEPARATOR);
    stb.append("</");
    stb.append(key);
    stb.append(">");
    final String xml = stb.toString();
    return new EnvEntry(key, "Implementation of the documentation manager.", xml, true);
  }

  private EnvEntry getDocumentStorageServiceManager() {
    final String key = EnvConstants.DOCUMENT_STORAGE_SERVICE_KEY;
    final String i = EnvGenerator.INDENT;

    final StringBuilder stb = new StringBuilder();
    stb.append("<");
    stb.append(key);
    stb.append(" name='");
    stb.append(key);
    stb.append("' class='");
    stb.append(DocumentStorageServiceImpl.class.getName());
    stb.append("'>");
    stb.append(Misc.LINE_SEPARATOR);
    stb.append(i);
    stb.append("<arg><string value='");
    stb.append(EnvConstants.BONITA_SESSION_CORE);
    stb.append("' /></arg>");
    stb.append(Misc.LINE_SEPARATOR);
    stb.append("</");
    stb.append(key);
    stb.append(">");
    final String xml = stb.toString();
    return new EnvEntry(key, "Implementation of the document storage service.", xml, true);
  }

  private EnvEntry getIdentityService() {
    final String key = EnvConstants.IDENTITY_SERVICE_DEFAULT_KEY;
    String xml = "<" + key + " name='" + key + "' class='" + DbIdentity.class.getName() + "'>" + Misc.LINE_SEPARATOR;
    xml += EnvGenerator.INDENT + "<arg><string value='" + EnvConstants.BONITA_SESSION_CORE + "' /></arg>"
        + Misc.LINE_SEPARATOR;
    xml += "</" + key + ">";
    return new EnvEntry(key, "Implementation of the identity service.", xml, true);
  }

  private EnvEntry getAuthenticationService() {
    final String key = EnvConstants.AUTHENTICATION_SERVICE_DEFAULT_KEY;
    String xml = "<" + key + " name='" + key + "' class='" + DbAuthentication.class.getName() + "'>"
        + Misc.LINE_SEPARATOR;
    xml += EnvGenerator.INDENT + "<arg><string value='" + EnvConstants.BONITA_SESSION_CORE + "' /></arg>"
        + Misc.LINE_SEPARATOR;
    xml += "</" + key + ">";
    return new EnvEntry(key, "Implementation of the authentication service.", xml, true);
  }

  private EnvEntry getPrivilegeService() {
    final String key = EnvConstants.PRIVILEGE_SERVICE_DEFAULT_KEY;
    String xml = "<" + key + " name='" + key + "' class='" + DbPrivilegeService.class.getName() + "'>"
        + Misc.LINE_SEPARATOR;
    xml += EnvGenerator.INDENT + "<arg><string value='" + EnvConstants.BONITA_SESSION_CORE + "' /></arg>"
        + Misc.LINE_SEPARATOR;
    xml += "</" + key + ">";
    return new EnvEntry(key, "Implementation of the privilege service.", xml, true);
  }

  private EnvEntry getWebTokenManagementService() {
    final String key = EnvConstants.WEB_TOKEN_MANAGEMENT_SERVICE_DEFAULT_KEY;
    String xml = "<" + key + " name='" + key + "' class='" + DbWebTokenManagementService.class.getName() + "'>"
        + Misc.LINE_SEPARATOR;
    xml += EnvGenerator.INDENT + "<arg><string value='" + EnvConstants.BONITA_SESSION_CORE + "' /></arg>"
        + Misc.LINE_SEPARATOR;
    xml += "</" + key + ">";
    return new EnvEntry(key, "Implementation of the web token management service.", xml, true);
  }

  /**
   * Generates an XML file for the given environment configuration.
   */
  public String createEnvironmentXml() {
    int indentDepth = 0;

    // ENV XML GENERATION BEGINS HERE
    final StringBuilder result = new StringBuilder("<environment-definition>" + Misc.LINE_SEPARATOR
        + Misc.LINE_SEPARATOR);

    // DEFINITION OF THE ENVIRONMENT FACTORY
    indentDepth++;
    result.append(getIndent(indentDepth) + "<environment-factory>" + Misc.LINE_SEPARATOR);

    indentDepth++;
    for (final EnvEntry entry : envFactoryEntries.values()) {
      result.append(entry.getEnvXml(getIndent(indentDepth))).append(Misc.LINE_SEPARATOR);
    }
    indentDepth--;
    result.append(getIndent(indentDepth)).append("</environment-factory>" + Misc.LINE_SEPARATOR + Misc.LINE_SEPARATOR);
    // END OF ENVIRONMENT FACTORY
    // DEFINITION OF THE ENVIRONMENT

    result.append(getIndent(indentDepth)).append("<environment>" + Misc.LINE_SEPARATOR);
    indentDepth++;

    // other entries
    for (final EnvEntry entry : envEntries.values()) {
      result.append(entry.getEnvXml(getIndent(indentDepth))).append(Misc.LINE_SEPARATOR);
    }

    indentDepth--;
    result.append(getIndent(indentDepth)).append("</environment>" + Misc.LINE_SEPARATOR + Misc.LINE_SEPARATOR);
    indentDepth--;
    result.append(getIndent(indentDepth)).append("</environment-definition>" + Misc.LINE_SEPARATOR);

    return result.toString();
  }

  /**
   * Creates a new EnvironmentFactory from the list of entries.
   */
  public EnvironmentFactory createEnvironmentFactory() {
    final String envConfig = createEnvironmentXml();
    if (EnvGenerator.LOG.isLoggable(Level.CONFIG)) {
      EnvGenerator.LOG.config("The following environment has been generated by: " + this.getClass().getName()
          + Misc.LINE_SEPARATOR + envConfig);
    }
    return BonitaEnvironmentParser.parseEnvironmentFactoryFromXmlString(envConfig);
  }

  /**
   * Return a string that declares an object to be added in the context.
   * 
   * @param classToUse
   *          classToUse at runtime
   * @return a string that declares an object to be added in the context.
   */
  @SuppressWarnings("rawtypes")
  private static String getObjectDecl(final Class classToUse) {
    return "<object class='" + classToUse.getName() + "' />";
  }

  @SuppressWarnings("rawtypes")
  private static String getObjectDecl(final String key, final Class classToUse) {
    return "<object name='" + key + "' class='" + classToUse.getName() + "' />";
  }

  public static EnvEntry getEnvEntry(final String name, final String description, final String xml,
      final boolean enabled) {
    return new EnvEntry(name, description, xml, enabled);
  }

  public static EnvEntry getEnvEntry(final String name, final String description, final Class<?> classToUse,
      final boolean enabled) {
    final String xml = EnvGenerator.getObjectDecl(name, classToUse);
    return new EnvEntry(name, description, xml, enabled);
  }

  /**
   * Prints the default environment on standard output.
   * 
   * @param args
   * @throws IOException
   */
  @SuppressWarnings("unchecked")
  public static void main(final String[] args) throws IOException {
    if (args.length != 1 && args.length != 2) {
      System.exit(1);
    }
    EnvGenerator envGenerator = null;
    try {
      final Class<EnvGenerator> envGeneratorClass = (Class<EnvGenerator>) EnvGenerator.class.getClassLoader()
          .loadClass(args[0]);
      envGenerator = envGeneratorClass.newInstance();
    } catch (final Exception e) {
      throw new RuntimeException(e);
    }
    if (args.length == 1) {
      if (EnvGenerator.LOG.isLoggable(Level.INFO)) {
        EnvGenerator.LOG.config(envGenerator.createEnvironmentXml());
      }
    } else {
      final File file = new File(args[1]);
      final File folder = file.getParentFile();
      if (!folder.exists()) {
        folder.mkdirs();
      }
      file.createNewFile();
      Misc.write(envGenerator.createEnvironmentXml(), file);
    }
  }

  private String getIndent(final int depth) {
    final StringBuffer buf = new StringBuffer();
    for (int i = 0; i <= depth; i++) {
      buf.append(EnvGenerator.INDENT);
    }
    return buf.toString();
  }

  public static EnvEntry getChainerEnvEntry(final String key, final String description, final boolean enable,
      final Class<?>... classes) {
    final List<EnvEntry> chainerEntries = new ArrayList<EnvEntry>();
    for (final Class<?> clazz : classes) {
      chainerEntries.add(new EnvEntry(clazz.getName(), "", EnvGenerator.getObjectDecl(clazz), true));
    }
    return new ChainerEnvEntry(key, description, chainerEntries, enable);
  }

  public final void addApplicationEntry(final EnvEntry entry) {
    envFactoryEntries.put(entry.getName(), entry);
  }

  public final void addBlockEntry(final EnvEntry entry) {
    envEntries.put(entry.getName(), entry);
  }

  private StringBuilder getDefaultServerFolder() {
    final StringBuilder defaultServerPath = new StringBuilder("${");
    defaultServerPath.append(BonitaConstants.HOME).append("}").append(File.separator).append("server")
        .append(File.separator).append("default");
    return defaultServerPath;
  }
}
