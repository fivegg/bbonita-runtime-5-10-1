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

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ow2.bonita.env.Environment;

public abstract class ReflectUtil {

	static final Logger LOG = Logger.getLogger(ReflectUtil.class.getName());

  static ClassLoader resolveClassLoader(ClassLoader classLoader) {
    // 1) if the user provided a classloader through the API, use that one
    if (classLoader != null) {
    	if (LOG.isLoggable(Level.FINE)) {
        LOG.fine("using provided classloader");
    	}
      return classLoader;
    }

    // 2) if the user provided a classloader through the environment, use that
    // one
    Environment environment = Environment.getCurrent();
    if (environment != null) {
      classLoader = environment.getClassLoader();
      if (classLoader != null) {
      	if (LOG.isLoggable(Level.FINE)) {
          LOG.fine("using environment classloader");
      	}
        return classLoader;
      }
    }

    // 3) otherwise, use the current thread's context classloader
    if (LOG.isLoggable(Level.FINE)) {
      LOG.fine("using context classloader");
    }
    return Thread.currentThread().getContextClassLoader();
  }

  public static Class<?> loadClass(ClassLoader classLoader, String className) {
    try {
      classLoader = resolveClassLoader(classLoader);
      if (LOG.isLoggable(Level.FINE)) {
        LOG.fine("loading class " + className);
      }
      return classLoader.loadClass(className);
    } catch (NoClassDefFoundError e) {
    	String message = ExceptionManager.getInstance().getFullMessage("bp_RU_1", className);
      throw new BonitaRuntimeException(message, e);
    } catch (ClassNotFoundException e) {
    	String message = ExceptionManager.getInstance().getFullMessage("bp_RU_2", className);
      throw new BonitaRuntimeException(message, e);
    }
  }

  public static InputStream getResourceAsStream(ClassLoader classLoader,
      String resource) {
    classLoader = resolveClassLoader(classLoader);
    if (LOG.isLoggable(Level.FINE)) {
      LOG.fine("getting resource as stream " + resource);
    }
    return classLoader.getResourceAsStream(resource);
  }

  public static Enumeration<URL> getResources(ClassLoader classLoader,
      String resource) {
    classLoader = resolveClassLoader(classLoader);
    try {
    	if (LOG.isLoggable(Level.FINE)) {
        LOG.fine("getting resources " + resource);
    	}
      return classLoader.getResources(resource);
    } catch (Exception e) {
    	String message = ExceptionManager.getInstance().getFullMessage("bp_RU_3", resource);
      throw new BonitaRuntimeException(message, e);
    }
  }

  public static URL getResource(ClassLoader classLoader, String resource) {
    classLoader = resolveClassLoader(classLoader);
    try {
    	if (LOG.isLoggable(Level.FINE)) {
        LOG.fine("getting resource " + resource);
    	}
      return classLoader.getResource(resource);
    } catch (Exception e) {
    	String message = ExceptionManager.getInstance().getFullMessage("bp_RU_4", resource);
      throw new BonitaRuntimeException(message, e);
    }
  }

  public static Object instantiate(ClassLoader classLoader, String className) {
    Object newObject;
    try {
      classLoader = resolveClassLoader(classLoader);
      Class<?> clazz = loadClass(classLoader, className);
      if (LOG.isLoggable(Level.FINE)) {
        LOG.fine("instantiating " + className);
      }
      newObject = clazz.newInstance();
    } catch (Exception e) {
    	String message = ExceptionManager.getInstance().getFullMessage("bp_RU_5", className);
      throw new BonitaRuntimeException(message, e);
    }
    return newObject;
  }

  public static Class<?>[] loadClasses(ClassLoader classLoader,
      List<String> constructorArgTypeNames) {
    if (constructorArgTypeNames == null)
      return null;
    Class<?>[] classes = new Class[constructorArgTypeNames.size()];
    for (int i = 0; i < constructorArgTypeNames.size(); i++) {
      classLoader = resolveClassLoader(classLoader);
      classes[i] = loadClass(classLoader, constructorArgTypeNames.get(i));
    }
    return classes;
  }

