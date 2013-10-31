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

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.MalformedURLException;

import org.ow2.bonita.util.BonitaRuntimeException;
import org.ow2.bonita.util.ExceptionManager;

/**
 * @author Tom Baeyens
 */
public class FileStreamSource extends StreamSource {

  protected File file;

  /**
   * @throws BonitaRuntimeException
   *           if file is null
   */
  public FileStreamSource(final File file) {
    super();
    if (file == null) {
      final String message = ExceptionManager.getInstance().getFullMessage("bp_FSS_1");
      throw new BonitaRuntimeException(message);
    }

    try {
      name = file.toURL().toString();
    } catch (final MalformedURLException e) {
      name = file.toString();
    }

    this.file = file;
  }

  @Override
  public InputStream openStream() {
    InputStream stream = null;
    try {
      if (!file.exists()) {
        final String message = ExceptionManager.getInstance().getFullMessage("bp_FSS_2", file);
        throw new BonitaRuntimeException(message);
      }
      if (file.isDirectory()) {
        final String message = ExceptionManager.getInstance().getFullMessage("bp_FSS_3", file);
        throw new BonitaRuntimeException(message);
      }
      stream = new FileInputStream(file);
    } catch (final Exception e) {
      final String message = ExceptionManager.getInstance().getFullMessage("bp_FSS_4", file, e.getMessage());
      throw new BonitaRuntimeException(message, e);
    }
    return stream;
  }

}
