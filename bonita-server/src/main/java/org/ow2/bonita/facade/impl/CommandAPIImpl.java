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
 **/
package org.ow2.bonita.facade.impl;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.ow2.bonita.env.Environment;
import org.ow2.bonita.facade.CommandAPI;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.util.Command;
import org.ow2.bonita.util.EnvTool;

public class CommandAPIImpl implements CommandAPI {

  private static final Logger LOG = Logger.getLogger(CommandAPIImpl.class.getName());

  protected CommandAPIImpl(final String queryList) {
  }

  @Override
  public <T> T execute(final Command<T> command) throws Exception {
    return executeCommand(command, null);
  }

  @Override
  public <T> T execute(final Command<T> command, final ProcessDefinitionUUID processUUID) throws Exception {
    return executeCommand(command, processUUID);
  }

  private <T> T executeCommand(final Command<T> command, final ProcessDefinitionUUID processUUID) throws Exception {
    final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();

    if (LOG.isLoggable(Level.FINE)) {
      LOG.fine("Executing command: " + command + ". ProcessUUID: " + processUUID);
    }

    ClassLoader classLoader = null;

    if (processUUID != null) {
      // the command was deployed at process level
      classLoader = EnvTool.getClassDataLoader().getProcessClassLoader(processUUID);
      if (LOG.isLoggable(Level.FINE)) {
        LOG.fine("Executing a command. processUUID is not null, processClassLoader found: " + classLoader);
      }
    }

    if (classLoader == null) {
      classLoader = EnvTool.getClassDataLoader().getCommonClassLoader();
      if (LOG.isLoggable(Level.FINE)) {
        LOG.fine("Executing a command. commonClassLoader found: " + classLoader);
      }
    }

    if (classLoader == null) {
      classLoader = contextClassLoader;
      if (LOG.isLoggable(Level.FINE)) {
        LOG.fine("Executing a command. commonClassLoader not found, taking the contextClassLoader: " + classLoader);
      }
    }

    try {
      if (LOG.isLoggable(Level.FINE)) {
        LOG.fine("Executing a command. Setting current classloader to: " + classLoader);
      }
      Thread.currentThread().setContextClassLoader(classLoader);
      return command.execute(Environment.getCurrent());
    } finally {
      Thread.currentThread().setContextClassLoader(contextClassLoader);
    }
  }

}
