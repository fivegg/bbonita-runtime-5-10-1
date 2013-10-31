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
 * Modified by Matthieu Chaffotte - BonitaSoft
 **/
package org.ow2.bonita.util;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Generic chainer.
 * 
 * This {@link InvocationHandler} allows chaining of <strong>void only</strong> method invocations.
 * 
 * If an exception is raised during the invocation of a <strong>void</strong> method in the chain, then the forwarding
 * is stopped, and the exception will be raised up.
 * 
 * Note that if a non-void method is invoked, an {@link IllegalArgumentException} is thrown.
 * 
 * @author Pierre Vigneras
 */
public class Chainer<T> implements InvocationHandler {
  // Object methods, should be handled specially here! We don't want
  // them to be forwarded, but still, we want to reply to them!.
  // See Proxy javadoc for details for list of Object methods that are
  // normally forwarded.
  private static final Method TOSTRING_METHOD;
  private static final Method EQUALS_METHOD;
  private static final Method HASHCODE_METHOD;

  static {
    Method tostringMethodTmp = null;
    Method equalsMethodTmp = null;
    Method hashcodeMethodTmp = null;
    try {
      tostringMethodTmp = Object.class.getMethod("toString");
      equalsMethodTmp = Object.class.getMethod("equals", Object.class);
      hashcodeMethodTmp = Object.class.getMethod("hashCode");
    } catch (final NoSuchMethodException nsme) {
      final String message = ExceptionManager.getInstance().getFullMessage("buc_C_1");
      throw new ExceptionInInitializerError(message);
    }
    TOSTRING_METHOD = tostringMethodTmp;
    EQUALS_METHOD = equalsMethodTmp;
    HASHCODE_METHOD = hashcodeMethodTmp;
  }

  private final List<T> chain = Collections.synchronizedList(new LinkedList<T>());

  public void add(final T element) {
    chain.add(element);
  }

  public void add(final int i, final T element) {
    chain.add(i, element);
  }

  public T remove(final int i) {
    return chain.remove(i);
  }

  public T get(final int i) {
    return chain.get(i);
  }

  public boolean contains(final T element) {
    return chain.contains(element);
  }

  public List<T> getAsList() {
    return new LinkedList<T>(chain);
  }

  @Override
  public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
    if (method.getReturnType().equals(Void.TYPE)) {
      for (final T t : chain) {
        try {
          method.invoke(t, args);
        } catch (final InvocationTargetException e) {
          throw e.getCause();
        }
      }
      return null;
    }
    if (method.equals(TOSTRING_METHOD)) {
      return "ChainerProxy: " + chain;
    } else if (method.equals(HASHCODE_METHOD)) {
      return this.hashCode() + chain.hashCode();
    } else if (method.equals(EQUALS_METHOD) && Proxy.isProxyClass(proxy.getClass())) {
      final InvocationHandler invocationHandler = Proxy.getInvocationHandler(proxy);
      return this.equals(invocationHandler);
    }
    final String message = ExceptionManager.getInstance().getFullMessage("buc_C_2");
    throw new IllegalArgumentException(message);
  }

}