  public static <T> Constructor<T> getConstructor(Class<T> clazz,
      Class<?>[] parameterTypes) {
    Constructor<T> constructor = null;
    try {
      constructor = clazz.getDeclaredConstructor(parameterTypes);

      if (LOG.isLoggable(Level.FINE)) {
        LOG.fine("found constructor " + clazz.getName() + "(" + ArrayUtil.toString(parameterTypes) + ")");
      }
    } catch (SecurityException e) {
    	String message = ExceptionManager.getInstance().getFullMessage(
    			"bp_RU_6", clazz.getName(), getParameterTypesText(parameterTypes));
      throw new BonitaRuntimeException(message, e);
    } catch (NoSuchMethodException e) {
    	String message = ExceptionManager.getInstance().getFullMessage(
    			"bp_RU_7", clazz.getName(), getParameterTypesText(parameterTypes));
      throw new BonitaRuntimeException(message, e);
    }

    return constructor;
  }

  public static Field getField(Class<?> clazz, String fieldName) {
    return getField(clazz, fieldName, clazz);
  }

  private static Field getField(Class<?> clazz, String fieldName,
      Class<?> original) {
    Field field = null;

    try {
      field = clazz.getDeclaredField(fieldName);
      if (LOG.isLoggable(Level.FINE)) {
        LOG.fine("found field " + fieldName + " in " + clazz.getName());
      }
    } catch (SecurityException e) {
    	String message = ExceptionManager.getInstance().getFullMessage(
    			"bp_RU_8",  clazz.getName(), fieldName);
      throw new BonitaRuntimeException(message, e);
    } catch (NoSuchFieldException e) {
      if (clazz.getSuperclass() != null) {
        return getField(clazz.getSuperclass(), fieldName, original);
      } else {
      	String message = ExceptionManager.getInstance().getFullMessage(
      			"bp_RU_9", original.getName(), fieldName);
        throw new BonitaRuntimeException(message, e);
      }
    }

    return field;
  }

  public static Method getMethod(Class<?> clazz, String methodName,
      Class<?>[] parameterTypes) {
    return getMethod(clazz, methodName, parameterTypes, clazz);
  }

  private static Method getMethod(Class<?> clazz, String methodName,
      Class<?>[] parameterTypes, Class<?> original) {
    Method method = null;

    try {
      method = clazz.getDeclaredMethod(methodName, parameterTypes);

      if (LOG.isLoggable(Level.FINE)) {
        LOG.fine("found method " + clazz.getName() + "." + methodName + "(" + ArrayUtil.toString(parameterTypes) + ")");
      }

    } catch (SecurityException e) {
    	String message = ExceptionManager.getInstance().getFullMessage(
    			"bp_RU_10", clazz.getName(), methodName, getParameterTypesText(parameterTypes));
      throw new BonitaRuntimeException(message, e);
    } catch (NoSuchMethodException e) {
      if (clazz.getSuperclass() != null) {
        return getMethod(clazz.getSuperclass(), methodName, parameterTypes,
            original);
      } else {
      	String message = ExceptionManager.getInstance().getFullMessage(
      			"bp_RU_11", original.getName(), methodName, getParameterTypesText(parameterTypes));
        throw new BonitaRuntimeException(message, e);
      }
    }

    return method;
  }

  private static String getParameterTypesText(Class<?>[] parameterTypes) {
    if (parameterTypes == null)
      return "";
    StringBuffer parametersTypeText = new StringBuffer();
    for (int i = 0; i < parameterTypes.length; i++) {
      Class<?> parameterType = parameterTypes[i];
      parametersTypeText.append(parameterType.getName());
      if (i != parameterTypes.length - 1) {
        parametersTypeText.append(", ");
      }
    }
    return parametersTypeText.toString();
  }

  public static <T> T newInstance(Class<T> clazz) {
    return newInstance(clazz, null, null);
  }

  public static <T> T newInstance(Constructor<T> constructor) {
    return newInstance(null, constructor, null);
  }

  public static <T> T newInstance(Constructor<T> constructor, Object[] args) {
    return newInstance(null, constructor, args);
  }

