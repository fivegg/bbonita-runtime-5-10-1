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
package org.ow2.bonita.type.converter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.Serializable;

import org.ow2.bonita.type.Converter;
import org.ow2.bonita.util.BonitaRuntimeException;
import org.ow2.bonita.util.ExceptionManager;

public class SerializableToBytesConverter implements Converter {

  private static final long serialVersionUID = 1L;

  @Override
  public boolean supports(final Object value) {
    return value == null || Serializable.class.isAssignableFrom(value.getClass());
  }

  @Override
  public Object convert(final Object o) {
    byte[] bytes = null;
    ObjectOutputStream oos = null;
    try {
      final ByteArrayOutputStream baos = new ByteArrayOutputStream();
      oos = new ObjectOutputStream(baos);
      oos.writeObject(o);
      oos.flush();
      bytes = baos.toByteArray();
    } catch (final IOException e) {
      final String message = ExceptionManager.getInstance().getFullMessage("bp_STBC_1", o);
      throw new BonitaRuntimeException(message, e);
    } finally {
      try {
        if (oos != null) {
          oos.close();
        }
      } catch (final IOException e) {
        final String message = ExceptionManager.getInstance().getFullMessage("bp_STBC_1", o);
        throw new BonitaRuntimeException(message, e);
      }
    }
    return bytes;
  }

  @Override
  public Object revert(final Object o) {
    final byte[] bytes = (byte[]) o;
    ObjectInputStream ois = null;
    try {
      final ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
      ois = new ObjectInputStream(bais) {

        @Override
        protected Class<?> resolveClass(final ObjectStreamClass desc) throws IOException, ClassNotFoundException {
          final String className = desc.getName();
          final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
          return Class.forName(className, true, classLoader);
        }
      };

      return ois.readObject();
    } catch (final Exception e) {
      final String message = ExceptionManager.getInstance().getFullMessage("bp_STBC_2");
      throw new BonitaRuntimeException(message, e);
    } finally {
      if (ois != null) {
        try {
          ois.close();
        } catch (final IOException e) {
          final String message = ExceptionManager.getInstance().getFullMessage("bp_STBC_2");
          throw new BonitaRuntimeException(message, e);
        }
      }
    }
  }

}
