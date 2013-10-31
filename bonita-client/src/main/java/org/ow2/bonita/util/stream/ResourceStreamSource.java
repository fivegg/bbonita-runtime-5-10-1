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
package org.ow2.bonita.util.stream;

import java.io.InputStream;

import org.ow2.bonita.util.BonitaRuntimeException;
import org.ow2.bonita.util.ExceptionManager;
import org.ow2.bonita.util.ReflectUtil;

/**
 * @author Tom Baeyens
 */
public class ResourceStreamSource extends StreamSource {

  protected ClassLoader classLoader;

  protected String resource;

  /**
   * @throws BonitaRuntimeException
   *           if resource is null
   */
  public ResourceStreamSource(final String resource) {
    this(resource, null);
  }

  /**
   * @throws BonitaRuntimeException
   *           if resource is null
   */
  public ResourceStreamSource(final String resource, final ClassLoader classLoader) {
    super();
    if (resource == null) {
      final String message = ExceptionManager.getInstance().getFullMessage("bp_RSS_1");
      throw new BonitaRuntimeException(message);
    }
    name = "resource://" + resource;
    this.resource = resource;
    this.classLoader = classLoader;
  }

  @Override
  public InputStream openStream() {
    final InputStream stream = ReflectUtil.getResourceAsStream(classLoader, resource);
    if (stream == null) {
      final String message = ExceptionManager.getInstance().getFullMessage("bp_RSS_2");
      throw new BonitaRuntimeException(message);
    }
    return stream;
  }

}
