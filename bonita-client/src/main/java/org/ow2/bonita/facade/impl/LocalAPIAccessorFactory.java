/**
 * Copyright (C) 2007  Bull S. A. S.
 * Bull, Rue Jean Jaures, B.P.68, 78340, Les Clayes-sous-Bois
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA  02110-1301, USA.
 * 
 * Modified by Elias Ricken de Medeiros - BonitaSoft S.A.
 **/
package org.ow2.bonita.facade.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.ow2.bonita.facade.exception.BonitaInternalException;
import org.ow2.bonita.facade.internal.InternalAPIAccessor;
import org.ow2.bonita.facade.internal.InternalQueryAPIAccessor;
import org.ow2.bonita.util.BonitaRuntimeException;
import org.ow2.bonita.util.ExceptionManager;


/**
 * @author Guillaume Porcher
 *
 */
public final class LocalAPIAccessorFactory {

  private LocalAPIAccessorFactory() { }

  private static final Object LOCK = new Object();
  private static Properties mapping = null;

  private static Properties getMapping() throws IOException {
    synchronized (LOCK) {
      if (mapping == null) {
        final Properties map = new Properties();
        final ClassLoader cl = Thread.currentThread().getContextClassLoader();
        final InputStream inStream = cl.getResourceAsStream("org.ow2.bonita.api.implementations");
        if (inStream != null) {
          try {
            map.load(inStream);
          } finally {
            inStream.close();
          }
        } else {
        	String message = ExceptionManager.getInstance().getFullMessage("baa_SIAPIAF_1");
          throw new BonitaInternalException(message);
        }
        mapping = map;
      }
    }
    return mapping;
  }

  @SuppressWarnings("unchecked")
  private static <T> T getObject(final Class<T> clazz, final boolean isREST) {
    try {
      String implementationClassName = null;
      if(isREST){
      	implementationClassName = getMapping().getProperty(clazz.getName()+"REST");
      } else {
      	implementationClassName = getMapping().getProperty(clazz.getName());
      }
      if (implementationClassName == null) {
      	String message = ExceptionManager.getInstance().getFullMessage("baa_SIAPIAF_2", clazz.getName());
        throw new BonitaInternalException(message);
      }
      final ClassLoader cl = Thread.currentThread().getContextClassLoader();
      final Class<T> implClass = (Class<T>) cl.loadClass(implementationClassName);
      return implClass.newInstance();
    } catch (final Exception e) {
      throw new BonitaRuntimeException(e);
    }
  }

  public static InternalAPIAccessor getStandardServerAPIAccessor() {
    return getObject(InternalAPIAccessor.class, false);
  }

  public static InternalQueryAPIAccessor getStandardServerQueryAPIAccessor() {
    return getObject(InternalQueryAPIAccessor.class, false);
  }
  
  public static InternalAPIAccessor getRESTServerAPIAccessor(){
  	return getObject(InternalAPIAccessor.class, true);
  }
  
  public static InternalQueryAPIAccessor getRESTServertQueryAPIAccessor(){
  	return getObject(InternalQueryAPIAccessor.class, true);
  }

}
