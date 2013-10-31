/**
 * Copyright (C) 2012  BonitaSoft S.A.
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

import org.ow2.bonita.util.Misc;

/**
 * 
 * @author Matthieu Chaffotte
 * 
 */
@Provider
@Consumes("application/octet-stream")
@Produces("application/octet-stream")
public class ByteArrayProvider implements MessageBodyReader<byte[]>, MessageBodyWriter<byte[]> {

  private static final Logger LOG = Logger.getLogger(ByteArrayProvider.class.getName());

  @Override
  public boolean isReadable(final Class<?> type, final Type genericType, final Annotation[] annotations,
      final MediaType mediaType) {
    return type.equals(byte[].class);
  }

  @Override
  public byte[] readFrom(final Class<byte[]> type, final Type genericType, final Annotation[] annotations,
      final MediaType mediaType, final MultivaluedMap<String, String> httpHeaders, final InputStream entityStream)
      throws IOException, WebApplicationException {
    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    final byte[] buffer = new byte[1024];
    try {
      int read;
      while ((read = entityStream.read(buffer)) != -1) {
        if (read > 0) {
          baos.write(buffer, 0, read);
        }
      }
      return baos.toByteArray();
    } finally {
      baos.close();
    }
  }

  @Override
  public long getSize(final byte[] t, final Class<?> type, final Type genericType, final Annotation[] annotations,
      final MediaType mediaType) {
    return t.length;
  }

  @Override
  public boolean isWriteable(final Class<?> type, final Type genericType, final Annotation[] annotations,
      final MediaType mediaType) {
    return type.equals(byte[].class);
  }

  @Override
  public void writeTo(final byte[] t, final Class<?> type, final Type genericType, final Annotation[] annotations,
      final MediaType mediaType, final MultivaluedMap<String, Object> httpHeaders, final OutputStream entityStream)
      throws IOException, WebApplicationException {
    try {
      entityStream.write(t);
    } catch (final IOException e) {
      if (LOG.isLoggable(Level.SEVERE)) {
        LOG.severe("Error while serializing the object: " + Misc.getStackTraceFrom(e));
      }
    }
  }

}
