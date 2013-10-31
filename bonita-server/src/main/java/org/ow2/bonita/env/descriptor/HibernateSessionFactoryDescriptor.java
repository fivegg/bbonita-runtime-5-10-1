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
 * 
 * Modified by Matthieu Chaffotte - BonitaSoft S.A. 
 */
package org.ow2.bonita.env.descriptor;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.hibernate.EmptyInterceptor;
import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.ow2.bonita.env.Descriptor;
import org.ow2.bonita.env.WireContext;
import org.ow2.bonita.env.WireDefinition;
import org.ow2.bonita.env.WireException;
import org.ow2.bonita.search.SearchUtil;
import org.ow2.bonita.util.ExceptionManager;
import org.ow2.bonita.util.Listener;
import org.ow2.bonita.util.Misc;
import org.ow2.bonita.util.ReflectUtil;

/**
 * @author Tom Baeyens
 */
public class HibernateSessionFactoryDescriptor extends AbstractDescriptor {

  private static final long serialVersionUID = 1L;
  private static final Logger LOG = Logger.getLogger(HibernateSessionFactoryDescriptor.class.getName());

  String configurationName;
  Descriptor configurationDescriptor;

  @Override
  public Object construct(final WireContext wireContext) {
    Configuration configuration = null;

    if (configurationName != null) {
      configuration = (Configuration) wireContext.get(configurationName);
    } else if (configurationDescriptor != null) {
      configuration = (Configuration) wireContext.create(configurationDescriptor, false);
    } else {
      configuration = wireContext.get(Configuration.class);
    }

    if (configuration == null) {
      final String message = ExceptionManager.getInstance().getFullMessage("bp_HSF_1");
      throw new WireException(message);
    }
    SearchUtil.addSearchConfiguration(configuration);
    addInterceptor(configuration);

    final SessionFactory sessionFactory = configuration.buildSessionFactory();
    wireContext.addListener(new SessionFactoryCloser(sessionFactory));
    return sessionFactory;
  }

  private static void addInterceptor(final Configuration configuration) {
    final String interceptorClassName = configuration.getProperty("bonita.hibernate.interceptor");
    if (interceptorClassName != null) {
      if (LOG.isLoggable(Level.INFO)) {
        LOG.info("Adding interceptor: " + interceptorClassName);
      }
      final Class<?> interceptorClass = ReflectUtil.loadClass(Thread.currentThread().getContextClassLoader(),
          interceptorClassName);
      final EmptyInterceptor interceptor = (EmptyInterceptor) ReflectUtil.newInstance(interceptorClass);
      configuration.setInterceptor(interceptor);
    }
  }

  public static class SessionFactoryCloser implements Listener {
    SessionFactory sessionFactory;

    public SessionFactoryCloser(final SessionFactory sessionFactory) {
      this.sessionFactory = sessionFactory;
    }

    @Override
    public void event(final Object source, final String eventName, final Object info) {
      if (WireContext.EVENT_CLOSE.equals(eventName)) {
        if (LOG.isLoggable(Level.FINE)) {
          LOG.fine("closing hibernate session factory");
        }
        try {
          sessionFactory.close();
        } catch (final HibernateException e) {
          if ("could not destruct listeners".equals(e.getMessage())) {
            LOG.severe("Unable to destruct listener: " + Misc.getStackTraceFrom(e));
          } else {
            throw e;
          }
        }
      }
    }
  }

  @Override
  public Class<?> getType(final WireDefinition wireDefinition) {
    return SessionFactory.class;
  }

  public void setConfigurationName(final String configurationName) {
    this.configurationName = configurationName;
  }

  public void setConfigurationDescriptor(final Descriptor configurationDescriptor) {
    this.configurationDescriptor = configurationDescriptor;
  }

}
