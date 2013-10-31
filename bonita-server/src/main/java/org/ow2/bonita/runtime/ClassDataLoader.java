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
 * Modified by Matthieu Chaffotte, Charles Souillard - BonitaSoft S.A.
 **/
package org.ow2.bonita.runtime;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ow2.bonita.facade.def.element.ConnectorDefinition;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.util.BonitaRuntimeException;
import org.ow2.bonita.util.ExceptionManager;
import org.ow2.bonita.util.Misc;
import org.ow2.bonita.util.ReflectUtil;

/**
 * This class must be in the environment for multi tenancy reasons (else the map of processes will generate conflicts
 * between two processes with the same UUID in two different tenants.
 * 
 */
public final class ClassDataLoader {

  private static final Logger LOG = Logger.getLogger(ClassDataLoader.class.getName());

  private final Map<ProcessDefinitionUUID, ProcessClassLoader> processClassLoaders = new HashMap<ProcessDefinitionUUID, ProcessClassLoader>();
  private final VirtualCommonClassloader virtualCommonClassloader = new VirtualCommonClassloader();

  public Class<?> getClass(final ProcessDefinitionUUID processUUID, final String className)
      throws ClassNotFoundException {
    if (LOG.isLoggable(Level.FINE)) {
      LOG.fine("Looking for class " + className + ", in process : " + processUUID);
    }

    Class<?> result = null;

    if (processUUID != null) {
      result = lookIntoProcessClassLoader(processUUID, className);
    }
    if (result != null) {
      return result;
    }
    if (LOG.isLoggable(Level.FINE)) {
      LOG.fine("Class " + className + " not found in packageClassLoaders...");
    }
    result = lookIntoCommonClassLoader(className);
    if (result != null) {
      return result;
    }
    if (LOG.isLoggable(Level.FINE)) {
      LOG.fine("Class " + className + " not found in globalClassLoaders...");
    }
    // maybe it is present in the current classLoader ? It may be a class delivered in bonita jar ?
    return ClassDataLoader.class.getClassLoader().loadClass(className);
  }

  public Object getInstance(final ProcessDefinitionUUID processUUID, final String className) {
    try {
      final Class<?> clazz = getClass(processUUID, className);
      return getClassInstance(clazz);
    } catch (final ClassNotFoundException e) {
      final String message = ExceptionManager.getInstance().getFullMessage("be_CDL_1", className);
      throw new BonitaRuntimeException(message);
    }
  }

  protected Object getInstance(final ProcessDefinitionUUID processUUID, final ConnectorDefinition connector) {
    final String className = connector.getClassName();
    return getInstance(processUUID, className);
  }

  private Object getClassInstance(final Class<?> clazz) {
    final Object obj = ReflectUtil.newInstance(clazz);
    if (obj == null) {
      final String message = ExceptionManager.getInstance().getFullMessage("be_CDL_2", clazz.getName());
      throw new BonitaRuntimeException(message);
    }
    return obj;
  }

  private Class<?> lookIntoProcessClassLoader(final ProcessDefinitionUUID processUUID, final String className) {
    final ClassLoader classLoader = getProcessClassLoader(processUUID);
    try {
      final Class<?> result = classLoader.loadClass(className);
      if (result != null && result.getClassLoader().equals(classLoader)) {
        return result;
      }
    } catch (final Throwable e) {
      return null;
    }
    return null;
  }

  private synchronized Class<?> lookIntoCommonClassLoader(final String className) {
    try {
      return virtualCommonClassloader.loadClass(className);
    } catch (final ClassNotFoundException e) {
      return null;
    }
  }

  public ClassLoader getProcessClassLoader(final ProcessDefinitionUUID processUUID) {
    if (!processClassLoaders.containsKey(processUUID)) {
      processClassLoaders.put(processUUID, new ProcessClassLoader(processUUID));
    }
    return processClassLoaders.get(processUUID);
  }

  @SuppressWarnings("unchecked")
  public <T> T getInstance(final Class<T> clazz, final ProcessDefinitionUUID processUUID,
      final ConnectorDefinition connector) {
    Misc.checkArgsNotNull(clazz, connector);
    final Object obj = getInstance(processUUID, connector);
    if (obj.getClass().isAssignableFrom(clazz)) {
      final String message = ExceptionManager.getInstance().getFullMessage("be_CDL_4", connector, clazz.getName(), obj);
      throw new BonitaRuntimeException(message);
    }
    return (T) obj;
  }

  public void removeProcessClassLoader(final ProcessDefinitionUUID processUUID) {
    final ProcessClassLoader loader = processClassLoaders.remove(processUUID);
    if (loader != null) {
      loader.release();
    }
  }

  public void clear() {
    for (final ProcessClassLoader loader : processClassLoaders.values()) {
      loader.release();
    }
    processClassLoaders.clear();
    virtualCommonClassloader.cleanCommonClassLoader();
  }

  public Set<ProcessDefinitionUUID> getActiveProcessClassLoaders() {
    return processClassLoaders.keySet();
  }

  public CommonClassLoader getCommonClassLoader() {
    return virtualCommonClassloader.getCommonClassLoader();
  }

  public synchronized void resetCommonClassloader() {
    virtualCommonClassloader.resetCommonClassloader();
  }

}
