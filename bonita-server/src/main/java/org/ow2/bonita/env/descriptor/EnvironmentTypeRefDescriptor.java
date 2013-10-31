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

import org.ow2.bonita.env.Environment;
import org.ow2.bonita.env.WireContext;
import org.ow2.bonita.env.WireException;

/**
 * @author Tom Baeyens
 */
public class EnvironmentTypeRefDescriptor extends AbstractDescriptor {

  private static final long serialVersionUID = 1L;

  private static final Logger LOG = Logger.getLogger(EnvironmentTypeRefDescriptor.class.getName());

  Class<?> type;

  public EnvironmentTypeRefDescriptor(final Class<?> type) {
    super();
    this.type = type;
  }

  @Override
  public Object construct(final WireContext wireContext) {
    if (LOG.isLoggable(Level.FINE)) {
      LOG.fine("looking up " + type + " by type in environment");
    }
    if (type != null) {
      final Environment environment = Environment.getCurrent();
      if (environment == null) {
        throw new WireException("no environment to search an object of type " + type.getName());
      }
      return environment.get(type);
    }
    return null;
  }

}
