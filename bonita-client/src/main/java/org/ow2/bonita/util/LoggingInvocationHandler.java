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
 **/
package org.ow2.bonita.util;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.logging.Logger;

/**
 * @author Marc Blachon, Guillaume Porcher, Charles Souillard, Miguel Valdes, Pierre Vigneras
 */
public class LoggingInvocationHandler<T> implements InvocationHandler {

  private final T target;
  private final Logger logger;

  public LoggingInvocationHandler(final T target, final Logger logger) {
    super();
    this.target = target;
    this.logger = logger;
  }

  @Override
  public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
    if (method.getName().equals("toString")) {
      return proxyToString();
    }
    if (method.getName().equals("equals") && args.length == 1) {
      return proxyEquals(proxy, args[0]);
    }
    final String className = method.getDeclaringClass().getName();
    final String methodName = method.getName();
    logger.entering(className, methodName, args);
    final Object ret = method.invoke(target, args);
    logger.exiting(className, methodName, ret);
    return ret;
  }

  private Object proxyToString() {
    return "Proxy(" + target.toString() + ")";
  }

  private Object proxyEquals(final Object proxy, final Object object) {
    return proxy == object;
  }

}
