/*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.ow2.bonita.env.descriptor;

import java.sql.Connection;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.ow2.bonita.env.Environment;
import org.ow2.bonita.env.WireContext;
import org.ow2.bonita.env.WireDefinition;
import org.ow2.bonita.env.WireException;
import org.ow2.bonita.runtime.tx.HibernateSessionResource;
import org.ow2.bonita.runtime.tx.StandardTransaction;
import org.ow2.bonita.util.ExceptionManager;

/**
 * @author Tom Baeyens
 */
public class HibernateSessionDescriptor extends AbstractDescriptor {

  private static final long serialVersionUID = 1L;
  private static final Logger LOG = Logger.getLogger(HibernateSessionDescriptor.class.getName());

  protected String factoryName;
  protected boolean useCurrent = false;
  protected boolean tx = true;
  protected boolean close = true;
  protected String standardTransactionName;
  protected String connectionName;

  public Object construct(WireContext wireContext) {
    Environment environment = Environment.getCurrent();
    if (environment == null) {
      throw new WireException("no environment");
    }

    // get the hibernate-session-factory
    SessionFactory sessionFactory = null;
    if (factoryName != null) {
      sessionFactory = (SessionFactory) wireContext.get(factoryName);
    } else {
      sessionFactory = environment.get(SessionFactory.class);
    }
    if (sessionFactory == null) {
    	String message = ExceptionManager.getInstance().getFullMessage(
    			"bp_HSD_1", (factoryName != null ? "'" + factoryName + "'" : "by type "));
      throw new WireException(message);
    }

    // open the hibernate-session
    Session session = null;
    if (useCurrent) {
    	if (LOG.isLoggable(Level.FINE)) {
        LOG.fine("getting current hibernate session");
    	}
      session = sessionFactory.getCurrentSession();

    } else if (connectionName != null) {
      Connection connection = (Connection) wireContext.get(connectionName);
      if (LOG.isLoggable(Level.FINE)) {
        LOG.fine("creating hibernate session with connection " + connection);
      }
      session = sessionFactory.openSession(connection);

    } else {
    	if (LOG.isLoggable(Level.FINE)) {
        LOG.fine("creating hibernate session");
    	}
      session = sessionFactory.openSession();
    }

    StandardTransaction standardTransaction = environment
        .get(StandardTransaction.class);
    if (standardTransaction != null) {
      HibernateSessionResource hibernateSessionResource = new HibernateSessionResource(
          session);
      standardTransaction.enlistResource(hibernateSessionResource);
    }

    return session;
  }

  public Class<?> getType(WireDefinition wireDefinition) {
    return Session.class;
  }

  public void setFactoryName(String factoryName) {
    this.factoryName = factoryName;
  }

  public void setTx(boolean tx) {
    this.tx = tx;
  }

  public void setStandardTransactionName(String standardTransactionName) {
    this.standardTransactionName = standardTransactionName;
  }

  public void setConnectionName(String connectionName) {
    this.connectionName = connectionName;
  }

  public void setUseCurrent(boolean useCurrent) {
    this.useCurrent = useCurrent;
  }

  public void setClose(boolean close) {
    this.close = close;
  }
}
