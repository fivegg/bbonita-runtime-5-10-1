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
package org.ow2.bonita.runtime.tx;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.hibernate.Session;

/**
 * @author Tom Baeyens
 */
public class HibernateSessionResource implements StandardResource {

	static final Logger LOG = Logger.getLogger(HibernateSessionResource.class.getName());

  protected Session session;
  protected org.hibernate.Transaction transaction;

  public HibernateSessionResource(Session session) {
    this.session = session;
    if (LOG.isLoggable(Level.FINE)) {
      LOG.fine("beginning transaction on hibernate session " + System.identityHashCode(session));
    }

    try {
      this.transaction = session.beginTransaction();
    } catch (RuntimeException e) {
    	if (LOG.isLoggable(Level.SEVERE)) {
        LOG.severe("hibernate transaction begin failed.  closing hibernate session: " + e);
    	}
      session.close();
      throw e;
    }

    if (LOG.isLoggable(Level.FINE)) {
      LOG.fine("begun hibernate transaction "
        + System.identityHashCode(transaction) + " on hibernate session "
        + System.identityHashCode(session));
    }
  }

  public void prepare() {
  	if (LOG.isLoggable(Level.FINE)) {
      LOG.fine("flushing hibernate session " + System.identityHashCode(session));
  	}
    session.flush();
  }

  public void commit() {
  	if (LOG.isLoggable(Level.FINE)) {
      LOG.fine("committing hibernate transaction " + System.identityHashCode(transaction));
  	}
    try {
      transaction.commit();
    } finally {
      closeSession();
    }
  }

  private void closeSession() {
  	if (LOG.isLoggable(Level.FINE)) {
      LOG.fine("closing hibernate session " + System.identityHashCode(session));
  	}
    session.close();
  }

  public void rollback() {
  	if (LOG.isLoggable(Level.FINE)) {
      LOG.fine("rolling back hibernate transaction " + System.identityHashCode(transaction));
  	}
    try {
      transaction.rollback();
    } finally {
      closeSession();
    }
  }
}