  private static <T> T newInstance(Class<T> clazz, Constructor<T> constructor,
      Object[] args) {
    if ((clazz == null) && (constructor == null)) {
    	String message = ExceptionManager.getInstance().getFullMessage("bp_RU_12");
      throw new IllegalArgumentException(message);
    }

    String className = null;
    try {
    	if (LOG.isLoggable(Level.FINE)) {
        LOG.fine("creating new instance for class '" + className + "' with args " + ArrayUtil.toString(args));
    	}
      if (constructor == null) {
      	if (LOG.isLoggable(Level.FINE)) {
          LOG.fine("getting default constructor");
      	}
        constructor = clazz.getConstructor((Class[]) null);
      }
      className = constructor.getDeclaringClass().getName();
      if (!constructor.isAccessible()) {
      	if (LOG.isLoggable(Level.FINE)) {
          LOG.fine("making constructor accessible");
      	}
        constructor.setAccessible(true);
      }
      return constructor.newInstance(args);

    } catch (Throwable t) {
    	String message = ExceptionManager.getInstance().getFullMessage(
    			"bp_RU_13", className, ArrayUtil.toString(args));
      throw new BonitaRuntimeException(message, t);
    }
  }

  public static Object get(Field field, Object object) {
    if (field == null) {
    	String message = ExceptionManager.getInstance().getFullMessage("bp_RU_14");
      throw new NullPointerException(message);
    }
    try {
      Object value = field.get(object);
      if (LOG.isLoggable(Level.FINE)) {
        LOG.fine("got value '" + value + "' from field '" + field.getName() + "'");
      }
      return value;
    } catch (Exception e) {
    	String message = ExceptionManager.getInstance().getFullMessage(
    			"bp_RU_15", field.getName());
      throw new BonitaRuntimeException(message, e);
    }
  }

  public static void set(Field field, Object object, Object value) {
    if (field == null) {
    	String message = ExceptionManager.getInstance().getFullMessage("bp_RU_16");
      throw new NullPointerException(message);
    }
    try {
    	if (LOG.isLoggable(Level.FINE)) {
        LOG.fine("setting field '" + field.getName() + "' to value '" + value + "'");
    	}
      if (!field.isAccessible()) {
      	if (LOG.isLoggable(Level.FINE)) {
          LOG.fine("making field accessible");
      	}
        field.setAccessible(true);
      }
      field.set(object, value);
    } catch (Exception e) {
    	String message = ExceptionManager.getInstance().getFullMessage(
    			"bp_RU_17", field.getName(), value);
      throw new BonitaRuntimeException(message, e);
    }
  }

  public static Object invoke(Method method, Object target, Object[] args) {
    if (method == null) {
    	String message = ExceptionManager.getInstance().getFullMessage(
    			"bp_RU_18");
      throw new BonitaRuntimeException(message);
    }
    try {
    	if (LOG.isLoggable(Level.FINE)) {
        LOG.fine("invoking '" + method.getName() + "' on '" + target + "' with " + ArrayUtil.toString(args));
    	}
      if (!method.isAccessible()) {
      	if (LOG.isLoggable(Level.FINE)) {
          LOG.fine("making method accessible");
      	}
        method.setAccessible(true);
      }
      return method.invoke(target, args);
    } catch (InvocationTargetException e) {
      Throwable targetException = e.getTargetException();
      
      String message = ExceptionManager.getInstance().getFullMessage(
      		"bp_RU_19", method.getName(), ArrayUtil.toString(args), target, targetException.getMessage());
      throw new BonitaRuntimeException(message, targetException);
    } catch (Exception e) {
    	String message = ExceptionManager.getInstance().getFullMessage(
    			"bp_RU_20", method.getName(), ArrayUtil.toString(args), target, e.getMessage());
      throw new BonitaRuntimeException(message, e);
    }
  }

  public static String getUnqualifiedClassName(Class<?> clazz) {
    if (clazz == null) {
      return null;
    }
    return getUnqualifiedClassName(clazz.getSimpleName());
  }

  public static String getUnqualifiedClassName(String className) {
    if (className == null) {
      return null;
    }
    int dotIndex = className.lastIndexOf('.');
    if (dotIndex != -1) {
      className = className.substring(dotIndex + 1);
    }
    return className;
  }
}
