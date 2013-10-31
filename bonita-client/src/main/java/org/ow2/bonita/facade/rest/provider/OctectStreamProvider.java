/**
 * Copyright (C) 2010  BonitaSoft S.A.
 * BonitaSoft, 31 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA  02110-1301, USA.
 **/
package org.ow2.bonita.facade.rest.provider;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.ow2.bonita.facade.def.element.BusinessArchive;
import org.ow2.bonita.util.Misc;

/**
 * @author Elias Ricken de Medeiros, Matthieu Chaffotte
 * 
 */
@Provider
@Consumes("application/octet-stream")
@Produces("application/octet-stream")
public class OctectStreamProvider implements MessageBodyReader<BusinessArchive>, MessageBodyWriter<BusinessArchive> {

  private static final Logger LOG = Logger.getLogger(OctectStreamProvider.class.getName());
  static final int BUFF_SIZE = 100000;
  static final byte[] BUFFER = new byte[BUFF_SIZE];

  private static void readFromInputStream(final InputStream in, final OutputStream out) throws IOException {
    while (true) {
      synchronized (BUFFER) {
        final int amountRead = in.read(BUFFER);
        if (amountRead == -1) {
          break;
        }
        out.write(BUFFER, 0, amountRead);
      }
    }
  }

  @Override
  public boolean isReadable(final Class<?> type, final Type genericType, final Annotation[] annotations,
      final MediaType mediaType) {
    return isReadableOrWritable(genericType);
  }

  private boolean isReadableOrWritable(final Type genericType) {
    return genericType.equals(BusinessArchive.class);
  }

  @Override
  public BusinessArchive readFrom(final Class<BusinessArchive> type, final Type genericType,
      final Annotation[] annotations, final MediaType mediaType, final MultivaluedMap<String, String> httpHeaders,
      final InputStream entityStream) throws IOException, WebApplicationException {
    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    Serializable serializable = null;
    try {
      readFromInputStream(entityStream, out);
      serializable = Misc.deserialize(out.toByteArray());
    } catch (final ClassNotFoundException e) {
      if (LOG.isLoggable(Level.SEVERE)) {
        LOG.severe("Error while reading the InputStream: " + Misc.getStackTraceFrom(e));
      }
    } catch (final IOException e) {
      if (LOG.isLoggable(Level.SEVERE)) {
        LOG.severe("Error while reading the InputStream: " + Misc.getStackTraceFrom(e));
      }
    } finally {
      out.close();
    }

    BusinessArchive businessArchive = null;
    if (serializable instanceof BusinessArchive) {
      businessArchive = (BusinessArchive) serializable;
    }
    return businessArchive;
  }

  @Override
  public long getSize(final BusinessArchive t, final Class<?> type, final Type genericType,
      final Annotation[] annotations, final MediaType mediaType) {
    return -1;
  }

  @Override
  public boolean isWriteable(final Class<?> type, final Type genericType, final Annotation[] annotations,
      final MediaType mediaType) {
    return isReadableOrWritable(genericType);
  }

  @Override
  public void writeTo(final BusinessArchive t, final Class<?> type, final Type genericType,
      final Annotation[] annotations, final MediaType mediaType, final MultivaluedMap<String, Object> httpHeaders,
      final OutputStream entityStream) throws IOException, WebApplicationException {
    try {
      entityStream.write(Misc.serialize(t));
    } catch (final ClassNotFoundException e) {
      if (LOG.isLoggable(Level.SEVERE)) {
        LOG.severe("Error while serializing the object: " + Misc.getStackTraceFrom(e));
      }
    } catch (final IOException e) {
      if (LOG.isLoggable(Level.SEVERE)) {
        LOG.severe("Error while serializing the object: " + Misc.getStackTraceFrom(e));
      }
    }
  }

}
