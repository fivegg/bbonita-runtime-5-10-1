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
package org.ow2.bonita.env;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.ow2.bonita.util.Closable;

/**
 * @author Tom Baeyens
 */
public class PvmEnvironment extends BasicEnvironment {

  private static final long serialVersionUID = 1L;

  private static final Logger LOG = Logger.getLogger(PvmEnvironment.class.getName());

  protected PvmEnvironmentFactory pvmEnvironmentFactory;

  public PvmEnvironment(final PvmEnvironmentFactory pvmEnvironmentFactory) {
    super();
    this.pvmEnvironmentFactory = pvmEnvironmentFactory;
  }

  @Override
  public String toString() {
    return "PvmEnvironment[" + System.identityHashCode(this) + "]";
  }

  @Override
  public void close() {
    final Context context = getEnvironmentContext();
    if (context instanceof Closable) {
      ((Closable) context).close();
    }
    super.close();
    if (LOG.isLoggable(Level.FINE)) {
      LOG.fine("closed " + this);
    }
  }

}
