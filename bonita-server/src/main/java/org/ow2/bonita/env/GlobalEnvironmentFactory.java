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
 * Modified by Matthieu Chaffotte - BonitaSoft S.A.
 **/
package org.ow2.bonita.env;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ow2.bonita.util.ExceptionManager;
import org.ow2.bonita.util.Misc;
import org.ow2.bonita.util.ServerConstants;

public final class GlobalEnvironmentFactory {

  private static final Logger LOG = Logger.getLogger(GlobalEnvironmentFactory.class.getName());
  private static Map<String, EnvironmentFactory> environmentFactories = new HashMap<String, EnvironmentFactory>();
  /**
   * Property key used to define the environment XML file to parse for the
   * holding of the {@link EnvironmentFactory} and returned by {@link #getEnvironmentFactory()}.
   *
   * This key is set to: <code>org.ow2.bonita.environment</code>
   */
  private static final Object LOCK = new Object();

  private GlobalEnvironmentFactory() { }  

  static class BonitaShutdownHook extends Thread {
    public BonitaShutdownHook() {
      super();
    }
    @Override
    public void run() {
      try {
        if (environmentFactories != null) {
          for (EnvironmentFactory environmentFactory : environmentFactories.values()) {
            if (environmentFactory != null) {
              environmentFactory.close();
            }
          }
          environmentFactories.clear();
        }
      } catch (final Exception ee) {
        LOG.severe(Misc.getStackTraceFrom(ee));
      }
    }
  }

  private static String getEnvironmentResource(final String domain) throws Exception {
    if (domain == null || domain.length() == 0) {
      throw new InvalidEnvironmentException("Unknown domain: '" + domain + "'");
    }
    String confPath = ServerConstants.getTenantConfigurationFolder(domain);
    StringBuilder serverConfigurationPath = new StringBuilder(confPath);
    serverConfigurationPath.append(File.separator).append("bonita-server.xml");

    final String envPath = serverConfigurationPath.toString();
    final File environmentPropertiesFile = new File(envPath);
    if (!environmentPropertiesFile.exists()) {
      throw new FileNotFoundException(envPath);
    }
    return envPath;
  }

  public static EnvironmentFactory getEnvironmentFactory(final String domain) throws Exception {
    synchronized (LOCK) {
      if (environmentFactories.get(domain) == null) {
        String environmentResource = getEnvironmentResource(domain);
        URL url = null;
        final File file = new File(environmentResource);
        if (file.exists()) {
          try {
            // Call toURI.toURL because toURL does not escape special characters.
            // see File.toURL() javadoc.
            url = file.toURI().toURL();
          } catch (final MalformedURLException e1) {
            Misc.unreachableStatement();
          }
        }
        if (LOG.isLoggable(Level.CONFIG)) {
          LOG.config("Reading environment configuration from: " + url);
        }
        final byte[] content;
        try {
          content = Misc.getAllContentFrom(url);
        } catch (final IOException e) {
          String message = ExceptionManager.getInstance().getFullMessage("benv_GEF_1", url);
          throw new InvalidEnvironmentException(message, e);
        }
        final String environment = new String(content);
        if (LOG.isLoggable(Level.CONFIG)) {
          LOG.config("The environment resource " + url + " contains: " + Misc.LINE_SEPARATOR + environment);
        }
        final EnvironmentFactory factory = BonitaEnvironmentParser.parseEnvironmentFactoryFromXmlString(environment);
        setEnvironmentFactory(domain, factory);
        Runtime.getRuntime().addShutdownHook(new BonitaShutdownHook());
      }
      if (environmentFactories.get(domain) == null) {
        String message = ExceptionManager.getInstance().getFullMessage("benv_GEF_2");
        throw new InvalidEnvironmentException(message);
      }
      return environmentFactories.get(domain);
    }
  }

  public static void setEnvironmentFactory(final String domain, final EnvironmentFactory envFactory) {
    synchronized (LOCK) {
      environmentFactories.put(domain, envFactory);
    }
  }

  public static boolean isInitialized(final String domain) {
    return environmentFactories.get(domain) != null;
  }

}
