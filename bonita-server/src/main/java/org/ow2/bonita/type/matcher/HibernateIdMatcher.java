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
package org.ow2.bonita.type.matcher;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.hibernate.SessionFactory;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.type.Type;
import org.ow2.bonita.env.Environment;
import org.ow2.bonita.type.Matcher;

/**
 * @author Tom Baeyens
 */
public abstract class HibernateIdMatcher implements Matcher {

  private static final long serialVersionUID = 1L;
  private static final Logger LOG = Logger.getLogger(HibernateIdMatcher.class.getName());

  String hibernateSessionFactoryName;

  public HibernateIdMatcher(String hibernateSessionFactoryName) {
    this.hibernateSessionFactoryName = hibernateSessionFactoryName;
  }

  protected abstract Class<? extends Type> getIdType();

  public boolean matches(String name, Object value) {
    boolean matches = false;

    Environment environment = Environment.getCurrent();
    if (environment != null) {
      SessionFactory sessionFactory = null;
      if (hibernateSessionFactoryName != null) {
        sessionFactory = (SessionFactory) environment
            .get(hibernateSessionFactoryName);
      } else {
        sessionFactory = environment.get(SessionFactory.class);
      }
      if (sessionFactory != null) {
        ClassMetadata classMetadata = sessionFactory.getClassMetadata(value
            .getClass());
        matches = ((classMetadata != null) && (classMetadata
            .getIdentifierType().getClass() == getIdType()));
      }
    } else {
    	if (LOG.isLoggable(Level.FINE)) {
        LOG.fine("no current environment so valueClass cannot be stored as an id-ref to a hibernate object");
    	}
      matches = false;
    }
    return matches;
  }

  public String getHibernateSessionFactoryName() {
    return hibernateSessionFactoryName;
  }

  public void setHibernateSessionFactoryName(String hibernateSessionFactoryName) {
    this.hibernateSessionFactoryName = hibernateSessionFactoryName;
  }
}
