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
package org.ow2.bonita.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ow2.bonita.env.descriptor.ArgDescriptor;

/**
 * 
 * @author Charles Souillard, Matthieu Chaffotte
 *
 */
public abstract class ReflectUtilDescriptor {

	static final Logger LOG = Logger.getLogger(ReflectUtilDescriptor.class.getName());

  public static Method findMethod(Class<?> clazz, String methodName, List<ArgDescriptor> argDescriptors, Object[] args) {
  	if (LOG.isLoggable(Level.FINE)) {
      LOG.fine("searching for method " + methodName + " in " + clazz.getName());
  	}
    Method[] candidates = clazz.getDeclaredMethods();
    for (int i = 0; i < candidates.length; i++) {
      Method candidate = candidates[i];
      if ((candidate.getName().equals(methodName))
          && (isArgumentMatch(candidate.getParameterTypes(), argDescriptors, args))) {

      	if (LOG.isLoggable(Level.FINE)) {
          LOG.fine("found matching method " + clazz.getName() + "." + methodName);
        }
        return candidate;
      }
    }
    if (clazz.getSuperclass() != null) {
      return findMethod(clazz.getSuperclass(), methodName, argDescriptors, args);
    }
    return null;
  }

  public static Constructor<?> findConstructor(Class<?> clazz, List<ArgDescriptor> argDescriptors, Object[] args) {
    Constructor<?>[] constructors = clazz.getDeclaredConstructors();
    for (int i = 0; i < constructors.length; i++) {
      if (isArgumentMatch(constructors[i].getParameterTypes(), argDescriptors, args)) {
        return constructors[i];
      }
    }
    return null;
  }

  public static boolean isArgumentMatch(Class<?>[] parameterTypes, List<ArgDescriptor> argDescriptors, Object[] args) {
    int nbrOfArgs = 0;
    if (args != null) {
      nbrOfArgs = args.length;
    }
    int nbrOfParameterTypes = 0;
    if (parameterTypes != null) {
      nbrOfParameterTypes = parameterTypes.length;
    }
    if ((nbrOfArgs == 0) && (nbrOfParameterTypes == 0)) {
      return true;
    }
    if (nbrOfArgs != nbrOfParameterTypes) {
      return false;
    }

    for (int i = 0; (i < parameterTypes.length); i++) {
      Class<?> parameterType = parameterTypes[i];
      String argTypeName = (argDescriptors != null ? argDescriptors.get(i).getTypeName() : null);
      if (argTypeName != null) {
        if (!argTypeName.equals(parameterType.getName())) {
          return false;
        }
      } else if ((args[i] != null) && (!parameterType.isAssignableFrom(args[i].getClass()))) {
        return false;
      }
    }
    return true;
  }

  public static String getSignature(String methodName, List<ArgDescriptor> argDescriptors, Object[] args) {
    StringBuilder signature = new StringBuilder(methodName);
    signature.append("(");
    if (args != null) {
      for (int i = 0; i < args.length; i++) {
        String argType = null;
        if (argDescriptors != null) {
          ArgDescriptor argDescriptor = argDescriptors.get(i);
          if ((argDescriptor != null) && (argDescriptor.getTypeName() != null)) {
            argType = argDescriptor.getTypeName();
          }
        }
        if ((argType == null) && (args[i] != null)) {
          argType = args[i].getClass().getName();
        }
        signature.append(argType);
        if (i < (args.length - 1)) {
          signature.append(", ");
        }
      }
    }
    signature.append(")");
    return signature.toString();
  }

}
