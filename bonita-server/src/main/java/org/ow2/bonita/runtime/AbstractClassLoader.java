/**
 * Copyright (C) 2009  BonitaSoft S.A.
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
package org.ow2.bonita.runtime;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.logging.LogFactory;
import org.ow2.bonita.services.LargeDataRepository;
import org.ow2.bonita.util.EnvTool;
import org.ow2.bonita.util.Misc;
import org.ow2.bonita.util.ServerConstants;

/**
 * 
 * @author Charles Souillard, Matthieu Chaffotte, Elias Ricken de Medeiros
 * 
 */
abstract class AbstractClassLoader extends URLClassLoader {

  private static final Logger LOG = Logger.getLogger(AbstractClassLoader.class.getName());

  protected Map<String, byte[]> otherResources;
  protected Set<URL> urls;
  private boolean isReleasing = false;

  static {
    // Setting useCaches to false avoids a memory leak of URLJarFile instances
    // It's a workaround for a Sun bug (see bug id 4167874 - fixed in jdk 1.7).
    // Otherwise,
    // URLJarFiles will never be garbage collected.
    // o.a.g.deployment.util.DeploymentUtil.readAll()
    // causes URLJarFiles to be created
    try {
      // Protocol/file shouldn't matter.
      // As long as we don't get an input/output stream, no operations should
      // occur...
      if (Misc.isOnWindows()) {
        if (LOG.isLoggable(Level.WARNING)) {
          LOG.warning("Running on Windows. Deactivating cache");
        }
        new URL("http://a").openConnection().setDefaultUseCaches(false);
      }
    } catch (final IOException ioe) {
      // Can't Log this. Should we send to STDOUT/STDERR?
    }
  }

  AbstractClassLoader(final List<String> categories, final ClassLoader parent) {
    super(new URL[] {}, parent);
    final LargeDataRepository ldr = EnvTool.getLargeDataRepository();
    final Map<String, byte[]> resources = ldr.getData(byte[].class, categories);
    if (LOG.isLoggable(Level.FINE)) {
      LOG.fine("Creating a new AbstratctClassLoader...");
    }
    if (resources != null) {
      otherResources = new HashMap<String, byte[]>();
      urls = new HashSet<URL>();
      for (final Map.Entry<String, byte[]> resource : resources.entrySet()) {
        if (resource.getKey().matches(".*\\.jar")) {
          final byte[] data = ldr.getData(byte[].class, categories, resource.getKey());
          try {
            final File tmpDir = Misc.createDirectories(ServerConstants.getTenantTemporaryFolder(EnvTool.getDomain()));
            final File file = Misc.createTempFile(resource.getKey(), null, tmpDir);
            Misc.write(file, data);
            final String path = file.getAbsolutePath();
            final URL url = new File(path).toURL();
            urls.add(url);
            addURL(url);
            file.deleteOnExit();
          } catch (final MalformedURLException e) {
            e.printStackTrace();
          } catch (final Exception e) {
            e.printStackTrace();
          }
        } else {
          otherResources.put(resource.getKey(), resource.getValue());
        }
      }
      if (otherResources.isEmpty()) {
        otherResources = null;
      }
    }
  }

  @Override
  public InputStream getResourceAsStream(final String name) {
    InputStream is = getInternalInputstream(name);
    if (is == null && name.startsWith("/")) {
      is = getInternalInputstream(name.substring(1));
    }
    return is;
  }

  private InputStream getInternalInputstream(final String name) {
    final byte[] classData = loadProcessResource(name);
    if (classData != null) {
      return new ByteArrayInputStream(classData);
    }
    return super.getResourceAsStream(name);
  }

  private byte[] loadProcessResource(final String resourceName) {
    if (otherResources == null) {
      return null;
    }
    return otherResources.get(resourceName);
  }

  @Override
  protected synchronized Class<?> loadClass(final String name, final boolean resolve) throws ClassNotFoundException {
    Class<?> c = findLoadedClass(name);
    if (c == null && !isReleasing) {
      try {
        c = findClass(name);
      } catch (final ClassNotFoundException e) {
      } catch (final IllegalStateException e) {
      }
    }
    if (c == null) {
      c = getParent().loadClass(name);
    }
    if (resolve) {
      resolveClass(c);
    }
    if (LOG.isLoggable(Level.FINE)) {
      LOG.fine("loadClass: " + name + ", result: " + c);
    }
    return c;
  }

  public void release() {
    isReleasing = true;
    LogFactory.release(this);
    for (final URL url : urls) {
      if (Misc.isOnWindows()) {
        releaseConnection(url);
      }
      final File removeFile = new File(url.getFile());
      removeFile.delete();
    }
  }

  private void releaseConnection(final URL url) {
    if (LOG.isLoggable(Level.INFO)) {
      LOG.info("Releasing class loader: " + this);
    }
    try {
      final URLConnection conn = url.openConnection();
      if (LOG.isLoggable(Level.INFO)) {
        LOG.info("Getting connection of url: " + url + ", conn=" + conn);
      }
      final String fileURLConnectionClassName = "sun.net.www.protocol.file.FileURLConnection";
      if (conn instanceof JarURLConnection) {
        final JarFile jarfile = ((JarURLConnection) conn).getJarFile();
        if (LOG.isLoggable(Level.INFO)) {
          LOG.info("Closing jar file: " + jarfile.getName());
        }
        jarfile.close();
      } else if (conn.getClass().getName().equals(fileURLConnectionClassName)) {
        if (LOG.isLoggable(Level.INFO)) {
          LOG.info("Closing connection (" + fileURLConnectionClassName + ": " + conn);
        }
        final Method close = conn.getClass().getMethod("close", (Class[]) null);
        close.invoke(conn, (Object[]) null);
      }
    } catch (final Exception e) {
      if (LOG.isLoggable(Level.WARNING)) {
        LOG.warning("Error while releasing classloader: " + this + ": " + Misc.getStackTraceFrom(e));
      }
      e.printStackTrace();
    }
    closeClassLoader();
  }

  private void closeClassLoader() {
    try {
      final Class<?> clazz = URLClassLoader.class;
      final Field ucp = clazz.getDeclaredField("ucp");
      ucp.setAccessible(true);
      final Object sun_misc_URLClassPath = ucp.get(this);
      final Field loaders = sun_misc_URLClassPath.getClass().getDeclaredField("loaders");
      loaders.setAccessible(true);
      final Object java_util_Collection = loaders.get(sun_misc_URLClassPath);
      for (final Object sun_misc_URLClassPath_JarLoader : ((Collection<?>) java_util_Collection).toArray()) {
        try {
          final java.lang.reflect.Field loader = sun_misc_URLClassPath_JarLoader.getClass().getDeclaredField("jar");
          loader.setAccessible(true);
          final Object java_util_jar_JarFile = loader.get(sun_misc_URLClassPath_JarLoader);
          ((java.util.jar.JarFile) java_util_jar_JarFile).close();
        } catch (final Throwable t) {
          // if we got this far, this is probably not a JAR loader so skip it
        }
      }
    } catch (final Throwable t) {
      // probably not a SUN VM
    }
    return;
  }

}
