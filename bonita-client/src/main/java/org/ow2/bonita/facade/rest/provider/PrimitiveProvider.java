/**
 * Copyright (C) 2012 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
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

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

/**
 * 
 * @author Matthieu Chaffotte
 * 
 */
@Provider
@Consumes("*/*")
@Produces("*/*")
public class PrimitiveProvider implements MessageBodyWriter<Object> {

  @Override
  public boolean isWriteable(final Class<?> type, final Type genericType, final Annotation[] annotations,
      final MediaType mediaType) {
    boolean isReadable = false;
    if (type != null) {
      isReadable = type.isPrimitive() || isPrimitiveWrapper(type);
    }
    return isReadable;
  }

  @Override
  public long getSize(final Object t, final Class<?> type, final Type genericType, final Annotation[] annotations,
      final MediaType mediaType) {
    return -1;
  }

  @Override
  public void writeTo(final Object t, final Class<?> type, final Type genericType, final Annotation[] annotations,
      final MediaType mediaType, final MultivaluedMap<String, Object> httpHeaders, final OutputStream entityStream)
      throws IOException, WebApplicationException {
    entityStream.write(t.toString().getBytes("UTF-8"));
  }

  private boolean isPrimitiveWrapper(final Class<?> type) {
    return String.class.equals(type) || Boolean.class.equals(type) || Integer.class.equals(type)
        || Long.class.equals(type) || Double.class.equals(type) || Short.class.equals(type) || Byte.class.equals(type)
        || Float.class.equals(type) || Character.class.equals(type);
  }

}
