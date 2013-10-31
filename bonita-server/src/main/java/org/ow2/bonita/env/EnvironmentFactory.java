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

import java.io.Serializable;

/**
 * factory for {@link Environment}s.
 * 
 * <p>
 * Default implementation is {@link PvmEnvironmentFactory}. EnvironmentFactory
 * is thread safe, you should use one environment factory for all your threads.
 * </p>
 * 
 * <p>
 * For the default parser's XML schema, see {@link PvmEnvironmentFactoryParser}.
 * </p>
 * 
 * @author Tom Baeyens
 */
public abstract class EnvironmentFactory implements Context, Serializable {

  /**
   * 
   */
  private static final long serialVersionUID = -909370824696632925L;

  /**
   * open a new Environment. The client is responsible for closing the
   * environment with {@link Environment#close()}.
   */
  public abstract Environment openEnvironment();

  /**
   * closes this environment factory and cleans any allocated resources.
   */
  public abstract void close();

}
