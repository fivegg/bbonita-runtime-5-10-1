/**
 * Copyright (C) 2010-2013 BonitaSoft S.A.
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
package org.ow2.bonita.util;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.ow2.bonita.env.EnvConstants;
import org.ow2.bonita.env.Environment;
import org.ow2.bonita.env.EnvironmentFactory;
import org.ow2.bonita.env.GlobalEnvironmentFactory;
import org.ow2.bonita.identity.auth.DomainOwner;
import org.ow2.bonita.services.CommandService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Anthony Birembaut, Matthieu Chaffotte, Elias Ricken de Medeiros
 */
public final class DbMigration {

  // FIXME check job name
  private static final Logger LOG = LoggerFactory.getLogger(DbMigration.class);
  
  private DbMigration() {
  }

  public static void main(final String[] args) throws Exception {
    if (args == null || args.length != 3) {
      final String message = ExceptionManager.getInstance().getFullMessage("bh_DBM_1");
      throw new IllegalArgumentException(message);
    }
    // Check that bonita home is set
    BonitaConstants.getBonitaHomeFolder();
    final String domain = args[0];
    final String db = args[1].toLowerCase();
    final int stage = Integer.valueOf(args[2]);
    final String useSearch = "bonita.search.use";
    System.setProperty(useSearch, "false");
    LOG.info("Starting Migration on tenant: " + domain);
    try {
      if (stage < 2) {
        LOG.info("Stage 1: Updating history database schema...");
        updateDatabase(domain, EnvConstants.HB_CONFIG_HISTORY, db);
        LOG.info("Stage 1: DONE");
      }
      if (stage < 3) {
        LOG.info("Stage 2: Updating journal database schema...");
        updateDatabase(domain, EnvConstants.HB_CONFIG_CORE, db);
        cleanIOEvents(domain);
        updateJobsAndJobLocks(domain);
        LOG.info("Stage 2: DONE");
      }
      LOG.info("Migration on tenant " + domain + ": DONE");
    } finally {
      System.clearProperty(useSearch);
    }
  }

  private static void updateJobsAndJobLocks(final String domain) throws Exception {
    final EnvironmentFactory envFactory = GlobalEnvironmentFactory.getEnvironmentFactory(domain);
      final Environment environment = envFactory.openEnvironment();
      try {
        DomainOwner.setDomain(domain);
        final CommandService commandService = envFactory.get(CommandService.class);
        
        //create job locks for process definition and root process instances
        final UpdateJobLocksCommand creteJobLockscommand = new UpdateJobLocksCommand();
        commandService.execute(creteJobLockscommand);
        
        //update jobs by setting the procesUUID where it's currently null
        final UpdateJobsCommand updateJobsCommand = new UpdateJobsCommand();
        commandService.execute(updateJobsCommand);
      } finally {
        DomainOwner.setDomain(null);
        environment.close();
      }
  }

  private static void updateDatabase(final String domain, final String configurationName, final String db)
      throws Exception {
    final String path = getSQLScriptPath(db);
    migrateDb(domain, configurationName, db, path);
  }

  private static String getSQLScriptPath(final String db) {
    final StringBuilder migrationScript = new StringBuilder("/migration/");
    migrationScript.append(db).append("-5.9-5.10.sql");
    return migrationScript.toString();
  }

  public static void migrateDb(final String domain, final String configurationName, final String database,
      final String resourcePath) throws Exception {
    final SessionFactory sessionFactory = DbTool.getSessionFactory(domain,
        configurationName.replaceAll("-configuration", "-session-factory"));
    try {
      InputStream inputStream = null;
      try {
        inputStream = getScriptStream(resourcePath);
        executeScript(sessionFactory, inputStream, database);
      } finally {
        if (inputStream != null) {
          inputStream.close();
        }
      }
    } finally {
      sessionFactory.close();
    }
  }

  private static void cleanIOEvents(final String domain) throws Exception {
    final SessionFactory sessionFactory = DbTool.getSessionFactory(domain,
        EnvConstants.HB_CONFIG_CORE.replaceAll("-configuration", "-session-factory"));
    try {
      LOG.info("Clean Incoming Events... ");
      final String incomingQuery = "UPDATE org.ow2.bonita.runtime.event.IncomingEventInstance SET locked = FALSE WHERE locked = TRUE";
      int updated = executeQuery(sessionFactory, incomingQuery);
      LOG.info("update " + updated + " Incoming Events.");
      LOG.info("Clean Outgoing Events: ");
      final String outgoingQuery = "UPDATE org.ow2.bonita.runtime.event.OutgoingEventInstance SET locked = FALSE, incomingId = NULL WHERE locked = TRUE";
      updated = executeQuery(sessionFactory, outgoingQuery);
      LOG.info("update " + updated + " Outgoing Events.");
    } finally {
      sessionFactory.close();
    }
  }

  private static InputStream getScriptStream(final String resourcePath) {
    final InputStream inputStream = DbMigration.class.getResourceAsStream(resourcePath);
    if (inputStream == null) {
      final String message = ExceptionManager.getInstance().getFullMessage("bh_DBM_2");
      throw new IllegalArgumentException(message);
    }
    return inputStream;
  }

  public static int executeQuery(final SessionFactory sessionFactory, final String query) {
    final Session session = sessionFactory.openSession();
    session.getTransaction().begin();
    int i = 0;
    try {
      i = session.createQuery(query).executeUpdate();
    } catch (final Exception e) {
      LOG.error("Error while executing command: " + query, e);
    }
    session.getTransaction().commit();
    session.close();
    return i;
  }

  public static void executeScript(final SessionFactory sessionFactory, final InputStream inputStream, final String db) {
    final byte[] bytes = IoUtil.readBytes(inputStream);
    final String scriptContent = new String(bytes);
    final List<String> commands = getCommands(scriptContent, db);

    final Session session = sessionFactory.openSession();
    session.getTransaction().begin();

    LOG.info("DB Commands Execution: " + commands.size());
    for (final String command : commands) {
      LOG.info("Executing command : " + command);
      try {
        session.createSQLQuery(command).executeUpdate();
      } catch (final Exception e) {
        LOG.error("Error while executing command: " + command, e);
      }
    }
    session.getTransaction().commit();
    session.close();
  }

  public static List<String> getCommands(final String scriptContent, final String db) {
    String delimiter = ";";
    if ("sqlserver".equals(db) || "sybase".equals(db)) {
      delimiter = "go";
    }
    final String regex = delimiter.concat("\r?\n");
    final List<String> commands = new ArrayList<String>();
    final String[] tmp = scriptContent.split(regex);
    for (final String command : tmp) {
      if (command.trim().length() > 0) {
        commands.add(command.trim());
      }
    }
    final int lastIndex = commands.size() - 1;
    if (lastIndex >= 0) {
      String lastCommand = commands.get(lastIndex);
      final int index = lastCommand.lastIndexOf(delimiter);
      if (index > 0) {
        lastCommand = lastCommand.substring(0, index);
        commands.remove(lastIndex);
        commands.add(lastCommand);
      }
    }
    return commands;
  }
  
}
