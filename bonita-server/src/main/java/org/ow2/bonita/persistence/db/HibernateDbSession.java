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
package org.ow2.bonita.persistence.db;

import java.io.Serializable;

import org.hibernate.Session;
import org.ow2.bonita.persistence.DbSession;

/**
 * @author Tom Baeyens
 */
public class HibernateDbSession implements DbSession {

  protected Session session;

  public void close() {
    session.close();
  }

  public <T> T get(Class<T> entityClass, Object primaryKey) {
    return entityClass
        .cast(session.get(entityClass, (Serializable) primaryKey));
  }

  public void flush() {
    session.flush();
  }

  public void save(Object entity) {
    session.persist(entity);
  }

  public void delete(Object entity) {
    session.delete(entity);
  }

  public Session getSession() {
    return session;
  }

  public void setSession(Session session) {
    this.session = session;
  }
}
