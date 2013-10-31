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
package org.ow2.bonita.util;

import org.hibernate.CacheMode;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.MassIndexer;
import org.hibernate.search.Search;
import org.ow2.bonita.env.EnvConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Matthieu Chaffotte, Elias Ricken de Medeiros
 * 
 */
public final class IndexTool {

  private static final Logger LOG = LoggerFactory.getLogger(IndexTool.class);
  private static final String REINDEX_DOMAIN_PROPERTY = "org.ow2.bonita.reindex.domain";

  public static void main(final String[] args) throws Exception {
    if (args == null || args.length != 3) {
      final String message = ExceptionManager.getInstance().getFullMessage("bh_DBM_3");
      throw new IllegalArgumentException(message);
    }
    final String domain = args[0];
    final int nbOfThreadLoad = Integer.valueOf(args[1]);
    final int nbOfThreadFetching = Integer.valueOf(args[2]);

    LOG.info("Indexing History DB");
    populateIndexes(domain, EnvConstants.HB_CONFIG_HISTORY, nbOfThreadLoad, nbOfThreadFetching);
    LOG.info("Indexing Core DB");
    populateIndexes(domain, EnvConstants.HB_CONFIG_CORE, nbOfThreadLoad, nbOfThreadFetching);
  }

  private static void populateIndexes(final SessionFactoryImplementor sessionFactory, final int nbOfThreadLoad,
      final int nbOfThreadFetching) throws InterruptedException {
    FullTextSession fullTextSession = null;
    try {
      final Session session = sessionFactory.openSession();
      fullTextSession = Search.getFullTextSession(session);
      final Transaction tx = fullTextSession.beginTransaction();
      final MassIndexer indexer = fullTextSession.createIndexer();
      indexer.batchSizeToLoadObjects(25).cacheMode(CacheMode.NORMAL).optimizeOnFinish(true);
      indexer.threadsToLoadObjects(nbOfThreadLoad).threadsForSubsequentFetching(nbOfThreadFetching).startAndWait();
      fullTextSession.createIndexer();
      tx.commit();
    } finally {
      if (fullTextSession != null) {
        fullTextSession.close();
      }
    }
  }

  public static void populateIndexes(final String domain, final String configurationName, final int nbOfThreadLoad,
      final int nbOfThreadFetching) throws Exception {
    System.setProperty(REINDEX_DOMAIN_PROPERTY, domain);
    SessionFactoryImplementor sessionFactory = null;
    try {
      BonitaConstants.getBonitaHomeFolder();
      final Configuration cfg = DbTool.getConfiguration(domain, configurationName);
      if ("true".equals(cfg.getProperty("bonita.search.use"))) {
        sessionFactory = DbTool.getSessionFactory(domain,
            configurationName.replaceAll("-configuration", "-session-factory"));
        if (sessionFactory != null) {
          populateIndexes(sessionFactory, nbOfThreadLoad, nbOfThreadFetching);
        }
      } else {
        LOG.info(configurationName + "does not support indexing");
      }
    } finally {
      System.clearProperty(REINDEX_DOMAIN_PROPERTY);
      closeSession(sessionFactory);
    }
  }

  private static void closeSession(final SessionFactoryImplementor sessionFactory) {
    if (sessionFactory != null) {
      try {
        sessionFactory.close();
      } catch (final Exception e) {
        if (LOG.isErrorEnabled()) {
          LOG.error("Problems while closing section factory: ", e);
        }
      }
    }
  }

}
