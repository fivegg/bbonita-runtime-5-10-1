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
package org.ow2.bonita.facade.ejb;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ow2.bonita.env.Environment;
import org.ow2.bonita.util.BonitaRuntimeException;
import org.ow2.bonita.util.Command;
import org.ow2.bonita.util.ExceptionManager;
import org.ow2.bonita.util.Misc;

public class SerializedCommand<T> implements Command<T> {

  private static final long serialVersionUID = -3780999471259012607L;
  private byte[] serializedCommand;
  private String className;
  private static final Logger LOG = Logger.getLogger(SerializedCommand.class.getName());

  public SerializedCommand(final Command<?> command) {
    super();
    try {
      this.className = command.getClass().getName();
      this.serializedCommand = Misc.serialize(command);
    } catch (final IOException e) {
      throw new BonitaRuntimeException(e);
    } catch (final ClassNotFoundException e) {
      throw new BonitaRuntimeException(e);
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public T execute(final Environment environment) throws Exception {
    try {
      final ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
      if (LOG.isLoggable(Level.FINE)) {
        LOG.fine("Executing a command in remote mode. Looking for Class: " + this.className + ". Current ClassLoader: "
            + currentClassLoader);
      }
      Class.forName(this.className, true, currentClassLoader);
      if (LOG.isLoggable(Level.FINE)) {
        LOG.fine("Executing a command in remote mode. Class: " + this.className + " found.");
      }

      final Command<T> command = (Command<T>) Misc.deserialize(this.serializedCommand);
      // execute command
      if (LOG.isLoggable(Level.FINE)) {
        LOG.fine("Executing a command in remote mode. Executing deserialized command: " + command);
      }
      return command.execute(environment);
    } catch (final ClassNotFoundException e) {
      if (LOG.isLoggable(Level.FINE)) {
        LOG.fine("Executing a command in remote mode. Class: " + this.className + " not found.");
      }
      final String message = ExceptionManager.getInstance().getFullMessage("baa_SC_1", this.className);
      throw new BonitaRuntimeException(message, e);
    }
  }

}
