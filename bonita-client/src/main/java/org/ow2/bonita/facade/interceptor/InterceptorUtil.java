/**
 * Copyright (C) 2006  Bull S. A. S.
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
 * Modified by Charles Souillard, Matthieu Chaffotte - BonitaSoft S.A.
 **/
package org.ow2.bonita.facade.interceptor;

import java.lang.reflect.Method;

import org.ow2.bonita.util.BonitaException;

/**
 * Util class used by the engine to access EJB2 API Accessor and manage invocation exception
 * @author Thomas Gueze
 *
 */
public final class InterceptorUtil {

  private InterceptorUtil() { }

  /**
   * Throw the throwable passed if it's an RuntimeException or if it's an exception declared by the method
   * If not, throw nothing and return
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public static void manageInvokeExceptionCause(final Method m, final Throwable t) throws BonitaException {
    if (t instanceof RuntimeException) {
      throw (RuntimeException) t;
    } else if (t instanceof Error) {
      throw (Error) t;
    }
    final Class[] throwedClasses = m.getExceptionTypes();
    for (final Class clazz : throwedClasses) {
      if (t != null && clazz.isAssignableFrom(t.getClass())) {
        throw (BonitaException) t;
      }
    }
  }

}
